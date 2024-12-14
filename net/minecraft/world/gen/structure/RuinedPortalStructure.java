/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.world.gen.structure;

import com.google.common.collect.ImmutableList;
import com.mojang.datafixers.kinds.Applicative;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import net.minecraft.block.BlockState;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.structure.RuinedPortalStructurePiece;
import net.minecraft.structure.StructureTemplate;
import net.minecraft.util.BlockMirror;
import net.minecraft.util.BlockRotation;
import net.minecraft.util.Identifier;
import net.minecraft.util.Util;
import net.minecraft.util.dynamic.Codecs;
import net.minecraft.util.math.BlockBox;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.random.ChunkRandom;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.HeightLimitView;
import net.minecraft.world.Heightmap;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.biome.source.BiomeCoords;
import net.minecraft.world.gen.chunk.ChunkGenerator;
import net.minecraft.world.gen.chunk.VerticalBlockSample;
import net.minecraft.world.gen.noise.NoiseConfig;
import net.minecraft.world.gen.structure.Structure;
import net.minecraft.world.gen.structure.StructureType;

public class RuinedPortalStructure
extends Structure {
    private static final String[] COMMON_PORTAL_STRUCTURE_IDS = new String[]{"ruined_portal/portal_1", "ruined_portal/portal_2", "ruined_portal/portal_3", "ruined_portal/portal_4", "ruined_portal/portal_5", "ruined_portal/portal_6", "ruined_portal/portal_7", "ruined_portal/portal_8", "ruined_portal/portal_9", "ruined_portal/portal_10"};
    private static final String[] RARE_PORTAL_STRUCTURE_IDS = new String[]{"ruined_portal/giant_portal_1", "ruined_portal/giant_portal_2", "ruined_portal/giant_portal_3"};
    private static final float RARE_PORTAL_CHANCE = 0.05f;
    private static final int MIN_BLOCKS_ABOVE_WORLD_BOTTOM = 15;
    private final List<Setup> setups;
    public static final MapCodec<RuinedPortalStructure> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(RuinedPortalStructure.configCodecBuilder(instance), ((MapCodec)Codecs.nonEmptyList(Setup.CODEC.listOf()).fieldOf("setups")).forGetter(structure -> structure.setups)).apply((Applicative<RuinedPortalStructure, ?>)instance, RuinedPortalStructure::new));

    public RuinedPortalStructure(Structure.Config config, List<Setup> setups) {
        super(config);
        this.setups = setups;
    }

    public RuinedPortalStructure(Structure.Config config, Setup setup) {
        this(config, List.of(setup));
    }

    @Override
    public Optional<Structure.StructurePosition> getStructurePosition(Structure.Context context) {
        RuinedPortalStructurePiece.Properties lv = new RuinedPortalStructurePiece.Properties();
        ChunkRandom lv2 = context.random();
        Setup lv3 = null;
        if (this.setups.size() > 1) {
            float f = 0.0f;
            for (Setup setup : this.setups) {
                f += setup.weight();
            }
            float g = lv2.nextFloat();
            for (Setup lv5 : this.setups) {
                if (!((g -= lv5.weight() / f) < 0.0f)) continue;
                lv3 = lv5;
                break;
            }
        } else {
            lv3 = this.setups.get(0);
        }
        if (lv3 == null) {
            throw new IllegalStateException();
        }
        Setup lv6 = lv3;
        lv.airPocket = RuinedPortalStructure.shouldPlaceAirPocket(lv2, lv6.airPocketProbability());
        lv.mossiness = lv6.mossiness();
        lv.overgrown = lv6.overgrown();
        lv.vines = lv6.vines();
        lv.replaceWithBlackstone = lv6.replaceWithBlackstone();
        Identifier lv7 = lv2.nextFloat() < 0.05f ? Identifier.ofVanilla(RARE_PORTAL_STRUCTURE_IDS[lv2.nextInt(RARE_PORTAL_STRUCTURE_IDS.length)]) : Identifier.ofVanilla(COMMON_PORTAL_STRUCTURE_IDS[lv2.nextInt(COMMON_PORTAL_STRUCTURE_IDS.length)]);
        StructureTemplate structureTemplate = context.structureTemplateManager().getTemplateOrBlank(lv7);
        BlockRotation lv9 = Util.getRandom(BlockRotation.values(), (Random)lv2);
        BlockMirror lv10 = lv2.nextFloat() < 0.5f ? BlockMirror.NONE : BlockMirror.FRONT_BACK;
        BlockPos lv11 = new BlockPos(structureTemplate.getSize().getX() / 2, 0, structureTemplate.getSize().getZ() / 2);
        ChunkGenerator lv12 = context.chunkGenerator();
        HeightLimitView lv13 = context.world();
        NoiseConfig lv14 = context.noiseConfig();
        BlockPos lv15 = context.chunkPos().getStartPos();
        BlockBox lv16 = structureTemplate.calculateBoundingBox(lv15, lv9, lv11, lv10);
        BlockPos lv17 = lv16.getCenter();
        int i = lv12.getHeight(lv17.getX(), lv17.getZ(), RuinedPortalStructurePiece.getHeightmapType(lv6.placement()), lv13, lv14) - 1;
        int j = RuinedPortalStructure.getFloorHeight(lv2, lv12, lv6.placement(), lv.airPocket, i, lv16.getBlockCountY(), lv16, lv13, lv14);
        BlockPos lv18 = new BlockPos(lv15.getX(), j, lv15.getZ());
        return Optional.of(new Structure.StructurePosition(lv18, collector -> {
            if (lv6.canBeCold()) {
                arg2.cold = RuinedPortalStructure.isColdAt(lv18, context.chunkGenerator().getBiomeSource().getBiome(BiomeCoords.fromBlock(lv18.getX()), BiomeCoords.fromBlock(lv18.getY()), BiomeCoords.fromBlock(lv18.getZ()), lv14.getMultiNoiseSampler()));
            }
            collector.addPiece(new RuinedPortalStructurePiece(context.structureTemplateManager(), lv18, lv6.placement(), lv, lv7, lv8, lv9, lv10, lv11));
        }));
    }

    private static boolean shouldPlaceAirPocket(ChunkRandom random, float probability) {
        if (probability == 0.0f) {
            return false;
        }
        if (probability == 1.0f) {
            return true;
        }
        return random.nextFloat() < probability;
    }

    private static boolean isColdAt(BlockPos pos, RegistryEntry<Biome> biome) {
        return biome.value().isCold(pos);
    }

    private static int getFloorHeight(Random random, ChunkGenerator chunkGenerator, RuinedPortalStructurePiece.VerticalPlacement verticalPlacement, boolean airPocket, int height, int blockCountY, BlockBox box, HeightLimitView world, NoiseConfig noiseConfig) {
        int n;
        int k = world.getBottomY() + 15;
        if (verticalPlacement == RuinedPortalStructurePiece.VerticalPlacement.IN_NETHER) {
            l = airPocket ? MathHelper.nextBetween(random, 32, 100) : (random.nextFloat() < 0.5f ? MathHelper.nextBetween(random, 27, 29) : MathHelper.nextBetween(random, 29, 100));
        } else if (verticalPlacement == RuinedPortalStructurePiece.VerticalPlacement.IN_MOUNTAIN) {
            m = height - blockCountY;
            l = RuinedPortalStructure.choosePlacementHeight(random, 70, m);
        } else if (verticalPlacement == RuinedPortalStructurePiece.VerticalPlacement.UNDERGROUND) {
            m = height - blockCountY;
            l = RuinedPortalStructure.choosePlacementHeight(random, k, m);
        } else {
            l = verticalPlacement == RuinedPortalStructurePiece.VerticalPlacement.PARTLY_BURIED ? height - blockCountY + MathHelper.nextBetween(random, 2, 8) : height;
        }
        ImmutableList<BlockPos> list = ImmutableList.of(new BlockPos(box.getMinX(), 0, box.getMinZ()), new BlockPos(box.getMaxX(), 0, box.getMinZ()), new BlockPos(box.getMinX(), 0, box.getMaxZ()), new BlockPos(box.getMaxX(), 0, box.getMaxZ()));
        List list2 = list.stream().map(pos -> chunkGenerator.getColumnSample(pos.getX(), pos.getZ(), world, noiseConfig)).collect(Collectors.toList());
        Heightmap.Type lv = verticalPlacement == RuinedPortalStructurePiece.VerticalPlacement.ON_OCEAN_FLOOR ? Heightmap.Type.OCEAN_FLOOR_WG : Heightmap.Type.WORLD_SURFACE_WG;
        block0: for (n = l; n > k; --n) {
            int o = 0;
            for (VerticalBlockSample lv2 : list2) {
                BlockState lv3 = lv2.getState(n);
                if (!lv.getBlockPredicate().test(lv3) || ++o != 3) continue;
                break block0;
            }
        }
        return n;
    }

    private static int choosePlacementHeight(Random random, int min, int max) {
        if (min < max) {
            return MathHelper.nextBetween(random, min, max);
        }
        return max;
    }

    @Override
    public StructureType<?> getType() {
        return StructureType.RUINED_PORTAL;
    }

    public record Setup(RuinedPortalStructurePiece.VerticalPlacement placement, float airPocketProbability, float mossiness, boolean overgrown, boolean vines, boolean canBeCold, boolean replaceWithBlackstone, float weight) {
        public static final Codec<Setup> CODEC = RecordCodecBuilder.create(instance -> instance.group(((MapCodec)RuinedPortalStructurePiece.VerticalPlacement.CODEC.fieldOf("placement")).forGetter(Setup::placement), ((MapCodec)Codec.floatRange(0.0f, 1.0f).fieldOf("air_pocket_probability")).forGetter(Setup::airPocketProbability), ((MapCodec)Codec.floatRange(0.0f, 1.0f).fieldOf("mossiness")).forGetter(Setup::mossiness), ((MapCodec)Codec.BOOL.fieldOf("overgrown")).forGetter(Setup::overgrown), ((MapCodec)Codec.BOOL.fieldOf("vines")).forGetter(Setup::vines), ((MapCodec)Codec.BOOL.fieldOf("can_be_cold")).forGetter(Setup::canBeCold), ((MapCodec)Codec.BOOL.fieldOf("replace_with_blackstone")).forGetter(Setup::replaceWithBlackstone), ((MapCodec)Codecs.POSITIVE_FLOAT.fieldOf("weight")).forGetter(Setup::weight)).apply((Applicative<Setup, ?>)instance, Setup::new));
    }
}

