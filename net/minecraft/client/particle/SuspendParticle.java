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

@Environment(value=EnvType.CLIENT)
public class SuspendParticle
extends SpriteBillboardParticle {
    SuspendParticle(ClientWorld arg, double d, double e, double f, double g, double h, double i) {
        super(arg, d, e, f, g, h, i);
        float j;
        this.red = j = this.random.nextFloat() * 0.1f + 0.2f;
        this.green = j;
        this.blue = j;
        this.setBoundingBoxSpacing(0.02f, 0.02f);
        this.scale *= this.random.nextFloat() * 0.6f + 0.5f;
        this.velocityX *= (double)0.02f;
        this.velocityY *= (double)0.02f;
        this.velocityZ *= (double)0.02f;
        this.maxAge = (int)(20.0 / (Math.random() * 0.8 + 0.2));
    }

    @Override
    public ParticleTextureSheet getType() {
        return ParticleTextureSheet.PARTICLE_SHEET_OPAQUE;
    }

    @Override
    public void move(double dx, double dy, double dz) {
        this.setBoundingBox(this.getBoundingBox().offset(dx, dy, dz));
        this.repositionFromBoundingBox();
    }

    @Override
    public void tick() {
        this.prevPosX = this.x;
        this.prevPosY = this.y;
        this.prevPosZ = this.z;
        if (this.maxAge-- <= 0) {
            this.markDead();
            return;
        }
        this.move(this.velocityX, this.velocityY, this.velocityZ);
        this.velocityX *= 0.99;
        this.velocityY *= 0.99;
        this.velocityZ *= 0.99;
    }

    @Environment(value=EnvType.CLIENT)
    public static class EggCrackFactory
    implements ParticleFactory<SimpleParticleType> {
        private final SpriteProvider spriteProvider;

        public EggCrackFactory(SpriteProvider spriteProvider) {
            this.spriteProvider = spriteProvider;
        }

        @Override
        public Particle createParticle(SimpleParticleType arg, ClientWorld arg2, double d, double e, double f, double g, double h, double i) {
            SuspendParticle lv = new SuspendParticle(arg2, d, e, f, g, h, i);
            lv.setSprite(this.spriteProvider);
            lv.setColor(1.0f, 1.0f, 1.0f);
            return lv;
        }

        @Override
        public /* synthetic */ Particle createParticle(ParticleEffect parameters, ClientWorld world, double x, double y, double z, double velocityX, double velocityY, double velocityZ) {
            return this.createParticle((SimpleParticleType)parameters, world, x, y, z, velocityX, velocityY, velocityZ);
        }
    }

    @Environment(value=EnvType.CLIENT)
    public static class DolphinFactory
    implements ParticleFactory<SimpleParticleType> {
        private final SpriteProvider spriteProvider;

        public DolphinFactory(SpriteProvider spriteProvider) {
            this.spriteProvider = spriteProvider;
        }

        @Override
        public Particle createParticle(SimpleParticleType arg, ClientWorld arg2, double d, double e, double f, double g, double h, double i) {
            SuspendParticle lv = new SuspendParticle(arg2, d, e, f, g, h, i);
            lv.setColor(0.3f, 0.5f, 1.0f);
            lv.setSprite(this.spriteProvider);
            lv.setAlpha(1.0f - arg2.random.nextFloat() * 0.7f);
            lv.setMaxAge(lv.getMaxAge() / 2);
            return lv;
        }

        @Override
        public /* synthetic */ Particle createParticle(ParticleEffect parameters, ClientWorld world, double x, double y, double z, double velocityX, double velocityY, double velocityZ) {
            return this.createParticle((SimpleParticleType)parameters, world, x, y, z, velocityX, velocityY, velocityZ);
        }
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
            SuspendParticle lv = new SuspendParticle(arg2, d, e, f, g, h, i);
            lv.setSprite(this.spriteProvider);
            lv.setColor(1.0f, 1.0f, 1.0f);
            lv.setMaxAge(3 + arg2.getRandom().nextInt(5));
            return lv;
        }

        @Override
        public /* synthetic */ Particle createParticle(ParticleEffect parameters, ClientWorld world, double x, double y, double z, double velocityX, double velocityY, double velocityZ) {
            return this.createParticle((SimpleParticleType)parameters, world, x, y, z, velocityX, velocityY, velocityZ);
        }
    }

    @Environment(value=EnvType.CLIENT)
    public static class HappyVillagerFactory
    implements ParticleFactory<SimpleParticleType> {
        private final SpriteProvider spriteProvider;

        public HappyVillagerFactory(SpriteProvider spriteProvider) {
            this.spriteProvider = spriteProvider;
        }

        @Override
        public Particle createParticle(SimpleParticleType arg, ClientWorld arg2, double d, double e, double f, double g, double h, double i) {
            SuspendParticle lv = new SuspendParticle(arg2, d, e, f, g, h, i);
            lv.setSprite(this.spriteProvider);
            lv.setColor(1.0f, 1.0f, 1.0f);
            return lv;
        }

        @Override
        public /* synthetic */ Particle createParticle(ParticleEffect parameters, ClientWorld world, double x, double y, double z, double velocityX, double velocityY, double velocityZ) {
            return this.createParticle((SimpleParticleType)parameters, world, x, y, z, velocityX, velocityY, velocityZ);
        }
    }

    @Environment(value=EnvType.CLIENT)
    public static class MyceliumFactory
    implements ParticleFactory<SimpleParticleType> {
        private final SpriteProvider spriteProvider;

        public MyceliumFactory(SpriteProvider spriteProvider) {
            this.spriteProvider = spriteProvider;
        }

        @Override
        public Particle createParticle(SimpleParticleType arg, ClientWorld arg2, double d, double e, double f, double g, double h, double i) {
            SuspendParticle lv = new SuspendParticle(arg2, d, e, f, g, h, i);
            lv.setSprite(this.spriteProvider);
            return lv;
        }

        @Override
        public /* synthetic */ Particle createParticle(ParticleEffect parameters, ClientWorld world, double x, double y, double z, double velocityX, double velocityY, double velocityZ) {
            return this.createParticle((SimpleParticleType)parameters, world, x, y, z, velocityX, velocityY, velocityZ);
        }
    }
}

