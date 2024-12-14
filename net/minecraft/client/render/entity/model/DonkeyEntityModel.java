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
import net.minecraft.client.render.entity.model.HorseEntityModel;
import net.minecraft.entity.passive.AbstractDonkeyEntity;

@Environment(value=EnvType.CLIENT)
public class DonkeyEntityModel<T extends AbstractDonkeyEntity>
extends HorseEntityModel<T> {
    private final ModelPart leftChest;
    private final ModelPart rightChest;

    public DonkeyEntityModel(ModelPart arg) {
        super(arg);
        this.leftChest = this.body.getChild(EntityModelPartNames.LEFT_CHEST);
        this.rightChest = this.body.getChild(EntityModelPartNames.RIGHT_CHEST);
    }

    public static TexturedModelData getTexturedModelData() {
        ModelData lv = HorseEntityModel.getModelData(Dilation.NONE);
        ModelPartData lv2 = lv.getRoot();
        ModelPartData lv3 = lv2.getChild(EntityModelPartNames.BODY);
        ModelPartBuilder lv4 = ModelPartBuilder.create().uv(26, 21).cuboid(-4.0f, 0.0f, -2.0f, 8.0f, 8.0f, 3.0f);
        lv3.addChild(EntityModelPartNames.LEFT_CHEST, lv4, ModelTransform.of(6.0f, -8.0f, 0.0f, 0.0f, -1.5707964f, 0.0f));
        lv3.addChild(EntityModelPartNames.RIGHT_CHEST, lv4, ModelTransform.of(-6.0f, -8.0f, 0.0f, 0.0f, 1.5707964f, 0.0f));
        ModelPartData lv5 = lv2.getChild("head_parts").getChild(EntityModelPartNames.HEAD);
        ModelPartBuilder lv6 = ModelPartBuilder.create().uv(0, 12).cuboid(-1.0f, -7.0f, 0.0f, 2.0f, 7.0f, 1.0f);
        lv5.addChild(EntityModelPartNames.LEFT_EAR, lv6, ModelTransform.of(1.25f, -10.0f, 4.0f, 0.2617994f, 0.0f, 0.2617994f));
        lv5.addChild(EntityModelPartNames.RIGHT_EAR, lv6, ModelTransform.of(-1.25f, -10.0f, 4.0f, 0.2617994f, 0.0f, -0.2617994f));
        return TexturedModelData.of(lv, 64, 64);
    }

    @Override
    public void setAngles(T arg, float f, float g, float h, float i, float j) {
        super.setAngles(arg, f, g, h, i, j);
        if (((AbstractDonkeyEntity)arg).hasChest()) {
            this.leftChest.visible = true;
            this.rightChest.visible = true;
        } else {
            this.leftChest.visible = false;
            this.rightChest.visible = false;
        }
    }
}

