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
import net.minecraft.client.render.entity.ZombieBaseEntityRenderer;
import net.minecraft.client.render.entity.model.EntityModelLayer;
import net.minecraft.client.render.entity.model.EntityModelLayers;
import net.minecraft.client.render.entity.model.ZombieEntityModel;
import net.minecraft.entity.mob.ZombieEntity;

@Environment(value=EnvType.CLIENT)
public class ZombieEntityRenderer
extends ZombieBaseEntityRenderer<ZombieEntity, ZombieEntityModel<ZombieEntity>> {
    public ZombieEntityRenderer(EntityRendererFactory.Context arg) {
        this(arg, EntityModelLayers.ZOMBIE, EntityModelLayers.ZOMBIE_INNER_ARMOR, EntityModelLayers.ZOMBIE_OUTER_ARMOR);
    }

    public ZombieEntityRenderer(EntityRendererFactory.Context ctx, EntityModelLayer layer, EntityModelLayer legsArmorLayer, EntityModelLayer bodyArmorLayer) {
        super(ctx, new ZombieEntityModel(ctx.getPart(layer)), new ZombieEntityModel(ctx.getPart(legsArmorLayer)), new ZombieEntityModel(ctx.getPart(bodyArmorLayer)));
    }
}

