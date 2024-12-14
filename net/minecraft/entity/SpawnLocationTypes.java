/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 * 
 * Could not load the following classes:
 *  org.jetbrains.annotations.Nullable
 */
package net.minecraft.entity;

import net.minecraft.block.BlockState;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.SpawnLocation;
import net.minecraft.entity.ai.pathing.NavigationType;
import net.minecraft.registry.tag.FluidTags;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.SpawnHelper;
import net.minecraft.world.WorldView;
import org.jetbrains.annotations.Nullable;

public interface SpawnLocationTypes {
    public static final SpawnLocation UNRESTRICTED = (world, pos, entityType) -> true;
    public static final SpawnLocation IN_WATER = (world, pos, entityType) -> {
        if (entityType == null || !world.getWorldBorder().contains(pos)) {
            return false;
        }
        BlockPos lv = pos.up();
        return world.getFluidState(pos).isIn(FluidTags.WATER) && !world.getBlockState(lv).isSolidBlock(world, lv);
    };
    public static final SpawnLocation IN_LAVA = (world, pos, entityType) -> {
        if (entityType == null || !world.getWorldBorder().contains(pos)) {
            return false;
        }
        return world.getFluidState(pos).isIn(FluidTags.LAVA);
    };
    public static final SpawnLocation ON_GROUND = new SpawnLocation(){

        @Override
        public boolean isSpawnPositionOk(WorldView arg, BlockPos arg2, @Nullable EntityType<?> arg3) {
            if (arg3 == null || !arg.getWorldBorder().contains(arg2)) {
                return false;
            }
            BlockPos lv = arg2.up();
            BlockPos lv2 = arg2.down();
            BlockState lv3 = arg.getBlockState(lv2);
            if (!lv3.allowsSpawning(arg, lv2, arg3)) {
                return false;
            }
            return this.isClearForSpawn(arg, arg2, arg3) && this.isClearForSpawn(arg, lv, arg3);
        }

        private boolean isClearForSpawn(WorldView world, BlockPos pos, EntityType<?> entityType) {
            BlockState lv = world.getBlockState(pos);
            return SpawnHelper.isClearForSpawn(world, pos, lv, lv.getFluidState(), entityType);
        }

        @Override
        public BlockPos adjustPosition(WorldView world, BlockPos pos) {
            BlockPos lv = pos.down();
            if (world.getBlockState(lv).canPathfindThrough(NavigationType.LAND)) {
                return lv;
            }
            return pos;
        }
    };
}

