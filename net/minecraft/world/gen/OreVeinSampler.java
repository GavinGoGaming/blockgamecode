/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.world.gen;

import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.random.Random;
import net.minecraft.util.math.random.RandomSplitter;
import net.minecraft.world.gen.chunk.ChunkNoiseSampler;
import net.minecraft.world.gen.densityfunction.DensityFunction;

public final class OreVeinSampler {
    private static final float DENSITY_THRESHOLD = 0.4f;
    private static final int MAX_DENSITY_INTRUSION = 20;
    private static final double LIMINAL_DENSITY_REDUCTION = 0.2;
    private static final float BLOCK_GENERATION_CHANCE = 0.7f;
    private static final float MIN_ORE_CHANCE = 0.1f;
    private static final float MAX_ORE_CHANCE = 0.3f;
    private static final float DENSITY_FOR_MAX_ORE_CHANCE = 0.6f;
    private static final float RAW_ORE_BLOCK_CHANCE = 0.02f;
    private static final float VEIN_GAP_THRESHOLD = -0.3f;

    private OreVeinSampler() {
    }

    protected static ChunkNoiseSampler.BlockStateSampler create(DensityFunction veinToggle, DensityFunction veinRidged, DensityFunction veinGap, RandomSplitter randomDeriver) {
        BlockState lv = null;
        return pos -> {
            double d = veinToggle.sample(pos);
            int i = pos.blockY();
            VeinType lv = d > 0.0 ? VeinType.COPPER : VeinType.IRON;
            double e = Math.abs(d);
            int j = lv.maxY - i;
            int k = i - lv.minY;
            if (k < 0 || j < 0) {
                return lv;
            }
            int l = Math.min(j, k);
            double f = MathHelper.clampedMap((double)l, 0.0, 20.0, -0.2, 0.0);
            if (e + f < (double)0.4f) {
                return lv;
            }
            Random lv2 = randomDeriver.split(pos.blockX(), i, pos.blockZ());
            if (lv2.nextFloat() > 0.7f) {
                return lv;
            }
            if (veinRidged.sample(pos) >= 0.0) {
                return lv;
            }
            double g = MathHelper.clampedMap(e, (double)0.4f, (double)0.6f, (double)0.1f, (double)0.3f);
            if ((double)lv2.nextFloat() < g && veinGap.sample(pos) > (double)-0.3f) {
                return lv2.nextFloat() < 0.02f ? lv.rawOreBlock : lv.ore;
            }
            return lv.stone;
        };
    }

    protected static enum VeinType {
        COPPER(Blocks.COPPER_ORE.getDefaultState(), Blocks.RAW_COPPER_BLOCK.getDefaultState(), Blocks.GRANITE.getDefaultState(), 0, 50),
        IRON(Blocks.DEEPSLATE_IRON_ORE.getDefaultState(), Blocks.RAW_IRON_BLOCK.getDefaultState(), Blocks.TUFF.getDefaultState(), -60, -8);

        final BlockState ore;
        final BlockState rawOreBlock;
        final BlockState stone;
        protected final int minY;
        protected final int maxY;

        private VeinType(BlockState ore, BlockState rawOreBlock, BlockState stone, int minY, int maxY) {
            this.ore = ore;
            this.rawOreBlock = rawOreBlock;
            this.stone = stone;
            this.minY = minY;
            this.maxY = maxY;
        }
    }
}

