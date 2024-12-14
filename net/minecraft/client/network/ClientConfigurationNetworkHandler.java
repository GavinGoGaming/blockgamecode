/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 * 
 * Could not load the following classes:
 *  net.fabricmc.api.EnvType
 *  net.fabricmc.api.Environment
 *  org.jetbrains.annotations.Nullable
 */
package net.minecraft.client.network;

import com.mojang.authlib.GameProfile;
import com.mojang.logging.LogUtils;
import java.util.List;
import java.util.function.Function;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.hud.ChatHud;
import net.minecraft.client.network.ClientCommonNetworkHandler;
import net.minecraft.client.network.ClientConnectionState;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.client.network.ClientRegistries;
import net.minecraft.client.resource.ClientDataPackManager;
import net.minecraft.network.ClientConnection;
import net.minecraft.network.DisconnectionInfo;
import net.minecraft.network.NetworkThreadUtils;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.listener.ClientConfigurationPacketListener;
import net.minecraft.network.listener.TickablePacketListener;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.network.packet.c2s.config.ReadyC2SPacket;
import net.minecraft.network.packet.c2s.config.SelectKnownPacksC2SPacket;
import net.minecraft.network.packet.s2c.common.SynchronizeTagsS2CPacket;
import net.minecraft.network.packet.s2c.config.DynamicRegistriesS2CPacket;
import net.minecraft.network.packet.s2c.config.FeaturesS2CPacket;
import net.minecraft.network.packet.s2c.config.ReadyS2CPacket;
import net.minecraft.network.packet.s2c.config.ResetChatS2CPacket;
import net.minecraft.network.packet.s2c.config.SelectKnownPacksS2CPacket;
import net.minecraft.network.state.PlayStateFactories;
import net.minecraft.registry.DynamicRegistryManager;
import net.minecraft.registry.VersionedIdentifier;
import net.minecraft.resource.LifecycledResourceManager;
import net.minecraft.resource.ResourceFactory;
import net.minecraft.resource.featuretoggle.FeatureFlags;
import net.minecraft.resource.featuretoggle.FeatureSet;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;

@Environment(value=EnvType.CLIENT)
public class ClientConfigurationNetworkHandler
extends ClientCommonNetworkHandler
implements ClientConfigurationPacketListener,
TickablePacketListener {
    private static final Logger LOGGER = LogUtils.getLogger();
    private final GameProfile profile;
    private FeatureSet enabledFeatures;
    private final DynamicRegistryManager.Immutable registryManager;
    private final ClientRegistries clientRegistries = new ClientRegistries();
    @Nullable
    private ClientDataPackManager dataPackManager;
    @Nullable
    protected ChatHud.ChatState chatState;

    public ClientConfigurationNetworkHandler(MinecraftClient arg, ClientConnection arg2, ClientConnectionState arg3) {
        super(arg, arg2, arg3);
        this.profile = arg3.localGameProfile();
        this.registryManager = arg3.receivedRegistries();
        this.enabledFeatures = arg3.enabledFeatures();
        this.chatState = arg3.chatState();
    }

    @Override
    public boolean isConnectionOpen() {
        return this.connection.isOpen();
    }

    @Override
    protected void onCustomPayload(CustomPayload payload) {
        this.handleCustomPayload(payload);
    }

    private void handleCustomPayload(CustomPayload payload) {
        LOGGER.warn("Unknown custom packet payload: {}", (Object)payload.getId().id());
    }

    @Override
    public void onDynamicRegistries(DynamicRegistriesS2CPacket packet) {
        NetworkThreadUtils.forceMainThread(packet, this, this.client);
        this.clientRegistries.putDynamicRegistry(packet.registry(), packet.entries());
    }

    @Override
    public void onSynchronizeTags(SynchronizeTagsS2CPacket packet) {
        NetworkThreadUtils.forceMainThread(packet, this, this.client);
        this.clientRegistries.putTags(packet.getGroups());
    }

    @Override
    public void onFeatures(FeaturesS2CPacket packet) {
        this.enabledFeatures = FeatureFlags.FEATURE_MANAGER.featureSetOf(packet.features());
    }

    @Override
    public void onSelectKnownPacks(SelectKnownPacksS2CPacket packet) {
        NetworkThreadUtils.forceMainThread(packet, this, this.client);
        if (this.dataPackManager == null) {
            this.dataPackManager = new ClientDataPackManager();
        }
        List<VersionedIdentifier> list = this.dataPackManager.getCommonKnownPacks(packet.knownPacks());
        this.sendPacket(new SelectKnownPacksC2SPacket(list));
    }

    @Override
    public void onResetChat(ResetChatS2CPacket packet) {
        this.chatState = null;
    }

    private <T> T openClientDataPack(Function<ResourceFactory, T> opener) {
        if (this.dataPackManager == null) {
            return opener.apply(ResourceFactory.MISSING);
        }
        try (LifecycledResourceManager lv = this.dataPackManager.createResourceManager();){
            T t = opener.apply(lv);
            return t;
        }
    }

    @Override
    public void onReady(ReadyS2CPacket packet) {
        NetworkThreadUtils.forceMainThread(packet, this, this.client);
        DynamicRegistryManager.Immutable lv = this.openClientDataPack(factory -> this.clientRegistries.createRegistryManager((ResourceFactory)factory, this.registryManager, this.connection.isLocal()));
        this.connection.transitionInbound(PlayStateFactories.S2C.bind(RegistryByteBuf.makeFactory(lv)), new ClientPlayNetworkHandler(this.client, this.connection, new ClientConnectionState(this.profile, this.worldSession, lv, this.enabledFeatures, this.brand, this.serverInfo, this.postDisconnectScreen, this.serverCookies, this.chatState, this.strictErrorHandling, this.customReportDetails, this.serverLinks)));
        this.connection.send(ReadyC2SPacket.INSTANCE);
        this.connection.transitionOutbound(PlayStateFactories.C2S.bind(RegistryByteBuf.makeFactory(lv)));
    }

    @Override
    public void tick() {
        this.sendQueuedPackets();
    }

    @Override
    public void onDisconnected(DisconnectionInfo info) {
        super.onDisconnected(info);
        this.client.onDisconnected();
    }
}

