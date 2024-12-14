/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.resource;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import net.minecraft.resource.ResourceManager;
import net.minecraft.resource.ResourceReloader;
import net.minecraft.util.profiler.Profiler;

public abstract class SinglePreparationResourceReloader<T>
implements ResourceReloader {
    @Override
    public final CompletableFuture<Void> reload(ResourceReloader.Synchronizer synchronizer, ResourceManager manager, Profiler prepareProfiler, Profiler applyProfiler, Executor prepareExecutor, Executor applyExecutor) {
        return ((CompletableFuture)CompletableFuture.supplyAsync(() -> this.prepare(manager, prepareProfiler), prepareExecutor).thenCompose(synchronizer::whenPrepared)).thenAcceptAsync(prepared -> this.apply(prepared, manager, applyProfiler), applyExecutor);
    }

    protected abstract T prepare(ResourceManager var1, Profiler var2);

    protected abstract void apply(T var1, ResourceManager var2, Profiler var3);
}

