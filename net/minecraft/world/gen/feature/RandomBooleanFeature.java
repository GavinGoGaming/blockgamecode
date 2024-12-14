/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.world.gen.feature;

import com.mojang.serialization.Codec;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.StructureWorldAccess;
import net.minecraft.world.gen.chunk.ChunkGenerator;
import net.minecraft.world.gen.feature.Feature;
import net.minecraft.world.gen.feature.RandomBooleanFeatureConfig;
import net.minecraft.world.gen.feature.util.FeatureContext;

public class RandomBooleanFeature
extends Feature<RandomBooleanFeatureConfig> {
    public RandomBooleanFeature(Codec<RandomBooleanFeatureConfig> codec) {
        super(codec);
    }

    @Override
    public boolean generate(FeatureContext<RandomBooleanFeatureConfig> context) {
        Random lv = context.getRandom();
        RandomBooleanFeatureConfig lv2 = context.getConfig();
        StructureWorldAccess lv3 = context.getWorld();
        ChunkGenerator lv4 = context.getGenerator();
        BlockPos lv5 = context.getOrigin();
        boolean bl = lv.nextBoolean();
        return (bl ? lv2.featureTrue : lv2.featureFalse).value().generateUnregistered(lv3, lv4, lv, lv5);
    }
}

