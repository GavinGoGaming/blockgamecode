/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 * 
 * Could not load the following classes:
 *  org.jetbrains.annotations.Nullable
 */
package net.minecraft.entity.decoration;

import net.minecraft.block.AbstractRedstoneGateBlock;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.MapIdComponent;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.MovementType;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.data.TrackedData;
import net.minecraft.entity.data.TrackedDataHandlerRegistry;
import net.minecraft.entity.decoration.AbstractDecorationEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.StackReference;
import net.minecraft.item.FilledMapItem;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.item.map.MapState;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.network.listener.ClientPlayPacketListener;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.s2c.play.EntitySpawnS2CPacket;
import net.minecraft.registry.tag.DamageTypeTags;
import net.minecraft.server.network.EntityTrackerEntry;
import net.minecraft.sound.SoundEvent;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.GameRules;
import net.minecraft.world.World;
import net.minecraft.world.event.GameEvent;
import org.apache.commons.lang3.Validate;
import org.jetbrains.annotations.Nullable;

public class ItemFrameEntity
extends AbstractDecorationEntity {
    private static final TrackedData<ItemStack> ITEM_STACK = DataTracker.registerData(ItemFrameEntity.class, TrackedDataHandlerRegistry.ITEM_STACK);
    private static final TrackedData<Integer> ROTATION = DataTracker.registerData(ItemFrameEntity.class, TrackedDataHandlerRegistry.INTEGER);
    public static final int field_30454 = 8;
    private static final float field_51592 = 0.0625f;
    private static final float field_51593 = 0.75f;
    private static final float field_51594 = 0.75f;
    private float itemDropChance = 1.0f;
    private boolean fixed;

    public ItemFrameEntity(EntityType<? extends ItemFrameEntity> arg, World arg2) {
        super((EntityType<? extends AbstractDecorationEntity>)arg, arg2);
    }

    public ItemFrameEntity(World world, BlockPos pos, Direction facing) {
        this(EntityType.ITEM_FRAME, world, pos, facing);
    }

    public ItemFrameEntity(EntityType<? extends ItemFrameEntity> type, World world, BlockPos pos, Direction facing) {
        super((EntityType<? extends AbstractDecorationEntity>)type, world, pos);
        this.setFacing(facing);
    }

    @Override
    protected void initDataTracker(DataTracker.Builder builder) {
        builder.add(ITEM_STACK, ItemStack.EMPTY);
        builder.add(ROTATION, 0);
    }

    @Override
    protected void setFacing(Direction facing) {
        Validate.notNull(facing);
        this.facing = facing;
        if (facing.getAxis().isHorizontal()) {
            this.setPitch(0.0f);
            this.setYaw(this.facing.getHorizontal() * 90);
        } else {
            this.setPitch(-90 * facing.getDirection().offset());
            this.setYaw(0.0f);
        }
        this.prevPitch = this.getPitch();
        this.prevYaw = this.getYaw();
        this.updateAttachmentPosition();
    }

    @Override
    protected Box calculateBoundingBox(BlockPos pos, Direction side) {
        float f = 0.46875f;
        Vec3d lv = Vec3d.ofCenter(pos).offset(side, -0.46875);
        Direction.Axis lv2 = side.getAxis();
        double d = lv2 == Direction.Axis.X ? 0.0625 : 0.75;
        double e = lv2 == Direction.Axis.Y ? 0.0625 : 0.75;
        double g = lv2 == Direction.Axis.Z ? 0.0625 : 0.75;
        return Box.of(lv, d, e, g);
    }

    @Override
    public boolean canStayAttached() {
        if (this.fixed) {
            return true;
        }
        if (!this.getWorld().isSpaceEmpty(this)) {
            return false;
        }
        BlockState lv = this.getWorld().getBlockState(this.attachedBlockPos.offset(this.facing.getOpposite()));
        if (!(lv.isSolid() || this.facing.getAxis().isHorizontal() && AbstractRedstoneGateBlock.isRedstoneGate(lv))) {
            return false;
        }
        return this.getWorld().getOtherEntities(this, this.getBoundingBox(), PREDICATE).isEmpty();
    }

    @Override
    public void move(MovementType movementType, Vec3d movement) {
        if (!this.fixed) {
            super.move(movementType, movement);
        }
    }

    @Override
    public void addVelocity(double deltaX, double deltaY, double deltaZ) {
        if (!this.fixed) {
            super.addVelocity(deltaX, deltaY, deltaZ);
        }
    }

    @Override
    public void kill() {
        this.removeFromFrame(this.getHeldItemStack());
        super.kill();
    }

    @Override
    public boolean damage(DamageSource source, float amount) {
        if (this.fixed) {
            if (source.isIn(DamageTypeTags.BYPASSES_INVULNERABILITY) || source.isSourceCreativePlayer()) {
                return super.damage(source, amount);
            }
            return false;
        }
        if (this.isInvulnerableTo(source)) {
            return false;
        }
        if (!source.isIn(DamageTypeTags.IS_EXPLOSION) && !this.getHeldItemStack().isEmpty()) {
            if (!this.getWorld().isClient) {
                this.dropHeldStack(source.getAttacker(), false);
                this.emitGameEvent(GameEvent.BLOCK_CHANGE, source.getAttacker());
                this.playSound(this.getRemoveItemSound(), 1.0f, 1.0f);
            }
            return true;
        }
        return super.damage(source, amount);
    }

    public SoundEvent getRemoveItemSound() {
        return SoundEvents.ENTITY_ITEM_FRAME_REMOVE_ITEM;
    }

    @Override
    public boolean shouldRender(double distance) {
        double e = 16.0;
        return distance < (e *= 64.0 * ItemFrameEntity.getRenderDistanceMultiplier()) * e;
    }

    @Override
    public void onBreak(@Nullable Entity breaker) {
        this.playSound(this.getBreakSound(), 1.0f, 1.0f);
        this.dropHeldStack(breaker, true);
        this.emitGameEvent(GameEvent.BLOCK_CHANGE, breaker);
    }

    public SoundEvent getBreakSound() {
        return SoundEvents.ENTITY_ITEM_FRAME_BREAK;
    }

    @Override
    public void onPlace() {
        this.playSound(this.getPlaceSound(), 1.0f, 1.0f);
    }

    public SoundEvent getPlaceSound() {
        return SoundEvents.ENTITY_ITEM_FRAME_PLACE;
    }

    private void dropHeldStack(@Nullable Entity entity, boolean alwaysDrop) {
        PlayerEntity lv2;
        if (this.fixed) {
            return;
        }
        ItemStack lv = this.getHeldItemStack();
        this.setHeldItemStack(ItemStack.EMPTY);
        if (!this.getWorld().getGameRules().getBoolean(GameRules.DO_ENTITY_DROPS)) {
            if (entity == null) {
                this.removeFromFrame(lv);
            }
            return;
        }
        if (entity instanceof PlayerEntity && (lv2 = (PlayerEntity)entity).isInCreativeMode()) {
            this.removeFromFrame(lv);
            return;
        }
        if (alwaysDrop) {
            this.dropStack(this.getAsItemStack());
        }
        if (!lv.isEmpty()) {
            lv = lv.copy();
            this.removeFromFrame(lv);
            if (this.random.nextFloat() < this.itemDropChance) {
                this.dropStack(lv);
            }
        }
    }

    private void removeFromFrame(ItemStack stack) {
        MapState lv2;
        MapIdComponent lv = this.getMapId(stack);
        if (lv != null && (lv2 = FilledMapItem.getMapState(lv, this.getWorld())) != null) {
            lv2.removeFrame(this.attachedBlockPos, this.getId());
            lv2.setDirty(true);
        }
        stack.setHolder(null);
    }

    public ItemStack getHeldItemStack() {
        return this.getDataTracker().get(ITEM_STACK);
    }

    @Nullable
    public MapIdComponent getMapId(ItemStack arg) {
        return arg.get(DataComponentTypes.MAP_ID);
    }

    public boolean containsMap() {
        return this.getHeldItemStack().contains(DataComponentTypes.MAP_ID);
    }

    public void setHeldItemStack(ItemStack stack) {
        this.setHeldItemStack(stack, true);
    }

    public void setHeldItemStack(ItemStack value, boolean update) {
        if (!value.isEmpty()) {
            value = value.copyWithCount(1);
        }
        this.setAsStackHolder(value);
        this.getDataTracker().set(ITEM_STACK, value);
        if (!value.isEmpty()) {
            this.playSound(this.getAddItemSound(), 1.0f, 1.0f);
        }
        if (update && this.attachedBlockPos != null) {
            this.getWorld().updateComparators(this.attachedBlockPos, Blocks.AIR);
        }
    }

    public SoundEvent getAddItemSound() {
        return SoundEvents.ENTITY_ITEM_FRAME_ADD_ITEM;
    }

    @Override
    public StackReference getStackReference(int mappedIndex) {
        if (mappedIndex == 0) {
            return StackReference.of(this::getHeldItemStack, this::setHeldItemStack);
        }
        return super.getStackReference(mappedIndex);
    }

    @Override
    public void onTrackedDataSet(TrackedData<?> data) {
        if (data.equals(ITEM_STACK)) {
            this.setAsStackHolder(this.getHeldItemStack());
        }
    }

    private void setAsStackHolder(ItemStack stack) {
        if (!stack.isEmpty() && stack.getFrame() != this) {
            stack.setHolder(this);
        }
        this.updateAttachmentPosition();
    }

    public int getRotation() {
        return this.getDataTracker().get(ROTATION);
    }

    public void setRotation(int value) {
        this.setRotation(value, true);
    }

    private void setRotation(int value, boolean updateComparators) {
        this.getDataTracker().set(ROTATION, value % 8);
        if (updateComparators && this.attachedBlockPos != null) {
            this.getWorld().updateComparators(this.attachedBlockPos, Blocks.AIR);
        }
    }

    @Override
    public void writeCustomDataToNbt(NbtCompound nbt) {
        super.writeCustomDataToNbt(nbt);
        if (!this.getHeldItemStack().isEmpty()) {
            nbt.put("Item", this.getHeldItemStack().encode(this.getRegistryManager()));
            nbt.putByte("ItemRotation", (byte)this.getRotation());
            nbt.putFloat("ItemDropChance", this.itemDropChance);
        }
        nbt.putByte("Facing", (byte)this.facing.getId());
        nbt.putBoolean("Invisible", this.isInvisible());
        nbt.putBoolean("Fixed", this.fixed);
    }

    @Override
    public void readCustomDataFromNbt(NbtCompound nbt) {
        ItemStack lv2;
        super.readCustomDataFromNbt(nbt);
        if (nbt.contains("Item", NbtElement.COMPOUND_TYPE)) {
            NbtCompound lv = nbt.getCompound("Item");
            lv2 = ItemStack.fromNbt(this.getRegistryManager(), lv).orElse(ItemStack.EMPTY);
        } else {
            lv2 = ItemStack.EMPTY;
        }
        ItemStack lv3 = this.getHeldItemStack();
        if (!lv3.isEmpty() && !ItemStack.areEqual(lv2, lv3)) {
            this.removeFromFrame(lv3);
        }
        this.setHeldItemStack(lv2, false);
        if (!lv2.isEmpty()) {
            this.setRotation(nbt.getByte("ItemRotation"), false);
            if (nbt.contains("ItemDropChance", NbtElement.NUMBER_TYPE)) {
                this.itemDropChance = nbt.getFloat("ItemDropChance");
            }
        }
        this.setFacing(Direction.byId(nbt.getByte("Facing")));
        this.setInvisible(nbt.getBoolean("Invisible"));
        this.fixed = nbt.getBoolean("Fixed");
    }

    @Override
    public ActionResult interact(PlayerEntity player, Hand hand) {
        boolean bl2;
        ItemStack lv = player.getStackInHand(hand);
        boolean bl = !this.getHeldItemStack().isEmpty();
        boolean bl3 = bl2 = !lv.isEmpty();
        if (this.fixed) {
            return ActionResult.PASS;
        }
        if (this.getWorld().isClient) {
            return bl || bl2 ? ActionResult.SUCCESS : ActionResult.PASS;
        }
        if (!bl) {
            if (bl2 && !this.isRemoved()) {
                MapState lv2;
                if (lv.isOf(Items.FILLED_MAP) && (lv2 = FilledMapItem.getMapState(lv, this.getWorld())) != null && lv2.decorationCountNotLessThan(256)) {
                    return ActionResult.FAIL;
                }
                this.setHeldItemStack(lv);
                this.emitGameEvent(GameEvent.BLOCK_CHANGE, player);
                lv.decrementUnlessCreative(1, player);
            }
        } else {
            this.playSound(this.getRotateItemSound(), 1.0f, 1.0f);
            this.setRotation(this.getRotation() + 1);
            this.emitGameEvent(GameEvent.BLOCK_CHANGE, player);
        }
        return ActionResult.CONSUME;
    }

    public SoundEvent getRotateItemSound() {
        return SoundEvents.ENTITY_ITEM_FRAME_ROTATE_ITEM;
    }

    public int getComparatorPower() {
        if (this.getHeldItemStack().isEmpty()) {
            return 0;
        }
        return this.getRotation() % 8 + 1;
    }

    @Override
    public Packet<ClientPlayPacketListener> createSpawnPacket(EntityTrackerEntry entityTrackerEntry) {
        return new EntitySpawnS2CPacket((Entity)this, this.facing.getId(), this.getAttachedBlockPos());
    }

    @Override
    public void onSpawnPacket(EntitySpawnS2CPacket packet) {
        super.onSpawnPacket(packet);
        this.setFacing(Direction.byId(packet.getEntityData()));
    }

    @Override
    public ItemStack getPickBlockStack() {
        ItemStack lv = this.getHeldItemStack();
        if (lv.isEmpty()) {
            return this.getAsItemStack();
        }
        return lv.copy();
    }

    protected ItemStack getAsItemStack() {
        return new ItemStack(Items.ITEM_FRAME);
    }

    @Override
    public float getBodyYaw() {
        Direction lv = this.getHorizontalFacing();
        int i = lv.getAxis().isVertical() ? 90 * lv.getDirection().offset() : 0;
        return MathHelper.wrapDegrees(180 + lv.getHorizontal() * 90 + this.getRotation() * 45 + i);
    }
}

