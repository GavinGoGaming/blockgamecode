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
import net.minecraft.client.render.entity.model.EntityModelLayers;
import net.minecraft.client.render.entity.model.EntityModelLoader;
import net.minecraft.client.render.entity.model.LargeTropicalFishEntityModel;
import net.minecraft.client.render.entity.model.SmallTropicalFishEntityModel;
import net.minecraft.client.render.entity.model.TintableCompositeModel;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.passive.TropicalFishEntity;
import net.minecraft.util.Identifier;

@Environment(value=EnvType.CLIENT)
public class TropicalFishColorFeatureRenderer
extends FeatureRenderer<TropicalFishEntity, TintableCompositeModel<TropicalFishEntity>> {
    private static final Identifier KOB_TEXTURE = Identifier.ofVanilla("textures/entity/fish/tropical_a_pattern_1.png");
    private static final Identifier SUNSTREAK_TEXTURE = Identifier.ofVanilla("textures/entity/fish/tropical_a_pattern_2.png");
    private static final Identifier SNOOPER_TEXTURE = Identifier.ofVanilla("textures/entity/fish/tropical_a_pattern_3.png");
    private static final Identifier DASHER_TEXTURE = Identifier.ofVanilla("textures/entity/fish/tropical_a_pattern_4.png");
    private static final Identifier BRINELY_TEXTURE = Identifier.ofVanilla("textures/entity/fish/tropical_a_pattern_5.png");
    private static final Identifier SPOTTY_TEXTURE = Identifier.ofVanilla("textures/entity/fish/tropical_a_pattern_6.png");
    private static final Identifier FLOPPER_TEXTURE = Identifier.ofVanilla("textures/entity/fish/tropical_b_pattern_1.png");
    private static final Identifier STRIPEY_TEXTURE = Identifier.ofVanilla("textures/entity/fish/tropical_b_pattern_2.png");
    private static final Identifier GLITTER_TEXTURE = Identifier.ofVanilla("textures/entity/fish/tropical_b_pattern_3.png");
    private static final Identifier BLOCKFISH_TEXTURE = Identifier.ofVanilla("textures/entity/fish/tropical_b_pattern_4.png");
    private static final Identifier BETTY_TEXTURE = Identifier.ofVanilla("textures/entity/fish/tropical_b_pattern_5.png");
    private static final Identifier CLAYFISH_TEXTURE = Identifier.ofVanilla("textures/entity/fish/tropical_b_pattern_6.png");
    private final SmallTropicalFishEntityModel<TropicalFishEntity> smallModel;
    private final LargeTropicalFishEntityModel<TropicalFishEntity> largeModel;

    public TropicalFishColorFeatureRenderer(FeatureRendererContext<TropicalFishEntity, TintableCompositeModel<TropicalFishEntity>> context, EntityModelLoader loader) {
        super(context);
        this.smallModel = new SmallTropicalFishEntityModel(loader.getModelPart(EntityModelLayers.TROPICAL_FISH_SMALL_PATTERN));
        this.largeModel = new LargeTropicalFishEntityModel(loader.getModelPart(EntityModelLayers.TROPICAL_FISH_LARGE_PATTERN));
    }

    @Override
    public void render(MatrixStack arg, VertexConsumerProvider arg2, int i, TropicalFishEntity arg3, float f, float g, float h, float j, float k, float l) {
        TropicalFishEntity.Variety lv = arg3.getVariant();
        TintableCompositeModel lv2 = switch (lv.getSize()) {
            default -> throw new MatchException(null, null);
            case TropicalFishEntity.Size.SMALL -> this.smallModel;
            case TropicalFishEntity.Size.LARGE -> this.largeModel;
        };
        Identifier lv3 = switch (lv) {
            default -> throw new MatchException(null, null);
            case TropicalFishEntity.Variety.KOB -> KOB_TEXTURE;
            case TropicalFishEntity.Variety.SUNSTREAK -> SUNSTREAK_TEXTURE;
            case TropicalFishEntity.Variety.SNOOPER -> SNOOPER_TEXTURE;
            case TropicalFishEntity.Variety.DASHER -> DASHER_TEXTURE;
            case TropicalFishEntity.Variety.BRINELY -> BRINELY_TEXTURE;
            case TropicalFishEntity.Variety.SPOTTY -> SPOTTY_TEXTURE;
            case TropicalFishEntity.Variety.FLOPPER -> FLOPPER_TEXTURE;
            case TropicalFishEntity.Variety.STRIPEY -> STRIPEY_TEXTURE;
            case TropicalFishEntity.Variety.GLITTER -> GLITTER_TEXTURE;
            case TropicalFishEntity.Variety.BLOCKFISH -> BLOCKFISH_TEXTURE;
            case TropicalFishEntity.Variety.BETTY -> BETTY_TEXTURE;
            case TropicalFishEntity.Variety.CLAYFISH -> CLAYFISH_TEXTURE;
        };
        int m = arg3.getPatternColorComponents().getEntityColor();
        TropicalFishColorFeatureRenderer.render(this.getContextModel(), lv2, lv3, arg, arg2, i, arg3, f, g, j, k, l, h, m);
    }
}

