/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.resource;

import net.minecraft.resource.ResourceManager;

public interface LifecycledResourceManager
extends ResourceManager,
AutoCloseable {
    @Override
    public void close();
}

