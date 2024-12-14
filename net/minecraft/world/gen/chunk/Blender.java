/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 * 
 * Could not load the following classes:
 *  org.jetbrains.annotations.Nullable
 */
package net.minecraft.world.gen.chunk;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import java.util.ArrayList;
import java.util.Map;
import java.util.stream.Stream;
import net.minecraft.block.BlockState;
import net.minecraft.fluid.FluidState;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.registry.tag.BlockTags;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.EightWayDirection;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.noise.DoublePerlinNoiseSampler;
import net.minecraft.util.math.random.Xoroshiro128PlusPlusRandom;
import net.minecraft.world.ChunkRegion;
import net.minecraft.world.Heightmap;
import net.minecraft.world.StructureWorldAccess;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.biome.source.BiomeCoords;
import net.minecraft.world.biome.source.BiomeSupplier;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.ProtoChunk;
import net.minecraft.world.gen.GenerationStep;
import net.minecraft.world.gen.carver.CarvingMask;
import net.minecraft.world.gen.chunk.BlendingData;
import net.minecraft.world.gen.densityfunction.DensityFunction;
import net.minecraft.world.gen.noise.BuiltinNoiseParameters;
import org.apache.commons.lang3.mutable.MutableDouble;
import org.apache.commons.lang3.mutable.MutableObject;
import org.jetbrains.annotations.Nullable;

public class Blender {
    private static final Blender NO_BLENDING = new Blender(new Long2ObjectOpenHashMap(), new Long2ObjectOpenHashMap()){

        @Override
        public BlendResult calculate(int blockX, int blockZ) {
            return new BlendResult(1.0, 0.0);
        }

        @Override
        public double applyBlendDensity(DensityFunction.NoisePos pos, double density) {
            return density;
        }

        @Override
        public BiomeSupplier getBiomeSupplier(BiomeSupplier biomeSupplier) {
            return biomeSupplier;
        }
    };
    private static final DoublePerlinNoiseSampler OFFSET_NOISE = DoublePerlinNoiseSampler.create(new Xoroshiro128PlusPlusRandom(42L), BuiltinNoiseParameters.OFFSET);
    private static final int BLENDING_BIOME_DISTANCE_THRESHOLD = BiomeCoords.fromChunk(7) - 1;
    private static final int BLENDING_CHUNK_DISTANCE_THRESHOLD = BiomeCoords.toChunk(BLENDING_BIOME_DISTANCE_THRESHOLD + 3);
    private static final int field_35504 = 2;
    private static final int CLOSE_BLENDING_DISTANCE_THRESHOLD = BiomeCoords.toChunk(5);
    private static final double field_36224 = 8.0;
    private final Long2ObjectOpenHashMap<BlendingData> blendingData;
    private final Long2ObjectOpenHashMap<BlendingData> closeBlendingData;

    public static Blender getNoBlending() {
        return NO_BLENDING;
    }

    public static Blender getBlender(@Nullable ChunkRegion chunkRegion) {
        if (chunkRegion == null) {
            return NO_BLENDING;
        }
        ChunkPos lv = chunkRegion.getCenterPos();
        if (!chunkRegion.needsBlending(lv, BLENDING_CHUNK_DISTANCE_THRESHOLD)) {
            return NO_BLENDING;
        }
        Long2ObjectOpenHashMap<BlendingData> long2ObjectOpenHashMap = new Long2ObjectOpenHashMap<BlendingData>();
        Long2ObjectOpenHashMap<BlendingData> long2ObjectOpenHashMap2 = new Long2ObjectOpenHashMap<BlendingData>();
        int i = MathHelper.square(BLENDING_CHUNK_DISTANCE_THRESHOLD + 1);
        for (int j = -BLENDING_CHUNK_DISTANCE_THRESHOLD; j <= BLENDING_CHUNK_DISTANCE_THRESHOLD; ++j) {
            for (int k = -BLENDING_CHUNK_DISTANCE_THRESHOLD; k <= BLENDING_CHUNK_DISTANCE_THRESHOLD; ++k) {
                int m;
                int l;
                BlendingData lv2;
                if (j * j + k * k > i || (lv2 = BlendingData.getBlendingData(chunkRegion, l = lv.x + j, m = lv.z + k)) == null) continue;
                long2ObjectOpenHashMap.put(ChunkPos.toLong(l, m), lv2);
                if (j < -CLOSE_BLENDING_DISTANCE_THRESHOLD || j > CLOSE_BLENDING_DISTANCE_THRESHOLD || k < -CLOSE_BLENDING_DISTANCE_THRESHOLD || k > CLOSE_BLENDING_DISTANCE_THRESHOLD) continue;
                long2ObjectOpenHashMap2.put(ChunkPos.toLong(l, m), lv2);
            }
        }
        if (long2ObjectOpenHashMap.isEmpty() && long2ObjectOpenHashMap2.isEmpty()) {
            return NO_BLENDING;
        }
        return new Blender(long2ObjectOpenHashMap, long2ObjectOpenHashMap2);
    }

    Blender(Long2ObjectOpenHashMap<BlendingData> blendingData, Long2ObjectOpenHashMap<BlendingData> closeBlendingData) {
        this.blendingData = blendingData;
        this.closeBlendingData = closeBlendingData;
    }

    public BlendResult calculate(int blockX, int blockZ) {
        int l;
        int k = BiomeCoords.fromBlock(blockX);
        double d = this.sampleClosest(k, 0, l = BiomeCoords.fromBlock(blockZ), BlendingData::getHeight);
        if (d != Double.MAX_VALUE) {
            return new BlendResult(0.0, Blender.getBlendOffset(d));
        }
        MutableDouble mutableDouble = new MutableDouble(0.0);
        MutableDouble mutableDouble2 = new MutableDouble(0.0);
        MutableDouble mutableDouble3 = new MutableDouble(Double.POSITIVE_INFINITY);
        this.blendingData.forEach((chunkPos, data) -> data.acceptHeights(BiomeCoords.fromChunk(ChunkPos.getPackedX(chunkPos)), BiomeCoords.fromChunk(ChunkPos.getPackedZ(chunkPos)), (biomeX, biomeZ, height) -> {
            double e = MathHelper.hypot(k - biomeX, l - biomeZ);
            if (e > (double)BLENDING_BIOME_DISTANCE_THRESHOLD) {
                return;
            }
            if (e < mutableDouble3.doubleValue()) {
                mutableDouble3.setValue(e);
            }
            double f = 1.0 / (e * e * e * e);
            mutableDouble2.add(height * f);
            mutableDouble.add(f);
        }));
        if (mutableDouble3.doubleValue() == Double.POSITIVE_INFINITY) {
            return new BlendResult(1.0, 0.0);
        }
        double e = mutableDouble2.doubleValue() / mutableDouble.doubleValue();
        double f = MathHelper.clamp(mutableDouble3.doubleValue() / (double)(BLENDING_BIOME_DISTANCE_THRESHOLD + 1), 0.0, 1.0);
        f = 3.0 * f * f - 2.0 * f * f * f;
        return new BlendResult(f, Blender.getBlendOffset(e));
    }

    private static double getBlendOffset(double height) {
        double e = 1.0;
        double f = height + 0.5;
        double g = MathHelper.floorMod(f, 8.0);
        return 1.0 * (32.0 * (f - 128.0) - 3.0 * (f - 120.0) * g + 3.0 * g * g) / (128.0 * (32.0 - 3.0 * g));
    }

    public double applyBlendDensity(DensityFunction.NoisePos pos, double density) {
        int k;
        int j;
        int i = BiomeCoords.fromBlock(pos.blockX());
        double e = this.sampleClosest(i, j = pos.blockY() / 8, k = BiomeCoords.fromBlock(pos.blockZ()), BlendingData::getCollidableBlockDensity);
        if (e != Double.MAX_VALUE) {
            return e;
        }
        MutableDouble mutableDouble = new MutableDouble(0.0);
        MutableDouble mutableDouble2 = new MutableDouble(0.0);
        MutableDouble mutableDouble3 = new MutableDouble(Double.POSITIVE_INFINITY);
        this.closeBlendingData.forEach((chunkPos, data) -> data.acceptCollidableBlockDensities(BiomeCoords.fromChunk(ChunkPos.getPackedX(chunkPos)), BiomeCoords.fromChunk(ChunkPos.getPackedZ(chunkPos)), j - 1, j + 1, (biomeX, halfSectionY, biomeZ, collidableBlockDensity) -> {
            double e = MathHelper.magnitude((double)(i - biomeX), (double)((j - halfSectionY) * 2), (double)(k - biomeZ));
            if (e > 2.0) {
                return;
            }
            if (e < mutableDouble3.doubleValue()) {
                mutableDouble3.setValue(e);
            }
            double f = 1.0 / (e * e * e * e);
            mutableDouble2.add(collidableBlockDensity * f);
            mutableDouble.add(f);
        }));
        if (mutableDouble3.doubleValue() == Double.POSITIVE_INFINITY) {
            return density;
        }
        double f = mutableDouble2.doubleValue() / mutableDouble.doubleValue();
        double g = MathHelper.clamp(mutableDouble3.doubleValue() / 3.0, 0.0, 1.0);
        return MathHelper.lerp(g, f, density);
    }

    private double sampleClosest(int biomeX, int biomeY, int biomeZ, BlendingSampler sampler) {
        int l = BiomeCoords.toChunk(biomeX);
        int m = BiomeCoords.toChunk(biomeZ);
        boolean bl = (biomeX & 3) == 0;
        boolean bl2 = (biomeZ & 3) == 0;
        double d = this.sample(sampler, l, m, biomeX, biomeY, biomeZ);
        if (d == Double.MAX_VALUE) {
            if (bl && bl2) {
                d = this.sample(sampler, l - 1, m - 1, biomeX, biomeY, biomeZ);
            }
            if (d == Double.MAX_VALUE) {
                if (bl) {
                    d = this.sample(sampler, l - 1, m, biomeX, biomeY, biomeZ);
                }
                if (d == Double.MAX_VALUE && bl2) {
                    d = this.sample(sampler, l, m - 1, biomeX, biomeY, biomeZ);
                }
            }
        }
        return d;
    }

    private double sample(BlendingSampler sampler, int chunkX, int chunkZ, int biomeX, int biomeY, int biomeZ) {
        BlendingData lv = this.blendingData.get(ChunkPos.toLong(chunkX, chunkZ));
        if (lv != null) {
            return sampler.get(lv, biomeX - BiomeCoords.fromChunk(chunkX), biomeY, biomeZ - BiomeCoords.fromChunk(chunkZ));
        }
        return Double.MAX_VALUE;
    }

    public BiomeSupplier getBiomeSupplier(BiomeSupplier biomeSupplier) {
        return (x, y, z, noise) -> {
            RegistryEntry<Biome> lv = this.blendBiome(x, y, z);
            if (lv == null) {
                return biomeSupplier.getBiome(x, y, z, noise);
            }
            return lv;
        };
    }

    @Nullable
    private RegistryEntry<Biome> blendBiome(int x, int y, int z) {
        MutableDouble mutableDouble = new MutableDouble(Double.POSITIVE_INFINITY);
        MutableObject mutableObject = new MutableObject();
        this.blendingData.forEach((chunkPos, data) -> data.acceptBiomes(BiomeCoords.fromChunk(ChunkPos.getPackedX(chunkPos)), y, BiomeCoords.fromChunk(ChunkPos.getPackedZ(chunkPos)), (biomeX, biomeZ, biome) -> {
            double d = MathHelper.hypot(x - biomeX, z - biomeZ);
            if (d > (double)BLENDING_BIOME_DISTANCE_THRESHOLD) {
                return;
            }
            if (d < mutableDouble.doubleValue()) {
                mutableObject.setValue(biome);
                mutableDouble.setValue(d);
            }
        }));
        if (mutableDouble.doubleValue() == Double.POSITIVE_INFINITY) {
            return null;
        }
        double d = OFFSET_NOISE.sample(x, 0.0, z) * 12.0;
        double e = MathHelper.clamp((mutableDouble.doubleValue() + d) / (double)(BLENDING_BIOME_DISTANCE_THRESHOLD + 1), 0.0, 1.0);
        if (e > 0.5) {
            return null;
        }
        return (RegistryEntry)mutableObject.getValue();
    }

    public static void tickLeavesAndFluids(ChunkRegion chunkRegion, Chunk chunk) {
        ChunkPos lv = chunk.getPos();
        boolean bl = chunk.usesOldNoise();
        BlockPos.Mutable lv2 = new BlockPos.Mutable();
        BlockPos lv3 = new BlockPos(lv.getStartX(), 0, lv.getStartZ());
        BlendingData lv4 = chunk.getBlendingData();
        if (lv4 == null) {
            return;
        }
        int i = lv4.getOldHeightLimit().getBottomY();
        int j = lv4.getOldHeightLimit().getTopY() - 1;
        if (bl) {
            for (int k = 0; k < 16; ++k) {
                for (int l = 0; l < 16; ++l) {
                    Blender.tickLeavesAndFluids(chunk, lv2.set(lv3, k, i - 1, l));
                    Blender.tickLeavesAndFluids(chunk, lv2.set(lv3, k, i, l));
                    Blender.tickLeavesAndFluids(chunk, lv2.set(lv3, k, j, l));
                    Blender.tickLeavesAndFluids(chunk, lv2.set(lv3, k, j + 1, l));
                }
            }
        }
        for (Direction lv5 : Direction.Type.HORIZONTAL) {
            if (chunkRegion.getChunk(lv.x + lv5.getOffsetX(), lv.z + lv5.getOffsetZ()).usesOldNoise() == bl) continue;
            int m = lv5 == Direction.EAST ? 15 : 0;
            int n = lv5 == Direction.WEST ? 0 : 15;
            int o = lv5 == Direction.SOUTH ? 15 : 0;
            int p = lv5 == Direction.NORTH ? 0 : 15;
            for (int q = m; q <= n; ++q) {
                for (int r = o; r <= p; ++r) {
                    int s = Math.min(j, chunk.sampleHeightmap(Heightmap.Type.MOTION_BLOCKING, q, r)) + 1;
                    for (int t = i; t < s; ++t) {
                        Blender.tickLeavesAndFluids(chunk, lv2.set(lv3, q, t, r));
                    }
                }
            }
        }
    }

    private static void tickLeavesAndFluids(Chunk chunk, BlockPos pos) {
        FluidState lv2;
        BlockState lv = chunk.getBlockState(pos);
        if (lv.isIn(BlockTags.LEAVES)) {
            chunk.markBlockForPostProcessing(pos);
        }
        if (!(lv2 = chunk.getFluidState(pos)).isEmpty()) {
            chunk.markBlockForPostProcessing(pos);
        }
    }

    public static void createCarvingMasks(StructureWorldAccess world, ProtoChunk chunk) {
        ChunkPos lv = chunk.getPos();
        ImmutableMap.Builder<EightWayDirection, BlendingData> builder = ImmutableMap.builder();
        for (EightWayDirection lv2 : EightWayDirection.values()) {
            int j;
            int i = lv.x + lv2.getOffsetX();
            BlendingData lv3 = world.getChunk(i, j = lv.z + lv2.getOffsetZ()).getBlendingData();
            if (lv3 == null) continue;
            builder.put(lv2, lv3);
        }
        ImmutableMap<EightWayDirection, BlendingData> immutableMap = builder.build();
        if (!chunk.usesOldNoise() && immutableMap.isEmpty()) {
            return;
        }
        DistanceFunction lv4 = Blender.createClosestDistanceFunction(chunk.getBlendingData(), immutableMap);
        CarvingMask.MaskPredicate lv5 = (offsetX, y, offsetZ) -> {
            double f;
            double e;
            double d = (double)offsetX + 0.5 + OFFSET_NOISE.sample(offsetX, y, offsetZ) * 4.0;
            return lv4.getDistance(d, e = (double)y + 0.5 + OFFSET_NOISE.sample(y, offsetZ, offsetX) * 4.0, f = (double)offsetZ + 0.5 + OFFSET_NOISE.sample(offsetZ, offsetX, y) * 4.0) < 4.0;
        };
        Stream.of(GenerationStep.Carver.values()).map(chunk::getOrCreateCarvingMask).forEach(mask -> mask.setMaskPredicate(lv5));
    }

    public static DistanceFunction createClosestDistanceFunction(@Nullable BlendingData data, Map<EightWayDirection, BlendingData> neighborData) {
        ArrayList<DistanceFunction> list = Lists.newArrayList();
        if (data != null) {
            list.add(Blender.createDistanceFunction(null, data));
        }
        neighborData.forEach((direction, datax) -> list.add(Blender.createDistanceFunction(direction, datax)));
        return (offsetX, y, offsetZ) -> {
            double g = Double.POSITIVE_INFINITY;
            for (DistanceFunction lv : list) {
                double h = lv.getDistance(offsetX, y, offsetZ);
                if (!(h < g)) continue;
                g = h;
            }
            return g;
        };
    }

    private static DistanceFunction createDistanceFunction(@Nullable EightWayDirection direction, BlendingData data) {
        double d = 0.0;
        double e = 0.0;
        if (direction != null) {
            for (Direction lv : direction.getDirections()) {
                d += (double)(lv.getOffsetX() * 16);
                e += (double)(lv.getOffsetZ() * 16);
            }
        }
        double f = d;
        double g = e;
        double h = (double)data.getOldHeightLimit().getHeight() / 2.0;
        double i = (double)data.getOldHeightLimit().getBottomY() + h;
        return (offsetX, y, offsetZ) -> Blender.getDistance(offsetX - 8.0 - f, y - i, offsetZ - 8.0 - g, 8.0, h, 8.0);
    }

    private static double getDistance(double x1, double y1, double z1, double x2, double y2, double z2) {
        double j = Math.abs(x1) - x2;
        double k = Math.abs(y1) - y2;
        double l = Math.abs(z1) - z2;
        return MathHelper.magnitude(Math.max(0.0, j), Math.max(0.0, k), Math.max(0.0, l));
    }

    static interface BlendingSampler {
        public double get(BlendingData var1, int var2, int var3, int var4);
    }

    public record BlendResult(double alpha, double blendingOffset) {
    }

    public static interface DistanceFunction {
        public double getDistance(double var1, double var3, double var5);
    }
}

