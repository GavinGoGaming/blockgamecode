/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.structure;

import com.google.common.collect.Lists;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.decoration.ItemFrameEntity;
import net.minecraft.entity.mob.ShulkerEntity;
import net.minecraft.inventory.LootableInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.loot.LootTables;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.structure.SimpleStructurePiece;
import net.minecraft.structure.StructureContext;
import net.minecraft.structure.StructurePiece;
import net.minecraft.structure.StructurePieceType;
import net.minecraft.structure.StructurePlacementData;
import net.minecraft.structure.StructureTemplateManager;
import net.minecraft.structure.processor.BlockIgnoreStructureProcessor;
import net.minecraft.util.BlockRotation;
import net.minecraft.util.Identifier;
import net.minecraft.util.Pair;
import net.minecraft.util.math.BlockBox;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.ServerWorldAccess;
import net.minecraft.world.World;

public class EndCityGenerator {
    private static final int MAX_DEPTH = 8;
    static final Part BUILDING = new Part(){

        @Override
        public void init() {
        }

        @Override
        public boolean create(StructureTemplateManager manager, int depth, Piece root, BlockPos pos, List<StructurePiece> pieces, Random random) {
            if (depth > 8) {
                return false;
            }
            BlockRotation lv = root.getPlacementData().getRotation();
            Piece lv2 = EndCityGenerator.addPiece(pieces, EndCityGenerator.createPiece(manager, root, pos, "base_floor", lv, true));
            int j = random.nextInt(3);
            if (j == 0) {
                lv2 = EndCityGenerator.addPiece(pieces, EndCityGenerator.createPiece(manager, lv2, new BlockPos(-1, 4, -1), "base_roof", lv, true));
            } else if (j == 1) {
                lv2 = EndCityGenerator.addPiece(pieces, EndCityGenerator.createPiece(manager, lv2, new BlockPos(-1, 0, -1), "second_floor_2", lv, false));
                lv2 = EndCityGenerator.addPiece(pieces, EndCityGenerator.createPiece(manager, lv2, new BlockPos(-1, 8, -1), "second_roof", lv, false));
                EndCityGenerator.createPart(manager, SMALL_TOWER, depth + 1, lv2, null, pieces, random);
            } else if (j == 2) {
                lv2 = EndCityGenerator.addPiece(pieces, EndCityGenerator.createPiece(manager, lv2, new BlockPos(-1, 0, -1), "second_floor_2", lv, false));
                lv2 = EndCityGenerator.addPiece(pieces, EndCityGenerator.createPiece(manager, lv2, new BlockPos(-1, 4, -1), "third_floor_2", lv, false));
                lv2 = EndCityGenerator.addPiece(pieces, EndCityGenerator.createPiece(manager, lv2, new BlockPos(-1, 8, -1), "third_roof", lv, true));
                EndCityGenerator.createPart(manager, SMALL_TOWER, depth + 1, lv2, null, pieces, random);
            }
            return true;
        }
    };
    static final List<Pair<BlockRotation, BlockPos>> SMALL_TOWER_BRIDGE_ATTACHMENTS = Lists.newArrayList(new Pair<BlockRotation, BlockPos>(BlockRotation.NONE, new BlockPos(1, -1, 0)), new Pair<BlockRotation, BlockPos>(BlockRotation.CLOCKWISE_90, new BlockPos(6, -1, 1)), new Pair<BlockRotation, BlockPos>(BlockRotation.COUNTERCLOCKWISE_90, new BlockPos(0, -1, 5)), new Pair<BlockRotation, BlockPos>(BlockRotation.CLOCKWISE_180, new BlockPos(5, -1, 6)));
    static final Part SMALL_TOWER = new Part(){

        @Override
        public void init() {
        }

        @Override
        public boolean create(StructureTemplateManager manager, int depth, Piece root, BlockPos pos, List<StructurePiece> pieces, Random random) {
            BlockRotation lv = root.getPlacementData().getRotation();
            Piece lv2 = root;
            lv2 = EndCityGenerator.addPiece(pieces, EndCityGenerator.createPiece(manager, lv2, new BlockPos(3 + random.nextInt(2), -3, 3 + random.nextInt(2)), "tower_base", lv, true));
            lv2 = EndCityGenerator.addPiece(pieces, EndCityGenerator.createPiece(manager, lv2, new BlockPos(0, 7, 0), "tower_piece", lv, true));
            Piece lv3 = random.nextInt(3) == 0 ? lv2 : null;
            int j = 1 + random.nextInt(3);
            for (int k = 0; k < j; ++k) {
                lv2 = EndCityGenerator.addPiece(pieces, EndCityGenerator.createPiece(manager, lv2, new BlockPos(0, 4, 0), "tower_piece", lv, true));
                if (k >= j - 1 || !random.nextBoolean()) continue;
                lv3 = lv2;
            }
            if (lv3 != null) {
                for (Pair<BlockRotation, BlockPos> lv4 : SMALL_TOWER_BRIDGE_ATTACHMENTS) {
                    if (!random.nextBoolean()) continue;
                    Piece lv5 = EndCityGenerator.addPiece(pieces, EndCityGenerator.createPiece(manager, lv3, lv4.getRight(), "bridge_end", lv.rotate(lv4.getLeft()), true));
                    EndCityGenerator.createPart(manager, BRIDGE_PIECE, depth + 1, lv5, null, pieces, random);
                }
                lv2 = EndCityGenerator.addPiece(pieces, EndCityGenerator.createPiece(manager, lv2, new BlockPos(-1, 4, -1), "tower_top", lv, true));
            } else if (depth == 7) {
                lv2 = EndCityGenerator.addPiece(pieces, EndCityGenerator.createPiece(manager, lv2, new BlockPos(-1, 4, -1), "tower_top", lv, true));
            } else {
                return EndCityGenerator.createPart(manager, FAT_TOWER, depth + 1, lv2, null, pieces, random);
            }
            return true;
        }
    };
    static final Part BRIDGE_PIECE = new Part(){
        public boolean shipGenerated;

        @Override
        public void init() {
            this.shipGenerated = false;
        }

        @Override
        public boolean create(StructureTemplateManager manager, int depth, Piece root, BlockPos pos, List<StructurePiece> pieces, Random random) {
            BlockRotation lv = root.getPlacementData().getRotation();
            int j = random.nextInt(4) + 1;
            Piece lv2 = EndCityGenerator.addPiece(pieces, EndCityGenerator.createPiece(manager, root, new BlockPos(0, 0, -4), "bridge_piece", lv, true));
            lv2.setChainLength(-1);
            int k = 0;
            for (int l = 0; l < j; ++l) {
                if (random.nextBoolean()) {
                    lv2 = EndCityGenerator.addPiece(pieces, EndCityGenerator.createPiece(manager, lv2, new BlockPos(0, k, -4), "bridge_piece", lv, true));
                    k = 0;
                    continue;
                }
                lv2 = random.nextBoolean() ? EndCityGenerator.addPiece(pieces, EndCityGenerator.createPiece(manager, lv2, new BlockPos(0, k, -4), "bridge_steep_stairs", lv, true)) : EndCityGenerator.addPiece(pieces, EndCityGenerator.createPiece(manager, lv2, new BlockPos(0, k, -8), "bridge_gentle_stairs", lv, true));
                k = 4;
            }
            if (this.shipGenerated || random.nextInt(10 - depth) != 0) {
                if (!EndCityGenerator.createPart(manager, BUILDING, depth + 1, lv2, new BlockPos(-3, k + 1, -11), pieces, random)) {
                    return false;
                }
            } else {
                EndCityGenerator.addPiece(pieces, EndCityGenerator.createPiece(manager, lv2, new BlockPos(-8 + random.nextInt(8), k, -70 + random.nextInt(10)), "ship", lv, true));
                this.shipGenerated = true;
            }
            lv2 = EndCityGenerator.addPiece(pieces, EndCityGenerator.createPiece(manager, lv2, new BlockPos(4, k, 0), "bridge_end", lv.rotate(BlockRotation.CLOCKWISE_180), true));
            lv2.setChainLength(-1);
            return true;
        }
    };
    static final List<Pair<BlockRotation, BlockPos>> FAT_TOWER_BRIDGE_ATTACHMENTS = Lists.newArrayList(new Pair<BlockRotation, BlockPos>(BlockRotation.NONE, new BlockPos(4, -1, 0)), new Pair<BlockRotation, BlockPos>(BlockRotation.CLOCKWISE_90, new BlockPos(12, -1, 4)), new Pair<BlockRotation, BlockPos>(BlockRotation.COUNTERCLOCKWISE_90, new BlockPos(0, -1, 8)), new Pair<BlockRotation, BlockPos>(BlockRotation.CLOCKWISE_180, new BlockPos(8, -1, 12)));
    static final Part FAT_TOWER = new Part(){

        @Override
        public void init() {
        }

        @Override
        public boolean create(StructureTemplateManager manager, int depth, Piece root, BlockPos pos, List<StructurePiece> pieces, Random random) {
            BlockRotation lv = root.getPlacementData().getRotation();
            Piece lv2 = EndCityGenerator.addPiece(pieces, EndCityGenerator.createPiece(manager, root, new BlockPos(-3, 4, -3), "fat_tower_base", lv, true));
            lv2 = EndCityGenerator.addPiece(pieces, EndCityGenerator.createPiece(manager, lv2, new BlockPos(0, 4, 0), "fat_tower_middle", lv, true));
            for (int j = 0; j < 2 && random.nextInt(3) != 0; ++j) {
                lv2 = EndCityGenerator.addPiece(pieces, EndCityGenerator.createPiece(manager, lv2, new BlockPos(0, 8, 0), "fat_tower_middle", lv, true));
                for (Pair<BlockRotation, BlockPos> lv3 : FAT_TOWER_BRIDGE_ATTACHMENTS) {
                    if (!random.nextBoolean()) continue;
                    Piece lv4 = EndCityGenerator.addPiece(pieces, EndCityGenerator.createPiece(manager, lv2, lv3.getRight(), "bridge_end", lv.rotate(lv3.getLeft()), true));
                    EndCityGenerator.createPart(manager, BRIDGE_PIECE, depth + 1, lv4, null, pieces, random);
                }
            }
            lv2 = EndCityGenerator.addPiece(pieces, EndCityGenerator.createPiece(manager, lv2, new BlockPos(-2, 8, -2), "fat_tower_top", lv, true));
            return true;
        }
    };

    static Piece createPiece(StructureTemplateManager structureTemplateManager, Piece lastPiece, BlockPos relativePosition, String template, BlockRotation rotation, boolean ignoreAir) {
        Piece lv = new Piece(structureTemplateManager, template, lastPiece.getPos(), rotation, ignoreAir);
        BlockPos lv2 = lastPiece.getTemplate().transformBox(lastPiece.getPlacementData(), relativePosition, lv.getPlacementData(), BlockPos.ORIGIN);
        lv.translate(lv2.getX(), lv2.getY(), lv2.getZ());
        return lv;
    }

    public static void addPieces(StructureTemplateManager structureTemplateManager, BlockPos pos, BlockRotation rotation, List<StructurePiece> pieces, Random random) {
        FAT_TOWER.init();
        BUILDING.init();
        BRIDGE_PIECE.init();
        SMALL_TOWER.init();
        Piece lv = EndCityGenerator.addPiece(pieces, new Piece(structureTemplateManager, "base_floor", pos, rotation, true));
        lv = EndCityGenerator.addPiece(pieces, EndCityGenerator.createPiece(structureTemplateManager, lv, new BlockPos(-1, 0, -1), "second_floor_1", rotation, false));
        lv = EndCityGenerator.addPiece(pieces, EndCityGenerator.createPiece(structureTemplateManager, lv, new BlockPos(-1, 4, -1), "third_floor_1", rotation, false));
        lv = EndCityGenerator.addPiece(pieces, EndCityGenerator.createPiece(structureTemplateManager, lv, new BlockPos(-1, 8, -1), "third_roof", rotation, true));
        EndCityGenerator.createPart(structureTemplateManager, SMALL_TOWER, 1, lv, null, pieces, random);
    }

    static Piece addPiece(List<StructurePiece> pieces, Piece piece) {
        pieces.add(piece);
        return piece;
    }

    static boolean createPart(StructureTemplateManager manager, Part piece, int depth, Piece parent, BlockPos pos, List<StructurePiece> pieces, Random random) {
        if (depth > 8) {
            return false;
        }
        ArrayList<StructurePiece> list2 = Lists.newArrayList();
        if (piece.create(manager, depth, parent, pos, list2, random)) {
            boolean bl = false;
            int j = random.nextInt();
            for (StructurePiece lv : list2) {
                lv.setChainLength(j);
                StructurePiece lv2 = StructurePiece.firstIntersecting(pieces, lv.getBoundingBox());
                if (lv2 == null || lv2.getChainLength() == parent.getChainLength()) continue;
                bl = true;
                break;
            }
            if (!bl) {
                pieces.addAll(list2);
                return true;
            }
        }
        return false;
    }

    public static class Piece
    extends SimpleStructurePiece {
        public Piece(StructureTemplateManager manager, String template, BlockPos pos, BlockRotation rotation, boolean includeAir) {
            super(StructurePieceType.END_CITY, 0, manager, Piece.getId(template), template, Piece.createPlacementData(includeAir, rotation), pos);
        }

        public Piece(StructureTemplateManager manager, NbtCompound nbt) {
            super(StructurePieceType.END_CITY, nbt, manager, id -> Piece.createPlacementData(nbt.getBoolean("OW"), BlockRotation.valueOf(nbt.getString("Rot"))));
        }

        private static StructurePlacementData createPlacementData(boolean includeAir, BlockRotation rotation) {
            BlockIgnoreStructureProcessor lv = includeAir ? BlockIgnoreStructureProcessor.IGNORE_STRUCTURE_BLOCKS : BlockIgnoreStructureProcessor.IGNORE_AIR_AND_STRUCTURE_BLOCKS;
            return new StructurePlacementData().setIgnoreEntities(true).addProcessor(lv).setRotation(rotation);
        }

        @Override
        protected Identifier getId() {
            return Piece.getId(this.templateIdString);
        }

        private static Identifier getId(String template) {
            return Identifier.ofVanilla("end_city/" + template);
        }

        @Override
        protected void writeNbt(StructureContext context, NbtCompound nbt) {
            super.writeNbt(context, nbt);
            nbt.putString("Rot", this.placementData.getRotation().name());
            nbt.putBoolean("OW", this.placementData.getProcessors().get(0) == BlockIgnoreStructureProcessor.IGNORE_STRUCTURE_BLOCKS);
        }

        @Override
        protected void handleMetadata(String metadata, BlockPos pos, ServerWorldAccess world, Random random, BlockBox boundingBox) {
            if (metadata.startsWith("Chest")) {
                BlockPos lv = pos.down();
                if (boundingBox.contains(lv)) {
                    LootableInventory.setLootTable(world, random, lv, LootTables.END_CITY_TREASURE_CHEST);
                }
            } else if (boundingBox.contains(pos) && World.isValid(pos)) {
                if (metadata.startsWith("Sentry")) {
                    ShulkerEntity lv2 = EntityType.SHULKER.create(world.toServerWorld());
                    if (lv2 != null) {
                        lv2.setPosition((double)pos.getX() + 0.5, pos.getY(), (double)pos.getZ() + 0.5);
                        world.spawnEntity(lv2);
                    }
                } else if (metadata.startsWith("Elytra")) {
                    ItemFrameEntity lv3 = new ItemFrameEntity(world.toServerWorld(), pos, this.placementData.getRotation().rotate(Direction.SOUTH));
                    lv3.setHeldItemStack(new ItemStack(Items.ELYTRA), false);
                    world.spawnEntity(lv3);
                }
            }
        }
    }

    static interface Part {
        public void init();

        public boolean create(StructureTemplateManager var1, int var2, Piece var3, BlockPos var4, List<StructurePiece> var5, Random var6);
    }
}

