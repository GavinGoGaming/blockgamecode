/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.world.gen.structure;

import com.mojang.datafixers.kinds.Applicative;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Optional;
import net.minecraft.structure.ShipwreckGenerator;
import net.minecraft.structure.StructurePiecesCollector;
import net.minecraft.util.BlockRotation;
import net.minecraft.util.math.BlockBox;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.Heightmap;
import net.minecraft.world.gen.structure.Structure;
import net.minecraft.world.gen.structure.StructureType;

public class ShipwreckStructure
extends Structure {
    public static final MapCodec<ShipwreckStructure> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(ShipwreckStructure.configCodecBuilder(instance), ((MapCodec)Codec.BOOL.fieldOf("is_beached")).forGetter(arg -> arg.beached)).apply((Applicative<ShipwreckStructure, ?>)instance, ShipwreckStructure::new));
    public final boolean beached;

    public ShipwreckStructure(Structure.Config config, boolean beached) {
        super(config);
        this.beached = beached;
    }

    @Override
    public Optional<Structure.StructurePosition> getStructurePosition(Structure.Context context) {
        Heightmap.Type lv = this.beached ? Heightmap.Type.WORLD_SURFACE_WG : Heightmap.Type.OCEAN_FLOOR_WG;
        return ShipwreckStructure.getStructurePosition(context, lv, collector -> this.addPieces((StructurePiecesCollector)collector, context));
    }

    private void addPieces(StructurePiecesCollector collector, Structure.Context context) {
        BlockRotation lv = BlockRotation.random(context.random());
        BlockPos lv2 = new BlockPos(context.chunkPos().getStartX(), 90, context.chunkPos().getStartZ());
        ShipwreckGenerator.Piece lv3 = ShipwreckGenerator.addParts(context.structureTemplateManager(), lv2, lv, collector, context.random(), this.beached);
        if (lv3.isTooLargeForNormalGeneration()) {
            int j;
            BlockBox lv4 = lv3.getBoundingBox();
            if (this.beached) {
                int i = Structure.getMinCornerHeight(context, lv4.getMinX(), lv4.getBlockCountX(), lv4.getMinZ(), lv4.getBlockCountZ());
                j = lv3.findGroundedY(i, context.random());
            } else {
                j = Structure.getAverageCornerHeights(context, lv4.getMinX(), lv4.getBlockCountX(), lv4.getMinZ(), lv4.getBlockCountZ());
            }
            lv3.setY(j);
        }
    }

    @Override
    public StructureType<?> getType() {
        return StructureType.SHIPWRECK;
    }
}

