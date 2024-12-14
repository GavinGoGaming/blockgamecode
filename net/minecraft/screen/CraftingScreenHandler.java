/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 * 
 * Could not load the following classes:
 *  org.jetbrains.annotations.Nullable
 */
package net.minecraft.screen;

import java.util.Optional;
import net.minecraft.block.Blocks;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.CraftingInventory;
import net.minecraft.inventory.CraftingResultInventory;
import net.minecraft.inventory.Inventory;
import net.minecraft.inventory.RecipeInputInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.network.packet.s2c.play.ScreenHandlerSlotUpdateS2CPacket;
import net.minecraft.recipe.CraftingRecipe;
import net.minecraft.recipe.RecipeEntry;
import net.minecraft.recipe.RecipeMatcher;
import net.minecraft.recipe.RecipeType;
import net.minecraft.recipe.book.RecipeBookCategory;
import net.minecraft.recipe.input.CraftingRecipeInput;
import net.minecraft.screen.AbstractRecipeScreenHandler;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.ScreenHandlerContext;
import net.minecraft.screen.ScreenHandlerType;
import net.minecraft.screen.slot.CraftingResultSlot;
import net.minecraft.screen.slot.Slot;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

public class CraftingScreenHandler
extends AbstractRecipeScreenHandler<CraftingRecipeInput, CraftingRecipe> {
    public static final int RESULT_ID = 0;
    private static final int INPUT_START = 1;
    private static final int INPUT_END = 10;
    private static final int INVENTORY_START = 10;
    private static final int INVENTORY_END = 37;
    private static final int HOTBAR_START = 37;
    private static final int HOTBAR_END = 46;
    private final RecipeInputInventory input = new CraftingInventory(this, 3, 3);
    private final CraftingResultInventory result = new CraftingResultInventory();
    private final ScreenHandlerContext context;
    private final PlayerEntity player;
    private boolean filling;

    public CraftingScreenHandler(int syncId, PlayerInventory playerInventory) {
        this(syncId, playerInventory, ScreenHandlerContext.EMPTY);
    }

    public CraftingScreenHandler(int syncId, PlayerInventory playerInventory, ScreenHandlerContext context) {
        super(ScreenHandlerType.CRAFTING, syncId);
        int k;
        int j;
        this.context = context;
        this.player = playerInventory.player;
        this.addSlot(new CraftingResultSlot(playerInventory.player, this.input, this.result, 0, 124, 35));
        for (j = 0; j < 3; ++j) {
            for (k = 0; k < 3; ++k) {
                this.addSlot(new Slot(this.input, k + j * 3, 30 + k * 18, 17 + j * 18));
            }
        }
        for (j = 0; j < 3; ++j) {
            for (k = 0; k < 9; ++k) {
                this.addSlot(new Slot(playerInventory, k + j * 9 + 9, 8 + k * 18, 84 + j * 18));
            }
        }
        for (j = 0; j < 9; ++j) {
            this.addSlot(new Slot(playerInventory, j, 8 + j * 18, 142));
        }
    }

    protected static void updateResult(ScreenHandler handler, World world, PlayerEntity player, RecipeInputInventory craftingInventory, CraftingResultInventory resultInventory, @Nullable RecipeEntry<CraftingRecipe> recipe) {
        if (world.isClient) {
            return;
        }
        CraftingRecipeInput lv = craftingInventory.createRecipeInput();
        ServerPlayerEntity lv2 = (ServerPlayerEntity)player;
        ItemStack lv3 = ItemStack.EMPTY;
        Optional<RecipeEntry<CraftingRecipe>> optional = world.getServer().getRecipeManager().getFirstMatch(RecipeType.CRAFTING, lv, world, recipe);
        if (optional.isPresent()) {
            ItemStack lv6;
            RecipeEntry<CraftingRecipe> lv4 = optional.get();
            CraftingRecipe lv5 = lv4.value();
            if (resultInventory.shouldCraftRecipe(world, lv2, lv4) && (lv6 = lv5.craft(lv, world.getRegistryManager())).isItemEnabled(world.getEnabledFeatures())) {
                lv3 = lv6;
            }
        }
        resultInventory.setStack(0, lv3);
        handler.setPreviousTrackedSlot(0, lv3);
        lv2.networkHandler.sendPacket(new ScreenHandlerSlotUpdateS2CPacket(handler.syncId, handler.nextRevision(), 0, lv3));
    }

    @Override
    public void onContentChanged(Inventory inventory) {
        if (!this.filling) {
            this.context.run((world, pos) -> CraftingScreenHandler.updateResult(this, world, this.player, this.input, this.result, null));
        }
    }

    @Override
    public void onInputSlotFillStart() {
        this.filling = true;
    }

    @Override
    public void onInputSlotFillFinish(RecipeEntry<CraftingRecipe> recipe) {
        this.filling = false;
        this.context.run((world, pos) -> CraftingScreenHandler.updateResult(this, world, this.player, this.input, this.result, recipe));
    }

    @Override
    public void populateRecipeFinder(RecipeMatcher finder) {
        this.input.provideRecipeInputs(finder);
    }

    @Override
    public void clearCraftingSlots() {
        this.input.clear();
        this.result.clear();
    }

    @Override
    public boolean matches(RecipeEntry<CraftingRecipe> recipe) {
        return recipe.value().matches(this.input.createRecipeInput(), this.player.getWorld());
    }

    @Override
    public void onClosed(PlayerEntity player) {
        super.onClosed(player);
        this.context.run((world, pos) -> this.dropInventory(player, this.input));
    }

    @Override
    public boolean canUse(PlayerEntity player) {
        return CraftingScreenHandler.canUse(this.context, player, Blocks.CRAFTING_TABLE);
    }

    @Override
    public ItemStack quickMove(PlayerEntity player, int slot) {
        ItemStack lv = ItemStack.EMPTY;
        Slot lv2 = (Slot)this.slots.get(slot);
        if (lv2 != null && lv2.hasStack()) {
            ItemStack lv3 = lv2.getStack();
            lv = lv3.copy();
            if (slot == 0) {
                this.context.run((world, pos) -> lv3.getItem().onCraftByPlayer(lv3, (World)world, player));
                if (!this.insertItem(lv3, 10, 46, true)) {
                    return ItemStack.EMPTY;
                }
                lv2.onQuickTransfer(lv3, lv);
            } else if (slot >= 10 && slot < 46 ? !this.insertItem(lv3, 1, 10, false) && (slot < 37 ? !this.insertItem(lv3, 37, 46, false) : !this.insertItem(lv3, 10, 37, false)) : !this.insertItem(lv3, 10, 46, false)) {
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
            if (slot == 0) {
                player.dropItem(lv3, false);
            }
        }
        return lv;
    }

    @Override
    public boolean canInsertIntoSlot(ItemStack stack, Slot slot) {
        return slot.inventory != this.result && super.canInsertIntoSlot(stack, slot);
    }

    @Override
    public int getCraftingResultSlotIndex() {
        return 0;
    }

    @Override
    public int getCraftingWidth() {
        return this.input.getWidth();
    }

    @Override
    public int getCraftingHeight() {
        return this.input.getHeight();
    }

    @Override
    public int getCraftingSlotCount() {
        return 10;
    }

    @Override
    public RecipeBookCategory getCategory() {
        return RecipeBookCategory.CRAFTING;
    }

    @Override
    public boolean canInsertIntoSlot(int index) {
        return index != this.getCraftingResultSlotIndex();
    }
}

