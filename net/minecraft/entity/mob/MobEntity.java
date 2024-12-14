/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 * 
 * Could not load the following classes:
 *  org.jetbrains.annotations.Nullable
 */
package net.minecraft.entity.mob;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.Iterables;
import com.google.common.collect.Maps;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.function.Predicate;
import net.minecraft.component.ComponentMap;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.EnchantmentEffectComponentTypes;
import net.minecraft.component.type.AttributeModifiersComponent;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.enchantment.provider.EnchantmentProviders;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityData;
import net.minecraft.entity.EntityStatuses;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.EquipmentHolder;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.EquipmentTable;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.Leashable;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.SpawnReason;
import net.minecraft.entity.Targeter;
import net.minecraft.entity.ai.brain.MemoryModuleType;
import net.minecraft.entity.ai.control.BodyControl;
import net.minecraft.entity.ai.control.JumpControl;
import net.minecraft.entity.ai.control.LookControl;
import net.minecraft.entity.ai.control.MoveControl;
import net.minecraft.entity.ai.goal.Goal;
import net.minecraft.entity.ai.goal.GoalSelector;
import net.minecraft.entity.ai.pathing.EntityNavigation;
import net.minecraft.entity.ai.pathing.MobNavigation;
import net.minecraft.entity.ai.pathing.PathNodeType;
import net.minecraft.entity.attribute.DefaultAttributeContainer;
import net.minecraft.entity.attribute.EntityAttributeInstance;
import net.minecraft.entity.attribute.EntityAttributeModifier;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.data.TrackedData;
import net.minecraft.entity.data.TrackedDataHandlerRegistry;
import net.minecraft.entity.mob.MobVisibilityCache;
import net.minecraft.entity.mob.Monster;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.vehicle.BoatEntity;
import net.minecraft.fluid.Fluid;
import net.minecraft.item.ArmorItem;
import net.minecraft.item.BlockItem;
import net.minecraft.item.BowItem;
import net.minecraft.item.CrossbowItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.item.MiningToolItem;
import net.minecraft.item.RangedWeaponItem;
import net.minecraft.item.SpawnEggItem;
import net.minecraft.item.SwordItem;
import net.minecraft.loot.LootTable;
import net.minecraft.loot.context.LootContextParameterSet;
import net.minecraft.loot.context.LootContextParameters;
import net.minecraft.loot.context.LootContextTypes;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtFloat;
import net.minecraft.nbt.NbtList;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.server.network.DebugInfoSender;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundEvent;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Arm;
import net.minecraft.util.Hand;
import net.minecraft.util.Identifier;
import net.minecraft.util.collection.DefaultedList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3i;
import net.minecraft.util.math.random.Random;
import net.minecraft.util.profiler.Profiler;
import net.minecraft.world.Difficulty;
import net.minecraft.world.GameRules;
import net.minecraft.world.LocalDifficulty;
import net.minecraft.world.ServerWorldAccess;
import net.minecraft.world.World;
import net.minecraft.world.WorldAccess;
import net.minecraft.world.WorldView;
import net.minecraft.world.event.GameEvent;
import org.jetbrains.annotations.Nullable;

public abstract class MobEntity
extends LivingEntity
implements EquipmentHolder,
Leashable,
Targeter {
    private static final TrackedData<Byte> MOB_FLAGS = DataTracker.registerData(MobEntity.class, TrackedDataHandlerRegistry.BYTE);
    private static final int AI_DISABLED_FLAG = 1;
    private static final int LEFT_HANDED_FLAG = 2;
    private static final int ATTACKING_FLAG = 4;
    protected static final int MINIMUM_DROPPED_XP_PER_EQUIPMENT = 1;
    private static final Vec3i ITEM_PICK_UP_RANGE_EXPANDER = new Vec3i(1, 0, 1);
    public static final float BASE_SPAWN_EQUIPMENT_CHANCE = 0.15f;
    public static final float DEFAULT_CAN_PICKUP_LOOT_CHANCE = 0.55f;
    public static final float BASE_ENCHANTED_ARMOR_CHANCE = 0.5f;
    public static final float BASE_ENCHANTED_MAIN_HAND_EQUIPMENT_CHANCE = 0.25f;
    public static final float DEFAULT_DROP_CHANCE = 0.085f;
    public static final float field_52220 = 1.0f;
    public static final int field_38932 = 2;
    public static final int field_35039 = 2;
    private static final double ATTACK_RANGE = Math.sqrt(2.04f) - (double)0.6f;
    protected static final Identifier RANDOM_SPAWN_BONUS_MODIFIER_ID = Identifier.ofVanilla("random_spawn_bonus");
    public int ambientSoundChance;
    protected int experiencePoints;
    protected LookControl lookControl;
    protected MoveControl moveControl;
    protected JumpControl jumpControl;
    private final BodyControl bodyControl;
    protected EntityNavigation navigation;
    protected final GoalSelector goalSelector;
    protected final GoalSelector targetSelector;
    @Nullable
    private LivingEntity target;
    private final MobVisibilityCache visibilityCache;
    private final DefaultedList<ItemStack> handItems = DefaultedList.ofSize(2, ItemStack.EMPTY);
    protected final float[] handDropChances = new float[2];
    private final DefaultedList<ItemStack> armorItems = DefaultedList.ofSize(4, ItemStack.EMPTY);
    protected final float[] armorDropChances = new float[4];
    private ItemStack bodyArmor = ItemStack.EMPTY;
    protected float bodyArmorDropChance;
    private boolean canPickUpLoot;
    private boolean persistent;
    private final Map<PathNodeType, Float> pathfindingPenalties = Maps.newEnumMap(PathNodeType.class);
    @Nullable
    private RegistryKey<LootTable> lootTable;
    private long lootTableSeed;
    @Nullable
    private Leashable.LeashData leashData;
    private BlockPos positionTarget = BlockPos.ORIGIN;
    private float positionTargetRange = -1.0f;

    protected MobEntity(EntityType<? extends MobEntity> arg, World arg2) {
        super((EntityType<? extends LivingEntity>)arg, arg2);
        this.goalSelector = new GoalSelector(arg2.getProfilerSupplier());
        this.targetSelector = new GoalSelector(arg2.getProfilerSupplier());
        this.lookControl = new LookControl(this);
        this.moveControl = new MoveControl(this);
        this.jumpControl = new JumpControl(this);
        this.bodyControl = this.createBodyControl();
        this.navigation = this.createNavigation(arg2);
        this.visibilityCache = new MobVisibilityCache(this);
        Arrays.fill(this.armorDropChances, 0.085f);
        Arrays.fill(this.handDropChances, 0.085f);
        this.bodyArmorDropChance = 0.085f;
        if (arg2 != null && !arg2.isClient) {
            this.initGoals();
        }
    }

    protected void initGoals() {
    }

    public static DefaultAttributeContainer.Builder createMobAttributes() {
        return LivingEntity.createLivingAttributes().add(EntityAttributes.GENERIC_FOLLOW_RANGE, 16.0);
    }

    protected EntityNavigation createNavigation(World world) {
        return new MobNavigation(this, world);
    }

    protected boolean movesIndependently() {
        return false;
    }

    public float getPathfindingPenalty(PathNodeType nodeType) {
        MobEntity lv;
        Entity entity = this.getControllingVehicle();
        MobEntity lv2 = entity instanceof MobEntity && (lv = (MobEntity)entity).movesIndependently() ? lv : this;
        Float float_ = lv2.pathfindingPenalties.get((Object)nodeType);
        return float_ == null ? nodeType.getDefaultPenalty() : float_.floatValue();
    }

    public void setPathfindingPenalty(PathNodeType nodeType, float penalty) {
        this.pathfindingPenalties.put(nodeType, Float.valueOf(penalty));
    }

    public void onStartPathfinding() {
    }

    public void onFinishPathfinding() {
    }

    protected BodyControl createBodyControl() {
        return new BodyControl(this);
    }

    public LookControl getLookControl() {
        return this.lookControl;
    }

    public MoveControl getMoveControl() {
        Entity entity = this.getControllingVehicle();
        if (entity instanceof MobEntity) {
            MobEntity lv = (MobEntity)entity;
            return lv.getMoveControl();
        }
        return this.moveControl;
    }

    public JumpControl getJumpControl() {
        return this.jumpControl;
    }

    public EntityNavigation getNavigation() {
        Entity entity = this.getControllingVehicle();
        if (entity instanceof MobEntity) {
            MobEntity lv = (MobEntity)entity;
            return lv.getNavigation();
        }
        return this.navigation;
    }

    /*
     * Enabled force condition propagation
     * Lifted jumps to return sites
     */
    @Override
    @Nullable
    public LivingEntity getControllingPassenger() {
        Entity lv = this.getFirstPassenger();
        if (this.isAiDisabled()) return null;
        if (!(lv instanceof MobEntity)) return null;
        MobEntity lv2 = (MobEntity)lv;
        if (!lv.shouldControlVehicles()) return null;
        MobEntity mobEntity = lv2;
        return mobEntity;
    }

    public MobVisibilityCache getVisibilityCache() {
        return this.visibilityCache;
    }

    @Override
    @Nullable
    public LivingEntity getTarget() {
        return this.target;
    }

    @Nullable
    protected final LivingEntity getTargetInBrain() {
        return this.getBrain().getOptionalRegisteredMemory(MemoryModuleType.ATTACK_TARGET).orElse(null);
    }

    public void setTarget(@Nullable LivingEntity target) {
        this.target = target;
    }

    @Override
    public boolean canTarget(EntityType<?> type) {
        return type != EntityType.GHAST;
    }

    public boolean canUseRangedWeapon(RangedWeaponItem weapon) {
        return false;
    }

    public void onEatingGrass() {
        this.emitGameEvent(GameEvent.EAT);
    }

    @Override
    protected void initDataTracker(DataTracker.Builder builder) {
        super.initDataTracker(builder);
        builder.add(MOB_FLAGS, (byte)0);
    }

    public int getMinAmbientSoundDelay() {
        return 80;
    }

    public void playAmbientSound() {
        this.playSound(this.getAmbientSound());
    }

    @Override
    public void baseTick() {
        super.baseTick();
        this.getWorld().getProfiler().push("mobBaseTick");
        if (this.isAlive() && this.random.nextInt(1000) < this.ambientSoundChance++) {
            this.resetSoundDelay();
            this.playAmbientSound();
        }
        this.getWorld().getProfiler().pop();
    }

    @Override
    protected void playHurtSound(DamageSource damageSource) {
        this.resetSoundDelay();
        super.playHurtSound(damageSource);
    }

    private void resetSoundDelay() {
        this.ambientSoundChance = -this.getMinAmbientSoundDelay();
    }

    @Override
    protected int getXpToDrop() {
        if (this.experiencePoints > 0) {
            int j;
            int i = this.experiencePoints;
            for (j = 0; j < this.armorItems.size(); ++j) {
                if (this.armorItems.get(j).isEmpty() || !(this.armorDropChances[j] <= 1.0f)) continue;
                i += 1 + this.random.nextInt(3);
            }
            for (j = 0; j < this.handItems.size(); ++j) {
                if (this.handItems.get(j).isEmpty() || !(this.handDropChances[j] <= 1.0f)) continue;
                i += 1 + this.random.nextInt(3);
            }
            if (!this.bodyArmor.isEmpty() && this.bodyArmorDropChance <= 1.0f) {
                i += 1 + this.random.nextInt(3);
            }
            return i;
        }
        return this.experiencePoints;
    }

    public void playSpawnEffects() {
        if (this.getWorld().isClient) {
            for (int i = 0; i < 20; ++i) {
                double d = this.random.nextGaussian() * 0.02;
                double e = this.random.nextGaussian() * 0.02;
                double f = this.random.nextGaussian() * 0.02;
                double g = 10.0;
                this.getWorld().addParticle(ParticleTypes.POOF, this.offsetX(1.0) - d * 10.0, this.getRandomBodyY() - e * 10.0, this.getParticleZ(1.0) - f * 10.0, d, e, f);
            }
        } else {
            this.getWorld().sendEntityStatus(this, EntityStatuses.PLAY_SPAWN_EFFECTS);
        }
    }

    @Override
    public void handleStatus(byte status) {
        if (status == EntityStatuses.PLAY_SPAWN_EFFECTS) {
            this.playSpawnEffects();
        } else {
            super.handleStatus(status);
        }
    }

    @Override
    public void tick() {
        super.tick();
        if (!this.getWorld().isClient && this.age % 5 == 0) {
            this.updateGoalControls();
        }
    }

    protected void updateGoalControls() {
        boolean bl = !(this.getControllingPassenger() instanceof MobEntity);
        boolean bl2 = !(this.getVehicle() instanceof BoatEntity);
        this.goalSelector.setControlEnabled(Goal.Control.MOVE, bl);
        this.goalSelector.setControlEnabled(Goal.Control.JUMP, bl && bl2);
        this.goalSelector.setControlEnabled(Goal.Control.LOOK, bl);
    }

    @Override
    protected float turnHead(float bodyRotation, float headRotation) {
        this.bodyControl.tick();
        return headRotation;
    }

    @Nullable
    protected SoundEvent getAmbientSound() {
        return null;
    }

    @Override
    public void writeCustomDataToNbt(NbtCompound nbt) {
        super.writeCustomDataToNbt(nbt);
        nbt.putBoolean("CanPickUpLoot", this.canPickUpLoot());
        nbt.putBoolean("PersistenceRequired", this.persistent);
        NbtList lv = new NbtList();
        for (ItemStack itemStack : this.armorItems) {
            if (!itemStack.isEmpty()) {
                lv.add(itemStack.encode(this.getRegistryManager()));
                continue;
            }
            lv.add(new NbtCompound());
        }
        nbt.put("ArmorItems", lv);
        NbtList lv3 = new NbtList();
        for (float f : this.armorDropChances) {
            lv3.add(NbtFloat.of(f));
        }
        nbt.put("ArmorDropChances", lv3);
        NbtList nbtList = new NbtList();
        for (ItemStack lv5 : this.handItems) {
            if (!lv5.isEmpty()) {
                nbtList.add(lv5.encode(this.getRegistryManager()));
                continue;
            }
            nbtList.add(new NbtCompound());
        }
        nbt.put("HandItems", nbtList);
        NbtList lv6 = new NbtList();
        for (float g : this.handDropChances) {
            lv6.add(NbtFloat.of(g));
        }
        nbt.put("HandDropChances", lv6);
        if (!this.bodyArmor.isEmpty()) {
            nbt.put("body_armor_item", this.bodyArmor.encode(this.getRegistryManager()));
            nbt.putFloat("body_armor_drop_chance", this.bodyArmorDropChance);
        }
        this.writeLeashDataToNbt(nbt, this.leashData);
        nbt.putBoolean("LeftHanded", this.isLeftHanded());
        if (this.lootTable != null) {
            nbt.putString("DeathLootTable", this.lootTable.getValue().toString());
            if (this.lootTableSeed != 0L) {
                nbt.putLong("DeathLootTableSeed", this.lootTableSeed);
            }
        }
        if (this.isAiDisabled()) {
            nbt.putBoolean("NoAI", this.isAiDisabled());
        }
    }

    @Override
    public void readCustomDataFromNbt(NbtCompound nbt) {
        NbtCompound lv2;
        int i;
        NbtList lv;
        super.readCustomDataFromNbt(nbt);
        if (nbt.contains("CanPickUpLoot", NbtElement.BYTE_TYPE)) {
            this.setCanPickUpLoot(nbt.getBoolean("CanPickUpLoot"));
        }
        this.persistent = nbt.getBoolean("PersistenceRequired");
        if (nbt.contains("ArmorItems", NbtElement.LIST_TYPE)) {
            lv = nbt.getList("ArmorItems", NbtElement.COMPOUND_TYPE);
            for (i = 0; i < this.armorItems.size(); ++i) {
                lv2 = lv.getCompound(i);
                this.armorItems.set(i, ItemStack.fromNbtOrEmpty(this.getRegistryManager(), lv2));
            }
        }
        if (nbt.contains("ArmorDropChances", NbtElement.LIST_TYPE)) {
            lv = nbt.getList("ArmorDropChances", NbtElement.FLOAT_TYPE);
            for (i = 0; i < lv.size(); ++i) {
                this.armorDropChances[i] = lv.getFloat(i);
            }
        }
        if (nbt.contains("HandItems", NbtElement.LIST_TYPE)) {
            lv = nbt.getList("HandItems", NbtElement.COMPOUND_TYPE);
            for (i = 0; i < this.handItems.size(); ++i) {
                lv2 = lv.getCompound(i);
                this.handItems.set(i, ItemStack.fromNbtOrEmpty(this.getRegistryManager(), lv2));
            }
        }
        if (nbt.contains("HandDropChances", NbtElement.LIST_TYPE)) {
            lv = nbt.getList("HandDropChances", NbtElement.FLOAT_TYPE);
            for (i = 0; i < lv.size(); ++i) {
                this.handDropChances[i] = lv.getFloat(i);
            }
        }
        if (nbt.contains("body_armor_item", NbtElement.COMPOUND_TYPE)) {
            this.bodyArmor = ItemStack.fromNbt(this.getRegistryManager(), nbt.getCompound("body_armor_item")).orElse(ItemStack.EMPTY);
            this.bodyArmorDropChance = nbt.getFloat("body_armor_drop_chance");
        } else {
            this.bodyArmor = ItemStack.EMPTY;
        }
        this.leashData = this.readLeashDataFromNbt(nbt);
        this.setLeftHanded(nbt.getBoolean("LeftHanded"));
        if (nbt.contains("DeathLootTable", NbtElement.STRING_TYPE)) {
            this.lootTable = RegistryKey.of(RegistryKeys.LOOT_TABLE, Identifier.of(nbt.getString("DeathLootTable")));
            this.lootTableSeed = nbt.getLong("DeathLootTableSeed");
        }
        this.setAiDisabled(nbt.getBoolean("NoAI"));
    }

    @Override
    protected void dropLoot(DamageSource damageSource, boolean causedByPlayer) {
        super.dropLoot(damageSource, causedByPlayer);
        this.lootTable = null;
    }

    @Override
    public final RegistryKey<LootTable> getLootTable() {
        return this.lootTable == null ? this.getLootTableId() : this.lootTable;
    }

    protected RegistryKey<LootTable> getLootTableId() {
        return super.getLootTable();
    }

    @Override
    public long getLootTableSeed() {
        return this.lootTableSeed;
    }

    public void setForwardSpeed(float forwardSpeed) {
        this.forwardSpeed = forwardSpeed;
    }

    public void setUpwardSpeed(float upwardSpeed) {
        this.upwardSpeed = upwardSpeed;
    }

    public void setSidewaysSpeed(float sidewaysSpeed) {
        this.sidewaysSpeed = sidewaysSpeed;
    }

    @Override
    public void setMovementSpeed(float movementSpeed) {
        super.setMovementSpeed(movementSpeed);
        this.setForwardSpeed(movementSpeed);
    }

    public void stopMovement() {
        this.getNavigation().stop();
        this.setSidewaysSpeed(0.0f);
        this.setUpwardSpeed(0.0f);
        this.setMovementSpeed(0.0f);
    }

    @Override
    public void tickMovement() {
        super.tickMovement();
        this.getWorld().getProfiler().push("looting");
        if (!this.getWorld().isClient && this.canPickUpLoot() && this.isAlive() && !this.dead && this.getWorld().getGameRules().getBoolean(GameRules.DO_MOB_GRIEFING)) {
            Vec3i lv = this.getItemPickUpRangeExpander();
            List<ItemEntity> list = this.getWorld().getNonSpectatingEntities(ItemEntity.class, this.getBoundingBox().expand(lv.getX(), lv.getY(), lv.getZ()));
            for (ItemEntity lv2 : list) {
                if (lv2.isRemoved() || lv2.getStack().isEmpty() || lv2.cannotPickup() || !this.canGather(lv2.getStack())) continue;
                this.loot(lv2);
            }
        }
        this.getWorld().getProfiler().pop();
    }

    protected Vec3i getItemPickUpRangeExpander() {
        return ITEM_PICK_UP_RANGE_EXPANDER;
    }

    protected void loot(ItemEntity item) {
        ItemStack lv = item.getStack();
        ItemStack lv2 = this.tryEquip(lv.copy());
        if (!lv2.isEmpty()) {
            this.triggerItemPickedUpByEntityCriteria(item);
            this.sendPickup(item, lv2.getCount());
            lv.decrement(lv2.getCount());
            if (lv.isEmpty()) {
                item.discard();
            }
        }
    }

    public ItemStack tryEquip(ItemStack stack) {
        EquipmentSlot lv = this.getPreferredEquipmentSlot(stack);
        ItemStack lv2 = this.getEquippedStack(lv);
        boolean bl = this.prefersNewEquipment(stack, lv2);
        if (lv.isArmorSlot() && !bl) {
            lv = EquipmentSlot.MAINHAND;
            lv2 = this.getEquippedStack(lv);
            bl = lv2.isEmpty();
        }
        if (bl && this.canPickupItem(stack)) {
            double d = this.getDropChance(lv);
            if (!lv2.isEmpty() && (double)Math.max(this.random.nextFloat() - 0.1f, 0.0f) < d) {
                this.dropStack(lv2);
            }
            ItemStack lv3 = lv.split(stack);
            this.equipLootStack(lv, lv3);
            return lv3;
        }
        return ItemStack.EMPTY;
    }

    protected void equipLootStack(EquipmentSlot slot, ItemStack stack) {
        this.equipStack(slot, stack);
        this.updateDropChances(slot);
        this.persistent = true;
    }

    public void updateDropChances(EquipmentSlot slot) {
        switch (slot.getType()) {
            case HAND: {
                this.handDropChances[slot.getEntitySlotId()] = 2.0f;
                break;
            }
            case HUMANOID_ARMOR: {
                this.armorDropChances[slot.getEntitySlotId()] = 2.0f;
                break;
            }
            case ANIMAL_ARMOR: {
                this.bodyArmorDropChance = 2.0f;
            }
        }
    }

    protected boolean prefersNewEquipment(ItemStack newStack, ItemStack oldStack) {
        if (oldStack.isEmpty()) {
            return true;
        }
        if (newStack.getItem() instanceof SwordItem) {
            double e;
            if (!(oldStack.getItem() instanceof SwordItem)) {
                return true;
            }
            double d = this.getAttackDamageWith(newStack);
            if (d != (e = this.getAttackDamageWith(oldStack))) {
                return d > e;
            }
            return this.prefersNewDamageableItem(newStack, oldStack);
        }
        if (newStack.getItem() instanceof BowItem && oldStack.getItem() instanceof BowItem) {
            return this.prefersNewDamageableItem(newStack, oldStack);
        }
        if (newStack.getItem() instanceof CrossbowItem && oldStack.getItem() instanceof CrossbowItem) {
            return this.prefersNewDamageableItem(newStack, oldStack);
        }
        Item d = newStack.getItem();
        if (d instanceof ArmorItem) {
            ArmorItem lv = (ArmorItem)d;
            if (EnchantmentHelper.hasAnyEnchantmentsWith(oldStack, EnchantmentEffectComponentTypes.PREVENT_ARMOR_CHANGE)) {
                return false;
            }
            if (!(oldStack.getItem() instanceof ArmorItem)) {
                return true;
            }
            ArmorItem lv2 = (ArmorItem)oldStack.getItem();
            if (lv.getProtection() != lv2.getProtection()) {
                return lv.getProtection() > lv2.getProtection();
            }
            if (lv.getToughness() != lv2.getToughness()) {
                return lv.getToughness() > lv2.getToughness();
            }
            return this.prefersNewDamageableItem(newStack, oldStack);
        }
        if (newStack.getItem() instanceof MiningToolItem) {
            if (oldStack.getItem() instanceof BlockItem) {
                return true;
            }
            if (oldStack.getItem() instanceof MiningToolItem) {
                double e;
                double d2 = this.getAttackDamageWith(newStack);
                if (d2 != (e = this.getAttackDamageWith(oldStack))) {
                    return d2 > e;
                }
                return this.prefersNewDamageableItem(newStack, oldStack);
            }
        }
        return false;
    }

    private double getAttackDamageWith(ItemStack stack) {
        AttributeModifiersComponent lv = stack.getOrDefault(DataComponentTypes.ATTRIBUTE_MODIFIERS, AttributeModifiersComponent.DEFAULT);
        return lv.applyOperations(this.getAttributeBaseValue(EntityAttributes.GENERIC_ATTACK_DAMAGE), EquipmentSlot.MAINHAND);
    }

    public boolean prefersNewDamageableItem(ItemStack newStack, ItemStack oldStack) {
        if (newStack.getDamage() < oldStack.getDamage()) {
            return true;
        }
        return MobEntity.hasComponentsOtherThanDamage(newStack) && !MobEntity.hasComponentsOtherThanDamage(oldStack);
    }

    private static boolean hasComponentsOtherThanDamage(ItemStack stack) {
        ComponentMap lv = stack.getComponents();
        int i = lv.size();
        return i > 1 || i == 1 && !lv.contains(DataComponentTypes.DAMAGE);
    }

    public boolean canPickupItem(ItemStack stack) {
        return true;
    }

    public boolean canGather(ItemStack stack) {
        return this.canPickupItem(stack);
    }

    public boolean canImmediatelyDespawn(double distanceSquared) {
        return true;
    }

    public boolean cannotDespawn() {
        return this.hasVehicle();
    }

    protected boolean isDisallowedInPeaceful() {
        return false;
    }

    @Override
    public void checkDespawn() {
        if (this.getWorld().getDifficulty() == Difficulty.PEACEFUL && this.isDisallowedInPeaceful()) {
            this.discard();
            return;
        }
        if (this.isPersistent() || this.cannotDespawn()) {
            this.despawnCounter = 0;
            return;
        }
        PlayerEntity lv = this.getWorld().getClosestPlayer(this, -1.0);
        if (lv != null) {
            int i;
            int j;
            double d = lv.squaredDistanceTo(this);
            if (d > (double)(j = (i = this.getType().getSpawnGroup().getImmediateDespawnRange()) * i) && this.canImmediatelyDespawn(d)) {
                this.discard();
            }
            int k = this.getType().getSpawnGroup().getDespawnStartRange();
            int l = k * k;
            if (this.despawnCounter > 600 && this.random.nextInt(800) == 0 && d > (double)l && this.canImmediatelyDespawn(d)) {
                this.discard();
            } else if (d < (double)l) {
                this.despawnCounter = 0;
            }
        }
    }

    @Override
    protected final void tickNewAi() {
        ++this.despawnCounter;
        Profiler lv = this.getWorld().getProfiler();
        lv.push("sensing");
        this.visibilityCache.clear();
        lv.pop();
        int i = this.age + this.getId();
        if (i % 2 == 0 || this.age <= 1) {
            lv.push("targetSelector");
            this.targetSelector.tick();
            lv.pop();
            lv.push("goalSelector");
            this.goalSelector.tick();
            lv.pop();
        } else {
            lv.push("targetSelector");
            this.targetSelector.tickGoals(false);
            lv.pop();
            lv.push("goalSelector");
            this.goalSelector.tickGoals(false);
            lv.pop();
        }
        lv.push("navigation");
        this.navigation.tick();
        lv.pop();
        lv.push("mob tick");
        this.mobTick();
        lv.pop();
        lv.push("controls");
        lv.push("move");
        this.moveControl.tick();
        lv.swap("look");
        this.lookControl.tick();
        lv.swap("jump");
        this.jumpControl.tick();
        lv.pop();
        lv.pop();
        this.sendAiDebugData();
    }

    protected void sendAiDebugData() {
        DebugInfoSender.sendGoalSelector(this.getWorld(), this, this.goalSelector);
    }

    protected void mobTick() {
    }

    public int getMaxLookPitchChange() {
        return 40;
    }

    public int getMaxHeadRotation() {
        return 75;
    }

    protected void clampHeadYaw() {
        float f = this.getMaxHeadRotation();
        float g = this.getHeadYaw();
        float h = MathHelper.wrapDegrees(this.bodyYaw - g);
        float i = MathHelper.clamp(MathHelper.wrapDegrees(this.bodyYaw - g), -f, f);
        float j = g + h - i;
        this.setHeadYaw(j);
    }

    public int getMaxLookYawChange() {
        return 10;
    }

    public void lookAtEntity(Entity targetEntity, float maxYawChange, float maxPitchChange) {
        double h;
        double d = targetEntity.getX() - this.getX();
        double e = targetEntity.getZ() - this.getZ();
        if (targetEntity instanceof LivingEntity) {
            LivingEntity lv = (LivingEntity)targetEntity;
            h = lv.getEyeY() - this.getEyeY();
        } else {
            h = (targetEntity.getBoundingBox().minY + targetEntity.getBoundingBox().maxY) / 2.0 - this.getEyeY();
        }
        double i = Math.sqrt(d * d + e * e);
        float j = (float)(MathHelper.atan2(e, d) * 57.2957763671875) - 90.0f;
        float k = (float)(-(MathHelper.atan2(h, i) * 57.2957763671875));
        this.setPitch(this.changeAngle(this.getPitch(), k, maxPitchChange));
        this.setYaw(this.changeAngle(this.getYaw(), j, maxYawChange));
    }

    private float changeAngle(float from, float to, float max) {
        float i = MathHelper.wrapDegrees(to - from);
        if (i > max) {
            i = max;
        }
        if (i < -max) {
            i = -max;
        }
        return from + i;
    }

    public static boolean canMobSpawn(EntityType<? extends MobEntity> type, WorldAccess world, SpawnReason spawnReason, BlockPos pos, Random random) {
        BlockPos lv = pos.down();
        return spawnReason == SpawnReason.SPAWNER || world.getBlockState(lv).allowsSpawning(world, lv, type);
    }

    public boolean canSpawn(WorldAccess world, SpawnReason spawnReason) {
        return true;
    }

    public boolean canSpawn(WorldView world) {
        return !world.containsFluid(this.getBoundingBox()) && world.doesNotIntersectEntities(this);
    }

    public int getLimitPerChunk() {
        return 4;
    }

    public boolean spawnsTooManyForEachTry(int count) {
        return false;
    }

    @Override
    public int getSafeFallDistance() {
        if (this.getTarget() == null) {
            return this.getSafeFallDistance(0.0f);
        }
        int i = (int)(this.getHealth() - this.getMaxHealth() * 0.33f);
        if ((i -= (3 - this.getWorld().getDifficulty().getId()) * 4) < 0) {
            i = 0;
        }
        return this.getSafeFallDistance(i);
    }

    @Override
    public Iterable<ItemStack> getHandItems() {
        return this.handItems;
    }

    @Override
    public Iterable<ItemStack> getArmorItems() {
        return this.armorItems;
    }

    public ItemStack getBodyArmor() {
        return this.bodyArmor;
    }

    @Override
    public boolean canUseSlot(EquipmentSlot slot) {
        return slot != EquipmentSlot.BODY;
    }

    public boolean isWearingBodyArmor() {
        return !this.getEquippedStack(EquipmentSlot.BODY).isEmpty();
    }

    public boolean isHorseArmor(ItemStack stack) {
        return false;
    }

    public void equipBodyArmor(ItemStack stack) {
        this.equipLootStack(EquipmentSlot.BODY, stack);
    }

    @Override
    public Iterable<ItemStack> getAllArmorItems() {
        if (this.bodyArmor.isEmpty()) {
            return this.armorItems;
        }
        return Iterables.concat(this.armorItems, List.of(this.bodyArmor));
    }

    @Override
    public ItemStack getEquippedStack(EquipmentSlot slot) {
        return switch (slot.getType()) {
            default -> throw new MatchException(null, null);
            case EquipmentSlot.Type.HAND -> this.handItems.get(slot.getEntitySlotId());
            case EquipmentSlot.Type.HUMANOID_ARMOR -> this.armorItems.get(slot.getEntitySlotId());
            case EquipmentSlot.Type.ANIMAL_ARMOR -> this.bodyArmor;
        };
    }

    @Override
    public void equipStack(EquipmentSlot slot, ItemStack stack) {
        this.processEquippedStack(stack);
        switch (slot.getType()) {
            case HAND: {
                this.onEquipStack(slot, this.handItems.set(slot.getEntitySlotId(), stack), stack);
                break;
            }
            case HUMANOID_ARMOR: {
                this.onEquipStack(slot, this.armorItems.set(slot.getEntitySlotId(), stack), stack);
                break;
            }
            case ANIMAL_ARMOR: {
                ItemStack lv = this.bodyArmor;
                this.bodyArmor = stack;
                this.onEquipStack(slot, lv, stack);
            }
        }
    }

    @Override
    protected void dropEquipment(ServerWorld world, DamageSource source, boolean causedByPlayer) {
        super.dropEquipment(world, source, causedByPlayer);
        for (EquipmentSlot lv : EquipmentSlot.values()) {
            ItemStack lv2 = this.getEquippedStack(lv);
            float f = this.getDropChance(lv);
            if (f == 0.0f) continue;
            boolean bl2 = f > 1.0f;
            Object object = source.getAttacker();
            if (object instanceof LivingEntity) {
                LivingEntity lv3 = (LivingEntity)object;
                object = this.getWorld();
                if (object instanceof ServerWorld) {
                    ServerWorld lv4 = (ServerWorld)object;
                    f = EnchantmentHelper.getEquipmentDropChance(lv4, lv3, source, f);
                }
            }
            if (lv2.isEmpty() || EnchantmentHelper.hasAnyEnchantmentsWith(lv2, EnchantmentEffectComponentTypes.PREVENT_EQUIPMENT_DROP) || !causedByPlayer && !bl2 || !(this.random.nextFloat() < f)) continue;
            if (!bl2 && lv2.isDamageable()) {
                lv2.setDamage(lv2.getMaxDamage() - this.random.nextInt(1 + this.random.nextInt(Math.max(lv2.getMaxDamage() - 3, 1))));
            }
            this.dropStack(lv2);
            this.equipStack(lv, ItemStack.EMPTY);
        }
    }

    protected float getDropChance(EquipmentSlot slot) {
        return switch (slot.getType()) {
            default -> throw new MatchException(null, null);
            case EquipmentSlot.Type.HAND -> this.handDropChances[slot.getEntitySlotId()];
            case EquipmentSlot.Type.HUMANOID_ARMOR -> this.armorDropChances[slot.getEntitySlotId()];
            case EquipmentSlot.Type.ANIMAL_ARMOR -> this.bodyArmorDropChance;
        };
    }

    public void dropAllEquipment() {
        this.dropEquipment(stack -> true);
    }

    public Set<EquipmentSlot> dropEquipment(Predicate<ItemStack> dropPredicate) {
        HashSet<EquipmentSlot> set = new HashSet<EquipmentSlot>();
        for (EquipmentSlot lv : EquipmentSlot.values()) {
            ItemStack lv2 = this.getEquippedStack(lv);
            if (lv2.isEmpty()) continue;
            if (!dropPredicate.test(lv2)) {
                set.add(lv);
                continue;
            }
            double d = this.getDropChance(lv);
            if (!(d > 1.0)) continue;
            this.equipStack(lv, ItemStack.EMPTY);
            this.dropStack(lv2);
        }
        return set;
    }

    private LootContextParameterSet createEquipmentLootParameters(ServerWorld world) {
        return new LootContextParameterSet.Builder(world).add(LootContextParameters.ORIGIN, this.getPos()).add(LootContextParameters.THIS_ENTITY, this).build(LootContextTypes.EQUIPMENT);
    }

    public void setEquipmentFromTable(EquipmentTable equipmentTable) {
        this.setEquipmentFromTable(equipmentTable.lootTable(), equipmentTable.slotDropChances());
    }

    public void setEquipmentFromTable(RegistryKey<LootTable> lootTable, Map<EquipmentSlot, Float> slotDropChances) {
        World world = this.getWorld();
        if (world instanceof ServerWorld) {
            ServerWorld lv = (ServerWorld)world;
            this.setEquipmentFromTable(lootTable, this.createEquipmentLootParameters(lv), slotDropChances);
        }
    }

    protected void initEquipment(Random random, LocalDifficulty localDifficulty) {
        if (random.nextFloat() < 0.15f * localDifficulty.getClampedLocalDifficulty()) {
            float f;
            int i = random.nextInt(2);
            float f2 = f = this.getWorld().getDifficulty() == Difficulty.HARD ? 0.1f : 0.25f;
            if (random.nextFloat() < 0.095f) {
                ++i;
            }
            if (random.nextFloat() < 0.095f) {
                ++i;
            }
            if (random.nextFloat() < 0.095f) {
                ++i;
            }
            boolean bl = true;
            for (EquipmentSlot lv : EquipmentSlot.values()) {
                Item lv3;
                if (lv.getType() != EquipmentSlot.Type.HUMANOID_ARMOR) continue;
                ItemStack lv2 = this.getEquippedStack(lv);
                if (!bl && random.nextFloat() < f) break;
                bl = false;
                if (!lv2.isEmpty() || (lv3 = MobEntity.getEquipmentForSlot(lv, i)) == null) continue;
                this.equipStack(lv, new ItemStack(lv3));
            }
        }
    }

    @Nullable
    public static Item getEquipmentForSlot(EquipmentSlot equipmentSlot, int equipmentLevel) {
        switch (equipmentSlot) {
            case HEAD: {
                if (equipmentLevel == 0) {
                    return Items.LEATHER_HELMET;
                }
                if (equipmentLevel == 1) {
                    return Items.GOLDEN_HELMET;
                }
                if (equipmentLevel == 2) {
                    return Items.CHAINMAIL_HELMET;
                }
                if (equipmentLevel == 3) {
                    return Items.IRON_HELMET;
                }
                if (equipmentLevel == 4) {
                    return Items.DIAMOND_HELMET;
                }
            }
            case CHEST: {
                if (equipmentLevel == 0) {
                    return Items.LEATHER_CHESTPLATE;
                }
                if (equipmentLevel == 1) {
                    return Items.GOLDEN_CHESTPLATE;
                }
                if (equipmentLevel == 2) {
                    return Items.CHAINMAIL_CHESTPLATE;
                }
                if (equipmentLevel == 3) {
                    return Items.IRON_CHESTPLATE;
                }
                if (equipmentLevel == 4) {
                    return Items.DIAMOND_CHESTPLATE;
                }
            }
            case LEGS: {
                if (equipmentLevel == 0) {
                    return Items.LEATHER_LEGGINGS;
                }
                if (equipmentLevel == 1) {
                    return Items.GOLDEN_LEGGINGS;
                }
                if (equipmentLevel == 2) {
                    return Items.CHAINMAIL_LEGGINGS;
                }
                if (equipmentLevel == 3) {
                    return Items.IRON_LEGGINGS;
                }
                if (equipmentLevel == 4) {
                    return Items.DIAMOND_LEGGINGS;
                }
            }
            case FEET: {
                if (equipmentLevel == 0) {
                    return Items.LEATHER_BOOTS;
                }
                if (equipmentLevel == 1) {
                    return Items.GOLDEN_BOOTS;
                }
                if (equipmentLevel == 2) {
                    return Items.CHAINMAIL_BOOTS;
                }
                if (equipmentLevel == 3) {
                    return Items.IRON_BOOTS;
                }
                if (equipmentLevel != 4) break;
                return Items.DIAMOND_BOOTS;
            }
        }
        return null;
    }

    protected void updateEnchantments(ServerWorldAccess world, Random random, LocalDifficulty localDifficulty) {
        this.enchantMainHandItem(world, random, localDifficulty);
        for (EquipmentSlot lv : EquipmentSlot.values()) {
            if (lv.getType() != EquipmentSlot.Type.HUMANOID_ARMOR) continue;
            this.enchantEquipment(world, random, lv, localDifficulty);
        }
    }

    protected void enchantMainHandItem(ServerWorldAccess world, Random random, LocalDifficulty localDifficulty) {
        this.enchantEquipment(world, EquipmentSlot.MAINHAND, random, 0.25f, localDifficulty);
    }

    protected void enchantEquipment(ServerWorldAccess world, Random random, EquipmentSlot slot, LocalDifficulty localDifficulty) {
        this.enchantEquipment(world, slot, random, 0.5f, localDifficulty);
    }

    private void enchantEquipment(ServerWorldAccess world, EquipmentSlot slot, Random random, float power, LocalDifficulty localDifficulty) {
        ItemStack lv = this.getEquippedStack(slot);
        if (!lv.isEmpty() && random.nextFloat() < power * localDifficulty.getClampedLocalDifficulty()) {
            EnchantmentHelper.applyEnchantmentProvider(lv, world.getRegistryManager(), EnchantmentProviders.MOB_SPAWN_EQUIPMENT, localDifficulty, random);
            this.equipStack(slot, lv);
        }
    }

    @Nullable
    public EntityData initialize(ServerWorldAccess world, LocalDifficulty difficulty, SpawnReason spawnReason, @Nullable EntityData entityData) {
        Random lv = world.getRandom();
        EntityAttributeInstance lv2 = Objects.requireNonNull(this.getAttributeInstance(EntityAttributes.GENERIC_FOLLOW_RANGE));
        if (!lv2.hasModifier(RANDOM_SPAWN_BONUS_MODIFIER_ID)) {
            lv2.addPersistentModifier(new EntityAttributeModifier(RANDOM_SPAWN_BONUS_MODIFIER_ID, lv.nextTriangular(0.0, 0.11485000000000001), EntityAttributeModifier.Operation.ADD_MULTIPLIED_BASE));
        }
        this.setLeftHanded(lv.nextFloat() < 0.05f);
        return entityData;
    }

    public void setPersistent() {
        this.persistent = true;
    }

    @Override
    public void setEquipmentDropChance(EquipmentSlot slot, float dropChance) {
        switch (slot.getType()) {
            case HAND: {
                this.handDropChances[slot.getEntitySlotId()] = dropChance;
                break;
            }
            case HUMANOID_ARMOR: {
                this.armorDropChances[slot.getEntitySlotId()] = dropChance;
                break;
            }
            case ANIMAL_ARMOR: {
                this.bodyArmorDropChance = dropChance;
            }
        }
    }

    public boolean canPickUpLoot() {
        return this.canPickUpLoot;
    }

    public void setCanPickUpLoot(boolean canPickUpLoot) {
        this.canPickUpLoot = canPickUpLoot;
    }

    @Override
    public boolean canEquip(ItemStack stack) {
        EquipmentSlot lv = this.getPreferredEquipmentSlot(stack);
        return this.getEquippedStack(lv).isEmpty() && this.canPickUpLoot();
    }

    public boolean isPersistent() {
        return this.persistent;
    }

    @Override
    public final ActionResult interact(PlayerEntity player, Hand hand) {
        if (!this.isAlive()) {
            return ActionResult.PASS;
        }
        ActionResult lv = this.interactWithItem(player, hand);
        if (lv.isAccepted()) {
            this.emitGameEvent(GameEvent.ENTITY_INTERACT, player);
            return lv;
        }
        ActionResult lv2 = super.interact(player, hand);
        if (lv2 != ActionResult.PASS) {
            return lv2;
        }
        lv = this.interactMob(player, hand);
        if (lv.isAccepted()) {
            this.emitGameEvent(GameEvent.ENTITY_INTERACT, player);
            return lv;
        }
        return ActionResult.PASS;
    }

    private ActionResult interactWithItem(PlayerEntity player, Hand hand) {
        ActionResult lv2;
        ItemStack lv = player.getStackInHand(hand);
        if (lv.isOf(Items.NAME_TAG) && (lv2 = lv.useOnEntity(player, this, hand)).isAccepted()) {
            return lv2;
        }
        if (lv.getItem() instanceof SpawnEggItem) {
            if (this.getWorld() instanceof ServerWorld) {
                SpawnEggItem lv3 = (SpawnEggItem)lv.getItem();
                Optional<MobEntity> optional = lv3.spawnBaby(player, this, this.getType(), (ServerWorld)this.getWorld(), this.getPos(), lv);
                optional.ifPresent(entity -> this.onPlayerSpawnedChild(player, (MobEntity)entity));
                return optional.isPresent() ? ActionResult.SUCCESS : ActionResult.PASS;
            }
            return ActionResult.CONSUME;
        }
        return ActionResult.PASS;
    }

    protected void onPlayerSpawnedChild(PlayerEntity player, MobEntity child) {
    }

    protected ActionResult interactMob(PlayerEntity player, Hand hand) {
        return ActionResult.PASS;
    }

    public boolean isInWalkTargetRange() {
        return this.isInWalkTargetRange(this.getBlockPos());
    }

    public boolean isInWalkTargetRange(BlockPos pos) {
        if (this.positionTargetRange == -1.0f) {
            return true;
        }
        return this.positionTarget.getSquaredDistance(pos) < (double)(this.positionTargetRange * this.positionTargetRange);
    }

    public void setPositionTarget(BlockPos target, int range) {
        this.positionTarget = target;
        this.positionTargetRange = range;
    }

    public BlockPos getPositionTarget() {
        return this.positionTarget;
    }

    public float getPositionTargetRange() {
        return this.positionTargetRange;
    }

    public void clearPositionTarget() {
        this.positionTargetRange = -1.0f;
    }

    public boolean hasPositionTarget() {
        return this.positionTargetRange != -1.0f;
    }

    @Nullable
    public <T extends MobEntity> T convertTo(EntityType<T> entityType, boolean keepEquipment) {
        if (this.isRemoved()) {
            return null;
        }
        MobEntity lv = (MobEntity)entityType.create(this.getWorld());
        if (lv == null) {
            return null;
        }
        lv.copyPositionAndRotation(this);
        lv.setBaby(this.isBaby());
        lv.setAiDisabled(this.isAiDisabled());
        if (this.hasCustomName()) {
            lv.setCustomName(this.getCustomName());
            lv.setCustomNameVisible(this.isCustomNameVisible());
        }
        if (this.isPersistent()) {
            lv.setPersistent();
        }
        lv.setInvulnerable(this.isInvulnerable());
        if (keepEquipment) {
            lv.setCanPickUpLoot(this.canPickUpLoot());
            for (EquipmentSlot lv2 : EquipmentSlot.values()) {
                ItemStack lv3 = this.getEquippedStack(lv2);
                if (lv3.isEmpty()) continue;
                lv.equipStack(lv2, lv3.copyAndEmpty());
                lv.setEquipmentDropChance(lv2, this.getDropChance(lv2));
            }
        }
        this.getWorld().spawnEntity(lv);
        if (this.hasVehicle()) {
            Entity lv4 = this.getVehicle();
            this.stopRiding();
            lv.startRiding(lv4, true);
        }
        this.discard();
        return (T)lv;
    }

    @Override
    @Nullable
    public Leashable.LeashData getLeashData() {
        return this.leashData;
    }

    @Override
    public void setLeashData(@Nullable Leashable.LeashData leashData) {
        this.leashData = leashData;
    }

    @Override
    public void detachLeash(boolean sendPacket, boolean dropItem) {
        Leashable.super.detachLeash(sendPacket, dropItem);
        if (this.getLeashData() == null) {
            this.clearPositionTarget();
        }
    }

    @Override
    public void detachLeash() {
        Leashable.super.detachLeash();
        this.goalSelector.disableControl(Goal.Control.MOVE);
    }

    @Override
    public boolean canBeLeashed() {
        return !(this instanceof Monster);
    }

    @Override
    public boolean startRiding(Entity entity, boolean force) {
        boolean bl2 = super.startRiding(entity, force);
        if (bl2 && this.isLeashed()) {
            this.detachLeash(true, true);
        }
        return bl2;
    }

    @Override
    public boolean canMoveVoluntarily() {
        return super.canMoveVoluntarily() && !this.isAiDisabled();
    }

    public void setAiDisabled(boolean aiDisabled) {
        byte b = this.dataTracker.get(MOB_FLAGS);
        this.dataTracker.set(MOB_FLAGS, aiDisabled ? (byte)(b | 1) : (byte)(b & 0xFFFFFFFE));
    }

    public void setLeftHanded(boolean leftHanded) {
        byte b = this.dataTracker.get(MOB_FLAGS);
        this.dataTracker.set(MOB_FLAGS, leftHanded ? (byte)(b | 2) : (byte)(b & 0xFFFFFFFD));
    }

    public void setAttacking(boolean attacking) {
        byte b = this.dataTracker.get(MOB_FLAGS);
        this.dataTracker.set(MOB_FLAGS, attacking ? (byte)(b | 4) : (byte)(b & 0xFFFFFFFB));
    }

    public boolean isAiDisabled() {
        return (this.dataTracker.get(MOB_FLAGS) & 1) != 0;
    }

    public boolean isLeftHanded() {
        return (this.dataTracker.get(MOB_FLAGS) & 2) != 0;
    }

    public boolean isAttacking() {
        return (this.dataTracker.get(MOB_FLAGS) & 4) != 0;
    }

    public void setBaby(boolean baby) {
    }

    @Override
    public Arm getMainArm() {
        return this.isLeftHanded() ? Arm.LEFT : Arm.RIGHT;
    }

    public boolean isInAttackRange(LivingEntity entity) {
        return this.getAttackBox().intersects(entity.getHitbox());
    }

    protected Box getAttackBox() {
        Box lv4;
        Entity lv = this.getVehicle();
        if (lv != null) {
            Box lv2 = lv.getBoundingBox();
            Box lv3 = this.getBoundingBox();
            lv4 = new Box(Math.min(lv3.minX, lv2.minX), lv3.minY, Math.min(lv3.minZ, lv2.minZ), Math.max(lv3.maxX, lv2.maxX), lv3.maxY, Math.max(lv3.maxZ, lv2.maxZ));
        } else {
            lv4 = this.getBoundingBox();
        }
        return lv4.expand(ATTACK_RANGE, 0.0, ATTACK_RANGE);
    }

    @Override
    public boolean tryAttack(Entity target) {
        boolean bl;
        float f = (float)this.getAttributeValue(EntityAttributes.GENERIC_ATTACK_DAMAGE);
        DamageSource lv = this.getDamageSources().mobAttack(this);
        World world = this.getWorld();
        if (world instanceof ServerWorld) {
            ServerWorld lv2 = (ServerWorld)world;
            f = EnchantmentHelper.getDamage(lv2, this.getWeaponStack(), target, lv, f);
        }
        if (bl = target.damage(lv, f)) {
            World world2;
            float g = this.getKnockbackAgainst(target, lv);
            if (g > 0.0f && target instanceof LivingEntity) {
                LivingEntity lv3 = (LivingEntity)target;
                lv3.takeKnockback(g * 0.5f, MathHelper.sin(this.getYaw() * ((float)Math.PI / 180)), -MathHelper.cos(this.getYaw() * ((float)Math.PI / 180)));
                this.setVelocity(this.getVelocity().multiply(0.6, 1.0, 0.6));
            }
            if ((world2 = this.getWorld()) instanceof ServerWorld) {
                ServerWorld lv4 = (ServerWorld)world2;
                EnchantmentHelper.onTargetDamaged(lv4, target, lv);
            }
            this.onAttacking(target);
            this.playAttackSound();
        }
        return bl;
    }

    protected void playAttackSound() {
    }

    protected boolean isAffectedByDaylight() {
        if (this.getWorld().isDay() && !this.getWorld().isClient) {
            boolean bl;
            float f = this.getBrightnessAtEyes();
            BlockPos lv = BlockPos.ofFloored(this.getX(), this.getEyeY(), this.getZ());
            boolean bl2 = bl = this.isWet() || this.inPowderSnow || this.wasInPowderSnow;
            if (f > 0.5f && this.random.nextFloat() * 30.0f < (f - 0.4f) * 2.0f && !bl && this.getWorld().isSkyVisible(lv)) {
                return true;
            }
        }
        return false;
    }

    @Override
    protected void swimUpward(TagKey<Fluid> fluid) {
        if (this.getNavigation().canSwim()) {
            super.swimUpward(fluid);
        } else {
            this.setVelocity(this.getVelocity().add(0.0, 0.3, 0.0));
        }
    }

    @VisibleForTesting
    public void clearGoalsAndTasks() {
        this.clearGoals(goal -> true);
        this.getBrain().clear();
    }

    public void clearGoals(Predicate<Goal> predicate) {
        this.goalSelector.clear(predicate);
    }

    @Override
    protected void removeFromDimension() {
        super.removeFromDimension();
        this.getEquippedItems().forEach(stack -> {
            if (!stack.isEmpty()) {
                stack.setCount(0);
            }
        });
    }

    @Override
    @Nullable
    public ItemStack getPickBlockStack() {
        SpawnEggItem lv = SpawnEggItem.forEntity(this.getType());
        if (lv == null) {
            return null;
        }
        return new ItemStack(lv);
    }
}

