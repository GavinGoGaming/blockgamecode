/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 * 
 * Could not load the following classes:
 *  org.jetbrains.annotations.Nullable
 */
package net.minecraft.util;

import org.jetbrains.annotations.Nullable;

public interface TypeFilter<B, T extends B> {
    public static <B, T extends B> TypeFilter<B, T> instanceOf(final Class<T> cls) {
        return new TypeFilter<B, T>(){

            @Override
            @Nullable
            public T downcast(B obj) {
                return cls.isInstance(obj) ? obj : null;
            }

            @Override
            public Class<? extends B> getBaseClass() {
                return cls;
            }
        };
    }

    public static <B, T extends B> TypeFilter<B, T> equals(final Class<T> cls) {
        return new TypeFilter<B, T>(){

            @Override
            @Nullable
            public T downcast(B obj) {
                return cls.equals(obj.getClass()) ? obj : null;
            }

            @Override
            public Class<? extends B> getBaseClass() {
                return cls;
            }
        };
    }

    @Nullable
    public T downcast(B var1);

    public Class<? extends B> getBaseClass();
}

