/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.structure;

import com.mojang.datafixers.util.Pair;
import java.util.List;
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

public class TrailRuinsGenerator {
    public static final RegistryKey<StructurePool> TOWER = StructurePools.ofVanilla("trail_ruins/tower");

    public static void bootstrap(Registerable<StructurePool> poolRegisterable) {
        RegistryEntryLookup<StructurePool> lv = poolRegisterable.getRegistryLookup(RegistryKeys.TEMPLATE_POOL);
        RegistryEntry.Reference<StructurePool> lv2 = lv.getOrThrow(StructurePools.EMPTY);
        RegistryEntryLookup<StructureProcessorList> lv3 = poolRegisterable.getRegistryLookup(RegistryKeys.PROCESSOR_LIST);
        RegistryEntry.Reference<StructureProcessorList> lv4 = lv3.getOrThrow(StructureProcessorLists.TRAIL_RUINS_HOUSES_ARCHAEOLOGY);
        RegistryEntry.Reference<StructureProcessorList> lv5 = lv3.getOrThrow(StructureProcessorLists.TRAIL_RUINS_ROADS_ARCHAEOLOGY);
        RegistryEntry.Reference<StructureProcessorList> lv6 = lv3.getOrThrow(StructureProcessorLists.TRAIL_RUINS_TOWER_TOP_ARCHAEOLOGY);
        poolRegisterable.register(TOWER, new StructurePool(lv2, List.of(Pair.of(StructurePoolElement.ofProcessedSingle("trail_ruins/tower/tower_1", lv4), 1), Pair.of(StructurePoolElement.ofProcessedSingle("trail_ruins/tower/tower_2", lv4), 1), Pair.of(StructurePoolElement.ofProcessedSingle("trail_ruins/tower/tower_3", lv4), 1), Pair.of(StructurePoolElement.ofProcessedSingle("trail_ruins/tower/tower_4", lv4), 1), Pair.of(StructurePoolElement.ofProcessedSingle("trail_ruins/tower/tower_5", lv4), 1)), StructurePool.Projection.RIGID));
        StructurePools.register(poolRegisterable, "trail_ruins/tower/tower_top", new StructurePool(lv2, List.of(Pair.of(StructurePoolElement.ofProcessedSingle("trail_ruins/tower/tower_top_1", lv6), 1), Pair.of(StructurePoolElement.ofProcessedSingle("trail_ruins/tower/tower_top_2", lv6), 1), Pair.of(StructurePoolElement.ofProcessedSingle("trail_ruins/tower/tower_top_3", lv6), 1), Pair.of(StructurePoolElement.ofProcessedSingle("trail_ruins/tower/tower_top_4", lv6), 1), Pair.of(StructurePoolElement.ofProcessedSingle("trail_ruins/tower/tower_top_5", lv6), 1)), StructurePool.Projection.RIGID));
        StructurePools.register(poolRegisterable, "trail_ruins/tower/additions", new StructurePool(lv2, List.of(Pair.of(StructurePoolElement.ofProcessedSingle("trail_ruins/tower/hall_1", lv4), 1), Pair.of(StructurePoolElement.ofProcessedSingle("trail_ruins/tower/hall_2", lv4), 1), Pair.of(StructurePoolElement.ofProcessedSingle("trail_ruins/tower/hall_3", lv4), 1), Pair.of(StructurePoolElement.ofProcessedSingle("trail_ruins/tower/hall_4", lv4), 1), Pair.of(StructurePoolElement.ofProcessedSingle("trail_ruins/tower/hall_5", lv4), 1), Pair.of(StructurePoolElement.ofProcessedSingle("trail_ruins/tower/large_hall_1", lv4), 1), Pair.of(StructurePoolElement.ofProcessedSingle("trail_ruins/tower/large_hall_2", lv4), 1), Pair.of(StructurePoolElement.ofProcessedSingle("trail_ruins/tower/large_hall_3", lv4), 1), Pair.of(StructurePoolElement.ofProcessedSingle("trail_ruins/tower/large_hall_4", lv4), 1), Pair.of(StructurePoolElement.ofProcessedSingle("trail_ruins/tower/large_hall_5", lv4), 1), Pair.of(StructurePoolElement.ofProcessedSingle("trail_ruins/tower/one_room_1", lv4), 1), Pair.of(StructurePoolElement.ofProcessedSingle("trail_ruins/tower/one_room_2", lv4), 1), Pair.of(StructurePoolElement.ofProcessedSingle("trail_ruins/tower/one_room_3", lv4), 1), Pair.of(StructurePoolElement.ofProcessedSingle("trail_ruins/tower/one_room_4", lv4), 1), Pair.of(StructurePoolElement.ofProcessedSingle("trail_ruins/tower/one_room_5", lv4), 1), Pair.of(StructurePoolElement.ofProcessedSingle("trail_ruins/tower/platform_1", lv4), 1), Pair.of(StructurePoolElement.ofProcessedSingle("trail_ruins/tower/platform_2", lv4), 1), Pair.of(StructurePoolElement.ofProcessedSingle("trail_ruins/tower/platform_3", lv4), 1), Pair.of(StructurePoolElement.ofProcessedSingle("trail_ruins/tower/platform_4", lv4), 1), Pair.of(StructurePoolElement.ofProcessedSingle("trail_ruins/tower/platform_5", lv4), 1), Pair.of(StructurePoolElement.ofProcessedSingle("trail_ruins/tower/stable_1", lv4), 1), Pair.of(StructurePoolElement.ofProcessedSingle("trail_ruins/tower/stable_2", lv4), 1), Pair.of(StructurePoolElement.ofProcessedSingle("trail_ruins/tower/stable_3", lv4), 1), Pair.of(StructurePoolElement.ofProcessedSingle("trail_ruins/tower/stable_4", lv4), 1), Pair.of(StructurePoolElement.ofProcessedSingle("trail_ruins/tower/stable_5", lv4), 1)), StructurePool.Projection.RIGID));
        StructurePools.register(poolRegisterable, "trail_ruins/roads", new StructurePool(lv2, List.of(Pair.of(StructurePoolElement.ofProcessedSingle("trail_ruins/roads/long_road_end", lv5), 1), Pair.of(StructurePoolElement.ofProcessedSingle("trail_ruins/roads/road_end_1", lv5), 1), Pair.of(StructurePoolElement.ofProcessedSingle("trail_ruins/roads/road_section_1", lv5), 1), Pair.of(StructurePoolElement.ofProcessedSingle("trail_ruins/roads/road_section_2", lv5), 1), Pair.of(StructurePoolElement.ofProcessedSingle("trail_ruins/roads/road_section_3", lv5), 1), Pair.of(StructurePoolElement.ofProcessedSingle("trail_ruins/roads/road_section_4", lv5), 1), Pair.of(StructurePoolElement.ofProcessedSingle("trail_ruins/roads/road_spacer_1", lv5), 1)), StructurePool.Projection.RIGID));
        StructurePools.register(poolRegisterable, "trail_ruins/buildings", new StructurePool(lv2, List.of(Pair.of(StructurePoolElement.ofProcessedSingle("trail_ruins/buildings/group_hall_1", lv4), 1), Pair.of(StructurePoolElement.ofProcessedSingle("trail_ruins/buildings/group_hall_2", lv4), 1), Pair.of(StructurePoolElement.ofProcessedSingle("trail_ruins/buildings/group_hall_3", lv4), 1), Pair.of(StructurePoolElement.ofProcessedSingle("trail_ruins/buildings/group_hall_4", lv4), 1), Pair.of(StructurePoolElement.ofProcessedSingle("trail_ruins/buildings/group_hall_5", lv4), 1), Pair.of(StructurePoolElement.ofProcessedSingle("trail_ruins/buildings/large_room_1", lv4), 1), Pair.of(StructurePoolElement.ofProcessedSingle("trail_ruins/buildings/large_room_2", lv4), 1), Pair.of(StructurePoolElement.ofProcessedSingle("trail_ruins/buildings/large_room_3", lv4), 1), Pair.of(StructurePoolElement.ofProcessedSingle("trail_ruins/buildings/large_room_4", lv4), 1), Pair.of(StructurePoolElement.ofProcessedSingle("trail_ruins/buildings/large_room_5", lv4), 1), Pair.of(StructurePoolElement.ofProcessedSingle("trail_ruins/buildings/one_room_1", lv4), 1), Pair.of(StructurePoolElement.ofProcessedSingle("trail_ruins/buildings/one_room_2", lv4), 1), Pair.of(StructurePoolElement.ofProcessedSingle("trail_ruins/buildings/one_room_3", lv4), 1), Pair.of(StructurePoolElement.ofProcessedSingle("trail_ruins/buildings/one_room_4", lv4), 1), Pair.of(StructurePoolElement.ofProcessedSingle("trail_ruins/buildings/one_room_5", lv4), 1)), StructurePool.Projection.RIGID));
        StructurePools.register(poolRegisterable, "trail_ruins/buildings/grouped", new StructurePool(lv2, List.of(Pair.of(StructurePoolElement.ofProcessedSingle("trail_ruins/buildings/group_full_1", lv4), 1), Pair.of(StructurePoolElement.ofProcessedSingle("trail_ruins/buildings/group_full_2", lv4), 1), Pair.of(StructurePoolElement.ofProcessedSingle("trail_ruins/buildings/group_full_3", lv4), 1), Pair.of(StructurePoolElement.ofProcessedSingle("trail_ruins/buildings/group_full_4", lv4), 1), Pair.of(StructurePoolElement.ofProcessedSingle("trail_ruins/buildings/group_full_5", lv4), 1), Pair.of(StructurePoolElement.ofProcessedSingle("trail_ruins/buildings/group_lower_1", lv4), 1), Pair.of(StructurePoolElement.ofProcessedSingle("trail_ruins/buildings/group_lower_2", lv4), 1), Pair.of(StructurePoolElement.ofProcessedSingle("trail_ruins/buildings/group_lower_3", lv4), 1), Pair.of(StructurePoolElement.ofProcessedSingle("trail_ruins/buildings/group_lower_4", lv4), 1), Pair.of(StructurePoolElement.ofProcessedSingle("trail_ruins/buildings/group_lower_5", lv4), 1), Pair.of(StructurePoolElement.ofProcessedSingle("trail_ruins/buildings/group_upper_1", lv4), 1), Pair.of(StructurePoolElement.ofProcessedSingle("trail_ruins/buildings/group_upper_2", lv4), 1), Pair.of(StructurePoolElement.ofProcessedSingle("trail_ruins/buildings/group_upper_3", lv4), 1), Pair.of(StructurePoolElement.ofProcessedSingle("trail_ruins/buildings/group_upper_4", lv4), 1), Pair.of(StructurePoolElement.ofProcessedSingle("trail_ruins/buildings/group_upper_5", lv4), 1), Pair.of(StructurePoolElement.ofProcessedSingle("trail_ruins/buildings/group_room_1", lv4), 1), Pair.of(StructurePoolElement.ofProcessedSingle("trail_ruins/buildings/group_room_2", lv4), 1), Pair.of(StructurePoolElement.ofProcessedSingle("trail_ruins/buildings/group_room_3", lv4), 1), Pair.of(StructurePoolElement.ofProcessedSingle("trail_ruins/buildings/group_room_4", lv4), 1), Pair.of(StructurePoolElement.ofProcessedSingle("trail_ruins/buildings/group_room_5", lv4), 1)), StructurePool.Projection.RIGID));
        StructurePools.register(poolRegisterable, "trail_ruins/decor", new StructurePool(lv2, List.of(Pair.of(StructurePoolElement.ofProcessedSingle("trail_ruins/decor/decor_1", lv4), 1), Pair.of(StructurePoolElement.ofProcessedSingle("trail_ruins/decor/decor_2", lv4), 1), Pair.of(StructurePoolElement.ofProcessedSingle("trail_ruins/decor/decor_3", lv4), 1), Pair.of(StructurePoolElement.ofProcessedSingle("trail_ruins/decor/decor_4", lv4), 1), Pair.of(StructurePoolElement.ofProcessedSingle("trail_ruins/decor/decor_5", lv4), 1), Pair.of(StructurePoolElement.ofProcessedSingle("trail_ruins/decor/decor_6", lv4), 1), Pair.of(StructurePoolElement.ofProcessedSingle("trail_ruins/decor/decor_7", lv4), 1)), StructurePool.Projection.RIGID));
    }
}

