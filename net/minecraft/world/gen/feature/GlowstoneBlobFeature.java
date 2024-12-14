/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.world.gen.feature;

import com.mojang.serialization.Codec;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.StructureWorldAccess;
import net.minecraft.world.gen.feature.DefaultFeatureConfig;
import net.minecraft.world.gen.feature.Feature;
import net.minecraft.world.gen.feature.util.FeatureContext;

public class GlowstoneBlobFeature
extends Feature<DefaultFeatureConfig> {
    public GlowstoneBlobFeature(Codec<DefaultFeatureConfig> codec) {
        super(codec);
    }

    @Override
    public boolean generate(FeatureContext<DefaultFeatureConfig> context) {
        StructureWorldAccess lv = context.getWorld();
        BlockPos lv2 = context.getOrigin();
        Random lv3 = context.getRandom();
        if (!lv.isAir(lv2)) {
            return false;
        }
        BlockState lv4 = lv.getBlockState(lv2.up());
        if (!(lv4.isOf(Blocks.NETHERRACK) || lv4.isOf(Blocks.BASALT) || lv4.isOf(Blocks.BLACKSTONE))) {
            return false;
        }
        lv.setBlockState(lv2, Blocks.GLOWSTONE.getDefaultState(), Block.NOTIFY_LISTENERS);
        for (int i = 0; i < 1500; ++i) {
            BlockPos lv5 = lv2.add(lv3.nextInt(8) - lv3.nextInt(8), -lv3.nextInt(12), lv3.nextInt(8) - lv3.nextInt(8));
            if (!lv.getBlockState(lv5).isAir()) continue;
            int j = 0;
            for (Direction lv6 : Direction.values()) {
                if (lv.getBlockState(lv5.offset(lv6)).isOf(Blocks.GLOWSTONE)) {
                    ++j;
                }
                if (j > 1) break;
            }
            if (j != true) continue;
            lv.setBlockState(lv5, Blocks.GLOWSTONE.getDefaultState(), Block.NOTIFY_LISTENERS);
        }
        return true;
    }
}

