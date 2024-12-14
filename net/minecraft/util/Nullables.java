/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 * 
 * Could not load the following classes:
 *  org.jetbrains.annotations.Nullable
 */
package net.minecraft.util;

import java.util.Collection;
import java.util.Iterator;
import java.util.function.Function;
import java.util.function.Supplier;
import org.jetbrains.annotations.Nullable;

public class Nullables {
    @Nullable
    public static <T, R> R map(@Nullable T value, Function<T, R> mapper) {
        return value == null ? null : (R)mapper.apply(value);
    }

    public static <T, R> R mapOrElse(@Nullable T value, Function<T, R> mapper, R other) {
        return value == null ? other : mapper.apply(value);
    }

    public static <T, R> R mapOrElseGet(@Nullable T value, Function<T, R> mapper, Supplier<R> getter) {
        return value == null ? getter.get() : mapper.apply(value);
    }

    @Nullable
    public static <T> T getFirst(Collection<T> collection) {
        Iterator<T> iterator = collection.iterator();
        return iterator.hasNext() ? (T)iterator.next() : null;
    }

    public static <T> T getFirstOrElse(Collection<T> collection, T defaultValue) {
        Iterator<T> iterator = collection.iterator();
        return iterator.hasNext() ? iterator.next() : defaultValue;
    }

    public static <T> T getFirstOrElseGet(Collection<T> collection, Supplier<T> getter) {
        Iterator<T> iterator = collection.iterator();
        return iterator.hasNext() ? iterator.next() : getter.get();
    }

    public static <T> boolean isEmpty(@Nullable T[] array) {
        return array == null || array.length == 0;
    }

    public static boolean isEmpty(@Nullable boolean[] array) {
        return array == null || array.length == 0;
    }

    public static boolean isEmpty(@Nullable byte[] array) {
        return array == null || array.length == 0;
    }

    public static boolean isEmpty(@Nullable char[] array) {
        return array == null || array.length == 0;
    }

    public static boolean isEmpty(@Nullable short[] array) {
        return array == null || array.length == 0;
    }

    public static boolean isEmpty(@Nullable int[] array) {
        return array == null || array.length == 0;
    }

    public static boolean isEmpty(@Nullable long[] array) {
        return array == null || array.length == 0;
    }

    public static boolean isEmpty(@Nullable float[] array) {
        return array == null || array.length == 0;
    }

    public static boolean isEmpty(@Nullable double[] array) {
        return array == null || array.length == 0;
    }
}

