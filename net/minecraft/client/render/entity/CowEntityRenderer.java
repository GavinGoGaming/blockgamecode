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
import net.minecraft.client.render.entity.model.CowEntityModel;
import net.minecraft.client.render.entity.model.EntityModelLayers;
import net.minecraft.entity.passive.CowEntity;
import net.minecraft.util.Identifier;

@Environment(value=EnvType.CLIENT)
public class CowEntityRenderer
extends MobEntityRenderer<CowEntity, CowEntityModel<CowEntity>> {
    private static final Identifier TEXTURE = Identifier.ofVanilla("textures/entity/cow/cow.png");

    public CowEntityRenderer(EntityRendererFactory.Context arg) {
        super(arg, new CowEntityModel(arg.getPart(EntityModelLayers.COW)), 0.7f);
    }

    @Override
    public Identifier getTexture(CowEntity arg) {
        return TEXTURE;
    }
}

