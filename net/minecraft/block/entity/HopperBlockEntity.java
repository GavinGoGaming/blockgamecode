/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 * 
 * Could not load the following classes:
 *  org.jetbrains.annotations.Nullable
 */
package net.minecraft.block.entity;

import java.util.List;
import java.util.function.BooleanSupplier;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.ChestBlock;
import net.minecraft.block.HopperBlock;
import net.minecraft.block.InventoryProvider;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.block.entity.ChestBlockEntity;
import net.minecraft.block.entity.Hopper;
import net.minecraft.block.entity.LootableContainerBlockEntity;
import net.minecraft.entity.Entity;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.Inventories;
import net.minecraft.inventory.Inventory;
import net.minecraft.inventory.SidedInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.predicate.entity.EntityPredicates;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.registry.tag.BlockTags;
import net.minecraft.screen.HopperScreenHandler;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.text.Text;
import net.minecraft.util.collection.DefaultedList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

public class HopperBlockEntity
extends LootableContainerBlockEntity
implements Hopper {
    public static final int TRANSFER_COOLDOWN = 8;
    public static final int INVENTORY_SIZE = 5;
    private static final int[][] AVAILABLE_SLOTS_CACHE = new int[54][];
    private DefaultedList<ItemStack> inventory = DefaultedList.ofSize(5, ItemStack.EMPTY);
    private int transferCooldown = -1;
    private long lastTickTime;
    private Direction facing;

    public HopperBlockEntity(BlockPos pos, BlockState state) {
        super(BlockEntityType.HOPPER, pos, state);
        this.facing = state.get(HopperBlock.FACING);
    }

    @Override
    protected void readNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup registryLookup) {
        super.readNbt(nbt, registryLookup);
        this.inventory = DefaultedList.ofSize(this.size(), ItemStack.EMPTY);
        if (!this.readLootTable(nbt)) {
            Inventories.readNbt(nbt, this.inventory, registryLookup);
        }
        this.transferCooldown = nbt.getInt("TransferCooldown");
    }

    @Override
    protected void writeNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup registryLookup) {
        super.writeNbt(nbt, registryLookup);
        if (!this.writeLootTable(nbt)) {
            Inventories.writeNbt(nbt, this.inventory, registryLookup);
        }
        nbt.putInt("TransferCooldown", this.transferCooldown);
    }

    @Override
    public int size() {
        return this.inventory.size();
    }

    @Override
    public ItemStack removeStack(int slot, int amount) {
        this.generateLoot(null);
        return Inventories.splitStack(this.getHeldStacks(), slot, amount);
    }

    @Override
    public void setStack(int slot, ItemStack stack) {
        this.generateLoot(null);
        this.getHeldStacks().set(slot, stack);
        stack.capCount(this.getMaxCount(stack));
    }

    @Override
    public void setCachedState(BlockState state) {
        super.setCachedState(state);
        this.facing = state.get(HopperBlock.FACING);
    }

    @Override
    protected Text getContainerName() {
        return Text.translatable("container.hopper");
    }

    public static void serverTick(World world, BlockPos pos, BlockState state, HopperBlockEntity blockEntity) {
        --blockEntity.transferCooldown;
        blockEntity.lastTickTime = world.getTime();
        if (!blockEntity.needsCooldown()) {
            blockEntity.setTransferCooldown(0);
            HopperBlockEntity.insertAndExtract(world, pos, state, blockEntity, () -> HopperBlockEntity.extract(world, blockEntity));
        }
    }

    private static boolean insertAndExtract(World world, BlockPos pos, BlockState state, HopperBlockEntity blockEntity, BooleanSupplier booleanSupplier) {
        if (world.isClient) {
            return false;
        }
        if (!blockEntity.needsCooldown() && state.get(HopperBlock.ENABLED).booleanValue()) {
            boolean bl = false;
            if (!blockEntity.isEmpty()) {
                bl = HopperBlockEntity.insert(world, pos, blockEntity);
            }
            if (!blockEntity.isFull()) {
                bl |= booleanSupplier.getAsBoolean();
            }
            if (bl) {
                blockEntity.setTransferCooldown(8);
                HopperBlockEntity.markDirty(world, pos, state);
                return true;
            }
        }
        return false;
    }

    private boolean isFull() {
        for (ItemStack lv : this.inventory) {
            if (!lv.isEmpty() && lv.getCount() == lv.getMaxCount()) continue;
            return false;
        }
        return true;
    }

    private static boolean insert(World world, BlockPos pos, HopperBlockEntity blockEntity) {
        Inventory lv = HopperBlockEntity.getOutputInventory(world, pos, blockEntity);
        if (lv == null) {
            return false;
        }
        Direction lv2 = blockEntity.facing.getOpposite();
        if (HopperBlockEntity.isInventoryFull(lv, lv2)) {
            return false;
        }
        for (int i = 0; i < blockEntity.size(); ++i) {
            ItemStack lv3 = blockEntity.getStack(i);
            if (lv3.isEmpty()) continue;
            int j = lv3.getCount();
            ItemStack lv4 = HopperBlockEntity.transfer(blockEntity, lv, blockEntity.removeStack(i, 1), lv2);
            if (lv4.isEmpty()) {
                lv.markDirty();
                return true;
            }
            lv3.setCount(j);
            if (j != 1) continue;
            blockEntity.setStack(i, lv3);
        }
        return false;
    }

    private static int[] getAvailableSlots(Inventory inventory, Direction side) {
        if (inventory instanceof SidedInventory) {
            SidedInventory lv = (SidedInventory)inventory;
            return lv.getAvailableSlots(side);
        }
        int i = inventory.size();
        if (i < AVAILABLE_SLOTS_CACHE.length) {
            int[] is = AVAILABLE_SLOTS_CACHE[i];
            if (is != null) {
                return is;
            }
            int[] js = HopperBlockEntity.indexArray(i);
            HopperBlockEntity.AVAILABLE_SLOTS_CACHE[i] = js;
            return js;
        }
        return HopperBlockEntity.indexArray(i);
    }

    private static int[] indexArray(int size) {
        int[] is = new int[size];
        for (int j = 0; j < is.length; ++j) {
            is[j] = j;
        }
        return is;
    }

    private static boolean isInventoryFull(Inventory inventory, Direction direction) {
        int[] is;
        for (int i : is = HopperBlockEntity.getAvailableSlots(inventory, direction)) {
            ItemStack lv = inventory.getStack(i);
            if (lv.getCount() >= lv.getMaxCount()) continue;
            return false;
        }
        return true;
    }

    public static boolean extract(World world, Hopper hopper) {
        boolean bl;
        BlockState lv2;
        BlockPos lv = BlockPos.ofFloored(hopper.getHopperX(), hopper.getHopperY() + 1.0, hopper.getHopperZ());
        Inventory lv3 = HopperBlockEntity.getInputInventory(world, hopper, lv, lv2 = world.getBlockState(lv));
        if (lv3 != null) {
            Direction lv4 = Direction.DOWN;
            for (int i : HopperBlockEntity.getAvailableSlots(lv3, lv4)) {
                if (!HopperBlockEntity.extract(hopper, lv3, i, lv4)) continue;
                return true;
            }
            return false;
        }
        boolean bl2 = bl = hopper.canBlockFromAbove() && lv2.isFullCube(world, lv) && !lv2.isIn(BlockTags.DOES_NOT_BLOCK_HOPPERS);
        if (!bl) {
            for (ItemEntity lv5 : HopperBlockEntity.getInputItemEntities(world, hopper)) {
                if (!HopperBlockEntity.extract(hopper, lv5)) continue;
                return true;
            }
        }
        return false;
    }

    private static boolean extract(Hopper hopper, Inventory inventory, int slot, Direction side) {
        ItemStack lv = inventory.getStack(slot);
        if (!lv.isEmpty() && HopperBlockEntity.canExtract(hopper, inventory, lv, slot, side)) {
            int j = lv.getCount();
            ItemStack lv2 = HopperBlockEntity.transfer(inventory, hopper, inventory.removeStack(slot, 1), null);
            if (lv2.isEmpty()) {
                inventory.markDirty();
                return true;
            }
            lv.setCount(j);
            if (j == 1) {
                inventory.setStack(slot, lv);
            }
        }
        return false;
    }

    public static boolean extract(Inventory inventory, ItemEntity itemEntity) {
        boolean bl = false;
        ItemStack lv = itemEntity.getStack().copy();
        ItemStack lv2 = HopperBlockEntity.transfer(null, inventory, lv, null);
        if (lv2.isEmpty()) {
            bl = true;
            itemEntity.setStack(ItemStack.EMPTY);
            itemEntity.discard();
        } else {
            itemEntity.setStack(lv2);
        }
        return bl;
    }

    /*
     * Enabled aggressive block sorting
     * Lifted jumps to return sites
     */
    public static ItemStack transfer(@Nullable Inventory from, Inventory to, ItemStack stack, @Nullable Direction side) {
        if (to instanceof SidedInventory) {
            SidedInventory lv = (SidedInventory)to;
            if (side != null) {
                int[] is = lv.getAvailableSlots(side);
                int i = 0;
                while (i < is.length) {
                    if (stack.isEmpty()) return stack;
                    stack = HopperBlockEntity.transfer(from, to, stack, is[i], side);
                    ++i;
                }
                return stack;
            }
        }
        int j = to.size();
        int i = 0;
        while (i < j) {
            if (stack.isEmpty()) return stack;
            stack = HopperBlockEntity.transfer(from, to, stack, i, side);
            ++i;
        }
        return stack;
    }

    private static boolean canInsert(Inventory inventory, ItemStack stack, int slot, @Nullable Direction side) {
        SidedInventory lv;
        if (!inventory.isValid(slot, stack)) {
            return false;
        }
        return !(inventory instanceof SidedInventory) || (lv = (SidedInventory)inventory).canInsert(slot, stack, side);
    }

    private static boolean canExtract(Inventory hopperInventory, Inventory fromInventory, ItemStack stack, int slot, Direction facing) {
        SidedInventory lv;
        if (!fromInventory.canTransferTo(hopperInventory, slot, stack)) {
            return false;
        }
        return !(fromInventory instanceof SidedInventory) || (lv = (SidedInventory)fromInventory).canExtract(slot, stack, facing);
    }

    private static ItemStack transfer(@Nullable Inventory from, Inventory to, ItemStack stack, int slot, @Nullable Direction side) {
        ItemStack lv = to.getStack(slot);
        if (HopperBlockEntity.canInsert(to, stack, slot, side)) {
            int k;
            boolean bl = false;
            boolean bl2 = to.isEmpty();
            if (lv.isEmpty()) {
                to.setStack(slot, stack);
                stack = ItemStack.EMPTY;
                bl = true;
            } else if (HopperBlockEntity.canMergeItems(lv, stack)) {
                int j = stack.getMaxCount() - lv.getCount();
                k = Math.min(stack.getCount(), j);
                stack.decrement(k);
                lv.increment(k);
                boolean bl3 = bl = k > 0;
            }
            if (bl) {
                HopperBlockEntity lv2;
                if (bl2 && to instanceof HopperBlockEntity && !(lv2 = (HopperBlockEntity)to).isDisabled()) {
                    k = 0;
                    if (from instanceof HopperBlockEntity) {
                        HopperBlockEntity lv3 = (HopperBlockEntity)from;
                        if (lv2.lastTickTime >= lv3.lastTickTime) {
                            k = 1;
                        }
                    }
                    lv2.setTransferCooldown(8 - k);
                }
                to.markDirty();
            }
        }
        return stack;
    }

    @Nullable
    private static Inventory getOutputInventory(World world, BlockPos pos, HopperBlockEntity blockEntity) {
        return HopperBlockEntity.getInventoryAt(world, pos.offset(blockEntity.facing));
    }

    @Nullable
    private static Inventory getInputInventory(World world, Hopper hopper, BlockPos pos, BlockState state) {
        return HopperBlockEntity.getInventoryAt(world, pos, state, hopper.getHopperX(), hopper.getHopperY() + 1.0, hopper.getHopperZ());
    }

    public static List<ItemEntity> getInputItemEntities(World world, Hopper hopper) {
        Box lv = hopper.getInputAreaShape().offset(hopper.getHopperX() - 0.5, hopper.getHopperY() - 0.5, hopper.getHopperZ() - 0.5);
        return world.getEntitiesByClass(ItemEntity.class, lv, EntityPredicates.VALID_ENTITY);
    }

    @Nullable
    public static Inventory getInventoryAt(World world, BlockPos pos) {
        return HopperBlockEntity.getInventoryAt(world, pos, world.getBlockState(pos), (double)pos.getX() + 0.5, (double)pos.getY() + 0.5, (double)pos.getZ() + 0.5);
    }

    @Nullable
    private static Inventory getInventoryAt(World world, BlockPos pos, BlockState state, double x, double y, double z) {
        Inventory lv = HopperBlockEntity.getBlockInventoryAt(world, pos, state);
        if (lv == null) {
            lv = HopperBlockEntity.getEntityInventoryAt(world, x, y, z);
        }
        return lv;
    }

    @Nullable
    private static Inventory getBlockInventoryAt(World world, BlockPos pos, BlockState state) {
        BlockEntity lv2;
        Block lv = state.getBlock();
        if (lv instanceof InventoryProvider) {
            return ((InventoryProvider)((Object)lv)).getInventory(state, world, pos);
        }
        if (state.hasBlockEntity() && (lv2 = world.getBlockEntity(pos)) instanceof Inventory) {
            Inventory lv3 = (Inventory)((Object)lv2);
            if (lv3 instanceof ChestBlockEntity && lv instanceof ChestBlock) {
                lv3 = ChestBlock.getInventory((ChestBlock)lv, state, world, pos, true);
            }
            return lv3;
        }
        return null;
    }

    @Nullable
    private static Inventory getEntityInventoryAt(World world, double x, double y, double z) {
        List<Entity> list = world.getOtherEntities(null, new Box(x - 0.5, y - 0.5, z - 0.5, x + 0.5, y + 0.5, z + 0.5), EntityPredicates.VALID_INVENTORIES);
        if (!list.isEmpty()) {
            return (Inventory)((Object)list.get(world.random.nextInt(list.size())));
        }
        return null;
    }

    private static boolean canMergeItems(ItemStack first, ItemStack second) {
        return first.getCount() <= first.getMaxCount() && ItemStack.areItemsAndComponentsEqual(first, second);
    }

    @Override
    public double getHopperX() {
        return (double)this.pos.getX() + 0.5;
    }

    @Override
    public double getHopperY() {
        return (double)this.pos.getY() + 0.5;
    }

    @Override
    public double getHopperZ() {
        return (double)this.pos.getZ() + 0.5;
    }

    @Override
    public boolean canBlockFromAbove() {
        return true;
    }

    private void setTransferCooldown(int transferCooldown) {
        this.transferCooldown = transferCooldown;
    }

    private boolean needsCooldown() {
        return this.transferCooldown > 0;
    }

    private boolean isDisabled() {
        return this.transferCooldown > 8;
    }

    @Override
    protected DefaultedList<ItemStack> getHeldStacks() {
        return this.inventory;
    }

    @Override
    protected void setHeldStacks(DefaultedList<ItemStack> inventory) {
        this.inventory = inventory;
    }

    public static void onEntityCollided(World world, BlockPos pos, BlockState state, Entity entity, HopperBlockEntity blockEntity) {
        ItemEntity lv;
        if (entity instanceof ItemEntity && !(lv = (ItemEntity)entity).getStack().isEmpty() && entity.getBoundingBox().offset(-pos.getX(), -pos.getY(), -pos.getZ()).intersects(blockEntity.getInputAreaShape())) {
            HopperBlockEntity.insertAndExtract(world, pos, state, blockEntity, () -> HopperBlockEntity.extract(blockEntity, lv));
        }
    }

    @Override
    protected ScreenHandler createScreenHandler(int syncId, PlayerInventory playerInventory) {
        return new HopperScreenHandler(syncId, playerInventory, this);
    }
}

