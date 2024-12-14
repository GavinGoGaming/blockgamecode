/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.world.dimension;

import java.util.Comparator;
import java.util.Optional;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.NetherPortalBlock;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.state.property.Properties;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3i;
import net.minecraft.world.BlockLocating;
import net.minecraft.world.Heightmap;
import net.minecraft.world.border.WorldBorder;
import net.minecraft.world.poi.PointOfInterest;
import net.minecraft.world.poi.PointOfInterestStorage;
import net.minecraft.world.poi.PointOfInterestTypes;

public class PortalForcer {
    public static final int field_31810 = 3;
    private static final int field_52248 = 16;
    private static final int field_52249 = 128;
    private static final int field_31813 = 5;
    private static final int field_31814 = 4;
    private static final int field_31815 = 3;
    private static final int field_31816 = -1;
    private static final int field_31817 = 4;
    private static final int field_31818 = -1;
    private static final int field_31819 = 3;
    private static final int field_31820 = -1;
    private static final int field_31821 = 2;
    private static final int field_31822 = -1;
    private final ServerWorld world;

    public PortalForcer(ServerWorld world) {
        this.world = world;
    }

    public Optional<BlockPos> getPortalPos(BlockPos pos, boolean destIsNether, WorldBorder worldBorder) {
        PointOfInterestStorage lv = this.world.getPointOfInterestStorage();
        int i = destIsNether ? 16 : 128;
        lv.preloadChunks(this.world, pos, i);
        return lv.getInSquare(poiType -> poiType.matchesKey(PointOfInterestTypes.NETHER_PORTAL), pos, i, PointOfInterestStorage.OccupationStatus.ANY).map(PointOfInterest::getPos).filter(worldBorder::contains).filter(arg -> this.world.getBlockState((BlockPos)arg).contains(Properties.HORIZONTAL_AXIS)).min(Comparator.comparingDouble(arg2 -> arg2.getSquaredDistance(pos)).thenComparingInt(Vec3i::getY));
    }

    public Optional<BlockLocating.Rectangle> createPortal(BlockPos pos, Direction.Axis axis) {
        int n;
        int m;
        int l;
        Direction lv = Direction.get(Direction.AxisDirection.POSITIVE, axis);
        double d = -1.0;
        BlockPos lv2 = null;
        double e = -1.0;
        BlockPos lv3 = null;
        WorldBorder lv4 = this.world.getWorldBorder();
        int i = Math.min(this.world.getTopY(), this.world.getBottomY() + this.world.getLogicalHeight()) - 1;
        boolean j = true;
        BlockPos.Mutable lv5 = pos.mutableCopy();
        for (BlockPos.Mutable lv6 : BlockPos.iterateInSquare(pos, 16, Direction.EAST, Direction.SOUTH)) {
            int k = Math.min(i, this.world.getTopY(Heightmap.Type.MOTION_BLOCKING, lv6.getX(), lv6.getZ()));
            if (!lv4.contains(lv6) || !lv4.contains(lv6.move(lv, 1))) continue;
            lv6.move(lv.getOpposite(), 1);
            for (l = k; l >= this.world.getBottomY(); --l) {
                lv6.setY(l);
                if (!this.isBlockStateValid(lv6)) continue;
                m = l;
                while (l > this.world.getBottomY() && this.isBlockStateValid(lv6.move(Direction.DOWN))) {
                    --l;
                }
                if (l + 4 > i || (n = m - l) > 0 && n < 3) continue;
                lv6.setY(l);
                if (!this.isValidPortalPos(lv6, lv5, lv, 0)) continue;
                double f = pos.getSquaredDistance(lv6);
                if (this.isValidPortalPos(lv6, lv5, lv, -1) && this.isValidPortalPos(lv6, lv5, lv, 1) && (d == -1.0 || d > f)) {
                    d = f;
                    lv2 = lv6.toImmutable();
                }
                if (d != -1.0 || e != -1.0 && !(e > f)) continue;
                e = f;
                lv3 = lv6.toImmutable();
            }
        }
        if (d == -1.0 && e != -1.0) {
            lv2 = lv3;
            d = e;
        }
        if (d == -1.0) {
            int p = i - 9;
            int o = Math.max(this.world.getBottomY() - -1, 70);
            if (p < o) {
                return Optional.empty();
            }
            lv2 = new BlockPos(pos.getX() - lv.getOffsetX() * 1, MathHelper.clamp(pos.getY(), o, p), pos.getZ() - lv.getOffsetZ() * 1).toImmutable();
            lv2 = lv4.clamp(lv2);
            Direction lv7 = lv.rotateYClockwise();
            for (l = -1; l < 2; ++l) {
                for (m = 0; m < 2; ++m) {
                    for (n = -1; n < 3; ++n) {
                        BlockState lv8 = n < 0 ? Blocks.OBSIDIAN.getDefaultState() : Blocks.AIR.getDefaultState();
                        lv5.set(lv2, m * lv.getOffsetX() + l * lv7.getOffsetX(), n, m * lv.getOffsetZ() + l * lv7.getOffsetZ());
                        this.world.setBlockState(lv5, lv8);
                    }
                }
            }
        }
        for (int o = -1; o < 3; ++o) {
            for (int p = -1; p < 4; ++p) {
                if (o != -1 && o != 2 && p != -1 && p != 3) continue;
                lv5.set(lv2, o * lv.getOffsetX(), p, o * lv.getOffsetZ());
                this.world.setBlockState(lv5, Blocks.OBSIDIAN.getDefaultState(), Block.NOTIFY_ALL);
            }
        }
        BlockState lv9 = (BlockState)Blocks.NETHER_PORTAL.getDefaultState().with(NetherPortalBlock.AXIS, axis);
        for (int p = 0; p < 2; ++p) {
            for (int k = 0; k < 3; ++k) {
                lv5.set(lv2, p * lv.getOffsetX(), k, p * lv.getOffsetZ());
                this.world.setBlockState(lv5, lv9, Block.NOTIFY_LISTENERS | Block.FORCE_STATE);
            }
        }
        return Optional.of(new BlockLocating.Rectangle(lv2.toImmutable(), 2, 3));
    }

    private boolean isBlockStateValid(BlockPos.Mutable pos) {
        BlockState lv = this.world.getBlockState(pos);
        return lv.isReplaceable() && lv.getFluidState().isEmpty();
    }

    private boolean isValidPortalPos(BlockPos pos, BlockPos.Mutable temp, Direction portalDirection, int distanceOrthogonalToPortal) {
        Direction lv = portalDirection.rotateYClockwise();
        for (int j = -1; j < 3; ++j) {
            for (int k = -1; k < 4; ++k) {
                temp.set(pos, portalDirection.getOffsetX() * j + lv.getOffsetX() * distanceOrthogonalToPortal, k, portalDirection.getOffsetZ() * j + lv.getOffsetZ() * distanceOrthogonalToPortal);
                if (k < 0 && !this.world.getBlockState(temp).isSolid()) {
                    return false;
                }
                if (k < 0 || this.isBlockStateValid(temp)) continue;
                return false;
            }
        }
        return true;
    }
}

