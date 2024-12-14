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
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.TntEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.ProjectileEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.stat.Stats;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.BooleanProperty;
import net.minecraft.state.property.Properties;
import net.minecraft.util.Hand;
import net.minecraft.util.ItemActionResult;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.event.GameEvent;
import net.minecraft.world.explosion.Explosion;
import org.jetbrains.annotations.Nullable;

public class TntBlock
extends Block {
    public static final MapCodec<TntBlock> CODEC = TntBlock.createCodec(TntBlock::new);
    public static final BooleanProperty UNSTABLE = Properties.UNSTABLE;

    public MapCodec<TntBlock> getCodec() {
        return CODEC;
    }

    public TntBlock(AbstractBlock.Settings arg) {
        super(arg);
        this.setDefaultState((BlockState)this.getDefaultState().with(UNSTABLE, false));
    }

    @Override
    protected void onBlockAdded(BlockState state, World world, BlockPos pos, BlockState oldState, boolean notify) {
        if (oldState.isOf(state.getBlock())) {
            return;
        }
        if (world.isReceivingRedstonePower(pos)) {
            TntBlock.primeTnt(world, pos);
            world.removeBlock(pos, false);
        }
    }

    @Override
    protected void neighborUpdate(BlockState state, World world, BlockPos pos, Block sourceBlock, BlockPos sourcePos, boolean notify) {
        if (world.isReceivingRedstonePower(pos)) {
            TntBlock.primeTnt(world, pos);
            world.removeBlock(pos, false);
        }
    }

    @Override
    public BlockState onBreak(World world, BlockPos pos, BlockState state, PlayerEntity player) {
        if (!world.isClient() && !player.isCreative() && state.get(UNSTABLE).booleanValue()) {
            TntBlock.primeTnt(world, pos);
        }
        return super.onBreak(world, pos, state, player);
    }

    @Override
    public void onDestroyedByExplosion(World world, BlockPos pos, Explosion explosion) {
        if (world.isClient) {
            return;
        }
        TntEntity lv = new TntEntity(world, (double)pos.getX() + 0.5, pos.getY(), (double)pos.getZ() + 0.5, explosion.getCausingEntity());
        int i = lv.getFuse();
        lv.setFuse((short)(world.random.nextInt(i / 4) + i / 8));
        world.spawnEntity(lv);
    }

    public static void primeTnt(World world, BlockPos pos) {
        TntBlock.primeTnt(world, pos, null);
    }

    private static void primeTnt(World world, BlockPos pos, @Nullable LivingEntity igniter) {
        if (world.isClient) {
            return;
        }
        TntEntity lv = new TntEntity(world, (double)pos.getX() + 0.5, pos.getY(), (double)pos.getZ() + 0.5, igniter);
        world.spawnEntity(lv);
        world.playSound(null, lv.getX(), lv.getY(), lv.getZ(), SoundEvents.ENTITY_TNT_PRIMED, SoundCategory.BLOCKS, 1.0f, 1.0f);
        world.emitGameEvent((Entity)igniter, GameEvent.PRIME_FUSE, pos);
    }

    @Override
    protected ItemActionResult onUseWithItem(ItemStack stack, BlockState state, World world, BlockPos pos, PlayerEntity player, Hand hand, BlockHitResult hit) {
        if (!stack.isOf(Items.FLINT_AND_STEEL) && !stack.isOf(Items.FIRE_CHARGE)) {
            return super.onUseWithItem(stack, state, world, pos, player, hand, hit);
        }
        TntBlock.primeTnt(world, pos, player);
        world.setBlockState(pos, Blocks.AIR.getDefaultState(), Block.NOTIFY_ALL_AND_REDRAW);
        Item lv = stack.getItem();
        if (stack.isOf(Items.FLINT_AND_STEEL)) {
            stack.damage(1, player, LivingEntity.getSlotForHand(hand));
        } else {
            stack.decrementUnlessCreative(1, player);
        }
        player.incrementStat(Stats.USED.getOrCreateStat(lv));
        return ItemActionResult.success(world.isClient);
    }

    @Override
    protected void onProjectileHit(World world, BlockState state, BlockHitResult hit, ProjectileEntity projectile) {
        if (!world.isClient) {
            BlockPos lv = hit.getBlockPos();
            Entity lv2 = projectile.getOwner();
            if (projectile.isOnFire() && projectile.canModifyAt(world, lv)) {
                TntBlock.primeTnt(world, lv, lv2 instanceof LivingEntity ? (LivingEntity)lv2 : null);
                world.removeBlock(lv, false);
            }
        }
    }

    @Override
    public boolean shouldDropItemsOnExplosion(Explosion explosion) {
        return false;
    }

    @Override
    protected void appendProperties(StateManager.Builder<Block, BlockState> builder) {
        builder.add(UNSTABLE);
    }
}

