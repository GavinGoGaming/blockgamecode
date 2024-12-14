/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 * 
 * Could not load the following classes:
 *  org.jetbrains.annotations.Nullable
 */
package net.minecraft.item;

import java.util.List;
import java.util.Map;
import net.minecraft.advancement.criterion.Criteria;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.ShapeContext;
import net.minecraft.block.ShulkerBoxBlock;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.BlockStateComponent;
import net.minecraft.component.type.ContainerComponent;
import net.minecraft.component.type.NbtComponent;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUsage;
import net.minecraft.item.ItemUsageContext;
import net.minecraft.item.tooltip.TooltipType;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.resource.featuretoggle.FeatureSet;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.sound.BlockSoundGroup;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvent;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.event.GameEvent;
import org.jetbrains.annotations.Nullable;

public class BlockItem
extends Item {
    @Deprecated
    private final Block block;

    public BlockItem(Block block, Item.Settings settings) {
        super(settings);
        this.block = block;
    }

    @Override
    public ActionResult useOnBlock(ItemUsageContext context) {
        ActionResult lv = this.place(new ItemPlacementContext(context));
        if (!lv.isAccepted() && context.getStack().contains(DataComponentTypes.FOOD)) {
            ActionResult lv2 = super.use(context.getWorld(), context.getPlayer(), context.getHand()).getResult();
            return lv2 == ActionResult.CONSUME ? ActionResult.CONSUME_PARTIAL : lv2;
        }
        return lv;
    }

    public ActionResult place(ItemPlacementContext context) {
        if (!this.getBlock().isEnabled(context.getWorld().getEnabledFeatures())) {
            return ActionResult.FAIL;
        }
        if (!context.canPlace()) {
            return ActionResult.FAIL;
        }
        ItemPlacementContext lv = this.getPlacementContext(context);
        if (lv == null) {
            return ActionResult.FAIL;
        }
        BlockState lv2 = this.getPlacementState(lv);
        if (lv2 == null) {
            return ActionResult.FAIL;
        }
        if (!this.place(lv, lv2)) {
            return ActionResult.FAIL;
        }
        BlockPos lv3 = lv.getBlockPos();
        World lv4 = lv.getWorld();
        PlayerEntity lv5 = lv.getPlayer();
        ItemStack lv6 = lv.getStack();
        BlockState lv7 = lv4.getBlockState(lv3);
        if (lv7.isOf(lv2.getBlock())) {
            lv7 = this.placeFromNbt(lv3, lv4, lv6, lv7);
            this.postPlacement(lv3, lv4, lv5, lv6, lv7);
            BlockItem.copyComponentsToBlockEntity(lv4, lv3, lv6);
            lv7.getBlock().onPlaced(lv4, lv3, lv7, lv5, lv6);
            if (lv5 instanceof ServerPlayerEntity) {
                Criteria.PLACED_BLOCK.trigger((ServerPlayerEntity)lv5, lv3, lv6);
            }
        }
        BlockSoundGroup lv8 = lv7.getSoundGroup();
        lv4.playSound(lv5, lv3, this.getPlaceSound(lv7), SoundCategory.BLOCKS, (lv8.getVolume() + 1.0f) / 2.0f, lv8.getPitch() * 0.8f);
        lv4.emitGameEvent(GameEvent.BLOCK_PLACE, lv3, GameEvent.Emitter.of(lv5, lv7));
        lv6.decrementUnlessCreative(1, lv5);
        return ActionResult.success(lv4.isClient);
    }

    protected SoundEvent getPlaceSound(BlockState state) {
        return state.getSoundGroup().getPlaceSound();
    }

    @Nullable
    public ItemPlacementContext getPlacementContext(ItemPlacementContext context) {
        return context;
    }

    private static void copyComponentsToBlockEntity(World world, BlockPos pos, ItemStack stack) {
        BlockEntity lv = world.getBlockEntity(pos);
        if (lv != null) {
            lv.readComponents(stack);
            lv.markDirty();
        }
    }

    protected boolean postPlacement(BlockPos pos, World world, @Nullable PlayerEntity player, ItemStack stack, BlockState state) {
        return BlockItem.writeNbtToBlockEntity(world, player, pos, stack);
    }

    @Nullable
    protected BlockState getPlacementState(ItemPlacementContext context) {
        BlockState lv = this.getBlock().getPlacementState(context);
        return lv != null && this.canPlace(context, lv) ? lv : null;
    }

    private BlockState placeFromNbt(BlockPos pos, World world, ItemStack stack, BlockState state) {
        BlockStateComponent lv = stack.getOrDefault(DataComponentTypes.BLOCK_STATE, BlockStateComponent.DEFAULT);
        if (lv.isEmpty()) {
            return state;
        }
        BlockState lv2 = lv.applyToState(state);
        if (lv2 != state) {
            world.setBlockState(pos, lv2, Block.NOTIFY_LISTENERS);
        }
        return lv2;
    }

    protected boolean canPlace(ItemPlacementContext context, BlockState state) {
        PlayerEntity lv = context.getPlayer();
        ShapeContext lv2 = lv == null ? ShapeContext.absent() : ShapeContext.of(lv);
        return (!this.checkStatePlacement() || state.canPlaceAt(context.getWorld(), context.getBlockPos())) && context.getWorld().canPlace(state, context.getBlockPos(), lv2);
    }

    protected boolean checkStatePlacement() {
        return true;
    }

    protected boolean place(ItemPlacementContext context, BlockState state) {
        return context.getWorld().setBlockState(context.getBlockPos(), state, Block.NOTIFY_ALL_AND_REDRAW);
    }

    public static boolean writeNbtToBlockEntity(World world, @Nullable PlayerEntity player, BlockPos pos, ItemStack stack) {
        BlockEntity lv2;
        MinecraftServer minecraftServer = world.getServer();
        if (minecraftServer == null) {
            return false;
        }
        NbtComponent lv = stack.getOrDefault(DataComponentTypes.BLOCK_ENTITY_DATA, NbtComponent.DEFAULT);
        if (!lv.isEmpty() && (lv2 = world.getBlockEntity(pos)) != null) {
            if (!(world.isClient || !lv2.copyItemDataRequiresOperator() || player != null && player.isCreativeLevelTwoOp())) {
                return false;
            }
            return lv.applyToBlockEntity(lv2, world.getRegistryManager());
        }
        return false;
    }

    @Override
    public String getTranslationKey() {
        return this.getBlock().getTranslationKey();
    }

    @Override
    public void appendTooltip(ItemStack stack, Item.TooltipContext context, List<Text> tooltip, TooltipType type) {
        super.appendTooltip(stack, context, tooltip, type);
        this.getBlock().appendTooltip(stack, context, tooltip, type);
    }

    public Block getBlock() {
        return this.block;
    }

    public void appendBlocks(Map<Block, Item> map, Item item) {
        map.put(this.getBlock(), item);
    }

    @Override
    public boolean canBeNested() {
        return !(this.getBlock() instanceof ShulkerBoxBlock);
    }

    @Override
    public void onItemEntityDestroyed(ItemEntity entity) {
        ContainerComponent lv = entity.getStack().set(DataComponentTypes.CONTAINER, ContainerComponent.DEFAULT);
        if (lv != null) {
            ItemUsage.spawnItemContents(entity, lv.iterateNonEmptyCopy());
        }
    }

    public static void setBlockEntityData(ItemStack stack, BlockEntityType<?> type, NbtCompound nbt) {
        nbt.remove("id");
        if (nbt.isEmpty()) {
            stack.remove(DataComponentTypes.BLOCK_ENTITY_DATA);
        } else {
            BlockEntity.writeIdToNbt(nbt, type);
            stack.set(DataComponentTypes.BLOCK_ENTITY_DATA, NbtComponent.of(nbt));
        }
    }

    @Override
    public FeatureSet getRequiredFeatures() {
        return this.getBlock().getRequiredFeatures();
    }
}

