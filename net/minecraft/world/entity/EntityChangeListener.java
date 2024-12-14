/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.world.entity;

import net.minecraft.entity.Entity;

public interface EntityChangeListener {
    public static final EntityChangeListener NONE = new EntityChangeListener(){

        @Override
        public void updateEntityPosition() {
        }

        @Override
        public void remove(Entity.RemovalReason reason) {
        }
    };

    public void updateEntityPosition();

    public void remove(Entity.RemovalReason var1);
}

