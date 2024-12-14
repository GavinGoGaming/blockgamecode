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
import net.minecraft.client.render.entity.feature.WitchHeldItemFeatureRenderer;
import net.minecraft.client.render.entity.model.EntityModelLayers;
import net.minecraft.client.render.entity.model.WitchEntityModel;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.mob.WitchEntity;
import net.minecraft.util.Identifier;

@Environment(value=EnvType.CLIENT)
public class WitchEntityRenderer
extends MobEntityRenderer<WitchEntity, WitchEntityModel<WitchEntity>> {
    private static final Identifier TEXTURE = Identifier.ofVanilla("textures/entity/witch.png");

    public WitchEntityRenderer(EntityRendererFactory.Context arg) {
        super(arg, new WitchEntityModel(arg.getPart(EntityModelLayers.WITCH)), 0.5f);
        this.addFeature(new WitchHeldItemFeatureRenderer<WitchEntity>(this, arg.getHeldItemRenderer()));
    }

    @Override
    public void render(WitchEntity arg, float f, float g, MatrixStack arg2, VertexConsumerProvider arg3, int i) {
        ((WitchEntityModel)this.model).setLiftingNose(!arg.getMainHandStack().isEmpty());
        super.render(arg, f, g, arg2, arg3, i);
    }

    @Override
    public Identifier getTexture(WitchEntity arg) {
        return TEXTURE;
    }

    @Override
    protected void scale(WitchEntity arg, MatrixStack arg2, float f) {
        float g = 0.9375f;
        arg2.scale(0.9375f, 0.9375f, 0.9375f);
    }
}

