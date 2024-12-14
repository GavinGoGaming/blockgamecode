/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.registry;

import com.mojang.serialization.Codec;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.entry.RegistryElementCodec;
import net.minecraft.registry.entry.RegistryEntryList;
import net.minecraft.registry.entry.RegistryEntryListCodec;
import net.minecraft.registry.entry.RegistryFixedCodec;

public class RegistryCodecs {
    public static <E> Codec<RegistryEntryList<E>> entryList(RegistryKey<? extends Registry<E>> registryRef, Codec<E> elementCodec) {
        return RegistryCodecs.entryList(registryRef, elementCodec, false);
    }

    public static <E> Codec<RegistryEntryList<E>> entryList(RegistryKey<? extends Registry<E>> registryRef, Codec<E> elementCodec, boolean alwaysSerializeAsList) {
        return RegistryEntryListCodec.create(registryRef, RegistryElementCodec.of(registryRef, elementCodec), alwaysSerializeAsList);
    }

    public static <E> Codec<RegistryEntryList<E>> entryList(RegistryKey<? extends Registry<E>> registryRef) {
        return RegistryCodecs.entryList(registryRef, false);
    }

    public static <E> Codec<RegistryEntryList<E>> entryList(RegistryKey<? extends Registry<E>> registryRef, boolean alwaysSerializeAsList) {
        return RegistryEntryListCodec.create(registryRef, RegistryFixedCodec.of(registryRef), alwaysSerializeAsList);
    }
}

