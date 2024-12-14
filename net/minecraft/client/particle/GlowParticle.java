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
import net.minecraft.client.particle.ParticleTextureSheet;
import net.minecraft.client.particle.SpriteBillboardParticle;
import net.minecraft.client.particle.SpriteProvider;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.particle.ParticleEffect;
import net.minecraft.particle.SimpleParticleType;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.random.Random;

@Environment(value=EnvType.CLIENT)
public class GlowParticle
extends SpriteBillboardParticle {
    static final Random RANDOM = Random.create();
    private final SpriteProvider spriteProvider;

    GlowParticle(ClientWorld world, double x, double y, double z, double velocityX, double velocityY, double velocityZ, SpriteProvider spriteProvider) {
        super(world, x, y, z, velocityX, velocityY, velocityZ);
        this.velocityMultiplier = 0.96f;
        this.ascending = true;
        this.spriteProvider = spriteProvider;
        this.scale *= 0.75f;
        this.collidesWithWorld = false;
        this.setSpriteForAge(spriteProvider);
    }

    @Override
    public ParticleTextureSheet getType() {
        return ParticleTextureSheet.PARTICLE_SHEET_TRANSLUCENT;
    }

    @Override
    public int getBrightness(float tint) {
        float g = ((float)this.age + tint) / (float)this.maxAge;
        g = MathHelper.clamp(g, 0.0f, 1.0f);
        int i = super.getBrightness(tint);
        int j = i & 0xFF;
        int k = i >> 16 & 0xFF;
        if ((j += (int)(g * 15.0f * 16.0f)) > 240) {
            j = 240;
        }
        return j | k << 16;
    }

    @Override
    public void tick() {
        super.tick();
        this.setSpriteForAge(this.spriteProvider);
    }

    @Environment(value=EnvType.CLIENT)
    public static class ScrapeFactory
    implements ParticleFactory<SimpleParticleType> {
        private final double velocityMultiplier = 0.01;
        private final SpriteProvider spriteProvider;

        public ScrapeFactory(SpriteProvider spriteProvider) {
            this.spriteProvider = spriteProvider;
        }

        @Override
        public Particle createParticle(SimpleParticleType arg, ClientWorld arg2, double d, double e, double f, double g, double h, double i) {
            GlowParticle lv = new GlowParticle(arg2, d, e, f, 0.0, 0.0, 0.0, this.spriteProvider);
            if (arg2.random.nextBoolean()) {
                lv.setColor(0.29f, 0.58f, 0.51f);
            } else {
                lv.setColor(0.43f, 0.77f, 0.62f);
            }
            lv.setVelocity(g * 0.01, h * 0.01, i * 0.01);
            int j = 10;
            int k = 40;
            lv.setMaxAge(arg2.random.nextInt(30) + 10);
            return lv;
        }

        @Override
        public /* synthetic */ Particle createParticle(ParticleEffect parameters, ClientWorld world, double x, double y, double z, double velocityX, double velocityY, double velocityZ) {
            return this.createParticle((SimpleParticleType)parameters, world, x, y, z, velocityX, velocityY, velocityZ);
        }
    }

    @Environment(value=EnvType.CLIENT)
    public static class ElectricSparkFactory
    implements ParticleFactory<SimpleParticleType> {
        private final double velocityMultiplier = 0.25;
        private final SpriteProvider spriteProvider;

        public ElectricSparkFactory(SpriteProvider spriteProvider) {
            this.spriteProvider = spriteProvider;
        }

        @Override
        public Particle createParticle(SimpleParticleType arg, ClientWorld arg2, double d, double e, double f, double g, double h, double i) {
            GlowParticle lv = new GlowParticle(arg2, d, e, f, 0.0, 0.0, 0.0, this.spriteProvider);
            lv.setColor(1.0f, 0.9f, 1.0f);
            lv.setVelocity(g * 0.25, h * 0.25, i * 0.25);
            int j = 2;
            int k = 4;
            lv.setMaxAge(arg2.random.nextInt(2) + 2);
            return lv;
        }

        @Override
        public /* synthetic */ Particle createParticle(ParticleEffect parameters, ClientWorld world, double x, double y, double z, double velocityX, double velocityY, double velocityZ) {
            return this.createParticle((SimpleParticleType)parameters, world, x, y, z, velocityX, velocityY, velocityZ);
        }
    }

    @Environment(value=EnvType.CLIENT)
    public static class WaxOffFactory
    implements ParticleFactory<SimpleParticleType> {
        private final double velocityMultiplier = 0.01;
        private final SpriteProvider spriteProvider;

        public WaxOffFactory(SpriteProvider spriteProvider) {
            this.spriteProvider = spriteProvider;
        }

        @Override
        public Particle createParticle(SimpleParticleType arg, ClientWorld arg2, double d, double e, double f, double g, double h, double i) {
            GlowParticle lv = new GlowParticle(arg2, d, e, f, 0.0, 0.0, 0.0, this.spriteProvider);
            lv.setColor(1.0f, 0.9f, 1.0f);
            lv.setVelocity(g * 0.01 / 2.0, h * 0.01, i * 0.01 / 2.0);
            int j = 10;
            int k = 40;
            lv.setMaxAge(arg2.random.nextInt(30) + 10);
            return lv;
        }

        @Override
        public /* synthetic */ Particle createParticle(ParticleEffect parameters, ClientWorld world, double x, double y, double z, double velocityX, double velocityY, double velocityZ) {
            return this.createParticle((SimpleParticleType)parameters, world, x, y, z, velocityX, velocityY, velocityZ);
        }
    }

    @Environment(value=EnvType.CLIENT)
    public static class WaxOnFactory
    implements ParticleFactory<SimpleParticleType> {
        private final double velocityMultiplier = 0.01;
        private final SpriteProvider spriteProvider;

        public WaxOnFactory(SpriteProvider spriteProvider) {
            this.spriteProvider = spriteProvider;
        }

        @Override
        public Particle createParticle(SimpleParticleType arg, ClientWorld arg2, double d, double e, double f, double g, double h, double i) {
            GlowParticle lv = new GlowParticle(arg2, d, e, f, 0.0, 0.0, 0.0, this.spriteProvider);
            lv.setColor(0.91f, 0.55f, 0.08f);
            lv.setVelocity(g * 0.01 / 2.0, h * 0.01, i * 0.01 / 2.0);
            int j = 10;
            int k = 40;
            lv.setMaxAge(arg2.random.nextInt(30) + 10);
            return lv;
        }

        @Override
        public /* synthetic */ Particle createParticle(ParticleEffect parameters, ClientWorld world, double x, double y, double z, double velocityX, double velocityY, double velocityZ) {
            return this.createParticle((SimpleParticleType)parameters, world, x, y, z, velocityX, velocityY, velocityZ);
        }
    }

    @Environment(value=EnvType.CLIENT)
    public static class GlowFactory
    implements ParticleFactory<SimpleParticleType> {
        private final SpriteProvider spriteProvider;

        public GlowFactory(SpriteProvider spriteProvider) {
            this.spriteProvider = spriteProvider;
        }

        @Override
        public Particle createParticle(SimpleParticleType arg, ClientWorld arg2, double d, double e, double f, double g, double h, double i) {
            GlowParticle lv = new GlowParticle(arg2, d, e, f, 0.5 - RANDOM.nextDouble(), h, 0.5 - RANDOM.nextDouble(), this.spriteProvider);
            if (arg2.random.nextBoolean()) {
                lv.setColor(0.6f, 1.0f, 0.8f);
            } else {
                lv.setColor(0.08f, 0.4f, 0.4f);
            }
            lv.velocityY *= (double)0.2f;
            if (g == 0.0 && i == 0.0) {
                lv.velocityX *= (double)0.1f;
                lv.velocityZ *= (double)0.1f;
            }
            lv.setMaxAge((int)(8.0 / (arg2.random.nextDouble() * 0.8 + 0.2)));
            return lv;
        }

        @Override
        public /* synthetic */ Particle createParticle(ParticleEffect parameters, ClientWorld world, double x, double y, double z, double velocityX, double velocityY, double velocityZ) {
            return this.createParticle((SimpleParticleType)parameters, world, x, y, z, velocityX, velocityY, velocityZ);
        }
    }
}

