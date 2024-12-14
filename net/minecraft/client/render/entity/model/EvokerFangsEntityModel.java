/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 * 
 * Could not load the following classes:
 *  net.fabricmc.api.EnvType
 *  net.fabricmc.api.Environment
 */
package net.minecraft.client.render.entity.model;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.model.ModelData;
import net.minecraft.client.model.ModelPart;
import net.minecraft.client.model.ModelPartBuilder;
import net.minecraft.client.model.ModelPartData;
import net.minecraft.client.model.ModelTransform;
import net.minecraft.client.model.TexturedModelData;
import net.minecraft.client.render.entity.model.SinglePartEntityModel;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.MathHelper;

@Environment(value=EnvType.CLIENT)
public class EvokerFangsEntityModel<T extends Entity>
extends SinglePartEntityModel<T> {
    private static final String BASE = "base";
    private static final String UPPER_JAW = "upper_jaw";
    private static final String LOWER_JAW = "lower_jaw";
    private final ModelPart root;
    private final ModelPart base;
    private final ModelPart upperJaw;
    private final ModelPart lowerJaw;

    public EvokerFangsEntityModel(ModelPart root) {
        this.root = root;
        this.base = root.getChild(BASE);
        this.upperJaw = root.getChild(UPPER_JAW);
        this.lowerJaw = root.getChild(LOWER_JAW);
    }

    public static TexturedModelData getTexturedModelData() {
        ModelData lv = new ModelData();
        ModelPartData lv2 = lv.getRoot();
        lv2.addChild(BASE, ModelPartBuilder.create().uv(0, 0).cuboid(0.0f, 0.0f, 0.0f, 10.0f, 12.0f, 10.0f), ModelTransform.pivot(-5.0f, 24.0f, -5.0f));
        ModelPartBuilder lv3 = ModelPartBuilder.create().uv(40, 0).cuboid(0.0f, 0.0f, 0.0f, 4.0f, 14.0f, 8.0f);
        lv2.addChild(UPPER_JAW, lv3, ModelTransform.pivot(1.5f, 24.0f, -4.0f));
        lv2.addChild(LOWER_JAW, lv3, ModelTransform.of(-1.5f, 24.0f, 4.0f, 0.0f, (float)Math.PI, 0.0f));
        return TexturedModelData.of(lv, 64, 32);
    }

    @Override
    public void setAngles(T entity, float limbAngle, float limbDistance, float animationProgress, float headYaw, float headPitch) {
        float k = limbAngle * 2.0f;
        if (k > 1.0f) {
            k = 1.0f;
        }
        k = 1.0f - k * k * k;
        this.upperJaw.roll = (float)Math.PI - k * 0.35f * (float)Math.PI;
        this.lowerJaw.roll = (float)Math.PI + k * 0.35f * (float)Math.PI;
        float l = (limbAngle + MathHelper.sin(limbAngle * 2.7f)) * 0.6f * 12.0f;
        this.lowerJaw.pivotY = this.upperJaw.pivotY = 24.0f - l;
        this.base.pivotY = this.upperJaw.pivotY;
    }

    @Override
    public ModelPart getPart() {
        return this.root;
    }
}

