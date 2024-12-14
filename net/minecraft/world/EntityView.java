/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 * 
 * Could not load the following classes:
 *  org.jetbrains.annotations.Nullable
 */
package net.minecraft.world;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.function.Predicate;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.TargetPredicate;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.predicate.entity.EntityPredicates;
import net.minecraft.util.TypeFilter;
import net.minecraft.util.function.BooleanBiFunction;
import net.minecraft.util.math.Box;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.util.shape.VoxelShapes;
import org.jetbrains.annotations.Nullable;

public interface EntityView {
    public List<Entity> getOtherEntities(@Nullable Entity var1, Box var2, Predicate<? super Entity> var3);

    public <T extends Entity> List<T> getEntitiesByType(TypeFilter<Entity, T> var1, Box var2, Predicate<? super T> var3);

    default public <T extends Entity> List<T> getEntitiesByClass(Class<T> entityClass, Box box, Predicate<? super T> predicate) {
        return this.getEntitiesByType(TypeFilter.instanceOf(entityClass), box, predicate);
    }

    public List<? extends PlayerEntity> getPlayers();

    default public List<Entity> getOtherEntities(@Nullable Entity except, Box box) {
        return this.getOtherEntities(except, box, EntityPredicates.EXCEPT_SPECTATOR);
    }

    default public boolean doesNotIntersectEntities(@Nullable Entity except, VoxelShape shape) {
        if (shape.isEmpty()) {
            return true;
        }
        for (Entity lv : this.getOtherEntities(except, shape.getBoundingBox())) {
            if (lv.isRemoved() || !lv.intersectionChecked || except != null && lv.isConnectedThroughVehicle(except) || !VoxelShapes.matchesAnywhere(shape, VoxelShapes.cuboid(lv.getBoundingBox()), BooleanBiFunction.AND)) continue;
            return false;
        }
        return true;
    }

    default public <T extends Entity> List<T> getNonSpectatingEntities(Class<T> entityClass, Box box) {
        return this.getEntitiesByClass(entityClass, box, EntityPredicates.EXCEPT_SPECTATOR);
    }

    default public List<VoxelShape> getEntityCollisions(@Nullable Entity entity, Box box) {
        if (box.getAverageSideLength() < 1.0E-7) {
            return List.of();
        }
        Predicate<Entity> predicate = entity == null ? EntityPredicates.CAN_COLLIDE : EntityPredicates.EXCEPT_SPECTATOR.and(entity::collidesWith);
        List<Entity> list = this.getOtherEntities(entity, box.expand(1.0E-7), predicate);
        if (list.isEmpty()) {
            return List.of();
        }
        ImmutableList.Builder builder = ImmutableList.builderWithExpectedSize(list.size());
        for (Entity lv : list) {
            builder.add(VoxelShapes.cuboid(lv.getBoundingBox()));
        }
        return builder.build();
    }

    @Nullable
    default public PlayerEntity getClosestPlayer(double x, double y, double z, double maxDistance, @Nullable Predicate<Entity> targetPredicate) {
        double h = -1.0;
        PlayerEntity lv = null;
        for (PlayerEntity playerEntity : this.getPlayers()) {
            if (targetPredicate != null && !targetPredicate.test(playerEntity)) continue;
            double i = playerEntity.squaredDistanceTo(x, y, z);
            if (!(maxDistance < 0.0) && !(i < maxDistance * maxDistance) || h != -1.0 && !(i < h)) continue;
            h = i;
            lv = playerEntity;
        }
        return lv;
    }

    @Nullable
    default public PlayerEntity getClosestPlayer(Entity entity, double maxDistance) {
        return this.getClosestPlayer(entity.getX(), entity.getY(), entity.getZ(), maxDistance, false);
    }

    @Nullable
    default public PlayerEntity getClosestPlayer(double x, double y, double z, double maxDistance, boolean ignoreCreative) {
        Predicate<Entity> predicate = ignoreCreative ? EntityPredicates.EXCEPT_CREATIVE_OR_SPECTATOR : EntityPredicates.EXCEPT_SPECTATOR;
        return this.getClosestPlayer(x, y, z, maxDistance, predicate);
    }

    default public boolean isPlayerInRange(double x, double y, double z, double range) {
        for (PlayerEntity playerEntity : this.getPlayers()) {
            if (!EntityPredicates.EXCEPT_SPECTATOR.test(playerEntity) || !EntityPredicates.VALID_LIVING_ENTITY.test(playerEntity)) continue;
            double h = playerEntity.squaredDistanceTo(x, y, z);
            if (!(range < 0.0) && !(h < range * range)) continue;
            return true;
        }
        return false;
    }

    @Nullable
    default public PlayerEntity getClosestPlayer(TargetPredicate targetPredicate, LivingEntity entity) {
        return this.getClosestEntity(this.getPlayers(), targetPredicate, entity, entity.getX(), entity.getY(), entity.getZ());
    }

    @Nullable
    default public PlayerEntity getClosestPlayer(TargetPredicate targetPredicate, LivingEntity entity, double x, double y, double z) {
        return this.getClosestEntity(this.getPlayers(), targetPredicate, entity, x, y, z);
    }

    @Nullable
    default public PlayerEntity getClosestPlayer(TargetPredicate targetPredicate, double x, double y, double z) {
        return this.getClosestEntity(this.getPlayers(), targetPredicate, null, x, y, z);
    }

    @Nullable
    default public <T extends LivingEntity> T getClosestEntity(Class<? extends T> entityClass, TargetPredicate targetPredicate, @Nullable LivingEntity entity, double x, double y, double z, Box box) {
        return (T)this.getClosestEntity(this.getEntitiesByClass(entityClass, box, entityOfClass -> true), targetPredicate, entity, x, y, z);
    }

    @Nullable
    default public <T extends LivingEntity> T getClosestEntity(List<? extends T> entityList, TargetPredicate targetPredicate, @Nullable LivingEntity entity, double x, double y, double z) {
        double g = -1.0;
        LivingEntity lv = null;
        for (LivingEntity lv2 : entityList) {
            if (!targetPredicate.test(entity, lv2)) continue;
            double h = lv2.squaredDistanceTo(x, y, z);
            if (g != -1.0 && !(h < g)) continue;
            g = h;
            lv = lv2;
        }
        return (T)lv;
    }

    default public List<PlayerEntity> getPlayers(TargetPredicate targetPredicate, LivingEntity entity, Box box) {
        ArrayList<PlayerEntity> list = Lists.newArrayList();
        for (PlayerEntity playerEntity : this.getPlayers()) {
            if (!box.contains(playerEntity.getX(), playerEntity.getY(), playerEntity.getZ()) || !targetPredicate.test(entity, playerEntity)) continue;
            list.add(playerEntity);
        }
        return list;
    }

    default public <T extends LivingEntity> List<T> getTargets(Class<T> entityClass, TargetPredicate targetPredicate, LivingEntity targetingEntity, Box box) {
        List<LivingEntity> list = this.getEntitiesByClass(entityClass, box, arg -> true);
        ArrayList<LivingEntity> list2 = Lists.newArrayList();
        for (LivingEntity lv : list) {
            if (!targetPredicate.test(targetingEntity, lv)) continue;
            list2.add(lv);
        }
        return list2;
    }

    @Nullable
    default public PlayerEntity getPlayerByUuid(UUID uuid) {
        for (int i = 0; i < this.getPlayers().size(); ++i) {
            PlayerEntity lv = this.getPlayers().get(i);
            if (!uuid.equals(lv.getUuid())) continue;
            return lv;
        }
        return null;
    }
}

