/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.inventory;

import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.function.Supplier;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.LivingEntity;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;

public interface StackReference {
    public static final StackReference EMPTY = new StackReference(){

        @Override
        public ItemStack get() {
            return ItemStack.EMPTY;
        }

        @Override
        public boolean set(ItemStack stack) {
            return false;
        }
    };

    public static StackReference of(final Supplier<ItemStack> getter, final Consumer<ItemStack> setter) {
        return new StackReference(){

            @Override
            public ItemStack get() {
                return (ItemStack)getter.get();
            }

            @Override
            public boolean set(ItemStack stack) {
                setter.accept(stack);
                return true;
            }
        };
    }

    public static StackReference of(final Inventory inventory, final int index, final Predicate<ItemStack> stackFilter) {
        return new StackReference(){

            @Override
            public ItemStack get() {
                return inventory.getStack(index);
            }

            @Override
            public boolean set(ItemStack stack) {
                if (!stackFilter.test(stack)) {
                    return false;
                }
                inventory.setStack(index, stack);
                return true;
            }
        };
    }

    public static StackReference of(Inventory inventory, int index) {
        return StackReference.of(inventory, index, (ItemStack stack) -> true);
    }

    public static StackReference of(final LivingEntity entity, final EquipmentSlot slot, final Predicate<ItemStack> filter) {
        return new StackReference(){

            @Override
            public ItemStack get() {
                return entity.getEquippedStack(slot);
            }

            @Override
            public boolean set(ItemStack stack) {
                if (!filter.test(stack)) {
                    return false;
                }
                entity.equipStack(slot, stack);
                return true;
            }
        };
    }

    public static StackReference of(LivingEntity entity, EquipmentSlot slot) {
        return StackReference.of(entity, slot, (ItemStack stack) -> true);
    }

    public ItemStack get();

    public boolean set(ItemStack var1);
}

