/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.registry;

import com.google.common.collect.MapMaker;
import com.mojang.serialization.Codec;
import io.netty.buffer.ByteBuf;
import java.util.Optional;
import java.util.concurrent.ConcurrentMap;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.util.Identifier;

public class RegistryKey<T> {
    private static final ConcurrentMap<RegistryIdPair, RegistryKey<?>> INSTANCES = new MapMaker().weakValues().makeMap();
    private final Identifier registry;
    private final Identifier value;

    public static <T> Codec<RegistryKey<T>> createCodec(RegistryKey<? extends Registry<T>> registry) {
        return Identifier.CODEC.xmap(id -> RegistryKey.of(registry, id), RegistryKey::getValue);
    }

    public static <T> PacketCodec<ByteBuf, RegistryKey<T>> createPacketCodec(RegistryKey<? extends Registry<T>> registry) {
        return Identifier.PACKET_CODEC.xmap(id -> RegistryKey.of(registry, id), RegistryKey::getValue);
    }

    public static <T> RegistryKey<T> of(RegistryKey<? extends Registry<T>> registry, Identifier value) {
        return RegistryKey.of(registry.value, value);
    }

    public static <T> RegistryKey<Registry<T>> ofRegistry(Identifier registry) {
        return RegistryKey.of(RegistryKeys.ROOT, registry);
    }

    private static <T> RegistryKey<T> of(Identifier registry, Identifier value) {
        return INSTANCES.computeIfAbsent(new RegistryIdPair(registry, value), pair -> new RegistryKey(pair.registry, pair.id));
    }

    private RegistryKey(Identifier registry, Identifier value) {
        this.registry = registry;
        this.value = value;
    }

    public String toString() {
        return "ResourceKey[" + String.valueOf(this.registry) + " / " + String.valueOf(this.value) + "]";
    }

    public boolean isOf(RegistryKey<? extends Registry<?>> registry) {
        return this.registry.equals(registry.getValue());
    }

    public <E> Optional<RegistryKey<E>> tryCast(RegistryKey<? extends Registry<E>> registryRef) {
        return this.isOf(registryRef) ? Optional.of(this) : Optional.empty();
    }

    public Identifier getValue() {
        return this.value;
    }

    public Identifier getRegistry() {
        return this.registry;
    }

    public RegistryKey<Registry<T>> getRegistryRef() {
        return RegistryKey.ofRegistry(this.registry);
    }

    record RegistryIdPair(Identifier registry, Identifier id) {
    }
}

