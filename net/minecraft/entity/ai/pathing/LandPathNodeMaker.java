/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 * 
 * Could not load the following classes:
 *  org.jetbrains.annotations.Nullable
 */
package net.minecraft.entity.ai.pathing;

import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.Object2BooleanMap;
import it.unimi.dsi.fastutil.objects.Object2BooleanOpenHashMap;
import java.util.EnumSet;
import java.util.Set;
import net.minecraft.block.AbstractRailBlock;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.DoorBlock;
import net.minecraft.block.FenceGateBlock;
import net.minecraft.block.LeavesBlock;
import net.minecraft.entity.ai.pathing.NavigationType;
import net.minecraft.entity.ai.pathing.PathContext;
import net.minecraft.entity.ai.pathing.PathNode;
import net.minecraft.entity.ai.pathing.PathNodeMaker;
import net.minecraft.entity.ai.pathing.PathNodeType;
import net.minecraft.entity.ai.pathing.TargetPathNode;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.fluid.FluidState;
import net.minecraft.fluid.Fluids;
import net.minecraft.registry.tag.BlockTags;
import net.minecraft.registry.tag.FluidTags;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.world.BlockView;
import net.minecraft.world.CollisionView;
import net.minecraft.world.chunk.ChunkCache;
import org.jetbrains.annotations.Nullable;

public class LandPathNodeMaker
extends PathNodeMaker {
    public static final double Y_OFFSET = 0.5;
    private static final double MIN_STEP_HEIGHT = 1.125;
    private final Long2ObjectMap<PathNodeType> nodeTypes = new Long2ObjectOpenHashMap<PathNodeType>();
    private final Object2BooleanMap<Box> collidedBoxes = new Object2BooleanOpenHashMap<Box>();
    private final PathNode[] successors = new PathNode[Direction.Type.HORIZONTAL.getFacingCount()];

    @Override
    public void init(ChunkCache cachedWorld, MobEntity entity) {
        super.init(cachedWorld, entity);
        entity.onStartPathfinding();
    }

    @Override
    public void clear() {
        this.entity.onFinishPathfinding();
        this.nodeTypes.clear();
        this.collidedBoxes.clear();
        super.clear();
    }

    @Override
    public PathNode getStart() {
        BlockPos.Mutable lv = new BlockPos.Mutable();
        int i = this.entity.getBlockY();
        BlockState lv2 = this.context.getBlockState(lv.set(this.entity.getX(), (double)i, this.entity.getZ()));
        if (this.entity.canWalkOnFluid(lv2.getFluidState())) {
            while (this.entity.canWalkOnFluid(lv2.getFluidState())) {
                lv2 = this.context.getBlockState(lv.set(this.entity.getX(), (double)(++i), this.entity.getZ()));
            }
            --i;
        } else if (this.canSwim() && this.entity.isTouchingWater()) {
            while (lv2.isOf(Blocks.WATER) || lv2.getFluidState() == Fluids.WATER.getStill(false)) {
                lv2 = this.context.getBlockState(lv.set(this.entity.getX(), (double)(++i), this.entity.getZ()));
            }
            --i;
        } else if (this.entity.isOnGround()) {
            i = MathHelper.floor(this.entity.getY() + 0.5);
        } else {
            lv.set(this.entity.getX(), this.entity.getY() + 1.0, this.entity.getZ());
            while (lv.getY() > this.context.getWorld().getBottomY()) {
                i = lv.getY();
                lv.setY(lv.getY() - 1);
                BlockState lv3 = this.context.getBlockState(lv);
                if (lv3.isAir() || lv3.canPathfindThrough(NavigationType.LAND)) continue;
                break;
            }
        }
        BlockPos lv4 = this.entity.getBlockPos();
        if (!this.canPathThrough(lv.set(lv4.getX(), i, lv4.getZ()))) {
            Box lv5 = this.entity.getBoundingBox();
            if (this.canPathThrough(lv.set(lv5.minX, (double)i, lv5.minZ)) || this.canPathThrough(lv.set(lv5.minX, (double)i, lv5.maxZ)) || this.canPathThrough(lv.set(lv5.maxX, (double)i, lv5.minZ)) || this.canPathThrough(lv.set(lv5.maxX, (double)i, lv5.maxZ))) {
                return this.getStart(lv);
            }
        }
        return this.getStart(new BlockPos(lv4.getX(), i, lv4.getZ()));
    }

    protected PathNode getStart(BlockPos pos) {
        PathNode lv = this.getNode(pos);
        lv.type = this.getNodeType(lv.x, lv.y, lv.z);
        lv.penalty = this.entity.getPathfindingPenalty(lv.type);
        return lv;
    }

    protected boolean canPathThrough(BlockPos pos) {
        PathNodeType lv = this.getNodeType(pos.getX(), pos.getY(), pos.getZ());
        return lv != PathNodeType.OPEN && this.entity.getPathfindingPenalty(lv) >= 0.0f;
    }

    @Override
    public TargetPathNode getNode(double x, double y, double z) {
        return this.createNode(x, y, z);
    }

    @Override
    public int getSuccessors(PathNode[] successors, PathNode node) {
        int i = 0;
        int j = 0;
        PathNodeType lv = this.getNodeType(node.x, node.y + 1, node.z);
        PathNodeType lv2 = this.getNodeType(node.x, node.y, node.z);
        if (this.entity.getPathfindingPenalty(lv) >= 0.0f && lv2 != PathNodeType.STICKY_HONEY) {
            j = MathHelper.floor(Math.max(1.0f, this.entity.getStepHeight()));
        }
        double d = this.getFeetY(new BlockPos(node.x, node.y, node.z));
        for (Direction lv3 : Direction.Type.HORIZONTAL) {
            PathNode lv4;
            this.successors[lv3.getHorizontal()] = lv4 = this.getPathNode(node.x + lv3.getOffsetX(), node.y, node.z + lv3.getOffsetZ(), j, d, lv3, lv2);
            if (!this.isValidAdjacentSuccessor(lv4, node)) continue;
            successors[i++] = lv4;
        }
        for (Direction lv3 : Direction.Type.HORIZONTAL) {
            PathNode lv6;
            Direction lv5 = lv3.rotateYClockwise();
            if (!this.isValidDiagonalSuccessor(node, this.successors[lv3.getHorizontal()], this.successors[lv5.getHorizontal()]) || !this.isValidDiagonalSuccessor(lv6 = this.getPathNode(node.x + lv3.getOffsetX() + lv5.getOffsetX(), node.y, node.z + lv3.getOffsetZ() + lv5.getOffsetZ(), j, d, lv3, lv2))) continue;
            successors[i++] = lv6;
        }
        return i;
    }

    protected boolean isValidAdjacentSuccessor(@Nullable PathNode node, PathNode successor) {
        return node != null && !node.visited && (node.penalty >= 0.0f || successor.penalty < 0.0f);
    }

    protected boolean isValidDiagonalSuccessor(PathNode xNode, @Nullable PathNode zNode, @Nullable PathNode xDiagNode) {
        if (xDiagNode == null || zNode == null || xDiagNode.y > xNode.y || zNode.y > xNode.y) {
            return false;
        }
        if (zNode.type == PathNodeType.WALKABLE_DOOR || xDiagNode.type == PathNodeType.WALKABLE_DOOR) {
            return false;
        }
        boolean bl = xDiagNode.type == PathNodeType.FENCE && zNode.type == PathNodeType.FENCE && (double)this.entity.getWidth() < 0.5;
        return (xDiagNode.y < xNode.y || xDiagNode.penalty >= 0.0f || bl) && (zNode.y < xNode.y || zNode.penalty >= 0.0f || bl);
    }

    protected boolean isValidDiagonalSuccessor(@Nullable PathNode node) {
        if (node == null || node.visited) {
            return false;
        }
        if (node.type == PathNodeType.WALKABLE_DOOR) {
            return false;
        }
        return node.penalty >= 0.0f;
    }

    private static boolean isBlocked(PathNodeType nodeType) {
        return nodeType == PathNodeType.FENCE || nodeType == PathNodeType.DOOR_WOOD_CLOSED || nodeType == PathNodeType.DOOR_IRON_CLOSED;
    }

    private boolean isBlocked(PathNode node) {
        Box lv = this.entity.getBoundingBox();
        Vec3d lv2 = new Vec3d((double)node.x - this.entity.getX() + lv.getLengthX() / 2.0, (double)node.y - this.entity.getY() + lv.getLengthY() / 2.0, (double)node.z - this.entity.getZ() + lv.getLengthZ() / 2.0);
        int i = MathHelper.ceil(lv2.length() / lv.getAverageSideLength());
        lv2 = lv2.multiply(1.0f / (float)i);
        for (int j = 1; j <= i; ++j) {
            if (!this.checkBoxCollision(lv = lv.offset(lv2))) continue;
            return false;
        }
        return true;
    }

    protected double getFeetY(BlockPos pos) {
        CollisionView lv = this.context.getWorld();
        if ((this.canSwim() || this.isAmphibious()) && lv.getFluidState(pos).isIn(FluidTags.WATER)) {
            return (double)pos.getY() + 0.5;
        }
        return LandPathNodeMaker.getFeetY(lv, pos);
    }

    public static double getFeetY(BlockView world, BlockPos pos) {
        BlockPos lv = pos.down();
        VoxelShape lv2 = world.getBlockState(lv).getCollisionShape(world, lv);
        return (double)lv.getY() + (lv2.isEmpty() ? 0.0 : lv2.getMax(Direction.Axis.Y));
    }

    protected boolean isAmphibious() {
        return false;
    }

    @Nullable
    protected PathNode getPathNode(int x, int y, int z, int maxYStep, double prevFeetY, Direction direction, PathNodeType nodeType) {
        PathNode lv = null;
        BlockPos.Mutable lv2 = new BlockPos.Mutable();
        double e = this.getFeetY(lv2.set(x, y, z));
        if (e - prevFeetY > this.getStepHeight()) {
            return null;
        }
        PathNodeType lv3 = this.getNodeType(x, y, z);
        float f = this.entity.getPathfindingPenalty(lv3);
        if (f >= 0.0f) {
            lv = this.getNodeWith(x, y, z, lv3, f);
        }
        if (LandPathNodeMaker.isBlocked(nodeType) && lv != null && lv.penalty >= 0.0f && !this.isBlocked(lv)) {
            lv = null;
        }
        if (lv3 == PathNodeType.WALKABLE || this.isAmphibious() && lv3 == PathNodeType.WATER) {
            return lv;
        }
        if ((lv == null || lv.penalty < 0.0f) && maxYStep > 0 && (lv3 != PathNodeType.FENCE || this.canWalkOverFences()) && lv3 != PathNodeType.UNPASSABLE_RAIL && lv3 != PathNodeType.TRAPDOOR && lv3 != PathNodeType.POWDER_SNOW) {
            lv = this.getJumpOnTopNode(x, y, z, maxYStep, prevFeetY, direction, nodeType, lv2);
        } else if (!this.isAmphibious() && lv3 == PathNodeType.WATER && !this.canSwim()) {
            lv = this.getNonWaterNodeBelow(x, y, z, lv);
        } else if (lv3 == PathNodeType.OPEN) {
            lv = this.getOpenNode(x, y, z);
        } else if (LandPathNodeMaker.isBlocked(lv3) && lv == null) {
            lv = this.getNodeWith(x, y, z, lv3);
        }
        return lv;
    }

    private double getStepHeight() {
        return Math.max(1.125, (double)this.entity.getStepHeight());
    }

    private PathNode getNodeWith(int x, int y, int z, PathNodeType type, float penalty) {
        PathNode lv = this.getNode(x, y, z);
        lv.type = type;
        lv.penalty = Math.max(lv.penalty, penalty);
        return lv;
    }

    private PathNode getBlockedNode(int x, int y, int z) {
        PathNode lv = this.getNode(x, y, z);
        lv.type = PathNodeType.BLOCKED;
        lv.penalty = -1.0f;
        return lv;
    }

    private PathNode getNodeWith(int x, int y, int z, PathNodeType type) {
        PathNode lv = this.getNode(x, y, z);
        lv.visited = true;
        lv.type = type;
        lv.penalty = type.getDefaultPenalty();
        return lv;
    }

    @Nullable
    private PathNode getJumpOnTopNode(int x, int y, int z, int maxYStep, double prevFeetY, Direction direction, PathNodeType nodeType, BlockPos.Mutable mutablePos) {
        PathNode lv = this.getPathNode(x, y + 1, z, maxYStep - 1, prevFeetY, direction, nodeType);
        if (lv == null) {
            return null;
        }
        if (this.entity.getWidth() >= 1.0f) {
            return lv;
        }
        if (lv.type != PathNodeType.OPEN && lv.type != PathNodeType.WALKABLE) {
            return lv;
        }
        double e = (double)(x - direction.getOffsetX()) + 0.5;
        double f = (double)(z - direction.getOffsetZ()) + 0.5;
        double g = (double)this.entity.getWidth() / 2.0;
        Box lv2 = new Box(e - g, this.getFeetY(mutablePos.set(e, (double)(y + 1), f)) + 0.001, f - g, e + g, (double)this.entity.getHeight() + this.getFeetY(mutablePos.set((double)lv.x, (double)lv.y, (double)lv.z)) - 0.002, f + g);
        return this.checkBoxCollision(lv2) ? null : lv;
    }

    @Nullable
    private PathNode getNonWaterNodeBelow(int x, int y, int z, @Nullable PathNode node) {
        --y;
        while (y > this.entity.getWorld().getBottomY()) {
            PathNodeType lv = this.getNodeType(x, y, z);
            if (lv != PathNodeType.WATER) {
                return node;
            }
            node = this.getNodeWith(x, y, z, lv, this.entity.getPathfindingPenalty(lv));
            --y;
        }
        return node;
    }

    private PathNode getOpenNode(int x, int y, int z) {
        for (int l = y - 1; l >= this.entity.getWorld().getBottomY(); --l) {
            if (y - l > this.entity.getSafeFallDistance()) {
                return this.getBlockedNode(x, l, z);
            }
            PathNodeType lv = this.getNodeType(x, l, z);
            float f = this.entity.getPathfindingPenalty(lv);
            if (lv == PathNodeType.OPEN) continue;
            if (f >= 0.0f) {
                return this.getNodeWith(x, l, z, lv, f);
            }
            return this.getBlockedNode(x, l, z);
        }
        return this.getBlockedNode(x, y, z);
    }

    private boolean checkBoxCollision(Box box) {
        return this.collidedBoxes.computeIfAbsent(box, box2 -> !this.context.getWorld().isSpaceEmpty(this.entity, box));
    }

    protected PathNodeType getNodeType(int x, int y, int z) {
        return this.nodeTypes.computeIfAbsent(BlockPos.asLong(x, y, z), l -> this.getNodeType(this.context, x, y, z, this.entity));
    }

    @Override
    public PathNodeType getNodeType(PathContext context, int x, int y, int z, MobEntity mob) {
        Set<PathNodeType> set = this.getCollidingNodeTypes(context, x, y, z);
        if (set.contains((Object)PathNodeType.FENCE)) {
            return PathNodeType.FENCE;
        }
        if (set.contains((Object)PathNodeType.UNPASSABLE_RAIL)) {
            return PathNodeType.UNPASSABLE_RAIL;
        }
        PathNodeType lv = PathNodeType.BLOCKED;
        for (PathNodeType lv2 : set) {
            if (mob.getPathfindingPenalty(lv2) < 0.0f) {
                return lv2;
            }
            if (!(mob.getPathfindingPenalty(lv2) >= mob.getPathfindingPenalty(lv))) continue;
            lv = lv2;
        }
        if (this.entityBlockXSize <= 1 && lv != PathNodeType.OPEN && mob.getPathfindingPenalty(lv) == 0.0f && this.getDefaultNodeType(context, x, y, z) == PathNodeType.OPEN) {
            return PathNodeType.OPEN;
        }
        return lv;
    }

    public Set<PathNodeType> getCollidingNodeTypes(PathContext context, int x, int y, int z) {
        EnumSet<PathNodeType> enumSet = EnumSet.noneOf(PathNodeType.class);
        for (int l = 0; l < this.entityBlockXSize; ++l) {
            for (int m = 0; m < this.entityBlockYSize; ++m) {
                for (int n = 0; n < this.entityBlockZSize; ++n) {
                    int o = l + x;
                    int p = m + y;
                    int q = n + z;
                    PathNodeType lv = this.getDefaultNodeType(context, o, p, q);
                    BlockPos lv2 = this.entity.getBlockPos();
                    boolean bl = this.canEnterOpenDoors();
                    if (lv == PathNodeType.DOOR_WOOD_CLOSED && this.canOpenDoors() && bl) {
                        lv = PathNodeType.WALKABLE_DOOR;
                    }
                    if (lv == PathNodeType.DOOR_OPEN && !bl) {
                        lv = PathNodeType.BLOCKED;
                    }
                    if (lv == PathNodeType.RAIL && this.getDefaultNodeType(context, lv2.getX(), lv2.getY(), lv2.getZ()) != PathNodeType.RAIL && this.getDefaultNodeType(context, lv2.getX(), lv2.getY() - 1, lv2.getZ()) != PathNodeType.RAIL) {
                        lv = PathNodeType.UNPASSABLE_RAIL;
                    }
                    enumSet.add(lv);
                }
            }
        }
        return enumSet;
    }

    @Override
    public PathNodeType getDefaultNodeType(PathContext context, int x, int y, int z) {
        return LandPathNodeMaker.getLandNodeType(context, new BlockPos.Mutable(x, y, z));
    }

    public static PathNodeType getLandNodeType(MobEntity entity, BlockPos pos) {
        return LandPathNodeMaker.getLandNodeType(new PathContext(entity.getWorld(), entity), pos.mutableCopy());
    }

    public static PathNodeType getLandNodeType(PathContext context, BlockPos.Mutable pos) {
        int k;
        int j;
        int i = pos.getX();
        PathNodeType lv = context.getNodeType(i, j = pos.getY(), k = pos.getZ());
        if (lv != PathNodeType.OPEN || j < context.getWorld().getBottomY() + 1) {
            return lv;
        }
        return switch (context.getNodeType(i, j - 1, k)) {
            case PathNodeType.OPEN, PathNodeType.WATER, PathNodeType.LAVA, PathNodeType.WALKABLE -> PathNodeType.OPEN;
            case PathNodeType.DAMAGE_FIRE -> PathNodeType.DAMAGE_FIRE;
            case PathNodeType.DAMAGE_OTHER -> PathNodeType.DAMAGE_OTHER;
            case PathNodeType.STICKY_HONEY -> PathNodeType.STICKY_HONEY;
            case PathNodeType.POWDER_SNOW -> PathNodeType.DANGER_POWDER_SNOW;
            case PathNodeType.DAMAGE_CAUTIOUS -> PathNodeType.DAMAGE_CAUTIOUS;
            case PathNodeType.TRAPDOOR -> PathNodeType.DANGER_TRAPDOOR;
            default -> LandPathNodeMaker.getNodeTypeFromNeighbors(context, i, j, k, PathNodeType.WALKABLE);
        };
    }

    public static PathNodeType getNodeTypeFromNeighbors(PathContext context, int x, int y, int z, PathNodeType fallback) {
        for (int l = -1; l <= 1; ++l) {
            for (int m = -1; m <= 1; ++m) {
                for (int n = -1; n <= 1; ++n) {
                    if (l == 0 && n == 0) continue;
                    PathNodeType lv = context.getNodeType(x + l, y + m, z + n);
                    if (lv == PathNodeType.DAMAGE_OTHER) {
                        return PathNodeType.DANGER_OTHER;
                    }
                    if (lv == PathNodeType.DAMAGE_FIRE || lv == PathNodeType.LAVA) {
                        return PathNodeType.DANGER_FIRE;
                    }
                    if (lv == PathNodeType.WATER) {
                        return PathNodeType.WATER_BORDER;
                    }
                    if (lv != PathNodeType.DAMAGE_CAUTIOUS) continue;
                    return PathNodeType.DAMAGE_CAUTIOUS;
                }
            }
        }
        return fallback;
    }

    protected static PathNodeType getCommonNodeType(BlockView world, BlockPos pos) {
        BlockState lv = world.getBlockState(pos);
        Block lv2 = lv.getBlock();
        if (lv.isAir()) {
            return PathNodeType.OPEN;
        }
        if (lv.isIn(BlockTags.TRAPDOORS) || lv.isOf(Blocks.LILY_PAD) || lv.isOf(Blocks.BIG_DRIPLEAF)) {
            return PathNodeType.TRAPDOOR;
        }
        if (lv.isOf(Blocks.POWDER_SNOW)) {
            return PathNodeType.POWDER_SNOW;
        }
        if (lv.isOf(Blocks.CACTUS) || lv.isOf(Blocks.SWEET_BERRY_BUSH)) {
            return PathNodeType.DAMAGE_OTHER;
        }
        if (lv.isOf(Blocks.HONEY_BLOCK)) {
            return PathNodeType.STICKY_HONEY;
        }
        if (lv.isOf(Blocks.COCOA)) {
            return PathNodeType.COCOA;
        }
        if (lv.isOf(Blocks.WITHER_ROSE) || lv.isOf(Blocks.POINTED_DRIPSTONE)) {
            return PathNodeType.DAMAGE_CAUTIOUS;
        }
        FluidState lv3 = lv.getFluidState();
        if (lv3.isIn(FluidTags.LAVA)) {
            return PathNodeType.LAVA;
        }
        if (LandPathNodeMaker.isFireDamaging(lv)) {
            return PathNodeType.DAMAGE_FIRE;
        }
        if (lv2 instanceof DoorBlock) {
            DoorBlock lv4 = (DoorBlock)lv2;
            if (lv.get(DoorBlock.OPEN).booleanValue()) {
                return PathNodeType.DOOR_OPEN;
            }
            return lv4.getBlockSetType().canOpenByHand() ? PathNodeType.DOOR_WOOD_CLOSED : PathNodeType.DOOR_IRON_CLOSED;
        }
        if (lv2 instanceof AbstractRailBlock) {
            return PathNodeType.RAIL;
        }
        if (lv2 instanceof LeavesBlock) {
            return PathNodeType.LEAVES;
        }
        if (lv.isIn(BlockTags.FENCES) || lv.isIn(BlockTags.WALLS) || lv2 instanceof FenceGateBlock && !lv.get(FenceGateBlock.OPEN).booleanValue()) {
            return PathNodeType.FENCE;
        }
        if (!lv.canPathfindThrough(NavigationType.LAND)) {
            return PathNodeType.BLOCKED;
        }
        if (lv3.isIn(FluidTags.WATER)) {
            return PathNodeType.WATER;
        }
        return PathNodeType.OPEN;
    }
}

