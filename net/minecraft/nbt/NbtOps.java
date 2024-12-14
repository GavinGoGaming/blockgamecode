/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 * 
 * Could not load the following classes:
 *  org.jetbrains.annotations.Nullable
 */
package net.minecraft.nbt;

import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.MapLike;
import com.mojang.serialization.RecordBuilder;
import it.unimi.dsi.fastutil.bytes.ByteArrayList;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.longs.LongArrayList;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.stream.IntStream;
import java.util.stream.LongStream;
import java.util.stream.Stream;
import net.minecraft.nbt.AbstractNbtList;
import net.minecraft.nbt.AbstractNbtNumber;
import net.minecraft.nbt.NbtByte;
import net.minecraft.nbt.NbtByteArray;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtDouble;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtEnd;
import net.minecraft.nbt.NbtFloat;
import net.minecraft.nbt.NbtInt;
import net.minecraft.nbt.NbtIntArray;
import net.minecraft.nbt.NbtList;
import net.minecraft.nbt.NbtLong;
import net.minecraft.nbt.NbtLongArray;
import net.minecraft.nbt.NbtShort;
import net.minecraft.nbt.NbtString;
import org.jetbrains.annotations.Nullable;

public class NbtOps
implements DynamicOps<NbtElement> {
    public static final NbtOps INSTANCE = new NbtOps();
    private static final String MARKER_KEY = "";

    protected NbtOps() {
    }

    @Override
    public NbtElement empty() {
        return NbtEnd.INSTANCE;
    }

    @Override
    public <U> U convertTo(DynamicOps<U> dynamicOps, NbtElement arg) {
        return switch (arg.getType()) {
            case 0 -> dynamicOps.empty();
            case 1 -> dynamicOps.createByte(((AbstractNbtNumber)arg).byteValue());
            case 2 -> dynamicOps.createShort(((AbstractNbtNumber)arg).shortValue());
            case 3 -> dynamicOps.createInt(((AbstractNbtNumber)arg).intValue());
            case 4 -> dynamicOps.createLong(((AbstractNbtNumber)arg).longValue());
            case 5 -> dynamicOps.createFloat(((AbstractNbtNumber)arg).floatValue());
            case 6 -> dynamicOps.createDouble(((AbstractNbtNumber)arg).doubleValue());
            case 7 -> dynamicOps.createByteList(ByteBuffer.wrap(((NbtByteArray)arg).getByteArray()));
            case 8 -> dynamicOps.createString(arg.asString());
            case 9 -> this.convertList(dynamicOps, arg);
            case 10 -> this.convertMap(dynamicOps, arg);
            case 11 -> dynamicOps.createIntList(Arrays.stream(((NbtIntArray)arg).getIntArray()));
            case 12 -> dynamicOps.createLongList(Arrays.stream(((NbtLongArray)arg).getLongArray()));
            default -> throw new IllegalStateException("Unknown tag type: " + String.valueOf(arg));
        };
    }

    @Override
    public DataResult<Number> getNumberValue(NbtElement arg) {
        if (arg instanceof AbstractNbtNumber) {
            AbstractNbtNumber lv = (AbstractNbtNumber)arg;
            return DataResult.success(lv.numberValue());
        }
        return DataResult.error(() -> "Not a number");
    }

    @Override
    public NbtElement createNumeric(Number number) {
        return NbtDouble.of(number.doubleValue());
    }

    @Override
    public NbtElement createByte(byte b) {
        return NbtByte.of(b);
    }

    @Override
    public NbtElement createShort(short s) {
        return NbtShort.of(s);
    }

    @Override
    public NbtElement createInt(int i) {
        return NbtInt.of(i);
    }

    @Override
    public NbtElement createLong(long l) {
        return NbtLong.of(l);
    }

    @Override
    public NbtElement createFloat(float f) {
        return NbtFloat.of(f);
    }

    @Override
    public NbtElement createDouble(double d) {
        return NbtDouble.of(d);
    }

    @Override
    public NbtElement createBoolean(boolean bl) {
        return NbtByte.of(bl);
    }

    @Override
    public DataResult<String> getStringValue(NbtElement arg) {
        if (arg instanceof NbtString) {
            NbtString lv = (NbtString)arg;
            return DataResult.success(lv.asString());
        }
        return DataResult.error(() -> "Not a string");
    }

    @Override
    public NbtElement createString(String string) {
        return NbtString.of(string);
    }

    @Override
    public DataResult<NbtElement> mergeToList(NbtElement arg, NbtElement arg2) {
        return NbtOps.createMerger(arg).map(merger -> DataResult.success(merger.merge(arg2).getResult())).orElseGet(() -> DataResult.error(() -> "mergeToList called with not a list: " + String.valueOf(arg), arg));
    }

    @Override
    public DataResult<NbtElement> mergeToList(NbtElement arg, List<NbtElement> list) {
        return NbtOps.createMerger(arg).map(merger -> DataResult.success(merger.merge(list).getResult())).orElseGet(() -> DataResult.error(() -> "mergeToList called with not a list: " + String.valueOf(arg), arg));
    }

    @Override
    public DataResult<NbtElement> mergeToMap(NbtElement arg, NbtElement arg2, NbtElement arg3) {
        NbtCompound nbtCompound;
        if (!(arg instanceof NbtCompound) && !(arg instanceof NbtEnd)) {
            return DataResult.error(() -> "mergeToMap called with not a map: " + String.valueOf(arg), arg);
        }
        if (!(arg2 instanceof NbtString)) {
            return DataResult.error(() -> "key is not a string: " + String.valueOf(arg2), arg);
        }
        if (arg instanceof NbtCompound) {
            NbtCompound lv = (NbtCompound)arg;
            nbtCompound = lv.shallowCopy();
        } else {
            nbtCompound = new NbtCompound();
        }
        NbtCompound lv2 = nbtCompound;
        lv2.put(arg2.asString(), arg3);
        return DataResult.success(lv2);
    }

    @Override
    public DataResult<NbtElement> mergeToMap(NbtElement arg, MapLike<NbtElement> mapLike) {
        NbtCompound nbtCompound;
        if (!(arg instanceof NbtCompound) && !(arg instanceof NbtEnd)) {
            return DataResult.error(() -> "mergeToMap called with not a map: " + String.valueOf(arg), arg);
        }
        if (arg instanceof NbtCompound) {
            NbtCompound lv = (NbtCompound)arg;
            nbtCompound = lv.shallowCopy();
        } else {
            nbtCompound = new NbtCompound();
        }
        NbtCompound lv2 = nbtCompound;
        ArrayList list = new ArrayList();
        mapLike.entries().forEach(pair -> {
            NbtElement lv = (NbtElement)pair.getFirst();
            if (!(lv instanceof NbtString)) {
                list.add(lv);
                return;
            }
            lv2.put(lv.asString(), (NbtElement)pair.getSecond());
        });
        if (!list.isEmpty()) {
            return DataResult.error(() -> "some keys are not strings: " + String.valueOf(list), lv2);
        }
        return DataResult.success(lv2);
    }

    @Override
    public DataResult<NbtElement> mergeToMap(NbtElement arg, Map<NbtElement, NbtElement> map) {
        NbtCompound nbtCompound;
        if (!(arg instanceof NbtCompound) && !(arg instanceof NbtEnd)) {
            return DataResult.error(() -> "mergeToMap called with not a map: " + String.valueOf(arg), arg);
        }
        if (arg instanceof NbtCompound) {
            NbtCompound lv = (NbtCompound)arg;
            nbtCompound = lv.shallowCopy();
        } else {
            nbtCompound = new NbtCompound();
        }
        NbtCompound lv2 = nbtCompound;
        ArrayList<NbtElement> list = new ArrayList<NbtElement>();
        for (Map.Entry<NbtElement, NbtElement> entry : map.entrySet()) {
            NbtElement lv3 = entry.getKey();
            if (lv3 instanceof NbtString) {
                lv2.put(lv3.asString(), entry.getValue());
                continue;
            }
            list.add(lv3);
        }
        if (!list.isEmpty()) {
            return DataResult.error(() -> "some keys are not strings: " + String.valueOf(list), lv2);
        }
        return DataResult.success(lv2);
    }

    @Override
    public DataResult<Stream<Pair<NbtElement, NbtElement>>> getMapValues(NbtElement arg) {
        if (arg instanceof NbtCompound) {
            NbtCompound lv = (NbtCompound)arg;
            return DataResult.success(lv.entrySet().stream().map(entry -> Pair.of(this.createString((String)entry.getKey()), (NbtElement)entry.getValue())));
        }
        return DataResult.error(() -> "Not a map: " + String.valueOf(arg));
    }

    @Override
    public DataResult<Consumer<BiConsumer<NbtElement, NbtElement>>> getMapEntries(NbtElement arg) {
        if (arg instanceof NbtCompound) {
            NbtCompound lv = (NbtCompound)arg;
            return DataResult.success(biConsumer -> {
                for (Map.Entry<String, NbtElement> entry : lv.entrySet()) {
                    biConsumer.accept(this.createString(entry.getKey()), entry.getValue());
                }
            });
        }
        return DataResult.error(() -> "Not a map: " + String.valueOf(arg));
    }

    @Override
    public DataResult<MapLike<NbtElement>> getMap(NbtElement arg) {
        if (arg instanceof NbtCompound) {
            final NbtCompound lv = (NbtCompound)arg;
            return DataResult.success(new MapLike<NbtElement>(){

                @Override
                @Nullable
                public NbtElement get(NbtElement arg) {
                    return lv.get(arg.asString());
                }

                @Override
                @Nullable
                public NbtElement get(String string) {
                    return lv.get(string);
                }

                @Override
                public Stream<Pair<NbtElement, NbtElement>> entries() {
                    return lv.entrySet().stream().map(entry -> Pair.of(NbtOps.this.createString((String)entry.getKey()), (NbtElement)entry.getValue()));
                }

                public String toString() {
                    return "MapLike[" + String.valueOf(lv) + "]";
                }

                @Override
                @Nullable
                public /* synthetic */ Object get(String key) {
                    return this.get(key);
                }

                @Override
                @Nullable
                public /* synthetic */ Object get(Object nbt) {
                    return this.get((NbtElement)nbt);
                }
            });
        }
        return DataResult.error(() -> "Not a map: " + String.valueOf(arg));
    }

    @Override
    public NbtElement createMap(Stream<Pair<NbtElement, NbtElement>> stream) {
        NbtCompound lv = new NbtCompound();
        stream.forEach(entry -> lv.put(((NbtElement)entry.getFirst()).asString(), (NbtElement)entry.getSecond()));
        return lv;
    }

    private static NbtElement unpackMarker(NbtCompound nbt) {
        NbtElement lv;
        if (nbt.getSize() == 1 && (lv = nbt.get(MARKER_KEY)) != null) {
            return lv;
        }
        return nbt;
    }

    @Override
    public DataResult<Stream<NbtElement>> getStream(NbtElement arg) {
        if (arg instanceof NbtList) {
            NbtList lv = (NbtList)arg;
            if (lv.getHeldType() == NbtElement.COMPOUND_TYPE) {
                return DataResult.success(lv.stream().map(nbt -> NbtOps.unpackMarker((NbtCompound)nbt)));
            }
            return DataResult.success(lv.stream());
        }
        if (arg instanceof AbstractNbtList) {
            AbstractNbtList lv2 = (AbstractNbtList)arg;
            return DataResult.success(lv2.stream().map(nbt -> nbt));
        }
        return DataResult.error(() -> "Not a list");
    }

    @Override
    public DataResult<Consumer<Consumer<NbtElement>>> getList(NbtElement arg) {
        if (arg instanceof NbtList) {
            NbtList lv = (NbtList)arg;
            if (lv.getHeldType() == NbtElement.COMPOUND_TYPE) {
                return DataResult.success(consumer -> {
                    for (NbtElement lv : lv) {
                        consumer.accept(NbtOps.unpackMarker((NbtCompound)lv));
                    }
                });
            }
            return DataResult.success(lv::forEach);
        }
        if (arg instanceof AbstractNbtList) {
            AbstractNbtList lv2 = (AbstractNbtList)arg;
            return DataResult.success(lv2::forEach);
        }
        return DataResult.error(() -> "Not a list: " + String.valueOf(arg));
    }

    @Override
    public DataResult<ByteBuffer> getByteBuffer(NbtElement arg) {
        if (arg instanceof NbtByteArray) {
            NbtByteArray lv = (NbtByteArray)arg;
            return DataResult.success(ByteBuffer.wrap(lv.getByteArray()));
        }
        return DynamicOps.super.getByteBuffer(arg);
    }

    @Override
    public NbtElement createByteList(ByteBuffer byteBuffer) {
        ByteBuffer byteBuffer2 = byteBuffer.duplicate().clear();
        byte[] bs = new byte[byteBuffer.capacity()];
        byteBuffer2.get(0, bs, 0, bs.length);
        return new NbtByteArray(bs);
    }

    @Override
    public DataResult<IntStream> getIntStream(NbtElement arg) {
        if (arg instanceof NbtIntArray) {
            NbtIntArray lv = (NbtIntArray)arg;
            return DataResult.success(Arrays.stream(lv.getIntArray()));
        }
        return DynamicOps.super.getIntStream(arg);
    }

    @Override
    public NbtElement createIntList(IntStream intStream) {
        return new NbtIntArray(intStream.toArray());
    }

    @Override
    public DataResult<LongStream> getLongStream(NbtElement arg) {
        if (arg instanceof NbtLongArray) {
            NbtLongArray lv = (NbtLongArray)arg;
            return DataResult.success(Arrays.stream(lv.getLongArray()));
        }
        return DynamicOps.super.getLongStream(arg);
    }

    @Override
    public NbtElement createLongList(LongStream longStream) {
        return new NbtLongArray(longStream.toArray());
    }

    @Override
    public NbtElement createList(Stream<NbtElement> stream) {
        return BasicMerger.EMPTY.merge(stream).getResult();
    }

    @Override
    public NbtElement remove(NbtElement arg, String string) {
        if (arg instanceof NbtCompound) {
            NbtCompound lv = (NbtCompound)arg;
            NbtCompound lv2 = lv.shallowCopy();
            lv2.remove(string);
            return lv2;
        }
        return arg;
    }

    public String toString() {
        return "NBT";
    }

    @Override
    public RecordBuilder<NbtElement> mapBuilder() {
        return new MapBuilder(this);
    }

    private static Optional<Merger> createMerger(NbtElement nbt) {
        if (nbt instanceof NbtEnd) {
            return Optional.of(BasicMerger.EMPTY);
        }
        if (nbt instanceof AbstractNbtList) {
            AbstractNbtList lv = (AbstractNbtList)nbt;
            if (lv.isEmpty()) {
                return Optional.of(BasicMerger.EMPTY);
            }
            if (lv instanceof NbtList) {
                NbtList lv2 = (NbtList)lv;
                return switch (lv2.getHeldType()) {
                    case 0 -> Optional.of(BasicMerger.EMPTY);
                    case 10 -> Optional.of(new CompoundListMerger(lv2));
                    default -> Optional.of(new ListMerger(lv2));
                };
            }
            if (lv instanceof NbtByteArray) {
                NbtByteArray lv3 = (NbtByteArray)lv;
                return Optional.of(new ByteArrayMerger(lv3.getByteArray()));
            }
            if (lv instanceof NbtIntArray) {
                NbtIntArray lv4 = (NbtIntArray)lv;
                return Optional.of(new IntArrayMerger(lv4.getIntArray()));
            }
            if (lv instanceof NbtLongArray) {
                NbtLongArray lv5 = (NbtLongArray)lv;
                return Optional.of(new LongArrayMerger(lv5.getLongArray()));
            }
        }
        return Optional.empty();
    }

    @Override
    public /* synthetic */ Object remove(Object element, String key) {
        return this.remove((NbtElement)element, key);
    }

    @Override
    public /* synthetic */ Object createLongList(LongStream stream) {
        return this.createLongList(stream);
    }

    @Override
    public /* synthetic */ DataResult getLongStream(Object element) {
        return this.getLongStream((NbtElement)element);
    }

    @Override
    public /* synthetic */ Object createIntList(IntStream stream) {
        return this.createIntList(stream);
    }

    @Override
    public /* synthetic */ DataResult getIntStream(Object element) {
        return this.getIntStream((NbtElement)element);
    }

    @Override
    public /* synthetic */ Object createByteList(ByteBuffer buf) {
        return this.createByteList(buf);
    }

    @Override
    public /* synthetic */ DataResult getByteBuffer(Object element) {
        return this.getByteBuffer((NbtElement)element);
    }

    @Override
    public /* synthetic */ Object createList(Stream stream) {
        return this.createList(stream);
    }

    @Override
    public /* synthetic */ DataResult getList(Object element) {
        return this.getList((NbtElement)element);
    }

    @Override
    public /* synthetic */ DataResult getStream(Object element) {
        return this.getStream((NbtElement)element);
    }

    @Override
    public /* synthetic */ DataResult getMap(Object element) {
        return this.getMap((NbtElement)element);
    }

    @Override
    public /* synthetic */ Object createMap(Stream entries) {
        return this.createMap(entries);
    }

    @Override
    public /* synthetic */ DataResult getMapEntries(Object element) {
        return this.getMapEntries((NbtElement)element);
    }

    @Override
    public /* synthetic */ DataResult getMapValues(Object element) {
        return this.getMapValues((NbtElement)element);
    }

    @Override
    public /* synthetic */ DataResult mergeToMap(Object element, MapLike map) {
        return this.mergeToMap((NbtElement)element, (MapLike<NbtElement>)map);
    }

    @Override
    public /* synthetic */ DataResult mergeToMap(Object nbt, Map map) {
        return this.mergeToMap((NbtElement)nbt, (Map<NbtElement, NbtElement>)map);
    }

    @Override
    public /* synthetic */ DataResult mergeToMap(Object map, Object key, Object value) {
        return this.mergeToMap((NbtElement)map, (NbtElement)key, (NbtElement)value);
    }

    @Override
    public /* synthetic */ DataResult mergeToList(Object list, List values) {
        return this.mergeToList((NbtElement)list, (List<NbtElement>)values);
    }

    @Override
    public /* synthetic */ DataResult mergeToList(Object list, Object value) {
        return this.mergeToList((NbtElement)list, (NbtElement)value);
    }

    @Override
    public /* synthetic */ Object createString(String string) {
        return this.createString(string);
    }

    @Override
    public /* synthetic */ DataResult getStringValue(Object element) {
        return this.getStringValue((NbtElement)element);
    }

    @Override
    public /* synthetic */ Object createBoolean(boolean value) {
        return this.createBoolean(value);
    }

    @Override
    public /* synthetic */ Object createDouble(double value) {
        return this.createDouble(value);
    }

    @Override
    public /* synthetic */ Object createFloat(float value) {
        return this.createFloat(value);
    }

    @Override
    public /* synthetic */ Object createLong(long value) {
        return this.createLong(value);
    }

    @Override
    public /* synthetic */ Object createInt(int value) {
        return this.createInt(value);
    }

    @Override
    public /* synthetic */ Object createShort(short value) {
        return this.createShort(value);
    }

    @Override
    public /* synthetic */ Object createByte(byte value) {
        return this.createByte(value);
    }

    @Override
    public /* synthetic */ Object createNumeric(Number value) {
        return this.createNumeric(value);
    }

    @Override
    public /* synthetic */ DataResult getNumberValue(Object element) {
        return this.getNumberValue((NbtElement)element);
    }

    @Override
    public /* synthetic */ Object convertTo(DynamicOps ops, Object element) {
        return this.convertTo(ops, (NbtElement)element);
    }

    @Override
    public /* synthetic */ Object empty() {
        return this.empty();
    }

    static class BasicMerger
    implements Merger {
        public static final BasicMerger EMPTY = new BasicMerger();

        private BasicMerger() {
        }

        @Override
        public Merger merge(NbtElement nbt) {
            if (nbt instanceof NbtCompound) {
                NbtCompound lv = (NbtCompound)nbt;
                return new CompoundListMerger().merge(lv);
            }
            if (nbt instanceof NbtByte) {
                NbtByte lv2 = (NbtByte)nbt;
                return new ByteArrayMerger(lv2.byteValue());
            }
            if (nbt instanceof NbtInt) {
                NbtInt lv3 = (NbtInt)nbt;
                return new IntArrayMerger(lv3.intValue());
            }
            if (nbt instanceof NbtLong) {
                NbtLong lv4 = (NbtLong)nbt;
                return new LongArrayMerger(lv4.longValue());
            }
            return new ListMerger(nbt);
        }

        @Override
        public NbtElement getResult() {
            return new NbtList();
        }
    }

    static interface Merger {
        public Merger merge(NbtElement var1);

        default public Merger merge(Iterable<NbtElement> nbts) {
            Merger lv = this;
            for (NbtElement lv2 : nbts) {
                lv = lv.merge(lv2);
            }
            return lv;
        }

        default public Merger merge(Stream<NbtElement> nbts) {
            return this.merge(nbts::iterator);
        }

        public NbtElement getResult();
    }

    class MapBuilder
    extends RecordBuilder.AbstractStringBuilder<NbtElement, NbtCompound> {
        protected MapBuilder(NbtOps ops) {
            super(ops);
        }

        @Override
        protected NbtCompound initBuilder() {
            return new NbtCompound();
        }

        @Override
        protected NbtCompound append(String string, NbtElement arg, NbtCompound arg2) {
            arg2.put(string, arg);
            return arg2;
        }

        @Override
        protected DataResult<NbtElement> build(NbtCompound arg, NbtElement arg2) {
            if (arg2 == null || arg2 == NbtEnd.INSTANCE) {
                return DataResult.success(arg);
            }
            if (arg2 instanceof NbtCompound) {
                NbtCompound lv = (NbtCompound)arg2;
                NbtCompound lv2 = lv.shallowCopy();
                for (Map.Entry<String, NbtElement> entry : arg.entrySet()) {
                    lv2.put(entry.getKey(), entry.getValue());
                }
                return DataResult.success(lv2);
            }
            return DataResult.error(() -> "mergeToMap called with not a map: " + String.valueOf(arg2), arg2);
        }

        @Override
        protected /* synthetic */ Object append(String key, Object value, Object nbt) {
            return this.append(key, (NbtElement)value, (NbtCompound)nbt);
        }

        @Override
        protected /* synthetic */ DataResult build(Object nbt, Object mergedValue) {
            return this.build((NbtCompound)nbt, (NbtElement)mergedValue);
        }

        @Override
        protected /* synthetic */ Object initBuilder() {
            return this.initBuilder();
        }
    }

    static class CompoundListMerger
    implements Merger {
        private final NbtList list = new NbtList();

        public CompoundListMerger() {
        }

        public CompoundListMerger(Collection<NbtElement> nbts) {
            this.list.addAll(nbts);
        }

        public CompoundListMerger(IntArrayList list) {
            list.forEach(value -> this.list.add(CompoundListMerger.createMarkerNbt(NbtInt.of(value))));
        }

        public CompoundListMerger(ByteArrayList list) {
            list.forEach(value -> this.list.add(CompoundListMerger.createMarkerNbt(NbtByte.of(value))));
        }

        public CompoundListMerger(LongArrayList list) {
            list.forEach(value -> this.list.add(CompoundListMerger.createMarkerNbt(NbtLong.of(value))));
        }

        private static boolean isMarker(NbtCompound nbt) {
            return nbt.getSize() == 1 && nbt.contains(NbtOps.MARKER_KEY);
        }

        private static NbtElement makeMarker(NbtElement value) {
            NbtCompound lv;
            if (value instanceof NbtCompound && !CompoundListMerger.isMarker(lv = (NbtCompound)value)) {
                return lv;
            }
            return CompoundListMerger.createMarkerNbt(value);
        }

        private static NbtCompound createMarkerNbt(NbtElement value) {
            NbtCompound lv = new NbtCompound();
            lv.put(NbtOps.MARKER_KEY, value);
            return lv;
        }

        @Override
        public Merger merge(NbtElement nbt) {
            this.list.add(CompoundListMerger.makeMarker(nbt));
            return this;
        }

        @Override
        public NbtElement getResult() {
            return this.list;
        }
    }

    static class ListMerger
    implements Merger {
        private final NbtList list = new NbtList();

        ListMerger(NbtElement nbt) {
            this.list.add(nbt);
        }

        ListMerger(NbtList nbt) {
            this.list.addAll(nbt);
        }

        @Override
        public Merger merge(NbtElement nbt) {
            if (nbt.getType() != this.list.getHeldType()) {
                return new CompoundListMerger().merge(this.list).merge(nbt);
            }
            this.list.add(nbt);
            return this;
        }

        @Override
        public NbtElement getResult() {
            return this.list;
        }
    }

    static class ByteArrayMerger
    implements Merger {
        private final ByteArrayList list = new ByteArrayList();

        public ByteArrayMerger(byte value) {
            this.list.add(value);
        }

        public ByteArrayMerger(byte[] values) {
            this.list.addElements(0, values);
        }

        @Override
        public Merger merge(NbtElement nbt) {
            if (nbt instanceof NbtByte) {
                NbtByte lv = (NbtByte)nbt;
                this.list.add(lv.byteValue());
                return this;
            }
            return new CompoundListMerger(this.list).merge(nbt);
        }

        @Override
        public NbtElement getResult() {
            return new NbtByteArray(this.list.toByteArray());
        }
    }

    static class IntArrayMerger
    implements Merger {
        private final IntArrayList list = new IntArrayList();

        public IntArrayMerger(int value) {
            this.list.add(value);
        }

        public IntArrayMerger(int[] values) {
            this.list.addElements(0, values);
        }

        @Override
        public Merger merge(NbtElement nbt) {
            if (nbt instanceof NbtInt) {
                NbtInt lv = (NbtInt)nbt;
                this.list.add(lv.intValue());
                return this;
            }
            return new CompoundListMerger(this.list).merge(nbt);
        }

        @Override
        public NbtElement getResult() {
            return new NbtIntArray(this.list.toIntArray());
        }
    }

    static class LongArrayMerger
    implements Merger {
        private final LongArrayList list = new LongArrayList();

        public LongArrayMerger(long value) {
            this.list.add(value);
        }

        public LongArrayMerger(long[] values) {
            this.list.addElements(0, values);
        }

        @Override
        public Merger merge(NbtElement nbt) {
            if (nbt instanceof NbtLong) {
                NbtLong lv = (NbtLong)nbt;
                this.list.add(lv.longValue());
                return this;
            }
            return new CompoundListMerger(this.list).merge(nbt);
        }

        @Override
        public NbtElement getResult() {
            return new NbtLongArray(this.list.toLongArray());
        }
    }
}

