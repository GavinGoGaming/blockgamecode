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
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.client.render.entity.MobEntityRenderer;
import net.minecraft.client.render.entity.feature.ArmorFeatureRenderer;
import net.minecraft.client.render.entity.feature.HeldItemFeatureRenderer;
import net.minecraft.client.render.entity.model.BipedEntityModel;
import net.minecraft.client.render.entity.model.EntityModelLayers;
import net.minecraft.client.render.entity.model.GiantEntityModel;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.mob.GiantEntity;
import net.minecraft.util.Identifier;

@Environment(value=EnvType.CLIENT)
public class GiantEntityRenderer
extends MobEntityRenderer<GiantEntity, BipedEntityModel<GiantEntity>> {
    private static final Identifier TEXTURE = Identifier.ofVanilla("textures/entity/zombie/zombie.png");
    private final float scale;

    public GiantEntityRenderer(EntityRendererFactory.Context ctx, float scale) {
        super(ctx, new GiantEntityModel(ctx.getPart(EntityModelLayers.GIANT)), 0.5f * scale);
        this.scale = scale;
        this.addFeature(new HeldItemFeatureRenderer<GiantEntity, BipedEntityModel<GiantEntity>>(this, ctx.getHeldItemRenderer()));
        this.addFeature(new ArmorFeatureRenderer<GiantEntity, BipedEntityModel<GiantEntity>, GiantEntityModel>(this, new GiantEntityModel(ctx.getPart(EntityModelLayers.GIANT_INNER_ARMOR)), new GiantEntityModel(ctx.getPart(EntityModelLayers.GIANT_OUTER_ARMOR)), ctx.getModelManager()));
    }

    @Override
    protected void scale(GiantEntity arg, MatrixStack arg2, float f) {
        arg2.scale(this.scale, this.scale, this.scale);
    }

    @Override
    public Identifier getTexture(GiantEntity arg) {
        return TEXTURE;
    }
}

