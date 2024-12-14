/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.util.collection;

import com.google.common.collect.ImmutableCollection;
import com.google.common.collect.ImmutableList;
import com.mojang.datafixers.kinds.Applicative;
import com.mojang.logging.LogUtils;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;
import net.minecraft.util.StringIdentifiable;
import net.minecraft.util.dynamic.Codecs;
import org.slf4j.Logger;

public interface ListOperation {
    public static final MapCodec<ListOperation> UNLIMITED_SIZE_CODEC = ListOperation.createCodec(Integer.MAX_VALUE);

    public static MapCodec<ListOperation> createCodec(int maxSize) {
        return Mode.CODEC.dispatchMap("mode", ListOperation::getMode, mode -> mode.codec).validate(operation -> {
            int j;
            ReplaceSection lv;
            if (operation instanceof ReplaceSection && (lv = (ReplaceSection)operation).size().isPresent() && (j = lv.size().get().intValue()) > maxSize) {
                return DataResult.error(() -> "Size value too large: " + j + ", max size is " + maxSize);
            }
            return DataResult.success(operation);
        });
    }

    public Mode getMode();

    default public <T> List<T> apply(List<T> current, List<T> values) {
        return this.apply(current, values, Integer.MAX_VALUE);
    }

    public <T> List<T> apply(List<T> var1, List<T> var2, int var3);

    public static enum Mode implements StringIdentifiable
    {
        REPLACE_ALL("replace_all", ReplaceAll.CODEC),
        REPLACE_SECTION("replace_section", ReplaceSection.CODEC),
        INSERT("insert", Insert.CODEC),
        APPEND("append", Append.CODEC);

        public static final Codec<Mode> CODEC;
        private final String id;
        final MapCodec<? extends ListOperation> codec;

        private Mode(String id, MapCodec<? extends ListOperation> codec) {
            this.id = id;
            this.codec = codec;
        }

        public MapCodec<? extends ListOperation> getCodec() {
            return this.codec;
        }

        @Override
        public String asString() {
            return this.id;
        }

        static {
            CODEC = StringIdentifiable.createCodec(Mode::values);
        }
    }

    public record ReplaceSection(int offset, Optional<Integer> size) implements ListOperation
    {
        private static final Logger LOGGER = LogUtils.getLogger();
        public static final MapCodec<ReplaceSection> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(Codecs.NONNEGATIVE_INT.optionalFieldOf("offset", 0).forGetter(ReplaceSection::offset), Codecs.NONNEGATIVE_INT.optionalFieldOf("size").forGetter(ReplaceSection::size)).apply((Applicative<ReplaceSection, ?>)instance, ReplaceSection::new));

        public ReplaceSection(int offset) {
            this(offset, Optional.empty());
        }

        @Override
        public Mode getMode() {
            return Mode.REPLACE_SECTION;
        }

        @Override
        public <T> List<T> apply(List<T> current, List<T> values, int maxSize) {
            ImmutableCollection list3;
            int j = current.size();
            if (this.offset > j) {
                LOGGER.error("Cannot replace when offset is out of bounds");
                return current;
            }
            ImmutableList.Builder builder = ImmutableList.builder();
            builder.addAll(current.subList(0, this.offset));
            builder.addAll(values);
            int k = this.offset + this.size.orElse(values.size());
            if (k < j) {
                builder.addAll(current.subList(k, j));
            }
            if ((list3 = builder.build()).size() > maxSize) {
                LOGGER.error("Contents overflow in section replacement");
                return current;
            }
            return list3;
        }
    }

    public record Values<T>(List<T> value, ListOperation operation) {
        public static <T> Codec<Values<T>> createCodec(Codec<T> codec, int maxSize) {
            return RecordCodecBuilder.create(instance -> instance.group(((MapCodec)codec.sizeLimitedListOf(maxSize).fieldOf("values")).forGetter(values -> values.value), ListOperation.createCodec(maxSize).forGetter(values -> values.operation)).apply((Applicative<Values, ?>)instance, Values::new));
        }

        public List<T> apply(List<T> current) {
            return this.operation.apply(current, this.value);
        }
    }

    public static class Append
    implements ListOperation {
        private static final Logger LOGGER = LogUtils.getLogger();
        public static final Append INSTANCE = new Append();
        public static final MapCodec<Append> CODEC = MapCodec.unit(() -> INSTANCE);

        private Append() {
        }

        @Override
        public Mode getMode() {
            return Mode.APPEND;
        }

        @Override
        public <T> List<T> apply(List<T> current, List<T> values, int maxSize) {
            if (current.size() + values.size() > maxSize) {
                LOGGER.error("Contents overflow in section append");
                return current;
            }
            return Stream.concat(current.stream(), values.stream()).toList();
        }
    }

    public record Insert(int offset) implements ListOperation
    {
        private static final Logger LOGGER = LogUtils.getLogger();
        public static final MapCodec<Insert> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(Codecs.NONNEGATIVE_INT.optionalFieldOf("offset", 0).forGetter(Insert::offset)).apply((Applicative<Insert, ?>)instance, Insert::new));

        @Override
        public Mode getMode() {
            return Mode.INSERT;
        }

        @Override
        public <T> List<T> apply(List<T> current, List<T> values, int maxSize) {
            int j = current.size();
            if (this.offset > j) {
                LOGGER.error("Cannot insert when offset is out of bounds");
                return current;
            }
            if (j + values.size() > maxSize) {
                LOGGER.error("Contents overflow in section insertion");
                return current;
            }
            ImmutableList.Builder builder = ImmutableList.builder();
            builder.addAll(current.subList(0, this.offset));
            builder.addAll(values);
            builder.addAll(current.subList(this.offset, j));
            return builder.build();
        }
    }

    public static class ReplaceAll
    implements ListOperation {
        public static final ReplaceAll INSTANCE = new ReplaceAll();
        public static final MapCodec<ReplaceAll> CODEC = MapCodec.unit(() -> INSTANCE);

        private ReplaceAll() {
        }

        @Override
        public Mode getMode() {
            return Mode.REPLACE_ALL;
        }

        @Override
        public <T> List<T> apply(List<T> current, List<T> values, int maxSize) {
            return values;
        }
    }
}

