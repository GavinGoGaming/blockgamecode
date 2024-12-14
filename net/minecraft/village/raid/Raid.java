/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 * 
 * Could not load the following classes:
 *  org.jetbrains.annotations.Nullable
 */
package net.minecraft.village.raid;

import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.function.Predicate;
import java.util.stream.Stream;
import net.minecraft.advancement.criterion.Criteria;
import net.minecraft.block.Blocks;
import net.minecraft.block.entity.BannerPattern;
import net.minecraft.block.entity.BannerPatterns;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.BannerPatternsComponent;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.SpawnLocation;
import net.minecraft.entity.SpawnReason;
import net.minecraft.entity.SpawnRestriction;
import net.minecraft.entity.boss.BossBar;
import net.minecraft.entity.boss.ServerBossBar;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.raid.RaiderEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtHelper;
import net.minecraft.nbt.NbtList;
import net.minecraft.network.packet.s2c.play.PlaySoundS2CPacket;
import net.minecraft.registry.RegistryEntryLookup;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.stat.Stats;
import net.minecraft.text.Text;
import net.minecraft.util.DyeColor;
import net.minecraft.util.Formatting;
import net.minecraft.util.Unit;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkSectionPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.Difficulty;
import net.minecraft.world.Heightmap;
import net.minecraft.world.LocalDifficulty;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

public class Raid {
    private static final int field_30676 = 2;
    private static final int field_30677 = 0;
    private static final int field_30678 = 1;
    private static final int field_30679 = 2;
    private static final int field_30680 = 32;
    private static final int field_30681 = 48000;
    private static final int field_30682 = 3;
    private static final Text OMINOUS_BANNER_TRANSLATION_KEY = Text.translatable("block.minecraft.ominous_banner").formatted(Formatting.GOLD);
    private static final String RAIDERS_REMAINING_TRANSLATION_KEY = "event.minecraft.raid.raiders_remaining";
    public static final int field_30669 = 16;
    private static final int field_30685 = 40;
    private static final int DEFAULT_PRE_RAID_TICKS = 300;
    public static final int MAX_DESPAWN_COUNTER = 2400;
    public static final int field_30671 = 600;
    private static final int field_30687 = 30;
    public static final int field_30672 = 24000;
    public static final int field_30673 = 5;
    private static final int field_30688 = 2;
    private static final Text EVENT_TEXT = Text.translatable("event.minecraft.raid");
    private static final Text VICTORY_TITLE = Text.translatable("event.minecraft.raid.victory.full");
    private static final Text DEFEAT_TITLE = Text.translatable("event.minecraft.raid.defeat.full");
    private static final int MAX_ACTIVE_TICKS = 48000;
    public static final int field_30674 = 9216;
    public static final int SQUARED_MAX_RAIDER_DISTANCE = 12544;
    private final Map<Integer, RaiderEntity> waveToCaptain = Maps.newHashMap();
    private final Map<Integer, Set<RaiderEntity>> waveToRaiders = Maps.newHashMap();
    private final Set<UUID> heroesOfTheVillage = Sets.newHashSet();
    private long ticksActive;
    private BlockPos center;
    private final ServerWorld world;
    private boolean started;
    private final int id;
    private float totalHealth;
    private int badOmenLevel;
    private boolean active;
    private int wavesSpawned;
    private final ServerBossBar bar = new ServerBossBar(EVENT_TEXT, BossBar.Color.RED, BossBar.Style.NOTCHED_10);
    private int postRaidTicks;
    private int preRaidTicks;
    private final Random random = Random.create();
    private final int waveCount;
    private Status status;
    private int finishCooldown;
    private Optional<BlockPos> preCalculatedRavagerSpawnLocation = Optional.empty();

    public Raid(int id, ServerWorld world, BlockPos pos) {
        this.id = id;
        this.world = world;
        this.active = true;
        this.preRaidTicks = 300;
        this.bar.setPercent(0.0f);
        this.center = pos;
        this.waveCount = this.getMaxWaves(world.getDifficulty());
        this.status = Status.ONGOING;
    }

    public Raid(ServerWorld world, NbtCompound nbt) {
        this.world = world;
        this.id = nbt.getInt("Id");
        this.started = nbt.getBoolean("Started");
        this.active = nbt.getBoolean("Active");
        this.ticksActive = nbt.getLong("TicksActive");
        this.badOmenLevel = nbt.getInt("BadOmenLevel");
        this.wavesSpawned = nbt.getInt("GroupsSpawned");
        this.preRaidTicks = nbt.getInt("PreRaidTicks");
        this.postRaidTicks = nbt.getInt("PostRaidTicks");
        this.totalHealth = nbt.getFloat("TotalHealth");
        this.center = new BlockPos(nbt.getInt("CX"), nbt.getInt("CY"), nbt.getInt("CZ"));
        this.waveCount = nbt.getInt("NumGroups");
        this.status = Status.fromName(nbt.getString("Status"));
        this.heroesOfTheVillage.clear();
        if (nbt.contains("HeroesOfTheVillage", NbtElement.LIST_TYPE)) {
            NbtList lv = nbt.getList("HeroesOfTheVillage", NbtElement.INT_ARRAY_TYPE);
            for (NbtElement lv2 : lv) {
                this.heroesOfTheVillage.add(NbtHelper.toUuid(lv2));
            }
        }
    }

    public boolean isFinished() {
        return this.hasWon() || this.hasLost();
    }

    public boolean isPreRaid() {
        return this.hasSpawned() && this.getRaiderCount() == 0 && this.preRaidTicks > 0;
    }

    public boolean hasSpawned() {
        return this.wavesSpawned > 0;
    }

    public boolean hasStopped() {
        return this.status == Status.STOPPED;
    }

    public boolean hasWon() {
        return this.status == Status.VICTORY;
    }

    public boolean hasLost() {
        return this.status == Status.LOSS;
    }

    public float getTotalHealth() {
        return this.totalHealth;
    }

    public Set<RaiderEntity> getAllRaiders() {
        HashSet<RaiderEntity> set = Sets.newHashSet();
        for (Set<RaiderEntity> set2 : this.waveToRaiders.values()) {
            set.addAll(set2);
        }
        return set;
    }

    public World getWorld() {
        return this.world;
    }

    public boolean hasStarted() {
        return this.started;
    }

    public int getGroupsSpawned() {
        return this.wavesSpawned;
    }

    private Predicate<ServerPlayerEntity> isInRaidDistance() {
        return player -> {
            BlockPos lv = player.getBlockPos();
            return player.isAlive() && this.world.getRaidAt(lv) == this;
        };
    }

    private void updateBarToPlayers() {
        HashSet<ServerPlayerEntity> set = Sets.newHashSet(this.bar.getPlayers());
        List<ServerPlayerEntity> list = this.world.getPlayers(this.isInRaidDistance());
        for (ServerPlayerEntity lv : list) {
            if (set.contains(lv)) continue;
            this.bar.addPlayer(lv);
        }
        for (ServerPlayerEntity lv : set) {
            if (list.contains(lv)) continue;
            this.bar.removePlayer(lv);
        }
    }

    public int getMaxAcceptableBadOmenLevel() {
        return 5;
    }

    public int getBadOmenLevel() {
        return this.badOmenLevel;
    }

    public void setBadOmenLevel(int badOmenLevel) {
        this.badOmenLevel = badOmenLevel;
    }

    public boolean start(ServerPlayerEntity arg) {
        StatusEffectInstance lv = arg.getStatusEffect(StatusEffects.RAID_OMEN);
        if (lv == null) {
            return false;
        }
        this.badOmenLevel += lv.getAmplifier() + 1;
        this.badOmenLevel = MathHelper.clamp(this.badOmenLevel, 0, this.getMaxAcceptableBadOmenLevel());
        if (!this.hasSpawned()) {
            arg.incrementStat(Stats.RAID_TRIGGER);
            Criteria.VOLUNTARY_EXILE.trigger(arg);
        }
        return true;
    }

    public void invalidate() {
        this.active = false;
        this.bar.clearPlayers();
        this.status = Status.STOPPED;
    }

    public void tick() {
        if (this.hasStopped()) {
            return;
        }
        if (this.status == Status.ONGOING) {
            boolean bl2;
            boolean bl = this.active;
            this.active = this.world.isChunkLoaded(this.center);
            if (this.world.getDifficulty() == Difficulty.PEACEFUL) {
                this.invalidate();
                return;
            }
            if (bl != this.active) {
                this.bar.setVisible(this.active);
            }
            if (!this.active) {
                return;
            }
            if (!this.world.isNearOccupiedPointOfInterest(this.center)) {
                this.moveRaidCenter();
            }
            if (!this.world.isNearOccupiedPointOfInterest(this.center)) {
                if (this.wavesSpawned > 0) {
                    this.status = Status.LOSS;
                } else {
                    this.invalidate();
                }
            }
            ++this.ticksActive;
            if (this.ticksActive >= 48000L) {
                this.invalidate();
                return;
            }
            int i = this.getRaiderCount();
            if (i == 0 && this.shouldSpawnMoreGroups()) {
                if (this.preRaidTicks > 0) {
                    boolean bl3;
                    bl2 = this.preCalculatedRavagerSpawnLocation.isPresent();
                    boolean bl4 = bl3 = !bl2 && this.preRaidTicks % 5 == 0;
                    if (bl2 && !this.world.shouldTickEntity(this.preCalculatedRavagerSpawnLocation.get())) {
                        bl3 = true;
                    }
                    if (bl3) {
                        int j = 0;
                        if (this.preRaidTicks < 100) {
                            j = 1;
                        } else if (this.preRaidTicks < 40) {
                            j = 2;
                        }
                        this.preCalculatedRavagerSpawnLocation = this.preCalculateRavagerSpawnLocation(j);
                    }
                    if (this.preRaidTicks == 300 || this.preRaidTicks % 20 == 0) {
                        this.updateBarToPlayers();
                    }
                    --this.preRaidTicks;
                    this.bar.setPercent(MathHelper.clamp((float)(300 - this.preRaidTicks) / 300.0f, 0.0f, 1.0f));
                } else if (this.preRaidTicks == 0 && this.wavesSpawned > 0) {
                    this.preRaidTicks = 300;
                    this.bar.setName(EVENT_TEXT);
                    return;
                }
            }
            if (this.ticksActive % 20L == 0L) {
                this.updateBarToPlayers();
                this.removeObsoleteRaiders();
                if (i > 0) {
                    if (i <= 2) {
                        this.bar.setName(EVENT_TEXT.copy().append(" - ").append(Text.translatable(RAIDERS_REMAINING_TRANSLATION_KEY, i)));
                    } else {
                        this.bar.setName(EVENT_TEXT);
                    }
                } else {
                    this.bar.setName(EVENT_TEXT);
                }
            }
            bl2 = false;
            int k = 0;
            while (this.canSpawnRaiders()) {
                BlockPos lv;
                BlockPos blockPos = lv = this.preCalculatedRavagerSpawnLocation.isPresent() ? this.preCalculatedRavagerSpawnLocation.get() : this.getRavagerSpawnLocation(k, 20);
                if (lv != null) {
                    this.started = true;
                    this.spawnNextWave(lv);
                    if (!bl2) {
                        this.playRaidHorn(lv);
                        bl2 = true;
                    }
                } else {
                    ++k;
                }
                if (k <= 3) continue;
                this.invalidate();
                break;
            }
            if (this.hasStarted() && !this.shouldSpawnMoreGroups() && i == 0) {
                if (this.postRaidTicks < 40) {
                    ++this.postRaidTicks;
                } else {
                    this.status = Status.VICTORY;
                    for (UUID uUID : this.heroesOfTheVillage) {
                        Entity lv2 = this.world.getEntity(uUID);
                        if (!(lv2 instanceof LivingEntity)) continue;
                        LivingEntity lv3 = (LivingEntity)lv2;
                        if (lv2.isSpectator()) continue;
                        lv3.addStatusEffect(new StatusEffectInstance(StatusEffects.HERO_OF_THE_VILLAGE, 48000, this.badOmenLevel - 1, false, false, true));
                        if (!(lv3 instanceof ServerPlayerEntity)) continue;
                        ServerPlayerEntity lv4 = (ServerPlayerEntity)lv3;
                        lv4.incrementStat(Stats.RAID_WIN);
                        Criteria.HERO_OF_THE_VILLAGE.trigger(lv4);
                    }
                }
            }
            this.markDirty();
        } else if (this.isFinished()) {
            ++this.finishCooldown;
            if (this.finishCooldown >= 600) {
                this.invalidate();
                return;
            }
            if (this.finishCooldown % 20 == 0) {
                this.updateBarToPlayers();
                this.bar.setVisible(true);
                if (this.hasWon()) {
                    this.bar.setPercent(0.0f);
                    this.bar.setName(VICTORY_TITLE);
                } else {
                    this.bar.setName(DEFEAT_TITLE);
                }
            }
        }
    }

    private void moveRaidCenter() {
        Stream<ChunkSectionPos> stream = ChunkSectionPos.stream(ChunkSectionPos.from(this.center), 2);
        stream.filter(this.world::isNearOccupiedPointOfInterest).map(ChunkSectionPos::getCenterPos).min(Comparator.comparingDouble(pos -> pos.getSquaredDistance(this.center))).ifPresent(this::setCenter);
    }

    private Optional<BlockPos> preCalculateRavagerSpawnLocation(int proximity) {
        for (int j = 0; j < 3; ++j) {
            BlockPos lv = this.getRavagerSpawnLocation(proximity, 1);
            if (lv == null) continue;
            return Optional.of(lv);
        }
        return Optional.empty();
    }

    private boolean shouldSpawnMoreGroups() {
        if (this.hasExtraWave()) {
            return !this.hasSpawnedExtraWave();
        }
        return !this.hasSpawnedFinalWave();
    }

    private boolean hasSpawnedFinalWave() {
        return this.getGroupsSpawned() == this.waveCount;
    }

    private boolean hasExtraWave() {
        return this.badOmenLevel > 1;
    }

    private boolean hasSpawnedExtraWave() {
        return this.getGroupsSpawned() > this.waveCount;
    }

    private boolean isSpawningExtraWave() {
        return this.hasSpawnedFinalWave() && this.getRaiderCount() == 0 && this.hasExtraWave();
    }

    private void removeObsoleteRaiders() {
        Iterator<Set<RaiderEntity>> iterator = this.waveToRaiders.values().iterator();
        HashSet<RaiderEntity> set = Sets.newHashSet();
        while (iterator.hasNext()) {
            Set<RaiderEntity> set2 = iterator.next();
            for (RaiderEntity lv : set2) {
                BlockPos lv2 = lv.getBlockPos();
                if (lv.isRemoved() || lv.getWorld().getRegistryKey() != this.world.getRegistryKey() || this.center.getSquaredDistance(lv2) >= 12544.0) {
                    set.add(lv);
                    continue;
                }
                if (lv.age <= 600) continue;
                if (this.world.getEntity(lv.getUuid()) == null) {
                    set.add(lv);
                }
                if (!this.world.isNearOccupiedPointOfInterest(lv2) && lv.getDespawnCounter() > 2400) {
                    lv.setOutOfRaidCounter(lv.getOutOfRaidCounter() + 1);
                }
                if (lv.getOutOfRaidCounter() < 30) continue;
                set.add(lv);
            }
        }
        for (RaiderEntity lv3 : set) {
            this.removeFromWave(lv3, true);
        }
    }

    private void playRaidHorn(BlockPos pos) {
        float f = 13.0f;
        int i = 64;
        Collection<ServerPlayerEntity> collection = this.bar.getPlayers();
        long l = this.random.nextLong();
        for (ServerPlayerEntity lv : this.world.getPlayers()) {
            Vec3d lv2 = lv.getPos();
            Vec3d lv3 = Vec3d.ofCenter(pos);
            double d = Math.sqrt((lv3.x - lv2.x) * (lv3.x - lv2.x) + (lv3.z - lv2.z) * (lv3.z - lv2.z));
            double e = lv2.x + 13.0 / d * (lv3.x - lv2.x);
            double g = lv2.z + 13.0 / d * (lv3.z - lv2.z);
            if (!(d <= 64.0) && !collection.contains(lv)) continue;
            lv.networkHandler.sendPacket(new PlaySoundS2CPacket(SoundEvents.EVENT_RAID_HORN, SoundCategory.NEUTRAL, e, lv.getY(), g, 64.0f, 1.0f, l));
        }
    }

    private void spawnNextWave(BlockPos pos) {
        boolean bl = false;
        int i = this.wavesSpawned + 1;
        this.totalHealth = 0.0f;
        LocalDifficulty lv = this.world.getLocalDifficulty(pos);
        boolean bl2 = this.isSpawningExtraWave();
        for (Member lv2 : Member.VALUES) {
            RaiderEntity lv3;
            int j = this.getCount(lv2, i, bl2) + this.getBonusCount(lv2, this.random, i, lv, bl2);
            int k = 0;
            for (int l = 0; l < j && (lv3 = lv2.type.create(this.world)) != null; ++l) {
                if (!bl && lv3.canLead()) {
                    lv3.setPatrolLeader(true);
                    this.setWaveCaptain(i, lv3);
                    bl = true;
                }
                this.addRaider(i, lv3, pos, false);
                if (lv2.type != EntityType.RAVAGER) continue;
                RaiderEntity lv4 = null;
                if (i == this.getMaxWaves(Difficulty.NORMAL)) {
                    lv4 = EntityType.PILLAGER.create(this.world);
                } else if (i >= this.getMaxWaves(Difficulty.HARD)) {
                    lv4 = k == 0 ? (RaiderEntity)EntityType.EVOKER.create(this.world) : (RaiderEntity)EntityType.VINDICATOR.create(this.world);
                }
                ++k;
                if (lv4 == null) continue;
                this.addRaider(i, lv4, pos, false);
                lv4.refreshPositionAndAngles(pos, 0.0f, 0.0f);
                lv4.startRiding(lv3);
            }
        }
        this.preCalculatedRavagerSpawnLocation = Optional.empty();
        ++this.wavesSpawned;
        this.updateBar();
        this.markDirty();
    }

    public void addRaider(int wave, RaiderEntity raider, @Nullable BlockPos pos, boolean existing) {
        boolean bl2 = this.addToWave(wave, raider);
        if (bl2) {
            raider.setRaid(this);
            raider.setWave(wave);
            raider.setAbleToJoinRaid(true);
            raider.setOutOfRaidCounter(0);
            if (!existing && pos != null) {
                raider.setPosition((double)pos.getX() + 0.5, (double)pos.getY() + 1.0, (double)pos.getZ() + 0.5);
                raider.initialize(this.world, this.world.getLocalDifficulty(pos), SpawnReason.EVENT, null);
                raider.addBonusForWave(this.world, wave, false);
                raider.setOnGround(true);
                this.world.spawnEntityAndPassengers(raider);
            }
        }
    }

    public void updateBar() {
        this.bar.setPercent(MathHelper.clamp(this.getCurrentRaiderHealth() / this.totalHealth, 0.0f, 1.0f));
    }

    public float getCurrentRaiderHealth() {
        float f = 0.0f;
        for (Set<RaiderEntity> set : this.waveToRaiders.values()) {
            for (RaiderEntity lv : set) {
                f += lv.getHealth();
            }
        }
        return f;
    }

    private boolean canSpawnRaiders() {
        return this.preRaidTicks == 0 && (this.wavesSpawned < this.waveCount || this.isSpawningExtraWave()) && this.getRaiderCount() == 0;
    }

    public int getRaiderCount() {
        return this.waveToRaiders.values().stream().mapToInt(Set::size).sum();
    }

    public void removeFromWave(RaiderEntity entity, boolean countHealth) {
        boolean bl2;
        Set<RaiderEntity> set = this.waveToRaiders.get(entity.getWave());
        if (set != null && (bl2 = set.remove(entity))) {
            if (countHealth) {
                this.totalHealth -= entity.getHealth();
            }
            entity.setRaid(null);
            this.updateBar();
            this.markDirty();
        }
    }

    private void markDirty() {
        this.world.getRaidManager().markDirty();
    }

    public static ItemStack getOminousBanner(RegistryEntryLookup<BannerPattern> bannerPatternLookup) {
        ItemStack lv = new ItemStack(Items.WHITE_BANNER);
        BannerPatternsComponent lv2 = new BannerPatternsComponent.Builder().add(bannerPatternLookup, BannerPatterns.RHOMBUS, DyeColor.CYAN).add(bannerPatternLookup, BannerPatterns.STRIPE_BOTTOM, DyeColor.LIGHT_GRAY).add(bannerPatternLookup, BannerPatterns.STRIPE_CENTER, DyeColor.GRAY).add(bannerPatternLookup, BannerPatterns.BORDER, DyeColor.LIGHT_GRAY).add(bannerPatternLookup, BannerPatterns.STRIPE_MIDDLE, DyeColor.BLACK).add(bannerPatternLookup, BannerPatterns.HALF_HORIZONTAL, DyeColor.LIGHT_GRAY).add(bannerPatternLookup, BannerPatterns.CIRCLE, DyeColor.LIGHT_GRAY).add(bannerPatternLookup, BannerPatterns.BORDER, DyeColor.BLACK).build();
        lv.set(DataComponentTypes.BANNER_PATTERNS, lv2);
        lv.set(DataComponentTypes.HIDE_ADDITIONAL_TOOLTIP, Unit.INSTANCE);
        lv.set(DataComponentTypes.ITEM_NAME, OMINOUS_BANNER_TRANSLATION_KEY);
        return lv;
    }

    @Nullable
    public RaiderEntity getCaptain(int wave) {
        return this.waveToCaptain.get(wave);
    }

    @Nullable
    private BlockPos getRavagerSpawnLocation(int proximity, int tries) {
        int k = proximity == 0 ? 2 : 2 - proximity;
        BlockPos.Mutable lv = new BlockPos.Mutable();
        SpawnLocation lv2 = SpawnRestriction.getLocation(EntityType.RAVAGER);
        for (int l = 0; l < tries; ++l) {
            float f = this.world.random.nextFloat() * ((float)Math.PI * 2);
            int m = this.center.getX() + MathHelper.floor(MathHelper.cos(f) * 32.0f * (float)k) + this.world.random.nextInt(5);
            int n = this.center.getZ() + MathHelper.floor(MathHelper.sin(f) * 32.0f * (float)k) + this.world.random.nextInt(5);
            int o = this.world.getTopY(Heightmap.Type.WORLD_SURFACE, m, n);
            lv.set(m, o, n);
            if (this.world.isNearOccupiedPointOfInterest(lv) && proximity < 2) continue;
            int p = 10;
            if (!this.world.isRegionLoaded(lv.getX() - 10, lv.getZ() - 10, lv.getX() + 10, lv.getZ() + 10) || !this.world.shouldTickEntity(lv) || !lv2.isSpawnPositionOk(this.world, lv, EntityType.RAVAGER) && (!this.world.getBlockState((BlockPos)lv.down()).isOf(Blocks.SNOW) || !this.world.getBlockState(lv).isAir())) continue;
            return lv;
        }
        return null;
    }

    private boolean addToWave(int wave, RaiderEntity entity) {
        return this.addToWave(wave, entity, true);
    }

    public boolean addToWave(int wave, RaiderEntity entity, boolean countHealth) {
        this.waveToRaiders.computeIfAbsent(wave, wavex -> Sets.newHashSet());
        Set<RaiderEntity> set = this.waveToRaiders.get(wave);
        RaiderEntity lv = null;
        for (RaiderEntity lv2 : set) {
            if (!lv2.getUuid().equals(entity.getUuid())) continue;
            lv = lv2;
            break;
        }
        if (lv != null) {
            set.remove(lv);
            set.add(entity);
        }
        set.add(entity);
        if (countHealth) {
            this.totalHealth += entity.getHealth();
        }
        this.updateBar();
        this.markDirty();
        return true;
    }

    public void setWaveCaptain(int wave, RaiderEntity entity) {
        this.waveToCaptain.put(wave, entity);
        entity.equipStack(EquipmentSlot.HEAD, Raid.getOminousBanner(entity.getRegistryManager().getWrapperOrThrow(RegistryKeys.BANNER_PATTERN)));
        entity.setEquipmentDropChance(EquipmentSlot.HEAD, 2.0f);
    }

    public void removeLeader(int wave) {
        this.waveToCaptain.remove(wave);
    }

    public BlockPos getCenter() {
        return this.center;
    }

    private void setCenter(BlockPos center) {
        this.center = center;
    }

    public int getRaidId() {
        return this.id;
    }

    private int getCount(Member member, int wave, boolean extra) {
        return extra ? member.countInWave[this.waveCount] : member.countInWave[wave];
    }

    private int getBonusCount(Member member, Random random, int wave, LocalDifficulty localDifficulty, boolean extra) {
        int j;
        Difficulty lv = localDifficulty.getGlobalDifficulty();
        boolean bl2 = lv == Difficulty.EASY;
        boolean bl3 = lv == Difficulty.NORMAL;
        switch (member.ordinal()) {
            case 3: {
                if (!bl2 && wave > 2 && wave != 4) {
                    j = 1;
                    break;
                }
                return 0;
            }
            case 0: 
            case 2: {
                if (bl2) {
                    j = random.nextInt(2);
                    break;
                }
                if (bl3) {
                    j = 1;
                    break;
                }
                j = 2;
                break;
            }
            case 4: {
                j = !bl2 && extra ? 1 : 0;
                break;
            }
            default: {
                return 0;
            }
        }
        return j > 0 ? random.nextInt(j + 1) : 0;
    }

    public boolean isActive() {
        return this.active;
    }

    public NbtCompound writeNbt(NbtCompound nbt) {
        nbt.putInt("Id", this.id);
        nbt.putBoolean("Started", this.started);
        nbt.putBoolean("Active", this.active);
        nbt.putLong("TicksActive", this.ticksActive);
        nbt.putInt("BadOmenLevel", this.badOmenLevel);
        nbt.putInt("GroupsSpawned", this.wavesSpawned);
        nbt.putInt("PreRaidTicks", this.preRaidTicks);
        nbt.putInt("PostRaidTicks", this.postRaidTicks);
        nbt.putFloat("TotalHealth", this.totalHealth);
        nbt.putInt("NumGroups", this.waveCount);
        nbt.putString("Status", this.status.getName());
        nbt.putInt("CX", this.center.getX());
        nbt.putInt("CY", this.center.getY());
        nbt.putInt("CZ", this.center.getZ());
        NbtList lv = new NbtList();
        for (UUID uUID : this.heroesOfTheVillage) {
            lv.add(NbtHelper.fromUuid(uUID));
        }
        nbt.put("HeroesOfTheVillage", lv);
        return nbt;
    }

    public int getMaxWaves(Difficulty difficulty) {
        switch (difficulty) {
            case EASY: {
                return 3;
            }
            case NORMAL: {
                return 5;
            }
            case HARD: {
                return 7;
            }
        }
        return 0;
    }

    public float getEnchantmentChance() {
        int i = this.getBadOmenLevel();
        if (i == 2) {
            return 0.1f;
        }
        if (i == 3) {
            return 0.25f;
        }
        if (i == 4) {
            return 0.5f;
        }
        if (i == 5) {
            return 0.75f;
        }
        return 0.0f;
    }

    public void addHero(Entity entity) {
        this.heroesOfTheVillage.add(entity.getUuid());
    }

    static enum Status {
        ONGOING,
        VICTORY,
        LOSS,
        STOPPED;

        private static final Status[] VALUES;

        static Status fromName(String name) {
            for (Status lv : VALUES) {
                if (!name.equalsIgnoreCase(lv.name())) continue;
                return lv;
            }
            return ONGOING;
        }

        public String getName() {
            return this.name().toLowerCase(Locale.ROOT);
        }

        static {
            VALUES = Status.values();
        }
    }

    static enum Member {
        VINDICATOR(EntityType.VINDICATOR, new int[]{0, 0, 2, 0, 1, 4, 2, 5}),
        EVOKER(EntityType.EVOKER, new int[]{0, 0, 0, 0, 0, 1, 1, 2}),
        PILLAGER(EntityType.PILLAGER, new int[]{0, 4, 3, 3, 4, 4, 4, 2}),
        WITCH(EntityType.WITCH, new int[]{0, 0, 0, 0, 3, 0, 0, 1}),
        RAVAGER(EntityType.RAVAGER, new int[]{0, 0, 0, 1, 0, 1, 0, 2});

        static final Member[] VALUES;
        final EntityType<? extends RaiderEntity> type;
        final int[] countInWave;

        private Member(EntityType<? extends RaiderEntity> type, int[] countInWave) {
            this.type = type;
            this.countInWave = countInWave;
        }

        static {
            VALUES = Member.values();
        }
    }
}

