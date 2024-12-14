/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.util.math.noise;

import it.unimi.dsi.fastutil.ints.IntRBTreeSet;
import it.unimi.dsi.fastutil.ints.IntSortedSet;
import java.util.List;
import net.minecraft.util.math.noise.SimplexNoiseSampler;
import net.minecraft.util.math.random.CheckedRandom;
import net.minecraft.util.math.random.ChunkRandom;
import net.minecraft.util.math.random.Random;

public class OctaveSimplexNoiseSampler {
    private final SimplexNoiseSampler[] octaveSamplers;
    private final double persistence;
    private final double lacunarity;

    public OctaveSimplexNoiseSampler(Random random, List<Integer> octaves) {
        this(random, new IntRBTreeSet(octaves));
    }

    private OctaveSimplexNoiseSampler(Random random, IntSortedSet octaves) {
        int j;
        if (octaves.isEmpty()) {
            throw new IllegalArgumentException("Need some octaves!");
        }
        int i = -octaves.firstInt();
        int k = i + (j = octaves.lastInt()) + 1;
        if (k < 1) {
            throw new IllegalArgumentException("Total number of octaves needs to be >= 1");
        }
        SimplexNoiseSampler lv = new SimplexNoiseSampler(random);
        int l = j;
        this.octaveSamplers = new SimplexNoiseSampler[k];
        if (l >= 0 && l < k && octaves.contains(0)) {
            this.octaveSamplers[l] = lv;
        }
        for (int m = l + 1; m < k; ++m) {
            if (m >= 0 && octaves.contains(l - m)) {
                this.octaveSamplers[m] = new SimplexNoiseSampler(random);
                continue;
            }
            random.skip(262);
        }
        if (j > 0) {
            long n = (long)(lv.sample(lv.originX, lv.originY, lv.originZ) * 9.223372036854776E18);
            ChunkRandom lv2 = new ChunkRandom(new CheckedRandom(n));
            for (int o = l - 1; o >= 0; --o) {
                if (o < k && octaves.contains(l - o)) {
                    this.octaveSamplers[o] = new SimplexNoiseSampler(lv2);
                    continue;
                }
                lv2.skip(262);
            }
        }
        this.lacunarity = Math.pow(2.0, j);
        this.persistence = 1.0 / (Math.pow(2.0, k) - 1.0);
    }

    public double sample(double x, double y, boolean useOrigin) {
        double f = 0.0;
        double g = this.lacunarity;
        double h = this.persistence;
        for (SimplexNoiseSampler lv : this.octaveSamplers) {
            if (lv != null) {
                f += lv.sample(x * g + (useOrigin ? lv.originX : 0.0), y * g + (useOrigin ? lv.originY : 0.0)) * h;
            }
            g /= 2.0;
            h *= 2.0;
        }
        return f;
    }
}

