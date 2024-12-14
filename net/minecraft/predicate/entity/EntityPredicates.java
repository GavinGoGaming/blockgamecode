/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 * 
 * Could not load the following classes:
 *  org.jetbrains.annotations.Nullable
 */
package net.minecraft.predicate.entity;

import com.google.common.base.Predicates;
import java.util.function.Predicate;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;
import net.minecraft.scoreboard.AbstractTeam;
import net.minecraft.scoreboard.Team;
import org.jetbrains.annotations.Nullable;

public final class EntityPredicates {
    public static final Predicate<Entity> VALID_ENTITY = Entity::isAlive;
    public static final Predicate<Entity> VALID_LIVING_ENTITY = entity -> entity.isAlive() && entity instanceof LivingEntity;
    public static final Predicate<Entity> NOT_MOUNTED = entity -> entity.isAlive() && !entity.hasPassengers() && !entity.hasVehicle();
    public static final Predicate<Entity> VALID_INVENTORIES = entity -> entity instanceof Inventory && entity.isAlive();
    public static final Predicate<Entity> EXCEPT_CREATIVE_OR_SPECTATOR = entity -> !(entity instanceof PlayerEntity) || !entity.isSpectator() && !((PlayerEntity)entity).isCreative();
    public static final Predicate<Entity> EXCEPT_SPECTATOR = entity -> !entity.isSpectator();
    public static final Predicate<Entity> CAN_COLLIDE = EXCEPT_SPECTATOR.and(Entity::isCollidable);

    private EntityPredicates() {
    }

    public static Predicate<Entity> maxDistance(double x, double y, double z, double max) {
        double h = max * max;
        return entity -> entity != null && entity.squaredDistanceTo(x, y, z) <= h;
    }

    public static Predicate<Entity> canBePushedBy(Entity entity) {
        AbstractTeam.CollisionRule lv2;
        Team lv = entity.getScoreboardTeam();
        AbstractTeam.CollisionRule collisionRule = lv2 = lv == null ? AbstractTeam.CollisionRule.ALWAYS : ((AbstractTeam)lv).getCollisionRule();
        if (lv2 == AbstractTeam.CollisionRule.NEVER) {
            return Predicates.alwaysFalse();
        }
        return EXCEPT_SPECTATOR.and(entityx -> {
            boolean bl;
            AbstractTeam.CollisionRule lv2;
            if (!entityx.isPushable()) {
                return false;
            }
            if (!(!arg.getWorld().isClient || entityx instanceof PlayerEntity && ((PlayerEntity)entityx).isMainPlayer())) {
                return false;
            }
            Team lv = entityx.getScoreboardTeam();
            AbstractTeam.CollisionRule collisionRule = lv2 = lv == null ? AbstractTeam.CollisionRule.ALWAYS : ((AbstractTeam)lv).getCollisionRule();
            if (lv2 == AbstractTeam.CollisionRule.NEVER) {
                return false;
            }
            boolean bl2 = bl = lv != null && lv.isEqual(lv);
            if ((lv2 == AbstractTeam.CollisionRule.PUSH_OWN_TEAM || lv2 == AbstractTeam.CollisionRule.PUSH_OWN_TEAM) && bl) {
                return false;
            }
            return lv2 != AbstractTeam.CollisionRule.PUSH_OTHER_TEAMS && lv2 != AbstractTeam.CollisionRule.PUSH_OTHER_TEAMS || bl;
        });
    }

    public static Predicate<Entity> rides(Entity entity) {
        return testedEntity -> {
            while (testedEntity.hasVehicle()) {
                if ((testedEntity = testedEntity.getVehicle()) != entity) continue;
                return false;
            }
            return true;
        };
    }

    public static class Equipable
    implements Predicate<Entity> {
        private final ItemStack stack;

        public Equipable(ItemStack stack) {
            this.stack = stack;
        }

        @Override
        public boolean test(@Nullable Entity arg) {
            if (!arg.isAlive()) {
                return false;
            }
            if (!(arg instanceof LivingEntity)) {
                return false;
            }
            LivingEntity lv = (LivingEntity)arg;
            return lv.canEquip(this.stack);
        }

        @Override
        public /* synthetic */ boolean test(@Nullable Object context) {
            return this.test((Entity)context);
        }
    }
}

