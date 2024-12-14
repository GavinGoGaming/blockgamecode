/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.item;

import java.util.List;
import java.util.function.Predicate;
import net.minecraft.block.BlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.Leashable;
import net.minecraft.entity.decoration.LeashKnotEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemUsageContext;
import net.minecraft.registry.tag.BlockTags;
import net.minecraft.util.ActionResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.world.World;
import net.minecraft.world.event.GameEvent;

public class LeadItem
extends Item {
    public LeadItem(Item.Settings arg) {
        super(arg);
    }

    @Override
    public ActionResult useOnBlock(ItemUsageContext context) {
        BlockPos lv2;
        World lv = context.getWorld();
        BlockState lv3 = lv.getBlockState(lv2 = context.getBlockPos());
        if (lv3.isIn(BlockTags.FENCES)) {
            PlayerEntity lv4 = context.getPlayer();
            if (!lv.isClient && lv4 != null) {
                LeadItem.attachHeldMobsToBlock(lv4, lv, lv2);
            }
            return ActionResult.success(lv.isClient);
        }
        return ActionResult.PASS;
    }

    public static ActionResult attachHeldMobsToBlock(PlayerEntity player, World world, BlockPos pos) {
        LeashKnotEntity lv = null;
        List<Leashable> list = LeadItem.collectLeashablesAround(world, pos, entity -> entity.getLeashHolder() == player);
        for (Leashable lv2 : list) {
            if (lv == null) {
                lv = LeashKnotEntity.getOrCreate(world, pos);
                lv.onPlace();
            }
            lv2.attachLeash(lv, true);
        }
        if (!list.isEmpty()) {
            world.emitGameEvent(GameEvent.BLOCK_ATTACH, pos, GameEvent.Emitter.of(player));
            return ActionResult.SUCCESS;
        }
        return ActionResult.PASS;
    }

    public static List<Leashable> collectLeashablesAround(World world, BlockPos pos, Predicate<Leashable> predicate) {
        double d = 7.0;
        int i = pos.getX();
        int j = pos.getY();
        int k = pos.getZ();
        Box lv = new Box((double)i - 7.0, (double)j - 7.0, (double)k - 7.0, (double)i + 7.0, (double)j + 7.0, (double)k + 7.0);
        return world.getEntitiesByClass(Entity.class, lv, entity -> {
            Leashable lv;
            return entity instanceof Leashable && predicate.test(lv = (Leashable)((Object)entity));
        }).stream().map(Leashable.class::cast).toList();
    }
}

