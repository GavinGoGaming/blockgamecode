/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.server.function;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.datafixers.util.Pair;
import com.mojang.logging.LogUtils;
import java.io.BufferedReader;
import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.Executor;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.tag.TagGroupLoader;
import net.minecraft.resource.Resource;
import net.minecraft.resource.ResourceFinder;
import net.minecraft.resource.ResourceManager;
import net.minecraft.resource.ResourceReloader;
import net.minecraft.screen.ScreenTexts;
import net.minecraft.server.command.CommandOutput;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.function.CommandFunction;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Vec2f;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.profiler.Profiler;
import org.slf4j.Logger;

public class FunctionLoader
implements ResourceReloader {
    private static final Logger LOGGER = LogUtils.getLogger();
    public static final RegistryKey<Registry<CommandFunction<ServerCommandSource>>> FUNCTION_REGISTRY_KEY = RegistryKey.ofRegistry(Identifier.ofVanilla("function"));
    private static final ResourceFinder FINDER = new ResourceFinder(RegistryKeys.getPath(FUNCTION_REGISTRY_KEY), ".mcfunction");
    private volatile Map<Identifier, CommandFunction<ServerCommandSource>> functions = ImmutableMap.of();
    private final TagGroupLoader<CommandFunction<ServerCommandSource>> tagLoader = new TagGroupLoader<CommandFunction<ServerCommandSource>>(this::get, RegistryKeys.getTagPath(FUNCTION_REGISTRY_KEY));
    private volatile Map<Identifier, Collection<CommandFunction<ServerCommandSource>>> tags = Map.of();
    private final int level;
    private final CommandDispatcher<ServerCommandSource> commandDispatcher;

    public Optional<CommandFunction<ServerCommandSource>> get(Identifier id) {
        return Optional.ofNullable(this.functions.get(id));
    }

    public Map<Identifier, CommandFunction<ServerCommandSource>> getFunctions() {
        return this.functions;
    }

    public Collection<CommandFunction<ServerCommandSource>> getTagOrEmpty(Identifier id) {
        return this.tags.getOrDefault(id, List.of());
    }

    public Iterable<Identifier> getTags() {
        return this.tags.keySet();
    }

    public FunctionLoader(int level, CommandDispatcher<ServerCommandSource> commandDispatcher) {
        this.level = level;
        this.commandDispatcher = commandDispatcher;
    }

    @Override
    public CompletableFuture<Void> reload(ResourceReloader.Synchronizer synchronizer, ResourceManager manager, Profiler prepareProfiler, Profiler applyProfiler, Executor prepareExecutor, Executor applyExecutor) {
        CompletableFuture<Map> completableFuture = CompletableFuture.supplyAsync(() -> this.tagLoader.loadTags(manager), prepareExecutor);
        CompletionStage completableFuture2 = CompletableFuture.supplyAsync(() -> FINDER.findResources(manager), prepareExecutor).thenCompose(functions -> {
            HashMap<Identifier, CompletableFuture<CommandFunction>> map2 = Maps.newHashMap();
            ServerCommandSource lv = new ServerCommandSource(CommandOutput.DUMMY, Vec3d.ZERO, Vec2f.ZERO, null, this.level, "", ScreenTexts.EMPTY, null, null);
            for (Map.Entry entry : functions.entrySet()) {
                Identifier lv2 = (Identifier)entry.getKey();
                Identifier lv3 = FINDER.toResourceId(lv2);
                map2.put(lv3, CompletableFuture.supplyAsync(() -> {
                    List<String> list = FunctionLoader.readLines((Resource)entry.getValue());
                    return CommandFunction.create(lv3, this.commandDispatcher, lv, list);
                }, prepareExecutor));
            }
            CompletableFuture[] completableFutures = map2.values().toArray(new CompletableFuture[0]);
            return CompletableFuture.allOf(completableFutures).handle((unused, ex) -> map2);
        });
        return ((CompletableFuture)((CompletableFuture)completableFuture.thenCombine(completableFuture2, Pair::of)).thenCompose(synchronizer::whenPrepared)).thenAcceptAsync(intermediate -> {
            Map map = (Map)intermediate.getSecond();
            ImmutableMap.Builder builder = ImmutableMap.builder();
            map.forEach((id, functionFuture) -> ((CompletableFuture)functionFuture.handle((function, ex) -> {
                if (ex != null) {
                    LOGGER.error("Failed to load function {}", id, ex);
                } else {
                    builder.put(id, function);
                }
                return null;
            })).join());
            this.functions = builder.build();
            this.tags = this.tagLoader.buildGroup((Map)intermediate.getFirst());
        }, applyExecutor);
    }

    private static List<String> readLines(Resource resource) {
        List<String> list;
        block8: {
            BufferedReader bufferedReader = resource.getReader();
            try {
                list = bufferedReader.lines().toList();
                if (bufferedReader == null) break block8;
            } catch (Throwable throwable) {
                try {
                    if (bufferedReader != null) {
                        try {
                            bufferedReader.close();
                        } catch (Throwable throwable2) {
                            throwable.addSuppressed(throwable2);
                        }
                    }
                    throw throwable;
                } catch (IOException iOException) {
                    throw new CompletionException(iOException);
                }
            }
            bufferedReader.close();
        }
        return list;
    }
}

