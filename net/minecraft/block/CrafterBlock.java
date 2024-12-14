/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 * 
 * Could not load the following classes:
 *  org.jetbrains.annotations.Nullable
 */
package net.minecraft.block;

import com.mojang.serialization.MapCodec;
import java.util.Optional;
import net.minecraft.advancement.criterion.Criteria;
import net.minecraft.block.AbstractBlock;
import net.minecraft.block.Block;
import net.minecraft.block.BlockRenderType;
import net.minecraft.block.BlockState;
import net.minecraft.block.BlockWithEntity;
import net.minecraft.block.dispenser.ItemDispenserBehavior;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityTicker;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.block.entity.CrafterBlockEntity;
import net.minecraft.block.entity.HopperBlockEntity;
import net.minecraft.block.enums.Orientation;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.item.ItemStack;
import net.minecraft.recipe.CraftingRecipe;
import net.minecraft.recipe.RecipeCache;
import net.minecraft.recipe.RecipeEntry;
import net.minecraft.recipe.input.CraftingRecipeInput;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.BooleanProperty;
import net.minecraft.state.property.EnumProperty;
import net.minecraft.state.property.Properties;
import net.minecraft.util.ActionResult;
import net.minecraft.util.BlockMirror;
import net.minecraft.util.BlockRotation;
import net.minecraft.util.ItemScatterer;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.World;
import net.minecraft.world.WorldEvents;
import org.jetbrains.annotations.Nullable;

public class CrafterBlock
extends BlockWithEntity {
    public static final MapCodec<CrafterBlock> CODEC = CrafterBlock.createCodec(CrafterBlock::new);
    public static final BooleanProperty CRAFTING = Properties.CRAFTING;
    public static final BooleanProperty TRIGGERED = Properties.TRIGGERED;
    private static final EnumProperty<Orientation> ORIENTATION = Properties.ORIENTATION;
    private static final int field_46802 = 6;
    private static final int TRIGGER_DELAY = 4;
    private static final RecipeCache recipeCache = new RecipeCache(10);
    private static final int field_50015 = 17;

    public CrafterBlock(AbstractBlock.Settings arg) {
        super(arg);
        this.setDefaultState((BlockState)((BlockState)((BlockState)((BlockState)this.stateManager.getDefaultState()).with(ORIENTATION, Orientation.NORTH_UP)).with(TRIGGERED, false)).with(CRAFTING, false));
    }

    protected MapCodec<CrafterBlock> getCodec() {
        return CODEC;
    }

    @Override
    protected boolean hasComparatorOutput(BlockState state) {
        return true;
    }

    @Override
    protected int getComparatorOutput(BlockState state, World world, BlockPos pos) {
        BlockEntity lv = world.getBlockEntity(pos);
        if (lv instanceof CrafterBlockEntity) {
            CrafterBlockEntity lv2 = (CrafterBlockEntity)lv;
            return lv2.getComparatorOutput();
        }
        return 0;
    }

    @Override
    protected void neighborUpdate(BlockState state, World world, BlockPos pos, Block sourceBlock, BlockPos sourcePos, boolean notify) {
        boolean bl2 = world.isReceivingRedstonePower(pos);
        boolean bl3 = state.get(TRIGGERED);
        BlockEntity lv = world.getBlockEntity(pos);
        if (bl2 && !bl3) {
            world.scheduleBlockTick(pos, this, 4);
            world.setBlockState(pos, (BlockState)state.with(TRIGGERED, true), Block.NOTIFY_LISTENERS);
            this.setTriggered(lv, true);
        } else if (!bl2 && bl3) {
            world.setBlockState(pos, (BlockState)((BlockState)state.with(TRIGGERED, false)).with(CRAFTING, false), Block.NOTIFY_LISTENERS);
            this.setTriggered(lv, false);
        }
    }

    @Override
    protected void scheduledTick(BlockState state, ServerWorld world, BlockPos pos, Random random) {
        this.craft(state, world, pos);
    }

    @Override
    @Nullable
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(World world, BlockState state, BlockEntityType<T> type) {
        return world.isClient ? null : CrafterBlock.validateTicker(type, BlockEntityType.CRAFTER, CrafterBlockEntity::tickCrafting);
    }

    private void setTriggered(@Nullable BlockEntity blockEntity, boolean triggered) {
        if (blockEntity instanceof CrafterBlockEntity) {
            CrafterBlockEntity lv = (CrafterBlockEntity)blockEntity;
            lv.setTriggered(triggered);
        }
    }

    @Override
    public BlockEntity createBlockEntity(BlockPos pos, BlockState state) {
        CrafterBlockEntity lv = new CrafterBlockEntity(pos, state);
        lv.setTriggered(state.contains(TRIGGERED) && state.get(TRIGGERED) != false);
        return lv;
    }

    @Override
    public BlockState getPlacementState(ItemPlacementContext ctx) {
        Direction lv = ctx.getPlayerLookDirection().getOpposite();
        Direction lv2 = switch (lv) {
            default -> throw new MatchException(null, null);
            case Direction.DOWN -> ctx.getHorizontalPlayerFacing().getOpposite();
            case Direction.UP -> ctx.getHorizontalPlayerFacing();
            case Direction.NORTH, Direction.SOUTH, Direction.WEST, Direction.EAST -> Direction.UP;
        };
        return (BlockState)((BlockState)this.getDefaultState().with(ORIENTATION, Orientation.byDirections(lv, lv2))).with(TRIGGERED, ctx.getWorld().isReceivingRedstonePower(ctx.getBlockPos()));
    }

    @Override
    public void onPlaced(World world, BlockPos pos, BlockState state, LivingEntity placer, ItemStack itemStack) {
        if (state.get(TRIGGERED).booleanValue()) {
            world.scheduleBlockTick(pos, this, 4);
        }
    }

    @Override
    protected void onStateReplaced(BlockState state, World world, BlockPos pos, BlockState newState, boolean moved) {
        ItemScatterer.onStateReplaced(state, newState, world, pos);
        super.onStateReplaced(state, world, pos, newState, moved);
    }

    @Override
    protected ActionResult onUse(BlockState state, World world, BlockPos pos, PlayerEntity player, BlockHitResult hit) {
        if (world.isClient) {
            return ActionResult.SUCCESS;
        }
        BlockEntity lv = world.getBlockEntity(pos);
        if (lv instanceof CrafterBlockEntity) {
            player.openHandledScreen((CrafterBlockEntity)lv);
        }
        return ActionResult.CONSUME;
    }

    protected void craft(BlockState state, ServerWorld world, BlockPos pos) {
        BlockEntity blockEntity = world.getBlockEntity(pos);
        if (!(blockEntity instanceof CrafterBlockEntity)) {
            return;
        }
        CrafterBlockEntity lv = (CrafterBlockEntity)blockEntity;
        CraftingRecipeInput lv2 = lv.createRecipeInput();
        Optional<RecipeEntry<CraftingRecipe>> optional = CrafterBlock.getCraftingRecipe(world, lv2);
        if (optional.isEmpty()) {
            world.syncWorldEvent(WorldEvents.CRAFTER_FAILS, pos, 0);
            return;
        }
        RecipeEntry<CraftingRecipe> lv3 = optional.get();
        ItemStack lv4 = lv3.value().craft(lv2, world.getRegistryManager());
        if (lv4.isEmpty()) {
            world.syncWorldEvent(WorldEvents.CRAFTER_FAILS, pos, 0);
            return;
        }
        lv.setCraftingTicksRemaining(6);
        world.setBlockState(pos, (BlockState)state.with(CRAFTING, true), Block.NOTIFY_LISTENERS);
        lv4.onCraftByCrafter(world);
        this.transferOrSpawnStack(world, pos, lv, lv4, state, lv3);
        for (ItemStack lv5 : lv3.value().getRemainder(lv2)) {
            if (lv5.isEmpty()) continue;
            this.transferOrSpawnStack(world, pos, lv, lv5, state, lv3);
        }
        lv.getHeldStacks().forEach(stack -> {
            if (stack.isEmpty()) {
                return;
            }
            stack.decrement(1);
        });
        lv.markDirty();
    }

    public static Optional<RecipeEntry<CraftingRecipe>> getCraftingRecipe(World world, CraftingRecipeInput input) {
        return recipeCache.getRecipe(world, input);
    }

    private void transferOrSpawnStack(ServerWorld world, BlockPos pos, CrafterBlockEntity blockEntity, ItemStack stack, BlockState state, RecipeEntry<CraftingRecipe> recipe) {
        Direction lv = state.get(ORIENTATION).getFacing();
        Inventory lv2 = HopperBlockEntity.getInventoryAt(world, pos.offset(lv));
        ItemStack lv3 = stack.copy();
        if (lv2 != null && (lv2 instanceof CrafterBlockEntity || stack.getCount() > lv2.getMaxCount(stack))) {
            ItemStack lv4;
            ItemStack lv5;
            while (!lv3.isEmpty() && (lv5 = HopperBlockEntity.transfer(blockEntity, lv2, lv4 = lv3.copyWithCount(1), lv.getOpposite())).isEmpty()) {
                lv3.decrement(1);
            }
        } else if (lv2 != null) {
            int i;
            while (!lv3.isEmpty() && (i = lv3.getCount()) != (lv3 = HopperBlockEntity.transfer(blockEntity, lv2, lv3, lv.getOpposite())).getCount()) {
            }
        }
        if (!lv3.isEmpty()) {
            Vec3d lv6 = Vec3d.ofCenter(pos);
            Vec3d lv7 = lv6.offset(lv, 0.7);
            ItemDispenserBehavior.spawnItem(world, lv3, 6, lv, lv7);
            for (ServerPlayerEntity lv8 : world.getNonSpectatingEntities(ServerPlayerEntity.class, Box.of(lv6, 17.0, 17.0, 17.0))) {
                Criteria.CRAFTER_RECIPE_CRAFTED.trigger(lv8, recipe.id(), blockEntity.getHeldStacks());
            }
            world.syncWorldEvent(WorldEvents.CRAFTER_CRAFTS, pos, 0);
            world.syncWorldEvent(WorldEvents.CRAFTER_SHOOTS, pos, lv.getId());
        }
    }

    @Override
    protected BlockRenderType getRenderType(BlockState state) {
        return BlockRenderType.MODEL;
    }

    @Override
    protected BlockState rotate(BlockState state, BlockRotation rotation) {
        return (BlockState)state.with(ORIENTATION, rotation.getDirectionTransformation().mapJigsawOrientation(state.get(ORIENTATION)));
    }

    @Override
    protected BlockState mirror(BlockState state, BlockMirror mirror) {
        return (BlockState)state.with(ORIENTATION, mirror.getDirectionTransformation().mapJigsawOrientation(state.get(ORIENTATION)));
    }

    @Override
    protected void appendProperties(StateManager.Builder<Block, BlockState> builder) {
        builder.add(ORIENTATION, TRIGGERED, CRAFTING);
    }
}

