/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 * 
 * Could not load the following classes:
 *  org.jetbrains.annotations.Nullable
 */
package net.minecraft.entity.passive;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.mojang.datafixers.util.Pair;
import com.mojang.logging.LogUtils;
import com.mojang.serialization.Dynamic;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.BiPredicate;
import java.util.stream.Collectors;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityData;
import net.minecraft.entity.EntityInteraction;
import net.minecraft.entity.EntityStatuses;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.ExperienceOrbEntity;
import net.minecraft.entity.InteractionObserver;
import net.minecraft.entity.InventoryOwner;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.LargeEntitySpawnHelper;
import net.minecraft.entity.LightningEntity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.SpawnReason;
import net.minecraft.entity.ai.brain.Activity;
import net.minecraft.entity.ai.brain.Brain;
import net.minecraft.entity.ai.brain.LivingTargetCache;
import net.minecraft.entity.ai.brain.MemoryModuleState;
import net.minecraft.entity.ai.brain.MemoryModuleType;
import net.minecraft.entity.ai.brain.Schedule;
import net.minecraft.entity.ai.brain.sensor.GolemLastSeenSensor;
import net.minecraft.entity.ai.brain.sensor.Sensor;
import net.minecraft.entity.ai.brain.sensor.SensorType;
import net.minecraft.entity.ai.brain.task.VillagerTaskListProvider;
import net.minecraft.entity.ai.pathing.MobNavigation;
import net.minecraft.entity.attribute.DefaultAttributeContainer;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.data.TrackedData;
import net.minecraft.entity.data.TrackedDataHandlerRegistry;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.mob.WitchEntity;
import net.minecraft.entity.passive.MerchantEntity;
import net.minecraft.entity.passive.PassiveEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.SimpleInventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtList;
import net.minecraft.nbt.NbtOps;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.registry.Registries;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.registry.tag.ItemTags;
import net.minecraft.resource.featuretoggle.FeatureFlags;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.DebugInfoSender;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundEvent;
import net.minecraft.sound.SoundEvents;
import net.minecraft.stat.Stats;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.GlobalPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.village.TradeOffer;
import net.minecraft.village.TradeOfferList;
import net.minecraft.village.TradeOffers;
import net.minecraft.village.VillageGossipType;
import net.minecraft.village.VillagerData;
import net.minecraft.village.VillagerDataContainer;
import net.minecraft.village.VillagerGossips;
import net.minecraft.village.VillagerProfession;
import net.minecraft.village.VillagerType;
import net.minecraft.village.raid.Raid;
import net.minecraft.world.Difficulty;
import net.minecraft.world.LocalDifficulty;
import net.minecraft.world.ServerWorldAccess;
import net.minecraft.world.World;
import net.minecraft.world.poi.PointOfInterestStorage;
import net.minecraft.world.poi.PointOfInterestType;
import net.minecraft.world.poi.PointOfInterestTypes;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;

public class VillagerEntity
extends MerchantEntity
implements InteractionObserver,
VillagerDataContainer {
    private static final Logger LOGGER = LogUtils.getLogger();
    private static final TrackedData<VillagerData> VILLAGER_DATA = DataTracker.registerData(VillagerEntity.class, TrackedDataHandlerRegistry.VILLAGER_DATA);
    public static final int field_30602 = 12;
    public static final Map<Item, Integer> ITEM_FOOD_VALUES = ImmutableMap.of(Items.BREAD, 4, Items.POTATO, 1, Items.CARROT, 1, Items.BEETROOT, 1);
    private static final int field_30604 = 2;
    private static final Set<Item> GATHERABLE_ITEMS = ImmutableSet.of(Items.BREAD, Items.POTATO, Items.CARROT, Items.WHEAT, Items.WHEAT_SEEDS, Items.BEETROOT, new Item[]{Items.BEETROOT_SEEDS, Items.TORCHFLOWER_SEEDS, Items.PITCHER_POD});
    private static final int field_30605 = 10;
    private static final int field_30606 = 1200;
    private static final int field_30607 = 24000;
    private static final int field_30608 = 25;
    private static final int field_30609 = 10;
    private static final int field_30610 = 5;
    private static final long field_30611 = 24000L;
    @VisibleForTesting
    public static final float field_30603 = 0.5f;
    private int levelUpTimer;
    private boolean levelingUp;
    @Nullable
    private PlayerEntity lastCustomer;
    private boolean field_30612;
    private int foodLevel;
    private final VillagerGossips gossip = new VillagerGossips();
    private long gossipStartTime;
    private long lastGossipDecayTime;
    private int experience;
    private long lastRestockTime;
    private int restocksToday;
    private long lastRestockCheckTime;
    private boolean natural;
    private static final ImmutableList<MemoryModuleType<?>> MEMORY_MODULES = ImmutableList.of(MemoryModuleType.HOME, MemoryModuleType.JOB_SITE, MemoryModuleType.POTENTIAL_JOB_SITE, MemoryModuleType.MEETING_POINT, MemoryModuleType.MOBS, MemoryModuleType.VISIBLE_MOBS, MemoryModuleType.VISIBLE_VILLAGER_BABIES, MemoryModuleType.NEAREST_PLAYERS, MemoryModuleType.NEAREST_VISIBLE_PLAYER, MemoryModuleType.NEAREST_VISIBLE_TARGETABLE_PLAYER, MemoryModuleType.NEAREST_VISIBLE_WANTED_ITEM, MemoryModuleType.ITEM_PICKUP_COOLDOWN_TICKS, new MemoryModuleType[]{MemoryModuleType.WALK_TARGET, MemoryModuleType.LOOK_TARGET, MemoryModuleType.INTERACTION_TARGET, MemoryModuleType.BREED_TARGET, MemoryModuleType.PATH, MemoryModuleType.DOORS_TO_CLOSE, MemoryModuleType.NEAREST_BED, MemoryModuleType.HURT_BY, MemoryModuleType.HURT_BY_ENTITY, MemoryModuleType.NEAREST_HOSTILE, MemoryModuleType.SECONDARY_JOB_SITE, MemoryModuleType.HIDING_PLACE, MemoryModuleType.HEARD_BELL_TIME, MemoryModuleType.CANT_REACH_WALK_TARGET_SINCE, MemoryModuleType.LAST_SLEPT, MemoryModuleType.LAST_WOKEN, MemoryModuleType.LAST_WORKED_AT_POI, MemoryModuleType.GOLEM_DETECTED_RECENTLY});
    private static final ImmutableList<SensorType<? extends Sensor<? super VillagerEntity>>> SENSORS = ImmutableList.of(SensorType.NEAREST_LIVING_ENTITIES, SensorType.NEAREST_PLAYERS, SensorType.NEAREST_ITEMS, SensorType.NEAREST_BED, SensorType.HURT_BY, SensorType.VILLAGER_HOSTILES, SensorType.VILLAGER_BABIES, SensorType.SECONDARY_POIS, SensorType.GOLEM_DETECTED);
    public static final Map<MemoryModuleType<GlobalPos>, BiPredicate<VillagerEntity, RegistryEntry<PointOfInterestType>>> POINTS_OF_INTEREST = ImmutableMap.of(MemoryModuleType.HOME, (villager, arg2) -> arg2.matchesKey(PointOfInterestTypes.HOME), MemoryModuleType.JOB_SITE, (villager, arg2) -> villager.getVillagerData().getProfession().heldWorkstation().test((RegistryEntry<PointOfInterestType>)arg2), MemoryModuleType.POTENTIAL_JOB_SITE, (villager, arg2) -> VillagerProfession.IS_ACQUIRABLE_JOB_SITE.test((RegistryEntry<PointOfInterestType>)arg2), MemoryModuleType.MEETING_POINT, (villager, arg2) -> arg2.matchesKey(PointOfInterestTypes.MEETING));

    public VillagerEntity(EntityType<? extends VillagerEntity> arg, World arg2) {
        this(arg, arg2, VillagerType.PLAINS);
    }

    public VillagerEntity(EntityType<? extends VillagerEntity> entityType, World world, VillagerType type) {
        super((EntityType<? extends MerchantEntity>)entityType, world);
        ((MobNavigation)this.getNavigation()).setCanPathThroughDoors(true);
        this.getNavigation().setCanSwim(true);
        this.setCanPickUpLoot(true);
        this.setVillagerData(this.getVillagerData().withType(type).withProfession(VillagerProfession.NONE));
    }

    public Brain<VillagerEntity> getBrain() {
        return super.getBrain();
    }

    protected Brain.Profile<VillagerEntity> createBrainProfile() {
        return Brain.createProfile(MEMORY_MODULES, SENSORS);
    }

    @Override
    protected Brain<?> deserializeBrain(Dynamic<?> dynamic) {
        Brain<VillagerEntity> lv = this.createBrainProfile().deserialize(dynamic);
        this.initBrain(lv);
        return lv;
    }

    public void reinitializeBrain(ServerWorld world) {
        Brain<VillagerEntity> lv = this.getBrain();
        lv.stopAllTasks(world, this);
        this.brain = lv.copy();
        this.initBrain(this.getBrain());
    }

    private void initBrain(Brain<VillagerEntity> brain) {
        VillagerProfession lv = this.getVillagerData().getProfession();
        if (this.isBaby()) {
            brain.setSchedule(Schedule.VILLAGER_BABY);
            brain.setTaskList(Activity.PLAY, VillagerTaskListProvider.createPlayTasks(0.5f));
        } else {
            brain.setSchedule(Schedule.VILLAGER_DEFAULT);
            brain.setTaskList(Activity.WORK, VillagerTaskListProvider.createWorkTasks(lv, 0.5f), ImmutableSet.of(Pair.of(MemoryModuleType.JOB_SITE, MemoryModuleState.VALUE_PRESENT)));
        }
        brain.setTaskList(Activity.CORE, VillagerTaskListProvider.createCoreTasks(lv, 0.5f));
        brain.setTaskList(Activity.MEET, VillagerTaskListProvider.createMeetTasks(lv, 0.5f), ImmutableSet.of(Pair.of(MemoryModuleType.MEETING_POINT, MemoryModuleState.VALUE_PRESENT)));
        brain.setTaskList(Activity.REST, VillagerTaskListProvider.createRestTasks(lv, 0.5f));
        brain.setTaskList(Activity.IDLE, VillagerTaskListProvider.createIdleTasks(lv, 0.5f));
        brain.setTaskList(Activity.PANIC, VillagerTaskListProvider.createPanicTasks(lv, 0.5f));
        brain.setTaskList(Activity.PRE_RAID, VillagerTaskListProvider.createPreRaidTasks(lv, 0.5f));
        brain.setTaskList(Activity.RAID, VillagerTaskListProvider.createRaidTasks(lv, 0.5f));
        brain.setTaskList(Activity.HIDE, VillagerTaskListProvider.createHideTasks(lv, 0.5f));
        brain.setCoreActivities(ImmutableSet.of(Activity.CORE));
        brain.setDefaultActivity(Activity.IDLE);
        brain.doExclusively(Activity.IDLE);
        brain.refreshActivities(this.getWorld().getTimeOfDay(), this.getWorld().getTime());
    }

    @Override
    protected void onGrowUp() {
        super.onGrowUp();
        if (this.getWorld() instanceof ServerWorld) {
            this.reinitializeBrain((ServerWorld)this.getWorld());
        }
    }

    public static DefaultAttributeContainer.Builder createVillagerAttributes() {
        return MobEntity.createMobAttributes().add(EntityAttributes.GENERIC_MOVEMENT_SPEED, 0.5).add(EntityAttributes.GENERIC_FOLLOW_RANGE, 48.0);
    }

    public boolean isNatural() {
        return this.natural;
    }

    @Override
    protected void mobTick() {
        Raid lv;
        this.getWorld().getProfiler().push("villagerBrain");
        this.getBrain().tick((ServerWorld)this.getWorld(), this);
        this.getWorld().getProfiler().pop();
        if (this.natural) {
            this.natural = false;
        }
        if (!this.hasCustomer() && this.levelUpTimer > 0) {
            --this.levelUpTimer;
            if (this.levelUpTimer <= 0) {
                if (this.levelingUp) {
                    this.levelUp();
                    this.levelingUp = false;
                }
                this.addStatusEffect(new StatusEffectInstance(StatusEffects.REGENERATION, 200, 0));
            }
        }
        if (this.lastCustomer != null && this.getWorld() instanceof ServerWorld) {
            ((ServerWorld)this.getWorld()).handleInteraction(EntityInteraction.TRADE, this.lastCustomer, this);
            this.getWorld().sendEntityStatus(this, EntityStatuses.ADD_VILLAGER_HAPPY_PARTICLES);
            this.lastCustomer = null;
        }
        if (!this.isAiDisabled() && this.random.nextInt(100) == 0 && (lv = ((ServerWorld)this.getWorld()).getRaidAt(this.getBlockPos())) != null && lv.isActive() && !lv.isFinished()) {
            this.getWorld().sendEntityStatus(this, EntityStatuses.ADD_SPLASH_PARTICLES);
        }
        if (this.getVillagerData().getProfession() == VillagerProfession.NONE && this.hasCustomer()) {
            this.resetCustomer();
        }
        super.mobTick();
    }

    @Override
    public void tick() {
        super.tick();
        if (this.getHeadRollingTimeLeft() > 0) {
            this.setHeadRollingTimeLeft(this.getHeadRollingTimeLeft() - 1);
        }
        this.decayGossip();
    }

    @Override
    public ActionResult interactMob(PlayerEntity player, Hand hand) {
        ItemStack lv = player.getStackInHand(hand);
        if (!lv.isOf(Items.VILLAGER_SPAWN_EGG) && this.isAlive() && !this.hasCustomer() && !this.isSleeping()) {
            if (this.isBaby()) {
                this.sayNo();
                return ActionResult.success(this.getWorld().isClient);
            }
            if (!this.getWorld().isClient) {
                boolean bl = this.getOffers().isEmpty();
                if (hand == Hand.MAIN_HAND) {
                    if (bl) {
                        this.sayNo();
                    }
                    player.incrementStat(Stats.TALKED_TO_VILLAGER);
                }
                if (bl) {
                    return ActionResult.CONSUME;
                }
                this.beginTradeWith(player);
            }
            return ActionResult.success(this.getWorld().isClient);
        }
        return super.interactMob(player, hand);
    }

    private void sayNo() {
        this.setHeadRollingTimeLeft(40);
        if (!this.getWorld().isClient()) {
            this.playSound(SoundEvents.ENTITY_VILLAGER_NO);
        }
    }

    private void beginTradeWith(PlayerEntity customer) {
        this.prepareOffersFor(customer);
        this.setCustomer(customer);
        this.sendOffers(customer, this.getDisplayName(), this.getVillagerData().getLevel());
    }

    @Override
    public void setCustomer(@Nullable PlayerEntity customer) {
        boolean bl = this.getCustomer() != null && customer == null;
        super.setCustomer(customer);
        if (bl) {
            this.resetCustomer();
        }
    }

    @Override
    protected void resetCustomer() {
        super.resetCustomer();
        this.clearSpecialPrices();
    }

    private void clearSpecialPrices() {
        if (this.getWorld().isClient()) {
            return;
        }
        for (TradeOffer lv : this.getOffers()) {
            lv.clearSpecialPrice();
        }
    }

    @Override
    public boolean canRefreshTrades() {
        return true;
    }

    @Override
    public boolean isClient() {
        return this.getWorld().isClient;
    }

    public void restock() {
        this.updateDemandBonus();
        for (TradeOffer lv : this.getOffers()) {
            lv.resetUses();
        }
        this.sendOffersToCustomer();
        this.lastRestockTime = this.getWorld().getTime();
        ++this.restocksToday;
    }

    private void sendOffersToCustomer() {
        TradeOfferList lv = this.getOffers();
        PlayerEntity lv2 = this.getCustomer();
        if (lv2 != null && !lv.isEmpty()) {
            lv2.sendTradeOffers(lv2.currentScreenHandler.syncId, lv, this.getVillagerData().getLevel(), this.getExperience(), this.isLeveledMerchant(), this.canRefreshTrades());
        }
    }

    private boolean needsRestock() {
        for (TradeOffer lv : this.getOffers()) {
            if (!lv.hasBeenUsed()) continue;
            return true;
        }
        return false;
    }

    private boolean canRestock() {
        return this.restocksToday == 0 || this.restocksToday < 2 && this.getWorld().getTime() > this.lastRestockTime + 2400L;
    }

    public boolean shouldRestock() {
        long l = this.lastRestockTime + 12000L;
        long m = this.getWorld().getTime();
        boolean bl = m > l;
        long n = this.getWorld().getTimeOfDay();
        if (this.lastRestockCheckTime > 0L) {
            long p = n / 24000L;
            long o = this.lastRestockCheckTime / 24000L;
            bl |= p > o;
        }
        this.lastRestockCheckTime = n;
        if (bl) {
            this.lastRestockTime = m;
            this.clearDailyRestockCount();
        }
        return this.canRestock() && this.needsRestock();
    }

    private void restockAndUpdateDemandBonus() {
        int i = 2 - this.restocksToday;
        if (i > 0) {
            for (TradeOffer lv : this.getOffers()) {
                lv.resetUses();
            }
        }
        for (int j = 0; j < i; ++j) {
            this.updateDemandBonus();
        }
        this.sendOffersToCustomer();
    }

    private void updateDemandBonus() {
        for (TradeOffer lv : this.getOffers()) {
            lv.updateDemandBonus();
        }
    }

    private void prepareOffersFor(PlayerEntity player) {
        int i = this.getReputation(player);
        if (i != 0) {
            for (TradeOffer lv : this.getOffers()) {
                lv.increaseSpecialPrice(-MathHelper.floor((float)i * lv.getPriceMultiplier()));
            }
        }
        if (player.hasStatusEffect(StatusEffects.HERO_OF_THE_VILLAGE)) {
            StatusEffectInstance lv2 = player.getStatusEffect(StatusEffects.HERO_OF_THE_VILLAGE);
            int j = lv2.getAmplifier();
            for (TradeOffer lv3 : this.getOffers()) {
                double d = 0.3 + 0.0625 * (double)j;
                int k = (int)Math.floor(d * (double)lv3.getOriginalFirstBuyItem().getCount());
                lv3.increaseSpecialPrice(-Math.max(k, 1));
            }
        }
    }

    @Override
    protected void initDataTracker(DataTracker.Builder builder) {
        super.initDataTracker(builder);
        builder.add(VILLAGER_DATA, new VillagerData(VillagerType.PLAINS, VillagerProfession.NONE, 1));
    }

    @Override
    public void writeCustomDataToNbt(NbtCompound nbt) {
        super.writeCustomDataToNbt(nbt);
        VillagerData.CODEC.encodeStart(NbtOps.INSTANCE, this.getVillagerData()).resultOrPartial(LOGGER::error).ifPresent(arg2 -> nbt.put("VillagerData", (NbtElement)arg2));
        nbt.putByte("FoodLevel", (byte)this.foodLevel);
        nbt.put("Gossips", this.gossip.serialize(NbtOps.INSTANCE));
        nbt.putInt("Xp", this.experience);
        nbt.putLong("LastRestock", this.lastRestockTime);
        nbt.putLong("LastGossipDecay", this.lastGossipDecayTime);
        nbt.putInt("RestocksToday", this.restocksToday);
        if (this.natural) {
            nbt.putBoolean("AssignProfessionWhenSpawned", true);
        }
    }

    @Override
    public void readCustomDataFromNbt(NbtCompound nbt) {
        super.readCustomDataFromNbt(nbt);
        if (nbt.contains("VillagerData", NbtElement.COMPOUND_TYPE)) {
            VillagerData.CODEC.parse(NbtOps.INSTANCE, nbt.get("VillagerData")).resultOrPartial(LOGGER::error).ifPresent(villagerData -> this.dataTracker.set(VILLAGER_DATA, villagerData));
        }
        if (nbt.contains("FoodLevel", NbtElement.BYTE_TYPE)) {
            this.foodLevel = nbt.getByte("FoodLevel");
        }
        NbtList lv = nbt.getList("Gossips", NbtElement.COMPOUND_TYPE);
        this.gossip.deserialize(new Dynamic<NbtList>(NbtOps.INSTANCE, lv));
        if (nbt.contains("Xp", NbtElement.INT_TYPE)) {
            this.experience = nbt.getInt("Xp");
        }
        this.lastRestockTime = nbt.getLong("LastRestock");
        this.lastGossipDecayTime = nbt.getLong("LastGossipDecay");
        this.setCanPickUpLoot(true);
        if (this.getWorld() instanceof ServerWorld) {
            this.reinitializeBrain((ServerWorld)this.getWorld());
        }
        this.restocksToday = nbt.getInt("RestocksToday");
        if (nbt.contains("AssignProfessionWhenSpawned")) {
            this.natural = nbt.getBoolean("AssignProfessionWhenSpawned");
        }
    }

    @Override
    public boolean canImmediatelyDespawn(double distanceSquared) {
        return false;
    }

    @Override
    @Nullable
    protected SoundEvent getAmbientSound() {
        if (this.isSleeping()) {
            return null;
        }
        if (this.hasCustomer()) {
            return SoundEvents.ENTITY_VILLAGER_TRADE;
        }
        return SoundEvents.ENTITY_VILLAGER_AMBIENT;
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource source) {
        return SoundEvents.ENTITY_VILLAGER_HURT;
    }

    @Override
    protected SoundEvent getDeathSound() {
        return SoundEvents.ENTITY_VILLAGER_DEATH;
    }

    public void playWorkSound() {
        this.playSound(this.getVillagerData().getProfession().workSound());
    }

    @Override
    public void setVillagerData(VillagerData villagerData) {
        VillagerData lv = this.getVillagerData();
        if (lv.getProfession() != villagerData.getProfession()) {
            this.offers = null;
        }
        this.dataTracker.set(VILLAGER_DATA, villagerData);
    }

    @Override
    public VillagerData getVillagerData() {
        return this.dataTracker.get(VILLAGER_DATA);
    }

    @Override
    protected void afterUsing(TradeOffer offer) {
        int i = 3 + this.random.nextInt(4);
        this.experience += offer.getMerchantExperience();
        this.lastCustomer = this.getCustomer();
        if (this.canLevelUp()) {
            this.levelUpTimer = 40;
            this.levelingUp = true;
            i += 5;
        }
        if (offer.shouldRewardPlayerExperience()) {
            this.getWorld().spawnEntity(new ExperienceOrbEntity(this.getWorld(), this.getX(), this.getY() + 0.5, this.getZ(), i));
        }
    }

    public void method_35201(boolean bl) {
        this.field_30612 = bl;
    }

    public boolean method_35200() {
        return this.field_30612;
    }

    @Override
    public void setAttacker(@Nullable LivingEntity attacker) {
        if (attacker != null && this.getWorld() instanceof ServerWorld) {
            ((ServerWorld)this.getWorld()).handleInteraction(EntityInteraction.VILLAGER_HURT, attacker, this);
            if (this.isAlive() && attacker instanceof PlayerEntity) {
                this.getWorld().sendEntityStatus(this, EntityStatuses.ADD_VILLAGER_ANGRY_PARTICLES);
            }
        }
        super.setAttacker(attacker);
    }

    @Override
    public void onDeath(DamageSource damageSource) {
        LOGGER.info("Villager {} died, message: '{}'", (Object)this, (Object)damageSource.getDeathMessage(this).getString());
        Entity lv = damageSource.getAttacker();
        if (lv != null) {
            this.notifyDeath(lv);
        }
        this.releaseAllTickets();
        super.onDeath(damageSource);
    }

    private void releaseAllTickets() {
        this.releaseTicketFor(MemoryModuleType.HOME);
        this.releaseTicketFor(MemoryModuleType.JOB_SITE);
        this.releaseTicketFor(MemoryModuleType.POTENTIAL_JOB_SITE);
        this.releaseTicketFor(MemoryModuleType.MEETING_POINT);
    }

    private void notifyDeath(Entity killer) {
        World world = this.getWorld();
        if (!(world instanceof ServerWorld)) {
            return;
        }
        ServerWorld lv = (ServerWorld)world;
        Optional<LivingTargetCache> optional = this.brain.getOptionalRegisteredMemory(MemoryModuleType.VISIBLE_MOBS);
        if (optional.isEmpty()) {
            return;
        }
        optional.get().iterate(InteractionObserver.class::isInstance).forEach(observer -> lv.handleInteraction(EntityInteraction.VILLAGER_KILLED, killer, (InteractionObserver)((Object)observer)));
    }

    public void releaseTicketFor(MemoryModuleType<GlobalPos> pos) {
        if (!(this.getWorld() instanceof ServerWorld)) {
            return;
        }
        MinecraftServer minecraftServer = ((ServerWorld)this.getWorld()).getServer();
        this.brain.getOptionalRegisteredMemory(pos).ifPresent(posx -> {
            ServerWorld lv = minecraftServer.getWorld(posx.dimension());
            if (lv == null) {
                return;
            }
            PointOfInterestStorage lv2 = lv.getPointOfInterestStorage();
            Optional<RegistryEntry<PointOfInterestType>> optional = lv2.getType(posx.pos());
            BiPredicate<VillagerEntity, RegistryEntry<PointOfInterestType>> biPredicate = POINTS_OF_INTEREST.get(pos);
            if (optional.isPresent() && biPredicate.test(this, optional.get())) {
                lv2.releaseTicket(posx.pos());
                DebugInfoSender.sendPointOfInterest(lv, posx.pos());
            }
        });
    }

    @Override
    public boolean isReadyToBreed() {
        return this.foodLevel + this.getAvailableFood() >= 12 && !this.isSleeping() && this.getBreedingAge() == 0;
    }

    private boolean lacksFood() {
        return this.foodLevel < 12;
    }

    private void consumeAvailableFood() {
        if (!this.lacksFood() || this.getAvailableFood() == 0) {
            return;
        }
        for (int i = 0; i < this.getInventory().size(); ++i) {
            int j;
            Integer integer;
            ItemStack lv = this.getInventory().getStack(i);
            if (lv.isEmpty() || (integer = ITEM_FOOD_VALUES.get(lv.getItem())) == null) continue;
            for (int k = j = lv.getCount(); k > 0; --k) {
                this.foodLevel += integer.intValue();
                this.getInventory().removeStack(i, 1);
                if (this.lacksFood()) continue;
                return;
            }
        }
    }

    public int getReputation(PlayerEntity player) {
        return this.gossip.getReputationFor(player.getUuid(), gossipType -> true);
    }

    private void depleteFood(int amount) {
        this.foodLevel -= amount;
    }

    public void eatForBreeding() {
        this.consumeAvailableFood();
        this.depleteFood(12);
    }

    public void setOffers(TradeOfferList offers) {
        this.offers = offers;
    }

    private boolean canLevelUp() {
        int i = this.getVillagerData().getLevel();
        return VillagerData.canLevelUp(i) && this.experience >= VillagerData.getUpperLevelExperience(i);
    }

    private void levelUp() {
        this.setVillagerData(this.getVillagerData().withLevel(this.getVillagerData().getLevel() + 1));
        this.fillRecipes();
    }

    @Override
    protected Text getDefaultName() {
        return Text.translatable(this.getType().getTranslationKey() + "." + Registries.VILLAGER_PROFESSION.getId(this.getVillagerData().getProfession()).getPath());
    }

    @Override
    public void handleStatus(byte status) {
        if (status == EntityStatuses.ADD_VILLAGER_HEART_PARTICLES) {
            this.produceParticles(ParticleTypes.HEART);
        } else if (status == EntityStatuses.ADD_VILLAGER_ANGRY_PARTICLES) {
            this.produceParticles(ParticleTypes.ANGRY_VILLAGER);
        } else if (status == EntityStatuses.ADD_VILLAGER_HAPPY_PARTICLES) {
            this.produceParticles(ParticleTypes.HAPPY_VILLAGER);
        } else if (status == EntityStatuses.ADD_SPLASH_PARTICLES) {
            this.produceParticles(ParticleTypes.SPLASH);
        } else {
            super.handleStatus(status);
        }
    }

    @Override
    @Nullable
    public EntityData initialize(ServerWorldAccess world, LocalDifficulty difficulty, SpawnReason spawnReason, @Nullable EntityData entityData) {
        if (spawnReason == SpawnReason.BREEDING) {
            this.setVillagerData(this.getVillagerData().withProfession(VillagerProfession.NONE));
        }
        if (spawnReason == SpawnReason.COMMAND || spawnReason == SpawnReason.SPAWN_EGG || SpawnReason.isAnySpawner(spawnReason) || spawnReason == SpawnReason.DISPENSER) {
            this.setVillagerData(this.getVillagerData().withType(VillagerType.forBiome(world.getBiome(this.getBlockPos()))));
        }
        if (spawnReason == SpawnReason.STRUCTURE) {
            this.natural = true;
        }
        return super.initialize(world, difficulty, spawnReason, entityData);
    }

    @Override
    @Nullable
    public VillagerEntity createChild(ServerWorld arg, PassiveEntity arg2) {
        double d = this.random.nextDouble();
        VillagerType lv = d < 0.5 ? VillagerType.forBiome(arg.getBiome(this.getBlockPos())) : (d < 0.75 ? this.getVillagerData().getType() : ((VillagerEntity)arg2).getVillagerData().getType());
        VillagerEntity lv2 = new VillagerEntity(EntityType.VILLAGER, arg, lv);
        lv2.initialize(arg, arg.getLocalDifficulty(lv2.getBlockPos()), SpawnReason.BREEDING, null);
        return lv2;
    }

    @Override
    public void onStruckByLightning(ServerWorld world, LightningEntity lightning) {
        if (world.getDifficulty() != Difficulty.PEACEFUL) {
            LOGGER.info("Villager {} was struck by lightning {}.", (Object)this, (Object)lightning);
            WitchEntity lv = EntityType.WITCH.create(world);
            if (lv != null) {
                lv.refreshPositionAndAngles(this.getX(), this.getY(), this.getZ(), this.getYaw(), this.getPitch());
                lv.initialize(world, world.getLocalDifficulty(lv.getBlockPos()), SpawnReason.CONVERSION, null);
                lv.setAiDisabled(this.isAiDisabled());
                if (this.hasCustomName()) {
                    lv.setCustomName(this.getCustomName());
                    lv.setCustomNameVisible(this.isCustomNameVisible());
                }
                lv.setPersistent();
                world.spawnEntityAndPassengers(lv);
                this.releaseAllTickets();
                this.discard();
            } else {
                super.onStruckByLightning(world, lightning);
            }
        } else {
            super.onStruckByLightning(world, lightning);
        }
    }

    @Override
    protected void loot(ItemEntity item) {
        InventoryOwner.pickUpItem(this, this, item);
    }

    @Override
    public boolean canGather(ItemStack stack) {
        Item lv = stack.getItem();
        return (GATHERABLE_ITEMS.contains(lv) || this.getVillagerData().getProfession().gatherableItems().contains(lv)) && this.getInventory().canInsert(stack);
    }

    public boolean wantsToStartBreeding() {
        return this.getAvailableFood() >= 24;
    }

    public boolean canBreed() {
        return this.getAvailableFood() < 12;
    }

    private int getAvailableFood() {
        SimpleInventory lv = this.getInventory();
        return ITEM_FOOD_VALUES.entrySet().stream().mapToInt(item -> lv.count((Item)item.getKey()) * (Integer)item.getValue()).sum();
    }

    public boolean hasSeedToPlant() {
        return this.getInventory().containsAny(stack -> stack.isIn(ItemTags.VILLAGER_PLANTABLE_SEEDS));
    }

    @Override
    protected void fillRecipes() {
        Int2ObjectMap<TradeOffers.Factory[]> int2ObjectMap;
        VillagerData lv = this.getVillagerData();
        Int2ObjectMap<TradeOffers.Factory[]> int2ObjectMap2 = this.getWorld().getEnabledFeatures().contains(FeatureFlags.TRADE_REBALANCE) ? ((int2ObjectMap = TradeOffers.REBALANCED_PROFESSION_TO_LEVELED_TRADE.get(lv.getProfession())) != null ? int2ObjectMap : TradeOffers.PROFESSION_TO_LEVELED_TRADE.get(lv.getProfession())) : TradeOffers.PROFESSION_TO_LEVELED_TRADE.get(lv.getProfession());
        if (int2ObjectMap2 == null || int2ObjectMap2.isEmpty()) {
            return;
        }
        TradeOffers.Factory[] lvs = (TradeOffers.Factory[])int2ObjectMap2.get(lv.getLevel());
        if (lvs == null) {
            return;
        }
        TradeOfferList lv2 = this.getOffers();
        this.fillRecipesFromPool(lv2, lvs, 2);
    }

    public void talkWithVillager(ServerWorld world, VillagerEntity villager, long time) {
        if (time >= this.gossipStartTime && time < this.gossipStartTime + 1200L || time >= villager.gossipStartTime && time < villager.gossipStartTime + 1200L) {
            return;
        }
        this.gossip.shareGossipFrom(villager.gossip, this.random, 10);
        this.gossipStartTime = time;
        villager.gossipStartTime = time;
        this.summonGolem(world, time, 5);
    }

    private void decayGossip() {
        long l = this.getWorld().getTime();
        if (this.lastGossipDecayTime == 0L) {
            this.lastGossipDecayTime = l;
            return;
        }
        if (l < this.lastGossipDecayTime + 24000L) {
            return;
        }
        this.gossip.decay();
        this.lastGossipDecayTime = l;
    }

    public void summonGolem(ServerWorld world, long time, int requiredCount) {
        if (!this.canSummonGolem(time)) {
            return;
        }
        Box lv = this.getBoundingBox().expand(10.0, 10.0, 10.0);
        List<VillagerEntity> list = world.getNonSpectatingEntities(VillagerEntity.class, lv);
        List list2 = list.stream().filter(villager -> villager.canSummonGolem(time)).limit(5L).collect(Collectors.toList());
        if (list2.size() < requiredCount) {
            return;
        }
        if (LargeEntitySpawnHelper.trySpawnAt(EntityType.IRON_GOLEM, SpawnReason.MOB_SUMMONED, world, this.getBlockPos(), 10, 8, 6, LargeEntitySpawnHelper.Requirements.IRON_GOLEM).isEmpty()) {
            return;
        }
        list.forEach(GolemLastSeenSensor::rememberIronGolem);
    }

    public boolean canSummonGolem(long time) {
        if (!this.hasRecentlySlept(this.getWorld().getTime())) {
            return false;
        }
        return !this.brain.hasMemoryModule(MemoryModuleType.GOLEM_DETECTED_RECENTLY);
    }

    @Override
    public void onInteractionWith(EntityInteraction interaction, Entity entity) {
        if (interaction == EntityInteraction.ZOMBIE_VILLAGER_CURED) {
            this.gossip.startGossip(entity.getUuid(), VillageGossipType.MAJOR_POSITIVE, 20);
            this.gossip.startGossip(entity.getUuid(), VillageGossipType.MINOR_POSITIVE, 25);
        } else if (interaction == EntityInteraction.TRADE) {
            this.gossip.startGossip(entity.getUuid(), VillageGossipType.TRADING, 2);
        } else if (interaction == EntityInteraction.VILLAGER_HURT) {
            this.gossip.startGossip(entity.getUuid(), VillageGossipType.MINOR_NEGATIVE, 25);
        } else if (interaction == EntityInteraction.VILLAGER_KILLED) {
            this.gossip.startGossip(entity.getUuid(), VillageGossipType.MAJOR_NEGATIVE, 25);
        }
    }

    @Override
    public int getExperience() {
        return this.experience;
    }

    public void setExperience(int experience) {
        this.experience = experience;
    }

    private void clearDailyRestockCount() {
        this.restockAndUpdateDemandBonus();
        this.restocksToday = 0;
    }

    public VillagerGossips getGossip() {
        return this.gossip;
    }

    public void readGossipDataNbt(NbtElement nbt) {
        this.gossip.deserialize(new Dynamic<NbtElement>(NbtOps.INSTANCE, nbt));
    }

    @Override
    protected void sendAiDebugData() {
        super.sendAiDebugData();
        DebugInfoSender.sendBrainDebugData(this);
    }

    @Override
    public void sleep(BlockPos pos) {
        super.sleep(pos);
        this.brain.remember(MemoryModuleType.LAST_SLEPT, this.getWorld().getTime());
        this.brain.forget(MemoryModuleType.WALK_TARGET);
        this.brain.forget(MemoryModuleType.CANT_REACH_WALK_TARGET_SINCE);
    }

    @Override
    public void wakeUp() {
        super.wakeUp();
        this.brain.remember(MemoryModuleType.LAST_WOKEN, this.getWorld().getTime());
    }

    private boolean hasRecentlySlept(long worldTime) {
        Optional<Long> optional = this.brain.getOptionalRegisteredMemory(MemoryModuleType.LAST_SLEPT);
        if (optional.isPresent()) {
            return worldTime - optional.get() < 24000L;
        }
        return false;
    }

    @Override
    @Nullable
    public /* synthetic */ PassiveEntity createChild(ServerWorld world, PassiveEntity entity) {
        return this.createChild(world, entity);
    }
}

