/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 * 
 * Could not load the following classes:
 *  org.jetbrains.annotations.Nullable
 */
package net.minecraft.entity.ai.goal;

import net.minecraft.block.Block;
import net.minecraft.entity.ai.goal.MoveToTargetPosGoal;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.mob.PathAwareEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.particle.ItemStackParticleEffect;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkSectionPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.BlockView;
import net.minecraft.world.GameRules;
import net.minecraft.world.World;
import net.minecraft.world.WorldAccess;
import net.minecraft.world.WorldView;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.ChunkStatus;
import org.jetbrains.annotations.Nullable;

public class StepAndDestroyBlockGoal
extends MoveToTargetPosGoal {
    private final Block targetBlock;
    private final MobEntity stepAndDestroyMob;
    private int counter;
    private static final int MAX_COOLDOWN = 20;

    public StepAndDestroyBlockGoal(Block targetBlock, PathAwareEntity mob, double speed, int maxYDifference) {
        super(mob, speed, 24, maxYDifference);
        this.targetBlock = targetBlock;
        this.stepAndDestroyMob = mob;
    }

    @Override
    public boolean canStart() {
        if (!this.stepAndDestroyMob.getWorld().getGameRules().getBoolean(GameRules.DO_MOB_GRIEFING)) {
            return false;
        }
        if (this.cooldown > 0) {
            --this.cooldown;
            return false;
        }
        if (this.findTargetPos()) {
            this.cooldown = StepAndDestroyBlockGoal.toGoalTicks(20);
            return true;
        }
        this.cooldown = this.getInterval(this.mob);
        return false;
    }

    @Override
    public void stop() {
        super.stop();
        this.stepAndDestroyMob.fallDistance = 1.0f;
    }

    @Override
    public void start() {
        super.start();
        this.counter = 0;
    }

    public void tickStepping(WorldAccess world, BlockPos pos) {
    }

    public void onDestroyBlock(World world, BlockPos pos) {
    }

    @Override
    public void tick() {
        super.tick();
        World lv = this.stepAndDestroyMob.getWorld();
        BlockPos lv2 = this.stepAndDestroyMob.getBlockPos();
        BlockPos lv3 = this.tweakToProperPos(lv2, lv);
        Random lv4 = this.stepAndDestroyMob.getRandom();
        if (this.hasReached() && lv3 != null) {
            double d;
            Vec3d lv5;
            if (this.counter > 0) {
                lv5 = this.stepAndDestroyMob.getVelocity();
                this.stepAndDestroyMob.setVelocity(lv5.x, 0.3, lv5.z);
                if (!lv.isClient) {
                    d = 0.08;
                    ((ServerWorld)lv).spawnParticles(new ItemStackParticleEffect(ParticleTypes.ITEM, new ItemStack(Items.EGG)), (double)lv3.getX() + 0.5, (double)lv3.getY() + 0.7, (double)lv3.getZ() + 0.5, 3, ((double)lv4.nextFloat() - 0.5) * 0.08, ((double)lv4.nextFloat() - 0.5) * 0.08, ((double)lv4.nextFloat() - 0.5) * 0.08, 0.15f);
                }
            }
            if (this.counter % 2 == 0) {
                lv5 = this.stepAndDestroyMob.getVelocity();
                this.stepAndDestroyMob.setVelocity(lv5.x, -0.3, lv5.z);
                if (this.counter % 6 == 0) {
                    this.tickStepping(lv, this.targetPos);
                }
            }
            if (this.counter > 60) {
                lv.removeBlock(lv3, false);
                if (!lv.isClient) {
                    for (int i = 0; i < 20; ++i) {
                        d = lv4.nextGaussian() * 0.02;
                        double e = lv4.nextGaussian() * 0.02;
                        double f = lv4.nextGaussian() * 0.02;
                        ((ServerWorld)lv).spawnParticles(ParticleTypes.POOF, (double)lv3.getX() + 0.5, lv3.getY(), (double)lv3.getZ() + 0.5, 1, d, e, f, 0.15f);
                    }
                    this.onDestroyBlock(lv, lv3);
                }
            }
            ++this.counter;
        }
    }

    @Nullable
    private BlockPos tweakToProperPos(BlockPos pos, BlockView world) {
        BlockPos[] lvs;
        if (world.getBlockState(pos).isOf(this.targetBlock)) {
            return pos;
        }
        for (BlockPos lv : lvs = new BlockPos[]{pos.down(), pos.west(), pos.east(), pos.north(), pos.south(), pos.down().down()}) {
            if (!world.getBlockState(lv).isOf(this.targetBlock)) continue;
            return lv;
        }
        return null;
    }

    @Override
    protected boolean isTargetPos(WorldView world, BlockPos pos) {
        Chunk lv = world.getChunk(ChunkSectionPos.getSectionCoord(pos.getX()), ChunkSectionPos.getSectionCoord(pos.getZ()), ChunkStatus.FULL, false);
        if (lv != null) {
            return lv.getBlockState(pos).isOf(this.targetBlock) && lv.getBlockState(pos.up()).isAir() && lv.getBlockState(pos.up(2)).isAir();
        }
        return false;
    }
}

