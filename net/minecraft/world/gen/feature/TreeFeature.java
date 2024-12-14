/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.world.gen.feature;

import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.mojang.serialization.Codec;
import java.util.HashSet;
import java.util.List;
import java.util.OptionalInt;
import java.util.Set;
import java.util.function.BiConsumer;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.LeavesBlock;
import net.minecraft.registry.tag.BlockTags;
import net.minecraft.state.property.Properties;
import net.minecraft.structure.StructureTemplate;
import net.minecraft.util.math.BlockBox;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3i;
import net.minecraft.util.math.random.Random;
import net.minecraft.util.shape.BitSetVoxelSet;
import net.minecraft.util.shape.VoxelSet;
import net.minecraft.world.ModifiableWorld;
import net.minecraft.world.StructureWorldAccess;
import net.minecraft.world.TestableWorld;
import net.minecraft.world.WorldAccess;
import net.minecraft.world.gen.feature.Feature;
import net.minecraft.world.gen.feature.TreeFeatureConfig;
import net.minecraft.world.gen.feature.util.FeatureContext;
import net.minecraft.world.gen.foliage.FoliagePlacer;
import net.minecraft.world.gen.treedecorator.TreeDecorator;

public class TreeFeature
extends Feature<TreeFeatureConfig> {
    private static final int FORCE_STATE_AND_NOTIFY_ALL = 19;

    public TreeFeature(Codec<TreeFeatureConfig> codec) {
        super(codec);
    }

    private static boolean isVine(TestableWorld world, BlockPos pos) {
        return world.testBlockState(pos, state -> state.isOf(Blocks.VINE));
    }

    public static boolean isAirOrLeaves(TestableWorld world, BlockPos pos) {
        return world.testBlockState(pos, state -> state.isAir() || state.isIn(BlockTags.LEAVES));
    }

    private static void setBlockStateWithoutUpdatingNeighbors(ModifiableWorld world, BlockPos pos, BlockState state) {
        world.setBlockState(pos, state, Block.NOTIFY_ALL | Block.FORCE_STATE);
    }

    public static boolean canReplace(TestableWorld world, BlockPos pos) {
        return world.testBlockState(pos, state -> state.isAir() || state.isIn(BlockTags.REPLACEABLE_BY_TREES));
    }

    private boolean generate(StructureWorldAccess world, Random random, BlockPos pos, BiConsumer<BlockPos, BlockState> rootPlacerReplacer, BiConsumer<BlockPos, BlockState> trunkPlacerReplacer, FoliagePlacer.BlockPlacer blockPlacer, TreeFeatureConfig config) {
        int i = config.trunkPlacer.getHeight(random);
        int j = config.foliagePlacer.getRandomHeight(random, i, config);
        int k = i - j;
        int l = config.foliagePlacer.getRandomRadius(random, k);
        BlockPos lv = config.rootPlacer.map(rootPlacer -> rootPlacer.trunkOffset(pos, random)).orElse(pos);
        int m = Math.min(pos.getY(), lv.getY());
        int n = Math.max(pos.getY(), lv.getY()) + i + 1;
        if (m < world.getBottomY() + 1 || n > world.getTopY()) {
            return false;
        }
        OptionalInt optionalInt = config.minimumSize.getMinClippedHeight();
        int o = this.getTopPosition(world, i, lv, config);
        if (o < i && (optionalInt.isEmpty() || o < optionalInt.getAsInt())) {
            return false;
        }
        if (config.rootPlacer.isPresent() && !config.rootPlacer.get().generate(world, rootPlacerReplacer, random, pos, lv, config)) {
            return false;
        }
        List<FoliagePlacer.TreeNode> list = config.trunkPlacer.generate(world, trunkPlacerReplacer, random, o, lv, config);
        list.forEach(node -> arg.foliagePlacer.generate(world, blockPlacer, random, config, o, (FoliagePlacer.TreeNode)node, j, l));
        return true;
    }

    private int getTopPosition(TestableWorld world, int height, BlockPos pos, TreeFeatureConfig config) {
        BlockPos.Mutable lv = new BlockPos.Mutable();
        for (int j = 0; j <= height + 1; ++j) {
            int k = config.minimumSize.getRadius(height, j);
            for (int l = -k; l <= k; ++l) {
                for (int m = -k; m <= k; ++m) {
                    lv.set(pos, l, j, m);
                    if (config.trunkPlacer.canReplaceOrIsLog(world, lv) && (config.ignoreVines || !TreeFeature.isVine(world, lv))) continue;
                    return j - 2;
                }
            }
        }
        return height;
    }

    @Override
    protected void setBlockState(ModifiableWorld world, BlockPos pos, BlockState state) {
        TreeFeature.setBlockStateWithoutUpdatingNeighbors(world, pos, state);
    }

    @Override
    public final boolean generate(FeatureContext<TreeFeatureConfig> context) {
        final StructureWorldAccess lv = context.getWorld();
        Random lv2 = context.getRandom();
        BlockPos lv3 = context.getOrigin();
        TreeFeatureConfig lv4 = context.getConfig();
        HashSet<BlockPos> set = Sets.newHashSet();
        HashSet<BlockPos> set2 = Sets.newHashSet();
        final HashSet<BlockPos> set3 = Sets.newHashSet();
        HashSet set4 = Sets.newHashSet();
        BiConsumer<BlockPos, BlockState> biConsumer = (pos, state) -> {
            set.add(pos.toImmutable());
            lv.setBlockState((BlockPos)pos, (BlockState)state, Block.NOTIFY_ALL | Block.FORCE_STATE);
        };
        BiConsumer<BlockPos, BlockState> biConsumer2 = (pos, state) -> {
            set2.add(pos.toImmutable());
            lv.setBlockState((BlockPos)pos, (BlockState)state, Block.NOTIFY_ALL | Block.FORCE_STATE);
        };
        FoliagePlacer.BlockPlacer lv5 = new FoliagePlacer.BlockPlacer(){

            @Override
            public void placeBlock(BlockPos pos, BlockState state) {
                set3.add(pos.toImmutable());
                lv.setBlockState(pos, state, Block.NOTIFY_ALL | Block.FORCE_STATE);
            }

            @Override
            public boolean hasPlacedBlock(BlockPos pos) {
                return set3.contains(pos);
            }
        };
        BiConsumer<BlockPos, BlockState> biConsumer3 = (pos, state) -> {
            set4.add(pos.toImmutable());
            lv.setBlockState((BlockPos)pos, (BlockState)state, Block.NOTIFY_ALL | Block.FORCE_STATE);
        };
        boolean bl = this.generate(lv, lv2, lv3, biConsumer, biConsumer2, lv5, lv4);
        if (!bl || set2.isEmpty() && set3.isEmpty()) {
            return false;
        }
        if (!lv4.decorators.isEmpty()) {
            TreeDecorator.Generator lv6 = new TreeDecorator.Generator(lv, biConsumer3, lv2, set2, set3, set);
            lv4.decorators.forEach(decorator -> decorator.generate(lv6));
        }
        return BlockBox.encompassPositions(Iterables.concat(set, set2, set3, set4)).map(box -> {
            VoxelSet lv = TreeFeature.placeLogsAndLeaves(lv, box, set2, set4, set);
            StructureTemplate.updateCorner(lv, 3, lv, box.getMinX(), box.getMinY(), box.getMinZ());
            return true;
        }).orElse(false);
    }

    /*
     * Unable to fully structure code
     */
    private static VoxelSet placeLogsAndLeaves(WorldAccess world, BlockBox box, Set<BlockPos> trunkPositions, Set<BlockPos> decorationPositions, Set<BlockPos> rootPositions) {
        lv = new BitSetVoxelSet(box.getBlockCountX(), box.getBlockCountY(), box.getBlockCountZ());
        i = 7;
        list = Lists.newArrayList();
        for (j = 0; j < 7; ++j) {
            list.add(Sets.newHashSet());
        }
        for (BlockPos lv2 : Lists.newArrayList(Sets.union(decorationPositions, rootPositions))) {
            if (!box.contains(lv2)) continue;
            lv.set(lv2.getX() - box.getMinX(), lv2.getY() - box.getMinY(), lv2.getZ() - box.getMinZ());
        }
        lv3 = new BlockPos.Mutable();
        k = 0;
        ((Set)list.get(0)).addAll(trunkPositions);
        block2: while (true) {
            if (k < 7 && ((Set)list.get(k)).isEmpty()) {
                ++k;
                continue;
            }
            if (k >= 7) break;
            iterator = ((Set)list.get(k)).iterator();
            lv4 = (BlockPos)iterator.next();
            iterator.remove();
            if (!box.contains(lv4)) continue;
            if (k != 0) {
                lv5 = world.getBlockState(lv4);
                TreeFeature.setBlockStateWithoutUpdatingNeighbors(world, lv4, (BlockState)lv5.with(Properties.DISTANCE_1_7, k));
            }
            lv.set(lv4.getX() - box.getMinX(), lv4.getY() - box.getMinY(), lv4.getZ() - box.getMinZ());
            var12_14 = Direction.values();
            var13_15 = var12_14.length;
            var14_16 = 0;
            while (true) {
                if (var14_16 < var13_15) ** break;
                continue block2;
                lv6 = var12_14[var14_16];
                lv3.set((Vec3i)lv4, lv6);
                if (box.contains(lv3) && !lv.contains(l = lv3.getX() - box.getMinX(), m = lv3.getY() - box.getMinY(), n = lv3.getZ() - box.getMinZ()) && !(optionalInt = LeavesBlock.getOptionalDistanceFromLog(lv7 = world.getBlockState(lv3))).isEmpty() && (o = Math.min(optionalInt.getAsInt(), k + 1)) < 7) {
                    ((Set)list.get(o)).add(lv3.toImmutable());
                    k = Math.min(k, o);
                }
                ++var14_16;
            }
            break;
        }
        return lv;
    }
}

