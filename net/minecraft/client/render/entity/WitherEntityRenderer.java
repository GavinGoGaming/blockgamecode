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
import net.minecraft.client.render.entity.feature.WitherArmorFeatureRenderer;
import net.minecraft.client.render.entity.model.EntityModelLayers;
import net.minecraft.client.render.entity.model.WitherEntityModel;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.boss.WitherEntity;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;

@Environment(value=EnvType.CLIENT)
public class WitherEntityRenderer
extends MobEntityRenderer<WitherEntity, WitherEntityModel<WitherEntity>> {
    private static final Identifier INVULNERABLE_TEXTURE = Identifier.ofVanilla("textures/entity/wither/wither_invulnerable.png");
    private static final Identifier TEXTURE = Identifier.ofVanilla("textures/entity/wither/wither.png");

    public WitherEntityRenderer(EntityRendererFactory.Context arg) {
        super(arg, new WitherEntityModel(arg.getPart(EntityModelLayers.WITHER)), 1.0f);
        this.addFeature(new WitherArmorFeatureRenderer(this, arg.getModelLoader()));
    }

    @Override
    protected int getBlockLight(WitherEntity arg, BlockPos arg2) {
        return 15;
    }

    @Override
    public Identifier getTexture(WitherEntity arg) {
        int i = arg.getInvulnerableTimer();
        if (i <= 0 || i <= 80 && i / 5 % 2 == 1) {
            return TEXTURE;
        }
        return INVULNERABLE_TEXTURE;
    }

    @Override
    protected void scale(WitherEntity arg, MatrixStack arg2, float f) {
        float g = 2.0f;
        int i = arg.getInvulnerableTimer();
        if (i > 0) {
            g -= ((float)i - f) / 220.0f * 0.5f;
        }
        arg2.scale(g, g, g);
    }
}

