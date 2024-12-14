/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 * 
 * Could not load the following classes:
 *  net.fabricmc.api.EnvType
 *  net.fabricmc.api.Environment
 */
package net.minecraft.client.gui.screen.recipebook;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.item.ItemStack;
import net.minecraft.recipe.Recipe;
import net.minecraft.recipe.RecipeEntry;
import net.minecraft.recipe.RecipeMatcher;
import net.minecraft.recipe.book.RecipeBook;
import net.minecraft.registry.DynamicRegistryManager;

@Environment(value=EnvType.CLIENT)
public class RecipeResultCollection {
    private final DynamicRegistryManager registryManager;
    private final List<RecipeEntry<?>> recipes;
    private final boolean singleOutput;
    private final Set<RecipeEntry<?>> craftableRecipes = Sets.newHashSet();
    private final Set<RecipeEntry<?>> fittingRecipes = Sets.newHashSet();
    private final Set<RecipeEntry<?>> unlockedRecipes = Sets.newHashSet();

    public RecipeResultCollection(DynamicRegistryManager registryManager, List<RecipeEntry<?>> recipes) {
        this.registryManager = registryManager;
        this.recipes = ImmutableList.copyOf(recipes);
        this.singleOutput = recipes.size() <= 1 ? true : RecipeResultCollection.shouldHaveSingleOutput(registryManager, recipes);
    }

    private static boolean shouldHaveSingleOutput(DynamicRegistryManager registryManager, List<RecipeEntry<?>> recipes) {
        int i = recipes.size();
        ItemStack lv = recipes.get(0).value().getResult(registryManager);
        for (int j = 1; j < i; ++j) {
            ItemStack lv2 = recipes.get(j).value().getResult(registryManager);
            if (ItemStack.areItemsAndComponentsEqual(lv, lv2)) continue;
            return false;
        }
        return true;
    }

    public DynamicRegistryManager getRegistryManager() {
        return this.registryManager;
    }

    public boolean isInitialized() {
        return !this.unlockedRecipes.isEmpty();
    }

    public void initialize(RecipeBook recipeBook) {
        for (RecipeEntry<?> lv : this.recipes) {
            if (!recipeBook.contains(lv)) continue;
            this.unlockedRecipes.add(lv);
        }
    }

    public void computeCraftables(RecipeMatcher recipeFinder, int gridWidth, int gridHeight, RecipeBook recipeBook) {
        for (RecipeEntry<?> lv : this.recipes) {
            boolean bl;
            boolean bl2 = bl = lv.value().fits(gridWidth, gridHeight) && recipeBook.contains(lv);
            if (bl) {
                this.fittingRecipes.add(lv);
            } else {
                this.fittingRecipes.remove(lv);
            }
            if (bl && recipeFinder.match((Recipe<?>)lv.value(), null)) {
                this.craftableRecipes.add(lv);
                continue;
            }
            this.craftableRecipes.remove(lv);
        }
    }

    public boolean isCraftable(RecipeEntry<?> recipe) {
        return this.craftableRecipes.contains(recipe);
    }

    public boolean hasCraftableRecipes() {
        return !this.craftableRecipes.isEmpty();
    }

    public boolean hasFittingRecipes() {
        return !this.fittingRecipes.isEmpty();
    }

    public List<RecipeEntry<?>> getAllRecipes() {
        return this.recipes;
    }

    public List<RecipeEntry<?>> getResults(boolean craftableOnly) {
        ArrayList<RecipeEntry<?>> list = Lists.newArrayList();
        Set<RecipeEntry<?>> set = craftableOnly ? this.craftableRecipes : this.fittingRecipes;
        for (RecipeEntry<?> lv : this.recipes) {
            if (!set.contains(lv)) continue;
            list.add(lv);
        }
        return list;
    }

    public List<RecipeEntry<?>> getRecipes(boolean craftable) {
        ArrayList<RecipeEntry<?>> list = Lists.newArrayList();
        for (RecipeEntry<?> lv : this.recipes) {
            if (!this.fittingRecipes.contains(lv) || this.craftableRecipes.contains(lv) != craftable) continue;
            list.add(lv);
        }
        return list;
    }

    public boolean hasSingleOutput() {
        return this.singleOutput;
    }
}

