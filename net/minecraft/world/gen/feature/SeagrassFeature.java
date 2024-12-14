/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.world.gen.feature;

import com.mojang.serialization.Codec;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.TallSeagrassBlock;
import net.minecraft.block.enums.DoubleBlockHalf;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.Heightmap;
import net.minecraft.world.StructureWorldAccess;
import net.minecraft.world.gen.ProbabilityConfig;
import net.minecraft.world.gen.feature.Feature;
import net.minecraft.world.gen.feature.util.FeatureContext;

public class SeagrassFeature
extends Feature<ProbabilityConfig> {
    public SeagrassFeature(Codec<ProbabilityConfig> codec) {
        super(codec);
    }

    @Override
    public boolean generate(FeatureContext<ProbabilityConfig> context) {
        boolean bl = false;
        Random lv = context.getRandom();
        StructureWorldAccess lv2 = context.getWorld();
        BlockPos lv3 = context.getOrigin();
        ProbabilityConfig lv4 = context.getConfig();
        int i = lv.nextInt(8) - lv.nextInt(8);
        int j = lv.nextInt(8) - lv.nextInt(8);
        int k = lv2.getTopY(Heightmap.Type.OCEAN_FLOOR, lv3.getX() + i, lv3.getZ() + j);
        BlockPos lv5 = new BlockPos(lv3.getX() + i, k, lv3.getZ() + j);
        if (lv2.getBlockState(lv5).isOf(Blocks.WATER)) {
            BlockState lv6;
            boolean bl2 = lv.nextDouble() < (double)lv4.probability;
            BlockState blockState = lv6 = bl2 ? Blocks.TALL_SEAGRASS.getDefaultState() : Blocks.SEAGRASS.getDefaultState();
            if (lv6.canPlaceAt(lv2, lv5)) {
                if (bl2) {
                    BlockState lv7 = (BlockState)lv6.with(TallSeagrassBlock.HALF, DoubleBlockHalf.UPPER);
                    BlockPos lv8 = lv5.up();
                    if (lv2.getBlockState(lv8).isOf(Blocks.WATER)) {
                        lv2.setBlockState(lv5, lv6, Block.NOTIFY_LISTENERS);
                        lv2.setBlockState(lv8, lv7, Block.NOTIFY_LISTENERS);
                    }
                } else {
                    lv2.setBlockState(lv5, lv6, Block.NOTIFY_LISTENERS);
                }
                bl = true;
            }
        }
        return bl;
    }
}

