/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.screen;

import net.minecraft.block.Blocks;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.MapPostProcessingComponent;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.CraftingResultInventory;
import net.minecraft.inventory.Inventory;
import net.minecraft.inventory.SimpleInventory;
import net.minecraft.item.FilledMapItem;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.item.map.MapState;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.ScreenHandlerContext;
import net.minecraft.screen.ScreenHandlerType;
import net.minecraft.screen.slot.Slot;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.math.BlockPos;

public class CartographyTableScreenHandler
extends ScreenHandler {
    public static final int MAP_SLOT_INDEX = 0;
    public static final int MATERIAL_SLOT_INDEX = 1;
    public static final int RESULT_SLOT_INDEX = 2;
    private static final int INVENTORY_START = 3;
    private static final int INVENTORY_END = 30;
    private static final int HOTBAR_START = 30;
    private static final int HOTBAR_END = 39;
    private final ScreenHandlerContext context;
    long lastTakeResultTime;
    public final Inventory inventory = new SimpleInventory(2){

        @Override
        public void markDirty() {
            CartographyTableScreenHandler.this.onContentChanged(this);
            super.markDirty();
        }
    };
    private final CraftingResultInventory resultInventory = new CraftingResultInventory(){

        @Override
        public void markDirty() {
            CartographyTableScreenHandler.this.onContentChanged(this);
            super.markDirty();
        }
    };

    public CartographyTableScreenHandler(int syncId, PlayerInventory inventory) {
        this(syncId, inventory, ScreenHandlerContext.EMPTY);
    }

    public CartographyTableScreenHandler(int syncId, PlayerInventory inventory, final ScreenHandlerContext context) {
        super(ScreenHandlerType.CARTOGRAPHY_TABLE, syncId);
        int j;
        this.context = context;
        this.addSlot(new Slot(this, this.inventory, 0, 15, 15){

            @Override
            public boolean canInsert(ItemStack stack) {
                return stack.isOf(Items.FILLED_MAP);
            }
        });
        this.addSlot(new Slot(this, this.inventory, 1, 15, 52){

            @Override
            public boolean canInsert(ItemStack stack) {
                return stack.isOf(Items.PAPER) || stack.isOf(Items.MAP) || stack.isOf(Items.GLASS_PANE);
            }
        });
        this.addSlot(new Slot(this.resultInventory, 2, 145, 39){

            @Override
            public boolean canInsert(ItemStack stack) {
                return false;
            }

            @Override
            public void onTakeItem(PlayerEntity player, ItemStack stack) {
                ((Slot)CartographyTableScreenHandler.this.slots.get(0)).takeStack(1);
                ((Slot)CartographyTableScreenHandler.this.slots.get(1)).takeStack(1);
                stack.getItem().onCraftByPlayer(stack, player.getWorld(), player);
                context.run((world, pos) -> {
                    long l = world.getTime();
                    if (CartographyTableScreenHandler.this.lastTakeResultTime != l) {
                        world.playSound(null, (BlockPos)pos, SoundEvents.UI_CARTOGRAPHY_TABLE_TAKE_RESULT, SoundCategory.BLOCKS, 1.0f, 1.0f);
                        CartographyTableScreenHandler.this.lastTakeResultTime = l;
                    }
                });
                super.onTakeItem(player, stack);
            }
        });
        for (j = 0; j < 3; ++j) {
            for (int k = 0; k < 9; ++k) {
                this.addSlot(new Slot(inventory, k + j * 9 + 9, 8 + k * 18, 84 + j * 18));
            }
        }
        for (j = 0; j < 9; ++j) {
            this.addSlot(new Slot(inventory, j, 8 + j * 18, 142));
        }
    }

    @Override
    public boolean canUse(PlayerEntity player) {
        return CartographyTableScreenHandler.canUse(this.context, player, Blocks.CARTOGRAPHY_TABLE);
    }

    @Override
    public void onContentChanged(Inventory inventory) {
        ItemStack lv = this.inventory.getStack(0);
        ItemStack lv2 = this.inventory.getStack(1);
        ItemStack lv3 = this.resultInventory.getStack(2);
        if (!lv3.isEmpty() && (lv.isEmpty() || lv2.isEmpty())) {
            this.resultInventory.removeStack(2);
        } else if (!lv.isEmpty() && !lv2.isEmpty()) {
            this.updateResult(lv, lv2, lv3);
        }
    }

    private void updateResult(ItemStack map, ItemStack item, ItemStack oldResult) {
        this.context.run((world, pos) -> {
            ItemStack lv2;
            MapState lv = FilledMapItem.getMapState(map, world);
            if (lv == null) {
                return;
            }
            if (item.isOf(Items.PAPER) && !lv.locked && lv.scale < 4) {
                lv2 = map.copyWithCount(1);
                lv2.set(DataComponentTypes.MAP_POST_PROCESSING, MapPostProcessingComponent.SCALE);
                this.sendContentUpdates();
            } else if (item.isOf(Items.GLASS_PANE) && !lv.locked) {
                lv2 = map.copyWithCount(1);
                lv2.set(DataComponentTypes.MAP_POST_PROCESSING, MapPostProcessingComponent.LOCK);
                this.sendContentUpdates();
            } else if (item.isOf(Items.MAP)) {
                lv2 = map.copyWithCount(2);
                this.sendContentUpdates();
            } else {
                this.resultInventory.removeStack(2);
                this.sendContentUpdates();
                return;
            }
            if (!ItemStack.areEqual(lv2, oldResult)) {
                this.resultInventory.setStack(2, lv2);
                this.sendContentUpdates();
            }
        });
    }

    @Override
    public boolean canInsertIntoSlot(ItemStack stack, Slot slot) {
        return slot.inventory != this.resultInventory && super.canInsertIntoSlot(stack, slot);
    }

    @Override
    public ItemStack quickMove(PlayerEntity player, int slot) {
        ItemStack lv = ItemStack.EMPTY;
        Slot lv2 = (Slot)this.slots.get(slot);
        if (lv2 != null && lv2.hasStack()) {
            ItemStack lv3 = lv2.getStack();
            lv = lv3.copy();
            if (slot == 2) {
                lv3.getItem().onCraftByPlayer(lv3, player.getWorld(), player);
                if (!this.insertItem(lv3, 3, 39, true)) {
                    return ItemStack.EMPTY;
                }
                lv2.onQuickTransfer(lv3, lv);
            } else if (slot == 1 || slot == 0 ? !this.insertItem(lv3, 3, 39, false) : (lv3.isOf(Items.FILLED_MAP) ? !this.insertItem(lv3, 0, 1, false) : (lv3.isOf(Items.PAPER) || lv3.isOf(Items.MAP) || lv3.isOf(Items.GLASS_PANE) ? !this.insertItem(lv3, 1, 2, false) : (slot >= 3 && slot < 30 ? !this.insertItem(lv3, 30, 39, false) : slot >= 30 && slot < 39 && !this.insertItem(lv3, 3, 30, false))))) {
                return ItemStack.EMPTY;
            }
            if (lv3.isEmpty()) {
                lv2.setStack(ItemStack.EMPTY);
            }
            lv2.markDirty();
            if (lv3.getCount() == lv.getCount()) {
                return ItemStack.EMPTY;
            }
            lv2.onTakeItem(player, lv3);
            this.sendContentUpdates();
        }
        return lv;
    }

    @Override
    public void onClosed(PlayerEntity player) {
        super.onClosed(player);
        this.resultInventory.removeStack(2);
        this.context.run((world, pos) -> this.dropInventory(player, this.inventory));
    }
}

