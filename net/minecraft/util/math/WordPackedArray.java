/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.util.math;

import net.minecraft.util.math.MathHelper;
import org.apache.commons.lang3.Validate;

public class WordPackedArray {
    private static final int BIT_TO_LONG_INDEX_SHIFT = 6;
    private final long[] array;
    private final int unitSize;
    private final long maxValue;
    private final int length;

    public WordPackedArray(int unitSize, int length) {
        this(unitSize, length, new long[MathHelper.roundUpToMultiple(length * unitSize, 64) / 64]);
    }

    public WordPackedArray(int unitSize, int length, long[] array) {
        Validate.inclusiveBetween(1L, 32L, unitSize);
        this.length = length;
        this.unitSize = unitSize;
        this.array = array;
        this.maxValue = (1L << unitSize) - 1L;
        int k = MathHelper.roundUpToMultiple(length * unitSize, 64) / 64;
        if (array.length != k) {
            throw new IllegalArgumentException("Invalid length given for storage, got: " + array.length + " but expected: " + k);
        }
    }

    public void set(int index, int value) {
        Validate.inclusiveBetween(0L, this.length - 1, index);
        Validate.inclusiveBetween(0L, this.maxValue, value);
        int k = index * this.unitSize;
        int l = k >> 6;
        int m = (index + 1) * this.unitSize - 1 >> 6;
        int n = k ^ l << 6;
        this.array[l] = this.array[l] & (this.maxValue << n ^ 0xFFFFFFFFFFFFFFFFL) | ((long)value & this.maxValue) << n;
        if (l != m) {
            int o = 64 - n;
            int p = this.unitSize - o;
            this.array[m] = this.array[m] >>> p << p | ((long)value & this.maxValue) >> o;
        }
    }

    public int get(int index) {
        Validate.inclusiveBetween(0L, this.length - 1, index);
        int j = index * this.unitSize;
        int k = j >> 6;
        int l = (index + 1) * this.unitSize - 1 >> 6;
        int m = j ^ k << 6;
        if (k == l) {
            return (int)(this.array[k] >>> m & this.maxValue);
        }
        int n = 64 - m;
        return (int)((this.array[k] >>> m | this.array[l] << n) & this.maxValue);
    }

    public long[] getAlignedArray() {
        return this.array;
    }

    public int getUnitSize() {
        return this.unitSize;
    }
}

