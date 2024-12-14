/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 * 
 * Could not load the following classes:
 *  org.jetbrains.annotations.Nullable
 */
package net.minecraft.server.world;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.Lists;
import com.mojang.datafixers.DataFixer;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.Executor;
import java.util.function.BooleanSupplier;
import java.util.function.Consumer;
import java.util.function.Supplier;
import net.minecraft.entity.Entity;
import net.minecraft.network.packet.Packet;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.WorldGenerationProgressListener;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ChunkHolder;
import net.minecraft.server.world.ChunkLevels;
import net.minecraft.server.world.ChunkTicketManager;
import net.minecraft.server.world.ChunkTicketType;
import net.minecraft.server.world.OptionalChunk;
import net.minecraft.server.world.ServerChunkLoadingManager;
import net.minecraft.server.world.ServerLightingProvider;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.structure.StructureTemplateManager;
import net.minecraft.util.Util;
import net.minecraft.util.annotation.Debug;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.ChunkSectionPos;
import net.minecraft.util.profiler.Profiler;
import net.minecraft.util.thread.ThreadExecutor;
import net.minecraft.world.BlockView;
import net.minecraft.world.GameRules;
import net.minecraft.world.LightType;
import net.minecraft.world.PersistentStateManager;
import net.minecraft.world.SpawnDensityCapper;
import net.minecraft.world.SpawnHelper;
import net.minecraft.world.World;
import net.minecraft.world.chunk.AbstractChunkHolder;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.ChunkManager;
import net.minecraft.world.chunk.ChunkStatus;
import net.minecraft.world.chunk.ChunkStatusChangeListener;
import net.minecraft.world.chunk.WorldChunk;
import net.minecraft.world.chunk.light.LightSourceView;
import net.minecraft.world.chunk.light.LightingProvider;
import net.minecraft.world.gen.chunk.ChunkGenerator;
import net.minecraft.world.gen.chunk.placement.StructurePlacementCalculator;
import net.minecraft.world.gen.noise.NoiseConfig;
import net.minecraft.world.level.storage.LevelStorage;
import net.minecraft.world.poi.PointOfInterestStorage;
import net.minecraft.world.storage.NbtScannable;
import org.jetbrains.annotations.Nullable;

public class ServerChunkManager
extends ChunkManager {
    private static final List<ChunkStatus> CHUNK_STATUSES = ChunkStatus.createOrderedList();
    private final ChunkTicketManager ticketManager;
    final ServerWorld world;
    final Thread serverThread;
    final ServerLightingProvider lightingProvider;
    private final MainThreadExecutor mainThreadExecutor;
    public final ServerChunkLoadingManager chunkLoadingManager;
    private final PersistentStateManager persistentStateManager;
    private long lastMobSpawningTime;
    private boolean spawnMonsters = true;
    private boolean spawnAnimals = true;
    private static final int CACHE_SIZE = 4;
    private final long[] chunkPosCache = new long[4];
    private final ChunkStatus[] chunkStatusCache = new ChunkStatus[4];
    private final Chunk[] chunkCache = new Chunk[4];
    @Nullable
    @Debug
    private SpawnHelper.Info spawnInfo;

    public ServerChunkManager(ServerWorld world, LevelStorage.Session session, DataFixer dataFixer, StructureTemplateManager structureTemplateManager, Executor workerExecutor, ChunkGenerator chunkGenerator, int viewDistance, int simulationDistance, boolean dsync, WorldGenerationProgressListener worldGenerationProgressListener, ChunkStatusChangeListener chunkStatusChangeListener, Supplier<PersistentStateManager> persistentStateManagerFactory) {
        this.world = world;
        this.mainThreadExecutor = new MainThreadExecutor(world);
        this.serverThread = Thread.currentThread();
        File file = session.getWorldDirectory(world.getRegistryKey()).resolve("data").toFile();
        file.mkdirs();
        this.persistentStateManager = new PersistentStateManager(file, dataFixer, world.getRegistryManager());
        this.chunkLoadingManager = new ServerChunkLoadingManager(world, session, dataFixer, structureTemplateManager, workerExecutor, this.mainThreadExecutor, this, chunkGenerator, worldGenerationProgressListener, chunkStatusChangeListener, persistentStateManagerFactory, viewDistance, dsync);
        this.lightingProvider = this.chunkLoadingManager.getLightingProvider();
        this.ticketManager = this.chunkLoadingManager.getTicketManager();
        this.ticketManager.setSimulationDistance(simulationDistance);
        this.initChunkCaches();
    }

    @Override
    public ServerLightingProvider getLightingProvider() {
        return this.lightingProvider;
    }

    @Nullable
    private ChunkHolder getChunkHolder(long pos) {
        return this.chunkLoadingManager.getChunkHolder(pos);
    }

    public int getTotalChunksLoadedCount() {
        return this.chunkLoadingManager.getTotalChunksLoadedCount();
    }

    private void putInCache(long pos, @Nullable Chunk chunk, ChunkStatus status) {
        for (int i = 3; i > 0; --i) {
            this.chunkPosCache[i] = this.chunkPosCache[i - 1];
            this.chunkStatusCache[i] = this.chunkStatusCache[i - 1];
            this.chunkCache[i] = this.chunkCache[i - 1];
        }
        this.chunkPosCache[0] = pos;
        this.chunkStatusCache[0] = status;
        this.chunkCache[0] = chunk;
    }

    @Override
    @Nullable
    public Chunk getChunk(int x, int z, ChunkStatus leastStatus, boolean create) {
        if (Thread.currentThread() != this.serverThread) {
            return CompletableFuture.supplyAsync(() -> this.getChunk(x, z, leastStatus, create), this.mainThreadExecutor).join();
        }
        Profiler lv = this.world.getProfiler();
        lv.visit("getChunk");
        long l = ChunkPos.toLong(x, z);
        for (int k = 0; k < 4; ++k) {
            Chunk lv2;
            if (l != this.chunkPosCache[k] || leastStatus != this.chunkStatusCache[k] || (lv2 = this.chunkCache[k]) == null && create) continue;
            return lv2;
        }
        lv.visit("getChunkCacheMiss");
        CompletableFuture<OptionalChunk<Chunk>> completableFuture = this.getChunkFuture(x, z, leastStatus, create);
        this.mainThreadExecutor.runTasks(completableFuture::isDone);
        OptionalChunk<Chunk> lv3 = completableFuture.join();
        Chunk lv4 = lv3.orElse(null);
        if (lv4 == null && create) {
            throw Util.throwOrPause(new IllegalStateException("Chunk not there when requested: " + lv3.getError()));
        }
        this.putInCache(l, lv4, leastStatus);
        return lv4;
    }

    @Override
    @Nullable
    public WorldChunk getWorldChunk(int chunkX, int chunkZ) {
        if (Thread.currentThread() != this.serverThread) {
            return null;
        }
        this.world.getProfiler().visit("getChunkNow");
        long l = ChunkPos.toLong(chunkX, chunkZ);
        for (int k = 0; k < 4; ++k) {
            if (l != this.chunkPosCache[k] || this.chunkStatusCache[k] != ChunkStatus.FULL) continue;
            Chunk lv = this.chunkCache[k];
            return lv instanceof WorldChunk ? (WorldChunk)lv : null;
        }
        ChunkHolder lv2 = this.getChunkHolder(l);
        if (lv2 == null) {
            return null;
        }
        Chunk lv = lv2.getOrNull(ChunkStatus.FULL);
        if (lv != null) {
            this.putInCache(l, lv, ChunkStatus.FULL);
            if (lv instanceof WorldChunk) {
                return (WorldChunk)lv;
            }
        }
        return null;
    }

    private void initChunkCaches() {
        Arrays.fill(this.chunkPosCache, ChunkPos.MARKER);
        Arrays.fill(this.chunkStatusCache, null);
        Arrays.fill(this.chunkCache, null);
    }

    public CompletableFuture<OptionalChunk<Chunk>> getChunkFutureSyncOnMainThread(int chunkX, int chunkZ, ChunkStatus leastStatus, boolean create) {
        CompletionStage<OptionalChunk<Chunk>> completableFuture;
        boolean bl2;
        boolean bl = bl2 = Thread.currentThread() == this.serverThread;
        if (bl2) {
            completableFuture = this.getChunkFuture(chunkX, chunkZ, leastStatus, create);
            this.mainThreadExecutor.runTasks(() -> completableFuture.isDone());
        } else {
            completableFuture = CompletableFuture.supplyAsync(() -> this.getChunkFuture(chunkX, chunkZ, leastStatus, create), this.mainThreadExecutor).thenCompose(future -> future);
        }
        return completableFuture;
    }

    private CompletableFuture<OptionalChunk<Chunk>> getChunkFuture(int chunkX, int chunkZ, ChunkStatus leastStatus, boolean create) {
        ChunkPos lv = new ChunkPos(chunkX, chunkZ);
        long l = lv.toLong();
        int k = ChunkLevels.getLevelFromStatus(leastStatus);
        ChunkHolder lv2 = this.getChunkHolder(l);
        if (create) {
            this.ticketManager.addTicketWithLevel(ChunkTicketType.UNKNOWN, lv, k, lv);
            if (this.isMissingForLevel(lv2, k)) {
                Profiler lv3 = this.world.getProfiler();
                lv3.push("chunkLoad");
                this.updateChunks();
                lv2 = this.getChunkHolder(l);
                lv3.pop();
                if (this.isMissingForLevel(lv2, k)) {
                    throw Util.throwOrPause(new IllegalStateException("No chunk holder after ticket has been added"));
                }
            }
        }
        if (this.isMissingForLevel(lv2, k)) {
            return AbstractChunkHolder.UNLOADED_FUTURE;
        }
        return lv2.load(leastStatus, this.chunkLoadingManager);
    }

    private boolean isMissingForLevel(@Nullable ChunkHolder holder, int maxLevel) {
        return holder == null || holder.getLevel() > maxLevel;
    }

    @Override
    public boolean isChunkLoaded(int x, int z) {
        int k;
        ChunkHolder lv = this.getChunkHolder(new ChunkPos(x, z).toLong());
        return !this.isMissingForLevel(lv, k = ChunkLevels.getLevelFromStatus(ChunkStatus.FULL));
    }

    @Override
    @Nullable
    public LightSourceView getChunk(int chunkX, int chunkZ) {
        long l = ChunkPos.toLong(chunkX, chunkZ);
        ChunkHolder lv = this.getChunkHolder(l);
        if (lv == null) {
            return null;
        }
        return lv.getUncheckedOrNull(ChunkStatus.INITIALIZE_LIGHT.getPrevious());
    }

    @Override
    public World getWorld() {
        return this.world;
    }

    public boolean executeQueuedTasks() {
        return this.mainThreadExecutor.runTask();
    }

    boolean updateChunks() {
        boolean bl = this.ticketManager.update(this.chunkLoadingManager);
        boolean bl2 = this.chunkLoadingManager.updateHolderMap();
        this.chunkLoadingManager.updateChunks();
        if (bl || bl2) {
            this.initChunkCaches();
            return true;
        }
        return false;
    }

    public boolean isTickingFutureReady(long pos) {
        ChunkHolder lv = this.getChunkHolder(pos);
        if (lv == null) {
            return false;
        }
        if (!this.world.shouldTickBlocksInChunk(pos)) {
            return false;
        }
        return lv.getTickingFuture().getNow(ChunkHolder.UNLOADED_WORLD_CHUNK).isPresent();
    }

    public void save(boolean flush) {
        this.updateChunks();
        this.chunkLoadingManager.save(flush);
    }

    @Override
    public void close() throws IOException {
        this.save(true);
        this.lightingProvider.close();
        this.chunkLoadingManager.close();
    }

    @Override
    public void tick(BooleanSupplier shouldKeepTicking, boolean tickChunks) {
        this.world.getProfiler().push("purge");
        if (this.world.getTickManager().shouldTick() || !tickChunks) {
            this.ticketManager.purge();
        }
        this.updateChunks();
        this.world.getProfiler().swap("chunks");
        if (tickChunks) {
            this.tickChunks();
            this.chunkLoadingManager.tickEntityMovement();
        }
        this.world.getProfiler().swap("unload");
        this.chunkLoadingManager.tick(shouldKeepTicking);
        this.world.getProfiler().pop();
        this.initChunkCaches();
    }

    private void tickChunks() {
        long l = this.world.getTime();
        long m = l - this.lastMobSpawningTime;
        this.lastMobSpawningTime = l;
        if (this.world.isDebugWorld()) {
            return;
        }
        Profiler lv = this.world.getProfiler();
        lv.push("pollingChunks");
        lv.push("filteringLoadedChunks");
        ArrayList<ChunkWithHolder> list = Lists.newArrayListWithCapacity(this.chunkLoadingManager.getLoadedChunkCount());
        for (ChunkHolder lv2 : this.chunkLoadingManager.entryIterator()) {
            WorldChunk lv3 = lv2.getWorldChunk();
            if (lv3 == null) continue;
            list.add(new ChunkWithHolder(lv3, lv2));
        }
        if (this.world.getTickManager().shouldTick()) {
            SpawnHelper.Info lv4;
            lv.swap("naturalSpawnCount");
            int i = this.ticketManager.getTickedChunkCount();
            this.spawnInfo = lv4 = SpawnHelper.setupSpawn(i, this.world.iterateEntities(), this::ifChunkLoaded, new SpawnDensityCapper(this.chunkLoadingManager));
            lv.swap("spawnAndTick");
            boolean bl = this.world.getGameRules().getBoolean(GameRules.DO_MOB_SPAWNING);
            Util.shuffle(list, this.world.random);
            int j = this.world.getGameRules().getInt(GameRules.RANDOM_TICK_SPEED);
            boolean bl2 = this.world.getLevelProperties().getTime() % 400L == 0L;
            for (ChunkWithHolder lv5 : list) {
                WorldChunk lv6 = lv5.chunk;
                ChunkPos lv7 = lv6.getPos();
                if (!this.world.shouldTick(lv7) || !this.chunkLoadingManager.shouldTick(lv7)) continue;
                lv6.increaseInhabitedTime(m);
                if (bl && (this.spawnMonsters || this.spawnAnimals) && this.world.getWorldBorder().contains(lv7)) {
                    SpawnHelper.spawn(this.world, lv6, lv4, this.spawnAnimals, this.spawnMonsters, bl2);
                }
                if (!this.world.shouldTickBlocksInChunk(lv7.toLong())) continue;
                this.world.tickChunk(lv6, j);
            }
            lv.swap("customSpawners");
            if (bl) {
                this.world.tickSpawners(this.spawnMonsters, this.spawnAnimals);
            }
        }
        lv.swap("broadcast");
        list.forEach(chunk -> chunk.holder.flushUpdates(chunk.chunk));
        lv.pop();
        lv.pop();
    }

    private void ifChunkLoaded(long pos, Consumer<WorldChunk> chunkConsumer) {
        ChunkHolder lv = this.getChunkHolder(pos);
        if (lv != null) {
            lv.getAccessibleFuture().getNow(ChunkHolder.UNLOADED_WORLD_CHUNK).ifPresent(chunkConsumer);
        }
    }

    @Override
    public String getDebugString() {
        return Integer.toString(this.getLoadedChunkCount());
    }

    @VisibleForTesting
    public int getPendingTasks() {
        return this.mainThreadExecutor.getTaskCount();
    }

    public ChunkGenerator getChunkGenerator() {
        return this.chunkLoadingManager.getChunkGenerator();
    }

    public StructurePlacementCalculator getStructurePlacementCalculator() {
        return this.chunkLoadingManager.getStructurePlacementCalculator();
    }

    public NoiseConfig getNoiseConfig() {
        return this.chunkLoadingManager.getNoiseConfig();
    }

    @Override
    public int getLoadedChunkCount() {
        return this.chunkLoadingManager.getLoadedChunkCount();
    }

    public void markForUpdate(BlockPos pos) {
        int j;
        int i = ChunkSectionPos.getSectionCoord(pos.getX());
        ChunkHolder lv = this.getChunkHolder(ChunkPos.toLong(i, j = ChunkSectionPos.getSectionCoord(pos.getZ())));
        if (lv != null) {
            lv.markForBlockUpdate(pos);
        }
    }

    @Override
    public void onLightUpdate(LightType type, ChunkSectionPos pos) {
        this.mainThreadExecutor.execute(() -> {
            ChunkHolder lv = this.getChunkHolder(pos.toChunkPos().toLong());
            if (lv != null) {
                lv.markForLightUpdate(type, pos.getSectionY());
            }
        });
    }

    public <T> void addTicket(ChunkTicketType<T> ticketType, ChunkPos pos, int radius, T argument) {
        this.ticketManager.addTicket(ticketType, pos, radius, argument);
    }

    public <T> void removeTicket(ChunkTicketType<T> ticketType, ChunkPos pos, int radius, T argument) {
        this.ticketManager.removeTicket(ticketType, pos, radius, argument);
    }

    @Override
    public void setChunkForced(ChunkPos pos, boolean forced) {
        this.ticketManager.setChunkForced(pos, forced);
    }

    public void updatePosition(ServerPlayerEntity player) {
        if (!player.isRemoved()) {
            this.chunkLoadingManager.updatePosition(player);
        }
    }

    public void unloadEntity(Entity entity) {
        this.chunkLoadingManager.unloadEntity(entity);
    }

    public void loadEntity(Entity entity) {
        this.chunkLoadingManager.loadEntity(entity);
    }

    public void sendToNearbyPlayers(Entity entity, Packet<?> packet) {
        this.chunkLoadingManager.sendToNearbyPlayers(entity, packet);
    }

    public void sendToOtherNearbyPlayers(Entity entity, Packet<?> packet) {
        this.chunkLoadingManager.sendToOtherNearbyPlayers(entity, packet);
    }

    public void applyViewDistance(int watchDistance) {
        this.chunkLoadingManager.setViewDistance(watchDistance);
    }

    public void applySimulationDistance(int simulationDistance) {
        this.ticketManager.setSimulationDistance(simulationDistance);
    }

    @Override
    public void setMobSpawnOptions(boolean spawnMonsters, boolean spawnAnimals) {
        this.spawnMonsters = spawnMonsters;
        this.spawnAnimals = spawnAnimals;
    }

    public String getChunkLoadingDebugInfo(ChunkPos pos) {
        return this.chunkLoadingManager.getChunkLoadingDebugInfo(pos);
    }

    public PersistentStateManager getPersistentStateManager() {
        return this.persistentStateManager;
    }

    public PointOfInterestStorage getPointOfInterestStorage() {
        return this.chunkLoadingManager.getPointOfInterestStorage();
    }

    public NbtScannable getChunkIoWorker() {
        return this.chunkLoadingManager.getWorker();
    }

    @Nullable
    @Debug
    public SpawnHelper.Info getSpawnInfo() {
        return this.spawnInfo;
    }

    public void removePersistentTickets() {
        this.ticketManager.removePersistentTickets();
    }

    @Override
    public /* synthetic */ LightingProvider getLightingProvider() {
        return this.getLightingProvider();
    }

    @Override
    public /* synthetic */ BlockView getWorld() {
        return this.getWorld();
    }

    final class MainThreadExecutor
    extends ThreadExecutor<Runnable> {
        MainThreadExecutor(World world) {
            super("Chunk source main thread executor for " + String.valueOf(world.getRegistryKey().getValue()));
        }

        @Override
        public void runTasks(BooleanSupplier stopCondition) {
            super.runTasks(() -> MinecraftServer.checkWorldGenException() && stopCondition.getAsBoolean());
        }

        @Override
        protected Runnable createTask(Runnable runnable) {
            return runnable;
        }

        @Override
        protected boolean canExecute(Runnable task) {
            return true;
        }

        @Override
        protected boolean shouldExecuteAsync() {
            return true;
        }

        @Override
        protected Thread getThread() {
            return ServerChunkManager.this.serverThread;
        }

        @Override
        protected void executeTask(Runnable task) {
            ServerChunkManager.this.world.getProfiler().visit("runTask");
            super.executeTask(task);
        }

        @Override
        protected boolean runTask() {
            if (ServerChunkManager.this.updateChunks()) {
                return true;
            }
            ServerChunkManager.this.lightingProvider.tick();
            return super.runTask();
        }
    }

    record ChunkWithHolder(WorldChunk chunk, ChunkHolder holder) {
    }
}

