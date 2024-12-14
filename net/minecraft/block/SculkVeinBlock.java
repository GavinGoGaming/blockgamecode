/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.block;

import com.mojang.serialization.MapCodec;
import java.util.Collection;
import net.minecraft.block.AbstractBlock;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.LichenGrower;
import net.minecraft.block.MultifaceGrowthBlock;
import net.minecraft.block.SculkSpreadable;
import net.minecraft.block.Waterloggable;
import net.minecraft.block.entity.SculkSpreadManager;
import net.minecraft.fluid.FluidState;
import net.minecraft.fluid.Fluids;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.item.Items;
import net.minecraft.registry.tag.BlockTags;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.BooleanProperty;
import net.minecraft.state.property.Properties;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.BlockView;
import net.minecraft.world.WorldAccess;

public class SculkVeinBlock
extends MultifaceGrowthBlock
implements SculkSpreadable,
Waterloggable {
    public static final MapCodec<SculkVeinBlock> CODEC = SculkVeinBlock.createCodec(SculkVeinBlock::new);
    private static final BooleanProperty WATERLOGGED = Properties.WATERLOGGED;
    private final LichenGrower allGrowTypeGrower = new LichenGrower(new SculkVeinGrowChecker(this, LichenGrower.GROW_TYPES));
    private final LichenGrower samePositionOnlyGrower = new LichenGrower(new SculkVeinGrowChecker(this, LichenGrower.GrowType.SAME_POSITION));

    public MapCodec<SculkVeinBlock> getCodec() {
        return CODEC;
    }

    public SculkVeinBlock(AbstractBlock.Settings arg) {
        super(arg);
        this.setDefaultState((BlockState)this.getDefaultState().with(WATERLOGGED, false));
    }

    @Override
    public LichenGrower getGrower() {
        return this.allGrowTypeGrower;
    }

    public LichenGrower getSamePositionOnlyGrower() {
        return this.samePositionOnlyGrower;
    }

    public static boolean place(WorldAccess world, BlockPos pos, BlockState state, Collection<Direction> directions) {
        boolean bl = false;
        BlockState lv = Blocks.SCULK_VEIN.getDefaultState();
        for (Direction lv2 : directions) {
            BlockPos lv3;
            if (!SculkVeinBlock.canGrowOn(world, lv2, lv3 = pos.offset(lv2), world.getBlockState(lv3))) continue;
            lv = (BlockState)lv.with(SculkVeinBlock.getProperty(lv2), true);
            bl = true;
        }
        if (!bl) {
            return false;
        }
        if (!state.getFluidState().isEmpty()) {
            lv = (BlockState)lv.with(WATERLOGGED, true);
        }
        world.setBlockState(pos, lv, Block.NOTIFY_ALL);
        return true;
    }

    @Override
    public void spreadAtSamePosition(WorldAccess world, BlockState state, BlockPos pos, Random random) {
        if (!state.isOf(this)) {
            return;
        }
        for (Direction lv : DIRECTIONS) {
            BooleanProperty lv2 = SculkVeinBlock.getProperty(lv);
            if (!state.get(lv2).booleanValue() || !world.getBlockState(pos.offset(lv)).isOf(Blocks.SCULK)) continue;
            state = (BlockState)state.with(lv2, false);
        }
        if (!SculkVeinBlock.hasAnyDirection(state)) {
            FluidState lv3 = world.getFluidState(pos);
            state = (lv3.isEmpty() ? Blocks.AIR : Blocks.WATER).getDefaultState();
        }
        world.setBlockState(pos, state, Block.NOTIFY_ALL);
        SculkSpreadable.super.spreadAtSamePosition(world, state, pos, random);
    }

    @Override
    public int spread(SculkSpreadManager.Cursor cursor, WorldAccess world, BlockPos catalystPos, Random random, SculkSpreadManager spreadManager, boolean shouldConvertToBlock) {
        if (shouldConvertToBlock && this.convertToBlock(spreadManager, world, cursor.getPos(), random)) {
            return cursor.getCharge() - 1;
        }
        return random.nextInt(spreadManager.getSpreadChance()) == 0 ? MathHelper.floor((float)cursor.getCharge() * 0.5f) : cursor.getCharge();
    }

    private boolean convertToBlock(SculkSpreadManager spreadManager, WorldAccess world, BlockPos pos, Random random) {
        BlockState lv = world.getBlockState(pos);
        TagKey<Block> lv2 = spreadManager.getReplaceableTag();
        for (Direction lv3 : Direction.shuffle(random)) {
            BlockPos lv4;
            BlockState lv5;
            if (!SculkVeinBlock.hasDirection(lv, lv3) || !(lv5 = world.getBlockState(lv4 = pos.offset(lv3))).isIn(lv2)) continue;
            BlockState lv6 = Blocks.SCULK.getDefaultState();
            world.setBlockState(lv4, lv6, Block.NOTIFY_ALL);
            Block.pushEntitiesUpBeforeBlockChange(lv5, lv6, world, lv4);
            world.playSound(null, lv4, SoundEvents.BLOCK_SCULK_SPREAD, SoundCategory.BLOCKS, 1.0f, 1.0f);
            this.allGrowTypeGrower.grow(lv6, world, lv4, spreadManager.isWorldGen());
            Direction lv7 = lv3.getOpposite();
            for (Direction lv8 : DIRECTIONS) {
                BlockPos lv9;
                BlockState lv10;
                if (lv8 == lv7 || !(lv10 = world.getBlockState(lv9 = lv4.offset(lv8))).isOf(this)) continue;
                this.spreadAtSamePosition(world, lv10, lv9, random);
            }
            return true;
        }
        return false;
    }

    public static boolean veinCoversSculkReplaceable(WorldAccess world, BlockState state, BlockPos pos) {
        if (!state.isOf(Blocks.SCULK_VEIN)) {
            return false;
        }
        for (Direction lv : DIRECTIONS) {
            if (!SculkVeinBlock.hasDirection(state, lv) || !world.getBlockState(pos.offset(lv)).isIn(BlockTags.SCULK_REPLACEABLE)) continue;
            return true;
        }
        return false;
    }

    @Override
    protected BlockState getStateForNeighborUpdate(BlockState state, Direction direction, BlockState neighborState, WorldAccess world, BlockPos pos, BlockPos neighborPos) {
        if (state.get(WATERLOGGED).booleanValue()) {
            world.scheduleFluidTick(pos, Fluids.WATER, Fluids.WATER.getTickRate(world));
        }
        return super.getStateForNeighborUpdate(state, direction, neighborState, world, pos, neighborPos);
    }

    @Override
    protected void appendProperties(StateManager.Builder<Block, BlockState> builder) {
        super.appendProperties(builder);
        builder.add(WATERLOGGED);
    }

    @Override
    protected boolean canReplace(BlockState state, ItemPlacementContext context) {
        return !context.getStack().isOf(Items.SCULK_VEIN) || super.canReplace(state, context);
    }

    @Override
    protected FluidState getFluidState(BlockState state) {
        if (state.get(WATERLOGGED).booleanValue()) {
            return Fluids.WATER.getStill(false);
        }
        return super.getFluidState(state);
    }

    class SculkVeinGrowChecker
    extends LichenGrower.LichenGrowChecker {
        private final LichenGrower.GrowType[] growTypes;

        public SculkVeinGrowChecker(SculkVeinBlock arg, LichenGrower.GrowType ... growTypes) {
            super(arg);
            this.growTypes = growTypes;
        }

        @Override
        public boolean canGrow(BlockView world, BlockPos pos, BlockPos growPos, Direction direction, BlockState state) {
            BlockPos lv2;
            BlockState lv = world.getBlockState(growPos.offset(direction));
            if (lv.isOf(Blocks.SCULK) || lv.isOf(Blocks.SCULK_CATALYST) || lv.isOf(Blocks.MOVING_PISTON)) {
                return false;
            }
            if (pos.getManhattanDistance(growPos) == 2 && world.getBlockState(lv2 = pos.offset(direction.getOpposite())).isSideSolidFullSquare(world, lv2, direction)) {
                return false;
            }
            FluidState lv3 = state.getFluidState();
            if (!lv3.isEmpty() && !lv3.isOf(Fluids.WATER)) {
                return false;
            }
            if (state.isIn(BlockTags.FIRE)) {
                return false;
            }
            return state.isReplaceable() || super.canGrow(world, pos, growPos, direction, state);
        }

        @Override
        public LichenGrower.GrowType[] getGrowTypes() {
            return this.growTypes;
        }

        @Override
        public boolean canGrow(BlockState state) {
            return !state.isOf(Blocks.SCULK_VEIN);
        }
    }
}

