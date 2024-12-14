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
import net.minecraft.client.particle.ParticleTextureSheet;
import net.minecraft.client.particle.SpriteBillboardParticle;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.fluid.Fluid;
import net.minecraft.fluid.FluidState;
import net.minecraft.fluid.Fluids;
import net.minecraft.particle.ParticleEffect;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.particle.SimpleParticleType;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvent;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;

@Environment(value=EnvType.CLIENT)
public class BlockLeakParticle
extends SpriteBillboardParticle {
    private final Fluid fluid;
    protected boolean obsidianTear;

    BlockLeakParticle(ClientWorld world, double x, double y, double z, Fluid fluid) {
        super(world, x, y, z);
        this.setBoundingBoxSpacing(0.01f, 0.01f);
        this.gravityStrength = 0.06f;
        this.fluid = fluid;
    }

    protected Fluid getFluid() {
        return this.fluid;
    }

    @Override
    public ParticleTextureSheet getType() {
        return ParticleTextureSheet.PARTICLE_SHEET_OPAQUE;
    }

    @Override
    public int getBrightness(float tint) {
        if (this.obsidianTear) {
            return 240;
        }
        return super.getBrightness(tint);
    }

    @Override
    public void tick() {
        this.prevPosX = this.x;
        this.prevPosY = this.y;
        this.prevPosZ = this.z;
        this.updateAge();
        if (this.dead) {
            return;
        }
        this.velocityY -= (double)this.gravityStrength;
        this.move(this.velocityX, this.velocityY, this.velocityZ);
        this.updateVelocity();
        if (this.dead) {
            return;
        }
        this.velocityX *= (double)0.98f;
        this.velocityY *= (double)0.98f;
        this.velocityZ *= (double)0.98f;
        if (this.fluid == Fluids.EMPTY) {
            return;
        }
        BlockPos lv = BlockPos.ofFloored(this.x, this.y, this.z);
        FluidState lv2 = this.world.getFluidState(lv);
        if (lv2.getFluid() == this.fluid && this.y < (double)((float)lv.getY() + lv2.getHeight(this.world, lv))) {
            this.markDead();
        }
    }

    protected void updateAge() {
        if (this.maxAge-- <= 0) {
            this.markDead();
        }
    }

    protected void updateVelocity() {
    }

    public static SpriteBillboardParticle createDrippingWater(SimpleParticleType type, ClientWorld world, double x, double y, double z, double velocityX, double velocityY, double velocityZ) {
        Dripping lv = new Dripping(world, x, y, z, Fluids.WATER, ParticleTypes.FALLING_WATER);
        lv.setColor(0.2f, 0.3f, 1.0f);
        return lv;
    }

    public static SpriteBillboardParticle createFallingWater(SimpleParticleType type, ClientWorld world, double x, double y, double z, double velocityX, double velocityY, double velocityZ) {
        ContinuousFalling lv = new ContinuousFalling(world, x, y, z, (Fluid)Fluids.WATER, ParticleTypes.SPLASH);
        lv.setColor(0.2f, 0.3f, 1.0f);
        return lv;
    }

    public static SpriteBillboardParticle createDrippingLava(SimpleParticleType type, ClientWorld world, double x, double y, double z, double velocityX, double velocityY, double velocityZ) {
        return new DrippingLava(world, x, y, z, Fluids.LAVA, ParticleTypes.FALLING_LAVA);
    }

    public static SpriteBillboardParticle createFallingLava(SimpleParticleType type, ClientWorld world, double x, double y, double z, double velocityX, double velocityY, double velocityZ) {
        ContinuousFalling lv = new ContinuousFalling(world, x, y, z, (Fluid)Fluids.LAVA, ParticleTypes.LANDING_LAVA);
        lv.setColor(1.0f, 0.2857143f, 0.083333336f);
        return lv;
    }

    public static SpriteBillboardParticle createLandingLava(SimpleParticleType type, ClientWorld world, double x, double y, double z, double velocityX, double velocityY, double velocityZ) {
        Landing lv = new Landing(world, x, y, z, Fluids.LAVA);
        lv.setColor(1.0f, 0.2857143f, 0.083333336f);
        return lv;
    }

    public static SpriteBillboardParticle createDrippingHoney(SimpleParticleType type, ClientWorld world, double x, double y, double z, double velocityX, double velocityY, double velocityZ) {
        Dripping lv = new Dripping(world, x, y, z, Fluids.EMPTY, ParticleTypes.FALLING_HONEY);
        lv.gravityStrength *= 0.01f;
        lv.maxAge = 100;
        lv.setColor(0.622f, 0.508f, 0.082f);
        return lv;
    }

    public static SpriteBillboardParticle createFallingHoney(SimpleParticleType type, ClientWorld world, double x, double y, double z, double velocityX, double velocityY, double velocityZ) {
        FallingHoney lv = new FallingHoney(world, x, y, z, Fluids.EMPTY, ParticleTypes.LANDING_HONEY);
        lv.gravityStrength = 0.01f;
        lv.setColor(0.582f, 0.448f, 0.082f);
        return lv;
    }

    public static SpriteBillboardParticle createLandingHoney(SimpleParticleType type, ClientWorld world, double x, double y, double z, double velocityX, double velocityY, double velocityZ) {
        Landing lv = new Landing(world, x, y, z, Fluids.EMPTY);
        lv.maxAge = (int)(128.0 / (Math.random() * 0.8 + 0.2));
        lv.setColor(0.522f, 0.408f, 0.082f);
        return lv;
    }

    public static SpriteBillboardParticle createDrippingDripstoneWater(SimpleParticleType type, ClientWorld world, double x, double y, double z, double velocityX, double velocityY, double velocityZ) {
        Dripping lv = new Dripping(world, x, y, z, Fluids.WATER, ParticleTypes.FALLING_DRIPSTONE_WATER);
        lv.setColor(0.2f, 0.3f, 1.0f);
        return lv;
    }

    public static SpriteBillboardParticle createFallingDripstoneWater(SimpleParticleType type, ClientWorld world, double x, double y, double z, double velocityX, double velocityY, double velocityZ) {
        DripstoneLavaDrip lv = new DripstoneLavaDrip(world, x, y, z, (Fluid)Fluids.WATER, ParticleTypes.SPLASH);
        lv.setColor(0.2f, 0.3f, 1.0f);
        return lv;
    }

    public static SpriteBillboardParticle createDrippingDripstoneLava(SimpleParticleType type, ClientWorld world, double x, double y, double z, double velocityX, double velocityY, double velocityZ) {
        return new DrippingLava(world, x, y, z, Fluids.LAVA, ParticleTypes.FALLING_DRIPSTONE_LAVA);
    }

    public static SpriteBillboardParticle createFallingDripstoneLava(SimpleParticleType type, ClientWorld world, double x, double y, double z, double velocityX, double velocityY, double velocityZ) {
        DripstoneLavaDrip lv = new DripstoneLavaDrip(world, x, y, z, (Fluid)Fluids.LAVA, ParticleTypes.LANDING_LAVA);
        lv.setColor(1.0f, 0.2857143f, 0.083333336f);
        return lv;
    }

    public static SpriteBillboardParticle createFallingNectar(SimpleParticleType type, ClientWorld world, double x, double y, double z, double velocityX, double velocityY, double velocityZ) {
        Falling lv = new Falling(world, x, y, z, Fluids.EMPTY);
        lv.maxAge = (int)(16.0 / (Math.random() * 0.8 + 0.2));
        lv.gravityStrength = 0.007f;
        lv.setColor(0.92f, 0.782f, 0.72f);
        return lv;
    }

    public static SpriteBillboardParticle createFallingSporeBlossom(SimpleParticleType type, ClientWorld world, double x, double y, double z, double velocityX, double velocityY, double velocityZ) {
        int j = (int)(64.0f / MathHelper.nextBetween(world.getRandom(), 0.1f, 0.9f));
        Falling lv = new Falling(world, x, y, z, Fluids.EMPTY, j);
        lv.gravityStrength = 0.005f;
        lv.setColor(0.32f, 0.5f, 0.22f);
        return lv;
    }

    public static SpriteBillboardParticle createDrippingObsidianTear(SimpleParticleType type, ClientWorld world, double x, double y, double z, double velocityX, double velocityY, double velocityZ) {
        Dripping lv = new Dripping(world, x, y, z, Fluids.EMPTY, ParticleTypes.FALLING_OBSIDIAN_TEAR);
        lv.obsidianTear = true;
        lv.gravityStrength *= 0.01f;
        lv.maxAge = 100;
        lv.setColor(0.51171875f, 0.03125f, 0.890625f);
        return lv;
    }

    public static SpriteBillboardParticle createFallingObsidianTear(SimpleParticleType type, ClientWorld world, double x, double y, double z, double velocityX, double velocityY, double velocityZ) {
        ContinuousFalling lv = new ContinuousFalling(world, x, y, z, Fluids.EMPTY, ParticleTypes.LANDING_OBSIDIAN_TEAR);
        lv.obsidianTear = true;
        lv.gravityStrength = 0.01f;
        lv.setColor(0.51171875f, 0.03125f, 0.890625f);
        return lv;
    }

    public static SpriteBillboardParticle createLandingObsidianTear(SimpleParticleType type, ClientWorld world, double x, double y, double z, double velocityX, double velocityY, double velocityZ) {
        Landing lv = new Landing(world, x, y, z, Fluids.EMPTY);
        lv.obsidianTear = true;
        lv.maxAge = (int)(28.0 / (Math.random() * 0.8 + 0.2));
        lv.setColor(0.51171875f, 0.03125f, 0.890625f);
        return lv;
    }

    @Environment(value=EnvType.CLIENT)
    static class Dripping
    extends BlockLeakParticle {
        private final ParticleEffect nextParticle;

        Dripping(ClientWorld world, double x, double y, double z, Fluid fluid, ParticleEffect nextParticle) {
            super(world, x, y, z, fluid);
            this.nextParticle = nextParticle;
            this.gravityStrength *= 0.02f;
            this.maxAge = 40;
        }

        @Override
        protected void updateAge() {
            if (this.maxAge-- <= 0) {
                this.markDead();
                this.world.addParticle(this.nextParticle, this.x, this.y, this.z, this.velocityX, this.velocityY, this.velocityZ);
            }
        }

        @Override
        protected void updateVelocity() {
            this.velocityX *= 0.02;
            this.velocityY *= 0.02;
            this.velocityZ *= 0.02;
        }
    }

    @Environment(value=EnvType.CLIENT)
    static class ContinuousFalling
    extends Falling {
        protected final ParticleEffect nextParticle;

        ContinuousFalling(ClientWorld world, double x, double y, double z, Fluid fluid, ParticleEffect nextParticle) {
            super(world, x, y, z, fluid);
            this.nextParticle = nextParticle;
        }

        @Override
        protected void updateVelocity() {
            if (this.onGround) {
                this.markDead();
                this.world.addParticle(this.nextParticle, this.x, this.y, this.z, 0.0, 0.0, 0.0);
            }
        }
    }

    @Environment(value=EnvType.CLIENT)
    static class DrippingLava
    extends Dripping {
        DrippingLava(ClientWorld arg, double d, double e, double f, Fluid arg2, ParticleEffect arg3) {
            super(arg, d, e, f, arg2, arg3);
        }

        @Override
        protected void updateAge() {
            this.red = 1.0f;
            this.green = 16.0f / (float)(40 - this.maxAge + 16);
            this.blue = 4.0f / (float)(40 - this.maxAge + 8);
            super.updateAge();
        }
    }

    @Environment(value=EnvType.CLIENT)
    static class Landing
    extends BlockLeakParticle {
        Landing(ClientWorld arg, double d, double e, double f, Fluid arg2) {
            super(arg, d, e, f, arg2);
            this.maxAge = (int)(16.0 / (Math.random() * 0.8 + 0.2));
        }
    }

    @Environment(value=EnvType.CLIENT)
    static class FallingHoney
    extends ContinuousFalling {
        FallingHoney(ClientWorld arg, double d, double e, double f, Fluid arg2, ParticleEffect arg3) {
            super(arg, d, e, f, arg2, arg3);
        }

        @Override
        protected void updateVelocity() {
            if (this.onGround) {
                this.markDead();
                this.world.addParticle(this.nextParticle, this.x, this.y, this.z, 0.0, 0.0, 0.0);
                float f = MathHelper.nextBetween(this.random, 0.3f, 1.0f);
                this.world.playSound(this.x, this.y, this.z, SoundEvents.BLOCK_BEEHIVE_DRIP, SoundCategory.BLOCKS, f, 1.0f, false);
            }
        }
    }

    @Environment(value=EnvType.CLIENT)
    static class DripstoneLavaDrip
    extends ContinuousFalling {
        DripstoneLavaDrip(ClientWorld arg, double d, double e, double f, Fluid arg2, ParticleEffect arg3) {
            super(arg, d, e, f, arg2, arg3);
        }

        @Override
        protected void updateVelocity() {
            if (this.onGround) {
                this.markDead();
                this.world.addParticle(this.nextParticle, this.x, this.y, this.z, 0.0, 0.0, 0.0);
                SoundEvent lv = this.getFluid() == Fluids.LAVA ? SoundEvents.BLOCK_POINTED_DRIPSTONE_DRIP_LAVA : SoundEvents.BLOCK_POINTED_DRIPSTONE_DRIP_WATER;
                float f = MathHelper.nextBetween(this.random, 0.3f, 1.0f);
                this.world.playSound(this.x, this.y, this.z, lv, SoundCategory.BLOCKS, f, 1.0f, false);
            }
        }
    }

    @Environment(value=EnvType.CLIENT)
    static class Falling
    extends BlockLeakParticle {
        Falling(ClientWorld arg, double d, double e, double f, Fluid arg2) {
            this(arg, d, e, f, arg2, (int)(64.0 / (Math.random() * 0.8 + 0.2)));
        }

        Falling(ClientWorld world, double x, double y, double z, Fluid fluid, int maxAge) {
            super(world, x, y, z, fluid);
            this.maxAge = maxAge;
        }

        @Override
        protected void updateVelocity() {
            if (this.onGround) {
                this.markDead();
            }
        }
    }
}

