/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 * 
 * Could not load the following classes:
 *  org.jetbrains.annotations.Nullable
 */
package net.minecraft.world.chunk;

import com.mojang.datafixers.util.Pair;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.concurrent.atomic.AtomicReferenceArray;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.world.ChunkLevelType;
import net.minecraft.server.world.ChunkLevels;
import net.minecraft.server.world.OptionalChunk;
import net.minecraft.server.world.ServerChunkLoadingManager;
import net.minecraft.util.annotation.Debug;
import net.minecraft.util.collection.BoundedRegionArray;
import net.minecraft.util.crash.CrashException;
import net.minecraft.util.crash.CrashReport;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.ChunkLoadingManager;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.ChunkGenerationStep;
import net.minecraft.world.chunk.ChunkLoader;
import net.minecraft.world.chunk.ChunkStatus;
import net.minecraft.world.chunk.ProtoChunk;
import net.minecraft.world.chunk.WrapperProtoChunk;
import org.jetbrains.annotations.Nullable;

public abstract class AbstractChunkHolder {
    private static final List<ChunkStatus> STATUSES = ChunkStatus.createOrderedList();
    private static final OptionalChunk<Chunk> NOT_DONE = OptionalChunk.of("Not done yet");
    public static final OptionalChunk<Chunk> UNLOADED = OptionalChunk.of("Unloaded chunk");
    public static final CompletableFuture<OptionalChunk<Chunk>> UNLOADED_FUTURE = CompletableFuture.completedFuture(UNLOADED);
    protected final ChunkPos pos;
    @Nullable
    private volatile ChunkStatus status;
    private final AtomicReference<ChunkStatus> currentStatus = new AtomicReference();
    private final AtomicReferenceArray<CompletableFuture<OptionalChunk<Chunk>>> chunkFuturesByStatus = new AtomicReferenceArray(STATUSES.size());
    private final AtomicReference<ChunkLoader> chunkLoader = new AtomicReference();
    private final AtomicInteger refCount = new AtomicInteger();

    public AbstractChunkHolder(ChunkPos pos) {
        this.pos = pos;
    }

    public CompletableFuture<OptionalChunk<Chunk>> load(ChunkStatus requestedStatus, ServerChunkLoadingManager chunkLoadingManager) {
        if (this.cannotBeLoaded(requestedStatus)) {
            return UNLOADED_FUTURE;
        }
        CompletableFuture<OptionalChunk<Chunk>> completableFuture = this.getOrCreateFuture(requestedStatus);
        if (completableFuture.isDone()) {
            return completableFuture;
        }
        ChunkLoader lv = this.chunkLoader.get();
        if (lv == null || requestedStatus.isLaterThan(lv.targetStatus)) {
            this.createLoader(chunkLoadingManager, requestedStatus);
        }
        return completableFuture;
    }

    CompletableFuture<OptionalChunk<Chunk>> generate(ChunkGenerationStep step, ChunkLoadingManager chunkLoadingManager, BoundedRegionArray<AbstractChunkHolder> chunks) {
        if (this.cannotBeLoaded(step.targetStatus())) {
            return UNLOADED_FUTURE;
        }
        if (this.progressStatus(step.targetStatus())) {
            return chunkLoadingManager.generate(this, step, chunks).handle((chunk, throwable) -> {
                if (throwable != null) {
                    CrashReport lv = CrashReport.create(throwable, "Exception chunk generation/loading");
                    MinecraftServer.setWorldGenException(new CrashException(lv));
                } else {
                    this.completeChunkFuture(step.targetStatus(), (Chunk)chunk);
                }
                return OptionalChunk.of(chunk);
            });
        }
        return this.getOrCreateFuture(step.targetStatus());
    }

    protected void updateStatus(ServerChunkLoadingManager chunkLoadingManager) {
        boolean bl;
        ChunkStatus lv2;
        ChunkStatus lv = this.status;
        this.status = lv2 = ChunkLevels.getStatus(this.getLevel());
        boolean bl2 = bl = lv != null && (lv2 == null || lv2.isEarlierThan(lv));
        if (bl) {
            this.unload(lv2, lv);
            if (this.chunkLoader.get() != null) {
                this.createLoader(chunkLoadingManager, this.getMaxPendingStatus(lv2));
            }
        }
    }

    public void replaceWith(WrapperProtoChunk chunk) {
        CompletableFuture<OptionalChunk<WrapperProtoChunk>> completableFuture = CompletableFuture.completedFuture(OptionalChunk.of(chunk));
        for (int i = 0; i < this.chunkFuturesByStatus.length() - 1; ++i) {
            CompletableFuture<OptionalChunk<Chunk>> completableFuture2 = this.chunkFuturesByStatus.get(i);
            Objects.requireNonNull(completableFuture2);
            Chunk lv = completableFuture2.getNow(NOT_DONE).orElse(null);
            if (lv instanceof ProtoChunk) {
                if (this.chunkFuturesByStatus.compareAndSet(i, completableFuture2, completableFuture)) continue;
                throw new IllegalStateException("Future changed by other thread while trying to replace it");
            }
            throw new IllegalStateException("Trying to replace a ProtoChunk, but found " + String.valueOf(lv));
        }
    }

    void clearLoader(ChunkLoader loader) {
        this.chunkLoader.compareAndSet(loader, null);
    }

    private void createLoader(ServerChunkLoadingManager chunkLoadingManager, @Nullable ChunkStatus requestedStatus) {
        ChunkLoader lv = requestedStatus != null ? chunkLoadingManager.createLoader(requestedStatus, this.getPos()) : null;
        ChunkLoader lv2 = this.chunkLoader.getAndSet(lv);
        if (lv2 != null) {
            lv2.markPendingDisposal();
        }
    }

    private CompletableFuture<OptionalChunk<Chunk>> getOrCreateFuture(ChunkStatus status) {
        if (this.cannotBeLoaded(status)) {
            return UNLOADED_FUTURE;
        }
        int i = status.getIndex();
        CompletableFuture<OptionalChunk<Chunk>> completableFuture = this.chunkFuturesByStatus.get(i);
        while (completableFuture == null) {
            CompletableFuture<OptionalChunk<Chunk>> completableFuture2 = new CompletableFuture<OptionalChunk<Chunk>>();
            completableFuture = this.chunkFuturesByStatus.compareAndExchange(i, null, completableFuture2);
            if (completableFuture != null) continue;
            if (this.cannotBeLoaded(status)) {
                this.unload(i, completableFuture2);
                return UNLOADED_FUTURE;
            }
            return completableFuture2;
        }
        return completableFuture;
    }

    private void unload(@Nullable ChunkStatus from, ChunkStatus to) {
        int i = from == null ? 0 : from.getIndex() + 1;
        int j = to.getIndex();
        for (int k = i; k <= j; ++k) {
            CompletableFuture<OptionalChunk<Chunk>> completableFuture = this.chunkFuturesByStatus.get(k);
            if (completableFuture == null) continue;
            this.unload(k, completableFuture);
        }
    }

    private void unload(int statusIndex, CompletableFuture<OptionalChunk<Chunk>> previousFuture) {
        if (previousFuture.complete(UNLOADED) && !this.chunkFuturesByStatus.compareAndSet(statusIndex, previousFuture, null)) {
            throw new IllegalStateException("Nothing else should replace the future here");
        }
    }

    private void completeChunkFuture(ChunkStatus status, Chunk chunk) {
        OptionalChunk<Chunk> lv = OptionalChunk.of(chunk);
        int i = status.getIndex();
        while (true) {
            CompletableFuture<OptionalChunk<Chunk>> completableFuture;
            if ((completableFuture = this.chunkFuturesByStatus.get(i)) == null) {
                if (!this.chunkFuturesByStatus.compareAndSet(i, null, CompletableFuture.completedFuture(lv))) continue;
                return;
            }
            if (completableFuture.complete(lv)) {
                return;
            }
            if (completableFuture.getNow(NOT_DONE).isPresent()) {
                throw new IllegalStateException("Trying to complete a future but found it to be completed successfully already");
            }
            Thread.yield();
        }
    }

    @Nullable
    private ChunkStatus getMaxPendingStatus(@Nullable ChunkStatus checkUpperBound) {
        if (checkUpperBound == null) {
            return null;
        }
        ChunkStatus lv = checkUpperBound;
        ChunkStatus lv2 = this.currentStatus.get();
        while (lv2 == null || lv.isLaterThan(lv2)) {
            if (this.chunkFuturesByStatus.get(lv.getIndex()) != null) {
                return lv;
            }
            if (lv == ChunkStatus.EMPTY) break;
            lv = lv.getPrevious();
        }
        return null;
    }

    private boolean progressStatus(ChunkStatus nextStatus) {
        ChunkStatus lv = nextStatus == ChunkStatus.EMPTY ? null : nextStatus.getPrevious();
        ChunkStatus lv2 = this.currentStatus.compareAndExchange(lv, nextStatus);
        if (lv2 == lv) {
            return true;
        }
        if (lv2 == null || nextStatus.isLaterThan(lv2)) {
            throw new IllegalStateException("Unexpected last startedWork status: " + String.valueOf(lv2) + " while trying to start: " + String.valueOf(nextStatus));
        }
        return false;
    }

    private boolean cannotBeLoaded(ChunkStatus status) {
        ChunkStatus lv = this.status;
        return lv == null || status.isLaterThan(lv);
    }

    public void incrementRefCount() {
        this.refCount.incrementAndGet();
    }

    public void decrementRefCount() {
        int i = this.refCount.decrementAndGet();
        if (i < 0) {
            throw new IllegalStateException("More releases than claims. Count: " + i);
        }
    }

    public int getRefCount() {
        return this.refCount.get();
    }

    @Nullable
    public Chunk getUncheckedOrNull(ChunkStatus requestedStatus) {
        CompletableFuture<OptionalChunk<Chunk>> completableFuture = this.chunkFuturesByStatus.get(requestedStatus.getIndex());
        return completableFuture == null ? null : (Chunk)completableFuture.getNow(NOT_DONE).orElse(null);
    }

    @Nullable
    public Chunk getOrNull(ChunkStatus requestedStatus) {
        if (this.cannotBeLoaded(requestedStatus)) {
            return null;
        }
        return this.getUncheckedOrNull(requestedStatus);
    }

    @Nullable
    public Chunk getLatest() {
        ChunkStatus lv = this.currentStatus.get();
        if (lv == null) {
            return null;
        }
        Chunk lv2 = this.getUncheckedOrNull(lv);
        if (lv2 != null) {
            return lv2;
        }
        return this.getUncheckedOrNull(lv.getPrevious());
    }

    @Nullable
    public ChunkStatus getActualStatus() {
        CompletableFuture<OptionalChunk<Chunk>> completableFuture = this.chunkFuturesByStatus.get(ChunkStatus.EMPTY.getIndex());
        Chunk lv = completableFuture == null ? null : (Chunk)completableFuture.getNow(NOT_DONE).orElse(null);
        return lv == null ? null : lv.getStatus();
    }

    public ChunkPos getPos() {
        return this.pos;
    }

    public ChunkLevelType getLevelType() {
        return ChunkLevels.getType(this.getLevel());
    }

    public abstract int getLevel();

    public abstract int getCompletedLevel();

    @Debug
    public List<Pair<ChunkStatus, CompletableFuture<OptionalChunk<Chunk>>>> enumerateFutures() {
        ArrayList<Pair<ChunkStatus, CompletableFuture<OptionalChunk<Chunk>>>> list = new ArrayList<Pair<ChunkStatus, CompletableFuture<OptionalChunk<Chunk>>>>();
        for (int i = 0; i < STATUSES.size(); ++i) {
            list.add(Pair.of(STATUSES.get(i), this.chunkFuturesByStatus.get(i)));
        }
        return list;
    }

    @Nullable
    @Debug
    public ChunkStatus getLatestStatus() {
        for (int i = STATUSES.size() - 1; i >= 0; --i) {
            ChunkStatus lv = STATUSES.get(i);
            Chunk lv2 = this.getUncheckedOrNull(lv);
            if (lv2 == null) continue;
            return lv;
        }
        return null;
    }
}

