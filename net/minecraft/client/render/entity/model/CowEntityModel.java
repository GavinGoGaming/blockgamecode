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
import net.minecraft.client.render.entity.model.QuadrupedEntityModel;
import net.minecraft.entity.Entity;

@Environment(value=EnvType.CLIENT)
public class CowEntityModel<T extends Entity>
extends QuadrupedEntityModel<T> {
    public CowEntityModel(ModelPart root) {
        super(root, false, 10.0f, 4.0f, 2.0f, 2.0f, 24);
    }

    public static TexturedModelData getTexturedModelData() {
        ModelData lv = new ModelData();
        ModelPartData lv2 = lv.getRoot();
        int i = 12;
        lv2.addChild(EntityModelPartNames.HEAD, ModelPartBuilder.create().uv(0, 0).cuboid(-4.0f, -4.0f, -6.0f, 8.0f, 8.0f, 6.0f).uv(22, 0).cuboid(EntityModelPartNames.RIGHT_HORN, -5.0f, -5.0f, -4.0f, 1.0f, 3.0f, 1.0f).uv(22, 0).cuboid(EntityModelPartNames.LEFT_HORN, 4.0f, -5.0f, -4.0f, 1.0f, 3.0f, 1.0f), ModelTransform.pivot(0.0f, 4.0f, -8.0f));
        lv2.addChild(EntityModelPartNames.BODY, ModelPartBuilder.create().uv(18, 4).cuboid(-6.0f, -10.0f, -7.0f, 12.0f, 18.0f, 10.0f).uv(52, 0).cuboid(-2.0f, 2.0f, -8.0f, 4.0f, 6.0f, 1.0f), ModelTransform.of(0.0f, 5.0f, 2.0f, 1.5707964f, 0.0f, 0.0f));
        ModelPartBuilder lv3 = ModelPartBuilder.create().uv(0, 16).cuboid(-2.0f, 0.0f, -2.0f, 4.0f, 12.0f, 4.0f);
        lv2.addChild(EntityModelPartNames.RIGHT_HIND_LEG, lv3, ModelTransform.pivot(-4.0f, 12.0f, 7.0f));
        lv2.addChild(EntityModelPartNames.LEFT_HIND_LEG, lv3, ModelTransform.pivot(4.0f, 12.0f, 7.0f));
        lv2.addChild(EntityModelPartNames.RIGHT_FRONT_LEG, lv3, ModelTransform.pivot(-4.0f, 12.0f, -6.0f));
        lv2.addChild(EntityModelPartNames.LEFT_FRONT_LEG, lv3, ModelTransform.pivot(4.0f, 12.0f, -6.0f));
        return TexturedModelData.of(lv, 64, 32);
    }

    public ModelPart getHead() {
        return this.head;
    }
}

