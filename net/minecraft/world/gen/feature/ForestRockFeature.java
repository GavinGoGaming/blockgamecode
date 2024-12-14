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
import net.minecraft.world.gen.feature.SingleStateFeatureConfig;
import net.minecraft.world.gen.feature.util.FeatureContext;

public class ForestRockFeature
extends Feature<SingleStateFeatureConfig> {
    public ForestRockFeature(Codec<SingleStateFeatureConfig> codec) {
        super(codec);
    }

    @Override
    public boolean generate(FeatureContext<SingleStateFeatureConfig> context) {
        BlockState lv5;
        BlockPos lv = context.getOrigin();
        StructureWorldAccess lv2 = context.getWorld();
        Random lv3 = context.getRandom();
        SingleStateFeatureConfig lv4 = context.getConfig();
        while (lv.getY() > lv2.getBottomY() + 3 && (lv2.isAir(lv.down()) || !ForestRockFeature.isSoil(lv5 = lv2.getBlockState(lv.down())) && !ForestRockFeature.isStone(lv5))) {
            lv = lv.down();
        }
        if (lv.getY() <= lv2.getBottomY() + 3) {
            return false;
        }
        for (int i = 0; i < 3; ++i) {
            int j = lv3.nextInt(2);
            int k = lv3.nextInt(2);
            int l = lv3.nextInt(2);
            float f = (float)(j + k + l) * 0.333f + 0.5f;
            for (BlockPos lv6 : BlockPos.iterate(lv.add(-j, -k, -l), lv.add(j, k, l))) {
                if (!(lv6.getSquaredDistance(lv) <= (double)(f * f))) continue;
                lv2.setBlockState(lv6, lv4.state, Block.NOTIFY_ALL);
            }
            lv = lv.add(-1 + lv3.nextInt(2), -lv3.nextInt(2), -1 + lv3.nextInt(2));
        }
        return true;
    }
}

