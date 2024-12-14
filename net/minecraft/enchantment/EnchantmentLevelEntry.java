/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.enchantment;

import net.minecraft.enchantment.Enchantment;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.util.collection.Weighted;

public class EnchantmentLevelEntry
extends Weighted.Absent {
    public final RegistryEntry<Enchantment> enchantment;
    public final int level;

    public EnchantmentLevelEntry(RegistryEntry<Enchantment> enchantment, int level) {
        super(enchantment.value().getWeight());
        this.enchantment = enchantment;
        this.level = level;
    }
}

