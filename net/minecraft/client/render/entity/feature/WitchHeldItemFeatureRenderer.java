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
import net.minecraft.client.render.entity.feature.FeatureRendererContext;
import net.minecraft.client.render.entity.feature.VillagerHeldItemFeatureRenderer;
import net.minecraft.client.render.entity.model.WitchEntityModel;
import net.minecraft.client.render.item.HeldItemRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.util.math.RotationAxis;

@Environment(value=EnvType.CLIENT)
public class WitchHeldItemFeatureRenderer<T extends LivingEntity>
extends VillagerHeldItemFeatureRenderer<T, WitchEntityModel<T>> {
    public WitchHeldItemFeatureRenderer(FeatureRendererContext<T, WitchEntityModel<T>> arg, HeldItemRenderer arg2) {
        super(arg, arg2);
    }

    @Override
    public void render(MatrixStack arg, VertexConsumerProvider arg2, int i, T arg3, float f, float g, float h, float j, float k, float l) {
        ItemStack lv = ((LivingEntity)arg3).getMainHandStack();
        arg.push();
        if (lv.isOf(Items.POTION)) {
            ((WitchEntityModel)this.getContextModel()).getHead().rotate(arg);
            ((WitchEntityModel)this.getContextModel()).getNose().rotate(arg);
            arg.translate(0.0625f, 0.25f, 0.0f);
            arg.multiply(RotationAxis.POSITIVE_Z.rotationDegrees(180.0f));
            arg.multiply(RotationAxis.POSITIVE_X.rotationDegrees(140.0f));
            arg.multiply(RotationAxis.POSITIVE_Z.rotationDegrees(10.0f));
            arg.translate(0.0f, -0.4f, 0.4f);
        }
        super.render(arg, arg2, i, arg3, f, g, h, j, k, l);
        arg.pop();
    }
}

