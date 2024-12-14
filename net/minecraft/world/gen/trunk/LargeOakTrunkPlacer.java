/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.world.gen.trunk;

import com.google.common.collect.Lists;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.BiConsumer;
import net.minecraft.block.BlockState;
import net.minecraft.block.PillarBlock;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.TestableWorld;
import net.minecraft.world.gen.feature.TreeFeatureConfig;
import net.minecraft.world.gen.foliage.FoliagePlacer;
import net.minecraft.world.gen.trunk.TrunkPlacer;
import net.minecraft.world.gen.trunk.TrunkPlacerType;

public class LargeOakTrunkPlacer
extends TrunkPlacer {
    public static final MapCodec<LargeOakTrunkPlacer> CODEC = RecordCodecBuilder.mapCodec(instance -> LargeOakTrunkPlacer.fillTrunkPlacerFields(instance).apply(instance, LargeOakTrunkPlacer::new));
    private static final double field_31524 = 0.618;
    private static final double field_31525 = 1.382;
    private static final double field_31526 = 0.381;
    private static final double field_31527 = 0.328;

    public LargeOakTrunkPlacer(int i, int j, int k) {
        super(i, j, k);
    }

    @Override
    protected TrunkPlacerType<?> getType() {
        return TrunkPlacerType.FANCY_TRUNK_PLACER;
    }

    @Override
    public List<FoliagePlacer.TreeNode> generate(TestableWorld world, BiConsumer<BlockPos, BlockState> replacer, Random random, int height, BlockPos startPos, TreeFeatureConfig config) {
        int o;
        int j = 5;
        int k = height + 2;
        int l = MathHelper.floor((double)k * 0.618);
        LargeOakTrunkPlacer.setToDirt(world, replacer, random, startPos.down(), config);
        double d = 1.0;
        int m = Math.min(1, MathHelper.floor(1.382 + Math.pow(1.0 * (double)k / 13.0, 2.0)));
        int n = startPos.getY() + l;
        ArrayList<BranchPosition> list = Lists.newArrayList();
        list.add(new BranchPosition(startPos.up(o), n));
        for (o = k - 5; o >= 0; --o) {
            float f = LargeOakTrunkPlacer.shouldGenerateBranch(k, o);
            if (f < 0.0f) continue;
            for (int p = 0; p < m; ++p) {
                BlockPos lv2;
                double e = 1.0;
                double g = 1.0 * (double)f * ((double)random.nextFloat() + 0.328);
                double h = (double)(random.nextFloat() * 2.0f) * Math.PI;
                double q = g * Math.sin(h) + 0.5;
                double r = g * Math.cos(h) + 0.5;
                BlockPos lv = startPos.add(MathHelper.floor(q), o - 1, MathHelper.floor(r));
                if (!this.makeOrCheckBranch(world, replacer, random, lv, lv2 = lv.up(5), false, config)) continue;
                int s = startPos.getX() - lv.getX();
                int t = startPos.getZ() - lv.getZ();
                double u = (double)lv.getY() - Math.sqrt(s * s + t * t) * 0.381;
                int v = u > (double)n ? n : (int)u;
                BlockPos lv3 = new BlockPos(startPos.getX(), v, startPos.getZ());
                if (!this.makeOrCheckBranch(world, replacer, random, lv3, lv, false, config)) continue;
                list.add(new BranchPosition(lv, lv3.getY()));
            }
        }
        this.makeOrCheckBranch(world, replacer, random, startPos, startPos.up(l), true, config);
        this.makeBranches(world, replacer, random, k, startPos, list, config);
        ArrayList<FoliagePlacer.TreeNode> list2 = Lists.newArrayList();
        for (BranchPosition lv4 : list) {
            if (!this.isHighEnough(k, lv4.getEndY() - startPos.getY())) continue;
            list2.add(lv4.node);
        }
        return list2;
    }

    private boolean makeOrCheckBranch(TestableWorld world, BiConsumer<BlockPos, BlockState> replacer, Random random, BlockPos startPos, BlockPos branchPos, boolean make, TreeFeatureConfig config) {
        if (!make && Objects.equals(startPos, branchPos)) {
            return true;
        }
        BlockPos lv = branchPos.add(-startPos.getX(), -startPos.getY(), -startPos.getZ());
        int i = this.getLongestSide(lv);
        float f = (float)lv.getX() / (float)i;
        float g = (float)lv.getY() / (float)i;
        float h = (float)lv.getZ() / (float)i;
        for (int j = 0; j <= i; ++j) {
            BlockPos lv2 = startPos.add(MathHelper.floor(0.5f + (float)j * f), MathHelper.floor(0.5f + (float)j * g), MathHelper.floor(0.5f + (float)j * h));
            if (make) {
                this.getAndSetState(world, replacer, random, lv2, config, state -> (BlockState)state.withIfExists(PillarBlock.AXIS, this.getLogAxis(startPos, lv2)));
                continue;
            }
            if (this.canReplaceOrIsLog(world, lv2)) continue;
            return false;
        }
        return true;
    }

    private int getLongestSide(BlockPos offset) {
        int i = MathHelper.abs(offset.getX());
        int j = MathHelper.abs(offset.getY());
        int k = MathHelper.abs(offset.getZ());
        return Math.max(i, Math.max(j, k));
    }

    private Direction.Axis getLogAxis(BlockPos branchStart, BlockPos branchEnd) {
        int j;
        Direction.Axis lv = Direction.Axis.Y;
        int i = Math.abs(branchEnd.getX() - branchStart.getX());
        int k = Math.max(i, j = Math.abs(branchEnd.getZ() - branchStart.getZ()));
        if (k > 0) {
            lv = i == k ? Direction.Axis.X : Direction.Axis.Z;
        }
        return lv;
    }

    private boolean isHighEnough(int treeHeight, int height) {
        return (double)height >= (double)treeHeight * 0.2;
    }

    private void makeBranches(TestableWorld world, BiConsumer<BlockPos, BlockState> replacer, Random random, int treeHeight, BlockPos startPos, List<BranchPosition> branchPositions, TreeFeatureConfig config) {
        for (BranchPosition lv : branchPositions) {
            int j = lv.getEndY();
            BlockPos lv2 = new BlockPos(startPos.getX(), j, startPos.getZ());
            if (lv2.equals(lv.node.getCenter()) || !this.isHighEnough(treeHeight, j - startPos.getY())) continue;
            this.makeOrCheckBranch(world, replacer, random, lv2, lv.node.getCenter(), true, config);
        }
    }

    private static float shouldGenerateBranch(int treeHeight, int height) {
        if ((float)height < (float)treeHeight * 0.3f) {
            return -1.0f;
        }
        float f = (float)treeHeight / 2.0f;
        float g = f - (float)height;
        float h = MathHelper.sqrt(f * f - g * g);
        if (g == 0.0f) {
            h = f;
        } else if (Math.abs(g) >= f) {
            return 0.0f;
        }
        return h * 0.5f;
    }

    static class BranchPosition {
        final FoliagePlacer.TreeNode node;
        private final int endY;

        public BranchPosition(BlockPos pos, int width) {
            this.node = new FoliagePlacer.TreeNode(pos, 0, false);
            this.endY = width;
        }

        public int getEndY() {
            return this.endY;
        }
    }
}

