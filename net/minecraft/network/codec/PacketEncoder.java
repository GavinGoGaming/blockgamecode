/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.network.codec;

@FunctionalInterface
public interface PacketEncoder<O, T> {
    public void encode(O var1, T var2);
}

