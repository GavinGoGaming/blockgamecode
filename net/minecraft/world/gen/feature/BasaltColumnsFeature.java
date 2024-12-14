/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 * 
 * Could not load the following classes:
 *  org.jetbrains.annotations.Nullable
 */
package net.minecraft.world.gen.feature;

import com.google.common.collect.ImmutableList;
import com.mojang.serialization.Codec;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.StructureWorldAccess;
import net.minecraft.world.WorldAccess;
import net.minecraft.world.gen.feature.BasaltColumnsFeatureConfig;
import net.minecraft.world.gen.feature.Feature;
import net.minecraft.world.gen.feature.util.FeatureContext;
import org.jetbrains.annotations.Nullable;

public class BasaltColumnsFeature
extends Feature<BasaltColumnsFeatureConfig> {
    private static final ImmutableList<Block> CANNOT_REPLACE_BLOCKS = ImmutableList.of(Blocks.LAVA, Blocks.BEDROCK, Blocks.MAGMA_BLOCK, Blocks.SOUL_SAND, Blocks.NETHER_BRICKS, Blocks.NETHER_BRICK_FENCE, Blocks.NETHER_BRICK_STAIRS, Blocks.NETHER_WART, Blocks.CHEST, Blocks.SPAWNER);
    private static final int field_31495 = 5;
    private static final int field_31496 = 50;
    private static final int field_31497 = 8;
    private static final int field_31498 = 15;

    public BasaltColumnsFeature(Codec<BasaltColumnsFeatureConfig> codec) {
        super(codec);
    }

    @Override
    public boolean generate(FeatureContext<BasaltColumnsFeatureConfig> context) {
        int i = context.getGenerator().getSeaLevel();
        BlockPos lv = context.getOrigin();
        StructureWorldAccess lv2 = context.getWorld();
        Random lv3 = context.getRandom();
        BasaltColumnsFeatureConfig lv4 = context.getConfig();
        if (!BasaltColumnsFeature.canPlaceAt(lv2, i, lv.mutableCopy())) {
            return false;
        }
        int j = lv4.getHeight().get(lv3);
        boolean bl = lv3.nextFloat() < 0.9f;
        int k = Math.min(j, bl ? 5 : 8);
        int l = bl ? 50 : 15;
        boolean bl2 = false;
        for (BlockPos lv5 : BlockPos.iterateRandomly(lv3, l, lv.getX() - k, lv.getY(), lv.getZ() - k, lv.getX() + k, lv.getY(), lv.getZ() + k)) {
            int m = j - lv5.getManhattanDistance(lv);
            if (m < 0) continue;
            bl2 |= this.placeBasaltColumn(lv2, i, lv5, m, lv4.getReach().get(lv3));
        }
        return bl2;
    }

    private boolean placeBasaltColumn(WorldAccess world, int seaLevel, BlockPos pos, int height, int reach) {
        boolean bl = false;
        block0: for (BlockPos lv : BlockPos.iterate(pos.getX() - reach, pos.getY(), pos.getZ() - reach, pos.getX() + reach, pos.getY(), pos.getZ() + reach)) {
            BlockPos lv2;
            int l = lv.getManhattanDistance(pos);
            BlockPos blockPos = lv2 = BasaltColumnsFeature.isAirOrLavaOcean(world, seaLevel, lv) ? BasaltColumnsFeature.moveDownToGround(world, seaLevel, lv.mutableCopy(), l) : BasaltColumnsFeature.moveUpToAir(world, lv.mutableCopy(), l);
            if (lv2 == null) continue;
            BlockPos.Mutable lv3 = lv2.mutableCopy();
            for (int m = height - l / 2; m >= 0; --m) {
                if (BasaltColumnsFeature.isAirOrLavaOcean(world, seaLevel, lv3)) {
                    this.setBlockState(world, lv3, Blocks.BASALT.getDefaultState());
                    lv3.move(Direction.UP);
                    bl = true;
                    continue;
                }
                if (!world.getBlockState(lv3).isOf(Blocks.BASALT)) continue block0;
                lv3.move(Direction.UP);
            }
        }
        return bl;
    }

    @Nullable
    private static BlockPos moveDownToGround(WorldAccess world, int seaLevel, BlockPos.Mutable mutablePos, int distance) {
        while (mutablePos.getY() > world.getBottomY() + 1 && distance > 0) {
            --distance;
            if (BasaltColumnsFeature.canPlaceAt(world, seaLevel, mutablePos)) {
                return mutablePos;
            }
            mutablePos.move(Direction.DOWN);
        }
        return null;
    }

    private static boolean canPlaceAt(WorldAccess world, int seaLevel, BlockPos.Mutable mutablePos) {
        if (BasaltColumnsFeature.isAirOrLavaOcean(world, seaLevel, mutablePos)) {
            BlockState lv = world.getBlockState(mutablePos.move(Direction.DOWN));
            mutablePos.move(Direction.UP);
            return !lv.isAir() && !CANNOT_REPLACE_BLOCKS.contains(lv.getBlock());
        }
        return false;
    }

    @Nullable
    private static BlockPos moveUpToAir(WorldAccess world, BlockPos.Mutable mutablePos, int distance) {
        while (mutablePos.getY() < world.getTopY() && distance > 0) {
            --distance;
            BlockState lv = world.getBlockState(mutablePos);
            if (CANNOT_REPLACE_BLOCKS.contains(lv.getBlock())) {
                return null;
            }
            if (lv.isAir()) {
                return mutablePos;
            }
            mutablePos.move(Direction.UP);
        }
        return null;
    }

    private static boolean isAirOrLavaOcean(WorldAccess world, int seaLevel, BlockPos pos) {
        BlockState lv = world.getBlockState(pos);
        return lv.isAir() || lv.isOf(Blocks.LAVA) && pos.getY() <= seaLevel;
    }
}

