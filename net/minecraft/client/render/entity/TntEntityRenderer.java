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
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.block.BlockRenderManager;
import net.minecraft.client.render.entity.EntityRenderer;
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.client.render.entity.TntMinecartEntityRenderer;
import net.minecraft.client.texture.SpriteAtlasTexture;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.TntEntity;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RotationAxis;

@Environment(value=EnvType.CLIENT)
public class TntEntityRenderer
extends EntityRenderer<TntEntity> {
    private final BlockRenderManager blockRenderManager;

    public TntEntityRenderer(EntityRendererFactory.Context arg) {
        super(arg);
        this.shadowRadius = 0.5f;
        this.blockRenderManager = arg.getBlockRenderManager();
    }

    @Override
    public void render(TntEntity arg, float f, float g, MatrixStack arg2, VertexConsumerProvider arg3, int i) {
        arg2.push();
        arg2.translate(0.0f, 0.5f, 0.0f);
        int j = arg.getFuse();
        if ((float)j - g + 1.0f < 10.0f) {
            float h = 1.0f - ((float)j - g + 1.0f) / 10.0f;
            h = MathHelper.clamp(h, 0.0f, 1.0f);
            h *= h;
            h *= h;
            float k = 1.0f + h * 0.3f;
            arg2.scale(k, k, k);
        }
        arg2.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(-90.0f));
        arg2.translate(-0.5f, -0.5f, 0.5f);
        arg2.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(90.0f));
        TntMinecartEntityRenderer.renderFlashingBlock(this.blockRenderManager, arg.getBlockState(), arg2, arg3, i, j / 5 % 2 == 0);
        arg2.pop();
        super.render(arg, f, g, arg2, arg3, i);
    }

    @Override
    public Identifier getTexture(TntEntity arg) {
        return SpriteAtlasTexture.BLOCK_ATLAS_TEXTURE;
    }
}

