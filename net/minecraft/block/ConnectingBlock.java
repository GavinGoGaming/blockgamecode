/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.block;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import com.mojang.serialization.MapCodec;
import java.util.Map;
import net.minecraft.block.AbstractBlock;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.ShapeContext;
import net.minecraft.state.property.BooleanProperty;
import net.minecraft.state.property.Properties;
import net.minecraft.util.Util;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.util.shape.VoxelShapes;
import net.minecraft.world.BlockView;

public abstract class ConnectingBlock
extends Block {
    private static final Direction[] FACINGS = Direction.values();
    public static final BooleanProperty NORTH = Properties.NORTH;
    public static final BooleanProperty EAST = Properties.EAST;
    public static final BooleanProperty SOUTH = Properties.SOUTH;
    public static final BooleanProperty WEST = Properties.WEST;
    public static final BooleanProperty UP = Properties.UP;
    public static final BooleanProperty DOWN = Properties.DOWN;
    public static final Map<Direction, BooleanProperty> FACING_PROPERTIES = ImmutableMap.copyOf(Util.make(Maps.newEnumMap(Direction.class), directions -> {
        directions.put(Direction.NORTH, NORTH);
        directions.put(Direction.EAST, EAST);
        directions.put(Direction.SOUTH, SOUTH);
        directions.put(Direction.WEST, WEST);
        directions.put(Direction.UP, UP);
        directions.put(Direction.DOWN, DOWN);
    }));
    protected final VoxelShape[] facingsToShape;

    protected ConnectingBlock(float radius, AbstractBlock.Settings settings) {
        super(settings);
        this.facingsToShape = this.generateFacingsToShapeMap(radius);
    }

    protected abstract MapCodec<? extends ConnectingBlock> getCodec();

    private VoxelShape[] generateFacingsToShapeMap(float radius) {
        float g = 0.5f - radius;
        float h = 0.5f + radius;
        VoxelShape lv = Block.createCuboidShape(g * 16.0f, g * 16.0f, g * 16.0f, h * 16.0f, h * 16.0f, h * 16.0f);
        VoxelShape[] lvs = new VoxelShape[FACINGS.length];
        for (int i = 0; i < FACINGS.length; ++i) {
            Direction lv2 = FACINGS[i];
            lvs[i] = VoxelShapes.cuboid(0.5 + Math.min((double)(-radius), (double)lv2.getOffsetX() * 0.5), 0.5 + Math.min((double)(-radius), (double)lv2.getOffsetY() * 0.5), 0.5 + Math.min((double)(-radius), (double)lv2.getOffsetZ() * 0.5), 0.5 + Math.max((double)radius, (double)lv2.getOffsetX() * 0.5), 0.5 + Math.max((double)radius, (double)lv2.getOffsetY() * 0.5), 0.5 + Math.max((double)radius, (double)lv2.getOffsetZ() * 0.5));
        }
        VoxelShape[] lvs2 = new VoxelShape[64];
        for (int j = 0; j < 64; ++j) {
            VoxelShape lv3 = lv;
            for (int k = 0; k < FACINGS.length; ++k) {
                if ((j & 1 << k) == 0) continue;
                lv3 = VoxelShapes.union(lv3, lvs[k]);
            }
            lvs2[j] = lv3;
        }
        return lvs2;
    }

    @Override
    protected boolean isTransparent(BlockState state, BlockView world, BlockPos pos) {
        return false;
    }

    @Override
    protected VoxelShape getOutlineShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context) {
        return this.facingsToShape[this.getConnectionMask(state)];
    }

    protected int getConnectionMask(BlockState state) {
        int i = 0;
        for (int j = 0; j < FACINGS.length; ++j) {
            if (!((Boolean)state.get(FACING_PROPERTIES.get(FACINGS[j]))).booleanValue()) continue;
            i |= 1 << j;
        }
        return i;
    }
}

