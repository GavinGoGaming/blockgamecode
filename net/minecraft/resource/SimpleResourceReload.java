/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.resource;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.Executor;
import java.util.concurrent.atomic.AtomicInteger;
import net.minecraft.resource.ProfiledResourceReload;
import net.minecraft.resource.ResourceManager;
import net.minecraft.resource.ResourceReload;
import net.minecraft.resource.ResourceReloader;
import net.minecraft.util.Unit;
import net.minecraft.util.Util;
import net.minecraft.util.profiler.DummyProfiler;

public class SimpleResourceReload<S>
implements ResourceReload {
    private static final int FIRST_PREPARE_APPLY_WEIGHT = 2;
    private static final int SECOND_PREPARE_APPLY_WEIGHT = 2;
    private static final int RELOADER_WEIGHT = 1;
    protected final CompletableFuture<Unit> prepareStageFuture = new CompletableFuture();
    protected CompletableFuture<List<S>> applyStageFuture;
    final Set<ResourceReloader> waitingReloaders;
    private final int reloaderCount;
    private int toApplyCount;
    private int appliedCount;
    private final AtomicInteger toPrepareCount = new AtomicInteger();
    private final AtomicInteger preparedCount = new AtomicInteger();

    public static SimpleResourceReload<Void> create(ResourceManager manager, List<ResourceReloader> reloaders, Executor prepareExecutor, Executor applyExecutor, CompletableFuture<Unit> initialStage) {
        return new SimpleResourceReload<Void>(prepareExecutor, applyExecutor, manager, reloaders, (synchronizer, resourceManager, reloader, prepare, apply) -> reloader.reload(synchronizer, resourceManager, DummyProfiler.INSTANCE, DummyProfiler.INSTANCE, prepareExecutor, apply), initialStage);
    }

    protected SimpleResourceReload(Executor prepareExecutor, final Executor applyExecutor, ResourceManager manager, List<ResourceReloader> reloaders, Factory<S> factory, CompletableFuture<Unit> initialStage) {
        this.reloaderCount = reloaders.size();
        this.toPrepareCount.incrementAndGet();
        initialStage.thenRun(this.preparedCount::incrementAndGet);
        ArrayList<CompletableFuture<S>> list2 = Lists.newArrayList();
        CompletableFuture<Unit> completableFuture2 = initialStage;
        this.waitingReloaders = Sets.newHashSet(reloaders);
        for (final ResourceReloader lv : reloaders) {
            final CompletableFuture<Unit> completableFuture3 = completableFuture2;
            CompletableFuture<S> completableFuture4 = factory.create(new ResourceReloader.Synchronizer(){

                @Override
                public <T> CompletableFuture<T> whenPrepared(T preparedObject) {
                    applyExecutor.execute(() -> {
                        SimpleResourceReload.this.waitingReloaders.remove(lv);
                        if (SimpleResourceReload.this.waitingReloaders.isEmpty()) {
                            SimpleResourceReload.this.prepareStageFuture.complete(Unit.INSTANCE);
                        }
                    });
                    return SimpleResourceReload.this.prepareStageFuture.thenCombine((CompletionStage)completableFuture3, (arg, object2) -> preparedObject);
                }
            }, manager, lv, preparation -> {
                this.toPrepareCount.incrementAndGet();
                prepareExecutor.execute(() -> {
                    preparation.run();
                    this.preparedCount.incrementAndGet();
                });
            }, application -> {
                ++this.toApplyCount;
                applyExecutor.execute(() -> {
                    application.run();
                    ++this.appliedCount;
                });
            });
            list2.add(completableFuture4);
            completableFuture2 = completableFuture4;
        }
        this.applyStageFuture = Util.combine(list2);
    }

    @Override
    public CompletableFuture<?> whenComplete() {
        return this.applyStageFuture;
    }

    @Override
    public float getProgress() {
        int i = this.reloaderCount - this.waitingReloaders.size();
        float f = this.preparedCount.get() * 2 + this.appliedCount * 2 + i * 1;
        float g = this.toPrepareCount.get() * 2 + this.toApplyCount * 2 + this.reloaderCount * 1;
        return f / g;
    }

    public static ResourceReload start(ResourceManager manager, List<ResourceReloader> reloaders, Executor prepareExecutor, Executor applyExecutor, CompletableFuture<Unit> initialStage, boolean profiled) {
        if (profiled) {
            return new ProfiledResourceReload(manager, reloaders, prepareExecutor, applyExecutor, initialStage);
        }
        return SimpleResourceReload.create(manager, reloaders, prepareExecutor, applyExecutor, initialStage);
    }

    protected static interface Factory<S> {
        public CompletableFuture<S> create(ResourceReloader.Synchronizer var1, ResourceManager var2, ResourceReloader var3, Executor var4, Executor var5);
    }
}

