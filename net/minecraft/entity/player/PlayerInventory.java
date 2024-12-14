/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.entity.player;

import com.google.common.collect.ImmutableList;
import java.util.List;
import java.util.function.Predicate;
import net.minecraft.block.BlockState;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.Inventories;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtList;
import net.minecraft.network.packet.s2c.play.ScreenHandlerSlotUpdateS2CPacket;
import net.minecraft.recipe.RecipeMatcher;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Nameable;
import net.minecraft.util.collection.DefaultedList;
import net.minecraft.util.crash.CrashException;
import net.minecraft.util.crash.CrashReport;
import net.minecraft.util.crash.CrashReportSection;

public class PlayerInventory
implements Inventory,
Nameable {
    public static final int ITEM_USAGE_COOLDOWN = 5;
    public static final int MAIN_SIZE = 36;
    private static final int HOTBAR_SIZE = 9;
    public static final int OFF_HAND_SLOT = 40;
    public static final int NOT_FOUND = -1;
    public static final int[] ARMOR_SLOTS = new int[]{0, 1, 2, 3};
    public static final int[] HELMET_SLOTS = new int[]{3};
    public final DefaultedList<ItemStack> main = DefaultedList.ofSize(36, ItemStack.EMPTY);
    public final DefaultedList<ItemStack> armor = DefaultedList.ofSize(4, ItemStack.EMPTY);
    public final DefaultedList<ItemStack> offHand = DefaultedList.ofSize(1, ItemStack.EMPTY);
    private final List<DefaultedList<ItemStack>> combinedInventory = ImmutableList.of(this.main, this.armor, this.offHand);
    public int selectedSlot;
    public final PlayerEntity player;
    private int changeCount;

    public PlayerInventory(PlayerEntity player) {
        this.player = player;
    }

    public ItemStack getMainHandStack() {
        if (PlayerInventory.isValidHotbarIndex(this.selectedSlot)) {
            return this.main.get(this.selectedSlot);
        }
        return ItemStack.EMPTY;
    }

    public static int getHotbarSize() {
        return 9;
    }

    private boolean canStackAddMore(ItemStack existingStack, ItemStack stack) {
        return !existingStack.isEmpty() && ItemStack.areItemsAndComponentsEqual(existingStack, stack) && existingStack.isStackable() && existingStack.getCount() < this.getMaxCount(existingStack);
    }

    public int getEmptySlot() {
        for (int i = 0; i < this.main.size(); ++i) {
            if (!this.main.get(i).isEmpty()) continue;
            return i;
        }
        return -1;
    }

    public void addPickBlock(ItemStack stack) {
        int i = this.getSlotWithStack(stack);
        if (PlayerInventory.isValidHotbarIndex(i)) {
            this.selectedSlot = i;
            return;
        }
        if (i == -1) {
            int j;
            this.selectedSlot = this.getSwappableHotbarSlot();
            if (!this.main.get(this.selectedSlot).isEmpty() && (j = this.getEmptySlot()) != -1) {
                this.main.set(j, this.main.get(this.selectedSlot));
            }
            this.main.set(this.selectedSlot, stack);
        } else {
            this.swapSlotWithHotbar(i);
        }
    }

    public void swapSlotWithHotbar(int slot) {
        this.selectedSlot = this.getSwappableHotbarSlot();
        ItemStack lv = this.main.get(this.selectedSlot);
        this.main.set(this.selectedSlot, this.main.get(slot));
        this.main.set(slot, lv);
    }

    public static boolean isValidHotbarIndex(int slot) {
        return slot >= 0 && slot < 9;
    }

    public int getSlotWithStack(ItemStack stack) {
        for (int i = 0; i < this.main.size(); ++i) {
            if (this.main.get(i).isEmpty() || !ItemStack.areItemsAndComponentsEqual(stack, this.main.get(i))) continue;
            return i;
        }
        return -1;
    }

    public int indexOf(ItemStack stack) {
        for (int i = 0; i < this.main.size(); ++i) {
            ItemStack lv = this.main.get(i);
            if (lv.isEmpty() || !ItemStack.areItemsAndComponentsEqual(stack, lv) || lv.isDamaged() || lv.hasEnchantments() || lv.contains(DataComponentTypes.CUSTOM_NAME)) continue;
            return i;
        }
        return -1;
    }

    public int getSwappableHotbarSlot() {
        int j;
        int i;
        for (i = 0; i < 9; ++i) {
            j = (this.selectedSlot + i) % 9;
            if (!this.main.get(j).isEmpty()) continue;
            return j;
        }
        for (i = 0; i < 9; ++i) {
            j = (this.selectedSlot + i) % 9;
            if (this.main.get(j).hasEnchantments()) continue;
            return j;
        }
        return this.selectedSlot;
    }

    public void scrollInHotbar(double scrollAmount) {
        int i = (int)Math.signum(scrollAmount);
        this.selectedSlot -= i;
        while (this.selectedSlot < 0) {
            this.selectedSlot += 9;
        }
        while (this.selectedSlot >= 9) {
            this.selectedSlot -= 9;
        }
    }

    public int remove(Predicate<ItemStack> shouldRemove, int maxCount, Inventory craftingInventory) {
        int j = 0;
        boolean bl = maxCount == 0;
        j += Inventories.remove(this, shouldRemove, maxCount - j, bl);
        j += Inventories.remove(craftingInventory, shouldRemove, maxCount - j, bl);
        ItemStack lv = this.player.currentScreenHandler.getCursorStack();
        j += Inventories.remove(lv, shouldRemove, maxCount - j, bl);
        if (lv.isEmpty()) {
            this.player.currentScreenHandler.setCursorStack(ItemStack.EMPTY);
        }
        return j;
    }

    private int addStack(ItemStack stack) {
        int i = this.getOccupiedSlotWithRoomForStack(stack);
        if (i == -1) {
            i = this.getEmptySlot();
        }
        if (i == -1) {
            return stack.getCount();
        }
        return this.addStack(i, stack);
    }

    private int addStack(int slot, ItemStack stack) {
        int k;
        int l;
        int j = stack.getCount();
        ItemStack lv = this.getStack(slot);
        if (lv.isEmpty()) {
            lv = stack.copyWithCount(0);
            this.setStack(slot, lv);
        }
        if ((l = Math.min(j, k = this.getMaxCount(lv) - lv.getCount())) == 0) {
            return j;
        }
        lv.increment(l);
        lv.setBobbingAnimationTime(5);
        return j -= l;
    }

    public int getOccupiedSlotWithRoomForStack(ItemStack stack) {
        if (this.canStackAddMore(this.getStack(this.selectedSlot), stack)) {
            return this.selectedSlot;
        }
        if (this.canStackAddMore(this.getStack(40), stack)) {
            return 40;
        }
        for (int i = 0; i < this.main.size(); ++i) {
            if (!this.canStackAddMore(this.main.get(i), stack)) continue;
            return i;
        }
        return -1;
    }

    public void updateItems() {
        for (DefaultedList<ItemStack> lv : this.combinedInventory) {
            for (int i = 0; i < lv.size(); ++i) {
                if (lv.get(i).isEmpty()) continue;
                lv.get(i).inventoryTick(this.player.getWorld(), this.player, i, this.selectedSlot == i);
            }
        }
    }

    public boolean insertStack(ItemStack stack) {
        return this.insertStack(-1, stack);
    }

    public boolean insertStack(int slot, ItemStack stack) {
        if (stack.isEmpty()) {
            return false;
        }
        try {
            if (!stack.isDamaged()) {
                int j;
                do {
                    j = stack.getCount();
                    if (slot == -1) {
                        stack.setCount(this.addStack(stack));
                        continue;
                    }
                    stack.setCount(this.addStack(slot, stack));
                } while (!stack.isEmpty() && stack.getCount() < j);
                if (stack.getCount() == j && this.player.isInCreativeMode()) {
                    stack.setCount(0);
                    return true;
                }
                return stack.getCount() < j;
            }
            if (slot == -1) {
                slot = this.getEmptySlot();
            }
            if (slot >= 0) {
                this.main.set(slot, stack.copyAndEmpty());
                this.main.get(slot).setBobbingAnimationTime(5);
                return true;
            }
            if (this.player.isInCreativeMode()) {
                stack.setCount(0);
                return true;
            }
            return false;
        } catch (Throwable throwable) {
            CrashReport lv = CrashReport.create(throwable, "Adding item to inventory");
            CrashReportSection lv2 = lv.addElement("Item being added");
            lv2.add("Item ID", Item.getRawId(stack.getItem()));
            lv2.add("Item data", stack.getDamage());
            lv2.add("Item name", () -> stack.getName().getString());
            throw new CrashException(lv);
        }
    }

    public void offerOrDrop(ItemStack stack) {
        this.offer(stack, true);
    }

    public void offer(ItemStack stack, boolean notifiesClient) {
        while (!stack.isEmpty()) {
            int i = this.getOccupiedSlotWithRoomForStack(stack);
            if (i == -1) {
                i = this.getEmptySlot();
            }
            if (i == -1) {
                this.player.dropItem(stack, false);
                break;
            }
            int j = stack.getMaxCount() - this.getStack(i).getCount();
            if (!this.insertStack(i, stack.split(j)) || !notifiesClient || !(this.player instanceof ServerPlayerEntity)) continue;
            ((ServerPlayerEntity)this.player).networkHandler.sendPacket(new ScreenHandlerSlotUpdateS2CPacket(-2, 0, i, this.getStack(i)));
        }
    }

    @Override
    public ItemStack removeStack(int slot, int amount) {
        DefaultedList<ItemStack> list = null;
        for (DefaultedList<ItemStack> lv : this.combinedInventory) {
            if (slot < lv.size()) {
                list = lv;
                break;
            }
            slot -= lv.size();
        }
        if (list != null && !((ItemStack)list.get(slot)).isEmpty()) {
            return Inventories.splitStack(list, slot, amount);
        }
        return ItemStack.EMPTY;
    }

    public void removeOne(ItemStack stack) {
        block0: for (DefaultedList<ItemStack> lv : this.combinedInventory) {
            for (int i = 0; i < lv.size(); ++i) {
                if (lv.get(i) != stack) continue;
                lv.set(i, ItemStack.EMPTY);
                continue block0;
            }
        }
    }

    @Override
    public ItemStack removeStack(int slot) {
        DefaultedList<ItemStack> lv = null;
        for (DefaultedList<ItemStack> lv2 : this.combinedInventory) {
            if (slot < lv2.size()) {
                lv = lv2;
                break;
            }
            slot -= lv2.size();
        }
        if (lv != null && !((ItemStack)lv.get(slot)).isEmpty()) {
            ItemStack lv3 = lv.get(slot);
            lv.set(slot, ItemStack.EMPTY);
            return lv3;
        }
        return ItemStack.EMPTY;
    }

    @Override
    public void setStack(int slot, ItemStack stack) {
        DefaultedList<ItemStack> lv = null;
        for (DefaultedList<ItemStack> lv2 : this.combinedInventory) {
            if (slot < lv2.size()) {
                lv = lv2;
                break;
            }
            slot -= lv2.size();
        }
        if (lv != null) {
            lv.set(slot, stack);
        }
    }

    public float getBlockBreakingSpeed(BlockState block) {
        return this.main.get(this.selectedSlot).getMiningSpeedMultiplier(block);
    }

    public NbtList writeNbt(NbtList nbtList) {
        NbtCompound lv;
        int i;
        for (i = 0; i < this.main.size(); ++i) {
            if (this.main.get(i).isEmpty()) continue;
            lv = new NbtCompound();
            lv.putByte("Slot", (byte)i);
            nbtList.add(this.main.get(i).encode(this.player.getRegistryManager(), lv));
        }
        for (i = 0; i < this.armor.size(); ++i) {
            if (this.armor.get(i).isEmpty()) continue;
            lv = new NbtCompound();
            lv.putByte("Slot", (byte)(i + 100));
            nbtList.add(this.armor.get(i).encode(this.player.getRegistryManager(), lv));
        }
        for (i = 0; i < this.offHand.size(); ++i) {
            if (this.offHand.get(i).isEmpty()) continue;
            lv = new NbtCompound();
            lv.putByte("Slot", (byte)(i + 150));
            nbtList.add(this.offHand.get(i).encode(this.player.getRegistryManager(), lv));
        }
        return nbtList;
    }

    public void readNbt(NbtList nbtList) {
        this.main.clear();
        this.armor.clear();
        this.offHand.clear();
        for (int i = 0; i < nbtList.size(); ++i) {
            NbtCompound lv = nbtList.getCompound(i);
            int j = lv.getByte("Slot") & 0xFF;
            ItemStack lv2 = ItemStack.fromNbt(this.player.getRegistryManager(), lv).orElse(ItemStack.EMPTY);
            if (j >= 0 && j < this.main.size()) {
                this.main.set(j, lv2);
                continue;
            }
            if (j >= 100 && j < this.armor.size() + 100) {
                this.armor.set(j - 100, lv2);
                continue;
            }
            if (j < 150 || j >= this.offHand.size() + 150) continue;
            this.offHand.set(j - 150, lv2);
        }
    }

    @Override
    public int size() {
        return this.main.size() + this.armor.size() + this.offHand.size();
    }

    @Override
    public boolean isEmpty() {
        for (ItemStack lv : this.main) {
            if (lv.isEmpty()) continue;
            return false;
        }
        for (ItemStack lv : this.armor) {
            if (lv.isEmpty()) continue;
            return false;
        }
        for (ItemStack lv : this.offHand) {
            if (lv.isEmpty()) continue;
            return false;
        }
        return true;
    }

    @Override
    public ItemStack getStack(int slot) {
        DefaultedList<ItemStack> list = null;
        for (DefaultedList<ItemStack> lv : this.combinedInventory) {
            if (slot < lv.size()) {
                list = lv;
                break;
            }
            slot -= lv.size();
        }
        return list == null ? ItemStack.EMPTY : (ItemStack)list.get(slot);
    }

    @Override
    public Text getName() {
        return Text.translatable("container.inventory");
    }

    public ItemStack getArmorStack(int slot) {
        return this.armor.get(slot);
    }

    public void dropAll() {
        for (List list : this.combinedInventory) {
            for (int i = 0; i < list.size(); ++i) {
                ItemStack lv = (ItemStack)list.get(i);
                if (lv.isEmpty()) continue;
                this.player.dropItem(lv, true, false);
                list.set(i, ItemStack.EMPTY);
            }
        }
    }

    @Override
    public void markDirty() {
        ++this.changeCount;
    }

    public int getChangeCount() {
        return this.changeCount;
    }

    @Override
    public boolean canPlayerUse(PlayerEntity player) {
        return player.canInteractWithEntity(this.player, 4.0);
    }

    public boolean contains(ItemStack stack) {
        for (List list : this.combinedInventory) {
            for (ItemStack lv : list) {
                if (lv.isEmpty() || !ItemStack.areItemsAndComponentsEqual(lv, stack)) continue;
                return true;
            }
        }
        return false;
    }

    public boolean contains(TagKey<Item> tag) {
        for (List list : this.combinedInventory) {
            for (ItemStack lv : list) {
                if (lv.isEmpty() || !lv.isIn(tag)) continue;
                return true;
            }
        }
        return false;
    }

    public boolean contains(Predicate<ItemStack> predicate) {
        for (List list : this.combinedInventory) {
            for (ItemStack lv : list) {
                if (!predicate.test(lv)) continue;
                return true;
            }
        }
        return false;
    }

    public void clone(PlayerInventory other) {
        for (int i = 0; i < this.size(); ++i) {
            this.setStack(i, other.getStack(i));
        }
        this.selectedSlot = other.selectedSlot;
    }

    @Override
    public void clear() {
        for (List list : this.combinedInventory) {
            list.clear();
        }
    }

    public void populateRecipeFinder(RecipeMatcher finder) {
        for (ItemStack lv : this.main) {
            finder.addUnenchantedInput(lv);
        }
    }

    public ItemStack dropSelectedItem(boolean entireStack) {
        ItemStack lv = this.getMainHandStack();
        if (lv.isEmpty()) {
            return ItemStack.EMPTY;
        }
        return this.removeStack(this.selectedSlot, entireStack ? lv.getCount() : 1);
    }
}

