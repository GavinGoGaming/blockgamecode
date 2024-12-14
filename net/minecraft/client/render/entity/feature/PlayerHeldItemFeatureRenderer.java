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
import net.minecraft.client.model.ModelPart;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.feature.FeatureRendererContext;
import net.minecraft.client.render.entity.feature.HeadFeatureRenderer;
import net.minecraft.client.render.entity.feature.HeldItemFeatureRenderer;
import net.minecraft.client.render.entity.model.EntityModel;
import net.minecraft.client.render.entity.model.ModelWithHead;
import net.minecraft.client.render.item.HeldItemRenderer;
import net.minecraft.client.render.model.json.ModelTransformationMode;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.util.Arm;
import net.minecraft.util.math.MathHelper;

@Environment(value=EnvType.CLIENT)
public class PlayerHeldItemFeatureRenderer<T extends PlayerEntity, M extends EntityModel<T> & ModelWithHead>
extends HeldItemFeatureRenderer<T, M> {
    private final HeldItemRenderer playerHeldItemRenderer;
    private static final float HEAD_YAW = -0.5235988f;
    private static final float HEAD_ROLL = 1.5707964f;

    public PlayerHeldItemFeatureRenderer(FeatureRendererContext<T, M> arg, HeldItemRenderer arg2) {
        super(arg, arg2);
        this.playerHeldItemRenderer = arg2;
    }

    @Override
    protected void renderItem(LivingEntity entity, ItemStack stack, ModelTransformationMode transformationMode, Arm arm, MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light) {
        if (stack.isOf(Items.SPYGLASS) && entity.getActiveItem() == stack && entity.handSwingTicks == 0) {
            this.renderSpyglass(entity, stack, arm, matrices, vertexConsumers, light);
        } else {
            super.renderItem(entity, stack, transformationMode, arm, matrices, vertexConsumers, light);
        }
    }

    private void renderSpyglass(LivingEntity entity, ItemStack stack, Arm arm, MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light) {
        matrices.push();
        ModelPart lv = ((ModelWithHead)this.getContextModel()).getHead();
        float f = lv.pitch;
        lv.pitch = MathHelper.clamp(lv.pitch, -0.5235988f, 1.5707964f);
        lv.rotate(matrices);
        lv.pitch = f;
        HeadFeatureRenderer.translate(matrices, false);
        boolean bl = arm == Arm.LEFT;
        matrices.translate((bl ? -2.5f : 2.5f) / 16.0f, -0.0625f, 0.0f);
        this.playerHeldItemRenderer.renderItem(entity, stack, ModelTransformationMode.HEAD, false, matrices, vertexConsumers, light);
        matrices.pop();
    }
}

