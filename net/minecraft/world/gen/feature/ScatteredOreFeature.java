/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.world.gen.feature;

import com.mojang.serialization.Codec;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.StructureWorldAccess;
import net.minecraft.world.gen.feature.Feature;
import net.minecraft.world.gen.feature.OreFeature;
import net.minecraft.world.gen.feature.OreFeatureConfig;
import net.minecraft.world.gen.feature.util.FeatureContext;

public class ScatteredOreFeature
extends Feature<OreFeatureConfig> {
    private static final int MAX_SPREAD = 7;

    ScatteredOreFeature(Codec<OreFeatureConfig> codec) {
        super(codec);
    }

    @Override
    public boolean generate(FeatureContext<OreFeatureConfig> context) {
        StructureWorldAccess lv = context.getWorld();
        Random lv2 = context.getRandom();
        OreFeatureConfig lv3 = context.getConfig();
        BlockPos lv4 = context.getOrigin();
        int i = lv2.nextInt(lv3.size + 1);
        BlockPos.Mutable lv5 = new BlockPos.Mutable();
        block0: for (int j = 0; j < i; ++j) {
            this.setPos(lv5, lv2, lv4, Math.min(j, 7));
            BlockState lv6 = lv.getBlockState(lv5);
            for (OreFeatureConfig.Target lv7 : lv3.targets) {
                if (!OreFeature.shouldPlace(lv6, lv::getBlockState, lv2, lv3, lv7, lv5)) continue;
                lv.setBlockState(lv5, lv7.state, Block.NOTIFY_LISTENERS);
                continue block0;
            }
        }
        return true;
    }

    private void setPos(BlockPos.Mutable mutable, Random random, BlockPos origin, int spread) {
        int j = this.getSpread(random, spread);
        int k = this.getSpread(random, spread);
        int l = this.getSpread(random, spread);
        mutable.set(origin, j, k, l);
    }

    private int getSpread(Random random, int spread) {
        return Math.round((random.nextFloat() - random.nextFloat()) * (float)spread);
    }
}

