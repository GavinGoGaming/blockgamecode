/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 * 
 * Could not load the following classes:
 *  org.jetbrains.annotations.Nullable
 */
package net.minecraft.entity.mob;

import java.util.EnumSet;
import java.util.List;
import java.util.Optional;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityData;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.MovementType;
import net.minecraft.entity.SpawnReason;
import net.minecraft.entity.VariantHolder;
import net.minecraft.entity.ai.control.BodyControl;
import net.minecraft.entity.ai.control.LookControl;
import net.minecraft.entity.ai.goal.ActiveTargetGoal;
import net.minecraft.entity.ai.goal.Goal;
import net.minecraft.entity.ai.goal.LookAroundGoal;
import net.minecraft.entity.ai.goal.LookAtEntityGoal;
import net.minecraft.entity.ai.goal.RevengeGoal;
import net.minecraft.entity.attribute.DefaultAttributeContainer;
import net.minecraft.entity.attribute.EntityAttributeModifier;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.data.TrackedData;
import net.minecraft.entity.data.TrackedDataHandlerRegistry;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.mob.Monster;
import net.minecraft.entity.passive.GolemEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.PersistentProjectileEntity;
import net.minecraft.entity.projectile.ShulkerBulletEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.network.packet.s2c.play.EntitySpawnS2CPacket;
import net.minecraft.predicate.entity.EntityPredicates;
import net.minecraft.registry.tag.DamageTypeTags;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvent;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.DyeColor;
import net.minecraft.util.Identifier;
import net.minecraft.util.Util;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.Vec3i;
import net.minecraft.world.Difficulty;
import net.minecraft.world.LocalDifficulty;
import net.minecraft.world.ServerWorldAccess;
import net.minecraft.world.World;
import net.minecraft.world.event.GameEvent;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector3f;

public class ShulkerEntity
extends GolemEntity
implements VariantHolder<Optional<DyeColor>>,
Monster {
    private static final Identifier COVERED_ARMOR_MODIFIER_ID = Identifier.ofVanilla("covered");
    private static final EntityAttributeModifier COVERED_ARMOR_BONUS = new EntityAttributeModifier(COVERED_ARMOR_MODIFIER_ID, 20.0, EntityAttributeModifier.Operation.ADD_VALUE);
    protected static final TrackedData<Direction> ATTACHED_FACE = DataTracker.registerData(ShulkerEntity.class, TrackedDataHandlerRegistry.FACING);
    protected static final TrackedData<Byte> PEEK_AMOUNT = DataTracker.registerData(ShulkerEntity.class, TrackedDataHandlerRegistry.BYTE);
    protected static final TrackedData<Byte> COLOR = DataTracker.registerData(ShulkerEntity.class, TrackedDataHandlerRegistry.BYTE);
    private static final int field_30487 = 6;
    private static final byte field_30488 = 16;
    private static final byte field_30489 = 16;
    private static final int field_30490 = 8;
    private static final int field_30491 = 8;
    private static final int field_30492 = 5;
    private static final float field_30493 = 0.05f;
    static final Vector3f SOUTH_VECTOR = Util.make(() -> {
        Vec3i lv = Direction.SOUTH.getVector();
        return new Vector3f(lv.getX(), lv.getY(), lv.getZ());
    });
    private static final float field_48343 = 3.0f;
    private float prevOpenProgress;
    private float openProgress;
    @Nullable
    private BlockPos prevAttachedBlock;
    private int teleportLerpTimer;
    private static final float field_30494 = 1.0f;

    public ShulkerEntity(EntityType<? extends ShulkerEntity> arg, World arg2) {
        super((EntityType<? extends GolemEntity>)arg, arg2);
        this.experiencePoints = 5;
        this.lookControl = new ShulkerLookControl(this);
    }

    @Override
    protected void initGoals() {
        this.goalSelector.add(1, new LookAtEntityGoal(this, PlayerEntity.class, 8.0f, 0.02f, true));
        this.goalSelector.add(4, new ShootBulletGoal());
        this.goalSelector.add(7, new PeekGoal());
        this.goalSelector.add(8, new LookAroundGoal(this));
        this.targetSelector.add(1, new RevengeGoal(this, this.getClass()).setGroupRevenge(new Class[0]));
        this.targetSelector.add(2, new TargetPlayerGoal(this));
        this.targetSelector.add(3, new TargetOtherTeamGoal(this));
    }

    @Override
    protected Entity.MoveEffect getMoveEffect() {
        return Entity.MoveEffect.NONE;
    }

    @Override
    public SoundCategory getSoundCategory() {
        return SoundCategory.HOSTILE;
    }

    @Override
    protected SoundEvent getAmbientSound() {
        return SoundEvents.ENTITY_SHULKER_AMBIENT;
    }

    @Override
    public void playAmbientSound() {
        if (!this.isClosed()) {
            super.playAmbientSound();
        }
    }

    @Override
    protected SoundEvent getDeathSound() {
        return SoundEvents.ENTITY_SHULKER_DEATH;
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource source) {
        if (this.isClosed()) {
            return SoundEvents.ENTITY_SHULKER_HURT_CLOSED;
        }
        return SoundEvents.ENTITY_SHULKER_HURT;
    }

    @Override
    protected void initDataTracker(DataTracker.Builder builder) {
        super.initDataTracker(builder);
        builder.add(ATTACHED_FACE, Direction.DOWN);
        builder.add(PEEK_AMOUNT, (byte)0);
        builder.add(COLOR, (byte)16);
    }

    public static DefaultAttributeContainer.Builder createShulkerAttributes() {
        return MobEntity.createMobAttributes().add(EntityAttributes.GENERIC_MAX_HEALTH, 30.0);
    }

    @Override
    protected BodyControl createBodyControl() {
        return new ShulkerBodyControl(this);
    }

    @Override
    public void readCustomDataFromNbt(NbtCompound nbt) {
        super.readCustomDataFromNbt(nbt);
        this.setAttachedFace(Direction.byId(nbt.getByte("AttachFace")));
        this.dataTracker.set(PEEK_AMOUNT, nbt.getByte("Peek"));
        if (nbt.contains("Color", NbtElement.NUMBER_TYPE)) {
            this.dataTracker.set(COLOR, nbt.getByte("Color"));
        }
    }

    @Override
    public void writeCustomDataToNbt(NbtCompound nbt) {
        super.writeCustomDataToNbt(nbt);
        nbt.putByte("AttachFace", (byte)this.getAttachedFace().getId());
        nbt.putByte("Peek", this.dataTracker.get(PEEK_AMOUNT));
        nbt.putByte("Color", this.dataTracker.get(COLOR));
    }

    @Override
    public void tick() {
        super.tick();
        if (!(this.getWorld().isClient || this.hasVehicle() || this.canStay(this.getBlockPos(), this.getAttachedFace()))) {
            this.tryAttachOrTeleport();
        }
        if (this.tickOpenProgress()) {
            this.moveEntities();
        }
        if (this.getWorld().isClient) {
            if (this.teleportLerpTimer > 0) {
                --this.teleportLerpTimer;
            } else {
                this.prevAttachedBlock = null;
            }
        }
    }

    private void tryAttachOrTeleport() {
        Direction lv = this.findAttachSide(this.getBlockPos());
        if (lv != null) {
            this.setAttachedFace(lv);
        } else {
            this.tryTeleport();
        }
    }

    @Override
    protected Box calculateBoundingBox() {
        float f = ShulkerEntity.getExtraLength(this.openProgress);
        Direction lv = this.getAttachedFace().getOpposite();
        float g = this.getWidth() / 2.0f;
        return ShulkerEntity.calculateBoundingBox(this.getScale(), lv, f).offset(this.getX() - (double)g, this.getY(), this.getZ() - (double)g);
    }

    private static float getExtraLength(float openProgress) {
        return 0.5f - MathHelper.sin((0.5f + openProgress) * (float)Math.PI) * 0.5f;
    }

    private boolean tickOpenProgress() {
        this.prevOpenProgress = this.openProgress;
        float f = (float)this.getPeekAmount() * 0.01f;
        if (this.openProgress == f) {
            return false;
        }
        this.openProgress = this.openProgress > f ? MathHelper.clamp(this.openProgress - 0.05f, f, 1.0f) : MathHelper.clamp(this.openProgress + 0.05f, 0.0f, f);
        return true;
    }

    private void moveEntities() {
        this.refreshPosition();
        float f = ShulkerEntity.getExtraLength(this.openProgress);
        float g = ShulkerEntity.getExtraLength(this.prevOpenProgress);
        Direction lv = this.getAttachedFace().getOpposite();
        float h = (f - g) * this.getScale();
        if (h <= 0.0f) {
            return;
        }
        List<Entity> list = this.getWorld().getOtherEntities(this, ShulkerEntity.calculateBoundingBox(this.getScale(), lv, g, f).offset(this.getX() - 0.5, this.getY(), this.getZ() - 0.5), EntityPredicates.EXCEPT_SPECTATOR.and(arg -> !arg.isConnectedThroughVehicle(this)));
        for (Entity lv2 : list) {
            if (lv2 instanceof ShulkerEntity || lv2.noClip) continue;
            lv2.move(MovementType.SHULKER, new Vec3d(h * (float)lv.getOffsetX(), h * (float)lv.getOffsetY(), h * (float)lv.getOffsetZ()));
        }
    }

    public static Box calculateBoundingBox(float scale, Direction facing, float extraLength) {
        return ShulkerEntity.calculateBoundingBox(scale, facing, -1.0f, extraLength);
    }

    public static Box calculateBoundingBox(float scale, Direction facing, float prevExtraLength, float extraLength) {
        Box lv = new Box(0.0, 0.0, 0.0, scale, scale, scale);
        double d = Math.max(prevExtraLength, extraLength);
        double e = Math.min(prevExtraLength, extraLength);
        return lv.stretch((double)facing.getOffsetX() * d * (double)scale, (double)facing.getOffsetY() * d * (double)scale, (double)facing.getOffsetZ() * d * (double)scale).shrink((double)(-facing.getOffsetX()) * (1.0 + e) * (double)scale, (double)(-facing.getOffsetY()) * (1.0 + e) * (double)scale, (double)(-facing.getOffsetZ()) * (1.0 + e) * (double)scale);
    }

    @Override
    public boolean startRiding(Entity entity, boolean force) {
        if (this.getWorld().isClient()) {
            this.prevAttachedBlock = null;
            this.teleportLerpTimer = 0;
        }
        this.setAttachedFace(Direction.DOWN);
        return super.startRiding(entity, force);
    }

    @Override
    public void stopRiding() {
        super.stopRiding();
        if (this.getWorld().isClient) {
            this.prevAttachedBlock = this.getBlockPos();
        }
        this.prevBodyYaw = 0.0f;
        this.bodyYaw = 0.0f;
    }

    @Override
    @Nullable
    public EntityData initialize(ServerWorldAccess world, LocalDifficulty difficulty, SpawnReason spawnReason, @Nullable EntityData entityData) {
        this.setYaw(0.0f);
        this.headYaw = this.getYaw();
        this.resetPosition();
        return super.initialize(world, difficulty, spawnReason, entityData);
    }

    @Override
    public void move(MovementType movementType, Vec3d movement) {
        if (movementType == MovementType.SHULKER_BOX) {
            this.tryTeleport();
        } else {
            super.move(movementType, movement);
        }
    }

    @Override
    public Vec3d getVelocity() {
        return Vec3d.ZERO;
    }

    @Override
    public void setVelocity(Vec3d velocity) {
    }

    @Override
    public void setPosition(double x, double y, double z) {
        BlockPos lv = this.getBlockPos();
        if (this.hasVehicle()) {
            super.setPosition(x, y, z);
        } else {
            super.setPosition((double)MathHelper.floor(x) + 0.5, MathHelper.floor(y + 0.5), (double)MathHelper.floor(z) + 0.5);
        }
        if (this.age == 0) {
            return;
        }
        BlockPos lv2 = this.getBlockPos();
        if (!lv2.equals(lv)) {
            this.dataTracker.set(PEEK_AMOUNT, (byte)0);
            this.velocityDirty = true;
            if (this.getWorld().isClient && !this.hasVehicle() && !lv2.equals(this.prevAttachedBlock)) {
                this.prevAttachedBlock = lv;
                this.teleportLerpTimer = 6;
                this.lastRenderX = this.getX();
                this.lastRenderY = this.getY();
                this.lastRenderZ = this.getZ();
            }
        }
    }

    @Nullable
    protected Direction findAttachSide(BlockPos pos) {
        for (Direction lv : Direction.values()) {
            if (!this.canStay(pos, lv)) continue;
            return lv;
        }
        return null;
    }

    boolean canStay(BlockPos pos, Direction direction) {
        if (this.isInvalidPosition(pos)) {
            return false;
        }
        Direction lv = direction.getOpposite();
        if (!this.getWorld().isDirectionSolid(pos.offset(direction), this, lv)) {
            return false;
        }
        Box lv2 = ShulkerEntity.calculateBoundingBox(this.getScale(), lv, 1.0f).offset(pos).contract(1.0E-6);
        return this.getWorld().isSpaceEmpty(this, lv2);
    }

    private boolean isInvalidPosition(BlockPos pos) {
        BlockState lv = this.getWorld().getBlockState(pos);
        if (lv.isAir()) {
            return false;
        }
        boolean bl = lv.isOf(Blocks.MOVING_PISTON) && pos.equals(this.getBlockPos());
        return !bl;
    }

    protected boolean tryTeleport() {
        if (this.isAiDisabled() || !this.isAlive()) {
            return false;
        }
        BlockPos lv = this.getBlockPos();
        for (int i = 0; i < 5; ++i) {
            Direction lv3;
            BlockPos lv2 = lv.add(MathHelper.nextBetween(this.random, -8, 8), MathHelper.nextBetween(this.random, -8, 8), MathHelper.nextBetween(this.random, -8, 8));
            if (lv2.getY() <= this.getWorld().getBottomY() || !this.getWorld().isAir(lv2) || !this.getWorld().getWorldBorder().contains(lv2) || !this.getWorld().isSpaceEmpty(this, new Box(lv2).contract(1.0E-6)) || (lv3 = this.findAttachSide(lv2)) == null) continue;
            this.detach();
            this.setAttachedFace(lv3);
            this.playSound(SoundEvents.ENTITY_SHULKER_TELEPORT, 1.0f, 1.0f);
            this.setPosition((double)lv2.getX() + 0.5, lv2.getY(), (double)lv2.getZ() + 0.5);
            this.getWorld().emitGameEvent(GameEvent.TELEPORT, lv, GameEvent.Emitter.of(this));
            this.dataTracker.set(PEEK_AMOUNT, (byte)0);
            this.setTarget(null);
            return true;
        }
        return false;
    }

    @Override
    public void updateTrackedPositionAndAngles(double x, double y, double z, float yaw, float pitch, int interpolationSteps) {
        this.bodyTrackingIncrements = 0;
        this.setPosition(x, y, z);
        this.setRotation(yaw, pitch);
    }

    @Override
    public boolean damage(DamageSource source, float amount) {
        Entity lv;
        if (this.isClosed() && (lv = source.getSource()) instanceof PersistentProjectileEntity) {
            return false;
        }
        if (super.damage(source, amount)) {
            if ((double)this.getHealth() < (double)this.getMaxHealth() * 0.5 && this.random.nextInt(4) == 0) {
                this.tryTeleport();
            } else if (source.isIn(DamageTypeTags.IS_PROJECTILE) && (lv = source.getSource()) != null && lv.getType() == EntityType.SHULKER_BULLET) {
                this.spawnNewShulker();
            }
            return true;
        }
        return false;
    }

    private boolean isClosed() {
        return this.getPeekAmount() == 0;
    }

    private void spawnNewShulker() {
        Vec3d lv = this.getPos();
        Box lv2 = this.getBoundingBox();
        if (this.isClosed() || !this.tryTeleport()) {
            return;
        }
        int i = this.getWorld().getEntitiesByType(EntityType.SHULKER, lv2.expand(8.0), Entity::isAlive).size();
        float f = (float)(i - 1) / 5.0f;
        if (this.getWorld().random.nextFloat() < f) {
            return;
        }
        ShulkerEntity lv3 = EntityType.SHULKER.create(this.getWorld());
        if (lv3 != null) {
            lv3.setVariant((Optional<DyeColor>)this.getVariant());
            lv3.refreshPositionAfterTeleport(lv);
            this.getWorld().spawnEntity(lv3);
        }
    }

    @Override
    public boolean isCollidable() {
        return this.isAlive();
    }

    public Direction getAttachedFace() {
        return this.dataTracker.get(ATTACHED_FACE);
    }

    private void setAttachedFace(Direction face) {
        this.dataTracker.set(ATTACHED_FACE, face);
    }

    @Override
    public void onTrackedDataSet(TrackedData<?> data) {
        if (ATTACHED_FACE.equals(data)) {
            this.setBoundingBox(this.calculateBoundingBox());
        }
        super.onTrackedDataSet(data);
    }

    private int getPeekAmount() {
        return this.dataTracker.get(PEEK_AMOUNT).byteValue();
    }

    void setPeekAmount(int peekAmount) {
        if (!this.getWorld().isClient) {
            this.getAttributeInstance(EntityAttributes.GENERIC_ARMOR).removeModifier(COVERED_ARMOR_MODIFIER_ID);
            if (peekAmount == 0) {
                this.getAttributeInstance(EntityAttributes.GENERIC_ARMOR).addPersistentModifier(COVERED_ARMOR_BONUS);
                this.playSound(SoundEvents.ENTITY_SHULKER_CLOSE, 1.0f, 1.0f);
                this.emitGameEvent(GameEvent.CONTAINER_CLOSE);
            } else {
                this.playSound(SoundEvents.ENTITY_SHULKER_OPEN, 1.0f, 1.0f);
                this.emitGameEvent(GameEvent.CONTAINER_OPEN);
            }
        }
        this.dataTracker.set(PEEK_AMOUNT, (byte)peekAmount);
    }

    public float getOpenProgress(float delta) {
        return MathHelper.lerp(delta, this.prevOpenProgress, this.openProgress);
    }

    @Override
    public void onSpawnPacket(EntitySpawnS2CPacket packet) {
        super.onSpawnPacket(packet);
        this.bodyYaw = 0.0f;
        this.prevBodyYaw = 0.0f;
    }

    @Override
    public int getMaxLookPitchChange() {
        return 180;
    }

    @Override
    public int getMaxHeadRotation() {
        return 180;
    }

    @Override
    public void pushAwayFrom(Entity entity) {
    }

    public Optional<Vec3d> getRenderPositionOffset(float tickDelta) {
        if (this.prevAttachedBlock == null || this.teleportLerpTimer <= 0) {
            return Optional.empty();
        }
        double d = (double)((float)this.teleportLerpTimer - tickDelta) / 6.0;
        d *= d;
        BlockPos lv = this.getBlockPos();
        double e = (double)(lv.getX() - this.prevAttachedBlock.getX()) * d;
        double g = (double)(lv.getY() - this.prevAttachedBlock.getY()) * d;
        double h = (double)(lv.getZ() - this.prevAttachedBlock.getZ()) * d;
        return Optional.of(new Vec3d(-e, -g, -h));
    }

    @Override
    protected float clampScale(float scale) {
        return Math.min(scale, 3.0f);
    }

    @Override
    public void setVariant(Optional<DyeColor> optional) {
        this.dataTracker.set(COLOR, optional.map(color -> (byte)color.getId()).orElse((byte)16));
    }

    @Override
    public Optional<DyeColor> getVariant() {
        return Optional.ofNullable(this.getColor());
    }

    @Nullable
    public DyeColor getColor() {
        byte b = this.dataTracker.get(COLOR);
        if (b == 16 || b > 15) {
            return null;
        }
        return DyeColor.byId(b);
    }

    @Override
    public /* synthetic */ Object getVariant() {
        return this.getVariant();
    }

    class ShulkerLookControl
    extends LookControl {
        public ShulkerLookControl(MobEntity entity) {
            super(entity);
        }

        @Override
        protected void clampHeadYaw() {
        }

        @Override
        protected Optional<Float> getTargetYaw() {
            Direction lv = ShulkerEntity.this.getAttachedFace().getOpposite();
            Vector3f vector3f = lv.getRotationQuaternion().transform(new Vector3f(SOUTH_VECTOR));
            Vec3i lv2 = lv.getVector();
            Vector3f vector3f2 = new Vector3f(lv2.getX(), lv2.getY(), lv2.getZ());
            vector3f2.cross(vector3f);
            double d = this.x - this.entity.getX();
            double e = this.y - this.entity.getEyeY();
            double f = this.z - this.entity.getZ();
            Vector3f vector3f3 = new Vector3f((float)d, (float)e, (float)f);
            float g = vector3f2.dot(vector3f3);
            float h = vector3f.dot(vector3f3);
            return Math.abs(g) > 1.0E-5f || Math.abs(h) > 1.0E-5f ? Optional.of(Float.valueOf((float)(MathHelper.atan2(-g, h) * 57.2957763671875))) : Optional.empty();
        }

        @Override
        protected Optional<Float> getTargetPitch() {
            return Optional.of(Float.valueOf(0.0f));
        }
    }

    class ShootBulletGoal
    extends Goal {
        private int counter;

        public ShootBulletGoal() {
            this.setControls(EnumSet.of(Goal.Control.MOVE, Goal.Control.LOOK));
        }

        @Override
        public boolean canStart() {
            LivingEntity lv = ShulkerEntity.this.getTarget();
            if (lv == null || !lv.isAlive()) {
                return false;
            }
            return ShulkerEntity.this.getWorld().getDifficulty() != Difficulty.PEACEFUL;
        }

        @Override
        public void start() {
            this.counter = 20;
            ShulkerEntity.this.setPeekAmount(100);
        }

        @Override
        public void stop() {
            ShulkerEntity.this.setPeekAmount(0);
        }

        @Override
        public boolean shouldRunEveryTick() {
            return true;
        }

        @Override
        public void tick() {
            if (ShulkerEntity.this.getWorld().getDifficulty() == Difficulty.PEACEFUL) {
                return;
            }
            --this.counter;
            LivingEntity lv = ShulkerEntity.this.getTarget();
            if (lv == null) {
                return;
            }
            ShulkerEntity.this.getLookControl().lookAt(lv, 180.0f, 180.0f);
            double d = ShulkerEntity.this.squaredDistanceTo(lv);
            if (d < 400.0) {
                if (this.counter <= 0) {
                    this.counter = 20 + ShulkerEntity.this.random.nextInt(10) * 20 / 2;
                    ShulkerEntity.this.getWorld().spawnEntity(new ShulkerBulletEntity(ShulkerEntity.this.getWorld(), ShulkerEntity.this, lv, ShulkerEntity.this.getAttachedFace().getAxis()));
                    ShulkerEntity.this.playSound(SoundEvents.ENTITY_SHULKER_SHOOT, 2.0f, (ShulkerEntity.this.random.nextFloat() - ShulkerEntity.this.random.nextFloat()) * 0.2f + 1.0f);
                }
            } else {
                ShulkerEntity.this.setTarget(null);
            }
            super.tick();
        }
    }

    class PeekGoal
    extends Goal {
        private int counter;

        PeekGoal() {
        }

        @Override
        public boolean canStart() {
            return ShulkerEntity.this.getTarget() == null && ShulkerEntity.this.random.nextInt(PeekGoal.toGoalTicks(40)) == 0 && ShulkerEntity.this.canStay(ShulkerEntity.this.getBlockPos(), ShulkerEntity.this.getAttachedFace());
        }

        @Override
        public boolean shouldContinue() {
            return ShulkerEntity.this.getTarget() == null && this.counter > 0;
        }

        @Override
        public void start() {
            this.counter = this.getTickCount(20 * (1 + ShulkerEntity.this.random.nextInt(3)));
            ShulkerEntity.this.setPeekAmount(30);
        }

        @Override
        public void stop() {
            if (ShulkerEntity.this.getTarget() == null) {
                ShulkerEntity.this.setPeekAmount(0);
            }
        }

        @Override
        public void tick() {
            --this.counter;
        }
    }

    class TargetPlayerGoal
    extends ActiveTargetGoal<PlayerEntity> {
        public TargetPlayerGoal(ShulkerEntity shulker) {
            super((MobEntity)shulker, PlayerEntity.class, true);
        }

        @Override
        public boolean canStart() {
            if (ShulkerEntity.this.getWorld().getDifficulty() == Difficulty.PEACEFUL) {
                return false;
            }
            return super.canStart();
        }

        @Override
        protected Box getSearchBox(double distance) {
            Direction lv = ((ShulkerEntity)this.mob).getAttachedFace();
            if (lv.getAxis() == Direction.Axis.X) {
                return this.mob.getBoundingBox().expand(4.0, distance, distance);
            }
            if (lv.getAxis() == Direction.Axis.Z) {
                return this.mob.getBoundingBox().expand(distance, distance, 4.0);
            }
            return this.mob.getBoundingBox().expand(distance, 4.0, distance);
        }
    }

    static class TargetOtherTeamGoal
    extends ActiveTargetGoal<LivingEntity> {
        public TargetOtherTeamGoal(ShulkerEntity shulker) {
            super(shulker, LivingEntity.class, 10, true, false, entity -> entity instanceof Monster);
        }

        @Override
        public boolean canStart() {
            if (this.mob.getScoreboardTeam() == null) {
                return false;
            }
            return super.canStart();
        }

        @Override
        protected Box getSearchBox(double distance) {
            Direction lv = ((ShulkerEntity)this.mob).getAttachedFace();
            if (lv.getAxis() == Direction.Axis.X) {
                return this.mob.getBoundingBox().expand(4.0, distance, distance);
            }
            if (lv.getAxis() == Direction.Axis.Z) {
                return this.mob.getBoundingBox().expand(distance, distance, 4.0);
            }
            return this.mob.getBoundingBox().expand(distance, 4.0, distance);
        }
    }

    static class ShulkerBodyControl
    extends BodyControl {
        public ShulkerBodyControl(MobEntity arg) {
            super(arg);
        }

        @Override
        public void tick() {
        }
    }
}

