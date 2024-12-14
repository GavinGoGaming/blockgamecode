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
import net.minecraft.world.gen.feature.RandomFeatureConfig;
import net.minecraft.world.gen.feature.RandomFeatureEntry;
import net.minecraft.world.gen.feature.util.FeatureContext;

public class RandomFeature
extends Feature<RandomFeatureConfig> {
    public RandomFeature(Codec<RandomFeatureConfig> codec) {
        super(codec);
    }

    @Override
    public boolean generate(FeatureContext<RandomFeatureConfig> context) {
        RandomFeatureConfig lv = context.getConfig();
        Random lv2 = context.getRandom();
        StructureWorldAccess lv3 = context.getWorld();
        ChunkGenerator lv4 = context.getGenerator();
        BlockPos lv5 = context.getOrigin();
        for (RandomFeatureEntry lv6 : lv.features) {
            if (!(lv2.nextFloat() < lv6.chance)) continue;
            return lv6.generate(lv3, lv4, lv2, lv5);
        }
        return lv.defaultFeature.value().generateUnregistered(lv3, lv4, lv2, lv5);
    }
}

