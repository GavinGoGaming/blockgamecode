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
import net.minecraft.client.model.TexturedModelData;
import net.minecraft.client.render.entity.model.EntityModelPartNames;
import net.minecraft.client.render.entity.model.QuadrupedEntityModel;
import net.minecraft.entity.Entity;

@Environment(value=EnvType.CLIENT)
public class PigEntityModel<T extends Entity>
extends QuadrupedEntityModel<T> {
    public PigEntityModel(ModelPart root) {
        super(root, false, 4.0f, 4.0f, 2.0f, 2.0f, 24);
    }

    public static TexturedModelData getTexturedModelData(Dilation dilation) {
        ModelData lv = QuadrupedEntityModel.getModelData(6, dilation);
        ModelPartData lv2 = lv.getRoot();
        lv2.addChild(EntityModelPartNames.HEAD, ModelPartBuilder.create().uv(0, 0).cuboid(-4.0f, -4.0f, -8.0f, 8.0f, 8.0f, 8.0f, dilation).uv(16, 16).cuboid(-2.0f, 0.0f, -9.0f, 4.0f, 3.0f, 1.0f, dilation), ModelTransform.pivot(0.0f, 12.0f, -6.0f));
        return TexturedModelData.of(lv, 64, 32);
    }
}

