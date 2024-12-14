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
import net.minecraft.client.render.entity.model.EntityModelLayers;
import net.minecraft.client.render.entity.model.FrogEntityModel;
import net.minecraft.entity.passive.FrogEntity;
import net.minecraft.entity.passive.FrogVariant;
import net.minecraft.util.Identifier;

@Environment(value=EnvType.CLIENT)
public class FrogEntityRenderer
extends MobEntityRenderer<FrogEntity, FrogEntityModel<FrogEntity>> {
    public FrogEntityRenderer(EntityRendererFactory.Context arg) {
        super(arg, new FrogEntityModel(arg.getPart(EntityModelLayers.FROG)), 0.3f);
    }

    @Override
    public Identifier getTexture(FrogEntity arg) {
        return ((FrogVariant)arg.getVariant().value()).texture();
    }
}

