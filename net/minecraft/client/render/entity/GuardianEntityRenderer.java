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
import net.minecraft.client.render.Frustum;
import net.minecraft.client.render.LightmapTextureManager;
import net.minecraft.client.render.OverlayTexture;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.client.render.entity.MobEntityRenderer;
import net.minecraft.client.render.entity.model.EntityModelLayer;
import net.minecraft.client.render.entity.model.EntityModelLayers;
import net.minecraft.client.render.entity.model.GuardianEntityModel;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.mob.GuardianEntity;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RotationAxis;
import net.minecraft.util.math.Vec3d;

@Environment(value=EnvType.CLIENT)
public class GuardianEntityRenderer
extends MobEntityRenderer<GuardianEntity, GuardianEntityModel> {
    private static final Identifier TEXTURE = Identifier.ofVanilla("textures/entity/guardian.png");
    private static final Identifier EXPLOSION_BEAM_TEXTURE = Identifier.ofVanilla("textures/entity/guardian_beam.png");
    private static final RenderLayer LAYER = RenderLayer.getEntityCutoutNoCull(EXPLOSION_BEAM_TEXTURE);

    public GuardianEntityRenderer(EntityRendererFactory.Context arg) {
        this(arg, 0.5f, EntityModelLayers.GUARDIAN);
    }

    protected GuardianEntityRenderer(EntityRendererFactory.Context ctx, float shadowRadius, EntityModelLayer layer) {
        super(ctx, new GuardianEntityModel(ctx.getPart(layer)), shadowRadius);
    }

    @Override
    public boolean shouldRender(GuardianEntity arg, Frustum arg2, double d, double e, double f) {
        LivingEntity lv;
        if (super.shouldRender(arg, arg2, d, e, f)) {
            return true;
        }
        if (arg.hasBeamTarget() && (lv = arg.getBeamTarget()) != null) {
            Vec3d lv2 = this.fromLerpedPosition(lv, (double)lv.getHeight() * 0.5, 1.0f);
            Vec3d lv3 = this.fromLerpedPosition(arg, arg.getStandingEyeHeight(), 1.0f);
            return arg2.isVisible(new Box(lv3.x, lv3.y, lv3.z, lv2.x, lv2.y, lv2.z));
        }
        return false;
    }

    private Vec3d fromLerpedPosition(LivingEntity entity, double yOffset, float delta) {
        double e = MathHelper.lerp((double)delta, entity.lastRenderX, entity.getX());
        double g = MathHelper.lerp((double)delta, entity.lastRenderY, entity.getY()) + yOffset;
        double h = MathHelper.lerp((double)delta, entity.lastRenderZ, entity.getZ());
        return new Vec3d(e, g, h);
    }

    @Override
    public void render(GuardianEntity arg, float f, float g, MatrixStack arg2, VertexConsumerProvider arg3, int i) {
        super.render(arg, f, g, arg2, arg3, i);
        LivingEntity lv = arg.getBeamTarget();
        if (lv != null) {
            float h = arg.getBeamProgress(g);
            float j = arg.getBeamTicks() + g;
            float k = j * 0.5f % 1.0f;
            float l = arg.getStandingEyeHeight();
            arg2.push();
            arg2.translate(0.0f, l, 0.0f);
            Vec3d lv2 = this.fromLerpedPosition(lv, (double)lv.getHeight() * 0.5, g);
            Vec3d lv3 = this.fromLerpedPosition(arg, l, g);
            Vec3d lv4 = lv2.subtract(lv3);
            float m = (float)(lv4.length() + 1.0);
            lv4 = lv4.normalize();
            float n = (float)Math.acos(lv4.y);
            float o = (float)Math.atan2(lv4.z, lv4.x);
            arg2.multiply(RotationAxis.POSITIVE_Y.rotationDegrees((1.5707964f - o) * 57.295776f));
            arg2.multiply(RotationAxis.POSITIVE_X.rotationDegrees(n * 57.295776f));
            boolean p = true;
            float q = j * 0.05f * -1.5f;
            float r = h * h;
            int s = 64 + (int)(r * 191.0f);
            int t = 32 + (int)(r * 191.0f);
            int u = 128 - (int)(r * 64.0f);
            float v = 0.2f;
            float w = 0.282f;
            float x = MathHelper.cos(q + 2.3561945f) * 0.282f;
            float y = MathHelper.sin(q + 2.3561945f) * 0.282f;
            float z = MathHelper.cos(q + 0.7853982f) * 0.282f;
            float aa = MathHelper.sin(q + 0.7853982f) * 0.282f;
            float ab = MathHelper.cos(q + 3.926991f) * 0.282f;
            float ac = MathHelper.sin(q + 3.926991f) * 0.282f;
            float ad = MathHelper.cos(q + 5.4977875f) * 0.282f;
            float ae = MathHelper.sin(q + 5.4977875f) * 0.282f;
            float af = MathHelper.cos(q + (float)Math.PI) * 0.2f;
            float ag = MathHelper.sin(q + (float)Math.PI) * 0.2f;
            float ah = MathHelper.cos(q + 0.0f) * 0.2f;
            float ai = MathHelper.sin(q + 0.0f) * 0.2f;
            float aj = MathHelper.cos(q + 1.5707964f) * 0.2f;
            float ak = MathHelper.sin(q + 1.5707964f) * 0.2f;
            float al = MathHelper.cos(q + 4.712389f) * 0.2f;
            float am = MathHelper.sin(q + 4.712389f) * 0.2f;
            float an = m;
            float ao = 0.0f;
            float ap = 0.4999f;
            float aq = -1.0f + k;
            float ar = m * 2.5f + aq;
            VertexConsumer lv5 = arg3.getBuffer(LAYER);
            MatrixStack.Entry lv6 = arg2.peek();
            GuardianEntityRenderer.vertex(lv5, lv6, af, an, ag, s, t, u, 0.4999f, ar);
            GuardianEntityRenderer.vertex(lv5, lv6, af, 0.0f, ag, s, t, u, 0.4999f, aq);
            GuardianEntityRenderer.vertex(lv5, lv6, ah, 0.0f, ai, s, t, u, 0.0f, aq);
            GuardianEntityRenderer.vertex(lv5, lv6, ah, an, ai, s, t, u, 0.0f, ar);
            GuardianEntityRenderer.vertex(lv5, lv6, aj, an, ak, s, t, u, 0.4999f, ar);
            GuardianEntityRenderer.vertex(lv5, lv6, aj, 0.0f, ak, s, t, u, 0.4999f, aq);
            GuardianEntityRenderer.vertex(lv5, lv6, al, 0.0f, am, s, t, u, 0.0f, aq);
            GuardianEntityRenderer.vertex(lv5, lv6, al, an, am, s, t, u, 0.0f, ar);
            float as = 0.0f;
            if (arg.age % 2 == 0) {
                as = 0.5f;
            }
            GuardianEntityRenderer.vertex(lv5, lv6, x, an, y, s, t, u, 0.5f, as + 0.5f);
            GuardianEntityRenderer.vertex(lv5, lv6, z, an, aa, s, t, u, 1.0f, as + 0.5f);
            GuardianEntityRenderer.vertex(lv5, lv6, ad, an, ae, s, t, u, 1.0f, as);
            GuardianEntityRenderer.vertex(lv5, lv6, ab, an, ac, s, t, u, 0.5f, as);
            arg2.pop();
        }
    }

    private static void vertex(VertexConsumer vertexConsumer, MatrixStack.Entry matrix, float x, float y, float z, int red, int green, int blue, float u, float v) {
        vertexConsumer.vertex(matrix, x, y, z).color(red, green, blue, 255).texture(u, v).overlay(OverlayTexture.DEFAULT_UV).light(LightmapTextureManager.MAX_LIGHT_COORDINATE).normal(matrix, 0.0f, 1.0f, 0.0f);
    }

    @Override
    public Identifier getTexture(GuardianEntity arg) {
        return TEXTURE;
    }
}

