/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 * 
 * Could not load the following classes:
 *  org.jetbrains.annotations.Nullable
 */
package net.minecraft.structure.processor;

import com.google.common.collect.ImmutableList;
import com.mojang.serialization.MapCodec;
import java.util.List;
import net.minecraft.block.BlockState;
import net.minecraft.structure.StructurePlacementData;
import net.minecraft.structure.StructureTemplate;
import net.minecraft.structure.processor.StructureProcessor;
import net.minecraft.structure.processor.StructureProcessorRule;
import net.minecraft.structure.processor.StructureProcessorType;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.WorldView;
import org.jetbrains.annotations.Nullable;

public class RuleStructureProcessor
extends StructureProcessor {
    public static final MapCodec<RuleStructureProcessor> CODEC = ((MapCodec)StructureProcessorRule.CODEC.listOf().fieldOf("rules")).xmap(RuleStructureProcessor::new, processor -> processor.rules);
    private final ImmutableList<StructureProcessorRule> rules;

    public RuleStructureProcessor(List<? extends StructureProcessorRule> rules) {
        this.rules = ImmutableList.copyOf(rules);
    }

    @Override
    @Nullable
    public StructureTemplate.StructureBlockInfo process(WorldView world, BlockPos pos, BlockPos pivot, StructureTemplate.StructureBlockInfo originalBlockInfo, StructureTemplate.StructureBlockInfo currentBlockInfo, StructurePlacementData data) {
        Random lv = Random.create(MathHelper.hashCode(currentBlockInfo.pos()));
        BlockState lv2 = world.getBlockState(currentBlockInfo.pos());
        for (StructureProcessorRule lv3 : this.rules) {
            if (!lv3.test(currentBlockInfo.state(), lv2, originalBlockInfo.pos(), currentBlockInfo.pos(), pivot, lv)) continue;
            return new StructureTemplate.StructureBlockInfo(currentBlockInfo.pos(), lv3.getOutputState(), lv3.getOutputNbt(lv, currentBlockInfo.nbt()));
        }
        return currentBlockInfo;
    }

    @Override
    protected StructureProcessorType<?> getType() {
        return StructureProcessorType.RULE;
    }
}

