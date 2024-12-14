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
import net.minecraft.block.BlockState;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.block.BlockRenderManager;
import net.minecraft.client.render.entity.LivingEntityRenderer;
import net.minecraft.client.render.entity.feature.FeatureRenderer;
import net.minecraft.client.render.entity.feature.FeatureRendererContext;
import net.minecraft.client.render.entity.model.CowEntityModel;
import net.minecraft.client.render.model.BakedModel;
import net.minecraft.client.texture.SpriteAtlasTexture;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.Entity;
import net.minecraft.entity.passive.MooshroomEntity;
import net.minecraft.entity.passive.PassiveEntity;
import net.minecraft.util.math.RotationAxis;

@Environment(value=EnvType.CLIENT)
public class MooshroomMushroomFeatureRenderer<T extends MooshroomEntity>
extends FeatureRenderer<T, CowEntityModel<T>> {
    private final BlockRenderManager blockRenderManager;

    public MooshroomMushroomFeatureRenderer(FeatureRendererContext<T, CowEntityModel<T>> context, BlockRenderManager blockRenderManager) {
        super(context);
        this.blockRenderManager = blockRenderManager;
    }

    @Override
    public void render(MatrixStack arg, VertexConsumerProvider arg2, int i, T arg3, float f, float g, float h, float j, float k, float l) {
        boolean bl;
        if (((PassiveEntity)arg3).isBaby()) {
            return;
        }
        MinecraftClient lv = MinecraftClient.getInstance();
        boolean bl2 = bl = lv.hasOutline((Entity)arg3) && ((Entity)arg3).isInvisible();
        if (((Entity)arg3).isInvisible() && !bl) {
            return;
        }
        BlockState lv2 = ((MooshroomEntity)arg3).getVariant().getMushroomState();
        int m = LivingEntityRenderer.getOverlay(arg3, 0.0f);
        BakedModel lv3 = this.blockRenderManager.getModel(lv2);
        arg.push();
        arg.translate(0.2f, -0.35f, 0.5f);
        arg.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(-48.0f));
        arg.scale(-1.0f, -1.0f, 1.0f);
        arg.translate(-0.5f, -0.5f, -0.5f);
        this.renderMushroom(arg, arg2, i, bl, lv2, m, lv3);
        arg.pop();
        arg.push();
        arg.translate(0.2f, -0.35f, 0.5f);
        arg.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(42.0f));
        arg.translate(0.1f, 0.0f, -0.6f);
        arg.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(-48.0f));
        arg.scale(-1.0f, -1.0f, 1.0f);
        arg.translate(-0.5f, -0.5f, -0.5f);
        this.renderMushroom(arg, arg2, i, bl, lv2, m, lv3);
        arg.pop();
        arg.push();
        ((CowEntityModel)this.getContextModel()).getHead().rotate(arg);
        arg.translate(0.0f, -0.7f, -0.2f);
        arg.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(-78.0f));
        arg.scale(-1.0f, -1.0f, 1.0f);
        arg.translate(-0.5f, -0.5f, -0.5f);
        this.renderMushroom(arg, arg2, i, bl, lv2, m, lv3);
        arg.pop();
    }

    private void renderMushroom(MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light, boolean renderAsModel, BlockState mushroomState, int overlay, BakedModel mushroomModel) {
        if (renderAsModel) {
            this.blockRenderManager.getModelRenderer().render(matrices.peek(), vertexConsumers.getBuffer(RenderLayer.getOutline(SpriteAtlasTexture.BLOCK_ATLAS_TEXTURE)), mushroomState, mushroomModel, 0.0f, 0.0f, 0.0f, light, overlay);
        } else {
            this.blockRenderManager.renderBlockAsEntity(mushroomState, matrices, vertexConsumers, light, overlay);
        }
    }
}

