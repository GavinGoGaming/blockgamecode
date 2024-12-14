/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.world.gen.feature;

import com.mojang.serialization.Codec;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.structure.StructurePlacementData;
import net.minecraft.structure.StructureTemplate;
import net.minecraft.structure.StructureTemplateManager;
import net.minecraft.util.BlockMirror;
import net.minecraft.util.BlockRotation;
import net.minecraft.util.math.BlockBox;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.Vec3i;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.Heightmap;
import net.minecraft.world.StructureWorldAccess;
import net.minecraft.world.gen.feature.Feature;
import net.minecraft.world.gen.feature.FossilFeatureConfig;
import net.minecraft.world.gen.feature.util.FeatureContext;
import org.apache.commons.lang3.mutable.MutableInt;

public class FossilFeature
extends Feature<FossilFeatureConfig> {
    public FossilFeature(Codec<FossilFeatureConfig> codec) {
        super(codec);
    }

    @Override
    public boolean generate(FeatureContext<FossilFeatureConfig> context) {
        int k;
        Random lv = context.getRandom();
        StructureWorldAccess lv2 = context.getWorld();
        BlockPos lv3 = context.getOrigin();
        BlockRotation lv4 = BlockRotation.random(lv);
        FossilFeatureConfig lv5 = context.getConfig();
        int i = lv.nextInt(lv5.fossilStructures.size());
        StructureTemplateManager lv6 = lv2.toServerWorld().getServer().getStructureTemplateManager();
        StructureTemplate lv7 = lv6.getTemplateOrBlank(lv5.fossilStructures.get(i));
        StructureTemplate lv8 = lv6.getTemplateOrBlank(lv5.overlayStructures.get(i));
        ChunkPos lv9 = new ChunkPos(lv3);
        BlockBox lv10 = new BlockBox(lv9.getStartX() - 16, lv2.getBottomY(), lv9.getStartZ() - 16, lv9.getEndX() + 16, lv2.getTopY(), lv9.getEndZ() + 16);
        StructurePlacementData lv11 = new StructurePlacementData().setRotation(lv4).setBoundingBox(lv10).setRandom(lv);
        Vec3i lv12 = lv7.getRotatedSize(lv4);
        BlockPos lv13 = lv3.add(-lv12.getX() / 2, 0, -lv12.getZ() / 2);
        int j = lv3.getY();
        for (k = 0; k < lv12.getX(); ++k) {
            for (int l = 0; l < lv12.getZ(); ++l) {
                j = Math.min(j, lv2.getTopY(Heightmap.Type.OCEAN_FLOOR_WG, lv13.getX() + k, lv13.getZ() + l));
            }
        }
        k = Math.max(j - 15 - lv.nextInt(10), lv2.getBottomY() + 10);
        BlockPos lv14 = lv7.offsetByTransformedSize(lv13.withY(k), BlockMirror.NONE, lv4);
        if (FossilFeature.getEmptyCorners(lv2, lv7.calculateBoundingBox(lv11, lv14)) > lv5.maxEmptyCorners) {
            return false;
        }
        lv11.clearProcessors();
        lv5.fossilProcessors.value().getList().forEach(lv11::addProcessor);
        lv7.place(lv2, lv14, lv14, lv11, lv, 4);
        lv11.clearProcessors();
        lv5.overlayProcessors.value().getList().forEach(lv11::addProcessor);
        lv8.place(lv2, lv14, lv14, lv11, lv, 4);
        return true;
    }

    private static int getEmptyCorners(StructureWorldAccess world, BlockBox box) {
        MutableInt mutableInt = new MutableInt(0);
        box.forEachVertex(pos -> {
            BlockState lv = world.getBlockState((BlockPos)pos);
            if (lv.isAir() || lv.isOf(Blocks.LAVA) || lv.isOf(Blocks.WATER)) {
                mutableInt.add(1);
            }
        });
        return mutableInt.getValue();
    }
}

