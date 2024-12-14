/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 * 
 * Could not load the following classes:
 *  org.jetbrains.annotations.Nullable
 */
package net.minecraft.world.biome.source;

import com.google.common.base.Suppliers;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Sets;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import net.minecraft.SharedConstants;
import net.minecraft.registry.Registries;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.WorldView;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.biome.source.BiomeCoords;
import net.minecraft.world.biome.source.BiomeSupplier;
import net.minecraft.world.biome.source.util.MultiNoiseUtil;
import org.jetbrains.annotations.Nullable;

public abstract class BiomeSource
implements BiomeSupplier {
    public static final Codec<BiomeSource> CODEC = Registries.BIOME_SOURCE.getCodec().dispatchStable(BiomeSource::getCodec, Function.identity());
    private final Supplier<Set<RegistryEntry<Biome>>> biomes = Suppliers.memoize(() -> this.biomeStream().distinct().collect(ImmutableSet.toImmutableSet()));

    protected BiomeSource() {
    }

    protected abstract MapCodec<? extends BiomeSource> getCodec();

    protected abstract Stream<RegistryEntry<Biome>> biomeStream();

    public Set<RegistryEntry<Biome>> getBiomes() {
        return this.biomes.get();
    }

    public Set<RegistryEntry<Biome>> getBiomesInArea(int x, int y, int z, int radius, MultiNoiseUtil.MultiNoiseSampler sampler) {
        int m = BiomeCoords.fromBlock(x - radius);
        int n = BiomeCoords.fromBlock(y - radius);
        int o = BiomeCoords.fromBlock(z - radius);
        int p = BiomeCoords.fromBlock(x + radius);
        int q = BiomeCoords.fromBlock(y + radius);
        int r = BiomeCoords.fromBlock(z + radius);
        int s = p - m + 1;
        int t = q - n + 1;
        int u = r - o + 1;
        HashSet<RegistryEntry<Biome>> set = Sets.newHashSet();
        for (int v = 0; v < u; ++v) {
            for (int w = 0; w < s; ++w) {
                for (int x2 = 0; x2 < t; ++x2) {
                    int y2 = m + w;
                    int z2 = n + x2;
                    int aa = o + v;
                    set.add(this.getBiome(y2, z2, aa, sampler));
                }
            }
        }
        return set;
    }

    @Nullable
    public Pair<BlockPos, RegistryEntry<Biome>> locateBiome(int x, int y, int z, int radius, Predicate<RegistryEntry<Biome>> predicate, Random random, MultiNoiseUtil.MultiNoiseSampler noiseSampler) {
        return this.locateBiome(x, y, z, radius, 1, predicate, random, false, noiseSampler);
    }

    @Nullable
    public Pair<BlockPos, RegistryEntry<Biome>> locateBiome(BlockPos origin, int radius, int horizontalBlockCheckInterval, int verticalBlockCheckInterval, Predicate<RegistryEntry<Biome>> predicate, MultiNoiseUtil.MultiNoiseSampler noiseSampler, WorldView world) {
        Set set = this.getBiomes().stream().filter(predicate).collect(Collectors.toUnmodifiableSet());
        if (set.isEmpty()) {
            return null;
        }
        int l = Math.floorDiv(radius, horizontalBlockCheckInterval);
        int[] is = MathHelper.stream(origin.getY(), world.getBottomY() + 1, world.getTopY(), verticalBlockCheckInterval).toArray();
        for (BlockPos.Mutable lv : BlockPos.iterateInSquare(BlockPos.ORIGIN, l, Direction.EAST, Direction.SOUTH)) {
            int m = origin.getX() + lv.getX() * horizontalBlockCheckInterval;
            int n = origin.getZ() + lv.getZ() * horizontalBlockCheckInterval;
            int o = BiomeCoords.fromBlock(m);
            int p = BiomeCoords.fromBlock(n);
            for (int q : is) {
                int r = BiomeCoords.fromBlock(q);
                RegistryEntry<Biome> lv2 = this.getBiome(o, r, p, noiseSampler);
                if (!set.contains(lv2)) continue;
                return Pair.of(new BlockPos(m, q, n), lv2);
            }
        }
        return null;
    }

    @Nullable
    public Pair<BlockPos, RegistryEntry<Biome>> locateBiome(int x, int y, int z, int radius, int blockCheckInterval, Predicate<RegistryEntry<Biome>> predicate, Random random, boolean bl, MultiNoiseUtil.MultiNoiseSampler noiseSampler) {
        int s;
        int n = BiomeCoords.fromBlock(x);
        int o = BiomeCoords.fromBlock(z);
        int p = BiomeCoords.fromBlock(radius);
        int q = BiomeCoords.fromBlock(y);
        Pair<BlockPos, RegistryEntry<Biome>> pair = null;
        int r = 0;
        for (int t = s = bl ? 0 : p; t <= p; t += blockCheckInterval) {
            int u;
            int n2 = u = SharedConstants.DEBUG_BIOME_SOURCE ? 0 : -t;
            while (u <= t) {
                boolean bl2 = Math.abs(u) == t;
                for (int v = -t; v <= t; v += blockCheckInterval) {
                    int x2;
                    int w;
                    RegistryEntry<Biome> lv;
                    if (bl) {
                        boolean bl3;
                        boolean bl4 = bl3 = Math.abs(v) == t;
                        if (!bl3 && !bl2) continue;
                    }
                    if (!predicate.test(lv = this.getBiome(w = n + v, q, x2 = o + u, noiseSampler))) continue;
                    if (pair == null || random.nextInt(r + 1) == 0) {
                        BlockPos lv2 = new BlockPos(BiomeCoords.toBlock(w), y, BiomeCoords.toBlock(x2));
                        if (bl) {
                            return Pair.of(lv2, lv);
                        }
                        pair = Pair.of(lv2, lv);
                    }
                    ++r;
                }
                u += blockCheckInterval;
            }
        }
        return pair;
    }

    @Override
    public abstract RegistryEntry<Biome> getBiome(int var1, int var2, int var3, MultiNoiseUtil.MultiNoiseSampler var4);

    public void addDebugInfo(List<String> info, BlockPos pos, MultiNoiseUtil.MultiNoiseSampler noiseSampler) {
    }
}

