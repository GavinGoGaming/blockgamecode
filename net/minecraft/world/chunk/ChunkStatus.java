/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 * 
 * Could not load the following classes:
 *  org.jetbrains.annotations.Nullable
 *  org.jetbrains.annotations.VisibleForTesting
 */
package net.minecraft.world.chunk;

import com.google.common.collect.Lists;
import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumSet;
import java.util.List;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;
import net.minecraft.world.Heightmap;
import net.minecraft.world.chunk.ChunkType;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.VisibleForTesting;

public class ChunkStatus {
    public static final int field_35470 = 8;
    private static final EnumSet<Heightmap.Type> WORLD_GEN_HEIGHTMAP_TYPES = EnumSet.of(Heightmap.Type.OCEAN_FLOOR_WG, Heightmap.Type.WORLD_SURFACE_WG);
    public static final EnumSet<Heightmap.Type> NORMAL_HEIGHTMAP_TYPES = EnumSet.of(Heightmap.Type.OCEAN_FLOOR, Heightmap.Type.WORLD_SURFACE, Heightmap.Type.MOTION_BLOCKING, Heightmap.Type.MOTION_BLOCKING_NO_LEAVES);
    public static final ChunkStatus EMPTY = ChunkStatus.register("empty", null, WORLD_GEN_HEIGHTMAP_TYPES, ChunkType.PROTOCHUNK);
    public static final ChunkStatus STRUCTURE_STARTS = ChunkStatus.register("structure_starts", EMPTY, WORLD_GEN_HEIGHTMAP_TYPES, ChunkType.PROTOCHUNK);
    public static final ChunkStatus STRUCTURE_REFERENCES = ChunkStatus.register("structure_references", STRUCTURE_STARTS, WORLD_GEN_HEIGHTMAP_TYPES, ChunkType.PROTOCHUNK);
    public static final ChunkStatus BIOMES = ChunkStatus.register("biomes", STRUCTURE_REFERENCES, WORLD_GEN_HEIGHTMAP_TYPES, ChunkType.PROTOCHUNK);
    public static final ChunkStatus NOISE = ChunkStatus.register("noise", BIOMES, WORLD_GEN_HEIGHTMAP_TYPES, ChunkType.PROTOCHUNK);
    public static final ChunkStatus SURFACE = ChunkStatus.register("surface", NOISE, WORLD_GEN_HEIGHTMAP_TYPES, ChunkType.PROTOCHUNK);
    public static final ChunkStatus CARVERS = ChunkStatus.register("carvers", SURFACE, NORMAL_HEIGHTMAP_TYPES, ChunkType.PROTOCHUNK);
    public static final ChunkStatus FEATURES = ChunkStatus.register("features", CARVERS, NORMAL_HEIGHTMAP_TYPES, ChunkType.PROTOCHUNK);
    public static final ChunkStatus INITIALIZE_LIGHT = ChunkStatus.register("initialize_light", FEATURES, NORMAL_HEIGHTMAP_TYPES, ChunkType.PROTOCHUNK);
    public static final ChunkStatus LIGHT = ChunkStatus.register("light", INITIALIZE_LIGHT, NORMAL_HEIGHTMAP_TYPES, ChunkType.PROTOCHUNK);
    public static final ChunkStatus SPAWN = ChunkStatus.register("spawn", LIGHT, NORMAL_HEIGHTMAP_TYPES, ChunkType.PROTOCHUNK);
    public static final ChunkStatus FULL = ChunkStatus.register("full", SPAWN, NORMAL_HEIGHTMAP_TYPES, ChunkType.LEVELCHUNK);
    private final int index;
    private final ChunkStatus previous;
    private final ChunkType chunkType;
    private final EnumSet<Heightmap.Type> heightMapTypes;

    private static ChunkStatus register(String id, @Nullable ChunkStatus previous, EnumSet<Heightmap.Type> heightMapTypes, ChunkType chunkType) {
        return Registry.register(Registries.CHUNK_STATUS, id, new ChunkStatus(previous, heightMapTypes, chunkType));
    }

    public static List<ChunkStatus> createOrderedList() {
        ChunkStatus lv;
        ArrayList<ChunkStatus> list = Lists.newArrayList();
        for (lv = FULL; lv.getPrevious() != lv; lv = lv.getPrevious()) {
            list.add(lv);
        }
        list.add(lv);
        Collections.reverse(list);
        return list;
    }

    @VisibleForTesting
    protected ChunkStatus(@Nullable ChunkStatus previous, EnumSet<Heightmap.Type> heightMapTypes, ChunkType chunkType) {
        this.previous = previous == null ? this : previous;
        this.chunkType = chunkType;
        this.heightMapTypes = heightMapTypes;
        this.index = previous == null ? 0 : previous.getIndex() + 1;
    }

    public int getIndex() {
        return this.index;
    }

    public ChunkStatus getPrevious() {
        return this.previous;
    }

    public ChunkType getChunkType() {
        return this.chunkType;
    }

    public static ChunkStatus byId(String id) {
        return Registries.CHUNK_STATUS.get(Identifier.tryParse(id));
    }

    public EnumSet<Heightmap.Type> getHeightmapTypes() {
        return this.heightMapTypes;
    }

    public boolean isAtLeast(ChunkStatus other) {
        return this.getIndex() >= other.getIndex();
    }

    public boolean isLaterThan(ChunkStatus other) {
        return this.getIndex() > other.getIndex();
    }

    public boolean isAtMost(ChunkStatus other) {
        return this.getIndex() <= other.getIndex();
    }

    public boolean isEarlierThan(ChunkStatus other) {
        return this.getIndex() < other.getIndex();
    }

    public static ChunkStatus max(ChunkStatus a, ChunkStatus b) {
        return a.isLaterThan(b) ? a : b;
    }

    public String toString() {
        return this.getId();
    }

    public String getId() {
        return Registries.CHUNK_STATUS.getId(this).toString();
    }
}

