/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.entity;

import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.inventory.SimpleInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.registry.RegistryWrapper;

public interface InventoryOwner {
    public static final String INVENTORY_KEY = "Inventory";

    public SimpleInventory getInventory();

    public static void pickUpItem(MobEntity entity, InventoryOwner inventoryOwner, ItemEntity item) {
        ItemStack lv = item.getStack();
        if (entity.canGather(lv)) {
            SimpleInventory lv2 = inventoryOwner.getInventory();
            boolean bl = lv2.canInsert(lv);
            if (!bl) {
                return;
            }
            entity.triggerItemPickedUpByEntityCriteria(item);
            int i = lv.getCount();
            ItemStack lv3 = lv2.addStack(lv);
            entity.sendPickup(item, i - lv3.getCount());
            if (lv3.isEmpty()) {
                item.discard();
            } else {
                lv.setCount(lv3.getCount());
            }
        }
    }

    default public void readInventory(NbtCompound nbt, RegistryWrapper.WrapperLookup arg2) {
        if (nbt.contains(INVENTORY_KEY, NbtElement.LIST_TYPE)) {
            this.getInventory().readNbtList(nbt.getList(INVENTORY_KEY, NbtElement.COMPOUND_TYPE), arg2);
        }
    }

    default public void writeInventory(NbtCompound nbt, RegistryWrapper.WrapperLookup arg2) {
        nbt.put(INVENTORY_KEY, this.getInventory().toNbtList(arg2));
    }
}

