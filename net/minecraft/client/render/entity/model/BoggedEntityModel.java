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
import net.minecraft.client.render.entity.model.BipedEntityModel;
import net.minecraft.client.render.entity.model.EntityModelPartNames;
import net.minecraft.client.render.entity.model.SkeletonEntityModel;
import net.minecraft.entity.mob.BoggedEntity;

@Environment(value=EnvType.CLIENT)
public class BoggedEntityModel
extends SkeletonEntityModel<BoggedEntity> {
    private final ModelPart mushrooms;

    public BoggedEntityModel(ModelPart arg) {
        super(arg);
        this.mushrooms = arg.getChild(EntityModelPartNames.HEAD).getChild(EntityModelPartNames.MUSHROOMS);
    }

    public static TexturedModelData getTexturedModelData() {
        ModelData lv = BipedEntityModel.getModelData(Dilation.NONE, 0.0f);
        ModelPartData lv2 = lv.getRoot();
        SkeletonEntityModel.addLimbs(lv2);
        ModelPartData lv3 = lv2.getChild(EntityModelPartNames.HEAD).addChild(EntityModelPartNames.MUSHROOMS, ModelPartBuilder.create(), ModelTransform.NONE);
        lv3.addChild("red_mushroom_1", ModelPartBuilder.create().uv(50, 16).cuboid(-3.0f, -3.0f, 0.0f, 6.0f, 4.0f, 0.0f), ModelTransform.of(3.0f, -8.0f, 3.0f, 0.0f, 0.7853982f, 0.0f));
        lv3.addChild("red_mushroom_2", ModelPartBuilder.create().uv(50, 16).cuboid(-3.0f, -3.0f, 0.0f, 6.0f, 4.0f, 0.0f), ModelTransform.of(3.0f, -8.0f, 3.0f, 0.0f, 2.3561945f, 0.0f));
        lv3.addChild("brown_mushroom_1", ModelPartBuilder.create().uv(50, 22).cuboid(-3.0f, -3.0f, 0.0f, 6.0f, 4.0f, 0.0f), ModelTransform.of(-3.0f, -8.0f, -3.0f, 0.0f, 0.7853982f, 0.0f));
        lv3.addChild("brown_mushroom_2", ModelPartBuilder.create().uv(50, 22).cuboid(-3.0f, -3.0f, 0.0f, 6.0f, 4.0f, 0.0f), ModelTransform.of(-3.0f, -8.0f, -3.0f, 0.0f, 2.3561945f, 0.0f));
        lv3.addChild("brown_mushroom_3", ModelPartBuilder.create().uv(50, 28).cuboid(-3.0f, -4.0f, 0.0f, 6.0f, 4.0f, 0.0f), ModelTransform.of(-2.0f, -1.0f, 4.0f, -1.5707964f, 0.0f, 0.7853982f));
        lv3.addChild("brown_mushroom_4", ModelPartBuilder.create().uv(50, 28).cuboid(-3.0f, -4.0f, 0.0f, 6.0f, 4.0f, 0.0f), ModelTransform.of(-2.0f, -1.0f, 4.0f, -1.5707964f, 0.0f, 2.3561945f));
        return TexturedModelData.of(lv, 64, 32);
    }

    @Override
    public void animateModel(BoggedEntity arg, float f, float g, float h) {
        this.mushrooms.visible = !arg.isSheared();
        super.animateModel(arg, f, g, h);
    }
}

