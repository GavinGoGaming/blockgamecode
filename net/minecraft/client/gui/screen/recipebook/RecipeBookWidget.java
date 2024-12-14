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
import it.unimi.dsi.fastutil.objects.ObjectLinkedOpenHashSet;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.Drawable;
import net.minecraft.client.gui.Element;
import net.minecraft.client.gui.Selectable;
import net.minecraft.client.gui.screen.ButtonTextures;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.narration.NarrationMessageBuilder;
import net.minecraft.client.gui.screen.recipebook.RecipeBookGhostSlots;
import net.minecraft.client.gui.screen.recipebook.RecipeBookResults;
import net.minecraft.client.gui.screen.recipebook.RecipeDisplayListener;
import net.minecraft.client.gui.screen.recipebook.RecipeGroupButtonWidget;
import net.minecraft.client.gui.screen.recipebook.RecipeResultCollection;
import net.minecraft.client.gui.tooltip.Tooltip;
import net.minecraft.client.gui.widget.ClickableWidget;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.client.gui.widget.ToggleButtonWidget;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.client.recipebook.ClientRecipeBook;
import net.minecraft.client.recipebook.RecipeBookGroup;
import net.minecraft.client.resource.language.LanguageDefinition;
import net.minecraft.client.resource.language.LanguageManager;
import net.minecraft.item.ItemStack;
import net.minecraft.network.packet.c2s.play.RecipeCategoryOptionsC2SPacket;
import net.minecraft.recipe.Ingredient;
import net.minecraft.recipe.RecipeEntry;
import net.minecraft.recipe.RecipeGridAligner;
import net.minecraft.recipe.RecipeMatcher;
import net.minecraft.recipe.book.RecipeBookCategory;
import net.minecraft.screen.AbstractRecipeScreenHandler;
import net.minecraft.screen.slot.Slot;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.Nullable;
import org.lwjgl.glfw.GLFW;

@Environment(value=EnvType.CLIENT)
public class RecipeBookWidget
implements RecipeGridAligner<Ingredient>,
Drawable,
Element,
Selectable,
RecipeDisplayListener {
    public static final ButtonTextures BUTTON_TEXTURES = new ButtonTextures(Identifier.ofVanilla("recipe_book/button"), Identifier.ofVanilla("recipe_book/button_highlighted"));
    private static final ButtonTextures FILTER_BUTTON_TEXTURES = new ButtonTextures(Identifier.ofVanilla("recipe_book/filter_enabled"), Identifier.ofVanilla("recipe_book/filter_disabled"), Identifier.ofVanilla("recipe_book/filter_enabled_highlighted"), Identifier.ofVanilla("recipe_book/filter_disabled_highlighted"));
    protected static final Identifier TEXTURE = Identifier.ofVanilla("textures/gui/recipe_book.png");
    private static final Text SEARCH_HINT_TEXT = Text.translatable("gui.recipebook.search_hint").formatted(Formatting.ITALIC).formatted(Formatting.GRAY);
    public static final int field_32408 = 147;
    public static final int field_32409 = 166;
    private static final int field_32410 = 86;
    private static final Text TOGGLE_CRAFTABLE_RECIPES_TEXT = Text.translatable("gui.recipebook.toggleRecipes.craftable");
    private static final Text TOGGLE_ALL_RECIPES_TEXT = Text.translatable("gui.recipebook.toggleRecipes.all");
    private int leftOffset;
    private int parentWidth;
    private int parentHeight;
    protected final RecipeBookGhostSlots ghostSlots = new RecipeBookGhostSlots();
    private final List<RecipeGroupButtonWidget> tabButtons = Lists.newArrayList();
    @Nullable
    private RecipeGroupButtonWidget currentTab;
    protected ToggleButtonWidget toggleCraftableButton;
    protected AbstractRecipeScreenHandler<?, ?> craftingScreenHandler;
    protected MinecraftClient client;
    @Nullable
    private TextFieldWidget searchField;
    private String searchText = "";
    private ClientRecipeBook recipeBook;
    private final RecipeBookResults recipesArea = new RecipeBookResults();
    private final RecipeMatcher recipeFinder = new RecipeMatcher();
    private int cachedInvChangeCount;
    private boolean searching;
    private boolean open;
    private boolean narrow;

    public void initialize(int parentWidth, int parentHeight, MinecraftClient client, boolean narrow, AbstractRecipeScreenHandler<?, ?> craftingScreenHandler) {
        this.client = client;
        this.parentWidth = parentWidth;
        this.parentHeight = parentHeight;
        this.craftingScreenHandler = craftingScreenHandler;
        this.narrow = narrow;
        client.player.currentScreenHandler = craftingScreenHandler;
        this.recipeBook = client.player.getRecipeBook();
        this.cachedInvChangeCount = client.player.getInventory().getChangeCount();
        this.open = this.isGuiOpen();
        if (this.open) {
            this.reset();
        }
    }

    public void reset() {
        this.leftOffset = this.narrow ? 0 : 86;
        int i = (this.parentWidth - 147) / 2 - this.leftOffset;
        int j = (this.parentHeight - 166) / 2;
        this.recipeFinder.clear();
        this.client.player.getInventory().populateRecipeFinder(this.recipeFinder);
        this.craftingScreenHandler.populateRecipeFinder(this.recipeFinder);
        String string = this.searchField != null ? this.searchField.getText() : "";
        this.searchField = new TextFieldWidget(this.client.textRenderer, i + 25, j + 13, 81, this.client.textRenderer.fontHeight + 5, Text.translatable("itemGroup.search"));
        this.searchField.setMaxLength(50);
        this.searchField.setVisible(true);
        this.searchField.setEditableColor(0xFFFFFF);
        this.searchField.setText(string);
        this.searchField.setPlaceholder(SEARCH_HINT_TEXT);
        this.recipesArea.initialize(this.client, i, j);
        this.recipesArea.setGui(this);
        this.toggleCraftableButton = new ToggleButtonWidget(i + 110, j + 12, 26, 16, this.recipeBook.isFilteringCraftable(this.craftingScreenHandler));
        this.updateTooltip();
        this.setBookButtonTexture();
        this.tabButtons.clear();
        for (RecipeBookGroup lv : RecipeBookGroup.getGroups(this.craftingScreenHandler.getCategory())) {
            this.tabButtons.add(new RecipeGroupButtonWidget(lv));
        }
        if (this.currentTab != null) {
            this.currentTab = this.tabButtons.stream().filter(button -> button.getCategory().equals((Object)this.currentTab.getCategory())).findFirst().orElse(null);
        }
        if (this.currentTab == null) {
            this.currentTab = this.tabButtons.get(0);
        }
        this.currentTab.setToggled(true);
        this.refreshResults(false);
        this.refreshTabButtons();
    }

    private void updateTooltip() {
        this.toggleCraftableButton.setTooltip(this.toggleCraftableButton.isToggled() ? Tooltip.of(this.getToggleCraftableButtonText()) : Tooltip.of(TOGGLE_ALL_RECIPES_TEXT));
    }

    protected void setBookButtonTexture() {
        this.toggleCraftableButton.setTextures(FILTER_BUTTON_TEXTURES);
    }

    public int findLeftEdge(int width, int backgroundWidth) {
        int k = this.isOpen() && !this.narrow ? 177 + (width - backgroundWidth - 200) / 2 : (width - backgroundWidth) / 2;
        return k;
    }

    public void toggleOpen() {
        this.setOpen(!this.isOpen());
    }

    public boolean isOpen() {
        return this.open;
    }

    private boolean isGuiOpen() {
        return this.recipeBook.isGuiOpen(this.craftingScreenHandler.getCategory());
    }

    protected void setOpen(boolean opened) {
        if (opened) {
            this.reset();
        }
        this.open = opened;
        this.recipeBook.setGuiOpen(this.craftingScreenHandler.getCategory(), opened);
        if (!opened) {
            this.recipesArea.hideAlternates();
        }
        this.sendBookDataPacket();
    }

    public void slotClicked(@Nullable Slot slot) {
        if (slot != null && slot.id < this.craftingScreenHandler.getCraftingSlotCount()) {
            this.ghostSlots.reset();
            if (this.isOpen()) {
                this.refreshInputs();
            }
        }
    }

    private void refreshResults(boolean resetCurrentPage) {
        ClientPlayNetworkHandler lv;
        List<RecipeResultCollection> list = this.recipeBook.getResultsForGroup(this.currentTab.getCategory());
        list.forEach(resultCollection -> resultCollection.computeCraftables(this.recipeFinder, this.craftingScreenHandler.getCraftingWidth(), this.craftingScreenHandler.getCraftingHeight(), this.recipeBook));
        ArrayList<RecipeResultCollection> list2 = Lists.newArrayList(list);
        list2.removeIf(resultCollection -> !resultCollection.isInitialized());
        list2.removeIf(resultCollection -> !resultCollection.hasFittingRecipes());
        String string = this.searchField.getText();
        if (!string.isEmpty() && (lv = this.client.getNetworkHandler()) != null) {
            ObjectLinkedOpenHashSet<RecipeResultCollection> objectSet = new ObjectLinkedOpenHashSet<RecipeResultCollection>(lv.getSearchManager().getRecipeOutputReloadFuture().findAll(string.toLowerCase(Locale.ROOT)));
            list2.removeIf(resultCollection -> !objectSet.contains(resultCollection));
        }
        if (this.recipeBook.isFilteringCraftable(this.craftingScreenHandler)) {
            list2.removeIf(resultCollection -> !resultCollection.hasCraftableRecipes());
        }
        this.recipesArea.setResults(list2, resetCurrentPage);
    }

    private void refreshTabButtons() {
        int i = (this.parentWidth - 147) / 2 - this.leftOffset - 30;
        int j = (this.parentHeight - 166) / 2 + 3;
        int k = 27;
        int l = 0;
        for (RecipeGroupButtonWidget lv : this.tabButtons) {
            RecipeBookGroup lv2 = lv.getCategory();
            if (lv2 == RecipeBookGroup.CRAFTING_SEARCH || lv2 == RecipeBookGroup.FURNACE_SEARCH) {
                lv.visible = true;
                lv.setPosition(i, j + 27 * l++);
                continue;
            }
            if (!lv.hasKnownRecipes(this.recipeBook)) continue;
            lv.setPosition(i, j + 27 * l++);
            lv.checkForNewRecipes(this.client);
        }
    }

    public void update() {
        boolean bl = this.isGuiOpen();
        if (this.isOpen() != bl) {
            this.setOpen(bl);
        }
        if (!this.isOpen()) {
            return;
        }
        if (this.cachedInvChangeCount != this.client.player.getInventory().getChangeCount()) {
            this.refreshInputs();
            this.cachedInvChangeCount = this.client.player.getInventory().getChangeCount();
        }
    }

    private void refreshInputs() {
        this.recipeFinder.clear();
        this.client.player.getInventory().populateRecipeFinder(this.recipeFinder);
        this.craftingScreenHandler.populateRecipeFinder(this.recipeFinder);
        this.refreshResults(false);
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        if (!this.isOpen()) {
            return;
        }
        context.getMatrices().push();
        context.getMatrices().translate(0.0f, 0.0f, 100.0f);
        int k = (this.parentWidth - 147) / 2 - this.leftOffset;
        int l = (this.parentHeight - 166) / 2;
        context.drawTexture(TEXTURE, k, l, 1, 1, 147, 166);
        this.searchField.render(context, mouseX, mouseY, delta);
        for (RecipeGroupButtonWidget lv : this.tabButtons) {
            lv.render(context, mouseX, mouseY, delta);
        }
        this.toggleCraftableButton.render(context, mouseX, mouseY, delta);
        this.recipesArea.draw(context, k, l, mouseX, mouseY, delta);
        context.getMatrices().pop();
    }

    public void drawTooltip(DrawContext context, int x, int y, int mouseX, int mouseY) {
        if (!this.isOpen()) {
            return;
        }
        this.recipesArea.drawTooltip(context, mouseX, mouseY);
        this.drawGhostSlotTooltip(context, x, y, mouseX, mouseY);
    }

    protected Text getToggleCraftableButtonText() {
        return TOGGLE_CRAFTABLE_RECIPES_TEXT;
    }

    private void drawGhostSlotTooltip(DrawContext context, int x, int y, int mouseX, int mouseY) {
        ItemStack lv = null;
        for (int m = 0; m < this.ghostSlots.getSlotCount(); ++m) {
            RecipeBookGhostSlots.GhostInputSlot lv2 = this.ghostSlots.getSlot(m);
            int n = lv2.getX() + x;
            int o = lv2.getY() + y;
            if (mouseX < n || mouseY < o || mouseX >= n + 16 || mouseY >= o + 16) continue;
            lv = lv2.getCurrentItemStack();
        }
        if (lv != null && this.client.currentScreen != null) {
            context.drawTooltip(this.client.textRenderer, Screen.getTooltipFromItem(this.client, lv), mouseX, mouseY);
        }
    }

    public void drawGhostSlots(DrawContext context, int x, int y, boolean notInventory, float delta) {
        this.ghostSlots.draw(context, this.client, x, y, notInventory, delta);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (!this.isOpen() || this.client.player.isSpectator()) {
            return false;
        }
        if (this.recipesArea.mouseClicked(mouseX, mouseY, button, (this.parentWidth - 147) / 2 - this.leftOffset, (this.parentHeight - 166) / 2, 147, 166)) {
            RecipeEntry<?> lv = this.recipesArea.getLastClickedRecipe();
            RecipeResultCollection lv2 = this.recipesArea.getLastClickedResults();
            if (lv != null && lv2 != null) {
                if (!lv2.isCraftable(lv) && this.ghostSlots.getRecipe() == lv) {
                    return false;
                }
                this.ghostSlots.reset();
                this.client.interactionManager.clickRecipe(this.client.player.currentScreenHandler.syncId, lv, Screen.hasShiftDown());
                if (!this.isWide()) {
                    this.setOpen(false);
                }
            }
            return true;
        }
        if (this.searchField.mouseClicked(mouseX, mouseY, button)) {
            this.searchField.setFocused(true);
            return true;
        }
        this.searchField.setFocused(false);
        if (this.toggleCraftableButton.mouseClicked(mouseX, mouseY, button)) {
            boolean bl = this.toggleFilteringCraftable();
            this.toggleCraftableButton.setToggled(bl);
            this.updateTooltip();
            this.sendBookDataPacket();
            this.refreshResults(false);
            return true;
        }
        for (RecipeGroupButtonWidget lv3 : this.tabButtons) {
            if (!lv3.mouseClicked(mouseX, mouseY, button)) continue;
            if (this.currentTab != lv3) {
                if (this.currentTab != null) {
                    this.currentTab.setToggled(false);
                }
                this.currentTab = lv3;
                this.currentTab.setToggled(true);
                this.refreshResults(true);
            }
            return true;
        }
        return false;
    }

    private boolean toggleFilteringCraftable() {
        RecipeBookCategory lv = this.craftingScreenHandler.getCategory();
        boolean bl = !this.recipeBook.isFilteringCraftable(lv);
        this.recipeBook.setFilteringCraftable(lv, bl);
        return bl;
    }

    public boolean isClickOutsideBounds(double mouseX, double mouseY, int x, int y, int backgroundWidth, int backgroundHeight, int button) {
        if (!this.isOpen()) {
            return true;
        }
        boolean bl = mouseX < (double)x || mouseY < (double)y || mouseX >= (double)(x + backgroundWidth) || mouseY >= (double)(y + backgroundHeight);
        boolean bl2 = (double)(x - 147) < mouseX && mouseX < (double)x && (double)y < mouseY && mouseY < (double)(y + backgroundHeight);
        return bl && !bl2 && !this.currentTab.isSelected();
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        this.searching = false;
        if (!this.isOpen() || this.client.player.isSpectator()) {
            return false;
        }
        if (keyCode == GLFW.GLFW_KEY_ESCAPE && !this.isWide()) {
            this.setOpen(false);
            return true;
        }
        if (this.searchField.keyPressed(keyCode, scanCode, modifiers)) {
            this.refreshSearchResults();
            return true;
        }
        if (this.searchField.isFocused() && this.searchField.isVisible() && keyCode != GLFW.GLFW_KEY_ESCAPE) {
            return true;
        }
        if (this.client.options.chatKey.matchesKey(keyCode, scanCode) && !this.searchField.isFocused()) {
            this.searching = true;
            this.searchField.setFocused(true);
            return true;
        }
        return false;
    }

    @Override
    public boolean keyReleased(int keyCode, int scanCode, int modifiers) {
        this.searching = false;
        return Element.super.keyReleased(keyCode, scanCode, modifiers);
    }

    @Override
    public boolean charTyped(char chr, int modifiers) {
        if (this.searching) {
            return false;
        }
        if (!this.isOpen() || this.client.player.isSpectator()) {
            return false;
        }
        if (this.searchField.charTyped(chr, modifiers)) {
            this.refreshSearchResults();
            return true;
        }
        return Element.super.charTyped(chr, modifiers);
    }

    @Override
    public boolean isMouseOver(double mouseX, double mouseY) {
        return false;
    }

    @Override
    public void setFocused(boolean focused) {
    }

    @Override
    public boolean isFocused() {
        return false;
    }

    private void refreshSearchResults() {
        String string = this.searchField.getText().toLowerCase(Locale.ROOT);
        this.triggerPirateSpeakEasterEgg(string);
        if (!string.equals(this.searchText)) {
            this.refreshResults(false);
            this.searchText = string;
        }
    }

    private void triggerPirateSpeakEasterEgg(String search) {
        if ("excitedze".equals(search)) {
            LanguageManager lv = this.client.getLanguageManager();
            String string2 = "en_pt";
            LanguageDefinition lv2 = lv.getLanguage("en_pt");
            if (lv2 == null || lv.getLanguage().equals("en_pt")) {
                return;
            }
            lv.setLanguage("en_pt");
            this.client.options.language = "en_pt";
            this.client.reloadResources();
            this.client.options.write();
        }
    }

    private boolean isWide() {
        return this.leftOffset == 86;
    }

    public void refresh() {
        this.refreshTabButtons();
        if (this.isOpen()) {
            this.refreshResults(false);
        }
    }

    @Override
    public void onRecipesDisplayed(List<RecipeEntry<?>> recipes) {
        for (RecipeEntry<?> lv : recipes) {
            this.client.player.onRecipeDisplayed(lv);
        }
    }

    public void showGhostRecipe(RecipeEntry<?> recipe, List<Slot> slots) {
        ItemStack lv = recipe.value().getResult(this.client.world.getRegistryManager());
        this.ghostSlots.setRecipe(recipe);
        this.ghostSlots.addSlot(Ingredient.ofStacks(lv), slots.get((int)0).x, slots.get((int)0).y);
        this.alignRecipeToGrid(this.craftingScreenHandler.getCraftingWidth(), this.craftingScreenHandler.getCraftingHeight(), this.craftingScreenHandler.getCraftingResultSlotIndex(), recipe, recipe.value().getIngredients().iterator(), 0);
    }

    @Override
    public void acceptAlignedInput(Ingredient arg, int i, int j, int k, int l) {
        if (!arg.isEmpty()) {
            Slot lv = (Slot)this.craftingScreenHandler.slots.get(i);
            this.ghostSlots.addSlot(arg, lv.x, lv.y);
        }
    }

    protected void sendBookDataPacket() {
        if (this.client.getNetworkHandler() != null) {
            RecipeBookCategory lv = this.craftingScreenHandler.getCategory();
            boolean bl = this.recipeBook.getOptions().isGuiOpen(lv);
            boolean bl2 = this.recipeBook.getOptions().isFilteringCraftable(lv);
            this.client.getNetworkHandler().sendPacket(new RecipeCategoryOptionsC2SPacket(lv, bl, bl2));
        }
    }

    @Override
    public Selectable.SelectionType getType() {
        return this.open ? Selectable.SelectionType.HOVERED : Selectable.SelectionType.NONE;
    }

    @Override
    public void appendNarrations(NarrationMessageBuilder builder) {
        ArrayList<ClickableWidget> list = Lists.newArrayList();
        this.recipesArea.forEachButton(button -> {
            if (button.isNarratable()) {
                list.add((ClickableWidget)button);
            }
        });
        list.add(this.searchField);
        list.add(this.toggleCraftableButton);
        list.addAll(this.tabButtons);
        Screen.SelectedElementNarrationData lv = Screen.findSelectedElementData(list, null);
        if (lv != null) {
            lv.selectable.appendNarrations(builder.nextMessage());
        }
    }
}

