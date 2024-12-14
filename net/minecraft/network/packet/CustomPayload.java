/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.network.packet;

import io.netty.buffer.ByteBuf;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketDecoder;
import net.minecraft.network.codec.ValueFirstEncoder;
import net.minecraft.util.Identifier;

public interface CustomPayload {
    public Id<? extends CustomPayload> getId();

    public static <B extends ByteBuf, T extends CustomPayload> PacketCodec<B, T> codecOf(ValueFirstEncoder<B, T> encoder, PacketDecoder<B, T> decoder) {
        return PacketCodec.of(encoder, decoder);
    }

    public static <T extends CustomPayload> Id<T> id(String id) {
        return new Id(Identifier.ofVanilla(id));
    }

    public static <B extends PacketByteBuf> PacketCodec<B, CustomPayload> createCodec(final CodecFactory<B> unknownCodecFactory, List<Type<? super B, ?>> types) {
        final Map<Identifier, PacketCodec> map = types.stream().collect(Collectors.toUnmodifiableMap(type -> type.id().id(), Type::codec));
        return new PacketCodec<B, CustomPayload>(){

            private PacketCodec<? super B, ? extends CustomPayload> getCodec(Identifier id) {
                PacketCodec lv = (PacketCodec)map.get(id);
                if (lv != null) {
                    return lv;
                }
                return unknownCodecFactory.create(id);
            }

            private <T extends CustomPayload> void encode(B value, Id<T> id, CustomPayload payload) {
                ((PacketByteBuf)value).writeIdentifier(id.id());
                PacketCodec lv = this.getCodec(id.id);
                lv.encode(value, payload);
            }

            @Override
            public void encode(B arg, CustomPayload arg2) {
                this.encode(arg, arg2.getId(), arg2);
            }

            @Override
            public CustomPayload decode(B arg) {
                Identifier lv = ((PacketByteBuf)arg).readIdentifier();
                return (CustomPayload)this.getCodec(lv).decode(arg);
            }

            @Override
            public /* synthetic */ void encode(Object object, Object object2) {
                this.encode((Object)((PacketByteBuf)object), (CustomPayload)object2);
            }

            @Override
            public /* synthetic */ Object decode(Object object) {
                return this.decode((B)((PacketByteBuf)object));
            }
        };
    }

    public record Id<T extends CustomPayload>(Identifier id) {
    }

    public static interface CodecFactory<B extends PacketByteBuf> {
        public PacketCodec<B, ? extends CustomPayload> create(Identifier var1);
    }

    public record Type<B extends PacketByteBuf, T extends CustomPayload>(Id<T> id, PacketCodec<B, T> codec) {
    }
}

