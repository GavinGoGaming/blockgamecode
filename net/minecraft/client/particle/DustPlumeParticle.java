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
import net.minecraft.client.particle.AscendingParticle;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleFactory;
import net.minecraft.client.particle.SpriteProvider;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.particle.ParticleEffect;
import net.minecraft.particle.SimpleParticleType;
import net.minecraft.util.math.ColorHelper;

@Environment(value=EnvType.CLIENT)
public class DustPlumeParticle
extends AscendingParticle {
    private static final int COLOR = 12235202;

    protected DustPlumeParticle(ClientWorld world, double x, double y, double z, double velocityX, double velocityY, double velocityZ, float scaleMultiplier, SpriteProvider spriteProvider) {
        super(world, x, y, z, 0.7f, 0.6f, 0.7f, velocityX, velocityY + (double)0.15f, velocityZ, scaleMultiplier, spriteProvider, 0.5f, 7, 0.5f, false);
        float k = (float)Math.random() * 0.2f;
        this.red = (float)ColorHelper.Argb.getRed(12235202) / 255.0f - k;
        this.green = (float)ColorHelper.Argb.getGreen(12235202) / 255.0f - k;
        this.blue = (float)ColorHelper.Argb.getBlue(12235202) / 255.0f - k;
    }

    @Override
    public void tick() {
        this.gravityStrength = 0.88f * this.gravityStrength;
        this.velocityMultiplier = 0.92f * this.velocityMultiplier;
        super.tick();
    }

    @Environment(value=EnvType.CLIENT)
    public static class Factory
    implements ParticleFactory<SimpleParticleType> {
        private final SpriteProvider spriteProvider;

        public Factory(SpriteProvider spriteProvider) {
            this.spriteProvider = spriteProvider;
        }

        @Override
        public Particle createParticle(SimpleParticleType arg, ClientWorld arg2, double d, double e, double f, double g, double h, double i) {
            return new DustPlumeParticle(arg2, d, e, f, g, h, i, 1.0f, this.spriteProvider);
        }

        @Override
        public /* synthetic */ Particle createParticle(ParticleEffect parameters, ClientWorld world, double x, double y, double z, double velocityX, double velocityY, double velocityZ) {
            return this.createParticle((SimpleParticleType)parameters, world, x, y, z, velocityX, velocityY, velocityZ);
        }
    }
}

