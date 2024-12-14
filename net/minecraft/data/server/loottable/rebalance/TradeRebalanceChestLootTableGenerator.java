/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.data.server.loottable.rebalance;

import java.util.function.BiConsumer;
import net.minecraft.block.Blocks;
import net.minecraft.data.server.loottable.LootTableGenerator;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.item.Items;
import net.minecraft.loot.LootPool;
import net.minecraft.loot.LootTable;
import net.minecraft.loot.LootTables;
import net.minecraft.loot.entry.EmptyEntry;
import net.minecraft.loot.entry.ItemEntry;
import net.minecraft.loot.entry.LeafEntry;
import net.minecraft.loot.entry.LootPoolEntry;
import net.minecraft.loot.function.EnchantRandomlyLootFunction;
import net.minecraft.loot.function.EnchantWithLevelsLootFunction;
import net.minecraft.loot.function.SetCountLootFunction;
import net.minecraft.loot.function.SetDamageLootFunction;
import net.minecraft.loot.function.SetInstrumentLootFunction;
import net.minecraft.loot.function.SetPotionLootFunction;
import net.minecraft.loot.provider.number.ConstantLootNumberProvider;
import net.minecraft.loot.provider.number.UniformLootNumberProvider;
import net.minecraft.potion.Potions;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.registry.tag.InstrumentTags;

public record TradeRebalanceChestLootTableGenerator(RegistryWrapper.WrapperLookup registries) implements LootTableGenerator
{
    @Override
    public void accept(BiConsumer<RegistryKey<LootTable>, LootTable.Builder> lootTableBiConsumer) {
        RegistryWrapper.Impl<Enchantment> lv = this.registries.getWrapperOrThrow(RegistryKeys.ENCHANTMENT);
        lootTableBiConsumer.accept(LootTables.ABANDONED_MINESHAFT_CHEST, LootTable.builder().pool(LootPool.builder().rolls(ConstantLootNumberProvider.create(1.0f)).with((LootPoolEntry.Builder<?>)ItemEntry.builder(Items.GOLDEN_APPLE).weight(20)).with(ItemEntry.builder(Items.ENCHANTED_GOLDEN_APPLE)).with((LootPoolEntry.Builder<?>)ItemEntry.builder(Items.NAME_TAG).weight(30)).with((LootPoolEntry.Builder<?>)((Object)((LeafEntry.Builder)ItemEntry.builder(Items.BOOK).weight(10)).apply(EnchantRandomlyLootFunction.builder(this.registries)))).with((LootPoolEntry.Builder<?>)ItemEntry.builder(Items.IRON_PICKAXE).weight(5)).with((LootPoolEntry.Builder<?>)EmptyEntry.builder().weight(5))).pool(LootPool.builder().rolls(UniformLootNumberProvider.create(2.0f, 4.0f)).with((LootPoolEntry.Builder<?>)((Object)((LeafEntry.Builder)ItemEntry.builder(Items.IRON_INGOT).weight(10)).apply(SetCountLootFunction.builder(UniformLootNumberProvider.create(1.0f, 5.0f))))).with((LootPoolEntry.Builder<?>)((Object)((LeafEntry.Builder)ItemEntry.builder(Items.GOLD_INGOT).weight(5)).apply(SetCountLootFunction.builder(UniformLootNumberProvider.create(1.0f, 3.0f))))).with((LootPoolEntry.Builder<?>)((Object)((LeafEntry.Builder)ItemEntry.builder(Items.REDSTONE).weight(5)).apply(SetCountLootFunction.builder(UniformLootNumberProvider.create(4.0f, 9.0f))))).with((LootPoolEntry.Builder<?>)((Object)((LeafEntry.Builder)ItemEntry.builder(Items.LAPIS_LAZULI).weight(5)).apply(SetCountLootFunction.builder(UniformLootNumberProvider.create(4.0f, 9.0f))))).with((LootPoolEntry.Builder<?>)((Object)((LeafEntry.Builder)ItemEntry.builder(Items.DIAMOND).weight(3)).apply(SetCountLootFunction.builder(UniformLootNumberProvider.create(1.0f, 2.0f))))).with((LootPoolEntry.Builder<?>)((Object)((LeafEntry.Builder)ItemEntry.builder(Items.COAL).weight(10)).apply(SetCountLootFunction.builder(UniformLootNumberProvider.create(3.0f, 8.0f))))).with((LootPoolEntry.Builder<?>)((Object)((LeafEntry.Builder)ItemEntry.builder(Items.BREAD).weight(15)).apply(SetCountLootFunction.builder(UniformLootNumberProvider.create(1.0f, 3.0f))))).with((LootPoolEntry.Builder<?>)((Object)((LeafEntry.Builder)ItemEntry.builder(Items.GLOW_BERRIES).weight(15)).apply(SetCountLootFunction.builder(UniformLootNumberProvider.create(3.0f, 6.0f))))).with((LootPoolEntry.Builder<?>)((Object)((LeafEntry.Builder)ItemEntry.builder(Items.MELON_SEEDS).weight(10)).apply(SetCountLootFunction.builder(UniformLootNumberProvider.create(2.0f, 4.0f))))).with((LootPoolEntry.Builder<?>)((Object)((LeafEntry.Builder)ItemEntry.builder(Items.PUMPKIN_SEEDS).weight(10)).apply(SetCountLootFunction.builder(UniformLootNumberProvider.create(2.0f, 4.0f))))).with((LootPoolEntry.Builder<?>)((Object)((LeafEntry.Builder)ItemEntry.builder(Items.BEETROOT_SEEDS).weight(10)).apply(SetCountLootFunction.builder(UniformLootNumberProvider.create(2.0f, 4.0f)))))).pool(LootPool.builder().rolls(ConstantLootNumberProvider.create(3.0f)).with((LootPoolEntry.Builder<?>)((Object)((LeafEntry.Builder)ItemEntry.builder(Blocks.RAIL).weight(20)).apply(SetCountLootFunction.builder(UniformLootNumberProvider.create(4.0f, 8.0f))))).with((LootPoolEntry.Builder<?>)((Object)((LeafEntry.Builder)ItemEntry.builder(Blocks.POWERED_RAIL).weight(5)).apply(SetCountLootFunction.builder(UniformLootNumberProvider.create(1.0f, 4.0f))))).with((LootPoolEntry.Builder<?>)((Object)((LeafEntry.Builder)ItemEntry.builder(Blocks.DETECTOR_RAIL).weight(5)).apply(SetCountLootFunction.builder(UniformLootNumberProvider.create(1.0f, 4.0f))))).with((LootPoolEntry.Builder<?>)((Object)((LeafEntry.Builder)ItemEntry.builder(Blocks.ACTIVATOR_RAIL).weight(5)).apply(SetCountLootFunction.builder(UniformLootNumberProvider.create(1.0f, 4.0f))))).with((LootPoolEntry.Builder<?>)((Object)((LeafEntry.Builder)ItemEntry.builder(Blocks.TORCH).weight(15)).apply(SetCountLootFunction.builder(UniformLootNumberProvider.create(1.0f, 16.0f)))))).pool(LootPool.builder().rolls(ConstantLootNumberProvider.create(1.0f)).with((LootPoolEntry.Builder<?>)EmptyEntry.builder().weight(4)).with((LootPoolEntry.Builder<?>)((Object)((LeafEntry.Builder)ItemEntry.builder(Items.BOOK).weight(1)).apply(new EnchantRandomlyLootFunction.Builder().option(lv.getOrThrow(Enchantments.EFFICIENCY)))))));
        lootTableBiConsumer.accept(LootTables.ANCIENT_CITY_CHEST, this.createAncientCityChestTableBuilder());
        lootTableBiConsumer.accept(LootTables.DESERT_PYRAMID_CHEST, this.createDesertPyramidChestTableBuilder());
        lootTableBiConsumer.accept(LootTables.JUNGLE_TEMPLE_CHEST, this.createJungleTempleChestTableBuilder());
        lootTableBiConsumer.accept(LootTables.PILLAGER_OUTPOST_CHEST, this.createPillagerOutpostChestTableBuilder());
    }

    public LootTable.Builder createPillagerOutpostChestTableBuilder() {
        RegistryWrapper.Impl<Enchantment> lv = this.registries.getWrapperOrThrow(RegistryKeys.ENCHANTMENT);
        return LootTable.builder().pool(LootPool.builder().rolls(UniformLootNumberProvider.create(0.0f, 1.0f)).with(ItemEntry.builder(Items.CROSSBOW))).pool(LootPool.builder().rolls(UniformLootNumberProvider.create(2.0f, 3.0f)).with((LootPoolEntry.Builder<?>)((Object)((LeafEntry.Builder)ItemEntry.builder(Items.WHEAT).weight(7)).apply(SetCountLootFunction.builder(UniformLootNumberProvider.create(3.0f, 5.0f))))).with((LootPoolEntry.Builder<?>)((Object)((LeafEntry.Builder)ItemEntry.builder(Items.POTATO).weight(5)).apply(SetCountLootFunction.builder(UniformLootNumberProvider.create(2.0f, 5.0f))))).with((LootPoolEntry.Builder<?>)((Object)((LeafEntry.Builder)ItemEntry.builder(Items.CARROT).weight(5)).apply(SetCountLootFunction.builder(UniformLootNumberProvider.create(3.0f, 5.0f)))))).pool(LootPool.builder().rolls(UniformLootNumberProvider.create(1.0f, 3.0f)).with((LootPoolEntry.Builder<?>)((Object)ItemEntry.builder(Blocks.DARK_OAK_LOG).apply(SetCountLootFunction.builder(UniformLootNumberProvider.create(2.0f, 3.0f)))))).pool(LootPool.builder().rolls(UniformLootNumberProvider.create(2.0f, 3.0f)).with((LootPoolEntry.Builder<?>)ItemEntry.builder(Items.EXPERIENCE_BOTTLE).weight(7)).with((LootPoolEntry.Builder<?>)((Object)((LeafEntry.Builder)ItemEntry.builder(Items.STRING).weight(4)).apply(SetCountLootFunction.builder(UniformLootNumberProvider.create(1.0f, 6.0f))))).with((LootPoolEntry.Builder<?>)((Object)((LeafEntry.Builder)ItemEntry.builder(Items.ARROW).weight(4)).apply(SetCountLootFunction.builder(UniformLootNumberProvider.create(2.0f, 7.0f))))).with((LootPoolEntry.Builder<?>)((Object)((LeafEntry.Builder)ItemEntry.builder(Items.TRIPWIRE_HOOK).weight(3)).apply(SetCountLootFunction.builder(UniformLootNumberProvider.create(1.0f, 3.0f))))).with((LootPoolEntry.Builder<?>)((Object)((LeafEntry.Builder)ItemEntry.builder(Items.IRON_INGOT).weight(3)).apply(SetCountLootFunction.builder(UniformLootNumberProvider.create(1.0f, 3.0f))))).with((LootPoolEntry.Builder<?>)((Object)((LeafEntry.Builder)ItemEntry.builder(Items.BOOK).weight(1)).apply(EnchantRandomlyLootFunction.builder(this.registries))))).pool(LootPool.builder().rolls(UniformLootNumberProvider.create(0.0f, 1.0f)).with(ItemEntry.builder(Items.GOAT_HORN)).apply(SetInstrumentLootFunction.builder(InstrumentTags.REGULAR_GOAT_HORNS))).pool(LootPool.builder().rolls(ConstantLootNumberProvider.create(1.0f)).with((LootPoolEntry.Builder<?>)EmptyEntry.builder().weight(3)).with((LootPoolEntry.Builder<?>)((Object)((LeafEntry.Builder)ItemEntry.builder(Items.SENTRY_ARMOR_TRIM_SMITHING_TEMPLATE).weight(1)).apply(SetCountLootFunction.builder(ConstantLootNumberProvider.create(2.0f)))))).pool(LootPool.builder().rolls(ConstantLootNumberProvider.create(1.0f)).with((LootPoolEntry.Builder<?>)EmptyEntry.builder().weight(1)).with((LootPoolEntry.Builder<?>)((Object)((LeafEntry.Builder)ItemEntry.builder(Items.BOOK).weight(2)).apply(new EnchantRandomlyLootFunction.Builder().option(lv.getOrThrow(Enchantments.QUICK_CHARGE))))));
    }

    public LootTable.Builder createDesertPyramidChestTableBuilder() {
        RegistryWrapper.Impl<Enchantment> lv = this.registries.getWrapperOrThrow(RegistryKeys.ENCHANTMENT);
        return LootTable.builder().pool(LootPool.builder().rolls(UniformLootNumberProvider.create(2.0f, 4.0f)).with((LootPoolEntry.Builder<?>)((Object)((LeafEntry.Builder)ItemEntry.builder(Items.DIAMOND).weight(5)).apply(SetCountLootFunction.builder(UniformLootNumberProvider.create(1.0f, 3.0f))))).with((LootPoolEntry.Builder<?>)((Object)((LeafEntry.Builder)ItemEntry.builder(Items.IRON_INGOT).weight(15)).apply(SetCountLootFunction.builder(UniformLootNumberProvider.create(1.0f, 5.0f))))).with((LootPoolEntry.Builder<?>)((Object)((LeafEntry.Builder)ItemEntry.builder(Items.GOLD_INGOT).weight(15)).apply(SetCountLootFunction.builder(UniformLootNumberProvider.create(2.0f, 7.0f))))).with((LootPoolEntry.Builder<?>)((Object)((LeafEntry.Builder)ItemEntry.builder(Items.EMERALD).weight(15)).apply(SetCountLootFunction.builder(UniformLootNumberProvider.create(1.0f, 3.0f))))).with((LootPoolEntry.Builder<?>)((Object)((LeafEntry.Builder)ItemEntry.builder(Items.BONE).weight(25)).apply(SetCountLootFunction.builder(UniformLootNumberProvider.create(4.0f, 6.0f))))).with((LootPoolEntry.Builder<?>)((Object)((LeafEntry.Builder)ItemEntry.builder(Items.SPIDER_EYE).weight(25)).apply(SetCountLootFunction.builder(UniformLootNumberProvider.create(1.0f, 3.0f))))).with((LootPoolEntry.Builder<?>)((Object)((LeafEntry.Builder)ItemEntry.builder(Items.ROTTEN_FLESH).weight(25)).apply(SetCountLootFunction.builder(UniformLootNumberProvider.create(3.0f, 7.0f))))).with((LootPoolEntry.Builder<?>)ItemEntry.builder(Items.SADDLE).weight(20)).with((LootPoolEntry.Builder<?>)ItemEntry.builder(Items.IRON_HORSE_ARMOR).weight(15)).with((LootPoolEntry.Builder<?>)ItemEntry.builder(Items.GOLDEN_HORSE_ARMOR).weight(10)).with((LootPoolEntry.Builder<?>)ItemEntry.builder(Items.DIAMOND_HORSE_ARMOR).weight(5)).with((LootPoolEntry.Builder<?>)((Object)((LeafEntry.Builder)ItemEntry.builder(Items.BOOK).weight(10)).apply(EnchantRandomlyLootFunction.builder(this.registries)))).with((LootPoolEntry.Builder<?>)ItemEntry.builder(Items.GOLDEN_APPLE).weight(20)).with((LootPoolEntry.Builder<?>)ItemEntry.builder(Items.ENCHANTED_GOLDEN_APPLE).weight(2)).with((LootPoolEntry.Builder<?>)EmptyEntry.builder().weight(15))).pool(LootPool.builder().rolls(ConstantLootNumberProvider.create(4.0f)).with((LootPoolEntry.Builder<?>)((Object)((LeafEntry.Builder)ItemEntry.builder(Items.BONE).weight(10)).apply(SetCountLootFunction.builder(UniformLootNumberProvider.create(1.0f, 8.0f))))).with((LootPoolEntry.Builder<?>)((Object)((LeafEntry.Builder)ItemEntry.builder(Items.GUNPOWDER).weight(10)).apply(SetCountLootFunction.builder(UniformLootNumberProvider.create(1.0f, 8.0f))))).with((LootPoolEntry.Builder<?>)((Object)((LeafEntry.Builder)ItemEntry.builder(Items.ROTTEN_FLESH).weight(10)).apply(SetCountLootFunction.builder(UniformLootNumberProvider.create(1.0f, 8.0f))))).with((LootPoolEntry.Builder<?>)((Object)((LeafEntry.Builder)ItemEntry.builder(Items.STRING).weight(10)).apply(SetCountLootFunction.builder(UniformLootNumberProvider.create(1.0f, 8.0f))))).with((LootPoolEntry.Builder<?>)((Object)((LeafEntry.Builder)ItemEntry.builder(Blocks.SAND).weight(10)).apply(SetCountLootFunction.builder(UniformLootNumberProvider.create(1.0f, 8.0f)))))).pool(LootPool.builder().rolls(ConstantLootNumberProvider.create(1.0f)).with((LootPoolEntry.Builder<?>)EmptyEntry.builder().weight(4)).with((LootPoolEntry.Builder<?>)((Object)((LeafEntry.Builder)ItemEntry.builder(Items.DUNE_ARMOR_TRIM_SMITHING_TEMPLATE).weight(1)).apply(SetCountLootFunction.builder(ConstantLootNumberProvider.create(2.0f))))).with((LootPoolEntry.Builder<?>)((Object)((LeafEntry.Builder)ItemEntry.builder(Items.BOOK).weight(2)).apply(new EnchantRandomlyLootFunction.Builder().option(lv.getOrThrow(Enchantments.UNBREAKING))))));
    }

    public LootTable.Builder createAncientCityChestTableBuilder() {
        RegistryWrapper.Impl<Enchantment> lv = this.registries.getWrapperOrThrow(RegistryKeys.ENCHANTMENT);
        return LootTable.builder().pool(LootPool.builder().rolls(UniformLootNumberProvider.create(5.0f, 10.0f)).with((LootPoolEntry.Builder<?>)((Object)((LeafEntry.Builder)ItemEntry.builder(Items.ENCHANTED_GOLDEN_APPLE).weight(1)).apply(SetCountLootFunction.builder(UniformLootNumberProvider.create(1.0f, 2.0f))))).with((LootPoolEntry.Builder<?>)ItemEntry.builder(Items.MUSIC_DISC_OTHERSIDE).weight(1)).with((LootPoolEntry.Builder<?>)((Object)((LeafEntry.Builder)ItemEntry.builder(Items.COMPASS).weight(2)).apply(SetCountLootFunction.builder(ConstantLootNumberProvider.create(1.0f))))).with((LootPoolEntry.Builder<?>)((Object)((LeafEntry.Builder)ItemEntry.builder(Items.SCULK_CATALYST).weight(2)).apply(SetCountLootFunction.builder(UniformLootNumberProvider.create(1.0f, 2.0f))))).with((LootPoolEntry.Builder<?>)ItemEntry.builder(Items.NAME_TAG).weight(2)).with((LootPoolEntry.Builder<?>)((Object)((LeafEntry.Builder)((LeafEntry.Builder)((LeafEntry.Builder)ItemEntry.builder(Items.DIAMOND_HOE).weight(2)).apply(SetCountLootFunction.builder(ConstantLootNumberProvider.create(1.0f)))).apply(SetDamageLootFunction.builder(UniformLootNumberProvider.create(0.8f, 1.0f)))).apply(EnchantWithLevelsLootFunction.builder(this.registries, UniformLootNumberProvider.create(30.0f, 50.0f))))).with((LootPoolEntry.Builder<?>)((Object)((LeafEntry.Builder)ItemEntry.builder(Items.LEAD).weight(2)).apply(SetCountLootFunction.builder(ConstantLootNumberProvider.create(1.0f))))).with((LootPoolEntry.Builder<?>)((Object)((LeafEntry.Builder)ItemEntry.builder(Items.DIAMOND_HORSE_ARMOR).weight(2)).apply(SetCountLootFunction.builder(ConstantLootNumberProvider.create(1.0f))))).with((LootPoolEntry.Builder<?>)((Object)((LeafEntry.Builder)ItemEntry.builder(Items.SADDLE).weight(2)).apply(SetCountLootFunction.builder(ConstantLootNumberProvider.create(1.0f))))).with((LootPoolEntry.Builder<?>)ItemEntry.builder(Items.MUSIC_DISC_13).weight(2)).with((LootPoolEntry.Builder<?>)ItemEntry.builder(Items.MUSIC_DISC_CAT).weight(2)).with((LootPoolEntry.Builder<?>)((Object)((LeafEntry.Builder)ItemEntry.builder(Items.DIAMOND_LEGGINGS).weight(2)).apply(EnchantWithLevelsLootFunction.builder(this.registries, UniformLootNumberProvider.create(30.0f, 50.0f))))).with((LootPoolEntry.Builder<?>)((Object)((LeafEntry.Builder)ItemEntry.builder(Items.BOOK).weight(3)).apply(new EnchantRandomlyLootFunction.Builder().option(lv.getOrThrow(Enchantments.SWIFT_SNEAK))))).with((LootPoolEntry.Builder<?>)((Object)((LeafEntry.Builder)ItemEntry.builder(Items.SCULK).weight(3)).apply(SetCountLootFunction.builder(UniformLootNumberProvider.create(4.0f, 10.0f))))).with((LootPoolEntry.Builder<?>)((Object)((LeafEntry.Builder)ItemEntry.builder(Items.SCULK_SENSOR).weight(3)).apply(SetCountLootFunction.builder(UniformLootNumberProvider.create(1.0f, 3.0f))))).with((LootPoolEntry.Builder<?>)((Object)((LeafEntry.Builder)ItemEntry.builder(Items.CANDLE).weight(3)).apply(SetCountLootFunction.builder(UniformLootNumberProvider.create(1.0f, 4.0f))))).with((LootPoolEntry.Builder<?>)((Object)((LeafEntry.Builder)ItemEntry.builder(Items.AMETHYST_SHARD).weight(3)).apply(SetCountLootFunction.builder(UniformLootNumberProvider.create(1.0f, 15.0f))))).with((LootPoolEntry.Builder<?>)((Object)((LeafEntry.Builder)ItemEntry.builder(Items.EXPERIENCE_BOTTLE).weight(3)).apply(SetCountLootFunction.builder(UniformLootNumberProvider.create(1.0f, 3.0f))))).with((LootPoolEntry.Builder<?>)((Object)((LeafEntry.Builder)ItemEntry.builder(Items.GLOW_BERRIES).weight(3)).apply(SetCountLootFunction.builder(UniformLootNumberProvider.create(1.0f, 15.0f))))).with((LootPoolEntry.Builder<?>)((Object)((LeafEntry.Builder)ItemEntry.builder(Items.IRON_LEGGINGS).weight(3)).apply(EnchantWithLevelsLootFunction.builder(this.registries, UniformLootNumberProvider.create(20.0f, 39.0f))))).with((LootPoolEntry.Builder<?>)((Object)((LeafEntry.Builder)ItemEntry.builder(Items.ECHO_SHARD).weight(4)).apply(SetCountLootFunction.builder(UniformLootNumberProvider.create(1.0f, 3.0f))))).with((LootPoolEntry.Builder<?>)((Object)((LeafEntry.Builder)ItemEntry.builder(Items.DISC_FRAGMENT_5).weight(4)).apply(SetCountLootFunction.builder(UniformLootNumberProvider.create(1.0f, 3.0f))))).with((LootPoolEntry.Builder<?>)((Object)((LeafEntry.Builder)((LeafEntry.Builder)ItemEntry.builder(Items.POTION).weight(5)).apply(SetCountLootFunction.builder(UniformLootNumberProvider.create(1.0f, 3.0f)))).apply(SetPotionLootFunction.builder(Potions.STRONG_REGENERATION)))).with((LootPoolEntry.Builder<?>)((Object)((LeafEntry.Builder)ItemEntry.builder(Items.BOOK).weight(5)).apply(EnchantRandomlyLootFunction.builder(this.registries)))).with((LootPoolEntry.Builder<?>)((Object)((LeafEntry.Builder)ItemEntry.builder(Items.BOOK).weight(5)).apply(SetCountLootFunction.builder(UniformLootNumberProvider.create(3.0f, 10.0f))))).with((LootPoolEntry.Builder<?>)((Object)((LeafEntry.Builder)ItemEntry.builder(Items.BONE).weight(5)).apply(SetCountLootFunction.builder(UniformLootNumberProvider.create(1.0f, 15.0f))))).with((LootPoolEntry.Builder<?>)((Object)((LeafEntry.Builder)ItemEntry.builder(Items.SOUL_TORCH).weight(5)).apply(SetCountLootFunction.builder(UniformLootNumberProvider.create(1.0f, 15.0f))))).with((LootPoolEntry.Builder<?>)((Object)((LeafEntry.Builder)ItemEntry.builder(Items.COAL).weight(7)).apply(SetCountLootFunction.builder(UniformLootNumberProvider.create(6.0f, 15.0f)))))).pool(LootPool.builder().rolls(ConstantLootNumberProvider.create(1.0f)).with((LootPoolEntry.Builder<?>)EmptyEntry.builder().weight(71)).with((LootPoolEntry.Builder<?>)((Object)((LeafEntry.Builder)ItemEntry.builder(Items.BOOK).weight(4)).apply(new EnchantRandomlyLootFunction.Builder().option(lv.getOrThrow(Enchantments.MENDING))))).with((LootPoolEntry.Builder<?>)ItemEntry.builder(Items.WARD_ARMOR_TRIM_SMITHING_TEMPLATE).weight(4)).with((LootPoolEntry.Builder<?>)ItemEntry.builder(Items.SILENCE_ARMOR_TRIM_SMITHING_TEMPLATE).weight(1)));
    }

    public LootTable.Builder createJungleTempleChestTableBuilder() {
        RegistryWrapper.Impl<Enchantment> lv = this.registries.getWrapperOrThrow(RegistryKeys.ENCHANTMENT);
        return LootTable.builder().pool(LootPool.builder().rolls(UniformLootNumberProvider.create(2.0f, 6.0f)).with((LootPoolEntry.Builder<?>)((Object)((LeafEntry.Builder)ItemEntry.builder(Items.DIAMOND).weight(3)).apply(SetCountLootFunction.builder(UniformLootNumberProvider.create(1.0f, 3.0f))))).with((LootPoolEntry.Builder<?>)((Object)((LeafEntry.Builder)ItemEntry.builder(Items.IRON_INGOT).weight(10)).apply(SetCountLootFunction.builder(UniformLootNumberProvider.create(1.0f, 5.0f))))).with((LootPoolEntry.Builder<?>)((Object)((LeafEntry.Builder)ItemEntry.builder(Items.GOLD_INGOT).weight(15)).apply(SetCountLootFunction.builder(UniformLootNumberProvider.create(2.0f, 7.0f))))).with((LootPoolEntry.Builder<?>)((Object)((LeafEntry.Builder)ItemEntry.builder(Blocks.BAMBOO).weight(15)).apply(SetCountLootFunction.builder(UniformLootNumberProvider.create(1.0f, 3.0f))))).with((LootPoolEntry.Builder<?>)((Object)((LeafEntry.Builder)ItemEntry.builder(Items.EMERALD).weight(2)).apply(SetCountLootFunction.builder(UniformLootNumberProvider.create(1.0f, 3.0f))))).with((LootPoolEntry.Builder<?>)((Object)((LeafEntry.Builder)ItemEntry.builder(Items.BONE).weight(20)).apply(SetCountLootFunction.builder(UniformLootNumberProvider.create(4.0f, 6.0f))))).with((LootPoolEntry.Builder<?>)((Object)((LeafEntry.Builder)ItemEntry.builder(Items.ROTTEN_FLESH).weight(16)).apply(SetCountLootFunction.builder(UniformLootNumberProvider.create(3.0f, 7.0f))))).with((LootPoolEntry.Builder<?>)ItemEntry.builder(Items.SADDLE).weight(3)).with(ItemEntry.builder(Items.IRON_HORSE_ARMOR)).with(ItemEntry.builder(Items.GOLDEN_HORSE_ARMOR)).with(ItemEntry.builder(Items.DIAMOND_HORSE_ARMOR)).with((LootPoolEntry.Builder<?>)((Object)ItemEntry.builder(Items.BOOK).apply(EnchantWithLevelsLootFunction.builder(this.registries, ConstantLootNumberProvider.create(30.0f)))))).pool(LootPool.builder().rolls(ConstantLootNumberProvider.create(1.0f)).with((LootPoolEntry.Builder<?>)EmptyEntry.builder().weight(2)).with((LootPoolEntry.Builder<?>)((Object)((LeafEntry.Builder)ItemEntry.builder(Items.WILD_ARMOR_TRIM_SMITHING_TEMPLATE).weight(1)).apply(SetCountLootFunction.builder(ConstantLootNumberProvider.create(2.0f)))))).pool(LootPool.builder().rolls(ConstantLootNumberProvider.create(1.0f)).with((LootPoolEntry.Builder<?>)EmptyEntry.builder().weight(1)).with((LootPoolEntry.Builder<?>)((Object)ItemEntry.builder(Items.BOOK).apply(new EnchantRandomlyLootFunction.Builder().option(lv.getOrThrow(Enchantments.UNBREAKING))))));
    }
}
