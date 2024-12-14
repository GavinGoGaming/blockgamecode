/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 * 
 * Could not load the following classes:
 *  net.fabricmc.api.EnvType
 *  net.fabricmc.api.Environment
 *  org.jetbrains.annotations.Nullable
 */
package net.minecraft.client.gui.screen.recipebook;

import java.util.Iterator;
import java.util.List;
import java.util.Set;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.screen.ButtonTextures;
import net.minecraft.client.gui.screen.recipebook.RecipeBookWidget;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.recipe.Ingredient;
import net.minecraft.recipe.RecipeEntry;
import net.minecraft.screen.slot.Slot;
import net.minecraft.util.Identifier;
import net.minecraft.util.collection.DefaultedList;
import org.jetbrains.annotations.Nullable;

@Environment(value=EnvType.CLIENT)
public abstract class AbstractFurnaceRecipeBookScreen
extends RecipeBookWidget {
    private static final ButtonTextures TEXTURES = new ButtonTextures(Identifier.ofVanilla("recipe_book/furnace_filter_enabled"), Identifier.ofVanilla("recipe_book/furnace_filter_disabled"), Identifier.ofVanilla("recipe_book/furnace_filter_enabled_highlighted"), Identifier.ofVanilla("recipe_book/furnace_filter_disabled_highlighted"));
    @Nullable
    private Ingredient fuels;

    @Override
    protected void setBookButtonTexture() {
        this.toggleCraftableButton.setTextures(TEXTURES);
    }

    @Override
    public void slotClicked(@Nullable Slot slot) {
        super.slotClicked(slot);
        if (slot != null && slot.id < this.craftingScreenHandler.getCraftingSlotCount()) {
            this.ghostSlots.reset();
        }
    }

    @Override
    public void showGhostRecipe(RecipeEntry<?> recipe, List<Slot> slots) {
        ItemStack lv = recipe.value().getResult(this.client.world.getRegistryManager());
        this.ghostSlots.setRecipe(recipe);
        this.ghostSlots.addSlot(Ingredient.ofStacks(lv), slots.get((int)2).x, slots.get((int)2).y);
        DefaultedList<Ingredient> lv2 = recipe.value().getIngredients();
        Slot lv3 = slots.get(1);
        if (lv3.getStack().isEmpty()) {
            if (this.fuels == null) {
                this.fuels = Ingredient.ofStacks(this.getAllowedFuels().stream().filter(item -> item.isEnabled(this.client.world.getEnabledFeatures())).map(ItemStack::new));
            }
            this.ghostSlots.addSlot(this.fuels, lv3.x, lv3.y);
        }
        Iterator iterator = lv2.iterator();
        for (int i = 0; i < 2; ++i) {
            if (!iterator.hasNext()) {
                return;
            }
            Ingredient lv4 = (Ingredient)iterator.next();
            if (lv4.isEmpty()) continue;
            Slot lv5 = slots.get(i);
            this.ghostSlots.addSlot(lv4, lv5.x, lv5.y);
        }
    }

    protected abstract Set<Item> getAllowedFuels();
}

