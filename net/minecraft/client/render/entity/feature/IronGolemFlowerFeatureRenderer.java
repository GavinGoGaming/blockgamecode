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
import net.minecraft.block.Blocks;
import net.minecraft.client.model.ModelPart;
import net.minecraft.client.render.OverlayTexture;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.block.BlockRenderManager;
import net.minecraft.client.render.entity.feature.FeatureRenderer;
import net.minecraft.client.render.entity.feature.FeatureRendererContext;
import net.minecraft.client.render.entity.model.IronGolemEntityModel;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.passive.IronGolemEntity;
import net.minecraft.util.math.RotationAxis;

@Environment(value=EnvType.CLIENT)
public class IronGolemFlowerFeatureRenderer
extends FeatureRenderer<IronGolemEntity, IronGolemEntityModel<IronGolemEntity>> {
    private final BlockRenderManager blockRenderManager;

    public IronGolemFlowerFeatureRenderer(FeatureRendererContext<IronGolemEntity, IronGolemEntityModel<IronGolemEntity>> context, BlockRenderManager blockRenderManager) {
        super(context);
        this.blockRenderManager = blockRenderManager;
    }

    @Override
    public void render(MatrixStack arg, VertexConsumerProvider arg2, int i, IronGolemEntity arg3, float f, float g, float h, float j, float k, float l) {
        if (arg3.getLookingAtVillagerTicks() == 0) {
            return;
        }
        arg.push();
        ModelPart lv = ((IronGolemEntityModel)this.getContextModel()).getRightArm();
        lv.rotate(arg);
        arg.translate(-1.1875f, 1.0625f, -0.9375f);
        arg.translate(0.5f, 0.5f, 0.5f);
        float m = 0.5f;
        arg.scale(0.5f, 0.5f, 0.5f);
        arg.multiply(RotationAxis.POSITIVE_X.rotationDegrees(-90.0f));
        arg.translate(-0.5f, -0.5f, -0.5f);
        this.blockRenderManager.renderBlockAsEntity(Blocks.POPPY.getDefaultState(), arg, arg2, i, OverlayTexture.DEFAULT_UV);
        arg.pop();
    }
}

