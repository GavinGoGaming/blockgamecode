/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.block;

import com.mojang.datafixers.kinds.Applicative;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.block.AbstractBlock;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.FallingBlock;
import net.minecraft.entity.FallingBlockEntity;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.registry.Registries;
import net.minecraft.registry.tag.FluidTags;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3i;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;
import net.minecraft.world.WorldAccess;

public class ConcretePowderBlock
extends FallingBlock {
    public static final MapCodec<ConcretePowderBlock> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(((MapCodec)Registries.BLOCK.getCodec().fieldOf("concrete")).forGetter(block -> block.hardenedState), ConcretePowderBlock.createSettingsCodec()).apply((Applicative<ConcretePowderBlock, ?>)instance, ConcretePowderBlock::new));
    private final Block hardenedState;

    public MapCodec<ConcretePowderBlock> getCodec() {
        return CODEC;
    }

    public ConcretePowderBlock(Block hardened, AbstractBlock.Settings settings) {
        super(settings);
        this.hardenedState = hardened;
    }

    @Override
    public void onLanding(World world, BlockPos pos, BlockState fallingBlockState, BlockState currentStateInPos, FallingBlockEntity fallingBlockEntity) {
        if (ConcretePowderBlock.shouldHarden(world, pos, currentStateInPos)) {
            world.setBlockState(pos, this.hardenedState.getDefaultState(), Block.NOTIFY_ALL);
        }
    }

    @Override
    public BlockState getPlacementState(ItemPlacementContext ctx) {
        BlockState lv3;
        BlockPos lv2;
        World lv = ctx.getWorld();
        if (ConcretePowderBlock.shouldHarden(lv, lv2 = ctx.getBlockPos(), lv3 = lv.getBlockState(lv2))) {
            return this.hardenedState.getDefaultState();
        }
        return super.getPlacementState(ctx);
    }

    private static boolean shouldHarden(BlockView world, BlockPos pos, BlockState state) {
        return ConcretePowderBlock.hardensIn(state) || ConcretePowderBlock.hardensOnAnySide(world, pos);
    }

    private static boolean hardensOnAnySide(BlockView world, BlockPos pos) {
        boolean bl = false;
        BlockPos.Mutable lv = pos.mutableCopy();
        for (Direction lv2 : Direction.values()) {
            BlockState lv3 = world.getBlockState(lv);
            if (lv2 == Direction.DOWN && !ConcretePowderBlock.hardensIn(lv3)) continue;
            lv.set((Vec3i)pos, lv2);
            lv3 = world.getBlockState(lv);
            if (!ConcretePowderBlock.hardensIn(lv3) || lv3.isSideSolidFullSquare(world, pos, lv2.getOpposite())) continue;
            bl = true;
            break;
        }
        return bl;
    }

    private static boolean hardensIn(BlockState state) {
        return state.getFluidState().isIn(FluidTags.WATER);
    }

    @Override
    protected BlockState getStateForNeighborUpdate(BlockState state, Direction direction, BlockState neighborState, WorldAccess world, BlockPos pos, BlockPos neighborPos) {
        if (ConcretePowderBlock.hardensOnAnySide(world, pos)) {
            return this.hardenedState.getDefaultState();
        }
        return super.getStateForNeighborUpdate(state, direction, neighborState, world, pos, neighborPos);
    }

    @Override
    public int getColor(BlockState state, BlockView world, BlockPos pos) {
        return state.getMapColor((BlockView)world, (BlockPos)pos).color;
    }
}

