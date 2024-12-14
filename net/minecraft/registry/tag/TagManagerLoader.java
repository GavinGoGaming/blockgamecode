/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.registry.tag;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.stream.Collectors;
import net.minecraft.registry.DynamicRegistryManager;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.registry.tag.TagGroupLoader;
import net.minecraft.resource.ResourceManager;
import net.minecraft.resource.ResourceReloader;
import net.minecraft.util.Identifier;
import net.minecraft.util.profiler.Profiler;

public class TagManagerLoader
implements ResourceReloader {
    private final DynamicRegistryManager registryManager;
    private List<RegistryTags<?>> registryTags = List.of();

    public TagManagerLoader(DynamicRegistryManager registryManager) {
        this.registryManager = registryManager;
    }

    public List<RegistryTags<?>> getRegistryTags() {
        return this.registryTags;
    }

    @Override
    public CompletableFuture<Void> reload(ResourceReloader.Synchronizer synchronizer, ResourceManager manager, Profiler prepareProfiler, Profiler applyProfiler, Executor prepareExecutor, Executor applyExecutor) {
        List<CompletableFuture> list = this.registryManager.streamAllRegistries().map(registry -> this.buildRequiredGroup(manager, prepareExecutor, (DynamicRegistryManager.Entry)registry)).toList();
        return ((CompletableFuture)CompletableFuture.allOf((CompletableFuture[])list.toArray(CompletableFuture[]::new)).thenCompose(synchronizer::whenPrepared)).thenAcceptAsync(void_ -> {
            this.registryTags = list.stream().map(CompletableFuture::join).collect(Collectors.toUnmodifiableList());
        }, applyExecutor);
    }

    private <T> CompletableFuture<RegistryTags<T>> buildRequiredGroup(ResourceManager resourceManager, Executor prepareExecutor, DynamicRegistryManager.Entry<T> requirement) {
        RegistryKey lv = requirement.key();
        Registry<T> lv2 = requirement.value();
        TagGroupLoader lv3 = new TagGroupLoader(lv2::getEntry, RegistryKeys.getTagPath(lv));
        return CompletableFuture.supplyAsync(() -> new RegistryTags(lv, lv3.load(resourceManager)), prepareExecutor);
    }

    public record RegistryTags<T>(RegistryKey<? extends Registry<T>> key, Map<Identifier, Collection<RegistryEntry<T>>> tags) {
    }
}

