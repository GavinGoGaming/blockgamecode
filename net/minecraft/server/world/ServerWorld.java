/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 * 
 * Could not load the following classes:
 *  org.jetbrains.annotations.NotNull
 *  org.jetbrains.annotations.Nullable
 */
package net.minecraft.server.world;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.Lists;
import com.mojang.datafixers.DataFixer;
import com.mojang.datafixers.util.Pair;
import com.mojang.logging.LogUtils;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.longs.LongSet;
import it.unimi.dsi.fastutil.longs.LongSets;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import it.unimi.dsi.fastutil.objects.ObjectLinkedOpenHashSet;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.OpenOption;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.Executor;
import java.util.function.BooleanSupplier;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.SnowBlock;
import net.minecraft.component.type.MapIdComponent;
import net.minecraft.datafixer.DataFixTypes;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityInteraction;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.InteractionObserver;
import net.minecraft.entity.LightningEntity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.Npc;
import net.minecraft.entity.SpawnGroup;
import net.minecraft.entity.ai.pathing.EntityNavigation;
import net.minecraft.entity.ai.pathing.PathNodeTypeCache;
import net.minecraft.entity.boss.dragon.EnderDragonEntity;
import net.minecraft.entity.boss.dragon.EnderDragonFight;
import net.minecraft.entity.boss.dragon.EnderDragonPart;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.mob.SkeletonHorseEntity;
import net.minecraft.entity.mob.WaterCreatureEntity;
import net.minecraft.entity.passive.AnimalEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.fluid.Fluid;
import net.minecraft.fluid.FluidState;
import net.minecraft.item.map.MapState;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.s2c.play.BlockBreakingProgressS2CPacket;
import net.minecraft.network.packet.s2c.play.BlockEventS2CPacket;
import net.minecraft.network.packet.s2c.play.EntityDamageS2CPacket;
import net.minecraft.network.packet.s2c.play.EntityStatusS2CPacket;
import net.minecraft.network.packet.s2c.play.ExplosionS2CPacket;
import net.minecraft.network.packet.s2c.play.GameStateChangeS2CPacket;
import net.minecraft.network.packet.s2c.play.ParticleS2CPacket;
import net.minecraft.network.packet.s2c.play.PlaySoundFromEntityS2CPacket;
import net.minecraft.network.packet.s2c.play.PlaySoundS2CPacket;
import net.minecraft.network.packet.s2c.play.PlayerSpawnPositionS2CPacket;
import net.minecraft.network.packet.s2c.play.WorldEventS2CPacket;
import net.minecraft.particle.ParticleEffect;
import net.minecraft.recipe.BrewingRecipeRegistry;
import net.minecraft.recipe.RecipeManager;
import net.minecraft.registry.Registries;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.registry.entry.RegistryEntryList;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.resource.featuretoggle.FeatureSet;
import net.minecraft.scoreboard.Scoreboard;
import net.minecraft.scoreboard.ServerScoreboard;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.WorldGenerationProgressListener;
import net.minecraft.server.network.DebugInfoSender;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.BlockEvent;
import net.minecraft.server.world.ChunkTicketType;
import net.minecraft.server.world.ServerChunkLoadingManager;
import net.minecraft.server.world.ServerChunkManager;
import net.minecraft.server.world.ServerEntityManager;
import net.minecraft.server.world.SleepManager;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvent;
import net.minecraft.structure.StructureTemplateManager;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.CsvWriter;
import net.minecraft.util.Identifier;
import net.minecraft.util.ProgressListener;
import net.minecraft.util.TypeFilter;
import net.minecraft.util.Unit;
import net.minecraft.util.Util;
import net.minecraft.util.crash.CrashReport;
import net.minecraft.util.crash.CrashReportSection;
import net.minecraft.util.crash.ReportType;
import net.minecraft.util.function.BooleanBiFunction;
import net.minecraft.util.function.LazyIterationConsumer;
import net.minecraft.util.math.BlockBox;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.ChunkSectionPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.intprovider.IntProvider;
import net.minecraft.util.math.intprovider.UniformIntProvider;
import net.minecraft.util.math.random.Random;
import net.minecraft.util.math.random.RandomSequencesState;
import net.minecraft.util.profiler.Profiler;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.util.shape.VoxelShapes;
import net.minecraft.village.raid.Raid;
import net.minecraft.village.raid.RaidManager;
import net.minecraft.world.EntityList;
import net.minecraft.world.ForcedChunkState;
import net.minecraft.world.GameRules;
import net.minecraft.world.Heightmap;
import net.minecraft.world.IdCountsState;
import net.minecraft.world.LocalDifficulty;
import net.minecraft.world.PersistentStateManager;
import net.minecraft.world.SpawnHelper;
import net.minecraft.world.StructureLocator;
import net.minecraft.world.StructureWorldAccess;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.chunk.BlockEntityTickInvoker;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.ChunkManager;
import net.minecraft.world.chunk.ChunkSection;
import net.minecraft.world.chunk.WorldChunk;
import net.minecraft.world.dimension.DimensionOptions;
import net.minecraft.world.dimension.DimensionTypes;
import net.minecraft.world.dimension.PortalForcer;
import net.minecraft.world.entity.EntityHandler;
import net.minecraft.world.entity.EntityLookup;
import net.minecraft.world.event.GameEvent;
import net.minecraft.world.event.listener.EntityGameEventHandler;
import net.minecraft.world.event.listener.GameEventDispatchManager;
import net.minecraft.world.explosion.Explosion;
import net.minecraft.world.explosion.ExplosionBehavior;
import net.minecraft.world.gen.StructureAccessor;
import net.minecraft.world.gen.chunk.ChunkGenerator;
import net.minecraft.world.gen.structure.Structure;
import net.minecraft.world.level.ServerWorldProperties;
import net.minecraft.world.level.storage.LevelStorage;
import net.minecraft.world.poi.PointOfInterestStorage;
import net.minecraft.world.poi.PointOfInterestType;
import net.minecraft.world.poi.PointOfInterestTypes;
import net.minecraft.world.spawner.SpecialSpawner;
import net.minecraft.world.storage.ChunkPosKeyedStorage;
import net.minecraft.world.storage.EntityChunkDataAccess;
import net.minecraft.world.storage.StorageKey;
import net.minecraft.world.tick.QueryableTickScheduler;
import net.minecraft.world.tick.TickManager;
import net.minecraft.world.tick.WorldTickScheduler;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;

public class ServerWorld
extends World
implements StructureWorldAccess {
    public static final BlockPos END_SPAWN_POS = new BlockPos(100, 50, 0);
    public static final IntProvider CLEAR_WEATHER_DURATION_PROVIDER = UniformIntProvider.create(12000, 180000);
    public static final IntProvider RAIN_WEATHER_DURATION_PROVIDER = UniformIntProvider.create(12000, 24000);
    private static final IntProvider CLEAR_THUNDER_WEATHER_DURATION_PROVIDER = UniformIntProvider.create(12000, 180000);
    public static final IntProvider THUNDER_WEATHER_DURATION_PROVIDER = UniformIntProvider.create(3600, 15600);
    private static final Logger LOGGER = LogUtils.getLogger();
    private static final int SERVER_IDLE_COOLDOWN = 300;
    private static final int MAX_TICKS = 65536;
    final List<ServerPlayerEntity> players = Lists.newArrayList();
    private final ServerChunkManager chunkManager;
    private final MinecraftServer server;
    private final ServerWorldProperties worldProperties;
    private int spawnChunkRadius;
    final EntityList entityList = new EntityList();
    private final ServerEntityManager<Entity> entityManager;
    private final GameEventDispatchManager gameEventDispatchManager;
    public boolean savingDisabled;
    private final SleepManager sleepManager;
    private int idleTimeout;
    private final PortalForcer portalForcer;
    private final WorldTickScheduler<Block> blockTickScheduler = new WorldTickScheduler(this::isTickingFutureReady, this.getProfilerSupplier());
    private final WorldTickScheduler<Fluid> fluidTickScheduler = new WorldTickScheduler(this::isTickingFutureReady, this.getProfilerSupplier());
    private final PathNodeTypeCache pathNodeTypeCache = new PathNodeTypeCache();
    final Set<MobEntity> loadedMobs = new ObjectOpenHashSet<MobEntity>();
    volatile boolean duringListenerUpdate;
    protected final RaidManager raidManager;
    private final ObjectLinkedOpenHashSet<BlockEvent> syncedBlockEventQueue = new ObjectLinkedOpenHashSet();
    private final List<BlockEvent> blockEventQueue = new ArrayList<BlockEvent>(64);
    private boolean inBlockTick;
    private final List<SpecialSpawner> spawners;
    @Nullable
    private EnderDragonFight enderDragonFight;
    final Int2ObjectMap<EnderDragonPart> dragonParts = new Int2ObjectOpenHashMap<EnderDragonPart>();
    private final StructureAccessor structureAccessor;
    private final StructureLocator structureLocator;
    private final boolean shouldTickTime;
    private final RandomSequencesState randomSequences;

    public ServerWorld(MinecraftServer server, Executor workerExecutor, LevelStorage.Session session, ServerWorldProperties properties, RegistryKey<World> worldKey, DimensionOptions dimensionOptions, WorldGenerationProgressListener worldGenerationProgressListener, boolean debugWorld, long seed, List<SpecialSpawner> spawners, boolean shouldTickTime, @Nullable RandomSequencesState randomSequencesState) {
        super(properties, worldKey, server.getRegistryManager(), dimensionOptions.dimensionTypeEntry(), server::getProfiler, false, debugWorld, seed, server.getMaxChainedNeighborUpdates());
        this.shouldTickTime = shouldTickTime;
        this.server = server;
        this.spawners = spawners;
        this.worldProperties = properties;
        ChunkGenerator lv = dimensionOptions.chunkGenerator();
        boolean bl3 = server.syncChunkWrites();
        DataFixer dataFixer = server.getDataFixer();
        EntityChunkDataAccess lv2 = new EntityChunkDataAccess(new ChunkPosKeyedStorage(new StorageKey(session.getDirectoryName(), worldKey, "entities"), session.getWorldDirectory(worldKey).resolve("entities"), dataFixer, bl3, DataFixTypes.ENTITY_CHUNK), this, server);
        this.entityManager = new ServerEntityManager<Entity>(Entity.class, new ServerEntityHandler(), lv2);
        this.chunkManager = new ServerChunkManager(this, session, dataFixer, server.getStructureTemplateManager(), workerExecutor, lv, server.getPlayerManager().getViewDistance(), server.getPlayerManager().getSimulationDistance(), bl3, worldGenerationProgressListener, this.entityManager::updateTrackingStatus, () -> server.getOverworld().getPersistentStateManager());
        this.chunkManager.getStructurePlacementCalculator().tryCalculate();
        this.portalForcer = new PortalForcer(this);
        this.calculateAmbientDarkness();
        this.initWeatherGradients();
        this.getWorldBorder().setMaxRadius(server.getMaxWorldBorderRadius());
        this.raidManager = this.getPersistentStateManager().getOrCreate(RaidManager.getPersistentStateType(this), RaidManager.nameFor(this.getDimensionEntry()));
        if (!server.isSingleplayer()) {
            properties.setGameMode(server.getDefaultGameMode());
        }
        long m = server.getSaveProperties().getGeneratorOptions().getSeed();
        this.structureLocator = new StructureLocator(this.chunkManager.getChunkIoWorker(), this.getRegistryManager(), server.getStructureTemplateManager(), worldKey, lv, this.chunkManager.getNoiseConfig(), this, lv.getBiomeSource(), m, dataFixer);
        this.structureAccessor = new StructureAccessor(this, server.getSaveProperties().getGeneratorOptions(), this.structureLocator);
        this.enderDragonFight = this.getRegistryKey() == World.END && this.getDimensionEntry().matchesKey(DimensionTypes.THE_END) ? new EnderDragonFight(this, m, server.getSaveProperties().getDragonFight()) : null;
        this.sleepManager = new SleepManager();
        this.gameEventDispatchManager = new GameEventDispatchManager(this);
        this.randomSequences = Objects.requireNonNullElseGet(randomSequencesState, () -> this.getPersistentStateManager().getOrCreate(RandomSequencesState.getPersistentStateType(m), "random_sequences"));
    }

    @Deprecated
    @VisibleForTesting
    public void setEnderDragonFight(@Nullable EnderDragonFight enderDragonFight) {
        this.enderDragonFight = enderDragonFight;
    }

    public void setWeather(int clearDuration, int rainDuration, boolean raining, boolean thundering) {
        this.worldProperties.setClearWeatherTime(clearDuration);
        this.worldProperties.setRainTime(rainDuration);
        this.worldProperties.setThunderTime(rainDuration);
        this.worldProperties.setRaining(raining);
        this.worldProperties.setThundering(thundering);
    }

    @Override
    public RegistryEntry<Biome> getGeneratorStoredBiome(int biomeX, int biomeY, int biomeZ) {
        return this.getChunkManager().getChunkGenerator().getBiomeSource().getBiome(biomeX, biomeY, biomeZ, this.getChunkManager().getNoiseConfig().getMultiNoiseSampler());
    }

    public StructureAccessor getStructureAccessor() {
        return this.structureAccessor;
    }

    public void tick(BooleanSupplier shouldKeepTicking) {
        boolean bl2;
        long l;
        int i;
        Profiler lv = this.getProfiler();
        this.inBlockTick = true;
        TickManager lv2 = this.getTickManager();
        boolean bl = lv2.shouldTick();
        if (bl) {
            lv.push("world border");
            this.getWorldBorder().tick();
            lv.swap("weather");
            this.tickWeather();
        }
        if (this.sleepManager.canSkipNight(i = this.getGameRules().getInt(GameRules.PLAYERS_SLEEPING_PERCENTAGE)) && this.sleepManager.canResetTime(i, this.players)) {
            if (this.getGameRules().getBoolean(GameRules.DO_DAYLIGHT_CYCLE)) {
                l = this.properties.getTimeOfDay() + 24000L;
                this.setTimeOfDay(l - l % 24000L);
            }
            this.wakeSleepingPlayers();
            if (this.getGameRules().getBoolean(GameRules.DO_WEATHER_CYCLE) && this.isRaining()) {
                this.resetWeather();
            }
        }
        this.calculateAmbientDarkness();
        if (bl) {
            this.tickTime();
        }
        lv.swap("tickPending");
        if (!this.isDebugWorld() && bl) {
            l = this.getTime();
            lv.push("blockTicks");
            this.blockTickScheduler.tick(l, 65536, this::tickBlock);
            lv.swap("fluidTicks");
            this.fluidTickScheduler.tick(l, 65536, this::tickFluid);
            lv.pop();
        }
        lv.swap("raid");
        if (bl) {
            this.raidManager.tick();
        }
        lv.swap("chunkSource");
        this.getChunkManager().tick(shouldKeepTicking, true);
        lv.swap("blockEvents");
        if (bl) {
            this.processSyncedBlockEvents();
        }
        this.inBlockTick = false;
        lv.pop();
        boolean bl3 = bl2 = !this.players.isEmpty() || !this.getForcedChunks().isEmpty();
        if (bl2) {
            this.resetIdleTimeout();
        }
        if (bl2 || this.idleTimeout++ < 300) {
            lv.push("entities");
            if (this.enderDragonFight != null && bl) {
                lv.push("dragonFight");
                this.enderDragonFight.tick();
                lv.pop();
            }
            this.entityList.forEach(entity -> {
                if (entity.isRemoved()) {
                    return;
                }
                if (this.shouldCancelSpawn((Entity)entity)) {
                    entity.discard();
                    return;
                }
                if (lv2.shouldSkipTick((Entity)entity)) {
                    return;
                }
                lv.push("checkDespawn");
                entity.checkDespawn();
                lv.pop();
                if (!this.chunkManager.chunkLoadingManager.getTicketManager().shouldTickEntities(entity.getChunkPos().toLong())) {
                    return;
                }
                Entity lv = entity.getVehicle();
                if (lv != null) {
                    if (lv.isRemoved() || !lv.hasPassenger((Entity)entity)) {
                        entity.stopRiding();
                    } else {
                        return;
                    }
                }
                lv.push("tick");
                this.tickEntity(this::tickEntity, entity);
                lv.pop();
            });
            lv.pop();
            this.tickBlockEntities();
        }
        lv.push("entityManagement");
        this.entityManager.tick();
        lv.pop();
    }

    @Override
    public boolean shouldTickBlocksInChunk(long chunkPos) {
        return this.chunkManager.chunkLoadingManager.getTicketManager().shouldTickBlocks(chunkPos);
    }

    protected void tickTime() {
        if (!this.shouldTickTime) {
            return;
        }
        long l = this.properties.getTime() + 1L;
        this.worldProperties.setTime(l);
        this.worldProperties.getScheduledEvents().processEvents(this.server, l);
        if (this.properties.getGameRules().getBoolean(GameRules.DO_DAYLIGHT_CYCLE)) {
            this.setTimeOfDay(this.properties.getTimeOfDay() + 1L);
        }
    }

    public void setTimeOfDay(long timeOfDay) {
        this.worldProperties.setTimeOfDay(timeOfDay);
    }

    public void tickSpawners(boolean spawnMonsters, boolean spawnAnimals) {
        for (SpecialSpawner lv : this.spawners) {
            lv.spawn(this, spawnMonsters, spawnAnimals);
        }
    }

    private boolean shouldCancelSpawn(Entity entity) {
        if (!this.server.shouldSpawnAnimals() && (entity instanceof AnimalEntity || entity instanceof WaterCreatureEntity)) {
            return true;
        }
        return !this.server.shouldSpawnNpcs() && entity instanceof Npc;
    }

    private void wakeSleepingPlayers() {
        this.sleepManager.clearSleeping();
        this.players.stream().filter(LivingEntity::isSleeping).collect(Collectors.toList()).forEach(player -> player.wakeUp(false, false));
    }

    public void tickChunk(WorldChunk chunk, int randomTickSpeed) {
        BlockPos lv3;
        ChunkPos lv = chunk.getPos();
        boolean bl = this.isRaining();
        int j = lv.getStartX();
        int k = lv.getStartZ();
        Profiler lv2 = this.getProfiler();
        lv2.push("thunder");
        if (bl && this.isThundering() && this.random.nextInt(100000) == 0 && this.hasRain(lv3 = this.getLightningPos(this.getRandomPosInChunk(j, 0, k, 15)))) {
            LightningEntity lv6;
            SkeletonHorseEntity lv5;
            boolean bl2;
            LocalDifficulty lv4 = this.getLocalDifficulty(lv3);
            boolean bl3 = bl2 = this.getGameRules().getBoolean(GameRules.DO_MOB_SPAWNING) && this.random.nextDouble() < (double)lv4.getLocalDifficulty() * 0.01 && !this.getBlockState(lv3.down()).isOf(Blocks.LIGHTNING_ROD);
            if (bl2 && (lv5 = EntityType.SKELETON_HORSE.create(this)) != null) {
                lv5.setTrapped(true);
                lv5.setBreedingAge(0);
                lv5.setPosition(lv3.getX(), lv3.getY(), lv3.getZ());
                this.spawnEntity(lv5);
            }
            if ((lv6 = EntityType.LIGHTNING_BOLT.create(this)) != null) {
                lv6.refreshPositionAfterTeleport(Vec3d.ofBottomCenter(lv3));
                lv6.setCosmetic(bl2);
                this.spawnEntity(lv6);
            }
        }
        lv2.swap("iceandsnow");
        for (int l = 0; l < randomTickSpeed; ++l) {
            if (this.random.nextInt(48) != 0) continue;
            this.tickIceAndSnow(this.getRandomPosInChunk(j, 0, k, 15));
        }
        lv2.swap("tickBlocks");
        if (randomTickSpeed > 0) {
            ChunkSection[] lvs = chunk.getSectionArray();
            for (int m = 0; m < lvs.length; ++m) {
                ChunkSection lv7 = lvs[m];
                if (!lv7.hasRandomTicks()) continue;
                int n = chunk.sectionIndexToCoord(m);
                int o = ChunkSectionPos.getBlockCoord(n);
                for (int p = 0; p < randomTickSpeed; ++p) {
                    FluidState lv10;
                    BlockPos lv8 = this.getRandomPosInChunk(j, o, k, 15);
                    lv2.push("randomTick");
                    BlockState lv9 = lv7.getBlockState(lv8.getX() - j, lv8.getY() - o, lv8.getZ() - k);
                    if (lv9.hasRandomTicks()) {
                        lv9.randomTick(this, lv8, this.random);
                    }
                    if ((lv10 = lv9.getFluidState()).hasRandomTicks()) {
                        lv10.onRandomTick(this, lv8, this.random);
                    }
                    lv2.pop();
                }
            }
        }
        lv2.pop();
    }

    @VisibleForTesting
    public void tickIceAndSnow(BlockPos pos) {
        BlockPos lv = this.getTopPosition(Heightmap.Type.MOTION_BLOCKING, pos);
        BlockPos lv2 = lv.down();
        Biome lv3 = this.getBiome(lv).value();
        if (lv3.canSetIce(this, lv2)) {
            this.setBlockState(lv2, Blocks.ICE.getDefaultState());
        }
        if (this.isRaining()) {
            Biome.Precipitation lv6;
            int i = this.getGameRules().getInt(GameRules.SNOW_ACCUMULATION_HEIGHT);
            if (i > 0 && lv3.canSetSnow(this, lv)) {
                BlockState lv4 = this.getBlockState(lv);
                if (lv4.isOf(Blocks.SNOW)) {
                    int j = lv4.get(SnowBlock.LAYERS);
                    if (j < Math.min(i, 8)) {
                        BlockState lv5 = (BlockState)lv4.with(SnowBlock.LAYERS, j + 1);
                        Block.pushEntitiesUpBeforeBlockChange(lv4, lv5, this, lv);
                        this.setBlockState(lv, lv5);
                    }
                } else {
                    this.setBlockState(lv, Blocks.SNOW.getDefaultState());
                }
            }
            if ((lv6 = lv3.getPrecipitation(lv2)) != Biome.Precipitation.NONE) {
                BlockState lv7 = this.getBlockState(lv2);
                lv7.getBlock().precipitationTick(lv7, this, lv2, lv6);
            }
        }
    }

    private Optional<BlockPos> getLightningRodPos(BlockPos pos) {
        Optional<BlockPos> optional = this.getPointOfInterestStorage().getNearestPosition(poiType -> poiType.matchesKey(PointOfInterestTypes.LIGHTNING_ROD), innerPos -> innerPos.getY() == this.getTopY(Heightmap.Type.WORLD_SURFACE, innerPos.getX(), innerPos.getZ()) - 1, pos, 128, PointOfInterestStorage.OccupationStatus.ANY);
        return optional.map(innerPos -> innerPos.up(1));
    }

    protected BlockPos getLightningPos(BlockPos pos) {
        BlockPos lv = this.getTopPosition(Heightmap.Type.MOTION_BLOCKING, pos);
        Optional<BlockPos> optional = this.getLightningRodPos(lv);
        if (optional.isPresent()) {
            return optional.get();
        }
        Box lv2 = Box.enclosing(lv, new BlockPos(lv.withY(this.getTopY()))).expand(3.0);
        List<LivingEntity> list = this.getEntitiesByClass(LivingEntity.class, lv2, entity -> entity != null && entity.isAlive() && this.isSkyVisible(entity.getBlockPos()));
        if (!list.isEmpty()) {
            return list.get(this.random.nextInt(list.size())).getBlockPos();
        }
        if (lv.getY() == this.getBottomY() - 1) {
            lv = lv.up(2);
        }
        return lv;
    }

    public boolean isInBlockTick() {
        return this.inBlockTick;
    }

    public boolean isSleepingEnabled() {
        return this.getGameRules().getInt(GameRules.PLAYERS_SLEEPING_PERCENTAGE) <= 100;
    }

    private void sendSleepingStatus() {
        if (!this.isSleepingEnabled()) {
            return;
        }
        if (this.getServer().isSingleplayer() && !this.getServer().isRemote()) {
            return;
        }
        int i = this.getGameRules().getInt(GameRules.PLAYERS_SLEEPING_PERCENTAGE);
        MutableText lv = this.sleepManager.canSkipNight(i) ? Text.translatable("sleep.skipping_night") : Text.translatable("sleep.players_sleeping", this.sleepManager.getSleeping(), this.sleepManager.getNightSkippingRequirement(i));
        for (ServerPlayerEntity lv2 : this.players) {
            lv2.sendMessage(lv, true);
        }
    }

    public void updateSleepingPlayers() {
        if (!this.players.isEmpty() && this.sleepManager.update(this.players)) {
            this.sendSleepingStatus();
        }
    }

    @Override
    public ServerScoreboard getScoreboard() {
        return this.server.getScoreboard();
    }

    private void tickWeather() {
        boolean bl = this.isRaining();
        if (this.getDimension().hasSkyLight()) {
            if (this.getGameRules().getBoolean(GameRules.DO_WEATHER_CYCLE)) {
                int i = this.worldProperties.getClearWeatherTime();
                int j = this.worldProperties.getThunderTime();
                int k = this.worldProperties.getRainTime();
                boolean bl2 = this.properties.isThundering();
                boolean bl3 = this.properties.isRaining();
                if (i > 0) {
                    --i;
                    j = bl2 ? 0 : 1;
                    k = bl3 ? 0 : 1;
                    bl2 = false;
                    bl3 = false;
                } else {
                    if (j > 0) {
                        if (--j == 0) {
                            bl2 = !bl2;
                        }
                    } else {
                        j = bl2 ? THUNDER_WEATHER_DURATION_PROVIDER.get(this.random) : CLEAR_THUNDER_WEATHER_DURATION_PROVIDER.get(this.random);
                    }
                    if (k > 0) {
                        if (--k == 0) {
                            bl3 = !bl3;
                        }
                    } else {
                        k = bl3 ? RAIN_WEATHER_DURATION_PROVIDER.get(this.random) : CLEAR_WEATHER_DURATION_PROVIDER.get(this.random);
                    }
                }
                this.worldProperties.setThunderTime(j);
                this.worldProperties.setRainTime(k);
                this.worldProperties.setClearWeatherTime(i);
                this.worldProperties.setThundering(bl2);
                this.worldProperties.setRaining(bl3);
            }
            this.thunderGradientPrev = this.thunderGradient;
            this.thunderGradient = this.properties.isThundering() ? (this.thunderGradient += 0.01f) : (this.thunderGradient -= 0.01f);
            this.thunderGradient = MathHelper.clamp(this.thunderGradient, 0.0f, 1.0f);
            this.rainGradientPrev = this.rainGradient;
            this.rainGradient = this.properties.isRaining() ? (this.rainGradient += 0.01f) : (this.rainGradient -= 0.01f);
            this.rainGradient = MathHelper.clamp(this.rainGradient, 0.0f, 1.0f);
        }
        if (this.rainGradientPrev != this.rainGradient) {
            this.server.getPlayerManager().sendToDimension(new GameStateChangeS2CPacket(GameStateChangeS2CPacket.RAIN_GRADIENT_CHANGED, this.rainGradient), this.getRegistryKey());
        }
        if (this.thunderGradientPrev != this.thunderGradient) {
            this.server.getPlayerManager().sendToDimension(new GameStateChangeS2CPacket(GameStateChangeS2CPacket.THUNDER_GRADIENT_CHANGED, this.thunderGradient), this.getRegistryKey());
        }
        if (bl != this.isRaining()) {
            if (bl) {
                this.server.getPlayerManager().sendToAll(new GameStateChangeS2CPacket(GameStateChangeS2CPacket.RAIN_STOPPED, GameStateChangeS2CPacket.field_33328));
            } else {
                this.server.getPlayerManager().sendToAll(new GameStateChangeS2CPacket(GameStateChangeS2CPacket.RAIN_STARTED, GameStateChangeS2CPacket.field_33328));
            }
            this.server.getPlayerManager().sendToAll(new GameStateChangeS2CPacket(GameStateChangeS2CPacket.RAIN_GRADIENT_CHANGED, this.rainGradient));
            this.server.getPlayerManager().sendToAll(new GameStateChangeS2CPacket(GameStateChangeS2CPacket.THUNDER_GRADIENT_CHANGED, this.thunderGradient));
        }
    }

    @VisibleForTesting
    public void resetWeather() {
        this.worldProperties.setRainTime(0);
        this.worldProperties.setRaining(false);
        this.worldProperties.setThunderTime(0);
        this.worldProperties.setThundering(false);
    }

    public void resetIdleTimeout() {
        this.idleTimeout = 0;
    }

    private void tickFluid(BlockPos pos, Fluid fluid) {
        FluidState lv = this.getFluidState(pos);
        if (lv.isOf(fluid)) {
            lv.onScheduledTick(this, pos);
        }
    }

    private void tickBlock(BlockPos pos, Block block) {
        BlockState lv = this.getBlockState(pos);
        if (lv.isOf(block)) {
            lv.scheduledTick(this, pos, this.random);
        }
    }

    public void tickEntity(Entity entity) {
        entity.resetPosition();
        Profiler lv = this.getProfiler();
        ++entity.age;
        this.getProfiler().push(() -> Registries.ENTITY_TYPE.getId(entity.getType()).toString());
        lv.visit("tickNonPassenger");
        entity.tick();
        this.getProfiler().pop();
        for (Entity lv2 : entity.getPassengerList()) {
            this.tickPassenger(entity, lv2);
        }
    }

    private void tickPassenger(Entity vehicle, Entity passenger) {
        if (passenger.isRemoved() || passenger.getVehicle() != vehicle) {
            passenger.stopRiding();
            return;
        }
        if (!(passenger instanceof PlayerEntity) && !this.entityList.has(passenger)) {
            return;
        }
        passenger.resetPosition();
        ++passenger.age;
        Profiler lv = this.getProfiler();
        lv.push(() -> Registries.ENTITY_TYPE.getId(passenger.getType()).toString());
        lv.visit("tickPassenger");
        passenger.tickRiding();
        lv.pop();
        for (Entity lv2 : passenger.getPassengerList()) {
            this.tickPassenger(passenger, lv2);
        }
    }

    @Override
    public boolean canPlayerModifyAt(PlayerEntity player, BlockPos pos) {
        return !this.server.isSpawnProtected(this, pos, player) && this.getWorldBorder().contains(pos);
    }

    public void save(@Nullable ProgressListener progressListener, boolean flush, boolean savingDisabled) {
        ServerChunkManager lv = this.getChunkManager();
        if (savingDisabled) {
            return;
        }
        if (progressListener != null) {
            progressListener.setTitle(Text.translatable("menu.savingLevel"));
        }
        this.saveLevel();
        if (progressListener != null) {
            progressListener.setTask(Text.translatable("menu.savingChunks"));
        }
        lv.save(flush);
        if (flush) {
            this.entityManager.flush();
        } else {
            this.entityManager.save();
        }
    }

    private void saveLevel() {
        if (this.enderDragonFight != null) {
            this.server.getSaveProperties().setDragonFight(this.enderDragonFight.toData());
        }
        this.getChunkManager().getPersistentStateManager().save();
    }

    public <T extends Entity> List<? extends T> getEntitiesByType(TypeFilter<Entity, T> filter, Predicate<? super T> predicate) {
        ArrayList list = Lists.newArrayList();
        this.collectEntitiesByType(filter, predicate, list);
        return list;
    }

    public <T extends Entity> void collectEntitiesByType(TypeFilter<Entity, T> filter, Predicate<? super T> predicate, List<? super T> result) {
        this.collectEntitiesByType(filter, predicate, result, Integer.MAX_VALUE);
    }

    public <T extends Entity> void collectEntitiesByType(TypeFilter<Entity, T> filter, Predicate<? super T> predicate, List<? super T> result, int limit) {
        this.getEntityLookup().forEach(filter, entity -> {
            if (predicate.test(entity)) {
                result.add((Object)entity);
                if (result.size() >= limit) {
                    return LazyIterationConsumer.NextIteration.ABORT;
                }
            }
            return LazyIterationConsumer.NextIteration.CONTINUE;
        });
    }

    public List<? extends EnderDragonEntity> getAliveEnderDragons() {
        return this.getEntitiesByType(EntityType.ENDER_DRAGON, LivingEntity::isAlive);
    }

    public List<ServerPlayerEntity> getPlayers(Predicate<? super ServerPlayerEntity> predicate) {
        return this.getPlayers(predicate, Integer.MAX_VALUE);
    }

    public List<ServerPlayerEntity> getPlayers(Predicate<? super ServerPlayerEntity> predicate, int limit) {
        ArrayList<ServerPlayerEntity> list = Lists.newArrayList();
        for (ServerPlayerEntity lv : this.players) {
            if (!predicate.test(lv)) continue;
            list.add(lv);
            if (list.size() < limit) continue;
            return list;
        }
        return list;
    }

    @Nullable
    public ServerPlayerEntity getRandomAlivePlayer() {
        List<ServerPlayerEntity> list = this.getPlayers(LivingEntity::isAlive);
        if (list.isEmpty()) {
            return null;
        }
        return list.get(this.random.nextInt(list.size()));
    }

    @Override
    public boolean spawnEntity(Entity entity) {
        return this.addEntity(entity);
    }

    public boolean tryLoadEntity(Entity entity) {
        return this.addEntity(entity);
    }

    public void onDimensionChanged(Entity entity) {
        if (entity instanceof ServerPlayerEntity) {
            ServerPlayerEntity lv = (ServerPlayerEntity)entity;
            this.addPlayer(lv);
        } else {
            this.addEntity(entity);
        }
    }

    public void onPlayerConnected(ServerPlayerEntity player) {
        this.addPlayer(player);
    }

    public void onPlayerRespawned(ServerPlayerEntity player) {
        this.addPlayer(player);
    }

    private void addPlayer(ServerPlayerEntity player) {
        Entity lv = this.getEntityLookup().get(player.getUuid());
        if (lv != null) {
            LOGGER.warn("Force-added player with duplicate UUID {}", (Object)player.getUuid());
            lv.detach();
            this.removePlayer((ServerPlayerEntity)lv, Entity.RemovalReason.DISCARDED);
        }
        this.entityManager.addEntity(player);
    }

    private boolean addEntity(Entity entity) {
        if (entity.isRemoved()) {
            LOGGER.warn("Tried to add entity {} but it was marked as removed already", (Object)EntityType.getId(entity.getType()));
            return false;
        }
        return this.entityManager.addEntity(entity);
    }

    public boolean spawnNewEntityAndPassengers(Entity entity) {
        if (entity.streamSelfAndPassengers().map(Entity::getUuid).anyMatch(this.entityManager::has)) {
            return false;
        }
        this.spawnEntityAndPassengers(entity);
        return true;
    }

    public void unloadEntities(WorldChunk chunk) {
        chunk.clear();
        chunk.removeChunkTickSchedulers(this);
    }

    public void removePlayer(ServerPlayerEntity player, Entity.RemovalReason reason) {
        player.remove(reason);
    }

    @Override
    public void setBlockBreakingInfo(int entityId, BlockPos pos, int progress) {
        for (ServerPlayerEntity lv : this.server.getPlayerManager().getPlayerList()) {
            double f;
            double e;
            double d;
            if (lv == null || lv.getWorld() != this || lv.getId() == entityId || !((d = (double)pos.getX() - lv.getX()) * d + (e = (double)pos.getY() - lv.getY()) * e + (f = (double)pos.getZ() - lv.getZ()) * f < 1024.0)) continue;
            lv.networkHandler.sendPacket(new BlockBreakingProgressS2CPacket(entityId, pos, progress));
        }
    }

    @Override
    public void playSound(@Nullable PlayerEntity source, double x, double y, double z, RegistryEntry<SoundEvent> sound, SoundCategory category, float volume, float pitch, long seed) {
        this.server.getPlayerManager().sendToAround(source, x, y, z, sound.value().getDistanceToTravel(volume), this.getRegistryKey(), new PlaySoundS2CPacket(sound, category, x, y, z, volume, pitch, seed));
    }

    @Override
    public void playSoundFromEntity(@Nullable PlayerEntity source, Entity entity, RegistryEntry<SoundEvent> sound, SoundCategory category, float volume, float pitch, long seed) {
        this.server.getPlayerManager().sendToAround(source, entity.getX(), entity.getY(), entity.getZ(), sound.value().getDistanceToTravel(volume), this.getRegistryKey(), new PlaySoundFromEntityS2CPacket(sound, category, entity, volume, pitch, seed));
    }

    @Override
    public void syncGlobalEvent(int eventId, BlockPos pos, int data) {
        if (this.getGameRules().getBoolean(GameRules.GLOBAL_SOUND_EVENTS)) {
            this.server.getPlayerManager().sendToAll(new WorldEventS2CPacket(eventId, pos, data, true));
        } else {
            this.syncWorldEvent(null, eventId, pos, data);
        }
    }

    @Override
    public void syncWorldEvent(@Nullable PlayerEntity player, int eventId, BlockPos pos, int data) {
        this.server.getPlayerManager().sendToAround(player, pos.getX(), pos.getY(), pos.getZ(), 64.0, this.getRegistryKey(), new WorldEventS2CPacket(eventId, pos, data, false));
    }

    public int getLogicalHeight() {
        return this.getDimension().logicalHeight();
    }

    @Override
    public void emitGameEvent(RegistryEntry<GameEvent> event, Vec3d emitterPos, GameEvent.Emitter emitter) {
        this.gameEventDispatchManager.dispatch(event, emitterPos, emitter);
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @Override
    public void updateListeners(BlockPos pos, BlockState oldState, BlockState newState, int flags) {
        if (this.duringListenerUpdate) {
            String string = "recursive call to sendBlockUpdated";
            Util.error("recursive call to sendBlockUpdated", new IllegalStateException("recursive call to sendBlockUpdated"));
        }
        this.getChunkManager().markForUpdate(pos);
        this.pathNodeTypeCache.invalidate(pos);
        VoxelShape lv = oldState.getCollisionShape(this, pos);
        VoxelShape lv2 = newState.getCollisionShape(this, pos);
        if (!VoxelShapes.matchesAnywhere(lv, lv2, BooleanBiFunction.NOT_SAME)) {
            return;
        }
        ObjectArrayList list = new ObjectArrayList();
        for (MobEntity lv3 : this.loadedMobs) {
            EntityNavigation lv4 = lv3.getNavigation();
            if (!lv4.shouldRecalculatePath(pos)) continue;
            list.add(lv4);
        }
        try {
            this.duringListenerUpdate = true;
            for (EntityNavigation lv5 : list) {
                lv5.recalculatePath();
            }
        } finally {
            this.duringListenerUpdate = false;
        }
    }

    @Override
    public void updateNeighborsAlways(BlockPos pos, Block sourceBlock) {
        this.neighborUpdater.updateNeighbors(pos, sourceBlock, null);
    }

    @Override
    public void updateNeighborsExcept(BlockPos pos, Block sourceBlock, Direction direction) {
        this.neighborUpdater.updateNeighbors(pos, sourceBlock, direction);
    }

    @Override
    public void updateNeighbor(BlockPos pos, Block sourceBlock, BlockPos sourcePos) {
        this.neighborUpdater.updateNeighbor(pos, sourceBlock, sourcePos);
    }

    @Override
    public void updateNeighbor(BlockState state, BlockPos pos, Block sourceBlock, BlockPos sourcePos, boolean notify) {
        this.neighborUpdater.updateNeighbor(state, pos, sourceBlock, sourcePos, notify);
    }

    @Override
    public void sendEntityStatus(Entity entity, byte status) {
        this.getChunkManager().sendToNearbyPlayers(entity, new EntityStatusS2CPacket(entity, status));
    }

    @Override
    public void sendEntityDamage(Entity entity, DamageSource damageSource) {
        this.getChunkManager().sendToNearbyPlayers(entity, new EntityDamageS2CPacket(entity, damageSource));
    }

    @Override
    public ServerChunkManager getChunkManager() {
        return this.chunkManager;
    }

    @Override
    public Explosion createExplosion(@Nullable Entity entity, @Nullable DamageSource damageSource, @Nullable ExplosionBehavior behavior, double x, double y, double z, float power, boolean createFire, World.ExplosionSourceType explosionSourceType, ParticleEffect particle, ParticleEffect emitterParticle, RegistryEntry<SoundEvent> soundEvent) {
        Explosion lv = this.createExplosion(entity, damageSource, behavior, x, y, z, power, createFire, explosionSourceType, false, particle, emitterParticle, soundEvent);
        if (!lv.shouldDestroy()) {
            lv.clearAffectedBlocks();
        }
        for (ServerPlayerEntity lv2 : this.players) {
            if (!(lv2.squaredDistanceTo(x, y, z) < 4096.0)) continue;
            lv2.networkHandler.sendPacket(new ExplosionS2CPacket(x, y, z, power, lv.getAffectedBlocks(), lv.getAffectedPlayers().get(lv2), lv.getDestructionType(), lv.getParticle(), lv.getEmitterParticle(), lv.getSoundEvent()));
        }
        return lv;
    }

    @Override
    public void addSyncedBlockEvent(BlockPos pos, Block block, int type, int data) {
        this.syncedBlockEventQueue.add(new BlockEvent(pos, block, type, data));
    }

    private void processSyncedBlockEvents() {
        this.blockEventQueue.clear();
        while (!this.syncedBlockEventQueue.isEmpty()) {
            BlockEvent lv = this.syncedBlockEventQueue.removeFirst();
            if (this.shouldTickBlockPos(lv.pos())) {
                if (!this.processBlockEvent(lv)) continue;
                this.server.getPlayerManager().sendToAround(null, lv.pos().getX(), lv.pos().getY(), lv.pos().getZ(), 64.0, this.getRegistryKey(), new BlockEventS2CPacket(lv.pos(), lv.block(), lv.type(), lv.data()));
                continue;
            }
            this.blockEventQueue.add(lv);
        }
        this.syncedBlockEventQueue.addAll((Collection<BlockEvent>)this.blockEventQueue);
    }

    private boolean processBlockEvent(BlockEvent event) {
        BlockState lv = this.getBlockState(event.pos());
        if (lv.isOf(event.block())) {
            return lv.onSyncedBlockEvent(this, event.pos(), event.type(), event.data());
        }
        return false;
    }

    public WorldTickScheduler<Block> getBlockTickScheduler() {
        return this.blockTickScheduler;
    }

    public WorldTickScheduler<Fluid> getFluidTickScheduler() {
        return this.fluidTickScheduler;
    }

    @Override
    @NotNull
    public MinecraftServer getServer() {
        return this.server;
    }

    public PortalForcer getPortalForcer() {
        return this.portalForcer;
    }

    public StructureTemplateManager getStructureTemplateManager() {
        return this.server.getStructureTemplateManager();
    }

    public <T extends ParticleEffect> int spawnParticles(T particle, double x, double y, double z, int count, double deltaX, double deltaY, double deltaZ, double speed) {
        ParticleS2CPacket lv = new ParticleS2CPacket(particle, false, x, y, z, (float)deltaX, (float)deltaY, (float)deltaZ, (float)speed, count);
        int l = 0;
        for (int m = 0; m < this.players.size(); ++m) {
            ServerPlayerEntity lv2 = this.players.get(m);
            if (!this.sendToPlayerIfNearby(lv2, false, x, y, z, lv)) continue;
            ++l;
        }
        return l;
    }

    public <T extends ParticleEffect> boolean spawnParticles(ServerPlayerEntity viewer, T particle, boolean force, double x, double y, double z, int count, double deltaX, double deltaY, double deltaZ, double speed) {
        ParticleS2CPacket lv = new ParticleS2CPacket(particle, force, x, y, z, (float)deltaX, (float)deltaY, (float)deltaZ, (float)speed, count);
        return this.sendToPlayerIfNearby(viewer, force, x, y, z, lv);
    }

    private boolean sendToPlayerIfNearby(ServerPlayerEntity player, boolean force, double x, double y, double z, Packet<?> packet) {
        if (player.getWorld() != this) {
            return false;
        }
        BlockPos lv = player.getBlockPos();
        if (lv.isWithinDistance(new Vec3d(x, y, z), force ? 512.0 : 32.0)) {
            player.networkHandler.sendPacket(packet);
            return true;
        }
        return false;
    }

    @Override
    @Nullable
    public Entity getEntityById(int id) {
        return this.getEntityLookup().get(id);
    }

    @Deprecated
    @Nullable
    public Entity getDragonPart(int id) {
        Entity lv = this.getEntityLookup().get(id);
        if (lv != null) {
            return lv;
        }
        return (Entity)this.dragonParts.get(id);
    }

    @Nullable
    public Entity getEntity(UUID uuid) {
        return this.getEntityLookup().get(uuid);
    }

    @Nullable
    public BlockPos locateStructure(TagKey<Structure> structureTag, BlockPos pos, int radius, boolean skipReferencedStructures) {
        if (!this.server.getSaveProperties().getGeneratorOptions().shouldGenerateStructures()) {
            return null;
        }
        Optional<RegistryEntryList.Named<Structure>> optional = this.getRegistryManager().get(RegistryKeys.STRUCTURE).getEntryList(structureTag);
        if (optional.isEmpty()) {
            return null;
        }
        Pair<BlockPos, RegistryEntry<Structure>> pair = this.getChunkManager().getChunkGenerator().locateStructure(this, (RegistryEntryList<Structure>)optional.get(), pos, radius, skipReferencedStructures);
        return pair != null ? pair.getFirst() : null;
    }

    @Nullable
    public Pair<BlockPos, RegistryEntry<Biome>> locateBiome(Predicate<RegistryEntry<Biome>> predicate, BlockPos pos, int radius, int horizontalBlockCheckInterval, int verticalBlockCheckInterval) {
        return this.getChunkManager().getChunkGenerator().getBiomeSource().locateBiome(pos, radius, horizontalBlockCheckInterval, verticalBlockCheckInterval, predicate, this.getChunkManager().getNoiseConfig().getMultiNoiseSampler(), this);
    }

    @Override
    public RecipeManager getRecipeManager() {
        return this.server.getRecipeManager();
    }

    @Override
    public TickManager getTickManager() {
        return this.server.getTickManager();
    }

    @Override
    public boolean isSavingDisabled() {
        return this.savingDisabled;
    }

    public PersistentStateManager getPersistentStateManager() {
        return this.getChunkManager().getPersistentStateManager();
    }

    @Override
    @Nullable
    public MapState getMapState(MapIdComponent id) {
        return this.getServer().getOverworld().getPersistentStateManager().get(MapState.getPersistentStateType(), id.asString());
    }

    @Override
    public void putMapState(MapIdComponent id, MapState state) {
        this.getServer().getOverworld().getPersistentStateManager().set(id.asString(), state);
    }

    @Override
    public MapIdComponent increaseAndGetMapId() {
        return this.getServer().getOverworld().getPersistentStateManager().getOrCreate(IdCountsState.getPersistentStateType(), "idcounts").increaseAndGetMapId();
    }

    public void setSpawnPos(BlockPos pos, float angle) {
        int i;
        BlockPos lv = this.properties.getSpawnPos();
        float g = this.properties.getSpawnAngle();
        if (!lv.equals(pos) || g != angle) {
            this.properties.setSpawnPos(pos, angle);
            this.getServer().getPlayerManager().sendToAll(new PlayerSpawnPositionS2CPacket(pos, angle));
        }
        if (this.spawnChunkRadius > 1) {
            this.getChunkManager().removeTicket(ChunkTicketType.START, new ChunkPos(lv), this.spawnChunkRadius, Unit.INSTANCE);
        }
        if ((i = this.getGameRules().getInt(GameRules.SPAWN_CHUNK_RADIUS) + 1) > 1) {
            this.getChunkManager().addTicket(ChunkTicketType.START, new ChunkPos(pos), i, Unit.INSTANCE);
        }
        this.spawnChunkRadius = i;
    }

    public LongSet getForcedChunks() {
        ForcedChunkState lv = this.getPersistentStateManager().get(ForcedChunkState.getPersistentStateType(), "chunks");
        return lv != null ? LongSets.unmodifiable(lv.getChunks()) : LongSets.EMPTY_SET;
    }

    public boolean setChunkForced(int x, int z, boolean forced) {
        boolean bl2;
        ForcedChunkState lv = this.getPersistentStateManager().getOrCreate(ForcedChunkState.getPersistentStateType(), "chunks");
        ChunkPos lv2 = new ChunkPos(x, z);
        long l = lv2.toLong();
        if (forced) {
            bl2 = lv.getChunks().add(l);
            if (bl2) {
                this.getChunk(x, z);
            }
        } else {
            bl2 = lv.getChunks().remove(l);
        }
        lv.setDirty(bl2);
        if (bl2) {
            this.getChunkManager().setChunkForced(lv2, forced);
        }
        return bl2;
    }

    public List<ServerPlayerEntity> getPlayers() {
        return this.players;
    }

    @Override
    public void onBlockChanged(BlockPos pos, BlockState oldBlock, BlockState newBlock) {
        Optional<RegistryEntry<PointOfInterestType>> optional2;
        Optional<RegistryEntry<PointOfInterestType>> optional = PointOfInterestTypes.getTypeForState(oldBlock);
        if (Objects.equals(optional, optional2 = PointOfInterestTypes.getTypeForState(newBlock))) {
            return;
        }
        BlockPos lv = pos.toImmutable();
        optional.ifPresent(oldPoiType -> this.getServer().execute(() -> {
            this.getPointOfInterestStorage().remove(lv);
            DebugInfoSender.sendPoiRemoval(this, lv);
        }));
        optional2.ifPresent(newPoiType -> this.getServer().execute(() -> {
            this.getPointOfInterestStorage().add(lv, (RegistryEntry<PointOfInterestType>)newPoiType);
            DebugInfoSender.sendPoiAddition(this, lv);
        }));
    }

    public PointOfInterestStorage getPointOfInterestStorage() {
        return this.getChunkManager().getPointOfInterestStorage();
    }

    public boolean isNearOccupiedPointOfInterest(BlockPos pos) {
        return this.isNearOccupiedPointOfInterest(pos, 1);
    }

    public boolean isNearOccupiedPointOfInterest(ChunkSectionPos sectionPos) {
        return this.isNearOccupiedPointOfInterest(sectionPos.getCenterPos());
    }

    public boolean isNearOccupiedPointOfInterest(BlockPos pos, int maxDistance) {
        if (maxDistance > 6) {
            return false;
        }
        return this.getOccupiedPointOfInterestDistance(ChunkSectionPos.from(pos)) <= maxDistance;
    }

    public int getOccupiedPointOfInterestDistance(ChunkSectionPos pos) {
        return this.getPointOfInterestStorage().getDistanceFromNearestOccupied(pos);
    }

    public RaidManager getRaidManager() {
        return this.raidManager;
    }

    @Nullable
    public Raid getRaidAt(BlockPos pos) {
        return this.raidManager.getRaidAt(pos, 9216);
    }

    public boolean hasRaidAt(BlockPos pos) {
        return this.getRaidAt(pos) != null;
    }

    public void handleInteraction(EntityInteraction interaction, Entity entity, InteractionObserver observer) {
        observer.onInteractionWith(interaction, entity);
    }

    public void dump(Path path) throws IOException {
        ServerChunkLoadingManager lv = this.getChunkManager().chunkLoadingManager;
        try (BufferedWriter writer = Files.newBufferedWriter(path.resolve("stats.txt"), new OpenOption[0]);){
            writer.write(String.format(Locale.ROOT, "spawning_chunks: %d\n", lv.getTicketManager().getTickedChunkCount()));
            SpawnHelper.Info lv2 = this.getChunkManager().getSpawnInfo();
            if (lv2 != null) {
                for (Object2IntMap.Entry entry : lv2.getGroupToCount().object2IntEntrySet()) {
                    writer.write(String.format(Locale.ROOT, "spawn_count.%s: %d\n", ((SpawnGroup)entry.getKey()).getName(), entry.getIntValue()));
                }
            }
            writer.write(String.format(Locale.ROOT, "entities: %s\n", this.entityManager.getDebugString()));
            writer.write(String.format(Locale.ROOT, "block_entity_tickers: %d\n", this.blockEntityTickers.size()));
            writer.write(String.format(Locale.ROOT, "block_ticks: %d\n", ((WorldTickScheduler)this.getBlockTickScheduler()).getTickCount()));
            writer.write(String.format(Locale.ROOT, "fluid_ticks: %d\n", ((WorldTickScheduler)this.getFluidTickScheduler()).getTickCount()));
            writer.write("distance_manager: " + lv.getTicketManager().toDumpString() + "\n");
            writer.write(String.format(Locale.ROOT, "pending_tasks: %d\n", this.getChunkManager().getPendingTasks()));
        }
        CrashReport lv3 = new CrashReport("Level dump", new Exception("dummy"));
        this.addDetailsToCrashReport(lv3);
        try (BufferedWriter writer2 = Files.newBufferedWriter(path.resolve("example_crash.txt"), new OpenOption[0]);){
            writer2.write(lv3.asString(ReportType.MINECRAFT_TEST_REPORT));
        }
        Path path2 = path.resolve("chunks.csv");
        try (BufferedWriter writer3 = Files.newBufferedWriter(path2, new OpenOption[0]);){
            lv.dump(writer3);
        }
        Path path3 = path.resolve("entity_chunks.csv");
        try (BufferedWriter bufferedWriter = Files.newBufferedWriter(path3, new OpenOption[0]);){
            this.entityManager.dump(bufferedWriter);
        }
        Path path4 = path.resolve("entities.csv");
        try (BufferedWriter writer5 = Files.newBufferedWriter(path4, new OpenOption[0]);){
            ServerWorld.dumpEntities(writer5, this.getEntityLookup().iterate());
        }
        Path path5 = path.resolve("block_entities.csv");
        try (BufferedWriter writer6 = Files.newBufferedWriter(path5, new OpenOption[0]);){
            this.dumpBlockEntities(writer6);
        }
    }

    private static void dumpEntities(Writer writer, Iterable<Entity> entities) throws IOException {
        CsvWriter lv = CsvWriter.makeHeader().addColumn("x").addColumn("y").addColumn("z").addColumn("uuid").addColumn("type").addColumn("alive").addColumn("display_name").addColumn("custom_name").startBody(writer);
        for (Entity lv2 : entities) {
            Text lv3 = lv2.getCustomName();
            Text lv4 = lv2.getDisplayName();
            lv.printRow(lv2.getX(), lv2.getY(), lv2.getZ(), lv2.getUuid(), Registries.ENTITY_TYPE.getId(lv2.getType()), lv2.isAlive(), lv4.getString(), lv3 != null ? lv3.getString() : null);
        }
    }

    private void dumpBlockEntities(Writer writer) throws IOException {
        CsvWriter lv = CsvWriter.makeHeader().addColumn("x").addColumn("y").addColumn("z").addColumn("type").startBody(writer);
        for (BlockEntityTickInvoker lv2 : this.blockEntityTickers) {
            BlockPos lv3 = lv2.getPos();
            lv.printRow(lv3.getX(), lv3.getY(), lv3.getZ(), lv2.getName());
        }
    }

    @VisibleForTesting
    public void clearUpdatesInArea(BlockBox box) {
        this.syncedBlockEventQueue.removeIf(event -> box.contains(event.pos()));
    }

    @Override
    public void updateNeighbors(BlockPos pos, Block block) {
        if (!this.isDebugWorld()) {
            this.updateNeighborsAlways(pos, block);
        }
    }

    @Override
    public float getBrightness(Direction direction, boolean shaded) {
        return 1.0f;
    }

    public Iterable<Entity> iterateEntities() {
        return this.getEntityLookup().iterate();
    }

    public String toString() {
        return "ServerLevel[" + this.worldProperties.getLevelName() + "]";
    }

    public boolean isFlat() {
        return this.server.getSaveProperties().isFlatWorld();
    }

    @Override
    public long getSeed() {
        return this.server.getSaveProperties().getGeneratorOptions().getSeed();
    }

    @Nullable
    public EnderDragonFight getEnderDragonFight() {
        return this.enderDragonFight;
    }

    @Override
    public ServerWorld toServerWorld() {
        return this;
    }

    @VisibleForTesting
    public String getDebugString() {
        return String.format(Locale.ROOT, "players: %s, entities: %s [%s], block_entities: %d [%s], block_ticks: %d, fluid_ticks: %d, chunk_source: %s", this.players.size(), this.entityManager.getDebugString(), ServerWorld.getTopFive(this.entityManager.getLookup().iterate(), entity -> Registries.ENTITY_TYPE.getId(entity.getType()).toString()), this.blockEntityTickers.size(), ServerWorld.getTopFive(this.blockEntityTickers, BlockEntityTickInvoker::getName), ((WorldTickScheduler)this.getBlockTickScheduler()).getTickCount(), ((WorldTickScheduler)this.getFluidTickScheduler()).getTickCount(), this.asString());
    }

    private static <T> String getTopFive(Iterable<T> items, Function<T, String> classifier) {
        try {
            Object2IntOpenHashMap<String> object2IntOpenHashMap = new Object2IntOpenHashMap<String>();
            for (T object : items) {
                String string = classifier.apply(object);
                object2IntOpenHashMap.addTo(string, 1);
            }
            return object2IntOpenHashMap.object2IntEntrySet().stream().sorted(Comparator.comparing(Object2IntMap.Entry::getIntValue).reversed()).limit(5L).map(entry -> (String)entry.getKey() + ":" + entry.getIntValue()).collect(Collectors.joining(","));
        } catch (Exception exception) {
            return "";
        }
    }

    @Override
    protected EntityLookup<Entity> getEntityLookup() {
        return this.entityManager.getLookup();
    }

    public void loadEntities(Stream<Entity> entities) {
        this.entityManager.loadEntities(entities);
    }

    public void addEntities(Stream<Entity> entities) {
        this.entityManager.addEntities(entities);
    }

    public void disableTickSchedulers(WorldChunk chunk) {
        chunk.disableTickSchedulers(this.getLevelProperties().getTime());
    }

    public void cacheStructures(Chunk chunk) {
        this.server.execute(() -> this.structureLocator.cache(chunk.getPos(), chunk.getStructureStarts()));
    }

    public PathNodeTypeCache getPathNodeTypeCache() {
        return this.pathNodeTypeCache;
    }

    @Override
    public void close() throws IOException {
        super.close();
        this.entityManager.close();
    }

    @Override
    public String asString() {
        return "Chunks[S] W: " + this.chunkManager.getDebugString() + " E: " + this.entityManager.getDebugString();
    }

    public boolean isChunkLoaded(long chunkPos) {
        return this.entityManager.isLoaded(chunkPos);
    }

    private boolean isTickingFutureReady(long chunkPos) {
        return this.isChunkLoaded(chunkPos) && this.chunkManager.isTickingFutureReady(chunkPos);
    }

    public boolean shouldTickEntity(BlockPos pos) {
        return this.entityManager.shouldTick(pos) && this.chunkManager.chunkLoadingManager.getTicketManager().shouldTickEntities(ChunkPos.toLong(pos));
    }

    public boolean shouldTick(BlockPos pos) {
        return this.entityManager.shouldTick(pos);
    }

    public boolean shouldTick(ChunkPos pos) {
        return this.entityManager.shouldTick(pos);
    }

    @Override
    public FeatureSet getEnabledFeatures() {
        return this.server.getSaveProperties().getEnabledFeatures();
    }

    @Override
    public BrewingRecipeRegistry getBrewingRecipeRegistry() {
        return this.server.getBrewingRecipeRegistry();
    }

    public Random getOrCreateRandom(Identifier id) {
        return this.randomSequences.getOrCreate(id);
    }

    public RandomSequencesState getRandomSequences() {
        return this.randomSequences;
    }

    @Override
    public CrashReportSection addDetailsToCrashReport(CrashReport report) {
        CrashReportSection lv = super.addDetailsToCrashReport(report);
        lv.add("Loaded entity count", () -> String.valueOf(this.entityManager.getIndexSize()));
        return lv;
    }

    @Override
    public /* synthetic */ Scoreboard getScoreboard() {
        return this.getScoreboard();
    }

    @Override
    public /* synthetic */ ChunkManager getChunkManager() {
        return this.getChunkManager();
    }

    public /* synthetic */ QueryableTickScheduler getFluidTickScheduler() {
        return this.getFluidTickScheduler();
    }

    public /* synthetic */ QueryableTickScheduler getBlockTickScheduler() {
        return this.getBlockTickScheduler();
    }

    final class ServerEntityHandler
    implements EntityHandler<Entity> {
        ServerEntityHandler() {
        }

        @Override
        public void create(Entity arg) {
        }

        @Override
        public void destroy(Entity arg) {
            ServerWorld.this.getScoreboard().clearDeadEntity(arg);
        }

        @Override
        public void startTicking(Entity arg) {
            ServerWorld.this.entityList.add(arg);
        }

        @Override
        public void stopTicking(Entity arg) {
            ServerWorld.this.entityList.remove(arg);
        }

        @Override
        public void startTracking(Entity arg) {
            ServerWorld.this.getChunkManager().loadEntity(arg);
            if (arg instanceof ServerPlayerEntity) {
                ServerPlayerEntity lv = (ServerPlayerEntity)arg;
                ServerWorld.this.players.add(lv);
                ServerWorld.this.updateSleepingPlayers();
            }
            if (arg instanceof MobEntity) {
                MobEntity lv2 = (MobEntity)arg;
                if (ServerWorld.this.duringListenerUpdate) {
                    String string = "onTrackingStart called during navigation iteration";
                    Util.error("onTrackingStart called during navigation iteration", new IllegalStateException("onTrackingStart called during navigation iteration"));
                }
                ServerWorld.this.loadedMobs.add(lv2);
            }
            if (arg instanceof EnderDragonEntity) {
                EnderDragonEntity lv3 = (EnderDragonEntity)arg;
                for (EnderDragonPart lv4 : lv3.getBodyParts()) {
                    ServerWorld.this.dragonParts.put(lv4.getId(), lv4);
                }
            }
            arg.updateEventHandler(EntityGameEventHandler::onEntitySetPosCallback);
        }

        @Override
        public void stopTracking(Entity arg) {
            ServerWorld.this.getChunkManager().unloadEntity(arg);
            if (arg instanceof ServerPlayerEntity) {
                ServerPlayerEntity lv = (ServerPlayerEntity)arg;
                ServerWorld.this.players.remove(lv);
                ServerWorld.this.updateSleepingPlayers();
            }
            if (arg instanceof MobEntity) {
                MobEntity lv2 = (MobEntity)arg;
                if (ServerWorld.this.duringListenerUpdate) {
                    String string = "onTrackingStart called during navigation iteration";
                    Util.error("onTrackingStart called during navigation iteration", new IllegalStateException("onTrackingStart called during navigation iteration"));
                }
                ServerWorld.this.loadedMobs.remove(lv2);
            }
            if (arg instanceof EnderDragonEntity) {
                EnderDragonEntity lv3 = (EnderDragonEntity)arg;
                for (EnderDragonPart lv4 : lv3.getBodyParts()) {
                    ServerWorld.this.dragonParts.remove(lv4.getId());
                }
            }
            arg.updateEventHandler(EntityGameEventHandler::onEntityRemoval);
        }

        @Override
        public void updateLoadStatus(Entity arg) {
            arg.updateEventHandler(EntityGameEventHandler::onEntitySetPos);
        }

        @Override
        public /* synthetic */ void updateLoadStatus(Object entity) {
            this.updateLoadStatus((Entity)entity);
        }

        @Override
        public /* synthetic */ void stopTracking(Object entity) {
            this.stopTracking((Entity)entity);
        }

        @Override
        public /* synthetic */ void startTracking(Object entity) {
            this.startTracking((Entity)entity);
        }

        @Override
        public /* synthetic */ void startTicking(Object entity) {
            this.startTicking((Entity)entity);
        }

        @Override
        public /* synthetic */ void destroy(Object entity) {
            this.destroy((Entity)entity);
        }

        @Override
        public /* synthetic */ void create(Object entity) {
            this.create((Entity)entity);
        }
    }
}

