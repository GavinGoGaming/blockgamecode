/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.item;

import net.minecraft.advancement.criterion.Criteria;
import net.minecraft.block.AbstractFireBlock;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.CampfireBlock;
import net.minecraft.block.CandleBlock;
import net.minecraft.block.CandleCakeBlock;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUsageContext;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.state.property.Properties;
import net.minecraft.util.ActionResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.event.GameEvent;

public class FlintAndSteelItem
extends Item {
    public FlintAndSteelItem(Item.Settings arg) {
        super(arg);
    }

    @Override
    public ActionResult useOnBlock(ItemUsageContext context) {
        BlockPos lv3;
        PlayerEntity lv = context.getPlayer();
        World lv2 = context.getWorld();
        BlockState lv4 = lv2.getBlockState(lv3 = context.getBlockPos());
        if (CampfireBlock.canBeLit(lv4) || CandleBlock.canBeLit(lv4) || CandleCakeBlock.canBeLit(lv4)) {
            lv2.playSound(lv, lv3, SoundEvents.ITEM_FLINTANDSTEEL_USE, SoundCategory.BLOCKS, 1.0f, lv2.getRandom().nextFloat() * 0.4f + 0.8f);
            lv2.setBlockState(lv3, (BlockState)lv4.with(Properties.LIT, true), Block.NOTIFY_ALL_AND_REDRAW);
            lv2.emitGameEvent((Entity)lv, GameEvent.BLOCK_CHANGE, lv3);
            if (lv != null) {
                context.getStack().damage(1, lv, LivingEntity.getSlotForHand(context.getHand()));
            }
            return ActionResult.success(lv2.isClient());
        }
        BlockPos lv5 = lv3.offset(context.getSide());
        if (AbstractFireBlock.canPlaceAt(lv2, lv5, context.getHorizontalPlayerFacing())) {
            lv2.playSound(lv, lv5, SoundEvents.ITEM_FLINTANDSTEEL_USE, SoundCategory.BLOCKS, 1.0f, lv2.getRandom().nextFloat() * 0.4f + 0.8f);
            BlockState lv6 = AbstractFireBlock.getState(lv2, lv5);
            lv2.setBlockState(lv5, lv6, Block.NOTIFY_ALL_AND_REDRAW);
            lv2.emitGameEvent((Entity)lv, GameEvent.BLOCK_PLACE, lv3);
            ItemStack lv7 = context.getStack();
            if (lv instanceof ServerPlayerEntity) {
                Criteria.PLACED_BLOCK.trigger((ServerPlayerEntity)lv, lv5, lv7);
                lv7.damage(1, lv, LivingEntity.getSlotForHand(context.getHand()));
            }
            return ActionResult.success(lv2.isClient());
        }
        return ActionResult.FAIL;
    }
}

