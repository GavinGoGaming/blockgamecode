/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 * 
 * Could not load the following classes:
 *  org.jetbrains.annotations.Nullable
 */
package net.minecraft.recipe;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.Multimap;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.mojang.logging.LogUtils;
import com.mojang.serialization.JsonOps;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import net.minecraft.item.ItemStack;
import net.minecraft.recipe.Recipe;
import net.minecraft.recipe.RecipeEntry;
import net.minecraft.recipe.RecipeType;
import net.minecraft.recipe.input.RecipeInput;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.RegistryOps;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.resource.JsonDataLoader;
import net.minecraft.resource.ResourceManager;
import net.minecraft.util.Identifier;
import net.minecraft.util.collection.DefaultedList;
import net.minecraft.util.profiler.Profiler;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;

public class RecipeManager
extends JsonDataLoader {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create();
    private static final Logger LOGGER = LogUtils.getLogger();
    private final RegistryWrapper.WrapperLookup registryLookup;
    private Multimap<RecipeType<?>, RecipeEntry<?>> recipesByType = ImmutableMultimap.of();
    private Map<Identifier, RecipeEntry<?>> recipesById = ImmutableMap.of();
    private boolean errored;

    public RecipeManager(RegistryWrapper.WrapperLookup registryLookup) {
        super(GSON, RegistryKeys.getPath(RegistryKeys.RECIPE));
        this.registryLookup = registryLookup;
    }

    @Override
    protected void apply(Map<Identifier, JsonElement> map, ResourceManager arg, Profiler arg2) {
        this.errored = false;
        ImmutableMultimap.Builder<RecipeType<?>, RecipeEntry<Recipe>> builder = ImmutableMultimap.builder();
        ImmutableMap.Builder<Identifier, RecipeEntry<Recipe>> builder2 = ImmutableMap.builder();
        RegistryOps<JsonElement> lv = this.registryLookup.getOps(JsonOps.INSTANCE);
        for (Map.Entry<Identifier, JsonElement> entry : map.entrySet()) {
            Identifier lv2 = entry.getKey();
            try {
                Recipe lv3 = (Recipe)Recipe.CODEC.parse(lv, entry.getValue()).getOrThrow(JsonParseException::new);
                RecipeEntry<Recipe> lv4 = new RecipeEntry<Recipe>(lv2, lv3);
                builder.put(lv3.getType(), lv4);
                builder2.put(lv2, lv4);
            } catch (JsonParseException | IllegalArgumentException runtimeException) {
                LOGGER.error("Parsing error loading recipe {}", (Object)lv2, (Object)runtimeException);
            }
        }
        this.recipesByType = builder.build();
        this.recipesById = builder2.build();
        LOGGER.info("Loaded {} recipes", (Object)this.recipesByType.size());
    }

    public boolean isErrored() {
        return this.errored;
    }

    public <I extends RecipeInput, T extends Recipe<I>> Optional<RecipeEntry<T>> getFirstMatch(RecipeType<T> type, I input, World world) {
        return this.getFirstMatch(type, input, world, (RecipeEntry)null);
    }

    public <I extends RecipeInput, T extends Recipe<I>> Optional<RecipeEntry<T>> getFirstMatch(RecipeType<T> type, I input, World world, @Nullable Identifier id) {
        RecipeEntry<T> lv = id != null ? this.get(type, id) : null;
        return this.getFirstMatch(type, input, world, lv);
    }

    public <I extends RecipeInput, T extends Recipe<I>> Optional<RecipeEntry<T>> getFirstMatch(RecipeType<T> type, I input, World world, @Nullable RecipeEntry<T> recipe2) {
        if (input.isEmpty()) {
            return Optional.empty();
        }
        if (recipe2 != null && recipe2.value().matches(input, world)) {
            return Optional.of(recipe2);
        }
        return this.getAllOfType(type).stream().filter(recipe -> recipe.value().matches((RecipeInput)input, world)).findFirst();
    }

    public <I extends RecipeInput, T extends Recipe<I>> List<RecipeEntry<T>> listAllOfType(RecipeType<T> type) {
        return List.copyOf(this.getAllOfType(type));
    }

    public <I extends RecipeInput, T extends Recipe<I>> List<RecipeEntry<T>> getAllMatches(RecipeType<T> type, I input, World world) {
        return this.getAllOfType(type).stream().filter(recipe -> recipe.value().matches((RecipeInput)input, world)).sorted(Comparator.comparing(entry -> entry.value().getResult(world.getRegistryManager()).getTranslationKey())).collect(Collectors.toList());
    }

    private <I extends RecipeInput, T extends Recipe<I>> Collection<RecipeEntry<T>> getAllOfType(RecipeType<T> type) {
        return this.recipesByType.get(type);
    }

    public <I extends RecipeInput, T extends Recipe<I>> DefaultedList<ItemStack> getRemainingStacks(RecipeType<T> type, I input, World world) {
        Optional<RecipeEntry<T>> optional = this.getFirstMatch(type, input, world);
        if (optional.isPresent()) {
            return optional.get().value().getRemainder(input);
        }
        DefaultedList<ItemStack> lv = DefaultedList.ofSize(input.getSize(), ItemStack.EMPTY);
        for (int i = 0; i < lv.size(); ++i) {
            lv.set(i, input.getStackInSlot(i));
        }
        return lv;
    }

    public Optional<RecipeEntry<?>> get(Identifier id) {
        return Optional.ofNullable(this.recipesById.get(id));
    }

    @Nullable
    private <T extends Recipe<?>> RecipeEntry<T> get(RecipeType<T> type, Identifier id) {
        RecipeEntry<?> lv = this.recipesById.get(id);
        if (lv != null && lv.value().getType().equals(type)) {
            return lv;
        }
        return null;
    }

    public Collection<RecipeEntry<?>> sortedValues() {
        return this.recipesByType.values();
    }

    public Collection<RecipeEntry<?>> values() {
        return this.recipesById.values();
    }

    public Stream<Identifier> keys() {
        return this.recipesById.keySet().stream();
    }

    @VisibleForTesting
    protected static RecipeEntry<?> deserialize(Identifier id, JsonObject json, RegistryWrapper.WrapperLookup registryLookup) {
        Recipe lv = (Recipe)Recipe.CODEC.parse(registryLookup.getOps(JsonOps.INSTANCE), json).getOrThrow(JsonParseException::new);
        return new RecipeEntry<Recipe>(id, lv);
    }

    public void setRecipes(Iterable<RecipeEntry<?>> recipes) {
        this.errored = false;
        ImmutableMultimap.Builder<RecipeType<?>, RecipeEntry<?>> builder = ImmutableMultimap.builder();
        ImmutableMap.Builder<Identifier, RecipeEntry<?>> builder2 = ImmutableMap.builder();
        for (RecipeEntry<?> lv : recipes) {
            RecipeType<?> lv2 = lv.value().getType();
            builder.put(lv2, lv);
            builder2.put(lv.id(), lv);
        }
        this.recipesByType = builder.build();
        this.recipesById = builder2.build();
    }

    public static <I extends RecipeInput, T extends Recipe<I>> MatchGetter<I, T> createCachedMatchGetter(final RecipeType<T> type) {
        return new MatchGetter<I, T>(){
            @Nullable
            private Identifier id;

            @Override
            public Optional<RecipeEntry<T>> getFirstMatch(I input, World world) {
                RecipeManager lv = world.getRecipeManager();
                Optional optional = lv.getFirstMatch(type, input, world, this.id);
                if (optional.isPresent()) {
                    RecipeEntry lv2 = optional.get();
                    this.id = lv2.id();
                    return Optional.of(lv2);
                }
                return Optional.empty();
            }
        };
    }

    public static interface MatchGetter<I extends RecipeInput, T extends Recipe<I>> {
        public Optional<RecipeEntry<T>> getFirstMatch(I var1, World var2);
    }
}

