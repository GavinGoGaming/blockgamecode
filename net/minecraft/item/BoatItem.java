/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.item;

import java.util.List;
import java.util.function.Predicate;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.vehicle.BoatEntity;
import net.minecraft.entity.vehicle.ChestBoatEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.predicate.entity.EntityPredicates;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.stat.Stats;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.RaycastContext;
import net.minecraft.world.World;
import net.minecraft.world.event.GameEvent;

public class BoatItem
extends Item {
    private static final Predicate<Entity> RIDERS = EntityPredicates.EXCEPT_SPECTATOR.and(Entity::canHit);
    private final BoatEntity.Type type;
    private final boolean chest;

    public BoatItem(boolean chest, BoatEntity.Type type, Item.Settings settings) {
        super(settings);
        this.chest = chest;
        this.type = type;
    }

    @Override
    public TypedActionResult<ItemStack> use(World world, PlayerEntity user, Hand hand) {
        ItemStack lv = user.getStackInHand(hand);
        BlockHitResult lv2 = BoatItem.raycast(world, user, RaycastContext.FluidHandling.ANY);
        if (((HitResult)lv2).getType() == HitResult.Type.MISS) {
            return TypedActionResult.pass(lv);
        }
        Vec3d lv3 = user.getRotationVec(1.0f);
        double d = 5.0;
        List<Entity> list = world.getOtherEntities(user, user.getBoundingBox().stretch(lv3.multiply(5.0)).expand(1.0), RIDERS);
        if (!list.isEmpty()) {
            Vec3d lv4 = user.getEyePos();
            for (Entity lv5 : list) {
                Box lv6 = lv5.getBoundingBox().expand(lv5.getTargetingMargin());
                if (!lv6.contains(lv4)) continue;
                return TypedActionResult.pass(lv);
            }
        }
        if (((HitResult)lv2).getType() == HitResult.Type.BLOCK) {
            BoatEntity lv7 = this.createEntity(world, lv2, lv, user);
            lv7.setVariant(this.type);
            lv7.setYaw(user.getYaw());
            if (!world.isSpaceEmpty(lv7, lv7.getBoundingBox())) {
                return TypedActionResult.fail(lv);
            }
            if (!world.isClient) {
                world.spawnEntity(lv7);
                world.emitGameEvent((Entity)user, GameEvent.ENTITY_PLACE, lv2.getPos());
                lv.decrementUnlessCreative(1, user);
            }
            user.incrementStat(Stats.USED.getOrCreateStat(this));
            return TypedActionResult.success(lv, world.isClient());
        }
        return TypedActionResult.pass(lv);
    }

    private BoatEntity createEntity(World world, HitResult hitResult, ItemStack stack, PlayerEntity player) {
        BoatEntity lv2;
        Vec3d lv = hitResult.getPos();
        BoatEntity boatEntity = lv2 = this.chest ? new ChestBoatEntity(world, lv.x, lv.y, lv.z) : new BoatEntity(world, lv.x, lv.y, lv.z);
        if (world instanceof ServerWorld) {
            ServerWorld lv3 = (ServerWorld)world;
            EntityType.copier(lv3, stack, player).accept((ChestBoatEntity)lv2);
        }
        return lv2;
    }
}

