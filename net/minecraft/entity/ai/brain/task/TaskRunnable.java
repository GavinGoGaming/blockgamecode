/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.entity.ai.brain.task;

import net.minecraft.entity.LivingEntity;
import net.minecraft.server.world.ServerWorld;

public interface TaskRunnable<E extends LivingEntity> {
    public boolean trigger(ServerWorld var1, E var2, long var3);
}

