/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 * 
 * Could not load the following classes:
 *  org.jetbrains.annotations.Nullable
 */
package net.minecraft.block;

import com.mojang.serialization.MapCodec;
import java.util.List;
import java.util.stream.Stream;
import net.minecraft.block.AbstractBlock;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.BlockWithEntity;
import net.minecraft.block.ShapeContext;
import net.minecraft.block.Waterloggable;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.DecoratedPotBlockEntity;
import net.minecraft.block.entity.Sherds;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.Entity;
import net.minecraft.entity.ai.pathing.NavigationType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.ProjectileEntity;
import net.minecraft.fluid.FluidState;
import net.minecraft.fluid.Fluids;
import net.minecraft.item.Item;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.item.tooltip.TooltipType;
import net.minecraft.loot.context.LootContextParameterSet;
import net.minecraft.loot.context.LootContextParameters;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.registry.tag.EnchantmentTags;
import net.minecraft.registry.tag.ItemTags;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.ScreenTexts;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.BlockSoundGroup;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.stat.Stats;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.BooleanProperty;
import net.minecraft.state.property.DirectionProperty;
import net.minecraft.state.property.Properties;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.BlockMirror;
import net.minecraft.util.BlockRotation;
import net.minecraft.util.Formatting;
import net.minecraft.util.Hand;
import net.minecraft.util.Identifier;
import net.minecraft.util.ItemActionResult;
import net.minecraft.util.ItemScatterer;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;
import net.minecraft.world.WorldAccess;
import net.minecraft.world.WorldView;
import net.minecraft.world.event.GameEvent;
import org.jetbrains.annotations.Nullable;

public class DecoratedPotBlock
extends BlockWithEntity
implements Waterloggable {
    public static final MapCodec<DecoratedPotBlock> CODEC = DecoratedPotBlock.createCodec(DecoratedPotBlock::new);
    public static final Identifier SHERDS_DYNAMIC_DROP_ID = Identifier.ofVanilla("sherds");
    private static final VoxelShape SHAPE = Block.createCuboidShape(1.0, 0.0, 1.0, 15.0, 16.0, 15.0);
    private static final DirectionProperty FACING = Properties.HORIZONTAL_FACING;
    public static final BooleanProperty CRACKED = Properties.CRACKED;
    private static final BooleanProperty WATERLOGGED = Properties.WATERLOGGED;

    public MapCodec<DecoratedPotBlock> getCodec() {
        return CODEC;
    }

    protected DecoratedPotBlock(AbstractBlock.Settings arg) {
        super(arg);
        this.setDefaultState((BlockState)((BlockState)((BlockState)((BlockState)this.stateManager.getDefaultState()).with(FACING, Direction.NORTH)).with(WATERLOGGED, false)).with(CRACKED, false));
    }

    @Override
    protected BlockState getStateForNeighborUpdate(BlockState state, Direction direction, BlockState neighborState, WorldAccess world, BlockPos pos, BlockPos neighborPos) {
        if (state.get(WATERLOGGED).booleanValue()) {
            world.scheduleFluidTick(pos, Fluids.WATER, Fluids.WATER.getTickRate(world));
        }
        return super.getStateForNeighborUpdate(state, direction, neighborState, world, pos, neighborPos);
    }

    @Override
    public BlockState getPlacementState(ItemPlacementContext ctx) {
        FluidState lv = ctx.getWorld().getFluidState(ctx.getBlockPos());
        return (BlockState)((BlockState)((BlockState)this.getDefaultState().with(FACING, ctx.getHorizontalPlayerFacing())).with(WATERLOGGED, lv.getFluid() == Fluids.WATER)).with(CRACKED, false);
    }

    @Override
    protected ItemActionResult onUseWithItem(ItemStack stack, BlockState state, World world, BlockPos pos, PlayerEntity player, Hand hand, BlockHitResult hit) {
        BlockEntity blockEntity = world.getBlockEntity(pos);
        if (!(blockEntity instanceof DecoratedPotBlockEntity)) {
            return ItemActionResult.SKIP_DEFAULT_BLOCK_INTERACTION;
        }
        DecoratedPotBlockEntity lv = (DecoratedPotBlockEntity)blockEntity;
        if (world.isClient) {
            return ItemActionResult.CONSUME;
        }
        ItemStack lv2 = lv.getStack();
        if (!stack.isEmpty() && (lv2.isEmpty() || ItemStack.areItemsAndComponentsEqual(lv2, stack) && lv2.getCount() < lv2.getMaxCount())) {
            float f;
            lv.wobble(DecoratedPotBlockEntity.WobbleType.POSITIVE);
            player.incrementStat(Stats.USED.getOrCreateStat(stack.getItem()));
            ItemStack lv3 = stack.splitUnlessCreative(1, player);
            if (lv.isEmpty()) {
                lv.setStack(lv3);
                f = (float)lv3.getCount() / (float)lv3.getMaxCount();
            } else {
                lv2.increment(1);
                f = (float)lv2.getCount() / (float)lv2.getMaxCount();
            }
            world.playSound(null, pos, SoundEvents.BLOCK_DECORATED_POT_INSERT, SoundCategory.BLOCKS, 1.0f, 0.7f + 0.5f * f);
            if (world instanceof ServerWorld) {
                ServerWorld lv4 = (ServerWorld)world;
                lv4.spawnParticles(ParticleTypes.DUST_PLUME, (double)pos.getX() + 0.5, (double)pos.getY() + 1.2, (double)pos.getZ() + 0.5, 7, 0.0, 0.0, 0.0, 0.0);
            }
            lv.markDirty();
            world.emitGameEvent((Entity)player, GameEvent.BLOCK_CHANGE, pos);
            return ItemActionResult.SUCCESS;
        }
        return ItemActionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
    }

    @Override
    protected ActionResult onUse(BlockState state, World world, BlockPos pos, PlayerEntity player, BlockHitResult hit) {
        BlockEntity blockEntity = world.getBlockEntity(pos);
        if (!(blockEntity instanceof DecoratedPotBlockEntity)) {
            return ActionResult.PASS;
        }
        DecoratedPotBlockEntity lv = (DecoratedPotBlockEntity)blockEntity;
        world.playSound(null, pos, SoundEvents.BLOCK_DECORATED_POT_INSERT_FAIL, SoundCategory.BLOCKS, 1.0f, 1.0f);
        lv.wobble(DecoratedPotBlockEntity.WobbleType.NEGATIVE);
        world.emitGameEvent((Entity)player, GameEvent.BLOCK_CHANGE, pos);
        return ActionResult.SUCCESS;
    }

    @Override
    protected boolean canPathfindThrough(BlockState state, NavigationType type) {
        return false;
    }

    @Override
    protected VoxelShape getOutlineShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context) {
        return SHAPE;
    }

    @Override
    protected void appendProperties(StateManager.Builder<Block, BlockState> builder) {
        builder.add(FACING, WATERLOGGED, CRACKED);
    }

    @Override
    @Nullable
    public BlockEntity createBlockEntity(BlockPos pos, BlockState state) {
        return new DecoratedPotBlockEntity(pos, state);
    }

    @Override
    protected void onStateReplaced(BlockState state, World world, BlockPos pos, BlockState newState, boolean moved) {
        ItemScatterer.onStateReplaced(state, newState, world, pos);
        super.onStateReplaced(state, world, pos, newState, moved);
    }

    @Override
    protected List<ItemStack> getDroppedStacks(BlockState state, LootContextParameterSet.Builder builder) {
        BlockEntity lv = builder.getOptional(LootContextParameters.BLOCK_ENTITY);
        if (lv instanceof DecoratedPotBlockEntity) {
            DecoratedPotBlockEntity lv2 = (DecoratedPotBlockEntity)lv;
            builder.addDynamicDrop(SHERDS_DYNAMIC_DROP_ID, lootConsumer -> {
                for (Item lv : lv2.getSherds().stream()) {
                    lootConsumer.accept(lv.getDefaultStack());
                }
            });
        }
        return super.getDroppedStacks(state, builder);
    }

    @Override
    public BlockState onBreak(World world, BlockPos pos, BlockState state, PlayerEntity player) {
        ItemStack lv = player.getMainHandStack();
        BlockState lv2 = state;
        if (lv.isIn(ItemTags.BREAKS_DECORATED_POTS) && !EnchantmentHelper.hasAnyEnchantmentsIn(lv, EnchantmentTags.PREVENTS_DECORATED_POT_SHATTERING)) {
            lv2 = (BlockState)state.with(CRACKED, true);
            world.setBlockState(pos, lv2, Block.NO_REDRAW);
        }
        return super.onBreak(world, pos, lv2, player);
    }

    @Override
    protected FluidState getFluidState(BlockState state) {
        if (state.get(WATERLOGGED).booleanValue()) {
            return Fluids.WATER.getStill(false);
        }
        return super.getFluidState(state);
    }

    @Override
    protected BlockSoundGroup getSoundGroup(BlockState state) {
        if (state.get(CRACKED).booleanValue()) {
            return BlockSoundGroup.DECORATED_POT_SHATTER;
        }
        return BlockSoundGroup.DECORATED_POT;
    }

    @Override
    public void appendTooltip(ItemStack stack, Item.TooltipContext context, List<Text> tooltip, TooltipType options) {
        super.appendTooltip(stack, context, tooltip, options);
        Sherds lv = stack.getOrDefault(DataComponentTypes.POT_DECORATIONS, Sherds.DEFAULT);
        if (lv.equals(Sherds.DEFAULT)) {
            return;
        }
        tooltip.add(ScreenTexts.EMPTY);
        Stream.of(lv.front(), lv.left(), lv.right(), lv.back()).forEach(sherd -> tooltip.add(new ItemStack(sherd.orElse(Items.BRICK), 1).getName().copyContentOnly().formatted(Formatting.GRAY)));
    }

    @Override
    protected void onProjectileHit(World world, BlockState state, BlockHitResult hit, ProjectileEntity projectile) {
        BlockPos lv = hit.getBlockPos();
        if (!world.isClient && projectile.canModifyAt(world, lv) && projectile.canBreakBlocks(world)) {
            world.setBlockState(lv, (BlockState)state.with(CRACKED, true), Block.NO_REDRAW);
            world.breakBlock(lv, true, projectile);
        }
    }

    @Override
    public ItemStack getPickStack(WorldView world, BlockPos pos, BlockState state) {
        BlockEntity blockEntity = world.getBlockEntity(pos);
        if (blockEntity instanceof DecoratedPotBlockEntity) {
            DecoratedPotBlockEntity lv = (DecoratedPotBlockEntity)blockEntity;
            return lv.asStack();
        }
        return super.getPickStack(world, pos, state);
    }

    @Override
    protected boolean hasComparatorOutput(BlockState state) {
        return true;
    }

    @Override
    protected int getComparatorOutput(BlockState state, World world, BlockPos pos) {
        return ScreenHandler.calculateComparatorOutput(world.getBlockEntity(pos));
    }

    @Override
    protected BlockState rotate(BlockState state, BlockRotation rotation) {
        return (BlockState)state.with(FACING, rotation.rotate(state.get(FACING)));
    }

    @Override
    protected BlockState mirror(BlockState state, BlockMirror mirror) {
        return state.rotate(mirror.getRotation(state.get(FACING)));
    }
}

