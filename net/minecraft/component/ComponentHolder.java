/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 * 
 * Could not load the following classes:
 *  org.jetbrains.annotations.Nullable
 */
package net.minecraft.component;

import net.minecraft.component.ComponentMap;
import net.minecraft.component.ComponentType;
import org.jetbrains.annotations.Nullable;

public interface ComponentHolder {
    public ComponentMap getComponents();

    @Nullable
    default public <T> T get(ComponentType<? extends T> type) {
        return this.getComponents().get(type);
    }

    default public <T> T getOrDefault(ComponentType<? extends T> type, T fallback) {
        return this.getComponents().getOrDefault(type, fallback);
    }

    default public boolean contains(ComponentType<?> type) {
        return this.getComponents().contains(type);
    }
}

