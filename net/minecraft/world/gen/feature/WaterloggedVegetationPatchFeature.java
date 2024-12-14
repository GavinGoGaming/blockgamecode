/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.world.gen.feature;

import com.mojang.serialization.Codec;
import java.util.HashSet;
import java.util.Set;
import java.util.function.Predicate;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.state.property.Properties;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3i;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.StructureWorldAccess;
import net.minecraft.world.gen.chunk.ChunkGenerator;
import net.minecraft.world.gen.feature.VegetationPatchFeature;
import net.minecraft.world.gen.feature.VegetationPatchFeatureConfig;

public class WaterloggedVegetationPatchFeature
extends VegetationPatchFeature {
    public WaterloggedVegetationPatchFeature(Codec<VegetationPatchFeatureConfig> codec) {
        super(codec);
    }

    @Override
    protected Set<BlockPos> placeGroundAndGetPositions(StructureWorldAccess world, VegetationPatchFeatureConfig config, Random random, BlockPos pos, Predicate<BlockState> replaceable, int radiusX, int radiusZ) {
        Set<BlockPos> set = super.placeGroundAndGetPositions(world, config, random, pos, replaceable, radiusX, radiusZ);
        HashSet<BlockPos> set2 = new HashSet<BlockPos>();
        BlockPos.Mutable lv = new BlockPos.Mutable();
        for (BlockPos lv2 : set) {
            if (WaterloggedVegetationPatchFeature.isSolidBlockAroundPos(world, set, lv2, lv)) continue;
            set2.add(lv2);
        }
        for (BlockPos lv2 : set2) {
            world.setBlockState(lv2, Blocks.WATER.getDefaultState(), Block.NOTIFY_LISTENERS);
        }
        return set2;
    }

    private static boolean isSolidBlockAroundPos(StructureWorldAccess world, Set<BlockPos> positions, BlockPos pos, BlockPos.Mutable mutablePos) {
        return WaterloggedVegetationPatchFeature.isSolidBlockSide(world, pos, mutablePos, Direction.NORTH) || WaterloggedVegetationPatchFeature.isSolidBlockSide(world, pos, mutablePos, Direction.EAST) || WaterloggedVegetationPatchFeature.isSolidBlockSide(world, pos, mutablePos, Direction.SOUTH) || WaterloggedVegetationPatchFeature.isSolidBlockSide(world, pos, mutablePos, Direction.WEST) || WaterloggedVegetationPatchFeature.isSolidBlockSide(world, pos, mutablePos, Direction.DOWN);
    }

    private static boolean isSolidBlockSide(StructureWorldAccess world, BlockPos pos, BlockPos.Mutable mutablePos, Direction direction) {
        mutablePos.set((Vec3i)pos, direction);
        return !world.getBlockState(mutablePos).isSideSolidFullSquare(world, mutablePos, direction.getOpposite());
    }

    @Override
    protected boolean generateVegetationFeature(StructureWorldAccess world, VegetationPatchFeatureConfig config, ChunkGenerator generator, Random random, BlockPos pos) {
        if (super.generateVegetationFeature(world, config, generator, random, pos.down())) {
            BlockState lv = world.getBlockState(pos);
            if (lv.contains(Properties.WATERLOGGED) && !lv.get(Properties.WATERLOGGED).booleanValue()) {
                world.setBlockState(pos, (BlockState)lv.with(Properties.WATERLOGGED, true), Block.NOTIFY_LISTENERS);
            }
            return true;
        }
        return false;
    }
}

