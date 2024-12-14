/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 * 
 * Could not load the following classes:
 *  org.jetbrains.annotations.Nullable
 */
package net.minecraft.entity.projectile;

import com.mojang.logging.LogUtils;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import java.util.Collections;
import net.minecraft.advancement.criterion.Criteria;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityStatuses;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.ExperienceOrbEntity;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.MovementType;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.data.TrackedData;
import net.minecraft.entity.data.TrackedDataHandlerRegistry;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.ProjectileEntity;
import net.minecraft.entity.projectile.ProjectileUtil;
import net.minecraft.fluid.FluidState;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.loot.LootTable;
import net.minecraft.loot.LootTables;
import net.minecraft.loot.context.LootContextParameterSet;
import net.minecraft.loot.context.LootContextParameters;
import net.minecraft.loot.context.LootContextTypes;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.network.listener.ClientPlayPacketListener;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.s2c.play.EntitySpawnS2CPacket;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.registry.tag.FluidTags;
import net.minecraft.registry.tag.ItemTags;
import net.minecraft.server.network.EntityTrackerEntry;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundEvents;
import net.minecraft.stat.Stats;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;

public class FishingBobberEntity
extends ProjectileEntity {
    private static final Logger LOGGER = LogUtils.getLogger();
    private final Random velocityRandom = Random.create();
    private boolean caughtFish;
    private int outOfOpenWaterTicks;
    private static final int field_30665 = 10;
    private static final TrackedData<Integer> HOOK_ENTITY_ID = DataTracker.registerData(FishingBobberEntity.class, TrackedDataHandlerRegistry.INTEGER);
    private static final TrackedData<Boolean> CAUGHT_FISH = DataTracker.registerData(FishingBobberEntity.class, TrackedDataHandlerRegistry.BOOLEAN);
    private int removalTimer;
    private int hookCountdown;
    private int waitCountdown;
    private int fishTravelCountdown;
    private float fishAngle;
    private boolean inOpenWater = true;
    @Nullable
    private Entity hookedEntity;
    private State state = State.FLYING;
    private final int luckBonus;
    private final int waitTimeReductionTicks;

    private FishingBobberEntity(EntityType<? extends FishingBobberEntity> type, World world, int luckBonus, int waitTimeReductionTicks) {
        super((EntityType<? extends ProjectileEntity>)type, world);
        this.ignoreCameraFrustum = true;
        this.luckBonus = Math.max(0, luckBonus);
        this.waitTimeReductionTicks = Math.max(0, waitTimeReductionTicks);
    }

    public FishingBobberEntity(EntityType<? extends FishingBobberEntity> arg, World arg2) {
        this(arg, arg2, 0, 0);
    }

    public FishingBobberEntity(PlayerEntity thrower, World world, int luckBonus, int waitTimeReductionTicks) {
        this(EntityType.FISHING_BOBBER, world, luckBonus, waitTimeReductionTicks);
        this.setOwner(thrower);
        float f = thrower.getPitch();
        float g = thrower.getYaw();
        float h = MathHelper.cos(-g * ((float)Math.PI / 180) - (float)Math.PI);
        float k = MathHelper.sin(-g * ((float)Math.PI / 180) - (float)Math.PI);
        float l = -MathHelper.cos(-f * ((float)Math.PI / 180));
        float m = MathHelper.sin(-f * ((float)Math.PI / 180));
        double d = thrower.getX() - (double)k * 0.3;
        double e = thrower.getEyeY();
        double n = thrower.getZ() - (double)h * 0.3;
        this.refreshPositionAndAngles(d, e, n, g, f);
        Vec3d lv = new Vec3d(-k, MathHelper.clamp(-(m / l), -5.0f, 5.0f), -h);
        double o = lv.length();
        lv = lv.multiply(0.6 / o + this.random.nextTriangular(0.5, 0.0103365), 0.6 / o + this.random.nextTriangular(0.5, 0.0103365), 0.6 / o + this.random.nextTriangular(0.5, 0.0103365));
        this.setVelocity(lv);
        this.setYaw((float)(MathHelper.atan2(lv.x, lv.z) * 57.2957763671875));
        this.setPitch((float)(MathHelper.atan2(lv.y, lv.horizontalLength()) * 57.2957763671875));
        this.prevYaw = this.getYaw();
        this.prevPitch = this.getPitch();
    }

    @Override
    protected void initDataTracker(DataTracker.Builder builder) {
        builder.add(HOOK_ENTITY_ID, 0);
        builder.add(CAUGHT_FISH, false);
    }

    @Override
    public void onTrackedDataSet(TrackedData<?> data) {
        if (HOOK_ENTITY_ID.equals(data)) {
            int i = this.getDataTracker().get(HOOK_ENTITY_ID);
            Entity entity = this.hookedEntity = i > 0 ? this.getWorld().getEntityById(i - 1) : null;
        }
        if (CAUGHT_FISH.equals(data)) {
            this.caughtFish = this.getDataTracker().get(CAUGHT_FISH);
            if (this.caughtFish) {
                this.setVelocity(this.getVelocity().x, -0.4f * MathHelper.nextFloat(this.velocityRandom, 0.6f, 1.0f), this.getVelocity().z);
            }
        }
        super.onTrackedDataSet(data);
    }

    @Override
    public boolean shouldRender(double distance) {
        double e = 64.0;
        return distance < 4096.0;
    }

    @Override
    public void updateTrackedPositionAndAngles(double x, double y, double z, float yaw, float pitch, int interpolationSteps) {
    }

    @Override
    public void tick() {
        boolean bl;
        this.velocityRandom.setSeed(this.getUuid().getLeastSignificantBits() ^ this.getWorld().getTime());
        super.tick();
        PlayerEntity lv = this.getPlayerOwner();
        if (lv == null) {
            this.discard();
            return;
        }
        if (!this.getWorld().isClient && this.removeIfInvalid(lv)) {
            return;
        }
        if (this.isOnGround()) {
            ++this.removalTimer;
            if (this.removalTimer >= 1200) {
                this.discard();
                return;
            }
        } else {
            this.removalTimer = 0;
        }
        float f = 0.0f;
        BlockPos lv2 = this.getBlockPos();
        FluidState lv3 = this.getWorld().getFluidState(lv2);
        if (lv3.isIn(FluidTags.WATER)) {
            f = lv3.getHeight(this.getWorld(), lv2);
        }
        boolean bl2 = bl = f > 0.0f;
        if (this.state == State.FLYING) {
            if (this.hookedEntity != null) {
                this.setVelocity(Vec3d.ZERO);
                this.state = State.HOOKED_IN_ENTITY;
                return;
            }
            if (bl) {
                this.setVelocity(this.getVelocity().multiply(0.3, 0.2, 0.3));
                this.state = State.BOBBING;
                return;
            }
            this.checkForCollision();
        } else {
            if (this.state == State.HOOKED_IN_ENTITY) {
                if (this.hookedEntity != null) {
                    if (this.hookedEntity.isRemoved() || this.hookedEntity.getWorld().getRegistryKey() != this.getWorld().getRegistryKey()) {
                        this.updateHookedEntityId(null);
                        this.state = State.FLYING;
                    } else {
                        this.setPosition(this.hookedEntity.getX(), this.hookedEntity.getBodyY(0.8), this.hookedEntity.getZ());
                    }
                }
                return;
            }
            if (this.state == State.BOBBING) {
                Vec3d lv4 = this.getVelocity();
                double d = this.getY() + lv4.y - (double)lv2.getY() - (double)f;
                if (Math.abs(d) < 0.01) {
                    d += Math.signum(d) * 0.1;
                }
                this.setVelocity(lv4.x * 0.9, lv4.y - d * (double)this.random.nextFloat() * 0.2, lv4.z * 0.9);
                this.inOpenWater = this.hookCountdown > 0 || this.fishTravelCountdown > 0 ? this.inOpenWater && this.outOfOpenWaterTicks < 10 && this.isOpenOrWaterAround(lv2) : true;
                if (bl) {
                    this.outOfOpenWaterTicks = Math.max(0, this.outOfOpenWaterTicks - 1);
                    if (this.caughtFish) {
                        this.setVelocity(this.getVelocity().add(0.0, -0.1 * (double)this.velocityRandom.nextFloat() * (double)this.velocityRandom.nextFloat(), 0.0));
                    }
                    if (!this.getWorld().isClient) {
                        this.tickFishingLogic(lv2);
                    }
                } else {
                    this.outOfOpenWaterTicks = Math.min(10, this.outOfOpenWaterTicks + 1);
                }
            }
        }
        if (!lv3.isIn(FluidTags.WATER)) {
            this.setVelocity(this.getVelocity().add(0.0, -0.03, 0.0));
        }
        this.move(MovementType.SELF, this.getVelocity());
        this.updateRotation();
        if (this.state == State.FLYING && (this.isOnGround() || this.horizontalCollision)) {
            this.setVelocity(Vec3d.ZERO);
        }
        double e = 0.92;
        this.setVelocity(this.getVelocity().multiply(0.92));
        this.refreshPosition();
    }

    private boolean removeIfInvalid(PlayerEntity player) {
        ItemStack lv = player.getMainHandStack();
        ItemStack lv2 = player.getOffHandStack();
        boolean bl = lv.isOf(Items.FISHING_ROD);
        boolean bl2 = lv2.isOf(Items.FISHING_ROD);
        if (player.isRemoved() || !player.isAlive() || !bl && !bl2 || this.squaredDistanceTo(player) > 1024.0) {
            this.discard();
            return true;
        }
        return false;
    }

    private void checkForCollision() {
        HitResult lv = ProjectileUtil.getCollision(this, this::canHit);
        this.hitOrDeflect(lv);
    }

    @Override
    protected boolean canHit(Entity entity) {
        return super.canHit(entity) || entity.isAlive() && entity instanceof ItemEntity;
    }

    @Override
    protected void onEntityHit(EntityHitResult entityHitResult) {
        super.onEntityHit(entityHitResult);
        if (!this.getWorld().isClient) {
            this.updateHookedEntityId(entityHitResult.getEntity());
        }
    }

    @Override
    protected void onBlockHit(BlockHitResult blockHitResult) {
        super.onBlockHit(blockHitResult);
        this.setVelocity(this.getVelocity().normalize().multiply(blockHitResult.squaredDistanceTo(this)));
    }

    private void updateHookedEntityId(@Nullable Entity entity) {
        this.hookedEntity = entity;
        this.getDataTracker().set(HOOK_ENTITY_ID, entity == null ? 0 : entity.getId() + 1);
    }

    private void tickFishingLogic(BlockPos pos) {
        ServerWorld lv = (ServerWorld)this.getWorld();
        int i = 1;
        BlockPos lv2 = pos.up();
        if (this.random.nextFloat() < 0.25f && this.getWorld().hasRain(lv2)) {
            ++i;
        }
        if (this.random.nextFloat() < 0.5f && !this.getWorld().isSkyVisible(lv2)) {
            --i;
        }
        if (this.hookCountdown > 0) {
            --this.hookCountdown;
            if (this.hookCountdown <= 0) {
                this.waitCountdown = 0;
                this.fishTravelCountdown = 0;
                this.getDataTracker().set(CAUGHT_FISH, false);
            }
        } else if (this.fishTravelCountdown > 0) {
            this.fishTravelCountdown -= i;
            if (this.fishTravelCountdown > 0) {
                double j;
                double e;
                this.fishAngle += (float)this.random.nextTriangular(0.0, 9.188);
                float f = this.fishAngle * ((float)Math.PI / 180);
                float g = MathHelper.sin(f);
                float h = MathHelper.cos(f);
                double d = this.getX() + (double)(g * (float)this.fishTravelCountdown * 0.1f);
                BlockState lv3 = lv.getBlockState(BlockPos.ofFloored(d, (e = (double)((float)MathHelper.floor(this.getY()) + 1.0f)) - 1.0, j = this.getZ() + (double)(h * (float)this.fishTravelCountdown * 0.1f)));
                if (lv3.isOf(Blocks.WATER)) {
                    if (this.random.nextFloat() < 0.15f) {
                        lv.spawnParticles(ParticleTypes.BUBBLE, d, e - (double)0.1f, j, 1, g, 0.1, h, 0.0);
                    }
                    float k = g * 0.04f;
                    float l = h * 0.04f;
                    lv.spawnParticles(ParticleTypes.FISHING, d, e, j, 0, l, 0.01, -k, 1.0);
                    lv.spawnParticles(ParticleTypes.FISHING, d, e, j, 0, -l, 0.01, k, 1.0);
                }
            } else {
                this.playSound(SoundEvents.ENTITY_FISHING_BOBBER_SPLASH, 0.25f, 1.0f + (this.random.nextFloat() - this.random.nextFloat()) * 0.4f);
                double m = this.getY() + 0.5;
                lv.spawnParticles(ParticleTypes.BUBBLE, this.getX(), m, this.getZ(), (int)(1.0f + this.getWidth() * 20.0f), this.getWidth(), 0.0, this.getWidth(), 0.2f);
                lv.spawnParticles(ParticleTypes.FISHING, this.getX(), m, this.getZ(), (int)(1.0f + this.getWidth() * 20.0f), this.getWidth(), 0.0, this.getWidth(), 0.2f);
                this.hookCountdown = MathHelper.nextInt(this.random, 20, 40);
                this.getDataTracker().set(CAUGHT_FISH, true);
            }
        } else if (this.waitCountdown > 0) {
            this.waitCountdown -= i;
            float f = 0.15f;
            if (this.waitCountdown < 20) {
                f += (float)(20 - this.waitCountdown) * 0.05f;
            } else if (this.waitCountdown < 40) {
                f += (float)(40 - this.waitCountdown) * 0.02f;
            } else if (this.waitCountdown < 60) {
                f += (float)(60 - this.waitCountdown) * 0.01f;
            }
            if (this.random.nextFloat() < f) {
                double j;
                double e;
                float g = MathHelper.nextFloat(this.random, 0.0f, 360.0f) * ((float)Math.PI / 180);
                float h = MathHelper.nextFloat(this.random, 25.0f, 60.0f);
                double d = this.getX() + (double)(MathHelper.sin(g) * h) * 0.1;
                BlockState lv3 = lv.getBlockState(BlockPos.ofFloored(d, (e = (double)((float)MathHelper.floor(this.getY()) + 1.0f)) - 1.0, j = this.getZ() + (double)(MathHelper.cos(g) * h) * 0.1));
                if (lv3.isOf(Blocks.WATER)) {
                    lv.spawnParticles(ParticleTypes.SPLASH, d, e, j, 2 + this.random.nextInt(2), 0.1f, 0.0, 0.1f, 0.0);
                }
            }
            if (this.waitCountdown <= 0) {
                this.fishAngle = MathHelper.nextFloat(this.random, 0.0f, 360.0f);
                this.fishTravelCountdown = MathHelper.nextInt(this.random, 20, 80);
            }
        } else {
            this.waitCountdown = MathHelper.nextInt(this.random, 100, 600);
            this.waitCountdown -= this.waitTimeReductionTicks;
        }
    }

    private boolean isOpenOrWaterAround(BlockPos pos) {
        PositionType lv = PositionType.INVALID;
        for (int i = -1; i <= 2; ++i) {
            PositionType lv2 = this.getPositionType(pos.add(-2, i, -2), pos.add(2, i, 2));
            switch (lv2.ordinal()) {
                case 2: {
                    return false;
                }
                case 0: {
                    if (lv != PositionType.INVALID) break;
                    return false;
                }
                case 1: {
                    if (lv != PositionType.ABOVE_WATER) break;
                    return false;
                }
            }
            lv = lv2;
        }
        return true;
    }

    private PositionType getPositionType(BlockPos start, BlockPos end) {
        return BlockPos.stream(start, end).map(this::getPositionType).reduce((arg, arg2) -> arg == arg2 ? arg : PositionType.INVALID).orElse(PositionType.INVALID);
    }

    private PositionType getPositionType(BlockPos pos) {
        BlockState lv = this.getWorld().getBlockState(pos);
        if (lv.isAir() || lv.isOf(Blocks.LILY_PAD)) {
            return PositionType.ABOVE_WATER;
        }
        FluidState lv2 = lv.getFluidState();
        if (lv2.isIn(FluidTags.WATER) && lv2.isStill() && lv.getCollisionShape(this.getWorld(), pos).isEmpty()) {
            return PositionType.INSIDE_WATER;
        }
        return PositionType.INVALID;
    }

    public boolean isInOpenWater() {
        return this.inOpenWater;
    }

    @Override
    public void writeCustomDataToNbt(NbtCompound nbt) {
    }

    @Override
    public void readCustomDataFromNbt(NbtCompound nbt) {
    }

    public int use(ItemStack usedItem) {
        PlayerEntity lv = this.getPlayerOwner();
        if (this.getWorld().isClient || lv == null || this.removeIfInvalid(lv)) {
            return 0;
        }
        int i = 0;
        if (this.hookedEntity != null) {
            this.pullHookedEntity(this.hookedEntity);
            Criteria.FISHING_ROD_HOOKED.trigger((ServerPlayerEntity)lv, usedItem, this, Collections.emptyList());
            this.getWorld().sendEntityStatus(this, EntityStatuses.PULL_HOOKED_ENTITY);
            i = this.hookedEntity instanceof ItemEntity ? 3 : 5;
        } else if (this.hookCountdown > 0) {
            LootContextParameterSet lv2 = new LootContextParameterSet.Builder((ServerWorld)this.getWorld()).add(LootContextParameters.ORIGIN, this.getPos()).add(LootContextParameters.TOOL, usedItem).add(LootContextParameters.THIS_ENTITY, this).luck((float)this.luckBonus + lv.getLuck()).build(LootContextTypes.FISHING);
            LootTable lv3 = this.getWorld().getServer().getReloadableRegistries().getLootTable(LootTables.FISHING_GAMEPLAY);
            ObjectArrayList<ItemStack> list = lv3.generateLoot(lv2);
            Criteria.FISHING_ROD_HOOKED.trigger((ServerPlayerEntity)lv, usedItem, this, list);
            for (ItemStack lv4 : list) {
                ItemEntity lv5 = new ItemEntity(this.getWorld(), this.getX(), this.getY(), this.getZ(), lv4);
                double d = lv.getX() - this.getX();
                double e = lv.getY() - this.getY();
                double f = lv.getZ() - this.getZ();
                double g = 0.1;
                lv5.setVelocity(d * 0.1, e * 0.1 + Math.sqrt(Math.sqrt(d * d + e * e + f * f)) * 0.08, f * 0.1);
                this.getWorld().spawnEntity(lv5);
                lv.getWorld().spawnEntity(new ExperienceOrbEntity(lv.getWorld(), lv.getX(), lv.getY() + 0.5, lv.getZ() + 0.5, this.random.nextInt(6) + 1));
                if (!lv4.isIn(ItemTags.FISHES)) continue;
                lv.increaseStat(Stats.FISH_CAUGHT, 1);
            }
            i = 1;
        }
        if (this.isOnGround()) {
            i = 2;
        }
        this.discard();
        return i;
    }

    @Override
    public void handleStatus(byte status) {
        if (status == EntityStatuses.PULL_HOOKED_ENTITY && this.getWorld().isClient && this.hookedEntity instanceof PlayerEntity && ((PlayerEntity)this.hookedEntity).isMainPlayer()) {
            this.pullHookedEntity(this.hookedEntity);
        }
        super.handleStatus(status);
    }

    protected void pullHookedEntity(Entity entity) {
        Entity lv = this.getOwner();
        if (lv == null) {
            return;
        }
        Vec3d lv2 = new Vec3d(lv.getX() - this.getX(), lv.getY() - this.getY(), lv.getZ() - this.getZ()).multiply(0.1);
        entity.setVelocity(entity.getVelocity().add(lv2));
    }

    @Override
    protected Entity.MoveEffect getMoveEffect() {
        return Entity.MoveEffect.NONE;
    }

    @Override
    public void remove(Entity.RemovalReason reason) {
        this.setPlayerFishHook(null);
        super.remove(reason);
    }

    @Override
    public void onRemoved() {
        this.setPlayerFishHook(null);
    }

    @Override
    public void setOwner(@Nullable Entity entity) {
        super.setOwner(entity);
        this.setPlayerFishHook(this);
    }

    private void setPlayerFishHook(@Nullable FishingBobberEntity fishingBobber) {
        PlayerEntity lv = this.getPlayerOwner();
        if (lv != null) {
            lv.fishHook = fishingBobber;
        }
    }

    @Nullable
    public PlayerEntity getPlayerOwner() {
        Entity lv = this.getOwner();
        return lv instanceof PlayerEntity ? (PlayerEntity)lv : null;
    }

    @Nullable
    public Entity getHookedEntity() {
        return this.hookedEntity;
    }

    @Override
    public boolean canUsePortals(boolean allowVehicles) {
        return false;
    }

    @Override
    public Packet<ClientPlayPacketListener> createSpawnPacket(EntityTrackerEntry entityTrackerEntry) {
        Entity lv = this.getOwner();
        return new EntitySpawnS2CPacket((Entity)this, entityTrackerEntry, lv == null ? this.getId() : lv.getId());
    }

    @Override
    public void onSpawnPacket(EntitySpawnS2CPacket packet) {
        super.onSpawnPacket(packet);
        if (this.getPlayerOwner() == null) {
            int i = packet.getEntityData();
            LOGGER.error("Failed to recreate fishing hook on client. {} (id: {}) is not a valid owner.", (Object)this.getWorld().getEntityById(i), (Object)i);
            this.kill();
        }
    }

    static enum State {
        FLYING,
        HOOKED_IN_ENTITY,
        BOBBING;

    }

    static enum PositionType {
        ABOVE_WATER,
        INSIDE_WATER,
        INVALID;

    }
}

