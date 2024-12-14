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
import net.minecraft.client.render.OverlayTexture;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.EntityRenderer;
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.Entity;
import net.minecraft.entity.projectile.PersistentProjectileEntity;
import net.minecraft.util.Colors;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RotationAxis;

@Environment(value=EnvType.CLIENT)
public abstract class ProjectileEntityRenderer<T extends PersistentProjectileEntity>
extends EntityRenderer<T> {
    public ProjectileEntityRenderer(EntityRendererFactory.Context arg) {
        super(arg);
    }

    @Override
    public void render(T arg, float f, float g, MatrixStack arg2, VertexConsumerProvider arg3, int i) {
        arg2.push();
        arg2.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(MathHelper.lerp(g, ((PersistentProjectileEntity)arg).prevYaw, ((Entity)arg).getYaw()) - 90.0f));
        arg2.multiply(RotationAxis.POSITIVE_Z.rotationDegrees(MathHelper.lerp(g, ((PersistentProjectileEntity)arg).prevPitch, ((Entity)arg).getPitch())));
        boolean j = false;
        float h = 0.0f;
        float k = 0.5f;
        float l = 0.0f;
        float m = 0.15625f;
        float n = 0.0f;
        float o = 0.15625f;
        float p = 0.15625f;
        float q = 0.3125f;
        float r = 0.05625f;
        float s = (float)((PersistentProjectileEntity)arg).shake - g;
        if (s > 0.0f) {
            float t = -MathHelper.sin(s * 3.0f) * s;
            arg2.multiply(RotationAxis.POSITIVE_Z.rotationDegrees(t));
        }
        arg2.multiply(RotationAxis.POSITIVE_X.rotationDegrees(45.0f));
        arg2.scale(0.05625f, 0.05625f, 0.05625f);
        arg2.translate(-4.0f, 0.0f, 0.0f);
        VertexConsumer lv = arg3.getBuffer(RenderLayer.getEntityCutout(this.getTexture(arg)));
        MatrixStack.Entry lv2 = arg2.peek();
        this.vertex(lv2, lv, -7, -2, -2, 0.0f, 0.15625f, -1, 0, 0, i);
        this.vertex(lv2, lv, -7, -2, 2, 0.15625f, 0.15625f, -1, 0, 0, i);
        this.vertex(lv2, lv, -7, 2, 2, 0.15625f, 0.3125f, -1, 0, 0, i);
        this.vertex(lv2, lv, -7, 2, -2, 0.0f, 0.3125f, -1, 0, 0, i);
        this.vertex(lv2, lv, -7, 2, -2, 0.0f, 0.15625f, 1, 0, 0, i);
        this.vertex(lv2, lv, -7, 2, 2, 0.15625f, 0.15625f, 1, 0, 0, i);
        this.vertex(lv2, lv, -7, -2, 2, 0.15625f, 0.3125f, 1, 0, 0, i);
        this.vertex(lv2, lv, -7, -2, -2, 0.0f, 0.3125f, 1, 0, 0, i);
        for (int u = 0; u < 4; ++u) {
            arg2.multiply(RotationAxis.POSITIVE_X.rotationDegrees(90.0f));
            this.vertex(lv2, lv, -8, -2, 0, 0.0f, 0.0f, 0, 1, 0, i);
            this.vertex(lv2, lv, 8, -2, 0, 0.5f, 0.0f, 0, 1, 0, i);
            this.vertex(lv2, lv, 8, 2, 0, 0.5f, 0.15625f, 0, 1, 0, i);
            this.vertex(lv2, lv, -8, 2, 0, 0.0f, 0.15625f, 0, 1, 0, i);
        }
        arg2.pop();
        super.render(arg, f, g, arg2, arg3, i);
    }

    public void vertex(MatrixStack.Entry matrix, VertexConsumer vertexConsumer, int x, int y, int z, float u, float v, int normalX, int normalZ, int normalY, int light) {
        vertexConsumer.vertex(matrix, (float)x, (float)y, (float)z).color(Colors.WHITE).texture(u, v).overlay(OverlayTexture.DEFAULT_UV).light(light).normal(matrix, normalX, normalY, normalZ);
    }
}

