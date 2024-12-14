/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 * 
 * Could not load the following classes:
 *  org.jetbrains.annotations.Nullable
 */
package net.minecraft.recipe;

import com.google.common.collect.Lists;
import it.unimi.dsi.fastutil.ints.Int2IntMap;
import it.unimi.dsi.fastutil.ints.Int2IntOpenHashMap;
import it.unimi.dsi.fastutil.ints.IntAVLTreeSet;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntIterator;
import it.unimi.dsi.fastutil.ints.IntList;
import it.unimi.dsi.fastutil.ints.IntListIterator;
import java.util.BitSet;
import java.util.List;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.recipe.Ingredient;
import net.minecraft.recipe.Recipe;
import net.minecraft.recipe.RecipeEntry;
import net.minecraft.registry.Registries;
import net.minecraft.util.collection.DefaultedList;
import org.jetbrains.annotations.Nullable;

public class RecipeMatcher {
    private static final int field_30653 = 0;
    public final Int2IntMap inputs = new Int2IntOpenHashMap();

    public void addUnenchantedInput(ItemStack stack) {
        if (!(stack.isDamaged() || stack.hasEnchantments() || stack.contains(DataComponentTypes.CUSTOM_NAME))) {
            this.addInput(stack);
        }
    }

    public void addInput(ItemStack stack) {
        this.addInput(stack, stack.getMaxCount());
    }

    public void addInput(ItemStack stack, int maxCount) {
        if (!stack.isEmpty()) {
            int j = RecipeMatcher.getItemId(stack);
            int k = Math.min(maxCount, stack.getCount());
            this.addInput(j, k);
        }
    }

    public static int getItemId(ItemStack stack) {
        return Registries.ITEM.getRawId(stack.getItem());
    }

    boolean contains(int itemId) {
        return this.inputs.get(itemId) > 0;
    }

    int consume(int itemId, int count) {
        int k = this.inputs.get(itemId);
        if (k >= count) {
            this.inputs.put(itemId, k - count);
            return itemId;
        }
        return 0;
    }

    void addInput(int itemId, int count) {
        this.inputs.put(itemId, this.inputs.get(itemId) + count);
    }

    public boolean match(Recipe<?> recipe, @Nullable IntList output) {
        return this.match(recipe, output, 1);
    }

    public boolean match(Recipe<?> recipe, @Nullable IntList output, int multiplier) {
        return new Matcher(recipe).match(multiplier, output);
    }

    public int countCrafts(RecipeEntry<?> recipe, @Nullable IntList output) {
        return this.countCrafts(recipe, Integer.MAX_VALUE, output);
    }

    public int countCrafts(RecipeEntry<?> recipe, int limit, @Nullable IntList output) {
        return new Matcher((Recipe<?>)recipe.value()).countCrafts(limit, output);
    }

    public static ItemStack getStackFromId(int itemId) {
        if (itemId == 0) {
            return ItemStack.EMPTY;
        }
        return new ItemStack(Item.byRawId(itemId));
    }

    public void clear() {
        this.inputs.clear();
    }

    class Matcher {
        private final Recipe<?> recipe;
        private final List<Ingredient> ingredients = Lists.newArrayList();
        private final int totalIngredients;
        private final int[] requiredItems;
        private final int totalRequiredItems;
        private final BitSet requirementsMatrix;
        private final IntList ingredientItemLookup = new IntArrayList();

        public Matcher(Recipe<?> recipe) {
            this.recipe = recipe;
            this.ingredients.addAll(recipe.getIngredients());
            this.ingredients.removeIf(Ingredient::isEmpty);
            this.totalIngredients = this.ingredients.size();
            this.requiredItems = this.createItemRequirementList();
            this.totalRequiredItems = this.requiredItems.length;
            this.requirementsMatrix = new BitSet(this.totalIngredients + this.totalRequiredItems + this.totalIngredients + this.totalIngredients * this.totalRequiredItems);
            for (int i = 0; i < this.ingredients.size(); ++i) {
                IntList intList = this.ingredients.get(i).getMatchingItemIds();
                for (int j = 0; j < this.totalRequiredItems; ++j) {
                    if (!intList.contains(this.requiredItems[j])) continue;
                    this.requirementsMatrix.set(this.getRequirementIndex(true, j, i));
                }
            }
        }

        public boolean match(int multiplier, @Nullable IntList output) {
            boolean bl2;
            if (multiplier <= 0) {
                return true;
            }
            int j = 0;
            while (this.checkRequirements(multiplier)) {
                RecipeMatcher.this.consume(this.requiredItems[this.ingredientItemLookup.getInt(0)], multiplier);
                int k = this.ingredientItemLookup.size() - 1;
                this.unfulfillRequirement(this.ingredientItemLookup.getInt(k));
                for (int l = 0; l < k; ++l) {
                    this.flipRequirement((l & 1) == 0, this.ingredientItemLookup.get(l), this.ingredientItemLookup.get(l + 1));
                }
                this.ingredientItemLookup.clear();
                this.requirementsMatrix.clear(0, this.totalIngredients + this.totalRequiredItems);
                ++j;
            }
            boolean bl = j == this.totalIngredients;
            boolean bl3 = bl2 = bl && output != null;
            if (bl2) {
                output.clear();
            }
            this.requirementsMatrix.clear(0, this.totalIngredients + this.totalRequiredItems + this.totalIngredients);
            int m = 0;
            DefaultedList<Ingredient> list = this.recipe.getIngredients();
            for (Ingredient lv : list) {
                if (bl2 && lv.isEmpty()) {
                    output.add(0);
                    continue;
                }
                for (int n = 0; n < this.totalRequiredItems; ++n) {
                    if (!this.checkRequirement(false, m, n)) continue;
                    this.flipRequirement(true, n, m);
                    RecipeMatcher.this.addInput(this.requiredItems[n], multiplier);
                    if (!bl2) continue;
                    output.add(this.requiredItems[n]);
                }
                ++m;
            }
            return bl;
        }

        private int[] createItemRequirementList() {
            IntAVLTreeSet intCollection = new IntAVLTreeSet();
            for (Ingredient lv : this.ingredients) {
                intCollection.addAll(lv.getMatchingItemIds());
            }
            IntIterator intIterator = intCollection.iterator();
            while (intIterator.hasNext()) {
                if (RecipeMatcher.this.contains(intIterator.nextInt())) continue;
                intIterator.remove();
            }
            return intCollection.toIntArray();
        }

        private boolean checkRequirements(int multiplier) {
            int j = this.totalRequiredItems;
            for (int k = 0; k < j; ++k) {
                if (RecipeMatcher.this.inputs.get(this.requiredItems[k]) < multiplier) continue;
                this.addRequirement(false, k);
                while (!this.ingredientItemLookup.isEmpty()) {
                    int o;
                    int l = this.ingredientItemLookup.size();
                    boolean bl = (l & 1) == 1;
                    int m = this.ingredientItemLookup.getInt(l - 1);
                    if (!bl && !this.getRequirement(m)) break;
                    int n = bl ? this.totalIngredients : j;
                    for (o = 0; o < n; ++o) {
                        if (this.isRequirementUnfulfilled(bl, o) || !this.needsRequirement(bl, m, o) || !this.checkRequirement(bl, m, o)) continue;
                        this.addRequirement(bl, o);
                        break;
                    }
                    if ((o = this.ingredientItemLookup.size()) != l) continue;
                    this.ingredientItemLookup.removeInt(o - 1);
                }
                if (this.ingredientItemLookup.isEmpty()) continue;
                return true;
            }
            return false;
        }

        private boolean getRequirement(int itemId) {
            return this.requirementsMatrix.get(this.getRequirementIndex(itemId));
        }

        private void unfulfillRequirement(int itemId) {
            this.requirementsMatrix.set(this.getRequirementIndex(itemId));
        }

        private int getRequirementIndex(int itemId) {
            return this.totalIngredients + this.totalRequiredItems + itemId;
        }

        private boolean needsRequirement(boolean reversed, int itemIndex, int ingredientIndex) {
            return this.requirementsMatrix.get(this.getRequirementIndex(reversed, itemIndex, ingredientIndex));
        }

        private boolean checkRequirement(boolean reversed, int itemIndex, int ingredientIndex) {
            return reversed != this.requirementsMatrix.get(1 + this.getRequirementIndex(reversed, itemIndex, ingredientIndex));
        }

        private void flipRequirement(boolean reversed, int itemIndex, int ingredientIndex) {
            this.requirementsMatrix.flip(1 + this.getRequirementIndex(reversed, itemIndex, ingredientIndex));
        }

        private int getRequirementIndex(boolean reversed, int itemIndex, int ingredientIndex) {
            int k = reversed ? itemIndex * this.totalIngredients + ingredientIndex : ingredientIndex * this.totalIngredients + itemIndex;
            return this.totalIngredients + this.totalRequiredItems + this.totalIngredients + 2 * k;
        }

        private void addRequirement(boolean reversed, int itemId) {
            this.requirementsMatrix.set(this.getRequirementIndex(reversed, itemId));
            this.ingredientItemLookup.add(itemId);
        }

        private boolean isRequirementUnfulfilled(boolean reversed, int itemId) {
            return this.requirementsMatrix.get(this.getRequirementIndex(reversed, itemId));
        }

        private int getRequirementIndex(boolean reversed, int itemId) {
            return (reversed ? 0 : this.totalIngredients) + itemId;
        }

        public int countCrafts(int minimum, @Nullable IntList output) {
            int l;
            int j = 0;
            int k = Math.min(minimum, this.getMaximumCrafts()) + 1;
            while (true) {
                if (this.match(l = (j + k) / 2, null)) {
                    if (k - j <= 1) break;
                    j = l;
                    continue;
                }
                k = l;
            }
            if (l > 0) {
                this.match(l, output);
            }
            return l;
        }

        private int getMaximumCrafts() {
            int i = Integer.MAX_VALUE;
            for (Ingredient lv : this.ingredients) {
                int j = 0;
                IntListIterator intListIterator = lv.getMatchingItemIds().iterator();
                while (intListIterator.hasNext()) {
                    int k = (Integer)intListIterator.next();
                    j = Math.max(j, RecipeMatcher.this.inputs.get(k));
                }
                if (i <= 0) continue;
                i = Math.min(i, j);
            }
            return i;
        }
    }
}

