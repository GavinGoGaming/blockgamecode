/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 * 
 * Could not load the following classes:
 *  org.jetbrains.annotations.Nullable
 */
package net.minecraft.entity.boss.dragon.phase;

import net.minecraft.entity.boss.dragon.EnderDragonEntity;
import net.minecraft.entity.boss.dragon.phase.AbstractPhase;
import net.minecraft.entity.boss.dragon.phase.PhaseType;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.Heightmap;
import net.minecraft.world.gen.feature.EndPortalFeature;
import org.jetbrains.annotations.Nullable;

public class LandingPhase
extends AbstractPhase {
    @Nullable
    private Vec3d target;

    public LandingPhase(EnderDragonEntity arg) {
        super(arg);
    }

    @Override
    public void clientTick() {
        Vec3d lv = this.dragon.getRotationVectorFromPhase(1.0f).normalize();
        lv.rotateY(-0.7853982f);
        double d = this.dragon.head.getX();
        double e = this.dragon.head.getBodyY(0.5);
        double f = this.dragon.head.getZ();
        for (int i = 0; i < 8; ++i) {
            Random lv2 = this.dragon.getRandom();
            double g = d + lv2.nextGaussian() / 2.0;
            double h = e + lv2.nextGaussian() / 2.0;
            double j = f + lv2.nextGaussian() / 2.0;
            Vec3d lv3 = this.dragon.getVelocity();
            this.dragon.getWorld().addParticle(ParticleTypes.DRAGON_BREATH, g, h, j, -lv.x * (double)0.08f + lv3.x, -lv.y * (double)0.3f + lv3.y, -lv.z * (double)0.08f + lv3.z);
            lv.rotateY(0.19634955f);
        }
    }

    @Override
    public void serverTick() {
        if (this.target == null) {
            this.target = Vec3d.ofBottomCenter(this.dragon.getWorld().getTopPosition(Heightmap.Type.MOTION_BLOCKING_NO_LEAVES, EndPortalFeature.offsetOrigin(this.dragon.getFightOrigin())));
        }
        if (this.target.squaredDistanceTo(this.dragon.getX(), this.dragon.getY(), this.dragon.getZ()) < 1.0) {
            this.dragon.getPhaseManager().create(PhaseType.SITTING_FLAMING).reset();
            this.dragon.getPhaseManager().setPhase(PhaseType.SITTING_SCANNING);
        }
    }

    @Override
    public float getMaxYAcceleration() {
        return 1.5f;
    }

    @Override
    public float getYawAcceleration() {
        float f = (float)this.dragon.getVelocity().horizontalLength() + 1.0f;
        float g = Math.min(f, 40.0f);
        return g / f;
    }

    @Override
    public void beginPhase() {
        this.target = null;
    }

    @Override
    @Nullable
    public Vec3d getPathTarget() {
        return this.target;
    }

    public PhaseType<LandingPhase> getType() {
        return PhaseType.LANDING;
    }
}

