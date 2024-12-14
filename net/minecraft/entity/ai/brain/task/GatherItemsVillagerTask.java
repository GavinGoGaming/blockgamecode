/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.entity.ai.brain.task;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import java.util.Set;
import java.util.stream.Collectors;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.brain.MemoryModuleState;
import net.minecraft.entity.ai.brain.MemoryModuleType;
import net.minecraft.entity.ai.brain.task.LookTargetUtil;
import net.minecraft.entity.ai.brain.task.MultiTickTask;
import net.minecraft.entity.passive.VillagerEntity;
import net.minecraft.inventory.SimpleInventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.village.VillagerProfession;

public class GatherItemsVillagerTask
extends MultiTickTask<VillagerEntity> {
    private Set<Item> items = ImmutableSet.of();

    public GatherItemsVillagerTask() {
        super(ImmutableMap.of(MemoryModuleType.INTERACTION_TARGET, MemoryModuleState.VALUE_PRESENT, MemoryModuleType.VISIBLE_MOBS, MemoryModuleState.VALUE_PRESENT));
    }

    @Override
    protected boolean shouldRun(ServerWorld arg, VillagerEntity arg2) {
        return LookTargetUtil.canSee(arg2.getBrain(), MemoryModuleType.INTERACTION_TARGET, EntityType.VILLAGER);
    }

    @Override
    protected boolean shouldKeepRunning(ServerWorld arg, VillagerEntity arg2, long l) {
        return this.shouldRun(arg, arg2);
    }

    @Override
    protected void run(ServerWorld arg, VillagerEntity arg2, long l) {
        VillagerEntity lv = (VillagerEntity)arg2.getBrain().getOptionalRegisteredMemory(MemoryModuleType.INTERACTION_TARGET).get();
        LookTargetUtil.lookAtAndWalkTowardsEachOther(arg2, lv, 0.5f, 2);
        this.items = GatherItemsVillagerTask.getGatherableItems(arg2, lv);
    }

    @Override
    protected void keepRunning(ServerWorld arg, VillagerEntity arg2, long l) {
        VillagerEntity lv = (VillagerEntity)arg2.getBrain().getOptionalRegisteredMemory(MemoryModuleType.INTERACTION_TARGET).get();
        if (arg2.squaredDistanceTo(lv) > 5.0) {
            return;
        }
        LookTargetUtil.lookAtAndWalkTowardsEachOther(arg2, lv, 0.5f, 2);
        arg2.talkWithVillager(arg, lv, l);
        if (arg2.wantsToStartBreeding() && (arg2.getVillagerData().getProfession() == VillagerProfession.FARMER || lv.canBreed())) {
            GatherItemsVillagerTask.giveHalfOfStack(arg2, VillagerEntity.ITEM_FOOD_VALUES.keySet(), lv);
        }
        if (lv.getVillagerData().getProfession() == VillagerProfession.FARMER && arg2.getInventory().count(Items.WHEAT) > Items.WHEAT.getMaxCount() / 2) {
            GatherItemsVillagerTask.giveHalfOfStack(arg2, ImmutableSet.of(Items.WHEAT), lv);
        }
        if (!this.items.isEmpty() && arg2.getInventory().containsAny(this.items)) {
            GatherItemsVillagerTask.giveHalfOfStack(arg2, this.items, lv);
        }
    }

    @Override
    protected void finishRunning(ServerWorld arg, VillagerEntity arg2, long l) {
        arg2.getBrain().forget(MemoryModuleType.INTERACTION_TARGET);
    }

    private static Set<Item> getGatherableItems(VillagerEntity entity, VillagerEntity target) {
        ImmutableSet<Item> immutableSet = target.getVillagerData().getProfession().gatherableItems();
        ImmutableSet<Item> immutableSet2 = entity.getVillagerData().getProfession().gatherableItems();
        return immutableSet.stream().filter(item -> !immutableSet2.contains(item)).collect(Collectors.toSet());
    }

    private static void giveHalfOfStack(VillagerEntity villager, Set<Item> validItems, LivingEntity target) {
        SimpleInventory lv = villager.getInventory();
        ItemStack lv2 = ItemStack.EMPTY;
        for (int i = 0; i < lv.size(); ++i) {
            int j;
            Item lv4;
            ItemStack lv3 = lv.getStack(i);
            if (lv3.isEmpty() || !validItems.contains(lv4 = lv3.getItem())) continue;
            if (lv3.getCount() > lv3.getMaxCount() / 2) {
                j = lv3.getCount() / 2;
            } else {
                if (lv3.getCount() <= 24) continue;
                j = lv3.getCount() - 24;
            }
            lv3.decrement(j);
            lv2 = new ItemStack(lv4, j);
            break;
        }
        if (!lv2.isEmpty()) {
            LookTargetUtil.give(villager, lv2, target.getPos());
        }
    }

    @Override
    protected /* synthetic */ void finishRunning(ServerWorld world, LivingEntity entity, long time) {
        this.finishRunning(world, (VillagerEntity)entity, time);
    }

    @Override
    protected /* synthetic */ void run(ServerWorld world, LivingEntity entity, long time) {
        this.run(world, (VillagerEntity)entity, time);
    }
}

