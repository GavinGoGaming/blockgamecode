/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 * 
 * Could not load the following classes:
 *  org.jetbrains.annotations.Nullable
 */
package net.minecraft.entity.ai.brain.task;

import com.google.common.collect.Sets;
import com.mojang.datafixers.kinds.OptionalBox;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import net.minecraft.block.BlockState;
import net.minecraft.block.DoorBlock;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.brain.Brain;
import net.minecraft.entity.ai.brain.MemoryModuleType;
import net.minecraft.entity.ai.brain.MemoryQueryResult;
import net.minecraft.entity.ai.brain.task.Task;
import net.minecraft.entity.ai.brain.task.TaskTriggerer;
import net.minecraft.entity.ai.pathing.Path;
import net.minecraft.entity.ai.pathing.PathNode;
import net.minecraft.registry.tag.BlockTags;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.GlobalPos;
import org.apache.commons.lang3.mutable.MutableInt;
import org.apache.commons.lang3.mutable.MutableObject;
import org.jetbrains.annotations.Nullable;

public class OpenDoorsTask {
    private static final int RUN_TIME = 20;
    private static final double PATHING_DISTANCE = 3.0;
    private static final double REACH_DISTANCE = 2.0;

    public static Task<LivingEntity> create() {
        MutableObject<Object> mutableObject = new MutableObject<Object>(null);
        MutableInt mutableInt = new MutableInt(0);
        return TaskTriggerer.task(context -> context.group(context.queryMemoryValue(MemoryModuleType.PATH), context.queryMemoryOptional(MemoryModuleType.DOORS_TO_CLOSE), context.queryMemoryOptional(MemoryModuleType.MOBS)).apply(context, (path, doorsToClose, mobs) -> (world, entity, time) -> {
            DoorBlock lv9;
            BlockPos lv7;
            BlockState lv8;
            Path lv = (Path)context.getValue(path);
            Optional<Set<GlobalPos>> optional = context.getOptionalValue(doorsToClose);
            if (lv.isStart() || lv.isFinished()) {
                return false;
            }
            if (Objects.equals(mutableObject.getValue(), lv.getCurrentNode())) {
                mutableInt.setValue(20);
            } else if (mutableInt.decrementAndGet() > 0) {
                return false;
            }
            mutableObject.setValue(lv.getCurrentNode());
            PathNode lv2 = lv.getLastNode();
            PathNode lv3 = lv.getCurrentNode();
            BlockPos lv4 = lv2.getBlockPos();
            BlockState lv5 = world.getBlockState(lv4);
            if (lv5.isIn(BlockTags.MOB_INTERACTABLE_DOORS, state -> state.getBlock() instanceof DoorBlock)) {
                DoorBlock lv6 = (DoorBlock)lv5.getBlock();
                if (!lv6.isOpen(lv5)) {
                    lv6.setOpen(entity, world, lv5, lv4, true);
                }
                optional = OpenDoorsTask.storePos(doorsToClose, optional, world, lv4);
            }
            if ((lv8 = world.getBlockState(lv7 = lv3.getBlockPos())).isIn(BlockTags.MOB_INTERACTABLE_DOORS, state -> state.getBlock() instanceof DoorBlock) && !(lv9 = (DoorBlock)lv8.getBlock()).isOpen(lv8)) {
                lv9.setOpen(entity, world, lv8, lv7, true);
                optional = OpenDoorsTask.storePos(doorsToClose, optional, world, lv7);
            }
            optional.ifPresent(doors -> OpenDoorsTask.pathToDoor(world, entity, lv2, lv3, doors, context.getOptionalValue(mobs)));
            return true;
        }));
    }

    public static void pathToDoor(ServerWorld world, LivingEntity entity, @Nullable PathNode lastNode, @Nullable PathNode currentNode, Set<GlobalPos> doors, Optional<List<LivingEntity>> otherMobs) {
        Iterator<GlobalPos> iterator = doors.iterator();
        while (iterator.hasNext()) {
            GlobalPos lv = iterator.next();
            BlockPos lv2 = lv.pos();
            if (lastNode != null && lastNode.getBlockPos().equals(lv2) || currentNode != null && currentNode.getBlockPos().equals(lv2)) continue;
            if (OpenDoorsTask.cannotReachDoor(world, entity, lv)) {
                iterator.remove();
                continue;
            }
            BlockState lv3 = world.getBlockState(lv2);
            if (!lv3.isIn(BlockTags.MOB_INTERACTABLE_DOORS, state -> state.getBlock() instanceof DoorBlock)) {
                iterator.remove();
                continue;
            }
            DoorBlock lv4 = (DoorBlock)lv3.getBlock();
            if (!lv4.isOpen(lv3)) {
                iterator.remove();
                continue;
            }
            if (OpenDoorsTask.hasOtherMobReachedDoor(entity, lv2, otherMobs)) {
                iterator.remove();
                continue;
            }
            lv4.setOpen(entity, world, lv3, lv2, false);
            iterator.remove();
        }
    }

    private static boolean hasOtherMobReachedDoor(LivingEntity entity, BlockPos pos, Optional<List<LivingEntity>> otherMobs) {
        if (otherMobs.isEmpty()) {
            return false;
        }
        return otherMobs.get().stream().filter(mob -> mob.getType() == entity.getType()).filter(mob -> pos.isWithinDistance(mob.getPos(), 2.0)).anyMatch(mob -> OpenDoorsTask.hasReached(mob.getBrain(), pos));
    }

    private static boolean hasReached(Brain<?> brain, BlockPos pos) {
        if (!brain.hasMemoryModule(MemoryModuleType.PATH)) {
            return false;
        }
        Path lv = brain.getOptionalRegisteredMemory(MemoryModuleType.PATH).get();
        if (lv.isFinished()) {
            return false;
        }
        PathNode lv2 = lv.getLastNode();
        if (lv2 == null) {
            return false;
        }
        PathNode lv3 = lv.getCurrentNode();
        return pos.equals(lv2.getBlockPos()) || pos.equals(lv3.getBlockPos());
    }

    private static boolean cannotReachDoor(ServerWorld world, LivingEntity entity, GlobalPos doorPos) {
        return doorPos.dimension() != world.getRegistryKey() || !doorPos.pos().isWithinDistance(entity.getPos(), 3.0);
    }

    private static Optional<Set<GlobalPos>> storePos(MemoryQueryResult<OptionalBox.Mu, Set<GlobalPos>> queryResult, Optional<Set<GlobalPos>> doors, ServerWorld world, BlockPos pos) {
        GlobalPos lv = GlobalPos.create(world.getRegistryKey(), pos);
        return Optional.of(doors.map(doorSet -> {
            doorSet.add(lv);
            return doorSet;
        }).orElseGet(() -> {
            HashSet<GlobalPos> set = Sets.newHashSet(lv);
            queryResult.remember(set);
            return set;
        }));
    }
}

