/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 * 
 * Could not load the following classes:
 *  org.jetbrains.annotations.Nullable
 */
package net.minecraft.entity.ai.goal;

import java.util.List;
import java.util.stream.Collectors;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.ai.FuzzyTargeting;
import net.minecraft.entity.ai.goal.WanderAroundGoal;
import net.minecraft.entity.mob.PathAwareEntity;
import net.minecraft.entity.passive.VillagerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkSectionPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.poi.PointOfInterest;
import net.minecraft.world.poi.PointOfInterestStorage;
import org.jetbrains.annotations.Nullable;

public class IronGolemWanderAroundGoal
extends WanderAroundGoal {
    private static final int CHUNK_RANGE = 2;
    private static final int ENTITY_COLLISION_RANGE = 32;
    private static final int HORIZONTAL_RANGE = 10;
    private static final int VERTICAL_RANGE = 7;

    public IronGolemWanderAroundGoal(PathAwareEntity arg, double d) {
        super(arg, d, 240, false);
    }

    @Override
    @Nullable
    protected Vec3d getWanderTarget() {
        Vec3d lv;
        float f = this.mob.getWorld().random.nextFloat();
        if (this.mob.getWorld().random.nextFloat() < 0.3f) {
            return this.findRandomInRange();
        }
        if (f < 0.7f) {
            lv = this.findVillagerPos();
            if (lv == null) {
                lv = this.findRandomBlockPos();
            }
        } else {
            lv = this.findRandomBlockPos();
            if (lv == null) {
                lv = this.findVillagerPos();
            }
        }
        return lv == null ? this.findRandomInRange() : lv;
    }

    @Nullable
    private Vec3d findRandomInRange() {
        return FuzzyTargeting.find(this.mob, 10, 7);
    }

    @Nullable
    private Vec3d findVillagerPos() {
        ServerWorld lv = (ServerWorld)this.mob.getWorld();
        List<VillagerEntity> list = lv.getEntitiesByType(EntityType.VILLAGER, this.mob.getBoundingBox().expand(32.0), this::canVillagerSummonGolem);
        if (list.isEmpty()) {
            return null;
        }
        VillagerEntity lv2 = list.get(this.mob.getWorld().random.nextInt(list.size()));
        Vec3d lv3 = lv2.getPos();
        return FuzzyTargeting.findTo(this.mob, 10, 7, lv3);
    }

    @Nullable
    private Vec3d findRandomBlockPos() {
        ChunkSectionPos lv = this.findRandomChunkPos();
        if (lv == null) {
            return null;
        }
        BlockPos lv2 = this.findRandomPosInChunk(lv);
        if (lv2 == null) {
            return null;
        }
        return FuzzyTargeting.findTo(this.mob, 10, 7, Vec3d.ofBottomCenter(lv2));
    }

    @Nullable
    private ChunkSectionPos findRandomChunkPos() {
        ServerWorld lv = (ServerWorld)this.mob.getWorld();
        List list = ChunkSectionPos.stream(ChunkSectionPos.from(this.mob), 2).filter(sectionPos -> lv.getOccupiedPointOfInterestDistance((ChunkSectionPos)sectionPos) == 0).collect(Collectors.toList());
        if (list.isEmpty()) {
            return null;
        }
        return (ChunkSectionPos)list.get(lv.random.nextInt(list.size()));
    }

    @Nullable
    private BlockPos findRandomPosInChunk(ChunkSectionPos pos) {
        ServerWorld lv = (ServerWorld)this.mob.getWorld();
        PointOfInterestStorage lv2 = lv.getPointOfInterestStorage();
        List list = lv2.getInCircle(arg -> true, pos.getCenterPos(), 8, PointOfInterestStorage.OccupationStatus.IS_OCCUPIED).map(PointOfInterest::getPos).collect(Collectors.toList());
        if (list.isEmpty()) {
            return null;
        }
        return (BlockPos)list.get(lv.random.nextInt(list.size()));
    }

    private boolean canVillagerSummonGolem(VillagerEntity villager) {
        return villager.canSummonGolem(this.mob.getWorld().getTime());
    }
}

