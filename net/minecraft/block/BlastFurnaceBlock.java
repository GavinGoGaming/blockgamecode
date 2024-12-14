/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 * 
 * Could not load the following classes:
 *  org.jetbrains.annotations.Nullable
 */
package net.minecraft.block;

import com.mojang.serialization.MapCodec;
import net.minecraft.block.AbstractBlock;
import net.minecraft.block.AbstractFurnaceBlock;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlastFurnaceBlockEntity;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityTicker;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.screen.NamedScreenHandlerFactory;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.stat.Stats;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

public class BlastFurnaceBlock
extends AbstractFurnaceBlock {
    public static final MapCodec<BlastFurnaceBlock> CODEC = BlastFurnaceBlock.createCodec(BlastFurnaceBlock::new);

    public MapCodec<BlastFurnaceBlock> getCodec() {
        return CODEC;
    }

    protected BlastFurnaceBlock(AbstractBlock.Settings arg) {
        super(arg);
    }

    @Override
    public BlockEntity createBlockEntity(BlockPos pos, BlockState state) {
        return new BlastFurnaceBlockEntity(pos, state);
    }

    @Override
    @Nullable
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(World world, BlockState state, BlockEntityType<T> type) {
        return BlastFurnaceBlock.validateTicker(world, type, BlockEntityType.BLAST_FURNACE);
    }

    @Override
    protected void openScreen(World world, BlockPos pos, PlayerEntity player) {
        BlockEntity lv = world.getBlockEntity(pos);
        if (lv instanceof BlastFurnaceBlockEntity) {
            player.openHandledScreen((NamedScreenHandlerFactory)((Object)lv));
            player.incrementStat(Stats.INTERACT_WITH_BLAST_FURNACE);
        }
    }

    @Override
    public void randomDisplayTick(BlockState state, World world, BlockPos pos, Random random) {
        if (!state.get(LIT).booleanValue()) {
            return;
        }
        double d = (double)pos.getX() + 0.5;
        double e = pos.getY();
        double f = (double)pos.getZ() + 0.5;
        if (random.nextDouble() < 0.1) {
            world.playSound(d, e, f, SoundEvents.BLOCK_BLASTFURNACE_FIRE_CRACKLE, SoundCategory.BLOCKS, 1.0f, 1.0f, false);
        }
        Direction lv = state.get(FACING);
        Direction.Axis lv2 = lv.getAxis();
        double g = 0.52;
        double h = random.nextDouble() * 0.6 - 0.3;
        double i = lv2 == Direction.Axis.X ? (double)lv.getOffsetX() * 0.52 : h;
        double j = random.nextDouble() * 9.0 / 16.0;
        double k = lv2 == Direction.Axis.Z ? (double)lv.getOffsetZ() * 0.52 : h;
        world.addParticle(ParticleTypes.SMOKE, d + i, e + j, f + k, 0.0, 0.0, 0.0);
    }
}

