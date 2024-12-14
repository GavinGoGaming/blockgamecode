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
import net.minecraft.client.model.ModelPart;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.client.render.entity.MobEntityRenderer;
import net.minecraft.client.render.entity.feature.BreezeEyesFeatureRenderer;
import net.minecraft.client.render.entity.feature.BreezeWindFeatureRenderer;
import net.minecraft.client.render.entity.model.BreezeEntityModel;
import net.minecraft.client.render.entity.model.EntityModelLayers;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.mob.BreezeEntity;
import net.minecraft.util.Identifier;

@Environment(value=EnvType.CLIENT)
public class BreezeEntityRenderer
extends MobEntityRenderer<BreezeEntity, BreezeEntityModel<BreezeEntity>> {
    private static final Identifier TEXTURE = Identifier.ofVanilla("textures/entity/breeze/breeze.png");

    public BreezeEntityRenderer(EntityRendererFactory.Context arg) {
        super(arg, new BreezeEntityModel(arg.getPart(EntityModelLayers.BREEZE)), 0.5f);
        this.addFeature(new BreezeWindFeatureRenderer(arg, this));
        this.addFeature(new BreezeEyesFeatureRenderer(this));
    }

    @Override
    public void render(BreezeEntity arg, float f, float g, MatrixStack arg2, VertexConsumerProvider arg3, int i) {
        BreezeEntityModel lv = (BreezeEntityModel)this.getModel();
        BreezeEntityRenderer.updatePartVisibility(lv, lv.getHead(), lv.getRods());
        super.render(arg, f, g, arg2, arg3, i);
    }

    @Override
    public Identifier getTexture(BreezeEntity arg) {
        return TEXTURE;
    }

    public static BreezeEntityModel<BreezeEntity> updatePartVisibility(BreezeEntityModel<BreezeEntity> model, ModelPart ... modelParts) {
        model.getHead().visible = false;
        model.getEyes().visible = false;
        model.getRods().visible = false;
        model.getWindBody().visible = false;
        for (ModelPart lv : modelParts) {
            lv.visible = true;
        }
        return model;
    }
}

