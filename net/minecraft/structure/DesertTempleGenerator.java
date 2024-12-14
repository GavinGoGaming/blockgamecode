/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.structure;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.StairsBlock;
import net.minecraft.loot.LootTables;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.structure.ShiftableStructurePiece;
import net.minecraft.structure.StructureContext;
import net.minecraft.structure.StructurePieceType;
import net.minecraft.util.BlockRotation;
import net.minecraft.util.math.BlockBox;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.StructureWorldAccess;
import net.minecraft.world.gen.StructureAccessor;
import net.minecraft.world.gen.chunk.ChunkGenerator;

public class DesertTempleGenerator
extends ShiftableStructurePiece {
    public static final int WIDTH = 21;
    public static final int DEPTH = 21;
    private final boolean[] hasPlacedChest = new boolean[4];
    private final List<BlockPos> potentialSuspiciousSandPositions = new ArrayList<BlockPos>();
    private BlockPos basementMarkerPos = BlockPos.ORIGIN;

    public DesertTempleGenerator(Random random, int x, int z) {
        super(StructurePieceType.DESERT_TEMPLE, x, 64, z, 21, 15, 21, DesertTempleGenerator.getRandomHorizontalDirection(random));
    }

    public DesertTempleGenerator(NbtCompound nbt) {
        super(StructurePieceType.DESERT_TEMPLE, nbt);
        this.hasPlacedChest[0] = nbt.getBoolean("hasPlacedChest0");
        this.hasPlacedChest[1] = nbt.getBoolean("hasPlacedChest1");
        this.hasPlacedChest[2] = nbt.getBoolean("hasPlacedChest2");
        this.hasPlacedChest[3] = nbt.getBoolean("hasPlacedChest3");
    }

    @Override
    protected void writeNbt(StructureContext context, NbtCompound nbt) {
        super.writeNbt(context, nbt);
        nbt.putBoolean("hasPlacedChest0", this.hasPlacedChest[0]);
        nbt.putBoolean("hasPlacedChest1", this.hasPlacedChest[1]);
        nbt.putBoolean("hasPlacedChest2", this.hasPlacedChest[2]);
        nbt.putBoolean("hasPlacedChest3", this.hasPlacedChest[3]);
    }

    @Override
    public void generate(StructureWorldAccess world, StructureAccessor structureAccessor, ChunkGenerator chunkGenerator, Random random, BlockBox chunkBox, ChunkPos chunkPos, BlockPos pivot) {
        int l;
        int i;
        if (!this.adjustToMinHeight(world, -random.nextInt(3))) {
            return;
        }
        this.fillWithOutline(world, chunkBox, 0, -4, 0, this.width - 1, 0, this.depth - 1, Blocks.SANDSTONE.getDefaultState(), Blocks.SANDSTONE.getDefaultState(), false);
        for (i = 1; i <= 9; ++i) {
            this.fillWithOutline(world, chunkBox, i, i, i, this.width - 1 - i, i, this.depth - 1 - i, Blocks.SANDSTONE.getDefaultState(), Blocks.SANDSTONE.getDefaultState(), false);
            this.fillWithOutline(world, chunkBox, i + 1, i, i + 1, this.width - 2 - i, i, this.depth - 2 - i, Blocks.AIR.getDefaultState(), Blocks.AIR.getDefaultState(), false);
        }
        for (i = 0; i < this.width; ++i) {
            for (int j = 0; j < this.depth; ++j) {
                int k = -5;
                this.fillDownwards(world, Blocks.SANDSTONE.getDefaultState(), i, -5, j, chunkBox);
            }
        }
        BlockState lv = (BlockState)Blocks.SANDSTONE_STAIRS.getDefaultState().with(StairsBlock.FACING, Direction.NORTH);
        BlockState lv2 = (BlockState)Blocks.SANDSTONE_STAIRS.getDefaultState().with(StairsBlock.FACING, Direction.SOUTH);
        BlockState lv3 = (BlockState)Blocks.SANDSTONE_STAIRS.getDefaultState().with(StairsBlock.FACING, Direction.EAST);
        BlockState lv4 = (BlockState)Blocks.SANDSTONE_STAIRS.getDefaultState().with(StairsBlock.FACING, Direction.WEST);
        this.fillWithOutline(world, chunkBox, 0, 0, 0, 4, 9, 4, Blocks.SANDSTONE.getDefaultState(), Blocks.AIR.getDefaultState(), false);
        this.fillWithOutline(world, chunkBox, 1, 10, 1, 3, 10, 3, Blocks.SANDSTONE.getDefaultState(), Blocks.SANDSTONE.getDefaultState(), false);
        this.addBlock(world, lv, 2, 10, 0, chunkBox);
        this.addBlock(world, lv2, 2, 10, 4, chunkBox);
        this.addBlock(world, lv3, 0, 10, 2, chunkBox);
        this.addBlock(world, lv4, 4, 10, 2, chunkBox);
        this.fillWithOutline(world, chunkBox, this.width - 5, 0, 0, this.width - 1, 9, 4, Blocks.SANDSTONE.getDefaultState(), Blocks.AIR.getDefaultState(), false);
        this.fillWithOutline(world, chunkBox, this.width - 4, 10, 1, this.width - 2, 10, 3, Blocks.SANDSTONE.getDefaultState(), Blocks.SANDSTONE.getDefaultState(), false);
        this.addBlock(world, lv, this.width - 3, 10, 0, chunkBox);
        this.addBlock(world, lv2, this.width - 3, 10, 4, chunkBox);
        this.addBlock(world, lv3, this.width - 5, 10, 2, chunkBox);
        this.addBlock(world, lv4, this.width - 1, 10, 2, chunkBox);
        this.fillWithOutline(world, chunkBox, 8, 0, 0, 12, 4, 4, Blocks.SANDSTONE.getDefaultState(), Blocks.AIR.getDefaultState(), false);
        this.fillWithOutline(world, chunkBox, 9, 1, 0, 11, 3, 4, Blocks.AIR.getDefaultState(), Blocks.AIR.getDefaultState(), false);
        this.addBlock(world, Blocks.CUT_SANDSTONE.getDefaultState(), 9, 1, 1, chunkBox);
        this.addBlock(world, Blocks.CUT_SANDSTONE.getDefaultState(), 9, 2, 1, chunkBox);
        this.addBlock(world, Blocks.CUT_SANDSTONE.getDefaultState(), 9, 3, 1, chunkBox);
        this.addBlock(world, Blocks.CUT_SANDSTONE.getDefaultState(), 10, 3, 1, chunkBox);
        this.addBlock(world, Blocks.CUT_SANDSTONE.getDefaultState(), 11, 3, 1, chunkBox);
        this.addBlock(world, Blocks.CUT_SANDSTONE.getDefaultState(), 11, 2, 1, chunkBox);
        this.addBlock(world, Blocks.CUT_SANDSTONE.getDefaultState(), 11, 1, 1, chunkBox);
        this.fillWithOutline(world, chunkBox, 4, 1, 1, 8, 3, 3, Blocks.SANDSTONE.getDefaultState(), Blocks.AIR.getDefaultState(), false);
        this.fillWithOutline(world, chunkBox, 4, 1, 2, 8, 2, 2, Blocks.AIR.getDefaultState(), Blocks.AIR.getDefaultState(), false);
        this.fillWithOutline(world, chunkBox, 12, 1, 1, 16, 3, 3, Blocks.SANDSTONE.getDefaultState(), Blocks.AIR.getDefaultState(), false);
        this.fillWithOutline(world, chunkBox, 12, 1, 2, 16, 2, 2, Blocks.AIR.getDefaultState(), Blocks.AIR.getDefaultState(), false);
        this.fillWithOutline(world, chunkBox, 5, 4, 5, this.width - 6, 4, this.depth - 6, Blocks.SANDSTONE.getDefaultState(), Blocks.SANDSTONE.getDefaultState(), false);
        this.fillWithOutline(world, chunkBox, 9, 4, 9, 11, 4, 11, Blocks.AIR.getDefaultState(), Blocks.AIR.getDefaultState(), false);
        this.fillWithOutline(world, chunkBox, 8, 1, 8, 8, 3, 8, Blocks.CUT_SANDSTONE.getDefaultState(), Blocks.CUT_SANDSTONE.getDefaultState(), false);
        this.fillWithOutline(world, chunkBox, 12, 1, 8, 12, 3, 8, Blocks.CUT_SANDSTONE.getDefaultState(), Blocks.CUT_SANDSTONE.getDefaultState(), false);
        this.fillWithOutline(world, chunkBox, 8, 1, 12, 8, 3, 12, Blocks.CUT_SANDSTONE.getDefaultState(), Blocks.CUT_SANDSTONE.getDefaultState(), false);
        this.fillWithOutline(world, chunkBox, 12, 1, 12, 12, 3, 12, Blocks.CUT_SANDSTONE.getDefaultState(), Blocks.CUT_SANDSTONE.getDefaultState(), false);
        this.fillWithOutline(world, chunkBox, 1, 1, 5, 4, 4, 11, Blocks.SANDSTONE.getDefaultState(), Blocks.SANDSTONE.getDefaultState(), false);
        this.fillWithOutline(world, chunkBox, this.width - 5, 1, 5, this.width - 2, 4, 11, Blocks.SANDSTONE.getDefaultState(), Blocks.SANDSTONE.getDefaultState(), false);
        this.fillWithOutline(world, chunkBox, 6, 7, 9, 6, 7, 11, Blocks.SANDSTONE.getDefaultState(), Blocks.SANDSTONE.getDefaultState(), false);
        this.fillWithOutline(world, chunkBox, this.width - 7, 7, 9, this.width - 7, 7, 11, Blocks.SANDSTONE.getDefaultState(), Blocks.SANDSTONE.getDefaultState(), false);
        this.fillWithOutline(world, chunkBox, 5, 5, 9, 5, 7, 11, Blocks.CUT_SANDSTONE.getDefaultState(), Blocks.CUT_SANDSTONE.getDefaultState(), false);
        this.fillWithOutline(world, chunkBox, this.width - 6, 5, 9, this.width - 6, 7, 11, Blocks.CUT_SANDSTONE.getDefaultState(), Blocks.CUT_SANDSTONE.getDefaultState(), false);
        this.addBlock(world, Blocks.AIR.getDefaultState(), 5, 5, 10, chunkBox);
        this.addBlock(world, Blocks.AIR.getDefaultState(), 5, 6, 10, chunkBox);
        this.addBlock(world, Blocks.AIR.getDefaultState(), 6, 6, 10, chunkBox);
        this.addBlock(world, Blocks.AIR.getDefaultState(), this.width - 6, 5, 10, chunkBox);
        this.addBlock(world, Blocks.AIR.getDefaultState(), this.width - 6, 6, 10, chunkBox);
        this.addBlock(world, Blocks.AIR.getDefaultState(), this.width - 7, 6, 10, chunkBox);
        this.fillWithOutline(world, chunkBox, 2, 4, 4, 2, 6, 4, Blocks.AIR.getDefaultState(), Blocks.AIR.getDefaultState(), false);
        this.fillWithOutline(world, chunkBox, this.width - 3, 4, 4, this.width - 3, 6, 4, Blocks.AIR.getDefaultState(), Blocks.AIR.getDefaultState(), false);
        this.addBlock(world, lv, 2, 4, 5, chunkBox);
        this.addBlock(world, lv, 2, 3, 4, chunkBox);
        this.addBlock(world, lv, this.width - 3, 4, 5, chunkBox);
        this.addBlock(world, lv, this.width - 3, 3, 4, chunkBox);
        this.fillWithOutline(world, chunkBox, 1, 1, 3, 2, 2, 3, Blocks.SANDSTONE.getDefaultState(), Blocks.SANDSTONE.getDefaultState(), false);
        this.fillWithOutline(world, chunkBox, this.width - 3, 1, 3, this.width - 2, 2, 3, Blocks.SANDSTONE.getDefaultState(), Blocks.SANDSTONE.getDefaultState(), false);
        this.addBlock(world, Blocks.SANDSTONE.getDefaultState(), 1, 1, 2, chunkBox);
        this.addBlock(world, Blocks.SANDSTONE.getDefaultState(), this.width - 2, 1, 2, chunkBox);
        this.addBlock(world, Blocks.SANDSTONE_SLAB.getDefaultState(), 1, 2, 2, chunkBox);
        this.addBlock(world, Blocks.SANDSTONE_SLAB.getDefaultState(), this.width - 2, 2, 2, chunkBox);
        this.addBlock(world, lv4, 2, 1, 2, chunkBox);
        this.addBlock(world, lv3, this.width - 3, 1, 2, chunkBox);
        this.fillWithOutline(world, chunkBox, 4, 3, 5, 4, 3, 17, Blocks.SANDSTONE.getDefaultState(), Blocks.SANDSTONE.getDefaultState(), false);
        this.fillWithOutline(world, chunkBox, this.width - 5, 3, 5, this.width - 5, 3, 17, Blocks.SANDSTONE.getDefaultState(), Blocks.SANDSTONE.getDefaultState(), false);
        this.fillWithOutline(world, chunkBox, 3, 1, 5, 4, 2, 16, Blocks.AIR.getDefaultState(), Blocks.AIR.getDefaultState(), false);
        this.fillWithOutline(world, chunkBox, this.width - 6, 1, 5, this.width - 5, 2, 16, Blocks.AIR.getDefaultState(), Blocks.AIR.getDefaultState(), false);
        for (l = 5; l <= 17; l += 2) {
            this.addBlock(world, Blocks.CUT_SANDSTONE.getDefaultState(), 4, 1, l, chunkBox);
            this.addBlock(world, Blocks.CHISELED_SANDSTONE.getDefaultState(), 4, 2, l, chunkBox);
            this.addBlock(world, Blocks.CUT_SANDSTONE.getDefaultState(), this.width - 5, 1, l, chunkBox);
            this.addBlock(world, Blocks.CHISELED_SANDSTONE.getDefaultState(), this.width - 5, 2, l, chunkBox);
        }
        this.addBlock(world, Blocks.ORANGE_TERRACOTTA.getDefaultState(), 10, 0, 7, chunkBox);
        this.addBlock(world, Blocks.ORANGE_TERRACOTTA.getDefaultState(), 10, 0, 8, chunkBox);
        this.addBlock(world, Blocks.ORANGE_TERRACOTTA.getDefaultState(), 9, 0, 9, chunkBox);
        this.addBlock(world, Blocks.ORANGE_TERRACOTTA.getDefaultState(), 11, 0, 9, chunkBox);
        this.addBlock(world, Blocks.ORANGE_TERRACOTTA.getDefaultState(), 8, 0, 10, chunkBox);
        this.addBlock(world, Blocks.ORANGE_TERRACOTTA.getDefaultState(), 12, 0, 10, chunkBox);
        this.addBlock(world, Blocks.ORANGE_TERRACOTTA.getDefaultState(), 7, 0, 10, chunkBox);
        this.addBlock(world, Blocks.ORANGE_TERRACOTTA.getDefaultState(), 13, 0, 10, chunkBox);
        this.addBlock(world, Blocks.ORANGE_TERRACOTTA.getDefaultState(), 9, 0, 11, chunkBox);
        this.addBlock(world, Blocks.ORANGE_TERRACOTTA.getDefaultState(), 11, 0, 11, chunkBox);
        this.addBlock(world, Blocks.ORANGE_TERRACOTTA.getDefaultState(), 10, 0, 12, chunkBox);
        this.addBlock(world, Blocks.ORANGE_TERRACOTTA.getDefaultState(), 10, 0, 13, chunkBox);
        this.addBlock(world, Blocks.BLUE_TERRACOTTA.getDefaultState(), 10, 0, 10, chunkBox);
        for (l = 0; l <= this.width - 1; l += this.width - 1) {
            this.addBlock(world, Blocks.CUT_SANDSTONE.getDefaultState(), l, 2, 1, chunkBox);
            this.addBlock(world, Blocks.ORANGE_TERRACOTTA.getDefaultState(), l, 2, 2, chunkBox);
            this.addBlock(world, Blocks.CUT_SANDSTONE.getDefaultState(), l, 2, 3, chunkBox);
            this.addBlock(world, Blocks.CUT_SANDSTONE.getDefaultState(), l, 3, 1, chunkBox);
            this.addBlock(world, Blocks.ORANGE_TERRACOTTA.getDefaultState(), l, 3, 2, chunkBox);
            this.addBlock(world, Blocks.CUT_SANDSTONE.getDefaultState(), l, 3, 3, chunkBox);
            this.addBlock(world, Blocks.ORANGE_TERRACOTTA.getDefaultState(), l, 4, 1, chunkBox);
            this.addBlock(world, Blocks.CHISELED_SANDSTONE.getDefaultState(), l, 4, 2, chunkBox);
            this.addBlock(world, Blocks.ORANGE_TERRACOTTA.getDefaultState(), l, 4, 3, chunkBox);
            this.addBlock(world, Blocks.CUT_SANDSTONE.getDefaultState(), l, 5, 1, chunkBox);
            this.addBlock(world, Blocks.ORANGE_TERRACOTTA.getDefaultState(), l, 5, 2, chunkBox);
            this.addBlock(world, Blocks.CUT_SANDSTONE.getDefaultState(), l, 5, 3, chunkBox);
            this.addBlock(world, Blocks.ORANGE_TERRACOTTA.getDefaultState(), l, 6, 1, chunkBox);
            this.addBlock(world, Blocks.CHISELED_SANDSTONE.getDefaultState(), l, 6, 2, chunkBox);
            this.addBlock(world, Blocks.ORANGE_TERRACOTTA.getDefaultState(), l, 6, 3, chunkBox);
            this.addBlock(world, Blocks.ORANGE_TERRACOTTA.getDefaultState(), l, 7, 1, chunkBox);
            this.addBlock(world, Blocks.ORANGE_TERRACOTTA.getDefaultState(), l, 7, 2, chunkBox);
            this.addBlock(world, Blocks.ORANGE_TERRACOTTA.getDefaultState(), l, 7, 3, chunkBox);
            this.addBlock(world, Blocks.CUT_SANDSTONE.getDefaultState(), l, 8, 1, chunkBox);
            this.addBlock(world, Blocks.CUT_SANDSTONE.getDefaultState(), l, 8, 2, chunkBox);
            this.addBlock(world, Blocks.CUT_SANDSTONE.getDefaultState(), l, 8, 3, chunkBox);
        }
        for (l = 2; l <= this.width - 3; l += this.width - 3 - 2) {
            this.addBlock(world, Blocks.CUT_SANDSTONE.getDefaultState(), l - 1, 2, 0, chunkBox);
            this.addBlock(world, Blocks.ORANGE_TERRACOTTA.getDefaultState(), l, 2, 0, chunkBox);
            this.addBlock(world, Blocks.CUT_SANDSTONE.getDefaultState(), l + 1, 2, 0, chunkBox);
            this.addBlock(world, Blocks.CUT_SANDSTONE.getDefaultState(), l - 1, 3, 0, chunkBox);
            this.addBlock(world, Blocks.ORANGE_TERRACOTTA.getDefaultState(), l, 3, 0, chunkBox);
            this.addBlock(world, Blocks.CUT_SANDSTONE.getDefaultState(), l + 1, 3, 0, chunkBox);
            this.addBlock(world, Blocks.ORANGE_TERRACOTTA.getDefaultState(), l - 1, 4, 0, chunkBox);
            this.addBlock(world, Blocks.CHISELED_SANDSTONE.getDefaultState(), l, 4, 0, chunkBox);
            this.addBlock(world, Blocks.ORANGE_TERRACOTTA.getDefaultState(), l + 1, 4, 0, chunkBox);
            this.addBlock(world, Blocks.CUT_SANDSTONE.getDefaultState(), l - 1, 5, 0, chunkBox);
            this.addBlock(world, Blocks.ORANGE_TERRACOTTA.getDefaultState(), l, 5, 0, chunkBox);
            this.addBlock(world, Blocks.CUT_SANDSTONE.getDefaultState(), l + 1, 5, 0, chunkBox);
            this.addBlock(world, Blocks.ORANGE_TERRACOTTA.getDefaultState(), l - 1, 6, 0, chunkBox);
            this.addBlock(world, Blocks.CHISELED_SANDSTONE.getDefaultState(), l, 6, 0, chunkBox);
            this.addBlock(world, Blocks.ORANGE_TERRACOTTA.getDefaultState(), l + 1, 6, 0, chunkBox);
            this.addBlock(world, Blocks.ORANGE_TERRACOTTA.getDefaultState(), l - 1, 7, 0, chunkBox);
            this.addBlock(world, Blocks.ORANGE_TERRACOTTA.getDefaultState(), l, 7, 0, chunkBox);
            this.addBlock(world, Blocks.ORANGE_TERRACOTTA.getDefaultState(), l + 1, 7, 0, chunkBox);
            this.addBlock(world, Blocks.CUT_SANDSTONE.getDefaultState(), l - 1, 8, 0, chunkBox);
            this.addBlock(world, Blocks.CUT_SANDSTONE.getDefaultState(), l, 8, 0, chunkBox);
            this.addBlock(world, Blocks.CUT_SANDSTONE.getDefaultState(), l + 1, 8, 0, chunkBox);
        }
        this.fillWithOutline(world, chunkBox, 8, 4, 0, 12, 6, 0, Blocks.CUT_SANDSTONE.getDefaultState(), Blocks.CUT_SANDSTONE.getDefaultState(), false);
        this.addBlock(world, Blocks.AIR.getDefaultState(), 8, 6, 0, chunkBox);
        this.addBlock(world, Blocks.AIR.getDefaultState(), 12, 6, 0, chunkBox);
        this.addBlock(world, Blocks.ORANGE_TERRACOTTA.getDefaultState(), 9, 5, 0, chunkBox);
        this.addBlock(world, Blocks.CHISELED_SANDSTONE.getDefaultState(), 10, 5, 0, chunkBox);
        this.addBlock(world, Blocks.ORANGE_TERRACOTTA.getDefaultState(), 11, 5, 0, chunkBox);
        this.fillWithOutline(world, chunkBox, 8, -14, 8, 12, -11, 12, Blocks.CUT_SANDSTONE.getDefaultState(), Blocks.CUT_SANDSTONE.getDefaultState(), false);
        this.fillWithOutline(world, chunkBox, 8, -10, 8, 12, -10, 12, Blocks.CHISELED_SANDSTONE.getDefaultState(), Blocks.CHISELED_SANDSTONE.getDefaultState(), false);
        this.fillWithOutline(world, chunkBox, 8, -9, 8, 12, -9, 12, Blocks.CUT_SANDSTONE.getDefaultState(), Blocks.CUT_SANDSTONE.getDefaultState(), false);
        this.fillWithOutline(world, chunkBox, 8, -8, 8, 12, -1, 12, Blocks.SANDSTONE.getDefaultState(), Blocks.SANDSTONE.getDefaultState(), false);
        this.fillWithOutline(world, chunkBox, 9, -11, 9, 11, -1, 11, Blocks.AIR.getDefaultState(), Blocks.AIR.getDefaultState(), false);
        this.addBlock(world, Blocks.STONE_PRESSURE_PLATE.getDefaultState(), 10, -11, 10, chunkBox);
        this.fillWithOutline(world, chunkBox, 9, -13, 9, 11, -13, 11, Blocks.TNT.getDefaultState(), Blocks.AIR.getDefaultState(), false);
        this.addBlock(world, Blocks.AIR.getDefaultState(), 8, -11, 10, chunkBox);
        this.addBlock(world, Blocks.AIR.getDefaultState(), 8, -10, 10, chunkBox);
        this.addBlock(world, Blocks.CHISELED_SANDSTONE.getDefaultState(), 7, -10, 10, chunkBox);
        this.addBlock(world, Blocks.CUT_SANDSTONE.getDefaultState(), 7, -11, 10, chunkBox);
        this.addBlock(world, Blocks.AIR.getDefaultState(), 12, -11, 10, chunkBox);
        this.addBlock(world, Blocks.AIR.getDefaultState(), 12, -10, 10, chunkBox);
        this.addBlock(world, Blocks.CHISELED_SANDSTONE.getDefaultState(), 13, -10, 10, chunkBox);
        this.addBlock(world, Blocks.CUT_SANDSTONE.getDefaultState(), 13, -11, 10, chunkBox);
        this.addBlock(world, Blocks.AIR.getDefaultState(), 10, -11, 8, chunkBox);
        this.addBlock(world, Blocks.AIR.getDefaultState(), 10, -10, 8, chunkBox);
        this.addBlock(world, Blocks.CHISELED_SANDSTONE.getDefaultState(), 10, -10, 7, chunkBox);
        this.addBlock(world, Blocks.CUT_SANDSTONE.getDefaultState(), 10, -11, 7, chunkBox);
        this.addBlock(world, Blocks.AIR.getDefaultState(), 10, -11, 12, chunkBox);
        this.addBlock(world, Blocks.AIR.getDefaultState(), 10, -10, 12, chunkBox);
        this.addBlock(world, Blocks.CHISELED_SANDSTONE.getDefaultState(), 10, -10, 13, chunkBox);
        this.addBlock(world, Blocks.CUT_SANDSTONE.getDefaultState(), 10, -11, 13, chunkBox);
        for (Direction lv5 : Direction.Type.HORIZONTAL) {
            if (this.hasPlacedChest[lv5.getHorizontal()]) continue;
            int m = lv5.getOffsetX() * 2;
            int n = lv5.getOffsetZ() * 2;
            this.hasPlacedChest[lv5.getHorizontal()] = this.addChest(world, chunkBox, random, 10 + m, -11, 10 + n, LootTables.DESERT_PYRAMID_CHEST);
        }
        this.generateBasement(world, chunkBox);
    }

    private void generateBasement(StructureWorldAccess world, BlockBox chunkBox) {
        BlockPos lv = new BlockPos(16, -4, 13);
        this.generateBasementStairs(lv, world, chunkBox);
        this.generateSuspiciousSandRoom(lv, world, chunkBox);
    }

    private void generateBasementStairs(BlockPos pos, StructureWorldAccess world, BlockBox chunkBox) {
        int i = pos.getX();
        int j = pos.getY();
        int k = pos.getZ();
        BlockState lv = Blocks.SANDSTONE_STAIRS.getDefaultState();
        this.addBlock(world, lv.rotate(BlockRotation.COUNTERCLOCKWISE_90), 13, -1, 17, chunkBox);
        this.addBlock(world, lv.rotate(BlockRotation.COUNTERCLOCKWISE_90), 14, -2, 17, chunkBox);
        this.addBlock(world, lv.rotate(BlockRotation.COUNTERCLOCKWISE_90), 15, -3, 17, chunkBox);
        BlockState lv2 = Blocks.SAND.getDefaultState();
        BlockState lv3 = Blocks.SANDSTONE.getDefaultState();
        boolean bl = world.getRandom().nextBoolean();
        this.addBlock(world, lv2, i - 4, j + 4, k + 4, chunkBox);
        this.addBlock(world, lv2, i - 3, j + 4, k + 4, chunkBox);
        this.addBlock(world, lv2, i - 2, j + 4, k + 4, chunkBox);
        this.addBlock(world, lv2, i - 1, j + 4, k + 4, chunkBox);
        this.addBlock(world, lv2, i, j + 4, k + 4, chunkBox);
        this.addBlock(world, lv2, i - 2, j + 3, k + 4, chunkBox);
        this.addBlock(world, bl ? lv2 : lv3, i - 1, j + 3, k + 4, chunkBox);
        this.addBlock(world, !bl ? lv2 : lv3, i, j + 3, k + 4, chunkBox);
        this.addBlock(world, lv2, i - 1, j + 2, k + 4, chunkBox);
        this.addBlock(world, lv3, i, j + 2, k + 4, chunkBox);
        this.addBlock(world, lv2, i, j + 1, k + 4, chunkBox);
    }

    private void generateSuspiciousSandRoom(BlockPos pos, StructureWorldAccess world, BlockBox chunkBox) {
        int i = pos.getX();
        int j = pos.getY();
        int k = pos.getZ();
        BlockState lv = Blocks.CUT_SANDSTONE.getDefaultState();
        BlockState lv2 = Blocks.CHISELED_SANDSTONE.getDefaultState();
        this.fillWithOutline(world, chunkBox, i - 3, j + 1, k - 3, i - 3, j + 1, k + 2, lv, lv, true);
        this.fillWithOutline(world, chunkBox, i + 3, j + 1, k - 3, i + 3, j + 1, k + 2, lv, lv, true);
        this.fillWithOutline(world, chunkBox, i - 3, j + 1, k - 3, i + 3, j + 1, k - 2, lv, lv, true);
        this.fillWithOutline(world, chunkBox, i - 3, j + 1, k + 3, i + 3, j + 1, k + 3, lv, lv, true);
        this.fillWithOutline(world, chunkBox, i - 3, j + 2, k - 3, i - 3, j + 2, k + 2, lv2, lv2, true);
        this.fillWithOutline(world, chunkBox, i + 3, j + 2, k - 3, i + 3, j + 2, k + 2, lv2, lv2, true);
        this.fillWithOutline(world, chunkBox, i - 3, j + 2, k - 3, i + 3, j + 2, k - 2, lv2, lv2, true);
        this.fillWithOutline(world, chunkBox, i - 3, j + 2, k + 3, i + 3, j + 2, k + 3, lv2, lv2, true);
        this.fillWithOutline(world, chunkBox, i - 3, -1, k - 3, i - 3, -1, k + 2, lv, lv, true);
        this.fillWithOutline(world, chunkBox, i + 3, -1, k - 3, i + 3, -1, k + 2, lv, lv, true);
        this.fillWithOutline(world, chunkBox, i - 3, -1, k - 3, i + 3, -1, k - 2, lv, lv, true);
        this.fillWithOutline(world, chunkBox, i - 3, -1, k + 3, i + 3, -1, k + 3, lv, lv, true);
        this.addPotentialSuspiciousSandArea(i - 2, j + 1, k - 2, i + 2, j + 3, k + 2);
        this.generateBasementRoof(world, chunkBox, i - 2, j + 4, k - 2, i + 2, k + 2);
        BlockState lv3 = Blocks.ORANGE_TERRACOTTA.getDefaultState();
        BlockState lv4 = Blocks.BLUE_TERRACOTTA.getDefaultState();
        this.addBlock(world, lv4, i, j, k, chunkBox);
        this.addBlock(world, lv3, i + 1, j, k - 1, chunkBox);
        this.addBlock(world, lv3, i + 1, j, k + 1, chunkBox);
        this.addBlock(world, lv3, i - 1, j, k - 1, chunkBox);
        this.addBlock(world, lv3, i - 1, j, k + 1, chunkBox);
        this.addBlock(world, lv3, i + 2, j, k, chunkBox);
        this.addBlock(world, lv3, i - 2, j, k, chunkBox);
        this.addBlock(world, lv3, i, j, k + 2, chunkBox);
        this.addBlock(world, lv3, i, j, k - 2, chunkBox);
        this.addBlock(world, lv3, i + 3, j, k, chunkBox);
        this.addPotentialSuspiciousSandPosition(i + 3, j + 1, k);
        this.addPotentialSuspiciousSandPosition(i + 3, j + 2, k);
        this.addBlock(world, lv, i + 4, j + 1, k, chunkBox);
        this.addBlock(world, lv2, i + 4, j + 2, k, chunkBox);
        this.addBlock(world, lv3, i - 3, j, k, chunkBox);
        this.addPotentialSuspiciousSandPosition(i - 3, j + 1, k);
        this.addPotentialSuspiciousSandPosition(i - 3, j + 2, k);
        this.addBlock(world, lv, i - 4, j + 1, k, chunkBox);
        this.addBlock(world, lv2, i - 4, j + 2, k, chunkBox);
        this.addBlock(world, lv3, i, j, k + 3, chunkBox);
        this.addPotentialSuspiciousSandPosition(i, j + 1, k + 3);
        this.addPotentialSuspiciousSandPosition(i, j + 2, k + 3);
        this.addBlock(world, lv3, i, j, k - 3, chunkBox);
        this.addPotentialSuspiciousSandPosition(i, j + 1, k - 3);
        this.addPotentialSuspiciousSandPosition(i, j + 2, k - 3);
        this.addBlock(world, lv, i, j + 1, k - 4, chunkBox);
        this.addBlock(world, lv2, i, -2, k - 4, chunkBox);
    }

    private void addPotentialSuspiciousSandPosition(int x, int y, int z) {
        BlockPos.Mutable lv = this.offsetPos(x, y, z);
        this.potentialSuspiciousSandPositions.add(lv);
    }

    private void addPotentialSuspiciousSandArea(int startX, int startY, int startZ, int endX, int endY, int endZ) {
        for (int o = startY; o <= endY; ++o) {
            for (int p = startX; p <= endX; ++p) {
                for (int q = startZ; q <= endZ; ++q) {
                    this.addPotentialSuspiciousSandPosition(p, o, q);
                }
            }
        }
    }

    private void addSandOrSandstone(StructureWorldAccess world, int x, int y, int z, BlockBox chunkBox) {
        if (world.getRandom().nextFloat() < 0.33f) {
            BlockState lv = Blocks.SANDSTONE.getDefaultState();
            this.addBlock(world, lv, x, y, z, chunkBox);
        } else {
            BlockState lv = Blocks.SAND.getDefaultState();
            this.addBlock(world, lv, x, y, z, chunkBox);
        }
    }

    private void generateBasementRoof(StructureWorldAccess world, BlockBox chunkBox, int startX, int y, int startZ, int endX, int endZ) {
        int o;
        for (int n = startX; n <= endX; ++n) {
            for (o = startZ; o <= endZ; ++o) {
                this.addSandOrSandstone(world, n, y, o, chunkBox);
            }
        }
        Random lv = Random.create(world.getSeed()).nextSplitter().split(this.offsetPos(startX, y, startZ));
        o = lv.nextBetween(startX, endX);
        int p = lv.nextBetween(startZ, endZ);
        this.basementMarkerPos = new BlockPos(this.applyXTransform(o, p), this.applyYTransform(y), this.applyZTransform(o, p));
    }

    public List<BlockPos> getPotentialSuspiciousSandPositions() {
        return this.potentialSuspiciousSandPositions;
    }

    public BlockPos getBasementMarkerPos() {
        return this.basementMarkerPos;
    }
}

