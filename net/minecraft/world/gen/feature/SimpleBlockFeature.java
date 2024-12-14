/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.world.gen.feature;

import com.mojang.serialization.Codec;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.TallPlantBlock;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.StructureWorldAccess;
import net.minecraft.world.gen.feature.Feature;
import net.minecraft.world.gen.feature.SimpleBlockFeatureConfig;
import net.minecraft.world.gen.feature.util.FeatureContext;

public class SimpleBlockFeature
extends Feature<SimpleBlockFeatureConfig> {
    public SimpleBlockFeature(Codec<SimpleBlockFeatureConfig> codec) {
        super(codec);
    }

    /*
     * Enabled force condition propagation
     * Lifted jumps to return sites
     */
    @Override
    public boolean generate(FeatureContext<SimpleBlockFeatureConfig> context) {
        SimpleBlockFeatureConfig lv = context.getConfig();
        StructureWorldAccess lv2 = context.getWorld();
        BlockPos lv3 = context.getOrigin();
        BlockState lv4 = lv.toPlace().get(context.getRandom(), lv3);
        if (!lv4.canPlaceAt(lv2, lv3)) return false;
        if (lv4.getBlock() instanceof TallPlantBlock) {
            if (!lv2.isAir(lv3.up())) return false;
            TallPlantBlock.placeAt(lv2, lv4, lv3, 2);
            return true;
        } else {
            lv2.setBlockState(lv3, lv4, Block.NOTIFY_LISTENERS);
        }
        return true;
    }
}

