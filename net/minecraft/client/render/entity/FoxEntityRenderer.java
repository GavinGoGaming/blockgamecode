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
import net.minecraft.client.render.entity.feature.FoxHeldItemFeatureRenderer;
import net.minecraft.client.render.entity.model.EntityModelLayers;
import net.minecraft.client.render.entity.model.FoxEntityModel;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.passive.FoxEntity;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RotationAxis;

@Environment(value=EnvType.CLIENT)
public class FoxEntityRenderer
extends MobEntityRenderer<FoxEntity, FoxEntityModel<FoxEntity>> {
    private static final Identifier TEXTURE = Identifier.ofVanilla("textures/entity/fox/fox.png");
    private static final Identifier SLEEPING_TEXTURE = Identifier.ofVanilla("textures/entity/fox/fox_sleep.png");
    private static final Identifier SNOW_TEXTURE = Identifier.ofVanilla("textures/entity/fox/snow_fox.png");
    private static final Identifier SLEEPING_SNOW_TEXTURE = Identifier.ofVanilla("textures/entity/fox/snow_fox_sleep.png");

    public FoxEntityRenderer(EntityRendererFactory.Context arg) {
        super(arg, new FoxEntityModel(arg.getPart(EntityModelLayers.FOX)), 0.4f);
        this.addFeature(new FoxHeldItemFeatureRenderer(this, arg.getHeldItemRenderer()));
    }

    @Override
    protected void setupTransforms(FoxEntity arg, MatrixStack arg2, float f, float g, float h, float i) {
        super.setupTransforms(arg, arg2, f, g, h, i);
        if (arg.isChasing() || arg.isWalking()) {
            float j = -MathHelper.lerp(h, arg.prevPitch, arg.getPitch());
            arg2.multiply(RotationAxis.POSITIVE_X.rotationDegrees(j));
        }
    }

    @Override
    public Identifier getTexture(FoxEntity arg) {
        if (arg.getVariant() == FoxEntity.Type.RED) {
            return arg.isSleeping() ? SLEEPING_TEXTURE : TEXTURE;
        }
        return arg.isSleeping() ? SLEEPING_SNOW_TEXTURE : SNOW_TEXTURE;
    }
}

