/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 * 
 * Could not load the following classes:
 *  net.fabricmc.api.EnvType
 *  net.fabricmc.api.Environment
 */
package net.minecraft.client.render.debug;

import com.google.common.collect.Maps;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.Camera;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.WorldRenderer;
import net.minecraft.client.render.debug.DebugRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.network.packet.s2c.custom.DebugStructuresCustomPayload;
import net.minecraft.registry.RegistryKey;
import net.minecraft.util.math.BlockBox;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

@Environment(value=EnvType.CLIENT)
public class StructureDebugRenderer
implements DebugRenderer.Renderer {
    private final MinecraftClient client;
    private final Map<RegistryKey<World>, Map<String, BlockBox>> structureBoundingBoxes = Maps.newIdentityHashMap();
    private final Map<RegistryKey<World>, Map<String, DebugStructuresCustomPayload.Piece>> structurePiecesBoundingBoxes = Maps.newIdentityHashMap();
    private static final int RANGE = 500;

    public StructureDebugRenderer(MinecraftClient client) {
        this.client = client;
    }

    @Override
    public void render(MatrixStack matrices, VertexConsumerProvider vertexConsumers, double cameraX, double cameraY, double cameraZ) {
        Map<String, DebugStructuresCustomPayload.Piece> map;
        Camera lv = this.client.gameRenderer.getCamera();
        RegistryKey<World> lv2 = this.client.world.getRegistryKey();
        BlockPos lv3 = BlockPos.ofFloored(lv.getPos().x, 0.0, lv.getPos().z);
        VertexConsumer lv4 = vertexConsumers.getBuffer(RenderLayer.getLines());
        if (this.structureBoundingBoxes.containsKey(lv2)) {
            for (BlockBox lv5 : this.structureBoundingBoxes.get(lv2).values()) {
                if (!lv3.isWithinDistance(lv5.getCenter(), 500.0)) continue;
                WorldRenderer.drawBox(matrices, lv4, (double)lv5.getMinX() - cameraX, (double)lv5.getMinY() - cameraY, (double)lv5.getMinZ() - cameraZ, (double)(lv5.getMaxX() + 1) - cameraX, (double)(lv5.getMaxY() + 1) - cameraY, (double)(lv5.getMaxZ() + 1) - cameraZ, 1.0f, 1.0f, 1.0f, 1.0f, 1.0f, 1.0f, 1.0f);
            }
        }
        if ((map = this.structurePiecesBoundingBoxes.get(lv2)) != null) {
            for (DebugStructuresCustomPayload.Piece lv6 : map.values()) {
                BlockBox lv7 = lv6.boundingBox();
                if (!lv3.isWithinDistance(lv7.getCenter(), 500.0)) continue;
                if (lv6.isStart()) {
                    WorldRenderer.drawBox(matrices, lv4, (double)lv7.getMinX() - cameraX, (double)lv7.getMinY() - cameraY, (double)lv7.getMinZ() - cameraZ, (double)(lv7.getMaxX() + 1) - cameraX, (double)(lv7.getMaxY() + 1) - cameraY, (double)(lv7.getMaxZ() + 1) - cameraZ, 0.0f, 1.0f, 0.0f, 1.0f, 0.0f, 1.0f, 0.0f);
                    continue;
                }
                WorldRenderer.drawBox(matrices, lv4, (double)lv7.getMinX() - cameraX, (double)lv7.getMinY() - cameraY, (double)lv7.getMinZ() - cameraZ, (double)(lv7.getMaxX() + 1) - cameraX, (double)(lv7.getMaxY() + 1) - cameraY, (double)(lv7.getMaxZ() + 1) - cameraZ, 0.0f, 0.0f, 1.0f, 1.0f, 0.0f, 0.0f, 1.0f);
            }
        }
    }

    public void addStructure(BlockBox boundingBox, List<DebugStructuresCustomPayload.Piece> pieces, RegistryKey<World> dimensionKey) {
        this.structureBoundingBoxes.computeIfAbsent(dimensionKey, dimension -> new HashMap()).put(boundingBox.toString(), boundingBox);
        Map map = this.structurePiecesBoundingBoxes.computeIfAbsent(dimensionKey, dimension -> new HashMap());
        for (DebugStructuresCustomPayload.Piece lv : pieces) {
            map.put(lv.boundingBox().toString(), lv);
        }
    }

    @Override
    public void clear() {
        this.structureBoundingBoxes.clear();
        this.structurePiecesBoundingBoxes.clear();
    }
}

