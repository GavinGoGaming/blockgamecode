/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 * 
 * Could not load the following classes:
 *  org.jetbrains.annotations.Nullable
 */
package net.minecraft.screen;

import java.util.Optional;
import net.minecraft.block.Blocks;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.Inventory;
import net.minecraft.inventory.SimpleInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registries;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.registry.tag.ItemTags;
import net.minecraft.screen.ArrayPropertyDelegate;
import net.minecraft.screen.PropertyDelegate;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.ScreenHandlerContext;
import net.minecraft.screen.ScreenHandlerType;
import net.minecraft.screen.slot.Slot;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

public class BeaconScreenHandler
extends ScreenHandler {
    private static final int PAYMENT_SLOT_ID = 0;
    private static final int BEACON_INVENTORY_SIZE = 1;
    private static final int PROPERTY_COUNT = 3;
    private static final int INVENTORY_START = 1;
    private static final int INVENTORY_END = 28;
    private static final int HOTBAR_START = 28;
    private static final int HOTBAR_END = 37;
    private static final int field_45758 = 0;
    private final Inventory payment = new SimpleInventory(this, 1){

        @Override
        public boolean isValid(int slot, ItemStack stack) {
            return stack.isIn(ItemTags.BEACON_PAYMENT_ITEMS);
        }

        @Override
        public int getMaxCountPerStack() {
            return 1;
        }
    };
    private final PaymentSlot paymentSlot;
    private final ScreenHandlerContext context;
    private final PropertyDelegate propertyDelegate;

    public BeaconScreenHandler(int syncId, Inventory inventory) {
        this(syncId, inventory, new ArrayPropertyDelegate(3), ScreenHandlerContext.EMPTY);
    }

    public BeaconScreenHandler(int syncId, Inventory inventory, PropertyDelegate propertyDelegate, ScreenHandlerContext context) {
        super(ScreenHandlerType.BEACON, syncId);
        int l;
        BeaconScreenHandler.checkDataCount(propertyDelegate, 3);
        this.propertyDelegate = propertyDelegate;
        this.context = context;
        this.paymentSlot = new PaymentSlot(this, this.payment, 0, 136, 110);
        this.addSlot(this.paymentSlot);
        this.addProperties(propertyDelegate);
        int j = 36;
        int k = 137;
        for (l = 0; l < 3; ++l) {
            for (int m = 0; m < 9; ++m) {
                this.addSlot(new Slot(inventory, m + l * 9 + 9, 36 + m * 18, 137 + l * 18));
            }
        }
        for (l = 0; l < 9; ++l) {
            this.addSlot(new Slot(inventory, l, 36 + l * 18, 195));
        }
    }

    @Override
    public void onClosed(PlayerEntity player) {
        super.onClosed(player);
        if (player.getWorld().isClient) {
            return;
        }
        ItemStack lv = this.paymentSlot.takeStack(this.paymentSlot.getMaxItemCount());
        if (!lv.isEmpty()) {
            player.dropItem(lv, false);
        }
    }

    @Override
    public boolean canUse(PlayerEntity player) {
        return BeaconScreenHandler.canUse(this.context, player, Blocks.BEACON);
    }

    @Override
    public void setProperty(int id, int value) {
        super.setProperty(id, value);
        this.sendContentUpdates();
    }

    @Override
    public ItemStack quickMove(PlayerEntity player, int slot) {
        ItemStack lv = ItemStack.EMPTY;
        Slot lv2 = (Slot)this.slots.get(slot);
        if (lv2 != null && lv2.hasStack()) {
            ItemStack lv3 = lv2.getStack();
            lv = lv3.copy();
            if (slot == 0) {
                if (!this.insertItem(lv3, 1, 37, true)) {
                    return ItemStack.EMPTY;
                }
                lv2.onQuickTransfer(lv3, lv);
            } else if (!this.paymentSlot.hasStack() && this.paymentSlot.canInsert(lv3) && lv3.getCount() == 1 ? !this.insertItem(lv3, 0, 1, false) : (slot >= 1 && slot < 28 ? !this.insertItem(lv3, 28, 37, false) : (slot >= 28 && slot < 37 ? !this.insertItem(lv3, 1, 28, false) : !this.insertItem(lv3, 1, 37, false)))) {
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

    public int getProperties() {
        return this.propertyDelegate.get(0);
    }

    public static int getRawIdForStatusEffect(@Nullable RegistryEntry<StatusEffect> effect) {
        return effect == null ? 0 : Registries.STATUS_EFFECT.getIndexedEntries().getRawId(effect) + 1;
    }

    @Nullable
    public static RegistryEntry<StatusEffect> getStatusEffectForRawId(int id) {
        return id == 0 ? null : Registries.STATUS_EFFECT.getIndexedEntries().get(id - 1);
    }

    @Nullable
    public RegistryEntry<StatusEffect> getPrimaryEffect() {
        return BeaconScreenHandler.getStatusEffectForRawId(this.propertyDelegate.get(1));
    }

    @Nullable
    public RegistryEntry<StatusEffect> getSecondaryEffect() {
        return BeaconScreenHandler.getStatusEffectForRawId(this.propertyDelegate.get(2));
    }

    public void setEffects(Optional<RegistryEntry<StatusEffect>> primary, Optional<RegistryEntry<StatusEffect>> secondary) {
        if (this.paymentSlot.hasStack()) {
            this.propertyDelegate.set(1, BeaconScreenHandler.getRawIdForStatusEffect(primary.orElse(null)));
            this.propertyDelegate.set(2, BeaconScreenHandler.getRawIdForStatusEffect(secondary.orElse(null)));
            this.paymentSlot.takeStack(1);
            this.context.run(World::markDirty);
        }
    }

    public boolean hasPayment() {
        return !this.payment.getStack(0).isEmpty();
    }

    class PaymentSlot
    extends Slot {
        public PaymentSlot(BeaconScreenHandler screenHandler, Inventory inventory, int index, int x, int y) {
            super(inventory, index, x, y);
        }

        @Override
        public boolean canInsert(ItemStack stack) {
            return stack.isIn(ItemTags.BEACON_PAYMENT_ITEMS);
        }

        @Override
        public int getMaxItemCount() {
            return 1;
        }
    }
}

