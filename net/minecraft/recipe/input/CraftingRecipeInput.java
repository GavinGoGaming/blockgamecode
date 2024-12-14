/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.recipe.input;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.item.ItemStack;
import net.minecraft.recipe.RecipeMatcher;
import net.minecraft.recipe.input.RecipeInput;

public class CraftingRecipeInput
implements RecipeInput {
    public static final CraftingRecipeInput EMPTY = new CraftingRecipeInput(0, 0, List.of());
    private final int width;
    private final int height;
    private final List<ItemStack> stacks;
    private final RecipeMatcher matcher = new RecipeMatcher();
    private final int stackCount;

    private CraftingRecipeInput(int width, int height, List<ItemStack> stacks) {
        this.width = width;
        this.height = height;
        this.stacks = stacks;
        int k = 0;
        for (ItemStack lv : stacks) {
            if (lv.isEmpty()) continue;
            ++k;
            this.matcher.addInput(lv, 1);
        }
        this.stackCount = k;
    }

    public static CraftingRecipeInput create(int width, int height, List<ItemStack> stacks) {
        return CraftingRecipeInput.createPositioned(width, height, stacks).input();
    }

    public static Positioned createPositioned(int width, int height, List<ItemStack> stacks) {
        int o;
        if (width == 0 || height == 0) {
            return Positioned.EMPTY;
        }
        int k = width - 1;
        int l = 0;
        int m = height - 1;
        int n = 0;
        for (o = 0; o < height; ++o) {
            boolean bl = true;
            for (int p = 0; p < width; ++p) {
                ItemStack lv = stacks.get(p + o * width);
                if (lv.isEmpty()) continue;
                k = Math.min(k, p);
                l = Math.max(l, p);
                bl = false;
            }
            if (bl) continue;
            m = Math.min(m, o);
            n = Math.max(n, o);
        }
        o = l - k + 1;
        int q = n - m + 1;
        if (o <= 0 || q <= 0) {
            return Positioned.EMPTY;
        }
        if (o == width && q == height) {
            return new Positioned(new CraftingRecipeInput(width, height, stacks), k, m);
        }
        ArrayList<ItemStack> list2 = new ArrayList<ItemStack>(o * q);
        for (int r = 0; r < q; ++r) {
            for (int s = 0; s < o; ++s) {
                int t = s + k + (r + m) * width;
                list2.add(stacks.get(t));
            }
        }
        return new Positioned(new CraftingRecipeInput(o, q, list2), k, m);
    }

    @Override
    public ItemStack getStackInSlot(int slot) {
        return this.stacks.get(slot);
    }

    public ItemStack getStackInSlot(int x, int y) {
        return this.stacks.get(x + y * this.width);
    }

    @Override
    public int getSize() {
        return this.stacks.size();
    }

    @Override
    public boolean isEmpty() {
        return this.stackCount == 0;
    }

    public RecipeMatcher getRecipeMatcher() {
        return this.matcher;
    }

    public List<ItemStack> getStacks() {
        return this.stacks;
    }

    public int getStackCount() {
        return this.stackCount;
    }

    public int getWidth() {
        return this.width;
    }

    public int getHeight() {
        return this.height;
    }

    public boolean equals(Object o) {
        if (o == this) {
            return true;
        }
        if (o instanceof CraftingRecipeInput) {
            CraftingRecipeInput lv = (CraftingRecipeInput)o;
            return this.width == lv.width && this.height == lv.height && this.stackCount == lv.stackCount && ItemStack.stacksEqual(this.stacks, lv.stacks);
        }
        return false;
    }

    public int hashCode() {
        int i = ItemStack.listHashCode(this.stacks);
        i = 31 * i + this.width;
        i = 31 * i + this.height;
        return i;
    }

    public record Positioned(CraftingRecipeInput input, int left, int top) {
        public static final Positioned EMPTY = new Positioned(EMPTY, 0, 0);
    }
}

