/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 * 
 * Could not load the following classes:
 *  net.fabricmc.api.EnvType
 *  net.fabricmc.api.Environment
 */
package net.minecraft.client.render.entity;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.OverlayTexture;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.WorldRenderer;
import net.minecraft.client.render.entity.EntityRenderer;
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.client.texture.PaintingManager;
import net.minecraft.client.texture.Sprite;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.decoration.painting.PaintingEntity;
import net.minecraft.entity.decoration.painting.PaintingVariant;
import net.minecraft.util.Colors;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RotationAxis;

@Environment(value=EnvType.CLIENT)
public class PaintingEntityRenderer
extends EntityRenderer<PaintingEntity> {
    public PaintingEntityRenderer(EntityRendererFactory.Context arg) {
        super(arg);
    }

    @Override
    public void render(PaintingEntity arg, float f, float g, MatrixStack arg2, VertexConsumerProvider arg3, int i) {
        arg2.push();
        arg2.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(180.0f - f));
        PaintingVariant lv = (PaintingVariant)arg.getVariant().value();
        VertexConsumer lv2 = arg3.getBuffer(RenderLayer.getEntitySolid(this.getTexture(arg)));
        PaintingManager lv3 = MinecraftClient.getInstance().getPaintingManager();
        this.renderPainting(arg2, lv2, arg, lv.width(), lv.height(), lv3.getPaintingSprite(lv), lv3.getBackSprite());
        arg2.pop();
        super.render(arg, f, g, arg2, arg3, i);
    }

    @Override
    public Identifier getTexture(PaintingEntity arg) {
        return MinecraftClient.getInstance().getPaintingManager().getBackSprite().getAtlasId();
    }

    private void renderPainting(MatrixStack matrices, VertexConsumer vertexConsumer, PaintingEntity entity, int width, int height, Sprite paintingSprite, Sprite backSprite) {
        MatrixStack.Entry lv = matrices.peek();
        float f = (float)(-width) / 2.0f;
        float g = (float)(-height) / 2.0f;
        float h = 0.03125f;
        float k = backSprite.getMinU();
        float l = backSprite.getMaxU();
        float m = backSprite.getMinV();
        float n = backSprite.getMaxV();
        float o = backSprite.getMinU();
        float p = backSprite.getMaxU();
        float q = backSprite.getMinV();
        float r = backSprite.getFrameV(0.0625f);
        float s = backSprite.getMinU();
        float t = backSprite.getFrameU(0.0625f);
        float u = backSprite.getMinV();
        float v = backSprite.getMaxV();
        double d = 1.0 / (double)width;
        double e = 1.0 / (double)height;
        for (int w = 0; w < width; ++w) {
            for (int x = 0; x < height; ++x) {
                float y = f + (float)(w + 1);
                float z = f + (float)w;
                float aa = g + (float)(x + 1);
                float ab = g + (float)x;
                int ac = entity.getBlockX();
                int ad = MathHelper.floor(entity.getY() + (double)((aa + ab) / 2.0f));
                int ae = entity.getBlockZ();
                Direction lv2 = entity.getHorizontalFacing();
                if (lv2 == Direction.NORTH) {
                    ac = MathHelper.floor(entity.getX() + (double)((y + z) / 2.0f));
                }
                if (lv2 == Direction.WEST) {
                    ae = MathHelper.floor(entity.getZ() - (double)((y + z) / 2.0f));
                }
                if (lv2 == Direction.SOUTH) {
                    ac = MathHelper.floor(entity.getX() - (double)((y + z) / 2.0f));
                }
                if (lv2 == Direction.EAST) {
                    ae = MathHelper.floor(entity.getZ() + (double)((y + z) / 2.0f));
                }
                int af = WorldRenderer.getLightmapCoordinates(entity.getWorld(), new BlockPos(ac, ad, ae));
                float ag = paintingSprite.getFrameU((float)(d * (double)(width - w)));
                float ah = paintingSprite.getFrameU((float)(d * (double)(width - (w + 1))));
                float ai = paintingSprite.getFrameV((float)(e * (double)(height - x)));
                float aj = paintingSprite.getFrameV((float)(e * (double)(height - (x + 1))));
                this.vertex(lv, vertexConsumer, y, ab, ah, ai, -0.03125f, 0, 0, -1, af);
                this.vertex(lv, vertexConsumer, z, ab, ag, ai, -0.03125f, 0, 0, -1, af);
                this.vertex(lv, vertexConsumer, z, aa, ag, aj, -0.03125f, 0, 0, -1, af);
                this.vertex(lv, vertexConsumer, y, aa, ah, aj, -0.03125f, 0, 0, -1, af);
                this.vertex(lv, vertexConsumer, y, aa, l, m, 0.03125f, 0, 0, 1, af);
                this.vertex(lv, vertexConsumer, z, aa, k, m, 0.03125f, 0, 0, 1, af);
                this.vertex(lv, vertexConsumer, z, ab, k, n, 0.03125f, 0, 0, 1, af);
                this.vertex(lv, vertexConsumer, y, ab, l, n, 0.03125f, 0, 0, 1, af);
                this.vertex(lv, vertexConsumer, y, aa, o, q, -0.03125f, 0, 1, 0, af);
                this.vertex(lv, vertexConsumer, z, aa, p, q, -0.03125f, 0, 1, 0, af);
                this.vertex(lv, vertexConsumer, z, aa, p, r, 0.03125f, 0, 1, 0, af);
                this.vertex(lv, vertexConsumer, y, aa, o, r, 0.03125f, 0, 1, 0, af);
                this.vertex(lv, vertexConsumer, y, ab, o, q, 0.03125f, 0, -1, 0, af);
                this.vertex(lv, vertexConsumer, z, ab, p, q, 0.03125f, 0, -1, 0, af);
                this.vertex(lv, vertexConsumer, z, ab, p, r, -0.03125f, 0, -1, 0, af);
                this.vertex(lv, vertexConsumer, y, ab, o, r, -0.03125f, 0, -1, 0, af);
                this.vertex(lv, vertexConsumer, y, aa, t, u, 0.03125f, -1, 0, 0, af);
                this.vertex(lv, vertexConsumer, y, ab, t, v, 0.03125f, -1, 0, 0, af);
                this.vertex(lv, vertexConsumer, y, ab, s, v, -0.03125f, -1, 0, 0, af);
                this.vertex(lv, vertexConsumer, y, aa, s, u, -0.03125f, -1, 0, 0, af);
                this.vertex(lv, vertexConsumer, z, aa, t, u, -0.03125f, 1, 0, 0, af);
                this.vertex(lv, vertexConsumer, z, ab, t, v, -0.03125f, 1, 0, 0, af);
                this.vertex(lv, vertexConsumer, z, ab, s, v, 0.03125f, 1, 0, 0, af);
                this.vertex(lv, vertexConsumer, z, aa, s, u, 0.03125f, 1, 0, 0, af);
            }
        }
    }

    private void vertex(MatrixStack.Entry matrix, VertexConsumer vertexConsumer, float x, float y, float u, float v, float z, int normalX, int normalY, int normalZ, int light) {
        vertexConsumer.vertex(matrix, x, y, z).color(Colors.WHITE).texture(u, v).overlay(OverlayTexture.DEFAULT_UV).light(light).normal(matrix, normalX, normalY, normalZ);
    }
}

