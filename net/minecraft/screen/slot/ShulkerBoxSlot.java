/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.screen.slot;

import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.slot.Slot;

public class ShulkerBoxSlot
extends Slot {
    public ShulkerBoxSlot(Inventory arg, int i, int j, int k) {
        super(arg, i, j, k);
    }

    @Override
    public boolean canInsert(ItemStack stack) {
        return stack.getItem().canBeNested();
    }
}

