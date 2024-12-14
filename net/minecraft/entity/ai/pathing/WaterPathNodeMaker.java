/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 * 
 * Could not load the following classes:
 *  org.jetbrains.annotations.Nullable
 */
package net.minecraft.entity.ai.pathing;

import com.google.common.collect.Maps;
import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import java.util.EnumMap;
import net.minecraft.block.BlockState;
import net.minecraft.entity.ai.pathing.NavigationType;
import net.minecraft.entity.ai.pathing.PathContext;
import net.minecraft.entity.ai.pathing.PathNode;
import net.minecraft.entity.ai.pathing.PathNodeMaker;
import net.minecraft.entity.ai.pathing.PathNodeType;
import net.minecraft.entity.ai.pathing.TargetPathNode;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.fluid.FluidState;
import net.minecraft.registry.tag.FluidTags;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.chunk.ChunkCache;
import org.jetbrains.annotations.Nullable;

public class WaterPathNodeMaker
extends PathNodeMaker {
    private final boolean canJumpOutOfWater;
    private final Long2ObjectMap<PathNodeType> nodePosToType = new Long2ObjectOpenHashMap<PathNodeType>();

    public WaterPathNodeMaker(boolean canJumpOutOfWater) {
        this.canJumpOutOfWater = canJumpOutOfWater;
    }

    @Override
    public void init(ChunkCache cachedWorld, MobEntity entity) {
        super.init(cachedWorld, entity);
        this.nodePosToType.clear();
    }

    @Override
    public void clear() {
        super.clear();
        this.nodePosToType.clear();
    }

    @Override
    public PathNode getStart() {
        return this.getNode(MathHelper.floor(this.entity.getBoundingBox().minX), MathHelper.floor(this.entity.getBoundingBox().minY + 0.5), MathHelper.floor(this.entity.getBoundingBox().minZ));
    }

    @Override
    public TargetPathNode getNode(double x, double y, double z) {
        return this.createNode(x, y, z);
    }

    @Override
    public int getSuccessors(PathNode[] successors, PathNode node) {
        int i = 0;
        EnumMap<Direction, PathNode> map = Maps.newEnumMap(Direction.class);
        for (Direction lv : Direction.values()) {
            PathNode lv2 = this.getPassableNode(node.x + lv.getOffsetX(), node.y + lv.getOffsetY(), node.z + lv.getOffsetZ());
            map.put(lv, lv2);
            if (!this.hasNotVisited(lv2)) continue;
            successors[i++] = lv2;
        }
        for (Direction lv3 : Direction.Type.HORIZONTAL) {
            PathNode lv5;
            Direction lv4 = lv3.rotateYClockwise();
            if (!WaterPathNodeMaker.hasPenalty((PathNode)map.get(lv3)) || !WaterPathNodeMaker.hasPenalty((PathNode)map.get(lv4)) || !this.hasNotVisited(lv5 = this.getPassableNode(node.x + lv3.getOffsetX() + lv4.getOffsetX(), node.y, node.z + lv3.getOffsetZ() + lv4.getOffsetZ()))) continue;
            successors[i++] = lv5;
        }
        return i;
    }

    protected boolean hasNotVisited(@Nullable PathNode node) {
        return node != null && !node.visited;
    }

    private static boolean hasPenalty(@Nullable PathNode node) {
        return node != null && node.penalty >= 0.0f;
    }

    @Nullable
    protected PathNode getPassableNode(int x, int y, int z) {
        float f;
        PathNode lv = null;
        PathNodeType lv2 = this.addPathNodePos(x, y, z);
        if ((this.canJumpOutOfWater && lv2 == PathNodeType.BREACH || lv2 == PathNodeType.WATER) && (f = this.entity.getPathfindingPenalty(lv2)) >= 0.0f) {
            lv = this.getNode(x, y, z);
            lv.type = lv2;
            lv.penalty = Math.max(lv.penalty, f);
            if (this.context.getWorld().getFluidState(new BlockPos(x, y, z)).isEmpty()) {
                lv.penalty += 8.0f;
            }
        }
        return lv;
    }

    protected PathNodeType addPathNodePos(int x, int y, int z) {
        return this.nodePosToType.computeIfAbsent(BlockPos.asLong(x, y, z), pos -> this.getDefaultNodeType(this.context, x, y, z));
    }

    @Override
    public PathNodeType getDefaultNodeType(PathContext context, int x, int y, int z) {
        return this.getNodeType(context, x, y, z, this.entity);
    }

    @Override
    public PathNodeType getNodeType(PathContext context, int x, int y, int z, MobEntity mob) {
        BlockPos.Mutable lv = new BlockPos.Mutable();
        for (int l = x; l < x + this.entityBlockXSize; ++l) {
            for (int m = y; m < y + this.entityBlockYSize; ++m) {
                for (int n = z; n < z + this.entityBlockZSize; ++n) {
                    BlockState lv2 = context.getBlockState(lv.set(l, m, n));
                    FluidState lv3 = lv2.getFluidState();
                    if (lv3.isEmpty() && lv2.canPathfindThrough(NavigationType.WATER) && lv2.isAir()) {
                        return PathNodeType.BREACH;
                    }
                    if (lv3.isIn(FluidTags.WATER)) continue;
                    return PathNodeType.BLOCKED;
                }
            }
        }
        BlockState lv4 = context.getBlockState(lv);
        if (lv4.canPathfindThrough(NavigationType.WATER)) {
            return PathNodeType.WATER;
        }
        return PathNodeType.BLOCKED;
    }
}

