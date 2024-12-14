/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.block;

import com.mojang.logging.LogUtils;
import com.mojang.serialization.MapCodec;
import net.minecraft.block.AbstractBlock;
import net.minecraft.block.BlockState;
import net.minecraft.block.DispenserBlock;
import net.minecraft.block.dispenser.DispenserBehavior;
import net.minecraft.block.dispenser.ItemDispenserBehavior;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.block.entity.DispenserBlockEntity;
import net.minecraft.block.entity.DropperBlockEntity;
import net.minecraft.block.entity.HopperBlockEntity;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPointer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;
import net.minecraft.world.WorldEvents;
import org.slf4j.Logger;

public class DropperBlock
extends DispenserBlock {
    private static final Logger LOGGER = LogUtils.getLogger();
    public static final MapCodec<DropperBlock> CODEC = DropperBlock.createCodec(DropperBlock::new);
    private static final DispenserBehavior BEHAVIOR = new ItemDispenserBehavior();

    public MapCodec<DropperBlock> getCodec() {
        return CODEC;
    }

    public DropperBlock(AbstractBlock.Settings arg) {
        super(arg);
    }

    @Override
    protected DispenserBehavior getBehaviorForItem(World world, ItemStack stack) {
        return BEHAVIOR;
    }

    @Override
    public BlockEntity createBlockEntity(BlockPos pos, BlockState state) {
        return new DropperBlockEntity(pos, state);
    }

    @Override
    protected void dispense(ServerWorld world, BlockState state, BlockPos pos) {
        ItemStack lv6;
        DispenserBlockEntity lv = world.getBlockEntity(pos, BlockEntityType.DROPPER).orElse(null);
        if (lv == null) {
            LOGGER.warn("Ignoring dispensing attempt for Dropper without matching block entity at {}", (Object)pos);
            return;
        }
        BlockPointer lv2 = new BlockPointer(world, pos, state, lv);
        int i = lv.chooseNonEmptySlot(world.random);
        if (i < 0) {
            world.syncWorldEvent(WorldEvents.DISPENSER_FAILS, pos, 0);
            return;
        }
        ItemStack lv3 = lv.getStack(i);
        if (lv3.isEmpty()) {
            return;
        }
        Direction lv4 = world.getBlockState(pos).get(FACING);
        Inventory lv5 = HopperBlockEntity.getInventoryAt(world, pos.offset(lv4));
        if (lv5 == null) {
            lv6 = BEHAVIOR.dispense(lv2, lv3);
        } else {
            lv6 = HopperBlockEntity.transfer(lv, lv5, lv3.copyWithCount(1), lv4.getOpposite());
            if (lv6.isEmpty()) {
                lv6 = lv3.copy();
                lv6.decrement(1);
            } else {
                lv6 = lv3.copy();
            }
        }
        lv.setStack(i, lv6);
    }
}

