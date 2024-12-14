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
import net.minecraft.client.model.ModelData;
import net.minecraft.client.model.ModelPart;
import net.minecraft.client.model.ModelPartBuilder;
import net.minecraft.client.model.ModelPartData;
import net.minecraft.client.model.ModelTransform;
import net.minecraft.client.model.TexturedModelData;
import net.minecraft.client.render.OverlayTexture;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.feature.FeatureRenderer;
import net.minecraft.client.render.entity.feature.FeatureRendererContext;
import net.minecraft.client.render.entity.model.EntityModelLayers;
import net.minecraft.client.render.entity.model.EntityModelLoader;
import net.minecraft.client.render.entity.model.PlayerEntityModel;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.RotationAxis;

@Environment(value=EnvType.CLIENT)
public class TridentRiptideFeatureRenderer<T extends LivingEntity>
extends FeatureRenderer<T, PlayerEntityModel<T>> {
    public static final Identifier TEXTURE = Identifier.ofVanilla("textures/entity/trident_riptide.png");
    public static final String BOX = "box";
    private final ModelPart aura;

    public TridentRiptideFeatureRenderer(FeatureRendererContext<T, PlayerEntityModel<T>> context, EntityModelLoader loader) {
        super(context);
        ModelPart lv = loader.getModelPart(EntityModelLayers.SPIN_ATTACK);
        this.aura = lv.getChild(BOX);
    }

    public static TexturedModelData getTexturedModelData() {
        ModelData lv = new ModelData();
        ModelPartData lv2 = lv.getRoot();
        lv2.addChild(BOX, ModelPartBuilder.create().uv(0, 0).cuboid(-8.0f, -16.0f, -8.0f, 16.0f, 32.0f, 16.0f), ModelTransform.NONE);
        return TexturedModelData.of(lv, 64, 64);
    }

    @Override
    public void render(MatrixStack arg, VertexConsumerProvider arg2, int i, T arg3, float f, float g, float h, float j, float k, float l) {
        if (!((LivingEntity)arg3).isUsingRiptide()) {
            return;
        }
        VertexConsumer lv = arg2.getBuffer(RenderLayer.getEntityCutoutNoCull(TEXTURE));
        for (int m = 0; m < 3; ++m) {
            arg.push();
            float n = j * (float)(-(45 + m * 5));
            arg.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(n));
            float o = 0.75f * (float)m;
            arg.scale(o, o, o);
            arg.translate(0.0f, -0.2f + 0.6f * (float)m, 0.0f);
            this.aura.render(arg, lv, i, OverlayTexture.DEFAULT_UV);
            arg.pop();
        }
    }
}

