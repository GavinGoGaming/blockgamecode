/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 * 
 * Could not load the following classes:
 *  org.jetbrains.annotations.Nullable
 */
package net.minecraft.world.gen;

import com.google.common.collect.ImmutableList;
import it.unimi.dsi.fastutil.longs.LongIterator;
import it.unimi.dsi.fastutil.longs.LongSet;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Predicate;
import net.minecraft.registry.DynamicRegistryManager;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.registry.entry.RegistryEntryList;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.structure.StructurePiece;
import net.minecraft.structure.StructureStart;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.ChunkSectionPos;
import net.minecraft.world.ChunkRegion;
import net.minecraft.world.StructureHolder;
import net.minecraft.world.StructureLocator;
import net.minecraft.world.StructurePresence;
import net.minecraft.world.WorldAccess;
import net.minecraft.world.chunk.ChunkStatus;
import net.minecraft.world.gen.GeneratorOptions;
import net.minecraft.world.gen.chunk.placement.StructurePlacement;
import net.minecraft.world.gen.structure.Structure;
import org.jetbrains.annotations.Nullable;

public class StructureAccessor {
    private final WorldAccess world;
    private final GeneratorOptions options;
    private final StructureLocator locator;

    public StructureAccessor(WorldAccess world, GeneratorOptions options, StructureLocator locator) {
        this.world = world;
        this.options = options;
        this.locator = locator;
    }

    public StructureAccessor forRegion(ChunkRegion region) {
        if (region.toServerWorld() != this.world) {
            throw new IllegalStateException("Using invalid structure manager (source level: " + String.valueOf(region.toServerWorld()) + ", region: " + String.valueOf(region));
        }
        return new StructureAccessor(region, this.options, this.locator);
    }

    public List<StructureStart> getStructureStarts(ChunkPos pos, Predicate<Structure> predicate) {
        Map<Structure, LongSet> map = this.world.getChunk(pos.x, pos.z, ChunkStatus.STRUCTURE_REFERENCES).getStructureReferences();
        ImmutableList.Builder builder = ImmutableList.builder();
        for (Map.Entry<Structure, LongSet> entry : map.entrySet()) {
            Structure lv = entry.getKey();
            if (!predicate.test(lv)) continue;
            this.acceptStructureStarts(lv, entry.getValue(), builder::add);
        }
        return builder.build();
    }

    public List<StructureStart> getStructureStarts(ChunkSectionPos sectionPos, Structure structure) {
        LongSet longSet = this.world.getChunk(sectionPos.getSectionX(), sectionPos.getSectionZ(), ChunkStatus.STRUCTURE_REFERENCES).getStructureReferences(structure);
        ImmutableList.Builder builder = ImmutableList.builder();
        this.acceptStructureStarts(structure, longSet, builder::add);
        return builder.build();
    }

    public void acceptStructureStarts(Structure structure, LongSet structureStartPositions, Consumer<StructureStart> consumer) {
        LongIterator longIterator = structureStartPositions.iterator();
        while (longIterator.hasNext()) {
            long l = (Long)longIterator.next();
            ChunkSectionPos lv = ChunkSectionPos.from(new ChunkPos(l), this.world.getBottomSectionCoord());
            StructureStart lv2 = this.getStructureStart(lv, structure, this.world.getChunk(lv.getSectionX(), lv.getSectionZ(), ChunkStatus.STRUCTURE_STARTS));
            if (lv2 == null || !lv2.hasChildren()) continue;
            consumer.accept(lv2);
        }
    }

    @Nullable
    public StructureStart getStructureStart(ChunkSectionPos pos, Structure structure, StructureHolder holder) {
        return holder.getStructureStart(structure);
    }

    public void setStructureStart(ChunkSectionPos pos, Structure structure, StructureStart structureStart, StructureHolder holder) {
        holder.setStructureStart(structure, structureStart);
    }

    public void addStructureReference(ChunkSectionPos pos, Structure structure, long reference, StructureHolder holder) {
        holder.addStructureReference(structure, reference);
    }

    public boolean shouldGenerateStructures() {
        return this.options.shouldGenerateStructures();
    }

    public StructureStart getStructureAt(BlockPos pos, Structure structure) {
        for (StructureStart lv : this.getStructureStarts(ChunkSectionPos.from(pos), structure)) {
            if (!lv.getBoundingBox().contains(pos)) continue;
            return lv;
        }
        return StructureStart.DEFAULT;
    }

    public StructureStart getStructureContaining(BlockPos pos, TagKey<Structure> tag) {
        return this.getStructureContaining(pos, (RegistryEntry<Structure> structure) -> structure.isIn(tag));
    }

    public StructureStart getStructureContaining(BlockPos pos, RegistryEntryList<Structure> structures) {
        return this.getStructureContaining(pos, structures::contains);
    }

    public StructureStart getStructureContaining(BlockPos pos, Predicate<RegistryEntry<Structure>> predicate) {
        Registry<Structure> lv = this.getRegistryManager().get(RegistryKeys.STRUCTURE);
        for (StructureStart lv2 : this.getStructureStarts(new ChunkPos(pos), (Structure structure) -> lv.getEntry(lv.getRawId((Structure)structure)).map(predicate::test).orElse(false))) {
            if (!this.structureContains(pos, lv2)) continue;
            return lv2;
        }
        return StructureStart.DEFAULT;
    }

    public StructureStart getStructureContaining(BlockPos pos, Structure structure) {
        for (StructureStart lv : this.getStructureStarts(ChunkSectionPos.from(pos), structure)) {
            if (!this.structureContains(pos, lv)) continue;
            return lv;
        }
        return StructureStart.DEFAULT;
    }

    public boolean structureContains(BlockPos pos, StructureStart structureStart) {
        for (StructurePiece lv : structureStart.getChildren()) {
            if (!lv.getBoundingBox().contains(pos)) continue;
            return true;
        }
        return false;
    }

    public boolean hasStructureReferences(BlockPos pos) {
        ChunkSectionPos lv = ChunkSectionPos.from(pos);
        return this.world.getChunk(lv.getSectionX(), lv.getSectionZ(), ChunkStatus.STRUCTURE_REFERENCES).hasStructureReferences();
    }

    public Map<Structure, LongSet> getStructureReferences(BlockPos pos) {
        ChunkSectionPos lv = ChunkSectionPos.from(pos);
        return this.world.getChunk(lv.getSectionX(), lv.getSectionZ(), ChunkStatus.STRUCTURE_REFERENCES).getStructureReferences();
    }

    public StructurePresence getStructurePresence(ChunkPos chunkPos, Structure structure, StructurePlacement placement, boolean skipReferencedStructures) {
        return this.locator.getStructurePresence(chunkPos, structure, placement, skipReferencedStructures);
    }

    public void incrementReferences(StructureStart structureStart) {
        structureStart.incrementReferences();
        this.locator.incrementReferences(structureStart.getPos(), structureStart.getStructure());
    }

    public DynamicRegistryManager getRegistryManager() {
        return this.world.getRegistryManager();
    }
}

