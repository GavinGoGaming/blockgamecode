/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.util.collection;

import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import net.minecraft.util.Util;

public class Class2IntMap {
    public static final int MISSING = -1;
    private final Object2IntMap<Class<?>> backingMap = Util.make(new Object2IntOpenHashMap(), object2IntOpenHashMap -> object2IntOpenHashMap.defaultReturnValue(-1));

    public int get(Class<?> clazz) {
        int i = this.backingMap.getInt(clazz);
        if (i != -1) {
            return i;
        }
        Class<?> class2 = clazz;
        while ((class2 = class2.getSuperclass()) != Object.class) {
            int j = this.backingMap.getInt(class2);
            if (j == -1) continue;
            return j;
        }
        return -1;
    }

    public int getNext(Class<?> clazz) {
        return this.get(clazz) + 1;
    }

    public int put(Class<?> clazz) {
        int i = this.get(clazz);
        int j = i == -1 ? 0 : i + 1;
        this.backingMap.put(clazz, j);
        return j;
    }
}

