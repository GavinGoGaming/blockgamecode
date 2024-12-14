/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 * 
 * Could not load the following classes:
 *  org.jetbrains.annotations.Nullable
 */
package net.minecraft.item;

import net.minecraft.component.EnchantmentEffectComponentTypes;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemConvertible;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.sound.SoundEvent;
import net.minecraft.sound.SoundEvents;
import net.minecraft.stat.Stats;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

public interface Equipment {
    public EquipmentSlot getSlotType();

    default public RegistryEntry<SoundEvent> getEquipSound() {
        return SoundEvents.ITEM_ARMOR_EQUIP_GENERIC;
    }

    default public TypedActionResult<ItemStack> equipAndSwap(Item item, World world, PlayerEntity user, Hand hand) {
        ItemStack lv = user.getStackInHand(hand);
        EquipmentSlot lv2 = user.getPreferredEquipmentSlot(lv);
        if (!user.canUseSlot(lv2)) {
            return TypedActionResult.pass(lv);
        }
        ItemStack lv3 = user.getEquippedStack(lv2);
        if (EnchantmentHelper.hasAnyEnchantmentsWith(lv3, EnchantmentEffectComponentTypes.PREVENT_ARMOR_CHANGE) && !user.isCreative() || ItemStack.areEqual(lv, lv3)) {
            return TypedActionResult.fail(lv);
        }
        if (!world.isClient()) {
            user.incrementStat(Stats.USED.getOrCreateStat(item));
        }
        ItemStack lv4 = lv3.isEmpty() ? lv : lv3.copyAndEmpty();
        ItemStack lv5 = user.isCreative() ? lv.copy() : lv.copyAndEmpty();
        user.equipStack(lv2, lv5);
        return TypedActionResult.success(lv4, world.isClient());
    }

    @Nullable
    public static Equipment fromStack(ItemStack stack) {
        BlockItem lv2;
        Item item = stack.getItem();
        if (item instanceof Equipment) {
            Equipment lv = (Equipment)((Object)item);
            return lv;
        }
        ItemConvertible itemConvertible = stack.getItem();
        if (itemConvertible instanceof BlockItem && (itemConvertible = (lv2 = (BlockItem)itemConvertible).getBlock()) instanceof Equipment) {
            Equipment lv3 = (Equipment)((Object)itemConvertible);
            return lv3;
        }
        return null;
    }
}

