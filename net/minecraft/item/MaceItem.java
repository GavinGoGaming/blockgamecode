/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.item;

import java.util.List;
import java.util.function.Predicate;
import net.minecraft.block.BlockState;
import net.minecraft.component.type.AttributeModifierSlot;
import net.minecraft.component.type.AttributeModifiersComponent;
import net.minecraft.component.type.ToolComponent;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.attribute.EntityAttributeModifier;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.decoration.ArmorStandEntity;
import net.minecraft.entity.passive.TameableEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.network.packet.s2c.play.EntityVelocityUpdateS2CPacket;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundEvent;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraft.world.WorldEvents;

public class MaceItem
extends Item {
    private static final int ATTACK_DAMAGE_MODIFIER_VALUE = 5;
    private static final float ATTACK_SPEED_MODIFIER_VALUE = -3.4f;
    public static final float MINING_SPEED_MULTIPLIER = 1.5f;
    private static final float field_50141 = 5.0f;
    public static final float KNOCKBACK_RANGE = 3.5f;
    private static final float KNOCKBACK_POWER = 0.7f;

    public MaceItem(Item.Settings arg) {
        super(arg);
    }

    public static AttributeModifiersComponent createAttributeModifiers() {
        return AttributeModifiersComponent.builder().add(EntityAttributes.GENERIC_ATTACK_DAMAGE, new EntityAttributeModifier(BASE_ATTACK_DAMAGE_MODIFIER_ID, 5.0, EntityAttributeModifier.Operation.ADD_VALUE), AttributeModifierSlot.MAINHAND).add(EntityAttributes.GENERIC_ATTACK_SPEED, new EntityAttributeModifier(BASE_ATTACK_SPEED_MODIFIER_ID, -3.4f, EntityAttributeModifier.Operation.ADD_VALUE), AttributeModifierSlot.MAINHAND).build();
    }

    public static ToolComponent createToolComponent() {
        return new ToolComponent(List.of(), 1.0f, 2);
    }

    @Override
    public boolean canMine(BlockState state, World world, BlockPos pos, PlayerEntity miner) {
        return !miner.isCreative();
    }

    @Override
    public int getEnchantability() {
        return 15;
    }

    @Override
    public boolean postHit(ItemStack stack, LivingEntity target, LivingEntity attacker) {
        ServerPlayerEntity lv;
        if (attacker instanceof ServerPlayerEntity && MaceItem.shouldDealAdditionalDamage(lv = (ServerPlayerEntity)attacker)) {
            ServerWorld lv2 = (ServerWorld)attacker.getWorld();
            if (lv.shouldIgnoreFallDamageFromCurrentExplosion() && lv.currentExplosionImpactPos != null) {
                if (lv.currentExplosionImpactPos.y > lv.getPos().y) {
                    lv.currentExplosionImpactPos = lv.getPos();
                }
            } else {
                lv.currentExplosionImpactPos = lv.getPos();
            }
            lv.setIgnoreFallDamageFromCurrentExplosion(true);
            lv.setVelocity(lv.getVelocity().withAxis(Direction.Axis.Y, 0.01f));
            lv.networkHandler.sendPacket(new EntityVelocityUpdateS2CPacket(lv));
            if (target.isOnGround()) {
                lv.setSpawnExtraParticlesOnFall(true);
                SoundEvent lv3 = lv.fallDistance > 5.0f ? SoundEvents.ITEM_MACE_SMASH_GROUND_HEAVY : SoundEvents.ITEM_MACE_SMASH_GROUND;
                lv2.playSound(null, lv.getX(), lv.getY(), lv.getZ(), lv3, lv.getSoundCategory(), 1.0f, 1.0f);
            } else {
                lv2.playSound(null, lv.getX(), lv.getY(), lv.getZ(), SoundEvents.ITEM_MACE_SMASH_AIR, lv.getSoundCategory(), 1.0f, 1.0f);
            }
            MaceItem.knockbackNearbyEntities(lv2, lv, target);
        }
        return true;
    }

    @Override
    public void postDamageEntity(ItemStack stack, LivingEntity target, LivingEntity attacker) {
        stack.damage(1, attacker, EquipmentSlot.MAINHAND);
        if (MaceItem.shouldDealAdditionalDamage(attacker)) {
            attacker.onLanding();
        }
    }

    @Override
    public boolean canRepair(ItemStack stack, ItemStack ingredient) {
        return ingredient.isOf(Items.BREEZE_ROD);
    }

    @Override
    public float getBonusAttackDamage(Entity target, float baseAttackDamage, DamageSource damageSource) {
        Entity entity = damageSource.getSource();
        if (!(entity instanceof LivingEntity)) {
            return 0.0f;
        }
        LivingEntity lv = (LivingEntity)entity;
        if (!MaceItem.shouldDealAdditionalDamage(lv)) {
            return 0.0f;
        }
        float g = 3.0f;
        float h = 8.0f;
        float i = lv.fallDistance;
        float j = i <= 3.0f ? 4.0f * i : (i <= 8.0f ? 12.0f + 2.0f * (i - 3.0f) : 22.0f + i - 8.0f);
        World world = lv.getWorld();
        if (world instanceof ServerWorld) {
            ServerWorld lv2 = (ServerWorld)world;
            return j + EnchantmentHelper.getSmashDamagePerFallenBlock(lv2, lv.getWeaponStack(), target, damageSource, 0.0f) * i;
        }
        return j;
    }

    private static void knockbackNearbyEntities(World world, PlayerEntity player, Entity attacked) {
        world.syncWorldEvent(WorldEvents.SMASH_ATTACK, attacked.getSteppingPos(), 750);
        world.getEntitiesByClass(LivingEntity.class, attacked.getBoundingBox().expand(3.5), MaceItem.getKnockbackPredicate(player, attacked)).forEach(entity -> {
            Vec3d lv = entity.getPos().subtract(attacked.getPos());
            double d = MaceItem.getKnockback(player, entity, lv);
            Vec3d lv2 = lv.normalize().multiply(d);
            if (d > 0.0) {
                entity.addVelocity(lv2.x, 0.7f, lv2.z);
                if (entity instanceof ServerPlayerEntity) {
                    ServerPlayerEntity lv3 = (ServerPlayerEntity)entity;
                    lv3.networkHandler.sendPacket(new EntityVelocityUpdateS2CPacket(lv3));
                }
            }
        });
    }

    private static Predicate<LivingEntity> getKnockbackPredicate(PlayerEntity player, Entity attacked) {
        return entity -> {
            ArmorStandEntity lv2;
            TameableEntity lv;
            boolean bl = !entity.isSpectator();
            boolean bl2 = entity != player && entity != attacked;
            boolean bl3 = !player.isTeammate((Entity)entity);
            boolean bl4 = !(entity instanceof TameableEntity && (lv = (TameableEntity)entity).isTamed() && player.getUuid().equals(lv.getOwnerUuid()));
            boolean bl5 = !(entity instanceof ArmorStandEntity) || !(lv2 = (ArmorStandEntity)entity).isMarker();
            boolean bl6 = attacked.squaredDistanceTo((Entity)entity) <= Math.pow(3.5, 2.0);
            return bl && bl2 && bl3 && bl4 && bl5 && bl6;
        };
    }

    private static double getKnockback(PlayerEntity player, LivingEntity attacked, Vec3d distance) {
        return (3.5 - distance.length()) * (double)0.7f * (double)(player.fallDistance > 5.0f ? 2 : 1) * (1.0 - attacked.getAttributeValue(EntityAttributes.GENERIC_KNOCKBACK_RESISTANCE));
    }

    public static boolean shouldDealAdditionalDamage(LivingEntity attacker) {
        return attacker.fallDistance > 1.5f && !attacker.isFallFlying();
    }
}

