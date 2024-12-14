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
import net.minecraft.world.gen.feature.PlacedFeature;
import net.minecraft.world.gen.feature.SimpleRandomFeatureConfig;
import net.minecraft.world.gen.feature.util.FeatureContext;

public class SimpleRandomFeature
extends Feature<SimpleRandomFeatureConfig> {
    public SimpleRandomFeature(Codec<SimpleRandomFeatureConfig> codec) {
        super(codec);
    }

    @Override
    public boolean generate(FeatureContext<SimpleRandomFeatureConfig> context) {
        Random lv = context.getRandom();
        SimpleRandomFeatureConfig lv2 = context.getConfig();
        StructureWorldAccess lv3 = context.getWorld();
        BlockPos lv4 = context.getOrigin();
        ChunkGenerator lv5 = context.getGenerator();
        int i = lv.nextInt(lv2.features.size());
        PlacedFeature lv6 = lv2.features.get(i).value();
        return lv6.generateUnregistered(lv3, lv5, lv, lv4);
    }
}

