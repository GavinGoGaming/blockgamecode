/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.inventory;

import com.mojang.serialization.Codec;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.text.Text;

public record ContainerLock(String key) {
    public static final ContainerLock EMPTY = new ContainerLock("");
    public static final Codec<ContainerLock> CODEC = Codec.STRING.xmap(ContainerLock::new, ContainerLock::key);
    public static final String LOCK_KEY = "Lock";

    public boolean canOpen(ItemStack stack) {
        if (this.key.isEmpty()) {
            return true;
        }
        Text lv = stack.get(DataComponentTypes.CUSTOM_NAME);
        return lv != null && this.key.equals(lv.getString());
    }

    public void writeNbt(NbtCompound nbt) {
        if (!this.key.isEmpty()) {
            nbt.putString(LOCK_KEY, this.key);
        }
    }

    public static ContainerLock fromNbt(NbtCompound nbt) {
        if (nbt.contains(LOCK_KEY, NbtElement.STRING_TYPE)) {
            return new ContainerLock(nbt.getString(LOCK_KEY));
        }
        return EMPTY;
    }
}

