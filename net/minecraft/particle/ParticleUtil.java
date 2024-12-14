/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.particle;

import java.util.function.Supplier;
import net.minecraft.block.BlockState;
import net.minecraft.particle.BlockStateParticleEffect;
import net.minecraft.particle.ParticleEffect;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.intprovider.IntProvider;
import net.minecraft.util.math.intprovider.UniformIntProvider;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.World;
import net.minecraft.world.WorldAccess;

public class ParticleUtil {
    public static void spawnParticle(World world, BlockPos pos, ParticleEffect effect, IntProvider count) {
        for (Direction lv : Direction.values()) {
            ParticleUtil.spawnParticles(world, pos, effect, count, lv, () -> ParticleUtil.getRandomVelocity(arg.random), 0.55);
        }
    }

    public static void spawnParticles(World world, BlockPos pos, ParticleEffect effect, IntProvider count, Direction direction, Supplier<Vec3d> velocity, double offsetMultiplier) {
        int i = count.get(world.random);
        for (int j = 0; j < i; ++j) {
            ParticleUtil.spawnParticle(world, pos, direction, effect, velocity.get(), offsetMultiplier);
        }
    }

    private static Vec3d getRandomVelocity(Random random) {
        return new Vec3d(MathHelper.nextDouble(random, -0.5, 0.5), MathHelper.nextDouble(random, -0.5, 0.5), MathHelper.nextDouble(random, -0.5, 0.5));
    }

    public static void spawnParticle(Direction.Axis axis, World world, BlockPos pos, double variance, ParticleEffect effect, UniformIntProvider range) {
        Vec3d lv = Vec3d.ofCenter(pos);
        boolean bl = axis == Direction.Axis.X;
        boolean bl2 = axis == Direction.Axis.Y;
        boolean bl3 = axis == Direction.Axis.Z;
        int i = range.get(world.random);
        for (int j = 0; j < i; ++j) {
            double e = lv.x + MathHelper.nextDouble(world.random, -1.0, 1.0) * (bl ? 0.5 : variance);
            double f = lv.y + MathHelper.nextDouble(world.random, -1.0, 1.0) * (bl2 ? 0.5 : variance);
            double g = lv.z + MathHelper.nextDouble(world.random, -1.0, 1.0) * (bl3 ? 0.5 : variance);
            double h = bl ? MathHelper.nextDouble(world.random, -1.0, 1.0) : 0.0;
            double k = bl2 ? MathHelper.nextDouble(world.random, -1.0, 1.0) : 0.0;
            double l = bl3 ? MathHelper.nextDouble(world.random, -1.0, 1.0) : 0.0;
            world.addParticle(effect, e, f, g, h, k, l);
        }
    }

    public static void spawnParticle(World world, BlockPos pos, Direction direction, ParticleEffect effect, Vec3d velocity, double offsetMultiplier) {
        Vec3d lv = Vec3d.ofCenter(pos);
        int i = direction.getOffsetX();
        int j = direction.getOffsetY();
        int k = direction.getOffsetZ();
        double e = lv.x + (i == 0 ? MathHelper.nextDouble(world.random, -0.5, 0.5) : (double)i * offsetMultiplier);
        double f = lv.y + (j == 0 ? MathHelper.nextDouble(world.random, -0.5, 0.5) : (double)j * offsetMultiplier);
        double g = lv.z + (k == 0 ? MathHelper.nextDouble(world.random, -0.5, 0.5) : (double)k * offsetMultiplier);
        double h = i == 0 ? velocity.getX() : 0.0;
        double l = j == 0 ? velocity.getY() : 0.0;
        double m = k == 0 ? velocity.getZ() : 0.0;
        world.addParticle(effect, e, f, g, h, l, m);
    }

    public static void spawnParticle(World world, BlockPos pos, Random random, ParticleEffect effect) {
        double d = (double)pos.getX() + random.nextDouble();
        double e = (double)pos.getY() - 0.05;
        double f = (double)pos.getZ() + random.nextDouble();
        world.addParticle(effect, d, e, f, 0.0, 0.0, 0.0);
    }

    public static void spawnParticlesAround(WorldAccess world, BlockPos pos, int count, ParticleEffect effect) {
        double d = 0.5;
        BlockState lv = world.getBlockState(pos);
        double e = lv.isAir() ? 1.0 : lv.getOutlineShape(world, pos).getMax(Direction.Axis.Y);
        ParticleUtil.spawnParticlesAround(world, pos, count, 0.5, e, true, effect);
    }

    public static void spawnParticlesAround(WorldAccess world, BlockPos pos, int count, double horizontalOffset, double verticalOffset, boolean force, ParticleEffect effect) {
        Random lv = world.getRandom();
        for (int j = 0; j < count; ++j) {
            double f = lv.nextGaussian() * 0.02;
            double g = lv.nextGaussian() * 0.02;
            double h = lv.nextGaussian() * 0.02;
            double k = 0.5 - horizontalOffset;
            double l = (double)pos.getX() + k + lv.nextDouble() * horizontalOffset * 2.0;
            double m = (double)pos.getY() + lv.nextDouble() * verticalOffset;
            double n = (double)pos.getZ() + k + lv.nextDouble() * horizontalOffset * 2.0;
            if (!force && world.getBlockState(BlockPos.ofFloored(l, m, n).down()).isAir()) continue;
            world.addParticle(effect, l, m, n, f, g, h);
        }
    }

    public static void spawnSmashAttackParticles(WorldAccess world, BlockPos pos, int count) {
        double k;
        double h;
        double g;
        double f;
        double e;
        double d;
        Vec3d lv = pos.toCenterPos().add(0.0, 0.5, 0.0);
        BlockStateParticleEffect lv2 = new BlockStateParticleEffect(ParticleTypes.DUST_PILLAR, world.getBlockState(pos));
        int j = 0;
        while ((float)j < (float)count / 3.0f) {
            d = lv.x + world.getRandom().nextGaussian() / 2.0;
            e = lv.y;
            f = lv.z + world.getRandom().nextGaussian() / 2.0;
            g = world.getRandom().nextGaussian() * (double)0.2f;
            h = world.getRandom().nextGaussian() * (double)0.2f;
            k = world.getRandom().nextGaussian() * (double)0.2f;
            world.addParticle(lv2, d, e, f, g, h, k);
            ++j;
        }
        j = 0;
        while ((float)j < (float)count / 1.5f) {
            d = lv.x + 3.5 * Math.cos(j) + world.getRandom().nextGaussian() / 2.0;
            e = lv.y;
            f = lv.z + 3.5 * Math.sin(j) + world.getRandom().nextGaussian() / 2.0;
            g = world.getRandom().nextGaussian() * (double)0.05f;
            h = world.getRandom().nextGaussian() * (double)0.05f;
            k = world.getRandom().nextGaussian() * (double)0.05f;
            world.addParticle(lv2, d, e, f, g, h, k);
            ++j;
        }
    }
}

