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
import net.minecraft.client.model.TexturedModelData;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.client.render.entity.model.AnimalModel;
import net.minecraft.client.render.entity.model.EntityModelPartNames;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.math.Vec3d;

@Environment(value=EnvType.CLIENT)
public class ElytraEntityModel<T extends LivingEntity>
extends AnimalModel<T> {
    private final ModelPart rightWing;
    private final ModelPart leftWing;

    public ElytraEntityModel(ModelPart root) {
        this.leftWing = root.getChild(EntityModelPartNames.LEFT_WING);
        this.rightWing = root.getChild(EntityModelPartNames.RIGHT_WING);
    }

    public static TexturedModelData getTexturedModelData() {
        ModelData lv = new ModelData();
        ModelPartData lv2 = lv.getRoot();
        Dilation lv3 = new Dilation(1.0f);
        lv2.addChild(EntityModelPartNames.LEFT_WING, ModelPartBuilder.create().uv(22, 0).cuboid(-10.0f, 0.0f, 0.0f, 10.0f, 20.0f, 2.0f, lv3), ModelTransform.of(5.0f, 0.0f, 0.0f, 0.2617994f, 0.0f, -0.2617994f));
        lv2.addChild(EntityModelPartNames.RIGHT_WING, ModelPartBuilder.create().uv(22, 0).mirrored().cuboid(0.0f, 0.0f, 0.0f, 10.0f, 20.0f, 2.0f, lv3), ModelTransform.of(-5.0f, 0.0f, 0.0f, 0.2617994f, 0.0f, 0.2617994f));
        return TexturedModelData.of(lv, 64, 32);
    }

    @Override
    protected Iterable<ModelPart> getHeadParts() {
        return ImmutableList.of();
    }

    @Override
    protected Iterable<ModelPart> getBodyParts() {
        return ImmutableList.of(this.leftWing, this.rightWing);
    }

    @Override
    public void setAngles(T arg, float f, float g, float h, float i, float j) {
        float k = 0.2617994f;
        float l = -0.2617994f;
        float m = 0.0f;
        float n = 0.0f;
        if (((LivingEntity)arg).isFallFlying()) {
            float o = 1.0f;
            Vec3d lv = ((Entity)arg).getVelocity();
            if (lv.y < 0.0) {
                Vec3d lv2 = lv.normalize();
                o = 1.0f - (float)Math.pow(-lv2.y, 1.5);
            }
            k = o * 0.34906584f + (1.0f - o) * k;
            l = o * -1.5707964f + (1.0f - o) * l;
        } else if (((Entity)arg).isInSneakingPose()) {
            k = 0.6981317f;
            l = -0.7853982f;
            m = 3.0f;
            n = 0.08726646f;
        }
        this.leftWing.pivotY = m;
        if (arg instanceof AbstractClientPlayerEntity) {
            AbstractClientPlayerEntity lv3 = (AbstractClientPlayerEntity)arg;
            lv3.elytraPitch += (k - lv3.elytraPitch) * 0.1f;
            lv3.elytraYaw += (n - lv3.elytraYaw) * 0.1f;
            lv3.elytraRoll += (l - lv3.elytraRoll) * 0.1f;
            this.leftWing.pitch = lv3.elytraPitch;
            this.leftWing.yaw = lv3.elytraYaw;
            this.leftWing.roll = lv3.elytraRoll;
        } else {
            this.leftWing.pitch = k;
            this.leftWing.roll = l;
            this.leftWing.yaw = n;
        }
        this.rightWing.yaw = -this.leftWing.yaw;
        this.rightWing.pivotY = this.leftWing.pivotY;
        this.rightWing.pitch = this.leftWing.pitch;
        this.rightWing.roll = -this.leftWing.roll;
    }
}

