/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.world.gen.feature;

import com.mojang.serialization.Codec;
import net.minecraft.block.BlockState;
import net.minecraft.block.MushroomBlock;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.WorldAccess;
import net.minecraft.world.gen.feature.HugeMushroomFeature;
import net.minecraft.world.gen.feature.HugeMushroomFeatureConfig;

public class HugeRedMushroomFeature
extends HugeMushroomFeature {
    public HugeRedMushroomFeature(Codec<HugeMushroomFeatureConfig> codec) {
        super(codec);
    }

    @Override
    protected void generateCap(WorldAccess world, Random random, BlockPos start, int y, BlockPos.Mutable mutable, HugeMushroomFeatureConfig config) {
        for (int j = y - 3; j <= y; ++j) {
            int k = j < y ? config.foliageRadius : config.foliageRadius - 1;
            int l = config.foliageRadius - 2;
            for (int m = -k; m <= k; ++m) {
                for (int n = -k; n <= k; ++n) {
                    boolean bl6;
                    boolean bl = m == -k;
                    boolean bl2 = m == k;
                    boolean bl3 = n == -k;
                    boolean bl4 = n == k;
                    boolean bl5 = bl || bl2;
                    boolean bl7 = bl6 = bl3 || bl4;
                    if (j < y && bl5 == bl6) continue;
                    mutable.set(start, m, j, n);
                    if (world.getBlockState(mutable).isOpaqueFullCube(world, mutable)) continue;
                    BlockState lv = config.capProvider.get(random, start);
                    if (lv.contains(MushroomBlock.WEST) && lv.contains(MushroomBlock.EAST) && lv.contains(MushroomBlock.NORTH) && lv.contains(MushroomBlock.SOUTH) && lv.contains(MushroomBlock.UP)) {
                        lv = (BlockState)((BlockState)((BlockState)((BlockState)((BlockState)lv.with(MushroomBlock.UP, j >= y - 1)).with(MushroomBlock.WEST, m < -l)).with(MushroomBlock.EAST, m > l)).with(MushroomBlock.NORTH, n < -l)).with(MushroomBlock.SOUTH, n > l);
                    }
                    this.setBlockState(world, mutable, lv);
                }
            }
        }
    }

    @Override
    protected int getCapSize(int i, int j, int capSize, int y) {
        int m = 0;
        if (y < j && y >= j - 3) {
            m = capSize;
        } else if (y == j) {
            m = capSize;
        }
        return m;
    }
}

