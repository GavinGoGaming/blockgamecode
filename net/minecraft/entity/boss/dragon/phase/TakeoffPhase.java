/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 * 
 * Could not load the following classes:
 *  org.jetbrains.annotations.Nullable
 */
package net.minecraft.entity.boss.dragon.phase;

import net.minecraft.entity.ai.pathing.Path;
import net.minecraft.entity.boss.dragon.EnderDragonEntity;
import net.minecraft.entity.boss.dragon.phase.AbstractPhase;
import net.minecraft.entity.boss.dragon.phase.PhaseType;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.Heightmap;
import net.minecraft.world.gen.feature.EndPortalFeature;
import org.jetbrains.annotations.Nullable;

public class TakeoffPhase
extends AbstractPhase {
    private boolean shouldFindNewPath;
    @Nullable
    private Path path;
    @Nullable
    private Vec3d pathTarget;

    public TakeoffPhase(EnderDragonEntity arg) {
        super(arg);
    }

    @Override
    public void serverTick() {
        if (this.shouldFindNewPath || this.path == null) {
            this.shouldFindNewPath = false;
            this.updatePath();
        } else {
            BlockPos lv = this.dragon.getWorld().getTopPosition(Heightmap.Type.MOTION_BLOCKING_NO_LEAVES, EndPortalFeature.offsetOrigin(this.dragon.getFightOrigin()));
            if (!lv.isWithinDistance(this.dragon.getPos(), 10.0)) {
                this.dragon.getPhaseManager().setPhase(PhaseType.HOLDING_PATTERN);
            }
        }
    }

    @Override
    public void beginPhase() {
        this.shouldFindNewPath = true;
        this.path = null;
        this.pathTarget = null;
    }

    private void updatePath() {
        int i = this.dragon.getNearestPathNodeIndex();
        Vec3d lv = this.dragon.getRotationVectorFromPhase(1.0f);
        int j = this.dragon.getNearestPathNodeIndex(-lv.x * 40.0, 105.0, -lv.z * 40.0);
        if (this.dragon.getFight() == null || this.dragon.getFight().getAliveEndCrystals() <= 0) {
            j -= 12;
            j &= 7;
            j += 12;
        } else if ((j %= 12) < 0) {
            j += 12;
        }
        this.path = this.dragon.findPath(i, j, null);
        this.followPath();
    }

    private void followPath() {
        if (this.path != null) {
            this.path.next();
            if (!this.path.isFinished()) {
                double d;
                BlockPos lv = this.path.getCurrentNodePos();
                this.path.next();
                while ((d = (double)((float)lv.getY() + this.dragon.getRandom().nextFloat() * 20.0f)) < (double)lv.getY()) {
                }
                this.pathTarget = new Vec3d(lv.getX(), d, lv.getZ());
            }
        }
    }

    @Override
    @Nullable
    public Vec3d getPathTarget() {
        return this.pathTarget;
    }

    public PhaseType<TakeoffPhase> getType() {
        return PhaseType.TAKEOFF;
    }
}

