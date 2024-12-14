/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 * 
 * Could not load the following classes:
 *  net.fabricmc.api.EnvType
 *  net.fabricmc.api.Environment
 */
package net.minecraft.client.particle;

import java.util.Optional;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleFactory;
import net.minecraft.client.particle.ParticleTextureSheet;
import net.minecraft.client.particle.SpriteBillboardParticle;
import net.minecraft.client.particle.SpriteProvider;
import net.minecraft.client.render.Camera;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.particle.ParticleEffect;
import net.minecraft.particle.VibrationParticleEffect;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.event.PositionSource;
import org.joml.Quaternionf;

@Environment(value=EnvType.CLIENT)
public class VibrationParticle
extends SpriteBillboardParticle {
    private final PositionSource vibration;
    private float field_28250;
    private float field_28248;
    private float field_40507;
    private float field_40508;

    VibrationParticle(ClientWorld world, double x, double y, double z, PositionSource vibration, int maxAge) {
        super(world, x, y, z, 0.0, 0.0, 0.0);
        this.scale = 0.3f;
        this.vibration = vibration;
        this.maxAge = maxAge;
        Optional<Vec3d> optional = vibration.getPos(world);
        if (optional.isPresent()) {
            Vec3d lv = optional.get();
            double g = x - lv.getX();
            double h = y - lv.getY();
            double j = z - lv.getZ();
            this.field_28248 = this.field_28250 = (float)MathHelper.atan2(g, j);
            this.field_40508 = this.field_40507 = (float)MathHelper.atan2(h, Math.sqrt(g * g + j * j));
        }
    }

    @Override
    public void buildGeometry(VertexConsumer vertexConsumer, Camera camera, float tickDelta) {
        float g = MathHelper.sin(((float)this.age + tickDelta - (float)Math.PI * 2) * 0.05f) * 2.0f;
        float h = MathHelper.lerp(tickDelta, this.field_28248, this.field_28250);
        float i = MathHelper.lerp(tickDelta, this.field_40508, this.field_40507) + 1.5707964f;
        Quaternionf quaternionf = new Quaternionf();
        quaternionf.rotationY(h).rotateX(-i).rotateY(g);
        this.method_60373(vertexConsumer, camera, quaternionf, tickDelta);
        quaternionf.rotationY((float)(-Math.PI) + h).rotateX(i).rotateY(g);
        this.method_60373(vertexConsumer, camera, quaternionf, tickDelta);
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
        this.prevPosX = this.x;
        this.prevPosY = this.y;
        this.prevPosZ = this.z;
        if (this.age++ >= this.maxAge) {
            this.markDead();
            return;
        }
        Optional<Vec3d> optional = this.vibration.getPos(this.world);
        if (optional.isEmpty()) {
            this.markDead();
            return;
        }
        int i = this.maxAge - this.age;
        double d = 1.0 / (double)i;
        Vec3d lv = optional.get();
        this.x = MathHelper.lerp(d, this.x, lv.getX());
        this.y = MathHelper.lerp(d, this.y, lv.getY());
        this.z = MathHelper.lerp(d, this.z, lv.getZ());
        double e = this.x - lv.getX();
        double f = this.y - lv.getY();
        double g = this.z - lv.getZ();
        this.field_28248 = this.field_28250;
        this.field_28250 = (float)MathHelper.atan2(e, g);
        this.field_40508 = this.field_40507;
        this.field_40507 = (float)MathHelper.atan2(f, Math.sqrt(e * e + g * g));
    }

    @Environment(value=EnvType.CLIENT)
    public static class Factory
    implements ParticleFactory<VibrationParticleEffect> {
        private final SpriteProvider spriteProvider;

        public Factory(SpriteProvider spriteProvider) {
            this.spriteProvider = spriteProvider;
        }

        @Override
        public Particle createParticle(VibrationParticleEffect arg, ClientWorld arg2, double d, double e, double f, double g, double h, double i) {
            VibrationParticle lv = new VibrationParticle(arg2, d, e, f, arg.getVibration(), arg.getArrivalInTicks());
            lv.setSprite(this.spriteProvider);
            lv.setAlpha(1.0f);
            return lv;
        }

        @Override
        public /* synthetic */ Particle createParticle(ParticleEffect parameters, ClientWorld world, double x, double y, double z, double velocityX, double velocityY, double velocityZ) {
            return this.createParticle((VibrationParticleEffect)parameters, world, x, y, z, velocityX, velocityY, velocityZ);
        }
    }
}

