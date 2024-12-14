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
import net.minecraft.particle.ParticleTypes;
import net.minecraft.particle.SimpleParticleType;

@Environment(value=EnvType.CLIENT)
public class LavaEmberParticle
extends SpriteBillboardParticle {
    LavaEmberParticle(ClientWorld arg, double d, double e, double f) {
        super(arg, d, e, f, 0.0, 0.0, 0.0);
        this.gravityStrength = 0.75f;
        this.velocityMultiplier = 0.999f;
        this.velocityX *= (double)0.8f;
        this.velocityY *= (double)0.8f;
        this.velocityZ *= (double)0.8f;
        this.velocityY = this.random.nextFloat() * 0.4f + 0.05f;
        this.scale *= this.random.nextFloat() * 2.0f + 0.2f;
        this.maxAge = (int)(16.0 / (Math.random() * 0.8 + 0.2));
    }

    @Override
    public ParticleTextureSheet getType() {
        return ParticleTextureSheet.PARTICLE_SHEET_OPAQUE;
    }

    @Override
    public int getBrightness(float tint) {
        int i = super.getBrightness(tint);
        int j = 240;
        int k = i >> 16 & 0xFF;
        return 0xF0 | k << 16;
    }

    @Override
    public float getSize(float tickDelta) {
        float g = ((float)this.age + tickDelta) / (float)this.maxAge;
        return this.scale * (1.0f - g * g);
    }

    @Override
    public void tick() {
        super.tick();
        if (!this.dead) {
            float f = (float)this.age / (float)this.maxAge;
            if (this.random.nextFloat() > f) {
                this.world.addParticle(ParticleTypes.SMOKE, this.x, this.y, this.z, this.velocityX, this.velocityY, this.velocityZ);
            }
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
            LavaEmberParticle lv = new LavaEmberParticle(arg2, d, e, f);
            lv.setSprite(this.spriteProvider);
            return lv;
        }

        @Override
        public /* synthetic */ Particle createParticle(ParticleEffect parameters, ClientWorld world, double x, double y, double z, double velocityX, double velocityY, double velocityZ) {
            return this.createParticle((SimpleParticleType)parameters, world, x, y, z, velocityX, velocityY, velocityZ);
        }
    }
}

