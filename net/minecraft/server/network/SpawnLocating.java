/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 * 
 * Could not load the following classes:
 *  org.jetbrains.annotations.Nullable
 */
package net.minecraft.server.network;

import net.minecraft.SharedConstants;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.ChunkSectionPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.Heightmap;
import net.minecraft.world.chunk.WorldChunk;
import org.jetbrains.annotations.Nullable;

public class SpawnLocating {
    @Nullable
    protected static BlockPos findOverworldSpawn(ServerWorld world, int x, int z) {
        int k;
        boolean bl = world.getDimension().hasCeiling();
        WorldChunk lv = world.getChunk(ChunkSectionPos.getSectionCoord(x), ChunkSectionPos.getSectionCoord(z));
        int n = k = bl ? world.getChunkManager().getChunkGenerator().getSpawnHeight(world) : lv.sampleHeightmap(Heightmap.Type.MOTION_BLOCKING, x & 0xF, z & 0xF);
        if (k < world.getBottomY()) {
            return null;
        }
        int l = lv.sampleHeightmap(Heightmap.Type.WORLD_SURFACE, x & 0xF, z & 0xF);
        if (l <= k && l > lv.sampleHeightmap(Heightmap.Type.OCEAN_FLOOR, x & 0xF, z & 0xF)) {
            return null;
        }
        BlockPos.Mutable lv2 = new BlockPos.Mutable();
        for (int m = k + 1; m >= world.getBottomY(); --m) {
            lv2.set(x, m, z);
            BlockState lv3 = world.getBlockState(lv2);
            if (!lv3.getFluidState().isEmpty()) break;
            if (!Block.isFaceFullSquare(lv3.getCollisionShape(world, lv2), Direction.UP)) continue;
            return ((BlockPos)lv2.up()).toImmutable();
        }
        return null;
    }

    @Nullable
    public static BlockPos findServerSpawnPoint(ServerWorld world, ChunkPos chunkPos) {
        if (SharedConstants.isOutsideGenerationArea(chunkPos)) {
            return null;
        }
        for (int i = chunkPos.getStartX(); i <= chunkPos.getEndX(); ++i) {
            for (int j = chunkPos.getStartZ(); j <= chunkPos.getEndZ(); ++j) {
                BlockPos lv = SpawnLocating.findOverworldSpawn(world, i, j);
                if (lv == null) continue;
                return lv;
            }
        }
        return null;
    }
}

