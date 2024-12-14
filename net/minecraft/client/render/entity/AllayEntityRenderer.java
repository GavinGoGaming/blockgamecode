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
import net.minecraft.client.render.entity.feature.HeldItemFeatureRenderer;
import net.minecraft.client.render.entity.model.AllayEntityModel;
import net.minecraft.client.render.entity.model.EntityModelLayers;
import net.minecraft.entity.passive.AllayEntity;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;

@Environment(value=EnvType.CLIENT)
public class AllayEntityRenderer
extends MobEntityRenderer<AllayEntity, AllayEntityModel> {
    private static final Identifier TEXTURE = Identifier.ofVanilla("textures/entity/allay/allay.png");

    public AllayEntityRenderer(EntityRendererFactory.Context arg) {
        super(arg, new AllayEntityModel(arg.getPart(EntityModelLayers.ALLAY)), 0.4f);
        this.addFeature(new HeldItemFeatureRenderer<AllayEntity, AllayEntityModel>(this, arg.getHeldItemRenderer()));
    }

    @Override
    public Identifier getTexture(AllayEntity arg) {
        return TEXTURE;
    }

    @Override
    protected int getBlockLight(AllayEntity arg, BlockPos arg2) {
        return 15;
    }
}

