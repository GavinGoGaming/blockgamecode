/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.world.gen.trunk;

import com.google.common.collect.Lists;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.ArrayList;
import java.util.List;
import java.util.function.BiConsumer;
import net.minecraft.block.BlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.TestableWorld;
import net.minecraft.world.gen.feature.TreeFeatureConfig;
import net.minecraft.world.gen.foliage.FoliagePlacer;
import net.minecraft.world.gen.trunk.GiantTrunkPlacer;
import net.minecraft.world.gen.trunk.TrunkPlacerType;

public class MegaJungleTrunkPlacer
extends GiantTrunkPlacer {
    public static final MapCodec<MegaJungleTrunkPlacer> CODEC = RecordCodecBuilder.mapCodec(instance -> MegaJungleTrunkPlacer.fillTrunkPlacerFields(instance).apply(instance, MegaJungleTrunkPlacer::new));

    public MegaJungleTrunkPlacer(int i, int j, int k) {
        super(i, j, k);
    }

    @Override
    protected TrunkPlacerType<?> getType() {
        return TrunkPlacerType.MEGA_JUNGLE_TRUNK_PLACER;
    }

    @Override
    public List<FoliagePlacer.TreeNode> generate(TestableWorld world, BiConsumer<BlockPos, BlockState> replacer, Random random, int height, BlockPos startPos, TreeFeatureConfig config) {
        ArrayList<FoliagePlacer.TreeNode> list = Lists.newArrayList();
        list.addAll(super.generate(world, replacer, random, height, startPos, config));
        for (int j = height - 2 - random.nextInt(4); j > height / 2; j -= 2 + random.nextInt(4)) {
            float f = random.nextFloat() * ((float)Math.PI * 2);
            int k = 0;
            int l = 0;
            for (int m = 0; m < 5; ++m) {
                k = (int)(1.5f + MathHelper.cos(f) * (float)m);
                l = (int)(1.5f + MathHelper.sin(f) * (float)m);
                BlockPos lv = startPos.add(k, j - 3 + m / 2, l);
                this.getAndSetState(world, replacer, random, lv, config);
            }
            list.add(new FoliagePlacer.TreeNode(startPos.add(k, j, l), -2, false));
        }
        return list;
    }
}

