/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 * 
 * Could not load the following classes:
 *  org.jetbrains.annotations.Nullable
 */
package net.minecraft.block;

import com.mojang.serialization.MapCodec;
import java.util.List;
import net.minecraft.advancement.criterion.Criteria;
import net.minecraft.block.AbstractBlock;
import net.minecraft.block.Block;
import net.minecraft.block.BlockRenderType;
import net.minecraft.block.BlockState;
import net.minecraft.block.BlockWithEntity;
import net.minecraft.block.CampfireBlock;
import net.minecraft.block.FireBlock;
import net.minecraft.block.HorizontalFacingBlock;
import net.minecraft.block.entity.BeehiveBlockEntity;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityTicker;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.BlockStateComponent;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.Entity;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.TntEntity;
import net.minecraft.entity.boss.WitherEntity;
import net.minecraft.entity.mob.CreeperEntity;
import net.minecraft.entity.passive.BeeEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.WitherSkullEntity;
import net.minecraft.entity.vehicle.TntMinecartEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.loot.context.LootContextParameterSet;
import net.minecraft.loot.context.LootContextParameters;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.registry.tag.BlockTags;
import net.minecraft.registry.tag.EnchantmentTags;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.stat.Stats;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.DirectionProperty;
import net.minecraft.state.property.IntProperty;
import net.minecraft.state.property.Properties;
import net.minecraft.util.BlockMirror;
import net.minecraft.util.BlockRotation;
import net.minecraft.util.Hand;
import net.minecraft.util.ItemActionResult;
import net.minecraft.util.Util;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.random.Random;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.world.GameRules;
import net.minecraft.world.World;
import net.minecraft.world.WorldAccess;
import net.minecraft.world.event.GameEvent;
import org.jetbrains.annotations.Nullable;

public class BeehiveBlock
extends BlockWithEntity {
    public static final MapCodec<BeehiveBlock> CODEC = BeehiveBlock.createCodec(BeehiveBlock::new);
    public static final DirectionProperty FACING = HorizontalFacingBlock.FACING;
    public static final IntProperty HONEY_LEVEL = Properties.HONEY_LEVEL;
    public static final int FULL_HONEY_LEVEL = 5;
    private static final int DROPPED_HONEYCOMB_COUNT = 3;

    public MapCodec<BeehiveBlock> getCodec() {
        return CODEC;
    }

    public BeehiveBlock(AbstractBlock.Settings arg) {
        super(arg);
        this.setDefaultState((BlockState)((BlockState)((BlockState)this.stateManager.getDefaultState()).with(HONEY_LEVEL, 0)).with(FACING, Direction.NORTH));
    }

    @Override
    protected boolean hasComparatorOutput(BlockState state) {
        return true;
    }

    @Override
    protected int getComparatorOutput(BlockState state, World world, BlockPos pos) {
        return state.get(HONEY_LEVEL);
    }

    @Override
    public void afterBreak(World world, PlayerEntity player, BlockPos pos, BlockState state, @Nullable BlockEntity blockEntity, ItemStack tool) {
        super.afterBreak(world, player, pos, state, blockEntity, tool);
        if (!world.isClient && blockEntity instanceof BeehiveBlockEntity) {
            BeehiveBlockEntity lv = (BeehiveBlockEntity)blockEntity;
            if (!EnchantmentHelper.hasAnyEnchantmentsIn(tool, EnchantmentTags.PREVENTS_BEE_SPAWNS_WHEN_MINING)) {
                lv.angerBees(player, state, BeehiveBlockEntity.BeeState.EMERGENCY);
                world.updateComparators(pos, this);
                this.angerNearbyBees(world, pos);
            }
            Criteria.BEE_NEST_DESTROYED.trigger((ServerPlayerEntity)player, state, tool, lv.getBeeCount());
        }
    }

    private void angerNearbyBees(World world, BlockPos pos) {
        Box lv = new Box(pos).expand(8.0, 6.0, 8.0);
        List<BeeEntity> list = world.getNonSpectatingEntities(BeeEntity.class, lv);
        if (!list.isEmpty()) {
            List<PlayerEntity> list2 = world.getNonSpectatingEntities(PlayerEntity.class, lv);
            if (list2.isEmpty()) {
                return;
            }
            for (BeeEntity lv2 : list) {
                if (lv2.getTarget() != null) continue;
                PlayerEntity lv3 = Util.getRandom(list2, world.random);
                lv2.setTarget(lv3);
            }
        }
    }

    public static void dropHoneycomb(World world, BlockPos pos) {
        BeehiveBlock.dropStack(world, pos, new ItemStack(Items.HONEYCOMB, 3));
    }

    @Override
    protected ItemActionResult onUseWithItem(ItemStack stack, BlockState state, World world, BlockPos pos, PlayerEntity player, Hand hand, BlockHitResult hit) {
        int i = state.get(HONEY_LEVEL);
        boolean bl = false;
        if (i >= 5) {
            Item lv = stack.getItem();
            if (stack.isOf(Items.SHEARS)) {
                world.playSound(player, player.getX(), player.getY(), player.getZ(), SoundEvents.BLOCK_BEEHIVE_SHEAR, SoundCategory.BLOCKS, 1.0f, 1.0f);
                BeehiveBlock.dropHoneycomb(world, pos);
                stack.damage(1, player, LivingEntity.getSlotForHand(hand));
                bl = true;
                world.emitGameEvent((Entity)player, GameEvent.SHEAR, pos);
            } else if (stack.isOf(Items.GLASS_BOTTLE)) {
                stack.decrement(1);
                world.playSound(player, player.getX(), player.getY(), player.getZ(), SoundEvents.ITEM_BOTTLE_FILL, SoundCategory.BLOCKS, 1.0f, 1.0f);
                if (stack.isEmpty()) {
                    player.setStackInHand(hand, new ItemStack(Items.HONEY_BOTTLE));
                } else if (!player.getInventory().insertStack(new ItemStack(Items.HONEY_BOTTLE))) {
                    player.dropItem(new ItemStack(Items.HONEY_BOTTLE), false);
                }
                bl = true;
                world.emitGameEvent((Entity)player, GameEvent.FLUID_PICKUP, pos);
            }
            if (!world.isClient() && bl) {
                player.incrementStat(Stats.USED.getOrCreateStat(lv));
            }
        }
        if (bl) {
            if (!CampfireBlock.isLitCampfireInRange(world, pos)) {
                if (this.hasBees(world, pos)) {
                    this.angerNearbyBees(world, pos);
                }
                this.takeHoney(world, state, pos, player, BeehiveBlockEntity.BeeState.EMERGENCY);
            } else {
                this.takeHoney(world, state, pos);
            }
            return ItemActionResult.success(world.isClient);
        }
        return super.onUseWithItem(stack, state, world, pos, player, hand, hit);
    }

    private boolean hasBees(World world, BlockPos pos) {
        BlockEntity lv = world.getBlockEntity(pos);
        if (lv instanceof BeehiveBlockEntity) {
            BeehiveBlockEntity lv2 = (BeehiveBlockEntity)lv;
            return !lv2.hasNoBees();
        }
        return false;
    }

    public void takeHoney(World world, BlockState state, BlockPos pos, @Nullable PlayerEntity player, BeehiveBlockEntity.BeeState beeState) {
        this.takeHoney(world, state, pos);
        BlockEntity lv = world.getBlockEntity(pos);
        if (lv instanceof BeehiveBlockEntity) {
            BeehiveBlockEntity lv2 = (BeehiveBlockEntity)lv;
            lv2.angerBees(player, state, beeState);
        }
    }

    public void takeHoney(World world, BlockState state, BlockPos pos) {
        world.setBlockState(pos, (BlockState)state.with(HONEY_LEVEL, 0), Block.NOTIFY_ALL);
    }

    @Override
    public void randomDisplayTick(BlockState state, World world, BlockPos pos, Random random) {
        if (state.get(HONEY_LEVEL) >= 5) {
            for (int i = 0; i < random.nextInt(1) + 1; ++i) {
                this.spawnHoneyParticles(world, pos, state);
            }
        }
    }

    private void spawnHoneyParticles(World world, BlockPos pos, BlockState state) {
        if (!state.getFluidState().isEmpty() || world.random.nextFloat() < 0.3f) {
            return;
        }
        VoxelShape lv = state.getCollisionShape(world, pos);
        double d = lv.getMax(Direction.Axis.Y);
        if (d >= 1.0 && !state.isIn(BlockTags.IMPERMEABLE)) {
            double e = lv.getMin(Direction.Axis.Y);
            if (e > 0.0) {
                this.addHoneyParticle(world, pos, lv, (double)pos.getY() + e - 0.05);
            } else {
                BlockPos lv2 = pos.down();
                BlockState lv3 = world.getBlockState(lv2);
                VoxelShape lv4 = lv3.getCollisionShape(world, lv2);
                double f = lv4.getMax(Direction.Axis.Y);
                if ((f < 1.0 || !lv3.isFullCube(world, lv2)) && lv3.getFluidState().isEmpty()) {
                    this.addHoneyParticle(world, pos, lv, (double)pos.getY() - 0.05);
                }
            }
        }
    }

    private void addHoneyParticle(World world, BlockPos pos, VoxelShape shape, double height) {
        this.addHoneyParticle(world, (double)pos.getX() + shape.getMin(Direction.Axis.X), (double)pos.getX() + shape.getMax(Direction.Axis.X), (double)pos.getZ() + shape.getMin(Direction.Axis.Z), (double)pos.getZ() + shape.getMax(Direction.Axis.Z), height);
    }

    private void addHoneyParticle(World world, double minX, double maxX, double minZ, double maxZ, double height) {
        world.addParticle(ParticleTypes.DRIPPING_HONEY, MathHelper.lerp(world.random.nextDouble(), minX, maxX), height, MathHelper.lerp(world.random.nextDouble(), minZ, maxZ), 0.0, 0.0, 0.0);
    }

    @Override
    public BlockState getPlacementState(ItemPlacementContext ctx) {
        return (BlockState)this.getDefaultState().with(FACING, ctx.getHorizontalPlayerFacing().getOpposite());
    }

    @Override
    protected void appendProperties(StateManager.Builder<Block, BlockState> builder) {
        builder.add(HONEY_LEVEL, FACING);
    }

    @Override
    protected BlockRenderType getRenderType(BlockState state) {
        return BlockRenderType.MODEL;
    }

    @Override
    @Nullable
    public BlockEntity createBlockEntity(BlockPos pos, BlockState state) {
        return new BeehiveBlockEntity(pos, state);
    }

    @Override
    @Nullable
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(World world, BlockState state, BlockEntityType<T> type) {
        return world.isClient ? null : BeehiveBlock.validateTicker(type, BlockEntityType.BEEHIVE, BeehiveBlockEntity::serverTick);
    }

    @Override
    public BlockState onBreak(World world, BlockPos pos, BlockState state, PlayerEntity player) {
        BlockEntity lv;
        if (!world.isClient && player.isCreative() && world.getGameRules().getBoolean(GameRules.DO_TILE_DROPS) && (lv = world.getBlockEntity(pos)) instanceof BeehiveBlockEntity) {
            boolean bl;
            BeehiveBlockEntity lv2 = (BeehiveBlockEntity)lv;
            int i = state.get(HONEY_LEVEL);
            boolean bl2 = bl = !lv2.hasNoBees();
            if (bl || i > 0) {
                ItemStack lv3 = new ItemStack(this);
                lv3.applyComponentsFrom(lv2.createComponentMap());
                lv3.set(DataComponentTypes.BLOCK_STATE, BlockStateComponent.DEFAULT.with(HONEY_LEVEL, i));
                ItemEntity lv4 = new ItemEntity(world, pos.getX(), pos.getY(), pos.getZ(), lv3);
                lv4.setToDefaultPickupDelay();
                world.spawnEntity(lv4);
            }
        }
        return super.onBreak(world, pos, state, player);
    }

    @Override
    protected List<ItemStack> getDroppedStacks(BlockState state, LootContextParameterSet.Builder builder) {
        BlockEntity lv2;
        Entity lv = builder.getOptional(LootContextParameters.THIS_ENTITY);
        if ((lv instanceof TntEntity || lv instanceof CreeperEntity || lv instanceof WitherSkullEntity || lv instanceof WitherEntity || lv instanceof TntMinecartEntity) && (lv2 = builder.getOptional(LootContextParameters.BLOCK_ENTITY)) instanceof BeehiveBlockEntity) {
            BeehiveBlockEntity lv3 = (BeehiveBlockEntity)lv2;
            lv3.angerBees(null, state, BeehiveBlockEntity.BeeState.EMERGENCY);
        }
        return super.getDroppedStacks(state, builder);
    }

    @Override
    protected BlockState getStateForNeighborUpdate(BlockState state, Direction direction, BlockState neighborState, WorldAccess world, BlockPos pos, BlockPos neighborPos) {
        BlockEntity lv;
        if (world.getBlockState(neighborPos).getBlock() instanceof FireBlock && (lv = world.getBlockEntity(pos)) instanceof BeehiveBlockEntity) {
            BeehiveBlockEntity lv2 = (BeehiveBlockEntity)lv;
            lv2.angerBees(null, state, BeehiveBlockEntity.BeeState.EMERGENCY);
        }
        return super.getStateForNeighborUpdate(state, direction, neighborState, world, pos, neighborPos);
    }

    @Override
    public BlockState rotate(BlockState state, BlockRotation rotation) {
        return (BlockState)state.with(FACING, rotation.rotate(state.get(FACING)));
    }

    @Override
    public BlockState mirror(BlockState state, BlockMirror mirror) {
        return state.rotate(mirror.getRotation(state.get(FACING)));
    }
}

