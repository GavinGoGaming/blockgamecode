/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 * 
 * Could not load the following classes:
 *  org.jetbrains.annotations.Nullable
 */
package net.minecraft.entity.vehicle;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import com.mojang.datafixers.util.Pair;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import net.minecraft.block.AbstractRailBlock;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.PoweredRailBlock;
import net.minecraft.block.enums.RailShape;
import net.minecraft.entity.Dismounting;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityDimensions;
import net.minecraft.entity.EntityPose;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.MovementType;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.data.TrackedData;
import net.minecraft.entity.data.TrackedDataHandlerRegistry;
import net.minecraft.entity.passive.IronGolemEntity;
import net.minecraft.entity.passive.VillagerEntity;
import net.minecraft.entity.passive.WanderingTraderEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.vehicle.BoatEntity;
import net.minecraft.entity.vehicle.ChestMinecartEntity;
import net.minecraft.entity.vehicle.CommandBlockMinecartEntity;
import net.minecraft.entity.vehicle.FurnaceMinecartEntity;
import net.minecraft.entity.vehicle.HopperMinecartEntity;
import net.minecraft.entity.vehicle.MinecartEntity;
import net.minecraft.entity.vehicle.SpawnerMinecartEntity;
import net.minecraft.entity.vehicle.TntMinecartEntity;
import net.minecraft.entity.vehicle.VehicleEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtHelper;
import net.minecraft.predicate.entity.EntityPredicates;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.tag.BlockTags;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Util;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.Vec3i;
import net.minecraft.world.BlockLocating;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

public abstract class AbstractMinecartEntity
extends VehicleEntity {
    private static final Vec3d VILLAGER_PASSENGER_ATTACHMENT_POS = new Vec3d(0.0, 0.0, 0.0);
    private static final TrackedData<Integer> CUSTOM_BLOCK_ID = DataTracker.registerData(AbstractMinecartEntity.class, TrackedDataHandlerRegistry.INTEGER);
    private static final TrackedData<Integer> CUSTOM_BLOCK_OFFSET = DataTracker.registerData(AbstractMinecartEntity.class, TrackedDataHandlerRegistry.INTEGER);
    private static final TrackedData<Boolean> CUSTOM_BLOCK_PRESENT = DataTracker.registerData(AbstractMinecartEntity.class, TrackedDataHandlerRegistry.BOOLEAN);
    private static final ImmutableMap<EntityPose, ImmutableList<Integer>> DISMOUNT_FREE_Y_SPACES_NEEDED = ImmutableMap.of(EntityPose.STANDING, ImmutableList.of(Integer.valueOf(0), Integer.valueOf(1), Integer.valueOf(-1)), EntityPose.CROUCHING, ImmutableList.of(Integer.valueOf(0), Integer.valueOf(1), Integer.valueOf(-1)), EntityPose.SWIMMING, ImmutableList.of(Integer.valueOf(0), Integer.valueOf(1)));
    protected static final float VELOCITY_SLOWDOWN_MULTIPLIER = 0.95f;
    private boolean yawFlipped;
    private boolean onRail;
    private int clientInterpolationSteps;
    private double clientX;
    private double clientY;
    private double clientZ;
    private double clientYaw;
    private double clientPitch;
    private Vec3d clientVelocity = Vec3d.ZERO;
    private static final Map<RailShape, Pair<Vec3i, Vec3i>> ADJACENT_RAIL_POSITIONS_BY_SHAPE = Util.make(Maps.newEnumMap(RailShape.class), map -> {
        Vec3i lv = Direction.WEST.getVector();
        Vec3i lv2 = Direction.EAST.getVector();
        Vec3i lv3 = Direction.NORTH.getVector();
        Vec3i lv4 = Direction.SOUTH.getVector();
        Vec3i lv5 = lv.down();
        Vec3i lv6 = lv2.down();
        Vec3i lv7 = lv3.down();
        Vec3i lv8 = lv4.down();
        map.put(RailShape.NORTH_SOUTH, Pair.of(lv3, lv4));
        map.put(RailShape.EAST_WEST, Pair.of(lv, lv2));
        map.put(RailShape.ASCENDING_EAST, Pair.of(lv5, lv2));
        map.put(RailShape.ASCENDING_WEST, Pair.of(lv, lv6));
        map.put(RailShape.ASCENDING_NORTH, Pair.of(lv3, lv8));
        map.put(RailShape.ASCENDING_SOUTH, Pair.of(lv7, lv4));
        map.put(RailShape.SOUTH_EAST, Pair.of(lv4, lv2));
        map.put(RailShape.SOUTH_WEST, Pair.of(lv4, lv));
        map.put(RailShape.NORTH_WEST, Pair.of(lv3, lv));
        map.put(RailShape.NORTH_EAST, Pair.of(lv3, lv2));
    });

    protected AbstractMinecartEntity(EntityType<?> arg, World arg2) {
        super(arg, arg2);
        this.intersectionChecked = true;
    }

    protected AbstractMinecartEntity(EntityType<?> type, World world, double x, double y, double z) {
        this(type, world);
        this.setPosition(x, y, z);
        this.prevX = x;
        this.prevY = y;
        this.prevZ = z;
    }

    public static AbstractMinecartEntity create(ServerWorld world, double x, double y, double z, Type type, ItemStack stack, @Nullable PlayerEntity player) {
        AbstractMinecartEntity lv = switch (type.ordinal()) {
            case 1 -> new ChestMinecartEntity(world, x, y, z);
            case 2 -> new FurnaceMinecartEntity(world, x, y, z);
            case 3 -> new TntMinecartEntity(world, x, y, z);
            case 4 -> new SpawnerMinecartEntity(world, x, y, z);
            case 5 -> new HopperMinecartEntity(world, x, y, z);
            case 6 -> new CommandBlockMinecartEntity(world, x, y, z);
            default -> new MinecartEntity(world, x, y, z);
        };
        EntityType.copier(world, stack, player).accept((ChestMinecartEntity)lv);
        return lv;
    }

    @Override
    protected Entity.MoveEffect getMoveEffect() {
        return Entity.MoveEffect.EVENTS;
    }

    @Override
    protected void initDataTracker(DataTracker.Builder builder) {
        super.initDataTracker(builder);
        builder.add(CUSTOM_BLOCK_ID, Block.getRawIdFromState(Blocks.AIR.getDefaultState()));
        builder.add(CUSTOM_BLOCK_OFFSET, 6);
        builder.add(CUSTOM_BLOCK_PRESENT, false);
    }

    @Override
    public boolean collidesWith(Entity other) {
        return BoatEntity.canCollide(this, other);
    }

    @Override
    public boolean isPushable() {
        return true;
    }

    @Override
    public Vec3d positionInPortal(Direction.Axis portalAxis, BlockLocating.Rectangle portalRect) {
        return LivingEntity.positionInPortal(super.positionInPortal(portalAxis, portalRect));
    }

    @Override
    protected Vec3d getPassengerAttachmentPos(Entity passenger, EntityDimensions dimensions, float scaleFactor) {
        boolean bl;
        boolean bl2 = bl = passenger instanceof VillagerEntity || passenger instanceof WanderingTraderEntity;
        if (bl) {
            return VILLAGER_PASSENGER_ATTACHMENT_POS;
        }
        return super.getPassengerAttachmentPos(passenger, dimensions, scaleFactor);
    }

    @Override
    public Vec3d updatePassengerForDismount(LivingEntity passenger) {
        Direction lv = this.getMovementDirection();
        if (lv.getAxis() == Direction.Axis.Y) {
            return super.updatePassengerForDismount(passenger);
        }
        int[][] is = Dismounting.getDismountOffsets(lv);
        BlockPos lv2 = this.getBlockPos();
        BlockPos.Mutable lv3 = new BlockPos.Mutable();
        ImmutableList<EntityPose> immutableList = passenger.getPoses();
        for (EntityPose lv4 : immutableList) {
            EntityDimensions lv5 = passenger.getDimensions(lv4);
            float f = Math.min(lv5.width(), 1.0f) / 2.0f;
            Iterator iterator = DISMOUNT_FREE_Y_SPACES_NEEDED.get((Object)lv4).iterator();
            while (iterator.hasNext()) {
                int i = (Integer)iterator.next();
                for (int[] js : is) {
                    lv3.set(lv2.getX() + js[0], lv2.getY() + i, lv2.getZ() + js[1]);
                    double d = this.getWorld().getDismountHeight(Dismounting.getCollisionShape(this.getWorld(), lv3), () -> Dismounting.getCollisionShape(this.getWorld(), (BlockPos)lv3.down()));
                    if (!Dismounting.canDismountInBlock(d)) continue;
                    Box lv6 = new Box(-f, 0.0, -f, f, lv5.height(), f);
                    Vec3d lv7 = Vec3d.ofCenter(lv3, d);
                    if (!Dismounting.canPlaceEntityAt(this.getWorld(), passenger, lv6.offset(lv7))) continue;
                    passenger.setPose(lv4);
                    return lv7;
                }
            }
        }
        double e = this.getBoundingBox().maxY;
        lv3.set((double)lv2.getX(), e, (double)lv2.getZ());
        for (EntityPose lv8 : immutableList) {
            int j;
            double h;
            double g = passenger.getDimensions(lv8).height();
            if (!(e + g <= (h = Dismounting.getCeilingHeight(lv3, j = MathHelper.ceil(e - (double)lv3.getY() + g), pos -> this.getWorld().getBlockState((BlockPos)pos).getCollisionShape(this.getWorld(), (BlockPos)pos))))) continue;
            passenger.setPose(lv8);
            break;
        }
        return super.updatePassengerForDismount(passenger);
    }

    @Override
    protected float getVelocityMultiplier() {
        BlockState lv = this.getWorld().getBlockState(this.getBlockPos());
        if (lv.isIn(BlockTags.RAILS)) {
            return 1.0f;
        }
        return super.getVelocityMultiplier();
    }

    @Override
    public void animateDamage(float yaw) {
        this.setDamageWobbleSide(-this.getDamageWobbleSide());
        this.setDamageWobbleTicks(10);
        this.setDamageWobbleStrength(this.getDamageWobbleStrength() + this.getDamageWobbleStrength() * 10.0f);
    }

    @Override
    public boolean canHit() {
        return !this.isRemoved();
    }

    private static Pair<Vec3i, Vec3i> getAdjacentRailPositionsByShape(RailShape shape) {
        return ADJACENT_RAIL_POSITIONS_BY_SHAPE.get(shape);
    }

    @Override
    public Direction getMovementDirection() {
        return this.yawFlipped ? this.getHorizontalFacing().getOpposite().rotateYClockwise() : this.getHorizontalFacing().rotateYClockwise();
    }

    @Override
    protected double getGravity() {
        return this.isTouchingWater() ? 0.005 : 0.04;
    }

    @Override
    public void tick() {
        double f;
        if (this.getDamageWobbleTicks() > 0) {
            this.setDamageWobbleTicks(this.getDamageWobbleTicks() - 1);
        }
        if (this.getDamageWobbleStrength() > 0.0f) {
            this.setDamageWobbleStrength(this.getDamageWobbleStrength() - 1.0f);
        }
        this.attemptTickInVoid();
        this.tickPortalTeleportation();
        if (this.getWorld().isClient) {
            if (this.clientInterpolationSteps > 0) {
                this.lerpPosAndRotation(this.clientInterpolationSteps, this.clientX, this.clientY, this.clientZ, this.clientYaw, this.clientPitch);
                --this.clientInterpolationSteps;
            } else {
                this.refreshPosition();
                this.setRotation(this.getYaw(), this.getPitch());
            }
            return;
        }
        this.applyGravity();
        int i = MathHelper.floor(this.getX());
        int j = MathHelper.floor(this.getY());
        int k = MathHelper.floor(this.getZ());
        if (this.getWorld().getBlockState(new BlockPos(i, j - 1, k)).isIn(BlockTags.RAILS)) {
            --j;
        }
        BlockPos lv = new BlockPos(i, j, k);
        BlockState lv2 = this.getWorld().getBlockState(lv);
        this.onRail = AbstractRailBlock.isRail(lv2);
        if (this.onRail) {
            this.moveOnRail(lv, lv2);
            if (lv2.isOf(Blocks.ACTIVATOR_RAIL)) {
                this.onActivatorRail(i, j, k, lv2.get(PoweredRailBlock.POWERED));
            }
        } else {
            this.moveOffRail();
        }
        this.checkBlockCollision();
        this.setPitch(0.0f);
        double d = this.prevX - this.getX();
        double e = this.prevZ - this.getZ();
        if (d * d + e * e > 0.001) {
            this.setYaw((float)(MathHelper.atan2(e, d) * 180.0 / Math.PI));
            if (this.yawFlipped) {
                this.setYaw(this.getYaw() + 180.0f);
            }
        }
        if ((f = (double)MathHelper.wrapDegrees(this.getYaw() - this.prevYaw)) < -170.0 || f >= 170.0) {
            this.setYaw(this.getYaw() + 180.0f);
            this.yawFlipped = !this.yawFlipped;
        }
        this.setRotation(this.getYaw(), this.getPitch());
        if (this.getMinecartType() == Type.RIDEABLE && this.getVelocity().horizontalLengthSquared() > 0.01) {
            List<Entity> list = this.getWorld().getOtherEntities(this, this.getBoundingBox().expand(0.2f, 0.0, 0.2f), EntityPredicates.canBePushedBy(this));
            if (!list.isEmpty()) {
                for (Entity lv3 : list) {
                    if (lv3 instanceof PlayerEntity || lv3 instanceof IronGolemEntity || lv3 instanceof AbstractMinecartEntity || this.hasPassengers() || lv3.hasVehicle()) {
                        lv3.pushAwayFrom(this);
                        continue;
                    }
                    lv3.startRiding(this);
                }
            }
        } else {
            for (Entity lv4 : this.getWorld().getOtherEntities(this, this.getBoundingBox().expand(0.2f, 0.0, 0.2f))) {
                if (this.hasPassenger(lv4) || !lv4.isPushable() || !(lv4 instanceof AbstractMinecartEntity)) continue;
                lv4.pushAwayFrom(this);
            }
        }
        this.updateWaterState();
        if (this.isInLava()) {
            this.setOnFireFromLava();
            this.fallDistance *= 0.5f;
        }
        this.firstUpdate = false;
    }

    protected double getMaxSpeed() {
        return (this.isTouchingWater() ? 4.0 : 8.0) / 20.0;
    }

    public void onActivatorRail(int x, int y, int z, boolean powered) {
    }

    protected void moveOffRail() {
        double d = this.getMaxSpeed();
        Vec3d lv = this.getVelocity();
        this.setVelocity(MathHelper.clamp(lv.x, -d, d), lv.y, MathHelper.clamp(lv.z, -d, d));
        if (this.isOnGround()) {
            this.setVelocity(this.getVelocity().multiply(0.5));
        }
        this.move(MovementType.SELF, this.getVelocity());
        if (!this.isOnGround()) {
            this.setVelocity(this.getVelocity().multiply(0.95));
        }
    }

    protected void moveOnRail(BlockPos pos, BlockState state) {
        double w;
        Vec3d lv9;
        double u;
        double t;
        double s;
        this.onLanding();
        double d = this.getX();
        double e = this.getY();
        double f = this.getZ();
        Vec3d lv = this.snapPositionToRail(d, e, f);
        e = pos.getY();
        boolean bl = false;
        boolean bl2 = false;
        if (state.isOf(Blocks.POWERED_RAIL)) {
            bl = state.get(PoweredRailBlock.POWERED);
            bl2 = !bl;
        }
        double g = 0.0078125;
        if (this.isTouchingWater()) {
            g *= 0.2;
        }
        Vec3d lv2 = this.getVelocity();
        RailShape lv3 = state.get(((AbstractRailBlock)state.getBlock()).getShapeProperty());
        switch (lv3) {
            case ASCENDING_EAST: {
                this.setVelocity(lv2.add(-g, 0.0, 0.0));
                e += 1.0;
                break;
            }
            case ASCENDING_WEST: {
                this.setVelocity(lv2.add(g, 0.0, 0.0));
                e += 1.0;
                break;
            }
            case ASCENDING_NORTH: {
                this.setVelocity(lv2.add(0.0, 0.0, g));
                e += 1.0;
                break;
            }
            case ASCENDING_SOUTH: {
                this.setVelocity(lv2.add(0.0, 0.0, -g));
                e += 1.0;
            }
        }
        lv2 = this.getVelocity();
        Pair<Vec3i, Vec3i> pair = AbstractMinecartEntity.getAdjacentRailPositionsByShape(lv3);
        Vec3i lv4 = pair.getFirst();
        Vec3i lv5 = pair.getSecond();
        double h = lv5.getX() - lv4.getX();
        double i = lv5.getZ() - lv4.getZ();
        double j = Math.sqrt(h * h + i * i);
        double k = lv2.x * h + lv2.z * i;
        if (k < 0.0) {
            h = -h;
            i = -i;
        }
        double l = Math.min(2.0, lv2.horizontalLength());
        lv2 = new Vec3d(l * h / j, lv2.y, l * i / j);
        this.setVelocity(lv2);
        Entity lv6 = this.getFirstPassenger();
        if (lv6 instanceof PlayerEntity) {
            Vec3d lv7 = lv6.getVelocity();
            double m = lv7.horizontalLengthSquared();
            double n = this.getVelocity().horizontalLengthSquared();
            if (m > 1.0E-4 && n < 0.01) {
                this.setVelocity(this.getVelocity().add(lv7.x * 0.1, 0.0, lv7.z * 0.1));
                bl2 = false;
            }
        }
        if (bl2) {
            double o = this.getVelocity().horizontalLength();
            if (o < 0.03) {
                this.setVelocity(Vec3d.ZERO);
            } else {
                this.setVelocity(this.getVelocity().multiply(0.5, 0.0, 0.5));
            }
        }
        double o = (double)pos.getX() + 0.5 + (double)lv4.getX() * 0.5;
        double p = (double)pos.getZ() + 0.5 + (double)lv4.getZ() * 0.5;
        double q = (double)pos.getX() + 0.5 + (double)lv5.getX() * 0.5;
        double r = (double)pos.getZ() + 0.5 + (double)lv5.getZ() * 0.5;
        h = q - o;
        i = r - p;
        if (h == 0.0) {
            s = f - (double)pos.getZ();
        } else if (i == 0.0) {
            s = d - (double)pos.getX();
        } else {
            t = d - o;
            u = f - p;
            s = (t * h + u * i) * 2.0;
        }
        d = o + h * s;
        f = p + i * s;
        this.setPosition(d, e, f);
        t = this.hasPassengers() ? 0.75 : 1.0;
        u = this.getMaxSpeed();
        lv2 = this.getVelocity();
        this.move(MovementType.SELF, new Vec3d(MathHelper.clamp(t * lv2.x, -u, u), 0.0, MathHelper.clamp(t * lv2.z, -u, u)));
        if (lv4.getY() != 0 && MathHelper.floor(this.getX()) - pos.getX() == lv4.getX() && MathHelper.floor(this.getZ()) - pos.getZ() == lv4.getZ()) {
            this.setPosition(this.getX(), this.getY() + (double)lv4.getY(), this.getZ());
        } else if (lv5.getY() != 0 && MathHelper.floor(this.getX()) - pos.getX() == lv5.getX() && MathHelper.floor(this.getZ()) - pos.getZ() == lv5.getZ()) {
            this.setPosition(this.getX(), this.getY() + (double)lv5.getY(), this.getZ());
        }
        this.applySlowdown();
        Vec3d lv8 = this.snapPositionToRail(this.getX(), this.getY(), this.getZ());
        if (lv8 != null && lv != null) {
            double v = (lv.y - lv8.y) * 0.05;
            lv9 = this.getVelocity();
            w = lv9.horizontalLength();
            if (w > 0.0) {
                this.setVelocity(lv9.multiply((w + v) / w, 1.0, (w + v) / w));
            }
            this.setPosition(this.getX(), lv8.y, this.getZ());
        }
        int x = MathHelper.floor(this.getX());
        int y = MathHelper.floor(this.getZ());
        if (x != pos.getX() || y != pos.getZ()) {
            lv9 = this.getVelocity();
            w = lv9.horizontalLength();
            this.setVelocity(w * (double)(x - pos.getX()), lv9.y, w * (double)(y - pos.getZ()));
        }
        if (bl) {
            lv9 = this.getVelocity();
            w = lv9.horizontalLength();
            if (w > 0.01) {
                double z = 0.06;
                this.setVelocity(lv9.add(lv9.x / w * 0.06, 0.0, lv9.z / w * 0.06));
            } else {
                Vec3d lv10 = this.getVelocity();
                double aa = lv10.x;
                double ab = lv10.z;
                if (lv3 == RailShape.EAST_WEST) {
                    if (this.willHitBlockAt(pos.west())) {
                        aa = 0.02;
                    } else if (this.willHitBlockAt(pos.east())) {
                        aa = -0.02;
                    }
                } else if (lv3 == RailShape.NORTH_SOUTH) {
                    if (this.willHitBlockAt(pos.north())) {
                        ab = 0.02;
                    } else if (this.willHitBlockAt(pos.south())) {
                        ab = -0.02;
                    }
                } else {
                    return;
                }
                this.setVelocity(aa, lv10.y, ab);
            }
        }
    }

    @Override
    public boolean isOnRail() {
        return this.onRail;
    }

    private boolean willHitBlockAt(BlockPos pos) {
        return this.getWorld().getBlockState(pos).isSolidBlock(this.getWorld(), pos);
    }

    protected void applySlowdown() {
        double d = this.hasPassengers() ? 0.997 : 0.96;
        Vec3d lv = this.getVelocity();
        lv = lv.multiply(d, 0.0, d);
        if (this.isTouchingWater()) {
            lv = lv.multiply(0.95f);
        }
        this.setVelocity(lv);
    }

    @Nullable
    public Vec3d snapPositionToRailWithOffset(double x, double y, double z, double offset) {
        BlockState lv;
        int i = MathHelper.floor(x);
        int j = MathHelper.floor(y);
        int k = MathHelper.floor(z);
        if (this.getWorld().getBlockState(new BlockPos(i, j - 1, k)).isIn(BlockTags.RAILS)) {
            --j;
        }
        if (AbstractRailBlock.isRail(lv = this.getWorld().getBlockState(new BlockPos(i, j, k)))) {
            RailShape lv2 = lv.get(((AbstractRailBlock)lv.getBlock()).getShapeProperty());
            y = j;
            if (lv2.isAscending()) {
                y = j + 1;
            }
            Pair<Vec3i, Vec3i> pair = AbstractMinecartEntity.getAdjacentRailPositionsByShape(lv2);
            Vec3i lv3 = pair.getFirst();
            Vec3i lv4 = pair.getSecond();
            double h = lv4.getX() - lv3.getX();
            double l = lv4.getZ() - lv3.getZ();
            double m = Math.sqrt(h * h + l * l);
            if (lv3.getY() != 0 && MathHelper.floor(x += (h /= m) * offset) - i == lv3.getX() && MathHelper.floor(z += (l /= m) * offset) - k == lv3.getZ()) {
                y += (double)lv3.getY();
            } else if (lv4.getY() != 0 && MathHelper.floor(x) - i == lv4.getX() && MathHelper.floor(z) - k == lv4.getZ()) {
                y += (double)lv4.getY();
            }
            return this.snapPositionToRail(x, y, z);
        }
        return null;
    }

    @Nullable
    public Vec3d snapPositionToRail(double x, double y, double z) {
        BlockState lv;
        int i = MathHelper.floor(x);
        int j = MathHelper.floor(y);
        int k = MathHelper.floor(z);
        if (this.getWorld().getBlockState(new BlockPos(i, j - 1, k)).isIn(BlockTags.RAILS)) {
            --j;
        }
        if (AbstractRailBlock.isRail(lv = this.getWorld().getBlockState(new BlockPos(i, j, k)))) {
            double s;
            RailShape lv2 = lv.get(((AbstractRailBlock)lv.getBlock()).getShapeProperty());
            Pair<Vec3i, Vec3i> pair = AbstractMinecartEntity.getAdjacentRailPositionsByShape(lv2);
            Vec3i lv3 = pair.getFirst();
            Vec3i lv4 = pair.getSecond();
            double g = (double)i + 0.5 + (double)lv3.getX() * 0.5;
            double h = (double)j + 0.0625 + (double)lv3.getY() * 0.5;
            double l = (double)k + 0.5 + (double)lv3.getZ() * 0.5;
            double m = (double)i + 0.5 + (double)lv4.getX() * 0.5;
            double n = (double)j + 0.0625 + (double)lv4.getY() * 0.5;
            double o = (double)k + 0.5 + (double)lv4.getZ() * 0.5;
            double p = m - g;
            double q = (n - h) * 2.0;
            double r = o - l;
            if (p == 0.0) {
                s = z - (double)k;
            } else if (r == 0.0) {
                s = x - (double)i;
            } else {
                double t = x - g;
                double u = z - l;
                s = (t * p + u * r) * 2.0;
            }
            x = g + p * s;
            y = h + q * s;
            z = l + r * s;
            if (q < 0.0) {
                y += 1.0;
            } else if (q > 0.0) {
                y += 0.5;
            }
            return new Vec3d(x, y, z);
        }
        return null;
    }

    @Override
    public Box getVisibilityBoundingBox() {
        Box lv = this.getBoundingBox();
        if (this.hasCustomBlock()) {
            return lv.expand((double)Math.abs(this.getBlockOffset()) / 16.0);
        }
        return lv;
    }

    @Override
    protected void readCustomDataFromNbt(NbtCompound nbt) {
        if (nbt.getBoolean("CustomDisplayTile")) {
            this.setCustomBlock(NbtHelper.toBlockState(this.getWorld().createCommandRegistryWrapper(RegistryKeys.BLOCK), nbt.getCompound("DisplayState")));
            this.setCustomBlockOffset(nbt.getInt("DisplayOffset"));
        }
    }

    @Override
    protected void writeCustomDataToNbt(NbtCompound nbt) {
        if (this.hasCustomBlock()) {
            nbt.putBoolean("CustomDisplayTile", true);
            nbt.put("DisplayState", NbtHelper.fromBlockState(this.getContainedBlock()));
            nbt.putInt("DisplayOffset", this.getBlockOffset());
        }
    }

    @Override
    public void pushAwayFrom(Entity entity) {
        double e;
        if (this.getWorld().isClient) {
            return;
        }
        if (entity.noClip || this.noClip) {
            return;
        }
        if (this.hasPassenger(entity)) {
            return;
        }
        double d = entity.getX() - this.getX();
        double f = d * d + (e = entity.getZ() - this.getZ()) * e;
        if (f >= (double)1.0E-4f) {
            f = Math.sqrt(f);
            d /= f;
            e /= f;
            double g = 1.0 / f;
            if (g > 1.0) {
                g = 1.0;
            }
            d *= g;
            e *= g;
            d *= (double)0.1f;
            e *= (double)0.1f;
            d *= 0.5;
            e *= 0.5;
            if (entity instanceof AbstractMinecartEntity) {
                Vec3d lv2;
                double i;
                double h = entity.getX() - this.getX();
                Vec3d lv = new Vec3d(h, 0.0, i = entity.getZ() - this.getZ()).normalize();
                double j = Math.abs(lv.dotProduct(lv2 = new Vec3d(MathHelper.cos(this.getYaw() * ((float)Math.PI / 180)), 0.0, MathHelper.sin(this.getYaw() * ((float)Math.PI / 180))).normalize()));
                if (j < (double)0.8f) {
                    return;
                }
                Vec3d lv3 = this.getVelocity();
                Vec3d lv4 = entity.getVelocity();
                if (((AbstractMinecartEntity)entity).getMinecartType() == Type.FURNACE && this.getMinecartType() != Type.FURNACE) {
                    this.setVelocity(lv3.multiply(0.2, 1.0, 0.2));
                    this.addVelocity(lv4.x - d, 0.0, lv4.z - e);
                    entity.setVelocity(lv4.multiply(0.95, 1.0, 0.95));
                } else if (((AbstractMinecartEntity)entity).getMinecartType() != Type.FURNACE && this.getMinecartType() == Type.FURNACE) {
                    entity.setVelocity(lv4.multiply(0.2, 1.0, 0.2));
                    entity.addVelocity(lv3.x + d, 0.0, lv3.z + e);
                    this.setVelocity(lv3.multiply(0.95, 1.0, 0.95));
                } else {
                    double k = (lv4.x + lv3.x) / 2.0;
                    double l = (lv4.z + lv3.z) / 2.0;
                    this.setVelocity(lv3.multiply(0.2, 1.0, 0.2));
                    this.addVelocity(k - d, 0.0, l - e);
                    entity.setVelocity(lv4.multiply(0.2, 1.0, 0.2));
                    entity.addVelocity(k + d, 0.0, l + e);
                }
            } else {
                this.addVelocity(-d, 0.0, -e);
                entity.addVelocity(d / 4.0, 0.0, e / 4.0);
            }
        }
    }

    @Override
    public void updateTrackedPositionAndAngles(double x, double y, double z, float yaw, float pitch, int interpolationSteps) {
        this.clientX = x;
        this.clientY = y;
        this.clientZ = z;
        this.clientYaw = yaw;
        this.clientPitch = pitch;
        this.clientInterpolationSteps = interpolationSteps + 2;
        this.setVelocity(this.clientVelocity);
    }

    @Override
    public double getLerpTargetX() {
        return this.clientInterpolationSteps > 0 ? this.clientX : this.getX();
    }

    @Override
    public double getLerpTargetY() {
        return this.clientInterpolationSteps > 0 ? this.clientY : this.getY();
    }

    @Override
    public double getLerpTargetZ() {
        return this.clientInterpolationSteps > 0 ? this.clientZ : this.getZ();
    }

    @Override
    public float getLerpTargetPitch() {
        return this.clientInterpolationSteps > 0 ? (float)this.clientPitch : this.getPitch();
    }

    @Override
    public float getLerpTargetYaw() {
        return this.clientInterpolationSteps > 0 ? (float)this.clientYaw : this.getYaw();
    }

    @Override
    public void setVelocityClient(double x, double y, double z) {
        this.clientVelocity = new Vec3d(x, y, z);
        this.setVelocity(this.clientVelocity);
    }

    public abstract Type getMinecartType();

    public BlockState getContainedBlock() {
        if (!this.hasCustomBlock()) {
            return this.getDefaultContainedBlock();
        }
        return Block.getStateFromRawId(this.getDataTracker().get(CUSTOM_BLOCK_ID));
    }

    public BlockState getDefaultContainedBlock() {
        return Blocks.AIR.getDefaultState();
    }

    public int getBlockOffset() {
        if (!this.hasCustomBlock()) {
            return this.getDefaultBlockOffset();
        }
        return this.getDataTracker().get(CUSTOM_BLOCK_OFFSET);
    }

    public int getDefaultBlockOffset() {
        return 6;
    }

    public void setCustomBlock(BlockState state) {
        this.getDataTracker().set(CUSTOM_BLOCK_ID, Block.getRawIdFromState(state));
        this.setCustomBlockPresent(true);
    }

    public void setCustomBlockOffset(int offset) {
        this.getDataTracker().set(CUSTOM_BLOCK_OFFSET, offset);
        this.setCustomBlockPresent(true);
    }

    public boolean hasCustomBlock() {
        return this.getDataTracker().get(CUSTOM_BLOCK_PRESENT);
    }

    public void setCustomBlockPresent(boolean present) {
        this.getDataTracker().set(CUSTOM_BLOCK_PRESENT, present);
    }

    @Override
    public ItemStack getPickBlockStack() {
        return new ItemStack(switch (this.getMinecartType().ordinal()) {
            case 2 -> Items.FURNACE_MINECART;
            case 1 -> Items.CHEST_MINECART;
            case 3 -> Items.TNT_MINECART;
            case 5 -> Items.HOPPER_MINECART;
            case 6 -> Items.COMMAND_BLOCK_MINECART;
            default -> Items.MINECART;
        });
    }

    public static enum Type {
        RIDEABLE,
        CHEST,
        FURNACE,
        TNT,
        SPAWNER,
        HOPPER,
        COMMAND_BLOCK;

    }
}

