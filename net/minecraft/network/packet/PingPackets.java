/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.network.packet;

import net.minecraft.network.NetworkSide;
import net.minecraft.network.listener.ClientPingResultPacketListener;
import net.minecraft.network.listener.ServerQueryPingPacketListener;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.PacketType;
import net.minecraft.network.packet.c2s.query.QueryPingC2SPacket;
import net.minecraft.network.packet.s2c.query.PingResultS2CPacket;
import net.minecraft.util.Identifier;

public class PingPackets {
    public static final PacketType<PingResultS2CPacket> PONG_RESPONSE = PingPackets.s2c("pong_response");
    public static final PacketType<QueryPingC2SPacket> PING_REQUEST = PingPackets.c2s("ping_request");

    private static <T extends Packet<ClientPingResultPacketListener>> PacketType<T> s2c(String id) {
        return new PacketType(NetworkSide.CLIENTBOUND, Identifier.ofVanilla(id));
    }

    private static <T extends Packet<ServerQueryPingPacketListener>> PacketType<T> c2s(String id) {
        return new PacketType(NetworkSide.SERVERBOUND, Identifier.ofVanilla(id));
    }
}

