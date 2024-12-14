/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 * 
 * Could not load the following classes:
 *  org.jetbrains.annotations.Nullable
 */
package net.minecraft.world.gen.carver;

import com.google.common.collect.ImmutableSet;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import java.util.Set;
import java.util.function.Function;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.fluid.Fluid;
import net.minecraft.fluid.FluidState;
import net.minecraft.fluid.Fluids;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.state.property.Properties;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3i;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.gen.carver.CarverConfig;
import net.minecraft.world.gen.carver.CarverContext;
import net.minecraft.world.gen.carver.CarvingMask;
import net.minecraft.world.gen.carver.CaveCarver;
import net.minecraft.world.gen.carver.CaveCarverConfig;
import net.minecraft.world.gen.carver.ConfiguredCarver;
import net.minecraft.world.gen.carver.NetherCaveCarver;
import net.minecraft.world.gen.carver.RavineCarver;
import net.minecraft.world.gen.carver.RavineCarverConfig;
import net.minecraft.world.gen.chunk.AquiferSampler;
import net.minecraft.world.gen.densityfunction.DensityFunction;
import org.apache.commons.lang3.mutable.MutableBoolean;
import org.jetbrains.annotations.Nullable;

public abstract class Carver<C extends CarverConfig> {
    public static final Carver<CaveCarverConfig> CAVE = Carver.register("cave", new CaveCarver(CaveCarverConfig.CAVE_CODEC));
    public static final Carver<CaveCarverConfig> NETHER_CAVE = Carver.register("nether_cave", new NetherCaveCarver(CaveCarverConfig.CAVE_CODEC));
    public static final Carver<RavineCarverConfig> RAVINE = Carver.register("canyon", new RavineCarver(RavineCarverConfig.RAVINE_CODEC));
    protected static final BlockState AIR = Blocks.AIR.getDefaultState();
    protected static final BlockState CAVE_AIR = Blocks.CAVE_AIR.getDefaultState();
    protected static final FluidState WATER = Fluids.WATER.getDefaultState();
    protected static final FluidState LAVA = Fluids.LAVA.getDefaultState();
    protected Set<Fluid> carvableFluids = ImmutableSet.of(Fluids.WATER);
    private final MapCodec<ConfiguredCarver<C>> codec;

    private static <C extends CarverConfig, F extends Carver<C>> F register(String name, F carver) {
        return (F)Registry.register(Registries.CARVER, name, carver);
    }

    public Carver(Codec<C> configCodec) {
        this.codec = ((MapCodec)configCodec.fieldOf("config")).xmap(this::configure, ConfiguredCarver::config);
    }

    public ConfiguredCarver<C> configure(C config) {
        return new ConfiguredCarver<C>(this, config);
    }

    public MapCodec<ConfiguredCarver<C>> getCodec() {
        return this.codec;
    }

    public int getBranchFactor() {
        return 4;
    }

    protected boolean carveRegion(CarverContext context, C config, Chunk chunk, Function<BlockPos, RegistryEntry<Biome>> posToBiome, AquiferSampler aquiferSampler, double x, double y, double z, double width, double height, CarvingMask mask, SkipPredicate skipPredicate) {
        ChunkPos lv = chunk.getPos();
        double i = lv.getCenterX();
        double j = lv.getCenterZ();
        double k = 16.0 + width * 2.0;
        if (Math.abs(x - i) > k || Math.abs(z - j) > k) {
            return false;
        }
        int l = lv.getStartX();
        int m = lv.getStartZ();
        int n = Math.max(MathHelper.floor(x - width) - l - 1, 0);
        int o = Math.min(MathHelper.floor(x + width) - l, 15);
        int p = Math.max(MathHelper.floor(y - height) - 1, context.getMinY() + 1);
        int q = chunk.hasBelowZeroRetrogen() ? 0 : 7;
        int r = Math.min(MathHelper.floor(y + height) + 1, context.getMinY() + context.getHeight() - 1 - q);
        int s = Math.max(MathHelper.floor(z - width) - m - 1, 0);
        int t = Math.min(MathHelper.floor(z + width) - m, 15);
        boolean bl = false;
        BlockPos.Mutable lv2 = new BlockPos.Mutable();
        BlockPos.Mutable lv3 = new BlockPos.Mutable();
        for (int u = n; u <= o; ++u) {
            int v = lv.getOffsetX(u);
            double w = ((double)v + 0.5 - x) / width;
            for (int x2 = s; x2 <= t; ++x2) {
                int y2 = lv.getOffsetZ(x2);
                double z2 = ((double)y2 + 0.5 - z) / width;
                if (w * w + z2 * z2 >= 1.0) continue;
                MutableBoolean mutableBoolean = new MutableBoolean(false);
                for (int aa = r; aa > p; --aa) {
                    double ab = ((double)aa - 0.5 - y) / height;
                    if (skipPredicate.shouldSkip(context, w, ab, z2, aa) || mask.get(u, aa, x2) && !Carver.isDebug(config)) continue;
                    mask.set(u, aa, x2);
                    lv2.set(v, aa, y2);
                    bl |= this.carveAtPoint(context, config, chunk, posToBiome, mask, lv2, lv3, aquiferSampler, mutableBoolean);
                }
            }
        }
        return bl;
    }

    protected boolean carveAtPoint(CarverContext context, C config, Chunk chunk, Function<BlockPos, RegistryEntry<Biome>> posToBiome, CarvingMask mask, BlockPos.Mutable pos, BlockPos.Mutable tmp, AquiferSampler aquiferSampler, MutableBoolean replacedGrassy) {
        BlockState lv = chunk.getBlockState(pos);
        if (lv.isOf(Blocks.GRASS_BLOCK) || lv.isOf(Blocks.MYCELIUM)) {
            replacedGrassy.setTrue();
        }
        if (!this.canAlwaysCarveBlock(config, lv) && !Carver.isDebug(config)) {
            return false;
        }
        BlockState lv2 = this.getState(context, config, pos, aquiferSampler);
        if (lv2 == null) {
            return false;
        }
        chunk.setBlockState(pos, lv2, false);
        if (aquiferSampler.needsFluidTick() && !lv2.getFluidState().isEmpty()) {
            chunk.markBlockForPostProcessing(pos);
        }
        if (replacedGrassy.isTrue()) {
            tmp.set((Vec3i)pos, Direction.DOWN);
            if (chunk.getBlockState(tmp).isOf(Blocks.DIRT)) {
                context.applyMaterialRule(posToBiome, chunk, tmp, !lv2.getFluidState().isEmpty()).ifPresent(state -> {
                    chunk.setBlockState(tmp, (BlockState)state, false);
                    if (!state.getFluidState().isEmpty()) {
                        chunk.markBlockForPostProcessing(tmp);
                    }
                });
            }
        }
        return true;
    }

    @Nullable
    private BlockState getState(CarverContext context, C config, BlockPos pos, AquiferSampler sampler) {
        if (pos.getY() <= ((CarverConfig)config).lavaLevel.getY(context)) {
            return LAVA.getBlockState();
        }
        BlockState lv = sampler.apply(new DensityFunction.UnblendedNoisePos(pos.getX(), pos.getY(), pos.getZ()), 0.0);
        if (lv == null) {
            return Carver.isDebug(config) ? ((CarverConfig)config).debugConfig.getBarrierState() : null;
        }
        return Carver.isDebug(config) ? Carver.getDebugState(config, lv) : lv;
    }

    private static BlockState getDebugState(CarverConfig config, BlockState state) {
        if (state.isOf(Blocks.AIR)) {
            return config.debugConfig.getAirState();
        }
        if (state.isOf(Blocks.WATER)) {
            BlockState lv = config.debugConfig.getWaterState();
            if (lv.contains(Properties.WATERLOGGED)) {
                return (BlockState)lv.with(Properties.WATERLOGGED, true);
            }
            return lv;
        }
        if (state.isOf(Blocks.LAVA)) {
            return config.debugConfig.getLavaState();
        }
        return state;
    }

    public abstract boolean carve(CarverContext var1, C var2, Chunk var3, Function<BlockPos, RegistryEntry<Biome>> var4, Random var5, AquiferSampler var6, ChunkPos var7, CarvingMask var8);

    public abstract boolean shouldCarve(C var1, Random var2);

    protected boolean canAlwaysCarveBlock(C config, BlockState state) {
        return state.isIn(((CarverConfig)config).replaceable);
    }

    protected static boolean canCarveBranch(ChunkPos pos, double x, double z, int branchIndex, int branchCount, float baseWidth) {
        double n;
        double m;
        double h;
        double l;
        double g = pos.getCenterX();
        double k = x - g;
        return k * k + (l = z - (h = (double)pos.getCenterZ())) * l - (m = (double)(branchCount - branchIndex)) * m <= (n = (double)(baseWidth + 2.0f + 16.0f)) * n;
    }

    private static boolean isDebug(CarverConfig config) {
        return config.debugConfig.isDebugMode();
    }

    public static interface SkipPredicate {
        public boolean shouldSkip(CarverContext var1, double var2, double var4, double var6, int var8);
    }
}

