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
import net.minecraft.structure.pool.StructurePool;
import net.minecraft.structure.pool.StructurePoolElement;
import net.minecraft.structure.pool.StructurePools;
import net.minecraft.structure.processor.StructureProcessorList;
import net.minecraft.structure.processor.StructureProcessorLists;

public class PillagerOutpostGenerator {
    public static final RegistryKey<StructurePool> STRUCTURE_POOLS = StructurePools.ofVanilla("pillager_outpost/base_plates");

    public static void bootstrap(Registerable<StructurePool> poolRegisterable) {
        RegistryEntryLookup<StructureProcessorList> lv = poolRegisterable.getRegistryLookup(RegistryKeys.PROCESSOR_LIST);
        RegistryEntry.Reference<StructureProcessorList> lv2 = lv.getOrThrow(StructureProcessorLists.OUTPOST_ROT);
        RegistryEntryLookup<StructurePool> lv3 = poolRegisterable.getRegistryLookup(RegistryKeys.TEMPLATE_POOL);
        RegistryEntry.Reference<StructurePool> lv4 = lv3.getOrThrow(StructurePools.EMPTY);
        poolRegisterable.register(STRUCTURE_POOLS, new StructurePool(lv4, ImmutableList.of(Pair.of(StructurePoolElement.ofLegacySingle("pillager_outpost/base_plate"), 1)), StructurePool.Projection.RIGID));
        StructurePools.register(poolRegisterable, "pillager_outpost/towers", new StructurePool(lv4, ImmutableList.of(Pair.of(StructurePoolElement.ofList(ImmutableList.of(StructurePoolElement.ofLegacySingle("pillager_outpost/watchtower"), StructurePoolElement.ofProcessedLegacySingle("pillager_outpost/watchtower_overgrown", lv2))), 1)), StructurePool.Projection.RIGID));
        StructurePools.register(poolRegisterable, "pillager_outpost/feature_plates", new StructurePool(lv4, ImmutableList.of(Pair.of(StructurePoolElement.ofLegacySingle("pillager_outpost/feature_plate"), 1)), StructurePool.Projection.TERRAIN_MATCHING));
        StructurePools.register(poolRegisterable, "pillager_outpost/features", new StructurePool(lv4, ImmutableList.of(Pair.of(StructurePoolElement.ofLegacySingle("pillager_outpost/feature_cage1"), 1), Pair.of(StructurePoolElement.ofLegacySingle("pillager_outpost/feature_cage2"), 1), Pair.of(StructurePoolElement.ofLegacySingle("pillager_outpost/feature_cage_with_allays"), 1), Pair.of(StructurePoolElement.ofLegacySingle("pillager_outpost/feature_logs"), 1), Pair.of(StructurePoolElement.ofLegacySingle("pillager_outpost/feature_tent1"), 1), Pair.of(StructurePoolElement.ofLegacySingle("pillager_outpost/feature_tent2"), 1), Pair.of(StructurePoolElement.ofLegacySingle("pillager_outpost/feature_targets"), 1), Pair.of(StructurePoolElement.ofEmpty(), 6)), StructurePool.Projection.RIGID));
    }
}

