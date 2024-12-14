/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 * 
 * Could not load the following classes:
 *  org.jetbrains.annotations.Nullable
 */
package net.minecraft.server;

import com.mojang.authlib.yggdrasil.YggdrasilAuthenticationService;
import com.mojang.datafixers.DataFixer;
import com.mojang.logging.LogUtils;
import com.mojang.serialization.Dynamic;
import com.mojang.serialization.Lifecycle;
import java.awt.GraphicsEnvironment;
import java.io.File;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.net.Proxy;
import java.nio.file.Files;
import java.nio.file.OpenOption;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Optional;
import java.util.function.BooleanSupplier;
import joptsimple.AbstractOptionSpec;
import joptsimple.ArgumentAcceptingOptionSpec;
import joptsimple.NonOptionArgumentSpec;
import joptsimple.OptionParser;
import joptsimple.OptionSet;
import joptsimple.OptionSpecBuilder;
import joptsimple.util.PathConverter;
import joptsimple.util.PathProperties;
import net.minecraft.Bootstrap;
import net.minecraft.SharedConstants;
import net.minecraft.datafixer.Schemas;
import net.minecraft.nbt.NbtCrashException;
import net.minecraft.nbt.NbtException;
import net.minecraft.obfuscate.DontObfuscate;
import net.minecraft.registry.DynamicRegistryManager;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.resource.DataConfiguration;
import net.minecraft.resource.ResourcePackManager;
import net.minecraft.resource.VanillaDataPackProvider;
import net.minecraft.resource.featuretoggle.FeatureFlags;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.SaveLoader;
import net.minecraft.server.SaveLoading;
import net.minecraft.server.WorldGenerationProgressLogger;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.dedicated.EulaReader;
import net.minecraft.server.dedicated.MinecraftDedicatedServer;
import net.minecraft.server.dedicated.ServerPropertiesHandler;
import net.minecraft.server.dedicated.ServerPropertiesLoader;
import net.minecraft.text.Text;
import net.minecraft.util.ApiServices;
import net.minecraft.util.Util;
import net.minecraft.util.crash.CrashReport;
import net.minecraft.util.logging.UncaughtExceptionLogger;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.profiling.jfr.FlightProfiler;
import net.minecraft.util.profiling.jfr.InstanceType;
import net.minecraft.world.GameRules;
import net.minecraft.world.SaveProperties;
import net.minecraft.world.dimension.DimensionOptions;
import net.minecraft.world.dimension.DimensionOptionsRegistryHolder;
import net.minecraft.world.gen.GeneratorOptions;
import net.minecraft.world.gen.WorldPresets;
import net.minecraft.world.level.LevelInfo;
import net.minecraft.world.level.LevelProperties;
import net.minecraft.world.level.storage.LevelStorage;
import net.minecraft.world.level.storage.LevelSummary;
import net.minecraft.world.level.storage.ParsedSaveProperties;
import net.minecraft.world.storage.ChunkCompressionFormat;
import net.minecraft.world.updater.WorldUpdater;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;

public class Main {
    private static final Logger LOGGER = LogUtils.getLogger();

    @DontObfuscate
    public static void main(String[] args) {
        SharedConstants.createGameVersion();
        OptionParser optionParser = new OptionParser();
        OptionSpecBuilder optionSpec = optionParser.accepts("nogui");
        OptionSpecBuilder optionSpec2 = optionParser.accepts("initSettings", "Initializes 'server.properties' and 'eula.txt', then quits");
        OptionSpecBuilder optionSpec3 = optionParser.accepts("demo");
        OptionSpecBuilder optionSpec4 = optionParser.accepts("bonusChest");
        OptionSpecBuilder optionSpec5 = optionParser.accepts("forceUpgrade");
        OptionSpecBuilder optionSpec6 = optionParser.accepts("eraseCache");
        OptionSpecBuilder optionSpec7 = optionParser.accepts("recreateRegionFiles");
        OptionSpecBuilder optionSpec8 = optionParser.accepts("safeMode", "Loads level with vanilla datapack only");
        AbstractOptionSpec optionSpec9 = optionParser.accepts("help").forHelp();
        ArgumentAcceptingOptionSpec<String> optionSpec10 = optionParser.accepts("universe").withRequiredArg().defaultsTo(".", (String[])new String[0]);
        ArgumentAcceptingOptionSpec<String> optionSpec11 = optionParser.accepts("world").withRequiredArg();
        ArgumentAcceptingOptionSpec<Integer> optionSpec12 = optionParser.accepts("port").withRequiredArg().ofType(Integer.class).defaultsTo(-1, (Integer[])new Integer[0]);
        ArgumentAcceptingOptionSpec<String> optionSpec13 = optionParser.accepts("serverId").withRequiredArg();
        OptionSpecBuilder optionSpec14 = optionParser.accepts("jfrProfile");
        ArgumentAcceptingOptionSpec<Path> optionSpec15 = optionParser.accepts("pidFile").withRequiredArg().withValuesConvertedBy(new PathConverter(new PathProperties[0]));
        NonOptionArgumentSpec<String> optionSpec16 = optionParser.nonOptions();
        try {
            SaveLoader lv10;
            Dynamic<?> dynamic;
            OptionSet optionSet = optionParser.parse(args);
            if (optionSet.has(optionSpec9)) {
                optionParser.printHelpOn(System.err);
                return;
            }
            Path path = optionSet.valueOf(optionSpec15);
            if (path != null) {
                Main.writePidFile(path);
            }
            CrashReport.initCrashReport();
            if (optionSet.has(optionSpec14)) {
                FlightProfiler.INSTANCE.start(InstanceType.SERVER);
            }
            Bootstrap.initialize();
            Bootstrap.logMissing();
            Util.startTimerHack();
            Path path2 = Paths.get("server.properties", new String[0]);
            ServerPropertiesLoader lv = new ServerPropertiesLoader(path2);
            lv.store();
            ChunkCompressionFormat.setCurrentFormat(lv.getPropertiesHandler().regionFileCompression);
            Path path3 = Paths.get("eula.txt", new String[0]);
            EulaReader lv2 = new EulaReader(path3);
            if (optionSet.has(optionSpec2)) {
                LOGGER.info("Initialized '{}' and '{}'", (Object)path2.toAbsolutePath(), (Object)path3.toAbsolutePath());
                return;
            }
            if (!lv2.isEulaAgreedTo()) {
                LOGGER.info("You need to agree to the EULA in order to run the server. Go to eula.txt for more info.");
                return;
            }
            File file = new File(optionSet.valueOf(optionSpec10));
            ApiServices lv3 = ApiServices.create(new YggdrasilAuthenticationService(Proxy.NO_PROXY), file);
            String string = Optional.ofNullable(optionSet.valueOf(optionSpec11)).orElse(lv.getPropertiesHandler().levelName);
            LevelStorage lv4 = LevelStorage.create(file.toPath());
            LevelStorage.Session lv5 = lv4.createSession(string);
            if (lv5.levelDatExists()) {
                LevelSummary lv6;
                try {
                    dynamic = lv5.readLevelProperties();
                    lv6 = lv5.getLevelSummary(dynamic);
                } catch (IOException | NbtCrashException | NbtException exception) {
                    LevelStorage.LevelSave lv7 = lv5.getDirectory();
                    LOGGER.warn("Failed to load world data from {}", (Object)lv7.getLevelDatPath(), (Object)exception);
                    LOGGER.info("Attempting to use fallback");
                    try {
                        dynamic = lv5.readOldLevelProperties();
                        lv6 = lv5.getLevelSummary(dynamic);
                    } catch (IOException | NbtCrashException | NbtException exception2) {
                        LOGGER.error("Failed to load world data from {}", (Object)lv7.getLevelDatOldPath(), (Object)exception2);
                        LOGGER.error("Failed to load world data from {} and {}. World files may be corrupted. Shutting down.", (Object)lv7.getLevelDatPath(), (Object)lv7.getLevelDatOldPath());
                        return;
                    }
                    lv5.tryRestoreBackup();
                }
                if (lv6.requiresConversion()) {
                    LOGGER.info("This world must be opened in an older version (like 1.6.4) to be safely converted");
                    return;
                }
                if (!lv6.isVersionAvailable()) {
                    LOGGER.info("This world was created by an incompatible version.");
                    return;
                }
            } else {
                dynamic = null;
            }
            Dynamic<?> dynamic2 = dynamic;
            boolean bl = optionSet.has(optionSpec8);
            if (bl) {
                LOGGER.warn("Safe mode active, only vanilla datapack will be loaded");
            }
            ResourcePackManager lv8 = VanillaDataPackProvider.createManager(lv5);
            try {
                SaveLoading.ServerConfig lv9 = Main.createServerConfig(lv.getPropertiesHandler(), dynamic2, bl, lv8);
                lv10 = (SaveLoader)Util.waitAndApply(applyExecutor -> SaveLoading.load(lv9, context -> {
                    DimensionOptionsRegistryHolder lv5;
                    GeneratorOptions lv4;
                    LevelInfo lv3;
                    Registry<DimensionOptions> lv = context.dimensionsRegistryManager().get(RegistryKeys.DIMENSION);
                    if (dynamic2 != null) {
                        ParsedSaveProperties lv2 = LevelStorage.parseSaveProperties(dynamic2, context.dataConfiguration(), lv, context.worldGenRegistryManager());
                        return new SaveLoading.LoadContext<SaveProperties>(lv2.properties(), lv2.dimensions().toDynamicRegistryManager());
                    }
                    LOGGER.info("No existing world data, creating new world");
                    if (optionSet.has(optionSpec3)) {
                        lv3 = MinecraftServer.DEMO_LEVEL_INFO;
                        lv4 = GeneratorOptions.DEMO_OPTIONS;
                        lv5 = WorldPresets.createDemoOptions(context.worldGenRegistryManager());
                    } else {
                        ServerPropertiesHandler lv6 = lv.getPropertiesHandler();
                        lv3 = new LevelInfo(lv6.levelName, lv6.gameMode, lv6.hardcore, lv6.difficulty, false, new GameRules(), context.dataConfiguration());
                        lv4 = optionSet.has(optionSpec4) ? lv6.generatorOptions.withBonusChest(true) : lv6.generatorOptions;
                        lv5 = lv6.createDimensionsRegistryHolder(context.worldGenRegistryManager());
                    }
                    DimensionOptionsRegistryHolder.DimensionsConfig lv7 = lv5.toConfig(lv);
                    Lifecycle lifecycle = lv7.getLifecycle().add(context.worldGenRegistryManager().getRegistryLifecycle());
                    return new SaveLoading.LoadContext<LevelProperties>(new LevelProperties(lv3, lv4, lv7.specialWorldProperty(), lifecycle), lv7.toDynamicRegistryManager());
                }, SaveLoader::new, Util.getMainWorkerExecutor(), applyExecutor)).get();
            } catch (Exception exception3) {
                LOGGER.warn("Failed to load datapacks, can't proceed with server load. You can either fix your datapacks or reset to vanilla with --safeMode", exception3);
                return;
            }
            DynamicRegistryManager.Immutable lv11 = lv10.combinedDynamicRegistries().getCombinedRegistryManager();
            boolean bl2 = optionSet.has(optionSpec7);
            if (optionSet.has(optionSpec5) || bl2) {
                Main.forceUpgradeWorld(lv5, Schemas.getFixer(), optionSet.has(optionSpec6), () -> true, lv11, bl2);
            }
            SaveProperties lv12 = lv10.saveProperties();
            lv5.backupLevelDataFile(lv11, lv12);
            final MinecraftDedicatedServer lv13 = MinecraftServer.startServer(thread -> {
                boolean bl;
                MinecraftDedicatedServer lv = new MinecraftDedicatedServer((Thread)thread, lv5, lv8, lv10, lv, Schemas.getFixer(), lv3, WorldGenerationProgressLogger::create);
                lv.setServerPort((Integer)optionSet.valueOf(optionSpec12));
                lv.setDemo(optionSet.has(optionSpec3));
                lv.setServerId((String)optionSet.valueOf(optionSpec13));
                boolean bl2 = bl = !optionSet.has(optionSpec) && !optionSet.valuesOf(optionSpec16).contains("nogui");
                if (bl && !GraphicsEnvironment.isHeadless()) {
                    lv.createGui();
                }
                return lv;
            });
            Thread thread2 = new Thread("Server Shutdown Thread"){

                @Override
                public void run() {
                    lv13.stop(true);
                }
            };
            thread2.setUncaughtExceptionHandler(new UncaughtExceptionLogger(LOGGER));
            Runtime.getRuntime().addShutdownHook(thread2);
        } catch (Exception exception4) {
            LOGGER.error(LogUtils.FATAL_MARKER, "Failed to start the minecraft server", exception4);
        }
    }

    private static void writePidFile(Path path) {
        try {
            long l = ProcessHandle.current().pid();
            Files.writeString(path, (CharSequence)Long.toString(l), new OpenOption[0]);
        } catch (IOException iOException) {
            throw new UncheckedIOException(iOException);
        }
    }

    private static SaveLoading.ServerConfig createServerConfig(ServerPropertiesHandler serverPropertiesHandler, @Nullable Dynamic<?> dynamic, boolean safeMode, ResourcePackManager dataPackManager) {
        DataConfiguration lv2;
        boolean bl2;
        if (dynamic != null) {
            DataConfiguration lv = LevelStorage.parseDataPackSettings(dynamic);
            bl2 = false;
            lv2 = lv;
        } else {
            bl2 = true;
            lv2 = new DataConfiguration(serverPropertiesHandler.dataPackSettings, FeatureFlags.DEFAULT_ENABLED_FEATURES);
        }
        SaveLoading.DataPacks lv3 = new SaveLoading.DataPacks(dataPackManager, lv2, safeMode, bl2);
        return new SaveLoading.ServerConfig(lv3, CommandManager.RegistrationEnvironment.DEDICATED, serverPropertiesHandler.functionPermissionLevel);
    }

    private static void forceUpgradeWorld(LevelStorage.Session session, DataFixer dataFixer, boolean eraseCache, BooleanSupplier continueCheck, DynamicRegistryManager dynamicRegistryManager, boolean recreateRegionFiles) {
        LOGGER.info("Forcing world upgrade!");
        WorldUpdater lv = new WorldUpdater(session, dataFixer, dynamicRegistryManager, eraseCache, recreateRegionFiles);
        Text lv2 = null;
        while (!lv.isDone()) {
            int i;
            Text lv3 = lv.getStatus();
            if (lv2 != lv3) {
                lv2 = lv3;
                LOGGER.info(lv.getStatus().getString());
            }
            if ((i = lv.getTotalChunkCount()) > 0) {
                int j = lv.getUpgradedChunkCount() + lv.getSkippedChunkCount();
                LOGGER.info("{}% completed ({} / {} chunks)...", MathHelper.floor((float)j / (float)i * 100.0f), j, i);
            }
            if (!continueCheck.getAsBoolean()) {
                lv.cancel();
                continue;
            }
            try {
                Thread.sleep(1000L);
            } catch (InterruptedException interruptedException) {}
        }
    }
}

