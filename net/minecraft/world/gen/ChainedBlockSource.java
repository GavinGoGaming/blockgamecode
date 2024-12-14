/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 * 
 * Could not load the following classes:
 *  org.jetbrains.annotations.Nullable
 */
package net.minecraft.world.gen;

import java.util.List;
import net.minecraft.block.BlockState;
import net.minecraft.world.gen.chunk.ChunkNoiseSampler;
import net.minecraft.world.gen.densityfunction.DensityFunction;
import org.jetbrains.annotations.Nullable;

public record ChainedBlockSource(List<ChunkNoiseSampler.BlockStateSampler> samplers) implements ChunkNoiseSampler.BlockStateSampler
{
    @Override
    @Nullable
    public BlockState sample(DensityFunction.NoisePos pos) {
        for (ChunkNoiseSampler.BlockStateSampler lv : this.samplers) {
            BlockState lv2 = lv.sample(pos);
            if (lv2 == null) continue;
            return lv2;
        }
        return null;
    }
}

