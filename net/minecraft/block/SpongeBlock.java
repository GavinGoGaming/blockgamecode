/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.block;

import com.mojang.serialization.MapCodec;
import net.minecraft.block.AbstractBlock;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.FluidBlock;
import net.minecraft.block.FluidDrainable;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.fluid.FluidState;
import net.minecraft.registry.tag.FluidTags;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;

public class SpongeBlock
extends Block {
    public static final MapCodec<SpongeBlock> CODEC = SpongeBlock.createCodec(SpongeBlock::new);
    public static final int ABSORB_RADIUS = 6;
    public static final int ABSORB_LIMIT = 64;
    private static final Direction[] DIRECTIONS = Direction.values();

    public MapCodec<SpongeBlock> getCodec() {
        return CODEC;
    }

    protected SpongeBlock(AbstractBlock.Settings arg) {
        super(arg);
    }

    @Override
    protected void onBlockAdded(BlockState state, World world, BlockPos pos, BlockState oldState, boolean notify) {
        if (oldState.isOf(state.getBlock())) {
            return;
        }
        this.update(world, pos);
    }

    @Override
    protected void neighborUpdate(BlockState state, World world, BlockPos pos, Block sourceBlock, BlockPos sourcePos, boolean notify) {
        this.update(world, pos);
        super.neighborUpdate(state, world, pos, sourceBlock, sourcePos, notify);
    }

    protected void update(World world, BlockPos pos) {
        if (this.absorbWater(world, pos)) {
            world.setBlockState(pos, Blocks.WET_SPONGE.getDefaultState(), Block.NOTIFY_LISTENERS);
            world.playSound(null, pos, SoundEvents.BLOCK_SPONGE_ABSORB, SoundCategory.BLOCKS, 1.0f, 1.0f);
        }
    }

    private boolean absorbWater(World world, BlockPos pos) {
        return BlockPos.iterateRecursively(pos, 6, 65, (currentPos, queuer) -> {
            for (Direction lv : DIRECTIONS) {
                queuer.accept(currentPos.offset(lv));
            }
        }, currentPos -> {
            FluidDrainable lv4;
            if (currentPos.equals(pos)) {
                return true;
            }
            BlockState lv = world.getBlockState((BlockPos)currentPos);
            FluidState lv2 = world.getFluidState((BlockPos)currentPos);
            if (!lv2.isIn(FluidTags.WATER)) {
                return false;
            }
            Block lv3 = lv.getBlock();
            if (lv3 instanceof FluidDrainable && !(lv4 = (FluidDrainable)((Object)lv3)).tryDrainFluid(null, world, (BlockPos)currentPos, lv).isEmpty()) {
                return true;
            }
            if (lv.getBlock() instanceof FluidBlock) {
                world.setBlockState((BlockPos)currentPos, Blocks.AIR.getDefaultState(), Block.NOTIFY_ALL);
            } else if (lv.isOf(Blocks.KELP) || lv.isOf(Blocks.KELP_PLANT) || lv.isOf(Blocks.SEAGRASS) || lv.isOf(Blocks.TALL_SEAGRASS)) {
                BlockEntity lv5 = lv.hasBlockEntity() ? world.getBlockEntity((BlockPos)currentPos) : null;
                SpongeBlock.dropStacks(lv, world, currentPos, lv5);
                world.setBlockState((BlockPos)currentPos, Blocks.AIR.getDefaultState(), Block.NOTIFY_ALL);
            } else {
                return false;
            }
            return true;
        }) > 1;
    }
}

