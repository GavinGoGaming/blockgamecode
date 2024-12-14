/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 * 
 * Could not load the following classes:
 *  org.jetbrains.annotations.Nullable
 */
package net.minecraft.structure.processor;

import com.mojang.serialization.MapCodec;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.structure.StructurePlacementData;
import net.minecraft.structure.StructureTemplate;
import net.minecraft.structure.processor.StructureProcessor;
import net.minecraft.structure.processor.StructureProcessorType;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.WorldView;
import org.jetbrains.annotations.Nullable;

public class LavaSubmergedBlockStructureProcessor
extends StructureProcessor {
    public static final MapCodec<LavaSubmergedBlockStructureProcessor> CODEC = MapCodec.unit(() -> INSTANCE);
    public static final LavaSubmergedBlockStructureProcessor INSTANCE = new LavaSubmergedBlockStructureProcessor();

    @Override
    @Nullable
    public StructureTemplate.StructureBlockInfo process(WorldView world, BlockPos pos, BlockPos pivot, StructureTemplate.StructureBlockInfo originalBlockInfo, StructureTemplate.StructureBlockInfo currentBlockInfo, StructurePlacementData data) {
        BlockPos lv = currentBlockInfo.pos();
        boolean bl = world.getBlockState(lv).isOf(Blocks.LAVA);
        if (bl && !Block.isShapeFullCube(currentBlockInfo.state().getOutlineShape(world, lv))) {
            return new StructureTemplate.StructureBlockInfo(lv, Blocks.LAVA.getDefaultState(), currentBlockInfo.nbt());
        }
        return currentBlockInfo;
    }

    @Override
    protected StructureProcessorType<?> getType() {
        return StructureProcessorType.LAVA_SUBMERGED_BLOCK;
    }
}

