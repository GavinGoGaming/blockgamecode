/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 * 
 * Could not load the following classes:
 *  org.jetbrains.annotations.Nullable
 */
package net.minecraft.block;

import com.mojang.serialization.MapCodec;
import net.minecraft.block.AbstractBlock;
import net.minecraft.block.Block;
import net.minecraft.block.BlockRenderType;
import net.minecraft.block.BlockState;
import net.minecraft.block.BlockWithEntity;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityTicker;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.block.entity.JukeboxBlockEntity;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.JukeboxPlayableComponent;
import net.minecraft.component.type.NbtComponent;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.BooleanProperty;
import net.minecraft.state.property.Properties;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.ItemActionResult;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

public class JukeboxBlock
extends BlockWithEntity {
    public static final MapCodec<JukeboxBlock> CODEC = JukeboxBlock.createCodec(JukeboxBlock::new);
    public static final BooleanProperty HAS_RECORD = Properties.HAS_RECORD;

    public MapCodec<JukeboxBlock> getCodec() {
        return CODEC;
    }

    protected JukeboxBlock(AbstractBlock.Settings arg) {
        super(arg);
        this.setDefaultState((BlockState)((BlockState)this.stateManager.getDefaultState()).with(HAS_RECORD, false));
    }

    @Override
    public void onPlaced(World world, BlockPos pos, BlockState state, @Nullable LivingEntity placer, ItemStack itemStack) {
        super.onPlaced(world, pos, state, placer, itemStack);
        NbtComponent lv = itemStack.getOrDefault(DataComponentTypes.BLOCK_ENTITY_DATA, NbtComponent.DEFAULT);
        if (lv.contains("RecordItem")) {
            world.setBlockState(pos, (BlockState)state.with(HAS_RECORD, true), Block.NOTIFY_LISTENERS);
        }
    }

    @Override
    protected ActionResult onUse(BlockState state, World world, BlockPos pos, PlayerEntity player, BlockHitResult hit) {
        BlockEntity blockEntity;
        if (state.get(HAS_RECORD).booleanValue() && (blockEntity = world.getBlockEntity(pos)) instanceof JukeboxBlockEntity) {
            JukeboxBlockEntity lv = (JukeboxBlockEntity)blockEntity;
            lv.dropRecord();
            return ActionResult.success(world.isClient);
        }
        return ActionResult.PASS;
    }

    @Override
    protected ItemActionResult onUseWithItem(ItemStack stack, BlockState state, World world, BlockPos pos, PlayerEntity player, Hand hand, BlockHitResult hit) {
        if (state.get(HAS_RECORD).booleanValue()) {
            return ItemActionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
        }
        ItemStack lv = player.getStackInHand(hand);
        ItemActionResult lv2 = JukeboxPlayableComponent.tryPlayStack(world, pos, lv, player);
        if (!lv2.isAccepted()) {
            return ItemActionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
        }
        return lv2;
    }

    @Override
    protected void onStateReplaced(BlockState state, World world, BlockPos pos, BlockState newState, boolean moved) {
        if (state.isOf(newState.getBlock())) {
            return;
        }
        BlockEntity blockEntity = world.getBlockEntity(pos);
        if (blockEntity instanceof JukeboxBlockEntity) {
            JukeboxBlockEntity lv = (JukeboxBlockEntity)blockEntity;
            lv.dropRecord();
        }
        super.onStateReplaced(state, world, pos, newState, moved);
    }

    @Override
    public BlockEntity createBlockEntity(BlockPos pos, BlockState state) {
        return new JukeboxBlockEntity(pos, state);
    }

    @Override
    public boolean emitsRedstonePower(BlockState state) {
        return true;
    }

    @Override
    public int getWeakRedstonePower(BlockState state, BlockView world, BlockPos pos, Direction direction) {
        JukeboxBlockEntity lv;
        BlockEntity blockEntity = world.getBlockEntity(pos);
        if (blockEntity instanceof JukeboxBlockEntity && (lv = (JukeboxBlockEntity)blockEntity).getManager().isPlaying()) {
            return 15;
        }
        return 0;
    }

    @Override
    protected boolean hasComparatorOutput(BlockState state) {
        return true;
    }

    @Override
    protected int getComparatorOutput(BlockState state, World world, BlockPos pos) {
        BlockEntity blockEntity = world.getBlockEntity(pos);
        if (blockEntity instanceof JukeboxBlockEntity) {
            JukeboxBlockEntity lv = (JukeboxBlockEntity)blockEntity;
            return lv.getComparatorOutput();
        }
        return 0;
    }

    @Override
    protected BlockRenderType getRenderType(BlockState state) {
        return BlockRenderType.MODEL;
    }

    @Override
    protected void appendProperties(StateManager.Builder<Block, BlockState> builder) {
        builder.add(HAS_RECORD);
    }

    @Override
    @Nullable
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(World world, BlockState state, BlockEntityType<T> type) {
        if (state.get(HAS_RECORD).booleanValue()) {
            return JukeboxBlock.validateTicker(type, BlockEntityType.JUKEBOX, JukeboxBlockEntity::tick);
        }
        return null;
    }
}

