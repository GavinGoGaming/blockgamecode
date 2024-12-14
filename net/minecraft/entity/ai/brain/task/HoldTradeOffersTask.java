/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 * 
 * Could not load the following classes:
 *  org.jetbrains.annotations.Nullable
 */
package net.minecraft.entity.ai.brain.task;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import java.util.List;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.brain.Brain;
import net.minecraft.entity.ai.brain.EntityLookTarget;
import net.minecraft.entity.ai.brain.MemoryModuleState;
import net.minecraft.entity.ai.brain.MemoryModuleType;
import net.minecraft.entity.ai.brain.task.MultiTickTask;
import net.minecraft.entity.passive.VillagerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.village.TradeOffer;
import org.jetbrains.annotations.Nullable;

public class HoldTradeOffersTask
extends MultiTickTask<VillagerEntity> {
    private static final int RUN_INTERVAL = 900;
    private static final int OFFER_SHOWING_INTERVAL = 40;
    @Nullable
    private ItemStack customerHeldStack;
    private final List<ItemStack> offers = Lists.newArrayList();
    private int offerShownTicks;
    private int offerIndex;
    private int ticksLeft;

    public HoldTradeOffersTask(int minRunTime, int maxRunTime) {
        super(ImmutableMap.of(MemoryModuleType.INTERACTION_TARGET, MemoryModuleState.VALUE_PRESENT), minRunTime, maxRunTime);
    }

    @Override
    public boolean shouldRun(ServerWorld arg, VillagerEntity arg2) {
        Brain<VillagerEntity> lv = arg2.getBrain();
        if (lv.getOptionalRegisteredMemory(MemoryModuleType.INTERACTION_TARGET).isEmpty()) {
            return false;
        }
        LivingEntity lv2 = lv.getOptionalRegisteredMemory(MemoryModuleType.INTERACTION_TARGET).get();
        return lv2.getType() == EntityType.PLAYER && arg2.isAlive() && lv2.isAlive() && !arg2.isBaby() && arg2.squaredDistanceTo(lv2) <= 17.0;
    }

    @Override
    public boolean shouldKeepRunning(ServerWorld arg, VillagerEntity arg2, long l) {
        return this.shouldRun(arg, arg2) && this.ticksLeft > 0 && arg2.getBrain().getOptionalRegisteredMemory(MemoryModuleType.INTERACTION_TARGET).isPresent();
    }

    @Override
    public void run(ServerWorld arg, VillagerEntity arg2, long l) {
        super.run(arg, arg2, l);
        this.findPotentialCustomer(arg2);
        this.offerShownTicks = 0;
        this.offerIndex = 0;
        this.ticksLeft = 40;
    }

    @Override
    public void keepRunning(ServerWorld arg, VillagerEntity arg2, long l) {
        LivingEntity lv = this.findPotentialCustomer(arg2);
        this.setupOffers(lv, arg2);
        if (!this.offers.isEmpty()) {
            this.refreshShownOffer(arg2);
        } else {
            HoldTradeOffersTask.holdNothing(arg2);
            this.ticksLeft = Math.min(this.ticksLeft, 40);
        }
        --this.ticksLeft;
    }

    @Override
    public void finishRunning(ServerWorld arg, VillagerEntity arg2, long l) {
        super.finishRunning(arg, arg2, l);
        arg2.getBrain().forget(MemoryModuleType.INTERACTION_TARGET);
        HoldTradeOffersTask.holdNothing(arg2);
        this.customerHeldStack = null;
    }

    private void setupOffers(LivingEntity customer, VillagerEntity villager) {
        boolean bl = false;
        ItemStack lv = customer.getMainHandStack();
        if (this.customerHeldStack == null || !ItemStack.areItemsEqual(this.customerHeldStack, lv)) {
            this.customerHeldStack = lv;
            bl = true;
            this.offers.clear();
        }
        if (bl && !this.customerHeldStack.isEmpty()) {
            this.loadPossibleOffers(villager);
            if (!this.offers.isEmpty()) {
                this.ticksLeft = 900;
                this.holdOffer(villager);
            }
        }
    }

    private void holdOffer(VillagerEntity villager) {
        HoldTradeOffersTask.holdOffer(villager, this.offers.get(0));
    }

    private void loadPossibleOffers(VillagerEntity villager) {
        for (TradeOffer lv : villager.getOffers()) {
            if (lv.isDisabled() || !this.isPossible(lv)) continue;
            this.offers.add(lv.copySellItem());
        }
    }

    private boolean isPossible(TradeOffer offer) {
        return ItemStack.areItemsEqual(this.customerHeldStack, offer.getDisplayedFirstBuyItem()) || ItemStack.areItemsEqual(this.customerHeldStack, offer.getDisplayedSecondBuyItem());
    }

    private static void holdNothing(VillagerEntity villager) {
        villager.equipStack(EquipmentSlot.MAINHAND, ItemStack.EMPTY);
        villager.setEquipmentDropChance(EquipmentSlot.MAINHAND, 0.085f);
    }

    private static void holdOffer(VillagerEntity villager, ItemStack stack) {
        villager.equipStack(EquipmentSlot.MAINHAND, stack);
        villager.setEquipmentDropChance(EquipmentSlot.MAINHAND, 0.0f);
    }

    private LivingEntity findPotentialCustomer(VillagerEntity villager) {
        Brain<VillagerEntity> lv = villager.getBrain();
        LivingEntity lv2 = lv.getOptionalRegisteredMemory(MemoryModuleType.INTERACTION_TARGET).get();
        lv.remember(MemoryModuleType.LOOK_TARGET, new EntityLookTarget(lv2, true));
        return lv2;
    }

    private void refreshShownOffer(VillagerEntity villager) {
        if (this.offers.size() >= 2 && ++this.offerShownTicks >= 40) {
            ++this.offerIndex;
            this.offerShownTicks = 0;
            if (this.offerIndex > this.offers.size() - 1) {
                this.offerIndex = 0;
            }
            HoldTradeOffersTask.holdOffer(villager, this.offers.get(this.offerIndex));
        }
    }

    @Override
    public /* synthetic */ void finishRunning(ServerWorld world, LivingEntity entity, long time) {
        this.finishRunning(world, (VillagerEntity)entity, time);
    }

    @Override
    public /* synthetic */ void run(ServerWorld world, LivingEntity entity, long time) {
        this.run(world, (VillagerEntity)entity, time);
    }
}

