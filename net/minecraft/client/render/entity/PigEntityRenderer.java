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
import net.minecraft.client.render.entity.feature.SaddleFeatureRenderer;
import net.minecraft.client.render.entity.model.EntityModelLayers;
import net.minecraft.client.render.entity.model.PigEntityModel;
import net.minecraft.entity.passive.PigEntity;
import net.minecraft.util.Identifier;

@Environment(value=EnvType.CLIENT)
public class PigEntityRenderer
extends MobEntityRenderer<PigEntity, PigEntityModel<PigEntity>> {
    private static final Identifier TEXTURE = Identifier.ofVanilla("textures/entity/pig/pig.png");

    public PigEntityRenderer(EntityRendererFactory.Context arg) {
        super(arg, new PigEntityModel(arg.getPart(EntityModelLayers.PIG)), 0.7f);
        this.addFeature(new SaddleFeatureRenderer(this, new PigEntityModel(arg.getPart(EntityModelLayers.PIG_SADDLE)), Identifier.ofVanilla("textures/entity/pig/pig_saddle.png")));
    }

    @Override
    public Identifier getTexture(PigEntity arg) {
        return TEXTURE;
    }
}

