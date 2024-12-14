/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 * 
 * Could not load the following classes:
 *  org.jetbrains.annotations.Nullable
 */
package net.minecraft.entity.ai.goal;

import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.ItemEnchantmentsComponent;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.enchantment.provider.EnchantmentProviders;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.LightningEntity;
import net.minecraft.entity.SpawnReason;
import net.minecraft.entity.ai.goal.Goal;
import net.minecraft.entity.mob.SkeletonEntity;
import net.minecraft.entity.mob.SkeletonHorseEntity;
import net.minecraft.entity.passive.AbstractHorseEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.world.LocalDifficulty;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

public class SkeletonHorseTrapTriggerGoal
extends Goal {
    private final SkeletonHorseEntity skeletonHorse;

    public SkeletonHorseTrapTriggerGoal(SkeletonHorseEntity skeletonHorse) {
        this.skeletonHorse = skeletonHorse;
    }

    @Override
    public boolean canStart() {
        return this.skeletonHorse.getWorld().isPlayerInRange(this.skeletonHorse.getX(), this.skeletonHorse.getY(), this.skeletonHorse.getZ(), 10.0);
    }

    @Override
    public void tick() {
        ServerWorld lv = (ServerWorld)this.skeletonHorse.getWorld();
        LocalDifficulty lv2 = lv.getLocalDifficulty(this.skeletonHorse.getBlockPos());
        this.skeletonHorse.setTrapped(false);
        this.skeletonHorse.setTame(true);
        this.skeletonHorse.setBreedingAge(0);
        LightningEntity lv3 = EntityType.LIGHTNING_BOLT.create(lv);
        if (lv3 == null) {
            return;
        }
        lv3.refreshPositionAfterTeleport(this.skeletonHorse.getX(), this.skeletonHorse.getY(), this.skeletonHorse.getZ());
        lv3.setCosmetic(true);
        lv.spawnEntity(lv3);
        SkeletonEntity lv4 = this.getSkeleton(lv2, this.skeletonHorse);
        if (lv4 == null) {
            return;
        }
        lv4.startRiding(this.skeletonHorse);
        lv.spawnEntityAndPassengers(lv4);
        for (int i = 0; i < 3; ++i) {
            SkeletonEntity lv6;
            AbstractHorseEntity lv5 = this.getHorse(lv2);
            if (lv5 == null || (lv6 = this.getSkeleton(lv2, lv5)) == null) continue;
            lv6.startRiding(lv5);
            lv5.addVelocity(this.skeletonHorse.getRandom().nextTriangular(0.0, 1.1485), 0.0, this.skeletonHorse.getRandom().nextTriangular(0.0, 1.1485));
            lv.spawnEntityAndPassengers(lv5);
        }
    }

    @Nullable
    private AbstractHorseEntity getHorse(LocalDifficulty localDifficulty) {
        SkeletonHorseEntity lv = EntityType.SKELETON_HORSE.create((World)this.skeletonHorse.getWorld());
        if (lv != null) {
            lv.initialize((ServerWorld)this.skeletonHorse.getWorld(), localDifficulty, SpawnReason.TRIGGERED, null);
            lv.setPosition(this.skeletonHorse.getX(), this.skeletonHorse.getY(), this.skeletonHorse.getZ());
            lv.timeUntilRegen = 60;
            lv.setPersistent();
            lv.setTame(true);
            lv.setBreedingAge(0);
        }
        return lv;
    }

    @Nullable
    private SkeletonEntity getSkeleton(LocalDifficulty localDifficulty, AbstractHorseEntity vehicle) {
        SkeletonEntity lv = EntityType.SKELETON.create((World)vehicle.getWorld());
        if (lv != null) {
            lv.initialize((ServerWorld)vehicle.getWorld(), localDifficulty, SpawnReason.TRIGGERED, null);
            lv.setPosition(vehicle.getX(), vehicle.getY(), vehicle.getZ());
            lv.timeUntilRegen = 60;
            lv.setPersistent();
            if (lv.getEquippedStack(EquipmentSlot.HEAD).isEmpty()) {
                lv.equipStack(EquipmentSlot.HEAD, new ItemStack(Items.IRON_HELMET));
            }
            this.enchantEquipment(lv, EquipmentSlot.MAINHAND, localDifficulty);
            this.enchantEquipment(lv, EquipmentSlot.HEAD, localDifficulty);
        }
        return lv;
    }

    private void enchantEquipment(SkeletonEntity rider, EquipmentSlot slot, LocalDifficulty localDifficulty) {
        ItemStack lv = rider.getEquippedStack(slot);
        lv.set(DataComponentTypes.ENCHANTMENTS, ItemEnchantmentsComponent.DEFAULT);
        EnchantmentHelper.applyEnchantmentProvider(lv, rider.getWorld().getRegistryManager(), EnchantmentProviders.MOB_SPAWN_EQUIPMENT, localDifficulty, rider.getRandom());
        rider.equipStack(slot, lv);
    }
}

