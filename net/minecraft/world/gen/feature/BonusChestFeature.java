/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.world.gen.feature;

import com.mojang.serialization.Codec;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import java.util.stream.IntStream;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.inventory.LootableInventory;
import net.minecraft.loot.LootTables;
import net.minecraft.util.Util;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.Heightmap;
import net.minecraft.world.StructureWorldAccess;
import net.minecraft.world.gen.feature.DefaultFeatureConfig;
import net.minecraft.world.gen.feature.Feature;
import net.minecraft.world.gen.feature.util.FeatureContext;

public class BonusChestFeature
extends Feature<DefaultFeatureConfig> {
    public BonusChestFeature(Codec<DefaultFeatureConfig> codec) {
        super(codec);
    }

    @Override
    public boolean generate(FeatureContext<DefaultFeatureConfig> context) {
        Random lv = context.getRandom();
        StructureWorldAccess lv2 = context.getWorld();
        ChunkPos lv3 = new ChunkPos(context.getOrigin());
        IntArrayList intArrayList = Util.shuffle(IntStream.rangeClosed(lv3.getStartX(), lv3.getEndX()), lv);
        IntArrayList intArrayList2 = Util.shuffle(IntStream.rangeClosed(lv3.getStartZ(), lv3.getEndZ()), lv);
        BlockPos.Mutable lv4 = new BlockPos.Mutable();
        for (Integer integer : intArrayList) {
            for (Integer integer2 : intArrayList2) {
                lv4.set(integer, 0, integer2);
                BlockPos lv5 = lv2.getTopPosition(Heightmap.Type.MOTION_BLOCKING_NO_LEAVES, lv4);
                if (!lv2.isAir(lv5) && !lv2.getBlockState(lv5).getCollisionShape(lv2, lv5).isEmpty()) continue;
                lv2.setBlockState(lv5, Blocks.CHEST.getDefaultState(), Block.NOTIFY_LISTENERS);
                LootableInventory.setLootTable(lv2, lv, lv5, LootTables.SPAWN_BONUS_CHEST);
                BlockState lv6 = Blocks.TORCH.getDefaultState();
                for (Direction lv7 : Direction.Type.HORIZONTAL) {
                    BlockPos lv8 = lv5.offset(lv7);
                    if (!lv6.canPlaceAt(lv2, lv8)) continue;
                    lv2.setBlockState(lv8, lv6, Block.NOTIFY_LISTENERS);
                }
                return true;
            }
        }
        return false;
    }
}

