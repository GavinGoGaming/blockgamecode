/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.text;

import net.minecraft.text.Style;

@FunctionalInterface
public interface CharacterVisitor {
    public boolean accept(int var1, Style var2, int var3);
}

