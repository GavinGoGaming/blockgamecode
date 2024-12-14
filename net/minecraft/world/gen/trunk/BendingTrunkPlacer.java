/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.world.gen.trunk;

import com.google.common.collect.Lists;
import com.mojang.datafixers.kinds.Applicative;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.ArrayList;
import java.util.List;
import java.util.function.BiConsumer;
import net.minecraft.block.BlockState;
import net.minecraft.util.dynamic.Codecs;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3i;
import net.minecraft.util.math.intprovider.IntProvider;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.TestableWorld;
import net.minecraft.world.gen.feature.TreeFeature;
import net.minecraft.world.gen.feature.TreeFeatureConfig;
import net.minecraft.world.gen.foliage.FoliagePlacer;
import net.minecraft.world.gen.trunk.TrunkPlacer;
import net.minecraft.world.gen.trunk.TrunkPlacerType;

public class BendingTrunkPlacer
extends TrunkPlacer {
    public static final MapCodec<BendingTrunkPlacer> CODEC = RecordCodecBuilder.mapCodec(instance -> BendingTrunkPlacer.fillTrunkPlacerFields(instance).and(instance.group(Codecs.POSITIVE_INT.optionalFieldOf("min_height_for_leaves", 1).forGetter(placer -> placer.minHeightForLeaves), ((MapCodec)IntProvider.createValidatingCodec(1, 64).fieldOf("bend_length")).forGetter(placer -> placer.bendLength))).apply((Applicative<BendingTrunkPlacer, ?>)instance, BendingTrunkPlacer::new));
    private final int minHeightForLeaves;
    private final IntProvider bendLength;

    public BendingTrunkPlacer(int baseHeight, int firstRandomHeight, int secondRandomHeight, int minHeightForLeaves, IntProvider bendLength) {
        super(baseHeight, firstRandomHeight, secondRandomHeight);
        this.minHeightForLeaves = minHeightForLeaves;
        this.bendLength = bendLength;
    }

    @Override
    protected TrunkPlacerType<?> getType() {
        return TrunkPlacerType.BENDING_TRUNK_PLACER;
    }

    @Override
    public List<FoliagePlacer.TreeNode> generate(TestableWorld world, BiConsumer<BlockPos, BlockState> replacer, Random random, int height, BlockPos startPos, TreeFeatureConfig config) {
        int k;
        Direction lv = Direction.Type.HORIZONTAL.random(random);
        int j = height - 1;
        BlockPos.Mutable lv2 = startPos.mutableCopy();
        Vec3i lv3 = lv2.down();
        BendingTrunkPlacer.setToDirt(world, replacer, random, (BlockPos)lv3, config);
        ArrayList<FoliagePlacer.TreeNode> list = Lists.newArrayList();
        for (k = 0; k <= j; ++k) {
            if (k + 1 >= j + random.nextInt(2)) {
                lv2.move(lv);
            }
            if (TreeFeature.canReplace(world, lv2)) {
                this.getAndSetState(world, replacer, random, lv2, config);
            }
            if (k >= this.minHeightForLeaves) {
                list.add(new FoliagePlacer.TreeNode(lv2.toImmutable(), 0, false));
            }
            lv2.move(Direction.UP);
        }
        k = this.bendLength.get(random);
        for (int l = 0; l <= k; ++l) {
            if (TreeFeature.canReplace(world, lv2)) {
                this.getAndSetState(world, replacer, random, lv2, config);
            }
            list.add(new FoliagePlacer.TreeNode(lv2.toImmutable(), 0, false));
            lv2.move(lv);
        }
        return list;
    }
}

