/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.structure;

import com.google.common.collect.ImmutableList;
import com.mojang.datafixers.util.Pair;
import net.minecraft.registry.Registerable;
import net.minecraft.registry.RegistryEntryLookup;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.structure.AncientCityOutskirtsGenerator;
import net.minecraft.structure.pool.StructurePool;
import net.minecraft.structure.pool.StructurePoolElement;
import net.minecraft.structure.pool.StructurePools;
import net.minecraft.structure.processor.StructureProcessorList;
import net.minecraft.structure.processor.StructureProcessorLists;

public class AncientCityGenerator {
    public static final RegistryKey<StructurePool> CITY_CENTER = StructurePools.ofVanilla("ancient_city/city_center");

    public static void bootstrap(Registerable<StructurePool> poolRegisterable) {
        RegistryEntryLookup<StructureProcessorList> lv = poolRegisterable.getRegistryLookup(RegistryKeys.PROCESSOR_LIST);
        RegistryEntry.Reference<StructureProcessorList> lv2 = lv.getOrThrow(StructureProcessorLists.ANCIENT_CITY_START_DEGRADATION);
        RegistryEntryLookup<StructurePool> lv3 = poolRegisterable.getRegistryLookup(RegistryKeys.TEMPLATE_POOL);
        RegistryEntry.Reference<StructurePool> lv4 = lv3.getOrThrow(StructurePools.EMPTY);
        poolRegisterable.register(CITY_CENTER, new StructurePool(lv4, ImmutableList.of(Pair.of(StructurePoolElement.ofProcessedSingle("ancient_city/city_center/city_center_1", lv2), 1), Pair.of(StructurePoolElement.ofProcessedSingle("ancient_city/city_center/city_center_2", lv2), 1), Pair.of(StructurePoolElement.ofProcessedSingle("ancient_city/city_center/city_center_3", lv2), 1)), StructurePool.Projection.RIGID));
        AncientCityOutskirtsGenerator.bootstrap(poolRegisterable);
    }
}

