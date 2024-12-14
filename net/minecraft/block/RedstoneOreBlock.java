/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.block;

import com.mojang.serialization.MapCodec;
import net.minecraft.block.AbstractBlock;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.RedstoneTorchBlock;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.BlockItem;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.item.ItemStack;
import net.minecraft.particle.DustParticleEffect;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.BooleanProperty;
import net.minecraft.util.Hand;
import net.minecraft.util.ItemActionResult;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.intprovider.UniformIntProvider;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.World;

public class RedstoneOreBlock
extends Block {
    public static final MapCodec<RedstoneOreBlock> CODEC = RedstoneOreBlock.createCodec(RedstoneOreBlock::new);
    public static final BooleanProperty LIT = RedstoneTorchBlock.LIT;

    public MapCodec<RedstoneOreBlock> getCodec() {
        return CODEC;
    }

    public RedstoneOreBlock(AbstractBlock.Settings arg) {
        super(arg);
        this.setDefaultState((BlockState)this.getDefaultState().with(LIT, false));
    }

    @Override
    protected void onBlockBreakStart(BlockState state, World world, BlockPos pos, PlayerEntity player) {
        RedstoneOreBlock.light(state, world, pos);
        super.onBlockBreakStart(state, world, pos, player);
    }

    @Override
    public void onSteppedOn(World world, BlockPos pos, BlockState state, Entity entity) {
        if (!entity.bypassesSteppingEffects()) {
            RedstoneOreBlock.light(state, world, pos);
        }
        super.onSteppedOn(world, pos, state, entity);
    }

    @Override
    protected ItemActionResult onUseWithItem(ItemStack stack, BlockState state, World world, BlockPos pos, PlayerEntity player, Hand hand, BlockHitResult hit) {
        if (world.isClient) {
            RedstoneOreBlock.spawnParticles(world, pos);
        } else {
            RedstoneOreBlock.light(state, world, pos);
        }
        if (stack.getItem() instanceof BlockItem && new ItemPlacementContext(player, hand, stack, hit).canPlace()) {
            return ItemActionResult.SKIP_DEFAULT_BLOCK_INTERACTION;
        }
        return ItemActionResult.SUCCESS;
    }

    private static void light(BlockState state, World world, BlockPos pos) {
        RedstoneOreBlock.spawnParticles(world, pos);
        if (!state.get(LIT).booleanValue()) {
            world.setBlockState(pos, (BlockState)state.with(LIT, true), Block.NOTIFY_ALL);
        }
    }

    @Override
    protected boolean hasRandomTicks(BlockState state) {
        return state.get(LIT);
    }

    @Override
    protected void randomTick(BlockState state, ServerWorld world, BlockPos pos, Random random) {
        if (state.get(LIT).booleanValue()) {
            world.setBlockState(pos, (BlockState)state.with(LIT, false), Block.NOTIFY_ALL);
        }
    }

    @Override
    protected void onStacksDropped(BlockState state, ServerWorld world, BlockPos pos, ItemStack tool, boolean dropExperience) {
        super.onStacksDropped(state, world, pos, tool, dropExperience);
        if (dropExperience) {
            this.dropExperienceWhenMined(world, pos, tool, UniformIntProvider.create(1, 5));
        }
    }

    @Override
    public void randomDisplayTick(BlockState state, World world, BlockPos pos, Random random) {
        if (state.get(LIT).booleanValue()) {
            RedstoneOreBlock.spawnParticles(world, pos);
        }
    }

    private static void spawnParticles(World world, BlockPos pos) {
        double d = 0.5625;
        Random lv = world.random;
        for (Direction lv2 : Direction.values()) {
            BlockPos lv3 = pos.offset(lv2);
            if (world.getBlockState(lv3).isOpaqueFullCube(world, lv3)) continue;
            Direction.Axis lv4 = lv2.getAxis();
            double e = lv4 == Direction.Axis.X ? 0.5 + 0.5625 * (double)lv2.getOffsetX() : (double)lv.nextFloat();
            double f = lv4 == Direction.Axis.Y ? 0.5 + 0.5625 * (double)lv2.getOffsetY() : (double)lv.nextFloat();
            double g = lv4 == Direction.Axis.Z ? 0.5 + 0.5625 * (double)lv2.getOffsetZ() : (double)lv.nextFloat();
            world.addParticle(DustParticleEffect.DEFAULT, (double)pos.getX() + e, (double)pos.getY() + f, (double)pos.getZ() + g, 0.0, 0.0, 0.0);
        }
    }

    @Override
    protected void appendProperties(StateManager.Builder<Block, BlockState> builder) {
        builder.add(LIT);
    }
}

