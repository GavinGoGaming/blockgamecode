/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.entity.mob;

import net.minecraft.entity.EntityType;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.world.World;

public abstract class AmbientEntity
extends MobEntity {
    protected AmbientEntity(EntityType<? extends AmbientEntity> arg, World arg2) {
        super((EntityType<? extends MobEntity>)arg, arg2);
    }

    @Override
    public boolean canBeLeashed() {
        return false;
    }
}

