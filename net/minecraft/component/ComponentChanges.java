/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 * 
 * Could not load the following classes:
 *  org.jetbrains.annotations.Nullable
 */
package net.minecraft.component;

import com.google.common.collect.Sets;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import it.unimi.dsi.fastutil.objects.Reference2ObjectArrayMap;
import it.unimi.dsi.fastutil.objects.Reference2ObjectMap;
import it.unimi.dsi.fastutil.objects.Reference2ObjectMaps;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Predicate;
import net.minecraft.component.Component;
import net.minecraft.component.ComponentMap;
import net.minecraft.component.ComponentType;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.registry.Registries;
import net.minecraft.util.Identifier;
import net.minecraft.util.Unit;
import org.jetbrains.annotations.Nullable;

public final class ComponentChanges {
    public static final ComponentChanges EMPTY = new ComponentChanges(Reference2ObjectMaps.emptyMap());
    public static final Codec<ComponentChanges> CODEC = Codec.dispatchedMap(Type.CODEC, Type::getValueCodec).xmap(changes -> {
        if (changes.isEmpty()) {
            return EMPTY;
        }
        Reference2ObjectArrayMap reference2ObjectMap = new Reference2ObjectArrayMap(changes.size());
        for (Map.Entry entry : changes.entrySet()) {
            Type lv = (Type)entry.getKey();
            if (lv.removed()) {
                reference2ObjectMap.put(lv.type(), Optional.empty());
                continue;
            }
            reference2ObjectMap.put(lv.type(), Optional.of(entry.getValue()));
        }
        return new ComponentChanges(reference2ObjectMap);
    }, changes -> {
        Reference2ObjectArrayMap<Type, Object> reference2ObjectMap = new Reference2ObjectArrayMap<Type, Object>(changes.changedComponents.size());
        for (Map.Entry entry : Reference2ObjectMaps.fastIterable(changes.changedComponents)) {
            ComponentType lv = (ComponentType)entry.getKey();
            if (lv.shouldSkipSerialization()) continue;
            Optional optional = (Optional)entry.getValue();
            if (optional.isPresent()) {
                reference2ObjectMap.put(new Type(lv, false), optional.get());
                continue;
            }
            reference2ObjectMap.put(new Type(lv, true), (Object)Unit.INSTANCE);
        }
        return reference2ObjectMap;
    });
    public static final PacketCodec<RegistryByteBuf, ComponentChanges> PACKET_CODEC = new PacketCodec<RegistryByteBuf, ComponentChanges>(){

        @Override
        public ComponentChanges decode(RegistryByteBuf arg) {
            ComponentType lv;
            int l;
            int i = arg.readVarInt();
            int j = arg.readVarInt();
            if (i == 0 && j == 0) {
                return EMPTY;
            }
            int k = i + j;
            Reference2ObjectArrayMap reference2ObjectMap = new Reference2ObjectArrayMap(Math.min(k, 65536));
            for (l = 0; l < i; ++l) {
                lv = (ComponentType)ComponentType.PACKET_CODEC.decode(arg);
                Object object = lv.getPacketCodec().decode(arg);
                reference2ObjectMap.put(lv, Optional.of(object));
            }
            for (l = 0; l < j; ++l) {
                lv = (ComponentType)ComponentType.PACKET_CODEC.decode(arg);
                reference2ObjectMap.put(lv, Optional.empty());
            }
            return new ComponentChanges(reference2ObjectMap);
        }

        @Override
        public void encode(RegistryByteBuf arg, ComponentChanges arg2) {
            if (arg2.isEmpty()) {
                arg.writeVarInt(0);
                arg.writeVarInt(0);
                return;
            }
            int i = 0;
            int j = 0;
            for (Reference2ObjectMap.Entry entry : Reference2ObjectMaps.fastIterable(arg2.changedComponents)) {
                if (((Optional)entry.getValue()).isPresent()) {
                    ++i;
                    continue;
                }
                ++j;
            }
            arg.writeVarInt(i);
            arg.writeVarInt(j);
            for (Reference2ObjectMap.Entry entry : Reference2ObjectMaps.fastIterable(arg2.changedComponents)) {
                Optional optional = (Optional)entry.getValue();
                if (!optional.isPresent()) continue;
                ComponentType lv = (ComponentType)entry.getKey();
                ComponentType.PACKET_CODEC.encode(arg, lv);
                1.encode(arg, lv, optional.get());
            }
            for (Reference2ObjectMap.Entry entry : Reference2ObjectMaps.fastIterable(arg2.changedComponents)) {
                if (!((Optional)entry.getValue()).isEmpty()) continue;
                ComponentType lv2 = (ComponentType)entry.getKey();
                ComponentType.PACKET_CODEC.encode(arg, lv2);
            }
        }

        private static <T> void encode(RegistryByteBuf buf, ComponentType<T> type, Object value) {
            type.getPacketCodec().encode(buf, value);
        }

        @Override
        public /* synthetic */ void encode(Object object, Object object2) {
            this.encode((RegistryByteBuf)object, (ComponentChanges)object2);
        }

        @Override
        public /* synthetic */ Object decode(Object object) {
            return this.decode((RegistryByteBuf)object);
        }
    };
    private static final String REMOVE_PREFIX = "!";
    final Reference2ObjectMap<ComponentType<?>, Optional<?>> changedComponents;

    ComponentChanges(Reference2ObjectMap<ComponentType<?>, Optional<?>> changedComponents) {
        this.changedComponents = changedComponents;
    }

    public static Builder builder() {
        return new Builder();
    }

    @Nullable
    public <T> Optional<? extends T> get(ComponentType<? extends T> type) {
        return (Optional)this.changedComponents.get(type);
    }

    public Set<Map.Entry<ComponentType<?>, Optional<?>>> entrySet() {
        return this.changedComponents.entrySet();
    }

    public int size() {
        return this.changedComponents.size();
    }

    public ComponentChanges withRemovedIf(Predicate<ComponentType<?>> removedTypePredicate) {
        if (this.isEmpty()) {
            return EMPTY;
        }
        Reference2ObjectArrayMap reference2ObjectMap = new Reference2ObjectArrayMap(this.changedComponents);
        reference2ObjectMap.keySet().removeIf(removedTypePredicate);
        if (reference2ObjectMap.isEmpty()) {
            return EMPTY;
        }
        return new ComponentChanges(reference2ObjectMap);
    }

    public boolean isEmpty() {
        return this.changedComponents.isEmpty();
    }

    public AddedRemovedPair toAddedRemovedPair() {
        if (this.isEmpty()) {
            return AddedRemovedPair.EMPTY;
        }
        ComponentMap.Builder lv = ComponentMap.builder();
        Set<ComponentType<?>> set = Sets.newIdentityHashSet();
        this.changedComponents.forEach((type, value) -> {
            if (value.isPresent()) {
                lv.put(type, value.get());
            } else {
                set.add((ComponentType<?>)type);
            }
        });
        return new AddedRemovedPair(lv.build(), set);
    }

    /*
     * Enabled force condition propagation
     * Lifted jumps to return sites
     */
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof ComponentChanges)) return false;
        ComponentChanges lv = (ComponentChanges)o;
        if (!this.changedComponents.equals(lv.changedComponents)) return false;
        return true;
    }

    public int hashCode() {
        return this.changedComponents.hashCode();
    }

    public String toString() {
        return ComponentChanges.toString(this.changedComponents);
    }

    static String toString(Reference2ObjectMap<ComponentType<?>, Optional<?>> changes) {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append('{');
        boolean bl = true;
        for (Map.Entry entry : Reference2ObjectMaps.fastIterable(changes)) {
            if (bl) {
                bl = false;
            } else {
                stringBuilder.append(", ");
            }
            Optional optional = (Optional)entry.getValue();
            if (optional.isPresent()) {
                stringBuilder.append(entry.getKey());
                stringBuilder.append("=>");
                stringBuilder.append(optional.get());
                continue;
            }
            stringBuilder.append(REMOVE_PREFIX);
            stringBuilder.append(entry.getKey());
        }
        stringBuilder.append('}');
        return stringBuilder.toString();
    }

    public static class Builder {
        private final Reference2ObjectMap<ComponentType<?>, Optional<?>> changes = new Reference2ObjectArrayMap();

        Builder() {
        }

        public <T> Builder add(ComponentType<T> type, T value) {
            this.changes.put(type, Optional.of(value));
            return this;
        }

        public <T> Builder remove(ComponentType<T> type) {
            this.changes.put(type, Optional.empty());
            return this;
        }

        public <T> Builder add(Component<T> component) {
            return this.add(component.type(), component.value());
        }

        public ComponentChanges build() {
            if (this.changes.isEmpty()) {
                return EMPTY;
            }
            return new ComponentChanges(this.changes);
        }
    }

    public record AddedRemovedPair(ComponentMap added, Set<ComponentType<?>> removed) {
        public static final AddedRemovedPair EMPTY = new AddedRemovedPair(ComponentMap.EMPTY, Set.of());
    }

    record Type(ComponentType<?> type, boolean removed) {
        public static final Codec<Type> CODEC = Codec.STRING.flatXmap(id -> {
            Identifier lv;
            ComponentType<?> lv2;
            boolean bl = id.startsWith(ComponentChanges.REMOVE_PREFIX);
            if (bl) {
                id = id.substring(ComponentChanges.REMOVE_PREFIX.length());
            }
            if ((lv2 = Registries.DATA_COMPONENT_TYPE.get(lv = Identifier.tryParse(id))) == null) {
                return DataResult.error(() -> "No component with type: '" + String.valueOf(lv) + "'");
            }
            if (lv2.shouldSkipSerialization()) {
                return DataResult.error(() -> "'" + String.valueOf(lv) + "' is not a persistent component");
            }
            return DataResult.success(new Type(lv2, bl));
        }, type -> {
            ComponentType<?> lv = type.type();
            Identifier lv2 = Registries.DATA_COMPONENT_TYPE.getId(lv);
            if (lv2 == null) {
                return DataResult.error(() -> "Unregistered component: " + String.valueOf(lv));
            }
            return DataResult.success(type.removed() ? ComponentChanges.REMOVE_PREFIX + String.valueOf(lv2) : lv2.toString());
        });

        public Codec<?> getValueCodec() {
            return this.removed ? Codec.EMPTY.codec() : this.type.getCodecOrThrow();
        }
    }
}

