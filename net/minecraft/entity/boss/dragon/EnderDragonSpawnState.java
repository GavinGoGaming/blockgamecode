/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.entity.boss.dragon;

import com.google.common.collect.ImmutableList;
import java.util.List;
import net.minecraft.entity.boss.dragon.EnderDragonFight;
import net.minecraft.entity.decoration.EndCrystalEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.World;
import net.minecraft.world.WorldEvents;
import net.minecraft.world.gen.feature.EndSpikeFeature;
import net.minecraft.world.gen.feature.EndSpikeFeatureConfig;
import net.minecraft.world.gen.feature.Feature;

public enum EnderDragonSpawnState {
    START{

        @Override
        public void run(ServerWorld world, EnderDragonFight fight, List<EndCrystalEntity> crystals, int tick, BlockPos pos) {
            BlockPos lv = new BlockPos(0, 128, 0);
            for (EndCrystalEntity lv2 : crystals) {
                lv2.setBeamTarget(lv);
            }
            fight.setSpawnState(PREPARING_TO_SUMMON_PILLARS);
        }
    }
    ,
    PREPARING_TO_SUMMON_PILLARS{

        @Override
        public void run(ServerWorld world, EnderDragonFight fight, List<EndCrystalEntity> crystals, int tick, BlockPos pos) {
            if (tick < 100) {
                if (tick == 0 || tick == 50 || tick == 51 || tick == 52 || tick >= 95) {
                    world.syncWorldEvent(WorldEvents.ENDER_DRAGON_RESURRECTED, new BlockPos(0, 128, 0), 0);
                }
            } else {
                fight.setSpawnState(SUMMONING_PILLARS);
            }
        }
    }
    ,
    SUMMONING_PILLARS{

        @Override
        public void run(ServerWorld world, EnderDragonFight fight, List<EndCrystalEntity> crystals, int tick, BlockPos pos) {
            boolean bl2;
            int j = 40;
            boolean bl = tick % 40 == 0;
            boolean bl3 = bl2 = tick % 40 == 39;
            if (bl || bl2) {
                int k = tick / 40;
                List<EndSpikeFeature.Spike> list2 = EndSpikeFeature.getSpikes(world);
                if (k < list2.size()) {
                    EndSpikeFeature.Spike lv = list2.get(k);
                    if (bl) {
                        for (EndCrystalEntity lv2 : crystals) {
                            lv2.setBeamTarget(new BlockPos(lv.getCenterX(), lv.getHeight() + 1, lv.getCenterZ()));
                        }
                    } else {
                        int l = 10;
                        for (BlockPos lv3 : BlockPos.iterate(new BlockPos(lv.getCenterX() - 10, lv.getHeight() - 10, lv.getCenterZ() - 10), new BlockPos(lv.getCenterX() + 10, lv.getHeight() + 10, lv.getCenterZ() + 10))) {
                            world.removeBlock(lv3, false);
                        }
                        world.createExplosion(null, (float)lv.getCenterX() + 0.5f, lv.getHeight(), (float)lv.getCenterZ() + 0.5f, 5.0f, World.ExplosionSourceType.BLOCK);
                        EndSpikeFeatureConfig lv4 = new EndSpikeFeatureConfig(true, ImmutableList.of(lv), new BlockPos(0, 128, 0));
                        Feature.END_SPIKE.generateIfValid(lv4, world, world.getChunkManager().getChunkGenerator(), Random.create(), new BlockPos(lv.getCenterX(), 45, lv.getCenterZ()));
                    }
                } else if (bl) {
                    fight.setSpawnState(SUMMONING_DRAGON);
                }
            }
        }
    }
    ,
    SUMMONING_DRAGON{

        @Override
        public void run(ServerWorld world, EnderDragonFight fight, List<EndCrystalEntity> crystals, int tick, BlockPos pos) {
            if (tick >= 100) {
                fight.setSpawnState(END);
                fight.resetEndCrystals();
                for (EndCrystalEntity lv : crystals) {
                    lv.setBeamTarget(null);
                    world.createExplosion(lv, lv.getX(), lv.getY(), lv.getZ(), 6.0f, World.ExplosionSourceType.NONE);
                    lv.discard();
                }
            } else if (tick >= 80) {
                world.syncWorldEvent(WorldEvents.ENDER_DRAGON_RESURRECTED, new BlockPos(0, 128, 0), 0);
            } else if (tick == 0) {
                for (EndCrystalEntity lv : crystals) {
                    lv.setBeamTarget(new BlockPos(0, 128, 0));
                }
            } else if (tick < 5) {
                world.syncWorldEvent(WorldEvents.ENDER_DRAGON_RESURRECTED, new BlockPos(0, 128, 0), 0);
            }
        }
    }
    ,
    END{

        @Override
        public void run(ServerWorld world, EnderDragonFight fight, List<EndCrystalEntity> crystals, int tick, BlockPos pos) {
        }
    };


    public abstract void run(ServerWorld var1, EnderDragonFight var2, List<EndCrystalEntity> var3, int var4, BlockPos var5);
}

