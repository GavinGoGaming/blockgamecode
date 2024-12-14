/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 * 
 * Could not load the following classes:
 *  net.fabricmc.api.EnvType
 *  net.fabricmc.api.Environment
 */
package net.minecraft.client.render.entity.model;

import com.google.common.collect.ImmutableList;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.model.Dilation;
import net.minecraft.client.model.ModelData;
import net.minecraft.client.model.ModelPart;
import net.minecraft.client.model.ModelPartBuilder;
import net.minecraft.client.model.ModelPartData;
import net.minecraft.client.model.ModelTransform;
import net.minecraft.client.render.entity.model.AnimalModel;
import net.minecraft.client.render.entity.model.EntityModelPartNames;
import net.minecraft.entity.Entity;
import net.minecraft.entity.passive.AbstractHorseEntity;
import net.minecraft.entity.passive.PassiveEntity;
import net.minecraft.util.math.MathHelper;

@Environment(value=EnvType.CLIENT)
public class HorseEntityModel<T extends AbstractHorseEntity>
extends AnimalModel<T> {
    private static final float EATING_GRASS_ANIMATION_HEAD_BASE_PITCH = 2.1816616f;
    private static final float ANGRY_ANIMATION_FRONT_LEG_PITCH_MULTIPLIER = 1.0471976f;
    private static final float ANGRY_ANIMATION_BODY_PITCH_MULTIPLIER = 0.7853982f;
    private static final float HEAD_TAIL_BASE_PITCH = 0.5235988f;
    private static final float ANGRY_ANIMATION_HIND_LEG_PITCH_MULTIPLIER = 0.2617994f;
    protected static final String HEAD_PARTS = "head_parts";
    private static final String LEFT_HIND_BABY_LEG = "left_hind_baby_leg";
    private static final String RIGHT_HIND_BABY_LEG = "right_hind_baby_leg";
    private static final String LEFT_FRONT_BABY_LEG = "left_front_baby_leg";
    private static final String RIGHT_FRONT_BABY_LEG = "right_front_baby_leg";
    private static final String SADDLE = "saddle";
    private static final String LEFT_SADDLE_MOUTH = "left_saddle_mouth";
    private static final String LEFT_SADDLE_LINE = "left_saddle_line";
    private static final String RIGHT_SADDLE_MOUTH = "right_saddle_mouth";
    private static final String RIGHT_SADDLE_LINE = "right_saddle_line";
    private static final String HEAD_SADDLE = "head_saddle";
    private static final String MOUTH_SADDLE_WRAP = "mouth_saddle_wrap";
    protected final ModelPart body;
    protected final ModelPart head;
    private final ModelPart rightHindLeg;
    private final ModelPart leftHindLeg;
    private final ModelPart rightFrontLeg;
    private final ModelPart leftFrontLeg;
    private final ModelPart rightHindBabyLeg;
    private final ModelPart leftHindBabyLeg;
    private final ModelPart rightFrontBabyLeg;
    private final ModelPart leftFrontBabyLeg;
    private final ModelPart tail;
    private final ModelPart[] saddle;
    private final ModelPart[] straps;

    public HorseEntityModel(ModelPart root) {
        super(true, 16.2f, 1.36f, 2.7272f, 2.0f, 20.0f);
        this.body = root.getChild(EntityModelPartNames.BODY);
        this.head = root.getChild(HEAD_PARTS);
        this.rightHindLeg = root.getChild(EntityModelPartNames.RIGHT_HIND_LEG);
        this.leftHindLeg = root.getChild(EntityModelPartNames.LEFT_HIND_LEG);
        this.rightFrontLeg = root.getChild(EntityModelPartNames.RIGHT_FRONT_LEG);
        this.leftFrontLeg = root.getChild(EntityModelPartNames.LEFT_FRONT_LEG);
        this.rightHindBabyLeg = root.getChild(RIGHT_HIND_BABY_LEG);
        this.leftHindBabyLeg = root.getChild(LEFT_HIND_BABY_LEG);
        this.rightFrontBabyLeg = root.getChild(RIGHT_FRONT_BABY_LEG);
        this.leftFrontBabyLeg = root.getChild(LEFT_FRONT_BABY_LEG);
        this.tail = this.body.getChild(EntityModelPartNames.TAIL);
        ModelPart lv = this.body.getChild(SADDLE);
        ModelPart lv2 = this.head.getChild(LEFT_SADDLE_MOUTH);
        ModelPart lv3 = this.head.getChild(RIGHT_SADDLE_MOUTH);
        ModelPart lv4 = this.head.getChild(LEFT_SADDLE_LINE);
        ModelPart lv5 = this.head.getChild(RIGHT_SADDLE_LINE);
        ModelPart lv6 = this.head.getChild(HEAD_SADDLE);
        ModelPart lv7 = this.head.getChild(MOUTH_SADDLE_WRAP);
        this.saddle = new ModelPart[]{lv, lv2, lv3, lv6, lv7};
        this.straps = new ModelPart[]{lv4, lv5};
    }

    public static ModelData getModelData(Dilation dilation) {
        ModelData lv = new ModelData();
        ModelPartData lv2 = lv.getRoot();
        ModelPartData lv3 = lv2.addChild(EntityModelPartNames.BODY, ModelPartBuilder.create().uv(0, 32).cuboid(-5.0f, -8.0f, -17.0f, 10.0f, 10.0f, 22.0f, new Dilation(0.05f)), ModelTransform.pivot(0.0f, 11.0f, 5.0f));
        ModelPartData lv4 = lv2.addChild(HEAD_PARTS, ModelPartBuilder.create().uv(0, 35).cuboid(-2.05f, -6.0f, -2.0f, 4.0f, 12.0f, 7.0f), ModelTransform.of(0.0f, 4.0f, -12.0f, 0.5235988f, 0.0f, 0.0f));
        ModelPartData lv5 = lv4.addChild(EntityModelPartNames.HEAD, ModelPartBuilder.create().uv(0, 13).cuboid(-3.0f, -11.0f, -2.0f, 6.0f, 5.0f, 7.0f, dilation), ModelTransform.NONE);
        lv4.addChild(EntityModelPartNames.MANE, ModelPartBuilder.create().uv(56, 36).cuboid(-1.0f, -11.0f, 5.01f, 2.0f, 16.0f, 2.0f, dilation), ModelTransform.NONE);
        lv4.addChild("upper_mouth", ModelPartBuilder.create().uv(0, 25).cuboid(-2.0f, -11.0f, -7.0f, 4.0f, 5.0f, 5.0f, dilation), ModelTransform.NONE);
        lv2.addChild(EntityModelPartNames.LEFT_HIND_LEG, ModelPartBuilder.create().uv(48, 21).mirrored().cuboid(-3.0f, -1.01f, -1.0f, 4.0f, 11.0f, 4.0f, dilation), ModelTransform.pivot(4.0f, 14.0f, 7.0f));
        lv2.addChild(EntityModelPartNames.RIGHT_HIND_LEG, ModelPartBuilder.create().uv(48, 21).cuboid(-1.0f, -1.01f, -1.0f, 4.0f, 11.0f, 4.0f, dilation), ModelTransform.pivot(-4.0f, 14.0f, 7.0f));
        lv2.addChild(EntityModelPartNames.LEFT_FRONT_LEG, ModelPartBuilder.create().uv(48, 21).mirrored().cuboid(-3.0f, -1.01f, -1.9f, 4.0f, 11.0f, 4.0f, dilation), ModelTransform.pivot(4.0f, 14.0f, -12.0f));
        lv2.addChild(EntityModelPartNames.RIGHT_FRONT_LEG, ModelPartBuilder.create().uv(48, 21).cuboid(-1.0f, -1.01f, -1.9f, 4.0f, 11.0f, 4.0f, dilation), ModelTransform.pivot(-4.0f, 14.0f, -12.0f));
        Dilation lv6 = dilation.add(0.0f, 5.5f, 0.0f);
        lv2.addChild(LEFT_HIND_BABY_LEG, ModelPartBuilder.create().uv(48, 21).mirrored().cuboid(-3.0f, -1.01f, -1.0f, 4.0f, 11.0f, 4.0f, lv6), ModelTransform.pivot(4.0f, 14.0f, 7.0f));
        lv2.addChild(RIGHT_HIND_BABY_LEG, ModelPartBuilder.create().uv(48, 21).cuboid(-1.0f, -1.01f, -1.0f, 4.0f, 11.0f, 4.0f, lv6), ModelTransform.pivot(-4.0f, 14.0f, 7.0f));
        lv2.addChild(LEFT_FRONT_BABY_LEG, ModelPartBuilder.create().uv(48, 21).mirrored().cuboid(-3.0f, -1.01f, -1.9f, 4.0f, 11.0f, 4.0f, lv6), ModelTransform.pivot(4.0f, 14.0f, -12.0f));
        lv2.addChild(RIGHT_FRONT_BABY_LEG, ModelPartBuilder.create().uv(48, 21).cuboid(-1.0f, -1.01f, -1.9f, 4.0f, 11.0f, 4.0f, lv6), ModelTransform.pivot(-4.0f, 14.0f, -12.0f));
        lv3.addChild(EntityModelPartNames.TAIL, ModelPartBuilder.create().uv(42, 36).cuboid(-1.5f, 0.0f, 0.0f, 3.0f, 14.0f, 4.0f, dilation), ModelTransform.of(0.0f, -5.0f, 2.0f, 0.5235988f, 0.0f, 0.0f));
        lv3.addChild(SADDLE, ModelPartBuilder.create().uv(26, 0).cuboid(-5.0f, -8.0f, -9.0f, 10.0f, 9.0f, 9.0f, new Dilation(0.5f)), ModelTransform.NONE);
        lv4.addChild(LEFT_SADDLE_MOUTH, ModelPartBuilder.create().uv(29, 5).cuboid(2.0f, -9.0f, -6.0f, 1.0f, 2.0f, 2.0f, dilation), ModelTransform.NONE);
        lv4.addChild(RIGHT_SADDLE_MOUTH, ModelPartBuilder.create().uv(29, 5).cuboid(-3.0f, -9.0f, -6.0f, 1.0f, 2.0f, 2.0f, dilation), ModelTransform.NONE);
        lv4.addChild(LEFT_SADDLE_LINE, ModelPartBuilder.create().uv(32, 2).cuboid(3.1f, -6.0f, -8.0f, 0.0f, 3.0f, 16.0f), ModelTransform.rotation(-0.5235988f, 0.0f, 0.0f));
        lv4.addChild(RIGHT_SADDLE_LINE, ModelPartBuilder.create().uv(32, 2).cuboid(-3.1f, -6.0f, -8.0f, 0.0f, 3.0f, 16.0f), ModelTransform.rotation(-0.5235988f, 0.0f, 0.0f));
        lv4.addChild(HEAD_SADDLE, ModelPartBuilder.create().uv(1, 1).cuboid(-3.0f, -11.0f, -1.9f, 6.0f, 5.0f, 6.0f, new Dilation(0.22f)), ModelTransform.NONE);
        lv4.addChild(MOUTH_SADDLE_WRAP, ModelPartBuilder.create().uv(19, 0).cuboid(-2.0f, -11.0f, -4.0f, 4.0f, 5.0f, 2.0f, new Dilation(0.2f)), ModelTransform.NONE);
        lv5.addChild(EntityModelPartNames.LEFT_EAR, ModelPartBuilder.create().uv(19, 16).cuboid(0.55f, -13.0f, 4.0f, 2.0f, 3.0f, 1.0f, new Dilation(-0.001f)), ModelTransform.NONE);
        lv5.addChild(EntityModelPartNames.RIGHT_EAR, ModelPartBuilder.create().uv(19, 16).cuboid(-2.55f, -13.0f, 4.0f, 2.0f, 3.0f, 1.0f, new Dilation(-0.001f)), ModelTransform.NONE);
        return lv;
    }

    @Override
    public void setAngles(T arg, float f, float g, float h, float i, float j) {
        boolean bl = ((AbstractHorseEntity)arg).isSaddled();
        boolean bl2 = ((Entity)arg).hasPassengers();
        for (ModelPart lv : this.saddle) {
            lv.visible = bl;
        }
        for (ModelPart lv : this.straps) {
            lv.visible = bl2 && bl;
        }
        this.body.pivotY = 11.0f;
    }

    @Override
    public Iterable<ModelPart> getHeadParts() {
        return ImmutableList.of(this.head);
    }

    @Override
    protected Iterable<ModelPart> getBodyParts() {
        return ImmutableList.of(this.body, this.rightHindLeg, this.leftHindLeg, this.rightFrontLeg, this.leftFrontLeg, this.rightHindBabyLeg, this.leftHindBabyLeg, this.rightFrontBabyLeg, this.leftFrontBabyLeg);
    }

    @Override
    public void animateModel(T arg, float f, float g, float h) {
        super.animateModel(arg, f, g, h);
        float i = MathHelper.lerpAngleDegrees(h, ((AbstractHorseEntity)arg).prevBodyYaw, ((AbstractHorseEntity)arg).bodyYaw);
        float j = MathHelper.lerpAngleDegrees(h, ((AbstractHorseEntity)arg).prevHeadYaw, ((AbstractHorseEntity)arg).headYaw);
        float k = MathHelper.lerp(h, ((AbstractHorseEntity)arg).prevPitch, ((Entity)arg).getPitch());
        float l = j - i;
        float m = k * ((float)Math.PI / 180);
        if (l > 20.0f) {
            l = 20.0f;
        }
        if (l < -20.0f) {
            l = -20.0f;
        }
        if (g > 0.2f) {
            m += MathHelper.cos(f * 0.8f) * 0.15f * g;
        }
        float n = ((AbstractHorseEntity)arg).getEatingGrassAnimationProgress(h);
        float o = ((AbstractHorseEntity)arg).getAngryAnimationProgress(h);
        float p = 1.0f - o;
        float q = ((AbstractHorseEntity)arg).getEatingAnimationProgress(h);
        boolean bl = ((AbstractHorseEntity)arg).tailWagTicks != 0;
        float r = (float)((AbstractHorseEntity)arg).age + h;
        this.head.pivotY = 4.0f;
        this.head.pivotZ = -12.0f;
        this.body.pitch = 0.0f;
        this.head.pitch = 0.5235988f + m;
        this.head.yaw = l * ((float)Math.PI / 180);
        float s = ((Entity)arg).isTouchingWater() ? 0.2f : 1.0f;
        float t = MathHelper.cos(s * f * 0.6662f + (float)Math.PI);
        float u = t * 0.8f * g;
        float v = (1.0f - Math.max(o, n)) * (0.5235988f + m + q * MathHelper.sin(r) * 0.05f);
        this.head.pitch = o * (0.2617994f + m) + n * (2.1816616f + MathHelper.sin(r) * 0.05f) + v;
        this.head.yaw = o * l * ((float)Math.PI / 180) + (1.0f - Math.max(o, n)) * this.head.yaw;
        this.head.pivotY = o * -4.0f + n * 11.0f + (1.0f - Math.max(o, n)) * this.head.pivotY;
        this.head.pivotZ = o * -4.0f + n * -12.0f + (1.0f - Math.max(o, n)) * this.head.pivotZ;
        this.body.pitch = o * -0.7853982f + p * this.body.pitch;
        float w = 0.2617994f * o;
        float x = MathHelper.cos(r * 0.6f + (float)Math.PI);
        this.leftFrontLeg.pivotY = 2.0f * o + 14.0f * p;
        this.leftFrontLeg.pivotZ = -6.0f * o - 10.0f * p;
        this.rightFrontLeg.pivotY = this.leftFrontLeg.pivotY;
        this.rightFrontLeg.pivotZ = this.leftFrontLeg.pivotZ;
        float y = (-1.0471976f + x) * o + u * p;
        float z = (-1.0471976f - x) * o - u * p;
        this.leftHindLeg.pitch = w - t * 0.5f * g * p;
        this.rightHindLeg.pitch = w + t * 0.5f * g * p;
        this.leftFrontLeg.pitch = y;
        this.rightFrontLeg.pitch = z;
        this.tail.pitch = 0.5235988f + g * 0.75f;
        this.tail.pivotY = -5.0f + g;
        this.tail.pivotZ = 2.0f + g * 2.0f;
        this.tail.yaw = bl ? MathHelper.cos(r * 0.7f) : 0.0f;
        this.rightHindBabyLeg.pivotY = this.rightHindLeg.pivotY;
        this.rightHindBabyLeg.pivotZ = this.rightHindLeg.pivotZ;
        this.rightHindBabyLeg.pitch = this.rightHindLeg.pitch;
        this.leftHindBabyLeg.pivotY = this.leftHindLeg.pivotY;
        this.leftHindBabyLeg.pivotZ = this.leftHindLeg.pivotZ;
        this.leftHindBabyLeg.pitch = this.leftHindLeg.pitch;
        this.rightFrontBabyLeg.pivotY = this.rightFrontLeg.pivotY;
        this.rightFrontBabyLeg.pivotZ = this.rightFrontLeg.pivotZ;
        this.rightFrontBabyLeg.pitch = this.rightFrontLeg.pitch;
        this.leftFrontBabyLeg.pivotY = this.leftFrontLeg.pivotY;
        this.leftFrontBabyLeg.pivotZ = this.leftFrontLeg.pivotZ;
        this.leftFrontBabyLeg.pitch = this.leftFrontLeg.pitch;
        boolean bl2 = ((PassiveEntity)arg).isBaby();
        this.rightHindLeg.visible = !bl2;
        this.leftHindLeg.visible = !bl2;
        this.rightFrontLeg.visible = !bl2;
        this.leftFrontLeg.visible = !bl2;
        this.rightHindBabyLeg.visible = bl2;
        this.leftHindBabyLeg.visible = bl2;
        this.rightFrontBabyLeg.visible = bl2;
        this.leftFrontBabyLeg.visible = bl2;
        this.body.pivotY = bl2 ? 10.8f : 0.0f;
    }
}

