/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.world.chunk.light;

import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.util.collection.PackedIntegerArray;
import net.minecraft.util.collection.PaletteStorage;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkSectionPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3i;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.util.shape.VoxelShapes;
import net.minecraft.world.BlockView;
import net.minecraft.world.HeightLimitView;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.ChunkSection;
import net.minecraft.world.chunk.light.ChunkLightProvider;

public class ChunkSkyLight {
    private static final int field_44711 = 16;
    public static final int field_44710 = Integer.MIN_VALUE;
    private final int minY;
    private final PaletteStorage palette;
    private final BlockPos.Mutable reusableBlockPos1 = new BlockPos.Mutable();
    private final BlockPos.Mutable reusableBlockPos2 = new BlockPos.Mutable();

    public ChunkSkyLight(HeightLimitView heightLimitView) {
        this.minY = heightLimitView.getBottomY() - 1;
        int i = heightLimitView.getTopY();
        int j = MathHelper.ceilLog2(i - this.minY + 1);
        this.palette = new PackedIntegerArray(j, 256);
    }

    public void refreshSurfaceY(Chunk chunk) {
        int i = chunk.getHighestNonEmptySection();
        if (i == -1) {
            this.fill(this.minY);
            return;
        }
        for (int j = 0; j < 16; ++j) {
            for (int k = 0; k < 16; ++k) {
                int l = Math.max(this.calculateSurfaceY(chunk, i, k, j), this.minY);
                this.set(ChunkSkyLight.getPackedIndex(k, j), l);
            }
        }
    }

    private int calculateSurfaceY(Chunk chunk, int topSectionIndex, int localX, int localZ) {
        int l = ChunkSectionPos.getBlockCoord(chunk.sectionIndexToCoord(topSectionIndex) + 1);
        BlockPos.Mutable lv = this.reusableBlockPos1.set(localX, l, localZ);
        BlockPos.Mutable lv2 = this.reusableBlockPos2.set((Vec3i)lv, Direction.DOWN);
        BlockState lv3 = Blocks.AIR.getDefaultState();
        for (int m = topSectionIndex; m >= 0; --m) {
            int n;
            ChunkSection lv4 = chunk.getSection(m);
            if (lv4.isEmpty()) {
                lv3 = Blocks.AIR.getDefaultState();
                n = chunk.sectionIndexToCoord(m);
                lv.setY(ChunkSectionPos.getBlockCoord(n));
                lv2.setY(lv.getY() - 1);
                continue;
            }
            for (n = 15; n >= 0; --n) {
                BlockState lv5 = lv4.getBlockState(localX, n, localZ);
                if (ChunkSkyLight.faceBlocksLight(chunk, lv, lv3, lv2, lv5)) {
                    return lv.getY();
                }
                lv3 = lv5;
                lv.set(lv2);
                lv2.move(Direction.DOWN);
            }
        }
        return this.minY;
    }

    public boolean isSkyLightAccessible(BlockView blockView, int localX, int y, int localZ) {
        BlockState lv4;
        BlockPos.Mutable lv3;
        BlockState lv2;
        int l = y + 1;
        int m = ChunkSkyLight.getPackedIndex(localX, localZ);
        int n = this.get(m);
        if (l < n) {
            return false;
        }
        BlockPos.Mutable lv = this.reusableBlockPos1.set(localX, y + 1, localZ);
        if (this.isSkyLightAccessible(blockView, m, n, lv, lv2 = blockView.getBlockState(lv), lv3 = this.reusableBlockPos2.set(localX, y, localZ), lv4 = blockView.getBlockState(lv3))) {
            return true;
        }
        BlockPos.Mutable lv5 = this.reusableBlockPos1.set(localX, y - 1, localZ);
        BlockState lv6 = blockView.getBlockState(lv5);
        return this.isSkyLightAccessible(blockView, m, n, lv3, lv4, lv5, lv6);
    }

    private boolean isSkyLightAccessible(BlockView blockView, int packedIndex, int value, BlockPos upperPos, BlockState upperState, BlockPos lowerPos, BlockState lowerState) {
        int k = upperPos.getY();
        if (ChunkSkyLight.faceBlocksLight(blockView, upperPos, upperState, lowerPos, lowerState)) {
            if (k > value) {
                this.set(packedIndex, k);
                return true;
            }
        } else if (k == value) {
            this.set(packedIndex, this.locateLightBlockingBlockBelow(blockView, lowerPos, lowerState));
            return true;
        }
        return false;
    }

    private int locateLightBlockingBlockBelow(BlockView blockView, BlockPos pos, BlockState blockState) {
        BlockPos.Mutable lv = this.reusableBlockPos1.set(pos);
        BlockPos.Mutable lv2 = this.reusableBlockPos2.set((Vec3i)pos, Direction.DOWN);
        BlockState lv3 = blockState;
        while (lv2.getY() >= this.minY) {
            BlockState lv4 = blockView.getBlockState(lv2);
            if (ChunkSkyLight.faceBlocksLight(blockView, lv, lv3, lv2, lv4)) {
                return lv.getY();
            }
            lv3 = lv4;
            lv.set(lv2);
            lv2.move(Direction.DOWN);
        }
        return this.minY;
    }

    private static boolean faceBlocksLight(BlockView blockView, BlockPos upperPos, BlockState upperState, BlockPos lowerPos, BlockState lowerState) {
        if (lowerState.getOpacity(blockView, lowerPos) != 0) {
            return true;
        }
        VoxelShape lv = ChunkLightProvider.getOpaqueShape(blockView, upperPos, upperState, Direction.DOWN);
        VoxelShape lv2 = ChunkLightProvider.getOpaqueShape(blockView, lowerPos, lowerState, Direction.UP);
        return VoxelShapes.unionCoversFullCube(lv, lv2);
    }

    public int get(int localX, int localZ) {
        int k = this.get(ChunkSkyLight.getPackedIndex(localX, localZ));
        return this.convertMinY(k);
    }

    public int getMaxSurfaceY() {
        int i = Integer.MIN_VALUE;
        for (int j = 0; j < this.palette.getSize(); ++j) {
            int k = this.palette.get(j);
            if (k <= i) continue;
            i = k;
        }
        return this.convertMinY(i + this.minY);
    }

    private void fill(int y) {
        int j = y - this.minY;
        for (int k = 0; k < this.palette.getSize(); ++k) {
            this.palette.set(k, j);
        }
    }

    private void set(int index, int y) {
        this.palette.set(index, y - this.minY);
    }

    private int get(int index) {
        return this.palette.get(index) + this.minY;
    }

    private int convertMinY(int y) {
        if (y == this.minY) {
            return Integer.MIN_VALUE;
        }
        return y;
    }

    private static int getPackedIndex(int localX, int localZ) {
        return localX + localZ * 16;
    }
}

