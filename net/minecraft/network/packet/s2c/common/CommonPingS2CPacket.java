/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.network.packet.s2c.common;

import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.listener.ClientCommonPacketListener;
import net.minecraft.network.packet.CommonPackets;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.PacketType;

public class CommonPingS2CPacket
implements Packet<ClientCommonPacketListener> {
    public static final PacketCodec<PacketByteBuf, CommonPingS2CPacket> CODEC = Packet.createCodec(CommonPingS2CPacket::write, CommonPingS2CPacket::new);
    private final int parameter;

    public CommonPingS2CPacket(int parameter) {
        this.parameter = parameter;
    }

    private CommonPingS2CPacket(PacketByteBuf buf) {
        this.parameter = buf.readInt();
    }

    private void write(PacketByteBuf buf) {
        buf.writeInt(this.parameter);
    }

    @Override
    public PacketType<CommonPingS2CPacket> getPacketId() {
        return CommonPackets.PING;
    }

    @Override
    public void apply(ClientCommonPacketListener arg) {
        arg.onPing(this);
    }

    public int getParameter() {
        return this.parameter;
    }
}

