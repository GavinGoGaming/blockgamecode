/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.world.gen.foliage;

import com.mojang.datafixers.Products;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.block.BlockState;
import net.minecraft.fluid.Fluids;
import net.minecraft.registry.Registries;
import net.minecraft.state.property.Properties;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.intprovider.IntProvider;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.TestableWorld;
import net.minecraft.world.gen.feature.TreeFeature;
import net.minecraft.world.gen.feature.TreeFeatureConfig;
import net.minecraft.world.gen.foliage.FoliagePlacerType;

public abstract class FoliagePlacer {
    public static final Codec<FoliagePlacer> TYPE_CODEC = Registries.FOLIAGE_PLACER_TYPE.getCodec().dispatch(FoliagePlacer::getType, FoliagePlacerType::getCodec);
    protected final IntProvider radius;
    protected final IntProvider offset;

    protected static <P extends FoliagePlacer> Products.P2<RecordCodecBuilder.Mu<P>, IntProvider, IntProvider> fillFoliagePlacerFields(RecordCodecBuilder.Instance<P> instance) {
        return instance.group(((MapCodec)IntProvider.createValidatingCodec(0, 16).fieldOf("radius")).forGetter(placer -> placer.radius), ((MapCodec)IntProvider.createValidatingCodec(0, 16).fieldOf("offset")).forGetter(placer -> placer.offset));
    }

    public FoliagePlacer(IntProvider radius, IntProvider offset) {
        this.radius = radius;
        this.offset = offset;
    }

    protected abstract FoliagePlacerType<?> getType();

    public void generate(TestableWorld world, BlockPlacer placer, Random random, TreeFeatureConfig config, int trunkHeight, TreeNode treeNode, int foliageHeight, int radius) {
        this.generate(world, placer, random, config, trunkHeight, treeNode, foliageHeight, radius, this.getRandomOffset(random));
    }

    protected abstract void generate(TestableWorld var1, BlockPlacer var2, Random var3, TreeFeatureConfig var4, int var5, TreeNode var6, int var7, int var8, int var9);

    public abstract int getRandomHeight(Random var1, int var2, TreeFeatureConfig var3);

    public int getRandomRadius(Random random, int baseHeight) {
        return this.radius.get(random);
    }

    private int getRandomOffset(Random random) {
        return this.offset.get(random);
    }

    protected abstract boolean isInvalidForLeaves(Random var1, int var2, int var3, int var4, int var5, boolean var6);

    protected boolean isPositionInvalid(Random random, int dx, int y, int dz, int radius, boolean giantTrunk) {
        int n;
        int m;
        if (giantTrunk) {
            m = Math.min(Math.abs(dx), Math.abs(dx - 1));
            n = Math.min(Math.abs(dz), Math.abs(dz - 1));
        } else {
            m = Math.abs(dx);
            n = Math.abs(dz);
        }
        return this.isInvalidForLeaves(random, m, y, n, radius, giantTrunk);
    }

    protected void generateSquare(TestableWorld world, BlockPlacer placer, Random random, TreeFeatureConfig config, BlockPos centerPos, int radius, int y, boolean giantTrunk) {
        int k = giantTrunk ? 1 : 0;
        BlockPos.Mutable lv = new BlockPos.Mutable();
        for (int l = -radius; l <= radius + k; ++l) {
            for (int m = -radius; m <= radius + k; ++m) {
                if (this.isPositionInvalid(random, l, y, m, radius, giantTrunk)) continue;
                lv.set(centerPos, l, y, m);
                FoliagePlacer.placeFoliageBlock(world, placer, random, config, lv);
            }
        }
    }

    protected final void generateSquareWithHangingLeaves(TestableWorld world, BlockPlacer placer, Random random, TreeFeatureConfig config, BlockPos centerPos, int radius, int y, boolean giantTrunk, float hangingLeavesChance, float hangingLeavesExtensionChance) {
        this.generateSquare(world, placer, random, config, centerPos, radius, y, giantTrunk);
        int k = giantTrunk ? 1 : 0;
        BlockPos lv = centerPos.down();
        BlockPos.Mutable lv2 = new BlockPos.Mutable();
        for (Direction lv3 : Direction.Type.HORIZONTAL) {
            Direction lv4 = lv3.rotateYClockwise();
            int l = lv4.getDirection() == Direction.AxisDirection.POSITIVE ? radius + k : radius;
            lv2.set(centerPos, 0, y - 1, 0).move(lv4, l).move(lv3, -radius);
            for (int m = -radius; m < radius + k; ++m) {
                boolean bl2 = placer.hasPlacedBlock(lv2.move(Direction.UP));
                lv2.move(Direction.DOWN);
                if (bl2 && FoliagePlacer.placeFoliageBlock(world, placer, random, config, hangingLeavesChance, lv, lv2)) {
                    lv2.move(Direction.DOWN);
                    FoliagePlacer.placeFoliageBlock(world, placer, random, config, hangingLeavesExtensionChance, lv, lv2);
                    lv2.move(Direction.UP);
                }
                lv2.move(lv3);
            }
        }
    }

    private static boolean placeFoliageBlock(TestableWorld world, BlockPlacer placer, Random random, TreeFeatureConfig config, float chance, BlockPos origin, BlockPos.Mutable pos) {
        if (pos.getManhattanDistance(origin) >= 7) {
            return false;
        }
        if (random.nextFloat() > chance) {
            return false;
        }
        return FoliagePlacer.placeFoliageBlock(world, placer, random, config, pos);
    }

    protected static boolean placeFoliageBlock(TestableWorld world, BlockPlacer placer, Random random, TreeFeatureConfig config, BlockPos pos) {
        if (!TreeFeature.canReplace(world, pos)) {
            return false;
        }
        BlockState lv = config.foliageProvider.get(random, pos);
        if (lv.contains(Properties.WATERLOGGED)) {
            lv = (BlockState)lv.with(Properties.WATERLOGGED, world.testFluidState(pos, fluidState -> fluidState.isEqualAndStill(Fluids.WATER)));
        }
        placer.placeBlock(pos, lv);
        return true;
    }

    public static interface BlockPlacer {
        public void placeBlock(BlockPos var1, BlockState var2);

        public boolean hasPlacedBlock(BlockPos var1);
    }

    public static final class TreeNode {
        private final BlockPos center;
        private final int foliageRadius;
        private final boolean giantTrunk;

        public TreeNode(BlockPos center, int foliageRadius, boolean giantTrunk) {
            this.center = center;
            this.foliageRadius = foliageRadius;
            this.giantTrunk = giantTrunk;
        }

        public BlockPos getCenter() {
            return this.center;
        }

        public int getFoliageRadius() {
            return this.foliageRadius;
        }

        public boolean isGiantTrunk() {
            return this.giantTrunk;
        }
    }
}

