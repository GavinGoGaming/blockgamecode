/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.network.packet.c2s.common;

import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.listener.ServerCommonPacketListener;
import net.minecraft.network.packet.CommonPackets;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.PacketType;

public class CommonPongC2SPacket
implements Packet<ServerCommonPacketListener> {
    public static final PacketCodec<PacketByteBuf, CommonPongC2SPacket> CODEC = Packet.createCodec(CommonPongC2SPacket::write, CommonPongC2SPacket::new);
    private final int parameter;

    public CommonPongC2SPacket(int parameter) {
        this.parameter = parameter;
    }

    private CommonPongC2SPacket(PacketByteBuf buf) {
        this.parameter = buf.readInt();
    }

    private void write(PacketByteBuf buf) {
        buf.writeInt(this.parameter);
    }

    @Override
    public PacketType<CommonPongC2SPacket> getPacketId() {
        return CommonPackets.PONG;
    }

    @Override
    public void apply(ServerCommonPacketListener arg) {
        arg.onPong(this);
    }

    public int getParameter() {
        return this.parameter;
    }
}

