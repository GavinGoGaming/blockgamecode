/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 * 
 * Could not load the following classes:
 *  net.fabricmc.api.EnvType
 *  net.fabricmc.api.Environment
 *  org.jetbrains.annotations.Nullable
 */
package net.minecraft.client.session.telemetry;

import com.mojang.serialization.DataResult;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.MapLike;
import com.mojang.serialization.RecordBuilder;
import it.unimi.dsi.fastutil.objects.Reference2ObjectOpenHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Stream;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.session.telemetry.TelemetryEventProperty;
import org.jetbrains.annotations.Nullable;

@Environment(value=EnvType.CLIENT)
public class PropertyMap {
    final Map<TelemetryEventProperty<?>, Object> backingMap;

    PropertyMap(Map<TelemetryEventProperty<?>, Object> backingMap) {
        this.backingMap = backingMap;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static MapCodec<PropertyMap> createCodec(final List<TelemetryEventProperty<?>> properties) {
        return new MapCodec<PropertyMap>(){

            @Override
            public <T> RecordBuilder<T> encode(PropertyMap arg, DynamicOps<T> dynamicOps, RecordBuilder<T> recordBuilder) {
                RecordBuilder<T> recordBuilder2 = recordBuilder;
                for (TelemetryEventProperty lv : properties) {
                    recordBuilder2 = this.encode(arg, recordBuilder2, lv);
                }
                return recordBuilder2;
            }

            @Override
            private <T, V> RecordBuilder<T> encode(PropertyMap map, RecordBuilder<T> builder, TelemetryEventProperty<V> property) {
                V object = map.get(property);
                if (object != null) {
                    return builder.add(property.id(), object, property.codec());
                }
                return builder;
            }

            @Override
            public <T> DataResult<PropertyMap> decode(DynamicOps<T> ops, MapLike<T> map) {
                DataResult<Builder> dataResult = DataResult.success(new Builder());
                for (TelemetryEventProperty lv : properties) {
                    dataResult = this.decode(dataResult, ops, map, lv);
                }
                return dataResult.map(Builder::build);
            }

            private <T, V> DataResult<Builder> decode(DataResult<Builder> result, DynamicOps<T> ops, MapLike<T> map, TelemetryEventProperty<V> property) {
                T object = map.get(property.id());
                if (object != null) {
                    DataResult dataResult2 = property.codec().parse(ops, object);
                    return result.apply2stable((mapBuilder, value) -> mapBuilder.put(property, value), dataResult2);
                }
                return result;
            }

            @Override
            public <T> Stream<T> keys(DynamicOps<T> ops) {
                return properties.stream().map(TelemetryEventProperty::id).map(ops::createString);
            }

            @Override
            public /* synthetic */ RecordBuilder encode(Object map, DynamicOps ops, RecordBuilder builder) {
                return this.encode((PropertyMap)map, ops, builder);
            }
        };
    }

    @Nullable
    public <T> T get(TelemetryEventProperty<T> property) {
        return (T)this.backingMap.get(property);
    }

    public String toString() {
        return this.backingMap.toString();
    }

    public Set<TelemetryEventProperty<?>> keySet() {
        return this.backingMap.keySet();
    }

    @Environment(value=EnvType.CLIENT)
    public static class Builder {
        private final Map<TelemetryEventProperty<?>, Object> backingMap = new Reference2ObjectOpenHashMap();

        Builder() {
        }

        public <T> Builder put(TelemetryEventProperty<T> property, T value) {
            this.backingMap.put(property, value);
            return this;
        }

        public <T> Builder putIfNonNull(TelemetryEventProperty<T> property, @Nullable T value) {
            if (value != null) {
                this.backingMap.put(property, value);
            }
            return this;
        }

        public Builder putAll(PropertyMap map) {
            this.backingMap.putAll(map.backingMap);
            return this;
        }

        public PropertyMap build() {
            return new PropertyMap(this.backingMap);
        }
    }
}

