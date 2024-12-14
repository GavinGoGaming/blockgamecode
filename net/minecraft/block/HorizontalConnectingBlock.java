/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.block;

import com.mojang.serialization.MapCodec;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import java.util.Map;
import net.minecraft.block.AbstractBlock;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.ConnectingBlock;
import net.minecraft.block.ShapeContext;
import net.minecraft.block.Waterloggable;
import net.minecraft.entity.ai.pathing.NavigationType;
import net.minecraft.fluid.FluidState;
import net.minecraft.fluid.Fluids;
import net.minecraft.state.property.BooleanProperty;
import net.minecraft.state.property.Properties;
import net.minecraft.util.BlockMirror;
import net.minecraft.util.BlockRotation;
import net.minecraft.util.Util;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.util.shape.VoxelShapes;
import net.minecraft.world.BlockView;

public abstract class HorizontalConnectingBlock
extends Block
implements Waterloggable {
    public static final BooleanProperty NORTH = ConnectingBlock.NORTH;
    public static final BooleanProperty EAST = ConnectingBlock.EAST;
    public static final BooleanProperty SOUTH = ConnectingBlock.SOUTH;
    public static final BooleanProperty WEST = ConnectingBlock.WEST;
    public static final BooleanProperty WATERLOGGED = Properties.WATERLOGGED;
    protected static final Map<Direction, BooleanProperty> FACING_PROPERTIES = ConnectingBlock.FACING_PROPERTIES.entrySet().stream().filter(entry -> ((Direction)entry.getKey()).getAxis().isHorizontal()).collect(Util.toMap());
    protected final VoxelShape[] collisionShapes;
    protected final VoxelShape[] boundingShapes;
    private final Object2IntMap<BlockState> SHAPE_INDEX_CACHE = new Object2IntOpenHashMap<BlockState>();

    protected HorizontalConnectingBlock(float radius1, float radius2, float boundingHeight1, float boundingHeight2, float collisionHeight, AbstractBlock.Settings settings) {
        super(settings);
        this.collisionShapes = this.createShapes(radius1, radius2, collisionHeight, 0.0f, collisionHeight);
        this.boundingShapes = this.createShapes(radius1, radius2, boundingHeight1, 0.0f, boundingHeight2);
        for (BlockState lv : this.stateManager.getStates()) {
            this.getShapeIndex(lv);
        }
    }

    protected abstract MapCodec<? extends HorizontalConnectingBlock> getCodec();

    protected VoxelShape[] createShapes(float radius1, float radius2, float height1, float offset2, float height2) {
        float k = 8.0f - radius1;
        float l = 8.0f + radius1;
        float m = 8.0f - radius2;
        float n = 8.0f + radius2;
        VoxelShape lv = Block.createCuboidShape(k, 0.0, k, l, height1, l);
        VoxelShape lv2 = Block.createCuboidShape(m, offset2, 0.0, n, height2, n);
        VoxelShape lv3 = Block.createCuboidShape(m, offset2, m, n, height2, 16.0);
        VoxelShape lv4 = Block.createCuboidShape(0.0, offset2, m, n, height2, n);
        VoxelShape lv5 = Block.createCuboidShape(m, offset2, m, 16.0, height2, n);
        VoxelShape lv6 = VoxelShapes.union(lv2, lv5);
        VoxelShape lv7 = VoxelShapes.union(lv3, lv4);
        VoxelShape[] lvs = new VoxelShape[]{VoxelShapes.empty(), lv3, lv4, lv7, lv2, VoxelShapes.union(lv3, lv2), VoxelShapes.union(lv4, lv2), VoxelShapes.union(lv7, lv2), lv5, VoxelShapes.union(lv3, lv5), VoxelShapes.union(lv4, lv5), VoxelShapes.union(lv7, lv5), lv6, VoxelShapes.union(lv3, lv6), VoxelShapes.union(lv4, lv6), VoxelShapes.union(lv7, lv6)};
        for (int o = 0; o < 16; ++o) {
            lvs[o] = VoxelShapes.union(lv, lvs[o]);
        }
        return lvs;
    }

    @Override
    protected boolean isTransparent(BlockState state, BlockView world, BlockPos pos) {
        return state.get(WATERLOGGED) == false;
    }

    @Override
    protected VoxelShape getOutlineShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context) {
        return this.boundingShapes[this.getShapeIndex(state)];
    }

    @Override
    protected VoxelShape getCollisionShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context) {
        return this.collisionShapes[this.getShapeIndex(state)];
    }

    private static int getDirectionMask(Direction dir) {
        return 1 << dir.getHorizontal();
    }

    protected int getShapeIndex(BlockState state) {
        return this.SHAPE_INDEX_CACHE.computeIntIfAbsent(state, statex -> {
            int i = 0;
            if (statex.get(NORTH).booleanValue()) {
                i |= HorizontalConnectingBlock.getDirectionMask(Direction.NORTH);
            }
            if (statex.get(EAST).booleanValue()) {
                i |= HorizontalConnectingBlock.getDirectionMask(Direction.EAST);
            }
            if (statex.get(SOUTH).booleanValue()) {
                i |= HorizontalConnectingBlock.getDirectionMask(Direction.SOUTH);
            }
            if (statex.get(WEST).booleanValue()) {
                i |= HorizontalConnectingBlock.getDirectionMask(Direction.WEST);
            }
            return i;
        });
    }

    @Override
    protected FluidState getFluidState(BlockState state) {
        if (state.get(WATERLOGGED).booleanValue()) {
            return Fluids.WATER.getStill(false);
        }
        return super.getFluidState(state);
    }

    @Override
    protected boolean canPathfindThrough(BlockState state, NavigationType type) {
        return false;
    }

    @Override
    protected BlockState rotate(BlockState state, BlockRotation rotation) {
        switch (rotation) {
            case CLOCKWISE_180: {
                return (BlockState)((BlockState)((BlockState)((BlockState)state.with(NORTH, state.get(SOUTH))).with(EAST, state.get(WEST))).with(SOUTH, state.get(NORTH))).with(WEST, state.get(EAST));
            }
            case COUNTERCLOCKWISE_90: {
                return (BlockState)((BlockState)((BlockState)((BlockState)state.with(NORTH, state.get(EAST))).with(EAST, state.get(SOUTH))).with(SOUTH, state.get(WEST))).with(WEST, state.get(NORTH));
            }
            case CLOCKWISE_90: {
                return (BlockState)((BlockState)((BlockState)((BlockState)state.with(NORTH, state.get(WEST))).with(EAST, state.get(NORTH))).with(SOUTH, state.get(EAST))).with(WEST, state.get(SOUTH));
            }
        }
        return state;
    }

    @Override
    protected BlockState mirror(BlockState state, BlockMirror mirror) {
        switch (mirror) {
            case LEFT_RIGHT: {
                return (BlockState)((BlockState)state.with(NORTH, state.get(SOUTH))).with(SOUTH, state.get(NORTH));
            }
            case FRONT_BACK: {
                return (BlockState)((BlockState)state.with(EAST, state.get(WEST))).with(WEST, state.get(EAST));
            }
        }
        return super.mirror(state, mirror);
    }
}

