/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 * 
 * Could not load the following classes:
 *  net.fabricmc.api.EnvType
 *  net.fabricmc.api.Environment
 */
package net.minecraft.client.render.debug;

import java.util.Map;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.WorldRenderer;
import net.minecraft.client.render.debug.DebugRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.ChunkSectionPos;
import net.minecraft.world.Heightmap;
import net.minecraft.world.chunk.Chunk;
import org.joml.Vector3f;

@Environment(value=EnvType.CLIENT)
public class HeightmapDebugRenderer
implements DebugRenderer.Renderer {
    private final MinecraftClient client;
    private static final int CHUNK_RANGE = 2;
    private static final float BOX_HEIGHT = 0.09375f;

    public HeightmapDebugRenderer(MinecraftClient client) {
        this.client = client;
    }

    @Override
    public void render(MatrixStack matrices, VertexConsumerProvider vertexConsumers, double cameraX, double cameraY, double cameraZ) {
        ClientWorld lv = this.client.world;
        VertexConsumer lv2 = vertexConsumers.getBuffer(RenderLayer.getDebugFilledBox());
        BlockPos lv3 = BlockPos.ofFloored(cameraX, 0.0, cameraZ);
        for (int i = -2; i <= 2; ++i) {
            for (int j = -2; j <= 2; ++j) {
                Chunk lv4 = lv.getChunk(lv3.add(i * 16, 0, j * 16));
                for (Map.Entry<Heightmap.Type, Heightmap> entry : lv4.getHeightmaps()) {
                    Heightmap.Type lv5 = entry.getKey();
                    ChunkPos lv6 = lv4.getPos();
                    Vector3f vector3f = this.getColorForHeightmapType(lv5);
                    for (int k = 0; k < 16; ++k) {
                        for (int l = 0; l < 16; ++l) {
                            int m = ChunkSectionPos.getOffsetPos(lv6.x, k);
                            int n = ChunkSectionPos.getOffsetPos(lv6.z, l);
                            float g = (float)((double)((float)lv.getTopY(lv5, m, n) + (float)lv5.ordinal() * 0.09375f) - cameraY);
                            WorldRenderer.renderFilledBox(matrices, lv2, (double)((float)m + 0.25f) - cameraX, (double)g, (double)((float)n + 0.25f) - cameraZ, (double)((float)m + 0.75f) - cameraX, (double)(g + 0.09375f), (double)((float)n + 0.75f) - cameraZ, vector3f.x(), vector3f.y(), vector3f.z(), 1.0f);
                        }
                    }
                }
            }
        }
    }

    private Vector3f getColorForHeightmapType(Heightmap.Type type) {
        return switch (type) {
            default -> throw new MatchException(null, null);
            case Heightmap.Type.WORLD_SURFACE_WG -> new Vector3f(1.0f, 1.0f, 0.0f);
            case Heightmap.Type.OCEAN_FLOOR_WG -> new Vector3f(1.0f, 0.0f, 1.0f);
            case Heightmap.Type.WORLD_SURFACE -> new Vector3f(0.0f, 0.7f, 0.0f);
            case Heightmap.Type.OCEAN_FLOOR -> new Vector3f(0.0f, 0.0f, 0.5f);
            case Heightmap.Type.MOTION_BLOCKING -> new Vector3f(0.0f, 0.3f, 0.3f);
            case Heightmap.Type.MOTION_BLOCKING_NO_LEAVES -> new Vector3f(0.0f, 0.5f, 0.5f);
        };
    }
}

