/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 * 
 * Could not load the following classes:
 *  org.jetbrains.annotations.Nullable
 */
package net.minecraft.entity;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.EquipmentTable;
import net.minecraft.item.Equipment;
import net.minecraft.item.ItemStack;
import net.minecraft.loot.LootTable;
import net.minecraft.loot.LootTables;
import net.minecraft.loot.context.LootContextParameterSet;
import net.minecraft.registry.RegistryKey;
import org.jetbrains.annotations.Nullable;

public interface EquipmentHolder {
    public void equipStack(EquipmentSlot var1, ItemStack var2);

    public ItemStack getEquippedStack(EquipmentSlot var1);

    public void setEquipmentDropChance(EquipmentSlot var1, float var2);

    default public void setEquipmentFromTable(EquipmentTable equipmentTable, LootContextParameterSet parameters) {
        this.setEquipmentFromTable(equipmentTable.lootTable(), parameters, equipmentTable.slotDropChances());
    }

    default public void setEquipmentFromTable(RegistryKey<LootTable> lootTable, LootContextParameterSet parameters, Map<EquipmentSlot, Float> slotDropChances) {
        this.setEquipmentFromTable(lootTable, parameters, 0L, slotDropChances);
    }

    default public void setEquipmentFromTable(RegistryKey<LootTable> lootTable, LootContextParameterSet parameters, long seed, Map<EquipmentSlot, Float> slotDropChances) {
        if (lootTable.equals(LootTables.EMPTY)) {
            return;
        }
        LootTable lv = parameters.getWorld().getServer().getReloadableRegistries().getLootTable(lootTable);
        if (lv == LootTable.EMPTY) {
            return;
        }
        ObjectArrayList<ItemStack> list = lv.generateLoot(parameters, seed);
        ArrayList<EquipmentSlot> list2 = new ArrayList<EquipmentSlot>();
        for (ItemStack lv2 : list) {
            EquipmentSlot lv3 = this.getSlotForStack(lv2, list2);
            if (lv3 == null) continue;
            ItemStack lv4 = lv3.split(lv2);
            this.equipStack(lv3, lv4);
            Float float_ = slotDropChances.get(lv3);
            if (float_ != null) {
                this.setEquipmentDropChance(lv3, float_.floatValue());
            }
            list2.add(lv3);
        }
    }

    @Nullable
    default public EquipmentSlot getSlotForStack(ItemStack stack, List<EquipmentSlot> slotBlacklist) {
        if (stack.isEmpty()) {
            return null;
        }
        Equipment lv = Equipment.fromStack(stack);
        if (lv != null) {
            EquipmentSlot lv2 = lv.getSlotType();
            if (!slotBlacklist.contains(lv2)) {
                return lv2;
            }
        } else if (!slotBlacklist.contains(EquipmentSlot.MAINHAND)) {
            return EquipmentSlot.MAINHAND;
        }
        return null;
    }
}

