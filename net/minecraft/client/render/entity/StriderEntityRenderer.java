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
import net.minecraft.client.render.entity.model.StriderEntityModel;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.passive.StriderEntity;
import net.minecraft.util.Identifier;

@Environment(value=EnvType.CLIENT)
public class StriderEntityRenderer
extends MobEntityRenderer<StriderEntity, StriderEntityModel<StriderEntity>> {
    private static final Identifier TEXTURE = Identifier.ofVanilla("textures/entity/strider/strider.png");
    private static final Identifier COLD_TEXTURE = Identifier.ofVanilla("textures/entity/strider/strider_cold.png");
    private static final float BABY_SHADOW_RADIUS_SCALE = 0.5f;

    public StriderEntityRenderer(EntityRendererFactory.Context arg) {
        super(arg, new StriderEntityModel(arg.getPart(EntityModelLayers.STRIDER)), 0.5f);
        this.addFeature(new SaddleFeatureRenderer(this, new StriderEntityModel(arg.getPart(EntityModelLayers.STRIDER_SADDLE)), Identifier.ofVanilla("textures/entity/strider/strider_saddle.png")));
    }

    @Override
    public Identifier getTexture(StriderEntity arg) {
        return arg.isCold() ? COLD_TEXTURE : TEXTURE;
    }

    @Override
    protected float getShadowRadius(StriderEntity arg) {
        float f = super.getShadowRadius(arg);
        if (arg.isBaby()) {
            return f * 0.5f;
        }
        return f;
    }

    @Override
    protected void scale(StriderEntity arg, MatrixStack arg2, float f) {
        float g = arg.getScaleFactor();
        arg2.scale(g, g, g);
    }

    @Override
    protected boolean isShaking(StriderEntity arg) {
        return super.isShaking(arg) || arg.isCold();
    }

    @Override
    protected /* synthetic */ float getShadowRadius(LivingEntity arg) {
        return this.getShadowRadius((StriderEntity)arg);
    }

    @Override
    protected /* synthetic */ boolean isShaking(LivingEntity entity) {
        return this.isShaking((StriderEntity)entity);
    }

    @Override
    protected /* synthetic */ float getShadowRadius(Entity entity) {
        return this.getShadowRadius((StriderEntity)entity);
    }
}

