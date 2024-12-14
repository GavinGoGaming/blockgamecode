/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.world.gen.feature;

import com.mojang.serialization.Codec;
import net.minecraft.block.Block;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.StructureWorldAccess;
import net.minecraft.world.gen.feature.Feature;
import net.minecraft.world.gen.feature.FillLayerFeatureConfig;
import net.minecraft.world.gen.feature.util.FeatureContext;

public class FillLayerFeature
extends Feature<FillLayerFeatureConfig> {
    public FillLayerFeature(Codec<FillLayerFeatureConfig> codec) {
        super(codec);
    }

    @Override
    public boolean generate(FeatureContext<FillLayerFeatureConfig> context) {
        BlockPos lv = context.getOrigin();
        FillLayerFeatureConfig lv2 = context.getConfig();
        StructureWorldAccess lv3 = context.getWorld();
        BlockPos.Mutable lv4 = new BlockPos.Mutable();
        for (int i = 0; i < 16; ++i) {
            for (int j = 0; j < 16; ++j) {
                int k = lv.getX() + i;
                int l = lv.getZ() + j;
                int m = lv3.getBottomY() + lv2.height;
                lv4.set(k, m, l);
                if (!lv3.getBlockState(lv4).isAir()) continue;
                lv3.setBlockState(lv4, lv2.state, Block.NOTIFY_LISTENERS);
            }
        }
        return true;
    }
}

