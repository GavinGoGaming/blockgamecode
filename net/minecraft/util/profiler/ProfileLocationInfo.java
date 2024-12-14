/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.util.profiler;

import it.unimi.dsi.fastutil.objects.Object2LongMap;

public interface ProfileLocationInfo {
    public long getTotalTime();

    public long getMaxTime();

    public long getVisitCount();

    public Object2LongMap<String> getCounts();
}

