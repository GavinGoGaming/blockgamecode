/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.block.entity;

import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.block.entity.DispenserBlockEntity;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;

public class DropperBlockEntity
extends DispenserBlockEntity {
    public DropperBlockEntity(BlockPos arg, BlockState arg2) {
        super(BlockEntityType.DROPPER, arg, arg2);
    }

    @Override
    protected Text getContainerName() {
        return Text.translatable("container.dropper");
    }
}

