/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.registry.entry;

public interface RegistryEntryOwner<T> {
    default public boolean ownerEquals(RegistryEntryOwner<T> other) {
        return other == this;
    }
}

