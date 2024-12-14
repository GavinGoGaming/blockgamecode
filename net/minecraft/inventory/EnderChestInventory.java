/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 * 
 * Could not load the following classes:
 *  org.jetbrains.annotations.Nullable
 */
package net.minecraft.inventory;

import net.minecraft.block.entity.EnderChestBlockEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.SimpleInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtList;
import net.minecraft.registry.RegistryWrapper;
import org.jetbrains.annotations.Nullable;

public class EnderChestInventory
extends SimpleInventory {
    @Nullable
    private EnderChestBlockEntity activeBlockEntity;

    public EnderChestInventory() {
        super(27);
    }

    public void setActiveBlockEntity(EnderChestBlockEntity blockEntity) {
        this.activeBlockEntity = blockEntity;
    }

    public boolean isActiveBlockEntity(EnderChestBlockEntity blockEntity) {
        return this.activeBlockEntity == blockEntity;
    }

    @Override
    public void readNbtList(NbtList list, RegistryWrapper.WrapperLookup registries) {
        int i;
        for (i = 0; i < this.size(); ++i) {
            this.setStack(i, ItemStack.EMPTY);
        }
        for (i = 0; i < list.size(); ++i) {
            NbtCompound lv = list.getCompound(i);
            int j = lv.getByte("Slot") & 0xFF;
            if (j < 0 || j >= this.size()) continue;
            this.setStack(j, ItemStack.fromNbt(registries, lv).orElse(ItemStack.EMPTY));
        }
    }

    @Override
    public NbtList toNbtList(RegistryWrapper.WrapperLookup registries) {
        NbtList lv = new NbtList();
        for (int i = 0; i < this.size(); ++i) {
            ItemStack lv2 = this.getStack(i);
            if (lv2.isEmpty()) continue;
            NbtCompound lv3 = new NbtCompound();
            lv3.putByte("Slot", (byte)i);
            lv.add(lv2.encode(registries, lv3));
        }
        return lv;
    }

    @Override
    public boolean canPlayerUse(PlayerEntity player) {
        if (this.activeBlockEntity != null && !this.activeBlockEntity.canPlayerUse(player)) {
            return false;
        }
        return super.canPlayerUse(player);
    }

    @Override
    public void onOpen(PlayerEntity player) {
        if (this.activeBlockEntity != null) {
            this.activeBlockEntity.onOpen(player);
        }
        super.onOpen(player);
    }

    @Override
    public void onClose(PlayerEntity player) {
        if (this.activeBlockEntity != null) {
            this.activeBlockEntity.onClose(player);
        }
        super.onClose(player);
        this.activeBlockEntity = null;
    }
}

