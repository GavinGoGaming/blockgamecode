/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 * 
 * Could not load the following classes:
 *  org.jetbrains.annotations.Nullable
 */
package net.minecraft.inventory;

import net.minecraft.advancement.criterion.Criteria;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.Inventory;
import net.minecraft.loot.LootTable;
import net.minecraft.loot.context.LootContextParameterSet;
import net.minecraft.loot.context.LootContextParameters;
import net.minecraft.loot.context.LootContextTypes;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

public interface LootableInventory
extends Inventory {
    public static final String LOOT_TABLE_KEY = "LootTable";
    public static final String LOOT_TABLE_SEED_KEY = "LootTableSeed";

    @Nullable
    public RegistryKey<LootTable> getLootTable();

    public void setLootTable(@Nullable RegistryKey<LootTable> var1);

    default public void setLootTable(RegistryKey<LootTable> lootTableId, long lootTableSeed) {
        this.setLootTable(lootTableId);
        this.setLootTableSeed(lootTableSeed);
    }

    public long getLootTableSeed();

    public void setLootTableSeed(long var1);

    public BlockPos getPos();

    @Nullable
    public World getWorld();

    public static void setLootTable(BlockView world, Random random, BlockPos pos, RegistryKey<LootTable> lootTableId) {
        BlockEntity lv = world.getBlockEntity(pos);
        if (lv instanceof LootableInventory) {
            LootableInventory lv2 = (LootableInventory)((Object)lv);
            lv2.setLootTable(lootTableId, random.nextLong());
        }
    }

    default public boolean readLootTable(NbtCompound nbt) {
        if (nbt.contains(LOOT_TABLE_KEY, NbtElement.STRING_TYPE)) {
            this.setLootTable(RegistryKey.of(RegistryKeys.LOOT_TABLE, Identifier.of(nbt.getString(LOOT_TABLE_KEY))));
            if (nbt.contains(LOOT_TABLE_SEED_KEY, NbtElement.LONG_TYPE)) {
                this.setLootTableSeed(nbt.getLong(LOOT_TABLE_SEED_KEY));
            } else {
                this.setLootTableSeed(0L);
            }
            return true;
        }
        return false;
    }

    default public boolean writeLootTable(NbtCompound nbt) {
        RegistryKey<LootTable> lv = this.getLootTable();
        if (lv == null) {
            return false;
        }
        nbt.putString(LOOT_TABLE_KEY, lv.getValue().toString());
        long l = this.getLootTableSeed();
        if (l != 0L) {
            nbt.putLong(LOOT_TABLE_SEED_KEY, l);
        }
        return true;
    }

    default public void generateLoot(@Nullable PlayerEntity player) {
        World lv = this.getWorld();
        BlockPos lv2 = this.getPos();
        RegistryKey<LootTable> lv3 = this.getLootTable();
        if (lv3 != null && lv != null && lv.getServer() != null) {
            LootTable lv4 = lv.getServer().getReloadableRegistries().getLootTable(lv3);
            if (player instanceof ServerPlayerEntity) {
                Criteria.PLAYER_GENERATES_CONTAINER_LOOT.trigger((ServerPlayerEntity)player, lv3);
            }
            this.setLootTable(null);
            LootContextParameterSet.Builder lv5 = new LootContextParameterSet.Builder((ServerWorld)lv).add(LootContextParameters.ORIGIN, Vec3d.ofCenter(lv2));
            if (player != null) {
                lv5.luck(player.getLuck()).add(LootContextParameters.THIS_ENTITY, player);
            }
            lv4.supplyInventory(this, lv5.build(LootContextTypes.CHEST), this.getLootTableSeed());
        }
    }
}

