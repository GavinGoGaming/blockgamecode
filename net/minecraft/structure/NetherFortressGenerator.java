/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 * 
 * Could not load the following classes:
 *  org.jetbrains.annotations.Nullable
 */
package net.minecraft.structure;

import com.google.common.collect.Lists;
import java.util.List;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.FenceBlock;
import net.minecraft.block.StairsBlock;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.MobSpawnerBlockEntity;
import net.minecraft.entity.EntityType;
import net.minecraft.fluid.Fluids;
import net.minecraft.loot.LootTables;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.structure.StructureContext;
import net.minecraft.structure.StructurePiece;
import net.minecraft.structure.StructurePieceType;
import net.minecraft.structure.StructurePiecesHolder;
import net.minecraft.util.math.BlockBox;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.StructureWorldAccess;
import net.minecraft.world.gen.StructureAccessor;
import net.minecraft.world.gen.chunk.ChunkGenerator;
import org.jetbrains.annotations.Nullable;

public class NetherFortressGenerator {
    private static final int field_31557 = 30;
    private static final int field_31558 = 10;
    public static final int field_34730 = 64;
    static final PieceData[] ALL_BRIDGE_PIECES = new PieceData[]{new PieceData(Bridge.class, 30, 0, true), new PieceData(BridgeCrossing.class, 10, 4), new PieceData(BridgeSmallCrossing.class, 10, 4), new PieceData(BridgeStairs.class, 10, 3), new PieceData(BridgePlatform.class, 5, 2), new PieceData(CorridorExit.class, 5, 1)};
    static final PieceData[] ALL_CORRIDOR_PIECES = new PieceData[]{new PieceData(SmallCorridor.class, 25, 0, true), new PieceData(CorridorCrossing.class, 15, 5), new PieceData(CorridorRightTurn.class, 5, 10), new PieceData(CorridorLeftTurn.class, 5, 10), new PieceData(CorridorStairs.class, 10, 3, true), new PieceData(CorridorBalcony.class, 7, 2), new PieceData(CorridorNetherWartsRoom.class, 5, 2)};

    static Piece createPiece(PieceData pieceData, StructurePiecesHolder holder, Random random, int x, int y, int z, Direction orientation, int chainLength) {
        Class<? extends Piece> class_ = pieceData.pieceType;
        Piece lv = null;
        if (class_ == Bridge.class) {
            lv = Bridge.create(holder, random, x, y, z, orientation, chainLength);
        } else if (class_ == BridgeCrossing.class) {
            lv = BridgeCrossing.create(holder, x, y, z, orientation, chainLength);
        } else if (class_ == BridgeSmallCrossing.class) {
            lv = BridgeSmallCrossing.create(holder, x, y, z, orientation, chainLength);
        } else if (class_ == BridgeStairs.class) {
            lv = BridgeStairs.create(holder, x, y, z, chainLength, orientation);
        } else if (class_ == BridgePlatform.class) {
            lv = BridgePlatform.create(holder, x, y, z, chainLength, orientation);
        } else if (class_ == CorridorExit.class) {
            lv = CorridorExit.create(holder, random, x, y, z, orientation, chainLength);
        } else if (class_ == SmallCorridor.class) {
            lv = SmallCorridor.create(holder, x, y, z, orientation, chainLength);
        } else if (class_ == CorridorRightTurn.class) {
            lv = CorridorRightTurn.create(holder, random, x, y, z, orientation, chainLength);
        } else if (class_ == CorridorLeftTurn.class) {
            lv = CorridorLeftTurn.create(holder, random, x, y, z, orientation, chainLength);
        } else if (class_ == CorridorStairs.class) {
            lv = CorridorStairs.create(holder, x, y, z, orientation, chainLength);
        } else if (class_ == CorridorBalcony.class) {
            lv = CorridorBalcony.create(holder, x, y, z, orientation, chainLength);
        } else if (class_ == CorridorCrossing.class) {
            lv = CorridorCrossing.create(holder, x, y, z, orientation, chainLength);
        } else if (class_ == CorridorNetherWartsRoom.class) {
            lv = CorridorNetherWartsRoom.create(holder, x, y, z, orientation, chainLength);
        }
        return lv;
    }

    static class PieceData {
        public final Class<? extends Piece> pieceType;
        public final int weight;
        public int generatedCount;
        public final int limit;
        public final boolean repeatable;

        public PieceData(Class<? extends Piece> pieceType, int weight, int limit, boolean repeatable) {
            this.pieceType = pieceType;
            this.weight = weight;
            this.limit = limit;
            this.repeatable = repeatable;
        }

        public PieceData(Class<? extends Piece> pieceType, int weight, int limit) {
            this(pieceType, weight, limit, false);
        }

        public boolean canGenerate(int chainLength) {
            return this.limit == 0 || this.generatedCount < this.limit;
        }

        public boolean canGenerate() {
            return this.limit == 0 || this.generatedCount < this.limit;
        }
    }

    public static class Bridge
    extends Piece {
        private static final int SIZE_X = 5;
        private static final int SIZE_Y = 10;
        private static final int SIZE_Z = 19;

        public Bridge(int chainLength, Random random, BlockBox boundingBox, Direction orientation) {
            super(StructurePieceType.NETHER_FORTRESS_BRIDGE, chainLength, boundingBox);
            this.setOrientation(orientation);
        }

        public Bridge(NbtCompound nbt) {
            super(StructurePieceType.NETHER_FORTRESS_BRIDGE, nbt);
        }

        @Override
        public void fillOpenings(StructurePiece start, StructurePiecesHolder holder, Random random) {
            this.fillForwardOpening((Start)start, holder, random, 1, 3, false);
        }

        public static Bridge create(StructurePiecesHolder holder, Random random, int x, int y, int z, Direction orientation, int chainLength) {
            BlockBox lv = BlockBox.rotated(x, y, z, -1, -3, 0, 5, 10, 19, orientation);
            if (!Bridge.isInBounds(lv) || holder.getIntersecting(lv) != null) {
                return null;
            }
            return new Bridge(chainLength, random, lv, orientation);
        }

        @Override
        public void generate(StructureWorldAccess world, StructureAccessor structureAccessor, ChunkGenerator chunkGenerator, Random random, BlockBox chunkBox, ChunkPos chunkPos, BlockPos pivot) {
            this.fillWithOutline(world, chunkBox, 0, 3, 0, 4, 4, 18, Blocks.NETHER_BRICKS.getDefaultState(), Blocks.NETHER_BRICKS.getDefaultState(), false);
            this.fillWithOutline(world, chunkBox, 1, 5, 0, 3, 7, 18, Blocks.AIR.getDefaultState(), Blocks.AIR.getDefaultState(), false);
            this.fillWithOutline(world, chunkBox, 0, 5, 0, 0, 5, 18, Blocks.NETHER_BRICKS.getDefaultState(), Blocks.NETHER_BRICKS.getDefaultState(), false);
            this.fillWithOutline(world, chunkBox, 4, 5, 0, 4, 5, 18, Blocks.NETHER_BRICKS.getDefaultState(), Blocks.NETHER_BRICKS.getDefaultState(), false);
            this.fillWithOutline(world, chunkBox, 0, 2, 0, 4, 2, 5, Blocks.NETHER_BRICKS.getDefaultState(), Blocks.NETHER_BRICKS.getDefaultState(), false);
            this.fillWithOutline(world, chunkBox, 0, 2, 13, 4, 2, 18, Blocks.NETHER_BRICKS.getDefaultState(), Blocks.NETHER_BRICKS.getDefaultState(), false);
            this.fillWithOutline(world, chunkBox, 0, 0, 0, 4, 1, 3, Blocks.NETHER_BRICKS.getDefaultState(), Blocks.NETHER_BRICKS.getDefaultState(), false);
            this.fillWithOutline(world, chunkBox, 0, 0, 15, 4, 1, 18, Blocks.NETHER_BRICKS.getDefaultState(), Blocks.NETHER_BRICKS.getDefaultState(), false);
            for (int i = 0; i <= 4; ++i) {
                for (int j = 0; j <= 2; ++j) {
                    this.fillDownwards(world, Blocks.NETHER_BRICKS.getDefaultState(), i, -1, j, chunkBox);
                    this.fillDownwards(world, Blocks.NETHER_BRICKS.getDefaultState(), i, -1, 18 - j, chunkBox);
                }
            }
            BlockState lv = (BlockState)((BlockState)Blocks.NETHER_BRICK_FENCE.getDefaultState().with(FenceBlock.NORTH, true)).with(FenceBlock.SOUTH, true);
            BlockState lv2 = (BlockState)lv.with(FenceBlock.EAST, true);
            BlockState lv3 = (BlockState)lv.with(FenceBlock.WEST, true);
            this.fillWithOutline(world, chunkBox, 0, 1, 1, 0, 4, 1, lv2, lv2, false);
            this.fillWithOutline(world, chunkBox, 0, 3, 4, 0, 4, 4, lv2, lv2, false);
            this.fillWithOutline(world, chunkBox, 0, 3, 14, 0, 4, 14, lv2, lv2, false);
            this.fillWithOutline(world, chunkBox, 0, 1, 17, 0, 4, 17, lv2, lv2, false);
            this.fillWithOutline(world, chunkBox, 4, 1, 1, 4, 4, 1, lv3, lv3, false);
            this.fillWithOutline(world, chunkBox, 4, 3, 4, 4, 4, 4, lv3, lv3, false);
            this.fillWithOutline(world, chunkBox, 4, 3, 14, 4, 4, 14, lv3, lv3, false);
            this.fillWithOutline(world, chunkBox, 4, 1, 17, 4, 4, 17, lv3, lv3, false);
        }
    }

    public static class BridgeCrossing
    extends Piece {
        private static final int SIZE_X = 19;
        private static final int SIZE_Y = 10;
        private static final int SIZE_Z = 19;

        public BridgeCrossing(int chainLength, BlockBox boundingBox, Direction orientation) {
            super(StructurePieceType.NETHER_FORTRESS_BRIDGE_CROSSING, chainLength, boundingBox);
            this.setOrientation(orientation);
        }

        protected BridgeCrossing(int x, int z, Direction orientation) {
            super(StructurePieceType.NETHER_FORTRESS_BRIDGE_CROSSING, 0, StructurePiece.createBox(x, 64, z, orientation, 19, 10, 19));
            this.setOrientation(orientation);
        }

        protected BridgeCrossing(StructurePieceType arg, NbtCompound arg2) {
            super(arg, arg2);
        }

        public BridgeCrossing(NbtCompound nbt) {
            this(StructurePieceType.NETHER_FORTRESS_BRIDGE_CROSSING, nbt);
        }

        @Override
        public void fillOpenings(StructurePiece start, StructurePiecesHolder holder, Random random) {
            this.fillForwardOpening((Start)start, holder, random, 8, 3, false);
            this.fillNWOpening((Start)start, holder, random, 3, 8, false);
            this.fillSEOpening((Start)start, holder, random, 3, 8, false);
        }

        public static BridgeCrossing create(StructurePiecesHolder holder, int x, int y, int z, Direction orientation, int chainLength) {
            BlockBox lv = BlockBox.rotated(x, y, z, -8, -3, 0, 19, 10, 19, orientation);
            if (!BridgeCrossing.isInBounds(lv) || holder.getIntersecting(lv) != null) {
                return null;
            }
            return new BridgeCrossing(chainLength, lv, orientation);
        }

        @Override
        public void generate(StructureWorldAccess world, StructureAccessor structureAccessor, ChunkGenerator chunkGenerator, Random random, BlockBox chunkBox, ChunkPos chunkPos, BlockPos pivot) {
            int j;
            int i;
            this.fillWithOutline(world, chunkBox, 7, 3, 0, 11, 4, 18, Blocks.NETHER_BRICKS.getDefaultState(), Blocks.NETHER_BRICKS.getDefaultState(), false);
            this.fillWithOutline(world, chunkBox, 0, 3, 7, 18, 4, 11, Blocks.NETHER_BRICKS.getDefaultState(), Blocks.NETHER_BRICKS.getDefaultState(), false);
            this.fillWithOutline(world, chunkBox, 8, 5, 0, 10, 7, 18, Blocks.AIR.getDefaultState(), Blocks.AIR.getDefaultState(), false);
            this.fillWithOutline(world, chunkBox, 0, 5, 8, 18, 7, 10, Blocks.AIR.getDefaultState(), Blocks.AIR.getDefaultState(), false);
            this.fillWithOutline(world, chunkBox, 7, 5, 0, 7, 5, 7, Blocks.NETHER_BRICKS.getDefaultState(), Blocks.NETHER_BRICKS.getDefaultState(), false);
            this.fillWithOutline(world, chunkBox, 7, 5, 11, 7, 5, 18, Blocks.NETHER_BRICKS.getDefaultState(), Blocks.NETHER_BRICKS.getDefaultState(), false);
            this.fillWithOutline(world, chunkBox, 11, 5, 0, 11, 5, 7, Blocks.NETHER_BRICKS.getDefaultState(), Blocks.NETHER_BRICKS.getDefaultState(), false);
            this.fillWithOutline(world, chunkBox, 11, 5, 11, 11, 5, 18, Blocks.NETHER_BRICKS.getDefaultState(), Blocks.NETHER_BRICKS.getDefaultState(), false);
            this.fillWithOutline(world, chunkBox, 0, 5, 7, 7, 5, 7, Blocks.NETHER_BRICKS.getDefaultState(), Blocks.NETHER_BRICKS.getDefaultState(), false);
            this.fillWithOutline(world, chunkBox, 11, 5, 7, 18, 5, 7, Blocks.NETHER_BRICKS.getDefaultState(), Blocks.NETHER_BRICKS.getDefaultState(), false);
            this.fillWithOutline(world, chunkBox, 0, 5, 11, 7, 5, 11, Blocks.NETHER_BRICKS.getDefaultState(), Blocks.NETHER_BRICKS.getDefaultState(), false);
            this.fillWithOutline(world, chunkBox, 11, 5, 11, 18, 5, 11, Blocks.NETHER_BRICKS.getDefaultState(), Blocks.NETHER_BRICKS.getDefaultState(), false);
            this.fillWithOutline(world, chunkBox, 7, 2, 0, 11, 2, 5, Blocks.NETHER_BRICKS.getDefaultState(), Blocks.NETHER_BRICKS.getDefaultState(), false);
            this.fillWithOutline(world, chunkBox, 7, 2, 13, 11, 2, 18, Blocks.NETHER_BRICKS.getDefaultState(), Blocks.NETHER_BRICKS.getDefaultState(), false);
            this.fillWithOutline(world, chunkBox, 7, 0, 0, 11, 1, 3, Blocks.NETHER_BRICKS.getDefaultState(), Blocks.NETHER_BRICKS.getDefaultState(), false);
            this.fillWithOutline(world, chunkBox, 7, 0, 15, 11, 1, 18, Blocks.NETHER_BRICKS.getDefaultState(), Blocks.NETHER_BRICKS.getDefaultState(), false);
            for (i = 7; i <= 11; ++i) {
                for (j = 0; j <= 2; ++j) {
                    this.fillDownwards(world, Blocks.NETHER_BRICKS.getDefaultState(), i, -1, j, chunkBox);
                    this.fillDownwards(world, Blocks.NETHER_BRICKS.getDefaultState(), i, -1, 18 - j, chunkBox);
                }
            }
            this.fillWithOutline(world, chunkBox, 0, 2, 7, 5, 2, 11, Blocks.NETHER_BRICKS.getDefaultState(), Blocks.NETHER_BRICKS.getDefaultState(), false);
            this.fillWithOutline(world, chunkBox, 13, 2, 7, 18, 2, 11, Blocks.NETHER_BRICKS.getDefaultState(), Blocks.NETHER_BRICKS.getDefaultState(), false);
            this.fillWithOutline(world, chunkBox, 0, 0, 7, 3, 1, 11, Blocks.NETHER_BRICKS.getDefaultState(), Blocks.NETHER_BRICKS.getDefaultState(), false);
            this.fillWithOutline(world, chunkBox, 15, 0, 7, 18, 1, 11, Blocks.NETHER_BRICKS.getDefaultState(), Blocks.NETHER_BRICKS.getDefaultState(), false);
            for (i = 0; i <= 2; ++i) {
                for (j = 7; j <= 11; ++j) {
                    this.fillDownwards(world, Blocks.NETHER_BRICKS.getDefaultState(), i, -1, j, chunkBox);
                    this.fillDownwards(world, Blocks.NETHER_BRICKS.getDefaultState(), 18 - i, -1, j, chunkBox);
                }
            }
        }
    }

    public static class BridgeSmallCrossing
    extends Piece {
        private static final int SIZE_X = 7;
        private static final int SIZE_Y = 9;
        private static final int SIZE_Z = 7;

        public BridgeSmallCrossing(int chainLength, BlockBox boundingBox, Direction orientation) {
            super(StructurePieceType.NETHER_FORTRESS_BRIDGE_SMALL_CROSSING, chainLength, boundingBox);
            this.setOrientation(orientation);
        }

        public BridgeSmallCrossing(NbtCompound nbt) {
            super(StructurePieceType.NETHER_FORTRESS_BRIDGE_SMALL_CROSSING, nbt);
        }

        @Override
        public void fillOpenings(StructurePiece start, StructurePiecesHolder holder, Random random) {
            this.fillForwardOpening((Start)start, holder, random, 2, 0, false);
            this.fillNWOpening((Start)start, holder, random, 0, 2, false);
            this.fillSEOpening((Start)start, holder, random, 0, 2, false);
        }

        public static BridgeSmallCrossing create(StructurePiecesHolder holder, int x, int y, int z, Direction orientation, int chainLength) {
            BlockBox lv = BlockBox.rotated(x, y, z, -2, 0, 0, 7, 9, 7, orientation);
            if (!BridgeSmallCrossing.isInBounds(lv) || holder.getIntersecting(lv) != null) {
                return null;
            }
            return new BridgeSmallCrossing(chainLength, lv, orientation);
        }

        @Override
        public void generate(StructureWorldAccess world, StructureAccessor structureAccessor, ChunkGenerator chunkGenerator, Random random, BlockBox chunkBox, ChunkPos chunkPos, BlockPos pivot) {
            this.fillWithOutline(world, chunkBox, 0, 0, 0, 6, 1, 6, Blocks.NETHER_BRICKS.getDefaultState(), Blocks.NETHER_BRICKS.getDefaultState(), false);
            this.fillWithOutline(world, chunkBox, 0, 2, 0, 6, 7, 6, Blocks.AIR.getDefaultState(), Blocks.AIR.getDefaultState(), false);
            this.fillWithOutline(world, chunkBox, 0, 2, 0, 1, 6, 0, Blocks.NETHER_BRICKS.getDefaultState(), Blocks.NETHER_BRICKS.getDefaultState(), false);
            this.fillWithOutline(world, chunkBox, 0, 2, 6, 1, 6, 6, Blocks.NETHER_BRICKS.getDefaultState(), Blocks.NETHER_BRICKS.getDefaultState(), false);
            this.fillWithOutline(world, chunkBox, 5, 2, 0, 6, 6, 0, Blocks.NETHER_BRICKS.getDefaultState(), Blocks.NETHER_BRICKS.getDefaultState(), false);
            this.fillWithOutline(world, chunkBox, 5, 2, 6, 6, 6, 6, Blocks.NETHER_BRICKS.getDefaultState(), Blocks.NETHER_BRICKS.getDefaultState(), false);
            this.fillWithOutline(world, chunkBox, 0, 2, 0, 0, 6, 1, Blocks.NETHER_BRICKS.getDefaultState(), Blocks.NETHER_BRICKS.getDefaultState(), false);
            this.fillWithOutline(world, chunkBox, 0, 2, 5, 0, 6, 6, Blocks.NETHER_BRICKS.getDefaultState(), Blocks.NETHER_BRICKS.getDefaultState(), false);
            this.fillWithOutline(world, chunkBox, 6, 2, 0, 6, 6, 1, Blocks.NETHER_BRICKS.getDefaultState(), Blocks.NETHER_BRICKS.getDefaultState(), false);
            this.fillWithOutline(world, chunkBox, 6, 2, 5, 6, 6, 6, Blocks.NETHER_BRICKS.getDefaultState(), Blocks.NETHER_BRICKS.getDefaultState(), false);
            BlockState lv = (BlockState)((BlockState)Blocks.NETHER_BRICK_FENCE.getDefaultState().with(FenceBlock.WEST, true)).with(FenceBlock.EAST, true);
            BlockState lv2 = (BlockState)((BlockState)Blocks.NETHER_BRICK_FENCE.getDefaultState().with(FenceBlock.NORTH, true)).with(FenceBlock.SOUTH, true);
            this.fillWithOutline(world, chunkBox, 2, 6, 0, 4, 6, 0, Blocks.NETHER_BRICKS.getDefaultState(), Blocks.NETHER_BRICKS.getDefaultState(), false);
            this.fillWithOutline(world, chunkBox, 2, 5, 0, 4, 5, 0, lv, lv, false);
            this.fillWithOutline(world, chunkBox, 2, 6, 6, 4, 6, 6, Blocks.NETHER_BRICKS.getDefaultState(), Blocks.NETHER_BRICKS.getDefaultState(), false);
            this.fillWithOutline(world, chunkBox, 2, 5, 6, 4, 5, 6, lv, lv, false);
            this.fillWithOutline(world, chunkBox, 0, 6, 2, 0, 6, 4, Blocks.NETHER_BRICKS.getDefaultState(), Blocks.NETHER_BRICKS.getDefaultState(), false);
            this.fillWithOutline(world, chunkBox, 0, 5, 2, 0, 5, 4, lv2, lv2, false);
            this.fillWithOutline(world, chunkBox, 6, 6, 2, 6, 6, 4, Blocks.NETHER_BRICKS.getDefaultState(), Blocks.NETHER_BRICKS.getDefaultState(), false);
            this.fillWithOutline(world, chunkBox, 6, 5, 2, 6, 5, 4, lv2, lv2, false);
            for (int i = 0; i <= 6; ++i) {
                for (int j = 0; j <= 6; ++j) {
                    this.fillDownwards(world, Blocks.NETHER_BRICKS.getDefaultState(), i, -1, j, chunkBox);
                }
            }
        }
    }

    public static class BridgeStairs
    extends Piece {
        private static final int SIZE_X = 7;
        private static final int SIZE_Y = 11;
        private static final int SIZE_Z = 7;

        public BridgeStairs(int chainLength, BlockBox boundingBox, Direction orientation) {
            super(StructurePieceType.NETHER_FORTRESS_BRIDGE_STAIRS, chainLength, boundingBox);
            this.setOrientation(orientation);
        }

        public BridgeStairs(NbtCompound nbt) {
            super(StructurePieceType.NETHER_FORTRESS_BRIDGE_STAIRS, nbt);
        }

        @Override
        public void fillOpenings(StructurePiece start, StructurePiecesHolder holder, Random random) {
            this.fillSEOpening((Start)start, holder, random, 6, 2, false);
        }

        public static BridgeStairs create(StructurePiecesHolder holder, int x, int y, int z, int chainlength, Direction orientation) {
            BlockBox lv = BlockBox.rotated(x, y, z, -2, 0, 0, 7, 11, 7, orientation);
            if (!BridgeStairs.isInBounds(lv) || holder.getIntersecting(lv) != null) {
                return null;
            }
            return new BridgeStairs(chainlength, lv, orientation);
        }

        @Override
        public void generate(StructureWorldAccess world, StructureAccessor structureAccessor, ChunkGenerator chunkGenerator, Random random, BlockBox chunkBox, ChunkPos chunkPos, BlockPos pivot) {
            this.fillWithOutline(world, chunkBox, 0, 0, 0, 6, 1, 6, Blocks.NETHER_BRICKS.getDefaultState(), Blocks.NETHER_BRICKS.getDefaultState(), false);
            this.fillWithOutline(world, chunkBox, 0, 2, 0, 6, 10, 6, Blocks.AIR.getDefaultState(), Blocks.AIR.getDefaultState(), false);
            this.fillWithOutline(world, chunkBox, 0, 2, 0, 1, 8, 0, Blocks.NETHER_BRICKS.getDefaultState(), Blocks.NETHER_BRICKS.getDefaultState(), false);
            this.fillWithOutline(world, chunkBox, 5, 2, 0, 6, 8, 0, Blocks.NETHER_BRICKS.getDefaultState(), Blocks.NETHER_BRICKS.getDefaultState(), false);
            this.fillWithOutline(world, chunkBox, 0, 2, 1, 0, 8, 6, Blocks.NETHER_BRICKS.getDefaultState(), Blocks.NETHER_BRICKS.getDefaultState(), false);
            this.fillWithOutline(world, chunkBox, 6, 2, 1, 6, 8, 6, Blocks.NETHER_BRICKS.getDefaultState(), Blocks.NETHER_BRICKS.getDefaultState(), false);
            this.fillWithOutline(world, chunkBox, 1, 2, 6, 5, 8, 6, Blocks.NETHER_BRICKS.getDefaultState(), Blocks.NETHER_BRICKS.getDefaultState(), false);
            BlockState lv = (BlockState)((BlockState)Blocks.NETHER_BRICK_FENCE.getDefaultState().with(FenceBlock.WEST, true)).with(FenceBlock.EAST, true);
            BlockState lv2 = (BlockState)((BlockState)Blocks.NETHER_BRICK_FENCE.getDefaultState().with(FenceBlock.NORTH, true)).with(FenceBlock.SOUTH, true);
            this.fillWithOutline(world, chunkBox, 0, 3, 2, 0, 5, 4, lv2, lv2, false);
            this.fillWithOutline(world, chunkBox, 6, 3, 2, 6, 5, 2, lv2, lv2, false);
            this.fillWithOutline(world, chunkBox, 6, 3, 4, 6, 5, 4, lv2, lv2, false);
            this.addBlock(world, Blocks.NETHER_BRICKS.getDefaultState(), 5, 2, 5, chunkBox);
            this.fillWithOutline(world, chunkBox, 4, 2, 5, 4, 3, 5, Blocks.NETHER_BRICKS.getDefaultState(), Blocks.NETHER_BRICKS.getDefaultState(), false);
            this.fillWithOutline(world, chunkBox, 3, 2, 5, 3, 4, 5, Blocks.NETHER_BRICKS.getDefaultState(), Blocks.NETHER_BRICKS.getDefaultState(), false);
            this.fillWithOutline(world, chunkBox, 2, 2, 5, 2, 5, 5, Blocks.NETHER_BRICKS.getDefaultState(), Blocks.NETHER_BRICKS.getDefaultState(), false);
            this.fillWithOutline(world, chunkBox, 1, 2, 5, 1, 6, 5, Blocks.NETHER_BRICKS.getDefaultState(), Blocks.NETHER_BRICKS.getDefaultState(), false);
            this.fillWithOutline(world, chunkBox, 1, 7, 1, 5, 7, 4, Blocks.NETHER_BRICKS.getDefaultState(), Blocks.NETHER_BRICKS.getDefaultState(), false);
            this.fillWithOutline(world, chunkBox, 6, 8, 2, 6, 8, 4, Blocks.AIR.getDefaultState(), Blocks.AIR.getDefaultState(), false);
            this.fillWithOutline(world, chunkBox, 2, 6, 0, 4, 8, 0, Blocks.NETHER_BRICKS.getDefaultState(), Blocks.NETHER_BRICKS.getDefaultState(), false);
            this.fillWithOutline(world, chunkBox, 2, 5, 0, 4, 5, 0, lv, lv, false);
            for (int i = 0; i <= 6; ++i) {
                for (int j = 0; j <= 6; ++j) {
                    this.fillDownwards(world, Blocks.NETHER_BRICKS.getDefaultState(), i, -1, j, chunkBox);
                }
            }
        }
    }

    public static class BridgePlatform
    extends Piece {
        private static final int SIZE_X = 7;
        private static final int SIZE_Y = 8;
        private static final int SIZE_Z = 9;
        private boolean hasBlazeSpawner;

        public BridgePlatform(int chainLength, BlockBox boundingBox, Direction orientation) {
            super(StructurePieceType.NETHER_FORTRESS_BRIDGE_PLATFORM, chainLength, boundingBox);
            this.setOrientation(orientation);
        }

        public BridgePlatform(NbtCompound nbt) {
            super(StructurePieceType.NETHER_FORTRESS_BRIDGE_PLATFORM, nbt);
            this.hasBlazeSpawner = nbt.getBoolean("Mob");
        }

        @Override
        protected void writeNbt(StructureContext context, NbtCompound nbt) {
            super.writeNbt(context, nbt);
            nbt.putBoolean("Mob", this.hasBlazeSpawner);
        }

        public static BridgePlatform create(StructurePiecesHolder holder, int x, int y, int z, int chainLength, Direction orientation) {
            BlockBox lv = BlockBox.rotated(x, y, z, -2, 0, 0, 7, 8, 9, orientation);
            if (!BridgePlatform.isInBounds(lv) || holder.getIntersecting(lv) != null) {
                return null;
            }
            return new BridgePlatform(chainLength, lv, orientation);
        }

        @Override
        public void generate(StructureWorldAccess world, StructureAccessor structureAccessor, ChunkGenerator chunkGenerator, Random random, BlockBox chunkBox, ChunkPos chunkPos, BlockPos pivot) {
            BlockPos.Mutable lv3;
            this.fillWithOutline(world, chunkBox, 0, 2, 0, 6, 7, 7, Blocks.AIR.getDefaultState(), Blocks.AIR.getDefaultState(), false);
            this.fillWithOutline(world, chunkBox, 1, 0, 0, 5, 1, 7, Blocks.NETHER_BRICKS.getDefaultState(), Blocks.NETHER_BRICKS.getDefaultState(), false);
            this.fillWithOutline(world, chunkBox, 1, 2, 1, 5, 2, 7, Blocks.NETHER_BRICKS.getDefaultState(), Blocks.NETHER_BRICKS.getDefaultState(), false);
            this.fillWithOutline(world, chunkBox, 1, 3, 2, 5, 3, 7, Blocks.NETHER_BRICKS.getDefaultState(), Blocks.NETHER_BRICKS.getDefaultState(), false);
            this.fillWithOutline(world, chunkBox, 1, 4, 3, 5, 4, 7, Blocks.NETHER_BRICKS.getDefaultState(), Blocks.NETHER_BRICKS.getDefaultState(), false);
            this.fillWithOutline(world, chunkBox, 1, 2, 0, 1, 4, 2, Blocks.NETHER_BRICKS.getDefaultState(), Blocks.NETHER_BRICKS.getDefaultState(), false);
            this.fillWithOutline(world, chunkBox, 5, 2, 0, 5, 4, 2, Blocks.NETHER_BRICKS.getDefaultState(), Blocks.NETHER_BRICKS.getDefaultState(), false);
            this.fillWithOutline(world, chunkBox, 1, 5, 2, 1, 5, 3, Blocks.NETHER_BRICKS.getDefaultState(), Blocks.NETHER_BRICKS.getDefaultState(), false);
            this.fillWithOutline(world, chunkBox, 5, 5, 2, 5, 5, 3, Blocks.NETHER_BRICKS.getDefaultState(), Blocks.NETHER_BRICKS.getDefaultState(), false);
            this.fillWithOutline(world, chunkBox, 0, 5, 3, 0, 5, 8, Blocks.NETHER_BRICKS.getDefaultState(), Blocks.NETHER_BRICKS.getDefaultState(), false);
            this.fillWithOutline(world, chunkBox, 6, 5, 3, 6, 5, 8, Blocks.NETHER_BRICKS.getDefaultState(), Blocks.NETHER_BRICKS.getDefaultState(), false);
            this.fillWithOutline(world, chunkBox, 1, 5, 8, 5, 5, 8, Blocks.NETHER_BRICKS.getDefaultState(), Blocks.NETHER_BRICKS.getDefaultState(), false);
            BlockState lv = (BlockState)((BlockState)Blocks.NETHER_BRICK_FENCE.getDefaultState().with(FenceBlock.WEST, true)).with(FenceBlock.EAST, true);
            BlockState lv2 = (BlockState)((BlockState)Blocks.NETHER_BRICK_FENCE.getDefaultState().with(FenceBlock.NORTH, true)).with(FenceBlock.SOUTH, true);
            this.addBlock(world, (BlockState)Blocks.NETHER_BRICK_FENCE.getDefaultState().with(FenceBlock.WEST, true), 1, 6, 3, chunkBox);
            this.addBlock(world, (BlockState)Blocks.NETHER_BRICK_FENCE.getDefaultState().with(FenceBlock.EAST, true), 5, 6, 3, chunkBox);
            this.addBlock(world, (BlockState)((BlockState)Blocks.NETHER_BRICK_FENCE.getDefaultState().with(FenceBlock.EAST, true)).with(FenceBlock.NORTH, true), 0, 6, 3, chunkBox);
            this.addBlock(world, (BlockState)((BlockState)Blocks.NETHER_BRICK_FENCE.getDefaultState().with(FenceBlock.WEST, true)).with(FenceBlock.NORTH, true), 6, 6, 3, chunkBox);
            this.fillWithOutline(world, chunkBox, 0, 6, 4, 0, 6, 7, lv2, lv2, false);
            this.fillWithOutline(world, chunkBox, 6, 6, 4, 6, 6, 7, lv2, lv2, false);
            this.addBlock(world, (BlockState)((BlockState)Blocks.NETHER_BRICK_FENCE.getDefaultState().with(FenceBlock.EAST, true)).with(FenceBlock.SOUTH, true), 0, 6, 8, chunkBox);
            this.addBlock(world, (BlockState)((BlockState)Blocks.NETHER_BRICK_FENCE.getDefaultState().with(FenceBlock.WEST, true)).with(FenceBlock.SOUTH, true), 6, 6, 8, chunkBox);
            this.fillWithOutline(world, chunkBox, 1, 6, 8, 5, 6, 8, lv, lv, false);
            this.addBlock(world, (BlockState)Blocks.NETHER_BRICK_FENCE.getDefaultState().with(FenceBlock.EAST, true), 1, 7, 8, chunkBox);
            this.fillWithOutline(world, chunkBox, 2, 7, 8, 4, 7, 8, lv, lv, false);
            this.addBlock(world, (BlockState)Blocks.NETHER_BRICK_FENCE.getDefaultState().with(FenceBlock.WEST, true), 5, 7, 8, chunkBox);
            this.addBlock(world, (BlockState)Blocks.NETHER_BRICK_FENCE.getDefaultState().with(FenceBlock.EAST, true), 2, 8, 8, chunkBox);
            this.addBlock(world, lv, 3, 8, 8, chunkBox);
            this.addBlock(world, (BlockState)Blocks.NETHER_BRICK_FENCE.getDefaultState().with(FenceBlock.WEST, true), 4, 8, 8, chunkBox);
            if (!this.hasBlazeSpawner && chunkBox.contains(lv3 = this.offsetPos(3, 5, 5))) {
                this.hasBlazeSpawner = true;
                world.setBlockState(lv3, Blocks.SPAWNER.getDefaultState(), Block.NOTIFY_LISTENERS);
                BlockEntity lv4 = world.getBlockEntity(lv3);
                if (lv4 instanceof MobSpawnerBlockEntity) {
                    MobSpawnerBlockEntity lv5 = (MobSpawnerBlockEntity)lv4;
                    lv5.setEntityType(EntityType.BLAZE, random);
                }
            }
            for (int i = 0; i <= 6; ++i) {
                for (int j = 0; j <= 6; ++j) {
                    this.fillDownwards(world, Blocks.NETHER_BRICKS.getDefaultState(), i, -1, j, chunkBox);
                }
            }
        }
    }

    public static class CorridorExit
    extends Piece {
        private static final int SIZE_X = 13;
        private static final int SIZE_Y = 14;
        private static final int SIZE_Z = 13;

        public CorridorExit(int chainLength, Random random, BlockBox boundingBox, Direction orientation) {
            super(StructurePieceType.NETHER_FORTRESS_CORRIDOR_EXIT, chainLength, boundingBox);
            this.setOrientation(orientation);
        }

        public CorridorExit(NbtCompound nbt) {
            super(StructurePieceType.NETHER_FORTRESS_CORRIDOR_EXIT, nbt);
        }

        @Override
        public void fillOpenings(StructurePiece start, StructurePiecesHolder holder, Random random) {
            this.fillForwardOpening((Start)start, holder, random, 5, 3, true);
        }

        public static CorridorExit create(StructurePiecesHolder holder, Random random, int x, int y, int z, Direction orientation, int chainLength) {
            BlockBox lv = BlockBox.rotated(x, y, z, -5, -3, 0, 13, 14, 13, orientation);
            if (!CorridorExit.isInBounds(lv) || holder.getIntersecting(lv) != null) {
                return null;
            }
            return new CorridorExit(chainLength, random, lv, orientation);
        }

        @Override
        public void generate(StructureWorldAccess world, StructureAccessor structureAccessor, ChunkGenerator chunkGenerator, Random random, BlockBox chunkBox, ChunkPos chunkPos, BlockPos pivot) {
            int j;
            int i;
            this.fillWithOutline(world, chunkBox, 0, 3, 0, 12, 4, 12, Blocks.NETHER_BRICKS.getDefaultState(), Blocks.NETHER_BRICKS.getDefaultState(), false);
            this.fillWithOutline(world, chunkBox, 0, 5, 0, 12, 13, 12, Blocks.AIR.getDefaultState(), Blocks.AIR.getDefaultState(), false);
            this.fillWithOutline(world, chunkBox, 0, 5, 0, 1, 12, 12, Blocks.NETHER_BRICKS.getDefaultState(), Blocks.NETHER_BRICKS.getDefaultState(), false);
            this.fillWithOutline(world, chunkBox, 11, 5, 0, 12, 12, 12, Blocks.NETHER_BRICKS.getDefaultState(), Blocks.NETHER_BRICKS.getDefaultState(), false);
            this.fillWithOutline(world, chunkBox, 2, 5, 11, 4, 12, 12, Blocks.NETHER_BRICKS.getDefaultState(), Blocks.NETHER_BRICKS.getDefaultState(), false);
            this.fillWithOutline(world, chunkBox, 8, 5, 11, 10, 12, 12, Blocks.NETHER_BRICKS.getDefaultState(), Blocks.NETHER_BRICKS.getDefaultState(), false);
            this.fillWithOutline(world, chunkBox, 5, 9, 11, 7, 12, 12, Blocks.NETHER_BRICKS.getDefaultState(), Blocks.NETHER_BRICKS.getDefaultState(), false);
            this.fillWithOutline(world, chunkBox, 2, 5, 0, 4, 12, 1, Blocks.NETHER_BRICKS.getDefaultState(), Blocks.NETHER_BRICKS.getDefaultState(), false);
            this.fillWithOutline(world, chunkBox, 8, 5, 0, 10, 12, 1, Blocks.NETHER_BRICKS.getDefaultState(), Blocks.NETHER_BRICKS.getDefaultState(), false);
            this.fillWithOutline(world, chunkBox, 5, 9, 0, 7, 12, 1, Blocks.NETHER_BRICKS.getDefaultState(), Blocks.NETHER_BRICKS.getDefaultState(), false);
            this.fillWithOutline(world, chunkBox, 2, 11, 2, 10, 12, 10, Blocks.NETHER_BRICKS.getDefaultState(), Blocks.NETHER_BRICKS.getDefaultState(), false);
            this.fillWithOutline(world, chunkBox, 5, 8, 0, 7, 8, 0, Blocks.NETHER_BRICK_FENCE.getDefaultState(), Blocks.NETHER_BRICK_FENCE.getDefaultState(), false);
            BlockState lv = (BlockState)((BlockState)Blocks.NETHER_BRICK_FENCE.getDefaultState().with(FenceBlock.WEST, true)).with(FenceBlock.EAST, true);
            BlockState lv2 = (BlockState)((BlockState)Blocks.NETHER_BRICK_FENCE.getDefaultState().with(FenceBlock.NORTH, true)).with(FenceBlock.SOUTH, true);
            for (i = 1; i <= 11; i += 2) {
                this.fillWithOutline(world, chunkBox, i, 10, 0, i, 11, 0, lv, lv, false);
                this.fillWithOutline(world, chunkBox, i, 10, 12, i, 11, 12, lv, lv, false);
                this.fillWithOutline(world, chunkBox, 0, 10, i, 0, 11, i, lv2, lv2, false);
                this.fillWithOutline(world, chunkBox, 12, 10, i, 12, 11, i, lv2, lv2, false);
                this.addBlock(world, Blocks.NETHER_BRICKS.getDefaultState(), i, 13, 0, chunkBox);
                this.addBlock(world, Blocks.NETHER_BRICKS.getDefaultState(), i, 13, 12, chunkBox);
                this.addBlock(world, Blocks.NETHER_BRICKS.getDefaultState(), 0, 13, i, chunkBox);
                this.addBlock(world, Blocks.NETHER_BRICKS.getDefaultState(), 12, 13, i, chunkBox);
                if (i == 11) continue;
                this.addBlock(world, lv, i + 1, 13, 0, chunkBox);
                this.addBlock(world, lv, i + 1, 13, 12, chunkBox);
                this.addBlock(world, lv2, 0, 13, i + 1, chunkBox);
                this.addBlock(world, lv2, 12, 13, i + 1, chunkBox);
            }
            this.addBlock(world, (BlockState)((BlockState)Blocks.NETHER_BRICK_FENCE.getDefaultState().with(FenceBlock.NORTH, true)).with(FenceBlock.EAST, true), 0, 13, 0, chunkBox);
            this.addBlock(world, (BlockState)((BlockState)Blocks.NETHER_BRICK_FENCE.getDefaultState().with(FenceBlock.SOUTH, true)).with(FenceBlock.EAST, true), 0, 13, 12, chunkBox);
            this.addBlock(world, (BlockState)((BlockState)Blocks.NETHER_BRICK_FENCE.getDefaultState().with(FenceBlock.SOUTH, true)).with(FenceBlock.WEST, true), 12, 13, 12, chunkBox);
            this.addBlock(world, (BlockState)((BlockState)Blocks.NETHER_BRICK_FENCE.getDefaultState().with(FenceBlock.NORTH, true)).with(FenceBlock.WEST, true), 12, 13, 0, chunkBox);
            for (i = 3; i <= 9; i += 2) {
                this.fillWithOutline(world, chunkBox, 1, 7, i, 1, 8, i, (BlockState)lv2.with(FenceBlock.WEST, true), (BlockState)lv2.with(FenceBlock.WEST, true), false);
                this.fillWithOutline(world, chunkBox, 11, 7, i, 11, 8, i, (BlockState)lv2.with(FenceBlock.EAST, true), (BlockState)lv2.with(FenceBlock.EAST, true), false);
            }
            this.fillWithOutline(world, chunkBox, 4, 2, 0, 8, 2, 12, Blocks.NETHER_BRICKS.getDefaultState(), Blocks.NETHER_BRICKS.getDefaultState(), false);
            this.fillWithOutline(world, chunkBox, 0, 2, 4, 12, 2, 8, Blocks.NETHER_BRICKS.getDefaultState(), Blocks.NETHER_BRICKS.getDefaultState(), false);
            this.fillWithOutline(world, chunkBox, 4, 0, 0, 8, 1, 3, Blocks.NETHER_BRICKS.getDefaultState(), Blocks.NETHER_BRICKS.getDefaultState(), false);
            this.fillWithOutline(world, chunkBox, 4, 0, 9, 8, 1, 12, Blocks.NETHER_BRICKS.getDefaultState(), Blocks.NETHER_BRICKS.getDefaultState(), false);
            this.fillWithOutline(world, chunkBox, 0, 0, 4, 3, 1, 8, Blocks.NETHER_BRICKS.getDefaultState(), Blocks.NETHER_BRICKS.getDefaultState(), false);
            this.fillWithOutline(world, chunkBox, 9, 0, 4, 12, 1, 8, Blocks.NETHER_BRICKS.getDefaultState(), Blocks.NETHER_BRICKS.getDefaultState(), false);
            for (i = 4; i <= 8; ++i) {
                for (j = 0; j <= 2; ++j) {
                    this.fillDownwards(world, Blocks.NETHER_BRICKS.getDefaultState(), i, -1, j, chunkBox);
                    this.fillDownwards(world, Blocks.NETHER_BRICKS.getDefaultState(), i, -1, 12 - j, chunkBox);
                }
            }
            for (i = 0; i <= 2; ++i) {
                for (j = 4; j <= 8; ++j) {
                    this.fillDownwards(world, Blocks.NETHER_BRICKS.getDefaultState(), i, -1, j, chunkBox);
                    this.fillDownwards(world, Blocks.NETHER_BRICKS.getDefaultState(), 12 - i, -1, j, chunkBox);
                }
            }
            this.fillWithOutline(world, chunkBox, 5, 5, 5, 7, 5, 7, Blocks.NETHER_BRICKS.getDefaultState(), Blocks.NETHER_BRICKS.getDefaultState(), false);
            this.fillWithOutline(world, chunkBox, 6, 1, 6, 6, 4, 6, Blocks.AIR.getDefaultState(), Blocks.AIR.getDefaultState(), false);
            this.addBlock(world, Blocks.NETHER_BRICKS.getDefaultState(), 6, 0, 6, chunkBox);
            this.addBlock(world, Blocks.LAVA.getDefaultState(), 6, 5, 6, chunkBox);
            BlockPos.Mutable lv3 = this.offsetPos(6, 5, 6);
            if (chunkBox.contains(lv3)) {
                world.scheduleFluidTick(lv3, Fluids.LAVA, 0);
            }
        }
    }

    public static class SmallCorridor
    extends Piece {
        private static final int SIZE_X = 5;
        private static final int SIZE_Y = 7;
        private static final int SIZE_Z = 5;

        public SmallCorridor(int chainLength, BlockBox boundingBox, Direction orientation) {
            super(StructurePieceType.NETHER_FORTRESS_SMALL_CORRIDOR, chainLength, boundingBox);
            this.setOrientation(orientation);
        }

        public SmallCorridor(NbtCompound nbt) {
            super(StructurePieceType.NETHER_FORTRESS_SMALL_CORRIDOR, nbt);
        }

        @Override
        public void fillOpenings(StructurePiece start, StructurePiecesHolder holder, Random random) {
            this.fillForwardOpening((Start)start, holder, random, 1, 0, true);
        }

        public static SmallCorridor create(StructurePiecesHolder holder, int x, int y, int z, Direction orientation, int chainLength) {
            BlockBox lv = BlockBox.rotated(x, y, z, -1, 0, 0, 5, 7, 5, orientation);
            if (!SmallCorridor.isInBounds(lv) || holder.getIntersecting(lv) != null) {
                return null;
            }
            return new SmallCorridor(chainLength, lv, orientation);
        }

        @Override
        public void generate(StructureWorldAccess world, StructureAccessor structureAccessor, ChunkGenerator chunkGenerator, Random random, BlockBox chunkBox, ChunkPos chunkPos, BlockPos pivot) {
            this.fillWithOutline(world, chunkBox, 0, 0, 0, 4, 1, 4, Blocks.NETHER_BRICKS.getDefaultState(), Blocks.NETHER_BRICKS.getDefaultState(), false);
            this.fillWithOutline(world, chunkBox, 0, 2, 0, 4, 5, 4, Blocks.AIR.getDefaultState(), Blocks.AIR.getDefaultState(), false);
            BlockState lv = (BlockState)((BlockState)Blocks.NETHER_BRICK_FENCE.getDefaultState().with(FenceBlock.NORTH, true)).with(FenceBlock.SOUTH, true);
            this.fillWithOutline(world, chunkBox, 0, 2, 0, 0, 5, 4, Blocks.NETHER_BRICKS.getDefaultState(), Blocks.NETHER_BRICKS.getDefaultState(), false);
            this.fillWithOutline(world, chunkBox, 4, 2, 0, 4, 5, 4, Blocks.NETHER_BRICKS.getDefaultState(), Blocks.NETHER_BRICKS.getDefaultState(), false);
            this.fillWithOutline(world, chunkBox, 0, 3, 1, 0, 4, 1, lv, lv, false);
            this.fillWithOutline(world, chunkBox, 0, 3, 3, 0, 4, 3, lv, lv, false);
            this.fillWithOutline(world, chunkBox, 4, 3, 1, 4, 4, 1, lv, lv, false);
            this.fillWithOutline(world, chunkBox, 4, 3, 3, 4, 4, 3, lv, lv, false);
            this.fillWithOutline(world, chunkBox, 0, 6, 0, 4, 6, 4, Blocks.NETHER_BRICKS.getDefaultState(), Blocks.NETHER_BRICKS.getDefaultState(), false);
            for (int i = 0; i <= 4; ++i) {
                for (int j = 0; j <= 4; ++j) {
                    this.fillDownwards(world, Blocks.NETHER_BRICKS.getDefaultState(), i, -1, j, chunkBox);
                }
            }
        }
    }

    public static class CorridorRightTurn
    extends Piece {
        private static final int SIZE_X = 5;
        private static final int SIZE_Y = 7;
        private static final int SIZE_Z = 5;
        private boolean containsChest;

        public CorridorRightTurn(int chainLength, Random random, BlockBox boundingBox, Direction orientation) {
            super(StructurePieceType.NETHER_FORTRESS_CORRIDOR_RIGHT_TURN, chainLength, boundingBox);
            this.setOrientation(orientation);
            this.containsChest = random.nextInt(3) == 0;
        }

        public CorridorRightTurn(NbtCompound nbt) {
            super(StructurePieceType.NETHER_FORTRESS_CORRIDOR_RIGHT_TURN, nbt);
            this.containsChest = nbt.getBoolean("Chest");
        }

        @Override
        protected void writeNbt(StructureContext context, NbtCompound nbt) {
            super.writeNbt(context, nbt);
            nbt.putBoolean("Chest", this.containsChest);
        }

        @Override
        public void fillOpenings(StructurePiece start, StructurePiecesHolder holder, Random random) {
            this.fillSEOpening((Start)start, holder, random, 0, 1, true);
        }

        public static CorridorRightTurn create(StructurePiecesHolder holder, Random random, int x, int y, int z, Direction orientation, int chainLength) {
            BlockBox lv = BlockBox.rotated(x, y, z, -1, 0, 0, 5, 7, 5, orientation);
            if (!CorridorRightTurn.isInBounds(lv) || holder.getIntersecting(lv) != null) {
                return null;
            }
            return new CorridorRightTurn(chainLength, random, lv, orientation);
        }

        @Override
        public void generate(StructureWorldAccess world, StructureAccessor structureAccessor, ChunkGenerator chunkGenerator, Random random, BlockBox chunkBox, ChunkPos chunkPos, BlockPos pivot) {
            this.fillWithOutline(world, chunkBox, 0, 0, 0, 4, 1, 4, Blocks.NETHER_BRICKS.getDefaultState(), Blocks.NETHER_BRICKS.getDefaultState(), false);
            this.fillWithOutline(world, chunkBox, 0, 2, 0, 4, 5, 4, Blocks.AIR.getDefaultState(), Blocks.AIR.getDefaultState(), false);
            BlockState lv = (BlockState)((BlockState)Blocks.NETHER_BRICK_FENCE.getDefaultState().with(FenceBlock.WEST, true)).with(FenceBlock.EAST, true);
            BlockState lv2 = (BlockState)((BlockState)Blocks.NETHER_BRICK_FENCE.getDefaultState().with(FenceBlock.NORTH, true)).with(FenceBlock.SOUTH, true);
            this.fillWithOutline(world, chunkBox, 0, 2, 0, 0, 5, 4, Blocks.NETHER_BRICKS.getDefaultState(), Blocks.NETHER_BRICKS.getDefaultState(), false);
            this.fillWithOutline(world, chunkBox, 0, 3, 1, 0, 4, 1, lv2, lv2, false);
            this.fillWithOutline(world, chunkBox, 0, 3, 3, 0, 4, 3, lv2, lv2, false);
            this.fillWithOutline(world, chunkBox, 4, 2, 0, 4, 5, 0, Blocks.NETHER_BRICKS.getDefaultState(), Blocks.NETHER_BRICKS.getDefaultState(), false);
            this.fillWithOutline(world, chunkBox, 1, 2, 4, 4, 5, 4, Blocks.NETHER_BRICKS.getDefaultState(), Blocks.NETHER_BRICKS.getDefaultState(), false);
            this.fillWithOutline(world, chunkBox, 1, 3, 4, 1, 4, 4, lv, lv, false);
            this.fillWithOutline(world, chunkBox, 3, 3, 4, 3, 4, 4, lv, lv, false);
            if (this.containsChest && chunkBox.contains(this.offsetPos(1, 2, 3))) {
                this.containsChest = false;
                this.addChest(world, chunkBox, random, 1, 2, 3, LootTables.NETHER_BRIDGE_CHEST);
            }
            this.fillWithOutline(world, chunkBox, 0, 6, 0, 4, 6, 4, Blocks.NETHER_BRICKS.getDefaultState(), Blocks.NETHER_BRICKS.getDefaultState(), false);
            for (int i = 0; i <= 4; ++i) {
                for (int j = 0; j <= 4; ++j) {
                    this.fillDownwards(world, Blocks.NETHER_BRICKS.getDefaultState(), i, -1, j, chunkBox);
                }
            }
        }
    }

    public static class CorridorLeftTurn
    extends Piece {
        private static final int SIZE_X = 5;
        private static final int SIZE_Y = 7;
        private static final int SIZE_Z = 5;
        private boolean containsChest;

        public CorridorLeftTurn(int chainLength, Random random, BlockBox boundingBox, Direction orientation) {
            super(StructurePieceType.NETHER_FORTRESS_CORRIDOR_LEFT_TURN, chainLength, boundingBox);
            this.setOrientation(orientation);
            this.containsChest = random.nextInt(3) == 0;
        }

        public CorridorLeftTurn(NbtCompound nbt) {
            super(StructurePieceType.NETHER_FORTRESS_CORRIDOR_LEFT_TURN, nbt);
            this.containsChest = nbt.getBoolean("Chest");
        }

        @Override
        protected void writeNbt(StructureContext context, NbtCompound nbt) {
            super.writeNbt(context, nbt);
            nbt.putBoolean("Chest", this.containsChest);
        }

        @Override
        public void fillOpenings(StructurePiece start, StructurePiecesHolder holder, Random random) {
            this.fillNWOpening((Start)start, holder, random, 0, 1, true);
        }

        public static CorridorLeftTurn create(StructurePiecesHolder holder, Random random, int x, int y, int z, Direction orientation, int chainLength) {
            BlockBox lv = BlockBox.rotated(x, y, z, -1, 0, 0, 5, 7, 5, orientation);
            if (!CorridorLeftTurn.isInBounds(lv) || holder.getIntersecting(lv) != null) {
                return null;
            }
            return new CorridorLeftTurn(chainLength, random, lv, orientation);
        }

        @Override
        public void generate(StructureWorldAccess world, StructureAccessor structureAccessor, ChunkGenerator chunkGenerator, Random random, BlockBox chunkBox, ChunkPos chunkPos, BlockPos pivot) {
            this.fillWithOutline(world, chunkBox, 0, 0, 0, 4, 1, 4, Blocks.NETHER_BRICKS.getDefaultState(), Blocks.NETHER_BRICKS.getDefaultState(), false);
            this.fillWithOutline(world, chunkBox, 0, 2, 0, 4, 5, 4, Blocks.AIR.getDefaultState(), Blocks.AIR.getDefaultState(), false);
            BlockState lv = (BlockState)((BlockState)Blocks.NETHER_BRICK_FENCE.getDefaultState().with(FenceBlock.WEST, true)).with(FenceBlock.EAST, true);
            BlockState lv2 = (BlockState)((BlockState)Blocks.NETHER_BRICK_FENCE.getDefaultState().with(FenceBlock.NORTH, true)).with(FenceBlock.SOUTH, true);
            this.fillWithOutline(world, chunkBox, 4, 2, 0, 4, 5, 4, Blocks.NETHER_BRICKS.getDefaultState(), Blocks.NETHER_BRICKS.getDefaultState(), false);
            this.fillWithOutline(world, chunkBox, 4, 3, 1, 4, 4, 1, lv2, lv2, false);
            this.fillWithOutline(world, chunkBox, 4, 3, 3, 4, 4, 3, lv2, lv2, false);
            this.fillWithOutline(world, chunkBox, 0, 2, 0, 0, 5, 0, Blocks.NETHER_BRICKS.getDefaultState(), Blocks.NETHER_BRICKS.getDefaultState(), false);
            this.fillWithOutline(world, chunkBox, 0, 2, 4, 3, 5, 4, Blocks.NETHER_BRICKS.getDefaultState(), Blocks.NETHER_BRICKS.getDefaultState(), false);
            this.fillWithOutline(world, chunkBox, 1, 3, 4, 1, 4, 4, lv, lv, false);
            this.fillWithOutline(world, chunkBox, 3, 3, 4, 3, 4, 4, lv, lv, false);
            if (this.containsChest && chunkBox.contains(this.offsetPos(3, 2, 3))) {
                this.containsChest = false;
                this.addChest(world, chunkBox, random, 3, 2, 3, LootTables.NETHER_BRIDGE_CHEST);
            }
            this.fillWithOutline(world, chunkBox, 0, 6, 0, 4, 6, 4, Blocks.NETHER_BRICKS.getDefaultState(), Blocks.NETHER_BRICKS.getDefaultState(), false);
            for (int i = 0; i <= 4; ++i) {
                for (int j = 0; j <= 4; ++j) {
                    this.fillDownwards(world, Blocks.NETHER_BRICKS.getDefaultState(), i, -1, j, chunkBox);
                }
            }
        }
    }

    public static class CorridorStairs
    extends Piece {
        private static final int SIZE_X = 5;
        private static final int SIZE_Y = 14;
        private static final int SIZE_Z = 10;

        public CorridorStairs(int chainLength, BlockBox boundingBox, Direction orientation) {
            super(StructurePieceType.NETHER_FORTRESS_CORRIDOR_STAIRS, chainLength, boundingBox);
            this.setOrientation(orientation);
        }

        public CorridorStairs(NbtCompound nbt) {
            super(StructurePieceType.NETHER_FORTRESS_CORRIDOR_STAIRS, nbt);
        }

        @Override
        public void fillOpenings(StructurePiece start, StructurePiecesHolder holder, Random random) {
            this.fillForwardOpening((Start)start, holder, random, 1, 0, true);
        }

        public static CorridorStairs create(StructurePiecesHolder holder, int x, int y, int z, Direction orientation, int chainLength) {
            BlockBox lv = BlockBox.rotated(x, y, z, -1, -7, 0, 5, 14, 10, orientation);
            if (!CorridorStairs.isInBounds(lv) || holder.getIntersecting(lv) != null) {
                return null;
            }
            return new CorridorStairs(chainLength, lv, orientation);
        }

        @Override
        public void generate(StructureWorldAccess world, StructureAccessor structureAccessor, ChunkGenerator chunkGenerator, Random random, BlockBox chunkBox, ChunkPos chunkPos, BlockPos pivot) {
            BlockState lv = (BlockState)Blocks.NETHER_BRICK_STAIRS.getDefaultState().with(StairsBlock.FACING, Direction.SOUTH);
            BlockState lv2 = (BlockState)((BlockState)Blocks.NETHER_BRICK_FENCE.getDefaultState().with(FenceBlock.NORTH, true)).with(FenceBlock.SOUTH, true);
            for (int i = 0; i <= 9; ++i) {
                int j = Math.max(1, 7 - i);
                int k = Math.min(Math.max(j + 5, 14 - i), 13);
                int l = i;
                this.fillWithOutline(world, chunkBox, 0, 0, l, 4, j, l, Blocks.NETHER_BRICKS.getDefaultState(), Blocks.NETHER_BRICKS.getDefaultState(), false);
                this.fillWithOutline(world, chunkBox, 1, j + 1, l, 3, k - 1, l, Blocks.AIR.getDefaultState(), Blocks.AIR.getDefaultState(), false);
                if (i <= 6) {
                    this.addBlock(world, lv, 1, j + 1, l, chunkBox);
                    this.addBlock(world, lv, 2, j + 1, l, chunkBox);
                    this.addBlock(world, lv, 3, j + 1, l, chunkBox);
                }
                this.fillWithOutline(world, chunkBox, 0, k, l, 4, k, l, Blocks.NETHER_BRICKS.getDefaultState(), Blocks.NETHER_BRICKS.getDefaultState(), false);
                this.fillWithOutline(world, chunkBox, 0, j + 1, l, 0, k - 1, l, Blocks.NETHER_BRICKS.getDefaultState(), Blocks.NETHER_BRICKS.getDefaultState(), false);
                this.fillWithOutline(world, chunkBox, 4, j + 1, l, 4, k - 1, l, Blocks.NETHER_BRICKS.getDefaultState(), Blocks.NETHER_BRICKS.getDefaultState(), false);
                if ((i & 1) == 0) {
                    this.fillWithOutline(world, chunkBox, 0, j + 2, l, 0, j + 3, l, lv2, lv2, false);
                    this.fillWithOutline(world, chunkBox, 4, j + 2, l, 4, j + 3, l, lv2, lv2, false);
                }
                for (int m = 0; m <= 4; ++m) {
                    this.fillDownwards(world, Blocks.NETHER_BRICKS.getDefaultState(), m, -1, l, chunkBox);
                }
            }
        }
    }

    public static class CorridorBalcony
    extends Piece {
        private static final int SIZE_X = 9;
        private static final int SIZE_Y = 7;
        private static final int SIZE_Z = 9;

        public CorridorBalcony(int chainLength, BlockBox boundingBox, Direction orientation) {
            super(StructurePieceType.NETHER_FORTRESS_CORRIDOR_BALCONY, chainLength, boundingBox);
            this.setOrientation(orientation);
        }

        public CorridorBalcony(NbtCompound nbt) {
            super(StructurePieceType.NETHER_FORTRESS_CORRIDOR_BALCONY, nbt);
        }

        @Override
        public void fillOpenings(StructurePiece start, StructurePiecesHolder holder, Random random) {
            int i = 1;
            Direction lv = this.getFacing();
            if (lv == Direction.WEST || lv == Direction.NORTH) {
                i = 5;
            }
            this.fillNWOpening((Start)start, holder, random, 0, i, random.nextInt(8) > 0);
            this.fillSEOpening((Start)start, holder, random, 0, i, random.nextInt(8) > 0);
        }

        public static CorridorBalcony create(StructurePiecesHolder holder, int x, int y, int z, Direction orientation, int chainLength) {
            BlockBox lv = BlockBox.rotated(x, y, z, -3, 0, 0, 9, 7, 9, orientation);
            if (!CorridorBalcony.isInBounds(lv) || holder.getIntersecting(lv) != null) {
                return null;
            }
            return new CorridorBalcony(chainLength, lv, orientation);
        }

        @Override
        public void generate(StructureWorldAccess world, StructureAccessor structureAccessor, ChunkGenerator chunkGenerator, Random random, BlockBox chunkBox, ChunkPos chunkPos, BlockPos pivot) {
            BlockState lv = (BlockState)((BlockState)Blocks.NETHER_BRICK_FENCE.getDefaultState().with(FenceBlock.NORTH, true)).with(FenceBlock.SOUTH, true);
            BlockState lv2 = (BlockState)((BlockState)Blocks.NETHER_BRICK_FENCE.getDefaultState().with(FenceBlock.WEST, true)).with(FenceBlock.EAST, true);
            this.fillWithOutline(world, chunkBox, 0, 0, 0, 8, 1, 8, Blocks.NETHER_BRICKS.getDefaultState(), Blocks.NETHER_BRICKS.getDefaultState(), false);
            this.fillWithOutline(world, chunkBox, 0, 2, 0, 8, 5, 8, Blocks.AIR.getDefaultState(), Blocks.AIR.getDefaultState(), false);
            this.fillWithOutline(world, chunkBox, 0, 6, 0, 8, 6, 5, Blocks.NETHER_BRICKS.getDefaultState(), Blocks.NETHER_BRICKS.getDefaultState(), false);
            this.fillWithOutline(world, chunkBox, 0, 2, 0, 2, 5, 0, Blocks.NETHER_BRICKS.getDefaultState(), Blocks.NETHER_BRICKS.getDefaultState(), false);
            this.fillWithOutline(world, chunkBox, 6, 2, 0, 8, 5, 0, Blocks.NETHER_BRICKS.getDefaultState(), Blocks.NETHER_BRICKS.getDefaultState(), false);
            this.fillWithOutline(world, chunkBox, 1, 3, 0, 1, 4, 0, lv2, lv2, false);
            this.fillWithOutline(world, chunkBox, 7, 3, 0, 7, 4, 0, lv2, lv2, false);
            this.fillWithOutline(world, chunkBox, 0, 2, 4, 8, 2, 8, Blocks.NETHER_BRICKS.getDefaultState(), Blocks.NETHER_BRICKS.getDefaultState(), false);
            this.fillWithOutline(world, chunkBox, 1, 1, 4, 2, 2, 4, Blocks.AIR.getDefaultState(), Blocks.AIR.getDefaultState(), false);
            this.fillWithOutline(world, chunkBox, 6, 1, 4, 7, 2, 4, Blocks.AIR.getDefaultState(), Blocks.AIR.getDefaultState(), false);
            this.fillWithOutline(world, chunkBox, 1, 3, 8, 7, 3, 8, lv2, lv2, false);
            this.addBlock(world, (BlockState)((BlockState)Blocks.NETHER_BRICK_FENCE.getDefaultState().with(FenceBlock.EAST, true)).with(FenceBlock.SOUTH, true), 0, 3, 8, chunkBox);
            this.addBlock(world, (BlockState)((BlockState)Blocks.NETHER_BRICK_FENCE.getDefaultState().with(FenceBlock.WEST, true)).with(FenceBlock.SOUTH, true), 8, 3, 8, chunkBox);
            this.fillWithOutline(world, chunkBox, 0, 3, 6, 0, 3, 7, lv, lv, false);
            this.fillWithOutline(world, chunkBox, 8, 3, 6, 8, 3, 7, lv, lv, false);
            this.fillWithOutline(world, chunkBox, 0, 3, 4, 0, 5, 5, Blocks.NETHER_BRICKS.getDefaultState(), Blocks.NETHER_BRICKS.getDefaultState(), false);
            this.fillWithOutline(world, chunkBox, 8, 3, 4, 8, 5, 5, Blocks.NETHER_BRICKS.getDefaultState(), Blocks.NETHER_BRICKS.getDefaultState(), false);
            this.fillWithOutline(world, chunkBox, 1, 3, 5, 2, 5, 5, Blocks.NETHER_BRICKS.getDefaultState(), Blocks.NETHER_BRICKS.getDefaultState(), false);
            this.fillWithOutline(world, chunkBox, 6, 3, 5, 7, 5, 5, Blocks.NETHER_BRICKS.getDefaultState(), Blocks.NETHER_BRICKS.getDefaultState(), false);
            this.fillWithOutline(world, chunkBox, 1, 4, 5, 1, 5, 5, lv2, lv2, false);
            this.fillWithOutline(world, chunkBox, 7, 4, 5, 7, 5, 5, lv2, lv2, false);
            for (int i = 0; i <= 5; ++i) {
                for (int j = 0; j <= 8; ++j) {
                    this.fillDownwards(world, Blocks.NETHER_BRICKS.getDefaultState(), j, -1, i, chunkBox);
                }
            }
        }
    }

    public static class CorridorCrossing
    extends Piece {
        private static final int SIZE_X = 5;
        private static final int SIZE_Y = 7;
        private static final int SIZE_Z = 5;

        public CorridorCrossing(int chainLength, BlockBox boundingBox, Direction orientation) {
            super(StructurePieceType.NETHER_FORTRESS_CORRIDOR_CROSSING, chainLength, boundingBox);
            this.setOrientation(orientation);
        }

        public CorridorCrossing(NbtCompound nbt) {
            super(StructurePieceType.NETHER_FORTRESS_CORRIDOR_CROSSING, nbt);
        }

        @Override
        public void fillOpenings(StructurePiece start, StructurePiecesHolder holder, Random random) {
            this.fillForwardOpening((Start)start, holder, random, 1, 0, true);
            this.fillNWOpening((Start)start, holder, random, 0, 1, true);
            this.fillSEOpening((Start)start, holder, random, 0, 1, true);
        }

        public static CorridorCrossing create(StructurePiecesHolder holder, int x, int y, int z, Direction orientation, int chainLength) {
            BlockBox lv = BlockBox.rotated(x, y, z, -1, 0, 0, 5, 7, 5, orientation);
            if (!CorridorCrossing.isInBounds(lv) || holder.getIntersecting(lv) != null) {
                return null;
            }
            return new CorridorCrossing(chainLength, lv, orientation);
        }

        @Override
        public void generate(StructureWorldAccess world, StructureAccessor structureAccessor, ChunkGenerator chunkGenerator, Random random, BlockBox chunkBox, ChunkPos chunkPos, BlockPos pivot) {
            this.fillWithOutline(world, chunkBox, 0, 0, 0, 4, 1, 4, Blocks.NETHER_BRICKS.getDefaultState(), Blocks.NETHER_BRICKS.getDefaultState(), false);
            this.fillWithOutline(world, chunkBox, 0, 2, 0, 4, 5, 4, Blocks.AIR.getDefaultState(), Blocks.AIR.getDefaultState(), false);
            this.fillWithOutline(world, chunkBox, 0, 2, 0, 0, 5, 0, Blocks.NETHER_BRICKS.getDefaultState(), Blocks.NETHER_BRICKS.getDefaultState(), false);
            this.fillWithOutline(world, chunkBox, 4, 2, 0, 4, 5, 0, Blocks.NETHER_BRICKS.getDefaultState(), Blocks.NETHER_BRICKS.getDefaultState(), false);
            this.fillWithOutline(world, chunkBox, 0, 2, 4, 0, 5, 4, Blocks.NETHER_BRICKS.getDefaultState(), Blocks.NETHER_BRICKS.getDefaultState(), false);
            this.fillWithOutline(world, chunkBox, 4, 2, 4, 4, 5, 4, Blocks.NETHER_BRICKS.getDefaultState(), Blocks.NETHER_BRICKS.getDefaultState(), false);
            this.fillWithOutline(world, chunkBox, 0, 6, 0, 4, 6, 4, Blocks.NETHER_BRICKS.getDefaultState(), Blocks.NETHER_BRICKS.getDefaultState(), false);
            for (int i = 0; i <= 4; ++i) {
                for (int j = 0; j <= 4; ++j) {
                    this.fillDownwards(world, Blocks.NETHER_BRICKS.getDefaultState(), i, -1, j, chunkBox);
                }
            }
        }
    }

    public static class CorridorNetherWartsRoom
    extends Piece {
        private static final int SIZE_X = 13;
        private static final int SIZE_Y = 14;
        private static final int SIZE_Z = 13;

        public CorridorNetherWartsRoom(int chainLength, BlockBox boundingBox, Direction orientation) {
            super(StructurePieceType.NETHER_FORTRESS_CORRIDOR_NETHER_WARTS_ROOM, chainLength, boundingBox);
            this.setOrientation(orientation);
        }

        public CorridorNetherWartsRoom(NbtCompound nbt) {
            super(StructurePieceType.NETHER_FORTRESS_CORRIDOR_NETHER_WARTS_ROOM, nbt);
        }

        @Override
        public void fillOpenings(StructurePiece start, StructurePiecesHolder holder, Random random) {
            this.fillForwardOpening((Start)start, holder, random, 5, 3, true);
            this.fillForwardOpening((Start)start, holder, random, 5, 11, true);
        }

        public static CorridorNetherWartsRoom create(StructurePiecesHolder holder, int x, int y, int z, Direction orientation, int chainlength) {
            BlockBox lv = BlockBox.rotated(x, y, z, -5, -3, 0, 13, 14, 13, orientation);
            if (!CorridorNetherWartsRoom.isInBounds(lv) || holder.getIntersecting(lv) != null) {
                return null;
            }
            return new CorridorNetherWartsRoom(chainlength, lv, orientation);
        }

        @Override
        public void generate(StructureWorldAccess world, StructureAccessor structureAccessor, ChunkGenerator chunkGenerator, Random random, BlockBox chunkBox, ChunkPos chunkPos, BlockPos pivot) {
            int m;
            int l;
            int j;
            int i;
            this.fillWithOutline(world, chunkBox, 0, 3, 0, 12, 4, 12, Blocks.NETHER_BRICKS.getDefaultState(), Blocks.NETHER_BRICKS.getDefaultState(), false);
            this.fillWithOutline(world, chunkBox, 0, 5, 0, 12, 13, 12, Blocks.AIR.getDefaultState(), Blocks.AIR.getDefaultState(), false);
            this.fillWithOutline(world, chunkBox, 0, 5, 0, 1, 12, 12, Blocks.NETHER_BRICKS.getDefaultState(), Blocks.NETHER_BRICKS.getDefaultState(), false);
            this.fillWithOutline(world, chunkBox, 11, 5, 0, 12, 12, 12, Blocks.NETHER_BRICKS.getDefaultState(), Blocks.NETHER_BRICKS.getDefaultState(), false);
            this.fillWithOutline(world, chunkBox, 2, 5, 11, 4, 12, 12, Blocks.NETHER_BRICKS.getDefaultState(), Blocks.NETHER_BRICKS.getDefaultState(), false);
            this.fillWithOutline(world, chunkBox, 8, 5, 11, 10, 12, 12, Blocks.NETHER_BRICKS.getDefaultState(), Blocks.NETHER_BRICKS.getDefaultState(), false);
            this.fillWithOutline(world, chunkBox, 5, 9, 11, 7, 12, 12, Blocks.NETHER_BRICKS.getDefaultState(), Blocks.NETHER_BRICKS.getDefaultState(), false);
            this.fillWithOutline(world, chunkBox, 2, 5, 0, 4, 12, 1, Blocks.NETHER_BRICKS.getDefaultState(), Blocks.NETHER_BRICKS.getDefaultState(), false);
            this.fillWithOutline(world, chunkBox, 8, 5, 0, 10, 12, 1, Blocks.NETHER_BRICKS.getDefaultState(), Blocks.NETHER_BRICKS.getDefaultState(), false);
            this.fillWithOutline(world, chunkBox, 5, 9, 0, 7, 12, 1, Blocks.NETHER_BRICKS.getDefaultState(), Blocks.NETHER_BRICKS.getDefaultState(), false);
            this.fillWithOutline(world, chunkBox, 2, 11, 2, 10, 12, 10, Blocks.NETHER_BRICKS.getDefaultState(), Blocks.NETHER_BRICKS.getDefaultState(), false);
            BlockState lv = (BlockState)((BlockState)Blocks.NETHER_BRICK_FENCE.getDefaultState().with(FenceBlock.WEST, true)).with(FenceBlock.EAST, true);
            BlockState lv2 = (BlockState)((BlockState)Blocks.NETHER_BRICK_FENCE.getDefaultState().with(FenceBlock.NORTH, true)).with(FenceBlock.SOUTH, true);
            BlockState lv3 = (BlockState)lv2.with(FenceBlock.WEST, true);
            BlockState lv4 = (BlockState)lv2.with(FenceBlock.EAST, true);
            for (i = 1; i <= 11; i += 2) {
                this.fillWithOutline(world, chunkBox, i, 10, 0, i, 11, 0, lv, lv, false);
                this.fillWithOutline(world, chunkBox, i, 10, 12, i, 11, 12, lv, lv, false);
                this.fillWithOutline(world, chunkBox, 0, 10, i, 0, 11, i, lv2, lv2, false);
                this.fillWithOutline(world, chunkBox, 12, 10, i, 12, 11, i, lv2, lv2, false);
                this.addBlock(world, Blocks.NETHER_BRICKS.getDefaultState(), i, 13, 0, chunkBox);
                this.addBlock(world, Blocks.NETHER_BRICKS.getDefaultState(), i, 13, 12, chunkBox);
                this.addBlock(world, Blocks.NETHER_BRICKS.getDefaultState(), 0, 13, i, chunkBox);
                this.addBlock(world, Blocks.NETHER_BRICKS.getDefaultState(), 12, 13, i, chunkBox);
                if (i == 11) continue;
                this.addBlock(world, lv, i + 1, 13, 0, chunkBox);
                this.addBlock(world, lv, i + 1, 13, 12, chunkBox);
                this.addBlock(world, lv2, 0, 13, i + 1, chunkBox);
                this.addBlock(world, lv2, 12, 13, i + 1, chunkBox);
            }
            this.addBlock(world, (BlockState)((BlockState)Blocks.NETHER_BRICK_FENCE.getDefaultState().with(FenceBlock.NORTH, true)).with(FenceBlock.EAST, true), 0, 13, 0, chunkBox);
            this.addBlock(world, (BlockState)((BlockState)Blocks.NETHER_BRICK_FENCE.getDefaultState().with(FenceBlock.SOUTH, true)).with(FenceBlock.EAST, true), 0, 13, 12, chunkBox);
            this.addBlock(world, (BlockState)((BlockState)Blocks.NETHER_BRICK_FENCE.getDefaultState().with(FenceBlock.SOUTH, true)).with(FenceBlock.WEST, true), 12, 13, 12, chunkBox);
            this.addBlock(world, (BlockState)((BlockState)Blocks.NETHER_BRICK_FENCE.getDefaultState().with(FenceBlock.NORTH, true)).with(FenceBlock.WEST, true), 12, 13, 0, chunkBox);
            for (i = 3; i <= 9; i += 2) {
                this.fillWithOutline(world, chunkBox, 1, 7, i, 1, 8, i, lv3, lv3, false);
                this.fillWithOutline(world, chunkBox, 11, 7, i, 11, 8, i, lv4, lv4, false);
            }
            BlockState lv5 = (BlockState)Blocks.NETHER_BRICK_STAIRS.getDefaultState().with(StairsBlock.FACING, Direction.NORTH);
            for (j = 0; j <= 6; ++j) {
                int k = j + 4;
                for (l = 5; l <= 7; ++l) {
                    this.addBlock(world, lv5, l, 5 + j, k, chunkBox);
                }
                if (k >= 5 && k <= 8) {
                    this.fillWithOutline(world, chunkBox, 5, 5, k, 7, j + 4, k, Blocks.NETHER_BRICKS.getDefaultState(), Blocks.NETHER_BRICKS.getDefaultState(), false);
                } else if (k >= 9 && k <= 10) {
                    this.fillWithOutline(world, chunkBox, 5, 8, k, 7, j + 4, k, Blocks.NETHER_BRICKS.getDefaultState(), Blocks.NETHER_BRICKS.getDefaultState(), false);
                }
                if (j < 1) continue;
                this.fillWithOutline(world, chunkBox, 5, 6 + j, k, 7, 9 + j, k, Blocks.AIR.getDefaultState(), Blocks.AIR.getDefaultState(), false);
            }
            for (j = 5; j <= 7; ++j) {
                this.addBlock(world, lv5, j, 12, 11, chunkBox);
            }
            this.fillWithOutline(world, chunkBox, 5, 6, 7, 5, 7, 7, lv4, lv4, false);
            this.fillWithOutline(world, chunkBox, 7, 6, 7, 7, 7, 7, lv3, lv3, false);
            this.fillWithOutline(world, chunkBox, 5, 13, 12, 7, 13, 12, Blocks.AIR.getDefaultState(), Blocks.AIR.getDefaultState(), false);
            this.fillWithOutline(world, chunkBox, 2, 5, 2, 3, 5, 3, Blocks.NETHER_BRICKS.getDefaultState(), Blocks.NETHER_BRICKS.getDefaultState(), false);
            this.fillWithOutline(world, chunkBox, 2, 5, 9, 3, 5, 10, Blocks.NETHER_BRICKS.getDefaultState(), Blocks.NETHER_BRICKS.getDefaultState(), false);
            this.fillWithOutline(world, chunkBox, 2, 5, 4, 2, 5, 8, Blocks.NETHER_BRICKS.getDefaultState(), Blocks.NETHER_BRICKS.getDefaultState(), false);
            this.fillWithOutline(world, chunkBox, 9, 5, 2, 10, 5, 3, Blocks.NETHER_BRICKS.getDefaultState(), Blocks.NETHER_BRICKS.getDefaultState(), false);
            this.fillWithOutline(world, chunkBox, 9, 5, 9, 10, 5, 10, Blocks.NETHER_BRICKS.getDefaultState(), Blocks.NETHER_BRICKS.getDefaultState(), false);
            this.fillWithOutline(world, chunkBox, 10, 5, 4, 10, 5, 8, Blocks.NETHER_BRICKS.getDefaultState(), Blocks.NETHER_BRICKS.getDefaultState(), false);
            BlockState lv6 = (BlockState)lv5.with(StairsBlock.FACING, Direction.EAST);
            BlockState lv7 = (BlockState)lv5.with(StairsBlock.FACING, Direction.WEST);
            this.addBlock(world, lv7, 4, 5, 2, chunkBox);
            this.addBlock(world, lv7, 4, 5, 3, chunkBox);
            this.addBlock(world, lv7, 4, 5, 9, chunkBox);
            this.addBlock(world, lv7, 4, 5, 10, chunkBox);
            this.addBlock(world, lv6, 8, 5, 2, chunkBox);
            this.addBlock(world, lv6, 8, 5, 3, chunkBox);
            this.addBlock(world, lv6, 8, 5, 9, chunkBox);
            this.addBlock(world, lv6, 8, 5, 10, chunkBox);
            this.fillWithOutline(world, chunkBox, 3, 4, 4, 4, 4, 8, Blocks.SOUL_SAND.getDefaultState(), Blocks.SOUL_SAND.getDefaultState(), false);
            this.fillWithOutline(world, chunkBox, 8, 4, 4, 9, 4, 8, Blocks.SOUL_SAND.getDefaultState(), Blocks.SOUL_SAND.getDefaultState(), false);
            this.fillWithOutline(world, chunkBox, 3, 5, 4, 4, 5, 8, Blocks.NETHER_WART.getDefaultState(), Blocks.NETHER_WART.getDefaultState(), false);
            this.fillWithOutline(world, chunkBox, 8, 5, 4, 9, 5, 8, Blocks.NETHER_WART.getDefaultState(), Blocks.NETHER_WART.getDefaultState(), false);
            this.fillWithOutline(world, chunkBox, 4, 2, 0, 8, 2, 12, Blocks.NETHER_BRICKS.getDefaultState(), Blocks.NETHER_BRICKS.getDefaultState(), false);
            this.fillWithOutline(world, chunkBox, 0, 2, 4, 12, 2, 8, Blocks.NETHER_BRICKS.getDefaultState(), Blocks.NETHER_BRICKS.getDefaultState(), false);
            this.fillWithOutline(world, chunkBox, 4, 0, 0, 8, 1, 3, Blocks.NETHER_BRICKS.getDefaultState(), Blocks.NETHER_BRICKS.getDefaultState(), false);
            this.fillWithOutline(world, chunkBox, 4, 0, 9, 8, 1, 12, Blocks.NETHER_BRICKS.getDefaultState(), Blocks.NETHER_BRICKS.getDefaultState(), false);
            this.fillWithOutline(world, chunkBox, 0, 0, 4, 3, 1, 8, Blocks.NETHER_BRICKS.getDefaultState(), Blocks.NETHER_BRICKS.getDefaultState(), false);
            this.fillWithOutline(world, chunkBox, 9, 0, 4, 12, 1, 8, Blocks.NETHER_BRICKS.getDefaultState(), Blocks.NETHER_BRICKS.getDefaultState(), false);
            for (l = 4; l <= 8; ++l) {
                for (m = 0; m <= 2; ++m) {
                    this.fillDownwards(world, Blocks.NETHER_BRICKS.getDefaultState(), l, -1, m, chunkBox);
                    this.fillDownwards(world, Blocks.NETHER_BRICKS.getDefaultState(), l, -1, 12 - m, chunkBox);
                }
            }
            for (l = 0; l <= 2; ++l) {
                for (m = 4; m <= 8; ++m) {
                    this.fillDownwards(world, Blocks.NETHER_BRICKS.getDefaultState(), l, -1, m, chunkBox);
                    this.fillDownwards(world, Blocks.NETHER_BRICKS.getDefaultState(), 12 - l, -1, m, chunkBox);
                }
            }
        }
    }

    public static class BridgeEnd
    extends Piece {
        private static final int SIZE_X = 5;
        private static final int SIZE_Y = 10;
        private static final int SIZE_Z = 8;
        private final int seed;

        public BridgeEnd(int chainLength, Random random, BlockBox boundingBox, Direction orientation) {
            super(StructurePieceType.NETHER_FORTRESS_BRIDGE_END, chainLength, boundingBox);
            this.setOrientation(orientation);
            this.seed = random.nextInt();
        }

        public BridgeEnd(NbtCompound nbt) {
            super(StructurePieceType.NETHER_FORTRESS_BRIDGE_END, nbt);
            this.seed = nbt.getInt("Seed");
        }

        public static BridgeEnd create(StructurePiecesHolder holder, Random random, int x, int y, int z, Direction orientation, int chainLength) {
            BlockBox lv = BlockBox.rotated(x, y, z, -1, -3, 0, 5, 10, 8, orientation);
            if (!BridgeEnd.isInBounds(lv) || holder.getIntersecting(lv) != null) {
                return null;
            }
            return new BridgeEnd(chainLength, random, lv, orientation);
        }

        @Override
        protected void writeNbt(StructureContext context, NbtCompound nbt) {
            super.writeNbt(context, nbt);
            nbt.putInt("Seed", this.seed);
        }

        @Override
        public void generate(StructureWorldAccess world, StructureAccessor structureAccessor, ChunkGenerator chunkGenerator, Random random, BlockBox chunkBox, ChunkPos chunkPos, BlockPos pivot) {
            int k;
            int j;
            int i;
            Random lv = Random.create(this.seed);
            for (i = 0; i <= 4; ++i) {
                for (j = 3; j <= 4; ++j) {
                    k = lv.nextInt(8);
                    this.fillWithOutline(world, chunkBox, i, j, 0, i, j, k, Blocks.NETHER_BRICKS.getDefaultState(), Blocks.NETHER_BRICKS.getDefaultState(), false);
                }
            }
            i = lv.nextInt(8);
            this.fillWithOutline(world, chunkBox, 0, 5, 0, 0, 5, i, Blocks.NETHER_BRICKS.getDefaultState(), Blocks.NETHER_BRICKS.getDefaultState(), false);
            i = lv.nextInt(8);
            this.fillWithOutline(world, chunkBox, 4, 5, 0, 4, 5, i, Blocks.NETHER_BRICKS.getDefaultState(), Blocks.NETHER_BRICKS.getDefaultState(), false);
            for (i = 0; i <= 4; ++i) {
                j = lv.nextInt(5);
                this.fillWithOutline(world, chunkBox, i, 2, 0, i, 2, j, Blocks.NETHER_BRICKS.getDefaultState(), Blocks.NETHER_BRICKS.getDefaultState(), false);
            }
            for (i = 0; i <= 4; ++i) {
                for (j = 0; j <= 1; ++j) {
                    k = lv.nextInt(3);
                    this.fillWithOutline(world, chunkBox, i, j, 0, i, j, k, Blocks.NETHER_BRICKS.getDefaultState(), Blocks.NETHER_BRICKS.getDefaultState(), false);
                }
            }
        }
    }

    public static class Start
    extends BridgeCrossing {
        public PieceData lastPiece;
        public List<PieceData> bridgePieces;
        public List<PieceData> corridorPieces;
        public final List<StructurePiece> pieces = Lists.newArrayList();

        public Start(Random random, int x, int z) {
            super(x, z, Start.getRandomHorizontalDirection(random));
            this.bridgePieces = Lists.newArrayList();
            for (PieceData lv : ALL_BRIDGE_PIECES) {
                lv.generatedCount = 0;
                this.bridgePieces.add(lv);
            }
            this.corridorPieces = Lists.newArrayList();
            for (PieceData lv : ALL_CORRIDOR_PIECES) {
                lv.generatedCount = 0;
                this.corridorPieces.add(lv);
            }
        }

        public Start(NbtCompound arg) {
            super(StructurePieceType.NETHER_FORTRESS_START, arg);
        }
    }

    static abstract class Piece
    extends StructurePiece {
        protected Piece(StructurePieceType arg, int i, BlockBox arg2) {
            super(arg, i, arg2);
        }

        public Piece(StructurePieceType arg, NbtCompound arg2) {
            super(arg, arg2);
        }

        @Override
        protected void writeNbt(StructureContext context, NbtCompound nbt) {
        }

        private int checkRemainingPieces(List<PieceData> possiblePieces) {
            boolean bl = false;
            int i = 0;
            for (PieceData lv : possiblePieces) {
                if (lv.limit > 0 && lv.generatedCount < lv.limit) {
                    bl = true;
                }
                i += lv.weight;
            }
            return bl ? i : -1;
        }

        private Piece pickPiece(Start start, List<PieceData> possiblePieces, StructurePiecesHolder holder, Random random, int x, int y, int z, Direction orientation, int chainLength) {
            int m = this.checkRemainingPieces(possiblePieces);
            boolean bl = m > 0 && chainLength <= 30;
            int n = 0;
            block0: while (n < 5 && bl) {
                ++n;
                int o = random.nextInt(m);
                for (PieceData lv : possiblePieces) {
                    if ((o -= lv.weight) >= 0) continue;
                    if (!lv.canGenerate(chainLength) || lv == start.lastPiece && !lv.repeatable) continue block0;
                    Piece lv2 = NetherFortressGenerator.createPiece(lv, holder, random, x, y, z, orientation, chainLength);
                    if (lv2 == null) continue;
                    ++lv.generatedCount;
                    start.lastPiece = lv;
                    if (!lv.canGenerate()) {
                        possiblePieces.remove(lv);
                    }
                    return lv2;
                }
            }
            return BridgeEnd.create(holder, random, x, y, z, orientation, chainLength);
        }

        private StructurePiece pieceGenerator(Start start, StructurePiecesHolder holder, Random random, int x, int y, int z, @Nullable Direction orientation, int chainLength, boolean inside) {
            Piece lv;
            if (Math.abs(x - start.getBoundingBox().getMinX()) > 112 || Math.abs(z - start.getBoundingBox().getMinZ()) > 112) {
                return BridgeEnd.create(holder, random, x, y, z, orientation, chainLength);
            }
            List<PieceData> list = start.bridgePieces;
            if (inside) {
                list = start.corridorPieces;
            }
            if ((lv = this.pickPiece(start, list, holder, random, x, y, z, orientation, chainLength + 1)) != null) {
                holder.addPiece(lv);
                start.pieces.add(lv);
            }
            return lv;
        }

        @Nullable
        protected StructurePiece fillForwardOpening(Start start, StructurePiecesHolder holder, Random random, int leftRightOffset, int heightOffset, boolean inside) {
            Direction lv = this.getFacing();
            if (lv != null) {
                switch (lv) {
                    case NORTH: {
                        return this.pieceGenerator(start, holder, random, this.boundingBox.getMinX() + leftRightOffset, this.boundingBox.getMinY() + heightOffset, this.boundingBox.getMinZ() - 1, lv, this.getChainLength(), inside);
                    }
                    case SOUTH: {
                        return this.pieceGenerator(start, holder, random, this.boundingBox.getMinX() + leftRightOffset, this.boundingBox.getMinY() + heightOffset, this.boundingBox.getMaxZ() + 1, lv, this.getChainLength(), inside);
                    }
                    case WEST: {
                        return this.pieceGenerator(start, holder, random, this.boundingBox.getMinX() - 1, this.boundingBox.getMinY() + heightOffset, this.boundingBox.getMinZ() + leftRightOffset, lv, this.getChainLength(), inside);
                    }
                    case EAST: {
                        return this.pieceGenerator(start, holder, random, this.boundingBox.getMaxX() + 1, this.boundingBox.getMinY() + heightOffset, this.boundingBox.getMinZ() + leftRightOffset, lv, this.getChainLength(), inside);
                    }
                }
            }
            return null;
        }

        @Nullable
        protected StructurePiece fillNWOpening(Start start, StructurePiecesHolder holder, Random random, int heightOffset, int leftRightOffset, boolean inside) {
            Direction lv = this.getFacing();
            if (lv != null) {
                switch (lv) {
                    case NORTH: {
                        return this.pieceGenerator(start, holder, random, this.boundingBox.getMinX() - 1, this.boundingBox.getMinY() + heightOffset, this.boundingBox.getMinZ() + leftRightOffset, Direction.WEST, this.getChainLength(), inside);
                    }
                    case SOUTH: {
                        return this.pieceGenerator(start, holder, random, this.boundingBox.getMinX() - 1, this.boundingBox.getMinY() + heightOffset, this.boundingBox.getMinZ() + leftRightOffset, Direction.WEST, this.getChainLength(), inside);
                    }
                    case WEST: {
                        return this.pieceGenerator(start, holder, random, this.boundingBox.getMinX() + leftRightOffset, this.boundingBox.getMinY() + heightOffset, this.boundingBox.getMinZ() - 1, Direction.NORTH, this.getChainLength(), inside);
                    }
                    case EAST: {
                        return this.pieceGenerator(start, holder, random, this.boundingBox.getMinX() + leftRightOffset, this.boundingBox.getMinY() + heightOffset, this.boundingBox.getMinZ() - 1, Direction.NORTH, this.getChainLength(), inside);
                    }
                }
            }
            return null;
        }

        @Nullable
        protected StructurePiece fillSEOpening(Start start, StructurePiecesHolder holder, Random random, int heightOffset, int leftRightOffset, boolean inside) {
            Direction lv = this.getFacing();
            if (lv != null) {
                switch (lv) {
                    case NORTH: {
                        return this.pieceGenerator(start, holder, random, this.boundingBox.getMaxX() + 1, this.boundingBox.getMinY() + heightOffset, this.boundingBox.getMinZ() + leftRightOffset, Direction.EAST, this.getChainLength(), inside);
                    }
                    case SOUTH: {
                        return this.pieceGenerator(start, holder, random, this.boundingBox.getMaxX() + 1, this.boundingBox.getMinY() + heightOffset, this.boundingBox.getMinZ() + leftRightOffset, Direction.EAST, this.getChainLength(), inside);
                    }
                    case WEST: {
                        return this.pieceGenerator(start, holder, random, this.boundingBox.getMinX() + leftRightOffset, this.boundingBox.getMinY() + heightOffset, this.boundingBox.getMaxZ() + 1, Direction.SOUTH, this.getChainLength(), inside);
                    }
                    case EAST: {
                        return this.pieceGenerator(start, holder, random, this.boundingBox.getMinX() + leftRightOffset, this.boundingBox.getMinY() + heightOffset, this.boundingBox.getMaxZ() + 1, Direction.SOUTH, this.getChainLength(), inside);
                    }
                }
            }
            return null;
        }

        protected static boolean isInBounds(BlockBox boundingBox) {
            return boundingBox != null && boundingBox.getMinY() > 10;
        }
    }
}

