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
import net.minecraft.client.render.OverlayTexture;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.BreezeEntityRenderer;
import net.minecraft.client.render.entity.feature.FeatureRenderer;
import net.minecraft.client.render.entity.feature.FeatureRendererContext;
import net.minecraft.client.render.entity.model.BreezeEntityModel;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.mob.BreezeEntity;
import net.minecraft.util.Identifier;

@Environment(value=EnvType.CLIENT)
public class BreezeEyesFeatureRenderer
extends FeatureRenderer<BreezeEntity, BreezeEntityModel<BreezeEntity>> {
    private static final RenderLayer TEXTURE = RenderLayer.getEntityTranslucentEmissiveNoOutline(Identifier.ofVanilla("textures/entity/breeze/breeze_eyes.png"));

    public BreezeEyesFeatureRenderer(FeatureRendererContext<BreezeEntity, BreezeEntityModel<BreezeEntity>> arg) {
        super(arg);
    }

    @Override
    public void render(MatrixStack arg, VertexConsumerProvider arg2, int i, BreezeEntity arg3, float f, float g, float h, float j, float k, float l) {
        VertexConsumer lv = arg2.getBuffer(TEXTURE);
        BreezeEntityModel lv2 = (BreezeEntityModel)this.getContextModel();
        BreezeEntityRenderer.updatePartVisibility(lv2, lv2.getHead(), lv2.getEyes()).render(arg, lv, i, OverlayTexture.DEFAULT_UV);
    }
}

