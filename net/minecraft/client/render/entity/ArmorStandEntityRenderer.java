/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 * 
 * Could not load the following classes:
 *  net.fabricmc.api.EnvType
 *  net.fabricmc.api.Environment
 *  org.jetbrains.annotations.Nullable
 */
package net.minecraft.client.render.entity;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.client.render.entity.LivingEntityRenderer;
import net.minecraft.client.render.entity.feature.ArmorFeatureRenderer;
import net.minecraft.client.render.entity.feature.ElytraFeatureRenderer;
import net.minecraft.client.render.entity.feature.HeadFeatureRenderer;
import net.minecraft.client.render.entity.feature.HeldItemFeatureRenderer;
import net.minecraft.client.render.entity.model.ArmorStandArmorEntityModel;
import net.minecraft.client.render.entity.model.ArmorStandEntityModel;
import net.minecraft.client.render.entity.model.EntityModelLayers;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.decoration.ArmorStandEntity;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RotationAxis;
import org.jetbrains.annotations.Nullable;

@Environment(value=EnvType.CLIENT)
public class ArmorStandEntityRenderer
extends LivingEntityRenderer<ArmorStandEntity, ArmorStandArmorEntityModel> {
    public static final Identifier TEXTURE = Identifier.ofVanilla("textures/entity/armorstand/wood.png");

    public ArmorStandEntityRenderer(EntityRendererFactory.Context arg) {
        super(arg, new ArmorStandEntityModel(arg.getPart(EntityModelLayers.ARMOR_STAND)), 0.0f);
        this.addFeature(new ArmorFeatureRenderer<ArmorStandEntity, ArmorStandArmorEntityModel, ArmorStandArmorEntityModel>(this, new ArmorStandArmorEntityModel(arg.getPart(EntityModelLayers.ARMOR_STAND_INNER_ARMOR)), new ArmorStandArmorEntityModel(arg.getPart(EntityModelLayers.ARMOR_STAND_OUTER_ARMOR)), arg.getModelManager()));
        this.addFeature(new HeldItemFeatureRenderer<ArmorStandEntity, ArmorStandArmorEntityModel>(this, arg.getHeldItemRenderer()));
        this.addFeature(new ElytraFeatureRenderer<ArmorStandEntity, ArmorStandArmorEntityModel>(this, arg.getModelLoader()));
        this.addFeature(new HeadFeatureRenderer<ArmorStandEntity, ArmorStandArmorEntityModel>(this, arg.getModelLoader(), arg.getHeldItemRenderer()));
    }

    @Override
    public Identifier getTexture(ArmorStandEntity arg) {
        return TEXTURE;
    }

    @Override
    protected void setupTransforms(ArmorStandEntity arg, MatrixStack arg2, float f, float g, float h, float i) {
        arg2.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(180.0f - g));
        float j = (float)(arg.getWorld().getTime() - arg.lastHitTime) + h;
        if (j < 5.0f) {
            arg2.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(MathHelper.sin(j / 1.5f * (float)Math.PI) * 3.0f));
        }
    }

    @Override
    protected boolean hasLabel(ArmorStandEntity arg) {
        float f;
        double d = this.dispatcher.getSquaredDistanceToCamera(arg);
        float f2 = f = arg.isInSneakingPose() ? 32.0f : 64.0f;
        if (d >= (double)(f * f)) {
            return false;
        }
        return arg.isCustomNameVisible();
    }

    @Override
    @Nullable
    protected RenderLayer getRenderLayer(ArmorStandEntity arg, boolean bl, boolean bl2, boolean bl3) {
        if (!arg.isMarker()) {
            return super.getRenderLayer(arg, bl, bl2, bl3);
        }
        Identifier lv = this.getTexture(arg);
        if (bl2) {
            return RenderLayer.getEntityTranslucent(lv, false);
        }
        if (bl) {
            return RenderLayer.getEntityCutoutNoCull(lv, false);
        }
        return null;
    }
}

