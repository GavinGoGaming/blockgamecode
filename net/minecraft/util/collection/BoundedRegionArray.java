/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.util.collection;

import java.util.Locale;
import java.util.function.Consumer;

public class BoundedRegionArray<T> {
    private final int minX;
    private final int minZ;
    private final int maxX;
    private final int maxZ;
    private final Object[] array;

    public static <T> BoundedRegionArray<T> create(int centerX, int centerZ, int radius, Getter<T> getter) {
        int l = centerX - radius;
        int m = centerZ - radius;
        int n = 2 * radius + 1;
        return new BoundedRegionArray<T>(l, m, n, n, getter);
    }

    private BoundedRegionArray(int minX, int minZ, int maxX, int maxZ, Getter<T> getter) {
        this.minX = minX;
        this.minZ = minZ;
        this.maxX = maxX;
        this.maxZ = maxZ;
        this.array = new Object[this.maxX * this.maxZ];
        for (int m = minX; m < minX + maxX; ++m) {
            for (int n = minZ; n < minZ + maxZ; ++n) {
                this.array[this.toIndex((int)m, (int)n)] = getter.get(m, n);
            }
        }
    }

    public void forEach(Consumer<T> callback) {
        for (Object object : this.array) {
            callback.accept(object);
        }
    }

    public T get(int x, int z) {
        if (!this.isWithinBounds(x, z)) {
            throw new IllegalArgumentException("Requested out of range value (" + x + "," + z + ") from " + String.valueOf(this));
        }
        return (T)this.array[this.toIndex(x, z)];
    }

    public boolean isWithinBounds(int x, int z) {
        int k = x - this.minX;
        int l = z - this.minZ;
        return k >= 0 && k < this.maxX && l >= 0 && l < this.maxZ;
    }

    public String toString() {
        return String.format(Locale.ROOT, "StaticCache2D[%d, %d, %d, %d]", this.minX, this.minZ, this.minX + this.maxX, this.minZ + this.maxZ);
    }

    private int toIndex(int x, int z) {
        int k = x - this.minX;
        int l = z - this.minZ;
        return k * this.maxZ + l;
    }

    @FunctionalInterface
    public static interface Getter<T> {
        public T get(int var1, int var2);
    }
}

