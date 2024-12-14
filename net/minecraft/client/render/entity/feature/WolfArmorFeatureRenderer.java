/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 * 
 * Could not load the following classes:
 *  net.fabricmc.api.EnvType
 *  net.fabricmc.api.Environment
 */
package net.minecraft.client.render.entity.feature;

import java.util.Map;
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
import net.minecraft.client.render.entity.model.WolfEntityModel;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.component.type.DyedColorComponent;
import net.minecraft.entity.passive.Cracks;
import net.minecraft.entity.passive.WolfEntity;
import net.minecraft.item.AnimalArmorItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.tag.ItemTags;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.ColorHelper;

@Environment(value=EnvType.CLIENT)
public class WolfArmorFeatureRenderer
extends FeatureRenderer<WolfEntity, WolfEntityModel<WolfEntity>> {
    private final WolfEntityModel<WolfEntity> model;
    private static final Map<Cracks.CrackLevel, Identifier> CRACK_TEXTURES = Map.of(Cracks.CrackLevel.LOW, Identifier.ofVanilla("textures/entity/wolf/wolf_armor_crackiness_low.png"), Cracks.CrackLevel.MEDIUM, Identifier.ofVanilla("textures/entity/wolf/wolf_armor_crackiness_medium.png"), Cracks.CrackLevel.HIGH, Identifier.ofVanilla("textures/entity/wolf/wolf_armor_crackiness_high.png"));

    public WolfArmorFeatureRenderer(FeatureRendererContext<WolfEntity, WolfEntityModel<WolfEntity>> context, EntityModelLoader loader) {
        super(context);
        this.model = new WolfEntityModel(loader.getModelPart(EntityModelLayers.WOLF_ARMOR));
    }

    @Override
    public void render(MatrixStack arg, VertexConsumerProvider arg2, int i, WolfEntity arg3, float f, float g, float h, float j, float k, float l) {
        AnimalArmorItem lv2;
        if (!arg3.hasArmor()) {
            return;
        }
        ItemStack lv = arg3.getBodyArmor();
        Item item = lv.getItem();
        if (!(item instanceof AnimalArmorItem) || (lv2 = (AnimalArmorItem)item).getType() != AnimalArmorItem.Type.CANINE) {
            return;
        }
        ((WolfEntityModel)this.getContextModel()).copyStateTo(this.model);
        this.model.animateModel(arg3, f, g, h);
        this.model.setAngles(arg3, f, g, j, k, l);
        VertexConsumer lv3 = arg2.getBuffer(RenderLayer.getEntityCutoutNoCull(lv2.getEntityTexture()));
        this.model.render(arg, lv3, i, OverlayTexture.DEFAULT_UV);
        this.renderDyed(arg, arg2, i, lv, lv2);
        this.renderCracks(arg, arg2, i, lv);
    }

    private void renderDyed(MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light, ItemStack stack, AnimalArmorItem item) {
        if (stack.isIn(ItemTags.DYEABLE)) {
            int j = DyedColorComponent.getColor(stack, 0);
            if (ColorHelper.Argb.getAlpha(j) == 0) {
                return;
            }
            Identifier lv = item.getOverlayTexture();
            if (lv == null) {
                return;
            }
            this.model.render(matrices, vertexConsumers.getBuffer(RenderLayer.getEntityCutoutNoCull(lv)), light, OverlayTexture.DEFAULT_UV, ColorHelper.Argb.fullAlpha(j));
        }
    }

    private void renderCracks(MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light, ItemStack stack) {
        Cracks.CrackLevel lv = Cracks.WOLF_ARMOR.getCrackLevel(stack);
        if (lv == Cracks.CrackLevel.NONE) {
            return;
        }
        Identifier lv2 = CRACK_TEXTURES.get((Object)lv);
        VertexConsumer lv3 = vertexConsumers.getBuffer(RenderLayer.getEntityTranslucent(lv2));
        this.model.render(matrices, lv3, light, OverlayTexture.DEFAULT_UV);
    }
}

