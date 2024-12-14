/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.util.math.random;

import io.netty.util.internal.ThreadLocalRandom;
import net.minecraft.util.math.random.CheckedRandom;
import net.minecraft.util.math.random.LocalRandom;
import net.minecraft.util.math.random.RandomSeed;
import net.minecraft.util.math.random.RandomSplitter;
import net.minecraft.util.math.random.ThreadSafeRandom;

public interface Random {
    @Deprecated
    public static final double field_38930 = 2.297;

    public static Random create() {
        return Random.create(RandomSeed.getSeed());
    }

    @Deprecated
    public static Random createThreadSafe() {
        return new ThreadSafeRandom(RandomSeed.getSeed());
    }

    public static Random create(long seed) {
        return new CheckedRandom(seed);
    }

    public static Random createLocal() {
        return new LocalRandom(ThreadLocalRandom.current().nextLong());
    }

    public Random split();

    public RandomSplitter nextSplitter();

    public void setSeed(long var1);

    public int nextInt();

    public int nextInt(int var1);

    default public int nextBetween(int min, int max) {
        return this.nextInt(max - min + 1) + min;
    }

    public long nextLong();

    public boolean nextBoolean();

    public float nextFloat();

    public double nextDouble();

    public double nextGaussian();

    default public double nextTriangular(double mode, double deviation) {
        return mode + deviation * (this.nextDouble() - this.nextDouble());
    }

    default public void skip(int count) {
        for (int j = 0; j < count; ++j) {
            this.nextInt();
        }
    }

    default public int nextBetweenExclusive(int min, int max) {
        if (min >= max) {
            throw new IllegalArgumentException("bound - origin is non positive");
        }
        return min + this.nextInt(max - min);
    }
}

