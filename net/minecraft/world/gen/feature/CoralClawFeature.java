/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.world.gen.feature;

import com.mojang.serialization.Codec;
import java.util.List;
import java.util.stream.Stream;
import net.minecraft.block.BlockState;
import net.minecraft.util.Util;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.WorldAccess;
import net.minecraft.world.gen.feature.CoralFeature;
import net.minecraft.world.gen.feature.DefaultFeatureConfig;

public class CoralClawFeature
extends CoralFeature {
    public CoralClawFeature(Codec<DefaultFeatureConfig> codec) {
        super(codec);
    }

    @Override
    protected boolean generateCoral(WorldAccess world, Random random, BlockPos pos, BlockState state) {
        if (!this.generateCoralPiece(world, random, pos, state)) {
            return false;
        }
        Direction lv = Direction.Type.HORIZONTAL.random(random);
        int i = random.nextInt(2) + 2;
        List<Direction> list = Util.copyShuffled(Stream.of(lv, lv.rotateYClockwise(), lv.rotateYCounterclockwise()), random);
        List<Direction> list2 = list.subList(0, i);
        block0: for (Direction lv2 : list2) {
            int l;
            int k;
            Direction lv4;
            BlockPos.Mutable lv3 = pos.mutableCopy();
            int j = random.nextInt(2) + 1;
            lv3.move(lv2);
            if (lv2 == lv) {
                lv4 = lv;
                k = random.nextInt(3) + 2;
            } else {
                lv3.move(Direction.UP);
                Direction[] lvs = new Direction[]{lv2, Direction.UP};
                lv4 = Util.getRandom(lvs, random);
                k = random.nextInt(3) + 3;
            }
            for (l = 0; l < j && this.generateCoralPiece(world, random, lv3, state); ++l) {
                lv3.move(lv4);
            }
            lv3.move(lv4.getOpposite());
            lv3.move(Direction.UP);
            for (l = 0; l < k; ++l) {
                lv3.move(lv);
                if (!this.generateCoralPiece(world, random, lv3, state)) continue block0;
                if (!(random.nextFloat() < 0.25f)) continue;
                lv3.move(Direction.UP);
            }
        }
        return true;
    }
}

