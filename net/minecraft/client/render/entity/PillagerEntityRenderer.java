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
import net.minecraft.client.render.entity.IllagerEntityRenderer;
import net.minecraft.client.render.entity.feature.HeldItemFeatureRenderer;
import net.minecraft.client.render.entity.model.EntityModelLayers;
import net.minecraft.client.render.entity.model.IllagerEntityModel;
import net.minecraft.entity.mob.PillagerEntity;
import net.minecraft.util.Identifier;

@Environment(value=EnvType.CLIENT)
public class PillagerEntityRenderer
extends IllagerEntityRenderer<PillagerEntity> {
    private static final Identifier TEXTURE = Identifier.ofVanilla("textures/entity/illager/pillager.png");

    public PillagerEntityRenderer(EntityRendererFactory.Context arg) {
        super(arg, new IllagerEntityModel(arg.getPart(EntityModelLayers.PILLAGER)), 0.5f);
        this.addFeature(new HeldItemFeatureRenderer<PillagerEntity, IllagerEntityModel<PillagerEntity>>(this, arg.getHeldItemRenderer()));
    }

    @Override
    public Identifier getTexture(PillagerEntity arg) {
        return TEXTURE;
    }
}

