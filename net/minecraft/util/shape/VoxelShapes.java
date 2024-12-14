/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.util.shape;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.math.DoubleMath;
import com.google.common.math.IntMath;
import it.unimi.dsi.fastutil.doubles.DoubleArrayList;
import it.unimi.dsi.fastutil.doubles.DoubleList;
import java.util.Arrays;
import java.util.Objects;
import net.minecraft.util.Util;
import net.minecraft.util.function.BooleanBiFunction;
import net.minecraft.util.math.AxisCycleDirection;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Direction;
import net.minecraft.util.shape.ArrayVoxelShape;
import net.minecraft.util.shape.BitSetVoxelSet;
import net.minecraft.util.shape.DisjointPairList;
import net.minecraft.util.shape.FractionalDoubleList;
import net.minecraft.util.shape.FractionalPairList;
import net.minecraft.util.shape.IdentityPairList;
import net.minecraft.util.shape.PairList;
import net.minecraft.util.shape.SimplePairList;
import net.minecraft.util.shape.SimpleVoxelShape;
import net.minecraft.util.shape.SlicedVoxelShape;
import net.minecraft.util.shape.VoxelSet;
import net.minecraft.util.shape.VoxelShape;

public final class VoxelShapes {
    public static final double MIN_SIZE = 1.0E-7;
    public static final double field_31881 = 1.0E-6;
    private static final VoxelShape FULL_CUBE = Util.make(() -> {
        BitSetVoxelSet lv = new BitSetVoxelSet(1, 1, 1);
        ((VoxelSet)lv).set(0, 0, 0);
        return new SimpleVoxelShape(lv);
    });
    public static final VoxelShape UNBOUNDED = VoxelShapes.cuboid(Double.NEGATIVE_INFINITY, Double.NEGATIVE_INFINITY, Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY, Double.POSITIVE_INFINITY, Double.POSITIVE_INFINITY);
    private static final VoxelShape EMPTY = new ArrayVoxelShape((VoxelSet)new BitSetVoxelSet(0, 0, 0), new DoubleArrayList(new double[]{0.0}), new DoubleArrayList(new double[]{0.0}), new DoubleArrayList(new double[]{0.0}));

    public static VoxelShape empty() {
        return EMPTY;
    }

    public static VoxelShape fullCube() {
        return FULL_CUBE;
    }

    public static VoxelShape cuboid(double minX, double minY, double minZ, double maxX, double maxY, double maxZ) {
        if (minX > maxX || minY > maxY || minZ > maxZ) {
            throw new IllegalArgumentException("The min values need to be smaller or equals to the max values");
        }
        return VoxelShapes.cuboidUnchecked(minX, minY, minZ, maxX, maxY, maxZ);
    }

    public static VoxelShape cuboidUnchecked(double minX, double minY, double minZ, double maxX, double maxY, double maxZ) {
        if (maxX - minX < 1.0E-7 || maxY - minY < 1.0E-7 || maxZ - minZ < 1.0E-7) {
            return VoxelShapes.empty();
        }
        int j = VoxelShapes.findRequiredBitResolution(minX, maxX);
        int k = VoxelShapes.findRequiredBitResolution(minY, maxY);
        int l = VoxelShapes.findRequiredBitResolution(minZ, maxZ);
        if (j < 0 || k < 0 || l < 0) {
            return new ArrayVoxelShape(VoxelShapes.FULL_CUBE.voxels, DoubleArrayList.wrap(new double[]{minX, maxX}), DoubleArrayList.wrap(new double[]{minY, maxY}), DoubleArrayList.wrap(new double[]{minZ, maxZ}));
        }
        if (j == 0 && k == 0 && l == 0) {
            return VoxelShapes.fullCube();
        }
        int m = 1 << j;
        int n = 1 << k;
        int o = 1 << l;
        BitSetVoxelSet lv = BitSetVoxelSet.create(m, n, o, (int)Math.round(minX * (double)m), (int)Math.round(minY * (double)n), (int)Math.round(minZ * (double)o), (int)Math.round(maxX * (double)m), (int)Math.round(maxY * (double)n), (int)Math.round(maxZ * (double)o));
        return new SimpleVoxelShape(lv);
    }

    public static VoxelShape cuboid(Box box) {
        return VoxelShapes.cuboidUnchecked(box.minX, box.minY, box.minZ, box.maxX, box.maxY, box.maxZ);
    }

    @VisibleForTesting
    protected static int findRequiredBitResolution(double min, double max) {
        if (min < -1.0E-7 || max > 1.0000001) {
            return -1;
        }
        for (int i = 0; i <= 3; ++i) {
            boolean bl2;
            int j = 1 << i;
            double f = min * (double)j;
            double g = max * (double)j;
            boolean bl = Math.abs(f - (double)Math.round(f)) < 1.0E-7 * (double)j;
            boolean bl3 = bl2 = Math.abs(g - (double)Math.round(g)) < 1.0E-7 * (double)j;
            if (!bl || !bl2) continue;
            return i;
        }
        return -1;
    }

    protected static long lcm(int a, int b) {
        return (long)a * (long)(b / IntMath.gcd(a, b));
    }

    public static VoxelShape union(VoxelShape first, VoxelShape second) {
        return VoxelShapes.combineAndSimplify(first, second, BooleanBiFunction.OR);
    }

    public static VoxelShape union(VoxelShape first, VoxelShape ... others) {
        return Arrays.stream(others).reduce(first, VoxelShapes::union);
    }

    public static VoxelShape combineAndSimplify(VoxelShape first, VoxelShape second, BooleanBiFunction function) {
        return VoxelShapes.combine(first, second, function).simplify();
    }

    public static VoxelShape combine(VoxelShape one, VoxelShape two, BooleanBiFunction function) {
        if (function.apply(false, false)) {
            throw Util.throwOrPause(new IllegalArgumentException());
        }
        if (one == two) {
            return function.apply(true, true) ? one : VoxelShapes.empty();
        }
        boolean bl = function.apply(true, false);
        boolean bl2 = function.apply(false, true);
        if (one.isEmpty()) {
            return bl2 ? two : VoxelShapes.empty();
        }
        if (two.isEmpty()) {
            return bl ? one : VoxelShapes.empty();
        }
        PairList lv = VoxelShapes.createListPair(1, one.getPointPositions(Direction.Axis.X), two.getPointPositions(Direction.Axis.X), bl, bl2);
        PairList lv2 = VoxelShapes.createListPair(lv.size() - 1, one.getPointPositions(Direction.Axis.Y), two.getPointPositions(Direction.Axis.Y), bl, bl2);
        PairList lv3 = VoxelShapes.createListPair((lv.size() - 1) * (lv2.size() - 1), one.getPointPositions(Direction.Axis.Z), two.getPointPositions(Direction.Axis.Z), bl, bl2);
        BitSetVoxelSet lv4 = BitSetVoxelSet.combine(one.voxels, two.voxels, lv, lv2, lv3, function);
        if (lv instanceof FractionalPairList && lv2 instanceof FractionalPairList && lv3 instanceof FractionalPairList) {
            return new SimpleVoxelShape(lv4);
        }
        return new ArrayVoxelShape((VoxelSet)lv4, lv.getPairs(), lv2.getPairs(), lv3.getPairs());
    }

    public static boolean matchesAnywhere(VoxelShape shape1, VoxelShape shape2, BooleanBiFunction predicate) {
        if (predicate.apply(false, false)) {
            throw Util.throwOrPause(new IllegalArgumentException());
        }
        boolean bl = shape1.isEmpty();
        boolean bl2 = shape2.isEmpty();
        if (bl || bl2) {
            return predicate.apply(!bl, !bl2);
        }
        if (shape1 == shape2) {
            return predicate.apply(true, true);
        }
        boolean bl3 = predicate.apply(true, false);
        boolean bl4 = predicate.apply(false, true);
        for (Direction.Axis lv : AxisCycleDirection.AXES) {
            if (shape1.getMax(lv) < shape2.getMin(lv) - 1.0E-7) {
                return bl3 || bl4;
            }
            if (!(shape2.getMax(lv) < shape1.getMin(lv) - 1.0E-7)) continue;
            return bl3 || bl4;
        }
        PairList lv2 = VoxelShapes.createListPair(1, shape1.getPointPositions(Direction.Axis.X), shape2.getPointPositions(Direction.Axis.X), bl3, bl4);
        PairList lv3 = VoxelShapes.createListPair(lv2.size() - 1, shape1.getPointPositions(Direction.Axis.Y), shape2.getPointPositions(Direction.Axis.Y), bl3, bl4);
        PairList lv4 = VoxelShapes.createListPair((lv2.size() - 1) * (lv3.size() - 1), shape1.getPointPositions(Direction.Axis.Z), shape2.getPointPositions(Direction.Axis.Z), bl3, bl4);
        return VoxelShapes.matchesAnywhere(lv2, lv3, lv4, shape1.voxels, shape2.voxels, predicate);
    }

    private static boolean matchesAnywhere(PairList mergedX, PairList mergedY, PairList mergedZ, VoxelSet shape1, VoxelSet shape2, BooleanBiFunction predicate) {
        return !mergedX.forEachPair((x1, x2, index1) -> mergedY.forEachPair((y1, y2, index2) -> mergedZ.forEachPair((z1, z2, index3) -> !predicate.apply(shape1.inBoundsAndContains(x1, y1, z1), shape2.inBoundsAndContains(x2, y2, z2)))));
    }

    public static double calculateMaxOffset(Direction.Axis axis, Box box, Iterable<VoxelShape> shapes, double maxDist) {
        for (VoxelShape lv : shapes) {
            if (Math.abs(maxDist) < 1.0E-7) {
                return 0.0;
            }
            maxDist = lv.calculateMaxDistance(axis, box, maxDist);
        }
        return maxDist;
    }

    public static boolean isSideCovered(VoxelShape shape, VoxelShape neighbor, Direction direction) {
        if (shape == VoxelShapes.fullCube() && neighbor == VoxelShapes.fullCube()) {
            return true;
        }
        if (neighbor.isEmpty()) {
            return false;
        }
        Direction.Axis lv = direction.getAxis();
        Direction.AxisDirection lv2 = direction.getDirection();
        VoxelShape lv3 = lv2 == Direction.AxisDirection.POSITIVE ? shape : neighbor;
        VoxelShape lv4 = lv2 == Direction.AxisDirection.POSITIVE ? neighbor : shape;
        BooleanBiFunction lv5 = lv2 == Direction.AxisDirection.POSITIVE ? BooleanBiFunction.ONLY_FIRST : BooleanBiFunction.ONLY_SECOND;
        return DoubleMath.fuzzyEquals(lv3.getMax(lv), 1.0, 1.0E-7) && DoubleMath.fuzzyEquals(lv4.getMin(lv), 0.0, 1.0E-7) && !VoxelShapes.matchesAnywhere(new SlicedVoxelShape(lv3, lv, lv3.voxels.getSize(lv) - 1), new SlicedVoxelShape(lv4, lv, 0), lv5);
    }

    public static VoxelShape extrudeFace(VoxelShape shape, Direction direction) {
        int i;
        boolean bl;
        if (shape == VoxelShapes.fullCube()) {
            return VoxelShapes.fullCube();
        }
        Direction.Axis lv = direction.getAxis();
        if (direction.getDirection() == Direction.AxisDirection.POSITIVE) {
            bl = DoubleMath.fuzzyEquals(shape.getMax(lv), 1.0, 1.0E-7);
            i = shape.voxels.getSize(lv) - 1;
        } else {
            bl = DoubleMath.fuzzyEquals(shape.getMin(lv), 0.0, 1.0E-7);
            i = 0;
        }
        if (!bl) {
            return VoxelShapes.empty();
        }
        return new SlicedVoxelShape(shape, lv, i);
    }

    public static boolean adjacentSidesCoverSquare(VoxelShape one, VoxelShape two, Direction direction) {
        VoxelShape lv4;
        if (one == VoxelShapes.fullCube() || two == VoxelShapes.fullCube()) {
            return true;
        }
        Direction.Axis lv = direction.getAxis();
        Direction.AxisDirection lv2 = direction.getDirection();
        VoxelShape lv3 = lv2 == Direction.AxisDirection.POSITIVE ? one : two;
        VoxelShape voxelShape = lv4 = lv2 == Direction.AxisDirection.POSITIVE ? two : one;
        if (!DoubleMath.fuzzyEquals(lv3.getMax(lv), 1.0, 1.0E-7)) {
            lv3 = VoxelShapes.empty();
        }
        if (!DoubleMath.fuzzyEquals(lv4.getMin(lv), 0.0, 1.0E-7)) {
            lv4 = VoxelShapes.empty();
        }
        return !VoxelShapes.matchesAnywhere(VoxelShapes.fullCube(), VoxelShapes.combine(new SlicedVoxelShape(lv3, lv, lv3.voxels.getSize(lv) - 1), new SlicedVoxelShape(lv4, lv, 0), BooleanBiFunction.OR), BooleanBiFunction.ONLY_FIRST);
    }

    public static boolean unionCoversFullCube(VoxelShape one, VoxelShape two) {
        if (one == VoxelShapes.fullCube() || two == VoxelShapes.fullCube()) {
            return true;
        }
        if (one.isEmpty() && two.isEmpty()) {
            return false;
        }
        return !VoxelShapes.matchesAnywhere(VoxelShapes.fullCube(), VoxelShapes.combine(one, two, BooleanBiFunction.OR), BooleanBiFunction.ONLY_FIRST);
    }

    @VisibleForTesting
    protected static PairList createListPair(int size, DoubleList first, DoubleList second, boolean includeFirst, boolean includeSecond) {
        long l;
        int j = first.size() - 1;
        int k = second.size() - 1;
        if (first instanceof FractionalDoubleList && second instanceof FractionalDoubleList && (long)size * (l = VoxelShapes.lcm(j, k)) <= 256L) {
            return new FractionalPairList(j, k);
        }
        if (first.getDouble(j) < second.getDouble(0) - 1.0E-7) {
            return new DisjointPairList(first, second, false);
        }
        if (second.getDouble(k) < first.getDouble(0) - 1.0E-7) {
            return new DisjointPairList(second, first, true);
        }
        if (j == k && Objects.equals(first, second)) {
            return new IdentityPairList(first);
        }
        return new SimplePairList(first, second, includeFirst, includeSecond);
    }

    public static interface BoxConsumer {
        public void consume(double var1, double var3, double var5, double var7, double var9, double var11);
    }
}

