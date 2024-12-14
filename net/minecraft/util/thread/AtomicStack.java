/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.util.thread;

import com.google.common.collect.ImmutableList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReferenceArray;

public class AtomicStack<T> {
    private final AtomicReferenceArray<T> contents;
    private final AtomicInteger size;

    public AtomicStack(int maxSize) {
        this.contents = new AtomicReferenceArray(maxSize);
        this.size = new AtomicInteger(0);
    }

    public void push(T value) {
        int k;
        int j;
        int i = this.contents.length();
        while (!this.size.compareAndSet(j = this.size.get(), k = (j + 1) % i)) {
        }
        this.contents.set(k, value);
    }

    public List<T> toList() {
        int i = this.size.get();
        ImmutableList.Builder builder = ImmutableList.builder();
        for (int j = 0; j < this.contents.length(); ++j) {
            int k = Math.floorMod(i - j, this.contents.length());
            T object = this.contents.get(k);
            if (object == null) continue;
            builder.add(object);
        }
        return builder.build();
    }
}

