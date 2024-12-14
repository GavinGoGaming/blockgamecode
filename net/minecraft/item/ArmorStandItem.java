/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.item;

import java.util.function.Consumer;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.SpawnReason;
import net.minecraft.entity.decoration.ArmorStandEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUsageContext;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.ActionResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraft.world.event.GameEvent;

public class ArmorStandItem
extends Item {
    public ArmorStandItem(Item.Settings arg) {
        super(arg);
    }

    @Override
    public ActionResult useOnBlock(ItemUsageContext context) {
        Direction lv = context.getSide();
        if (lv == Direction.DOWN) {
            return ActionResult.FAIL;
        }
        World lv2 = context.getWorld();
        ItemPlacementContext lv3 = new ItemPlacementContext(context);
        BlockPos lv4 = lv3.getBlockPos();
        ItemStack lv5 = context.getStack();
        Vec3d lv6 = Vec3d.ofBottomCenter(lv4);
        Box lv7 = EntityType.ARMOR_STAND.getDimensions().getBoxAt(lv6.getX(), lv6.getY(), lv6.getZ());
        if (!lv2.isSpaceEmpty(null, lv7) || !lv2.getOtherEntities(null, lv7).isEmpty()) {
            return ActionResult.FAIL;
        }
        if (lv2 instanceof ServerWorld) {
            ServerWorld lv8 = (ServerWorld)lv2;
            Consumer consumer = EntityType.copier(lv8, lv5, context.getPlayer());
            ArmorStandEntity lv9 = EntityType.ARMOR_STAND.create(lv8, consumer, lv4, SpawnReason.SPAWN_EGG, true, true);
            if (lv9 == null) {
                return ActionResult.FAIL;
            }
            float f = (float)MathHelper.floor((MathHelper.wrapDegrees(context.getPlayerYaw() - 180.0f) + 22.5f) / 45.0f) * 45.0f;
            lv9.refreshPositionAndAngles(lv9.getX(), lv9.getY(), lv9.getZ(), f, 0.0f);
            lv8.spawnEntityAndPassengers(lv9);
            lv2.playSound(null, lv9.getX(), lv9.getY(), lv9.getZ(), SoundEvents.ENTITY_ARMOR_STAND_PLACE, SoundCategory.BLOCKS, 0.75f, 0.8f);
            lv9.emitGameEvent(GameEvent.ENTITY_PLACE, context.getPlayer());
        }
        lv5.decrement(1);
        return ActionResult.success(lv2.isClient);
    }
}

