/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 * 
 * Could not load the following classes:
 *  org.jetbrains.annotations.Nullable
 */
package net.minecraft.inventory;

import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.Direction;
import org.jetbrains.annotations.Nullable;

public interface SidedInventory
extends Inventory {
    public int[] getAvailableSlots(Direction var1);

    public boolean canInsert(int var1, ItemStack var2, @Nullable Direction var3);

    public boolean canExtract(int var1, ItemStack var2, Direction var3);
}

