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
import net.minecraft.client.render.entity.ZombieEntityRenderer;
import net.minecraft.client.render.entity.model.EntityModelLayers;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.mob.ZombieEntity;
import net.minecraft.util.Identifier;

@Environment(value=EnvType.CLIENT)
public class HuskEntityRenderer
extends ZombieEntityRenderer {
    private static final Identifier TEXTURE = Identifier.ofVanilla("textures/entity/zombie/husk.png");

    public HuskEntityRenderer(EntityRendererFactory.Context arg) {
        super(arg, EntityModelLayers.HUSK, EntityModelLayers.HUSK_INNER_ARMOR, EntityModelLayers.HUSK_OUTER_ARMOR);
    }

    @Override
    protected void scale(ZombieEntity arg, MatrixStack arg2, float f) {
        float g = 1.0625f;
        arg2.scale(1.0625f, 1.0625f, 1.0625f);
        super.scale(arg, arg2, f);
    }

    @Override
    public Identifier getTexture(ZombieEntity arg) {
        return TEXTURE;
    }
}

