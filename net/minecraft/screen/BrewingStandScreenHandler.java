/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.screen;

import java.util.Optional;
import net.minecraft.advancement.criterion.Criteria;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.PotionContentsComponent;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.Inventory;
import net.minecraft.inventory.SimpleInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.potion.Potion;
import net.minecraft.recipe.BrewingRecipeRegistry;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.screen.ArrayPropertyDelegate;
import net.minecraft.screen.PropertyDelegate;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.ScreenHandlerType;
import net.minecraft.screen.slot.Slot;
import net.minecraft.server.network.ServerPlayerEntity;

public class BrewingStandScreenHandler
extends ScreenHandler {
    private static final int field_30763 = 0;
    private static final int field_30764 = 2;
    private static final int INGREDIENT_SLOT_ID = 3;
    private static final int FUEL_SLOT_ID = 4;
    private static final int BREWING_STAND_INVENTORY_SIZE = 5;
    private static final int PROPERTY_COUNT = 2;
    private static final int INVENTORY_START = 5;
    private static final int INVENTORY_END = 32;
    private static final int HOTBAR_START = 32;
    private static final int HOTBAR_END = 41;
    private final Inventory inventory;
    private final PropertyDelegate propertyDelegate;
    private final Slot ingredientSlot;

    public BrewingStandScreenHandler(int syncId, PlayerInventory playerInventory) {
        this(syncId, playerInventory, new SimpleInventory(5), new ArrayPropertyDelegate(2));
    }

    public BrewingStandScreenHandler(int syncId, PlayerInventory playerInventory, Inventory inventory, PropertyDelegate propertyDelegate) {
        super(ScreenHandlerType.BREWING_STAND, syncId);
        int j;
        BrewingStandScreenHandler.checkSize(inventory, 5);
        BrewingStandScreenHandler.checkDataCount(propertyDelegate, 2);
        this.inventory = inventory;
        this.propertyDelegate = propertyDelegate;
        BrewingRecipeRegistry lv = playerInventory.player.getWorld().getBrewingRecipeRegistry();
        this.addSlot(new PotionSlot(inventory, 0, 56, 51));
        this.addSlot(new PotionSlot(inventory, 1, 79, 58));
        this.addSlot(new PotionSlot(inventory, 2, 102, 51));
        this.ingredientSlot = this.addSlot(new IngredientSlot(lv, inventory, 3, 79, 17));
        this.addSlot(new FuelSlot(inventory, 4, 17, 17));
        this.addProperties(propertyDelegate);
        for (j = 0; j < 3; ++j) {
            for (int k = 0; k < 9; ++k) {
                this.addSlot(new Slot(playerInventory, k + j * 9 + 9, 8 + k * 18, 84 + j * 18));
            }
        }
        for (j = 0; j < 9; ++j) {
            this.addSlot(new Slot(playerInventory, j, 8 + j * 18, 142));
        }
    }

    @Override
    public boolean canUse(PlayerEntity player) {
        return this.inventory.canPlayerUse(player);
    }

    @Override
    public ItemStack quickMove(PlayerEntity player, int slot) {
        ItemStack lv = ItemStack.EMPTY;
        Slot lv2 = (Slot)this.slots.get(slot);
        if (lv2 != null && lv2.hasStack()) {
            ItemStack lv3 = lv2.getStack();
            lv = lv3.copy();
            if (slot >= 0 && slot <= 2 || slot == 3 || slot == 4) {
                if (!this.insertItem(lv3, 5, 41, true)) {
                    return ItemStack.EMPTY;
                }
                lv2.onQuickTransfer(lv3, lv);
            } else if (FuelSlot.matches(lv) ? this.insertItem(lv3, 4, 5, false) || this.ingredientSlot.canInsert(lv3) && !this.insertItem(lv3, 3, 4, false) : (this.ingredientSlot.canInsert(lv3) ? !this.insertItem(lv3, 3, 4, false) : (PotionSlot.matches(lv) ? !this.insertItem(lv3, 0, 3, false) : (slot >= 5 && slot < 32 ? !this.insertItem(lv3, 32, 41, false) : (slot >= 32 && slot < 41 ? !this.insertItem(lv3, 5, 32, false) : !this.insertItem(lv3, 5, 41, false)))))) {
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
            lv2.onTakeItem(player, lv);
        }
        return lv;
    }

    public int getFuel() {
        return this.propertyDelegate.get(1);
    }

    public int getBrewTime() {
        return this.propertyDelegate.get(0);
    }

    static class PotionSlot
    extends Slot {
        public PotionSlot(Inventory arg, int i, int j, int k) {
            super(arg, i, j, k);
        }

        @Override
        public boolean canInsert(ItemStack stack) {
            return PotionSlot.matches(stack);
        }

        @Override
        public int getMaxItemCount() {
            return 1;
        }

        @Override
        public void onTakeItem(PlayerEntity player, ItemStack stack) {
            Optional<RegistryEntry<Potion>> optional = stack.getOrDefault(DataComponentTypes.POTION_CONTENTS, PotionContentsComponent.DEFAULT).potion();
            if (optional.isPresent() && player instanceof ServerPlayerEntity) {
                ServerPlayerEntity lv = (ServerPlayerEntity)player;
                Criteria.BREWED_POTION.trigger(lv, optional.get());
            }
            super.onTakeItem(player, stack);
        }

        public static boolean matches(ItemStack stack) {
            return stack.isOf(Items.POTION) || stack.isOf(Items.SPLASH_POTION) || stack.isOf(Items.LINGERING_POTION) || stack.isOf(Items.GLASS_BOTTLE);
        }
    }

    static class IngredientSlot
    extends Slot {
        private final BrewingRecipeRegistry brewingRecipeRegistry;

        public IngredientSlot(BrewingRecipeRegistry brewingRecipeRegistry, Inventory inventory, int index, int x, int y) {
            super(inventory, index, x, y);
            this.brewingRecipeRegistry = brewingRecipeRegistry;
        }

        @Override
        public boolean canInsert(ItemStack stack) {
            return this.brewingRecipeRegistry.isValidIngredient(stack);
        }
    }

    static class FuelSlot
    extends Slot {
        public FuelSlot(Inventory arg, int i, int j, int k) {
            super(arg, i, j, k);
        }

        @Override
        public boolean canInsert(ItemStack stack) {
            return FuelSlot.matches(stack);
        }

        public static boolean matches(ItemStack stack) {
            return stack.isOf(Items.BLAZE_POWDER);
        }
    }
}

