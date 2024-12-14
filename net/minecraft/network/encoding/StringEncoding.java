/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.network.encoding;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufUtil;
import io.netty.handler.codec.DecoderException;
import io.netty.handler.codec.EncoderException;
import java.nio.charset.StandardCharsets;
import net.minecraft.network.encoding.VarInts;

public class StringEncoding {
    public static String decode(ByteBuf buf, int maxLength) {
        int j = ByteBufUtil.utf8MaxBytes(maxLength);
        int k = VarInts.read(buf);
        if (k > j) {
            throw new DecoderException("The received encoded string buffer length is longer than maximum allowed (" + k + " > " + j + ")");
        }
        if (k < 0) {
            throw new DecoderException("The received encoded string buffer length is less than zero! Weird string!");
        }
        int l = buf.readableBytes();
        if (k > l) {
            throw new DecoderException("Not enough bytes in buffer, expected " + k + ", but got " + l);
        }
        String string = buf.toString(buf.readerIndex(), k, StandardCharsets.UTF_8);
        buf.readerIndex(buf.readerIndex() + k);
        if (string.length() > maxLength) {
            throw new DecoderException("The received string length is longer than maximum allowed (" + string.length() + " > " + maxLength + ")");
        }
        return string;
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public static void encode(ByteBuf buf, CharSequence string, int maxLength) {
        if (string.length() > maxLength) {
            throw new EncoderException("String too big (was " + string.length() + " characters, max " + maxLength + ")");
        }
        int j = ByteBufUtil.utf8MaxBytes(string);
        ByteBuf byteBuf2 = buf.alloc().buffer(j);
        try {
            int k = ByteBufUtil.writeUtf8(byteBuf2, string);
            int l = ByteBufUtil.utf8MaxBytes(maxLength);
            if (k > l) {
                throw new EncoderException("String too big (was " + k + " bytes encoded, max " + l + ")");
            }
            VarInts.write(buf, k);
            buf.writeBytes(byteBuf2);
        } finally {
            byteBuf2.release();
        }
    }
}

