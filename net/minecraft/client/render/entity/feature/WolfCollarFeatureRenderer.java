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
import net.minecraft.client.render.OverlayTexture;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.feature.FeatureRenderer;
import net.minecraft.client.render.entity.feature.FeatureRendererContext;
import net.minecraft.client.render.entity.model.WolfEntityModel;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.passive.WolfEntity;
import net.minecraft.util.Identifier;

@Environment(value=EnvType.CLIENT)
public class WolfCollarFeatureRenderer
extends FeatureRenderer<WolfEntity, WolfEntityModel<WolfEntity>> {
    private static final Identifier SKIN = Identifier.ofVanilla("textures/entity/wolf/wolf_collar.png");

    public WolfCollarFeatureRenderer(FeatureRendererContext<WolfEntity, WolfEntityModel<WolfEntity>> arg) {
        super(arg);
    }

    @Override
    public void render(MatrixStack arg, VertexConsumerProvider arg2, int i, WolfEntity arg3, float f, float g, float h, float j, float k, float l) {
        if (!arg3.isTamed() || arg3.isInvisible()) {
            return;
        }
        int m = arg3.getCollarColor().getEntityColor();
        VertexConsumer lv = arg2.getBuffer(RenderLayer.getEntityCutoutNoCull(SKIN));
        ((WolfEntityModel)this.getContextModel()).render(arg, lv, i, OverlayTexture.DEFAULT_UV, m);
    }
}

