/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.block.dispenser;

import java.util.List;
import net.minecraft.block.BeehiveBlock;
import net.minecraft.block.BlockState;
import net.minecraft.block.DispenserBlock;
import net.minecraft.block.dispenser.FallibleItemDispenserBehavior;
import net.minecraft.block.entity.BeehiveBlockEntity;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.Shearable;
import net.minecraft.item.ItemStack;
import net.minecraft.predicate.entity.EntityPredicates;
import net.minecraft.registry.tag.BlockTags;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.math.BlockPointer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.world.event.GameEvent;

public class ShearsDispenserBehavior
extends FallibleItemDispenserBehavior {
    @Override
    protected ItemStack dispenseSilently(BlockPointer pointer, ItemStack stack) {
        ServerWorld lv = pointer.world();
        if (!lv.isClient()) {
            BlockPos lv2 = pointer.pos().offset(pointer.state().get(DispenserBlock.FACING));
            this.setSuccess(ShearsDispenserBehavior.tryShearBlock(lv, lv2) || ShearsDispenserBehavior.tryShearEntity(lv, lv2));
            if (this.isSuccess()) {
                stack.damage(1, lv, null, item -> {});
            }
        }
        return stack;
    }

    private static boolean tryShearBlock(ServerWorld world, BlockPos pos) {
        int i;
        BlockState lv = world.getBlockState(pos);
        if (lv.isIn(BlockTags.BEEHIVES, state -> state.contains(BeehiveBlock.HONEY_LEVEL) && state.getBlock() instanceof BeehiveBlock) && (i = lv.get(BeehiveBlock.HONEY_LEVEL).intValue()) >= 5) {
            world.playSound(null, pos, SoundEvents.BLOCK_BEEHIVE_SHEAR, SoundCategory.BLOCKS, 1.0f, 1.0f);
            BeehiveBlock.dropHoneycomb(world, pos);
            ((BeehiveBlock)lv.getBlock()).takeHoney(world, lv, pos, null, BeehiveBlockEntity.BeeState.BEE_RELEASED);
            world.emitGameEvent(null, GameEvent.SHEAR, pos);
            return true;
        }
        return false;
    }

    private static boolean tryShearEntity(ServerWorld world, BlockPos pos) {
        List<Entity> list = world.getEntitiesByClass(LivingEntity.class, new Box(pos), EntityPredicates.EXCEPT_SPECTATOR);
        for (LivingEntity livingEntity : list) {
            Shearable lv2;
            if (!(livingEntity instanceof Shearable) || !(lv2 = (Shearable)((Object)livingEntity)).isShearable()) continue;
            lv2.sheared(SoundCategory.BLOCKS);
            world.emitGameEvent(null, GameEvent.SHEAR, pos);
            return true;
        }
        return false;
    }
}

