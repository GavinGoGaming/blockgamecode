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
import net.minecraft.client.render.entity.model.BlazeEntityModel;
import net.minecraft.client.render.entity.model.EntityModelLayers;
import net.minecraft.entity.mob.BlazeEntity;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;

@Environment(value=EnvType.CLIENT)
public class BlazeEntityRenderer
extends MobEntityRenderer<BlazeEntity, BlazeEntityModel<BlazeEntity>> {
    private static final Identifier TEXTURE = Identifier.ofVanilla("textures/entity/blaze.png");

    public BlazeEntityRenderer(EntityRendererFactory.Context arg) {
        super(arg, new BlazeEntityModel(arg.getPart(EntityModelLayers.BLAZE)), 0.5f);
    }

    @Override
    protected int getBlockLight(BlazeEntity arg, BlockPos arg2) {
        return 15;
    }

    @Override
    public Identifier getTexture(BlazeEntity arg) {
        return TEXTURE;
    }
}

