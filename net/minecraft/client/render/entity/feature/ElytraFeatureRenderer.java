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
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.client.render.OverlayTexture;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.feature.FeatureRenderer;
import net.minecraft.client.render.entity.feature.FeatureRendererContext;
import net.minecraft.client.render.entity.model.ElytraEntityModel;
import net.minecraft.client.render.entity.model.EntityModel;
import net.minecraft.client.render.entity.model.EntityModelLayers;
import net.minecraft.client.render.entity.model.EntityModelLoader;
import net.minecraft.client.render.item.ItemRenderer;
import net.minecraft.client.util.SkinTextures;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerModelPart;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.util.Identifier;

@Environment(value=EnvType.CLIENT)
public class ElytraFeatureRenderer<T extends LivingEntity, M extends EntityModel<T>>
extends FeatureRenderer<T, M> {
    private static final Identifier SKIN = Identifier.ofVanilla("textures/entity/elytra.png");
    private final ElytraEntityModel<T> elytra;

    public ElytraFeatureRenderer(FeatureRendererContext<T, M> context, EntityModelLoader loader) {
        super(context);
        this.elytra = new ElytraEntityModel(loader.getModelPart(EntityModelLayers.ELYTRA));
    }

    @Override
    public void render(MatrixStack arg, VertexConsumerProvider arg2, int i, T arg3, float f, float g, float h, float j, float k, float l) {
        AbstractClientPlayerEntity lv2;
        SkinTextures lv3;
        ItemStack lv = ((LivingEntity)arg3).getEquippedStack(EquipmentSlot.CHEST);
        if (!lv.isOf(Items.ELYTRA)) {
            return;
        }
        Identifier lv4 = arg3 instanceof AbstractClientPlayerEntity ? ((lv3 = (lv2 = (AbstractClientPlayerEntity)arg3).getSkinTextures()).elytraTexture() != null ? lv3.elytraTexture() : (lv3.capeTexture() != null && lv2.isPartVisible(PlayerModelPart.CAPE) ? lv3.capeTexture() : SKIN)) : SKIN;
        arg.push();
        arg.translate(0.0f, 0.0f, 0.125f);
        ((EntityModel)this.getContextModel()).copyStateTo(this.elytra);
        this.elytra.setAngles(arg3, f, g, j, k, l);
        VertexConsumer lv5 = ItemRenderer.getArmorGlintConsumer(arg2, RenderLayer.getArmorCutoutNoCull(lv4), lv.hasGlint());
        this.elytra.render(arg, lv5, i, OverlayTexture.DEFAULT_UV);
        arg.pop();
    }
}

