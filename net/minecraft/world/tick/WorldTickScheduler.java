/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.world.tick;

import it.unimi.dsi.fastutil.longs.Long2LongMap;
import it.unimi.dsi.fastutil.longs.Long2LongMaps;
import it.unimi.dsi.fastutil.longs.Long2LongOpenHashMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectIterator;
import it.unimi.dsi.fastutil.objects.ObjectOpenCustomHashSet;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.LongSummaryStatistics;
import java.util.PriorityQueue;
import java.util.Queue;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.LongPredicate;
import java.util.function.Predicate;
import java.util.function.Supplier;
import net.minecraft.util.Util;
import net.minecraft.util.math.BlockBox;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.ChunkSectionPos;
import net.minecraft.util.math.Vec3i;
import net.minecraft.util.profiler.Profiler;
import net.minecraft.world.tick.ChunkTickScheduler;
import net.minecraft.world.tick.OrderedTick;
import net.minecraft.world.tick.QueryableTickScheduler;
import net.minecraft.world.tick.TickScheduler;

public class WorldTickScheduler<T>
implements QueryableTickScheduler<T> {
    private static final Comparator<ChunkTickScheduler<?>> COMPARATOR = (a, b) -> OrderedTick.BASIC_COMPARATOR.compare(a.peekNextTick(), b.peekNextTick());
    private final LongPredicate tickingFutureReadyPredicate;
    private final Supplier<Profiler> profilerGetter;
    private final Long2ObjectMap<ChunkTickScheduler<T>> chunkTickSchedulers = new Long2ObjectOpenHashMap<ChunkTickScheduler<T>>();
    private final Long2LongMap nextTriggerTickByChunkPos = Util.make(new Long2LongOpenHashMap(), map -> map.defaultReturnValue(Long.MAX_VALUE));
    private final Queue<ChunkTickScheduler<T>> tickableChunkTickSchedulers = new PriorityQueue(COMPARATOR);
    private final Queue<OrderedTick<T>> tickableTicks = new ArrayDeque<OrderedTick<T>>();
    private final List<OrderedTick<T>> tickedTicks = new ArrayList<OrderedTick<T>>();
    private final Set<OrderedTick<?>> copiedTickableTicksList = new ObjectOpenCustomHashSet(OrderedTick.HASH_STRATEGY);
    private final BiConsumer<ChunkTickScheduler<T>, OrderedTick<T>> queuedTickConsumer = (chunkTickScheduler, tick) -> {
        if (tick.equals(chunkTickScheduler.peekNextTick())) {
            this.schedule((OrderedTick<T>)tick);
        }
    };

    public WorldTickScheduler(LongPredicate tickingFutureReadyPredicate, Supplier<Profiler> profilerGetter) {
        this.tickingFutureReadyPredicate = tickingFutureReadyPredicate;
        this.profilerGetter = profilerGetter;
    }

    public void addChunkTickScheduler(ChunkPos pos, ChunkTickScheduler<T> scheduler) {
        long l = pos.toLong();
        this.chunkTickSchedulers.put(l, scheduler);
        OrderedTick<T> lv = scheduler.peekNextTick();
        if (lv != null) {
            this.nextTriggerTickByChunkPos.put(l, lv.triggerTick());
        }
        scheduler.setTickConsumer(this.queuedTickConsumer);
    }

    public void removeChunkTickScheduler(ChunkPos pos) {
        long l = pos.toLong();
        ChunkTickScheduler lv = (ChunkTickScheduler)this.chunkTickSchedulers.remove(l);
        this.nextTriggerTickByChunkPos.remove(l);
        if (lv != null) {
            lv.setTickConsumer(null);
        }
    }

    @Override
    public void scheduleTick(OrderedTick<T> orderedTick) {
        long l = ChunkPos.toLong(orderedTick.pos());
        ChunkTickScheduler lv = (ChunkTickScheduler)this.chunkTickSchedulers.get(l);
        if (lv == null) {
            Util.throwOrPause(new IllegalStateException("Trying to schedule tick in not loaded position " + String.valueOf(orderedTick.pos())));
            return;
        }
        lv.scheduleTick(orderedTick);
    }

    public void tick(long time, int maxTicks, BiConsumer<BlockPos, T> ticker) {
        Profiler lv = this.profilerGetter.get();
        lv.push("collect");
        this.collectTickableTicks(time, maxTicks, lv);
        lv.swap("run");
        lv.visit("ticksToRun", this.tickableTicks.size());
        this.tick(ticker);
        lv.swap("cleanup");
        this.clear();
        lv.pop();
    }

    private void collectTickableTicks(long time, int maxTicks, Profiler profiler) {
        this.collectTickableChunkTickSchedulers(time);
        profiler.visit("containersToTick", this.tickableChunkTickSchedulers.size());
        this.addTickableTicks(time, maxTicks);
        this.delayAllTicks();
    }

    private void collectTickableChunkTickSchedulers(long time) {
        ObjectIterator<Long2LongMap.Entry> objectIterator = Long2LongMaps.fastIterator(this.nextTriggerTickByChunkPos);
        while (objectIterator.hasNext()) {
            Long2LongMap.Entry entry = (Long2LongMap.Entry)objectIterator.next();
            long m = entry.getLongKey();
            long n = entry.getLongValue();
            if (n > time) continue;
            ChunkTickScheduler lv = (ChunkTickScheduler)this.chunkTickSchedulers.get(m);
            if (lv == null) {
                objectIterator.remove();
                continue;
            }
            OrderedTick lv2 = lv.peekNextTick();
            if (lv2 == null) {
                objectIterator.remove();
                continue;
            }
            if (lv2.triggerTick() > time) {
                entry.setValue(lv2.triggerTick());
                continue;
            }
            if (!this.tickingFutureReadyPredicate.test(m)) continue;
            objectIterator.remove();
            this.tickableChunkTickSchedulers.add(lv);
        }
    }

    private void addTickableTicks(long time, int maxTicks) {
        ChunkTickScheduler<T> lv;
        while (this.isTickableTicksCountUnder(maxTicks) && (lv = this.tickableChunkTickSchedulers.poll()) != null) {
            OrderedTick<T> lv2 = lv.pollNextTick();
            this.addTickableTick(lv2);
            this.addTickableTicks(this.tickableChunkTickSchedulers, lv, time, maxTicks);
            OrderedTick<T> lv3 = lv.peekNextTick();
            if (lv3 == null) continue;
            if (lv3.triggerTick() <= time && this.isTickableTicksCountUnder(maxTicks)) {
                this.tickableChunkTickSchedulers.add(lv);
                continue;
            }
            this.schedule(lv3);
        }
    }

    private void delayAllTicks() {
        for (ChunkTickScheduler chunkTickScheduler : this.tickableChunkTickSchedulers) {
            this.schedule(chunkTickScheduler.peekNextTick());
        }
    }

    private void schedule(OrderedTick<T> tick) {
        this.nextTriggerTickByChunkPos.put(ChunkPos.toLong(tick.pos()), tick.triggerTick());
    }

    private void addTickableTicks(Queue<ChunkTickScheduler<T>> tickableChunkTickSchedulers, ChunkTickScheduler<T> chunkTickScheduler, long tick, int maxTicks) {
        OrderedTick<T> lv3;
        OrderedTick<T> lv2;
        if (!this.isTickableTicksCountUnder(maxTicks)) {
            return;
        }
        ChunkTickScheduler<T> lv = tickableChunkTickSchedulers.peek();
        OrderedTick<T> orderedTick = lv2 = lv != null ? lv.peekNextTick() : null;
        while (this.isTickableTicksCountUnder(maxTicks) && (lv3 = chunkTickScheduler.peekNextTick()) != null && lv3.triggerTick() <= tick && (lv2 == null || OrderedTick.BASIC_COMPARATOR.compare(lv3, lv2) <= 0)) {
            chunkTickScheduler.pollNextTick();
            this.addTickableTick(lv3);
        }
    }

    private void addTickableTick(OrderedTick<T> tick) {
        this.tickableTicks.add(tick);
    }

    private boolean isTickableTicksCountUnder(int maxTicks) {
        return this.tickableTicks.size() < maxTicks;
    }

    private void tick(BiConsumer<BlockPos, T> ticker) {
        while (!this.tickableTicks.isEmpty()) {
            OrderedTick<T> lv = this.tickableTicks.poll();
            if (!this.copiedTickableTicksList.isEmpty()) {
                this.copiedTickableTicksList.remove(lv);
            }
            this.tickedTicks.add(lv);
            ticker.accept(lv.pos(), (BlockPos)lv.type());
        }
    }

    private void clear() {
        this.tickableTicks.clear();
        this.tickableChunkTickSchedulers.clear();
        this.tickedTicks.clear();
        this.copiedTickableTicksList.clear();
    }

    @Override
    public boolean isQueued(BlockPos pos, T type) {
        ChunkTickScheduler lv = (ChunkTickScheduler)this.chunkTickSchedulers.get(ChunkPos.toLong(pos));
        return lv != null && lv.isQueued(pos, type);
    }

    @Override
    public boolean isTicking(BlockPos pos, T type) {
        this.copyTickableTicksList();
        return this.copiedTickableTicksList.contains(OrderedTick.create(type, pos));
    }

    private void copyTickableTicksList() {
        if (this.copiedTickableTicksList.isEmpty() && !this.tickableTicks.isEmpty()) {
            this.copiedTickableTicksList.addAll(this.tickableTicks);
        }
    }

    private void visitChunks(BlockBox box, ChunkVisitor<T> visitor) {
        int i = ChunkSectionPos.getSectionCoord((double)box.getMinX());
        int j = ChunkSectionPos.getSectionCoord((double)box.getMinZ());
        int k = ChunkSectionPos.getSectionCoord((double)box.getMaxX());
        int l = ChunkSectionPos.getSectionCoord((double)box.getMaxZ());
        for (int m = i; m <= k; ++m) {
            for (int n = j; n <= l; ++n) {
                long o = ChunkPos.toLong(m, n);
                ChunkTickScheduler lv = (ChunkTickScheduler)this.chunkTickSchedulers.get(o);
                if (lv == null) continue;
                visitor.accept(o, lv);
            }
        }
    }

    public void clearNextTicks(BlockBox box) {
        Predicate<OrderedTick> predicate = tick -> box.contains(tick.pos());
        this.visitChunks(box, (chunkPos, chunkTickScheduler) -> {
            OrderedTick lv = chunkTickScheduler.peekNextTick();
            chunkTickScheduler.removeTicksIf(predicate);
            OrderedTick lv2 = chunkTickScheduler.peekNextTick();
            if (lv2 != lv) {
                if (lv2 != null) {
                    this.schedule(lv2);
                } else {
                    this.nextTriggerTickByChunkPos.remove(chunkPos);
                }
            }
        });
        this.tickedTicks.removeIf(predicate);
        this.tickableTicks.removeIf(predicate);
    }

    public void scheduleTicks(BlockBox box, Vec3i offset) {
        this.scheduleTicks(this, box, offset);
    }

    public void scheduleTicks(WorldTickScheduler<T> scheduler, BlockBox box, Vec3i offset) {
        ArrayList list = new ArrayList();
        Predicate<OrderedTick> predicate = tick -> box.contains(tick.pos());
        scheduler.tickedTicks.stream().filter(predicate).forEach(list::add);
        scheduler.tickableTicks.stream().filter(predicate).forEach(list::add);
        scheduler.visitChunks(box, (chunkPos, chunkTickScheduler) -> chunkTickScheduler.getQueueAsStream().filter(predicate).forEach(list::add));
        LongSummaryStatistics longSummaryStatistics = list.stream().mapToLong(OrderedTick::subTickOrder).summaryStatistics();
        long l = longSummaryStatistics.getMin();
        long m = longSummaryStatistics.getMax();
        list.forEach(tick -> this.scheduleTick(new OrderedTick(tick.type(), tick.pos().add(offset), tick.triggerTick(), tick.priority(), tick.subTickOrder() - l + m + 1L)));
    }

    @Override
    public int getTickCount() {
        return this.chunkTickSchedulers.values().stream().mapToInt(TickScheduler::getTickCount).sum();
    }

    @FunctionalInterface
    static interface ChunkVisitor<T> {
        public void accept(long var1, ChunkTickScheduler<T> var3);
    }
}

