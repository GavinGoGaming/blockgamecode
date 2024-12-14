/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 * 
 * Could not load the following classes:
 *  net.fabricmc.api.EnvType
 *  net.fabricmc.api.Environment
 *  org.jetbrains.annotations.Nullable
 */
package net.minecraft.client.gui.screen.recipebook;

import com.google.common.collect.Lists;
import com.mojang.blaze3d.systems.RenderSystem;
import java.util.Collections;
import java.util.List;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.Drawable;
import net.minecraft.client.gui.Element;
import net.minecraft.client.gui.screen.narration.NarrationMessageBuilder;
import net.minecraft.client.gui.screen.recipebook.RecipeResultCollection;
import net.minecraft.client.gui.widget.ClickableWidget;
import net.minecraft.item.ItemStack;
import net.minecraft.recipe.Ingredient;
import net.minecraft.recipe.RecipeEntry;
import net.minecraft.recipe.RecipeGridAligner;
import net.minecraft.screen.AbstractFurnaceScreenHandler;
import net.minecraft.screen.AbstractRecipeScreenHandler;
import net.minecraft.screen.ScreenTexts;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.MathHelper;
import org.jetbrains.annotations.Nullable;

@Environment(value=EnvType.CLIENT)
public class RecipeAlternativesWidget
implements Drawable,
Element {
    private static final Identifier OVERLAY_RECIPE_TEXTURE = Identifier.ofVanilla("recipe_book/overlay_recipe");
    static final Identifier FURNACE_OVERLAY_HIGHLIGHTED_TEXTURE = Identifier.ofVanilla("recipe_book/furnace_overlay_highlighted");
    static final Identifier FURNACE_OVERLAY_TEXTURE = Identifier.ofVanilla("recipe_book/furnace_overlay");
    static final Identifier CRAFTING_OVERLAY_HIGHLIGHTED_TEXTURE = Identifier.ofVanilla("recipe_book/crafting_overlay_highlighted");
    static final Identifier CRAFTING_OVERLAY_TEXTURE = Identifier.ofVanilla("recipe_book/crafting_overlay");
    static final Identifier FURNACE_OVERLAY_DISABLED_HIGHLIGHTED_TEXTURE = Identifier.ofVanilla("recipe_book/furnace_overlay_disabled_highlighted");
    static final Identifier FURNACE_OVERLAY_DISABLED_TEXTURE = Identifier.ofVanilla("recipe_book/furnace_overlay_disabled");
    static final Identifier CRAFTING_OVERLAY_DISABLED_HIGHLIGHTED_TEXTURE = Identifier.ofVanilla("recipe_book/crafting_overlay_disabled_highlighted");
    static final Identifier CRAFTING_OVERLAY_DISABLED_TEXTURE = Identifier.ofVanilla("recipe_book/crafting_overlay_disabled");
    private static final int field_32406 = 4;
    private static final int field_32407 = 5;
    private static final float field_33739 = 0.375f;
    public static final int field_42162 = 25;
    private final List<AlternativeButtonWidget> alternativeButtons = Lists.newArrayList();
    private boolean visible;
    private int buttonX;
    private int buttonY;
    private MinecraftClient client;
    private RecipeResultCollection resultCollection;
    @Nullable
    private RecipeEntry<?> lastClickedRecipe;
    float time;
    boolean furnace;

    public void showAlternativesForResult(MinecraftClient client, RecipeResultCollection results, int buttonX, int buttonY, int areaCenterX, int areaCenterY, float delta) {
        float t;
        float s;
        float r;
        float q;
        float h;
        this.client = client;
        this.resultCollection = results;
        if (client.player.currentScreenHandler instanceof AbstractFurnaceScreenHandler) {
            this.furnace = true;
        }
        boolean bl = client.player.getRecipeBook().isFilteringCraftable((AbstractRecipeScreenHandler)client.player.currentScreenHandler);
        List<RecipeEntry<?>> list = results.getRecipes(true);
        List list2 = bl ? Collections.emptyList() : results.getRecipes(false);
        int m = list.size();
        int n = m + list2.size();
        int o = n <= 16 ? 4 : 5;
        int p = (int)Math.ceil((float)n / (float)o);
        this.buttonX = buttonX;
        this.buttonY = buttonY;
        float g = this.buttonX + Math.min(n, o) * 25;
        if (g > (h = (float)(areaCenterX + 50))) {
            this.buttonX = (int)((float)this.buttonX - delta * (float)((int)((g - h) / delta)));
        }
        if ((q = (float)(this.buttonY + p * 25)) > (r = (float)(areaCenterY + 50))) {
            this.buttonY = (int)((float)this.buttonY - delta * (float)MathHelper.ceil((q - r) / delta));
        }
        if ((s = (float)this.buttonY) < (t = (float)(areaCenterY - 100))) {
            this.buttonY = (int)((float)this.buttonY - delta * (float)MathHelper.ceil((s - t) / delta));
        }
        this.visible = true;
        this.alternativeButtons.clear();
        for (int u = 0; u < n; ++u) {
            boolean bl2 = u < m;
            RecipeEntry lv = bl2 ? list.get(u) : (RecipeEntry)list2.get(u - m);
            int v = this.buttonX + 4 + 25 * (u % o);
            int w = this.buttonY + 5 + 25 * (u / o);
            if (this.furnace) {
                this.alternativeButtons.add(new FurnaceAlternativeButtonWidget(this, v, w, lv, bl2));
                continue;
            }
            this.alternativeButtons.add(new AlternativeButtonWidget(v, w, lv, bl2));
        }
        this.lastClickedRecipe = null;
    }

    public RecipeResultCollection getResults() {
        return this.resultCollection;
    }

    @Nullable
    public RecipeEntry<?> getLastClickedRecipe() {
        return this.lastClickedRecipe;
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (button != 0) {
            return false;
        }
        for (AlternativeButtonWidget lv : this.alternativeButtons) {
            if (!lv.mouseClicked(mouseX, mouseY, button)) continue;
            this.lastClickedRecipe = lv.recipe;
            return true;
        }
        return false;
    }

    @Override
    public boolean isMouseOver(double mouseX, double mouseY) {
        return false;
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        if (!this.visible) {
            return;
        }
        this.time += delta;
        RenderSystem.enableBlend();
        context.getMatrices().push();
        context.getMatrices().translate(0.0f, 0.0f, 1000.0f);
        int k = this.alternativeButtons.size() <= 16 ? 4 : 5;
        int l = Math.min(this.alternativeButtons.size(), k);
        int m = MathHelper.ceil((float)this.alternativeButtons.size() / (float)k);
        int n = 4;
        context.drawGuiTexture(OVERLAY_RECIPE_TEXTURE, this.buttonX, this.buttonY, l * 25 + 8, m * 25 + 8);
        RenderSystem.disableBlend();
        for (AlternativeButtonWidget lv : this.alternativeButtons) {
            lv.render(context, mouseX, mouseY, delta);
        }
        context.getMatrices().pop();
    }

    public void setVisible(boolean visible) {
        this.visible = visible;
    }

    public boolean isVisible() {
        return this.visible;
    }

    @Override
    public void setFocused(boolean focused) {
    }

    @Override
    public boolean isFocused() {
        return false;
    }

    @Environment(value=EnvType.CLIENT)
    class FurnaceAlternativeButtonWidget
    extends AlternativeButtonWidget {
        public FurnaceAlternativeButtonWidget(RecipeAlternativesWidget arg, int i, int j, RecipeEntry<?> arg2, boolean bl) {
            super(i, j, arg2, bl);
        }

        @Override
        protected void alignRecipe(RecipeEntry<?> recipe) {
            Ingredient lv = recipe.value().getIngredients().get(0);
            ItemStack[] lvs = lv.getMatchingStacks();
            this.slots.add(new AlternativeButtonWidget.InputSlot(this, 10, 10, lvs));
        }
    }

    @Environment(value=EnvType.CLIENT)
    class AlternativeButtonWidget
    extends ClickableWidget
    implements RecipeGridAligner<Ingredient> {
        final RecipeEntry<?> recipe;
        private final boolean craftable;
        protected final List<InputSlot> slots;

        public AlternativeButtonWidget(int x, int y, RecipeEntry<?> recipe, boolean craftable) {
            super(x, y, 200, 20, ScreenTexts.EMPTY);
            this.slots = Lists.newArrayList();
            this.width = 24;
            this.height = 24;
            this.recipe = recipe;
            this.craftable = craftable;
            this.alignRecipe(recipe);
        }

        protected void alignRecipe(RecipeEntry<?> recipe) {
            this.alignRecipeToGrid(3, 3, -1, recipe, recipe.value().getIngredients().iterator(), 0);
        }

        @Override
        public void appendClickableNarrations(NarrationMessageBuilder builder) {
            this.appendDefaultNarrations(builder);
        }

        @Override
        public void acceptAlignedInput(Ingredient arg, int i, int j, int k, int l) {
            ItemStack[] lvs = arg.getMatchingStacks();
            if (lvs.length != 0) {
                this.slots.add(new InputSlot(this, 3 + k * 7, 3 + l * 7, lvs));
            }
        }

        @Override
        public void renderWidget(DrawContext context, int mouseX, int mouseY, float delta) {
            Identifier lv = this.craftable ? (RecipeAlternativesWidget.this.furnace ? (this.isSelected() ? FURNACE_OVERLAY_HIGHLIGHTED_TEXTURE : FURNACE_OVERLAY_TEXTURE) : (this.isSelected() ? CRAFTING_OVERLAY_HIGHLIGHTED_TEXTURE : CRAFTING_OVERLAY_TEXTURE)) : (RecipeAlternativesWidget.this.furnace ? (this.isSelected() ? FURNACE_OVERLAY_DISABLED_HIGHLIGHTED_TEXTURE : FURNACE_OVERLAY_DISABLED_TEXTURE) : (this.isSelected() ? CRAFTING_OVERLAY_DISABLED_HIGHLIGHTED_TEXTURE : CRAFTING_OVERLAY_DISABLED_TEXTURE));
            context.drawGuiTexture(lv, this.getX(), this.getY(), this.width, this.height);
            context.getMatrices().push();
            context.getMatrices().translate((double)(this.getX() + 2), (double)(this.getY() + 2), 150.0);
            for (InputSlot lv2 : this.slots) {
                context.getMatrices().push();
                context.getMatrices().translate((double)lv2.y, (double)lv2.x, 0.0);
                context.getMatrices().scale(0.375f, 0.375f, 1.0f);
                context.getMatrices().translate(-8.0, -8.0, 0.0);
                if (lv2.stacks.length > 0) {
                    context.drawItem(lv2.stacks[MathHelper.floor(RecipeAlternativesWidget.this.time / 30.0f) % lv2.stacks.length], 0, 0);
                }
                context.getMatrices().pop();
            }
            context.getMatrices().pop();
        }

        @Environment(value=EnvType.CLIENT)
        protected class InputSlot {
            public final ItemStack[] stacks;
            public final int y;
            public final int x;

            public InputSlot(AlternativeButtonWidget widget, int y, int x, ItemStack[] stacks) {
                this.y = y;
                this.x = x;
                this.stacks = stacks;
            }
        }
    }
}

