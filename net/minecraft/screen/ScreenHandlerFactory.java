/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 * 
 * Could not load the following classes:
 *  org.jetbrains.annotations.Nullable
 */
package net.minecraft.screen;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.screen.ScreenHandler;
import org.jetbrains.annotations.Nullable;

@FunctionalInterface
public interface ScreenHandlerFactory {
    @Nullable
    public ScreenHandler createMenu(int var1, PlayerInventory var2, PlayerEntity var3);
}

