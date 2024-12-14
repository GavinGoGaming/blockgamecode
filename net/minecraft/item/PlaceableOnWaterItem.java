/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.item;

import net.minecraft.block.Block;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUsageContext;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.world.RaycastContext;
import net.minecraft.world.World;

public class PlaceableOnWaterItem
extends BlockItem {
    public PlaceableOnWaterItem(Block arg, Item.Settings arg2) {
        super(arg, arg2);
    }

    @Override
    public ActionResult useOnBlock(ItemUsageContext context) {
        return ActionResult.PASS;
    }

    @Override
    public TypedActionResult<ItemStack> use(World world, PlayerEntity user, Hand hand) {
        BlockHitResult lv = PlaceableOnWaterItem.raycast(world, user, RaycastContext.FluidHandling.SOURCE_ONLY);
        BlockHitResult lv2 = lv.withBlockPos(lv.getBlockPos().up());
        ActionResult lv3 = super.useOnBlock(new ItemUsageContext(user, hand, lv2));
        return new TypedActionResult<ItemStack>(lv3, user.getStackInHand(hand));
    }
}

