/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.entity.ai.brain.sensor;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Lists;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.CampfireBlock;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.brain.Brain;
import net.minecraft.entity.ai.brain.LivingTargetCache;
import net.minecraft.entity.ai.brain.MemoryModuleType;
import net.minecraft.entity.ai.brain.sensor.Sensor;
import net.minecraft.entity.boss.WitherEntity;
import net.minecraft.entity.mob.AbstractPiglinEntity;
import net.minecraft.entity.mob.HoglinEntity;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.mob.PiglinBrain;
import net.minecraft.entity.mob.PiglinBruteEntity;
import net.minecraft.entity.mob.PiglinEntity;
import net.minecraft.entity.mob.WitherSkeletonEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.registry.tag.BlockTags;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;

public class PiglinSpecificSensor
extends Sensor<LivingEntity> {
    @Override
    public Set<MemoryModuleType<?>> getOutputMemoryModules() {
        return ImmutableSet.of(MemoryModuleType.VISIBLE_MOBS, MemoryModuleType.MOBS, MemoryModuleType.NEAREST_VISIBLE_NEMESIS, MemoryModuleType.NEAREST_TARGETABLE_PLAYER_NOT_WEARING_GOLD, MemoryModuleType.NEAREST_PLAYER_HOLDING_WANTED_ITEM, MemoryModuleType.NEAREST_VISIBLE_HUNTABLE_HOGLIN, new MemoryModuleType[]{MemoryModuleType.NEAREST_VISIBLE_BABY_HOGLIN, MemoryModuleType.NEAREST_VISIBLE_ADULT_PIGLINS, MemoryModuleType.NEARBY_ADULT_PIGLINS, MemoryModuleType.VISIBLE_ADULT_PIGLIN_COUNT, MemoryModuleType.VISIBLE_ADULT_HOGLIN_COUNT, MemoryModuleType.NEAREST_REPELLENT});
    }

    @Override
    protected void sense(ServerWorld world, LivingEntity entity) {
        Brain<?> lv = entity.getBrain();
        lv.remember(MemoryModuleType.NEAREST_REPELLENT, PiglinSpecificSensor.findPiglinRepellent(world, entity));
        Optional<Object> optional = Optional.empty();
        Optional<Object> optional2 = Optional.empty();
        Optional<Object> optional3 = Optional.empty();
        Optional<Object> optional4 = Optional.empty();
        Optional<Object> optional5 = Optional.empty();
        Optional<Object> optional6 = Optional.empty();
        Optional<Object> optional7 = Optional.empty();
        int i = 0;
        ArrayList<AbstractPiglinEntity> list = Lists.newArrayList();
        ArrayList<AbstractPiglinEntity> list2 = Lists.newArrayList();
        LivingTargetCache lv2 = lv.getOptionalRegisteredMemory(MemoryModuleType.VISIBLE_MOBS).orElse(LivingTargetCache.empty());
        for (LivingEntity lv3 : lv2.iterate(arg -> true)) {
            if (lv3 instanceof HoglinEntity) {
                HoglinEntity lv4 = (HoglinEntity)lv3;
                if (lv4.isBaby() && optional3.isEmpty()) {
                    optional3 = Optional.of(lv4);
                    continue;
                }
                if (!lv4.isAdult()) continue;
                ++i;
                if (!optional2.isEmpty() || !lv4.canBeHunted()) continue;
                optional2 = Optional.of(lv4);
                continue;
            }
            if (lv3 instanceof PiglinBruteEntity) {
                PiglinBruteEntity lv5 = (PiglinBruteEntity)lv3;
                list.add(lv5);
                continue;
            }
            if (lv3 instanceof PiglinEntity) {
                PiglinEntity lv6 = (PiglinEntity)lv3;
                if (lv6.isBaby() && optional4.isEmpty()) {
                    optional4 = Optional.of(lv6);
                    continue;
                }
                if (!lv6.isAdult()) continue;
                list.add(lv6);
                continue;
            }
            if (lv3 instanceof PlayerEntity) {
                PlayerEntity lv7 = (PlayerEntity)lv3;
                if (optional6.isEmpty() && !PiglinBrain.wearsGoldArmor(lv7) && entity.canTarget(lv3)) {
                    optional6 = Optional.of(lv7);
                }
                if (!optional7.isEmpty() || lv7.isSpectator() || !PiglinBrain.isGoldHoldingPlayer(lv7)) continue;
                optional7 = Optional.of(lv7);
                continue;
            }
            if (optional.isEmpty() && (lv3 instanceof WitherSkeletonEntity || lv3 instanceof WitherEntity)) {
                optional = Optional.of((MobEntity)lv3);
                continue;
            }
            if (!optional5.isEmpty() || !PiglinBrain.isZombified(lv3.getType())) continue;
            optional5 = Optional.of(lv3);
        }
        List list3 = lv.getOptionalRegisteredMemory(MemoryModuleType.MOBS).orElse(ImmutableList.of());
        for (LivingEntity lv8 : list3) {
            AbstractPiglinEntity lv9;
            if (!(lv8 instanceof AbstractPiglinEntity) || !(lv9 = (AbstractPiglinEntity)lv8).isAdult()) continue;
            list2.add(lv9);
        }
        lv.remember(MemoryModuleType.NEAREST_VISIBLE_NEMESIS, optional);
        lv.remember(MemoryModuleType.NEAREST_VISIBLE_HUNTABLE_HOGLIN, optional2);
        lv.remember(MemoryModuleType.NEAREST_VISIBLE_BABY_HOGLIN, optional3);
        lv.remember(MemoryModuleType.NEAREST_VISIBLE_ZOMBIFIED, optional5);
        lv.remember(MemoryModuleType.NEAREST_TARGETABLE_PLAYER_NOT_WEARING_GOLD, optional6);
        lv.remember(MemoryModuleType.NEAREST_PLAYER_HOLDING_WANTED_ITEM, optional7);
        lv.remember(MemoryModuleType.NEARBY_ADULT_PIGLINS, list2);
        lv.remember(MemoryModuleType.NEAREST_VISIBLE_ADULT_PIGLINS, list);
        lv.remember(MemoryModuleType.VISIBLE_ADULT_PIGLIN_COUNT, list.size());
        lv.remember(MemoryModuleType.VISIBLE_ADULT_HOGLIN_COUNT, i);
    }

    private static Optional<BlockPos> findPiglinRepellent(ServerWorld world, LivingEntity entity) {
        return BlockPos.findClosest(entity.getBlockPos(), 8, 4, pos -> PiglinSpecificSensor.isPiglinRepellent(world, pos));
    }

    private static boolean isPiglinRepellent(ServerWorld world, BlockPos pos) {
        BlockState lv = world.getBlockState(pos);
        boolean bl = lv.isIn(BlockTags.PIGLIN_REPELLENTS);
        if (bl && lv.isOf(Blocks.SOUL_CAMPFIRE)) {
            return CampfireBlock.isLitCampfire(lv);
        }
        return bl;
    }
}

