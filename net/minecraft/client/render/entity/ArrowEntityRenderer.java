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
import net.minecraft.client.render.entity.ProjectileEntityRenderer;
import net.minecraft.entity.projectile.ArrowEntity;
import net.minecraft.util.Identifier;

@Environment(value=EnvType.CLIENT)
public class ArrowEntityRenderer
extends ProjectileEntityRenderer<ArrowEntity> {
    public static final Identifier TEXTURE = Identifier.ofVanilla("textures/entity/projectiles/arrow.png");
    public static final Identifier TIPPED_TEXTURE = Identifier.ofVanilla("textures/entity/projectiles/tipped_arrow.png");

    public ArrowEntityRenderer(EntityRendererFactory.Context arg) {
        super(arg);
    }

    @Override
    public Identifier getTexture(ArrowEntity arg) {
        return arg.getColor() > 0 ? TIPPED_TEXTURE : TEXTURE;
    }
}

