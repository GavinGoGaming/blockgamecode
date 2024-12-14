/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 * 
 * Could not load the following classes:
 *  org.jetbrains.annotations.Nullable
 */
package net.minecraft.util.collection;

import com.google.common.base.Predicates;
import com.google.common.collect.Iterators;
import java.util.Arrays;
import java.util.Iterator;
import net.minecraft.util.collection.IndexedIterable;
import net.minecraft.util.math.MathHelper;
import org.jetbrains.annotations.Nullable;

public class Int2ObjectBiMap<K>
implements IndexedIterable<K> {
    private static final int ABSENT = -1;
    private static final Object EMPTY = null;
    private static final float LOAD_FACTOR = 0.8f;
    private K[] values;
    private int[] ids;
    private K[] idToValues;
    private int nextId;
    private int size;

    private Int2ObjectBiMap(int size) {
        this.values = new Object[size];
        this.ids = new int[size];
        this.idToValues = new Object[size];
    }

    private Int2ObjectBiMap(K[] values, int[] ids, K[] idToValues, int nextId, int size) {
        this.values = values;
        this.ids = ids;
        this.idToValues = idToValues;
        this.nextId = nextId;
        this.size = size;
    }

    public static <A> Int2ObjectBiMap<A> create(int expectedSize) {
        return new Int2ObjectBiMap((int)((float)expectedSize / 0.8f));
    }

    @Override
    public int getRawId(@Nullable K value) {
        return this.getIdFromIndex(this.findIndex(value, this.getIdealIndex(value)));
    }

    @Override
    @Nullable
    public K get(int index) {
        if (index < 0 || index >= this.idToValues.length) {
            return null;
        }
        return this.idToValues[index];
    }

    private int getIdFromIndex(int index) {
        if (index == -1) {
            return -1;
        }
        return this.ids[index];
    }

    public boolean contains(K value) {
        return this.getRawId(value) != -1;
    }

    public boolean containsKey(int index) {
        return this.get(index) != null;
    }

    public int add(K value) {
        int i = this.nextId();
        this.put(value, i);
        return i;
    }

    private int nextId() {
        while (this.nextId < this.idToValues.length && this.idToValues[this.nextId] != null) {
            ++this.nextId;
        }
        return this.nextId;
    }

    private void resize(int newSize) {
        K[] objects = this.values;
        int[] is = this.ids;
        Int2ObjectBiMap<K> lv = new Int2ObjectBiMap<K>(newSize);
        for (int j = 0; j < objects.length; ++j) {
            if (objects[j] == null) continue;
            lv.put(objects[j], is[j]);
        }
        this.values = lv.values;
        this.ids = lv.ids;
        this.idToValues = lv.idToValues;
        this.nextId = lv.nextId;
        this.size = lv.size;
    }

    public void put(K value, int id) {
        int k;
        int j = Math.max(id, this.size + 1);
        if ((float)j >= (float)this.values.length * 0.8f) {
            for (k = this.values.length << 1; k < id; k <<= 1) {
            }
            this.resize(k);
        }
        k = this.findFree(this.getIdealIndex(value));
        this.values[k] = value;
        this.ids[k] = id;
        this.idToValues[id] = value;
        ++this.size;
        if (id == this.nextId) {
            ++this.nextId;
        }
    }

    private int getIdealIndex(@Nullable K value) {
        return (MathHelper.idealHash(System.identityHashCode(value)) & Integer.MAX_VALUE) % this.values.length;
    }

    private int findIndex(@Nullable K value, int id) {
        int j;
        for (j = id; j < this.values.length; ++j) {
            if (this.values[j] == value) {
                return j;
            }
            if (this.values[j] != EMPTY) continue;
            return -1;
        }
        for (j = 0; j < id; ++j) {
            if (this.values[j] == value) {
                return j;
            }
            if (this.values[j] != EMPTY) continue;
            return -1;
        }
        return -1;
    }

    private int findFree(int size) {
        int j;
        for (j = size; j < this.values.length; ++j) {
            if (this.values[j] != EMPTY) continue;
            return j;
        }
        for (j = 0; j < size; ++j) {
            if (this.values[j] != EMPTY) continue;
            return j;
        }
        throw new RuntimeException("Overflowed :(");
    }

    @Override
    public Iterator<K> iterator() {
        return Iterators.filter(Iterators.forArray(this.idToValues), Predicates.notNull());
    }

    public void clear() {
        Arrays.fill(this.values, null);
        Arrays.fill(this.idToValues, null);
        this.nextId = 0;
        this.size = 0;
    }

    @Override
    public int size() {
        return this.size;
    }

    public Int2ObjectBiMap<K> copy() {
        return new Int2ObjectBiMap<Object>((Object[])this.values.clone(), (int[])this.ids.clone(), (Object[])this.idToValues.clone(), this.nextId, this.size);
    }
}

