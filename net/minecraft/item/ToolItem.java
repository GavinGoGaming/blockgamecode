/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.item;

import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ToolMaterial;

public class ToolItem
extends Item {
    private final ToolMaterial material;

    public ToolItem(ToolMaterial material, Item.Settings settings) {
        super(settings.maxDamage(material.getDurability()));
        this.material = material;
    }

    public ToolMaterial getMaterial() {
        return this.material;
    }

    @Override
    public int getEnchantability() {
        return this.material.getEnchantability();
    }

    @Override
    public boolean canRepair(ItemStack stack, ItemStack ingredient) {
        return this.material.getRepairIngredient().test(ingredient) || super.canRepair(stack, ingredient);
    }
}

