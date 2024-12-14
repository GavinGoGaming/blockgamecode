/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.structure;

import net.minecraft.nbt.NbtCompound;
import net.minecraft.structure.StructureContext;
import net.minecraft.structure.StructurePiece;
import net.minecraft.structure.StructurePieceType;
import net.minecraft.util.math.BlockBox;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.Heightmap;
import net.minecraft.world.WorldAccess;

public abstract class ShiftableStructurePiece
extends StructurePiece {
    protected final int width;
    protected final int height;
    protected final int depth;
    protected int hPos = -1;

    protected ShiftableStructurePiece(StructurePieceType type, int x, int y, int z, int width, int height, int depth, Direction orientation) {
        super(type, 0, StructurePiece.createBox(x, y, z, orientation, width, height, depth));
        this.width = width;
        this.height = height;
        this.depth = depth;
        this.setOrientation(orientation);
    }

    protected ShiftableStructurePiece(StructurePieceType arg, NbtCompound arg2) {
        super(arg, arg2);
        this.width = arg2.getInt("Width");
        this.height = arg2.getInt("Height");
        this.depth = arg2.getInt("Depth");
        this.hPos = arg2.getInt("HPos");
    }

    @Override
    protected void writeNbt(StructureContext context, NbtCompound nbt) {
        nbt.putInt("Width", this.width);
        nbt.putInt("Height", this.height);
        nbt.putInt("Depth", this.depth);
        nbt.putInt("HPos", this.hPos);
    }

    protected boolean adjustToAverageHeight(WorldAccess world, BlockBox boundingBox, int deltaY) {
        if (this.hPos >= 0) {
            return true;
        }
        int j = 0;
        int k = 0;
        BlockPos.Mutable lv = new BlockPos.Mutable();
        for (int l = this.boundingBox.getMinZ(); l <= this.boundingBox.getMaxZ(); ++l) {
            for (int m = this.boundingBox.getMinX(); m <= this.boundingBox.getMaxX(); ++m) {
                lv.set(m, 64, l);
                if (!boundingBox.contains(lv)) continue;
                j += world.getTopPosition(Heightmap.Type.MOTION_BLOCKING_NO_LEAVES, lv).getY();
                ++k;
            }
        }
        if (k == 0) {
            return false;
        }
        this.hPos = j / k;
        this.boundingBox.move(0, this.hPos - this.boundingBox.getMinY() + deltaY, 0);
        return true;
    }

    protected boolean adjustToMinHeight(WorldAccess world, int yOffset) {
        if (this.hPos >= 0) {
            return true;
        }
        int j = world.getTopY();
        boolean bl = false;
        BlockPos.Mutable lv = new BlockPos.Mutable();
        for (int k = this.boundingBox.getMinZ(); k <= this.boundingBox.getMaxZ(); ++k) {
            for (int l = this.boundingBox.getMinX(); l <= this.boundingBox.getMaxX(); ++l) {
                lv.set(l, 0, k);
                j = Math.min(j, world.getTopPosition(Heightmap.Type.MOTION_BLOCKING_NO_LEAVES, lv).getY());
                bl = true;
            }
        }
        if (!bl) {
            return false;
        }
        this.hPos = j;
        this.boundingBox.move(0, this.hPos - this.boundingBox.getMinY() + yOffset, 0);
        return true;
    }
}

