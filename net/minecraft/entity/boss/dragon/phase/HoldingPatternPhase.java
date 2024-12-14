/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 * 
 * Could not load the following classes:
 *  org.jetbrains.annotations.Nullable
 */
package net.minecraft.entity.boss.dragon.phase;

import net.minecraft.entity.ai.TargetPredicate;
import net.minecraft.entity.ai.pathing.Path;
import net.minecraft.entity.boss.dragon.EnderDragonEntity;
import net.minecraft.entity.boss.dragon.phase.AbstractPhase;
import net.minecraft.entity.boss.dragon.phase.PhaseType;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.decoration.EndCrystalEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.Heightmap;
import net.minecraft.world.gen.feature.EndPortalFeature;
import org.jetbrains.annotations.Nullable;

public class HoldingPatternPhase
extends AbstractPhase {
    private static final TargetPredicate PLAYERS_IN_RANGE_PREDICATE = TargetPredicate.createAttackable().ignoreVisibility();
    @Nullable
    private Path path;
    @Nullable
    private Vec3d pathTarget;
    private boolean shouldFindNewPath;

    public HoldingPatternPhase(EnderDragonEntity arg) {
        super(arg);
    }

    public PhaseType<HoldingPatternPhase> getType() {
        return PhaseType.HOLDING_PATTERN;
    }

    @Override
    public void serverTick() {
        double d;
        double d2 = d = this.pathTarget == null ? 0.0 : this.pathTarget.squaredDistanceTo(this.dragon.getX(), this.dragon.getY(), this.dragon.getZ());
        if (d < 100.0 || d > 22500.0 || this.dragon.horizontalCollision || this.dragon.verticalCollision) {
            this.tickInRange();
        }
    }

    @Override
    public void beginPhase() {
        this.path = null;
        this.pathTarget = null;
    }

    @Override
    @Nullable
    public Vec3d getPathTarget() {
        return this.pathTarget;
    }

    private void tickInRange() {
        int i;
        if (this.path != null && this.path.isFinished()) {
            BlockPos lv = this.dragon.getWorld().getTopPosition(Heightmap.Type.MOTION_BLOCKING_NO_LEAVES, new BlockPos(EndPortalFeature.offsetOrigin(this.dragon.getFightOrigin())));
            int n = i = this.dragon.getFight() == null ? 0 : this.dragon.getFight().getAliveEndCrystals();
            if (this.dragon.getRandom().nextInt(i + 3) == 0) {
                this.dragon.getPhaseManager().setPhase(PhaseType.LANDING_APPROACH);
                return;
            }
            PlayerEntity lv2 = this.dragon.getWorld().getClosestPlayer(PLAYERS_IN_RANGE_PREDICATE, this.dragon, (double)lv.getX(), (double)lv.getY(), lv.getZ());
            double d = lv2 != null ? lv.getSquaredDistance(lv2.getPos()) / 512.0 : 64.0;
            if (lv2 != null && (this.dragon.getRandom().nextInt((int)(d + 2.0)) == 0 || this.dragon.getRandom().nextInt(i + 2) == 0)) {
                this.strafePlayer(lv2);
                return;
            }
        }
        if (this.path == null || this.path.isFinished()) {
            int j;
            i = j = this.dragon.getNearestPathNodeIndex();
            if (this.dragon.getRandom().nextInt(8) == 0) {
                this.shouldFindNewPath = !this.shouldFindNewPath;
                i += 6;
            }
            i = this.shouldFindNewPath ? ++i : --i;
            if (this.dragon.getFight() == null || this.dragon.getFight().getAliveEndCrystals() < 0) {
                i -= 12;
                i &= 7;
                i += 12;
            } else if ((i %= 12) < 0) {
                i += 12;
            }
            this.path = this.dragon.findPath(j, i, null);
            if (this.path != null) {
                this.path.next();
            }
        }
        this.followPath();
    }

    private void strafePlayer(PlayerEntity player) {
        this.dragon.getPhaseManager().setPhase(PhaseType.STRAFE_PLAYER);
        this.dragon.getPhaseManager().create(PhaseType.STRAFE_PLAYER).setTargetEntity(player);
    }

    private void followPath() {
        if (this.path != null && !this.path.isFinished()) {
            double f;
            BlockPos lv = this.path.getCurrentNodePos();
            this.path.next();
            double d = lv.getX();
            double e = lv.getZ();
            while ((f = (double)((float)lv.getY() + this.dragon.getRandom().nextFloat() * 20.0f)) < (double)lv.getY()) {
            }
            this.pathTarget = new Vec3d(d, f, e);
        }
    }

    @Override
    public void crystalDestroyed(EndCrystalEntity crystal, BlockPos pos, DamageSource source, @Nullable PlayerEntity player) {
        if (player != null && this.dragon.canTarget(player)) {
            this.strafePlayer(player);
        }
    }
}

