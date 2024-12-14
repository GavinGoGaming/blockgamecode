/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.screen.slot;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.Inventory;
import net.minecraft.inventory.RecipeInputInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.recipe.RecipeType;
import net.minecraft.recipe.RecipeUnlocker;
import net.minecraft.recipe.input.CraftingRecipeInput;
import net.minecraft.screen.slot.Slot;
import net.minecraft.util.collection.DefaultedList;

public class CraftingResultSlot
extends Slot {
    private final RecipeInputInventory input;
    private final PlayerEntity player;
    private int amount;

    public CraftingResultSlot(PlayerEntity player, RecipeInputInventory input, Inventory inventory, int index, int x, int y) {
        super(inventory, index, x, y);
        this.player = player;
        this.input = input;
    }

    @Override
    public boolean canInsert(ItemStack stack) {
        return false;
    }

    @Override
    public ItemStack takeStack(int amount) {
        if (this.hasStack()) {
            this.amount += Math.min(amount, this.getStack().getCount());
        }
        return super.takeStack(amount);
    }

    @Override
    protected void onCrafted(ItemStack stack, int amount) {
        this.amount += amount;
        this.onCrafted(stack);
    }

    @Override
    protected void onTake(int amount) {
        this.amount += amount;
    }

    @Override
    protected void onCrafted(ItemStack stack) {
        Inventory inventory;
        if (this.amount > 0) {
            stack.onCraftByPlayer(this.player.getWorld(), this.player, this.amount);
        }
        if ((inventory = this.inventory) instanceof RecipeUnlocker) {
            RecipeUnlocker lv = (RecipeUnlocker)((Object)inventory);
            lv.unlockLastRecipe(this.player, this.input.getHeldStacks());
        }
        this.amount = 0;
    }

    @Override
    public void onTakeItem(PlayerEntity player, ItemStack stack) {
        this.onCrafted(stack);
        CraftingRecipeInput.Positioned lv = this.input.createPositionedRecipeInput();
        CraftingRecipeInput lv2 = lv.input();
        int i = lv.left();
        int j = lv.top();
        DefaultedList<ItemStack> lv3 = player.getWorld().getRecipeManager().getRemainingStacks(RecipeType.CRAFTING, lv2, player.getWorld());
        for (int k = 0; k < lv2.getHeight(); ++k) {
            for (int l = 0; l < lv2.getWidth(); ++l) {
                int m = l + i + (k + j) * this.input.getWidth();
                ItemStack lv4 = this.input.getStack(m);
                ItemStack lv5 = lv3.get(l + k * lv2.getWidth());
                if (!lv4.isEmpty()) {
                    this.input.removeStack(m, 1);
                    lv4 = this.input.getStack(m);
                }
                if (lv5.isEmpty()) continue;
                if (lv4.isEmpty()) {
                    this.input.setStack(m, lv5);
                    continue;
                }
                if (ItemStack.areItemsAndComponentsEqual(lv4, lv5)) {
                    lv5.increment(lv4.getCount());
                    this.input.setStack(m, lv5);
                    continue;
                }
                if (this.player.getInventory().insertStack(lv5)) continue;
                this.player.dropItem(lv5, false);
            }
        }
    }

    @Override
    public boolean disablesDynamicDisplay() {
        return true;
    }
}

