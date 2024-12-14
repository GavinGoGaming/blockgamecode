/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.screen;

import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.ScreenHandlerType;
import net.minecraft.screen.slot.Slot;
import net.minecraft.screen.slot.TradeOutputSlot;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.sound.SoundCategory;
import net.minecraft.village.Merchant;
import net.minecraft.village.MerchantInventory;
import net.minecraft.village.SimpleMerchant;
import net.minecraft.village.TradeOffer;
import net.minecraft.village.TradeOfferList;
import net.minecraft.village.TradedItem;

public class MerchantScreenHandler
extends ScreenHandler {
    protected static final int INPUT_1_ID = 0;
    protected static final int INPUT_2_ID = 1;
    protected static final int OUTPUT_ID = 2;
    private static final int INVENTORY_START = 3;
    private static final int INVENTORY_END = 30;
    private static final int HOTBAR_START = 30;
    private static final int HOTBAR_END = 39;
    private static final int INPUT_1_X = 136;
    private static final int INPUT_2_X = 162;
    private static final int OUTPUT_X = 220;
    private static final int SLOT_Y = 37;
    private final Merchant merchant;
    private final MerchantInventory merchantInventory;
    private int levelProgress;
    private boolean leveled;
    private boolean canRefreshTrades;

    public MerchantScreenHandler(int syncId, PlayerInventory playerInventory) {
        this(syncId, playerInventory, new SimpleMerchant(playerInventory.player));
    }

    public MerchantScreenHandler(int syncId, PlayerInventory playerInventory, Merchant merchant) {
        super(ScreenHandlerType.MERCHANT, syncId);
        int j;
        this.merchant = merchant;
        this.merchantInventory = new MerchantInventory(merchant);
        this.addSlot(new Slot(this.merchantInventory, 0, 136, 37));
        this.addSlot(new Slot(this.merchantInventory, 1, 162, 37));
        this.addSlot(new TradeOutputSlot(playerInventory.player, merchant, this.merchantInventory, 2, 220, 37));
        for (j = 0; j < 3; ++j) {
            for (int k = 0; k < 9; ++k) {
                this.addSlot(new Slot(playerInventory, k + j * 9 + 9, 108 + k * 18, 84 + j * 18));
            }
        }
        for (j = 0; j < 9; ++j) {
            this.addSlot(new Slot(playerInventory, j, 108 + j * 18, 142));
        }
    }

    public void setLeveled(boolean leveled) {
        this.leveled = leveled;
    }

    @Override
    public void onContentChanged(Inventory inventory) {
        this.merchantInventory.updateOffers();
        super.onContentChanged(inventory);
    }

    public void setRecipeIndex(int index) {
        this.merchantInventory.setOfferIndex(index);
    }

    @Override
    public boolean canUse(PlayerEntity player) {
        return this.merchant.getCustomer() == player;
    }

    public int getExperience() {
        return this.merchant.getExperience();
    }

    public int getMerchantRewardedExperience() {
        return this.merchantInventory.getMerchantRewardedExperience();
    }

    public void setExperienceFromServer(int experience) {
        this.merchant.setExperienceFromServer(experience);
    }

    public int getLevelProgress() {
        return this.levelProgress;
    }

    public void setLevelProgress(int levelProgress) {
        this.levelProgress = levelProgress;
    }

    public void setCanRefreshTrades(boolean canRefreshTrades) {
        this.canRefreshTrades = canRefreshTrades;
    }

    public boolean canRefreshTrades() {
        return this.canRefreshTrades;
    }

    @Override
    public boolean canInsertIntoSlot(ItemStack stack, Slot slot) {
        return false;
    }

    @Override
    public ItemStack quickMove(PlayerEntity player, int slot) {
        ItemStack lv = ItemStack.EMPTY;
        Slot lv2 = (Slot)this.slots.get(slot);
        if (lv2 != null && lv2.hasStack()) {
            ItemStack lv3 = lv2.getStack();
            lv = lv3.copy();
            if (slot == 2) {
                if (!this.insertItem(lv3, 3, 39, true)) {
                    return ItemStack.EMPTY;
                }
                lv2.onQuickTransfer(lv3, lv);
                this.playYesSound();
            } else if (slot == 0 || slot == 1 ? !this.insertItem(lv3, 3, 39, false) : (slot >= 3 && slot < 30 ? !this.insertItem(lv3, 30, 39, false) : slot >= 30 && slot < 39 && !this.insertItem(lv3, 3, 30, false))) {
                return ItemStack.EMPTY;
            }
            if (lv3.isEmpty()) {
                lv2.setStack(ItemStack.EMPTY);
            } else {
                lv2.markDirty();
            }
            if (lv3.getCount() == lv.getCount()) {
                return ItemStack.EMPTY;
            }
            lv2.onTakeItem(player, lv3);
        }
        return lv;
    }

    private void playYesSound() {
        if (!this.merchant.isClient()) {
            Entity lv = (Entity)((Object)this.merchant);
            lv.getWorld().playSound(lv.getX(), lv.getY(), lv.getZ(), this.merchant.getYesSound(), SoundCategory.NEUTRAL, 1.0f, 1.0f, false);
        }
    }

    @Override
    public void onClosed(PlayerEntity player) {
        super.onClosed(player);
        this.merchant.setCustomer(null);
        if (this.merchant.isClient()) {
            return;
        }
        if (!player.isAlive() || player instanceof ServerPlayerEntity && ((ServerPlayerEntity)player).isDisconnected()) {
            ItemStack lv = this.merchantInventory.removeStack(0);
            if (!lv.isEmpty()) {
                player.dropItem(lv, false);
            }
            if (!(lv = this.merchantInventory.removeStack(1)).isEmpty()) {
                player.dropItem(lv, false);
            }
        } else if (player instanceof ServerPlayerEntity) {
            player.getInventory().offerOrDrop(this.merchantInventory.removeStack(0));
            player.getInventory().offerOrDrop(this.merchantInventory.removeStack(1));
        }
    }

    public void switchTo(int recipeIndex) {
        ItemStack lv2;
        if (recipeIndex < 0 || this.getRecipes().size() <= recipeIndex) {
            return;
        }
        ItemStack lv = this.merchantInventory.getStack(0);
        if (!lv.isEmpty()) {
            if (!this.insertItem(lv, 3, 39, true)) {
                return;
            }
            this.merchantInventory.setStack(0, lv);
        }
        if (!(lv2 = this.merchantInventory.getStack(1)).isEmpty()) {
            if (!this.insertItem(lv2, 3, 39, true)) {
                return;
            }
            this.merchantInventory.setStack(1, lv2);
        }
        if (this.merchantInventory.getStack(0).isEmpty() && this.merchantInventory.getStack(1).isEmpty()) {
            TradeOffer lv3 = (TradeOffer)this.getRecipes().get(recipeIndex);
            this.autofill(0, lv3.getFirstBuyItem());
            lv3.getSecondBuyItem().ifPresent(item -> this.autofill(1, (TradedItem)item));
        }
    }

    private void autofill(int slot, TradedItem stack) {
        for (int j = 3; j < 39; ++j) {
            ItemStack lv2;
            ItemStack lv = ((Slot)this.slots.get(j)).getStack();
            if (lv.isEmpty() || !stack.matches(lv) || !(lv2 = this.merchantInventory.getStack(slot)).isEmpty() && !ItemStack.areItemsAndComponentsEqual(lv, lv2)) continue;
            int k = lv.getMaxCount();
            int l = Math.min(k - lv2.getCount(), lv.getCount());
            ItemStack lv3 = lv.copyWithCount(lv2.getCount() + l);
            lv.decrement(l);
            this.merchantInventory.setStack(slot, lv3);
            if (lv3.getCount() >= k) break;
        }
    }

    public void setOffers(TradeOfferList offers) {
        this.merchant.setOffersFromServer(offers);
    }

    public TradeOfferList getRecipes() {
        return this.merchant.getOffers();
    }

    public boolean isLeveled() {
        return this.leveled;
    }
}

