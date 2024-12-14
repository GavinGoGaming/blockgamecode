/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.block.dispenser;

import com.mojang.logging.LogUtils;
import net.minecraft.block.DispenserBlock;
import net.minecraft.block.dispenser.FallibleItemDispenserBehavior;
import net.minecraft.item.AutomaticItemPlacementContext;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPointer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;
import org.slf4j.Logger;

public class BlockPlacementDispenserBehavior
extends FallibleItemDispenserBehavior {
    private static final Logger LOGGER = LogUtils.getLogger();

    @Override
    protected ItemStack dispenseSilently(BlockPointer pointer, ItemStack stack) {
        this.setSuccess(false);
        Item lv = stack.getItem();
        if (lv instanceof BlockItem) {
            Direction lv2 = pointer.state().get(DispenserBlock.FACING);
            BlockPos lv3 = pointer.pos().offset(lv2);
            Direction lv4 = pointer.world().isAir(lv3.down()) ? lv2 : Direction.UP;
            try {
                this.setSuccess(((BlockItem)lv).place(new AutomaticItemPlacementContext((World)pointer.world(), lv3, lv2, stack, lv4)).isAccepted());
            } catch (Exception exception) {
                LOGGER.error("Error trying to place shulker box at {}", (Object)lv3, (Object)exception);
            }
        }
        return stack;
    }
}

