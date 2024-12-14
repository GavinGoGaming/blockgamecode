/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.nbt;

import net.minecraft.nbt.NbtElement;

public abstract class AbstractNbtNumber
implements NbtElement {
    protected AbstractNbtNumber() {
    }

    public abstract long longValue();

    public abstract int intValue();

    public abstract short shortValue();

    public abstract byte byteValue();

    public abstract double doubleValue();

    public abstract float floatValue();

    public abstract Number numberValue();

    @Override
    public String toString() {
        return this.asString();
    }
}

