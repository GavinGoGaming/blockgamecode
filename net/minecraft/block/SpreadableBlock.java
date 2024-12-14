/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.block;

import com.mojang.serialization.MapCodec;
import net.minecraft.block.AbstractBlock;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.SnowBlock;
import net.minecraft.block.SnowyBlock;
import net.minecraft.registry.tag.FluidTags;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.WorldView;
import net.minecraft.world.chunk.light.ChunkLightProvider;

public abstract class SpreadableBlock
extends SnowyBlock {
    protected SpreadableBlock(AbstractBlock.Settings arg) {
        super(arg);
    }

    private static boolean canSurvive(BlockState state, WorldView world, BlockPos pos) {
        BlockPos lv = pos.up();
        BlockState lv2 = world.getBlockState(lv);
        if (lv2.isOf(Blocks.SNOW) && lv2.get(SnowBlock.LAYERS) == 1) {
            return true;
        }
        if (lv2.getFluidState().getLevel() == 8) {
            return false;
        }
        int i = ChunkLightProvider.getRealisticOpacity(world, state, pos, lv2, lv, Direction.UP, lv2.getOpacity(world, lv));
        return i < world.getMaxLightLevel();
    }

    protected abstract MapCodec<? extends SpreadableBlock> getCodec();

    private static boolean canSpread(BlockState state, WorldView world, BlockPos pos) {
        BlockPos lv = pos.up();
        return SpreadableBlock.canSurvive(state, world, pos) && !world.getFluidState(lv).isIn(FluidTags.WATER);
    }

    @Override
    protected void randomTick(BlockState state, ServerWorld world, BlockPos pos, Random random) {
        if (!SpreadableBlock.canSurvive(state, world, pos)) {
            world.setBlockState(pos, Blocks.DIRT.getDefaultState());
            return;
        }
        if (world.getLightLevel(pos.up()) >= 9) {
            BlockState lv = this.getDefaultState();
            for (int i = 0; i < 4; ++i) {
                BlockPos lv2 = pos.add(random.nextInt(3) - 1, random.nextInt(5) - 3, random.nextInt(3) - 1);
                if (!world.getBlockState(lv2).isOf(Blocks.DIRT) || !SpreadableBlock.canSpread(lv, world, lv2)) continue;
                world.setBlockState(lv2, (BlockState)lv.with(SNOWY, world.getBlockState(lv2.up()).isOf(Blocks.SNOW)));
            }
        }
    }
}

