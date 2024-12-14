/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.world.gen.feature;

import com.mojang.serialization.Codec;
import java.util.HashSet;
import java.util.Set;
import java.util.function.Predicate;
import net.minecraft.block.AbstractBlock;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3i;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.StructureWorldAccess;
import net.minecraft.world.gen.chunk.ChunkGenerator;
import net.minecraft.world.gen.feature.Feature;
import net.minecraft.world.gen.feature.VegetationPatchFeatureConfig;
import net.minecraft.world.gen.feature.util.FeatureContext;

public class VegetationPatchFeature
extends Feature<VegetationPatchFeatureConfig> {
    public VegetationPatchFeature(Codec<VegetationPatchFeatureConfig> codec) {
        super(codec);
    }

    @Override
    public boolean generate(FeatureContext<VegetationPatchFeatureConfig> context) {
        StructureWorldAccess lv = context.getWorld();
        VegetationPatchFeatureConfig lv2 = context.getConfig();
        Random lv3 = context.getRandom();
        BlockPos lv4 = context.getOrigin();
        Predicate<BlockState> predicate = state -> state.isIn(arg.replaceable);
        int i = lv2.horizontalRadius.get(lv3) + 1;
        int j = lv2.horizontalRadius.get(lv3) + 1;
        Set<BlockPos> set = this.placeGroundAndGetPositions(lv, lv2, lv3, lv4, predicate, i, j);
        this.generateVegetation(context, lv, lv2, lv3, set, i, j);
        return !set.isEmpty();
    }

    protected Set<BlockPos> placeGroundAndGetPositions(StructureWorldAccess world, VegetationPatchFeatureConfig config, Random random, BlockPos pos, Predicate<BlockState> replaceable, int radiusX, int radiusZ) {
        BlockPos.Mutable lv = pos.mutableCopy();
        BlockPos.Mutable lv2 = lv.mutableCopy();
        Direction lv3 = config.surface.getDirection();
        Direction lv4 = lv3.getOpposite();
        HashSet<BlockPos> set = new HashSet<BlockPos>();
        for (int k = -radiusX; k <= radiusX; ++k) {
            boolean bl = k == -radiusX || k == radiusX;
            for (int l = -radiusZ; l <= radiusZ; ++l) {
                int m;
                boolean bl5;
                boolean bl2 = l == -radiusZ || l == radiusZ;
                boolean bl3 = bl || bl2;
                boolean bl4 = bl && bl2;
                boolean bl6 = bl5 = bl3 && !bl4;
                if (bl4 || bl5 && (config.extraEdgeColumnChance == 0.0f || random.nextFloat() > config.extraEdgeColumnChance)) continue;
                lv.set(pos, k, 0, l);
                for (m = 0; world.testBlockState(lv, AbstractBlock.AbstractBlockState::isAir) && m < config.verticalRange; ++m) {
                    lv.move(lv3);
                }
                for (m = 0; world.testBlockState(lv, state -> !state.isAir()) && m < config.verticalRange; ++m) {
                    lv.move(lv4);
                }
                lv2.set((Vec3i)lv, config.surface.getDirection());
                BlockState lv5 = world.getBlockState(lv2);
                if (!world.isAir(lv) || !lv5.isSideSolidFullSquare(world, lv2, config.surface.getDirection().getOpposite())) continue;
                int n = config.depth.get(random) + (config.extraBottomBlockChance > 0.0f && random.nextFloat() < config.extraBottomBlockChance ? 1 : 0);
                BlockPos lv6 = lv2.toImmutable();
                boolean bl62 = this.placeGround(world, config, replaceable, random, lv2, n);
                if (!bl62) continue;
                set.add(lv6);
            }
        }
        return set;
    }

    protected void generateVegetation(FeatureContext<VegetationPatchFeatureConfig> context, StructureWorldAccess world, VegetationPatchFeatureConfig config, Random random, Set<BlockPos> positions, int radiusX, int radiusZ) {
        for (BlockPos lv : positions) {
            if (!(config.vegetationChance > 0.0f) || !(random.nextFloat() < config.vegetationChance)) continue;
            this.generateVegetationFeature(world, config, context.getGenerator(), random, lv);
        }
    }

    protected boolean generateVegetationFeature(StructureWorldAccess world, VegetationPatchFeatureConfig config, ChunkGenerator generator, Random random, BlockPos pos) {
        return config.vegetationFeature.value().generateUnregistered(world, generator, random, pos.offset(config.surface.getDirection().getOpposite()));
    }

    protected boolean placeGround(StructureWorldAccess world, VegetationPatchFeatureConfig config, Predicate<BlockState> replaceable, Random random, BlockPos.Mutable pos, int depth) {
        for (int j = 0; j < depth; ++j) {
            BlockState lv2;
            BlockState lv = config.groundState.get(random, pos);
            if (lv.isOf((lv2 = world.getBlockState(pos)).getBlock())) continue;
            if (!replaceable.test(lv2)) {
                return j != 0;
            }
            world.setBlockState(pos, lv, Block.NOTIFY_LISTENERS);
            pos.move(config.surface.getDirection());
        }
        return true;
    }
}

