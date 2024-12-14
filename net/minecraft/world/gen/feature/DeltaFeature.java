/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
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
import net.minecraft.world.gen.feature.DeltaFeatureConfig;
import net.minecraft.world.gen.feature.Feature;
import net.minecraft.world.gen.feature.util.FeatureContext;

public class DeltaFeature
extends Feature<DeltaFeatureConfig> {
    private static final ImmutableList<Block> CANNOT_REPLACE_BLOCKS = ImmutableList.of(Blocks.BEDROCK, Blocks.NETHER_BRICKS, Blocks.NETHER_BRICK_FENCE, Blocks.NETHER_BRICK_STAIRS, Blocks.NETHER_WART, Blocks.CHEST, Blocks.SPAWNER);
    private static final Direction[] DIRECTIONS = Direction.values();
    private static final double field_31501 = 0.9;

    public DeltaFeature(Codec<DeltaFeatureConfig> codec) {
        super(codec);
    }

    @Override
    public boolean generate(FeatureContext<DeltaFeatureConfig> context) {
        boolean bl = false;
        Random lv = context.getRandom();
        StructureWorldAccess lv2 = context.getWorld();
        DeltaFeatureConfig lv3 = context.getConfig();
        BlockPos lv4 = context.getOrigin();
        boolean bl2 = lv.nextDouble() < 0.9;
        int i = bl2 ? lv3.getRimSize().get(lv) : 0;
        int j = bl2 ? lv3.getRimSize().get(lv) : 0;
        boolean bl3 = bl2 && i != 0 && j != 0;
        int k = lv3.getSize().get(lv);
        int l = lv3.getSize().get(lv);
        int m = Math.max(k, l);
        for (BlockPos lv5 : BlockPos.iterateOutwards(lv4, k, 0, l)) {
            BlockPos lv6;
            if (lv5.getManhattanDistance(lv4) > m) break;
            if (!DeltaFeature.canPlace(lv2, lv5, lv3)) continue;
            if (bl3) {
                bl = true;
                this.setBlockState(lv2, lv5, lv3.getRim());
            }
            if (!DeltaFeature.canPlace(lv2, lv6 = lv5.add(i, 0, j), lv3)) continue;
            bl = true;
            this.setBlockState(lv2, lv6, lv3.getContents());
        }
        return bl;
    }

    private static boolean canPlace(WorldAccess world, BlockPos pos, DeltaFeatureConfig config) {
        BlockState lv = world.getBlockState(pos);
        if (lv.isOf(config.getContents().getBlock())) {
            return false;
        }
        if (CANNOT_REPLACE_BLOCKS.contains(lv.getBlock())) {
            return false;
        }
        for (Direction lv2 : DIRECTIONS) {
            boolean bl = world.getBlockState(pos.offset(lv2)).isAir();
            if ((!bl || lv2 == Direction.UP) && (bl || lv2 != Direction.UP)) continue;
            return false;
        }
        return true;
    }
}

