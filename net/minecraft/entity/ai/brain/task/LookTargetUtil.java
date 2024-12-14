/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 * 
 * Could not load the following classes:
 *  org.jetbrains.annotations.Nullable
 */
package net.minecraft.entity.ai.brain.task;

import java.util.Comparator;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Predicate;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.NoPenaltyTargeting;
import net.minecraft.entity.ai.brain.BlockPosLookTarget;
import net.minecraft.entity.ai.brain.Brain;
import net.minecraft.entity.ai.brain.EntityLookTarget;
import net.minecraft.entity.ai.brain.LivingTargetCache;
import net.minecraft.entity.ai.brain.LookTarget;
import net.minecraft.entity.ai.brain.MemoryModuleType;
import net.minecraft.entity.ai.brain.WalkTarget;
import net.minecraft.entity.ai.pathing.NavigationType;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.mob.PathAwareEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.RangedWeaponItem;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkSectionPos;
import net.minecraft.util.math.Vec3d;
import org.jetbrains.annotations.Nullable;

public class LookTargetUtil {
    private LookTargetUtil() {
    }

    public static void lookAtAndWalkTowardsEachOther(LivingEntity first, LivingEntity second, float speed, int walkCompletionRange) {
        LookTargetUtil.lookAtEachOther(first, second);
        LookTargetUtil.walkTowardsEachOther(first, second, speed, walkCompletionRange);
    }

    public static boolean canSee(Brain<?> brain, LivingEntity target) {
        Optional<LivingTargetCache> optional = brain.getOptionalRegisteredMemory(MemoryModuleType.VISIBLE_MOBS);
        return optional.isPresent() && optional.get().contains(target);
    }

    public static boolean canSee(Brain<?> brain, MemoryModuleType<? extends LivingEntity> memoryModuleType, EntityType<?> entityType) {
        return LookTargetUtil.canSee(brain, memoryModuleType, (LivingEntity entity) -> entity.getType() == entityType);
    }

    private static boolean canSee(Brain<?> brain, MemoryModuleType<? extends LivingEntity> memoryType, Predicate<LivingEntity> filter) {
        return brain.getOptionalRegisteredMemory(memoryType).filter(filter).filter(LivingEntity::isAlive).filter(target -> LookTargetUtil.canSee(brain, target)).isPresent();
    }

    private static void lookAtEachOther(LivingEntity first, LivingEntity second) {
        LookTargetUtil.lookAt(first, second);
        LookTargetUtil.lookAt(second, first);
    }

    public static void lookAt(LivingEntity entity, LivingEntity target) {
        entity.getBrain().remember(MemoryModuleType.LOOK_TARGET, new EntityLookTarget(target, true));
    }

    private static void walkTowardsEachOther(LivingEntity first, LivingEntity second, float speed, int completionRange) {
        LookTargetUtil.walkTowards(first, second, speed, completionRange);
        LookTargetUtil.walkTowards(second, first, speed, completionRange);
    }

    public static void walkTowards(LivingEntity entity, Entity target, float speed, int completionRange) {
        LookTargetUtil.walkTowards(entity, new EntityLookTarget(target, true), speed, completionRange);
    }

    public static void walkTowards(LivingEntity entity, BlockPos target, float speed, int completionRange) {
        LookTargetUtil.walkTowards(entity, new BlockPosLookTarget(target), speed, completionRange);
    }

    public static void walkTowards(LivingEntity entity, LookTarget target, float speed, int completionRange) {
        WalkTarget lv = new WalkTarget(target, speed, completionRange);
        entity.getBrain().remember(MemoryModuleType.LOOK_TARGET, target);
        entity.getBrain().remember(MemoryModuleType.WALK_TARGET, lv);
    }

    public static void give(LivingEntity entity, ItemStack stack, Vec3d targetLocation) {
        Vec3d lv = new Vec3d(0.3f, 0.3f, 0.3f);
        LookTargetUtil.give(entity, stack, targetLocation, lv, 0.3f);
    }

    public static void give(LivingEntity entity, ItemStack stack, Vec3d targetLocation, Vec3d velocityFactor, float yOffset) {
        double d = entity.getEyeY() - (double)yOffset;
        ItemEntity lv = new ItemEntity(entity.getWorld(), entity.getX(), d, entity.getZ(), stack);
        lv.setThrower(entity);
        Vec3d lv2 = targetLocation.subtract(entity.getPos());
        lv2 = lv2.normalize().multiply(velocityFactor.x, velocityFactor.y, velocityFactor.z);
        lv.setVelocity(lv2);
        lv.setToDefaultPickupDelay();
        entity.getWorld().spawnEntity(lv);
    }

    public static ChunkSectionPos getPosClosestToOccupiedPointOfInterest(ServerWorld world, ChunkSectionPos center, int radius) {
        int j = world.getOccupiedPointOfInterestDistance(center);
        return ChunkSectionPos.stream(center, radius).filter(sectionPos -> world.getOccupiedPointOfInterestDistance((ChunkSectionPos)sectionPos) < j).min(Comparator.comparingInt(world::getOccupiedPointOfInterestDistance)).orElse(center);
    }

    public static boolean isTargetWithinAttackRange(MobEntity mob, LivingEntity target, int rangedWeaponReachReduction) {
        RangedWeaponItem lv;
        Item item = mob.getMainHandStack().getItem();
        if (item instanceof RangedWeaponItem && mob.canUseRangedWeapon(lv = (RangedWeaponItem)item)) {
            int j = lv.getRange() - rangedWeaponReachReduction;
            return mob.isInRange(target, j);
        }
        return mob.isInAttackRange(target);
    }

    public static boolean isNewTargetTooFar(LivingEntity source, LivingEntity target, double extraDistance) {
        Optional<LivingEntity> optional = source.getBrain().getOptionalRegisteredMemory(MemoryModuleType.ATTACK_TARGET);
        if (optional.isEmpty()) {
            return false;
        }
        double e = source.squaredDistanceTo(optional.get().getPos());
        double f = source.squaredDistanceTo(target.getPos());
        return f > e + extraDistance * extraDistance;
    }

    public static boolean isVisibleInMemory(LivingEntity source, LivingEntity target) {
        Brain<LivingTargetCache> lv = source.getBrain();
        if (!lv.hasMemoryModule(MemoryModuleType.VISIBLE_MOBS)) {
            return false;
        }
        return lv.getOptionalRegisteredMemory(MemoryModuleType.VISIBLE_MOBS).get().contains(target);
    }

    public static LivingEntity getCloserEntity(LivingEntity source, Optional<LivingEntity> first, LivingEntity second) {
        if (first.isEmpty()) {
            return second;
        }
        return LookTargetUtil.getCloserEntity(source, first.get(), second);
    }

    public static LivingEntity getCloserEntity(LivingEntity source, LivingEntity first, LivingEntity second) {
        Vec3d lv = first.getPos();
        Vec3d lv2 = second.getPos();
        return source.squaredDistanceTo(lv) < source.squaredDistanceTo(lv2) ? first : second;
    }

    public static Optional<LivingEntity> getEntity(LivingEntity entity, MemoryModuleType<UUID> uuidMemoryModule) {
        Optional<UUID> optional = entity.getBrain().getOptionalRegisteredMemory(uuidMemoryModule);
        return optional.map(uuid -> ((ServerWorld)entity.getWorld()).getEntity((UUID)uuid)).map(target -> {
            LivingEntity lv;
            return target instanceof LivingEntity ? (lv = (LivingEntity)target) : null;
        });
    }

    @Nullable
    public static Vec3d find(PathAwareEntity entity, int horizontalRange, int verticalRange) {
        Vec3d lv = NoPenaltyTargeting.find(entity, horizontalRange, verticalRange);
        int k = 0;
        while (lv != null && !entity.getWorld().getBlockState(BlockPos.ofFloored(lv)).canPathfindThrough(NavigationType.WATER) && k++ < 10) {
            lv = NoPenaltyTargeting.find(entity, horizontalRange, verticalRange);
        }
        return lv;
    }

    public static boolean hasBreedTarget(LivingEntity entity) {
        return entity.getBrain().hasMemoryModule(MemoryModuleType.BREED_TARGET);
    }
}

