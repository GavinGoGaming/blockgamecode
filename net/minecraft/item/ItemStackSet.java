/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 * 
 * Could not load the following classes:
 *  org.jetbrains.annotations.Nullable
 */
package net.minecraft.item;

import it.unimi.dsi.fastutil.Hash;
import it.unimi.dsi.fastutil.objects.ObjectLinkedOpenCustomHashSet;
import java.util.Set;
import net.minecraft.item.ItemStack;
import org.jetbrains.annotations.Nullable;

public class ItemStackSet {
    private static final Hash.Strategy<? super ItemStack> HASH_STRATEGY = new Hash.Strategy<ItemStack>(){

        @Override
        public int hashCode(@Nullable ItemStack arg) {
            return ItemStack.hashCode(arg);
        }

        @Override
        public boolean equals(@Nullable ItemStack arg, @Nullable ItemStack arg2) {
            return arg == arg2 || arg != null && arg2 != null && arg.isEmpty() == arg2.isEmpty() && ItemStack.areItemsAndComponentsEqual(arg, arg2);
        }

        @Override
        public /* synthetic */ boolean equals(@Nullable Object first, @Nullable Object second) {
            return this.equals((ItemStack)first, (ItemStack)second);
        }

        @Override
        public /* synthetic */ int hashCode(@Nullable Object stack) {
            return this.hashCode((ItemStack)stack);
        }
    };

    public static Set<ItemStack> create() {
        return new ObjectLinkedOpenCustomHashSet<ItemStack>(HASH_STRATEGY);
    }
}

