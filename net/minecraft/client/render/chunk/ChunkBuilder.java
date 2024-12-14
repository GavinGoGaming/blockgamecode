/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 * 
 * Could not load the following classes:
 *  net.fabricmc.api.EnvType
 *  net.fabricmc.api.Environment
 *  org.jetbrains.annotations.Nullable
 */
package net.minecraft.client.render.chunk;

import com.google.common.collect.Lists;
import com.google.common.collect.Queues;
import com.google.common.collect.Sets;
import com.google.common.primitives.Doubles;
import com.mojang.blaze3d.systems.VertexSorter;
import it.unimi.dsi.fastutil.objects.ObjectArraySet;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.CancellationException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.Executor;
import java.util.concurrent.PriorityBlockingQueue;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gl.VertexBuffer;
import net.minecraft.client.render.BufferBuilderStorage;
import net.minecraft.client.render.BuiltBuffer;
import net.minecraft.client.render.Camera;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.WorldRenderer;
import net.minecraft.client.render.block.BlockRenderManager;
import net.minecraft.client.render.block.entity.BlockEntityRenderDispatcher;
import net.minecraft.client.render.chunk.BlockBufferAllocatorStorage;
import net.minecraft.client.render.chunk.BlockBufferBuilderPool;
import net.minecraft.client.render.chunk.ChunkOcclusionData;
import net.minecraft.client.render.chunk.ChunkRendererRegion;
import net.minecraft.client.render.chunk.ChunkRendererRegionBuilder;
import net.minecraft.client.render.chunk.SectionBuilder;
import net.minecraft.client.util.BufferAllocator;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.util.Util;
import net.minecraft.util.crash.CrashReport;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.ChunkSectionPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.thread.TaskExecutor;
import net.minecraft.world.chunk.ChunkStatus;
import org.jetbrains.annotations.Nullable;

@Environment(value=EnvType.CLIENT)
public class ChunkBuilder {
    private static final int field_35300 = 2;
    private final PriorityBlockingQueue<BuiltChunk.Task> prioritizedTaskQueue = Queues.newPriorityBlockingQueue();
    private final Queue<BuiltChunk.Task> taskQueue = Queues.newLinkedBlockingDeque();
    private int processablePrioritizedTaskCount = 2;
    private final Queue<Runnable> uploadQueue = Queues.newConcurrentLinkedQueue();
    final BlockBufferAllocatorStorage buffers;
    private final BlockBufferBuilderPool buffersPool;
    private volatile int queuedTaskCount;
    private volatile boolean stopped;
    private final TaskExecutor<Runnable> mailbox;
    private final Executor executor;
    ClientWorld world;
    final WorldRenderer worldRenderer;
    private Vec3d cameraPosition = Vec3d.ZERO;
    final SectionBuilder field_52171;

    public ChunkBuilder(ClientWorld world, WorldRenderer worldRenderer, Executor executor, BufferBuilderStorage bufferBuilderStorage, BlockRenderManager arg4, BlockEntityRenderDispatcher arg5) {
        this.world = world;
        this.worldRenderer = worldRenderer;
        this.buffers = bufferBuilderStorage.getBlockBufferBuilders();
        this.buffersPool = bufferBuilderStorage.getBlockBufferBuildersPool();
        this.executor = executor;
        this.mailbox = TaskExecutor.create(executor, "Section Renderer");
        this.mailbox.send(this::scheduleRunTasks);
        this.field_52171 = new SectionBuilder(arg4, arg5);
    }

    public void setWorld(ClientWorld world) {
        this.world = world;
    }

    private void scheduleRunTasks() {
        if (this.stopped || this.buffersPool.hasNoAvailableBuilder()) {
            return;
        }
        BuiltChunk.Task lv = this.pollTask();
        if (lv == null) {
            return;
        }
        BlockBufferAllocatorStorage lv2 = Objects.requireNonNull(this.buffersPool.acquire());
        this.queuedTaskCount = this.prioritizedTaskQueue.size() + this.taskQueue.size();
        ((CompletableFuture)CompletableFuture.supplyAsync(Util.debugSupplier(lv.getName(), () -> lv.run(lv2)), this.executor).thenCompose(future -> future)).whenComplete((result, throwable) -> {
            if (throwable != null) {
                MinecraftClient.getInstance().setCrashReportSupplierAndAddDetails(CrashReport.create(throwable, "Batching sections"));
                return;
            }
            this.mailbox.send(() -> {
                if (result == Result.SUCCESSFUL) {
                    lv2.clear();
                } else {
                    lv2.reset();
                }
                this.buffersPool.release(lv2);
                this.scheduleRunTasks();
            });
        });
    }

    @Nullable
    private BuiltChunk.Task pollTask() {
        BuiltChunk.Task lv;
        if (this.processablePrioritizedTaskCount <= 0 && (lv = this.taskQueue.poll()) != null) {
            this.processablePrioritizedTaskCount = 2;
            return lv;
        }
        lv = this.prioritizedTaskQueue.poll();
        if (lv != null) {
            --this.processablePrioritizedTaskCount;
            return lv;
        }
        this.processablePrioritizedTaskCount = 2;
        return this.taskQueue.poll();
    }

    public String getDebugString() {
        return String.format(Locale.ROOT, "pC: %03d, pU: %02d, aB: %02d", this.queuedTaskCount, this.uploadQueue.size(), this.buffersPool.getAvailableBuilderCount());
    }

    public int getToBatchCount() {
        return this.queuedTaskCount;
    }

    public int getChunksToUpload() {
        return this.uploadQueue.size();
    }

    public int getFreeBufferCount() {
        return this.buffersPool.getAvailableBuilderCount();
    }

    public void setCameraPosition(Vec3d cameraPosition) {
        this.cameraPosition = cameraPosition;
    }

    public Vec3d getCameraPosition() {
        return this.cameraPosition;
    }

    public void upload() {
        Runnable runnable;
        while ((runnable = this.uploadQueue.poll()) != null) {
            runnable.run();
        }
    }

    public void rebuild(BuiltChunk chunk, ChunkRendererRegionBuilder builder) {
        chunk.rebuild(builder);
    }

    public void reset() {
        this.clear();
    }

    public void send(BuiltChunk.Task task) {
        if (this.stopped) {
            return;
        }
        this.mailbox.send(() -> {
            if (this.stopped) {
                return;
            }
            if (arg.prioritized) {
                this.prioritizedTaskQueue.offer(task);
            } else {
                this.taskQueue.offer(task);
            }
            this.queuedTaskCount = this.prioritizedTaskQueue.size() + this.taskQueue.size();
            this.scheduleRunTasks();
        });
    }

    public CompletableFuture<Void> scheduleUpload(BuiltBuffer builtBuffer, VertexBuffer glBuffer) {
        if (this.stopped) {
            return CompletableFuture.completedFuture(null);
        }
        return CompletableFuture.runAsync(() -> {
            if (glBuffer.isClosed()) {
                builtBuffer.close();
                return;
            }
            glBuffer.bind();
            glBuffer.upload(builtBuffer);
            VertexBuffer.unbind();
        }, this.uploadQueue::add);
    }

    public CompletableFuture<Void> method_60906(BufferAllocator.CloseableBuffer arg, VertexBuffer arg2) {
        if (this.stopped) {
            return CompletableFuture.completedFuture(null);
        }
        return CompletableFuture.runAsync(() -> {
            if (arg2.isClosed()) {
                arg.close();
                return;
            }
            arg2.bind();
            arg2.uploadIndexBuffer(arg);
            VertexBuffer.unbind();
        }, this.uploadQueue::add);
    }

    private void clear() {
        BuiltChunk.Task lv;
        while (!this.prioritizedTaskQueue.isEmpty()) {
            lv = this.prioritizedTaskQueue.poll();
            if (lv == null) continue;
            lv.cancel();
        }
        while (!this.taskQueue.isEmpty()) {
            lv = this.taskQueue.poll();
            if (lv == null) continue;
            lv.cancel();
        }
        this.queuedTaskCount = 0;
    }

    public boolean isEmpty() {
        return this.queuedTaskCount == 0 && this.uploadQueue.isEmpty();
    }

    public void stop() {
        this.stopped = true;
        this.clear();
        this.upload();
    }

    @Environment(value=EnvType.CLIENT)
    public class BuiltChunk {
        public static final int field_32832 = 16;
        public final int index;
        public final AtomicReference<ChunkData> data = new AtomicReference<ChunkData>(ChunkData.EMPTY);
        private final AtomicInteger numFailures = new AtomicInteger(0);
        @Nullable
        private RebuildTask rebuildTask;
        @Nullable
        private SortTask sortTask;
        private final Set<BlockEntity> blockEntities = Sets.newHashSet();
        private final Map<RenderLayer, VertexBuffer> buffers = RenderLayer.getBlockLayers().stream().collect(Collectors.toMap(layer -> layer, layer -> new VertexBuffer(VertexBuffer.Usage.STATIC)));
        private Box boundingBox;
        private boolean needsRebuild = true;
        final BlockPos.Mutable origin = new BlockPos.Mutable(-1, -1, -1);
        private final BlockPos.Mutable[] neighborPositions = Util.make(new BlockPos.Mutable[6], neighborPositions -> {
            for (int i = 0; i < ((BlockPos.Mutable[])neighborPositions).length; ++i) {
                neighborPositions[i] = new BlockPos.Mutable();
            }
        });
        private boolean needsImportantRebuild;

        public BuiltChunk(int index, int originX, int originY, int originZ) {
            this.index = index;
            this.setOrigin(originX, originY, originZ);
        }

        private boolean isChunkNonEmpty(BlockPos pos) {
            return ChunkBuilder.this.world.getChunk(ChunkSectionPos.getSectionCoord(pos.getX()), ChunkSectionPos.getSectionCoord(pos.getZ()), ChunkStatus.FULL, false) != null;
        }

        public boolean shouldBuild() {
            int i = 24;
            if (this.getSquaredCameraDistance() > 576.0) {
                return this.isChunkNonEmpty(this.neighborPositions[Direction.WEST.ordinal()]) && this.isChunkNonEmpty(this.neighborPositions[Direction.NORTH.ordinal()]) && this.isChunkNonEmpty(this.neighborPositions[Direction.EAST.ordinal()]) && this.isChunkNonEmpty(this.neighborPositions[Direction.SOUTH.ordinal()]);
            }
            return true;
        }

        public Box getBoundingBox() {
            return this.boundingBox;
        }

        public VertexBuffer getBuffer(RenderLayer layer) {
            return this.buffers.get(layer);
        }

        public void setOrigin(int x, int y, int z) {
            this.clear();
            this.origin.set(x, y, z);
            this.boundingBox = new Box(x, y, z, x + 16, y + 16, z + 16);
            for (Direction lv : Direction.values()) {
                this.neighborPositions[lv.ordinal()].set(this.origin).move(lv, 16);
            }
        }

        protected double getSquaredCameraDistance() {
            Camera lv = MinecraftClient.getInstance().gameRenderer.getCamera();
            double d = this.boundingBox.minX + 8.0 - lv.getPos().x;
            double e = this.boundingBox.minY + 8.0 - lv.getPos().y;
            double f = this.boundingBox.minZ + 8.0 - lv.getPos().z;
            return d * d + e * e + f * f;
        }

        public ChunkData getData() {
            return this.data.get();
        }

        private void clear() {
            this.cancel();
            this.data.set(ChunkData.EMPTY);
            this.needsRebuild = true;
        }

        public void delete() {
            this.clear();
            this.buffers.values().forEach(VertexBuffer::close);
        }

        public BlockPos getOrigin() {
            return this.origin;
        }

        public void scheduleRebuild(boolean important) {
            boolean bl2 = this.needsRebuild;
            this.needsRebuild = true;
            this.needsImportantRebuild = important | (bl2 && this.needsImportantRebuild);
        }

        public void cancelRebuild() {
            this.needsRebuild = false;
            this.needsImportantRebuild = false;
        }

        public boolean needsRebuild() {
            return this.needsRebuild;
        }

        public boolean needsImportantRebuild() {
            return this.needsRebuild && this.needsImportantRebuild;
        }

        public BlockPos getNeighborPosition(Direction direction) {
            return this.neighborPositions[direction.ordinal()];
        }

        public boolean scheduleSort(RenderLayer layer, ChunkBuilder chunkRenderer) {
            ChunkData lv = this.getData();
            if (this.sortTask != null) {
                this.sortTask.cancel();
            }
            if (!lv.nonEmptyLayers.contains(layer)) {
                return false;
            }
            this.sortTask = new SortTask(this.getSquaredCameraDistance(), lv);
            chunkRenderer.send(this.sortTask);
            return true;
        }

        protected boolean cancel() {
            boolean bl = false;
            if (this.rebuildTask != null) {
                this.rebuildTask.cancel();
                this.rebuildTask = null;
                bl = true;
            }
            if (this.sortTask != null) {
                this.sortTask.cancel();
                this.sortTask = null;
            }
            return bl;
        }

        public Task createRebuildTask(ChunkRendererRegionBuilder arg) {
            boolean bl2;
            boolean bl = this.cancel();
            ChunkRendererRegion lv = arg.build(ChunkBuilder.this.world, ChunkSectionPos.from(this.origin));
            boolean bl3 = bl2 = this.data.get() == ChunkData.EMPTY;
            if (bl2 && bl) {
                this.numFailures.incrementAndGet();
            }
            this.rebuildTask = new RebuildTask(this.getSquaredCameraDistance(), lv, !bl2 || this.numFailures.get() > 2);
            return this.rebuildTask;
        }

        public void scheduleRebuild(ChunkBuilder chunkRenderer, ChunkRendererRegionBuilder builder) {
            Task lv = this.createRebuildTask(builder);
            chunkRenderer.send(lv);
        }

        /*
         * WARNING - Removed try catching itself - possible behaviour change.
         */
        void setNoCullingBlockEntities(Collection<BlockEntity> blockEntities) {
            HashSet<BlockEntity> set2;
            HashSet<BlockEntity> set = Sets.newHashSet(blockEntities);
            Set<BlockEntity> set3 = this.blockEntities;
            synchronized (set3) {
                set2 = Sets.newHashSet(this.blockEntities);
                set.removeAll(this.blockEntities);
                set2.removeAll(blockEntities);
                this.blockEntities.clear();
                this.blockEntities.addAll(blockEntities);
            }
            ChunkBuilder.this.worldRenderer.updateNoCullingBlockEntities(set2, set);
        }

        public void rebuild(ChunkRendererRegionBuilder builder) {
            Task lv = this.createRebuildTask(builder);
            lv.run(ChunkBuilder.this.buffers);
        }

        public boolean method_52841(int i, int j, int k) {
            BlockPos lv = this.getOrigin();
            return i == ChunkSectionPos.getSectionCoord(lv.getX()) || k == ChunkSectionPos.getSectionCoord(lv.getZ()) || j == ChunkSectionPos.getSectionCoord(lv.getY());
        }

        void method_60908(ChunkData arg) {
            this.data.set(arg);
            this.numFailures.set(0);
            ChunkBuilder.this.worldRenderer.addBuiltChunk(this);
        }

        VertexSorter method_60909() {
            Vec3d lv = ChunkBuilder.this.getCameraPosition();
            return VertexSorter.byDistance((float)(lv.x - (double)this.origin.getX()), (float)(lv.y - (double)this.origin.getY()), (float)(lv.z - (double)this.origin.getZ()));
        }

        @Environment(value=EnvType.CLIENT)
        class SortTask
        extends Task {
            private final ChunkData data;

            public SortTask(double distance, ChunkData data) {
                super(BuiltChunk.this, distance, true);
                this.data = data;
            }

            @Override
            protected String getName() {
                return "rend_chk_sort";
            }

            @Override
            public CompletableFuture<Result> run(BlockBufferAllocatorStorage buffers) {
                if (this.cancelled.get()) {
                    return CompletableFuture.completedFuture(Result.CANCELLED);
                }
                if (!BuiltChunk.this.shouldBuild()) {
                    this.cancelled.set(true);
                    return CompletableFuture.completedFuture(Result.CANCELLED);
                }
                if (this.cancelled.get()) {
                    return CompletableFuture.completedFuture(Result.CANCELLED);
                }
                BuiltBuffer.SortState lv = this.data.transparentSortingData;
                if (lv == null || this.data.isEmpty(RenderLayer.getTranslucent())) {
                    return CompletableFuture.completedFuture(Result.CANCELLED);
                }
                VertexSorter lv2 = BuiltChunk.this.method_60909();
                BufferAllocator.CloseableBuffer lv3 = lv.sortAndStore(buffers.get(RenderLayer.getTranslucent()), lv2);
                if (lv3 == null) {
                    return CompletableFuture.completedFuture(Result.CANCELLED);
                }
                if (this.cancelled.get()) {
                    lv3.close();
                    return CompletableFuture.completedFuture(Result.CANCELLED);
                }
                CompletionStage completableFuture = ChunkBuilder.this.method_60906(lv3, BuiltChunk.this.getBuffer(RenderLayer.getTranslucent())).thenApply(v -> Result.CANCELLED);
                return ((CompletableFuture)completableFuture).handle((result, throwable) -> {
                    if (throwable != null && !(throwable instanceof CancellationException) && !(throwable instanceof InterruptedException)) {
                        MinecraftClient.getInstance().setCrashReportSupplierAndAddDetails(CrashReport.create(throwable, "Rendering section"));
                    }
                    return this.cancelled.get() ? Result.CANCELLED : Result.SUCCESSFUL;
                });
            }

            @Override
            public void cancel() {
                this.cancelled.set(true);
            }
        }

        @Environment(value=EnvType.CLIENT)
        abstract class Task
        implements Comparable<Task> {
            protected final double distance;
            protected final AtomicBoolean cancelled = new AtomicBoolean(false);
            protected final boolean prioritized;

            public Task(BuiltChunk arg, double distance, boolean prioritized) {
                this.distance = distance;
                this.prioritized = prioritized;
            }

            public abstract CompletableFuture<Result> run(BlockBufferAllocatorStorage var1);

            public abstract void cancel();

            protected abstract String getName();

            @Override
            public int compareTo(Task arg) {
                return Doubles.compare(this.distance, arg.distance);
            }

            @Override
            public /* synthetic */ int compareTo(Object other) {
                return this.compareTo((Task)other);
            }
        }

        @Environment(value=EnvType.CLIENT)
        class RebuildTask
        extends Task {
            @Nullable
            protected ChunkRendererRegion region;

            public RebuildTask(@Nullable double distance, ChunkRendererRegion region, boolean prioritized) {
                super(BuiltChunk.this, distance, prioritized);
                this.region = region;
            }

            @Override
            protected String getName() {
                return "rend_chk_rebuild";
            }

            @Override
            public CompletableFuture<Result> run(BlockBufferAllocatorStorage buffers) {
                if (this.cancelled.get()) {
                    return CompletableFuture.completedFuture(Result.CANCELLED);
                }
                if (!BuiltChunk.this.shouldBuild()) {
                    this.cancel();
                    return CompletableFuture.completedFuture(Result.CANCELLED);
                }
                if (this.cancelled.get()) {
                    return CompletableFuture.completedFuture(Result.CANCELLED);
                }
                ChunkRendererRegion lv = this.region;
                this.region = null;
                if (lv == null) {
                    BuiltChunk.this.method_60908(ChunkData.field_52172);
                    return CompletableFuture.completedFuture(Result.SUCCESSFUL);
                }
                ChunkSectionPos lv2 = ChunkSectionPos.from(BuiltChunk.this.origin);
                SectionBuilder.RenderData lv3 = ChunkBuilder.this.field_52171.build(lv2, lv, BuiltChunk.this.method_60909(), buffers);
                BuiltChunk.this.setNoCullingBlockEntities(lv3.noCullingBlockEntities);
                if (this.cancelled.get()) {
                    lv3.close();
                    return CompletableFuture.completedFuture(Result.CANCELLED);
                }
                ChunkData lv4 = new ChunkData();
                lv4.occlusionGraph = lv3.chunkOcclusionData;
                lv4.blockEntities.addAll(lv3.blockEntities);
                lv4.transparentSortingData = lv3.translucencySortingData;
                ArrayList list = new ArrayList(lv3.buffers.size());
                lv3.buffers.forEach((renderLayer, buffer) -> {
                    list.add(ChunkBuilder.this.scheduleUpload((BuiltBuffer)buffer, BuiltChunk.this.getBuffer((RenderLayer)renderLayer)));
                    arg.nonEmptyLayers.add((RenderLayer)renderLayer);
                });
                return Util.combine(list).handle((results, throwable) -> {
                    if (throwable != null && !(throwable instanceof CancellationException) && !(throwable instanceof InterruptedException)) {
                        MinecraftClient.getInstance().setCrashReportSupplierAndAddDetails(CrashReport.create(throwable, "Rendering section"));
                    }
                    if (this.cancelled.get()) {
                        return Result.CANCELLED;
                    }
                    BuiltChunk.this.method_60908(lv4);
                    return Result.SUCCESSFUL;
                });
            }

            @Override
            public void cancel() {
                this.region = null;
                if (this.cancelled.compareAndSet(false, true)) {
                    BuiltChunk.this.scheduleRebuild(false);
                }
            }
        }
    }

    @Environment(value=EnvType.CLIENT)
    static enum Result {
        SUCCESSFUL,
        CANCELLED;

    }

    @Environment(value=EnvType.CLIENT)
    public static class ChunkData {
        public static final ChunkData EMPTY = new ChunkData(){

            @Override
            public boolean isVisibleThrough(Direction from, Direction to) {
                return false;
            }
        };
        public static final ChunkData field_52172 = new ChunkData(){

            @Override
            public boolean isVisibleThrough(Direction from, Direction to) {
                return true;
            }
        };
        final Set<RenderLayer> nonEmptyLayers = new ObjectArraySet<RenderLayer>(RenderLayer.getBlockLayers().size());
        final List<BlockEntity> blockEntities = Lists.newArrayList();
        ChunkOcclusionData occlusionGraph = new ChunkOcclusionData();
        @Nullable
        BuiltBuffer.SortState transparentSortingData;

        public boolean isEmpty() {
            return this.nonEmptyLayers.isEmpty();
        }

        public boolean isEmpty(RenderLayer layer) {
            return !this.nonEmptyLayers.contains(layer);
        }

        public List<BlockEntity> getBlockEntities() {
            return this.blockEntities;
        }

        public boolean isVisibleThrough(Direction from, Direction to) {
            return this.occlusionGraph.isVisibleThrough(from, to);
        }
    }
}

