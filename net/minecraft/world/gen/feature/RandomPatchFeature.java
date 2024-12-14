/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.world.gen.feature;

import com.mojang.serialization.Codec;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.StructureWorldAccess;
import net.minecraft.world.gen.feature.Feature;
import net.minecraft.world.gen.feature.RandomPatchFeatureConfig;
import net.minecraft.world.gen.feature.util.FeatureContext;

public class RandomPatchFeature
extends Feature<RandomPatchFeatureConfig> {
    public RandomPatchFeature(Codec<RandomPatchFeatureConfig> codec) {
        super(codec);
    }

    @Override
    public boolean generate(FeatureContext<RandomPatchFeatureConfig> context) {
        RandomPatchFeatureConfig lv = context.getConfig();
        Random lv2 = context.getRandom();
        BlockPos lv3 = context.getOrigin();
        StructureWorldAccess lv4 = context.getWorld();
        int i = 0;
        BlockPos.Mutable lv5 = new BlockPos.Mutable();
        int j = lv.xzSpread() + 1;
        int k = lv.ySpread() + 1;
        for (int l = 0; l < lv.tries(); ++l) {
            lv5.set(lv3, lv2.nextInt(j) - lv2.nextInt(j), lv2.nextInt(k) - lv2.nextInt(k), lv2.nextInt(j) - lv2.nextInt(j));
            if (!lv.feature().value().generateUnregistered(lv4, context.getGenerator(), lv2, lv5)) continue;
            ++i;
        }
        return i > 0;
    }
}

