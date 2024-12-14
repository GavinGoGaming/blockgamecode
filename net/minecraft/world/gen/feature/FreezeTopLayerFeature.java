/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.world.gen.feature;

import com.mojang.serialization.Codec;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.SnowyBlock;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.Heightmap;
import net.minecraft.world.StructureWorldAccess;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.gen.feature.DefaultFeatureConfig;
import net.minecraft.world.gen.feature.Feature;
import net.minecraft.world.gen.feature.util.FeatureContext;

public class FreezeTopLayerFeature
extends Feature<DefaultFeatureConfig> {
    public FreezeTopLayerFeature(Codec<DefaultFeatureConfig> codec) {
        super(codec);
    }

    @Override
    public boolean generate(FeatureContext<DefaultFeatureConfig> context) {
        StructureWorldAccess lv = context.getWorld();
        BlockPos lv2 = context.getOrigin();
        BlockPos.Mutable lv3 = new BlockPos.Mutable();
        BlockPos.Mutable lv4 = new BlockPos.Mutable();
        for (int i = 0; i < 16; ++i) {
            for (int j = 0; j < 16; ++j) {
                int k = lv2.getX() + i;
                int l = lv2.getZ() + j;
                int m = lv.getTopY(Heightmap.Type.MOTION_BLOCKING, k, l);
                lv3.set(k, m, l);
                lv4.set(lv3).move(Direction.DOWN, 1);
                Biome lv5 = lv.getBiome(lv3).value();
                if (lv5.canSetIce(lv, lv4, false)) {
                    lv.setBlockState(lv4, Blocks.ICE.getDefaultState(), Block.NOTIFY_LISTENERS);
                }
                if (!lv5.canSetSnow(lv, lv3)) continue;
                lv.setBlockState(lv3, Blocks.SNOW.getDefaultState(), Block.NOTIFY_LISTENERS);
                BlockState lv6 = lv.getBlockState(lv4);
                if (!lv6.contains(SnowyBlock.SNOWY)) continue;
                lv.setBlockState(lv4, (BlockState)lv6.with(SnowyBlock.SNOWY, true), Block.NOTIFY_LISTENERS);
            }
        }
        return true;
    }
}

