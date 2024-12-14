/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 * 
 * Could not load the following classes:
 *  net.fabricmc.api.EnvType
 *  net.fabricmc.api.Environment
 *  org.jetbrains.annotations.Nullable
 */
package net.minecraft.client.render.debug;

import java.time.Duration;
import java.time.Instant;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.WorldRenderer;
import net.minecraft.client.render.debug.DebugRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.ChunkSectionPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.shape.BitSetVoxelSet;
import net.minecraft.util.shape.VoxelSet;
import net.minecraft.world.LightType;
import net.minecraft.world.chunk.light.LightStorage;
import net.minecraft.world.chunk.light.LightingProvider;
import org.jetbrains.annotations.Nullable;
import org.joml.Matrix4f;
import org.joml.Vector4f;

@Environment(value=EnvType.CLIENT)
public class LightDebugRenderer
implements DebugRenderer.Renderer {
    private static final Duration UPDATE_INTERVAL = Duration.ofMillis(500L);
    private static final int RADIUS = 10;
    private static final Vector4f READY_SHAPE_COLOR = new Vector4f(1.0f, 1.0f, 0.0f, 0.25f);
    private static final Vector4f DEFAULT_SHAPE_COLOR = new Vector4f(0.25f, 0.125f, 0.0f, 0.125f);
    private final MinecraftClient client;
    private final LightType lightType;
    private Instant prevUpdateTime = Instant.now();
    @Nullable
    private Data data;

    public LightDebugRenderer(MinecraftClient client, LightType lightType) {
        this.client = client;
        this.lightType = lightType;
    }

    @Override
    public void render(MatrixStack matrices, VertexConsumerProvider vertexConsumers, double cameraX, double cameraY, double cameraZ) {
        Instant instant = Instant.now();
        if (this.data == null || Duration.between(this.prevUpdateTime, instant).compareTo(UPDATE_INTERVAL) > 0) {
            this.prevUpdateTime = instant;
            this.data = new Data(this.client.world.getLightingProvider(), ChunkSectionPos.from(this.client.player.getBlockPos()), 10, this.lightType);
        }
        LightDebugRenderer.drawEdges(matrices, this.data.readyShape, this.data.minSectionPos, vertexConsumers, cameraX, cameraY, cameraZ, READY_SHAPE_COLOR);
        LightDebugRenderer.drawEdges(matrices, this.data.shape, this.data.minSectionPos, vertexConsumers, cameraX, cameraY, cameraZ, DEFAULT_SHAPE_COLOR);
        VertexConsumer lv = vertexConsumers.getBuffer(RenderLayer.getDebugSectionQuads());
        LightDebugRenderer.drawFaces(matrices, this.data.readyShape, this.data.minSectionPos, lv, cameraX, cameraY, cameraZ, READY_SHAPE_COLOR);
        LightDebugRenderer.drawFaces(matrices, this.data.shape, this.data.minSectionPos, lv, cameraX, cameraY, cameraZ, DEFAULT_SHAPE_COLOR);
    }

    private static void drawFaces(MatrixStack matrices, VoxelSet shape, ChunkSectionPos sectionPos, VertexConsumer vertexConsumer, double cameraX, double cameraY, double cameraZ, Vector4f color) {
        shape.forEachDirection((direction, offsetX, offsetY, offsetZ) -> {
            int l = offsetX + sectionPos.getX();
            int m = offsetY + sectionPos.getY();
            int n = offsetZ + sectionPos.getZ();
            LightDebugRenderer.drawFace(matrices, vertexConsumer, direction, cameraX, cameraY, cameraZ, l, m, n, color);
        });
    }

    private static void drawEdges(MatrixStack matrices, VoxelSet shape, ChunkSectionPos sectionPos, VertexConsumerProvider vertexConsumers, double cameraX, double cameraY, double cameraZ, Vector4f color) {
        shape.forEachEdge((x1, y1, z1, x2, y2, z2) -> {
            int o = x1 + sectionPos.getX();
            int p = y1 + sectionPos.getY();
            int q = z1 + sectionPos.getZ();
            int r = x2 + sectionPos.getX();
            int s = y2 + sectionPos.getY();
            int t = z2 + sectionPos.getZ();
            VertexConsumer lv = vertexConsumers.getBuffer(RenderLayer.getDebugLineStrip(1.0));
            LightDebugRenderer.drawEdge(matrices, lv, cameraX, cameraY, cameraZ, o, p, q, r, s, t, color);
        }, true);
    }

    private static void drawFace(MatrixStack matrices, VertexConsumer vertexConsumer, Direction direction, double cameraX, double cameraY, double cameraZ, int x, int y, int z, Vector4f color) {
        float g = (float)((double)ChunkSectionPos.getBlockCoord(x) - cameraX);
        float h = (float)((double)ChunkSectionPos.getBlockCoord(y) - cameraY);
        float l = (float)((double)ChunkSectionPos.getBlockCoord(z) - cameraZ);
        WorldRenderer.renderFilledBoxFace(matrices, vertexConsumer, direction, g, h, l, g + 16.0f, h + 16.0f, l + 16.0f, color.x(), color.y(), color.z(), color.w());
    }

    private static void drawEdge(MatrixStack matrices, VertexConsumer vertexConsumer, double cameraX, double cameraY, double cameraZ, int x1, int y1, int z1, int x2, int y2, int z, Vector4f color) {
        float g = (float)((double)ChunkSectionPos.getBlockCoord(x1) - cameraX);
        float h = (float)((double)ChunkSectionPos.getBlockCoord(y1) - cameraY);
        float o = (float)((double)ChunkSectionPos.getBlockCoord(z1) - cameraZ);
        float p = (float)((double)ChunkSectionPos.getBlockCoord(x2) - cameraX);
        float q = (float)((double)ChunkSectionPos.getBlockCoord(y2) - cameraY);
        float r = (float)((double)ChunkSectionPos.getBlockCoord(z) - cameraZ);
        Matrix4f matrix4f = matrices.peek().getPositionMatrix();
        vertexConsumer.vertex(matrix4f, g, h, o).color(color.x(), color.y(), color.z(), 1.0f);
        vertexConsumer.vertex(matrix4f, p, q, r).color(color.x(), color.y(), color.z(), 1.0f);
    }

    @Environment(value=EnvType.CLIENT)
    static final class Data {
        final VoxelSet readyShape;
        final VoxelSet shape;
        final ChunkSectionPos minSectionPos;

        Data(LightingProvider lightingProvider, ChunkSectionPos sectionPos, int radius, LightType lightType) {
            int j = radius * 2 + 1;
            this.readyShape = new BitSetVoxelSet(j, j, j);
            this.shape = new BitSetVoxelSet(j, j, j);
            for (int k = 0; k < j; ++k) {
                for (int l = 0; l < j; ++l) {
                    for (int m = 0; m < j; ++m) {
                        ChunkSectionPos lv = ChunkSectionPos.from(sectionPos.getSectionX() + m - radius, sectionPos.getSectionY() + l - radius, sectionPos.getSectionZ() + k - radius);
                        LightStorage.Status lv2 = lightingProvider.getStatus(lightType, lv);
                        if (lv2 == LightStorage.Status.LIGHT_AND_DATA) {
                            this.readyShape.set(m, l, k);
                            this.shape.set(m, l, k);
                            continue;
                        }
                        if (lv2 != LightStorage.Status.LIGHT_ONLY) continue;
                        this.shape.set(m, l, k);
                    }
                }
            }
            this.minSectionPos = ChunkSectionPos.from(sectionPos.getSectionX() - radius, sectionPos.getSectionY() - radius, sectionPos.getSectionZ() - radius);
        }
    }
}

