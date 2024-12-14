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
import net.minecraft.client.render.entity.EntityRenderer;
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.FishingBobberEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.util.Arm;
import net.minecraft.util.Colors;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;

@Environment(value=EnvType.CLIENT)
public class FishingBobberEntityRenderer
extends EntityRenderer<FishingBobberEntity> {
    private static final Identifier TEXTURE = Identifier.ofVanilla("textures/entity/fishing_hook.png");
    private static final RenderLayer LAYER = RenderLayer.getEntityCutout(TEXTURE);
    private static final double field_33632 = 960.0;

    public FishingBobberEntityRenderer(EntityRendererFactory.Context arg) {
        super(arg);
    }

    @Override
    public void render(FishingBobberEntity arg, float f, float g, MatrixStack arg2, VertexConsumerProvider arg3, int i) {
        PlayerEntity lv = arg.getPlayerOwner();
        if (lv == null) {
            return;
        }
        arg2.push();
        arg2.push();
        arg2.scale(0.5f, 0.5f, 0.5f);
        arg2.multiply(this.dispatcher.getRotation());
        MatrixStack.Entry lv2 = arg2.peek();
        VertexConsumer lv3 = arg3.getBuffer(LAYER);
        FishingBobberEntityRenderer.vertex(lv3, lv2, i, 0.0f, 0, 0, 1);
        FishingBobberEntityRenderer.vertex(lv3, lv2, i, 1.0f, 0, 1, 1);
        FishingBobberEntityRenderer.vertex(lv3, lv2, i, 1.0f, 1, 1, 0);
        FishingBobberEntityRenderer.vertex(lv3, lv2, i, 0.0f, 1, 0, 0);
        arg2.pop();
        float h = lv.getHandSwingProgress(g);
        float j = MathHelper.sin(MathHelper.sqrt(h) * (float)Math.PI);
        Vec3d lv4 = this.getHandPos(lv, j, g);
        Vec3d lv5 = arg.getLerpedPos(g).add(0.0, 0.25, 0.0);
        float k = (float)(lv4.x - lv5.x);
        float l = (float)(lv4.y - lv5.y);
        float m = (float)(lv4.z - lv5.z);
        VertexConsumer lv6 = arg3.getBuffer(RenderLayer.getLineStrip());
        MatrixStack.Entry lv7 = arg2.peek();
        int n = 16;
        for (int o = 0; o <= 16; ++o) {
            FishingBobberEntityRenderer.renderFishingLine(k, l, m, lv6, lv7, FishingBobberEntityRenderer.percentage(o, 16), FishingBobberEntityRenderer.percentage(o + 1, 16));
        }
        arg2.pop();
        super.render(arg, f, g, arg2, arg3, i);
    }

    private Vec3d getHandPos(PlayerEntity player, float f, float tickDelta) {
        int i = player.getMainArm() == Arm.RIGHT ? 1 : -1;
        ItemStack lv = player.getMainHandStack();
        if (!lv.isOf(Items.FISHING_ROD)) {
            i = -i;
        }
        if (!this.dispatcher.gameOptions.getPerspective().isFirstPerson() || player != MinecraftClient.getInstance().player) {
            float h = MathHelper.lerp(tickDelta, player.prevBodyYaw, player.bodyYaw) * ((float)Math.PI / 180);
            double d = MathHelper.sin(h);
            double e = MathHelper.cos(h);
            float j = player.getScale();
            double k = (double)i * 0.35 * (double)j;
            double l = 0.8 * (double)j;
            float m = player.isInSneakingPose() ? -0.1875f : 0.0f;
            return player.getCameraPosVec(tickDelta).add(-e * k - d * l, (double)m - 0.45 * (double)j, -d * k + e * l);
        }
        double n = 960.0 / (double)this.dispatcher.gameOptions.getFov().getValue().intValue();
        Vec3d lv2 = this.dispatcher.camera.getProjection().getPosition((float)i * 0.525f, -0.1f).multiply(n).rotateY(f * 0.5f).rotateX(-f * 0.7f);
        return player.getCameraPosVec(tickDelta).add(lv2);
    }

    private static float percentage(int value, int max) {
        return (float)value / (float)max;
    }

    private static void vertex(VertexConsumer buffer, MatrixStack.Entry matrix, int light, float x, int y, int u, int v) {
        buffer.vertex(matrix, x - 0.5f, (float)y - 0.5f, 0.0f).color(Colors.WHITE).texture(u, v).overlay(OverlayTexture.DEFAULT_UV).light(light).normal(matrix, 0.0f, 1.0f, 0.0f);
    }

    private static void renderFishingLine(float x, float y, float z, VertexConsumer buffer, MatrixStack.Entry matrices, float segmentStart, float segmentEnd) {
        float k = x * segmentStart;
        float l = y * (segmentStart * segmentStart + segmentStart) * 0.5f + 0.25f;
        float m = z * segmentStart;
        float n = x * segmentEnd - k;
        float o = y * (segmentEnd * segmentEnd + segmentEnd) * 0.5f + 0.25f - l;
        float p = z * segmentEnd - m;
        float q = MathHelper.sqrt(n * n + o * o + p * p);
        buffer.vertex(matrices, k, l, m).color(Colors.BLACK).normal(matrices, n /= q, o /= q, p /= q);
    }

    @Override
    public Identifier getTexture(FishingBobberEntity arg) {
        return TEXTURE;
    }
}

