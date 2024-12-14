/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.screen;

import it.unimi.dsi.fastutil.objects.Object2IntMap;
import net.minecraft.block.Blocks;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.ItemEnchantmentsComponent;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.ExperienceOrbEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.CraftingResultInventory;
import net.minecraft.inventory.Inventory;
import net.minecraft.inventory.SimpleInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.registry.tag.EnchantmentTags;
import net.minecraft.screen.AnvilScreenHandler;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.ScreenHandlerContext;
import net.minecraft.screen.ScreenHandlerType;
import net.minecraft.screen.slot.Slot;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraft.world.WorldEvents;

public class GrindstoneScreenHandler
extends ScreenHandler {
    public static final int field_30793 = 35;
    public static final int INPUT_1_ID = 0;
    public static final int INPUT_2_ID = 1;
    public static final int OUTPUT_ID = 2;
    private static final int INVENTORY_START = 3;
    private static final int INVENTORY_END = 30;
    private static final int HOTBAR_START = 30;
    private static final int HOTBAR_END = 39;
    private final Inventory result = new CraftingResultInventory();
    final Inventory input = new SimpleInventory(2){

        @Override
        public void markDirty() {
            super.markDirty();
            GrindstoneScreenHandler.this.onContentChanged(this);
        }
    };
    private final ScreenHandlerContext context;

    public GrindstoneScreenHandler(int syncId, PlayerInventory playerInventory) {
        this(syncId, playerInventory, ScreenHandlerContext.EMPTY);
    }

    public GrindstoneScreenHandler(int syncId, PlayerInventory playerInventory, final ScreenHandlerContext context) {
        super(ScreenHandlerType.GRINDSTONE, syncId);
        int j;
        this.context = context;
        this.addSlot(new Slot(this, this.input, 0, 49, 19){

            @Override
            public boolean canInsert(ItemStack stack) {
                return stack.isDamageable() || EnchantmentHelper.hasEnchantments(stack);
            }
        });
        this.addSlot(new Slot(this, this.input, 1, 49, 40){

            @Override
            public boolean canInsert(ItemStack stack) {
                return stack.isDamageable() || EnchantmentHelper.hasEnchantments(stack);
            }
        });
        this.addSlot(new Slot(this.result, 2, 129, 34){

            @Override
            public boolean canInsert(ItemStack stack) {
                return false;
            }

            @Override
            public void onTakeItem(PlayerEntity player, ItemStack stack) {
                context.run((world, pos) -> {
                    if (world instanceof ServerWorld) {
                        ExperienceOrbEntity.spawn((ServerWorld)world, Vec3d.ofCenter(pos), this.getExperience((World)world));
                    }
                    world.syncWorldEvent(WorldEvents.GRINDSTONE_USED, (BlockPos)pos, 0);
                });
                GrindstoneScreenHandler.this.input.setStack(0, ItemStack.EMPTY);
                GrindstoneScreenHandler.this.input.setStack(1, ItemStack.EMPTY);
            }

            private int getExperience(World world) {
                int i = 0;
                i += this.getExperience(GrindstoneScreenHandler.this.input.getStack(0));
                if ((i += this.getExperience(GrindstoneScreenHandler.this.input.getStack(1))) > 0) {
                    int j = (int)Math.ceil((double)i / 2.0);
                    return j + world.random.nextInt(j);
                }
                return 0;
            }

            private int getExperience(ItemStack stack) {
                int i = 0;
                ItemEnchantmentsComponent lv = EnchantmentHelper.getEnchantments(stack);
                for (Object2IntMap.Entry<RegistryEntry<Enchantment>> entry : lv.getEnchantmentEntries()) {
                    RegistryEntry lv2 = (RegistryEntry)entry.getKey();
                    int j = entry.getIntValue();
                    if (lv2.isIn(EnchantmentTags.CURSE)) continue;
                    i += ((Enchantment)lv2.value()).getMinPower(j);
                }
                return i;
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
    }

    @Override
    public void onContentChanged(Inventory inventory) {
        super.onContentChanged(inventory);
        if (inventory == this.input) {
            this.updateResult();
        }
    }

    private void updateResult() {
        this.result.setStack(0, this.getOutputStack(this.input.getStack(0), this.input.getStack(1)));
        this.sendContentUpdates();
    }

    private ItemStack getOutputStack(ItemStack firstInput, ItemStack secondInput) {
        boolean bl2;
        boolean bl;
        boolean bl3 = bl = !firstInput.isEmpty() || !secondInput.isEmpty();
        if (!bl) {
            return ItemStack.EMPTY;
        }
        if (firstInput.getCount() > 1 || secondInput.getCount() > 1) {
            return ItemStack.EMPTY;
        }
        boolean bl4 = bl2 = !firstInput.isEmpty() && !secondInput.isEmpty();
        if (!bl2) {
            ItemStack lv;
            ItemStack itemStack = lv = !firstInput.isEmpty() ? firstInput : secondInput;
            if (!EnchantmentHelper.hasEnchantments(lv)) {
                return ItemStack.EMPTY;
            }
            return this.grind(lv.copy());
        }
        return this.combineItems(firstInput, secondInput);
    }

    private ItemStack combineItems(ItemStack firstInput, ItemStack secondInput) {
        ItemStack lv;
        if (!firstInput.isOf(secondInput.getItem())) {
            return ItemStack.EMPTY;
        }
        int i = Math.max(firstInput.getMaxDamage(), secondInput.getMaxDamage());
        int j = firstInput.getMaxDamage() - firstInput.getDamage();
        int k = secondInput.getMaxDamage() - secondInput.getDamage();
        int l = j + k + i * 5 / 100;
        int m = 1;
        if (!firstInput.isDamageable()) {
            if (firstInput.getMaxCount() < 2 || !ItemStack.areEqual(firstInput, secondInput)) {
                return ItemStack.EMPTY;
            }
            m = 2;
        }
        if ((lv = firstInput.copyWithCount(m)).isDamageable()) {
            lv.set(DataComponentTypes.MAX_DAMAGE, i);
            lv.setDamage(Math.max(i - l, 0));
        }
        this.transferEnchantments(lv, secondInput);
        return this.grind(lv);
    }

    private void transferEnchantments(ItemStack target, ItemStack source) {
        EnchantmentHelper.apply(target, components -> {
            ItemEnchantmentsComponent lv = EnchantmentHelper.getEnchantments(source);
            for (Object2IntMap.Entry<RegistryEntry<Enchantment>> entry : lv.getEnchantmentEntries()) {
                RegistryEntry lv2 = (RegistryEntry)entry.getKey();
                if (lv2.isIn(EnchantmentTags.CURSE) && components.getLevel(lv2) != 0) continue;
                components.add(lv2, entry.getIntValue());
            }
        });
    }

    private ItemStack grind(ItemStack item) {
        ItemEnchantmentsComponent lv = EnchantmentHelper.apply(item, components -> components.remove(enchantment -> !enchantment.isIn(EnchantmentTags.CURSE)));
        if (item.isOf(Items.ENCHANTED_BOOK) && lv.isEmpty()) {
            item = item.withItem(Items.BOOK);
        }
        int i = 0;
        for (int j = 0; j < lv.getSize(); ++j) {
            i = AnvilScreenHandler.getNextCost(i);
        }
        item.set(DataComponentTypes.REPAIR_COST, i);
        return item;
    }

    @Override
    public void onClosed(PlayerEntity player) {
        super.onClosed(player);
        this.context.run((world, pos) -> this.dropInventory(player, this.input));
    }

    @Override
    public boolean canUse(PlayerEntity player) {
        return GrindstoneScreenHandler.canUse(this.context, player, Blocks.GRINDSTONE);
    }

    @Override
    public ItemStack quickMove(PlayerEntity player, int slot) {
        ItemStack lv = ItemStack.EMPTY;
        Slot lv2 = (Slot)this.slots.get(slot);
        if (lv2 != null && lv2.hasStack()) {
            ItemStack lv3 = lv2.getStack();
            lv = lv3.copy();
            ItemStack lv4 = this.input.getStack(0);
            ItemStack lv5 = this.input.getStack(1);
            if (slot == 2) {
                if (!this.insertItem(lv3, 3, 39, true)) {
                    return ItemStack.EMPTY;
                }
                lv2.onQuickTransfer(lv3, lv);
            } else if (slot == 0 || slot == 1 ? !this.insertItem(lv3, 3, 39, false) : (lv4.isEmpty() || lv5.isEmpty() ? !this.insertItem(lv3, 0, 2, false) : (slot >= 3 && slot < 30 ? !this.insertItem(lv3, 30, 39, false) : slot >= 30 && slot < 39 && !this.insertItem(lv3, 3, 30, false)))) {
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
}

