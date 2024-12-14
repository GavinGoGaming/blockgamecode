/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.util.collection;

import it.unimi.dsi.fastutil.HashCommon;
import it.unimi.dsi.fastutil.longs.Long2LongLinkedOpenHashMap;
import it.unimi.dsi.fastutil.longs.LongLinkedOpenHashSet;
import java.util.NoSuchElementException;
import net.minecraft.util.math.MathHelper;

public class LinkedBlockPosHashSet
extends LongLinkedOpenHashSet {
    private final Storage buffer;

    public LinkedBlockPosHashSet(int expectedSize, float loadFactor) {
        super(expectedSize, loadFactor);
        this.buffer = new Storage(expectedSize / 64, loadFactor);
    }

    @Override
    public boolean add(long posLong) {
        return this.buffer.add(posLong);
    }

    @Override
    public boolean rem(long posLong) {
        return this.buffer.rem(posLong);
    }

    @Override
    public long removeFirstLong() {
        return this.buffer.removeFirstLong();
    }

    @Override
    public int size() {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean isEmpty() {
        return this.buffer.isEmpty();
    }

    protected static class Storage
    extends Long2LongLinkedOpenHashMap {
        private static final int STARTING_OFFSET = MathHelper.floorLog2(60000000);
        private static final int HORIZONTAL_COLUMN_BIT_SEPARATION = MathHelper.floorLog2(60000000);
        private static final int FIELD_SPACING;
        private static final int Y_BIT_OFFSET = 0;
        private static final int X_BIT_OFFSET;
        private static final int Z_BIT_OFFSET;
        private static final long MAX_POSITION;
        private int lastWrittenIndex = -1;
        private long lastWrittenKey;
        private final int expectedSize;

        public Storage(int expectedSize, float loadFactor) {
            super(expectedSize, loadFactor);
            this.expectedSize = expectedSize;
        }

        static long getKey(long posLong) {
            return posLong & (MAX_POSITION ^ 0xFFFFFFFFFFFFFFFFL);
        }

        static int getBlockOffset(long posLong) {
            int i = (int)(posLong >>> Z_BIT_OFFSET & 3L);
            int j = (int)(posLong >>> 0 & 3L);
            int k = (int)(posLong >>> X_BIT_OFFSET & 3L);
            return i << 4 | k << 2 | j;
        }

        static long getBlockPosLong(long key, int valueLength) {
            key |= (long)(valueLength >>> 4 & 3) << Z_BIT_OFFSET;
            key |= (long)(valueLength >>> 2 & 3) << X_BIT_OFFSET;
            return key |= (long)(valueLength >>> 0 & 3) << 0;
        }

        public boolean add(long posLong) {
            int j;
            long m = Storage.getKey(posLong);
            int i = Storage.getBlockOffset(posLong);
            long n = 1L << i;
            if (m == 0L) {
                if (this.containsNullKey) {
                    return this.setBits(this.n, n);
                }
                this.containsNullKey = true;
                j = this.n;
            } else {
                if (this.lastWrittenIndex != -1 && m == this.lastWrittenKey) {
                    return this.setBits(this.lastWrittenIndex, n);
                }
                long[] ls = this.key;
                j = (int)HashCommon.mix(m) & this.mask;
                long o = ls[j];
                while (o != 0L) {
                    if (o == m) {
                        this.lastWrittenIndex = j;
                        this.lastWrittenKey = m;
                        return this.setBits(j, n);
                    }
                    j = j + 1 & this.mask;
                    o = ls[j];
                }
            }
            this.key[j] = m;
            this.value[j] = n;
            if (this.size == 0) {
                this.first = this.last = j;
                this.link[j] = -1L;
            } else {
                int n2 = this.last;
                this.link[n2] = this.link[n2] ^ (this.link[this.last] ^ (long)j & 0xFFFFFFFFL) & 0xFFFFFFFFL;
                this.link[j] = ((long)this.last & 0xFFFFFFFFL) << 32 | 0xFFFFFFFFL;
                this.last = j;
            }
            if (this.size++ >= this.maxFill) {
                this.rehash(HashCommon.arraySize(this.size + 1, this.f));
            }
            return false;
        }

        private boolean setBits(int index, long mask) {
            boolean bl = (this.value[index] & mask) != 0L;
            int n = index;
            this.value[n] = this.value[n] | mask;
            return bl;
        }

        public boolean rem(long posLong) {
            long m = Storage.getKey(posLong);
            int i = Storage.getBlockOffset(posLong);
            long n = 1L << i;
            if (m == 0L) {
                if (this.containsNullKey) {
                    return this.unsetBits(n);
                }
                return false;
            }
            if (this.lastWrittenIndex != -1 && m == this.lastWrittenKey) {
                return this.unsetBitsAt(this.lastWrittenIndex, n);
            }
            long[] ls = this.key;
            int j = (int)HashCommon.mix(m) & this.mask;
            long o = ls[j];
            while (o != 0L) {
                if (m == o) {
                    this.lastWrittenIndex = j;
                    this.lastWrittenKey = m;
                    return this.unsetBitsAt(j, n);
                }
                j = j + 1 & this.mask;
                o = ls[j];
            }
            return false;
        }

        private boolean unsetBits(long mask) {
            if ((this.value[this.n] & mask) == 0L) {
                return false;
            }
            int n = this.n;
            this.value[n] = this.value[n] & (mask ^ 0xFFFFFFFFFFFFFFFFL);
            if (this.value[this.n] != 0L) {
                return true;
            }
            this.containsNullKey = false;
            --this.size;
            this.fixPointers(this.n);
            if (this.size < this.maxFill / 4 && this.n > 16) {
                this.rehash(this.n / 2);
            }
            return true;
        }

        private boolean unsetBitsAt(int index, long mask) {
            if ((this.value[index] & mask) == 0L) {
                return false;
            }
            int n = index;
            this.value[n] = this.value[n] & (mask ^ 0xFFFFFFFFFFFFFFFFL);
            if (this.value[index] != 0L) {
                return true;
            }
            this.lastWrittenIndex = -1;
            --this.size;
            this.fixPointers(index);
            this.shiftKeys(index);
            if (this.size < this.maxFill / 4 && this.n > 16) {
                this.rehash(this.n / 2);
            }
            return true;
        }

        @Override
        public long removeFirstLong() {
            if (this.size == 0) {
                throw new NoSuchElementException();
            }
            int i = this.first;
            long l = this.key[i];
            int j = Long.numberOfTrailingZeros(this.value[i]);
            int n = i;
            this.value[n] = this.value[n] & (1L << j ^ 0xFFFFFFFFFFFFFFFFL);
            if (this.value[i] == 0L) {
                this.removeFirstLong();
                this.lastWrittenIndex = -1;
            }
            return Storage.getBlockPosLong(l, j);
        }

        @Override
        protected void rehash(int newN) {
            if (newN > this.expectedSize) {
                super.rehash(newN);
            }
        }

        static {
            X_BIT_OFFSET = FIELD_SPACING = 64 - STARTING_OFFSET - HORIZONTAL_COLUMN_BIT_SEPARATION;
            Z_BIT_OFFSET = FIELD_SPACING + HORIZONTAL_COLUMN_BIT_SEPARATION;
            MAX_POSITION = 3L << Z_BIT_OFFSET | 3L | 3L << X_BIT_OFFSET;
        }
    }
}

