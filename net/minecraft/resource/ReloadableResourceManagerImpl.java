/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.resource;

import com.google.common.collect.Lists;
import com.mojang.logging.LogUtils;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import net.minecraft.resource.LifecycledResourceManager;
import net.minecraft.resource.LifecycledResourceManagerImpl;
import net.minecraft.resource.Resource;
import net.minecraft.resource.ResourceManager;
import net.minecraft.resource.ResourcePack;
import net.minecraft.resource.ResourceReload;
import net.minecraft.resource.ResourceReloader;
import net.minecraft.resource.ResourceType;
import net.minecraft.resource.SimpleResourceReload;
import net.minecraft.util.Identifier;
import net.minecraft.util.Unit;
import org.slf4j.Logger;

public class ReloadableResourceManagerImpl
implements ResourceManager,
AutoCloseable {
    private static final Logger LOGGER = LogUtils.getLogger();
    private LifecycledResourceManager activeManager;
    private final List<ResourceReloader> reloaders = Lists.newArrayList();
    private final ResourceType type;

    public ReloadableResourceManagerImpl(ResourceType type) {
        this.type = type;
        this.activeManager = new LifecycledResourceManagerImpl(type, List.of());
    }

    @Override
    public void close() {
        this.activeManager.close();
    }

    public void registerReloader(ResourceReloader reloader) {
        this.reloaders.add(reloader);
    }

    public ResourceReload reload(Executor prepareExecutor, Executor applyExecutor, CompletableFuture<Unit> initialStage, List<ResourcePack> packs) {
        LOGGER.info("Reloading ResourceManager: {}", LogUtils.defer(() -> packs.stream().map(ResourcePack::getId).collect(Collectors.joining(", "))));
        this.activeManager.close();
        this.activeManager = new LifecycledResourceManagerImpl(this.type, packs);
        return SimpleResourceReload.start(this.activeManager, this.reloaders, prepareExecutor, applyExecutor, initialStage, LOGGER.isDebugEnabled());
    }

    @Override
    public Optional<Resource> getResource(Identifier id) {
        return this.activeManager.getResource(id);
    }

    @Override
    public Set<String> getAllNamespaces() {
        return this.activeManager.getAllNamespaces();
    }

    @Override
    public List<Resource> getAllResources(Identifier id) {
        return this.activeManager.getAllResources(id);
    }

    @Override
    public Map<Identifier, Resource> findResources(String startingPath, Predicate<Identifier> allowedPathPredicate) {
        return this.activeManager.findResources(startingPath, allowedPathPredicate);
    }

    @Override
    public Map<Identifier, List<Resource>> findAllResources(String startingPath, Predicate<Identifier> allowedPathPredicate) {
        return this.activeManager.findAllResources(startingPath, allowedPathPredicate);
    }

    @Override
    public Stream<ResourcePack> streamResourcePacks() {
        return this.activeManager.streamResourcePacks();
    }
}

