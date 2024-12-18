/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 * 
 * Could not load the following classes:
 *  org.jetbrains.annotations.Nullable
 */
package net.minecraft.server.network;

import com.mojang.authlib.GameProfile;
import com.mojang.logging.LogUtils;
import net.minecraft.network.ClientConnection;
import net.minecraft.network.DisconnectionInfo;
import net.minecraft.network.NetworkThreadUtils;
import net.minecraft.network.PacketCallbacks;
import net.minecraft.network.listener.ServerCommonPacketListener;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.c2s.common.CommonPongC2SPacket;
import net.minecraft.network.packet.c2s.common.CookieResponseC2SPacket;
import net.minecraft.network.packet.c2s.common.CustomPayloadC2SPacket;
import net.minecraft.network.packet.c2s.common.KeepAliveC2SPacket;
import net.minecraft.network.packet.c2s.common.ResourcePackStatusC2SPacket;
import net.minecraft.network.packet.c2s.common.SyncedClientOptions;
import net.minecraft.network.packet.s2c.common.DisconnectS2CPacket;
import net.minecraft.network.packet.s2c.common.KeepAliveS2CPacket;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ConnectedClientData;
import net.minecraft.text.Text;
import net.minecraft.util.Util;
import net.minecraft.util.annotation.Debug;
import net.minecraft.util.crash.CrashException;
import net.minecraft.util.crash.CrashReport;
import net.minecraft.util.crash.CrashReportSection;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;

public abstract class ServerCommonNetworkHandler
implements ServerCommonPacketListener {
    private static final Logger LOGGER = LogUtils.getLogger();
    public static final int KEEP_ALIVE_INTERVAL = 15000;
    private static final int TRANSITION_TIMEOUT = 15000;
    private static final Text TIMEOUT_TEXT = Text.translatable("disconnect.timeout");
    static final Text UNEXPECTED_QUERY_RESPONSE_TEXT = Text.translatable("multiplayer.disconnect.unexpected_query_response");
    protected final MinecraftServer server;
    protected final ClientConnection connection;
    private final boolean transferred;
    private long lastKeepAliveTime;
    private boolean waitingForKeepAlive;
    private long keepAliveId;
    private long transitionStartTime;
    private boolean transitioning = false;
    private int latency;
    private volatile boolean flushDisabled = false;

    public ServerCommonNetworkHandler(MinecraftServer server, ClientConnection connection, ConnectedClientData clientData) {
        this.server = server;
        this.connection = connection;
        this.lastKeepAliveTime = Util.getMeasuringTimeMs();
        this.latency = clientData.latency();
        this.transferred = clientData.transferred();
    }

    private void markTransitionTime() {
        if (!this.transitioning) {
            this.transitionStartTime = Util.getMeasuringTimeMs();
            this.transitioning = true;
        }
    }

    @Override
    public void onDisconnected(DisconnectionInfo info) {
        if (this.isHost()) {
            LOGGER.info("Stopping singleplayer server as player logged out");
            this.server.stop(false);
        }
    }

    @Override
    public void onKeepAlive(KeepAliveC2SPacket packet) {
        if (this.waitingForKeepAlive && packet.getId() == this.keepAliveId) {
            int i = (int)(Util.getMeasuringTimeMs() - this.lastKeepAliveTime);
            this.latency = (this.latency * 3 + i) / 4;
            this.waitingForKeepAlive = false;
        } else if (!this.isHost()) {
            this.disconnect(TIMEOUT_TEXT);
        }
    }

    @Override
    public void onPong(CommonPongC2SPacket packet) {
    }

    @Override
    public void onCustomPayload(CustomPayloadC2SPacket packet) {
    }

    @Override
    public void onResourcePackStatus(ResourcePackStatusC2SPacket packet) {
        NetworkThreadUtils.forceMainThread(packet, this, this.server);
        if (packet.status() == ResourcePackStatusC2SPacket.Status.DECLINED && this.server.requireResourcePack()) {
            LOGGER.info("Disconnecting {} due to resource pack {} rejection", (Object)this.getProfile().getName(), (Object)packet.id());
            this.disconnect(Text.translatable("multiplayer.requiredTexturePrompt.disconnect"));
        }
    }

    @Override
    public void onCookieResponse(CookieResponseC2SPacket packet) {
        this.disconnect(UNEXPECTED_QUERY_RESPONSE_TEXT);
    }

    protected void baseTick() {
        this.server.getProfiler().push("keepAlive");
        long l = Util.getMeasuringTimeMs();
        if (!this.isHost() && l - this.lastKeepAliveTime >= 15000L) {
            if (this.waitingForKeepAlive) {
                this.disconnect(TIMEOUT_TEXT);
            } else if (this.checkTransitionTimeout(l)) {
                this.waitingForKeepAlive = true;
                this.lastKeepAliveTime = l;
                this.keepAliveId = l;
                this.sendPacket(new KeepAliveS2CPacket(this.keepAliveId));
            }
        }
        this.server.getProfiler().pop();
    }

    private boolean checkTransitionTimeout(long time) {
        if (this.transitioning) {
            if (time - this.transitionStartTime >= 15000L) {
                this.disconnect(TIMEOUT_TEXT);
            }
            return false;
        }
        return true;
    }

    public void disableFlush() {
        this.flushDisabled = true;
    }

    public void enableFlush() {
        this.flushDisabled = false;
        this.connection.flush();
    }

    public void sendPacket(Packet<?> packet) {
        this.send(packet, null);
    }

    public void send(Packet<?> packet, @Nullable PacketCallbacks callbacks) {
        if (packet.transitionsNetworkState()) {
            this.markTransitionTime();
        }
        boolean bl = !this.flushDisabled || !this.server.isOnThread();
        try {
            this.connection.send(packet, callbacks, bl);
        } catch (Throwable throwable) {
            CrashReport lv = CrashReport.create(throwable, "Sending packet");
            CrashReportSection lv2 = lv.addElement("Packet being sent");
            lv2.add("Packet class", () -> packet.getClass().getCanonicalName());
            throw new CrashException(lv);
        }
    }

    public void disconnect(Text reason) {
        this.disconnect(new DisconnectionInfo(reason));
    }

    public void disconnect(DisconnectionInfo disconnectionInfo) {
        this.connection.send(new DisconnectS2CPacket(disconnectionInfo.reason()), PacketCallbacks.always(() -> this.connection.disconnect(disconnectionInfo)));
        this.connection.tryDisableAutoRead();
        this.server.submitAndJoin(this.connection::handleDisconnection);
    }

    protected boolean isHost() {
        return this.server.isHost(this.getProfile());
    }

    protected abstract GameProfile getProfile();

    @Debug
    public GameProfile getDebugProfile() {
        return this.getProfile();
    }

    public int getLatency() {
        return this.latency;
    }

    protected ConnectedClientData createClientData(SyncedClientOptions syncedOptions) {
        return new ConnectedClientData(this.getProfile(), this.latency, syncedOptions, this.transferred);
    }
}

