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
import net.minecraft.client.render.entity.model.CatEntityModel;
import net.minecraft.client.render.entity.model.EntityModelLayers;
import net.minecraft.client.render.entity.model.EntityModelLoader;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.passive.CatEntity;
import net.minecraft.util.Identifier;

@Environment(value=EnvType.CLIENT)
public class CatCollarFeatureRenderer
extends FeatureRenderer<CatEntity, CatEntityModel<CatEntity>> {
    private static final Identifier SKIN = Identifier.ofVanilla("textures/entity/cat/cat_collar.png");
    private final CatEntityModel<CatEntity> model;

    public CatCollarFeatureRenderer(FeatureRendererContext<CatEntity, CatEntityModel<CatEntity>> context, EntityModelLoader loader) {
        super(context);
        this.model = new CatEntityModel(loader.getModelPart(EntityModelLayers.CAT_COLLAR));
    }

    @Override
    public void render(MatrixStack arg, VertexConsumerProvider arg2, int i, CatEntity arg3, float f, float g, float h, float j, float k, float l) {
        if (!arg3.isTamed()) {
            return;
        }
        int m = arg3.getCollarColor().getEntityColor();
        CatCollarFeatureRenderer.render(this.getContextModel(), this.model, SKIN, arg, arg2, i, arg3, f, g, j, k, l, h, m);
    }
}

