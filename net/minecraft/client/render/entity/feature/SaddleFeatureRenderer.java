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
import net.minecraft.client.model.Model;
import net.minecraft.client.render.OverlayTexture;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.feature.FeatureRenderer;
import net.minecraft.client.render.entity.feature.FeatureRendererContext;
import net.minecraft.client.render.entity.model.EntityModel;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.Entity;
import net.minecraft.entity.Saddleable;
import net.minecraft.util.Identifier;

@Environment(value=EnvType.CLIENT)
public class SaddleFeatureRenderer<T extends Entity, M extends EntityModel<T>>
extends FeatureRenderer<T, M> {
    private final Identifier texture;
    private final M model;

    public SaddleFeatureRenderer(FeatureRendererContext<T, M> context, M model, Identifier texture) {
        super(context);
        this.model = model;
        this.texture = texture;
    }

    @Override
    public void render(MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light, T entity, float limbAngle, float limbDistance, float tickDelta, float animationProgress, float headYaw, float headPitch) {
        if (!((Saddleable)entity).isSaddled()) {
            return;
        }
        ((EntityModel)this.getContextModel()).copyStateTo(this.model);
        ((EntityModel)this.model).animateModel(entity, limbAngle, limbDistance, tickDelta);
        ((EntityModel)this.model).setAngles(entity, limbAngle, limbDistance, animationProgress, headYaw, headPitch);
        VertexConsumer lv = vertexConsumers.getBuffer(RenderLayer.getEntityCutoutNoCull(this.texture));
        ((Model)this.model).render(matrices, lv, light, OverlayTexture.DEFAULT_UV);
    }
}

