/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.entity.projectile.thrown;

import net.minecraft.entity.EntityType;
import net.minecraft.entity.FlyingItemEntity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.data.TrackedData;
import net.minecraft.entity.data.TrackedDataHandlerRegistry;
import net.minecraft.entity.projectile.thrown.ThrownEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.world.World;

public abstract class ThrownItemEntity
extends ThrownEntity
implements FlyingItemEntity {
    private static final TrackedData<ItemStack> ITEM = DataTracker.registerData(ThrownItemEntity.class, TrackedDataHandlerRegistry.ITEM_STACK);

    public ThrownItemEntity(EntityType<? extends ThrownItemEntity> arg, World arg2) {
        super((EntityType<? extends ThrownEntity>)arg, arg2);
    }

    public ThrownItemEntity(EntityType<? extends ThrownItemEntity> arg, double d, double e, double f, World arg2) {
        super(arg, d, e, f, arg2);
    }

    public ThrownItemEntity(EntityType<? extends ThrownItemEntity> arg, LivingEntity arg2, World arg3) {
        super(arg, arg2, arg3);
    }

    public void setItem(ItemStack stack) {
        this.getDataTracker().set(ITEM, stack.copyWithCount(1));
    }

    protected abstract Item getDefaultItem();

    @Override
    public ItemStack getStack() {
        return this.getDataTracker().get(ITEM);
    }

    @Override
    protected void initDataTracker(DataTracker.Builder builder) {
        builder.add(ITEM, new ItemStack(this.getDefaultItem()));
    }

    @Override
    public void writeCustomDataToNbt(NbtCompound nbt) {
        super.writeCustomDataToNbt(nbt);
        nbt.put("Item", this.getStack().encode(this.getRegistryManager()));
    }

    @Override
    public void readCustomDataFromNbt(NbtCompound nbt) {
        super.readCustomDataFromNbt(nbt);
        if (nbt.contains("Item", NbtElement.COMPOUND_TYPE)) {
            this.setItem(ItemStack.fromNbt(this.getRegistryManager(), nbt.getCompound("Item")).orElseGet(() -> new ItemStack(this.getDefaultItem())));
        } else {
            this.setItem(new ItemStack(this.getDefaultItem()));
        }
    }
}

