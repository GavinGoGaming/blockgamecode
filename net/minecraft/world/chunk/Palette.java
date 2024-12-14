/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.world.chunk;

import java.util.List;
import java.util.function.Predicate;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.util.collection.IndexedIterable;
import net.minecraft.world.chunk.PaletteResizeListener;

public interface Palette<T> {
    public int index(T var1);

    public boolean hasAny(Predicate<T> var1);

    public T get(int var1);

    public void readPacket(PacketByteBuf var1);

    public void writePacket(PacketByteBuf var1);

    public int getPacketSize();

    public int getSize();

    public Palette<T> copy();

    public static interface Factory {
        public <A> Palette<A> create(int var1, IndexedIterable<A> var2, PaletteResizeListener<A> var3, List<A> var4);
    }
}

