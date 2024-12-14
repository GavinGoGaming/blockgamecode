/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 * 
 * Could not load the following classes:
 *  org.jetbrains.annotations.Nullable
 */
package net.minecraft.world;

import java.util.Optional;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Stream;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.fluid.FluidState;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.world.BlockStateRaycastContext;
import net.minecraft.world.HeightLimitView;
import net.minecraft.world.RaycastContext;
import org.jetbrains.annotations.Nullable;

public interface BlockView
extends HeightLimitView {
    @Nullable
    public BlockEntity getBlockEntity(BlockPos var1);

    default public <T extends BlockEntity> Optional<T> getBlockEntity(BlockPos pos, BlockEntityType<T> type) {
        BlockEntity lv = this.getBlockEntity(pos);
        if (lv == null || lv.getType() != type) {
            return Optional.empty();
        }
        return Optional.of(lv);
    }

    public BlockState getBlockState(BlockPos var1);

    public FluidState getFluidState(BlockPos var1);

    default public int getLuminance(BlockPos pos) {
        return this.getBlockState(pos).getLuminance();
    }

    default public int getMaxLightLevel() {
        return 15;
    }

    default public Stream<BlockState> getStatesInBox(Box box) {
        return BlockPos.stream(box).map(this::getBlockState);
    }

    default public BlockHitResult raycast(BlockStateRaycastContext context) {
        return BlockView.raycast(context.getStart(), context.getEnd(), context, (innerContext, pos) -> {
            BlockState lv = this.getBlockState((BlockPos)pos);
            Vec3d lv2 = innerContext.getStart().subtract(innerContext.getEnd());
            return innerContext.getStatePredicate().test(lv) ? new BlockHitResult(innerContext.getEnd(), Direction.getFacing(lv2.x, lv2.y, lv2.z), BlockPos.ofFloored(innerContext.getEnd()), false) : null;
        }, innerContext -> {
            Vec3d lv = innerContext.getStart().subtract(innerContext.getEnd());
            return BlockHitResult.createMissed(innerContext.getEnd(), Direction.getFacing(lv.x, lv.y, lv.z), BlockPos.ofFloored(innerContext.getEnd()));
        });
    }

    default public BlockHitResult raycast(RaycastContext context) {
        return BlockView.raycast(context.getStart(), context.getEnd(), context, (innerContext, pos) -> {
            BlockState lv = this.getBlockState((BlockPos)pos);
            FluidState lv2 = this.getFluidState((BlockPos)pos);
            Vec3d lv3 = innerContext.getStart();
            Vec3d lv4 = innerContext.getEnd();
            VoxelShape lv5 = innerContext.getBlockShape(lv, this, (BlockPos)pos);
            BlockHitResult lv6 = this.raycastBlock(lv3, lv4, (BlockPos)pos, lv5, lv);
            VoxelShape lv7 = innerContext.getFluidShape(lv2, this, (BlockPos)pos);
            BlockHitResult lv8 = lv7.raycast(lv3, lv4, (BlockPos)pos);
            double d = lv6 == null ? Double.MAX_VALUE : innerContext.getStart().squaredDistanceTo(lv6.getPos());
            double e = lv8 == null ? Double.MAX_VALUE : innerContext.getStart().squaredDistanceTo(lv8.getPos());
            return d <= e ? lv6 : lv8;
        }, innerContext -> {
            Vec3d lv = innerContext.getStart().subtract(innerContext.getEnd());
            return BlockHitResult.createMissed(innerContext.getEnd(), Direction.getFacing(lv.x, lv.y, lv.z), BlockPos.ofFloored(innerContext.getEnd()));
        });
    }

    @Nullable
    default public BlockHitResult raycastBlock(Vec3d start, Vec3d end, BlockPos pos, VoxelShape shape, BlockState state) {
        BlockHitResult lv2;
        BlockHitResult lv = shape.raycast(start, end, pos);
        if (lv != null && (lv2 = state.getRaycastShape(this, pos).raycast(start, end, pos)) != null && lv2.getPos().subtract(start).lengthSquared() < lv.getPos().subtract(start).lengthSquared()) {
            return lv.withSide(lv2.getSide());
        }
        return lv;
    }

    default public double getDismountHeight(VoxelShape blockCollisionShape, Supplier<VoxelShape> belowBlockCollisionShapeGetter) {
        if (!blockCollisionShape.isEmpty()) {
            return blockCollisionShape.getMax(Direction.Axis.Y);
        }
        double d = belowBlockCollisionShapeGetter.get().getMax(Direction.Axis.Y);
        if (d >= 1.0) {
            return d - 1.0;
        }
        return Double.NEGATIVE_INFINITY;
    }

    default public double getDismountHeight(BlockPos pos) {
        return this.getDismountHeight(this.getBlockState(pos).getCollisionShape(this, pos), () -> {
            BlockPos lv = pos.down();
            return this.getBlockState(lv).getCollisionShape(this, lv);
        });
    }

    public static <T, C> T raycast(Vec3d start, Vec3d end, C context, BiFunction<C, BlockPos, T> blockHitFactory, Function<C, T> missFactory) {
        int l;
        int k;
        if (start.equals(end)) {
            return missFactory.apply(context);
        }
        double d = MathHelper.lerp(-1.0E-7, end.x, start.x);
        double e = MathHelper.lerp(-1.0E-7, end.y, start.y);
        double f = MathHelper.lerp(-1.0E-7, end.z, start.z);
        double g = MathHelper.lerp(-1.0E-7, start.x, end.x);
        double h = MathHelper.lerp(-1.0E-7, start.y, end.y);
        double i = MathHelper.lerp(-1.0E-7, start.z, end.z);
        int j = MathHelper.floor(g);
        BlockPos.Mutable lv = new BlockPos.Mutable(j, k = MathHelper.floor(h), l = MathHelper.floor(i));
        T object2 = blockHitFactory.apply(context, lv);
        if (object2 != null) {
            return object2;
        }
        double m = d - g;
        double n = e - h;
        double o = f - i;
        int p = MathHelper.sign(m);
        int q = MathHelper.sign(n);
        int r = MathHelper.sign(o);
        double s = p == 0 ? Double.MAX_VALUE : (double)p / m;
        double t = q == 0 ? Double.MAX_VALUE : (double)q / n;
        double u = r == 0 ? Double.MAX_VALUE : (double)r / o;
        double v = s * (p > 0 ? 1.0 - MathHelper.fractionalPart(g) : MathHelper.fractionalPart(g));
        double w = t * (q > 0 ? 1.0 - MathHelper.fractionalPart(h) : MathHelper.fractionalPart(h));
        double x = u * (r > 0 ? 1.0 - MathHelper.fractionalPart(i) : MathHelper.fractionalPart(i));
        while (v <= 1.0 || w <= 1.0 || x <= 1.0) {
            T object3;
            if (v < w) {
                if (v < x) {
                    j += p;
                    v += s;
                } else {
                    l += r;
                    x += u;
                }
            } else if (w < x) {
                k += q;
                w += t;
            } else {
                l += r;
                x += u;
            }
            if ((object3 = blockHitFactory.apply(context, lv.set(j, k, l))) == null) continue;
            return object3;
        }
        return missFactory.apply(context);
    }
}

