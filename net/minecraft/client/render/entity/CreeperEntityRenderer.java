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
import net.minecraft.client.render.entity.feature.CreeperChargeFeatureRenderer;
import net.minecraft.client.render.entity.model.CreeperEntityModel;
import net.minecraft.client.render.entity.model.EntityModelLayers;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.mob.CreeperEntity;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.MathHelper;

@Environment(value=EnvType.CLIENT)
public class CreeperEntityRenderer
extends MobEntityRenderer<CreeperEntity, CreeperEntityModel<CreeperEntity>> {
    private static final Identifier TEXTURE = Identifier.ofVanilla("textures/entity/creeper/creeper.png");

    public CreeperEntityRenderer(EntityRendererFactory.Context arg) {
        super(arg, new CreeperEntityModel(arg.getPart(EntityModelLayers.CREEPER)), 0.5f);
        this.addFeature(new CreeperChargeFeatureRenderer(this, arg.getModelLoader()));
    }

    @Override
    protected void scale(CreeperEntity arg, MatrixStack arg2, float f) {
        float g = arg.getClientFuseTime(f);
        float h = 1.0f + MathHelper.sin(g * 100.0f) * g * 0.01f;
        g = MathHelper.clamp(g, 0.0f, 1.0f);
        g *= g;
        g *= g;
        float i = (1.0f + g * 0.4f) * h;
        float j = (1.0f + g * 0.1f) / h;
        arg2.scale(i, j, i);
    }

    @Override
    protected float getAnimationCounter(CreeperEntity arg, float f) {
        float g = arg.getClientFuseTime(f);
        if ((int)(g * 10.0f) % 2 == 0) {
            return 0.0f;
        }
        return MathHelper.clamp(g, 0.5f, 1.0f);
    }

    @Override
    public Identifier getTexture(CreeperEntity arg) {
        return TEXTURE;
    }

    @Override
    protected /* synthetic */ float getAnimationCounter(LivingEntity entity, float tickDelta) {
        return this.getAnimationCounter((CreeperEntity)entity, tickDelta);
    }
}

