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
import net.minecraft.client.render.entity.feature.HeadFeatureRenderer;
import net.minecraft.client.render.entity.feature.VillagerClothingFeatureRenderer;
import net.minecraft.client.render.entity.feature.VillagerHeldItemFeatureRenderer;
import net.minecraft.client.render.entity.model.EntityModelLayers;
import net.minecraft.client.render.entity.model.VillagerResemblingModel;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.passive.VillagerEntity;
import net.minecraft.util.Identifier;

@Environment(value=EnvType.CLIENT)
public class VillagerEntityRenderer
extends MobEntityRenderer<VillagerEntity, VillagerResemblingModel<VillagerEntity>> {
    private static final Identifier TEXTURE = Identifier.ofVanilla("textures/entity/villager/villager.png");

    public VillagerEntityRenderer(EntityRendererFactory.Context arg) {
        super(arg, new VillagerResemblingModel(arg.getPart(EntityModelLayers.VILLAGER)), 0.5f);
        this.addFeature(new HeadFeatureRenderer<VillagerEntity, VillagerResemblingModel<VillagerEntity>>(this, arg.getModelLoader(), arg.getHeldItemRenderer()));
        this.addFeature(new VillagerClothingFeatureRenderer<VillagerEntity, VillagerResemblingModel<VillagerEntity>>(this, arg.getResourceManager(), "villager"));
        this.addFeature(new VillagerHeldItemFeatureRenderer<VillagerEntity, VillagerResemblingModel<VillagerEntity>>(this, arg.getHeldItemRenderer()));
    }

    @Override
    public Identifier getTexture(VillagerEntity arg) {
        return TEXTURE;
    }

    @Override
    protected void scale(VillagerEntity arg, MatrixStack arg2, float f) {
        float g = 0.9375f * arg.getScaleFactor();
        arg2.scale(g, g, g);
    }

    @Override
    protected float getShadowRadius(VillagerEntity arg) {
        float f = super.getShadowRadius(arg);
        if (arg.isBaby()) {
            return f * 0.5f;
        }
        return f;
    }

    @Override
    protected /* synthetic */ float getShadowRadius(LivingEntity arg) {
        return this.getShadowRadius((VillagerEntity)arg);
    }

    @Override
    protected /* synthetic */ float getShadowRadius(Entity entity) {
        return this.getShadowRadius((VillagerEntity)entity);
    }
}

