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
import net.minecraft.client.model.ModelData;
import net.minecraft.client.model.ModelPart;
import net.minecraft.client.model.ModelPartBuilder;
import net.minecraft.client.model.ModelPartData;
import net.minecraft.client.model.ModelTransform;
import net.minecraft.client.model.TexturedModelData;
import net.minecraft.client.render.entity.model.AnimalModel;
import net.minecraft.client.render.entity.model.EntityModelPartNames;
import net.minecraft.entity.Entity;
import net.minecraft.entity.passive.TadpoleEntity;
import net.minecraft.util.math.MathHelper;

@Environment(value=EnvType.CLIENT)
public class TadpoleEntityModel<T extends TadpoleEntity>
extends AnimalModel<T> {
    private final ModelPart root;
    private final ModelPart tail;

    public TadpoleEntityModel(ModelPart root) {
        super(true, 8.0f, 3.35f);
        this.root = root;
        this.tail = root.getChild(EntityModelPartNames.TAIL);
    }

    public static TexturedModelData getTexturedModelData() {
        ModelData lv = new ModelData();
        ModelPartData lv2 = lv.getRoot();
        float f = 0.0f;
        float g = 22.0f;
        float h = -3.0f;
        lv2.addChild(EntityModelPartNames.BODY, ModelPartBuilder.create().uv(0, 0).cuboid(-1.5f, -1.0f, 0.0f, 3.0f, 2.0f, 3.0f), ModelTransform.pivot(0.0f, 22.0f, -3.0f));
        lv2.addChild(EntityModelPartNames.TAIL, ModelPartBuilder.create().uv(0, 0).cuboid(0.0f, -1.0f, 0.0f, 0.0f, 2.0f, 7.0f), ModelTransform.pivot(0.0f, 22.0f, 0.0f));
        return TexturedModelData.of(lv, 16, 16);
    }

    @Override
    protected Iterable<ModelPart> getHeadParts() {
        return ImmutableList.of(this.root);
    }

    @Override
    protected Iterable<ModelPart> getBodyParts() {
        return ImmutableList.of(this.tail);
    }

    @Override
    public void setAngles(T arg, float f, float g, float h, float i, float j) {
        float k = ((Entity)arg).isTouchingWater() ? 1.0f : 1.5f;
        this.tail.yaw = -k * 0.25f * MathHelper.sin(0.3f * h);
    }
}

