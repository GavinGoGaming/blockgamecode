/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 * 
 * Could not load the following classes:
 *  org.jetbrains.annotations.Nullable
 */
package net.minecraft.util.profiling.jfr.sample;

import java.time.Duration;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import net.minecraft.util.math.Quantiles;
import net.minecraft.util.profiling.jfr.sample.LongRunningSample;
import org.jetbrains.annotations.Nullable;

public record LongRunningSampleStatistics<T extends LongRunningSample>(T fastestSample, T slowestSample, @Nullable T secondSlowestSample, int count, Map<Integer, Double> quantiles, Duration totalDuration) {
    public static <T extends LongRunningSample> LongRunningSampleStatistics<T> fromSamples(List<T> samples) {
        if (samples.isEmpty()) {
            throw new IllegalArgumentException("No values");
        }
        List<LongRunningSample> list2 = samples.stream().sorted(Comparator.comparing(LongRunningSample::duration)).toList();
        Duration duration = list2.stream().map(LongRunningSample::duration).reduce(Duration::plus).orElse(Duration.ZERO);
        LongRunningSample lv = list2.get(0);
        LongRunningSample lv2 = list2.get(list2.size() - 1);
        LongRunningSample lv3 = list2.size() > 1 ? list2.get(list2.size() - 2) : null;
        int i = list2.size();
        Map<Integer, Double> map = Quantiles.create(list2.stream().mapToLong(sample -> sample.duration().toNanos()).toArray());
        return new LongRunningSampleStatistics<LongRunningSample>(lv, lv2, lv3, i, map, duration);
    }

    @Nullable
    public T secondSlowestSample() {
        return this.secondSlowestSample;
    }
}

