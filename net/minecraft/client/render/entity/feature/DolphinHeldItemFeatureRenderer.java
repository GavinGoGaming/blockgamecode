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
import net.minecraft.client.render.entity.model.DolphinEntityModel;
import net.minecraft.client.render.item.HeldItemRenderer;
import net.minecraft.client.render.model.json.ModelTransformationMode;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.passive.DolphinEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Arm;
import net.minecraft.util.math.MathHelper;

@Environment(value=EnvType.CLIENT)
public class DolphinHeldItemFeatureRenderer
extends FeatureRenderer<DolphinEntity, DolphinEntityModel<DolphinEntity>> {
    private final HeldItemRenderer heldItemRenderer;

    public DolphinHeldItemFeatureRenderer(FeatureRendererContext<DolphinEntity, DolphinEntityModel<DolphinEntity>> context, HeldItemRenderer heldItemRenderer) {
        super(context);
        this.heldItemRenderer = heldItemRenderer;
    }

    @Override
    public void render(MatrixStack arg, VertexConsumerProvider arg2, int i, DolphinEntity arg3, float f, float g, float h, float j, float k, float l) {
        boolean bl = arg3.getMainArm() == Arm.RIGHT;
        arg.push();
        float m = 1.0f;
        float n = -1.0f;
        float o = MathHelper.abs(arg3.getPitch()) / 60.0f;
        if (arg3.getPitch() < 0.0f) {
            arg.translate(0.0f, 1.0f - o * 0.5f, -1.0f + o * 0.5f);
        } else {
            arg.translate(0.0f, 1.0f + o * 0.8f, -1.0f + o * 0.2f);
        }
        ItemStack lv = bl ? arg3.getMainHandStack() : arg3.getOffHandStack();
        this.heldItemRenderer.renderItem(arg3, lv, ModelTransformationMode.GROUND, false, arg, arg2, i);
        arg.pop();
    }
}

