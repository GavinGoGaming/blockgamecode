/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.world.gen.root;

import com.google.common.collect.Lists;
import com.mojang.datafixers.kinds.Applicative;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.BiConsumer;
import net.minecraft.block.BlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.intprovider.IntProvider;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.TestableWorld;
import net.minecraft.world.gen.feature.TreeFeatureConfig;
import net.minecraft.world.gen.root.AboveRootPlacement;
import net.minecraft.world.gen.root.MangroveRootPlacement;
import net.minecraft.world.gen.root.RootPlacer;
import net.minecraft.world.gen.root.RootPlacerType;
import net.minecraft.world.gen.stateprovider.BlockStateProvider;

public class MangroveRootPlacer
extends RootPlacer {
    public static final int field_38769 = 8;
    public static final int field_38770 = 15;
    public static final MapCodec<MangroveRootPlacer> CODEC = RecordCodecBuilder.mapCodec(instance -> MangroveRootPlacer.method_43182(instance).and(((MapCodec)MangroveRootPlacement.CODEC.fieldOf("mangrove_root_placement")).forGetter(rootPlacer -> rootPlacer.mangroveRootPlacement)).apply((Applicative<MangroveRootPlacer, ?>)instance, MangroveRootPlacer::new));
    private final MangroveRootPlacement mangroveRootPlacement;

    public MangroveRootPlacer(IntProvider trunkOffsetY, BlockStateProvider rootProvider, Optional<AboveRootPlacement> aboveRootPlacement, MangroveRootPlacement mangroveRootPlacement) {
        super(trunkOffsetY, rootProvider, aboveRootPlacement);
        this.mangroveRootPlacement = mangroveRootPlacement;
    }

    @Override
    public boolean generate(TestableWorld world, BiConsumer<BlockPos, BlockState> replacer, Random random, BlockPos pos, BlockPos trunkPos, TreeFeatureConfig config) {
        ArrayList<BlockPos> list = Lists.newArrayList();
        BlockPos.Mutable lv = pos.mutableCopy();
        while (lv.getY() < trunkPos.getY()) {
            if (!this.canGrowThrough(world, lv)) {
                return false;
            }
            lv.move(Direction.UP);
        }
        list.add(trunkPos.down());
        for (Direction lv2 : Direction.Type.HORIZONTAL) {
            ArrayList<BlockPos> list2;
            BlockPos lv3 = trunkPos.offset(lv2);
            if (!this.canGrow(world, random, lv3, lv2, trunkPos, list2 = Lists.newArrayList(), 0)) {
                return false;
            }
            list.addAll(list2);
            list.add(trunkPos.offset(lv2));
        }
        for (BlockPos lv4 : list) {
            this.placeRoots(world, replacer, random, lv4, config);
        }
        return true;
    }

    private boolean canGrow(TestableWorld world, Random random, BlockPos pos, Direction direction, BlockPos origin, List<BlockPos> offshootPositions, int rootLength) {
        int j = this.mangroveRootPlacement.maxRootLength();
        if (rootLength == j || offshootPositions.size() > j) {
            return false;
        }
        List<BlockPos> list2 = this.getOffshootPositions(pos, direction, random, origin);
        for (BlockPos lv : list2) {
            if (!this.canGrowThrough(world, lv)) continue;
            offshootPositions.add(lv);
            if (this.canGrow(world, random, lv, direction, origin, offshootPositions, rootLength + 1)) continue;
            return false;
        }
        return true;
    }

    protected List<BlockPos> getOffshootPositions(BlockPos pos, Direction direction, Random random, BlockPos origin) {
        BlockPos lv = pos.down();
        BlockPos lv2 = pos.offset(direction);
        int i = pos.getManhattanDistance(origin);
        int j = this.mangroveRootPlacement.maxRootWidth();
        float f = this.mangroveRootPlacement.randomSkewChance();
        if (i > j - 3 && i <= j) {
            return random.nextFloat() < f ? List.of(lv, lv2.down()) : List.of(lv);
        }
        if (i > j) {
            return List.of(lv);
        }
        if (random.nextFloat() < f) {
            return List.of(lv);
        }
        return random.nextBoolean() ? List.of(lv2) : List.of(lv);
    }

    @Override
    protected boolean canGrowThrough(TestableWorld world, BlockPos pos) {
        return super.canGrowThrough(world, pos) || world.testBlockState(pos, state -> state.isIn(this.mangroveRootPlacement.canGrowThrough()));
    }

    @Override
    protected void placeRoots(TestableWorld world, BiConsumer<BlockPos, BlockState> replacer, Random random, BlockPos pos, TreeFeatureConfig config) {
        if (world.testBlockState(pos, state -> state.isIn(this.mangroveRootPlacement.muddyRootsIn()))) {
            BlockState lv = this.mangroveRootPlacement.muddyRootsProvider().get(random, pos);
            replacer.accept(pos, this.applyWaterlogging(world, pos, lv));
        } else {
            super.placeRoots(world, replacer, random, pos, config);
        }
    }

    @Override
    protected RootPlacerType<?> getType() {
        return RootPlacerType.MANGROVE_ROOT_PLACER;
    }
}

