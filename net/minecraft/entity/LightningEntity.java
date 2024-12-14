/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 * 
 * Could not load the following classes:
 *  org.jetbrains.annotations.Nullable
 */
package net.minecraft.entity;

import com.google.common.collect.Sets;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Stream;
import net.minecraft.advancement.criterion.Criteria;
import net.minecraft.block.AbstractFireBlock;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.LightningRodBlock;
import net.minecraft.block.Oxidizable;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.Difficulty;
import net.minecraft.world.GameRules;
import net.minecraft.world.World;
import net.minecraft.world.WorldEvents;
import net.minecraft.world.event.GameEvent;
import org.jetbrains.annotations.Nullable;

public class LightningEntity
extends Entity {
    private static final int field_30062 = 2;
    private static final double field_33906 = 3.0;
    private static final double field_33907 = 15.0;
    private int ambientTick;
    public long seed;
    private int remainingActions;
    private boolean cosmetic;
    @Nullable
    private ServerPlayerEntity channeler;
    private final Set<Entity> struckEntities = Sets.newHashSet();
    private int blocksSetOnFire;

    public LightningEntity(EntityType<? extends LightningEntity> arg, World arg2) {
        super(arg, arg2);
        this.ignoreCameraFrustum = true;
        this.ambientTick = 2;
        this.seed = this.random.nextLong();
        this.remainingActions = this.random.nextInt(3) + 1;
    }

    public void setCosmetic(boolean cosmetic) {
        this.cosmetic = cosmetic;
    }

    @Override
    public SoundCategory getSoundCategory() {
        return SoundCategory.WEATHER;
    }

    @Nullable
    public ServerPlayerEntity getChanneler() {
        return this.channeler;
    }

    public void setChanneler(@Nullable ServerPlayerEntity channeler) {
        this.channeler = channeler;
    }

    private void powerLightningRod() {
        BlockPos lv = this.getAffectedBlockPos();
        BlockState lv2 = this.getWorld().getBlockState(lv);
        if (lv2.isOf(Blocks.LIGHTNING_ROD)) {
            ((LightningRodBlock)lv2.getBlock()).setPowered(lv2, this.getWorld(), lv);
        }
    }

    @Override
    public void tick() {
        List<Entity> list;
        super.tick();
        if (this.ambientTick == 2) {
            if (this.getWorld().isClient()) {
                this.getWorld().playSound(this.getX(), this.getY(), this.getZ(), SoundEvents.ENTITY_LIGHTNING_BOLT_THUNDER, SoundCategory.WEATHER, 10000.0f, 0.8f + this.random.nextFloat() * 0.2f, false);
                this.getWorld().playSound(this.getX(), this.getY(), this.getZ(), SoundEvents.ENTITY_LIGHTNING_BOLT_IMPACT, SoundCategory.WEATHER, 2.0f, 0.5f + this.random.nextFloat() * 0.2f, false);
            } else {
                Difficulty lv = this.getWorld().getDifficulty();
                if (lv == Difficulty.NORMAL || lv == Difficulty.HARD) {
                    this.spawnFire(4);
                }
                this.powerLightningRod();
                LightningEntity.cleanOxidation(this.getWorld(), this.getAffectedBlockPos());
                this.emitGameEvent(GameEvent.LIGHTNING_STRIKE);
            }
        }
        --this.ambientTick;
        if (this.ambientTick < 0) {
            if (this.remainingActions == 0) {
                if (this.getWorld() instanceof ServerWorld) {
                    list = this.getWorld().getOtherEntities(this, new Box(this.getX() - 15.0, this.getY() - 15.0, this.getZ() - 15.0, this.getX() + 15.0, this.getY() + 6.0 + 15.0, this.getZ() + 15.0), arg -> arg.isAlive() && !this.struckEntities.contains(arg));
                    for (ServerPlayerEntity lv2 : ((ServerWorld)this.getWorld()).getPlayers(arg -> arg.distanceTo(this) < 256.0f)) {
                        Criteria.LIGHTNING_STRIKE.trigger(lv2, this, list);
                    }
                }
                this.discard();
            } else if (this.ambientTick < -this.random.nextInt(10)) {
                --this.remainingActions;
                this.ambientTick = 1;
                this.seed = this.random.nextLong();
                this.spawnFire(0);
            }
        }
        if (this.ambientTick >= 0) {
            if (!(this.getWorld() instanceof ServerWorld)) {
                this.getWorld().setLightningTicksLeft(2);
            } else if (!this.cosmetic) {
                list = this.getWorld().getOtherEntities(this, new Box(this.getX() - 3.0, this.getY() - 3.0, this.getZ() - 3.0, this.getX() + 3.0, this.getY() + 6.0 + 3.0, this.getZ() + 3.0), Entity::isAlive);
                for (Entity lv3 : list) {
                    lv3.onStruckByLightning((ServerWorld)this.getWorld(), this);
                }
                this.struckEntities.addAll(list);
                if (this.channeler != null) {
                    Criteria.CHANNELED_LIGHTNING.trigger(this.channeler, list);
                }
            }
        }
    }

    private BlockPos getAffectedBlockPos() {
        Vec3d lv = this.getPos();
        return BlockPos.ofFloored(lv.x, lv.y - 1.0E-6, lv.z);
    }

    private void spawnFire(int spreadAttempts) {
        if (this.cosmetic || this.getWorld().isClient || !this.getWorld().getGameRules().getBoolean(GameRules.DO_FIRE_TICK)) {
            return;
        }
        BlockPos lv = this.getBlockPos();
        BlockState lv2 = AbstractFireBlock.getState(this.getWorld(), lv);
        if (this.getWorld().getBlockState(lv).isAir() && lv2.canPlaceAt(this.getWorld(), lv)) {
            this.getWorld().setBlockState(lv, lv2);
            ++this.blocksSetOnFire;
        }
        for (int j = 0; j < spreadAttempts; ++j) {
            BlockPos lv3 = lv.add(this.random.nextInt(3) - 1, this.random.nextInt(3) - 1, this.random.nextInt(3) - 1);
            lv2 = AbstractFireBlock.getState(this.getWorld(), lv3);
            if (!this.getWorld().getBlockState(lv3).isAir() || !lv2.canPlaceAt(this.getWorld(), lv3)) continue;
            this.getWorld().setBlockState(lv3, lv2);
            ++this.blocksSetOnFire;
        }
    }

    private static void cleanOxidation(World world, BlockPos pos) {
        BlockState lv3;
        BlockPos lv2;
        BlockState lv = world.getBlockState(pos);
        if (lv.isOf(Blocks.LIGHTNING_ROD)) {
            lv2 = pos.offset(lv.get(LightningRodBlock.FACING).getOpposite());
            lv3 = world.getBlockState(lv2);
        } else {
            lv2 = pos;
            lv3 = lv;
        }
        if (!(lv3.getBlock() instanceof Oxidizable)) {
            return;
        }
        world.setBlockState(lv2, Oxidizable.getUnaffectedOxidationState(world.getBlockState(lv2)));
        BlockPos.Mutable lv4 = pos.mutableCopy();
        int i = world.random.nextInt(3) + 3;
        for (int j = 0; j < i; ++j) {
            int k = world.random.nextInt(8) + 1;
            LightningEntity.cleanOxidationAround(world, lv2, lv4, k);
        }
    }

    private static void cleanOxidationAround(World world, BlockPos pos, BlockPos.Mutable mutablePos, int count) {
        Optional<BlockPos> optional;
        mutablePos.set(pos);
        for (int j = 0; j < count && !(optional = LightningEntity.cleanOxidationAround(world, mutablePos)).isEmpty(); ++j) {
            mutablePos.set(optional.get());
        }
    }

    private static Optional<BlockPos> cleanOxidationAround(World world, BlockPos pos) {
        for (BlockPos lv : BlockPos.iterateRandomly(world.random, 10, pos, 1)) {
            BlockState lv2 = world.getBlockState(lv);
            if (!(lv2.getBlock() instanceof Oxidizable)) continue;
            Oxidizable.getDecreasedOxidationState(lv2).ifPresent(state -> world.setBlockState(lv, (BlockState)state));
            world.syncWorldEvent(WorldEvents.ELECTRICITY_SPARKS, lv, -1);
            return Optional.of(lv);
        }
        return Optional.empty();
    }

    @Override
    public boolean shouldRender(double distance) {
        double e = 64.0 * LightningEntity.getRenderDistanceMultiplier();
        return distance < e * e;
    }

    @Override
    protected void initDataTracker(DataTracker.Builder builder) {
    }

    @Override
    protected void readCustomDataFromNbt(NbtCompound nbt) {
    }

    @Override
    protected void writeCustomDataToNbt(NbtCompound nbt) {
    }

    public int getBlocksSetOnFire() {
        return this.blocksSetOnFire;
    }

    public Stream<Entity> getStruckEntities() {
        return this.struckEntities.stream().filter(Entity::isAlive);
    }
}

