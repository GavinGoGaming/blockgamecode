/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.world.gen.trunk;

import com.google.common.collect.ImmutableList;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.List;
import java.util.function.BiConsumer;
import net.minecraft.block.BlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.TestableWorld;
import net.minecraft.world.gen.feature.TreeFeatureConfig;
import net.minecraft.world.gen.foliage.FoliagePlacer;
import net.minecraft.world.gen.trunk.TrunkPlacer;
import net.minecraft.world.gen.trunk.TrunkPlacerType;

public class GiantTrunkPlacer
extends TrunkPlacer {
    public static final MapCodec<GiantTrunkPlacer> CODEC = RecordCodecBuilder.mapCodec(instance -> GiantTrunkPlacer.fillTrunkPlacerFields(instance).apply(instance, GiantTrunkPlacer::new));

    public GiantTrunkPlacer(int i, int j, int k) {
        super(i, j, k);
    }

    @Override
    protected TrunkPlacerType<?> getType() {
        return TrunkPlacerType.GIANT_TRUNK_PLACER;
    }

    @Override
    public List<FoliagePlacer.TreeNode> generate(TestableWorld world, BiConsumer<BlockPos, BlockState> replacer, Random random, int height, BlockPos startPos, TreeFeatureConfig config) {
        BlockPos lv = startPos.down();
        GiantTrunkPlacer.setToDirt(world, replacer, random, lv, config);
        GiantTrunkPlacer.setToDirt(world, replacer, random, lv.east(), config);
        GiantTrunkPlacer.setToDirt(world, replacer, random, lv.south(), config);
        GiantTrunkPlacer.setToDirt(world, replacer, random, lv.south().east(), config);
        BlockPos.Mutable lv2 = new BlockPos.Mutable();
        for (int j = 0; j < height; ++j) {
            this.setLog(world, replacer, random, lv2, config, startPos, 0, j, 0);
            if (j >= height - 1) continue;
            this.setLog(world, replacer, random, lv2, config, startPos, 1, j, 0);
            this.setLog(world, replacer, random, lv2, config, startPos, 1, j, 1);
            this.setLog(world, replacer, random, lv2, config, startPos, 0, j, 1);
        }
        return ImmutableList.of(new FoliagePlacer.TreeNode(startPos.up(height), 0, true));
    }

    private void setLog(TestableWorld world, BiConsumer<BlockPos, BlockState> replacer, Random random, BlockPos.Mutable tmpPos, TreeFeatureConfig config, BlockPos startPos, int dx, int dy, int dz) {
        tmpPos.set(startPos, dx, dy, dz);
        this.trySetState(world, replacer, random, tmpPos, config);
    }
}

