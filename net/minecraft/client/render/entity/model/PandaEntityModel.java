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
import net.minecraft.client.model.ModelUtil;
import net.minecraft.client.model.TexturedModelData;
import net.minecraft.client.render.entity.model.EntityModelPartNames;
import net.minecraft.client.render.entity.model.QuadrupedEntityModel;
import net.minecraft.entity.passive.PandaEntity;
import net.minecraft.entity.passive.PassiveEntity;
import net.minecraft.util.math.MathHelper;

@Environment(value=EnvType.CLIENT)
public class PandaEntityModel<T extends PandaEntity>
extends QuadrupedEntityModel<T> {
    private float sittingAnimationProgress;
    private float lieOnBackAnimationProgress;
    private float playAnimationProgress;

    public PandaEntityModel(ModelPart root) {
        super(root, true, 23.0f, 4.8f, 2.7f, 3.0f, 49);
    }

    public static TexturedModelData getTexturedModelData() {
        ModelData lv = new ModelData();
        ModelPartData lv2 = lv.getRoot();
        lv2.addChild(EntityModelPartNames.HEAD, ModelPartBuilder.create().uv(0, 6).cuboid(-6.5f, -5.0f, -4.0f, 13.0f, 10.0f, 9.0f).uv(45, 16).cuboid(EntityModelPartNames.NOSE, -3.5f, 0.0f, -6.0f, 7.0f, 5.0f, 2.0f).uv(52, 25).cuboid(EntityModelPartNames.LEFT_EAR, 3.5f, -8.0f, -1.0f, 5.0f, 4.0f, 1.0f).uv(52, 25).cuboid(EntityModelPartNames.RIGHT_EAR, -8.5f, -8.0f, -1.0f, 5.0f, 4.0f, 1.0f), ModelTransform.pivot(0.0f, 11.5f, -17.0f));
        lv2.addChild(EntityModelPartNames.BODY, ModelPartBuilder.create().uv(0, 25).cuboid(-9.5f, -13.0f, -6.5f, 19.0f, 26.0f, 13.0f), ModelTransform.of(0.0f, 10.0f, 0.0f, 1.5707964f, 0.0f, 0.0f));
        int i = 9;
        int j = 6;
        ModelPartBuilder lv3 = ModelPartBuilder.create().uv(40, 0).cuboid(-3.0f, 0.0f, -3.0f, 6.0f, 9.0f, 6.0f);
        lv2.addChild(EntityModelPartNames.RIGHT_HIND_LEG, lv3, ModelTransform.pivot(-5.5f, 15.0f, 9.0f));
        lv2.addChild(EntityModelPartNames.LEFT_HIND_LEG, lv3, ModelTransform.pivot(5.5f, 15.0f, 9.0f));
        lv2.addChild(EntityModelPartNames.RIGHT_FRONT_LEG, lv3, ModelTransform.pivot(-5.5f, 15.0f, -9.0f));
        lv2.addChild(EntityModelPartNames.LEFT_FRONT_LEG, lv3, ModelTransform.pivot(5.5f, 15.0f, -9.0f));
        return TexturedModelData.of(lv, 64, 64);
    }

    @Override
    public void animateModel(T arg, float f, float g, float h) {
        super.animateModel(arg, f, g, h);
        this.sittingAnimationProgress = ((PandaEntity)arg).getSittingAnimationProgress(h);
        this.lieOnBackAnimationProgress = ((PandaEntity)arg).getLieOnBackAnimationProgress(h);
        this.playAnimationProgress = ((PassiveEntity)arg).isBaby() ? 0.0f : ((PandaEntity)arg).getRollOverAnimationProgress(h);
    }

    @Override
    public void setAngles(T arg, float f, float g, float h, float i, float j) {
        super.setAngles(arg, f, g, h, i, j);
        boolean bl = ((PandaEntity)arg).getAskForBambooTicks() > 0;
        boolean bl2 = ((PandaEntity)arg).isSneezing();
        int k = ((PandaEntity)arg).getSneezeProgress();
        boolean bl3 = ((PandaEntity)arg).isEating();
        boolean bl4 = ((PandaEntity)arg).isScaredByThunderstorm();
        if (bl) {
            this.head.yaw = 0.35f * MathHelper.sin(0.6f * h);
            this.head.roll = 0.35f * MathHelper.sin(0.6f * h);
            this.rightFrontLeg.pitch = -0.75f * MathHelper.sin(0.3f * h);
            this.leftFrontLeg.pitch = 0.75f * MathHelper.sin(0.3f * h);
        } else {
            this.head.roll = 0.0f;
        }
        if (bl2) {
            if (k < 15) {
                this.head.pitch = -0.7853982f * (float)k / 14.0f;
            } else if (k < 20) {
                float l = (k - 15) / 5;
                this.head.pitch = -0.7853982f + 0.7853982f * l;
            }
        }
        if (this.sittingAnimationProgress > 0.0f) {
            this.body.pitch = ModelUtil.interpolateAngle(this.body.pitch, 1.7407963f, this.sittingAnimationProgress);
            this.head.pitch = ModelUtil.interpolateAngle(this.head.pitch, 1.5707964f, this.sittingAnimationProgress);
            this.rightFrontLeg.roll = -0.27079642f;
            this.leftFrontLeg.roll = 0.27079642f;
            this.rightHindLeg.roll = 0.5707964f;
            this.leftHindLeg.roll = -0.5707964f;
            if (bl3) {
                this.head.pitch = 1.5707964f + 0.2f * MathHelper.sin(h * 0.6f);
                this.rightFrontLeg.pitch = -0.4f - 0.2f * MathHelper.sin(h * 0.6f);
                this.leftFrontLeg.pitch = -0.4f - 0.2f * MathHelper.sin(h * 0.6f);
            }
            if (bl4) {
                this.head.pitch = 2.1707964f;
                this.rightFrontLeg.pitch = -0.9f;
                this.leftFrontLeg.pitch = -0.9f;
            }
        } else {
            this.rightHindLeg.roll = 0.0f;
            this.leftHindLeg.roll = 0.0f;
            this.rightFrontLeg.roll = 0.0f;
            this.leftFrontLeg.roll = 0.0f;
        }
        if (this.lieOnBackAnimationProgress > 0.0f) {
            this.rightHindLeg.pitch = -0.6f * MathHelper.sin(h * 0.15f);
            this.leftHindLeg.pitch = 0.6f * MathHelper.sin(h * 0.15f);
            this.rightFrontLeg.pitch = 0.3f * MathHelper.sin(h * 0.25f);
            this.leftFrontLeg.pitch = -0.3f * MathHelper.sin(h * 0.25f);
            this.head.pitch = ModelUtil.interpolateAngle(this.head.pitch, 1.5707964f, this.lieOnBackAnimationProgress);
        }
        if (this.playAnimationProgress > 0.0f) {
            this.head.pitch = ModelUtil.interpolateAngle(this.head.pitch, 2.0561945f, this.playAnimationProgress);
            this.rightHindLeg.pitch = -0.5f * MathHelper.sin(h * 0.5f);
            this.leftHindLeg.pitch = 0.5f * MathHelper.sin(h * 0.5f);
            this.rightFrontLeg.pitch = 0.5f * MathHelper.sin(h * 0.5f);
            this.leftFrontLeg.pitch = -0.5f * MathHelper.sin(h * 0.5f);
        }
    }
}

