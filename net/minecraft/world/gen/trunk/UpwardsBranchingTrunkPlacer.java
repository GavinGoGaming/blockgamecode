/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.world.gen.trunk;

import com.google.common.collect.Lists;
import com.mojang.datafixers.kinds.Applicative;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.ArrayList;
import java.util.List;
import java.util.function.BiConsumer;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.registry.RegistryCodecs;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.entry.RegistryEntryList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.intprovider.IntProvider;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.TestableWorld;
import net.minecraft.world.gen.feature.TreeFeatureConfig;
import net.minecraft.world.gen.foliage.FoliagePlacer;
import net.minecraft.world.gen.trunk.TrunkPlacer;
import net.minecraft.world.gen.trunk.TrunkPlacerType;

public class UpwardsBranchingTrunkPlacer
extends TrunkPlacer {
    public static final MapCodec<UpwardsBranchingTrunkPlacer> CODEC = RecordCodecBuilder.mapCodec(instance -> UpwardsBranchingTrunkPlacer.fillTrunkPlacerFields(instance).and(instance.group(((MapCodec)IntProvider.POSITIVE_CODEC.fieldOf("extra_branch_steps")).forGetter(trunkPlacer -> trunkPlacer.extraBranchSteps), ((MapCodec)Codec.floatRange(0.0f, 1.0f).fieldOf("place_branch_per_log_probability")).forGetter(trunkPlacer -> Float.valueOf(trunkPlacer.placeBranchPerLogProbability)), ((MapCodec)IntProvider.NON_NEGATIVE_CODEC.fieldOf("extra_branch_length")).forGetter(trunkPlacer -> trunkPlacer.extraBranchLength), ((MapCodec)RegistryCodecs.entryList(RegistryKeys.BLOCK).fieldOf("can_grow_through")).forGetter(trunkPlacer -> trunkPlacer.canGrowThrough))).apply((Applicative<UpwardsBranchingTrunkPlacer, ?>)instance, UpwardsBranchingTrunkPlacer::new));
    private final IntProvider extraBranchSteps;
    private final float placeBranchPerLogProbability;
    private final IntProvider extraBranchLength;
    private final RegistryEntryList<Block> canGrowThrough;

    public UpwardsBranchingTrunkPlacer(int baseHeight, int firstRandomHeight, int secondRandomHeight, IntProvider extraBranchSteps, float placeBranchPerLogProbability, IntProvider extraBranchLength, RegistryEntryList<Block> canGrowThrough) {
        super(baseHeight, firstRandomHeight, secondRandomHeight);
        this.extraBranchSteps = extraBranchSteps;
        this.placeBranchPerLogProbability = placeBranchPerLogProbability;
        this.extraBranchLength = extraBranchLength;
        this.canGrowThrough = canGrowThrough;
    }

    @Override
    protected TrunkPlacerType<?> getType() {
        return TrunkPlacerType.UPWARDS_BRANCHING_TRUNK_PLACER;
    }

    @Override
    public List<FoliagePlacer.TreeNode> generate(TestableWorld world, BiConsumer<BlockPos, BlockState> replacer, Random random, int height, BlockPos startPos, TreeFeatureConfig config) {
        ArrayList<FoliagePlacer.TreeNode> list = Lists.newArrayList();
        BlockPos.Mutable lv = new BlockPos.Mutable();
        for (int j = 0; j < height; ++j) {
            int k = startPos.getY() + j;
            if (this.getAndSetState(world, replacer, random, lv.set(startPos.getX(), k, startPos.getZ()), config) && j < height - 1 && random.nextFloat() < this.placeBranchPerLogProbability) {
                Direction lv2 = Direction.Type.HORIZONTAL.random(random);
                int l = this.extraBranchLength.get(random);
                int m = Math.max(0, l - this.extraBranchLength.get(random) - 1);
                int n = this.extraBranchSteps.get(random);
                this.generateExtraBranch(world, replacer, random, height, config, list, lv, k, lv2, m, n);
            }
            if (j != height - 1) continue;
            list.add(new FoliagePlacer.TreeNode(lv.set(startPos.getX(), k + 1, startPos.getZ()), 0, false));
        }
        return list;
    }

    private void generateExtraBranch(TestableWorld world, BiConsumer<BlockPos, BlockState> replacer, Random random, int height, TreeFeatureConfig config, List<FoliagePlacer.TreeNode> nodes, BlockPos.Mutable pos, int yOffset, Direction direction, int length, int steps) {
        int m = yOffset + length;
        int n = pos.getX();
        int o = pos.getZ();
        for (int p = length; p < height && steps > 0; ++p, --steps) {
            if (p < 1) continue;
            int q = yOffset + p;
            m = q;
            if (this.getAndSetState(world, replacer, random, pos.set(n += direction.getOffsetX(), q, o += direction.getOffsetZ()), config)) {
                ++m;
            }
            nodes.add(new FoliagePlacer.TreeNode(pos.toImmutable(), 0, false));
        }
        if (m - yOffset > 1) {
            BlockPos lv = new BlockPos(n, m, o);
            nodes.add(new FoliagePlacer.TreeNode(lv, 0, false));
            nodes.add(new FoliagePlacer.TreeNode(lv.down(2), 0, false));
        }
    }

    @Override
    protected boolean canReplace(TestableWorld world, BlockPos pos) {
        return super.canReplace(world, pos) || world.testBlockState(pos, state -> state.isIn(this.canGrowThrough));
    }
}

