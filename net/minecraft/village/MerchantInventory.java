/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 * 
 * Could not load the following classes:
 *  org.jetbrains.annotations.Nullable
 */
package net.minecraft.village;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.Inventories;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;
import net.minecraft.util.collection.DefaultedList;
import net.minecraft.village.Merchant;
import net.minecraft.village.TradeOffer;
import net.minecraft.village.TradeOfferList;
import org.jetbrains.annotations.Nullable;

public class MerchantInventory
implements Inventory {
    private final Merchant merchant;
    private final DefaultedList<ItemStack> inventory = DefaultedList.ofSize(3, ItemStack.EMPTY);
    @Nullable
    private TradeOffer tradeOffer;
    private int offerIndex;
    private int merchantRewardedExperience;

    public MerchantInventory(Merchant merchant) {
        this.merchant = merchant;
    }

    @Override
    public int size() {
        return this.inventory.size();
    }

    @Override
    public boolean isEmpty() {
        for (ItemStack lv : this.inventory) {
            if (lv.isEmpty()) continue;
            return false;
        }
        return true;
    }

    @Override
    public ItemStack getStack(int slot) {
        return this.inventory.get(slot);
    }

    @Override
    public ItemStack removeStack(int slot, int amount) {
        ItemStack lv = this.inventory.get(slot);
        if (slot == 2 && !lv.isEmpty()) {
            return Inventories.splitStack(this.inventory, slot, lv.getCount());
        }
        ItemStack lv2 = Inventories.splitStack(this.inventory, slot, amount);
        if (!lv2.isEmpty() && this.needsOfferUpdate(slot)) {
            this.updateOffers();
        }
        return lv2;
    }

    private boolean needsOfferUpdate(int slot) {
        return slot == 0 || slot == 1;
    }

    @Override
    public ItemStack removeStack(int slot) {
        return Inventories.removeStack(this.inventory, slot);
    }

    @Override
    public void setStack(int slot, ItemStack stack) {
        this.inventory.set(slot, stack);
        stack.capCount(this.getMaxCount(stack));
        if (this.needsOfferUpdate(slot)) {
            this.updateOffers();
        }
    }

    @Override
    public boolean canPlayerUse(PlayerEntity player) {
        return this.merchant.getCustomer() == player;
    }

    @Override
    public void markDirty() {
        this.updateOffers();
    }

    public void updateOffers() {
        ItemStack lv2;
        ItemStack lv;
        this.tradeOffer = null;
        if (this.inventory.get(0).isEmpty()) {
            lv = this.inventory.get(1);
            lv2 = ItemStack.EMPTY;
        } else {
            lv = this.inventory.get(0);
            lv2 = this.inventory.get(1);
        }
        if (lv.isEmpty()) {
            this.setStack(2, ItemStack.EMPTY);
            this.merchantRewardedExperience = 0;
            return;
        }
        TradeOfferList lv3 = this.merchant.getOffers();
        if (!lv3.isEmpty()) {
            TradeOffer lv4 = lv3.getValidOffer(lv, lv2, this.offerIndex);
            if (lv4 == null || lv4.isDisabled()) {
                this.tradeOffer = lv4;
                lv4 = lv3.getValidOffer(lv2, lv, this.offerIndex);
            }
            if (lv4 != null && !lv4.isDisabled()) {
                this.tradeOffer = lv4;
                this.setStack(2, lv4.copySellItem());
                this.merchantRewardedExperience = lv4.getMerchantExperience();
            } else {
                this.setStack(2, ItemStack.EMPTY);
                this.merchantRewardedExperience = 0;
            }
        }
        this.merchant.onSellingItem(this.getStack(2));
    }

    @Nullable
    public TradeOffer getTradeOffer() {
        return this.tradeOffer;
    }

    public void setOfferIndex(int index) {
        this.offerIndex = index;
        this.updateOffers();
    }

    @Override
    public void clear() {
        this.inventory.clear();
    }

    public int getMerchantRewardedExperience() {
        return this.merchantRewardedExperience;
    }
}

