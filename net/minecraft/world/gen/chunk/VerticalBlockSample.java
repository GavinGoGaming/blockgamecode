/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.world.gen.chunk;

import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.world.gen.chunk.BlockColumn;

public final class VerticalBlockSample
implements BlockColumn {
    private final int startY;
    private final BlockState[] states;

    public VerticalBlockSample(int startY, BlockState[] states) {
        this.startY = startY;
        this.states = states;
    }

    @Override
    public BlockState getState(int y) {
        int j = y - this.startY;
        if (j < 0 || j >= this.states.length) {
            return Blocks.AIR.getDefaultState();
        }
        return this.states[j];
    }

    @Override
    public void setState(int y, BlockState state) {
        int j = y - this.startY;
        if (j < 0 || j >= this.states.length) {
            throw new IllegalArgumentException("Outside of column height: " + y);
        }
        this.states[j] = state;
    }
}

