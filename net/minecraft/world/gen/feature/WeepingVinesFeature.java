/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.world.gen.feature;

import com.mojang.serialization.Codec;
import net.minecraft.block.AbstractPlantStemBlock;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3i;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.StructureWorldAccess;
import net.minecraft.world.WorldAccess;
import net.minecraft.world.gen.feature.DefaultFeatureConfig;
import net.minecraft.world.gen.feature.Feature;
import net.minecraft.world.gen.feature.util.FeatureContext;

public class WeepingVinesFeature
extends Feature<DefaultFeatureConfig> {
    private static final Direction[] DIRECTIONS = Direction.values();

    public WeepingVinesFeature(Codec<DefaultFeatureConfig> codec) {
        super(codec);
    }

    @Override
    public boolean generate(FeatureContext<DefaultFeatureConfig> context) {
        StructureWorldAccess lv = context.getWorld();
        BlockPos lv2 = context.getOrigin();
        Random lv3 = context.getRandom();
        if (!lv.isAir(lv2)) {
            return false;
        }
        BlockState lv4 = lv.getBlockState(lv2.up());
        if (!lv4.isOf(Blocks.NETHERRACK) && !lv4.isOf(Blocks.NETHER_WART_BLOCK)) {
            return false;
        }
        this.generateNetherWartBlocksInArea(lv, lv3, lv2);
        this.generateVinesInArea(lv, lv3, lv2);
        return true;
    }

    private void generateNetherWartBlocksInArea(WorldAccess world, Random random, BlockPos pos) {
        world.setBlockState(pos, Blocks.NETHER_WART_BLOCK.getDefaultState(), Block.NOTIFY_LISTENERS);
        BlockPos.Mutable lv = new BlockPos.Mutable();
        BlockPos.Mutable lv2 = new BlockPos.Mutable();
        for (int i = 0; i < 200; ++i) {
            lv.set(pos, random.nextInt(6) - random.nextInt(6), random.nextInt(2) - random.nextInt(5), random.nextInt(6) - random.nextInt(6));
            if (!world.isAir(lv)) continue;
            int j = 0;
            for (Direction lv3 : DIRECTIONS) {
                BlockState lv4 = world.getBlockState(lv2.set((Vec3i)lv, lv3));
                if (lv4.isOf(Blocks.NETHERRACK) || lv4.isOf(Blocks.NETHER_WART_BLOCK)) {
                    ++j;
                }
                if (j > 1) break;
            }
            if (j != true) continue;
            world.setBlockState(lv, Blocks.NETHER_WART_BLOCK.getDefaultState(), Block.NOTIFY_LISTENERS);
        }
    }

    private void generateVinesInArea(WorldAccess world, Random random, BlockPos pos) {
        BlockPos.Mutable lv = new BlockPos.Mutable();
        for (int i = 0; i < 100; ++i) {
            BlockState lv2;
            lv.set(pos, random.nextInt(8) - random.nextInt(8), random.nextInt(2) - random.nextInt(7), random.nextInt(8) - random.nextInt(8));
            if (!world.isAir(lv) || !(lv2 = world.getBlockState((BlockPos)lv.up())).isOf(Blocks.NETHERRACK) && !lv2.isOf(Blocks.NETHER_WART_BLOCK)) continue;
            int j = MathHelper.nextInt(random, 1, 8);
            if (random.nextInt(6) == 0) {
                j *= 2;
            }
            if (random.nextInt(5) == 0) {
                j = 1;
            }
            int k = 17;
            int l = 25;
            WeepingVinesFeature.generateVineColumn(world, random, lv, j, 17, 25);
        }
    }

    public static void generateVineColumn(WorldAccess world, Random random, BlockPos.Mutable pos, int length, int minAge, int maxAge) {
        for (int l = 0; l <= length; ++l) {
            if (world.isAir(pos)) {
                if (l == length || !world.isAir((BlockPos)pos.down())) {
                    world.setBlockState(pos, (BlockState)Blocks.WEEPING_VINES.getDefaultState().with(AbstractPlantStemBlock.AGE, MathHelper.nextInt(random, minAge, maxAge)), Block.NOTIFY_LISTENERS);
                    break;
                }
                world.setBlockState(pos, Blocks.WEEPING_VINES_PLANT.getDefaultState(), Block.NOTIFY_LISTENERS);
            }
            pos.move(Direction.DOWN);
        }
    }
}

