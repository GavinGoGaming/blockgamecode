/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.block.piston;

import com.google.common.collect.Lists;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.PistonBlock;
import net.minecraft.block.piston.PistonBehavior;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;

public class PistonHandler {
    public static final int MAX_MOVABLE_BLOCKS = 12;
    private final World world;
    private final BlockPos posFrom;
    private final boolean retracted;
    private final BlockPos posTo;
    private final Direction motionDirection;
    private final List<BlockPos> movedBlocks = Lists.newArrayList();
    private final List<BlockPos> brokenBlocks = Lists.newArrayList();
    private final Direction pistonDirection;

    public PistonHandler(World world, BlockPos pos, Direction dir, boolean retracted) {
        this.world = world;
        this.posFrom = pos;
        this.pistonDirection = dir;
        this.retracted = retracted;
        if (retracted) {
            this.motionDirection = dir;
            this.posTo = pos.offset(dir);
        } else {
            this.motionDirection = dir.getOpposite();
            this.posTo = pos.offset(dir, 2);
        }
    }

    public boolean calculatePush() {
        this.movedBlocks.clear();
        this.brokenBlocks.clear();
        BlockState lv = this.world.getBlockState(this.posTo);
        if (!PistonBlock.isMovable(lv, this.world, this.posTo, this.motionDirection, false, this.pistonDirection)) {
            if (this.retracted && lv.getPistonBehavior() == PistonBehavior.DESTROY) {
                this.brokenBlocks.add(this.posTo);
                return true;
            }
            return false;
        }
        if (!this.tryMove(this.posTo, this.motionDirection)) {
            return false;
        }
        for (int i = 0; i < this.movedBlocks.size(); ++i) {
            BlockPos lv2 = this.movedBlocks.get(i);
            if (!PistonHandler.isBlockSticky(this.world.getBlockState(lv2)) || this.tryMoveAdjacentBlock(lv2)) continue;
            return false;
        }
        return true;
    }

    private static boolean isBlockSticky(BlockState state) {
        return state.isOf(Blocks.SLIME_BLOCK) || state.isOf(Blocks.HONEY_BLOCK);
    }

    private static boolean isAdjacentBlockStuck(BlockState state, BlockState adjacentState) {
        if (state.isOf(Blocks.HONEY_BLOCK) && adjacentState.isOf(Blocks.SLIME_BLOCK)) {
            return false;
        }
        if (state.isOf(Blocks.SLIME_BLOCK) && adjacentState.isOf(Blocks.HONEY_BLOCK)) {
            return false;
        }
        return PistonHandler.isBlockSticky(state) || PistonHandler.isBlockSticky(adjacentState);
    }

    private boolean tryMove(BlockPos pos, Direction dir) {
        int k;
        BlockState lv = this.world.getBlockState(pos);
        if (lv.isAir()) {
            return true;
        }
        if (!PistonBlock.isMovable(lv, this.world, pos, this.motionDirection, false, dir)) {
            return true;
        }
        if (pos.equals(this.posFrom)) {
            return true;
        }
        if (this.movedBlocks.contains(pos)) {
            return true;
        }
        int i = 1;
        if (i + this.movedBlocks.size() > 12) {
            return false;
        }
        while (PistonHandler.isBlockSticky(lv)) {
            BlockPos lv2 = pos.offset(this.motionDirection.getOpposite(), i);
            BlockState lv3 = lv;
            lv = this.world.getBlockState(lv2);
            if (lv.isAir() || !PistonHandler.isAdjacentBlockStuck(lv3, lv) || !PistonBlock.isMovable(lv, this.world, lv2, this.motionDirection, false, this.motionDirection.getOpposite()) || lv2.equals(this.posFrom)) break;
            if (++i + this.movedBlocks.size() <= 12) continue;
            return false;
        }
        int j = 0;
        for (k = i - 1; k >= 0; --k) {
            this.movedBlocks.add(pos.offset(this.motionDirection.getOpposite(), k));
            ++j;
        }
        k = 1;
        while (true) {
            BlockPos lv4;
            int l;
            if ((l = this.movedBlocks.indexOf(lv4 = pos.offset(this.motionDirection, k))) > -1) {
                this.setMovedBlocks(j, l);
                for (int m = 0; m <= l + j; ++m) {
                    BlockPos lv5 = this.movedBlocks.get(m);
                    if (!PistonHandler.isBlockSticky(this.world.getBlockState(lv5)) || this.tryMoveAdjacentBlock(lv5)) continue;
                    return false;
                }
                return true;
            }
            lv = this.world.getBlockState(lv4);
            if (lv.isAir()) {
                return true;
            }
            if (!PistonBlock.isMovable(lv, this.world, lv4, this.motionDirection, true, this.motionDirection) || lv4.equals(this.posFrom)) {
                return false;
            }
            if (lv.getPistonBehavior() == PistonBehavior.DESTROY) {
                this.brokenBlocks.add(lv4);
                return true;
            }
            if (this.movedBlocks.size() >= 12) {
                return false;
            }
            this.movedBlocks.add(lv4);
            ++j;
            ++k;
        }
    }

    private void setMovedBlocks(int from, int to) {
        ArrayList<BlockPos> list = Lists.newArrayList();
        ArrayList<BlockPos> list2 = Lists.newArrayList();
        ArrayList<BlockPos> list3 = Lists.newArrayList();
        list.addAll(this.movedBlocks.subList(0, to));
        list2.addAll(this.movedBlocks.subList(this.movedBlocks.size() - from, this.movedBlocks.size()));
        list3.addAll(this.movedBlocks.subList(to, this.movedBlocks.size() - from));
        this.movedBlocks.clear();
        this.movedBlocks.addAll(list);
        this.movedBlocks.addAll(list2);
        this.movedBlocks.addAll(list3);
    }

    private boolean tryMoveAdjacentBlock(BlockPos pos) {
        BlockState lv = this.world.getBlockState(pos);
        for (Direction lv2 : Direction.values()) {
            BlockPos lv3;
            BlockState lv4;
            if (lv2.getAxis() == this.motionDirection.getAxis() || !PistonHandler.isAdjacentBlockStuck(lv4 = this.world.getBlockState(lv3 = pos.offset(lv2)), lv) || this.tryMove(lv3, lv2)) continue;
            return false;
        }
        return true;
    }

    public Direction getMotionDirection() {
        return this.motionDirection;
    }

    public List<BlockPos> getMovedBlocks() {
        return this.movedBlocks;
    }

    public List<BlockPos> getBrokenBlocks() {
        return this.brokenBlocks;
    }
}

