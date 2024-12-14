/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.registry.entry;

import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.Lifecycle;
import java.util.Optional;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryEntryLookup;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryOps;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.util.Identifier;

public final class RegistryElementCodec<E>
implements Codec<RegistryEntry<E>> {
    private final RegistryKey<? extends Registry<E>> registryRef;
    private final Codec<E> elementCodec;
    private final boolean allowInlineDefinitions;

    public static <E> RegistryElementCodec<E> of(RegistryKey<? extends Registry<E>> registryRef, Codec<E> elementCodec) {
        return RegistryElementCodec.of(registryRef, elementCodec, true);
    }

    public static <E> RegistryElementCodec<E> of(RegistryKey<? extends Registry<E>> registryRef, Codec<E> elementCodec, boolean allowInlineDefinitions) {
        return new RegistryElementCodec<E>(registryRef, elementCodec, allowInlineDefinitions);
    }

    private RegistryElementCodec(RegistryKey<? extends Registry<E>> registryRef, Codec<E> elementCodec, boolean allowInlineDefinitions) {
        this.registryRef = registryRef;
        this.elementCodec = elementCodec;
        this.allowInlineDefinitions = allowInlineDefinitions;
    }

    @Override
    public <T> DataResult<T> encode(RegistryEntry<E> arg, DynamicOps<T> dynamicOps, T object) {
        RegistryOps lv;
        Optional optional;
        if (dynamicOps instanceof RegistryOps && (optional = (lv = (RegistryOps)dynamicOps).getOwner(this.registryRef)).isPresent()) {
            if (!arg.ownerEquals(optional.get())) {
                return DataResult.error(() -> "Element " + String.valueOf(arg) + " is not valid in current registry set");
            }
            return arg.getKeyOrValue().map(key -> Identifier.CODEC.encode(key.getValue(), dynamicOps, object), value -> this.elementCodec.encode(value, dynamicOps, object));
        }
        return this.elementCodec.encode(arg.value(), dynamicOps, object);
    }

    @Override
    public <T> DataResult<Pair<RegistryEntry<E>, T>> decode(DynamicOps<T> ops, T input) {
        if (ops instanceof RegistryOps) {
            RegistryOps lv = (RegistryOps)ops;
            Optional optional = lv.getEntryLookup(this.registryRef);
            if (optional.isEmpty()) {
                return DataResult.error(() -> "Registry does not exist: " + String.valueOf(this.registryRef));
            }
            RegistryEntryLookup lv2 = optional.get();
            DataResult dataResult = Identifier.CODEC.decode(ops, input);
            if (dataResult.result().isEmpty()) {
                if (!this.allowInlineDefinitions) {
                    return DataResult.error(() -> "Inline definitions not allowed here");
                }
                return this.elementCodec.decode(ops, input).map((? super R pair) -> pair.mapFirst(RegistryEntry::of));
            }
            Pair pair2 = dataResult.result().get();
            RegistryKey lv3 = RegistryKey.of(this.registryRef, (Identifier)pair2.getFirst());
            return lv2.getOptional(lv3).map(DataResult::success).orElseGet(() -> DataResult.error(() -> "Failed to get element " + String.valueOf(lv3))).map((? super R arg) -> Pair.of(arg, pair2.getSecond())).setLifecycle(Lifecycle.stable());
        }
        return this.elementCodec.decode(ops, input).map((? super R pair) -> pair.mapFirst(RegistryEntry::of));
    }

    public String toString() {
        return "RegistryFileCodec[" + String.valueOf(this.registryRef) + " " + String.valueOf(this.elementCodec) + "]";
    }

    @Override
    public /* synthetic */ DataResult encode(Object input, DynamicOps ops, Object prefix) {
        return this.encode((RegistryEntry)input, ops, prefix);
    }
}

