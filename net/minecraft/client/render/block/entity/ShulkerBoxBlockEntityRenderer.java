/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 * 
 * Could not load the following classes:
 *  net.fabricmc.api.EnvType
 *  net.fabricmc.api.Environment
 */
package net.minecraft.client.render.block.entity;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.block.BlockState;
import net.minecraft.block.ShulkerBoxBlock;
import net.minecraft.block.entity.ShulkerBoxBlockEntity;
import net.minecraft.client.model.ModelPart;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.TexturedRenderLayers;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.block.entity.BlockEntityRenderer;
import net.minecraft.client.render.block.entity.BlockEntityRendererFactory;
import net.minecraft.client.render.entity.model.EntityModelLayers;
import net.minecraft.client.render.entity.model.ShulkerEntityModel;
import net.minecraft.client.util.SpriteIdentifier;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.DyeColor;
import net.minecraft.util.math.Direction;

@Environment(value=EnvType.CLIENT)
public class ShulkerBoxBlockEntityRenderer
implements BlockEntityRenderer<ShulkerBoxBlockEntity> {
    private final ShulkerEntityModel<?> model;

    public ShulkerBoxBlockEntityRenderer(BlockEntityRendererFactory.Context ctx) {
        this.model = new ShulkerEntityModel(ctx.getLayerModelPart(EntityModelLayers.SHULKER));
    }

    @Override
    public void render(ShulkerBoxBlockEntity arg, float f, MatrixStack arg2, VertexConsumerProvider arg3, int i, int j) {
        DyeColor lv3;
        BlockState lv2;
        Direction lv = Direction.UP;
        if (arg.hasWorld() && (lv2 = arg.getWorld().getBlockState(arg.getPos())).getBlock() instanceof ShulkerBoxBlock) {
            lv = lv2.get(ShulkerBoxBlock.FACING);
        }
        SpriteIdentifier lv4 = (lv3 = arg.getColor()) == null ? TexturedRenderLayers.SHULKER_TEXTURE_ID : TexturedRenderLayers.COLORED_SHULKER_BOXES_TEXTURES.get(lv3.getId());
        arg2.push();
        arg2.translate(0.5f, 0.5f, 0.5f);
        float g = 0.9995f;
        arg2.scale(0.9995f, 0.9995f, 0.9995f);
        arg2.multiply(lv.getRotationQuaternion());
        arg2.scale(1.0f, -1.0f, -1.0f);
        arg2.translate(0.0f, -1.0f, 0.0f);
        ModelPart lv5 = this.model.getLid();
        lv5.setPivot(0.0f, 24.0f - arg.getAnimationProgress(f) * 0.5f * 16.0f, 0.0f);
        lv5.yaw = 270.0f * arg.getAnimationProgress(f) * ((float)Math.PI / 180);
        VertexConsumer lv6 = lv4.getVertexConsumer(arg3, RenderLayer::getEntityCutoutNoCull);
        this.model.render(arg2, lv6, i, j);
        arg2.pop();
    }
}

