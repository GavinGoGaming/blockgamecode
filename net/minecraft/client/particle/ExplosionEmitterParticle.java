/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 * 
 * Could not load the following classes:
 *  net.fabricmc.api.EnvType
 *  net.fabricmc.api.Environment
 */
package net.minecraft.client.particle;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.particle.NoRenderParticle;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleFactory;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.particle.ParticleEffect;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.particle.SimpleParticleType;

@Environment(value=EnvType.CLIENT)
public class ExplosionEmitterParticle
extends NoRenderParticle {
    ExplosionEmitterParticle(ClientWorld arg, double d, double e, double f) {
        super(arg, d, e, f, 0.0, 0.0, 0.0);
        this.maxAge = 8;
    }

    @Override
    public void tick() {
        for (int i = 0; i < 6; ++i) {
            double d = this.x + (this.random.nextDouble() - this.random.nextDouble()) * 4.0;
            double e = this.y + (this.random.nextDouble() - this.random.nextDouble()) * 4.0;
            double f = this.z + (this.random.nextDouble() - this.random.nextDouble()) * 4.0;
            this.world.addParticle(ParticleTypes.EXPLOSION, d, e, f, (float)this.age / (float)this.maxAge, 0.0, 0.0);
        }
        ++this.age;
        if (this.age == this.maxAge) {
            this.markDead();
        }
    }

    @Environment(value=EnvType.CLIENT)
    public static class Factory
    implements ParticleFactory<SimpleParticleType> {
        @Override
        public Particle createParticle(SimpleParticleType arg, ClientWorld arg2, double d, double e, double f, double g, double h, double i) {
            return new ExplosionEmitterParticle(arg2, d, e, f);
        }

        @Override
        public /* synthetic */ Particle createParticle(ParticleEffect parameters, ClientWorld world, double x, double y, double z, double velocityX, double velocityY, double velocityZ) {
            return this.createParticle((SimpleParticleType)parameters, world, x, y, z, velocityX, velocityY, velocityZ);
        }
    }
}

