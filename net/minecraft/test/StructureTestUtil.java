/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.test;

import com.mojang.logging.LogUtils;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Stream;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.block.entity.CommandBlockBlockEntity;
import net.minecraft.block.entity.StructureBlockBlockEntity;
import net.minecraft.block.enums.StructureBlockMode;
import net.minecraft.command.argument.BlockStateArgument;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.structure.StructureTemplate;
import net.minecraft.test.GameTestState;
import net.minecraft.util.BlockMirror;
import net.minecraft.util.BlockRotation;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockBox;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.Vec3i;
import net.minecraft.world.Heightmap;
import net.minecraft.world.tick.WorldTickScheduler;
import org.slf4j.Logger;

public class StructureTestUtil {
    private static final Logger LOGGER = LogUtils.getLogger();
    public static final int field_51468 = 10;
    public static final String TEST_STRUCTURES_DIRECTORY_NAME = "gameteststructures";
    public static String testStructuresDirectoryName = "gameteststructures";

    public static BlockRotation getRotation(int steps) {
        switch (steps) {
            case 0: {
                return BlockRotation.NONE;
            }
            case 1: {
                return BlockRotation.CLOCKWISE_90;
            }
            case 2: {
                return BlockRotation.CLOCKWISE_180;
            }
            case 3: {
                return BlockRotation.COUNTERCLOCKWISE_90;
            }
        }
        throw new IllegalArgumentException("rotationSteps must be a value from 0-3. Got value " + steps);
    }

    public static int getRotationSteps(BlockRotation rotation) {
        switch (rotation) {
            case NONE: {
                return 0;
            }
            case CLOCKWISE_90: {
                return 1;
            }
            case CLOCKWISE_180: {
                return 2;
            }
            case COUNTERCLOCKWISE_90: {
                return 3;
            }
        }
        throw new IllegalArgumentException("Unknown rotation value, don't know how many steps it represents: " + String.valueOf(rotation));
    }

    public static Box getStructureBoundingBox(StructureBlockBlockEntity structureBlockEntity) {
        return Box.from(StructureTestUtil.getStructureBlockBox(structureBlockEntity));
    }

    public static BlockBox getStructureBlockBox(StructureBlockBlockEntity structureBlockEntity) {
        BlockPos lv = StructureTestUtil.getStructureBlockPos(structureBlockEntity);
        BlockPos lv2 = StructureTestUtil.getStructureBlockBoxCornerPos(lv, structureBlockEntity.getSize(), structureBlockEntity.getRotation());
        return BlockBox.create(lv, lv2);
    }

    public static BlockPos getStructureBlockPos(StructureBlockBlockEntity structureBlockEntity) {
        return structureBlockEntity.getPos().add(structureBlockEntity.getOffset());
    }

    public static void placeStartButton(BlockPos pos, BlockPos relativePos, BlockRotation rotation, ServerWorld world) {
        BlockPos lv = StructureTemplate.transformAround(pos.add(relativePos), BlockMirror.NONE, rotation, pos);
        world.setBlockState(lv, Blocks.COMMAND_BLOCK.getDefaultState());
        CommandBlockBlockEntity lv2 = (CommandBlockBlockEntity)world.getBlockEntity(lv);
        lv2.getCommandExecutor().setCommand("test runclosest");
        BlockPos lv3 = StructureTemplate.transformAround(lv.add(0, 0, -1), BlockMirror.NONE, rotation, lv);
        world.setBlockState(lv3, Blocks.STONE_BUTTON.getDefaultState().rotate(rotation));
    }

    public static void createTestArea(String testName, BlockPos pos, Vec3i relativePos, BlockRotation rotation, ServerWorld world) {
        BlockBox lv = StructureTestUtil.getStructureBlockBox(pos.up(), relativePos, rotation);
        StructureTestUtil.clearArea(lv, world);
        world.setBlockState(pos, Blocks.STRUCTURE_BLOCK.getDefaultState());
        StructureBlockBlockEntity lv2 = (StructureBlockBlockEntity)world.getBlockEntity(pos);
        lv2.setIgnoreEntities(false);
        lv2.setTemplateName(Identifier.of(testName));
        lv2.setSize(relativePos);
        lv2.setMode(StructureBlockMode.SAVE);
        lv2.setShowBoundingBox(true);
    }

    public static StructureBlockBlockEntity initStructure(GameTestState state, BlockPos pos, BlockRotation rotation, ServerWorld world) {
        BlockPos lv3;
        Vec3i lv = world.getStructureTemplateManager().getTemplate(Identifier.of(state.getTemplateName())).orElseThrow(() -> new IllegalStateException("Missing test structure: " + state.getTemplateName())).getSize();
        BlockBox lv2 = StructureTestUtil.getStructureBlockBox(pos, lv, rotation);
        if (rotation == BlockRotation.NONE) {
            lv3 = pos;
        } else if (rotation == BlockRotation.CLOCKWISE_90) {
            lv3 = pos.add(lv.getZ() - 1, 0, 0);
        } else if (rotation == BlockRotation.CLOCKWISE_180) {
            lv3 = pos.add(lv.getX() - 1, 0, lv.getZ() - 1);
        } else if (rotation == BlockRotation.COUNTERCLOCKWISE_90) {
            lv3 = pos.add(0, 0, lv.getX() - 1);
        } else {
            throw new IllegalArgumentException("Invalid rotation: " + String.valueOf(rotation));
        }
        StructureTestUtil.forceLoadNearbyChunks(lv2, world);
        StructureTestUtil.clearArea(lv2, world);
        return StructureTestUtil.placeStructureTemplate(state, lv3.down(), rotation, world);
    }

    public static void placeBarrierBox(Box box, ServerWorld world, boolean noSkyAccess) {
        BlockPos lv = BlockPos.ofFloored(box.minX, box.minY, box.minZ).add(-1, 0, -1);
        BlockPos lv2 = BlockPos.ofFloored(box.maxX, box.maxY, box.maxZ);
        BlockPos.stream(lv, lv2).forEach(pos -> {
            boolean bl3;
            boolean bl2 = pos.getX() == lv.getX() || pos.getX() == lv2.getX() || pos.getZ() == lv.getZ() || pos.getZ() == lv2.getZ();
            boolean bl4 = bl3 = pos.getY() == lv2.getY();
            if (bl2 || bl3 && noSkyAccess) {
                world.setBlockState((BlockPos)pos, Blocks.BARRIER.getDefaultState());
            }
        });
    }

    public static void clearBarrierBox(Box box, ServerWorld world) {
        BlockPos lv = BlockPos.ofFloored(box.minX, box.minY, box.minZ).add(-1, 0, -1);
        BlockPos lv2 = BlockPos.ofFloored(box.maxX, box.maxY, box.maxZ);
        BlockPos.stream(lv, lv2).forEach(pos -> {
            boolean bl2;
            boolean bl = pos.getX() == lv.getX() || pos.getX() == lv2.getX() || pos.getZ() == lv.getZ() || pos.getZ() == lv2.getZ();
            boolean bl3 = bl2 = pos.getY() == lv2.getY();
            if (world.getBlockState((BlockPos)pos).isOf(Blocks.BARRIER) && (bl || bl2)) {
                world.setBlockState((BlockPos)pos, Blocks.AIR.getDefaultState());
            }
        });
    }

    private static void forceLoadNearbyChunks(BlockBox box, ServerWorld world) {
        box.streamChunkPos().forEach(chunkPos -> world.setChunkForced(chunkPos.x, chunkPos.z, true));
    }

    public static void clearArea(BlockBox area, ServerWorld world) {
        int i = area.getMinY() - 1;
        BlockBox lv = new BlockBox(area.getMinX() - 2, area.getMinY() - 3, area.getMinZ() - 3, area.getMaxX() + 3, area.getMaxY() + 20, area.getMaxZ() + 3);
        BlockPos.stream(lv).forEach(pos -> StructureTestUtil.resetBlock(i, pos, world));
        ((WorldTickScheduler)world.getBlockTickScheduler()).clearNextTicks(lv);
        world.clearUpdatesInArea(lv);
        Box lv2 = Box.from(lv);
        List<Entity> list = world.getEntitiesByClass(Entity.class, lv2, entity -> !(entity instanceof PlayerEntity));
        list.forEach(Entity::discard);
    }

    public static BlockPos getStructureBlockBoxCornerPos(BlockPos pos, Vec3i size, BlockRotation rotation) {
        BlockPos lv = pos.add(size).add(-1, -1, -1);
        return StructureTemplate.transformAround(lv, BlockMirror.NONE, rotation, pos);
    }

    public static BlockBox getStructureBlockBox(BlockPos pos, Vec3i relativePos, BlockRotation rotation) {
        BlockPos lv = StructureTestUtil.getStructureBlockBoxCornerPos(pos, relativePos, rotation);
        BlockBox lv2 = BlockBox.create(pos, lv);
        int i = Math.min(lv2.getMinX(), lv2.getMaxX());
        int j = Math.min(lv2.getMinZ(), lv2.getMaxZ());
        return lv2.move(pos.getX() - i, 0, pos.getZ() - j);
    }

    public static Optional<BlockPos> findContainingStructureBlock(BlockPos pos, int radius, ServerWorld world) {
        return StructureTestUtil.findStructureBlocks(pos, radius, world).filter(structureBlockPos -> StructureTestUtil.isInStructureBounds(structureBlockPos, pos, world)).findFirst();
    }

    public static Optional<BlockPos> findNearestStructureBlock(BlockPos pos, int radius, ServerWorld world) {
        Comparator<BlockPos> comparator = Comparator.comparingInt(posx -> posx.getManhattanDistance(pos));
        return StructureTestUtil.findStructureBlocks(pos, radius, world).min(comparator);
    }

    public static Stream<BlockPos> findStructureBlocks(BlockPos pos, int radius, ServerWorld world, String templateName) {
        return StructureTestUtil.findStructureBlocks(pos, radius, world).map(posx -> (StructureBlockBlockEntity)world.getBlockEntity((BlockPos)posx)).filter(Objects::nonNull).filter(blockEntity -> Objects.equals(blockEntity.getTemplateName(), templateName)).map(BlockEntity::getPos).map(BlockPos::toImmutable);
    }

    public static Stream<BlockPos> findStructureBlocks(BlockPos pos, int radius, ServerWorld world) {
        BlockBox lv = StructureTestUtil.createBox(pos, radius, world);
        return BlockPos.stream(lv).filter(p -> world.getBlockState((BlockPos)p).isOf(Blocks.STRUCTURE_BLOCK)).map(BlockPos::toImmutable);
    }

    private static StructureBlockBlockEntity placeStructureTemplate(GameTestState state, BlockPos pos, BlockRotation rotation, ServerWorld world) {
        world.setBlockState(pos, Blocks.STRUCTURE_BLOCK.getDefaultState());
        StructureBlockBlockEntity lv = (StructureBlockBlockEntity)world.getBlockEntity(pos);
        lv.setMode(StructureBlockMode.LOAD);
        lv.setRotation(rotation);
        lv.setIgnoreEntities(false);
        lv.setTemplateName(Identifier.of(state.getTemplateName()));
        lv.setMetadata(state.getTemplatePath());
        if (!lv.loadStructure(world)) {
            throw new RuntimeException("Failed to load structure info for test: " + state.getTemplatePath() + ". Structure name: " + state.getTemplateName());
        }
        return lv;
    }

    private static BlockBox createBox(BlockPos pos, int radius, ServerWorld world) {
        BlockPos lv = BlockPos.ofFloored(pos.getX(), world.getTopPosition(Heightmap.Type.WORLD_SURFACE, pos).getY(), pos.getZ());
        return new BlockBox(lv).expand(radius, 10, radius);
    }

    public static Stream<BlockPos> findTargetedStructureBlock(BlockPos pos, Entity entity, ServerWorld world) {
        int i = 200;
        Vec3d lv = entity.getEyePos();
        Vec3d lv2 = lv.add(entity.getRotationVector().multiply(200.0));
        return StructureTestUtil.findStructureBlocks(pos, 200, world).map(p -> world.getBlockEntity((BlockPos)p, BlockEntityType.STRUCTURE_BLOCK)).flatMap(Optional::stream).filter(blockEntity -> StructureTestUtil.getStructureBoundingBox(blockEntity).raycast(lv, lv2).isPresent()).map(BlockEntity::getPos).sorted(Comparator.comparing(pos::getSquaredDistance)).limit(1L);
    }

    private static void resetBlock(int altitude, BlockPos pos, ServerWorld world) {
        BlockState lv = pos.getY() < altitude ? Blocks.STONE.getDefaultState() : Blocks.AIR.getDefaultState();
        BlockStateArgument lv2 = new BlockStateArgument(lv, Collections.emptySet(), null);
        lv2.setBlockState(world, pos, Block.NOTIFY_LISTENERS);
        world.updateNeighbors(pos, lv.getBlock());
    }

    private static boolean isInStructureBounds(BlockPos structureBlockPos, BlockPos pos, ServerWorld world) {
        StructureBlockBlockEntity lv = (StructureBlockBlockEntity)world.getBlockEntity(structureBlockPos);
        return StructureTestUtil.getStructureBlockBox(lv).contains(pos);
    }
}

