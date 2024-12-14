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
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.EntityRenderer;
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.client.render.entity.model.EntityModelLayers;
import net.minecraft.client.render.entity.model.TridentEntityModel;
import net.minecraft.client.render.item.ItemRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.projectile.TridentEntity;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RotationAxis;

@Environment(value=EnvType.CLIENT)
public class TridentEntityRenderer
extends EntityRenderer<TridentEntity> {
    public static final Identifier TEXTURE = Identifier.ofVanilla("textures/entity/trident.png");
    private final TridentEntityModel model;

    public TridentEntityRenderer(EntityRendererFactory.Context arg) {
        super(arg);
        this.model = new TridentEntityModel(arg.getPart(EntityModelLayers.TRIDENT));
    }

    @Override
    public void render(TridentEntity arg, float f, float g, MatrixStack arg2, VertexConsumerProvider arg3, int i) {
        arg2.push();
        arg2.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(MathHelper.lerp(g, arg.prevYaw, arg.getYaw()) - 90.0f));
        arg2.multiply(RotationAxis.POSITIVE_Z.rotationDegrees(MathHelper.lerp(g, arg.prevPitch, arg.getPitch()) + 90.0f));
        VertexConsumer lv = ItemRenderer.getDirectItemGlintConsumer(arg3, this.model.getLayer(this.getTexture(arg)), false, arg.isEnchanted());
        this.model.render(arg2, lv, i, OverlayTexture.DEFAULT_UV);
        arg2.pop();
        super.render(arg, f, g, arg2, arg3, i);
    }

    @Override
    public Identifier getTexture(TridentEntity arg) {
        return TEXTURE;
    }
}

