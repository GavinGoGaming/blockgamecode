/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 * 
 * Could not load the following classes:
 *  org.jetbrains.annotations.Nullable
 */
package net.minecraft.predicate;

import com.mojang.serialization.Codec;
import io.netty.buffer.ByteBuf;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.NbtComponent;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtHelper;
import net.minecraft.nbt.StringNbtReader;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import org.jetbrains.annotations.Nullable;

public record NbtPredicate(NbtCompound nbt) {
    public static final Codec<NbtPredicate> CODEC = StringNbtReader.NBT_COMPOUND_CODEC.xmap(NbtPredicate::new, NbtPredicate::nbt);
    public static final PacketCodec<ByteBuf, NbtPredicate> PACKET_CODEC = PacketCodecs.NBT_COMPOUND.xmap(NbtPredicate::new, NbtPredicate::nbt);

    public boolean test(ItemStack stack) {
        NbtComponent lv = stack.getOrDefault(DataComponentTypes.CUSTOM_DATA, NbtComponent.DEFAULT);
        return lv.matches(this.nbt);
    }

    public boolean test(Entity entity) {
        return this.test(NbtPredicate.entityToNbt(entity));
    }

    public boolean test(@Nullable NbtElement element) {
        return element != null && NbtHelper.matches(this.nbt, element, true);
    }

    public static NbtCompound entityToNbt(Entity entity) {
        ItemStack lv2;
        NbtCompound lv = entity.writeNbt(new NbtCompound());
        if (entity instanceof PlayerEntity && !(lv2 = ((PlayerEntity)entity).getInventory().getMainHandStack()).isEmpty()) {
            lv.put("SelectedItem", lv2.encode(entity.getRegistryManager()));
        }
        return lv;
    }
}

