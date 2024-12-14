/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.screen;

import com.google.common.collect.ImmutableList;
import java.util.List;
import net.minecraft.block.Blocks;
import net.minecraft.block.entity.BannerPattern;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.BannerPatternsComponent;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.Inventory;
import net.minecraft.inventory.SimpleInventory;
import net.minecraft.item.BannerItem;
import net.minecraft.item.BannerPatternItem;
import net.minecraft.item.DyeItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.RegistryEntryLookup;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.registry.tag.BannerPatternTags;
import net.minecraft.screen.Property;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.ScreenHandlerContext;
import net.minecraft.screen.ScreenHandlerType;
import net.minecraft.screen.slot.Slot;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.DyeColor;
import net.minecraft.util.math.BlockPos;

public class LoomScreenHandler
extends ScreenHandler {
    private static final int NO_PATTERN = -1;
    private static final int INVENTORY_START = 4;
    private static final int INVENTORY_END = 31;
    private static final int HOTBAR_START = 31;
    private static final int HOTBAR_END = 40;
    private final ScreenHandlerContext context;
    final Property selectedPattern = Property.create();
    private List<RegistryEntry<BannerPattern>> bannerPatterns = List.of();
    Runnable inventoryChangeListener = () -> {};
    private final RegistryEntryLookup<BannerPattern> bannerPatternLookup;
    final Slot bannerSlot;
    final Slot dyeSlot;
    private final Slot patternSlot;
    private final Slot outputSlot;
    long lastTakeResultTime;
    private final Inventory input = new SimpleInventory(3){

        @Override
        public void markDirty() {
            super.markDirty();
            LoomScreenHandler.this.onContentChanged(this);
            LoomScreenHandler.this.inventoryChangeListener.run();
        }
    };
    private final Inventory output = new SimpleInventory(1){

        @Override
        public void markDirty() {
            super.markDirty();
            LoomScreenHandler.this.inventoryChangeListener.run();
        }
    };

    public LoomScreenHandler(int syncId, PlayerInventory playerInventory) {
        this(syncId, playerInventory, ScreenHandlerContext.EMPTY);
    }

    public LoomScreenHandler(int syncId, PlayerInventory playerInventory, final ScreenHandlerContext context) {
        super(ScreenHandlerType.LOOM, syncId);
        int j;
        this.context = context;
        this.bannerSlot = this.addSlot(new Slot(this, this.input, 0, 13, 26){

            @Override
            public boolean canInsert(ItemStack stack) {
                return stack.getItem() instanceof BannerItem;
            }
        });
        this.dyeSlot = this.addSlot(new Slot(this, this.input, 1, 33, 26){

            @Override
            public boolean canInsert(ItemStack stack) {
                return stack.getItem() instanceof DyeItem;
            }
        });
        this.patternSlot = this.addSlot(new Slot(this, this.input, 2, 23, 45){

            @Override
            public boolean canInsert(ItemStack stack) {
                return stack.getItem() instanceof BannerPatternItem;
            }
        });
        this.outputSlot = this.addSlot(new Slot(this.output, 0, 143, 57){

            @Override
            public boolean canInsert(ItemStack stack) {
                return false;
            }

            @Override
            public void onTakeItem(PlayerEntity player, ItemStack stack) {
                LoomScreenHandler.this.bannerSlot.takeStack(1);
                LoomScreenHandler.this.dyeSlot.takeStack(1);
                if (!LoomScreenHandler.this.bannerSlot.hasStack() || !LoomScreenHandler.this.dyeSlot.hasStack()) {
                    LoomScreenHandler.this.selectedPattern.set(-1);
                }
                context.run((world, pos) -> {
                    long l = world.getTime();
                    if (LoomScreenHandler.this.lastTakeResultTime != l) {
                        world.playSound(null, (BlockPos)pos, SoundEvents.UI_LOOM_TAKE_RESULT, SoundCategory.BLOCKS, 1.0f, 1.0f);
                        LoomScreenHandler.this.lastTakeResultTime = l;
                    }
                });
                super.onTakeItem(player, stack);
            }
        });
        for (j = 0; j < 3; ++j) {
            for (int k = 0; k < 9; ++k) {
                this.addSlot(new Slot(playerInventory, k + j * 9 + 9, 8 + k * 18, 84 + j * 18));
            }
        }
        for (j = 0; j < 9; ++j) {
            this.addSlot(new Slot(playerInventory, j, 8 + j * 18, 142));
        }
        this.addProperty(this.selectedPattern);
        this.bannerPatternLookup = playerInventory.player.getRegistryManager().getWrapperOrThrow(RegistryKeys.BANNER_PATTERN);
    }

    @Override
    public boolean canUse(PlayerEntity player) {
        return LoomScreenHandler.canUse(this.context, player, Blocks.LOOM);
    }

    @Override
    public boolean onButtonClick(PlayerEntity player, int id) {
        if (id >= 0 && id < this.bannerPatterns.size()) {
            this.selectedPattern.set(id);
            this.updateOutputSlot(this.bannerPatterns.get(id));
            return true;
        }
        return false;
    }

    private List<RegistryEntry<BannerPattern>> getPatternsFor(ItemStack stack) {
        if (stack.isEmpty()) {
            return this.bannerPatternLookup.getOptional(BannerPatternTags.NO_ITEM_REQUIRED).map(ImmutableList::copyOf).orElse(ImmutableList.of());
        }
        Item item = stack.getItem();
        if (item instanceof BannerPatternItem) {
            BannerPatternItem lv = (BannerPatternItem)item;
            return this.bannerPatternLookup.getOptional(lv.getPattern()).map(ImmutableList::copyOf).orElse(ImmutableList.of());
        }
        return List.of();
    }

    private boolean isPatternIndexValid(int index) {
        return index >= 0 && index < this.bannerPatterns.size();
    }

    @Override
    public void onContentChanged(Inventory inventory) {
        RegistryEntry<BannerPattern> lv4;
        ItemStack lv = this.bannerSlot.getStack();
        ItemStack lv2 = this.dyeSlot.getStack();
        ItemStack lv3 = this.patternSlot.getStack();
        if (lv.isEmpty() || lv2.isEmpty()) {
            this.outputSlot.setStackNoCallbacks(ItemStack.EMPTY);
            this.bannerPatterns = List.of();
            this.selectedPattern.set(-1);
            return;
        }
        int i = this.selectedPattern.get();
        boolean bl = this.isPatternIndexValid(i);
        List<RegistryEntry<BannerPattern>> list = this.bannerPatterns;
        this.bannerPatterns = this.getPatternsFor(lv3);
        if (this.bannerPatterns.size() == 1) {
            this.selectedPattern.set(0);
            lv4 = this.bannerPatterns.get(0);
        } else if (!bl) {
            this.selectedPattern.set(-1);
            lv4 = null;
        } else {
            RegistryEntry<BannerPattern> lv5 = list.get(i);
            int j = this.bannerPatterns.indexOf(lv5);
            if (j != -1) {
                lv4 = lv5;
                this.selectedPattern.set(j);
            } else {
                lv4 = null;
                this.selectedPattern.set(-1);
            }
        }
        if (lv4 != null) {
            boolean bl2;
            BannerPatternsComponent lv6 = lv.getOrDefault(DataComponentTypes.BANNER_PATTERNS, BannerPatternsComponent.DEFAULT);
            boolean bl3 = bl2 = lv6.layers().size() >= 6;
            if (bl2) {
                this.selectedPattern.set(-1);
                this.outputSlot.setStackNoCallbacks(ItemStack.EMPTY);
            } else {
                this.updateOutputSlot(lv4);
            }
        } else {
            this.outputSlot.setStackNoCallbacks(ItemStack.EMPTY);
        }
        this.sendContentUpdates();
    }

    public List<RegistryEntry<BannerPattern>> getBannerPatterns() {
        return this.bannerPatterns;
    }

    public int getSelectedPattern() {
        return this.selectedPattern.get();
    }

    public void setInventoryChangeListener(Runnable inventoryChangeListener) {
        this.inventoryChangeListener = inventoryChangeListener;
    }

    @Override
    public ItemStack quickMove(PlayerEntity player, int slot) {
        ItemStack lv = ItemStack.EMPTY;
        Slot lv2 = (Slot)this.slots.get(slot);
        if (lv2 != null && lv2.hasStack()) {
            ItemStack lv3 = lv2.getStack();
            lv = lv3.copy();
            if (slot == this.outputSlot.id) {
                if (!this.insertItem(lv3, 4, 40, true)) {
                    return ItemStack.EMPTY;
                }
                lv2.onQuickTransfer(lv3, lv);
            } else if (slot == this.dyeSlot.id || slot == this.bannerSlot.id || slot == this.patternSlot.id ? !this.insertItem(lv3, 4, 40, false) : (lv3.getItem() instanceof BannerItem ? !this.insertItem(lv3, this.bannerSlot.id, this.bannerSlot.id + 1, false) : (lv3.getItem() instanceof DyeItem ? !this.insertItem(lv3, this.dyeSlot.id, this.dyeSlot.id + 1, false) : (lv3.getItem() instanceof BannerPatternItem ? !this.insertItem(lv3, this.patternSlot.id, this.patternSlot.id + 1, false) : (slot >= 4 && slot < 31 ? !this.insertItem(lv3, 31, 40, false) : slot >= 31 && slot < 40 && !this.insertItem(lv3, 4, 31, false)))))) {
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

    @Override
    public void onClosed(PlayerEntity player) {
        super.onClosed(player);
        this.context.run((world, pos) -> this.dropInventory(player, this.input));
    }

    private void updateOutputSlot(RegistryEntry<BannerPattern> pattern) {
        ItemStack lv = this.bannerSlot.getStack();
        ItemStack lv2 = this.dyeSlot.getStack();
        ItemStack lv3 = ItemStack.EMPTY;
        if (!lv.isEmpty() && !lv2.isEmpty()) {
            lv3 = lv.copyWithCount(1);
            DyeColor lv4 = ((DyeItem)lv2.getItem()).getColor();
            lv3.apply(DataComponentTypes.BANNER_PATTERNS, BannerPatternsComponent.DEFAULT, component -> new BannerPatternsComponent.Builder().addAll((BannerPatternsComponent)component).add(pattern, lv4).build());
        }
        if (!ItemStack.areEqual(lv3, this.outputSlot.getStack())) {
            this.outputSlot.setStackNoCallbacks(lv3);
        }
    }

    public Slot getBannerSlot() {
        return this.bannerSlot;
    }

    public Slot getDyeSlot() {
        return this.dyeSlot;
    }

    public Slot getPatternSlot() {
        return this.patternSlot;
    }

    public Slot getOutputSlot() {
        return this.outputSlot;
    }
}

