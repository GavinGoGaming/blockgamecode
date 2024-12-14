/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.world.gen.feature;

import com.mojang.serialization.Codec;
import net.minecraft.block.Block;
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

public class BasaltPillarFeature
extends Feature<DefaultFeatureConfig> {
    public BasaltPillarFeature(Codec<DefaultFeatureConfig> codec) {
        super(codec);
    }

    @Override
    public boolean generate(FeatureContext<DefaultFeatureConfig> context) {
        BlockPos lv = context.getOrigin();
        StructureWorldAccess lv2 = context.getWorld();
        Random lv3 = context.getRandom();
        if (!lv2.isAir(lv) || lv2.isAir(lv.up())) {
            return false;
        }
        BlockPos.Mutable lv4 = lv.mutableCopy();
        BlockPos.Mutable lv5 = lv.mutableCopy();
        boolean bl = true;
        boolean bl2 = true;
        boolean bl3 = true;
        boolean bl4 = true;
        while (lv2.isAir(lv4)) {
            if (lv2.isOutOfHeightLimit(lv4)) {
                return true;
            }
            lv2.setBlockState(lv4, Blocks.BASALT.getDefaultState(), Block.NOTIFY_LISTENERS);
            bl = bl && this.stopOrPlaceBasalt(lv2, lv3, lv5.set((Vec3i)lv4, Direction.NORTH));
            bl2 = bl2 && this.stopOrPlaceBasalt(lv2, lv3, lv5.set((Vec3i)lv4, Direction.SOUTH));
            bl3 = bl3 && this.stopOrPlaceBasalt(lv2, lv3, lv5.set((Vec3i)lv4, Direction.WEST));
            bl4 = bl4 && this.stopOrPlaceBasalt(lv2, lv3, lv5.set((Vec3i)lv4, Direction.EAST));
            lv4.move(Direction.DOWN);
        }
        lv4.move(Direction.UP);
        this.tryPlaceBasalt(lv2, lv3, lv5.set((Vec3i)lv4, Direction.NORTH));
        this.tryPlaceBasalt(lv2, lv3, lv5.set((Vec3i)lv4, Direction.SOUTH));
        this.tryPlaceBasalt(lv2, lv3, lv5.set((Vec3i)lv4, Direction.WEST));
        this.tryPlaceBasalt(lv2, lv3, lv5.set((Vec3i)lv4, Direction.EAST));
        lv4.move(Direction.DOWN);
        BlockPos.Mutable lv6 = new BlockPos.Mutable();
        for (int i = -3; i < 4; ++i) {
            for (int j = -3; j < 4; ++j) {
                int k = MathHelper.abs(i) * MathHelper.abs(j);
                if (lv3.nextInt(10) >= 10 - k) continue;
                lv6.set(lv4.add(i, 0, j));
                int l = 3;
                while (lv2.isAir(lv5.set((Vec3i)lv6, Direction.DOWN))) {
                    lv6.move(Direction.DOWN);
                    if (--l > 0) continue;
                }
                if (lv2.isAir(lv5.set((Vec3i)lv6, Direction.DOWN))) continue;
                lv2.setBlockState(lv6, Blocks.BASALT.getDefaultState(), Block.NOTIFY_LISTENERS);
            }
        }
        return true;
    }

    private void tryPlaceBasalt(WorldAccess world, Random random, BlockPos pos) {
        if (random.nextBoolean()) {
            world.setBlockState(pos, Blocks.BASALT.getDefaultState(), Block.NOTIFY_LISTENERS);
        }
    }

    private boolean stopOrPlaceBasalt(WorldAccess world, Random random, BlockPos pos) {
        if (random.nextInt(10) != 0) {
            world.setBlockState(pos, Blocks.BASALT.getDefaultState(), Block.NOTIFY_LISTENERS);
            return true;
        }
        return false;
    }
}

