/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.loot;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.mojang.datafixers.kinds.Applicative;
import com.mojang.logging.LogUtils;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import it.unimi.dsi.fastutil.objects.ObjectIterator;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;
import net.minecraft.loot.LootPool;
import net.minecraft.loot.LootTableReporter;
import net.minecraft.loot.context.LootContext;
import net.minecraft.loot.context.LootContextParameterSet;
import net.minecraft.loot.context.LootContextType;
import net.minecraft.loot.context.LootContextTypes;
import net.minecraft.loot.function.LootFunction;
import net.minecraft.loot.function.LootFunctionConsumingBuilder;
import net.minecraft.loot.function.LootFunctionTypes;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.entry.RegistryElementCodec;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Identifier;
import net.minecraft.util.Util;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.random.Random;
import org.slf4j.Logger;

public class LootTable {
    private static final Logger LOGGER = LogUtils.getLogger();
    public static final LootTable EMPTY = new LootTable(LootContextTypes.EMPTY, Optional.empty(), List.of(), List.of());
    public static final LootContextType GENERIC = LootContextTypes.GENERIC;
    public static final long DEFAULT_SEED = 0L;
    public static final Codec<LootTable> CODEC = RecordCodecBuilder.create(instance -> instance.group(LootContextTypes.CODEC.lenientOptionalFieldOf("type", GENERIC).forGetter(table -> table.type), Identifier.CODEC.optionalFieldOf("random_sequence").forGetter(table -> table.randomSequenceId), LootPool.CODEC.listOf().optionalFieldOf("pools", List.of()).forGetter(table -> table.pools), LootFunctionTypes.CODEC.listOf().optionalFieldOf("functions", List.of()).forGetter(table -> table.functions)).apply((Applicative<LootTable, ?>)instance, LootTable::new));
    public static final Codec<RegistryEntry<LootTable>> ENTRY_CODEC = RegistryElementCodec.of(RegistryKeys.LOOT_TABLE, CODEC);
    private final LootContextType type;
    private final Optional<Identifier> randomSequenceId;
    private final List<LootPool> pools;
    private final List<LootFunction> functions;
    private final BiFunction<ItemStack, LootContext, ItemStack> combinedFunction;

    LootTable(LootContextType type, Optional<Identifier> randomSequenceId, List<LootPool> pools, List<LootFunction> functions) {
        this.type = type;
        this.randomSequenceId = randomSequenceId;
        this.pools = pools;
        this.functions = functions;
        this.combinedFunction = LootFunctionTypes.join(functions);
    }

    public static Consumer<ItemStack> processStacks(ServerWorld world, Consumer<ItemStack> consumer) {
        return stack -> {
            if (!stack.isItemEnabled(world.getEnabledFeatures())) {
                return;
            }
            if (stack.getCount() < stack.getMaxCount()) {
                consumer.accept((ItemStack)stack);
            } else {
                ItemStack lv;
                for (int i = stack.getCount(); i > 0; i -= lv.getCount()) {
                    lv = stack.copyWithCount(Math.min(stack.getMaxCount(), i));
                    consumer.accept(lv);
                }
            }
        };
    }

    public void generateUnprocessedLoot(LootContextParameterSet parameters, Consumer<ItemStack> lootConsumer) {
        this.generateUnprocessedLoot(new LootContext.Builder(parameters).build(this.randomSequenceId), lootConsumer);
    }

    public void generateUnprocessedLoot(LootContext context, Consumer<ItemStack> lootConsumer) {
        LootContext.Entry<LootTable> lv = LootContext.table(this);
        if (context.markActive(lv)) {
            Consumer<ItemStack> consumer2 = LootFunction.apply(this.combinedFunction, lootConsumer, context);
            for (LootPool lv2 : this.pools) {
                lv2.addGeneratedLoot(consumer2, context);
            }
            context.markInactive(lv);
        } else {
            LOGGER.warn("Detected infinite loop in loot tables");
        }
    }

    public void generateLoot(LootContextParameterSet parameters, long seed, Consumer<ItemStack> lootConsumer) {
        this.generateUnprocessedLoot(new LootContext.Builder(parameters).random(seed).build(this.randomSequenceId), LootTable.processStacks(parameters.getWorld(), lootConsumer));
    }

    public void generateLoot(LootContextParameterSet parameters, Consumer<ItemStack> lootConsumer) {
        this.generateUnprocessedLoot(parameters, LootTable.processStacks(parameters.getWorld(), lootConsumer));
    }

    public void generateLoot(LootContext context, Consumer<ItemStack> lootConsumer) {
        this.generateUnprocessedLoot(context, LootTable.processStacks(context.getWorld(), lootConsumer));
    }

    public ObjectArrayList<ItemStack> generateLoot(LootContextParameterSet parameters, Random random) {
        return this.generateLoot(new LootContext.Builder(parameters).random(random).build(this.randomSequenceId));
    }

    public ObjectArrayList<ItemStack> generateLoot(LootContextParameterSet parameters, long seed) {
        return this.generateLoot(new LootContext.Builder(parameters).random(seed).build(this.randomSequenceId));
    }

    public ObjectArrayList<ItemStack> generateLoot(LootContextParameterSet parameters) {
        return this.generateLoot(new LootContext.Builder(parameters).build(this.randomSequenceId));
    }

    private ObjectArrayList<ItemStack> generateLoot(LootContext context) {
        ObjectArrayList<ItemStack> objectArrayList = new ObjectArrayList<ItemStack>();
        this.generateLoot(context, objectArrayList::add);
        return objectArrayList;
    }

    public LootContextType getType() {
        return this.type;
    }

    public void validate(LootTableReporter reporter) {
        int i;
        for (i = 0; i < this.pools.size(); ++i) {
            this.pools.get(i).validate(reporter.makeChild(".pools[" + i + "]"));
        }
        for (i = 0; i < this.functions.size(); ++i) {
            this.functions.get(i).validate(reporter.makeChild(".functions[" + i + "]"));
        }
    }

    public void supplyInventory(Inventory inventory, LootContextParameterSet parameters, long seed) {
        LootContext lv = new LootContext.Builder(parameters).random(seed).build(this.randomSequenceId);
        ObjectArrayList<ItemStack> objectArrayList = this.generateLoot(lv);
        Random lv2 = lv.getRandom();
        List<Integer> list = this.getFreeSlots(inventory, lv2);
        this.shuffle(objectArrayList, list.size(), lv2);
        for (ItemStack lv3 : objectArrayList) {
            if (list.isEmpty()) {
                LOGGER.warn("Tried to over-fill a container");
                return;
            }
            if (lv3.isEmpty()) {
                inventory.setStack(list.remove(list.size() - 1), ItemStack.EMPTY);
                continue;
            }
            inventory.setStack(list.remove(list.size() - 1), lv3);
        }
    }

    private void shuffle(ObjectArrayList<ItemStack> drops, int freeSlots, Random random) {
        ArrayList<ItemStack> list = Lists.newArrayList();
        ObjectIterator iterator = drops.iterator();
        while (iterator.hasNext()) {
            ItemStack lv = (ItemStack)iterator.next();
            if (lv.isEmpty()) {
                iterator.remove();
                continue;
            }
            if (lv.getCount() <= 1) continue;
            list.add(lv);
            iterator.remove();
        }
        while (freeSlots - drops.size() - list.size() > 0 && !list.isEmpty()) {
            ItemStack lv2 = (ItemStack)list.remove(MathHelper.nextInt(random, 0, list.size() - 1));
            int j = MathHelper.nextInt(random, 1, lv2.getCount() / 2);
            ItemStack lv3 = lv2.split(j);
            if (lv2.getCount() > 1 && random.nextBoolean()) {
                list.add(lv2);
            } else {
                drops.add(lv2);
            }
            if (lv3.getCount() > 1 && random.nextBoolean()) {
                list.add(lv3);
                continue;
            }
            drops.add(lv3);
        }
        drops.addAll((Collection<ItemStack>)list);
        Util.shuffle(drops, random);
    }

    private List<Integer> getFreeSlots(Inventory inventory, Random random) {
        ObjectArrayList<Integer> objectArrayList = new ObjectArrayList<Integer>();
        for (int i = 0; i < inventory.size(); ++i) {
            if (!inventory.getStack(i).isEmpty()) continue;
            objectArrayList.add(i);
        }
        Util.shuffle(objectArrayList, random);
        return objectArrayList;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder
    implements LootFunctionConsumingBuilder<Builder> {
        private final ImmutableList.Builder<LootPool> pools = ImmutableList.builder();
        private final ImmutableList.Builder<LootFunction> functions = ImmutableList.builder();
        private LootContextType type = GENERIC;
        private Optional<Identifier> randomSequenceId = Optional.empty();

        public Builder pool(LootPool.Builder poolBuilder) {
            this.pools.add((Object)poolBuilder.build());
            return this;
        }

        public Builder type(LootContextType type) {
            this.type = type;
            return this;
        }

        public Builder randomSequenceId(Identifier randomSequenceId) {
            this.randomSequenceId = Optional.of(randomSequenceId);
            return this;
        }

        @Override
        public Builder apply(LootFunction.Builder arg) {
            this.functions.add((Object)arg.build());
            return this;
        }

        @Override
        public Builder getThisFunctionConsumingBuilder() {
            return this;
        }

        public LootTable build() {
            return new LootTable(this.type, this.randomSequenceId, (List<LootPool>)((Object)this.pools.build()), (List<LootFunction>)((Object)this.functions.build()));
        }

        @Override
        public /* synthetic */ LootFunctionConsumingBuilder getThisFunctionConsumingBuilder() {
            return this.getThisFunctionConsumingBuilder();
        }

        @Override
        public /* synthetic */ LootFunctionConsumingBuilder apply(LootFunction.Builder function) {
            return this.apply(function);
        }
    }
}

