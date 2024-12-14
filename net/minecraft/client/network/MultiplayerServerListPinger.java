/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 * 
 * Could not load the following classes:
 *  net.fabricmc.api.EnvType
 *  net.fabricmc.api.Environment
 */
package net.minecraft.client.network;

import com.google.common.collect.Lists;
import com.mojang.authlib.GameProfile;
import com.mojang.logging.LogUtils;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelException;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.socket.nio.NioSocketChannel;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.screen.multiplayer.ConnectScreen;
import net.minecraft.client.network.Address;
import net.minecraft.client.network.AllowedAddressResolver;
import net.minecraft.client.network.LegacyServerPinger;
import net.minecraft.client.network.ServerAddress;
import net.minecraft.client.network.ServerInfo;
import net.minecraft.network.ClientConnection;
import net.minecraft.network.DisconnectionInfo;
import net.minecraft.network.listener.ClientQueryPacketListener;
import net.minecraft.network.packet.c2s.query.QueryPingC2SPacket;
import net.minecraft.network.packet.c2s.query.QueryRequestC2SPacket;
import net.minecraft.network.packet.s2c.query.PingResultS2CPacket;
import net.minecraft.network.packet.s2c.query.QueryResponseS2CPacket;
import net.minecraft.screen.ScreenTexts;
import net.minecraft.server.ServerMetadata;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Colors;
import net.minecraft.util.Formatting;
import net.minecraft.util.Util;
import org.slf4j.Logger;

@Environment(value=EnvType.CLIENT)
public class MultiplayerServerListPinger {
    private static final Logger LOGGER = LogUtils.getLogger();
    private static final Text CANNOT_CONNECT_TEXT = Text.translatable("multiplayer.status.cannot_connect").withColor(Colors.RED);
    private final List<ClientConnection> clientConnections = Collections.synchronizedList(Lists.newArrayList());

    public void add(final ServerInfo entry, final Runnable saver, final Runnable pingCallback) throws UnknownHostException {
        final ServerAddress lv = ServerAddress.parse(entry.address);
        Optional<InetSocketAddress> optional = AllowedAddressResolver.DEFAULT.resolve(lv).map(Address::getInetSocketAddress);
        if (optional.isEmpty()) {
            this.showError(ConnectScreen.UNKNOWN_HOST_TEXT, entry);
            return;
        }
        final InetSocketAddress inetSocketAddress = optional.get();
        final ClientConnection lv2 = ClientConnection.connect(inetSocketAddress, false, null);
        this.clientConnections.add(lv2);
        entry.label = Text.translatable("multiplayer.status.pinging");
        entry.playerListSummary = Collections.emptyList();
        ClientQueryPacketListener lv3 = new ClientQueryPacketListener(){
            private boolean sentQuery;
            private boolean received;
            private long startTime;

            @Override
            public void onResponse(QueryResponseS2CPacket packet) {
                if (this.received) {
                    lv2.disconnect(Text.translatable("multiplayer.status.unrequested"));
                    return;
                }
                this.received = true;
                ServerMetadata lv3 = packet.metadata();
                entry.label = lv3.description();
                lv3.version().ifPresentOrElse(version -> {
                    arg.version = Text.literal(version.gameVersion());
                    arg.protocolVersion = version.protocolVersion();
                }, () -> {
                    arg.version = Text.translatable("multiplayer.status.old");
                    arg.protocolVersion = 0;
                });
                lv3.players().ifPresentOrElse(players -> {
                    arg.playerCountLabel = MultiplayerServerListPinger.createPlayerCountText(players.online(), players.max());
                    arg.players = players;
                    if (!players.sample().isEmpty()) {
                        ArrayList<Text> list = new ArrayList<Text>(players.sample().size());
                        for (GameProfile gameProfile : players.sample()) {
                            list.add(Text.literal(gameProfile.getName()));
                        }
                        if (players.sample().size() < players.online()) {
                            list.add(Text.translatable("multiplayer.status.and_more", players.online() - players.sample().size()));
                        }
                        arg.playerListSummary = list;
                    } else {
                        arg.playerListSummary = List.of();
                    }
                }, () -> {
                    arg.playerCountLabel = Text.translatable("multiplayer.status.unknown").formatted(Formatting.DARK_GRAY);
                });
                lv3.favicon().ifPresent(favicon -> {
                    if (!Arrays.equals(favicon.iconBytes(), entry.getFavicon())) {
                        entry.setFavicon(ServerInfo.validateFavicon(favicon.iconBytes()));
                        saver.run();
                    }
                });
                this.startTime = Util.getMeasuringTimeMs();
                lv2.send(new QueryPingC2SPacket(this.startTime));
                this.sentQuery = true;
            }

            @Override
            public void onPingResult(PingResultS2CPacket packet) {
                long l = this.startTime;
                long m = Util.getMeasuringTimeMs();
                entry.ping = m - l;
                lv2.disconnect(Text.translatable("multiplayer.status.finished"));
                pingCallback.run();
            }

            @Override
            public void onDisconnected(DisconnectionInfo info) {
                if (!this.sentQuery) {
                    MultiplayerServerListPinger.this.showError(info.reason(), entry);
                    MultiplayerServerListPinger.this.ping(inetSocketAddress, lv, entry);
                }
            }

            @Override
            public boolean isConnectionOpen() {
                return lv2.isOpen();
            }
        };
        try {
            lv2.connect(lv.getAddress(), lv.getPort(), lv3);
            lv2.send(QueryRequestC2SPacket.INSTANCE);
        } catch (Throwable throwable) {
            LOGGER.error("Failed to ping server {}", (Object)lv, (Object)throwable);
        }
    }

    void showError(Text error, ServerInfo info) {
        LOGGER.error("Can't ping {}: {}", (Object)info.address, (Object)error.getString());
        info.label = CANNOT_CONNECT_TEXT;
        info.playerCountLabel = ScreenTexts.EMPTY;
    }

    void ping(InetSocketAddress socketAddress, final ServerAddress address, final ServerInfo serverInfo) {
        ((Bootstrap)((Bootstrap)((Bootstrap)new Bootstrap().group(ClientConnection.CLIENT_IO_GROUP.get())).handler(new ChannelInitializer<Channel>(this){

            @Override
            protected void initChannel(Channel channel) {
                try {
                    channel.config().setOption(ChannelOption.TCP_NODELAY, true);
                } catch (ChannelException channelException) {
                    // empty catch block
                }
                channel.pipeline().addLast(new LegacyServerPinger(address, (protocolVersion, version, label, currentPlayers, maxPlayers) -> {
                    serverInfo.setStatus(ServerInfo.Status.INCOMPATIBLE);
                    arg.version = Text.literal(version);
                    arg.label = Text.literal(label);
                    arg.playerCountLabel = MultiplayerServerListPinger.createPlayerCountText(currentPlayers, maxPlayers);
                    arg.players = new ServerMetadata.Players(maxPlayers, currentPlayers, List.of());
                }));
            }
        })).channel(NioSocketChannel.class)).connect(socketAddress.getAddress(), socketAddress.getPort());
    }

    public static Text createPlayerCountText(int current, int max) {
        MutableText lv = Text.literal(Integer.toString(current)).formatted(Formatting.GRAY);
        MutableText lv2 = Text.literal(Integer.toString(max)).formatted(Formatting.GRAY);
        return Text.translatable("multiplayer.status.player_count", lv, lv2).formatted(Formatting.DARK_GRAY);
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public void tick() {
        List<ClientConnection> list = this.clientConnections;
        synchronized (list) {
            Iterator<ClientConnection> iterator = this.clientConnections.iterator();
            while (iterator.hasNext()) {
                ClientConnection lv = iterator.next();
                if (lv.isOpen()) {
                    lv.tick();
                    continue;
                }
                iterator.remove();
                lv.handleDisconnection();
            }
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public void cancel() {
        List<ClientConnection> list = this.clientConnections;
        synchronized (list) {
            Iterator<ClientConnection> iterator = this.clientConnections.iterator();
            while (iterator.hasNext()) {
                ClientConnection lv = iterator.next();
                if (!lv.isOpen()) continue;
                iterator.remove();
                lv.disconnect(Text.translatable("multiplayer.status.cancelled"));
            }
        }
    }
}

