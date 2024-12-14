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
import net.minecraft.block.BannerBlock;
import net.minecraft.block.BlockState;
import net.minecraft.block.WallBannerBlock;
import net.minecraft.block.entity.BannerBlockEntity;
import net.minecraft.client.model.ModelData;
import net.minecraft.client.model.ModelPart;
import net.minecraft.client.model.ModelPartBuilder;
import net.minecraft.client.model.ModelPartData;
import net.minecraft.client.model.ModelTransform;
import net.minecraft.client.model.TexturedModelData;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.TexturedRenderLayers;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.block.entity.BlockEntityRenderer;
import net.minecraft.client.render.block.entity.BlockEntityRendererFactory;
import net.minecraft.client.render.entity.model.EntityModelLayers;
import net.minecraft.client.render.model.ModelLoader;
import net.minecraft.client.util.SpriteIdentifier;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.component.type.BannerPatternsComponent;
import net.minecraft.util.DyeColor;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RotationAxis;
import net.minecraft.util.math.RotationPropertyHelper;

@Environment(value=EnvType.CLIENT)
public class BannerBlockEntityRenderer
implements BlockEntityRenderer<BannerBlockEntity> {
    private static final int WIDTH = 20;
    private static final int HEIGHT = 40;
    private static final int ROTATIONS = 16;
    public static final String BANNER = "flag";
    private static final String PILLAR = "pole";
    private static final String CROSSBAR = "bar";
    private final ModelPart banner;
    private final ModelPart pillar;
    private final ModelPart crossbar;

    public BannerBlockEntityRenderer(BlockEntityRendererFactory.Context ctx) {
        ModelPart lv = ctx.getLayerModelPart(EntityModelLayers.BANNER);
        this.banner = lv.getChild(BANNER);
        this.pillar = lv.getChild(PILLAR);
        this.crossbar = lv.getChild(CROSSBAR);
    }

    public static TexturedModelData getTexturedModelData() {
        ModelData lv = new ModelData();
        ModelPartData lv2 = lv.getRoot();
        lv2.addChild(BANNER, ModelPartBuilder.create().uv(0, 0).cuboid(-10.0f, 0.0f, -2.0f, 20.0f, 40.0f, 1.0f), ModelTransform.NONE);
        lv2.addChild(PILLAR, ModelPartBuilder.create().uv(44, 0).cuboid(-1.0f, -30.0f, -1.0f, 2.0f, 42.0f, 2.0f), ModelTransform.NONE);
        lv2.addChild(CROSSBAR, ModelPartBuilder.create().uv(0, 42).cuboid(-10.0f, -32.0f, -1.0f, 20.0f, 2.0f, 2.0f), ModelTransform.NONE);
        return TexturedModelData.of(lv, 64, 64);
    }

    @Override
    public void render(BannerBlockEntity arg, float f, MatrixStack arg2, VertexConsumerProvider arg3, int i, int j) {
        long l;
        float g = 0.6666667f;
        boolean bl = arg.getWorld() == null;
        arg2.push();
        if (bl) {
            l = 0L;
            arg2.translate(0.5f, 0.5f, 0.5f);
            this.pillar.visible = true;
        } else {
            l = arg.getWorld().getTime();
            BlockState lv = arg.getCachedState();
            if (lv.getBlock() instanceof BannerBlock) {
                arg2.translate(0.5f, 0.5f, 0.5f);
                h = -RotationPropertyHelper.toDegrees(lv.get(BannerBlock.ROTATION));
                arg2.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(h));
                this.pillar.visible = true;
            } else {
                arg2.translate(0.5f, -0.16666667f, 0.5f);
                h = -lv.get(WallBannerBlock.FACING).asRotation();
                arg2.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(h));
                arg2.translate(0.0f, -0.3125f, -0.4375f);
                this.pillar.visible = false;
            }
        }
        arg2.push();
        arg2.scale(0.6666667f, -0.6666667f, -0.6666667f);
        VertexConsumer lv2 = ModelLoader.BANNER_BASE.getVertexConsumer(arg3, RenderLayer::getEntitySolid);
        this.pillar.render(arg2, lv2, i, j);
        this.crossbar.render(arg2, lv2, i, j);
        BlockPos lv3 = arg.getPos();
        float k = ((float)Math.floorMod((long)(lv3.getX() * 7 + lv3.getY() * 9 + lv3.getZ() * 13) + l, 100L) + f) / 100.0f;
        this.banner.pitch = (-0.0125f + 0.01f * MathHelper.cos((float)Math.PI * 2 * k)) * (float)Math.PI;
        this.banner.pivotY = -32.0f;
        BannerBlockEntityRenderer.renderCanvas(arg2, arg3, i, j, this.banner, ModelLoader.BANNER_BASE, true, arg.getColorForState(), arg.getPatterns());
        arg2.pop();
        arg2.pop();
    }

    public static void renderCanvas(MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light, int overlay, ModelPart canvas, SpriteIdentifier baseSprite, boolean isBanner, DyeColor color, BannerPatternsComponent patterns) {
        BannerBlockEntityRenderer.renderCanvas(matrices, vertexConsumers, light, overlay, canvas, baseSprite, isBanner, color, patterns, false);
    }

    public static void renderCanvas(MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light, int overlay, ModelPart canvas, SpriteIdentifier baseSprite, boolean isBanner, DyeColor color, BannerPatternsComponent patterns, boolean glint) {
        canvas.render(matrices, baseSprite.getVertexConsumer(vertexConsumers, RenderLayer::getEntitySolid, glint), light, overlay);
        BannerBlockEntityRenderer.renderLayer(matrices, vertexConsumers, light, overlay, canvas, isBanner ? TexturedRenderLayers.BANNER_BASE : TexturedRenderLayers.SHIELD_BASE, color);
        for (int k = 0; k < 16 && k < patterns.layers().size(); ++k) {
            BannerPatternsComponent.Layer lv = patterns.layers().get(k);
            SpriteIdentifier lv2 = isBanner ? TexturedRenderLayers.getBannerPatternTextureId(lv.pattern()) : TexturedRenderLayers.getShieldPatternTextureId(lv.pattern());
            BannerBlockEntityRenderer.renderLayer(matrices, vertexConsumers, light, overlay, canvas, lv2, lv.color());
        }
    }

    private static void renderLayer(MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light, int overlay, ModelPart canvas, SpriteIdentifier textureId, DyeColor color) {
        int k = color.getEntityColor();
        canvas.render(matrices, textureId.getVertexConsumer(vertexConsumers, RenderLayer::getEntityNoOutline), light, overlay, k);
    }
}

