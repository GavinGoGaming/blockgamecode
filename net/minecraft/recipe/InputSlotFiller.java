/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 * 
 * Could not load the following classes:
 *  org.jetbrains.annotations.Nullable
 */
package net.minecraft.recipe;

import com.google.common.collect.Lists;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntListIterator;
import java.util.ArrayList;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.network.packet.s2c.play.CraftFailedResponseS2CPacket;
import net.minecraft.recipe.Recipe;
import net.minecraft.recipe.RecipeEntry;
import net.minecraft.recipe.RecipeGridAligner;
import net.minecraft.recipe.RecipeMatcher;
import net.minecraft.recipe.input.RecipeInput;
import net.minecraft.screen.AbstractRecipeScreenHandler;
import net.minecraft.screen.slot.Slot;
import net.minecraft.server.network.ServerPlayerEntity;
import org.jetbrains.annotations.Nullable;

public class InputSlotFiller<I extends RecipeInput, R extends Recipe<I>>
implements RecipeGridAligner<Integer> {
    private static final int field_51523 = -1;
    protected final RecipeMatcher matcher = new RecipeMatcher();
    protected PlayerInventory inventory;
    protected AbstractRecipeScreenHandler<I, R> handler;

    public InputSlotFiller(AbstractRecipeScreenHandler<I, R> handler) {
        this.handler = handler;
    }

    public void fillInputSlots(ServerPlayerEntity entity, @Nullable RecipeEntry<R> recipe, boolean craftAll) {
        if (recipe == null || !entity.getRecipeBook().contains(recipe)) {
            return;
        }
        this.inventory = entity.getInventory();
        if (!this.canReturnInputs() && !entity.isCreative()) {
            return;
        }
        this.matcher.clear();
        entity.getInventory().populateRecipeFinder(this.matcher);
        this.handler.populateRecipeFinder(this.matcher);
        if (this.matcher.match((Recipe<?>)recipe.value(), null)) {
            this.fillInputSlots(recipe, craftAll);
        } else {
            this.returnInputs();
            entity.networkHandler.sendPacket(new CraftFailedResponseS2CPacket(entity.currentScreenHandler.syncId, recipe));
        }
        entity.getInventory().markDirty();
    }

    protected void returnInputs() {
        for (int i = 0; i < this.handler.getCraftingSlotCount(); ++i) {
            if (!this.handler.canInsertIntoSlot(i)) continue;
            ItemStack lv = this.handler.getSlot(i).getStack().copy();
            this.inventory.offer(lv, false);
            this.handler.getSlot(i).setStackNoCallbacks(lv);
        }
        this.handler.clearCraftingSlots();
    }

    protected void fillInputSlots(RecipeEntry<R> recipe, boolean craftAll) {
        int j;
        boolean bl2 = this.handler.matches(recipe);
        int i = this.matcher.countCrafts(recipe, null);
        if (bl2) {
            for (j = 0; j < this.handler.getCraftingHeight() * this.handler.getCraftingWidth() + 1; ++j) {
                ItemStack lv;
                if (j == this.handler.getCraftingResultSlotIndex() || (lv = this.handler.getSlot(j).getStack()).isEmpty() || Math.min(i, lv.getMaxCount()) >= lv.getCount() + 1) continue;
                return;
            }
        }
        j = this.getAmountToFill(craftAll, i, bl2);
        IntArrayList intList = new IntArrayList();
        if (this.matcher.match((Recipe<?>)recipe.value(), intList, j)) {
            int k = j;
            IntListIterator intListIterator = intList.iterator();
            while (intListIterator.hasNext()) {
                int m;
                int l = (Integer)intListIterator.next();
                ItemStack lv2 = RecipeMatcher.getStackFromId(l);
                if (lv2.isEmpty() || (m = lv2.getMaxCount()) >= k) continue;
                k = m;
            }
            j = k;
            if (this.matcher.match((Recipe<?>)recipe.value(), intList, j)) {
                this.returnInputs();
                this.alignRecipeToGrid(this.handler.getCraftingWidth(), this.handler.getCraftingHeight(), this.handler.getCraftingResultSlotIndex(), recipe, intList.iterator(), j);
            }
        }
    }

    @Override
    public void acceptAlignedInput(Integer integer, int i, int j, int k, int l) {
        Slot lv = this.handler.getSlot(i);
        ItemStack lv2 = RecipeMatcher.getStackFromId(integer);
        if (lv2.isEmpty()) {
            return;
        }
        int m = j;
        while (m > 0) {
            if ((m = this.fillInputSlot(lv, lv2, m)) != -1) continue;
            return;
        }
    }

    protected int getAmountToFill(boolean craftAll, int limit, boolean recipeInCraftingSlots) {
        int j = 1;
        if (craftAll) {
            j = limit;
        } else if (recipeInCraftingSlots) {
            j = Integer.MAX_VALUE;
            for (int k = 0; k < this.handler.getCraftingWidth() * this.handler.getCraftingHeight() + 1; ++k) {
                ItemStack lv;
                if (k == this.handler.getCraftingResultSlotIndex() || (lv = this.handler.getSlot(k).getStack()).isEmpty() || j <= lv.getCount()) continue;
                j = lv.getCount();
            }
            if (j != Integer.MAX_VALUE) {
                ++j;
            }
        }
        return j;
    }

    protected int fillInputSlot(Slot slot, ItemStack stack, int i) {
        int k;
        int j = this.inventory.indexOf(stack);
        if (j == -1) {
            return -1;
        }
        ItemStack lv = this.inventory.getStack(j);
        if (i < lv.getCount()) {
            this.inventory.removeStack(j, i);
            k = i;
        } else {
            this.inventory.removeStack(j);
            k = lv.getCount();
        }
        if (slot.getStack().isEmpty()) {
            slot.setStackNoCallbacks(lv.copyWithCount(k));
        } else {
            slot.getStack().increment(k);
        }
        return i - k;
    }

    private boolean canReturnInputs() {
        ArrayList<ItemStack> list = Lists.newArrayList();
        int i = this.getFreeInventorySlots();
        for (int j = 0; j < this.handler.getCraftingWidth() * this.handler.getCraftingHeight() + 1; ++j) {
            ItemStack lv;
            if (j == this.handler.getCraftingResultSlotIndex() || (lv = this.handler.getSlot(j).getStack().copy()).isEmpty()) continue;
            int k = this.inventory.getOccupiedSlotWithRoomForStack(lv);
            if (k == -1 && list.size() <= i) {
                for (ItemStack lv2 : list) {
                    if (!ItemStack.areItemsEqual(lv2, lv) || lv2.getCount() == lv2.getMaxCount() || lv2.getCount() + lv.getCount() > lv2.getMaxCount()) continue;
                    lv2.increment(lv.getCount());
                    lv.setCount(0);
                    break;
                }
                if (lv.isEmpty()) continue;
                if (list.size() < i) {
                    list.add(lv);
                    continue;
                }
                return false;
            }
            if (k != -1) continue;
            return false;
        }
        return true;
    }

    private int getFreeInventorySlots() {
        int i = 0;
        for (ItemStack lv : this.inventory.main) {
            if (!lv.isEmpty()) continue;
            ++i;
        }
        return i;
    }
}

