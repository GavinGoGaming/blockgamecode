/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 * 
 * Could not load the following classes:
 *  net.fabricmc.api.EnvType
 *  net.fabricmc.api.Environment
 */
package net.minecraft.client.render.entity.feature;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.feature.FeatureRenderer;
import net.minecraft.client.render.entity.feature.FeatureRendererContext;
import net.minecraft.client.render.entity.model.EntityModel;
import net.minecraft.client.render.entity.model.ModelWithArms;
import net.minecraft.client.render.item.HeldItemRenderer;
import net.minecraft.client.render.model.json.ModelTransformationMode;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Arm;
import net.minecraft.util.math.RotationAxis;

@Environment(value=EnvType.CLIENT)
public class HeldItemFeatureRenderer<T extends LivingEntity, M extends EntityModel<T>>
extends FeatureRenderer<T, M> {
    private final HeldItemRenderer heldItemRenderer;

    public HeldItemFeatureRenderer(FeatureRendererContext<T, M> context, HeldItemRenderer heldItemRenderer) {
        super(context);
        this.heldItemRenderer = heldItemRenderer;
    }

    @Override
    public void render(MatrixStack arg, VertexConsumerProvider arg2, int i, T arg3, float f, float g, float h, float j, float k, float l) {
        ItemStack lv2;
        boolean bl = ((LivingEntity)arg3).getMainArm() == Arm.RIGHT;
        ItemStack lv = bl ? ((LivingEntity)arg3).getOffHandStack() : ((LivingEntity)arg3).getMainHandStack();
        ItemStack itemStack = lv2 = bl ? ((LivingEntity)arg3).getMainHandStack() : ((LivingEntity)arg3).getOffHandStack();
        if (lv.isEmpty() && lv2.isEmpty()) {
            return;
        }
        arg.push();
        if (((EntityModel)this.getContextModel()).child) {
            float m = 0.5f;
            arg.translate(0.0f, 0.75f, 0.0f);
            arg.scale(0.5f, 0.5f, 0.5f);
        }
        this.renderItem((LivingEntity)arg3, lv2, ModelTransformationMode.THIRD_PERSON_RIGHT_HAND, Arm.RIGHT, arg, arg2, i);
        this.renderItem((LivingEntity)arg3, lv, ModelTransformationMode.THIRD_PERSON_LEFT_HAND, Arm.LEFT, arg, arg2, i);
        arg.pop();
    }

    protected void renderItem(LivingEntity entity, ItemStack stack, ModelTransformationMode transformationMode, Arm arm, MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light) {
        if (stack.isEmpty()) {
            return;
        }
        matrices.push();
        ((ModelWithArms)this.getContextModel()).setArmAngle(arm, matrices);
        matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(-90.0f));
        matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(180.0f));
        boolean bl = arm == Arm.LEFT;
        matrices.translate((float)(bl ? -1 : 1) / 16.0f, 0.125f, -0.625f);
        this.heldItemRenderer.renderItem(entity, stack, transformationMode, bl, matrices, vertexConsumers, light);
        matrices.pop();
    }
}

