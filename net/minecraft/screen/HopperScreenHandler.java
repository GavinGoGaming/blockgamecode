/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.screen;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.Inventory;
import net.minecraft.inventory.SimpleInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.ScreenHandlerType;
import net.minecraft.screen.slot.Slot;

public class HopperScreenHandler
extends ScreenHandler {
    public static final int SLOT_COUNT = 5;
    private final Inventory inventory;

    public HopperScreenHandler(int syncId, PlayerInventory playerInventory) {
        this(syncId, playerInventory, new SimpleInventory(5));
    }

    public HopperScreenHandler(int syncId, PlayerInventory playerInventory, Inventory inventory) {
        super(ScreenHandlerType.HOPPER, syncId);
        int k;
        this.inventory = inventory;
        HopperScreenHandler.checkSize(inventory, 5);
        inventory.onOpen(playerInventory.player);
        int j = 51;
        for (k = 0; k < 5; ++k) {
            this.addSlot(new Slot(inventory, k, 44 + k * 18, 20));
        }
        for (k = 0; k < 3; ++k) {
            for (int l = 0; l < 9; ++l) {
                this.addSlot(new Slot(playerInventory, l + k * 9 + 9, 8 + l * 18, k * 18 + 51));
            }
        }
        for (k = 0; k < 9; ++k) {
            this.addSlot(new Slot(playerInventory, k, 8 + k * 18, 109));
        }
    }

    @Override
    public boolean canUse(PlayerEntity player) {
        return this.inventory.canPlayerUse(player);
    }

    @Override
    public ItemStack quickMove(PlayerEntity player, int slot) {
        ItemStack lv = ItemStack.EMPTY;
        Slot lv2 = (Slot)this.slots.get(slot);
        if (lv2 != null && lv2.hasStack()) {
            ItemStack lv3 = lv2.getStack();
            lv = lv3.copy();
            if (slot < this.inventory.size() ? !this.insertItem(lv3, this.inventory.size(), this.slots.size(), true) : !this.insertItem(lv3, 0, this.inventory.size(), false)) {
                return ItemStack.EMPTY;
            }
            if (lv3.isEmpty()) {
                lv2.setStack(ItemStack.EMPTY);
            } else {
                lv2.markDirty();
            }
        }
        return lv;
    }

    @Override
    public void onClosed(PlayerEntity player) {
        super.onClosed(player);
        this.inventory.onClose(player);
    }
}

