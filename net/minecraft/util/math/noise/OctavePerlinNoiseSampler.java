/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 * 
 * Could not load the following classes:
 *  org.jetbrains.annotations.Nullable
 */
package net.minecraft.util.math.noise;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.ImmutableList;
import com.mojang.datafixers.util.Pair;
import it.unimi.dsi.fastutil.doubles.DoubleArrayList;
import it.unimi.dsi.fastutil.doubles.DoubleList;
import it.unimi.dsi.fastutil.ints.IntBidirectionalIterator;
import it.unimi.dsi.fastutil.ints.IntRBTreeSet;
import it.unimi.dsi.fastutil.ints.IntSortedSet;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.stream.IntStream;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.noise.PerlinNoiseSampler;
import net.minecraft.util.math.random.Random;
import net.minecraft.util.math.random.RandomSplitter;
import org.jetbrains.annotations.Nullable;

public class OctavePerlinNoiseSampler {
    private static final int field_31704 = 0x2000000;
    private final PerlinNoiseSampler[] octaveSamplers;
    private final int firstOctave;
    private final DoubleList amplitudes;
    private final double persistence;
    private final double lacunarity;
    private final double maxValue;

    @Deprecated
    public static OctavePerlinNoiseSampler createLegacy(Random random, IntStream octaves) {
        return new OctavePerlinNoiseSampler(random, OctavePerlinNoiseSampler.calculateAmplitudes(new IntRBTreeSet(octaves.boxed().collect(ImmutableList.toImmutableList()))), false);
    }

    @Deprecated
    public static OctavePerlinNoiseSampler createLegacy(Random random, int offset, DoubleList amplitudes) {
        return new OctavePerlinNoiseSampler(random, Pair.of(offset, amplitudes), false);
    }

    public static OctavePerlinNoiseSampler create(Random random, IntStream octaves) {
        return OctavePerlinNoiseSampler.create(random, octaves.boxed().collect(ImmutableList.toImmutableList()));
    }

    public static OctavePerlinNoiseSampler create(Random random, List<Integer> octaves) {
        return new OctavePerlinNoiseSampler(random, OctavePerlinNoiseSampler.calculateAmplitudes(new IntRBTreeSet(octaves)), true);
    }

    public static OctavePerlinNoiseSampler create(Random random, int offset, double firstAmplitude, double ... amplitudes) {
        DoubleArrayList doubleArrayList = new DoubleArrayList(amplitudes);
        doubleArrayList.add(0, firstAmplitude);
        return new OctavePerlinNoiseSampler(random, Pair.of(offset, doubleArrayList), true);
    }

    public static OctavePerlinNoiseSampler create(Random random, int offset, DoubleList amplitudes) {
        return new OctavePerlinNoiseSampler(random, Pair.of(offset, amplitudes), true);
    }

    private static Pair<Integer, DoubleList> calculateAmplitudes(IntSortedSet octaves) {
        int j;
        if (octaves.isEmpty()) {
            throw new IllegalArgumentException("Need some octaves!");
        }
        int i = -octaves.firstInt();
        int k = i + (j = octaves.lastInt()) + 1;
        if (k < 1) {
            throw new IllegalArgumentException("Total number of octaves needs to be >= 1");
        }
        DoubleArrayList doubleList = new DoubleArrayList(new double[k]);
        IntBidirectionalIterator intBidirectionalIterator = octaves.iterator();
        while (intBidirectionalIterator.hasNext()) {
            int l = intBidirectionalIterator.nextInt();
            doubleList.set(l + i, 1.0);
        }
        return Pair.of(-i, doubleList);
    }

    protected OctavePerlinNoiseSampler(Random random, Pair<Integer, DoubleList> firstOctaveAndAmplitudes, boolean xoroshiro) {
        this.firstOctave = firstOctaveAndAmplitudes.getFirst();
        this.amplitudes = firstOctaveAndAmplitudes.getSecond();
        int i = this.amplitudes.size();
        int j = -this.firstOctave;
        this.octaveSamplers = new PerlinNoiseSampler[i];
        if (xoroshiro) {
            RandomSplitter lv = random.nextSplitter();
            for (int k = 0; k < i; ++k) {
                if (this.amplitudes.getDouble(k) == 0.0) continue;
                int l = this.firstOctave + k;
                this.octaveSamplers[k] = new PerlinNoiseSampler(lv.split("octave_" + l));
            }
        } else {
            double d;
            PerlinNoiseSampler lv2 = new PerlinNoiseSampler(random);
            if (j >= 0 && j < i && (d = this.amplitudes.getDouble(j)) != 0.0) {
                this.octaveSamplers[j] = lv2;
            }
            for (int k = j - 1; k >= 0; --k) {
                if (k < i) {
                    double e = this.amplitudes.getDouble(k);
                    if (e != 0.0) {
                        this.octaveSamplers[k] = new PerlinNoiseSampler(random);
                        continue;
                    }
                    OctavePerlinNoiseSampler.skipCalls(random);
                    continue;
                }
                OctavePerlinNoiseSampler.skipCalls(random);
            }
            if (Arrays.stream(this.octaveSamplers).filter(Objects::nonNull).count() != this.amplitudes.stream().filter(amplitude -> amplitude != 0.0).count()) {
                throw new IllegalStateException("Failed to create correct number of noise levels for given non-zero amplitudes");
            }
            if (j < i - 1) {
                throw new IllegalArgumentException("Positive octaves are temporarily disabled");
            }
        }
        this.lacunarity = Math.pow(2.0, -j);
        this.persistence = Math.pow(2.0, i - 1) / (Math.pow(2.0, i) - 1.0);
        this.maxValue = this.getTotalAmplitude(2.0);
    }

    protected double getMaxValue() {
        return this.maxValue;
    }

    private static void skipCalls(Random random) {
        random.skip(262);
    }

    public double sample(double x, double y, double z) {
        return this.sample(x, y, z, 0.0, 0.0, false);
    }

    @Deprecated
    public double sample(double x, double y, double z, double yScale, double yMax, boolean useOrigin) {
        double i = 0.0;
        double j = this.lacunarity;
        double k = this.persistence;
        for (int l = 0; l < this.octaveSamplers.length; ++l) {
            PerlinNoiseSampler lv = this.octaveSamplers[l];
            if (lv != null) {
                double m = lv.sample(OctavePerlinNoiseSampler.maintainPrecision(x * j), useOrigin ? -lv.originY : OctavePerlinNoiseSampler.maintainPrecision(y * j), OctavePerlinNoiseSampler.maintainPrecision(z * j), yScale * j, yMax * j);
                i += this.amplitudes.getDouble(l) * m * k;
            }
            j *= 2.0;
            k /= 2.0;
        }
        return i;
    }

    public double method_40556(double d) {
        return this.getTotalAmplitude(d + 2.0);
    }

    private double getTotalAmplitude(double scale) {
        double e = 0.0;
        double f = this.persistence;
        for (int i = 0; i < this.octaveSamplers.length; ++i) {
            PerlinNoiseSampler lv = this.octaveSamplers[i];
            if (lv != null) {
                e += this.amplitudes.getDouble(i) * scale * f;
            }
            f /= 2.0;
        }
        return e;
    }

    @Nullable
    public PerlinNoiseSampler getOctave(int octave) {
        return this.octaveSamplers[this.octaveSamplers.length - 1 - octave];
    }

    public static double maintainPrecision(double value) {
        return value - (double)MathHelper.lfloor(value / 3.3554432E7 + 0.5) * 3.3554432E7;
    }

    protected int getFirstOctave() {
        return this.firstOctave;
    }

    protected DoubleList getAmplitudes() {
        return this.amplitudes;
    }

    @VisibleForTesting
    public void addDebugInfo(StringBuilder info) {
        info.append("PerlinNoise{");
        List<String> list = this.amplitudes.stream().map(double_ -> String.format(Locale.ROOT, "%.2f", double_)).toList();
        info.append("first octave: ").append(this.firstOctave).append(", amplitudes: ").append(list).append(", noise levels: [");
        for (int i = 0; i < this.octaveSamplers.length; ++i) {
            info.append(i).append(": ");
            PerlinNoiseSampler lv = this.octaveSamplers[i];
            if (lv == null) {
                info.append("null");
            } else {
                lv.addDebugInfo(info);
            }
            info.append(", ");
        }
        info.append("]");
        info.append("}");
    }
}

