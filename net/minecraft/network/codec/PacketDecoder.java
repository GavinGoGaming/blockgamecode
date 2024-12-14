/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.network.codec;

@FunctionalInterface
public interface PacketDecoder<I, T> {
    public T decode(I var1);
}

