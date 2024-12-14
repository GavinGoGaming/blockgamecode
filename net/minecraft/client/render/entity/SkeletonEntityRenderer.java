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
import net.minecraft.client.render.entity.BipedEntityRenderer;
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.client.render.entity.feature.ArmorFeatureRenderer;
import net.minecraft.client.render.entity.model.EntityModelLayer;
import net.minecraft.client.render.entity.model.EntityModelLayers;
import net.minecraft.client.render.entity.model.SkeletonEntityModel;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.mob.AbstractSkeletonEntity;
import net.minecraft.util.Identifier;

@Environment(value=EnvType.CLIENT)
public class SkeletonEntityRenderer<T extends AbstractSkeletonEntity>
extends BipedEntityRenderer<T, SkeletonEntityModel<T>> {
    private static final Identifier TEXTURE = Identifier.ofVanilla("textures/entity/skeleton/skeleton.png");

    public SkeletonEntityRenderer(EntityRendererFactory.Context arg) {
        this(arg, EntityModelLayers.SKELETON, EntityModelLayers.SKELETON_INNER_ARMOR, EntityModelLayers.SKELETON_OUTER_ARMOR);
    }

    public SkeletonEntityRenderer(EntityRendererFactory.Context ctx, EntityModelLayer layer, EntityModelLayer legArmorLayer, EntityModelLayer bodyArmorLayer) {
        this(ctx, legArmorLayer, bodyArmorLayer, new SkeletonEntityModel(ctx.getPart(layer)));
    }

    public SkeletonEntityRenderer(EntityRendererFactory.Context arg, EntityModelLayer arg2, EntityModelLayer arg3, SkeletonEntityModel<T> arg4) {
        super(arg, arg4, 0.5f);
        this.addFeature(new ArmorFeatureRenderer(this, new SkeletonEntityModel(arg.getPart(arg2)), new SkeletonEntityModel(arg.getPart(arg3)), arg.getModelManager()));
    }

    @Override
    public Identifier getTexture(T arg) {
        return TEXTURE;
    }

    @Override
    protected boolean isShaking(T arg) {
        return ((AbstractSkeletonEntity)arg).isShaking();
    }

    @Override
    protected /* synthetic */ boolean isShaking(LivingEntity entity) {
        return this.isShaking((T)((AbstractSkeletonEntity)entity));
    }
}

