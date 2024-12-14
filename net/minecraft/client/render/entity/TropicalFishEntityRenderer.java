/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 * 
 * Could not load the following classes:
 *  net.fabricmc.api.EnvType
 *  net.fabricmc.api.Environment
 */
package net.minecraft.client.render.entity;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.client.render.entity.MobEntityRenderer;
import net.minecraft.client.render.entity.feature.TropicalFishColorFeatureRenderer;
import net.minecraft.client.render.entity.model.EntityModelLayers;
import net.minecraft.client.render.entity.model.LargeTropicalFishEntityModel;
import net.minecraft.client.render.entity.model.SmallTropicalFishEntityModel;
import net.minecraft.client.render.entity.model.TintableCompositeModel;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.passive.TropicalFishEntity;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RotationAxis;

@Environment(value=EnvType.CLIENT)
public class TropicalFishEntityRenderer
extends MobEntityRenderer<TropicalFishEntity, TintableCompositeModel<TropicalFishEntity>> {
    private final TintableCompositeModel<TropicalFishEntity> smallModel = (TintableCompositeModel)this.getModel();
    private final TintableCompositeModel<TropicalFishEntity> largeModel;
    private static final Identifier A_TEXTURE = Identifier.ofVanilla("textures/entity/fish/tropical_a.png");
    private static final Identifier B_TEXTURE = Identifier.ofVanilla("textures/entity/fish/tropical_b.png");

    public TropicalFishEntityRenderer(EntityRendererFactory.Context arg) {
        super(arg, new SmallTropicalFishEntityModel(arg.getPart(EntityModelLayers.TROPICAL_FISH_SMALL)), 0.15f);
        this.largeModel = new LargeTropicalFishEntityModel<TropicalFishEntity>(arg.getPart(EntityModelLayers.TROPICAL_FISH_LARGE));
        this.addFeature(new TropicalFishColorFeatureRenderer(this, arg.getModelLoader()));
    }

    @Override
    public Identifier getTexture(TropicalFishEntity arg) {
        return switch (arg.getVariant().getSize()) {
            default -> throw new MatchException(null, null);
            case TropicalFishEntity.Size.SMALL -> A_TEXTURE;
            case TropicalFishEntity.Size.LARGE -> B_TEXTURE;
        };
    }

    @Override
    public void render(TropicalFishEntity arg, float f, float g, MatrixStack arg2, VertexConsumerProvider arg3, int i) {
        TintableCompositeModel<TropicalFishEntity> lv;
        this.model = lv = (switch (arg.getVariant().getSize()) {
            default -> throw new MatchException(null, null);
            case TropicalFishEntity.Size.SMALL -> this.smallModel;
            case TropicalFishEntity.Size.LARGE -> this.largeModel;
        });
        lv.setColorMultiplier(arg.getBaseColorComponents().getEntityColor());
        super.render(arg, f, g, arg2, arg3, i);
        lv.setColorMultiplier(-1);
    }

    @Override
    protected void setupTransforms(TropicalFishEntity arg, MatrixStack arg2, float f, float g, float h, float i) {
        super.setupTransforms(arg, arg2, f, g, h, i);
        float j = 4.3f * MathHelper.sin(0.6f * f);
        arg2.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(j));
        if (!arg.isTouchingWater()) {
            arg2.translate(0.2f, 0.1f, 0.0f);
            arg2.multiply(RotationAxis.POSITIVE_Z.rotationDegrees(90.0f));
        }
    }
}

