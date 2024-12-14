/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.world.gen.feature;

import com.mojang.serialization.Codec;
import net.minecraft.block.BlockState;
import net.minecraft.registry.tag.BlockTags;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.StructureWorldAccess;
import net.minecraft.world.WorldAccess;
import net.minecraft.world.gen.feature.Feature;
import net.minecraft.world.gen.feature.HugeMushroomFeatureConfig;
import net.minecraft.world.gen.feature.util.FeatureContext;

public abstract class HugeMushroomFeature
extends Feature<HugeMushroomFeatureConfig> {
    public HugeMushroomFeature(Codec<HugeMushroomFeatureConfig> codec) {
        super(codec);
    }

    protected void generateStem(WorldAccess world, Random random, BlockPos pos, HugeMushroomFeatureConfig config, int height, BlockPos.Mutable mutablePos) {
        for (int j = 0; j < height; ++j) {
            mutablePos.set(pos).move(Direction.UP, j);
            if (world.getBlockState(mutablePos).isOpaqueFullCube(world, mutablePos)) continue;
            this.setBlockState(world, mutablePos, config.stemProvider.get(random, pos));
        }
    }

    protected int getHeight(Random random) {
        int i = random.nextInt(3) + 4;
        if (random.nextInt(12) == 0) {
            i *= 2;
        }
        return i;
    }

    protected boolean canGenerate(WorldAccess world, BlockPos pos, int height, BlockPos.Mutable mutablePos, HugeMushroomFeatureConfig config) {
        int j = pos.getY();
        if (j < world.getBottomY() + 1 || j + height + 1 >= world.getTopY()) {
            return false;
        }
        BlockState lv = world.getBlockState(pos.down());
        if (!HugeMushroomFeature.isSoil(lv) && !lv.isIn(BlockTags.MUSHROOM_GROW_BLOCK)) {
            return false;
        }
        for (int k = 0; k <= height; ++k) {
            int l = this.getCapSize(-1, -1, config.foliageRadius, k);
            for (int m = -l; m <= l; ++m) {
                for (int n = -l; n <= l; ++n) {
                    BlockState lv2 = world.getBlockState(mutablePos.set(pos, m, k, n));
                    if (lv2.isAir() || lv2.isIn(BlockTags.LEAVES)) continue;
                    return false;
                }
            }
        }
        return true;
    }

    @Override
    public boolean generate(FeatureContext<HugeMushroomFeatureConfig> context) {
        BlockPos.Mutable lv5;
        StructureWorldAccess lv = context.getWorld();
        BlockPos lv2 = context.getOrigin();
        Random lv3 = context.getRandom();
        HugeMushroomFeatureConfig lv4 = context.getConfig();
        int i = this.getHeight(lv3);
        if (!this.canGenerate(lv, lv2, i, lv5 = new BlockPos.Mutable(), lv4)) {
            return false;
        }
        this.generateCap(lv, lv3, lv2, i, lv5, lv4);
        this.generateStem(lv, lv3, lv2, lv4, i, lv5);
        return true;
    }

    protected abstract int getCapSize(int var1, int var2, int var3, int var4);

    protected abstract void generateCap(WorldAccess var1, Random var2, BlockPos var3, int var4, BlockPos.Mutable var5, HugeMushroomFeatureConfig var6);
}

