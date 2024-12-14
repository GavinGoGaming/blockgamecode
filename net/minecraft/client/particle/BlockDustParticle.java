/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 * 
 * Could not load the following classes:
 *  net.fabricmc.api.EnvType
 *  net.fabricmc.api.Environment
 *  org.jetbrains.annotations.Nullable
 */
package net.minecraft.client.particle;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleFactory;
import net.minecraft.client.particle.ParticleTextureSheet;
import net.minecraft.client.particle.SpriteBillboardParticle;
import net.minecraft.client.render.WorldRenderer;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.particle.BlockStateParticleEffect;
import net.minecraft.particle.ParticleEffect;
import net.minecraft.util.math.BlockPos;
import org.jetbrains.annotations.Nullable;

@Environment(value=EnvType.CLIENT)
public class BlockDustParticle
extends SpriteBillboardParticle {
    private final BlockPos blockPos;
    private final float sampleU;
    private final float sampleV;

    public BlockDustParticle(ClientWorld world, double x, double y, double z, double velocityX, double velocityY, double velocityZ, BlockState state) {
        this(world, x, y, z, velocityX, velocityY, velocityZ, state, BlockPos.ofFloored(x, y, z));
    }

    public BlockDustParticle(ClientWorld world, double x, double y, double z, double velocityX, double velocityY, double velocityZ, BlockState state, BlockPos blockPos) {
        super(world, x, y, z, velocityX, velocityY, velocityZ);
        this.blockPos = blockPos;
        this.setSprite(MinecraftClient.getInstance().getBlockRenderManager().getModels().getModelParticleSprite(state));
        this.gravityStrength = 1.0f;
        this.red = 0.6f;
        this.green = 0.6f;
        this.blue = 0.6f;
        if (!state.isOf(Blocks.GRASS_BLOCK)) {
            int j = MinecraftClient.getInstance().getBlockColors().getColor(state, world, blockPos, 0);
            this.red *= (float)(j >> 16 & 0xFF) / 255.0f;
            this.green *= (float)(j >> 8 & 0xFF) / 255.0f;
            this.blue *= (float)(j & 0xFF) / 255.0f;
        }
        this.scale /= 2.0f;
        this.sampleU = this.random.nextFloat() * 3.0f;
        this.sampleV = this.random.nextFloat() * 3.0f;
    }

    @Override
    public ParticleTextureSheet getType() {
        return ParticleTextureSheet.TERRAIN_SHEET;
    }

    @Override
    protected float getMinU() {
        return this.sprite.getFrameU((this.sampleU + 1.0f) / 4.0f);
    }

    @Override
    protected float getMaxU() {
        return this.sprite.getFrameU(this.sampleU / 4.0f);
    }

    @Override
    protected float getMinV() {
        return this.sprite.getFrameV(this.sampleV / 4.0f);
    }

    @Override
    protected float getMaxV() {
        return this.sprite.getFrameV((this.sampleV + 1.0f) / 4.0f);
    }

    @Override
    public int getBrightness(float tint) {
        int i = super.getBrightness(tint);
        if (i == 0 && this.world.isChunkLoaded(this.blockPos)) {
            return WorldRenderer.getLightmapCoordinates(this.world, this.blockPos);
        }
        return i;
    }

    @Nullable
    static BlockDustParticle create(BlockStateParticleEffect parameters, ClientWorld world, double x, double y, double z, double velocityX, double velocityY, double velocityZ) {
        BlockState lv = parameters.getBlockState();
        if (lv.isAir() || lv.isOf(Blocks.MOVING_PISTON) || !lv.hasBlockBreakParticles()) {
            return null;
        }
        return new BlockDustParticle(world, x, y, z, velocityX, velocityY, velocityZ, lv);
    }

    @Environment(value=EnvType.CLIENT)
    public static class DustPillarFactory
    implements ParticleFactory<BlockStateParticleEffect> {
        @Override
        @Nullable
        public Particle createParticle(BlockStateParticleEffect arg, ClientWorld arg2, double d, double e, double f, double g, double h, double i) {
            BlockDustParticle lv = BlockDustParticle.create(arg, arg2, d, e, f, g, h, i);
            if (lv != null) {
                lv.setVelocity(arg2.random.nextGaussian() / 30.0, h + arg2.random.nextGaussian() / 2.0, arg2.random.nextGaussian() / 30.0);
                lv.setMaxAge(arg2.random.nextInt(20) + 20);
            }
            return lv;
        }

        @Override
        @Nullable
        public /* synthetic */ Particle createParticle(ParticleEffect parameters, ClientWorld world, double x, double y, double z, double velocityX, double velocityY, double velocityZ) {
            return this.createParticle((BlockStateParticleEffect)parameters, world, x, y, z, velocityX, velocityY, velocityZ);
        }
    }

    @Environment(value=EnvType.CLIENT)
    public static class Factory
    implements ParticleFactory<BlockStateParticleEffect> {
        @Override
        @Nullable
        public Particle createParticle(BlockStateParticleEffect arg, ClientWorld arg2, double d, double e, double f, double g, double h, double i) {
            return BlockDustParticle.create(arg, arg2, d, e, f, g, h, i);
        }

        @Override
        @Nullable
        public /* synthetic */ Particle createParticle(ParticleEffect parameters, ClientWorld world, double x, double y, double z, double velocityX, double velocityY, double velocityZ) {
            return this.createParticle((BlockStateParticleEffect)parameters, world, x, y, z, velocityX, velocityY, velocityZ);
        }
    }
}

