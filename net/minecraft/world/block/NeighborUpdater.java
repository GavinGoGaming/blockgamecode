/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 * 
 * Could not load the following classes:
 *  org.jetbrains.annotations.Nullable
 */
package net.minecraft.world.block;

import java.util.Locale;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.registry.Registries;
import net.minecraft.util.crash.CrashException;
import net.minecraft.util.crash.CrashReport;
import net.minecraft.util.crash.CrashReportSection;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;
import net.minecraft.world.WorldAccess;
import org.jetbrains.annotations.Nullable;

public interface NeighborUpdater {
    public static final Direction[] UPDATE_ORDER = new Direction[]{Direction.WEST, Direction.EAST, Direction.DOWN, Direction.UP, Direction.NORTH, Direction.SOUTH};

    public void replaceWithStateForNeighborUpdate(Direction var1, BlockState var2, BlockPos var3, BlockPos var4, int var5, int var6);

    public void updateNeighbor(BlockPos var1, Block var2, BlockPos var3);

    public void updateNeighbor(BlockState var1, BlockPos var2, Block var3, BlockPos var4, boolean var5);

    default public void updateNeighbors(BlockPos pos, Block sourceBlock, @Nullable Direction except) {
        for (Direction lv : UPDATE_ORDER) {
            if (lv == except) continue;
            this.updateNeighbor(pos.offset(lv), sourceBlock, pos);
        }
    }

    public static void replaceWithStateForNeighborUpdate(WorldAccess world, Direction direction, BlockState neighborState, BlockPos pos, BlockPos neighborPos, int flags, int maxUpdateDepth) {
        BlockState lv = world.getBlockState(pos);
        BlockState lv2 = lv.getStateForNeighborUpdate(direction, neighborState, world, pos, neighborPos);
        Block.replace(lv, lv2, world, pos, flags, maxUpdateDepth);
    }

    public static void tryNeighborUpdate(World world, BlockState state, BlockPos pos, Block sourceBlock, BlockPos sourcePos, boolean notify) {
        try {
            state.neighborUpdate(world, pos, sourceBlock, sourcePos, notify);
        } catch (Throwable throwable) {
            CrashReport lv = CrashReport.create(throwable, "Exception while updating neighbours");
            CrashReportSection lv2 = lv.addElement("Block being updated");
            lv2.add("Source block type", () -> {
                try {
                    return String.format(Locale.ROOT, "ID #%s (%s // %s)", Registries.BLOCK.getId(sourceBlock), sourceBlock.getTranslationKey(), sourceBlock.getClass().getCanonicalName());
                } catch (Throwable throwable) {
                    return "ID #" + String.valueOf(Registries.BLOCK.getId(sourceBlock));
                }
            });
            CrashReportSection.addBlockInfo(lv2, world, pos, state);
            throw new CrashException(lv);
        }
    }
}

