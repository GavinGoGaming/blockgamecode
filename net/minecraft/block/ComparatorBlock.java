/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 * 
 * Could not load the following classes:
 *  org.jetbrains.annotations.Nullable
 */
package net.minecraft.block;

import com.mojang.serialization.MapCodec;
import java.util.List;
import net.minecraft.block.AbstractBlock;
import net.minecraft.block.AbstractRedstoneGateBlock;
import net.minecraft.block.Block;
import net.minecraft.block.BlockEntityProvider;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.ComparatorBlockEntity;
import net.minecraft.block.enums.ComparatorMode;
import net.minecraft.entity.decoration.ItemFrameEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.EnumProperty;
import net.minecraft.state.property.Properties;
import net.minecraft.util.ActionResult;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;
import net.minecraft.world.WorldAccess;
import net.minecraft.world.tick.TickPriority;
import org.jetbrains.annotations.Nullable;

public class ComparatorBlock
extends AbstractRedstoneGateBlock
implements BlockEntityProvider {
    public static final MapCodec<ComparatorBlock> CODEC = ComparatorBlock.createCodec(ComparatorBlock::new);
    public static final EnumProperty<ComparatorMode> MODE = Properties.COMPARATOR_MODE;

    public MapCodec<ComparatorBlock> getCodec() {
        return CODEC;
    }

    public ComparatorBlock(AbstractBlock.Settings arg) {
        super(arg);
        this.setDefaultState((BlockState)((BlockState)((BlockState)((BlockState)this.stateManager.getDefaultState()).with(FACING, Direction.NORTH)).with(POWERED, false)).with(MODE, ComparatorMode.COMPARE));
    }

    @Override
    protected int getUpdateDelayInternal(BlockState state) {
        return 2;
    }

    @Override
    public BlockState getStateForNeighborUpdate(BlockState state, Direction direction, BlockState neighborState, WorldAccess world, BlockPos pos, BlockPos neighborPos) {
        if (direction == Direction.DOWN && !this.canPlaceAbove(world, neighborPos, neighborState)) {
            return Blocks.AIR.getDefaultState();
        }
        return super.getStateForNeighborUpdate(state, direction, neighborState, world, pos, neighborPos);
    }

    @Override
    protected int getOutputLevel(BlockView world, BlockPos pos, BlockState state) {
        BlockEntity lv = world.getBlockEntity(pos);
        if (lv instanceof ComparatorBlockEntity) {
            return ((ComparatorBlockEntity)lv).getOutputSignal();
        }
        return 0;
    }

    private int calculateOutputSignal(World world, BlockPos pos, BlockState state) {
        int i = this.getPower(world, pos, state);
        if (i == 0) {
            return 0;
        }
        int j = this.getMaxInputLevelSides(world, pos, state);
        if (j > i) {
            return 0;
        }
        if (state.get(MODE) == ComparatorMode.SUBTRACT) {
            return i - j;
        }
        return i;
    }

    @Override
    protected boolean hasPower(World world, BlockPos pos, BlockState state) {
        int i = this.getPower(world, pos, state);
        if (i == 0) {
            return false;
        }
        int j = this.getMaxInputLevelSides(world, pos, state);
        if (i > j) {
            return true;
        }
        return i == j && state.get(MODE) == ComparatorMode.COMPARE;
    }

    @Override
    protected int getPower(World world, BlockPos pos, BlockState state) {
        int i = super.getPower(world, pos, state);
        Direction lv = state.get(FACING);
        BlockPos lv2 = pos.offset(lv);
        BlockState lv3 = world.getBlockState(lv2);
        if (lv3.hasComparatorOutput()) {
            i = lv3.getComparatorOutput(world, lv2);
        } else if (i < 15 && lv3.isSolidBlock(world, lv2)) {
            lv2 = lv2.offset(lv);
            lv3 = world.getBlockState(lv2);
            ItemFrameEntity lv4 = this.getAttachedItemFrame(world, lv, lv2);
            int j = Math.max(lv4 == null ? Integer.MIN_VALUE : lv4.getComparatorPower(), lv3.hasComparatorOutput() ? lv3.getComparatorOutput(world, lv2) : Integer.MIN_VALUE);
            if (j != Integer.MIN_VALUE) {
                i = j;
            }
        }
        return i;
    }

    @Nullable
    private ItemFrameEntity getAttachedItemFrame(World world, Direction facing, BlockPos pos) {
        List<ItemFrameEntity> list = world.getEntitiesByClass(ItemFrameEntity.class, new Box(pos.getX(), pos.getY(), pos.getZ(), pos.getX() + 1, pos.getY() + 1, pos.getZ() + 1), itemFrame -> itemFrame != null && itemFrame.getHorizontalFacing() == facing);
        if (list.size() == 1) {
            return list.get(0);
        }
        return null;
    }

    @Override
    protected ActionResult onUse(BlockState state, World world, BlockPos pos, PlayerEntity player, BlockHitResult hit) {
        if (!player.getAbilities().allowModifyWorld) {
            return ActionResult.PASS;
        }
        float f = (state = (BlockState)state.cycle(MODE)).get(MODE) == ComparatorMode.SUBTRACT ? 0.55f : 0.5f;
        world.playSound(player, pos, SoundEvents.BLOCK_COMPARATOR_CLICK, SoundCategory.BLOCKS, 0.3f, f);
        world.setBlockState(pos, state, Block.NOTIFY_LISTENERS);
        this.update(world, pos, state);
        return ActionResult.success(world.isClient);
    }

    @Override
    protected void updatePowered(World world, BlockPos pos, BlockState state) {
        int j;
        if (world.getBlockTickScheduler().isTicking(pos, this)) {
            return;
        }
        int i = this.calculateOutputSignal(world, pos, state);
        BlockEntity lv = world.getBlockEntity(pos);
        int n = j = lv instanceof ComparatorBlockEntity ? ((ComparatorBlockEntity)lv).getOutputSignal() : 0;
        if (i != j || state.get(POWERED).booleanValue() != this.hasPower(world, pos, state)) {
            TickPriority lv2 = this.isTargetNotAligned(world, pos, state) ? TickPriority.HIGH : TickPriority.NORMAL;
            world.scheduleBlockTick(pos, this, 2, lv2);
        }
    }

    private void update(World world, BlockPos pos, BlockState state) {
        int i = this.calculateOutputSignal(world, pos, state);
        BlockEntity lv = world.getBlockEntity(pos);
        int j = 0;
        if (lv instanceof ComparatorBlockEntity) {
            ComparatorBlockEntity lv2 = (ComparatorBlockEntity)lv;
            j = lv2.getOutputSignal();
            lv2.setOutputSignal(i);
        }
        if (j != i || state.get(MODE) == ComparatorMode.COMPARE) {
            boolean bl = this.hasPower(world, pos, state);
            boolean bl2 = state.get(POWERED);
            if (bl2 && !bl) {
                world.setBlockState(pos, (BlockState)state.with(POWERED, false), Block.NOTIFY_LISTENERS);
            } else if (!bl2 && bl) {
                world.setBlockState(pos, (BlockState)state.with(POWERED, true), Block.NOTIFY_LISTENERS);
            }
            this.updateTarget(world, pos, state);
        }
    }

    @Override
    protected void scheduledTick(BlockState state, ServerWorld world, BlockPos pos, Random random) {
        this.update(world, pos, state);
    }

    @Override
    protected boolean onSyncedBlockEvent(BlockState state, World world, BlockPos pos, int type, int data) {
        super.onSyncedBlockEvent(state, world, pos, type, data);
        BlockEntity lv = world.getBlockEntity(pos);
        return lv != null && lv.onSyncedBlockEvent(type, data);
    }

    @Override
    public BlockEntity createBlockEntity(BlockPos pos, BlockState state) {
        return new ComparatorBlockEntity(pos, state);
    }

    @Override
    protected void appendProperties(StateManager.Builder<Block, BlockState> builder) {
        builder.add(FACING, MODE, POWERED);
    }
}

