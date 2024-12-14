/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 * 
 * Could not load the following classes:
 *  org.jetbrains.annotations.Nullable
 */
package net.minecraft.entity.projectile;

import com.google.common.collect.Lists;
import it.unimi.dsi.fastutil.ints.IntOpenHashSet;
import java.lang.runtime.SwitchBootstraps;
import java.util.Arrays;
import java.util.List;
import net.minecraft.advancement.criterion.Criteria;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.MovementType;
import net.minecraft.entity.OminousItemSpawnerEntity;
import net.minecraft.entity.ProjectileDeflection;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.data.TrackedData;
import net.minecraft.entity.data.TrackedDataHandlerRegistry;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.ProjectileEntity;
import net.minecraft.entity.projectile.ProjectileUtil;
import net.minecraft.inventory.StackReference;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtHelper;
import net.minecraft.network.packet.s2c.play.GameStateChangeS2CPacket;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.registry.Registries;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.tag.EntityTypeTags;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundEvent;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.Identifier;
import net.minecraft.util.Unit;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.world.RaycastContext;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

public abstract class PersistentProjectileEntity
extends ProjectileEntity {
    private static final double field_30657 = 2.0;
    private static final TrackedData<Byte> PROJECTILE_FLAGS = DataTracker.registerData(PersistentProjectileEntity.class, TrackedDataHandlerRegistry.BYTE);
    private static final TrackedData<Byte> PIERCE_LEVEL = DataTracker.registerData(PersistentProjectileEntity.class, TrackedDataHandlerRegistry.BYTE);
    private static final int CRITICAL_FLAG = 1;
    private static final int NO_CLIP_FLAG = 2;
    @Nullable
    private BlockState inBlockState;
    protected boolean inGround;
    protected int inGroundTime;
    public PickupPermission pickupType = PickupPermission.DISALLOWED;
    public int shake;
    private int life;
    private double damage = 2.0;
    private SoundEvent sound = this.getHitSound();
    @Nullable
    private IntOpenHashSet piercedEntities;
    @Nullable
    private List<Entity> piercingKilledEntities;
    private ItemStack stack = this.getDefaultItemStack();
    @Nullable
    private ItemStack weapon = null;

    protected PersistentProjectileEntity(EntityType<? extends PersistentProjectileEntity> arg, World arg2) {
        super((EntityType<? extends ProjectileEntity>)arg, arg2);
    }

    protected PersistentProjectileEntity(EntityType<? extends PersistentProjectileEntity> type, double x, double y, double z, World world, ItemStack stack, @Nullable ItemStack weapon) {
        this(type, world);
        this.stack = stack.copy();
        this.setCustomName(stack.get(DataComponentTypes.CUSTOM_NAME));
        Unit lv = stack.remove(DataComponentTypes.INTANGIBLE_PROJECTILE);
        if (lv != null) {
            this.pickupType = PickupPermission.CREATIVE_ONLY;
        }
        this.setPosition(x, y, z);
        if (weapon != null && world instanceof ServerWorld) {
            ServerWorld lv2 = (ServerWorld)world;
            if (weapon.isEmpty()) {
                throw new IllegalArgumentException("Invalid weapon firing an arrow");
            }
            this.weapon = weapon.copy();
            int i = EnchantmentHelper.getProjectilePiercing(lv2, weapon, this.stack);
            if (i > 0) {
                this.setPierceLevel((byte)i);
            }
            EnchantmentHelper.onProjectileSpawned(lv2, weapon, this, item -> {
                this.weapon = null;
            });
        }
    }

    protected PersistentProjectileEntity(EntityType<? extends PersistentProjectileEntity> type, LivingEntity owner, World world, ItemStack stack, @Nullable ItemStack shotFrom) {
        this(type, owner.getX(), owner.getEyeY() - (double)0.1f, owner.getZ(), world, stack, shotFrom);
        this.setOwner(owner);
    }

    public void setSound(SoundEvent sound) {
        this.sound = sound;
    }

    @Override
    public boolean shouldRender(double distance) {
        double e = this.getBoundingBox().getAverageSideLength() * 10.0;
        if (Double.isNaN(e)) {
            e = 1.0;
        }
        return distance < (e *= 64.0 * PersistentProjectileEntity.getRenderDistanceMultiplier()) * e;
    }

    @Override
    protected void initDataTracker(DataTracker.Builder builder) {
        builder.add(PROJECTILE_FLAGS, (byte)0);
        builder.add(PIERCE_LEVEL, (byte)0);
    }

    @Override
    public void setVelocity(double x, double y, double z, float power, float uncertainty) {
        super.setVelocity(x, y, z, power, uncertainty);
        this.life = 0;
    }

    @Override
    public void updateTrackedPositionAndAngles(double x, double y, double z, float yaw, float pitch, int interpolationSteps) {
        this.setPosition(x, y, z);
        this.setRotation(yaw, pitch);
    }

    @Override
    public void setVelocityClient(double x, double y, double z) {
        super.setVelocityClient(x, y, z);
        this.life = 0;
    }

    @Override
    public void tick() {
        Vec3d lv5;
        VoxelShape lv4;
        super.tick();
        boolean bl = this.isNoClip();
        Vec3d lv = this.getVelocity();
        if (this.prevPitch == 0.0f && this.prevYaw == 0.0f) {
            double d = lv.horizontalLength();
            this.setYaw((float)(MathHelper.atan2(lv.x, lv.z) * 57.2957763671875));
            this.setPitch((float)(MathHelper.atan2(lv.y, d) * 57.2957763671875));
            this.prevYaw = this.getYaw();
            this.prevPitch = this.getPitch();
        }
        BlockPos lv2 = this.getBlockPos();
        BlockState lv3 = this.getWorld().getBlockState(lv2);
        if (!(lv3.isAir() || bl || (lv4 = lv3.getCollisionShape(this.getWorld(), lv2)).isEmpty())) {
            lv5 = this.getPos();
            for (Box lv6 : lv4.getBoundingBoxes()) {
                if (!lv6.offset(lv2).contains(lv5)) continue;
                this.inGround = true;
                break;
            }
        }
        if (this.shake > 0) {
            --this.shake;
        }
        if (this.isTouchingWaterOrRain() || lv3.isOf(Blocks.POWDER_SNOW)) {
            this.extinguish();
        }
        if (this.inGround && !bl) {
            if (this.inBlockState != lv3 && this.shouldFall()) {
                this.fall();
            } else if (!this.getWorld().isClient) {
                this.age();
            }
            ++this.inGroundTime;
            return;
        }
        this.inGroundTime = 0;
        Vec3d lv7 = this.getPos();
        lv5 = lv7.add(lv);
        HitResult lv8 = this.getWorld().raycast(new RaycastContext(lv7, lv5, RaycastContext.ShapeType.COLLIDER, RaycastContext.FluidHandling.NONE, this));
        if (lv8.getType() != HitResult.Type.MISS) {
            lv5 = lv8.getPos();
        }
        while (!this.isRemoved()) {
            EntityHitResult lv9 = this.getEntityCollision(lv7, lv5);
            if (lv9 != null) {
                lv8 = lv9;
            }
            if (lv8 != null && lv8.getType() == HitResult.Type.ENTITY) {
                Entity lv10 = ((EntityHitResult)lv8).getEntity();
                Entity lv11 = this.getOwner();
                if (lv10 instanceof PlayerEntity && lv11 instanceof PlayerEntity && !((PlayerEntity)lv11).shouldDamagePlayer((PlayerEntity)lv10)) {
                    lv8 = null;
                    lv9 = null;
                }
            }
            if (lv8 != null && !bl) {
                ProjectileDeflection lv12 = this.hitOrDeflect(lv8);
                this.velocityDirty = true;
                if (lv12 != ProjectileDeflection.NONE) break;
            }
            if (lv9 == null || this.getPierceLevel() <= 0) break;
            lv8 = null;
        }
        lv = this.getVelocity();
        double e = lv.x;
        double f = lv.y;
        double g = lv.z;
        if (this.isCritical()) {
            for (int i = 0; i < 4; ++i) {
                this.getWorld().addParticle(ParticleTypes.CRIT, this.getX() + e * (double)i / 4.0, this.getY() + f * (double)i / 4.0, this.getZ() + g * (double)i / 4.0, -e, -f + 0.2, -g);
            }
        }
        double h = this.getX() + e;
        double j = this.getY() + f;
        double k = this.getZ() + g;
        double l = lv.horizontalLength();
        if (bl) {
            this.setYaw((float)(MathHelper.atan2(-e, -g) * 57.2957763671875));
        } else {
            this.setYaw((float)(MathHelper.atan2(e, g) * 57.2957763671875));
        }
        this.setPitch((float)(MathHelper.atan2(f, l) * 57.2957763671875));
        this.setPitch(PersistentProjectileEntity.updateRotation(this.prevPitch, this.getPitch()));
        this.setYaw(PersistentProjectileEntity.updateRotation(this.prevYaw, this.getYaw()));
        float m = 0.99f;
        if (this.isTouchingWater()) {
            for (int n = 0; n < 4; ++n) {
                float o = 0.25f;
                this.getWorld().addParticle(ParticleTypes.BUBBLE, h - e * 0.25, j - f * 0.25, k - g * 0.25, e, f, g);
            }
            m = this.getDragInWater();
        }
        this.setVelocity(lv.multiply(m));
        if (!bl) {
            this.applyGravity();
        }
        this.setPosition(h, j, k);
        this.checkBlockCollision();
    }

    @Override
    protected double getGravity() {
        return 0.05;
    }

    private boolean shouldFall() {
        return this.inGround && this.getWorld().isSpaceEmpty(new Box(this.getPos(), this.getPos()).expand(0.06));
    }

    private void fall() {
        this.inGround = false;
        Vec3d lv = this.getVelocity();
        this.setVelocity(lv.multiply(this.random.nextFloat() * 0.2f, this.random.nextFloat() * 0.2f, this.random.nextFloat() * 0.2f));
        this.life = 0;
    }

    @Override
    public void move(MovementType movementType, Vec3d movement) {
        super.move(movementType, movement);
        if (movementType != MovementType.SELF && this.shouldFall()) {
            this.fall();
        }
    }

    protected void age() {
        ++this.life;
        if (this.life >= 1200) {
            this.discard();
        }
    }

    private void clearPiercingStatus() {
        if (this.piercingKilledEntities != null) {
            this.piercingKilledEntities.clear();
        }
        if (this.piercedEntities != null) {
            this.piercedEntities.clear();
        }
    }

    @Override
    protected void onEntityHit(EntityHitResult entityHitResult) {
        World world;
        super.onEntityHit(entityHitResult);
        Entity lv = entityHitResult.getEntity();
        float f = (float)this.getVelocity().length();
        double d = this.damage;
        Entity lv2 = this.getOwner();
        DamageSource lv3 = this.getDamageSources().arrow(this, lv2 != null ? lv2 : this);
        if (this.getWeaponStack() != null && (world = this.getWorld()) instanceof ServerWorld) {
            ServerWorld lv4 = (ServerWorld)world;
            d = EnchantmentHelper.getDamage(lv4, this.getWeaponStack(), lv, lv3, (float)d);
        }
        int i = MathHelper.ceil(MathHelper.clamp((double)f * d, 0.0, 2.147483647E9));
        if (this.getPierceLevel() > 0) {
            if (this.piercedEntities == null) {
                this.piercedEntities = new IntOpenHashSet(5);
            }
            if (this.piercingKilledEntities == null) {
                this.piercingKilledEntities = Lists.newArrayListWithCapacity(5);
            }
            if (this.piercedEntities.size() < this.getPierceLevel() + 1) {
                this.piercedEntities.add(lv.getId());
            } else {
                this.discard();
                return;
            }
        }
        if (this.isCritical()) {
            long l = this.random.nextInt(i / 2 + 2);
            i = (int)Math.min(l + (long)i, Integer.MAX_VALUE);
        }
        if (lv2 instanceof LivingEntity) {
            LivingEntity lv5 = (LivingEntity)lv2;
            lv5.onAttacking(lv);
        }
        boolean bl = lv.getType() == EntityType.ENDERMAN;
        int j = lv.getFireTicks();
        if (this.isOnFire() && !bl) {
            lv.setOnFireFor(5.0f);
        }
        if (lv.damage(lv3, i)) {
            if (bl) {
                return;
            }
            if (lv instanceof LivingEntity) {
                LivingEntity lv6 = (LivingEntity)lv;
                if (!this.getWorld().isClient && this.getPierceLevel() <= 0) {
                    lv6.setStuckArrowCount(lv6.getStuckArrowCount() + 1);
                }
                this.knockback(lv6, lv3);
                World world2 = this.getWorld();
                if (world2 instanceof ServerWorld) {
                    ServerWorld lv7 = (ServerWorld)world2;
                    EnchantmentHelper.onTargetDamaged(lv7, lv6, lv3, this.getWeaponStack());
                }
                this.onHit(lv6);
                if (lv6 != lv2 && lv6 instanceof PlayerEntity && lv2 instanceof ServerPlayerEntity && !this.isSilent()) {
                    ((ServerPlayerEntity)lv2).networkHandler.sendPacket(new GameStateChangeS2CPacket(GameStateChangeS2CPacket.PROJECTILE_HIT_PLAYER, GameStateChangeS2CPacket.field_33328));
                }
                if (!lv.isAlive() && this.piercingKilledEntities != null) {
                    this.piercingKilledEntities.add(lv6);
                }
                if (!this.getWorld().isClient && lv2 instanceof ServerPlayerEntity) {
                    ServerPlayerEntity lv8 = (ServerPlayerEntity)lv2;
                    if (this.piercingKilledEntities != null && this.isShotFromCrossbow()) {
                        Criteria.KILLED_BY_CROSSBOW.trigger(lv8, this.piercingKilledEntities);
                    } else if (!lv.isAlive() && this.isShotFromCrossbow()) {
                        Criteria.KILLED_BY_CROSSBOW.trigger(lv8, Arrays.asList(lv));
                    }
                }
            }
            this.playSound(this.sound, 1.0f, 1.2f / (this.random.nextFloat() * 0.2f + 0.9f));
            if (this.getPierceLevel() <= 0) {
                this.discard();
            }
        } else {
            lv.setFireTicks(j);
            this.deflect(ProjectileDeflection.SIMPLE, lv, this.getOwner(), false);
            this.setVelocity(this.getVelocity().multiply(0.2));
            if (!this.getWorld().isClient && this.getVelocity().lengthSquared() < 1.0E-7) {
                if (this.pickupType == PickupPermission.ALLOWED) {
                    this.dropStack(this.asItemStack(), 0.1f);
                }
                this.discard();
            }
        }
    }

    protected void knockback(LivingEntity target, DamageSource source) {
        float f;
        World world;
        if (this.weapon != null && (world = this.getWorld()) instanceof ServerWorld) {
            ServerWorld lv = (ServerWorld)world;
            f = EnchantmentHelper.modifyKnockback(lv, this.weapon, target, source, 0.0f);
        } else {
            f = 0.0f;
        }
        double d = f;
        if (d > 0.0) {
            double e = Math.max(0.0, 1.0 - target.getAttributeValue(EntityAttributes.GENERIC_KNOCKBACK_RESISTANCE));
            Vec3d lv2 = this.getVelocity().multiply(1.0, 0.0, 1.0).normalize().multiply(d * 0.6 * e);
            if (lv2.lengthSquared() > 0.0) {
                target.addVelocity(lv2.x, 0.1, lv2.z);
            }
        }
    }

    @Override
    protected void onBlockHit(BlockHitResult blockHitResult) {
        this.inBlockState = this.getWorld().getBlockState(blockHitResult.getBlockPos());
        super.onBlockHit(blockHitResult);
        Vec3d lv = blockHitResult.getPos().subtract(this.getX(), this.getY(), this.getZ());
        this.setVelocity(lv);
        ItemStack lv2 = this.getWeaponStack();
        World world = this.getWorld();
        if (world instanceof ServerWorld) {
            ServerWorld lv3 = (ServerWorld)world;
            if (lv2 != null) {
                this.onBlockHitEnchantmentEffects(lv3, blockHitResult, lv2);
            }
        }
        Vec3d lv4 = lv.normalize().multiply(0.05f);
        this.setPos(this.getX() - lv4.x, this.getY() - lv4.y, this.getZ() - lv4.z);
        this.playSound(this.getSound(), 1.0f, 1.2f / (this.random.nextFloat() * 0.2f + 0.9f));
        this.inGround = true;
        this.shake = 7;
        this.setCritical(false);
        this.setPierceLevel((byte)0);
        this.setSound(SoundEvents.ENTITY_ARROW_HIT);
        this.clearPiercingStatus();
    }

    protected void onBlockHitEnchantmentEffects(ServerWorld world, BlockHitResult blockHitResult, ItemStack weaponStack) {
        LivingEntity lv2;
        Vec3d lv = blockHitResult.getBlockPos().clampToWithin(blockHitResult.getPos());
        Entity entity = this.getOwner();
        EnchantmentHelper.onHitBlock(world, weaponStack, entity instanceof LivingEntity ? (lv2 = (LivingEntity)entity) : null, this, null, lv, world.getBlockState(blockHitResult.getBlockPos()), item -> {
            this.weapon = null;
        });
    }

    @Override
    public ItemStack getWeaponStack() {
        return this.weapon;
    }

    protected SoundEvent getHitSound() {
        return SoundEvents.ENTITY_ARROW_HIT;
    }

    protected final SoundEvent getSound() {
        return this.sound;
    }

    protected void onHit(LivingEntity target) {
    }

    @Nullable
    protected EntityHitResult getEntityCollision(Vec3d currentPosition, Vec3d nextPosition) {
        return ProjectileUtil.getEntityCollision(this.getWorld(), this, currentPosition, nextPosition, this.getBoundingBox().stretch(this.getVelocity()).expand(1.0), this::canHit);
    }

    @Override
    protected boolean canHit(Entity entity) {
        return super.canHit(entity) && (this.piercedEntities == null || !this.piercedEntities.contains(entity.getId()));
    }

    @Override
    public void writeCustomDataToNbt(NbtCompound nbt) {
        super.writeCustomDataToNbt(nbt);
        nbt.putShort("life", (short)this.life);
        if (this.inBlockState != null) {
            nbt.put("inBlockState", NbtHelper.fromBlockState(this.inBlockState));
        }
        nbt.putByte("shake", (byte)this.shake);
        nbt.putBoolean("inGround", this.inGround);
        nbt.putByte("pickup", (byte)this.pickupType.ordinal());
        nbt.putDouble("damage", this.damage);
        nbt.putBoolean("crit", this.isCritical());
        nbt.putByte("PierceLevel", this.getPierceLevel());
        nbt.putString("SoundEvent", Registries.SOUND_EVENT.getId(this.sound).toString());
        nbt.put("item", this.stack.encode(this.getRegistryManager()));
        if (this.weapon != null) {
            nbt.put("weapon", this.weapon.encode(this.getRegistryManager(), new NbtCompound()));
        }
    }

    @Override
    public void readCustomDataFromNbt(NbtCompound nbt) {
        super.readCustomDataFromNbt(nbt);
        this.life = nbt.getShort("life");
        if (nbt.contains("inBlockState", NbtElement.COMPOUND_TYPE)) {
            this.inBlockState = NbtHelper.toBlockState(this.getWorld().createCommandRegistryWrapper(RegistryKeys.BLOCK), nbt.getCompound("inBlockState"));
        }
        this.shake = nbt.getByte("shake") & 0xFF;
        this.inGround = nbt.getBoolean("inGround");
        if (nbt.contains("damage", NbtElement.NUMBER_TYPE)) {
            this.damage = nbt.getDouble("damage");
        }
        this.pickupType = PickupPermission.fromOrdinal(nbt.getByte("pickup"));
        this.setCritical(nbt.getBoolean("crit"));
        this.setPierceLevel(nbt.getByte("PierceLevel"));
        if (nbt.contains("SoundEvent", NbtElement.STRING_TYPE)) {
            this.sound = Registries.SOUND_EVENT.getOrEmpty(Identifier.of(nbt.getString("SoundEvent"))).orElse(this.getHitSound());
        }
        if (nbt.contains("item", NbtElement.COMPOUND_TYPE)) {
            this.setStack(ItemStack.fromNbt(this.getRegistryManager(), nbt.getCompound("item")).orElse(this.getDefaultItemStack()));
        } else {
            this.setStack(this.getDefaultItemStack());
        }
        this.weapon = nbt.contains("weapon", NbtElement.COMPOUND_TYPE) ? (ItemStack)ItemStack.fromNbt(this.getRegistryManager(), nbt.getCompound("weapon")).orElse(null) : null;
    }

    @Override
    public void setOwner(@Nullable Entity entity) {
        PickupPermission pickupPermission;
        super.setOwner(entity);
        Entity entity2 = entity;
        int n = 0;
        block4: while (true) {
            switch (SwitchBootstraps.typeSwitch("typeSwitch", new Object[]{PlayerEntity.class, OminousItemSpawnerEntity.class}, (Object)entity2, n)) {
                case 0: {
                    PlayerEntity lv = (PlayerEntity)entity2;
                    if (this.pickupType != PickupPermission.DISALLOWED) {
                        n = 1;
                        continue block4;
                    }
                    pickupPermission = PickupPermission.ALLOWED;
                    break block4;
                }
                case 1: {
                    OminousItemSpawnerEntity lv2 = (OminousItemSpawnerEntity)entity2;
                    pickupPermission = PickupPermission.DISALLOWED;
                    break block4;
                }
                default: {
                    pickupPermission = this.pickupType;
                    break block4;
                }
            }
            break;
        }
        this.pickupType = pickupPermission;
    }

    @Override
    public void onPlayerCollision(PlayerEntity player) {
        if (this.getWorld().isClient || !this.inGround && !this.isNoClip() || this.shake > 0) {
            return;
        }
        if (this.tryPickup(player)) {
            player.sendPickup(this, 1);
            this.discard();
        }
    }

    protected boolean tryPickup(PlayerEntity player) {
        return switch (this.pickupType.ordinal()) {
            default -> throw new MatchException(null, null);
            case 0 -> false;
            case 1 -> player.getInventory().insertStack(this.asItemStack());
            case 2 -> player.isInCreativeMode();
        };
    }

    protected ItemStack asItemStack() {
        return this.stack.copy();
    }

    protected abstract ItemStack getDefaultItemStack();

    @Override
    protected Entity.MoveEffect getMoveEffect() {
        return Entity.MoveEffect.NONE;
    }

    public ItemStack getItemStack() {
        return this.stack;
    }

    public void setDamage(double damage) {
        this.damage = damage;
    }

    public double getDamage() {
        return this.damage;
    }

    @Override
    public boolean isAttackable() {
        return this.getType().isIn(EntityTypeTags.REDIRECTABLE_PROJECTILE);
    }

    public void setCritical(boolean critical) {
        this.setProjectileFlag(CRITICAL_FLAG, critical);
    }

    private void setPierceLevel(byte level) {
        this.dataTracker.set(PIERCE_LEVEL, level);
    }

    private void setProjectileFlag(int index, boolean flag) {
        byte b = this.dataTracker.get(PROJECTILE_FLAGS);
        if (flag) {
            this.dataTracker.set(PROJECTILE_FLAGS, (byte)(b | index));
        } else {
            this.dataTracker.set(PROJECTILE_FLAGS, (byte)(b & ~index));
        }
    }

    protected void setStack(ItemStack stack) {
        this.stack = !stack.isEmpty() ? stack : this.getDefaultItemStack();
    }

    public boolean isCritical() {
        byte b = this.dataTracker.get(PROJECTILE_FLAGS);
        return (b & 1) != 0;
    }

    public boolean isShotFromCrossbow() {
        return this.weapon != null && this.weapon.isOf(Items.CROSSBOW);
    }

    public byte getPierceLevel() {
        return this.dataTracker.get(PIERCE_LEVEL);
    }

    public void applyDamageModifier(float damageModifier) {
        this.setDamage((double)(damageModifier * 2.0f) + this.random.nextTriangular((double)this.getWorld().getDifficulty().getId() * 0.11, 0.57425));
    }

    protected float getDragInWater() {
        return 0.6f;
    }

    public void setNoClip(boolean noClip) {
        this.noClip = noClip;
        this.setProjectileFlag(NO_CLIP_FLAG, noClip);
    }

    public boolean isNoClip() {
        if (!this.getWorld().isClient) {
            return this.noClip;
        }
        return (this.dataTracker.get(PROJECTILE_FLAGS) & 2) != 0;
    }

    @Override
    public boolean canHit() {
        return super.canHit() && !this.inGround;
    }

    @Override
    public StackReference getStackReference(int mappedIndex) {
        if (mappedIndex == 0) {
            return StackReference.of(this::getItemStack, this::setStack);
        }
        return super.getStackReference(mappedIndex);
    }

    public static enum PickupPermission {
        DISALLOWED,
        ALLOWED,
        CREATIVE_ONLY;


        public static PickupPermission fromOrdinal(int ordinal) {
            if (ordinal < 0 || ordinal > PickupPermission.values().length) {
                ordinal = 0;
            }
            return PickupPermission.values()[ordinal];
        }
    }
}

