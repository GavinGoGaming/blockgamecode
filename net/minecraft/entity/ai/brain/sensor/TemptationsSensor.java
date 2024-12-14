/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.entity.ai.brain.sensor;

import com.google.common.collect.ImmutableSet;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.TargetPredicate;
import net.minecraft.entity.ai.brain.Brain;
import net.minecraft.entity.ai.brain.MemoryModuleType;
import net.minecraft.entity.ai.brain.sensor.Sensor;
import net.minecraft.entity.mob.PathAwareEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.predicate.entity.EntityPredicates;
import net.minecraft.server.world.ServerWorld;

public class TemptationsSensor
extends Sensor<PathAwareEntity> {
    public static final int MAX_DISTANCE = 10;
    private static final TargetPredicate TEMPTER_PREDICATE = TargetPredicate.createNonAttackable().setBaseMaxDistance(10.0).ignoreVisibility();
    private final Predicate<ItemStack> predicate;

    public TemptationsSensor(Predicate<ItemStack> predicate) {
        this.predicate = predicate;
    }

    @Override
    protected void sense(ServerWorld arg, PathAwareEntity arg22) {
        Brain<?> lv = arg22.getBrain();
        List list = arg.getPlayers().stream().filter(EntityPredicates.EXCEPT_SPECTATOR).filter(player -> TEMPTER_PREDICATE.test(arg22, (LivingEntity)player)).filter(player -> arg22.isInRange((Entity)player, 10.0)).filter(this::test).filter(arg2 -> !arg22.hasPassenger((Entity)arg2)).sorted(Comparator.comparingDouble(arg22::squaredDistanceTo)).collect(Collectors.toList());
        if (!list.isEmpty()) {
            PlayerEntity lv2 = (PlayerEntity)list.get(0);
            lv.remember(MemoryModuleType.TEMPTING_PLAYER, lv2);
        } else {
            lv.forget(MemoryModuleType.TEMPTING_PLAYER);
        }
    }

    private boolean test(PlayerEntity player) {
        return this.test(player.getMainHandStack()) || this.test(player.getOffHandStack());
    }

    private boolean test(ItemStack stack) {
        return this.predicate.test(stack);
    }

    @Override
    public Set<MemoryModuleType<?>> getOutputMemoryModules() {
        return ImmutableSet.of(MemoryModuleType.TEMPTING_PLAYER);
    }
}

