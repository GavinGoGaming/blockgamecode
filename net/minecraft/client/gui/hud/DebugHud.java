/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 * 
 * Could not load the following classes:
 *  net.fabricmc.api.EnvType
 *  net.fabricmc.api.Environment
 *  org.jetbrains.annotations.Nullable
 */
package net.minecraft.client.gui.hud;

import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import com.mojang.blaze3d.platform.GlDebugInfo;
import com.mojang.datafixers.DataFixUtils;
import it.unimi.dsi.fastutil.longs.LongSets;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import java.lang.management.GarbageCollectorMXBean;
import java.lang.management.ManagementFactory;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.SharedConstants;
import net.minecraft.block.BlockState;
import net.minecraft.client.ClientBrandRetriever;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gl.PostEffectProcessor;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.hud.debug.PacketSizeChart;
import net.minecraft.client.gui.hud.debug.PingChart;
import net.minecraft.client.gui.hud.debug.RenderingChart;
import net.minecraft.client.gui.hud.debug.TickChart;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.entity.Entity;
import net.minecraft.entity.SpawnGroup;
import net.minecraft.fluid.FluidState;
import net.minecraft.network.ClientConnection;
import net.minecraft.registry.Registries;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.server.ServerTickManager;
import net.minecraft.server.integrated.IntegratedServer;
import net.minecraft.server.world.ServerChunkManager;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.state.property.Property;
import net.minecraft.util.Formatting;
import net.minecraft.util.Util;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.ChunkSectionPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.profiler.MultiValueDebugSampleLogImpl;
import net.minecraft.util.profiler.ServerTickType;
import net.minecraft.util.profiler.log.DebugSampleType;
import net.minecraft.world.Heightmap;
import net.minecraft.world.LightType;
import net.minecraft.world.LocalDifficulty;
import net.minecraft.world.SpawnHelper;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.biome.source.BiomeSource;
import net.minecraft.world.biome.source.util.MultiNoiseUtil;
import net.minecraft.world.chunk.ChunkStatus;
import net.minecraft.world.chunk.WorldChunk;
import net.minecraft.world.gen.chunk.ChunkGenerator;
import net.minecraft.world.gen.noise.NoiseConfig;
import net.minecraft.world.tick.TickManager;
import org.jetbrains.annotations.Nullable;

@Environment(value=EnvType.CLIENT)
public class DebugHud {
    private static final int TEXT_COLOR = 0xE0E0E0;
    private static final int field_32188 = 2;
    private static final int field_32189 = 2;
    private static final int field_32190 = 2;
    private static final Map<Heightmap.Type, String> HEIGHT_MAP_TYPES = Util.make(new EnumMap(Heightmap.Type.class), types -> {
        types.put(Heightmap.Type.WORLD_SURFACE_WG, "SW");
        types.put(Heightmap.Type.WORLD_SURFACE, "S");
        types.put(Heightmap.Type.OCEAN_FLOOR_WG, "OW");
        types.put(Heightmap.Type.OCEAN_FLOOR, "O");
        types.put(Heightmap.Type.MOTION_BLOCKING, "M");
        types.put(Heightmap.Type.MOTION_BLOCKING_NO_LEAVES, "ML");
    });
    private final MinecraftClient client;
    private final AllocationRateCalculator allocationRateCalculator;
    private final TextRenderer textRenderer;
    private HitResult blockHit;
    private HitResult fluidHit;
    @Nullable
    private ChunkPos pos;
    @Nullable
    private WorldChunk chunk;
    @Nullable
    private CompletableFuture<WorldChunk> chunkFuture;
    private boolean showDebugHud;
    private boolean renderingChartVisible;
    private boolean renderingAndTickChartsVisible;
    private boolean packetSizeAndPingChartsVisible;
    private final MultiValueDebugSampleLogImpl frameNanosLog = new MultiValueDebugSampleLogImpl(1);
    private final MultiValueDebugSampleLogImpl tickNanosLog = new MultiValueDebugSampleLogImpl(ServerTickType.values().length);
    private final MultiValueDebugSampleLogImpl pingLog = new MultiValueDebugSampleLogImpl(1);
    private final MultiValueDebugSampleLogImpl packetSizeLog = new MultiValueDebugSampleLogImpl(1);
    private final Map<DebugSampleType, MultiValueDebugSampleLogImpl> receivedDebugSamples = Map.of(DebugSampleType.TICK_TIME, this.tickNanosLog);
    private final RenderingChart renderingChart;
    private final TickChart tickChart;
    private final PingChart pingChart;
    private final PacketSizeChart packetSizeChart;

    public DebugHud(MinecraftClient client) {
        this.client = client;
        this.allocationRateCalculator = new AllocationRateCalculator();
        this.textRenderer = client.textRenderer;
        this.renderingChart = new RenderingChart(this.textRenderer, this.frameNanosLog);
        this.tickChart = new TickChart(this.textRenderer, this.tickNanosLog, () -> Float.valueOf(arg.world.getTickManager().getMillisPerTick()));
        this.pingChart = new PingChart(this.textRenderer, this.pingLog);
        this.packetSizeChart = new PacketSizeChart(this.textRenderer, this.packetSizeLog);
    }

    public void resetChunk() {
        this.chunkFuture = null;
        this.chunk = null;
    }

    public void render(DrawContext context) {
        this.client.getProfiler().push("debug");
        Entity lv = this.client.getCameraEntity();
        this.blockHit = lv.raycast(20.0, 0.0f, false);
        this.fluidHit = lv.raycast(20.0, 0.0f, true);
        context.draw(() -> {
            int k;
            int j;
            int i;
            this.drawLeftText(context);
            this.drawRightText(context);
            if (this.renderingAndTickChartsVisible) {
                i = context.getScaledWindowWidth();
                j = i / 2;
                this.renderingChart.render(context, 0, this.renderingChart.getWidth(j));
                if (this.tickNanosLog.getLength() > 0) {
                    k = this.tickChart.getWidth(j);
                    this.tickChart.render(context, i - k, k);
                }
            }
            if (this.packetSizeAndPingChartsVisible) {
                i = context.getScaledWindowWidth();
                j = i / 2;
                if (!this.client.isInSingleplayer()) {
                    this.packetSizeChart.render(context, 0, this.packetSizeChart.getWidth(j));
                }
                k = this.pingChart.getWidth(j);
                this.pingChart.render(context, i - k, k);
            }
        });
        this.client.getProfiler().pop();
    }

    protected void drawLeftText(DrawContext context) {
        List<String> list = this.getLeftText();
        list.add("");
        boolean bl = this.client.getServer() != null;
        list.add("Debug charts: [F3+1] Profiler " + (this.renderingChartVisible ? "visible" : "hidden") + "; [F3+2] " + (bl ? "FPS + TPS " : "FPS ") + (this.renderingAndTickChartsVisible ? "visible" : "hidden") + "; [F3+3] " + (!this.client.isInSingleplayer() ? "Bandwidth + Ping" : "Ping") + (this.packetSizeAndPingChartsVisible ? " visible" : " hidden"));
        list.add("For help: press F3 + Q");
        this.drawText(context, list, true);
    }

    protected void drawRightText(DrawContext context) {
        List<String> list = this.getRightText();
        this.drawText(context, list, false);
    }

    private void drawText(DrawContext context, List<String> text, boolean left) {
        int m;
        int l;
        int k;
        String string;
        int j;
        int i = this.textRenderer.fontHeight;
        for (j = 0; j < text.size(); ++j) {
            string = text.get(j);
            if (Strings.isNullOrEmpty(string)) continue;
            k = this.textRenderer.getWidth(string);
            l = left ? 2 : context.getScaledWindowWidth() - 2 - k;
            m = 2 + i * j;
            context.fill(l - 1, m - 1, l + k + 1, m + i - 1, -1873784752);
        }
        for (j = 0; j < text.size(); ++j) {
            string = text.get(j);
            if (Strings.isNullOrEmpty(string)) continue;
            k = this.textRenderer.getWidth(string);
            l = left ? 2 : context.getScaledWindowWidth() - 2 - k;
            m = 2 + i * j;
            context.drawText(this.textRenderer, string, l, m, 0xE0E0E0, false);
        }
    }

    protected List<String> getLeftText() {
        PostEffectProcessor lv22;
        World lv10;
        String string3;
        IntegratedServer lv = this.client.getServer();
        ClientPlayNetworkHandler lv2 = this.client.getNetworkHandler();
        ClientConnection lv3 = lv2.getConnection();
        float f = lv3.getAveragePacketsSent();
        float g = lv3.getAveragePacketsReceived();
        TickManager lv4 = this.getWorld().getTickManager();
        String string = lv4.isStepping() ? " (frozen - stepping)" : (lv4.isFrozen() ? " (frozen)" : "");
        if (lv != null) {
            ServerTickManager lv5 = lv.getTickManager();
            boolean bl = lv5.isSprinting();
            if (bl) {
                string = " (sprinting)";
            }
            String string2 = bl ? "-" : String.format(Locale.ROOT, "%.1f", Float.valueOf(lv4.getMillisPerTick()));
            string3 = String.format(Locale.ROOT, "Integrated server @ %.1f/%s ms%s, %.0f tx, %.0f rx", Float.valueOf(lv.getAverageTickTime()), string2, string, Float.valueOf(f), Float.valueOf(g));
        } else {
            string3 = String.format(Locale.ROOT, "\"%s\" server%s, %.0f tx, %.0f rx", lv2.getBrand(), string, Float.valueOf(f), Float.valueOf(g));
        }
        BlockPos lv6 = this.client.getCameraEntity().getBlockPos();
        if (this.client.hasReducedDebugInfo()) {
            return Lists.newArrayList("Minecraft " + SharedConstants.getGameVersion().getName() + " (" + this.client.getGameVersion() + "/" + ClientBrandRetriever.getClientModName() + ")", this.client.fpsDebugString, string3, this.client.worldRenderer.getChunksDebugString(), this.client.worldRenderer.getEntitiesDebugString(), "P: " + this.client.particleManager.getDebugString() + ". T: " + this.client.world.getRegularEntityCount(), this.client.world.asString(), "", String.format(Locale.ROOT, "Chunk-relative: %d %d %d", lv6.getX() & 0xF, lv6.getY() & 0xF, lv6.getZ() & 0xF));
        }
        Entity lv7 = this.client.getCameraEntity();
        Direction lv8 = lv7.getHorizontalFacing();
        String string4 = switch (lv8) {
            case Direction.NORTH -> "Towards negative Z";
            case Direction.SOUTH -> "Towards positive Z";
            case Direction.WEST -> "Towards negative X";
            case Direction.EAST -> "Towards positive X";
            default -> "Invalid";
        };
        ChunkPos lv9 = new ChunkPos(lv6);
        if (!Objects.equals(this.pos, lv9)) {
            this.pos = lv9;
            this.resetChunk();
        }
        LongSets.EmptySet longSet = (lv10 = this.getWorld()) instanceof ServerWorld ? ((ServerWorld)lv10).getForcedChunks() : LongSets.EMPTY_SET;
        ArrayList<String> list = Lists.newArrayList("Minecraft " + SharedConstants.getGameVersion().getName() + " (" + this.client.getGameVersion() + "/" + ClientBrandRetriever.getClientModName() + (String)("release".equalsIgnoreCase(this.client.getVersionType()) ? "" : "/" + this.client.getVersionType()) + ")", this.client.fpsDebugString, string3, this.client.worldRenderer.getChunksDebugString(), this.client.worldRenderer.getEntitiesDebugString(), "P: " + this.client.particleManager.getDebugString() + ". T: " + this.client.world.getRegularEntityCount(), this.client.world.asString());
        String string5 = this.getServerWorldDebugString();
        if (string5 != null) {
            list.add(string5);
        }
        list.add(String.valueOf(this.client.world.getRegistryKey().getValue()) + " FC: " + longSet.size());
        list.add("");
        list.add(String.format(Locale.ROOT, "XYZ: %.3f / %.5f / %.3f", this.client.getCameraEntity().getX(), this.client.getCameraEntity().getY(), this.client.getCameraEntity().getZ()));
        list.add(String.format(Locale.ROOT, "Block: %d %d %d [%d %d %d]", lv6.getX(), lv6.getY(), lv6.getZ(), lv6.getX() & 0xF, lv6.getY() & 0xF, lv6.getZ() & 0xF));
        list.add(String.format(Locale.ROOT, "Chunk: %d %d %d [%d %d in r.%d.%d.mca]", lv9.x, ChunkSectionPos.getSectionCoord(lv6.getY()), lv9.z, lv9.getRegionRelativeX(), lv9.getRegionRelativeZ(), lv9.getRegionX(), lv9.getRegionZ()));
        list.add(String.format(Locale.ROOT, "Facing: %s (%s) (%.1f / %.1f)", lv8, string4, Float.valueOf(MathHelper.wrapDegrees(lv7.getYaw())), Float.valueOf(MathHelper.wrapDegrees(lv7.getPitch()))));
        WorldChunk lv11 = this.getClientChunk();
        if (lv11.isEmpty()) {
            list.add("Waiting for chunk...");
        } else {
            int i = this.client.world.getChunkManager().getLightingProvider().getLight(lv6, 0);
            int j = this.client.world.getLightLevel(LightType.SKY, lv6);
            int k = this.client.world.getLightLevel(LightType.BLOCK, lv6);
            list.add("Client Light: " + i + " (" + j + " sky, " + k + " block)");
            WorldChunk lv12 = this.getChunk();
            StringBuilder stringBuilder = new StringBuilder("CH");
            for (Heightmap.Type lv13 : Heightmap.Type.values()) {
                if (!lv13.shouldSendToClient()) continue;
                stringBuilder.append(" ").append(HEIGHT_MAP_TYPES.get(lv13)).append(": ").append(lv11.sampleHeightmap(lv13, lv6.getX(), lv6.getZ()));
            }
            list.add(stringBuilder.toString());
            stringBuilder.setLength(0);
            stringBuilder.append("SH");
            for (Heightmap.Type lv13 : Heightmap.Type.values()) {
                if (!lv13.isStoredServerSide()) continue;
                stringBuilder.append(" ").append(HEIGHT_MAP_TYPES.get(lv13)).append(": ");
                if (lv12 != null) {
                    stringBuilder.append(lv12.sampleHeightmap(lv13, lv6.getX(), lv6.getZ()));
                    continue;
                }
                stringBuilder.append("??");
            }
            list.add(stringBuilder.toString());
            if (lv6.getY() >= this.client.world.getBottomY() && lv6.getY() < this.client.world.getTopY()) {
                list.add("Biome: " + DebugHud.getBiomeString(this.client.world.getBiome(lv6)));
                if (lv12 != null) {
                    float h = lv10.getMoonSize();
                    long l = lv12.getInhabitedTime();
                    LocalDifficulty lv14 = new LocalDifficulty(lv10.getDifficulty(), lv10.getTimeOfDay(), l, h);
                    list.add(String.format(Locale.ROOT, "Local Difficulty: %.2f // %.2f (Day %d)", Float.valueOf(lv14.getLocalDifficulty()), Float.valueOf(lv14.getClampedLocalDifficulty()), this.client.world.getTimeOfDay() / 24000L));
                } else {
                    list.add("Local Difficulty: ??");
                }
            }
            if (lv12 != null && lv12.usesOldNoise()) {
                list.add("Blending: Old");
            }
        }
        ServerWorld lv15 = this.getServerWorld();
        if (lv15 != null) {
            ServerChunkManager lv16 = lv15.getChunkManager();
            ChunkGenerator lv17 = lv16.getChunkGenerator();
            NoiseConfig lv18 = lv16.getNoiseConfig();
            lv17.getDebugHudText(list, lv18, lv6);
            MultiNoiseUtil.MultiNoiseSampler lv19 = lv18.getMultiNoiseSampler();
            BiomeSource lv20 = lv17.getBiomeSource();
            lv20.addDebugInfo(list, lv6, lv19);
            SpawnHelper.Info lv21 = lv16.getSpawnInfo();
            if (lv21 != null) {
                Object2IntMap<SpawnGroup> object2IntMap = lv21.getGroupToCount();
                int m = lv21.getSpawningChunkCount();
                list.add("SC: " + m + ", " + Stream.of(SpawnGroup.values()).map(group -> Character.toUpperCase(group.getName().charAt(0)) + ": " + object2IntMap.getInt(group)).collect(Collectors.joining(", ")));
            } else {
                list.add("SC: N/A");
            }
        }
        if ((lv22 = this.client.gameRenderer.getPostProcessor()) != null) {
            list.add("Shader: " + lv22.getName());
        }
        list.add(this.client.getSoundManager().getDebugString() + String.format(Locale.ROOT, " (Mood %d%%)", Math.round(this.client.player.getMoodPercentage() * 100.0f)));
        return list;
    }

    private static String getBiomeString(RegistryEntry<Biome> biome) {
        return biome.getKeyOrValue().map(biomeKey -> biomeKey.getValue().toString(), biome_ -> "[unregistered " + String.valueOf(biome_) + "]");
    }

    @Nullable
    private ServerWorld getServerWorld() {
        IntegratedServer lv = this.client.getServer();
        if (lv != null) {
            return lv.getWorld(this.client.world.getRegistryKey());
        }
        return null;
    }

    @Nullable
    private String getServerWorldDebugString() {
        ServerWorld lv = this.getServerWorld();
        if (lv != null) {
            return lv.asString();
        }
        return null;
    }

    private World getWorld() {
        return DataFixUtils.orElse(Optional.ofNullable(this.client.getServer()).flatMap(server -> Optional.ofNullable(server.getWorld(this.client.world.getRegistryKey()))), this.client.world);
    }

    @Nullable
    private WorldChunk getChunk() {
        if (this.chunkFuture == null) {
            ServerWorld lv = this.getServerWorld();
            if (lv == null) {
                return null;
            }
            this.chunkFuture = lv.getChunkManager().getChunkFutureSyncOnMainThread(this.pos.x, this.pos.z, ChunkStatus.FULL, false).thenApply(arg -> arg.orElse(null));
        }
        return this.chunkFuture.getNow(null);
    }

    private WorldChunk getClientChunk() {
        if (this.chunk == null) {
            this.chunk = this.client.world.getChunk(this.pos.x, this.pos.z);
        }
        return this.chunk;
    }

    protected List<String> getRightText() {
        Entity lv4;
        BlockPos lv;
        long l = Runtime.getRuntime().maxMemory();
        long m = Runtime.getRuntime().totalMemory();
        long n = Runtime.getRuntime().freeMemory();
        long o = m - n;
        ArrayList<String> list = Lists.newArrayList(String.format(Locale.ROOT, "Java: %s", System.getProperty("java.version")), String.format(Locale.ROOT, "Mem: %2d%% %03d/%03dMB", o * 100L / l, DebugHud.toMiB(o), DebugHud.toMiB(l)), String.format(Locale.ROOT, "Allocation rate: %03dMB/s", DebugHud.toMiB(this.allocationRateCalculator.get(o))), String.format(Locale.ROOT, "Allocated: %2d%% %03dMB", m * 100L / l, DebugHud.toMiB(m)), "", String.format(Locale.ROOT, "CPU: %s", GlDebugInfo.getCpuInfo()), "", String.format(Locale.ROOT, "Display: %dx%d (%s)", MinecraftClient.getInstance().getWindow().getFramebufferWidth(), MinecraftClient.getInstance().getWindow().getFramebufferHeight(), GlDebugInfo.getVendor()), GlDebugInfo.getRenderer(), GlDebugInfo.getVersion());
        if (this.client.hasReducedDebugInfo()) {
            return list;
        }
        if (this.blockHit.getType() == HitResult.Type.BLOCK) {
            lv = ((BlockHitResult)this.blockHit).getBlockPos();
            BlockState lv2 = this.client.world.getBlockState(lv);
            list.add("");
            list.add(String.valueOf(Formatting.UNDERLINE) + "Targeted Block: " + lv.getX() + ", " + lv.getY() + ", " + lv.getZ());
            list.add(String.valueOf(Registries.BLOCK.getId(lv2.getBlock())));
            for (Map.Entry<Property<?>, Comparable<?>> entry : lv2.getEntries().entrySet()) {
                list.add(this.propertyToString(entry));
            }
            lv2.streamTags().map(tag -> "#" + String.valueOf(tag.id())).forEach(list::add);
        }
        if (this.fluidHit.getType() == HitResult.Type.BLOCK) {
            lv = ((BlockHitResult)this.fluidHit).getBlockPos();
            FluidState lv3 = this.client.world.getFluidState(lv);
            list.add("");
            list.add(String.valueOf(Formatting.UNDERLINE) + "Targeted Fluid: " + lv.getX() + ", " + lv.getY() + ", " + lv.getZ());
            list.add(String.valueOf(Registries.FLUID.getId(lv3.getFluid())));
            for (Map.Entry<Property<?>, Comparable<?>> entry : lv3.getEntries().entrySet()) {
                list.add(this.propertyToString(entry));
            }
            lv3.streamTags().map(tag -> "#" + String.valueOf(tag.id())).forEach(list::add);
        }
        if ((lv4 = this.client.targetedEntity) != null) {
            list.add("");
            list.add(String.valueOf(Formatting.UNDERLINE) + "Targeted Entity");
            list.add(String.valueOf(Registries.ENTITY_TYPE.getId(lv4.getType())));
        }
        return list;
    }

    private String propertyToString(Map.Entry<Property<?>, Comparable<?>> propEntry) {
        Property<?> lv = propEntry.getKey();
        Comparable<?> comparable = propEntry.getValue();
        Object string = Util.getValueAsString(lv, comparable);
        if (Boolean.TRUE.equals(comparable)) {
            string = String.valueOf(Formatting.GREEN) + (String)string;
        } else if (Boolean.FALSE.equals(comparable)) {
            string = String.valueOf(Formatting.RED) + (String)string;
        }
        return lv.getName() + ": " + (String)string;
    }

    private static long toMiB(long bytes) {
        return bytes / 1024L / 1024L;
    }

    public boolean shouldShowDebugHud() {
        return this.showDebugHud && !this.client.options.hudHidden;
    }

    public boolean shouldShowRenderingChart() {
        return this.shouldShowDebugHud() && this.renderingChartVisible;
    }

    public boolean shouldShowPacketSizeAndPingCharts() {
        return this.shouldShowDebugHud() && this.packetSizeAndPingChartsVisible;
    }

    public boolean shouldRenderTickCharts() {
        return this.shouldShowDebugHud() && this.renderingAndTickChartsVisible;
    }

    public void toggleDebugHud() {
        this.showDebugHud = !this.showDebugHud;
    }

    public void togglePacketSizeAndPingCharts() {
        boolean bl = this.packetSizeAndPingChartsVisible = !this.showDebugHud || !this.packetSizeAndPingChartsVisible;
        if (this.packetSizeAndPingChartsVisible) {
            this.showDebugHud = true;
            this.renderingAndTickChartsVisible = false;
        }
    }

    public void toggleRenderingAndTickCharts() {
        boolean bl = this.renderingAndTickChartsVisible = !this.showDebugHud || !this.renderingAndTickChartsVisible;
        if (this.renderingAndTickChartsVisible) {
            this.showDebugHud = true;
            this.packetSizeAndPingChartsVisible = false;
        }
    }

    public void toggleRenderingChart() {
        boolean bl = this.renderingChartVisible = !this.showDebugHud || !this.renderingChartVisible;
        if (this.renderingChartVisible) {
            this.showDebugHud = true;
        }
    }

    public void pushToFrameLog(long value) {
        this.frameNanosLog.push(value);
    }

    public MultiValueDebugSampleLogImpl getTickNanosLog() {
        return this.tickNanosLog;
    }

    public MultiValueDebugSampleLogImpl getPingLog() {
        return this.pingLog;
    }

    public MultiValueDebugSampleLogImpl getPacketSizeLog() {
        return this.packetSizeLog;
    }

    public void set(long[] values, DebugSampleType type) {
        MultiValueDebugSampleLogImpl lv = this.receivedDebugSamples.get((Object)type);
        if (lv != null) {
            lv.set(values);
        }
    }

    public void clear() {
        this.showDebugHud = false;
        this.tickNanosLog.clear();
        this.pingLog.clear();
        this.packetSizeLog.clear();
    }

    @Environment(value=EnvType.CLIENT)
    static class AllocationRateCalculator {
        private static final int INTERVAL = 500;
        private static final List<GarbageCollectorMXBean> GARBAGE_COLLECTORS = ManagementFactory.getGarbageCollectorMXBeans();
        private long lastCalculated = 0L;
        private long allocatedBytes = -1L;
        private long collectionCount = -1L;
        private long allocationRate = 0L;

        AllocationRateCalculator() {
        }

        long get(long allocatedBytes) {
            long m = System.currentTimeMillis();
            if (m - this.lastCalculated < 500L) {
                return this.allocationRate;
            }
            long n = AllocationRateCalculator.getCollectionCount();
            if (this.lastCalculated != 0L && n == this.collectionCount) {
                double d = (double)TimeUnit.SECONDS.toMillis(1L) / (double)(m - this.lastCalculated);
                long o = allocatedBytes - this.allocatedBytes;
                this.allocationRate = Math.round((double)o * d);
            }
            this.lastCalculated = m;
            this.allocatedBytes = allocatedBytes;
            this.collectionCount = n;
            return this.allocationRate;
        }

        private static long getCollectionCount() {
            long l = 0L;
            for (GarbageCollectorMXBean garbageCollectorMXBean : GARBAGE_COLLECTORS) {
                l += garbageCollectorMXBean.getCollectionCount();
            }
            return l;
        }
    }
}

