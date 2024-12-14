/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.world.spawner;

import net.minecraft.block.BlockState;
import net.minecraft.entity.EntityData;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.SpawnReason;
import net.minecraft.entity.mob.PhantomEntity;
import net.minecraft.fluid.FluidState;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.stat.ServerStatHandler;
import net.minecraft.stat.Stats;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.GameRules;
import net.minecraft.world.LocalDifficulty;
import net.minecraft.world.SpawnHelper;
import net.minecraft.world.spawner.SpecialSpawner;

public class PhantomSpawner
implements SpecialSpawner {
    private int cooldown;

    @Override
    public int spawn(ServerWorld world, boolean spawnMonsters, boolean spawnAnimals) {
        if (!spawnMonsters) {
            return 0;
        }
        if (!world.getGameRules().getBoolean(GameRules.DO_INSOMNIA)) {
            return 0;
        }
        Random lv = world.random;
        --this.cooldown;
        if (this.cooldown > 0) {
            return 0;
        }
        this.cooldown += (60 + lv.nextInt(60)) * 20;
        if (world.getAmbientDarkness() < 5 && world.getDimension().hasSkyLight()) {
            return 0;
        }
        int i = 0;
        for (ServerPlayerEntity lv2 : world.getPlayers()) {
            FluidState lv8;
            BlockState lv7;
            BlockPos lv6;
            LocalDifficulty lv4;
            if (lv2.isSpectator()) continue;
            BlockPos lv3 = lv2.getBlockPos();
            if (world.getDimension().hasSkyLight() && (lv3.getY() < world.getSeaLevel() || !world.isSkyVisible(lv3)) || !(lv4 = world.getLocalDifficulty(lv3)).isHarderThan(lv.nextFloat() * 3.0f)) continue;
            ServerStatHandler lv5 = lv2.getStatHandler();
            int j = MathHelper.clamp(lv5.getStat(Stats.CUSTOM.getOrCreateStat(Stats.TIME_SINCE_REST)), 1, Integer.MAX_VALUE);
            int k = 24000;
            if (lv.nextInt(j) < 72000 || !SpawnHelper.isClearForSpawn(world, lv6 = lv3.up(20 + lv.nextInt(15)).east(-10 + lv.nextInt(21)).south(-10 + lv.nextInt(21)), lv7 = world.getBlockState(lv6), lv8 = world.getFluidState(lv6), EntityType.PHANTOM)) continue;
            EntityData lv9 = null;
            int l = 1 + lv.nextInt(lv4.getGlobalDifficulty().getId() + 1);
            for (int m = 0; m < l; ++m) {
                PhantomEntity lv10 = EntityType.PHANTOM.create(world);
                if (lv10 == null) continue;
                lv10.refreshPositionAndAngles(lv6, 0.0f, 0.0f);
                lv9 = lv10.initialize(world, lv4, SpawnReason.NATURAL, lv9);
                world.spawnEntityAndPassengers(lv10);
                ++i;
            }
        }
        return i;
    }
}

