/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 * 
 * Could not load the following classes:
 *  org.jetbrains.annotations.Nullable
 */
package net.minecraft.entity;

import net.minecraft.entity.Entity;
import org.jetbrains.annotations.Nullable;

public interface Ownable {
    @Nullable
    public Entity getOwner();
}

