/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 * 
 * Could not load the following classes:
 *  org.jetbrains.annotations.Nullable
 */
package net.minecraft.block.entity;

import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.block.entity.LockableContainerBlockEntity;
import net.minecraft.component.ComponentMap;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.ContainerLootComponent;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.LootableInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.loot.LootTable;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.registry.RegistryKey;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.util.math.BlockPos;
import org.jetbrains.annotations.Nullable;

public abstract class LootableContainerBlockEntity
extends LockableContainerBlockEntity
implements LootableInventory {
    @Nullable
    protected RegistryKey<LootTable> lootTable;
    protected long lootTableSeed = 0L;

    protected LootableContainerBlockEntity(BlockEntityType<?> arg, BlockPos arg2, BlockState arg3) {
        super(arg, arg2, arg3);
    }

    @Override
    @Nullable
    public RegistryKey<LootTable> getLootTable() {
        return this.lootTable;
    }

    @Override
    public void setLootTable(@Nullable RegistryKey<LootTable> lootTable) {
        this.lootTable = lootTable;
    }

    @Override
    public long getLootTableSeed() {
        return this.lootTableSeed;
    }

    @Override
    public void setLootTableSeed(long lootTableSeed) {
        this.lootTableSeed = lootTableSeed;
    }

    @Override
    public boolean isEmpty() {
        this.generateLoot(null);
        return super.isEmpty();
    }

    @Override
    public ItemStack getStack(int slot) {
        this.generateLoot(null);
        return super.getStack(slot);
    }

    @Override
    public ItemStack removeStack(int slot, int amount) {
        this.generateLoot(null);
        return super.removeStack(slot, amount);
    }

    @Override
    public ItemStack removeStack(int slot) {
        this.generateLoot(null);
        return super.removeStack(slot);
    }

    @Override
    public void setStack(int slot, ItemStack stack) {
        this.generateLoot(null);
        super.setStack(slot, stack);
    }

    @Override
    public boolean checkUnlocked(PlayerEntity player) {
        return super.checkUnlocked(player) && (this.lootTable == null || !player.isSpectator());
    }

    @Override
    @Nullable
    public ScreenHandler createMenu(int i, PlayerInventory arg, PlayerEntity arg2) {
        if (this.checkUnlocked(arg2)) {
            this.generateLoot(arg.player);
            return this.createScreenHandler(i, arg);
        }
        return null;
    }

    @Override
    protected void readComponents(BlockEntity.ComponentsAccess components) {
        super.readComponents(components);
        ContainerLootComponent lv = components.get(DataComponentTypes.CONTAINER_LOOT);
        if (lv != null) {
            this.lootTable = lv.lootTable();
            this.lootTableSeed = lv.seed();
        }
    }

    @Override
    protected void addComponents(ComponentMap.Builder componentMapBuilder) {
        super.addComponents(componentMapBuilder);
        if (this.lootTable != null) {
            componentMapBuilder.add(DataComponentTypes.CONTAINER_LOOT, new ContainerLootComponent(this.lootTable, this.lootTableSeed));
        }
    }

    @Override
    public void removeFromCopiedStackNbt(NbtCompound nbt) {
        super.removeFromCopiedStackNbt(nbt);
        nbt.remove("LootTable");
        nbt.remove("LootTableSeed");
    }
}

