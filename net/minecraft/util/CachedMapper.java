/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 * 
 * Could not load the following classes:
 *  org.jetbrains.annotations.Nullable
 */
package net.minecraft.util;

import java.util.Objects;
import java.util.function.Function;
import org.jetbrains.annotations.Nullable;

public class CachedMapper<K, V> {
    private final Function<K, V> mapper;
    @Nullable
    private K cachedInput = null;
    @Nullable
    private V cachedOutput;

    public CachedMapper(Function<K, V> mapper) {
        this.mapper = mapper;
    }

    public V map(K input) {
        if (this.cachedOutput == null || !Objects.equals(this.cachedInput, input)) {
            this.cachedOutput = this.mapper.apply(input);
            this.cachedInput = input;
        }
        return this.cachedOutput;
    }
}

