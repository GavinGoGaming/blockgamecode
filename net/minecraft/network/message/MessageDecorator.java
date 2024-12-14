/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 * 
 * Could not load the following classes:
 *  org.jetbrains.annotations.Nullable
 */
package net.minecraft.network.message;

import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import org.jetbrains.annotations.Nullable;

@FunctionalInterface
public interface MessageDecorator {
    public static final MessageDecorator NOOP = (sender, message) -> message;

    public Text decorate(@Nullable ServerPlayerEntity var1, Text var2);
}

