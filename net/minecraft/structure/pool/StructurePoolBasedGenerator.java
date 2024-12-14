/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.structure.pool;

import com.google.common.collect.Lists;
import com.mojang.logging.LogUtils;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import net.minecraft.block.JigsawBlock;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.registry.DynamicRegistryManager;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.structure.JigsawJunction;
import net.minecraft.structure.PoolStructurePiece;
import net.minecraft.structure.StructureLiquidSettings;
import net.minecraft.structure.StructurePiece;
import net.minecraft.structure.StructurePiecesCollector;
import net.minecraft.structure.StructureTemplate;
import net.minecraft.structure.StructureTemplateManager;
import net.minecraft.structure.pool.EmptyPoolElement;
import net.minecraft.structure.pool.StructurePool;
import net.minecraft.structure.pool.StructurePoolElement;
import net.minecraft.structure.pool.StructurePools;
import net.minecraft.structure.pool.alias.StructurePoolAliasLookup;
import net.minecraft.util.BlockRotation;
import net.minecraft.util.Identifier;
import net.minecraft.util.collection.PriorityIterator;
import net.minecraft.util.function.BooleanBiFunction;
import net.minecraft.util.math.BlockBox;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.random.ChunkRandom;
import net.minecraft.util.math.random.Random;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.util.shape.VoxelShapes;
import net.minecraft.world.HeightLimitView;
import net.minecraft.world.Heightmap;
import net.minecraft.world.StructureWorldAccess;
import net.minecraft.world.gen.StructureAccessor;
import net.minecraft.world.gen.chunk.ChunkGenerator;
import net.minecraft.world.gen.noise.NoiseConfig;
import net.minecraft.world.gen.structure.DimensionPadding;
import net.minecraft.world.gen.structure.JigsawStructure;
import net.minecraft.world.gen.structure.Structure;
import org.apache.commons.lang3.mutable.MutableObject;
import org.slf4j.Logger;

public class StructurePoolBasedGenerator {
    static final Logger LOGGER = LogUtils.getLogger();

    public static Optional<Structure.StructurePosition> generate(Structure.Context context, RegistryEntry<StructurePool> structurePool, Optional<Identifier> id, int size, BlockPos pos, boolean useExpansionHack, Optional<Heightmap.Type> projectStartToHeightmap, int maxDistanceFromCenter, StructurePoolAliasLookup aliasLookup, DimensionPadding dimensionPadding, StructureLiquidSettings liquidSettings) {
        BlockPos lv11;
        DynamicRegistryManager lv = context.dynamicRegistryManager();
        ChunkGenerator lv2 = context.chunkGenerator();
        StructureTemplateManager lv3 = context.structureTemplateManager();
        HeightLimitView lv4 = context.world();
        ChunkRandom lv5 = context.random();
        Registry<StructurePool> lv6 = lv.get(RegistryKeys.TEMPLATE_POOL);
        BlockRotation lv7 = BlockRotation.random(lv5);
        StructurePool lv8 = structurePool.getKey().flatMap(key -> lv6.getOrEmpty(aliasLookup.lookup((RegistryKey<StructurePool>)key))).orElse(structurePool.value());
        StructurePoolElement lv9 = lv8.getRandomElement(lv5);
        if (lv9 == EmptyPoolElement.INSTANCE) {
            return Optional.empty();
        }
        if (id.isPresent()) {
            Identifier lv10 = id.get();
            Optional<BlockPos> optional3 = StructurePoolBasedGenerator.findStartingJigsawPos(lv9, lv10, pos, lv7, lv3, lv5);
            if (optional3.isEmpty()) {
                LOGGER.error("No starting jigsaw {} found in start pool {}", (Object)lv10, (Object)structurePool.getKey().map(key -> key.getValue().toString()).orElse("<unregistered>"));
                return Optional.empty();
            }
            lv11 = optional3.get();
        } else {
            lv11 = pos;
        }
        BlockPos lv12 = lv11.subtract(pos);
        BlockPos lv13 = pos.subtract(lv12);
        PoolStructurePiece lv14 = new PoolStructurePiece(lv3, lv9, lv13, lv9.getGroundLevelDelta(), lv7, lv9.getBoundingBox(lv3, lv13, lv7), liquidSettings);
        BlockBox lv15 = lv14.getBoundingBox();
        int k = (lv15.getMaxX() + lv15.getMinX()) / 2;
        int l = (lv15.getMaxZ() + lv15.getMinZ()) / 2;
        int m = projectStartToHeightmap.isPresent() ? pos.getY() + lv2.getHeightOnGround(k, l, projectStartToHeightmap.get(), lv4, context.noiseConfig()) : lv13.getY();
        int n = lv15.getMinY() + lv14.getGroundLevelDelta();
        lv14.translate(0, m - n, 0);
        int o = m + lv12.getY();
        return Optional.of(new Structure.StructurePosition(new BlockPos(k, o, l), collector -> {
            ArrayList<PoolStructurePiece> list = Lists.newArrayList();
            list.add(lv14);
            if (size <= 0) {
                return;
            }
            Box lv = new Box(k - maxDistanceFromCenter, Math.max(o - maxDistanceFromCenter, lv4.getBottomY() + dimensionPadding.bottom()), l - maxDistanceFromCenter, k + maxDistanceFromCenter + 1, Math.min(o + maxDistanceFromCenter + 1, lv4.getTopY() - dimensionPadding.top()), l + maxDistanceFromCenter + 1);
            VoxelShape lv2 = VoxelShapes.combineAndSimplify(VoxelShapes.cuboid(lv), VoxelShapes.cuboid(Box.from(lv15)), BooleanBiFunction.ONLY_FIRST);
            StructurePoolBasedGenerator.generate(context.noiseConfig(), size, useExpansionHack, lv2, lv3, lv4, lv5, lv6, lv14, list, lv2, aliasLookup, liquidSettings);
            list.forEach(collector::addPiece);
        }));
    }

    private static Optional<BlockPos> findStartingJigsawPos(StructurePoolElement pool, Identifier id, BlockPos pos, BlockRotation rotation, StructureTemplateManager structureManager, ChunkRandom random) {
        List<StructureTemplate.StructureBlockInfo> list = pool.getStructureBlockInfos(structureManager, pos, rotation, random);
        Optional<BlockPos> optional = Optional.empty();
        for (StructureTemplate.StructureBlockInfo lv : list) {
            Identifier lv2 = Identifier.tryParse(Objects.requireNonNull(lv.nbt(), () -> String.valueOf(lv) + " nbt was null").getString("name"));
            if (!id.equals(lv2)) continue;
            optional = Optional.of(lv.pos());
            break;
        }
        return optional;
    }

    private static void generate(NoiseConfig noiseConfig, int maxSize, boolean modifyBoundingBox, ChunkGenerator chunkGenerator, StructureTemplateManager structureTemplateManager, HeightLimitView heightLimitView, Random random, Registry<StructurePool> structurePoolRegistry, PoolStructurePiece firstPiece, List<PoolStructurePiece> pieces, VoxelShape pieceShape, StructurePoolAliasLookup aliasLookup, StructureLiquidSettings liquidSettings) {
        StructurePoolGenerator lv = new StructurePoolGenerator(structurePoolRegistry, maxSize, chunkGenerator, structureTemplateManager, pieces, random);
        lv.generatePiece(firstPiece, new MutableObject<VoxelShape>(pieceShape), 0, modifyBoundingBox, heightLimitView, noiseConfig, aliasLookup, liquidSettings);
        while (lv.structurePieces.hasNext()) {
            ShapedPoolStructurePiece lv2 = (ShapedPoolStructurePiece)lv.structurePieces.next();
            lv.generatePiece(lv2.piece, lv2.pieceShape, lv2.currentSize, modifyBoundingBox, heightLimitView, noiseConfig, aliasLookup, liquidSettings);
        }
    }

    public static boolean generate(ServerWorld world, RegistryEntry<StructurePool> structurePool, Identifier id, int size, BlockPos pos, boolean keepJigsaws) {
        ChunkGenerator lv = world.getChunkManager().getChunkGenerator();
        StructureTemplateManager lv2 = world.getStructureTemplateManager();
        StructureAccessor lv3 = world.getStructureAccessor();
        Random lv4 = world.getRandom();
        Structure.Context lv5 = new Structure.Context(world.getRegistryManager(), lv, lv.getBiomeSource(), world.getChunkManager().getNoiseConfig(), lv2, world.getSeed(), new ChunkPos(pos), world, biome -> true);
        Optional<Structure.StructurePosition> optional = StructurePoolBasedGenerator.generate(lv5, structurePool, Optional.of(id), size, pos, false, Optional.empty(), 128, StructurePoolAliasLookup.EMPTY, JigsawStructure.DEFAULT_DIMENSION_PADDING, JigsawStructure.DEFAULT_LIQUID_SETTINGS);
        if (optional.isPresent()) {
            StructurePiecesCollector lv6 = optional.get().generate();
            for (StructurePiece lv7 : lv6.toList().pieces()) {
                if (!(lv7 instanceof PoolStructurePiece)) continue;
                PoolStructurePiece lv8 = (PoolStructurePiece)lv7;
                lv8.generate((StructureWorldAccess)world, lv3, lv, lv4, BlockBox.infinite(), pos, keepJigsaws);
            }
            return true;
        }
        return false;
    }

    static final class StructurePoolGenerator {
        private final Registry<StructurePool> registry;
        private final int maxSize;
        private final ChunkGenerator chunkGenerator;
        private final StructureTemplateManager structureTemplateManager;
        private final List<? super PoolStructurePiece> children;
        private final Random random;
        final PriorityIterator<ShapedPoolStructurePiece> structurePieces = new PriorityIterator();

        StructurePoolGenerator(Registry<StructurePool> registry, int maxSize, ChunkGenerator chunkGenerator, StructureTemplateManager structureTemplateManager, List<? super PoolStructurePiece> children, Random random) {
            this.registry = registry;
            this.maxSize = maxSize;
            this.chunkGenerator = chunkGenerator;
            this.structureTemplateManager = structureTemplateManager;
            this.children = children;
            this.random = random;
        }

        void generatePiece(PoolStructurePiece piece, MutableObject<VoxelShape> pieceShape, int minY, boolean modifyBoundingBox, HeightLimitView world, NoiseConfig noiseConfig, StructurePoolAliasLookup aliasLookup, StructureLiquidSettings liquidSettings) {
            StructurePoolElement lv = piece.getPoolElement();
            BlockPos lv2 = piece.getPos();
            BlockRotation lv3 = piece.getRotation();
            StructurePool.Projection lv4 = lv.getProjection();
            boolean bl2 = lv4 == StructurePool.Projection.RIGID;
            MutableObject<VoxelShape> mutableObject2 = new MutableObject<VoxelShape>();
            BlockBox lv5 = piece.getBoundingBox();
            int j = lv5.getMinY();
            block0: for (StructureTemplate.StructureBlockInfo lv6 : lv.getStructureBlockInfos(this.structureTemplateManager, lv2, lv3, this.random)) {
                StructurePoolElement lv13;
                MutableObject<Object> mutableObject3;
                Direction lv7 = JigsawBlock.getFacing(lv6.state());
                BlockPos lv8 = lv6.pos();
                BlockPos lv9 = lv8.offset(lv7);
                int k = lv8.getY() - j;
                int l = -1;
                RegistryKey<StructurePool> lv10 = StructurePoolGenerator.lookupPool(lv6, aliasLookup);
                Optional<RegistryEntry.Reference<StructurePool>> optional = this.registry.getEntry(lv10);
                if (optional.isEmpty()) {
                    LOGGER.warn("Empty or non-existent pool: {}", (Object)lv10.getValue());
                    continue;
                }
                RegistryEntry lv11 = optional.get();
                if (((StructurePool)lv11.value()).getElementCount() == 0 && !lv11.matchesKey(StructurePools.EMPTY)) {
                    LOGGER.warn("Empty or non-existent pool: {}", (Object)lv10.getValue());
                    continue;
                }
                RegistryEntry<StructurePool> lv12 = ((StructurePool)lv11.value()).getFallback();
                if (lv12.value().getElementCount() == 0 && !lv12.matchesKey(StructurePools.EMPTY)) {
                    LOGGER.warn("Empty or non-existent fallback pool: {}", (Object)lv12.getKey().map(key -> key.getValue().toString()).orElse("<unregistered>"));
                    continue;
                }
                boolean bl3 = lv5.contains(lv9);
                if (bl3) {
                    mutableObject3 = mutableObject2;
                    if (mutableObject2.getValue() == null) {
                        mutableObject2.setValue(VoxelShapes.cuboid(Box.from(lv5)));
                    }
                } else {
                    mutableObject3 = pieceShape;
                }
                ArrayList<StructurePoolElement> list = Lists.newArrayList();
                if (minY != this.maxSize) {
                    list.addAll(((StructurePool)lv11.value()).getElementIndicesInRandomOrder(this.random));
                }
                list.addAll(lv12.value().getElementIndicesInRandomOrder(this.random));
                int m = lv6.nbt() != null ? lv6.nbt().getInt("placement_priority") : 0;
                Iterator iterator = list.iterator();
                while (iterator.hasNext() && (lv13 = (StructurePoolElement)iterator.next()) != EmptyPoolElement.INSTANCE) {
                    for (BlockRotation lv14 : BlockRotation.randomRotationOrder(this.random)) {
                        List<StructureTemplate.StructureBlockInfo> list2 = lv13.getStructureBlockInfos(this.structureTemplateManager, BlockPos.ORIGIN, lv14, this.random);
                        BlockBox lv15 = lv13.getBoundingBox(this.structureTemplateManager, BlockPos.ORIGIN, lv14);
                        int n = !modifyBoundingBox || lv15.getBlockCountY() > 16 ? 0 : list2.stream().mapToInt(structureBlockInfo -> {
                            if (!lv15.contains(structureBlockInfo.pos().offset(JigsawBlock.getFacing(structureBlockInfo.state())))) {
                                return 0;
                            }
                            RegistryKey<StructurePool> lv = StructurePoolGenerator.lookupPool(structureBlockInfo, aliasLookup);
                            Optional<RegistryEntry.Reference<StructurePool>> optional = this.registry.getEntry(lv);
                            Optional<RegistryEntry> optional2 = optional.map(entry -> ((StructurePool)entry.value()).getFallback());
                            int i = optional.map(entry -> ((StructurePool)entry.value()).getHighestY(this.structureTemplateManager)).orElse(0);
                            int j = optional2.map(entry -> ((StructurePool)entry.value()).getHighestY(this.structureTemplateManager)).orElse(0);
                            return Math.max(i, j);
                        }).max().orElse(0);
                        for (StructureTemplate.StructureBlockInfo lv16 : list2) {
                            int v;
                            int t;
                            int r;
                            if (!JigsawBlock.attachmentMatches(lv6, lv16)) continue;
                            BlockPos lv17 = lv16.pos();
                            BlockPos lv18 = lv9.subtract(lv17);
                            BlockBox lv19 = lv13.getBoundingBox(this.structureTemplateManager, lv18, lv14);
                            int o = lv19.getMinY();
                            StructurePool.Projection lv20 = lv13.getProjection();
                            boolean bl4 = lv20 == StructurePool.Projection.RIGID;
                            int p = lv17.getY();
                            int q = k - p + JigsawBlock.getFacing(lv6.state()).getOffsetY();
                            if (bl2 && bl4) {
                                r = j + q;
                            } else {
                                if (l == -1) {
                                    l = this.chunkGenerator.getHeightOnGround(lv8.getX(), lv8.getZ(), Heightmap.Type.WORLD_SURFACE_WG, world, noiseConfig);
                                }
                                r = l - p;
                            }
                            int s = r - o;
                            BlockBox lv21 = lv19.offset(0, s, 0);
                            BlockPos lv22 = lv18.add(0, s, 0);
                            if (n > 0) {
                                t = Math.max(n + 1, lv21.getMaxY() - lv21.getMinY());
                                lv21.encompass(new BlockPos(lv21.getMinX(), lv21.getMinY() + t, lv21.getMinZ()));
                            }
                            if (VoxelShapes.matchesAnywhere((VoxelShape)mutableObject3.getValue(), VoxelShapes.cuboid(Box.from(lv21).contract(0.25)), BooleanBiFunction.ONLY_SECOND)) continue;
                            mutableObject3.setValue(VoxelShapes.combine((VoxelShape)mutableObject3.getValue(), VoxelShapes.cuboid(Box.from(lv21)), BooleanBiFunction.ONLY_FIRST));
                            t = piece.getGroundLevelDelta();
                            int u = bl4 ? t - q : lv13.getGroundLevelDelta();
                            PoolStructurePiece lv23 = new PoolStructurePiece(this.structureTemplateManager, lv13, lv22, u, lv14, lv21, liquidSettings);
                            if (bl2) {
                                v = j + k;
                            } else if (bl4) {
                                v = r + p;
                            } else {
                                if (l == -1) {
                                    l = this.chunkGenerator.getHeightOnGround(lv8.getX(), lv8.getZ(), Heightmap.Type.WORLD_SURFACE_WG, world, noiseConfig);
                                }
                                v = l + q / 2;
                            }
                            piece.addJunction(new JigsawJunction(lv9.getX(), v - k + t, lv9.getZ(), q, lv20));
                            lv23.addJunction(new JigsawJunction(lv8.getX(), v - p + u, lv8.getZ(), -q, lv4));
                            this.children.add(lv23);
                            if (minY + 1 > this.maxSize) continue block0;
                            ShapedPoolStructurePiece lv24 = new ShapedPoolStructurePiece(lv23, mutableObject3, minY + 1);
                            this.structurePieces.enqueue(lv24, m);
                            continue block0;
                        }
                    }
                }
            }
        }

        private static RegistryKey<StructurePool> lookupPool(StructureTemplate.StructureBlockInfo structureBlockInfo, StructurePoolAliasLookup aliasLookup) {
            NbtCompound lv = Objects.requireNonNull(structureBlockInfo.nbt(), () -> String.valueOf(structureBlockInfo) + " nbt was null");
            RegistryKey<StructurePool> lv2 = StructurePools.of(lv.getString("pool"));
            return aliasLookup.lookup(lv2);
        }
    }

    record ShapedPoolStructurePiece(PoolStructurePiece piece, MutableObject<VoxelShape> pieceShape, int currentSize) {
    }
}

