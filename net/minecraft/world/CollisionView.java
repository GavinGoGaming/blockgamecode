/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 * 
 * Could not load the following classes:
 *  org.jetbrains.annotations.Nullable
 */
package net.minecraft.world;

import com.google.common.collect.Iterables;
import java.util.List;
import java.util.Optional;
import java.util.stream.StreamSupport;
import net.minecraft.block.BlockState;
import net.minecraft.block.ShapeContext;
import net.minecraft.entity.Entity;
import net.minecraft.util.function.BooleanBiFunction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.util.shape.VoxelShapes;
import net.minecraft.world.BlockCollisionSpliterator;
import net.minecraft.world.BlockView;
import net.minecraft.world.border.WorldBorder;
import org.jetbrains.annotations.Nullable;

public interface CollisionView
extends BlockView {
    public WorldBorder getWorldBorder();

    @Nullable
    public BlockView getChunkAsView(int var1, int var2);

    default public boolean doesNotIntersectEntities(@Nullable Entity except, VoxelShape shape) {
        return true;
    }

    default public boolean canPlace(BlockState state, BlockPos pos, ShapeContext context) {
        VoxelShape lv = state.getCollisionShape(this, pos, context);
        return lv.isEmpty() || this.doesNotIntersectEntities(null, lv.offset(pos.getX(), pos.getY(), pos.getZ()));
    }

    default public boolean doesNotIntersectEntities(Entity entity) {
        return this.doesNotIntersectEntities(entity, VoxelShapes.cuboid(entity.getBoundingBox()));
    }

    default public boolean isSpaceEmpty(Box box) {
        return this.isSpaceEmpty(null, box);
    }

    default public boolean isSpaceEmpty(Entity entity) {
        return this.isSpaceEmpty(entity, entity.getBoundingBox());
    }

    default public boolean isSpaceEmpty(@Nullable Entity entity, Box box) {
        for (VoxelShape lv : this.getBlockCollisions(entity, box)) {
            if (lv.isEmpty()) continue;
            return false;
        }
        if (!this.getEntityCollisions(entity, box).isEmpty()) {
            return false;
        }
        if (entity != null) {
            VoxelShape lv2 = this.getWorldBorderCollisions(entity, box);
            return lv2 == null || !VoxelShapes.matchesAnywhere(lv2, VoxelShapes.cuboid(box), BooleanBiFunction.AND);
        }
        return true;
    }

    default public boolean isBlockSpaceEmpty(@Nullable Entity entity, Box box) {
        for (VoxelShape lv : this.getBlockCollisions(entity, box)) {
            if (lv.isEmpty()) continue;
            return false;
        }
        return true;
    }

    public List<VoxelShape> getEntityCollisions(@Nullable Entity var1, Box var2);

    default public Iterable<VoxelShape> getCollisions(@Nullable Entity entity, Box box) {
        List<VoxelShape> list = this.getEntityCollisions(entity, box);
        Iterable<VoxelShape> iterable = this.getBlockCollisions(entity, box);
        return list.isEmpty() ? iterable : Iterables.concat(list, iterable);
    }

    default public Iterable<VoxelShape> getBlockCollisions(@Nullable Entity entity, Box box) {
        return () -> new BlockCollisionSpliterator<VoxelShape>(this, entity, box, false, (pos, voxelShape) -> voxelShape);
    }

    @Nullable
    private VoxelShape getWorldBorderCollisions(Entity entity, Box box) {
        WorldBorder lv = this.getWorldBorder();
        return lv.canCollide(entity, box) ? lv.asVoxelShape() : null;
    }

    default public boolean canCollide(@Nullable Entity entity, Box box) {
        BlockCollisionSpliterator<VoxelShape> lv = new BlockCollisionSpliterator<VoxelShape>(this, entity, box, true, (pos, voxelShape) -> voxelShape);
        while (lv.hasNext()) {
            if (((VoxelShape)lv.next()).isEmpty()) continue;
            return true;
        }
        return false;
    }

    default public Optional<BlockPos> findSupportingBlockPos(Entity entity, Box box) {
        BlockPos lv = null;
        double d = Double.MAX_VALUE;
        BlockCollisionSpliterator<BlockPos> lv2 = new BlockCollisionSpliterator<BlockPos>(this, entity, box, false, (pos, voxelShape) -> pos);
        while (lv2.hasNext()) {
            BlockPos lv3 = (BlockPos)lv2.next();
            double e = lv3.getSquaredDistance(entity.getPos());
            if (!(e < d) && (e != d || lv != null && lv.compareTo(lv3) >= 0)) continue;
            lv = lv3.toImmutable();
            d = e;
        }
        return Optional.ofNullable(lv);
    }

    default public Optional<Vec3d> findClosestCollision(@Nullable Entity entity, VoxelShape shape, Vec3d target, double x, double y, double z) {
        if (shape.isEmpty()) {
            return Optional.empty();
        }
        Box lv = shape.getBoundingBox().expand(x, y, z);
        VoxelShape lv2 = StreamSupport.stream(this.getBlockCollisions(entity, lv).spliterator(), false).filter(arg -> this.getWorldBorder() == null || this.getWorldBorder().contains(arg.getBoundingBox())).flatMap(arg -> arg.getBoundingBoxes().stream()).map(arg -> arg.expand(x / 2.0, y / 2.0, z / 2.0)).map(VoxelShapes::cuboid).reduce(VoxelShapes.empty(), VoxelShapes::union);
        VoxelShape lv3 = VoxelShapes.combineAndSimplify(shape, lv2, BooleanBiFunction.ONLY_FIRST);
        return lv3.getClosestPointTo(target);
    }
}

