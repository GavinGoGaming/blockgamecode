/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 * 
 * Could not load the following classes:
 *  net.fabricmc.api.EnvType
 *  net.fabricmc.api.Environment
 */
package net.minecraft.client.render.entity.model;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.model.Dilation;
import net.minecraft.client.model.ModelData;
import net.minecraft.client.model.ModelPart;
import net.minecraft.client.model.ModelPartBuilder;
import net.minecraft.client.model.ModelPartData;
import net.minecraft.client.model.ModelTransform;
import net.minecraft.client.model.TexturedModelData;
import net.minecraft.client.render.entity.model.ArmorStandArmorEntityModel;
import net.minecraft.client.render.entity.model.BipedEntityModel;
import net.minecraft.client.render.entity.model.EntityModelPartNames;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.decoration.ArmorStandEntity;
import net.minecraft.util.Arm;
import net.minecraft.util.math.MathHelper;

@Environment(value=EnvType.CLIENT)
public class ArmorStandEntityModel
extends ArmorStandArmorEntityModel {
    private static final String RIGHT_BODY_STICK = "right_body_stick";
    private static final String LEFT_BODY_STICK = "left_body_stick";
    private static final String SHOULDER_STICK = "shoulder_stick";
    private static final String BASE_PLATE = "base_plate";
    private final ModelPart rightBodyStick;
    private final ModelPart leftBodyStick;
    private final ModelPart shoulderStick;
    private final ModelPart basePlate;

    public ArmorStandEntityModel(ModelPart arg) {
        super(arg);
        this.rightBodyStick = arg.getChild(RIGHT_BODY_STICK);
        this.leftBodyStick = arg.getChild(LEFT_BODY_STICK);
        this.shoulderStick = arg.getChild(SHOULDER_STICK);
        this.basePlate = arg.getChild(BASE_PLATE);
        this.hat.visible = false;
    }

    public static TexturedModelData getTexturedModelData() {
        ModelData lv = BipedEntityModel.getModelData(Dilation.NONE, 0.0f);
        ModelPartData lv2 = lv.getRoot();
        lv2.addChild(EntityModelPartNames.HEAD, ModelPartBuilder.create().uv(0, 0).cuboid(-1.0f, -7.0f, -1.0f, 2.0f, 7.0f, 2.0f), ModelTransform.pivot(0.0f, 1.0f, 0.0f));
        lv2.addChild(EntityModelPartNames.BODY, ModelPartBuilder.create().uv(0, 26).cuboid(-6.0f, 0.0f, -1.5f, 12.0f, 3.0f, 3.0f), ModelTransform.NONE);
        lv2.addChild(EntityModelPartNames.RIGHT_ARM, ModelPartBuilder.create().uv(24, 0).cuboid(-2.0f, -2.0f, -1.0f, 2.0f, 12.0f, 2.0f), ModelTransform.pivot(-5.0f, 2.0f, 0.0f));
        lv2.addChild(EntityModelPartNames.LEFT_ARM, ModelPartBuilder.create().uv(32, 16).mirrored().cuboid(0.0f, -2.0f, -1.0f, 2.0f, 12.0f, 2.0f), ModelTransform.pivot(5.0f, 2.0f, 0.0f));
        lv2.addChild(EntityModelPartNames.RIGHT_LEG, ModelPartBuilder.create().uv(8, 0).cuboid(-1.0f, 0.0f, -1.0f, 2.0f, 11.0f, 2.0f), ModelTransform.pivot(-1.9f, 12.0f, 0.0f));
        lv2.addChild(EntityModelPartNames.LEFT_LEG, ModelPartBuilder.create().uv(40, 16).mirrored().cuboid(-1.0f, 0.0f, -1.0f, 2.0f, 11.0f, 2.0f), ModelTransform.pivot(1.9f, 12.0f, 0.0f));
        lv2.addChild(RIGHT_BODY_STICK, ModelPartBuilder.create().uv(16, 0).cuboid(-3.0f, 3.0f, -1.0f, 2.0f, 7.0f, 2.0f), ModelTransform.NONE);
        lv2.addChild(LEFT_BODY_STICK, ModelPartBuilder.create().uv(48, 16).cuboid(1.0f, 3.0f, -1.0f, 2.0f, 7.0f, 2.0f), ModelTransform.NONE);
        lv2.addChild(SHOULDER_STICK, ModelPartBuilder.create().uv(0, 48).cuboid(-4.0f, 10.0f, -1.0f, 8.0f, 2.0f, 2.0f), ModelTransform.NONE);
        lv2.addChild(BASE_PLATE, ModelPartBuilder.create().uv(0, 32).cuboid(-6.0f, 11.0f, -6.0f, 12.0f, 1.0f, 12.0f), ModelTransform.pivot(0.0f, 12.0f, 0.0f));
        return TexturedModelData.of(lv, 64, 64);
    }

    @Override
    public void animateModel(ArmorStandEntity arg, float f, float g, float h) {
        this.basePlate.pitch = 0.0f;
        this.basePlate.yaw = (float)Math.PI / 180 * -MathHelper.lerpAngleDegrees(h, arg.prevYaw, arg.getYaw());
        this.basePlate.roll = 0.0f;
    }

    @Override
    public void setAngles(ArmorStandEntity arg, float f, float g, float h, float i, float j) {
        super.setAngles(arg, f, g, h, i, j);
        this.leftArm.visible = arg.shouldShowArms();
        this.rightArm.visible = arg.shouldShowArms();
        this.basePlate.visible = !arg.shouldHideBasePlate();
        this.rightBodyStick.pitch = (float)Math.PI / 180 * arg.getBodyRotation().getPitch();
        this.rightBodyStick.yaw = (float)Math.PI / 180 * arg.getBodyRotation().getYaw();
        this.rightBodyStick.roll = (float)Math.PI / 180 * arg.getBodyRotation().getRoll();
        this.leftBodyStick.pitch = (float)Math.PI / 180 * arg.getBodyRotation().getPitch();
        this.leftBodyStick.yaw = (float)Math.PI / 180 * arg.getBodyRotation().getYaw();
        this.leftBodyStick.roll = (float)Math.PI / 180 * arg.getBodyRotation().getRoll();
        this.shoulderStick.pitch = (float)Math.PI / 180 * arg.getBodyRotation().getPitch();
        this.shoulderStick.yaw = (float)Math.PI / 180 * arg.getBodyRotation().getYaw();
        this.shoulderStick.roll = (float)Math.PI / 180 * arg.getBodyRotation().getRoll();
    }

    @Override
    protected Iterable<ModelPart> getBodyParts() {
        return Iterables.concat(super.getBodyParts(), ImmutableList.of(this.rightBodyStick, this.leftBodyStick, this.shoulderStick, this.basePlate));
    }

    @Override
    public void setArmAngle(Arm arm, MatrixStack matrices) {
        ModelPart lv = this.getArm(arm);
        boolean bl = lv.visible;
        lv.visible = true;
        super.setArmAngle(arm, matrices);
        lv.visible = bl;
    }
}

