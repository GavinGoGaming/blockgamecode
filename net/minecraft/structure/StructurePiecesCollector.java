/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 * 
 * Could not load the following classes:
 *  org.jetbrains.annotations.Nullable
 */
package net.minecraft.structure;

import com.google.common.collect.Lists;
import java.util.List;
import net.minecraft.structure.StructurePiece;
import net.minecraft.structure.StructurePiecesHolder;
import net.minecraft.structure.StructurePiecesList;
import net.minecraft.util.math.BlockBox;
import net.minecraft.util.math.random.Random;
import org.jetbrains.annotations.Nullable;

public class StructurePiecesCollector
implements StructurePiecesHolder {
    private final List<StructurePiece> pieces = Lists.newArrayList();

    @Override
    public void addPiece(StructurePiece piece) {
        this.pieces.add(piece);
    }

    @Override
    @Nullable
    public StructurePiece getIntersecting(BlockBox box) {
        return StructurePiece.firstIntersecting(this.pieces, box);
    }

    @Deprecated
    public void shift(int y) {
        for (StructurePiece lv : this.pieces) {
            lv.translate(0, y, 0);
        }
    }

    @Deprecated
    public int shiftInto(int topY, int bottomY, Random random, int topPenalty) {
        int l = topY - topPenalty;
        BlockBox lv = this.getBoundingBox();
        int m = lv.getBlockCountY() + bottomY + 1;
        if (m < l) {
            m += random.nextInt(l - m);
        }
        int n = m - lv.getMaxY();
        this.shift(n);
        return n;
    }

    public void shiftInto(Random random, int baseY, int topY) {
        BlockBox lv = this.getBoundingBox();
        int k = topY - baseY + 1 - lv.getBlockCountY();
        int l = k > 1 ? baseY + random.nextInt(k) : baseY;
        int m = l - lv.getMinY();
        this.shift(m);
    }

    public StructurePiecesList toList() {
        return new StructurePiecesList(this.pieces);
    }

    public void clear() {
        this.pieces.clear();
    }

    public boolean isEmpty() {
        return this.pieces.isEmpty();
    }

    public BlockBox getBoundingBox() {
        return StructurePiece.boundingBox(this.pieces.stream());
    }
}

