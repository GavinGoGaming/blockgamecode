/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 * 
 * Could not load the following classes:
 *  org.jetbrains.annotations.Nullable
 */
package net.minecraft.util;

import net.minecraft.text.Text;
import org.jetbrains.annotations.Nullable;

public interface Nameable {
    public Text getName();

    default public boolean hasCustomName() {
        return this.getCustomName() != null;
    }

    default public Text getDisplayName() {
        return this.getName();
    }

    @Nullable
    default public Text getCustomName() {
        return null;
    }
}

