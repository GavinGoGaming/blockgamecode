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
import net.minecraft.client.render.entity.model.EntityModelLayers;
import net.minecraft.client.render.entity.model.SalmonEntityModel;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.passive.SalmonEntity;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RotationAxis;

@Environment(value=EnvType.CLIENT)
public class SalmonEntityRenderer
extends MobEntityRenderer<SalmonEntity, SalmonEntityModel<SalmonEntity>> {
    private static final Identifier TEXTURE = Identifier.ofVanilla("textures/entity/fish/salmon.png");

    public SalmonEntityRenderer(EntityRendererFactory.Context arg) {
        super(arg, new SalmonEntityModel(arg.getPart(EntityModelLayers.SALMON)), 0.4f);
    }

    @Override
    public Identifier getTexture(SalmonEntity arg) {
        return TEXTURE;
    }

    @Override
    protected void setupTransforms(SalmonEntity arg, MatrixStack arg2, float f, float g, float h, float i) {
        super.setupTransforms(arg, arg2, f, g, h, i);
        float j = 1.0f;
        float k = 1.0f;
        if (!arg.isTouchingWater()) {
            j = 1.3f;
            k = 1.7f;
        }
        float l = j * 4.3f * MathHelper.sin(k * 0.6f * f);
        arg2.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(l));
        arg2.translate(0.0f, 0.0f, -0.4f);
        if (!arg.isTouchingWater()) {
            arg2.translate(0.2f, 0.1f, 0.0f);
            arg2.multiply(RotationAxis.POSITIVE_Z.rotationDegrees(90.0f));
        }
    }
}

