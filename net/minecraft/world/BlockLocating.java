/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.world;

import com.google.common.annotations.VisibleForTesting;
import com.mojang.datafixers.util.Pair;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import java.util.Optional;
import java.util.function.Predicate;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.BlockView;

public class BlockLocating {
    public static Rectangle getLargestRectangle(BlockPos center, Direction.Axis primaryAxis, int primaryMaxBlocks, Direction.Axis secondaryAxis, int secondaryMaxBlocks, Predicate<BlockPos> predicate) {
        IntBounds lv6;
        int o;
        BlockPos.Mutable lv = center.mutableCopy();
        Direction lv2 = Direction.get(Direction.AxisDirection.NEGATIVE, primaryAxis);
        Direction lv3 = lv2.getOpposite();
        Direction lv4 = Direction.get(Direction.AxisDirection.NEGATIVE, secondaryAxis);
        Direction lv5 = lv4.getOpposite();
        int k = BlockLocating.moveWhile(predicate, lv.set(center), lv2, primaryMaxBlocks);
        int l = BlockLocating.moveWhile(predicate, lv.set(center), lv3, primaryMaxBlocks);
        int m = k;
        IntBounds[] lvs = new IntBounds[m + 1 + l];
        lvs[m] = new IntBounds(BlockLocating.moveWhile(predicate, lv.set(center), lv4, secondaryMaxBlocks), BlockLocating.moveWhile(predicate, lv.set(center), lv5, secondaryMaxBlocks));
        int n = lvs[m].min;
        for (o = 1; o <= k; ++o) {
            lv6 = lvs[m - (o - 1)];
            lvs[m - o] = new IntBounds(BlockLocating.moveWhile(predicate, lv.set(center).move(lv2, o), lv4, lv6.min), BlockLocating.moveWhile(predicate, lv.set(center).move(lv2, o), lv5, lv6.max));
        }
        for (o = 1; o <= l; ++o) {
            lv6 = lvs[m + o - 1];
            lvs[m + o] = new IntBounds(BlockLocating.moveWhile(predicate, lv.set(center).move(lv3, o), lv4, lv6.min), BlockLocating.moveWhile(predicate, lv.set(center).move(lv3, o), lv5, lv6.max));
        }
        o = 0;
        int p = 0;
        int q = 0;
        int r = 0;
        int[] is = new int[lvs.length];
        for (int s = n; s >= 0; --s) {
            int v;
            int u;
            IntBounds lv7;
            for (int t = 0; t < lvs.length; ++t) {
                lv7 = lvs[t];
                u = n - lv7.min;
                v = n + lv7.max;
                is[t] = s >= u && s <= v ? v + 1 - s : 0;
            }
            Pair<IntBounds, Integer> pair = BlockLocating.findLargestRectangle(is);
            lv7 = pair.getFirst();
            u = 1 + lv7.max - lv7.min;
            v = pair.getSecond();
            if (u * v <= q * r) continue;
            o = lv7.min;
            p = s;
            q = u;
            r = v;
        }
        return new Rectangle(center.offset(primaryAxis, o - m).offset(secondaryAxis, p - n), q, r);
    }

    private static int moveWhile(Predicate<BlockPos> predicate, BlockPos.Mutable pos, Direction direction, int max) {
        int j;
        for (j = 0; j < max && predicate.test(pos.move(direction)); ++j) {
        }
        return j;
    }

    @VisibleForTesting
    static Pair<IntBounds, Integer> findLargestRectangle(int[] heights) {
        int i = 0;
        int j = 0;
        int k = 0;
        IntArrayList intStack = new IntArrayList();
        intStack.push(0);
        for (int l = 1; l <= heights.length; ++l) {
            int m;
            int n = m = l == heights.length ? 0 : heights[l];
            while (!intStack.isEmpty()) {
                int n2 = heights[intStack.topInt()];
                if (m >= n2) {
                    intStack.push(l);
                    break;
                }
                intStack.popInt();
                int o = intStack.isEmpty() ? 0 : intStack.topInt() + 1;
                if (n2 * (l - o) <= k * (j - i)) continue;
                j = l;
                i = o;
                k = n2;
            }
            if (!intStack.isEmpty()) continue;
            intStack.push(l);
        }
        return new Pair<IntBounds, Integer>(new IntBounds(i, j - 1), k);
    }

    public static Optional<BlockPos> findColumnEnd(BlockView world, BlockPos pos, Block intermediateBlock, Direction direction, Block endBlock) {
        BlockState lv2;
        BlockPos.Mutable lv = pos.mutableCopy();
        do {
            lv.move(direction);
        } while ((lv2 = world.getBlockState(lv)).isOf(intermediateBlock));
        if (lv2.isOf(endBlock)) {
            return Optional.of(lv);
        }
        return Optional.empty();
    }

    public static class IntBounds {
        public final int min;
        public final int max;

        public IntBounds(int min, int max) {
            this.min = min;
            this.max = max;
        }

        public String toString() {
            return "IntBounds{min=" + this.min + ", max=" + this.max + "}";
        }
    }

    public static class Rectangle {
        public final BlockPos lowerLeft;
        public final int width;
        public final int height;

        public Rectangle(BlockPos lowerLeft, int width, int height) {
            this.lowerLeft = lowerLeft;
            this.width = width;
            this.height = height;
        }
    }
}

