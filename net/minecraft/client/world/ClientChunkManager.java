/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 * 
 * Could not load the following classes:
 *  net.fabricmc.api.EnvType
 *  net.fabricmc.api.Environment
 *  org.jetbrains.annotations.Nullable
 */
package net.minecraft.client.world;

import com.mojang.logging.LogUtils;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.atomic.AtomicReferenceArray;
import java.util.function.BooleanSupplier;
import java.util.function.Consumer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.packet.s2c.play.ChunkData;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.ChunkSectionPos;
import net.minecraft.world.BlockView;
import net.minecraft.world.LightType;
import net.minecraft.world.biome.BiomeKeys;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.ChunkManager;
import net.minecraft.world.chunk.ChunkStatus;
import net.minecraft.world.chunk.EmptyChunk;
import net.minecraft.world.chunk.WorldChunk;
import net.minecraft.world.chunk.light.LightingProvider;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;

@Environment(value=EnvType.CLIENT)
public class ClientChunkManager
extends ChunkManager {
    static final Logger LOGGER = LogUtils.getLogger();
    private final WorldChunk emptyChunk;
    private final LightingProvider lightingProvider;
    volatile ClientChunkMap chunks;
    final ClientWorld world;

    public ClientChunkManager(ClientWorld world, int loadDistance) {
        this.world = world;
        this.emptyChunk = new EmptyChunk(world, new ChunkPos(0, 0), world.getRegistryManager().get(RegistryKeys.BIOME).entryOf(BiomeKeys.PLAINS));
        this.lightingProvider = new LightingProvider(this, true, world.getDimension().hasSkyLight());
        this.chunks = new ClientChunkMap(ClientChunkManager.getChunkMapRadius(loadDistance));
    }

    @Override
    public LightingProvider getLightingProvider() {
        return this.lightingProvider;
    }

    private static boolean positionEquals(@Nullable WorldChunk chunk, int x, int z) {
        if (chunk == null) {
            return false;
        }
        ChunkPos lv = chunk.getPos();
        return lv.x == x && lv.z == z;
    }

    public void unload(ChunkPos pos) {
        if (!this.chunks.isInRadius(pos.x, pos.z)) {
            return;
        }
        int i = this.chunks.getIndex(pos.x, pos.z);
        WorldChunk lv = this.chunks.getChunk(i);
        if (ClientChunkManager.positionEquals(lv, pos.x, pos.z)) {
            this.chunks.compareAndSet(i, lv, null);
        }
    }

    @Override
    @Nullable
    public WorldChunk getChunk(int i, int j, ChunkStatus arg, boolean bl) {
        WorldChunk lv;
        if (this.chunks.isInRadius(i, j) && ClientChunkManager.positionEquals(lv = this.chunks.getChunk(this.chunks.getIndex(i, j)), i, j)) {
            return lv;
        }
        if (bl) {
            return this.emptyChunk;
        }
        return null;
    }

    @Override
    public BlockView getWorld() {
        return this.world;
    }

    public void onChunkBiomeData(int x, int z, PacketByteBuf buf) {
        if (!this.chunks.isInRadius(x, z)) {
            LOGGER.warn("Ignoring chunk since it's not in the view range: {}, {}", (Object)x, (Object)z);
            return;
        }
        int k = this.chunks.getIndex(x, z);
        WorldChunk lv = this.chunks.chunks.get(k);
        if (!ClientChunkManager.positionEquals(lv, x, z)) {
            LOGGER.warn("Ignoring chunk since it's not present: {}, {}", (Object)x, (Object)z);
        } else {
            lv.loadBiomeFromPacket(buf);
        }
    }

    @Nullable
    public WorldChunk loadChunkFromPacket(int x, int z, PacketByteBuf buf, NbtCompound nbt, Consumer<ChunkData.BlockEntityVisitor> consumer) {
        if (!this.chunks.isInRadius(x, z)) {
            LOGGER.warn("Ignoring chunk since it's not in the view range: {}, {}", (Object)x, (Object)z);
            return null;
        }
        int k = this.chunks.getIndex(x, z);
        WorldChunk lv = this.chunks.chunks.get(k);
        ChunkPos lv2 = new ChunkPos(x, z);
        if (!ClientChunkManager.positionEquals(lv, x, z)) {
            lv = new WorldChunk(this.world, lv2);
            lv.loadFromPacket(buf, nbt, consumer);
            this.chunks.set(k, lv);
        } else {
            lv.loadFromPacket(buf, nbt, consumer);
        }
        this.world.resetChunkColor(lv2);
        return lv;
    }

    @Override
    public void tick(BooleanSupplier shouldKeepTicking, boolean tickChunks) {
    }

    public void setChunkMapCenter(int x, int z) {
        this.chunks.centerChunkX = x;
        this.chunks.centerChunkZ = z;
    }

    public void updateLoadDistance(int loadDistance) {
        int j = this.chunks.radius;
        int k = ClientChunkManager.getChunkMapRadius(loadDistance);
        if (j != k) {
            ClientChunkMap lv = new ClientChunkMap(k);
            lv.centerChunkX = this.chunks.centerChunkX;
            lv.centerChunkZ = this.chunks.centerChunkZ;
            for (int l = 0; l < this.chunks.chunks.length(); ++l) {
                WorldChunk lv2 = this.chunks.chunks.get(l);
                if (lv2 == null) continue;
                ChunkPos lv3 = lv2.getPos();
                if (!lv.isInRadius(lv3.x, lv3.z)) continue;
                lv.set(lv.getIndex(lv3.x, lv3.z), lv2);
            }
            this.chunks = lv;
        }
    }

    private static int getChunkMapRadius(int loadDistance) {
        return Math.max(2, loadDistance) + 3;
    }

    @Override
    public String getDebugString() {
        return this.chunks.chunks.length() + ", " + this.getLoadedChunkCount();
    }

    @Override
    public int getLoadedChunkCount() {
        return this.chunks.loadedChunkCount;
    }

    @Override
    public void onLightUpdate(LightType type, ChunkSectionPos pos) {
        MinecraftClient.getInstance().worldRenderer.scheduleBlockRender(pos.getSectionX(), pos.getSectionY(), pos.getSectionZ());
    }

    @Override
    @Nullable
    public /* synthetic */ Chunk getChunk(int x, int z, ChunkStatus leastStatus, boolean create) {
        return this.getChunk(x, z, leastStatus, create);
    }

    @Environment(value=EnvType.CLIENT)
    final class ClientChunkMap {
        final AtomicReferenceArray<WorldChunk> chunks;
        final int radius;
        private final int diameter;
        volatile int centerChunkX;
        volatile int centerChunkZ;
        int loadedChunkCount;

        ClientChunkMap(int radius) {
            this.radius = radius;
            this.diameter = radius * 2 + 1;
            this.chunks = new AtomicReferenceArray(this.diameter * this.diameter);
        }

        int getIndex(int chunkX, int chunkZ) {
            return Math.floorMod(chunkZ, this.diameter) * this.diameter + Math.floorMod(chunkX, this.diameter);
        }

        protected void set(int index, @Nullable WorldChunk chunk) {
            WorldChunk lv = this.chunks.getAndSet(index, chunk);
            if (lv != null) {
                --this.loadedChunkCount;
                ClientChunkManager.this.world.unloadBlockEntities(lv);
            }
            if (chunk != null) {
                ++this.loadedChunkCount;
            }
        }

        protected WorldChunk compareAndSet(int index, WorldChunk expect, @Nullable WorldChunk update) {
            if (this.chunks.compareAndSet(index, expect, update) && update == null) {
                --this.loadedChunkCount;
            }
            ClientChunkManager.this.world.unloadBlockEntities(expect);
            return expect;
        }

        boolean isInRadius(int chunkX, int chunkZ) {
            return Math.abs(chunkX - this.centerChunkX) <= this.radius && Math.abs(chunkZ - this.centerChunkZ) <= this.radius;
        }

        @Nullable
        protected WorldChunk getChunk(int index) {
            return this.chunks.get(index);
        }

        private void writePositions(String fileName) {
            try (FileOutputStream fileOutputStream = new FileOutputStream(fileName);){
                int i = ClientChunkManager.this.chunks.radius;
                for (int j = this.centerChunkZ - i; j <= this.centerChunkZ + i; ++j) {
                    for (int k = this.centerChunkX - i; k <= this.centerChunkX + i; ++k) {
                        WorldChunk lv = ClientChunkManager.this.chunks.chunks.get(ClientChunkManager.this.chunks.getIndex(k, j));
                        if (lv == null) continue;
                        ChunkPos lv2 = lv.getPos();
                        fileOutputStream.write((lv2.x + "\t" + lv2.z + "\t" + lv.isEmpty() + "\n").getBytes(StandardCharsets.UTF_8));
                    }
                }
            } catch (IOException iOException) {
                LOGGER.error("Failed to dump chunks to file {}", (Object)fileName, (Object)iOException);
            }
        }
    }
}

