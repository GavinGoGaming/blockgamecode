/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.block;

import com.mojang.serialization.MapCodec;
import net.minecraft.block.AbstractBlock;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.ExperienceDroppingBlock;
import net.minecraft.block.SculkShriekerBlock;
import net.minecraft.block.SculkSpreadable;
import net.minecraft.block.entity.SculkSpreadManager;
import net.minecraft.fluid.Fluids;
import net.minecraft.sound.SoundCategory;
import net.minecraft.state.property.Properties;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.intprovider.ConstantIntProvider;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.WorldAccess;

public class SculkBlock
extends ExperienceDroppingBlock
implements SculkSpreadable {
    public static final MapCodec<SculkBlock> CODEC = SculkBlock.createCodec(SculkBlock::new);

    public MapCodec<SculkBlock> getCodec() {
        return CODEC;
    }

    public SculkBlock(AbstractBlock.Settings arg) {
        super(ConstantIntProvider.create(1), arg);
    }

    @Override
    public int spread(SculkSpreadManager.Cursor cursor, WorldAccess world, BlockPos catalystPos, Random random, SculkSpreadManager spreadManager, boolean shouldConvertToBlock) {
        int i = cursor.getCharge();
        if (i == 0 || random.nextInt(spreadManager.getSpreadChance()) != 0) {
            return i;
        }
        BlockPos lv = cursor.getPos();
        boolean bl2 = lv.isWithinDistance(catalystPos, (double)spreadManager.getMaxDistance());
        if (bl2 || !SculkBlock.shouldNotDecay(world, lv)) {
            if (random.nextInt(spreadManager.getDecayChance()) != 0) {
                return i;
            }
            return i - (bl2 ? 1 : SculkBlock.getDecay(spreadManager, lv, catalystPos, i));
        }
        int j = spreadManager.getExtraBlockChance();
        if (random.nextInt(j) < i) {
            BlockPos lv2 = lv.up();
            BlockState lv3 = this.getExtraBlockState(world, lv2, random, spreadManager.isWorldGen());
            world.setBlockState(lv2, lv3, Block.NOTIFY_ALL);
            world.playSound(null, lv, lv3.getSoundGroup().getPlaceSound(), SoundCategory.BLOCKS, 1.0f, 1.0f);
        }
        return Math.max(0, i - j);
    }

    private static int getDecay(SculkSpreadManager spreadManager, BlockPos cursorPos, BlockPos catalystPos, int charge) {
        int j = spreadManager.getMaxDistance();
        float f = MathHelper.square((float)Math.sqrt(cursorPos.getSquaredDistance(catalystPos)) - (float)j);
        int k = MathHelper.square(24 - j);
        float g = Math.min(1.0f, f / (float)k);
        return Math.max(1, (int)((float)charge * g * 0.5f));
    }

    private BlockState getExtraBlockState(WorldAccess world, BlockPos pos, Random random, boolean allowShrieker) {
        BlockState lv = random.nextInt(11) == 0 ? (BlockState)Blocks.SCULK_SHRIEKER.getDefaultState().with(SculkShriekerBlock.CAN_SUMMON, allowShrieker) : Blocks.SCULK_SENSOR.getDefaultState();
        if (lv.contains(Properties.WATERLOGGED) && !world.getFluidState(pos).isEmpty()) {
            return (BlockState)lv.with(Properties.WATERLOGGED, true);
        }
        return lv;
    }

    private static boolean shouldNotDecay(WorldAccess world, BlockPos pos) {
        BlockState lv = world.getBlockState(pos.up());
        if (!(lv.isAir() || lv.isOf(Blocks.WATER) && lv.getFluidState().isOf(Fluids.WATER))) {
            return false;
        }
        int i = 0;
        for (BlockPos lv2 : BlockPos.iterate(pos.add(-4, 0, -4), pos.add(4, 2, 4))) {
            BlockState lv3 = world.getBlockState(lv2);
            if (lv3.isOf(Blocks.SCULK_SENSOR) || lv3.isOf(Blocks.SCULK_SHRIEKER)) {
                ++i;
            }
            if (i <= 2) continue;
            return false;
        }
        return true;
    }

    @Override
    public boolean shouldConvertToSpreadable() {
        return false;
    }
}

