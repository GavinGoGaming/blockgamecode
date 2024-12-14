/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.block.entity;

import java.util.List;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.brain.MemoryModuleType;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.particle.EntityEffectParticleEffect;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.registry.tag.EntityTypeTags;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;
import org.apache.commons.lang3.mutable.MutableInt;

public class BellBlockEntity
extends BlockEntity {
    private static final int MAX_RINGING_TICKS = 50;
    private static final int field_31317 = 60;
    private static final int field_31318 = 60;
    private static final int MAX_RESONATING_TICKS = 40;
    private static final int field_31320 = 5;
    private static final int field_31321 = 48;
    private static final int MAX_BELL_HEARING_DISTANCE = 32;
    private static final int field_31323 = 48;
    private long lastRingTime;
    public int ringTicks;
    public boolean ringing;
    public Direction lastSideHit;
    private List<LivingEntity> hearingEntities;
    private boolean resonating;
    private int resonateTime;

    public BellBlockEntity(BlockPos pos, BlockState state) {
        super(BlockEntityType.BELL, pos, state);
    }

    @Override
    public boolean onSyncedBlockEvent(int type, int data) {
        if (type == 1) {
            this.notifyMemoriesOfBell();
            this.resonateTime = 0;
            this.lastSideHit = Direction.byId(data);
            this.ringTicks = 0;
            this.ringing = true;
            return true;
        }
        return super.onSyncedBlockEvent(type, data);
    }

    private static void tick(World world, BlockPos pos, BlockState state, BellBlockEntity blockEntity, Effect bellEffect) {
        if (blockEntity.ringing) {
            ++blockEntity.ringTicks;
        }
        if (blockEntity.ringTicks >= 50) {
            blockEntity.ringing = false;
            blockEntity.ringTicks = 0;
        }
        if (blockEntity.ringTicks >= 5 && blockEntity.resonateTime == 0 && BellBlockEntity.raidersHearBell(pos, blockEntity.hearingEntities)) {
            blockEntity.resonating = true;
            world.playSound(null, pos, SoundEvents.BLOCK_BELL_RESONATE, SoundCategory.BLOCKS, 1.0f, 1.0f);
        }
        if (blockEntity.resonating) {
            if (blockEntity.resonateTime < 40) {
                ++blockEntity.resonateTime;
            } else {
                bellEffect.run(world, pos, blockEntity.hearingEntities);
                blockEntity.resonating = false;
            }
        }
    }

    public static void clientTick(World world, BlockPos pos, BlockState state, BellBlockEntity blockEntity) {
        BellBlockEntity.tick(world, pos, state, blockEntity, BellBlockEntity::applyParticlesToRaiders);
    }

    public static void serverTick(World world, BlockPos pos, BlockState state, BellBlockEntity blockEntity) {
        BellBlockEntity.tick(world, pos, state, blockEntity, BellBlockEntity::applyGlowToRaiders);
    }

    public void activate(Direction direction) {
        BlockPos lv = this.getPos();
        this.lastSideHit = direction;
        if (this.ringing) {
            this.ringTicks = 0;
        } else {
            this.ringing = true;
        }
        this.world.addSyncedBlockEvent(lv, this.getCachedState().getBlock(), 1, direction.getId());
    }

    private void notifyMemoriesOfBell() {
        BlockPos lv = this.getPos();
        if (this.world.getTime() > this.lastRingTime + 60L || this.hearingEntities == null) {
            this.lastRingTime = this.world.getTime();
            Box lv2 = new Box(lv).expand(48.0);
            this.hearingEntities = this.world.getNonSpectatingEntities(LivingEntity.class, lv2);
        }
        if (!this.world.isClient) {
            for (LivingEntity lv3 : this.hearingEntities) {
                if (!lv3.isAlive() || lv3.isRemoved() || !lv.isWithinDistance(lv3.getPos(), 32.0)) continue;
                lv3.getBrain().remember(MemoryModuleType.HEARD_BELL_TIME, this.world.getTime());
            }
        }
    }

    private static boolean raidersHearBell(BlockPos pos, List<LivingEntity> hearingEntities) {
        for (LivingEntity lv : hearingEntities) {
            if (!lv.isAlive() || lv.isRemoved() || !pos.isWithinDistance(lv.getPos(), 32.0) || !lv.getType().isIn(EntityTypeTags.RAIDERS)) continue;
            return true;
        }
        return false;
    }

    private static void applyGlowToRaiders(World world, BlockPos pos, List<LivingEntity> hearingEntities) {
        hearingEntities.stream().filter(entity -> BellBlockEntity.isRaiderEntity(pos, entity)).forEach(BellBlockEntity::applyGlowToEntity);
    }

    private static void applyParticlesToRaiders(World world, BlockPos pos, List<LivingEntity> hearingEntities) {
        MutableInt mutableInt = new MutableInt(16700985);
        int i = (int)hearingEntities.stream().filter(entity -> pos.isWithinDistance(entity.getPos(), 48.0)).count();
        hearingEntities.stream().filter(entity -> BellBlockEntity.isRaiderEntity(pos, entity)).forEach(entity -> {
            float f = 1.0f;
            double d = Math.sqrt((entity.getX() - (double)pos.getX()) * (entity.getX() - (double)pos.getX()) + (entity.getZ() - (double)pos.getZ()) * (entity.getZ() - (double)pos.getZ()));
            double e = (double)((float)pos.getX() + 0.5f) + 1.0 / d * (entity.getX() - (double)pos.getX());
            double g = (double)((float)pos.getZ() + 0.5f) + 1.0 / d * (entity.getZ() - (double)pos.getZ());
            int j = MathHelper.clamp((i - 21) / -2, 3, 15);
            for (int k = 0; k < j; ++k) {
                int l = mutableInt.addAndGet(5);
                world.addParticle(EntityEffectParticleEffect.create(ParticleTypes.ENTITY_EFFECT, l), e, (float)pos.getY() + 0.5f, g, 0.0, 0.0, 0.0);
            }
        });
    }

    private static boolean isRaiderEntity(BlockPos pos, LivingEntity entity) {
        return entity.isAlive() && !entity.isRemoved() && pos.isWithinDistance(entity.getPos(), 48.0) && entity.getType().isIn(EntityTypeTags.RAIDERS);
    }

    private static void applyGlowToEntity(LivingEntity entity) {
        entity.addStatusEffect(new StatusEffectInstance(StatusEffects.GLOWING, 60));
    }

    @FunctionalInterface
    static interface Effect {
        public void run(World var1, BlockPos var2, List<LivingEntity> var3);
    }
}

