/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 * 
 * Could not load the following classes:
 *  net.fabricmc.api.EnvType
 *  net.fabricmc.api.Environment
 */
package net.minecraft.client.render.entity;

import java.util.List;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.client.render.entity.MobEntityRenderer;
import net.minecraft.client.render.entity.feature.CatCollarFeatureRenderer;
import net.minecraft.client.render.entity.model.CatEntityModel;
import net.minecraft.client.render.entity.model.EntityModelLayers;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.passive.CatEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RotationAxis;

@Environment(value=EnvType.CLIENT)
public class CatEntityRenderer
extends MobEntityRenderer<CatEntity, CatEntityModel<CatEntity>> {
    public CatEntityRenderer(EntityRendererFactory.Context arg) {
        super(arg, new CatEntityModel(arg.getPart(EntityModelLayers.CAT)), 0.4f);
        this.addFeature(new CatCollarFeatureRenderer(this, arg.getModelLoader()));
    }

    @Override
    public Identifier getTexture(CatEntity arg) {
        return arg.getTexture();
    }

    @Override
    protected void scale(CatEntity arg, MatrixStack arg2, float f) {
        super.scale(arg, arg2, f);
        arg2.scale(0.8f, 0.8f, 0.8f);
    }

    @Override
    protected void setupTransforms(CatEntity arg, MatrixStack arg2, float f, float g, float h, float i) {
        super.setupTransforms(arg, arg2, f, g, h, i);
        float j = arg.getSleepAnimation(h);
        if (j > 0.0f) {
            arg2.translate(0.4f * j, 0.15f * j, 0.1f * j);
            arg2.multiply(RotationAxis.POSITIVE_Z.rotationDegrees(MathHelper.lerpAngleDegrees(j, 0.0f, 90.0f)));
            BlockPos lv = arg.getBlockPos();
            List<PlayerEntity> list = arg.getWorld().getNonSpectatingEntities(PlayerEntity.class, new Box(lv).expand(2.0, 2.0, 2.0));
            for (PlayerEntity lv2 : list) {
                if (!lv2.isSleeping()) continue;
                arg2.translate(0.15f * j, 0.0f, 0.0f);
                break;
            }
        }
    }
}

