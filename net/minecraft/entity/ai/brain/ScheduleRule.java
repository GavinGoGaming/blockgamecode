/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.entity.ai.brain;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import it.unimi.dsi.fastutil.ints.Int2ObjectAVLTreeMap;
import java.util.Collection;
import java.util.List;
import net.minecraft.entity.ai.brain.ScheduleRuleEntry;

public class ScheduleRule {
    private final List<ScheduleRuleEntry> entries = Lists.newArrayList();
    private int prioritizedEntryIndex;

    public ImmutableList<ScheduleRuleEntry> getEntries() {
        return ImmutableList.copyOf(this.entries);
    }

    public ScheduleRule add(int startTime, float priority) {
        this.entries.add(new ScheduleRuleEntry(startTime, priority));
        this.sort();
        return this;
    }

    public ScheduleRule add(Collection<ScheduleRuleEntry> entries) {
        this.entries.addAll(entries);
        this.sort();
        return this;
    }

    private void sort() {
        Int2ObjectAVLTreeMap int2ObjectSortedMap = new Int2ObjectAVLTreeMap();
        this.entries.forEach(arg -> int2ObjectSortedMap.put(arg.getStartTime(), arg));
        this.entries.clear();
        this.entries.addAll(int2ObjectSortedMap.values());
        this.prioritizedEntryIndex = 0;
    }

    public float getPriority(int time) {
        ScheduleRuleEntry lv3;
        if (this.entries.size() <= 0) {
            return 0.0f;
        }
        ScheduleRuleEntry lv = this.entries.get(this.prioritizedEntryIndex);
        ScheduleRuleEntry lv2 = this.entries.get(this.entries.size() - 1);
        boolean bl = time < lv.getStartTime();
        int j = bl ? 0 : this.prioritizedEntryIndex;
        float f = bl ? lv2.getPriority() : lv.getPriority();
        int k = j;
        while (k < this.entries.size() && (lv3 = this.entries.get(k)).getStartTime() <= time) {
            this.prioritizedEntryIndex = k++;
            f = lv3.getPriority();
        }
        return f;
    }
}

