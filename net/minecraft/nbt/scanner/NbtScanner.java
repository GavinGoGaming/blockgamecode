/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.nbt.scanner;

import net.minecraft.nbt.NbtType;

public interface NbtScanner {
    public Result visitEnd();

    public Result visitString(String var1);

    public Result visitByte(byte var1);

    public Result visitShort(short var1);

    public Result visitInt(int var1);

    public Result visitLong(long var1);

    public Result visitFloat(float var1);

    public Result visitDouble(double var1);

    public Result visitByteArray(byte[] var1);

    public Result visitIntArray(int[] var1);

    public Result visitLongArray(long[] var1);

    public Result visitListMeta(NbtType<?> var1, int var2);

    public NestedResult visitSubNbtType(NbtType<?> var1);

    public NestedResult startSubNbt(NbtType<?> var1, String var2);

    public NestedResult startListItem(NbtType<?> var1, int var2);

    public Result endNested();

    public Result start(NbtType<?> var1);

    public static enum NestedResult {
        ENTER,
        SKIP,
        BREAK,
        HALT;

    }

    public static enum Result {
        CONTINUE,
        BREAK,
        HALT;

    }
}

