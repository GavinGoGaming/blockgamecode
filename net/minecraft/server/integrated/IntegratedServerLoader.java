/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 * 
 * Could not load the following classes:
 *  net.fabricmc.api.EnvType
 *  net.fabricmc.api.Environment
 *  org.jetbrains.annotations.Nullable
 */
package net.minecraft.server.integrated;

import com.mojang.datafixers.util.Pair;
import com.mojang.logging.LogUtils;
import com.mojang.serialization.Dynamic;
import com.mojang.serialization.Lifecycle;
import it.unimi.dsi.fastutil.booleans.BooleanConsumer;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.function.Function;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.SharedConstants;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.ConfirmScreen;
import net.minecraft.client.gui.screen.MessageScreen;
import net.minecraft.client.gui.screen.NoticeScreen;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.world.BackupPromptScreen;
import net.minecraft.client.gui.screen.world.CreateWorldScreen;
import net.minecraft.client.gui.screen.world.DataPackFailureScreen;
import net.minecraft.client.gui.screen.world.EditWorldScreen;
import net.minecraft.client.gui.screen.world.RecoverWorldScreen;
import net.minecraft.client.gui.screen.world.SymlinkWarningScreen;
import net.minecraft.client.resource.server.ServerResourcePackLoader;
import net.minecraft.client.toast.SystemToast;
import net.minecraft.client.world.GeneratorOptionsHolder;
import net.minecraft.nbt.NbtCrashException;
import net.minecraft.nbt.NbtException;
import net.minecraft.registry.CombinedDynamicRegistries;
import net.minecraft.registry.DynamicRegistryManager;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.ServerDynamicRegistryType;
import net.minecraft.registry.SimpleRegistry;
import net.minecraft.resource.DataConfiguration;
import net.minecraft.resource.LifecycledResourceManager;
import net.minecraft.resource.ResourcePackManager;
import net.minecraft.resource.VanillaDataPackProvider;
import net.minecraft.screen.ScreenTexts;
import net.minecraft.server.DataPackContents;
import net.minecraft.server.SaveLoader;
import net.minecraft.server.SaveLoading;
import net.minecraft.server.command.CommandManager;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Colors;
import net.minecraft.util.Formatting;
import net.minecraft.util.Util;
import net.minecraft.util.WorldSavePath;
import net.minecraft.util.crash.CrashException;
import net.minecraft.util.crash.CrashMemoryReserve;
import net.minecraft.util.crash.CrashReport;
import net.minecraft.util.crash.CrashReportSection;
import net.minecraft.util.path.SymlinkValidationException;
import net.minecraft.world.SaveProperties;
import net.minecraft.world.dimension.DimensionOptions;
import net.minecraft.world.dimension.DimensionOptionsRegistryHolder;
import net.minecraft.world.gen.GeneratorOptions;
import net.minecraft.world.level.LevelInfo;
import net.minecraft.world.level.LevelProperties;
import net.minecraft.world.level.storage.LevelStorage;
import net.minecraft.world.level.storage.LevelSummary;
import net.minecraft.world.level.storage.ParsedSaveProperties;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;

@Environment(value=EnvType.CLIENT)
public class IntegratedServerLoader {
    private static final Logger LOGGER = LogUtils.getLogger();
    private static final UUID WORLD_PACK_ID = UUID.fromString("640a6a92-b6cb-48a0-b391-831586500359");
    private final MinecraftClient client;
    private final LevelStorage storage;

    public IntegratedServerLoader(MinecraftClient client, LevelStorage storage) {
        this.client = client;
        this.storage = storage;
    }

    public void createAndStart(String levelName, LevelInfo levelInfo, GeneratorOptions dynamicRegistryManager, Function<DynamicRegistryManager, DimensionOptionsRegistryHolder> dimensionsRegistrySupplier, Screen screen) {
        this.client.setScreenAndRender(new MessageScreen(Text.translatable("selectWorld.data_read")));
        LevelStorage.Session lv = this.createSession(levelName);
        if (lv == null) {
            return;
        }
        ResourcePackManager lv2 = VanillaDataPackProvider.createManager(lv);
        DataConfiguration lv3 = levelInfo.getDataConfiguration();
        try {
            SaveLoading.DataPacks lv4 = new SaveLoading.DataPacks(lv2, lv3, false, false);
            SaveLoader lv5 = this.load(lv4, context -> {
                DimensionOptionsRegistryHolder.DimensionsConfig lv = ((DimensionOptionsRegistryHolder)dimensionsRegistrySupplier.apply(context.worldGenRegistryManager())).toConfig(context.dimensionsRegistryManager().get(RegistryKeys.DIMENSION));
                return new SaveLoading.LoadContext<LevelProperties>(new LevelProperties(levelInfo, dynamicRegistryManager, lv.specialWorldProperty(), lv.getLifecycle()), lv.toDynamicRegistryManager());
            }, SaveLoader::new);
            this.client.startIntegratedServer(lv, lv2, lv5, true);
        } catch (Exception exception) {
            LOGGER.warn("Failed to load datapacks, can't proceed with server load", exception);
            lv.tryClose();
            this.client.setScreen(screen);
        }
    }

    @Nullable
    private LevelStorage.Session createSession(String levelName) {
        try {
            return this.storage.createSession(levelName);
        } catch (IOException iOException) {
            LOGGER.warn("Failed to read level {} data", (Object)levelName, (Object)iOException);
            SystemToast.addWorldAccessFailureToast(this.client, levelName);
            this.client.setScreen(null);
            return null;
        } catch (SymlinkValidationException lv) {
            LOGGER.warn("{}", (Object)lv.getMessage());
            this.client.setScreen(SymlinkWarningScreen.world(() -> this.client.setScreen(null)));
            return null;
        }
    }

    public void startNewWorld(LevelStorage.Session session, DataPackContents dataPackContents, CombinedDynamicRegistries<ServerDynamicRegistryType> dynamicRegistryManager, SaveProperties saveProperties) {
        ResourcePackManager lv = VanillaDataPackProvider.createManager(session);
        LifecycledResourceManager lv2 = new SaveLoading.DataPacks(lv, saveProperties.getDataConfiguration(), false, false).load().getSecond();
        this.client.startIntegratedServer(session, lv, new SaveLoader(lv2, dataPackContents, dynamicRegistryManager, saveProperties), true);
    }

    public SaveLoader load(Dynamic<?> levelProperties, boolean safeMode, ResourcePackManager dataPackManager) throws Exception {
        SaveLoading.DataPacks lv = LevelStorage.parseDataPacks(levelProperties, dataPackManager, safeMode);
        return this.load(lv, context -> {
            Registry<DimensionOptions> lv = context.dimensionsRegistryManager().get(RegistryKeys.DIMENSION);
            ParsedSaveProperties lv2 = LevelStorage.parseSaveProperties(levelProperties, context.dataConfiguration(), lv, context.worldGenRegistryManager());
            return new SaveLoading.LoadContext<SaveProperties>(lv2.properties(), lv2.dimensions().toDynamicRegistryManager());
        }, SaveLoader::new);
    }

    public Pair<LevelInfo, GeneratorOptionsHolder> loadForRecreation(LevelStorage.Session session) throws Exception {
        @Environment(value=EnvType.CLIENT)
        record CurrentSettings(LevelInfo levelInfo, GeneratorOptions options, Registry<DimensionOptions> existingDimensionRegistry) {
        }
        ResourcePackManager lv = VanillaDataPackProvider.createManager(session);
        Dynamic<?> dynamic = session.readLevelProperties();
        SaveLoading.DataPacks lv2 = LevelStorage.parseDataPacks(dynamic, lv, false);
        return this.load(lv2, context -> {
            Registry<DimensionOptions> lv = new SimpleRegistry<DimensionOptions>(RegistryKeys.DIMENSION, Lifecycle.stable()).freeze();
            ParsedSaveProperties lv2 = LevelStorage.parseSaveProperties(dynamic, context.dataConfiguration(), lv, context.worldGenRegistryManager());
            return new SaveLoading.LoadContext<CurrentSettings>(new CurrentSettings(lv2.properties().getLevelInfo(), lv2.properties().getGeneratorOptions(), lv2.dimensions().dimensions()), context.dimensionsRegistryManager());
        }, (LifecycledResourceManager resourceManager, DataPackContents dataPackContents, CombinedDynamicRegistries<ServerDynamicRegistryType> combinedRegistryManager, D currentSettings) -> {
            resourceManager.close();
            return Pair.of(currentSettings.levelInfo, new GeneratorOptionsHolder(currentSettings.options, new DimensionOptionsRegistryHolder(currentSettings.existingDimensionRegistry), combinedRegistryManager, dataPackContents, currentSettings.levelInfo.getDataConfiguration()));
        });
    }

    private <D, R> R load(SaveLoading.DataPacks dataPacks, SaveLoading.LoadContextSupplier<D> loadContextSupplier, SaveLoading.SaveApplierFactory<D, R> saveApplierFactory) throws Exception {
        SaveLoading.ServerConfig lv = new SaveLoading.ServerConfig(dataPacks, CommandManager.RegistrationEnvironment.INTEGRATED, 2);
        CompletableFuture<R> completableFuture = SaveLoading.load(lv, loadContextSupplier, saveApplierFactory, Util.getMainWorkerExecutor(), this.client);
        this.client.runTasks(completableFuture::isDone);
        return completableFuture.get();
    }

    private void showBackupPromptScreen(LevelStorage.Session session, boolean customized, Runnable callback, Runnable onCancel) {
        MutableText lv2;
        MutableText lv;
        if (customized) {
            lv = Text.translatable("selectWorld.backupQuestion.customized");
            lv2 = Text.translatable("selectWorld.backupWarning.customized");
        } else {
            lv = Text.translatable("selectWorld.backupQuestion.experimental");
            lv2 = Text.translatable("selectWorld.backupWarning.experimental");
        }
        this.client.setScreen(new BackupPromptScreen(onCancel, (backup, eraseCache) -> {
            if (backup) {
                EditWorldScreen.backupLevel(session);
            }
            callback.run();
        }, lv, lv2, false));
    }

    public static void tryLoad(MinecraftClient client, CreateWorldScreen parent, Lifecycle lifecycle, Runnable loader, boolean bypassWarnings) {
        BooleanConsumer booleanConsumer = confirmed -> {
            if (confirmed) {
                loader.run();
            } else {
                client.setScreen(parent);
            }
        };
        if (bypassWarnings || lifecycle == Lifecycle.stable()) {
            loader.run();
        } else if (lifecycle == Lifecycle.experimental()) {
            client.setScreen(new ConfirmScreen(booleanConsumer, Text.translatable("selectWorld.warning.experimental.title"), Text.translatable("selectWorld.warning.experimental.question")));
        } else {
            client.setScreen(new ConfirmScreen(booleanConsumer, Text.translatable("selectWorld.warning.deprecated.title"), Text.translatable("selectWorld.warning.deprecated.question")));
        }
    }

    public void start(String name, Runnable onCancel) {
        this.client.setScreenAndRender(new MessageScreen(Text.translatable("selectWorld.data_read")));
        LevelStorage.Session lv = this.createSession(name);
        if (lv == null) {
            return;
        }
        this.start(lv, onCancel);
    }

    private void start(LevelStorage.Session session, Runnable onCancel) {
        LevelSummary lv;
        Dynamic<?> dynamic;
        this.client.setScreenAndRender(new MessageScreen(Text.translatable("selectWorld.data_read")));
        try {
            dynamic = session.readLevelProperties();
            lv = session.getLevelSummary(dynamic);
        } catch (IOException | NbtCrashException | NbtException exception) {
            this.client.setScreen(new RecoverWorldScreen(this.client, confirmed -> {
                if (confirmed) {
                    this.start(session, onCancel);
                } else {
                    session.tryClose();
                    onCancel.run();
                }
            }, session));
            return;
        } catch (OutOfMemoryError outOfMemoryError) {
            CrashMemoryReserve.releaseMemory();
            System.gc();
            String string = "Ran out of memory trying to read level data of world folder \"" + session.getDirectoryName() + "\"";
            LOGGER.error(LogUtils.FATAL_MARKER, string);
            OutOfMemoryError outOfMemoryError2 = new OutOfMemoryError("Ran out of memory reading level data");
            outOfMemoryError2.initCause(outOfMemoryError);
            CrashReport lv2 = CrashReport.create(outOfMemoryError2, string);
            CrashReportSection lv3 = lv2.addElement("World details");
            lv3.add("World folder", session.getDirectoryName());
            throw new CrashException(lv2);
        }
        this.start(session, lv, dynamic, onCancel);
    }

    private void start(LevelStorage.Session session, LevelSummary summary, Dynamic<?> levelProperties, Runnable onCancel) {
        if (!summary.isVersionAvailable()) {
            session.tryClose();
            this.client.setScreen(new NoticeScreen(onCancel, Text.translatable("selectWorld.incompatible.title").withColor(Colors.RED), Text.translatable("selectWorld.incompatible.description", summary.getVersion())));
            return;
        }
        LevelSummary.ConversionWarning lv = summary.getConversionWarning();
        if (lv.promptsBackup()) {
            String string = "selectWorld.backupQuestion." + lv.getTranslationKeySuffix();
            String string2 = "selectWorld.backupWarning." + lv.getTranslationKeySuffix();
            MutableText lv2 = Text.translatable(string);
            if (lv.isDangerous()) {
                lv2.withColor(Colors.LIGHT_RED);
            }
            MutableText lv3 = Text.translatable(string2, summary.getVersion(), SharedConstants.getGameVersion().getName());
            this.client.setScreen(new BackupPromptScreen(() -> {
                session.tryClose();
                onCancel.run();
            }, (backup, eraseCache) -> {
                if (backup) {
                    EditWorldScreen.backupLevel(session);
                }
                this.start(session, levelProperties, false, onCancel);
            }, lv2, lv3, false));
        } else {
            this.start(session, levelProperties, false, onCancel);
        }
    }

    private void start(LevelStorage.Session session, Dynamic<?> levelProperties, boolean safeMode, Runnable onCancel) {
        SaveLoader lv2;
        this.client.setScreenAndRender(new MessageScreen(Text.translatable("selectWorld.resource_load")));
        ResourcePackManager lv = VanillaDataPackProvider.createManager(session);
        try {
            lv2 = this.load(levelProperties, safeMode, lv);
            for (DimensionOptions lv3 : lv2.combinedDynamicRegistries().getCombinedRegistryManager().get(RegistryKeys.DIMENSION)) {
                lv3.chunkGenerator().initializeIndexedFeaturesList();
            }
        } catch (Exception exception) {
            LOGGER.warn("Failed to load level data or datapacks, can't proceed with server load", exception);
            if (!safeMode) {
                this.client.setScreen(new DataPackFailureScreen(() -> {
                    session.tryClose();
                    onCancel.run();
                }, () -> this.start(session, levelProperties, true, onCancel)));
            } else {
                session.tryClose();
                this.client.setScreen(new NoticeScreen(onCancel, Text.translatable("datapackFailure.safeMode.failed.title"), Text.translatable("datapackFailure.safeMode.failed.description"), ScreenTexts.BACK, true));
            }
            return;
        }
        this.checkBackupAndStart(session, lv2, lv, onCancel);
    }

    private void checkBackupAndStart(LevelStorage.Session session, SaveLoader saveLoader, ResourcePackManager dataPackManager, Runnable onCancel) {
        boolean bl2;
        SaveProperties lv = saveLoader.saveProperties();
        boolean bl = lv.getGeneratorOptions().isLegacyCustomizedType();
        boolean bl3 = bl2 = lv.getLifecycle() != Lifecycle.stable();
        if (bl || bl2) {
            this.showBackupPromptScreen(session, bl, () -> this.start(session, saveLoader, dataPackManager, onCancel), () -> {
                saveLoader.close();
                session.tryClose();
                onCancel.run();
            });
            return;
        }
        this.start(session, saveLoader, dataPackManager, onCancel);
    }

    private void start(LevelStorage.Session session, SaveLoader saveLoader, ResourcePackManager dataPackManager, Runnable onCancel) {
        ServerResourcePackLoader lv = this.client.getServerResourcePackProvider();
        ((CompletableFuture)((CompletableFuture)((CompletableFuture)this.applyWorldPack(lv, session).thenApply(v -> true)).exceptionallyComposeAsync(throwable -> {
            LOGGER.warn("Failed to load pack: ", (Throwable)throwable);
            return this.showPackLoadFailureScreen();
        }, (Executor)this.client)).thenAcceptAsync(successful -> {
            if (successful.booleanValue()) {
                this.start(session, saveLoader, lv, dataPackManager, onCancel);
            } else {
                lv.removeAll();
                saveLoader.close();
                session.tryClose();
                onCancel.run();
            }
        }, (Executor)this.client)).exceptionally(throwable -> {
            this.client.setCrashReportSupplierAndAddDetails(CrashReport.create(throwable, "Load world"));
            return null;
        });
    }

    private void start(LevelStorage.Session session, SaveLoader saveLoader, ServerResourcePackLoader resourcePackLoader, ResourcePackManager dataPackManager, Runnable onCancel) {
        if (session.shouldShowLowDiskSpaceWarning()) {
            this.client.setScreen(new ConfirmScreen(confirmed -> {
                if (confirmed) {
                    this.start(session, saveLoader, dataPackManager);
                } else {
                    resourcePackLoader.removeAll();
                    saveLoader.close();
                    session.tryClose();
                    onCancel.run();
                }
            }, Text.translatable("selectWorld.warning.lowDiskSpace.title").formatted(Formatting.RED), Text.translatable("selectWorld.warning.lowDiskSpace.description"), ScreenTexts.CONTINUE, ScreenTexts.BACK));
        } else {
            this.start(session, saveLoader, dataPackManager);
        }
    }

    private void start(LevelStorage.Session session, SaveLoader saveLoader, ResourcePackManager dataPackManager) {
        this.client.startIntegratedServer(session, dataPackManager, saveLoader, false);
    }

    private CompletableFuture<Void> applyWorldPack(ServerResourcePackLoader loader, LevelStorage.Session session) {
        Path path = session.getDirectory(WorldSavePath.RESOURCES_ZIP);
        if (Files.exists(path, new LinkOption[0]) && !Files.isDirectory(path, new LinkOption[0])) {
            loader.initWorldPack();
            CompletableFuture<Void> completableFuture = loader.getPackLoadFuture(WORLD_PACK_ID);
            loader.addResourcePack(WORLD_PACK_ID, path);
            return completableFuture;
        }
        return CompletableFuture.completedFuture(null);
    }

    private CompletableFuture<Boolean> showPackLoadFailureScreen() {
        CompletableFuture<Boolean> completableFuture = new CompletableFuture<Boolean>();
        this.client.setScreen(new ConfirmScreen(completableFuture::complete, Text.translatable("multiplayer.texturePrompt.failure.line1"), Text.translatable("multiplayer.texturePrompt.failure.line2"), ScreenTexts.PROCEED, ScreenTexts.CANCEL));
        return completableFuture;
    }
}

