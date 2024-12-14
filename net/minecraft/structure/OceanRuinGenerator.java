/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.structure;

import com.google.common.collect.Lists;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.ChestBlock;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.ChestBlockEntity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.SpawnReason;
import net.minecraft.entity.mob.DrownedEntity;
import net.minecraft.fluid.FluidState;
import net.minecraft.loot.LootTable;
import net.minecraft.loot.LootTables;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.tag.BlockTags;
import net.minecraft.registry.tag.FluidTags;
import net.minecraft.structure.SimpleStructurePiece;
import net.minecraft.structure.StructureContext;
import net.minecraft.structure.StructurePieceType;
import net.minecraft.structure.StructurePiecesHolder;
import net.minecraft.structure.StructurePlacementData;
import net.minecraft.structure.StructureTemplate;
import net.minecraft.structure.StructureTemplateManager;
import net.minecraft.structure.processor.BlockIgnoreStructureProcessor;
import net.minecraft.structure.processor.BlockRotStructureProcessor;
import net.minecraft.structure.processor.CappedStructureProcessor;
import net.minecraft.structure.processor.RuleStructureProcessor;
import net.minecraft.structure.processor.StructureProcessor;
import net.minecraft.structure.processor.StructureProcessorRule;
import net.minecraft.structure.rule.AlwaysTruePosRuleTest;
import net.minecraft.structure.rule.AlwaysTrueRuleTest;
import net.minecraft.structure.rule.BlockMatchRuleTest;
import net.minecraft.structure.rule.blockentity.AppendLootRuleBlockEntityModifier;
import net.minecraft.util.BlockMirror;
import net.minecraft.util.BlockRotation;
import net.minecraft.util.Identifier;
import net.minecraft.util.Util;
import net.minecraft.util.math.BlockBox;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.intprovider.ConstantIntProvider;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.BlockView;
import net.minecraft.world.Heightmap;
import net.minecraft.world.ServerWorldAccess;
import net.minecraft.world.StructureWorldAccess;
import net.minecraft.world.gen.StructureAccessor;
import net.minecraft.world.gen.chunk.ChunkGenerator;
import net.minecraft.world.gen.structure.OceanRuinStructure;

public class OceanRuinGenerator {
    static final StructureProcessor SUSPICIOUS_SAND_PROCESSOR = OceanRuinGenerator.createArchaeologyStructureProcessor(Blocks.SAND, Blocks.SUSPICIOUS_SAND, LootTables.OCEAN_RUIN_WARM_ARCHAEOLOGY);
    static final StructureProcessor SUSPICIOUS_GRAVEL_PROCESSOR = OceanRuinGenerator.createArchaeologyStructureProcessor(Blocks.GRAVEL, Blocks.SUSPICIOUS_GRAVEL, LootTables.OCEAN_RUIN_COLD_ARCHAEOLOGY);
    private static final Identifier[] WARM_RUINS = new Identifier[]{Identifier.ofVanilla("underwater_ruin/warm_1"), Identifier.ofVanilla("underwater_ruin/warm_2"), Identifier.ofVanilla("underwater_ruin/warm_3"), Identifier.ofVanilla("underwater_ruin/warm_4"), Identifier.ofVanilla("underwater_ruin/warm_5"), Identifier.ofVanilla("underwater_ruin/warm_6"), Identifier.ofVanilla("underwater_ruin/warm_7"), Identifier.ofVanilla("underwater_ruin/warm_8")};
    private static final Identifier[] BRICK_RUINS = new Identifier[]{Identifier.ofVanilla("underwater_ruin/brick_1"), Identifier.ofVanilla("underwater_ruin/brick_2"), Identifier.ofVanilla("underwater_ruin/brick_3"), Identifier.ofVanilla("underwater_ruin/brick_4"), Identifier.ofVanilla("underwater_ruin/brick_5"), Identifier.ofVanilla("underwater_ruin/brick_6"), Identifier.ofVanilla("underwater_ruin/brick_7"), Identifier.ofVanilla("underwater_ruin/brick_8")};
    private static final Identifier[] CRACKED_RUINS = new Identifier[]{Identifier.ofVanilla("underwater_ruin/cracked_1"), Identifier.ofVanilla("underwater_ruin/cracked_2"), Identifier.ofVanilla("underwater_ruin/cracked_3"), Identifier.ofVanilla("underwater_ruin/cracked_4"), Identifier.ofVanilla("underwater_ruin/cracked_5"), Identifier.ofVanilla("underwater_ruin/cracked_6"), Identifier.ofVanilla("underwater_ruin/cracked_7"), Identifier.ofVanilla("underwater_ruin/cracked_8")};
    private static final Identifier[] MOSSY_RUINS = new Identifier[]{Identifier.ofVanilla("underwater_ruin/mossy_1"), Identifier.ofVanilla("underwater_ruin/mossy_2"), Identifier.ofVanilla("underwater_ruin/mossy_3"), Identifier.ofVanilla("underwater_ruin/mossy_4"), Identifier.ofVanilla("underwater_ruin/mossy_5"), Identifier.ofVanilla("underwater_ruin/mossy_6"), Identifier.ofVanilla("underwater_ruin/mossy_7"), Identifier.ofVanilla("underwater_ruin/mossy_8")};
    private static final Identifier[] BIG_BRICK_RUINS = new Identifier[]{Identifier.ofVanilla("underwater_ruin/big_brick_1"), Identifier.ofVanilla("underwater_ruin/big_brick_2"), Identifier.ofVanilla("underwater_ruin/big_brick_3"), Identifier.ofVanilla("underwater_ruin/big_brick_8")};
    private static final Identifier[] BIG_MOSSY_RUINS = new Identifier[]{Identifier.ofVanilla("underwater_ruin/big_mossy_1"), Identifier.ofVanilla("underwater_ruin/big_mossy_2"), Identifier.ofVanilla("underwater_ruin/big_mossy_3"), Identifier.ofVanilla("underwater_ruin/big_mossy_8")};
    private static final Identifier[] BIG_CRACKED_RUINS = new Identifier[]{Identifier.ofVanilla("underwater_ruin/big_cracked_1"), Identifier.ofVanilla("underwater_ruin/big_cracked_2"), Identifier.ofVanilla("underwater_ruin/big_cracked_3"), Identifier.ofVanilla("underwater_ruin/big_cracked_8")};
    private static final Identifier[] BIG_WARM_RUINS = new Identifier[]{Identifier.ofVanilla("underwater_ruin/big_warm_4"), Identifier.ofVanilla("underwater_ruin/big_warm_5"), Identifier.ofVanilla("underwater_ruin/big_warm_6"), Identifier.ofVanilla("underwater_ruin/big_warm_7")};

    private static StructureProcessor createArchaeologyStructureProcessor(Block baseBlock, Block suspiciousBlock, RegistryKey<LootTable> lootTable) {
        return new CappedStructureProcessor(new RuleStructureProcessor(List.of(new StructureProcessorRule(new BlockMatchRuleTest(baseBlock), AlwaysTrueRuleTest.INSTANCE, AlwaysTruePosRuleTest.INSTANCE, suspiciousBlock.getDefaultState(), new AppendLootRuleBlockEntityModifier(lootTable)))), ConstantIntProvider.create(5));
    }

    private static Identifier getRandomWarmRuin(Random random) {
        return Util.getRandom(WARM_RUINS, random);
    }

    private static Identifier getRandomBigWarmRuin(Random random) {
        return Util.getRandom(BIG_WARM_RUINS, random);
    }

    public static void addPieces(StructureTemplateManager manager, BlockPos pos, BlockRotation rotation, StructurePiecesHolder holder, Random random, OceanRuinStructure structure) {
        boolean bl = random.nextFloat() <= structure.largeProbability;
        float f = bl ? 0.9f : 0.8f;
        OceanRuinGenerator.addPieces(manager, pos, rotation, holder, random, structure, bl, f);
        if (bl && random.nextFloat() <= structure.clusterProbability) {
            OceanRuinGenerator.addCluster(manager, random, rotation, pos, structure, holder);
        }
    }

    private static void addCluster(StructureTemplateManager manager, Random random, BlockRotation rotation, BlockPos pos, OceanRuinStructure structure, StructurePiecesHolder pieces) {
        BlockPos lv = new BlockPos(pos.getX(), 90, pos.getZ());
        BlockPos lv2 = StructureTemplate.transformAround(new BlockPos(15, 0, 15), BlockMirror.NONE, rotation, BlockPos.ORIGIN).add(lv);
        BlockBox lv3 = BlockBox.create(lv, lv2);
        BlockPos lv4 = new BlockPos(Math.min(lv.getX(), lv2.getX()), lv.getY(), Math.min(lv.getZ(), lv2.getZ()));
        List<BlockPos> list = OceanRuinGenerator.getRoomPositions(random, lv4);
        int i = MathHelper.nextInt(random, 4, 8);
        for (int j = 0; j < i; ++j) {
            BlockRotation lv6;
            BlockPos lv7;
            int k;
            BlockPos lv5;
            BlockBox lv8;
            if (list.isEmpty() || (lv8 = BlockBox.create(lv5 = list.remove(k = random.nextInt(list.size())), lv7 = StructureTemplate.transformAround(new BlockPos(5, 0, 6), BlockMirror.NONE, lv6 = BlockRotation.random(random), BlockPos.ORIGIN).add(lv5))).intersects(lv3)) continue;
            OceanRuinGenerator.addPieces(manager, lv5, lv6, pieces, random, structure, false, 0.8f);
        }
    }

    private static List<BlockPos> getRoomPositions(Random random, BlockPos pos) {
        ArrayList<BlockPos> list = Lists.newArrayList();
        list.add(pos.add(-16 + MathHelper.nextInt(random, 1, 8), 0, 16 + MathHelper.nextInt(random, 1, 7)));
        list.add(pos.add(-16 + MathHelper.nextInt(random, 1, 8), 0, MathHelper.nextInt(random, 1, 7)));
        list.add(pos.add(-16 + MathHelper.nextInt(random, 1, 8), 0, -16 + MathHelper.nextInt(random, 4, 8)));
        list.add(pos.add(MathHelper.nextInt(random, 1, 7), 0, 16 + MathHelper.nextInt(random, 1, 7)));
        list.add(pos.add(MathHelper.nextInt(random, 1, 7), 0, -16 + MathHelper.nextInt(random, 4, 6)));
        list.add(pos.add(16 + MathHelper.nextInt(random, 1, 7), 0, 16 + MathHelper.nextInt(random, 3, 8)));
        list.add(pos.add(16 + MathHelper.nextInt(random, 1, 7), 0, MathHelper.nextInt(random, 1, 7)));
        list.add(pos.add(16 + MathHelper.nextInt(random, 1, 7), 0, -16 + MathHelper.nextInt(random, 4, 8)));
        return list;
    }

    private static void addPieces(StructureTemplateManager manager, BlockPos pos, BlockRotation rotation, StructurePiecesHolder holder, Random random, OceanRuinStructure structure, boolean large, float integrity) {
        switch (structure.biomeTemperature) {
            default: {
                Identifier lv = large ? OceanRuinGenerator.getRandomBigWarmRuin(random) : OceanRuinGenerator.getRandomWarmRuin(random);
                holder.addPiece(new Piece(manager, lv, pos, rotation, integrity, structure.biomeTemperature, large));
                break;
            }
            case COLD: {
                Identifier[] lvs = large ? BIG_BRICK_RUINS : BRICK_RUINS;
                Identifier[] lvs2 = large ? BIG_CRACKED_RUINS : CRACKED_RUINS;
                Identifier[] lvs3 = large ? BIG_MOSSY_RUINS : MOSSY_RUINS;
                int i = random.nextInt(lvs.length);
                holder.addPiece(new Piece(manager, lvs[i], pos, rotation, integrity, structure.biomeTemperature, large));
                holder.addPiece(new Piece(manager, lvs2[i], pos, rotation, 0.7f, structure.biomeTemperature, large));
                holder.addPiece(new Piece(manager, lvs3[i], pos, rotation, 0.5f, structure.biomeTemperature, large));
            }
        }
    }

    public static class Piece
    extends SimpleStructurePiece {
        private final OceanRuinStructure.BiomeTemperature biomeType;
        private final float integrity;
        private final boolean large;

        public Piece(StructureTemplateManager structureTemplateManager, Identifier template, BlockPos pos, BlockRotation rotation, float integrity, OceanRuinStructure.BiomeTemperature biomeType, boolean large) {
            super(StructurePieceType.OCEAN_TEMPLE, 0, structureTemplateManager, template, template.toString(), Piece.createPlacementData(rotation, integrity, biomeType), pos);
            this.integrity = integrity;
            this.biomeType = biomeType;
            this.large = large;
        }

        private Piece(StructureTemplateManager holder, NbtCompound nbt, BlockRotation rotation, float integrity, OceanRuinStructure.BiomeTemperature biomeType, boolean large) {
            super(StructurePieceType.OCEAN_TEMPLE, nbt, holder, arg3 -> Piece.createPlacementData(rotation, integrity, biomeType));
            this.integrity = integrity;
            this.biomeType = biomeType;
            this.large = large;
        }

        private static StructurePlacementData createPlacementData(BlockRotation rotation, float integrity, OceanRuinStructure.BiomeTemperature temperature) {
            StructureProcessor lv = temperature == OceanRuinStructure.BiomeTemperature.COLD ? SUSPICIOUS_GRAVEL_PROCESSOR : SUSPICIOUS_SAND_PROCESSOR;
            return new StructurePlacementData().setRotation(rotation).setMirror(BlockMirror.NONE).addProcessor(new BlockRotStructureProcessor(integrity)).addProcessor(BlockIgnoreStructureProcessor.IGNORE_AIR_AND_STRUCTURE_BLOCKS).addProcessor(lv);
        }

        public static Piece fromNbt(StructureTemplateManager structureTemplateManager, NbtCompound nbt) {
            BlockRotation lv = BlockRotation.valueOf(nbt.getString("Rot"));
            float f = nbt.getFloat("Integrity");
            OceanRuinStructure.BiomeTemperature lv2 = OceanRuinStructure.BiomeTemperature.valueOf(nbt.getString("BiomeType"));
            boolean bl = nbt.getBoolean("IsLarge");
            return new Piece(structureTemplateManager, nbt, lv, f, lv2, bl);
        }

        @Override
        protected void writeNbt(StructureContext context, NbtCompound nbt) {
            super.writeNbt(context, nbt);
            nbt.putString("Rot", this.placementData.getRotation().name());
            nbt.putFloat("Integrity", this.integrity);
            nbt.putString("BiomeType", this.biomeType.toString());
            nbt.putBoolean("IsLarge", this.large);
        }

        @Override
        protected void handleMetadata(String metadata, BlockPos pos, ServerWorldAccess world, Random random, BlockBox boundingBox) {
            DrownedEntity lv2;
            if ("chest".equals(metadata)) {
                world.setBlockState(pos, (BlockState)Blocks.CHEST.getDefaultState().with(ChestBlock.WATERLOGGED, world.getFluidState(pos).isIn(FluidTags.WATER)), Block.NOTIFY_LISTENERS);
                BlockEntity lv = world.getBlockEntity(pos);
                if (lv instanceof ChestBlockEntity) {
                    ((ChestBlockEntity)lv).setLootTable(this.large ? LootTables.UNDERWATER_RUIN_BIG_CHEST : LootTables.UNDERWATER_RUIN_SMALL_CHEST, random.nextLong());
                }
            } else if ("drowned".equals(metadata) && (lv2 = EntityType.DROWNED.create(world.toServerWorld())) != null) {
                lv2.setPersistent();
                lv2.refreshPositionAndAngles(pos, 0.0f, 0.0f);
                lv2.initialize(world, world.getLocalDifficulty(pos), SpawnReason.STRUCTURE, null);
                world.spawnEntityAndPassengers(lv2);
                if (pos.getY() > world.getSeaLevel()) {
                    world.setBlockState(pos, Blocks.AIR.getDefaultState(), Block.NOTIFY_LISTENERS);
                } else {
                    world.setBlockState(pos, Blocks.WATER.getDefaultState(), Block.NOTIFY_LISTENERS);
                }
            }
        }

        @Override
        public void generate(StructureWorldAccess world, StructureAccessor structureAccessor, ChunkGenerator chunkGenerator, Random random, BlockBox chunkBox, ChunkPos chunkPos, BlockPos pivot) {
            int i = world.getTopY(Heightmap.Type.OCEAN_FLOOR_WG, this.pos.getX(), this.pos.getZ());
            this.pos = new BlockPos(this.pos.getX(), i, this.pos.getZ());
            BlockPos lv = StructureTemplate.transformAround(new BlockPos(this.template.getSize().getX() - 1, 0, this.template.getSize().getZ() - 1), BlockMirror.NONE, this.placementData.getRotation(), BlockPos.ORIGIN).add(this.pos);
            this.pos = new BlockPos(this.pos.getX(), this.getGenerationY(this.pos, world, lv), this.pos.getZ());
            super.generate(world, structureAccessor, chunkGenerator, random, chunkBox, chunkPos, pivot);
        }

        private int getGenerationY(BlockPos start, BlockView world, BlockPos end) {
            int i = start.getY();
            int j = 512;
            int k = i - 1;
            int l = 0;
            for (BlockPos lv : BlockPos.iterate(start, end)) {
                int m = lv.getX();
                int n = lv.getZ();
                int o = start.getY() - 1;
                BlockPos.Mutable lv2 = new BlockPos.Mutable(m, o, n);
                BlockState lv3 = world.getBlockState(lv2);
                FluidState lv4 = world.getFluidState(lv2);
                while ((lv3.isAir() || lv4.isIn(FluidTags.WATER) || lv3.isIn(BlockTags.ICE)) && o > world.getBottomY() + 1) {
                    lv2.set(m, --o, n);
                    lv3 = world.getBlockState(lv2);
                    lv4 = world.getFluidState(lv2);
                }
                j = Math.min(j, o);
                if (o >= k - 2) continue;
                ++l;
            }
            int p = Math.abs(start.getX() - end.getX());
            if (k - j > 2 && l > p - 2) {
                i = j + 1;
            }
            return i;
        }
    }
}

