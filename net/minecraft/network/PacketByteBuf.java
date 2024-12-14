/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 * 
 * Could not load the following classes:
 *  org.jetbrains.annotations.Nullable
 */
package net.minecraft.network;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.JsonOps;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;
import io.netty.buffer.ByteBufInputStream;
import io.netty.buffer.ByteBufOutputStream;
import io.netty.handler.codec.DecoderException;
import io.netty.handler.codec.EncoderException;
import io.netty.util.ByteProcessor;
import io.netty.util.ReferenceCounted;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntList;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.channels.FileChannel;
import java.nio.channels.GatheringByteChannel;
import java.nio.channels.ScatteringByteChannel;
import java.nio.charset.Charset;
import java.security.PublicKey;
import java.time.Instant;
import java.util.Arrays;
import java.util.BitSet;
import java.util.Collection;
import java.util.Date;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Consumer;
import java.util.function.IntFunction;
import java.util.function.ToIntFunction;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtEnd;
import net.minecraft.nbt.NbtIo;
import net.minecraft.nbt.NbtSizeTracker;
import net.minecraft.network.codec.PacketDecoder;
import net.minecraft.network.codec.PacketEncoder;
import net.minecraft.network.encoding.StringEncoding;
import net.minecraft.network.encoding.VarInts;
import net.minecraft.network.encoding.VarLongs;
import net.minecraft.network.encryption.NetworkEncryptionException;
import net.minecraft.network.encryption.NetworkEncryptionUtils;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.util.Identifier;
import net.minecraft.util.JsonHelper;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.ChunkSectionPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.GlobalPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;
import org.joml.Quaternionf;
import org.joml.Vector3f;

public class PacketByteBuf
extends ByteBuf {
    public static final int MAX_READ_NBT_SIZE = 0x200000;
    private final ByteBuf parent;
    public static final short DEFAULT_MAX_STRING_LENGTH = Short.MAX_VALUE;
    public static final int MAX_TEXT_LENGTH = 262144;
    private static final int field_39381 = 256;
    private static final int field_39382 = 256;
    private static final int field_39383 = 512;
    private static final Gson GSON = new Gson();

    public PacketByteBuf(ByteBuf parent) {
        this.parent = parent;
    }

    @Deprecated
    public <T> T decode(DynamicOps<NbtElement> ops, Codec<T> codec) {
        return this.decode(ops, codec, NbtSizeTracker.ofUnlimitedBytes());
    }

    @Deprecated
    public <T> T decode(DynamicOps<NbtElement> ops, Codec<T> codec, NbtSizeTracker sizeTracker) {
        NbtElement lv = this.readNbt(sizeTracker);
        return (T)codec.parse(ops, lv).getOrThrow(error -> new DecoderException("Failed to decode: " + error + " " + String.valueOf(lv)));
    }

    @Deprecated
    public <T> PacketByteBuf encode(DynamicOps<NbtElement> ops, Codec<T> codec, T value) {
        NbtElement lv = codec.encodeStart(ops, (NbtElement)value).getOrThrow(error -> new EncoderException("Failed to encode: " + error + " " + String.valueOf(value)));
        this.writeNbt(lv);
        return this;
    }

    public <T> T decodeAsJson(Codec<T> codec) {
        JsonElement jsonElement = JsonHelper.deserialize(GSON, this.readString(), JsonElement.class);
        DataResult dataResult = codec.parse(JsonOps.INSTANCE, jsonElement);
        return (T)dataResult.getOrThrow(error -> new DecoderException("Failed to decode json: " + error));
    }

    public <T> void encodeAsJson(Codec<T> codec, T value) {
        DataResult<JsonElement> dataResult = codec.encodeStart(JsonOps.INSTANCE, (JsonElement)value);
        this.writeString(GSON.toJson(dataResult.getOrThrow(error -> new EncoderException("Failed to encode: " + error + " " + String.valueOf(value)))));
    }

    public static <T> IntFunction<T> getMaxValidator(IntFunction<T> applier, int max) {
        return value -> {
            if (value > max) {
                throw new DecoderException("Value " + value + " is larger than limit " + max);
            }
            return applier.apply(value);
        };
    }

    public <T, C extends Collection<T>> C readCollection(IntFunction<C> collectionFactory, PacketDecoder<? super PacketByteBuf, T> reader) {
        int i = this.readVarInt();
        Collection collection = (Collection)collectionFactory.apply(i);
        for (int j = 0; j < i; ++j) {
            collection.add(reader.decode(this));
        }
        return (C)collection;
    }

    public <T> void writeCollection(Collection<T> collection, PacketEncoder<? super PacketByteBuf, T> writer) {
        this.writeVarInt(collection.size());
        for (T object : collection) {
            writer.encode(this, object);
        }
    }

    public <T> List<T> readList(PacketDecoder<? super PacketByteBuf, T> reader) {
        return this.readCollection(Lists::newArrayListWithCapacity, reader);
    }

    public IntList readIntList() {
        int i = this.readVarInt();
        IntArrayList intList = new IntArrayList();
        for (int j = 0; j < i; ++j) {
            intList.add(this.readVarInt());
        }
        return intList;
    }

    public void writeIntList(IntList list) {
        this.writeVarInt(list.size());
        list.forEach(this::writeVarInt);
    }

    public <K, V, M extends Map<K, V>> M readMap(IntFunction<M> mapFactory, PacketDecoder<? super PacketByteBuf, K> keyReader, PacketDecoder<? super PacketByteBuf, V> valueReader) {
        int i = this.readVarInt();
        Map map = (Map)mapFactory.apply(i);
        for (int j = 0; j < i; ++j) {
            K object = keyReader.decode(this);
            V object2 = valueReader.decode(this);
            map.put(object, object2);
        }
        return (M)map;
    }

    public <K, V> Map<K, V> readMap(PacketDecoder<? super PacketByteBuf, K> keyReader, PacketDecoder<? super PacketByteBuf, V> valueReader) {
        return this.readMap(Maps::newHashMapWithExpectedSize, keyReader, valueReader);
    }

    public <K, V> void writeMap(Map<K, V> map, PacketEncoder<? super PacketByteBuf, K> keyWriter, PacketEncoder<? super PacketByteBuf, V> valueWriter) {
        this.writeVarInt(map.size());
        map.forEach((key, value) -> {
            keyWriter.encode(this, key);
            valueWriter.encode(this, value);
        });
    }

    public void forEachInCollection(Consumer<PacketByteBuf> consumer) {
        int i = this.readVarInt();
        for (int j = 0; j < i; ++j) {
            consumer.accept(this);
        }
    }

    public <E extends Enum<E>> void writeEnumSet(EnumSet<E> enumSet, Class<E> type) {
        Enum[] enums = (Enum[])type.getEnumConstants();
        BitSet bitSet = new BitSet(enums.length);
        for (int i = 0; i < enums.length; ++i) {
            bitSet.set(i, enumSet.contains(enums[i]));
        }
        this.writeBitSet(bitSet, enums.length);
    }

    public <E extends Enum<E>> EnumSet<E> readEnumSet(Class<E> type) {
        Enum[] enums = (Enum[])type.getEnumConstants();
        BitSet bitSet = this.readBitSet(enums.length);
        EnumSet<Enum> enumSet = EnumSet.noneOf(type);
        for (int i = 0; i < enums.length; ++i) {
            if (!bitSet.get(i)) continue;
            enumSet.add(enums[i]);
        }
        return enumSet;
    }

    public <T> void writeOptional(Optional<T> value, PacketEncoder<? super PacketByteBuf, T> writer) {
        if (value.isPresent()) {
            this.writeBoolean(true);
            writer.encode(this, value.get());
        } else {
            this.writeBoolean(false);
        }
    }

    public <T> Optional<T> readOptional(PacketDecoder<? super PacketByteBuf, T> reader) {
        if (this.readBoolean()) {
            return Optional.of(reader.decode(this));
        }
        return Optional.empty();
    }

    @Nullable
    public <T> T readNullable(PacketDecoder<? super PacketByteBuf, T> reader) {
        return PacketByteBuf.readNullable(this, reader);
    }

    @Nullable
    public static <T, B extends ByteBuf> T readNullable(B buf, PacketDecoder<? super B, T> reader) {
        if (buf.readBoolean()) {
            return reader.decode(buf);
        }
        return null;
    }

    public <T> void writeNullable(@Nullable T value, PacketEncoder<? super PacketByteBuf, T> writer) {
        PacketByteBuf.writeNullable(this, value, writer);
    }

    public static <T, B extends ByteBuf> void writeNullable(B buf, @Nullable T value, PacketEncoder<? super B, T> writer) {
        if (value != null) {
            buf.writeBoolean(true);
            writer.encode(buf, value);
        } else {
            buf.writeBoolean(false);
        }
    }

    public byte[] readByteArray() {
        return PacketByteBuf.readByteArray(this);
    }

    public static byte[] readByteArray(ByteBuf buf) {
        return PacketByteBuf.readByteArray(buf, buf.readableBytes());
    }

    public PacketByteBuf writeByteArray(byte[] array) {
        PacketByteBuf.writeByteArray(this, array);
        return this;
    }

    public static void writeByteArray(ByteBuf buf, byte[] array) {
        VarInts.write(buf, array.length);
        buf.writeBytes(array);
    }

    public byte[] readByteArray(int maxSize) {
        return PacketByteBuf.readByteArray(this, maxSize);
    }

    public static byte[] readByteArray(ByteBuf buf, int maxSize) {
        int j = VarInts.read(buf);
        if (j > maxSize) {
            throw new DecoderException("ByteArray with size " + j + " is bigger than allowed " + maxSize);
        }
        byte[] bs = new byte[j];
        buf.readBytes(bs);
        return bs;
    }

    public PacketByteBuf writeIntArray(int[] array) {
        this.writeVarInt(array.length);
        for (int i : array) {
            this.writeVarInt(i);
        }
        return this;
    }

    public int[] readIntArray() {
        return this.readIntArray(this.readableBytes());
    }

    public int[] readIntArray(int maxSize) {
        int j = this.readVarInt();
        if (j > maxSize) {
            throw new DecoderException("VarIntArray with size " + j + " is bigger than allowed " + maxSize);
        }
        int[] is = new int[j];
        for (int k = 0; k < is.length; ++k) {
            is[k] = this.readVarInt();
        }
        return is;
    }

    public PacketByteBuf writeLongArray(long[] array) {
        this.writeVarInt(array.length);
        for (long l : array) {
            this.writeLong(l);
        }
        return this;
    }

    public long[] readLongArray() {
        return this.readLongArray(null);
    }

    public long[] readLongArray(@Nullable long[] toArray) {
        return this.readLongArray(toArray, this.readableBytes() / 8);
    }

    public long[] readLongArray(@Nullable long[] toArray, int maxSize) {
        int j = this.readVarInt();
        if (toArray == null || toArray.length != j) {
            if (j > maxSize) {
                throw new DecoderException("LongArray with size " + j + " is bigger than allowed " + maxSize);
            }
            toArray = new long[j];
        }
        for (int k = 0; k < toArray.length; ++k) {
            toArray[k] = this.readLong();
        }
        return toArray;
    }

    public BlockPos readBlockPos() {
        return PacketByteBuf.readBlockPos(this);
    }

    public static BlockPos readBlockPos(ByteBuf buf) {
        return BlockPos.fromLong(buf.readLong());
    }

    public PacketByteBuf writeBlockPos(BlockPos pos) {
        PacketByteBuf.writeBlockPos(this, pos);
        return this;
    }

    public static void writeBlockPos(ByteBuf buf, BlockPos pos) {
        buf.writeLong(pos.asLong());
    }

    public ChunkPos readChunkPos() {
        return new ChunkPos(this.readLong());
    }

    public PacketByteBuf writeChunkPos(ChunkPos pos) {
        this.writeLong(pos.toLong());
        return this;
    }

    public ChunkSectionPos readChunkSectionPos() {
        return ChunkSectionPos.from(this.readLong());
    }

    public PacketByteBuf writeChunkSectionPos(ChunkSectionPos pos) {
        this.writeLong(pos.asLong());
        return this;
    }

    public GlobalPos readGlobalPos() {
        RegistryKey<World> lv = this.readRegistryKey(RegistryKeys.WORLD);
        BlockPos lv2 = this.readBlockPos();
        return GlobalPos.create(lv, lv2);
    }

    public void writeGlobalPos(GlobalPos pos) {
        this.writeRegistryKey(pos.dimension());
        this.writeBlockPos(pos.pos());
    }

    public Vector3f readVector3f() {
        return PacketByteBuf.readVector3f(this);
    }

    public static Vector3f readVector3f(ByteBuf buf) {
        return new Vector3f(buf.readFloat(), buf.readFloat(), buf.readFloat());
    }

    public void writeVector3f(Vector3f vector3f) {
        PacketByteBuf.writeVector3f(this, vector3f);
    }

    public static void writeVector3f(ByteBuf buf, Vector3f vector) {
        buf.writeFloat(vector.x());
        buf.writeFloat(vector.y());
        buf.writeFloat(vector.z());
    }

    public Quaternionf readQuaternionf() {
        return PacketByteBuf.readQuaternionf(this);
    }

    public static Quaternionf readQuaternionf(ByteBuf buf) {
        return new Quaternionf(buf.readFloat(), buf.readFloat(), buf.readFloat(), buf.readFloat());
    }

    public void writeQuaternionf(Quaternionf quaternionf) {
        PacketByteBuf.writeQuaternionf(this, quaternionf);
    }

    public static void writeQuaternionf(ByteBuf buf, Quaternionf quaternion) {
        buf.writeFloat(quaternion.x);
        buf.writeFloat(quaternion.y);
        buf.writeFloat(quaternion.z);
        buf.writeFloat(quaternion.w);
    }

    public Vec3d readVec3d() {
        return new Vec3d(this.readDouble(), this.readDouble(), this.readDouble());
    }

    public void writeVec3d(Vec3d vec) {
        this.writeDouble(vec.getX());
        this.writeDouble(vec.getY());
        this.writeDouble(vec.getZ());
    }

    public <T extends Enum<T>> T readEnumConstant(Class<T> enumClass) {
        return (T)((Enum[])enumClass.getEnumConstants())[this.readVarInt()];
    }

    public PacketByteBuf writeEnumConstant(Enum<?> instance) {
        return this.writeVarInt(instance.ordinal());
    }

    public <T> T decode(IntFunction<T> idToValue) {
        int i = this.readVarInt();
        return idToValue.apply(i);
    }

    public <T> PacketByteBuf encode(ToIntFunction<T> valueToId, T value) {
        int i = valueToId.applyAsInt(value);
        return this.writeVarInt(i);
    }

    public int readVarInt() {
        return VarInts.read(this.parent);
    }

    public long readVarLong() {
        return VarLongs.read(this.parent);
    }

    public PacketByteBuf writeUuid(UUID uuid) {
        PacketByteBuf.writeUuid(this, uuid);
        return this;
    }

    public static void writeUuid(ByteBuf buf, UUID uuid) {
        buf.writeLong(uuid.getMostSignificantBits());
        buf.writeLong(uuid.getLeastSignificantBits());
    }

    public UUID readUuid() {
        return PacketByteBuf.readUuid(this);
    }

    public static UUID readUuid(ByteBuf buf) {
        return new UUID(buf.readLong(), buf.readLong());
    }

    public PacketByteBuf writeVarInt(int value) {
        VarInts.write(this.parent, value);
        return this;
    }

    public PacketByteBuf writeVarLong(long value) {
        VarLongs.write(this.parent, value);
        return this;
    }

    public PacketByteBuf writeNbt(@Nullable NbtElement nbt) {
        PacketByteBuf.writeNbt(this, nbt);
        return this;
    }

    public static void writeNbt(ByteBuf buf, @Nullable NbtElement nbt) {
        if (nbt == null) {
            nbt = NbtEnd.INSTANCE;
        }
        try {
            NbtIo.writeForPacket(nbt, new ByteBufOutputStream(buf));
        } catch (IOException iOException) {
            throw new EncoderException(iOException);
        }
    }

    @Nullable
    public NbtCompound readNbt() {
        return PacketByteBuf.readNbt(this);
    }

    @Nullable
    public static NbtCompound readNbt(ByteBuf buf) {
        NbtElement lv = PacketByteBuf.readNbt(buf, NbtSizeTracker.of(0x200000L));
        if (lv == null || lv instanceof NbtCompound) {
            return (NbtCompound)lv;
        }
        throw new DecoderException("Not a compound tag: " + String.valueOf(lv));
    }

    @Nullable
    public static NbtElement readNbt(ByteBuf buf, NbtSizeTracker sizeTracker) {
        try {
            NbtElement lv = NbtIo.read(new ByteBufInputStream(buf), sizeTracker);
            if (lv.getType() == 0) {
                return null;
            }
            return lv;
        } catch (IOException iOException) {
            throw new EncoderException(iOException);
        }
    }

    @Nullable
    public NbtElement readNbt(NbtSizeTracker sizeTracker) {
        return PacketByteBuf.readNbt(this, sizeTracker);
    }

    public String readString() {
        return this.readString(DEFAULT_MAX_STRING_LENGTH);
    }

    public String readString(int maxLength) {
        return StringEncoding.decode(this.parent, maxLength);
    }

    public PacketByteBuf writeString(String string) {
        return this.writeString(string, DEFAULT_MAX_STRING_LENGTH);
    }

    public PacketByteBuf writeString(String string, int maxLength) {
        StringEncoding.encode(this.parent, string, maxLength);
        return this;
    }

    public Identifier readIdentifier() {
        return Identifier.of(this.readString(DEFAULT_MAX_STRING_LENGTH));
    }

    public PacketByteBuf writeIdentifier(Identifier id) {
        this.writeString(id.toString());
        return this;
    }

    public <T> RegistryKey<T> readRegistryKey(RegistryKey<? extends Registry<T>> registryRef) {
        Identifier lv = this.readIdentifier();
        return RegistryKey.of(registryRef, lv);
    }

    public void writeRegistryKey(RegistryKey<?> key) {
        this.writeIdentifier(key.getValue());
    }

    public <T> RegistryKey<? extends Registry<T>> readRegistryRefKey() {
        Identifier lv = this.readIdentifier();
        return RegistryKey.ofRegistry(lv);
    }

    public Date readDate() {
        return new Date(this.readLong());
    }

    public PacketByteBuf writeDate(Date date) {
        this.writeLong(date.getTime());
        return this;
    }

    public Instant readInstant() {
        return Instant.ofEpochMilli(this.readLong());
    }

    public void writeInstant(Instant instant) {
        this.writeLong(instant.toEpochMilli());
    }

    public PublicKey readPublicKey() {
        try {
            return NetworkEncryptionUtils.decodeEncodedRsaPublicKey(this.readByteArray(512));
        } catch (NetworkEncryptionException lv) {
            throw new DecoderException("Malformed public key bytes", lv);
        }
    }

    public PacketByteBuf writePublicKey(PublicKey publicKey) {
        this.writeByteArray(publicKey.getEncoded());
        return this;
    }

    public BlockHitResult readBlockHitResult() {
        BlockPos lv = this.readBlockPos();
        Direction lv2 = this.readEnumConstant(Direction.class);
        float f = this.readFloat();
        float g = this.readFloat();
        float h = this.readFloat();
        boolean bl = this.readBoolean();
        return new BlockHitResult(new Vec3d((double)lv.getX() + (double)f, (double)lv.getY() + (double)g, (double)lv.getZ() + (double)h), lv2, lv, bl);
    }

    public void writeBlockHitResult(BlockHitResult hitResult) {
        BlockPos lv = hitResult.getBlockPos();
        this.writeBlockPos(lv);
        this.writeEnumConstant(hitResult.getSide());
        Vec3d lv2 = hitResult.getPos();
        this.writeFloat((float)(lv2.x - (double)lv.getX()));
        this.writeFloat((float)(lv2.y - (double)lv.getY()));
        this.writeFloat((float)(lv2.z - (double)lv.getZ()));
        this.writeBoolean(hitResult.isInsideBlock());
    }

    public BitSet readBitSet() {
        return BitSet.valueOf(this.readLongArray());
    }

    public void writeBitSet(BitSet bitSet) {
        this.writeLongArray(bitSet.toLongArray());
    }

    public BitSet readBitSet(int size) {
        byte[] bs = new byte[MathHelper.ceilDiv(size, 8)];
        this.readBytes(bs);
        return BitSet.valueOf(bs);
    }

    public void writeBitSet(BitSet bitSet, int size) {
        if (bitSet.length() > size) {
            throw new EncoderException("BitSet is larger than expected size (" + bitSet.length() + ">" + size + ")");
        }
        byte[] bs = bitSet.toByteArray();
        this.writeBytes(Arrays.copyOf(bs, MathHelper.ceilDiv(size, 8)));
    }

    @Override
    public boolean isContiguous() {
        return this.parent.isContiguous();
    }

    @Override
    public int maxFastWritableBytes() {
        return this.parent.maxFastWritableBytes();
    }

    @Override
    public int capacity() {
        return this.parent.capacity();
    }

    @Override
    public PacketByteBuf capacity(int i) {
        this.parent.capacity(i);
        return this;
    }

    @Override
    public int maxCapacity() {
        return this.parent.maxCapacity();
    }

    @Override
    public ByteBufAllocator alloc() {
        return this.parent.alloc();
    }

    @Override
    public ByteOrder order() {
        return this.parent.order();
    }

    @Override
    public ByteBuf order(ByteOrder byteOrder) {
        return this.parent.order(byteOrder);
    }

    @Override
    public ByteBuf unwrap() {
        return this.parent;
    }

    @Override
    public boolean isDirect() {
        return this.parent.isDirect();
    }

    @Override
    public boolean isReadOnly() {
        return this.parent.isReadOnly();
    }

    @Override
    public ByteBuf asReadOnly() {
        return this.parent.asReadOnly();
    }

    @Override
    public int readerIndex() {
        return this.parent.readerIndex();
    }

    @Override
    public PacketByteBuf readerIndex(int i) {
        this.parent.readerIndex(i);
        return this;
    }

    @Override
    public int writerIndex() {
        return this.parent.writerIndex();
    }

    @Override
    public PacketByteBuf writerIndex(int i) {
        this.parent.writerIndex(i);
        return this;
    }

    @Override
    public PacketByteBuf setIndex(int i, int j) {
        this.parent.setIndex(i, j);
        return this;
    }

    @Override
    public int readableBytes() {
        return this.parent.readableBytes();
    }

    @Override
    public int writableBytes() {
        return this.parent.writableBytes();
    }

    @Override
    public int maxWritableBytes() {
        return this.parent.maxWritableBytes();
    }

    @Override
    public boolean isReadable() {
        return this.parent.isReadable();
    }

    @Override
    public boolean isReadable(int size) {
        return this.parent.isReadable(size);
    }

    @Override
    public boolean isWritable() {
        return this.parent.isWritable();
    }

    @Override
    public boolean isWritable(int size) {
        return this.parent.isWritable(size);
    }

    @Override
    public PacketByteBuf clear() {
        this.parent.clear();
        return this;
    }

    @Override
    public PacketByteBuf markReaderIndex() {
        this.parent.markReaderIndex();
        return this;
    }

    @Override
    public PacketByteBuf resetReaderIndex() {
        this.parent.resetReaderIndex();
        return this;
    }

    @Override
    public PacketByteBuf markWriterIndex() {
        this.parent.markWriterIndex();
        return this;
    }

    @Override
    public PacketByteBuf resetWriterIndex() {
        this.parent.resetWriterIndex();
        return this;
    }

    @Override
    public PacketByteBuf discardReadBytes() {
        this.parent.discardReadBytes();
        return this;
    }

    @Override
    public PacketByteBuf discardSomeReadBytes() {
        this.parent.discardSomeReadBytes();
        return this;
    }

    @Override
    public PacketByteBuf ensureWritable(int i) {
        this.parent.ensureWritable(i);
        return this;
    }

    @Override
    public int ensureWritable(int minBytes, boolean force) {
        return this.parent.ensureWritable(minBytes, force);
    }

    @Override
    public boolean getBoolean(int index) {
        return this.parent.getBoolean(index);
    }

    @Override
    public byte getByte(int index) {
        return this.parent.getByte(index);
    }

    @Override
    public short getUnsignedByte(int index) {
        return this.parent.getUnsignedByte(index);
    }

    @Override
    public short getShort(int index) {
        return this.parent.getShort(index);
    }

    @Override
    public short getShortLE(int index) {
        return this.parent.getShortLE(index);
    }

    @Override
    public int getUnsignedShort(int index) {
        return this.parent.getUnsignedShort(index);
    }

    @Override
    public int getUnsignedShortLE(int index) {
        return this.parent.getUnsignedShortLE(index);
    }

    @Override
    public int getMedium(int index) {
        return this.parent.getMedium(index);
    }

    @Override
    public int getMediumLE(int index) {
        return this.parent.getMediumLE(index);
    }

    @Override
    public int getUnsignedMedium(int index) {
        return this.parent.getUnsignedMedium(index);
    }

    @Override
    public int getUnsignedMediumLE(int index) {
        return this.parent.getUnsignedMediumLE(index);
    }

    @Override
    public int getInt(int index) {
        return this.parent.getInt(index);
    }

    @Override
    public int getIntLE(int index) {
        return this.parent.getIntLE(index);
    }

    @Override
    public long getUnsignedInt(int index) {
        return this.parent.getUnsignedInt(index);
    }

    @Override
    public long getUnsignedIntLE(int index) {
        return this.parent.getUnsignedIntLE(index);
    }

    @Override
    public long getLong(int index) {
        return this.parent.getLong(index);
    }

    @Override
    public long getLongLE(int index) {
        return this.parent.getLongLE(index);
    }

    @Override
    public char getChar(int index) {
        return this.parent.getChar(index);
    }

    @Override
    public float getFloat(int index) {
        return this.parent.getFloat(index);
    }

    @Override
    public double getDouble(int index) {
        return this.parent.getDouble(index);
    }

    @Override
    public PacketByteBuf getBytes(int i, ByteBuf byteBuf) {
        this.parent.getBytes(i, byteBuf);
        return this;
    }

    @Override
    public PacketByteBuf getBytes(int i, ByteBuf byteBuf, int j) {
        this.parent.getBytes(i, byteBuf, j);
        return this;
    }

    @Override
    public PacketByteBuf getBytes(int i, ByteBuf byteBuf, int j, int k) {
        this.parent.getBytes(i, byteBuf, j, k);
        return this;
    }

    @Override
    public PacketByteBuf getBytes(int i, byte[] bs) {
        this.parent.getBytes(i, bs);
        return this;
    }

    @Override
    public PacketByteBuf getBytes(int i, byte[] bs, int j, int k) {
        this.parent.getBytes(i, bs, j, k);
        return this;
    }

    @Override
    public PacketByteBuf getBytes(int i, ByteBuffer byteBuffer) {
        this.parent.getBytes(i, byteBuffer);
        return this;
    }

    @Override
    public PacketByteBuf getBytes(int i, OutputStream outputStream, int j) throws IOException {
        this.parent.getBytes(i, outputStream, j);
        return this;
    }

    @Override
    public int getBytes(int index, GatheringByteChannel channel, int length) throws IOException {
        return this.parent.getBytes(index, channel, length);
    }

    @Override
    public int getBytes(int index, FileChannel channel, long pos, int length) throws IOException {
        return this.parent.getBytes(index, channel, pos, length);
    }

    @Override
    public CharSequence getCharSequence(int index, int length, Charset charset) {
        return this.parent.getCharSequence(index, length, charset);
    }

    @Override
    public PacketByteBuf setBoolean(int i, boolean bl) {
        this.parent.setBoolean(i, bl);
        return this;
    }

    @Override
    public PacketByteBuf setByte(int i, int j) {
        this.parent.setByte(i, j);
        return this;
    }

    @Override
    public PacketByteBuf setShort(int i, int j) {
        this.parent.setShort(i, j);
        return this;
    }

    @Override
    public PacketByteBuf setShortLE(int i, int j) {
        this.parent.setShortLE(i, j);
        return this;
    }

    @Override
    public PacketByteBuf setMedium(int i, int j) {
        this.parent.setMedium(i, j);
        return this;
    }

    @Override
    public PacketByteBuf setMediumLE(int i, int j) {
        this.parent.setMediumLE(i, j);
        return this;
    }

    @Override
    public PacketByteBuf setInt(int i, int j) {
        this.parent.setInt(i, j);
        return this;
    }

    @Override
    public PacketByteBuf setIntLE(int i, int j) {
        this.parent.setIntLE(i, j);
        return this;
    }

    @Override
    public PacketByteBuf setLong(int i, long l) {
        this.parent.setLong(i, l);
        return this;
    }

    @Override
    public PacketByteBuf setLongLE(int i, long l) {
        this.parent.setLongLE(i, l);
        return this;
    }

    @Override
    public PacketByteBuf setChar(int i, int j) {
        this.parent.setChar(i, j);
        return this;
    }

    @Override
    public PacketByteBuf setFloat(int i, float f) {
        this.parent.setFloat(i, f);
        return this;
    }

    @Override
    public PacketByteBuf setDouble(int i, double d) {
        this.parent.setDouble(i, d);
        return this;
    }

    @Override
    public PacketByteBuf setBytes(int i, ByteBuf byteBuf) {
        this.parent.setBytes(i, byteBuf);
        return this;
    }

    @Override
    public PacketByteBuf setBytes(int i, ByteBuf byteBuf, int j) {
        this.parent.setBytes(i, byteBuf, j);
        return this;
    }

    @Override
    public PacketByteBuf setBytes(int i, ByteBuf byteBuf, int j, int k) {
        this.parent.setBytes(i, byteBuf, j, k);
        return this;
    }

    @Override
    public PacketByteBuf setBytes(int i, byte[] bs) {
        this.parent.setBytes(i, bs);
        return this;
    }

    @Override
    public PacketByteBuf setBytes(int i, byte[] bs, int j, int k) {
        this.parent.setBytes(i, bs, j, k);
        return this;
    }

    @Override
    public PacketByteBuf setBytes(int i, ByteBuffer byteBuffer) {
        this.parent.setBytes(i, byteBuffer);
        return this;
    }

    @Override
    public int setBytes(int index, InputStream stream, int length) throws IOException {
        return this.parent.setBytes(index, stream, length);
    }

    @Override
    public int setBytes(int index, ScatteringByteChannel channel, int length) throws IOException {
        return this.parent.setBytes(index, channel, length);
    }

    @Override
    public int setBytes(int index, FileChannel channel, long pos, int length) throws IOException {
        return this.parent.setBytes(index, channel, pos, length);
    }

    @Override
    public PacketByteBuf setZero(int i, int j) {
        this.parent.setZero(i, j);
        return this;
    }

    @Override
    public int setCharSequence(int index, CharSequence sequence, Charset charset) {
        return this.parent.setCharSequence(index, sequence, charset);
    }

    @Override
    public boolean readBoolean() {
        return this.parent.readBoolean();
    }

    @Override
    public byte readByte() {
        return this.parent.readByte();
    }

    @Override
    public short readUnsignedByte() {
        return this.parent.readUnsignedByte();
    }

    @Override
    public short readShort() {
        return this.parent.readShort();
    }

    @Override
    public short readShortLE() {
        return this.parent.readShortLE();
    }

    @Override
    public int readUnsignedShort() {
        return this.parent.readUnsignedShort();
    }

    @Override
    public int readUnsignedShortLE() {
        return this.parent.readUnsignedShortLE();
    }

    @Override
    public int readMedium() {
        return this.parent.readMedium();
    }

    @Override
    public int readMediumLE() {
        return this.parent.readMediumLE();
    }

    @Override
    public int readUnsignedMedium() {
        return this.parent.readUnsignedMedium();
    }

    @Override
    public int readUnsignedMediumLE() {
        return this.parent.readUnsignedMediumLE();
    }

    @Override
    public int readInt() {
        return this.parent.readInt();
    }

    @Override
    public int readIntLE() {
        return this.parent.readIntLE();
    }

    @Override
    public long readUnsignedInt() {
        return this.parent.readUnsignedInt();
    }

    @Override
    public long readUnsignedIntLE() {
        return this.parent.readUnsignedIntLE();
    }

    @Override
    public long readLong() {
        return this.parent.readLong();
    }

    @Override
    public long readLongLE() {
        return this.parent.readLongLE();
    }

    @Override
    public char readChar() {
        return this.parent.readChar();
    }

    @Override
    public float readFloat() {
        return this.parent.readFloat();
    }

    @Override
    public double readDouble() {
        return this.parent.readDouble();
    }

    @Override
    public ByteBuf readBytes(int length) {
        return this.parent.readBytes(length);
    }

    @Override
    public ByteBuf readSlice(int length) {
        return this.parent.readSlice(length);
    }

    @Override
    public ByteBuf readRetainedSlice(int length) {
        return this.parent.readRetainedSlice(length);
    }

    @Override
    public PacketByteBuf readBytes(ByteBuf byteBuf) {
        this.parent.readBytes(byteBuf);
        return this;
    }

    @Override
    public PacketByteBuf readBytes(ByteBuf byteBuf, int i) {
        this.parent.readBytes(byteBuf, i);
        return this;
    }

    @Override
    public PacketByteBuf readBytes(ByteBuf byteBuf, int i, int j) {
        this.parent.readBytes(byteBuf, i, j);
        return this;
    }

    @Override
    public PacketByteBuf readBytes(byte[] bs) {
        this.parent.readBytes(bs);
        return this;
    }

    @Override
    public PacketByteBuf readBytes(byte[] bs, int i, int j) {
        this.parent.readBytes(bs, i, j);
        return this;
    }

    @Override
    public PacketByteBuf readBytes(ByteBuffer byteBuffer) {
        this.parent.readBytes(byteBuffer);
        return this;
    }

    @Override
    public PacketByteBuf readBytes(OutputStream outputStream, int i) throws IOException {
        this.parent.readBytes(outputStream, i);
        return this;
    }

    @Override
    public int readBytes(GatheringByteChannel channel, int length) throws IOException {
        return this.parent.readBytes(channel, length);
    }

    @Override
    public CharSequence readCharSequence(int length, Charset charset) {
        return this.parent.readCharSequence(length, charset);
    }

    @Override
    public int readBytes(FileChannel channel, long pos, int length) throws IOException {
        return this.parent.readBytes(channel, pos, length);
    }

    @Override
    public PacketByteBuf skipBytes(int i) {
        this.parent.skipBytes(i);
        return this;
    }

    @Override
    public PacketByteBuf writeBoolean(boolean bl) {
        this.parent.writeBoolean(bl);
        return this;
    }

    @Override
    public PacketByteBuf writeByte(int i) {
        this.parent.writeByte(i);
        return this;
    }

    @Override
    public PacketByteBuf writeShort(int i) {
        this.parent.writeShort(i);
        return this;
    }

    @Override
    public PacketByteBuf writeShortLE(int i) {
        this.parent.writeShortLE(i);
        return this;
    }

    @Override
    public PacketByteBuf writeMedium(int i) {
        this.parent.writeMedium(i);
        return this;
    }

    @Override
    public PacketByteBuf writeMediumLE(int i) {
        this.parent.writeMediumLE(i);
        return this;
    }

    @Override
    public PacketByteBuf writeInt(int i) {
        this.parent.writeInt(i);
        return this;
    }

    @Override
    public PacketByteBuf writeIntLE(int i) {
        this.parent.writeIntLE(i);
        return this;
    }

    @Override
    public PacketByteBuf writeLong(long l) {
        this.parent.writeLong(l);
        return this;
    }

    @Override
    public PacketByteBuf writeLongLE(long l) {
        this.parent.writeLongLE(l);
        return this;
    }

    @Override
    public PacketByteBuf writeChar(int i) {
        this.parent.writeChar(i);
        return this;
    }

    @Override
    public PacketByteBuf writeFloat(float f) {
        this.parent.writeFloat(f);
        return this;
    }

    @Override
    public PacketByteBuf writeDouble(double d) {
        this.parent.writeDouble(d);
        return this;
    }

    @Override
    public PacketByteBuf writeBytes(ByteBuf byteBuf) {
        this.parent.writeBytes(byteBuf);
        return this;
    }

    @Override
    public PacketByteBuf writeBytes(ByteBuf byteBuf, int i) {
        this.parent.writeBytes(byteBuf, i);
        return this;
    }

    @Override
    public PacketByteBuf writeBytes(ByteBuf byteBuf, int i, int j) {
        this.parent.writeBytes(byteBuf, i, j);
        return this;
    }

    @Override
    public PacketByteBuf writeBytes(byte[] bs) {
        this.parent.writeBytes(bs);
        return this;
    }

    @Override
    public PacketByteBuf writeBytes(byte[] bs, int i, int j) {
        this.parent.writeBytes(bs, i, j);
        return this;
    }

    @Override
    public PacketByteBuf writeBytes(ByteBuffer byteBuffer) {
        this.parent.writeBytes(byteBuffer);
        return this;
    }

    @Override
    public int writeBytes(InputStream stream, int length) throws IOException {
        return this.parent.writeBytes(stream, length);
    }

    @Override
    public int writeBytes(ScatteringByteChannel channel, int length) throws IOException {
        return this.parent.writeBytes(channel, length);
    }

    @Override
    public int writeBytes(FileChannel channel, long pos, int length) throws IOException {
        return this.parent.writeBytes(channel, pos, length);
    }

    @Override
    public PacketByteBuf writeZero(int i) {
        this.parent.writeZero(i);
        return this;
    }

    @Override
    public int writeCharSequence(CharSequence sequence, Charset charset) {
        return this.parent.writeCharSequence(sequence, charset);
    }

    @Override
    public int indexOf(int from, int to, byte value) {
        return this.parent.indexOf(from, to, value);
    }

    @Override
    public int bytesBefore(byte value) {
        return this.parent.bytesBefore(value);
    }

    @Override
    public int bytesBefore(int length, byte value) {
        return this.parent.bytesBefore(length, value);
    }

    @Override
    public int bytesBefore(int index, int length, byte value) {
        return this.parent.bytesBefore(index, length, value);
    }

    @Override
    public int forEachByte(ByteProcessor byteProcessor) {
        return this.parent.forEachByte(byteProcessor);
    }

    @Override
    public int forEachByte(int index, int length, ByteProcessor byteProcessor) {
        return this.parent.forEachByte(index, length, byteProcessor);
    }

    @Override
    public int forEachByteDesc(ByteProcessor byteProcessor) {
        return this.parent.forEachByteDesc(byteProcessor);
    }

    @Override
    public int forEachByteDesc(int index, int length, ByteProcessor byteProcessor) {
        return this.parent.forEachByteDesc(index, length, byteProcessor);
    }

    @Override
    public ByteBuf copy() {
        return this.parent.copy();
    }

    @Override
    public ByteBuf copy(int index, int length) {
        return this.parent.copy(index, length);
    }

    @Override
    public ByteBuf slice() {
        return this.parent.slice();
    }

    @Override
    public ByteBuf retainedSlice() {
        return this.parent.retainedSlice();
    }

    @Override
    public ByteBuf slice(int index, int length) {
        return this.parent.slice(index, length);
    }

    @Override
    public ByteBuf retainedSlice(int index, int length) {
        return this.parent.retainedSlice(index, length);
    }

    @Override
    public ByteBuf duplicate() {
        return this.parent.duplicate();
    }

    @Override
    public ByteBuf retainedDuplicate() {
        return this.parent.retainedDuplicate();
    }

    @Override
    public int nioBufferCount() {
        return this.parent.nioBufferCount();
    }

    @Override
    public ByteBuffer nioBuffer() {
        return this.parent.nioBuffer();
    }

    @Override
    public ByteBuffer nioBuffer(int index, int length) {
        return this.parent.nioBuffer(index, length);
    }

    @Override
    public ByteBuffer internalNioBuffer(int index, int length) {
        return this.parent.internalNioBuffer(index, length);
    }

    @Override
    public ByteBuffer[] nioBuffers() {
        return this.parent.nioBuffers();
    }

    @Override
    public ByteBuffer[] nioBuffers(int index, int length) {
        return this.parent.nioBuffers(index, length);
    }

    @Override
    public boolean hasArray() {
        return this.parent.hasArray();
    }

    @Override
    public byte[] array() {
        return this.parent.array();
    }

    @Override
    public int arrayOffset() {
        return this.parent.arrayOffset();
    }

    @Override
    public boolean hasMemoryAddress() {
        return this.parent.hasMemoryAddress();
    }

    @Override
    public long memoryAddress() {
        return this.parent.memoryAddress();
    }

    @Override
    public String toString(Charset charset) {
        return this.parent.toString(charset);
    }

    @Override
    public String toString(int index, int length, Charset charset) {
        return this.parent.toString(index, length, charset);
    }

    @Override
    public int hashCode() {
        return this.parent.hashCode();
    }

    @Override
    public boolean equals(Object o) {
        return this.parent.equals(o);
    }

    @Override
    public int compareTo(ByteBuf byteBuf) {
        return this.parent.compareTo(byteBuf);
    }

    @Override
    public String toString() {
        return this.parent.toString();
    }

    @Override
    public PacketByteBuf retain(int i) {
        this.parent.retain(i);
        return this;
    }

    @Override
    public PacketByteBuf retain() {
        this.parent.retain();
        return this;
    }

    @Override
    public PacketByteBuf touch() {
        this.parent.touch();
        return this;
    }

    @Override
    public PacketByteBuf touch(Object object) {
        this.parent.touch(object);
        return this;
    }

    @Override
    public int refCnt() {
        return this.parent.refCnt();
    }

    @Override
    public boolean release() {
        return this.parent.release();
    }

    @Override
    public boolean release(int decrement) {
        return this.parent.release(decrement);
    }

    @Override
    public /* synthetic */ ByteBuf touch(Object object) {
        return this.touch(object);
    }

    @Override
    public /* synthetic */ ByteBuf touch() {
        return this.touch();
    }

    @Override
    public /* synthetic */ ByteBuf retain() {
        return this.retain();
    }

    @Override
    public /* synthetic */ ByteBuf retain(int increment) {
        return this.retain(increment);
    }

    @Override
    public /* synthetic */ ByteBuf writeZero(int length) {
        return this.writeZero(length);
    }

    @Override
    public /* synthetic */ ByteBuf writeBytes(ByteBuffer buf) {
        return this.writeBytes(buf);
    }

    @Override
    public /* synthetic */ ByteBuf writeBytes(byte[] bytes, int sourceIndex, int length) {
        return this.writeBytes(bytes, sourceIndex, length);
    }

    @Override
    public /* synthetic */ ByteBuf writeBytes(byte[] bytes) {
        return this.writeBytes(bytes);
    }

    @Override
    public /* synthetic */ ByteBuf writeBytes(ByteBuf buf, int sourceIndex, int length) {
        return this.writeBytes(buf, sourceIndex, length);
    }

    @Override
    public /* synthetic */ ByteBuf writeBytes(ByteBuf buf, int length) {
        return this.writeBytes(buf, length);
    }

    @Override
    public /* synthetic */ ByteBuf writeBytes(ByteBuf buf) {
        return this.writeBytes(buf);
    }

    @Override
    public /* synthetic */ ByteBuf writeDouble(double value) {
        return this.writeDouble(value);
    }

    @Override
    public /* synthetic */ ByteBuf writeFloat(float value) {
        return this.writeFloat(value);
    }

    @Override
    public /* synthetic */ ByteBuf writeChar(int value) {
        return this.writeChar(value);
    }

    @Override
    public /* synthetic */ ByteBuf writeLongLE(long value) {
        return this.writeLongLE(value);
    }

    @Override
    public /* synthetic */ ByteBuf writeLong(long value) {
        return this.writeLong(value);
    }

    @Override
    public /* synthetic */ ByteBuf writeIntLE(int value) {
        return this.writeIntLE(value);
    }

    @Override
    public /* synthetic */ ByteBuf writeInt(int value) {
        return this.writeInt(value);
    }

    @Override
    public /* synthetic */ ByteBuf writeMediumLE(int value) {
        return this.writeMediumLE(value);
    }

    @Override
    public /* synthetic */ ByteBuf writeMedium(int value) {
        return this.writeMedium(value);
    }

    @Override
    public /* synthetic */ ByteBuf writeShortLE(int value) {
        return this.writeShortLE(value);
    }

    @Override
    public /* synthetic */ ByteBuf writeShort(int value) {
        return this.writeShort(value);
    }

    @Override
    public /* synthetic */ ByteBuf writeByte(int value) {
        return this.writeByte(value);
    }

    @Override
    public /* synthetic */ ByteBuf writeBoolean(boolean value) {
        return this.writeBoolean(value);
    }

    @Override
    public /* synthetic */ ByteBuf skipBytes(int length) {
        return this.skipBytes(length);
    }

    @Override
    public /* synthetic */ ByteBuf readBytes(OutputStream stream, int length) throws IOException {
        return this.readBytes(stream, length);
    }

    @Override
    public /* synthetic */ ByteBuf readBytes(ByteBuffer buf) {
        return this.readBytes(buf);
    }

    @Override
    public /* synthetic */ ByteBuf readBytes(byte[] bytes, int outputIndex, int length) {
        return this.readBytes(bytes, outputIndex, length);
    }

    @Override
    public /* synthetic */ ByteBuf readBytes(byte[] bytes) {
        return this.readBytes(bytes);
    }

    @Override
    public /* synthetic */ ByteBuf readBytes(ByteBuf buf, int outputIndex, int length) {
        return this.readBytes(buf, outputIndex, length);
    }

    @Override
    public /* synthetic */ ByteBuf readBytes(ByteBuf buf, int length) {
        return this.readBytes(buf, length);
    }

    @Override
    public /* synthetic */ ByteBuf readBytes(ByteBuf buf) {
        return this.readBytes(buf);
    }

    @Override
    public /* synthetic */ ByteBuf setZero(int index, int length) {
        return this.setZero(index, length);
    }

    @Override
    public /* synthetic */ ByteBuf setBytes(int index, ByteBuffer buf) {
        return this.setBytes(index, buf);
    }

    @Override
    public /* synthetic */ ByteBuf setBytes(int index, byte[] bytes, int sourceIndex, int length) {
        return this.setBytes(index, bytes, sourceIndex, length);
    }

    @Override
    public /* synthetic */ ByteBuf setBytes(int index, byte[] bytes) {
        return this.setBytes(index, bytes);
    }

    @Override
    public /* synthetic */ ByteBuf setBytes(int index, ByteBuf buf, int sourceIndex, int length) {
        return this.setBytes(index, buf, sourceIndex, length);
    }

    @Override
    public /* synthetic */ ByteBuf setBytes(int index, ByteBuf buf, int length) {
        return this.setBytes(index, buf, length);
    }

    @Override
    public /* synthetic */ ByteBuf setBytes(int index, ByteBuf buf) {
        return this.setBytes(index, buf);
    }

    @Override
    public /* synthetic */ ByteBuf setDouble(int index, double value) {
        return this.setDouble(index, value);
    }

    @Override
    public /* synthetic */ ByteBuf setFloat(int index, float value) {
        return this.setFloat(index, value);
    }

    @Override
    public /* synthetic */ ByteBuf setChar(int index, int value) {
        return this.setChar(index, value);
    }

    @Override
    public /* synthetic */ ByteBuf setLongLE(int index, long value) {
        return this.setLongLE(index, value);
    }

    @Override
    public /* synthetic */ ByteBuf setLong(int index, long value) {
        return this.setLong(index, value);
    }

    @Override
    public /* synthetic */ ByteBuf setIntLE(int index, int value) {
        return this.setIntLE(index, value);
    }

    @Override
    public /* synthetic */ ByteBuf setInt(int index, int value) {
        return this.setInt(index, value);
    }

    @Override
    public /* synthetic */ ByteBuf setMediumLE(int index, int value) {
        return this.setMediumLE(index, value);
    }

    @Override
    public /* synthetic */ ByteBuf setMedium(int index, int value) {
        return this.setMedium(index, value);
    }

    @Override
    public /* synthetic */ ByteBuf setShortLE(int index, int value) {
        return this.setShortLE(index, value);
    }

    @Override
    public /* synthetic */ ByteBuf setShort(int index, int value) {
        return this.setShort(index, value);
    }

    @Override
    public /* synthetic */ ByteBuf setByte(int index, int value) {
        return this.setByte(index, value);
    }

    @Override
    public /* synthetic */ ByteBuf setBoolean(int index, boolean value) {
        return this.setBoolean(index, value);
    }

    @Override
    public /* synthetic */ ByteBuf getBytes(int index, OutputStream stream, int length) throws IOException {
        return this.getBytes(index, stream, length);
    }

    @Override
    public /* synthetic */ ByteBuf getBytes(int index, ByteBuffer buf) {
        return this.getBytes(index, buf);
    }

    @Override
    public /* synthetic */ ByteBuf getBytes(int index, byte[] bytes, int outputIndex, int length) {
        return this.getBytes(index, bytes, outputIndex, length);
    }

    @Override
    public /* synthetic */ ByteBuf getBytes(int index, byte[] bytes) {
        return this.getBytes(index, bytes);
    }

    @Override
    public /* synthetic */ ByteBuf getBytes(int index, ByteBuf buf, int outputIndex, int length) {
        return this.getBytes(index, buf, outputIndex, length);
    }

    @Override
    public /* synthetic */ ByteBuf getBytes(int index, ByteBuf buf, int length) {
        return this.getBytes(index, buf, length);
    }

    @Override
    public /* synthetic */ ByteBuf getBytes(int index, ByteBuf buf) {
        return this.getBytes(index, buf);
    }

    @Override
    public /* synthetic */ ByteBuf ensureWritable(int minBytes) {
        return this.ensureWritable(minBytes);
    }

    @Override
    public /* synthetic */ ByteBuf discardSomeReadBytes() {
        return this.discardSomeReadBytes();
    }

    @Override
    public /* synthetic */ ByteBuf discardReadBytes() {
        return this.discardReadBytes();
    }

    @Override
    public /* synthetic */ ByteBuf resetWriterIndex() {
        return this.resetWriterIndex();
    }

    @Override
    public /* synthetic */ ByteBuf markWriterIndex() {
        return this.markWriterIndex();
    }

    @Override
    public /* synthetic */ ByteBuf resetReaderIndex() {
        return this.resetReaderIndex();
    }

    @Override
    public /* synthetic */ ByteBuf markReaderIndex() {
        return this.markReaderIndex();
    }

    @Override
    public /* synthetic */ ByteBuf clear() {
        return this.clear();
    }

    @Override
    public /* synthetic */ ByteBuf setIndex(int readerIndex, int writerIndex) {
        return this.setIndex(readerIndex, writerIndex);
    }

    @Override
    public /* synthetic */ ByteBuf writerIndex(int index) {
        return this.writerIndex(index);
    }

    @Override
    public /* synthetic */ ByteBuf readerIndex(int index) {
        return this.readerIndex(index);
    }

    @Override
    public /* synthetic */ ByteBuf capacity(int capacity) {
        return this.capacity(capacity);
    }

    @Override
    public /* synthetic */ ReferenceCounted touch(Object object) {
        return this.touch(object);
    }

    @Override
    public /* synthetic */ ReferenceCounted touch() {
        return this.touch();
    }

    @Override
    public /* synthetic */ ReferenceCounted retain(int increment) {
        return this.retain(increment);
    }

    @Override
    public /* synthetic */ ReferenceCounted retain() {
        return this.retain();
    }
}

