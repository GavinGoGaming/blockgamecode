/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.recipe;

import java.util.Iterator;
import net.minecraft.recipe.RecipeEntry;
import net.minecraft.recipe.ShapedRecipe;
import net.minecraft.util.math.MathHelper;

public interface RecipeGridAligner<T> {
    default public void alignRecipeToGrid(int gridWidth, int gridHeight, int gridOutputSlot, RecipeEntry<?> recipe, Iterator<T> inputs, int amount) {
        int m = gridWidth;
        int n = gridHeight;
        Object lv = recipe.value();
        if (lv instanceof ShapedRecipe) {
            ShapedRecipe lv2 = (ShapedRecipe)lv;
            m = lv2.getWidth();
            n = lv2.getHeight();
        }
        int o = 0;
        block0: for (int p = 0; p < gridHeight; ++p) {
            if (o == gridOutputSlot) {
                ++o;
            }
            boolean bl = (float)n < (float)gridHeight / 2.0f;
            int q = MathHelper.floor((float)gridHeight / 2.0f - (float)n / 2.0f);
            if (bl && q > p) {
                o += gridWidth;
                ++p;
            }
            for (int r = 0; r < gridWidth; ++r) {
                boolean bl2;
                if (!inputs.hasNext()) {
                    return;
                }
                bl = (float)m < (float)gridWidth / 2.0f;
                q = MathHelper.floor((float)gridWidth / 2.0f - (float)m / 2.0f);
                int s = m;
                boolean bl3 = bl2 = r < m;
                if (bl) {
                    s = q + m;
                    boolean bl4 = bl2 = q <= r && r < q + m;
                }
                if (bl2) {
                    this.acceptAlignedInput(inputs.next(), o, amount, r, p);
                } else if (s == r) {
                    o += gridWidth - r;
                    continue block0;
                }
                ++o;
            }
        }
    }

    public void acceptAlignedInput(T var1, int var2, int var3, int var4, int var5);
}

