/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.entity.ai.brain.sensor;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Lists;
import java.util.ArrayList;
import java.util.Set;
import net.minecraft.entity.ai.brain.Brain;
import net.minecraft.entity.ai.brain.MemoryModuleType;
import net.minecraft.entity.ai.brain.sensor.Sensor;
import net.minecraft.entity.passive.VillagerEntity;
import net.minecraft.registry.RegistryKey;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.GlobalPos;
import net.minecraft.world.World;

public class SecondaryPointsOfInterestSensor
extends Sensor<VillagerEntity> {
    private static final int RUN_TIME = 40;

    public SecondaryPointsOfInterestSensor() {
        super(40);
    }

    @Override
    protected void sense(ServerWorld arg, VillagerEntity arg2) {
        RegistryKey<World> lv = arg.getRegistryKey();
        BlockPos lv2 = arg2.getBlockPos();
        ArrayList<GlobalPos> list = Lists.newArrayList();
        int i = 4;
        for (int j = -4; j <= 4; ++j) {
            for (int k = -2; k <= 2; ++k) {
                for (int l = -4; l <= 4; ++l) {
                    BlockPos lv3 = lv2.add(j, k, l);
                    if (!arg2.getVillagerData().getProfession().secondaryJobSites().contains(arg.getBlockState(lv3).getBlock())) continue;
                    list.add(GlobalPos.create(lv, lv3));
                }
            }
        }
        Brain<VillagerEntity> lv4 = arg2.getBrain();
        if (!list.isEmpty()) {
            lv4.remember(MemoryModuleType.SECONDARY_JOB_SITE, list);
        } else {
            lv4.forget(MemoryModuleType.SECONDARY_JOB_SITE);
        }
    }

    @Override
    public Set<MemoryModuleType<?>> getOutputMemoryModules() {
        return ImmutableSet.of(MemoryModuleType.SECONDARY_JOB_SITE);
    }
}

