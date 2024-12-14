/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.resource;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import net.minecraft.resource.ResourceManager;
import net.minecraft.resource.ResourceReloader;
import net.minecraft.util.Unit;
import net.minecraft.util.profiler.Profiler;

public interface SynchronousResourceReloader
extends ResourceReloader {
    @Override
    default public CompletableFuture<Void> reload(ResourceReloader.Synchronizer synchronizer, ResourceManager manager, Profiler prepareProfiler, Profiler applyProfiler, Executor prepareExecutor, Executor applyExecutor) {
        return synchronizer.whenPrepared(Unit.INSTANCE).thenRunAsync(() -> {
            applyProfiler.startTick();
            applyProfiler.push("listener");
            this.reload(manager);
            applyProfiler.pop();
            applyProfiler.endTick();
        }, applyExecutor);
    }

    public void reload(ResourceManager var1);
}

