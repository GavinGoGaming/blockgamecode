/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.world.gen.feature;

import com.mojang.serialization.Codec;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.VineBlock;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.StructureWorldAccess;
import net.minecraft.world.gen.feature.DefaultFeatureConfig;
import net.minecraft.world.gen.feature.Feature;
import net.minecraft.world.gen.feature.util.FeatureContext;

public class VinesFeature
extends Feature<DefaultFeatureConfig> {
    public VinesFeature(Codec<DefaultFeatureConfig> codec) {
        super(codec);
    }

    @Override
    public boolean generate(FeatureContext<DefaultFeatureConfig> context) {
        StructureWorldAccess lv = context.getWorld();
        BlockPos lv2 = context.getOrigin();
        context.getConfig();
        if (!lv.isAir(lv2)) {
            return false;
        }
        for (Direction lv3 : Direction.values()) {
            if (lv3 == Direction.DOWN || !VineBlock.shouldConnectTo(lv, lv2.offset(lv3), lv3)) continue;
            lv.setBlockState(lv2, (BlockState)Blocks.VINE.getDefaultState().with(VineBlock.getFacingProperty(lv3), true), Block.NOTIFY_LISTENERS);
            return true;
        }
        return false;
    }
}

