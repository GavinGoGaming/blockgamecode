/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 * 
 * Could not load the following classes:
 *  org.jetbrains.annotations.Nullable
 */
package net.minecraft.item;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.ScaffoldingBlock;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

public class ScaffoldingItem
extends BlockItem {
    public ScaffoldingItem(Block arg, Item.Settings arg2) {
        super(arg, arg2);
    }

    @Override
    @Nullable
    public ItemPlacementContext getPlacementContext(ItemPlacementContext context) {
        Block lv4;
        BlockPos lv = context.getBlockPos();
        World lv2 = context.getWorld();
        BlockState lv3 = lv2.getBlockState(lv);
        if (lv3.isOf(lv4 = this.getBlock())) {
            Direction lv5 = context.shouldCancelInteraction() ? (context.hitsInsideBlock() ? context.getSide().getOpposite() : context.getSide()) : (context.getSide() == Direction.UP ? context.getHorizontalPlayerFacing() : Direction.UP);
            int i = 0;
            BlockPos.Mutable lv6 = lv.mutableCopy().move(lv5);
            while (i < 7) {
                if (!lv2.isClient && !lv2.isInBuildLimit(lv6)) {
                    PlayerEntity lv7 = context.getPlayer();
                    int j = lv2.getTopY();
                    if (!(lv7 instanceof ServerPlayerEntity) || lv6.getY() < j) break;
                    ((ServerPlayerEntity)lv7).sendMessageToClient(Text.translatable("build.tooHigh", j - 1).formatted(Formatting.RED), true);
                    break;
                }
                lv3 = lv2.getBlockState(lv6);
                if (!lv3.isOf(this.getBlock())) {
                    if (!lv3.canReplace(context)) break;
                    return ItemPlacementContext.offset(context, lv6, lv5);
                }
                lv6.move(lv5);
                if (!lv5.getAxis().isHorizontal()) continue;
                ++i;
            }
            return null;
        }
        if (ScaffoldingBlock.calculateDistance(lv2, lv) == 7) {
            return null;
        }
        return context;
    }

    @Override
    protected boolean checkStatePlacement() {
        return false;
    }
}

