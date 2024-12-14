/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.structure;

import com.google.common.collect.ImmutableList;
import com.mojang.datafixers.util.Pair;
import net.minecraft.registry.Registerable;
import net.minecraft.registry.RegistryEntryLookup;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.structure.pool.StructurePool;
import net.minecraft.structure.pool.StructurePoolElement;
import net.minecraft.structure.pool.StructurePools;
import net.minecraft.structure.processor.StructureProcessorList;
import net.minecraft.structure.processor.StructureProcessorLists;

public class BastionBridgeData {
    public static void bootstrap(Registerable<StructurePool> poolRegisterable) {
        RegistryEntryLookup<StructureProcessorList> lv = poolRegisterable.getRegistryLookup(RegistryKeys.PROCESSOR_LIST);
        RegistryEntry.Reference<StructureProcessorList> lv2 = lv.getOrThrow(StructureProcessorLists.ENTRANCE_REPLACEMENT);
        RegistryEntry.Reference<StructureProcessorList> lv3 = lv.getOrThrow(StructureProcessorLists.BASTION_GENERIC_DEGRADATION);
        RegistryEntry.Reference<StructureProcessorList> lv4 = lv.getOrThrow(StructureProcessorLists.BRIDGE);
        RegistryEntry.Reference<StructureProcessorList> lv5 = lv.getOrThrow(StructureProcessorLists.RAMPART_DEGRADATION);
        RegistryEntryLookup<StructurePool> lv6 = poolRegisterable.getRegistryLookup(RegistryKeys.TEMPLATE_POOL);
        RegistryEntry.Reference<StructurePool> lv7 = lv6.getOrThrow(StructurePools.EMPTY);
        StructurePools.register(poolRegisterable, "bastion/bridge/starting_pieces", new StructurePool(lv7, ImmutableList.of(Pair.of(StructurePoolElement.ofProcessedSingle("bastion/bridge/starting_pieces/entrance", lv2), 1), Pair.of(StructurePoolElement.ofProcessedSingle("bastion/bridge/starting_pieces/entrance_face", lv3), 1)), StructurePool.Projection.RIGID));
        StructurePools.register(poolRegisterable, "bastion/bridge/bridge_pieces", new StructurePool(lv7, ImmutableList.of(Pair.of(StructurePoolElement.ofProcessedSingle("bastion/bridge/bridge_pieces/bridge", lv4), 1)), StructurePool.Projection.RIGID));
        StructurePools.register(poolRegisterable, "bastion/bridge/legs", new StructurePool(lv7, ImmutableList.of(Pair.of(StructurePoolElement.ofProcessedSingle("bastion/bridge/legs/leg_0", lv3), 1), Pair.of(StructurePoolElement.ofProcessedSingle("bastion/bridge/legs/leg_1", lv3), 1)), StructurePool.Projection.RIGID));
        StructurePools.register(poolRegisterable, "bastion/bridge/walls", new StructurePool(lv7, ImmutableList.of(Pair.of(StructurePoolElement.ofProcessedSingle("bastion/bridge/walls/wall_base_0", lv5), 1), Pair.of(StructurePoolElement.ofProcessedSingle("bastion/bridge/walls/wall_base_1", lv5), 1)), StructurePool.Projection.RIGID));
        StructurePools.register(poolRegisterable, "bastion/bridge/ramparts", new StructurePool(lv7, ImmutableList.of(Pair.of(StructurePoolElement.ofProcessedSingle("bastion/bridge/ramparts/rampart_0", lv5), 1), Pair.of(StructurePoolElement.ofProcessedSingle("bastion/bridge/ramparts/rampart_1", lv5), 1)), StructurePool.Projection.RIGID));
        StructurePools.register(poolRegisterable, "bastion/bridge/rampart_plates", new StructurePool(lv7, ImmutableList.of(Pair.of(StructurePoolElement.ofProcessedSingle("bastion/bridge/rampart_plates/plate_0", lv5), 1)), StructurePool.Projection.RIGID));
        StructurePools.register(poolRegisterable, "bastion/bridge/connectors", new StructurePool(lv7, ImmutableList.of(Pair.of(StructurePoolElement.ofProcessedSingle("bastion/bridge/connectors/back_bridge_top", lv3), 1), Pair.of(StructurePoolElement.ofProcessedSingle("bastion/bridge/connectors/back_bridge_bottom", lv3), 1)), StructurePool.Projection.RIGID));
    }
}

