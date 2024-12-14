/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 * 
 * Could not load the following classes:
 *  net.fabricmc.api.EnvType
 *  net.fabricmc.api.Environment
 */
package net.minecraft.client.particle;

import it.unimi.dsi.fastutil.ints.IntList;
import java.util.List;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.particle.AnimatedParticle;
import net.minecraft.client.particle.NoRenderParticle;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleFactory;
import net.minecraft.client.particle.ParticleManager;
import net.minecraft.client.particle.ParticleTextureSheet;
import net.minecraft.client.particle.SpriteBillboardParticle;
import net.minecraft.client.particle.SpriteProvider;
import net.minecraft.client.render.Camera;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.component.type.FireworkExplosionComponent;
import net.minecraft.particle.ParticleEffect;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.particle.SimpleParticleType;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvent;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.DyeColor;
import net.minecraft.util.Util;
import net.minecraft.util.math.ColorHelper;
import net.minecraft.util.math.MathHelper;

@Environment(value=EnvType.CLIENT)
public class FireworksSparkParticle {

    @Environment(value=EnvType.CLIENT)
    public static class ExplosionFactory
    implements ParticleFactory<SimpleParticleType> {
        private final SpriteProvider spriteProvider;

        public ExplosionFactory(SpriteProvider spriteProvider) {
            this.spriteProvider = spriteProvider;
        }

        @Override
        public Particle createParticle(SimpleParticleType arg, ClientWorld arg2, double d, double e, double f, double g, double h, double i) {
            Explosion lv = new Explosion(arg2, d, e, f, g, h, i, MinecraftClient.getInstance().particleManager, this.spriteProvider);
            lv.setAlpha(0.99f);
            return lv;
        }

        @Override
        public /* synthetic */ Particle createParticle(ParticleEffect parameters, ClientWorld world, double x, double y, double z, double velocityX, double velocityY, double velocityZ) {
            return this.createParticle((SimpleParticleType)parameters, world, x, y, z, velocityX, velocityY, velocityZ);
        }
    }

    @Environment(value=EnvType.CLIENT)
    public static class FlashFactory
    implements ParticleFactory<SimpleParticleType> {
        private final SpriteProvider spriteProvider;

        public FlashFactory(SpriteProvider spriteProvider) {
            this.spriteProvider = spriteProvider;
        }

        @Override
        public Particle createParticle(SimpleParticleType arg, ClientWorld arg2, double d, double e, double f, double g, double h, double i) {
            Flash lv = new Flash(arg2, d, e, f);
            lv.setSprite(this.spriteProvider);
            return lv;
        }

        @Override
        public /* synthetic */ Particle createParticle(ParticleEffect parameters, ClientWorld world, double x, double y, double z, double velocityX, double velocityY, double velocityZ) {
            return this.createParticle((SimpleParticleType)parameters, world, x, y, z, velocityX, velocityY, velocityZ);
        }
    }

    @Environment(value=EnvType.CLIENT)
    public static class Flash
    extends SpriteBillboardParticle {
        Flash(ClientWorld arg, double d, double e, double f) {
            super(arg, d, e, f);
            this.maxAge = 4;
        }

        @Override
        public ParticleTextureSheet getType() {
            return ParticleTextureSheet.PARTICLE_SHEET_TRANSLUCENT;
        }

        @Override
        public void buildGeometry(VertexConsumer vertexConsumer, Camera camera, float tickDelta) {
            this.setAlpha(0.6f - ((float)this.age + tickDelta - 1.0f) * 0.25f * 0.5f);
            super.buildGeometry(vertexConsumer, camera, tickDelta);
        }

        @Override
        public float getSize(float tickDelta) {
            return 7.1f * MathHelper.sin(((float)this.age + tickDelta - 1.0f) * 0.25f * (float)Math.PI);
        }
    }

    @Environment(value=EnvType.CLIENT)
    static class Explosion
    extends AnimatedParticle {
        private boolean trail;
        private boolean flicker;
        private final ParticleManager particleManager;
        private float field_3801;
        private float field_3800;
        private float field_3799;
        private boolean field_3802;

        Explosion(ClientWorld world, double x, double y, double z, double velocityX, double velocityY, double velocityZ, ParticleManager particleManager, SpriteProvider spriteProvider) {
            super(world, x, y, z, spriteProvider, 0.1f);
            this.velocityX = velocityX;
            this.velocityY = velocityY;
            this.velocityZ = velocityZ;
            this.particleManager = particleManager;
            this.scale *= 0.75f;
            this.maxAge = 48 + this.random.nextInt(12);
            this.setSpriteForAge(spriteProvider);
        }

        public void setTrail(boolean trail) {
            this.trail = trail;
        }

        public void setFlicker(boolean flicker) {
            this.flicker = flicker;
        }

        @Override
        public void buildGeometry(VertexConsumer vertexConsumer, Camera camera, float tickDelta) {
            if (!this.flicker || this.age < this.maxAge / 3 || (this.age + this.maxAge) / 3 % 2 == 0) {
                super.buildGeometry(vertexConsumer, camera, tickDelta);
            }
        }

        @Override
        public void tick() {
            super.tick();
            if (this.trail && this.age < this.maxAge / 2 && (this.age + this.maxAge) % 2 == 0) {
                Explosion lv = new Explosion(this.world, this.x, this.y, this.z, 0.0, 0.0, 0.0, this.particleManager, this.spriteProvider);
                lv.setAlpha(0.99f);
                lv.setColor(this.red, this.green, this.blue);
                lv.age = lv.maxAge / 2;
                if (this.field_3802) {
                    lv.field_3802 = true;
                    lv.field_3801 = this.field_3801;
                    lv.field_3800 = this.field_3800;
                    lv.field_3799 = this.field_3799;
                }
                lv.flicker = this.flicker;
                this.particleManager.addParticle(lv);
            }
        }
    }

    @Environment(value=EnvType.CLIENT)
    public static class FireworkParticle
    extends NoRenderParticle {
        private static final double[][] CREEPER_PATTERN = new double[][]{{0.0, 0.2}, {0.2, 0.2}, {0.2, 0.6}, {0.6, 0.6}, {0.6, 0.2}, {0.2, 0.2}, {0.2, 0.0}, {0.4, 0.0}, {0.4, -0.6}, {0.2, -0.6}, {0.2, -0.4}, {0.0, -0.4}};
        private static final double[][] STAR_PATTERN = new double[][]{{0.0, 1.0}, {0.3455, 0.309}, {0.9511, 0.309}, {0.3795918367346939, -0.12653061224489795}, {0.6122448979591837, -0.8040816326530612}, {0.0, -0.35918367346938773}};
        private int age;
        private final ParticleManager particleManager;
        private final List<FireworkExplosionComponent> explosions;
        private boolean flicker;

        public FireworkParticle(ClientWorld world, double x, double y, double z, double velocityX, double velocityY, double velocityZ, ParticleManager particleManager, List<FireworkExplosionComponent> fireworkExplosions) {
            super(world, x, y, z);
            this.velocityX = velocityX;
            this.velocityY = velocityY;
            this.velocityZ = velocityZ;
            this.particleManager = particleManager;
            if (fireworkExplosions.isEmpty()) {
                throw new IllegalArgumentException("Cannot create firework starter with no explosions");
            }
            this.explosions = fireworkExplosions;
            this.maxAge = fireworkExplosions.size() * 2 - 1;
            for (FireworkExplosionComponent lv : fireworkExplosions) {
                if (!lv.hasTwinkle()) continue;
                this.flicker = true;
                this.maxAge += 15;
                break;
            }
        }

        @Override
        public void tick() {
            boolean bl;
            if (this.age == 0) {
                bl = this.isFar();
                boolean bl2 = false;
                if (this.explosions.size() >= 3) {
                    bl2 = true;
                } else {
                    for (FireworkExplosionComponent lv : this.explosions) {
                        if (lv.shape() != FireworkExplosionComponent.Type.LARGE_BALL) continue;
                        bl2 = true;
                        break;
                    }
                }
                SoundEvent lv2 = bl2 ? (bl ? SoundEvents.ENTITY_FIREWORK_ROCKET_LARGE_BLAST_FAR : SoundEvents.ENTITY_FIREWORK_ROCKET_LARGE_BLAST) : (bl ? SoundEvents.ENTITY_FIREWORK_ROCKET_BLAST_FAR : SoundEvents.ENTITY_FIREWORK_ROCKET_BLAST);
                this.world.playSound(this.x, this.y, this.z, lv2, SoundCategory.AMBIENT, 20.0f, 0.95f + this.random.nextFloat() * 0.1f, true);
            }
            if (this.age % 2 == 0 && this.age / 2 < this.explosions.size()) {
                int i = this.age / 2;
                FireworkExplosionComponent lv3 = this.explosions.get(i);
                boolean bl3 = lv3.hasTrail();
                boolean bl4 = lv3.hasTwinkle();
                IntList intList = lv3.colors();
                IntList intList2 = lv3.fadeColors();
                if (intList.isEmpty()) {
                    intList = IntList.of(DyeColor.BLACK.getFireworkColor());
                }
                switch (lv3.shape()) {
                    case SMALL_BALL: {
                        this.explodeBall(0.25, 2, intList, intList2, bl3, bl4);
                        break;
                    }
                    case LARGE_BALL: {
                        this.explodeBall(0.5, 4, intList, intList2, bl3, bl4);
                        break;
                    }
                    case STAR: {
                        this.explodeStar(0.5, STAR_PATTERN, intList, intList2, bl3, bl4, false);
                        break;
                    }
                    case CREEPER: {
                        this.explodeStar(0.5, CREEPER_PATTERN, intList, intList2, bl3, bl4, true);
                        break;
                    }
                    case BURST: {
                        this.explodeBurst(intList, intList2, bl3, bl4);
                    }
                }
                int j = intList.getInt(0);
                Particle lv4 = this.particleManager.addParticle(ParticleTypes.FLASH, this.x, this.y, this.z, 0.0, 0.0, 0.0);
                lv4.setColor((float)ColorHelper.Argb.getRed(j) / 255.0f, (float)ColorHelper.Argb.getGreen(j) / 255.0f, (float)ColorHelper.Argb.getBlue(j) / 255.0f);
            }
            ++this.age;
            if (this.age > this.maxAge) {
                if (this.flicker) {
                    bl = this.isFar();
                    SoundEvent lv5 = bl ? SoundEvents.ENTITY_FIREWORK_ROCKET_TWINKLE_FAR : SoundEvents.ENTITY_FIREWORK_ROCKET_TWINKLE;
                    this.world.playSound(this.x, this.y, this.z, lv5, SoundCategory.AMBIENT, 20.0f, 0.9f + this.random.nextFloat() * 0.15f, true);
                }
                this.markDead();
            }
        }

        private boolean isFar() {
            MinecraftClient lv = MinecraftClient.getInstance();
            return lv.gameRenderer.getCamera().getPos().squaredDistanceTo(this.x, this.y, this.z) >= 256.0;
        }

        private void addExplosionParticle(double x, double y, double z, double velocityX, double velocityY, double velocityZ, IntList colors, IntList targetColors, boolean trail, boolean flicker) {
            Explosion lv = (Explosion)this.particleManager.addParticle(ParticleTypes.FIREWORK, x, y, z, velocityX, velocityY, velocityZ);
            lv.setTrail(trail);
            lv.setFlicker(flicker);
            lv.setAlpha(0.99f);
            lv.setColor(Util.getRandom(colors, this.random));
            if (!targetColors.isEmpty()) {
                lv.setTargetColor(Util.getRandom(targetColors, this.random));
            }
        }

        private void explodeBall(double size, int amount, IntList colors, IntList targetColors, boolean trail, boolean flicker) {
            double e = this.x;
            double f = this.y;
            double g = this.z;
            for (int j = -amount; j <= amount; ++j) {
                for (int k = -amount; k <= amount; ++k) {
                    for (int l = -amount; l <= amount; ++l) {
                        double h = (double)k + (this.random.nextDouble() - this.random.nextDouble()) * 0.5;
                        double m = (double)j + (this.random.nextDouble() - this.random.nextDouble()) * 0.5;
                        double n = (double)l + (this.random.nextDouble() - this.random.nextDouble()) * 0.5;
                        double o = Math.sqrt(h * h + m * m + n * n) / size + this.random.nextGaussian() * 0.05;
                        this.addExplosionParticle(e, f, g, h / o, m / o, n / o, colors, targetColors, trail, flicker);
                        if (j == -amount || j == amount || k == -amount || k == amount) continue;
                        l += amount * 2 - 1;
                    }
                }
            }
        }

        private void explodeStar(double size, double[][] pattern, IntList colors, IntList targetColors, boolean trail, boolean flicker, boolean keepShape) {
            double e = pattern[0][0];
            double f = pattern[0][1];
            this.addExplosionParticle(this.x, this.y, this.z, e * size, f * size, 0.0, colors, targetColors, trail, flicker);
            float g = this.random.nextFloat() * (float)Math.PI;
            double h = keepShape ? 0.034 : 0.34;
            for (int i = 0; i < 3; ++i) {
                double j = (double)g + (double)((float)i * (float)Math.PI) * h;
                double k = e;
                double l = f;
                for (int m = 1; m < pattern.length; ++m) {
                    double n = pattern[m][0];
                    double o = pattern[m][1];
                    for (double p = 0.25; p <= 1.0; p += 0.25) {
                        double q = MathHelper.lerp(p, k, n) * size;
                        double r = MathHelper.lerp(p, l, o) * size;
                        double s = q * Math.sin(j);
                        q *= Math.cos(j);
                        for (double t = -1.0; t <= 1.0; t += 2.0) {
                            this.addExplosionParticle(this.x, this.y, this.z, q * t, r, s * t, colors, targetColors, trail, flicker);
                        }
                    }
                    k = n;
                    l = o;
                }
            }
        }

        private void explodeBurst(IntList colors, IntList targetColors, boolean trail, boolean flicker) {
            double d = this.random.nextGaussian() * 0.05;
            double e = this.random.nextGaussian() * 0.05;
            for (int i = 0; i < 70; ++i) {
                double f = this.velocityX * 0.5 + this.random.nextGaussian() * 0.15 + d;
                double g = this.velocityZ * 0.5 + this.random.nextGaussian() * 0.15 + e;
                double h = this.velocityY * 0.5 + this.random.nextDouble() * 0.5;
                this.addExplosionParticle(this.x, this.y, this.z, f, h, g, colors, targetColors, trail, flicker);
            }
        }
    }
}

