/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.world.gen.feature;

import com.mojang.serialization.Codec;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.ServerWorldAccess;
import net.minecraft.world.gen.feature.DefaultFeatureConfig;
import net.minecraft.world.gen.feature.Feature;
import net.minecraft.world.gen.feature.util.FeatureContext;

public class EndPlatformFeature
extends Feature<DefaultFeatureConfig> {
    public EndPlatformFeature(Codec<DefaultFeatureConfig> codec) {
        super(codec);
    }

    @Override
    public boolean generate(FeatureContext<DefaultFeatureConfig> context) {
        EndPlatformFeature.generate(context.getWorld(), context.getOrigin(), false);
        return true;
    }

    public static void generate(ServerWorldAccess world, BlockPos pos, boolean breakBlocks) {
        BlockPos.Mutable lv = pos.mutableCopy();
        for (int i = -2; i <= 2; ++i) {
            for (int j = -2; j <= 2; ++j) {
                for (int k = -1; k < 3; ++k) {
                    Block lv3;
                    BlockPos.Mutable lv2 = lv.set(pos).move(j, k, i);
                    Block block = lv3 = k == -1 ? Blocks.OBSIDIAN : Blocks.AIR;
                    if (world.getBlockState(lv2).isOf(lv3)) continue;
                    if (breakBlocks) {
                        world.breakBlock(lv2, true, null);
                    }
                    world.setBlockState(lv2, lv3.getDefaultState(), Block.NOTIFY_ALL);
                }
            }
        }
    }
}

