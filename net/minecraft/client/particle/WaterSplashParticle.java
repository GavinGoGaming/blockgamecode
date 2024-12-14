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
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleFactory;
import net.minecraft.client.particle.RainSplashParticle;
import net.minecraft.client.particle.SpriteProvider;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.particle.ParticleEffect;
import net.minecraft.particle.SimpleParticleType;

@Environment(value=EnvType.CLIENT)
public class WaterSplashParticle
extends RainSplashParticle {
    WaterSplashParticle(ClientWorld arg, double d, double e, double f, double g, double h, double i) {
        super(arg, d, e, f);
        this.gravityStrength = 0.04f;
        if (h == 0.0 && (g != 0.0 || i != 0.0)) {
            this.velocityX = g;
            this.velocityY = 0.1;
            this.velocityZ = i;
        }
    }

    @Environment(value=EnvType.CLIENT)
    public static class SplashFactory
    implements ParticleFactory<SimpleParticleType> {
        private final SpriteProvider spriteProvider;

        public SplashFactory(SpriteProvider spriteProvider) {
            this.spriteProvider = spriteProvider;
        }

        @Override
        public Particle createParticle(SimpleParticleType arg, ClientWorld arg2, double d, double e, double f, double g, double h, double i) {
            WaterSplashParticle lv = new WaterSplashParticle(arg2, d, e, f, g, h, i);
            lv.setSprite(this.spriteProvider);
            return lv;
        }

        @Override
        public /* synthetic */ Particle createParticle(ParticleEffect parameters, ClientWorld world, double x, double y, double z, double velocityX, double velocityY, double velocityZ) {
            return this.createParticle((SimpleParticleType)parameters, world, x, y, z, velocityX, velocityY, velocityZ);
        }
    }
}

