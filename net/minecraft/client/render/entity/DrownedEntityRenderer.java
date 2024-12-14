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
import net.minecraft.client.render.entity.ZombieBaseEntityRenderer;
import net.minecraft.client.render.entity.feature.DrownedOverlayFeatureRenderer;
import net.minecraft.client.render.entity.model.DrownedEntityModel;
import net.minecraft.client.render.entity.model.EntityModelLayers;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.mob.DrownedEntity;
import net.minecraft.entity.mob.ZombieEntity;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RotationAxis;

@Environment(value=EnvType.CLIENT)
public class DrownedEntityRenderer
extends ZombieBaseEntityRenderer<DrownedEntity, DrownedEntityModel<DrownedEntity>> {
    private static final Identifier TEXTURE = Identifier.ofVanilla("textures/entity/zombie/drowned.png");

    public DrownedEntityRenderer(EntityRendererFactory.Context arg) {
        super(arg, new DrownedEntityModel(arg.getPart(EntityModelLayers.DROWNED)), new DrownedEntityModel(arg.getPart(EntityModelLayers.DROWNED_INNER_ARMOR)), new DrownedEntityModel(arg.getPart(EntityModelLayers.DROWNED_OUTER_ARMOR)));
        this.addFeature(new DrownedOverlayFeatureRenderer<DrownedEntity>(this, arg.getModelLoader()));
    }

    @Override
    public Identifier getTexture(ZombieEntity arg) {
        return TEXTURE;
    }

    @Override
    protected void setupTransforms(DrownedEntity arg, MatrixStack arg2, float f, float g, float h, float i) {
        super.setupTransforms(arg, arg2, f, g, h, i);
        float j = arg.getLeaningPitch(h);
        if (j > 0.0f) {
            float k = -10.0f - arg.getPitch();
            float l = MathHelper.lerp(j, 0.0f, k);
            arg2.multiply(RotationAxis.POSITIVE_X.rotationDegrees(l), 0.0f, arg.getHeight() / 2.0f / i, 0.0f);
        }
    }
}

