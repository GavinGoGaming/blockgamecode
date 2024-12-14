/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 * 
 * Could not load the following classes:
 *  org.jetbrains.annotations.Nullable
 */
package net.minecraft.structure;

import net.minecraft.structure.StructurePiece;
import net.minecraft.util.math.BlockBox;
import org.jetbrains.annotations.Nullable;

public interface StructurePiecesHolder {
    public void addPiece(StructurePiece var1);

    @Nullable
    public StructurePiece getIntersecting(BlockBox var1);
}

