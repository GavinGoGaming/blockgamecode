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
import net.minecraft.client.render.OverlayTexture;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.feature.FeatureRenderer;
import net.minecraft.client.render.entity.feature.FeatureRendererContext;
import net.minecraft.client.render.entity.model.EntityModelLayers;
import net.minecraft.client.render.entity.model.EntityModelLoader;
import net.minecraft.client.render.entity.model.HorseEntityModel;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.component.type.DyedColorComponent;
import net.minecraft.entity.passive.HorseEntity;
import net.minecraft.item.AnimalArmorItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.tag.ItemTags;
import net.minecraft.util.math.ColorHelper;

@Environment(value=EnvType.CLIENT)
public class HorseArmorFeatureRenderer
extends FeatureRenderer<HorseEntity, HorseEntityModel<HorseEntity>> {
    private final HorseEntityModel<HorseEntity> model;

    public HorseArmorFeatureRenderer(FeatureRendererContext<HorseEntity, HorseEntityModel<HorseEntity>> context, EntityModelLoader loader) {
        super(context);
        this.model = new HorseEntityModel(loader.getModelPart(EntityModelLayers.HORSE_ARMOR));
    }

    @Override
    public void render(MatrixStack arg, VertexConsumerProvider arg2, int i, HorseEntity arg3, float f, float g, float h, float j, float k, float l) {
        AnimalArmorItem lv2;
        ItemStack lv = arg3.getBodyArmor();
        Item item = lv.getItem();
        if (!(item instanceof AnimalArmorItem) || (lv2 = (AnimalArmorItem)item).getType() != AnimalArmorItem.Type.EQUESTRIAN) {
            return;
        }
        ((HorseEntityModel)this.getContextModel()).copyStateTo(this.model);
        this.model.animateModel(arg3, f, g, h);
        this.model.setAngles(arg3, f, g, j, k, l);
        int m = lv.isIn(ItemTags.DYEABLE) ? ColorHelper.Argb.fullAlpha(DyedColorComponent.getColor(lv, -6265536)) : -1;
        VertexConsumer lv3 = arg2.getBuffer(RenderLayer.getEntityCutoutNoCull(lv2.getEntityTexture()));
        this.model.render(arg, lv3, i, OverlayTexture.DEFAULT_UV, m);
    }
}

