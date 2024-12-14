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
public class CodEntityModel<T extends Entity>
extends SinglePartEntityModel<T> {
    private final ModelPart root;
    private final ModelPart tailFin;

    public CodEntityModel(ModelPart root) {
        this.root = root;
        this.tailFin = root.getChild(EntityModelPartNames.TAIL_FIN);
    }

    public static TexturedModelData getTexturedModelData() {
        ModelData lv = new ModelData();
        ModelPartData lv2 = lv.getRoot();
        int i = 22;
        lv2.addChild(EntityModelPartNames.BODY, ModelPartBuilder.create().uv(0, 0).cuboid(-1.0f, -2.0f, 0.0f, 2.0f, 4.0f, 7.0f), ModelTransform.pivot(0.0f, 22.0f, 0.0f));
        lv2.addChild(EntityModelPartNames.HEAD, ModelPartBuilder.create().uv(11, 0).cuboid(-1.0f, -2.0f, -3.0f, 2.0f, 4.0f, 3.0f), ModelTransform.pivot(0.0f, 22.0f, 0.0f));
        lv2.addChild(EntityModelPartNames.NOSE, ModelPartBuilder.create().uv(0, 0).cuboid(-1.0f, -2.0f, -1.0f, 2.0f, 3.0f, 1.0f), ModelTransform.pivot(0.0f, 22.0f, -3.0f));
        lv2.addChild(EntityModelPartNames.RIGHT_FIN, ModelPartBuilder.create().uv(22, 1).cuboid(-2.0f, 0.0f, -1.0f, 2.0f, 0.0f, 2.0f), ModelTransform.of(-1.0f, 23.0f, 0.0f, 0.0f, 0.0f, -0.7853982f));
        lv2.addChild(EntityModelPartNames.LEFT_FIN, ModelPartBuilder.create().uv(22, 4).cuboid(0.0f, 0.0f, -1.0f, 2.0f, 0.0f, 2.0f), ModelTransform.of(1.0f, 23.0f, 0.0f, 0.0f, 0.0f, 0.7853982f));
        lv2.addChild(EntityModelPartNames.TAIL_FIN, ModelPartBuilder.create().uv(22, 3).cuboid(0.0f, -2.0f, 0.0f, 0.0f, 4.0f, 4.0f), ModelTransform.pivot(0.0f, 22.0f, 7.0f));
        lv2.addChild(EntityModelPartNames.TOP_FIN, ModelPartBuilder.create().uv(20, -6).cuboid(0.0f, -1.0f, -1.0f, 0.0f, 1.0f, 6.0f), ModelTransform.pivot(0.0f, 20.0f, 0.0f));
        return TexturedModelData.of(lv, 32, 32);
    }

    @Override
    public ModelPart getPart() {
        return this.root;
    }

    @Override
    public void setAngles(T entity, float limbAngle, float limbDistance, float animationProgress, float headYaw, float headPitch) {
        float k = 1.0f;
        if (!((Entity)entity).isTouchingWater()) {
            k = 1.5f;
        }
        this.tailFin.yaw = -k * 0.45f * MathHelper.sin(0.6f * animationProgress);
    }
}

