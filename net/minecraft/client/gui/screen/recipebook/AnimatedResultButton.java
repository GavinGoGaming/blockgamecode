/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 * 
 * Could not load the following classes:
 *  net.fabricmc.api.EnvType
 *  net.fabricmc.api.Environment
 */
package net.minecraft.client.gui.screen.recipebook;

import com.google.common.collect.Lists;
import java.util.ArrayList;
import java.util.List;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.narration.NarrationMessageBuilder;
import net.minecraft.client.gui.screen.narration.NarrationPart;
import net.minecraft.client.gui.screen.recipebook.RecipeBookResults;
import net.minecraft.client.gui.screen.recipebook.RecipeResultCollection;
import net.minecraft.client.gui.widget.ClickableWidget;
import net.minecraft.item.ItemStack;
import net.minecraft.recipe.RecipeEntry;
import net.minecraft.recipe.book.RecipeBook;
import net.minecraft.screen.AbstractRecipeScreenHandler;
import net.minecraft.screen.ScreenTexts;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.MathHelper;

@Environment(value=EnvType.CLIENT)
public class AnimatedResultButton
extends ClickableWidget {
    private static final Identifier SLOT_MANY_CRAFTABLE_TEXTURE = Identifier.ofVanilla("recipe_book/slot_many_craftable");
    private static final Identifier SLOT_CRAFTABLE_TEXTURE = Identifier.ofVanilla("recipe_book/slot_craftable");
    private static final Identifier SLOT_MANY_UNCRAFTABLE_TEXTURE = Identifier.ofVanilla("recipe_book/slot_many_uncraftable");
    private static final Identifier SLOT_UNCRAFTABLE_TEXTURE = Identifier.ofVanilla("recipe_book/slot_uncraftable");
    private static final float field_32414 = 15.0f;
    private static final int field_32415 = 25;
    public static final int field_32413 = 30;
    private static final Text MORE_RECIPES_TEXT = Text.translatable("gui.recipebook.moreRecipes");
    private AbstractRecipeScreenHandler<?, ?> craftingScreenHandler;
    private RecipeBook recipeBook;
    private RecipeResultCollection resultCollection;
    private float time;
    private float bounce;
    private int currentResultIndex;

    public AnimatedResultButton() {
        super(0, 0, 25, 25, ScreenTexts.EMPTY);
    }

    public void showResultCollection(RecipeResultCollection resultCollection, RecipeBookResults results) {
        this.resultCollection = resultCollection;
        this.craftingScreenHandler = (AbstractRecipeScreenHandler)results.getClient().player.currentScreenHandler;
        this.recipeBook = results.getRecipeBook();
        List<RecipeEntry<?>> list = resultCollection.getResults(this.recipeBook.isFilteringCraftable(this.craftingScreenHandler));
        for (RecipeEntry<?> lv : list) {
            if (!this.recipeBook.shouldDisplay(lv)) continue;
            results.onRecipesDisplayed(list);
            this.bounce = 15.0f;
            break;
        }
    }

    public RecipeResultCollection getResultCollection() {
        return this.resultCollection;
    }

    @Override
    public void renderWidget(DrawContext context, int mouseX, int mouseY, float delta) {
        boolean bl;
        if (!Screen.hasControlDown()) {
            this.time += delta;
        }
        Identifier lv = this.resultCollection.hasCraftableRecipes() ? (this.resultCollection.getResults(this.recipeBook.isFilteringCraftable(this.craftingScreenHandler)).size() > 1 ? SLOT_MANY_CRAFTABLE_TEXTURE : SLOT_CRAFTABLE_TEXTURE) : (this.resultCollection.getResults(this.recipeBook.isFilteringCraftable(this.craftingScreenHandler)).size() > 1 ? SLOT_MANY_UNCRAFTABLE_TEXTURE : SLOT_UNCRAFTABLE_TEXTURE);
        boolean bl2 = bl = this.bounce > 0.0f;
        if (bl) {
            float g = 1.0f + 0.1f * (float)Math.sin(this.bounce / 15.0f * (float)Math.PI);
            context.getMatrices().push();
            context.getMatrices().translate(this.getX() + 8, this.getY() + 12, 0.0f);
            context.getMatrices().scale(g, g, 1.0f);
            context.getMatrices().translate(-(this.getX() + 8), -(this.getY() + 12), 0.0f);
            this.bounce -= delta;
        }
        context.drawGuiTexture(lv, this.getX(), this.getY(), this.width, this.height);
        List<RecipeEntry<?>> list = this.getResults();
        this.currentResultIndex = MathHelper.floor(this.time / 30.0f) % list.size();
        ItemStack lv2 = list.get(this.currentResultIndex).value().getResult(this.resultCollection.getRegistryManager());
        int k = 4;
        if (this.resultCollection.hasSingleOutput() && this.getResults().size() > 1) {
            context.drawItem(lv2, this.getX() + k + 1, this.getY() + k + 1, 0, 10);
            --k;
        }
        context.drawItemWithoutEntity(lv2, this.getX() + k, this.getY() + k);
        if (bl) {
            context.getMatrices().pop();
        }
    }

    private List<RecipeEntry<?>> getResults() {
        List<RecipeEntry<?>> list = this.resultCollection.getRecipes(true);
        if (!this.recipeBook.isFilteringCraftable(this.craftingScreenHandler)) {
            list.addAll(this.resultCollection.getRecipes(false));
        }
        return list;
    }

    public boolean hasResults() {
        return this.getResults().size() == 1;
    }

    public RecipeEntry<?> currentRecipe() {
        List<RecipeEntry<?>> list = this.getResults();
        return list.get(this.currentResultIndex);
    }

    public List<Text> getTooltip() {
        ItemStack lv = this.getResults().get(this.currentResultIndex).value().getResult(this.resultCollection.getRegistryManager());
        ArrayList<Text> list = Lists.newArrayList(Screen.getTooltipFromItem(MinecraftClient.getInstance(), lv));
        if (this.resultCollection.getResults(this.recipeBook.isFilteringCraftable(this.craftingScreenHandler)).size() > 1) {
            list.add(MORE_RECIPES_TEXT);
        }
        return list;
    }

    @Override
    public void appendClickableNarrations(NarrationMessageBuilder builder) {
        ItemStack lv = this.getResults().get(this.currentResultIndex).value().getResult(this.resultCollection.getRegistryManager());
        builder.put(NarrationPart.TITLE, (Text)Text.translatable("narration.recipe", lv.getName()));
        if (this.resultCollection.getResults(this.recipeBook.isFilteringCraftable(this.craftingScreenHandler)).size() > 1) {
            builder.put(NarrationPart.USAGE, Text.translatable("narration.button.usage.hovered"), Text.translatable("narration.recipe.usage.more"));
        } else {
            builder.put(NarrationPart.USAGE, (Text)Text.translatable("narration.button.usage.hovered"));
        }
    }

    @Override
    public int getWidth() {
        return 25;
    }

    @Override
    protected boolean isValidClickButton(int button) {
        return button == 0 || button == 1;
    }
}

