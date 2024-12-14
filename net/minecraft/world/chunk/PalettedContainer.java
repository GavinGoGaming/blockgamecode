/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 * 
 * Could not load the following classes:
 *  org.jetbrains.annotations.Nullable
 */
package net.minecraft.world.chunk;

import com.mojang.datafixers.kinds.Applicative;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import it.unimi.dsi.fastutil.ints.Int2IntOpenHashMap;
import it.unimi.dsi.fastutil.ints.IntArraySet;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.IntUnaryOperator;
import java.util.function.Predicate;
import java.util.stream.LongStream;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.encoding.VarInts;
import net.minecraft.util.collection.EmptyPaletteStorage;
import net.minecraft.util.collection.IndexedIterable;
import net.minecraft.util.collection.PackedIntegerArray;
import net.minecraft.util.collection.PaletteStorage;
import net.minecraft.util.dynamic.Codecs;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.thread.LockHelper;
import net.minecraft.world.chunk.ArrayPalette;
import net.minecraft.world.chunk.BiMapPalette;
import net.minecraft.world.chunk.IdListPalette;
import net.minecraft.world.chunk.Palette;
import net.minecraft.world.chunk.PaletteResizeListener;
import net.minecraft.world.chunk.ReadableContainer;
import net.minecraft.world.chunk.SingularPalette;
import org.jetbrains.annotations.Nullable;

public class PalettedContainer<T>
implements PaletteResizeListener<T>,
ReadableContainer<T> {
    private static final int field_34557 = 0;
    private final PaletteResizeListener<T> dummyListener = (newSize, added) -> 0;
    private final IndexedIterable<T> idList;
    private volatile Data<T> data;
    private final PaletteProvider paletteProvider;
    private final LockHelper lockHelper = new LockHelper("PalettedContainer");

    public void lock() {
        this.lockHelper.lock();
    }

    public void unlock() {
        this.lockHelper.unlock();
    }

    public static <T> Codec<PalettedContainer<T>> createPalettedContainerCodec(IndexedIterable<T> idList, Codec<T> entryCodec, PaletteProvider paletteProvider, T defaultValue) {
        ReadableContainer.Reader lv = PalettedContainer::read;
        return PalettedContainer.createCodec(idList, entryCodec, paletteProvider, defaultValue, lv);
    }

    public static <T> Codec<ReadableContainer<T>> createReadableContainerCodec(IndexedIterable<T> idList, Codec<T> entryCodec, PaletteProvider paletteProvider, T defaultValue) {
        ReadableContainer.Reader lv = (idListx, paletteProviderx, serialized) -> PalettedContainer.read(idListx, paletteProviderx, serialized).map(result -> result);
        return PalettedContainer.createCodec(idList, entryCodec, paletteProvider, defaultValue, lv);
    }

    private static <T, C extends ReadableContainer<T>> Codec<C> createCodec(IndexedIterable<T> idList, Codec<T> entryCodec, PaletteProvider provider, T defaultValue, ReadableContainer.Reader<T, C> reader) {
        return RecordCodecBuilder.create(instance -> instance.group(((MapCodec)entryCodec.mapResult(Codecs.orElsePartial(defaultValue)).listOf().fieldOf("palette")).forGetter(ReadableContainer.Serialized::paletteEntries), Codec.LONG_STREAM.lenientOptionalFieldOf("data").forGetter(ReadableContainer.Serialized::storage)).apply((Applicative<ReadableContainer.Serialized, ?>)instance, ReadableContainer.Serialized::new)).comapFlatMap(serialized -> reader.read(idList, provider, (ReadableContainer.Serialized)serialized), container -> container.serialize(idList, provider));
    }

    public PalettedContainer(IndexedIterable<T> idList, PaletteProvider paletteProvider, DataProvider<T> dataProvider, PaletteStorage storage, List<T> paletteEntries) {
        this.idList = idList;
        this.paletteProvider = paletteProvider;
        this.data = new Data<T>(dataProvider, storage, dataProvider.factory().create(dataProvider.bits(), idList, this, paletteEntries));
    }

    private PalettedContainer(IndexedIterable<T> idList, PaletteProvider paletteProvider, Data<T> data) {
        this.idList = idList;
        this.paletteProvider = paletteProvider;
        this.data = data;
    }

    public PalettedContainer(IndexedIterable<T> idList, T object, PaletteProvider paletteProvider) {
        this.paletteProvider = paletteProvider;
        this.idList = idList;
        this.data = this.getCompatibleData(null, 0);
        this.data.palette.index(object);
    }

    private Data<T> getCompatibleData(@Nullable Data<T> previousData, int bits) {
        DataProvider<T> lv = this.paletteProvider.createDataProvider(this.idList, bits);
        if (previousData != null && lv.equals(previousData.configuration())) {
            return previousData;
        }
        return lv.createData(this.idList, this, this.paletteProvider.getContainerSize());
    }

    @Override
    public int onResize(int i, T object) {
        Data<T> lv = this.data;
        Data lv2 = this.getCompatibleData(lv, i);
        lv2.importFrom(lv.palette, lv.storage);
        this.data = lv2;
        return lv2.palette.index(object);
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public T swap(int x, int y, int z, T value) {
        this.lock();
        try {
            T t = this.swap(this.paletteProvider.computeIndex(x, y, z), value);
            return t;
        } finally {
            this.unlock();
        }
    }

    public T swapUnsafe(int x, int y, int z, T value) {
        return this.swap(this.paletteProvider.computeIndex(x, y, z), value);
    }

    private T swap(int index, T value) {
        int j = this.data.palette.index(value);
        int k = this.data.storage.swap(index, j);
        return this.data.palette.get(k);
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public void set(int x, int y, int z, T value) {
        this.lock();
        try {
            this.set(this.paletteProvider.computeIndex(x, y, z), value);
        } finally {
            this.unlock();
        }
    }

    private void set(int index, T value) {
        int j = this.data.palette.index(value);
        this.data.storage.set(index, j);
    }

    @Override
    public T get(int x, int y, int z) {
        return this.get(this.paletteProvider.computeIndex(x, y, z));
    }

    protected T get(int index) {
        Data<T> lv = this.data;
        return lv.palette.get(lv.storage.get(index));
    }

    @Override
    public void forEachValue(Consumer<T> action) {
        Palette lv = this.data.palette();
        IntArraySet intSet = new IntArraySet();
        this.data.storage.forEach(intSet::add);
        intSet.forEach(id -> action.accept(lv.get(id)));
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public void readPacket(PacketByteBuf buf) {
        this.lock();
        try {
            byte i = buf.readByte();
            Data<T> lv = this.getCompatibleData(this.data, i);
            lv.palette.readPacket(buf);
            buf.readLongArray(lv.storage.getData());
            this.data = lv;
        } finally {
            this.unlock();
        }
    }

    @Override
    public void writePacket(PacketByteBuf buf) {
        this.lock();
        try {
            this.data.writePacket(buf);
        } finally {
            this.unlock();
        }
    }

    private static <T> DataResult<PalettedContainer<T>> read(IndexedIterable<T> idList, PaletteProvider paletteProvider, ReadableContainer.Serialized<T> serialized) {
        PaletteStorage lv2;
        List<T> list = serialized.paletteEntries();
        int i = paletteProvider.getContainerSize();
        int j = paletteProvider.getBits(idList, list.size());
        DataProvider<T> lv = paletteProvider.createDataProvider(idList, j);
        if (j == 0) {
            lv2 = new EmptyPaletteStorage(i);
        } else {
            Optional<LongStream> optional = serialized.storage();
            if (optional.isEmpty()) {
                return DataResult.error(() -> "Missing values for non-zero storage");
            }
            long[] ls = optional.get().toArray();
            try {
                if (lv.factory() == PaletteProvider.ID_LIST) {
                    BiMapPalette<Object> lv3 = new BiMapPalette<Object>(idList, j, (id, value) -> 0, list);
                    PackedIntegerArray lv4 = new PackedIntegerArray(j, i, ls);
                    int[] is = new int[i];
                    lv4.writePaletteIndices(is);
                    PalettedContainer.applyEach(is, id -> idList.getRawId(lv3.get(id)));
                    lv2 = new PackedIntegerArray(lv.bits(), i, is);
                } else {
                    lv2 = new PackedIntegerArray(lv.bits(), i, ls);
                }
            } catch (PackedIntegerArray.InvalidLengthException lv5) {
                return DataResult.error(() -> "Failed to read PalettedContainer: " + lv5.getMessage());
            }
        }
        return DataResult.success(new PalettedContainer<T>(idList, paletteProvider, lv, lv2, list));
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @Override
    public ReadableContainer.Serialized<T> serialize(IndexedIterable<T> idList, PaletteProvider paletteProvider) {
        this.lock();
        try {
            Optional<LongStream> optional;
            BiMapPalette<T> lv = new BiMapPalette<T>(idList, this.data.storage.getElementBits(), this.dummyListener);
            int i = paletteProvider.getContainerSize();
            int[] is = new int[i];
            this.data.storage.writePaletteIndices(is);
            PalettedContainer.applyEach(is, id -> lv.index(this.data.palette.get(id)));
            int j = paletteProvider.getBits(idList, lv.getSize());
            if (j != 0) {
                PackedIntegerArray lv2 = new PackedIntegerArray(j, i, is);
                optional = Optional.of(Arrays.stream(lv2.getData()));
            } else {
                optional = Optional.empty();
            }
            ReadableContainer.Serialized<T> serialized = new ReadableContainer.Serialized<T>(lv.getElements(), optional);
            return serialized;
        } finally {
            this.unlock();
        }
    }

    private static <T> void applyEach(int[] is, IntUnaryOperator applier) {
        int i = -1;
        int j = -1;
        for (int k = 0; k < is.length; ++k) {
            int l = is[k];
            if (l != i) {
                i = l;
                j = applier.applyAsInt(l);
            }
            is[k] = j;
        }
    }

    @Override
    public int getPacketSize() {
        return this.data.getPacketSize();
    }

    @Override
    public boolean hasAny(Predicate<T> predicate) {
        return this.data.palette.hasAny(predicate);
    }

    public PalettedContainer<T> copy() {
        return new PalettedContainer<T>(this.idList, this.paletteProvider, this.data.copy());
    }

    @Override
    public PalettedContainer<T> slice() {
        return new PalettedContainer<T>(this.idList, this.data.palette.get(0), this.paletteProvider);
    }

    @Override
    public void count(Counter<T> counter) {
        if (this.data.palette.getSize() == 1) {
            counter.accept(this.data.palette.get(0), this.data.storage.getSize());
            return;
        }
        Int2IntOpenHashMap int2IntOpenHashMap = new Int2IntOpenHashMap();
        this.data.storage.forEach(key -> int2IntOpenHashMap.addTo(key, 1));
        int2IntOpenHashMap.int2IntEntrySet().forEach(entry -> counter.accept(this.data.palette.get(entry.getIntKey()), entry.getIntValue()));
    }

    public static abstract class PaletteProvider {
        public static final Palette.Factory SINGULAR = SingularPalette::create;
        public static final Palette.Factory ARRAY = ArrayPalette::create;
        public static final Palette.Factory BI_MAP = BiMapPalette::create;
        static final Palette.Factory ID_LIST = IdListPalette::create;
        public static final PaletteProvider BLOCK_STATE = new PaletteProvider(4){

            @Override
            public <A> DataProvider<A> createDataProvider(IndexedIterable<A> idList, int bits) {
                return switch (bits) {
                    case 0 -> new DataProvider(SINGULAR, bits);
                    case 1, 2, 3, 4 -> new DataProvider(ARRAY, 4);
                    case 5, 6, 7, 8 -> new DataProvider(BI_MAP, bits);
                    default -> new DataProvider(ID_LIST, MathHelper.ceilLog2(idList.size()));
                };
            }
        };
        public static final PaletteProvider BIOME = new PaletteProvider(2){

            @Override
            public <A> DataProvider<A> createDataProvider(IndexedIterable<A> idList, int bits) {
                return switch (bits) {
                    case 0 -> new DataProvider(SINGULAR, bits);
                    case 1, 2, 3 -> new DataProvider(ARRAY, bits);
                    default -> new DataProvider(ID_LIST, MathHelper.ceilLog2(idList.size()));
                };
            }
        };
        private final int edgeBits;

        PaletteProvider(int edgeBits) {
            this.edgeBits = edgeBits;
        }

        public int getContainerSize() {
            return 1 << this.edgeBits * 3;
        }

        public int computeIndex(int x, int y, int z) {
            return (y << this.edgeBits | z) << this.edgeBits | x;
        }

        public abstract <A> DataProvider<A> createDataProvider(IndexedIterable<A> var1, int var2);

        <A> int getBits(IndexedIterable<A> idList, int size) {
            int j = MathHelper.ceilLog2(size);
            DataProvider<A> lv = this.createDataProvider(idList, j);
            return lv.factory() == ID_LIST ? j : lv.bits();
        }
    }

    record Data<T>(DataProvider<T> configuration, PaletteStorage storage, Palette<T> palette) {
        public void importFrom(Palette<T> palette, PaletteStorage storage) {
            for (int i = 0; i < storage.getSize(); ++i) {
                T object = palette.get(storage.get(i));
                this.storage.set(i, this.palette.index(object));
            }
        }

        public int getPacketSize() {
            return 1 + this.palette.getPacketSize() + VarInts.getSizeInBytes(this.storage.getData().length) + this.storage.getData().length * 8;
        }

        public void writePacket(PacketByteBuf buf) {
            buf.writeByte(this.storage.getElementBits());
            this.palette.writePacket(buf);
            buf.writeLongArray(this.storage.getData());
        }

        public Data<T> copy() {
            return new Data<T>(this.configuration, this.storage.copy(), this.palette.copy());
        }
    }

    record DataProvider<T>(Palette.Factory factory, int bits) {
        public Data<T> createData(IndexedIterable<T> idList, PaletteResizeListener<T> listener, int size) {
            PaletteStorage lv = this.bits == 0 ? new EmptyPaletteStorage(size) : new PackedIntegerArray(this.bits, size);
            Palette<T> lv2 = this.factory.create(this.bits, idList, listener, List.of());
            return new Data<T>(this, lv, lv2);
        }
    }

    @FunctionalInterface
    public static interface Counter<T> {
        public void accept(T var1, int var2);
    }
}

