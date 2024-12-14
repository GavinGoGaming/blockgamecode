/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 * 
 * Could not load the following classes:
 *  org.jetbrains.annotations.Nullable
 */
package net.minecraft.item;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemPlacementContext;
import org.jetbrains.annotations.Nullable;

public class OperatorOnlyBlockItem
extends BlockItem {
    public OperatorOnlyBlockItem(Block arg, Item.Settings arg2) {
        super(arg, arg2);
    }

    @Override
    @Nullable
    protected BlockState getPlacementState(ItemPlacementContext context) {
        PlayerEntity lv = context.getPlayer();
        return lv == null || lv.isCreativeLevelTwoOp() ? super.getPlacementState(context) : null;
    }
}

