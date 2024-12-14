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

public class GenericContainerScreenHandler
extends ScreenHandler {
    private static final int NUM_COLUMNS = 9;
    private final Inventory inventory;
    private final int rows;

    private GenericContainerScreenHandler(ScreenHandlerType<?> type, int syncId, PlayerInventory playerInventory, int rows) {
        this(type, syncId, playerInventory, new SimpleInventory(9 * rows), rows);
    }

    public static GenericContainerScreenHandler createGeneric9x1(int syncId, PlayerInventory playerInventory) {
        return new GenericContainerScreenHandler(ScreenHandlerType.GENERIC_9X1, syncId, playerInventory, 1);
    }

    public static GenericContainerScreenHandler createGeneric9x2(int syncId, PlayerInventory playerInventory) {
        return new GenericContainerScreenHandler(ScreenHandlerType.GENERIC_9X2, syncId, playerInventory, 2);
    }

    public static GenericContainerScreenHandler createGeneric9x3(int syncId, PlayerInventory playerInventory) {
        return new GenericContainerScreenHandler(ScreenHandlerType.GENERIC_9X3, syncId, playerInventory, 3);
    }

    public static GenericContainerScreenHandler createGeneric9x4(int syncId, PlayerInventory playerInventory) {
        return new GenericContainerScreenHandler(ScreenHandlerType.GENERIC_9X4, syncId, playerInventory, 4);
    }

    public static GenericContainerScreenHandler createGeneric9x5(int syncId, PlayerInventory playerInventory) {
        return new GenericContainerScreenHandler(ScreenHandlerType.GENERIC_9X5, syncId, playerInventory, 5);
    }

    public static GenericContainerScreenHandler createGeneric9x6(int syncId, PlayerInventory playerInventory) {
        return new GenericContainerScreenHandler(ScreenHandlerType.GENERIC_9X6, syncId, playerInventory, 6);
    }

    public static GenericContainerScreenHandler createGeneric9x3(int syncId, PlayerInventory playerInventory, Inventory inventory) {
        return new GenericContainerScreenHandler(ScreenHandlerType.GENERIC_9X3, syncId, playerInventory, inventory, 3);
    }

    public static GenericContainerScreenHandler createGeneric9x6(int syncId, PlayerInventory playerInventory, Inventory inventory) {
        return new GenericContainerScreenHandler(ScreenHandlerType.GENERIC_9X6, syncId, playerInventory, inventory, 6);
    }

    public GenericContainerScreenHandler(ScreenHandlerType<?> type, int syncId, PlayerInventory playerInventory, Inventory inventory, int rows) {
        super(type, syncId);
        int m;
        int l;
        GenericContainerScreenHandler.checkSize(inventory, rows * 9);
        this.inventory = inventory;
        this.rows = rows;
        inventory.onOpen(playerInventory.player);
        int k = (this.rows - 4) * 18;
        for (l = 0; l < this.rows; ++l) {
            for (m = 0; m < 9; ++m) {
                this.addSlot(new Slot(inventory, m + l * 9, 8 + m * 18, 18 + l * 18));
            }
        }
        for (l = 0; l < 3; ++l) {
            for (m = 0; m < 9; ++m) {
                this.addSlot(new Slot(playerInventory, m + l * 9 + 9, 8 + m * 18, 103 + l * 18 + k));
            }
        }
        for (l = 0; l < 9; ++l) {
            this.addSlot(new Slot(playerInventory, l, 8 + l * 18, 161 + k));
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
            if (slot < this.rows * 9 ? !this.insertItem(lv3, this.rows * 9, this.slots.size(), true) : !this.insertItem(lv3, 0, this.rows * 9, false)) {
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

    public Inventory getInventory() {
        return this.inventory;
    }

    public int getRows() {
        return this.rows;
    }
}

