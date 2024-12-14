/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.data.server.loottable;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.stream.IntStream;
import net.minecraft.block.BeehiveBlock;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.block.CandleBlock;
import net.minecraft.block.CaveVines;
import net.minecraft.block.DoorBlock;
import net.minecraft.block.FlowerPotBlock;
import net.minecraft.block.FlowerbedBlock;
import net.minecraft.block.MultifaceGrowthBlock;
import net.minecraft.block.SlabBlock;
import net.minecraft.block.StemBlock;
import net.minecraft.block.TallPlantBlock;
import net.minecraft.block.enums.DoubleBlockHalf;
import net.minecraft.block.enums.SlabType;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.data.server.loottable.LootTableGenerator;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.item.Item;
import net.minecraft.item.ItemConvertible;
import net.minecraft.item.Items;
import net.minecraft.loot.LootPool;
import net.minecraft.loot.LootTable;
import net.minecraft.loot.LootTables;
import net.minecraft.loot.condition.BlockStatePropertyLootCondition;
import net.minecraft.loot.condition.LocationCheckLootCondition;
import net.minecraft.loot.condition.LootCondition;
import net.minecraft.loot.condition.LootConditionConsumingBuilder;
import net.minecraft.loot.condition.MatchToolLootCondition;
import net.minecraft.loot.condition.RandomChanceLootCondition;
import net.minecraft.loot.condition.SurvivesExplosionLootCondition;
import net.minecraft.loot.condition.TableBonusLootCondition;
import net.minecraft.loot.entry.AlternativeEntry;
import net.minecraft.loot.entry.ItemEntry;
import net.minecraft.loot.entry.LeafEntry;
import net.minecraft.loot.entry.LootPoolEntry;
import net.minecraft.loot.function.ApplyBonusLootFunction;
import net.minecraft.loot.function.CopyComponentsLootFunction;
import net.minecraft.loot.function.CopyStateLootFunction;
import net.minecraft.loot.function.ExplosionDecayLootFunction;
import net.minecraft.loot.function.LimitCountLootFunction;
import net.minecraft.loot.function.LootFunction;
import net.minecraft.loot.function.LootFunctionConsumingBuilder;
import net.minecraft.loot.function.SetCountLootFunction;
import net.minecraft.loot.operator.BoundedIntUnaryOperator;
import net.minecraft.loot.provider.number.BinomialLootNumberProvider;
import net.minecraft.loot.provider.number.ConstantLootNumberProvider;
import net.minecraft.loot.provider.number.LootNumberProvider;
import net.minecraft.loot.provider.number.UniformLootNumberProvider;
import net.minecraft.predicate.BlockPredicate;
import net.minecraft.predicate.NumberRange;
import net.minecraft.predicate.StatePredicate;
import net.minecraft.predicate.entity.LocationPredicate;
import net.minecraft.predicate.item.EnchantmentPredicate;
import net.minecraft.predicate.item.EnchantmentsPredicate;
import net.minecraft.predicate.item.ItemPredicate;
import net.minecraft.predicate.item.ItemSubPredicateTypes;
import net.minecraft.registry.Registries;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.resource.featuretoggle.FeatureSet;
import net.minecraft.state.property.Property;
import net.minecraft.util.StringIdentifiable;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;

public abstract class BlockLootTableGenerator
implements LootTableGenerator {
    protected static final LootCondition.Builder WITH_SHEARS = MatchToolLootCondition.builder(ItemPredicate.Builder.create().items(Items.SHEARS));
    protected final RegistryWrapper.WrapperLookup registryLookup;
    protected final Set<Item> explosionImmuneItems;
    protected final FeatureSet requiredFeatures;
    protected final Map<RegistryKey<LootTable>, LootTable.Builder> lootTables;
    protected static final float[] SAPLING_DROP_CHANCE = new float[]{0.05f, 0.0625f, 0.083333336f, 0.1f};
    private static final float[] LEAVES_STICK_DROP_CHANCE = new float[]{0.02f, 0.022222223f, 0.025f, 0.033333335f, 0.1f};

    protected LootCondition.Builder createSilkTouchCondition() {
        RegistryWrapper.Impl<Enchantment> lv = this.registryLookup.getWrapperOrThrow(RegistryKeys.ENCHANTMENT);
        return MatchToolLootCondition.builder(ItemPredicate.Builder.create().subPredicate(ItemSubPredicateTypes.ENCHANTMENTS, EnchantmentsPredicate.enchantments(List.of(new EnchantmentPredicate(lv.getOrThrow(Enchantments.SILK_TOUCH), NumberRange.IntRange.atLeast(1))))));
    }

    protected LootCondition.Builder createWithoutSilkTouchCondition() {
        return this.createSilkTouchCondition().invert();
    }

    private LootCondition.Builder createWithShearsOrSilkTouchCondition() {
        return WITH_SHEARS.or(this.createSilkTouchCondition());
    }

    private LootCondition.Builder createWithoutShearsOrSilkTouchCondition() {
        return this.createWithShearsOrSilkTouchCondition().invert();
    }

    protected BlockLootTableGenerator(Set<Item> explosionImmuneItems, FeatureSet requiredFeatures, RegistryWrapper.WrapperLookup registryLookup) {
        this(explosionImmuneItems, requiredFeatures, new HashMap<RegistryKey<LootTable>, LootTable.Builder>(), registryLookup);
    }

    protected BlockLootTableGenerator(Set<Item> explosionImmuneItems, FeatureSet requiredFeatures, Map<RegistryKey<LootTable>, LootTable.Builder> lootTables, RegistryWrapper.WrapperLookup registryLookup) {
        this.explosionImmuneItems = explosionImmuneItems;
        this.requiredFeatures = requiredFeatures;
        this.lootTables = lootTables;
        this.registryLookup = registryLookup;
    }

    protected <T extends LootFunctionConsumingBuilder<T>> T applyExplosionDecay(ItemConvertible drop, LootFunctionConsumingBuilder<T> builder) {
        if (!this.explosionImmuneItems.contains(drop.asItem())) {
            return builder.apply(ExplosionDecayLootFunction.builder());
        }
        return builder.getThisFunctionConsumingBuilder();
    }

    protected <T extends LootConditionConsumingBuilder<T>> T addSurvivesExplosionCondition(ItemConvertible drop, LootConditionConsumingBuilder<T> builder) {
        if (!this.explosionImmuneItems.contains(drop.asItem())) {
            return builder.conditionally(SurvivesExplosionLootCondition.builder());
        }
        return builder.getThisConditionConsumingBuilder();
    }

    public LootTable.Builder drops(ItemConvertible drop) {
        return LootTable.builder().pool(this.addSurvivesExplosionCondition(drop, LootPool.builder().rolls(ConstantLootNumberProvider.create(1.0f)).with(ItemEntry.builder(drop))));
    }

    private static LootTable.Builder drops(Block drop, LootCondition.Builder conditionBuilder, LootPoolEntry.Builder<?> child) {
        return LootTable.builder().pool(LootPool.builder().rolls(ConstantLootNumberProvider.create(1.0f)).with(((LeafEntry.Builder)ItemEntry.builder(drop).conditionally(conditionBuilder)).alternatively(child)));
    }

    protected LootTable.Builder dropsWithSilkTouch(Block block, LootPoolEntry.Builder<?> loot) {
        return BlockLootTableGenerator.drops(block, this.createSilkTouchCondition(), loot);
    }

    protected LootTable.Builder dropsWithShears(Block block, LootPoolEntry.Builder<?> loot) {
        return BlockLootTableGenerator.drops(block, WITH_SHEARS, loot);
    }

    protected LootTable.Builder dropsWithSilkTouchOrShears(Block block, LootPoolEntry.Builder<?> loot) {
        return BlockLootTableGenerator.drops(block, this.createWithShearsOrSilkTouchCondition(), loot);
    }

    protected LootTable.Builder drops(Block withSilkTouch, ItemConvertible withoutSilkTouch) {
        return this.dropsWithSilkTouch(withSilkTouch, (LootPoolEntry.Builder)this.addSurvivesExplosionCondition(withSilkTouch, ItemEntry.builder(withoutSilkTouch)));
    }

    protected LootTable.Builder drops(ItemConvertible drop, LootNumberProvider count) {
        return LootTable.builder().pool(LootPool.builder().rolls(ConstantLootNumberProvider.create(1.0f)).with((LootPoolEntry.Builder)this.applyExplosionDecay(drop, ItemEntry.builder(drop).apply(SetCountLootFunction.builder(count)))));
    }

    protected LootTable.Builder drops(Block block, ItemConvertible drop, LootNumberProvider count) {
        return this.dropsWithSilkTouch(block, (LootPoolEntry.Builder)this.applyExplosionDecay(block, ItemEntry.builder(drop).apply(SetCountLootFunction.builder(count))));
    }

    private LootTable.Builder dropsWithSilkTouch(ItemConvertible drop) {
        return LootTable.builder().pool(LootPool.builder().conditionally(this.createSilkTouchCondition()).rolls(ConstantLootNumberProvider.create(1.0f)).with(ItemEntry.builder(drop)));
    }

    private LootTable.Builder pottedPlantDrops(ItemConvertible drop) {
        return LootTable.builder().pool(this.addSurvivesExplosionCondition(Blocks.FLOWER_POT, LootPool.builder().rolls(ConstantLootNumberProvider.create(1.0f)).with(ItemEntry.builder(Blocks.FLOWER_POT)))).pool(this.addSurvivesExplosionCondition(drop, LootPool.builder().rolls(ConstantLootNumberProvider.create(1.0f)).with(ItemEntry.builder(drop))));
    }

    protected LootTable.Builder slabDrops(Block drop) {
        return LootTable.builder().pool(LootPool.builder().rolls(ConstantLootNumberProvider.create(1.0f)).with((LootPoolEntry.Builder)this.applyExplosionDecay(drop, ItemEntry.builder(drop).apply((LootFunction.Builder)((Object)SetCountLootFunction.builder(ConstantLootNumberProvider.create(2.0f)).conditionally(BlockStatePropertyLootCondition.builder(drop).properties(StatePredicate.Builder.create().exactMatch(SlabBlock.TYPE, SlabType.DOUBLE))))))));
    }

    protected <T extends Comparable<T> & StringIdentifiable> LootTable.Builder dropsWithProperty(Block drop, Property<T> property, T value) {
        return LootTable.builder().pool(this.addSurvivesExplosionCondition(drop, LootPool.builder().rolls(ConstantLootNumberProvider.create(1.0f)).with((LootPoolEntry.Builder<?>)ItemEntry.builder(drop).conditionally(BlockStatePropertyLootCondition.builder(drop).properties(StatePredicate.Builder.create().exactMatch(property, value))))));
    }

    protected LootTable.Builder nameableContainerDrops(Block drop) {
        return LootTable.builder().pool(this.addSurvivesExplosionCondition(drop, LootPool.builder().rolls(ConstantLootNumberProvider.create(1.0f)).with((LootPoolEntry.Builder<?>)((Object)ItemEntry.builder(drop).apply(CopyComponentsLootFunction.builder(CopyComponentsLootFunction.Source.BLOCK_ENTITY).include(DataComponentTypes.CUSTOM_NAME))))));
    }

    protected LootTable.Builder shulkerBoxDrops(Block drop) {
        return LootTable.builder().pool(this.addSurvivesExplosionCondition(drop, LootPool.builder().rolls(ConstantLootNumberProvider.create(1.0f)).with((LootPoolEntry.Builder<?>)((Object)ItemEntry.builder(drop).apply(CopyComponentsLootFunction.builder(CopyComponentsLootFunction.Source.BLOCK_ENTITY).include(DataComponentTypes.CUSTOM_NAME).include(DataComponentTypes.CONTAINER).include(DataComponentTypes.LOCK).include(DataComponentTypes.CONTAINER_LOOT))))));
    }

    protected LootTable.Builder copperOreDrops(Block drop) {
        RegistryWrapper.Impl<Enchantment> lv = this.registryLookup.getWrapperOrThrow(RegistryKeys.ENCHANTMENT);
        return this.dropsWithSilkTouch(drop, (LootPoolEntry.Builder)this.applyExplosionDecay(drop, ((LeafEntry.Builder)ItemEntry.builder(Items.RAW_COPPER).apply(SetCountLootFunction.builder(UniformLootNumberProvider.create(2.0f, 5.0f)))).apply(ApplyBonusLootFunction.oreDrops(lv.getOrThrow(Enchantments.FORTUNE)))));
    }

    protected LootTable.Builder lapisOreDrops(Block drop) {
        RegistryWrapper.Impl<Enchantment> lv = this.registryLookup.getWrapperOrThrow(RegistryKeys.ENCHANTMENT);
        return this.dropsWithSilkTouch(drop, (LootPoolEntry.Builder)this.applyExplosionDecay(drop, ((LeafEntry.Builder)ItemEntry.builder(Items.LAPIS_LAZULI).apply(SetCountLootFunction.builder(UniformLootNumberProvider.create(4.0f, 9.0f)))).apply(ApplyBonusLootFunction.oreDrops(lv.getOrThrow(Enchantments.FORTUNE)))));
    }

    protected LootTable.Builder redstoneOreDrops(Block drop) {
        RegistryWrapper.Impl<Enchantment> lv = this.registryLookup.getWrapperOrThrow(RegistryKeys.ENCHANTMENT);
        return this.dropsWithSilkTouch(drop, (LootPoolEntry.Builder)this.applyExplosionDecay(drop, ((LeafEntry.Builder)ItemEntry.builder(Items.REDSTONE).apply(SetCountLootFunction.builder(UniformLootNumberProvider.create(4.0f, 5.0f)))).apply(ApplyBonusLootFunction.uniformBonusCount(lv.getOrThrow(Enchantments.FORTUNE)))));
    }

    protected LootTable.Builder bannerDrops(Block drop) {
        return LootTable.builder().pool(this.addSurvivesExplosionCondition(drop, LootPool.builder().rolls(ConstantLootNumberProvider.create(1.0f)).with((LootPoolEntry.Builder<?>)((Object)ItemEntry.builder(drop).apply(CopyComponentsLootFunction.builder(CopyComponentsLootFunction.Source.BLOCK_ENTITY).include(DataComponentTypes.CUSTOM_NAME).include(DataComponentTypes.ITEM_NAME).include(DataComponentTypes.HIDE_ADDITIONAL_TOOLTIP).include(DataComponentTypes.BANNER_PATTERNS))))));
    }

    protected LootTable.Builder beeNestDrops(Block drop) {
        return LootTable.builder().pool(LootPool.builder().conditionally(this.createSilkTouchCondition()).rolls(ConstantLootNumberProvider.create(1.0f)).with((LootPoolEntry.Builder<?>)((Object)((LeafEntry.Builder)ItemEntry.builder(drop).apply(CopyComponentsLootFunction.builder(CopyComponentsLootFunction.Source.BLOCK_ENTITY).include(DataComponentTypes.BEES))).apply(CopyStateLootFunction.builder(drop).addProperty(BeehiveBlock.HONEY_LEVEL)))));
    }

    protected LootTable.Builder beehiveDrops(Block drop) {
        return LootTable.builder().pool(LootPool.builder().rolls(ConstantLootNumberProvider.create(1.0f)).with(((LootPoolEntry.Builder)((Object)((LeafEntry.Builder)((LeafEntry.Builder)ItemEntry.builder(drop).conditionally(this.createSilkTouchCondition())).apply(CopyComponentsLootFunction.builder(CopyComponentsLootFunction.Source.BLOCK_ENTITY).include(DataComponentTypes.BEES))).apply(CopyStateLootFunction.builder(drop).addProperty(BeehiveBlock.HONEY_LEVEL)))).alternatively(ItemEntry.builder(drop))));
    }

    protected LootTable.Builder glowBerryDrops(Block drop) {
        return LootTable.builder().pool(LootPool.builder().with(ItemEntry.builder(Items.GLOW_BERRIES)).conditionally(BlockStatePropertyLootCondition.builder(drop).properties(StatePredicate.Builder.create().exactMatch(CaveVines.BERRIES, true))));
    }

    protected LootTable.Builder oreDrops(Block withSilkTouch, Item withoutSilkTouch) {
        RegistryWrapper.Impl<Enchantment> lv = this.registryLookup.getWrapperOrThrow(RegistryKeys.ENCHANTMENT);
        return this.dropsWithSilkTouch(withSilkTouch, (LootPoolEntry.Builder)this.applyExplosionDecay(withSilkTouch, ItemEntry.builder(withoutSilkTouch).apply(ApplyBonusLootFunction.oreDrops(lv.getOrThrow(Enchantments.FORTUNE)))));
    }

    protected LootTable.Builder mushroomBlockDrops(Block withSilkTouch, ItemConvertible withoutSilkTouch) {
        return this.dropsWithSilkTouch(withSilkTouch, (LootPoolEntry.Builder)this.applyExplosionDecay(withSilkTouch, ((LeafEntry.Builder)ItemEntry.builder(withoutSilkTouch).apply(SetCountLootFunction.builder(UniformLootNumberProvider.create(-6.0f, 2.0f)))).apply(LimitCountLootFunction.builder(BoundedIntUnaryOperator.createMin(0)))));
    }

    protected LootTable.Builder shortPlantDrops(Block withShears) {
        RegistryWrapper.Impl<Enchantment> lv = this.registryLookup.getWrapperOrThrow(RegistryKeys.ENCHANTMENT);
        return this.dropsWithShears(withShears, (LootPoolEntry.Builder)this.applyExplosionDecay(withShears, ((LeafEntry.Builder)ItemEntry.builder(Items.WHEAT_SEEDS).conditionally(RandomChanceLootCondition.builder(0.125f))).apply(ApplyBonusLootFunction.uniformBonusCount(lv.getOrThrow(Enchantments.FORTUNE), 2))));
    }

    public LootTable.Builder cropStemDrops(Block stem, Item drop) {
        return LootTable.builder().pool(this.applyExplosionDecay(stem, LootPool.builder().rolls(ConstantLootNumberProvider.create(1.0f)).with((LootPoolEntry.Builder)ItemEntry.builder(drop).apply(StemBlock.AGE.getValues(), age -> SetCountLootFunction.builder(BinomialLootNumberProvider.create(3, (float)(age + 1) / 15.0f)).conditionally(BlockStatePropertyLootCondition.builder(stem).properties(StatePredicate.Builder.create().exactMatch(StemBlock.AGE, age.intValue())))))));
    }

    public LootTable.Builder attachedCropStemDrops(Block stem, Item drop) {
        return LootTable.builder().pool(this.applyExplosionDecay(stem, LootPool.builder().rolls(ConstantLootNumberProvider.create(1.0f)).with((LootPoolEntry.Builder<?>)((Object)ItemEntry.builder(drop).apply(SetCountLootFunction.builder(BinomialLootNumberProvider.create(3, 0.53333336f)))))));
    }

    protected static LootTable.Builder dropsWithShears(ItemConvertible drop) {
        return LootTable.builder().pool(LootPool.builder().rolls(ConstantLootNumberProvider.create(1.0f)).conditionally(WITH_SHEARS).with(ItemEntry.builder(drop)));
    }

    protected LootTable.Builder multifaceGrowthDrops(Block drop, LootCondition.Builder condition) {
        return LootTable.builder().pool(LootPool.builder().with((LootPoolEntry.Builder)this.applyExplosionDecay(drop, ((LeafEntry.Builder)((LeafEntry.Builder)ItemEntry.builder(drop).conditionally(condition)).apply(Direction.values(), direction -> SetCountLootFunction.builder(ConstantLootNumberProvider.create(1.0f), true).conditionally(BlockStatePropertyLootCondition.builder(drop).properties(StatePredicate.Builder.create().exactMatch(MultifaceGrowthBlock.getProperty(direction), true))))).apply(SetCountLootFunction.builder(ConstantLootNumberProvider.create(-1.0f), true)))));
    }

    protected LootTable.Builder leavesDrops(Block leaves, Block sapling, float ... saplingChance) {
        RegistryWrapper.Impl<Enchantment> lv = this.registryLookup.getWrapperOrThrow(RegistryKeys.ENCHANTMENT);
        return this.dropsWithSilkTouchOrShears(leaves, (LootPoolEntry.Builder<?>)((LeafEntry.Builder)this.addSurvivesExplosionCondition(leaves, ItemEntry.builder(sapling))).conditionally(TableBonusLootCondition.builder(lv.getOrThrow(Enchantments.FORTUNE), saplingChance))).pool(LootPool.builder().rolls(ConstantLootNumberProvider.create(1.0f)).conditionally(this.createWithoutShearsOrSilkTouchCondition()).with((LootPoolEntry.Builder<?>)((LeafEntry.Builder)this.applyExplosionDecay(leaves, ItemEntry.builder(Items.STICK).apply(SetCountLootFunction.builder(UniformLootNumberProvider.create(1.0f, 2.0f))))).conditionally(TableBonusLootCondition.builder(lv.getOrThrow(Enchantments.FORTUNE), LEAVES_STICK_DROP_CHANCE))));
    }

    protected LootTable.Builder oakLeavesDrops(Block leaves, Block sapling, float ... saplingChance) {
        RegistryWrapper.Impl<Enchantment> lv = this.registryLookup.getWrapperOrThrow(RegistryKeys.ENCHANTMENT);
        return this.leavesDrops(leaves, sapling, saplingChance).pool(LootPool.builder().rolls(ConstantLootNumberProvider.create(1.0f)).conditionally(this.createWithoutShearsOrSilkTouchCondition()).with((LootPoolEntry.Builder<?>)((LeafEntry.Builder)this.addSurvivesExplosionCondition(leaves, ItemEntry.builder(Items.APPLE))).conditionally(TableBonusLootCondition.builder(lv.getOrThrow(Enchantments.FORTUNE), 0.005f, 0.0055555557f, 0.00625f, 0.008333334f, 0.025f))));
    }

    protected LootTable.Builder mangroveLeavesDrops(Block leaves) {
        RegistryWrapper.Impl<Enchantment> lv = this.registryLookup.getWrapperOrThrow(RegistryKeys.ENCHANTMENT);
        return this.dropsWithSilkTouchOrShears(leaves, (LootPoolEntry.Builder<?>)((LeafEntry.Builder)this.applyExplosionDecay(Blocks.MANGROVE_LEAVES, ItemEntry.builder(Items.STICK).apply(SetCountLootFunction.builder(UniformLootNumberProvider.create(1.0f, 2.0f))))).conditionally(TableBonusLootCondition.builder(lv.getOrThrow(Enchantments.FORTUNE), LEAVES_STICK_DROP_CHANCE)));
    }

    protected LootTable.Builder cropDrops(Block crop, Item product, Item seeds, LootCondition.Builder condition) {
        RegistryWrapper.Impl<Enchantment> lv = this.registryLookup.getWrapperOrThrow(RegistryKeys.ENCHANTMENT);
        return this.applyExplosionDecay(crop, LootTable.builder().pool(LootPool.builder().with(((LeafEntry.Builder)ItemEntry.builder(product).conditionally(condition)).alternatively(ItemEntry.builder(seeds)))).pool(LootPool.builder().conditionally(condition).with((LootPoolEntry.Builder<?>)((Object)ItemEntry.builder(seeds).apply(ApplyBonusLootFunction.binomialWithBonusCount(lv.getOrThrow(Enchantments.FORTUNE), 0.5714286f, 3))))));
    }

    protected LootTable.Builder seagrassDrops(Block seagrass) {
        return LootTable.builder().pool(LootPool.builder().conditionally(WITH_SHEARS).with((LootPoolEntry.Builder<?>)((Object)ItemEntry.builder(seagrass).apply(SetCountLootFunction.builder(ConstantLootNumberProvider.create(2.0f))))));
    }

    protected LootTable.Builder tallPlantDrops(Block tallPlant, Block shortPlant) {
        AlternativeEntry.Builder lv = ((LeafEntry.Builder)((LootPoolEntry.Builder)((Object)ItemEntry.builder(shortPlant).apply(SetCountLootFunction.builder(ConstantLootNumberProvider.create(2.0f))))).conditionally(WITH_SHEARS)).alternatively((LootPoolEntry.Builder<?>)((LeafEntry.Builder)this.addSurvivesExplosionCondition(tallPlant, ItemEntry.builder(Items.WHEAT_SEEDS))).conditionally(RandomChanceLootCondition.builder(0.125f)));
        return LootTable.builder().pool(LootPool.builder().with(lv).conditionally(BlockStatePropertyLootCondition.builder(tallPlant).properties(StatePredicate.Builder.create().exactMatch(TallPlantBlock.HALF, DoubleBlockHalf.LOWER))).conditionally(LocationCheckLootCondition.builder(LocationPredicate.Builder.create().block(BlockPredicate.Builder.create().blocks(tallPlant).state(StatePredicate.Builder.create().exactMatch(TallPlantBlock.HALF, DoubleBlockHalf.UPPER))), new BlockPos(0, 1, 0)))).pool(LootPool.builder().with(lv).conditionally(BlockStatePropertyLootCondition.builder(tallPlant).properties(StatePredicate.Builder.create().exactMatch(TallPlantBlock.HALF, DoubleBlockHalf.UPPER))).conditionally(LocationCheckLootCondition.builder(LocationPredicate.Builder.create().block(BlockPredicate.Builder.create().blocks(tallPlant).state(StatePredicate.Builder.create().exactMatch(TallPlantBlock.HALF, DoubleBlockHalf.LOWER))), new BlockPos(0, -1, 0))));
    }

    protected LootTable.Builder candleDrops(Block candle) {
        return LootTable.builder().pool(LootPool.builder().rolls(ConstantLootNumberProvider.create(1.0f)).with((LootPoolEntry.Builder)this.applyExplosionDecay(candle, (LootFunctionConsumingBuilder)ItemEntry.builder(candle).apply(List.of(Integer.valueOf(2), Integer.valueOf(3), Integer.valueOf(4)), candles -> SetCountLootFunction.builder(ConstantLootNumberProvider.create(candles.intValue())).conditionally(BlockStatePropertyLootCondition.builder(candle).properties(StatePredicate.Builder.create().exactMatch(CandleBlock.CANDLES, candles.intValue())))))));
    }

    protected LootTable.Builder flowerbedDrops(Block flowerbed) {
        return LootTable.builder().pool(LootPool.builder().rolls(ConstantLootNumberProvider.create(1.0f)).with((LootPoolEntry.Builder)this.applyExplosionDecay(flowerbed, (LootFunctionConsumingBuilder)ItemEntry.builder(flowerbed).apply(IntStream.rangeClosed(1, 4).boxed().toList(), flowerAmount -> SetCountLootFunction.builder(ConstantLootNumberProvider.create(flowerAmount.intValue())).conditionally(BlockStatePropertyLootCondition.builder(flowerbed).properties(StatePredicate.Builder.create().exactMatch(FlowerbedBlock.FLOWER_AMOUNT, flowerAmount.intValue())))))));
    }

    protected static LootTable.Builder candleCakeDrops(Block candleCake) {
        return LootTable.builder().pool(LootPool.builder().rolls(ConstantLootNumberProvider.create(1.0f)).with(ItemEntry.builder(candleCake)));
    }

    public static LootTable.Builder dropsNothing() {
        return LootTable.builder();
    }

    protected abstract void generate();

    @Override
    public void accept(BiConsumer<RegistryKey<LootTable>, LootTable.Builder> lootTableBiConsumer) {
        this.generate();
        HashSet<RegistryKey<LootTable>> set = new HashSet<RegistryKey<LootTable>>();
        for (Block lv : Registries.BLOCK) {
            RegistryKey<LootTable> lv2;
            if (!lv.isEnabled(this.requiredFeatures) || (lv2 = lv.getLootTableKey()) == LootTables.EMPTY || !set.add(lv2)) continue;
            LootTable.Builder lv3 = this.lootTables.remove(lv2);
            if (lv3 == null) {
                throw new IllegalStateException(String.format(Locale.ROOT, "Missing loottable '%s' for '%s'", lv2.getValue(), Registries.BLOCK.getId(lv)));
            }
            lootTableBiConsumer.accept(lv2, lv3);
        }
        if (!this.lootTables.isEmpty()) {
            throw new IllegalStateException("Created block loot tables for non-blocks: " + String.valueOf(this.lootTables.keySet()));
        }
    }

    protected void addVinePlantDrop(Block vine, Block vinePlant) {
        RegistryWrapper.Impl<Enchantment> lv = this.registryLookup.getWrapperOrThrow(RegistryKeys.ENCHANTMENT);
        LootTable.Builder lv2 = this.dropsWithSilkTouchOrShears(vine, (LootPoolEntry.Builder<?>)ItemEntry.builder(vine).conditionally(TableBonusLootCondition.builder(lv.getOrThrow(Enchantments.FORTUNE), 0.33f, 0.55f, 0.77f, 1.0f)));
        this.addDrop(vine, lv2);
        this.addDrop(vinePlant, lv2);
    }

    protected LootTable.Builder doorDrops(Block block) {
        return this.dropsWithProperty(block, DoorBlock.HALF, DoubleBlockHalf.LOWER);
    }

    protected void addPottedPlantDrops(Block block) {
        this.addDrop(block, (Block flowerPot) -> this.pottedPlantDrops(((FlowerPotBlock)flowerPot).getContent()));
    }

    protected void addDropWithSilkTouch(Block block, Block drop) {
        this.addDrop(block, this.dropsWithSilkTouch(drop));
    }

    protected void addDrop(Block block, ItemConvertible drop) {
        this.addDrop(block, this.drops(drop));
    }

    protected void addDropWithSilkTouch(Block block) {
        this.addDropWithSilkTouch(block, block);
    }

    protected void addDrop(Block block) {
        this.addDrop(block, block);
    }

    protected void addDrop(Block block, Function<Block, LootTable.Builder> lootTableFunction) {
        this.addDrop(block, lootTableFunction.apply(block));
    }

    protected void addDrop(Block block, LootTable.Builder lootTable) {
        this.lootTables.put(block.getLootTableKey(), lootTable);
    }
}

