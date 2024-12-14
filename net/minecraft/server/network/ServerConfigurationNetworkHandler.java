/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 * 
 * Could not load the following classes:
 *  org.jetbrains.annotations.Nullable
 */
package net.minecraft.server.network;

import com.mojang.authlib.GameProfile;
import com.mojang.logging.LogUtils;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import net.minecraft.network.ClientConnection;
import net.minecraft.network.DisconnectionInfo;
import net.minecraft.network.NetworkThreadUtils;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.listener.ServerConfigurationPacketListener;
import net.minecraft.network.listener.TickablePacketListener;
import net.minecraft.network.packet.BrandCustomPayload;
import net.minecraft.network.packet.c2s.common.ClientOptionsC2SPacket;
import net.minecraft.network.packet.c2s.common.ResourcePackStatusC2SPacket;
import net.minecraft.network.packet.c2s.common.SyncedClientOptions;
import net.minecraft.network.packet.c2s.config.ReadyC2SPacket;
import net.minecraft.network.packet.c2s.config.SelectKnownPacksC2SPacket;
import net.minecraft.network.packet.s2c.common.CustomPayloadS2CPacket;
import net.minecraft.network.packet.s2c.common.DisconnectS2CPacket;
import net.minecraft.network.packet.s2c.common.ServerLinksS2CPacket;
import net.minecraft.network.packet.s2c.config.FeaturesS2CPacket;
import net.minecraft.network.state.PlayStateFactories;
import net.minecraft.registry.CombinedDynamicRegistries;
import net.minecraft.registry.ServerDynamicRegistryType;
import net.minecraft.registry.VersionedIdentifier;
import net.minecraft.resource.featuretoggle.FeatureFlags;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.PlayerManager;
import net.minecraft.server.ServerLinks;
import net.minecraft.server.network.ConnectedClientData;
import net.minecraft.server.network.JoinWorldTask;
import net.minecraft.server.network.SendResourcePackTask;
import net.minecraft.server.network.ServerCommonNetworkHandler;
import net.minecraft.server.network.ServerPlayerConfigurationTask;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.network.SynchronizeRegistriesTask;
import net.minecraft.text.Text;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;

public class ServerConfigurationNetworkHandler
extends ServerCommonNetworkHandler
implements ServerConfigurationPacketListener,
TickablePacketListener {
    private static final Logger LOGGER = LogUtils.getLogger();
    private static final Text INVALID_PLAYER_DATA_TEXT = Text.translatable("multiplayer.disconnect.invalid_player_data");
    private final GameProfile profile;
    private final Queue<ServerPlayerConfigurationTask> tasks = new ConcurrentLinkedQueue<ServerPlayerConfigurationTask>();
    @Nullable
    private ServerPlayerConfigurationTask currentTask;
    private SyncedClientOptions syncedOptions;
    @Nullable
    private SynchronizeRegistriesTask synchronizedRegistriesTask;

    public ServerConfigurationNetworkHandler(MinecraftServer minecraftServer, ClientConnection arg, ConnectedClientData arg2) {
        super(minecraftServer, arg, arg2);
        this.profile = arg2.gameProfile();
        this.syncedOptions = arg2.syncedOptions();
    }

    @Override
    protected GameProfile getProfile() {
        return this.profile;
    }

    @Override
    public void onDisconnected(DisconnectionInfo info) {
        LOGGER.info("{} lost connection: {}", (Object)this.profile, (Object)info.reason().getString());
        super.onDisconnected(info);
    }

    @Override
    public boolean isConnectionOpen() {
        return this.connection.isOpen();
    }

    public void sendConfigurations() {
        this.sendPacket(new CustomPayloadS2CPacket(new BrandCustomPayload(this.server.getServerModName())));
        ServerLinks lv = this.server.getServerLinks();
        if (!lv.isEmpty()) {
            this.sendPacket(new ServerLinksS2CPacket(lv.getLinks()));
        }
        CombinedDynamicRegistries<ServerDynamicRegistryType> lv2 = this.server.getCombinedDynamicRegistries();
        List<VersionedIdentifier> list = this.server.getResourceManager().streamResourcePacks().flatMap(pack -> pack.getInfo().knownPackInfo().stream()).toList();
        this.sendPacket(new FeaturesS2CPacket(FeatureFlags.FEATURE_MANAGER.toId(this.server.getSaveProperties().getEnabledFeatures())));
        this.synchronizedRegistriesTask = new SynchronizeRegistriesTask(list, lv2);
        this.tasks.add(this.synchronizedRegistriesTask);
        this.queueSendResourcePackTask();
        this.tasks.add(new JoinWorldTask());
        this.pollTask();
    }

    public void endConfiguration() {
        this.tasks.add(new JoinWorldTask());
        this.pollTask();
    }

    private void queueSendResourcePackTask() {
        this.server.getResourcePackProperties().ifPresent(properties -> this.tasks.add(new SendResourcePackTask((MinecraftServer.ServerResourcePackProperties)properties)));
    }

    @Override
    public void onClientOptions(ClientOptionsC2SPacket packet) {
        this.syncedOptions = packet.options();
    }

    @Override
    public void onResourcePackStatus(ResourcePackStatusC2SPacket packet) {
        super.onResourcePackStatus(packet);
        if (packet.status().hasFinished()) {
            this.onTaskFinished(SendResourcePackTask.KEY);
        }
    }

    @Override
    public void onSelectKnownPacks(SelectKnownPacksC2SPacket packet) {
        NetworkThreadUtils.forceMainThread(packet, this, this.server);
        if (this.synchronizedRegistriesTask == null) {
            throw new IllegalStateException("Unexpected response from client: received pack selection, but no negotiation ongoing");
        }
        this.synchronizedRegistriesTask.onSelectKnownPacks(packet.knownPacks(), this::sendPacket);
        this.onTaskFinished(SynchronizeRegistriesTask.KEY);
    }

    @Override
    public void onReady(ReadyC2SPacket packet) {
        NetworkThreadUtils.forceMainThread(packet, this, this.server);
        this.onTaskFinished(JoinWorldTask.KEY);
        this.connection.transitionOutbound(PlayStateFactories.S2C.bind(RegistryByteBuf.makeFactory(this.server.getRegistryManager())));
        try {
            PlayerManager lv = this.server.getPlayerManager();
            if (lv.getPlayer(this.profile.getId()) != null) {
                this.disconnect(PlayerManager.DUPLICATE_LOGIN_TEXT);
                return;
            }
            Text lv2 = lv.checkCanJoin(this.connection.getAddress(), this.profile);
            if (lv2 != null) {
                this.disconnect(lv2);
                return;
            }
            ServerPlayerEntity lv3 = lv.createPlayer(this.profile, this.syncedOptions);
            lv.onPlayerConnect(this.connection, lv3, this.createClientData(this.syncedOptions));
        } catch (Exception exception) {
            LOGGER.error("Couldn't place player in world", exception);
            this.connection.send(new DisconnectS2CPacket(INVALID_PLAYER_DATA_TEXT));
            this.connection.disconnect(INVALID_PLAYER_DATA_TEXT);
        }
    }

    @Override
    public void tick() {
        this.baseTick();
    }

    private void pollTask() {
        if (this.currentTask != null) {
            throw new IllegalStateException("Task " + this.currentTask.getKey().id() + " has not finished yet");
        }
        if (!this.isConnectionOpen()) {
            return;
        }
        ServerPlayerConfigurationTask lv = this.tasks.poll();
        if (lv != null) {
            this.currentTask = lv;
            lv.sendPacket(this::sendPacket);
        }
    }

    private void onTaskFinished(ServerPlayerConfigurationTask.Key key) {
        ServerPlayerConfigurationTask.Key lv;
        ServerPlayerConfigurationTask.Key key2 = lv = this.currentTask != null ? this.currentTask.getKey() : null;
        if (!key.equals(lv)) {
            throw new IllegalStateException("Unexpected request for task finish, current task: " + String.valueOf(lv) + ", requested: " + String.valueOf(key));
        }
        this.currentTask = null;
        this.pollTask();
    }
}

