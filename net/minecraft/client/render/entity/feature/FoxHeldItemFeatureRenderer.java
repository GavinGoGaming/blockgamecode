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
import net.minecraft.client.render.entity.model.FoxEntityModel;
import net.minecraft.client.render.item.HeldItemRenderer;
import net.minecraft.client.render.model.json.ModelTransformationMode;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.passive.FoxEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.RotationAxis;

@Environment(value=EnvType.CLIENT)
public class FoxHeldItemFeatureRenderer
extends FeatureRenderer<FoxEntity, FoxEntityModel<FoxEntity>> {
    private final HeldItemRenderer heldItemRenderer;

    public FoxHeldItemFeatureRenderer(FeatureRendererContext<FoxEntity, FoxEntityModel<FoxEntity>> context, HeldItemRenderer heldItemRenderer) {
        super(context);
        this.heldItemRenderer = heldItemRenderer;
    }

    @Override
    public void render(MatrixStack arg, VertexConsumerProvider arg2, int i, FoxEntity arg3, float f, float g, float h, float j, float k, float l) {
        float m;
        boolean bl = arg3.isSleeping();
        boolean bl2 = arg3.isBaby();
        arg.push();
        if (bl2) {
            m = 0.75f;
            arg.scale(0.75f, 0.75f, 0.75f);
            arg.translate(0.0f, 0.5f, 0.209375f);
        }
        arg.translate(((FoxEntityModel)this.getContextModel()).head.pivotX / 16.0f, ((FoxEntityModel)this.getContextModel()).head.pivotY / 16.0f, ((FoxEntityModel)this.getContextModel()).head.pivotZ / 16.0f);
        m = arg3.getHeadRoll(h);
        arg.multiply(RotationAxis.POSITIVE_Z.rotation(m));
        arg.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(k));
        arg.multiply(RotationAxis.POSITIVE_X.rotationDegrees(l));
        if (arg3.isBaby()) {
            if (bl) {
                arg.translate(0.4f, 0.26f, 0.15f);
            } else {
                arg.translate(0.06f, 0.26f, -0.5f);
            }
        } else if (bl) {
            arg.translate(0.46f, 0.26f, 0.22f);
        } else {
            arg.translate(0.06f, 0.27f, -0.5f);
        }
        arg.multiply(RotationAxis.POSITIVE_X.rotationDegrees(90.0f));
        if (bl) {
            arg.multiply(RotationAxis.POSITIVE_Z.rotationDegrees(90.0f));
        }
        ItemStack lv = arg3.getEquippedStack(EquipmentSlot.MAINHAND);
        this.heldItemRenderer.renderItem(arg3, lv, ModelTransformationMode.GROUND, false, arg, arg2, i);
        arg.pop();
    }
}

