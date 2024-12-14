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
import net.minecraft.client.render.entity.model.GhastEntityModel;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.mob.GhastEntity;
import net.minecraft.util.Identifier;

@Environment(value=EnvType.CLIENT)
public class GhastEntityRenderer
extends MobEntityRenderer<GhastEntity, GhastEntityModel<GhastEntity>> {
    private static final Identifier TEXTURE = Identifier.ofVanilla("textures/entity/ghast/ghast.png");
    private static final Identifier ANGRY_TEXTURE = Identifier.ofVanilla("textures/entity/ghast/ghast_shooting.png");

    public GhastEntityRenderer(EntityRendererFactory.Context arg) {
        super(arg, new GhastEntityModel(arg.getPart(EntityModelLayers.GHAST)), 1.5f);
    }

    @Override
    public Identifier getTexture(GhastEntity arg) {
        if (arg.isShooting()) {
            return ANGRY_TEXTURE;
        }
        return TEXTURE;
    }

    @Override
    protected void scale(GhastEntity arg, MatrixStack arg2, float f) {
        float g = 1.0f;
        float h = 4.5f;
        float i = 4.5f;
        arg2.scale(4.5f, 4.5f, 4.5f);
    }
}

