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

public class BlueIceFeature
extends Feature<DefaultFeatureConfig> {
    public BlueIceFeature(Codec<DefaultFeatureConfig> codec) {
        super(codec);
    }

    @Override
    public boolean generate(FeatureContext<DefaultFeatureConfig> context) {
        BlockPos lv = context.getOrigin();
        StructureWorldAccess lv2 = context.getWorld();
        Random lv3 = context.getRandom();
        if (lv.getY() > lv2.getSeaLevel() - 1) {
            return false;
        }
        if (!lv2.getBlockState(lv).isOf(Blocks.WATER) && !lv2.getBlockState(lv.down()).isOf(Blocks.WATER)) {
            return false;
        }
        boolean bl = false;
        for (Direction lv4 : Direction.values()) {
            if (lv4 == Direction.DOWN || !lv2.getBlockState(lv.offset(lv4)).isOf(Blocks.PACKED_ICE)) continue;
            bl = true;
            break;
        }
        if (!bl) {
            return false;
        }
        lv2.setBlockState(lv, Blocks.BLUE_ICE.getDefaultState(), Block.NOTIFY_LISTENERS);
        block1: for (int i = 0; i < 200; ++i) {
            BlockPos lv5;
            BlockState lv6;
            int j = lv3.nextInt(5) - lv3.nextInt(6);
            int k = 3;
            if (j < 2) {
                k += j / 2;
            }
            if (k < 1 || !(lv6 = lv2.getBlockState(lv5 = lv.add(lv3.nextInt(k) - lv3.nextInt(k), j, lv3.nextInt(k) - lv3.nextInt(k)))).isAir() && !lv6.isOf(Blocks.WATER) && !lv6.isOf(Blocks.PACKED_ICE) && !lv6.isOf(Blocks.ICE)) continue;
            for (Direction lv7 : Direction.values()) {
                BlockState lv8 = lv2.getBlockState(lv5.offset(lv7));
                if (!lv8.isOf(Blocks.BLUE_ICE)) continue;
                lv2.setBlockState(lv5, Blocks.BLUE_ICE.getDefaultState(), Block.NOTIFY_LISTENERS);
                continue block1;
            }
        }
        return true;
    }
}

