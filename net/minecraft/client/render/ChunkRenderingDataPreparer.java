/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 * 
 * Could not load the following classes:
 *  net.fabricmc.api.EnvType
 *  net.fabricmc.api.Environment
 *  org.jetbrains.annotations.Nullable
 */
package net.minecraft.client.render;

import com.google.common.collect.Lists;
import com.google.common.collect.Queues;
import com.mojang.logging.LogUtils;
import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.longs.LongIterator;
import it.unimi.dsi.fastutil.longs.LongOpenHashSet;
import it.unimi.dsi.fastutil.longs.LongSet;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Future;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.render.BuiltChunkStorage;
import net.minecraft.client.render.Camera;
import net.minecraft.client.render.Frustum;
import net.minecraft.client.render.WorldRenderer;
import net.minecraft.client.render.chunk.ChunkBuilder;
import net.minecraft.server.network.ChunkFilter;
import net.minecraft.util.Util;
import net.minecraft.util.annotation.Debug;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.ChunkSectionPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.HeightLimitView;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;

@Environment(value=EnvType.CLIENT)
public class ChunkRenderingDataPreparer {
    private static final Logger LOGGER = LogUtils.getLogger();
    private static final Direction[] field_45618 = Direction.values();
    private static final int field_45619 = 60;
    private static final double field_45620 = Math.ceil(Math.sqrt(3.0) * 16.0);
    private boolean field_45621 = true;
    @Nullable
    private Future<?> field_45622;
    @Nullable
    private BuiltChunkStorage field_45623;
    private final AtomicReference<class_8681> field_45624 = new AtomicReference();
    private final AtomicReference<class_8680> field_45625 = new AtomicReference();
    private final AtomicBoolean field_45626 = new AtomicBoolean(false);

    public void method_52826(@Nullable BuiltChunkStorage arg) {
        if (this.field_45622 != null) {
            try {
                this.field_45622.get();
                this.field_45622 = null;
            } catch (Exception exception) {
                LOGGER.warn("Full update failed", exception);
            }
        }
        this.field_45623 = arg;
        if (arg != null) {
            this.field_45624.set(new class_8681(arg.chunks.length));
            this.method_52817();
        } else {
            this.field_45624.set(null);
        }
    }

    public void method_52817() {
        this.field_45621 = true;
    }

    public void method_52828(Frustum arg, List<ChunkBuilder.BuiltChunk> list) {
        for (ChunkInfo lv : this.field_45624.get().storage().chunks) {
            if (!arg.isVisible(lv.chunk.getBoundingBox())) continue;
            list.add(lv.chunk);
        }
    }

    public boolean method_52836() {
        return this.field_45626.compareAndSet(true, false);
    }

    public void method_52819(ChunkPos arg) {
        class_8680 lv2;
        class_8680 lv = this.field_45625.get();
        if (lv != null) {
            this.method_52822(lv, arg);
        }
        if ((lv2 = this.field_45624.get().events) != lv) {
            this.method_52822(lv2, arg);
        }
    }

    public void method_52827(ChunkBuilder.BuiltChunk arg) {
        class_8680 lv2;
        class_8680 lv = this.field_45625.get();
        if (lv != null) {
            lv.sectionsToPropagateFrom.add(arg);
        }
        if ((lv2 = this.field_45624.get().events) != lv) {
            lv2.sectionsToPropagateFrom.add(arg);
        }
    }

    public void method_52834(boolean bl, Camera arg, Frustum arg2, List<ChunkBuilder.BuiltChunk> list) {
        Vec3d lv = arg.getPos();
        if (this.field_45621 && (this.field_45622 == null || this.field_45622.isDone())) {
            this.method_52833(bl, arg, lv);
        }
        this.method_52835(bl, arg2, list, lv);
    }

    private void method_52833(boolean bl, Camera arg, Vec3d arg2) {
        this.field_45621 = false;
        this.field_45622 = Util.getMainWorkerExecutor().submit(() -> {
            class_8681 lv = new class_8681(this.field_45623.chunks.length);
            this.field_45625.set(lv.events);
            ArrayDeque<ChunkInfo> queue = Queues.newArrayDeque();
            this.method_52821(arg, queue);
            queue.forEach(arg2 -> arg.storage.field_45627.setInfo(arg2.chunk, (ChunkInfo)arg2));
            this.method_52825(lv.storage, arg2, queue, bl, arg -> {});
            this.field_45624.set(lv);
            this.field_45625.set(null);
            this.field_45626.set(true);
        });
    }

    private void method_52835(boolean bl, Frustum arg, List<ChunkBuilder.BuiltChunk> list, Vec3d arg22) {
        class_8681 lv = this.field_45624.get();
        this.method_52823(lv);
        if (!lv.events.sectionsToPropagateFrom.isEmpty()) {
            ArrayDeque<ChunkInfo> queue = Queues.newArrayDeque();
            while (!lv.events.sectionsToPropagateFrom.isEmpty()) {
                ChunkBuilder.BuiltChunk lv2 = (ChunkBuilder.BuiltChunk)lv.events.sectionsToPropagateFrom.poll();
                ChunkInfo lv3 = lv.storage.field_45627.getInfo(lv2);
                if (lv3 == null || lv3.chunk != lv2) continue;
                queue.add(lv3);
            }
            Frustum lv4 = WorldRenderer.method_52816(arg);
            Consumer<ChunkBuilder.BuiltChunk> consumer = arg2 -> {
                if (lv4.isVisible(arg2.getBoundingBox())) {
                    list.add((ChunkBuilder.BuiltChunk)arg2);
                }
            };
            this.method_52825(lv.storage, arg22, queue, bl, consumer);
        }
    }

    private void method_52823(class_8681 arg) {
        LongIterator longIterator = arg.events.chunksWhichReceivedNeighbors.iterator();
        while (longIterator.hasNext()) {
            long l = longIterator.nextLong();
            List list = (List)arg.storage.field_45628.get(l);
            if (list == null || !((ChunkBuilder.BuiltChunk)list.get(0)).shouldBuild()) continue;
            arg.events.sectionsToPropagateFrom.addAll(list);
            arg.storage.field_45628.remove(l);
        }
        arg.events.chunksWhichReceivedNeighbors.clear();
    }

    private void method_52822(class_8680 arg, ChunkPos arg2) {
        arg.chunksWhichReceivedNeighbors.add(ChunkPos.toLong(arg2.x - 1, arg2.z));
        arg.chunksWhichReceivedNeighbors.add(ChunkPos.toLong(arg2.x, arg2.z - 1));
        arg.chunksWhichReceivedNeighbors.add(ChunkPos.toLong(arg2.x + 1, arg2.z));
        arg.chunksWhichReceivedNeighbors.add(ChunkPos.toLong(arg2.x, arg2.z + 1));
    }

    private void method_52821(Camera arg, Queue<ChunkInfo> queue) {
        int i = 16;
        Vec3d lv = arg.getPos();
        BlockPos lv2 = arg.getBlockPos();
        ChunkBuilder.BuiltChunk lv3 = this.field_45623.getRenderedChunk(lv2);
        if (lv3 == null) {
            HeightLimitView lv4 = this.field_45623.getWorld();
            boolean bl = lv2.getY() > lv4.getBottomY();
            int j = bl ? lv4.getTopY() - 8 : lv4.getBottomY() + 8;
            int k = MathHelper.floor(lv.x / 16.0) * 16;
            int l = MathHelper.floor(lv.z / 16.0) * 16;
            int m = this.field_45623.getViewDistance();
            ArrayList<ChunkInfo> list = Lists.newArrayList();
            for (int n = -m; n <= m; ++n) {
                for (int o = -m; o <= m; ++o) {
                    ChunkBuilder.BuiltChunk lv5 = this.field_45623.getRenderedChunk(new BlockPos(k + ChunkSectionPos.getOffsetPos(n, 8), j, l + ChunkSectionPos.getOffsetPos(o, 8)));
                    if (lv5 == null || !this.method_52832(lv2, lv5.getOrigin())) continue;
                    Direction lv6 = bl ? Direction.DOWN : Direction.UP;
                    ChunkInfo lv7 = new ChunkInfo(lv5, lv6, 0);
                    lv7.updateCullingState(lv7.cullingState, lv6);
                    if (n > 0) {
                        lv7.updateCullingState(lv7.cullingState, Direction.EAST);
                    } else if (n < 0) {
                        lv7.updateCullingState(lv7.cullingState, Direction.WEST);
                    }
                    if (o > 0) {
                        lv7.updateCullingState(lv7.cullingState, Direction.SOUTH);
                    } else if (o < 0) {
                        lv7.updateCullingState(lv7.cullingState, Direction.NORTH);
                    }
                    list.add(lv7);
                }
            }
            list.sort(Comparator.comparingDouble(arg2 -> lv2.getSquaredDistance(arg2.chunk.getOrigin().add(8, 8, 8))));
            queue.addAll(list);
        } else {
            queue.add(new ChunkInfo(lv3, null, 0));
        }
    }

    private void method_52825(RenderableChunks arg, Vec3d arg2, Queue<ChunkInfo> queue, boolean bl, Consumer<ChunkBuilder.BuiltChunk> consumer) {
        int i = 16;
        BlockPos lv = new BlockPos(MathHelper.floor(arg2.x / 16.0) * 16, MathHelper.floor(arg2.y / 16.0) * 16, MathHelper.floor(arg2.z / 16.0) * 16);
        BlockPos lv2 = lv.add(8, 8, 8);
        while (!queue.isEmpty()) {
            ChunkInfo lv3 = queue.poll();
            ChunkBuilder.BuiltChunk lv4 = lv3.chunk;
            if (arg.chunks.add(lv3)) {
                consumer.accept(lv3.chunk);
            }
            boolean bl2 = Math.abs(lv4.getOrigin().getX() - lv.getX()) > 60 || Math.abs(lv4.getOrigin().getY() - lv.getY()) > 60 || Math.abs(lv4.getOrigin().getZ() - lv.getZ()) > 60;
            for (Direction lv5 : field_45618) {
                ChunkInfo lv14;
                ChunkBuilder.BuiltChunk lv6 = this.method_52831(lv, lv4, lv5);
                if (lv6 == null || bl && lv3.canCull(lv5.getOpposite())) continue;
                if (bl && lv3.hasAnyDirection()) {
                    ChunkBuilder.ChunkData lv7 = lv4.getData();
                    boolean bl3 = false;
                    for (int j = 0; j < field_45618.length; ++j) {
                        if (!lv3.hasDirection(j) || !lv7.isVisibleThrough(field_45618[j].getOpposite(), lv5)) continue;
                        bl3 = true;
                        break;
                    }
                    if (!bl3) continue;
                }
                if (bl && bl2) {
                    BlockPos lv8 = lv6.getOrigin();
                    BlockPos lv9 = lv8.add((lv5.getAxis() == Direction.Axis.X ? lv2.getX() > lv8.getX() : lv2.getX() < lv8.getX()) ? 16 : 0, (lv5.getAxis() == Direction.Axis.Y ? lv2.getY() > lv8.getY() : lv2.getY() < lv8.getY()) ? 16 : 0, (lv5.getAxis() == Direction.Axis.Z ? lv2.getZ() > lv8.getZ() : lv2.getZ() < lv8.getZ()) ? 16 : 0);
                    Vec3d lv10 = new Vec3d(lv9.getX(), lv9.getY(), lv9.getZ());
                    Vec3d lv11 = arg2.subtract(lv10).normalize().multiply(field_45620);
                    boolean bl4 = true;
                    while (arg2.subtract(lv10).lengthSquared() > 3600.0) {
                        lv10 = lv10.add(lv11);
                        HeightLimitView lv12 = this.field_45623.getWorld();
                        if (lv10.y > (double)lv12.getTopY() || lv10.y < (double)lv12.getBottomY()) break;
                        ChunkBuilder.BuiltChunk lv13 = this.field_45623.getRenderedChunk(BlockPos.ofFloored(lv10.x, lv10.y, lv10.z));
                        if (lv13 != null && arg.field_45627.getInfo(lv13) != null) continue;
                        bl4 = false;
                        break;
                    }
                    if (!bl4) continue;
                }
                if ((lv14 = arg.field_45627.getInfo(lv6)) != null) {
                    lv14.addDirection(lv5);
                    continue;
                }
                ChunkInfo lv15 = new ChunkInfo(lv6, lv5, lv3.propagationLevel + 1);
                lv15.updateCullingState(lv3.cullingState, lv5);
                if (lv6.shouldBuild()) {
                    queue.add(lv15);
                    arg.field_45627.setInfo(lv6, lv15);
                    continue;
                }
                if (!this.method_52832(lv, lv6.getOrigin())) continue;
                arg.field_45627.setInfo(lv6, lv15);
                arg.field_45628.computeIfAbsent(ChunkPos.toLong(lv6.getOrigin()), l -> new ArrayList()).add(lv6);
            }
        }
    }

    private boolean method_52832(BlockPos arg, BlockPos arg2) {
        int i = ChunkSectionPos.getSectionCoord(arg.getX());
        int j = ChunkSectionPos.getSectionCoord(arg.getZ());
        int k = ChunkSectionPos.getSectionCoord(arg2.getX());
        int l = ChunkSectionPos.getSectionCoord(arg2.getZ());
        return ChunkFilter.isWithinDistanceExcludingEdge(i, j, this.field_45623.getViewDistance(), k, l);
    }

    @Nullable
    private ChunkBuilder.BuiltChunk method_52831(BlockPos arg, ChunkBuilder.BuiltChunk arg2, Direction arg3) {
        BlockPos lv = arg2.getNeighborPosition(arg3);
        if (!this.method_52832(arg, lv)) {
            return null;
        }
        if (MathHelper.abs(arg.getY() - lv.getY()) > this.field_45623.getViewDistance() * 16) {
            return null;
        }
        return this.field_45623.getRenderedChunk(lv);
    }

    @Nullable
    @Debug
    protected ChunkInfo method_52837(ChunkBuilder.BuiltChunk arg) {
        return this.field_45624.get().storage.field_45627.getInfo(arg);
    }

    @Environment(value=EnvType.CLIENT)
    record class_8681(RenderableChunks storage, class_8680 events) {
        public class_8681(int i) {
            this(new RenderableChunks(i), new class_8680());
        }
    }

    @Environment(value=EnvType.CLIENT)
    static class RenderableChunks {
        public final ChunkInfoList field_45627;
        public final LinkedHashSet<ChunkInfo> chunks;
        public final Long2ObjectMap<List<ChunkBuilder.BuiltChunk>> field_45628;

        public RenderableChunks(int chunkCount) {
            this.field_45627 = new ChunkInfoList(chunkCount);
            this.chunks = new LinkedHashSet(chunkCount);
            this.field_45628 = new Long2ObjectOpenHashMap<List<ChunkBuilder.BuiltChunk>>();
        }
    }

    @Environment(value=EnvType.CLIENT)
    @Debug
    protected static class ChunkInfo {
        @Debug
        protected final ChunkBuilder.BuiltChunk chunk;
        private byte direction;
        byte cullingState;
        @Debug
        protected final int propagationLevel;

        ChunkInfo(ChunkBuilder.BuiltChunk chunk, @Nullable Direction direction, int propagationLevel) {
            this.chunk = chunk;
            if (direction != null) {
                this.addDirection(direction);
            }
            this.propagationLevel = propagationLevel;
        }

        void updateCullingState(byte parentCullingState, Direction from) {
            this.cullingState = (byte)(this.cullingState | (parentCullingState | 1 << from.ordinal()));
        }

        boolean canCull(Direction from) {
            return (this.cullingState & 1 << from.ordinal()) > 0;
        }

        void addDirection(Direction direction) {
            this.direction = (byte)(this.direction | (this.direction | 1 << direction.ordinal()));
        }

        @Debug
        protected boolean hasDirection(int ordinal) {
            return (this.direction & 1 << ordinal) > 0;
        }

        boolean hasAnyDirection() {
            return this.direction != 0;
        }

        public int hashCode() {
            return this.chunk.getOrigin().hashCode();
        }

        public boolean equals(Object o) {
            if (!(o instanceof ChunkInfo)) {
                return false;
            }
            ChunkInfo lv = (ChunkInfo)o;
            return this.chunk.getOrigin().equals(lv.chunk.getOrigin());
        }
    }

    @Environment(value=EnvType.CLIENT)
    record class_8680(LongSet chunksWhichReceivedNeighbors, BlockingQueue<ChunkBuilder.BuiltChunk> sectionsToPropagateFrom) {
        public class_8680() {
            this(new LongOpenHashSet(), new LinkedBlockingQueue<ChunkBuilder.BuiltChunk>());
        }
    }

    @Environment(value=EnvType.CLIENT)
    static class ChunkInfoList {
        private final ChunkInfo[] current;

        ChunkInfoList(int size) {
            this.current = new ChunkInfo[size];
        }

        public void setInfo(ChunkBuilder.BuiltChunk chunk, ChunkInfo info) {
            this.current[chunk.index] = info;
        }

        @Nullable
        public ChunkInfo getInfo(ChunkBuilder.BuiltChunk chunk) {
            int i = chunk.index;
            if (i < 0 || i >= this.current.length) {
                return null;
            }
            return this.current[i];
        }
    }
}

