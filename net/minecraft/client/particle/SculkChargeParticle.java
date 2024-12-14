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
import net.minecraft.particle.SculkChargeParticleEffect;

@Environment(value=EnvType.CLIENT)
public class SculkChargeParticle
extends SpriteBillboardParticle {
    private final SpriteProvider spriteProvider;

    SculkChargeParticle(ClientWorld world, double x, double y, double z, double velocityX, double velocityY, double velocityZ, SpriteProvider spriteProvider) {
        super(world, x, y, z, velocityX, velocityY, velocityZ);
        this.velocityMultiplier = 0.96f;
        this.spriteProvider = spriteProvider;
        this.scale(1.5f);
        this.collidesWithWorld = false;
        this.setSpriteForAge(spriteProvider);
    }

    @Override
    public int getBrightness(float tint) {
        return 240;
    }

    @Override
    public ParticleTextureSheet getType() {
        return ParticleTextureSheet.PARTICLE_SHEET_TRANSLUCENT;
    }

    @Override
    public void tick() {
        super.tick();
        this.setSpriteForAge(this.spriteProvider);
    }

    @Environment(value=EnvType.CLIENT)
    public record Factory(SpriteProvider spriteProvider) implements ParticleFactory<SculkChargeParticleEffect>
    {
        @Override
        public Particle createParticle(SculkChargeParticleEffect arg, ClientWorld arg2, double d, double e, double f, double g, double h, double i) {
            SculkChargeParticle lv = new SculkChargeParticle(arg2, d, e, f, g, h, i, this.spriteProvider);
            lv.setAlpha(1.0f);
            lv.setVelocity(g, h, i);
            lv.prevAngle = arg.roll();
            lv.angle = arg.roll();
            lv.setMaxAge(arg2.random.nextInt(12) + 8);
            return lv;
        }

        @Override
        public /* synthetic */ Particle createParticle(ParticleEffect parameters, ClientWorld world, double x, double y, double z, double velocityX, double velocityY, double velocityZ) {
            return this.createParticle((SculkChargeParticleEffect)parameters, world, x, y, z, velocityX, velocityY, velocityZ);
        }
    }
}

