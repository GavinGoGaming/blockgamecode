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
import net.minecraft.block.entity.ConduitBlockEntity;
import net.minecraft.client.model.Dilation;
import net.minecraft.client.model.ModelData;
import net.minecraft.client.model.ModelPart;
import net.minecraft.client.model.ModelPartBuilder;
import net.minecraft.client.model.ModelPartData;
import net.minecraft.client.model.ModelTransform;
import net.minecraft.client.model.TexturedModelData;
import net.minecraft.client.render.Camera;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.block.entity.BlockEntityRenderDispatcher;
import net.minecraft.client.render.block.entity.BlockEntityRenderer;
import net.minecraft.client.render.block.entity.BlockEntityRendererFactory;
import net.minecraft.client.render.entity.model.EntityModelLayers;
import net.minecraft.client.texture.SpriteAtlasTexture;
import net.minecraft.client.util.SpriteIdentifier;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.MathHelper;
import org.joml.Quaternionf;
import org.joml.Vector3f;

@Environment(value=EnvType.CLIENT)
public class ConduitBlockEntityRenderer
implements BlockEntityRenderer<ConduitBlockEntity> {
    public static final SpriteIdentifier BASE_TEXTURE = new SpriteIdentifier(SpriteAtlasTexture.BLOCK_ATLAS_TEXTURE, Identifier.ofVanilla("entity/conduit/base"));
    public static final SpriteIdentifier CAGE_TEXTURE = new SpriteIdentifier(SpriteAtlasTexture.BLOCK_ATLAS_TEXTURE, Identifier.ofVanilla("entity/conduit/cage"));
    public static final SpriteIdentifier WIND_TEXTURE = new SpriteIdentifier(SpriteAtlasTexture.BLOCK_ATLAS_TEXTURE, Identifier.ofVanilla("entity/conduit/wind"));
    public static final SpriteIdentifier WIND_VERTICAL_TEXTURE = new SpriteIdentifier(SpriteAtlasTexture.BLOCK_ATLAS_TEXTURE, Identifier.ofVanilla("entity/conduit/wind_vertical"));
    public static final SpriteIdentifier OPEN_EYE_TEXTURE = new SpriteIdentifier(SpriteAtlasTexture.BLOCK_ATLAS_TEXTURE, Identifier.ofVanilla("entity/conduit/open_eye"));
    public static final SpriteIdentifier CLOSED_EYE_TEXTURE = new SpriteIdentifier(SpriteAtlasTexture.BLOCK_ATLAS_TEXTURE, Identifier.ofVanilla("entity/conduit/closed_eye"));
    private final ModelPart conduitEye;
    private final ModelPart conduitWind;
    private final ModelPart conduitShell;
    private final ModelPart conduit;
    private final BlockEntityRenderDispatcher dispatcher;

    public ConduitBlockEntityRenderer(BlockEntityRendererFactory.Context ctx) {
        this.dispatcher = ctx.getRenderDispatcher();
        this.conduitEye = ctx.getLayerModelPart(EntityModelLayers.CONDUIT_EYE);
        this.conduitWind = ctx.getLayerModelPart(EntityModelLayers.CONDUIT_WIND);
        this.conduitShell = ctx.getLayerModelPart(EntityModelLayers.CONDUIT_SHELL);
        this.conduit = ctx.getLayerModelPart(EntityModelLayers.CONDUIT);
    }

    public static TexturedModelData getEyeTexturedModelData() {
        ModelData lv = new ModelData();
        ModelPartData lv2 = lv.getRoot();
        lv2.addChild("eye", ModelPartBuilder.create().uv(0, 0).cuboid(-4.0f, -4.0f, 0.0f, 8.0f, 8.0f, 0.0f, new Dilation(0.01f)), ModelTransform.NONE);
        return TexturedModelData.of(lv, 16, 16);
    }

    public static TexturedModelData getWindTexturedModelData() {
        ModelData lv = new ModelData();
        ModelPartData lv2 = lv.getRoot();
        lv2.addChild("wind", ModelPartBuilder.create().uv(0, 0).cuboid(-8.0f, -8.0f, -8.0f, 16.0f, 16.0f, 16.0f), ModelTransform.NONE);
        return TexturedModelData.of(lv, 64, 32);
    }

    public static TexturedModelData getShellTexturedModelData() {
        ModelData lv = new ModelData();
        ModelPartData lv2 = lv.getRoot();
        lv2.addChild("shell", ModelPartBuilder.create().uv(0, 0).cuboid(-3.0f, -3.0f, -3.0f, 6.0f, 6.0f, 6.0f), ModelTransform.NONE);
        return TexturedModelData.of(lv, 32, 16);
    }

    public static TexturedModelData getPlainTexturedModelData() {
        ModelData lv = new ModelData();
        ModelPartData lv2 = lv.getRoot();
        lv2.addChild("shell", ModelPartBuilder.create().uv(0, 0).cuboid(-4.0f, -4.0f, -4.0f, 8.0f, 8.0f, 8.0f), ModelTransform.NONE);
        return TexturedModelData.of(lv, 32, 16);
    }

    @Override
    public void render(ConduitBlockEntity arg, float f, MatrixStack arg2, VertexConsumerProvider arg3, int i, int j) {
        float g = (float)arg.ticks + f;
        if (!arg.isActive()) {
            float h = arg.getRotation(0.0f);
            VertexConsumer lv = BASE_TEXTURE.getVertexConsumer(arg3, RenderLayer::getEntitySolid);
            arg2.push();
            arg2.translate(0.5f, 0.5f, 0.5f);
            arg2.multiply(new Quaternionf().rotationY(h * ((float)Math.PI / 180)));
            this.conduitShell.render(arg2, lv, i, j);
            arg2.pop();
            return;
        }
        float h = arg.getRotation(f) * 57.295776f;
        float k = MathHelper.sin(g * 0.1f) / 2.0f + 0.5f;
        k = k * k + k;
        arg2.push();
        arg2.translate(0.5f, 0.3f + k * 0.2f, 0.5f);
        Vector3f vector3f = new Vector3f(0.5f, 1.0f, 0.5f).normalize();
        arg2.multiply(new Quaternionf().rotationAxis(h * ((float)Math.PI / 180), vector3f));
        this.conduit.render(arg2, CAGE_TEXTURE.getVertexConsumer(arg3, RenderLayer::getEntityCutoutNoCull), i, j);
        arg2.pop();
        int l = arg.ticks / 66 % 3;
        arg2.push();
        arg2.translate(0.5f, 0.5f, 0.5f);
        if (l == 1) {
            arg2.multiply(new Quaternionf().rotationX(1.5707964f));
        } else if (l == 2) {
            arg2.multiply(new Quaternionf().rotationZ(1.5707964f));
        }
        VertexConsumer lv2 = (l == 1 ? WIND_VERTICAL_TEXTURE : WIND_TEXTURE).getVertexConsumer(arg3, RenderLayer::getEntityCutoutNoCull);
        this.conduitWind.render(arg2, lv2, i, j);
        arg2.pop();
        arg2.push();
        arg2.translate(0.5f, 0.5f, 0.5f);
        arg2.scale(0.875f, 0.875f, 0.875f);
        arg2.multiply(new Quaternionf().rotationXYZ((float)Math.PI, 0.0f, (float)Math.PI));
        this.conduitWind.render(arg2, lv2, i, j);
        arg2.pop();
        Camera lv3 = this.dispatcher.camera;
        arg2.push();
        arg2.translate(0.5f, 0.3f + k * 0.2f, 0.5f);
        arg2.scale(0.5f, 0.5f, 0.5f);
        float m = -lv3.getYaw();
        arg2.multiply(new Quaternionf().rotationYXZ(m * ((float)Math.PI / 180), lv3.getPitch() * ((float)Math.PI / 180), (float)Math.PI));
        float n = 1.3333334f;
        arg2.scale(1.3333334f, 1.3333334f, 1.3333334f);
        this.conduitEye.render(arg2, (arg.isEyeOpen() ? OPEN_EYE_TEXTURE : CLOSED_EYE_TEXTURE).getVertexConsumer(arg3, RenderLayer::getEntityCutoutNoCull), i, j);
        arg2.pop();
    }
}

