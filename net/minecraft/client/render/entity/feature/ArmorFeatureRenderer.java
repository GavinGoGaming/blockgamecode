/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 * 
 * Could not load the following classes:
 *  net.fabricmc.api.EnvType
 *  net.fabricmc.api.Environment
 */
package net.minecraft.client.render.entity.feature;

import com.google.common.collect.Maps;
import java.util.Map;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.model.Model;
import net.minecraft.client.render.OverlayTexture;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.TexturedRenderLayers;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.feature.FeatureRenderer;
import net.minecraft.client.render.entity.feature.FeatureRendererContext;
import net.minecraft.client.render.entity.model.AnimalModel;
import net.minecraft.client.render.entity.model.BipedEntityModel;
import net.minecraft.client.render.model.BakedModelManager;
import net.minecraft.client.texture.Sprite;
import net.minecraft.client.texture.SpriteAtlasTexture;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.DyedColorComponent;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ArmorItem;
import net.minecraft.item.ArmorMaterial;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.trim.ArmorTrim;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.registry.tag.ItemTags;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.ColorHelper;

@Environment(value=EnvType.CLIENT)
public class ArmorFeatureRenderer<T extends LivingEntity, M extends BipedEntityModel<T>, A extends BipedEntityModel<T>>
extends FeatureRenderer<T, M> {
    private static final Map<String, Identifier> ARMOR_TEXTURE_CACHE = Maps.newHashMap();
    private final A innerModel;
    private final A outerModel;
    private final SpriteAtlasTexture armorTrimsAtlas;

    public ArmorFeatureRenderer(FeatureRendererContext<T, M> context, A innerModel, A outerModel, BakedModelManager bakery) {
        super(context);
        this.innerModel = innerModel;
        this.outerModel = outerModel;
        this.armorTrimsAtlas = bakery.getAtlas(TexturedRenderLayers.ARMOR_TRIMS_ATLAS_TEXTURE);
    }

    @Override
    public void render(MatrixStack arg, VertexConsumerProvider arg2, int i, T arg3, float f, float g, float h, float j, float k, float l) {
        this.renderArmor(arg, arg2, arg3, EquipmentSlot.CHEST, i, this.getModel(EquipmentSlot.CHEST));
        this.renderArmor(arg, arg2, arg3, EquipmentSlot.LEGS, i, this.getModel(EquipmentSlot.LEGS));
        this.renderArmor(arg, arg2, arg3, EquipmentSlot.FEET, i, this.getModel(EquipmentSlot.FEET));
        this.renderArmor(arg, arg2, arg3, EquipmentSlot.HEAD, i, this.getModel(EquipmentSlot.HEAD));
    }

    private void renderArmor(MatrixStack matrices, VertexConsumerProvider vertexConsumers, T entity, EquipmentSlot armorSlot, int light, A model) {
        ItemStack lv = ((LivingEntity)entity).getEquippedStack(armorSlot);
        Item item = lv.getItem();
        if (!(item instanceof ArmorItem)) {
            return;
        }
        ArmorItem lv2 = (ArmorItem)item;
        if (lv2.getSlotType() != armorSlot) {
            return;
        }
        ((BipedEntityModel)this.getContextModel()).copyBipedStateTo(model);
        this.setVisible(model, armorSlot);
        boolean bl = this.usesInnerModel(armorSlot);
        ArmorMaterial lv3 = lv2.getMaterial().value();
        int j = lv.isIn(ItemTags.DYEABLE) ? ColorHelper.Argb.fullAlpha(DyedColorComponent.getColor(lv, -6265536)) : -1;
        for (ArmorMaterial.Layer lv4 : lv3.layers()) {
            int k = lv4.isDyeable() ? j : -1;
            this.renderArmorParts(matrices, vertexConsumers, light, model, k, lv4.getTexture(bl));
        }
        ArmorTrim lv5 = lv.get(DataComponentTypes.TRIM);
        if (lv5 != null) {
            this.renderTrim(lv2.getMaterial(), matrices, vertexConsumers, light, lv5, model, bl);
        }
        if (lv.hasGlint()) {
            this.renderGlint(matrices, vertexConsumers, light, model);
        }
    }

    protected void setVisible(A bipedModel, EquipmentSlot slot) {
        ((BipedEntityModel)bipedModel).setVisible(false);
        switch (slot) {
            case HEAD: {
                ((BipedEntityModel)bipedModel).head.visible = true;
                ((BipedEntityModel)bipedModel).hat.visible = true;
                break;
            }
            case CHEST: {
                ((BipedEntityModel)bipedModel).body.visible = true;
                ((BipedEntityModel)bipedModel).rightArm.visible = true;
                ((BipedEntityModel)bipedModel).leftArm.visible = true;
                break;
            }
            case LEGS: {
                ((BipedEntityModel)bipedModel).body.visible = true;
                ((BipedEntityModel)bipedModel).rightLeg.visible = true;
                ((BipedEntityModel)bipedModel).leftLeg.visible = true;
                break;
            }
            case FEET: {
                ((BipedEntityModel)bipedModel).rightLeg.visible = true;
                ((BipedEntityModel)bipedModel).leftLeg.visible = true;
            }
        }
    }

    private void renderArmorParts(MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light, A model, int j, Identifier arg4) {
        VertexConsumer lv = vertexConsumers.getBuffer(RenderLayer.getArmorCutoutNoCull(arg4));
        ((AnimalModel)model).render(matrices, lv, light, OverlayTexture.DEFAULT_UV, j);
    }

    private void renderTrim(RegistryEntry<ArmorMaterial> armorMaterial, MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light, ArmorTrim trim, A model, boolean leggings) {
        Sprite lv = this.armorTrimsAtlas.getSprite(leggings ? trim.getLeggingsModelId(armorMaterial) : trim.getGenericModelId(armorMaterial));
        VertexConsumer lv2 = lv.getTextureSpecificVertexConsumer(vertexConsumers.getBuffer(TexturedRenderLayers.getArmorTrims(trim.getPattern().value().decal())));
        ((Model)model).render(matrices, lv2, light, OverlayTexture.DEFAULT_UV);
    }

    private void renderGlint(MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light, A model) {
        ((Model)model).render(matrices, vertexConsumers.getBuffer(RenderLayer.getArmorEntityGlint()), light, OverlayTexture.DEFAULT_UV);
    }

    private A getModel(EquipmentSlot slot) {
        return this.usesInnerModel(slot) ? this.innerModel : this.outerModel;
    }

    private boolean usesInnerModel(EquipmentSlot slot) {
        return slot == EquipmentSlot.LEGS;
    }
}

