/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.world.gen.feature;

import com.google.common.collect.Lists;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.function.Predicate;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.BuddingAmethystBlock;
import net.minecraft.fluid.FluidState;
import net.minecraft.registry.tag.BlockTags;
import net.minecraft.state.property.Properties;
import net.minecraft.util.Util;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3i;
import net.minecraft.util.math.noise.DoublePerlinNoiseSampler;
import net.minecraft.util.math.random.CheckedRandom;
import net.minecraft.util.math.random.ChunkRandom;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.StructureWorldAccess;
import net.minecraft.world.gen.feature.Feature;
import net.minecraft.world.gen.feature.GeodeCrackConfig;
import net.minecraft.world.gen.feature.GeodeFeatureConfig;
import net.minecraft.world.gen.feature.GeodeLayerConfig;
import net.minecraft.world.gen.feature.GeodeLayerThicknessConfig;
import net.minecraft.world.gen.feature.util.FeatureContext;

public class GeodeFeature
extends Feature<GeodeFeatureConfig> {
    private static final Direction[] DIRECTIONS = Direction.values();

    public GeodeFeature(Codec<GeodeFeatureConfig> codec) {
        super(codec);
    }

    /*
     * Could not resolve type clashes
     */
    @Override
    public boolean generate(FeatureContext<GeodeFeatureConfig> context) {
        BlockState lv11;
        int o;
        int n;
        GeodeFeatureConfig lv = context.getConfig();
        Random lv2 = context.getRandom();
        BlockPos lv3 = context.getOrigin();
        StructureWorldAccess lv4 = context.getWorld();
        int i = lv.minGenOffset;
        int j = lv.maxGenOffset;
        LinkedList<Pair<BlockPos, Integer>> list = Lists.newLinkedList();
        int k = lv.distributionPoints.get(lv2);
        ChunkRandom lv5 = new ChunkRandom(new CheckedRandom(lv4.getSeed()));
        DoublePerlinNoiseSampler lv6 = DoublePerlinNoiseSampler.create(lv5, -4, 1.0);
        LinkedList<BlockPos> list2 = Lists.newLinkedList();
        double d = (double)k / (double)lv.outerWallDistance.getMax();
        GeodeLayerThicknessConfig lv7 = lv.layerThicknessConfig;
        GeodeLayerConfig lv8 = lv.layerConfig;
        GeodeCrackConfig lv9 = lv.crackConfig;
        double e = 1.0 / Math.sqrt(lv7.filling);
        double f = 1.0 / Math.sqrt(lv7.innerLayer + d);
        double g = 1.0 / Math.sqrt(lv7.middleLayer + d);
        double h = 1.0 / Math.sqrt(lv7.outerLayer + d);
        double l = 1.0 / Math.sqrt(lv9.baseCrackSize + lv2.nextDouble() / 2.0 + (k > 3 ? d : 0.0));
        boolean bl = (double)lv2.nextFloat() < lv9.generateCrackChance;
        int m = 0;
        for (n = 0; n < k; ++n) {
            int q;
            int p;
            o = lv.outerWallDistance.get(lv2);
            BlockPos lv10 = lv3.add(o, p = lv.outerWallDistance.get(lv2), q = lv.outerWallDistance.get(lv2));
            lv11 = lv4.getBlockState(lv10);
            if ((lv11.isAir() || lv11.isIn(BlockTags.GEODE_INVALID_BLOCKS)) && ++m > lv.invalidBlocksThreshold) {
                return false;
            }
            list.add(Pair.of(lv10, lv.pointOffset.get(lv2)));
        }
        if (bl) {
            n = lv2.nextInt(4);
            o = k * 2 + 1;
            if (n == 0) {
                list2.add(lv3.add(o, 7, 0));
                list2.add(lv3.add(o, 5, 0));
                list2.add(lv3.add(o, 1, 0));
            } else if (n == 1) {
                list2.add(lv3.add(0, 7, o));
                list2.add(lv3.add(0, 5, o));
                list2.add(lv3.add(0, 1, o));
            } else if (n == 2) {
                list2.add(lv3.add(o, 7, o));
                list2.add(lv3.add(o, 5, o));
                list2.add(lv3.add(o, 1, o));
            } else {
                list2.add(lv3.add(0, 7, 0));
                list2.add(lv3.add(0, 5, 0));
                list2.add(lv3.add(0, 1, 0));
            }
        }
        ArrayList<BlockPos> list3 = Lists.newArrayList();
        Predicate<BlockState> predicate = GeodeFeature.notInBlockTagPredicate(lv.layerConfig.cannotReplace);
        for (BlockPos lv12 : BlockPos.iterate(lv3.add(i, i, i), lv3.add(j, j, j))) {
            double r = lv6.sample(lv12.getX(), lv12.getY(), lv12.getZ()) * lv.noiseMultiplier;
            double s = 0.0;
            double t = 0.0;
            for (Pair pair : list) {
                s += MathHelper.inverseSqrt(lv12.getSquaredDistance((Vec3i)pair.getFirst()) + (double)((Integer)pair.getSecond()).intValue()) + r;
            }
            for (BlockPos lv13 : list2) {
                t += MathHelper.inverseSqrt(lv12.getSquaredDistance(lv13) + (double)lv9.crackPointOffset) + r;
            }
            if (s < h) continue;
            if (bl && t >= l && s < e) {
                this.setBlockStateIf(lv4, lv12, Blocks.AIR.getDefaultState(), predicate);
                for (Direction lv14 : DIRECTIONS) {
                    BlockPos lv15 = lv12.offset(lv14);
                    FluidState lv16 = lv4.getFluidState(lv15);
                    if (lv16.isEmpty()) continue;
                    lv4.scheduleFluidTick(lv15, lv16.getFluid(), 0);
                }
                continue;
            }
            if (s >= e) {
                this.setBlockStateIf(lv4, lv12, lv8.fillingProvider.get(lv2, lv12), predicate);
                continue;
            }
            if (s >= f) {
                boolean bl2;
                boolean bl3 = bl2 = (double)lv2.nextFloat() < lv.useAlternateLayer0Chance;
                if (bl2) {
                    this.setBlockStateIf(lv4, lv12, lv8.alternateInnerLayerProvider.get(lv2, lv12), predicate);
                } else {
                    this.setBlockStateIf(lv4, lv12, lv8.innerLayerProvider.get(lv2, lv12), predicate);
                }
                if (lv.placementsRequireLayer0Alternate && !bl2 || !((double)lv2.nextFloat() < lv.usePotentialPlacementsChance)) continue;
                list3.add(lv12.toImmutable());
                continue;
            }
            if (s >= g) {
                this.setBlockStateIf(lv4, lv12, lv8.middleLayerProvider.get(lv2, lv12), predicate);
                continue;
            }
            if (!(s >= h)) continue;
            this.setBlockStateIf(lv4, lv12, lv8.outerLayerProvider.get(lv2, lv12), predicate);
        }
        List<BlockState> list4 = lv8.innerBlocks;
        block5: for (BlockPos lv10 : list3) {
            lv11 = Util.getRandom(list4, lv2);
            for (Direction lv17 : DIRECTIONS) {
                if (lv11.contains(Properties.FACING)) {
                    lv11 = (BlockState)lv11.with(Properties.FACING, lv17);
                }
                BlockPos lv18 = lv10.offset(lv17);
                BlockState lv19 = lv4.getBlockState(lv18);
                if (lv11.contains(Properties.WATERLOGGED)) {
                    lv11 = (BlockState)lv11.with(Properties.WATERLOGGED, lv19.getFluidState().isStill());
                }
                if (!BuddingAmethystBlock.canGrowIn(lv19)) continue;
                this.setBlockStateIf(lv4, lv18, lv11, predicate);
                continue block5;
            }
        }
        return true;
    }
}

