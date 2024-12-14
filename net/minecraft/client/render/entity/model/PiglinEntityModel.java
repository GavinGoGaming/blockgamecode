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
import net.minecraft.client.model.Dilation;
import net.minecraft.client.model.ModelData;
import net.minecraft.client.model.ModelPart;
import net.minecraft.client.model.ModelPartBuilder;
import net.minecraft.client.model.ModelPartData;
import net.minecraft.client.model.ModelTransform;
import net.minecraft.client.render.entity.model.CrossbowPosing;
import net.minecraft.client.render.entity.model.EntityModelPartNames;
import net.minecraft.client.render.entity.model.PlayerEntityModel;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.mob.AbstractPiglinEntity;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.mob.PiglinActivity;
import net.minecraft.entity.mob.PiglinEntity;
import net.minecraft.util.math.MathHelper;

@Environment(value=EnvType.CLIENT)
public class PiglinEntityModel<T extends MobEntity>
extends PlayerEntityModel<T> {
    public final ModelPart rightEar;
    private final ModelPart leftEar;
    private final ModelTransform bodyRotation;
    private final ModelTransform headRotation;
    private final ModelTransform leftArmRotation;
    private final ModelTransform rightArmRotation;

    public PiglinEntityModel(ModelPart arg) {
        super(arg, false);
        this.rightEar = this.head.getChild(EntityModelPartNames.RIGHT_EAR);
        this.leftEar = this.head.getChild(EntityModelPartNames.LEFT_EAR);
        this.bodyRotation = this.body.getTransform();
        this.headRotation = this.head.getTransform();
        this.leftArmRotation = this.leftArm.getTransform();
        this.rightArmRotation = this.rightArm.getTransform();
    }

    public static ModelData getModelData(Dilation dilation) {
        ModelData lv = PlayerEntityModel.getTexturedModelData(dilation, false);
        ModelPartData lv2 = lv.getRoot();
        lv2.addChild(EntityModelPartNames.BODY, ModelPartBuilder.create().uv(16, 16).cuboid(-4.0f, 0.0f, -2.0f, 8.0f, 12.0f, 4.0f, dilation), ModelTransform.NONE);
        PiglinEntityModel.addHead(dilation, lv);
        lv2.addChild(EntityModelPartNames.HAT, ModelPartBuilder.create(), ModelTransform.NONE);
        return lv;
    }

    public static void addHead(Dilation dilation, ModelData baseModelData) {
        ModelPartData lv = baseModelData.getRoot();
        ModelPartData lv2 = lv.addChild(EntityModelPartNames.HEAD, ModelPartBuilder.create().uv(0, 0).cuboid(-5.0f, -8.0f, -4.0f, 10.0f, 8.0f, 8.0f, dilation).uv(31, 1).cuboid(-2.0f, -4.0f, -5.0f, 4.0f, 4.0f, 1.0f, dilation).uv(2, 4).cuboid(2.0f, -2.0f, -5.0f, 1.0f, 2.0f, 1.0f, dilation).uv(2, 0).cuboid(-3.0f, -2.0f, -5.0f, 1.0f, 2.0f, 1.0f, dilation), ModelTransform.NONE);
        lv2.addChild(EntityModelPartNames.LEFT_EAR, ModelPartBuilder.create().uv(51, 6).cuboid(0.0f, 0.0f, -2.0f, 1.0f, 5.0f, 4.0f, dilation), ModelTransform.of(4.5f, -6.0f, 0.0f, 0.0f, 0.0f, -0.5235988f));
        lv2.addChild(EntityModelPartNames.RIGHT_EAR, ModelPartBuilder.create().uv(39, 6).cuboid(-1.0f, 0.0f, -2.0f, 1.0f, 5.0f, 4.0f, dilation), ModelTransform.of(-4.5f, -6.0f, 0.0f, 0.0f, 0.0f, 0.5235988f));
    }

    @Override
    public void setAngles(T arg, float f, float g, float h, float i, float j) {
        this.body.setTransform(this.bodyRotation);
        this.head.setTransform(this.headRotation);
        this.leftArm.setTransform(this.leftArmRotation);
        this.rightArm.setTransform(this.rightArmRotation);
        super.setAngles(arg, f, g, h, i, j);
        float k = 0.5235988f;
        float l = h * 0.1f + f * 0.5f;
        float m = 0.08f + g * 0.4f;
        this.leftEar.roll = -0.5235988f - MathHelper.cos(l * 1.2f) * m;
        this.rightEar.roll = 0.5235988f + MathHelper.cos(l) * m;
        if (arg instanceof AbstractPiglinEntity) {
            AbstractPiglinEntity lv = (AbstractPiglinEntity)arg;
            PiglinActivity lv2 = lv.getActivity();
            if (lv2 == PiglinActivity.DANCING) {
                float n = h / 60.0f;
                this.rightEar.roll = 0.5235988f + (float)Math.PI / 180 * MathHelper.sin(n * 30.0f) * 10.0f;
                this.leftEar.roll = -0.5235988f - (float)Math.PI / 180 * MathHelper.cos(n * 30.0f) * 10.0f;
                this.head.pivotX = MathHelper.sin(n * 10.0f);
                this.head.pivotY = MathHelper.sin(n * 40.0f) + 0.4f;
                this.rightArm.roll = (float)Math.PI / 180 * (70.0f + MathHelper.cos(n * 40.0f) * 10.0f);
                this.leftArm.roll = this.rightArm.roll * -1.0f;
                this.rightArm.pivotY = MathHelper.sin(n * 40.0f) * 0.5f + 1.5f;
                this.leftArm.pivotY = MathHelper.sin(n * 40.0f) * 0.5f + 1.5f;
                this.body.pivotY = MathHelper.sin(n * 40.0f) * 0.35f;
            } else if (lv2 == PiglinActivity.ATTACKING_WITH_MELEE_WEAPON && this.handSwingProgress == 0.0f) {
                this.rotateMainArm(arg);
            } else if (lv2 == PiglinActivity.CROSSBOW_HOLD) {
                CrossbowPosing.hold(this.rightArm, this.leftArm, this.head, !((MobEntity)arg).isLeftHanded());
            } else if (lv2 == PiglinActivity.CROSSBOW_CHARGE) {
                CrossbowPosing.charge(this.rightArm, this.leftArm, arg, !((MobEntity)arg).isLeftHanded());
            } else if (lv2 == PiglinActivity.ADMIRING_ITEM) {
                this.head.pitch = 0.5f;
                this.head.yaw = 0.0f;
                if (((MobEntity)arg).isLeftHanded()) {
                    this.rightArm.yaw = -0.5f;
                    this.rightArm.pitch = -0.9f;
                } else {
                    this.leftArm.yaw = 0.5f;
                    this.leftArm.pitch = -0.9f;
                }
            }
        } else if (((Entity)arg).getType() == EntityType.ZOMBIFIED_PIGLIN) {
            CrossbowPosing.meleeAttack(this.leftArm, this.rightArm, ((MobEntity)arg).isAttacking(), this.handSwingProgress, h);
        }
        this.leftPants.copyTransform(this.leftLeg);
        this.rightPants.copyTransform(this.rightLeg);
        this.leftSleeve.copyTransform(this.leftArm);
        this.rightSleeve.copyTransform(this.rightArm);
        this.jacket.copyTransform(this.body);
        this.hat.copyTransform(this.head);
    }

    @Override
    protected void animateArms(T arg, float f) {
        if (this.handSwingProgress > 0.0f && arg instanceof PiglinEntity && ((PiglinEntity)arg).getActivity() == PiglinActivity.ATTACKING_WITH_MELEE_WEAPON) {
            CrossbowPosing.meleeAttack(this.rightArm, this.leftArm, arg, this.handSwingProgress, f);
            return;
        }
        super.animateArms(arg, f);
    }

    private void rotateMainArm(T entity) {
        if (((MobEntity)entity).isLeftHanded()) {
            this.leftArm.pitch = -1.8f;
        } else {
            this.rightArm.pitch = -1.8f;
        }
    }
}

