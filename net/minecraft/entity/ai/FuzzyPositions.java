/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 * 
 * Could not load the following classes:
 *  org.jetbrains.annotations.Nullable
 */
package net.minecraft.entity.ai;

import com.google.common.annotations.VisibleForTesting;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.function.ToDoubleFunction;
import net.minecraft.entity.mob.PathAwareEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.random.Random;
import org.jetbrains.annotations.Nullable;

public class FuzzyPositions {
    private static final int GAUSS_RANGE = 10;

    public static BlockPos localFuzz(Random random, int horizontalRange, int verticalRange) {
        int k = random.nextInt(2 * horizontalRange + 1) - horizontalRange;
        int l = random.nextInt(2 * verticalRange + 1) - verticalRange;
        int m = random.nextInt(2 * horizontalRange + 1) - horizontalRange;
        return new BlockPos(k, l, m);
    }

    @Nullable
    public static BlockPos localFuzz(Random random, int horizontalRange, int verticalRange, int startHeight, double directionX, double directionZ, double angleRange) {
        double g = MathHelper.atan2(directionZ, directionX) - 1.5707963705062866;
        double h = g + (double)(2.0f * random.nextFloat() - 1.0f) * angleRange;
        double l = Math.sqrt(random.nextDouble()) * (double)MathHelper.SQUARE_ROOT_OF_TWO * (double)horizontalRange;
        double m = -l * Math.sin(h);
        double n = l * Math.cos(h);
        if (Math.abs(m) > (double)horizontalRange || Math.abs(n) > (double)horizontalRange) {
            return null;
        }
        int o = random.nextInt(2 * verticalRange + 1) - verticalRange + startHeight;
        return BlockPos.ofFloored(m, o, n);
    }

    @VisibleForTesting
    public static BlockPos upWhile(BlockPos pos, int maxY, Predicate<BlockPos> condition) {
        if (condition.test(pos)) {
            BlockPos lv = pos.up();
            while (lv.getY() < maxY && condition.test(lv)) {
                lv = lv.up();
            }
            return lv;
        }
        return pos;
    }

    @VisibleForTesting
    public static BlockPos upWhile(BlockPos pos, int extraAbove, int max, Predicate<BlockPos> condition) {
        if (extraAbove < 0) {
            throw new IllegalArgumentException("aboveSolidAmount was " + extraAbove + ", expected >= 0");
        }
        if (condition.test(pos)) {
            BlockPos lv3;
            BlockPos lv = pos.up();
            while (lv.getY() < max && condition.test(lv)) {
                lv = lv.up();
            }
            BlockPos lv2 = lv;
            while (lv2.getY() < max && lv2.getY() - lv.getY() < extraAbove && !condition.test(lv3 = lv2.up())) {
                lv2 = lv3;
            }
            return lv2;
        }
        return pos;
    }

    @Nullable
    public static Vec3d guessBestPathTarget(PathAwareEntity entity, Supplier<BlockPos> factory) {
        return FuzzyPositions.guessBest(factory, entity::getPathfindingFavor);
    }

    @Nullable
    public static Vec3d guessBest(Supplier<BlockPos> factory, ToDoubleFunction<BlockPos> scorer) {
        double d = Double.NEGATIVE_INFINITY;
        BlockPos lv = null;
        for (int i = 0; i < 10; ++i) {
            double e;
            BlockPos lv2 = factory.get();
            if (lv2 == null || !((e = scorer.applyAsDouble(lv2)) > d)) continue;
            d = e;
            lv = lv2;
        }
        return lv != null ? Vec3d.ofBottomCenter(lv) : null;
    }

    public static BlockPos towardTarget(PathAwareEntity entity, int horizontalRange, Random random, BlockPos fuzz) {
        int j = fuzz.getX();
        int k = fuzz.getZ();
        if (entity.hasPositionTarget() && horizontalRange > 1) {
            BlockPos lv = entity.getPositionTarget();
            j = entity.getX() > (double)lv.getX() ? (j -= random.nextInt(horizontalRange / 2)) : (j += random.nextInt(horizontalRange / 2));
            k = entity.getZ() > (double)lv.getZ() ? (k -= random.nextInt(horizontalRange / 2)) : (k += random.nextInt(horizontalRange / 2));
        }
        return BlockPos.ofFloored((double)j + entity.getX(), (double)fuzz.getY() + entity.getY(), (double)k + entity.getZ());
    }
}

