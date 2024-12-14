/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 * 
 * Could not load the following classes:
 *  org.jetbrains.annotations.Nullable
 */
package net.minecraft.util;

import org.jetbrains.annotations.Nullable;

public interface Clearable {
    public void clear();

    public static void clear(@Nullable Object o) {
        if (o instanceof Clearable) {
            ((Clearable)o).clear();
        }
    }
}

