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
import net.minecraft.client.render.entity.feature.PhantomEyesFeatureRenderer;
import net.minecraft.client.render.entity.model.EntityModelLayers;
import net.minecraft.client.render.entity.model.PhantomEntityModel;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.mob.PhantomEntity;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.RotationAxis;

@Environment(value=EnvType.CLIENT)
public class PhantomEntityRenderer
extends MobEntityRenderer<PhantomEntity, PhantomEntityModel<PhantomEntity>> {
    private static final Identifier TEXTURE = Identifier.ofVanilla("textures/entity/phantom.png");

    public PhantomEntityRenderer(EntityRendererFactory.Context arg) {
        super(arg, new PhantomEntityModel(arg.getPart(EntityModelLayers.PHANTOM)), 0.75f);
        this.addFeature(new PhantomEyesFeatureRenderer<PhantomEntity>(this));
    }

    @Override
    public Identifier getTexture(PhantomEntity arg) {
        return TEXTURE;
    }

    @Override
    protected void scale(PhantomEntity arg, MatrixStack arg2, float f) {
        int i = arg.getPhantomSize();
        float g = 1.0f + 0.15f * (float)i;
        arg2.scale(g, g, g);
        arg2.translate(0.0f, 1.3125f, 0.1875f);
    }

    @Override
    protected void setupTransforms(PhantomEntity arg, MatrixStack arg2, float f, float g, float h, float i) {
        super.setupTransforms(arg, arg2, f, g, h, i);
        arg2.multiply(RotationAxis.POSITIVE_X.rotationDegrees(arg.getPitch()));
    }
}

