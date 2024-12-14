/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.resource;

import java.util.function.Consumer;
import net.minecraft.resource.ResourcePackProfile;

@FunctionalInterface
public interface ResourcePackProvider {
    public void register(Consumer<ResourcePackProfile> var1);
}

