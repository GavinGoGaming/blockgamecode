/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 * 
 * Could not load the following classes:
 *  org.jetbrains.annotations.Nullable
 */
package net.minecraft.util.crash;

import com.google.common.collect.Lists;
import java.util.List;
import java.util.Locale;
import net.minecraft.block.BlockState;
import net.minecraft.util.crash.CrashCallable;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkSectionPos;
import net.minecraft.world.HeightLimitView;
import org.jetbrains.annotations.Nullable;

public class CrashReportSection {
    private final String title;
    private final List<Element> elements = Lists.newArrayList();
    private StackTraceElement[] stackTrace = new StackTraceElement[0];

    public CrashReportSection(String title) {
        this.title = title;
    }

    public static String createPositionString(HeightLimitView world, double x, double y, double z) {
        return String.format(Locale.ROOT, "%.2f,%.2f,%.2f - %s", x, y, z, CrashReportSection.createPositionString(world, BlockPos.ofFloored(x, y, z)));
    }

    public static String createPositionString(HeightLimitView world, BlockPos pos) {
        return CrashReportSection.createPositionString(world, pos.getX(), pos.getY(), pos.getZ());
    }

    public static String createPositionString(HeightLimitView world, int x, int y, int z) {
        int w;
        int v;
        int u;
        int t;
        int s;
        int r;
        int q;
        int p;
        int o;
        int n;
        int m;
        StringBuilder stringBuilder = new StringBuilder();
        try {
            stringBuilder.append(String.format(Locale.ROOT, "World: (%d,%d,%d)", x, y, z));
        } catch (Throwable throwable) {
            stringBuilder.append("(Error finding world loc)");
        }
        stringBuilder.append(", ");
        try {
            int l = ChunkSectionPos.getSectionCoord(x);
            m = ChunkSectionPos.getSectionCoord(y);
            n = ChunkSectionPos.getSectionCoord(z);
            o = x & 0xF;
            p = y & 0xF;
            q = z & 0xF;
            r = ChunkSectionPos.getBlockCoord(l);
            s = world.getBottomY();
            t = ChunkSectionPos.getBlockCoord(n);
            u = ChunkSectionPos.getBlockCoord(l + 1) - 1;
            v = world.getTopY() - 1;
            w = ChunkSectionPos.getBlockCoord(n + 1) - 1;
            stringBuilder.append(String.format(Locale.ROOT, "Section: (at %d,%d,%d in %d,%d,%d; chunk contains blocks %d,%d,%d to %d,%d,%d)", o, p, q, l, m, n, r, s, t, u, v, w));
        } catch (Throwable throwable) {
            stringBuilder.append("(Error finding chunk loc)");
        }
        stringBuilder.append(", ");
        try {
            int l = x >> 9;
            m = z >> 9;
            n = l << 5;
            o = m << 5;
            p = (l + 1 << 5) - 1;
            q = (m + 1 << 5) - 1;
            r = l << 9;
            s = world.getBottomY();
            t = m << 9;
            u = (l + 1 << 9) - 1;
            v = world.getTopY() - 1;
            w = (m + 1 << 9) - 1;
            stringBuilder.append(String.format(Locale.ROOT, "Region: (%d,%d; contains chunks %d,%d to %d,%d, blocks %d,%d,%d to %d,%d,%d)", l, m, n, o, p, q, r, s, t, u, v, w));
        } catch (Throwable throwable) {
            stringBuilder.append("(Error finding world loc)");
        }
        return stringBuilder.toString();
    }

    public CrashReportSection add(String name, CrashCallable<String> callable) {
        try {
            this.add(name, callable.call());
        } catch (Throwable throwable) {
            this.add(name, throwable);
        }
        return this;
    }

    public CrashReportSection add(String name, Object detail) {
        this.elements.add(new Element(name, detail));
        return this;
    }

    public void add(String name, Throwable throwable) {
        this.add(name, (Object)throwable);
    }

    public int initStackTrace(int ignoredCallCount) {
        StackTraceElement[] stackTraceElements = Thread.currentThread().getStackTrace();
        if (stackTraceElements.length <= 0) {
            return 0;
        }
        this.stackTrace = new StackTraceElement[stackTraceElements.length - 3 - ignoredCallCount];
        System.arraycopy(stackTraceElements, 3 + ignoredCallCount, this.stackTrace, 0, this.stackTrace.length);
        return this.stackTrace.length;
    }

    public boolean shouldGenerateStackTrace(StackTraceElement prev, StackTraceElement next) {
        if (this.stackTrace.length == 0 || prev == null) {
            return false;
        }
        StackTraceElement stackTraceElement3 = this.stackTrace[0];
        if (!(stackTraceElement3.isNativeMethod() == prev.isNativeMethod() && stackTraceElement3.getClassName().equals(prev.getClassName()) && stackTraceElement3.getFileName().equals(prev.getFileName()) && stackTraceElement3.getMethodName().equals(prev.getMethodName()))) {
            return false;
        }
        if (next != null != this.stackTrace.length > 1) {
            return false;
        }
        if (next != null && !this.stackTrace[1].equals(next)) {
            return false;
        }
        this.stackTrace[0] = prev;
        return true;
    }

    public void trimStackTraceEnd(int callCount) {
        StackTraceElement[] stackTraceElements = new StackTraceElement[this.stackTrace.length - callCount];
        System.arraycopy(this.stackTrace, 0, stackTraceElements, 0, stackTraceElements.length);
        this.stackTrace = stackTraceElements;
    }

    public void addStackTrace(StringBuilder crashReportBuilder) {
        crashReportBuilder.append("-- ").append(this.title).append(" --\n");
        crashReportBuilder.append("Details:");
        for (Element lv : this.elements) {
            crashReportBuilder.append("\n\t");
            crashReportBuilder.append(lv.getName());
            crashReportBuilder.append(": ");
            crashReportBuilder.append(lv.getDetail());
        }
        if (this.stackTrace != null && this.stackTrace.length > 0) {
            crashReportBuilder.append("\nStacktrace:");
            for (StackTraceElement stackTraceElement : this.stackTrace) {
                crashReportBuilder.append("\n\tat ");
                crashReportBuilder.append(stackTraceElement);
            }
        }
    }

    public StackTraceElement[] getStackTrace() {
        return this.stackTrace;
    }

    public static void addBlockInfo(CrashReportSection element, HeightLimitView world, BlockPos pos, @Nullable BlockState state) {
        if (state != null) {
            element.add("Block", state::toString);
        }
        element.add("Block location", () -> CrashReportSection.createPositionString(world, pos));
    }

    static class Element {
        private final String name;
        private final String detail;

        public Element(String name, @Nullable Object detail) {
            this.name = name;
            if (detail == null) {
                this.detail = "~~NULL~~";
            } else if (detail instanceof Throwable) {
                Throwable throwable = (Throwable)detail;
                this.detail = "~~ERROR~~ " + throwable.getClass().getSimpleName() + ": " + throwable.getMessage();
            } else {
                this.detail = detail.toString();
            }
        }

        public String getName() {
            return this.name;
        }

        public String getDetail() {
            return this.detail;
        }
    }
}

