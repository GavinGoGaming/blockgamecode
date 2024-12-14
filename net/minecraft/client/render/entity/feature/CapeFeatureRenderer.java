/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 * 
 * Could not load the following classes:
 *  net.fabricmc.api.EnvType
 *  net.fabricmc.api.Environment
 */
package net.minecraft.client.render.entity.feature;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.client.render.OverlayTexture;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.feature.FeatureRenderer;
import net.minecraft.client.render.entity.feature.FeatureRendererContext;
import net.minecraft.client.render.entity.model.PlayerEntityModel;
import net.minecraft.client.util.SkinTextures;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.player.PlayerModelPart;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RotationAxis;

@Environment(value=EnvType.CLIENT)
public class CapeFeatureRenderer
extends FeatureRenderer<AbstractClientPlayerEntity, PlayerEntityModel<AbstractClientPlayerEntity>> {
    public CapeFeatureRenderer(FeatureRendererContext<AbstractClientPlayerEntity, PlayerEntityModel<AbstractClientPlayerEntity>> arg) {
        super(arg);
    }

    @Override
    public void render(MatrixStack arg, VertexConsumerProvider arg2, int i, AbstractClientPlayerEntity arg3, float f, float g, float h, float j, float k, float l) {
        if (arg3.isInvisible() || !arg3.isPartVisible(PlayerModelPart.CAPE)) {
            return;
        }
        SkinTextures lv = arg3.getSkinTextures();
        if (lv.capeTexture() == null) {
            return;
        }
        ItemStack lv2 = arg3.getEquippedStack(EquipmentSlot.CHEST);
        if (lv2.isOf(Items.ELYTRA)) {
            return;
        }
        arg.push();
        arg.translate(0.0f, 0.0f, 0.125f);
        double d = MathHelper.lerp((double)h, arg3.prevCapeX, arg3.capeX) - MathHelper.lerp((double)h, arg3.prevX, arg3.getX());
        double e = MathHelper.lerp((double)h, arg3.prevCapeY, arg3.capeY) - MathHelper.lerp((double)h, arg3.prevY, arg3.getY());
        double m = MathHelper.lerp((double)h, arg3.prevCapeZ, arg3.capeZ) - MathHelper.lerp((double)h, arg3.prevZ, arg3.getZ());
        float n = MathHelper.lerpAngleDegrees(h, arg3.prevBodyYaw, arg3.bodyYaw);
        double o = MathHelper.sin(n * ((float)Math.PI / 180));
        double p = -MathHelper.cos(n * ((float)Math.PI / 180));
        float q = (float)e * 10.0f;
        q = MathHelper.clamp(q, -6.0f, 32.0f);
        float r = (float)(d * o + m * p) * 100.0f;
        r = MathHelper.clamp(r, 0.0f, 150.0f);
        float s = (float)(d * p - m * o) * 100.0f;
        s = MathHelper.clamp(s, -20.0f, 20.0f);
        if (r < 0.0f) {
            r = 0.0f;
        }
        float t = MathHelper.lerp(h, arg3.prevStrideDistance, arg3.strideDistance);
        q += MathHelper.sin(MathHelper.lerp(h, arg3.prevHorizontalSpeed, arg3.horizontalSpeed) * 6.0f) * 32.0f * t;
        if (arg3.isInSneakingPose()) {
            q += 25.0f;
        }
        arg.multiply(RotationAxis.POSITIVE_X.rotationDegrees(6.0f + r / 2.0f + q));
        arg.multiply(RotationAxis.POSITIVE_Z.rotationDegrees(s / 2.0f));
        arg.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(180.0f - s / 2.0f));
        VertexConsumer lv3 = arg2.getBuffer(RenderLayer.getEntitySolid(lv.capeTexture()));
        ((PlayerEntityModel)this.getContextModel()).renderCape(arg, lv3, i, OverlayTexture.DEFAULT_UV);
        arg.pop();
    }
}

