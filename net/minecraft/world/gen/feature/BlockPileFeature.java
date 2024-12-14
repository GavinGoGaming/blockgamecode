/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.world.gen.feature;

import com.mojang.serialization.Codec;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.StructureWorldAccess;
import net.minecraft.world.WorldAccess;
import net.minecraft.world.gen.feature.BlockPileFeatureConfig;
import net.minecraft.world.gen.feature.Feature;
import net.minecraft.world.gen.feature.util.FeatureContext;

public class BlockPileFeature
extends Feature<BlockPileFeatureConfig> {
    public BlockPileFeature(Codec<BlockPileFeatureConfig> codec) {
        super(codec);
    }

    @Override
    public boolean generate(FeatureContext<BlockPileFeatureConfig> context) {
        BlockPos lv = context.getOrigin();
        StructureWorldAccess lv2 = context.getWorld();
        Random lv3 = context.getRandom();
        BlockPileFeatureConfig lv4 = context.getConfig();
        if (lv.getY() < lv2.getBottomY() + 5) {
            return false;
        }
        int i = 2 + lv3.nextInt(2);
        int j = 2 + lv3.nextInt(2);
        for (BlockPos lv5 : BlockPos.iterate(lv.add(-i, 0, -j), lv.add(i, 1, j))) {
            int l;
            int k = lv.getX() - lv5.getX();
            if ((float)(k * k + (l = lv.getZ() - lv5.getZ()) * l) <= lv3.nextFloat() * 10.0f - lv3.nextFloat() * 6.0f) {
                this.addPileBlock(lv2, lv5, lv3, lv4);
                continue;
            }
            if (!((double)lv3.nextFloat() < 0.031)) continue;
            this.addPileBlock(lv2, lv5, lv3, lv4);
        }
        return true;
    }

    private boolean canPlace(WorldAccess world, BlockPos pos, Random random) {
        BlockPos lv = pos.down();
        BlockState lv2 = world.getBlockState(lv);
        if (lv2.isOf(Blocks.DIRT_PATH)) {
            return random.nextBoolean();
        }
        return lv2.isSideSolidFullSquare(world, lv, Direction.UP);
    }

    private void addPileBlock(WorldAccess world, BlockPos pos, Random random, BlockPileFeatureConfig config) {
        if (world.isAir(pos) && this.canPlace(world, pos, random)) {
            world.setBlockState(pos, config.stateProvider.get(random, pos), Block.NO_REDRAW);
        }
    }
}

