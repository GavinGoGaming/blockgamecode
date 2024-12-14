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

@Environment(value=EnvType.CLIENT)
public class EmotionParticle
extends SpriteBillboardParticle {
    EmotionParticle(ClientWorld arg, double d, double e, double f) {
        super(arg, d, e, f, 0.0, 0.0, 0.0);
        this.ascending = true;
        this.velocityMultiplier = 0.86f;
        this.velocityX *= (double)0.01f;
        this.velocityY *= (double)0.01f;
        this.velocityZ *= (double)0.01f;
        this.velocityY += 0.1;
        this.scale *= 1.5f;
        this.maxAge = 16;
        this.collidesWithWorld = false;
    }

    @Override
    public ParticleTextureSheet getType() {
        return ParticleTextureSheet.PARTICLE_SHEET_OPAQUE;
    }

    @Override
    public float getSize(float tickDelta) {
        return this.scale * MathHelper.clamp(((float)this.age + tickDelta) / (float)this.maxAge * 32.0f, 0.0f, 1.0f);
    }

    @Environment(value=EnvType.CLIENT)
    public static class AngryVillagerFactory
    implements ParticleFactory<SimpleParticleType> {
        private final SpriteProvider spriteProvider;

        public AngryVillagerFactory(SpriteProvider spriteProvider) {
            this.spriteProvider = spriteProvider;
        }

        @Override
        public Particle createParticle(SimpleParticleType arg, ClientWorld arg2, double d, double e, double f, double g, double h, double i) {
            EmotionParticle lv = new EmotionParticle(arg2, d, e + 0.5, f);
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
    public static class HeartFactory
    implements ParticleFactory<SimpleParticleType> {
        private final SpriteProvider spriteProvider;

        public HeartFactory(SpriteProvider spriteProvider) {
            this.spriteProvider = spriteProvider;
        }

        @Override
        public Particle createParticle(SimpleParticleType arg, ClientWorld arg2, double d, double e, double f, double g, double h, double i) {
            EmotionParticle lv = new EmotionParticle(arg2, d, e, f);
            lv.setSprite(this.spriteProvider);
            return lv;
        }

        @Override
        public /* synthetic */ Particle createParticle(ParticleEffect parameters, ClientWorld world, double x, double y, double z, double velocityX, double velocityY, double velocityZ) {
            return this.createParticle((SimpleParticleType)parameters, world, x, y, z, velocityX, velocityY, velocityZ);
        }
    }
}

