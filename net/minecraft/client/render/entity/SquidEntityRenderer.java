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
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.client.render.entity.MobEntityRenderer;
import net.minecraft.client.render.entity.model.SquidEntityModel;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.passive.SquidEntity;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RotationAxis;

@Environment(value=EnvType.CLIENT)
public class SquidEntityRenderer<T extends SquidEntity>
extends MobEntityRenderer<T, SquidEntityModel<T>> {
    private static final Identifier TEXTURE = Identifier.ofVanilla("textures/entity/squid/squid.png");

    public SquidEntityRenderer(EntityRendererFactory.Context ctx, SquidEntityModel<T> model) {
        super(ctx, model, 0.7f);
    }

    @Override
    public Identifier getTexture(T arg) {
        return TEXTURE;
    }

    @Override
    protected void setupTransforms(T arg, MatrixStack arg2, float f, float g, float h, float i) {
        float j = MathHelper.lerp(h, ((SquidEntity)arg).prevTiltAngle, ((SquidEntity)arg).tiltAngle);
        float k = MathHelper.lerp(h, ((SquidEntity)arg).prevRollAngle, ((SquidEntity)arg).rollAngle);
        arg2.translate(0.0f, 0.5f, 0.0f);
        arg2.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(180.0f - g));
        arg2.multiply(RotationAxis.POSITIVE_X.rotationDegrees(j));
        arg2.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(k));
        arg2.translate(0.0f, -1.2f, 0.0f);
    }

    @Override
    protected float getAnimationProgress(T arg, float f) {
        return MathHelper.lerp(f, ((SquidEntity)arg).prevTentacleAngle, ((SquidEntity)arg).tentacleAngle);
    }
}

