/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 * 
 * Could not load the following classes:
 *  net.fabricmc.api.EnvType
 *  net.fabricmc.api.Environment
 *  org.jetbrains.annotations.Nullable
 */
package net.minecraft.client;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.google.common.collect.Queues;
import com.mojang.authlib.GameProfile;
import com.mojang.authlib.exceptions.AuthenticationException;
import com.mojang.authlib.minecraft.BanDetails;
import com.mojang.authlib.minecraft.MinecraftSessionService;
import com.mojang.authlib.minecraft.UserApiService;
import com.mojang.authlib.yggdrasil.ProfileActionType;
import com.mojang.authlib.yggdrasil.ServicesKeyType;
import com.mojang.authlib.yggdrasil.YggdrasilAuthenticationService;
import com.mojang.blaze3d.platform.GlConst;
import com.mojang.blaze3d.platform.GlDebugInfo;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.systems.VertexSorter;
import com.mojang.datafixers.DataFixer;
import com.mojang.logging.LogUtils;
import java.io.File;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.lang.management.ManagementFactory;
import java.net.Proxy;
import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.MissingResourceException;
import java.util.Queue;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.Bootstrap;
import net.minecraft.SharedConstants;
import net.minecraft.block.Block;
import net.minecraft.block.BlockRenderType;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.SkullBlockEntity;
import net.minecraft.client.ClientBrandRetriever;
import net.minecraft.client.Keyboard;
import net.minecraft.client.Mouse;
import net.minecraft.client.QuickPlay;
import net.minecraft.client.QuickPlayLogger;
import net.minecraft.client.RunArgs;
import net.minecraft.client.WindowEventHandler;
import net.minecraft.client.WindowSettings;
import net.minecraft.client.color.block.BlockColors;
import net.minecraft.client.color.item.ItemColors;
import net.minecraft.client.font.FontManager;
import net.minecraft.client.font.FreeTypeUtil;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gl.Framebuffer;
import net.minecraft.client.gl.GlDebug;
import net.minecraft.client.gl.GlTimer;
import net.minecraft.client.gl.SimpleFramebuffer;
import net.minecraft.client.gl.WindowFramebuffer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.hud.DebugHud;
import net.minecraft.client.gui.hud.InGameHud;
import net.minecraft.client.gui.navigation.GuiNavigationType;
import net.minecraft.client.gui.screen.AccessibilityOnboardingScreen;
import net.minecraft.client.gui.screen.ChatScreen;
import net.minecraft.client.gui.screen.ConfirmLinkScreen;
import net.minecraft.client.gui.screen.DeathScreen;
import net.minecraft.client.gui.screen.DownloadingTerrainScreen;
import net.minecraft.client.gui.screen.GameMenuScreen;
import net.minecraft.client.gui.screen.MessageScreen;
import net.minecraft.client.gui.screen.OutOfMemoryScreen;
import net.minecraft.client.gui.screen.Overlay;
import net.minecraft.client.gui.screen.ProgressScreen;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.SleepingChatScreen;
import net.minecraft.client.gui.screen.SplashOverlay;
import net.minecraft.client.gui.screen.TitleScreen;
import net.minecraft.client.gui.screen.advancement.AdvancementsScreen;
import net.minecraft.client.gui.screen.ingame.CreativeInventoryScreen;
import net.minecraft.client.gui.screen.ingame.HandledScreens;
import net.minecraft.client.gui.screen.ingame.InventoryScreen;
import net.minecraft.client.gui.screen.multiplayer.SocialInteractionsScreen;
import net.minecraft.client.gui.screen.world.LevelLoadingScreen;
import net.minecraft.client.network.ClientLoginNetworkHandler;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.network.ClientPlayerInteractionManager;
import net.minecraft.client.network.ServerInfo;
import net.minecraft.client.network.SocialInteractionsManager;
import net.minecraft.client.network.message.MessageHandler;
import net.minecraft.client.option.CloudRenderMode;
import net.minecraft.client.option.GameOptions;
import net.minecraft.client.option.GraphicsMode;
import net.minecraft.client.option.HotbarStorage;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.option.NarratorMode;
import net.minecraft.client.option.Perspective;
import net.minecraft.client.particle.ParticleManager;
import net.minecraft.client.realms.RealmsClient;
import net.minecraft.client.realms.RealmsPeriodicCheckers;
import net.minecraft.client.render.BackgroundRenderer;
import net.minecraft.client.render.BufferBuilder;
import net.minecraft.client.render.BufferBuilderStorage;
import net.minecraft.client.render.BufferRenderer;
import net.minecraft.client.render.BuiltBuffer;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.client.render.RenderTickCounter;
import net.minecraft.client.render.Tessellator;
import net.minecraft.client.render.VertexFormat;
import net.minecraft.client.render.VertexFormats;
import net.minecraft.client.render.WorldRenderer;
import net.minecraft.client.render.block.BlockModels;
import net.minecraft.client.render.block.BlockRenderManager;
import net.minecraft.client.render.block.entity.BlockEntityRenderDispatcher;
import net.minecraft.client.render.debug.DebugRenderer;
import net.minecraft.client.render.entity.EntityRenderDispatcher;
import net.minecraft.client.render.entity.EntityRenderers;
import net.minecraft.client.render.entity.model.EntityModelLoader;
import net.minecraft.client.render.item.BuiltinModelItemRenderer;
import net.minecraft.client.render.item.ItemRenderer;
import net.minecraft.client.render.model.BakedModel;
import net.minecraft.client.render.model.BakedModelManager;
import net.minecraft.client.resource.DefaultClientResourcePackProvider;
import net.minecraft.client.resource.FoliageColormapResourceSupplier;
import net.minecraft.client.resource.GrassColormapResourceSupplier;
import net.minecraft.client.resource.PeriodicNotificationManager;
import net.minecraft.client.resource.ResourceReloadLogger;
import net.minecraft.client.resource.SplashTextResourceSupplier;
import net.minecraft.client.resource.VideoWarningManager;
import net.minecraft.client.resource.language.I18n;
import net.minecraft.client.resource.language.LanguageManager;
import net.minecraft.client.resource.server.ServerResourcePackLoader;
import net.minecraft.client.session.Bans;
import net.minecraft.client.session.ProfileKeys;
import net.minecraft.client.session.Session;
import net.minecraft.client.session.report.AbuseReportContext;
import net.minecraft.client.session.report.ReporterEnvironment;
import net.minecraft.client.session.telemetry.GameLoadTimeEvent;
import net.minecraft.client.session.telemetry.TelemetryEventProperty;
import net.minecraft.client.session.telemetry.TelemetryManager;
import net.minecraft.client.sound.MusicTracker;
import net.minecraft.client.sound.SoundManager;
import net.minecraft.client.texture.GuiAtlasManager;
import net.minecraft.client.texture.MapDecorationsAtlasManager;
import net.minecraft.client.texture.PaintingManager;
import net.minecraft.client.texture.PlayerSkinProvider;
import net.minecraft.client.texture.Sprite;
import net.minecraft.client.texture.SpriteAtlasTexture;
import net.minecraft.client.texture.StatusEffectSpriteManager;
import net.minecraft.client.texture.TextureManager;
import net.minecraft.client.toast.SystemToast;
import net.minecraft.client.toast.ToastManager;
import net.minecraft.client.toast.TutorialToast;
import net.minecraft.client.tutorial.TutorialManager;
import net.minecraft.client.util.ClientSamplerSource;
import net.minecraft.client.util.CommandHistoryManager;
import net.minecraft.client.util.GlException;
import net.minecraft.client.util.Icons;
import net.minecraft.client.util.NarratorManager;
import net.minecraft.client.util.ScreenshotRecorder;
import net.minecraft.client.util.Window;
import net.minecraft.client.util.WindowProvider;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.datafixer.Schemas;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.network.ClientConnection;
import net.minecraft.network.encryption.SignatureVerifier;
import net.minecraft.network.message.ChatVisibility;
import net.minecraft.network.packet.c2s.login.LoginHelloC2SPacket;
import net.minecraft.network.packet.c2s.play.PlayerActionC2SPacket;
import net.minecraft.registry.DynamicRegistryManager;
import net.minecraft.registry.Registries;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.registry.tag.BiomeTags;
import net.minecraft.resource.DefaultResourcePack;
import net.minecraft.resource.FileResourcePackProvider;
import net.minecraft.resource.ReloadableResourceManagerImpl;
import net.minecraft.resource.ResourceManager;
import net.minecraft.resource.ResourcePack;
import net.minecraft.resource.ResourcePackManager;
import net.minecraft.resource.ResourcePackSource;
import net.minecraft.resource.ResourceReload;
import net.minecraft.resource.ResourceType;
import net.minecraft.screen.ScreenTexts;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.QueueingWorldGenerationProgressListener;
import net.minecraft.server.SaveLoader;
import net.minecraft.server.WorldGenerationProgressTracker;
import net.minecraft.server.integrated.IntegratedServer;
import net.minecraft.server.integrated.IntegratedServerLoader;
import net.minecraft.sound.MusicSound;
import net.minecraft.sound.MusicType;
import net.minecraft.text.ClickEvent;
import net.minecraft.text.KeybindTranslations;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.ApiServices;
import net.minecraft.util.Formatting;
import net.minecraft.util.Hand;
import net.minecraft.util.Identifier;
import net.minecraft.util.ModStatus;
import net.minecraft.util.Nullables;
import net.minecraft.util.PathUtil;
import net.minecraft.util.SystemDetails;
import net.minecraft.util.TickDurationMonitor;
import net.minecraft.util.TimeHelper;
import net.minecraft.util.Unit;
import net.minecraft.util.Urls;
import net.minecraft.util.UserCache;
import net.minecraft.util.Util;
import net.minecraft.util.ZipCompressor;
import net.minecraft.util.crash.CrashException;
import net.minecraft.util.crash.CrashMemoryReserve;
import net.minecraft.util.crash.CrashReport;
import net.minecraft.util.crash.CrashReportSection;
import net.minecraft.util.crash.ReportType;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ColorHelper;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.path.SymlinkFinder;
import net.minecraft.util.profiler.DebugRecorder;
import net.minecraft.util.profiler.DummyProfiler;
import net.minecraft.util.profiler.DummyRecorder;
import net.minecraft.util.profiler.EmptyProfileResult;
import net.minecraft.util.profiler.ProfileResult;
import net.minecraft.util.profiler.Profiler;
import net.minecraft.util.profiler.ProfilerTiming;
import net.minecraft.util.profiler.RecordDumper;
import net.minecraft.util.profiler.Recorder;
import net.minecraft.util.profiler.TickTimeTracker;
import net.minecraft.util.thread.ReentrantThreadExecutor;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.level.storage.LevelStorage;
import net.minecraft.world.tick.TickManager;
import org.apache.commons.io.FileUtils;
import org.jetbrains.annotations.Nullable;
import org.joml.Matrix4f;
import org.joml.Matrix4fStack;
import org.lwjgl.util.tinyfd.TinyFileDialogs;
import org.slf4j.Logger;

@Environment(value=EnvType.CLIENT)
public class MinecraftClient
extends ReentrantThreadExecutor<Runnable>
implements WindowEventHandler {
    static MinecraftClient instance;
    private static final Logger LOGGER;
    public static final boolean IS_SYSTEM_MAC;
    private static final int field_32145 = 10;
    public static final Identifier DEFAULT_FONT_ID;
    public static final Identifier UNICODE_FONT_ID;
    public static final Identifier ALT_TEXT_RENDERER_ID;
    private static final Identifier REGIONAL_COMPLIANCIES_ID;
    private static final CompletableFuture<Unit> COMPLETED_UNIT_FUTURE;
    private static final Text SOCIAL_INTERACTIONS_NOT_AVAILABLE;
    public static final String GL_ERROR_DIALOGUE = "Please make sure you have up-to-date drivers (see aka.ms/mcdriver for instructions).";
    private final long field_46550 = Double.doubleToLongBits(Math.PI);
    private final Path resourcePackDir;
    private final CompletableFuture<com.mojang.authlib.yggdrasil.ProfileResult> gameProfileFuture;
    private final TextureManager textureManager;
    private final DataFixer dataFixer;
    private final WindowProvider windowProvider;
    private final Window window;
    private final RenderTickCounter.Dynamic renderTickCounter = new RenderTickCounter.Dynamic(20.0f, 0L, this::getTargetMillisPerTick);
    private final BufferBuilderStorage bufferBuilders;
    public final WorldRenderer worldRenderer;
    private final EntityRenderDispatcher entityRenderDispatcher;
    private final ItemRenderer itemRenderer;
    public final ParticleManager particleManager;
    private final Session session;
    public final TextRenderer textRenderer;
    public final TextRenderer advanceValidatingTextRenderer;
    public final GameRenderer gameRenderer;
    public final DebugRenderer debugRenderer;
    private final AtomicReference<WorldGenerationProgressTracker> worldGenProgressTracker = new AtomicReference();
    public final InGameHud inGameHud;
    public final GameOptions options;
    private final HotbarStorage creativeHotbarStorage;
    public final Mouse mouse;
    public final Keyboard keyboard;
    private GuiNavigationType navigationType = GuiNavigationType.NONE;
    public final File runDirectory;
    private final String gameVersion;
    private final String versionType;
    private final Proxy networkProxy;
    private final LevelStorage levelStorage;
    private final boolean isDemo;
    private final boolean multiplayerEnabled;
    private final boolean onlineChatEnabled;
    private final ReloadableResourceManagerImpl resourceManager;
    private final DefaultResourcePack defaultResourcePack;
    private final ServerResourcePackLoader serverResourcePackLoader;
    private final ResourcePackManager resourcePackManager;
    private final LanguageManager languageManager;
    private final BlockColors blockColors;
    private final ItemColors itemColors;
    private final Framebuffer framebuffer;
    private final SoundManager soundManager;
    private final MusicTracker musicTracker;
    private final FontManager fontManager;
    private final SplashTextResourceSupplier splashTextLoader;
    private final VideoWarningManager videoWarningManager;
    private final PeriodicNotificationManager regionalComplianciesManager = new PeriodicNotificationManager(REGIONAL_COMPLIANCIES_ID, MinecraftClient::isCountrySetTo);
    private final YggdrasilAuthenticationService authenticationService;
    private final MinecraftSessionService sessionService;
    private final UserApiService userApiService;
    private final CompletableFuture<UserApiService.UserProperties> userPropertiesFuture;
    private final PlayerSkinProvider skinProvider;
    private final BakedModelManager bakedModelManager;
    private final BlockRenderManager blockRenderManager;
    private final PaintingManager paintingManager;
    private final StatusEffectSpriteManager statusEffectSpriteManager;
    private final MapDecorationsAtlasManager mapDecorationsAtlasManager;
    private final GuiAtlasManager guiAtlasManager;
    private final ToastManager toastManager;
    private final TutorialManager tutorialManager;
    private final SocialInteractionsManager socialInteractionsManager;
    private final EntityModelLoader entityModelLoader;
    private final BlockEntityRenderDispatcher blockEntityRenderDispatcher;
    private final TelemetryManager telemetryManager;
    private final ProfileKeys profileKeys;
    private final RealmsPeriodicCheckers realmsPeriodicCheckers;
    private final QuickPlayLogger quickPlayLogger;
    @Nullable
    public ClientPlayerInteractionManager interactionManager;
    @Nullable
    public ClientWorld world;
    @Nullable
    public ClientPlayerEntity player;
    @Nullable
    private IntegratedServer server;
    @Nullable
    private ClientConnection integratedServerConnection;
    private boolean integratedServerRunning;
    @Nullable
    public Entity cameraEntity;
    @Nullable
    public Entity targetedEntity;
    @Nullable
    public HitResult crosshairTarget;
    private int itemUseCooldown;
    protected int attackCooldown;
    private volatile boolean paused;
    private long lastMetricsSampleTime = Util.getMeasuringTimeNano();
    private long nextDebugInfoUpdateTime;
    private int fpsCounter;
    public boolean skipGameRender;
    @Nullable
    public Screen currentScreen;
    @Nullable
    private Overlay overlay;
    private boolean disconnecting;
    private Thread thread;
    private volatile boolean running;
    @Nullable
    private Supplier<CrashReport> crashReportSupplier;
    private static int currentFps;
    public String fpsDebugString = "";
    private long renderTime;
    public boolean wireFrame;
    public boolean debugChunkInfo;
    public boolean debugChunkOcclusion;
    public boolean chunkCullingEnabled = true;
    private boolean windowFocused;
    private final Queue<Runnable> renderTaskQueue = Queues.newConcurrentLinkedQueue();
    @Nullable
    private CompletableFuture<Void> resourceReloadFuture;
    @Nullable
    private TutorialToast socialInteractionsToast;
    private Profiler profiler = DummyProfiler.INSTANCE;
    private int trackingTick;
    private final TickTimeTracker tickTimeTracker = new TickTimeTracker(Util.nanoTimeSupplier, () -> this.trackingTick);
    @Nullable
    private ProfileResult tickProfilerResult;
    private Recorder recorder = DummyRecorder.INSTANCE;
    private final ResourceReloadLogger resourceReloadLogger = new ResourceReloadLogger();
    private long metricsSampleDuration;
    private double gpuUtilizationPercentage;
    @Nullable
    private GlTimer.Query currentGlTimerQuery;
    private final NarratorManager narratorManager;
    private final MessageHandler messageHandler;
    private AbuseReportContext abuseReportContext;
    private final CommandHistoryManager commandHistoryManager;
    private final SymlinkFinder symlinkFinder;
    private boolean finishedLoading;
    private final long startTime;
    private long uptimeInTicks;
    private String openProfilerSection = "root";

    public MinecraftClient(RunArgs args) {
        super("Client");
        instance = this;
        this.startTime = System.currentTimeMillis();
        this.runDirectory = args.directories.runDir;
        File file = args.directories.assetDir;
        this.resourcePackDir = args.directories.resourcePackDir.toPath();
        this.gameVersion = args.game.version;
        this.versionType = args.game.versionType;
        Path path = this.runDirectory.toPath();
        this.symlinkFinder = LevelStorage.createSymlinkFinder(path.resolve("allowed_symlinks.txt"));
        DefaultClientResourcePackProvider lv = new DefaultClientResourcePackProvider(args.directories.getAssetDir(), this.symlinkFinder);
        this.serverResourcePackLoader = new ServerResourcePackLoader(this, path.resolve("downloads"), args.network);
        FileResourcePackProvider lv2 = new FileResourcePackProvider(this.resourcePackDir, ResourceType.CLIENT_RESOURCES, ResourcePackSource.NONE, this.symlinkFinder);
        this.resourcePackManager = new ResourcePackManager(lv, this.serverResourcePackLoader.getPassthroughPackProvider(), lv2);
        this.defaultResourcePack = lv.getResourcePack();
        this.networkProxy = args.network.netProxy;
        this.authenticationService = new YggdrasilAuthenticationService(this.networkProxy);
        this.sessionService = this.authenticationService.createMinecraftSessionService();
        this.session = args.network.session;
        this.gameProfileFuture = CompletableFuture.supplyAsync(() -> this.sessionService.fetchProfile(this.session.getUuidOrNull(), true), Util.getDownloadWorkerExecutor());
        this.userApiService = this.createUserApiService(this.authenticationService, args);
        this.userPropertiesFuture = CompletableFuture.supplyAsync(() -> {
            try {
                return this.userApiService.fetchProperties();
            } catch (AuthenticationException authenticationException) {
                LOGGER.error("Failed to fetch user properties", authenticationException);
                return UserApiService.OFFLINE_PROPERTIES;
            }
        }, Util.getDownloadWorkerExecutor());
        LOGGER.info("Setting user: {}", (Object)this.session.getUsername());
        LOGGER.debug("(Session ID is {})", (Object)this.session.getSessionId());
        this.isDemo = args.game.demo;
        this.multiplayerEnabled = !args.game.multiplayerDisabled;
        this.onlineChatEnabled = !args.game.onlineChatDisabled;
        this.server = null;
        KeybindTranslations.setFactory(KeyBinding::getLocalizedName);
        this.dataFixer = Schemas.getFixer();
        this.toastManager = new ToastManager(this);
        this.thread = Thread.currentThread();
        this.options = new GameOptions(this, this.runDirectory);
        RenderSystem.setShaderGlintAlpha(this.options.getGlintStrength().getValue());
        this.running = true;
        this.tutorialManager = new TutorialManager(this, this.options);
        this.creativeHotbarStorage = new HotbarStorage(path, this.dataFixer);
        LOGGER.info("Backend library: {}", (Object)RenderSystem.getBackendDescription());
        WindowSettings lv3 = this.options.overrideHeight > 0 && this.options.overrideWidth > 0 ? new WindowSettings(this.options.overrideWidth, this.options.overrideHeight, args.windowSettings.fullscreenWidth, args.windowSettings.fullscreenHeight, args.windowSettings.fullscreen) : args.windowSettings;
        Util.nanoTimeSupplier = RenderSystem.initBackendSystem();
        this.windowProvider = new WindowProvider(this);
        this.window = this.windowProvider.createWindow(lv3, this.options.fullscreenResolution, this.getWindowTitle());
        this.onWindowFocusChanged(true);
        GameLoadTimeEvent.INSTANCE.stopTimer(TelemetryEventProperty.LOAD_TIME_PRE_WINDOW_MS);
        try {
            this.window.setIcon(this.defaultResourcePack, SharedConstants.getGameVersion().isStable() ? Icons.RELEASE : Icons.SNAPSHOT);
        } catch (IOException iOException) {
            LOGGER.error("Couldn't set icon", iOException);
        }
        this.window.setFramerateLimit(this.options.getMaxFps().getValue());
        this.mouse = new Mouse(this);
        this.mouse.setup(this.window.getHandle());
        this.keyboard = new Keyboard(this);
        this.keyboard.setup(this.window.getHandle());
        RenderSystem.initRenderer(this.options.glDebugVerbosity, false);
        this.framebuffer = new WindowFramebuffer(this.window.getFramebufferWidth(), this.window.getFramebufferHeight());
        this.framebuffer.setClearColor(0.0f, 0.0f, 0.0f, 0.0f);
        this.framebuffer.clear(IS_SYSTEM_MAC);
        this.resourceManager = new ReloadableResourceManagerImpl(ResourceType.CLIENT_RESOURCES);
        this.resourcePackManager.scanPacks();
        this.options.addResourcePackProfilesToManager(this.resourcePackManager);
        this.languageManager = new LanguageManager(this.options.language, translationStorage -> {
            if (this.player != null) {
                this.player.networkHandler.refreshSearchManager();
            }
        });
        this.resourceManager.registerReloader(this.languageManager);
        this.textureManager = new TextureManager(this.resourceManager);
        this.resourceManager.registerReloader(this.textureManager);
        this.skinProvider = new PlayerSkinProvider(this.textureManager, file.toPath().resolve("skins"), this.sessionService, this);
        this.levelStorage = new LevelStorage(path.resolve("saves"), path.resolve("backups"), this.symlinkFinder, this.dataFixer);
        this.commandHistoryManager = new CommandHistoryManager(path);
        this.soundManager = new SoundManager(this.options);
        this.resourceManager.registerReloader(this.soundManager);
        this.splashTextLoader = new SplashTextResourceSupplier(this.session);
        this.resourceManager.registerReloader(this.splashTextLoader);
        this.musicTracker = new MusicTracker(this);
        this.fontManager = new FontManager(this.textureManager);
        this.textRenderer = this.fontManager.createTextRenderer();
        this.advanceValidatingTextRenderer = this.fontManager.createAdvanceValidatingTextRenderer();
        this.resourceManager.registerReloader(this.fontManager);
        this.onFontOptionsChanged();
        this.resourceManager.registerReloader(new GrassColormapResourceSupplier());
        this.resourceManager.registerReloader(new FoliageColormapResourceSupplier());
        this.window.setPhase("Startup");
        RenderSystem.setupDefaultState(0, 0, this.window.getFramebufferWidth(), this.window.getFramebufferHeight());
        this.window.setPhase("Post startup");
        this.blockColors = BlockColors.create();
        this.itemColors = ItemColors.create(this.blockColors);
        this.bakedModelManager = new BakedModelManager(this.textureManager, this.blockColors, this.options.getMipmapLevels().getValue());
        this.resourceManager.registerReloader(this.bakedModelManager);
        this.entityModelLoader = new EntityModelLoader();
        this.resourceManager.registerReloader(this.entityModelLoader);
        this.blockEntityRenderDispatcher = new BlockEntityRenderDispatcher(this.textRenderer, this.entityModelLoader, this::getBlockRenderManager, this::getItemRenderer, this::getEntityRenderDispatcher);
        this.resourceManager.registerReloader(this.blockEntityRenderDispatcher);
        BuiltinModelItemRenderer lv4 = new BuiltinModelItemRenderer(this.blockEntityRenderDispatcher, this.entityModelLoader);
        this.resourceManager.registerReloader(lv4);
        this.itemRenderer = new ItemRenderer(this, this.textureManager, this.bakedModelManager, this.itemColors, lv4);
        this.resourceManager.registerReloader(this.itemRenderer);
        try {
            int i = Runtime.getRuntime().availableProcessors();
            Tessellator.initialize();
            this.bufferBuilders = new BufferBuilderStorage(i);
        } catch (OutOfMemoryError outOfMemoryError) {
            TinyFileDialogs.tinyfd_messageBox("Minecraft", "Oh no! The game was unable to allocate memory off-heap while trying to start. You may try to free some memory by closing other applications on your computer, check that your system meets the minimum requirements, and try again. If the problem persists, please visit: " + String.valueOf(Urls.MINECRAFT_SUPPORT), "ok", "error", true);
            throw new GlException("Unable to allocate render buffers", outOfMemoryError);
        }
        this.socialInteractionsManager = new SocialInteractionsManager(this, this.userApiService);
        this.blockRenderManager = new BlockRenderManager(this.bakedModelManager.getBlockModels(), lv4, this.blockColors);
        this.resourceManager.registerReloader(this.blockRenderManager);
        this.entityRenderDispatcher = new EntityRenderDispatcher(this, this.textureManager, this.itemRenderer, this.blockRenderManager, this.textRenderer, this.options, this.entityModelLoader);
        this.resourceManager.registerReloader(this.entityRenderDispatcher);
        this.particleManager = new ParticleManager(this.world, this.textureManager);
        this.resourceManager.registerReloader(this.particleManager);
        this.paintingManager = new PaintingManager(this.textureManager);
        this.resourceManager.registerReloader(this.paintingManager);
        this.statusEffectSpriteManager = new StatusEffectSpriteManager(this.textureManager);
        this.resourceManager.registerReloader(this.statusEffectSpriteManager);
        this.mapDecorationsAtlasManager = new MapDecorationsAtlasManager(this.textureManager);
        this.resourceManager.registerReloader(this.mapDecorationsAtlasManager);
        this.guiAtlasManager = new GuiAtlasManager(this.textureManager);
        this.resourceManager.registerReloader(this.guiAtlasManager);
        this.gameRenderer = new GameRenderer(this, this.entityRenderDispatcher.getHeldItemRenderer(), this.resourceManager, this.bufferBuilders);
        this.resourceManager.registerReloader(this.gameRenderer.createProgramReloader());
        this.worldRenderer = new WorldRenderer(this, this.entityRenderDispatcher, this.blockEntityRenderDispatcher, this.bufferBuilders);
        this.resourceManager.registerReloader(this.worldRenderer);
        this.videoWarningManager = new VideoWarningManager();
        this.resourceManager.registerReloader(this.videoWarningManager);
        this.resourceManager.registerReloader(this.regionalComplianciesManager);
        this.inGameHud = new InGameHud(this);
        this.debugRenderer = new DebugRenderer(this);
        RealmsClient lv5 = RealmsClient.createRealmsClient(this);
        this.realmsPeriodicCheckers = new RealmsPeriodicCheckers(lv5);
        RenderSystem.setErrorCallback(this::handleGlErrorByDisableVsync);
        if (this.framebuffer.textureWidth != this.window.getFramebufferWidth() || this.framebuffer.textureHeight != this.window.getFramebufferHeight()) {
            StringBuilder stringBuilder = new StringBuilder("Recovering from unsupported resolution (" + this.window.getFramebufferWidth() + "x" + this.window.getFramebufferHeight() + ").\nPlease make sure you have up-to-date drivers (see aka.ms/mcdriver for instructions).");
            if (GlDebug.isDebugMessageEnabled()) {
                stringBuilder.append("\n\nReported GL debug messages:\n").append(String.join((CharSequence)"\n", GlDebug.collectDebugMessages()));
            }
            this.window.setWindowedSize(this.framebuffer.textureWidth, this.framebuffer.textureHeight);
            TinyFileDialogs.tinyfd_messageBox("Minecraft", stringBuilder.toString(), "ok", "error", false);
        } else if (this.options.getFullscreen().getValue().booleanValue() && !this.window.isFullscreen()) {
            this.window.toggleFullscreen();
            this.options.getFullscreen().setValue(this.window.isFullscreen());
        }
        this.window.setVsync(this.options.getEnableVsync().getValue());
        this.window.setRawMouseMotion(this.options.getRawMouseInput().getValue());
        this.window.logOnGlError();
        this.onResolutionChanged();
        this.gameRenderer.preloadPrograms(this.defaultResourcePack.getFactory());
        this.telemetryManager = new TelemetryManager(this, this.userApiService, this.session);
        this.profileKeys = ProfileKeys.create(this.userApiService, this.session, path);
        this.narratorManager = new NarratorManager(this);
        this.narratorManager.checkNarratorLibrary(this.options.getNarrator().getValue() != NarratorMode.OFF);
        this.messageHandler = new MessageHandler(this);
        this.messageHandler.setChatDelay(this.options.getChatDelay().getValue());
        this.abuseReportContext = AbuseReportContext.create(ReporterEnvironment.ofIntegratedServer(), this.userApiService);
        SplashOverlay.init(this);
        this.setScreen(new MessageScreen(Text.translatable("gui.loadingMinecraft")));
        List<ResourcePack> list = this.resourcePackManager.createResourcePacks();
        this.resourceReloadLogger.reload(ResourceReloadLogger.ReloadReason.INITIAL, list);
        ResourceReload lv6 = this.resourceManager.reload(Util.getMainWorkerExecutor(), this, COMPLETED_UNIT_FUTURE, list);
        GameLoadTimeEvent.INSTANCE.startTimer(TelemetryEventProperty.LOAD_TIME_LOADING_OVERLAY_MS);
        LoadingContext lv7 = new LoadingContext(lv5, args.quickPlay);
        this.setOverlay(new SplashOverlay(this, lv6, error -> Util.ifPresentOrElse(error, throwable -> this.handleResourceReloadException((Throwable)throwable, lv7), () -> {
            if (SharedConstants.isDevelopment) {
                this.checkGameData();
            }
            this.resourceReloadLogger.finish();
            this.onFinishedLoading(lv7);
        }), false));
        this.quickPlayLogger = QuickPlayLogger.create(args.quickPlay.path());
    }

    private void onFinishedLoading(@Nullable LoadingContext loadingContext) {
        if (!this.finishedLoading) {
            this.finishedLoading = true;
            this.collectLoadTimes(loadingContext);
        }
    }

    private void collectLoadTimes(@Nullable LoadingContext loadingContext) {
        Runnable runnable = this.onInitFinished(loadingContext);
        GameLoadTimeEvent.INSTANCE.stopTimer(TelemetryEventProperty.LOAD_TIME_LOADING_OVERLAY_MS);
        GameLoadTimeEvent.INSTANCE.stopTimer(TelemetryEventProperty.LOAD_TIME_TOTAL_TIME_MS);
        GameLoadTimeEvent.INSTANCE.send(this.telemetryManager.getSender());
        runnable.run();
    }

    public boolean isFinishedLoading() {
        return this.finishedLoading;
    }

    private Runnable onInitFinished(@Nullable LoadingContext loadingContext) {
        ArrayList<Function<Runnable, Screen>> list = new ArrayList<Function<Runnable, Screen>>();
        this.createInitScreens(list);
        Runnable runnable = () -> {
            if (loadingContext != null && loadingContext.quickPlayData().isEnabled()) {
                QuickPlay.startQuickPlay(this, loadingContext.quickPlayData(), loadingContext.realmsClient());
            } else {
                this.setScreen(new TitleScreen(true));
            }
        };
        for (Function<Runnable, Screen> function : Lists.reverse(list)) {
            Screen lv = function.apply(runnable);
            runnable = () -> this.setScreen(lv);
        }
        return runnable;
    }

    private void createInitScreens(List<Function<Runnable, Screen>> list) {
        com.mojang.authlib.yggdrasil.ProfileResult profileResult;
        if (this.options.onboardAccessibility) {
            list.add(onClose -> new AccessibilityOnboardingScreen(this.options, (Runnable)onClose));
        }
        BanDetails banDetails = this.getMultiplayerBanDetails();
        if (banDetails != null) {
            list.add(onClose -> Bans.createBanScreen(confirmed -> {
                if (confirmed) {
                    Util.getOperatingSystem().open(Urls.JAVA_MODERATION);
                }
                onClose.run();
            }, banDetails));
        }
        if ((profileResult = this.gameProfileFuture.join()) != null) {
            GameProfile gameProfile = profileResult.profile();
            Set<ProfileActionType> set = profileResult.actions();
            if (set.contains((Object)ProfileActionType.FORCED_NAME_CHANGE)) {
                list.add(onClose -> Bans.createUsernameBanScreen(gameProfile.getName(), onClose));
            }
            if (set.contains((Object)ProfileActionType.USING_BANNED_SKIN)) {
                list.add(Bans::createSkinBanScreen);
            }
        }
    }

    private static boolean isCountrySetTo(Object country) {
        try {
            return Locale.getDefault().getISO3Country().equals(country);
        } catch (MissingResourceException missingResourceException) {
            return false;
        }
    }

    public void updateWindowTitle() {
        this.window.setTitle(this.getWindowTitle());
    }

    private String getWindowTitle() {
        StringBuilder stringBuilder = new StringBuilder("Minecraft");
        if (MinecraftClient.getModStatus().isModded()) {
            stringBuilder.append("*");
        }
        stringBuilder.append(" ");
        stringBuilder.append(SharedConstants.getGameVersion().getName());
        ClientPlayNetworkHandler lv = this.getNetworkHandler();
        if (lv != null && lv.getConnection().isOpen()) {
            stringBuilder.append(" - ");
            ServerInfo lv2 = this.getCurrentServerEntry();
            if (this.server != null && !this.server.isRemote()) {
                stringBuilder.append(I18n.translate("title.singleplayer", new Object[0]));
            } else if (lv2 != null && lv2.isRealm()) {
                stringBuilder.append(I18n.translate("title.multiplayer.realms", new Object[0]));
            } else if (this.server != null || lv2 != null && lv2.isLocal()) {
                stringBuilder.append(I18n.translate("title.multiplayer.lan", new Object[0]));
            } else {
                stringBuilder.append(I18n.translate("title.multiplayer.other", new Object[0]));
            }
        }
        return stringBuilder.toString();
    }

    private UserApiService createUserApiService(YggdrasilAuthenticationService authService, RunArgs runArgs) {
        if (runArgs.network.session.getAccountType() != Session.AccountType.MSA) {
            return UserApiService.OFFLINE;
        }
        return authService.createUserApiService(runArgs.network.session.getAccessToken());
    }

    public static ModStatus getModStatus() {
        return ModStatus.check("vanilla", ClientBrandRetriever::getClientModName, "Client", MinecraftClient.class);
    }

    private void handleResourceReloadException(Throwable throwable, @Nullable LoadingContext loadingContext) {
        if (this.resourcePackManager.getEnabledIds().size() > 1) {
            this.onResourceReloadFailure(throwable, null, loadingContext);
        } else {
            Util.throwUnchecked(throwable);
        }
    }

    public void onResourceReloadFailure(Throwable exception, @Nullable Text resourceName, @Nullable LoadingContext loadingContext) {
        LOGGER.info("Caught error loading resourcepacks, removing all selected resourcepacks", exception);
        this.resourceReloadLogger.recover(exception);
        this.serverResourcePackLoader.onReloadFailure();
        this.resourcePackManager.setEnabledProfiles(Collections.emptyList());
        this.options.resourcePacks.clear();
        this.options.incompatibleResourcePacks.clear();
        this.options.write();
        this.reloadResources(true, loadingContext).thenRun(() -> this.showResourceReloadFailureToast(resourceName));
    }

    private void onForcedResourceReloadFailure() {
        this.setOverlay(null);
        if (this.world != null) {
            this.world.disconnect();
            this.disconnect();
        }
        this.setScreen(new TitleScreen());
        this.showResourceReloadFailureToast(null);
    }

    private void showResourceReloadFailureToast(@Nullable Text description) {
        ToastManager lv = this.getToastManager();
        SystemToast.show(lv, SystemToast.Type.PACK_LOAD_FAILURE, Text.translatable("resourcePack.load_fail"), description);
    }

    public void run() {
        this.thread = Thread.currentThread();
        if (Runtime.getRuntime().availableProcessors() > 4) {
            this.thread.setPriority(10);
        }
        try {
            boolean bl = false;
            while (this.running) {
                this.printCrashReport();
                try {
                    TickDurationMonitor lv = TickDurationMonitor.create("Renderer");
                    boolean bl2 = this.getDebugHud().shouldShowRenderingChart();
                    this.profiler = this.startMonitor(bl2, lv);
                    this.profiler.startTick();
                    this.recorder.startTick();
                    this.render(!bl);
                    this.recorder.endTick();
                    this.profiler.endTick();
                    this.endMonitor(bl2, lv);
                } catch (OutOfMemoryError outOfMemoryError) {
                    if (bl) {
                        throw outOfMemoryError;
                    }
                    this.cleanUpAfterCrash();
                    this.setScreen(new OutOfMemoryScreen());
                    System.gc();
                    LOGGER.error(LogUtils.FATAL_MARKER, "Out of memory", outOfMemoryError);
                    bl = true;
                }
            }
        } catch (CrashException lv2) {
            LOGGER.error(LogUtils.FATAL_MARKER, "Reported exception thrown!", lv2);
            this.printCrashReport(lv2.getReport());
        } catch (Throwable throwable) {
            LOGGER.error(LogUtils.FATAL_MARKER, "Unreported exception thrown!", throwable);
            this.printCrashReport(new CrashReport("Unexpected error", throwable));
        }
    }

    void onFontOptionsChanged() {
        this.fontManager.setActiveFilters(this.options);
    }

    private void handleGlErrorByDisableVsync(int error, long description) {
        this.options.getEnableVsync().setValue(false);
        this.options.write();
    }

    public Framebuffer getFramebuffer() {
        return this.framebuffer;
    }

    public String getGameVersion() {
        return this.gameVersion;
    }

    public String getVersionType() {
        return this.versionType;
    }

    public void setCrashReportSupplierAndAddDetails(CrashReport crashReport) {
        this.crashReportSupplier = () -> this.addDetailsToCrashReport(crashReport);
    }

    public void setCrashReportSupplier(CrashReport crashReport) {
        this.crashReportSupplier = () -> crashReport;
    }

    private void printCrashReport() {
        if (this.crashReportSupplier != null) {
            MinecraftClient.printCrashReport(this, this.runDirectory, this.crashReportSupplier.get());
        }
    }

    public void printCrashReport(CrashReport crashReport) {
        CrashReport lv = this.addDetailsToCrashReport(crashReport);
        this.cleanUpAfterCrash();
        MinecraftClient.printCrashReport(this, this.runDirectory, lv);
    }

    public static void printCrashReport(@Nullable MinecraftClient client, File runDirectory, CrashReport crashReport) {
        Path path = runDirectory.toPath().resolve("crash-reports");
        Path path2 = path.resolve("crash-" + Util.getFormattedCurrentTime() + "-client.txt");
        Bootstrap.println(crashReport.asString(ReportType.MINECRAFT_CRASH_REPORT));
        if (client != null) {
            client.soundManager.stopAbruptly();
        }
        if (crashReport.getFile() != null) {
            Bootstrap.println("#@!@# Game crashed! Crash report saved to: #@!@# " + String.valueOf(crashReport.getFile().toAbsolutePath()));
            System.exit(-1);
        } else if (crashReport.writeToFile(path2, ReportType.MINECRAFT_CRASH_REPORT)) {
            Bootstrap.println("#@!@# Game crashed! Crash report saved to: #@!@# " + String.valueOf(path2.toAbsolutePath()));
            System.exit(-1);
        } else {
            Bootstrap.println("#@?@# Game crashed! Crash report could not be saved. #@?@#");
            System.exit(-2);
        }
    }

    public boolean forcesUnicodeFont() {
        return this.options.getForceUnicodeFont().getValue();
    }

    public CompletableFuture<Void> reloadResources() {
        return this.reloadResources(false, null);
    }

    private CompletableFuture<Void> reloadResources(boolean force, @Nullable LoadingContext loadingContext) {
        if (this.resourceReloadFuture != null) {
            return this.resourceReloadFuture;
        }
        CompletableFuture<Void> completableFuture = new CompletableFuture<Void>();
        if (!force && this.overlay instanceof SplashOverlay) {
            this.resourceReloadFuture = completableFuture;
            return completableFuture;
        }
        this.resourcePackManager.scanPacks();
        List<ResourcePack> list = this.resourcePackManager.createResourcePacks();
        if (!force) {
            this.resourceReloadLogger.reload(ResourceReloadLogger.ReloadReason.MANUAL, list);
        }
        this.setOverlay(new SplashOverlay(this, this.resourceManager.reload(Util.getMainWorkerExecutor(), this, COMPLETED_UNIT_FUTURE, list), error -> Util.ifPresentOrElse(error, throwable -> {
            if (force) {
                this.serverResourcePackLoader.onForcedReloadFailure();
                this.onForcedResourceReloadFailure();
            } else {
                this.handleResourceReloadException((Throwable)throwable, loadingContext);
            }
        }, () -> {
            this.worldRenderer.reload();
            this.resourceReloadLogger.finish();
            this.serverResourcePackLoader.onReloadSuccess();
            completableFuture.complete(null);
            this.onFinishedLoading(loadingContext);
        }), !force));
        return completableFuture;
    }

    private void checkGameData() {
        boolean bl = false;
        BlockModels lv = this.getBlockRenderManager().getModels();
        BakedModel lv2 = lv.getModelManager().getMissingModel();
        for (Block lv3 : Registries.BLOCK) {
            for (BlockState blockState : lv3.getStateManager().getStates()) {
                BakedModel lv5;
                if (blockState.getRenderType() != BlockRenderType.MODEL || (lv5 = lv.getModel(blockState)) != lv2) continue;
                LOGGER.debug("Missing model for: {}", (Object)blockState);
                bl = true;
            }
        }
        Sprite lv6 = lv2.getParticleSprite();
        for (Block lv7 : Registries.BLOCK) {
            for (BlockState lv8 : lv7.getStateManager().getStates()) {
                Sprite lv9 = lv.getModelParticleSprite(lv8);
                if (lv8.isAir() || lv9 != lv6) continue;
                LOGGER.debug("Missing particle icon for: {}", (Object)lv8);
            }
        }
        for (Item lv10 : Registries.ITEM) {
            ItemStack itemStack = lv10.getDefaultStack();
            String string = itemStack.getTranslationKey();
            String string2 = Text.translatable(string).getString();
            if (!string2.toLowerCase(Locale.ROOT).equals(lv10.getTranslationKey())) continue;
            LOGGER.debug("Missing translation for: {} {} {}", itemStack, string, lv10);
        }
        bl |= HandledScreens.isMissingScreens();
        if (bl |= EntityRenderers.isMissingRendererFactories()) {
            throw new IllegalStateException("Your game data is foobar, fix the errors above!");
        }
    }

    public LevelStorage getLevelStorage() {
        return this.levelStorage;
    }

    private void openChatScreen(String text) {
        ChatRestriction lv = this.getChatRestriction();
        if (!lv.allowsChat(this.isInSingleplayer())) {
            if (this.inGameHud.shouldShowChatDisabledScreen()) {
                this.inGameHud.setCanShowChatDisabledScreen(false);
                this.setScreen(new ConfirmLinkScreen(confirmed -> {
                    if (confirmed) {
                        Util.getOperatingSystem().open(Urls.JAVA_ACCOUNT_SETTINGS);
                    }
                    this.setScreen(null);
                }, ChatRestriction.MORE_INFO_TEXT, Urls.JAVA_ACCOUNT_SETTINGS, true));
            } else {
                Text lv2 = lv.getDescription();
                this.inGameHud.setOverlayMessage(lv2, false);
                this.narratorManager.narrate(lv2);
                this.inGameHud.setCanShowChatDisabledScreen(lv == ChatRestriction.DISABLED_BY_PROFILE);
            }
        } else {
            this.setScreen(new ChatScreen(text));
        }
    }

    public void setScreen(@Nullable Screen screen) {
        if (SharedConstants.isDevelopment && Thread.currentThread() != this.thread) {
            LOGGER.error("setScreen called from non-game thread");
        }
        if (this.currentScreen != null) {
            this.currentScreen.removed();
        } else {
            this.setNavigationType(GuiNavigationType.NONE);
        }
        if (screen == null && this.disconnecting) {
            throw new IllegalStateException("Trying to return to in-game GUI during disconnection");
        }
        if (screen == null && this.world == null) {
            screen = new TitleScreen();
        } else if (screen == null && this.player.isDead()) {
            if (this.player.showsDeathScreen()) {
                screen = new DeathScreen(null, this.world.getLevelProperties().isHardcore());
            } else {
                this.player.requestRespawn();
            }
        }
        this.currentScreen = screen;
        if (this.currentScreen != null) {
            this.currentScreen.onDisplayed();
        }
        BufferRenderer.reset();
        if (screen != null) {
            this.mouse.unlockCursor();
            KeyBinding.unpressAll();
            screen.init(this, this.window.getScaledWidth(), this.window.getScaledHeight());
            this.skipGameRender = false;
        } else {
            this.soundManager.resumeAll();
            this.mouse.lockCursor();
        }
        this.updateWindowTitle();
    }

    public void setOverlay(@Nullable Overlay overlay) {
        this.overlay = overlay;
    }

    public void stop() {
        try {
            LOGGER.info("Stopping!");
            try {
                this.narratorManager.destroy();
            } catch (Throwable throwable) {
                // empty catch block
            }
            try {
                if (this.world != null) {
                    this.world.disconnect();
                }
                this.disconnect();
            } catch (Throwable throwable) {
                // empty catch block
            }
            if (this.currentScreen != null) {
                this.currentScreen.removed();
            }
            this.close();
        } finally {
            Util.nanoTimeSupplier = System::nanoTime;
            if (this.crashReportSupplier == null) {
                System.exit(0);
            }
        }
    }

    @Override
    public void close() {
        if (this.currentGlTimerQuery != null) {
            this.currentGlTimerQuery.close();
        }
        try {
            this.telemetryManager.close();
            this.regionalComplianciesManager.close();
            this.bakedModelManager.close();
            this.fontManager.close();
            this.gameRenderer.close();
            this.worldRenderer.close();
            this.soundManager.close();
            this.particleManager.clearAtlas();
            this.statusEffectSpriteManager.close();
            this.paintingManager.close();
            this.mapDecorationsAtlasManager.close();
            this.guiAtlasManager.close();
            this.textureManager.close();
            this.resourceManager.close();
            FreeTypeUtil.release();
            Util.shutdownExecutors();
        } catch (Throwable throwable) {
            LOGGER.error("Shutdown failure!", throwable);
            throw throwable;
        } finally {
            this.windowProvider.close();
            this.window.close();
        }
    }

    private void render(boolean tick) {
        boolean bl2;
        Runnable runnable;
        this.window.setPhase("Pre render");
        if (this.window.shouldClose()) {
            this.scheduleStop();
        }
        if (this.resourceReloadFuture != null && !(this.overlay instanceof SplashOverlay)) {
            CompletableFuture<Void> completableFuture = this.resourceReloadFuture;
            this.resourceReloadFuture = null;
            this.reloadResources().thenRun(() -> completableFuture.complete(null));
        }
        while ((runnable = this.renderTaskQueue.poll()) != null) {
            runnable.run();
        }
        int i = this.renderTickCounter.beginRenderTick(Util.getMeasuringTimeMs(), tick);
        if (tick) {
            this.profiler.push("scheduledExecutables");
            this.runTasks();
            this.profiler.pop();
            this.profiler.push("tick");
            for (int j = 0; j < Math.min(10, i); ++j) {
                this.profiler.visit("clientTick");
                this.tick();
            }
            this.profiler.pop();
        }
        this.window.setPhase("Render");
        this.profiler.push("sound");
        this.soundManager.updateListenerPosition(this.gameRenderer.getCamera());
        this.profiler.pop();
        this.profiler.push("render");
        long l = Util.getMeasuringTimeNano();
        if (this.getDebugHud().shouldShowDebugHud() || this.recorder.isActive()) {
            boolean bl = bl2 = this.currentGlTimerQuery == null || this.currentGlTimerQuery.isResultAvailable();
            if (bl2) {
                GlTimer.getInstance().ifPresent(GlTimer::beginProfile);
            }
        } else {
            bl2 = false;
            this.gpuUtilizationPercentage = 0.0;
        }
        RenderSystem.clear(GlConst.GL_DEPTH_BUFFER_BIT | GlConst.GL_COLOR_BUFFER_BIT, IS_SYSTEM_MAC);
        this.framebuffer.beginWrite(true);
        BackgroundRenderer.clearFog();
        this.profiler.push("display");
        RenderSystem.enableCull();
        this.profiler.swap("mouse");
        this.mouse.tick();
        this.profiler.pop();
        if (!this.skipGameRender) {
            this.profiler.swap("gameRenderer");
            this.gameRenderer.render(this.renderTickCounter, tick);
            this.profiler.pop();
        }
        if (this.tickProfilerResult != null) {
            this.profiler.push("fpsPie");
            DrawContext lv = new DrawContext(this, this.bufferBuilders.getEntityVertexConsumers());
            this.drawProfilerResults(lv, this.tickProfilerResult);
            lv.draw();
            this.profiler.pop();
        }
        this.profiler.push("blit");
        this.framebuffer.endWrite();
        this.framebuffer.draw(this.window.getFramebufferWidth(), this.window.getFramebufferHeight());
        this.renderTime = Util.getMeasuringTimeNano() - l;
        if (bl2) {
            GlTimer.getInstance().ifPresent(glTimer -> {
                this.currentGlTimerQuery = glTimer.endProfile();
            });
        }
        this.profiler.swap("updateDisplay");
        this.window.swapBuffers();
        int k = this.getFramerateLimit();
        if (k < 260) {
            RenderSystem.limitDisplayFPS(k);
        }
        this.profiler.swap("yield");
        Thread.yield();
        this.profiler.pop();
        this.window.setPhase("Post render");
        ++this.fpsCounter;
        this.paused = this.isIntegratedServerRunning() && (this.currentScreen != null && this.currentScreen.shouldPause() || this.overlay != null && this.overlay.pausesGame()) && !this.server.isRemote();
        this.renderTickCounter.tick(this.paused);
        this.renderTickCounter.setTickFrozen(!this.shouldTick());
        long m = Util.getMeasuringTimeNano();
        long n = m - this.lastMetricsSampleTime;
        if (bl2) {
            this.metricsSampleDuration = n;
        }
        this.getDebugHud().pushToFrameLog(n);
        this.lastMetricsSampleTime = m;
        this.profiler.push("fpsUpdate");
        if (this.currentGlTimerQuery != null && this.currentGlTimerQuery.isResultAvailable()) {
            this.gpuUtilizationPercentage = (double)this.currentGlTimerQuery.queryResult() * 100.0 / (double)this.metricsSampleDuration;
        }
        while (Util.getMeasuringTimeMs() >= this.nextDebugInfoUpdateTime + 1000L) {
            Object string = this.gpuUtilizationPercentage > 0.0 ? " GPU: " + (this.gpuUtilizationPercentage > 100.0 ? String.valueOf(Formatting.RED) + "100%" : Math.round(this.gpuUtilizationPercentage) + "%") : "";
            currentFps = this.fpsCounter;
            this.fpsDebugString = String.format(Locale.ROOT, "%d fps T: %s%s%s%s B: %d%s", currentFps, k == 260 ? "inf" : Integer.valueOf(k), this.options.getEnableVsync().getValue() != false ? " vsync " : " ", this.options.getGraphicsMode().getValue(), this.options.getCloudRenderMode().getValue() == CloudRenderMode.OFF ? "" : (this.options.getCloudRenderMode().getValue() == CloudRenderMode.FAST ? " fast-clouds" : " fancy-clouds"), this.options.getBiomeBlendRadius().getValue(), string);
            this.nextDebugInfoUpdateTime += 1000L;
            this.fpsCounter = 0;
        }
        this.profiler.pop();
    }

    private Profiler startMonitor(boolean active, @Nullable TickDurationMonitor monitor) {
        Profiler lv;
        if (!active) {
            this.tickTimeTracker.disable();
            if (!this.recorder.isActive() && monitor == null) {
                return DummyProfiler.INSTANCE;
            }
        }
        if (active) {
            if (!this.tickTimeTracker.isActive()) {
                this.trackingTick = 0;
                this.tickTimeTracker.enable();
            }
            ++this.trackingTick;
            lv = this.tickTimeTracker.getProfiler();
        } else {
            lv = DummyProfiler.INSTANCE;
        }
        if (this.recorder.isActive()) {
            lv = Profiler.union(lv, this.recorder.getProfiler());
        }
        return TickDurationMonitor.tickProfiler(lv, monitor);
    }

    private void endMonitor(boolean active, @Nullable TickDurationMonitor monitor) {
        if (monitor != null) {
            monitor.endTick();
        }
        this.tickProfilerResult = active ? this.tickTimeTracker.getResult() : null;
        this.profiler = this.tickTimeTracker.getProfiler();
    }

    @Override
    public void onResolutionChanged() {
        int i = this.window.calculateScaleFactor(this.options.getGuiScale().getValue(), this.forcesUnicodeFont());
        this.window.setScaleFactor(i);
        if (this.currentScreen != null) {
            this.currentScreen.resize(this, this.window.getScaledWidth(), this.window.getScaledHeight());
        }
        Framebuffer lv = this.getFramebuffer();
        lv.resize(this.window.getFramebufferWidth(), this.window.getFramebufferHeight(), IS_SYSTEM_MAC);
        this.gameRenderer.onResized(this.window.getFramebufferWidth(), this.window.getFramebufferHeight());
        this.mouse.onResolutionChanged();
    }

    @Override
    public void onCursorEnterChanged() {
        this.mouse.setResolutionChanged();
    }

    public int getCurrentFps() {
        return currentFps;
    }

    public long getRenderTime() {
        return this.renderTime;
    }

    private int getFramerateLimit() {
        if (this.world == null && (this.currentScreen != null || this.overlay != null)) {
            return 60;
        }
        return this.window.getFramerateLimit();
    }

    private void cleanUpAfterCrash() {
        try {
            CrashMemoryReserve.releaseMemory();
            this.worldRenderer.cleanUp();
        } catch (Throwable throwable) {
            // empty catch block
        }
        try {
            System.gc();
            if (this.integratedServerRunning && this.server != null) {
                this.server.stop(true);
            }
            this.disconnect(new MessageScreen(Text.translatable("menu.savingLevel")));
        } catch (Throwable throwable) {
            // empty catch block
        }
        System.gc();
    }

    public boolean toggleDebugProfiler(Consumer<Text> chatMessageSender) {
        Consumer<Path> consumer5;
        if (this.recorder.isActive()) {
            this.stopRecorder();
            return false;
        }
        Consumer<ProfileResult> consumer2 = result -> {
            if (result == EmptyProfileResult.INSTANCE) {
                return;
            }
            int i = result.getTickSpan();
            double d = (double)result.getTimeSpan() / (double)TimeHelper.SECOND_IN_NANOS;
            this.execute(() -> chatMessageSender.accept(Text.translatable("commands.debug.stopped", String.format(Locale.ROOT, "%.2f", d), i, String.format(Locale.ROOT, "%.2f", (double)i / d))));
        };
        Consumer<Path> consumer3 = path -> {
            MutableText lv = Text.literal(path.toString()).formatted(Formatting.UNDERLINE).styled(style -> style.withClickEvent(new ClickEvent(ClickEvent.Action.OPEN_FILE, path.toFile().getParent())));
            this.execute(() -> chatMessageSender.accept(Text.translatable("debug.profiling.stop", lv)));
        };
        SystemDetails lv = MinecraftClient.addSystemDetailsToCrashReport(new SystemDetails(), this, this.languageManager, this.gameVersion, this.options);
        Consumer<List> consumer4 = files -> {
            Path path = this.saveProfilingResult(lv, (List<Path>)files);
            consumer3.accept(path);
        };
        if (this.server == null) {
            consumer5 = path -> consumer4.accept(ImmutableList.of(path));
        } else {
            this.server.addSystemDetails(lv);
            CompletableFuture completableFuture = new CompletableFuture();
            CompletableFuture completableFuture2 = new CompletableFuture();
            CompletableFuture.allOf(completableFuture, completableFuture2).thenRunAsync(() -> consumer4.accept(ImmutableList.of((Path)completableFuture.join(), (Path)completableFuture2.join())), Util.getIoWorkerExecutor());
            this.server.setupRecorder(result -> {}, completableFuture2::complete);
            consumer5 = completableFuture::complete;
        }
        this.recorder = DebugRecorder.of(new ClientSamplerSource(Util.nanoTimeSupplier, this.worldRenderer), Util.nanoTimeSupplier, Util.getIoWorkerExecutor(), new RecordDumper("client"), result -> {
            this.recorder = DummyRecorder.INSTANCE;
            consumer2.accept((ProfileResult)result);
        }, consumer5);
        return true;
    }

    private void stopRecorder() {
        this.recorder.stop();
        if (this.server != null) {
            this.server.stopRecorder();
        }
    }

    private void forceStopRecorder() {
        this.recorder.forceStop();
        if (this.server != null) {
            this.server.forceStopRecorder();
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    private Path saveProfilingResult(SystemDetails details, List<Path> files) {
        Path path;
        ServerInfo lv;
        String string = this.isInSingleplayer() ? this.getServer().getSaveProperties().getLevelName() : ((lv = this.getCurrentServerEntry()) != null ? lv.name : "unknown");
        try {
            String string2 = String.format(Locale.ROOT, "%s-%s-%s", Util.getFormattedCurrentTime(), string, SharedConstants.getGameVersion().getId());
            String string3 = PathUtil.getNextUniqueName(RecordDumper.DEBUG_PROFILING_DIRECTORY, string2, ".zip");
            path = RecordDumper.DEBUG_PROFILING_DIRECTORY.resolve(string3);
        } catch (IOException iOException) {
            throw new UncheckedIOException(iOException);
        }
        try (ZipCompressor lv2 = new ZipCompressor(path);){
            lv2.write(Paths.get("system.txt", new String[0]), details.collect());
            lv2.write(Paths.get("client", new String[0]).resolve(this.options.getOptionsFile().getName()), this.options.collectProfiledOptions());
            files.forEach(lv2::copyAll);
        } finally {
            for (Path path2 : files) {
                try {
                    FileUtils.forceDelete(path2.toFile());
                } catch (IOException iOException2) {
                    LOGGER.warn("Failed to delete temporary profiling result {}", (Object)path2, (Object)iOException2);
                }
            }
        }
        return path;
    }

    public void handleProfilerKeyPress(int digit) {
        if (this.tickProfilerResult == null) {
            return;
        }
        List<ProfilerTiming> list = this.tickProfilerResult.getTimings(this.openProfilerSection);
        if (list.isEmpty()) {
            return;
        }
        ProfilerTiming lv = list.remove(0);
        if (digit == 0) {
            int j;
            if (!lv.name.isEmpty() && (j = this.openProfilerSection.lastIndexOf(30)) >= 0) {
                this.openProfilerSection = this.openProfilerSection.substring(0, j);
            }
        } else if (--digit < list.size() && !"unspecified".equals(list.get((int)digit).name)) {
            if (!this.openProfilerSection.isEmpty()) {
                this.openProfilerSection = this.openProfilerSection + "\u001e";
            }
            this.openProfilerSection = this.openProfilerSection + list.get((int)digit).name;
        }
    }

    private void drawProfilerResults(DrawContext context, ProfileResult profileResult) {
        List<ProfilerTiming> list = profileResult.getTimings(this.openProfilerSection);
        ProfilerTiming lv = list.removeFirst();
        RenderSystem.clear(GlConst.GL_DEPTH_BUFFER_BIT, IS_SYSTEM_MAC);
        RenderSystem.setShader(GameRenderer::getPositionColorProgram);
        Matrix4f matrix4f = new Matrix4f().setOrtho(0.0f, this.window.getFramebufferWidth(), this.window.getFramebufferHeight(), 0.0f, 1000.0f, 3000.0f);
        RenderSystem.setProjectionMatrix(matrix4f, VertexSorter.BY_Z);
        Tessellator lv2 = Tessellator.getInstance();
        Matrix4fStack matrix4fStack = RenderSystem.getModelViewStack();
        matrix4fStack.pushMatrix();
        matrix4fStack.translation(0.0f, 0.0f, -2000.0f);
        RenderSystem.applyModelViewMatrix();
        int i = 160;
        int j = this.window.getFramebufferWidth() - 160 - 10;
        int k = this.window.getFramebufferHeight() - 320;
        double d = 0.0;
        for (ProfilerTiming lv3 : list) {
            float h;
            float g;
            float f;
            int o;
            int l = MathHelper.floor(lv3.parentSectionUsagePercentage / 4.0) + 1;
            BufferBuilder lv4 = lv2.begin(VertexFormat.DrawMode.TRIANGLE_FAN, VertexFormats.POSITION_COLOR);
            int m = ColorHelper.Argb.fullAlpha(lv3.getColor());
            int n = ColorHelper.Argb.mixColor(m, -8355712);
            lv4.vertex(j, k, 0.0f).color(m);
            for (o = l; o >= 0; --o) {
                f = (float)((d + lv3.parentSectionUsagePercentage * (double)o / (double)l) * 6.2831854820251465 / 100.0);
                g = MathHelper.sin(f) * 160.0f;
                h = MathHelper.cos(f) * 160.0f * 0.5f;
                lv4.vertex((float)j + g, (float)k - h, 0.0f).color(m);
            }
            BufferRenderer.drawWithGlobalProgram(lv4.end());
            lv4 = lv2.begin(VertexFormat.DrawMode.TRIANGLE_STRIP, VertexFormats.POSITION_COLOR);
            for (o = l; o >= 0; --o) {
                f = (float)((d + lv3.parentSectionUsagePercentage * (double)o / (double)l) * 6.2831854820251465 / 100.0);
                g = MathHelper.sin(f) * 160.0f;
                h = MathHelper.cos(f) * 160.0f * 0.5f;
                if (h > 0.0f) continue;
                lv4.vertex((float)j + g, (float)k - h, 0.0f).color(n);
                lv4.vertex((float)j + g, (float)k - h + 10.0f, 0.0f).color(n);
            }
            BuiltBuffer lv5 = lv4.endNullable();
            if (lv5 != null) {
                BufferRenderer.drawWithGlobalProgram(lv5);
            }
            d += lv3.parentSectionUsagePercentage;
        }
        DecimalFormat decimalFormat = new DecimalFormat("##0.00");
        decimalFormat.setDecimalFormatSymbols(DecimalFormatSymbols.getInstance(Locale.ROOT));
        String string = ProfileResult.getHumanReadableName(lv.name);
        Object string2 = "";
        if (!"unspecified".equals(string)) {
            string2 = (String)string2 + "[0] ";
        }
        string2 = string.isEmpty() ? (String)string2 + "ROOT " : (String)string2 + string + " ";
        int p = 0xFFFFFF;
        context.drawTextWithShadow(this.textRenderer, (String)string2, j - 160, k - 80 - 16, 0xFFFFFF);
        string2 = decimalFormat.format(lv.totalUsagePercentage) + "%";
        context.drawTextWithShadow(this.textRenderer, (String)string2, j + 160 - this.textRenderer.getWidth((String)string2), k - 80 - 16, 0xFFFFFF);
        for (int q = 0; q < list.size(); ++q) {
            ProfilerTiming lv6 = list.get(q);
            StringBuilder stringBuilder = new StringBuilder();
            if ("unspecified".equals(lv6.name)) {
                stringBuilder.append("[?] ");
            } else {
                stringBuilder.append("[").append(q + 1).append("] ");
            }
            Object string3 = stringBuilder.append(lv6.name).toString();
            context.drawTextWithShadow(this.textRenderer, (String)string3, j - 160, k + 80 + q * 8 + 20, lv6.getColor());
            string3 = decimalFormat.format(lv6.parentSectionUsagePercentage) + "%";
            context.drawTextWithShadow(this.textRenderer, (String)string3, j + 160 - 50 - this.textRenderer.getWidth((String)string3), k + 80 + q * 8 + 20, lv6.getColor());
            string3 = decimalFormat.format(lv6.totalUsagePercentage) + "%";
            context.drawTextWithShadow(this.textRenderer, (String)string3, j + 160 - this.textRenderer.getWidth((String)string3), k + 80 + q * 8 + 20, lv6.getColor());
        }
        matrix4fStack.popMatrix();
        RenderSystem.applyModelViewMatrix();
    }

    public void scheduleStop() {
        this.running = false;
    }

    public boolean isRunning() {
        return this.running;
    }

    public void openGameMenu(boolean pauseOnly) {
        boolean bl2;
        if (this.currentScreen != null) {
            return;
        }
        boolean bl = bl2 = this.isIntegratedServerRunning() && !this.server.isRemote();
        if (bl2) {
            this.setScreen(new GameMenuScreen(!pauseOnly));
            this.soundManager.pauseAll();
        } else {
            this.setScreen(new GameMenuScreen(true));
        }
    }

    private void handleBlockBreaking(boolean breaking) {
        if (!breaking) {
            this.attackCooldown = 0;
        }
        if (this.attackCooldown > 0 || this.player.isUsingItem()) {
            return;
        }
        if (breaking && this.crosshairTarget != null && this.crosshairTarget.getType() == HitResult.Type.BLOCK) {
            Direction lv3;
            BlockHitResult lv = (BlockHitResult)this.crosshairTarget;
            BlockPos lv2 = lv.getBlockPos();
            if (!this.world.getBlockState(lv2).isAir() && this.interactionManager.updateBlockBreakingProgress(lv2, lv3 = lv.getSide())) {
                this.particleManager.addBlockBreakingParticles(lv2, lv3);
                this.player.swingHand(Hand.MAIN_HAND);
            }
            return;
        }
        this.interactionManager.cancelBlockBreaking();
    }

    private boolean doAttack() {
        if (this.attackCooldown > 0) {
            return false;
        }
        if (this.crosshairTarget == null) {
            LOGGER.error("Null returned as 'hitResult', this shouldn't happen!");
            if (this.interactionManager.hasLimitedAttackSpeed()) {
                this.attackCooldown = 10;
            }
            return false;
        }
        if (this.player.isRiding()) {
            return false;
        }
        ItemStack lv = this.player.getStackInHand(Hand.MAIN_HAND);
        if (!lv.isItemEnabled(this.world.getEnabledFeatures())) {
            return false;
        }
        boolean bl = false;
        switch (this.crosshairTarget.getType()) {
            case ENTITY: {
                this.interactionManager.attackEntity(this.player, ((EntityHitResult)this.crosshairTarget).getEntity());
                break;
            }
            case BLOCK: {
                BlockHitResult lv2 = (BlockHitResult)this.crosshairTarget;
                BlockPos lv3 = lv2.getBlockPos();
                if (!this.world.getBlockState(lv3).isAir()) {
                    this.interactionManager.attackBlock(lv3, lv2.getSide());
                    if (!this.world.getBlockState(lv3).isAir()) break;
                    bl = true;
                    break;
                }
            }
            case MISS: {
                if (this.interactionManager.hasLimitedAttackSpeed()) {
                    this.attackCooldown = 10;
                }
                this.player.resetLastAttackedTicks();
            }
        }
        this.player.swingHand(Hand.MAIN_HAND);
        return bl;
    }

    private void doItemUse() {
        if (this.interactionManager.isBreakingBlock()) {
            return;
        }
        this.itemUseCooldown = 4;
        if (this.player.isRiding()) {
            return;
        }
        if (this.crosshairTarget == null) {
            LOGGER.warn("Null returned as 'hitResult', this shouldn't happen!");
        }
        for (Hand lv : Hand.values()) {
            ActionResult lv8;
            ItemStack lv2 = this.player.getStackInHand(lv);
            if (!lv2.isItemEnabled(this.world.getEnabledFeatures())) {
                return;
            }
            if (this.crosshairTarget != null) {
                switch (this.crosshairTarget.getType()) {
                    case ENTITY: {
                        EntityHitResult lv3 = (EntityHitResult)this.crosshairTarget;
                        Entity lv4 = lv3.getEntity();
                        if (!this.world.getWorldBorder().contains(lv4.getBlockPos())) {
                            return;
                        }
                        ActionResult lv5 = this.interactionManager.interactEntityAtLocation(this.player, lv4, lv3, lv);
                        if (!lv5.isAccepted()) {
                            lv5 = this.interactionManager.interactEntity(this.player, lv4, lv);
                        }
                        if (!lv5.isAccepted()) break;
                        if (lv5.shouldSwingHand()) {
                            this.player.swingHand(lv);
                        }
                        return;
                    }
                    case BLOCK: {
                        BlockHitResult lv6 = (BlockHitResult)this.crosshairTarget;
                        int i = lv2.getCount();
                        ActionResult lv7 = this.interactionManager.interactBlock(this.player, lv, lv6);
                        if (lv7.isAccepted()) {
                            if (lv7.shouldSwingHand()) {
                                this.player.swingHand(lv);
                                if (!lv2.isEmpty() && (lv2.getCount() != i || this.interactionManager.hasCreativeInventory())) {
                                    this.gameRenderer.firstPersonRenderer.resetEquipProgress(lv);
                                }
                            }
                            return;
                        }
                        if (lv7 != ActionResult.FAIL) break;
                        return;
                    }
                }
            }
            if (lv2.isEmpty() || !(lv8 = this.interactionManager.interactItem(this.player, lv)).isAccepted()) continue;
            if (lv8.shouldSwingHand()) {
                this.player.swingHand(lv);
            }
            this.gameRenderer.firstPersonRenderer.resetEquipProgress(lv);
            return;
        }
    }

    public MusicTracker getMusicTracker() {
        return this.musicTracker;
    }

    public void tick() {
        ++this.uptimeInTicks;
        if (this.world != null && !this.paused) {
            this.world.getTickManager().step();
        }
        if (this.itemUseCooldown > 0) {
            --this.itemUseCooldown;
        }
        this.profiler.push("gui");
        this.messageHandler.processDelayedMessages();
        this.inGameHud.tick(this.paused);
        this.profiler.pop();
        this.gameRenderer.updateCrosshairTarget(1.0f);
        this.tutorialManager.tick(this.world, this.crosshairTarget);
        this.profiler.push("gameMode");
        if (!this.paused && this.world != null) {
            this.interactionManager.tick();
        }
        this.profiler.swap("textures");
        if (this.shouldTick()) {
            this.textureManager.tick();
        }
        if (this.currentScreen == null && this.player != null) {
            if (this.player.isDead() && !(this.currentScreen instanceof DeathScreen)) {
                this.setScreen(null);
            } else if (this.player.isSleeping() && this.world != null) {
                this.setScreen(new SleepingChatScreen());
            }
        } else {
            Screen screen = this.currentScreen;
            if (screen instanceof SleepingChatScreen) {
                SleepingChatScreen lv = (SleepingChatScreen)screen;
                if (!this.player.isSleeping()) {
                    lv.closeChatIfEmpty();
                }
            }
        }
        if (this.currentScreen != null) {
            this.attackCooldown = 10000;
        }
        if (this.currentScreen != null) {
            Screen.wrapScreenError(() -> this.currentScreen.tick(), "Ticking screen", this.currentScreen.getClass().getCanonicalName());
        }
        if (!this.getDebugHud().shouldShowDebugHud()) {
            this.inGameHud.resetDebugHudChunk();
        }
        if (this.overlay == null && this.currentScreen == null) {
            this.profiler.swap("Keybindings");
            this.handleInputEvents();
            if (this.attackCooldown > 0) {
                --this.attackCooldown;
            }
        }
        if (this.world != null) {
            this.profiler.swap("gameRenderer");
            if (!this.paused) {
                this.gameRenderer.tick();
            }
            this.profiler.swap("levelRenderer");
            if (!this.paused) {
                this.worldRenderer.tick();
            }
            this.profiler.swap("level");
            if (!this.paused) {
                this.world.tickEntities();
            }
        } else if (this.gameRenderer.getPostProcessor() != null) {
            this.gameRenderer.disablePostProcessor();
        }
        if (!this.paused) {
            this.musicTracker.tick();
        }
        this.soundManager.tick(this.paused);
        if (this.world != null) {
            if (!this.paused) {
                if (!this.options.joinedFirstServer && this.isConnectedToServer()) {
                    MutableText lv2 = Text.translatable("tutorial.socialInteractions.title");
                    MutableText lv3 = Text.translatable("tutorial.socialInteractions.description", TutorialManager.keyToText("socialInteractions"));
                    this.socialInteractionsToast = new TutorialToast(TutorialToast.Type.SOCIAL_INTERACTIONS, lv2, lv3, true);
                    this.tutorialManager.add(this.socialInteractionsToast, 160);
                    this.options.joinedFirstServer = true;
                    this.options.write();
                }
                this.tutorialManager.tick();
                try {
                    this.world.tick(() -> true);
                } catch (Throwable throwable) {
                    CrashReport lv4 = CrashReport.create(throwable, "Exception in world tick");
                    if (this.world == null) {
                        CrashReportSection lv5 = lv4.addElement("Affected level");
                        lv5.add("Problem", "Level is null!");
                    } else {
                        this.world.addDetailsToCrashReport(lv4);
                    }
                    throw new CrashException(lv4);
                }
            }
            this.profiler.swap("animateTick");
            if (!this.paused && this.shouldTick()) {
                this.world.doRandomBlockDisplayTicks(this.player.getBlockX(), this.player.getBlockY(), this.player.getBlockZ());
            }
            this.profiler.swap("particles");
            if (!this.paused && this.shouldTick()) {
                this.particleManager.tick();
            }
        } else if (this.integratedServerConnection != null) {
            this.profiler.swap("pendingConnection");
            this.integratedServerConnection.tick();
        }
        this.profiler.swap("keyboard");
        this.keyboard.pollDebugCrash();
        this.profiler.pop();
    }

    private boolean shouldTick() {
        return this.world == null || this.world.getTickManager().shouldTick();
    }

    private boolean isConnectedToServer() {
        return !this.integratedServerRunning || this.server != null && this.server.isRemote();
    }

    private void handleInputEvents() {
        while (this.options.togglePerspectiveKey.wasPressed()) {
            Perspective lv = this.options.getPerspective();
            this.options.setPerspective(this.options.getPerspective().next());
            if (lv.isFirstPerson() != this.options.getPerspective().isFirstPerson()) {
                this.gameRenderer.onCameraEntitySet(this.options.getPerspective().isFirstPerson() ? this.getCameraEntity() : null);
            }
            this.worldRenderer.scheduleTerrainUpdate();
        }
        while (this.options.smoothCameraKey.wasPressed()) {
            this.options.smoothCameraEnabled = !this.options.smoothCameraEnabled;
        }
        for (int i = 0; i < 9; ++i) {
            boolean bl = this.options.saveToolbarActivatorKey.isPressed();
            boolean bl2 = this.options.loadToolbarActivatorKey.isPressed();
            if (!this.options.hotbarKeys[i].wasPressed()) continue;
            if (this.player.isSpectator()) {
                this.inGameHud.getSpectatorHud().selectSlot(i);
                continue;
            }
            if (this.player.isCreative() && this.currentScreen == null && (bl2 || bl)) {
                CreativeInventoryScreen.onHotbarKeyPress(this, i, bl2, bl);
                continue;
            }
            this.player.getInventory().selectedSlot = i;
        }
        while (this.options.socialInteractionsKey.wasPressed()) {
            if (!this.isConnectedToServer()) {
                this.player.sendMessage(SOCIAL_INTERACTIONS_NOT_AVAILABLE, true);
                this.narratorManager.narrate(SOCIAL_INTERACTIONS_NOT_AVAILABLE);
                continue;
            }
            if (this.socialInteractionsToast != null) {
                this.tutorialManager.remove(this.socialInteractionsToast);
                this.socialInteractionsToast = null;
            }
            this.setScreen(new SocialInteractionsScreen());
        }
        while (this.options.inventoryKey.wasPressed()) {
            if (this.interactionManager.hasRidingInventory()) {
                this.player.openRidingInventory();
                continue;
            }
            this.tutorialManager.onInventoryOpened();
            this.setScreen(new InventoryScreen(this.player));
        }
        while (this.options.advancementsKey.wasPressed()) {
            this.setScreen(new AdvancementsScreen(this.player.networkHandler.getAdvancementHandler()));
        }
        while (this.options.swapHandsKey.wasPressed()) {
            if (this.player.isSpectator()) continue;
            this.getNetworkHandler().sendPacket(new PlayerActionC2SPacket(PlayerActionC2SPacket.Action.SWAP_ITEM_WITH_OFFHAND, BlockPos.ORIGIN, Direction.DOWN));
        }
        while (this.options.dropKey.wasPressed()) {
            if (this.player.isSpectator() || !this.player.dropSelectedItem(Screen.hasControlDown())) continue;
            this.player.swingHand(Hand.MAIN_HAND);
        }
        while (this.options.chatKey.wasPressed()) {
            this.openChatScreen("");
        }
        if (this.currentScreen == null && this.overlay == null && this.options.commandKey.wasPressed()) {
            this.openChatScreen("/");
        }
        boolean bl3 = false;
        if (this.player.isUsingItem()) {
            if (!this.options.useKey.isPressed()) {
                this.interactionManager.stopUsingItem(this.player);
            }
            while (this.options.attackKey.wasPressed()) {
            }
            while (this.options.useKey.wasPressed()) {
            }
            while (this.options.pickItemKey.wasPressed()) {
            }
        } else {
            while (this.options.attackKey.wasPressed()) {
                bl3 |= this.doAttack();
            }
            while (this.options.useKey.wasPressed()) {
                this.doItemUse();
            }
            while (this.options.pickItemKey.wasPressed()) {
                this.doItemPick();
            }
        }
        if (this.options.useKey.isPressed() && this.itemUseCooldown == 0 && !this.player.isUsingItem()) {
            this.doItemUse();
        }
        this.handleBlockBreaking(this.currentScreen == null && !bl3 && this.options.attackKey.isPressed() && this.mouse.isCursorLocked());
    }

    public TelemetryManager getTelemetryManager() {
        return this.telemetryManager;
    }

    public double getGpuUtilizationPercentage() {
        return this.gpuUtilizationPercentage;
    }

    public ProfileKeys getProfileKeys() {
        return this.profileKeys;
    }

    public IntegratedServerLoader createIntegratedServerLoader() {
        return new IntegratedServerLoader(this, this.levelStorage);
    }

    public void startIntegratedServer(LevelStorage.Session session, ResourcePackManager dataPackManager, SaveLoader saveLoader, boolean newWorld) {
        this.disconnect();
        this.worldGenProgressTracker.set(null);
        Instant instant = Instant.now();
        try {
            session.backupLevelDataFile(saveLoader.combinedDynamicRegistries().getCombinedRegistryManager(), saveLoader.saveProperties());
            ApiServices lv = ApiServices.create(this.authenticationService, this.runDirectory);
            lv.userCache().setExecutor(this);
            SkullBlockEntity.setServices(lv, this);
            UserCache.setUseRemote(false);
            this.server = MinecraftServer.startServer(thread -> new IntegratedServer((Thread)thread, this, session, dataPackManager, saveLoader, lv, spawnChunkRadius -> {
                WorldGenerationProgressTracker lv = WorldGenerationProgressTracker.create(spawnChunkRadius + 0);
                this.worldGenProgressTracker.set(lv);
                return QueueingWorldGenerationProgressListener.create(lv, this.renderTaskQueue::add);
            }));
            this.integratedServerRunning = true;
            this.ensureAbuseReportContext(ReporterEnvironment.ofIntegratedServer());
            this.quickPlayLogger.setWorld(QuickPlayLogger.WorldType.SINGLEPLAYER, session.getDirectoryName(), saveLoader.saveProperties().getLevelName());
        } catch (Throwable throwable) {
            CrashReport lv2 = CrashReport.create(throwable, "Starting integrated server");
            CrashReportSection lv3 = lv2.addElement("Starting integrated server");
            lv3.add("Level ID", session.getDirectoryName());
            lv3.add("Level Name", () -> saveLoader.saveProperties().getLevelName());
            throw new CrashException(lv2);
        }
        while (this.worldGenProgressTracker.get() == null) {
            Thread.yield();
        }
        LevelLoadingScreen lv4 = new LevelLoadingScreen(this.worldGenProgressTracker.get());
        this.setScreen(lv4);
        this.profiler.push("waitForServer");
        while (!this.server.isLoading() || this.overlay != null) {
            lv4.tick();
            this.render(false);
            try {
                Thread.sleep(16L);
            } catch (InterruptedException lv2) {
                // empty catch block
            }
            this.printCrashReport();
        }
        this.profiler.pop();
        Duration duration = Duration.between(instant, Instant.now());
        SocketAddress socketAddress = this.server.getNetworkIo().bindLocal();
        ClientConnection lv5 = ClientConnection.connectLocal(socketAddress);
        lv5.connect(socketAddress.toString(), 0, new ClientLoginNetworkHandler(lv5, this, null, null, newWorld, duration, status -> {}, null));
        lv5.send(new LoginHelloC2SPacket(this.getSession().getUsername(), this.getSession().getUuidOrNull()));
        this.integratedServerConnection = lv5;
    }

    public void joinWorld(ClientWorld world, DownloadingTerrainScreen.WorldEntryReason worldEntryReason) {
        this.reset(new DownloadingTerrainScreen(() -> false, worldEntryReason));
        this.world = world;
        this.setWorld(world);
        if (!this.integratedServerRunning) {
            ApiServices lv = ApiServices.create(this.authenticationService, this.runDirectory);
            lv.userCache().setExecutor(this);
            SkullBlockEntity.setServices(lv, this);
            UserCache.setUseRemote(false);
        }
    }

    public void disconnect() {
        this.disconnect(new ProgressScreen(true), false);
    }

    public void disconnect(Screen disconnectionScreen) {
        this.disconnect(disconnectionScreen, false);
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public void disconnect(Screen disconnectionScreen, boolean transferring) {
        ClientPlayNetworkHandler lv = this.getNetworkHandler();
        if (lv != null) {
            this.cancelTasks();
            lv.unloadWorld();
            if (!transferring) {
                this.onDisconnected();
            }
        }
        this.socialInteractionsManager.unloadBlockList();
        if (this.recorder.isActive()) {
            this.forceStopRecorder();
        }
        IntegratedServer lv2 = this.server;
        this.server = null;
        this.gameRenderer.reset();
        this.interactionManager = null;
        this.narratorManager.clear();
        this.disconnecting = true;
        try {
            this.reset(disconnectionScreen);
            if (this.world != null) {
                if (lv2 != null) {
                    this.profiler.push("waitForServer");
                    while (!lv2.isStopping()) {
                        this.render(false);
                    }
                    this.profiler.pop();
                }
                this.inGameHud.clear();
                this.integratedServerRunning = false;
            }
            this.world = null;
            this.setWorld(null);
            this.player = null;
        } finally {
            this.disconnecting = false;
        }
        SkullBlockEntity.clearServices();
    }

    public void onDisconnected() {
        this.serverResourcePackLoader.clear();
        this.runTasks();
    }

    public void enterReconfiguration(Screen reconfigurationScreen) {
        ClientPlayNetworkHandler lv = this.getNetworkHandler();
        if (lv != null) {
            lv.clearWorld();
        }
        if (this.recorder.isActive()) {
            this.forceStopRecorder();
        }
        this.gameRenderer.reset();
        this.interactionManager = null;
        this.narratorManager.clear();
        this.disconnecting = true;
        try {
            this.reset(reconfigurationScreen);
            this.inGameHud.clear();
            this.world = null;
            this.setWorld(null);
            this.player = null;
        } finally {
            this.disconnecting = false;
        }
        SkullBlockEntity.clearServices();
    }

    private void reset(Screen resettingScreen) {
        this.profiler.push("forcedTick");
        this.soundManager.stopAll();
        this.cameraEntity = null;
        this.integratedServerConnection = null;
        this.setScreen(resettingScreen);
        this.render(false);
        this.profiler.pop();
    }

    public void setScreenAndRender(Screen screen) {
        this.profiler.push("forcedTick");
        this.setScreen(screen);
        this.render(false);
        this.profiler.pop();
    }

    private void setWorld(@Nullable ClientWorld world) {
        this.worldRenderer.setWorld(world);
        this.particleManager.setWorld(world);
        this.blockEntityRenderDispatcher.setWorld(world);
        this.updateWindowTitle();
    }

    private UserApiService.UserProperties getUserProperties() {
        return this.userPropertiesFuture.join();
    }

    public boolean isOptionalTelemetryEnabled() {
        return this.isOptionalTelemetryEnabledByApi() && this.options.getTelemetryOptInExtra().getValue() != false;
    }

    public boolean isOptionalTelemetryEnabledByApi() {
        return this.isTelemetryEnabledByApi() && this.getUserProperties().flag(UserApiService.UserFlag.OPTIONAL_TELEMETRY_AVAILABLE);
    }

    public boolean isTelemetryEnabledByApi() {
        if (SharedConstants.isDevelopment) {
            return false;
        }
        return this.getUserProperties().flag(UserApiService.UserFlag.TELEMETRY_ENABLED);
    }

    public boolean isMultiplayerEnabled() {
        return this.multiplayerEnabled && this.getUserProperties().flag(UserApiService.UserFlag.SERVERS_ALLOWED) && this.getMultiplayerBanDetails() == null && !this.isUsernameBanned();
    }

    public boolean isRealmsEnabled() {
        return this.getUserProperties().flag(UserApiService.UserFlag.REALMS_ALLOWED) && this.getMultiplayerBanDetails() == null;
    }

    @Nullable
    public BanDetails getMultiplayerBanDetails() {
        return this.getUserProperties().bannedScopes().get("MULTIPLAYER");
    }

    public boolean isUsernameBanned() {
        com.mojang.authlib.yggdrasil.ProfileResult profileResult = this.gameProfileFuture.getNow(null);
        return profileResult != null && profileResult.actions().contains((Object)ProfileActionType.FORCED_NAME_CHANGE);
    }

    public boolean shouldBlockMessages(UUID sender) {
        if (!this.getChatRestriction().allowsChat(false)) {
            return (this.player == null || !sender.equals(this.player.getUuid())) && !sender.equals(Util.NIL_UUID);
        }
        return this.socialInteractionsManager.isPlayerMuted(sender);
    }

    public ChatRestriction getChatRestriction() {
        if (this.options.getChatVisibility().getValue() == ChatVisibility.HIDDEN) {
            return ChatRestriction.DISABLED_BY_OPTIONS;
        }
        if (!this.onlineChatEnabled) {
            return ChatRestriction.DISABLED_BY_LAUNCHER;
        }
        if (!this.getUserProperties().flag(UserApiService.UserFlag.CHAT_ALLOWED)) {
            return ChatRestriction.DISABLED_BY_PROFILE;
        }
        return ChatRestriction.ENABLED;
    }

    public final boolean isDemo() {
        return this.isDemo;
    }

    @Nullable
    public ClientPlayNetworkHandler getNetworkHandler() {
        return this.player == null ? null : this.player.networkHandler;
    }

    public static boolean isHudEnabled() {
        return !MinecraftClient.instance.options.hudHidden;
    }

    public static boolean isFancyGraphicsOrBetter() {
        return MinecraftClient.instance.options.getGraphicsMode().getValue().getId() >= GraphicsMode.FANCY.getId();
    }

    public static boolean isFabulousGraphicsOrBetter() {
        return !MinecraftClient.instance.gameRenderer.isRenderingPanorama() && MinecraftClient.instance.options.getGraphicsMode().getValue().getId() >= GraphicsMode.FABULOUS.getId();
    }

    public static boolean isAmbientOcclusionEnabled() {
        return MinecraftClient.instance.options.getAo().getValue();
    }

    private void doItemPick() {
        ItemStack lv6;
        if (this.crosshairTarget == null || this.crosshairTarget.getType() == HitResult.Type.MISS) {
            return;
        }
        boolean bl = this.player.getAbilities().creativeMode;
        BlockEntity lv = null;
        HitResult.Type lv2 = this.crosshairTarget.getType();
        if (lv2 == HitResult.Type.BLOCK) {
            BlockPos lv3 = ((BlockHitResult)this.crosshairTarget).getBlockPos();
            BlockState lv4 = this.world.getBlockState(lv3);
            if (lv4.isAir()) {
                return;
            }
            Block lv5 = lv4.getBlock();
            lv6 = lv5.getPickStack(this.world, lv3, lv4);
            if (lv6.isEmpty()) {
                return;
            }
            if (bl && Screen.hasControlDown() && lv4.hasBlockEntity()) {
                lv = this.world.getBlockEntity(lv3);
            }
        } else if (lv2 == HitResult.Type.ENTITY && bl) {
            Entity lv7 = ((EntityHitResult)this.crosshairTarget).getEntity();
            lv6 = lv7.getPickBlockStack();
            if (lv6 == null) {
                return;
            }
        } else {
            return;
        }
        if (lv6.isEmpty()) {
            String string = "";
            if (lv2 == HitResult.Type.BLOCK) {
                string = Registries.BLOCK.getId(this.world.getBlockState(((BlockHitResult)this.crosshairTarget).getBlockPos()).getBlock()).toString();
            } else if (lv2 == HitResult.Type.ENTITY) {
                string = Registries.ENTITY_TYPE.getId(((EntityHitResult)this.crosshairTarget).getEntity().getType()).toString();
            }
            LOGGER.warn("Picking on: [{}] {} gave null item", (Object)lv2, (Object)string);
            return;
        }
        PlayerInventory lv8 = this.player.getInventory();
        if (lv != null) {
            this.addBlockEntityNbt(lv6, lv, this.world.getRegistryManager());
        }
        int i = lv8.getSlotWithStack(lv6);
        if (bl) {
            lv8.addPickBlock(lv6);
            this.interactionManager.clickCreativeStack(this.player.getStackInHand(Hand.MAIN_HAND), 36 + lv8.selectedSlot);
        } else if (i != -1) {
            if (PlayerInventory.isValidHotbarIndex(i)) {
                lv8.selectedSlot = i;
            } else {
                this.interactionManager.pickFromInventory(i);
            }
        }
    }

    private void addBlockEntityNbt(ItemStack stack, BlockEntity blockEntity, DynamicRegistryManager registryManager) {
        NbtCompound lv = blockEntity.createComponentlessNbtWithIdentifyingData(registryManager);
        blockEntity.removeFromCopiedStackNbt(lv);
        BlockItem.setBlockEntityData(stack, blockEntity.getType(), lv);
        stack.applyComponentsFrom(blockEntity.createComponentMap());
    }

    public CrashReport addDetailsToCrashReport(CrashReport report) {
        SystemDetails lv = report.getSystemDetailsSection();
        MinecraftClient.addSystemDetailsToCrashReport(lv, this, this.languageManager, this.gameVersion, this.options);
        this.addUptimesToCrashReport(report.addElement("Uptime"));
        if (this.world != null) {
            this.world.addDetailsToCrashReport(report);
        }
        if (this.server != null) {
            this.server.addSystemDetails(lv);
        }
        this.resourceReloadLogger.addReloadSection(report);
        return report;
    }

    public static void addSystemDetailsToCrashReport(@Nullable MinecraftClient client, @Nullable LanguageManager languageManager, String version, @Nullable GameOptions options, CrashReport report) {
        SystemDetails lv = report.getSystemDetailsSection();
        MinecraftClient.addSystemDetailsToCrashReport(lv, client, languageManager, version, options);
    }

    private static String formatSeconds(double seconds) {
        return String.format(Locale.ROOT, "%.3fs", seconds);
    }

    private void addUptimesToCrashReport(CrashReportSection section) {
        section.add("JVM uptime", () -> MinecraftClient.formatSeconds((double)ManagementFactory.getRuntimeMXBean().getUptime() / 1000.0));
        section.add("Wall uptime", () -> MinecraftClient.formatSeconds((double)(System.currentTimeMillis() - this.startTime) / 1000.0));
        section.add("High-res time", () -> MinecraftClient.formatSeconds((double)Util.getMeasuringTimeMs() / 1000.0));
        section.add("Client ticks", () -> String.format(Locale.ROOT, "%d ticks / %.3fs", this.uptimeInTicks, (double)this.uptimeInTicks / 20.0));
    }

    private static SystemDetails addSystemDetailsToCrashReport(SystemDetails systemDetails, @Nullable MinecraftClient client, @Nullable LanguageManager languageManager, String version, @Nullable GameOptions options) {
        systemDetails.addSection("Launched Version", () -> version);
        String string2 = MinecraftClient.getLauncherBrand();
        if (string2 != null) {
            systemDetails.addSection("Launcher name", string2);
        }
        systemDetails.addSection("Backend library", RenderSystem::getBackendDescription);
        systemDetails.addSection("Backend API", RenderSystem::getApiDescription);
        systemDetails.addSection("Window size", () -> client != null ? arg.window.getFramebufferWidth() + "x" + arg.window.getFramebufferHeight() : "<not initialized>");
        systemDetails.addSection("GFLW Platform", Window::getGlfwPlatform);
        systemDetails.addSection("GL Caps", RenderSystem::getCapsString);
        systemDetails.addSection("GL debug messages", () -> GlDebug.isDebugMessageEnabled() ? String.join((CharSequence)"\n", GlDebug.collectDebugMessages()) : "<disabled>");
        systemDetails.addSection("Is Modded", () -> MinecraftClient.getModStatus().getMessage());
        systemDetails.addSection("Universe", () -> client != null ? Long.toHexString(arg.field_46550) : "404");
        systemDetails.addSection("Type", "Client (map_client.txt)");
        if (options != null) {
            String string3;
            if (client != null && (string3 = client.getVideoWarningManager().getWarningsAsString()) != null) {
                systemDetails.addSection("GPU Warnings", string3);
            }
            systemDetails.addSection("Graphics mode", options.getGraphicsMode().getValue().toString());
            systemDetails.addSection("Render Distance", options.getClampedViewDistance() + "/" + String.valueOf(options.getViewDistance().getValue()) + " chunks");
        }
        if (client != null) {
            systemDetails.addSection("Resource Packs", () -> ResourcePackManager.listPacks(client.getResourcePackManager().getEnabledProfiles()));
        }
        if (languageManager != null) {
            systemDetails.addSection("Current Language", () -> languageManager.getLanguage());
        }
        systemDetails.addSection("Locale", String.valueOf(Locale.getDefault()));
        systemDetails.addSection("System encoding", () -> System.getProperty("sun.jnu.encoding", "<not set>"));
        systemDetails.addSection("File encoding", () -> System.getProperty("file.encoding", "<not set>"));
        systemDetails.addSection("CPU", GlDebugInfo::getCpuInfo);
        return systemDetails;
    }

    public static MinecraftClient getInstance() {
        return instance;
    }

    public CompletableFuture<Void> reloadResourcesConcurrently() {
        return this.submit(this::reloadResources).thenCompose(future -> future);
    }

    public void ensureAbuseReportContext(ReporterEnvironment environment) {
        if (!this.abuseReportContext.environmentEquals(environment)) {
            this.abuseReportContext = AbuseReportContext.create(environment, this.userApiService);
        }
    }

    @Nullable
    public ServerInfo getCurrentServerEntry() {
        return Nullables.map(this.getNetworkHandler(), ClientPlayNetworkHandler::getServerInfo);
    }

    public boolean isInSingleplayer() {
        return this.integratedServerRunning;
    }

    public boolean isIntegratedServerRunning() {
        return this.integratedServerRunning && this.server != null;
    }

    @Nullable
    public IntegratedServer getServer() {
        return this.server;
    }

    public boolean isConnectedToLocalServer() {
        IntegratedServer lv = this.getServer();
        return lv != null && !lv.isRemote();
    }

    public boolean uuidEquals(UUID uuid) {
        return uuid.equals(this.getSession().getUuidOrNull());
    }

    public Session getSession() {
        return this.session;
    }

    public GameProfile getGameProfile() {
        com.mojang.authlib.yggdrasil.ProfileResult profileResult = this.gameProfileFuture.join();
        if (profileResult != null) {
            return profileResult.profile();
        }
        return new GameProfile(this.session.getUuidOrNull(), this.session.getUsername());
    }

    public Proxy getNetworkProxy() {
        return this.networkProxy;
    }

    public TextureManager getTextureManager() {
        return this.textureManager;
    }

    public ResourceManager getResourceManager() {
        return this.resourceManager;
    }

    public ResourcePackManager getResourcePackManager() {
        return this.resourcePackManager;
    }

    public DefaultResourcePack getDefaultResourcePack() {
        return this.defaultResourcePack;
    }

    public ServerResourcePackLoader getServerResourcePackProvider() {
        return this.serverResourcePackLoader;
    }

    public Path getResourcePackDir() {
        return this.resourcePackDir;
    }

    public LanguageManager getLanguageManager() {
        return this.languageManager;
    }

    public Function<Identifier, Sprite> getSpriteAtlas(Identifier id) {
        return this.bakedModelManager.getAtlas(id)::getSprite;
    }

    public boolean isPaused() {
        return this.paused;
    }

    public VideoWarningManager getVideoWarningManager() {
        return this.videoWarningManager;
    }

    public SoundManager getSoundManager() {
        return this.soundManager;
    }

    public MusicSound getMusicType() {
        MusicSound lv = Nullables.map(this.currentScreen, Screen::getMusic);
        if (lv != null) {
            return lv;
        }
        if (this.player != null) {
            if (this.player.getWorld().getRegistryKey() == World.END) {
                if (this.inGameHud.getBossBarHud().shouldPlayDragonMusic()) {
                    return MusicType.DRAGON;
                }
                return MusicType.END;
            }
            RegistryEntry<Biome> lv2 = this.player.getWorld().getBiome(this.player.getBlockPos());
            if (this.musicTracker.isPlayingType(MusicType.UNDERWATER) || this.player.isSubmergedInWater() && lv2.isIn(BiomeTags.PLAYS_UNDERWATER_MUSIC)) {
                return MusicType.UNDERWATER;
            }
            if (this.player.getWorld().getRegistryKey() != World.NETHER && this.player.getAbilities().creativeMode && this.player.getAbilities().allowFlying) {
                return MusicType.CREATIVE;
            }
            return lv2.value().getMusic().orElse(MusicType.GAME);
        }
        return MusicType.MENU;
    }

    public MinecraftSessionService getSessionService() {
        return this.sessionService;
    }

    public PlayerSkinProvider getSkinProvider() {
        return this.skinProvider;
    }

    @Nullable
    public Entity getCameraEntity() {
        return this.cameraEntity;
    }

    public void setCameraEntity(Entity entity) {
        this.cameraEntity = entity;
        this.gameRenderer.onCameraEntitySet(entity);
    }

    public boolean hasOutline(Entity entity) {
        return entity.isGlowing() || this.player != null && this.player.isSpectator() && this.options.spectatorOutlinesKey.isPressed() && entity.getType() == EntityType.PLAYER;
    }

    @Override
    protected Thread getThread() {
        return this.thread;
    }

    @Override
    protected Runnable createTask(Runnable runnable) {
        return runnable;
    }

    @Override
    protected boolean canExecute(Runnable task) {
        return true;
    }

    public BlockRenderManager getBlockRenderManager() {
        return this.blockRenderManager;
    }

    public EntityRenderDispatcher getEntityRenderDispatcher() {
        return this.entityRenderDispatcher;
    }

    public BlockEntityRenderDispatcher getBlockEntityRenderDispatcher() {
        return this.blockEntityRenderDispatcher;
    }

    public ItemRenderer getItemRenderer() {
        return this.itemRenderer;
    }

    public DataFixer getDataFixer() {
        return this.dataFixer;
    }

    public RenderTickCounter getRenderTickCounter() {
        return this.renderTickCounter;
    }

    public BlockColors getBlockColors() {
        return this.blockColors;
    }

    public boolean hasReducedDebugInfo() {
        return this.player != null && this.player.hasReducedDebugInfo() || this.options.getReducedDebugInfo().getValue() != false;
    }

    public ToastManager getToastManager() {
        return this.toastManager;
    }

    public TutorialManager getTutorialManager() {
        return this.tutorialManager;
    }

    public boolean isWindowFocused() {
        return this.windowFocused;
    }

    public HotbarStorage getCreativeHotbarStorage() {
        return this.creativeHotbarStorage;
    }

    public BakedModelManager getBakedModelManager() {
        return this.bakedModelManager;
    }

    public PaintingManager getPaintingManager() {
        return this.paintingManager;
    }

    public StatusEffectSpriteManager getStatusEffectSpriteManager() {
        return this.statusEffectSpriteManager;
    }

    public MapDecorationsAtlasManager getMapDecorationsAtlasManager() {
        return this.mapDecorationsAtlasManager;
    }

    public GuiAtlasManager getGuiAtlasManager() {
        return this.guiAtlasManager;
    }

    @Override
    public void onWindowFocusChanged(boolean focused) {
        this.windowFocused = focused;
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public Text takePanorama(File directory, int width, int height) {
        int k = this.window.getFramebufferWidth();
        int l = this.window.getFramebufferHeight();
        SimpleFramebuffer lv = new SimpleFramebuffer(width, height, true, IS_SYSTEM_MAC);
        float f = this.player.getPitch();
        float g = this.player.getYaw();
        float h = this.player.prevPitch;
        float m = this.player.prevYaw;
        this.gameRenderer.setBlockOutlineEnabled(false);
        try {
            this.gameRenderer.setRenderingPanorama(true);
            this.worldRenderer.reloadTransparencyPostProcessor();
            this.window.setFramebufferWidth(width);
            this.window.setFramebufferHeight(height);
            for (int n = 0; n < 6; ++n) {
                switch (n) {
                    case 0: {
                        this.player.setYaw(g);
                        this.player.setPitch(0.0f);
                        break;
                    }
                    case 1: {
                        this.player.setYaw((g + 90.0f) % 360.0f);
                        this.player.setPitch(0.0f);
                        break;
                    }
                    case 2: {
                        this.player.setYaw((g + 180.0f) % 360.0f);
                        this.player.setPitch(0.0f);
                        break;
                    }
                    case 3: {
                        this.player.setYaw((g - 90.0f) % 360.0f);
                        this.player.setPitch(0.0f);
                        break;
                    }
                    case 4: {
                        this.player.setYaw(g);
                        this.player.setPitch(-90.0f);
                        break;
                    }
                    default: {
                        this.player.setYaw(g);
                        this.player.setPitch(90.0f);
                    }
                }
                this.player.prevYaw = this.player.getYaw();
                this.player.prevPitch = this.player.getPitch();
                lv.beginWrite(true);
                this.gameRenderer.renderWorld(RenderTickCounter.ONE);
                try {
                    Thread.sleep(10L);
                } catch (InterruptedException interruptedException) {
                    // empty catch block
                }
                ScreenshotRecorder.saveScreenshot(directory, "panorama_" + n + ".png", lv, message -> {});
            }
            MutableText lv2 = Text.literal(directory.getName()).formatted(Formatting.UNDERLINE).styled(style -> style.withClickEvent(new ClickEvent(ClickEvent.Action.OPEN_FILE, directory.getAbsolutePath())));
            MutableText mutableText = Text.translatable("screenshot.success", lv2);
            return mutableText;
        } catch (Exception exception) {
            LOGGER.error("Couldn't save image", exception);
            MutableText mutableText = Text.translatable("screenshot.failure", exception.getMessage());
            return mutableText;
        } finally {
            this.player.setPitch(f);
            this.player.setYaw(g);
            this.player.prevPitch = h;
            this.player.prevYaw = m;
            this.gameRenderer.setBlockOutlineEnabled(true);
            this.window.setFramebufferWidth(k);
            this.window.setFramebufferHeight(l);
            lv.delete();
            this.gameRenderer.setRenderingPanorama(false);
            this.worldRenderer.reloadTransparencyPostProcessor();
            this.getFramebuffer().beginWrite(true);
        }
    }

    private Text takeHugeScreenshot(File gameDirectory, int unitWidth, int unitHeight, int width, int height) {
        try {
            ByteBuffer byteBuffer = GlDebugInfo.allocateMemory(unitWidth * unitHeight * 3);
            ScreenshotRecorder lv = new ScreenshotRecorder(gameDirectory, width, height, unitHeight);
            float f = (float)width / (float)unitWidth;
            float g = (float)height / (float)unitHeight;
            float h = f > g ? f : g;
            for (int m = (height - 1) / unitHeight * unitHeight; m >= 0; m -= unitHeight) {
                for (int n = 0; n < width; n += unitWidth) {
                    RenderSystem.setShaderTexture(0, SpriteAtlasTexture.BLOCK_ATLAS_TEXTURE);
                    float o = (float)(width - unitWidth) / 2.0f * 2.0f - (float)(n * 2);
                    float p = (float)(height - unitHeight) / 2.0f * 2.0f - (float)(m * 2);
                    this.gameRenderer.renderWithZoom(h, o /= (float)unitWidth, p /= (float)unitHeight);
                    byteBuffer.clear();
                    RenderSystem.pixelStore(3333, 1);
                    RenderSystem.pixelStore(3317, 1);
                    RenderSystem.readPixels(0, 0, unitWidth, unitHeight, 32992, 5121, byteBuffer);
                    lv.getIntoBuffer(byteBuffer, n, m, unitWidth, unitHeight);
                }
                lv.writeToStream();
            }
            File file2 = lv.finish();
            GlDebugInfo.freeMemory(byteBuffer);
            MutableText lv2 = Text.literal(file2.getName()).formatted(Formatting.UNDERLINE).styled(style -> style.withClickEvent(new ClickEvent(ClickEvent.Action.OPEN_FILE, file2.getAbsolutePath())));
            return Text.translatable("screenshot.success", lv2);
        } catch (Exception exception) {
            LOGGER.warn("Couldn't save screenshot", exception);
            return Text.translatable("screenshot.failure", exception.getMessage());
        }
    }

    public Profiler getProfiler() {
        return this.profiler;
    }

    @Nullable
    public WorldGenerationProgressTracker getWorldGenerationProgressTracker() {
        return this.worldGenProgressTracker.get();
    }

    public SplashTextResourceSupplier getSplashTextLoader() {
        return this.splashTextLoader;
    }

    @Nullable
    public Overlay getOverlay() {
        return this.overlay;
    }

    public SocialInteractionsManager getSocialInteractionsManager() {
        return this.socialInteractionsManager;
    }

    public Window getWindow() {
        return this.window;
    }

    public DebugHud getDebugHud() {
        return this.inGameHud.getDebugHud();
    }

    public BufferBuilderStorage getBufferBuilders() {
        return this.bufferBuilders;
    }

    public void setMipmapLevels(int mipmapLevels) {
        this.bakedModelManager.setMipmapLevels(mipmapLevels);
    }

    public EntityModelLoader getEntityModelLoader() {
        return this.entityModelLoader;
    }

    public boolean shouldFilterText() {
        return this.getUserProperties().flag(UserApiService.UserFlag.PROFANITY_FILTER_ENABLED);
    }

    public void loadBlockList() {
        this.socialInteractionsManager.loadBlockList();
        this.getProfileKeys().fetchKeyPair();
    }

    @Nullable
    public SignatureVerifier getServicesSignatureVerifier() {
        return SignatureVerifier.create(this.authenticationService.getServicesKeySet(), ServicesKeyType.PROFILE_KEY);
    }

    public boolean providesProfileKeys() {
        return !this.authenticationService.getServicesKeySet().keys(ServicesKeyType.PROFILE_KEY).isEmpty();
    }

    public GuiNavigationType getNavigationType() {
        return this.navigationType;
    }

    public void setNavigationType(GuiNavigationType navigationType) {
        this.navigationType = navigationType;
    }

    public NarratorManager getNarratorManager() {
        return this.narratorManager;
    }

    public MessageHandler getMessageHandler() {
        return this.messageHandler;
    }

    public AbuseReportContext getAbuseReportContext() {
        return this.abuseReportContext;
    }

    public RealmsPeriodicCheckers getRealmsPeriodicCheckers() {
        return this.realmsPeriodicCheckers;
    }

    public QuickPlayLogger getQuickPlayLogger() {
        return this.quickPlayLogger;
    }

    public CommandHistoryManager getCommandHistoryManager() {
        return this.commandHistoryManager;
    }

    public SymlinkFinder getSymlinkFinder() {
        return this.symlinkFinder;
    }

    private float getTargetMillisPerTick(float millis) {
        TickManager lv;
        if (this.world != null && (lv = this.world.getTickManager()).shouldTick()) {
            return Math.max(millis, lv.getMillisPerTick());
        }
        return millis;
    }

    @Nullable
    public static String getLauncherBrand() {
        return System.getProperty("minecraft.launcher.brand");
    }

    static {
        LOGGER = LogUtils.getLogger();
        IS_SYSTEM_MAC = Util.getOperatingSystem() == Util.OperatingSystem.OSX;
        DEFAULT_FONT_ID = Identifier.ofVanilla("default");
        UNICODE_FONT_ID = Identifier.ofVanilla("uniform");
        ALT_TEXT_RENDERER_ID = Identifier.ofVanilla("alt");
        REGIONAL_COMPLIANCIES_ID = Identifier.ofVanilla("regional_compliancies.json");
        COMPLETED_UNIT_FUTURE = CompletableFuture.completedFuture(Unit.INSTANCE);
        SOCIAL_INTERACTIONS_NOT_AVAILABLE = Text.translatable("multiplayer.socialInteractions.not_available");
    }

    @Environment(value=EnvType.CLIENT)
    record LoadingContext(RealmsClient realmsClient, RunArgs.QuickPlay quickPlayData) {
    }

    @Environment(value=EnvType.CLIENT)
    public static enum ChatRestriction {
        ENABLED(ScreenTexts.EMPTY){

            @Override
            public boolean allowsChat(boolean singlePlayer) {
                return true;
            }
        }
        ,
        DISABLED_BY_OPTIONS(Text.translatable("chat.disabled.options").formatted(Formatting.RED)){

            @Override
            public boolean allowsChat(boolean singlePlayer) {
                return false;
            }
        }
        ,
        DISABLED_BY_LAUNCHER(Text.translatable("chat.disabled.launcher").formatted(Formatting.RED)){

            @Override
            public boolean allowsChat(boolean singlePlayer) {
                return singlePlayer;
            }
        }
        ,
        DISABLED_BY_PROFILE(Text.translatable("chat.disabled.profile", Text.keybind(MinecraftClient.instance.options.chatKey.getTranslationKey())).formatted(Formatting.RED)){

            @Override
            public boolean allowsChat(boolean singlePlayer) {
                return singlePlayer;
            }
        };

        static final Text MORE_INFO_TEXT;
        private final Text description;

        ChatRestriction(Text description) {
            this.description = description;
        }

        public Text getDescription() {
            return this.description;
        }

        public abstract boolean allowsChat(boolean var1);

        static {
            MORE_INFO_TEXT = Text.translatable("chat.disabled.profile.moreInfo");
        }
    }
}

