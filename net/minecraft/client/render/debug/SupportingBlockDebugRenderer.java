/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 * 
 * Could not load the following classes:
 *  net.fabricmc.api.EnvType
 *  net.fabricmc.api.Environment
 */
package net.minecraft.client.render.debug;

import com.google.common.collect.ImmutableList;
import java.util.Collections;
import java.util.List;
import java.util.function.DoubleSupplier;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.block.ShapeContext;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.WorldRenderer;
import net.minecraft.client.render.debug.DebugRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.Entity;
import net.minecraft.util.Util;
import net.minecraft.util.math.BlockPos;

@Environment(value=EnvType.CLIENT)
public class SupportingBlockDebugRenderer
implements DebugRenderer.Renderer {
    private final MinecraftClient client;
    private double lastEntityCheckTime = Double.MIN_VALUE;
    private List<Entity> entities = Collections.emptyList();

    public SupportingBlockDebugRenderer(MinecraftClient client) {
        this.client = client;
    }

    @Override
    public void render(MatrixStack matrices, VertexConsumerProvider vertexConsumers, double cameraX, double cameraY, double cameraZ) {
        ClientPlayerEntity lv2;
        double g = Util.getMeasuringTimeNano();
        if (g - this.lastEntityCheckTime > 1.0E8) {
            this.lastEntityCheckTime = g;
            Entity lv = this.client.gameRenderer.getCamera().getFocusedEntity();
            this.entities = ImmutableList.copyOf(lv.getWorld().getOtherEntities(lv, lv.getBoundingBox().expand(16.0)));
        }
        if ((lv2 = this.client.player) != null && lv2.supportingBlockPos.isPresent()) {
            this.renderBlockHighlights(matrices, vertexConsumers, cameraX, cameraY, cameraZ, lv2, () -> 0.0, 1.0f, 0.0f, 0.0f);
        }
        for (Entity lv3 : this.entities) {
            if (lv3 == lv2) continue;
            this.renderBlockHighlights(matrices, vertexConsumers, cameraX, cameraY, cameraZ, lv3, () -> this.getAdditionalDilation(lv3), 0.0f, 1.0f, 0.0f);
        }
    }

    private void renderBlockHighlights(MatrixStack matrices, VertexConsumerProvider vertexConsumers, double cameraX, double cameraY, double cameraZ, Entity entity, DoubleSupplier extraDilation, float red, float green, float blue) {
        entity.supportingBlockPos.ifPresent(pos -> {
            double j = extraDilation.getAsDouble();
            BlockPos lv = entity.getSteppingPos();
            this.renderBlockHighlight(lv, matrices, cameraX, cameraY, cameraZ, vertexConsumers, 0.02 + j, red, green, blue);
            BlockPos lv2 = entity.getLandingPos();
            if (!lv2.equals(lv)) {
                this.renderBlockHighlight(lv2, matrices, cameraX, cameraY, cameraZ, vertexConsumers, 0.04 + j, 0.0f, 1.0f, 1.0f);
            }
        });
    }

    private double getAdditionalDilation(Entity entity) {
        return 0.02 * (double)(String.valueOf((double)entity.getId() + 0.132453657).hashCode() % 1000) / 1000.0;
    }

    private void renderBlockHighlight(BlockPos pos, MatrixStack matrices, double cameraX, double cameraY, double cameraZ, VertexConsumerProvider vertexConsumers, double dilation, float red, float green, float blue) {
        double k = (double)pos.getX() - cameraX - 2.0 * dilation;
        double l = (double)pos.getY() - cameraY - 2.0 * dilation;
        double m = (double)pos.getZ() - cameraZ - 2.0 * dilation;
        double n = k + 1.0 + 4.0 * dilation;
        double o = l + 1.0 + 4.0 * dilation;
        double p = m + 1.0 + 4.0 * dilation;
        WorldRenderer.drawBox(matrices, vertexConsumers.getBuffer(RenderLayer.getLines()), k, l, m, n, o, p, red, green, blue, 0.4f);
        WorldRenderer.drawShapeOutline(matrices, vertexConsumers.getBuffer(RenderLayer.getLines()), this.client.world.getBlockState(pos).getCollisionShape(this.client.world, pos, ShapeContext.absent()).offset(pos.getX(), pos.getY(), pos.getZ()), -cameraX, -cameraY, -cameraZ, red, green, blue, 1.0f, false);
    }
}

