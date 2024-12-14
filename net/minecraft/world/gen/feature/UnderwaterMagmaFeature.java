/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.world.gen.feature;

import com.mojang.serialization.Codec;
import java.util.Optional;
import java.util.OptionalInt;
import java.util.function.Predicate;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.util.math.BlockBox;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3i;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.StructureWorldAccess;
import net.minecraft.world.WorldAccess;
import net.minecraft.world.gen.feature.Feature;
import net.minecraft.world.gen.feature.UnderwaterMagmaFeatureConfig;
import net.minecraft.world.gen.feature.util.CaveSurface;
import net.minecraft.world.gen.feature.util.FeatureContext;

public class UnderwaterMagmaFeature
extends Feature<UnderwaterMagmaFeatureConfig> {
    public UnderwaterMagmaFeature(Codec<UnderwaterMagmaFeatureConfig> codec) {
        super(codec);
    }

    @Override
    public boolean generate(FeatureContext<UnderwaterMagmaFeatureConfig> context) {
        Vec3i lv6;
        StructureWorldAccess lv = context.getWorld();
        BlockPos lv2 = context.getOrigin();
        UnderwaterMagmaFeatureConfig lv3 = context.getConfig();
        Random lv4 = context.getRandom();
        OptionalInt optionalInt = UnderwaterMagmaFeature.getFloorHeight(lv, lv2, lv3);
        if (optionalInt.isEmpty()) {
            return false;
        }
        BlockPos lv5 = lv2.withY(optionalInt.getAsInt());
        BlockBox lv7 = BlockBox.create(lv5.subtract(lv6 = new Vec3i(lv3.placementRadiusAroundFloor, lv3.placementRadiusAroundFloor, lv3.placementRadiusAroundFloor)), lv5.add(lv6));
        return BlockPos.stream(lv7).filter(pos -> lv4.nextFloat() < arg2.placementProbabilityPerValidPosition).filter(pos -> this.isValidPosition(lv, (BlockPos)pos)).mapToInt(pos -> {
            lv.setBlockState((BlockPos)pos, Blocks.MAGMA_BLOCK.getDefaultState(), Block.NOTIFY_LISTENERS);
            return 1;
        }).sum() > 0;
    }

    private static OptionalInt getFloorHeight(StructureWorldAccess world, BlockPos pos, UnderwaterMagmaFeatureConfig config) {
        Predicate<BlockState> predicate = state -> state.isOf(Blocks.WATER);
        Predicate<BlockState> predicate2 = state -> !state.isOf(Blocks.WATER);
        Optional<CaveSurface> optional = CaveSurface.create(world, pos, config.floorSearchRange, predicate, predicate2);
        return optional.map(CaveSurface::getFloorHeight).orElseGet(OptionalInt::empty);
    }

    private boolean isValidPosition(StructureWorldAccess world, BlockPos pos) {
        if (this.isWaterOrAir(world, pos) || this.isWaterOrAir(world, pos.down())) {
            return false;
        }
        for (Direction lv : Direction.Type.HORIZONTAL) {
            if (!this.isWaterOrAir(world, pos.offset(lv))) continue;
            return false;
        }
        return true;
    }

    private boolean isWaterOrAir(WorldAccess world, BlockPos pos) {
        BlockState lv = world.getBlockState(pos);
        return lv.isOf(Blocks.WATER) || lv.isAir();
    }
}

