/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 * 
 * Could not load the following classes:
 *  net.fabricmc.api.EnvType
 *  net.fabricmc.api.Environment
 */
package net.minecraft.client.render.entity.model;

import java.util.Arrays;
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
public class BlazeEntityModel<T extends Entity>
extends SinglePartEntityModel<T> {
    private final ModelPart root;
    private final ModelPart[] rods;
    private final ModelPart head;

    public BlazeEntityModel(ModelPart root) {
        this.root = root;
        this.head = root.getChild(EntityModelPartNames.HEAD);
        this.rods = new ModelPart[12];
        Arrays.setAll(this.rods, index -> root.getChild(BlazeEntityModel.getRodName(index)));
    }

    private static String getRodName(int index) {
        return "part" + index;
    }

    public static TexturedModelData getTexturedModelData() {
        float j;
        float h;
        float g;
        int i;
        ModelData lv = new ModelData();
        ModelPartData lv2 = lv.getRoot();
        lv2.addChild(EntityModelPartNames.HEAD, ModelPartBuilder.create().uv(0, 0).cuboid(-4.0f, -4.0f, -4.0f, 8.0f, 8.0f, 8.0f), ModelTransform.NONE);
        float f = 0.0f;
        ModelPartBuilder lv3 = ModelPartBuilder.create().uv(0, 16).cuboid(0.0f, 0.0f, 0.0f, 2.0f, 8.0f, 2.0f);
        for (i = 0; i < 4; ++i) {
            g = MathHelper.cos(f) * 9.0f;
            h = -2.0f + MathHelper.cos((float)(i * 2) * 0.25f);
            j = MathHelper.sin(f) * 9.0f;
            lv2.addChild(BlazeEntityModel.getRodName(i), lv3, ModelTransform.pivot(g, h, j));
            f += 1.5707964f;
        }
        f = 0.7853982f;
        for (i = 4; i < 8; ++i) {
            g = MathHelper.cos(f) * 7.0f;
            h = 2.0f + MathHelper.cos((float)(i * 2) * 0.25f);
            j = MathHelper.sin(f) * 7.0f;
            lv2.addChild(BlazeEntityModel.getRodName(i), lv3, ModelTransform.pivot(g, h, j));
            f += 1.5707964f;
        }
        f = 0.47123894f;
        for (i = 8; i < 12; ++i) {
            g = MathHelper.cos(f) * 5.0f;
            h = 11.0f + MathHelper.cos((float)i * 1.5f * 0.5f);
            j = MathHelper.sin(f) * 5.0f;
            lv2.addChild(BlazeEntityModel.getRodName(i), lv3, ModelTransform.pivot(g, h, j));
            f += 1.5707964f;
        }
        return TexturedModelData.of(lv, 64, 32);
    }

    @Override
    public ModelPart getPart() {
        return this.root;
    }

    @Override
    public void setAngles(T entity, float limbAngle, float limbDistance, float animationProgress, float headYaw, float headPitch) {
        int l;
        float k = animationProgress * (float)Math.PI * -0.1f;
        for (l = 0; l < 4; ++l) {
            this.rods[l].pivotY = -2.0f + MathHelper.cos(((float)(l * 2) + animationProgress) * 0.25f);
            this.rods[l].pivotX = MathHelper.cos(k) * 9.0f;
            this.rods[l].pivotZ = MathHelper.sin(k) * 9.0f;
            k += 1.5707964f;
        }
        k = 0.7853982f + animationProgress * (float)Math.PI * 0.03f;
        for (l = 4; l < 8; ++l) {
            this.rods[l].pivotY = 2.0f + MathHelper.cos(((float)(l * 2) + animationProgress) * 0.25f);
            this.rods[l].pivotX = MathHelper.cos(k) * 7.0f;
            this.rods[l].pivotZ = MathHelper.sin(k) * 7.0f;
            k += 1.5707964f;
        }
        k = 0.47123894f + animationProgress * (float)Math.PI * -0.05f;
        for (l = 8; l < 12; ++l) {
            this.rods[l].pivotY = 11.0f + MathHelper.cos(((float)l * 1.5f + animationProgress) * 0.5f);
            this.rods[l].pivotX = MathHelper.cos(k) * 5.0f;
            this.rods[l].pivotZ = MathHelper.sin(k) * 5.0f;
            k += 1.5707964f;
        }
        this.head.yaw = headYaw * ((float)Math.PI / 180);
        this.head.pitch = headPitch * ((float)Math.PI / 180);
    }
}

