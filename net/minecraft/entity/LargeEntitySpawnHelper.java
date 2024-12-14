/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.entity;

import java.util.Optional;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.LeavesBlock;
import net.minecraft.block.StainedGlassBlock;
import net.minecraft.block.StainedGlassPaneBlock;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.SpawnReason;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3i;

public class LargeEntitySpawnHelper {
    public static <T extends MobEntity> Optional<T> trySpawnAt(EntityType<T> entityType, SpawnReason reason, ServerWorld world, BlockPos pos, int tries, int horizontalRange, int verticalRange, Requirements requirements) {
        BlockPos.Mutable lv = pos.mutableCopy();
        for (int l = 0; l < tries; ++l) {
            MobEntity lv2;
            int m = MathHelper.nextBetween(world.random, -horizontalRange, horizontalRange);
            int n = MathHelper.nextBetween(world.random, -horizontalRange, horizontalRange);
            lv.set(pos, m, verticalRange, n);
            if (!world.getWorldBorder().contains(lv) || !LargeEntitySpawnHelper.findSpawnPos(world, verticalRange, lv, requirements) || (lv2 = (MobEntity)entityType.create(world, null, lv, reason, false, false)) == null) continue;
            if (lv2.canSpawn(world, reason) && lv2.canSpawn(world)) {
                world.spawnEntityAndPassengers(lv2);
                return Optional.of(lv2);
            }
            lv2.discard();
        }
        return Optional.empty();
    }

    private static boolean findSpawnPos(ServerWorld world, int verticalRange, BlockPos.Mutable pos, Requirements requirements) {
        BlockPos.Mutable lv = new BlockPos.Mutable().set(pos);
        BlockState lv2 = world.getBlockState(lv);
        for (int j = verticalRange; j >= -verticalRange; --j) {
            pos.move(Direction.DOWN);
            lv.set((Vec3i)pos, Direction.UP);
            BlockState lv3 = world.getBlockState(pos);
            if (requirements.canSpawnOn(world, pos, lv3, lv, lv2)) {
                pos.move(Direction.UP);
                return true;
            }
            lv2 = lv3;
        }
        return false;
    }

    public static interface Requirements {
        @Deprecated
        public static final Requirements IRON_GOLEM = (world, pos, state, abovePos, aboveState) -> {
            if (state.isOf(Blocks.COBWEB) || state.isOf(Blocks.CACTUS) || state.isOf(Blocks.GLASS_PANE) || state.getBlock() instanceof StainedGlassPaneBlock || state.getBlock() instanceof StainedGlassBlock || state.getBlock() instanceof LeavesBlock || state.isOf(Blocks.CONDUIT) || state.isOf(Blocks.ICE) || state.isOf(Blocks.TNT) || state.isOf(Blocks.GLOWSTONE) || state.isOf(Blocks.BEACON) || state.isOf(Blocks.SEA_LANTERN) || state.isOf(Blocks.FROSTED_ICE) || state.isOf(Blocks.TINTED_GLASS) || state.isOf(Blocks.GLASS)) {
                return false;
            }
            return !(!aboveState.isAir() && !aboveState.isLiquid() || !state.isSolid() && !state.isOf(Blocks.POWDER_SNOW));
        };
        public static final Requirements WARDEN = (world, pos, state, abovePos, aboveState) -> aboveState.getCollisionShape(world, abovePos).isEmpty() && Block.isFaceFullSquare(state.getCollisionShape(world, pos), Direction.UP);

        public boolean canSpawnOn(ServerWorld var1, BlockPos var2, BlockState var3, BlockPos var4, BlockState var5);
    }
}

