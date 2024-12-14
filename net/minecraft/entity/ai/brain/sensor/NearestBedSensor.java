/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.entity.ai.brain.sensor;

import com.google.common.collect.ImmutableSet;
import com.mojang.datafixers.util.Pair;
import it.unimi.dsi.fastutil.longs.Long2LongMap;
import it.unimi.dsi.fastutil.longs.Long2LongOpenHashMap;
import java.util.Optional;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import net.minecraft.entity.ai.brain.MemoryModuleType;
import net.minecraft.entity.ai.brain.sensor.Sensor;
import net.minecraft.entity.ai.brain.task.FindPointOfInterestTask;
import net.minecraft.entity.ai.pathing.Path;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.poi.PointOfInterestStorage;
import net.minecraft.world.poi.PointOfInterestType;
import net.minecraft.world.poi.PointOfInterestTypes;

public class NearestBedSensor
extends Sensor<MobEntity> {
    private static final int REMEMBER_TIME = 40;
    private static final int MAX_TRIES = 5;
    private static final int MAX_EXPIRY_TIME = 20;
    private final Long2LongMap positionToExpiryTime = new Long2LongOpenHashMap();
    private int tries;
    private long expiryTime;

    public NearestBedSensor() {
        super(20);
    }

    @Override
    public Set<MemoryModuleType<?>> getOutputMemoryModules() {
        return ImmutableSet.of(MemoryModuleType.NEAREST_BED);
    }

    @Override
    protected void sense(ServerWorld arg2, MobEntity arg22) {
        Predicate<BlockPos> predicate;
        if (!arg22.isBaby()) {
            return;
        }
        this.tries = 0;
        this.expiryTime = arg2.getTime() + (long)arg2.getRandom().nextInt(20);
        PointOfInterestStorage lv = arg2.getPointOfInterestStorage();
        Set<Pair<RegistryEntry<PointOfInterestType>, BlockPos>> set = lv.getTypesAndPositions(arg -> arg.matchesKey(PointOfInterestTypes.HOME), predicate = pos -> {
            long l = pos.asLong();
            if (this.positionToExpiryTime.containsKey(l)) {
                return false;
            }
            if (++this.tries >= 5) {
                return false;
            }
            this.positionToExpiryTime.put(l, this.expiryTime + 40L);
            return true;
        }, arg22.getBlockPos(), 48, PointOfInterestStorage.OccupationStatus.ANY).collect(Collectors.toSet());
        Path lv2 = FindPointOfInterestTask.findPathToPoi(arg22, set);
        if (lv2 != null && lv2.reachesTarget()) {
            BlockPos lv3 = lv2.getTarget();
            Optional<RegistryEntry<PointOfInterestType>> optional = lv.getType(lv3);
            if (optional.isPresent()) {
                arg22.getBrain().remember(MemoryModuleType.NEAREST_BED, lv3);
            }
        } else if (this.tries < 5) {
            this.positionToExpiryTime.long2LongEntrySet().removeIf(entry -> entry.getLongValue() < this.expiryTime);
        }
    }
}

