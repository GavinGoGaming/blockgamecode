/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 * 
 * Could not load the following classes:
 *  org.jetbrains.annotations.Nullable
 */
package net.minecraft.entity.data;

import com.mojang.logging.LogUtils;
import io.netty.handler.codec.DecoderException;
import io.netty.handler.codec.EncoderException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import net.minecraft.entity.data.DataTracked;
import net.minecraft.entity.data.TrackedData;
import net.minecraft.entity.data.TrackedDataHandler;
import net.minecraft.entity.data.TrackedDataHandlerRegistry;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.util.collection.Class2IntMap;
import org.apache.commons.lang3.ObjectUtils;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;

public class DataTracker {
    private static final Logger LOGGER = LogUtils.getLogger();
    private static final int MAX_DATA_VALUE_ID = 254;
    static final Class2IntMap CLASS_TO_LAST_ID = new Class2IntMap();
    private final DataTracked trackedEntity;
    private final Entry<?>[] entries;
    private boolean dirty;

    DataTracker(DataTracked trackedEntity, Entry<?>[] entries) {
        this.trackedEntity = trackedEntity;
        this.entries = entries;
    }

    public static <T> TrackedData<T> registerData(Class<? extends DataTracked> entityClass, TrackedDataHandler<T> dataHandler) {
        int i;
        if (LOGGER.isDebugEnabled()) {
            try {
                Class<?> class2 = Class.forName(Thread.currentThread().getStackTrace()[2].getClassName());
                if (!class2.equals(entityClass)) {
                    LOGGER.debug("defineId called for: {} from {}", entityClass, class2, new RuntimeException());
                }
            } catch (ClassNotFoundException class2) {
                // empty catch block
            }
        }
        if ((i = CLASS_TO_LAST_ID.put(entityClass)) > 254) {
            throw new IllegalArgumentException("Data value id is too big with " + i + "! (Max is 254)");
        }
        return dataHandler.create(i);
    }

    private <T> Entry<T> getEntry(TrackedData<T> key) {
        return this.entries[key.id()];
    }

    public <T> T get(TrackedData<T> data) {
        return this.getEntry(data).get();
    }

    public <T> void set(TrackedData<T> key, T value) {
        this.set(key, value, false);
    }

    public <T> void set(TrackedData<T> key, T value, boolean force) {
        Entry<T> lv = this.getEntry(key);
        if (force || ObjectUtils.notEqual(value, lv.get())) {
            lv.set(value);
            this.trackedEntity.onTrackedDataSet(key);
            lv.setDirty(true);
            this.dirty = true;
        }
    }

    public boolean isDirty() {
        return this.dirty;
    }

    @Nullable
    public List<SerializedEntry<?>> getDirtyEntries() {
        if (!this.dirty) {
            return null;
        }
        this.dirty = false;
        ArrayList list = new ArrayList();
        for (Entry<?> lv : this.entries) {
            if (!lv.isDirty()) continue;
            lv.setDirty(false);
            list.add(lv.toSerialized());
        }
        return list;
    }

    @Nullable
    public List<SerializedEntry<?>> getChangedEntries() {
        ArrayList list = null;
        for (Entry<?> lv : this.entries) {
            if (lv.isUnchanged()) continue;
            if (list == null) {
                list = new ArrayList();
            }
            list.add(lv.toSerialized());
        }
        return list;
    }

    public void writeUpdatedEntries(List<SerializedEntry<?>> entries) {
        for (SerializedEntry<?> lv : entries) {
            Entry<?> lv2 = this.entries[lv.id];
            this.copyToFrom(lv2, lv);
            this.trackedEntity.onTrackedDataSet(lv2.getData());
        }
        this.trackedEntity.onDataTrackerUpdate(entries);
    }

    private <T> void copyToFrom(Entry<T> to, SerializedEntry<?> from) {
        if (!Objects.equals(from.handler(), to.data.dataType())) {
            throw new IllegalStateException(String.format(Locale.ROOT, "Invalid entity data item type for field %d on entity %s: old=%s(%s), new=%s(%s)", to.data.id(), this.trackedEntity, to.value, to.value.getClass(), from.value, from.value.getClass()));
        }
        to.set(from.value);
    }

    public static class Entry<T> {
        final TrackedData<T> data;
        T value;
        private final T initialValue;
        private boolean dirty;

        public Entry(TrackedData<T> data, T value) {
            this.data = data;
            this.initialValue = value;
            this.value = value;
        }

        public TrackedData<T> getData() {
            return this.data;
        }

        public void set(T value) {
            this.value = value;
        }

        public T get() {
            return this.value;
        }

        public boolean isDirty() {
            return this.dirty;
        }

        public void setDirty(boolean dirty) {
            this.dirty = dirty;
        }

        public boolean isUnchanged() {
            return this.initialValue.equals(this.value);
        }

        public SerializedEntry<T> toSerialized() {
            return SerializedEntry.of(this.data, this.value);
        }
    }

    public record SerializedEntry<T>(int id, TrackedDataHandler<T> handler, T value) {
        public static <T> SerializedEntry<T> of(TrackedData<T> data, T value) {
            TrackedDataHandler<T> lv = data.dataType();
            return new SerializedEntry<T>(data.id(), lv, lv.copy(value));
        }

        public void write(RegistryByteBuf buf) {
            int i = TrackedDataHandlerRegistry.getId(this.handler);
            if (i < 0) {
                throw new EncoderException("Unknown serializer type " + String.valueOf(this.handler));
            }
            buf.writeByte(this.id);
            buf.writeVarInt(i);
            this.handler.codec().encode(buf, this.value);
        }

        public static SerializedEntry<?> fromBuf(RegistryByteBuf buf, int id) {
            int j = buf.readVarInt();
            TrackedDataHandler<?> lv = TrackedDataHandlerRegistry.get(j);
            if (lv == null) {
                throw new DecoderException("Unknown serializer type " + j);
            }
            return SerializedEntry.fromBuf(buf, id, lv);
        }

        private static <T> SerializedEntry<T> fromBuf(RegistryByteBuf buf, int id, TrackedDataHandler<T> handler) {
            return new SerializedEntry<T>(id, handler, handler.codec().decode(buf));
        }
    }

    public static class Builder {
        private final DataTracked entity;
        private final Entry<?>[] entries;

        public Builder(DataTracked entity) {
            this.entity = entity;
            this.entries = new Entry[CLASS_TO_LAST_ID.getNext(entity.getClass())];
        }

        public <T> Builder add(TrackedData<T> data, T value) {
            int i = data.id();
            if (i > this.entries.length) {
                throw new IllegalArgumentException("Data value id is too big with " + i + "! (Max is " + this.entries.length + ")");
            }
            if (this.entries[i] != null) {
                throw new IllegalArgumentException("Duplicate id value for " + i + "!");
            }
            if (TrackedDataHandlerRegistry.getId(data.dataType()) < 0) {
                throw new IllegalArgumentException("Unregistered serializer " + String.valueOf(data.dataType()) + " for " + i + "!");
            }
            this.entries[data.id()] = new Entry<T>(data, value);
            return this;
        }

        public DataTracker build() {
            for (int i = 0; i < this.entries.length; ++i) {
                if (this.entries[i] != null) continue;
                throw new IllegalStateException("Entity " + String.valueOf(this.entity.getClass()) + " has not defined synched data value " + i);
            }
            return new DataTracker(this.entity, this.entries);
        }
    }
}

