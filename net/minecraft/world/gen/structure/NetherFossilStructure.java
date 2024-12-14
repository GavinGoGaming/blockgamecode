/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.world.gen.structure;

import com.mojang.datafixers.kinds.Applicative;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Optional;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.structure.NetherFossilGenerator;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.random.ChunkRandom;
import net.minecraft.world.EmptyBlockView;
import net.minecraft.world.gen.HeightContext;
import net.minecraft.world.gen.chunk.VerticalBlockSample;
import net.minecraft.world.gen.heightprovider.HeightProvider;
import net.minecraft.world.gen.structure.Structure;
import net.minecraft.world.gen.structure.StructureType;

public class NetherFossilStructure
extends Structure {
    public static final MapCodec<NetherFossilStructure> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(NetherFossilStructure.configCodecBuilder(instance), ((MapCodec)HeightProvider.CODEC.fieldOf("height")).forGetter(structure -> structure.height)).apply((Applicative<NetherFossilStructure, ?>)instance, NetherFossilStructure::new));
    public final HeightProvider height;

    public NetherFossilStructure(Structure.Config config, HeightProvider height) {
        super(config);
        this.height = height;
    }

    @Override
    public Optional<Structure.StructurePosition> getStructurePosition(Structure.Context context) {
        ChunkRandom lv = context.random();
        int i = context.chunkPos().getStartX() + lv.nextInt(16);
        int j = context.chunkPos().getStartZ() + lv.nextInt(16);
        int k = context.chunkGenerator().getSeaLevel();
        HeightContext lv2 = new HeightContext(context.chunkGenerator(), context.world());
        int l = this.height.get(lv, lv2);
        VerticalBlockSample lv3 = context.chunkGenerator().getColumnSample(i, j, context.world(), context.noiseConfig());
        BlockPos.Mutable lv4 = new BlockPos.Mutable(i, l, j);
        while (l > k) {
            BlockState lv5 = lv3.getState(l);
            BlockState lv6 = lv3.getState(--l);
            if (!lv5.isAir() || !lv6.isOf(Blocks.SOUL_SAND) && !lv6.isSideSolidFullSquare(EmptyBlockView.INSTANCE, lv4.setY(l), Direction.UP)) continue;
            break;
        }
        if (l <= k) {
            return Optional.empty();
        }
        BlockPos lv7 = new BlockPos(i, l, j);
        return Optional.of(new Structure.StructurePosition(lv7, holder -> NetherFossilGenerator.addPieces(context.structureTemplateManager(), holder, lv, lv7)));
    }

    @Override
    public StructureType<?> getType() {
        return StructureType.NETHER_FOSSIL;
    }
}

