/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 * 
 * Could not load the following classes:
 *  net.fabricmc.api.EnvType
 *  net.fabricmc.api.Environment
 *  org.jetbrains.annotations.Nullable
 */
package net.minecraft.client.render;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.google.gson.JsonSyntaxException;
import com.mojang.blaze3d.platform.GlConst;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.logging.LogUtils;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;
import java.util.function.Supplier;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.BrushableBlock;
import net.minecraft.block.CampfireBlock;
import net.minecraft.block.ComposterBlock;
import net.minecraft.block.MultifaceGrowthBlock;
import net.minecraft.block.PointedDripstoneBlock;
import net.minecraft.block.SculkShriekerBlock;
import net.minecraft.block.ShapeContext;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.VaultBlockEntity;
import net.minecraft.block.enums.CameraSubmersionType;
import net.minecraft.block.jukebox.JukeboxSong;
import net.minecraft.block.spawner.TrialSpawnerLogic;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gl.Framebuffer;
import net.minecraft.client.gl.GlUniform;
import net.minecraft.client.gl.PostEffectProcessor;
import net.minecraft.client.gl.ShaderProgram;
import net.minecraft.client.gl.VertexBuffer;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.option.CloudRenderMode;
import net.minecraft.client.option.GraphicsMode;
import net.minecraft.client.option.ParticlesMode;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.render.BackgroundRenderer;
import net.minecraft.client.render.BufferBuilder;
import net.minecraft.client.render.BufferBuilderStorage;
import net.minecraft.client.render.BufferRenderer;
import net.minecraft.client.render.BuiltBuffer;
import net.minecraft.client.render.BuiltChunkStorage;
import net.minecraft.client.render.Camera;
import net.minecraft.client.render.ChunkBuilderMode;
import net.minecraft.client.render.ChunkRenderingDataPreparer;
import net.minecraft.client.render.DiffuseLighting;
import net.minecraft.client.render.DimensionEffects;
import net.minecraft.client.render.FpsSmoother;
import net.minecraft.client.render.Frustum;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.client.render.LightmapTextureManager;
import net.minecraft.client.render.OutlineVertexConsumerProvider;
import net.minecraft.client.render.OverlayVertexConsumer;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.RenderLayers;
import net.minecraft.client.render.RenderPhase;
import net.minecraft.client.render.RenderTickCounter;
import net.minecraft.client.render.Tessellator;
import net.minecraft.client.render.TexturedRenderLayers;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.VertexConsumers;
import net.minecraft.client.render.VertexFormat;
import net.minecraft.client.render.VertexFormats;
import net.minecraft.client.render.block.entity.BlockEntityRenderDispatcher;
import net.minecraft.client.render.chunk.ChunkBuilder;
import net.minecraft.client.render.chunk.ChunkRendererRegionBuilder;
import net.minecraft.client.render.entity.EntityRenderDispatcher;
import net.minecraft.client.render.model.ModelLoader;
import net.minecraft.client.sound.PositionedSoundInstance;
import net.minecraft.client.sound.SoundInstance;
import net.minecraft.client.texture.SpriteAtlasTexture;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.player.BlockBreakingInfo;
import net.minecraft.fluid.FluidState;
import net.minecraft.item.BoneMealItem;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtOps;
import net.minecraft.particle.ItemStackParticleEffect;
import net.minecraft.particle.ParticleEffect;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.particle.ParticleUtil;
import net.minecraft.particle.SculkChargeParticleEffect;
import net.minecraft.particle.ShriekParticleEffect;
import net.minecraft.particle.SimpleParticleType;
import net.minecraft.registry.Registries;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.registry.tag.FluidTags;
import net.minecraft.resource.ResourceManager;
import net.minecraft.resource.SynchronousResourceReloader;
import net.minecraft.sound.BlockSoundGroup;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvent;
import net.minecraft.sound.SoundEvents;
import net.minecraft.state.property.Properties;
import net.minecraft.text.Text;
import net.minecraft.util.Colors;
import net.minecraft.util.Identifier;
import net.minecraft.util.Util;
import net.minecraft.util.crash.CrashException;
import net.minecraft.util.crash.CrashReport;
import net.minecraft.util.crash.CrashReportSection;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.ChunkSectionPos;
import net.minecraft.util.math.ColorHelper;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RotationAxis;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.Vec3i;
import net.minecraft.util.math.intprovider.UniformIntProvider;
import net.minecraft.util.math.random.Random;
import net.minecraft.util.profiler.Profiler;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.util.shape.VoxelShapes;
import net.minecraft.world.BlockRenderView;
import net.minecraft.world.BlockView;
import net.minecraft.world.HeightLimitView;
import net.minecraft.world.Heightmap;
import net.minecraft.world.LightType;
import net.minecraft.world.World;
import net.minecraft.world.WorldEvents;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.border.WorldBorder;
import net.minecraft.world.chunk.light.LightingProvider;
import net.minecraft.world.tick.TickManager;
import org.jetbrains.annotations.Nullable;
import org.joml.Matrix4f;
import org.joml.Matrix4fStack;
import org.joml.Quaternionf;
import org.joml.Vector3d;
import org.joml.Vector3f;
import org.joml.Vector4f;
import org.slf4j.Logger;

@Environment(value=EnvType.CLIENT)
public class WorldRenderer
implements SynchronousResourceReloader,
AutoCloseable {
    private static final Logger LOGGER = LogUtils.getLogger();
    public static final int field_32759 = 16;
    public static final int field_34812 = 8;
    private static final float field_32762 = 512.0f;
    private static final int field_32763 = 32;
    private static final int field_32764 = 10;
    private static final int field_32765 = 21;
    private static final int field_32766 = 15;
    private static final Identifier MOON_PHASES = Identifier.ofVanilla("textures/environment/moon_phases.png");
    private static final Identifier SUN = Identifier.ofVanilla("textures/environment/sun.png");
    protected static final Identifier CLOUDS = Identifier.ofVanilla("textures/environment/clouds.png");
    private static final Identifier END_SKY = Identifier.ofVanilla("textures/environment/end_sky.png");
    private static final Identifier FORCEFIELD = Identifier.ofVanilla("textures/misc/forcefield.png");
    private static final Identifier RAIN = Identifier.ofVanilla("textures/environment/rain.png");
    private static final Identifier SNOW = Identifier.ofVanilla("textures/environment/snow.png");
    public static final Direction[] DIRECTIONS = Direction.values();
    private final MinecraftClient client;
    private final EntityRenderDispatcher entityRenderDispatcher;
    private final BlockEntityRenderDispatcher blockEntityRenderDispatcher;
    private final BufferBuilderStorage bufferBuilders;
    @Nullable
    private ClientWorld world;
    private final ChunkRenderingDataPreparer chunkRenderingDataPreparer = new ChunkRenderingDataPreparer();
    private final ObjectArrayList<ChunkBuilder.BuiltChunk> builtChunks = new ObjectArrayList(10000);
    private final Set<BlockEntity> noCullingBlockEntities = Sets.newHashSet();
    @Nullable
    private BuiltChunkStorage chunks;
    @Nullable
    private VertexBuffer starsBuffer;
    @Nullable
    private VertexBuffer lightSkyBuffer;
    @Nullable
    private VertexBuffer darkSkyBuffer;
    private boolean cloudsDirty = true;
    @Nullable
    private VertexBuffer cloudsBuffer;
    private final FpsSmoother chunkUpdateSmoother = new FpsSmoother(100);
    private int ticks;
    private final Int2ObjectMap<BlockBreakingInfo> blockBreakingInfos = new Int2ObjectOpenHashMap<BlockBreakingInfo>();
    private final Long2ObjectMap<SortedSet<BlockBreakingInfo>> blockBreakingProgressions = new Long2ObjectOpenHashMap<SortedSet<BlockBreakingInfo>>();
    private final Map<BlockPos, SoundInstance> playingSongs = Maps.newHashMap();
    @Nullable
    private Framebuffer entityOutlinesFramebuffer;
    @Nullable
    private PostEffectProcessor entityOutlinePostProcessor;
    @Nullable
    private Framebuffer translucentFramebuffer;
    @Nullable
    private Framebuffer entityFramebuffer;
    @Nullable
    private Framebuffer particlesFramebuffer;
    @Nullable
    private Framebuffer weatherFramebuffer;
    @Nullable
    private Framebuffer cloudsFramebuffer;
    @Nullable
    private PostEffectProcessor transparencyPostProcessor;
    private int cameraChunkX = Integer.MIN_VALUE;
    private int cameraChunkY = Integer.MIN_VALUE;
    private int cameraChunkZ = Integer.MIN_VALUE;
    private double lastCameraX = Double.MIN_VALUE;
    private double lastCameraY = Double.MIN_VALUE;
    private double lastCameraZ = Double.MIN_VALUE;
    private double lastCameraPitch = Double.MIN_VALUE;
    private double lastCameraYaw = Double.MIN_VALUE;
    private int lastCloudsBlockX = Integer.MIN_VALUE;
    private int lastCloudsBlockY = Integer.MIN_VALUE;
    private int lastCloudsBlockZ = Integer.MIN_VALUE;
    private Vec3d lastCloudsColor = Vec3d.ZERO;
    @Nullable
    private CloudRenderMode lastCloudRenderMode;
    @Nullable
    private ChunkBuilder chunkBuilder;
    private int viewDistance = -1;
    private int regularEntityCount;
    private int blockEntityCount;
    private Frustum frustum;
    private boolean shouldCaptureFrustum;
    @Nullable
    private Frustum capturedFrustum;
    private final Vector4f[] capturedFrustumOrientation = new Vector4f[8];
    private final Vector3d capturedFrustumPosition = new Vector3d(0.0, 0.0, 0.0);
    private double lastTranslucentSortX;
    private double lastTranslucentSortY;
    private double lastTranslucentSortZ;
    private int rainSoundCounter;
    private final float[] NORMAL_LINE_DX = new float[1024];
    private final float[] NORMAL_LINE_DZ = new float[1024];

    public WorldRenderer(MinecraftClient client, EntityRenderDispatcher entityRenderDispatcher, BlockEntityRenderDispatcher blockEntityRenderDispatcher, BufferBuilderStorage bufferBuilders) {
        this.client = client;
        this.entityRenderDispatcher = entityRenderDispatcher;
        this.blockEntityRenderDispatcher = blockEntityRenderDispatcher;
        this.bufferBuilders = bufferBuilders;
        for (int i = 0; i < 32; ++i) {
            for (int j = 0; j < 32; ++j) {
                float f = j - 16;
                float g = i - 16;
                float h = MathHelper.sqrt(f * f + g * g);
                this.NORMAL_LINE_DX[i << 5 | j] = -g / h;
                this.NORMAL_LINE_DZ[i << 5 | j] = f / h;
            }
        }
        this.renderStars();
        this.renderLightSky();
        this.renderDarkSky();
    }

    private void renderWeather(LightmapTextureManager manager, float tickDelta, double cameraX, double cameraY, double cameraZ) {
        float h = this.client.world.getRainGradient(tickDelta);
        if (h <= 0.0f) {
            return;
        }
        manager.enable();
        ClientWorld lv = this.client.world;
        int i = MathHelper.floor(cameraX);
        int j = MathHelper.floor(cameraY);
        int k = MathHelper.floor(cameraZ);
        Tessellator lv2 = Tessellator.getInstance();
        BufferBuilder lv3 = null;
        RenderSystem.disableCull();
        RenderSystem.enableBlend();
        RenderSystem.enableDepthTest();
        int l = 5;
        if (MinecraftClient.isFancyGraphicsOrBetter()) {
            l = 10;
        }
        RenderSystem.depthMask(MinecraftClient.isFabulousGraphicsOrBetter());
        int m = -1;
        float n = (float)this.ticks + tickDelta;
        RenderSystem.setShader(GameRenderer::getParticleProgram);
        BlockPos.Mutable lv4 = new BlockPos.Mutable();
        for (int o = k - l; o <= k + l; ++o) {
            for (int p = i - l; p <= i + l; ++p) {
                int ag;
                double ac;
                float z;
                int w;
                int q = (o - k + 16) * 32 + p - i + 16;
                double r = (double)this.NORMAL_LINE_DX[q] * 0.5;
                double s = (double)this.NORMAL_LINE_DZ[q] * 0.5;
                lv4.set((double)p, cameraY, (double)o);
                Biome lv5 = lv.getBiome(lv4).value();
                if (!lv5.hasPrecipitation()) continue;
                int t = lv.getTopY(Heightmap.Type.MOTION_BLOCKING, p, o);
                int u = j - l;
                int v = j + l;
                if (u < t) {
                    u = t;
                }
                if (v < t) {
                    v = t;
                }
                if ((w = t) < j) {
                    w = j;
                }
                if (u == v) continue;
                Random lv6 = Random.create(p * p * 3121 + p * 45238971 ^ o * o * 418711 + o * 13761);
                lv4.set(p, u, o);
                Biome.Precipitation lv7 = lv5.getPrecipitation(lv4);
                if (lv7 == Biome.Precipitation.RAIN) {
                    if (m != 0) {
                        if (m >= 0) {
                            BufferRenderer.drawWithGlobalProgram(lv3.end());
                        }
                        m = 0;
                        RenderSystem.setShaderTexture(0, RAIN);
                        lv3 = lv2.begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_TEXTURE_COLOR_LIGHT);
                    }
                    int x = this.ticks & 0x1FFFF;
                    int y = p * p * 3121 + p * 45238971 + o * o * 418711 + o * 13761 & 0xFF;
                    z = 3.0f + lv6.nextFloat();
                    float aa = -((float)(x + y) + tickDelta) / 32.0f * z;
                    float ab = aa % 32.0f;
                    ac = (double)p + 0.5 - cameraX;
                    double ad = (double)o + 0.5 - cameraZ;
                    float ae = (float)Math.sqrt(ac * ac + ad * ad) / (float)l;
                    float af = ((1.0f - ae * ae) * 0.5f + 0.5f) * h;
                    lv4.set(p, w, o);
                    ag = WorldRenderer.getLightmapCoordinates(lv, lv4);
                    lv3.vertex((float)((double)p - cameraX - r + 0.5), (float)((double)v - cameraY), (float)((double)o - cameraZ - s + 0.5)).texture(0.0f, (float)u * 0.25f + ab).color(1.0f, 1.0f, 1.0f, af).light(ag);
                    lv3.vertex((float)((double)p - cameraX + r + 0.5), (float)((double)v - cameraY), (float)((double)o - cameraZ + s + 0.5)).texture(1.0f, (float)u * 0.25f + ab).color(1.0f, 1.0f, 1.0f, af).light(ag);
                    lv3.vertex((float)((double)p - cameraX + r + 0.5), (float)((double)u - cameraY), (float)((double)o - cameraZ + s + 0.5)).texture(1.0f, (float)v * 0.25f + ab).color(1.0f, 1.0f, 1.0f, af).light(ag);
                    lv3.vertex((float)((double)p - cameraX - r + 0.5), (float)((double)u - cameraY), (float)((double)o - cameraZ - s + 0.5)).texture(0.0f, (float)v * 0.25f + ab).color(1.0f, 1.0f, 1.0f, af).light(ag);
                    continue;
                }
                if (lv7 != Biome.Precipitation.SNOW) continue;
                if (m != 1) {
                    if (m >= 0) {
                        BufferRenderer.drawWithGlobalProgram(lv3.end());
                    }
                    m = 1;
                    RenderSystem.setShaderTexture(0, SNOW);
                    lv3 = lv2.begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_TEXTURE_COLOR_LIGHT);
                }
                float ah = -((float)(this.ticks & 0x1FF) + tickDelta) / 512.0f;
                float ai = (float)(lv6.nextDouble() + (double)n * 0.01 * (double)((float)lv6.nextGaussian()));
                z = (float)(lv6.nextDouble() + (double)(n * (float)lv6.nextGaussian()) * 0.001);
                double aj = (double)p + 0.5 - cameraX;
                ac = (double)o + 0.5 - cameraZ;
                float ak = (float)Math.sqrt(aj * aj + ac * ac) / (float)l;
                float al = ((1.0f - ak * ak) * 0.3f + 0.5f) * h;
                lv4.set(p, w, o);
                int am = WorldRenderer.getLightmapCoordinates(lv, lv4);
                int an = am >> 16 & (LightmapTextureManager.MAX_BLOCK_LIGHT_COORDINATE | 0xFF0F);
                ag = am & (LightmapTextureManager.MAX_BLOCK_LIGHT_COORDINATE | 0xFF0F);
                int ao = (an * 3 + LightmapTextureManager.MAX_BLOCK_LIGHT_COORDINATE) / 4;
                int ap = (ag * 3 + LightmapTextureManager.MAX_BLOCK_LIGHT_COORDINATE) / 4;
                lv3.vertex((float)((double)p - cameraX - r + 0.5), (float)((double)v - cameraY), (float)((double)o - cameraZ - s + 0.5)).texture(0.0f + ai, (float)u * 0.25f + ah + z).color(1.0f, 1.0f, 1.0f, al).light(ap, ao);
                lv3.vertex((float)((double)p - cameraX + r + 0.5), (float)((double)v - cameraY), (float)((double)o - cameraZ + s + 0.5)).texture(1.0f + ai, (float)u * 0.25f + ah + z).color(1.0f, 1.0f, 1.0f, al).light(ap, ao);
                lv3.vertex((float)((double)p - cameraX + r + 0.5), (float)((double)u - cameraY), (float)((double)o - cameraZ + s + 0.5)).texture(1.0f + ai, (float)v * 0.25f + ah + z).color(1.0f, 1.0f, 1.0f, al).light(ap, ao);
                lv3.vertex((float)((double)p - cameraX - r + 0.5), (float)((double)u - cameraY), (float)((double)o - cameraZ - s + 0.5)).texture(0.0f + ai, (float)v * 0.25f + ah + z).color(1.0f, 1.0f, 1.0f, al).light(ap, ao);
            }
        }
        if (m >= 0) {
            BufferRenderer.drawWithGlobalProgram(lv3.end());
        }
        RenderSystem.enableCull();
        RenderSystem.disableBlend();
        manager.disable();
    }

    public void tickRainSplashing(Camera camera) {
        float f = this.client.world.getRainGradient(1.0f) / (MinecraftClient.isFancyGraphicsOrBetter() ? 1.0f : 2.0f);
        if (f <= 0.0f) {
            return;
        }
        Random lv = Random.create((long)this.ticks * 312987231L);
        ClientWorld lv2 = this.client.world;
        BlockPos lv3 = BlockPos.ofFloored(camera.getPos());
        Vec3i lv4 = null;
        int i = (int)(100.0f * f * f) / (this.client.options.getParticles().getValue() == ParticlesMode.DECREASED ? 2 : 1);
        for (int j = 0; j < i; ++j) {
            Biome lv6;
            int l;
            int k = lv.nextInt(21) - 10;
            BlockPos lv5 = lv2.getTopPosition(Heightmap.Type.MOTION_BLOCKING, lv3.add(k, 0, l = lv.nextInt(21) - 10));
            if (lv5.getY() <= lv2.getBottomY() || lv5.getY() > lv3.getY() + 10 || lv5.getY() < lv3.getY() - 10 || (lv6 = lv2.getBiome(lv5).value()).getPrecipitation(lv5) != Biome.Precipitation.RAIN) continue;
            lv4 = lv5.down();
            if (this.client.options.getParticles().getValue() == ParticlesMode.MINIMAL) break;
            double d = lv.nextDouble();
            double e = lv.nextDouble();
            BlockState lv7 = lv2.getBlockState((BlockPos)lv4);
            FluidState lv8 = lv2.getFluidState((BlockPos)lv4);
            VoxelShape lv9 = lv7.getCollisionShape(lv2, (BlockPos)lv4);
            double g = lv9.getEndingCoord(Direction.Axis.Y, d, e);
            double h = lv8.getHeight(lv2, (BlockPos)lv4);
            double m = Math.max(g, h);
            SimpleParticleType lv10 = lv8.isIn(FluidTags.LAVA) || lv7.isOf(Blocks.MAGMA_BLOCK) || CampfireBlock.isLitCampfire(lv7) ? ParticleTypes.SMOKE : ParticleTypes.RAIN;
            this.client.world.addParticle(lv10, (double)lv4.getX() + d, (double)lv4.getY() + m, (double)lv4.getZ() + e, 0.0, 0.0, 0.0);
        }
        if (lv4 != null && lv.nextInt(3) < this.rainSoundCounter++) {
            this.rainSoundCounter = 0;
            if (lv4.getY() > lv3.getY() + 1 && lv2.getTopPosition(Heightmap.Type.MOTION_BLOCKING, lv3).getY() > MathHelper.floor(lv3.getY())) {
                this.client.world.playSoundAtBlockCenter((BlockPos)lv4, SoundEvents.WEATHER_RAIN_ABOVE, SoundCategory.WEATHER, 0.1f, 0.5f, false);
            } else {
                this.client.world.playSoundAtBlockCenter((BlockPos)lv4, SoundEvents.WEATHER_RAIN, SoundCategory.WEATHER, 0.2f, 1.0f, false);
            }
        }
    }

    @Override
    public void close() {
        if (this.entityOutlinePostProcessor != null) {
            this.entityOutlinePostProcessor.close();
        }
        if (this.transparencyPostProcessor != null) {
            this.transparencyPostProcessor.close();
        }
    }

    @Override
    public void reload(ResourceManager manager) {
        this.loadEntityOutlinePostProcessor();
        if (MinecraftClient.isFabulousGraphicsOrBetter()) {
            this.loadTransparencyPostProcessor();
        }
    }

    public void loadEntityOutlinePostProcessor() {
        if (this.entityOutlinePostProcessor != null) {
            this.entityOutlinePostProcessor.close();
        }
        Identifier lv = Identifier.ofVanilla("shaders/post/entity_outline.json");
        try {
            this.entityOutlinePostProcessor = new PostEffectProcessor(this.client.getTextureManager(), this.client.getResourceManager(), this.client.getFramebuffer(), lv);
            this.entityOutlinePostProcessor.setupDimensions(this.client.getWindow().getFramebufferWidth(), this.client.getWindow().getFramebufferHeight());
            this.entityOutlinesFramebuffer = this.entityOutlinePostProcessor.getSecondaryTarget("final");
        } catch (IOException iOException) {
            LOGGER.warn("Failed to load shader: {}", (Object)lv, (Object)iOException);
            this.entityOutlinePostProcessor = null;
            this.entityOutlinesFramebuffer = null;
        } catch (JsonSyntaxException jsonSyntaxException) {
            LOGGER.warn("Failed to parse shader: {}", (Object)lv, (Object)jsonSyntaxException);
            this.entityOutlinePostProcessor = null;
            this.entityOutlinesFramebuffer = null;
        }
    }

    private void loadTransparencyPostProcessor() {
        this.resetTransparencyPostProcessor();
        Identifier lv = Identifier.ofVanilla("shaders/post/transparency.json");
        try {
            PostEffectProcessor lv2 = new PostEffectProcessor(this.client.getTextureManager(), this.client.getResourceManager(), this.client.getFramebuffer(), lv);
            lv2.setupDimensions(this.client.getWindow().getFramebufferWidth(), this.client.getWindow().getFramebufferHeight());
            Framebuffer lv3 = lv2.getSecondaryTarget("translucent");
            Framebuffer lv4 = lv2.getSecondaryTarget("itemEntity");
            Framebuffer lv5 = lv2.getSecondaryTarget("particles");
            Framebuffer lv6 = lv2.getSecondaryTarget("weather");
            Framebuffer lv7 = lv2.getSecondaryTarget("clouds");
            this.transparencyPostProcessor = lv2;
            this.translucentFramebuffer = lv3;
            this.entityFramebuffer = lv4;
            this.particlesFramebuffer = lv5;
            this.weatherFramebuffer = lv6;
            this.cloudsFramebuffer = lv7;
        } catch (Exception exception) {
            String string = exception instanceof JsonSyntaxException ? "parse" : "load";
            String string2 = "Failed to " + string + " shader: " + String.valueOf(lv);
            ProgramInitException lv8 = new ProgramInitException(string2, exception);
            if (this.client.getResourcePackManager().getEnabledIds().size() > 1) {
                Text lv9 = this.client.getResourceManager().streamResourcePacks().findFirst().map(arg -> Text.literal(arg.getId())).orElse(null);
                this.client.options.getGraphicsMode().setValue(GraphicsMode.FANCY);
                this.client.onResourceReloadFailure(lv8, lv9, null);
            }
            this.client.options.getGraphicsMode().setValue(GraphicsMode.FANCY);
            this.client.options.write();
            LOGGER.error(LogUtils.FATAL_MARKER, string2, lv8);
            this.client.printCrashReport(new CrashReport(string2, lv8));
        }
    }

    private void resetTransparencyPostProcessor() {
        if (this.transparencyPostProcessor != null) {
            this.transparencyPostProcessor.close();
            this.translucentFramebuffer.delete();
            this.entityFramebuffer.delete();
            this.particlesFramebuffer.delete();
            this.weatherFramebuffer.delete();
            this.cloudsFramebuffer.delete();
            this.transparencyPostProcessor = null;
            this.translucentFramebuffer = null;
            this.entityFramebuffer = null;
            this.particlesFramebuffer = null;
            this.weatherFramebuffer = null;
            this.cloudsFramebuffer = null;
        }
    }

    public void drawEntityOutlinesFramebuffer() {
        if (this.canDrawEntityOutlines()) {
            RenderSystem.enableBlend();
            RenderSystem.blendFuncSeparate(GlStateManager.SrcFactor.SRC_ALPHA, GlStateManager.DstFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SrcFactor.ZERO, GlStateManager.DstFactor.ONE);
            this.entityOutlinesFramebuffer.draw(this.client.getWindow().getFramebufferWidth(), this.client.getWindow().getFramebufferHeight(), false);
            RenderSystem.disableBlend();
            RenderSystem.defaultBlendFunc();
        }
    }

    protected boolean canDrawEntityOutlines() {
        return !this.client.gameRenderer.isRenderingPanorama() && this.entityOutlinesFramebuffer != null && this.entityOutlinePostProcessor != null && this.client.player != null;
    }

    private void renderDarkSky() {
        if (this.darkSkyBuffer != null) {
            this.darkSkyBuffer.close();
        }
        this.darkSkyBuffer = new VertexBuffer(VertexBuffer.Usage.STATIC);
        this.darkSkyBuffer.bind();
        this.darkSkyBuffer.upload(WorldRenderer.buildSkyBuffer(Tessellator.getInstance(), -16.0f));
        VertexBuffer.unbind();
    }

    private void renderLightSky() {
        if (this.lightSkyBuffer != null) {
            this.lightSkyBuffer.close();
        }
        this.lightSkyBuffer = new VertexBuffer(VertexBuffer.Usage.STATIC);
        this.lightSkyBuffer.bind();
        this.lightSkyBuffer.upload(WorldRenderer.buildSkyBuffer(Tessellator.getInstance(), 16.0f));
        VertexBuffer.unbind();
    }

    private static BuiltBuffer buildSkyBuffer(Tessellator arg, float f) {
        float g = Math.signum(f) * 512.0f;
        float h = 512.0f;
        BufferBuilder lv = arg.begin(VertexFormat.DrawMode.TRIANGLE_FAN, VertexFormats.POSITION);
        lv.vertex(0.0f, f, 0.0f);
        for (int i = -180; i <= 180; i += 45) {
            lv.vertex(g * MathHelper.cos((float)i * ((float)Math.PI / 180)), f, 512.0f * MathHelper.sin((float)i * ((float)Math.PI / 180)));
        }
        return lv.end();
    }

    private void renderStars() {
        if (this.starsBuffer != null) {
            this.starsBuffer.close();
        }
        this.starsBuffer = new VertexBuffer(VertexBuffer.Usage.STATIC);
        this.starsBuffer.bind();
        this.starsBuffer.upload(this.buildStarsBuffer(Tessellator.getInstance()));
        VertexBuffer.unbind();
    }

    private BuiltBuffer buildStarsBuffer(Tessellator arg) {
        Random lv = Random.create(10842L);
        int i = 1500;
        float f = 100.0f;
        BufferBuilder lv2 = arg.begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION);
        for (int j = 0; j < 1500; ++j) {
            float g = lv.nextFloat() * 2.0f - 1.0f;
            float h = lv.nextFloat() * 2.0f - 1.0f;
            float k = lv.nextFloat() * 2.0f - 1.0f;
            float l = 0.15f + lv.nextFloat() * 0.1f;
            float m = MathHelper.magnitude(g, h, k);
            if (m <= 0.010000001f || m >= 1.0f) continue;
            Vector3f vector3f = new Vector3f(g, h, k).normalize(100.0f);
            float n = (float)(lv.nextDouble() * 3.1415927410125732 * 2.0);
            Quaternionf quaternionf = new Quaternionf().rotateTo(new Vector3f(0.0f, 0.0f, -1.0f), vector3f).rotateZ(n);
            lv2.vertex(vector3f.add(new Vector3f(l, -l, 0.0f).rotate(quaternionf)));
            lv2.vertex(vector3f.add(new Vector3f(l, l, 0.0f).rotate(quaternionf)));
            lv2.vertex(vector3f.add(new Vector3f(-l, l, 0.0f).rotate(quaternionf)));
            lv2.vertex(vector3f.add(new Vector3f(-l, -l, 0.0f).rotate(quaternionf)));
        }
        return lv2.end();
    }

    public void setWorld(@Nullable ClientWorld world) {
        this.cameraChunkX = Integer.MIN_VALUE;
        this.cameraChunkY = Integer.MIN_VALUE;
        this.cameraChunkZ = Integer.MIN_VALUE;
        this.entityRenderDispatcher.setWorld(world);
        this.world = world;
        if (world != null) {
            this.reload();
        } else {
            if (this.chunks != null) {
                this.chunks.clear();
                this.chunks = null;
            }
            if (this.chunkBuilder != null) {
                this.chunkBuilder.stop();
            }
            this.chunkBuilder = null;
            this.noCullingBlockEntities.clear();
            this.chunkRenderingDataPreparer.method_52826(null);
            this.builtChunks.clear();
        }
    }

    public void reloadTransparencyPostProcessor() {
        if (MinecraftClient.isFabulousGraphicsOrBetter()) {
            this.loadTransparencyPostProcessor();
        } else {
            this.resetTransparencyPostProcessor();
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public void reload() {
        if (this.world == null) {
            return;
        }
        this.reloadTransparencyPostProcessor();
        this.world.reloadColor();
        if (this.chunkBuilder == null) {
            this.chunkBuilder = new ChunkBuilder(this.world, this, Util.getMainWorkerExecutor(), this.bufferBuilders, this.client.getBlockRenderManager(), this.client.getBlockEntityRenderDispatcher());
        } else {
            this.chunkBuilder.setWorld(this.world);
        }
        this.cloudsDirty = true;
        RenderLayers.setFancyGraphicsOrBetter(MinecraftClient.isFancyGraphicsOrBetter());
        this.viewDistance = this.client.options.getClampedViewDistance();
        if (this.chunks != null) {
            this.chunks.clear();
        }
        this.chunkBuilder.reset();
        Set<BlockEntity> set = this.noCullingBlockEntities;
        synchronized (set) {
            this.noCullingBlockEntities.clear();
        }
        this.chunks = new BuiltChunkStorage(this.chunkBuilder, this.world, this.client.options.getClampedViewDistance(), this);
        this.chunkRenderingDataPreparer.method_52826(this.chunks);
        this.builtChunks.clear();
        Entity lv = this.client.getCameraEntity();
        if (lv != null) {
            this.chunks.updateCameraPosition(lv.getX(), lv.getZ());
        }
    }

    public void onResized(int width, int height) {
        this.scheduleTerrainUpdate();
        if (this.entityOutlinePostProcessor != null) {
            this.entityOutlinePostProcessor.setupDimensions(width, height);
        }
        if (this.transparencyPostProcessor != null) {
            this.transparencyPostProcessor.setupDimensions(width, height);
        }
    }

    public String getChunksDebugString() {
        int i = this.chunks.chunks.length;
        int j = this.getCompletedChunkCount();
        return String.format(Locale.ROOT, "C: %d/%d %sD: %d, %s", j, i, this.client.chunkCullingEnabled ? "(s) " : "", this.viewDistance, this.chunkBuilder == null ? "null" : this.chunkBuilder.getDebugString());
    }

    public ChunkBuilder getChunkBuilder() {
        return this.chunkBuilder;
    }

    public double getChunkCount() {
        return this.chunks.chunks.length;
    }

    public double getViewDistance() {
        return this.viewDistance;
    }

    public int getCompletedChunkCount() {
        int i = 0;
        for (ChunkBuilder.BuiltChunk lv : this.builtChunks) {
            if (lv.getData().isEmpty()) continue;
            ++i;
        }
        return i;
    }

    public String getEntitiesDebugString() {
        return "E: " + this.regularEntityCount + "/" + this.world.getRegularEntityCount() + ", B: " + this.blockEntityCount + ", SD: " + this.world.getSimulationDistance();
    }

    private void setupTerrain(Camera camera, Frustum frustum, boolean hasForcedFrustum, boolean spectator) {
        Vec3d lv = camera.getPos();
        if (this.client.options.getClampedViewDistance() != this.viewDistance) {
            this.reload();
        }
        this.world.getProfiler().push("camera");
        double d = this.client.player.getX();
        double e = this.client.player.getY();
        double f = this.client.player.getZ();
        int i = ChunkSectionPos.getSectionCoord(d);
        int j = ChunkSectionPos.getSectionCoord(e);
        int k = ChunkSectionPos.getSectionCoord(f);
        if (this.cameraChunkX != i || this.cameraChunkY != j || this.cameraChunkZ != k) {
            this.cameraChunkX = i;
            this.cameraChunkY = j;
            this.cameraChunkZ = k;
            this.chunks.updateCameraPosition(d, f);
        }
        this.chunkBuilder.setCameraPosition(lv);
        this.world.getProfiler().swap("cull");
        this.client.getProfiler().swap("culling");
        BlockPos lv2 = camera.getBlockPos();
        double g = Math.floor(lv.x / 8.0);
        double h = Math.floor(lv.y / 8.0);
        double l = Math.floor(lv.z / 8.0);
        if (g != this.lastCameraX || h != this.lastCameraY || l != this.lastCameraZ) {
            this.chunkRenderingDataPreparer.method_52817();
        }
        this.lastCameraX = g;
        this.lastCameraY = h;
        this.lastCameraZ = l;
        this.client.getProfiler().swap("update");
        if (!hasForcedFrustum) {
            boolean bl3 = this.client.chunkCullingEnabled;
            if (spectator && this.world.getBlockState(lv2).isOpaqueFullCube(this.world, lv2)) {
                bl3 = false;
            }
            Entity.setRenderDistanceMultiplier(MathHelper.clamp((double)this.client.options.getClampedViewDistance() / 8.0, 1.0, 2.5) * this.client.options.getEntityDistanceScaling().getValue());
            this.client.getProfiler().push("section_occlusion_graph");
            this.chunkRenderingDataPreparer.method_52834(bl3, camera, frustum, this.builtChunks);
            this.client.getProfiler().pop();
            double m = Math.floor(camera.getPitch() / 2.0f);
            double n = Math.floor(camera.getYaw() / 2.0f);
            if (this.chunkRenderingDataPreparer.method_52836() || m != this.lastCameraPitch || n != this.lastCameraYaw) {
                this.applyFrustum(WorldRenderer.method_52816(frustum));
                this.lastCameraPitch = m;
                this.lastCameraYaw = n;
            }
        }
        this.client.getProfiler().pop();
    }

    public static Frustum method_52816(Frustum arg) {
        return new Frustum(arg).coverBoxAroundSetPosition(8);
    }

    private void applyFrustum(Frustum frustum) {
        if (!MinecraftClient.getInstance().isOnThread()) {
            throw new IllegalStateException("applyFrustum called from wrong thread: " + Thread.currentThread().getName());
        }
        this.client.getProfiler().push("apply_frustum");
        this.builtChunks.clear();
        this.chunkRenderingDataPreparer.method_52828(frustum, this.builtChunks);
        this.client.getProfiler().pop();
    }

    public void addBuiltChunk(ChunkBuilder.BuiltChunk chunk) {
        this.chunkRenderingDataPreparer.method_52827(chunk);
    }

    private void captureFrustum(Matrix4f positionMatrix, Matrix4f projectionMatrix, double x, double y, double z, Frustum frustum) {
        this.capturedFrustum = frustum;
        Matrix4f matrix4f3 = new Matrix4f(projectionMatrix);
        matrix4f3.mul(positionMatrix);
        matrix4f3.invert();
        this.capturedFrustumPosition.x = x;
        this.capturedFrustumPosition.y = y;
        this.capturedFrustumPosition.z = z;
        this.capturedFrustumOrientation[0] = new Vector4f(-1.0f, -1.0f, -1.0f, 1.0f);
        this.capturedFrustumOrientation[1] = new Vector4f(1.0f, -1.0f, -1.0f, 1.0f);
        this.capturedFrustumOrientation[2] = new Vector4f(1.0f, 1.0f, -1.0f, 1.0f);
        this.capturedFrustumOrientation[3] = new Vector4f(-1.0f, 1.0f, -1.0f, 1.0f);
        this.capturedFrustumOrientation[4] = new Vector4f(-1.0f, -1.0f, 1.0f, 1.0f);
        this.capturedFrustumOrientation[5] = new Vector4f(1.0f, -1.0f, 1.0f, 1.0f);
        this.capturedFrustumOrientation[6] = new Vector4f(1.0f, 1.0f, 1.0f, 1.0f);
        this.capturedFrustumOrientation[7] = new Vector4f(-1.0f, 1.0f, 1.0f, 1.0f);
        for (int i = 0; i < 8; ++i) {
            matrix4f3.transform(this.capturedFrustumOrientation[i]);
            this.capturedFrustumOrientation[i].div(this.capturedFrustumOrientation[i].w());
        }
    }

    public void setupFrustum(Vec3d arg, Matrix4f matrix4f, Matrix4f matrix4f2) {
        this.frustum = new Frustum(matrix4f, matrix4f2);
        this.frustum.setPosition(arg.getX(), arg.getY(), arg.getZ());
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public void render(RenderTickCounter tickCounter, boolean renderBlockOutline, Camera camera, GameRenderer gameRenderer, LightmapTextureManager lightmapTextureManager, Matrix4f matrix4f, Matrix4f matrix4f2) {
        BlockPos lv8;
        Frustum lv4;
        boolean bl2;
        TickManager lv = this.client.world.getTickManager();
        float f = tickCounter.getTickDelta(false);
        RenderSystem.setShaderGameTime(this.world.getTime(), f);
        this.blockEntityRenderDispatcher.configure(this.world, camera, this.client.crosshairTarget);
        this.entityRenderDispatcher.configure(this.world, camera, this.client.targetedEntity);
        Profiler lv2 = this.world.getProfiler();
        lv2.swap("light_update_queue");
        this.world.runQueuedChunkUpdates();
        lv2.swap("light_updates");
        this.world.getChunkManager().getLightingProvider().doLightUpdates();
        Vec3d lv3 = camera.getPos();
        double d = lv3.getX();
        double e = lv3.getY();
        double g = lv3.getZ();
        lv2.swap("culling");
        boolean bl = bl2 = this.capturedFrustum != null;
        if (bl2) {
            lv4 = this.capturedFrustum;
            lv4.setPosition(this.capturedFrustumPosition.x, this.capturedFrustumPosition.y, this.capturedFrustumPosition.z);
        } else {
            lv4 = this.frustum;
        }
        this.client.getProfiler().swap("captureFrustum");
        if (this.shouldCaptureFrustum) {
            this.captureFrustum(matrix4f, matrix4f2, lv3.x, lv3.y, lv3.z, bl2 ? new Frustum(matrix4f, matrix4f2) : lv4);
            this.shouldCaptureFrustum = false;
        }
        lv2.swap("clear");
        BackgroundRenderer.render(camera, f, this.client.world, this.client.options.getClampedViewDistance(), gameRenderer.getSkyDarkness(f));
        BackgroundRenderer.applyFogColor();
        RenderSystem.clear(GlConst.GL_DEPTH_BUFFER_BIT | GlConst.GL_COLOR_BUFFER_BIT, MinecraftClient.IS_SYSTEM_MAC);
        float h = gameRenderer.getViewDistance();
        boolean bl3 = this.client.world.getDimensionEffects().useThickFog(MathHelper.floor(d), MathHelper.floor(e)) || this.client.inGameHud.getBossBarHud().shouldThickenFog();
        lv2.swap("sky");
        RenderSystem.setShader(GameRenderer::getPositionProgram);
        this.renderSky(matrix4f, matrix4f2, f, camera, bl3, () -> BackgroundRenderer.applyFog(camera, BackgroundRenderer.FogType.FOG_SKY, h, bl3, f));
        lv2.swap("fog");
        BackgroundRenderer.applyFog(camera, BackgroundRenderer.FogType.FOG_TERRAIN, Math.max(h, 32.0f), bl3, f);
        lv2.swap("terrain_setup");
        this.setupTerrain(camera, lv4, bl2, this.client.player.isSpectator());
        lv2.swap("compile_sections");
        this.updateChunks(camera);
        lv2.swap("terrain");
        this.renderLayer(RenderLayer.getSolid(), d, e, g, matrix4f, matrix4f2);
        this.renderLayer(RenderLayer.getCutoutMipped(), d, e, g, matrix4f, matrix4f2);
        this.renderLayer(RenderLayer.getCutout(), d, e, g, matrix4f, matrix4f2);
        if (this.world.getDimensionEffects().isDarkened()) {
            DiffuseLighting.enableForLevel();
        } else {
            DiffuseLighting.disableForLevel();
        }
        lv2.swap("entities");
        this.regularEntityCount = 0;
        this.blockEntityCount = 0;
        if (this.entityFramebuffer != null) {
            this.entityFramebuffer.clear(MinecraftClient.IS_SYSTEM_MAC);
            this.entityFramebuffer.copyDepthFrom(this.client.getFramebuffer());
            this.client.getFramebuffer().beginWrite(false);
        }
        if (this.weatherFramebuffer != null) {
            this.weatherFramebuffer.clear(MinecraftClient.IS_SYSTEM_MAC);
        }
        if (this.canDrawEntityOutlines()) {
            this.entityOutlinesFramebuffer.clear(MinecraftClient.IS_SYSTEM_MAC);
            this.client.getFramebuffer().beginWrite(false);
        }
        Matrix4fStack matrix4fStack = RenderSystem.getModelViewStack();
        matrix4fStack.pushMatrix();
        matrix4fStack.mul(matrix4f);
        RenderSystem.applyModelViewMatrix();
        boolean bl4 = false;
        MatrixStack lv5 = new MatrixStack();
        VertexConsumerProvider.Immediate lv6 = this.bufferBuilders.getEntityVertexConsumers();
        for (Entity entity : this.world.getEntities()) {
            Object lv10;
            if (!this.entityRenderDispatcher.shouldRender(entity, lv4, d, e, g) && !entity.hasPassengerDeep(this.client.player) || !this.world.isOutOfHeightLimit((lv8 = entity.getBlockPos()).getY()) && !this.isRenderingReady(lv8) || entity == camera.getFocusedEntity() && !camera.isThirdPerson() && (!(camera.getFocusedEntity() instanceof LivingEntity) || !((LivingEntity)camera.getFocusedEntity()).isSleeping()) || entity instanceof ClientPlayerEntity && camera.getFocusedEntity() != entity) continue;
            ++this.regularEntityCount;
            if (entity.age == 0) {
                entity.lastRenderX = entity.getX();
                entity.lastRenderY = entity.getY();
                entity.lastRenderZ = entity.getZ();
            }
            if (this.canDrawEntityOutlines() && this.client.hasOutline(entity)) {
                bl4 = true;
                OutlineVertexConsumerProvider lv9 = this.bufferBuilders.getOutlineVertexConsumers();
                lv10 = lv9;
                int i = entity.getTeamColorValue();
                lv9.setColor(ColorHelper.Argb.getRed(i), ColorHelper.Argb.getGreen(i), ColorHelper.Argb.getBlue(i), 255);
            } else {
                lv10 = lv6;
            }
            float j = tickCounter.getTickDelta(!lv.shouldSkipTick(entity));
            this.renderEntity(entity, d, e, g, j, lv5, (VertexConsumerProvider)lv10);
        }
        lv6.drawCurrentLayer();
        this.checkEmpty(lv5);
        lv6.draw(RenderLayer.getEntitySolid(SpriteAtlasTexture.BLOCK_ATLAS_TEXTURE));
        lv6.draw(RenderLayer.getEntityCutout(SpriteAtlasTexture.BLOCK_ATLAS_TEXTURE));
        lv6.draw(RenderLayer.getEntityCutoutNoCull(SpriteAtlasTexture.BLOCK_ATLAS_TEXTURE));
        lv6.draw(RenderLayer.getEntitySmoothCutout(SpriteAtlasTexture.BLOCK_ATLAS_TEXTURE));
        lv2.swap("blockentities");
        for (ChunkBuilder.BuiltChunk builtChunk : this.builtChunks) {
            List<BlockEntity> list = builtChunk.getData().getBlockEntities();
            if (list.isEmpty()) continue;
            for (BlockEntity lv12 : list) {
                int k;
                BlockPos lv13 = lv12.getPos();
                VertexConsumerProvider lv14 = lv6;
                lv5.push();
                lv5.translate((double)lv13.getX() - d, (double)lv13.getY() - e, (double)lv13.getZ() - g);
                SortedSet sortedSet = (SortedSet)this.blockBreakingProgressions.get(lv13.asLong());
                if (sortedSet != null && !sortedSet.isEmpty() && (k = ((BlockBreakingInfo)sortedSet.last()).getStage()) >= 0) {
                    MatrixStack.Entry lv15 = lv5.peek();
                    OverlayVertexConsumer lv16 = new OverlayVertexConsumer(this.bufferBuilders.getEffectVertexConsumers().getBuffer(ModelLoader.BLOCK_DESTRUCTION_RENDER_LAYERS.get(k)), lv15, 1.0f);
                    lv14 = renderLayer -> {
                        VertexConsumer lv = lv6.getBuffer(renderLayer);
                        if (renderLayer.hasCrumbling()) {
                            return VertexConsumers.union(lv16, lv);
                        }
                        return lv;
                    };
                }
                this.blockEntityRenderDispatcher.render(lv12, f, lv5, lv14);
                lv5.pop();
            }
        }
        Set<BlockEntity> set = this.noCullingBlockEntities;
        synchronized (set) {
            for (BlockEntity lv17 : this.noCullingBlockEntities) {
                BlockPos lv18 = lv17.getPos();
                lv5.push();
                lv5.translate((double)lv18.getX() - d, (double)lv18.getY() - e, (double)lv18.getZ() - g);
                this.blockEntityRenderDispatcher.render(lv17, f, lv5, lv6);
                lv5.pop();
            }
        }
        this.checkEmpty(lv5);
        lv6.draw(RenderLayer.getSolid());
        lv6.draw(RenderLayer.getEndPortal());
        lv6.draw(RenderLayer.getEndGateway());
        lv6.draw(TexturedRenderLayers.getEntitySolid());
        lv6.draw(TexturedRenderLayers.getEntityCutout());
        lv6.draw(TexturedRenderLayers.getBeds());
        lv6.draw(TexturedRenderLayers.getShulkerBoxes());
        lv6.draw(TexturedRenderLayers.getSign());
        lv6.draw(TexturedRenderLayers.getHangingSign());
        lv6.draw(TexturedRenderLayers.getChest());
        this.bufferBuilders.getOutlineVertexConsumers().draw();
        if (bl4) {
            this.entityOutlinePostProcessor.render(tickCounter.getLastFrameDuration());
            this.client.getFramebuffer().beginWrite(false);
        }
        lv2.swap("destroyProgress");
        for (Long2ObjectMap.Entry entry : this.blockBreakingProgressions.long2ObjectEntrySet()) {
            SortedSet sortedSet2;
            double n;
            double m;
            lv8 = BlockPos.fromLong(entry.getLongKey());
            double l = (double)lv8.getX() - d;
            if (l * l + (m = (double)lv8.getY() - e) * m + (n = (double)lv8.getZ() - g) * n > 1024.0 || (sortedSet2 = (SortedSet)entry.getValue()) == null || sortedSet2.isEmpty()) continue;
            int o = ((BlockBreakingInfo)sortedSet2.last()).getStage();
            lv5.push();
            lv5.translate((double)lv8.getX() - d, (double)lv8.getY() - e, (double)lv8.getZ() - g);
            MatrixStack.Entry lv19 = lv5.peek();
            OverlayVertexConsumer lv20 = new OverlayVertexConsumer(this.bufferBuilders.getEffectVertexConsumers().getBuffer(ModelLoader.BLOCK_DESTRUCTION_RENDER_LAYERS.get(o)), lv19, 1.0f);
            this.client.getBlockRenderManager().renderDamage(this.world.getBlockState(lv8), lv8, this.world, lv5, lv20);
            lv5.pop();
        }
        this.checkEmpty(lv5);
        HitResult lv21 = this.client.crosshairTarget;
        if (renderBlockOutline && lv21 != null && lv21.getType() == HitResult.Type.BLOCK) {
            lv2.swap("outline");
            BlockPos blockPos = ((BlockHitResult)lv21).getBlockPos();
            BlockState lv23 = this.world.getBlockState(blockPos);
            if (!lv23.isAir() && this.world.getWorldBorder().contains(blockPos)) {
                VertexConsumer lv24 = lv6.getBuffer(RenderLayer.getLines());
                this.drawBlockOutline(lv5, lv24, camera.getFocusedEntity(), d, e, g, blockPos, lv23);
            }
        }
        this.client.debugRenderer.render(lv5, lv6, d, e, g);
        lv6.drawCurrentLayer();
        lv6.draw(TexturedRenderLayers.getEntityTranslucentCull());
        lv6.draw(TexturedRenderLayers.getBannerPatterns());
        lv6.draw(TexturedRenderLayers.getShieldPatterns());
        lv6.draw(RenderLayer.getArmorEntityGlint());
        lv6.draw(RenderLayer.getGlint());
        lv6.draw(RenderLayer.getGlintTranslucent());
        lv6.draw(RenderLayer.getEntityGlint());
        lv6.draw(RenderLayer.getDirectEntityGlint());
        lv6.draw(RenderLayer.getWaterMask());
        this.bufferBuilders.getEffectVertexConsumers().draw();
        if (this.transparencyPostProcessor != null) {
            lv6.draw(RenderLayer.getLines());
            lv6.draw();
            this.translucentFramebuffer.clear(MinecraftClient.IS_SYSTEM_MAC);
            this.translucentFramebuffer.copyDepthFrom(this.client.getFramebuffer());
            lv2.swap("translucent");
            this.renderLayer(RenderLayer.getTranslucent(), d, e, g, matrix4f, matrix4f2);
            lv2.swap("string");
            this.renderLayer(RenderLayer.getTripwire(), d, e, g, matrix4f, matrix4f2);
            this.particlesFramebuffer.clear(MinecraftClient.IS_SYSTEM_MAC);
            this.particlesFramebuffer.copyDepthFrom(this.client.getFramebuffer());
            RenderPhase.PARTICLES_TARGET.startDrawing();
            lv2.swap("particles");
            this.client.particleManager.renderParticles(lightmapTextureManager, camera, f);
            RenderPhase.PARTICLES_TARGET.endDrawing();
        } else {
            lv2.swap("translucent");
            if (this.translucentFramebuffer != null) {
                this.translucentFramebuffer.clear(MinecraftClient.IS_SYSTEM_MAC);
            }
            this.renderLayer(RenderLayer.getTranslucent(), d, e, g, matrix4f, matrix4f2);
            lv6.draw(RenderLayer.getLines());
            lv6.draw();
            lv2.swap("string");
            this.renderLayer(RenderLayer.getTripwire(), d, e, g, matrix4f, matrix4f2);
            lv2.swap("particles");
            this.client.particleManager.renderParticles(lightmapTextureManager, camera, f);
        }
        if (this.client.options.getCloudRenderModeValue() != CloudRenderMode.OFF) {
            if (this.transparencyPostProcessor != null) {
                this.cloudsFramebuffer.clear(MinecraftClient.IS_SYSTEM_MAC);
            }
            lv2.swap("clouds");
            this.renderClouds(lv5, matrix4f, matrix4f2, f, d, e, g);
        }
        if (this.transparencyPostProcessor != null) {
            RenderPhase.WEATHER_TARGET.startDrawing();
            lv2.swap("weather");
            this.renderWeather(lightmapTextureManager, f, d, e, g);
            this.renderWorldBorder(camera);
            RenderPhase.WEATHER_TARGET.endDrawing();
            this.transparencyPostProcessor.render(tickCounter.getLastFrameDuration());
            this.client.getFramebuffer().beginWrite(false);
        } else {
            RenderSystem.depthMask(false);
            lv2.swap("weather");
            this.renderWeather(lightmapTextureManager, f, d, e, g);
            this.renderWorldBorder(camera);
            RenderSystem.depthMask(true);
        }
        this.renderChunkDebugInfo(lv5, lv6, camera);
        lv6.drawCurrentLayer();
        matrix4fStack.popMatrix();
        RenderSystem.applyModelViewMatrix();
        RenderSystem.depthMask(true);
        RenderSystem.disableBlend();
        BackgroundRenderer.clearFog();
    }

    private void checkEmpty(MatrixStack matrices) {
        if (!matrices.isEmpty()) {
            throw new IllegalStateException("Pose stack not empty");
        }
    }

    private void renderEntity(Entity entity, double cameraX, double cameraY, double cameraZ, float tickDelta, MatrixStack matrices, VertexConsumerProvider vertexConsumers) {
        double h = MathHelper.lerp((double)tickDelta, entity.lastRenderX, entity.getX());
        double i = MathHelper.lerp((double)tickDelta, entity.lastRenderY, entity.getY());
        double j = MathHelper.lerp((double)tickDelta, entity.lastRenderZ, entity.getZ());
        float k = MathHelper.lerp(tickDelta, entity.prevYaw, entity.getYaw());
        this.entityRenderDispatcher.render(entity, h - cameraX, i - cameraY, j - cameraZ, k, tickDelta, matrices, vertexConsumers, this.entityRenderDispatcher.getLight(entity, tickDelta));
    }

    private void renderLayer(RenderLayer renderLayer, double x, double y, double z, Matrix4f matrix4f, Matrix4f positionMatrix) {
        RenderSystem.assertOnRenderThread();
        renderLayer.startDrawing();
        if (renderLayer == RenderLayer.getTranslucent()) {
            this.client.getProfiler().push("translucent_sort");
            double g = x - this.lastTranslucentSortX;
            double h = y - this.lastTranslucentSortY;
            double i = z - this.lastTranslucentSortZ;
            if (g * g + h * h + i * i > 1.0) {
                int j = ChunkSectionPos.getSectionCoord(x);
                int k = ChunkSectionPos.getSectionCoord(y);
                int l = ChunkSectionPos.getSectionCoord(z);
                boolean bl = j != ChunkSectionPos.getSectionCoord(this.lastTranslucentSortX) || l != ChunkSectionPos.getSectionCoord(this.lastTranslucentSortZ) || k != ChunkSectionPos.getSectionCoord(this.lastTranslucentSortY);
                this.lastTranslucentSortX = x;
                this.lastTranslucentSortY = y;
                this.lastTranslucentSortZ = z;
                int m = 0;
                for (ChunkBuilder.BuiltChunk lv : this.builtChunks) {
                    if (m >= 15 || !bl && !lv.method_52841(j, k, l) || !lv.scheduleSort(renderLayer, this.chunkBuilder)) continue;
                    ++m;
                }
            }
            this.client.getProfiler().pop();
        }
        this.client.getProfiler().push("filterempty");
        this.client.getProfiler().swap(() -> "render_" + String.valueOf(renderLayer));
        boolean bl2 = renderLayer != RenderLayer.getTranslucent();
        ListIterator objectListIterator = this.builtChunks.listIterator(bl2 ? 0 : this.builtChunks.size());
        ShaderProgram lv2 = RenderSystem.getShader();
        lv2.initializeUniforms(VertexFormat.DrawMode.QUADS, matrix4f, positionMatrix, this.client.getWindow());
        lv2.bind();
        GlUniform lv3 = lv2.chunkOffset;
        while (bl2 ? objectListIterator.hasNext() : objectListIterator.hasPrevious()) {
            ChunkBuilder.BuiltChunk lv4;
            ChunkBuilder.BuiltChunk builtChunk = lv4 = bl2 ? (ChunkBuilder.BuiltChunk)objectListIterator.next() : (ChunkBuilder.BuiltChunk)objectListIterator.previous();
            if (lv4.getData().isEmpty(renderLayer)) continue;
            VertexBuffer lv5 = lv4.getBuffer(renderLayer);
            BlockPos lv6 = lv4.getOrigin();
            if (lv3 != null) {
                lv3.set((float)((double)lv6.getX() - x), (float)((double)lv6.getY() - y), (float)((double)lv6.getZ() - z));
                lv3.upload();
            }
            lv5.bind();
            lv5.draw();
        }
        if (lv3 != null) {
            lv3.set(0.0f, 0.0f, 0.0f);
        }
        lv2.unbind();
        VertexBuffer.unbind();
        this.client.getProfiler().pop();
        renderLayer.endDrawing();
    }

    private void renderChunkDebugInfo(MatrixStack matrices, VertexConsumerProvider vertexConsumers, Camera camera) {
        if (this.client.debugChunkInfo || this.client.debugChunkOcclusion) {
            double d = camera.getPos().getX();
            double e = camera.getPos().getY();
            double f = camera.getPos().getZ();
            for (ChunkBuilder.BuiltChunk lv : this.builtChunks) {
                int i;
                VertexConsumer lv4;
                ChunkRenderingDataPreparer.ChunkInfo lv2 = this.chunkRenderingDataPreparer.method_52837(lv);
                if (lv2 == null) continue;
                BlockPos lv3 = lv.getOrigin();
                matrices.push();
                matrices.translate((double)lv3.getX() - d, (double)lv3.getY() - e, (double)lv3.getZ() - f);
                Matrix4f matrix4f = matrices.peek().getPositionMatrix();
                if (this.client.debugChunkInfo) {
                    lv4 = vertexConsumers.getBuffer(RenderLayer.getLines());
                    i = lv2.propagationLevel == 0 ? 0 : MathHelper.hsvToRgb((float)lv2.propagationLevel / 50.0f, 0.9f, 0.9f);
                    int j = i >> 16 & 0xFF;
                    int k = i >> 8 & 0xFF;
                    int l = i & 0xFF;
                    for (int m = 0; m < DIRECTIONS.length; ++m) {
                        if (!lv2.hasDirection(m)) continue;
                        Direction lv5 = DIRECTIONS[m];
                        lv4.vertex(matrix4f, 8.0f, 8.0f, 8.0f).color(j, k, l, 255).normal(lv5.getOffsetX(), lv5.getOffsetY(), lv5.getOffsetZ());
                        lv4.vertex(matrix4f, (float)(8 - 16 * lv5.getOffsetX()), (float)(8 - 16 * lv5.getOffsetY()), (float)(8 - 16 * lv5.getOffsetZ())).color(j, k, l, 255).normal(lv5.getOffsetX(), lv5.getOffsetY(), lv5.getOffsetZ());
                    }
                }
                if (this.client.debugChunkOcclusion && !lv.getData().isEmpty()) {
                    lv4 = vertexConsumers.getBuffer(RenderLayer.getLines());
                    i = 0;
                    for (Direction lv6 : DIRECTIONS) {
                        for (Direction lv7 : DIRECTIONS) {
                            boolean bl = lv.getData().isVisibleThrough(lv6, lv7);
                            if (bl) continue;
                            ++i;
                            lv4.vertex(matrix4f, (float)(8 + 8 * lv6.getOffsetX()), (float)(8 + 8 * lv6.getOffsetY()), (float)(8 + 8 * lv6.getOffsetZ())).color(255, 0, 0, 255).normal(lv6.getOffsetX(), lv6.getOffsetY(), lv6.getOffsetZ());
                            lv4.vertex(matrix4f, (float)(8 + 8 * lv7.getOffsetX()), (float)(8 + 8 * lv7.getOffsetY()), (float)(8 + 8 * lv7.getOffsetZ())).color(255, 0, 0, 255).normal(lv7.getOffsetX(), lv7.getOffsetY(), lv7.getOffsetZ());
                        }
                    }
                    if (i > 0) {
                        VertexConsumer lv8 = vertexConsumers.getBuffer(RenderLayer.getDebugQuads());
                        float g = 0.5f;
                        float h = 0.2f;
                        lv8.vertex(matrix4f, 0.5f, 15.5f, 0.5f).color(0.9f, 0.9f, 0.0f, 0.2f);
                        lv8.vertex(matrix4f, 15.5f, 15.5f, 0.5f).color(0.9f, 0.9f, 0.0f, 0.2f);
                        lv8.vertex(matrix4f, 15.5f, 15.5f, 15.5f).color(0.9f, 0.9f, 0.0f, 0.2f);
                        lv8.vertex(matrix4f, 0.5f, 15.5f, 15.5f).color(0.9f, 0.9f, 0.0f, 0.2f);
                        lv8.vertex(matrix4f, 0.5f, 0.5f, 15.5f).color(0.9f, 0.9f, 0.0f, 0.2f);
                        lv8.vertex(matrix4f, 15.5f, 0.5f, 15.5f).color(0.9f, 0.9f, 0.0f, 0.2f);
                        lv8.vertex(matrix4f, 15.5f, 0.5f, 0.5f).color(0.9f, 0.9f, 0.0f, 0.2f);
                        lv8.vertex(matrix4f, 0.5f, 0.5f, 0.5f).color(0.9f, 0.9f, 0.0f, 0.2f);
                        lv8.vertex(matrix4f, 0.5f, 15.5f, 0.5f).color(0.9f, 0.9f, 0.0f, 0.2f);
                        lv8.vertex(matrix4f, 0.5f, 15.5f, 15.5f).color(0.9f, 0.9f, 0.0f, 0.2f);
                        lv8.vertex(matrix4f, 0.5f, 0.5f, 15.5f).color(0.9f, 0.9f, 0.0f, 0.2f);
                        lv8.vertex(matrix4f, 0.5f, 0.5f, 0.5f).color(0.9f, 0.9f, 0.0f, 0.2f);
                        lv8.vertex(matrix4f, 15.5f, 0.5f, 0.5f).color(0.9f, 0.9f, 0.0f, 0.2f);
                        lv8.vertex(matrix4f, 15.5f, 0.5f, 15.5f).color(0.9f, 0.9f, 0.0f, 0.2f);
                        lv8.vertex(matrix4f, 15.5f, 15.5f, 15.5f).color(0.9f, 0.9f, 0.0f, 0.2f);
                        lv8.vertex(matrix4f, 15.5f, 15.5f, 0.5f).color(0.9f, 0.9f, 0.0f, 0.2f);
                        lv8.vertex(matrix4f, 0.5f, 0.5f, 0.5f).color(0.9f, 0.9f, 0.0f, 0.2f);
                        lv8.vertex(matrix4f, 15.5f, 0.5f, 0.5f).color(0.9f, 0.9f, 0.0f, 0.2f);
                        lv8.vertex(matrix4f, 15.5f, 15.5f, 0.5f).color(0.9f, 0.9f, 0.0f, 0.2f);
                        lv8.vertex(matrix4f, 0.5f, 15.5f, 0.5f).color(0.9f, 0.9f, 0.0f, 0.2f);
                        lv8.vertex(matrix4f, 0.5f, 15.5f, 15.5f).color(0.9f, 0.9f, 0.0f, 0.2f);
                        lv8.vertex(matrix4f, 15.5f, 15.5f, 15.5f).color(0.9f, 0.9f, 0.0f, 0.2f);
                        lv8.vertex(matrix4f, 15.5f, 0.5f, 15.5f).color(0.9f, 0.9f, 0.0f, 0.2f);
                        lv8.vertex(matrix4f, 0.5f, 0.5f, 15.5f).color(0.9f, 0.9f, 0.0f, 0.2f);
                    }
                }
                matrices.pop();
            }
        }
        if (this.capturedFrustum != null) {
            matrices.push();
            matrices.translate((float)(this.capturedFrustumPosition.x - camera.getPos().x), (float)(this.capturedFrustumPosition.y - camera.getPos().y), (float)(this.capturedFrustumPosition.z - camera.getPos().z));
            Matrix4f matrix4f2 = matrices.peek().getPositionMatrix();
            VertexConsumer lv9 = vertexConsumers.getBuffer(RenderLayer.getDebugQuads());
            this.renderCapturedFrustumFace(lv9, matrix4f2, 0, 1, 2, 3, 0, 1, 1);
            this.renderCapturedFrustumFace(lv9, matrix4f2, 4, 5, 6, 7, 1, 0, 0);
            this.renderCapturedFrustumFace(lv9, matrix4f2, 0, 1, 5, 4, 1, 1, 0);
            this.renderCapturedFrustumFace(lv9, matrix4f2, 2, 3, 7, 6, 0, 0, 1);
            this.renderCapturedFrustumFace(lv9, matrix4f2, 0, 4, 7, 3, 0, 1, 0);
            this.renderCapturedFrustumFace(lv9, matrix4f2, 1, 5, 6, 2, 1, 0, 1);
            VertexConsumer lv10 = vertexConsumers.getBuffer(RenderLayer.getLines());
            this.renderCapturedFrustumVertex(lv10, matrix4f2, 0);
            this.renderCapturedFrustumVertex(lv10, matrix4f2, 1);
            this.renderCapturedFrustumVertex(lv10, matrix4f2, 1);
            this.renderCapturedFrustumVertex(lv10, matrix4f2, 2);
            this.renderCapturedFrustumVertex(lv10, matrix4f2, 2);
            this.renderCapturedFrustumVertex(lv10, matrix4f2, 3);
            this.renderCapturedFrustumVertex(lv10, matrix4f2, 3);
            this.renderCapturedFrustumVertex(lv10, matrix4f2, 0);
            this.renderCapturedFrustumVertex(lv10, matrix4f2, 4);
            this.renderCapturedFrustumVertex(lv10, matrix4f2, 5);
            this.renderCapturedFrustumVertex(lv10, matrix4f2, 5);
            this.renderCapturedFrustumVertex(lv10, matrix4f2, 6);
            this.renderCapturedFrustumVertex(lv10, matrix4f2, 6);
            this.renderCapturedFrustumVertex(lv10, matrix4f2, 7);
            this.renderCapturedFrustumVertex(lv10, matrix4f2, 7);
            this.renderCapturedFrustumVertex(lv10, matrix4f2, 4);
            this.renderCapturedFrustumVertex(lv10, matrix4f2, 0);
            this.renderCapturedFrustumVertex(lv10, matrix4f2, 4);
            this.renderCapturedFrustumVertex(lv10, matrix4f2, 1);
            this.renderCapturedFrustumVertex(lv10, matrix4f2, 5);
            this.renderCapturedFrustumVertex(lv10, matrix4f2, 2);
            this.renderCapturedFrustumVertex(lv10, matrix4f2, 6);
            this.renderCapturedFrustumVertex(lv10, matrix4f2, 3);
            this.renderCapturedFrustumVertex(lv10, matrix4f2, 7);
            matrices.pop();
        }
    }

    private void renderCapturedFrustumVertex(VertexConsumer vertexConsumer, Matrix4f matrix4f, int planeNum) {
        vertexConsumer.vertex(matrix4f, this.capturedFrustumOrientation[planeNum].x(), this.capturedFrustumOrientation[planeNum].y(), this.capturedFrustumOrientation[planeNum].z()).color(Colors.BLACK).normal(0.0f, 0.0f, -1.0f);
    }

    private void renderCapturedFrustumFace(VertexConsumer vertexConsumer, Matrix4f matrix4f, int plane0, int plane1, int plane2, int plane3, int r, int g, int b) {
        float f = 0.25f;
        vertexConsumer.vertex(matrix4f, this.capturedFrustumOrientation[plane0].x(), this.capturedFrustumOrientation[plane0].y(), this.capturedFrustumOrientation[plane0].z()).color((float)r, (float)g, (float)b, 0.25f);
        vertexConsumer.vertex(matrix4f, this.capturedFrustumOrientation[plane1].x(), this.capturedFrustumOrientation[plane1].y(), this.capturedFrustumOrientation[plane1].z()).color((float)r, (float)g, (float)b, 0.25f);
        vertexConsumer.vertex(matrix4f, this.capturedFrustumOrientation[plane2].x(), this.capturedFrustumOrientation[plane2].y(), this.capturedFrustumOrientation[plane2].z()).color((float)r, (float)g, (float)b, 0.25f);
        vertexConsumer.vertex(matrix4f, this.capturedFrustumOrientation[plane3].x(), this.capturedFrustumOrientation[plane3].y(), this.capturedFrustumOrientation[plane3].z()).color((float)r, (float)g, (float)b, 0.25f);
    }

    public void captureFrustum() {
        this.shouldCaptureFrustum = true;
    }

    public void killFrustum() {
        this.capturedFrustum = null;
    }

    public void tick() {
        if (this.world.getTickManager().shouldTick()) {
            ++this.ticks;
        }
        if (this.ticks % 20 != 0) {
            return;
        }
        Iterator iterator = this.blockBreakingInfos.values().iterator();
        while (iterator.hasNext()) {
            BlockBreakingInfo lv = (BlockBreakingInfo)iterator.next();
            int i = lv.getLastUpdateTick();
            if (this.ticks - i <= 400) continue;
            iterator.remove();
            this.removeBlockBreakingInfo(lv);
        }
    }

    private void removeBlockBreakingInfo(BlockBreakingInfo info) {
        long l = info.getPos().asLong();
        Set set = (Set)this.blockBreakingProgressions.get(l);
        set.remove(info);
        if (set.isEmpty()) {
            this.blockBreakingProgressions.remove(l);
        }
    }

    private void renderEndSky(MatrixStack matrices) {
        RenderSystem.enableBlend();
        RenderSystem.depthMask(false);
        RenderSystem.setShader(GameRenderer::getPositionTexColorProgram);
        RenderSystem.setShaderTexture(0, END_SKY);
        Tessellator lv = Tessellator.getInstance();
        for (int i = 0; i < 6; ++i) {
            matrices.push();
            if (i == 1) {
                matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(90.0f));
            }
            if (i == 2) {
                matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(-90.0f));
            }
            if (i == 3) {
                matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(180.0f));
            }
            if (i == 4) {
                matrices.multiply(RotationAxis.POSITIVE_Z.rotationDegrees(90.0f));
            }
            if (i == 5) {
                matrices.multiply(RotationAxis.POSITIVE_Z.rotationDegrees(-90.0f));
            }
            Matrix4f matrix4f = matrices.peek().getPositionMatrix();
            BufferBuilder lv2 = lv.begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_TEXTURE_COLOR);
            lv2.vertex(matrix4f, -100.0f, -100.0f, -100.0f).texture(0.0f, 0.0f).color(-14145496);
            lv2.vertex(matrix4f, -100.0f, -100.0f, 100.0f).texture(0.0f, 16.0f).color(-14145496);
            lv2.vertex(matrix4f, 100.0f, -100.0f, 100.0f).texture(16.0f, 16.0f).color(-14145496);
            lv2.vertex(matrix4f, 100.0f, -100.0f, -100.0f).texture(16.0f, 0.0f).color(-14145496);
            BufferRenderer.drawWithGlobalProgram(lv2.end());
            matrices.pop();
        }
        RenderSystem.depthMask(true);
        RenderSystem.disableBlend();
    }

    public void renderSky(Matrix4f matrix4f, Matrix4f projectionMatrix, float tickDelta, Camera camera, boolean thickFog, Runnable fogCallback) {
        float r;
        float q;
        float p;
        int n;
        float l;
        float j;
        fogCallback.run();
        if (thickFog) {
            return;
        }
        CameraSubmersionType lv = camera.getSubmersionType();
        if (lv == CameraSubmersionType.POWDER_SNOW || lv == CameraSubmersionType.LAVA || this.hasBlindnessOrDarkness(camera)) {
            return;
        }
        MatrixStack lv2 = new MatrixStack();
        lv2.multiplyPositionMatrix(matrix4f);
        if (this.client.world.getDimensionEffects().getSkyType() == DimensionEffects.SkyType.END) {
            this.renderEndSky(lv2);
            return;
        }
        if (this.client.world.getDimensionEffects().getSkyType() != DimensionEffects.SkyType.NORMAL) {
            return;
        }
        Vec3d lv3 = this.world.getSkyColor(this.client.gameRenderer.getCamera().getPos(), tickDelta);
        float g = (float)lv3.x;
        float h = (float)lv3.y;
        float i = (float)lv3.z;
        BackgroundRenderer.applyFogColor();
        Tessellator lv4 = Tessellator.getInstance();
        RenderSystem.depthMask(false);
        RenderSystem.setShaderColor(g, h, i, 1.0f);
        ShaderProgram lv5 = RenderSystem.getShader();
        this.lightSkyBuffer.bind();
        this.lightSkyBuffer.draw(lv2.peek().getPositionMatrix(), projectionMatrix, lv5);
        VertexBuffer.unbind();
        RenderSystem.enableBlend();
        float[] fs = this.world.getDimensionEffects().getFogColorOverride(this.world.getSkyAngle(tickDelta), tickDelta);
        if (fs != null) {
            RenderSystem.setShader(GameRenderer::getPositionColorProgram);
            RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, 1.0f);
            lv2.push();
            lv2.multiply(RotationAxis.POSITIVE_X.rotationDegrees(90.0f));
            j = MathHelper.sin(this.world.getSkyAngleRadians(tickDelta)) < 0.0f ? 180.0f : 0.0f;
            lv2.multiply(RotationAxis.POSITIVE_Z.rotationDegrees(j));
            lv2.multiply(RotationAxis.POSITIVE_Z.rotationDegrees(90.0f));
            float k = fs[0];
            l = fs[1];
            float m = fs[2];
            Matrix4f matrix4f3 = lv2.peek().getPositionMatrix();
            BufferBuilder lv6 = lv4.begin(VertexFormat.DrawMode.TRIANGLE_FAN, VertexFormats.POSITION_COLOR);
            lv6.vertex(matrix4f3, 0.0f, 100.0f, 0.0f).color(k, l, m, fs[3]);
            n = 16;
            for (int o = 0; o <= 16; ++o) {
                p = (float)o * ((float)Math.PI * 2) / 16.0f;
                q = MathHelper.sin(p);
                r = MathHelper.cos(p);
                lv6.vertex(matrix4f3, q * 120.0f, r * 120.0f, -r * 40.0f * fs[3]).color(fs[0], fs[1], fs[2], 0.0f);
            }
            BufferRenderer.drawWithGlobalProgram(lv6.end());
            lv2.pop();
        }
        RenderSystem.blendFuncSeparate(GlStateManager.SrcFactor.SRC_ALPHA, GlStateManager.DstFactor.ONE, GlStateManager.SrcFactor.ONE, GlStateManager.DstFactor.ZERO);
        lv2.push();
        j = 1.0f - this.world.getRainGradient(tickDelta);
        RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, j);
        lv2.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(-90.0f));
        lv2.multiply(RotationAxis.POSITIVE_X.rotationDegrees(this.world.getSkyAngle(tickDelta) * 360.0f));
        Matrix4f matrix4f4 = lv2.peek().getPositionMatrix();
        l = 30.0f;
        RenderSystem.setShader(GameRenderer::getPositionTexProgram);
        RenderSystem.setShaderTexture(0, SUN);
        BufferBuilder lv7 = lv4.begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_TEXTURE);
        lv7.vertex(matrix4f4, -l, 100.0f, -l).texture(0.0f, 0.0f);
        lv7.vertex(matrix4f4, l, 100.0f, -l).texture(1.0f, 0.0f);
        lv7.vertex(matrix4f4, l, 100.0f, l).texture(1.0f, 1.0f);
        lv7.vertex(matrix4f4, -l, 100.0f, l).texture(0.0f, 1.0f);
        BufferRenderer.drawWithGlobalProgram(lv7.end());
        l = 20.0f;
        RenderSystem.setShaderTexture(0, MOON_PHASES);
        int s = this.world.getMoonPhase();
        int t = s % 4;
        n = s / 4 % 2;
        float u = (float)(t + 0) / 4.0f;
        p = (float)(n + 0) / 2.0f;
        q = (float)(t + 1) / 4.0f;
        r = (float)(n + 1) / 2.0f;
        lv7 = lv4.begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_TEXTURE);
        lv7.vertex(matrix4f4, -l, -100.0f, l).texture(q, r);
        lv7.vertex(matrix4f4, l, -100.0f, l).texture(u, r);
        lv7.vertex(matrix4f4, l, -100.0f, -l).texture(u, p);
        lv7.vertex(matrix4f4, -l, -100.0f, -l).texture(q, p);
        BufferRenderer.drawWithGlobalProgram(lv7.end());
        float v = this.world.getStarBrightness(tickDelta) * j;
        if (v > 0.0f) {
            RenderSystem.setShaderColor(v, v, v, v);
            BackgroundRenderer.clearFog();
            this.starsBuffer.bind();
            this.starsBuffer.draw(lv2.peek().getPositionMatrix(), projectionMatrix, GameRenderer.getPositionProgram());
            VertexBuffer.unbind();
            fogCallback.run();
        }
        RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, 1.0f);
        RenderSystem.disableBlend();
        RenderSystem.defaultBlendFunc();
        lv2.pop();
        RenderSystem.setShaderColor(0.0f, 0.0f, 0.0f, 1.0f);
        double d = this.client.player.getCameraPosVec((float)tickDelta).y - this.world.getLevelProperties().getSkyDarknessHeight(this.world);
        if (d < 0.0) {
            lv2.push();
            lv2.translate(0.0f, 12.0f, 0.0f);
            this.darkSkyBuffer.bind();
            this.darkSkyBuffer.draw(lv2.peek().getPositionMatrix(), projectionMatrix, lv5);
            VertexBuffer.unbind();
            lv2.pop();
        }
        RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, 1.0f);
        RenderSystem.depthMask(true);
    }

    private boolean hasBlindnessOrDarkness(Camera camera) {
        Entity entity = camera.getFocusedEntity();
        if (entity instanceof LivingEntity) {
            LivingEntity lv = (LivingEntity)entity;
            return lv.hasStatusEffect(StatusEffects.BLINDNESS) || lv.hasStatusEffect(StatusEffects.DARKNESS);
        }
        return false;
    }

    public void renderClouds(MatrixStack matrices, Matrix4f matrix4f, Matrix4f matrix4f2, float tickDelta, double cameraX, double cameraY, double cameraZ) {
        float h = this.world.getDimensionEffects().getCloudsHeight();
        if (Float.isNaN(h)) {
            return;
        }
        float i = 12.0f;
        float j = 4.0f;
        double k = 2.0E-4;
        double l = ((float)this.ticks + tickDelta) * 0.03f;
        double m = (cameraX + l) / 12.0;
        double n = h - (float)cameraY + 0.33f;
        double o = cameraZ / 12.0 + (double)0.33f;
        m -= (double)(MathHelper.floor(m / 2048.0) * 2048);
        o -= (double)(MathHelper.floor(o / 2048.0) * 2048);
        float p = (float)(m - (double)MathHelper.floor(m));
        float q = (float)(n / 4.0 - (double)MathHelper.floor(n / 4.0)) * 4.0f;
        float r = (float)(o - (double)MathHelper.floor(o));
        Vec3d lv = this.world.getCloudsColor(tickDelta);
        int s = (int)Math.floor(m);
        int t = (int)Math.floor(n / 4.0);
        int u = (int)Math.floor(o);
        if (s != this.lastCloudsBlockX || t != this.lastCloudsBlockY || u != this.lastCloudsBlockZ || this.client.options.getCloudRenderModeValue() != this.lastCloudRenderMode || this.lastCloudsColor.squaredDistanceTo(lv) > 2.0E-4) {
            this.lastCloudsBlockX = s;
            this.lastCloudsBlockY = t;
            this.lastCloudsBlockZ = u;
            this.lastCloudsColor = lv;
            this.lastCloudRenderMode = this.client.options.getCloudRenderModeValue();
            this.cloudsDirty = true;
        }
        if (this.cloudsDirty) {
            this.cloudsDirty = false;
            if (this.cloudsBuffer != null) {
                this.cloudsBuffer.close();
            }
            this.cloudsBuffer = new VertexBuffer(VertexBuffer.Usage.STATIC);
            this.cloudsBuffer.bind();
            this.cloudsBuffer.upload(this.buildCloudsBuffer(Tessellator.getInstance(), m, n, o, lv));
            VertexBuffer.unbind();
        }
        BackgroundRenderer.applyFogColor();
        matrices.push();
        matrices.multiplyPositionMatrix(matrix4f);
        matrices.scale(12.0f, 1.0f, 12.0f);
        matrices.translate(-p, q, -r);
        if (this.cloudsBuffer != null) {
            int v;
            this.cloudsBuffer.bind();
            for (int w = v = this.lastCloudRenderMode == CloudRenderMode.FANCY ? 0 : 1; w < 2; ++w) {
                RenderLayer lv2 = w == 0 ? RenderLayer.getFancyClouds() : RenderLayer.getFastClouds();
                lv2.startDrawing();
                ShaderProgram lv3 = RenderSystem.getShader();
                this.cloudsBuffer.draw(matrices.peek().getPositionMatrix(), matrix4f2, lv3);
                lv2.endDrawing();
            }
            VertexBuffer.unbind();
        }
        matrices.pop();
    }

    private BuiltBuffer buildCloudsBuffer(Tessellator arg, double x, double y, double z, Vec3d color) {
        float g = 4.0f;
        float h = 0.00390625f;
        int i = 8;
        int j = 4;
        float k = 9.765625E-4f;
        float l = (float)MathHelper.floor(x) * 0.00390625f;
        float m = (float)MathHelper.floor(z) * 0.00390625f;
        float n = (float)color.x;
        float o = (float)color.y;
        float p = (float)color.z;
        float q = n * 0.9f;
        float r = o * 0.9f;
        float s = p * 0.9f;
        float t = n * 0.7f;
        float u = o * 0.7f;
        float v = p * 0.7f;
        float w = n * 0.8f;
        float x2 = o * 0.8f;
        float y2 = p * 0.8f;
        BufferBuilder lv = arg.begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_TEXTURE_COLOR_NORMAL);
        float z2 = (float)Math.floor(y / 4.0) * 4.0f;
        if (this.lastCloudRenderMode == CloudRenderMode.FANCY) {
            for (int aa = -3; aa <= 4; ++aa) {
                for (int ab = -3; ab <= 4; ++ab) {
                    int ae;
                    float ac = aa * 8;
                    float ad = ab * 8;
                    if (z2 > -5.0f) {
                        lv.vertex(ac + 0.0f, z2 + 0.0f, ad + 8.0f).texture((ac + 0.0f) * 0.00390625f + l, (ad + 8.0f) * 0.00390625f + m).color(t, u, v, 0.8f).normal(0.0f, -1.0f, 0.0f);
                        lv.vertex(ac + 8.0f, z2 + 0.0f, ad + 8.0f).texture((ac + 8.0f) * 0.00390625f + l, (ad + 8.0f) * 0.00390625f + m).color(t, u, v, 0.8f).normal(0.0f, -1.0f, 0.0f);
                        lv.vertex(ac + 8.0f, z2 + 0.0f, ad + 0.0f).texture((ac + 8.0f) * 0.00390625f + l, (ad + 0.0f) * 0.00390625f + m).color(t, u, v, 0.8f).normal(0.0f, -1.0f, 0.0f);
                        lv.vertex(ac + 0.0f, z2 + 0.0f, ad + 0.0f).texture((ac + 0.0f) * 0.00390625f + l, (ad + 0.0f) * 0.00390625f + m).color(t, u, v, 0.8f).normal(0.0f, -1.0f, 0.0f);
                    }
                    if (z2 <= 5.0f) {
                        lv.vertex(ac + 0.0f, z2 + 4.0f - 9.765625E-4f, ad + 8.0f).texture((ac + 0.0f) * 0.00390625f + l, (ad + 8.0f) * 0.00390625f + m).color(n, o, p, 0.8f).normal(0.0f, 1.0f, 0.0f);
                        lv.vertex(ac + 8.0f, z2 + 4.0f - 9.765625E-4f, ad + 8.0f).texture((ac + 8.0f) * 0.00390625f + l, (ad + 8.0f) * 0.00390625f + m).color(n, o, p, 0.8f).normal(0.0f, 1.0f, 0.0f);
                        lv.vertex(ac + 8.0f, z2 + 4.0f - 9.765625E-4f, ad + 0.0f).texture((ac + 8.0f) * 0.00390625f + l, (ad + 0.0f) * 0.00390625f + m).color(n, o, p, 0.8f).normal(0.0f, 1.0f, 0.0f);
                        lv.vertex(ac + 0.0f, z2 + 4.0f - 9.765625E-4f, ad + 0.0f).texture((ac + 0.0f) * 0.00390625f + l, (ad + 0.0f) * 0.00390625f + m).color(n, o, p, 0.8f).normal(0.0f, 1.0f, 0.0f);
                    }
                    if (aa > -1) {
                        for (ae = 0; ae < 8; ++ae) {
                            lv.vertex(ac + (float)ae + 0.0f, z2 + 0.0f, ad + 8.0f).texture((ac + (float)ae + 0.5f) * 0.00390625f + l, (ad + 8.0f) * 0.00390625f + m).color(q, r, s, 0.8f).normal(-1.0f, 0.0f, 0.0f);
                            lv.vertex(ac + (float)ae + 0.0f, z2 + 4.0f, ad + 8.0f).texture((ac + (float)ae + 0.5f) * 0.00390625f + l, (ad + 8.0f) * 0.00390625f + m).color(q, r, s, 0.8f).normal(-1.0f, 0.0f, 0.0f);
                            lv.vertex(ac + (float)ae + 0.0f, z2 + 4.0f, ad + 0.0f).texture((ac + (float)ae + 0.5f) * 0.00390625f + l, (ad + 0.0f) * 0.00390625f + m).color(q, r, s, 0.8f).normal(-1.0f, 0.0f, 0.0f);
                            lv.vertex(ac + (float)ae + 0.0f, z2 + 0.0f, ad + 0.0f).texture((ac + (float)ae + 0.5f) * 0.00390625f + l, (ad + 0.0f) * 0.00390625f + m).color(q, r, s, 0.8f).normal(-1.0f, 0.0f, 0.0f);
                        }
                    }
                    if (aa <= 1) {
                        for (ae = 0; ae < 8; ++ae) {
                            lv.vertex(ac + (float)ae + 1.0f - 9.765625E-4f, z2 + 0.0f, ad + 8.0f).texture((ac + (float)ae + 0.5f) * 0.00390625f + l, (ad + 8.0f) * 0.00390625f + m).color(q, r, s, 0.8f).normal(1.0f, 0.0f, 0.0f);
                            lv.vertex(ac + (float)ae + 1.0f - 9.765625E-4f, z2 + 4.0f, ad + 8.0f).texture((ac + (float)ae + 0.5f) * 0.00390625f + l, (ad + 8.0f) * 0.00390625f + m).color(q, r, s, 0.8f).normal(1.0f, 0.0f, 0.0f);
                            lv.vertex(ac + (float)ae + 1.0f - 9.765625E-4f, z2 + 4.0f, ad + 0.0f).texture((ac + (float)ae + 0.5f) * 0.00390625f + l, (ad + 0.0f) * 0.00390625f + m).color(q, r, s, 0.8f).normal(1.0f, 0.0f, 0.0f);
                            lv.vertex(ac + (float)ae + 1.0f - 9.765625E-4f, z2 + 0.0f, ad + 0.0f).texture((ac + (float)ae + 0.5f) * 0.00390625f + l, (ad + 0.0f) * 0.00390625f + m).color(q, r, s, 0.8f).normal(1.0f, 0.0f, 0.0f);
                        }
                    }
                    if (ab > -1) {
                        for (ae = 0; ae < 8; ++ae) {
                            lv.vertex(ac + 0.0f, z2 + 4.0f, ad + (float)ae + 0.0f).texture((ac + 0.0f) * 0.00390625f + l, (ad + (float)ae + 0.5f) * 0.00390625f + m).color(w, x2, y2, 0.8f).normal(0.0f, 0.0f, -1.0f);
                            lv.vertex(ac + 8.0f, z2 + 4.0f, ad + (float)ae + 0.0f).texture((ac + 8.0f) * 0.00390625f + l, (ad + (float)ae + 0.5f) * 0.00390625f + m).color(w, x2, y2, 0.8f).normal(0.0f, 0.0f, -1.0f);
                            lv.vertex(ac + 8.0f, z2 + 0.0f, ad + (float)ae + 0.0f).texture((ac + 8.0f) * 0.00390625f + l, (ad + (float)ae + 0.5f) * 0.00390625f + m).color(w, x2, y2, 0.8f).normal(0.0f, 0.0f, -1.0f);
                            lv.vertex(ac + 0.0f, z2 + 0.0f, ad + (float)ae + 0.0f).texture((ac + 0.0f) * 0.00390625f + l, (ad + (float)ae + 0.5f) * 0.00390625f + m).color(w, x2, y2, 0.8f).normal(0.0f, 0.0f, -1.0f);
                        }
                    }
                    if (ab > 1) continue;
                    for (ae = 0; ae < 8; ++ae) {
                        lv.vertex(ac + 0.0f, z2 + 4.0f, ad + (float)ae + 1.0f - 9.765625E-4f).texture((ac + 0.0f) * 0.00390625f + l, (ad + (float)ae + 0.5f) * 0.00390625f + m).color(w, x2, y2, 0.8f).normal(0.0f, 0.0f, 1.0f);
                        lv.vertex(ac + 8.0f, z2 + 4.0f, ad + (float)ae + 1.0f - 9.765625E-4f).texture((ac + 8.0f) * 0.00390625f + l, (ad + (float)ae + 0.5f) * 0.00390625f + m).color(w, x2, y2, 0.8f).normal(0.0f, 0.0f, 1.0f);
                        lv.vertex(ac + 8.0f, z2 + 0.0f, ad + (float)ae + 1.0f - 9.765625E-4f).texture((ac + 8.0f) * 0.00390625f + l, (ad + (float)ae + 0.5f) * 0.00390625f + m).color(w, x2, y2, 0.8f).normal(0.0f, 0.0f, 1.0f);
                        lv.vertex(ac + 0.0f, z2 + 0.0f, ad + (float)ae + 1.0f - 9.765625E-4f).texture((ac + 0.0f) * 0.00390625f + l, (ad + (float)ae + 0.5f) * 0.00390625f + m).color(w, x2, y2, 0.8f).normal(0.0f, 0.0f, 1.0f);
                    }
                }
            }
        } else {
            boolean aa = true;
            int ab = 32;
            for (int af = -32; af < 32; af += 32) {
                for (int ag = -32; ag < 32; ag += 32) {
                    lv.vertex(af + 0, z2, ag + 32).texture((float)(af + 0) * 0.00390625f + l, (float)(ag + 32) * 0.00390625f + m).color(n, o, p, 0.8f).normal(0.0f, -1.0f, 0.0f);
                    lv.vertex(af + 32, z2, ag + 32).texture((float)(af + 32) * 0.00390625f + l, (float)(ag + 32) * 0.00390625f + m).color(n, o, p, 0.8f).normal(0.0f, -1.0f, 0.0f);
                    lv.vertex(af + 32, z2, ag + 0).texture((float)(af + 32) * 0.00390625f + l, (float)(ag + 0) * 0.00390625f + m).color(n, o, p, 0.8f).normal(0.0f, -1.0f, 0.0f);
                    lv.vertex(af + 0, z2, ag + 0).texture((float)(af + 0) * 0.00390625f + l, (float)(ag + 0) * 0.00390625f + m).color(n, o, p, 0.8f).normal(0.0f, -1.0f, 0.0f);
                }
            }
        }
        return lv.end();
    }

    private void updateChunks(Camera camera) {
        this.client.getProfiler().push("populate_sections_to_compile");
        LightingProvider lv = this.world.getLightingProvider();
        ChunkRendererRegionBuilder lv2 = new ChunkRendererRegionBuilder();
        BlockPos lv3 = camera.getBlockPos();
        ArrayList<ChunkBuilder.BuiltChunk> list = Lists.newArrayList();
        for (ChunkBuilder.BuiltChunk lv4 : this.builtChunks) {
            ChunkSectionPos lv5 = ChunkSectionPos.from(lv4.getOrigin());
            if (!lv4.needsRebuild() || !lv.isLightingEnabled(lv5)) continue;
            boolean bl = false;
            if (this.client.options.getChunkBuilderMode().getValue() == ChunkBuilderMode.NEARBY) {
                BlockPos lv6 = lv4.getOrigin().add(8, 8, 8);
                bl = lv6.getSquaredDistance(lv3) < 768.0 || lv4.needsImportantRebuild();
            } else if (this.client.options.getChunkBuilderMode().getValue() == ChunkBuilderMode.PLAYER_AFFECTED) {
                bl = lv4.needsImportantRebuild();
            }
            if (bl) {
                this.client.getProfiler().push("build_near_sync");
                this.chunkBuilder.rebuild(lv4, lv2);
                lv4.cancelRebuild();
                this.client.getProfiler().pop();
                continue;
            }
            list.add(lv4);
        }
        this.client.getProfiler().swap("upload");
        this.chunkBuilder.upload();
        this.client.getProfiler().swap("schedule_async_compile");
        for (ChunkBuilder.BuiltChunk lv4 : list) {
            lv4.scheduleRebuild(this.chunkBuilder, lv2);
            lv4.cancelRebuild();
        }
        this.client.getProfiler().pop();
    }

    private void renderWorldBorder(Camera camera) {
        BuiltBuffer lv3;
        float v;
        double u;
        double t;
        float s;
        WorldBorder lv = this.world.getWorldBorder();
        double d = this.client.options.getClampedViewDistance() * 16;
        if (camera.getPos().x < lv.getBoundEast() - d && camera.getPos().x > lv.getBoundWest() + d && camera.getPos().z < lv.getBoundSouth() - d && camera.getPos().z > lv.getBoundNorth() + d) {
            return;
        }
        double e = 1.0 - lv.getDistanceInsideBorder(camera.getPos().x, camera.getPos().z) / d;
        e = Math.pow(e, 4.0);
        e = MathHelper.clamp(e, 0.0, 1.0);
        double f = camera.getPos().x;
        double g = camera.getPos().z;
        double h = this.client.gameRenderer.getFarPlaneDistance();
        RenderSystem.enableBlend();
        RenderSystem.enableDepthTest();
        RenderSystem.blendFuncSeparate(GlStateManager.SrcFactor.SRC_ALPHA, GlStateManager.DstFactor.ONE, GlStateManager.SrcFactor.ONE, GlStateManager.DstFactor.ZERO);
        RenderSystem.setShaderTexture(0, FORCEFIELD);
        RenderSystem.depthMask(MinecraftClient.isFabulousGraphicsOrBetter());
        int i = lv.getStage().getColor();
        float j = (float)(i >> 16 & 0xFF) / 255.0f;
        float k = (float)(i >> 8 & 0xFF) / 255.0f;
        float l = (float)(i & 0xFF) / 255.0f;
        RenderSystem.setShaderColor(j, k, l, (float)e);
        RenderSystem.setShader(GameRenderer::getPositionTexProgram);
        RenderSystem.polygonOffset(-3.0f, -3.0f);
        RenderSystem.enablePolygonOffset();
        RenderSystem.disableCull();
        float m = (float)(Util.getMeasuringTimeMs() % 3000L) / 3000.0f;
        float n = (float)(-MathHelper.fractionalPart(camera.getPos().y * 0.5));
        float o = n + (float)h;
        BufferBuilder lv2 = Tessellator.getInstance().begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_TEXTURE);
        double p = Math.max((double)MathHelper.floor(g - d), lv.getBoundNorth());
        double q = Math.min((double)MathHelper.ceil(g + d), lv.getBoundSouth());
        float r = (float)(MathHelper.floor(p) & 1) * 0.5f;
        if (f > lv.getBoundEast() - d) {
            s = r;
            t = p;
            while (t < q) {
                u = Math.min(1.0, q - t);
                v = (float)u * 0.5f;
                lv2.vertex((float)(lv.getBoundEast() - f), (float)(-h), (float)(t - g)).texture(m - s, m + o);
                lv2.vertex((float)(lv.getBoundEast() - f), (float)(-h), (float)(t + u - g)).texture(m - (v + s), m + o);
                lv2.vertex((float)(lv.getBoundEast() - f), (float)h, (float)(t + u - g)).texture(m - (v + s), m + n);
                lv2.vertex((float)(lv.getBoundEast() - f), (float)h, (float)(t - g)).texture(m - s, m + n);
                t += 1.0;
                s += 0.5f;
            }
        }
        if (f < lv.getBoundWest() + d) {
            s = r;
            t = p;
            while (t < q) {
                u = Math.min(1.0, q - t);
                v = (float)u * 0.5f;
                lv2.vertex((float)(lv.getBoundWest() - f), (float)(-h), (float)(t - g)).texture(m + s, m + o);
                lv2.vertex((float)(lv.getBoundWest() - f), (float)(-h), (float)(t + u - g)).texture(m + v + s, m + o);
                lv2.vertex((float)(lv.getBoundWest() - f), (float)h, (float)(t + u - g)).texture(m + v + s, m + n);
                lv2.vertex((float)(lv.getBoundWest() - f), (float)h, (float)(t - g)).texture(m + s, m + n);
                t += 1.0;
                s += 0.5f;
            }
        }
        p = Math.max((double)MathHelper.floor(f - d), lv.getBoundWest());
        q = Math.min((double)MathHelper.ceil(f + d), lv.getBoundEast());
        r = (float)(MathHelper.floor(p) & 1) * 0.5f;
        if (g > lv.getBoundSouth() - d) {
            s = r;
            t = p;
            while (t < q) {
                u = Math.min(1.0, q - t);
                v = (float)u * 0.5f;
                lv2.vertex((float)(t - f), (float)(-h), (float)(lv.getBoundSouth() - g)).texture(m + s, m + o);
                lv2.vertex((float)(t + u - f), (float)(-h), (float)(lv.getBoundSouth() - g)).texture(m + v + s, m + o);
                lv2.vertex((float)(t + u - f), (float)h, (float)(lv.getBoundSouth() - g)).texture(m + v + s, m + n);
                lv2.vertex((float)(t - f), (float)h, (float)(lv.getBoundSouth() - g)).texture(m + s, m + n);
                t += 1.0;
                s += 0.5f;
            }
        }
        if (g < lv.getBoundNorth() + d) {
            s = r;
            t = p;
            while (t < q) {
                u = Math.min(1.0, q - t);
                v = (float)u * 0.5f;
                lv2.vertex((float)(t - f), (float)(-h), (float)(lv.getBoundNorth() - g)).texture(m - s, m + o);
                lv2.vertex((float)(t + u - f), (float)(-h), (float)(lv.getBoundNorth() - g)).texture(m - (v + s), m + o);
                lv2.vertex((float)(t + u - f), (float)h, (float)(lv.getBoundNorth() - g)).texture(m - (v + s), m + n);
                lv2.vertex((float)(t - f), (float)h, (float)(lv.getBoundNorth() - g)).texture(m - s, m + n);
                t += 1.0;
                s += 0.5f;
            }
        }
        if ((lv3 = lv2.endNullable()) != null) {
            BufferRenderer.drawWithGlobalProgram(lv3);
        }
        RenderSystem.enableCull();
        RenderSystem.polygonOffset(0.0f, 0.0f);
        RenderSystem.disablePolygonOffset();
        RenderSystem.disableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, 1.0f);
        RenderSystem.depthMask(true);
    }

    private void drawBlockOutline(MatrixStack matrices, VertexConsumer vertexConsumer, Entity entity, double cameraX, double cameraY, double cameraZ, BlockPos pos, BlockState state) {
        WorldRenderer.drawCuboidShapeOutline(matrices, vertexConsumer, state.getOutlineShape(this.world, pos, ShapeContext.of(entity)), (double)pos.getX() - cameraX, (double)pos.getY() - cameraY, (double)pos.getZ() - cameraZ, 0.0f, 0.0f, 0.0f, 0.4f);
    }

    private static Vec3d getMaxIntensityColor(float hue) {
        float g = 5.99999f;
        int i = (int)(MathHelper.clamp(hue, 0.0f, 1.0f) * 5.99999f);
        float h = hue * 5.99999f - (float)i;
        return switch (i) {
            case 0 -> new Vec3d(1.0, h, 0.0);
            case 1 -> new Vec3d(1.0f - h, 1.0, 0.0);
            case 2 -> new Vec3d(0.0, 1.0, h);
            case 3 -> new Vec3d(0.0, 1.0 - (double)h, 1.0);
            case 4 -> new Vec3d(h, 0.0, 1.0);
            case 5 -> new Vec3d(1.0, 0.0, 1.0 - (double)h);
            default -> throw new IllegalStateException("Unexpected value: " + i);
        };
    }

    private static Vec3d shiftHue(float red, float green, float blue, float hueOffset) {
        Vec3d lv = WorldRenderer.getMaxIntensityColor(hueOffset).multiply(red);
        Vec3d lv2 = WorldRenderer.getMaxIntensityColor((hueOffset + 0.33333334f) % 1.0f).multiply(green);
        Vec3d lv3 = WorldRenderer.getMaxIntensityColor((hueOffset + 0.6666667f) % 1.0f).multiply(blue);
        Vec3d lv4 = lv.add(lv2).add(lv3);
        double d = Math.max(Math.max(1.0, lv4.x), Math.max(lv4.y, lv4.z));
        return new Vec3d(lv4.x / d, lv4.y / d, lv4.z / d);
    }

    public static void drawShapeOutline(MatrixStack matrices, VertexConsumer vertexConsumer, VoxelShape shape, double offsetX, double offsetY, double offsetZ, float red, float green, float blue, float alpha, boolean colorize) {
        List<Box> list = shape.getBoundingBoxes();
        if (list.isEmpty()) {
            return;
        }
        int k = colorize ? list.size() : list.size() * 8;
        WorldRenderer.drawCuboidShapeOutline(matrices, vertexConsumer, VoxelShapes.cuboid(list.get(0)), offsetX, offsetY, offsetZ, red, green, blue, alpha);
        for (int l = 1; l < list.size(); ++l) {
            Box lv = list.get(l);
            float m = (float)l / (float)k;
            Vec3d lv2 = WorldRenderer.shiftHue(red, green, blue, m);
            WorldRenderer.drawCuboidShapeOutline(matrices, vertexConsumer, VoxelShapes.cuboid(lv), offsetX, offsetY, offsetZ, (float)lv2.x, (float)lv2.y, (float)lv2.z, alpha);
        }
    }

    private static void drawCuboidShapeOutline(MatrixStack matrices, VertexConsumer vertexConsumer, VoxelShape shape, double offsetX, double offsetY, double offsetZ, float red, float green, float blue, float alpha) {
        MatrixStack.Entry lv = matrices.peek();
        shape.forEachEdge((minX, minY, minZ, maxX, maxY, maxZ) -> {
            float q = (float)(maxX - minX);
            float r = (float)(maxY - minY);
            float s = (float)(maxZ - minZ);
            float t = MathHelper.sqrt(q * q + r * r + s * s);
            vertexConsumer.vertex(lv, (float)(minX + offsetX), (float)(minY + offsetY), (float)(minZ + offsetZ)).color(red, green, blue, alpha).normal(lv, q /= t, r /= t, s /= t);
            vertexConsumer.vertex(lv, (float)(maxX + offsetX), (float)(maxY + offsetY), (float)(maxZ + offsetZ)).color(red, green, blue, alpha).normal(lv, q, r, s);
        });
    }

    public static void drawBox(VertexConsumer vertexConsumer, double x1, double y1, double z1, double x2, double y2, double z2, float red, float green, float blue, float alpha) {
        WorldRenderer.drawBox(new MatrixStack(), vertexConsumer, x1, y1, z1, x2, y2, z2, red, green, blue, alpha, red, green, blue);
    }

    public static void drawBox(MatrixStack matrices, VertexConsumer vertexConsumer, Box box, float red, float green, float blue, float alpha) {
        WorldRenderer.drawBox(matrices, vertexConsumer, box.minX, box.minY, box.minZ, box.maxX, box.maxY, box.maxZ, red, green, blue, alpha, red, green, blue);
    }

    public static void drawBox(MatrixStack matrices, VertexConsumer vertexConsumer, double x1, double y1, double z1, double x2, double y2, double z2, float red, float green, float blue, float alpha) {
        WorldRenderer.drawBox(matrices, vertexConsumer, x1, y1, z1, x2, y2, z2, red, green, blue, alpha, red, green, blue);
    }

    public static void drawBox(MatrixStack matrices, VertexConsumer vertexConsumer, double x1, double y1, double z1, double x2, double y2, double z2, float red, float green, float blue, float alpha, float xAxisRed, float yAxisGreen, float zAxisBlue) {
        MatrixStack.Entry lv = matrices.peek();
        float q = (float)x1;
        float r = (float)y1;
        float s = (float)z1;
        float t = (float)x2;
        float u = (float)y2;
        float v = (float)z2;
        vertexConsumer.vertex(lv, q, r, s).color(red, yAxisGreen, zAxisBlue, alpha).normal(lv, 1.0f, 0.0f, 0.0f);
        vertexConsumer.vertex(lv, t, r, s).color(red, yAxisGreen, zAxisBlue, alpha).normal(lv, 1.0f, 0.0f, 0.0f);
        vertexConsumer.vertex(lv, q, r, s).color(xAxisRed, green, zAxisBlue, alpha).normal(lv, 0.0f, 1.0f, 0.0f);
        vertexConsumer.vertex(lv, q, u, s).color(xAxisRed, green, zAxisBlue, alpha).normal(lv, 0.0f, 1.0f, 0.0f);
        vertexConsumer.vertex(lv, q, r, s).color(xAxisRed, yAxisGreen, blue, alpha).normal(lv, 0.0f, 0.0f, 1.0f);
        vertexConsumer.vertex(lv, q, r, v).color(xAxisRed, yAxisGreen, blue, alpha).normal(lv, 0.0f, 0.0f, 1.0f);
        vertexConsumer.vertex(lv, t, r, s).color(red, green, blue, alpha).normal(lv, 0.0f, 1.0f, 0.0f);
        vertexConsumer.vertex(lv, t, u, s).color(red, green, blue, alpha).normal(lv, 0.0f, 1.0f, 0.0f);
        vertexConsumer.vertex(lv, t, u, s).color(red, green, blue, alpha).normal(lv, -1.0f, 0.0f, 0.0f);
        vertexConsumer.vertex(lv, q, u, s).color(red, green, blue, alpha).normal(lv, -1.0f, 0.0f, 0.0f);
        vertexConsumer.vertex(lv, q, u, s).color(red, green, blue, alpha).normal(lv, 0.0f, 0.0f, 1.0f);
        vertexConsumer.vertex(lv, q, u, v).color(red, green, blue, alpha).normal(lv, 0.0f, 0.0f, 1.0f);
        vertexConsumer.vertex(lv, q, u, v).color(red, green, blue, alpha).normal(lv, 0.0f, -1.0f, 0.0f);
        vertexConsumer.vertex(lv, q, r, v).color(red, green, blue, alpha).normal(lv, 0.0f, -1.0f, 0.0f);
        vertexConsumer.vertex(lv, q, r, v).color(red, green, blue, alpha).normal(lv, 1.0f, 0.0f, 0.0f);
        vertexConsumer.vertex(lv, t, r, v).color(red, green, blue, alpha).normal(lv, 1.0f, 0.0f, 0.0f);
        vertexConsumer.vertex(lv, t, r, v).color(red, green, blue, alpha).normal(lv, 0.0f, 0.0f, -1.0f);
        vertexConsumer.vertex(lv, t, r, s).color(red, green, blue, alpha).normal(lv, 0.0f, 0.0f, -1.0f);
        vertexConsumer.vertex(lv, q, u, v).color(red, green, blue, alpha).normal(lv, 1.0f, 0.0f, 0.0f);
        vertexConsumer.vertex(lv, t, u, v).color(red, green, blue, alpha).normal(lv, 1.0f, 0.0f, 0.0f);
        vertexConsumer.vertex(lv, t, r, v).color(red, green, blue, alpha).normal(lv, 0.0f, 1.0f, 0.0f);
        vertexConsumer.vertex(lv, t, u, v).color(red, green, blue, alpha).normal(lv, 0.0f, 1.0f, 0.0f);
        vertexConsumer.vertex(lv, t, u, s).color(red, green, blue, alpha).normal(lv, 0.0f, 0.0f, 1.0f);
        vertexConsumer.vertex(lv, t, u, v).color(red, green, blue, alpha).normal(lv, 0.0f, 0.0f, 1.0f);
    }

    public static void renderFilledBox(MatrixStack matrices, VertexConsumer vertexConsumer, double minX, double minY, double minZ, double maxX, double maxY, double maxZ, float red, float green, float blue, float alpha) {
        WorldRenderer.renderFilledBox(matrices, vertexConsumer, (float)minX, (float)minY, (float)minZ, (float)maxX, (float)maxY, (float)maxZ, red, green, blue, alpha);
    }

    public static void renderFilledBox(MatrixStack matrices, VertexConsumer vertexConsumer, float minX, float minY, float minZ, float maxX, float maxY, float maxZ, float red, float green, float blue, float alpha) {
        Matrix4f matrix4f = matrices.peek().getPositionMatrix();
        vertexConsumer.vertex(matrix4f, minX, minY, minZ).color(red, green, blue, alpha);
        vertexConsumer.vertex(matrix4f, minX, minY, minZ).color(red, green, blue, alpha);
        vertexConsumer.vertex(matrix4f, minX, minY, minZ).color(red, green, blue, alpha);
        vertexConsumer.vertex(matrix4f, minX, minY, maxZ).color(red, green, blue, alpha);
        vertexConsumer.vertex(matrix4f, minX, maxY, minZ).color(red, green, blue, alpha);
        vertexConsumer.vertex(matrix4f, minX, maxY, maxZ).color(red, green, blue, alpha);
        vertexConsumer.vertex(matrix4f, minX, maxY, maxZ).color(red, green, blue, alpha);
        vertexConsumer.vertex(matrix4f, minX, minY, maxZ).color(red, green, blue, alpha);
        vertexConsumer.vertex(matrix4f, maxX, maxY, maxZ).color(red, green, blue, alpha);
        vertexConsumer.vertex(matrix4f, maxX, minY, maxZ).color(red, green, blue, alpha);
        vertexConsumer.vertex(matrix4f, maxX, minY, maxZ).color(red, green, blue, alpha);
        vertexConsumer.vertex(matrix4f, maxX, minY, minZ).color(red, green, blue, alpha);
        vertexConsumer.vertex(matrix4f, maxX, maxY, maxZ).color(red, green, blue, alpha);
        vertexConsumer.vertex(matrix4f, maxX, maxY, minZ).color(red, green, blue, alpha);
        vertexConsumer.vertex(matrix4f, maxX, maxY, minZ).color(red, green, blue, alpha);
        vertexConsumer.vertex(matrix4f, maxX, minY, minZ).color(red, green, blue, alpha);
        vertexConsumer.vertex(matrix4f, minX, maxY, minZ).color(red, green, blue, alpha);
        vertexConsumer.vertex(matrix4f, minX, minY, minZ).color(red, green, blue, alpha);
        vertexConsumer.vertex(matrix4f, minX, minY, minZ).color(red, green, blue, alpha);
        vertexConsumer.vertex(matrix4f, maxX, minY, minZ).color(red, green, blue, alpha);
        vertexConsumer.vertex(matrix4f, minX, minY, maxZ).color(red, green, blue, alpha);
        vertexConsumer.vertex(matrix4f, maxX, minY, maxZ).color(red, green, blue, alpha);
        vertexConsumer.vertex(matrix4f, maxX, minY, maxZ).color(red, green, blue, alpha);
        vertexConsumer.vertex(matrix4f, minX, maxY, minZ).color(red, green, blue, alpha);
        vertexConsumer.vertex(matrix4f, minX, maxY, minZ).color(red, green, blue, alpha);
        vertexConsumer.vertex(matrix4f, minX, maxY, maxZ).color(red, green, blue, alpha);
        vertexConsumer.vertex(matrix4f, maxX, maxY, minZ).color(red, green, blue, alpha);
        vertexConsumer.vertex(matrix4f, maxX, maxY, maxZ).color(red, green, blue, alpha);
        vertexConsumer.vertex(matrix4f, maxX, maxY, maxZ).color(red, green, blue, alpha);
        vertexConsumer.vertex(matrix4f, maxX, maxY, maxZ).color(red, green, blue, alpha);
    }

    public static void renderFilledBoxFace(MatrixStack matrices, VertexConsumer vertexConsumer, Direction face, float minX, float minY, float minZ, float maxX, float maxY, float maxZ, float red, float green, float blue, float alpha) {
        Matrix4f matrix4f = matrices.peek().getPositionMatrix();
        switch (face) {
            case DOWN: {
                vertexConsumer.vertex(matrix4f, minX, minY, minZ).color(red, green, blue, alpha);
                vertexConsumer.vertex(matrix4f, maxX, minY, minZ).color(red, green, blue, alpha);
                vertexConsumer.vertex(matrix4f, maxX, minY, maxZ).color(red, green, blue, alpha);
                vertexConsumer.vertex(matrix4f, minX, minY, maxZ).color(red, green, blue, alpha);
                break;
            }
            case UP: {
                vertexConsumer.vertex(matrix4f, minX, maxY, minZ).color(red, green, blue, alpha);
                vertexConsumer.vertex(matrix4f, minX, maxY, maxZ).color(red, green, blue, alpha);
                vertexConsumer.vertex(matrix4f, maxX, maxY, maxZ).color(red, green, blue, alpha);
                vertexConsumer.vertex(matrix4f, maxX, maxY, minZ).color(red, green, blue, alpha);
                break;
            }
            case NORTH: {
                vertexConsumer.vertex(matrix4f, minX, minY, minZ).color(red, green, blue, alpha);
                vertexConsumer.vertex(matrix4f, minX, maxY, minZ).color(red, green, blue, alpha);
                vertexConsumer.vertex(matrix4f, maxX, maxY, minZ).color(red, green, blue, alpha);
                vertexConsumer.vertex(matrix4f, maxX, minY, minZ).color(red, green, blue, alpha);
                break;
            }
            case SOUTH: {
                vertexConsumer.vertex(matrix4f, minX, minY, maxZ).color(red, green, blue, alpha);
                vertexConsumer.vertex(matrix4f, maxX, minY, maxZ).color(red, green, blue, alpha);
                vertexConsumer.vertex(matrix4f, maxX, maxY, maxZ).color(red, green, blue, alpha);
                vertexConsumer.vertex(matrix4f, minX, maxY, maxZ).color(red, green, blue, alpha);
                break;
            }
            case WEST: {
                vertexConsumer.vertex(matrix4f, minX, minY, minZ).color(red, green, blue, alpha);
                vertexConsumer.vertex(matrix4f, minX, minY, maxZ).color(red, green, blue, alpha);
                vertexConsumer.vertex(matrix4f, minX, maxY, maxZ).color(red, green, blue, alpha);
                vertexConsumer.vertex(matrix4f, minX, maxY, minZ).color(red, green, blue, alpha);
                break;
            }
            case EAST: {
                vertexConsumer.vertex(matrix4f, maxX, minY, minZ).color(red, green, blue, alpha);
                vertexConsumer.vertex(matrix4f, maxX, maxY, minZ).color(red, green, blue, alpha);
                vertexConsumer.vertex(matrix4f, maxX, maxY, maxZ).color(red, green, blue, alpha);
                vertexConsumer.vertex(matrix4f, maxX, minY, maxZ).color(red, green, blue, alpha);
            }
        }
    }

    public void updateBlock(BlockView world, BlockPos pos, BlockState oldState, BlockState newState, int flags) {
        this.scheduleSectionRender(pos, (flags & 8) != 0);
    }

    private void scheduleSectionRender(BlockPos pos, boolean important) {
        for (int i = pos.getZ() - 1; i <= pos.getZ() + 1; ++i) {
            for (int j = pos.getX() - 1; j <= pos.getX() + 1; ++j) {
                for (int k = pos.getY() - 1; k <= pos.getY() + 1; ++k) {
                    this.scheduleChunkRender(ChunkSectionPos.getSectionCoord(j), ChunkSectionPos.getSectionCoord(k), ChunkSectionPos.getSectionCoord(i), important);
                }
            }
        }
    }

    public void scheduleBlockRenders(int minX, int minY, int minZ, int maxX, int maxY, int maxZ) {
        for (int o = minZ - 1; o <= maxZ + 1; ++o) {
            for (int p = minX - 1; p <= maxX + 1; ++p) {
                for (int q = minY - 1; q <= maxY + 1; ++q) {
                    this.scheduleBlockRender(ChunkSectionPos.getSectionCoord(p), ChunkSectionPos.getSectionCoord(q), ChunkSectionPos.getSectionCoord(o));
                }
            }
        }
    }

    public void scheduleBlockRerenderIfNeeded(BlockPos pos, BlockState old, BlockState updated) {
        if (this.client.getBakedModelManager().shouldRerender(old, updated)) {
            this.scheduleBlockRenders(pos.getX(), pos.getY(), pos.getZ(), pos.getX(), pos.getY(), pos.getZ());
        }
    }

    public void scheduleBlockRenders(int x, int y, int z) {
        for (int l = z - 1; l <= z + 1; ++l) {
            for (int m = x - 1; m <= x + 1; ++m) {
                for (int n = y - 1; n <= y + 1; ++n) {
                    this.scheduleBlockRender(m, n, l);
                }
            }
        }
    }

    public void scheduleBlockRender(int x, int y, int z) {
        this.scheduleChunkRender(x, y, z, false);
    }

    private void scheduleChunkRender(int x, int y, int z, boolean important) {
        this.chunks.scheduleRebuild(x, y, z, important);
    }

    public void playJukeboxSong(RegistryEntry<JukeboxSong> song, BlockPos jukeboxPos) {
        if (this.world == null) {
            return;
        }
        this.stopJukeboxSong(jukeboxPos);
        JukeboxSong lv = song.value();
        SoundEvent lv2 = lv.soundEvent().value();
        PositionedSoundInstance lv3 = PositionedSoundInstance.record(lv2, Vec3d.ofCenter(jukeboxPos));
        this.playingSongs.put(jukeboxPos, lv3);
        this.client.getSoundManager().play(lv3);
        this.client.inGameHud.setRecordPlayingOverlay(lv.description());
        this.updateEntitiesForSong(this.world, jukeboxPos, true);
    }

    private void stopJukeboxSong(BlockPos jukeboxPos) {
        SoundInstance lv = this.playingSongs.remove(jukeboxPos);
        if (lv != null) {
            this.client.getSoundManager().stop(lv);
        }
    }

    public void stopJukeboxSongAndUpdate(BlockPos jukeboxPos) {
        this.stopJukeboxSong(jukeboxPos);
        if (this.world != null) {
            this.updateEntitiesForSong(this.world, jukeboxPos, false);
        }
    }

    private void updateEntitiesForSong(World world, BlockPos pos, boolean playing) {
        List<LivingEntity> list = world.getNonSpectatingEntities(LivingEntity.class, new Box(pos).expand(3.0));
        for (LivingEntity lv : list) {
            lv.setNearbySongPlaying(pos, playing);
        }
    }

    public void addParticle(ParticleEffect parameters, boolean shouldAlwaysSpawn, double x, double y, double z, double velocityX, double velocityY, double velocityZ) {
        this.addParticle(parameters, shouldAlwaysSpawn, false, x, y, z, velocityX, velocityY, velocityZ);
    }

    public void addParticle(ParticleEffect parameters, boolean shouldAlwaysSpawn, boolean important, double x, double y, double z, double velocityX, double velocityY, double velocityZ) {
        try {
            this.spawnParticle(parameters, shouldAlwaysSpawn, important, x, y, z, velocityX, velocityY, velocityZ);
        } catch (Throwable throwable) {
            CrashReport lv = CrashReport.create(throwable, "Exception while adding particle");
            CrashReportSection lv2 = lv.addElement("Particle being added");
            lv2.add("ID", Registries.PARTICLE_TYPE.getId(parameters.getType()));
            lv2.add("Parameters", () -> ParticleTypes.TYPE_CODEC.encodeStart(this.world.getRegistryManager().getOps(NbtOps.INSTANCE), parameters).toString());
            lv2.add("Position", () -> CrashReportSection.createPositionString((HeightLimitView)this.world, x, y, z));
            throw new CrashException(lv);
        }
    }

    private <T extends ParticleEffect> void addParticle(T parameters, double x, double y, double z, double velocityX, double velocityY, double velocityZ) {
        this.addParticle(parameters, parameters.getType().shouldAlwaysSpawn(), x, y, z, velocityX, velocityY, velocityZ);
    }

    @Nullable
    private Particle spawnParticle(ParticleEffect parameters, boolean alwaysSpawn, double x, double y, double z, double velocityX, double velocityY, double velocityZ) {
        return this.spawnParticle(parameters, alwaysSpawn, false, x, y, z, velocityX, velocityY, velocityZ);
    }

    @Nullable
    private Particle spawnParticle(ParticleEffect parameters, boolean alwaysSpawn, boolean canSpawnOnMinimal, double x, double y, double z, double velocityX, double velocityY, double velocityZ) {
        Camera lv = this.client.gameRenderer.getCamera();
        ParticlesMode lv2 = this.getRandomParticleSpawnChance(canSpawnOnMinimal);
        if (alwaysSpawn) {
            return this.client.particleManager.addParticle(parameters, x, y, z, velocityX, velocityY, velocityZ);
        }
        if (lv.getPos().squaredDistanceTo(x, y, z) > 1024.0) {
            return null;
        }
        if (lv2 == ParticlesMode.MINIMAL) {
            return null;
        }
        return this.client.particleManager.addParticle(parameters, x, y, z, velocityX, velocityY, velocityZ);
    }

    private ParticlesMode getRandomParticleSpawnChance(boolean canSpawnOnMinimal) {
        ParticlesMode lv = this.client.options.getParticles().getValue();
        if (canSpawnOnMinimal && lv == ParticlesMode.MINIMAL && this.world.random.nextInt(10) == 0) {
            lv = ParticlesMode.DECREASED;
        }
        if (lv == ParticlesMode.DECREASED && this.world.random.nextInt(3) == 0) {
            lv = ParticlesMode.MINIMAL;
        }
        return lv;
    }

    public void cleanUp() {
    }

    public void processGlobalEvent(int eventId, BlockPos pos, int data) {
        switch (eventId) {
            case 1023: 
            case 1028: 
            case 1038: {
                Camera lv = this.client.gameRenderer.getCamera();
                if (!lv.isReady()) break;
                double d = (double)pos.getX() - lv.getPos().x;
                double e = (double)pos.getY() - lv.getPos().y;
                double f = (double)pos.getZ() - lv.getPos().z;
                double g = Math.sqrt(d * d + e * e + f * f);
                double h = lv.getPos().x;
                double k = lv.getPos().y;
                double l = lv.getPos().z;
                if (g > 0.0) {
                    h += d / g * 2.0;
                    k += e / g * 2.0;
                    l += f / g * 2.0;
                }
                if (eventId == WorldEvents.WITHER_SPAWNS) {
                    this.world.playSound(h, k, l, SoundEvents.ENTITY_WITHER_SPAWN, SoundCategory.HOSTILE, 1.0f, 1.0f, false);
                    break;
                }
                if (eventId == WorldEvents.END_PORTAL_OPENED) {
                    this.world.playSound(h, k, l, SoundEvents.BLOCK_END_PORTAL_SPAWN, SoundCategory.HOSTILE, 1.0f, 1.0f, false);
                    break;
                }
                this.world.playSound(h, k, l, SoundEvents.ENTITY_ENDER_DRAGON_DEATH, SoundCategory.HOSTILE, 5.0f, 1.0f, false);
            }
        }
    }

    public void processWorldEvent(int eventId, BlockPos pos, int data) {
        Random lv = this.world.random;
        switch (eventId) {
            case 1035: {
                this.world.playSoundAtBlockCenter(pos, SoundEvents.BLOCK_BREWING_STAND_BREW, SoundCategory.BLOCKS, 1.0f, 1.0f, false);
                break;
            }
            case 1033: {
                this.world.playSoundAtBlockCenter(pos, SoundEvents.BLOCK_CHORUS_FLOWER_GROW, SoundCategory.BLOCKS, 1.0f, 1.0f, false);
                break;
            }
            case 1034: {
                this.world.playSoundAtBlockCenter(pos, SoundEvents.BLOCK_CHORUS_FLOWER_DEATH, SoundCategory.BLOCKS, 1.0f, 1.0f, false);
                break;
            }
            case 1032: {
                this.client.getSoundManager().play(PositionedSoundInstance.ambient(SoundEvents.BLOCK_PORTAL_TRAVEL, lv.nextFloat() * 0.4f + 0.8f, 0.25f));
                break;
            }
            case 1001: {
                this.world.playSoundAtBlockCenter(pos, SoundEvents.BLOCK_DISPENSER_FAIL, SoundCategory.BLOCKS, 1.0f, 1.2f, false);
                break;
            }
            case 1000: {
                this.world.playSoundAtBlockCenter(pos, SoundEvents.BLOCK_DISPENSER_DISPENSE, SoundCategory.BLOCKS, 1.0f, 1.0f, false);
                break;
            }
            case 1049: {
                this.world.playSoundAtBlockCenter(pos, SoundEvents.BLOCK_CRAFTER_CRAFT, SoundCategory.BLOCKS, 1.0f, 1.0f, false);
                break;
            }
            case 1050: {
                this.world.playSoundAtBlockCenter(pos, SoundEvents.BLOCK_CRAFTER_FAIL, SoundCategory.BLOCKS, 1.0f, 1.0f, false);
                break;
            }
            case 1004: {
                this.world.playSoundAtBlockCenter(pos, SoundEvents.ENTITY_FIREWORK_ROCKET_SHOOT, SoundCategory.NEUTRAL, 1.0f, 1.2f, false);
                break;
            }
            case 1002: {
                this.world.playSoundAtBlockCenter(pos, SoundEvents.BLOCK_DISPENSER_LAUNCH, SoundCategory.BLOCKS, 1.0f, 1.2f, false);
                break;
            }
            case 1051: {
                this.world.playSoundAtBlockCenter(pos, SoundEvents.ENTITY_WIND_CHARGE_THROW, SoundCategory.BLOCKS, 0.5f, 0.4f / (this.world.getRandom().nextFloat() * 0.4f + 0.8f), false);
            }
            case 2010: {
                this.shootParticles(data, pos, lv, ParticleTypes.WHITE_SMOKE);
                break;
            }
            case 2000: {
                this.shootParticles(data, pos, lv, ParticleTypes.SMOKE);
                break;
            }
            case 2003: {
                double d = (double)pos.getX() + 0.5;
                double e = pos.getY();
                double f = (double)pos.getZ() + 0.5;
                for (int k = 0; k < 8; ++k) {
                    this.addParticle(new ItemStackParticleEffect(ParticleTypes.ITEM, new ItemStack(Items.ENDER_EYE)), d, e, f, lv.nextGaussian() * 0.15, lv.nextDouble() * 0.2, lv.nextGaussian() * 0.15);
                }
                for (double g = 0.0; g < Math.PI * 2; g += 0.15707963267948966) {
                    this.addParticle(ParticleTypes.PORTAL, d + Math.cos(g) * 5.0, e - 0.4, f + Math.sin(g) * 5.0, Math.cos(g) * -5.0, 0.0, Math.sin(g) * -5.0);
                    this.addParticle(ParticleTypes.PORTAL, d + Math.cos(g) * 5.0, e - 0.4, f + Math.sin(g) * 5.0, Math.cos(g) * -7.0, 0.0, Math.sin(g) * -7.0);
                }
                break;
            }
            case 2002: 
            case 2007: {
                Vec3d lv2 = Vec3d.ofBottomCenter(pos);
                for (int l = 0; l < 8; ++l) {
                    this.addParticle(new ItemStackParticleEffect(ParticleTypes.ITEM, new ItemStack(Items.SPLASH_POTION)), lv2.x, lv2.y, lv2.z, lv.nextGaussian() * 0.15, lv.nextDouble() * 0.2, lv.nextGaussian() * 0.15);
                }
                float h = (float)(data >> 16 & 0xFF) / 255.0f;
                float m = (float)(data >> 8 & 0xFF) / 255.0f;
                float n = (float)(data >> 0 & 0xFF) / 255.0f;
                SimpleParticleType lv3 = eventId == 2007 ? ParticleTypes.INSTANT_EFFECT : ParticleTypes.EFFECT;
                for (int o = 0; o < 100; ++o) {
                    double g = lv.nextDouble() * 4.0;
                    double p = lv.nextDouble() * Math.PI * 2.0;
                    double q = Math.cos(p) * g;
                    double r = 0.01 + lv.nextDouble() * 0.5;
                    double s = Math.sin(p) * g;
                    Particle lv4 = this.spawnParticle(lv3, lv3.getType().shouldAlwaysSpawn(), lv2.x + q * 0.1, lv2.y + 0.3, lv2.z + s * 0.1, q, r, s);
                    if (lv4 == null) continue;
                    float t = 0.75f + lv.nextFloat() * 0.25f;
                    lv4.setColor(h * t, m * t, n * t);
                    lv4.move((float)g);
                }
                this.world.playSoundAtBlockCenter(pos, SoundEvents.ENTITY_SPLASH_POTION_BREAK, SoundCategory.NEUTRAL, 1.0f, lv.nextFloat() * 0.1f + 0.9f, false);
                break;
            }
            case 2001: {
                BlockState lv5 = Block.getStateFromRawId(data);
                if (!lv5.isAir()) {
                    BlockSoundGroup lv6 = lv5.getSoundGroup();
                    this.world.playSoundAtBlockCenter(pos, lv6.getBreakSound(), SoundCategory.BLOCKS, (lv6.getVolume() + 1.0f) / 2.0f, lv6.getPitch() * 0.8f, false);
                }
                this.world.addBlockBreakParticles(pos, lv5);
                break;
            }
            case 3008: {
                BlockState lv7 = Block.getStateFromRawId(data);
                Block n = lv7.getBlock();
                if (n instanceof BrushableBlock) {
                    BrushableBlock lv8 = (BrushableBlock)n;
                    this.world.playSoundAtBlockCenter(pos, lv8.getBrushingCompleteSound(), SoundCategory.PLAYERS, 1.0f, 1.0f, false);
                }
                this.world.addBlockBreakParticles(pos, lv7);
                break;
            }
            case 2004: {
                for (int u = 0; u < 20; ++u) {
                    double v = (double)pos.getX() + 0.5 + (lv.nextDouble() - 0.5) * 2.0;
                    double w = (double)pos.getY() + 0.5 + (lv.nextDouble() - 0.5) * 2.0;
                    double x = (double)pos.getZ() + 0.5 + (lv.nextDouble() - 0.5) * 2.0;
                    this.world.addParticle(ParticleTypes.SMOKE, v, w, x, 0.0, 0.0, 0.0);
                    this.world.addParticle(ParticleTypes.FLAME, v, w, x, 0.0, 0.0, 0.0);
                }
                break;
            }
            case 3011: {
                TrialSpawnerLogic.addMobSpawnParticles(this.world, pos, lv, TrialSpawnerLogic.Type.fromIndex((int)data).particle);
                break;
            }
            case 3012: {
                this.world.playSoundAtBlockCenter(pos, SoundEvents.BLOCK_TRIAL_SPAWNER_SPAWN_MOB, SoundCategory.BLOCKS, 1.0f, (lv.nextFloat() - lv.nextFloat()) * 0.2f + 1.0f, true);
                TrialSpawnerLogic.addMobSpawnParticles(this.world, pos, lv, TrialSpawnerLogic.Type.fromIndex((int)data).particle);
                break;
            }
            case 3021: {
                this.world.playSoundAtBlockCenter(pos, SoundEvents.BLOCK_TRIAL_SPAWNER_SPAWN_ITEM, SoundCategory.BLOCKS, 1.0f, (lv.nextFloat() - lv.nextFloat()) * 0.2f + 1.0f, true);
                TrialSpawnerLogic.addMobSpawnParticles(this.world, pos, lv, TrialSpawnerLogic.Type.fromIndex((int)data).particle);
                break;
            }
            case 3013: {
                this.world.playSoundAtBlockCenter(pos, SoundEvents.BLOCK_TRIAL_SPAWNER_DETECT_PLAYER, SoundCategory.BLOCKS, 1.0f, (lv.nextFloat() - lv.nextFloat()) * 0.2f + 1.0f, true);
                TrialSpawnerLogic.addDetectionParticles(this.world, pos, lv, data, ParticleTypes.TRIAL_SPAWNER_DETECTION);
                break;
            }
            case 3019: {
                this.world.playSoundAtBlockCenter(pos, SoundEvents.BLOCK_TRIAL_SPAWNER_DETECT_PLAYER, SoundCategory.BLOCKS, 1.0f, (lv.nextFloat() - lv.nextFloat()) * 0.2f + 1.0f, true);
                TrialSpawnerLogic.addDetectionParticles(this.world, pos, lv, data, ParticleTypes.TRIAL_SPAWNER_DETECTION_OMINOUS);
                break;
            }
            case 3020: {
                this.world.playSoundAtBlockCenter(pos, SoundEvents.BLOCK_TRIAL_SPAWNER_OMINOUS_ACTIVATE, SoundCategory.BLOCKS, data == 0 ? 0.3f : 1.0f, (lv.nextFloat() - lv.nextFloat()) * 0.2f + 1.0f, true);
                TrialSpawnerLogic.addDetectionParticles(this.world, pos, lv, 0, ParticleTypes.TRIAL_SPAWNER_DETECTION_OMINOUS);
                TrialSpawnerLogic.addTrialOmenParticles(this.world, pos, lv);
                break;
            }
            case 3014: {
                this.world.playSoundAtBlockCenter(pos, SoundEvents.BLOCK_TRIAL_SPAWNER_EJECT_ITEM, SoundCategory.BLOCKS, 1.0f, (lv.nextFloat() - lv.nextFloat()) * 0.2f + 1.0f, true);
                TrialSpawnerLogic.addEjectItemParticles(this.world, pos, lv);
                break;
            }
            case 3017: {
                TrialSpawnerLogic.addEjectItemParticles(this.world, pos, lv);
                break;
            }
            case 3015: {
                BlockEntity v = this.world.getBlockEntity(pos);
                if (!(v instanceof VaultBlockEntity)) break;
                VaultBlockEntity lv9 = (VaultBlockEntity)v;
                VaultBlockEntity.Client.spawnActivateParticles(this.world, lv9.getPos(), lv9.getCachedState(), lv9.getSharedData(), data == 0 ? ParticleTypes.SMALL_FLAME : ParticleTypes.SOUL_FIRE_FLAME);
                this.world.playSoundAtBlockCenter(pos, SoundEvents.BLOCK_VAULT_ACTIVATE, SoundCategory.BLOCKS, 1.0f, (lv.nextFloat() - lv.nextFloat()) * 0.2f + 1.0f, true);
                break;
            }
            case 3016: {
                VaultBlockEntity.Client.spawnDeactivateParticles(this.world, pos, data == 0 ? ParticleTypes.SMALL_FLAME : ParticleTypes.SOUL_FIRE_FLAME);
                this.world.playSoundAtBlockCenter(pos, SoundEvents.BLOCK_VAULT_DEACTIVATE, SoundCategory.BLOCKS, 1.0f, (lv.nextFloat() - lv.nextFloat()) * 0.2f + 1.0f, true);
                break;
            }
            case 3018: {
                for (int u = 0; u < 10; ++u) {
                    double v = lv.nextGaussian() * 0.02;
                    double w = lv.nextGaussian() * 0.02;
                    double x = lv.nextGaussian() * 0.02;
                    this.world.addParticle(ParticleTypes.POOF, (double)pos.getX() + lv.nextDouble(), (double)pos.getY() + lv.nextDouble(), (double)pos.getZ() + lv.nextDouble(), v, w, x);
                }
                this.world.playSoundAtBlockCenter(pos, SoundEvents.BLOCK_COBWEB_PLACE, SoundCategory.BLOCKS, 1.0f, (lv.nextFloat() - lv.nextFloat()) * 0.2f + 1.0f, true);
                break;
            }
            case 1505: {
                BoneMealItem.createParticles(this.world, pos, data);
                this.world.playSoundAtBlockCenter(pos, SoundEvents.ITEM_BONE_MEAL_USE, SoundCategory.BLOCKS, 1.0f, 1.0f, false);
                break;
            }
            case 2011: {
                ParticleUtil.spawnParticlesAround(this.world, pos, data, ParticleTypes.HAPPY_VILLAGER);
                break;
            }
            case 2012: {
                ParticleUtil.spawnParticlesAround(this.world, pos, data, ParticleTypes.HAPPY_VILLAGER);
                break;
            }
            case 3009: {
                ParticleUtil.spawnParticle((World)this.world, pos, ParticleTypes.EGG_CRACK, UniformIntProvider.create(3, 6));
                break;
            }
            case 3002: {
                if (data >= 0 && data < Direction.Axis.VALUES.length) {
                    ParticleUtil.spawnParticle(Direction.Axis.VALUES[data], this.world, pos, 0.125, ParticleTypes.ELECTRIC_SPARK, UniformIntProvider.create(10, 19));
                    break;
                }
                ParticleUtil.spawnParticle((World)this.world, pos, ParticleTypes.ELECTRIC_SPARK, UniformIntProvider.create(3, 5));
                break;
            }
            case 2013: {
                ParticleUtil.spawnSmashAttackParticles(this.world, pos, data);
                break;
            }
            case 3006: {
                int u = data >> 6;
                if (u > 0) {
                    if (lv.nextFloat() < 0.3f + (float)u * 0.1f) {
                        float n = 0.15f + 0.02f * (float)u * (float)u * lv.nextFloat();
                        float y = 0.4f + 0.3f * (float)u * lv.nextFloat();
                        this.world.playSoundAtBlockCenter(pos, SoundEvents.BLOCK_SCULK_CHARGE, SoundCategory.BLOCKS, n, y, false);
                    }
                    byte b = (byte)(data & 0x3F);
                    UniformIntProvider lv10 = UniformIntProvider.create(0, u);
                    float z = 0.005f;
                    Supplier<Vec3d> supplier = () -> new Vec3d(MathHelper.nextDouble(lv, -0.005f, 0.005f), MathHelper.nextDouble(lv, -0.005f, 0.005f), MathHelper.nextDouble(lv, -0.005f, 0.005f));
                    if (b == 0) {
                        for (Direction lv11 : Direction.values()) {
                            float aa = lv11 == Direction.DOWN ? (float)Math.PI : 0.0f;
                            double r = lv11.getAxis() == Direction.Axis.Y ? 0.65 : 0.57;
                            ParticleUtil.spawnParticles(this.world, pos, new SculkChargeParticleEffect(aa), lv10, lv11, supplier, r);
                        }
                    } else {
                        for (Direction lv12 : MultifaceGrowthBlock.flagToDirections(b)) {
                            float ab = lv12 == Direction.UP ? (float)Math.PI : 0.0f;
                            double q = 0.35;
                            ParticleUtil.spawnParticles(this.world, pos, new SculkChargeParticleEffect(ab), lv10, lv12, supplier, 0.35);
                        }
                    }
                } else {
                    this.world.playSoundAtBlockCenter(pos, SoundEvents.BLOCK_SCULK_CHARGE, SoundCategory.BLOCKS, 1.0f, 1.0f, false);
                    boolean bl = this.world.getBlockState(pos).isFullCube(this.world, pos);
                    int ac = bl ? 40 : 20;
                    float z = bl ? 0.45f : 0.25f;
                    float ad = 0.07f;
                    for (int ae = 0; ae < ac; ++ae) {
                        float af = 2.0f * lv.nextFloat() - 1.0f;
                        float ab = 2.0f * lv.nextFloat() - 1.0f;
                        float ag = 2.0f * lv.nextFloat() - 1.0f;
                        this.world.addParticle(ParticleTypes.SCULK_CHARGE_POP, (double)pos.getX() + 0.5 + (double)(af * z), (double)pos.getY() + 0.5 + (double)(ab * z), (double)pos.getZ() + 0.5 + (double)(ag * z), af * 0.07f, ab * 0.07f, ag * 0.07f);
                    }
                }
                break;
            }
            case 3007: {
                boolean bl2;
                for (int ah = 0; ah < 10; ++ah) {
                    this.world.addParticle(new ShriekParticleEffect(ah * 5), false, (double)pos.getX() + 0.5, (double)pos.getY() + SculkShriekerBlock.TOP, (double)pos.getZ() + 0.5, 0.0, 0.0, 0.0);
                }
                BlockState lv13 = this.world.getBlockState(pos);
                boolean bl = bl2 = lv13.contains(Properties.WATERLOGGED) && lv13.get(Properties.WATERLOGGED) != false;
                if (bl2) break;
                this.world.playSound((double)pos.getX() + 0.5, (double)pos.getY() + SculkShriekerBlock.TOP, (double)pos.getZ() + 0.5, SoundEvents.BLOCK_SCULK_SHRIEKER_SHRIEK, SoundCategory.BLOCKS, 2.0f, 0.6f + this.world.random.nextFloat() * 0.4f, false);
                break;
            }
            case 3003: {
                ParticleUtil.spawnParticle((World)this.world, pos, ParticleTypes.WAX_ON, UniformIntProvider.create(3, 5));
                this.world.playSoundAtBlockCenter(pos, SoundEvents.ITEM_HONEYCOMB_WAX_ON, SoundCategory.BLOCKS, 1.0f, 1.0f, false);
                break;
            }
            case 3004: {
                ParticleUtil.spawnParticle((World)this.world, pos, ParticleTypes.WAX_OFF, UniformIntProvider.create(3, 5));
                break;
            }
            case 3005: {
                ParticleUtil.spawnParticle((World)this.world, pos, ParticleTypes.SCRAPE, UniformIntProvider.create(3, 5));
                break;
            }
            case 2008: {
                this.world.addParticle(ParticleTypes.EXPLOSION, (double)pos.getX() + 0.5, (double)pos.getY() + 0.5, (double)pos.getZ() + 0.5, 0.0, 0.0, 0.0);
                break;
            }
            case 1500: {
                ComposterBlock.playEffects(this.world, pos, data > 0);
                break;
            }
            case 1504: {
                PointedDripstoneBlock.createParticle(this.world, pos, this.world.getBlockState(pos));
                break;
            }
            case 1501: {
                this.world.playSoundAtBlockCenter(pos, SoundEvents.BLOCK_LAVA_EXTINGUISH, SoundCategory.BLOCKS, 0.5f, 2.6f + (lv.nextFloat() - lv.nextFloat()) * 0.8f, false);
                for (int o = 0; o < 8; ++o) {
                    this.world.addParticle(ParticleTypes.LARGE_SMOKE, (double)pos.getX() + lv.nextDouble(), (double)pos.getY() + 1.2, (double)pos.getZ() + lv.nextDouble(), 0.0, 0.0, 0.0);
                }
                break;
            }
            case 1502: {
                this.world.playSoundAtBlockCenter(pos, SoundEvents.BLOCK_REDSTONE_TORCH_BURNOUT, SoundCategory.BLOCKS, 0.5f, 2.6f + (lv.nextFloat() - lv.nextFloat()) * 0.8f, false);
                for (int o = 0; o < 5; ++o) {
                    double g = (double)pos.getX() + lv.nextDouble() * 0.6 + 0.2;
                    double p = (double)pos.getY() + lv.nextDouble() * 0.6 + 0.2;
                    double q = (double)pos.getZ() + lv.nextDouble() * 0.6 + 0.2;
                    this.world.addParticle(ParticleTypes.SMOKE, g, p, q, 0.0, 0.0, 0.0);
                }
                break;
            }
            case 1503: {
                this.world.playSoundAtBlockCenter(pos, SoundEvents.BLOCK_END_PORTAL_FRAME_FILL, SoundCategory.BLOCKS, 1.0f, 1.0f, false);
                for (int o = 0; o < 16; ++o) {
                    double g = (double)pos.getX() + (5.0 + lv.nextDouble() * 6.0) / 16.0;
                    double p = (double)pos.getY() + 0.8125;
                    double q = (double)pos.getZ() + (5.0 + lv.nextDouble() * 6.0) / 16.0;
                    this.world.addParticle(ParticleTypes.SMOKE, g, p, q, 0.0, 0.0, 0.0);
                }
                break;
            }
            case 2006: {
                for (int o = 0; o < 200; ++o) {
                    float ad = lv.nextFloat() * 4.0f;
                    float ai = lv.nextFloat() * ((float)Math.PI * 2);
                    double p = MathHelper.cos(ai) * ad;
                    double q = 0.01 + lv.nextDouble() * 0.5;
                    double r = MathHelper.sin(ai) * ad;
                    Particle lv14 = this.spawnParticle(ParticleTypes.DRAGON_BREATH, false, (double)pos.getX() + p * 0.1, (double)pos.getY() + 0.3, (double)pos.getZ() + r * 0.1, p, q, r);
                    if (lv14 == null) continue;
                    lv14.move(ad);
                }
                if (data != 1) break;
                this.world.playSoundAtBlockCenter(pos, SoundEvents.ENTITY_DRAGON_FIREBALL_EXPLODE, SoundCategory.HOSTILE, 1.0f, lv.nextFloat() * 0.1f + 0.9f, false);
                break;
            }
            case 2009: {
                for (int o = 0; o < 8; ++o) {
                    this.world.addParticle(ParticleTypes.CLOUD, (double)pos.getX() + lv.nextDouble(), (double)pos.getY() + 1.2, (double)pos.getZ() + lv.nextDouble(), 0.0, 0.0, 0.0);
                }
                break;
            }
            case 1009: {
                if (data == 0) {
                    this.world.playSoundAtBlockCenter(pos, SoundEvents.BLOCK_FIRE_EXTINGUISH, SoundCategory.BLOCKS, 0.5f, 2.6f + (lv.nextFloat() - lv.nextFloat()) * 0.8f, false);
                    break;
                }
                if (data != 1) break;
                this.world.playSoundAtBlockCenter(pos, SoundEvents.ENTITY_GENERIC_EXTINGUISH_FIRE, SoundCategory.BLOCKS, 0.7f, 1.6f + (lv.nextFloat() - lv.nextFloat()) * 0.4f, false);
                break;
            }
            case 1029: {
                this.world.playSoundAtBlockCenter(pos, SoundEvents.BLOCK_ANVIL_DESTROY, SoundCategory.BLOCKS, 1.0f, lv.nextFloat() * 0.1f + 0.9f, false);
                break;
            }
            case 1030: {
                this.world.playSoundAtBlockCenter(pos, SoundEvents.BLOCK_ANVIL_USE, SoundCategory.BLOCKS, 1.0f, lv.nextFloat() * 0.1f + 0.9f, false);
                break;
            }
            case 1044: {
                this.world.playSoundAtBlockCenter(pos, SoundEvents.BLOCK_SMITHING_TABLE_USE, SoundCategory.BLOCKS, 1.0f, this.world.random.nextFloat() * 0.1f + 0.9f, false);
                break;
            }
            case 1031: {
                this.world.playSoundAtBlockCenter(pos, SoundEvents.BLOCK_ANVIL_LAND, SoundCategory.BLOCKS, 0.3f, this.world.random.nextFloat() * 0.1f + 0.9f, false);
                break;
            }
            case 1039: {
                this.world.playSoundAtBlockCenter(pos, SoundEvents.ENTITY_PHANTOM_BITE, SoundCategory.HOSTILE, 0.3f, this.world.random.nextFloat() * 0.1f + 0.9f, false);
                break;
            }
            case 1010: {
                this.world.getRegistryManager().get(RegistryKeys.JUKEBOX_SONG).getEntry(data).ifPresent(arg2 -> this.playJukeboxSong((RegistryEntry<JukeboxSong>)arg2, pos));
                break;
            }
            case 1011: {
                this.stopJukeboxSongAndUpdate(pos);
                break;
            }
            case 1015: {
                this.world.playSoundAtBlockCenter(pos, SoundEvents.ENTITY_GHAST_WARN, SoundCategory.HOSTILE, 10.0f, (lv.nextFloat() - lv.nextFloat()) * 0.2f + 1.0f, false);
                break;
            }
            case 1017: {
                this.world.playSoundAtBlockCenter(pos, SoundEvents.ENTITY_ENDER_DRAGON_SHOOT, SoundCategory.HOSTILE, 10.0f, (lv.nextFloat() - lv.nextFloat()) * 0.2f + 1.0f, false);
                break;
            }
            case 1016: {
                this.world.playSoundAtBlockCenter(pos, SoundEvents.ENTITY_GHAST_SHOOT, SoundCategory.HOSTILE, 10.0f, (lv.nextFloat() - lv.nextFloat()) * 0.2f + 1.0f, false);
                break;
            }
            case 1019: {
                this.world.playSoundAtBlockCenter(pos, SoundEvents.ENTITY_ZOMBIE_ATTACK_WOODEN_DOOR, SoundCategory.HOSTILE, 2.0f, (lv.nextFloat() - lv.nextFloat()) * 0.2f + 1.0f, false);
                break;
            }
            case 1022: {
                this.world.playSoundAtBlockCenter(pos, SoundEvents.ENTITY_WITHER_BREAK_BLOCK, SoundCategory.HOSTILE, 2.0f, (lv.nextFloat() - lv.nextFloat()) * 0.2f + 1.0f, false);
                break;
            }
            case 1021: {
                this.world.playSoundAtBlockCenter(pos, SoundEvents.ENTITY_ZOMBIE_BREAK_WOODEN_DOOR, SoundCategory.HOSTILE, 2.0f, (lv.nextFloat() - lv.nextFloat()) * 0.2f + 1.0f, false);
                break;
            }
            case 1020: {
                this.world.playSoundAtBlockCenter(pos, SoundEvents.ENTITY_ZOMBIE_ATTACK_IRON_DOOR, SoundCategory.HOSTILE, 2.0f, (lv.nextFloat() - lv.nextFloat()) * 0.2f + 1.0f, false);
                break;
            }
            case 1018: {
                this.world.playSoundAtBlockCenter(pos, SoundEvents.ENTITY_BLAZE_SHOOT, SoundCategory.HOSTILE, 2.0f, (lv.nextFloat() - lv.nextFloat()) * 0.2f + 1.0f, false);
                break;
            }
            case 1024: {
                this.world.playSoundAtBlockCenter(pos, SoundEvents.ENTITY_WITHER_SHOOT, SoundCategory.HOSTILE, 2.0f, (lv.nextFloat() - lv.nextFloat()) * 0.2f + 1.0f, false);
                break;
            }
            case 1026: {
                this.world.playSoundAtBlockCenter(pos, SoundEvents.ENTITY_ZOMBIE_INFECT, SoundCategory.HOSTILE, 2.0f, (lv.nextFloat() - lv.nextFloat()) * 0.2f + 1.0f, false);
                break;
            }
            case 1027: {
                this.world.playSoundAtBlockCenter(pos, SoundEvents.ENTITY_ZOMBIE_VILLAGER_CONVERTED, SoundCategory.HOSTILE, 2.0f, (lv.nextFloat() - lv.nextFloat()) * 0.2f + 1.0f, false);
                break;
            }
            case 1040: {
                this.world.playSoundAtBlockCenter(pos, SoundEvents.ENTITY_ZOMBIE_CONVERTED_TO_DROWNED, SoundCategory.HOSTILE, 2.0f, (lv.nextFloat() - lv.nextFloat()) * 0.2f + 1.0f, false);
                break;
            }
            case 1041: {
                this.world.playSoundAtBlockCenter(pos, SoundEvents.ENTITY_HUSK_CONVERTED_TO_ZOMBIE, SoundCategory.HOSTILE, 2.0f, (lv.nextFloat() - lv.nextFloat()) * 0.2f + 1.0f, false);
                break;
            }
            case 1025: {
                this.world.playSoundAtBlockCenter(pos, SoundEvents.ENTITY_BAT_TAKEOFF, SoundCategory.NEUTRAL, 0.05f, (lv.nextFloat() - lv.nextFloat()) * 0.2f + 1.0f, false);
                break;
            }
            case 1042: {
                this.world.playSoundAtBlockCenter(pos, SoundEvents.BLOCK_GRINDSTONE_USE, SoundCategory.BLOCKS, 1.0f, this.world.random.nextFloat() * 0.1f + 0.9f, false);
                break;
            }
            case 1043: {
                this.world.playSoundAtBlockCenter(pos, SoundEvents.ITEM_BOOK_PAGE_TURN, SoundCategory.BLOCKS, 1.0f, this.world.random.nextFloat() * 0.1f + 0.9f, false);
                break;
            }
            case 3000: {
                this.world.addParticle(ParticleTypes.EXPLOSION_EMITTER, true, (double)pos.getX() + 0.5, (double)pos.getY() + 0.5, (double)pos.getZ() + 0.5, 0.0, 0.0, 0.0);
                this.world.playSoundAtBlockCenter(pos, SoundEvents.BLOCK_END_GATEWAY_SPAWN, SoundCategory.BLOCKS, 10.0f, (1.0f + (this.world.random.nextFloat() - this.world.random.nextFloat()) * 0.2f) * 0.7f, false);
                break;
            }
            case 3001: {
                this.world.playSoundAtBlockCenter(pos, SoundEvents.ENTITY_ENDER_DRAGON_GROWL, SoundCategory.HOSTILE, 64.0f, 0.8f + this.world.random.nextFloat() * 0.3f, false);
                break;
            }
            case 1045: {
                this.world.playSoundAtBlockCenter(pos, SoundEvents.BLOCK_POINTED_DRIPSTONE_LAND, SoundCategory.BLOCKS, 2.0f, this.world.random.nextFloat() * 0.1f + 0.9f, false);
                break;
            }
            case 1046: {
                this.world.playSoundAtBlockCenter(pos, SoundEvents.BLOCK_POINTED_DRIPSTONE_DRIP_LAVA_INTO_CAULDRON, SoundCategory.BLOCKS, 2.0f, this.world.random.nextFloat() * 0.1f + 0.9f, false);
                break;
            }
            case 1047: {
                this.world.playSoundAtBlockCenter(pos, SoundEvents.BLOCK_POINTED_DRIPSTONE_DRIP_WATER_INTO_CAULDRON, SoundCategory.BLOCKS, 2.0f, this.world.random.nextFloat() * 0.1f + 0.9f, false);
                break;
            }
            case 1048: {
                this.world.playSoundAtBlockCenter(pos, SoundEvents.ENTITY_SKELETON_CONVERTED_TO_STRAY, SoundCategory.HOSTILE, 2.0f, (lv.nextFloat() - lv.nextFloat()) * 0.2f + 1.0f, false);
            }
        }
    }

    public void setBlockBreakingInfo(int entityId, BlockPos pos, int stage) {
        if (stage < 0 || stage >= 10) {
            BlockBreakingInfo lv = (BlockBreakingInfo)this.blockBreakingInfos.remove(entityId);
            if (lv != null) {
                this.removeBlockBreakingInfo(lv);
            }
        } else {
            BlockBreakingInfo lv = (BlockBreakingInfo)this.blockBreakingInfos.get(entityId);
            if (lv != null) {
                this.removeBlockBreakingInfo(lv);
            }
            if (lv == null || lv.getPos().getX() != pos.getX() || lv.getPos().getY() != pos.getY() || lv.getPos().getZ() != pos.getZ()) {
                lv = new BlockBreakingInfo(entityId, pos);
                this.blockBreakingInfos.put(entityId, lv);
            }
            lv.setStage(stage);
            lv.setLastUpdateTick(this.ticks);
            this.blockBreakingProgressions.computeIfAbsent(lv.getPos().asLong(), l -> Sets.newTreeSet()).add(lv);
        }
    }

    public boolean isTerrainRenderComplete() {
        return this.chunkBuilder.isEmpty();
    }

    public void method_52815(ChunkPos arg) {
        this.chunkRenderingDataPreparer.method_52819(arg);
    }

    public void scheduleTerrainUpdate() {
        this.chunkRenderingDataPreparer.method_52817();
        this.cloudsDirty = true;
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public void updateNoCullingBlockEntities(Collection<BlockEntity> removed, Collection<BlockEntity> added) {
        Set<BlockEntity> set = this.noCullingBlockEntities;
        synchronized (set) {
            this.noCullingBlockEntities.removeAll(removed);
            this.noCullingBlockEntities.addAll(added);
        }
    }

    public static int getLightmapCoordinates(BlockRenderView world, BlockPos pos) {
        return WorldRenderer.getLightmapCoordinates(world, world.getBlockState(pos), pos);
    }

    public static int getLightmapCoordinates(BlockRenderView world, BlockState state, BlockPos pos) {
        int k;
        if (state.hasEmissiveLighting(world, pos)) {
            return 0xF000F0;
        }
        int i = world.getLightLevel(LightType.SKY, pos);
        int j = world.getLightLevel(LightType.BLOCK, pos);
        if (j < (k = state.getLuminance())) {
            j = k;
        }
        return i << 20 | j << 4;
    }

    public boolean isRenderingReady(BlockPos pos) {
        ChunkBuilder.BuiltChunk lv = this.chunks.getRenderedChunk(pos);
        return lv != null && lv.data.get() != ChunkBuilder.ChunkData.EMPTY;
    }

    @Nullable
    public Framebuffer getEntityOutlinesFramebuffer() {
        return this.entityOutlinesFramebuffer;
    }

    @Nullable
    public Framebuffer getTranslucentFramebuffer() {
        return this.translucentFramebuffer;
    }

    @Nullable
    public Framebuffer getEntityFramebuffer() {
        return this.entityFramebuffer;
    }

    @Nullable
    public Framebuffer getParticlesFramebuffer() {
        return this.particlesFramebuffer;
    }

    @Nullable
    public Framebuffer getWeatherFramebuffer() {
        return this.weatherFramebuffer;
    }

    @Nullable
    public Framebuffer getCloudsFramebuffer() {
        return this.cloudsFramebuffer;
    }

    private void shootParticles(int direction, BlockPos pos, Random random, SimpleParticleType particleType) {
        Direction lv = Direction.byId(direction);
        int j = lv.getOffsetX();
        int k = lv.getOffsetY();
        int l = lv.getOffsetZ();
        double d = (double)pos.getX() + (double)j * 0.6 + 0.5;
        double e = (double)pos.getY() + (double)k * 0.6 + 0.5;
        double f = (double)pos.getZ() + (double)l * 0.6 + 0.5;
        for (int m = 0; m < 10; ++m) {
            double g = random.nextDouble() * 0.2 + 0.01;
            double h = d + (double)j * 0.01 + (random.nextDouble() - 0.5) * (double)l * 0.5;
            double n = e + (double)k * 0.01 + (random.nextDouble() - 0.5) * (double)k * 0.5;
            double o = f + (double)l * 0.01 + (random.nextDouble() - 0.5) * (double)j * 0.5;
            double p = (double)j * g + random.nextGaussian() * 0.01;
            double q = (double)k * g + random.nextGaussian() * 0.01;
            double r = (double)l * g + random.nextGaussian() * 0.01;
            this.addParticle(particleType, h, n, o, p, q, r);
        }
    }

    @Environment(value=EnvType.CLIENT)
    public static class ProgramInitException
    extends RuntimeException {
        public ProgramInitException(String message, Throwable cause) {
            super(message, cause);
        }
    }
}

