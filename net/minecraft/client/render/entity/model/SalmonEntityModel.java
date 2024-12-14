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
public class SalmonEntityModel<T extends Entity>
extends SinglePartEntityModel<T> {
    private static final String BODY_FRONT = "body_front";
    private static final String BODY_BACK = "body_back";
    private final ModelPart root;
    private final ModelPart tail;

    public SalmonEntityModel(ModelPart root) {
        this.root = root;
        this.tail = root.getChild(BODY_BACK);
    }

    public static TexturedModelData getTexturedModelData() {
        ModelData lv = new ModelData();
        ModelPartData lv2 = lv.getRoot();
        int i = 20;
        ModelPartData lv3 = lv2.addChild(BODY_FRONT, ModelPartBuilder.create().uv(0, 0).cuboid(-1.5f, -2.5f, 0.0f, 3.0f, 5.0f, 8.0f), ModelTransform.pivot(0.0f, 20.0f, 0.0f));
        ModelPartData lv4 = lv2.addChild(BODY_BACK, ModelPartBuilder.create().uv(0, 13).cuboid(-1.5f, -2.5f, 0.0f, 3.0f, 5.0f, 8.0f), ModelTransform.pivot(0.0f, 20.0f, 8.0f));
        lv2.addChild(EntityModelPartNames.HEAD, ModelPartBuilder.create().uv(22, 0).cuboid(-1.0f, -2.0f, -3.0f, 2.0f, 4.0f, 3.0f), ModelTransform.pivot(0.0f, 20.0f, 0.0f));
        lv4.addChild(EntityModelPartNames.BACK_FIN, ModelPartBuilder.create().uv(20, 10).cuboid(0.0f, -2.5f, 0.0f, 0.0f, 5.0f, 6.0f), ModelTransform.pivot(0.0f, 0.0f, 8.0f));
        lv3.addChild("top_front_fin", ModelPartBuilder.create().uv(2, 1).cuboid(0.0f, 0.0f, 0.0f, 0.0f, 2.0f, 3.0f), ModelTransform.pivot(0.0f, -4.5f, 5.0f));
        lv4.addChild("top_back_fin", ModelPartBuilder.create().uv(0, 2).cuboid(0.0f, 0.0f, 0.0f, 0.0f, 2.0f, 4.0f), ModelTransform.pivot(0.0f, -4.5f, -1.0f));
        lv2.addChild(EntityModelPartNames.RIGHT_FIN, ModelPartBuilder.create().uv(-4, 0).cuboid(-2.0f, 0.0f, 0.0f, 2.0f, 0.0f, 2.0f), ModelTransform.of(-1.5f, 21.5f, 0.0f, 0.0f, 0.0f, -0.7853982f));
        lv2.addChild(EntityModelPartNames.LEFT_FIN, ModelPartBuilder.create().uv(0, 0).cuboid(0.0f, 0.0f, 0.0f, 2.0f, 0.0f, 2.0f), ModelTransform.of(1.5f, 21.5f, 0.0f, 0.0f, 0.0f, 0.7853982f));
        return TexturedModelData.of(lv, 32, 32);
    }

    @Override
    public ModelPart getPart() {
        return this.root;
    }

    @Override
    public void setAngles(T entity, float limbAngle, float limbDistance, float animationProgress, float headYaw, float headPitch) {
        float k = 1.0f;
        float l = 1.0f;
        if (!((Entity)entity).isTouchingWater()) {
            k = 1.3f;
            l = 1.7f;
        }
        this.tail.yaw = -k * 0.25f * MathHelper.sin(l * 0.6f * animationProgress);
    }
}

