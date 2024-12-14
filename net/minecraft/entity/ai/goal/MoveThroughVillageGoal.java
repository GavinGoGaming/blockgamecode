/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 * 
 * Could not load the following classes:
 *  org.jetbrains.annotations.Nullable
 */
package net.minecraft.entity.ai.goal;

import com.google.common.collect.Lists;
import java.util.EnumSet;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.function.BooleanSupplier;
import net.minecraft.block.DoorBlock;
import net.minecraft.entity.ai.FuzzyTargeting;
import net.minecraft.entity.ai.NavigationConditions;
import net.minecraft.entity.ai.NoPenaltyTargeting;
import net.minecraft.entity.ai.goal.Goal;
import net.minecraft.entity.ai.pathing.MobNavigation;
import net.minecraft.entity.ai.pathing.Path;
import net.minecraft.entity.ai.pathing.PathNode;
import net.minecraft.entity.mob.PathAwareEntity;
import net.minecraft.registry.tag.PointOfInterestTypeTags;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.poi.PointOfInterestStorage;
import org.jetbrains.annotations.Nullable;

public class MoveThroughVillageGoal
extends Goal {
    protected final PathAwareEntity mob;
    private final double speed;
    @Nullable
    private Path targetPath;
    private BlockPos target;
    private final boolean requiresNighttime;
    private final List<BlockPos> visitedTargets = Lists.newArrayList();
    private final int distance;
    private final BooleanSupplier doorPassingThroughGetter;

    public MoveThroughVillageGoal(PathAwareEntity entity, double speed, boolean requiresNighttime, int distance, BooleanSupplier doorPassingThroughGetter) {
        this.mob = entity;
        this.speed = speed;
        this.requiresNighttime = requiresNighttime;
        this.distance = distance;
        this.doorPassingThroughGetter = doorPassingThroughGetter;
        this.setControls(EnumSet.of(Goal.Control.MOVE));
        if (!NavigationConditions.hasMobNavigation(entity)) {
            throw new IllegalArgumentException("Unsupported mob for MoveThroughVillageGoal");
        }
    }

    @Override
    public boolean canStart() {
        BlockPos lv2;
        if (!NavigationConditions.hasMobNavigation(this.mob)) {
            return false;
        }
        this.forgetOldTarget();
        if (this.requiresNighttime && this.mob.getWorld().isDay()) {
            return false;
        }
        ServerWorld lv = (ServerWorld)this.mob.getWorld();
        if (!lv.isNearOccupiedPointOfInterest(lv2 = this.mob.getBlockPos(), 6)) {
            return false;
        }
        Vec3d lv3 = FuzzyTargeting.find(this.mob, 15, 7, pos -> {
            if (!lv.isNearOccupiedPointOfInterest((BlockPos)pos)) {
                return Double.NEGATIVE_INFINITY;
            }
            Optional<BlockPos> optional = lv.getPointOfInterestStorage().getPosition(poiType -> poiType.isIn(PointOfInterestTypeTags.VILLAGE), this::shouldVisit, (BlockPos)pos, 10, PointOfInterestStorage.OccupationStatus.IS_OCCUPIED);
            return optional.map(arg2 -> -arg2.getSquaredDistance(lv2)).orElse(Double.NEGATIVE_INFINITY);
        });
        if (lv3 == null) {
            return false;
        }
        Optional<BlockPos> optional = lv.getPointOfInterestStorage().getPosition(poiType -> poiType.isIn(PointOfInterestTypeTags.VILLAGE), this::shouldVisit, BlockPos.ofFloored(lv3), 10, PointOfInterestStorage.OccupationStatus.IS_OCCUPIED);
        if (optional.isEmpty()) {
            return false;
        }
        this.target = optional.get().toImmutable();
        MobNavigation lv4 = (MobNavigation)this.mob.getNavigation();
        boolean bl = lv4.canEnterOpenDoors();
        lv4.setCanPathThroughDoors(this.doorPassingThroughGetter.getAsBoolean());
        this.targetPath = lv4.findPathTo(this.target, 0);
        lv4.setCanPathThroughDoors(bl);
        if (this.targetPath == null) {
            Vec3d lv5 = NoPenaltyTargeting.findTo(this.mob, 10, 7, Vec3d.ofBottomCenter(this.target), 1.5707963705062866);
            if (lv5 == null) {
                return false;
            }
            lv4.setCanPathThroughDoors(this.doorPassingThroughGetter.getAsBoolean());
            this.targetPath = this.mob.getNavigation().findPathTo(lv5.x, lv5.y, lv5.z, 0);
            lv4.setCanPathThroughDoors(bl);
            if (this.targetPath == null) {
                return false;
            }
        }
        for (int i = 0; i < this.targetPath.getLength(); ++i) {
            PathNode lv6 = this.targetPath.getNode(i);
            BlockPos lv7 = new BlockPos(lv6.x, lv6.y + 1, lv6.z);
            if (!DoorBlock.canOpenByHand(this.mob.getWorld(), lv7)) continue;
            this.targetPath = this.mob.getNavigation().findPathTo(lv6.x, (double)lv6.y, lv6.z, 0);
            break;
        }
        return this.targetPath != null;
    }

    @Override
    public boolean shouldContinue() {
        if (this.mob.getNavigation().isIdle()) {
            return false;
        }
        return !this.target.isWithinDistance(this.mob.getPos(), (double)(this.mob.getWidth() + (float)this.distance));
    }

    @Override
    public void start() {
        this.mob.getNavigation().startMovingAlong(this.targetPath, this.speed);
    }

    @Override
    public void stop() {
        if (this.mob.getNavigation().isIdle() || this.target.isWithinDistance(this.mob.getPos(), (double)this.distance)) {
            this.visitedTargets.add(this.target);
        }
    }

    private boolean shouldVisit(BlockPos pos) {
        for (BlockPos lv : this.visitedTargets) {
            if (!Objects.equals(pos, lv)) continue;
            return false;
        }
        return true;
    }

    private void forgetOldTarget() {
        if (this.visitedTargets.size() > 15) {
            this.visitedTargets.remove(0);
        }
    }
}

