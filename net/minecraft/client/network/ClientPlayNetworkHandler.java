/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 * 
 * Could not load the following classes:
 *  net.fabricmc.api.EnvType
 *  net.fabricmc.api.Environment
 *  org.jetbrains.annotations.Nullable
 */
package net.minecraft.client.network;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.mojang.authlib.GameProfile;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.ParseResults;
import com.mojang.logging.LogUtils;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.ReferenceOpenHashSet;
import java.time.Instant;
import java.util.ArrayList;
import java.util.BitSet;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.Executor;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.advancement.AdvancementEntry;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.CommandBlockBlockEntity;
import net.minecraft.block.entity.SignBlockEntity;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.hud.ChatHud;
import net.minecraft.client.gui.screen.CreditsScreen;
import net.minecraft.client.gui.screen.DeathScreen;
import net.minecraft.client.gui.screen.DemoScreen;
import net.minecraft.client.gui.screen.DownloadingTerrainScreen;
import net.minecraft.client.gui.screen.ReconfiguringScreen;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.StatsScreen;
import net.minecraft.client.gui.screen.ingame.BookScreen;
import net.minecraft.client.gui.screen.ingame.CommandBlockScreen;
import net.minecraft.client.gui.screen.ingame.CreativeInventoryScreen;
import net.minecraft.client.gui.screen.ingame.HandledScreens;
import net.minecraft.client.gui.screen.ingame.HorseScreen;
import net.minecraft.client.gui.screen.recipebook.RecipeBookProvider;
import net.minecraft.client.gui.screen.recipebook.RecipeBookWidget;
import net.minecraft.client.input.KeyboardInput;
import net.minecraft.client.network.ChunkBatchSizeCalculator;
import net.minecraft.client.network.ClientAdvancementManager;
import net.minecraft.client.network.ClientCommandSource;
import net.minecraft.client.network.ClientCommonNetworkHandler;
import net.minecraft.client.network.ClientConfigurationNetworkHandler;
import net.minecraft.client.network.ClientConnectionState;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.network.ClientPlayerInteractionManager;
import net.minecraft.client.network.ClientTagLoader;
import net.minecraft.client.network.DataQueryHandler;
import net.minecraft.client.network.DebugSampleSubscriber;
import net.minecraft.client.network.OtherClientPlayerEntity;
import net.minecraft.client.network.PingMeasurer;
import net.minecraft.client.network.PlayerListEntry;
import net.minecraft.client.network.ServerInfo;
import net.minecraft.client.network.WorldLoadingState;
import net.minecraft.client.option.GameOptions;
import net.minecraft.client.option.ServerList;
import net.minecraft.client.particle.ItemPickupParticle;
import net.minecraft.client.recipebook.ClientRecipeBook;
import net.minecraft.client.render.MapRenderer;
import net.minecraft.client.render.debug.NeighborUpdateDebugRenderer;
import net.minecraft.client.render.debug.VillageDebugRenderer;
import net.minecraft.client.render.debug.VillageSectionsDebugRenderer;
import net.minecraft.client.render.debug.WorldGenAttemptDebugRenderer;
import net.minecraft.client.search.SearchManager;
import net.minecraft.client.session.ProfileKeys;
import net.minecraft.client.sound.AbstractBeeSoundInstance;
import net.minecraft.client.sound.AggressiveBeeSoundInstance;
import net.minecraft.client.sound.GuardianAttackSoundInstance;
import net.minecraft.client.sound.MovingMinecartSoundInstance;
import net.minecraft.client.sound.PassiveBeeSoundInstance;
import net.minecraft.client.sound.SnifferDigSoundInstance;
import net.minecraft.client.toast.RecipeToast;
import net.minecraft.client.toast.SystemToast;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.command.CommandRegistryAccess;
import net.minecraft.command.CommandSource;
import net.minecraft.command.argument.SignedArgumentList;
import net.minecraft.component.type.MapIdComponent;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.ExperienceOrbEntity;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.Leashable;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.TrackedPosition;
import net.minecraft.entity.attribute.AttributeContainer;
import net.minecraft.entity.attribute.EntityAttributeInstance;
import net.minecraft.entity.attribute.EntityAttributeModifier;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.mob.GuardianEntity;
import net.minecraft.entity.passive.AbstractHorseEntity;
import net.minecraft.entity.passive.BeeEntity;
import net.minecraft.entity.passive.SnifferEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.entity.projectile.ExplosiveProjectileEntity;
import net.minecraft.entity.vehicle.AbstractMinecartEntity;
import net.minecraft.entity.vehicle.BoatEntity;
import net.minecraft.inventory.SimpleInventory;
import net.minecraft.item.ItemGroups;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.item.map.MapState;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.network.ClientConnection;
import net.minecraft.network.NetworkThreadUtils;
import net.minecraft.network.encryption.ClientPlayerSession;
import net.minecraft.network.encryption.NetworkEncryptionUtils;
import net.minecraft.network.encryption.PlayerKeyPair;
import net.minecraft.network.encryption.PlayerPublicKey;
import net.minecraft.network.encryption.PublicPlayerSession;
import net.minecraft.network.encryption.SignatureVerifier;
import net.minecraft.network.listener.ClientPlayPacketListener;
import net.minecraft.network.listener.TickablePacketListener;
import net.minecraft.network.message.ArgumentSignatureDataMap;
import net.minecraft.network.message.LastSeenMessagesCollector;
import net.minecraft.network.message.MessageBody;
import net.minecraft.network.message.MessageChain;
import net.minecraft.network.message.MessageLink;
import net.minecraft.network.message.MessageSignatureData;
import net.minecraft.network.message.MessageSignatureStorage;
import net.minecraft.network.message.SignedMessage;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.c2s.play.AcknowledgeChunksC2SPacket;
import net.minecraft.network.packet.c2s.play.AcknowledgeReconfigurationC2SPacket;
import net.minecraft.network.packet.c2s.play.ChatCommandSignedC2SPacket;
import net.minecraft.network.packet.c2s.play.ChatMessageC2SPacket;
import net.minecraft.network.packet.c2s.play.ClientStatusC2SPacket;
import net.minecraft.network.packet.c2s.play.CommandExecutionC2SPacket;
import net.minecraft.network.packet.c2s.play.MessageAcknowledgmentC2SPacket;
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket;
import net.minecraft.network.packet.c2s.play.PlayerSessionC2SPacket;
import net.minecraft.network.packet.c2s.play.TeleportConfirmC2SPacket;
import net.minecraft.network.packet.c2s.play.VehicleMoveC2SPacket;
import net.minecraft.network.packet.s2c.common.SynchronizeTagsS2CPacket;
import net.minecraft.network.packet.s2c.custom.DebugBeeCustomPayload;
import net.minecraft.network.packet.s2c.custom.DebugBrainCustomPayload;
import net.minecraft.network.packet.s2c.custom.DebugBreezeCustomPayload;
import net.minecraft.network.packet.s2c.custom.DebugGameEventCustomPayload;
import net.minecraft.network.packet.s2c.custom.DebugGameEventListenersCustomPayload;
import net.minecraft.network.packet.s2c.custom.DebugGameTestAddMarkerCustomPayload;
import net.minecraft.network.packet.s2c.custom.DebugGameTestClearCustomPayload;
import net.minecraft.network.packet.s2c.custom.DebugGoalSelectorCustomPayload;
import net.minecraft.network.packet.s2c.custom.DebugHiveCustomPayload;
import net.minecraft.network.packet.s2c.custom.DebugNeighborsUpdateCustomPayload;
import net.minecraft.network.packet.s2c.custom.DebugPathCustomPayload;
import net.minecraft.network.packet.s2c.custom.DebugPoiAddedCustomPayload;
import net.minecraft.network.packet.s2c.custom.DebugPoiRemovedCustomPayload;
import net.minecraft.network.packet.s2c.custom.DebugPoiTicketCountCustomPayload;
import net.minecraft.network.packet.s2c.custom.DebugRaidsCustomPayload;
import net.minecraft.network.packet.s2c.custom.DebugStructuresCustomPayload;
import net.minecraft.network.packet.s2c.custom.DebugVillageSectionsCustomPayload;
import net.minecraft.network.packet.s2c.custom.DebugWorldgenAttemptCustomPayload;
import net.minecraft.network.packet.s2c.play.AdvancementUpdateS2CPacket;
import net.minecraft.network.packet.s2c.play.BlockBreakingProgressS2CPacket;
import net.minecraft.network.packet.s2c.play.BlockEntityUpdateS2CPacket;
import net.minecraft.network.packet.s2c.play.BlockEventS2CPacket;
import net.minecraft.network.packet.s2c.play.BlockUpdateS2CPacket;
import net.minecraft.network.packet.s2c.play.BossBarS2CPacket;
import net.minecraft.network.packet.s2c.play.BundleS2CPacket;
import net.minecraft.network.packet.s2c.play.ChangeUnlockedRecipesS2CPacket;
import net.minecraft.network.packet.s2c.play.ChatMessageS2CPacket;
import net.minecraft.network.packet.s2c.play.ChatSuggestionsS2CPacket;
import net.minecraft.network.packet.s2c.play.ChunkBiomeDataS2CPacket;
import net.minecraft.network.packet.s2c.play.ChunkData;
import net.minecraft.network.packet.s2c.play.ChunkDataS2CPacket;
import net.minecraft.network.packet.s2c.play.ChunkDeltaUpdateS2CPacket;
import net.minecraft.network.packet.s2c.play.ChunkLoadDistanceS2CPacket;
import net.minecraft.network.packet.s2c.play.ChunkRenderDistanceCenterS2CPacket;
import net.minecraft.network.packet.s2c.play.ChunkSentS2CPacket;
import net.minecraft.network.packet.s2c.play.ClearTitleS2CPacket;
import net.minecraft.network.packet.s2c.play.CloseScreenS2CPacket;
import net.minecraft.network.packet.s2c.play.CommandSuggestionsS2CPacket;
import net.minecraft.network.packet.s2c.play.CommandTreeS2CPacket;
import net.minecraft.network.packet.s2c.play.CommonPlayerSpawnInfo;
import net.minecraft.network.packet.s2c.play.CooldownUpdateS2CPacket;
import net.minecraft.network.packet.s2c.play.CraftFailedResponseS2CPacket;
import net.minecraft.network.packet.s2c.play.DamageTiltS2CPacket;
import net.minecraft.network.packet.s2c.play.DeathMessageS2CPacket;
import net.minecraft.network.packet.s2c.play.DebugSampleS2CPacket;
import net.minecraft.network.packet.s2c.play.DifficultyS2CPacket;
import net.minecraft.network.packet.s2c.play.EndCombatS2CPacket;
import net.minecraft.network.packet.s2c.play.EnterCombatS2CPacket;
import net.minecraft.network.packet.s2c.play.EnterReconfigurationS2CPacket;
import net.minecraft.network.packet.s2c.play.EntitiesDestroyS2CPacket;
import net.minecraft.network.packet.s2c.play.EntityAnimationS2CPacket;
import net.minecraft.network.packet.s2c.play.EntityAttachS2CPacket;
import net.minecraft.network.packet.s2c.play.EntityAttributesS2CPacket;
import net.minecraft.network.packet.s2c.play.EntityDamageS2CPacket;
import net.minecraft.network.packet.s2c.play.EntityEquipmentUpdateS2CPacket;
import net.minecraft.network.packet.s2c.play.EntityPassengersSetS2CPacket;
import net.minecraft.network.packet.s2c.play.EntityPositionS2CPacket;
import net.minecraft.network.packet.s2c.play.EntityS2CPacket;
import net.minecraft.network.packet.s2c.play.EntitySetHeadYawS2CPacket;
import net.minecraft.network.packet.s2c.play.EntitySpawnS2CPacket;
import net.minecraft.network.packet.s2c.play.EntityStatusEffectS2CPacket;
import net.minecraft.network.packet.s2c.play.EntityStatusS2CPacket;
import net.minecraft.network.packet.s2c.play.EntityTrackerUpdateS2CPacket;
import net.minecraft.network.packet.s2c.play.EntityVelocityUpdateS2CPacket;
import net.minecraft.network.packet.s2c.play.ExperienceBarUpdateS2CPacket;
import net.minecraft.network.packet.s2c.play.ExperienceOrbSpawnS2CPacket;
import net.minecraft.network.packet.s2c.play.ExplosionS2CPacket;
import net.minecraft.network.packet.s2c.play.GameJoinS2CPacket;
import net.minecraft.network.packet.s2c.play.GameMessageS2CPacket;
import net.minecraft.network.packet.s2c.play.GameStateChangeS2CPacket;
import net.minecraft.network.packet.s2c.play.HealthUpdateS2CPacket;
import net.minecraft.network.packet.s2c.play.InventoryS2CPacket;
import net.minecraft.network.packet.s2c.play.ItemPickupAnimationS2CPacket;
import net.minecraft.network.packet.s2c.play.LightData;
import net.minecraft.network.packet.s2c.play.LightUpdateS2CPacket;
import net.minecraft.network.packet.s2c.play.LookAtS2CPacket;
import net.minecraft.network.packet.s2c.play.MapUpdateS2CPacket;
import net.minecraft.network.packet.s2c.play.NbtQueryResponseS2CPacket;
import net.minecraft.network.packet.s2c.play.OpenHorseScreenS2CPacket;
import net.minecraft.network.packet.s2c.play.OpenScreenS2CPacket;
import net.minecraft.network.packet.s2c.play.OpenWrittenBookS2CPacket;
import net.minecraft.network.packet.s2c.play.OverlayMessageS2CPacket;
import net.minecraft.network.packet.s2c.play.ParticleS2CPacket;
import net.minecraft.network.packet.s2c.play.PlaySoundFromEntityS2CPacket;
import net.minecraft.network.packet.s2c.play.PlaySoundS2CPacket;
import net.minecraft.network.packet.s2c.play.PlayerAbilitiesS2CPacket;
import net.minecraft.network.packet.s2c.play.PlayerActionResponseS2CPacket;
import net.minecraft.network.packet.s2c.play.PlayerListHeaderS2CPacket;
import net.minecraft.network.packet.s2c.play.PlayerListS2CPacket;
import net.minecraft.network.packet.s2c.play.PlayerPositionLookS2CPacket;
import net.minecraft.network.packet.s2c.play.PlayerRemoveS2CPacket;
import net.minecraft.network.packet.s2c.play.PlayerRespawnS2CPacket;
import net.minecraft.network.packet.s2c.play.PlayerSpawnPositionS2CPacket;
import net.minecraft.network.packet.s2c.play.PositionFlag;
import net.minecraft.network.packet.s2c.play.ProfilelessChatMessageS2CPacket;
import net.minecraft.network.packet.s2c.play.ProjectilePowerS2CPacket;
import net.minecraft.network.packet.s2c.play.RemoveEntityStatusEffectS2CPacket;
import net.minecraft.network.packet.s2c.play.RemoveMessageS2CPacket;
import net.minecraft.network.packet.s2c.play.ScoreboardDisplayS2CPacket;
import net.minecraft.network.packet.s2c.play.ScoreboardObjectiveUpdateS2CPacket;
import net.minecraft.network.packet.s2c.play.ScoreboardScoreResetS2CPacket;
import net.minecraft.network.packet.s2c.play.ScoreboardScoreUpdateS2CPacket;
import net.minecraft.network.packet.s2c.play.ScreenHandlerPropertyUpdateS2CPacket;
import net.minecraft.network.packet.s2c.play.ScreenHandlerSlotUpdateS2CPacket;
import net.minecraft.network.packet.s2c.play.SelectAdvancementTabS2CPacket;
import net.minecraft.network.packet.s2c.play.ServerMetadataS2CPacket;
import net.minecraft.network.packet.s2c.play.SetCameraEntityS2CPacket;
import net.minecraft.network.packet.s2c.play.SetTradeOffersS2CPacket;
import net.minecraft.network.packet.s2c.play.SignEditorOpenS2CPacket;
import net.minecraft.network.packet.s2c.play.SimulationDistanceS2CPacket;
import net.minecraft.network.packet.s2c.play.StartChunkSendS2CPacket;
import net.minecraft.network.packet.s2c.play.StatisticsS2CPacket;
import net.minecraft.network.packet.s2c.play.StopSoundS2CPacket;
import net.minecraft.network.packet.s2c.play.SubtitleS2CPacket;
import net.minecraft.network.packet.s2c.play.SynchronizeRecipesS2CPacket;
import net.minecraft.network.packet.s2c.play.TeamS2CPacket;
import net.minecraft.network.packet.s2c.play.TickStepS2CPacket;
import net.minecraft.network.packet.s2c.play.TitleFadeS2CPacket;
import net.minecraft.network.packet.s2c.play.TitleS2CPacket;
import net.minecraft.network.packet.s2c.play.UnloadChunkS2CPacket;
import net.minecraft.network.packet.s2c.play.UpdateSelectedSlotS2CPacket;
import net.minecraft.network.packet.s2c.play.UpdateTickRateS2CPacket;
import net.minecraft.network.packet.s2c.play.VehicleMoveS2CPacket;
import net.minecraft.network.packet.s2c.play.WorldBorderCenterChangedS2CPacket;
import net.minecraft.network.packet.s2c.play.WorldBorderInitializeS2CPacket;
import net.minecraft.network.packet.s2c.play.WorldBorderInterpolateSizeS2CPacket;
import net.minecraft.network.packet.s2c.play.WorldBorderSizeChangedS2CPacket;
import net.minecraft.network.packet.s2c.play.WorldBorderWarningBlocksChangedS2CPacket;
import net.minecraft.network.packet.s2c.play.WorldBorderWarningTimeChangedS2CPacket;
import net.minecraft.network.packet.s2c.play.WorldEventS2CPacket;
import net.minecraft.network.packet.s2c.play.WorldTimeUpdateS2CPacket;
import net.minecraft.network.packet.s2c.query.PingResultS2CPacket;
import net.minecraft.network.state.ConfigurationStates;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.recipe.BrewingRecipeRegistry;
import net.minecraft.recipe.RecipeEntry;
import net.minecraft.recipe.RecipeManager;
import net.minecraft.registry.DynamicRegistryManager;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.resource.featuretoggle.FeatureSet;
import net.minecraft.scoreboard.AbstractTeam;
import net.minecraft.scoreboard.ScoreAccess;
import net.minecraft.scoreboard.ScoreHolder;
import net.minecraft.scoreboard.Scoreboard;
import net.minecraft.scoreboard.ScoreboardCriterion;
import net.minecraft.scoreboard.ScoreboardObjective;
import net.minecraft.scoreboard.Team;
import net.minecraft.screen.HorseScreenHandler;
import net.minecraft.screen.MerchantScreenHandler;
import net.minecraft.screen.PlayerScreenHandler;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.slot.Slot;
import net.minecraft.server.ServerLinks;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.stat.Stat;
import net.minecraft.stat.StatHandler;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Hand;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.ChunkSectionPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.Difficulty;
import net.minecraft.world.GameMode;
import net.minecraft.world.LightType;
import net.minecraft.world.World;
import net.minecraft.world.border.WorldBorder;
import net.minecraft.world.chunk.ChunkNibbleArray;
import net.minecraft.world.chunk.ChunkSection;
import net.minecraft.world.chunk.WorldChunk;
import net.minecraft.world.chunk.light.LightingProvider;
import net.minecraft.world.dimension.DimensionType;
import net.minecraft.world.explosion.Explosion;
import net.minecraft.world.tick.TickManager;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;

@Environment(value=EnvType.CLIENT)
public class ClientPlayNetworkHandler
extends ClientCommonNetworkHandler
implements ClientPlayPacketListener,
TickablePacketListener {
    private static final Logger LOGGER = LogUtils.getLogger();
    private static final Text UNSECURE_SERVER_TOAST_TITLE = Text.translatable("multiplayer.unsecureserver.toast.title");
    private static final Text UNSECURE_SERVER_TOAST_TEXT = Text.translatable("multiplayer.unsecureserver.toast");
    private static final Text INVALID_PACKET_TEXT = Text.translatable("multiplayer.disconnect.invalid_packet");
    private static final Text CHAT_VALIDATION_FAILED_TEXT = Text.translatable("multiplayer.disconnect.chat_validation_failed");
    private static final Text RECONFIGURING_TEXT = Text.translatable("connect.reconfiguring");
    private static final int ACKNOWLEDGMENT_BATCH_SIZE = 64;
    private final GameProfile profile;
    private ClientWorld world;
    private ClientWorld.Properties worldProperties;
    private final Map<UUID, PlayerListEntry> playerListEntries = Maps.newHashMap();
    private final Set<PlayerListEntry> listedPlayerListEntries = new ReferenceOpenHashSet<PlayerListEntry>();
    private final ClientAdvancementManager advancementHandler;
    private final ClientCommandSource commandSource;
    private final DataQueryHandler dataQueryHandler = new DataQueryHandler(this);
    private int chunkLoadDistance = 3;
    private int simulationDistance = 3;
    private final Random random = Random.createThreadSafe();
    private CommandDispatcher<CommandSource> commandDispatcher = new CommandDispatcher();
    private final RecipeManager recipeManager;
    private final UUID sessionId = UUID.randomUUID();
    private Set<RegistryKey<World>> worldKeys;
    private final DynamicRegistryManager.Immutable combinedDynamicRegistries;
    private final FeatureSet enabledFeatures;
    private final BrewingRecipeRegistry brewingRecipeRegistry;
    @Nullable
    private ClientPlayerSession session;
    private MessageChain.Packer messagePacker = MessageChain.Packer.NONE;
    private LastSeenMessagesCollector lastSeenMessagesCollector = new LastSeenMessagesCollector(20);
    private MessageSignatureStorage signatureStorage = MessageSignatureStorage.create();
    private final ChunkBatchSizeCalculator chunkBatchSizeCalculator = new ChunkBatchSizeCalculator();
    private final PingMeasurer pingMeasurer;
    private final DebugSampleSubscriber debugSampleSubscriber;
    @Nullable
    private WorldLoadingState worldLoadingState;
    private boolean secureChatEnforced;
    private boolean displayedUnsecureChatWarning = false;
    private volatile boolean worldCleared;
    private final Scoreboard scoreboard = new Scoreboard();
    private final SearchManager searchManager = new SearchManager();

    public ClientPlayNetworkHandler(MinecraftClient client, ClientConnection arg2, ClientConnectionState arg3) {
        super(client, arg2, arg3);
        this.profile = arg3.localGameProfile();
        this.combinedDynamicRegistries = arg3.receivedRegistries();
        this.enabledFeatures = arg3.enabledFeatures();
        this.advancementHandler = new ClientAdvancementManager(client, this.worldSession);
        this.commandSource = new ClientCommandSource(this, client);
        this.pingMeasurer = new PingMeasurer(this, client.getDebugHud().getPingLog());
        this.recipeManager = new RecipeManager(this.combinedDynamicRegistries);
        this.debugSampleSubscriber = new DebugSampleSubscriber(this, client.getDebugHud());
        if (arg3.chatState() != null) {
            client.inGameHud.getChatHud().restoreChatState(arg3.chatState());
        }
        this.brewingRecipeRegistry = BrewingRecipeRegistry.create(this.enabledFeatures);
    }

    public ClientCommandSource getCommandSource() {
        return this.commandSource;
    }

    public void unloadWorld() {
        this.worldCleared = true;
        this.clearWorld();
        this.worldSession.onUnload();
    }

    public void clearWorld() {
        this.world = null;
        this.worldLoadingState = null;
    }

    public RecipeManager getRecipeManager() {
        return this.recipeManager;
    }

    @Override
    public void onGameJoin(GameJoinS2CPacket packet) {
        ClientWorld.Properties lv4;
        NetworkThreadUtils.forceMainThread(packet, this, this.client);
        this.client.interactionManager = new ClientPlayerInteractionManager(this.client, this);
        CommonPlayerSpawnInfo lv = packet.commonPlayerSpawnInfo();
        ArrayList<RegistryKey<World>> list = Lists.newArrayList(packet.dimensionIds());
        Collections.shuffle(list);
        this.worldKeys = Sets.newLinkedHashSet(list);
        RegistryKey<World> lv2 = lv.dimension();
        RegistryEntry<DimensionType> lv3 = lv.dimensionType();
        this.chunkLoadDistance = packet.viewDistance();
        this.simulationDistance = packet.simulationDistance();
        boolean bl = lv.isDebug();
        boolean bl2 = lv.isFlat();
        this.worldProperties = lv4 = new ClientWorld.Properties(Difficulty.NORMAL, packet.hardcore(), bl2);
        this.world = new ClientWorld(this, lv4, lv2, lv3, this.chunkLoadDistance, this.simulationDistance, this.client::getProfiler, this.client.worldRenderer, bl, lv.seed());
        this.client.joinWorld(this.world, DownloadingTerrainScreen.WorldEntryReason.OTHER);
        if (this.client.player == null) {
            this.client.player = this.client.interactionManager.createPlayer(this.world, new StatHandler(), new ClientRecipeBook());
            this.client.player.setYaw(-180.0f);
            if (this.client.getServer() != null) {
                this.client.getServer().setLocalPlayerUuid(this.client.player.getUuid());
            }
        }
        this.client.debugRenderer.reset();
        this.client.player.init();
        this.client.player.setId(packet.playerEntityId());
        this.world.addEntity(this.client.player);
        this.client.player.input = new KeyboardInput(this.client.options);
        this.client.interactionManager.copyAbilities(this.client.player);
        this.client.cameraEntity = this.client.player;
        this.startWorldLoading(this.client.player, this.world, DownloadingTerrainScreen.WorldEntryReason.OTHER);
        this.client.player.setReducedDebugInfo(packet.reducedDebugInfo());
        this.client.player.setShowsDeathScreen(packet.showDeathScreen());
        this.client.player.setLimitedCraftingEnabled(packet.doLimitedCrafting());
        this.client.player.setLastDeathPos(lv.lastDeathLocation());
        this.client.player.setPortalCooldown(lv.portalCooldown());
        this.client.interactionManager.setGameModes(lv.gameMode(), lv.prevGameMode());
        this.client.options.setServerViewDistance(packet.viewDistance());
        this.session = null;
        this.lastSeenMessagesCollector = new LastSeenMessagesCollector(20);
        this.signatureStorage = MessageSignatureStorage.create();
        if (this.connection.isEncrypted()) {
            this.client.getProfileKeys().fetchKeyPair().thenAcceptAsync(keyPair -> keyPair.ifPresent(this::updateKeyPair), (Executor)this.client);
        }
        this.worldSession.setGameMode(lv.gameMode(), packet.hardcore());
        this.client.getQuickPlayLogger().save(this.client);
        this.secureChatEnforced = packet.enforcesSecureChat();
        if (this.serverInfo != null && !this.displayedUnsecureChatWarning && !this.isSecureChatEnforced()) {
            SystemToast lv5 = SystemToast.create(this.client, SystemToast.Type.UNSECURE_SERVER_WARNING, UNSECURE_SERVER_TOAST_TITLE, UNSECURE_SERVER_TOAST_TEXT);
            this.client.getToastManager().add(lv5);
            this.displayedUnsecureChatWarning = true;
        }
    }

    @Override
    public void onEntitySpawn(EntitySpawnS2CPacket packet) {
        NetworkThreadUtils.forceMainThread(packet, this, this.client);
        Entity lv = this.createEntity(packet);
        if (lv != null) {
            lv.onSpawnPacket(packet);
            this.world.addEntity(lv);
            this.playSpawnSound(lv);
        } else {
            LOGGER.warn("Skipping Entity with id {}", (Object)packet.getEntityType());
        }
    }

    @Nullable
    private Entity createEntity(EntitySpawnS2CPacket packet) {
        EntityType<?> lv = packet.getEntityType();
        if (lv == EntityType.PLAYER) {
            PlayerListEntry lv2 = this.getPlayerListEntry(packet.getUuid());
            if (lv2 == null) {
                LOGGER.warn("Server attempted to add player prior to sending player info (Player id {})", (Object)packet.getUuid());
                return null;
            }
            return new OtherClientPlayerEntity(this.world, lv2.getProfile());
        }
        return lv.create(this.world);
    }

    private void playSpawnSound(Entity entity) {
        if (entity instanceof AbstractMinecartEntity) {
            AbstractMinecartEntity lv = (AbstractMinecartEntity)entity;
            this.client.getSoundManager().play(new MovingMinecartSoundInstance(lv));
        } else if (entity instanceof BeeEntity) {
            BeeEntity lv2 = (BeeEntity)entity;
            boolean bl = lv2.hasAngerTime();
            AbstractBeeSoundInstance lv3 = bl ? new AggressiveBeeSoundInstance(lv2) : new PassiveBeeSoundInstance(lv2);
            this.client.getSoundManager().playNextTick(lv3);
        }
    }

    @Override
    public void onExperienceOrbSpawn(ExperienceOrbSpawnS2CPacket packet) {
        NetworkThreadUtils.forceMainThread(packet, this, this.client);
        double d = packet.getX();
        double e = packet.getY();
        double f = packet.getZ();
        ExperienceOrbEntity lv = new ExperienceOrbEntity(this.world, d, e, f, packet.getExperience());
        lv.updateTrackedPosition(d, e, f);
        lv.setYaw(0.0f);
        lv.setPitch(0.0f);
        lv.setId(packet.getEntityId());
        this.world.addEntity(lv);
    }

    @Override
    public void onEntityVelocityUpdate(EntityVelocityUpdateS2CPacket packet) {
        NetworkThreadUtils.forceMainThread(packet, this, this.client);
        Entity lv = this.world.getEntityById(packet.getEntityId());
        if (lv == null) {
            return;
        }
        lv.setVelocityClient(packet.getVelocityX(), packet.getVelocityY(), packet.getVelocityZ());
    }

    @Override
    public void onEntityTrackerUpdate(EntityTrackerUpdateS2CPacket packet) {
        NetworkThreadUtils.forceMainThread(packet, this, this.client);
        Entity lv = this.world.getEntityById(packet.id());
        if (lv != null) {
            lv.getDataTracker().writeUpdatedEntries(packet.trackedValues());
        }
    }

    @Override
    public void onEntityPosition(EntityPositionS2CPacket packet) {
        NetworkThreadUtils.forceMainThread(packet, this, this.client);
        Entity lv = this.world.getEntityById(packet.getEntityId());
        if (lv == null) {
            return;
        }
        double d = packet.getX();
        double e = packet.getY();
        double f = packet.getZ();
        lv.updateTrackedPosition(d, e, f);
        if (!lv.isLogicalSideForUpdatingMovement()) {
            float g = (float)(packet.getYaw() * 360) / 256.0f;
            float h = (float)(packet.getPitch() * 360) / 256.0f;
            lv.updateTrackedPositionAndAngles(d, e, f, g, h, 3);
            lv.setOnGround(packet.isOnGround());
        }
    }

    @Override
    public void onUpdateTickRate(UpdateTickRateS2CPacket packet) {
        NetworkThreadUtils.forceMainThread(packet, this, this.client);
        if (this.client.world == null) {
            return;
        }
        TickManager lv = this.client.world.getTickManager();
        lv.setTickRate(packet.tickRate());
        lv.setFrozen(packet.isFrozen());
    }

    @Override
    public void onTickStep(TickStepS2CPacket packet) {
        NetworkThreadUtils.forceMainThread(packet, this, this.client);
        if (this.client.world == null) {
            return;
        }
        TickManager lv = this.client.world.getTickManager();
        lv.setStepTicks(packet.tickSteps());
    }

    @Override
    public void onUpdateSelectedSlot(UpdateSelectedSlotS2CPacket packet) {
        NetworkThreadUtils.forceMainThread(packet, this, this.client);
        if (PlayerInventory.isValidHotbarIndex(packet.getSlot())) {
            this.client.player.getInventory().selectedSlot = packet.getSlot();
        }
    }

    @Override
    public void onEntity(EntityS2CPacket packet) {
        NetworkThreadUtils.forceMainThread(packet, this, this.client);
        Entity lv = packet.getEntity(this.world);
        if (lv == null) {
            return;
        }
        if (!lv.isLogicalSideForUpdatingMovement()) {
            if (packet.isPositionChanged()) {
                TrackedPosition lv2 = lv.getTrackedPosition();
                Vec3d lv3 = lv2.withDelta(packet.getDeltaX(), packet.getDeltaY(), packet.getDeltaZ());
                lv2.setPos(lv3);
                float f = packet.hasRotation() ? (float)(packet.getYaw() * 360) / 256.0f : lv.getLerpTargetYaw();
                float g = packet.hasRotation() ? (float)(packet.getPitch() * 360) / 256.0f : lv.getLerpTargetPitch();
                lv.updateTrackedPositionAndAngles(lv3.getX(), lv3.getY(), lv3.getZ(), f, g, 3);
            } else if (packet.hasRotation()) {
                float h = (float)(packet.getYaw() * 360) / 256.0f;
                float i = (float)(packet.getPitch() * 360) / 256.0f;
                lv.updateTrackedPositionAndAngles(lv.getLerpTargetX(), lv.getLerpTargetY(), lv.getLerpTargetZ(), h, i, 3);
            }
            lv.setOnGround(packet.isOnGround());
        }
    }

    @Override
    public void onEntitySetHeadYaw(EntitySetHeadYawS2CPacket packet) {
        NetworkThreadUtils.forceMainThread(packet, this, this.client);
        Entity lv = packet.getEntity(this.world);
        if (lv == null) {
            return;
        }
        float f = (float)(packet.getHeadYaw() * 360) / 256.0f;
        lv.updateTrackedHeadRotation(f, 3);
    }

    @Override
    public void onEntitiesDestroy(EntitiesDestroyS2CPacket packet) {
        NetworkThreadUtils.forceMainThread(packet, this, this.client);
        packet.getEntityIds().forEach(entityId -> this.world.removeEntity(entityId, Entity.RemovalReason.DISCARDED));
    }

    @Override
    public void onPlayerPositionLook(PlayerPositionLookS2CPacket packet) {
        double i;
        double h;
        double g;
        double f;
        double e;
        double d;
        NetworkThreadUtils.forceMainThread(packet, this, this.client);
        ClientPlayerEntity lv = this.client.player;
        Vec3d lv2 = lv.getVelocity();
        boolean bl = packet.getFlags().contains((Object)PositionFlag.X);
        boolean bl2 = packet.getFlags().contains((Object)PositionFlag.Y);
        boolean bl3 = packet.getFlags().contains((Object)PositionFlag.Z);
        if (bl) {
            d = lv2.getX();
            e = lv.getX() + packet.getX();
            lv.lastRenderX += packet.getX();
            lv.prevX += packet.getX();
        } else {
            d = 0.0;
            lv.lastRenderX = e = packet.getX();
            lv.prevX = e;
        }
        if (bl2) {
            f = lv2.getY();
            g = lv.getY() + packet.getY();
            lv.lastRenderY += packet.getY();
            lv.prevY += packet.getY();
        } else {
            f = 0.0;
            lv.lastRenderY = g = packet.getY();
            lv.prevY = g;
        }
        if (bl3) {
            h = lv2.getZ();
            i = lv.getZ() + packet.getZ();
            lv.lastRenderZ += packet.getZ();
            lv.prevZ += packet.getZ();
        } else {
            h = 0.0;
            lv.lastRenderZ = i = packet.getZ();
            lv.prevZ = i;
        }
        lv.setPosition(e, g, i);
        lv.setVelocity(d, f, h);
        float j = packet.getYaw();
        float k = packet.getPitch();
        if (packet.getFlags().contains((Object)PositionFlag.X_ROT)) {
            lv.setPitch(lv.getPitch() + k);
            lv.prevPitch += k;
        } else {
            lv.setPitch(k);
            lv.prevPitch = k;
        }
        if (packet.getFlags().contains((Object)PositionFlag.Y_ROT)) {
            lv.setYaw(lv.getYaw() + j);
            lv.prevYaw += j;
        } else {
            lv.setYaw(j);
            lv.prevYaw = j;
        }
        this.connection.send(new TeleportConfirmC2SPacket(packet.getTeleportId()));
        this.connection.send(new PlayerMoveC2SPacket.Full(lv.getX(), lv.getY(), lv.getZ(), lv.getYaw(), lv.getPitch(), false));
    }

    @Override
    public void onChunkDeltaUpdate(ChunkDeltaUpdateS2CPacket packet) {
        NetworkThreadUtils.forceMainThread(packet, this, this.client);
        packet.visitUpdates((pos, state) -> this.world.handleBlockUpdate((BlockPos)pos, (BlockState)state, Block.NOTIFY_ALL | Block.FORCE_STATE));
    }

    @Override
    public void onChunkData(ChunkDataS2CPacket packet) {
        NetworkThreadUtils.forceMainThread(packet, this, this.client);
        int i = packet.getChunkX();
        int j = packet.getChunkZ();
        this.loadChunk(i, j, packet.getChunkData());
        LightData lv = packet.getLightData();
        this.world.enqueueChunkUpdate(() -> {
            this.readLightData(i, j, lv);
            WorldChunk lv = this.world.getChunkManager().getWorldChunk(i, j, false);
            if (lv != null) {
                this.scheduleRenderChunk(lv, i, j);
            }
        });
    }

    @Override
    public void onChunkBiomeData(ChunkBiomeDataS2CPacket packet) {
        NetworkThreadUtils.forceMainThread(packet, this, this.client);
        for (ChunkBiomeDataS2CPacket.Serialized lv : packet.chunkBiomeData()) {
            this.world.getChunkManager().onChunkBiomeData(lv.pos().x, lv.pos().z, lv.toReadingBuf());
        }
        for (ChunkBiomeDataS2CPacket.Serialized lv : packet.chunkBiomeData()) {
            this.world.resetChunkColor(new ChunkPos(lv.pos().x, lv.pos().z));
        }
        for (ChunkBiomeDataS2CPacket.Serialized lv : packet.chunkBiomeData()) {
            for (int i = -1; i <= 1; ++i) {
                for (int j = -1; j <= 1; ++j) {
                    for (int k = this.world.getBottomSectionCoord(); k < this.world.getTopSectionCoord(); ++k) {
                        this.client.worldRenderer.scheduleBlockRender(lv.pos().x + i, k, lv.pos().z + j);
                    }
                }
            }
        }
    }

    private void loadChunk(int x, int z, ChunkData chunkData) {
        this.world.getChunkManager().loadChunkFromPacket(x, z, chunkData.getSectionsDataBuf(), chunkData.getHeightmap(), chunkData.getBlockEntities(x, z));
    }

    private void scheduleRenderChunk(WorldChunk chunk, int x, int z) {
        LightingProvider lv = this.world.getChunkManager().getLightingProvider();
        ChunkSection[] lvs = chunk.getSectionArray();
        ChunkPos lv2 = chunk.getPos();
        for (int k = 0; k < lvs.length; ++k) {
            ChunkSection lv3 = lvs[k];
            int l = this.world.sectionIndexToCoord(k);
            lv.setSectionStatus(ChunkSectionPos.from(lv2, l), lv3.isEmpty());
            this.world.scheduleBlockRenders(x, l, z);
        }
    }

    @Override
    public void onUnloadChunk(UnloadChunkS2CPacket packet) {
        NetworkThreadUtils.forceMainThread(packet, this, this.client);
        this.world.getChunkManager().unload(packet.pos());
        this.unloadChunk(packet);
    }

    private void unloadChunk(UnloadChunkS2CPacket packet) {
        ChunkPos lv = packet.pos();
        this.world.enqueueChunkUpdate(() -> {
            int i;
            LightingProvider lv = this.world.getLightingProvider();
            lv.setColumnEnabled(lv, false);
            for (i = lv.getBottomY(); i < lv.getTopY(); ++i) {
                ChunkSectionPos lv2 = ChunkSectionPos.from(lv, i);
                lv.enqueueSectionData(LightType.BLOCK, lv2, null);
                lv.enqueueSectionData(LightType.SKY, lv2, null);
            }
            for (i = this.world.getBottomSectionCoord(); i < this.world.getTopSectionCoord(); ++i) {
                lv.setSectionStatus(ChunkSectionPos.from(lv, i), true);
            }
        });
    }

    @Override
    public void onBlockUpdate(BlockUpdateS2CPacket packet) {
        NetworkThreadUtils.forceMainThread(packet, this, this.client);
        this.world.handleBlockUpdate(packet.getPos(), packet.getState(), Block.NOTIFY_ALL | Block.FORCE_STATE);
    }

    @Override
    public void onEnterReconfiguration(EnterReconfigurationS2CPacket packet) {
        NetworkThreadUtils.forceMainThread(packet, this, this.client);
        this.client.getMessageHandler().processAll();
        this.sendAcknowledgment();
        ChatHud.ChatState lv = this.client.inGameHud.getChatHud().toChatState();
        this.client.enterReconfiguration(new ReconfiguringScreen(RECONFIGURING_TEXT, this.connection));
        this.connection.transitionInbound(ConfigurationStates.S2C, new ClientConfigurationNetworkHandler(this.client, this.connection, new ClientConnectionState(this.profile, this.worldSession, this.combinedDynamicRegistries, this.enabledFeatures, this.brand, this.serverInfo, this.postDisconnectScreen, this.serverCookies, lv, this.strictErrorHandling, this.customReportDetails, this.serverLinks)));
        this.sendPacket(AcknowledgeReconfigurationC2SPacket.INSTANCE);
        this.connection.transitionOutbound(ConfigurationStates.C2S);
    }

    @Override
    public void onItemPickupAnimation(ItemPickupAnimationS2CPacket packet) {
        NetworkThreadUtils.forceMainThread(packet, this, this.client);
        Entity lv = this.world.getEntityById(packet.getEntityId());
        LivingEntity lv2 = (LivingEntity)this.world.getEntityById(packet.getCollectorEntityId());
        if (lv2 == null) {
            lv2 = this.client.player;
        }
        if (lv != null) {
            if (lv instanceof ExperienceOrbEntity) {
                this.world.playSound(lv.getX(), lv.getY(), lv.getZ(), SoundEvents.ENTITY_EXPERIENCE_ORB_PICKUP, SoundCategory.PLAYERS, 0.1f, (this.random.nextFloat() - this.random.nextFloat()) * 0.35f + 0.9f, false);
            } else {
                this.world.playSound(lv.getX(), lv.getY(), lv.getZ(), SoundEvents.ENTITY_ITEM_PICKUP, SoundCategory.PLAYERS, 0.2f, (this.random.nextFloat() - this.random.nextFloat()) * 1.4f + 2.0f, false);
            }
            this.client.particleManager.addParticle(new ItemPickupParticle(this.client.getEntityRenderDispatcher(), this.client.getBufferBuilders(), this.world, lv, lv2));
            if (lv instanceof ItemEntity) {
                ItemEntity lv3 = (ItemEntity)lv;
                ItemStack lv4 = lv3.getStack();
                if (!lv4.isEmpty()) {
                    lv4.decrement(packet.getStackAmount());
                }
                if (lv4.isEmpty()) {
                    this.world.removeEntity(packet.getEntityId(), Entity.RemovalReason.DISCARDED);
                }
            } else if (!(lv instanceof ExperienceOrbEntity)) {
                this.world.removeEntity(packet.getEntityId(), Entity.RemovalReason.DISCARDED);
            }
        }
    }

    @Override
    public void onGameMessage(GameMessageS2CPacket packet) {
        NetworkThreadUtils.forceMainThread(packet, this, this.client);
        this.client.getMessageHandler().onGameMessage(packet.content(), packet.overlay());
    }

    @Override
    public void onChatMessage(ChatMessageS2CPacket packet) {
        NetworkThreadUtils.forceMainThread(packet, this, this.client);
        Optional<MessageBody> optional = packet.body().toBody(this.signatureStorage);
        if (optional.isEmpty()) {
            this.connection.disconnect(INVALID_PACKET_TEXT);
            return;
        }
        this.signatureStorage.add(optional.get(), packet.signature());
        UUID uUID = packet.sender();
        PlayerListEntry lv = this.getPlayerListEntry(uUID);
        if (lv == null) {
            LOGGER.error("Received player chat packet for unknown player with ID: {}", (Object)uUID);
            this.client.getMessageHandler().onUnverifiedMessage(uUID, packet.serializedParameters());
            return;
        }
        PublicPlayerSession lv2 = lv.getSession();
        MessageLink lv3 = lv2 != null ? new MessageLink(packet.index(), uUID, lv2.sessionId()) : MessageLink.of(uUID);
        SignedMessage lv4 = new SignedMessage(lv3, packet.signature(), optional.get(), packet.unsignedContent(), packet.filterMask());
        lv4 = lv.getMessageVerifier().ensureVerified(lv4);
        if (lv4 != null) {
            this.client.getMessageHandler().onChatMessage(lv4, lv.getProfile(), packet.serializedParameters());
        } else {
            this.client.getMessageHandler().onUnverifiedMessage(uUID, packet.serializedParameters());
        }
    }

    @Override
    public void onProfilelessChatMessage(ProfilelessChatMessageS2CPacket packet) {
        NetworkThreadUtils.forceMainThread(packet, this, this.client);
        this.client.getMessageHandler().onProfilelessMessage(packet.message(), packet.chatType());
    }

    @Override
    public void onRemoveMessage(RemoveMessageS2CPacket packet) {
        NetworkThreadUtils.forceMainThread(packet, this, this.client);
        Optional<MessageSignatureData> optional = packet.messageSignature().getSignature(this.signatureStorage);
        if (optional.isEmpty()) {
            this.connection.disconnect(INVALID_PACKET_TEXT);
            return;
        }
        this.lastSeenMessagesCollector.remove(optional.get());
        if (!this.client.getMessageHandler().removeDelayedMessage(optional.get())) {
            this.client.inGameHud.getChatHud().removeMessage(optional.get());
        }
    }

    @Override
    public void onEntityAnimation(EntityAnimationS2CPacket packet) {
        NetworkThreadUtils.forceMainThread(packet, this, this.client);
        Entity lv = this.world.getEntityById(packet.getEntityId());
        if (lv == null) {
            return;
        }
        if (packet.getAnimationId() == 0) {
            LivingEntity lv2 = (LivingEntity)lv;
            lv2.swingHand(Hand.MAIN_HAND);
        } else if (packet.getAnimationId() == EntityAnimationS2CPacket.SWING_OFF_HAND) {
            LivingEntity lv2 = (LivingEntity)lv;
            lv2.swingHand(Hand.OFF_HAND);
        } else if (packet.getAnimationId() == EntityAnimationS2CPacket.WAKE_UP) {
            PlayerEntity lv3 = (PlayerEntity)lv;
            lv3.wakeUp(false, false);
        } else if (packet.getAnimationId() == EntityAnimationS2CPacket.CRIT) {
            this.client.particleManager.addEmitter(lv, ParticleTypes.CRIT);
        } else if (packet.getAnimationId() == EntityAnimationS2CPacket.ENCHANTED_HIT) {
            this.client.particleManager.addEmitter(lv, ParticleTypes.ENCHANTED_HIT);
        }
    }

    @Override
    public void onDamageTilt(DamageTiltS2CPacket packet) {
        NetworkThreadUtils.forceMainThread(packet, this, this.client);
        Entity lv = this.world.getEntityById(packet.id());
        if (lv == null) {
            return;
        }
        lv.animateDamage(packet.yaw());
    }

    @Override
    public void onWorldTimeUpdate(WorldTimeUpdateS2CPacket packet) {
        NetworkThreadUtils.forceMainThread(packet, this, this.client);
        this.client.world.setTime(packet.getTime());
        this.client.world.setTimeOfDay(packet.getTimeOfDay());
        this.worldSession.setTick(packet.getTime());
    }

    @Override
    public void onPlayerSpawnPosition(PlayerSpawnPositionS2CPacket packet) {
        NetworkThreadUtils.forceMainThread(packet, this, this.client);
        this.client.world.setSpawnPos(packet.getPos(), packet.getAngle());
    }

    @Override
    public void onEntityPassengersSet(EntityPassengersSetS2CPacket packet) {
        NetworkThreadUtils.forceMainThread(packet, this, this.client);
        Entity lv = this.world.getEntityById(packet.getEntityId());
        if (lv == null) {
            LOGGER.warn("Received passengers for unknown entity");
            return;
        }
        boolean bl = lv.hasPassengerDeep(this.client.player);
        lv.removeAllPassengers();
        for (int i : packet.getPassengerIds()) {
            Entity lv2 = this.world.getEntityById(i);
            if (lv2 == null) continue;
            lv2.startRiding(lv, true);
            if (lv2 != this.client.player || bl) continue;
            if (lv instanceof BoatEntity) {
                this.client.player.prevYaw = lv.getYaw();
                this.client.player.setYaw(lv.getYaw());
                this.client.player.setHeadYaw(lv.getYaw());
            }
            MutableText lv3 = Text.translatable("mount.onboard", this.client.options.sneakKey.getBoundKeyLocalizedText());
            this.client.inGameHud.setOverlayMessage(lv3, false);
            this.client.getNarratorManager().narrate(lv3);
        }
    }

    @Override
    public void onEntityAttach(EntityAttachS2CPacket packet) {
        NetworkThreadUtils.forceMainThread(packet, this, this.client);
        Entity lv = this.world.getEntityById(packet.getAttachedEntityId());
        if (lv instanceof Leashable) {
            Leashable lv2 = (Leashable)((Object)lv);
            lv2.setUnresolvedLeashHolderId(packet.getHoldingEntityId());
        }
    }

    private static ItemStack getActiveTotemOfUndying(PlayerEntity player) {
        for (Hand lv : Hand.values()) {
            ItemStack lv2 = player.getStackInHand(lv);
            if (!lv2.isOf(Items.TOTEM_OF_UNDYING)) continue;
            return lv2;
        }
        return new ItemStack(Items.TOTEM_OF_UNDYING);
    }

    @Override
    public void onEntityStatus(EntityStatusS2CPacket packet) {
        NetworkThreadUtils.forceMainThread(packet, this, this.client);
        Entity lv = packet.getEntity(this.world);
        if (lv != null) {
            switch (packet.getStatus()) {
                case 63: {
                    this.client.getSoundManager().play(new SnifferDigSoundInstance((SnifferEntity)lv));
                    break;
                }
                case 21: {
                    this.client.getSoundManager().play(new GuardianAttackSoundInstance((GuardianEntity)lv));
                    break;
                }
                case 35: {
                    int i = 40;
                    this.client.particleManager.addEmitter(lv, ParticleTypes.TOTEM_OF_UNDYING, 30);
                    this.world.playSound(lv.getX(), lv.getY(), lv.getZ(), SoundEvents.ITEM_TOTEM_USE, lv.getSoundCategory(), 1.0f, 1.0f, false);
                    if (lv != this.client.player) break;
                    this.client.gameRenderer.showFloatingItem(ClientPlayNetworkHandler.getActiveTotemOfUndying(this.client.player));
                    break;
                }
                default: {
                    lv.handleStatus(packet.getStatus());
                }
            }
        }
    }

    @Override
    public void onEntityDamage(EntityDamageS2CPacket packet) {
        NetworkThreadUtils.forceMainThread(packet, this, this.client);
        Entity lv = this.world.getEntityById(packet.entityId());
        if (lv == null) {
            return;
        }
        lv.onDamaged(packet.createDamageSource(this.world));
    }

    @Override
    public void onHealthUpdate(HealthUpdateS2CPacket packet) {
        NetworkThreadUtils.forceMainThread(packet, this, this.client);
        this.client.player.updateHealth(packet.getHealth());
        this.client.player.getHungerManager().setFoodLevel(packet.getFood());
        this.client.player.getHungerManager().setSaturationLevel(packet.getSaturation());
    }

    @Override
    public void onExperienceBarUpdate(ExperienceBarUpdateS2CPacket packet) {
        NetworkThreadUtils.forceMainThread(packet, this, this.client);
        this.client.player.setExperience(packet.getBarProgress(), packet.getExperienceLevel(), packet.getExperience());
    }

    @Override
    public void onPlayerRespawn(PlayerRespawnS2CPacket packet) {
        List<DataTracker.SerializedEntry<?>> list;
        NetworkThreadUtils.forceMainThread(packet, this, this.client);
        CommonPlayerSpawnInfo lv = packet.commonPlayerSpawnInfo();
        RegistryKey<World> lv2 = lv.dimension();
        RegistryEntry<DimensionType> lv3 = lv.dimensionType();
        ClientPlayerEntity lv4 = this.client.player;
        RegistryKey<World> lv5 = lv4.getWorld().getRegistryKey();
        boolean bl = lv2 != lv5;
        DownloadingTerrainScreen.WorldEntryReason lv6 = this.getWorldEntryReason(lv4.isDead(), lv2, lv5);
        if (bl) {
            ClientWorld.Properties lv7;
            Map<MapIdComponent, MapState> map = this.world.getMapStates();
            boolean bl2 = lv.isDebug();
            boolean bl3 = lv.isFlat();
            this.worldProperties = lv7 = new ClientWorld.Properties(this.worldProperties.getDifficulty(), this.worldProperties.isHardcore(), bl3);
            this.world = new ClientWorld(this, lv7, lv2, lv3, this.chunkLoadDistance, this.simulationDistance, this.client::getProfiler, this.client.worldRenderer, bl2, lv.seed());
            this.world.putMapStates(map);
            this.client.joinWorld(this.world, lv6);
        }
        this.client.cameraEntity = null;
        if (lv4.shouldCloseHandledScreenOnRespawn()) {
            lv4.closeHandledScreen();
        }
        ClientPlayerEntity lv8 = packet.hasFlag(PlayerRespawnS2CPacket.KEEP_TRACKED_DATA) ? this.client.interactionManager.createPlayer(this.world, lv4.getStatHandler(), lv4.getRecipeBook(), lv4.isSneaking(), lv4.isSprinting()) : this.client.interactionManager.createPlayer(this.world, lv4.getStatHandler(), lv4.getRecipeBook());
        this.startWorldLoading(lv8, this.world, lv6);
        lv8.setId(lv4.getId());
        this.client.player = lv8;
        if (bl) {
            this.client.getMusicTracker().stop();
        }
        this.client.cameraEntity = lv8;
        if (packet.hasFlag(PlayerRespawnS2CPacket.KEEP_TRACKED_DATA) && (list = lv4.getDataTracker().getChangedEntries()) != null) {
            lv8.getDataTracker().writeUpdatedEntries(list);
        }
        if (packet.hasFlag(PlayerRespawnS2CPacket.KEEP_ATTRIBUTES)) {
            lv8.getAttributes().setFrom(lv4.getAttributes());
        } else {
            lv8.getAttributes().setBaseFrom(lv4.getAttributes());
        }
        lv8.init();
        this.world.addEntity(lv8);
        lv8.setYaw(-180.0f);
        lv8.input = new KeyboardInput(this.client.options);
        this.client.interactionManager.copyAbilities(lv8);
        lv8.setReducedDebugInfo(lv4.hasReducedDebugInfo());
        lv8.setShowsDeathScreen(lv4.showsDeathScreen());
        lv8.setLastDeathPos(lv.lastDeathLocation());
        lv8.setPortalCooldown(lv.portalCooldown());
        lv8.nauseaIntensity = lv4.nauseaIntensity;
        lv8.prevNauseaIntensity = lv4.prevNauseaIntensity;
        if (this.client.currentScreen instanceof DeathScreen || this.client.currentScreen instanceof DeathScreen.TitleScreenConfirmScreen) {
            this.client.setScreen(null);
        }
        this.client.interactionManager.setGameModes(lv.gameMode(), lv.prevGameMode());
    }

    private DownloadingTerrainScreen.WorldEntryReason getWorldEntryReason(boolean dead, RegistryKey<World> from, RegistryKey<World> to) {
        DownloadingTerrainScreen.WorldEntryReason lv = DownloadingTerrainScreen.WorldEntryReason.OTHER;
        if (!dead) {
            if (from == World.NETHER || to == World.NETHER) {
                lv = DownloadingTerrainScreen.WorldEntryReason.NETHER_PORTAL;
            } else if (from == World.END || to == World.END) {
                lv = DownloadingTerrainScreen.WorldEntryReason.END_PORTAL;
            }
        }
        return lv;
    }

    @Override
    public void onExplosion(ExplosionS2CPacket packet) {
        NetworkThreadUtils.forceMainThread(packet, this, this.client);
        Explosion lv = new Explosion(this.client.world, null, packet.getX(), packet.getY(), packet.getZ(), packet.getRadius(), packet.getAffectedBlocks(), packet.getDestructionType(), packet.getParticle(), packet.getEmitterParticle(), packet.getSoundEvent());
        lv.affectWorld(true);
        this.client.player.setVelocity(this.client.player.getVelocity().add(packet.getPlayerVelocityX(), packet.getPlayerVelocityY(), packet.getPlayerVelocityZ()));
    }

    @Override
    public void onOpenHorseScreen(OpenHorseScreenS2CPacket packet) {
        NetworkThreadUtils.forceMainThread(packet, this, this.client);
        Entity lv = this.world.getEntityById(packet.getHorseId());
        if (lv instanceof AbstractHorseEntity) {
            AbstractHorseEntity lv2 = (AbstractHorseEntity)lv;
            ClientPlayerEntity lv3 = this.client.player;
            int i = packet.getSlotColumnCount();
            SimpleInventory lv4 = new SimpleInventory(AbstractHorseEntity.getInventorySize(i));
            HorseScreenHandler lv5 = new HorseScreenHandler(packet.getSyncId(), lv3.getInventory(), lv4, lv2, i);
            lv3.currentScreenHandler = lv5;
            this.client.setScreen(new HorseScreen(lv5, lv3.getInventory(), lv2, i));
        }
    }

    @Override
    public void onOpenScreen(OpenScreenS2CPacket packet) {
        NetworkThreadUtils.forceMainThread(packet, this, this.client);
        HandledScreens.open(packet.getScreenHandlerType(), this.client, packet.getSyncId(), packet.getName());
    }

    @Override
    public void onScreenHandlerSlotUpdate(ScreenHandlerSlotUpdateS2CPacket packet) {
        NetworkThreadUtils.forceMainThread(packet, this, this.client);
        ClientPlayerEntity lv = this.client.player;
        ItemStack lv2 = packet.getStack();
        int i = packet.getSlot();
        this.client.getTutorialManager().onSlotUpdate(lv2);
        if (packet.getSyncId() == ScreenHandlerSlotUpdateS2CPacket.UPDATE_CURSOR_SYNC_ID) {
            if (!(this.client.currentScreen instanceof CreativeInventoryScreen)) {
                lv.currentScreenHandler.setCursorStack(lv2);
            }
        } else if (packet.getSyncId() == ScreenHandlerSlotUpdateS2CPacket.UPDATE_PLAYER_INVENTORY_SYNC_ID) {
            lv.getInventory().setStack(i, lv2);
        } else {
            boolean bl = false;
            Screen screen = this.client.currentScreen;
            if (screen instanceof CreativeInventoryScreen) {
                CreativeInventoryScreen lv3 = (CreativeInventoryScreen)screen;
                boolean bl2 = bl = !lv3.isInventoryTabSelected();
            }
            if (packet.getSyncId() == 0 && PlayerScreenHandler.isInHotbar(i)) {
                ItemStack lv4;
                if (!lv2.isEmpty() && ((lv4 = lv.playerScreenHandler.getSlot(i).getStack()).isEmpty() || lv4.getCount() < lv2.getCount())) {
                    lv2.setBobbingAnimationTime(5);
                }
                lv.playerScreenHandler.setStackInSlot(i, packet.getRevision(), lv2);
            } else if (!(packet.getSyncId() != lv.currentScreenHandler.syncId || packet.getSyncId() == 0 && bl)) {
                lv.currentScreenHandler.setStackInSlot(i, packet.getRevision(), lv2);
            }
        }
    }

    @Override
    public void onInventory(InventoryS2CPacket packet) {
        NetworkThreadUtils.forceMainThread(packet, this, this.client);
        ClientPlayerEntity lv = this.client.player;
        if (packet.getSyncId() == 0) {
            lv.playerScreenHandler.updateSlotStacks(packet.getRevision(), packet.getContents(), packet.getCursorStack());
        } else if (packet.getSyncId() == lv.currentScreenHandler.syncId) {
            lv.currentScreenHandler.updateSlotStacks(packet.getRevision(), packet.getContents(), packet.getCursorStack());
        }
    }

    @Override
    public void onSignEditorOpen(SignEditorOpenS2CPacket packet) {
        NetworkThreadUtils.forceMainThread(packet, this, this.client);
        BlockPos lv = packet.getPos();
        BlockEntity blockEntity = this.world.getBlockEntity(lv);
        if (blockEntity instanceof SignBlockEntity) {
            SignBlockEntity lv2 = (SignBlockEntity)blockEntity;
            this.client.player.openEditSignScreen(lv2, packet.isFront());
        } else {
            BlockState lv3 = this.world.getBlockState(lv);
            SignBlockEntity lv4 = new SignBlockEntity(lv, lv3);
            lv4.setWorld(this.world);
            this.client.player.openEditSignScreen(lv4, packet.isFront());
        }
    }

    @Override
    public void onBlockEntityUpdate(BlockEntityUpdateS2CPacket packet) {
        NetworkThreadUtils.forceMainThread(packet, this, this.client);
        BlockPos lv = packet.getPos();
        this.client.world.getBlockEntity(lv, packet.getBlockEntityType()).ifPresent(blockEntity -> {
            NbtCompound lv = packet.getNbt();
            if (!lv.isEmpty()) {
                blockEntity.read(lv, this.combinedDynamicRegistries);
            }
            if (blockEntity instanceof CommandBlockBlockEntity && this.client.currentScreen instanceof CommandBlockScreen) {
                ((CommandBlockScreen)this.client.currentScreen).updateCommandBlock();
            }
        });
    }

    @Override
    public void onScreenHandlerPropertyUpdate(ScreenHandlerPropertyUpdateS2CPacket packet) {
        NetworkThreadUtils.forceMainThread(packet, this, this.client);
        ClientPlayerEntity lv = this.client.player;
        if (lv.currentScreenHandler != null && lv.currentScreenHandler.syncId == packet.getSyncId()) {
            lv.currentScreenHandler.setProperty(packet.getPropertyId(), packet.getValue());
        }
    }

    @Override
    public void onEntityEquipmentUpdate(EntityEquipmentUpdateS2CPacket packet) {
        NetworkThreadUtils.forceMainThread(packet, this, this.client);
        Entity lv = this.world.getEntityById(packet.getEntityId());
        if (lv instanceof LivingEntity) {
            LivingEntity lv2 = (LivingEntity)lv;
            packet.getEquipmentList().forEach(pair -> lv2.equipStack((EquipmentSlot)pair.getFirst(), (ItemStack)pair.getSecond()));
        }
    }

    @Override
    public void onCloseScreen(CloseScreenS2CPacket packet) {
        NetworkThreadUtils.forceMainThread(packet, this, this.client);
        this.client.player.closeScreen();
    }

    @Override
    public void onBlockEvent(BlockEventS2CPacket packet) {
        NetworkThreadUtils.forceMainThread(packet, this, this.client);
        this.client.world.addSyncedBlockEvent(packet.getPos(), packet.getBlock(), packet.getType(), packet.getData());
    }

    @Override
    public void onBlockBreakingProgress(BlockBreakingProgressS2CPacket packet) {
        NetworkThreadUtils.forceMainThread(packet, this, this.client);
        this.client.world.setBlockBreakingInfo(packet.getEntityId(), packet.getPos(), packet.getProgress());
    }

    @Override
    public void onGameStateChange(GameStateChangeS2CPacket packet) {
        NetworkThreadUtils.forceMainThread(packet, this, this.client);
        ClientPlayerEntity lv = this.client.player;
        GameStateChangeS2CPacket.Reason lv2 = packet.getReason();
        float f = packet.getValue();
        int i = MathHelper.floor(f + 0.5f);
        if (lv2 == GameStateChangeS2CPacket.NO_RESPAWN_BLOCK) {
            ((PlayerEntity)lv).sendMessage(Text.translatable("block.minecraft.spawn.not_valid"), false);
        } else if (lv2 == GameStateChangeS2CPacket.RAIN_STARTED) {
            this.world.getLevelProperties().setRaining(true);
            this.world.setRainGradient(0.0f);
        } else if (lv2 == GameStateChangeS2CPacket.RAIN_STOPPED) {
            this.world.getLevelProperties().setRaining(false);
            this.world.setRainGradient(1.0f);
        } else if (lv2 == GameStateChangeS2CPacket.GAME_MODE_CHANGED) {
            this.client.interactionManager.setGameMode(GameMode.byId(i));
        } else if (lv2 == GameStateChangeS2CPacket.GAME_WON) {
            this.client.setScreen(new CreditsScreen(true, () -> {
                this.client.player.networkHandler.sendPacket(new ClientStatusC2SPacket(ClientStatusC2SPacket.Mode.PERFORM_RESPAWN));
                this.client.setScreen(null);
            }));
        } else if (lv2 == GameStateChangeS2CPacket.DEMO_MESSAGE_SHOWN) {
            GameOptions lv3 = this.client.options;
            if (f == GameStateChangeS2CPacket.field_33328) {
                this.client.setScreen(new DemoScreen());
            } else if (f == GameStateChangeS2CPacket.field_33329) {
                this.client.inGameHud.getChatHud().addMessage(Text.translatable("demo.help.movement", lv3.forwardKey.getBoundKeyLocalizedText(), lv3.leftKey.getBoundKeyLocalizedText(), lv3.backKey.getBoundKeyLocalizedText(), lv3.rightKey.getBoundKeyLocalizedText()));
            } else if (f == GameStateChangeS2CPacket.field_33330) {
                this.client.inGameHud.getChatHud().addMessage(Text.translatable("demo.help.jump", lv3.jumpKey.getBoundKeyLocalizedText()));
            } else if (f == GameStateChangeS2CPacket.field_33331) {
                this.client.inGameHud.getChatHud().addMessage(Text.translatable("demo.help.inventory", lv3.inventoryKey.getBoundKeyLocalizedText()));
            } else if (f == GameStateChangeS2CPacket.field_33332) {
                this.client.inGameHud.getChatHud().addMessage(Text.translatable("demo.day.6", lv3.screenshotKey.getBoundKeyLocalizedText()));
            }
        } else if (lv2 == GameStateChangeS2CPacket.PROJECTILE_HIT_PLAYER) {
            this.world.playSound((PlayerEntity)lv, lv.getX(), lv.getEyeY(), lv.getZ(), SoundEvents.ENTITY_ARROW_HIT_PLAYER, SoundCategory.PLAYERS, 0.18f, 0.45f);
        } else if (lv2 == GameStateChangeS2CPacket.RAIN_GRADIENT_CHANGED) {
            this.world.setRainGradient(f);
        } else if (lv2 == GameStateChangeS2CPacket.THUNDER_GRADIENT_CHANGED) {
            this.world.setThunderGradient(f);
        } else if (lv2 == GameStateChangeS2CPacket.PUFFERFISH_STING) {
            this.world.playSound((PlayerEntity)lv, lv.getX(), lv.getY(), lv.getZ(), SoundEvents.ENTITY_PUFFER_FISH_STING, SoundCategory.NEUTRAL, 1.0f, 1.0f);
        } else if (lv2 == GameStateChangeS2CPacket.ELDER_GUARDIAN_EFFECT) {
            this.world.addParticle(ParticleTypes.ELDER_GUARDIAN, lv.getX(), lv.getY(), lv.getZ(), 0.0, 0.0, 0.0);
            if (i == 1) {
                this.world.playSound((PlayerEntity)lv, lv.getX(), lv.getY(), lv.getZ(), SoundEvents.ENTITY_ELDER_GUARDIAN_CURSE, SoundCategory.HOSTILE, 1.0f, 1.0f);
            }
        } else if (lv2 == GameStateChangeS2CPacket.IMMEDIATE_RESPAWN) {
            this.client.player.setShowsDeathScreen(f == GameStateChangeS2CPacket.field_33328);
        } else if (lv2 == GameStateChangeS2CPacket.LIMITED_CRAFTING_TOGGLED) {
            this.client.player.setLimitedCraftingEnabled(f == 1.0f);
        } else if (lv2 == GameStateChangeS2CPacket.INITIAL_CHUNKS_COMING && this.worldLoadingState != null) {
            this.worldLoadingState.handleChunksComingPacket();
        }
    }

    private void startWorldLoading(ClientPlayerEntity player, ClientWorld world, DownloadingTerrainScreen.WorldEntryReason worldEntryReason) {
        this.worldLoadingState = new WorldLoadingState(player, world, this.client.worldRenderer);
        this.client.setScreen(new DownloadingTerrainScreen(this.worldLoadingState::isReady, worldEntryReason));
    }

    @Override
    public void onMapUpdate(MapUpdateS2CPacket packet) {
        NetworkThreadUtils.forceMainThread(packet, this, this.client);
        MapRenderer lv = this.client.gameRenderer.getMapRenderer();
        MapIdComponent lv2 = packet.mapId();
        MapState lv3 = this.client.world.getMapState(lv2);
        if (lv3 == null) {
            lv3 = MapState.of(packet.scale(), packet.locked(), this.client.world.getRegistryKey());
            this.client.world.putClientsideMapState(lv2, lv3);
        }
        packet.apply(lv3);
        lv.updateTexture(lv2, lv3);
    }

    @Override
    public void onWorldEvent(WorldEventS2CPacket packet) {
        NetworkThreadUtils.forceMainThread(packet, this, this.client);
        if (packet.isGlobal()) {
            this.client.world.syncGlobalEvent(packet.getEventId(), packet.getPos(), packet.getData());
        } else {
            this.client.world.syncWorldEvent(packet.getEventId(), packet.getPos(), packet.getData());
        }
    }

    @Override
    public void onAdvancements(AdvancementUpdateS2CPacket packet) {
        NetworkThreadUtils.forceMainThread(packet, this, this.client);
        this.advancementHandler.onAdvancements(packet);
    }

    @Override
    public void onSelectAdvancementTab(SelectAdvancementTabS2CPacket packet) {
        NetworkThreadUtils.forceMainThread(packet, this, this.client);
        Identifier lv = packet.getTabId();
        if (lv == null) {
            this.advancementHandler.selectTab(null, false);
        } else {
            AdvancementEntry lv2 = this.advancementHandler.get(lv);
            this.advancementHandler.selectTab(lv2, false);
        }
    }

    @Override
    public void onCommandTree(CommandTreeS2CPacket packet) {
        NetworkThreadUtils.forceMainThread(packet, this, this.client);
        this.commandDispatcher = new CommandDispatcher<CommandSource>(packet.getCommandTree(CommandRegistryAccess.of(this.combinedDynamicRegistries, this.enabledFeatures)));
    }

    @Override
    public void onStopSound(StopSoundS2CPacket packet) {
        NetworkThreadUtils.forceMainThread(packet, this, this.client);
        this.client.getSoundManager().stopSounds(packet.getSoundId(), packet.getCategory());
    }

    @Override
    public void onCommandSuggestions(CommandSuggestionsS2CPacket packet) {
        NetworkThreadUtils.forceMainThread(packet, this, this.client);
        this.commandSource.onCommandSuggestions(packet.id(), packet.getSuggestions());
    }

    @Override
    public void onSynchronizeRecipes(SynchronizeRecipesS2CPacket packet) {
        NetworkThreadUtils.forceMainThread(packet, this, this.client);
        this.recipeManager.setRecipes(packet.getRecipes());
        ClientRecipeBook lv = this.client.player.getRecipeBook();
        lv.reload(this.recipeManager.sortedValues(), this.client.world.getRegistryManager());
        this.searchManager.addRecipeOutputReloader(lv, this.combinedDynamicRegistries);
    }

    @Override
    public void onLookAt(LookAtS2CPacket packet) {
        NetworkThreadUtils.forceMainThread(packet, this, this.client);
        Vec3d lv = packet.getTargetPosition(this.world);
        if (lv != null) {
            this.client.player.lookAt(packet.getSelfAnchor(), lv);
        }
    }

    @Override
    public void onNbtQueryResponse(NbtQueryResponseS2CPacket packet) {
        NetworkThreadUtils.forceMainThread(packet, this, this.client);
        if (!this.dataQueryHandler.handleQueryResponse(packet.getTransactionId(), packet.getNbt())) {
            LOGGER.debug("Got unhandled response to tag query {}", (Object)packet.getTransactionId());
        }
    }

    @Override
    public void onStatistics(StatisticsS2CPacket packet) {
        NetworkThreadUtils.forceMainThread(packet, this, this.client);
        for (Object2IntMap.Entry screen : packet.stats().object2IntEntrySet()) {
            Stat lv = (Stat)screen.getKey();
            int i = screen.getIntValue();
            this.client.player.getStatHandler().setStat(this.client.player, lv, i);
        }
        Screen screen = this.client.currentScreen;
        if (screen instanceof StatsScreen) {
            StatsScreen lv2 = (StatsScreen)screen;
            lv2.onStatsReady();
        }
    }

    @Override
    public void onUnlockRecipes(ChangeUnlockedRecipesS2CPacket packet) {
        NetworkThreadUtils.forceMainThread(packet, this, this.client);
        ClientRecipeBook lv = this.client.player.getRecipeBook();
        lv.setOptions(packet.getOptions());
        ChangeUnlockedRecipesS2CPacket.Action lv2 = packet.getAction();
        switch (lv2) {
            case REMOVE: {
                for (Identifier lv3 : packet.getRecipeIdsToChange()) {
                    this.recipeManager.get(lv3).ifPresent(lv::remove);
                }
                break;
            }
            case INIT: {
                for (Identifier lv3 : packet.getRecipeIdsToChange()) {
                    this.recipeManager.get(lv3).ifPresent(lv::add);
                }
                for (Identifier lv3 : packet.getRecipeIdsToInit()) {
                    this.recipeManager.get(lv3).ifPresent(lv::display);
                }
                break;
            }
            case ADD: {
                for (Identifier lv3 : packet.getRecipeIdsToChange()) {
                    this.recipeManager.get(lv3).ifPresent(recipe -> {
                        lv.add((RecipeEntry<?>)recipe);
                        lv.display((RecipeEntry<?>)recipe);
                        if (recipe.value().showNotification()) {
                            RecipeToast.show(this.client.getToastManager(), recipe);
                        }
                    });
                }
                break;
            }
        }
        lv.getOrderedResults().forEach(recipeResultCollection -> recipeResultCollection.initialize(lv));
        if (this.client.currentScreen instanceof RecipeBookProvider) {
            ((RecipeBookProvider)((Object)this.client.currentScreen)).refreshRecipeBook();
        }
    }

    @Override
    public void onEntityStatusEffect(EntityStatusEffectS2CPacket packet) {
        NetworkThreadUtils.forceMainThread(packet, this, this.client);
        Entity lv = this.world.getEntityById(packet.getEntityId());
        if (!(lv instanceof LivingEntity)) {
            return;
        }
        RegistryEntry<StatusEffect> lv2 = packet.getEffectId();
        StatusEffectInstance lv3 = new StatusEffectInstance(lv2, packet.getDuration(), packet.getAmplifier(), packet.isAmbient(), packet.shouldShowParticles(), packet.shouldShowIcon(), null);
        if (!packet.keepFading()) {
            lv3.skipFading();
        }
        ((LivingEntity)lv).setStatusEffect(lv3, null);
    }

    @Override
    public void onSynchronizeTags(SynchronizeTagsS2CPacket packet) {
        NetworkThreadUtils.forceMainThread(packet, this, this.client);
        ClientTagLoader lv = new ClientTagLoader();
        packet.getGroups().forEach(lv::put);
        lv.load((DynamicRegistryManager)this.combinedDynamicRegistries, this.connection.isLocal());
        List<ItemStack> list = List.copyOf(ItemGroups.getSearchGroup().getDisplayStacks());
        this.searchManager.addItemTagReloader(list);
    }

    @Override
    public void onEndCombat(EndCombatS2CPacket packet) {
    }

    @Override
    public void onEnterCombat(EnterCombatS2CPacket packet) {
    }

    @Override
    public void onDeathMessage(DeathMessageS2CPacket packet) {
        NetworkThreadUtils.forceMainThread(packet, this, this.client);
        Entity lv = this.world.getEntityById(packet.playerId());
        if (lv == this.client.player) {
            if (this.client.player.showsDeathScreen()) {
                this.client.setScreen(new DeathScreen(packet.message(), this.world.getLevelProperties().isHardcore()));
            } else {
                this.client.player.requestRespawn();
            }
        }
    }

    @Override
    public void onDifficulty(DifficultyS2CPacket packet) {
        NetworkThreadUtils.forceMainThread(packet, this, this.client);
        this.worldProperties.setDifficulty(packet.getDifficulty());
        this.worldProperties.setDifficultyLocked(packet.isDifficultyLocked());
    }

    @Override
    public void onSetCameraEntity(SetCameraEntityS2CPacket packet) {
        NetworkThreadUtils.forceMainThread(packet, this, this.client);
        Entity lv = packet.getEntity(this.world);
        if (lv != null) {
            this.client.setCameraEntity(lv);
        }
    }

    @Override
    public void onWorldBorderInitialize(WorldBorderInitializeS2CPacket packet) {
        NetworkThreadUtils.forceMainThread(packet, this, this.client);
        WorldBorder lv = this.world.getWorldBorder();
        lv.setCenter(packet.getCenterX(), packet.getCenterZ());
        long l = packet.getSizeLerpTime();
        if (l > 0L) {
            lv.interpolateSize(packet.getSize(), packet.getSizeLerpTarget(), l);
        } else {
            lv.setSize(packet.getSizeLerpTarget());
        }
        lv.setMaxRadius(packet.getMaxRadius());
        lv.setWarningBlocks(packet.getWarningBlocks());
        lv.setWarningTime(packet.getWarningTime());
    }

    @Override
    public void onWorldBorderCenterChanged(WorldBorderCenterChangedS2CPacket packet) {
        NetworkThreadUtils.forceMainThread(packet, this, this.client);
        this.world.getWorldBorder().setCenter(packet.getCenterX(), packet.getCenterZ());
    }

    @Override
    public void onWorldBorderInterpolateSize(WorldBorderInterpolateSizeS2CPacket packet) {
        NetworkThreadUtils.forceMainThread(packet, this, this.client);
        this.world.getWorldBorder().interpolateSize(packet.getSize(), packet.getSizeLerpTarget(), packet.getSizeLerpTime());
    }

    @Override
    public void onWorldBorderSizeChanged(WorldBorderSizeChangedS2CPacket packet) {
        NetworkThreadUtils.forceMainThread(packet, this, this.client);
        this.world.getWorldBorder().setSize(packet.getSizeLerpTarget());
    }

    @Override
    public void onWorldBorderWarningBlocksChanged(WorldBorderWarningBlocksChangedS2CPacket packet) {
        NetworkThreadUtils.forceMainThread(packet, this, this.client);
        this.world.getWorldBorder().setWarningBlocks(packet.getWarningBlocks());
    }

    @Override
    public void onWorldBorderWarningTimeChanged(WorldBorderWarningTimeChangedS2CPacket packet) {
        NetworkThreadUtils.forceMainThread(packet, this, this.client);
        this.world.getWorldBorder().setWarningTime(packet.getWarningTime());
    }

    @Override
    public void onTitleClear(ClearTitleS2CPacket packet) {
        NetworkThreadUtils.forceMainThread(packet, this, this.client);
        this.client.inGameHud.clearTitle();
        if (packet.shouldReset()) {
            this.client.inGameHud.setDefaultTitleFade();
        }
    }

    @Override
    public void onServerMetadata(ServerMetadataS2CPacket packet) {
        NetworkThreadUtils.forceMainThread(packet, this, this.client);
        if (this.serverInfo == null) {
            return;
        }
        this.serverInfo.label = packet.description();
        packet.favicon().map(ServerInfo::validateFavicon).ifPresent(this.serverInfo::setFavicon);
        ServerList.updateServerListEntry(this.serverInfo);
    }

    @Override
    public void onChatSuggestions(ChatSuggestionsS2CPacket packet) {
        NetworkThreadUtils.forceMainThread(packet, this, this.client);
        this.commandSource.onChatSuggestions(packet.action(), packet.entries());
    }

    @Override
    public void onOverlayMessage(OverlayMessageS2CPacket packet) {
        NetworkThreadUtils.forceMainThread(packet, this, this.client);
        this.client.inGameHud.setOverlayMessage(packet.text(), false);
    }

    @Override
    public void onTitle(TitleS2CPacket packet) {
        NetworkThreadUtils.forceMainThread(packet, this, this.client);
        this.client.inGameHud.setTitle(packet.text());
    }

    @Override
    public void onSubtitle(SubtitleS2CPacket packet) {
        NetworkThreadUtils.forceMainThread(packet, this, this.client);
        this.client.inGameHud.setSubtitle(packet.text());
    }

    @Override
    public void onTitleFade(TitleFadeS2CPacket packet) {
        NetworkThreadUtils.forceMainThread(packet, this, this.client);
        this.client.inGameHud.setTitleTicks(packet.getFadeInTicks(), packet.getStayTicks(), packet.getFadeOutTicks());
    }

    @Override
    public void onPlayerListHeader(PlayerListHeaderS2CPacket packet) {
        NetworkThreadUtils.forceMainThread(packet, this, this.client);
        this.client.inGameHud.getPlayerListHud().setHeader(packet.header().getString().isEmpty() ? null : packet.header());
        this.client.inGameHud.getPlayerListHud().setFooter(packet.footer().getString().isEmpty() ? null : packet.footer());
    }

    @Override
    public void onRemoveEntityStatusEffect(RemoveEntityStatusEffectS2CPacket packet) {
        NetworkThreadUtils.forceMainThread(packet, this, this.client);
        Entity entity = packet.getEntity(this.world);
        if (entity instanceof LivingEntity) {
            LivingEntity lv = (LivingEntity)entity;
            lv.removeStatusEffectInternal(packet.effect());
        }
    }

    @Override
    public void onPlayerRemove(PlayerRemoveS2CPacket packet) {
        NetworkThreadUtils.forceMainThread(packet, this, this.client);
        for (UUID uUID : packet.profileIds()) {
            this.client.getSocialInteractionsManager().setPlayerOffline(uUID);
            PlayerListEntry lv = this.playerListEntries.remove(uUID);
            if (lv == null) continue;
            this.listedPlayerListEntries.remove(lv);
        }
    }

    @Override
    public void onPlayerList(PlayerListS2CPacket packet) {
        PlayerListEntry lv2;
        NetworkThreadUtils.forceMainThread(packet, this, this.client);
        for (PlayerListS2CPacket.Entry lv : packet.getPlayerAdditionEntries()) {
            lv2 = new PlayerListEntry(Objects.requireNonNull(lv.profile()), this.isSecureChatEnforced());
            if (this.playerListEntries.putIfAbsent(lv.profileId(), lv2) != null) continue;
            this.client.getSocialInteractionsManager().setPlayerOnline(lv2);
        }
        for (PlayerListS2CPacket.Entry lv : packet.getEntries()) {
            lv2 = this.playerListEntries.get(lv.profileId());
            if (lv2 == null) {
                LOGGER.warn("Ignoring player info update for unknown player {} ({})", (Object)lv.profileId(), (Object)packet.getActions());
                continue;
            }
            for (PlayerListS2CPacket.Action lv3 : packet.getActions()) {
                this.handlePlayerListAction(lv3, lv, lv2);
            }
        }
    }

    private void handlePlayerListAction(PlayerListS2CPacket.Action action, PlayerListS2CPacket.Entry receivedEntry, PlayerListEntry currentEntry) {
        switch (action) {
            case INITIALIZE_CHAT: {
                this.setPublicSession(receivedEntry, currentEntry);
                break;
            }
            case UPDATE_GAME_MODE: {
                if (currentEntry.getGameMode() != receivedEntry.gameMode() && this.client.player != null && this.client.player.getUuid().equals(receivedEntry.profileId())) {
                    this.client.player.onGameModeChanged(receivedEntry.gameMode());
                }
                currentEntry.setGameMode(receivedEntry.gameMode());
                break;
            }
            case UPDATE_LISTED: {
                if (receivedEntry.listed()) {
                    this.listedPlayerListEntries.add(currentEntry);
                    break;
                }
                this.listedPlayerListEntries.remove(currentEntry);
                break;
            }
            case UPDATE_LATENCY: {
                currentEntry.setLatency(receivedEntry.latency());
                break;
            }
            case UPDATE_DISPLAY_NAME: {
                currentEntry.setDisplayName(receivedEntry.displayName());
            }
        }
    }

    private void setPublicSession(PlayerListS2CPacket.Entry receivedEntry, PlayerListEntry currentEntry) {
        GameProfile gameProfile = currentEntry.getProfile();
        SignatureVerifier lv = this.client.getServicesSignatureVerifier();
        if (lv == null) {
            LOGGER.warn("Ignoring chat session from {} due to missing Services public key", (Object)gameProfile.getName());
            currentEntry.resetSession(this.isSecureChatEnforced());
            return;
        }
        PublicPlayerSession.Serialized lv2 = receivedEntry.chatSession();
        if (lv2 != null) {
            try {
                PublicPlayerSession lv3 = lv2.toSession(gameProfile, lv);
                currentEntry.setSession(lv3);
            } catch (PlayerPublicKey.PublicKeyException lv4) {
                LOGGER.error("Failed to validate profile key for player: '{}'", (Object)gameProfile.getName(), (Object)lv4);
                currentEntry.resetSession(this.isSecureChatEnforced());
            }
        } else {
            currentEntry.resetSession(this.isSecureChatEnforced());
        }
    }

    private boolean isSecureChatEnforced() {
        return this.client.providesProfileKeys() && this.secureChatEnforced;
    }

    @Override
    public void onPlayerAbilities(PlayerAbilitiesS2CPacket packet) {
        NetworkThreadUtils.forceMainThread(packet, this, this.client);
        ClientPlayerEntity lv = this.client.player;
        lv.getAbilities().flying = packet.isFlying();
        lv.getAbilities().creativeMode = packet.isCreativeMode();
        lv.getAbilities().invulnerable = packet.isInvulnerable();
        lv.getAbilities().allowFlying = packet.allowFlying();
        lv.getAbilities().setFlySpeed(packet.getFlySpeed());
        lv.getAbilities().setWalkSpeed(packet.getWalkSpeed());
    }

    @Override
    public void onPlaySound(PlaySoundS2CPacket packet) {
        NetworkThreadUtils.forceMainThread(packet, this, this.client);
        this.client.world.playSound((PlayerEntity)this.client.player, packet.getX(), packet.getY(), packet.getZ(), packet.getSound(), packet.getCategory(), packet.getVolume(), packet.getPitch(), packet.getSeed());
    }

    @Override
    public void onPlaySoundFromEntity(PlaySoundFromEntityS2CPacket packet) {
        NetworkThreadUtils.forceMainThread(packet, this, this.client);
        Entity lv = this.world.getEntityById(packet.getEntityId());
        if (lv == null) {
            return;
        }
        this.client.world.playSoundFromEntity(this.client.player, lv, packet.getSound(), packet.getCategory(), packet.getVolume(), packet.getPitch(), packet.getSeed());
    }

    @Override
    public void onBossBar(BossBarS2CPacket packet) {
        NetworkThreadUtils.forceMainThread(packet, this, this.client);
        this.client.inGameHud.getBossBarHud().handlePacket(packet);
    }

    @Override
    public void onCooldownUpdate(CooldownUpdateS2CPacket packet) {
        NetworkThreadUtils.forceMainThread(packet, this, this.client);
        if (packet.cooldown() == 0) {
            this.client.player.getItemCooldownManager().remove(packet.item());
        } else {
            this.client.player.getItemCooldownManager().set(packet.item(), packet.cooldown());
        }
    }

    @Override
    public void onVehicleMove(VehicleMoveS2CPacket packet) {
        NetworkThreadUtils.forceMainThread(packet, this, this.client);
        Entity lv = this.client.player.getRootVehicle();
        if (lv != this.client.player && lv.isLogicalSideForUpdatingMovement()) {
            lv.updatePositionAndAngles(packet.getX(), packet.getY(), packet.getZ(), packet.getYaw(), packet.getPitch());
            this.connection.send(new VehicleMoveC2SPacket(lv));
        }
    }

    @Override
    public void onOpenWrittenBook(OpenWrittenBookS2CPacket packet) {
        NetworkThreadUtils.forceMainThread(packet, this, this.client);
        ItemStack lv = this.client.player.getStackInHand(packet.getHand());
        BookScreen.Contents lv2 = BookScreen.Contents.create(lv);
        if (lv2 != null) {
            this.client.setScreen(new BookScreen(lv2));
        }
    }

    @Override
    public void onCustomPayload(CustomPayload payload) {
        if (payload instanceof DebugPathCustomPayload) {
            DebugPathCustomPayload lv = (DebugPathCustomPayload)payload;
            this.client.debugRenderer.pathfindingDebugRenderer.addPath(lv.entityId(), lv.path(), lv.maxNodeDistance());
        } else if (payload instanceof DebugNeighborsUpdateCustomPayload) {
            DebugNeighborsUpdateCustomPayload lv2 = (DebugNeighborsUpdateCustomPayload)payload;
            ((NeighborUpdateDebugRenderer)this.client.debugRenderer.neighborUpdateDebugRenderer).addNeighborUpdate(lv2.time(), lv2.pos());
        } else if (payload instanceof DebugStructuresCustomPayload) {
            DebugStructuresCustomPayload lv3 = (DebugStructuresCustomPayload)payload;
            this.client.debugRenderer.structureDebugRenderer.addStructure(lv3.mainBB(), lv3.pieces(), lv3.dimension());
        } else if (payload instanceof DebugWorldgenAttemptCustomPayload) {
            DebugWorldgenAttemptCustomPayload lv4 = (DebugWorldgenAttemptCustomPayload)payload;
            ((WorldGenAttemptDebugRenderer)this.client.debugRenderer.worldGenAttemptDebugRenderer).addBox(lv4.pos(), lv4.scale(), lv4.red(), lv4.green(), lv4.blue(), lv4.alpha());
        } else if (payload instanceof DebugPoiTicketCountCustomPayload) {
            DebugPoiTicketCountCustomPayload lv5 = (DebugPoiTicketCountCustomPayload)payload;
            this.client.debugRenderer.villageDebugRenderer.setFreeTicketCount(lv5.pos(), lv5.freeTicketCount());
        } else if (payload instanceof DebugPoiAddedCustomPayload) {
            DebugPoiAddedCustomPayload lv6 = (DebugPoiAddedCustomPayload)payload;
            VillageDebugRenderer.PointOfInterest lv7 = new VillageDebugRenderer.PointOfInterest(lv6.pos(), lv6.poiType(), lv6.freeTicketCount());
            this.client.debugRenderer.villageDebugRenderer.addPointOfInterest(lv7);
        } else if (payload instanceof DebugPoiRemovedCustomPayload) {
            DebugPoiRemovedCustomPayload lv8 = (DebugPoiRemovedCustomPayload)payload;
            this.client.debugRenderer.villageDebugRenderer.removePointOfInterest(lv8.pos());
        } else if (payload instanceof DebugVillageSectionsCustomPayload) {
            DebugVillageSectionsCustomPayload lv9 = (DebugVillageSectionsCustomPayload)payload;
            VillageSectionsDebugRenderer lv10 = this.client.debugRenderer.villageSectionsDebugRenderer;
            lv9.villageChunks().forEach(lv10::addSection);
            lv9.notVillageChunks().forEach(lv10::removeSection);
        } else if (payload instanceof DebugGoalSelectorCustomPayload) {
            DebugGoalSelectorCustomPayload lv11 = (DebugGoalSelectorCustomPayload)payload;
            this.client.debugRenderer.goalSelectorDebugRenderer.setGoalSelectorList(lv11.entityId(), lv11.pos(), lv11.goals());
        } else if (payload instanceof DebugBrainCustomPayload) {
            DebugBrainCustomPayload lv12 = (DebugBrainCustomPayload)payload;
            this.client.debugRenderer.villageDebugRenderer.addBrain(lv12.brainDump());
        } else if (payload instanceof DebugBeeCustomPayload) {
            DebugBeeCustomPayload lv13 = (DebugBeeCustomPayload)payload;
            this.client.debugRenderer.beeDebugRenderer.addBee(lv13.beeInfo());
        } else if (payload instanceof DebugHiveCustomPayload) {
            DebugHiveCustomPayload lv14 = (DebugHiveCustomPayload)payload;
            this.client.debugRenderer.beeDebugRenderer.addHive(lv14.hiveInfo(), this.world.getTime());
        } else if (payload instanceof DebugGameTestAddMarkerCustomPayload) {
            DebugGameTestAddMarkerCustomPayload lv15 = (DebugGameTestAddMarkerCustomPayload)payload;
            this.client.debugRenderer.gameTestDebugRenderer.addMarker(lv15.pos(), lv15.color(), lv15.text(), lv15.durationMs());
        } else if (payload instanceof DebugGameTestClearCustomPayload) {
            this.client.debugRenderer.gameTestDebugRenderer.clear();
        } else if (payload instanceof DebugRaidsCustomPayload) {
            DebugRaidsCustomPayload lv16 = (DebugRaidsCustomPayload)payload;
            this.client.debugRenderer.raidCenterDebugRenderer.setRaidCenters(lv16.raidCenters());
        } else if (payload instanceof DebugGameEventCustomPayload) {
            DebugGameEventCustomPayload lv17 = (DebugGameEventCustomPayload)payload;
            this.client.debugRenderer.gameEventDebugRenderer.addEvent(lv17.gameEventType(), lv17.pos());
        } else if (payload instanceof DebugGameEventListenersCustomPayload) {
            DebugGameEventListenersCustomPayload lv18 = (DebugGameEventListenersCustomPayload)payload;
            this.client.debugRenderer.gameEventDebugRenderer.addListener(lv18.listenerPos(), lv18.listenerRange());
        } else if (payload instanceof DebugBreezeCustomPayload) {
            DebugBreezeCustomPayload lv19 = (DebugBreezeCustomPayload)payload;
            this.client.debugRenderer.breezeDebugRenderer.addBreezeDebugInfo(lv19.breezeInfo());
        } else {
            this.warnOnUnknownPayload(payload);
        }
    }

    private void warnOnUnknownPayload(CustomPayload payload) {
        LOGGER.warn("Unknown custom packet payload: {}", (Object)payload.getId().id());
    }

    @Override
    public void onScoreboardObjectiveUpdate(ScoreboardObjectiveUpdateS2CPacket packet) {
        NetworkThreadUtils.forceMainThread(packet, this, this.client);
        String string = packet.getName();
        if (packet.getMode() == 0) {
            this.scoreboard.addObjective(string, ScoreboardCriterion.DUMMY, packet.getDisplayName(), packet.getType(), false, packet.getNumberFormat().orElse(null));
        } else {
            ScoreboardObjective lv = this.scoreboard.getNullableObjective(string);
            if (lv != null) {
                if (packet.getMode() == ScoreboardObjectiveUpdateS2CPacket.REMOVE_MODE) {
                    this.scoreboard.removeObjective(lv);
                } else if (packet.getMode() == ScoreboardObjectiveUpdateS2CPacket.UPDATE_MODE) {
                    lv.setRenderType(packet.getType());
                    lv.setDisplayName(packet.getDisplayName());
                    lv.setNumberFormat(packet.getNumberFormat().orElse(null));
                }
            }
        }
    }

    @Override
    public void onScoreboardScoreUpdate(ScoreboardScoreUpdateS2CPacket packet) {
        NetworkThreadUtils.forceMainThread(packet, this, this.client);
        String string = packet.objectiveName();
        ScoreHolder lv = ScoreHolder.fromName(packet.scoreHolderName());
        ScoreboardObjective lv2 = this.scoreboard.getNullableObjective(string);
        if (lv2 != null) {
            ScoreAccess lv3 = this.scoreboard.getOrCreateScore(lv, lv2, true);
            lv3.setScore(packet.score());
            lv3.setDisplayText(packet.display().orElse(null));
            lv3.setNumberFormat(packet.numberFormat().orElse(null));
        } else {
            LOGGER.warn("Received packet for unknown scoreboard objective: {}", (Object)string);
        }
    }

    @Override
    public void onScoreboardScoreReset(ScoreboardScoreResetS2CPacket packet) {
        NetworkThreadUtils.forceMainThread(packet, this, this.client);
        String string = packet.objectiveName();
        ScoreHolder lv = ScoreHolder.fromName(packet.scoreHolderName());
        if (string == null) {
            this.scoreboard.removeScores(lv);
        } else {
            ScoreboardObjective lv2 = this.scoreboard.getNullableObjective(string);
            if (lv2 != null) {
                this.scoreboard.removeScore(lv, lv2);
            } else {
                LOGGER.warn("Received packet for unknown scoreboard objective: {}", (Object)string);
            }
        }
    }

    @Override
    public void onScoreboardDisplay(ScoreboardDisplayS2CPacket packet) {
        NetworkThreadUtils.forceMainThread(packet, this, this.client);
        String string = packet.getName();
        ScoreboardObjective lv = string == null ? null : this.scoreboard.getNullableObjective(string);
        this.scoreboard.setObjectiveSlot(packet.getSlot(), lv);
    }

    @Override
    public void onTeam(TeamS2CPacket packet) {
        Team lv2;
        NetworkThreadUtils.forceMainThread(packet, this, this.client);
        TeamS2CPacket.Operation lv = packet.getTeamOperation();
        if (lv == TeamS2CPacket.Operation.ADD) {
            lv2 = this.scoreboard.addTeam(packet.getTeamName());
        } else {
            lv2 = this.scoreboard.getTeam(packet.getTeamName());
            if (lv2 == null) {
                LOGGER.warn("Received packet for unknown team {}: team action: {}, player action: {}", new Object[]{packet.getTeamName(), packet.getTeamOperation(), packet.getPlayerListOperation()});
                return;
            }
        }
        Optional<TeamS2CPacket.SerializableTeam> optional = packet.getTeam();
        optional.ifPresent(team -> {
            AbstractTeam.CollisionRule lv2;
            lv2.setDisplayName(team.getDisplayName());
            lv2.setColor(team.getColor());
            lv2.setFriendlyFlagsBitwise(team.getFriendlyFlagsBitwise());
            AbstractTeam.VisibilityRule lv = AbstractTeam.VisibilityRule.getRule(team.getNameTagVisibilityRule());
            if (lv != null) {
                lv2.setNameTagVisibilityRule(lv);
            }
            if ((lv2 = AbstractTeam.CollisionRule.getRule(team.getCollisionRule())) != null) {
                lv2.setCollisionRule(lv2);
            }
            lv2.setPrefix(team.getPrefix());
            lv2.setSuffix(team.getSuffix());
        });
        TeamS2CPacket.Operation lv3 = packet.getPlayerListOperation();
        if (lv3 == TeamS2CPacket.Operation.ADD) {
            for (String string : packet.getPlayerNames()) {
                this.scoreboard.addScoreHolderToTeam(string, lv2);
            }
        } else if (lv3 == TeamS2CPacket.Operation.REMOVE) {
            for (String string : packet.getPlayerNames()) {
                this.scoreboard.removeScoreHolderFromTeam(string, lv2);
            }
        }
        if (lv == TeamS2CPacket.Operation.REMOVE) {
            this.scoreboard.removeTeam(lv2);
        }
    }

    @Override
    public void onParticle(ParticleS2CPacket packet) {
        NetworkThreadUtils.forceMainThread(packet, this, this.client);
        if (packet.getCount() == 0) {
            double d = packet.getSpeed() * packet.getOffsetX();
            double e = packet.getSpeed() * packet.getOffsetY();
            double f = packet.getSpeed() * packet.getOffsetZ();
            try {
                this.world.addParticle(packet.getParameters(), packet.isLongDistance(), packet.getX(), packet.getY(), packet.getZ(), d, e, f);
            } catch (Throwable throwable) {
                LOGGER.warn("Could not spawn particle effect {}", (Object)packet.getParameters());
            }
        } else {
            for (int i = 0; i < packet.getCount(); ++i) {
                double g = this.random.nextGaussian() * (double)packet.getOffsetX();
                double h = this.random.nextGaussian() * (double)packet.getOffsetY();
                double j = this.random.nextGaussian() * (double)packet.getOffsetZ();
                double k = this.random.nextGaussian() * (double)packet.getSpeed();
                double l = this.random.nextGaussian() * (double)packet.getSpeed();
                double m = this.random.nextGaussian() * (double)packet.getSpeed();
                try {
                    this.world.addParticle(packet.getParameters(), packet.isLongDistance(), packet.getX() + g, packet.getY() + h, packet.getZ() + j, k, l, m);
                    continue;
                } catch (Throwable throwable2) {
                    LOGGER.warn("Could not spawn particle effect {}", (Object)packet.getParameters());
                    return;
                }
            }
        }
    }

    @Override
    public void onEntityAttributes(EntityAttributesS2CPacket packet) {
        NetworkThreadUtils.forceMainThread(packet, this, this.client);
        Entity lv = this.world.getEntityById(packet.getEntityId());
        if (lv == null) {
            return;
        }
        if (!(lv instanceof LivingEntity)) {
            throw new IllegalStateException("Server tried to update attributes of a non-living entity (actually: " + String.valueOf(lv) + ")");
        }
        AttributeContainer lv2 = ((LivingEntity)lv).getAttributes();
        for (EntityAttributesS2CPacket.Entry lv3 : packet.getEntries()) {
            EntityAttributeInstance lv4 = lv2.getCustomInstance(lv3.attribute());
            if (lv4 == null) {
                LOGGER.warn("Entity {} does not have attribute {}", (Object)lv, (Object)lv3.attribute().getIdAsString());
                continue;
            }
            lv4.setBaseValue(lv3.base());
            lv4.clearModifiers();
            for (EntityAttributeModifier lv5 : lv3.modifiers()) {
                lv4.addTemporaryModifier(lv5);
            }
        }
    }

    @Override
    public void onCraftFailedResponse(CraftFailedResponseS2CPacket packet) {
        NetworkThreadUtils.forceMainThread(packet, this, this.client);
        ScreenHandler lv = this.client.player.currentScreenHandler;
        if (lv.syncId != packet.getSyncId()) {
            return;
        }
        this.recipeManager.get(packet.getRecipeId()).ifPresent(recipe -> {
            if (this.client.currentScreen instanceof RecipeBookProvider) {
                RecipeBookWidget lv = ((RecipeBookProvider)((Object)this.client.currentScreen)).getRecipeBookWidget();
                lv.showGhostRecipe((RecipeEntry<?>)recipe, (List<Slot>)arg.slots);
            }
        });
    }

    @Override
    public void onLightUpdate(LightUpdateS2CPacket packet) {
        NetworkThreadUtils.forceMainThread(packet, this, this.client);
        int i = packet.getChunkX();
        int j = packet.getChunkZ();
        LightData lv = packet.getData();
        this.world.enqueueChunkUpdate(() -> this.readLightData(i, j, lv));
    }

    private void readLightData(int x, int z, LightData data) {
        LightingProvider lv = this.world.getChunkManager().getLightingProvider();
        BitSet bitSet = data.getInitedSky();
        BitSet bitSet2 = data.getUninitedSky();
        Iterator<byte[]> iterator = data.getSkyNibbles().iterator();
        this.updateLighting(x, z, lv, LightType.SKY, bitSet, bitSet2, iterator);
        BitSet bitSet3 = data.getInitedBlock();
        BitSet bitSet4 = data.getUninitedBlock();
        Iterator<byte[]> iterator2 = data.getBlockNibbles().iterator();
        this.updateLighting(x, z, lv, LightType.BLOCK, bitSet3, bitSet4, iterator2);
        lv.setColumnEnabled(new ChunkPos(x, z), true);
    }

    @Override
    public void onSetTradeOffers(SetTradeOffersS2CPacket packet) {
        NetworkThreadUtils.forceMainThread(packet, this, this.client);
        ScreenHandler lv = this.client.player.currentScreenHandler;
        if (packet.getSyncId() == lv.syncId && lv instanceof MerchantScreenHandler) {
            MerchantScreenHandler lv2 = (MerchantScreenHandler)lv;
            lv2.setOffers(packet.getOffers());
            lv2.setExperienceFromServer(packet.getExperience());
            lv2.setLevelProgress(packet.getLevelProgress());
            lv2.setLeveled(packet.isLeveled());
            lv2.setCanRefreshTrades(packet.isRefreshable());
        }
    }

    @Override
    public void onChunkLoadDistance(ChunkLoadDistanceS2CPacket packet) {
        NetworkThreadUtils.forceMainThread(packet, this, this.client);
        this.chunkLoadDistance = packet.getDistance();
        this.client.options.setServerViewDistance(this.chunkLoadDistance);
        this.world.getChunkManager().updateLoadDistance(packet.getDistance());
    }

    @Override
    public void onSimulationDistance(SimulationDistanceS2CPacket packet) {
        NetworkThreadUtils.forceMainThread(packet, this, this.client);
        this.simulationDistance = packet.simulationDistance();
        this.world.setSimulationDistance(this.simulationDistance);
    }

    @Override
    public void onChunkRenderDistanceCenter(ChunkRenderDistanceCenterS2CPacket packet) {
        NetworkThreadUtils.forceMainThread(packet, this, this.client);
        this.world.getChunkManager().setChunkMapCenter(packet.getChunkX(), packet.getChunkZ());
    }

    @Override
    public void onPlayerActionResponse(PlayerActionResponseS2CPacket packet) {
        NetworkThreadUtils.forceMainThread(packet, this, this.client);
        this.world.handlePlayerActionResponse(packet.sequence());
    }

    @Override
    public void onBundle(BundleS2CPacket packet) {
        NetworkThreadUtils.forceMainThread(packet, this, this.client);
        for (Packet<ClientPlayNetworkHandler> packet2 : packet.getPackets()) {
            packet2.apply(this);
        }
    }

    @Override
    public void onProjectilePower(ProjectilePowerS2CPacket packet) {
        NetworkThreadUtils.forceMainThread(packet, this, this.client);
        Entity lv = this.world.getEntityById(packet.getEntityId());
        if (lv instanceof ExplosiveProjectileEntity) {
            ExplosiveProjectileEntity lv2 = (ExplosiveProjectileEntity)lv;
            lv2.accelerationPower = packet.getAccelerationPower();
        }
    }

    @Override
    public void onStartChunkSend(StartChunkSendS2CPacket packet) {
        this.chunkBatchSizeCalculator.onStartChunkSend();
    }

    @Override
    public void onChunkSent(ChunkSentS2CPacket packet) {
        this.chunkBatchSizeCalculator.onChunkSent(packet.batchSize());
        this.sendPacket(new AcknowledgeChunksC2SPacket(this.chunkBatchSizeCalculator.getDesiredChunksPerTick()));
    }

    @Override
    public void onDebugSample(DebugSampleS2CPacket packet) {
        this.client.getDebugHud().set(packet.sample(), packet.debugSampleType());
    }

    @Override
    public void onPingResult(PingResultS2CPacket packet) {
        this.pingMeasurer.onPingResult(packet);
    }

    private void updateLighting(int chunkX, int chunkZ, LightingProvider provider, LightType type, BitSet inited, BitSet uninited, Iterator<byte[]> nibbles) {
        for (int k = 0; k < provider.getHeight(); ++k) {
            int l = provider.getBottomY() + k;
            boolean bl = inited.get(k);
            boolean bl2 = uninited.get(k);
            if (!bl && !bl2) continue;
            provider.enqueueSectionData(type, ChunkSectionPos.from(chunkX, l, chunkZ), bl ? new ChunkNibbleArray((byte[])nibbles.next().clone()) : new ChunkNibbleArray());
            this.world.scheduleBlockRenders(chunkX, l, chunkZ);
        }
    }

    public ClientConnection getConnection() {
        return this.connection;
    }

    @Override
    public boolean isConnectionOpen() {
        return this.connection.isOpen() && !this.worldCleared;
    }

    public Collection<PlayerListEntry> getListedPlayerListEntries() {
        return this.listedPlayerListEntries;
    }

    public Collection<PlayerListEntry> getPlayerList() {
        return this.playerListEntries.values();
    }

    public Collection<UUID> getPlayerUuids() {
        return this.playerListEntries.keySet();
    }

    @Nullable
    public PlayerListEntry getPlayerListEntry(UUID uuid) {
        return this.playerListEntries.get(uuid);
    }

    @Nullable
    public PlayerListEntry getPlayerListEntry(String profileName) {
        for (PlayerListEntry lv : this.playerListEntries.values()) {
            if (!lv.getProfile().getName().equals(profileName)) continue;
            return lv;
        }
        return null;
    }

    public GameProfile getProfile() {
        return this.profile;
    }

    public ClientAdvancementManager getAdvancementHandler() {
        return this.advancementHandler;
    }

    public CommandDispatcher<CommandSource> getCommandDispatcher() {
        return this.commandDispatcher;
    }

    public ClientWorld getWorld() {
        return this.world;
    }

    public DataQueryHandler getDataQueryHandler() {
        return this.dataQueryHandler;
    }

    public UUID getSessionId() {
        return this.sessionId;
    }

    public Set<RegistryKey<World>> getWorldKeys() {
        return this.worldKeys;
    }

    public DynamicRegistryManager.Immutable getRegistryManager() {
        return this.combinedDynamicRegistries;
    }

    public void acknowledge(SignedMessage message, boolean displayed) {
        MessageSignatureData lv = message.signature();
        if (lv != null && this.lastSeenMessagesCollector.add(lv, displayed) && this.lastSeenMessagesCollector.getMessageCount() > 64) {
            this.sendAcknowledgment();
        }
    }

    private void sendAcknowledgment() {
        int i = this.lastSeenMessagesCollector.resetMessageCount();
        if (i > 0) {
            this.sendPacket(new MessageAcknowledgmentC2SPacket(i));
        }
    }

    public void sendChatMessage(String content) {
        Instant instant = Instant.now();
        long l = NetworkEncryptionUtils.SecureRandomUtil.nextLong();
        LastSeenMessagesCollector.LastSeenMessages lv = this.lastSeenMessagesCollector.collect();
        MessageSignatureData lv2 = this.messagePacker.pack(new MessageBody(content, instant, l, lv.lastSeen()));
        this.sendPacket(new ChatMessageC2SPacket(content, instant, l, lv2, lv.update()));
    }

    public void sendChatCommand(String command) {
        SignedArgumentList<CommandSource> lv = SignedArgumentList.of(this.parse(command));
        if (lv.arguments().isEmpty()) {
            this.sendPacket(new CommandExecutionC2SPacket(command));
            return;
        }
        Instant instant = Instant.now();
        long l = NetworkEncryptionUtils.SecureRandomUtil.nextLong();
        LastSeenMessagesCollector.LastSeenMessages lv2 = this.lastSeenMessagesCollector.collect();
        ArgumentSignatureDataMap lv3 = ArgumentSignatureDataMap.sign(lv, value -> {
            MessageBody lv = new MessageBody(value, instant, l, lv2.lastSeen());
            return this.messagePacker.pack(lv);
        });
        this.sendPacket(new ChatCommandSignedC2SPacket(command, instant, l, lv3, lv2.update()));
    }

    public boolean sendCommand(String command) {
        if (!SignedArgumentList.isNotEmpty(this.parse(command))) {
            this.sendPacket(new CommandExecutionC2SPacket(command));
            return true;
        }
        return false;
    }

    private ParseResults<CommandSource> parse(String command) {
        return this.commandDispatcher.parse(command, (CommandSource)this.commandSource);
    }

    @Override
    public void tick() {
        ProfileKeys lv;
        if (this.connection.isEncrypted() && (lv = this.client.getProfileKeys()).isExpired()) {
            lv.fetchKeyPair().thenAcceptAsync(keyPair -> keyPair.ifPresent(this::updateKeyPair), (Executor)this.client);
        }
        this.sendQueuedPackets();
        if (this.client.getDebugHud().shouldShowPacketSizeAndPingCharts()) {
            this.pingMeasurer.ping();
        }
        this.debugSampleSubscriber.tick();
        this.worldSession.tick();
        if (this.worldLoadingState != null) {
            this.worldLoadingState.tick();
        }
    }

    public void updateKeyPair(PlayerKeyPair keyPair) {
        if (!this.client.uuidEquals(this.profile.getId())) {
            return;
        }
        if (this.session != null && this.session.keyPair().equals(keyPair)) {
            return;
        }
        this.session = ClientPlayerSession.create(keyPair);
        this.messagePacker = this.session.createPacker(this.profile.getId());
        this.sendPacket(new PlayerSessionC2SPacket(this.session.toPublicSession().toSerialized()));
    }

    @Nullable
    public ServerInfo getServerInfo() {
        return this.serverInfo;
    }

    public FeatureSet getEnabledFeatures() {
        return this.enabledFeatures;
    }

    public boolean hasFeature(FeatureSet feature) {
        return feature.isSubsetOf(this.getEnabledFeatures());
    }

    public Scoreboard getScoreboard() {
        return this.scoreboard;
    }

    public BrewingRecipeRegistry getBrewingRecipeRegistry() {
        return this.brewingRecipeRegistry;
    }

    public void refreshSearchManager() {
        this.searchManager.refresh();
    }

    public SearchManager getSearchManager() {
        return this.searchManager;
    }

    public ServerLinks getServerLinks() {
        return this.serverLinks;
    }
}

