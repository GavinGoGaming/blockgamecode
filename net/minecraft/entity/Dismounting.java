/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 * 
 * Could not load the following classes:
 *  org.jetbrains.annotations.Nullable
 */
package net.minecraft.entity;

import java.util.function.Function;
import net.minecraft.block.BlockState;
import net.minecraft.block.TrapdoorBlock;
import net.minecraft.entity.EntityPose;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.registry.tag.BlockTags;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.util.shape.VoxelShapes;
import net.minecraft.world.BlockView;
import net.minecraft.world.CollisionView;
import org.jetbrains.annotations.Nullable;

public class Dismounting {
    public static int[][] getDismountOffsets(Direction movementDirection) {
        Direction lv = movementDirection.rotateYClockwise();
        Direction lv2 = lv.getOpposite();
        Direction lv3 = movementDirection.getOpposite();
        return new int[][]{{lv.getOffsetX(), lv.getOffsetZ()}, {lv2.getOffsetX(), lv2.getOffsetZ()}, {lv3.getOffsetX() + lv.getOffsetX(), lv3.getOffsetZ() + lv.getOffsetZ()}, {lv3.getOffsetX() + lv2.getOffsetX(), lv3.getOffsetZ() + lv2.getOffsetZ()}, {movementDirection.getOffsetX() + lv.getOffsetX(), movementDirection.getOffsetZ() + lv.getOffsetZ()}, {movementDirection.getOffsetX() + lv2.getOffsetX(), movementDirection.getOffsetZ() + lv2.getOffsetZ()}, {lv3.getOffsetX(), lv3.getOffsetZ()}, {movementDirection.getOffsetX(), movementDirection.getOffsetZ()}};
    }

    public static boolean canDismountInBlock(double height) {
        return !Double.isInfinite(height) && height < 1.0;
    }

    public static boolean canPlaceEntityAt(CollisionView world, LivingEntity entity, Box targetBox) {
        Iterable<VoxelShape> iterable = world.getBlockCollisions(entity, targetBox);
        for (VoxelShape lv : iterable) {
            if (lv.isEmpty()) continue;
            return false;
        }
        return world.getWorldBorder().contains(targetBox);
    }

    public static boolean canPlaceEntityAt(CollisionView world, Vec3d offset, LivingEntity entity, EntityPose pose) {
        return Dismounting.canPlaceEntityAt(world, entity, entity.getBoundingBox(pose).offset(offset));
    }

    public static VoxelShape getCollisionShape(BlockView world, BlockPos pos) {
        BlockState lv = world.getBlockState(pos);
        if (lv.isIn(BlockTags.CLIMBABLE) || lv.getBlock() instanceof TrapdoorBlock && lv.get(TrapdoorBlock.OPEN).booleanValue()) {
            return VoxelShapes.empty();
        }
        return lv.getCollisionShape(world, pos);
    }

    public static double getCeilingHeight(BlockPos pos, int maxDistance, Function<BlockPos, VoxelShape> collisionShapeGetter) {
        BlockPos.Mutable lv = pos.mutableCopy();
        for (int j = 0; j < maxDistance; ++j) {
            VoxelShape lv2 = collisionShapeGetter.apply(lv);
            if (!lv2.isEmpty()) {
                return (double)(pos.getY() + j) + lv2.getMin(Direction.Axis.Y);
            }
            lv.move(Direction.UP);
        }
        return Double.POSITIVE_INFINITY;
    }

    @Nullable
    public static Vec3d findRespawnPos(EntityType<?> entityType, CollisionView world, BlockPos pos, boolean ignoreInvalidPos) {
        if (ignoreInvalidPos && entityType.isInvalidSpawn(world.getBlockState(pos))) {
            return null;
        }
        double d = world.getDismountHeight(Dismounting.getCollisionShape(world, pos), () -> Dismounting.getCollisionShape(world, pos.down()));
        if (!Dismounting.canDismountInBlock(d)) {
            return null;
        }
        if (ignoreInvalidPos && d <= 0.0 && entityType.isInvalidSpawn(world.getBlockState(pos.down()))) {
            return null;
        }
        Vec3d lv = Vec3d.ofCenter(pos, d);
        Box lv2 = entityType.getDimensions().getBoxAt(lv);
        Iterable<VoxelShape> iterable = world.getBlockCollisions(null, lv2);
        for (VoxelShape lv3 : iterable) {
            if (lv3.isEmpty()) continue;
            return null;
        }
        if (entityType == EntityType.PLAYER && (world.getBlockState(pos).isIn(BlockTags.INVALID_SPAWN_INSIDE) || world.getBlockState(pos.up()).isIn(BlockTags.INVALID_SPAWN_INSIDE))) {
            return null;
        }
        if (!world.getWorldBorder().contains(lv2)) {
            return null;
        }
        return lv;
    }
}

