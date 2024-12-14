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
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.client.render.entity.MobEntityRenderer;
import net.minecraft.client.render.entity.feature.WolfArmorFeatureRenderer;
import net.minecraft.client.render.entity.feature.WolfCollarFeatureRenderer;
import net.minecraft.client.render.entity.model.EntityModelLayers;
import net.minecraft.client.render.entity.model.WolfEntityModel;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.passive.WolfEntity;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.ColorHelper;

@Environment(value=EnvType.CLIENT)
public class WolfEntityRenderer
extends MobEntityRenderer<WolfEntity, WolfEntityModel<WolfEntity>> {
    public WolfEntityRenderer(EntityRendererFactory.Context arg) {
        super(arg, new WolfEntityModel(arg.getPart(EntityModelLayers.WOLF)), 0.5f);
        this.addFeature(new WolfArmorFeatureRenderer(this, arg.getModelLoader()));
        this.addFeature(new WolfCollarFeatureRenderer(this));
    }

    @Override
    protected float getAnimationProgress(WolfEntity arg, float f) {
        return arg.getTailAngle();
    }

    @Override
    public void render(WolfEntity arg, float f, float g, MatrixStack arg2, VertexConsumerProvider arg3, int i) {
        if (arg.isFurWet()) {
            float h = arg.getFurWetBrightnessMultiplier(g);
            ((WolfEntityModel)this.model).setColorMultiplier(ColorHelper.Argb.fromFloats(1.0f, h, h, h));
        }
        super.render(arg, f, g, arg2, arg3, i);
        if (arg.isFurWet()) {
            ((WolfEntityModel)this.model).setColorMultiplier(-1);
        }
    }

    @Override
    public Identifier getTexture(WolfEntity arg) {
        return arg.getTextureId();
    }
}

