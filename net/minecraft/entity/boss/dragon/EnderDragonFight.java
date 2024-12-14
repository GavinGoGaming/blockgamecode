/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 * 
 * Could not load the following classes:
 *  org.jetbrains.annotations.Nullable
 */
package net.minecraft.entity.boss.dragon;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.ContiguousSet;
import com.google.common.collect.DiscreteDomain;
import com.google.common.collect.Lists;
import com.google.common.collect.Range;
import com.google.common.collect.Sets;
import com.mojang.datafixers.kinds.Applicative;
import com.mojang.logging.LogUtils;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Predicate;
import net.minecraft.advancement.criterion.Criteria;
import net.minecraft.block.Blocks;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.EndPortalBlockEntity;
import net.minecraft.block.pattern.BlockPattern;
import net.minecraft.block.pattern.BlockPatternBuilder;
import net.minecraft.block.pattern.CachedBlockPosition;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.boss.BossBar;
import net.minecraft.entity.boss.ServerBossBar;
import net.minecraft.entity.boss.dragon.EnderDragonEntity;
import net.minecraft.entity.boss.dragon.EnderDragonSpawnState;
import net.minecraft.entity.boss.dragon.phase.PhaseType;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.decoration.EndCrystalEntity;
import net.minecraft.predicate.block.BlockPredicate;
import net.minecraft.predicate.entity.EntityPredicates;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ChunkLevelType;
import net.minecraft.server.world.ChunkTicketType;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.util.Unit;
import net.minecraft.util.Util;
import net.minecraft.util.Uuids;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.Heightmap;
import net.minecraft.world.WorldEvents;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.ChunkStatus;
import net.minecraft.world.chunk.WorldChunk;
import net.minecraft.world.gen.feature.ConfiguredFeature;
import net.minecraft.world.gen.feature.EndConfiguredFeatures;
import net.minecraft.world.gen.feature.EndPortalFeature;
import net.minecraft.world.gen.feature.EndSpikeFeature;
import net.minecraft.world.gen.feature.FeatureConfig;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;

public class EnderDragonFight {
    private static final Logger LOGGER = LogUtils.getLogger();
    private static final int CHECK_DRAGON_SEEN_INTERVAL = 1200;
    private static final int CRYSTAL_COUNTING_INTERVAL = 100;
    public static final int field_31445 = 20;
    private static final int ISLAND_SIZE = 8;
    public static final int field_31441 = 9;
    private static final int PLAYER_COUNTING_INTERVAL = 20;
    private static final int field_31448 = 96;
    public static final int SPAWN_Y = 128;
    private final Predicate<Entity> showBossBarPredicate;
    private final ServerBossBar bossBar = (ServerBossBar)new ServerBossBar(Text.translatable("entity.minecraft.ender_dragon"), BossBar.Color.PINK, BossBar.Style.PROGRESS).setDragonMusic(true).setThickenFog(true);
    private final ServerWorld world;
    private final BlockPos origin;
    private final ObjectArrayList<Integer> gateways = new ObjectArrayList();
    private final BlockPattern endPortalPattern;
    private int dragonSeenTimer;
    private int endCrystalsAlive;
    private int crystalCountTimer;
    private int playerUpdateTimer = 21;
    private boolean dragonKilled;
    private boolean previouslyKilled;
    private boolean skipChunksLoadedCheck = false;
    @Nullable
    private UUID dragonUuid;
    private boolean doLegacyCheck = true;
    @Nullable
    private BlockPos exitPortalLocation;
    @Nullable
    private EnderDragonSpawnState dragonSpawnState;
    private int spawnStateTimer;
    @Nullable
    private List<EndCrystalEntity> crystals;

    public EnderDragonFight(ServerWorld world, long gatewaysSeed, Data data) {
        this(world, gatewaysSeed, data, BlockPos.ORIGIN);
    }

    public EnderDragonFight(ServerWorld world, long gatewaysSeed, Data data, BlockPos origin) {
        this.world = world;
        this.origin = origin;
        this.showBossBarPredicate = EntityPredicates.VALID_ENTITY.and(EntityPredicates.maxDistance(origin.getX(), 128 + origin.getY(), origin.getZ(), 192.0));
        this.doLegacyCheck = data.needsStateScanning;
        this.dragonUuid = data.dragonUUID.orElse(null);
        this.dragonKilled = data.dragonKilled;
        this.previouslyKilled = data.previouslyKilled;
        if (data.isRespawning) {
            this.dragonSpawnState = EnderDragonSpawnState.START;
        }
        this.exitPortalLocation = data.exitPortalLocation.orElse(null);
        this.gateways.addAll(data.gateways.orElseGet(() -> {
            ObjectArrayList<Integer> objectArrayList = new ObjectArrayList<Integer>(ContiguousSet.create(Range.closedOpen(0, 20), DiscreteDomain.integers()));
            Util.shuffle(objectArrayList, Random.create(gatewaysSeed));
            return objectArrayList;
        }));
        this.endPortalPattern = BlockPatternBuilder.start().aisle("       ", "       ", "       ", "   #   ", "       ", "       ", "       ").aisle("       ", "       ", "       ", "   #   ", "       ", "       ", "       ").aisle("       ", "       ", "       ", "   #   ", "       ", "       ", "       ").aisle("  ###  ", " #   # ", "#     #", "#  #  #", "#     #", " #   # ", "  ###  ").aisle("       ", "  ###  ", " ##### ", " ##### ", " ##### ", "  ###  ", "       ").where('#', CachedBlockPosition.matchesBlockState(BlockPredicate.make(Blocks.BEDROCK))).build();
    }

    @Deprecated
    @VisibleForTesting
    public void setSkipChunksLoadedCheck() {
        this.skipChunksLoadedCheck = true;
    }

    public Data toData() {
        return new Data(this.doLegacyCheck, this.dragonKilled, this.previouslyKilled, false, Optional.ofNullable(this.dragonUuid), Optional.ofNullable(this.exitPortalLocation), Optional.of(this.gateways));
    }

    public void tick() {
        this.bossBar.setVisible(!this.dragonKilled);
        if (++this.playerUpdateTimer >= 20) {
            this.updatePlayers();
            this.playerUpdateTimer = 0;
        }
        if (!this.bossBar.getPlayers().isEmpty()) {
            this.world.getChunkManager().addTicket(ChunkTicketType.DRAGON, new ChunkPos(0, 0), 9, Unit.INSTANCE);
            boolean bl = this.areChunksLoaded();
            if (this.doLegacyCheck && bl) {
                this.convertFromLegacy();
                this.doLegacyCheck = false;
            }
            if (this.dragonSpawnState != null) {
                if (this.crystals == null && bl) {
                    this.dragonSpawnState = null;
                    this.respawnDragon();
                }
                this.dragonSpawnState.run(this.world, this, this.crystals, this.spawnStateTimer++, this.exitPortalLocation);
            }
            if (!this.dragonKilled) {
                if ((this.dragonUuid == null || ++this.dragonSeenTimer >= 1200) && bl) {
                    this.checkDragonSeen();
                    this.dragonSeenTimer = 0;
                }
                if (++this.crystalCountTimer >= 100 && bl) {
                    this.countAliveCrystals();
                    this.crystalCountTimer = 0;
                }
            }
        } else {
            this.world.getChunkManager().removeTicket(ChunkTicketType.DRAGON, new ChunkPos(0, 0), 9, Unit.INSTANCE);
        }
    }

    private void convertFromLegacy() {
        LOGGER.info("Scanning for legacy world dragon fight...");
        boolean bl = this.worldContainsEndPortal();
        if (bl) {
            LOGGER.info("Found that the dragon has been killed in this world already.");
            this.previouslyKilled = true;
        } else {
            LOGGER.info("Found that the dragon has not yet been killed in this world.");
            this.previouslyKilled = false;
            if (this.findEndPortal() == null) {
                this.generateEndPortal(false);
            }
        }
        List<? extends EnderDragonEntity> list = this.world.getAliveEnderDragons();
        if (list.isEmpty()) {
            this.dragonKilled = true;
        } else {
            EnderDragonEntity lv = list.get(0);
            this.dragonUuid = lv.getUuid();
            LOGGER.info("Found that there's a dragon still alive ({})", (Object)lv);
            this.dragonKilled = false;
            if (!bl) {
                LOGGER.info("But we didn't have a portal, let's remove it.");
                lv.discard();
                this.dragonUuid = null;
            }
        }
        if (!this.previouslyKilled && this.dragonKilled) {
            this.dragonKilled = false;
        }
    }

    private void checkDragonSeen() {
        List<? extends EnderDragonEntity> list = this.world.getAliveEnderDragons();
        if (list.isEmpty()) {
            LOGGER.debug("Haven't seen the dragon, respawning it");
            this.createDragon();
        } else {
            LOGGER.debug("Haven't seen our dragon, but found another one to use.");
            this.dragonUuid = list.get(0).getUuid();
        }
    }

    protected void setSpawnState(EnderDragonSpawnState spawnState) {
        if (this.dragonSpawnState == null) {
            throw new IllegalStateException("Dragon respawn isn't in progress, can't skip ahead in the animation.");
        }
        this.spawnStateTimer = 0;
        if (spawnState == EnderDragonSpawnState.END) {
            this.dragonSpawnState = null;
            this.dragonKilled = false;
            EnderDragonEntity lv = this.createDragon();
            if (lv != null) {
                for (ServerPlayerEntity lv2 : this.bossBar.getPlayers()) {
                    Criteria.SUMMONED_ENTITY.trigger(lv2, lv);
                }
            }
        } else {
            this.dragonSpawnState = spawnState;
        }
    }

    private boolean worldContainsEndPortal() {
        for (int i = -8; i <= 8; ++i) {
            for (int j = -8; j <= 8; ++j) {
                WorldChunk lv = this.world.getChunk(i, j);
                for (BlockEntity lv2 : lv.getBlockEntities().values()) {
                    if (!(lv2 instanceof EndPortalBlockEntity)) continue;
                    return true;
                }
            }
        }
        return false;
    }

    @Nullable
    private BlockPattern.Result findEndPortal() {
        int j;
        ChunkPos lv = new ChunkPos(this.origin);
        for (int i = -8 + lv.x; i <= 8 + lv.x; ++i) {
            for (j = -8 + lv.z; j <= 8 + lv.z; ++j) {
                WorldChunk lv2 = this.world.getChunk(i, j);
                for (BlockEntity lv3 : lv2.getBlockEntities().values()) {
                    BlockPattern.Result lv4;
                    if (!(lv3 instanceof EndPortalBlockEntity) || (lv4 = this.endPortalPattern.searchAround(this.world, lv3.getPos())) == null) continue;
                    BlockPos lv5 = lv4.translate(3, 3, 3).getBlockPos();
                    if (this.exitPortalLocation == null) {
                        this.exitPortalLocation = lv5;
                    }
                    return lv4;
                }
            }
        }
        BlockPos lv6 = EndPortalFeature.offsetOrigin(this.origin);
        for (int k = j = this.world.getTopPosition(Heightmap.Type.MOTION_BLOCKING, lv6).getY(); k >= this.world.getBottomY(); --k) {
            BlockPattern.Result lv7 = this.endPortalPattern.searchAround(this.world, new BlockPos(lv6.getX(), k, lv6.getZ()));
            if (lv7 == null) continue;
            if (this.exitPortalLocation == null) {
                this.exitPortalLocation = lv7.translate(3, 3, 3).getBlockPos();
            }
            return lv7;
        }
        return null;
    }

    private boolean areChunksLoaded() {
        if (this.skipChunksLoadedCheck) {
            return true;
        }
        ChunkPos lv = new ChunkPos(this.origin);
        for (int i = -8 + lv.x; i <= 8 + lv.x; ++i) {
            for (int j = 8 + lv.z; j <= 8 + lv.z; ++j) {
                Chunk lv2 = this.world.getChunk(i, j, ChunkStatus.FULL, false);
                if (!(lv2 instanceof WorldChunk)) {
                    return false;
                }
                ChunkLevelType lv3 = ((WorldChunk)lv2).getLevelType();
                if (lv3.isAfter(ChunkLevelType.BLOCK_TICKING)) continue;
                return false;
            }
        }
        return true;
    }

    private void updatePlayers() {
        HashSet<ServerPlayerEntity> set = Sets.newHashSet();
        for (ServerPlayerEntity lv : this.world.getPlayers(this.showBossBarPredicate)) {
            this.bossBar.addPlayer(lv);
            set.add(lv);
        }
        HashSet<ServerPlayerEntity> set2 = Sets.newHashSet(this.bossBar.getPlayers());
        set2.removeAll(set);
        for (ServerPlayerEntity lv2 : set2) {
            this.bossBar.removePlayer(lv2);
        }
    }

    private void countAliveCrystals() {
        this.crystalCountTimer = 0;
        this.endCrystalsAlive = 0;
        for (EndSpikeFeature.Spike lv : EndSpikeFeature.getSpikes(this.world)) {
            this.endCrystalsAlive += this.world.getNonSpectatingEntities(EndCrystalEntity.class, lv.getBoundingBox()).size();
        }
        LOGGER.debug("Found {} end crystals still alive", (Object)this.endCrystalsAlive);
    }

    public void dragonKilled(EnderDragonEntity dragon) {
        if (dragon.getUuid().equals(this.dragonUuid)) {
            this.bossBar.setPercent(0.0f);
            this.bossBar.setVisible(false);
            this.generateEndPortal(true);
            this.generateNewEndGateway();
            if (!this.previouslyKilled) {
                this.world.setBlockState(this.world.getTopPosition(Heightmap.Type.MOTION_BLOCKING, EndPortalFeature.offsetOrigin(this.origin)), Blocks.DRAGON_EGG.getDefaultState());
            }
            this.previouslyKilled = true;
            this.dragonKilled = true;
        }
    }

    @Deprecated
    @VisibleForTesting
    public void clearGatewaysList() {
        this.gateways.clear();
    }

    private void generateNewEndGateway() {
        if (this.gateways.isEmpty()) {
            return;
        }
        int i = this.gateways.remove(this.gateways.size() - 1);
        int j = MathHelper.floor(96.0 * Math.cos(2.0 * (-Math.PI + 0.15707963267948966 * (double)i)));
        int k = MathHelper.floor(96.0 * Math.sin(2.0 * (-Math.PI + 0.15707963267948966 * (double)i)));
        this.generateEndGateway(new BlockPos(j, 75, k));
    }

    private void generateEndGateway(BlockPos pos) {
        this.world.syncWorldEvent(WorldEvents.END_GATEWAY_SPAWNS, pos, 0);
        this.world.getRegistryManager().getOptional(RegistryKeys.CONFIGURED_FEATURE).flatMap(arg -> arg.getEntry(EndConfiguredFeatures.END_GATEWAY_DELAYED)).ifPresent(arg2 -> ((ConfiguredFeature)arg2.value()).generate(this.world, this.world.getChunkManager().getChunkGenerator(), Random.create(), pos));
    }

    private void generateEndPortal(boolean previouslyKilled) {
        EndPortalFeature lv = new EndPortalFeature(previouslyKilled);
        if (this.exitPortalLocation == null) {
            this.exitPortalLocation = this.world.getTopPosition(Heightmap.Type.MOTION_BLOCKING_NO_LEAVES, EndPortalFeature.offsetOrigin(this.origin)).down();
            while (this.world.getBlockState(this.exitPortalLocation).isOf(Blocks.BEDROCK) && this.exitPortalLocation.getY() > this.world.getSeaLevel()) {
                this.exitPortalLocation = this.exitPortalLocation.down();
            }
        }
        if (lv.generateIfValid(FeatureConfig.DEFAULT, this.world, this.world.getChunkManager().getChunkGenerator(), Random.create(), this.exitPortalLocation)) {
            int i = MathHelper.ceilDiv(4, 16);
            this.world.getChunkManager().chunkLoadingManager.forceLighting(new ChunkPos(this.exitPortalLocation), i);
        }
    }

    @Nullable
    private EnderDragonEntity createDragon() {
        this.world.getWorldChunk(new BlockPos(this.origin.getX(), 128 + this.origin.getY(), this.origin.getZ()));
        EnderDragonEntity lv = EntityType.ENDER_DRAGON.create(this.world);
        if (lv != null) {
            lv.setFight(this);
            lv.setFightOrigin(this.origin);
            lv.getPhaseManager().setPhase(PhaseType.HOLDING_PATTERN);
            lv.refreshPositionAndAngles(this.origin.getX(), 128 + this.origin.getY(), this.origin.getZ(), this.world.random.nextFloat() * 360.0f, 0.0f);
            this.world.spawnEntity(lv);
            this.dragonUuid = lv.getUuid();
        }
        return lv;
    }

    public void updateFight(EnderDragonEntity dragon) {
        if (dragon.getUuid().equals(this.dragonUuid)) {
            this.bossBar.setPercent(dragon.getHealth() / dragon.getMaxHealth());
            this.dragonSeenTimer = 0;
            if (dragon.hasCustomName()) {
                this.bossBar.setName(dragon.getDisplayName());
            }
        }
    }

    public int getAliveEndCrystals() {
        return this.endCrystalsAlive;
    }

    public void crystalDestroyed(EndCrystalEntity enderCrystal, DamageSource source) {
        if (this.dragonSpawnState != null && this.crystals.contains(enderCrystal)) {
            LOGGER.debug("Aborting respawn sequence");
            this.dragonSpawnState = null;
            this.spawnStateTimer = 0;
            this.resetEndCrystals();
            this.generateEndPortal(true);
        } else {
            this.countAliveCrystals();
            Entity lv = this.world.getEntity(this.dragonUuid);
            if (lv instanceof EnderDragonEntity) {
                ((EnderDragonEntity)lv).crystalDestroyed(enderCrystal, enderCrystal.getBlockPos(), source);
            }
        }
    }

    public boolean hasPreviouslyKilled() {
        return this.previouslyKilled;
    }

    public void respawnDragon() {
        if (this.dragonKilled && this.dragonSpawnState == null) {
            BlockPos lv = this.exitPortalLocation;
            if (lv == null) {
                LOGGER.debug("Tried to respawn, but need to find the portal first.");
                BlockPattern.Result lv2 = this.findEndPortal();
                if (lv2 == null) {
                    LOGGER.debug("Couldn't find a portal, so we made one.");
                    this.generateEndPortal(true);
                } else {
                    LOGGER.debug("Found the exit portal & saved its location for next time.");
                }
                lv = this.exitPortalLocation;
            }
            ArrayList<EndCrystalEntity> list = Lists.newArrayList();
            BlockPos lv3 = lv.up(1);
            for (Direction lv4 : Direction.Type.HORIZONTAL) {
                List<EndCrystalEntity> list2 = this.world.getNonSpectatingEntities(EndCrystalEntity.class, new Box(lv3.offset(lv4, 2)));
                if (list2.isEmpty()) {
                    return;
                }
                list.addAll(list2);
            }
            LOGGER.debug("Found all crystals, respawning dragon.");
            this.respawnDragon(list);
        }
    }

    private void respawnDragon(List<EndCrystalEntity> crystals) {
        if (this.dragonKilled && this.dragonSpawnState == null) {
            BlockPattern.Result lv = this.findEndPortal();
            while (lv != null) {
                for (int i = 0; i < this.endPortalPattern.getWidth(); ++i) {
                    for (int j = 0; j < this.endPortalPattern.getHeight(); ++j) {
                        for (int k = 0; k < this.endPortalPattern.getDepth(); ++k) {
                            CachedBlockPosition lv2 = lv.translate(i, j, k);
                            if (!lv2.getBlockState().isOf(Blocks.BEDROCK) && !lv2.getBlockState().isOf(Blocks.END_PORTAL)) continue;
                            this.world.setBlockState(lv2.getBlockPos(), Blocks.END_STONE.getDefaultState());
                        }
                    }
                }
                lv = this.findEndPortal();
            }
            this.dragonSpawnState = EnderDragonSpawnState.START;
            this.spawnStateTimer = 0;
            this.generateEndPortal(false);
            this.crystals = crystals;
        }
    }

    public void resetEndCrystals() {
        for (EndSpikeFeature.Spike lv : EndSpikeFeature.getSpikes(this.world)) {
            List<EndCrystalEntity> list = this.world.getNonSpectatingEntities(EndCrystalEntity.class, lv.getBoundingBox());
            for (EndCrystalEntity lv2 : list) {
                lv2.setInvulnerable(false);
                lv2.setBeamTarget(null);
            }
        }
    }

    @Nullable
    public UUID getDragonUuid() {
        return this.dragonUuid;
    }

    public record Data(boolean needsStateScanning, boolean dragonKilled, boolean previouslyKilled, boolean isRespawning, Optional<UUID> dragonUUID, Optional<BlockPos> exitPortalLocation, Optional<List<Integer>> gateways) {
        public static final Codec<Data> CODEC = RecordCodecBuilder.create(instance -> instance.group(((MapCodec)Codec.BOOL.fieldOf("NeedsStateScanning")).orElse(true).forGetter(Data::needsStateScanning), ((MapCodec)Codec.BOOL.fieldOf("DragonKilled")).orElse(false).forGetter(Data::dragonKilled), ((MapCodec)Codec.BOOL.fieldOf("PreviouslyKilled")).orElse(false).forGetter(Data::previouslyKilled), Codec.BOOL.lenientOptionalFieldOf("IsRespawning", false).forGetter(Data::isRespawning), Uuids.INT_STREAM_CODEC.lenientOptionalFieldOf("Dragon").forGetter(Data::dragonUUID), BlockPos.CODEC.lenientOptionalFieldOf("ExitPortalLocation").forGetter(Data::exitPortalLocation), Codec.list(Codec.INT).lenientOptionalFieldOf("Gateways").forGetter(Data::gateways)).apply((Applicative<Data, ?>)instance, Data::new));
        public static final Data DEFAULT = new Data(true, false, false, false, Optional.empty(), Optional.empty(), Optional.empty());
    }
}

