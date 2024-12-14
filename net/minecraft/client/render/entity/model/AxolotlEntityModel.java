/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 * 
 * Could not load the following classes:
 *  net.fabricmc.api.EnvType
 *  net.fabricmc.api.Environment
 */
package net.minecraft.client.render.entity.model;

import com.google.common.collect.ImmutableList;
import java.util.Map;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.model.Dilation;
import net.minecraft.client.model.ModelData;
import net.minecraft.client.model.ModelPart;
import net.minecraft.client.model.ModelPartBuilder;
import net.minecraft.client.model.ModelPartData;
import net.minecraft.client.model.ModelTransform;
import net.minecraft.client.model.TexturedModelData;
import net.minecraft.client.render.entity.model.AnimalModel;
import net.minecraft.client.render.entity.model.EntityModelPartNames;
import net.minecraft.entity.Entity;
import net.minecraft.entity.passive.AxolotlEntity;
import net.minecraft.util.math.MathHelper;
import org.joml.Vector3f;

@Environment(value=EnvType.CLIENT)
public class AxolotlEntityModel<T extends AxolotlEntity>
extends AnimalModel<T> {
    public static final float MOVING_IN_WATER_LEG_PITCH = 1.8849558f;
    private final ModelPart tail;
    private final ModelPart leftHindLeg;
    private final ModelPart rightHindLeg;
    private final ModelPart leftFrontLeg;
    private final ModelPart rightFrontLeg;
    private final ModelPart body;
    private final ModelPart head;
    private final ModelPart topGills;
    private final ModelPart leftGills;
    private final ModelPart rightGills;

    public AxolotlEntityModel(ModelPart root) {
        super(true, 8.0f, 3.35f);
        this.body = root.getChild(EntityModelPartNames.BODY);
        this.head = this.body.getChild(EntityModelPartNames.HEAD);
        this.rightHindLeg = this.body.getChild(EntityModelPartNames.RIGHT_HIND_LEG);
        this.leftHindLeg = this.body.getChild(EntityModelPartNames.LEFT_HIND_LEG);
        this.rightFrontLeg = this.body.getChild(EntityModelPartNames.RIGHT_FRONT_LEG);
        this.leftFrontLeg = this.body.getChild(EntityModelPartNames.LEFT_FRONT_LEG);
        this.tail = this.body.getChild(EntityModelPartNames.TAIL);
        this.topGills = this.head.getChild(EntityModelPartNames.TOP_GILLS);
        this.leftGills = this.head.getChild(EntityModelPartNames.LEFT_GILLS);
        this.rightGills = this.head.getChild(EntityModelPartNames.RIGHT_GILLS);
    }

    public static TexturedModelData getTexturedModelData() {
        ModelData lv = new ModelData();
        ModelPartData lv2 = lv.getRoot();
        ModelPartData lv3 = lv2.addChild(EntityModelPartNames.BODY, ModelPartBuilder.create().uv(0, 11).cuboid(-4.0f, -2.0f, -9.0f, 8.0f, 4.0f, 10.0f).uv(2, 17).cuboid(0.0f, -3.0f, -8.0f, 0.0f, 5.0f, 9.0f), ModelTransform.pivot(0.0f, 20.0f, 5.0f));
        Dilation lv4 = new Dilation(0.001f);
        ModelPartData lv5 = lv3.addChild(EntityModelPartNames.HEAD, ModelPartBuilder.create().uv(0, 1).cuboid(-4.0f, -3.0f, -5.0f, 8.0f, 5.0f, 5.0f, lv4), ModelTransform.pivot(0.0f, 0.0f, -9.0f));
        ModelPartBuilder lv6 = ModelPartBuilder.create().uv(3, 37).cuboid(-4.0f, -3.0f, 0.0f, 8.0f, 3.0f, 0.0f, lv4);
        ModelPartBuilder lv7 = ModelPartBuilder.create().uv(0, 40).cuboid(-3.0f, -5.0f, 0.0f, 3.0f, 7.0f, 0.0f, lv4);
        ModelPartBuilder lv8 = ModelPartBuilder.create().uv(11, 40).cuboid(0.0f, -5.0f, 0.0f, 3.0f, 7.0f, 0.0f, lv4);
        lv5.addChild(EntityModelPartNames.TOP_GILLS, lv6, ModelTransform.pivot(0.0f, -3.0f, -1.0f));
        lv5.addChild(EntityModelPartNames.LEFT_GILLS, lv7, ModelTransform.pivot(-4.0f, 0.0f, -1.0f));
        lv5.addChild(EntityModelPartNames.RIGHT_GILLS, lv8, ModelTransform.pivot(4.0f, 0.0f, -1.0f));
        ModelPartBuilder lv9 = ModelPartBuilder.create().uv(2, 13).cuboid(-1.0f, 0.0f, 0.0f, 3.0f, 5.0f, 0.0f, lv4);
        ModelPartBuilder lv10 = ModelPartBuilder.create().uv(2, 13).cuboid(-2.0f, 0.0f, 0.0f, 3.0f, 5.0f, 0.0f, lv4);
        lv3.addChild(EntityModelPartNames.RIGHT_HIND_LEG, lv10, ModelTransform.pivot(-3.5f, 1.0f, -1.0f));
        lv3.addChild(EntityModelPartNames.LEFT_HIND_LEG, lv9, ModelTransform.pivot(3.5f, 1.0f, -1.0f));
        lv3.addChild(EntityModelPartNames.RIGHT_FRONT_LEG, lv10, ModelTransform.pivot(-3.5f, 1.0f, -8.0f));
        lv3.addChild(EntityModelPartNames.LEFT_FRONT_LEG, lv9, ModelTransform.pivot(3.5f, 1.0f, -8.0f));
        lv3.addChild(EntityModelPartNames.TAIL, ModelPartBuilder.create().uv(2, 19).cuboid(0.0f, -3.0f, 0.0f, 0.0f, 5.0f, 12.0f), ModelTransform.pivot(0.0f, 0.0f, 1.0f));
        return TexturedModelData.of(lv, 64, 64);
    }

    @Override
    protected Iterable<ModelPart> getHeadParts() {
        return ImmutableList.of();
    }

    @Override
    protected Iterable<ModelPart> getBodyParts() {
        return ImmutableList.of(this.body);
    }

    @Override
    public void setAngles(T arg, float f, float g, float h, float i, float j) {
        boolean bl;
        this.resetAngles(arg, i, j);
        if (((AxolotlEntity)arg).isPlayingDead()) {
            this.setPlayingDeadAngles(i);
            this.updateAnglesCache(arg);
            return;
        }
        boolean bl2 = bl = g > 1.0E-5f || ((Entity)arg).getPitch() != ((AxolotlEntity)arg).prevPitch || ((Entity)arg).getYaw() != ((AxolotlEntity)arg).prevYaw;
        if (((Entity)arg).isInsideWaterOrBubbleColumn()) {
            if (bl) {
                this.setMovingInWaterAngles(h, j);
            } else {
                this.setStandingInWaterAngles(h);
            }
            this.updateAnglesCache(arg);
            return;
        }
        if (((Entity)arg).isOnGround()) {
            if (bl) {
                this.setMovingOnGroundAngles(h, i);
            } else {
                this.setStandingOnGroundAngles(h, i);
            }
        }
        this.updateAnglesCache(arg);
    }

    private void updateAnglesCache(T axolotl) {
        Map<String, Vector3f> map = ((AxolotlEntity)axolotl).getModelAngles();
        map.put("body", this.getAngles(this.body));
        map.put("head", this.getAngles(this.head));
        map.put("right_hind_leg", this.getAngles(this.rightHindLeg));
        map.put("left_hind_leg", this.getAngles(this.leftHindLeg));
        map.put("right_front_leg", this.getAngles(this.rightFrontLeg));
        map.put("left_front_leg", this.getAngles(this.leftFrontLeg));
        map.put("tail", this.getAngles(this.tail));
        map.put("top_gills", this.getAngles(this.topGills));
        map.put("left_gills", this.getAngles(this.leftGills));
        map.put("right_gills", this.getAngles(this.rightGills));
    }

    private Vector3f getAngles(ModelPart part) {
        return new Vector3f(part.pitch, part.yaw, part.roll);
    }

    private void setAngles(ModelPart part, Vector3f angles) {
        part.setAngles(angles.x(), angles.y(), angles.z());
    }

    private void resetAngles(T axolotl, float headYaw, float headPitch) {
        this.body.pivotX = 0.0f;
        this.head.pivotY = 0.0f;
        this.body.pivotY = 20.0f;
        Map<String, Vector3f> map = ((AxolotlEntity)axolotl).getModelAngles();
        if (map.isEmpty()) {
            this.body.setAngles(headPitch * ((float)Math.PI / 180), headYaw * ((float)Math.PI / 180), 0.0f);
            this.head.setAngles(0.0f, 0.0f, 0.0f);
            this.leftHindLeg.setAngles(0.0f, 0.0f, 0.0f);
            this.rightHindLeg.setAngles(0.0f, 0.0f, 0.0f);
            this.leftFrontLeg.setAngles(0.0f, 0.0f, 0.0f);
            this.rightFrontLeg.setAngles(0.0f, 0.0f, 0.0f);
            this.leftGills.setAngles(0.0f, 0.0f, 0.0f);
            this.rightGills.setAngles(0.0f, 0.0f, 0.0f);
            this.topGills.setAngles(0.0f, 0.0f, 0.0f);
            this.tail.setAngles(0.0f, 0.0f, 0.0f);
        } else {
            this.setAngles(this.body, map.get("body"));
            this.setAngles(this.head, map.get("head"));
            this.setAngles(this.leftHindLeg, map.get("left_hind_leg"));
            this.setAngles(this.rightHindLeg, map.get("right_hind_leg"));
            this.setAngles(this.leftFrontLeg, map.get("left_front_leg"));
            this.setAngles(this.rightFrontLeg, map.get("right_front_leg"));
            this.setAngles(this.leftGills, map.get("left_gills"));
            this.setAngles(this.rightGills, map.get("right_gills"));
            this.setAngles(this.topGills, map.get("top_gills"));
            this.setAngles(this.tail, map.get("tail"));
        }
    }

    private float lerpAngleDegrees(float start, float end) {
        return this.lerpAngleDegrees(0.05f, start, end);
    }

    private float lerpAngleDegrees(float delta, float start, float end) {
        return MathHelper.lerpAngleDegrees(delta, start, end);
    }

    private void setAngles(ModelPart part, float pitch, float yaw, float roll) {
        part.setAngles(this.lerpAngleDegrees(part.pitch, pitch), this.lerpAngleDegrees(part.yaw, yaw), this.lerpAngleDegrees(part.roll, roll));
    }

    private void setStandingOnGroundAngles(float animationProgress, float headYaw) {
        float h = animationProgress * 0.09f;
        float i = MathHelper.sin(h);
        float j = MathHelper.cos(h);
        float k = i * i - 2.0f * i;
        float l = j * j - 3.0f * i;
        this.head.pitch = this.lerpAngleDegrees(this.head.pitch, -0.09f * k);
        this.head.yaw = this.lerpAngleDegrees(this.head.yaw, 0.0f);
        this.head.roll = this.lerpAngleDegrees(this.head.roll, -0.2f);
        this.tail.yaw = this.lerpAngleDegrees(this.tail.yaw, -0.1f + 0.1f * k);
        this.topGills.pitch = this.lerpAngleDegrees(this.topGills.pitch, 0.6f + 0.05f * l);
        this.leftGills.yaw = this.lerpAngleDegrees(this.leftGills.yaw, -this.topGills.pitch);
        this.rightGills.yaw = this.lerpAngleDegrees(this.rightGills.yaw, -this.leftGills.yaw);
        this.setAngles(this.leftHindLeg, 1.1f, 1.0f, 0.0f);
        this.setAngles(this.leftFrontLeg, 0.8f, 2.3f, -0.5f);
        this.copyLegAngles();
        this.body.pitch = this.lerpAngleDegrees(0.2f, this.body.pitch, 0.0f);
        this.body.yaw = this.lerpAngleDegrees(this.body.yaw, headYaw * ((float)Math.PI / 180));
        this.body.roll = this.lerpAngleDegrees(this.body.roll, 0.0f);
    }

    private void setMovingOnGroundAngles(float animationProgress, float headYaw) {
        float h = animationProgress * 0.11f;
        float i = MathHelper.cos(h);
        float j = (i * i - 2.0f * i) / 5.0f;
        float k = 0.7f * i;
        this.head.pitch = this.lerpAngleDegrees(this.head.pitch, 0.0f);
        this.head.yaw = this.lerpAngleDegrees(this.head.yaw, 0.09f * i);
        this.head.roll = this.lerpAngleDegrees(this.head.roll, 0.0f);
        this.tail.yaw = this.lerpAngleDegrees(this.tail.yaw, this.head.yaw);
        this.topGills.pitch = this.lerpAngleDegrees(this.topGills.pitch, 0.6f - 0.08f * (i * i + 2.0f * MathHelper.sin(h)));
        this.leftGills.yaw = this.lerpAngleDegrees(this.leftGills.yaw, -this.topGills.pitch);
        this.rightGills.yaw = this.lerpAngleDegrees(this.rightGills.yaw, -this.leftGills.yaw);
        this.setAngles(this.leftHindLeg, 0.9424779f, 1.5f - j, -0.1f);
        this.setAngles(this.leftFrontLeg, 1.0995574f, 1.5707964f - k, 0.0f);
        this.setAngles(this.rightHindLeg, this.leftHindLeg.pitch, -1.0f - j, 0.0f);
        this.setAngles(this.rightFrontLeg, this.leftFrontLeg.pitch, -1.5707964f - k, 0.0f);
        this.body.pitch = this.lerpAngleDegrees(0.2f, this.body.pitch, 0.0f);
        this.body.yaw = this.lerpAngleDegrees(this.body.yaw, headYaw * ((float)Math.PI / 180));
        this.body.roll = this.lerpAngleDegrees(this.body.roll, 0.0f);
    }

    private void setStandingInWaterAngles(float animationProgress) {
        float g = animationProgress * 0.075f;
        float h = MathHelper.cos(g);
        float i = MathHelper.sin(g) * 0.15f;
        this.body.pitch = this.lerpAngleDegrees(this.body.pitch, -0.15f + 0.075f * h);
        this.body.pivotY -= i;
        this.head.pitch = this.lerpAngleDegrees(this.head.pitch, -this.body.pitch);
        this.topGills.pitch = this.lerpAngleDegrees(this.topGills.pitch, 0.2f * h);
        this.leftGills.yaw = this.lerpAngleDegrees(this.leftGills.yaw, -0.3f * h - 0.19f);
        this.rightGills.yaw = this.lerpAngleDegrees(this.rightGills.yaw, -this.leftGills.yaw);
        this.setAngles(this.leftHindLeg, 2.3561945f - h * 0.11f, 0.47123894f, 1.7278761f);
        this.setAngles(this.leftFrontLeg, 0.7853982f - h * 0.2f, 2.042035f, 0.0f);
        this.copyLegAngles();
        this.tail.yaw = this.lerpAngleDegrees(this.tail.yaw, 0.5f * h);
        this.head.yaw = this.lerpAngleDegrees(this.head.yaw, 0.0f);
        this.head.roll = this.lerpAngleDegrees(this.head.roll, 0.0f);
    }

    private void setMovingInWaterAngles(float animationProgress, float headPitch) {
        float h = animationProgress * 0.33f;
        float i = MathHelper.sin(h);
        float j = MathHelper.cos(h);
        float k = 0.13f * i;
        this.body.pitch = this.lerpAngleDegrees(0.1f, this.body.pitch, headPitch * ((float)Math.PI / 180) + k);
        this.head.pitch = -k * 1.8f;
        this.body.pivotY -= 0.45f * j;
        this.topGills.pitch = this.lerpAngleDegrees(this.topGills.pitch, -0.5f * i - 0.8f);
        this.leftGills.yaw = this.lerpAngleDegrees(this.leftGills.yaw, 0.3f * i + 0.9f);
        this.rightGills.yaw = this.lerpAngleDegrees(this.rightGills.yaw, -this.leftGills.yaw);
        this.tail.yaw = this.lerpAngleDegrees(this.tail.yaw, 0.3f * MathHelper.cos(h * 0.9f));
        this.setAngles(this.leftHindLeg, 1.8849558f, -0.4f * i, 1.5707964f);
        this.setAngles(this.leftFrontLeg, 1.8849558f, -0.2f * j - 0.1f, 1.5707964f);
        this.copyLegAngles();
        this.head.yaw = this.lerpAngleDegrees(this.head.yaw, 0.0f);
        this.head.roll = this.lerpAngleDegrees(this.head.roll, 0.0f);
    }

    private void setPlayingDeadAngles(float headYaw) {
        this.setAngles(this.leftHindLeg, 1.4137167f, 1.0995574f, 0.7853982f);
        this.setAngles(this.leftFrontLeg, 0.7853982f, 2.042035f, 0.0f);
        this.body.pitch = this.lerpAngleDegrees(this.body.pitch, -0.15f);
        this.body.roll = this.lerpAngleDegrees(this.body.roll, 0.35f);
        this.copyLegAngles();
        this.body.yaw = this.lerpAngleDegrees(this.body.yaw, headYaw * ((float)Math.PI / 180));
        this.head.pitch = this.lerpAngleDegrees(this.head.pitch, 0.0f);
        this.head.yaw = this.lerpAngleDegrees(this.head.yaw, 0.0f);
        this.head.roll = this.lerpAngleDegrees(this.head.roll, 0.0f);
        this.tail.yaw = this.lerpAngleDegrees(this.tail.yaw, 0.0f);
        this.setAngles(this.topGills, 0.0f, 0.0f, 0.0f);
        this.setAngles(this.leftGills, 0.0f, 0.0f, 0.0f);
        this.setAngles(this.rightGills, 0.0f, 0.0f, 0.0f);
    }

    private void copyLegAngles() {
        this.setAngles(this.rightHindLeg, this.leftHindLeg.pitch, -this.leftHindLeg.yaw, -this.leftHindLeg.roll);
        this.setAngles(this.rightFrontLeg, this.leftFrontLeg.pitch, -this.leftFrontLeg.yaw, -this.leftFrontLeg.roll);
    }
}

