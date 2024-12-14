/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.item;

import java.util.List;
import java.util.Optional;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.BundleContentsComponent;
import net.minecraft.entity.Entity;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.StackReference;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUsage;
import net.minecraft.item.tooltip.BundleTooltipData;
import net.minecraft.item.tooltip.TooltipData;
import net.minecraft.item.tooltip.TooltipType;
import net.minecraft.screen.slot.Slot;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.sound.SoundEvents;
import net.minecraft.stat.Stats;
import net.minecraft.text.Text;
import net.minecraft.util.ClickType;
import net.minecraft.util.Formatting;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;
import org.apache.commons.lang3.math.Fraction;

public class BundleItem
extends Item {
    private static final int ITEM_BAR_COLOR = MathHelper.packRgb(0.4f, 0.4f, 1.0f);
    private static final int field_51352 = 64;

    public BundleItem(Item.Settings arg) {
        super(arg);
    }

    public static float getAmountFilled(ItemStack stack) {
        BundleContentsComponent lv = stack.getOrDefault(DataComponentTypes.BUNDLE_CONTENTS, BundleContentsComponent.DEFAULT);
        return lv.getOccupancy().floatValue();
    }

    @Override
    public boolean onStackClicked(ItemStack stack, Slot slot, ClickType clickType, PlayerEntity player) {
        int i;
        if (clickType != ClickType.RIGHT) {
            return false;
        }
        BundleContentsComponent lv = stack.get(DataComponentTypes.BUNDLE_CONTENTS);
        if (lv == null) {
            return false;
        }
        ItemStack lv2 = slot.getStack();
        BundleContentsComponent.Builder lv3 = new BundleContentsComponent.Builder(lv);
        if (lv2.isEmpty()) {
            this.playRemoveOneSound(player);
            ItemStack lv4 = lv3.removeFirst();
            if (lv4 != null) {
                ItemStack lv5 = slot.insertStack(lv4);
                lv3.add(lv5);
            }
        } else if (lv2.getItem().canBeNested() && (i = lv3.add(slot, player)) > 0) {
            this.playInsertSound(player);
        }
        stack.set(DataComponentTypes.BUNDLE_CONTENTS, lv3.build());
        return true;
    }

    @Override
    public boolean onClicked(ItemStack stack, ItemStack otherStack, Slot slot, ClickType clickType, PlayerEntity player, StackReference cursorStackReference) {
        if (clickType != ClickType.RIGHT || !slot.canTakePartial(player)) {
            return false;
        }
        BundleContentsComponent lv = stack.get(DataComponentTypes.BUNDLE_CONTENTS);
        if (lv == null) {
            return false;
        }
        BundleContentsComponent.Builder lv2 = new BundleContentsComponent.Builder(lv);
        if (otherStack.isEmpty()) {
            ItemStack lv3 = lv2.removeFirst();
            if (lv3 != null) {
                this.playRemoveOneSound(player);
                cursorStackReference.set(lv3);
            }
        } else {
            int i = lv2.add(otherStack);
            if (i > 0) {
                this.playInsertSound(player);
            }
        }
        stack.set(DataComponentTypes.BUNDLE_CONTENTS, lv2.build());
        return true;
    }

    @Override
    public TypedActionResult<ItemStack> use(World world, PlayerEntity user, Hand hand) {
        ItemStack lv = user.getStackInHand(hand);
        if (BundleItem.dropAllBundledItems(lv, user)) {
            this.playDropContentsSound(user);
            user.incrementStat(Stats.USED.getOrCreateStat(this));
            return TypedActionResult.success(lv, world.isClient());
        }
        return TypedActionResult.fail(lv);
    }

    @Override
    public boolean isItemBarVisible(ItemStack stack) {
        BundleContentsComponent lv = stack.getOrDefault(DataComponentTypes.BUNDLE_CONTENTS, BundleContentsComponent.DEFAULT);
        return lv.getOccupancy().compareTo(Fraction.ZERO) > 0;
    }

    @Override
    public int getItemBarStep(ItemStack stack) {
        BundleContentsComponent lv = stack.getOrDefault(DataComponentTypes.BUNDLE_CONTENTS, BundleContentsComponent.DEFAULT);
        return Math.min(1 + MathHelper.multiplyFraction(lv.getOccupancy(), 12), 13);
    }

    @Override
    public int getItemBarColor(ItemStack stack) {
        return ITEM_BAR_COLOR;
    }

    private static boolean dropAllBundledItems(ItemStack stack2, PlayerEntity player) {
        BundleContentsComponent lv = stack2.get(DataComponentTypes.BUNDLE_CONTENTS);
        if (lv == null || lv.isEmpty()) {
            return false;
        }
        stack2.set(DataComponentTypes.BUNDLE_CONTENTS, BundleContentsComponent.DEFAULT);
        if (player instanceof ServerPlayerEntity) {
            lv.iterateCopy().forEach(stack -> player.dropItem((ItemStack)stack, true));
        }
        return true;
    }

    @Override
    public Optional<TooltipData> getTooltipData(ItemStack stack) {
        if (stack.contains(DataComponentTypes.HIDE_TOOLTIP) || stack.contains(DataComponentTypes.HIDE_ADDITIONAL_TOOLTIP)) {
            return Optional.empty();
        }
        return Optional.ofNullable(stack.get(DataComponentTypes.BUNDLE_CONTENTS)).map(BundleTooltipData::new);
    }

    @Override
    public void appendTooltip(ItemStack stack, Item.TooltipContext context, List<Text> tooltip, TooltipType type) {
        BundleContentsComponent lv = stack.get(DataComponentTypes.BUNDLE_CONTENTS);
        if (lv != null) {
            int i = MathHelper.multiplyFraction(lv.getOccupancy(), 64);
            tooltip.add(Text.translatable("item.minecraft.bundle.fullness", i, 64).formatted(Formatting.GRAY));
        }
    }

    @Override
    public void onItemEntityDestroyed(ItemEntity entity) {
        BundleContentsComponent lv = entity.getStack().get(DataComponentTypes.BUNDLE_CONTENTS);
        if (lv == null) {
            return;
        }
        entity.getStack().set(DataComponentTypes.BUNDLE_CONTENTS, BundleContentsComponent.DEFAULT);
        ItemUsage.spawnItemContents(entity, lv.iterateCopy());
    }

    private void playRemoveOneSound(Entity entity) {
        entity.playSound(SoundEvents.ITEM_BUNDLE_REMOVE_ONE, 0.8f, 0.8f + entity.getWorld().getRandom().nextFloat() * 0.4f);
    }

    private void playInsertSound(Entity entity) {
        entity.playSound(SoundEvents.ITEM_BUNDLE_INSERT, 0.8f, 0.8f + entity.getWorld().getRandom().nextFloat() * 0.4f);
    }

    private void playDropContentsSound(Entity entity) {
        entity.playSound(SoundEvents.ITEM_BUNDLE_DROP_CONTENTS, 0.8f, 0.8f + entity.getWorld().getRandom().nextFloat() * 0.4f);
    }
}

