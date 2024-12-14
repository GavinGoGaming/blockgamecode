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
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.LivingEntityRenderer;
import net.minecraft.client.render.entity.feature.FeatureRenderer;
import net.minecraft.client.render.entity.feature.FeatureRendererContext;
import net.minecraft.client.render.entity.model.EntityModel;
import net.minecraft.client.render.entity.model.EntityModelLayers;
import net.minecraft.client.render.entity.model.EntityModelLoader;
import net.minecraft.client.render.entity.model.SlimeEntityModel;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;

@Environment(value=EnvType.CLIENT)
public class SlimeOverlayFeatureRenderer<T extends LivingEntity>
extends FeatureRenderer<T, SlimeEntityModel<T>> {
    private final EntityModel<T> model;

    public SlimeOverlayFeatureRenderer(FeatureRendererContext<T, SlimeEntityModel<T>> context, EntityModelLoader loader) {
        super(context);
        this.model = new SlimeEntityModel(loader.getModelPart(EntityModelLayers.SLIME_OUTER));
    }

    @Override
    public void render(MatrixStack arg, VertexConsumerProvider arg2, int i, T arg3, float f, float g, float h, float j, float k, float l) {
        boolean bl;
        MinecraftClient lv = MinecraftClient.getInstance();
        boolean bl2 = bl = lv.hasOutline((Entity)arg3) && ((Entity)arg3).isInvisible();
        if (((Entity)arg3).isInvisible() && !bl) {
            return;
        }
        VertexConsumer lv2 = bl ? arg2.getBuffer(RenderLayer.getOutline(this.getTexture(arg3))) : arg2.getBuffer(RenderLayer.getEntityTranslucent(this.getTexture(arg3)));
        ((SlimeEntityModel)this.getContextModel()).copyStateTo(this.model);
        this.model.animateModel(arg3, f, g, h);
        this.model.setAngles(arg3, f, g, j, k, l);
        this.model.render(arg, lv2, i, LivingEntityRenderer.getOverlay(arg3, 0.0f));
    }
}

