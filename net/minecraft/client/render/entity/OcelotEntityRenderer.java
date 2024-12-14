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
import net.minecraft.client.render.entity.model.OcelotEntityModel;
import net.minecraft.entity.passive.OcelotEntity;
import net.minecraft.util.Identifier;

@Environment(value=EnvType.CLIENT)
public class OcelotEntityRenderer
extends MobEntityRenderer<OcelotEntity, OcelotEntityModel<OcelotEntity>> {
    private static final Identifier TEXTURE = Identifier.ofVanilla("textures/entity/cat/ocelot.png");

    public OcelotEntityRenderer(EntityRendererFactory.Context arg) {
        super(arg, new OcelotEntityModel(arg.getPart(EntityModelLayers.OCELOT)), 0.4f);
    }

    @Override
    public Identifier getTexture(OcelotEntity arg) {
        return TEXTURE;
    }
}

