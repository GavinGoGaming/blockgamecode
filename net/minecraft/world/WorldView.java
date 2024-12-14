/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 * 
 * Could not load the following classes:
 *  org.jetbrains.annotations.Nullable
 */
package net.minecraft.world;

import java.util.stream.Stream;
import net.minecraft.block.BlockState;
import net.minecraft.registry.DynamicRegistryManager;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.registry.tag.FluidTags;
import net.minecraft.resource.featuretoggle.FeatureSet;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.ChunkSectionPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.BlockRenderView;
import net.minecraft.world.BlockView;
import net.minecraft.world.CollisionView;
import net.minecraft.world.Heightmap;
import net.minecraft.world.RedstoneView;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.biome.ColorResolver;
import net.minecraft.world.biome.source.BiomeAccess;
import net.minecraft.world.biome.source.BiomeCoords;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.ChunkStatus;
import net.minecraft.world.dimension.DimensionType;
import org.jetbrains.annotations.Nullable;

public interface WorldView
extends BlockRenderView,
CollisionView,
RedstoneView,
BiomeAccess.Storage {
    @Nullable
    public Chunk getChunk(int var1, int var2, ChunkStatus var3, boolean var4);

    @Deprecated
    public boolean isChunkLoaded(int var1, int var2);

    public int getTopY(Heightmap.Type var1, int var2, int var3);

    public int getAmbientDarkness();

    public BiomeAccess getBiomeAccess();

    default public RegistryEntry<Biome> getBiome(BlockPos pos) {
        return this.getBiomeAccess().getBiome(pos);
    }

    default public Stream<BlockState> getStatesInBoxIfLoaded(Box box) {
        int n;
        int i = MathHelper.floor(box.minX);
        int j = MathHelper.floor(box.maxX);
        int k = MathHelper.floor(box.minY);
        int l = MathHelper.floor(box.maxY);
        int m = MathHelper.floor(box.minZ);
        if (this.isRegionLoaded(i, k, m, j, l, n = MathHelper.floor(box.maxZ))) {
            return this.getStatesInBox(box);
        }
        return Stream.empty();
    }

    @Override
    default public int getColor(BlockPos pos, ColorResolver colorResolver) {
        return colorResolver.getColor(this.getBiome(pos).value(), pos.getX(), pos.getZ());
    }

    @Override
    default public RegistryEntry<Biome> getBiomeForNoiseGen(int biomeX, int biomeY, int biomeZ) {
        Chunk lv = this.getChunk(BiomeCoords.toChunk(biomeX), BiomeCoords.toChunk(biomeZ), ChunkStatus.BIOMES, false);
        if (lv != null) {
            return lv.getBiomeForNoiseGen(biomeX, biomeY, biomeZ);
        }
        return this.getGeneratorStoredBiome(biomeX, biomeY, biomeZ);
    }

    public RegistryEntry<Biome> getGeneratorStoredBiome(int var1, int var2, int var3);

    public boolean isClient();

    @Deprecated
    public int getSeaLevel();

    public DimensionType getDimension();

    @Override
    default public int getBottomY() {
        return this.getDimension().minY();
    }

    @Override
    default public int getHeight() {
        return this.getDimension().height();
    }

    default public BlockPos getTopPosition(Heightmap.Type heightmap, BlockPos pos) {
        return new BlockPos(pos.getX(), this.getTopY(heightmap, pos.getX(), pos.getZ()), pos.getZ());
    }

    default public boolean isAir(BlockPos pos) {
        return this.getBlockState(pos).isAir();
    }

    default public boolean isSkyVisibleAllowingSea(BlockPos pos) {
        if (pos.getY() >= this.getSeaLevel()) {
            return this.isSkyVisible(pos);
        }
        BlockPos lv = new BlockPos(pos.getX(), this.getSeaLevel(), pos.getZ());
        if (!this.isSkyVisible(lv)) {
            return false;
        }
        lv = lv.down();
        while (lv.getY() > pos.getY()) {
            BlockState lv2 = this.getBlockState(lv);
            if (lv2.getOpacity(this, lv) > 0 && !lv2.isLiquid()) {
                return false;
            }
            lv = lv.down();
        }
        return true;
    }

    default public float getPhototaxisFavor(BlockPos pos) {
        return this.getBrightness(pos) - 0.5f;
    }

    @Deprecated
    default public float getBrightness(BlockPos pos) {
        float f = (float)this.getLightLevel(pos) / 15.0f;
        float g = f / (4.0f - 3.0f * f);
        return MathHelper.lerp(this.getDimension().ambientLight(), g, 1.0f);
    }

    default public Chunk getChunk(BlockPos pos) {
        return this.getChunk(ChunkSectionPos.getSectionCoord(pos.getX()), ChunkSectionPos.getSectionCoord(pos.getZ()));
    }

    default public Chunk getChunk(int chunkX, int chunkZ) {
        return this.getChunk(chunkX, chunkZ, ChunkStatus.FULL, true);
    }

    default public Chunk getChunk(int chunkX, int chunkZ, ChunkStatus status) {
        return this.getChunk(chunkX, chunkZ, status, true);
    }

    @Override
    @Nullable
    default public BlockView getChunkAsView(int chunkX, int chunkZ) {
        return this.getChunk(chunkX, chunkZ, ChunkStatus.EMPTY, false);
    }

    default public boolean isWater(BlockPos pos) {
        return this.getFluidState(pos).isIn(FluidTags.WATER);
    }

    default public boolean containsFluid(Box box) {
        int i = MathHelper.floor(box.minX);
        int j = MathHelper.ceil(box.maxX);
        int k = MathHelper.floor(box.minY);
        int l = MathHelper.ceil(box.maxY);
        int m = MathHelper.floor(box.minZ);
        int n = MathHelper.ceil(box.maxZ);
        BlockPos.Mutable lv = new BlockPos.Mutable();
        for (int o = i; o < j; ++o) {
            for (int p = k; p < l; ++p) {
                for (int q = m; q < n; ++q) {
                    BlockState lv2 = this.getBlockState(lv.set(o, p, q));
                    if (lv2.getFluidState().isEmpty()) continue;
                    return true;
                }
            }
        }
        return false;
    }

    default public int getLightLevel(BlockPos pos) {
        return this.getLightLevel(pos, this.getAmbientDarkness());
    }

    default public int getLightLevel(BlockPos pos, int ambientDarkness) {
        if (pos.getX() < -30000000 || pos.getZ() < -30000000 || pos.getX() >= 30000000 || pos.getZ() >= 30000000) {
            return 15;
        }
        return this.getBaseLightLevel(pos, ambientDarkness);
    }

    @Deprecated
    default public boolean isPosLoaded(int x, int z) {
        return this.isChunkLoaded(ChunkSectionPos.getSectionCoord(x), ChunkSectionPos.getSectionCoord(z));
    }

    @Deprecated
    default public boolean isChunkLoaded(BlockPos pos) {
        return this.isPosLoaded(pos.getX(), pos.getZ());
    }

    @Deprecated
    default public boolean isRegionLoaded(BlockPos min, BlockPos max) {
        return this.isRegionLoaded(min.getX(), min.getY(), min.getZ(), max.getX(), max.getY(), max.getZ());
    }

    @Deprecated
    default public boolean isRegionLoaded(int minX, int minY, int minZ, int maxX, int maxY, int maxZ) {
        if (maxY < this.getBottomY() || minY >= this.getTopY()) {
            return false;
        }
        return this.isRegionLoaded(minX, minZ, maxX, maxZ);
    }

    @Deprecated
    default public boolean isRegionLoaded(int minX, int minZ, int maxX, int maxZ) {
        int m = ChunkSectionPos.getSectionCoord(minX);
        int n = ChunkSectionPos.getSectionCoord(maxX);
        int o = ChunkSectionPos.getSectionCoord(minZ);
        int p = ChunkSectionPos.getSectionCoord(maxZ);
        for (int q = m; q <= n; ++q) {
            for (int r = o; r <= p; ++r) {
                if (this.isChunkLoaded(q, r)) continue;
                return false;
            }
        }
        return true;
    }

    public DynamicRegistryManager getRegistryManager();

    public FeatureSet getEnabledFeatures();

    default public <T> RegistryWrapper<T> createCommandRegistryWrapper(RegistryKey<? extends Registry<? extends T>> registryRef) {
        Registry lv = this.getRegistryManager().get(registryRef);
        return lv.getReadOnlyWrapper().withFeatureFilter(this.getEnabledFeatures());
    }
}

