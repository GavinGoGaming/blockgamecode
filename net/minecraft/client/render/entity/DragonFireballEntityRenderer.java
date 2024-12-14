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
import net.minecraft.client.render.OverlayTexture;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.EntityRenderer;
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.projectile.DragonFireballEntity;
import net.minecraft.util.Colors;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;

@Environment(value=EnvType.CLIENT)
public class DragonFireballEntityRenderer
extends EntityRenderer<DragonFireballEntity> {
    private static final Identifier TEXTURE = Identifier.ofVanilla("textures/entity/enderdragon/dragon_fireball.png");
    private static final RenderLayer LAYER = RenderLayer.getEntityCutoutNoCull(TEXTURE);

    public DragonFireballEntityRenderer(EntityRendererFactory.Context arg) {
        super(arg);
    }

    @Override
    protected int getBlockLight(DragonFireballEntity arg, BlockPos arg2) {
        return 15;
    }

    @Override
    public void render(DragonFireballEntity arg, float f, float g, MatrixStack arg2, VertexConsumerProvider arg3, int i) {
        arg2.push();
        arg2.scale(2.0f, 2.0f, 2.0f);
        arg2.multiply(this.dispatcher.getRotation());
        MatrixStack.Entry lv = arg2.peek();
        VertexConsumer lv2 = arg3.getBuffer(LAYER);
        DragonFireballEntityRenderer.produceVertex(lv2, lv, i, 0.0f, 0, 0, 1);
        DragonFireballEntityRenderer.produceVertex(lv2, lv, i, 1.0f, 0, 1, 1);
        DragonFireballEntityRenderer.produceVertex(lv2, lv, i, 1.0f, 1, 1, 0);
        DragonFireballEntityRenderer.produceVertex(lv2, lv, i, 0.0f, 1, 0, 0);
        arg2.pop();
        super.render(arg, f, g, arg2, arg3, i);
    }

    private static void produceVertex(VertexConsumer vertexConsumer, MatrixStack.Entry matrix, int light, float x, int z, int textureU, int textureV) {
        vertexConsumer.vertex(matrix, x - 0.5f, (float)z - 0.25f, 0.0f).color(Colors.WHITE).texture(textureU, textureV).overlay(OverlayTexture.DEFAULT_UV).light(light).normal(matrix, 0.0f, 1.0f, 0.0f);
    }

    @Override
    public Identifier getTexture(DragonFireballEntity arg) {
        return TEXTURE;
    }
}

