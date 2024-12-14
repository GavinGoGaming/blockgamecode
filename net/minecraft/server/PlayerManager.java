/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 * 
 * Could not load the following classes:
 *  org.jetbrains.annotations.Nullable
 */
package net.minecraft.server;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.mojang.authlib.GameProfile;
import com.mojang.logging.LogUtils;
import com.mojang.serialization.Dynamic;
import java.io.File;
import java.net.SocketAddress;
import java.nio.file.Path;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collection;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.function.Function;
import java.util.function.Predicate;
import net.minecraft.advancement.PlayerAdvancementTracker;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityStatuses;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtOps;
import net.minecraft.network.ClientConnection;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.message.MessageType;
import net.minecraft.network.message.SentMessage;
import net.minecraft.network.message.SignedMessage;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.c2s.common.SyncedClientOptions;
import net.minecraft.network.packet.s2c.common.SynchronizeTagsS2CPacket;
import net.minecraft.network.packet.s2c.play.ChunkLoadDistanceS2CPacket;
import net.minecraft.network.packet.s2c.play.DifficultyS2CPacket;
import net.minecraft.network.packet.s2c.play.EntityStatusEffectS2CPacket;
import net.minecraft.network.packet.s2c.play.EntityStatusS2CPacket;
import net.minecraft.network.packet.s2c.play.ExperienceBarUpdateS2CPacket;
import net.minecraft.network.packet.s2c.play.GameJoinS2CPacket;
import net.minecraft.network.packet.s2c.play.GameStateChangeS2CPacket;
import net.minecraft.network.packet.s2c.play.PlaySoundS2CPacket;
import net.minecraft.network.packet.s2c.play.PlayerAbilitiesS2CPacket;
import net.minecraft.network.packet.s2c.play.PlayerListS2CPacket;
import net.minecraft.network.packet.s2c.play.PlayerRemoveS2CPacket;
import net.minecraft.network.packet.s2c.play.PlayerRespawnS2CPacket;
import net.minecraft.network.packet.s2c.play.PlayerSpawnPositionS2CPacket;
import net.minecraft.network.packet.s2c.play.SimulationDistanceS2CPacket;
import net.minecraft.network.packet.s2c.play.SynchronizeRecipesS2CPacket;
import net.minecraft.network.packet.s2c.play.TeamS2CPacket;
import net.minecraft.network.packet.s2c.play.UpdateSelectedSlotS2CPacket;
import net.minecraft.network.packet.s2c.play.WorldBorderCenterChangedS2CPacket;
import net.minecraft.network.packet.s2c.play.WorldBorderInitializeS2CPacket;
import net.minecraft.network.packet.s2c.play.WorldBorderInterpolateSizeS2CPacket;
import net.minecraft.network.packet.s2c.play.WorldBorderSizeChangedS2CPacket;
import net.minecraft.network.packet.s2c.play.WorldBorderWarningBlocksChangedS2CPacket;
import net.minecraft.network.packet.s2c.play.WorldBorderWarningTimeChangedS2CPacket;
import net.minecraft.network.packet.s2c.play.WorldTimeUpdateS2CPacket;
import net.minecraft.network.state.PlayStateFactories;
import net.minecraft.registry.CombinedDynamicRegistries;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.ServerDynamicRegistryType;
import net.minecraft.registry.tag.TagPacketSerializer;
import net.minecraft.scoreboard.AbstractTeam;
import net.minecraft.scoreboard.ScoreboardDisplaySlot;
import net.minecraft.scoreboard.ScoreboardObjective;
import net.minecraft.scoreboard.ServerScoreboard;
import net.minecraft.scoreboard.Team;
import net.minecraft.server.BannedIpEntry;
import net.minecraft.server.BannedIpList;
import net.minecraft.server.BannedPlayerEntry;
import net.minecraft.server.BannedPlayerList;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.OperatorEntry;
import net.minecraft.server.OperatorList;
import net.minecraft.server.ServerMetadata;
import net.minecraft.server.Whitelist;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ConnectedClientData;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.stat.ServerStatHandler;
import net.minecraft.stat.Stats;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.PathUtil;
import net.minecraft.util.UserCache;
import net.minecraft.util.WorldSavePath;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.GameRules;
import net.minecraft.world.PlayerSaveHandler;
import net.minecraft.world.TeleportTarget;
import net.minecraft.world.World;
import net.minecraft.world.WorldProperties;
import net.minecraft.world.border.WorldBorder;
import net.minecraft.world.border.WorldBorderListener;
import net.minecraft.world.dimension.DimensionType;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;

public abstract class PlayerManager {
    public static final File BANNED_PLAYERS_FILE = new File("banned-players.json");
    public static final File BANNED_IPS_FILE = new File("banned-ips.json");
    public static final File OPERATORS_FILE = new File("ops.json");
    public static final File WHITELIST_FILE = new File("whitelist.json");
    public static final Text FILTERED_FULL_TEXT = Text.translatable("chat.filtered_full");
    public static final Text DUPLICATE_LOGIN_TEXT = Text.translatable("multiplayer.disconnect.duplicate_login");
    private static final Logger LOGGER = LogUtils.getLogger();
    private static final int LATENCY_UPDATE_INTERVAL = 600;
    private static final SimpleDateFormat DATE_FORMATTER = new SimpleDateFormat("yyyy-MM-dd 'at' HH:mm:ss z");
    private final MinecraftServer server;
    private final List<ServerPlayerEntity> players = Lists.newArrayList();
    private final Map<UUID, ServerPlayerEntity> playerMap = Maps.newHashMap();
    private final BannedPlayerList bannedProfiles = new BannedPlayerList(BANNED_PLAYERS_FILE);
    private final BannedIpList bannedIps = new BannedIpList(BANNED_IPS_FILE);
    private final OperatorList ops = new OperatorList(OPERATORS_FILE);
    private final Whitelist whitelist = new Whitelist(WHITELIST_FILE);
    private final Map<UUID, ServerStatHandler> statisticsMap = Maps.newHashMap();
    private final Map<UUID, PlayerAdvancementTracker> advancementTrackers = Maps.newHashMap();
    private final PlayerSaveHandler saveHandler;
    private boolean whitelistEnabled;
    private final CombinedDynamicRegistries<ServerDynamicRegistryType> registryManager;
    protected final int maxPlayers;
    private int viewDistance;
    private int simulationDistance;
    private boolean cheatsAllowed;
    private static final boolean field_29791 = false;
    private int latencyUpdateTimer;

    public PlayerManager(MinecraftServer server, CombinedDynamicRegistries<ServerDynamicRegistryType> registryManager, PlayerSaveHandler saveHandler, int maxPlayers) {
        this.server = server;
        this.registryManager = registryManager;
        this.maxPlayers = maxPlayers;
        this.saveHandler = saveHandler;
    }

    public void onPlayerConnect(ClientConnection connection, ServerPlayerEntity player, ConnectedClientData clientData) {
        NbtCompound lv10;
        Entity lv11;
        ServerWorld lv4;
        String string;
        Optional<Object> optional;
        GameProfile gameProfile = player.getGameProfile();
        UserCache lv = this.server.getUserCache();
        if (lv != null) {
            optional = lv.getByUuid(gameProfile.getId());
            string = optional.map(GameProfile::getName).orElse(gameProfile.getName());
            lv.add(gameProfile);
        } else {
            string = gameProfile.getName();
        }
        optional = this.loadPlayerData(player);
        RegistryKey<World> lv2 = optional.flatMap(nbt -> DimensionType.worldFromDimensionNbt(new Dynamic<NbtElement>(NbtOps.INSTANCE, nbt.get("Dimension"))).resultOrPartial(LOGGER::error)).orElse(World.OVERWORLD);
        ServerWorld lv3 = this.server.getWorld(lv2);
        if (lv3 == null) {
            LOGGER.warn("Unknown respawn dimension {}, defaulting to overworld", (Object)lv2);
            lv4 = this.server.getOverworld();
        } else {
            lv4 = lv3;
        }
        player.setServerWorld(lv4);
        String string2 = connection.getAddressAsString(this.server.shouldLogIps());
        LOGGER.info("{}[{}] logged in with entity id {} at ({}, {}, {})", player.getName().getString(), string2, player.getId(), player.getX(), player.getY(), player.getZ());
        WorldProperties lv5 = lv4.getLevelProperties();
        player.readGameModeNbt(optional.orElse(null));
        ServerPlayNetworkHandler lv6 = new ServerPlayNetworkHandler(this.server, connection, player, clientData);
        connection.transitionInbound(PlayStateFactories.C2S.bind(RegistryByteBuf.makeFactory(this.server.getRegistryManager())), lv6);
        GameRules lv7 = lv4.getGameRules();
        boolean bl = lv7.getBoolean(GameRules.DO_IMMEDIATE_RESPAWN);
        boolean bl2 = lv7.getBoolean(GameRules.REDUCED_DEBUG_INFO);
        boolean bl3 = lv7.getBoolean(GameRules.DO_LIMITED_CRAFTING);
        lv6.sendPacket(new GameJoinS2CPacket(player.getId(), lv5.isHardcore(), this.server.getWorldRegistryKeys(), this.getMaxPlayerCount(), this.viewDistance, this.simulationDistance, bl2, !bl, bl3, player.createCommonPlayerSpawnInfo(lv4), this.server.shouldEnforceSecureProfile()));
        lv6.sendPacket(new DifficultyS2CPacket(lv5.getDifficulty(), lv5.isDifficultyLocked()));
        lv6.sendPacket(new PlayerAbilitiesS2CPacket(player.getAbilities()));
        lv6.sendPacket(new UpdateSelectedSlotS2CPacket(player.getInventory().selectedSlot));
        lv6.sendPacket(new SynchronizeRecipesS2CPacket(this.server.getRecipeManager().sortedValues()));
        this.sendCommandTree(player);
        player.getStatHandler().updateStatSet();
        player.getRecipeBook().sendInitRecipesPacket(player);
        this.sendScoreboard(lv4.getScoreboard(), player);
        this.server.forcePlayerSampleUpdate();
        MutableText lv8 = player.getGameProfile().getName().equalsIgnoreCase(string) ? Text.translatable("multiplayer.player.joined", player.getDisplayName()) : Text.translatable("multiplayer.player.joined.renamed", player.getDisplayName(), string);
        this.broadcast(lv8.formatted(Formatting.YELLOW), false);
        lv6.requestTeleport(player.getX(), player.getY(), player.getZ(), player.getYaw(), player.getPitch());
        ServerMetadata lv9 = this.server.getServerMetadata();
        if (lv9 != null && !clientData.transferred()) {
            player.sendServerMetadata(lv9);
        }
        player.networkHandler.sendPacket(PlayerListS2CPacket.entryFromPlayer(this.players));
        this.players.add(player);
        this.playerMap.put(player.getUuid(), player);
        this.sendToAll(PlayerListS2CPacket.entryFromPlayer(List.of(player)));
        this.sendWorldInfo(player, lv4);
        lv4.onPlayerConnected(player);
        this.server.getBossBarManager().onPlayerConnect(player);
        this.sendStatusEffects(player);
        if (optional.isPresent() && ((NbtCompound)optional.get()).contains("RootVehicle", NbtElement.COMPOUND_TYPE) && (lv11 = EntityType.loadEntityWithPassengers((lv10 = ((NbtCompound)optional.get()).getCompound("RootVehicle")).getCompound("Entity"), lv4, vehicle -> {
            if (!lv4.tryLoadEntity((Entity)vehicle)) {
                return null;
            }
            return vehicle;
        })) != null) {
            UUID uUID = lv10.containsUuid("Attach") ? lv10.getUuid("Attach") : null;
            if (lv11.getUuid().equals(uUID)) {
                player.startRiding(lv11, true);
            } else {
                for (Entity lv12 : lv11.getPassengersDeep()) {
                    if (!lv12.getUuid().equals(uUID)) continue;
                    player.startRiding(lv12, true);
                    break;
                }
            }
            if (!player.hasVehicle()) {
                LOGGER.warn("Couldn't reattach entity to player");
                lv11.discard();
                for (Entity lv12 : lv11.getPassengersDeep()) {
                    lv12.discard();
                }
            }
        }
        player.onSpawn();
    }

    protected void sendScoreboard(ServerScoreboard scoreboard, ServerPlayerEntity player) {
        HashSet<ScoreboardObjective> set = Sets.newHashSet();
        for (Team lv : scoreboard.getTeams()) {
            player.networkHandler.sendPacket(TeamS2CPacket.updateTeam(lv, true));
        }
        for (ScoreboardDisplaySlot lv2 : ScoreboardDisplaySlot.values()) {
            ScoreboardObjective lv3 = scoreboard.getObjectiveForSlot(lv2);
            if (lv3 == null || set.contains(lv3)) continue;
            List<Packet<?>> list = scoreboard.createChangePackets(lv3);
            for (Packet<?> lv4 : list) {
                player.networkHandler.sendPacket(lv4);
            }
            set.add(lv3);
        }
    }

    public void setMainWorld(ServerWorld world) {
        world.getWorldBorder().addListener(new WorldBorderListener(){

            @Override
            public void onSizeChange(WorldBorder border, double size) {
                PlayerManager.this.sendToAll(new WorldBorderSizeChangedS2CPacket(border));
            }

            @Override
            public void onInterpolateSize(WorldBorder border, double fromSize, double toSize, long time) {
                PlayerManager.this.sendToAll(new WorldBorderInterpolateSizeS2CPacket(border));
            }

            @Override
            public void onCenterChanged(WorldBorder border, double centerX, double centerZ) {
                PlayerManager.this.sendToAll(new WorldBorderCenterChangedS2CPacket(border));
            }

            @Override
            public void onWarningTimeChanged(WorldBorder border, int warningTime) {
                PlayerManager.this.sendToAll(new WorldBorderWarningTimeChangedS2CPacket(border));
            }

            @Override
            public void onWarningBlocksChanged(WorldBorder border, int warningBlockDistance) {
                PlayerManager.this.sendToAll(new WorldBorderWarningBlocksChangedS2CPacket(border));
            }

            @Override
            public void onDamagePerBlockChanged(WorldBorder border, double damagePerBlock) {
            }

            @Override
            public void onSafeZoneChanged(WorldBorder border, double safeZoneRadius) {
            }
        });
    }

    public Optional<NbtCompound> loadPlayerData(ServerPlayerEntity player) {
        Optional<NbtCompound> optional;
        NbtCompound lv = this.server.getSaveProperties().getPlayerData();
        if (this.server.isHost(player.getGameProfile()) && lv != null) {
            optional = Optional.of(lv);
            player.readNbt(lv);
            LOGGER.debug("loading single player");
        } else {
            optional = this.saveHandler.loadPlayerData(player);
        }
        return optional;
    }

    protected void savePlayerData(ServerPlayerEntity player) {
        PlayerAdvancementTracker lv2;
        this.saveHandler.savePlayerData(player);
        ServerStatHandler lv = this.statisticsMap.get(player.getUuid());
        if (lv != null) {
            lv.save();
        }
        if ((lv2 = this.advancementTrackers.get(player.getUuid())) != null) {
            lv2.save();
        }
    }

    public void remove(ServerPlayerEntity player) {
        Entity lv2;
        ServerWorld lv = player.getServerWorld();
        player.incrementStat(Stats.LEAVE_GAME);
        this.savePlayerData(player);
        if (player.hasVehicle() && (lv2 = player.getRootVehicle()).hasPlayerRider()) {
            LOGGER.debug("Removing player mount");
            player.stopRiding();
            lv2.streamPassengersAndSelf().forEach(entity -> entity.setRemoved(Entity.RemovalReason.UNLOADED_WITH_PLAYER));
        }
        player.detach();
        lv.removePlayer(player, Entity.RemovalReason.UNLOADED_WITH_PLAYER);
        player.getAdvancementTracker().clearCriteria();
        this.players.remove(player);
        this.server.getBossBarManager().onPlayerDisconnect(player);
        UUID uUID = player.getUuid();
        ServerPlayerEntity lv3 = this.playerMap.get(uUID);
        if (lv3 == player) {
            this.playerMap.remove(uUID);
            this.statisticsMap.remove(uUID);
            this.advancementTrackers.remove(uUID);
        }
        this.sendToAll(new PlayerRemoveS2CPacket(List.of(player.getUuid())));
    }

    @Nullable
    public Text checkCanJoin(SocketAddress address, GameProfile profile) {
        if (this.bannedProfiles.contains(profile)) {
            BannedPlayerEntry lv = (BannedPlayerEntry)this.bannedProfiles.get(profile);
            MutableText lv2 = Text.translatable("multiplayer.disconnect.banned.reason", lv.getReason());
            if (lv.getExpiryDate() != null) {
                lv2.append(Text.translatable("multiplayer.disconnect.banned.expiration", DATE_FORMATTER.format(lv.getExpiryDate())));
            }
            return lv2;
        }
        if (!this.isWhitelisted(profile)) {
            return Text.translatable("multiplayer.disconnect.not_whitelisted");
        }
        if (this.bannedIps.isBanned(address)) {
            BannedIpEntry lv3 = this.bannedIps.get(address);
            MutableText lv2 = Text.translatable("multiplayer.disconnect.banned_ip.reason", lv3.getReason());
            if (lv3.getExpiryDate() != null) {
                lv2.append(Text.translatable("multiplayer.disconnect.banned_ip.expiration", DATE_FORMATTER.format(lv3.getExpiryDate())));
            }
            return lv2;
        }
        if (this.players.size() >= this.maxPlayers && !this.canBypassPlayerLimit(profile)) {
            return Text.translatable("multiplayer.disconnect.server_full");
        }
        return null;
    }

    public ServerPlayerEntity createPlayer(GameProfile profile, SyncedClientOptions syncedOptions) {
        return new ServerPlayerEntity(this.server, this.server.getOverworld(), profile, syncedOptions);
    }

    public boolean disconnectDuplicateLogins(GameProfile profile) {
        UUID uUID = profile.getId();
        Set<ServerPlayerEntity> set = Sets.newIdentityHashSet();
        for (ServerPlayerEntity lv : this.players) {
            if (!lv.getUuid().equals(uUID)) continue;
            set.add(lv);
        }
        ServerPlayerEntity lv2 = this.playerMap.get(profile.getId());
        if (lv2 != null) {
            set.add(lv2);
        }
        for (ServerPlayerEntity lv3 : set) {
            lv3.networkHandler.disconnect(DUPLICATE_LOGIN_TEXT);
        }
        return !set.isEmpty();
    }

    public ServerPlayerEntity respawnPlayer(ServerPlayerEntity player, boolean alive, Entity.RemovalReason removalReason) {
        BlockPos lv7;
        BlockState lv8;
        this.players.remove(player);
        player.getServerWorld().removePlayer(player, removalReason);
        TeleportTarget lv = player.getRespawnTarget(alive, TeleportTarget.NO_OP);
        ServerWorld lv2 = lv.world();
        ServerPlayerEntity lv3 = new ServerPlayerEntity(this.server, lv2, player.getGameProfile(), player.getClientOptions());
        lv3.networkHandler = player.networkHandler;
        lv3.copyFrom(player, alive);
        lv3.setId(player.getId());
        lv3.setMainArm(player.getMainArm());
        if (!lv.missingRespawnBlock()) {
            lv3.setSpawnPointFrom(player);
        }
        for (String string : player.getCommandTags()) {
            lv3.addCommandTag(string);
        }
        Vec3d lv4 = lv.pos();
        lv3.refreshPositionAndAngles(lv4.x, lv4.y, lv4.z, lv.yaw(), lv.pitch());
        if (lv.missingRespawnBlock()) {
            lv3.networkHandler.sendPacket(new GameStateChangeS2CPacket(GameStateChangeS2CPacket.NO_RESPAWN_BLOCK, GameStateChangeS2CPacket.field_33328));
        }
        byte b = alive ? PlayerRespawnS2CPacket.KEEP_ATTRIBUTES : (byte)0;
        ServerWorld lv5 = lv3.getServerWorld();
        WorldProperties lv6 = lv5.getLevelProperties();
        lv3.networkHandler.sendPacket(new PlayerRespawnS2CPacket(lv3.createCommonPlayerSpawnInfo(lv5), b));
        lv3.networkHandler.requestTeleport(lv3.getX(), lv3.getY(), lv3.getZ(), lv3.getYaw(), lv3.getPitch());
        lv3.networkHandler.sendPacket(new PlayerSpawnPositionS2CPacket(lv2.getSpawnPos(), lv2.getSpawnAngle()));
        lv3.networkHandler.sendPacket(new DifficultyS2CPacket(lv6.getDifficulty(), lv6.isDifficultyLocked()));
        lv3.networkHandler.sendPacket(new ExperienceBarUpdateS2CPacket(lv3.experienceProgress, lv3.totalExperience, lv3.experienceLevel));
        this.sendStatusEffects(lv3);
        this.sendWorldInfo(lv3, lv2);
        this.sendCommandTree(lv3);
        lv2.onPlayerRespawned(lv3);
        this.players.add(lv3);
        this.playerMap.put(lv3.getUuid(), lv3);
        lv3.onSpawn();
        lv3.setHealth(lv3.getHealth());
        if (!alive && (lv8 = lv2.getBlockState(lv7 = BlockPos.ofFloored(lv.pos()))).isOf(Blocks.RESPAWN_ANCHOR)) {
            lv3.networkHandler.sendPacket(new PlaySoundS2CPacket(SoundEvents.BLOCK_RESPAWN_ANCHOR_DEPLETE, SoundCategory.BLOCKS, lv7.getX(), lv7.getY(), lv7.getZ(), 1.0f, 1.0f, lv2.getRandom().nextLong()));
        }
        return lv3;
    }

    public void sendStatusEffects(ServerPlayerEntity player) {
        this.sendStatusEffects(player, player.networkHandler);
    }

    public void sendStatusEffects(LivingEntity entity, ServerPlayNetworkHandler networkHandler) {
        for (StatusEffectInstance lv : entity.getStatusEffects()) {
            networkHandler.sendPacket(new EntityStatusEffectS2CPacket(entity.getId(), lv, false));
        }
    }

    public void sendCommandTree(ServerPlayerEntity player) {
        GameProfile gameProfile = player.getGameProfile();
        int i = this.server.getPermissionLevel(gameProfile);
        this.sendCommandTree(player, i);
    }

    public void updatePlayerLatency() {
        if (++this.latencyUpdateTimer > 600) {
            this.sendToAll(new PlayerListS2CPacket(EnumSet.of(PlayerListS2CPacket.Action.UPDATE_LATENCY), this.players));
            this.latencyUpdateTimer = 0;
        }
    }

    public void sendToAll(Packet<?> packet) {
        for (ServerPlayerEntity lv : this.players) {
            lv.networkHandler.sendPacket(packet);
        }
    }

    public void sendToDimension(Packet<?> packet, RegistryKey<World> dimension) {
        for (ServerPlayerEntity lv : this.players) {
            if (lv.getWorld().getRegistryKey() != dimension) continue;
            lv.networkHandler.sendPacket(packet);
        }
    }

    public void sendToTeam(PlayerEntity source, Text message) {
        Team lv = source.getScoreboardTeam();
        if (lv == null) {
            return;
        }
        Collection<String> collection = ((AbstractTeam)lv).getPlayerList();
        for (String string : collection) {
            ServerPlayerEntity lv2 = this.getPlayer(string);
            if (lv2 == null || lv2 == source) continue;
            lv2.sendMessage(message);
        }
    }

    public void sendToOtherTeams(PlayerEntity source, Text message) {
        Team lv = source.getScoreboardTeam();
        if (lv == null) {
            this.broadcast(message, false);
            return;
        }
        for (int i = 0; i < this.players.size(); ++i) {
            ServerPlayerEntity lv2 = this.players.get(i);
            if (lv2.getScoreboardTeam() == lv) continue;
            lv2.sendMessage(message);
        }
    }

    public String[] getPlayerNames() {
        String[] strings = new String[this.players.size()];
        for (int i = 0; i < this.players.size(); ++i) {
            strings[i] = this.players.get(i).getGameProfile().getName();
        }
        return strings;
    }

    public BannedPlayerList getUserBanList() {
        return this.bannedProfiles;
    }

    public BannedIpList getIpBanList() {
        return this.bannedIps;
    }

    public void addToOperators(GameProfile profile) {
        this.ops.add(new OperatorEntry(profile, this.server.getOpPermissionLevel(), this.ops.canBypassPlayerLimit(profile)));
        ServerPlayerEntity lv = this.getPlayer(profile.getId());
        if (lv != null) {
            this.sendCommandTree(lv);
        }
    }

    public void removeFromOperators(GameProfile profile) {
        this.ops.remove(profile);
        ServerPlayerEntity lv = this.getPlayer(profile.getId());
        if (lv != null) {
            this.sendCommandTree(lv);
        }
    }

    private void sendCommandTree(ServerPlayerEntity player, int permissionLevel) {
        if (player.networkHandler != null) {
            byte b = permissionLevel <= 0 ? (byte)24 : (permissionLevel >= 4 ? (byte)28 : (byte)((byte)(EntityStatuses.SET_OP_LEVEL_0 + permissionLevel)));
            player.networkHandler.sendPacket(new EntityStatusS2CPacket(player, b));
        }
        this.server.getCommandManager().sendCommandTree(player);
    }

    public boolean isWhitelisted(GameProfile profile) {
        return !this.whitelistEnabled || this.ops.contains(profile) || this.whitelist.contains(profile);
    }

    public boolean isOperator(GameProfile profile) {
        return this.ops.contains(profile) || this.server.isHost(profile) && this.server.getSaveProperties().areCommandsAllowed() || this.cheatsAllowed;
    }

    @Nullable
    public ServerPlayerEntity getPlayer(String name) {
        int i = this.players.size();
        for (int j = 0; j < i; ++j) {
            ServerPlayerEntity lv = this.players.get(j);
            if (!lv.getGameProfile().getName().equalsIgnoreCase(name)) continue;
            return lv;
        }
        return null;
    }

    public void sendToAround(@Nullable PlayerEntity player, double x, double y, double z, double distance, RegistryKey<World> worldKey, Packet<?> packet) {
        for (int i = 0; i < this.players.size(); ++i) {
            double k;
            double j;
            double h;
            ServerPlayerEntity lv = this.players.get(i);
            if (lv == player || lv.getWorld().getRegistryKey() != worldKey || !((h = x - lv.getX()) * h + (j = y - lv.getY()) * j + (k = z - lv.getZ()) * k < distance * distance)) continue;
            lv.networkHandler.sendPacket(packet);
        }
    }

    public void saveAllPlayerData() {
        for (int i = 0; i < this.players.size(); ++i) {
            this.savePlayerData(this.players.get(i));
        }
    }

    public Whitelist getWhitelist() {
        return this.whitelist;
    }

    public String[] getWhitelistedNames() {
        return this.whitelist.getNames();
    }

    public OperatorList getOpList() {
        return this.ops;
    }

    public String[] getOpNames() {
        return this.ops.getNames();
    }

    public void reloadWhitelist() {
    }

    public void sendWorldInfo(ServerPlayerEntity player, ServerWorld world) {
        WorldBorder lv = this.server.getOverworld().getWorldBorder();
        player.networkHandler.sendPacket(new WorldBorderInitializeS2CPacket(lv));
        player.networkHandler.sendPacket(new WorldTimeUpdateS2CPacket(world.getTime(), world.getTimeOfDay(), world.getGameRules().getBoolean(GameRules.DO_DAYLIGHT_CYCLE)));
        player.networkHandler.sendPacket(new PlayerSpawnPositionS2CPacket(world.getSpawnPos(), world.getSpawnAngle()));
        if (world.isRaining()) {
            player.networkHandler.sendPacket(new GameStateChangeS2CPacket(GameStateChangeS2CPacket.RAIN_STARTED, GameStateChangeS2CPacket.field_33328));
            player.networkHandler.sendPacket(new GameStateChangeS2CPacket(GameStateChangeS2CPacket.RAIN_GRADIENT_CHANGED, world.getRainGradient(1.0f)));
            player.networkHandler.sendPacket(new GameStateChangeS2CPacket(GameStateChangeS2CPacket.THUNDER_GRADIENT_CHANGED, world.getThunderGradient(1.0f)));
        }
        player.networkHandler.sendPacket(new GameStateChangeS2CPacket(GameStateChangeS2CPacket.INITIAL_CHUNKS_COMING, GameStateChangeS2CPacket.field_33328));
        this.server.getTickManager().sendPackets(player);
    }

    public void sendPlayerStatus(ServerPlayerEntity player) {
        player.playerScreenHandler.syncState();
        player.markHealthDirty();
        player.networkHandler.sendPacket(new UpdateSelectedSlotS2CPacket(player.getInventory().selectedSlot));
    }

    public int getCurrentPlayerCount() {
        return this.players.size();
    }

    public int getMaxPlayerCount() {
        return this.maxPlayers;
    }

    public boolean isWhitelistEnabled() {
        return this.whitelistEnabled;
    }

    public void setWhitelistEnabled(boolean whitelistEnabled) {
        this.whitelistEnabled = whitelistEnabled;
    }

    public List<ServerPlayerEntity> getPlayersByIp(String ip) {
        ArrayList<ServerPlayerEntity> list = Lists.newArrayList();
        for (ServerPlayerEntity lv : this.players) {
            if (!lv.getIp().equals(ip)) continue;
            list.add(lv);
        }
        return list;
    }

    public int getViewDistance() {
        return this.viewDistance;
    }

    public int getSimulationDistance() {
        return this.simulationDistance;
    }

    public MinecraftServer getServer() {
        return this.server;
    }

    @Nullable
    public NbtCompound getUserData() {
        return null;
    }

    public void setCheatsAllowed(boolean cheatsAllowed) {
        this.cheatsAllowed = cheatsAllowed;
    }

    public void disconnectAllPlayers() {
        for (int i = 0; i < this.players.size(); ++i) {
            this.players.get((int)i).networkHandler.disconnect(Text.translatable("multiplayer.disconnect.server_shutdown"));
        }
    }

    public void broadcast(Text message, boolean overlay) {
        this.broadcast(message, (ServerPlayerEntity player) -> message, overlay);
    }

    public void broadcast(Text message, Function<ServerPlayerEntity, Text> playerMessageFactory, boolean overlay) {
        this.server.sendMessage(message);
        for (ServerPlayerEntity lv : this.players) {
            Text lv2 = playerMessageFactory.apply(lv);
            if (lv2 == null) continue;
            lv.sendMessageToClient(lv2, overlay);
        }
    }

    public void broadcast(SignedMessage message, ServerCommandSource source, MessageType.Parameters params) {
        this.broadcast(message, source::shouldFilterText, source.getPlayer(), params);
    }

    public void broadcast(SignedMessage message, ServerPlayerEntity sender, MessageType.Parameters params) {
        this.broadcast(message, sender::shouldFilterMessagesSentTo, sender, params);
    }

    private void broadcast(SignedMessage message, Predicate<ServerPlayerEntity> shouldSendFiltered, @Nullable ServerPlayerEntity sender, MessageType.Parameters params) {
        boolean bl = this.verify(message);
        this.server.logChatMessage(message.getContent(), params, bl ? null : "Not Secure");
        SentMessage lv = SentMessage.of(message);
        boolean bl2 = false;
        for (ServerPlayerEntity lv2 : this.players) {
            boolean bl3 = shouldSendFiltered.test(lv2);
            lv2.sendChatMessage(lv, bl3, params);
            bl2 |= bl3 && message.isFullyFiltered();
        }
        if (bl2 && sender != null) {
            sender.sendMessage(FILTERED_FULL_TEXT);
        }
    }

    private boolean verify(SignedMessage message) {
        return message.hasSignature() && !message.isExpiredOnServer(Instant.now());
    }

    public ServerStatHandler createStatHandler(PlayerEntity player) {
        UUID uUID = player.getUuid();
        ServerStatHandler lv = this.statisticsMap.get(uUID);
        if (lv == null) {
            File file3;
            Path path;
            File file = this.server.getSavePath(WorldSavePath.STATS).toFile();
            File file2 = new File(file, String.valueOf(uUID) + ".json");
            if (!file2.exists() && PathUtil.isNormal(path = (file3 = new File(file, player.getName().getString() + ".json")).toPath()) && PathUtil.isAllowedName(path) && path.startsWith(file.getPath()) && file3.isFile()) {
                file3.renameTo(file2);
            }
            lv = new ServerStatHandler(this.server, file2);
            this.statisticsMap.put(uUID, lv);
        }
        return lv;
    }

    public PlayerAdvancementTracker getAdvancementTracker(ServerPlayerEntity player) {
        UUID uUID = player.getUuid();
        PlayerAdvancementTracker lv = this.advancementTrackers.get(uUID);
        if (lv == null) {
            Path path = this.server.getSavePath(WorldSavePath.ADVANCEMENTS).resolve(String.valueOf(uUID) + ".json");
            lv = new PlayerAdvancementTracker(this.server.getDataFixer(), this, this.server.getAdvancementLoader(), path, player);
            this.advancementTrackers.put(uUID, lv);
        }
        lv.setOwner(player);
        return lv;
    }

    public void setViewDistance(int viewDistance) {
        this.viewDistance = viewDistance;
        this.sendToAll(new ChunkLoadDistanceS2CPacket(viewDistance));
        for (ServerWorld lv : this.server.getWorlds()) {
            if (lv == null) continue;
            lv.getChunkManager().applyViewDistance(viewDistance);
        }
    }

    public void setSimulationDistance(int simulationDistance) {
        this.simulationDistance = simulationDistance;
        this.sendToAll(new SimulationDistanceS2CPacket(simulationDistance));
        for (ServerWorld lv : this.server.getWorlds()) {
            if (lv == null) continue;
            lv.getChunkManager().applySimulationDistance(simulationDistance);
        }
    }

    public List<ServerPlayerEntity> getPlayerList() {
        return this.players;
    }

    @Nullable
    public ServerPlayerEntity getPlayer(UUID uuid) {
        return this.playerMap.get(uuid);
    }

    public boolean canBypassPlayerLimit(GameProfile profile) {
        return false;
    }

    public void onDataPacksReloaded() {
        for (PlayerAdvancementTracker lv : this.advancementTrackers.values()) {
            lv.reload(this.server.getAdvancementLoader());
        }
        this.sendToAll(new SynchronizeTagsS2CPacket(TagPacketSerializer.serializeTags(this.registryManager)));
        SynchronizeRecipesS2CPacket lv2 = new SynchronizeRecipesS2CPacket(this.server.getRecipeManager().sortedValues());
        for (ServerPlayerEntity lv3 : this.players) {
            lv3.networkHandler.sendPacket(lv2);
            lv3.getRecipeBook().sendInitRecipesPacket(lv3);
        }
    }

    public boolean areCheatsAllowed() {
        return this.cheatsAllowed;
    }
}

