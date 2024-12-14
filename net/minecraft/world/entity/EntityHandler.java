/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.world.entity;

public interface EntityHandler<T> {
    public void create(T var1);

    public void destroy(T var1);

    public void startTicking(T var1);

    public void stopTicking(T var1);

    public void startTracking(T var1);

    public void stopTracking(T var1);

    public void updateLoadStatus(T var1);
}

