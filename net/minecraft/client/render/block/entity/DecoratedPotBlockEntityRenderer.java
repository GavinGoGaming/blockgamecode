/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 * 
 * Could not load the following classes:
 *  net.fabricmc.api.EnvType
 *  net.fabricmc.api.Environment
 */
package net.minecraft.client.render.block.entity;

import java.util.EnumSet;
import java.util.Optional;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.block.DecoratedPotPatterns;
import net.minecraft.block.entity.DecoratedPotBlockEntity;
import net.minecraft.block.entity.Sherds;
import net.minecraft.client.model.Dilation;
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
import net.minecraft.client.render.entity.model.EntityModelPartNames;
import net.minecraft.client.util.SpriteIdentifier;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.Item;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RotationAxis;

@Environment(value=EnvType.CLIENT)
public class DecoratedPotBlockEntityRenderer
implements BlockEntityRenderer<DecoratedPotBlockEntity> {
    private static final String NECK = "neck";
    private static final String FRONT = "front";
    private static final String BACK = "back";
    private static final String LEFT = "left";
    private static final String RIGHT = "right";
    private static final String TOP = "top";
    private static final String BOTTOM = "bottom";
    private final ModelPart neck;
    private final ModelPart front;
    private final ModelPart back;
    private final ModelPart left;
    private final ModelPart right;
    private final ModelPart top;
    private final ModelPart bottom;
    private static final float field_46728 = 0.125f;

    public DecoratedPotBlockEntityRenderer(BlockEntityRendererFactory.Context context) {
        ModelPart lv = context.getLayerModelPart(EntityModelLayers.DECORATED_POT_BASE);
        this.neck = lv.getChild(EntityModelPartNames.NECK);
        this.top = lv.getChild(TOP);
        this.bottom = lv.getChild(BOTTOM);
        ModelPart lv2 = context.getLayerModelPart(EntityModelLayers.DECORATED_POT_SIDES);
        this.front = lv2.getChild(FRONT);
        this.back = lv2.getChild(BACK);
        this.left = lv2.getChild(LEFT);
        this.right = lv2.getChild(RIGHT);
    }

    public static TexturedModelData getTopBottomNeckTexturedModelData() {
        ModelData lv = new ModelData();
        ModelPartData lv2 = lv.getRoot();
        Dilation lv3 = new Dilation(0.2f);
        Dilation lv4 = new Dilation(-0.1f);
        lv2.addChild(EntityModelPartNames.NECK, ModelPartBuilder.create().uv(0, 0).cuboid(4.0f, 17.0f, 4.0f, 8.0f, 3.0f, 8.0f, lv4).uv(0, 5).cuboid(5.0f, 20.0f, 5.0f, 6.0f, 1.0f, 6.0f, lv3), ModelTransform.of(0.0f, 37.0f, 16.0f, (float)Math.PI, 0.0f, 0.0f));
        ModelPartBuilder lv5 = ModelPartBuilder.create().uv(-14, 13).cuboid(0.0f, 0.0f, 0.0f, 14.0f, 0.0f, 14.0f);
        lv2.addChild(TOP, lv5, ModelTransform.of(1.0f, 16.0f, 1.0f, 0.0f, 0.0f, 0.0f));
        lv2.addChild(BOTTOM, lv5, ModelTransform.of(1.0f, 0.0f, 1.0f, 0.0f, 0.0f, 0.0f));
        return TexturedModelData.of(lv, 32, 32);
    }

    public static TexturedModelData getSidesTexturedModelData() {
        ModelData lv = new ModelData();
        ModelPartData lv2 = lv.getRoot();
        ModelPartBuilder lv3 = ModelPartBuilder.create().uv(1, 0).cuboid(0.0f, 0.0f, 0.0f, 14.0f, 16.0f, 0.0f, EnumSet.of(Direction.NORTH));
        lv2.addChild(BACK, lv3, ModelTransform.of(15.0f, 16.0f, 1.0f, 0.0f, 0.0f, (float)Math.PI));
        lv2.addChild(LEFT, lv3, ModelTransform.of(1.0f, 16.0f, 1.0f, 0.0f, -1.5707964f, (float)Math.PI));
        lv2.addChild(RIGHT, lv3, ModelTransform.of(15.0f, 16.0f, 15.0f, 0.0f, 1.5707964f, (float)Math.PI));
        lv2.addChild(FRONT, lv3, ModelTransform.of(1.0f, 16.0f, 15.0f, (float)Math.PI, 0.0f, 0.0f));
        return TexturedModelData.of(lv, 16, 16);
    }

    private static SpriteIdentifier getTextureIdFromSherd(Optional<Item> sherd) {
        SpriteIdentifier lv;
        if (sherd.isPresent() && (lv = TexturedRenderLayers.getDecoratedPotPatternTextureId(DecoratedPotPatterns.fromSherd(sherd.get()))) != null) {
            return lv;
        }
        return TexturedRenderLayers.DECORATED_POT_SIDE;
    }

    @Override
    public void render(DecoratedPotBlockEntity arg, float f, MatrixStack arg2, VertexConsumerProvider arg3, int i, int j) {
        float g;
        arg2.push();
        Direction lv = arg.getHorizontalFacing();
        arg2.translate(0.5, 0.0, 0.5);
        arg2.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(180.0f - lv.asRotation()));
        arg2.translate(-0.5, 0.0, -0.5);
        DecoratedPotBlockEntity.WobbleType lv2 = arg.lastWobbleType;
        if (lv2 != null && arg.getWorld() != null && (g = ((float)(arg.getWorld().getTime() - arg.lastWobbleTime) + f) / (float)lv2.lengthInTicks) >= 0.0f && g <= 1.0f) {
            if (lv2 == DecoratedPotBlockEntity.WobbleType.POSITIVE) {
                h = 0.015625f;
                float k = g * ((float)Math.PI * 2);
                float l = -1.5f * (MathHelper.cos(k) + 0.5f) * MathHelper.sin(k / 2.0f);
                arg2.multiply(RotationAxis.POSITIVE_X.rotation(l * 0.015625f), 0.5f, 0.0f, 0.5f);
                float m = MathHelper.sin(k);
                arg2.multiply(RotationAxis.POSITIVE_Z.rotation(m * 0.015625f), 0.5f, 0.0f, 0.5f);
            } else {
                h = MathHelper.sin(-g * 3.0f * (float)Math.PI) * 0.125f;
                float k = 1.0f - g;
                arg2.multiply(RotationAxis.POSITIVE_Y.rotation(h * k), 0.5f, 0.0f, 0.5f);
            }
        }
        VertexConsumer lv3 = TexturedRenderLayers.DECORATED_POT_BASE.getVertexConsumer(arg3, RenderLayer::getEntitySolid);
        this.neck.render(arg2, lv3, i, j);
        this.top.render(arg2, lv3, i, j);
        this.bottom.render(arg2, lv3, i, j);
        Sherds lv4 = arg.getSherds();
        this.renderDecoratedSide(this.front, arg2, arg3, i, j, DecoratedPotBlockEntityRenderer.getTextureIdFromSherd(lv4.front()));
        this.renderDecoratedSide(this.back, arg2, arg3, i, j, DecoratedPotBlockEntityRenderer.getTextureIdFromSherd(lv4.back()));
        this.renderDecoratedSide(this.left, arg2, arg3, i, j, DecoratedPotBlockEntityRenderer.getTextureIdFromSherd(lv4.left()));
        this.renderDecoratedSide(this.right, arg2, arg3, i, j, DecoratedPotBlockEntityRenderer.getTextureIdFromSherd(lv4.right()));
        arg2.pop();
    }

    private void renderDecoratedSide(ModelPart part, MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light, int overlay, SpriteIdentifier textureId) {
        part.render(matrices, textureId.getVertexConsumer(vertexConsumers, RenderLayer::getEntitySolid), light, overlay);
    }
}

