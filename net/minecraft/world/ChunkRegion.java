/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 * 
 * Could not load the following classes:
 *  org.jetbrains.annotations.Nullable
 */
package net.minecraft.world;

import com.mojang.logging.LogUtils;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Predicate;
import java.util.function.Supplier;
import net.minecraft.block.Block;
import net.minecraft.block.BlockEntityProvider;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.fluid.Fluid;
import net.minecraft.fluid.FluidState;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.particle.ParticleEffect;
import net.minecraft.registry.DynamicRegistryManager;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.resource.featuretoggle.FeatureSet;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvent;
import net.minecraft.util.Identifier;
import net.minecraft.util.TypeFilter;
import net.minecraft.util.Util;
import net.minecraft.util.collection.BoundedRegionArray;
import net.minecraft.util.crash.CrashException;
import net.minecraft.util.crash.CrashReport;
import net.minecraft.util.crash.CrashReportSection;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.ChunkSectionPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.HeightLimitView;
import net.minecraft.world.Heightmap;
import net.minecraft.world.LocalDifficulty;
import net.minecraft.world.StructureWorldAccess;
import net.minecraft.world.WorldProperties;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.biome.source.BiomeAccess;
import net.minecraft.world.border.WorldBorder;
import net.minecraft.world.chunk.AbstractChunkHolder;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.ChunkGenerationStep;
import net.minecraft.world.chunk.ChunkManager;
import net.minecraft.world.chunk.ChunkStatus;
import net.minecraft.world.chunk.ChunkType;
import net.minecraft.world.chunk.light.LightingProvider;
import net.minecraft.world.dimension.DimensionType;
import net.minecraft.world.event.GameEvent;
import net.minecraft.world.tick.MultiTickScheduler;
import net.minecraft.world.tick.QueryableTickScheduler;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;

public class ChunkRegion
implements StructureWorldAccess {
    private static final Logger LOGGER = LogUtils.getLogger();
    private final BoundedRegionArray<AbstractChunkHolder> chunks;
    private final Chunk centerPos;
    private final ServerWorld world;
    private final long seed;
    private final WorldProperties levelProperties;
    private final Random random;
    private final DimensionType dimension;
    private final MultiTickScheduler<Block> blockTickScheduler = new MultiTickScheduler(pos -> this.getChunk((BlockPos)pos).getBlockTickScheduler());
    private final MultiTickScheduler<Fluid> fluidTickScheduler = new MultiTickScheduler(pos -> this.getChunk((BlockPos)pos).getFluidTickScheduler());
    private final BiomeAccess biomeAccess;
    private final ChunkGenerationStep generationStep;
    @Nullable
    private Supplier<String> currentlyGeneratingStructureName;
    private final AtomicLong tickOrder = new AtomicLong();
    private static final Identifier WORLDGEN_REGION_RANDOM_ID = Identifier.ofVanilla("worldgen_region_random");

    public ChunkRegion(ServerWorld world, BoundedRegionArray<AbstractChunkHolder> chunks, ChunkGenerationStep generationStep, Chunk centerPos) {
        this.generationStep = generationStep;
        this.chunks = chunks;
        this.centerPos = centerPos;
        this.world = world;
        this.seed = world.getSeed();
        this.levelProperties = world.getLevelProperties();
        this.random = world.getChunkManager().getNoiseConfig().getOrCreateRandomDeriver(WORLDGEN_REGION_RANDOM_ID).split(this.centerPos.getPos().getStartPos());
        this.dimension = world.getDimension();
        this.biomeAccess = new BiomeAccess(this, BiomeAccess.hashSeed(this.seed));
    }

    public boolean needsBlending(ChunkPos chunkPos, int checkRadius) {
        return this.world.getChunkManager().chunkLoadingManager.needsBlending(chunkPos, checkRadius);
    }

    public ChunkPos getCenterPos() {
        return this.centerPos.getPos();
    }

    @Override
    public void setCurrentlyGeneratingStructureName(@Nullable Supplier<String> structureName) {
        this.currentlyGeneratingStructureName = structureName;
    }

    @Override
    public Chunk getChunk(int chunkX, int chunkZ) {
        return this.getChunk(chunkX, chunkZ, ChunkStatus.EMPTY);
    }

    @Override
    @Nullable
    public Chunk getChunk(int chunkX, int chunkZ, ChunkStatus leastStatus, boolean create) {
        AbstractChunkHolder lv2;
        ChunkStatus lv;
        int k = this.centerPos.getPos().getChebyshevDistance(chunkX, chunkZ);
        ChunkStatus chunkStatus = lv = k >= this.generationStep.directDependencies().size() ? null : this.generationStep.directDependencies().get(k);
        if (lv != null) {
            Chunk lv3;
            lv2 = this.chunks.get(chunkX, chunkZ);
            if (leastStatus.isAtMost(lv) && (lv3 = lv2.getUncheckedOrNull(lv)) != null) {
                return lv3;
            }
        } else {
            lv2 = null;
        }
        CrashReport lv4 = CrashReport.create(new IllegalStateException("Requested chunk unavailable during world generation"), "Exception generating new chunk");
        CrashReportSection lv5 = lv4.addElement("Chunk request details");
        lv5.add("Requested chunk", String.format(Locale.ROOT, "%d, %d", chunkX, chunkZ));
        lv5.add("Generating status", () -> this.generationStep.targetStatus().getId());
        lv5.add("Requested status", leastStatus::getId);
        lv5.add("Actual status", () -> lv2 == null ? "[out of cache bounds]" : lv2.getActualStatus().getId());
        lv5.add("Maximum allowed status", () -> lv == null ? "null" : lv.getId());
        lv5.add("Dependencies", this.generationStep.directDependencies()::toString);
        lv5.add("Requested distance", k);
        lv5.add("Generating chunk", this.centerPos.getPos()::toString);
        throw new CrashException(lv4);
    }

    @Override
    public boolean isChunkLoaded(int chunkX, int chunkZ) {
        int k = this.centerPos.getPos().getChebyshevDistance(chunkX, chunkZ);
        return k < this.generationStep.directDependencies().size();
    }

    @Override
    public BlockState getBlockState(BlockPos pos) {
        return this.getChunk(ChunkSectionPos.getSectionCoord(pos.getX()), ChunkSectionPos.getSectionCoord(pos.getZ())).getBlockState(pos);
    }

    @Override
    public FluidState getFluidState(BlockPos pos) {
        return this.getChunk(pos).getFluidState(pos);
    }

    @Override
    @Nullable
    public PlayerEntity getClosestPlayer(double x, double y, double z, double maxDistance, Predicate<Entity> targetPredicate) {
        return null;
    }

    @Override
    public int getAmbientDarkness() {
        return 0;
    }

    @Override
    public BiomeAccess getBiomeAccess() {
        return this.biomeAccess;
    }

    @Override
    public RegistryEntry<Biome> getGeneratorStoredBiome(int biomeX, int biomeY, int biomeZ) {
        return this.world.getGeneratorStoredBiome(biomeX, biomeY, biomeZ);
    }

    @Override
    public float getBrightness(Direction direction, boolean shaded) {
        return 1.0f;
    }

    @Override
    public LightingProvider getLightingProvider() {
        return this.world.getLightingProvider();
    }

    @Override
    public boolean breakBlock(BlockPos pos, boolean drop, @Nullable Entity breakingEntity, int maxUpdateDepth) {
        BlockState lv = this.getBlockState(pos);
        if (lv.isAir()) {
            return false;
        }
        if (drop) {
            BlockEntity lv2 = lv.hasBlockEntity() ? this.getBlockEntity(pos) : null;
            Block.dropStacks(lv, this.world, pos, lv2, breakingEntity, ItemStack.EMPTY);
        }
        return this.setBlockState(pos, Blocks.AIR.getDefaultState(), Block.NOTIFY_ALL, maxUpdateDepth);
    }

    @Override
    @Nullable
    public BlockEntity getBlockEntity(BlockPos pos) {
        Chunk lv = this.getChunk(pos);
        BlockEntity lv2 = lv.getBlockEntity(pos);
        if (lv2 != null) {
            return lv2;
        }
        NbtCompound lv3 = lv.getBlockEntityNbt(pos);
        BlockState lv4 = lv.getBlockState(pos);
        if (lv3 != null) {
            if ("DUMMY".equals(lv3.getString("id"))) {
                if (!lv4.hasBlockEntity()) {
                    return null;
                }
                lv2 = ((BlockEntityProvider)((Object)lv4.getBlock())).createBlockEntity(pos, lv4);
            } else {
                lv2 = BlockEntity.createFromNbt(pos, lv4, lv3, this.world.getRegistryManager());
            }
            if (lv2 != null) {
                lv.setBlockEntity(lv2);
                return lv2;
            }
        }
        if (lv4.hasBlockEntity()) {
            LOGGER.warn("Tried to access a block entity before it was created. {}", (Object)pos);
        }
        return null;
    }

    @Override
    public boolean isValidForSetBlock(BlockPos pos) {
        int i = ChunkSectionPos.getSectionCoord(pos.getX());
        int j = ChunkSectionPos.getSectionCoord(pos.getZ());
        ChunkPos lv = this.getCenterPos();
        int k = Math.abs(lv.x - i);
        int l = Math.abs(lv.z - j);
        if (k > this.generationStep.blockStateWriteRadius() || l > this.generationStep.blockStateWriteRadius()) {
            Util.error("Detected setBlock in a far chunk [" + i + ", " + j + "], pos: " + String.valueOf(pos) + ", status: " + String.valueOf(this.generationStep.targetStatus()) + (String)(this.currentlyGeneratingStructureName == null ? "" : ", currently generating: " + this.currentlyGeneratingStructureName.get()));
            return false;
        }
        if (this.centerPos.hasBelowZeroRetrogen()) {
            HeightLimitView lv2 = this.centerPos.getHeightLimitView();
            if (pos.getY() < lv2.getBottomY() || pos.getY() >= lv2.getTopY()) {
                return false;
            }
        }
        return true;
    }

    @Override
    public boolean setBlockState(BlockPos pos, BlockState state, int flags, int maxUpdateDepth) {
        if (!this.isValidForSetBlock(pos)) {
            return false;
        }
        Chunk lv = this.getChunk(pos);
        BlockState lv2 = lv.setBlockState(pos, state, false);
        if (lv2 != null) {
            this.world.onBlockChanged(pos, lv2, state);
        }
        if (state.hasBlockEntity()) {
            if (lv.getStatus().getChunkType() == ChunkType.LEVELCHUNK) {
                BlockEntity lv3 = ((BlockEntityProvider)((Object)state.getBlock())).createBlockEntity(pos, state);
                if (lv3 != null) {
                    lv.setBlockEntity(lv3);
                } else {
                    lv.removeBlockEntity(pos);
                }
            } else {
                NbtCompound lv4 = new NbtCompound();
                lv4.putInt("x", pos.getX());
                lv4.putInt("y", pos.getY());
                lv4.putInt("z", pos.getZ());
                lv4.putString("id", "DUMMY");
                lv.addPendingBlockEntityNbt(lv4);
            }
        } else if (lv2 != null && lv2.hasBlockEntity()) {
            lv.removeBlockEntity(pos);
        }
        if (state.shouldPostProcess(this, pos)) {
            this.markBlockForPostProcessing(pos);
        }
        return true;
    }

    private void markBlockForPostProcessing(BlockPos pos) {
        this.getChunk(pos).markBlockForPostProcessing(pos);
    }

    @Override
    public boolean spawnEntity(Entity entity) {
        int i = ChunkSectionPos.getSectionCoord(entity.getBlockX());
        int j = ChunkSectionPos.getSectionCoord(entity.getBlockZ());
        this.getChunk(i, j).addEntity(entity);
        return true;
    }

    @Override
    public boolean removeBlock(BlockPos pos, boolean move) {
        return this.setBlockState(pos, Blocks.AIR.getDefaultState(), Block.NOTIFY_ALL);
    }

    @Override
    public WorldBorder getWorldBorder() {
        return this.world.getWorldBorder();
    }

    @Override
    public boolean isClient() {
        return false;
    }

    @Override
    @Deprecated
    public ServerWorld toServerWorld() {
        return this.world;
    }

    @Override
    public DynamicRegistryManager getRegistryManager() {
        return this.world.getRegistryManager();
    }

    @Override
    public FeatureSet getEnabledFeatures() {
        return this.world.getEnabledFeatures();
    }

    @Override
    public WorldProperties getLevelProperties() {
        return this.levelProperties;
    }

    @Override
    public LocalDifficulty getLocalDifficulty(BlockPos pos) {
        if (!this.isChunkLoaded(ChunkSectionPos.getSectionCoord(pos.getX()), ChunkSectionPos.getSectionCoord(pos.getZ()))) {
            throw new RuntimeException("We are asking a region for a chunk out of bound");
        }
        return new LocalDifficulty(this.world.getDifficulty(), this.world.getTimeOfDay(), 0L, this.world.getMoonSize());
    }

    @Override
    @Nullable
    public MinecraftServer getServer() {
        return this.world.getServer();
    }

    @Override
    public ChunkManager getChunkManager() {
        return this.world.getChunkManager();
    }

    @Override
    public long getSeed() {
        return this.seed;
    }

    @Override
    public QueryableTickScheduler<Block> getBlockTickScheduler() {
        return this.blockTickScheduler;
    }

    @Override
    public QueryableTickScheduler<Fluid> getFluidTickScheduler() {
        return this.fluidTickScheduler;
    }

    @Override
    public int getSeaLevel() {
        return this.world.getSeaLevel();
    }

    @Override
    public Random getRandom() {
        return this.random;
    }

    @Override
    public int getTopY(Heightmap.Type heightmap, int x, int z) {
        return this.getChunk(ChunkSectionPos.getSectionCoord(x), ChunkSectionPos.getSectionCoord(z)).sampleHeightmap(heightmap, x & 0xF, z & 0xF) + 1;
    }

    @Override
    public void playSound(@Nullable PlayerEntity source, BlockPos pos, SoundEvent sound, SoundCategory category, float volume, float pitch) {
    }

    @Override
    public void addParticle(ParticleEffect parameters, double x, double y, double z, double velocityX, double velocityY, double velocityZ) {
    }

    @Override
    public void syncWorldEvent(@Nullable PlayerEntity player, int eventId, BlockPos pos, int data) {
    }

    @Override
    public void emitGameEvent(RegistryEntry<GameEvent> event, Vec3d emitterPos, GameEvent.Emitter emitter) {
    }

    @Override
    public DimensionType getDimension() {
        return this.dimension;
    }

    @Override
    public boolean testBlockState(BlockPos pos, Predicate<BlockState> state) {
        return state.test(this.getBlockState(pos));
    }

    @Override
    public boolean testFluidState(BlockPos pos, Predicate<FluidState> state) {
        return state.test(this.getFluidState(pos));
    }

    @Override
    public <T extends Entity> List<T> getEntitiesByType(TypeFilter<Entity, T> filter, Box box, Predicate<? super T> predicate) {
        return Collections.emptyList();
    }

    @Override
    public List<Entity> getOtherEntities(@Nullable Entity except, Box box, @Nullable Predicate<? super Entity> predicate) {
        return Collections.emptyList();
    }

    public List<PlayerEntity> getPlayers() {
        return Collections.emptyList();
    }

    @Override
    public int getBottomY() {
        return this.world.getBottomY();
    }

    @Override
    public int getHeight() {
        return this.world.getHeight();
    }

    @Override
    public long getTickOrder() {
        return this.tickOrder.getAndIncrement();
    }
}

