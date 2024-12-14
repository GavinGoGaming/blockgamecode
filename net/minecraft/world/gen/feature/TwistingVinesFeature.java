/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.world.gen.feature;

import com.mojang.serialization.Codec;
import net.minecraft.block.AbstractPlantStemBlock;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.StructureWorldAccess;
import net.minecraft.world.WorldAccess;
import net.minecraft.world.gen.feature.Feature;
import net.minecraft.world.gen.feature.TwistingVinesFeatureConfig;
import net.minecraft.world.gen.feature.util.FeatureContext;

public class TwistingVinesFeature
extends Feature<TwistingVinesFeatureConfig> {
    public TwistingVinesFeature(Codec<TwistingVinesFeatureConfig> codec) {
        super(codec);
    }

    @Override
    public boolean generate(FeatureContext<TwistingVinesFeatureConfig> context) {
        BlockPos lv2;
        StructureWorldAccess lv = context.getWorld();
        if (TwistingVinesFeature.isNotSuitable(lv, lv2 = context.getOrigin())) {
            return false;
        }
        Random lv3 = context.getRandom();
        TwistingVinesFeatureConfig lv4 = context.getConfig();
        int i = lv4.spreadWidth();
        int j = lv4.spreadHeight();
        int k = lv4.maxHeight();
        BlockPos.Mutable lv5 = new BlockPos.Mutable();
        for (int l = 0; l < i * i; ++l) {
            lv5.set(lv2).move(MathHelper.nextInt(lv3, -i, i), MathHelper.nextInt(lv3, -j, j), MathHelper.nextInt(lv3, -i, i));
            if (!TwistingVinesFeature.canGenerate(lv, lv5) || TwistingVinesFeature.isNotSuitable(lv, lv5)) continue;
            int m = MathHelper.nextInt(lv3, 1, k);
            if (lv3.nextInt(6) == 0) {
                m *= 2;
            }
            if (lv3.nextInt(5) == 0) {
                m = 1;
            }
            int n = 17;
            int o = 25;
            TwistingVinesFeature.generateVineColumn(lv, lv3, lv5, m, 17, 25);
        }
        return true;
    }

    private static boolean canGenerate(WorldAccess world, BlockPos.Mutable pos) {
        do {
            pos.move(0, -1, 0);
            if (!world.isOutOfHeightLimit(pos)) continue;
            return false;
        } while (world.getBlockState(pos).isAir());
        pos.move(0, 1, 0);
        return true;
    }

    public static void generateVineColumn(WorldAccess world, Random random, BlockPos.Mutable pos, int maxLength, int minAge, int maxAge) {
        for (int l = 1; l <= maxLength; ++l) {
            if (world.isAir(pos)) {
                if (l == maxLength || !world.isAir((BlockPos)pos.up())) {
                    world.setBlockState(pos, (BlockState)Blocks.TWISTING_VINES.getDefaultState().with(AbstractPlantStemBlock.AGE, MathHelper.nextInt(random, minAge, maxAge)), Block.NOTIFY_LISTENERS);
                    break;
                }
                world.setBlockState(pos, Blocks.TWISTING_VINES_PLANT.getDefaultState(), Block.NOTIFY_LISTENERS);
            }
            pos.move(Direction.UP);
        }
    }

    private static boolean isNotSuitable(WorldAccess world, BlockPos pos) {
        if (!world.isAir(pos)) {
            return true;
        }
        BlockState lv = world.getBlockState(pos.down());
        return !lv.isOf(Blocks.NETHERRACK) && !lv.isOf(Blocks.WARPED_NYLIUM) && !lv.isOf(Blocks.WARPED_WART_BLOCK);
    }
}

