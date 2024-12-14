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
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.client.render.entity.MobEntityRenderer;
import net.minecraft.client.render.entity.model.EntityModelLayers;
import net.minecraft.client.render.entity.model.ParrotEntityModel;
import net.minecraft.entity.passive.ParrotEntity;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.MathHelper;

@Environment(value=EnvType.CLIENT)
public class ParrotEntityRenderer
extends MobEntityRenderer<ParrotEntity, ParrotEntityModel> {
    private static final Identifier RED_BLUE_TEXTURE = Identifier.ofVanilla("textures/entity/parrot/parrot_red_blue.png");
    private static final Identifier BLUE_TEXTURE = Identifier.ofVanilla("textures/entity/parrot/parrot_blue.png");
    private static final Identifier GREEN_TEXTURE = Identifier.ofVanilla("textures/entity/parrot/parrot_green.png");
    private static final Identifier YELLOW_TEXTURE = Identifier.ofVanilla("textures/entity/parrot/parrot_yellow_blue.png");
    private static final Identifier GREY_TEXTURE = Identifier.ofVanilla("textures/entity/parrot/parrot_grey.png");

    public ParrotEntityRenderer(EntityRendererFactory.Context arg) {
        super(arg, new ParrotEntityModel(arg.getPart(EntityModelLayers.PARROT)), 0.3f);
    }

    @Override
    public Identifier getTexture(ParrotEntity arg) {
        return ParrotEntityRenderer.getTexture(arg.getVariant());
    }

    public static Identifier getTexture(ParrotEntity.Variant variant) {
        return switch (variant) {
            default -> throw new MatchException(null, null);
            case ParrotEntity.Variant.RED_BLUE -> RED_BLUE_TEXTURE;
            case ParrotEntity.Variant.BLUE -> BLUE_TEXTURE;
            case ParrotEntity.Variant.GREEN -> GREEN_TEXTURE;
            case ParrotEntity.Variant.YELLOW_BLUE -> YELLOW_TEXTURE;
            case ParrotEntity.Variant.GRAY -> GREY_TEXTURE;
        };
    }

    @Override
    public float getAnimationProgress(ParrotEntity arg, float f) {
        float g = MathHelper.lerp(f, arg.prevFlapProgress, arg.flapProgress);
        float h = MathHelper.lerp(f, arg.prevMaxWingDeviation, arg.maxWingDeviation);
        return (MathHelper.sin(g) + 1.0f) * h;
    }
}

