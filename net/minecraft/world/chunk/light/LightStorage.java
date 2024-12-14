/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 * 
 * Could not load the following classes:
 *  org.jetbrains.annotations.Nullable
 */
package net.minecraft.world.chunk.light;

import it.unimi.dsi.fastutil.longs.Long2ByteMap;
import it.unimi.dsi.fastutil.longs.Long2ByteOpenHashMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectMaps;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.longs.LongIterator;
import it.unimi.dsi.fastutil.longs.LongOpenHashSet;
import it.unimi.dsi.fastutil.longs.LongSet;
import it.unimi.dsi.fastutil.objects.ObjectIterator;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkSectionPos;
import net.minecraft.world.LightType;
import net.minecraft.world.chunk.ChunkNibbleArray;
import net.minecraft.world.chunk.ChunkProvider;
import net.minecraft.world.chunk.ChunkToNibbleArrayMap;
import net.minecraft.world.chunk.light.ChunkLightProvider;
import org.jetbrains.annotations.Nullable;

public abstract class LightStorage<M extends ChunkToNibbleArrayMap<M>> {
    private final LightType lightType;
    protected final ChunkProvider chunkProvider;
    protected final Long2ByteMap sectionPropagations = new Long2ByteOpenHashMap();
    private final LongSet enabledColumns = new LongOpenHashSet();
    protected volatile M uncachedStorage;
    protected final M storage;
    protected final LongSet dirtySections = new LongOpenHashSet();
    protected final LongSet notifySections = new LongOpenHashSet();
    protected final Long2ObjectMap<ChunkNibbleArray> queuedSections = Long2ObjectMaps.synchronize(new Long2ObjectOpenHashMap());
    private final LongSet columnsToRetain = new LongOpenHashSet();
    private final LongSet sectionsToRemove = new LongOpenHashSet();
    protected volatile boolean hasLightUpdates;

    protected LightStorage(LightType lightType, ChunkProvider chunkProvider, M lightData) {
        this.lightType = lightType;
        this.chunkProvider = chunkProvider;
        this.storage = lightData;
        this.uncachedStorage = ((ChunkToNibbleArrayMap)lightData).copy();
        ((ChunkToNibbleArrayMap)this.uncachedStorage).disableCache();
        this.sectionPropagations.defaultReturnValue((byte)0);
    }

    protected boolean hasSection(long sectionPos) {
        return this.getLightSection(sectionPos, true) != null;
    }

    @Nullable
    protected ChunkNibbleArray getLightSection(long sectionPos, boolean cached) {
        return this.getLightSection(cached ? this.storage : this.uncachedStorage, sectionPos);
    }

    @Nullable
    protected ChunkNibbleArray getLightSection(M storage, long sectionPos) {
        return ((ChunkToNibbleArrayMap)storage).get(sectionPos);
    }

    @Nullable
    protected ChunkNibbleArray method_51547(long sectionPos) {
        ChunkNibbleArray lv = ((ChunkToNibbleArrayMap)this.storage).get(sectionPos);
        if (lv == null) {
            return null;
        }
        if (this.dirtySections.add(sectionPos)) {
            lv = lv.copy();
            ((ChunkToNibbleArrayMap)this.storage).put(sectionPos, lv);
            ((ChunkToNibbleArrayMap)this.storage).clearCache();
        }
        return lv;
    }

    @Nullable
    public ChunkNibbleArray getLightSection(long sectionPos) {
        ChunkNibbleArray lv = (ChunkNibbleArray)this.queuedSections.get(sectionPos);
        if (lv != null) {
            return lv;
        }
        return this.getLightSection(sectionPos, false);
    }

    protected abstract int getLight(long var1);

    protected int get(long blockPos) {
        long m = ChunkSectionPos.fromBlockPos(blockPos);
        ChunkNibbleArray lv = this.getLightSection(m, true);
        return lv.get(ChunkSectionPos.getLocalCoord(BlockPos.unpackLongX(blockPos)), ChunkSectionPos.getLocalCoord(BlockPos.unpackLongY(blockPos)), ChunkSectionPos.getLocalCoord(BlockPos.unpackLongZ(blockPos)));
    }

    protected void set(long blockPos, int value) {
        long m = ChunkSectionPos.fromBlockPos(blockPos);
        ChunkNibbleArray lv = this.dirtySections.add(m) ? ((ChunkToNibbleArrayMap)this.storage).replaceWithCopy(m) : this.getLightSection(m, true);
        lv.set(ChunkSectionPos.getLocalCoord(BlockPos.unpackLongX(blockPos)), ChunkSectionPos.getLocalCoord(BlockPos.unpackLongY(blockPos)), ChunkSectionPos.getLocalCoord(BlockPos.unpackLongZ(blockPos)), value);
        ChunkSectionPos.forEachChunkSectionAround(blockPos, this.notifySections::add);
    }

    protected void addNotifySections(long id) {
        int i = ChunkSectionPos.unpackX(id);
        int j = ChunkSectionPos.unpackY(id);
        int k = ChunkSectionPos.unpackZ(id);
        for (int m = -1; m <= 1; ++m) {
            for (int n = -1; n <= 1; ++n) {
                for (int o = -1; o <= 1; ++o) {
                    this.notifySections.add(ChunkSectionPos.asLong(i + n, j + o, k + m));
                }
            }
        }
    }

    protected ChunkNibbleArray createSection(long sectionPos) {
        ChunkNibbleArray lv = (ChunkNibbleArray)this.queuedSections.get(sectionPos);
        if (lv != null) {
            return lv;
        }
        return new ChunkNibbleArray();
    }

    protected boolean hasLightUpdates() {
        return this.hasLightUpdates;
    }

    protected void updateLight(ChunkLightProvider<M, ?> lightProvider) {
        ChunkNibbleArray lv2;
        long l;
        if (!this.hasLightUpdates) {
            return;
        }
        this.hasLightUpdates = false;
        LongIterator longIterator = this.sectionsToRemove.iterator();
        while (longIterator.hasNext()) {
            l = (Long)longIterator.next();
            ChunkNibbleArray lv = (ChunkNibbleArray)this.queuedSections.remove(l);
            lv2 = ((ChunkToNibbleArrayMap)this.storage).removeChunk(l);
            if (!this.columnsToRetain.contains(ChunkSectionPos.withZeroY(l))) continue;
            if (lv != null) {
                this.queuedSections.put(l, lv);
                continue;
            }
            if (lv2 == null) continue;
            this.queuedSections.put(l, lv2);
        }
        ((ChunkToNibbleArrayMap)this.storage).clearCache();
        longIterator = this.sectionsToRemove.iterator();
        while (longIterator.hasNext()) {
            l = (Long)longIterator.next();
            this.onUnloadSection(l);
            this.dirtySections.add(l);
        }
        this.sectionsToRemove.clear();
        ObjectIterator<Long2ObjectMap.Entry<ChunkNibbleArray>> objectIterator = Long2ObjectMaps.fastIterator(this.queuedSections);
        while (objectIterator.hasNext()) {
            Long2ObjectMap.Entry entry = (Long2ObjectMap.Entry)objectIterator.next();
            long m = entry.getLongKey();
            if (!this.hasSection(m)) continue;
            lv2 = (ChunkNibbleArray)entry.getValue();
            if (((ChunkToNibbleArrayMap)this.storage).get(m) != lv2) {
                ((ChunkToNibbleArrayMap)this.storage).put(m, lv2);
                this.dirtySections.add(m);
            }
            objectIterator.remove();
        }
        ((ChunkToNibbleArrayMap)this.storage).clearCache();
    }

    protected void onLoadSection(long sectionPos) {
    }

    protected void onUnloadSection(long sectionPos) {
    }

    protected void setColumnEnabled(long columnPos, boolean enabled) {
        if (enabled) {
            this.enabledColumns.add(columnPos);
        } else {
            this.enabledColumns.remove(columnPos);
        }
    }

    protected boolean isSectionInEnabledColumn(long sectionPos) {
        long m = ChunkSectionPos.withZeroY(sectionPos);
        return this.enabledColumns.contains(m);
    }

    public void setRetainColumn(long sectionPos, boolean retain) {
        if (retain) {
            this.columnsToRetain.add(sectionPos);
        } else {
            this.columnsToRetain.remove(sectionPos);
        }
    }

    protected void enqueueSectionData(long sectionPos, @Nullable ChunkNibbleArray array) {
        if (array != null) {
            this.queuedSections.put(sectionPos, array);
            this.hasLightUpdates = true;
        } else {
            this.queuedSections.remove(sectionPos);
        }
    }

    protected void setSectionStatus(long sectionPos, boolean notReady) {
        byte c;
        byte b = this.sectionPropagations.get(sectionPos);
        if (b == (c = PropagationFlags.setReady(b, !notReady))) {
            return;
        }
        this.setSectionPropagation(sectionPos, c);
        int i = notReady ? -1 : 1;
        for (int j = -1; j <= 1; ++j) {
            for (int k = -1; k <= 1; ++k) {
                for (int m = -1; m <= 1; ++m) {
                    if (j == 0 && k == 0 && m == 0) continue;
                    long n = ChunkSectionPos.offset(sectionPos, j, k, m);
                    byte d = this.sectionPropagations.get(n);
                    this.setSectionPropagation(n, PropagationFlags.withNeighborCount(d, PropagationFlags.getNeighborCount(d) + i));
                }
            }
        }
    }

    protected void setSectionPropagation(long sectionPos, byte flags) {
        if (flags != 0) {
            if (this.sectionPropagations.put(sectionPos, flags) == 0) {
                this.queueForUpdate(sectionPos);
            }
        } else if (this.sectionPropagations.remove(sectionPos) != 0) {
            this.queueForRemoval(sectionPos);
        }
    }

    private void queueForUpdate(long sectionPos) {
        if (!this.sectionsToRemove.remove(sectionPos)) {
            ((ChunkToNibbleArrayMap)this.storage).put(sectionPos, this.createSection(sectionPos));
            this.dirtySections.add(sectionPos);
            this.onLoadSection(sectionPos);
            this.addNotifySections(sectionPos);
            this.hasLightUpdates = true;
        }
    }

    private void queueForRemoval(long sectionPos) {
        this.sectionsToRemove.add(sectionPos);
        this.hasLightUpdates = true;
    }

    protected void notifyChanges() {
        if (!this.dirtySections.isEmpty()) {
            Object lv = ((ChunkToNibbleArrayMap)this.storage).copy();
            ((ChunkToNibbleArrayMap)lv).disableCache();
            this.uncachedStorage = lv;
            this.dirtySections.clear();
        }
        if (!this.notifySections.isEmpty()) {
            LongIterator longIterator = this.notifySections.iterator();
            while (longIterator.hasNext()) {
                long l = longIterator.nextLong();
                this.chunkProvider.onLightUpdate(this.lightType, ChunkSectionPos.from(l));
            }
            this.notifySections.clear();
        }
    }

    public Status getStatus(long sectionPos) {
        return PropagationFlags.getStatus(this.sectionPropagations.get(sectionPos));
    }

    protected static class PropagationFlags {
        public static final byte field_44719 = 0;
        private static final int MIN_NEIGHBOR_COUNT = 0;
        private static final int MAX_NEIGHBOR_COUNT = 26;
        private static final byte field_44722 = 32;
        private static final byte NEIGHBOR_COUNT_MASK = 31;

        protected PropagationFlags() {
        }

        public static byte setReady(byte packed, boolean ready) {
            return (byte)(ready ? packed | 0x20 : packed & 0xFFFFFFDF);
        }

        public static byte withNeighborCount(byte packed, int neighborCount) {
            if (neighborCount < 0 || neighborCount > 26) {
                throw new IllegalArgumentException("Neighbor count was not within range [0; 26]");
            }
            return (byte)(packed & 0xFFFFFFE0 | neighborCount & 0x1F);
        }

        public static boolean isReady(byte packed) {
            return (packed & 0x20) != 0;
        }

        public static int getNeighborCount(byte packed) {
            return packed & 0x1F;
        }

        public static Status getStatus(byte packed) {
            if (packed == 0) {
                return Status.EMPTY;
            }
            if (PropagationFlags.isReady(packed)) {
                return Status.LIGHT_AND_DATA;
            }
            return Status.LIGHT_ONLY;
        }
    }

    public static enum Status {
        EMPTY("2"),
        LIGHT_ONLY("1"),
        LIGHT_AND_DATA("0");

        private final String sigil;

        private Status(String sigil) {
            this.sigil = sigil;
        }

        public String getSigil() {
            return this.sigil;
        }
    }
}

