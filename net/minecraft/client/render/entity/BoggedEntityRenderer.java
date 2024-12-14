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
import net.minecraft.client.render.entity.SkeletonEntityRenderer;
import net.minecraft.client.render.entity.feature.SkeletonOverlayFeatureRenderer;
import net.minecraft.client.render.entity.model.BoggedEntityModel;
import net.minecraft.client.render.entity.model.EntityModelLayers;
import net.minecraft.client.render.entity.model.SkeletonEntityModel;
import net.minecraft.entity.mob.BoggedEntity;
import net.minecraft.util.Identifier;

@Environment(value=EnvType.CLIENT)
public class BoggedEntityRenderer
extends SkeletonEntityRenderer<BoggedEntity> {
    private static final Identifier TEXTURE = Identifier.ofVanilla("textures/entity/skeleton/bogged.png");
    private static final Identifier OVERLAY_TEXTURE = Identifier.ofVanilla("textures/entity/skeleton/bogged_overlay.png");

    public BoggedEntityRenderer(EntityRendererFactory.Context arg) {
        super(arg, EntityModelLayers.BOGGED_INNER_ARMOR, EntityModelLayers.BOGGED_OUTER_ARMOR, new BoggedEntityModel(arg.getPart(EntityModelLayers.BOGGED)));
        this.addFeature(new SkeletonOverlayFeatureRenderer<BoggedEntity, SkeletonEntityModel<BoggedEntity>>(this, arg.getModelLoader(), EntityModelLayers.BOGGED_OUTER, OVERLAY_TEXTURE));
    }

    @Override
    public Identifier getTexture(BoggedEntity arg) {
        return TEXTURE;
    }
}

