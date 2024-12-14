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
import net.minecraft.entity.mob.PhantomEntity;
import net.minecraft.util.math.MathHelper;

@Environment(value=EnvType.CLIENT)
public class PhantomEntityModel<T extends PhantomEntity>
extends SinglePartEntityModel<T> {
    private static final String TAIL_BASE = "tail_base";
    private static final String TAIL_TIP = "tail_tip";
    private final ModelPart root;
    private final ModelPart leftWingBase;
    private final ModelPart leftWingTip;
    private final ModelPart rightWingBase;
    private final ModelPart rightWingTip;
    private final ModelPart tailBase;
    private final ModelPart tailTip;

    public PhantomEntityModel(ModelPart root) {
        this.root = root;
        ModelPart lv = root.getChild(EntityModelPartNames.BODY);
        this.tailBase = lv.getChild(TAIL_BASE);
        this.tailTip = this.tailBase.getChild(TAIL_TIP);
        this.leftWingBase = lv.getChild(EntityModelPartNames.LEFT_WING_BASE);
        this.leftWingTip = this.leftWingBase.getChild(EntityModelPartNames.LEFT_WING_TIP);
        this.rightWingBase = lv.getChild(EntityModelPartNames.RIGHT_WING_BASE);
        this.rightWingTip = this.rightWingBase.getChild(EntityModelPartNames.RIGHT_WING_TIP);
    }

    public static TexturedModelData getTexturedModelData() {
        ModelData lv = new ModelData();
        ModelPartData lv2 = lv.getRoot();
        ModelPartData lv3 = lv2.addChild(EntityModelPartNames.BODY, ModelPartBuilder.create().uv(0, 8).cuboid(-3.0f, -2.0f, -8.0f, 5.0f, 3.0f, 9.0f), ModelTransform.rotation(-0.1f, 0.0f, 0.0f));
        ModelPartData lv4 = lv3.addChild(TAIL_BASE, ModelPartBuilder.create().uv(3, 20).cuboid(-2.0f, 0.0f, 0.0f, 3.0f, 2.0f, 6.0f), ModelTransform.pivot(0.0f, -2.0f, 1.0f));
        lv4.addChild(TAIL_TIP, ModelPartBuilder.create().uv(4, 29).cuboid(-1.0f, 0.0f, 0.0f, 1.0f, 1.0f, 6.0f), ModelTransform.pivot(0.0f, 0.5f, 6.0f));
        ModelPartData lv5 = lv3.addChild(EntityModelPartNames.LEFT_WING_BASE, ModelPartBuilder.create().uv(23, 12).cuboid(0.0f, 0.0f, 0.0f, 6.0f, 2.0f, 9.0f), ModelTransform.of(2.0f, -2.0f, -8.0f, 0.0f, 0.0f, 0.1f));
        lv5.addChild(EntityModelPartNames.LEFT_WING_TIP, ModelPartBuilder.create().uv(16, 24).cuboid(0.0f, 0.0f, 0.0f, 13.0f, 1.0f, 9.0f), ModelTransform.of(6.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.1f));
        ModelPartData lv6 = lv3.addChild(EntityModelPartNames.RIGHT_WING_BASE, ModelPartBuilder.create().uv(23, 12).mirrored().cuboid(-6.0f, 0.0f, 0.0f, 6.0f, 2.0f, 9.0f), ModelTransform.of(-3.0f, -2.0f, -8.0f, 0.0f, 0.0f, -0.1f));
        lv6.addChild(EntityModelPartNames.RIGHT_WING_TIP, ModelPartBuilder.create().uv(16, 24).mirrored().cuboid(-13.0f, 0.0f, 0.0f, 13.0f, 1.0f, 9.0f), ModelTransform.of(-6.0f, 0.0f, 0.0f, 0.0f, 0.0f, -0.1f));
        lv3.addChild(EntityModelPartNames.HEAD, ModelPartBuilder.create().uv(0, 0).cuboid(-4.0f, -2.0f, -5.0f, 7.0f, 3.0f, 5.0f), ModelTransform.of(0.0f, 1.0f, -7.0f, 0.2f, 0.0f, 0.0f));
        return TexturedModelData.of(lv, 64, 64);
    }

    @Override
    public ModelPart getPart() {
        return this.root;
    }

    @Override
    public void setAngles(T arg, float f, float g, float h, float i, float j) {
        float k = ((float)((PhantomEntity)arg).getWingFlapTickOffset() + h) * 7.448451f * ((float)Math.PI / 180);
        float l = 16.0f;
        this.leftWingBase.roll = MathHelper.cos(k) * 16.0f * ((float)Math.PI / 180);
        this.leftWingTip.roll = MathHelper.cos(k) * 16.0f * ((float)Math.PI / 180);
        this.rightWingBase.roll = -this.leftWingBase.roll;
        this.rightWingTip.roll = -this.leftWingTip.roll;
        this.tailBase.pitch = -(5.0f + MathHelper.cos(k * 2.0f) * 5.0f) * ((float)Math.PI / 180);
        this.tailTip.pitch = -(5.0f + MathHelper.cos(k * 2.0f) * 5.0f) * ((float)Math.PI / 180);
    }
}

