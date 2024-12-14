/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 * 
 * Could not load the following classes:
 *  org.jetbrains.annotations.Nullable
 */
package net.minecraft.world;

import com.google.common.collect.Lists;
import com.mojang.serialization.Codec;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.function.Supplier;
import net.minecraft.block.AbstractFireBlock;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.component.type.FireworkExplosionComponent;
import net.minecraft.component.type.MapIdComponent;
import net.minecraft.entity.Entity;
import net.minecraft.entity.boss.dragon.EnderDragonEntity;
import net.minecraft.entity.boss.dragon.EnderDragonPart;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.damage.DamageSources;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.fluid.FluidState;
import net.minecraft.fluid.Fluids;
import net.minecraft.item.ItemStack;
import net.minecraft.item.map.MapState;
import net.minecraft.network.packet.Packet;
import net.minecraft.particle.ParticleEffect;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.recipe.BrewingRecipeRegistry;
import net.minecraft.recipe.RecipeManager;
import net.minecraft.registry.DynamicRegistryManager;
import net.minecraft.registry.Registries;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.scoreboard.Scoreboard;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.world.ChunkLevelType;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvent;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.Identifier;
import net.minecraft.util.StringIdentifiable;
import net.minecraft.util.TypeFilter;
import net.minecraft.util.crash.CrashException;
import net.minecraft.util.crash.CrashReport;
import net.minecraft.util.crash.CrashReportSection;
import net.minecraft.util.function.LazyIterationConsumer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.ChunkSectionPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.random.Random;
import net.minecraft.util.profiler.Profiler;
import net.minecraft.world.BlockView;
import net.minecraft.world.GameRules;
import net.minecraft.world.Heightmap;
import net.minecraft.world.LocalDifficulty;
import net.minecraft.world.MutableWorldProperties;
import net.minecraft.world.WorldAccess;
import net.minecraft.world.WorldEvents;
import net.minecraft.world.WorldProperties;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.biome.source.BiomeAccess;
import net.minecraft.world.block.ChainRestrictedNeighborUpdater;
import net.minecraft.world.block.NeighborUpdater;
import net.minecraft.world.border.WorldBorder;
import net.minecraft.world.chunk.BlockEntityTickInvoker;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.ChunkStatus;
import net.minecraft.world.chunk.WorldChunk;
import net.minecraft.world.chunk.light.LightingProvider;
import net.minecraft.world.dimension.DimensionType;
import net.minecraft.world.entity.EntityLookup;
import net.minecraft.world.event.GameEvent;
import net.minecraft.world.explosion.Explosion;
import net.minecraft.world.explosion.ExplosionBehavior;
import net.minecraft.world.tick.TickManager;
import org.jetbrains.annotations.Nullable;

public abstract class World
implements WorldAccess,
AutoCloseable {
    public static final Codec<RegistryKey<World>> CODEC = RegistryKey.createCodec(RegistryKeys.WORLD);
    public static final RegistryKey<World> OVERWORLD = RegistryKey.of(RegistryKeys.WORLD, Identifier.ofVanilla("overworld"));
    public static final RegistryKey<World> NETHER = RegistryKey.of(RegistryKeys.WORLD, Identifier.ofVanilla("the_nether"));
    public static final RegistryKey<World> END = RegistryKey.of(RegistryKeys.WORLD, Identifier.ofVanilla("the_end"));
    public static final int HORIZONTAL_LIMIT = 30000000;
    public static final int MAX_UPDATE_DEPTH = 512;
    public static final int field_30967 = 32;
    public static final int field_30968 = 15;
    public static final int field_30969 = 24000;
    public static final int MAX_Y = 20000000;
    public static final int MIN_Y = -20000000;
    protected final List<BlockEntityTickInvoker> blockEntityTickers = Lists.newArrayList();
    protected final NeighborUpdater neighborUpdater;
    private final List<BlockEntityTickInvoker> pendingBlockEntityTickers = Lists.newArrayList();
    private boolean iteratingTickingBlockEntities;
    private final Thread thread;
    private final boolean debugWorld;
    private int ambientDarkness;
    protected int lcgBlockSeed = Random.create().nextInt();
    protected final int lcgBlockSeedIncrement = 1013904223;
    protected float rainGradientPrev;
    protected float rainGradient;
    protected float thunderGradientPrev;
    protected float thunderGradient;
    public final Random random = Random.create();
    @Deprecated
    private final Random threadSafeRandom = Random.createThreadSafe();
    private final RegistryEntry<DimensionType> dimensionEntry;
    protected final MutableWorldProperties properties;
    private final Supplier<Profiler> profiler;
    public final boolean isClient;
    private final WorldBorder border;
    private final BiomeAccess biomeAccess;
    private final RegistryKey<World> registryKey;
    private final DynamicRegistryManager registryManager;
    private final DamageSources damageSources;
    private long tickOrder;

    protected World(MutableWorldProperties properties, RegistryKey<World> registryRef, DynamicRegistryManager registryManager, RegistryEntry<DimensionType> dimensionEntry, Supplier<Profiler> profiler, boolean isClient, boolean debugWorld, long biomeAccess, int maxChainedNeighborUpdates) {
        this.profiler = profiler;
        this.properties = properties;
        this.dimensionEntry = dimensionEntry;
        final DimensionType lv = dimensionEntry.value();
        this.registryKey = registryRef;
        this.isClient = isClient;
        this.border = lv.coordinateScale() != 1.0 ? new WorldBorder(this){

            @Override
            public double getCenterX() {
                return super.getCenterX() / lv.coordinateScale();
            }

            @Override
            public double getCenterZ() {
                return super.getCenterZ() / lv.coordinateScale();
            }
        } : new WorldBorder();
        this.thread = Thread.currentThread();
        this.biomeAccess = new BiomeAccess(this, biomeAccess);
        this.debugWorld = debugWorld;
        this.neighborUpdater = new ChainRestrictedNeighborUpdater(this, maxChainedNeighborUpdates);
        this.registryManager = registryManager;
        this.damageSources = new DamageSources(registryManager);
    }

    @Override
    public boolean isClient() {
        return this.isClient;
    }

    @Override
    @Nullable
    public MinecraftServer getServer() {
        return null;
    }

    public boolean isInBuildLimit(BlockPos pos) {
        return !this.isOutOfHeightLimit(pos) && World.isValidHorizontally(pos);
    }

    public static boolean isValid(BlockPos pos) {
        return !World.isInvalidVertically(pos.getY()) && World.isValidHorizontally(pos);
    }

    private static boolean isValidHorizontally(BlockPos pos) {
        return pos.getX() >= -30000000 && pos.getZ() >= -30000000 && pos.getX() < 30000000 && pos.getZ() < 30000000;
    }

    private static boolean isInvalidVertically(int y) {
        return y < -20000000 || y >= 20000000;
    }

    public WorldChunk getWorldChunk(BlockPos pos) {
        return this.getChunk(ChunkSectionPos.getSectionCoord(pos.getX()), ChunkSectionPos.getSectionCoord(pos.getZ()));
    }

    @Override
    public WorldChunk getChunk(int i, int j) {
        return (WorldChunk)this.getChunk(i, j, ChunkStatus.FULL);
    }

    @Override
    @Nullable
    public Chunk getChunk(int chunkX, int chunkZ, ChunkStatus leastStatus, boolean create) {
        Chunk lv = this.getChunkManager().getChunk(chunkX, chunkZ, leastStatus, create);
        if (lv == null && create) {
            throw new IllegalStateException("Should always be able to create a chunk!");
        }
        return lv;
    }

    @Override
    public boolean setBlockState(BlockPos pos, BlockState state, int flags) {
        return this.setBlockState(pos, state, flags, 512);
    }

    @Override
    public boolean setBlockState(BlockPos pos, BlockState state, int flags, int maxUpdateDepth) {
        if (this.isOutOfHeightLimit(pos)) {
            return false;
        }
        if (!this.isClient && this.isDebugWorld()) {
            return false;
        }
        WorldChunk lv = this.getWorldChunk(pos);
        Block lv2 = state.getBlock();
        BlockState lv3 = lv.setBlockState(pos, state, (flags & Block.MOVED) != 0);
        if (lv3 != null) {
            BlockState lv4 = this.getBlockState(pos);
            if (lv4 == state) {
                if (lv3 != lv4) {
                    this.scheduleBlockRerenderIfNeeded(pos, lv3, lv4);
                }
                if ((flags & Block.NOTIFY_LISTENERS) != 0 && (!this.isClient || (flags & Block.NO_REDRAW) == 0) && (this.isClient || lv.getLevelType() != null && lv.getLevelType().isAfter(ChunkLevelType.BLOCK_TICKING))) {
                    this.updateListeners(pos, lv3, state, flags);
                }
                if ((flags & Block.NOTIFY_NEIGHBORS) != 0) {
                    this.updateNeighbors(pos, lv3.getBlock());
                    if (!this.isClient && state.hasComparatorOutput()) {
                        this.updateComparators(pos, lv2);
                    }
                }
                if ((flags & Block.FORCE_STATE) == 0 && maxUpdateDepth > 0) {
                    int k = flags & ~(Block.NOTIFY_NEIGHBORS | Block.SKIP_DROPS);
                    lv3.prepare(this, pos, k, maxUpdateDepth - 1);
                    state.updateNeighbors(this, pos, k, maxUpdateDepth - 1);
                    state.prepare(this, pos, k, maxUpdateDepth - 1);
                }
                this.onBlockChanged(pos, lv3, lv4);
            }
            return true;
        }
        return false;
    }

    public void onBlockChanged(BlockPos pos, BlockState oldBlock, BlockState newBlock) {
    }

    @Override
    public boolean removeBlock(BlockPos pos, boolean move) {
        FluidState lv = this.getFluidState(pos);
        return this.setBlockState(pos, lv.getBlockState(), Block.NOTIFY_ALL | (move ? Block.MOVED : 0));
    }

    @Override
    public boolean breakBlock(BlockPos pos, boolean drop, @Nullable Entity breakingEntity, int maxUpdateDepth) {
        boolean bl2;
        BlockState lv = this.getBlockState(pos);
        if (lv.isAir()) {
            return false;
        }
        FluidState lv2 = this.getFluidState(pos);
        if (!(lv.getBlock() instanceof AbstractFireBlock)) {
            this.syncWorldEvent(WorldEvents.BLOCK_BROKEN, pos, Block.getRawIdFromState(lv));
        }
        if (drop) {
            BlockEntity lv3 = lv.hasBlockEntity() ? this.getBlockEntity(pos) : null;
            Block.dropStacks(lv, this, pos, lv3, breakingEntity, ItemStack.EMPTY);
        }
        if (bl2 = this.setBlockState(pos, lv2.getBlockState(), Block.NOTIFY_ALL, maxUpdateDepth)) {
            this.emitGameEvent(GameEvent.BLOCK_DESTROY, pos, GameEvent.Emitter.of(breakingEntity, lv));
        }
        return bl2;
    }

    public void addBlockBreakParticles(BlockPos pos, BlockState state) {
    }

    public boolean setBlockState(BlockPos pos, BlockState state) {
        return this.setBlockState(pos, state, Block.NOTIFY_ALL);
    }

    public abstract void updateListeners(BlockPos var1, BlockState var2, BlockState var3, int var4);

    public void scheduleBlockRerenderIfNeeded(BlockPos pos, BlockState old, BlockState updated) {
    }

    public void updateNeighborsAlways(BlockPos pos, Block sourceBlock) {
    }

    public void updateNeighborsExcept(BlockPos pos, Block sourceBlock, Direction direction) {
    }

    public void updateNeighbor(BlockPos pos, Block sourceBlock, BlockPos sourcePos) {
    }

    public void updateNeighbor(BlockState state, BlockPos pos, Block sourceBlock, BlockPos sourcePos, boolean notify) {
    }

    @Override
    public void replaceWithStateForNeighborUpdate(Direction direction, BlockState neighborState, BlockPos pos, BlockPos neighborPos, int flags, int maxUpdateDepth) {
        this.neighborUpdater.replaceWithStateForNeighborUpdate(direction, neighborState, pos, neighborPos, flags, maxUpdateDepth);
    }

    @Override
    public int getTopY(Heightmap.Type heightmap, int x, int z) {
        int k = x < -30000000 || z < -30000000 || x >= 30000000 || z >= 30000000 ? this.getSeaLevel() + 1 : (this.isChunkLoaded(ChunkSectionPos.getSectionCoord(x), ChunkSectionPos.getSectionCoord(z)) ? this.getChunk(ChunkSectionPos.getSectionCoord(x), ChunkSectionPos.getSectionCoord(z)).sampleHeightmap(heightmap, x & 0xF, z & 0xF) + 1 : this.getBottomY());
        return k;
    }

    @Override
    public LightingProvider getLightingProvider() {
        return this.getChunkManager().getLightingProvider();
    }

    @Override
    public BlockState getBlockState(BlockPos pos) {
        if (this.isOutOfHeightLimit(pos)) {
            return Blocks.VOID_AIR.getDefaultState();
        }
        WorldChunk lv = this.getChunk(ChunkSectionPos.getSectionCoord(pos.getX()), ChunkSectionPos.getSectionCoord(pos.getZ()));
        return lv.getBlockState(pos);
    }

    @Override
    public FluidState getFluidState(BlockPos pos) {
        if (this.isOutOfHeightLimit(pos)) {
            return Fluids.EMPTY.getDefaultState();
        }
        WorldChunk lv = this.getWorldChunk(pos);
        return lv.getFluidState(pos);
    }

    public boolean isDay() {
        return !this.getDimension().hasFixedTime() && this.ambientDarkness < 4;
    }

    public boolean isNight() {
        return !this.getDimension().hasFixedTime() && !this.isDay();
    }

    public void playSound(@Nullable Entity source, BlockPos pos, SoundEvent sound, SoundCategory category, float volume, float pitch) {
        PlayerEntity lv;
        this.playSound(source instanceof PlayerEntity ? (lv = (PlayerEntity)source) : null, pos, sound, category, volume, pitch);
    }

    @Override
    public void playSound(@Nullable PlayerEntity source, BlockPos pos, SoundEvent sound, SoundCategory category, float volume, float pitch) {
        this.playSound(source, (double)pos.getX() + 0.5, (double)pos.getY() + 0.5, (double)pos.getZ() + 0.5, sound, category, volume, pitch);
    }

    public abstract void playSound(@Nullable PlayerEntity var1, double var2, double var4, double var6, RegistryEntry<SoundEvent> var8, SoundCategory var9, float var10, float var11, long var12);

    public void playSound(@Nullable PlayerEntity source, double x, double y, double z, SoundEvent sound, SoundCategory category, float volume, float pitch, long seed) {
        this.playSound(source, x, y, z, Registries.SOUND_EVENT.getEntry(sound), category, volume, pitch, seed);
    }

    public abstract void playSoundFromEntity(@Nullable PlayerEntity var1, Entity var2, RegistryEntry<SoundEvent> var3, SoundCategory var4, float var5, float var6, long var7);

    public void playSound(@Nullable PlayerEntity source, double x, double y, double z, SoundEvent sound, SoundCategory category) {
        this.playSound(source, x, y, z, sound, category, 1.0f, 1.0f);
    }

    public void playSound(@Nullable PlayerEntity source, double x, double y, double z, SoundEvent sound, SoundCategory category, float volume, float pitch) {
        this.playSound(source, x, y, z, sound, category, volume, pitch, this.threadSafeRandom.nextLong());
    }

    public void playSound(@Nullable PlayerEntity source, double x, double y, double z, RegistryEntry<SoundEvent> sound, SoundCategory category, float volume, float pitch) {
        this.playSound(source, x, y, z, sound, category, volume, pitch, this.threadSafeRandom.nextLong());
    }

    public void playSoundFromEntity(@Nullable PlayerEntity source, Entity entity, SoundEvent sound, SoundCategory category, float volume, float pitch) {
        this.playSoundFromEntity(source, entity, Registries.SOUND_EVENT.getEntry(sound), category, volume, pitch, this.threadSafeRandom.nextLong());
    }

    public void playSoundAtBlockCenter(BlockPos pos, SoundEvent sound, SoundCategory category, float volume, float pitch, boolean useDistance) {
        this.playSound((double)pos.getX() + 0.5, (double)pos.getY() + 0.5, (double)pos.getZ() + 0.5, sound, category, volume, pitch, useDistance);
    }

    public void playSoundFromEntity(Entity entity, SoundEvent sound, SoundCategory category, float volume, float pitch) {
    }

    public void playSound(double x, double y, double z, SoundEvent sound, SoundCategory category, float volume, float pitch, boolean useDistance) {
    }

    @Override
    public void addParticle(ParticleEffect parameters, double x, double y, double z, double velocityX, double velocityY, double velocityZ) {
    }

    public void addParticle(ParticleEffect parameters, boolean alwaysSpawn, double x, double y, double z, double velocityX, double velocityY, double velocityZ) {
    }

    public void addImportantParticle(ParticleEffect parameters, double x, double y, double z, double velocityX, double velocityY, double velocityZ) {
    }

    public void addImportantParticle(ParticleEffect parameters, boolean alwaysSpawn, double x, double y, double z, double velocityX, double velocityY, double velocityZ) {
    }

    public float getSkyAngleRadians(float tickDelta) {
        float g = this.getSkyAngle(tickDelta);
        return g * ((float)Math.PI * 2);
    }

    public void addBlockEntityTicker(BlockEntityTickInvoker ticker) {
        (this.iteratingTickingBlockEntities ? this.pendingBlockEntityTickers : this.blockEntityTickers).add(ticker);
    }

    protected void tickBlockEntities() {
        Profiler lv = this.getProfiler();
        lv.push("blockEntities");
        this.iteratingTickingBlockEntities = true;
        if (!this.pendingBlockEntityTickers.isEmpty()) {
            this.blockEntityTickers.addAll(this.pendingBlockEntityTickers);
            this.pendingBlockEntityTickers.clear();
        }
        Iterator<BlockEntityTickInvoker> iterator = this.blockEntityTickers.iterator();
        boolean bl = this.getTickManager().shouldTick();
        while (iterator.hasNext()) {
            BlockEntityTickInvoker lv2 = iterator.next();
            if (lv2.isRemoved()) {
                iterator.remove();
                continue;
            }
            if (!bl || !this.shouldTickBlockPos(lv2.getPos())) continue;
            lv2.tick();
        }
        this.iteratingTickingBlockEntities = false;
        lv.pop();
    }

    public <T extends Entity> void tickEntity(Consumer<T> tickConsumer, T entity) {
        try {
            tickConsumer.accept(entity);
        } catch (Throwable throwable) {
            CrashReport lv = CrashReport.create(throwable, "Ticking entity");
            CrashReportSection lv2 = lv.addElement("Entity being ticked");
            entity.populateCrashReport(lv2);
            throw new CrashException(lv);
        }
    }

    public boolean shouldUpdatePostDeath(Entity entity) {
        return true;
    }

    public boolean shouldTickBlocksInChunk(long chunkPos) {
        return true;
    }

    public boolean shouldTickBlockPos(BlockPos pos) {
        return this.shouldTickBlocksInChunk(ChunkPos.toLong(pos));
    }

    public Explosion createExplosion(@Nullable Entity entity, double x, double y, double z, float power, ExplosionSourceType explosionSourceType) {
        return this.createExplosion(entity, Explosion.createDamageSource(this, entity), null, x, y, z, power, false, explosionSourceType, ParticleTypes.EXPLOSION, ParticleTypes.EXPLOSION_EMITTER, SoundEvents.ENTITY_GENERIC_EXPLODE);
    }

    public Explosion createExplosion(@Nullable Entity entity, double x, double y, double z, float power, boolean createFire, ExplosionSourceType explosionSourceType) {
        return this.createExplosion(entity, Explosion.createDamageSource(this, entity), null, x, y, z, power, createFire, explosionSourceType, ParticleTypes.EXPLOSION, ParticleTypes.EXPLOSION_EMITTER, SoundEvents.ENTITY_GENERIC_EXPLODE);
    }

    public Explosion createExplosion(@Nullable Entity entity, @Nullable DamageSource damageSource, @Nullable ExplosionBehavior behavior, Vec3d pos, float power, boolean createFire, ExplosionSourceType explosionSourceType) {
        return this.createExplosion(entity, damageSource, behavior, pos.getX(), pos.getY(), pos.getZ(), power, createFire, explosionSourceType, ParticleTypes.EXPLOSION, ParticleTypes.EXPLOSION_EMITTER, SoundEvents.ENTITY_GENERIC_EXPLODE);
    }

    public Explosion createExplosion(@Nullable Entity entity, @Nullable DamageSource damageSource, @Nullable ExplosionBehavior behavior, double x, double y, double z, float power, boolean createFire, ExplosionSourceType explosionSourceType) {
        return this.createExplosion(entity, damageSource, behavior, x, y, z, power, createFire, explosionSourceType, ParticleTypes.EXPLOSION, ParticleTypes.EXPLOSION_EMITTER, SoundEvents.ENTITY_GENERIC_EXPLODE);
    }

    public Explosion createExplosion(@Nullable Entity entity, @Nullable DamageSource damageSource, @Nullable ExplosionBehavior behavior, double x, double y, double z, float power, boolean createFire, ExplosionSourceType explosionSourceType, ParticleEffect particle, ParticleEffect emitterParticle, RegistryEntry<SoundEvent> soundEvent) {
        return this.createExplosion(entity, damageSource, behavior, x, y, z, power, createFire, explosionSourceType, true, particle, emitterParticle, soundEvent);
    }

    public Explosion createExplosion(@Nullable Entity entity, @Nullable DamageSource damageSource, @Nullable ExplosionBehavior behavior, double x, double y, double z, float power, boolean createFire, ExplosionSourceType explosionSourceType, boolean particles, ParticleEffect particle, ParticleEffect emitterParticle, RegistryEntry<SoundEvent> soundEvent) {
        Explosion.DestructionType lv = switch (explosionSourceType.ordinal()) {
            default -> throw new MatchException(null, null);
            case 0 -> Explosion.DestructionType.KEEP;
            case 1 -> this.getDestructionType(GameRules.BLOCK_EXPLOSION_DROP_DECAY);
            case 2 -> {
                if (this.getGameRules().getBoolean(GameRules.DO_MOB_GRIEFING)) {
                    yield this.getDestructionType(GameRules.MOB_EXPLOSION_DROP_DECAY);
                }
                yield Explosion.DestructionType.KEEP;
            }
            case 3 -> this.getDestructionType(GameRules.TNT_EXPLOSION_DROP_DECAY);
            case 4 -> Explosion.DestructionType.TRIGGER_BLOCK;
        };
        Explosion lv2 = new Explosion(this, entity, damageSource, behavior, x, y, z, power, createFire, lv, particle, emitterParticle, soundEvent);
        lv2.collectBlocksAndDamageEntities();
        lv2.affectWorld(particles);
        return lv2;
    }

    private Explosion.DestructionType getDestructionType(GameRules.Key<GameRules.BooleanRule> gameRuleKey) {
        return this.getGameRules().getBoolean(gameRuleKey) ? Explosion.DestructionType.DESTROY_WITH_DECAY : Explosion.DestructionType.DESTROY;
    }

    public abstract String asString();

    @Override
    @Nullable
    public BlockEntity getBlockEntity(BlockPos pos) {
        if (this.isOutOfHeightLimit(pos)) {
            return null;
        }
        if (!this.isClient && Thread.currentThread() != this.thread) {
            return null;
        }
        return this.getWorldChunk(pos).getBlockEntity(pos, WorldChunk.CreationType.IMMEDIATE);
    }

    public void addBlockEntity(BlockEntity blockEntity) {
        BlockPos lv = blockEntity.getPos();
        if (this.isOutOfHeightLimit(lv)) {
            return;
        }
        this.getWorldChunk(lv).addBlockEntity(blockEntity);
    }

    public void removeBlockEntity(BlockPos pos) {
        if (this.isOutOfHeightLimit(pos)) {
            return;
        }
        this.getWorldChunk(pos).removeBlockEntity(pos);
    }

    public boolean canSetBlock(BlockPos pos) {
        if (this.isOutOfHeightLimit(pos)) {
            return false;
        }
        return this.getChunkManager().isChunkLoaded(ChunkSectionPos.getSectionCoord(pos.getX()), ChunkSectionPos.getSectionCoord(pos.getZ()));
    }

    public boolean isDirectionSolid(BlockPos pos, Entity entity, Direction direction) {
        if (this.isOutOfHeightLimit(pos)) {
            return false;
        }
        Chunk lv = this.getChunk(ChunkSectionPos.getSectionCoord(pos.getX()), ChunkSectionPos.getSectionCoord(pos.getZ()), ChunkStatus.FULL, false);
        if (lv == null) {
            return false;
        }
        return lv.getBlockState(pos).isSolidSurface(this, pos, entity, direction);
    }

    public boolean isTopSolid(BlockPos pos, Entity entity) {
        return this.isDirectionSolid(pos, entity, Direction.UP);
    }

    public void calculateAmbientDarkness() {
        double d = 1.0 - (double)(this.getRainGradient(1.0f) * 5.0f) / 16.0;
        double e = 1.0 - (double)(this.getThunderGradient(1.0f) * 5.0f) / 16.0;
        double f = 0.5 + 2.0 * MathHelper.clamp((double)MathHelper.cos(this.getSkyAngle(1.0f) * ((float)Math.PI * 2)), -0.25, 0.25);
        this.ambientDarkness = (int)((1.0 - f * d * e) * 11.0);
    }

    public void setMobSpawnOptions(boolean spawnMonsters, boolean spawnAnimals) {
        this.getChunkManager().setMobSpawnOptions(spawnMonsters, spawnAnimals);
    }

    public BlockPos getSpawnPos() {
        BlockPos lv = this.properties.getSpawnPos();
        if (!this.getWorldBorder().contains(lv)) {
            lv = this.getTopPosition(Heightmap.Type.MOTION_BLOCKING, BlockPos.ofFloored(this.getWorldBorder().getCenterX(), 0.0, this.getWorldBorder().getCenterZ()));
        }
        return lv;
    }

    public float getSpawnAngle() {
        return this.properties.getSpawnAngle();
    }

    protected void initWeatherGradients() {
        if (this.properties.isRaining()) {
            this.rainGradient = 1.0f;
            if (this.properties.isThundering()) {
                this.thunderGradient = 1.0f;
            }
        }
    }

    @Override
    public void close() throws IOException {
        this.getChunkManager().close();
    }

    @Override
    @Nullable
    public BlockView getChunkAsView(int chunkX, int chunkZ) {
        return this.getChunk(chunkX, chunkZ, ChunkStatus.FULL, false);
    }

    @Override
    public List<Entity> getOtherEntities(@Nullable Entity except, Box box, Predicate<? super Entity> predicate) {
        this.getProfiler().visit("getEntities");
        ArrayList<Entity> list = Lists.newArrayList();
        this.getEntityLookup().forEachIntersects(box, entity -> {
            if (entity != except && predicate.test((Entity)entity)) {
                list.add((Entity)entity);
            }
            if (entity instanceof EnderDragonEntity) {
                for (EnderDragonPart lv : ((EnderDragonEntity)entity).getBodyParts()) {
                    if (entity == except || !predicate.test(lv)) continue;
                    list.add(lv);
                }
            }
        });
        return list;
    }

    @Override
    public <T extends Entity> List<T> getEntitiesByType(TypeFilter<Entity, T> filter, Box box, Predicate<? super T> predicate) {
        ArrayList list = Lists.newArrayList();
        this.collectEntitiesByType(filter, box, predicate, list);
        return list;
    }

    public <T extends Entity> void collectEntitiesByType(TypeFilter<Entity, T> filter, Box box, Predicate<? super T> predicate, List<? super T> result) {
        this.collectEntitiesByType(filter, box, predicate, result, Integer.MAX_VALUE);
    }

    public <T extends Entity> void collectEntitiesByType(TypeFilter<Entity, T> filter, Box box, Predicate<? super T> predicate, List<? super T> result, int limit) {
        this.getProfiler().visit("getEntities");
        this.getEntityLookup().forEachIntersects(filter, box, entity -> {
            if (predicate.test(entity)) {
                result.add((Object)entity);
                if (result.size() >= limit) {
                    return LazyIterationConsumer.NextIteration.ABORT;
                }
            }
            if (entity instanceof EnderDragonEntity) {
                EnderDragonEntity lv = (EnderDragonEntity)entity;
                for (EnderDragonPart lv2 : lv.getBodyParts()) {
                    Entity lv3 = (Entity)filter.downcast(lv2);
                    if (lv3 == null || !predicate.test(lv3)) continue;
                    result.add((Object)lv3);
                    if (result.size() < limit) continue;
                    return LazyIterationConsumer.NextIteration.ABORT;
                }
            }
            return LazyIterationConsumer.NextIteration.CONTINUE;
        });
    }

    @Nullable
    public abstract Entity getEntityById(int var1);

    public void markDirty(BlockPos pos) {
        if (this.isChunkLoaded(pos)) {
            this.getWorldChunk(pos).setNeedsSaving(true);
        }
    }

    @Override
    public int getSeaLevel() {
        return 63;
    }

    public void disconnect() {
    }

    public long getTime() {
        return this.properties.getTime();
    }

    public long getTimeOfDay() {
        return this.properties.getTimeOfDay();
    }

    public boolean canPlayerModifyAt(PlayerEntity player, BlockPos pos) {
        return true;
    }

    public void sendEntityStatus(Entity entity, byte status) {
    }

    public void sendEntityDamage(Entity entity, DamageSource damageSource) {
    }

    public void addSyncedBlockEvent(BlockPos pos, Block block, int type, int data) {
        this.getBlockState(pos).onSyncedBlockEvent(this, pos, type, data);
    }

    @Override
    public WorldProperties getLevelProperties() {
        return this.properties;
    }

    public GameRules getGameRules() {
        return this.properties.getGameRules();
    }

    public abstract TickManager getTickManager();

    public float getThunderGradient(float delta) {
        return MathHelper.lerp(delta, this.thunderGradientPrev, this.thunderGradient) * this.getRainGradient(delta);
    }

    public void setThunderGradient(float thunderGradient) {
        float g;
        this.thunderGradientPrev = g = MathHelper.clamp(thunderGradient, 0.0f, 1.0f);
        this.thunderGradient = g;
    }

    public float getRainGradient(float delta) {
        return MathHelper.lerp(delta, this.rainGradientPrev, this.rainGradient);
    }

    public void setRainGradient(float rainGradient) {
        float g;
        this.rainGradientPrev = g = MathHelper.clamp(rainGradient, 0.0f, 1.0f);
        this.rainGradient = g;
    }

    public boolean isThundering() {
        if (!this.getDimension().hasSkyLight() || this.getDimension().hasCeiling()) {
            return false;
        }
        return (double)this.getThunderGradient(1.0f) > 0.9;
    }

    public boolean isRaining() {
        return (double)this.getRainGradient(1.0f) > 0.2;
    }

    public boolean hasRain(BlockPos pos) {
        if (!this.isRaining()) {
            return false;
        }
        if (!this.isSkyVisible(pos)) {
            return false;
        }
        if (this.getTopPosition(Heightmap.Type.MOTION_BLOCKING, pos).getY() > pos.getY()) {
            return false;
        }
        Biome lv = this.getBiome(pos).value();
        return lv.getPrecipitation(pos) == Biome.Precipitation.RAIN;
    }

    @Nullable
    public abstract MapState getMapState(MapIdComponent var1);

    public abstract void putMapState(MapIdComponent var1, MapState var2);

    public abstract MapIdComponent increaseAndGetMapId();

    public void syncGlobalEvent(int eventId, BlockPos pos, int data) {
    }

    public CrashReportSection addDetailsToCrashReport(CrashReport report) {
        CrashReportSection lv = report.addElement("Affected level", 1);
        lv.add("All players", () -> this.getPlayers().size() + " total; " + String.valueOf(this.getPlayers()));
        lv.add("Chunk stats", this.getChunkManager()::getDebugString);
        lv.add("Level dimension", () -> this.getRegistryKey().getValue().toString());
        try {
            this.properties.populateCrashReport(lv, this);
        } catch (Throwable throwable) {
            lv.add("Level Data Unobtainable", throwable);
        }
        return lv;
    }

    public abstract void setBlockBreakingInfo(int var1, BlockPos var2, int var3);

    public void addFireworkParticle(double x, double y, double z, double velocityX, double velocityY, double velocityZ, List<FireworkExplosionComponent> explosions) {
    }

    public abstract Scoreboard getScoreboard();

    public void updateComparators(BlockPos pos, Block block) {
        for (Direction lv : Direction.Type.HORIZONTAL) {
            BlockPos lv2 = pos.offset(lv);
            if (!this.isChunkLoaded(lv2)) continue;
            BlockState lv3 = this.getBlockState(lv2);
            if (lv3.isOf(Blocks.COMPARATOR)) {
                this.updateNeighbor(lv3, lv2, block, pos, false);
                continue;
            }
            if (!lv3.isSolidBlock(this, lv2) || !(lv3 = this.getBlockState(lv2 = lv2.offset(lv))).isOf(Blocks.COMPARATOR)) continue;
            this.updateNeighbor(lv3, lv2, block, pos, false);
        }
    }

    @Override
    public LocalDifficulty getLocalDifficulty(BlockPos pos) {
        long l = 0L;
        float f = 0.0f;
        if (this.isChunkLoaded(pos)) {
            f = this.getMoonSize();
            l = this.getWorldChunk(pos).getInhabitedTime();
        }
        return new LocalDifficulty(this.getDifficulty(), this.getTimeOfDay(), l, f);
    }

    @Override
    public int getAmbientDarkness() {
        return this.ambientDarkness;
    }

    public void setLightningTicksLeft(int lightningTicksLeft) {
    }

    @Override
    public WorldBorder getWorldBorder() {
        return this.border;
    }

    public void sendPacket(Packet<?> packet) {
        throw new UnsupportedOperationException("Can't send packets to server unless you're on the client.");
    }

    @Override
    public DimensionType getDimension() {
        return this.dimensionEntry.value();
    }

    public RegistryEntry<DimensionType> getDimensionEntry() {
        return this.dimensionEntry;
    }

    public RegistryKey<World> getRegistryKey() {
        return this.registryKey;
    }

    @Override
    public Random getRandom() {
        return this.random;
    }

    @Override
    public boolean testBlockState(BlockPos pos, Predicate<BlockState> state) {
        return state.test(this.getBlockState(pos));
    }

    @Override
    public boolean testFluidState(BlockPos pos, Predicate<FluidState> state) {
        return state.test(this.getFluidState(pos));
    }

    public abstract RecipeManager getRecipeManager();

    public BlockPos getRandomPosInChunk(int x, int y, int z, int l) {
        this.lcgBlockSeed = this.lcgBlockSeed * 3 + 1013904223;
        int m = this.lcgBlockSeed >> 2;
        return new BlockPos(x + (m & 0xF), y + (m >> 16 & l), z + (m >> 8 & 0xF));
    }

    public boolean isSavingDisabled() {
        return false;
    }

    public Profiler getProfiler() {
        return this.profiler.get();
    }

    public Supplier<Profiler> getProfilerSupplier() {
        return this.profiler;
    }

    @Override
    public BiomeAccess getBiomeAccess() {
        return this.biomeAccess;
    }

    public final boolean isDebugWorld() {
        return this.debugWorld;
    }

    protected abstract EntityLookup<Entity> getEntityLookup();

    @Override
    public long getTickOrder() {
        return this.tickOrder++;
    }

    @Override
    public DynamicRegistryManager getRegistryManager() {
        return this.registryManager;
    }

    public DamageSources getDamageSources() {
        return this.damageSources;
    }

    public abstract BrewingRecipeRegistry getBrewingRecipeRegistry();

    @Override
    public /* synthetic */ Chunk getChunk(int chunkX, int chunkZ) {
        return this.getChunk(chunkX, chunkZ);
    }

    public static enum ExplosionSourceType implements StringIdentifiable
    {
        NONE("none"),
        BLOCK("block"),
        MOB("mob"),
        TNT("tnt"),
        TRIGGER("trigger");

        public static final Codec<ExplosionSourceType> CODEC;
        private final String id;

        private ExplosionSourceType(String id) {
            this.id = id;
        }

        @Override
        public String asString() {
            return this.id;
        }

        static {
            CODEC = StringIdentifiable.createCodec(ExplosionSourceType::values);
        }
    }
}

