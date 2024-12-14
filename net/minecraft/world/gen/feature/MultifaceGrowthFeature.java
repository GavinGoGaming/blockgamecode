/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.world.gen.feature;

import com.mojang.serialization.Codec;
import java.util.List;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3i;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.StructureWorldAccess;
import net.minecraft.world.WorldAccess;
import net.minecraft.world.gen.feature.Feature;
import net.minecraft.world.gen.feature.MultifaceGrowthFeatureConfig;
import net.minecraft.world.gen.feature.util.FeatureContext;

public class MultifaceGrowthFeature
extends Feature<MultifaceGrowthFeatureConfig> {
    public MultifaceGrowthFeature(Codec<MultifaceGrowthFeatureConfig> codec) {
        super(codec);
    }

    @Override
    public boolean generate(FeatureContext<MultifaceGrowthFeatureConfig> context) {
        StructureWorldAccess lv = context.getWorld();
        BlockPos lv2 = context.getOrigin();
        Random lv3 = context.getRandom();
        MultifaceGrowthFeatureConfig lv4 = context.getConfig();
        if (!MultifaceGrowthFeature.isAirOrWater(lv.getBlockState(lv2))) {
            return false;
        }
        List<Direction> list = lv4.shuffleDirections(lv3);
        if (MultifaceGrowthFeature.generate(lv, lv2, lv.getBlockState(lv2), lv4, lv3, list)) {
            return true;
        }
        BlockPos.Mutable lv5 = lv2.mutableCopy();
        block0: for (Direction lv6 : list) {
            lv5.set(lv2);
            List<Direction> list2 = lv4.shuffleDirections(lv3, lv6.getOpposite());
            for (int i = 0; i < lv4.searchRange; ++i) {
                lv5.set((Vec3i)lv2, lv6);
                BlockState lv7 = lv.getBlockState(lv5);
                if (!MultifaceGrowthFeature.isAirOrWater(lv7) && !lv7.isOf(lv4.lichen)) continue block0;
                if (!MultifaceGrowthFeature.generate(lv, lv5, lv7, lv4, lv3, list2)) continue;
                return true;
            }
        }
        return false;
    }

    public static boolean generate(StructureWorldAccess world, BlockPos pos, BlockState state, MultifaceGrowthFeatureConfig config, Random random, List<Direction> directions) {
        BlockPos.Mutable lv = pos.mutableCopy();
        for (Direction lv2 : directions) {
            BlockState lv3 = world.getBlockState(lv.set((Vec3i)pos, lv2));
            if (!lv3.isIn(config.canPlaceOn)) continue;
            BlockState lv4 = config.lichen.withDirection(state, world, pos, lv2);
            if (lv4 == null) {
                return false;
            }
            world.setBlockState(pos, lv4, Block.NOTIFY_ALL);
            world.getChunk(pos).markBlockForPostProcessing(pos);
            if (random.nextFloat() < config.spreadChance) {
                config.lichen.getGrower().grow(lv4, (WorldAccess)world, pos, lv2, random, true);
            }
            return true;
        }
        return false;
    }

    private static boolean isAirOrWater(BlockState state) {
        return state.isAir() || state.isOf(Blocks.WATER);
    }
}

