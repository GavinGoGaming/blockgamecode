/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.world.gen.feature;

import com.mojang.serialization.Codec;
import java.util.Optional;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.DeadCoralWallFanBlock;
import net.minecraft.block.SeaPickleBlock;
import net.minecraft.registry.Registries;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.registry.tag.BlockTags;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.StructureWorldAccess;
import net.minecraft.world.WorldAccess;
import net.minecraft.world.gen.feature.DefaultFeatureConfig;
import net.minecraft.world.gen.feature.Feature;
import net.minecraft.world.gen.feature.util.FeatureContext;

public abstract class CoralFeature
extends Feature<DefaultFeatureConfig> {
    public CoralFeature(Codec<DefaultFeatureConfig> codec) {
        super(codec);
    }

    @Override
    public boolean generate(FeatureContext<DefaultFeatureConfig> context) {
        Random lv = context.getRandom();
        StructureWorldAccess lv2 = context.getWorld();
        BlockPos lv3 = context.getOrigin();
        Optional<Block> optional = Registries.BLOCK.getRandomEntry(BlockTags.CORAL_BLOCKS, lv).map(RegistryEntry::value);
        if (optional.isEmpty()) {
            return false;
        }
        return this.generateCoral(lv2, lv, lv3, optional.get().getDefaultState());
    }

    protected abstract boolean generateCoral(WorldAccess var1, Random var2, BlockPos var3, BlockState var4);

    protected boolean generateCoralPiece(WorldAccess world, Random random, BlockPos pos, BlockState state) {
        BlockPos lv = pos.up();
        BlockState lv2 = world.getBlockState(pos);
        if (!lv2.isOf(Blocks.WATER) && !lv2.isIn(BlockTags.CORALS) || !world.getBlockState(lv).isOf(Blocks.WATER)) {
            return false;
        }
        world.setBlockState(pos, state, Block.NOTIFY_ALL);
        if (random.nextFloat() < 0.25f) {
            Registries.BLOCK.getRandomEntry(BlockTags.CORALS, random).map(RegistryEntry::value).ifPresent(block -> world.setBlockState(lv, block.getDefaultState(), Block.NOTIFY_LISTENERS));
        } else if (random.nextFloat() < 0.05f) {
            world.setBlockState(lv, (BlockState)Blocks.SEA_PICKLE.getDefaultState().with(SeaPickleBlock.PICKLES, random.nextInt(4) + 1), Block.NOTIFY_LISTENERS);
        }
        for (Direction lv3 : Direction.Type.HORIZONTAL) {
            BlockPos lv4;
            if (!(random.nextFloat() < 0.2f) || !world.getBlockState(lv4 = pos.offset(lv3)).isOf(Blocks.WATER)) continue;
            Registries.BLOCK.getRandomEntry(BlockTags.WALL_CORALS, random).map(RegistryEntry::value).ifPresent(block -> {
                BlockState lv = block.getDefaultState();
                if (lv.contains(DeadCoralWallFanBlock.FACING)) {
                    lv = (BlockState)lv.with(DeadCoralWallFanBlock.FACING, lv3);
                }
                world.setBlockState(lv4, lv, Block.NOTIFY_LISTENERS);
            });
        }
        return true;
    }
}

