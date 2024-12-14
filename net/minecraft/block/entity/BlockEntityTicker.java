/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.block.entity;

import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

@FunctionalInterface
public interface BlockEntityTicker<T extends BlockEntity> {
    public void tick(World var1, BlockPos var2, BlockState var3, T var4);
}

