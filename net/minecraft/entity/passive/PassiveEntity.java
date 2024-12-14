/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 * 
 * Could not load the following classes:
 *  org.jetbrains.annotations.Nullable
 */
package net.minecraft.entity.passive;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityData;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.SpawnReason;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.data.TrackedData;
import net.minecraft.entity.data.TrackedDataHandlerRegistry;
import net.minecraft.entity.mob.PathAwareEntity;
import net.minecraft.entity.vehicle.BoatEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.world.LocalDifficulty;
import net.minecraft.world.ServerWorldAccess;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

public abstract class PassiveEntity
extends PathAwareEntity {
    private static final TrackedData<Boolean> CHILD = DataTracker.registerData(PassiveEntity.class, TrackedDataHandlerRegistry.BOOLEAN);
    public static final int BABY_AGE = -24000;
    private static final int HAPPY_TICKS = 40;
    protected int breedingAge;
    protected int forcedAge;
    protected int happyTicksRemaining;

    protected PassiveEntity(EntityType<? extends PassiveEntity> arg, World arg2) {
        super((EntityType<? extends PathAwareEntity>)arg, arg2);
    }

    @Override
    public EntityData initialize(ServerWorldAccess world, LocalDifficulty difficulty, SpawnReason spawnReason, @Nullable EntityData entityData) {
        PassiveData lv;
        if (entityData == null) {
            entityData = new PassiveData(true);
        }
        if ((lv = (PassiveData)entityData).canSpawnBaby() && lv.getSpawnedCount() > 0 && world.getRandom().nextFloat() <= lv.getBabyChance()) {
            this.setBreedingAge(-24000);
        }
        lv.countSpawned();
        return super.initialize(world, difficulty, spawnReason, entityData);
    }

    @Nullable
    public abstract PassiveEntity createChild(ServerWorld var1, PassiveEntity var2);

    @Override
    protected void initDataTracker(DataTracker.Builder builder) {
        super.initDataTracker(builder);
        builder.add(CHILD, false);
    }

    public boolean isReadyToBreed() {
        return false;
    }

    public int getBreedingAge() {
        if (this.getWorld().isClient) {
            return this.dataTracker.get(CHILD) != false ? -1 : 1;
        }
        return this.breedingAge;
    }

    public void growUp(int age, boolean overGrow) {
        int j;
        int k = j = this.getBreedingAge();
        if ((j += age * 20) > 0) {
            j = 0;
        }
        int l = j - k;
        this.setBreedingAge(j);
        if (overGrow) {
            this.forcedAge += l;
            if (this.happyTicksRemaining == 0) {
                this.happyTicksRemaining = 40;
            }
        }
        if (this.getBreedingAge() == 0) {
            this.setBreedingAge(this.forcedAge);
        }
    }

    public void growUp(int age) {
        this.growUp(age, false);
    }

    public void setBreedingAge(int age) {
        int j = this.getBreedingAge();
        this.breedingAge = age;
        if (j < 0 && age >= 0 || j >= 0 && age < 0) {
            this.dataTracker.set(CHILD, age < 0);
            this.onGrowUp();
        }
    }

    @Override
    public void writeCustomDataToNbt(NbtCompound nbt) {
        super.writeCustomDataToNbt(nbt);
        nbt.putInt("Age", this.getBreedingAge());
        nbt.putInt("ForcedAge", this.forcedAge);
    }

    @Override
    public void readCustomDataFromNbt(NbtCompound nbt) {
        super.readCustomDataFromNbt(nbt);
        this.setBreedingAge(nbt.getInt("Age"));
        this.forcedAge = nbt.getInt("ForcedAge");
    }

    @Override
    public void onTrackedDataSet(TrackedData<?> data) {
        if (CHILD.equals(data)) {
            this.calculateDimensions();
        }
        super.onTrackedDataSet(data);
    }

    @Override
    public void tickMovement() {
        super.tickMovement();
        if (this.getWorld().isClient) {
            if (this.happyTicksRemaining > 0) {
                if (this.happyTicksRemaining % 4 == 0) {
                    this.getWorld().addParticle(ParticleTypes.HAPPY_VILLAGER, this.getParticleX(1.0), this.getRandomBodyY() + 0.5, this.getParticleZ(1.0), 0.0, 0.0, 0.0);
                }
                --this.happyTicksRemaining;
            }
        } else if (this.isAlive()) {
            int i = this.getBreedingAge();
            if (i < 0) {
                this.setBreedingAge(++i);
            } else if (i > 0) {
                this.setBreedingAge(--i);
            }
        }
    }

    protected void onGrowUp() {
        BoatEntity lv;
        Entity entity;
        if (!this.isBaby() && this.hasVehicle() && (entity = this.getVehicle()) instanceof BoatEntity && !(lv = (BoatEntity)entity).isSmallerThanBoat(this)) {
            this.stopRiding();
        }
    }

    @Override
    public boolean isBaby() {
        return this.getBreedingAge() < 0;
    }

    @Override
    public void setBaby(boolean baby) {
        this.setBreedingAge(baby ? -24000 : 0);
    }

    public static int toGrowUpAge(int breedingAge) {
        return (int)((float)(breedingAge / 20) * 0.1f);
    }

    public static class PassiveData
    implements EntityData {
        private int spawnCount;
        private final boolean babyAllowed;
        private final float babyChance;

        private PassiveData(boolean babyAllowed, float babyChance) {
            this.babyAllowed = babyAllowed;
            this.babyChance = babyChance;
        }

        public PassiveData(boolean babyAllowed) {
            this(babyAllowed, 0.05f);
        }

        public PassiveData(float babyChance) {
            this(true, babyChance);
        }

        public int getSpawnedCount() {
            return this.spawnCount;
        }

        public void countSpawned() {
            ++this.spawnCount;
        }

        public boolean canSpawnBaby() {
            return this.babyAllowed;
        }

        public float getBabyChance() {
            return this.babyChance;
        }
    }
}

