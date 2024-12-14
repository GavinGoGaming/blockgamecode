/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.util.shape;

import it.unimi.dsi.fastutil.doubles.DoubleArrayList;
import it.unimi.dsi.fastutil.doubles.DoubleList;
import it.unimi.dsi.fastutil.doubles.DoubleLists;
import net.minecraft.util.shape.PairList;

public class SimplePairList
implements PairList {
    private static final DoubleList ZERO = DoubleLists.unmodifiable(DoubleArrayList.wrap(new double[]{0.0}));
    private final double[] valueIndices;
    private final int[] minValues;
    private final int[] maxValues;
    private final int size;

    public SimplePairList(DoubleList first, DoubleList second, boolean includeFirstOnly, boolean includeSecondOnly) {
        double d = Double.NaN;
        int i = first.size();
        int j = second.size();
        int k = i + j;
        this.valueIndices = new double[k];
        this.minValues = new int[k];
        this.maxValues = new int[k];
        boolean bl3 = !includeFirstOnly;
        boolean bl4 = !includeSecondOnly;
        int l = 0;
        int m = 0;
        int n = 0;
        while (true) {
            double e;
            boolean bl7;
            boolean bl6;
            boolean bl5 = m >= i;
            boolean bl = bl6 = n >= j;
            if (bl5 && bl6) break;
            boolean bl2 = bl7 = !bl5 && (bl6 || first.getDouble(m) < second.getDouble(n) + 1.0E-7);
            if (bl7) {
                ++m;
                if (bl3 && (n == 0 || bl6)) {
                    continue;
                }
            } else {
                ++n;
                if (bl4 && (m == 0 || bl5)) continue;
            }
            int o = m - 1;
            int p = n - 1;
            double d2 = e = bl7 ? first.getDouble(o) : second.getDouble(p);
            if (!(d >= e - 1.0E-7)) {
                this.minValues[l] = o;
                this.maxValues[l] = p;
                this.valueIndices[l] = e;
                ++l;
                d = e;
                continue;
            }
            this.minValues[l - 1] = o;
            this.maxValues[l - 1] = p;
        }
        this.size = Math.max(1, l);
    }

    @Override
    public boolean forEachPair(PairList.Consumer predicate) {
        int i = this.size - 1;
        for (int j = 0; j < i; ++j) {
            if (predicate.merge(this.minValues[j], this.maxValues[j], j)) continue;
            return false;
        }
        return true;
    }

    @Override
    public int size() {
        return this.size;
    }

    @Override
    public DoubleList getPairs() {
        return this.size <= 1 ? ZERO : DoubleArrayList.wrap(this.valueIndices, this.size);
    }
}

