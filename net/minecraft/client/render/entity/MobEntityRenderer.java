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
import net.minecraft.client.render.entity.LivingEntityRenderer;
import net.minecraft.client.render.entity.model.EntityModel;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.mob.MobEntity;

@Environment(value=EnvType.CLIENT)
public abstract class MobEntityRenderer<T extends MobEntity, M extends EntityModel<T>>
extends LivingEntityRenderer<T, M> {
    public MobEntityRenderer(EntityRendererFactory.Context arg, M arg2, float f) {
        super(arg, arg2, f);
    }

    @Override
    protected boolean hasLabel(T arg) {
        return super.hasLabel(arg) && (((LivingEntity)arg).shouldRenderName() || ((Entity)arg).hasCustomName() && arg == this.dispatcher.targetedEntity);
    }

    @Override
    protected float getShadowRadius(T arg) {
        return super.getShadowRadius(arg) * ((LivingEntity)arg).getScaleFactor();
    }

    @Override
    protected /* synthetic */ float getShadowRadius(LivingEntity arg) {
        return this.getShadowRadius((T)((MobEntity)arg));
    }

    @Override
    protected /* synthetic */ boolean hasLabel(LivingEntity arg) {
        return this.hasLabel((T)((MobEntity)arg));
    }

    @Override
    protected /* synthetic */ float getShadowRadius(Entity entity) {
        return this.getShadowRadius((T)((MobEntity)entity));
    }

    @Override
    protected /* synthetic */ boolean hasLabel(Entity entity) {
        return this.hasLabel((T)((MobEntity)entity));
    }
}

