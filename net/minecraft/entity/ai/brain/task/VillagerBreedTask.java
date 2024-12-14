/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.entity.ai.brain.task;

import com.google.common.collect.ImmutableMap;
import java.util.Optional;
import net.minecraft.entity.EntityStatuses;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.brain.Brain;
import net.minecraft.entity.ai.brain.MemoryModuleState;
import net.minecraft.entity.ai.brain.MemoryModuleType;
import net.minecraft.entity.ai.brain.task.LookTargetUtil;
import net.minecraft.entity.ai.brain.task.MultiTickTask;
import net.minecraft.entity.ai.pathing.Path;
import net.minecraft.entity.passive.PassiveEntity;
import net.minecraft.entity.passive.VillagerEntity;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.server.network.DebugInfoSender;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.GlobalPos;
import net.minecraft.world.poi.PointOfInterestType;
import net.minecraft.world.poi.PointOfInterestTypes;

public class VillagerBreedTask
extends MultiTickTask<VillagerEntity> {
    private long breedEndTime;

    public VillagerBreedTask() {
        super(ImmutableMap.of(MemoryModuleType.BREED_TARGET, MemoryModuleState.VALUE_PRESENT, MemoryModuleType.VISIBLE_MOBS, MemoryModuleState.VALUE_PRESENT), 350, 350);
    }

    @Override
    protected boolean shouldRun(ServerWorld arg, VillagerEntity arg2) {
        return this.isReadyToBreed(arg2);
    }

    @Override
    protected boolean shouldKeepRunning(ServerWorld arg, VillagerEntity arg2, long l) {
        return l <= this.breedEndTime && this.isReadyToBreed(arg2);
    }

    @Override
    protected void run(ServerWorld arg, VillagerEntity arg2, long l) {
        PassiveEntity lv = arg2.getBrain().getOptionalRegisteredMemory(MemoryModuleType.BREED_TARGET).get();
        LookTargetUtil.lookAtAndWalkTowardsEachOther(arg2, lv, 0.5f, 2);
        arg.sendEntityStatus(lv, EntityStatuses.ADD_BREEDING_PARTICLES);
        arg.sendEntityStatus(arg2, EntityStatuses.ADD_BREEDING_PARTICLES);
        int i = 275 + arg2.getRandom().nextInt(50);
        this.breedEndTime = l + (long)i;
    }

    @Override
    protected void keepRunning(ServerWorld arg, VillagerEntity arg2, long l) {
        VillagerEntity lv = (VillagerEntity)arg2.getBrain().getOptionalRegisteredMemory(MemoryModuleType.BREED_TARGET).get();
        if (arg2.squaredDistanceTo(lv) > 5.0) {
            return;
        }
        LookTargetUtil.lookAtAndWalkTowardsEachOther(arg2, lv, 0.5f, 2);
        if (l >= this.breedEndTime) {
            arg2.eatForBreeding();
            lv.eatForBreeding();
            this.goHome(arg, arg2, lv);
        } else if (arg2.getRandom().nextInt(35) == 0) {
            arg.sendEntityStatus(lv, EntityStatuses.ADD_VILLAGER_HEART_PARTICLES);
            arg.sendEntityStatus(arg2, EntityStatuses.ADD_VILLAGER_HEART_PARTICLES);
        }
    }

    private void goHome(ServerWorld world, VillagerEntity first, VillagerEntity second) {
        Optional<BlockPos> optional = this.getReachableHome(world, first);
        if (optional.isEmpty()) {
            world.sendEntityStatus(second, EntityStatuses.ADD_VILLAGER_ANGRY_PARTICLES);
            world.sendEntityStatus(first, EntityStatuses.ADD_VILLAGER_ANGRY_PARTICLES);
        } else {
            Optional<VillagerEntity> optional2 = this.createChild(world, first, second);
            if (optional2.isPresent()) {
                this.setChildHome(world, optional2.get(), optional.get());
            } else {
                world.getPointOfInterestStorage().releaseTicket(optional.get());
                DebugInfoSender.sendPointOfInterest(world, optional.get());
            }
        }
    }

    @Override
    protected void finishRunning(ServerWorld arg, VillagerEntity arg2, long l) {
        arg2.getBrain().forget(MemoryModuleType.BREED_TARGET);
    }

    private boolean isReadyToBreed(VillagerEntity villager) {
        Brain<VillagerEntity> lv = villager.getBrain();
        Optional<PassiveEntity> optional = lv.getOptionalRegisteredMemory(MemoryModuleType.BREED_TARGET).filter(arg -> arg.getType() == EntityType.VILLAGER);
        if (optional.isEmpty()) {
            return false;
        }
        return LookTargetUtil.canSee(lv, MemoryModuleType.BREED_TARGET, EntityType.VILLAGER) && villager.isReadyToBreed() && optional.get().isReadyToBreed();
    }

    private Optional<BlockPos> getReachableHome(ServerWorld world, VillagerEntity villager) {
        return world.getPointOfInterestStorage().getPosition(poiType -> poiType.matchesKey(PointOfInterestTypes.HOME), (poiType, pos) -> this.canReachHome(villager, (BlockPos)pos, (RegistryEntry<PointOfInterestType>)poiType), villager.getBlockPos(), 48);
    }

    private boolean canReachHome(VillagerEntity villager, BlockPos pos, RegistryEntry<PointOfInterestType> poiType) {
        Path lv = villager.getNavigation().findPathTo(pos, poiType.value().searchDistance());
        return lv != null && lv.reachesTarget();
    }

    private Optional<VillagerEntity> createChild(ServerWorld world, VillagerEntity parent, VillagerEntity partner) {
        VillagerEntity lv = parent.createChild(world, partner);
        if (lv == null) {
            return Optional.empty();
        }
        parent.setBreedingAge(6000);
        partner.setBreedingAge(6000);
        lv.setBreedingAge(-24000);
        lv.refreshPositionAndAngles(parent.getX(), parent.getY(), parent.getZ(), 0.0f, 0.0f);
        world.spawnEntityAndPassengers(lv);
        world.sendEntityStatus(lv, EntityStatuses.ADD_VILLAGER_HEART_PARTICLES);
        return Optional.of(lv);
    }

    private void setChildHome(ServerWorld world, VillagerEntity child, BlockPos pos) {
        GlobalPos lv = GlobalPos.create(world.getRegistryKey(), pos);
        child.getBrain().remember(MemoryModuleType.HOME, lv);
    }

    @Override
    protected /* synthetic */ void finishRunning(ServerWorld world, LivingEntity entity, long time) {
        this.finishRunning(world, (VillagerEntity)entity, time);
    }

    @Override
    protected /* synthetic */ void run(ServerWorld world, LivingEntity entity, long time) {
        this.run(world, (VillagerEntity)entity, time);
    }
}

