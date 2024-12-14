/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.network.codec;

import com.google.common.collect.Multimap;
import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.Property;
import com.mojang.authlib.properties.PropertyMap;
import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import io.netty.buffer.ByteBuf;
import io.netty.handler.codec.DecoderException;
import io.netty.handler.codec.EncoderException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Function;
import java.util.function.IntFunction;
import java.util.function.Supplier;
import java.util.function.ToIntFunction;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtEnd;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.NbtSizeTracker;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.encoding.StringEncoding;
import net.minecraft.network.encoding.VarInts;
import net.minecraft.network.encoding.VarLongs;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryOps;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.registry.entry.RegistryEntryList;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.util.Identifier;
import net.minecraft.util.Uuids;
import net.minecraft.util.collection.IndexedIterable;
import org.joml.Quaternionf;
import org.joml.Vector3f;

public interface PacketCodecs {
    public static final int field_49674 = 65536;
    public static final PacketCodec<ByteBuf, Boolean> BOOL = new PacketCodec<ByteBuf, Boolean>(){

        @Override
        public Boolean decode(ByteBuf byteBuf) {
            return byteBuf.readBoolean();
        }

        @Override
        public void encode(ByteBuf byteBuf, Boolean boolean_) {
            byteBuf.writeBoolean(boolean_);
        }

        @Override
        public /* synthetic */ void encode(Object object, Object object2) {
            this.encode((ByteBuf)object, (Boolean)object2);
        }

        @Override
        public /* synthetic */ Object decode(Object object) {
            return this.decode((ByteBuf)object);
        }
    };
    public static final PacketCodec<ByteBuf, Byte> BYTE = new PacketCodec<ByteBuf, Byte>(){

        @Override
        public Byte decode(ByteBuf byteBuf) {
            return byteBuf.readByte();
        }

        @Override
        public void encode(ByteBuf byteBuf, Byte byte_) {
            byteBuf.writeByte(byte_.byteValue());
        }

        @Override
        public /* synthetic */ void encode(Object object, Object object2) {
            this.encode((ByteBuf)object, (Byte)object2);
        }

        @Override
        public /* synthetic */ Object decode(Object object) {
            return this.decode((ByteBuf)object);
        }
    };
    public static final PacketCodec<ByteBuf, Short> SHORT = new PacketCodec<ByteBuf, Short>(){

        @Override
        public Short decode(ByteBuf byteBuf) {
            return byteBuf.readShort();
        }

        @Override
        public void encode(ByteBuf byteBuf, Short short_) {
            byteBuf.writeShort(short_.shortValue());
        }

        @Override
        public /* synthetic */ void encode(Object object, Object object2) {
            this.encode((ByteBuf)object, (Short)object2);
        }

        @Override
        public /* synthetic */ Object decode(Object object) {
            return this.decode((ByteBuf)object);
        }
    };
    public static final PacketCodec<ByteBuf, Integer> UNSIGNED_SHORT = new PacketCodec<ByteBuf, Integer>(){

        @Override
        public Integer decode(ByteBuf byteBuf) {
            return byteBuf.readUnsignedShort();
        }

        @Override
        public void encode(ByteBuf byteBuf, Integer integer) {
            byteBuf.writeShort(integer);
        }

        @Override
        public /* synthetic */ void encode(Object object, Object object2) {
            this.encode((ByteBuf)object, (Integer)object2);
        }

        @Override
        public /* synthetic */ Object decode(Object object) {
            return this.decode((ByteBuf)object);
        }
    };
    public static final PacketCodec<ByteBuf, Integer> INTEGER = new PacketCodec<ByteBuf, Integer>(){

        @Override
        public Integer decode(ByteBuf byteBuf) {
            return byteBuf.readInt();
        }

        @Override
        public void encode(ByteBuf byteBuf, Integer integer) {
            byteBuf.writeInt(integer);
        }

        @Override
        public /* synthetic */ void encode(Object object, Object object2) {
            this.encode((ByteBuf)object, (Integer)object2);
        }

        @Override
        public /* synthetic */ Object decode(Object object) {
            return this.decode((ByteBuf)object);
        }
    };
    public static final PacketCodec<ByteBuf, Integer> VAR_INT = new PacketCodec<ByteBuf, Integer>(){

        @Override
        public Integer decode(ByteBuf byteBuf) {
            return VarInts.read(byteBuf);
        }

        @Override
        public void encode(ByteBuf byteBuf, Integer integer) {
            VarInts.write(byteBuf, integer);
        }

        @Override
        public /* synthetic */ void encode(Object object, Object object2) {
            this.encode((ByteBuf)object, (Integer)object2);
        }

        @Override
        public /* synthetic */ Object decode(Object object) {
            return this.decode((ByteBuf)object);
        }
    };
    public static final PacketCodec<ByteBuf, Long> VAR_LONG = new PacketCodec<ByteBuf, Long>(){

        @Override
        public Long decode(ByteBuf byteBuf) {
            return VarLongs.read(byteBuf);
        }

        @Override
        public void encode(ByteBuf byteBuf, Long long_) {
            VarLongs.write(byteBuf, long_);
        }

        @Override
        public /* synthetic */ void encode(Object object, Object object2) {
            this.encode((ByteBuf)object, (Long)object2);
        }

        @Override
        public /* synthetic */ Object decode(Object object) {
            return this.decode((ByteBuf)object);
        }
    };
    public static final PacketCodec<ByteBuf, Float> FLOAT = new PacketCodec<ByteBuf, Float>(){

        @Override
        public Float decode(ByteBuf byteBuf) {
            return Float.valueOf(byteBuf.readFloat());
        }

        @Override
        public void encode(ByteBuf byteBuf, Float float_) {
            byteBuf.writeFloat(float_.floatValue());
        }

        @Override
        public /* synthetic */ void encode(Object object, Object object2) {
            this.encode((ByteBuf)object, (Float)object2);
        }

        @Override
        public /* synthetic */ Object decode(Object object) {
            return this.decode((ByteBuf)object);
        }
    };
    public static final PacketCodec<ByteBuf, Double> DOUBLE = new PacketCodec<ByteBuf, Double>(){

        @Override
        public Double decode(ByteBuf byteBuf) {
            return byteBuf.readDouble();
        }

        @Override
        public void encode(ByteBuf byteBuf, Double double_) {
            byteBuf.writeDouble(double_);
        }

        @Override
        public /* synthetic */ void encode(Object object, Object object2) {
            this.encode((ByteBuf)object, (Double)object2);
        }

        @Override
        public /* synthetic */ Object decode(Object object) {
            return this.decode((ByteBuf)object);
        }
    };
    public static final PacketCodec<ByteBuf, byte[]> BYTE_ARRAY = new PacketCodec<ByteBuf, byte[]>(){

        public byte[] method_59799(ByteBuf byteBuf) {
            return PacketByteBuf.readByteArray(byteBuf);
        }

        public void method_59800(ByteBuf byteBuf, byte[] bs) {
            PacketByteBuf.writeByteArray(byteBuf, bs);
        }

        @Override
        public /* synthetic */ void encode(Object object, Object object2) {
            this.method_59800((ByteBuf)object, (byte[])object2);
        }

        @Override
        public /* synthetic */ Object decode(Object object) {
            return this.method_59799((ByteBuf)object);
        }
    };
    public static final PacketCodec<ByteBuf, String> STRING = PacketCodecs.string(Short.MAX_VALUE);
    public static final PacketCodec<ByteBuf, NbtElement> NBT_ELEMENT = PacketCodecs.nbt(() -> NbtSizeTracker.of(0x200000L));
    public static final PacketCodec<ByteBuf, NbtElement> UNLIMITED_NBT_ELEMENT = PacketCodecs.nbt(NbtSizeTracker::ofUnlimitedBytes);
    public static final PacketCodec<ByteBuf, NbtCompound> NBT_COMPOUND = PacketCodecs.nbtCompound(() -> NbtSizeTracker.of(0x200000L));
    public static final PacketCodec<ByteBuf, NbtCompound> UNLIMITED_NBT_COMPOUND = PacketCodecs.nbtCompound(NbtSizeTracker::ofUnlimitedBytes);
    public static final PacketCodec<ByteBuf, Optional<NbtCompound>> OPTIONAL_NBT = new PacketCodec<ByteBuf, Optional<NbtCompound>>(){

        @Override
        public Optional<NbtCompound> decode(ByteBuf byteBuf) {
            return Optional.ofNullable(PacketByteBuf.readNbt(byteBuf));
        }

        @Override
        public void encode(ByteBuf byteBuf, Optional<NbtCompound> optional) {
            PacketByteBuf.writeNbt(byteBuf, optional.orElse(null));
        }

        @Override
        public /* synthetic */ void encode(Object object, Object object2) {
            this.encode((ByteBuf)object, (Optional)object2);
        }

        @Override
        public /* synthetic */ Object decode(Object object) {
            return this.decode((ByteBuf)object);
        }
    };
    public static final PacketCodec<ByteBuf, Vector3f> VECTOR3F = new PacketCodec<ByteBuf, Vector3f>(){

        @Override
        public Vector3f decode(ByteBuf byteBuf) {
            return PacketByteBuf.readVector3f(byteBuf);
        }

        @Override
        public void encode(ByteBuf byteBuf, Vector3f vector3f) {
            PacketByteBuf.writeVector3f(byteBuf, vector3f);
        }

        @Override
        public /* synthetic */ void encode(Object object, Object object2) {
            this.encode((ByteBuf)object, (Vector3f)object2);
        }

        @Override
        public /* synthetic */ Object decode(Object object) {
            return this.decode((ByteBuf)object);
        }
    };
    public static final PacketCodec<ByteBuf, Quaternionf> QUATERNIONF = new PacketCodec<ByteBuf, Quaternionf>(){

        @Override
        public Quaternionf decode(ByteBuf byteBuf) {
            return PacketByteBuf.readQuaternionf(byteBuf);
        }

        @Override
        public void encode(ByteBuf byteBuf, Quaternionf quaternionf) {
            PacketByteBuf.writeQuaternionf(byteBuf, quaternionf);
        }

        @Override
        public /* synthetic */ void encode(Object object, Object object2) {
            this.encode((ByteBuf)object, (Quaternionf)object2);
        }

        @Override
        public /* synthetic */ Object decode(Object object) {
            return this.decode((ByteBuf)object);
        }
    };
    public static final PacketCodec<ByteBuf, PropertyMap> PROPERTY_MAP = new PacketCodec<ByteBuf, PropertyMap>(){
        private static final int NAME_MAX_LENGTH = 64;
        private static final int VALUE_MAX_LENGTH = Short.MAX_VALUE;
        private static final int SIGNATURE_MAX_LENGTH = 1024;
        private static final int MAP_MAX_SIZE = 16;

        @Override
        public PropertyMap decode(ByteBuf byteBuf) {
            int i = PacketCodecs.readCollectionSize(byteBuf, 16);
            PropertyMap propertyMap = new PropertyMap();
            for (int j = 0; j < i; ++j) {
                String string = StringEncoding.decode(byteBuf, 64);
                String string2 = StringEncoding.decode(byteBuf, Short.MAX_VALUE);
                String string3 = PacketByteBuf.readNullable(byteBuf, buf2 -> StringEncoding.decode(buf2, 1024));
                Property property = new Property(string, string2, string3);
                propertyMap.put(property.name(), property);
            }
            return propertyMap;
        }

        @Override
        public void encode(ByteBuf byteBuf, PropertyMap propertyMap) {
            PacketCodecs.writeCollectionSize(byteBuf, propertyMap.size(), 16);
            for (Property property : propertyMap.values()) {
                StringEncoding.encode(byteBuf, property.name(), 64);
                StringEncoding.encode(byteBuf, property.value(), Short.MAX_VALUE);
                PacketByteBuf.writeNullable(byteBuf, property.signature(), (buf2, signature) -> StringEncoding.encode(buf2, signature, 1024));
            }
        }

        @Override
        public /* synthetic */ void encode(Object object, Object object2) {
            this.encode((ByteBuf)object, (PropertyMap)object2);
        }

        @Override
        public /* synthetic */ Object decode(Object object) {
            return this.decode((ByteBuf)object);
        }
    };
    public static final PacketCodec<ByteBuf, GameProfile> GAME_PROFILE = new PacketCodec<ByteBuf, GameProfile>(){

        @Override
        public GameProfile decode(ByteBuf byteBuf) {
            UUID uUID = (UUID)Uuids.PACKET_CODEC.decode(byteBuf);
            String string = StringEncoding.decode(byteBuf, 16);
            GameProfile gameProfile = new GameProfile(uUID, string);
            gameProfile.getProperties().putAll((Multimap)PROPERTY_MAP.decode(byteBuf));
            return gameProfile;
        }

        @Override
        public void encode(ByteBuf byteBuf, GameProfile gameProfile) {
            Uuids.PACKET_CODEC.encode(byteBuf, gameProfile.getId());
            StringEncoding.encode(byteBuf, gameProfile.getName(), 16);
            PROPERTY_MAP.encode(byteBuf, gameProfile.getProperties());
        }

        @Override
        public /* synthetic */ void encode(Object object, Object object2) {
            this.encode((ByteBuf)object, (GameProfile)object2);
        }

        @Override
        public /* synthetic */ Object decode(Object object) {
            return this.decode((ByteBuf)object);
        }
    };

    public static PacketCodec<ByteBuf, byte[]> byteArray(final int maxLength) {
        return new PacketCodec<ByteBuf, byte[]>(){

            @Override
            public byte[] decode(ByteBuf buf) {
                return PacketByteBuf.readByteArray(buf, maxLength);
            }

            @Override
            public void encode(ByteBuf byteBuf, byte[] bs) {
                if (bs.length > maxLength) {
                    throw new EncoderException("ByteArray with size " + bs.length + " is bigger than allowed " + maxLength);
                }
                PacketByteBuf.writeByteArray(byteBuf, bs);
            }

            @Override
            public /* synthetic */ void encode(Object object, Object object2) {
                this.encode((ByteBuf)object, (byte[])object2);
            }

            @Override
            public /* synthetic */ Object decode(Object object) {
                return this.decode((ByteBuf)object);
            }
        };
    }

    public static PacketCodec<ByteBuf, String> string(final int maxLength) {
        return new PacketCodec<ByteBuf, String>(){

            @Override
            public String decode(ByteBuf byteBuf) {
                return StringEncoding.decode(byteBuf, maxLength);
            }

            @Override
            public void encode(ByteBuf byteBuf, String string) {
                StringEncoding.encode(byteBuf, string, maxLength);
            }

            @Override
            public /* synthetic */ void encode(Object object, Object object2) {
                this.encode((ByteBuf)object, (String)object2);
            }

            @Override
            public /* synthetic */ Object decode(Object object) {
                return this.decode((ByteBuf)object);
            }
        };
    }

    public static PacketCodec<ByteBuf, NbtElement> nbt(final Supplier<NbtSizeTracker> sizeTracker) {
        return new PacketCodec<ByteBuf, NbtElement>(){

            @Override
            public NbtElement decode(ByteBuf byteBuf) {
                NbtElement lv = PacketByteBuf.readNbt(byteBuf, (NbtSizeTracker)sizeTracker.get());
                if (lv == null) {
                    throw new DecoderException("Expected non-null compound tag");
                }
                return lv;
            }

            @Override
            public void encode(ByteBuf byteBuf, NbtElement arg) {
                if (arg == NbtEnd.INSTANCE) {
                    throw new EncoderException("Expected non-null compound tag");
                }
                PacketByteBuf.writeNbt(byteBuf, arg);
            }

            @Override
            public /* synthetic */ void encode(Object object, Object object2) {
                this.encode((ByteBuf)object, (NbtElement)object2);
            }

            @Override
            public /* synthetic */ Object decode(Object object) {
                return this.decode((ByteBuf)object);
            }
        };
    }

    public static PacketCodec<ByteBuf, NbtCompound> nbtCompound(Supplier<NbtSizeTracker> sizeTracker) {
        return PacketCodecs.nbt(sizeTracker).xmap(nbt -> {
            if (nbt instanceof NbtCompound) {
                NbtCompound lv = (NbtCompound)nbt;
                return lv;
            }
            throw new DecoderException("Not a compound tag: " + String.valueOf(nbt));
        }, nbt -> nbt);
    }

    public static <T> PacketCodec<ByteBuf, T> unlimitedCodec(Codec<T> codec) {
        return PacketCodecs.codec(codec, NbtSizeTracker::ofUnlimitedBytes);
    }

    public static <T> PacketCodec<ByteBuf, T> codec(Codec<T> codec) {
        return PacketCodecs.codec(codec, () -> NbtSizeTracker.of(0x200000L));
    }

    public static <T> PacketCodec<ByteBuf, T> codec(Codec<T> codec, Supplier<NbtSizeTracker> sizeTracker) {
        return PacketCodecs.nbt(sizeTracker).xmap(nbt -> codec.parse(NbtOps.INSTANCE, nbt).getOrThrow(error -> new DecoderException("Failed to decode: " + error + " " + String.valueOf(nbt))), value -> codec.encodeStart(NbtOps.INSTANCE, value).getOrThrow(error -> new EncoderException("Failed to encode: " + error + " " + String.valueOf(value))));
    }

    public static <T> PacketCodec<RegistryByteBuf, T> unlimitedRegistryCodec(Codec<T> codec) {
        return PacketCodecs.registryCodec(codec, NbtSizeTracker::ofUnlimitedBytes);
    }

    public static <T> PacketCodec<RegistryByteBuf, T> registryCodec(Codec<T> codec) {
        return PacketCodecs.registryCodec(codec, () -> NbtSizeTracker.of(0x200000L));
    }

    public static <T> PacketCodec<RegistryByteBuf, T> registryCodec(final Codec<T> codec, Supplier<NbtSizeTracker> sizeTracker) {
        final PacketCodec<ByteBuf, NbtElement> lv = PacketCodecs.nbt(sizeTracker);
        return new PacketCodec<RegistryByteBuf, T>(){

            @Override
            public T decode(RegistryByteBuf arg) {
                NbtElement lv3 = (NbtElement)lv.decode(arg);
                RegistryOps<NbtElement> lv2 = arg.getRegistryManager().getOps(NbtOps.INSTANCE);
                return codec.parse(lv2, lv3).getOrThrow(error -> new DecoderException("Failed to decode: " + error + " " + String.valueOf(lv3)));
            }

            @Override
            public void encode(RegistryByteBuf arg, T object) {
                RegistryOps<NbtElement> lv3 = arg.getRegistryManager().getOps(NbtOps.INSTANCE);
                NbtElement lv2 = codec.encodeStart(lv3, object).getOrThrow(error -> new EncoderException("Failed to encode: " + error + " " + String.valueOf(object)));
                lv.encode(arg, lv2);
            }

            @Override
            public /* synthetic */ void encode(Object object, Object object2) {
                this.encode((RegistryByteBuf)object, object2);
            }

            @Override
            public /* synthetic */ Object decode(Object object) {
                return this.decode((RegistryByteBuf)object);
            }
        };
    }

    public static <B extends ByteBuf, V> PacketCodec<B, Optional<V>> optional(final PacketCodec<B, V> codec) {
        return new PacketCodec<B, Optional<V>>(){

            @Override
            public Optional<V> decode(B byteBuf) {
                if (((ByteBuf)byteBuf).readBoolean()) {
                    return Optional.of(codec.decode(byteBuf));
                }
                return Optional.empty();
            }

            @Override
            public void encode(B byteBuf, Optional<V> optional) {
                if (optional.isPresent()) {
                    ((ByteBuf)byteBuf).writeBoolean(true);
                    codec.encode(byteBuf, optional.get());
                } else {
                    ((ByteBuf)byteBuf).writeBoolean(false);
                }
            }

            @Override
            public /* synthetic */ void encode(Object object, Object object2) {
                this.encode((Object)((ByteBuf)object), (Optional)object2);
            }

            @Override
            public /* synthetic */ Object decode(Object object) {
                return this.decode((B)((ByteBuf)object));
            }
        };
    }

    public static int readCollectionSize(ByteBuf buf, int maxSize) {
        int j = VarInts.read(buf);
        if (j > maxSize) {
            throw new DecoderException(j + " elements exceeded max size of: " + maxSize);
        }
        return j;
    }

    public static void writeCollectionSize(ByteBuf buf, int size, int maxSize) {
        if (size > maxSize) {
            throw new EncoderException(size + " elements exceeded max size of: " + maxSize);
        }
        VarInts.write(buf, size);
    }

    public static <B extends ByteBuf, V, C extends Collection<V>> PacketCodec<B, C> collection(IntFunction<C> factory, PacketCodec<? super B, V> elementCodec) {
        return PacketCodecs.collection(factory, elementCodec, Integer.MAX_VALUE);
    }

    public static <B extends ByteBuf, V, C extends Collection<V>> PacketCodec<B, C> collection(final IntFunction<C> factory, final PacketCodec<? super B, V> elementCodec, final int maxSize) {
        return new PacketCodec<B, C>(){

            @Override
            public C decode(B byteBuf) {
                int i = PacketCodecs.readCollectionSize(byteBuf, maxSize);
                Collection collection = (Collection)factory.apply(Math.min(i, 65536));
                for (int j = 0; j < i; ++j) {
                    collection.add(elementCodec.decode(byteBuf));
                }
                return collection;
            }

            @Override
            public void encode(B byteBuf, C collection) {
                PacketCodecs.writeCollectionSize(byteBuf, collection.size(), maxSize);
                for (Object object : collection) {
                    elementCodec.encode(byteBuf, object);
                }
            }

            @Override
            public /* synthetic */ void encode(Object object, Object object2) {
                this.encode((B)((ByteBuf)object), (C)((Collection)object2));
            }

            @Override
            public /* synthetic */ Object decode(Object object) {
                return this.decode((B)((ByteBuf)object));
            }
        };
    }

    public static <B extends ByteBuf, V, C extends Collection<V>> PacketCodec.ResultFunction<B, V, C> toCollection(IntFunction<C> collectionFactory) {
        return codec -> PacketCodecs.collection(collectionFactory, codec);
    }

    public static <B extends ByteBuf, V> PacketCodec.ResultFunction<B, V, List<V>> toList() {
        return codec -> PacketCodecs.collection(ArrayList::new, codec);
    }

    public static <B extends ByteBuf, V> PacketCodec.ResultFunction<B, V, List<V>> toList(int maxLength) {
        return codec -> PacketCodecs.collection(ArrayList::new, codec, maxLength);
    }

    public static <B extends ByteBuf, K, V, M extends Map<K, V>> PacketCodec<B, M> map(IntFunction<? extends M> factory, PacketCodec<? super B, K> keyCodec, PacketCodec<? super B, V> valueCodec) {
        return PacketCodecs.map(factory, keyCodec, valueCodec, Integer.MAX_VALUE);
    }

    public static <B extends ByteBuf, K, V, M extends Map<K, V>> PacketCodec<B, M> map(final IntFunction<? extends M> factory, final PacketCodec<? super B, K> keyCodec, final PacketCodec<? super B, V> valueCodec, final int maxSize) {
        return new PacketCodec<B, M>(){

            @Override
            public void encode(B byteBuf, M map) {
                PacketCodecs.writeCollectionSize(byteBuf, map.size(), maxSize);
                map.forEach((k, v) -> {
                    keyCodec.encode(byteBuf, k);
                    valueCodec.encode(byteBuf, v);
                });
            }

            @Override
            public M decode(B byteBuf) {
                int i = PacketCodecs.readCollectionSize(byteBuf, maxSize);
                Map map = (Map)factory.apply(Math.min(i, 65536));
                for (int j = 0; j < i; ++j) {
                    Object object = keyCodec.decode(byteBuf);
                    Object object2 = valueCodec.decode(byteBuf);
                    map.put(object, object2);
                }
                return map;
            }

            @Override
            public /* synthetic */ void encode(Object object, Object object2) {
                this.encode((B)((ByteBuf)object), (M)((Map)object2));
            }

            @Override
            public /* synthetic */ Object decode(Object object) {
                return this.decode((B)((ByteBuf)object));
            }
        };
    }

    public static <B extends ByteBuf, L, R> PacketCodec<B, Either<L, R>> either(final PacketCodec<? super B, L> left, final PacketCodec<? super B, R> right) {
        return new PacketCodec<B, Either<L, R>>(){

            @Override
            public Either<L, R> decode(B byteBuf) {
                if (((ByteBuf)byteBuf).readBoolean()) {
                    return Either.left(left.decode(byteBuf));
                }
                return Either.right(right.decode(byteBuf));
            }

            @Override
            public void encode(B byteBuf, Either<L, R> either) {
                either.ifLeft(left -> {
                    byteBuf.writeBoolean(true);
                    left.encode(byteBuf, left);
                }).ifRight(right -> {
                    byteBuf.writeBoolean(false);
                    right.encode(byteBuf, right);
                });
            }

            @Override
            public /* synthetic */ void encode(Object object, Object object2) {
                this.encode((Object)((ByteBuf)object), (Either)object2);
            }

            @Override
            public /* synthetic */ Object decode(Object object) {
                return this.decode((B)((ByteBuf)object));
            }
        };
    }

    public static <T> PacketCodec<ByteBuf, T> indexed(final IntFunction<T> indexToValue, final ToIntFunction<T> valueToIndex) {
        return new PacketCodec<ByteBuf, T>(){

            @Override
            public T decode(ByteBuf byteBuf) {
                int i = VarInts.read(byteBuf);
                return indexToValue.apply(i);
            }

            @Override
            public void encode(ByteBuf byteBuf, T object) {
                int i = valueToIndex.applyAsInt(object);
                VarInts.write(byteBuf, i);
            }

            @Override
            public /* synthetic */ void encode(Object object, Object object2) {
                this.encode((ByteBuf)object, object2);
            }

            @Override
            public /* synthetic */ Object decode(Object object) {
                return this.decode((ByteBuf)object);
            }
        };
    }

    public static <T> PacketCodec<ByteBuf, T> entryOf(IndexedIterable<T> iterable) {
        return PacketCodecs.indexed(iterable::getOrThrow, iterable::getRawIdOrThrow);
    }

    private static <T, R> PacketCodec<RegistryByteBuf, R> registry(final RegistryKey<? extends Registry<T>> registry, final Function<Registry<T>, IndexedIterable<R>> registryTransformer) {
        return new PacketCodec<RegistryByteBuf, R>(){

            private IndexedIterable<R> getIterable(RegistryByteBuf buf) {
                return (IndexedIterable)registryTransformer.apply(buf.getRegistryManager().get(registry));
            }

            @Override
            public R decode(RegistryByteBuf arg) {
                int i = VarInts.read(arg);
                return this.getIterable(arg).getOrThrow(i);
            }

            @Override
            public void encode(RegistryByteBuf arg, R object) {
                int i = this.getIterable(arg).getRawIdOrThrow(object);
                VarInts.write(arg, i);
            }

            @Override
            public /* synthetic */ void encode(Object object, Object object2) {
                this.encode((RegistryByteBuf)object, object2);
            }

            @Override
            public /* synthetic */ Object decode(Object object) {
                return this.decode((RegistryByteBuf)object);
            }
        };
    }

    public static <T> PacketCodec<RegistryByteBuf, T> registryValue(RegistryKey<? extends Registry<T>> registry2) {
        return PacketCodecs.registry(registry2, registry -> registry);
    }

    public static <T> PacketCodec<RegistryByteBuf, RegistryEntry<T>> registryEntry(RegistryKey<? extends Registry<T>> registry) {
        return PacketCodecs.registry(registry, Registry::getIndexedEntries);
    }

    public static <T> PacketCodec<RegistryByteBuf, RegistryEntry<T>> registryEntry(final RegistryKey<? extends Registry<T>> registry, final PacketCodec<? super RegistryByteBuf, T> directCodec) {
        return new PacketCodec<RegistryByteBuf, RegistryEntry<T>>(){
            private static final int DIRECT_ENTRY_MARKER = 0;

            private IndexedIterable<RegistryEntry<T>> getEntries(RegistryByteBuf buf) {
                return buf.getRegistryManager().get(registry).getIndexedEntries();
            }

            @Override
            public RegistryEntry<T> decode(RegistryByteBuf arg) {
                int i = VarInts.read(arg);
                if (i == 0) {
                    return RegistryEntry.of(directCodec.decode(arg));
                }
                return this.getEntries(arg).getOrThrow(i - 1);
            }

            @Override
            public void encode(RegistryByteBuf arg, RegistryEntry<T> arg2) {
                switch (arg2.getType()) {
                    case REFERENCE: {
                        int i = this.getEntries(arg).getRawIdOrThrow(arg2);
                        VarInts.write(arg, i + 1);
                        break;
                    }
                    case DIRECT: {
                        VarInts.write(arg, 0);
                        directCodec.encode(arg, arg2.value());
                    }
                }
            }

            @Override
            public /* synthetic */ void encode(Object object, Object object2) {
                this.encode((RegistryByteBuf)object, (RegistryEntry)object2);
            }

            @Override
            public /* synthetic */ Object decode(Object object) {
                return this.decode((RegistryByteBuf)object);
            }
        };
    }

    public static <T> PacketCodec<RegistryByteBuf, RegistryEntryList<T>> registryEntryList(final RegistryKey<? extends Registry<T>> registryRef) {
        return new PacketCodec<RegistryByteBuf, RegistryEntryList<T>>(){
            private static final int DIRECT_MARKER = -1;
            private final PacketCodec<RegistryByteBuf, RegistryEntry<T>> entryPacketCodec;
            {
                this.entryPacketCodec = PacketCodecs.registryEntry(registryRef);
            }

            @Override
            public RegistryEntryList<T> decode(RegistryByteBuf arg) {
                int i = VarInts.read(arg) - 1;
                if (i == -1) {
                    Registry lv = arg.getRegistryManager().get(registryRef);
                    return lv.getEntryList(TagKey.of(registryRef, (Identifier)Identifier.PACKET_CODEC.decode(arg))).orElseThrow();
                }
                ArrayList<RegistryEntry> list = new ArrayList<RegistryEntry>(Math.min(i, 65536));
                for (int j = 0; j < i; ++j) {
                    list.add((RegistryEntry)this.entryPacketCodec.decode(arg));
                }
                return RegistryEntryList.of(list);
            }

            @Override
            public void encode(RegistryByteBuf arg, RegistryEntryList<T> arg2) {
                Optional optional = arg2.getTagKey();
                if (optional.isPresent()) {
                    VarInts.write(arg, 0);
                    Identifier.PACKET_CODEC.encode(arg, optional.get().id());
                } else {
                    VarInts.write(arg, arg2.size() + 1);
                    for (RegistryEntry registryEntry : arg2) {
                        this.entryPacketCodec.encode(arg, registryEntry);
                    }
                }
            }

            @Override
            public /* synthetic */ void encode(Object object, Object object2) {
                this.encode((RegistryByteBuf)object, (RegistryEntryList)object2);
            }

            @Override
            public /* synthetic */ Object decode(Object object) {
                return this.decode((RegistryByteBuf)object);
            }
        };
    }
}

