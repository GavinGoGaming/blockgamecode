/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 * 
 * Could not load the following classes:
 *  org.jetbrains.annotations.Nullable
 */
package net.minecraft.block.entity;

import com.mojang.logging.LogUtils;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import java.util.Objects;
import net.minecraft.advancement.criterion.Criteria;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.BrushableBlock;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.loot.LootTable;
import net.minecraft.loot.context.LootContextParameterSet;
import net.minecraft.loot.context.LootContextParameters;
import net.minecraft.loot.context.LootContextTypes;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.s2c.play.BlockEntityUpdateS2CPacket;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.state.property.Properties;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.WorldEvents;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;

public class BrushableBlockEntity
extends BlockEntity {
    private static final Logger LOGGER = LogUtils.getLogger();
    private static final String LOOT_TABLE_NBT_KEY = "LootTable";
    private static final String LOOT_TABLE_SEED_NBT_KEY = "LootTableSeed";
    private static final String HIT_DIRECTION_NBT_KEY = "hit_direction";
    private static final String ITEM_NBT_KEY = "item";
    private static final int field_42806 = 10;
    private static final int field_42807 = 40;
    private static final int field_42808 = 10;
    private int brushesCount;
    private long nextDustTime;
    private long nextBrushTime;
    private ItemStack item = ItemStack.EMPTY;
    @Nullable
    private Direction hitDirection;
    @Nullable
    private RegistryKey<LootTable> lootTable;
    private long lootTableSeed;

    public BrushableBlockEntity(BlockPos pos, BlockState state) {
        super(BlockEntityType.BRUSHABLE_BLOCK, pos, state);
    }

    public boolean brush(long worldTime, PlayerEntity player, Direction hitDirection) {
        if (this.hitDirection == null) {
            this.hitDirection = hitDirection;
        }
        this.nextDustTime = worldTime + 40L;
        if (worldTime < this.nextBrushTime || !(this.world instanceof ServerWorld)) {
            return false;
        }
        this.nextBrushTime = worldTime + 10L;
        this.generateItem(player);
        int i = this.getDustedLevel();
        if (++this.brushesCount >= 10) {
            this.finishBrushing(player);
            return true;
        }
        this.world.scheduleBlockTick(this.getPos(), this.getCachedState().getBlock(), 2);
        int j = this.getDustedLevel();
        if (i != j) {
            BlockState lv = this.getCachedState();
            BlockState lv2 = (BlockState)lv.with(Properties.DUSTED, j);
            this.world.setBlockState(this.getPos(), lv2, Block.NOTIFY_ALL);
        }
        return false;
    }

    public void generateItem(PlayerEntity player) {
        if (this.lootTable == null || this.world == null || this.world.isClient() || this.world.getServer() == null) {
            return;
        }
        LootTable lv = this.world.getServer().getReloadableRegistries().getLootTable(this.lootTable);
        if (player instanceof ServerPlayerEntity) {
            ServerPlayerEntity lv2 = (ServerPlayerEntity)player;
            Criteria.PLAYER_GENERATES_CONTAINER_LOOT.trigger(lv2, this.lootTable);
        }
        LootContextParameterSet lv3 = new LootContextParameterSet.Builder((ServerWorld)this.world).add(LootContextParameters.ORIGIN, Vec3d.ofCenter(this.pos)).luck(player.getLuck()).add(LootContextParameters.THIS_ENTITY, player).build(LootContextTypes.CHEST);
        ObjectArrayList<ItemStack> objectArrayList = lv.generateLoot(lv3, this.lootTableSeed);
        this.item = switch (objectArrayList.size()) {
            case 0 -> ItemStack.EMPTY;
            case 1 -> objectArrayList.get(0);
            default -> {
                LOGGER.warn("Expected max 1 loot from loot table {}, but got {}", (Object)this.lootTable.getValue(), (Object)objectArrayList.size());
                yield objectArrayList.get(0);
            }
        };
        this.lootTable = null;
        this.markDirty();
    }

    private void finishBrushing(PlayerEntity player) {
        Block lv4;
        if (this.world == null || this.world.getServer() == null) {
            return;
        }
        this.spawnItem(player);
        BlockState lv = this.getCachedState();
        this.world.syncWorldEvent(WorldEvents.BLOCK_FINISHED_BRUSHING, this.getPos(), Block.getRawIdFromState(lv));
        Block lv2 = this.getCachedState().getBlock();
        if (lv2 instanceof BrushableBlock) {
            BrushableBlock lv3 = (BrushableBlock)lv2;
            lv4 = lv3.getBaseBlock();
        } else {
            lv4 = Blocks.AIR;
        }
        this.world.setBlockState(this.pos, lv4.getDefaultState(), Block.NOTIFY_ALL);
    }

    private void spawnItem(PlayerEntity player) {
        if (this.world == null || this.world.getServer() == null) {
            return;
        }
        this.generateItem(player);
        if (!this.item.isEmpty()) {
            double d = EntityType.ITEM.getWidth();
            double e = 1.0 - d;
            double f = d / 2.0;
            Direction lv = Objects.requireNonNullElse(this.hitDirection, Direction.UP);
            BlockPos lv2 = this.pos.offset(lv, 1);
            double g = (double)lv2.getX() + 0.5 * e + f;
            double h = (double)lv2.getY() + 0.5 + (double)(EntityType.ITEM.getHeight() / 2.0f);
            double i = (double)lv2.getZ() + 0.5 * e + f;
            ItemEntity lv3 = new ItemEntity(this.world, g, h, i, this.item.split(this.world.random.nextInt(21) + 10));
            lv3.setVelocity(Vec3d.ZERO);
            this.world.spawnEntity(lv3);
            this.item = ItemStack.EMPTY;
        }
    }

    public void scheduledTick() {
        if (this.world == null) {
            return;
        }
        if (this.brushesCount != 0 && this.world.getTime() >= this.nextDustTime) {
            int i = this.getDustedLevel();
            this.brushesCount = Math.max(0, this.brushesCount - 2);
            int j = this.getDustedLevel();
            if (i != j) {
                this.world.setBlockState(this.getPos(), (BlockState)this.getCachedState().with(Properties.DUSTED, j), Block.NOTIFY_ALL);
            }
            int k = 4;
            this.nextDustTime = this.world.getTime() + 4L;
        }
        if (this.brushesCount == 0) {
            this.hitDirection = null;
            this.nextDustTime = 0L;
            this.nextBrushTime = 0L;
        } else {
            this.world.scheduleBlockTick(this.getPos(), this.getCachedState().getBlock(), 2);
        }
    }

    private boolean readLootTableFromNbt(NbtCompound nbt) {
        if (nbt.contains(LOOT_TABLE_NBT_KEY, NbtElement.STRING_TYPE)) {
            this.lootTable = RegistryKey.of(RegistryKeys.LOOT_TABLE, Identifier.of(nbt.getString(LOOT_TABLE_NBT_KEY)));
            this.lootTableSeed = nbt.getLong(LOOT_TABLE_SEED_NBT_KEY);
            return true;
        }
        return false;
    }

    private boolean writeLootTableToNbt(NbtCompound nbt) {
        if (this.lootTable == null) {
            return false;
        }
        nbt.putString(LOOT_TABLE_NBT_KEY, this.lootTable.getValue().toString());
        if (this.lootTableSeed != 0L) {
            nbt.putLong(LOOT_TABLE_SEED_NBT_KEY, this.lootTableSeed);
        }
        return true;
    }

    @Override
    public NbtCompound toInitialChunkDataNbt(RegistryWrapper.WrapperLookup registryLookup) {
        NbtCompound lv = super.toInitialChunkDataNbt(registryLookup);
        if (this.hitDirection != null) {
            lv.putInt(HIT_DIRECTION_NBT_KEY, this.hitDirection.ordinal());
        }
        if (!this.item.isEmpty()) {
            lv.put(ITEM_NBT_KEY, this.item.encode(registryLookup));
        }
        return lv;
    }

    public BlockEntityUpdateS2CPacket toUpdatePacket() {
        return BlockEntityUpdateS2CPacket.create(this);
    }

    @Override
    protected void readNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup registryLookup) {
        super.readNbt(nbt, registryLookup);
        this.item = !this.readLootTableFromNbt(nbt) && nbt.contains(ITEM_NBT_KEY) ? ItemStack.fromNbt(registryLookup, nbt.getCompound(ITEM_NBT_KEY)).orElse(ItemStack.EMPTY) : ItemStack.EMPTY;
        if (nbt.contains(HIT_DIRECTION_NBT_KEY)) {
            this.hitDirection = Direction.values()[nbt.getInt(HIT_DIRECTION_NBT_KEY)];
        }
    }

    @Override
    protected void writeNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup registryLookup) {
        super.writeNbt(nbt, registryLookup);
        if (!this.writeLootTableToNbt(nbt) && !this.item.isEmpty()) {
            nbt.put(ITEM_NBT_KEY, this.item.encode(registryLookup));
        }
    }

    public void setLootTable(RegistryKey<LootTable> lootTable, long seed) {
        this.lootTable = lootTable;
        this.lootTableSeed = seed;
    }

    private int getDustedLevel() {
        if (this.brushesCount == 0) {
            return 0;
        }
        if (this.brushesCount < 3) {
            return 1;
        }
        if (this.brushesCount < 6) {
            return 2;
        }
        return 3;
    }

    @Nullable
    public Direction getHitDirection() {
        return this.hitDirection;
    }

    public ItemStack getItem() {
        return this.item;
    }

    public /* synthetic */ Packet toUpdatePacket() {
        return this.toUpdatePacket();
    }
}

