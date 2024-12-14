/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 * 
 * Could not load the following classes:
 *  org.jetbrains.annotations.Nullable
 */
package net.minecraft.util.shape;

import com.google.common.collect.Lists;
import com.google.common.math.DoubleMath;
import it.unimi.dsi.fastutil.doubles.DoubleList;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import net.minecraft.util.Util;
import net.minecraft.util.function.BooleanBiFunction;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.AxisCycleDirection;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.shape.ArrayVoxelShape;
import net.minecraft.util.shape.OffsetDoubleList;
import net.minecraft.util.shape.SlicedVoxelShape;
import net.minecraft.util.shape.VoxelSet;
import net.minecraft.util.shape.VoxelShapes;
import org.jetbrains.annotations.Nullable;

public abstract class VoxelShape {
    protected final VoxelSet voxels;
    @Nullable
    private VoxelShape[] shapeCache;

    protected VoxelShape(VoxelSet voxels) {
        this.voxels = voxels;
    }

    public double getMin(Direction.Axis axis) {
        int i = this.voxels.getMin(axis);
        if (i >= this.voxels.getSize(axis)) {
            return Double.POSITIVE_INFINITY;
        }
        return this.getPointPosition(axis, i);
    }

    public double getMax(Direction.Axis axis) {
        int i = this.voxels.getMax(axis);
        if (i <= 0) {
            return Double.NEGATIVE_INFINITY;
        }
        return this.getPointPosition(axis, i);
    }

    public Box getBoundingBox() {
        if (this.isEmpty()) {
            throw Util.throwOrPause(new UnsupportedOperationException("No bounds for empty shape."));
        }
        return new Box(this.getMin(Direction.Axis.X), this.getMin(Direction.Axis.Y), this.getMin(Direction.Axis.Z), this.getMax(Direction.Axis.X), this.getMax(Direction.Axis.Y), this.getMax(Direction.Axis.Z));
    }

    public VoxelShape asCuboid() {
        if (this.isEmpty()) {
            return VoxelShapes.empty();
        }
        return VoxelShapes.cuboid(this.getMin(Direction.Axis.X), this.getMin(Direction.Axis.Y), this.getMin(Direction.Axis.Z), this.getMax(Direction.Axis.X), this.getMax(Direction.Axis.Y), this.getMax(Direction.Axis.Z));
    }

    protected double getPointPosition(Direction.Axis axis, int index) {
        return this.getPointPositions(axis).getDouble(index);
    }

    public abstract DoubleList getPointPositions(Direction.Axis var1);

    public boolean isEmpty() {
        return this.voxels.isEmpty();
    }

    public VoxelShape offset(double x, double y, double z) {
        if (this.isEmpty()) {
            return VoxelShapes.empty();
        }
        return new ArrayVoxelShape(this.voxels, new OffsetDoubleList(this.getPointPositions(Direction.Axis.X), x), new OffsetDoubleList(this.getPointPositions(Direction.Axis.Y), y), new OffsetDoubleList(this.getPointPositions(Direction.Axis.Z), z));
    }

    public VoxelShape simplify() {
        VoxelShape[] lvs = new VoxelShape[]{VoxelShapes.empty()};
        this.forEachBox((minX, minY, minZ, maxX, maxY, maxZ) -> {
            args[0] = VoxelShapes.combine(lvs[0], VoxelShapes.cuboid(minX, minY, minZ, maxX, maxY, maxZ), BooleanBiFunction.OR);
        });
        return lvs[0];
    }

    public void forEachEdge(VoxelShapes.BoxConsumer consumer) {
        this.voxels.forEachEdge((minX, minY, minZ, maxX, maxY, maxZ) -> consumer.consume(this.getPointPosition(Direction.Axis.X, minX), this.getPointPosition(Direction.Axis.Y, minY), this.getPointPosition(Direction.Axis.Z, minZ), this.getPointPosition(Direction.Axis.X, maxX), this.getPointPosition(Direction.Axis.Y, maxY), this.getPointPosition(Direction.Axis.Z, maxZ)), true);
    }

    public void forEachBox(VoxelShapes.BoxConsumer consumer) {
        DoubleList doubleList = this.getPointPositions(Direction.Axis.X);
        DoubleList doubleList2 = this.getPointPositions(Direction.Axis.Y);
        DoubleList doubleList3 = this.getPointPositions(Direction.Axis.Z);
        this.voxels.forEachBox((minX, minY, minZ, maxX, maxY, maxZ) -> consumer.consume(doubleList.getDouble(minX), doubleList2.getDouble(minY), doubleList3.getDouble(minZ), doubleList.getDouble(maxX), doubleList2.getDouble(maxY), doubleList3.getDouble(maxZ)), true);
    }

    public List<Box> getBoundingBoxes() {
        ArrayList<Box> list = Lists.newArrayList();
        this.forEachBox((x1, y1, z1, x2, y2, z2) -> list.add(new Box(x1, y1, z1, x2, y2, z2)));
        return list;
    }

    public double getStartingCoord(Direction.Axis axis, double from, double to) {
        int j;
        Direction.Axis lv = AxisCycleDirection.FORWARD.cycle(axis);
        Direction.Axis lv2 = AxisCycleDirection.BACKWARD.cycle(axis);
        int i = this.getCoordIndex(lv, from);
        int k = this.voxels.getStartingAxisCoord(axis, i, j = this.getCoordIndex(lv2, to));
        if (k >= this.voxels.getSize(axis)) {
            return Double.POSITIVE_INFINITY;
        }
        return this.getPointPosition(axis, k);
    }

    public double getEndingCoord(Direction.Axis axis, double from, double to) {
        int j;
        Direction.Axis lv = AxisCycleDirection.FORWARD.cycle(axis);
        Direction.Axis lv2 = AxisCycleDirection.BACKWARD.cycle(axis);
        int i = this.getCoordIndex(lv, from);
        int k = this.voxels.getEndingAxisCoord(axis, i, j = this.getCoordIndex(lv2, to));
        if (k <= 0) {
            return Double.NEGATIVE_INFINITY;
        }
        return this.getPointPosition(axis, k);
    }

    protected int getCoordIndex(Direction.Axis axis, double coord) {
        return MathHelper.binarySearch(0, this.voxels.getSize(axis) + 1, i -> coord < this.getPointPosition(axis, i)) - 1;
    }

    @Nullable
    public BlockHitResult raycast(Vec3d start, Vec3d end, BlockPos pos) {
        if (this.isEmpty()) {
            return null;
        }
        Vec3d lv = end.subtract(start);
        if (lv.lengthSquared() < 1.0E-7) {
            return null;
        }
        Vec3d lv2 = start.add(lv.multiply(0.001));
        if (this.voxels.inBoundsAndContains(this.getCoordIndex(Direction.Axis.X, lv2.x - (double)pos.getX()), this.getCoordIndex(Direction.Axis.Y, lv2.y - (double)pos.getY()), this.getCoordIndex(Direction.Axis.Z, lv2.z - (double)pos.getZ()))) {
            return new BlockHitResult(lv2, Direction.getFacing(lv.x, lv.y, lv.z).getOpposite(), pos, true);
        }
        return Box.raycast(this.getBoundingBoxes(), start, end, pos);
    }

    public Optional<Vec3d> getClosestPointTo(Vec3d target) {
        if (this.isEmpty()) {
            return Optional.empty();
        }
        Vec3d[] lvs = new Vec3d[1];
        this.forEachBox((minX, minY, minZ, maxX, maxY, maxZ) -> {
            double j = MathHelper.clamp(target.getX(), minX, maxX);
            double k = MathHelper.clamp(target.getY(), minY, maxY);
            double l = MathHelper.clamp(target.getZ(), minZ, maxZ);
            if (lvs[0] == null || target.squaredDistanceTo(j, k, l) < target.squaredDistanceTo(lvs[0])) {
                args[0] = new Vec3d(j, k, l);
            }
        });
        return Optional.of(lvs[0]);
    }

    public VoxelShape getFace(Direction facing) {
        VoxelShape lv;
        if (this.isEmpty() || this == VoxelShapes.fullCube()) {
            return this;
        }
        if (this.shapeCache != null) {
            lv = this.shapeCache[facing.ordinal()];
            if (lv != null) {
                return lv;
            }
        } else {
            this.shapeCache = new VoxelShape[6];
        }
        this.shapeCache[facing.ordinal()] = lv = this.getUncachedFace(facing);
        return lv;
    }

    private VoxelShape getUncachedFace(Direction direction) {
        Direction.Axis lv = direction.getAxis();
        DoubleList doubleList = this.getPointPositions(lv);
        if (doubleList.size() == 2 && DoubleMath.fuzzyEquals(doubleList.getDouble(0), 0.0, 1.0E-7) && DoubleMath.fuzzyEquals(doubleList.getDouble(1), 1.0, 1.0E-7)) {
            return this;
        }
        Direction.AxisDirection lv2 = direction.getDirection();
        int i = this.getCoordIndex(lv, lv2 == Direction.AxisDirection.POSITIVE ? 0.9999999 : 1.0E-7);
        return new SlicedVoxelShape(this, lv, i);
    }

    public double calculateMaxDistance(Direction.Axis axis, Box box, double maxDist) {
        return this.calculateMaxDistance(AxisCycleDirection.between(axis, Direction.Axis.X), box, maxDist);
    }

    protected double calculateMaxDistance(AxisCycleDirection axisCycle, Box box, double maxDist) {
        block11: {
            int n;
            int l;
            double f;
            Direction.Axis lv2;
            AxisCycleDirection lv;
            block10: {
                if (this.isEmpty()) {
                    return maxDist;
                }
                if (Math.abs(maxDist) < 1.0E-7) {
                    return 0.0;
                }
                lv = axisCycle.opposite();
                lv2 = lv.cycle(Direction.Axis.X);
                Direction.Axis lv3 = lv.cycle(Direction.Axis.Y);
                Direction.Axis lv4 = lv.cycle(Direction.Axis.Z);
                double e = box.getMax(lv2);
                f = box.getMin(lv2);
                int i = this.getCoordIndex(lv2, f + 1.0E-7);
                int j = this.getCoordIndex(lv2, e - 1.0E-7);
                int k = Math.max(0, this.getCoordIndex(lv3, box.getMin(lv3) + 1.0E-7));
                l = Math.min(this.voxels.getSize(lv3), this.getCoordIndex(lv3, box.getMax(lv3) - 1.0E-7) + 1);
                int m = Math.max(0, this.getCoordIndex(lv4, box.getMin(lv4) + 1.0E-7));
                n = Math.min(this.voxels.getSize(lv4), this.getCoordIndex(lv4, box.getMax(lv4) - 1.0E-7) + 1);
                int o = this.voxels.getSize(lv2);
                if (!(maxDist > 0.0)) break block10;
                for (int p = j + 1; p < o; ++p) {
                    for (int q = k; q < l; ++q) {
                        for (int r = m; r < n; ++r) {
                            if (!this.voxels.inBoundsAndContains(lv, p, q, r)) continue;
                            double g = this.getPointPosition(lv2, p) - e;
                            if (g >= -1.0E-7) {
                                maxDist = Math.min(maxDist, g);
                            }
                            return maxDist;
                        }
                    }
                }
                break block11;
            }
            if (!(maxDist < 0.0)) break block11;
            for (int p = i - 1; p >= 0; --p) {
                for (int q = k; q < l; ++q) {
                    for (int r = m; r < n; ++r) {
                        if (!this.voxels.inBoundsAndContains(lv, p, q, r)) continue;
                        double g = this.getPointPosition(lv2, p + 1) - f;
                        if (g <= 1.0E-7) {
                            maxDist = Math.max(maxDist, g);
                        }
                        return maxDist;
                    }
                }
            }
        }
        return maxDist;
    }

    public String toString() {
        return this.isEmpty() ? "EMPTY" : "VoxelShape[" + String.valueOf(this.getBoundingBox()) + "]";
    }
}

