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
import net.minecraft.client.render.entity.EntityRenderer;
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.client.texture.SpriteAtlasTexture;
import net.minecraft.entity.Entity;
import net.minecraft.util.Identifier;

@Environment(value=EnvType.CLIENT)
public class EmptyEntityRenderer<T extends Entity>
extends EntityRenderer<T> {
    public EmptyEntityRenderer(EntityRendererFactory.Context arg) {
        super(arg);
    }

    @Override
    public Identifier getTexture(T entity) {
        return SpriteAtlasTexture.BLOCK_ATLAS_TEXTURE;
    }
}

