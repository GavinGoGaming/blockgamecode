/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.network.handler;

import com.mojang.logging.LogUtils;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;
import net.minecraft.network.ClientConnection;
import net.minecraft.network.NetworkState;
import net.minecraft.network.handler.NetworkStateTransitionHandler;
import net.minecraft.network.handler.PacketEncoderException;
import net.minecraft.network.listener.PacketListener;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.PacketType;
import net.minecraft.util.profiling.jfr.FlightProfiler;
import org.slf4j.Logger;

public class EncoderHandler<T extends PacketListener>
extends MessageToByteEncoder<Packet<T>> {
    private static final Logger LOGGER = LogUtils.getLogger();
    private final NetworkState<T> state;

    public EncoderHandler(NetworkState<T> state) {
        this.state = state;
    }

    @Override
    protected void encode(ChannelHandlerContext channelHandlerContext, Packet<T> arg, ByteBuf byteBuf) throws Exception {
        PacketType<Packet<T>> lv = arg.getPacketId();
        try {
            this.state.codec().encode(byteBuf, arg);
            int i = byteBuf.readableBytes();
            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug(ClientConnection.PACKET_SENT_MARKER, "OUT: [{}:{}] {} -> {} bytes", this.state.id().getId(), lv, arg.getClass().getName(), i);
            }
            FlightProfiler.INSTANCE.onPacketSent(this.state.id(), lv, channelHandlerContext.channel().remoteAddress(), i);
        } catch (Throwable throwable) {
            LOGGER.error("Error sending packet {}", (Object)lv, (Object)throwable);
            if (arg.isWritingErrorSkippable()) {
                throw new PacketEncoderException(throwable);
            }
            throw throwable;
        } finally {
            NetworkStateTransitionHandler.onEncoded(channelHandlerContext, arg);
        }
    }

    @Override
    protected /* synthetic */ void encode(ChannelHandlerContext context, Object packet, ByteBuf out) throws Exception {
        this.encode(context, (Packet)packet, out);
    }
}

