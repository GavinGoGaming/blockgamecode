/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.entity;

import net.minecraft.block.piston.PistonBehavior;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.data.TrackedData;
import net.minecraft.entity.data.TrackedDataHandlerRegistry;
import net.minecraft.entity.projectile.ProjectileEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ProjectileItem;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraft.world.WorldEvents;
import net.minecraft.world.event.GameEvent;

public class OminousItemSpawnerEntity
extends Entity {
    private static final int MIN_SPAWN_ITEM_AFTER_TICKS = 60;
    private static final int MAX_SPAWN_ITEM_AFTER_TICKS = 120;
    private static final String SPAWN_ITEM_AFTER_TICKS_NBT_KEY = "spawn_item_after_ticks";
    private static final String ITEM_NBT_KEY = "item";
    private static final TrackedData<ItemStack> ITEM = DataTracker.registerData(OminousItemSpawnerEntity.class, TrackedDataHandlerRegistry.ITEM_STACK);
    public static final int field_50128 = 36;
    private long spawnItemAfterTicks;

    public OminousItemSpawnerEntity(EntityType<? extends OminousItemSpawnerEntity> arg, World arg2) {
        super(arg, arg2);
        this.noClip = true;
    }

    public static OminousItemSpawnerEntity create(World world, ItemStack stack) {
        OminousItemSpawnerEntity lv = new OminousItemSpawnerEntity((EntityType<? extends OminousItemSpawnerEntity>)EntityType.OMINOUS_ITEM_SPAWNER, world);
        lv.spawnItemAfterTicks = world.random.nextBetween(60, 120);
        lv.setItem(stack);
        return lv;
    }

    @Override
    public void tick() {
        super.tick();
        if (this.getWorld().isClient) {
            this.tickClient();
            return;
        }
        this.tickServer();
    }

    private void tickServer() {
        if ((long)this.age == this.spawnItemAfterTicks - 36L) {
            this.getWorld().playSound(null, this.getBlockPos(), SoundEvents.BLOCK_TRIAL_SPAWNER_ABOUT_TO_SPAWN_ITEM, SoundCategory.NEUTRAL);
        }
        if ((long)this.age >= this.spawnItemAfterTicks) {
            this.spawnItem();
            this.kill();
        }
    }

    private void tickClient() {
        if (this.getWorld().getTime() % 5L == 0L) {
            this.addParticles();
        }
    }

    private void spawnItem() {
        Entity lv7;
        World lv = this.getWorld();
        ItemStack lv2 = this.getItem();
        if (lv2.isEmpty()) {
            return;
        }
        Item item = lv2.getItem();
        if (item instanceof ProjectileItem) {
            ProjectileItem lv3 = (ProjectileItem)((Object)item);
            Direction lv4 = Direction.DOWN;
            ProjectileEntity lv5 = lv3.createEntity(lv, this.getPos(), lv2, lv4);
            lv5.setOwner(this);
            ProjectileItem.Settings lv6 = lv3.getProjectileSettings();
            lv3.initializeProjectile(lv5, lv4.getOffsetX(), lv4.getOffsetY(), lv4.getOffsetZ(), lv6.power(), lv6.uncertainty());
            lv6.overrideDispenseEvent().ifPresent(event -> lv.syncWorldEvent(event, this.getBlockPos(), 0));
            lv7 = lv5;
        } else {
            lv7 = new ItemEntity(lv, this.getX(), this.getY(), this.getZ(), lv2);
        }
        lv.spawnEntity(lv7);
        lv.syncWorldEvent(WorldEvents.OMINOUS_ITEM_SPAWNER_SPAWNS_ITEM, this.getBlockPos(), 1);
        lv.emitGameEvent(lv7, GameEvent.ENTITY_PLACE, this.getPos());
        this.setItem(ItemStack.EMPTY);
    }

    @Override
    protected void initDataTracker(DataTracker.Builder builder) {
        builder.add(ITEM, ItemStack.EMPTY);
    }

    @Override
    protected void readCustomDataFromNbt(NbtCompound nbt) {
        ItemStack lv = nbt.contains(ITEM_NBT_KEY, NbtElement.COMPOUND_TYPE) ? ItemStack.fromNbt(this.getRegistryManager(), nbt.getCompound(ITEM_NBT_KEY)).orElse(ItemStack.EMPTY) : ItemStack.EMPTY;
        this.setItem(lv);
        this.spawnItemAfterTicks = nbt.getLong(SPAWN_ITEM_AFTER_TICKS_NBT_KEY);
    }

    @Override
    protected void writeCustomDataToNbt(NbtCompound nbt) {
        if (!this.getItem().isEmpty()) {
            nbt.put(ITEM_NBT_KEY, this.getItem().encode(this.getRegistryManager()).copy());
        }
        nbt.putLong(SPAWN_ITEM_AFTER_TICKS_NBT_KEY, this.spawnItemAfterTicks);
    }

    @Override
    protected boolean canAddPassenger(Entity passenger) {
        return false;
    }

    @Override
    protected boolean couldAcceptPassenger() {
        return false;
    }

    @Override
    protected void addPassenger(Entity passenger) {
        throw new IllegalStateException("Should never addPassenger without checking couldAcceptPassenger()");
    }

    @Override
    public PistonBehavior getPistonBehavior() {
        return PistonBehavior.IGNORE;
    }

    @Override
    public boolean canAvoidTraps() {
        return true;
    }

    public void addParticles() {
        Vec3d lv = this.getPos();
        int i = this.random.nextBetween(1, 3);
        for (int j = 0; j < i; ++j) {
            double d = 0.4;
            Vec3d lv2 = new Vec3d(this.getX() + 0.4 * (this.random.nextGaussian() - this.random.nextGaussian()), this.getY() + 0.4 * (this.random.nextGaussian() - this.random.nextGaussian()), this.getZ() + 0.4 * (this.random.nextGaussian() - this.random.nextGaussian()));
            Vec3d lv3 = lv.relativize(lv2);
            this.getWorld().addParticle(ParticleTypes.OMINOUS_SPAWNING, lv.getX(), lv.getY(), lv.getZ(), lv3.getX(), lv3.getY(), lv3.getZ());
        }
    }

    public ItemStack getItem() {
        return this.getDataTracker().get(ITEM);
    }

    private void setItem(ItemStack stack) {
        this.getDataTracker().set(ITEM, stack);
    }
}

