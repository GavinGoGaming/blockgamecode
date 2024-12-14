/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 * 
 * Could not load the following classes:
 *  org.jetbrains.annotations.Nullable
 */
package net.minecraft.util;

import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.Keyable;
import java.util.Arrays;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.function.ToIntFunction;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import net.minecraft.util.Util;
import net.minecraft.util.dynamic.Codecs;
import org.jetbrains.annotations.Nullable;

public interface StringIdentifiable {
    public static final int CACHED_MAP_THRESHOLD = 16;

    public String asString();

    public static <E extends Enum<E>> EnumCodec<E> createCodec(Supplier<E[]> enumValues) {
        return StringIdentifiable.createCodec(enumValues, id -> id);
    }

    public static <E extends Enum<E>> EnumCodec<E> createCodec(Supplier<E[]> enumValues, Function<String, String> valueNameTransformer) {
        Enum[] enums = (Enum[])enumValues.get();
        Function function2 = StringIdentifiable.createMapper((StringIdentifiable[])enums, valueNameTransformer);
        return new EnumCodec(enums, function2);
    }

    public static <T extends StringIdentifiable> Codec<T> createBasicCodec(Supplier<T[]> values) {
        StringIdentifiable[] lvs = (StringIdentifiable[])values.get();
        Function function = StringIdentifiable.createMapper((StringIdentifiable[])lvs, (T valueName) -> valueName);
        ToIntFunction<StringIdentifiable> toIntFunction = Util.lastIndexGetter(Arrays.asList(lvs));
        return new BasicCodec(lvs, function, toIntFunction);
    }

    public static <T extends StringIdentifiable> Function<String, T> createMapper(T[] values, Function<String, String> valueNameTransformer) {
        if (values.length > 16) {
            Map<String, StringIdentifiable> map = Arrays.stream(values).collect(Collectors.toMap(value -> (String)valueNameTransformer.apply(value.asString()), value -> value));
            return name -> name == null ? null : (StringIdentifiable)map.get(name);
        }
        return name -> {
            for (StringIdentifiable lv : values) {
                if (!((String)valueNameTransformer.apply(lv.asString())).equals(name)) continue;
                return lv;
            }
            return null;
        };
    }

    public static Keyable toKeyable(final StringIdentifiable[] values) {
        return new Keyable(){

            @Override
            public <T> Stream<T> keys(DynamicOps<T> ops) {
                return Arrays.stream(values).map(StringIdentifiable::asString).map(ops::createString);
            }
        };
    }

    @Deprecated
    public static class EnumCodec<E extends Enum<E>>
    extends BasicCodec<E> {
        private final Function<String, E> idToIdentifiable;

        public EnumCodec(E[] values, Function<String, E> idToIdentifiable) {
            super(values, idToIdentifiable, enum_ -> ((Enum)enum_).ordinal());
            this.idToIdentifiable = idToIdentifiable;
        }

        @Nullable
        public E byId(@Nullable String id) {
            return (E)((Enum)this.idToIdentifiable.apply(id));
        }

        public E byId(@Nullable String id, E fallback) {
            return (E)((Enum)Objects.requireNonNullElse(this.byId(id), fallback));
        }
    }

    public static class BasicCodec<S extends StringIdentifiable>
    implements Codec<S> {
        private final Codec<S> codec;

        public BasicCodec(S[] values, Function<String, S> idToIdentifiable, ToIntFunction<S> identifiableToOrdinal) {
            this.codec = Codecs.orCompressed(Codec.stringResolver(StringIdentifiable::asString, idToIdentifiable), Codecs.rawIdChecked(identifiableToOrdinal, ordinal -> ordinal >= 0 && ordinal < values.length ? values[ordinal] : null, -1));
        }

        @Override
        public <T> DataResult<Pair<S, T>> decode(DynamicOps<T> ops, T input) {
            return this.codec.decode(ops, input);
        }

        @Override
        public <T> DataResult<T> encode(S arg, DynamicOps<T> dynamicOps, T object) {
            return this.codec.encode(arg, dynamicOps, object);
        }

        @Override
        public /* synthetic */ DataResult encode(Object input, DynamicOps ops, Object prefix) {
            return this.encode((S)((StringIdentifiable)input), (DynamicOps<T>)ops, (T)prefix);
        }
    }
}

