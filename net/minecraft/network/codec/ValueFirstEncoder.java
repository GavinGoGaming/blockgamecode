/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.network.codec;

@FunctionalInterface
public interface ValueFirstEncoder<O, T> {
    public void encode(T var1, O var2);
}

