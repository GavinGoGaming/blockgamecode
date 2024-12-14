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
import net.minecraft.client.model.ModelPart;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.item.CrossbowItem;
import net.minecraft.util.Arm;
import net.minecraft.util.math.MathHelper;

@Environment(value=EnvType.CLIENT)
public class CrossbowPosing {
    public static void hold(ModelPart holdingArm, ModelPart otherArm, ModelPart head, boolean rightArmed) {
        ModelPart lv = rightArmed ? holdingArm : otherArm;
        ModelPart lv2 = rightArmed ? otherArm : holdingArm;
        lv.yaw = (rightArmed ? -0.3f : 0.3f) + head.yaw;
        lv2.yaw = (rightArmed ? 0.6f : -0.6f) + head.yaw;
        lv.pitch = -1.5707964f + head.pitch + 0.1f;
        lv2.pitch = -1.5f + head.pitch;
    }

    public static void charge(ModelPart holdingArm, ModelPart pullingArm, LivingEntity actor, boolean rightArmed) {
        ModelPart lv = rightArmed ? holdingArm : pullingArm;
        ModelPart lv2 = rightArmed ? pullingArm : holdingArm;
        lv.yaw = rightArmed ? -0.8f : 0.8f;
        lv2.pitch = lv.pitch = -0.97079635f;
        float f = CrossbowItem.getPullTime(actor.getActiveItem(), actor);
        float g = MathHelper.clamp((float)actor.getItemUseTime(), 0.0f, f);
        float h = g / f;
        lv2.yaw = MathHelper.lerp(h, 0.4f, 0.85f) * (float)(rightArmed ? 1 : -1);
        lv2.pitch = MathHelper.lerp(h, lv2.pitch, -1.5707964f);
    }

    public static <T extends MobEntity> void meleeAttack(ModelPart leftArm, ModelPart rightArm, T actor, float swingProgress, float animationProgress) {
        float h = MathHelper.sin(swingProgress * (float)Math.PI);
        float i = MathHelper.sin((1.0f - (1.0f - swingProgress) * (1.0f - swingProgress)) * (float)Math.PI);
        leftArm.roll = 0.0f;
        rightArm.roll = 0.0f;
        leftArm.yaw = 0.15707964f;
        rightArm.yaw = -0.15707964f;
        if (actor.getMainArm() == Arm.RIGHT) {
            leftArm.pitch = -1.8849558f + MathHelper.cos(animationProgress * 0.09f) * 0.15f;
            rightArm.pitch = -0.0f + MathHelper.cos(animationProgress * 0.19f) * 0.5f;
            leftArm.pitch += h * 2.2f - i * 0.4f;
            rightArm.pitch += h * 1.2f - i * 0.4f;
        } else {
            leftArm.pitch = -0.0f + MathHelper.cos(animationProgress * 0.19f) * 0.5f;
            rightArm.pitch = -1.8849558f + MathHelper.cos(animationProgress * 0.09f) * 0.15f;
            leftArm.pitch += h * 1.2f - i * 0.4f;
            rightArm.pitch += h * 2.2f - i * 0.4f;
        }
        CrossbowPosing.swingArms(leftArm, rightArm, animationProgress);
    }

    public static void swingArm(ModelPart arm, float animationProgress, float sigma) {
        arm.roll += sigma * (MathHelper.cos(animationProgress * 0.09f) * 0.05f + 0.05f);
        arm.pitch += sigma * (MathHelper.sin(animationProgress * 0.067f) * 0.05f);
    }

    public static void swingArms(ModelPart leftArm, ModelPart rightArm, float animationProgress) {
        CrossbowPosing.swingArm(leftArm, animationProgress, 1.0f);
        CrossbowPosing.swingArm(rightArm, animationProgress, -1.0f);
    }

    public static void meleeAttack(ModelPart leftArm, ModelPart rightArm, boolean attacking, float swingProgress, float animationProgress) {
        float j;
        float h = MathHelper.sin(swingProgress * (float)Math.PI);
        float i = MathHelper.sin((1.0f - (1.0f - swingProgress) * (1.0f - swingProgress)) * (float)Math.PI);
        rightArm.roll = 0.0f;
        leftArm.roll = 0.0f;
        rightArm.yaw = -(0.1f - h * 0.6f);
        leftArm.yaw = 0.1f - h * 0.6f;
        rightArm.pitch = j = (float)(-Math.PI) / (attacking ? 1.5f : 2.25f);
        leftArm.pitch = j;
        rightArm.pitch += h * 1.2f - i * 0.4f;
        leftArm.pitch += h * 1.2f - i * 0.4f;
        CrossbowPosing.swingArms(rightArm, leftArm, animationProgress);
    }
}

