/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.util.profiling.jfr.sample;

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import jdk.jfr.consumer.RecordedEvent;

public record GcHeapSummarySample(Instant time, long heapUsed, SummaryType summaryType) {
    public static GcHeapSummarySample fromEvent(RecordedEvent event) {
        return new GcHeapSummarySample(event.getStartTime(), event.getLong("heapUsed"), event.getString("when").equalsIgnoreCase("before gc") ? SummaryType.BEFORE_GC : SummaryType.AFTER_GC);
    }

    public static Statistics toStatistics(Duration duration, List<GcHeapSummarySample> samples, Duration gcDuration, int count) {
        return new Statistics(duration, gcDuration, count, GcHeapSummarySample.getAllocatedBytesPerSecond(samples));
    }

    private static double getAllocatedBytesPerSecond(List<GcHeapSummarySample> samples) {
        long l = 0L;
        Map<SummaryType, List<GcHeapSummarySample>> map = samples.stream().collect(Collectors.groupingBy(arg -> arg.summaryType));
        List<GcHeapSummarySample> list2 = map.get((Object)SummaryType.BEFORE_GC);
        List<GcHeapSummarySample> list3 = map.get((Object)SummaryType.AFTER_GC);
        for (int i = 1; i < list2.size(); ++i) {
            GcHeapSummarySample lv = list2.get(i);
            GcHeapSummarySample lv2 = list3.get(i - 1);
            l += lv.heapUsed - lv2.heapUsed;
        }
        Duration duration = Duration.between(samples.get((int)1).time, samples.get((int)(samples.size() - 1)).time);
        return (double)l / (double)duration.getSeconds();
    }

    static enum SummaryType {
        BEFORE_GC,
        AFTER_GC;

    }

    public record Statistics(Duration duration, Duration gcDuration, int count, double allocatedBytesPerSecond) {
        public float getGcDurationRatio() {
            return (float)this.gcDuration.toMillis() / (float)this.duration.toMillis();
        }
    }
}

