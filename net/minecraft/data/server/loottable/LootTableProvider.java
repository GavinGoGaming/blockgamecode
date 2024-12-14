/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.data.server.loottable;

import com.google.common.collect.Multimap;
import com.google.common.collect.Sets;
import com.mojang.logging.LogUtils;
import com.mojang.serialization.Lifecycle;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import java.nio.file.Path;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;
import net.minecraft.data.DataOutput;
import net.minecraft.data.DataProvider;
import net.minecraft.data.DataWriter;
import net.minecraft.data.server.loottable.LootTableGenerator;
import net.minecraft.loot.LootTable;
import net.minecraft.loot.LootTableReporter;
import net.minecraft.loot.context.LootContextType;
import net.minecraft.loot.context.LootContextTypes;
import net.minecraft.registry.DynamicRegistryManager;
import net.minecraft.registry.RegistryEntryLookup;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.registry.SimpleRegistry;
import net.minecraft.registry.entry.RegistryEntryInfo;
import net.minecraft.util.ErrorReporter;
import net.minecraft.util.Identifier;
import net.minecraft.util.Util;
import net.minecraft.util.math.random.RandomSequence;
import org.slf4j.Logger;

public class LootTableProvider
implements DataProvider {
    private static final Logger LOGGER = LogUtils.getLogger();
    private final DataOutput.PathResolver pathResolver;
    private final Set<RegistryKey<LootTable>> lootTableIds;
    private final List<LootTypeGenerator> lootTypeGenerators;
    private final CompletableFuture<RegistryWrapper.WrapperLookup> registryLookupFuture;

    public LootTableProvider(DataOutput output, Set<RegistryKey<LootTable>> lootTableIds, List<LootTypeGenerator> lootTypeGenerators, CompletableFuture<RegistryWrapper.WrapperLookup> registryLookupFuture) {
        this.pathResolver = output.getResolver(RegistryKeys.LOOT_TABLE);
        this.lootTypeGenerators = lootTypeGenerators;
        this.lootTableIds = lootTableIds;
        this.registryLookupFuture = registryLookupFuture;
    }

    @Override
    public CompletableFuture<?> run(DataWriter writer) {
        return this.registryLookupFuture.thenCompose(registryLookup -> this.run(writer, (RegistryWrapper.WrapperLookup)registryLookup));
    }

    private CompletableFuture<?> run(DataWriter writer, RegistryWrapper.WrapperLookup registryLookup) {
        SimpleRegistry<LootTable> lv = new SimpleRegistry<LootTable>(RegistryKeys.LOOT_TABLE, Lifecycle.experimental());
        Object2ObjectOpenHashMap map = new Object2ObjectOpenHashMap();
        this.lootTypeGenerators.forEach(lootTypeGenerator -> lootTypeGenerator.provider().apply(registryLookup).accept((lootTable, builder) -> {
            Identifier lv = LootTableProvider.getId(lootTable);
            Identifier lv2 = map.put(RandomSequence.createSeed(lv), lv);
            if (lv2 != null) {
                Util.error("Loot table random sequence seed collision on " + String.valueOf(lv2) + " and " + String.valueOf(lootTable.getValue()));
            }
            builder.randomSequenceId(lv);
            LootTable lv3 = builder.type(arg.paramSet).build();
            lv.add((RegistryKey<LootTable>)lootTable, lv3, RegistryEntryInfo.DEFAULT);
        }));
        lv.freeze();
        ErrorReporter.Impl lv2 = new ErrorReporter.Impl();
        RegistryEntryLookup.RegistryLookup lv3 = new DynamicRegistryManager.ImmutableImpl(List.of(lv)).toImmutable().createRegistryLookup();
        LootTableReporter lv4 = new LootTableReporter(lv2, LootContextTypes.GENERIC, lv3);
        Sets.SetView<RegistryKey<LootTable>> set = Sets.difference(this.lootTableIds, lv.getKeys());
        for (RegistryKey registryKey : set) {
            lv2.report("Missing built-in table: " + String.valueOf(registryKey.getValue()));
        }
        lv.streamEntries().forEach(entry -> ((LootTable)entry.value()).validate(lv4.withContextType(((LootTable)entry.value()).getType()).makeChild("{" + String.valueOf(entry.registryKey().getValue()) + "}", entry.registryKey())));
        Multimap<String, String> multimap = lv2.getErrors();
        if (!multimap.isEmpty()) {
            multimap.forEach((name, message) -> LOGGER.warn("Found validation problem in {}: {}", name, message));
            throw new IllegalStateException("Failed to validate loot tables, see logs");
        }
        return CompletableFuture.allOf((CompletableFuture[])lv.getEntrySet().stream().map(entry -> {
            RegistryKey lv = (RegistryKey)entry.getKey();
            LootTable lv2 = (LootTable)entry.getValue();
            Path path = this.pathResolver.resolveJson(lv.getValue());
            return DataProvider.writeCodecToPath(writer, registryLookup, LootTable.CODEC, lv2, path);
        }).toArray(CompletableFuture[]::new));
    }

    private static Identifier getId(RegistryKey<LootTable> lootTableKey) {
        return lootTableKey.getValue();
    }

    @Override
    public final String getName() {
        return "Loot Tables";
    }

    public record LootTypeGenerator(Function<RegistryWrapper.WrapperLookup, LootTableGenerator> provider, LootContextType paramSet) {
    }
}

