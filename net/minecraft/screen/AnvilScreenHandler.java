/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 * 
 * Could not load the following classes:
 *  org.jetbrains.annotations.Nullable
 */
package net.minecraft.screen;

import com.mojang.logging.LogUtils;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import net.minecraft.block.AnvilBlock;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.ItemEnchantmentsComponent;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.registry.tag.BlockTags;
import net.minecraft.screen.ForgingScreenHandler;
import net.minecraft.screen.Property;
import net.minecraft.screen.ScreenHandlerContext;
import net.minecraft.screen.ScreenHandlerType;
import net.minecraft.screen.slot.ForgingSlotsManager;
import net.minecraft.text.Text;
import net.minecraft.util.StringHelper;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.WorldEvents;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;

public class AnvilScreenHandler
extends ForgingScreenHandler {
    public static final int INPUT_1_ID = 0;
    public static final int INPUT_2_ID = 1;
    public static final int OUTPUT_ID = 2;
    private static final Logger LOGGER = LogUtils.getLogger();
    private static final boolean field_30752 = false;
    public static final int MAX_NAME_LENGTH = 50;
    private int repairItemUsage;
    @Nullable
    private String newItemName;
    private final Property levelCost = Property.create();
    private static final int field_30753 = 0;
    private static final int field_30754 = 1;
    private static final int field_30755 = 1;
    private static final int field_30747 = 1;
    private static final int field_30748 = 2;
    private static final int field_30749 = 1;
    private static final int field_30750 = 1;
    private static final int INPUT_1_X = 27;
    private static final int INPUT_2_X = 76;
    private static final int OUTPUT_X = 134;
    private static final int SLOT_Y = 47;

    public AnvilScreenHandler(int syncId, PlayerInventory inventory) {
        this(syncId, inventory, ScreenHandlerContext.EMPTY);
    }

    public AnvilScreenHandler(int syncId, PlayerInventory inventory, ScreenHandlerContext context) {
        super(ScreenHandlerType.ANVIL, syncId, inventory, context);
        this.addProperty(this.levelCost);
    }

    @Override
    protected ForgingSlotsManager getForgingSlotsManager() {
        return ForgingSlotsManager.create().input(0, 27, 47, stack -> true).input(1, 76, 47, stack -> true).output(2, 134, 47).build();
    }

    @Override
    protected boolean canUse(BlockState state) {
        return state.isIn(BlockTags.ANVIL);
    }

    @Override
    protected boolean canTakeOutput(PlayerEntity player, boolean present) {
        return (player.isInCreativeMode() || player.experienceLevel >= this.levelCost.get()) && this.levelCost.get() > 0;
    }

    @Override
    protected void onTakeOutput(PlayerEntity player, ItemStack stack) {
        if (!player.getAbilities().creativeMode) {
            player.addExperienceLevels(-this.levelCost.get());
        }
        this.input.setStack(0, ItemStack.EMPTY);
        if (this.repairItemUsage > 0) {
            ItemStack lv = this.input.getStack(1);
            if (!lv.isEmpty() && lv.getCount() > this.repairItemUsage) {
                lv.decrement(this.repairItemUsage);
                this.input.setStack(1, lv);
            } else {
                this.input.setStack(1, ItemStack.EMPTY);
            }
        } else {
            this.input.setStack(1, ItemStack.EMPTY);
        }
        this.levelCost.set(0);
        this.context.run((world, pos) -> {
            BlockState lv = world.getBlockState((BlockPos)pos);
            if (!player.isInCreativeMode() && lv.isIn(BlockTags.ANVIL) && player.getRandom().nextFloat() < 0.12f) {
                BlockState lv2 = AnvilBlock.getLandingState(lv);
                if (lv2 == null) {
                    world.removeBlock((BlockPos)pos, false);
                    world.syncWorldEvent(WorldEvents.ANVIL_DESTROYED, (BlockPos)pos, 0);
                } else {
                    world.setBlockState((BlockPos)pos, lv2, Block.NOTIFY_LISTENERS);
                    world.syncWorldEvent(WorldEvents.ANVIL_USED, (BlockPos)pos, 0);
                }
            } else {
                world.syncWorldEvent(WorldEvents.ANVIL_USED, (BlockPos)pos, 0);
            }
        });
    }

    @Override
    public void updateResult() {
        int k;
        ItemStack lv = this.input.getStack(0);
        this.levelCost.set(1);
        int i = 0;
        long l = 0L;
        int j = 0;
        if (lv.isEmpty() || !EnchantmentHelper.canHaveEnchantments(lv)) {
            this.output.setStack(0, ItemStack.EMPTY);
            this.levelCost.set(0);
            return;
        }
        ItemStack lv2 = lv.copy();
        ItemStack lv3 = this.input.getStack(1);
        ItemEnchantmentsComponent.Builder lv4 = new ItemEnchantmentsComponent.Builder(EnchantmentHelper.getEnchantments(lv2));
        l += (long)lv.getOrDefault(DataComponentTypes.REPAIR_COST, 0).intValue() + (long)lv3.getOrDefault(DataComponentTypes.REPAIR_COST, 0).intValue();
        this.repairItemUsage = 0;
        if (!lv3.isEmpty()) {
            boolean bl = lv3.contains(DataComponentTypes.STORED_ENCHANTMENTS);
            if (lv2.isDamageable() && lv2.getItem().canRepair(lv, lv3)) {
                int m;
                k = Math.min(lv2.getDamage(), lv2.getMaxDamage() / 4);
                if (k <= 0) {
                    this.output.setStack(0, ItemStack.EMPTY);
                    this.levelCost.set(0);
                    return;
                }
                for (m = 0; k > 0 && m < lv3.getCount(); ++m) {
                    int n = lv2.getDamage() - k;
                    lv2.setDamage(n);
                    ++i;
                    k = Math.min(lv2.getDamage(), lv2.getMaxDamage() / 4);
                }
                this.repairItemUsage = m;
            } else {
                if (!(bl || lv2.isOf(lv3.getItem()) && lv2.isDamageable())) {
                    this.output.setStack(0, ItemStack.EMPTY);
                    this.levelCost.set(0);
                    return;
                }
                if (lv2.isDamageable() && !bl) {
                    int k2 = lv.getMaxDamage() - lv.getDamage();
                    int m = lv3.getMaxDamage() - lv3.getDamage();
                    int n = m + lv2.getMaxDamage() * 12 / 100;
                    int o = k2 + n;
                    int p = lv2.getMaxDamage() - o;
                    if (p < 0) {
                        p = 0;
                    }
                    if (p < lv2.getDamage()) {
                        lv2.setDamage(p);
                        i += 2;
                    }
                }
                ItemEnchantmentsComponent lv5 = EnchantmentHelper.getEnchantments(lv3);
                boolean bl2 = false;
                boolean bl3 = false;
                for (Object2IntMap.Entry<RegistryEntry<Enchantment>> entry : lv5.getEnchantmentEntries()) {
                    int r;
                    RegistryEntry lv6 = (RegistryEntry)entry.getKey();
                    int q = lv4.getLevel(lv6);
                    r = q == (r = entry.getIntValue()) ? r + 1 : Math.max(r, q);
                    Enchantment lv7 = (Enchantment)lv6.value();
                    boolean bl4 = lv7.isAcceptableItem(lv);
                    if (this.player.getAbilities().creativeMode || lv.isOf(Items.ENCHANTED_BOOK)) {
                        bl4 = true;
                    }
                    for (RegistryEntry<Enchantment> lv8 : lv4.getEnchantments()) {
                        if (lv8.equals(lv6) || Enchantment.canBeCombined(lv6, lv8)) continue;
                        bl4 = false;
                        ++i;
                    }
                    if (!bl4) {
                        bl3 = true;
                        continue;
                    }
                    bl2 = true;
                    if (r > lv7.getMaxLevel()) {
                        r = lv7.getMaxLevel();
                    }
                    lv4.set(lv6, r);
                    int s = lv7.getAnvilCost();
                    if (bl) {
                        s = Math.max(1, s / 2);
                    }
                    i += s * r;
                    if (lv.getCount() <= 1) continue;
                    i = 40;
                }
                if (bl3 && !bl2) {
                    this.output.setStack(0, ItemStack.EMPTY);
                    this.levelCost.set(0);
                    return;
                }
            }
        }
        if (this.newItemName == null || StringHelper.isBlank(this.newItemName)) {
            if (lv.contains(DataComponentTypes.CUSTOM_NAME)) {
                j = 1;
                i += j;
                lv2.remove(DataComponentTypes.CUSTOM_NAME);
            }
        } else if (!this.newItemName.equals(lv.getName().getString())) {
            j = 1;
            i += j;
            lv2.set(DataComponentTypes.CUSTOM_NAME, Text.literal(this.newItemName));
        }
        int t = (int)MathHelper.clamp(l + (long)i, 0L, Integer.MAX_VALUE);
        this.levelCost.set(t);
        if (i <= 0) {
            lv2 = ItemStack.EMPTY;
        }
        if (j == i && j > 0 && this.levelCost.get() >= 40) {
            this.levelCost.set(39);
        }
        if (this.levelCost.get() >= 40 && !this.player.getAbilities().creativeMode) {
            lv2 = ItemStack.EMPTY;
        }
        if (!lv2.isEmpty()) {
            k = lv2.getOrDefault(DataComponentTypes.REPAIR_COST, 0);
            if (k < lv3.getOrDefault(DataComponentTypes.REPAIR_COST, 0)) {
                k = lv3.getOrDefault(DataComponentTypes.REPAIR_COST, 0);
            }
            if (j != i || j == 0) {
                k = AnvilScreenHandler.getNextCost(k);
            }
            lv2.set(DataComponentTypes.REPAIR_COST, k);
            EnchantmentHelper.set(lv2, lv4.build());
        }
        this.output.setStack(0, lv2);
        this.sendContentUpdates();
    }

    public static int getNextCost(int cost) {
        return (int)Math.min((long)cost * 2L + 1L, Integer.MAX_VALUE);
    }

    public boolean setNewItemName(String newItemName) {
        String string2 = AnvilScreenHandler.sanitize(newItemName);
        if (string2 == null || string2.equals(this.newItemName)) {
            return false;
        }
        this.newItemName = string2;
        if (this.getSlot(2).hasStack()) {
            ItemStack lv = this.getSlot(2).getStack();
            if (StringHelper.isBlank(string2)) {
                lv.remove(DataComponentTypes.CUSTOM_NAME);
            } else {
                lv.set(DataComponentTypes.CUSTOM_NAME, Text.literal(string2));
            }
        }
        this.updateResult();
        return true;
    }

    @Nullable
    private static String sanitize(String name) {
        String string2 = StringHelper.stripInvalidChars(name);
        if (string2.length() <= 50) {
            return string2;
        }
        return null;
    }

    public int getLevelCost() {
        return this.levelCost.get();
    }
}

