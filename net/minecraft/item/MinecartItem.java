/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.item;

import net.minecraft.block.AbstractRailBlock;
import net.minecraft.block.BlockState;
import net.minecraft.block.DispenserBlock;
import net.minecraft.block.dispenser.DispenserBehavior;
import net.minecraft.block.dispenser.ItemDispenserBehavior;
import net.minecraft.block.enums.RailShape;
import net.minecraft.entity.vehicle.AbstractMinecartEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUsageContext;
import net.minecraft.registry.tag.BlockTags;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.ActionResult;
import net.minecraft.util.math.BlockPointer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraft.world.WorldEvents;
import net.minecraft.world.event.GameEvent;

public class MinecartItem
extends Item {
    private static final DispenserBehavior DISPENSER_BEHAVIOR = new ItemDispenserBehavior(){
        private final ItemDispenserBehavior defaultBehavior = new ItemDispenserBehavior();

        @Override
        public ItemStack dispenseSilently(BlockPointer pointer, ItemStack stack) {
            double g;
            RailShape lv6;
            Direction lv = pointer.state().get(DispenserBlock.FACING);
            ServerWorld lv2 = pointer.world();
            Vec3d lv3 = pointer.centerPos();
            double d = lv3.getX() + (double)lv.getOffsetX() * 1.125;
            double e = Math.floor(lv3.getY()) + (double)lv.getOffsetY();
            double f = lv3.getZ() + (double)lv.getOffsetZ() * 1.125;
            BlockPos lv4 = pointer.pos().offset(lv);
            BlockState lv5 = lv2.getBlockState(lv4);
            RailShape railShape = lv6 = lv5.getBlock() instanceof AbstractRailBlock ? lv5.get(((AbstractRailBlock)lv5.getBlock()).getShapeProperty()) : RailShape.NORTH_SOUTH;
            if (lv5.isIn(BlockTags.RAILS)) {
                g = lv6.isAscending() ? 0.6 : 0.1;
            } else if (lv5.isAir() && lv2.getBlockState(lv4.down()).isIn(BlockTags.RAILS)) {
                RailShape lv8;
                BlockState lv7 = lv2.getBlockState(lv4.down());
                RailShape railShape2 = lv8 = lv7.getBlock() instanceof AbstractRailBlock ? lv7.get(((AbstractRailBlock)lv7.getBlock()).getShapeProperty()) : RailShape.NORTH_SOUTH;
                g = lv == Direction.DOWN || !lv8.isAscending() ? -0.9 : -0.4;
            } else {
                return this.defaultBehavior.dispense(pointer, stack);
            }
            AbstractMinecartEntity lv9 = AbstractMinecartEntity.create(lv2, d, e + g, f, ((MinecartItem)stack.getItem()).type, stack, null);
            lv2.spawnEntity(lv9);
            stack.decrement(1);
            return stack;
        }

        @Override
        protected void playSound(BlockPointer pointer) {
            pointer.world().syncWorldEvent(WorldEvents.DISPENSER_DISPENSES, pointer.pos(), 0);
        }
    };
    final AbstractMinecartEntity.Type type;

    public MinecartItem(AbstractMinecartEntity.Type type, Item.Settings settings) {
        super(settings);
        this.type = type;
        DispenserBlock.registerBehavior(this, DISPENSER_BEHAVIOR);
    }

    @Override
    public ActionResult useOnBlock(ItemUsageContext context) {
        BlockPos lv2;
        World lv = context.getWorld();
        BlockState lv3 = lv.getBlockState(lv2 = context.getBlockPos());
        if (!lv3.isIn(BlockTags.RAILS)) {
            return ActionResult.FAIL;
        }
        ItemStack lv4 = context.getStack();
        if (lv instanceof ServerWorld) {
            ServerWorld lv5 = (ServerWorld)lv;
            RailShape lv6 = lv3.getBlock() instanceof AbstractRailBlock ? lv3.get(((AbstractRailBlock)lv3.getBlock()).getShapeProperty()) : RailShape.NORTH_SOUTH;
            double d = 0.0;
            if (lv6.isAscending()) {
                d = 0.5;
            }
            AbstractMinecartEntity lv7 = AbstractMinecartEntity.create(lv5, (double)lv2.getX() + 0.5, (double)lv2.getY() + 0.0625 + d, (double)lv2.getZ() + 0.5, this.type, lv4, context.getPlayer());
            lv5.spawnEntity(lv7);
            lv5.emitGameEvent(GameEvent.ENTITY_PLACE, lv2, GameEvent.Emitter.of(context.getPlayer(), lv5.getBlockState(lv2.down())));
        }
        lv4.decrement(1);
        return ActionResult.success(lv.isClient);
    }
}

