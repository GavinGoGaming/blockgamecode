/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 * 
 * Could not load the following classes:
 *  net.fabricmc.api.EnvType
 *  net.fabricmc.api.Environment
 *  org.jetbrains.annotations.Nullable
 */
package net.minecraft.client.gui.screen.world;

import com.google.common.collect.ImmutableList;
import com.google.gson.JsonElement;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.datafixers.util.Pair;
import com.mojang.logging.LogUtils;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.JsonOps;
import com.mojang.serialization.Lifecycle;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.FileVisitOption;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.FileAttribute;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.OptionalLong;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.function.Consumer;
import java.util.stream.Stream;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.SharedConstants;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.Drawable;
import net.minecraft.client.gui.Element;
import net.minecraft.client.gui.ScreenRect;
import net.minecraft.client.gui.Selectable;
import net.minecraft.client.gui.screen.ConfirmScreen;
import net.minecraft.client.gui.screen.MessageScreen;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.pack.ExperimentalWarningScreen;
import net.minecraft.client.gui.screen.pack.PackScreen;
import net.minecraft.client.gui.screen.world.EditGameRulesScreen;
import net.minecraft.client.gui.screen.world.ExperimentsScreen;
import net.minecraft.client.gui.screen.world.LevelScreenProvider;
import net.minecraft.client.gui.screen.world.WorldCreator;
import net.minecraft.client.gui.screen.world.WorldScreenOptionGrid;
import net.minecraft.client.gui.tab.GridScreenTab;
import net.minecraft.client.gui.tab.TabManager;
import net.minecraft.client.gui.tooltip.Tooltip;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.CyclingButtonWidget;
import net.minecraft.client.gui.widget.DirectionalLayoutWidget;
import net.minecraft.client.gui.widget.GridWidget;
import net.minecraft.client.gui.widget.LayoutWidgets;
import net.minecraft.client.gui.widget.Positioner;
import net.minecraft.client.gui.widget.TabNavigationWidget;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.client.gui.widget.ThreePartsLayoutWidget;
import net.minecraft.client.toast.SystemToast;
import net.minecraft.client.world.GeneratorOptionsHolder;
import net.minecraft.registry.CombinedDynamicRegistries;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.RegistryOps;
import net.minecraft.registry.ServerDynamicRegistryType;
import net.minecraft.resource.DataConfiguration;
import net.minecraft.resource.DataPackSettings;
import net.minecraft.resource.ResourcePackManager;
import net.minecraft.resource.VanillaDataPackProvider;
import net.minecraft.resource.featuretoggle.FeatureFlags;
import net.minecraft.resource.featuretoggle.FeatureSet;
import net.minecraft.screen.ScreenTexts;
import net.minecraft.server.SaveLoading;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.integrated.IntegratedServerLoader;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;
import net.minecraft.util.PathUtil;
import net.minecraft.util.Util;
import net.minecraft.util.WorldSavePath;
import net.minecraft.util.path.SymlinkFinder;
import net.minecraft.world.Difficulty;
import net.minecraft.world.GameMode;
import net.minecraft.world.GameRules;
import net.minecraft.world.dimension.DimensionOptionsRegistryHolder;
import net.minecraft.world.gen.GeneratorOptions;
import net.minecraft.world.gen.WorldPreset;
import net.minecraft.world.gen.WorldPresets;
import net.minecraft.world.level.LevelInfo;
import net.minecraft.world.level.LevelProperties;
import net.minecraft.world.level.WorldGenSettings;
import net.minecraft.world.level.storage.LevelStorage;
import org.apache.commons.lang3.mutable.MutableObject;
import org.jetbrains.annotations.Nullable;
import org.lwjgl.glfw.GLFW;
import org.slf4j.Logger;

@Environment(value=EnvType.CLIENT)
public class CreateWorldScreen
extends Screen {
    private static final int field_42165 = 1;
    private static final int field_42166 = 210;
    private static final Logger LOGGER = LogUtils.getLogger();
    private static final String TEMP_DIR_PREFIX = "mcworld-";
    static final Text GAME_MODE_TEXT = Text.translatable("selectWorld.gameMode");
    static final Text ENTER_NAME_TEXT = Text.translatable("selectWorld.enterName");
    static final Text EXPERIMENTS_TEXT = Text.translatable("selectWorld.experiments");
    static final Text ALLOW_COMMANDS_INFO_TEXT = Text.translatable("selectWorld.allowCommands.info");
    private static final Text PREPARING_TEXT = Text.translatable("createWorld.preparing");
    private static final int field_42170 = 10;
    private static final int field_42171 = 8;
    public static final Identifier TAB_HEADER_BACKGROUND_TEXTURE = Identifier.ofVanilla("textures/gui/tab_header_background.png");
    private final ThreePartsLayoutWidget layout = new ThreePartsLayoutWidget(this);
    final WorldCreator worldCreator;
    private final TabManager tabManager = new TabManager(this::addDrawableChild, child -> this.remove((Element)child));
    private boolean recreated;
    private final SymlinkFinder symlinkFinder;
    @Nullable
    private final Screen parent;
    @Nullable
    private Path dataPackTempDir;
    @Nullable
    private ResourcePackManager packManager;
    @Nullable
    private TabNavigationWidget tabNavigation;

    public static void create(MinecraftClient client, @Nullable Screen parent) {
        CreateWorldScreen.showMessage(client, PREPARING_TEXT);
        ResourcePackManager lv = new ResourcePackManager(new VanillaDataPackProvider(client.getSymlinkFinder()));
        SaveLoading.ServerConfig lv2 = CreateWorldScreen.createServerConfig(lv, DataConfiguration.SAFE_MODE);
        CompletableFuture<GeneratorOptionsHolder> completableFuture = SaveLoading.load(lv2, context -> new SaveLoading.LoadContext<WorldCreationSettings>(new WorldCreationSettings(new WorldGenSettings(GeneratorOptions.createRandom(), WorldPresets.createDemoOptions(context.worldGenRegistryManager())), context.dataConfiguration()), context.dimensionsRegistryManager()), (resourceManager, dataPackContents, combinedDynamicRegistries, generatorOptions) -> {
            resourceManager.close();
            return new GeneratorOptionsHolder(generatorOptions.worldGenSettings(), combinedDynamicRegistries, dataPackContents, generatorOptions.dataConfiguration());
        }, Util.getMainWorkerExecutor(), client);
        client.runTasks(completableFuture::isDone);
        client.setScreen(new CreateWorldScreen(client, parent, completableFuture.join(), Optional.of(WorldPresets.DEFAULT), OptionalLong.empty()));
    }

    public static CreateWorldScreen create(MinecraftClient client, @Nullable Screen parent, LevelInfo levelInfo, GeneratorOptionsHolder generatorOptionsHolder, @Nullable Path dataPackTempDir) {
        CreateWorldScreen lv = new CreateWorldScreen(client, parent, generatorOptionsHolder, WorldPresets.getWorldPreset(generatorOptionsHolder.selectedDimensions()), OptionalLong.of(generatorOptionsHolder.generatorOptions().getSeed()));
        lv.recreated = true;
        lv.worldCreator.setWorldName(levelInfo.getLevelName());
        lv.worldCreator.setCheatsEnabled(levelInfo.areCommandsAllowed());
        lv.worldCreator.setDifficulty(levelInfo.getDifficulty());
        lv.worldCreator.getGameRules().setAllValues(levelInfo.getGameRules(), null);
        if (levelInfo.isHardcore()) {
            lv.worldCreator.setGameMode(WorldCreator.Mode.HARDCORE);
        } else if (levelInfo.getGameMode().isSurvivalLike()) {
            lv.worldCreator.setGameMode(WorldCreator.Mode.SURVIVAL);
        } else if (levelInfo.getGameMode().isCreative()) {
            lv.worldCreator.setGameMode(WorldCreator.Mode.CREATIVE);
        }
        lv.dataPackTempDir = dataPackTempDir;
        return lv;
    }

    private CreateWorldScreen(MinecraftClient client, @Nullable Screen parent, GeneratorOptionsHolder generatorOptionsHolder, Optional<RegistryKey<WorldPreset>> defaultWorldType, OptionalLong seed) {
        super(Text.translatable("selectWorld.create"));
        this.parent = parent;
        this.symlinkFinder = client.getSymlinkFinder();
        this.worldCreator = new WorldCreator(client.getLevelStorage().getSavesDirectory(), generatorOptionsHolder, defaultWorldType, seed);
    }

    public WorldCreator getWorldCreator() {
        return this.worldCreator;
    }

    @Override
    protected void init() {
        this.tabNavigation = TabNavigationWidget.builder(this.tabManager, this.width).tabs(new GameTab(), new WorldTab(), new MoreTab()).build();
        this.addDrawableChild(this.tabNavigation);
        DirectionalLayoutWidget lv = this.layout.addFooter(DirectionalLayoutWidget.horizontal().spacing(8));
        lv.add(ButtonWidget.builder(Text.translatable("selectWorld.create"), button -> this.createLevel()).build());
        lv.add(ButtonWidget.builder(ScreenTexts.CANCEL, button -> this.onCloseScreen()).build());
        this.layout.forEachChild(child -> {
            child.setNavigationOrder(1);
            this.addDrawableChild(child);
        });
        this.tabNavigation.selectTab(0, false);
        this.worldCreator.update();
        this.initTabNavigation();
    }

    @Override
    protected void setInitialFocus() {
    }

    @Override
    public void initTabNavigation() {
        if (this.tabNavigation == null) {
            return;
        }
        this.tabNavigation.setWidth(this.width);
        this.tabNavigation.init();
        int i = this.tabNavigation.getNavigationFocus().getBottom();
        ScreenRect lv = new ScreenRect(0, i, this.width, this.height - this.layout.getFooterHeight() - i);
        this.tabManager.setTabArea(lv);
        this.layout.setHeaderHeight(i);
        this.layout.refreshPositions();
    }

    private static void showMessage(MinecraftClient client, Text text) {
        client.setScreenAndRender(new MessageScreen(text));
    }

    private void createLevel() {
        GeneratorOptionsHolder lv = this.worldCreator.getGeneratorOptionsHolder();
        DimensionOptionsRegistryHolder.DimensionsConfig lv2 = lv.selectedDimensions().toConfig(lv.dimensionOptionsRegistry());
        CombinedDynamicRegistries<ServerDynamicRegistryType> lv3 = lv.combinedDynamicRegistries().with(ServerDynamicRegistryType.DIMENSIONS, lv2.toDynamicRegistryManager());
        Lifecycle lifecycle = FeatureFlags.isNotVanilla(lv.dataConfiguration().enabledFeatures()) ? Lifecycle.experimental() : Lifecycle.stable();
        Lifecycle lifecycle2 = lv3.getCombinedRegistryManager().getRegistryLifecycle();
        Lifecycle lifecycle3 = lifecycle2.add(lifecycle);
        boolean bl = !this.recreated && lifecycle2 == Lifecycle.stable();
        IntegratedServerLoader.tryLoad(this.client, this, lifecycle3, () -> this.startServer(lv2.specialWorldProperty(), lv3, lifecycle3), bl);
    }

    private void startServer(LevelProperties.SpecialProperty specialProperty, CombinedDynamicRegistries<ServerDynamicRegistryType> combinedDynamicRegistries, Lifecycle lifecycle) {
        CreateWorldScreen.showMessage(this.client, PREPARING_TEXT);
        Optional<LevelStorage.Session> optional = this.createSession();
        if (optional.isEmpty()) {
            return;
        }
        this.clearDataPackTempDir();
        boolean bl = specialProperty == LevelProperties.SpecialProperty.DEBUG;
        GeneratorOptionsHolder lv = this.worldCreator.getGeneratorOptionsHolder();
        LevelInfo lv2 = this.createLevelInfo(bl);
        LevelProperties lv3 = new LevelProperties(lv2, lv.generatorOptions(), specialProperty, lifecycle);
        this.client.createIntegratedServerLoader().startNewWorld(optional.get(), lv.dataPackContents(), combinedDynamicRegistries, lv3);
    }

    private LevelInfo createLevelInfo(boolean debugWorld) {
        String string = this.worldCreator.getWorldName().trim();
        if (debugWorld) {
            GameRules lv = new GameRules();
            lv.get(GameRules.DO_DAYLIGHT_CYCLE).set(false, null);
            return new LevelInfo(string, GameMode.SPECTATOR, false, Difficulty.PEACEFUL, true, lv, DataConfiguration.SAFE_MODE);
        }
        return new LevelInfo(string, this.worldCreator.getGameMode().defaultGameMode, this.worldCreator.isHardcore(), this.worldCreator.getDifficulty(), this.worldCreator.areCheatsEnabled(), this.worldCreator.getGameRules(), this.worldCreator.getGeneratorOptionsHolder().dataConfiguration());
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (this.tabNavigation.trySwitchTabsWithKey(keyCode)) {
            return true;
        }
        if (super.keyPressed(keyCode, scanCode, modifiers)) {
            return true;
        }
        if (keyCode == GLFW.GLFW_KEY_ENTER || keyCode == GLFW.GLFW_KEY_KP_ENTER) {
            this.createLevel();
            return true;
        }
        return false;
    }

    @Override
    public void close() {
        this.onCloseScreen();
    }

    public void onCloseScreen() {
        this.client.setScreen(this.parent);
        this.clearDataPackTempDir();
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        super.render(context, mouseX, mouseY, delta);
        RenderSystem.enableBlend();
        context.drawTexture(Screen.FOOTER_SEPARATOR_TEXTURE, 0, this.height - this.layout.getFooterHeight() - 2, 0.0f, 0.0f, this.width, 2, 32, 2);
        RenderSystem.disableBlend();
    }

    @Override
    protected void renderDarkening(DrawContext context) {
        context.drawTexture(TAB_HEADER_BACKGROUND_TEXTURE, 0, 0, 0.0f, 0.0f, this.width, this.layout.getHeaderHeight(), 16, 16);
        this.renderDarkening(context, 0, this.layout.getHeaderHeight(), this.width, this.height);
    }

    @Override
    protected <T extends Element & Selectable> T addSelectableChild(T child) {
        return super.addSelectableChild(child);
    }

    @Override
    protected <T extends Element & Drawable> T addDrawableChild(T drawableElement) {
        return super.addDrawableChild(drawableElement);
    }

    @Nullable
    private Path getDataPackTempDir() {
        if (this.dataPackTempDir == null) {
            try {
                this.dataPackTempDir = Files.createTempDirectory(TEMP_DIR_PREFIX, new FileAttribute[0]);
            } catch (IOException iOException) {
                LOGGER.warn("Failed to create temporary dir", iOException);
                SystemToast.addPackCopyFailure(this.client, this.worldCreator.getWorldDirectoryName());
                this.onCloseScreen();
            }
        }
        return this.dataPackTempDir;
    }

    void openExperimentsScreen(DataConfiguration dataConfiguration) {
        Pair<Path, ResourcePackManager> pair = this.getScannedPack(dataConfiguration);
        if (pair != null) {
            this.client.setScreen(new ExperimentsScreen(this, pair.getSecond(), resourcePackManager -> this.applyDataPacks((ResourcePackManager)resourcePackManager, false, this::openExperimentsScreen)));
        }
    }

    void openPackScreen(DataConfiguration dataConfiguration) {
        Pair<Path, ResourcePackManager> pair = this.getScannedPack(dataConfiguration);
        if (pair != null) {
            this.client.setScreen(new PackScreen(pair.getSecond(), resourcePackManager -> this.applyDataPacks((ResourcePackManager)resourcePackManager, true, this::openPackScreen), pair.getFirst(), Text.translatable("dataPack.title")));
        }
    }

    private void applyDataPacks(ResourcePackManager dataPackManager, boolean fromPackScreen, Consumer<DataConfiguration> configurationSetter) {
        List list2;
        ImmutableList<String> list = ImmutableList.copyOf(dataPackManager.getEnabledIds());
        DataConfiguration lv = new DataConfiguration(new DataPackSettings(list, list2 = (List)dataPackManager.getIds().stream().filter(name -> !list.contains(name)).collect(ImmutableList.toImmutableList())), this.worldCreator.getGeneratorOptionsHolder().dataConfiguration().enabledFeatures());
        if (this.worldCreator.updateDataConfiguration(lv)) {
            this.client.setScreen(this);
            return;
        }
        FeatureSet lv2 = dataPackManager.getRequestedFeatures();
        if (FeatureFlags.isNotVanilla(lv2) && fromPackScreen) {
            this.client.setScreen(new ExperimentalWarningScreen(dataPackManager.getEnabledProfiles(), confirmed -> {
                if (confirmed) {
                    this.validateDataPacks(dataPackManager, lv, configurationSetter);
                } else {
                    configurationSetter.accept(this.worldCreator.getGeneratorOptionsHolder().dataConfiguration());
                }
            }));
        } else {
            this.validateDataPacks(dataPackManager, lv, configurationSetter);
        }
    }

    private void validateDataPacks(ResourcePackManager dataPackManager, DataConfiguration dataConfiguration, Consumer<DataConfiguration> configurationSetter) {
        this.client.setScreenAndRender(new MessageScreen(Text.translatable("dataPack.validation.working")));
        SaveLoading.ServerConfig lv = CreateWorldScreen.createServerConfig(dataPackManager, dataConfiguration);
        ((CompletableFuture)((CompletableFuture)SaveLoading.load(lv, context -> {
            if (context.worldGenRegistryManager().get(RegistryKeys.WORLD_PRESET).size() == 0) {
                throw new IllegalStateException("Needs at least one world preset to continue");
            }
            if (context.worldGenRegistryManager().get(RegistryKeys.BIOME).size() == 0) {
                throw new IllegalStateException("Needs at least one biome continue");
            }
            GeneratorOptionsHolder lv = this.worldCreator.getGeneratorOptionsHolder();
            RegistryOps<JsonElement> dynamicOps = lv.getCombinedRegistryManager().getOps(JsonOps.INSTANCE);
            DataResult<JsonElement> dataResult = WorldGenSettings.encode(dynamicOps, lv.generatorOptions(), lv.selectedDimensions()).setLifecycle(Lifecycle.stable());
            RegistryOps<JsonElement> dynamicOps2 = context.worldGenRegistryManager().getOps(JsonOps.INSTANCE);
            WorldGenSettings lv2 = (WorldGenSettings)dataResult.flatMap(json -> WorldGenSettings.CODEC.parse(dynamicOps2, json)).getOrThrow(string -> new IllegalStateException("Error parsing worldgen settings after loading data packs: " + string));
            return new SaveLoading.LoadContext<WorldCreationSettings>(new WorldCreationSettings(lv2, context.dataConfiguration()), context.dimensionsRegistryManager());
        }, (resourceManager, dataPackContents, combinedDynamicRegistries, context) -> {
            resourceManager.close();
            return new GeneratorOptionsHolder(context.worldGenSettings(), combinedDynamicRegistries, dataPackContents, context.dataConfiguration());
        }, Util.getMainWorkerExecutor(), this.client).thenApply(generatorOptionsHolder -> {
            generatorOptionsHolder.initializeIndexedFeaturesLists();
            return generatorOptionsHolder;
        })).thenAcceptAsync(this.worldCreator::setGeneratorOptionsHolder, (Executor)this.client)).handleAsync((void_, throwable) -> {
            if (throwable != null) {
                LOGGER.warn("Failed to validate datapack", (Throwable)throwable);
                this.client.setScreen(new ConfirmScreen(confirmed -> {
                    if (confirmed) {
                        configurationSetter.accept(this.worldCreator.getGeneratorOptionsHolder().dataConfiguration());
                    } else {
                        configurationSetter.accept(DataConfiguration.SAFE_MODE);
                    }
                }, Text.translatable("dataPack.validation.failed"), ScreenTexts.EMPTY, Text.translatable("dataPack.validation.back"), Text.translatable("dataPack.validation.reset")));
            } else {
                this.client.setScreen(this);
            }
            return null;
        }, (Executor)this.client);
    }

    private static SaveLoading.ServerConfig createServerConfig(ResourcePackManager dataPackManager, DataConfiguration dataConfiguration) {
        SaveLoading.DataPacks lv = new SaveLoading.DataPacks(dataPackManager, dataConfiguration, false, true);
        return new SaveLoading.ServerConfig(lv, CommandManager.RegistrationEnvironment.INTEGRATED, 2);
    }

    private void clearDataPackTempDir() {
        if (this.dataPackTempDir != null) {
            try (Stream<Path> stream = Files.walk(this.dataPackTempDir, new FileVisitOption[0]);){
                stream.sorted(Comparator.reverseOrder()).forEach(path -> {
                    try {
                        Files.delete(path);
                    } catch (IOException iOException) {
                        LOGGER.warn("Failed to remove temporary file {}", path, (Object)iOException);
                    }
                });
            } catch (IOException iOException) {
                LOGGER.warn("Failed to list temporary dir {}", (Object)this.dataPackTempDir);
            }
            this.dataPackTempDir = null;
        }
    }

    private static void copyDataPack(Path srcFolder, Path destFolder, Path dataPackFile) {
        try {
            Util.relativeCopy(srcFolder, destFolder, dataPackFile);
        } catch (IOException iOException) {
            LOGGER.warn("Failed to copy datapack file from {} to {}", (Object)dataPackFile, (Object)destFolder);
            throw new UncheckedIOException(iOException);
        }
    }

    private Optional<LevelStorage.Session> createSession() {
        Optional<LevelStorage.Session> optional;
        String string;
        block12: {
            LevelStorage.Session lv;
            block11: {
                string = this.worldCreator.getWorldDirectoryName();
                lv = this.client.getLevelStorage().createSessionWithoutSymlinkCheck(string);
                if (this.dataPackTempDir != null) break block11;
                return Optional.of(lv);
            }
            Stream<Path> stream = Files.walk(this.dataPackTempDir, new FileVisitOption[0]);
            try {
                Path path3 = lv.getDirectory(WorldSavePath.DATAPACKS);
                PathUtil.createDirectories(path3);
                stream.filter(path -> !path.equals(this.dataPackTempDir)).forEach(path2 -> CreateWorldScreen.copyDataPack(this.dataPackTempDir, path3, path2));
                optional = Optional.of(lv);
                if (stream == null) break block12;
            } catch (Throwable throwable) {
                try {
                    try {
                        if (stream != null) {
                            try {
                                stream.close();
                            } catch (Throwable throwable2) {
                                throwable.addSuppressed(throwable2);
                            }
                        }
                        throw throwable;
                    } catch (IOException | UncheckedIOException exception) {
                        LOGGER.warn("Failed to copy datapacks to world {}", (Object)string, (Object)exception);
                        lv.close();
                    }
                } catch (IOException | UncheckedIOException exception2) {
                    LOGGER.warn("Failed to create access for {}", (Object)string, (Object)exception2);
                }
            }
            stream.close();
        }
        return optional;
        SystemToast.addPackCopyFailure(this.client, string);
        this.onCloseScreen();
        return Optional.empty();
    }

    @Nullable
    public static Path copyDataPack(Path srcFolder, MinecraftClient client) {
        MutableObject mutableObject = new MutableObject();
        try (Stream<Path> stream = Files.walk(srcFolder, new FileVisitOption[0]);){
            stream.filter(dataPackFile -> !dataPackFile.equals(srcFolder)).forEach(dataPackFile -> {
                Path path3 = (Path)mutableObject.getValue();
                if (path3 == null) {
                    try {
                        path3 = Files.createTempDirectory(TEMP_DIR_PREFIX, new FileAttribute[0]);
                    } catch (IOException iOException) {
                        LOGGER.warn("Failed to create temporary dir");
                        throw new UncheckedIOException(iOException);
                    }
                    mutableObject.setValue(path3);
                }
                CreateWorldScreen.copyDataPack(srcFolder, path3, dataPackFile);
            });
        } catch (IOException | UncheckedIOException exception) {
            LOGGER.warn("Failed to copy datapacks from world {}", (Object)srcFolder, (Object)exception);
            SystemToast.addPackCopyFailure(client, srcFolder.toString());
            return null;
        }
        return (Path)mutableObject.getValue();
    }

    @Nullable
    private Pair<Path, ResourcePackManager> getScannedPack(DataConfiguration dataConfiguration) {
        Path path = this.getDataPackTempDir();
        if (path != null) {
            if (this.packManager == null) {
                this.packManager = VanillaDataPackProvider.createManager(path, this.symlinkFinder);
                this.packManager.scanPacks();
            }
            this.packManager.setEnabledProfiles(dataConfiguration.dataPacks().getEnabled());
            return Pair.of(path, this.packManager);
        }
        return null;
    }

    @Environment(value=EnvType.CLIENT)
    class GameTab
    extends GridScreenTab {
        private static final Text GAME_TAB_TITLE_TEXT = Text.translatable("createWorld.tab.game.title");
        private static final Text ALLOW_COMMANDS_TEXT = Text.translatable("selectWorld.allowCommands.new");
        private final TextFieldWidget worldNameField;

        GameTab() {
            super(GAME_TAB_TITLE_TEXT);
            GridWidget.Adder lv = this.grid.setRowSpacing(8).createAdder(1);
            Positioner lv2 = lv.copyPositioner();
            this.worldNameField = new TextFieldWidget(CreateWorldScreen.this.textRenderer, 208, 20, Text.translatable("selectWorld.enterName"));
            this.worldNameField.setText(CreateWorldScreen.this.worldCreator.getWorldName());
            this.worldNameField.setChangedListener(CreateWorldScreen.this.worldCreator::setWorldName);
            CreateWorldScreen.this.worldCreator.addListener(creator -> this.worldNameField.setTooltip(Tooltip.of(Text.translatable("selectWorld.targetFolder", Text.literal(creator.getWorldDirectoryName()).formatted(Formatting.ITALIC)))));
            CreateWorldScreen.this.setInitialFocus(this.worldNameField);
            lv.add(LayoutWidgets.createLabeledWidget(CreateWorldScreen.this.textRenderer, this.worldNameField, ENTER_NAME_TEXT), lv.copyPositioner().alignHorizontalCenter());
            CyclingButtonWidget<WorldCreator.Mode> lv3 = lv.add(CyclingButtonWidget.builder(value -> value.name).values((WorldCreator.Mode[])new WorldCreator.Mode[]{WorldCreator.Mode.SURVIVAL, WorldCreator.Mode.HARDCORE, WorldCreator.Mode.CREATIVE}).build(0, 0, 210, 20, GAME_MODE_TEXT, (button, value) -> CreateWorldScreen.this.worldCreator.setGameMode((WorldCreator.Mode)((Object)value))), lv2);
            CreateWorldScreen.this.worldCreator.addListener(creator -> {
                lv3.setValue(creator.getGameMode());
                arg.active = !creator.isDebug();
                lv3.setTooltip(Tooltip.of(creator.getGameMode().getInfo()));
            });
            CyclingButtonWidget<Difficulty> lv4 = lv.add(CyclingButtonWidget.builder(Difficulty::getTranslatableName).values((Difficulty[])Difficulty.values()).build(0, 0, 210, 20, Text.translatable("options.difficulty"), (button, value) -> CreateWorldScreen.this.worldCreator.setDifficulty((Difficulty)value)), lv2);
            CreateWorldScreen.this.worldCreator.addListener(creator -> {
                lv4.setValue(CreateWorldScreen.this.worldCreator.getDifficulty());
                CreateWorldScreen.this.active = !CreateWorldScreen.this.worldCreator.isHardcore();
                lv4.setTooltip(Tooltip.of(CreateWorldScreen.this.worldCreator.getDifficulty().getInfo()));
            });
            CyclingButtonWidget<Boolean> lv5 = lv.add(CyclingButtonWidget.onOffBuilder().tooltip(value -> Tooltip.of(ALLOW_COMMANDS_INFO_TEXT)).build(0, 0, 210, 20, ALLOW_COMMANDS_TEXT, (button, value) -> CreateWorldScreen.this.worldCreator.setCheatsEnabled((boolean)value)));
            CreateWorldScreen.this.worldCreator.addListener(creator -> {
                lv5.setValue(CreateWorldScreen.this.worldCreator.areCheatsEnabled());
                CreateWorldScreen.this.active = !CreateWorldScreen.this.worldCreator.isDebug() && !CreateWorldScreen.this.worldCreator.isHardcore();
            });
            if (!SharedConstants.getGameVersion().isStable()) {
                lv.add(ButtonWidget.builder(EXPERIMENTS_TEXT, button -> CreateWorldScreen.this.openExperimentsScreen(CreateWorldScreen.this.worldCreator.getGeneratorOptionsHolder().dataConfiguration())).width(210).build());
            }
        }
    }

    @Environment(value=EnvType.CLIENT)
    class WorldTab
    extends GridScreenTab {
        private static final Text WORLD_TAB_TITLE_TEXT = Text.translatable("createWorld.tab.world.title");
        private static final Text AMPLIFIED_GENERATOR_INFO_TEXT = Text.translatable("generator.minecraft.amplified.info");
        private static final Text MAP_FEATURES_TEXT = Text.translatable("selectWorld.mapFeatures");
        private static final Text MAP_FEATURES_INFO_TEXT = Text.translatable("selectWorld.mapFeatures.info");
        private static final Text BONUS_ITEMS_TEXT = Text.translatable("selectWorld.bonusItems");
        private static final Text ENTER_SEED_TEXT = Text.translatable("selectWorld.enterSeed");
        static final Text SEED_INFO_TEXT = Text.translatable("selectWorld.seedInfo").formatted(Formatting.DARK_GRAY);
        private static final int field_42190 = 310;
        private final TextFieldWidget seedField;
        private final ButtonWidget customizeButton;

        WorldTab() {
            super(WORLD_TAB_TITLE_TEXT);
            GridWidget.Adder lv = this.grid.setColumnSpacing(10).setRowSpacing(8).createAdder(2);
            CyclingButtonWidget<WorldCreator.WorldType> lv2 = lv.add(CyclingButtonWidget.builder(WorldCreator.WorldType::getName).values(this.getWorldTypes()).narration(WorldTab::getWorldTypeNarrationMessage).build(0, 0, 150, 20, Text.translatable("selectWorld.mapType"), (button, worldType) -> CreateWorldScreen.this.worldCreator.setWorldType((WorldCreator.WorldType)worldType)));
            lv2.setValue(CreateWorldScreen.this.worldCreator.getWorldType());
            CreateWorldScreen.this.worldCreator.addListener(creator -> {
                WorldCreator.WorldType lv = creator.getWorldType();
                lv2.setValue(lv);
                if (lv.isAmplified()) {
                    lv2.setTooltip(Tooltip.of(AMPLIFIED_GENERATOR_INFO_TEXT));
                } else {
                    lv2.setTooltip(null);
                }
                CreateWorldScreen.this.active = CreateWorldScreen.this.worldCreator.getWorldType().preset() != null;
            });
            this.customizeButton = lv.add(ButtonWidget.builder(Text.translatable("selectWorld.customizeType"), button -> this.openCustomizeScreen()).build());
            CreateWorldScreen.this.worldCreator.addListener(creator -> {
                this.customizeButton.active = !creator.isDebug() && creator.getLevelScreenProvider() != null;
            });
            this.seedField = new TextFieldWidget(this, CreateWorldScreen.this.textRenderer, 308, 20, Text.translatable("selectWorld.enterSeed")){

                @Override
                protected MutableText getNarrationMessage() {
                    return super.getNarrationMessage().append(ScreenTexts.SENTENCE_SEPARATOR).append(SEED_INFO_TEXT);
                }
            };
            this.seedField.setPlaceholder(SEED_INFO_TEXT);
            this.seedField.setText(CreateWorldScreen.this.worldCreator.getSeed());
            this.seedField.setChangedListener(seed -> CreateWorldScreen.this.worldCreator.setSeed(this.seedField.getText()));
            lv.add(LayoutWidgets.createLabeledWidget(CreateWorldScreen.this.textRenderer, this.seedField, ENTER_SEED_TEXT), 2);
            WorldScreenOptionGrid.Builder lv3 = WorldScreenOptionGrid.builder(310);
            lv3.add(MAP_FEATURES_TEXT, CreateWorldScreen.this.worldCreator::shouldGenerateStructures, CreateWorldScreen.this.worldCreator::setGenerateStructures).toggleable(() -> !CreateWorldScreen.this.worldCreator.isDebug()).tooltip(MAP_FEATURES_INFO_TEXT);
            lv3.add(BONUS_ITEMS_TEXT, CreateWorldScreen.this.worldCreator::isBonusChestEnabled, CreateWorldScreen.this.worldCreator::setBonusChestEnabled).toggleable(() -> !CreateWorldScreen.this.worldCreator.isHardcore() && !CreateWorldScreen.this.worldCreator.isDebug());
            WorldScreenOptionGrid lv4 = lv3.build(widget -> lv.add(widget, 2));
            CreateWorldScreen.this.worldCreator.addListener(creator -> lv4.refresh());
        }

        private void openCustomizeScreen() {
            LevelScreenProvider lv = CreateWorldScreen.this.worldCreator.getLevelScreenProvider();
            if (lv != null) {
                CreateWorldScreen.this.client.setScreen(lv.createEditScreen(CreateWorldScreen.this, CreateWorldScreen.this.worldCreator.getGeneratorOptionsHolder()));
            }
        }

        private CyclingButtonWidget.Values<WorldCreator.WorldType> getWorldTypes() {
            return new CyclingButtonWidget.Values<WorldCreator.WorldType>(){

                @Override
                public List<WorldCreator.WorldType> getCurrent() {
                    return CyclingButtonWidget.HAS_ALT_DOWN.getAsBoolean() ? CreateWorldScreen.this.worldCreator.getExtendedWorldTypes() : CreateWorldScreen.this.worldCreator.getNormalWorldTypes();
                }

                @Override
                public List<WorldCreator.WorldType> getDefaults() {
                    return CreateWorldScreen.this.worldCreator.getNormalWorldTypes();
                }
            };
        }

        private static MutableText getWorldTypeNarrationMessage(CyclingButtonWidget<WorldCreator.WorldType> worldTypeButton) {
            if (worldTypeButton.getValue().isAmplified()) {
                return ScreenTexts.joinSentences(worldTypeButton.getGenericNarrationMessage(), AMPLIFIED_GENERATOR_INFO_TEXT);
            }
            return worldTypeButton.getGenericNarrationMessage();
        }
    }

    @Environment(value=EnvType.CLIENT)
    class MoreTab
    extends GridScreenTab {
        private static final Text MORE_TAB_TITLE_TEXT = Text.translatable("createWorld.tab.more.title");
        private static final Text GAME_RULES_TEXT = Text.translatable("selectWorld.gameRules");
        private static final Text DATA_PACKS_TEXT = Text.translatable("selectWorld.dataPacks");

        MoreTab() {
            super(MORE_TAB_TITLE_TEXT);
            GridWidget.Adder lv = this.grid.setRowSpacing(8).createAdder(1);
            lv.add(ButtonWidget.builder(GAME_RULES_TEXT, button -> this.openGameRulesScreen()).width(210).build());
            lv.add(ButtonWidget.builder(EXPERIMENTS_TEXT, button -> CreateWorldScreen.this.openExperimentsScreen(CreateWorldScreen.this.worldCreator.getGeneratorOptionsHolder().dataConfiguration())).width(210).build());
            lv.add(ButtonWidget.builder(DATA_PACKS_TEXT, button -> CreateWorldScreen.this.openPackScreen(CreateWorldScreen.this.worldCreator.getGeneratorOptionsHolder().dataConfiguration())).width(210).build());
        }

        private void openGameRulesScreen() {
            CreateWorldScreen.this.client.setScreen(new EditGameRulesScreen(CreateWorldScreen.this.worldCreator.getGameRules().copy(), gameRules -> {
                CreateWorldScreen.this.client.setScreen(CreateWorldScreen.this);
                gameRules.ifPresent(CreateWorldScreen.this.worldCreator::setGameRules);
            }));
        }
    }

    @Environment(value=EnvType.CLIENT)
    record WorldCreationSettings(WorldGenSettings worldGenSettings, DataConfiguration dataConfiguration) {
    }
}

