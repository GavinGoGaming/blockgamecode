/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 * 
 * Could not load the following classes:
 *  org.jetbrains.annotations.Nullable
 */
package net.minecraft.world;

import java.util.function.Supplier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.ServerWorldAccess;
import org.jetbrains.annotations.Nullable;

public interface StructureWorldAccess
extends ServerWorldAccess {
    public long getSeed();

    default public boolean isValidForSetBlock(BlockPos pos) {
        return true;
    }

    default public void setCurrentlyGeneratingStructureName(@Nullable Supplier<String> structureName) {
    }
}

