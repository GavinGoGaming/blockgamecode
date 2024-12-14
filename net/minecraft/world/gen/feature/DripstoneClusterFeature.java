/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.world.gen.feature;

import com.mojang.serialization.Codec;
import java.util.Optional;
import java.util.OptionalInt;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.registry.tag.BlockTags;
import net.minecraft.registry.tag.FluidTags;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.floatprovider.ClampedNormalFloatProvider;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.StructureWorldAccess;
import net.minecraft.world.WorldAccess;
import net.minecraft.world.WorldView;
import net.minecraft.world.gen.feature.DripstoneClusterFeatureConfig;
import net.minecraft.world.gen.feature.Feature;
import net.minecraft.world.gen.feature.util.CaveSurface;
import net.minecraft.world.gen.feature.util.DripstoneHelper;
import net.minecraft.world.gen.feature.util.FeatureContext;

public class DripstoneClusterFeature
extends Feature<DripstoneClusterFeatureConfig> {
    public DripstoneClusterFeature(Codec<DripstoneClusterFeatureConfig> codec) {
        super(codec);
    }

    @Override
    public boolean generate(FeatureContext<DripstoneClusterFeatureConfig> context) {
        StructureWorldAccess lv = context.getWorld();
        BlockPos lv2 = context.getOrigin();
        DripstoneClusterFeatureConfig lv3 = context.getConfig();
        Random lv4 = context.getRandom();
        if (!DripstoneHelper.canGenerate(lv, lv2)) {
            return false;
        }
        int i = lv3.height.get(lv4);
        float f = lv3.wetness.get(lv4);
        float g = lv3.density.get(lv4);
        int j = lv3.radius.get(lv4);
        int k = lv3.radius.get(lv4);
        for (int l = -j; l <= j; ++l) {
            for (int m = -k; m <= k; ++m) {
                double d = this.dripstoneChance(j, k, l, m, lv3);
                BlockPos lv5 = lv2.add(l, 0, m);
                this.generate(lv, lv4, lv5, l, m, f, d, i, g, lv3);
            }
        }
        return true;
    }

    private void generate(StructureWorldAccess world, Random random, BlockPos pos, int localX, int localZ, float wetness, double dripstoneChance, int height, float density, DripstoneClusterFeatureConfig config) {
        boolean bl4;
        int w;
        int p;
        boolean bl3;
        int o;
        int m;
        boolean bl2;
        CaveSurface lv;
        boolean bl;
        Optional<CaveSurface> optional = CaveSurface.create(world, pos, config.floorToCeilingSearchRange, DripstoneHelper::canGenerate, DripstoneHelper::cannotGenerate);
        if (optional.isEmpty()) {
            return;
        }
        OptionalInt optionalInt = optional.get().getCeilingHeight();
        OptionalInt optionalInt2 = optional.get().getFloorHeight();
        if (optionalInt.isEmpty() && optionalInt2.isEmpty()) {
            return;
        }
        boolean bl5 = bl = random.nextFloat() < wetness;
        if (bl && optionalInt2.isPresent() && this.canWaterSpawn(world, pos.withY(optionalInt2.getAsInt()))) {
            int l = optionalInt2.getAsInt();
            lv = optional.get().withFloor(OptionalInt.of(l - 1));
            world.setBlockState(pos.withY(l), Blocks.WATER.getDefaultState(), Block.NOTIFY_LISTENERS);
        } else {
            lv = optional.get();
        }
        OptionalInt optionalInt3 = lv.getFloorHeight();
        boolean bl6 = bl2 = random.nextDouble() < dripstoneChance;
        if (optionalInt.isPresent() && bl2 && !this.isLava(world, pos.withY(optionalInt.getAsInt()))) {
            m = config.dripstoneBlockLayerThickness.get(random);
            this.placeDripstoneBlocks(world, pos.withY(optionalInt.getAsInt()), m, Direction.UP);
            int n = optionalInt3.isPresent() ? Math.min(height, optionalInt.getAsInt() - optionalInt3.getAsInt()) : height;
            o = this.getHeight(random, localX, localZ, density, n, config);
        } else {
            o = 0;
        }
        boolean bl7 = bl3 = random.nextDouble() < dripstoneChance;
        if (optionalInt3.isPresent() && bl3 && !this.isLava(world, pos.withY(optionalInt3.getAsInt()))) {
            p = config.dripstoneBlockLayerThickness.get(random);
            this.placeDripstoneBlocks(world, pos.withY(optionalInt3.getAsInt()), p, Direction.DOWN);
            m = optionalInt.isPresent() ? Math.max(0, o + MathHelper.nextBetween(random, -config.maxStalagmiteStalactiteHeightDiff, config.maxStalagmiteStalactiteHeightDiff)) : this.getHeight(random, localX, localZ, density, height, config);
        } else {
            m = 0;
        }
        if (optionalInt.isPresent() && optionalInt3.isPresent() && optionalInt.getAsInt() - o <= optionalInt3.getAsInt() + m) {
            int q = optionalInt3.getAsInt();
            int r = optionalInt.getAsInt();
            int s = Math.max(r - o, q + 1);
            int t = Math.min(q + m, r - 1);
            int u = MathHelper.nextBetween(random, s, t + 1);
            int v = u - 1;
            p = r - u;
            w = v - q;
        } else {
            p = o;
            w = m;
        }
        boolean bl8 = bl4 = random.nextBoolean() && p > 0 && w > 0 && lv.getOptionalHeight().isPresent() && p + w == lv.getOptionalHeight().getAsInt();
        if (optionalInt.isPresent()) {
            DripstoneHelper.generatePointedDripstone(world, pos.withY(optionalInt.getAsInt() - 1), Direction.DOWN, p, bl4);
        }
        if (optionalInt3.isPresent()) {
            DripstoneHelper.generatePointedDripstone(world, pos.withY(optionalInt3.getAsInt() + 1), Direction.UP, w, bl4);
        }
    }

    private boolean isLava(WorldView world, BlockPos pos) {
        return world.getBlockState(pos).isOf(Blocks.LAVA);
    }

    private int getHeight(Random random, int localX, int localZ, float density, int height, DripstoneClusterFeatureConfig config) {
        if (random.nextFloat() > density) {
            return 0;
        }
        int l = Math.abs(localX) + Math.abs(localZ);
        float g = (float)MathHelper.clampedMap((double)l, 0.0, (double)config.maxDistanceFromCenterAffectingHeightBias, (double)height / 2.0, 0.0);
        return (int)DripstoneClusterFeature.clampedGaussian(random, 0.0f, height, g, config.heightDeviation);
    }

    private boolean canWaterSpawn(StructureWorldAccess world, BlockPos pos) {
        BlockState lv = world.getBlockState(pos);
        if (lv.isOf(Blocks.WATER) || lv.isOf(Blocks.DRIPSTONE_BLOCK) || lv.isOf(Blocks.POINTED_DRIPSTONE)) {
            return false;
        }
        if (world.getBlockState(pos.up()).getFluidState().isIn(FluidTags.WATER)) {
            return false;
        }
        for (Direction lv2 : Direction.Type.HORIZONTAL) {
            if (this.isStoneOrWater(world, pos.offset(lv2))) continue;
            return false;
        }
        return this.isStoneOrWater(world, pos.down());
    }

    private boolean isStoneOrWater(WorldAccess world, BlockPos pos) {
        BlockState lv = world.getBlockState(pos);
        return lv.isIn(BlockTags.BASE_STONE_OVERWORLD) || lv.getFluidState().isIn(FluidTags.WATER);
    }

    private void placeDripstoneBlocks(StructureWorldAccess world, BlockPos pos, int height, Direction direction) {
        BlockPos.Mutable lv = pos.mutableCopy();
        for (int j = 0; j < height; ++j) {
            if (!DripstoneHelper.generateDripstoneBlock(world, lv)) {
                return;
            }
            lv.move(direction);
        }
    }

    private double dripstoneChance(int radiusX, int radiusZ, int localX, int localZ, DripstoneClusterFeatureConfig config) {
        int m = radiusX - Math.abs(localX);
        int n = radiusZ - Math.abs(localZ);
        int o = Math.min(m, n);
        return MathHelper.clampedMap(o, 0.0f, config.maxDistanceFromCenterAffectingChanceOfDripstoneColumn, config.chanceOfDripstoneColumnAtMaxDistanceFromCenter, 1.0f);
    }

    private static float clampedGaussian(Random random, float min, float max, float mean, float deviation) {
        return ClampedNormalFloatProvider.get(random, mean, deviation, min, max);
    }
}

