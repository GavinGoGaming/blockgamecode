/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.inventory;

import java.util.List;
import java.util.function.Predicate;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtList;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.util.collection.DefaultedList;

public class Inventories {
    public static final String ITEMS_NBT_KEY = "Items";

    public static ItemStack splitStack(List<ItemStack> stacks, int slot, int amount) {
        if (slot < 0 || slot >= stacks.size() || stacks.get(slot).isEmpty() || amount <= 0) {
            return ItemStack.EMPTY;
        }
        return stacks.get(slot).split(amount);
    }

    public static ItemStack removeStack(List<ItemStack> stacks, int slot) {
        if (slot < 0 || slot >= stacks.size()) {
            return ItemStack.EMPTY;
        }
        return stacks.set(slot, ItemStack.EMPTY);
    }

    public static NbtCompound writeNbt(NbtCompound nbt, DefaultedList<ItemStack> stacks, RegistryWrapper.WrapperLookup registries) {
        return Inventories.writeNbt(nbt, stacks, true, registries);
    }

    public static NbtCompound writeNbt(NbtCompound nbt, DefaultedList<ItemStack> stacks, boolean setIfEmpty, RegistryWrapper.WrapperLookup registries) {
        NbtList lv = new NbtList();
        for (int i = 0; i < stacks.size(); ++i) {
            ItemStack lv2 = stacks.get(i);
            if (lv2.isEmpty()) continue;
            NbtCompound lv3 = new NbtCompound();
            lv3.putByte("Slot", (byte)i);
            lv.add(lv2.encode(registries, lv3));
        }
        if (!lv.isEmpty() || setIfEmpty) {
            nbt.put(ITEMS_NBT_KEY, lv);
        }
        return nbt;
    }

    public static void readNbt(NbtCompound nbt, DefaultedList<ItemStack> stacks, RegistryWrapper.WrapperLookup registries) {
        NbtList lv = nbt.getList(ITEMS_NBT_KEY, NbtElement.COMPOUND_TYPE);
        for (int i = 0; i < lv.size(); ++i) {
            NbtCompound lv2 = lv.getCompound(i);
            int j = lv2.getByte("Slot") & 0xFF;
            if (j < 0 || j >= stacks.size()) continue;
            stacks.set(j, ItemStack.fromNbt(registries, lv2).orElse(ItemStack.EMPTY));
        }
    }

    public static int remove(Inventory inventory, Predicate<ItemStack> shouldRemove, int maxCount, boolean dryRun) {
        int j = 0;
        for (int k = 0; k < inventory.size(); ++k) {
            ItemStack lv = inventory.getStack(k);
            int l = Inventories.remove(lv, shouldRemove, maxCount - j, dryRun);
            if (l > 0 && !dryRun && lv.isEmpty()) {
                inventory.setStack(k, ItemStack.EMPTY);
            }
            j += l;
        }
        return j;
    }

    public static int remove(ItemStack stack, Predicate<ItemStack> shouldRemove, int maxCount, boolean dryRun) {
        if (stack.isEmpty() || !shouldRemove.test(stack)) {
            return 0;
        }
        if (dryRun) {
            return stack.getCount();
        }
        int j = maxCount < 0 ? stack.getCount() : Math.min(maxCount, stack.getCount());
        stack.decrement(j);
        return j;
    }
}

