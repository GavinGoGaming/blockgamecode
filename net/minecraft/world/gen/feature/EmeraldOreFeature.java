/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.world.gen.feature;

import com.mojang.serialization.Codec;
import net.minecraft.block.Block;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.StructureWorldAccess;
import net.minecraft.world.gen.feature.EmeraldOreFeatureConfig;
import net.minecraft.world.gen.feature.Feature;
import net.minecraft.world.gen.feature.OreFeatureConfig;
import net.minecraft.world.gen.feature.util.FeatureContext;

public class EmeraldOreFeature
extends Feature<EmeraldOreFeatureConfig> {
    public EmeraldOreFeature(Codec<EmeraldOreFeatureConfig> codec) {
        super(codec);
    }

    @Override
    public boolean generate(FeatureContext<EmeraldOreFeatureConfig> context) {
        StructureWorldAccess lv = context.getWorld();
        BlockPos lv2 = context.getOrigin();
        EmeraldOreFeatureConfig lv3 = context.getConfig();
        for (OreFeatureConfig.Target lv4 : lv3.targets) {
            if (!lv4.target.test(lv.getBlockState(lv2), context.getRandom())) continue;
            lv.setBlockState(lv2, lv4.state, Block.NOTIFY_LISTENERS);
            break;
        }
        return true;
    }
}

