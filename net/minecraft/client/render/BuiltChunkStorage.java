/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 * 
 * Could not load the following classes:
 *  net.fabricmc.api.EnvType
 *  net.fabricmc.api.Environment
 *  org.jetbrains.annotations.Nullable
 */
package net.minecraft.client.render;

import java.util.Objects;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.WorldRenderer;
import net.minecraft.client.render.chunk.ChunkBuilder;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.HeightLimitView;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

@Environment(value=EnvType.CLIENT)
public class BuiltChunkStorage {
    protected final WorldRenderer worldRenderer;
    protected final World world;
    protected int sizeY;
    protected int sizeX;
    protected int sizeZ;
    private int viewDistance;
    public ChunkBuilder.BuiltChunk[] chunks;

    public BuiltChunkStorage(ChunkBuilder chunkBuilder, World world, int viewDistance, WorldRenderer worldRenderer) {
        this.worldRenderer = worldRenderer;
        this.world = world;
        this.setViewDistance(viewDistance);
        this.createChunks(chunkBuilder);
    }

    protected void createChunks(ChunkBuilder chunkBuilder) {
        if (!MinecraftClient.getInstance().isOnThread()) {
            throw new IllegalStateException("createSections called from wrong thread: " + Thread.currentThread().getName());
        }
        int i = this.sizeX * this.sizeY * this.sizeZ;
        this.chunks = new ChunkBuilder.BuiltChunk[i];
        for (int j = 0; j < this.sizeX; ++j) {
            for (int k = 0; k < this.sizeY; ++k) {
                for (int l = 0; l < this.sizeZ; ++l) {
                    int m = this.getChunkIndex(j, k, l);
                    ChunkBuilder chunkBuilder2 = chunkBuilder;
                    Objects.requireNonNull(chunkBuilder2);
                    this.chunks[m] = chunkBuilder2.new ChunkBuilder.BuiltChunk(m, j * 16, this.world.getBottomY() + k * 16, l * 16);
                }
            }
        }
    }

    public void clear() {
        for (ChunkBuilder.BuiltChunk lv : this.chunks) {
            lv.delete();
        }
    }

    private int getChunkIndex(int x, int y, int z) {
        return (z * this.sizeY + y) * this.sizeX + x;
    }

    protected void setViewDistance(int viewDistance) {
        int j;
        this.sizeX = j = viewDistance * 2 + 1;
        this.sizeY = this.world.countVerticalSections();
        this.sizeZ = j;
        this.viewDistance = viewDistance;
    }

    public int getViewDistance() {
        return this.viewDistance;
    }

    public HeightLimitView getWorld() {
        return this.world;
    }

    public void updateCameraPosition(double x, double z) {
        int i = MathHelper.ceil(x);
        int j = MathHelper.ceil(z);
        for (int k = 0; k < this.sizeX; ++k) {
            int l = this.sizeX * 16;
            int m = i - 8 - l / 2;
            int n = m + Math.floorMod(k * 16 - m, l);
            for (int o = 0; o < this.sizeZ; ++o) {
                int p = this.sizeZ * 16;
                int q = j - 8 - p / 2;
                int r = q + Math.floorMod(o * 16 - q, p);
                for (int s = 0; s < this.sizeY; ++s) {
                    int t = this.world.getBottomY() + s * 16;
                    ChunkBuilder.BuiltChunk lv = this.chunks[this.getChunkIndex(k, s, o)];
                    BlockPos lv2 = lv.getOrigin();
                    if (n == lv2.getX() && t == lv2.getY() && r == lv2.getZ()) continue;
                    lv.setOrigin(n, t, r);
                }
            }
        }
    }

    public void scheduleRebuild(int x, int y, int z, boolean important) {
        int l = Math.floorMod(x, this.sizeX);
        int m = Math.floorMod(y - this.world.getBottomSectionCoord(), this.sizeY);
        int n = Math.floorMod(z, this.sizeZ);
        ChunkBuilder.BuiltChunk lv = this.chunks[this.getChunkIndex(l, m, n)];
        lv.scheduleRebuild(important);
    }

    @Nullable
    protected ChunkBuilder.BuiltChunk getRenderedChunk(BlockPos pos) {
        int i = MathHelper.floorDiv(pos.getY() - this.world.getBottomY(), 16);
        if (i < 0 || i >= this.sizeY) {
            return null;
        }
        int j = MathHelper.floorMod(MathHelper.floorDiv(pos.getX(), 16), this.sizeX);
        int k = MathHelper.floorMod(MathHelper.floorDiv(pos.getZ(), 16), this.sizeZ);
        return this.chunks[this.getChunkIndex(j, i, k)];
    }
}

