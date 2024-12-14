/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 * 
 * Could not load the following classes:
 *  org.jetbrains.annotations.Nullable
 */
package net.minecraft.world;

import java.util.Optional;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.SpawnLocation;
import net.minecraft.entity.SpawnReason;
import net.minecraft.entity.SpawnRestriction;
import net.minecraft.entity.passive.TraderLlamaEntity;
import net.minecraft.entity.passive.WanderingTraderEntity;
import net.minecraft.registry.tag.BiomeTags;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.BlockView;
import net.minecraft.world.GameRules;
import net.minecraft.world.Heightmap;
import net.minecraft.world.WorldView;
import net.minecraft.world.level.ServerWorldProperties;
import net.minecraft.world.poi.PointOfInterestStorage;
import net.minecraft.world.poi.PointOfInterestTypes;
import net.minecraft.world.spawner.SpecialSpawner;
import org.jetbrains.annotations.Nullable;

public class WanderingTraderManager
implements SpecialSpawner {
    private static final int DEFAULT_SPAWN_TIMER = 1200;
    public static final int DEFAULT_SPAWN_DELAY = 24000;
    private static final int MIN_SPAWN_CHANCE = 25;
    private static final int MAX_SPAWN_CHANCE = 75;
    private static final int DEFAULT_SPAWN_CHANCE = 25;
    private static final int field_30635 = 10;
    private static final int field_30636 = 10;
    private final Random random = Random.create();
    private final ServerWorldProperties properties;
    private int spawnTimer;
    private int spawnDelay;
    private int spawnChance;

    public WanderingTraderManager(ServerWorldProperties properties) {
        this.properties = properties;
        this.spawnTimer = 1200;
        this.spawnDelay = properties.getWanderingTraderSpawnDelay();
        this.spawnChance = properties.getWanderingTraderSpawnChance();
        if (this.spawnDelay == 0 && this.spawnChance == 0) {
            this.spawnDelay = 24000;
            properties.setWanderingTraderSpawnDelay(this.spawnDelay);
            this.spawnChance = 25;
            properties.setWanderingTraderSpawnChance(this.spawnChance);
        }
    }

    @Override
    public int spawn(ServerWorld world, boolean spawnMonsters, boolean spawnAnimals) {
        if (!world.getGameRules().getBoolean(GameRules.DO_TRADER_SPAWNING)) {
            return 0;
        }
        if (--this.spawnTimer > 0) {
            return 0;
        }
        this.spawnTimer = 1200;
        this.spawnDelay -= 1200;
        this.properties.setWanderingTraderSpawnDelay(this.spawnDelay);
        if (this.spawnDelay > 0) {
            return 0;
        }
        this.spawnDelay = 24000;
        if (!world.getGameRules().getBoolean(GameRules.DO_MOB_SPAWNING)) {
            return 0;
        }
        int i = this.spawnChance;
        this.spawnChance = MathHelper.clamp(this.spawnChance + 25, 25, 75);
        this.properties.setWanderingTraderSpawnChance(this.spawnChance);
        if (this.random.nextInt(100) > i) {
            return 0;
        }
        if (this.trySpawn(world)) {
            this.spawnChance = 25;
            return 1;
        }
        return 0;
    }

    private boolean trySpawn(ServerWorld world) {
        ServerPlayerEntity lv = world.getRandomAlivePlayer();
        if (lv == null) {
            return true;
        }
        if (this.random.nextInt(10) != 0) {
            return false;
        }
        BlockPos lv2 = lv.getBlockPos();
        int i = 48;
        PointOfInterestStorage lv3 = world.getPointOfInterestStorage();
        Optional<BlockPos> optional = lv3.getPosition(poiType -> poiType.matchesKey(PointOfInterestTypes.MEETING), pos -> true, lv2, 48, PointOfInterestStorage.OccupationStatus.ANY);
        BlockPos lv4 = optional.orElse(lv2);
        BlockPos lv5 = this.getNearbySpawnPos(world, lv4, 48);
        if (lv5 != null && this.doesNotSuffocateAt(world, lv5)) {
            if (world.getBiome(lv5).isIn(BiomeTags.WITHOUT_WANDERING_TRADER_SPAWNS)) {
                return false;
            }
            WanderingTraderEntity lv6 = EntityType.WANDERING_TRADER.spawn(world, lv5, SpawnReason.EVENT);
            if (lv6 != null) {
                for (int j = 0; j < 2; ++j) {
                    this.spawnLlama(world, lv6, 4);
                }
                this.properties.setWanderingTraderId(lv6.getUuid());
                lv6.setDespawnDelay(48000);
                lv6.setWanderTarget(lv4);
                lv6.setPositionTarget(lv4, 16);
                return true;
            }
        }
        return false;
    }

    private void spawnLlama(ServerWorld world, WanderingTraderEntity wanderingTrader, int range) {
        BlockPos lv = this.getNearbySpawnPos(world, wanderingTrader.getBlockPos(), range);
        if (lv == null) {
            return;
        }
        TraderLlamaEntity lv2 = EntityType.TRADER_LLAMA.spawn(world, lv, SpawnReason.EVENT);
        if (lv2 == null) {
            return;
        }
        lv2.attachLeash(wanderingTrader, true);
    }

    @Nullable
    private BlockPos getNearbySpawnPos(WorldView world, BlockPos pos, int range) {
        BlockPos lv = null;
        SpawnLocation lv2 = SpawnRestriction.getLocation(EntityType.WANDERING_TRADER);
        for (int j = 0; j < 10; ++j) {
            int l;
            int m;
            int k = pos.getX() + this.random.nextInt(range * 2) - range;
            BlockPos lv3 = new BlockPos(k, m = world.getTopY(Heightmap.Type.WORLD_SURFACE, k, l = pos.getZ() + this.random.nextInt(range * 2) - range), l);
            if (!lv2.isSpawnPositionOk(world, lv3, EntityType.WANDERING_TRADER)) continue;
            lv = lv3;
            break;
        }
        return lv;
    }

    private boolean doesNotSuffocateAt(BlockView world, BlockPos pos) {
        for (BlockPos lv : BlockPos.iterate(pos, pos.add(1, 2, 1))) {
            if (world.getBlockState(lv).getCollisionShape(world, lv).isEmpty()) continue;
            return false;
        }
        return true;
    }
}

