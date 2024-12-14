/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 * 
 * Could not load the following classes:
 *  net.fabricmc.api.EnvType
 *  net.fabricmc.api.Environment
 */
package net.minecraft.client.particle;

import java.util.List;
import java.util.Optional;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.particle.ParticleTextureSheet;
import net.minecraft.client.render.Camera;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.WorldRenderer;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.Entity;
import net.minecraft.particle.ParticleGroup;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.random.Random;

@Environment(value=EnvType.CLIENT)
public abstract class Particle {
    private static final Box EMPTY_BOUNDING_BOX = new Box(0.0, 0.0, 0.0, 0.0, 0.0, 0.0);
    private static final double MAX_SQUARED_COLLISION_CHECK_DISTANCE = MathHelper.square(100.0);
    protected final ClientWorld world;
    protected double prevPosX;
    protected double prevPosY;
    protected double prevPosZ;
    protected double x;
    protected double y;
    protected double z;
    protected double velocityX;
    protected double velocityY;
    protected double velocityZ;
    private Box boundingBox = EMPTY_BOUNDING_BOX;
    protected boolean onGround;
    protected boolean collidesWithWorld = true;
    private boolean stopped;
    protected boolean dead;
    protected float spacingXZ = 0.6f;
    protected float spacingY = 1.8f;
    protected final Random random = Random.create();
    protected int age;
    protected int maxAge;
    protected float gravityStrength;
    protected float red = 1.0f;
    protected float green = 1.0f;
    protected float blue = 1.0f;
    protected float alpha = 1.0f;
    protected float angle;
    protected float prevAngle;
    protected float velocityMultiplier = 0.98f;
    protected boolean ascending = false;

    protected Particle(ClientWorld world, double x, double y, double z) {
        this.world = world;
        this.setBoundingBoxSpacing(0.2f, 0.2f);
        this.setPos(x, y, z);
        this.prevPosX = x;
        this.prevPosY = y;
        this.prevPosZ = z;
        this.maxAge = (int)(4.0f / (this.random.nextFloat() * 0.9f + 0.1f));
    }

    public Particle(ClientWorld world, double x, double y, double z, double velocityX, double velocityY, double velocityZ) {
        this(world, x, y, z);
        this.velocityX = velocityX + (Math.random() * 2.0 - 1.0) * (double)0.4f;
        this.velocityY = velocityY + (Math.random() * 2.0 - 1.0) * (double)0.4f;
        this.velocityZ = velocityZ + (Math.random() * 2.0 - 1.0) * (double)0.4f;
        double j = (Math.random() + Math.random() + 1.0) * (double)0.15f;
        double k = Math.sqrt(this.velocityX * this.velocityX + this.velocityY * this.velocityY + this.velocityZ * this.velocityZ);
        this.velocityX = this.velocityX / k * j * (double)0.4f;
        this.velocityY = this.velocityY / k * j * (double)0.4f + (double)0.1f;
        this.velocityZ = this.velocityZ / k * j * (double)0.4f;
    }

    public Particle move(float speed) {
        this.velocityX *= (double)speed;
        this.velocityY = (this.velocityY - (double)0.1f) * (double)speed + (double)0.1f;
        this.velocityZ *= (double)speed;
        return this;
    }

    public void setVelocity(double velocityX, double velocityY, double velocityZ) {
        this.velocityX = velocityX;
        this.velocityY = velocityY;
        this.velocityZ = velocityZ;
    }

    public Particle scale(float scale) {
        this.setBoundingBoxSpacing(0.2f * scale, 0.2f * scale);
        return this;
    }

    public void setColor(float red, float green, float blue) {
        this.red = red;
        this.green = green;
        this.blue = blue;
    }

    protected void setAlpha(float alpha) {
        this.alpha = alpha;
    }

    public void setMaxAge(int maxAge) {
        this.maxAge = maxAge;
    }

    public int getMaxAge() {
        return this.maxAge;
    }

    public void tick() {
        this.prevPosX = this.x;
        this.prevPosY = this.y;
        this.prevPosZ = this.z;
        if (this.age++ >= this.maxAge) {
            this.markDead();
            return;
        }
        this.velocityY -= 0.04 * (double)this.gravityStrength;
        this.move(this.velocityX, this.velocityY, this.velocityZ);
        if (this.ascending && this.y == this.prevPosY) {
            this.velocityX *= 1.1;
            this.velocityZ *= 1.1;
        }
        this.velocityX *= (double)this.velocityMultiplier;
        this.velocityY *= (double)this.velocityMultiplier;
        this.velocityZ *= (double)this.velocityMultiplier;
        if (this.onGround) {
            this.velocityX *= (double)0.7f;
            this.velocityZ *= (double)0.7f;
        }
    }

    public abstract void buildGeometry(VertexConsumer var1, Camera var2, float var3);

    public abstract ParticleTextureSheet getType();

    public String toString() {
        return this.getClass().getSimpleName() + ", Pos (" + this.x + "," + this.y + "," + this.z + "), RGBA (" + this.red + "," + this.green + "," + this.blue + "," + this.alpha + "), Age " + this.age;
    }

    public void markDead() {
        this.dead = true;
    }

    protected void setBoundingBoxSpacing(float spacingXZ, float spacingY) {
        if (spacingXZ != this.spacingXZ || spacingY != this.spacingY) {
            this.spacingXZ = spacingXZ;
            this.spacingY = spacingY;
            Box lv = this.getBoundingBox();
            double d = (lv.minX + lv.maxX - (double)spacingXZ) / 2.0;
            double e = (lv.minZ + lv.maxZ - (double)spacingXZ) / 2.0;
            this.setBoundingBox(new Box(d, lv.minY, e, d + (double)this.spacingXZ, lv.minY + (double)this.spacingY, e + (double)this.spacingXZ));
        }
    }

    public void setPos(double x, double y, double z) {
        this.x = x;
        this.y = y;
        this.z = z;
        float g = this.spacingXZ / 2.0f;
        float h = this.spacingY;
        this.setBoundingBox(new Box(x - (double)g, y, z - (double)g, x + (double)g, y + (double)h, z + (double)g));
    }

    public void move(double dx, double dy, double dz) {
        if (this.stopped) {
            return;
        }
        double g = dx;
        double h = dy;
        double i = dz;
        if (this.collidesWithWorld && (dx != 0.0 || dy != 0.0 || dz != 0.0) && dx * dx + dy * dy + dz * dz < MAX_SQUARED_COLLISION_CHECK_DISTANCE) {
            Vec3d lv = Entity.adjustMovementForCollisions(null, new Vec3d(dx, dy, dz), this.getBoundingBox(), this.world, List.of());
            dx = lv.x;
            dy = lv.y;
            dz = lv.z;
        }
        if (dx != 0.0 || dy != 0.0 || dz != 0.0) {
            this.setBoundingBox(this.getBoundingBox().offset(dx, dy, dz));
            this.repositionFromBoundingBox();
        }
        if (Math.abs(h) >= (double)1.0E-5f && Math.abs(dy) < (double)1.0E-5f) {
            this.stopped = true;
        }
        boolean bl = this.onGround = h != dy && h < 0.0;
        if (g != dx) {
            this.velocityX = 0.0;
        }
        if (i != dz) {
            this.velocityZ = 0.0;
        }
    }

    protected void repositionFromBoundingBox() {
        Box lv = this.getBoundingBox();
        this.x = (lv.minX + lv.maxX) / 2.0;
        this.y = lv.minY;
        this.z = (lv.minZ + lv.maxZ) / 2.0;
    }

    protected int getBrightness(float tint) {
        BlockPos lv = BlockPos.ofFloored(this.x, this.y, this.z);
        if (this.world.isChunkLoaded(lv)) {
            return WorldRenderer.getLightmapCoordinates(this.world, lv);
        }
        return 0;
    }

    public boolean isAlive() {
        return !this.dead;
    }

    public Box getBoundingBox() {
        return this.boundingBox;
    }

    public void setBoundingBox(Box boundingBox) {
        this.boundingBox = boundingBox;
    }

    public Optional<ParticleGroup> getGroup() {
        return Optional.empty();
    }

    @Environment(value=EnvType.CLIENT)
    public record DynamicAlpha(float startAlpha, float endAlpha, float startAtNormalizedAge, float endAtNormalizedAge) {
        public static final DynamicAlpha OPAQUE = new DynamicAlpha(1.0f, 1.0f, 0.0f, 1.0f);

        public boolean isOpaque() {
            return this.startAlpha >= 1.0f && this.endAlpha >= 1.0f;
        }

        public float getAlpha(int age, int maxAge, float tickDelta) {
            if (MathHelper.approximatelyEquals(this.startAlpha, this.endAlpha)) {
                return this.startAlpha;
            }
            float g = MathHelper.getLerpProgress(((float)age + tickDelta) / (float)maxAge, this.startAtNormalizedAge, this.endAtNormalizedAge);
            return MathHelper.clampedLerp(this.startAlpha, this.endAlpha, g);
        }
    }
}

