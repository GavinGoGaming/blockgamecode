/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 * 
 * Could not load the following classes:
 *  org.jetbrains.annotations.Nullable
 */
package net.minecraft.entity.decoration;

import java.util.List;
import java.util.function.Predicate;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.block.piston.PistonBehavior;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityDimensions;
import net.minecraft.entity.EntityPose;
import net.minecraft.entity.EntityStatuses;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.LightningEntity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.attribute.DefaultAttributeContainer;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.data.TrackedData;
import net.minecraft.entity.data.TrackedDataHandlerRegistry;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.vehicle.AbstractMinecartEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtList;
import net.minecraft.particle.BlockStateParticleEffect;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.registry.tag.DamageTypeTags;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundEvent;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Arm;
import net.minecraft.util.Hand;
import net.minecraft.util.collection.DefaultedList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.EulerAngle;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.LightType;
import net.minecraft.world.World;
import net.minecraft.world.event.GameEvent;
import net.minecraft.world.explosion.Explosion;
import org.jetbrains.annotations.Nullable;

public class ArmorStandEntity
extends LivingEntity {
    public static final int field_30443 = 5;
    private static final boolean field_30445 = true;
    private static final EulerAngle DEFAULT_HEAD_ROTATION = new EulerAngle(0.0f, 0.0f, 0.0f);
    private static final EulerAngle DEFAULT_BODY_ROTATION = new EulerAngle(0.0f, 0.0f, 0.0f);
    private static final EulerAngle DEFAULT_LEFT_ARM_ROTATION = new EulerAngle(-10.0f, 0.0f, -10.0f);
    private static final EulerAngle DEFAULT_RIGHT_ARM_ROTATION = new EulerAngle(-15.0f, 0.0f, 10.0f);
    private static final EulerAngle DEFAULT_LEFT_LEG_ROTATION = new EulerAngle(-1.0f, 0.0f, -1.0f);
    private static final EulerAngle DEFAULT_RIGHT_LEG_ROTATION = new EulerAngle(1.0f, 0.0f, 1.0f);
    private static final EntityDimensions MARKER_DIMENSIONS = EntityDimensions.fixed(0.0f, 0.0f);
    private static final EntityDimensions SMALL_DIMENSIONS = EntityType.ARMOR_STAND.getDimensions().scaled(0.5f).withEyeHeight(0.9875f);
    private static final double field_30447 = 0.1;
    private static final double field_30448 = 0.9;
    private static final double field_30449 = 0.4;
    private static final double field_30450 = 1.6;
    public static final int field_30446 = 8;
    public static final int field_30451 = 16;
    public static final int SMALL_FLAG = 1;
    public static final int SHOW_ARMS_FLAG = 4;
    public static final int HIDE_BASE_PLATE_FLAG = 8;
    public static final int MARKER_FLAG = 16;
    public static final TrackedData<Byte> ARMOR_STAND_FLAGS = DataTracker.registerData(ArmorStandEntity.class, TrackedDataHandlerRegistry.BYTE);
    public static final TrackedData<EulerAngle> TRACKER_HEAD_ROTATION = DataTracker.registerData(ArmorStandEntity.class, TrackedDataHandlerRegistry.ROTATION);
    public static final TrackedData<EulerAngle> TRACKER_BODY_ROTATION = DataTracker.registerData(ArmorStandEntity.class, TrackedDataHandlerRegistry.ROTATION);
    public static final TrackedData<EulerAngle> TRACKER_LEFT_ARM_ROTATION = DataTracker.registerData(ArmorStandEntity.class, TrackedDataHandlerRegistry.ROTATION);
    public static final TrackedData<EulerAngle> TRACKER_RIGHT_ARM_ROTATION = DataTracker.registerData(ArmorStandEntity.class, TrackedDataHandlerRegistry.ROTATION);
    public static final TrackedData<EulerAngle> TRACKER_LEFT_LEG_ROTATION = DataTracker.registerData(ArmorStandEntity.class, TrackedDataHandlerRegistry.ROTATION);
    public static final TrackedData<EulerAngle> TRACKER_RIGHT_LEG_ROTATION = DataTracker.registerData(ArmorStandEntity.class, TrackedDataHandlerRegistry.ROTATION);
    private static final Predicate<Entity> RIDEABLE_MINECART_PREDICATE = entity -> entity instanceof AbstractMinecartEntity && ((AbstractMinecartEntity)entity).getMinecartType() == AbstractMinecartEntity.Type.RIDEABLE;
    private final DefaultedList<ItemStack> heldItems = DefaultedList.ofSize(2, ItemStack.EMPTY);
    private final DefaultedList<ItemStack> armorItems = DefaultedList.ofSize(4, ItemStack.EMPTY);
    private boolean invisible;
    public long lastHitTime;
    private int disabledSlots;
    private EulerAngle headRotation = DEFAULT_HEAD_ROTATION;
    private EulerAngle bodyRotation = DEFAULT_BODY_ROTATION;
    private EulerAngle leftArmRotation = DEFAULT_LEFT_ARM_ROTATION;
    private EulerAngle rightArmRotation = DEFAULT_RIGHT_ARM_ROTATION;
    private EulerAngle leftLegRotation = DEFAULT_LEFT_LEG_ROTATION;
    private EulerAngle rightLegRotation = DEFAULT_RIGHT_LEG_ROTATION;

    public ArmorStandEntity(EntityType<? extends ArmorStandEntity> arg, World arg2) {
        super((EntityType<? extends LivingEntity>)arg, arg2);
    }

    public ArmorStandEntity(World world, double x, double y, double z) {
        this((EntityType<? extends ArmorStandEntity>)EntityType.ARMOR_STAND, world);
        this.setPosition(x, y, z);
    }

    public static DefaultAttributeContainer.Builder createArmorStandAttributes() {
        return ArmorStandEntity.createLivingAttributes().add(EntityAttributes.GENERIC_STEP_HEIGHT, 0.0);
    }

    @Override
    public void calculateDimensions() {
        double d = this.getX();
        double e = this.getY();
        double f = this.getZ();
        super.calculateDimensions();
        this.setPosition(d, e, f);
    }

    private boolean canClip() {
        return !this.isMarker() && !this.hasNoGravity();
    }

    @Override
    public boolean canMoveVoluntarily() {
        return super.canMoveVoluntarily() && this.canClip();
    }

    @Override
    protected void initDataTracker(DataTracker.Builder builder) {
        super.initDataTracker(builder);
        builder.add(ARMOR_STAND_FLAGS, (byte)0);
        builder.add(TRACKER_HEAD_ROTATION, DEFAULT_HEAD_ROTATION);
        builder.add(TRACKER_BODY_ROTATION, DEFAULT_BODY_ROTATION);
        builder.add(TRACKER_LEFT_ARM_ROTATION, DEFAULT_LEFT_ARM_ROTATION);
        builder.add(TRACKER_RIGHT_ARM_ROTATION, DEFAULT_RIGHT_ARM_ROTATION);
        builder.add(TRACKER_LEFT_LEG_ROTATION, DEFAULT_LEFT_LEG_ROTATION);
        builder.add(TRACKER_RIGHT_LEG_ROTATION, DEFAULT_RIGHT_LEG_ROTATION);
    }

    @Override
    public Iterable<ItemStack> getHandItems() {
        return this.heldItems;
    }

    @Override
    public Iterable<ItemStack> getArmorItems() {
        return this.armorItems;
    }

    @Override
    public ItemStack getEquippedStack(EquipmentSlot slot) {
        switch (slot.getType()) {
            case HAND: {
                return this.heldItems.get(slot.getEntitySlotId());
            }
            case HUMANOID_ARMOR: {
                return this.armorItems.get(slot.getEntitySlotId());
            }
        }
        return ItemStack.EMPTY;
    }

    @Override
    public boolean canUseSlot(EquipmentSlot slot) {
        return slot != EquipmentSlot.BODY;
    }

    @Override
    public void equipStack(EquipmentSlot slot, ItemStack stack) {
        this.processEquippedStack(stack);
        switch (slot.getType()) {
            case HAND: {
                this.onEquipStack(slot, this.heldItems.set(slot.getEntitySlotId(), stack), stack);
                break;
            }
            case HUMANOID_ARMOR: {
                this.onEquipStack(slot, this.armorItems.set(slot.getEntitySlotId(), stack), stack);
            }
        }
    }

    @Override
    public boolean canEquip(ItemStack stack) {
        EquipmentSlot lv = this.getPreferredEquipmentSlot(stack);
        return this.getEquippedStack(lv).isEmpty() && !this.isSlotDisabled(lv);
    }

    @Override
    public void writeCustomDataToNbt(NbtCompound nbt) {
        super.writeCustomDataToNbt(nbt);
        NbtList lv = new NbtList();
        for (ItemStack lv2 : this.armorItems) {
            lv.add(lv2.encodeAllowEmpty(this.getRegistryManager()));
        }
        nbt.put("ArmorItems", lv);
        NbtList lv3 = new NbtList();
        for (ItemStack lv4 : this.heldItems) {
            lv3.add(lv4.encodeAllowEmpty(this.getRegistryManager()));
        }
        nbt.put("HandItems", lv3);
        nbt.putBoolean("Invisible", this.isInvisible());
        nbt.putBoolean("Small", this.isSmall());
        nbt.putBoolean("ShowArms", this.shouldShowArms());
        nbt.putInt("DisabledSlots", this.disabledSlots);
        nbt.putBoolean("NoBasePlate", this.shouldHideBasePlate());
        if (this.isMarker()) {
            nbt.putBoolean("Marker", this.isMarker());
        }
        nbt.put("Pose", this.poseToNbt());
    }

    @Override
    public void readCustomDataFromNbt(NbtCompound nbt) {
        NbtCompound lv2;
        int i;
        NbtList lv;
        super.readCustomDataFromNbt(nbt);
        if (nbt.contains("ArmorItems", NbtElement.LIST_TYPE)) {
            lv = nbt.getList("ArmorItems", NbtElement.COMPOUND_TYPE);
            for (i = 0; i < this.armorItems.size(); ++i) {
                lv2 = lv.getCompound(i);
                this.armorItems.set(i, ItemStack.fromNbtOrEmpty(this.getRegistryManager(), lv2));
            }
        }
        if (nbt.contains("HandItems", NbtElement.LIST_TYPE)) {
            lv = nbt.getList("HandItems", NbtElement.COMPOUND_TYPE);
            for (i = 0; i < this.heldItems.size(); ++i) {
                lv2 = lv.getCompound(i);
                this.heldItems.set(i, ItemStack.fromNbtOrEmpty(this.getRegistryManager(), lv2));
            }
        }
        this.setInvisible(nbt.getBoolean("Invisible"));
        this.setSmall(nbt.getBoolean("Small"));
        this.setShowArms(nbt.getBoolean("ShowArms"));
        this.disabledSlots = nbt.getInt("DisabledSlots");
        this.setHideBasePlate(nbt.getBoolean("NoBasePlate"));
        this.setMarker(nbt.getBoolean("Marker"));
        this.noClip = !this.canClip();
        NbtCompound lv3 = nbt.getCompound("Pose");
        this.readPoseNbt(lv3);
    }

    private void readPoseNbt(NbtCompound nbt) {
        NbtList lv = nbt.getList("Head", NbtElement.FLOAT_TYPE);
        this.setHeadRotation(lv.isEmpty() ? DEFAULT_HEAD_ROTATION : new EulerAngle(lv));
        NbtList lv2 = nbt.getList("Body", NbtElement.FLOAT_TYPE);
        this.setBodyRotation(lv2.isEmpty() ? DEFAULT_BODY_ROTATION : new EulerAngle(lv2));
        NbtList lv3 = nbt.getList("LeftArm", NbtElement.FLOAT_TYPE);
        this.setLeftArmRotation(lv3.isEmpty() ? DEFAULT_LEFT_ARM_ROTATION : new EulerAngle(lv3));
        NbtList lv4 = nbt.getList("RightArm", NbtElement.FLOAT_TYPE);
        this.setRightArmRotation(lv4.isEmpty() ? DEFAULT_RIGHT_ARM_ROTATION : new EulerAngle(lv4));
        NbtList lv5 = nbt.getList("LeftLeg", NbtElement.FLOAT_TYPE);
        this.setLeftLegRotation(lv5.isEmpty() ? DEFAULT_LEFT_LEG_ROTATION : new EulerAngle(lv5));
        NbtList lv6 = nbt.getList("RightLeg", NbtElement.FLOAT_TYPE);
        this.setRightLegRotation(lv6.isEmpty() ? DEFAULT_RIGHT_LEG_ROTATION : new EulerAngle(lv6));
    }

    private NbtCompound poseToNbt() {
        NbtCompound lv = new NbtCompound();
        if (!DEFAULT_HEAD_ROTATION.equals(this.headRotation)) {
            lv.put("Head", this.headRotation.toNbt());
        }
        if (!DEFAULT_BODY_ROTATION.equals(this.bodyRotation)) {
            lv.put("Body", this.bodyRotation.toNbt());
        }
        if (!DEFAULT_LEFT_ARM_ROTATION.equals(this.leftArmRotation)) {
            lv.put("LeftArm", this.leftArmRotation.toNbt());
        }
        if (!DEFAULT_RIGHT_ARM_ROTATION.equals(this.rightArmRotation)) {
            lv.put("RightArm", this.rightArmRotation.toNbt());
        }
        if (!DEFAULT_LEFT_LEG_ROTATION.equals(this.leftLegRotation)) {
            lv.put("LeftLeg", this.leftLegRotation.toNbt());
        }
        if (!DEFAULT_RIGHT_LEG_ROTATION.equals(this.rightLegRotation)) {
            lv.put("RightLeg", this.rightLegRotation.toNbt());
        }
        return lv;
    }

    @Override
    public boolean isPushable() {
        return false;
    }

    @Override
    protected void pushAway(Entity entity) {
    }

    @Override
    protected void tickCramming() {
        List<Entity> list = this.getWorld().getOtherEntities(this, this.getBoundingBox(), RIDEABLE_MINECART_PREDICATE);
        for (Entity lv : list) {
            if (!(this.squaredDistanceTo(lv) <= 0.2)) continue;
            lv.pushAwayFrom(this);
        }
    }

    @Override
    public ActionResult interactAt(PlayerEntity player, Vec3d hitPos, Hand hand) {
        ItemStack lv = player.getStackInHand(hand);
        if (this.isMarker() || lv.isOf(Items.NAME_TAG)) {
            return ActionResult.PASS;
        }
        if (player.isSpectator()) {
            return ActionResult.SUCCESS;
        }
        if (player.getWorld().isClient) {
            return ActionResult.CONSUME;
        }
        EquipmentSlot lv2 = this.getPreferredEquipmentSlot(lv);
        if (lv.isEmpty()) {
            EquipmentSlot lv4;
            EquipmentSlot lv3 = this.getSlotFromPosition(hitPos);
            EquipmentSlot equipmentSlot = lv4 = this.isSlotDisabled(lv3) ? lv2 : lv3;
            if (this.hasStackEquipped(lv4) && this.equip(player, lv4, lv, hand)) {
                return ActionResult.SUCCESS;
            }
        } else {
            if (this.isSlotDisabled(lv2)) {
                return ActionResult.FAIL;
            }
            if (lv2.getType() == EquipmentSlot.Type.HAND && !this.shouldShowArms()) {
                return ActionResult.FAIL;
            }
            if (this.equip(player, lv2, lv, hand)) {
                return ActionResult.SUCCESS;
            }
        }
        return ActionResult.PASS;
    }

    /*
     * Enabled force condition propagation
     * Lifted jumps to return sites
     */
    private EquipmentSlot getSlotFromPosition(Vec3d hitPos) {
        EquipmentSlot lv = EquipmentSlot.MAINHAND;
        boolean bl = this.isSmall();
        double d = hitPos.y / (double)(this.getScale() * this.getScaleFactor());
        EquipmentSlot lv2 = EquipmentSlot.FEET;
        if (d >= 0.1) {
            double d2 = bl ? 0.8 : 0.45;
            if (d < 0.1 + d2 && this.hasStackEquipped(lv2)) {
                return EquipmentSlot.FEET;
            }
        }
        double d3 = bl ? 0.3 : 0.0;
        if (d >= 0.9 + d3) {
            double d4 = bl ? 1.0 : 0.7;
            if (d < 0.9 + d4 && this.hasStackEquipped(EquipmentSlot.CHEST)) {
                return EquipmentSlot.CHEST;
            }
        }
        if (d >= 0.4) {
            double d5 = bl ? 1.0 : 0.8;
            if (d < 0.4 + d5 && this.hasStackEquipped(EquipmentSlot.LEGS)) {
                return EquipmentSlot.LEGS;
            }
        }
        if (d >= 1.6 && this.hasStackEquipped(EquipmentSlot.HEAD)) {
            return EquipmentSlot.HEAD;
        }
        if (this.hasStackEquipped(EquipmentSlot.MAINHAND)) return lv;
        if (!this.hasStackEquipped(EquipmentSlot.OFFHAND)) return lv;
        return EquipmentSlot.OFFHAND;
    }

    private boolean isSlotDisabled(EquipmentSlot slot) {
        return (this.disabledSlots & 1 << slot.getArmorStandSlotId()) != 0 || slot.getType() == EquipmentSlot.Type.HAND && !this.shouldShowArms();
    }

    private boolean equip(PlayerEntity player, EquipmentSlot slot, ItemStack stack, Hand hand) {
        ItemStack lv = this.getEquippedStack(slot);
        if (!lv.isEmpty() && (this.disabledSlots & 1 << slot.getArmorStandSlotId() + 8) != 0) {
            return false;
        }
        if (lv.isEmpty() && (this.disabledSlots & 1 << slot.getArmorStandSlotId() + 16) != 0) {
            return false;
        }
        if (player.isInCreativeMode() && lv.isEmpty() && !stack.isEmpty()) {
            this.equipStack(slot, stack.copyWithCount(1));
            return true;
        }
        if (!stack.isEmpty() && stack.getCount() > 1) {
            if (!lv.isEmpty()) {
                return false;
            }
            this.equipStack(slot, stack.split(1));
            return true;
        }
        this.equipStack(slot, stack);
        player.setStackInHand(hand, lv);
        return true;
    }

    @Override
    public boolean damage(DamageSource source, float amount) {
        if (this.isRemoved()) {
            return false;
        }
        World world = this.getWorld();
        if (!(world instanceof ServerWorld)) {
            return false;
        }
        ServerWorld lv = (ServerWorld)world;
        if (source.isIn(DamageTypeTags.BYPASSES_INVULNERABILITY)) {
            this.kill();
            return false;
        }
        if (this.isInvulnerableTo(source) || this.invisible || this.isMarker()) {
            return false;
        }
        if (source.isIn(DamageTypeTags.IS_EXPLOSION)) {
            this.onBreak(lv, source);
            this.kill();
            return false;
        }
        if (source.isIn(DamageTypeTags.IGNITES_ARMOR_STANDS)) {
            if (this.isOnFire()) {
                this.updateHealth(lv, source, 0.15f);
            } else {
                this.setOnFireFor(5.0f);
            }
            return false;
        }
        if (source.isIn(DamageTypeTags.BURNS_ARMOR_STANDS) && this.getHealth() > 0.5f) {
            this.updateHealth(lv, source, 4.0f);
            return false;
        }
        boolean bl = source.isIn(DamageTypeTags.CAN_BREAK_ARMOR_STAND);
        boolean bl2 = source.isIn(DamageTypeTags.ALWAYS_KILLS_ARMOR_STANDS);
        if (!bl && !bl2) {
            return false;
        }
        Entity entity = source.getAttacker();
        if (entity instanceof PlayerEntity) {
            PlayerEntity lv2 = (PlayerEntity)entity;
            if (!lv2.getAbilities().allowModifyWorld) {
                return false;
            }
        }
        if (source.isSourceCreativePlayer()) {
            this.playBreakSound();
            this.spawnBreakParticles();
            this.kill();
            return true;
        }
        long l = lv.getTime();
        if (l - this.lastHitTime <= 5L || bl2) {
            this.breakAndDropItem(lv, source);
            this.spawnBreakParticles();
            this.kill();
        } else {
            lv.sendEntityStatus(this, EntityStatuses.HIT_ARMOR_STAND);
            this.emitGameEvent(GameEvent.ENTITY_DAMAGE, source.getAttacker());
            this.lastHitTime = l;
        }
        return true;
    }

    @Override
    public void handleStatus(byte status) {
        if (status == EntityStatuses.HIT_ARMOR_STAND) {
            if (this.getWorld().isClient) {
                this.getWorld().playSound(this.getX(), this.getY(), this.getZ(), SoundEvents.ENTITY_ARMOR_STAND_HIT, this.getSoundCategory(), 0.3f, 1.0f, false);
                this.lastHitTime = this.getWorld().getTime();
            }
        } else {
            super.handleStatus(status);
        }
    }

    @Override
    public boolean shouldRender(double distance) {
        double e = this.getBoundingBox().getAverageSideLength() * 4.0;
        if (Double.isNaN(e) || e == 0.0) {
            e = 4.0;
        }
        return distance < (e *= 64.0) * e;
    }

    private void spawnBreakParticles() {
        if (this.getWorld() instanceof ServerWorld) {
            ((ServerWorld)this.getWorld()).spawnParticles(new BlockStateParticleEffect(ParticleTypes.BLOCK, Blocks.OAK_PLANKS.getDefaultState()), this.getX(), this.getBodyY(0.6666666666666666), this.getZ(), 10, this.getWidth() / 4.0f, this.getHeight() / 4.0f, this.getWidth() / 4.0f, 0.05);
        }
    }

    private void updateHealth(ServerWorld world, DamageSource damageSource, float amount) {
        float g = this.getHealth();
        if ((g -= amount) <= 0.5f) {
            this.onBreak(world, damageSource);
            this.kill();
        } else {
            this.setHealth(g);
            this.emitGameEvent(GameEvent.ENTITY_DAMAGE, damageSource.getAttacker());
        }
    }

    private void breakAndDropItem(ServerWorld world, DamageSource damageSource) {
        ItemStack lv = new ItemStack(Items.ARMOR_STAND);
        lv.set(DataComponentTypes.CUSTOM_NAME, this.getCustomName());
        Block.dropStack(this.getWorld(), this.getBlockPos(), lv);
        this.onBreak(world, damageSource);
    }

    private void onBreak(ServerWorld world, DamageSource damageSource) {
        ItemStack lv;
        int i;
        this.playBreakSound();
        this.drop(world, damageSource);
        for (i = 0; i < this.heldItems.size(); ++i) {
            lv = this.heldItems.get(i);
            if (lv.isEmpty()) continue;
            Block.dropStack(this.getWorld(), this.getBlockPos().up(), lv);
            this.heldItems.set(i, ItemStack.EMPTY);
        }
        for (i = 0; i < this.armorItems.size(); ++i) {
            lv = this.armorItems.get(i);
            if (lv.isEmpty()) continue;
            Block.dropStack(this.getWorld(), this.getBlockPos().up(), lv);
            this.armorItems.set(i, ItemStack.EMPTY);
        }
    }

    private void playBreakSound() {
        this.getWorld().playSound(null, this.getX(), this.getY(), this.getZ(), SoundEvents.ENTITY_ARMOR_STAND_BREAK, this.getSoundCategory(), 1.0f, 1.0f);
    }

    @Override
    protected float turnHead(float bodyRotation, float headRotation) {
        this.prevBodyYaw = this.prevYaw;
        this.bodyYaw = this.getYaw();
        return 0.0f;
    }

    @Override
    public void travel(Vec3d movementInput) {
        if (!this.canClip()) {
            return;
        }
        super.travel(movementInput);
    }

    @Override
    public void setBodyYaw(float bodyYaw) {
        this.prevBodyYaw = this.prevYaw = bodyYaw;
        this.prevHeadYaw = this.headYaw = bodyYaw;
    }

    @Override
    public void setHeadYaw(float headYaw) {
        this.prevBodyYaw = this.prevYaw = headYaw;
        this.prevHeadYaw = this.headYaw = headYaw;
    }

    @Override
    public void tick() {
        EulerAngle lv6;
        EulerAngle lv5;
        EulerAngle lv4;
        EulerAngle lv3;
        EulerAngle lv2;
        super.tick();
        EulerAngle lv = this.dataTracker.get(TRACKER_HEAD_ROTATION);
        if (!this.headRotation.equals(lv)) {
            this.setHeadRotation(lv);
        }
        if (!this.bodyRotation.equals(lv2 = this.dataTracker.get(TRACKER_BODY_ROTATION))) {
            this.setBodyRotation(lv2);
        }
        if (!this.leftArmRotation.equals(lv3 = this.dataTracker.get(TRACKER_LEFT_ARM_ROTATION))) {
            this.setLeftArmRotation(lv3);
        }
        if (!this.rightArmRotation.equals(lv4 = this.dataTracker.get(TRACKER_RIGHT_ARM_ROTATION))) {
            this.setRightArmRotation(lv4);
        }
        if (!this.leftLegRotation.equals(lv5 = this.dataTracker.get(TRACKER_LEFT_LEG_ROTATION))) {
            this.setLeftLegRotation(lv5);
        }
        if (!this.rightLegRotation.equals(lv6 = this.dataTracker.get(TRACKER_RIGHT_LEG_ROTATION))) {
            this.setRightLegRotation(lv6);
        }
    }

    @Override
    protected void updatePotionVisibility() {
        this.setInvisible(this.invisible);
    }

    @Override
    public void setInvisible(boolean invisible) {
        this.invisible = invisible;
        super.setInvisible(invisible);
    }

    @Override
    public boolean isBaby() {
        return this.isSmall();
    }

    @Override
    public void kill() {
        this.remove(Entity.RemovalReason.KILLED);
        this.emitGameEvent(GameEvent.ENTITY_DIE);
    }

    @Override
    public boolean isImmuneToExplosion(Explosion explosion) {
        return this.isInvisible();
    }

    @Override
    public PistonBehavior getPistonBehavior() {
        if (this.isMarker()) {
            return PistonBehavior.IGNORE;
        }
        return super.getPistonBehavior();
    }

    @Override
    public boolean canAvoidTraps() {
        return this.isMarker();
    }

    private void setSmall(boolean small) {
        this.dataTracker.set(ARMOR_STAND_FLAGS, this.setBitField(this.dataTracker.get(ARMOR_STAND_FLAGS), SMALL_FLAG, small));
    }

    public boolean isSmall() {
        return (this.dataTracker.get(ARMOR_STAND_FLAGS) & 1) != 0;
    }

    public void setShowArms(boolean showArms) {
        this.dataTracker.set(ARMOR_STAND_FLAGS, this.setBitField(this.dataTracker.get(ARMOR_STAND_FLAGS), SHOW_ARMS_FLAG, showArms));
    }

    public boolean shouldShowArms() {
        return (this.dataTracker.get(ARMOR_STAND_FLAGS) & 4) != 0;
    }

    public void setHideBasePlate(boolean hideBasePlate) {
        this.dataTracker.set(ARMOR_STAND_FLAGS, this.setBitField(this.dataTracker.get(ARMOR_STAND_FLAGS), HIDE_BASE_PLATE_FLAG, hideBasePlate));
    }

    public boolean shouldHideBasePlate() {
        return (this.dataTracker.get(ARMOR_STAND_FLAGS) & 8) != 0;
    }

    private void setMarker(boolean marker) {
        this.dataTracker.set(ARMOR_STAND_FLAGS, this.setBitField(this.dataTracker.get(ARMOR_STAND_FLAGS), MARKER_FLAG, marker));
    }

    public boolean isMarker() {
        return (this.dataTracker.get(ARMOR_STAND_FLAGS) & 0x10) != 0;
    }

    private byte setBitField(byte value, int bitField, boolean set) {
        value = set ? (byte)(value | bitField) : (byte)(value & ~bitField);
        return value;
    }

    public void setHeadRotation(EulerAngle angle) {
        this.headRotation = angle;
        this.dataTracker.set(TRACKER_HEAD_ROTATION, angle);
    }

    public void setBodyRotation(EulerAngle angle) {
        this.bodyRotation = angle;
        this.dataTracker.set(TRACKER_BODY_ROTATION, angle);
    }

    public void setLeftArmRotation(EulerAngle angle) {
        this.leftArmRotation = angle;
        this.dataTracker.set(TRACKER_LEFT_ARM_ROTATION, angle);
    }

    public void setRightArmRotation(EulerAngle angle) {
        this.rightArmRotation = angle;
        this.dataTracker.set(TRACKER_RIGHT_ARM_ROTATION, angle);
    }

    public void setLeftLegRotation(EulerAngle angle) {
        this.leftLegRotation = angle;
        this.dataTracker.set(TRACKER_LEFT_LEG_ROTATION, angle);
    }

    public void setRightLegRotation(EulerAngle angle) {
        this.rightLegRotation = angle;
        this.dataTracker.set(TRACKER_RIGHT_LEG_ROTATION, angle);
    }

    public EulerAngle getHeadRotation() {
        return this.headRotation;
    }

    public EulerAngle getBodyRotation() {
        return this.bodyRotation;
    }

    public EulerAngle getLeftArmRotation() {
        return this.leftArmRotation;
    }

    public EulerAngle getRightArmRotation() {
        return this.rightArmRotation;
    }

    public EulerAngle getLeftLegRotation() {
        return this.leftLegRotation;
    }

    public EulerAngle getRightLegRotation() {
        return this.rightLegRotation;
    }

    @Override
    public boolean canHit() {
        return super.canHit() && !this.isMarker();
    }

    @Override
    public boolean handleAttack(Entity attacker) {
        return attacker instanceof PlayerEntity && !this.getWorld().canPlayerModifyAt((PlayerEntity)attacker, this.getBlockPos());
    }

    @Override
    public Arm getMainArm() {
        return Arm.RIGHT;
    }

    @Override
    public LivingEntity.FallSounds getFallSounds() {
        return new LivingEntity.FallSounds(SoundEvents.ENTITY_ARMOR_STAND_FALL, SoundEvents.ENTITY_ARMOR_STAND_FALL);
    }

    @Override
    @Nullable
    protected SoundEvent getHurtSound(DamageSource source) {
        return SoundEvents.ENTITY_ARMOR_STAND_HIT;
    }

    @Override
    @Nullable
    protected SoundEvent getDeathSound() {
        return SoundEvents.ENTITY_ARMOR_STAND_BREAK;
    }

    @Override
    public void onStruckByLightning(ServerWorld world, LightningEntity lightning) {
    }

    @Override
    public boolean isAffectedBySplashPotions() {
        return false;
    }

    @Override
    public void onTrackedDataSet(TrackedData<?> data) {
        if (ARMOR_STAND_FLAGS.equals(data)) {
            this.calculateDimensions();
            this.intersectionChecked = !this.isMarker();
        }
        super.onTrackedDataSet(data);
    }

    @Override
    public boolean isMobOrPlayer() {
        return false;
    }

    @Override
    public EntityDimensions getBaseDimensions(EntityPose pose) {
        return this.getDimensions(this.isMarker());
    }

    private EntityDimensions getDimensions(boolean marker) {
        if (marker) {
            return MARKER_DIMENSIONS;
        }
        return this.isBaby() ? SMALL_DIMENSIONS : this.getType().getDimensions();
    }

    @Override
    public Vec3d getClientCameraPosVec(float tickDelta) {
        if (this.isMarker()) {
            Box lv = this.getDimensions(false).getBoxAt(this.getPos());
            BlockPos lv2 = this.getBlockPos();
            int i = Integer.MIN_VALUE;
            for (BlockPos lv3 : BlockPos.iterate(BlockPos.ofFloored(lv.minX, lv.minY, lv.minZ), BlockPos.ofFloored(lv.maxX, lv.maxY, lv.maxZ))) {
                int j = Math.max(this.getWorld().getLightLevel(LightType.BLOCK, lv3), this.getWorld().getLightLevel(LightType.SKY, lv3));
                if (j == 15) {
                    return Vec3d.ofCenter(lv3);
                }
                if (j <= i) continue;
                i = j;
                lv2 = lv3.toImmutable();
            }
            return Vec3d.ofCenter(lv2);
        }
        return super.getClientCameraPosVec(tickDelta);
    }

    @Override
    public ItemStack getPickBlockStack() {
        return new ItemStack(Items.ARMOR_STAND);
    }

    @Override
    public boolean isPartOfGame() {
        return !this.isInvisible() && !this.isMarker();
    }
}

