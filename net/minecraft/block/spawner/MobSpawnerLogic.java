/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 * 
 * Could not load the following classes:
 *  org.jetbrains.annotations.Nullable
 */
package net.minecraft.block.spawner;

import com.mojang.logging.LogUtils;
import java.util.Optional;
import java.util.function.Function;
import net.minecraft.block.spawner.MobSpawnerEntry;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.SpawnReason;
import net.minecraft.entity.SpawnRestriction;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtList;
import net.minecraft.nbt.NbtOps;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.predicate.entity.EntityPredicates;
import net.minecraft.registry.Registries;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.TypeFilter;
import net.minecraft.util.collection.DataPool;
import net.minecraft.util.collection.Weighted;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.Difficulty;
import net.minecraft.world.World;
import net.minecraft.world.WorldEvents;
import net.minecraft.world.event.GameEvent;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;

public abstract class MobSpawnerLogic {
    public static final String SPAWN_DATA_KEY = "SpawnData";
    private static final Logger LOGGER = LogUtils.getLogger();
    private static final int field_30951 = 1;
    private int spawnDelay = 20;
    private DataPool<MobSpawnerEntry> spawnPotentials = DataPool.empty();
    @Nullable
    private MobSpawnerEntry spawnEntry;
    private double rotation;
    private double lastRotation;
    private int minSpawnDelay = 200;
    private int maxSpawnDelay = 800;
    private int spawnCount = 4;
    @Nullable
    private Entity renderedEntity;
    private int maxNearbyEntities = 6;
    private int requiredPlayerRange = 16;
    private int spawnRange = 4;

    public void setEntityId(EntityType<?> type, @Nullable World world, Random random, BlockPos pos) {
        this.getSpawnEntry(world, random, pos).getNbt().putString("id", Registries.ENTITY_TYPE.getId(type).toString());
    }

    private boolean isPlayerInRange(World world, BlockPos pos) {
        return world.isPlayerInRange((double)pos.getX() + 0.5, (double)pos.getY() + 0.5, (double)pos.getZ() + 0.5, this.requiredPlayerRange);
    }

    public void clientTick(World world, BlockPos pos) {
        if (!this.isPlayerInRange(world, pos)) {
            this.lastRotation = this.rotation;
        } else if (this.renderedEntity != null) {
            Random lv = world.getRandom();
            double d = (double)pos.getX() + lv.nextDouble();
            double e = (double)pos.getY() + lv.nextDouble();
            double f = (double)pos.getZ() + lv.nextDouble();
            world.addParticle(ParticleTypes.SMOKE, d, e, f, 0.0, 0.0, 0.0);
            world.addParticle(ParticleTypes.FLAME, d, e, f, 0.0, 0.0, 0.0);
            if (this.spawnDelay > 0) {
                --this.spawnDelay;
            }
            this.lastRotation = this.rotation;
            this.rotation = (this.rotation + (double)(1000.0f / ((float)this.spawnDelay + 200.0f))) % 360.0;
        }
    }

    public void serverTick(ServerWorld world, BlockPos pos) {
        if (!this.isPlayerInRange(world, pos)) {
            return;
        }
        if (this.spawnDelay == -1) {
            this.updateSpawns(world, pos);
        }
        if (this.spawnDelay > 0) {
            --this.spawnDelay;
            return;
        }
        boolean bl = false;
        Random lv = world.getRandom();
        MobSpawnerEntry lv2 = this.getSpawnEntry(world, lv, pos);
        for (int i = 0; i < this.spawnCount; ++i) {
            MobSpawnerEntry.CustomSpawnRules lv6;
            double f;
            NbtCompound lv3 = lv2.getNbt();
            Optional<EntityType<?>> optional = EntityType.fromNbt(lv3);
            if (optional.isEmpty()) {
                this.updateSpawns(world, pos);
                return;
            }
            NbtList lv4 = lv3.getList("Pos", NbtElement.DOUBLE_TYPE);
            int j = lv4.size();
            double d = j >= 1 ? lv4.getDouble(0) : (double)pos.getX() + (lv.nextDouble() - lv.nextDouble()) * (double)this.spawnRange + 0.5;
            double e = j >= 2 ? lv4.getDouble(1) : (double)(pos.getY() + lv.nextInt(3) - 1);
            double d2 = f = j >= 3 ? lv4.getDouble(2) : (double)pos.getZ() + (lv.nextDouble() - lv.nextDouble()) * (double)this.spawnRange + 0.5;
            if (!world.isSpaceEmpty(optional.get().getSpawnBox(d, e, f))) continue;
            BlockPos lv5 = BlockPos.ofFloored(d, e, f);
            if (!lv2.getCustomSpawnRules().isPresent() ? !SpawnRestriction.canSpawn(optional.get(), world, SpawnReason.SPAWNER, lv5, world.getRandom()) : !optional.get().getSpawnGroup().isPeaceful() && world.getDifficulty() == Difficulty.PEACEFUL || !(lv6 = lv2.getCustomSpawnRules().get()).canSpawn(lv5, world)) continue;
            Entity lv7 = EntityType.loadEntityWithPassengers(lv3, world, entity -> {
                entity.refreshPositionAndAngles(d, e, f, entity.getYaw(), entity.getPitch());
                return entity;
            });
            if (lv7 == null) {
                this.updateSpawns(world, pos);
                return;
            }
            int k = world.getEntitiesByType(TypeFilter.equals(lv7.getClass()), new Box(pos.getX(), pos.getY(), pos.getZ(), pos.getX() + 1, pos.getY() + 1, pos.getZ() + 1).expand(this.spawnRange), EntityPredicates.EXCEPT_SPECTATOR).size();
            if (k >= this.maxNearbyEntities) {
                this.updateSpawns(world, pos);
                return;
            }
            lv7.refreshPositionAndAngles(lv7.getX(), lv7.getY(), lv7.getZ(), lv.nextFloat() * 360.0f, 0.0f);
            if (lv7 instanceof MobEntity) {
                boolean bl2;
                MobEntity lv8 = (MobEntity)lv7;
                if (lv2.getCustomSpawnRules().isEmpty() && !lv8.canSpawn(world, SpawnReason.SPAWNER) || !lv8.canSpawn(world)) continue;
                boolean bl3 = bl2 = lv2.getNbt().getSize() == 1 && lv2.getNbt().contains("id", NbtElement.STRING_TYPE);
                if (bl2) {
                    ((MobEntity)lv7).initialize(world, world.getLocalDifficulty(lv7.getBlockPos()), SpawnReason.SPAWNER, null);
                }
                lv2.getEquipment().ifPresent(lv8::setEquipmentFromTable);
            }
            if (!world.spawnNewEntityAndPassengers(lv7)) {
                this.updateSpawns(world, pos);
                return;
            }
            world.syncWorldEvent(WorldEvents.SPAWNER_SPAWNS_MOB, pos, 0);
            world.emitGameEvent(lv7, GameEvent.ENTITY_PLACE, lv5);
            if (lv7 instanceof MobEntity) {
                ((MobEntity)lv7).playSpawnEffects();
            }
            bl = true;
        }
        if (bl) {
            this.updateSpawns(world, pos);
        }
    }

    private void updateSpawns(World world, BlockPos pos) {
        Random lv = world.random;
        this.spawnDelay = this.maxSpawnDelay <= this.minSpawnDelay ? this.minSpawnDelay : this.minSpawnDelay + lv.nextInt(this.maxSpawnDelay - this.minSpawnDelay);
        this.spawnPotentials.getOrEmpty(lv).ifPresent(spawnPotential -> this.setSpawnEntry(world, pos, (MobSpawnerEntry)spawnPotential.data()));
        this.sendStatus(world, pos, 1);
    }

    public void readNbt(@Nullable World world, BlockPos pos, NbtCompound nbt) {
        boolean bl2;
        this.spawnDelay = nbt.getShort("Delay");
        boolean bl = nbt.contains(SPAWN_DATA_KEY, NbtElement.COMPOUND_TYPE);
        if (bl) {
            MobSpawnerEntry lv = MobSpawnerEntry.CODEC.parse(NbtOps.INSTANCE, nbt.getCompound(SPAWN_DATA_KEY)).resultOrPartial(string -> LOGGER.warn("Invalid SpawnData: {}", string)).orElseGet(MobSpawnerEntry::new);
            this.setSpawnEntry(world, pos, lv);
        }
        if (bl2 = nbt.contains("SpawnPotentials", NbtElement.LIST_TYPE)) {
            NbtList lv2 = nbt.getList("SpawnPotentials", NbtElement.COMPOUND_TYPE);
            this.spawnPotentials = MobSpawnerEntry.DATA_POOL_CODEC.parse(NbtOps.INSTANCE, lv2).resultOrPartial(error -> LOGGER.warn("Invalid SpawnPotentials list: {}", error)).orElseGet(DataPool::empty);
        } else {
            this.spawnPotentials = DataPool.of(this.spawnEntry != null ? this.spawnEntry : new MobSpawnerEntry());
        }
        if (nbt.contains("MinSpawnDelay", NbtElement.NUMBER_TYPE)) {
            this.minSpawnDelay = nbt.getShort("MinSpawnDelay");
            this.maxSpawnDelay = nbt.getShort("MaxSpawnDelay");
            this.spawnCount = nbt.getShort("SpawnCount");
        }
        if (nbt.contains("MaxNearbyEntities", NbtElement.NUMBER_TYPE)) {
            this.maxNearbyEntities = nbt.getShort("MaxNearbyEntities");
            this.requiredPlayerRange = nbt.getShort("RequiredPlayerRange");
        }
        if (nbt.contains("SpawnRange", NbtElement.NUMBER_TYPE)) {
            this.spawnRange = nbt.getShort("SpawnRange");
        }
        this.renderedEntity = null;
    }

    public NbtCompound writeNbt(NbtCompound nbt) {
        nbt.putShort("Delay", (short)this.spawnDelay);
        nbt.putShort("MinSpawnDelay", (short)this.minSpawnDelay);
        nbt.putShort("MaxSpawnDelay", (short)this.maxSpawnDelay);
        nbt.putShort("SpawnCount", (short)this.spawnCount);
        nbt.putShort("MaxNearbyEntities", (short)this.maxNearbyEntities);
        nbt.putShort("RequiredPlayerRange", (short)this.requiredPlayerRange);
        nbt.putShort("SpawnRange", (short)this.spawnRange);
        if (this.spawnEntry != null) {
            nbt.put(SPAWN_DATA_KEY, MobSpawnerEntry.CODEC.encodeStart(NbtOps.INSTANCE, this.spawnEntry).getOrThrow(string -> new IllegalStateException("Invalid SpawnData: " + string)));
        }
        nbt.put("SpawnPotentials", MobSpawnerEntry.DATA_POOL_CODEC.encodeStart(NbtOps.INSTANCE, this.spawnPotentials).getOrThrow());
        return nbt;
    }

    @Nullable
    public Entity getRenderedEntity(World world, BlockPos pos) {
        if (this.renderedEntity == null) {
            NbtCompound lv = this.getSpawnEntry(world, world.getRandom(), pos).getNbt();
            if (!lv.contains("id", NbtElement.STRING_TYPE)) {
                return null;
            }
            this.renderedEntity = EntityType.loadEntityWithPassengers(lv, world, Function.identity());
            if (lv.getSize() != 1 || this.renderedEntity instanceof MobEntity) {
                // empty if block
            }
        }
        return this.renderedEntity;
    }

    public boolean handleStatus(World world, int status) {
        if (status == 1) {
            if (world.isClient) {
                this.spawnDelay = this.minSpawnDelay;
            }
            return true;
        }
        return false;
    }

    protected void setSpawnEntry(@Nullable World world, BlockPos pos, MobSpawnerEntry spawnEntry) {
        this.spawnEntry = spawnEntry;
    }

    private MobSpawnerEntry getSpawnEntry(@Nullable World world, Random random, BlockPos pos) {
        if (this.spawnEntry != null) {
            return this.spawnEntry;
        }
        this.setSpawnEntry(world, pos, this.spawnPotentials.getOrEmpty(random).map(Weighted.Present::data).orElseGet(MobSpawnerEntry::new));
        return this.spawnEntry;
    }

    public abstract void sendStatus(World var1, BlockPos var2, int var3);

    public double getRotation() {
        return this.rotation;
    }

    public double getLastRotation() {
        return this.lastRotation;
    }
}

