/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 * 
 * Could not load the following classes:
 *  net.fabricmc.api.EnvType
 *  net.fabricmc.api.Environment
 *  org.jetbrains.annotations.Nullable
 */
package net.minecraft.client.render.debug;

import com.google.common.collect.ImmutableMap;
import java.lang.invoke.CallSite;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.debug.DebugRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.client.world.ClientChunkManager;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.registry.RegistryKey;
import net.minecraft.server.integrated.IntegratedServer;
import net.minecraft.server.world.ServerChunkManager;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Colors;
import net.minecraft.util.Util;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.ChunkSectionPos;
import net.minecraft.world.World;
import net.minecraft.world.chunk.WorldChunk;
import org.jetbrains.annotations.Nullable;

@Environment(value=EnvType.CLIENT)
public class ChunkLoadingDebugRenderer
implements DebugRenderer.Renderer {
    final MinecraftClient client;
    private double lastUpdateTime = Double.MIN_VALUE;
    private final int LOADING_DATA_CHUNK_RANGE = 12;
    @Nullable
    private ChunkLoadingStatus loadingData;

    public ChunkLoadingDebugRenderer(MinecraftClient client) {
        this.client = client;
    }

    @Override
    public void render(MatrixStack matrices, VertexConsumerProvider vertexConsumers, double cameraX, double cameraY, double cameraZ) {
        double g = Util.getMeasuringTimeNano();
        if (g - this.lastUpdateTime > 3.0E9) {
            this.lastUpdateTime = g;
            IntegratedServer lv = this.client.getServer();
            this.loadingData = lv != null ? new ChunkLoadingStatus(this, lv, cameraX, cameraZ) : null;
        }
        if (this.loadingData != null) {
            Map map = this.loadingData.serverStates.getNow(null);
            double h = this.client.gameRenderer.getCamera().getPos().y * 0.85;
            for (Map.Entry<ChunkPos, String> entry : this.loadingData.clientStates.entrySet()) {
                ChunkPos lv2 = entry.getKey();
                Object string = entry.getValue();
                if (map != null) {
                    string = (String)string + (String)map.get(lv2);
                }
                String[] strings = ((String)string).split("\n");
                int i = 0;
                for (String string2 : strings) {
                    DebugRenderer.drawString(matrices, vertexConsumers, string2, ChunkSectionPos.getOffsetPos(lv2.x, 8), h + (double)i, ChunkSectionPos.getOffsetPos(lv2.z, 8), Colors.WHITE, 0.15f, true, 0.0f, true);
                    i -= 2;
                }
            }
        }
    }

    @Environment(value=EnvType.CLIENT)
    final class ChunkLoadingStatus {
        final Map<ChunkPos, String> clientStates;
        final CompletableFuture<Map<ChunkPos, String>> serverStates;

        ChunkLoadingStatus(ChunkLoadingDebugRenderer arg, IntegratedServer server, double x, double z) {
            ClientWorld lv = arg.client.world;
            RegistryKey<World> lv2 = lv.getRegistryKey();
            int i = ChunkSectionPos.getSectionCoord(x);
            int j = ChunkSectionPos.getSectionCoord(z);
            ImmutableMap.Builder<ChunkPos, Object> builder = ImmutableMap.builder();
            ClientChunkManager lv3 = lv.getChunkManager();
            for (int k = i - 12; k <= i + 12; ++k) {
                for (int l = j - 12; l <= j + 12; ++l) {
                    ChunkPos lv4 = new ChunkPos(k, l);
                    Object string = "";
                    WorldChunk lv5 = lv3.getWorldChunk(k, l, false);
                    string = (String)string + "Client: ";
                    if (lv5 == null) {
                        string = (String)string + "0n/a\n";
                    } else {
                        string = (String)string + (lv5.isEmpty() ? " E" : "");
                        string = (String)string + "\n";
                    }
                    builder.put(lv4, string);
                }
            }
            this.clientStates = builder.build();
            this.serverStates = server.submit(() -> {
                ServerWorld lv = server.getWorld(lv2);
                if (lv == null) {
                    return ImmutableMap.of();
                }
                ImmutableMap.Builder<ChunkPos, CallSite> builder = ImmutableMap.builder();
                ServerChunkManager lv2 = lv.getChunkManager();
                for (int k = i - 12; k <= i + 12; ++k) {
                    for (int l = j - 12; l <= j + 12; ++l) {
                        ChunkPos lv3 = new ChunkPos(k, l);
                        builder.put(lv3, (CallSite)((Object)("Server: " + lv2.getChunkLoadingDebugInfo(lv3))));
                    }
                }
                return builder.build();
            });
        }
    }
}

