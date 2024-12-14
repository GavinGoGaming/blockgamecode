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
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.LivingEntityRenderer;
import net.minecraft.client.render.entity.ShulkerEntityRenderer;
import net.minecraft.client.render.entity.feature.FeatureRenderer;
import net.minecraft.client.render.entity.feature.FeatureRendererContext;
import net.minecraft.client.render.entity.model.ShulkerEntityModel;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.mob.ShulkerEntity;
import net.minecraft.util.Identifier;

@Environment(value=EnvType.CLIENT)
public class ShulkerHeadFeatureRenderer
extends FeatureRenderer<ShulkerEntity, ShulkerEntityModel<ShulkerEntity>> {
    public ShulkerHeadFeatureRenderer(FeatureRendererContext<ShulkerEntity, ShulkerEntityModel<ShulkerEntity>> arg) {
        super(arg);
    }

    @Override
    public void render(MatrixStack arg, VertexConsumerProvider arg2, int i, ShulkerEntity arg3, float f, float g, float h, float j, float k, float l) {
        Identifier lv = ShulkerEntityRenderer.getTexture(arg3.getColor());
        VertexConsumer lv2 = arg2.getBuffer(RenderLayer.getEntitySolid(lv));
        ((ShulkerEntityModel)this.getContextModel()).getHead().render(arg, lv2, i, LivingEntityRenderer.getOverlay(arg3, 0.0f));
    }
}

