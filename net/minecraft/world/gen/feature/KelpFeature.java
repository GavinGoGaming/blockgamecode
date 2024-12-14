/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.world.gen.feature;

import com.mojang.serialization.Codec;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.KelpBlock;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.Heightmap;
import net.minecraft.world.StructureWorldAccess;
import net.minecraft.world.gen.feature.DefaultFeatureConfig;
import net.minecraft.world.gen.feature.Feature;
import net.minecraft.world.gen.feature.util.FeatureContext;

public class KelpFeature
extends Feature<DefaultFeatureConfig> {
    public KelpFeature(Codec<DefaultFeatureConfig> codec) {
        super(codec);
    }

    @Override
    public boolean generate(FeatureContext<DefaultFeatureConfig> context) {
        int i = 0;
        StructureWorldAccess lv = context.getWorld();
        BlockPos lv2 = context.getOrigin();
        Random lv3 = context.getRandom();
        int j = lv.getTopY(Heightmap.Type.OCEAN_FLOOR, lv2.getX(), lv2.getZ());
        BlockPos lv4 = new BlockPos(lv2.getX(), j, lv2.getZ());
        if (lv.getBlockState(lv4).isOf(Blocks.WATER)) {
            BlockState lv5 = Blocks.KELP.getDefaultState();
            BlockState lv6 = Blocks.KELP_PLANT.getDefaultState();
            int k = 1 + lv3.nextInt(10);
            for (int l = 0; l <= k; ++l) {
                if (lv.getBlockState(lv4).isOf(Blocks.WATER) && lv.getBlockState(lv4.up()).isOf(Blocks.WATER) && lv6.canPlaceAt(lv, lv4)) {
                    if (l == k) {
                        lv.setBlockState(lv4, (BlockState)lv5.with(KelpBlock.AGE, lv3.nextInt(4) + 20), Block.NOTIFY_LISTENERS);
                        ++i;
                    } else {
                        lv.setBlockState(lv4, lv6, Block.NOTIFY_LISTENERS);
                    }
                } else if (l > 0) {
                    BlockPos lv7 = lv4.down();
                    if (!lv5.canPlaceAt(lv, lv7) || lv.getBlockState(lv7.down()).isOf(Blocks.KELP)) break;
                    lv.setBlockState(lv7, (BlockState)lv5.with(KelpBlock.AGE, lv3.nextInt(4) + 20), Block.NOTIFY_LISTENERS);
                    ++i;
                    break;
                }
                lv4 = lv4.up();
            }
        }
        return i > 0;
    }
}

