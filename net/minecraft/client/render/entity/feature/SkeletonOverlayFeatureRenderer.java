/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 * 
 * Could not load the following classes:
 *  net.fabricmc.api.EnvType
 *  net.fabricmc.api.Environment
 */
package net.minecraft.client.render.entity.feature;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.feature.FeatureRenderer;
import net.minecraft.client.render.entity.feature.FeatureRendererContext;
import net.minecraft.client.render.entity.model.EntityModel;
import net.minecraft.client.render.entity.model.EntityModelLayer;
import net.minecraft.client.render.entity.model.EntityModelLoader;
import net.minecraft.client.render.entity.model.SkeletonEntityModel;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.util.Identifier;

@Environment(value=EnvType.CLIENT)
public class SkeletonOverlayFeatureRenderer<T extends MobEntity, M extends EntityModel<T>>
extends FeatureRenderer<T, M> {
    private final SkeletonEntityModel<T> model;
    private final Identifier texture;

    public SkeletonOverlayFeatureRenderer(FeatureRendererContext<T, M> context, EntityModelLoader loader, EntityModelLayer layer, Identifier texture) {
        super(context);
        this.texture = texture;
        this.model = new SkeletonEntityModel(loader.getModelPart(layer));
    }

    @Override
    public void render(MatrixStack arg, VertexConsumerProvider arg2, int i, T arg3, float f, float g, float h, float j, float k, float l) {
        SkeletonOverlayFeatureRenderer.render(this.getContextModel(), this.model, this.texture, arg, arg2, i, arg3, f, g, j, k, l, h, -1);
    }
}

