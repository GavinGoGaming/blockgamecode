/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.world.gen.feature;

import com.mojang.serialization.Codec;
import java.util.List;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.loot.LootTables;
import net.minecraft.predicate.block.BlockStatePredicate;
import net.minecraft.util.Util;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.StructureWorldAccess;
import net.minecraft.world.gen.feature.DefaultFeatureConfig;
import net.minecraft.world.gen.feature.Feature;
import net.minecraft.world.gen.feature.util.FeatureContext;

public class DesertWellFeature
extends Feature<DefaultFeatureConfig> {
    private static final BlockStatePredicate CAN_GENERATE = BlockStatePredicate.forBlock(Blocks.SAND);
    private final BlockState sand = Blocks.SAND.getDefaultState();
    private final BlockState slab = Blocks.SANDSTONE_SLAB.getDefaultState();
    private final BlockState wall = Blocks.SANDSTONE.getDefaultState();
    private final BlockState fluidInside = Blocks.WATER.getDefaultState();

    public DesertWellFeature(Codec<DefaultFeatureConfig> codec) {
        super(codec);
    }

    @Override
    public boolean generate(FeatureContext<DefaultFeatureConfig> context) {
        int j;
        int j2;
        int i;
        StructureWorldAccess lv = context.getWorld();
        BlockPos lv2 = context.getOrigin();
        lv2 = lv2.up();
        while (lv.isAir(lv2) && lv2.getY() > lv.getBottomY() + 2) {
            lv2 = lv2.down();
        }
        if (!CAN_GENERATE.test(lv.getBlockState(lv2))) {
            return false;
        }
        for (i = -2; i <= 2; ++i) {
            for (j2 = -2; j2 <= 2; ++j2) {
                if (!lv.isAir(lv2.add(i, -1, j2)) || !lv.isAir(lv2.add(i, -2, j2))) continue;
                return false;
            }
        }
        for (i = -2; i <= 0; ++i) {
            for (j2 = -2; j2 <= 2; ++j2) {
                for (int k = -2; k <= 2; ++k) {
                    lv.setBlockState(lv2.add(j2, i, k), this.wall, Block.NOTIFY_LISTENERS);
                }
            }
        }
        lv.setBlockState(lv2, this.fluidInside, Block.NOTIFY_LISTENERS);
        for (Direction lv3 : Direction.Type.HORIZONTAL) {
            lv.setBlockState(lv2.offset(lv3), this.fluidInside, Block.NOTIFY_LISTENERS);
        }
        BlockPos lv4 = lv2.down();
        lv.setBlockState(lv4, this.sand, Block.NOTIFY_LISTENERS);
        for (Direction lv5 : Direction.Type.HORIZONTAL) {
            lv.setBlockState(lv4.offset(lv5), this.sand, Block.NOTIFY_LISTENERS);
        }
        for (j = -2; j <= 2; ++j) {
            for (int k = -2; k <= 2; ++k) {
                if (j != -2 && j != 2 && k != -2 && k != 2) continue;
                lv.setBlockState(lv2.add(j, 1, k), this.wall, Block.NOTIFY_LISTENERS);
            }
        }
        lv.setBlockState(lv2.add(2, 1, 0), this.slab, Block.NOTIFY_LISTENERS);
        lv.setBlockState(lv2.add(-2, 1, 0), this.slab, Block.NOTIFY_LISTENERS);
        lv.setBlockState(lv2.add(0, 1, 2), this.slab, Block.NOTIFY_LISTENERS);
        lv.setBlockState(lv2.add(0, 1, -2), this.slab, Block.NOTIFY_LISTENERS);
        for (j = -1; j <= 1; ++j) {
            for (int k = -1; k <= 1; ++k) {
                if (j == 0 && k == 0) {
                    lv.setBlockState(lv2.add(j, 4, k), this.wall, Block.NOTIFY_LISTENERS);
                    continue;
                }
                lv.setBlockState(lv2.add(j, 4, k), this.slab, Block.NOTIFY_LISTENERS);
            }
        }
        for (j = 1; j <= 3; ++j) {
            lv.setBlockState(lv2.add(-1, j, -1), this.wall, Block.NOTIFY_LISTENERS);
            lv.setBlockState(lv2.add(-1, j, 1), this.wall, Block.NOTIFY_LISTENERS);
            lv.setBlockState(lv2.add(1, j, -1), this.wall, Block.NOTIFY_LISTENERS);
            lv.setBlockState(lv2.add(1, j, 1), this.wall, Block.NOTIFY_LISTENERS);
        }
        BlockPos lv6 = lv2;
        List<BlockPos> list = List.of(lv6, lv6.east(), lv6.south(), lv6.west(), lv6.north());
        Random lv7 = context.getRandom();
        DesertWellFeature.generateSuspiciousSand(lv, Util.getRandom(list, lv7).down(1));
        DesertWellFeature.generateSuspiciousSand(lv, Util.getRandom(list, lv7).down(2));
        return true;
    }

    private static void generateSuspiciousSand(StructureWorldAccess world, BlockPos pos) {
        world.setBlockState(pos, Blocks.SUSPICIOUS_SAND.getDefaultState(), Block.NOTIFY_ALL);
        world.getBlockEntity(pos, BlockEntityType.BRUSHABLE_BLOCK).ifPresent(blockEntity -> blockEntity.setLootTable(LootTables.DESERT_WELL_ARCHAEOLOGY, pos.asLong()));
    }
}

