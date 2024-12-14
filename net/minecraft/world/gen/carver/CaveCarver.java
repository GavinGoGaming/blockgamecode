/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.world.gen.carver;

import com.mojang.serialization.Codec;
import java.util.function.Function;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.ChunkSectionPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.gen.carver.Carver;
import net.minecraft.world.gen.carver.CarverContext;
import net.minecraft.world.gen.carver.CarvingMask;
import net.minecraft.world.gen.carver.CaveCarverConfig;
import net.minecraft.world.gen.chunk.AquiferSampler;

public class CaveCarver
extends Carver<CaveCarverConfig> {
    public CaveCarver(Codec<CaveCarverConfig> codec) {
        super(codec);
    }

    @Override
    public boolean shouldCarve(CaveCarverConfig arg, Random arg2) {
        return arg2.nextFloat() <= arg.probability;
    }

    @Override
    public boolean carve(CarverContext arg, CaveCarverConfig arg2, Chunk arg3, Function<BlockPos, RegistryEntry<Biome>> function, Random arg4, AquiferSampler arg5, ChunkPos arg6, CarvingMask arg7) {
        int i = ChunkSectionPos.getBlockCoord(this.getBranchFactor() * 2 - 1);
        int j = arg4.nextInt(arg4.nextInt(arg4.nextInt(this.getMaxCaveCount()) + 1) + 1);
        for (int k = 0; k < j; ++k) {
            float o;
            double d = arg6.getOffsetX(arg4.nextInt(16));
            double e = arg2.y.get(arg4, arg);
            double f = arg6.getOffsetZ(arg4.nextInt(16));
            double g = arg2.horizontalRadiusMultiplier.get(arg4);
            double h = arg2.verticalRadiusMultiplier.get(arg4);
            double l = arg2.floorLevel.get(arg4);
            Carver.SkipPredicate lv = (contextx, scaledRelativeX, scaledRelativeY, scaledRelativeZ, y) -> CaveCarver.isPositionExcluded(scaledRelativeX, scaledRelativeY, scaledRelativeZ, l);
            int m = 1;
            if (arg4.nextInt(4) == 0) {
                double n = arg2.yScale.get(arg4);
                o = 1.0f + arg4.nextFloat() * 6.0f;
                this.carveCave(arg, arg2, arg3, function, arg5, d, e, f, o, n, arg7, lv);
                m += arg4.nextInt(4);
            }
            for (int p = 0; p < m; ++p) {
                float q = arg4.nextFloat() * ((float)Math.PI * 2);
                o = (arg4.nextFloat() - 0.5f) / 4.0f;
                float r = this.getTunnelSystemWidth(arg4);
                int s = i - arg4.nextInt(i / 4);
                boolean t = false;
                this.carveTunnels(arg, arg2, arg3, function, arg4.nextLong(), arg5, d, e, f, g, h, r, q, o, 0, s, this.getTunnelSystemHeightWidthRatio(), arg7, lv);
            }
        }
        return true;
    }

    protected int getMaxCaveCount() {
        return 15;
    }

    protected float getTunnelSystemWidth(Random random) {
        float f = random.nextFloat() * 2.0f + random.nextFloat();
        if (random.nextInt(10) == 0) {
            f *= random.nextFloat() * random.nextFloat() * 3.0f + 1.0f;
        }
        return f;
    }

    protected double getTunnelSystemHeightWidthRatio() {
        return 1.0;
    }

    protected void carveCave(CarverContext context, CaveCarverConfig config, Chunk chunk, Function<BlockPos, RegistryEntry<Biome>> posToBiome, AquiferSampler aquiferSampler, double d, double e, double f, float g, double h, CarvingMask mask, Carver.SkipPredicate skipPredicate) {
        double i = 1.5 + (double)(MathHelper.sin(1.5707964f) * g);
        double j = i * h;
        this.carveRegion(context, config, chunk, posToBiome, aquiferSampler, d + 1.0, e, f, i, j, mask, skipPredicate);
    }

    protected void carveTunnels(CarverContext context, CaveCarverConfig config, Chunk chunk, Function<BlockPos, RegistryEntry<Biome>> posToBiome, long seed, AquiferSampler aquiferSampler, double x, double y, double z, double horizontalScale, double verticalScale, float width, float yaw, float pitch, int branchStartIndex, int branchCount, double yawPitchRatio, CarvingMask mask, Carver.SkipPredicate skipPredicate) {
        Random lv = Random.create(seed);
        int p = lv.nextInt(branchCount / 2) + branchCount / 4;
        boolean bl = lv.nextInt(6) == 0;
        float q = 0.0f;
        float r = 0.0f;
        for (int s = branchStartIndex; s < branchCount; ++s) {
            double t = 1.5 + (double)(MathHelper.sin((float)Math.PI * (float)s / (float)branchCount) * width);
            double u = t * yawPitchRatio;
            float v = MathHelper.cos(pitch);
            x += (double)(MathHelper.cos(yaw) * v);
            y += (double)MathHelper.sin(pitch);
            z += (double)(MathHelper.sin(yaw) * v);
            pitch *= bl ? 0.92f : 0.7f;
            pitch += r * 0.1f;
            yaw += q * 0.1f;
            r *= 0.9f;
            q *= 0.75f;
            r += (lv.nextFloat() - lv.nextFloat()) * lv.nextFloat() * 2.0f;
            q += (lv.nextFloat() - lv.nextFloat()) * lv.nextFloat() * 4.0f;
            if (s == p && width > 1.0f) {
                this.carveTunnels(context, config, chunk, posToBiome, lv.nextLong(), aquiferSampler, x, y, z, horizontalScale, verticalScale, lv.nextFloat() * 0.5f + 0.5f, yaw - 1.5707964f, pitch / 3.0f, s, branchCount, 1.0, mask, skipPredicate);
                this.carveTunnels(context, config, chunk, posToBiome, lv.nextLong(), aquiferSampler, x, y, z, horizontalScale, verticalScale, lv.nextFloat() * 0.5f + 0.5f, yaw + 1.5707964f, pitch / 3.0f, s, branchCount, 1.0, mask, skipPredicate);
                return;
            }
            if (lv.nextInt(4) == 0) continue;
            if (!CaveCarver.canCarveBranch(chunk.getPos(), x, z, s, branchCount, width)) {
                return;
            }
            this.carveRegion(context, config, chunk, posToBiome, aquiferSampler, x, y, z, t * horizontalScale, u * verticalScale, mask, skipPredicate);
        }
    }

    private static boolean isPositionExcluded(double scaledRelativeX, double scaledRelativeY, double scaledRelativeZ, double floorY) {
        if (scaledRelativeY <= floorY) {
            return true;
        }
        return scaledRelativeX * scaledRelativeX + scaledRelativeY * scaledRelativeY + scaledRelativeZ * scaledRelativeZ >= 1.0;
    }
}

