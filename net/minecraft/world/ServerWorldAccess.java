/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.world;

import net.minecraft.entity.Entity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.world.WorldAccess;

public interface ServerWorldAccess
extends WorldAccess {
    public ServerWorld toServerWorld();

    default public void spawnEntityAndPassengers(Entity entity) {
        entity.streamSelfAndPassengers().forEach(this::spawnEntity);
    }
}

