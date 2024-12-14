/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 * 
 * Could not load the following classes:
 *  net.fabricmc.api.EnvType
 *  net.fabricmc.api.Environment
 */
package net.minecraft.client.render.debug;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.debug.DebugRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.EntityType;
import net.minecraft.network.packet.s2c.custom.DebugBreezeCustomPayload;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.ColorHelper;
import net.minecraft.util.math.Vec3d;
import org.joml.Matrix4f;

@Environment(value=EnvType.CLIENT)
public class BreezeDebugRenderer {
    private static final int PINK = ColorHelper.Argb.getArgb(255, 255, 100, 255);
    private static final int LIGHT_BLUE = ColorHelper.Argb.getArgb(255, 100, 255, 255);
    private static final int GREEN = ColorHelper.Argb.getArgb(255, 0, 255, 0);
    private static final int ORANGE = ColorHelper.Argb.getArgb(255, 255, 165, 0);
    private static final int RED = ColorHelper.Argb.getArgb(255, 255, 0, 0);
    private static final int field_47470 = 20;
    private static final float field_47471 = 0.31415927f;
    private final MinecraftClient client;
    private final Map<Integer, DebugBreezeCustomPayload.BreezeInfo> breezes = new HashMap<Integer, DebugBreezeCustomPayload.BreezeInfo>();

    public BreezeDebugRenderer(MinecraftClient client) {
        this.client = client;
    }

    public void render(MatrixStack matrices, VertexConsumerProvider vertexConsumers, double cameraX, double cameraY, double cameraZ) {
        ClientPlayerEntity lv = this.client.player;
        lv.getWorld().getEntitiesByType(EntityType.BREEZE, lv.getBoundingBox().expand(100.0), entity -> true).forEach(breeze -> {
            Optional<DebugBreezeCustomPayload.BreezeInfo> optional = Optional.ofNullable(this.breezes.get(breeze.getId()));
            optional.map(DebugBreezeCustomPayload.BreezeInfo::attackTarget).map(attackTarget -> lv.getWorld().getEntityById((int)attackTarget)).map(attackTarget -> attackTarget.getLerpedPos(this.client.getRenderTickCounter().getTickDelta(true))).ifPresent(targetPos -> {
                BreezeDebugRenderer.drawLine(matrices, vertexConsumers, cameraX, cameraY, cameraZ, breeze.getPos(), targetPos, LIGHT_BLUE);
                Vec3d lv = targetPos.add(0.0, 0.01f, 0.0);
                BreezeDebugRenderer.drawCurve(matrices.peek().getPositionMatrix(), cameraX, cameraY, cameraZ, vertexConsumers.getBuffer(RenderLayer.getDebugLineStrip(2.0)), lv, 4.0f, GREEN);
                BreezeDebugRenderer.drawCurve(matrices.peek().getPositionMatrix(), cameraX, cameraY, cameraZ, vertexConsumers.getBuffer(RenderLayer.getDebugLineStrip(2.0)), lv, 8.0f, ORANGE);
                BreezeDebugRenderer.drawCurve(matrices.peek().getPositionMatrix(), cameraX, cameraY, cameraZ, vertexConsumers.getBuffer(RenderLayer.getDebugLineStrip(2.0)), lv, 20.0f, RED);
            });
            optional.map(DebugBreezeCustomPayload.BreezeInfo::jumpTarget).ifPresent(jumpTarget -> {
                BreezeDebugRenderer.drawLine(matrices, vertexConsumers, cameraX, cameraY, cameraZ, breeze.getPos(), jumpTarget.toCenterPos(), PINK);
                DebugRenderer.drawBox(matrices, vertexConsumers, Box.from(Vec3d.of(jumpTarget)).offset(-cameraX, -cameraY, -cameraZ), 1.0f, 0.0f, 0.0f, 1.0f);
            });
        });
    }

    private static void drawLine(MatrixStack matrices, VertexConsumerProvider vertexConsumers, double cameraX, double cameraY, double cameraZ, Vec3d entityPos, Vec3d targetPos, int color) {
        VertexConsumer lv = vertexConsumers.getBuffer(RenderLayer.getDebugLineStrip(2.0));
        lv.vertex(matrices.peek(), (float)(entityPos.x - cameraX), (float)(entityPos.y - cameraY), (float)(entityPos.z - cameraZ)).color(color);
        lv.vertex(matrices.peek(), (float)(targetPos.x - cameraX), (float)(targetPos.y - cameraY), (float)(targetPos.z - cameraZ)).color(color);
    }

    private static void drawCurve(Matrix4f matrix, double cameraX, double cameraY, double cameraZ, VertexConsumer vertexConsumer, Vec3d targetPos, float multiplier, int color) {
        for (int j = 0; j < 20; ++j) {
            BreezeDebugRenderer.drawCurvePart(j, matrix, cameraX, cameraY, cameraZ, vertexConsumer, targetPos, multiplier, color);
        }
        BreezeDebugRenderer.drawCurvePart(0, matrix, cameraX, cameraY, cameraZ, vertexConsumer, targetPos, multiplier, color);
    }

    private static void drawCurvePart(int index, Matrix4f matrix, double cameraX, double cameraY, double cameraZ, VertexConsumer vertexConsumer, Vec3d targetPos, float multiplier, int color) {
        float h = (float)index * 0.31415927f;
        Vec3d lv = targetPos.add((double)multiplier * Math.cos(h), 0.0, (double)multiplier * Math.sin(h));
        vertexConsumer.vertex(matrix, (float)(lv.x - cameraX), (float)(lv.y - cameraY), (float)(lv.z - cameraZ)).color(color);
    }

    public void clear() {
        this.breezes.clear();
    }

    public void addBreezeDebugInfo(DebugBreezeCustomPayload.BreezeInfo breezeDebugInfo) {
        this.breezes.put(breezeDebugInfo.id(), breezeDebugInfo);
    }
}

