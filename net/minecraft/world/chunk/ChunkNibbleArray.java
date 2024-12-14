/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 * 
 * Could not load the following classes:
 *  org.jetbrains.annotations.Nullable
 */
package net.minecraft.world.chunk;

import java.util.Arrays;
import net.minecraft.util.Util;
import net.minecraft.util.annotation.Debug;
import org.jetbrains.annotations.Nullable;

public class ChunkNibbleArray {
    public static final int COPY_TIMES = 16;
    public static final int COPY_BLOCK_SIZE = 128;
    public static final int BYTES_LENGTH = 2048;
    private static final int NIBBLE_BITS = 4;
    @Nullable
    protected byte[] bytes;
    private int defaultValue;

    public ChunkNibbleArray() {
        this(0);
    }

    public ChunkNibbleArray(int defaultValue) {
        this.defaultValue = defaultValue;
    }

    public ChunkNibbleArray(byte[] bytes) {
        this.bytes = bytes;
        this.defaultValue = 0;
        if (bytes.length != 2048) {
            throw Util.throwOrPause(new IllegalArgumentException("DataLayer should be 2048 bytes not: " + bytes.length));
        }
    }

    public int get(int x, int y, int z) {
        return this.get(ChunkNibbleArray.getIndex(x, y, z));
    }

    public void set(int x, int y, int z, int value) {
        this.set(ChunkNibbleArray.getIndex(x, y, z), value);
    }

    private static int getIndex(int x, int y, int z) {
        return y << 8 | z << 4 | x;
    }

    private int get(int index) {
        if (this.bytes == null) {
            return this.defaultValue;
        }
        int j = ChunkNibbleArray.getArrayIndex(index);
        int k = ChunkNibbleArray.occupiesSmallerBits(index);
        return this.bytes[j] >> 4 * k & 0xF;
    }

    private void set(int index, int value) {
        byte[] bs = this.asByteArray();
        int k = ChunkNibbleArray.getArrayIndex(index);
        int l = ChunkNibbleArray.occupiesSmallerBits(index);
        int m = ~(15 << 4 * l);
        int n = (value & 0xF) << 4 * l;
        bs[k] = (byte)(bs[k] & m | n);
    }

    private static int occupiesSmallerBits(int i) {
        return i & 1;
    }

    private static int getArrayIndex(int i) {
        return i >> 1;
    }

    public void clear(int defaultValue) {
        this.defaultValue = defaultValue;
        this.bytes = null;
    }

    private static byte pack(int value) {
        byte b = (byte)value;
        for (int j = 4; j < 8; j += 4) {
            b = (byte)(b | value << j);
        }
        return b;
    }

    public byte[] asByteArray() {
        if (this.bytes == null) {
            this.bytes = new byte[2048];
            if (this.defaultValue != 0) {
                Arrays.fill(this.bytes, ChunkNibbleArray.pack(this.defaultValue));
            }
        }
        return this.bytes;
    }

    public ChunkNibbleArray copy() {
        if (this.bytes == null) {
            return new ChunkNibbleArray(this.defaultValue);
        }
        return new ChunkNibbleArray((byte[])this.bytes.clone());
    }

    public String toString() {
        StringBuilder stringBuilder = new StringBuilder();
        for (int i = 0; i < 4096; ++i) {
            stringBuilder.append(Integer.toHexString(this.get(i)));
            if ((i & 0xF) == 15) {
                stringBuilder.append("\n");
            }
            if ((i & 0xFF) != 255) continue;
            stringBuilder.append("\n");
        }
        return stringBuilder.toString();
    }

    @Debug
    public String bottomToString(int unused) {
        StringBuilder stringBuilder = new StringBuilder();
        for (int j = 0; j < 256; ++j) {
            stringBuilder.append(Integer.toHexString(this.get(j)));
            if ((j & 0xF) != 15) continue;
            stringBuilder.append("\n");
        }
        return stringBuilder.toString();
    }

    public boolean isArrayUninitialized() {
        return this.bytes == null;
    }

    public boolean isUninitialized(int expectedDefaultValue) {
        return this.bytes == null && this.defaultValue == expectedDefaultValue;
    }

    public boolean isUninitialized() {
        return this.bytes == null && this.defaultValue == 0;
    }
}

