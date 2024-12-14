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
import net.minecraft.client.render.entity.model.EntityModelPartNames;
import net.minecraft.client.render.entity.model.SinglePartEntityModel;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.MathHelper;

@Environment(value=EnvType.CLIENT)
public class SpiderEntityModel<T extends Entity>
extends SinglePartEntityModel<T> {
    private static final String BODY0 = "body0";
    private static final String BODY1 = "body1";
    private static final String RIGHT_MIDDLE_FRONT_LEG = "right_middle_front_leg";
    private static final String LEFT_MIDDLE_FRONT_LEG = "left_middle_front_leg";
    private static final String RIGHT_MIDDLE_HIND_LEG = "right_middle_hind_leg";
    private static final String LEFT_MIDDLE_HIND_LEG = "left_middle_hind_leg";
    private final ModelPart root;
    private final ModelPart head;
    private final ModelPart rightHindLeg;
    private final ModelPart leftHindLeg;
    private final ModelPart rightMiddleLeg;
    private final ModelPart leftMiddleLeg;
    private final ModelPart rightMiddleFrontLeg;
    private final ModelPart leftMiddleFrontLeg;
    private final ModelPart rightFrontLeg;
    private final ModelPart leftFrontLeg;

    public SpiderEntityModel(ModelPart root) {
        this.root = root;
        this.head = root.getChild(EntityModelPartNames.HEAD);
        this.rightHindLeg = root.getChild(EntityModelPartNames.RIGHT_HIND_LEG);
        this.leftHindLeg = root.getChild(EntityModelPartNames.LEFT_HIND_LEG);
        this.rightMiddleLeg = root.getChild(RIGHT_MIDDLE_HIND_LEG);
        this.leftMiddleLeg = root.getChild(LEFT_MIDDLE_HIND_LEG);
        this.rightMiddleFrontLeg = root.getChild(RIGHT_MIDDLE_FRONT_LEG);
        this.leftMiddleFrontLeg = root.getChild(LEFT_MIDDLE_FRONT_LEG);
        this.rightFrontLeg = root.getChild(EntityModelPartNames.RIGHT_FRONT_LEG);
        this.leftFrontLeg = root.getChild(EntityModelPartNames.LEFT_FRONT_LEG);
    }

    public static TexturedModelData getTexturedModelData() {
        ModelData lv = new ModelData();
        ModelPartData lv2 = lv.getRoot();
        int i = 15;
        lv2.addChild(EntityModelPartNames.HEAD, ModelPartBuilder.create().uv(32, 4).cuboid(-4.0f, -4.0f, -8.0f, 8.0f, 8.0f, 8.0f), ModelTransform.pivot(0.0f, 15.0f, -3.0f));
        lv2.addChild(BODY0, ModelPartBuilder.create().uv(0, 0).cuboid(-3.0f, -3.0f, -3.0f, 6.0f, 6.0f, 6.0f), ModelTransform.pivot(0.0f, 15.0f, 0.0f));
        lv2.addChild(BODY1, ModelPartBuilder.create().uv(0, 12).cuboid(-5.0f, -4.0f, -6.0f, 10.0f, 8.0f, 12.0f), ModelTransform.pivot(0.0f, 15.0f, 9.0f));
        ModelPartBuilder lv3 = ModelPartBuilder.create().uv(18, 0).cuboid(-15.0f, -1.0f, -1.0f, 16.0f, 2.0f, 2.0f);
        ModelPartBuilder lv4 = ModelPartBuilder.create().uv(18, 0).mirrored().cuboid(-1.0f, -1.0f, -1.0f, 16.0f, 2.0f, 2.0f);
        lv2.addChild(EntityModelPartNames.RIGHT_HIND_LEG, lv3, ModelTransform.pivot(-4.0f, 15.0f, 2.0f));
        lv2.addChild(EntityModelPartNames.LEFT_HIND_LEG, lv4, ModelTransform.pivot(4.0f, 15.0f, 2.0f));
        lv2.addChild(RIGHT_MIDDLE_HIND_LEG, lv3, ModelTransform.pivot(-4.0f, 15.0f, 1.0f));
        lv2.addChild(LEFT_MIDDLE_HIND_LEG, lv4, ModelTransform.pivot(4.0f, 15.0f, 1.0f));
        lv2.addChild(RIGHT_MIDDLE_FRONT_LEG, lv3, ModelTransform.pivot(-4.0f, 15.0f, 0.0f));
        lv2.addChild(LEFT_MIDDLE_FRONT_LEG, lv4, ModelTransform.pivot(4.0f, 15.0f, 0.0f));
        lv2.addChild(EntityModelPartNames.RIGHT_FRONT_LEG, lv3, ModelTransform.pivot(-4.0f, 15.0f, -1.0f));
        lv2.addChild(EntityModelPartNames.LEFT_FRONT_LEG, lv4, ModelTransform.pivot(4.0f, 15.0f, -1.0f));
        return TexturedModelData.of(lv, 64, 32);
    }

    @Override
    public ModelPart getPart() {
        return this.root;
    }

    @Override
    public void setAngles(T entity, float limbAngle, float limbDistance, float animationProgress, float headYaw, float headPitch) {
        this.head.yaw = headYaw * ((float)Math.PI / 180);
        this.head.pitch = headPitch * ((float)Math.PI / 180);
        float k = 0.7853982f;
        this.rightHindLeg.roll = -0.7853982f;
        this.leftHindLeg.roll = 0.7853982f;
        this.rightMiddleLeg.roll = -0.58119464f;
        this.leftMiddleLeg.roll = 0.58119464f;
        this.rightMiddleFrontLeg.roll = -0.58119464f;
        this.leftMiddleFrontLeg.roll = 0.58119464f;
        this.rightFrontLeg.roll = -0.7853982f;
        this.leftFrontLeg.roll = 0.7853982f;
        float l = -0.0f;
        float m = 0.3926991f;
        this.rightHindLeg.yaw = 0.7853982f;
        this.leftHindLeg.yaw = -0.7853982f;
        this.rightMiddleLeg.yaw = 0.3926991f;
        this.leftMiddleLeg.yaw = -0.3926991f;
        this.rightMiddleFrontLeg.yaw = -0.3926991f;
        this.leftMiddleFrontLeg.yaw = 0.3926991f;
        this.rightFrontLeg.yaw = -0.7853982f;
        this.leftFrontLeg.yaw = 0.7853982f;
        float n = -(MathHelper.cos(limbAngle * 0.6662f * 2.0f + 0.0f) * 0.4f) * limbDistance;
        float o = -(MathHelper.cos(limbAngle * 0.6662f * 2.0f + (float)Math.PI) * 0.4f) * limbDistance;
        float p = -(MathHelper.cos(limbAngle * 0.6662f * 2.0f + 1.5707964f) * 0.4f) * limbDistance;
        float q = -(MathHelper.cos(limbAngle * 0.6662f * 2.0f + 4.712389f) * 0.4f) * limbDistance;
        float r = Math.abs(MathHelper.sin(limbAngle * 0.6662f + 0.0f) * 0.4f) * limbDistance;
        float s = Math.abs(MathHelper.sin(limbAngle * 0.6662f + (float)Math.PI) * 0.4f) * limbDistance;
        float t = Math.abs(MathHelper.sin(limbAngle * 0.6662f + 1.5707964f) * 0.4f) * limbDistance;
        float u = Math.abs(MathHelper.sin(limbAngle * 0.6662f + 4.712389f) * 0.4f) * limbDistance;
        this.rightHindLeg.yaw += n;
        this.leftHindLeg.yaw += -n;
        this.rightMiddleLeg.yaw += o;
        this.leftMiddleLeg.yaw += -o;
        this.rightMiddleFrontLeg.yaw += p;
        this.leftMiddleFrontLeg.yaw += -p;
        this.rightFrontLeg.yaw += q;
        this.leftFrontLeg.yaw += -q;
        this.rightHindLeg.roll += r;
        this.leftHindLeg.roll += -r;
        this.rightMiddleLeg.roll += s;
        this.leftMiddleLeg.roll += -s;
        this.rightMiddleFrontLeg.roll += t;
        this.leftMiddleFrontLeg.roll += -t;
        this.rightFrontLeg.roll += u;
        this.leftFrontLeg.roll += -u;
    }
}

