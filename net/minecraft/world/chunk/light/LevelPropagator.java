/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.world.chunk.light;

import it.unimi.dsi.fastutil.longs.Long2ByteMap;
import it.unimi.dsi.fastutil.longs.Long2ByteOpenHashMap;
import it.unimi.dsi.fastutil.longs.LongArrayList;
import java.util.function.LongPredicate;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.chunk.light.PendingUpdateQueue;

public abstract class LevelPropagator {
    public static final long field_43397 = Long.MAX_VALUE;
    private static final int MAX_LEVEL = 255;
    protected final int levelCount;
    private final PendingUpdateQueue pendingUpdateQueue;
    private final Long2ByteMap pendingUpdates;
    private volatile boolean hasPendingUpdates;

    protected LevelPropagator(int levelCount, int expectedLevelSize, final int expectedTotalSize) {
        if (levelCount >= 254) {
            throw new IllegalArgumentException("Level count must be < 254.");
        }
        this.levelCount = levelCount;
        this.pendingUpdateQueue = new PendingUpdateQueue(levelCount, expectedLevelSize);
        this.pendingUpdates = new Long2ByteOpenHashMap(expectedTotalSize, 0.5f){

            @Override
            protected void rehash(int newN) {
                if (newN > expectedTotalSize) {
                    super.rehash(newN);
                }
            }
        };
        this.pendingUpdates.defaultReturnValue((byte)-1);
    }

    protected void removePendingUpdate(long id) {
        int i = this.pendingUpdates.remove(id) & 0xFF;
        if (i == 255) {
            return;
        }
        int j = this.getLevel(id);
        int k = this.calculateLevel(j, i);
        this.pendingUpdateQueue.remove(id, k, this.levelCount);
        this.hasPendingUpdates = !this.pendingUpdateQueue.isEmpty();
    }

    public void removePendingUpdateIf(LongPredicate predicate) {
        LongArrayList longList = new LongArrayList();
        this.pendingUpdates.keySet().forEach(l -> {
            if (predicate.test(l)) {
                longList.add(l);
            }
        });
        longList.forEach(this::removePendingUpdate);
    }

    private int calculateLevel(int a, int b) {
        return Math.min(Math.min(a, b), this.levelCount - 1);
    }

    protected void resetLevel(long id) {
        this.updateLevel(id, id, this.levelCount - 1, false);
    }

    protected void updateLevel(long sourceId, long id, int level, boolean decrease) {
        this.updateLevel(sourceId, id, level, this.getLevel(id), this.pendingUpdates.get(id) & 0xFF, decrease);
        this.hasPendingUpdates = !this.pendingUpdateQueue.isEmpty();
    }

    private void updateLevel(long sourceId, long id, int level, int currentLevel, int k, boolean decrease) {
        boolean bl2;
        if (this.isMarker(id)) {
            return;
        }
        level = MathHelper.clamp(level, 0, this.levelCount - 1);
        currentLevel = MathHelper.clamp(currentLevel, 0, this.levelCount - 1);
        boolean bl = bl2 = k == 255;
        if (bl2) {
            k = currentLevel;
        }
        int n = decrease ? Math.min(k, level) : MathHelper.clamp(this.recalculateLevel(id, sourceId, level), 0, this.levelCount - 1);
        int o = this.calculateLevel(currentLevel, k);
        if (currentLevel != n) {
            int p = this.calculateLevel(currentLevel, n);
            if (o != p && !bl2) {
                this.pendingUpdateQueue.remove(id, o, p);
            }
            this.pendingUpdateQueue.enqueue(id, p);
            this.pendingUpdates.put(id, (byte)n);
        } else if (!bl2) {
            this.pendingUpdateQueue.remove(id, o, this.levelCount);
            this.pendingUpdates.remove(id);
        }
    }

    protected final void propagateLevel(long sourceId, long targetId, int level, boolean decrease) {
        int j = this.pendingUpdates.get(targetId) & 0xFF;
        int k = MathHelper.clamp(this.getPropagatedLevel(sourceId, targetId, level), 0, this.levelCount - 1);
        if (decrease) {
            this.updateLevel(sourceId, targetId, k, this.getLevel(targetId), j, decrease);
        } else {
            boolean bl2 = j == 255;
            int n = bl2 ? MathHelper.clamp(this.getLevel(targetId), 0, this.levelCount - 1) : j;
            if (k == n) {
                this.updateLevel(sourceId, targetId, this.levelCount - 1, bl2 ? n : this.getLevel(targetId), j, decrease);
            }
        }
    }

    protected final boolean hasPendingUpdates() {
        return this.hasPendingUpdates;
    }

    protected final int applyPendingUpdates(int maxSteps) {
        if (this.pendingUpdateQueue.isEmpty()) {
            return maxSteps;
        }
        while (!this.pendingUpdateQueue.isEmpty() && maxSteps > 0) {
            --maxSteps;
            long l = this.pendingUpdateQueue.dequeue();
            int j = MathHelper.clamp(this.getLevel(l), 0, this.levelCount - 1);
            int k = this.pendingUpdates.remove(l) & 0xFF;
            if (k < j) {
                this.setLevel(l, k);
                this.propagateLevel(l, k, true);
                continue;
            }
            if (k <= j) continue;
            this.setLevel(l, this.levelCount - 1);
            if (k != this.levelCount - 1) {
                this.pendingUpdateQueue.enqueue(l, this.calculateLevel(this.levelCount - 1, k));
                this.pendingUpdates.put(l, (byte)k);
            }
            this.propagateLevel(l, j, false);
        }
        this.hasPendingUpdates = !this.pendingUpdateQueue.isEmpty();
        return maxSteps;
    }

    public int getPendingUpdateCount() {
        return this.pendingUpdates.size();
    }

    protected boolean isMarker(long id) {
        return id == Long.MAX_VALUE;
    }

    protected abstract int recalculateLevel(long var1, long var3, int var5);

    protected abstract void propagateLevel(long var1, int var3, boolean var4);

    protected abstract int getLevel(long var1);

    protected abstract void setLevel(long var1, int var3);

    protected abstract int getPropagatedLevel(long var1, long var3, int var5);
}

