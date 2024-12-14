/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 * 
 * Could not load the following classes:
 *  org.jetbrains.annotations.Nullable
 */
package net.minecraft.block.entity;

import com.mojang.logging.LogUtils;
import com.mojang.serialization.Codec;
import java.util.HashSet;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.component.ComponentChanges;
import net.minecraft.component.ComponentMap;
import net.minecraft.component.ComponentMapImpl;
import net.minecraft.component.ComponentType;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.NbtComponent;
import net.minecraft.item.BlockItem;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtOps;
import net.minecraft.network.listener.ClientPlayPacketListener;
import net.minecraft.network.packet.Packet;
import net.minecraft.registry.Registries;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.crash.CrashReportSection;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;

public abstract class BlockEntity {
    private static final Logger LOGGER = LogUtils.getLogger();
    private final BlockEntityType<?> type;
    @Nullable
    protected World world;
    protected final BlockPos pos;
    protected boolean removed;
    private BlockState cachedState;
    private ComponentMap components = ComponentMap.EMPTY;

    public BlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        this.type = type;
        this.pos = pos.toImmutable();
        this.cachedState = state;
    }

    public static BlockPos posFromNbt(NbtCompound nbt) {
        return new BlockPos(nbt.getInt("x"), nbt.getInt("y"), nbt.getInt("z"));
    }

    @Nullable
    public World getWorld() {
        return this.world;
    }

    public void setWorld(World world) {
        this.world = world;
    }

    public boolean hasWorld() {
        return this.world != null;
    }

    protected void readNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup registryLookup) {
    }

    public final void read(NbtCompound nbt, RegistryWrapper.WrapperLookup registryLookup) {
        this.readNbt(nbt, registryLookup);
        Components.CODEC.parse(registryLookup.getOps(NbtOps.INSTANCE), nbt).resultOrPartial(error -> LOGGER.warn("Failed to load components: {}", error)).ifPresent(components -> {
            this.components = components;
        });
    }

    public final void readComponentlessNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup registryLookup) {
        this.readNbt(nbt, registryLookup);
    }

    protected void writeNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup registryLookup) {
    }

    public final NbtCompound createNbtWithIdentifyingData(RegistryWrapper.WrapperLookup registryLookup) {
        NbtCompound lv = this.createNbt(registryLookup);
        this.writeIdentifyingData(lv);
        return lv;
    }

    public final NbtCompound createNbtWithId(RegistryWrapper.WrapperLookup registryLookup) {
        NbtCompound lv = this.createNbt(registryLookup);
        this.writeIdToNbt(lv);
        return lv;
    }

    public final NbtCompound createNbt(RegistryWrapper.WrapperLookup registryLookup) {
        NbtCompound lv = new NbtCompound();
        this.writeNbt(lv, registryLookup);
        Components.CODEC.encodeStart(registryLookup.getOps(NbtOps.INSTANCE), this.components).resultOrPartial(snbt -> LOGGER.warn("Failed to save components: {}", snbt)).ifPresent(nbt -> lv.copyFrom((NbtCompound)nbt));
        return lv;
    }

    public final NbtCompound createComponentlessNbt(RegistryWrapper.WrapperLookup registryLookup) {
        NbtCompound lv = new NbtCompound();
        this.writeNbt(lv, registryLookup);
        return lv;
    }

    public final NbtCompound createComponentlessNbtWithIdentifyingData(RegistryWrapper.WrapperLookup registryLookup) {
        NbtCompound lv = this.createComponentlessNbt(registryLookup);
        this.writeIdentifyingData(lv);
        return lv;
    }

    private void writeIdToNbt(NbtCompound nbt) {
        Identifier lv = BlockEntityType.getId(this.getType());
        if (lv == null) {
            throw new RuntimeException(String.valueOf(this.getClass()) + " is missing a mapping! This is a bug!");
        }
        nbt.putString("id", lv.toString());
    }

    public static void writeIdToNbt(NbtCompound nbt, BlockEntityType<?> type) {
        nbt.putString("id", BlockEntityType.getId(type).toString());
    }

    public void setStackNbt(ItemStack stack, RegistryWrapper.WrapperLookup registries) {
        NbtCompound lv = this.createComponentlessNbt(registries);
        this.removeFromCopiedStackNbt(lv);
        BlockItem.setBlockEntityData(stack, this.getType(), lv);
        stack.applyComponentsFrom(this.createComponentMap());
    }

    private void writeIdentifyingData(NbtCompound nbt) {
        this.writeIdToNbt(nbt);
        nbt.putInt("x", this.pos.getX());
        nbt.putInt("y", this.pos.getY());
        nbt.putInt("z", this.pos.getZ());
    }

    @Nullable
    public static BlockEntity createFromNbt(BlockPos pos, BlockState state, NbtCompound nbt, RegistryWrapper.WrapperLookup registryLookup) {
        String string = nbt.getString("id");
        Identifier lv = Identifier.tryParse(string);
        if (lv == null) {
            LOGGER.error("Block entity has invalid type: {}", (Object)string);
            return null;
        }
        return Registries.BLOCK_ENTITY_TYPE.getOrEmpty(lv).map(type -> {
            try {
                return type.instantiate(pos, state);
            } catch (Throwable throwable) {
                LOGGER.error("Failed to create block entity {}", (Object)string, (Object)throwable);
                return null;
            }
        }).map(blockEntity -> {
            try {
                blockEntity.read(nbt, registryLookup);
                return blockEntity;
            } catch (Throwable throwable) {
                LOGGER.error("Failed to load data for block entity {}", (Object)string, (Object)throwable);
                return null;
            }
        }).orElseGet(() -> {
            LOGGER.warn("Skipping BlockEntity with id {}", (Object)string);
            return null;
        });
    }

    public void markDirty() {
        if (this.world != null) {
            BlockEntity.markDirty(this.world, this.pos, this.cachedState);
        }
    }

    protected static void markDirty(World world, BlockPos pos, BlockState state) {
        world.markDirty(pos);
        if (!state.isAir()) {
            world.updateComparators(pos, state.getBlock());
        }
    }

    public BlockPos getPos() {
        return this.pos;
    }

    public BlockState getCachedState() {
        return this.cachedState;
    }

    @Nullable
    public Packet<ClientPlayPacketListener> toUpdatePacket() {
        return null;
    }

    public NbtCompound toInitialChunkDataNbt(RegistryWrapper.WrapperLookup registryLookup) {
        return new NbtCompound();
    }

    public boolean isRemoved() {
        return this.removed;
    }

    public void markRemoved() {
        this.removed = true;
    }

    public void cancelRemoval() {
        this.removed = false;
    }

    public boolean onSyncedBlockEvent(int type, int data) {
        return false;
    }

    public void populateCrashReport(CrashReportSection crashReportSection) {
        crashReportSection.add("Name", () -> String.valueOf(Registries.BLOCK_ENTITY_TYPE.getId(this.getType())) + " // " + this.getClass().getCanonicalName());
        if (this.world == null) {
            return;
        }
        CrashReportSection.addBlockInfo(crashReportSection, this.world, this.pos, this.getCachedState());
        CrashReportSection.addBlockInfo(crashReportSection, this.world, this.pos, this.world.getBlockState(this.pos));
    }

    public boolean copyItemDataRequiresOperator() {
        return false;
    }

    public BlockEntityType<?> getType() {
        return this.type;
    }

    @Deprecated
    public void setCachedState(BlockState state) {
        this.cachedState = state;
    }

    protected void readComponents(ComponentsAccess components) {
    }

    public final void readComponents(ItemStack stack) {
        this.readComponents(stack.getDefaultComponents(), stack.getComponentChanges());
    }

    public final void readComponents(ComponentMap defaultComponents, ComponentChanges components) {
        final HashSet<ComponentType<NbtComponent>> set = new HashSet<ComponentType<NbtComponent>>();
        set.add(DataComponentTypes.BLOCK_ENTITY_DATA);
        final ComponentMapImpl lv = ComponentMapImpl.create(defaultComponents, components);
        this.readComponents(new ComponentsAccess(){

            @Override
            @Nullable
            public <T> T get(ComponentType<T> type) {
                set.add(type);
                return lv.get(type);
            }

            @Override
            public <T> T getOrDefault(ComponentType<? extends T> type, T fallback) {
                set.add(type);
                return lv.getOrDefault(type, fallback);
            }
        });
        ComponentChanges lv2 = components.withRemovedIf(set::contains);
        this.components = lv2.toAddedRemovedPair().added();
    }

    protected void addComponents(ComponentMap.Builder componentMapBuilder) {
    }

    @Deprecated
    public void removeFromCopiedStackNbt(NbtCompound nbt) {
    }

    public final ComponentMap createComponentMap() {
        ComponentMap.Builder lv = ComponentMap.builder();
        lv.addAll(this.components);
        this.addComponents(lv);
        return lv.build();
    }

    public ComponentMap getComponents() {
        return this.components;
    }

    public void setComponents(ComponentMap components) {
        this.components = components;
    }

    @Nullable
    public static Text tryParseCustomName(String json, RegistryWrapper.WrapperLookup registryLookup) {
        try {
            return Text.Serialization.fromJson(json, registryLookup);
        } catch (Exception exception) {
            LOGGER.warn("Failed to parse custom name from string '{}', discarding", (Object)json, (Object)exception);
            return null;
        }
    }

    static class Components {
        public static final Codec<ComponentMap> CODEC = ComponentMap.CODEC.optionalFieldOf("components", ComponentMap.EMPTY).codec();

        private Components() {
        }
    }

    protected static interface ComponentsAccess {
        @Nullable
        public <T> T get(ComponentType<T> var1);

        public <T> T getOrDefault(ComponentType<? extends T> var1, T var2);
    }
}

