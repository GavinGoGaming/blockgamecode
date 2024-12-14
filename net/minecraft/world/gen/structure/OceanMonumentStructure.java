/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.world.gen.structure;

import com.mojang.serialization.MapCodec;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.registry.tag.BiomeTags;
import net.minecraft.structure.OceanMonumentGenerator;
import net.minecraft.structure.StructurePiece;
import net.minecraft.structure.StructurePiecesCollector;
import net.minecraft.structure.StructurePiecesList;
import net.minecraft.util.math.BlockBox;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.random.CheckedRandom;
import net.minecraft.util.math.random.ChunkRandom;
import net.minecraft.util.math.random.RandomSeed;
import net.minecraft.world.Heightmap;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.gen.structure.Structure;
import net.minecraft.world.gen.structure.StructureType;

public class OceanMonumentStructure
extends Structure {
    public static final MapCodec<OceanMonumentStructure> CODEC = OceanMonumentStructure.createCodec(OceanMonumentStructure::new);

    public OceanMonumentStructure(Structure.Config arg) {
        super(arg);
    }

    @Override
    public Optional<Structure.StructurePosition> getStructurePosition(Structure.Context context) {
        int i = context.chunkPos().getOffsetX(9);
        int j = context.chunkPos().getOffsetZ(9);
        Set<RegistryEntry<Biome>> set = context.biomeSource().getBiomesInArea(i, context.chunkGenerator().getSeaLevel(), j, 29, context.noiseConfig().getMultiNoiseSampler());
        for (RegistryEntry<Biome> lv : set) {
            if (lv.isIn(BiomeTags.REQUIRED_OCEAN_MONUMENT_SURROUNDING)) continue;
            return Optional.empty();
        }
        return OceanMonumentStructure.getStructurePosition(context, Heightmap.Type.OCEAN_FLOOR_WG, collector -> OceanMonumentStructure.addPieces(collector, context));
    }

    private static StructurePiece createBasePiece(ChunkPos pos, ChunkRandom random) {
        int i = pos.getStartX() - 29;
        int j = pos.getStartZ() - 29;
        Direction lv = Direction.Type.HORIZONTAL.random(random);
        return new OceanMonumentGenerator.Base(random, i, j, lv);
    }

    private static void addPieces(StructurePiecesCollector collector, Structure.Context context) {
        collector.addPiece(OceanMonumentStructure.createBasePiece(context.chunkPos(), context.random()));
    }

    public static StructurePiecesList modifyPiecesOnRead(ChunkPos pos, long worldSeed, StructurePiecesList pieces) {
        if (pieces.isEmpty()) {
            return pieces;
        }
        ChunkRandom lv = new ChunkRandom(new CheckedRandom(RandomSeed.getSeed()));
        lv.setCarverSeed(worldSeed, pos.x, pos.z);
        StructurePiece lv2 = pieces.pieces().get(0);
        BlockBox lv3 = lv2.getBoundingBox();
        int i = lv3.getMinX();
        int j = lv3.getMinZ();
        Direction lv4 = Direction.Type.HORIZONTAL.random(lv);
        Direction lv5 = Objects.requireNonNullElse(lv2.getFacing(), lv4);
        OceanMonumentGenerator.Base lv6 = new OceanMonumentGenerator.Base(lv, i, j, lv5);
        StructurePiecesCollector lv7 = new StructurePiecesCollector();
        lv7.addPiece(lv6);
        return lv7.toList();
    }

    @Override
    public StructureType<?> getType() {
        return StructureType.OCEAN_MONUMENT;
    }
}

