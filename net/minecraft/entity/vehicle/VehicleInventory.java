/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 * 
 * Could not load the following classes:
 *  org.jetbrains.annotations.Nullable
 */
package net.minecraft.entity.vehicle;

import net.minecraft.advancement.criterion.Criteria;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.mob.PiglinBrain;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.Inventories;
import net.minecraft.inventory.Inventory;
import net.minecraft.inventory.StackReference;
import net.minecraft.item.ItemStack;
import net.minecraft.loot.LootTable;
import net.minecraft.loot.context.LootContextParameterSet;
import net.minecraft.loot.context.LootContextParameters;
import net.minecraft.loot.context.LootContextTypes;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.screen.NamedScreenHandlerFactory;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Identifier;
import net.minecraft.util.ItemScatterer;
import net.minecraft.util.collection.DefaultedList;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.GameRules;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

public interface VehicleInventory
extends Inventory,
NamedScreenHandlerFactory {
    public Vec3d getPos();

    public Box getBoundingBox();

    @Nullable
    public RegistryKey<LootTable> getLootTable();

    public void setLootTable(@Nullable RegistryKey<LootTable> var1);

    public long getLootTableSeed();

    public void setLootTableSeed(long var1);

    public DefaultedList<ItemStack> getInventory();

    public void resetInventory();

    public World getWorld();

    public boolean isRemoved();

    @Override
    default public boolean isEmpty() {
        return this.isInventoryEmpty();
    }

    default public void writeInventoryToNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup registriesLookup) {
        if (this.getLootTable() != null) {
            nbt.putString("LootTable", this.getLootTable().getValue().toString());
            if (this.getLootTableSeed() != 0L) {
                nbt.putLong("LootTableSeed", this.getLootTableSeed());
            }
        } else {
            Inventories.writeNbt(nbt, this.getInventory(), registriesLookup);
        }
    }

    default public void readInventoryFromNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup registriesLookup) {
        this.resetInventory();
        if (nbt.contains("LootTable", NbtElement.STRING_TYPE)) {
            this.setLootTable(RegistryKey.of(RegistryKeys.LOOT_TABLE, Identifier.of(nbt.getString("LootTable"))));
            this.setLootTableSeed(nbt.getLong("LootTableSeed"));
        } else {
            Inventories.readNbt(nbt, this.getInventory(), registriesLookup);
        }
    }

    default public void onBroken(DamageSource source, World world, Entity vehicle) {
        Entity lv;
        if (!world.getGameRules().getBoolean(GameRules.DO_ENTITY_DROPS)) {
            return;
        }
        ItemScatterer.spawn(world, vehicle, (Inventory)this);
        if (!world.isClient && (lv = source.getSource()) != null && lv.getType() == EntityType.PLAYER) {
            PiglinBrain.onGuardedBlockInteracted((PlayerEntity)lv, true);
        }
    }

    default public ActionResult open(PlayerEntity player) {
        player.openHandledScreen(this);
        if (!player.getWorld().isClient) {
            return ActionResult.CONSUME;
        }
        return ActionResult.SUCCESS;
    }

    default public void generateInventoryLoot(@Nullable PlayerEntity player) {
        MinecraftServer minecraftServer = this.getWorld().getServer();
        if (this.getLootTable() != null && minecraftServer != null) {
            LootTable lv = minecraftServer.getReloadableRegistries().getLootTable(this.getLootTable());
            if (player != null) {
                Criteria.PLAYER_GENERATES_CONTAINER_LOOT.trigger((ServerPlayerEntity)player, this.getLootTable());
            }
            this.setLootTable(null);
            LootContextParameterSet.Builder lv2 = new LootContextParameterSet.Builder((ServerWorld)this.getWorld()).add(LootContextParameters.ORIGIN, this.getPos());
            if (player != null) {
                lv2.luck(player.getLuck()).add(LootContextParameters.THIS_ENTITY, player);
            }
            lv.supplyInventory(this, lv2.build(LootContextTypes.CHEST), this.getLootTableSeed());
        }
    }

    default public void clearInventory() {
        this.generateInventoryLoot(null);
        this.getInventory().clear();
    }

    default public boolean isInventoryEmpty() {
        for (ItemStack lv : this.getInventory()) {
            if (lv.isEmpty()) continue;
            return false;
        }
        return true;
    }

    default public ItemStack removeInventoryStack(int slot) {
        this.generateInventoryLoot(null);
        ItemStack lv = this.getInventory().get(slot);
        if (lv.isEmpty()) {
            return ItemStack.EMPTY;
        }
        this.getInventory().set(slot, ItemStack.EMPTY);
        return lv;
    }

    default public ItemStack getInventoryStack(int slot) {
        this.generateInventoryLoot(null);
        return this.getInventory().get(slot);
    }

    default public ItemStack removeInventoryStack(int slot, int amount) {
        this.generateInventoryLoot(null);
        return Inventories.splitStack(this.getInventory(), slot, amount);
    }

    default public void setInventoryStack(int slot, ItemStack stack) {
        this.generateInventoryLoot(null);
        this.getInventory().set(slot, stack);
        stack.capCount(this.getMaxCount(stack));
    }

    default public StackReference getInventoryStackReference(final int slot) {
        if (slot >= 0 && slot < this.size()) {
            return new StackReference(){

                @Override
                public ItemStack get() {
                    return VehicleInventory.this.getInventoryStack(slot);
                }

                @Override
                public boolean set(ItemStack stack) {
                    VehicleInventory.this.setInventoryStack(slot, stack);
                    return true;
                }
            };
        }
        return StackReference.EMPTY;
    }

    default public boolean canPlayerAccess(PlayerEntity player) {
        return !this.isRemoved() && player.canInteractWithEntityIn(this.getBoundingBox(), 4.0);
    }
}

