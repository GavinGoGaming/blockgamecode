/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.world.gen.feature;

import com.mojang.serialization.Codec;
import java.util.function.Predicate;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.registry.tag.FluidTags;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3i;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.StructureWorldAccess;
import net.minecraft.world.gen.chunk.ChunkGenerator;
import net.minecraft.world.gen.feature.Feature;
import net.minecraft.world.gen.feature.RootSystemFeatureConfig;
import net.minecraft.world.gen.feature.util.FeatureContext;

public class RootSystemFeature
extends Feature<RootSystemFeatureConfig> {
    public RootSystemFeature(Codec<RootSystemFeatureConfig> codec) {
        super(codec);
    }

    @Override
    public boolean generate(FeatureContext<RootSystemFeatureConfig> context) {
        BlockPos lv2;
        StructureWorldAccess lv = context.getWorld();
        if (!lv.getBlockState(lv2 = context.getOrigin()).isAir()) {
            return false;
        }
        Random lv3 = context.getRandom();
        BlockPos lv4 = context.getOrigin();
        RootSystemFeatureConfig lv5 = context.getConfig();
        BlockPos.Mutable lv6 = lv4.mutableCopy();
        if (RootSystemFeature.generateTreeAndRoots(lv, context.getGenerator(), lv5, lv3, lv6, lv4)) {
            RootSystemFeature.generateHangingRoots(lv, lv5, lv3, lv4, lv6);
        }
        return true;
    }

    private static boolean hasSpaceForTree(StructureWorldAccess world, RootSystemFeatureConfig config, BlockPos pos) {
        BlockPos.Mutable lv = pos.mutableCopy();
        for (int i = 1; i <= config.requiredVerticalSpaceForTree; ++i) {
            lv.move(Direction.UP);
            BlockState lv2 = world.getBlockState(lv);
            if (RootSystemFeature.isAirOrWater(lv2, i, config.allowedVerticalWaterForTree)) continue;
            return false;
        }
        return true;
    }

    private static boolean isAirOrWater(BlockState state, int height, int allowedVerticalWaterForTree) {
        if (state.isAir()) {
            return true;
        }
        int k = height + 1;
        return k <= allowedVerticalWaterForTree && state.getFluidState().isIn(FluidTags.WATER);
    }

    private static boolean generateTreeAndRoots(StructureWorldAccess world, ChunkGenerator generator, RootSystemFeatureConfig config, Random random, BlockPos.Mutable mutablePos, BlockPos pos) {
        for (int i = 0; i < config.maxRootColumnHeight; ++i) {
            mutablePos.move(Direction.UP);
            if (!config.predicate.test(world, mutablePos) || !RootSystemFeature.hasSpaceForTree(world, config, mutablePos)) continue;
            Vec3i lv = mutablePos.down();
            if (world.getFluidState((BlockPos)lv).isIn(FluidTags.LAVA) || !world.getBlockState((BlockPos)lv).isSolid()) {
                return false;
            }
            if (!config.feature.value().generateUnregistered(world, generator, random, mutablePos)) continue;
            RootSystemFeature.generateRootsColumn(pos, pos.getY() + i, world, config, random);
            return true;
        }
        return false;
    }

    private static void generateRootsColumn(BlockPos pos, int maxY, StructureWorldAccess world, RootSystemFeatureConfig config, Random random) {
        int j = pos.getX();
        int k = pos.getZ();
        BlockPos.Mutable lv = pos.mutableCopy();
        for (int l = pos.getY(); l < maxY; ++l) {
            RootSystemFeature.generateRoots(world, config, random, j, k, lv.set(j, l, k));
        }
    }

    private static void generateRoots(StructureWorldAccess world, RootSystemFeatureConfig config, Random random, int x, int z, BlockPos.Mutable mutablePos) {
        int k = config.rootRadius;
        Predicate<BlockState> predicate = state -> state.isIn(arg.rootReplaceable);
        for (int l = 0; l < config.rootPlacementAttempts; ++l) {
            mutablePos.set(mutablePos, random.nextInt(k) - random.nextInt(k), 0, random.nextInt(k) - random.nextInt(k));
            if (predicate.test(world.getBlockState(mutablePos))) {
                world.setBlockState(mutablePos, config.rootStateProvider.get(random, mutablePos), Block.NOTIFY_LISTENERS);
            }
            mutablePos.setX(x);
            mutablePos.setZ(z);
        }
    }

    private static void generateHangingRoots(StructureWorldAccess world, RootSystemFeatureConfig config, Random random, BlockPos pos, BlockPos.Mutable mutablePos) {
        int i = config.hangingRootRadius;
        int j = config.hangingRootVerticalSpan;
        for (int k = 0; k < config.hangingRootPlacementAttempts; ++k) {
            BlockState lv;
            mutablePos.set(pos, random.nextInt(i) - random.nextInt(i), random.nextInt(j) - random.nextInt(j), random.nextInt(i) - random.nextInt(i));
            if (!world.isAir(mutablePos) || !(lv = config.hangingRootStateProvider.get(random, mutablePos)).canPlaceAt(world, mutablePos) || !world.getBlockState((BlockPos)mutablePos.up()).isSideSolidFullSquare(world, mutablePos, Direction.DOWN)) continue;
            world.setBlockState(mutablePos, lv, Block.NOTIFY_LISTENERS);
        }
    }
}

