/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.world.gen.feature;

import com.mojang.serialization.Codec;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.StructureWorldAccess;
import net.minecraft.world.gen.feature.DefaultFeatureConfig;
import net.minecraft.world.gen.feature.Feature;
import net.minecraft.world.gen.feature.util.FeatureContext;

public class VoidStartPlatformFeature
extends Feature<DefaultFeatureConfig> {
    private static final BlockPos START_BLOCK = new BlockPos(8, 3, 8);
    private static final ChunkPos START_CHUNK = new ChunkPos(START_BLOCK);
    private static final int MAX_RADIUS = 16;
    private static final int field_31521 = 1;

    public VoidStartPlatformFeature(Codec<DefaultFeatureConfig> codec) {
        super(codec);
    }

    private static int getDistance(int x1, int z1, int x2, int z2) {
        return Math.max(Math.abs(x1 - x2), Math.abs(z1 - z2));
    }

    @Override
    public boolean generate(FeatureContext<DefaultFeatureConfig> context) {
        StructureWorldAccess lv = context.getWorld();
        ChunkPos lv2 = new ChunkPos(context.getOrigin());
        if (VoidStartPlatformFeature.getDistance(lv2.x, lv2.z, VoidStartPlatformFeature.START_CHUNK.x, VoidStartPlatformFeature.START_CHUNK.z) > 1) {
            return true;
        }
        BlockPos lv3 = START_BLOCK.withY(context.getOrigin().getY() + START_BLOCK.getY());
        BlockPos.Mutable lv4 = new BlockPos.Mutable();
        for (int i = lv2.getStartZ(); i <= lv2.getEndZ(); ++i) {
            for (int j = lv2.getStartX(); j <= lv2.getEndX(); ++j) {
                if (VoidStartPlatformFeature.getDistance(lv3.getX(), lv3.getZ(), j, i) > 16) continue;
                lv4.set(j, lv3.getY(), i);
                if (lv4.equals(lv3)) {
                    lv.setBlockState(lv4, Blocks.COBBLESTONE.getDefaultState(), Block.NOTIFY_LISTENERS);
                    continue;
                }
                lv.setBlockState(lv4, Blocks.STONE.getDefaultState(), Block.NOTIFY_LISTENERS);
            }
        }
        return true;
    }
}

