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
import net.minecraft.client.model.ModelData;
import net.minecraft.client.model.ModelPart;
import net.minecraft.client.model.ModelPartBuilder;
import net.minecraft.client.model.ModelPartData;
import net.minecraft.client.model.ModelTransform;
import net.minecraft.client.model.TexturedModelData;
import net.minecraft.client.render.Frustum;
import net.minecraft.client.render.OverlayTexture;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.EnderDragonEntityRenderer;
import net.minecraft.client.render.entity.EntityRenderer;
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.client.render.entity.model.EntityModelLayers;
import net.minecraft.client.render.entity.model.EntityModelPartNames;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.decoration.EndCrystalEntity;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RotationAxis;
import org.joml.Quaternionf;

@Environment(value=EnvType.CLIENT)
public class EndCrystalEntityRenderer
extends EntityRenderer<EndCrystalEntity> {
    private static final Identifier TEXTURE = Identifier.ofVanilla("textures/entity/end_crystal/end_crystal.png");
    private static final RenderLayer END_CRYSTAL = RenderLayer.getEntityCutoutNoCull(TEXTURE);
    private static final float SINE_45_DEGREES = (float)Math.sin(0.7853981633974483);
    private static final String GLASS = "glass";
    private static final String BASE = "base";
    private final ModelPart core;
    private final ModelPart frame;
    private final ModelPart bottom;

    public EndCrystalEntityRenderer(EntityRendererFactory.Context arg) {
        super(arg);
        this.shadowRadius = 0.5f;
        ModelPart lv = arg.getPart(EntityModelLayers.END_CRYSTAL);
        this.frame = lv.getChild(GLASS);
        this.core = lv.getChild(EntityModelPartNames.CUBE);
        this.bottom = lv.getChild(BASE);
    }

    public static TexturedModelData getTexturedModelData() {
        ModelData lv = new ModelData();
        ModelPartData lv2 = lv.getRoot();
        lv2.addChild(GLASS, ModelPartBuilder.create().uv(0, 0).cuboid(-4.0f, -4.0f, -4.0f, 8.0f, 8.0f, 8.0f), ModelTransform.NONE);
        lv2.addChild(EntityModelPartNames.CUBE, ModelPartBuilder.create().uv(32, 0).cuboid(-4.0f, -4.0f, -4.0f, 8.0f, 8.0f, 8.0f), ModelTransform.NONE);
        lv2.addChild(BASE, ModelPartBuilder.create().uv(0, 16).cuboid(-6.0f, 0.0f, -6.0f, 12.0f, 4.0f, 12.0f), ModelTransform.NONE);
        return TexturedModelData.of(lv, 64, 32);
    }

    @Override
    public void render(EndCrystalEntity arg, float f, float g, MatrixStack arg2, VertexConsumerProvider arg3, int i) {
        arg2.push();
        float h = EndCrystalEntityRenderer.getYOffset(arg, g);
        float j = ((float)arg.endCrystalAge + g) * 3.0f;
        VertexConsumer lv = arg3.getBuffer(END_CRYSTAL);
        arg2.push();
        arg2.scale(2.0f, 2.0f, 2.0f);
        arg2.translate(0.0f, -0.5f, 0.0f);
        int k = OverlayTexture.DEFAULT_UV;
        if (arg.shouldShowBottom()) {
            this.bottom.render(arg2, lv, i, k);
        }
        arg2.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(j));
        arg2.translate(0.0f, 1.5f + h / 2.0f, 0.0f);
        arg2.multiply(new Quaternionf().setAngleAxis(1.0471976f, SINE_45_DEGREES, 0.0f, SINE_45_DEGREES));
        this.frame.render(arg2, lv, i, k);
        float l = 0.875f;
        arg2.scale(0.875f, 0.875f, 0.875f);
        arg2.multiply(new Quaternionf().setAngleAxis(1.0471976f, SINE_45_DEGREES, 0.0f, SINE_45_DEGREES));
        arg2.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(j));
        this.frame.render(arg2, lv, i, k);
        arg2.scale(0.875f, 0.875f, 0.875f);
        arg2.multiply(new Quaternionf().setAngleAxis(1.0471976f, SINE_45_DEGREES, 0.0f, SINE_45_DEGREES));
        arg2.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(j));
        this.core.render(arg2, lv, i, k);
        arg2.pop();
        arg2.pop();
        BlockPos lv2 = arg.getBeamTarget();
        if (lv2 != null) {
            float m = (float)lv2.getX() + 0.5f;
            float n = (float)lv2.getY() + 0.5f;
            float o = (float)lv2.getZ() + 0.5f;
            float p = (float)((double)m - arg.getX());
            float q = (float)((double)n - arg.getY());
            float r = (float)((double)o - arg.getZ());
            arg2.translate(p, q, r);
            EnderDragonEntityRenderer.renderCrystalBeam(-p, -q + h, -r, g, arg.endCrystalAge, arg2, arg3, i);
        }
        super.render(arg, f, g, arg2, arg3, i);
    }

    public static float getYOffset(EndCrystalEntity crystal, float tickDelta) {
        float g = (float)crystal.endCrystalAge + tickDelta;
        float h = MathHelper.sin(g * 0.2f) / 2.0f + 0.5f;
        h = (h * h + h) * 0.4f;
        return h - 1.4f;
    }

    @Override
    public Identifier getTexture(EndCrystalEntity arg) {
        return TEXTURE;
    }

    @Override
    public boolean shouldRender(EndCrystalEntity arg, Frustum arg2, double d, double e, double f) {
        return super.shouldRender(arg, arg2, d, e, f) || arg.getBeamTarget() != null;
    }
}

