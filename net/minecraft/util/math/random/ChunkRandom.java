/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.util.math.random;

import java.util.function.LongFunction;
import net.minecraft.util.math.random.CheckedRandom;
import net.minecraft.util.math.random.Random;
import net.minecraft.util.math.random.RandomSplitter;
import net.minecraft.util.math.random.Xoroshiro128PlusPlusRandom;

public class ChunkRandom
extends CheckedRandom {
    private final Random baseRandom;
    private int sampleCount;

    public ChunkRandom(Random baseRandom) {
        super(0L);
        this.baseRandom = baseRandom;
    }

    public int getSampleCount() {
        return this.sampleCount;
    }

    @Override
    public Random split() {
        return this.baseRandom.split();
    }

    @Override
    public RandomSplitter nextSplitter() {
        return this.baseRandom.nextSplitter();
    }

    @Override
    public int next(int bits) {
        ++this.sampleCount;
        Random random = this.baseRandom;
        if (random instanceof CheckedRandom) {
            CheckedRandom lv = (CheckedRandom)random;
            return lv.next(bits);
        }
        return (int)(this.baseRandom.nextLong() >>> 64 - bits);
    }

    @Override
    public synchronized void setSeed(long seed) {
        if (this.baseRandom == null) {
            return;
        }
        this.baseRandom.setSeed(seed);
    }

    public long setPopulationSeed(long worldSeed, int blockX, int blockZ) {
        this.setSeed(worldSeed);
        long m = this.nextLong() | 1L;
        long n = this.nextLong() | 1L;
        long o = (long)blockX * m + (long)blockZ * n ^ worldSeed;
        this.setSeed(o);
        return o;
    }

    public void setDecoratorSeed(long populationSeed, int index, int step) {
        long m = populationSeed + (long)index + (long)(10000 * step);
        this.setSeed(m);
    }

    public void setCarverSeed(long worldSeed, int chunkX, int chunkZ) {
        this.setSeed(worldSeed);
        long m = this.nextLong();
        long n = this.nextLong();
        long o = (long)chunkX * m ^ (long)chunkZ * n ^ worldSeed;
        this.setSeed(o);
    }

    public void setRegionSeed(long worldSeed, int regionX, int regionZ, int salt) {
        long m = (long)regionX * 341873128712L + (long)regionZ * 132897987541L + worldSeed + (long)salt;
        this.setSeed(m);
    }

    public static Random getSlimeRandom(int chunkX, int chunkZ, long worldSeed, long scrambler) {
        return Random.create(worldSeed + (long)(chunkX * chunkX * 4987142) + (long)(chunkX * 5947611) + (long)(chunkZ * chunkZ) * 4392871L + (long)(chunkZ * 389711) ^ scrambler);
    }

    public static enum RandomProvider {
        LEGACY(CheckedRandom::new),
        XOROSHIRO(Xoroshiro128PlusPlusRandom::new);

        private final LongFunction<Random> provider;

        private RandomProvider(LongFunction<Random> provider) {
            this.provider = provider;
        }

        public Random create(long seed) {
            return this.provider.apply(seed);
        }
    }
}

