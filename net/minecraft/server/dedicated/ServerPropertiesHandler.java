/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 * 
 * Could not load the following classes:
 *  org.jetbrains.annotations.Nullable
 */
package net.minecraft.server.dedicated;

import com.google.common.base.Splitter;
import com.google.common.base.Strings;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.mojang.logging.LogUtils;
import com.mojang.serialization.Dynamic;
import com.mojang.serialization.JsonOps;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.Properties;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;
import net.minecraft.registry.DynamicRegistryManager;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.RegistryOps;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.resource.DataConfiguration;
import net.minecraft.resource.DataPackSettings;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.dedicated.AbstractPropertiesHandler;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.JsonHelper;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.Difficulty;
import net.minecraft.world.GameMode;
import net.minecraft.world.dimension.DimensionOptionsRegistryHolder;
import net.minecraft.world.gen.GeneratorOptions;
import net.minecraft.world.gen.WorldPreset;
import net.minecraft.world.gen.WorldPresets;
import net.minecraft.world.gen.chunk.FlatChunkGenerator;
import net.minecraft.world.gen.chunk.FlatChunkGeneratorConfig;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;

public class ServerPropertiesHandler
extends AbstractPropertiesHandler<ServerPropertiesHandler> {
    static final Logger LOGGER = LogUtils.getLogger();
    private static final Pattern SHA1_PATTERN = Pattern.compile("^[a-fA-F0-9]{40}$");
    private static final Splitter COMMA_SPLITTER = Splitter.on(',').trimResults();
    public final boolean onlineMode = this.parseBoolean("online-mode", true);
    public final boolean preventProxyConnections = this.parseBoolean("prevent-proxy-connections", false);
    public final String serverIp = this.getString("server-ip", "");
    public final boolean spawnAnimals = this.parseBoolean("spawn-animals", true);
    public final boolean spawnNpcs = this.parseBoolean("spawn-npcs", true);
    public final boolean pvp = this.parseBoolean("pvp", true);
    public final boolean allowFlight = this.parseBoolean("allow-flight", false);
    public final String motd = this.getString("motd", "A Minecraft Server");
    public final String bugReportLink = this.getString("bug-report-link", "");
    public final boolean forceGameMode = this.parseBoolean("force-gamemode", false);
    public final boolean enforceWhitelist = this.parseBoolean("enforce-whitelist", false);
    public final Difficulty difficulty = this.get("difficulty", ServerPropertiesHandler.combineParser(Difficulty::byId, Difficulty::byName), Difficulty::getName, Difficulty.EASY);
    public final GameMode gameMode = this.get("gamemode", ServerPropertiesHandler.combineParser(GameMode::byId, GameMode::byName), GameMode::getName, GameMode.SURVIVAL);
    public final String levelName = this.getString("level-name", "world");
    public final int serverPort = this.getInt("server-port", 25565);
    @Nullable
    public final Boolean announcePlayerAchievements = this.getDeprecatedBoolean("announce-player-achievements");
    public final boolean enableQuery = this.parseBoolean("enable-query", false);
    public final int queryPort = this.getInt("query.port", 25565);
    public final boolean enableRcon = this.parseBoolean("enable-rcon", false);
    public final int rconPort = this.getInt("rcon.port", 25575);
    public final String rconPassword = this.getString("rcon.password", "");
    public final boolean hardcore = this.parseBoolean("hardcore", false);
    public final boolean allowNether = this.parseBoolean("allow-nether", true);
    public final boolean spawnMonsters = this.parseBoolean("spawn-monsters", true);
    public final boolean useNativeTransport = this.parseBoolean("use-native-transport", true);
    public final boolean enableCommandBlock = this.parseBoolean("enable-command-block", false);
    public final int spawnProtection = this.getInt("spawn-protection", 16);
    public final int opPermissionLevel = this.getInt("op-permission-level", 4);
    public final int functionPermissionLevel = this.getInt("function-permission-level", 2);
    public final long maxTickTime = this.parseLong("max-tick-time", TimeUnit.MINUTES.toMillis(1L));
    public final int maxChainedNeighborUpdates = this.getInt("max-chained-neighbor-updates", 1000000);
    public final int rateLimit = this.getInt("rate-limit", 0);
    public final int viewDistance = this.getInt("view-distance", 10);
    public final int simulationDistance = this.getInt("simulation-distance", 10);
    public final int maxPlayers = this.getInt("max-players", 20);
    public final int networkCompressionThreshold = this.getInt("network-compression-threshold", 256);
    public final boolean broadcastRconToOps = this.parseBoolean("broadcast-rcon-to-ops", true);
    public final boolean broadcastConsoleToOps = this.parseBoolean("broadcast-console-to-ops", true);
    public final int maxWorldSize = this.transformedParseInt("max-world-size", maxWorldSize -> MathHelper.clamp(maxWorldSize, 1, 29999984), 29999984);
    public final boolean syncChunkWrites = this.parseBoolean("sync-chunk-writes", true);
    public final String regionFileCompression = this.getString("region-file-compression", "deflate");
    public final boolean enableJmxMonitoring = this.parseBoolean("enable-jmx-monitoring", false);
    public final boolean enableStatus = this.parseBoolean("enable-status", true);
    public final boolean hideOnlinePlayers = this.parseBoolean("hide-online-players", false);
    public final int entityBroadcastRangePercentage = this.transformedParseInt("entity-broadcast-range-percentage", percentage -> MathHelper.clamp(percentage, 10, 1000), 100);
    public final String textFilteringConfig = this.getString("text-filtering-config", "");
    public final Optional<MinecraftServer.ServerResourcePackProperties> serverResourcePackProperties;
    public final DataPackSettings dataPackSettings;
    public final AbstractPropertiesHandler.PropertyAccessor<Integer> playerIdleTimeout = this.intAccessor("player-idle-timeout", 0);
    public final AbstractPropertiesHandler.PropertyAccessor<Boolean> whiteList = this.booleanAccessor("white-list", false);
    public final boolean enforceSecureProfile = this.parseBoolean("enforce-secure-profile", true);
    public final boolean logIps = this.parseBoolean("log-ips", true);
    private final WorldGenProperties worldGenProperties;
    public final GeneratorOptions generatorOptions;
    public boolean acceptsTransfers = this.parseBoolean("accepts-transfers", false);

    public ServerPropertiesHandler(Properties properties) {
        super(properties);
        String string = this.getString("level-seed", "");
        boolean bl = this.parseBoolean("generate-structures", true);
        long l = GeneratorOptions.parseSeed(string).orElse(GeneratorOptions.getRandomSeed());
        this.generatorOptions = new GeneratorOptions(l, bl, false);
        this.worldGenProperties = new WorldGenProperties(this.get("generator-settings", generatorSettings -> JsonHelper.deserialize(!generatorSettings.isEmpty() ? generatorSettings : "{}"), new JsonObject()), this.get("level-type", type -> type.toLowerCase(Locale.ROOT), WorldPresets.DEFAULT.getValue().toString()));
        this.serverResourcePackProperties = ServerPropertiesHandler.getServerResourcePackProperties(this.getString("resource-pack-id", ""), this.getString("resource-pack", ""), this.getString("resource-pack-sha1", ""), this.getDeprecatedString("resource-pack-hash"), this.parseBoolean("require-resource-pack", false), this.getString("resource-pack-prompt", ""));
        this.dataPackSettings = ServerPropertiesHandler.parseDataPackSettings(this.getString("initial-enabled-packs", String.join((CharSequence)",", DataConfiguration.SAFE_MODE.dataPacks().getEnabled())), this.getString("initial-disabled-packs", String.join((CharSequence)",", DataConfiguration.SAFE_MODE.dataPacks().getDisabled())));
    }

    public static ServerPropertiesHandler load(Path path) {
        return new ServerPropertiesHandler(ServerPropertiesHandler.loadProperties(path));
    }

    @Override
    protected ServerPropertiesHandler create(DynamicRegistryManager arg, Properties properties) {
        return new ServerPropertiesHandler(properties);
    }

    @Nullable
    private static Text parseResourcePackPrompt(String prompt) {
        if (!Strings.isNullOrEmpty(prompt)) {
            try {
                return Text.Serialization.fromJson(prompt, (RegistryWrapper.WrapperLookup)DynamicRegistryManager.EMPTY);
            } catch (Exception exception) {
                LOGGER.warn("Failed to parse resource pack prompt '{}'", (Object)prompt, (Object)exception);
            }
        }
        return null;
    }

    private static Optional<MinecraftServer.ServerResourcePackProperties> getServerResourcePackProperties(String id, String url, String sha1, @Nullable String hash, boolean required, String prompt) {
        UUID uUID;
        String string6;
        if (url.isEmpty()) {
            return Optional.empty();
        }
        if (!sha1.isEmpty()) {
            string6 = sha1;
            if (!Strings.isNullOrEmpty(hash)) {
                LOGGER.warn("resource-pack-hash is deprecated and found along side resource-pack-sha1. resource-pack-hash will be ignored.");
            }
        } else if (!Strings.isNullOrEmpty(hash)) {
            LOGGER.warn("resource-pack-hash is deprecated. Please use resource-pack-sha1 instead.");
            string6 = hash;
        } else {
            string6 = "";
        }
        if (string6.isEmpty()) {
            LOGGER.warn("You specified a resource pack without providing a sha1 hash. Pack will be updated on the client only if you change the name of the pack.");
        } else if (!SHA1_PATTERN.matcher(string6).matches()) {
            LOGGER.warn("Invalid sha1 for resource-pack-sha1");
        }
        Text lv = ServerPropertiesHandler.parseResourcePackPrompt(prompt);
        if (id.isEmpty()) {
            uUID = UUID.nameUUIDFromBytes(url.getBytes(StandardCharsets.UTF_8));
            LOGGER.warn("resource-pack-id missing, using default of {}", (Object)uUID);
        } else {
            try {
                uUID = UUID.fromString(id);
            } catch (IllegalArgumentException illegalArgumentException) {
                LOGGER.warn("Failed to parse '{}' into UUID", (Object)id);
                return Optional.empty();
            }
        }
        return Optional.of(new MinecraftServer.ServerResourcePackProperties(uUID, url, string6, required, lv));
    }

    private static DataPackSettings parseDataPackSettings(String enabled, String disabled) {
        List<String> list = COMMA_SPLITTER.splitToList(enabled);
        List<String> list2 = COMMA_SPLITTER.splitToList(disabled);
        return new DataPackSettings(list, list2);
    }

    public DimensionOptionsRegistryHolder createDimensionsRegistryHolder(DynamicRegistryManager dynamicRegistry) {
        return this.worldGenProperties.createDimensionsRegistryHolder(dynamicRegistry);
    }

    @Override
    protected /* synthetic */ AbstractPropertiesHandler create(DynamicRegistryManager registryManager, Properties properties) {
        return this.create(registryManager, properties);
    }

    record WorldGenProperties(JsonObject generatorSettings, String levelType) {
        private static final Map<String, RegistryKey<WorldPreset>> LEVEL_TYPE_TO_PRESET_KEY = Map.of("default", WorldPresets.DEFAULT, "largebiomes", WorldPresets.LARGE_BIOMES);

        public DimensionOptionsRegistryHolder createDimensionsRegistryHolder(DynamicRegistryManager dynamicRegistryManager) {
            Registry<WorldPreset> lv = dynamicRegistryManager.get(RegistryKeys.WORLD_PRESET);
            RegistryEntry.Reference<WorldPreset> lv2 = lv.getEntry(WorldPresets.DEFAULT).or(() -> lv.streamEntries().findAny()).orElseThrow(() -> new IllegalStateException("Invalid datapack contents: can't find default preset"));
            RegistryEntry lv3 = Optional.ofNullable(Identifier.tryParse(this.levelType)).map(levelTypeId -> RegistryKey.of(RegistryKeys.WORLD_PRESET, levelTypeId)).or(() -> Optional.ofNullable(LEVEL_TYPE_TO_PRESET_KEY.get(this.levelType))).flatMap(lv::getEntry).orElseGet(() -> {
                LOGGER.warn("Failed to parse level-type {}, defaulting to {}", (Object)this.levelType, (Object)lv2.registryKey().getValue());
                return lv2;
            });
            DimensionOptionsRegistryHolder lv4 = ((WorldPreset)lv3.value()).createDimensionsRegistryHolder();
            if (lv3.matchesKey(WorldPresets.FLAT)) {
                RegistryOps<JsonElement> lv5 = dynamicRegistryManager.getOps(JsonOps.INSTANCE);
                Optional optional = FlatChunkGeneratorConfig.CODEC.parse(new Dynamic<JsonObject>(lv5, this.generatorSettings())).resultOrPartial(LOGGER::error);
                if (optional.isPresent()) {
                    return lv4.with(dynamicRegistryManager, new FlatChunkGenerator((FlatChunkGeneratorConfig)optional.get()));
                }
            }
            return lv4;
        }
    }
}

