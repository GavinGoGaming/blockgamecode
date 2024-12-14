/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.world.gen.feature;

import com.mojang.serialization.Codec;
import java.util.BitSet;
import java.util.function.Function;
import net.minecraft.block.BlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkSectionPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.ChunkSectionCache;
import net.minecraft.world.Heightmap;
import net.minecraft.world.StructureWorldAccess;
import net.minecraft.world.chunk.ChunkSection;
import net.minecraft.world.gen.feature.Feature;
import net.minecraft.world.gen.feature.OreFeatureConfig;
import net.minecraft.world.gen.feature.util.FeatureContext;

public class OreFeature
extends Feature<OreFeatureConfig> {
    public OreFeature(Codec<OreFeatureConfig> codec) {
        super(codec);
    }

    @Override
    public boolean generate(FeatureContext<OreFeatureConfig> context) {
        Random lv = context.getRandom();
        BlockPos lv2 = context.getOrigin();
        StructureWorldAccess lv3 = context.getWorld();
        OreFeatureConfig lv4 = context.getConfig();
        float f = lv.nextFloat() * (float)Math.PI;
        float g = (float)lv4.size / 8.0f;
        int i = MathHelper.ceil(((float)lv4.size / 16.0f * 2.0f + 1.0f) / 2.0f);
        double d = (double)lv2.getX() + Math.sin(f) * (double)g;
        double e = (double)lv2.getX() - Math.sin(f) * (double)g;
        double h = (double)lv2.getZ() + Math.cos(f) * (double)g;
        double j = (double)lv2.getZ() - Math.cos(f) * (double)g;
        int k = 2;
        double l = lv2.getY() + lv.nextInt(3) - 2;
        double m = lv2.getY() + lv.nextInt(3) - 2;
        int n = lv2.getX() - MathHelper.ceil(g) - i;
        int o = lv2.getY() - 2 - i;
        int p = lv2.getZ() - MathHelper.ceil(g) - i;
        int q = 2 * (MathHelper.ceil(g) + i);
        int r = 2 * (2 + i);
        for (int s = n; s <= n + q; ++s) {
            for (int t = p; t <= p + q; ++t) {
                if (o > lv3.getTopY(Heightmap.Type.OCEAN_FLOOR_WG, s, t)) continue;
                return this.generateVeinPart(lv3, lv, lv4, d, e, h, j, l, m, n, o, p, q, r);
            }
        }
        return false;
    }

    protected boolean generateVeinPart(StructureWorldAccess world, Random random, OreFeatureConfig config, double startX, double endX, double startZ, double endZ, double startY, double endY, int x, int y, int z, int horizontalSize, int verticalSize) {
        double v;
        double u;
        double t;
        double s;
        int q;
        int o = 0;
        BitSet bitSet = new BitSet(horizontalSize * verticalSize * horizontalSize);
        BlockPos.Mutable lv = new BlockPos.Mutable();
        int p = config.size;
        double[] ds = new double[p * 4];
        for (q = 0; q < p; ++q) {
            float r = (float)q / (float)p;
            s = MathHelper.lerp((double)r, startX, endX);
            t = MathHelper.lerp((double)r, startY, endY);
            u = MathHelper.lerp((double)r, startZ, endZ);
            v = random.nextDouble() * (double)p / 16.0;
            double w = ((double)(MathHelper.sin((float)Math.PI * r) + 1.0f) * v + 1.0) / 2.0;
            ds[q * 4 + 0] = s;
            ds[q * 4 + 1] = t;
            ds[q * 4 + 2] = u;
            ds[q * 4 + 3] = w;
        }
        for (q = 0; q < p - 1; ++q) {
            if (ds[q * 4 + 3] <= 0.0) continue;
            for (int x2 = q + 1; x2 < p; ++x2) {
                if (ds[x2 * 4 + 3] <= 0.0 || !((v = ds[q * 4 + 3] - ds[x2 * 4 + 3]) * v > (s = ds[q * 4 + 0] - ds[x2 * 4 + 0]) * s + (t = ds[q * 4 + 1] - ds[x2 * 4 + 1]) * t + (u = ds[q * 4 + 2] - ds[x2 * 4 + 2]) * u)) continue;
                if (v > 0.0) {
                    ds[x2 * 4 + 3] = -1.0;
                    continue;
                }
                ds[q * 4 + 3] = -1.0;
            }
        }
        try (ChunkSectionCache lv2 = new ChunkSectionCache(world);){
            for (int x3 = 0; x3 < p; ++x3) {
                s = ds[x3 * 4 + 3];
                if (s < 0.0) continue;
                t = ds[x3 * 4 + 0];
                u = ds[x3 * 4 + 1];
                v = ds[x3 * 4 + 2];
                int y2 = Math.max(MathHelper.floor(t - s), x);
                int z2 = Math.max(MathHelper.floor(u - s), y);
                int aa = Math.max(MathHelper.floor(v - s), z);
                int ab = Math.max(MathHelper.floor(t + s), y2);
                int ac = Math.max(MathHelper.floor(u + s), z2);
                int ad = Math.max(MathHelper.floor(v + s), aa);
                for (int ae = y2; ae <= ab; ++ae) {
                    double af = ((double)ae + 0.5 - t) / s;
                    if (!(af * af < 1.0)) continue;
                    for (int ag = z2; ag <= ac; ++ag) {
                        double ah = ((double)ag + 0.5 - u) / s;
                        if (!(af * af + ah * ah < 1.0)) continue;
                        block11: for (int ai = aa; ai <= ad; ++ai) {
                            ChunkSection lv3;
                            int ak;
                            double aj = ((double)ai + 0.5 - v) / s;
                            if (!(af * af + ah * ah + aj * aj < 1.0) || world.isOutOfHeightLimit(ag) || bitSet.get(ak = ae - x + (ag - y) * horizontalSize + (ai - z) * horizontalSize * verticalSize)) continue;
                            bitSet.set(ak);
                            lv.set(ae, ag, ai);
                            if (!world.isValidForSetBlock(lv) || (lv3 = lv2.getSection(lv)) == null) continue;
                            int al = ChunkSectionPos.getLocalCoord(ae);
                            int am = ChunkSectionPos.getLocalCoord(ag);
                            int an = ChunkSectionPos.getLocalCoord(ai);
                            BlockState lv4 = lv3.getBlockState(al, am, an);
                            for (OreFeatureConfig.Target lv5 : config.targets) {
                                if (!OreFeature.shouldPlace(lv4, lv2::getBlockState, random, config, lv5, lv)) continue;
                                lv3.setBlockState(al, am, an, lv5.state, false);
                                ++o;
                                continue block11;
                            }
                        }
                    }
                }
            }
        }
        return o > 0;
    }

    public static boolean shouldPlace(BlockState state, Function<BlockPos, BlockState> posToState, Random random, OreFeatureConfig config, OreFeatureConfig.Target target, BlockPos.Mutable pos) {
        if (!target.target.test(state, random)) {
            return false;
        }
        if (OreFeature.shouldNotDiscard(random, config.discardOnAirChance)) {
            return true;
        }
        return !OreFeature.isExposedToAir(posToState, pos);
    }

    protected static boolean shouldNotDiscard(Random random, float chance) {
        if (chance <= 0.0f) {
            return true;
        }
        if (chance >= 1.0f) {
            return false;
        }
        return random.nextFloat() >= chance;
    }
}

