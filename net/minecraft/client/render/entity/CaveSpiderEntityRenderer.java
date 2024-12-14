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
import net.minecraft.client.render.entity.SpiderEntityRenderer;
import net.minecraft.client.render.entity.model.EntityModelLayers;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.mob.CaveSpiderEntity;
import net.minecraft.util.Identifier;

@Environment(value=EnvType.CLIENT)
public class CaveSpiderEntityRenderer
extends SpiderEntityRenderer<CaveSpiderEntity> {
    private static final Identifier TEXTURE = Identifier.ofVanilla("textures/entity/spider/cave_spider.png");
    private static final float SCALE = 0.7f;

    public CaveSpiderEntityRenderer(EntityRendererFactory.Context arg) {
        super(arg, EntityModelLayers.CAVE_SPIDER);
        this.shadowRadius *= 0.7f;
    }

    @Override
    protected void scale(CaveSpiderEntity arg, MatrixStack arg2, float f) {
        arg2.scale(0.7f, 0.7f, 0.7f);
    }

    @Override
    public Identifier getTexture(CaveSpiderEntity arg) {
        return TEXTURE;
    }
}

