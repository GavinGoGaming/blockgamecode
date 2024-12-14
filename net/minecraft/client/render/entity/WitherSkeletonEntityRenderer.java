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
import net.minecraft.client.render.entity.SkeletonEntityRenderer;
import net.minecraft.client.render.entity.model.EntityModelLayers;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.mob.WitherSkeletonEntity;
import net.minecraft.util.Identifier;

@Environment(value=EnvType.CLIENT)
public class WitherSkeletonEntityRenderer
extends SkeletonEntityRenderer<WitherSkeletonEntity> {
    private static final Identifier TEXTURE = Identifier.ofVanilla("textures/entity/skeleton/wither_skeleton.png");

    public WitherSkeletonEntityRenderer(EntityRendererFactory.Context arg) {
        super(arg, EntityModelLayers.WITHER_SKELETON, EntityModelLayers.WITHER_SKELETON_INNER_ARMOR, EntityModelLayers.WITHER_SKELETON_OUTER_ARMOR);
    }

    @Override
    public Identifier getTexture(WitherSkeletonEntity arg) {
        return TEXTURE;
    }

    @Override
    protected void scale(WitherSkeletonEntity arg, MatrixStack arg2, float f) {
        arg2.scale(1.2f, 1.2f, 1.2f);
    }
}

