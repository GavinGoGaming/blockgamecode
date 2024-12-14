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
import net.minecraft.client.render.entity.model.PolarBearEntityModel;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.passive.PolarBearEntity;
import net.minecraft.util.Identifier;

@Environment(value=EnvType.CLIENT)
public class PolarBearEntityRenderer
extends MobEntityRenderer<PolarBearEntity, PolarBearEntityModel<PolarBearEntity>> {
    private static final Identifier TEXTURE = Identifier.ofVanilla("textures/entity/bear/polarbear.png");

    public PolarBearEntityRenderer(EntityRendererFactory.Context arg) {
        super(arg, new PolarBearEntityModel(arg.getPart(EntityModelLayers.POLAR_BEAR)), 0.9f);
    }

    @Override
    public Identifier getTexture(PolarBearEntity arg) {
        return TEXTURE;
    }

    @Override
    protected void scale(PolarBearEntity arg, MatrixStack arg2, float f) {
        arg2.scale(1.2f, 1.2f, 1.2f);
        super.scale(arg, arg2, f);
    }
}

