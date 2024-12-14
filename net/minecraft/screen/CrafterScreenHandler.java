/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.screen;

import net.minecraft.block.CrafterBlock;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.CraftingInventory;
import net.minecraft.inventory.CraftingResultInventory;
import net.minecraft.inventory.Inventory;
import net.minecraft.inventory.RecipeInputInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.recipe.CraftingRecipe;
import net.minecraft.recipe.input.CraftingRecipeInput;
import net.minecraft.screen.ArrayPropertyDelegate;
import net.minecraft.screen.PropertyDelegate;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.ScreenHandlerListener;
import net.minecraft.screen.ScreenHandlerType;
import net.minecraft.screen.slot.CrafterInputSlot;
import net.minecraft.screen.slot.CrafterOutputSlot;
import net.minecraft.screen.slot.Slot;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.world.World;

public class CrafterScreenHandler
extends ScreenHandler
implements ScreenHandlerListener {
    protected static final int field_46781 = 9;
    private static final int field_46782 = 9;
    private static final int field_46783 = 36;
    private static final int field_46784 = 36;
    private static final int field_46785 = 45;
    private final CraftingResultInventory resultInventory = new CraftingResultInventory();
    private final PropertyDelegate propertyDelegate;
    private final PlayerEntity player;
    private final RecipeInputInventory inputInventory;

    public CrafterScreenHandler(int syncId, PlayerInventory playerInventory) {
        super(ScreenHandlerType.CRAFTER_3X3, syncId);
        this.player = playerInventory.player;
        this.propertyDelegate = new ArrayPropertyDelegate(10);
        this.inputInventory = new CraftingInventory(this, 3, 3);
        this.addSlots(playerInventory);
    }

    public CrafterScreenHandler(int syncId, PlayerInventory playerInventory, RecipeInputInventory inputInventory, PropertyDelegate propertyDelegate) {
        super(ScreenHandlerType.CRAFTER_3X3, syncId);
        this.player = playerInventory.player;
        this.propertyDelegate = propertyDelegate;
        this.inputInventory = inputInventory;
        CrafterScreenHandler.checkSize(inputInventory, 9);
        inputInventory.onOpen(playerInventory.player);
        this.addSlots(playerInventory);
        this.addListener(this);
    }

    private void addSlots(PlayerInventory playerInventory) {
        int j;
        int i;
        for (i = 0; i < 3; ++i) {
            for (j = 0; j < 3; ++j) {
                int k = j + i * 3;
                this.addSlot(new CrafterInputSlot(this.inputInventory, k, 26 + j * 18, 17 + i * 18, this));
            }
        }
        for (i = 0; i < 3; ++i) {
            for (j = 0; j < 9; ++j) {
                this.addSlot(new Slot(playerInventory, j + i * 9 + 9, 8 + j * 18, 84 + i * 18));
            }
        }
        for (i = 0; i < 9; ++i) {
            this.addSlot(new Slot(playerInventory, i, 8 + i * 18, 142));
        }
        this.addSlot(new CrafterOutputSlot(this.resultInventory, 0, 134, 35));
        this.addProperties(this.propertyDelegate);
        this.updateResult();
    }

    public void setSlotEnabled(int slot, boolean enabled) {
        CrafterInputSlot lv = (CrafterInputSlot)this.getSlot(slot);
        this.propertyDelegate.set(lv.id, enabled ? 0 : 1);
        this.sendContentUpdates();
    }

    public boolean isSlotDisabled(int slot) {
        if (slot > -1 && slot < 9) {
            return this.propertyDelegate.get(slot) == 1;
        }
        return false;
    }

    public boolean isTriggered() {
        return this.propertyDelegate.get(9) == 1;
    }

    @Override
    public ItemStack quickMove(PlayerEntity player, int slot) {
        ItemStack lv = ItemStack.EMPTY;
        Slot lv2 = (Slot)this.slots.get(slot);
        if (lv2 != null && lv2.hasStack()) {
            ItemStack lv3 = lv2.getStack();
            lv = lv3.copy();
            if (slot < 9 ? !this.insertItem(lv3, 9, 45, true) : !this.insertItem(lv3, 0, 9, false)) {
                return ItemStack.EMPTY;
            }
            if (lv3.isEmpty()) {
                lv2.setStackNoCallbacks(ItemStack.EMPTY);
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

    @Override
    public boolean canUse(PlayerEntity player) {
        return this.inputInventory.canPlayerUse(player);
    }

    private void updateResult() {
        PlayerEntity playerEntity = this.player;
        if (playerEntity instanceof ServerPlayerEntity) {
            ServerPlayerEntity lv = (ServerPlayerEntity)playerEntity;
            World lv2 = lv.getWorld();
            CraftingRecipeInput lv3 = this.inputInventory.createRecipeInput();
            ItemStack lv4 = CrafterBlock.getCraftingRecipe(lv2, lv3).map(arg3 -> ((CraftingRecipe)arg3.value()).craft(lv3, lv2.getRegistryManager())).orElse(ItemStack.EMPTY);
            this.resultInventory.setStack(0, lv4);
        }
    }

    public Inventory getInputInventory() {
        return this.inputInventory;
    }

    @Override
    public void onSlotUpdate(ScreenHandler handler, int slotId, ItemStack stack) {
        this.updateResult();
    }

    @Override
    public void onPropertyUpdate(ScreenHandler handler, int property, int value) {
    }
}

