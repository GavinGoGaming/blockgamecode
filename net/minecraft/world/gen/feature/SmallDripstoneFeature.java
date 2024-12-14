/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.world.gen.feature;

import com.mojang.serialization.Codec;
import java.util.Optional;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.StructureWorldAccess;
import net.minecraft.world.WorldAccess;
import net.minecraft.world.gen.feature.Feature;
import net.minecraft.world.gen.feature.SmallDripstoneFeatureConfig;
import net.minecraft.world.gen.feature.util.DripstoneHelper;
import net.minecraft.world.gen.feature.util.FeatureContext;

public class SmallDripstoneFeature
extends Feature<SmallDripstoneFeatureConfig> {
    public SmallDripstoneFeature(Codec<SmallDripstoneFeatureConfig> codec) {
        super(codec);
    }

    @Override
    public boolean generate(FeatureContext<SmallDripstoneFeatureConfig> context) {
        StructureWorldAccess lv = context.getWorld();
        BlockPos lv2 = context.getOrigin();
        Random lv3 = context.getRandom();
        SmallDripstoneFeatureConfig lv4 = context.getConfig();
        Optional<Direction> optional = SmallDripstoneFeature.getDirection(lv, lv2, lv3);
        if (optional.isEmpty()) {
            return false;
        }
        BlockPos lv5 = lv2.offset(optional.get().getOpposite());
        SmallDripstoneFeature.generateDripstoneBlocks(lv, lv3, lv5, lv4);
        int i = lv3.nextFloat() < lv4.chanceOfTallerDripstone && DripstoneHelper.canGenerate(lv.getBlockState(lv2.offset(optional.get()))) ? 2 : 1;
        DripstoneHelper.generatePointedDripstone(lv, lv2, optional.get(), i, false);
        return true;
    }

    private static Optional<Direction> getDirection(WorldAccess world, BlockPos pos, Random random) {
        boolean bl = DripstoneHelper.canReplace(world.getBlockState(pos.up()));
        boolean bl2 = DripstoneHelper.canReplace(world.getBlockState(pos.down()));
        if (bl && bl2) {
            return Optional.of(random.nextBoolean() ? Direction.DOWN : Direction.UP);
        }
        if (bl) {
            return Optional.of(Direction.DOWN);
        }
        if (bl2) {
            return Optional.of(Direction.UP);
        }
        return Optional.empty();
    }

    private static void generateDripstoneBlocks(WorldAccess world, Random random, BlockPos pos, SmallDripstoneFeatureConfig config) {
        DripstoneHelper.generateDripstoneBlock(world, pos);
        for (Direction lv : Direction.Type.HORIZONTAL) {
            if (random.nextFloat() > config.chanceOfDirectionalSpread) continue;
            BlockPos lv2 = pos.offset(lv);
            DripstoneHelper.generateDripstoneBlock(world, lv2);
            if (random.nextFloat() > config.chanceOfSpreadRadius2) continue;
            BlockPos lv3 = lv2.offset(Direction.random(random));
            DripstoneHelper.generateDripstoneBlock(world, lv3);
            if (random.nextFloat() > config.chanceOfSpreadRadius3) continue;
            BlockPos lv4 = lv3.offset(Direction.random(random));
            DripstoneHelper.generateDripstoneBlock(world, lv4);
        }
    }
}

