/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 * 
 * Could not load the following classes:
 *  org.jetbrains.annotations.Nullable
 */
package net.minecraft.world.storage;

import com.google.common.collect.Maps;
import com.mojang.datafixers.util.Either;
import com.mojang.logging.LogUtils;
import it.unimi.dsi.fastutil.longs.Long2ObjectLinkedOpenHashMap;
import java.io.IOException;
import java.nio.file.Path;
import java.util.BitSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Function;
import java.util.function.Supplier;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtInt;
import net.minecraft.nbt.scanner.NbtScanQuery;
import net.minecraft.nbt.scanner.NbtScanner;
import net.minecraft.nbt.scanner.SelectiveNbtCollector;
import net.minecraft.util.Unit;
import net.minecraft.util.Util;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.thread.TaskExecutor;
import net.minecraft.util.thread.TaskQueue;
import net.minecraft.world.storage.NbtScannable;
import net.minecraft.world.storage.RegionBasedStorage;
import net.minecraft.world.storage.StorageKey;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;

public class StorageIoWorker
implements NbtScannable,
AutoCloseable {
    private static final Logger LOGGER = LogUtils.getLogger();
    private final AtomicBoolean closed = new AtomicBoolean();
    private final TaskExecutor<TaskQueue.PrioritizedTask> executor;
    private final RegionBasedStorage storage;
    private final Map<ChunkPos, Result> results = Maps.newLinkedHashMap();
    private final Long2ObjectLinkedOpenHashMap<CompletableFuture<BitSet>> blendingStatusCaches = new Long2ObjectLinkedOpenHashMap();
    private static final int MAX_CACHE_SIZE = 1024;

    protected StorageIoWorker(StorageKey storageKey, Path directory, boolean dsync) {
        this.storage = new RegionBasedStorage(storageKey, directory, dsync);
        this.executor = new TaskExecutor<TaskQueue.PrioritizedTask>(new TaskQueue.Prioritized(Priority.values().length), Util.getIoWorkerExecutor(), "IOWorker-" + storageKey.type());
    }

    public boolean needsBlending(ChunkPos chunkPos, int checkRadius) {
        ChunkPos lv = new ChunkPos(chunkPos.x - checkRadius, chunkPos.z - checkRadius);
        ChunkPos lv2 = new ChunkPos(chunkPos.x + checkRadius, chunkPos.z + checkRadius);
        for (int j = lv.getRegionX(); j <= lv2.getRegionX(); ++j) {
            for (int k = lv.getRegionZ(); k <= lv2.getRegionZ(); ++k) {
                BitSet bitSet = this.getOrComputeBlendingStatus(j, k).join();
                if (bitSet.isEmpty()) continue;
                ChunkPos lv3 = ChunkPos.fromRegion(j, k);
                int l = Math.max(lv.x - lv3.x, 0);
                int m = Math.max(lv.z - lv3.z, 0);
                int n = Math.min(lv2.x - lv3.x, 31);
                int o = Math.min(lv2.z - lv3.z, 31);
                for (int p = l; p <= n; ++p) {
                    for (int q = m; q <= o; ++q) {
                        int r = q * 32 + p;
                        if (!bitSet.get(r)) continue;
                        return true;
                    }
                }
            }
        }
        return false;
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    private CompletableFuture<BitSet> getOrComputeBlendingStatus(int chunkX, int chunkZ) {
        long l = ChunkPos.toLong(chunkX, chunkZ);
        Long2ObjectLinkedOpenHashMap<CompletableFuture<BitSet>> long2ObjectLinkedOpenHashMap = this.blendingStatusCaches;
        synchronized (long2ObjectLinkedOpenHashMap) {
            CompletableFuture<BitSet> completableFuture = this.blendingStatusCaches.getAndMoveToFirst(l);
            if (completableFuture == null) {
                completableFuture = this.computeBlendingStatus(chunkX, chunkZ);
                this.blendingStatusCaches.putAndMoveToFirst(l, completableFuture);
                if (this.blendingStatusCaches.size() > 1024) {
                    this.blendingStatusCaches.removeLast();
                }
            }
            return completableFuture;
        }
    }

    private CompletableFuture<BitSet> computeBlendingStatus(int chunkX, int chunkZ) {
        return CompletableFuture.supplyAsync(() -> {
            ChunkPos lv = ChunkPos.fromRegion(chunkX, chunkZ);
            ChunkPos lv2 = ChunkPos.fromRegionCenter(chunkX, chunkZ);
            BitSet bitSet = new BitSet();
            ChunkPos.stream(lv, lv2).forEach(chunkPos -> {
                NbtCompound lv3;
                SelectiveNbtCollector lv = new SelectiveNbtCollector(new NbtScanQuery(NbtInt.TYPE, "DataVersion"), new NbtScanQuery(NbtCompound.TYPE, "blending_data"));
                try {
                    this.scanChunk((ChunkPos)chunkPos, lv).join();
                } catch (Exception exception) {
                    LOGGER.warn("Failed to scan chunk {}", chunkPos, (Object)exception);
                    return;
                }
                NbtElement lv2 = lv.getRoot();
                if (lv2 instanceof NbtCompound && this.needsBlending(lv3 = (NbtCompound)lv2)) {
                    int i = chunkPos.getRegionRelativeZ() * 32 + chunkPos.getRegionRelativeX();
                    bitSet.set(i);
                }
            });
            return bitSet;
        }, Util.getMainWorkerExecutor());
    }

    private boolean needsBlending(NbtCompound nbt) {
        if (!nbt.contains("DataVersion", NbtElement.NUMBER_TYPE) || nbt.getInt("DataVersion") < 3441) {
            return true;
        }
        return nbt.contains("blending_data", NbtElement.COMPOUND_TYPE);
    }

    public CompletableFuture<Void> setResult(ChunkPos pos, @Nullable NbtCompound nbt) {
        return this.run(() -> {
            Result lv = this.results.computeIfAbsent(pos, pos2 -> new Result(nbt));
            lv.nbt = nbt;
            return Either.left(lv.future);
        }).thenCompose(Function.identity());
    }

    public CompletableFuture<Optional<NbtCompound>> readChunkData(ChunkPos pos) {
        return this.run(() -> {
            Result lv = this.results.get(pos);
            if (lv != null) {
                return Either.left(Optional.ofNullable(lv.copyNbt()));
            }
            try {
                NbtCompound lv2 = this.storage.getTagAt(pos);
                return Either.left(Optional.ofNullable(lv2));
            } catch (Exception exception) {
                LOGGER.warn("Failed to read chunk {}", (Object)pos, (Object)exception);
                return Either.right(exception);
            }
        });
    }

    public CompletableFuture<Void> completeAll(boolean sync) {
        CompletionStage completableFuture = this.run(() -> Either.left(CompletableFuture.allOf((CompletableFuture[])this.results.values().stream().map(result -> result.future).toArray(CompletableFuture[]::new)))).thenCompose(Function.identity());
        if (sync) {
            return ((CompletableFuture)completableFuture).thenCompose(void_ -> this.run(() -> {
                try {
                    this.storage.sync();
                    return Either.left(null);
                } catch (Exception exception) {
                    LOGGER.warn("Failed to synchronize chunks", exception);
                    return Either.right(exception);
                }
            }));
        }
        return ((CompletableFuture)completableFuture).thenCompose(void_ -> this.run(() -> Either.left(null)));
    }

    @Override
    public CompletableFuture<Void> scanChunk(ChunkPos pos, NbtScanner scanner) {
        return this.run(() -> {
            try {
                Result lv = this.results.get(pos);
                if (lv != null) {
                    if (lv.nbt != null) {
                        lv.nbt.accept(scanner);
                    }
                } else {
                    this.storage.scanChunk(pos, scanner);
                }
                return Either.left(null);
            } catch (Exception exception) {
                LOGGER.warn("Failed to bulk scan chunk {}", (Object)pos, (Object)exception);
                return Either.right(exception);
            }
        });
    }

    private <T> CompletableFuture<T> run(Supplier<Either<T, Exception>> task) {
        return this.executor.askFallible(listener -> new TaskQueue.PrioritizedTask(Priority.FOREGROUND.ordinal(), () -> {
            if (!this.closed.get()) {
                listener.send((Either)((Supplier)task).get());
            }
            this.writeRemainingResults();
        }));
    }

    private void writeResult() {
        if (this.results.isEmpty()) {
            return;
        }
        Iterator<Map.Entry<ChunkPos, Result>> iterator = this.results.entrySet().iterator();
        Map.Entry<ChunkPos, Result> entry = iterator.next();
        iterator.remove();
        this.write(entry.getKey(), entry.getValue());
        this.writeRemainingResults();
    }

    private void writeRemainingResults() {
        this.executor.send(new TaskQueue.PrioritizedTask(Priority.BACKGROUND.ordinal(), this::writeResult));
    }

    private void write(ChunkPos pos, Result result) {
        try {
            this.storage.write(pos, result.nbt);
            result.future.complete(null);
        } catch (Exception exception) {
            LOGGER.error("Failed to store chunk {}", (Object)pos, (Object)exception);
            result.future.completeExceptionally(exception);
        }
    }

    @Override
    public void close() throws IOException {
        if (!this.closed.compareAndSet(false, true)) {
            return;
        }
        this.executor.ask(listener -> new TaskQueue.PrioritizedTask(Priority.SHUTDOWN.ordinal(), () -> listener.send(Unit.INSTANCE))).join();
        this.executor.close();
        try {
            this.storage.close();
        } catch (Exception exception) {
            LOGGER.error("Failed to close storage", exception);
        }
    }

    public StorageKey getStorageKey() {
        return this.storage.getStorageKey();
    }

    static enum Priority {
        FOREGROUND,
        BACKGROUND,
        SHUTDOWN;

    }

    static class Result {
        @Nullable
        NbtCompound nbt;
        final CompletableFuture<Void> future = new CompletableFuture();

        public Result(@Nullable NbtCompound nbt) {
            this.nbt = nbt;
        }

        @Nullable
        NbtCompound copyNbt() {
            NbtCompound lv = this.nbt;
            return lv == null ? null : lv.copy();
        }
    }
}

