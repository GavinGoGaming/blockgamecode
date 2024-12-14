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
import net.minecraft.world.gen.feature.DiskFeatureConfig;
import net.minecraft.world.gen.feature.Feature;
import net.minecraft.world.gen.feature.util.FeatureContext;

public class DiskFeature
extends Feature<DiskFeatureConfig> {
    public DiskFeature(Codec<DiskFeatureConfig> codec) {
        super(codec);
    }

    @Override
    public boolean generate(FeatureContext<DiskFeatureConfig> context) {
        DiskFeatureConfig lv = context.getConfig();
        BlockPos lv2 = context.getOrigin();
        StructureWorldAccess lv3 = context.getWorld();
        Random lv4 = context.getRandom();
        boolean bl = false;
        int i = lv2.getY();
        int j = i + lv.halfHeight();
        int k = i - lv.halfHeight() - 1;
        int l = lv.radius().get(lv4);
        BlockPos.Mutable lv5 = new BlockPos.Mutable();
        for (BlockPos lv6 : BlockPos.iterate(lv2.add(-l, 0, -l), lv2.add(l, 0, l))) {
            int n;
            int m = lv6.getX() - lv2.getX();
            if (m * m + (n = lv6.getZ() - lv2.getZ()) * n > l * l) continue;
            bl |= this.placeBlock(lv, lv3, lv4, j, k, lv5.set(lv6));
        }
        return bl;
    }

    protected boolean placeBlock(DiskFeatureConfig config, StructureWorldAccess world, Random random, int topY, int bottomY, BlockPos.Mutable pos) {
        boolean bl = false;
        for (int k = topY; k > bottomY; --k) {
            pos.setY(k);
            if (!config.target().test(world, pos)) continue;
            BlockState lv = config.stateProvider().getBlockState(world, random, pos);
            world.setBlockState(pos, lv, Block.NOTIFY_LISTENERS);
            this.markBlocksAboveForPostProcessing(world, pos);
            bl = true;
        }
        return bl;
    }
}

