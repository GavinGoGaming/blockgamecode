/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 * 
 * Could not load the following classes:
 *  org.jetbrains.annotations.Nullable
 */
package net.minecraft.entity;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.mojang.logging.LogUtils;
import it.unimi.dsi.fastutil.doubles.DoubleList;
import it.unimi.dsi.fastutil.doubles.DoubleListIterator;
import it.unimi.dsi.fastutil.floats.FloatArraySet;
import it.unimi.dsi.fastutil.floats.FloatArrays;
import it.unimi.dsi.fastutil.objects.Object2DoubleArrayMap;
import it.unimi.dsi.fastutil.objects.Object2DoubleMap;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.BiConsumer;
import java.util.function.Predicate;
import java.util.stream.Stream;
import net.minecraft.advancement.criterion.Criteria;
import net.minecraft.block.Block;
import net.minecraft.block.BlockRenderType;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.FenceGateBlock;
import net.minecraft.block.HoneyBlock;
import net.minecraft.block.Portal;
import net.minecraft.block.ShapeContext;
import net.minecraft.block.piston.PistonBehavior;
import net.minecraft.command.argument.EntityAnchorArgumentType;
import net.minecraft.entity.EntityAttachmentType;
import net.minecraft.entity.EntityAttachments;
import net.minecraft.entity.EntityDimensions;
import net.minecraft.entity.EntityPose;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.Leashable;
import net.minecraft.entity.LightningEntity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.MovementType;
import net.minecraft.entity.ProjectileDeflection;
import net.minecraft.entity.TrackedPosition;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.damage.DamageSources;
import net.minecraft.entity.data.DataTracked;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.data.TrackedData;
import net.minecraft.entity.data.TrackedDataHandlerRegistry;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.ProjectileEntity;
import net.minecraft.entity.vehicle.BoatEntity;
import net.minecraft.fluid.Fluid;
import net.minecraft.fluid.FluidState;
import net.minecraft.inventory.StackReference;
import net.minecraft.item.Item;
import net.minecraft.item.ItemConvertible;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtDouble;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtFloat;
import net.minecraft.nbt.NbtList;
import net.minecraft.nbt.NbtString;
import net.minecraft.network.listener.ClientPlayPacketListener;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.s2c.play.EntitySpawnS2CPacket;
import net.minecraft.network.packet.s2c.play.PositionFlag;
import net.minecraft.particle.BlockStateParticleEffect;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.registry.DynamicRegistryManager;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.registry.tag.BlockTags;
import net.minecraft.registry.tag.DamageTypeTags;
import net.minecraft.registry.tag.EntityTypeTags;
import net.minecraft.registry.tag.FluidTags;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.scoreboard.AbstractTeam;
import net.minecraft.scoreboard.ScoreHolder;
import net.minecraft.scoreboard.Team;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.command.CommandOutput;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.EntityTrackerEntry;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ChunkTicketType;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.BlockSoundGroup;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvent;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.HoverEvent;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Arm;
import net.minecraft.util.BlockMirror;
import net.minecraft.util.BlockRotation;
import net.minecraft.util.Hand;
import net.minecraft.util.Identifier;
import net.minecraft.util.Nameable;
import net.minecraft.util.Util;
import net.minecraft.util.crash.CrashException;
import net.minecraft.util.crash.CrashReport;
import net.minecraft.util.crash.CrashReportSection;
import net.minecraft.util.function.BooleanBiFunction;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.ChunkSectionPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec2f;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.Vec3i;
import net.minecraft.util.math.random.Random;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.util.shape.VoxelShapes;
import net.minecraft.world.BlockLocating;
import net.minecraft.world.BlockView;
import net.minecraft.world.GameRules;
import net.minecraft.world.HeightLimitView;
import net.minecraft.world.Heightmap;
import net.minecraft.world.RaycastContext;
import net.minecraft.world.TeleportTarget;
import net.minecraft.world.World;
import net.minecraft.world.WorldEvents;
import net.minecraft.world.border.WorldBorder;
import net.minecraft.world.dimension.NetherPortal;
import net.minecraft.world.dimension.PortalManager;
import net.minecraft.world.entity.EntityChangeListener;
import net.minecraft.world.entity.EntityLike;
import net.minecraft.world.event.GameEvent;
import net.minecraft.world.event.listener.EntityGameEventHandler;
import net.minecraft.world.explosion.Explosion;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;

public abstract class Entity
implements DataTracked,
Nameable,
EntityLike,
CommandOutput,
ScoreHolder {
    private static final Logger LOGGER = LogUtils.getLogger();
    public static final String ID_KEY = "id";
    public static final String PASSENGERS_KEY = "Passengers";
    private static final AtomicInteger CURRENT_ID = new AtomicInteger();
    public static final int field_49791 = 0;
    public static final int MAX_RIDING_COOLDOWN = 60;
    public static final int DEFAULT_PORTAL_COOLDOWN = 300;
    public static final int MAX_COMMAND_TAGS = 1024;
    public static final float field_44870 = 0.2f;
    public static final double field_44871 = 0.500001;
    public static final double field_44872 = 0.999999;
    public static final int DEFAULT_MIN_FREEZE_DAMAGE_TICKS = 140;
    public static final int FREEZING_DAMAGE_INTERVAL = 40;
    public static final int field_49073 = 3;
    private static final Box NULL_BOX = new Box(0.0, 0.0, 0.0, 0.0, 0.0, 0.0);
    private static final double SPEED_IN_WATER = 0.014;
    private static final double SPEED_IN_LAVA_IN_NETHER = 0.007;
    private static final double SPEED_IN_LAVA = 0.0023333333333333335;
    public static final String UUID_KEY = "UUID";
    private static double renderDistanceMultiplier = 1.0;
    private final EntityType<?> type;
    private int id = CURRENT_ID.incrementAndGet();
    public boolean intersectionChecked;
    private ImmutableList<Entity> passengerList = ImmutableList.of();
    protected int ridingCooldown;
    @Nullable
    private Entity vehicle;
    private World world;
    public double prevX;
    public double prevY;
    public double prevZ;
    private Vec3d pos;
    private BlockPos blockPos;
    private ChunkPos chunkPos;
    private Vec3d velocity = Vec3d.ZERO;
    private float yaw;
    private float pitch;
    public float prevYaw;
    public float prevPitch;
    private Box boundingBox = NULL_BOX;
    private boolean onGround;
    public boolean horizontalCollision;
    public boolean verticalCollision;
    public boolean groundCollision;
    public boolean collidedSoftly;
    public boolean velocityModified;
    protected Vec3d movementMultiplier = Vec3d.ZERO;
    @Nullable
    private RemovalReason removalReason;
    public static final float DEFAULT_FRICTION = 0.6f;
    public static final float MIN_RISING_BUBBLE_COLUMN_SPEED = 1.8f;
    public float prevHorizontalSpeed;
    public float horizontalSpeed;
    public float distanceTraveled;
    public float speed;
    public float fallDistance;
    private float nextStepSoundDistance = 1.0f;
    public double lastRenderX;
    public double lastRenderY;
    public double lastRenderZ;
    public boolean noClip;
    protected final Random random = Random.create();
    public int age;
    private int fireTicks = -this.getBurningDuration();
    protected boolean touchingWater;
    protected Object2DoubleMap<TagKey<Fluid>> fluidHeight = new Object2DoubleArrayMap<TagKey<Fluid>>(2);
    protected boolean submergedInWater;
    private final Set<TagKey<Fluid>> submergedFluidTag = new HashSet<TagKey<Fluid>>();
    public int timeUntilRegen;
    protected boolean firstUpdate = true;
    protected final DataTracker dataTracker;
    protected static final TrackedData<Byte> FLAGS = DataTracker.registerData(Entity.class, TrackedDataHandlerRegistry.BYTE);
    protected static final int ON_FIRE_FLAG_INDEX = 0;
    private static final int SNEAKING_FLAG_INDEX = 1;
    private static final int SPRINTING_FLAG_INDEX = 3;
    private static final int SWIMMING_FLAG_INDEX = 4;
    private static final int INVISIBLE_FLAG_INDEX = 5;
    protected static final int GLOWING_FLAG_INDEX = 6;
    protected static final int FALL_FLYING_FLAG_INDEX = 7;
    private static final TrackedData<Integer> AIR = DataTracker.registerData(Entity.class, TrackedDataHandlerRegistry.INTEGER);
    private static final TrackedData<Optional<Text>> CUSTOM_NAME = DataTracker.registerData(Entity.class, TrackedDataHandlerRegistry.OPTIONAL_TEXT_COMPONENT);
    private static final TrackedData<Boolean> NAME_VISIBLE = DataTracker.registerData(Entity.class, TrackedDataHandlerRegistry.BOOLEAN);
    private static final TrackedData<Boolean> SILENT = DataTracker.registerData(Entity.class, TrackedDataHandlerRegistry.BOOLEAN);
    private static final TrackedData<Boolean> NO_GRAVITY = DataTracker.registerData(Entity.class, TrackedDataHandlerRegistry.BOOLEAN);
    protected static final TrackedData<EntityPose> POSE = DataTracker.registerData(Entity.class, TrackedDataHandlerRegistry.ENTITY_POSE);
    private static final TrackedData<Integer> FROZEN_TICKS = DataTracker.registerData(Entity.class, TrackedDataHandlerRegistry.INTEGER);
    private EntityChangeListener changeListener = EntityChangeListener.NONE;
    private final TrackedPosition trackedPosition = new TrackedPosition();
    public boolean ignoreCameraFrustum;
    public boolean velocityDirty;
    @Nullable
    public PortalManager portalManager;
    private int portalCooldown;
    private boolean invulnerable;
    protected UUID uuid = MathHelper.randomUuid(this.random);
    protected String uuidString = this.uuid.toString();
    private boolean glowing;
    private final Set<String> commandTags = Sets.newHashSet();
    private final double[] pistonMovementDelta = new double[]{0.0, 0.0, 0.0};
    private long pistonMovementTick;
    private EntityDimensions dimensions;
    private float standingEyeHeight;
    public boolean inPowderSnow;
    public boolean wasInPowderSnow;
    public boolean wasOnFire;
    public Optional<BlockPos> supportingBlockPos = Optional.empty();
    private boolean forceUpdateSupportingBlockPos = false;
    private float lastChimeIntensity;
    private int lastChimeAge;
    private boolean hasVisualFire;
    @Nullable
    private BlockState stateAtPos = null;

    public Entity(EntityType<?> type, World world) {
        this.type = type;
        this.world = world;
        this.dimensions = type.getDimensions();
        this.pos = Vec3d.ZERO;
        this.blockPos = BlockPos.ORIGIN;
        this.chunkPos = ChunkPos.ORIGIN;
        DataTracker.Builder lv = new DataTracker.Builder(this);
        lv.add(FLAGS, (byte)0);
        lv.add(AIR, this.getMaxAir());
        lv.add(NAME_VISIBLE, false);
        lv.add(CUSTOM_NAME, Optional.empty());
        lv.add(SILENT, false);
        lv.add(NO_GRAVITY, false);
        lv.add(POSE, EntityPose.STANDING);
        lv.add(FROZEN_TICKS, 0);
        this.initDataTracker(lv);
        this.dataTracker = lv.build();
        this.setPosition(0.0, 0.0, 0.0);
        this.standingEyeHeight = this.dimensions.eyeHeight();
    }

    public boolean collidesWithStateAtPos(BlockPos pos, BlockState state) {
        VoxelShape lv = state.getCollisionShape(this.getWorld(), pos, ShapeContext.of(this));
        VoxelShape lv2 = lv.offset(pos.getX(), pos.getY(), pos.getZ());
        return VoxelShapes.matchesAnywhere(lv2, VoxelShapes.cuboid(this.getBoundingBox()), BooleanBiFunction.AND);
    }

    public int getTeamColorValue() {
        Team lv = this.getScoreboardTeam();
        if (lv != null && ((AbstractTeam)lv).getColor().getColorValue() != null) {
            return ((AbstractTeam)lv).getColor().getColorValue();
        }
        return 0xFFFFFF;
    }

    public boolean isSpectator() {
        return false;
    }

    public final void detach() {
        if (this.hasPassengers()) {
            this.removeAllPassengers();
        }
        if (this.hasVehicle()) {
            this.stopRiding();
        }
    }

    public void updateTrackedPosition(double x, double y, double z) {
        this.trackedPosition.setPos(new Vec3d(x, y, z));
    }

    public TrackedPosition getTrackedPosition() {
        return this.trackedPosition;
    }

    public EntityType<?> getType() {
        return this.type;
    }

    @Override
    public int getId() {
        return this.id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public Set<String> getCommandTags() {
        return this.commandTags;
    }

    public boolean addCommandTag(String tag) {
        if (this.commandTags.size() >= 1024) {
            return false;
        }
        return this.commandTags.add(tag);
    }

    public boolean removeCommandTag(String tag) {
        return this.commandTags.remove(tag);
    }

    public void kill() {
        this.remove(RemovalReason.KILLED);
        this.emitGameEvent(GameEvent.ENTITY_DIE);
    }

    public final void discard() {
        this.remove(RemovalReason.DISCARDED);
    }

    protected abstract void initDataTracker(DataTracker.Builder var1);

    public DataTracker getDataTracker() {
        return this.dataTracker;
    }

    public boolean equals(Object o) {
        if (o instanceof Entity) {
            return ((Entity)o).id == this.id;
        }
        return false;
    }

    public int hashCode() {
        return this.id;
    }

    public void remove(RemovalReason reason) {
        this.setRemoved(reason);
    }

    public void onRemoved() {
    }

    public void setPose(EntityPose pose) {
        this.dataTracker.set(POSE, pose);
    }

    public EntityPose getPose() {
        return this.dataTracker.get(POSE);
    }

    public boolean isInPose(EntityPose pose) {
        return this.getPose() == pose;
    }

    public boolean isInRange(Entity entity, double radius) {
        return this.getPos().isInRange(entity.getPos(), radius);
    }

    public boolean isInRange(Entity entity, double horizontalRadius, double verticalRadius) {
        double f = entity.getX() - this.getX();
        double g = entity.getY() - this.getY();
        double h = entity.getZ() - this.getZ();
        return MathHelper.squaredHypot(f, h) < MathHelper.square(horizontalRadius) && MathHelper.square(g) < MathHelper.square(verticalRadius);
    }

    protected void setRotation(float yaw, float pitch) {
        this.setYaw(yaw % 360.0f);
        this.setPitch(pitch % 360.0f);
    }

    public final void setPosition(Vec3d pos) {
        this.setPosition(pos.getX(), pos.getY(), pos.getZ());
    }

    public void setPosition(double x, double y, double z) {
        this.setPos(x, y, z);
        this.setBoundingBox(this.calculateBoundingBox());
    }

    protected Box calculateBoundingBox() {
        return this.dimensions.getBoxAt(this.pos);
    }

    protected void refreshPosition() {
        this.setPosition(this.pos.x, this.pos.y, this.pos.z);
    }

    public void changeLookDirection(double cursorDeltaX, double cursorDeltaY) {
        float f = (float)cursorDeltaY * 0.15f;
        float g = (float)cursorDeltaX * 0.15f;
        this.setPitch(this.getPitch() + f);
        this.setYaw(this.getYaw() + g);
        this.setPitch(MathHelper.clamp(this.getPitch(), -90.0f, 90.0f));
        this.prevPitch += f;
        this.prevYaw += g;
        this.prevPitch = MathHelper.clamp(this.prevPitch, -90.0f, 90.0f);
        if (this.vehicle != null) {
            this.vehicle.onPassengerLookAround(this);
        }
    }

    public void tick() {
        this.baseTick();
    }

    public void baseTick() {
        this.getWorld().getProfiler().push("entityBaseTick");
        this.stateAtPos = null;
        if (this.hasVehicle() && this.getVehicle().isRemoved()) {
            this.stopRiding();
        }
        if (this.ridingCooldown > 0) {
            --this.ridingCooldown;
        }
        this.prevHorizontalSpeed = this.horizontalSpeed;
        this.prevPitch = this.getPitch();
        this.prevYaw = this.getYaw();
        this.tickPortalTeleportation();
        if (this.shouldSpawnSprintingParticles()) {
            this.spawnSprintingParticles();
        }
        this.wasInPowderSnow = this.inPowderSnow;
        this.inPowderSnow = false;
        this.updateWaterState();
        this.updateSubmergedInWaterState();
        this.updateSwimming();
        if (this.getWorld().isClient) {
            this.extinguish();
        } else if (this.fireTicks > 0) {
            if (this.isFireImmune()) {
                this.setFireTicks(this.fireTicks - 4);
                if (this.fireTicks < 0) {
                    this.extinguish();
                }
            } else {
                if (this.fireTicks % 20 == 0 && !this.isInLava()) {
                    this.damage(this.getDamageSources().onFire(), 1.0f);
                }
                this.setFireTicks(this.fireTicks - 1);
            }
            if (this.getFrozenTicks() > 0) {
                this.setFrozenTicks(0);
                this.getWorld().syncWorldEvent(null, WorldEvents.FIRE_EXTINGUISHED, this.blockPos, 1);
            }
        }
        if (this.isInLava()) {
            this.setOnFireFromLava();
            this.fallDistance *= 0.5f;
        }
        this.attemptTickInVoid();
        if (!this.getWorld().isClient) {
            this.setOnFire(this.fireTicks > 0);
        }
        this.firstUpdate = false;
        if (!this.getWorld().isClient && this instanceof Leashable) {
            Leashable.tickLeash((Entity)((Object)((Leashable)((Object)this))));
        }
        this.getWorld().getProfiler().pop();
    }

    public void setOnFire(boolean onFire) {
        this.setFlag(ON_FIRE_FLAG_INDEX, onFire || this.hasVisualFire);
    }

    public void attemptTickInVoid() {
        if (this.getY() < (double)(this.getWorld().getBottomY() - 64)) {
            this.tickInVoid();
        }
    }

    public void resetPortalCooldown() {
        this.portalCooldown = this.getDefaultPortalCooldown();
    }

    public void setPortalCooldown(int portalCooldown) {
        this.portalCooldown = portalCooldown;
    }

    public int getPortalCooldown() {
        return this.portalCooldown;
    }

    public boolean hasPortalCooldown() {
        return this.portalCooldown > 0;
    }

    protected void tickPortalCooldown() {
        if (this.hasPortalCooldown()) {
            --this.portalCooldown;
        }
    }

    public void setOnFireFromLava() {
        if (this.isFireImmune()) {
            return;
        }
        this.setOnFireFor(15.0f);
        if (this.damage(this.getDamageSources().lava(), 4.0f)) {
            this.playSound(SoundEvents.ENTITY_GENERIC_BURN, 0.4f, 2.0f + this.random.nextFloat() * 0.4f);
        }
    }

    public final void setOnFireFor(float seconds) {
        this.setOnFireForTicks(MathHelper.floor(seconds * 20.0f));
    }

    public void setOnFireForTicks(int ticks) {
        if (this.fireTicks < ticks) {
            this.setFireTicks(ticks);
        }
    }

    public void setFireTicks(int fireTicks) {
        this.fireTicks = fireTicks;
    }

    public int getFireTicks() {
        return this.fireTicks;
    }

    public void extinguish() {
        this.setFireTicks(0);
    }

    protected void tickInVoid() {
        this.discard();
    }

    public boolean doesNotCollide(double offsetX, double offsetY, double offsetZ) {
        return this.doesNotCollide(this.getBoundingBox().offset(offsetX, offsetY, offsetZ));
    }

    private boolean doesNotCollide(Box box) {
        return this.getWorld().isSpaceEmpty(this, box) && !this.getWorld().containsFluid(box);
    }

    public void setOnGround(boolean onGround) {
        this.onGround = onGround;
        this.updateSupportingBlockPos(onGround, null);
    }

    public void setOnGround(boolean onGround, Vec3d movement) {
        this.onGround = onGround;
        this.updateSupportingBlockPos(onGround, movement);
    }

    public boolean isSupportedBy(BlockPos pos) {
        return this.supportingBlockPos.isPresent() && this.supportingBlockPos.get().equals(pos);
    }

    protected void updateSupportingBlockPos(boolean onGround, @Nullable Vec3d movement) {
        if (onGround) {
            Box lv = this.getBoundingBox();
            Box lv2 = new Box(lv.minX, lv.minY - 1.0E-6, lv.minZ, lv.maxX, lv.minY, lv.maxZ);
            Optional<BlockPos> optional = this.world.findSupportingBlockPos(this, lv2);
            if (optional.isPresent() || this.forceUpdateSupportingBlockPos) {
                this.supportingBlockPos = optional;
            } else if (movement != null) {
                Box lv3 = lv2.offset(-movement.x, 0.0, -movement.z);
                optional = this.world.findSupportingBlockPos(this, lv3);
                this.supportingBlockPos = optional;
            }
            this.forceUpdateSupportingBlockPos = optional.isEmpty();
        } else {
            this.forceUpdateSupportingBlockPos = false;
            if (this.supportingBlockPos.isPresent()) {
                this.supportingBlockPos = Optional.empty();
            }
        }
    }

    public boolean isOnGround() {
        return this.onGround;
    }

    public void move(MovementType movementType, Vec3d movement) {
        MoveEffect lv7;
        Vec3d lv;
        double d;
        if (this.noClip) {
            this.setPosition(this.getX() + movement.x, this.getY() + movement.y, this.getZ() + movement.z);
            return;
        }
        this.wasOnFire = this.isOnFire();
        if (movementType == MovementType.PISTON && (movement = this.adjustMovementForPiston(movement)).equals(Vec3d.ZERO)) {
            return;
        }
        this.getWorld().getProfiler().push("move");
        if (this.movementMultiplier.lengthSquared() > 1.0E-7) {
            movement = movement.multiply(this.movementMultiplier);
            this.movementMultiplier = Vec3d.ZERO;
            this.setVelocity(Vec3d.ZERO);
        }
        if ((d = (lv = this.adjustMovementForCollisions(movement = this.adjustMovementForSneaking(movement, movementType))).lengthSquared()) > 1.0E-7) {
            BlockHitResult lv2;
            if (this.fallDistance != 0.0f && d >= 1.0 && (lv2 = this.getWorld().raycast(new RaycastContext(this.getPos(), this.getPos().add(lv), RaycastContext.ShapeType.FALLDAMAGE_RESETTING, RaycastContext.FluidHandling.WATER, this))).getType() != HitResult.Type.MISS) {
                this.onLanding();
            }
            this.setPosition(this.getX() + lv.x, this.getY() + lv.y, this.getZ() + lv.z);
        }
        this.getWorld().getProfiler().pop();
        this.getWorld().getProfiler().push("rest");
        boolean bl = !MathHelper.approximatelyEquals(movement.x, lv.x);
        boolean bl2 = !MathHelper.approximatelyEquals(movement.z, lv.z);
        this.horizontalCollision = bl || bl2;
        this.verticalCollision = movement.y != lv.y;
        this.groundCollision = this.verticalCollision && movement.y < 0.0;
        this.collidedSoftly = this.horizontalCollision ? this.hasCollidedSoftly(lv) : false;
        this.setOnGround(this.groundCollision, lv);
        BlockPos lv3 = this.getLandingPos();
        BlockState lv4 = this.getWorld().getBlockState(lv3);
        this.fall(lv.y, this.isOnGround(), lv4, lv3);
        if (this.isRemoved()) {
            this.getWorld().getProfiler().pop();
            return;
        }
        if (this.horizontalCollision) {
            Vec3d lv5 = this.getVelocity();
            this.setVelocity(bl ? 0.0 : lv5.x, lv5.y, bl2 ? 0.0 : lv5.z);
        }
        Block lv6 = lv4.getBlock();
        if (movement.y != lv.y) {
            lv6.onEntityLand(this.getWorld(), this);
        }
        if (this.isOnGround()) {
            lv6.onSteppedOn(this.getWorld(), lv3, lv4, this);
        }
        if ((lv7 = this.getMoveEffect()).hasAny() && !this.hasVehicle()) {
            double e = lv.x;
            double f = lv.y;
            double g = lv.z;
            this.speed += (float)(lv.length() * 0.6);
            BlockPos lv8 = this.getSteppingPos();
            BlockState lv9 = this.getWorld().getBlockState(lv8);
            boolean bl3 = this.canClimb(lv9);
            if (!bl3) {
                f = 0.0;
            }
            this.horizontalSpeed += (float)lv.horizontalLength() * 0.6f;
            this.distanceTraveled += (float)Math.sqrt(e * e + f * f + g * g) * 0.6f;
            if (this.distanceTraveled > this.nextStepSoundDistance && !lv9.isAir()) {
                boolean bl4 = lv8.equals(lv3);
                boolean bl5 = this.stepOnBlock(lv3, lv4, lv7.playsSounds(), bl4, movement);
                if (!bl4) {
                    bl5 |= this.stepOnBlock(lv8, lv9, false, lv7.emitsGameEvents(), movement);
                }
                if (bl5) {
                    this.nextStepSoundDistance = this.calculateNextStepSoundDistance();
                } else if (this.isTouchingWater()) {
                    this.nextStepSoundDistance = this.calculateNextStepSoundDistance();
                    if (lv7.playsSounds()) {
                        this.playSwimSound();
                    }
                    if (lv7.emitsGameEvents()) {
                        this.emitGameEvent(GameEvent.SWIM);
                    }
                }
            } else if (lv9.isAir()) {
                this.addAirTravelEffects();
            }
        }
        this.tryCheckBlockCollision();
        float h = this.getVelocityMultiplier();
        this.setVelocity(this.getVelocity().multiply(h, 1.0, h));
        if (this.getWorld().getStatesInBoxIfLoaded(this.getBoundingBox().contract(1.0E-6)).noneMatch(state -> state.isIn(BlockTags.FIRE) || state.isOf(Blocks.LAVA))) {
            if (this.fireTicks <= 0) {
                this.setFireTicks(-this.getBurningDuration());
            }
            if (this.wasOnFire && (this.inPowderSnow || this.isWet())) {
                this.playExtinguishSound();
            }
        }
        if (this.isOnFire() && (this.inPowderSnow || this.isWet())) {
            this.setFireTicks(-this.getBurningDuration());
        }
        this.getWorld().getProfiler().pop();
    }

    private boolean canClimb(BlockState state) {
        return state.isIn(BlockTags.CLIMBABLE) || state.isOf(Blocks.POWDER_SNOW);
    }

    private boolean stepOnBlock(BlockPos pos, BlockState state, boolean playSound, boolean emitEvent, Vec3d movement) {
        if (state.isAir()) {
            return false;
        }
        boolean bl3 = this.canClimb(state);
        if ((this.isOnGround() || bl3 || this.isInSneakingPose() && movement.y == 0.0 || this.isOnRail()) && !this.isSwimming()) {
            if (playSound) {
                this.playStepSounds(pos, state);
            }
            if (emitEvent) {
                this.getWorld().emitGameEvent(GameEvent.STEP, this.getPos(), GameEvent.Emitter.of(this, state));
            }
            return true;
        }
        return false;
    }

    protected boolean hasCollidedSoftly(Vec3d adjustedMovement) {
        return false;
    }

    protected void tryCheckBlockCollision() {
        try {
            this.checkBlockCollision();
        } catch (Throwable throwable) {
            CrashReport lv = CrashReport.create(throwable, "Checking entity block collision");
            CrashReportSection lv2 = lv.addElement("Entity being checked for collision");
            this.populateCrashReport(lv2);
            throw new CrashException(lv);
        }
    }

    protected void playExtinguishSound() {
        this.playSound(SoundEvents.ENTITY_GENERIC_EXTINGUISH_FIRE, 0.7f, 1.6f + (this.random.nextFloat() - this.random.nextFloat()) * 0.4f);
    }

    public void extinguishWithSound() {
        if (!this.getWorld().isClient && this.wasOnFire) {
            this.playExtinguishSound();
        }
        this.extinguish();
    }

    protected void addAirTravelEffects() {
        if (this.isFlappingWings()) {
            this.addFlapEffects();
            if (this.getMoveEffect().emitsGameEvents()) {
                this.emitGameEvent(GameEvent.FLAP);
            }
        }
    }

    @Deprecated
    public BlockPos getLandingPos() {
        return this.getPosWithYOffset(0.2f);
    }

    public BlockPos getVelocityAffectingPos() {
        return this.getPosWithYOffset(0.500001f);
    }

    public BlockPos getSteppingPos() {
        return this.getPosWithYOffset(1.0E-5f);
    }

    protected BlockPos getPosWithYOffset(float offset) {
        if (this.supportingBlockPos.isPresent()) {
            BlockPos lv = this.supportingBlockPos.get();
            if (offset > 1.0E-5f) {
                BlockState lv2 = this.getWorld().getBlockState(lv);
                if ((double)offset <= 0.5 && lv2.isIn(BlockTags.FENCES) || lv2.isIn(BlockTags.WALLS) || lv2.getBlock() instanceof FenceGateBlock) {
                    return lv;
                }
                return lv.withY(MathHelper.floor(this.pos.y - (double)offset));
            }
            return lv;
        }
        int i = MathHelper.floor(this.pos.x);
        int j = MathHelper.floor(this.pos.y - (double)offset);
        int k = MathHelper.floor(this.pos.z);
        return new BlockPos(i, j, k);
    }

    protected float getJumpVelocityMultiplier() {
        float f = this.getWorld().getBlockState(this.getBlockPos()).getBlock().getJumpVelocityMultiplier();
        float g = this.getWorld().getBlockState(this.getVelocityAffectingPos()).getBlock().getJumpVelocityMultiplier();
        return (double)f == 1.0 ? g : f;
    }

    protected float getVelocityMultiplier() {
        BlockState lv = this.getWorld().getBlockState(this.getBlockPos());
        float f = lv.getBlock().getVelocityMultiplier();
        if (lv.isOf(Blocks.WATER) || lv.isOf(Blocks.BUBBLE_COLUMN)) {
            return f;
        }
        return (double)f == 1.0 ? this.getWorld().getBlockState(this.getVelocityAffectingPos()).getBlock().getVelocityMultiplier() : f;
    }

    protected Vec3d adjustMovementForSneaking(Vec3d movement, MovementType type) {
        return movement;
    }

    protected Vec3d adjustMovementForPiston(Vec3d movement) {
        if (movement.lengthSquared() <= 1.0E-7) {
            return movement;
        }
        long l = this.getWorld().getTime();
        if (l != this.pistonMovementTick) {
            Arrays.fill(this.pistonMovementDelta, 0.0);
            this.pistonMovementTick = l;
        }
        if (movement.x != 0.0) {
            double d = this.calculatePistonMovementFactor(Direction.Axis.X, movement.x);
            return Math.abs(d) <= (double)1.0E-5f ? Vec3d.ZERO : new Vec3d(d, 0.0, 0.0);
        }
        if (movement.y != 0.0) {
            double d = this.calculatePistonMovementFactor(Direction.Axis.Y, movement.y);
            return Math.abs(d) <= (double)1.0E-5f ? Vec3d.ZERO : new Vec3d(0.0, d, 0.0);
        }
        if (movement.z != 0.0) {
            double d = this.calculatePistonMovementFactor(Direction.Axis.Z, movement.z);
            return Math.abs(d) <= (double)1.0E-5f ? Vec3d.ZERO : new Vec3d(0.0, 0.0, d);
        }
        return Vec3d.ZERO;
    }

    private double calculatePistonMovementFactor(Direction.Axis axis, double offsetFactor) {
        int i = axis.ordinal();
        double e = MathHelper.clamp(offsetFactor + this.pistonMovementDelta[i], -0.51, 0.51);
        offsetFactor = e - this.pistonMovementDelta[i];
        this.pistonMovementDelta[i] = e;
        return offsetFactor;
    }

    private Vec3d adjustMovementForCollisions(Vec3d movement) {
        boolean bl4;
        Box lv = this.getBoundingBox();
        List<VoxelShape> list = this.getWorld().getEntityCollisions(this, lv.stretch(movement));
        Vec3d lv2 = movement.lengthSquared() == 0.0 ? movement : Entity.adjustMovementForCollisions(this, movement, lv, this.getWorld(), list);
        boolean bl = movement.x != lv2.x;
        boolean bl2 = movement.y != lv2.y;
        boolean bl3 = movement.z != lv2.z;
        boolean bl5 = bl4 = bl2 && movement.y < 0.0;
        if (this.getStepHeight() > 0.0f && (bl4 || this.isOnGround()) && (bl || bl3)) {
            float[] fs;
            Box lv3 = bl4 ? lv.offset(0.0, lv2.y, 0.0) : lv;
            Box lv4 = lv3.stretch(movement.x, this.getStepHeight(), movement.z);
            if (!bl4) {
                lv4 = lv4.stretch(0.0, -1.0E-5f, 0.0);
            }
            List<VoxelShape> list2 = Entity.findCollisionsForMovement(this, this.world, list, lv4);
            float f = (float)lv2.y;
            for (float g : fs = Entity.collectStepHeights(lv3, list2, this.getStepHeight(), f)) {
                Vec3d lv5 = Entity.adjustMovementForCollisions(new Vec3d(movement.x, g, movement.z), lv3, list2);
                if (!(lv5.horizontalLengthSquared() > lv2.horizontalLengthSquared())) continue;
                double d = lv.minY - lv3.minY;
                return lv5.add(0.0, -d, 0.0);
            }
        }
        return lv2;
    }

    private static float[] collectStepHeights(Box collisionBox, List<VoxelShape> collisions, float f, float stepHeight) {
        FloatArraySet floatSet = new FloatArraySet(4);
        block0: for (VoxelShape lv : collisions) {
            DoubleList doubleList = lv.getPointPositions(Direction.Axis.Y);
            DoubleListIterator doubleListIterator = doubleList.iterator();
            while (doubleListIterator.hasNext()) {
                double d = (Double)doubleListIterator.next();
                float h = (float)(d - collisionBox.minY);
                if (h < 0.0f || h == stepHeight) continue;
                if (h > f) continue block0;
                floatSet.add(h);
            }
        }
        float[] fs = floatSet.toFloatArray();
        FloatArrays.unstableSort(fs);
        return fs;
    }

    public static Vec3d adjustMovementForCollisions(@Nullable Entity entity, Vec3d movement, Box entityBoundingBox, World world, List<VoxelShape> collisions) {
        List<VoxelShape> list2 = Entity.findCollisionsForMovement(entity, world, collisions, entityBoundingBox.stretch(movement));
        return Entity.adjustMovementForCollisions(movement, entityBoundingBox, list2);
    }

    private static List<VoxelShape> findCollisionsForMovement(@Nullable Entity entity, World world, List<VoxelShape> regularCollisions, Box movingEntityBoundingBox) {
        boolean bl;
        ImmutableList.Builder builder = ImmutableList.builderWithExpectedSize(regularCollisions.size() + 1);
        if (!regularCollisions.isEmpty()) {
            builder.addAll(regularCollisions);
        }
        WorldBorder lv = world.getWorldBorder();
        boolean bl2 = bl = entity != null && lv.canCollide(entity, movingEntityBoundingBox);
        if (bl) {
            builder.add(lv.asVoxelShape());
        }
        builder.addAll(world.getBlockCollisions(entity, movingEntityBoundingBox));
        return builder.build();
    }

    private static Vec3d adjustMovementForCollisions(Vec3d movement, Box entityBoundingBox, List<VoxelShape> collisions) {
        boolean bl;
        if (collisions.isEmpty()) {
            return movement;
        }
        double d = movement.x;
        double e = movement.y;
        double f = movement.z;
        if (e != 0.0 && (e = VoxelShapes.calculateMaxOffset(Direction.Axis.Y, entityBoundingBox, collisions, e)) != 0.0) {
            entityBoundingBox = entityBoundingBox.offset(0.0, e, 0.0);
        }
        boolean bl2 = bl = Math.abs(d) < Math.abs(f);
        if (bl && f != 0.0 && (f = VoxelShapes.calculateMaxOffset(Direction.Axis.Z, entityBoundingBox, collisions, f)) != 0.0) {
            entityBoundingBox = entityBoundingBox.offset(0.0, 0.0, f);
        }
        if (d != 0.0) {
            d = VoxelShapes.calculateMaxOffset(Direction.Axis.X, entityBoundingBox, collisions, d);
            if (!bl && d != 0.0) {
                entityBoundingBox = entityBoundingBox.offset(d, 0.0, 0.0);
            }
        }
        if (!bl && f != 0.0) {
            f = VoxelShapes.calculateMaxOffset(Direction.Axis.Z, entityBoundingBox, collisions, f);
        }
        return new Vec3d(d, e, f);
    }

    protected float calculateNextStepSoundDistance() {
        return (int)this.distanceTraveled + 1;
    }

    protected SoundEvent getSwimSound() {
        return SoundEvents.ENTITY_GENERIC_SWIM;
    }

    protected SoundEvent getSplashSound() {
        return SoundEvents.ENTITY_GENERIC_SPLASH;
    }

    protected SoundEvent getHighSpeedSplashSound() {
        return SoundEvents.ENTITY_GENERIC_SPLASH;
    }

    protected void checkBlockCollision() {
        Box lv = this.getBoundingBox();
        BlockPos lv2 = BlockPos.ofFloored(lv.minX + 1.0E-7, lv.minY + 1.0E-7, lv.minZ + 1.0E-7);
        BlockPos lv3 = BlockPos.ofFloored(lv.maxX - 1.0E-7, lv.maxY - 1.0E-7, lv.maxZ - 1.0E-7);
        if (this.getWorld().isRegionLoaded(lv2, lv3)) {
            BlockPos.Mutable lv4 = new BlockPos.Mutable();
            for (int i = lv2.getX(); i <= lv3.getX(); ++i) {
                for (int j = lv2.getY(); j <= lv3.getY(); ++j) {
                    for (int k = lv2.getZ(); k <= lv3.getZ(); ++k) {
                        if (!this.isAlive()) {
                            return;
                        }
                        lv4.set(i, j, k);
                        BlockState lv5 = this.getWorld().getBlockState(lv4);
                        try {
                            lv5.onEntityCollision(this.getWorld(), lv4, this);
                            this.onBlockCollision(lv5);
                            continue;
                        } catch (Throwable throwable) {
                            CrashReport lv6 = CrashReport.create(throwable, "Colliding entity with block");
                            CrashReportSection lv7 = lv6.addElement("Block being collided with");
                            CrashReportSection.addBlockInfo(lv7, this.getWorld(), lv4, lv5);
                            throw new CrashException(lv6);
                        }
                    }
                }
            }
        }
    }

    protected void onBlockCollision(BlockState state) {
    }

    public BlockPos getWorldSpawnPos(ServerWorld world, BlockPos basePos) {
        BlockPos lv = world.getSpawnPos();
        Vec3d lv2 = lv.toCenterPos();
        int i = world.getWorldChunk(lv).sampleHeightmap(Heightmap.Type.MOTION_BLOCKING_NO_LEAVES, lv.getX(), lv.getZ()) + 1;
        return BlockPos.ofFloored(lv2.x, i, lv2.z);
    }

    public void emitGameEvent(RegistryEntry<GameEvent> event, @Nullable Entity entity) {
        this.getWorld().emitGameEvent(entity, event, this.pos);
    }

    public void emitGameEvent(RegistryEntry<GameEvent> event) {
        this.emitGameEvent(event, this);
    }

    private void playStepSounds(BlockPos pos, BlockState state) {
        this.playStepSound(pos, state);
        if (this.shouldPlayAmethystChimeSound(state)) {
            this.playAmethystChimeSound();
        }
    }

    protected void playSwimSound() {
        Entity lv = Objects.requireNonNullElse(this.getControllingPassenger(), this);
        float f = lv == this ? 0.35f : 0.4f;
        Vec3d lv2 = lv.getVelocity();
        float g = Math.min(1.0f, (float)Math.sqrt(lv2.x * lv2.x * (double)0.2f + lv2.y * lv2.y + lv2.z * lv2.z * (double)0.2f) * f);
        this.playSwimSound(g);
    }

    protected BlockPos getStepSoundPos(BlockPos pos) {
        BlockPos lv = pos.up();
        BlockState lv2 = this.getWorld().getBlockState(lv);
        if (lv2.isIn(BlockTags.INSIDE_STEP_SOUND_BLOCKS) || lv2.isIn(BlockTags.COMBINATION_STEP_SOUND_BLOCKS)) {
            return lv;
        }
        return pos;
    }

    protected void playCombinationStepSounds(BlockState primaryState, BlockState secondaryState) {
        BlockSoundGroup lv = primaryState.getSoundGroup();
        this.playSound(lv.getStepSound(), lv.getVolume() * 0.15f, lv.getPitch());
        this.playSecondaryStepSound(secondaryState);
    }

    protected void playSecondaryStepSound(BlockState state) {
        BlockSoundGroup lv = state.getSoundGroup();
        this.playSound(lv.getStepSound(), lv.getVolume() * 0.05f, lv.getPitch() * 0.8f);
    }

    protected void playStepSound(BlockPos pos, BlockState state) {
        BlockSoundGroup lv = state.getSoundGroup();
        this.playSound(lv.getStepSound(), lv.getVolume() * 0.15f, lv.getPitch());
    }

    private boolean shouldPlayAmethystChimeSound(BlockState state) {
        return state.isIn(BlockTags.CRYSTAL_SOUND_BLOCKS) && this.age >= this.lastChimeAge + 20;
    }

    private void playAmethystChimeSound() {
        this.lastChimeIntensity *= (float)Math.pow(0.997, this.age - this.lastChimeAge);
        this.lastChimeIntensity = Math.min(1.0f, this.lastChimeIntensity + 0.07f);
        float f = 0.5f + this.lastChimeIntensity * this.random.nextFloat() * 1.2f;
        float g = 0.1f + this.lastChimeIntensity * 1.2f;
        this.playSound(SoundEvents.BLOCK_AMETHYST_BLOCK_CHIME, g, f);
        this.lastChimeAge = this.age;
    }

    protected void playSwimSound(float volume) {
        this.playSound(this.getSwimSound(), volume, 1.0f + (this.random.nextFloat() - this.random.nextFloat()) * 0.4f);
    }

    protected void addFlapEffects() {
    }

    protected boolean isFlappingWings() {
        return false;
    }

    public void playSound(SoundEvent sound, float volume, float pitch) {
        if (!this.isSilent()) {
            this.getWorld().playSound(null, this.getX(), this.getY(), this.getZ(), sound, this.getSoundCategory(), volume, pitch);
        }
    }

    public void playSoundIfNotSilent(SoundEvent event) {
        if (!this.isSilent()) {
            this.playSound(event, 1.0f, 1.0f);
        }
    }

    public boolean isSilent() {
        return this.dataTracker.get(SILENT);
    }

    public void setSilent(boolean silent) {
        this.dataTracker.set(SILENT, silent);
    }

    public boolean hasNoGravity() {
        return this.dataTracker.get(NO_GRAVITY);
    }

    public void setNoGravity(boolean noGravity) {
        this.dataTracker.set(NO_GRAVITY, noGravity);
    }

    protected double getGravity() {
        return 0.0;
    }

    public final double getFinalGravity() {
        return this.hasNoGravity() ? 0.0 : this.getGravity();
    }

    protected void applyGravity() {
        double d = this.getFinalGravity();
        if (d != 0.0) {
            this.setVelocity(this.getVelocity().add(0.0, -d, 0.0));
        }
    }

    protected MoveEffect getMoveEffect() {
        return MoveEffect.ALL;
    }

    public boolean occludeVibrationSignals() {
        return false;
    }

    protected void fall(double heightDifference, boolean onGround, BlockState state, BlockPos landedPosition) {
        if (onGround) {
            if (this.fallDistance > 0.0f) {
                state.getBlock().onLandedUpon(this.getWorld(), state, landedPosition, this, this.fallDistance);
                this.getWorld().emitGameEvent(GameEvent.HIT_GROUND, this.pos, GameEvent.Emitter.of(this, this.supportingBlockPos.map(arg -> this.getWorld().getBlockState((BlockPos)arg)).orElse(state)));
            }
            this.onLanding();
        } else if (heightDifference < 0.0) {
            this.fallDistance -= (float)heightDifference;
        }
    }

    public boolean isFireImmune() {
        return this.getType().isFireImmune();
    }

    public boolean handleFallDamage(float fallDistance, float damageMultiplier, DamageSource damageSource) {
        if (this.type.isIn(EntityTypeTags.FALL_DAMAGE_IMMUNE)) {
            return false;
        }
        if (this.hasPassengers()) {
            for (Entity lv : this.getPassengerList()) {
                lv.handleFallDamage(fallDistance, damageMultiplier, damageSource);
            }
        }
        return false;
    }

    public boolean isTouchingWater() {
        return this.touchingWater;
    }

    private boolean isBeingRainedOn() {
        BlockPos lv = this.getBlockPos();
        return this.getWorld().hasRain(lv) || this.getWorld().hasRain(BlockPos.ofFloored(lv.getX(), this.getBoundingBox().maxY, lv.getZ()));
    }

    private boolean isInsideBubbleColumn() {
        return this.getBlockStateAtPos().isOf(Blocks.BUBBLE_COLUMN);
    }

    public boolean isTouchingWaterOrRain() {
        return this.isTouchingWater() || this.isBeingRainedOn();
    }

    public boolean isWet() {
        return this.isTouchingWater() || this.isBeingRainedOn() || this.isInsideBubbleColumn();
    }

    public boolean isInsideWaterOrBubbleColumn() {
        return this.isTouchingWater() || this.isInsideBubbleColumn();
    }

    public boolean isInFluid() {
        return this.isInsideWaterOrBubbleColumn() || this.isInLava();
    }

    public boolean isSubmergedInWater() {
        return this.submergedInWater && this.isTouchingWater();
    }

    public void updateSwimming() {
        if (this.isSwimming()) {
            this.setSwimming(this.isSprinting() && this.isTouchingWater() && !this.hasVehicle());
        } else {
            this.setSwimming(this.isSprinting() && this.isSubmergedInWater() && !this.hasVehicle() && this.getWorld().getFluidState(this.blockPos).isIn(FluidTags.WATER));
        }
    }

    protected boolean updateWaterState() {
        this.fluidHeight.clear();
        this.checkWaterState();
        double d = this.getWorld().getDimension().ultrawarm() ? 0.007 : 0.0023333333333333335;
        boolean bl = this.updateMovementInFluid(FluidTags.LAVA, d);
        return this.isTouchingWater() || bl;
    }

    void checkWaterState() {
        BoatEntity lv;
        Entity entity = this.getVehicle();
        if (entity instanceof BoatEntity && !(lv = (BoatEntity)entity).isSubmergedInWater()) {
            this.touchingWater = false;
        } else if (this.updateMovementInFluid(FluidTags.WATER, 0.014)) {
            if (!this.touchingWater && !this.firstUpdate) {
                this.onSwimmingStart();
            }
            this.onLanding();
            this.touchingWater = true;
            this.extinguish();
        } else {
            this.touchingWater = false;
        }
    }

    private void updateSubmergedInWaterState() {
        BoatEntity lv2;
        this.submergedInWater = this.isSubmergedIn(FluidTags.WATER);
        this.submergedFluidTag.clear();
        double d = this.getEyeY();
        Entity lv = this.getVehicle();
        if (lv instanceof BoatEntity && !(lv2 = (BoatEntity)lv).isSubmergedInWater() && lv2.getBoundingBox().maxY >= d && lv2.getBoundingBox().minY <= d) {
            return;
        }
        BlockPos lv3 = BlockPos.ofFloored(this.getX(), d, this.getZ());
        FluidState lv4 = this.getWorld().getFluidState(lv3);
        double e = (float)lv3.getY() + lv4.getHeight(this.getWorld(), lv3);
        if (e > d) {
            lv4.streamTags().forEach(this.submergedFluidTag::add);
        }
    }

    protected void onSwimmingStart() {
        double e;
        double d;
        Entity lv = Objects.requireNonNullElse(this.getControllingPassenger(), this);
        float f = lv == this ? 0.2f : 0.9f;
        Vec3d lv2 = lv.getVelocity();
        float g = Math.min(1.0f, (float)Math.sqrt(lv2.x * lv2.x * (double)0.2f + lv2.y * lv2.y + lv2.z * lv2.z * (double)0.2f) * f);
        if (g < 0.25f) {
            this.playSound(this.getSplashSound(), g, 1.0f + (this.random.nextFloat() - this.random.nextFloat()) * 0.4f);
        } else {
            this.playSound(this.getHighSpeedSplashSound(), g, 1.0f + (this.random.nextFloat() - this.random.nextFloat()) * 0.4f);
        }
        float h = MathHelper.floor(this.getY());
        int i = 0;
        while ((float)i < 1.0f + this.dimensions.width() * 20.0f) {
            d = (this.random.nextDouble() * 2.0 - 1.0) * (double)this.dimensions.width();
            e = (this.random.nextDouble() * 2.0 - 1.0) * (double)this.dimensions.width();
            this.getWorld().addParticle(ParticleTypes.BUBBLE, this.getX() + d, h + 1.0f, this.getZ() + e, lv2.x, lv2.y - this.random.nextDouble() * (double)0.2f, lv2.z);
            ++i;
        }
        i = 0;
        while ((float)i < 1.0f + this.dimensions.width() * 20.0f) {
            d = (this.random.nextDouble() * 2.0 - 1.0) * (double)this.dimensions.width();
            e = (this.random.nextDouble() * 2.0 - 1.0) * (double)this.dimensions.width();
            this.getWorld().addParticle(ParticleTypes.SPLASH, this.getX() + d, h + 1.0f, this.getZ() + e, lv2.x, lv2.y, lv2.z);
            ++i;
        }
        this.emitGameEvent(GameEvent.SPLASH);
    }

    @Deprecated
    protected BlockState getLandingBlockState() {
        return this.getWorld().getBlockState(this.getLandingPos());
    }

    public BlockState getSteppingBlockState() {
        return this.getWorld().getBlockState(this.getSteppingPos());
    }

    public boolean shouldSpawnSprintingParticles() {
        return this.isSprinting() && !this.isTouchingWater() && !this.isSpectator() && !this.isInSneakingPose() && !this.isInLava() && this.isAlive();
    }

    protected void spawnSprintingParticles() {
        BlockPos lv = this.getLandingPos();
        BlockState lv2 = this.getWorld().getBlockState(lv);
        if (lv2.getRenderType() != BlockRenderType.INVISIBLE) {
            Vec3d lv3 = this.getVelocity();
            BlockPos lv4 = this.getBlockPos();
            double d = this.getX() + (this.random.nextDouble() - 0.5) * (double)this.dimensions.width();
            double e = this.getZ() + (this.random.nextDouble() - 0.5) * (double)this.dimensions.width();
            if (lv4.getX() != lv.getX()) {
                d = MathHelper.clamp(d, (double)lv.getX(), (double)lv.getX() + 1.0);
            }
            if (lv4.getZ() != lv.getZ()) {
                e = MathHelper.clamp(e, (double)lv.getZ(), (double)lv.getZ() + 1.0);
            }
            this.getWorld().addParticle(new BlockStateParticleEffect(ParticleTypes.BLOCK, lv2), d, this.getY() + 0.1, e, lv3.x * -4.0, 1.5, lv3.z * -4.0);
        }
    }

    public boolean isSubmergedIn(TagKey<Fluid> fluidTag) {
        return this.submergedFluidTag.contains(fluidTag);
    }

    public boolean isInLava() {
        return !this.firstUpdate && this.fluidHeight.getDouble(FluidTags.LAVA) > 0.0;
    }

    public void updateVelocity(float speed, Vec3d movementInput) {
        Vec3d lv = Entity.movementInputToVelocity(movementInput, speed, this.getYaw());
        this.setVelocity(this.getVelocity().add(lv));
    }

    private static Vec3d movementInputToVelocity(Vec3d movementInput, float speed, float yaw) {
        double d = movementInput.lengthSquared();
        if (d < 1.0E-7) {
            return Vec3d.ZERO;
        }
        Vec3d lv = (d > 1.0 ? movementInput.normalize() : movementInput).multiply(speed);
        float h = MathHelper.sin(yaw * ((float)Math.PI / 180));
        float i = MathHelper.cos(yaw * ((float)Math.PI / 180));
        return new Vec3d(lv.x * (double)i - lv.z * (double)h, lv.y, lv.z * (double)i + lv.x * (double)h);
    }

    @Deprecated
    public float getBrightnessAtEyes() {
        if (this.getWorld().isPosLoaded(this.getBlockX(), this.getBlockZ())) {
            return this.getWorld().getBrightness(BlockPos.ofFloored(this.getX(), this.getEyeY(), this.getZ()));
        }
        return 0.0f;
    }

    public void updatePositionAndAngles(double x, double y, double z, float yaw, float pitch) {
        this.updatePosition(x, y, z);
        this.setAngles(yaw, pitch);
    }

    public void setAngles(float yaw, float pitch) {
        this.setYaw(yaw % 360.0f);
        this.setPitch(MathHelper.clamp(pitch, -90.0f, 90.0f) % 360.0f);
        this.prevYaw = this.getYaw();
        this.prevPitch = this.getPitch();
    }

    public void updatePosition(double x, double y, double z) {
        double g = MathHelper.clamp(x, -3.0E7, 3.0E7);
        double h = MathHelper.clamp(z, -3.0E7, 3.0E7);
        this.prevX = g;
        this.prevY = y;
        this.prevZ = h;
        this.setPosition(g, y, h);
    }

    public void refreshPositionAfterTeleport(Vec3d pos) {
        this.refreshPositionAfterTeleport(pos.x, pos.y, pos.z);
    }

    public void refreshPositionAfterTeleport(double x, double y, double z) {
        this.refreshPositionAndAngles(x, y, z, this.getYaw(), this.getPitch());
    }

    public void refreshPositionAndAngles(BlockPos pos, float yaw, float pitch) {
        this.refreshPositionAndAngles(pos.toBottomCenterPos(), yaw, pitch);
    }

    public void refreshPositionAndAngles(Vec3d pos, float yaw, float pitch) {
        this.refreshPositionAndAngles(pos.x, pos.y, pos.z, yaw, pitch);
    }

    public void refreshPositionAndAngles(double x, double y, double z, float yaw, float pitch) {
        this.setPos(x, y, z);
        this.setYaw(yaw);
        this.setPitch(pitch);
        this.resetPosition();
        this.refreshPosition();
    }

    public final void resetPosition() {
        double d = this.getX();
        double e = this.getY();
        double f = this.getZ();
        this.prevX = d;
        this.prevY = e;
        this.prevZ = f;
        this.lastRenderX = d;
        this.lastRenderY = e;
        this.lastRenderZ = f;
        this.prevYaw = this.getYaw();
        this.prevPitch = this.getPitch();
    }

    public float distanceTo(Entity entity) {
        float f = (float)(this.getX() - entity.getX());
        float g = (float)(this.getY() - entity.getY());
        float h = (float)(this.getZ() - entity.getZ());
        return MathHelper.sqrt(f * f + g * g + h * h);
    }

    public double squaredDistanceTo(double x, double y, double z) {
        double g = this.getX() - x;
        double h = this.getY() - y;
        double i = this.getZ() - z;
        return g * g + h * h + i * i;
    }

    public double squaredDistanceTo(Entity entity) {
        return this.squaredDistanceTo(entity.getPos());
    }

    public double squaredDistanceTo(Vec3d vector) {
        double d = this.getX() - vector.x;
        double e = this.getY() - vector.y;
        double f = this.getZ() - vector.z;
        return d * d + e * e + f * f;
    }

    public void onPlayerCollision(PlayerEntity player) {
    }

    public void pushAwayFrom(Entity entity) {
        double e;
        if (this.isConnectedThroughVehicle(entity)) {
            return;
        }
        if (entity.noClip || this.noClip) {
            return;
        }
        double d = entity.getX() - this.getX();
        double f = MathHelper.absMax(d, e = entity.getZ() - this.getZ());
        if (f >= (double)0.01f) {
            f = Math.sqrt(f);
            d /= f;
            e /= f;
            double g = 1.0 / f;
            if (g > 1.0) {
                g = 1.0;
            }
            d *= g;
            e *= g;
            d *= (double)0.05f;
            e *= (double)0.05f;
            if (!this.hasPassengers() && this.isPushable()) {
                this.addVelocity(-d, 0.0, -e);
            }
            if (!entity.hasPassengers() && entity.isPushable()) {
                entity.addVelocity(d, 0.0, e);
            }
        }
    }

    public void addVelocity(Vec3d velocity) {
        this.addVelocity(velocity.x, velocity.y, velocity.z);
    }

    public void addVelocity(double deltaX, double deltaY, double deltaZ) {
        this.setVelocity(this.getVelocity().add(deltaX, deltaY, deltaZ));
        this.velocityDirty = true;
    }

    protected void scheduleVelocityUpdate() {
        this.velocityModified = true;
    }

    public boolean damage(DamageSource source, float amount) {
        if (this.isInvulnerableTo(source)) {
            return false;
        }
        this.scheduleVelocityUpdate();
        return false;
    }

    public final Vec3d getRotationVec(float tickDelta) {
        return this.getRotationVector(this.getPitch(tickDelta), this.getYaw(tickDelta));
    }

    public Direction getFacing() {
        return Direction.getFacing(this.getRotationVec(1.0f));
    }

    public float getPitch(float tickDelta) {
        if (tickDelta == 1.0f) {
            return this.getPitch();
        }
        return MathHelper.lerp(tickDelta, this.prevPitch, this.getPitch());
    }

    public float getYaw(float tickDelta) {
        if (tickDelta == 1.0f) {
            return this.getYaw();
        }
        return MathHelper.lerp(tickDelta, this.prevYaw, this.getYaw());
    }

    public final Vec3d getRotationVector(float pitch, float yaw) {
        float h = pitch * ((float)Math.PI / 180);
        float i = -yaw * ((float)Math.PI / 180);
        float j = MathHelper.cos(i);
        float k = MathHelper.sin(i);
        float l = MathHelper.cos(h);
        float m = MathHelper.sin(h);
        return new Vec3d(k * l, -m, j * l);
    }

    public final Vec3d getOppositeRotationVector(float tickDelta) {
        return this.getOppositeRotationVector(this.getPitch(tickDelta), this.getYaw(tickDelta));
    }

    protected final Vec3d getOppositeRotationVector(float pitch, float yaw) {
        return this.getRotationVector(pitch - 90.0f, yaw);
    }

    public final Vec3d getEyePos() {
        return new Vec3d(this.getX(), this.getEyeY(), this.getZ());
    }

    public final Vec3d getCameraPosVec(float tickDelta) {
        double d = MathHelper.lerp((double)tickDelta, this.prevX, this.getX());
        double e = MathHelper.lerp((double)tickDelta, this.prevY, this.getY()) + (double)this.getStandingEyeHeight();
        double g = MathHelper.lerp((double)tickDelta, this.prevZ, this.getZ());
        return new Vec3d(d, e, g);
    }

    public Vec3d getClientCameraPosVec(float tickDelta) {
        return this.getCameraPosVec(tickDelta);
    }

    public final Vec3d getLerpedPos(float delta) {
        double d = MathHelper.lerp((double)delta, this.prevX, this.getX());
        double e = MathHelper.lerp((double)delta, this.prevY, this.getY());
        double g = MathHelper.lerp((double)delta, this.prevZ, this.getZ());
        return new Vec3d(d, e, g);
    }

    public HitResult raycast(double maxDistance, float tickDelta, boolean includeFluids) {
        Vec3d lv = this.getCameraPosVec(tickDelta);
        Vec3d lv2 = this.getRotationVec(tickDelta);
        Vec3d lv3 = lv.add(lv2.x * maxDistance, lv2.y * maxDistance, lv2.z * maxDistance);
        return this.getWorld().raycast(new RaycastContext(lv, lv3, RaycastContext.ShapeType.OUTLINE, includeFluids ? RaycastContext.FluidHandling.ANY : RaycastContext.FluidHandling.NONE, this));
    }

    public boolean canBeHitByProjectile() {
        return this.isAlive() && this.canHit();
    }

    public boolean canHit() {
        return false;
    }

    public boolean isPushable() {
        return false;
    }

    public void updateKilledAdvancementCriterion(Entity entityKilled, int score, DamageSource damageSource) {
        if (entityKilled instanceof ServerPlayerEntity) {
            Criteria.ENTITY_KILLED_PLAYER.trigger((ServerPlayerEntity)entityKilled, this, damageSource);
        }
    }

    public boolean shouldRender(double cameraX, double cameraY, double cameraZ) {
        double g = this.getX() - cameraX;
        double h = this.getY() - cameraY;
        double i = this.getZ() - cameraZ;
        double j = g * g + h * h + i * i;
        return this.shouldRender(j);
    }

    public boolean shouldRender(double distance) {
        double e = this.getBoundingBox().getAverageSideLength();
        if (Double.isNaN(e)) {
            e = 1.0;
        }
        return distance < (e *= 64.0 * renderDistanceMultiplier) * e;
    }

    public boolean saveSelfNbt(NbtCompound nbt) {
        if (this.removalReason != null && !this.removalReason.shouldSave()) {
            return false;
        }
        String string = this.getSavedEntityId();
        if (string == null) {
            return false;
        }
        nbt.putString(ID_KEY, string);
        this.writeNbt(nbt);
        return true;
    }

    public boolean saveNbt(NbtCompound nbt) {
        if (this.hasVehicle()) {
            return false;
        }
        return this.saveSelfNbt(nbt);
    }

    public NbtCompound writeNbt(NbtCompound nbt) {
        try {
            NbtList lv3;
            int i;
            if (this.vehicle != null) {
                nbt.put("Pos", this.toNbtList(this.vehicle.getX(), this.getY(), this.vehicle.getZ()));
            } else {
                nbt.put("Pos", this.toNbtList(this.getX(), this.getY(), this.getZ()));
            }
            Vec3d lv = this.getVelocity();
            nbt.put("Motion", this.toNbtList(lv.x, lv.y, lv.z));
            nbt.put("Rotation", this.toNbtList(this.getYaw(), this.getPitch()));
            nbt.putFloat("FallDistance", this.fallDistance);
            nbt.putShort("Fire", (short)this.fireTicks);
            nbt.putShort("Air", (short)this.getAir());
            nbt.putBoolean("OnGround", this.isOnGround());
            nbt.putBoolean("Invulnerable", this.invulnerable);
            nbt.putInt("PortalCooldown", this.portalCooldown);
            nbt.putUuid(UUID_KEY, this.getUuid());
            Text lv2 = this.getCustomName();
            if (lv2 != null) {
                nbt.putString("CustomName", Text.Serialization.toJsonString(lv2, this.getRegistryManager()));
            }
            if (this.isCustomNameVisible()) {
                nbt.putBoolean("CustomNameVisible", this.isCustomNameVisible());
            }
            if (this.isSilent()) {
                nbt.putBoolean("Silent", this.isSilent());
            }
            if (this.hasNoGravity()) {
                nbt.putBoolean("NoGravity", this.hasNoGravity());
            }
            if (this.glowing) {
                nbt.putBoolean("Glowing", true);
            }
            if ((i = this.getFrozenTicks()) > 0) {
                nbt.putInt("TicksFrozen", this.getFrozenTicks());
            }
            if (this.hasVisualFire) {
                nbt.putBoolean("HasVisualFire", this.hasVisualFire);
            }
            if (!this.commandTags.isEmpty()) {
                lv3 = new NbtList();
                for (String string : this.commandTags) {
                    lv3.add(NbtString.of(string));
                }
                nbt.put("Tags", lv3);
            }
            this.writeCustomDataToNbt(nbt);
            if (this.hasPassengers()) {
                lv3 = new NbtList();
                for (Entity lv4 : this.getPassengerList()) {
                    NbtCompound lv5;
                    if (!lv4.saveSelfNbt(lv5 = new NbtCompound())) continue;
                    lv3.add(lv5);
                }
                if (!lv3.isEmpty()) {
                    nbt.put(PASSENGERS_KEY, lv3);
                }
            }
        } catch (Throwable throwable) {
            CrashReport lv6 = CrashReport.create(throwable, "Saving entity NBT");
            CrashReportSection lv7 = lv6.addElement("Entity being saved");
            this.populateCrashReport(lv7);
            throw new CrashException(lv6);
        }
        return nbt;
    }

    public void readNbt(NbtCompound nbt) {
        try {
            NbtList lv = nbt.getList("Pos", NbtElement.DOUBLE_TYPE);
            NbtList lv2 = nbt.getList("Motion", NbtElement.DOUBLE_TYPE);
            NbtList lv3 = nbt.getList("Rotation", NbtElement.FLOAT_TYPE);
            double d = lv2.getDouble(0);
            double e = lv2.getDouble(1);
            double f = lv2.getDouble(2);
            this.setVelocity(Math.abs(d) > 10.0 ? 0.0 : d, Math.abs(e) > 10.0 ? 0.0 : e, Math.abs(f) > 10.0 ? 0.0 : f);
            double g = 3.0000512E7;
            this.setPos(MathHelper.clamp(lv.getDouble(0), -3.0000512E7, 3.0000512E7), MathHelper.clamp(lv.getDouble(1), -2.0E7, 2.0E7), MathHelper.clamp(lv.getDouble(2), -3.0000512E7, 3.0000512E7));
            this.setYaw(lv3.getFloat(0));
            this.setPitch(lv3.getFloat(1));
            this.resetPosition();
            this.setHeadYaw(this.getYaw());
            this.setBodyYaw(this.getYaw());
            this.fallDistance = nbt.getFloat("FallDistance");
            this.fireTicks = nbt.getShort("Fire");
            if (nbt.contains("Air")) {
                this.setAir(nbt.getShort("Air"));
            }
            this.onGround = nbt.getBoolean("OnGround");
            this.invulnerable = nbt.getBoolean("Invulnerable");
            this.portalCooldown = nbt.getInt("PortalCooldown");
            if (nbt.containsUuid(UUID_KEY)) {
                this.uuid = nbt.getUuid(UUID_KEY);
                this.uuidString = this.uuid.toString();
            }
            if (!(Double.isFinite(this.getX()) && Double.isFinite(this.getY()) && Double.isFinite(this.getZ()))) {
                throw new IllegalStateException("Entity has invalid position");
            }
            if (!Double.isFinite(this.getYaw()) || !Double.isFinite(this.getPitch())) {
                throw new IllegalStateException("Entity has invalid rotation");
            }
            this.refreshPosition();
            this.setRotation(this.getYaw(), this.getPitch());
            if (nbt.contains("CustomName", NbtElement.STRING_TYPE)) {
                String string = nbt.getString("CustomName");
                try {
                    this.setCustomName(Text.Serialization.fromJson(string, (RegistryWrapper.WrapperLookup)this.getRegistryManager()));
                } catch (Exception exception) {
                    LOGGER.warn("Failed to parse entity custom name {}", (Object)string, (Object)exception);
                }
            }
            this.setCustomNameVisible(nbt.getBoolean("CustomNameVisible"));
            this.setSilent(nbt.getBoolean("Silent"));
            this.setNoGravity(nbt.getBoolean("NoGravity"));
            this.setGlowing(nbt.getBoolean("Glowing"));
            this.setFrozenTicks(nbt.getInt("TicksFrozen"));
            this.hasVisualFire = nbt.getBoolean("HasVisualFire");
            if (nbt.contains("Tags", NbtElement.LIST_TYPE)) {
                this.commandTags.clear();
                NbtList lv4 = nbt.getList("Tags", NbtElement.STRING_TYPE);
                int i = Math.min(lv4.size(), 1024);
                for (int j = 0; j < i; ++j) {
                    this.commandTags.add(lv4.getString(j));
                }
            }
            this.readCustomDataFromNbt(nbt);
            if (this.shouldSetPositionOnLoad()) {
                this.refreshPosition();
            }
        } catch (Throwable throwable) {
            CrashReport lv5 = CrashReport.create(throwable, "Loading entity NBT");
            CrashReportSection lv6 = lv5.addElement("Entity being loaded");
            this.populateCrashReport(lv6);
            throw new CrashException(lv5);
        }
    }

    protected boolean shouldSetPositionOnLoad() {
        return true;
    }

    @Nullable
    protected final String getSavedEntityId() {
        EntityType<?> lv = this.getType();
        Identifier lv2 = EntityType.getId(lv);
        return !lv.isSaveable() || lv2 == null ? null : lv2.toString();
    }

    protected abstract void readCustomDataFromNbt(NbtCompound var1);

    protected abstract void writeCustomDataToNbt(NbtCompound var1);

    protected NbtList toNbtList(double ... values) {
        NbtList lv = new NbtList();
        for (double d : values) {
            lv.add(NbtDouble.of(d));
        }
        return lv;
    }

    protected NbtList toNbtList(float ... values) {
        NbtList lv = new NbtList();
        for (float f : values) {
            lv.add(NbtFloat.of(f));
        }
        return lv;
    }

    @Nullable
    public ItemEntity dropItem(ItemConvertible item) {
        return this.dropItem(item, 0);
    }

    @Nullable
    public ItemEntity dropItem(ItemConvertible item, int yOffset) {
        return this.dropStack(new ItemStack(item), yOffset);
    }

    @Nullable
    public ItemEntity dropStack(ItemStack stack) {
        return this.dropStack(stack, 0.0f);
    }

    @Nullable
    public ItemEntity dropStack(ItemStack stack, float yOffset) {
        if (stack.isEmpty()) {
            return null;
        }
        if (this.getWorld().isClient) {
            return null;
        }
        ItemEntity lv = new ItemEntity(this.getWorld(), this.getX(), this.getY() + (double)yOffset, this.getZ(), stack);
        lv.setToDefaultPickupDelay();
        this.getWorld().spawnEntity(lv);
        return lv;
    }

    public boolean isAlive() {
        return !this.isRemoved();
    }

    public boolean isInsideWall() {
        if (this.noClip) {
            return false;
        }
        float f = this.dimensions.width() * 0.8f;
        Box lv = Box.of(this.getEyePos(), f, 1.0E-6, f);
        return BlockPos.stream(lv).anyMatch(pos -> {
            BlockState lv = this.getWorld().getBlockState((BlockPos)pos);
            return !lv.isAir() && lv.shouldSuffocate(this.getWorld(), (BlockPos)pos) && VoxelShapes.matchesAnywhere(lv.getCollisionShape(this.getWorld(), (BlockPos)pos).offset(pos.getX(), pos.getY(), pos.getZ()), VoxelShapes.cuboid(lv), BooleanBiFunction.AND);
        });
    }

    public ActionResult interact(PlayerEntity player, Hand hand) {
        Entity entity;
        if (this.isAlive() && (entity = this) instanceof Leashable) {
            Leashable lv = (Leashable)((Object)entity);
            if (lv.getLeashHolder() == player) {
                if (!this.getWorld().isClient()) {
                    lv.detachLeash(true, !player.isInCreativeMode());
                    this.emitGameEvent(GameEvent.ENTITY_INTERACT, player);
                }
                return ActionResult.success(this.getWorld().isClient);
            }
            ItemStack lv2 = player.getStackInHand(hand);
            if (lv2.isOf(Items.LEAD) && lv.canLeashAttachTo()) {
                if (!this.getWorld().isClient()) {
                    lv.attachLeash(player, true);
                }
                lv2.decrement(1);
                return ActionResult.success(this.getWorld().isClient);
            }
        }
        return ActionResult.PASS;
    }

    public boolean collidesWith(Entity other) {
        return other.isCollidable() && !this.isConnectedThroughVehicle(other);
    }

    public boolean isCollidable() {
        return false;
    }

    public void tickRiding() {
        this.setVelocity(Vec3d.ZERO);
        this.tick();
        if (!this.hasVehicle()) {
            return;
        }
        this.getVehicle().updatePassengerPosition(this);
    }

    public final void updatePassengerPosition(Entity passenger) {
        if (!this.hasPassenger(passenger)) {
            return;
        }
        this.updatePassengerPosition(passenger, Entity::setPosition);
    }

    protected void updatePassengerPosition(Entity passenger, PositionUpdater positionUpdater) {
        Vec3d lv = this.getPassengerRidingPos(passenger);
        Vec3d lv2 = passenger.getVehicleAttachmentPos(this);
        positionUpdater.accept(passenger, lv.x - lv2.x, lv.y - lv2.y, lv.z - lv2.z);
    }

    public void onPassengerLookAround(Entity passenger) {
    }

    public Vec3d getVehicleAttachmentPos(Entity vehicle) {
        return this.getAttachments().getPoint(EntityAttachmentType.VEHICLE, 0, this.yaw);
    }

    public Vec3d getPassengerRidingPos(Entity passenger) {
        return this.getPos().add(this.getPassengerAttachmentPos(passenger, this.dimensions, 1.0f));
    }

    protected Vec3d getPassengerAttachmentPos(Entity passenger, EntityDimensions dimensions, float scaleFactor) {
        return Entity.getPassengerAttachmentPos(this, passenger, dimensions.attachments());
    }

    protected static Vec3d getPassengerAttachmentPos(Entity vehicle, Entity passenger, EntityAttachments attachments) {
        int i = vehicle.getPassengerList().indexOf(passenger);
        return attachments.getPointOrDefault(EntityAttachmentType.PASSENGER, i, vehicle.yaw);
    }

    public boolean startRiding(Entity entity) {
        return this.startRiding(entity, false);
    }

    public boolean isLiving() {
        return this instanceof LivingEntity;
    }

    public boolean startRiding(Entity entity, boolean force) {
        if (entity == this.vehicle) {
            return false;
        }
        if (!entity.couldAcceptPassenger()) {
            return false;
        }
        Entity lv = entity;
        while (lv.vehicle != null) {
            if (lv.vehicle == this) {
                return false;
            }
            lv = lv.vehicle;
        }
        if (!(force || this.canStartRiding(entity) && entity.canAddPassenger(this))) {
            return false;
        }
        if (this.hasVehicle()) {
            this.stopRiding();
        }
        this.setPose(EntityPose.STANDING);
        this.vehicle = entity;
        this.vehicle.addPassenger(this);
        entity.streamIntoPassengers().filter(passenger -> passenger instanceof ServerPlayerEntity).forEach(player -> Criteria.STARTED_RIDING.trigger((ServerPlayerEntity)player));
        return true;
    }

    protected boolean canStartRiding(Entity entity) {
        return !this.isSneaking() && this.ridingCooldown <= 0;
    }

    public void removeAllPassengers() {
        for (int i = this.passengerList.size() - 1; i >= 0; --i) {
            ((Entity)this.passengerList.get(i)).stopRiding();
        }
    }

    public void dismountVehicle() {
        if (this.vehicle != null) {
            Entity lv = this.vehicle;
            this.vehicle = null;
            lv.removePassenger(this);
        }
    }

    public void stopRiding() {
        this.dismountVehicle();
    }

    protected void addPassenger(Entity passenger) {
        if (passenger.getVehicle() != this) {
            throw new IllegalStateException("Use x.startRiding(y), not y.addPassenger(x)");
        }
        if (this.passengerList.isEmpty()) {
            this.passengerList = ImmutableList.of(passenger);
        } else {
            ArrayList<Entity> list = Lists.newArrayList(this.passengerList);
            if (!this.getWorld().isClient && passenger instanceof PlayerEntity && !(this.getFirstPassenger() instanceof PlayerEntity)) {
                list.add(0, passenger);
            } else {
                list.add(passenger);
            }
            this.passengerList = ImmutableList.copyOf(list);
        }
        this.emitGameEvent(GameEvent.ENTITY_MOUNT, passenger);
    }

    protected void removePassenger(Entity passenger) {
        if (passenger.getVehicle() == this) {
            throw new IllegalStateException("Use x.stopRiding(y), not y.removePassenger(x)");
        }
        this.passengerList = this.passengerList.size() == 1 && this.passengerList.get(0) == passenger ? ImmutableList.of() : this.passengerList.stream().filter(entity -> entity != passenger).collect(ImmutableList.toImmutableList());
        passenger.ridingCooldown = 60;
        this.emitGameEvent(GameEvent.ENTITY_DISMOUNT, passenger);
    }

    protected boolean canAddPassenger(Entity passenger) {
        return this.passengerList.isEmpty();
    }

    protected boolean couldAcceptPassenger() {
        return true;
    }

    public void updateTrackedPositionAndAngles(double x, double y, double z, float yaw, float pitch, int interpolationSteps) {
        this.setPosition(x, y, z);
        this.setRotation(yaw, pitch);
    }

    public double getLerpTargetX() {
        return this.getX();
    }

    public double getLerpTargetY() {
        return this.getY();
    }

    public double getLerpTargetZ() {
        return this.getZ();
    }

    public float getLerpTargetPitch() {
        return this.getPitch();
    }

    public float getLerpTargetYaw() {
        return this.getYaw();
    }

    public void updateTrackedHeadRotation(float yaw, int interpolationSteps) {
        this.setHeadYaw(yaw);
    }

    public float getTargetingMargin() {
        return 0.0f;
    }

    public Vec3d getRotationVector() {
        return this.getRotationVector(this.getPitch(), this.getYaw());
    }

    public Vec3d getHandPosOffset(Item item) {
        Entity entity = this;
        if (entity instanceof PlayerEntity) {
            PlayerEntity lv = (PlayerEntity)entity;
            boolean bl = lv.getOffHandStack().isOf(item) && !lv.getMainHandStack().isOf(item);
            Arm lv2 = bl ? lv.getMainArm().getOpposite() : lv.getMainArm();
            return this.getRotationVector(0.0f, this.getYaw() + (float)(lv2 == Arm.RIGHT ? 80 : -80)).multiply(0.5);
        }
        return Vec3d.ZERO;
    }

    public Vec2f getRotationClient() {
        return new Vec2f(this.getPitch(), this.getYaw());
    }

    public Vec3d getRotationVecClient() {
        return Vec3d.fromPolar(this.getRotationClient());
    }

    public void tryUsePortal(Portal portal, BlockPos pos) {
        if (this.hasPortalCooldown()) {
            this.resetPortalCooldown();
            return;
        }
        if (this.portalManager == null || !this.portalManager.portalMatches(portal)) {
            this.portalManager = new PortalManager(portal, pos.toImmutable());
        } else {
            this.portalManager.setPortalPos(pos.toImmutable());
            this.portalManager.setInPortal(true);
        }
    }

    protected void tickPortalTeleportation() {
        World world = this.getWorld();
        if (!(world instanceof ServerWorld)) {
            return;
        }
        ServerWorld lv = (ServerWorld)world;
        this.tickPortalCooldown();
        if (this.portalManager == null) {
            return;
        }
        if (this.portalManager.tick(lv, this, this.canUsePortals(false))) {
            lv.getProfiler().push("portal");
            this.resetPortalCooldown();
            TeleportTarget lv2 = this.portalManager.createTeleportTarget(lv, this);
            if (lv2 != null) {
                ServerWorld lv3 = lv2.world();
                if (lv.getServer().isWorldAllowed(lv3) && (lv3.getRegistryKey() == lv.getRegistryKey() || this.canTeleportBetween(lv, lv3))) {
                    this.teleportTo(lv2);
                }
            }
            lv.getProfiler().pop();
        } else if (this.portalManager.hasExpired()) {
            this.portalManager = null;
        }
    }

    public int getDefaultPortalCooldown() {
        Entity lv = this.getFirstPassenger();
        return lv instanceof ServerPlayerEntity ? lv.getDefaultPortalCooldown() : 300;
    }

    public void setVelocityClient(double x, double y, double z) {
        this.setVelocity(x, y, z);
    }

    public void onDamaged(DamageSource damageSource) {
    }

    public void handleStatus(byte status) {
        switch (status) {
            case 53: {
                HoneyBlock.addRegularParticles(this);
            }
        }
    }

    public void animateDamage(float yaw) {
    }

    public boolean isOnFire() {
        boolean bl = this.getWorld() != null && this.getWorld().isClient;
        return !this.isFireImmune() && (this.fireTicks > 0 || bl && this.getFlag(ON_FIRE_FLAG_INDEX));
    }

    public boolean hasVehicle() {
        return this.getVehicle() != null;
    }

    public boolean hasPassengers() {
        return !this.passengerList.isEmpty();
    }

    public boolean shouldDismountUnderwater() {
        return this.getType().isIn(EntityTypeTags.DISMOUNTS_UNDERWATER);
    }

    public boolean shouldControlVehicles() {
        return !this.getType().isIn(EntityTypeTags.NON_CONTROLLING_RIDER);
    }

    public void setSneaking(boolean sneaking) {
        this.setFlag(SNEAKING_FLAG_INDEX, sneaking);
    }

    public boolean isSneaking() {
        return this.getFlag(SNEAKING_FLAG_INDEX);
    }

    public boolean bypassesSteppingEffects() {
        return this.isSneaking();
    }

    public boolean bypassesLandingEffects() {
        return this.isSneaking();
    }

    public boolean isSneaky() {
        return this.isSneaking();
    }

    public boolean isDescending() {
        return this.isSneaking();
    }

    public boolean isInSneakingPose() {
        return this.isInPose(EntityPose.CROUCHING);
    }

    public boolean isSprinting() {
        return this.getFlag(SPRINTING_FLAG_INDEX);
    }

    public void setSprinting(boolean sprinting) {
        this.setFlag(SPRINTING_FLAG_INDEX, sprinting);
    }

    public boolean isSwimming() {
        return this.getFlag(SWIMMING_FLAG_INDEX);
    }

    public boolean isInSwimmingPose() {
        return this.isInPose(EntityPose.SWIMMING);
    }

    public boolean isCrawling() {
        return this.isInSwimmingPose() && !this.isTouchingWater();
    }

    public void setSwimming(boolean swimming) {
        this.setFlag(SWIMMING_FLAG_INDEX, swimming);
    }

    public final boolean isGlowingLocal() {
        return this.glowing;
    }

    public final void setGlowing(boolean glowing) {
        this.glowing = glowing;
        this.setFlag(GLOWING_FLAG_INDEX, this.isGlowing());
    }

    public boolean isGlowing() {
        if (this.getWorld().isClient()) {
            return this.getFlag(GLOWING_FLAG_INDEX);
        }
        return this.glowing;
    }

    public boolean isInvisible() {
        return this.getFlag(INVISIBLE_FLAG_INDEX);
    }

    public boolean isInvisibleTo(PlayerEntity player) {
        if (player.isSpectator()) {
            return false;
        }
        Team lv = this.getScoreboardTeam();
        if (lv != null && player != null && player.getScoreboardTeam() == lv && ((AbstractTeam)lv).shouldShowFriendlyInvisibles()) {
            return false;
        }
        return this.isInvisible();
    }

    public boolean isOnRail() {
        return false;
    }

    public void updateEventHandler(BiConsumer<EntityGameEventHandler<?>, ServerWorld> callback) {
    }

    @Nullable
    public Team getScoreboardTeam() {
        return this.getWorld().getScoreboard().getScoreHolderTeam(this.getNameForScoreboard());
    }

    public boolean isTeammate(Entity other) {
        return this.isTeamPlayer(other.getScoreboardTeam());
    }

    public boolean isTeamPlayer(AbstractTeam team) {
        if (this.getScoreboardTeam() != null) {
            return this.getScoreboardTeam().isEqual(team);
        }
        return false;
    }

    public void setInvisible(boolean invisible) {
        this.setFlag(INVISIBLE_FLAG_INDEX, invisible);
    }

    protected boolean getFlag(int index) {
        return (this.dataTracker.get(FLAGS) & 1 << index) != 0;
    }

    protected void setFlag(int index, boolean value) {
        byte b = this.dataTracker.get(FLAGS);
        if (value) {
            this.dataTracker.set(FLAGS, (byte)(b | 1 << index));
        } else {
            this.dataTracker.set(FLAGS, (byte)(b & ~(1 << index)));
        }
    }

    public int getMaxAir() {
        return 300;
    }

    public int getAir() {
        return this.dataTracker.get(AIR);
    }

    public void setAir(int air) {
        this.dataTracker.set(AIR, air);
    }

    public int getFrozenTicks() {
        return this.dataTracker.get(FROZEN_TICKS);
    }

    public void setFrozenTicks(int frozenTicks) {
        this.dataTracker.set(FROZEN_TICKS, frozenTicks);
    }

    public float getFreezingScale() {
        int i = this.getMinFreezeDamageTicks();
        return (float)Math.min(this.getFrozenTicks(), i) / (float)i;
    }

    public boolean isFrozen() {
        return this.getFrozenTicks() >= this.getMinFreezeDamageTicks();
    }

    public int getMinFreezeDamageTicks() {
        return 140;
    }

    public void onStruckByLightning(ServerWorld world, LightningEntity lightning) {
        this.setFireTicks(this.fireTicks + 1);
        if (this.fireTicks == 0) {
            this.setOnFireFor(8.0f);
        }
        this.damage(this.getDamageSources().lightningBolt(), 5.0f);
    }

    public void onBubbleColumnSurfaceCollision(boolean drag) {
        Vec3d lv = this.getVelocity();
        double d = drag ? Math.max(-0.9, lv.y - 0.03) : Math.min(1.8, lv.y + 0.1);
        this.setVelocity(lv.x, d, lv.z);
    }

    public void onBubbleColumnCollision(boolean drag) {
        Vec3d lv = this.getVelocity();
        double d = drag ? Math.max(-0.3, lv.y - 0.03) : Math.min(0.7, lv.y + 0.06);
        this.setVelocity(lv.x, d, lv.z);
        this.onLanding();
    }

    public boolean onKilledOther(ServerWorld world, LivingEntity other) {
        return true;
    }

    public void limitFallDistance() {
        if (this.getVelocity().getY() > -0.5 && this.fallDistance > 1.0f) {
            this.fallDistance = 1.0f;
        }
    }

    public void onLanding() {
        this.fallDistance = 0.0f;
    }

    protected void pushOutOfBlocks(double x, double y, double z) {
        BlockPos lv = BlockPos.ofFloored(x, y, z);
        Vec3d lv2 = new Vec3d(x - (double)lv.getX(), y - (double)lv.getY(), z - (double)lv.getZ());
        BlockPos.Mutable lv3 = new BlockPos.Mutable();
        Direction lv4 = Direction.UP;
        double g = Double.MAX_VALUE;
        for (Direction lv5 : new Direction[]{Direction.NORTH, Direction.SOUTH, Direction.WEST, Direction.EAST, Direction.UP}) {
            double i;
            lv3.set((Vec3i)lv, lv5);
            if (this.getWorld().getBlockState(lv3).isFullCube(this.getWorld(), lv3)) continue;
            double h = lv2.getComponentAlongAxis(lv5.getAxis());
            double d = i = lv5.getDirection() == Direction.AxisDirection.POSITIVE ? 1.0 - h : h;
            if (!(i < g)) continue;
            g = i;
            lv4 = lv5;
        }
        float j = this.random.nextFloat() * 0.2f + 0.1f;
        float k = lv4.getDirection().offset();
        Vec3d lv6 = this.getVelocity().multiply(0.75);
        if (lv4.getAxis() == Direction.Axis.X) {
            this.setVelocity(k * j, lv6.y, lv6.z);
        } else if (lv4.getAxis() == Direction.Axis.Y) {
            this.setVelocity(lv6.x, k * j, lv6.z);
        } else if (lv4.getAxis() == Direction.Axis.Z) {
            this.setVelocity(lv6.x, lv6.y, k * j);
        }
    }

    public void slowMovement(BlockState state, Vec3d multiplier) {
        this.onLanding();
        this.movementMultiplier = multiplier;
    }

    private static Text removeClickEvents(Text textComponent) {
        MutableText lv = textComponent.copyContentOnly().setStyle(textComponent.getStyle().withClickEvent(null));
        for (Text lv2 : textComponent.getSiblings()) {
            lv.append(Entity.removeClickEvents(lv2));
        }
        return lv;
    }

    @Override
    public Text getName() {
        Text lv = this.getCustomName();
        if (lv != null) {
            return Entity.removeClickEvents(lv);
        }
        return this.getDefaultName();
    }

    protected Text getDefaultName() {
        return this.type.getName();
    }

    public boolean isPartOf(Entity entity) {
        return this == entity;
    }

    public float getHeadYaw() {
        return 0.0f;
    }

    public void setHeadYaw(float headYaw) {
    }

    public void setBodyYaw(float bodyYaw) {
    }

    public boolean isAttackable() {
        return true;
    }

    public boolean handleAttack(Entity attacker) {
        return false;
    }

    public String toString() {
        String string;
        String string2 = string = this.getWorld() == null ? "~NULL~" : this.getWorld().toString();
        if (this.removalReason != null) {
            return String.format(Locale.ROOT, "%s['%s'/%d, l='%s', x=%.2f, y=%.2f, z=%.2f, removed=%s]", new Object[]{this.getClass().getSimpleName(), this.getName().getString(), this.id, string, this.getX(), this.getY(), this.getZ(), this.removalReason});
        }
        return String.format(Locale.ROOT, "%s['%s'/%d, l='%s', x=%.2f, y=%.2f, z=%.2f]", this.getClass().getSimpleName(), this.getName().getString(), this.id, string, this.getX(), this.getY(), this.getZ());
    }

    public boolean isInvulnerableTo(DamageSource damageSource) {
        return this.isRemoved() || this.invulnerable && !damageSource.isIn(DamageTypeTags.BYPASSES_INVULNERABILITY) && !damageSource.isSourceCreativePlayer() || damageSource.isIn(DamageTypeTags.IS_FIRE) && this.isFireImmune() || damageSource.isIn(DamageTypeTags.IS_FALL) && this.getType().isIn(EntityTypeTags.FALL_DAMAGE_IMMUNE);
    }

    public boolean isInvulnerable() {
        return this.invulnerable;
    }

    public void setInvulnerable(boolean invulnerable) {
        this.invulnerable = invulnerable;
    }

    public void copyPositionAndRotation(Entity entity) {
        this.refreshPositionAndAngles(entity.getX(), entity.getY(), entity.getZ(), entity.getYaw(), entity.getPitch());
    }

    public void copyFrom(Entity original) {
        NbtCompound lv = original.writeNbt(new NbtCompound());
        lv.remove("Dimension");
        this.readNbt(lv);
        this.portalCooldown = original.portalCooldown;
        this.portalManager = original.portalManager;
    }

    @Nullable
    public Entity teleportTo(TeleportTarget teleportTarget) {
        Entity lv5;
        ServerWorld lv;
        block9: {
            block8: {
                World world = this.getWorld();
                if (!(world instanceof ServerWorld)) break block8;
                lv = (ServerWorld)world;
                if (!this.isRemoved()) break block9;
            }
            return null;
        }
        ServerWorld lv2 = teleportTarget.world();
        List<Entity> list = this.getPassengerList();
        this.detach();
        ArrayList<Entity> list2 = new ArrayList<Entity>();
        for (Entity lv3 : list) {
            Entity lv4 = lv3.teleportTo(teleportTarget);
            if (lv4 == null) continue;
            list2.add(lv4);
        }
        lv.getProfiler().push("changeDimension");
        Entity entity = lv5 = lv2.getRegistryKey() == lv.getRegistryKey() ? this : this.getType().create(lv2);
        if (lv5 != null) {
            if (this != lv5) {
                lv5.copyFrom(this);
                this.removeFromDimension();
            }
            lv5.refreshPositionAndAngles(teleportTarget.pos().x, teleportTarget.pos().y, teleportTarget.pos().z, teleportTarget.yaw(), lv5.getPitch());
            lv5.setVelocity(teleportTarget.velocity());
            if (this != lv5) {
                lv2.onDimensionChanged(lv5);
            }
            for (Entity lv4 : list2) {
                lv4.startRiding(lv5, true);
            }
            lv.resetIdleTimeout();
            lv2.resetIdleTimeout();
            teleportTarget.postDimensionTransition().onTransition(lv5);
        }
        lv.getProfiler().pop();
        return lv5;
    }

    public void addPortalChunkTicketAt(BlockPos pos) {
        World world = this.getWorld();
        if (world instanceof ServerWorld) {
            ServerWorld lv = (ServerWorld)world;
            lv.getChunkManager().addTicket(ChunkTicketType.PORTAL, new ChunkPos(pos), 3, pos);
        }
    }

    protected void removeFromDimension() {
        this.setRemoved(RemovalReason.CHANGED_DIMENSION);
        Entity entity = this;
        if (entity instanceof Leashable) {
            Leashable lv = (Leashable)((Object)entity);
            lv.detachLeash(true, false);
        }
    }

    public Vec3d positionInPortal(Direction.Axis portalAxis, BlockLocating.Rectangle portalRect) {
        return NetherPortal.entityPosInPortal(portalRect, portalAxis, this.getPos(), this.getDimensions(this.getPose()));
    }

    public boolean canUsePortals(boolean allowVehicles) {
        return (allowVehicles || !this.hasVehicle()) && this.isAlive();
    }

    public boolean canTeleportBetween(World from, World to) {
        return true;
    }

    public float getEffectiveExplosionResistance(Explosion explosion, BlockView world, BlockPos pos, BlockState blockState, FluidState fluidState, float max) {
        return max;
    }

    public boolean canExplosionDestroyBlock(Explosion explosion, BlockView world, BlockPos pos, BlockState state, float explosionPower) {
        return true;
    }

    public int getSafeFallDistance() {
        return 3;
    }

    public boolean canAvoidTraps() {
        return false;
    }

    public void populateCrashReport(CrashReportSection section) {
        section.add("Entity Type", () -> String.valueOf(EntityType.getId(this.getType())) + " (" + this.getClass().getCanonicalName() + ")");
        section.add("Entity ID", this.id);
        section.add("Entity Name", () -> this.getName().getString());
        section.add("Entity's Exact location", String.format(Locale.ROOT, "%.2f, %.2f, %.2f", this.getX(), this.getY(), this.getZ()));
        section.add("Entity's Block location", CrashReportSection.createPositionString((HeightLimitView)this.getWorld(), MathHelper.floor(this.getX()), MathHelper.floor(this.getY()), MathHelper.floor(this.getZ())));
        Vec3d lv = this.getVelocity();
        section.add("Entity's Momentum", String.format(Locale.ROOT, "%.2f, %.2f, %.2f", lv.x, lv.y, lv.z));
        section.add("Entity's Passengers", () -> this.getPassengerList().toString());
        section.add("Entity's Vehicle", () -> String.valueOf(this.getVehicle()));
    }

    public boolean doesRenderOnFire() {
        return this.isOnFire() && !this.isSpectator();
    }

    public void setUuid(UUID uuid) {
        this.uuid = uuid;
        this.uuidString = this.uuid.toString();
    }

    @Override
    public UUID getUuid() {
        return this.uuid;
    }

    public String getUuidAsString() {
        return this.uuidString;
    }

    @Override
    public String getNameForScoreboard() {
        return this.uuidString;
    }

    public boolean isPushedByFluids() {
        return true;
    }

    public static double getRenderDistanceMultiplier() {
        return renderDistanceMultiplier;
    }

    public static void setRenderDistanceMultiplier(double value) {
        renderDistanceMultiplier = value;
    }

    @Override
    public Text getDisplayName() {
        return Team.decorateName(this.getScoreboardTeam(), this.getName()).styled(style -> style.withHoverEvent(this.getHoverEvent()).withInsertion(this.getUuidAsString()));
    }

    public void setCustomName(@Nullable Text name) {
        this.dataTracker.set(CUSTOM_NAME, Optional.ofNullable(name));
    }

    @Override
    @Nullable
    public Text getCustomName() {
        return this.dataTracker.get(CUSTOM_NAME).orElse(null);
    }

    @Override
    public boolean hasCustomName() {
        return this.dataTracker.get(CUSTOM_NAME).isPresent();
    }

    public void setCustomNameVisible(boolean visible) {
        this.dataTracker.set(NAME_VISIBLE, visible);
    }

    public boolean isCustomNameVisible() {
        return this.dataTracker.get(NAME_VISIBLE);
    }

    public boolean teleport(ServerWorld world, double destX, double destY, double destZ, Set<PositionFlag> flags, float yaw, float pitch) {
        float i = MathHelper.clamp(pitch, -90.0f, 90.0f);
        if (world == this.getWorld()) {
            this.refreshPositionAndAngles(destX, destY, destZ, yaw, i);
            this.teleportPassengers();
            this.setHeadYaw(yaw);
        } else {
            this.detach();
            Object lv = this.getType().create(world);
            if (lv != null) {
                ((Entity)lv).copyFrom(this);
                ((Entity)lv).refreshPositionAndAngles(destX, destY, destZ, yaw, i);
                ((Entity)lv).setHeadYaw(yaw);
                this.setRemoved(RemovalReason.CHANGED_DIMENSION);
                world.onDimensionChanged((Entity)lv);
            } else {
                return false;
            }
        }
        return true;
    }

    public void requestTeleportAndDismount(double destX, double destY, double destZ) {
        this.requestTeleport(destX, destY, destZ);
    }

    public void requestTeleport(double destX, double destY, double destZ) {
        if (!(this.getWorld() instanceof ServerWorld)) {
            return;
        }
        this.refreshPositionAndAngles(destX, destY, destZ, this.getYaw(), this.getPitch());
        this.teleportPassengers();
    }

    private void teleportPassengers() {
        this.streamSelfAndPassengers().forEach(entity -> {
            for (Entity lv : entity.passengerList) {
                entity.updatePassengerPosition(lv, Entity::refreshPositionAfterTeleport);
            }
        });
    }

    public void requestTeleportOffset(double offsetX, double offsetY, double offsetZ) {
        this.requestTeleport(this.getX() + offsetX, this.getY() + offsetY, this.getZ() + offsetZ);
    }

    public boolean shouldRenderName() {
        return this.isCustomNameVisible();
    }

    @Override
    public void onDataTrackerUpdate(List<DataTracker.SerializedEntry<?>> entries) {
    }

    @Override
    public void onTrackedDataSet(TrackedData<?> data) {
        if (POSE.equals(data)) {
            this.calculateDimensions();
        }
    }

    @Deprecated
    protected void reinitDimensions() {
        EntityDimensions lv2;
        EntityPose lv = this.getPose();
        this.dimensions = lv2 = this.getDimensions(lv);
        this.standingEyeHeight = lv2.eyeHeight();
    }

    public void calculateDimensions() {
        boolean bl;
        EntityDimensions lv3;
        EntityDimensions lv = this.dimensions;
        EntityPose lv2 = this.getPose();
        this.dimensions = lv3 = this.getDimensions(lv2);
        this.standingEyeHeight = lv3.eyeHeight();
        this.refreshPosition();
        boolean bl2 = bl = (double)lv3.width() <= 4.0 && (double)lv3.height() <= 4.0;
        if (!(this.world.isClient || this.firstUpdate || this.noClip || !bl || !(lv3.width() > lv.width()) && !(lv3.height() > lv.height()) || this instanceof PlayerEntity)) {
            this.recalculateDimensions(lv);
        }
    }

    public boolean recalculateDimensions(EntityDimensions previous) {
        VoxelShape lv4;
        Optional<Vec3d> optional2;
        double e;
        double d;
        EntityDimensions lv = this.getDimensions(this.getPose());
        Vec3d lv2 = this.getPos().add(0.0, (double)previous.height() / 2.0, 0.0);
        VoxelShape lv3 = VoxelShapes.cuboid(Box.of(lv2, d = (double)Math.max(0.0f, lv.width() - previous.width()) + 1.0E-6, e = (double)Math.max(0.0f, lv.height() - previous.height()) + 1.0E-6, d));
        Optional<Vec3d> optional = this.world.findClosestCollision(this, lv3, lv2, lv.width(), lv.height(), lv.width());
        if (optional.isPresent()) {
            this.setPosition(optional.get().add(0.0, (double)(-lv.height()) / 2.0, 0.0));
            return true;
        }
        if (lv.width() > previous.width() && lv.height() > previous.height() && (optional2 = this.world.findClosestCollision(this, lv4 = VoxelShapes.cuboid(Box.of(lv2, d, 1.0E-6, d)), lv2, lv.width(), previous.height(), lv.width())).isPresent()) {
            this.setPosition(optional2.get().add(0.0, (double)(-previous.height()) / 2.0 + 1.0E-6, 0.0));
            return true;
        }
        return false;
    }

    public Direction getHorizontalFacing() {
        return Direction.fromRotation(this.getYaw());
    }

    public Direction getMovementDirection() {
        return this.getHorizontalFacing();
    }

    protected HoverEvent getHoverEvent() {
        return new HoverEvent(HoverEvent.Action.SHOW_ENTITY, new HoverEvent.EntityContent(this.getType(), this.getUuid(), this.getName()));
    }

    public boolean canBeSpectated(ServerPlayerEntity spectator) {
        return true;
    }

    @Override
    public final Box getBoundingBox() {
        return this.boundingBox;
    }

    public Box getVisibilityBoundingBox() {
        return this.getBoundingBox();
    }

    public final void setBoundingBox(Box boundingBox) {
        this.boundingBox = boundingBox;
    }

    public final float getEyeHeight(EntityPose pose) {
        return this.getDimensions(pose).eyeHeight();
    }

    public final float getStandingEyeHeight() {
        return this.standingEyeHeight;
    }

    public Vec3d getLeashOffset(float tickDelta) {
        return this.getLeashOffset();
    }

    protected Vec3d getLeashOffset() {
        return new Vec3d(0.0, this.getStandingEyeHeight(), this.getWidth() * 0.4f);
    }

    public StackReference getStackReference(int mappedIndex) {
        return StackReference.EMPTY;
    }

    @Override
    public void sendMessage(Text message) {
    }

    public World getEntityWorld() {
        return this.getWorld();
    }

    @Nullable
    public MinecraftServer getServer() {
        return this.getWorld().getServer();
    }

    public ActionResult interactAt(PlayerEntity player, Vec3d hitPos, Hand hand) {
        return ActionResult.PASS;
    }

    public boolean isImmuneToExplosion(Explosion explosion) {
        return false;
    }

    public void onStartedTrackingBy(ServerPlayerEntity player) {
    }

    public void onStoppedTrackingBy(ServerPlayerEntity player) {
    }

    public float applyRotation(BlockRotation rotation) {
        float f = MathHelper.wrapDegrees(this.getYaw());
        switch (rotation) {
            case CLOCKWISE_180: {
                return f + 180.0f;
            }
            case COUNTERCLOCKWISE_90: {
                return f + 270.0f;
            }
            case CLOCKWISE_90: {
                return f + 90.0f;
            }
        }
        return f;
    }

    public float applyMirror(BlockMirror mirror) {
        float f = MathHelper.wrapDegrees(this.getYaw());
        switch (mirror) {
            case FRONT_BACK: {
                return -f;
            }
            case LEFT_RIGHT: {
                return 180.0f - f;
            }
        }
        return f;
    }

    public boolean entityDataRequiresOperator() {
        return false;
    }

    public ProjectileDeflection getProjectileDeflection(ProjectileEntity projectile) {
        return this.getType().isIn(EntityTypeTags.DEFLECTS_PROJECTILES) ? ProjectileDeflection.SIMPLE : ProjectileDeflection.NONE;
    }

    @Nullable
    public LivingEntity getControllingPassenger() {
        return null;
    }

    public final boolean hasControllingPassenger() {
        return this.getControllingPassenger() != null;
    }

    public final List<Entity> getPassengerList() {
        return this.passengerList;
    }

    @Nullable
    public Entity getFirstPassenger() {
        return this.passengerList.isEmpty() ? null : (Entity)this.passengerList.get(0);
    }

    public boolean hasPassenger(Entity passenger) {
        return this.passengerList.contains(passenger);
    }

    public boolean hasPassenger(Predicate<Entity> predicate) {
        for (Entity lv : this.passengerList) {
            if (!predicate.test(lv)) continue;
            return true;
        }
        return false;
    }

    private Stream<Entity> streamIntoPassengers() {
        return this.passengerList.stream().flatMap(Entity::streamSelfAndPassengers);
    }

    public Stream<Entity> streamSelfAndPassengers() {
        return Stream.concat(Stream.of(this), this.streamIntoPassengers());
    }

    public Stream<Entity> streamPassengersAndSelf() {
        return Stream.concat(this.passengerList.stream().flatMap(Entity::streamPassengersAndSelf), Stream.of(this));
    }

    public Iterable<Entity> getPassengersDeep() {
        return () -> this.streamIntoPassengers().iterator();
    }

    public int getPlayerPassengers() {
        return (int)this.streamIntoPassengers().filter(passenger -> passenger instanceof PlayerEntity).count();
    }

    public boolean hasPlayerRider() {
        return this.getPlayerPassengers() == 1;
    }

    public Entity getRootVehicle() {
        Entity lv = this;
        while (lv.hasVehicle()) {
            lv = lv.getVehicle();
        }
        return lv;
    }

    public boolean isConnectedThroughVehicle(Entity entity) {
        return this.getRootVehicle() == entity.getRootVehicle();
    }

    public boolean hasPassengerDeep(Entity passenger) {
        if (!passenger.hasVehicle()) {
            return false;
        }
        Entity lv = passenger.getVehicle();
        if (lv == this) {
            return true;
        }
        return this.hasPassengerDeep(lv);
    }

    public boolean isLogicalSideForUpdatingMovement() {
        LivingEntity livingEntity = this.getControllingPassenger();
        if (livingEntity instanceof PlayerEntity) {
            PlayerEntity lv = (PlayerEntity)livingEntity;
            return lv.isMainPlayer();
        }
        return this.canMoveVoluntarily();
    }

    public boolean canMoveVoluntarily() {
        return !this.getWorld().isClient;
    }

    protected static Vec3d getPassengerDismountOffset(double vehicleWidth, double passengerWidth, float passengerYaw) {
        double g = (vehicleWidth + passengerWidth + (double)1.0E-5f) / 2.0;
        float h = -MathHelper.sin(passengerYaw * ((float)Math.PI / 180));
        float i = MathHelper.cos(passengerYaw * ((float)Math.PI / 180));
        float j = Math.max(Math.abs(h), Math.abs(i));
        return new Vec3d((double)h * g / (double)j, 0.0, (double)i * g / (double)j);
    }

    public Vec3d updatePassengerForDismount(LivingEntity passenger) {
        return new Vec3d(this.getX(), this.getBoundingBox().maxY, this.getZ());
    }

    @Nullable
    public Entity getVehicle() {
        return this.vehicle;
    }

    @Nullable
    public Entity getControllingVehicle() {
        return this.vehicle != null && this.vehicle.getControllingPassenger() == this ? this.vehicle : null;
    }

    public PistonBehavior getPistonBehavior() {
        return PistonBehavior.NORMAL;
    }

    public SoundCategory getSoundCategory() {
        return SoundCategory.NEUTRAL;
    }

    protected int getBurningDuration() {
        return 1;
    }

    public ServerCommandSource getCommandSource() {
        return new ServerCommandSource(this, this.getPos(), this.getRotationClient(), this.getWorld() instanceof ServerWorld ? (ServerWorld)this.getWorld() : null, this.getPermissionLevel(), this.getName().getString(), this.getDisplayName(), this.getWorld().getServer(), this);
    }

    protected int getPermissionLevel() {
        return 0;
    }

    public boolean hasPermissionLevel(int permissionLevel) {
        return this.getPermissionLevel() >= permissionLevel;
    }

    @Override
    public boolean shouldReceiveFeedback() {
        return this.getWorld().getGameRules().getBoolean(GameRules.SEND_COMMAND_FEEDBACK);
    }

    @Override
    public boolean shouldTrackOutput() {
        return true;
    }

    @Override
    public boolean shouldBroadcastConsoleToOps() {
        return true;
    }

    public void lookAt(EntityAnchorArgumentType.EntityAnchor anchorPoint, Vec3d target) {
        Vec3d lv = anchorPoint.positionAt(this);
        double d = target.x - lv.x;
        double e = target.y - lv.y;
        double f = target.z - lv.z;
        double g = Math.sqrt(d * d + f * f);
        this.setPitch(MathHelper.wrapDegrees((float)(-(MathHelper.atan2(e, g) * 57.2957763671875))));
        this.setYaw(MathHelper.wrapDegrees((float)(MathHelper.atan2(f, d) * 57.2957763671875) - 90.0f));
        this.setHeadYaw(this.getYaw());
        this.prevPitch = this.getPitch();
        this.prevYaw = this.getYaw();
    }

    public float lerpYaw(float delta) {
        return MathHelper.lerp(delta, this.prevYaw, this.yaw);
    }

    public boolean updateMovementInFluid(TagKey<Fluid> tag, double speed) {
        if (this.isRegionUnloaded()) {
            return false;
        }
        Box lv = this.getBoundingBox().contract(0.001);
        int i = MathHelper.floor(lv.minX);
        int j = MathHelper.ceil(lv.maxX);
        int k = MathHelper.floor(lv.minY);
        int l = MathHelper.ceil(lv.maxY);
        int m = MathHelper.floor(lv.minZ);
        int n = MathHelper.ceil(lv.maxZ);
        double e = 0.0;
        boolean bl = this.isPushedByFluids();
        boolean bl2 = false;
        Vec3d lv2 = Vec3d.ZERO;
        int o = 0;
        BlockPos.Mutable lv3 = new BlockPos.Mutable();
        for (int p = i; p < j; ++p) {
            for (int q = k; q < l; ++q) {
                for (int r = m; r < n; ++r) {
                    double f;
                    lv3.set(p, q, r);
                    FluidState lv4 = this.getWorld().getFluidState(lv3);
                    if (!lv4.isIn(tag) || !((f = (double)((float)q + lv4.getHeight(this.getWorld(), lv3))) >= lv.minY)) continue;
                    bl2 = true;
                    e = Math.max(f - lv.minY, e);
                    if (!bl) continue;
                    Vec3d lv5 = lv4.getVelocity(this.getWorld(), lv3);
                    if (e < 0.4) {
                        lv5 = lv5.multiply(e);
                    }
                    lv2 = lv2.add(lv5);
                    ++o;
                }
            }
        }
        if (lv2.length() > 0.0) {
            if (o > 0) {
                lv2 = lv2.multiply(1.0 / (double)o);
            }
            if (!(this instanceof PlayerEntity)) {
                lv2 = lv2.normalize();
            }
            Vec3d lv6 = this.getVelocity();
            lv2 = lv2.multiply(speed);
            double g = 0.003;
            if (Math.abs(lv6.x) < 0.003 && Math.abs(lv6.z) < 0.003 && lv2.length() < 0.0045000000000000005) {
                lv2 = lv2.normalize().multiply(0.0045000000000000005);
            }
            this.setVelocity(this.getVelocity().add(lv2));
        }
        this.fluidHeight.put(tag, e);
        return bl2;
    }

    public boolean isRegionUnloaded() {
        Box lv = this.getBoundingBox().expand(1.0);
        int i = MathHelper.floor(lv.minX);
        int j = MathHelper.ceil(lv.maxX);
        int k = MathHelper.floor(lv.minZ);
        int l = MathHelper.ceil(lv.maxZ);
        return !this.getWorld().isRegionLoaded(i, k, j, l);
    }

    public double getFluidHeight(TagKey<Fluid> fluid) {
        return this.fluidHeight.getDouble(fluid);
    }

    public double getSwimHeight() {
        return (double)this.getStandingEyeHeight() < 0.4 ? 0.0 : 0.4;
    }

    public final float getWidth() {
        return this.dimensions.width();
    }

    public final float getHeight() {
        return this.dimensions.height();
    }

    public Packet<ClientPlayPacketListener> createSpawnPacket(EntityTrackerEntry entityTrackerEntry) {
        return new EntitySpawnS2CPacket(this, entityTrackerEntry);
    }

    public EntityDimensions getDimensions(EntityPose pose) {
        return this.type.getDimensions();
    }

    public final EntityAttachments getAttachments() {
        return this.dimensions.attachments();
    }

    public Vec3d getPos() {
        return this.pos;
    }

    public Vec3d getSyncedPos() {
        return this.getPos();
    }

    @Override
    public BlockPos getBlockPos() {
        return this.blockPos;
    }

    public BlockState getBlockStateAtPos() {
        if (this.stateAtPos == null) {
            this.stateAtPos = this.getWorld().getBlockState(this.getBlockPos());
        }
        return this.stateAtPos;
    }

    public ChunkPos getChunkPos() {
        return this.chunkPos;
    }

    public Vec3d getVelocity() {
        return this.velocity;
    }

    public void setVelocity(Vec3d velocity) {
        this.velocity = velocity;
    }

    public void addVelocityInternal(Vec3d velocity) {
        this.setVelocity(this.getVelocity().add(velocity));
    }

    public void setVelocity(double x, double y, double z) {
        this.setVelocity(new Vec3d(x, y, z));
    }

    public final int getBlockX() {
        return this.blockPos.getX();
    }

    public final double getX() {
        return this.pos.x;
    }

    public double offsetX(double widthScale) {
        return this.pos.x + (double)this.getWidth() * widthScale;
    }

    public double getParticleX(double widthScale) {
        return this.offsetX((2.0 * this.random.nextDouble() - 1.0) * widthScale);
    }

    public final int getBlockY() {
        return this.blockPos.getY();
    }

    public final double getY() {
        return this.pos.y;
    }

    public double getBodyY(double heightScale) {
        return this.pos.y + (double)this.getHeight() * heightScale;
    }

    public double getRandomBodyY() {
        return this.getBodyY(this.random.nextDouble());
    }

    public double getEyeY() {
        return this.pos.y + (double)this.standingEyeHeight;
    }

    public final int getBlockZ() {
        return this.blockPos.getZ();
    }

    public final double getZ() {
        return this.pos.z;
    }

    public double offsetZ(double widthScale) {
        return this.pos.z + (double)this.getWidth() * widthScale;
    }

    public double getParticleZ(double widthScale) {
        return this.offsetZ((2.0 * this.random.nextDouble() - 1.0) * widthScale);
    }

    public final void setPos(double x, double y, double z) {
        if (this.pos.x != x || this.pos.y != y || this.pos.z != z) {
            this.pos = new Vec3d(x, y, z);
            int i = MathHelper.floor(x);
            int j = MathHelper.floor(y);
            int k = MathHelper.floor(z);
            if (i != this.blockPos.getX() || j != this.blockPos.getY() || k != this.blockPos.getZ()) {
                this.blockPos = new BlockPos(i, j, k);
                this.stateAtPos = null;
                if (ChunkSectionPos.getSectionCoord(i) != this.chunkPos.x || ChunkSectionPos.getSectionCoord(k) != this.chunkPos.z) {
                    this.chunkPos = new ChunkPos(this.blockPos);
                }
            }
            this.changeListener.updateEntityPosition();
        }
    }

    public void checkDespawn() {
    }

    public Vec3d getLeashPos(float delta) {
        return this.getLerpedPos(delta).add(0.0, (double)this.standingEyeHeight * 0.7, 0.0);
    }

    public void onSpawnPacket(EntitySpawnS2CPacket packet) {
        int i = packet.getEntityId();
        double d = packet.getX();
        double e = packet.getY();
        double f = packet.getZ();
        this.updateTrackedPosition(d, e, f);
        this.refreshPositionAfterTeleport(d, e, f);
        this.setPitch(packet.getPitch());
        this.setYaw(packet.getYaw());
        this.setId(i);
        this.setUuid(packet.getUuid());
    }

    @Nullable
    public ItemStack getPickBlockStack() {
        return null;
    }

    public void setInPowderSnow(boolean inPowderSnow) {
        this.inPowderSnow = inPowderSnow;
    }

    public boolean canFreeze() {
        return !this.getType().isIn(EntityTypeTags.FREEZE_IMMUNE_ENTITY_TYPES);
    }

    public boolean shouldEscapePowderSnow() {
        return (this.inPowderSnow || this.wasInPowderSnow) && this.canFreeze();
    }

    public float getYaw() {
        return this.yaw;
    }

    public float getBodyYaw() {
        return this.getYaw();
    }

    public void setYaw(float yaw) {
        if (!Float.isFinite(yaw)) {
            Util.error("Invalid entity rotation: " + yaw + ", discarding.");
            return;
        }
        this.yaw = yaw;
    }

    public float getPitch() {
        return this.pitch;
    }

    public void setPitch(float pitch) {
        if (!Float.isFinite(pitch)) {
            Util.error("Invalid entity rotation: " + pitch + ", discarding.");
            return;
        }
        this.pitch = pitch;
    }

    public boolean canSprintAsVehicle() {
        return false;
    }

    public float getStepHeight() {
        return 0.0f;
    }

    public void onExplodedBy(@Nullable Entity entity) {
    }

    public final boolean isRemoved() {
        return this.removalReason != null;
    }

    @Nullable
    public RemovalReason getRemovalReason() {
        return this.removalReason;
    }

    @Override
    public final void setRemoved(RemovalReason reason) {
        if (this.removalReason == null) {
            this.removalReason = reason;
        }
        if (this.removalReason.shouldDestroy()) {
            this.stopRiding();
        }
        this.getPassengerList().forEach(Entity::stopRiding);
        this.changeListener.remove(reason);
    }

    protected void unsetRemoved() {
        this.removalReason = null;
    }

    @Override
    public void setChangeListener(EntityChangeListener changeListener) {
        this.changeListener = changeListener;
    }

    @Override
    public boolean shouldSave() {
        if (this.removalReason != null && !this.removalReason.shouldSave()) {
            return false;
        }
        if (this.hasVehicle()) {
            return false;
        }
        return !this.hasPassengers() || !this.hasPlayerRider();
    }

    @Override
    public boolean isPlayer() {
        return false;
    }

    public boolean canModifyAt(World world, BlockPos pos) {
        return true;
    }

    public World getWorld() {
        return this.world;
    }

    protected void setWorld(World world) {
        this.world = world;
    }

    public DamageSources getDamageSources() {
        return this.getWorld().getDamageSources();
    }

    public DynamicRegistryManager getRegistryManager() {
        return this.getWorld().getRegistryManager();
    }

    protected void lerpPosAndRotation(int step, double x, double y, double z, double yaw, double pitch) {
        double j = 1.0 / (double)step;
        double k = MathHelper.lerp(j, this.getX(), x);
        double l = MathHelper.lerp(j, this.getY(), y);
        double m = MathHelper.lerp(j, this.getZ(), z);
        float n = (float)MathHelper.lerpAngleDegrees(j, (double)this.getYaw(), yaw);
        float o = (float)MathHelper.lerp(j, (double)this.getPitch(), pitch);
        this.setPosition(k, l, m);
        this.setRotation(n, o);
    }

    public Random getRandom() {
        return this.random;
    }

    public Vec3d getMovement() {
        LivingEntity livingEntity = this.getControllingPassenger();
        if (livingEntity instanceof PlayerEntity) {
            PlayerEntity lv = (PlayerEntity)livingEntity;
            if (this.isAlive()) {
                return lv.getMovement();
            }
        }
        return this.getVelocity();
    }

    @Nullable
    public ItemStack getWeaponStack() {
        return null;
    }

    public static enum RemovalReason {
        KILLED(true, false),
        DISCARDED(true, false),
        UNLOADED_TO_CHUNK(false, true),
        UNLOADED_WITH_PLAYER(false, false),
        CHANGED_DIMENSION(false, false);

        private final boolean destroy;
        private final boolean save;

        private RemovalReason(boolean destroy, boolean save) {
            this.destroy = destroy;
            this.save = save;
        }

        public boolean shouldDestroy() {
            return this.destroy;
        }

        public boolean shouldSave() {
            return this.save;
        }
    }

    public static enum MoveEffect {
        NONE(false, false),
        SOUNDS(true, false),
        EVENTS(false, true),
        ALL(true, true);

        final boolean sounds;
        final boolean events;

        private MoveEffect(boolean sounds, boolean events) {
            this.sounds = sounds;
            this.events = events;
        }

        public boolean hasAny() {
            return this.events || this.sounds;
        }

        public boolean emitsGameEvents() {
            return this.events;
        }

        public boolean playsSounds() {
            return this.sounds;
        }
    }

    @FunctionalInterface
    public static interface PositionUpdater {
        public void accept(Entity var1, double var2, double var4, double var6);
    }
}

