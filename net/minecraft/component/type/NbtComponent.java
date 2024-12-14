/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.component.type;

import com.mojang.logging.LogUtils;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.MapDecoder;
import com.mojang.serialization.MapEncoder;
import com.mojang.serialization.MapLike;
import io.netty.buffer.ByteBuf;
import java.util.UUID;
import java.util.function.Consumer;
import java.util.function.Predicate;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.component.ComponentType;
import net.minecraft.entity.Entity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtHelper;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.StringNbtReader;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.registry.RegistryWrapper;
import org.slf4j.Logger;

public final class NbtComponent {
    private static final Logger LOGGER = LogUtils.getLogger();
    public static final NbtComponent DEFAULT = new NbtComponent(new NbtCompound());
    public static final Codec<NbtComponent> CODEC = Codec.withAlternative(NbtCompound.CODEC, StringNbtReader.STRINGIFIED_CODEC).xmap(NbtComponent::new, component -> component.nbt);
    public static final Codec<NbtComponent> CODEC_WITH_ID = CODEC.validate(component -> component.getNbt().contains("id", NbtElement.STRING_TYPE) ? DataResult.success(component) : DataResult.error(() -> "Missing id for entity in: " + String.valueOf(component)));
    @Deprecated
    public static final PacketCodec<ByteBuf, NbtComponent> PACKET_CODEC = PacketCodecs.NBT_COMPOUND.xmap(NbtComponent::new, component -> component.nbt);
    private final NbtCompound nbt;

    private NbtComponent(NbtCompound nbt) {
        this.nbt = nbt;
    }

    public static NbtComponent of(NbtCompound nbt) {
        return new NbtComponent(nbt.copy());
    }

    public static Predicate<ItemStack> createPredicate(ComponentType<NbtComponent> type, NbtCompound nbt) {
        return stack -> {
            NbtComponent lv = stack.getOrDefault(type, DEFAULT);
            return lv.matches(nbt);
        };
    }

    public boolean matches(NbtCompound nbt) {
        return NbtHelper.matches(nbt, this.nbt, true);
    }

    public static void set(ComponentType<NbtComponent> type, ItemStack stack, Consumer<NbtCompound> nbtSetter) {
        NbtComponent lv = stack.getOrDefault(type, DEFAULT).apply(nbtSetter);
        if (lv.nbt.isEmpty()) {
            stack.remove(type);
        } else {
            stack.set(type, lv);
        }
    }

    public static void set(ComponentType<NbtComponent> type, ItemStack stack, NbtCompound nbt) {
        if (!nbt.isEmpty()) {
            stack.set(type, NbtComponent.of(nbt));
        } else {
            stack.remove(type);
        }
    }

    public NbtComponent apply(Consumer<NbtCompound> nbtConsumer) {
        NbtCompound lv = this.nbt.copy();
        nbtConsumer.accept(lv);
        return new NbtComponent(lv);
    }

    public void applyToEntity(Entity entity) {
        NbtCompound lv = entity.writeNbt(new NbtCompound());
        UUID uUID = entity.getUuid();
        lv.copyFrom(this.nbt);
        entity.readNbt(lv);
        entity.setUuid(uUID);
    }

    public boolean applyToBlockEntity(BlockEntity blockEntity, RegistryWrapper.WrapperLookup registryLookup) {
        NbtCompound lv = blockEntity.createComponentlessNbt(registryLookup);
        NbtCompound lv2 = lv.copy();
        lv.copyFrom(this.nbt);
        if (!lv.equals(lv2)) {
            try {
                blockEntity.readComponentlessNbt(lv, registryLookup);
                blockEntity.markDirty();
                return true;
            } catch (Exception exception) {
                LOGGER.warn("Failed to apply custom data to block entity at {}", (Object)blockEntity.getPos(), (Object)exception);
                try {
                    blockEntity.readComponentlessNbt(lv2, registryLookup);
                } catch (Exception exception2) {
                    LOGGER.warn("Failed to rollback block entity at {} after failure", (Object)blockEntity.getPos(), (Object)exception2);
                }
            }
        }
        return false;
    }

    public <T> DataResult<NbtComponent> with(DynamicOps<NbtElement> ops, MapEncoder<T> encoder, T value) {
        return encoder.encode((NbtElement)value, ops, ops.mapBuilder()).build(this.nbt).map(nbt -> new NbtComponent((NbtCompound)nbt));
    }

    public <T> DataResult<T> get(MapDecoder<T> decoder) {
        return this.get(NbtOps.INSTANCE, decoder);
    }

    public <T> DataResult<T> get(DynamicOps<NbtElement> ops, MapDecoder<T> decoder) {
        MapLike<NbtElement> mapLike = ops.getMap(this.nbt).getOrThrow();
        return decoder.decode(ops, mapLike);
    }

    public int getSize() {
        return this.nbt.getSize();
    }

    public boolean isEmpty() {
        return this.nbt.isEmpty();
    }

    public NbtCompound copyNbt() {
        return this.nbt.copy();
    }

    public boolean contains(String key) {
        return this.nbt.contains(key);
    }

    public boolean equals(Object o) {
        if (o == this) {
            return true;
        }
        if (o instanceof NbtComponent) {
            NbtComponent lv = (NbtComponent)o;
            return this.nbt.equals(lv.nbt);
        }
        return false;
    }

    public int hashCode() {
        return this.nbt.hashCode();
    }

    public String toString() {
        return this.nbt.toString();
    }

    @Deprecated
    public NbtCompound getNbt() {
        return this.nbt;
    }
}

