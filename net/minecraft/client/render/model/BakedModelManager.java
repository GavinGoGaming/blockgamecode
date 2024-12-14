/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 * 
 * Could not load the following classes:
 *  net.fabricmc.api.EnvType
 *  net.fabricmc.api.Environment
 */
package net.minecraft.client.render.model;

import com.google.common.collect.HashMultimap;
import com.google.gson.JsonObject;
import com.mojang.datafixers.util.Pair;
import com.mojang.logging.LogUtils;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import java.io.BufferedReader;
import java.io.Reader;
import java.util.ArrayList;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.Executor;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.client.color.block.BlockColors;
import net.minecraft.client.render.TexturedRenderLayers;
import net.minecraft.client.render.block.BlockModels;
import net.minecraft.client.render.model.BakedModel;
import net.minecraft.client.render.model.BlockStatesLoader;
import net.minecraft.client.render.model.ModelLoader;
import net.minecraft.client.render.model.SpriteAtlasManager;
import net.minecraft.client.render.model.json.JsonUnbakedModel;
import net.minecraft.client.texture.Sprite;
import net.minecraft.client.texture.SpriteAtlasTexture;
import net.minecraft.client.texture.TextureManager;
import net.minecraft.client.util.ModelIdentifier;
import net.minecraft.client.util.SpriteIdentifier;
import net.minecraft.fluid.FluidState;
import net.minecraft.registry.Registries;
import net.minecraft.resource.Resource;
import net.minecraft.resource.ResourceManager;
import net.minecraft.resource.ResourceReloader;
import net.minecraft.util.Identifier;
import net.minecraft.util.JsonHelper;
import net.minecraft.util.Util;
import net.minecraft.util.profiler.Profiler;
import org.slf4j.Logger;

@Environment(value=EnvType.CLIENT)
public class BakedModelManager
implements ResourceReloader,
AutoCloseable {
    private static final Logger LOGGER = LogUtils.getLogger();
    private static final Map<Identifier, Identifier> LAYERS_TO_LOADERS = Map.of(TexturedRenderLayers.BANNER_PATTERNS_ATLAS_TEXTURE, Identifier.ofVanilla("banner_patterns"), TexturedRenderLayers.BEDS_ATLAS_TEXTURE, Identifier.ofVanilla("beds"), TexturedRenderLayers.CHEST_ATLAS_TEXTURE, Identifier.ofVanilla("chests"), TexturedRenderLayers.SHIELD_PATTERNS_ATLAS_TEXTURE, Identifier.ofVanilla("shield_patterns"), TexturedRenderLayers.SIGNS_ATLAS_TEXTURE, Identifier.ofVanilla("signs"), TexturedRenderLayers.SHULKER_BOXES_ATLAS_TEXTURE, Identifier.ofVanilla("shulker_boxes"), TexturedRenderLayers.ARMOR_TRIMS_ATLAS_TEXTURE, Identifier.ofVanilla("armor_trims"), TexturedRenderLayers.DECORATED_POT_ATLAS_TEXTURE, Identifier.ofVanilla("decorated_pot"), SpriteAtlasTexture.BLOCK_ATLAS_TEXTURE, Identifier.ofVanilla("blocks"));
    private Map<ModelIdentifier, BakedModel> models;
    private final SpriteAtlasManager atlasManager;
    private final BlockModels blockModelCache;
    private final BlockColors colorMap;
    private int mipmapLevels;
    private BakedModel missingModel;
    private Object2IntMap<BlockState> stateLookup;

    public BakedModelManager(TextureManager textureManager, BlockColors colorMap, int mipmap) {
        this.colorMap = colorMap;
        this.mipmapLevels = mipmap;
        this.blockModelCache = new BlockModels(this);
        this.atlasManager = new SpriteAtlasManager(LAYERS_TO_LOADERS, textureManager);
    }

    public BakedModel getModel(ModelIdentifier id) {
        return this.models.getOrDefault(id, this.missingModel);
    }

    public BakedModel getMissingModel() {
        return this.missingModel;
    }

    public BlockModels getBlockModels() {
        return this.blockModelCache;
    }

    @Override
    public final CompletableFuture<Void> reload(ResourceReloader.Synchronizer synchronizer, ResourceManager manager, Profiler prepareProfiler, Profiler applyProfiler, Executor prepareExecutor, Executor applyExecutor) {
        prepareProfiler.startTick();
        CompletableFuture<Map<Identifier, JsonUnbakedModel>> completableFuture = BakedModelManager.reloadModels(manager, prepareExecutor);
        CompletableFuture<Map<Identifier, List<BlockStatesLoader.SourceTrackedData>>> completableFuture2 = BakedModelManager.reloadBlockStates(manager, prepareExecutor);
        CompletionStage completableFuture3 = completableFuture.thenCombineAsync(completableFuture2, (jsonUnbakedModels, blockStates) -> new ModelLoader(this.colorMap, prepareProfiler, (Map<Identifier, JsonUnbakedModel>)jsonUnbakedModels, (Map<Identifier, List<BlockStatesLoader.SourceTrackedData>>)blockStates), prepareExecutor);
        Map<Identifier, CompletableFuture<SpriteAtlasManager.AtlasPreparation>> map = this.atlasManager.reload(manager, this.mipmapLevels, prepareExecutor);
        return ((CompletableFuture)((CompletableFuture)((CompletableFuture)CompletableFuture.allOf((CompletableFuture[])Stream.concat(map.values().stream(), Stream.of(completableFuture3)).toArray(CompletableFuture[]::new)).thenApplyAsync(void_2 -> this.bake(prepareProfiler, map.entrySet().stream().collect(Collectors.toMap(Map.Entry::getKey, entry -> (SpriteAtlasManager.AtlasPreparation)((CompletableFuture)entry.getValue()).join())), (ModelLoader)((CompletableFuture)completableFuture3).join()), prepareExecutor)).thenCompose(result -> result.readyForUpload.thenApply(void_ -> result))).thenCompose(synchronizer::whenPrepared)).thenAcceptAsync(result -> this.upload((BakingResult)result, applyProfiler), applyExecutor);
    }

    private static CompletableFuture<Map<Identifier, JsonUnbakedModel>> reloadModels(ResourceManager resourceManager, Executor executor) {
        return CompletableFuture.supplyAsync(() -> ModelLoader.MODELS_FINDER.findResources(resourceManager), executor).thenCompose(models2 -> {
            ArrayList<CompletableFuture<Pair>> list = new ArrayList<CompletableFuture<Pair>>(models2.size());
            for (Map.Entry entry : models2.entrySet()) {
                list.add(CompletableFuture.supplyAsync(() -> {
                    Pair<Identifier, JsonUnbakedModel> pair;
                    block8: {
                        BufferedReader reader = ((Resource)entry.getValue()).getReader();
                        try {
                            pair = Pair.of((Identifier)entry.getKey(), JsonUnbakedModel.deserialize(reader));
                            if (reader == null) break block8;
                        } catch (Throwable throwable) {
                            try {
                                if (reader != null) {
                                    try {
                                        ((Reader)reader).close();
                                    } catch (Throwable throwable2) {
                                        throwable.addSuppressed(throwable2);
                                    }
                                }
                                throw throwable;
                            } catch (Exception exception) {
                                LOGGER.error("Failed to load model {}", entry.getKey(), (Object)exception);
                                return null;
                            }
                        }
                        ((Reader)reader).close();
                    }
                    return pair;
                }, executor));
            }
            return Util.combineSafe(list).thenApply(models -> models.stream().filter(Objects::nonNull).collect(Collectors.toUnmodifiableMap(Pair::getFirst, Pair::getSecond)));
        });
    }

    private static CompletableFuture<Map<Identifier, List<BlockStatesLoader.SourceTrackedData>>> reloadBlockStates(ResourceManager resourceManager, Executor executor) {
        return CompletableFuture.supplyAsync(() -> BlockStatesLoader.FINDER.findAllResources(resourceManager), executor).thenCompose(blockStates2 -> {
            ArrayList<CompletableFuture<Pair>> list = new ArrayList<CompletableFuture<Pair>>(blockStates2.size());
            for (Map.Entry entry : blockStates2.entrySet()) {
                list.add(CompletableFuture.supplyAsync(() -> {
                    List list = (List)entry.getValue();
                    ArrayList<BlockStatesLoader.SourceTrackedData> list2 = new ArrayList<BlockStatesLoader.SourceTrackedData>(list.size());
                    for (Resource lv : list) {
                        try {
                            BufferedReader reader = lv.getReader();
                            try {
                                JsonObject jsonObject = JsonHelper.deserialize(reader);
                                list2.add(new BlockStatesLoader.SourceTrackedData(lv.getPackId(), jsonObject));
                            } finally {
                                if (reader == null) continue;
                                ((Reader)reader).close();
                            }
                        } catch (Exception exception) {
                            LOGGER.error("Failed to load blockstate {} from pack {}", entry.getKey(), lv.getPackId(), exception);
                        }
                    }
                    return Pair.of((Identifier)entry.getKey(), list2);
                }, executor));
            }
            return Util.combineSafe(list).thenApply(blockStates -> blockStates.stream().filter(Objects::nonNull).collect(Collectors.toUnmodifiableMap(Pair::getFirst, Pair::getSecond)));
        });
    }

    private BakingResult bake(Profiler profiler, Map<Identifier, SpriteAtlasManager.AtlasPreparation> preparations, ModelLoader modelLoader) {
        profiler.push("load");
        profiler.swap("baking");
        HashMultimap multimap = HashMultimap.create();
        modelLoader.bake((arg, spriteId) -> {
            SpriteAtlasManager.AtlasPreparation lv = (SpriteAtlasManager.AtlasPreparation)preparations.get(spriteId.getAtlasId());
            Sprite lv2 = lv.getSprite(spriteId.getTextureId());
            if (lv2 != null) {
                return lv2;
            }
            multimap.put(arg, spriteId);
            return lv.getMissingSprite();
        });
        multimap.asMap().forEach((arg2, spriteIds) -> LOGGER.warn("Missing textures in model {}:\n{}", arg2, (Object)spriteIds.stream().sorted(SpriteIdentifier.COMPARATOR).map(arg -> "    " + String.valueOf(arg.getAtlasId()) + ":" + String.valueOf(arg.getTextureId())).collect(Collectors.joining("\n"))));
        profiler.swap("dispatch");
        Map<ModelIdentifier, BakedModel> map2 = modelLoader.getBakedModelMap();
        BakedModel lv = map2.get(ModelLoader.MISSING_MODEL_ID);
        IdentityHashMap<BlockState, BakedModel> map3 = new IdentityHashMap<BlockState, BakedModel>();
        for (Block lv2 : Registries.BLOCK) {
            lv2.getStateManager().getStates().forEach(state -> {
                Identifier lv = state.getBlock().getRegistryEntry().registryKey().getValue();
                BakedModel lv2 = map2.getOrDefault(BlockModels.getModelId(lv, state), lv);
                map3.put((BlockState)state, lv2);
            });
        }
        CompletableFuture<Void> completableFuture = CompletableFuture.allOf((CompletableFuture[])preparations.values().stream().map(SpriteAtlasManager.AtlasPreparation::whenComplete).toArray(CompletableFuture[]::new));
        profiler.pop();
        profiler.endTick();
        return new BakingResult(modelLoader, lv, map3, preparations, completableFuture);
    }

    private void upload(BakingResult bakingResult, Profiler profiler) {
        profiler.startTick();
        profiler.push("upload");
        bakingResult.atlasPreparations.values().forEach(SpriteAtlasManager.AtlasPreparation::upload);
        ModelLoader lv = bakingResult.modelLoader;
        this.models = lv.getBakedModelMap();
        this.stateLookup = lv.getStateLookup();
        this.missingModel = bakingResult.missingModel;
        profiler.swap("cache");
        this.blockModelCache.setModels(bakingResult.modelCache);
        profiler.pop();
        profiler.endTick();
    }

    public boolean shouldRerender(BlockState from, BlockState to) {
        int j;
        if (from == to) {
            return false;
        }
        int i = this.stateLookup.getInt(from);
        if (i != -1 && i == (j = this.stateLookup.getInt(to))) {
            FluidState lv2;
            FluidState lv = from.getFluidState();
            return lv != (lv2 = to.getFluidState());
        }
        return true;
    }

    public SpriteAtlasTexture getAtlas(Identifier id) {
        return this.atlasManager.getAtlas(id);
    }

    @Override
    public void close() {
        this.atlasManager.close();
    }

    public void setMipmapLevels(int mipmapLevels) {
        this.mipmapLevels = mipmapLevels;
    }

    @Environment(value=EnvType.CLIENT)
    record BakingResult(ModelLoader modelLoader, BakedModel missingModel, Map<BlockState, BakedModel> modelCache, Map<Identifier, SpriteAtlasManager.AtlasPreparation> atlasPreparations, CompletableFuture<Void> readyForUpload) {
    }
}

