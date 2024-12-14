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
import com.google.gson.JsonSyntaxException;
import com.mojang.blaze3d.platform.GlConst;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.systems.VertexSorter;
import com.mojang.datafixers.util.Pair;
import com.mojang.logging.LogUtils;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.function.Consumer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.enums.CameraSubmersionType;
import net.minecraft.block.pattern.CachedBlockPosition;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gl.PostEffectProcessor;
import net.minecraft.client.gl.ShaderProgram;
import net.minecraft.client.gl.ShaderStage;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.hud.InGameOverlayRenderer;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.client.render.BufferBuilderStorage;
import net.minecraft.client.render.Camera;
import net.minecraft.client.render.DiffuseLighting;
import net.minecraft.client.render.LightmapTextureManager;
import net.minecraft.client.render.MapRenderer;
import net.minecraft.client.render.OverlayTexture;
import net.minecraft.client.render.RenderTickCounter;
import net.minecraft.client.render.VertexFormat;
import net.minecraft.client.render.VertexFormats;
import net.minecraft.client.render.item.HeldItemRenderer;
import net.minecraft.client.render.model.json.ModelTransformationMode;
import net.minecraft.client.texture.NativeImage;
import net.minecraft.client.util.ScreenshotRecorder;
import net.minecraft.client.util.Window;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.mob.CreeperEntity;
import net.minecraft.entity.mob.EndermanEntity;
import net.minecraft.entity.mob.SpiderEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.ProjectileUtil;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.resource.Resource;
import net.minecraft.resource.ResourceFactory;
import net.minecraft.resource.ResourceManager;
import net.minecraft.resource.ResourceReloader;
import net.minecraft.resource.SinglePreparationResourceReloader;
import net.minecraft.server.integrated.IntegratedServer;
import net.minecraft.util.Identifier;
import net.minecraft.util.Util;
import net.minecraft.util.crash.CrashException;
import net.minecraft.util.crash.CrashReport;
import net.minecraft.util.crash.CrashReportSection;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RotationAxis;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.random.Random;
import net.minecraft.util.profiler.Profiler;
import net.minecraft.world.GameMode;
import org.jetbrains.annotations.Nullable;
import org.joml.Matrix4f;
import org.joml.Matrix4fStack;
import org.joml.Quaternionf;
import org.joml.Vector3f;
import org.slf4j.Logger;

@Environment(value=EnvType.CLIENT)
public class GameRenderer
implements AutoCloseable {
    private static final Identifier NAUSEA_OVERLAY = Identifier.ofVanilla("textures/misc/nausea.png");
    private static final Identifier BLUR_PROCESSOR = Identifier.ofVanilla("shaders/post/blur.json");
    public static final int field_49904 = 10;
    static final Logger LOGGER = LogUtils.getLogger();
    private static final boolean field_32688 = false;
    public static final float CAMERA_DEPTH = 0.05f;
    private static final float field_44940 = 1000.0f;
    final MinecraftClient client;
    private final ResourceManager resourceManager;
    private final Random random = Random.create();
    private float viewDistance;
    public final HeldItemRenderer firstPersonRenderer;
    private final MapRenderer mapRenderer;
    private final BufferBuilderStorage buffers;
    private int ticks;
    private float fovMultiplier;
    private float lastFovMultiplier;
    private float skyDarkness;
    private float lastSkyDarkness;
    private boolean renderHand = true;
    private boolean blockOutlineEnabled = true;
    private long lastWorldIconUpdate;
    private boolean hasWorldIcon;
    private long lastWindowFocusedTime = Util.getMeasuringTimeMs();
    private final LightmapTextureManager lightmapTextureManager;
    private final OverlayTexture overlayTexture = new OverlayTexture();
    private boolean renderingPanorama;
    private float zoom = 1.0f;
    private float zoomX;
    private float zoomY;
    public static final int field_32687 = 40;
    @Nullable
    private ItemStack floatingItem;
    private int floatingItemTimeLeft;
    private float floatingItemWidth;
    private float floatingItemHeight;
    @Nullable
    PostEffectProcessor postProcessor;
    @Nullable
    private PostEffectProcessor blurPostProcessor;
    private boolean postProcessorEnabled;
    private final Camera camera = new Camera();
    @Nullable
    public ShaderProgram blitScreenProgram;
    private final Map<String, ShaderProgram> programs = Maps.newHashMap();
    @Nullable
    private static ShaderProgram positionProgram;
    @Nullable
    private static ShaderProgram positionColorProgram;
    @Nullable
    private static ShaderProgram positionTexProgram;
    @Nullable
    private static ShaderProgram positionTexColorProgram;
    @Nullable
    private static ShaderProgram particleProgram;
    @Nullable
    private static ShaderProgram positionColorLightmapProgram;
    @Nullable
    private static ShaderProgram positionColorTexLightmapProgram;
    @Nullable
    private static ShaderProgram renderTypeSolidProgram;
    @Nullable
    private static ShaderProgram renderTypeCutoutMippedProgram;
    @Nullable
    private static ShaderProgram renderTypeCutoutProgram;
    @Nullable
    private static ShaderProgram renderTypeTranslucentProgram;
    @Nullable
    private static ShaderProgram renderTypeTranslucentMovingBlockProgram;
    @Nullable
    private static ShaderProgram renderTypeArmorCutoutNoCullProgram;
    @Nullable
    private static ShaderProgram renderTypeEntitySolidProgram;
    @Nullable
    private static ShaderProgram renderTypeEntityCutoutProgram;
    @Nullable
    private static ShaderProgram renderTypeEntityCutoutNoNullProgram;
    @Nullable
    private static ShaderProgram renderTypeEntityCutoutNoNullZOffsetProgram;
    @Nullable
    private static ShaderProgram renderTypeItemEntityTranslucentCullProgram;
    @Nullable
    private static ShaderProgram renderTypeEntityTranslucentCullProgram;
    @Nullable
    private static ShaderProgram renderTypeEntityTranslucentProgram;
    @Nullable
    private static ShaderProgram renderTypeEntityTranslucentEmissiveProgram;
    @Nullable
    private static ShaderProgram renderTypeEntitySmoothCutoutProgram;
    @Nullable
    private static ShaderProgram renderTypeBeaconBeamProgram;
    @Nullable
    private static ShaderProgram renderTypeEntityDecalProgram;
    @Nullable
    private static ShaderProgram renderTypeEntityNoOutlineProgram;
    @Nullable
    private static ShaderProgram renderTypeEntityShadowProgram;
    @Nullable
    private static ShaderProgram renderTypeEntityAlphaProgram;
    @Nullable
    private static ShaderProgram renderTypeEyesProgram;
    @Nullable
    private static ShaderProgram renderTypeEnergySwirlProgram;
    @Nullable
    private static ShaderProgram renderTypeBreezeWindProgram;
    @Nullable
    private static ShaderProgram renderTypeLeashProgram;
    @Nullable
    private static ShaderProgram renderTypeWaterMaskProgram;
    @Nullable
    private static ShaderProgram renderTypeOutlineProgram;
    @Nullable
    private static ShaderProgram renderTypeArmorGlintProgram;
    @Nullable
    private static ShaderProgram renderTypeArmorEntityGlintProgram;
    @Nullable
    private static ShaderProgram renderTypeGlintTranslucentProgram;
    @Nullable
    private static ShaderProgram renderTypeGlintProgram;
    @Nullable
    private static ShaderProgram renderTypeGlintDirectProgram;
    @Nullable
    private static ShaderProgram renderTypeEntityGlintProgram;
    @Nullable
    private static ShaderProgram renderTypeEntityGlintDirectProgram;
    @Nullable
    private static ShaderProgram renderTypeTextProgram;
    @Nullable
    private static ShaderProgram renderTypeTextBackgroundProgram;
    @Nullable
    private static ShaderProgram renderTypeTextIntensityProgram;
    @Nullable
    private static ShaderProgram renderTypeTextSeeThroughProgram;
    @Nullable
    private static ShaderProgram renderTypeTextBackgroundSeeThroughProgram;
    @Nullable
    private static ShaderProgram renderTypeTextIntensitySeeThroughProgram;
    @Nullable
    private static ShaderProgram renderTypeLightningProgram;
    @Nullable
    private static ShaderProgram renderTypeTripwireProgram;
    @Nullable
    private static ShaderProgram renderTypeEndPortalProgram;
    @Nullable
    private static ShaderProgram renderTypeEndGatewayProgram;
    @Nullable
    private static ShaderProgram renderTypeCloudsProgram;
    @Nullable
    private static ShaderProgram renderTypeLinesProgram;
    @Nullable
    private static ShaderProgram renderTypeCrumblingProgram;
    @Nullable
    private static ShaderProgram renderTypeGuiProgram;
    @Nullable
    private static ShaderProgram renderTypeGuiOverlayProgram;
    @Nullable
    private static ShaderProgram renderTypeGuiTextHighlightProgram;
    @Nullable
    private static ShaderProgram renderTypeGuiGhostRecipeOverlayProgram;

    public GameRenderer(MinecraftClient client, HeldItemRenderer heldItemRenderer, ResourceManager resourceManager, BufferBuilderStorage buffers) {
        this.client = client;
        this.resourceManager = resourceManager;
        this.firstPersonRenderer = heldItemRenderer;
        this.mapRenderer = new MapRenderer(client.getTextureManager(), client.getMapDecorationsAtlasManager());
        this.lightmapTextureManager = new LightmapTextureManager(this, client);
        this.buffers = buffers;
        this.postProcessor = null;
    }

    @Override
    public void close() {
        this.lightmapTextureManager.close();
        this.mapRenderer.close();
        this.overlayTexture.close();
        this.disablePostProcessor();
        this.clearPrograms();
        if (this.blurPostProcessor != null) {
            this.blurPostProcessor.close();
        }
        if (this.blitScreenProgram != null) {
            this.blitScreenProgram.close();
        }
    }

    public void setRenderHand(boolean renderHand) {
        this.renderHand = renderHand;
    }

    public void setBlockOutlineEnabled(boolean blockOutlineEnabled) {
        this.blockOutlineEnabled = blockOutlineEnabled;
    }

    public void setRenderingPanorama(boolean renderingPanorama) {
        this.renderingPanorama = renderingPanorama;
    }

    public boolean isRenderingPanorama() {
        return this.renderingPanorama;
    }

    public void disablePostProcessor() {
        if (this.postProcessor != null) {
            this.postProcessor.close();
        }
        this.postProcessor = null;
    }

    public void togglePostProcessorEnabled() {
        this.postProcessorEnabled = !this.postProcessorEnabled;
    }

    public void onCameraEntitySet(@Nullable Entity entity) {
        if (this.postProcessor != null) {
            this.postProcessor.close();
        }
        this.postProcessor = null;
        if (entity instanceof CreeperEntity) {
            this.loadPostProcessor(Identifier.ofVanilla("shaders/post/creeper.json"));
        } else if (entity instanceof SpiderEntity) {
            this.loadPostProcessor(Identifier.ofVanilla("shaders/post/spider.json"));
        } else if (entity instanceof EndermanEntity) {
            this.loadPostProcessor(Identifier.ofVanilla("shaders/post/invert.json"));
        }
    }

    private void loadPostProcessor(Identifier id) {
        if (this.postProcessor != null) {
            this.postProcessor.close();
        }
        try {
            this.postProcessor = new PostEffectProcessor(this.client.getTextureManager(), this.resourceManager, this.client.getFramebuffer(), id);
            this.postProcessor.setupDimensions(this.client.getWindow().getFramebufferWidth(), this.client.getWindow().getFramebufferHeight());
            this.postProcessorEnabled = true;
        } catch (IOException iOException) {
            LOGGER.warn("Failed to load shader: {}", (Object)id, (Object)iOException);
            this.postProcessorEnabled = false;
        } catch (JsonSyntaxException jsonSyntaxException) {
            LOGGER.warn("Failed to parse shader: {}", (Object)id, (Object)jsonSyntaxException);
            this.postProcessorEnabled = false;
        }
    }

    private void loadBlurPostProcessor(ResourceFactory resourceFactory) {
        if (this.blurPostProcessor != null) {
            this.blurPostProcessor.close();
        }
        try {
            this.blurPostProcessor = new PostEffectProcessor(this.client.getTextureManager(), resourceFactory, this.client.getFramebuffer(), BLUR_PROCESSOR);
            this.blurPostProcessor.setupDimensions(this.client.getWindow().getFramebufferWidth(), this.client.getWindow().getFramebufferHeight());
        } catch (IOException iOException) {
            LOGGER.warn("Failed to load shader: {}", (Object)BLUR_PROCESSOR, (Object)iOException);
        } catch (JsonSyntaxException jsonSyntaxException) {
            LOGGER.warn("Failed to parse shader: {}", (Object)BLUR_PROCESSOR, (Object)jsonSyntaxException);
        }
    }

    public void renderBlur(float delta) {
        float g = this.client.options.getMenuBackgroundBlurrinessValue();
        if (this.blurPostProcessor != null && g >= 1.0f) {
            this.blurPostProcessor.setUniforms("Radius", g);
            this.blurPostProcessor.render(delta);
        }
    }

    public ResourceReloader createProgramReloader() {
        return new SinglePreparationResourceReloader<CachedResourceFactory>(){

            @Override
            protected CachedResourceFactory prepare(ResourceManager arg, Profiler arg2) {
                Map<Identifier, Resource> map = arg.findResources("shaders", id -> {
                    String string = id.getPath();
                    return string.endsWith(".json") || string.endsWith(ShaderStage.Type.FRAGMENT.getFileExtension()) || string.endsWith(ShaderStage.Type.VERTEX.getFileExtension()) || string.endsWith(".glsl");
                });
                HashMap<Identifier, Resource> map2 = new HashMap<Identifier, Resource>();
                map.forEach((id, resource) -> {
                    try (InputStream inputStream = resource.getInputStream();){
                        byte[] bs = inputStream.readAllBytes();
                        map2.put((Identifier)id, new Resource(resource.getPack(), () -> new ByteArrayInputStream(bs)));
                    } catch (Exception exception) {
                        LOGGER.warn("Failed to read resource {}", id, (Object)exception);
                    }
                });
                return new CachedResourceFactory(arg, map2);
            }

            @Override
            protected void apply(CachedResourceFactory arg, ResourceManager arg2, Profiler arg3) {
                GameRenderer.this.loadPrograms(arg);
                if (GameRenderer.this.postProcessor != null) {
                    GameRenderer.this.postProcessor.close();
                }
                GameRenderer.this.postProcessor = null;
                GameRenderer.this.onCameraEntitySet(GameRenderer.this.client.getCameraEntity());
            }

            @Override
            public String getName() {
                return "Shader Loader";
            }

            @Override
            protected /* synthetic */ Object prepare(ResourceManager manager, Profiler profiler) {
                return this.prepare(manager, profiler);
            }
        };
    }

    public void preloadPrograms(ResourceFactory factory) {
        if (this.blitScreenProgram != null) {
            throw new RuntimeException("Blit shader already preloaded");
        }
        try {
            this.blitScreenProgram = new ShaderProgram(factory, "blit_screen", VertexFormats.BLIT_SCREEN);
        } catch (IOException iOException) {
            throw new RuntimeException("could not preload blit shader", iOException);
        }
        renderTypeGuiProgram = this.preloadProgram(factory, "rendertype_gui", VertexFormats.POSITION_COLOR);
        renderTypeGuiOverlayProgram = this.preloadProgram(factory, "rendertype_gui_overlay", VertexFormats.POSITION_COLOR);
        positionProgram = this.preloadProgram(factory, "position", VertexFormats.POSITION);
        positionColorProgram = this.preloadProgram(factory, "position_color", VertexFormats.POSITION_COLOR);
        positionTexProgram = this.preloadProgram(factory, "position_tex", VertexFormats.POSITION_TEXTURE);
        positionTexColorProgram = this.preloadProgram(factory, "position_tex_color", VertexFormats.POSITION_TEXTURE_COLOR);
        renderTypeTextProgram = this.preloadProgram(factory, "rendertype_text", VertexFormats.POSITION_COLOR_TEXTURE_LIGHT);
    }

    private ShaderProgram preloadProgram(ResourceFactory factory, String name, VertexFormat format) {
        try {
            ShaderProgram lv = new ShaderProgram(factory, name, format);
            this.programs.put(name, lv);
            return lv;
        } catch (Exception exception) {
            throw new IllegalStateException("could not preload shader " + name, exception);
        }
    }

    void loadPrograms(ResourceFactory factory) {
        RenderSystem.assertOnRenderThread();
        ArrayList<ShaderStage> list = Lists.newArrayList();
        list.addAll(ShaderStage.Type.FRAGMENT.getLoadedShaders().values());
        list.addAll(ShaderStage.Type.VERTEX.getLoadedShaders().values());
        list.forEach(ShaderStage::release);
        ArrayList<Pair<ShaderProgram, Consumer<ShaderProgram>>> list2 = Lists.newArrayListWithCapacity(this.programs.size());
        try {
            list2.add(Pair.of(new ShaderProgram(factory, "particle", VertexFormats.POSITION_TEXTURE_COLOR_LIGHT), program -> {
                particleProgram = program;
            }));
            list2.add(Pair.of(new ShaderProgram(factory, "position", VertexFormats.POSITION), program -> {
                positionProgram = program;
            }));
            list2.add(Pair.of(new ShaderProgram(factory, "position_color", VertexFormats.POSITION_COLOR), program -> {
                positionColorProgram = program;
            }));
            list2.add(Pair.of(new ShaderProgram(factory, "position_color_lightmap", VertexFormats.POSITION_COLOR_LIGHT), program -> {
                positionColorLightmapProgram = program;
            }));
            list2.add(Pair.of(new ShaderProgram(factory, "position_color_tex_lightmap", VertexFormats.POSITION_COLOR_TEXTURE_LIGHT), program -> {
                positionColorTexLightmapProgram = program;
            }));
            list2.add(Pair.of(new ShaderProgram(factory, "position_tex", VertexFormats.POSITION_TEXTURE), program -> {
                positionTexProgram = program;
            }));
            list2.add(Pair.of(new ShaderProgram(factory, "position_tex_color", VertexFormats.POSITION_TEXTURE_COLOR), program -> {
                positionTexColorProgram = program;
            }));
            list2.add(Pair.of(new ShaderProgram(factory, "rendertype_solid", VertexFormats.POSITION_COLOR_TEXTURE_LIGHT_NORMAL), program -> {
                renderTypeSolidProgram = program;
            }));
            list2.add(Pair.of(new ShaderProgram(factory, "rendertype_cutout_mipped", VertexFormats.POSITION_COLOR_TEXTURE_LIGHT_NORMAL), program -> {
                renderTypeCutoutMippedProgram = program;
            }));
            list2.add(Pair.of(new ShaderProgram(factory, "rendertype_cutout", VertexFormats.POSITION_COLOR_TEXTURE_LIGHT_NORMAL), program -> {
                renderTypeCutoutProgram = program;
            }));
            list2.add(Pair.of(new ShaderProgram(factory, "rendertype_translucent", VertexFormats.POSITION_COLOR_TEXTURE_LIGHT_NORMAL), program -> {
                renderTypeTranslucentProgram = program;
            }));
            list2.add(Pair.of(new ShaderProgram(factory, "rendertype_translucent_moving_block", VertexFormats.POSITION_COLOR_TEXTURE_LIGHT_NORMAL), program -> {
                renderTypeTranslucentMovingBlockProgram = program;
            }));
            list2.add(Pair.of(new ShaderProgram(factory, "rendertype_armor_cutout_no_cull", VertexFormats.POSITION_COLOR_TEXTURE_OVERLAY_LIGHT_NORMAL), program -> {
                renderTypeArmorCutoutNoCullProgram = program;
            }));
            list2.add(Pair.of(new ShaderProgram(factory, "rendertype_entity_solid", VertexFormats.POSITION_COLOR_TEXTURE_OVERLAY_LIGHT_NORMAL), program -> {
                renderTypeEntitySolidProgram = program;
            }));
            list2.add(Pair.of(new ShaderProgram(factory, "rendertype_entity_cutout", VertexFormats.POSITION_COLOR_TEXTURE_OVERLAY_LIGHT_NORMAL), program -> {
                renderTypeEntityCutoutProgram = program;
            }));
            list2.add(Pair.of(new ShaderProgram(factory, "rendertype_entity_cutout_no_cull", VertexFormats.POSITION_COLOR_TEXTURE_OVERLAY_LIGHT_NORMAL), program -> {
                renderTypeEntityCutoutNoNullProgram = program;
            }));
            list2.add(Pair.of(new ShaderProgram(factory, "rendertype_entity_cutout_no_cull_z_offset", VertexFormats.POSITION_COLOR_TEXTURE_OVERLAY_LIGHT_NORMAL), program -> {
                renderTypeEntityCutoutNoNullZOffsetProgram = program;
            }));
            list2.add(Pair.of(new ShaderProgram(factory, "rendertype_item_entity_translucent_cull", VertexFormats.POSITION_COLOR_TEXTURE_OVERLAY_LIGHT_NORMAL), arg -> {
                renderTypeItemEntityTranslucentCullProgram = arg;
            }));
            list2.add(Pair.of(new ShaderProgram(factory, "rendertype_entity_translucent_cull", VertexFormats.POSITION_COLOR_TEXTURE_OVERLAY_LIGHT_NORMAL), program -> {
                renderTypeEntityTranslucentCullProgram = program;
            }));
            list2.add(Pair.of(new ShaderProgram(factory, "rendertype_entity_translucent", VertexFormats.POSITION_COLOR_TEXTURE_OVERLAY_LIGHT_NORMAL), program -> {
                renderTypeEntityTranslucentProgram = program;
            }));
            list2.add(Pair.of(new ShaderProgram(factory, "rendertype_entity_translucent_emissive", VertexFormats.POSITION_COLOR_TEXTURE_OVERLAY_LIGHT_NORMAL), program -> {
                renderTypeEntityTranslucentEmissiveProgram = program;
            }));
            list2.add(Pair.of(new ShaderProgram(factory, "rendertype_entity_smooth_cutout", VertexFormats.POSITION_COLOR_TEXTURE_OVERLAY_LIGHT_NORMAL), program -> {
                renderTypeEntitySmoothCutoutProgram = program;
            }));
            list2.add(Pair.of(new ShaderProgram(factory, "rendertype_beacon_beam", VertexFormats.POSITION_COLOR_TEXTURE_LIGHT_NORMAL), program -> {
                renderTypeBeaconBeamProgram = program;
            }));
            list2.add(Pair.of(new ShaderProgram(factory, "rendertype_entity_decal", VertexFormats.POSITION_COLOR_TEXTURE_OVERLAY_LIGHT_NORMAL), program -> {
                renderTypeEntityDecalProgram = program;
            }));
            list2.add(Pair.of(new ShaderProgram(factory, "rendertype_entity_no_outline", VertexFormats.POSITION_COLOR_TEXTURE_OVERLAY_LIGHT_NORMAL), program -> {
                renderTypeEntityNoOutlineProgram = program;
            }));
            list2.add(Pair.of(new ShaderProgram(factory, "rendertype_entity_shadow", VertexFormats.POSITION_COLOR_TEXTURE_OVERLAY_LIGHT_NORMAL), program -> {
                renderTypeEntityShadowProgram = program;
            }));
            list2.add(Pair.of(new ShaderProgram(factory, "rendertype_entity_alpha", VertexFormats.POSITION_COLOR_TEXTURE_OVERLAY_LIGHT_NORMAL), program -> {
                renderTypeEntityAlphaProgram = program;
            }));
            list2.add(Pair.of(new ShaderProgram(factory, "rendertype_eyes", VertexFormats.POSITION_COLOR_TEXTURE_OVERLAY_LIGHT_NORMAL), program -> {
                renderTypeEyesProgram = program;
            }));
            list2.add(Pair.of(new ShaderProgram(factory, "rendertype_energy_swirl", VertexFormats.POSITION_COLOR_TEXTURE_OVERLAY_LIGHT_NORMAL), program -> {
                renderTypeEnergySwirlProgram = program;
            }));
            list2.add(Pair.of(new ShaderProgram(factory, "rendertype_leash", VertexFormats.POSITION_COLOR_LIGHT), program -> {
                renderTypeLeashProgram = program;
            }));
            list2.add(Pair.of(new ShaderProgram(factory, "rendertype_water_mask", VertexFormats.POSITION), program -> {
                renderTypeWaterMaskProgram = program;
            }));
            list2.add(Pair.of(new ShaderProgram(factory, "rendertype_outline", VertexFormats.POSITION_TEXTURE_COLOR), program -> {
                renderTypeOutlineProgram = program;
            }));
            list2.add(Pair.of(new ShaderProgram(factory, "rendertype_armor_entity_glint", VertexFormats.POSITION_TEXTURE), program -> {
                renderTypeArmorEntityGlintProgram = program;
            }));
            list2.add(Pair.of(new ShaderProgram(factory, "rendertype_glint_translucent", VertexFormats.POSITION_TEXTURE), program -> {
                renderTypeGlintTranslucentProgram = program;
            }));
            list2.add(Pair.of(new ShaderProgram(factory, "rendertype_glint", VertexFormats.POSITION_TEXTURE), program -> {
                renderTypeGlintProgram = program;
            }));
            list2.add(Pair.of(new ShaderProgram(factory, "rendertype_entity_glint", VertexFormats.POSITION_TEXTURE), program -> {
                renderTypeEntityGlintProgram = program;
            }));
            list2.add(Pair.of(new ShaderProgram(factory, "rendertype_entity_glint_direct", VertexFormats.POSITION_TEXTURE), program -> {
                renderTypeEntityGlintDirectProgram = program;
            }));
            list2.add(Pair.of(new ShaderProgram(factory, "rendertype_text", VertexFormats.POSITION_COLOR_TEXTURE_LIGHT), program -> {
                renderTypeTextProgram = program;
            }));
            list2.add(Pair.of(new ShaderProgram(factory, "rendertype_text_background", VertexFormats.POSITION_COLOR_LIGHT), program -> {
                renderTypeTextBackgroundProgram = program;
            }));
            list2.add(Pair.of(new ShaderProgram(factory, "rendertype_text_intensity", VertexFormats.POSITION_COLOR_TEXTURE_LIGHT), program -> {
                renderTypeTextIntensityProgram = program;
            }));
            list2.add(Pair.of(new ShaderProgram(factory, "rendertype_text_see_through", VertexFormats.POSITION_COLOR_TEXTURE_LIGHT), program -> {
                renderTypeTextSeeThroughProgram = program;
            }));
            list2.add(Pair.of(new ShaderProgram(factory, "rendertype_text_background_see_through", VertexFormats.POSITION_COLOR_LIGHT), program -> {
                renderTypeTextBackgroundSeeThroughProgram = program;
            }));
            list2.add(Pair.of(new ShaderProgram(factory, "rendertype_text_intensity_see_through", VertexFormats.POSITION_COLOR_TEXTURE_LIGHT), program -> {
                renderTypeTextIntensitySeeThroughProgram = program;
            }));
            list2.add(Pair.of(new ShaderProgram(factory, "rendertype_lightning", VertexFormats.POSITION_COLOR), program -> {
                renderTypeLightningProgram = program;
            }));
            list2.add(Pair.of(new ShaderProgram(factory, "rendertype_tripwire", VertexFormats.POSITION_COLOR_TEXTURE_LIGHT_NORMAL), program -> {
                renderTypeTripwireProgram = program;
            }));
            list2.add(Pair.of(new ShaderProgram(factory, "rendertype_end_portal", VertexFormats.POSITION), program -> {
                renderTypeEndPortalProgram = program;
            }));
            list2.add(Pair.of(new ShaderProgram(factory, "rendertype_end_gateway", VertexFormats.POSITION), program -> {
                renderTypeEndGatewayProgram = program;
            }));
            list2.add(Pair.of(new ShaderProgram(factory, "rendertype_clouds", VertexFormats.POSITION_TEXTURE_COLOR_NORMAL), program -> {
                renderTypeCloudsProgram = program;
            }));
            list2.add(Pair.of(new ShaderProgram(factory, "rendertype_lines", VertexFormats.LINES), program -> {
                renderTypeLinesProgram = program;
            }));
            list2.add(Pair.of(new ShaderProgram(factory, "rendertype_crumbling", VertexFormats.POSITION_COLOR_TEXTURE_LIGHT_NORMAL), program -> {
                renderTypeCrumblingProgram = program;
            }));
            list2.add(Pair.of(new ShaderProgram(factory, "rendertype_gui", VertexFormats.POSITION_COLOR), program -> {
                renderTypeGuiProgram = program;
            }));
            list2.add(Pair.of(new ShaderProgram(factory, "rendertype_gui_overlay", VertexFormats.POSITION_COLOR), program -> {
                renderTypeGuiOverlayProgram = program;
            }));
            list2.add(Pair.of(new ShaderProgram(factory, "rendertype_gui_text_highlight", VertexFormats.POSITION_COLOR), program -> {
                renderTypeGuiTextHighlightProgram = program;
            }));
            list2.add(Pair.of(new ShaderProgram(factory, "rendertype_gui_ghost_recipe_overlay", VertexFormats.POSITION_COLOR), program -> {
                renderTypeGuiGhostRecipeOverlayProgram = program;
            }));
            list2.add(Pair.of(new ShaderProgram(factory, "rendertype_breeze_wind", VertexFormats.POSITION_COLOR_TEXTURE_OVERLAY_LIGHT_NORMAL), program -> {
                renderTypeBreezeWindProgram = program;
            }));
            this.loadBlurPostProcessor(factory);
        } catch (IOException iOException) {
            list2.forEach(pair -> ((ShaderProgram)pair.getFirst()).close());
            throw new RuntimeException("could not reload shaders", iOException);
        }
        this.clearPrograms();
        list2.forEach(pair -> {
            ShaderProgram lv = (ShaderProgram)pair.getFirst();
            this.programs.put(lv.getName(), lv);
            ((Consumer)pair.getSecond()).accept(lv);
        });
    }

    private void clearPrograms() {
        RenderSystem.assertOnRenderThread();
        this.programs.values().forEach(ShaderProgram::close);
        this.programs.clear();
    }

    @Nullable
    public ShaderProgram getProgram(@Nullable String name) {
        if (name == null) {
            return null;
        }
        return this.programs.get(name);
    }

    public void tick() {
        this.updateFovMultiplier();
        this.lightmapTextureManager.tick();
        if (this.client.getCameraEntity() == null) {
            this.client.setCameraEntity(this.client.player);
        }
        this.camera.updateEyeHeight();
        this.firstPersonRenderer.updateHeldItems();
        ++this.ticks;
        if (!this.client.world.getTickManager().shouldTick()) {
            return;
        }
        this.client.worldRenderer.tickRainSplashing(this.camera);
        this.lastSkyDarkness = this.skyDarkness;
        if (this.client.inGameHud.getBossBarHud().shouldDarkenSky()) {
            this.skyDarkness += 0.05f;
            if (this.skyDarkness > 1.0f) {
                this.skyDarkness = 1.0f;
            }
        } else if (this.skyDarkness > 0.0f) {
            this.skyDarkness -= 0.0125f;
        }
        if (this.floatingItemTimeLeft > 0) {
            --this.floatingItemTimeLeft;
            if (this.floatingItemTimeLeft == 0) {
                this.floatingItem = null;
            }
        }
    }

    @Nullable
    public PostEffectProcessor getPostProcessor() {
        return this.postProcessor;
    }

    public void onResized(int width, int height) {
        if (this.postProcessor != null) {
            this.postProcessor.setupDimensions(width, height);
        }
        if (this.blurPostProcessor != null) {
            this.blurPostProcessor.setupDimensions(width, height);
        }
        this.client.worldRenderer.onResized(width, height);
    }

    public void updateCrosshairTarget(float tickDelta) {
        Entity entity;
        HitResult lv2;
        Entity lv = this.client.getCameraEntity();
        if (lv == null) {
            return;
        }
        if (this.client.world == null || this.client.player == null) {
            return;
        }
        this.client.getProfiler().push("pick");
        double d = this.client.player.getBlockInteractionRange();
        double e = this.client.player.getEntityInteractionRange();
        this.client.crosshairTarget = lv2 = this.findCrosshairTarget(lv, d, e, tickDelta);
        if (lv2 instanceof EntityHitResult) {
            EntityHitResult lv3 = (EntityHitResult)lv2;
            entity = lv3.getEntity();
        } else {
            entity = null;
        }
        this.client.targetedEntity = entity;
        this.client.getProfiler().pop();
    }

    private HitResult findCrosshairTarget(Entity camera, double blockInteractionRange, double entityInteractionRange, float tickDelta) {
        double g = Math.max(blockInteractionRange, entityInteractionRange);
        double h = MathHelper.square(g);
        Vec3d lv = camera.getCameraPosVec(tickDelta);
        HitResult lv2 = camera.raycast(g, tickDelta, false);
        double i = lv2.getPos().squaredDistanceTo(lv);
        if (lv2.getType() != HitResult.Type.MISS) {
            h = i;
            g = Math.sqrt(h);
        }
        Vec3d lv3 = camera.getRotationVec(tickDelta);
        Vec3d lv4 = lv.add(lv3.x * g, lv3.y * g, lv3.z * g);
        float j = 1.0f;
        Box lv5 = camera.getBoundingBox().stretch(lv3.multiply(g)).expand(1.0, 1.0, 1.0);
        EntityHitResult lv6 = ProjectileUtil.raycast(camera, lv, lv4, lv5, entity -> !entity.isSpectator() && entity.canHit(), h);
        if (lv6 != null && lv6.getPos().squaredDistanceTo(lv) < i) {
            return GameRenderer.ensureTargetInRange(lv6, lv, entityInteractionRange);
        }
        return GameRenderer.ensureTargetInRange(lv2, lv, blockInteractionRange);
    }

    private static HitResult ensureTargetInRange(HitResult hitResult, Vec3d cameraPos, double interactionRange) {
        Vec3d lv = hitResult.getPos();
        if (!lv.isInRange(cameraPos, interactionRange)) {
            Vec3d lv2 = hitResult.getPos();
            Direction lv3 = Direction.getFacing(lv2.x - cameraPos.x, lv2.y - cameraPos.y, lv2.z - cameraPos.z);
            return BlockHitResult.createMissed(lv2, lv3, BlockPos.ofFloored(lv2));
        }
        return hitResult;
    }

    private void updateFovMultiplier() {
        float f = 1.0f;
        Entity entity = this.client.getCameraEntity();
        if (entity instanceof AbstractClientPlayerEntity) {
            AbstractClientPlayerEntity lv = (AbstractClientPlayerEntity)entity;
            f = lv.getFovMultiplier();
        }
        this.lastFovMultiplier = this.fovMultiplier;
        this.fovMultiplier += (f - this.fovMultiplier) * 0.5f;
        if (this.fovMultiplier > 1.5f) {
            this.fovMultiplier = 1.5f;
        }
        if (this.fovMultiplier < 0.1f) {
            this.fovMultiplier = 0.1f;
        }
    }

    private double getFov(Camera camera, float tickDelta, boolean changingFov) {
        CameraSubmersionType lv;
        if (this.renderingPanorama) {
            return 90.0;
        }
        double d = 70.0;
        if (changingFov) {
            d = this.client.options.getFov().getValue().intValue();
            d *= (double)MathHelper.lerp(tickDelta, this.lastFovMultiplier, this.fovMultiplier);
        }
        if (camera.getFocusedEntity() instanceof LivingEntity && ((LivingEntity)camera.getFocusedEntity()).isDead()) {
            float g = Math.min((float)((LivingEntity)camera.getFocusedEntity()).deathTime + tickDelta, 20.0f);
            d /= (double)((1.0f - 500.0f / (g + 500.0f)) * 2.0f + 1.0f);
        }
        if ((lv = camera.getSubmersionType()) == CameraSubmersionType.LAVA || lv == CameraSubmersionType.WATER) {
            d *= MathHelper.lerp(this.client.options.getFovEffectScale().getValue(), 1.0, 0.8571428656578064);
        }
        return d;
    }

    private void tiltViewWhenHurt(MatrixStack matrices, float tickDelta) {
        Entity entity = this.client.getCameraEntity();
        if (entity instanceof LivingEntity) {
            float h;
            LivingEntity lv = (LivingEntity)entity;
            float g = (float)lv.hurtTime - tickDelta;
            if (lv.isDead()) {
                h = Math.min((float)lv.deathTime + tickDelta, 20.0f);
                matrices.multiply(RotationAxis.POSITIVE_Z.rotationDegrees(40.0f - 8000.0f / (h + 200.0f)));
            }
            if (g < 0.0f) {
                return;
            }
            g /= (float)lv.maxHurtTime;
            g = MathHelper.sin(g * g * g * g * (float)Math.PI);
            h = lv.getDamageTiltYaw();
            matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(-h));
            float i = (float)((double)(-g) * 14.0 * this.client.options.getDamageTiltStrength().getValue());
            matrices.multiply(RotationAxis.POSITIVE_Z.rotationDegrees(i));
            matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(h));
        }
    }

    private void bobView(MatrixStack matrices, float tickDelta) {
        if (!(this.client.getCameraEntity() instanceof PlayerEntity)) {
            return;
        }
        PlayerEntity lv = (PlayerEntity)this.client.getCameraEntity();
        float g = lv.horizontalSpeed - lv.prevHorizontalSpeed;
        float h = -(lv.horizontalSpeed + g * tickDelta);
        float i = MathHelper.lerp(tickDelta, lv.prevStrideDistance, lv.strideDistance);
        matrices.translate(MathHelper.sin(h * (float)Math.PI) * i * 0.5f, -Math.abs(MathHelper.cos(h * (float)Math.PI) * i), 0.0f);
        matrices.multiply(RotationAxis.POSITIVE_Z.rotationDegrees(MathHelper.sin(h * (float)Math.PI) * i * 3.0f));
        matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(Math.abs(MathHelper.cos(h * (float)Math.PI - 0.2f) * i) * 5.0f));
    }

    public void renderWithZoom(float zoom, float zoomX, float zoomY) {
        this.zoom = zoom;
        this.zoomX = zoomX;
        this.zoomY = zoomY;
        this.setBlockOutlineEnabled(false);
        this.setRenderHand(false);
        this.renderWorld(RenderTickCounter.ZERO);
        this.zoom = 1.0f;
    }

    private void renderHand(Camera camera, float tickDelta, Matrix4f matrix4f) {
        boolean bl;
        if (this.renderingPanorama) {
            return;
        }
        this.loadProjectionMatrix(this.getBasicProjectionMatrix(this.getFov(camera, tickDelta, false)));
        MatrixStack lv = new MatrixStack();
        lv.push();
        lv.multiplyPositionMatrix(matrix4f.invert(new Matrix4f()));
        Matrix4fStack matrix4fStack = RenderSystem.getModelViewStack();
        matrix4fStack.pushMatrix().mul(matrix4f);
        RenderSystem.applyModelViewMatrix();
        this.tiltViewWhenHurt(lv, tickDelta);
        if (this.client.options.getBobView().getValue().booleanValue()) {
            this.bobView(lv, tickDelta);
        }
        boolean bl2 = bl = this.client.getCameraEntity() instanceof LivingEntity && ((LivingEntity)this.client.getCameraEntity()).isSleeping();
        if (this.client.options.getPerspective().isFirstPerson() && !bl && !this.client.options.hudHidden && this.client.interactionManager.getCurrentGameMode() != GameMode.SPECTATOR) {
            this.lightmapTextureManager.enable();
            this.firstPersonRenderer.renderItem(tickDelta, lv, this.buffers.getEntityVertexConsumers(), this.client.player, this.client.getEntityRenderDispatcher().getLight(this.client.player, tickDelta));
            this.lightmapTextureManager.disable();
        }
        matrix4fStack.popMatrix();
        RenderSystem.applyModelViewMatrix();
        lv.pop();
        if (this.client.options.getPerspective().isFirstPerson() && !bl) {
            InGameOverlayRenderer.renderOverlays(this.client, lv);
        }
    }

    public void loadProjectionMatrix(Matrix4f projectionMatrix) {
        RenderSystem.setProjectionMatrix(projectionMatrix, VertexSorter.BY_DISTANCE);
    }

    public Matrix4f getBasicProjectionMatrix(double fov) {
        Matrix4f matrix4f = new Matrix4f();
        if (this.zoom != 1.0f) {
            matrix4f.translate(this.zoomX, -this.zoomY, 0.0f);
            matrix4f.scale(this.zoom, this.zoom, 1.0f);
        }
        return matrix4f.perspective((float)(fov * 0.01745329238474369), (float)this.client.getWindow().getFramebufferWidth() / (float)this.client.getWindow().getFramebufferHeight(), 0.05f, this.getFarPlaneDistance());
    }

    public float getFarPlaneDistance() {
        return this.viewDistance * 4.0f;
    }

    public static float getNightVisionStrength(LivingEntity entity, float tickDelta) {
        StatusEffectInstance lv = entity.getStatusEffect(StatusEffects.NIGHT_VISION);
        if (!lv.isDurationBelow(200)) {
            return 1.0f;
        }
        return 0.7f + MathHelper.sin(((float)lv.getDuration() - tickDelta) * (float)Math.PI * 0.2f) * 0.3f;
    }

    public void render(RenderTickCounter tickCounter, boolean tick) {
        if (this.client.isWindowFocused() || !this.client.options.pauseOnLostFocus || this.client.options.getTouchscreen().getValue().booleanValue() && this.client.mouse.wasRightButtonClicked()) {
            this.lastWindowFocusedTime = Util.getMeasuringTimeMs();
        } else if (Util.getMeasuringTimeMs() - this.lastWindowFocusedTime > 500L) {
            this.client.openGameMenu(false);
        }
        if (this.client.skipGameRender) {
            return;
        }
        boolean bl2 = this.client.isFinishedLoading();
        int i = (int)(this.client.mouse.getX() * (double)this.client.getWindow().getScaledWidth() / (double)this.client.getWindow().getWidth());
        int j = (int)(this.client.mouse.getY() * (double)this.client.getWindow().getScaledHeight() / (double)this.client.getWindow().getHeight());
        RenderSystem.viewport(0, 0, this.client.getWindow().getFramebufferWidth(), this.client.getWindow().getFramebufferHeight());
        if (bl2 && tick && this.client.world != null) {
            this.client.getProfiler().push("level");
            this.renderWorld(tickCounter);
            this.updateWorldIcon();
            this.client.worldRenderer.drawEntityOutlinesFramebuffer();
            if (this.postProcessor != null && this.postProcessorEnabled) {
                RenderSystem.disableBlend();
                RenderSystem.disableDepthTest();
                RenderSystem.resetTextureMatrix();
                this.postProcessor.render(tickCounter.getLastFrameDuration());
            }
            this.client.getFramebuffer().beginWrite(true);
        }
        Window lv = this.client.getWindow();
        RenderSystem.clear(GlConst.GL_DEPTH_BUFFER_BIT, MinecraftClient.IS_SYSTEM_MAC);
        Matrix4f matrix4f = new Matrix4f().setOrtho(0.0f, (float)((double)lv.getFramebufferWidth() / lv.getScaleFactor()), (float)((double)lv.getFramebufferHeight() / lv.getScaleFactor()), 0.0f, 1000.0f, 21000.0f);
        RenderSystem.setProjectionMatrix(matrix4f, VertexSorter.BY_Z);
        Matrix4fStack matrix4fStack = RenderSystem.getModelViewStack();
        matrix4fStack.pushMatrix();
        matrix4fStack.translation(0.0f, 0.0f, -11000.0f);
        RenderSystem.applyModelViewMatrix();
        DiffuseLighting.enableGuiDepthLighting();
        DrawContext lv2 = new DrawContext(this.client, this.buffers.getEntityVertexConsumers());
        if (bl2 && tick && this.client.world != null) {
            this.client.getProfiler().swap("gui");
            if (this.client.player != null) {
                float f = MathHelper.lerp(tickCounter.getTickDelta(false), this.client.player.prevNauseaIntensity, this.client.player.nauseaIntensity);
                float g = this.client.options.getDistortionEffectScale().getValue().floatValue();
                if (f > 0.0f && this.client.player.hasStatusEffect(StatusEffects.NAUSEA) && g < 1.0f) {
                    this.renderNausea(lv2, f * (1.0f - g));
                }
            }
            if (!this.client.options.hudHidden) {
                this.renderFloatingItem(lv2, tickCounter.getTickDelta(false));
            }
            this.client.inGameHud.render(lv2, tickCounter);
            RenderSystem.clear(GlConst.GL_DEPTH_BUFFER_BIT, MinecraftClient.IS_SYSTEM_MAC);
            this.client.getProfiler().pop();
        }
        if (this.client.getOverlay() != null) {
            try {
                this.client.getOverlay().render(lv2, i, j, tickCounter.getLastDuration());
            } catch (Throwable throwable) {
                CrashReport lv3 = CrashReport.create(throwable, "Rendering overlay");
                CrashReportSection lv4 = lv3.addElement("Overlay render details");
                lv4.add("Overlay name", () -> this.client.getOverlay().getClass().getCanonicalName());
                throw new CrashException(lv3);
            }
        }
        if (bl2 && this.client.currentScreen != null) {
            try {
                this.client.currentScreen.renderWithTooltip(lv2, i, j, tickCounter.getLastDuration());
            } catch (Throwable throwable) {
                CrashReport lv3 = CrashReport.create(throwable, "Rendering screen");
                CrashReportSection lv4 = lv3.addElement("Screen render details");
                lv4.add("Screen name", () -> this.client.currentScreen.getClass().getCanonicalName());
                lv4.add("Mouse location", () -> String.format(Locale.ROOT, "Scaled: (%d, %d). Absolute: (%f, %f)", i, j, this.client.mouse.getX(), this.client.mouse.getY()));
                lv4.add("Screen size", () -> String.format(Locale.ROOT, "Scaled: (%d, %d). Absolute: (%d, %d). Scale factor of %f", this.client.getWindow().getScaledWidth(), this.client.getWindow().getScaledHeight(), this.client.getWindow().getFramebufferWidth(), this.client.getWindow().getFramebufferHeight(), this.client.getWindow().getScaleFactor()));
                throw new CrashException(lv3);
            }
            try {
                if (this.client.currentScreen != null) {
                    this.client.currentScreen.updateNarrator();
                }
            } catch (Throwable throwable) {
                CrashReport lv3 = CrashReport.create(throwable, "Narrating screen");
                CrashReportSection lv4 = lv3.addElement("Screen details");
                lv4.add("Screen name", () -> this.client.currentScreen.getClass().getCanonicalName());
                throw new CrashException(lv3);
            }
        }
        if (bl2 && tick && this.client.world != null) {
            this.client.inGameHud.renderAutosaveIndicator(lv2, tickCounter);
        }
        if (bl2) {
            this.client.getProfiler().push("toasts");
            this.client.getToastManager().draw(lv2);
            this.client.getProfiler().pop();
        }
        lv2.draw();
        matrix4fStack.popMatrix();
        RenderSystem.applyModelViewMatrix();
    }

    private void updateWorldIcon() {
        if (this.hasWorldIcon || !this.client.isInSingleplayer()) {
            return;
        }
        long l = Util.getMeasuringTimeMs();
        if (l - this.lastWorldIconUpdate < 1000L) {
            return;
        }
        this.lastWorldIconUpdate = l;
        IntegratedServer lv = this.client.getServer();
        if (lv == null || lv.isStopped()) {
            return;
        }
        lv.getIconFile().ifPresent(path -> {
            if (Files.isRegularFile(path, new LinkOption[0])) {
                this.hasWorldIcon = true;
            } else {
                this.updateWorldIcon((Path)path);
            }
        });
    }

    private void updateWorldIcon(Path path) {
        if (this.client.worldRenderer.getCompletedChunkCount() > 10 && this.client.worldRenderer.isTerrainRenderComplete()) {
            NativeImage lv = ScreenshotRecorder.takeScreenshot(this.client.getFramebuffer());
            Util.getIoWorkerExecutor().execute(() -> {
                int i = lv.getWidth();
                int j = lv.getHeight();
                int k = 0;
                int l = 0;
                if (i > j) {
                    k = (i - j) / 2;
                    i = j;
                } else {
                    l = (j - i) / 2;
                    j = i;
                }
                try (NativeImage lv = new NativeImage(64, 64, false);){
                    lv.resizeSubRectTo(k, l, i, j, lv);
                    lv.writeTo(path);
                } catch (IOException iOException) {
                    LOGGER.warn("Couldn't save auto screenshot", iOException);
                } finally {
                    lv.close();
                }
            });
        }
    }

    private boolean shouldRenderBlockOutline() {
        boolean bl;
        if (!this.blockOutlineEnabled) {
            return false;
        }
        Entity lv = this.client.getCameraEntity();
        boolean bl2 = bl = lv instanceof PlayerEntity && !this.client.options.hudHidden;
        if (bl && !((PlayerEntity)lv).getAbilities().allowModifyWorld) {
            ItemStack lv2 = ((LivingEntity)lv).getMainHandStack();
            HitResult lv3 = this.client.crosshairTarget;
            if (lv3 != null && lv3.getType() == HitResult.Type.BLOCK) {
                BlockPos lv4 = ((BlockHitResult)lv3).getBlockPos();
                BlockState lv5 = this.client.world.getBlockState(lv4);
                if (this.client.interactionManager.getCurrentGameMode() == GameMode.SPECTATOR) {
                    bl = lv5.createScreenHandlerFactory(this.client.world, lv4) != null;
                } else {
                    CachedBlockPosition lv6 = new CachedBlockPosition(this.client.world, lv4, false);
                    Registry<Block> lv7 = this.client.world.getRegistryManager().get(RegistryKeys.BLOCK);
                    bl = !lv2.isEmpty() && (lv2.canBreak(lv6) || lv2.canPlaceOn(lv6));
                }
            }
        }
        return bl;
    }

    public void renderWorld(RenderTickCounter tickCounter) {
        float f = tickCounter.getTickDelta(true);
        this.lightmapTextureManager.update(f);
        if (this.client.getCameraEntity() == null) {
            this.client.setCameraEntity(this.client.player);
        }
        this.updateCrosshairTarget(f);
        this.client.getProfiler().push("center");
        boolean bl = this.shouldRenderBlockOutline();
        this.client.getProfiler().swap("camera");
        Camera lv = this.camera;
        Entity lv2 = this.client.getCameraEntity() == null ? this.client.player : this.client.getCameraEntity();
        float g = this.client.world.getTickManager().shouldSkipTick(lv2) ? 1.0f : f;
        lv.update(this.client.world, lv2, !this.client.options.getPerspective().isFirstPerson(), this.client.options.getPerspective().isFrontView(), g);
        this.viewDistance = this.client.options.getClampedViewDistance() * 16;
        double d = this.getFov(lv, f, true);
        Matrix4f matrix4f = this.getBasicProjectionMatrix(d);
        MatrixStack lv3 = new MatrixStack();
        this.tiltViewWhenHurt(lv3, lv.getLastTickDelta());
        if (this.client.options.getBobView().getValue().booleanValue()) {
            this.bobView(lv3, lv.getLastTickDelta());
        }
        matrix4f.mul(lv3.peek().getPositionMatrix());
        float h = this.client.options.getDistortionEffectScale().getValue().floatValue();
        float i = MathHelper.lerp(f, this.client.player.prevNauseaIntensity, this.client.player.nauseaIntensity) * (h * h);
        if (i > 0.0f) {
            int j = this.client.player.hasStatusEffect(StatusEffects.NAUSEA) ? 7 : 20;
            float k = 5.0f / (i * i + 5.0f) - i * 0.04f;
            k *= k;
            Vector3f vector3f = new Vector3f(0.0f, MathHelper.SQUARE_ROOT_OF_TWO / 2.0f, MathHelper.SQUARE_ROOT_OF_TWO / 2.0f);
            float l = ((float)this.ticks + f) * (float)j * ((float)Math.PI / 180);
            matrix4f.rotate(l, vector3f);
            matrix4f.scale(1.0f / k, 1.0f, 1.0f);
            matrix4f.rotate(-l, vector3f);
        }
        this.loadProjectionMatrix(matrix4f);
        Quaternionf quaternionf = lv.getRotation().conjugate(new Quaternionf());
        Matrix4f matrix4f2 = new Matrix4f().rotation(quaternionf);
        this.client.worldRenderer.setupFrustum(lv.getPos(), matrix4f2, this.getBasicProjectionMatrix(Math.max(d, (double)this.client.options.getFov().getValue().intValue())));
        this.client.worldRenderer.render(tickCounter, bl, lv, this, this.lightmapTextureManager, matrix4f2, matrix4f);
        this.client.getProfiler().swap("hand");
        if (this.renderHand) {
            RenderSystem.clear(GlConst.GL_DEPTH_BUFFER_BIT, MinecraftClient.IS_SYSTEM_MAC);
            this.renderHand(lv, f, matrix4f2);
        }
        this.client.getProfiler().pop();
    }

    public void reset() {
        this.floatingItem = null;
        this.mapRenderer.clearStateTextures();
        this.camera.reset();
        this.hasWorldIcon = false;
    }

    public MapRenderer getMapRenderer() {
        return this.mapRenderer;
    }

    public void showFloatingItem(ItemStack floatingItem) {
        this.floatingItem = floatingItem;
        this.floatingItemTimeLeft = 40;
        this.floatingItemWidth = this.random.nextFloat() * 2.0f - 1.0f;
        this.floatingItemHeight = this.random.nextFloat() * 2.0f - 1.0f;
    }

    private void renderFloatingItem(DrawContext context, float tickDelta) {
        if (this.floatingItem == null || this.floatingItemTimeLeft <= 0) {
            return;
        }
        int i = 40 - this.floatingItemTimeLeft;
        float g = ((float)i + tickDelta) / 40.0f;
        float h = g * g;
        float j = g * h;
        float k = 10.25f * j * h - 24.95f * h * h + 25.5f * j - 13.8f * h + 4.0f * g;
        float l = k * (float)Math.PI;
        float m = this.floatingItemWidth * (float)(context.getScaledWindowWidth() / 4);
        float n = this.floatingItemHeight * (float)(context.getScaledWindowHeight() / 4);
        MatrixStack lv = new MatrixStack();
        lv.push();
        lv.translate((float)(context.getScaledWindowWidth() / 2) + m * MathHelper.abs(MathHelper.sin(l * 2.0f)), (float)(context.getScaledWindowHeight() / 2) + n * MathHelper.abs(MathHelper.sin(l * 2.0f)), -50.0f);
        float o = 50.0f + 175.0f * MathHelper.sin(l);
        lv.scale(o, -o, o);
        lv.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(900.0f * MathHelper.abs(MathHelper.sin(l))));
        lv.multiply(RotationAxis.POSITIVE_X.rotationDegrees(6.0f * MathHelper.cos(g * 8.0f)));
        lv.multiply(RotationAxis.POSITIVE_Z.rotationDegrees(6.0f * MathHelper.cos(g * 8.0f)));
        context.draw(() -> this.client.getItemRenderer().renderItem(this.floatingItem, ModelTransformationMode.FIXED, 0xF000F0, OverlayTexture.DEFAULT_UV, lv, context.getVertexConsumers(), this.client.world, 0));
        lv.pop();
    }

    private void renderNausea(DrawContext context, float distortionStrength) {
        int i = context.getScaledWindowWidth();
        int j = context.getScaledWindowHeight();
        context.getMatrices().push();
        float g = MathHelper.lerp(distortionStrength, 2.0f, 1.0f);
        context.getMatrices().translate((float)i / 2.0f, (float)j / 2.0f, 0.0f);
        context.getMatrices().scale(g, g, g);
        context.getMatrices().translate((float)(-i) / 2.0f, (float)(-j) / 2.0f, 0.0f);
        float h = 0.2f * distortionStrength;
        float k = 0.4f * distortionStrength;
        float l = 0.2f * distortionStrength;
        RenderSystem.disableDepthTest();
        RenderSystem.depthMask(false);
        RenderSystem.enableBlend();
        RenderSystem.blendFuncSeparate(GlStateManager.SrcFactor.ONE, GlStateManager.DstFactor.ONE, GlStateManager.SrcFactor.ONE, GlStateManager.DstFactor.ONE);
        context.setShaderColor(h, k, l, 1.0f);
        context.drawTexture(NAUSEA_OVERLAY, 0, 0, -90, 0.0f, 0.0f, i, j, i, j);
        context.setShaderColor(1.0f, 1.0f, 1.0f, 1.0f);
        RenderSystem.defaultBlendFunc();
        RenderSystem.disableBlend();
        RenderSystem.depthMask(true);
        RenderSystem.enableDepthTest();
        context.getMatrices().pop();
    }

    public MinecraftClient getClient() {
        return this.client;
    }

    public float getSkyDarkness(float tickDelta) {
        return MathHelper.lerp(tickDelta, this.lastSkyDarkness, this.skyDarkness);
    }

    public float getViewDistance() {
        return this.viewDistance;
    }

    public Camera getCamera() {
        return this.camera;
    }

    public LightmapTextureManager getLightmapTextureManager() {
        return this.lightmapTextureManager;
    }

    public OverlayTexture getOverlayTexture() {
        return this.overlayTexture;
    }

    @Nullable
    public static ShaderProgram getPositionProgram() {
        return positionProgram;
    }

    @Nullable
    public static ShaderProgram getPositionColorProgram() {
        return positionColorProgram;
    }

    @Nullable
    public static ShaderProgram getPositionTexProgram() {
        return positionTexProgram;
    }

    @Nullable
    public static ShaderProgram getPositionTexColorProgram() {
        return positionTexColorProgram;
    }

    @Nullable
    public static ShaderProgram getParticleProgram() {
        return particleProgram;
    }

    @Nullable
    public static ShaderProgram getPositionColorLightmapProgram() {
        return positionColorLightmapProgram;
    }

    @Nullable
    public static ShaderProgram getPositionColorTexLightmapProgram() {
        return positionColorTexLightmapProgram;
    }

    @Nullable
    public static ShaderProgram getRenderTypeSolidProgram() {
        return renderTypeSolidProgram;
    }

    @Nullable
    public static ShaderProgram getRenderTypeCutoutMippedProgram() {
        return renderTypeCutoutMippedProgram;
    }

    @Nullable
    public static ShaderProgram getRenderTypeCutoutProgram() {
        return renderTypeCutoutProgram;
    }

    @Nullable
    public static ShaderProgram getRenderTypeTranslucentProgram() {
        return renderTypeTranslucentProgram;
    }

    @Nullable
    public static ShaderProgram getRenderTypeTranslucentMovingBlockProgram() {
        return renderTypeTranslucentMovingBlockProgram;
    }

    @Nullable
    public static ShaderProgram getRenderTypeArmorCutoutNoCullProgram() {
        return renderTypeArmorCutoutNoCullProgram;
    }

    @Nullable
    public static ShaderProgram getRenderTypeEntitySolidProgram() {
        return renderTypeEntitySolidProgram;
    }

    @Nullable
    public static ShaderProgram getRenderTypeEntityCutoutProgram() {
        return renderTypeEntityCutoutProgram;
    }

    @Nullable
    public static ShaderProgram getRenderTypeEntityCutoutNoNullProgram() {
        return renderTypeEntityCutoutNoNullProgram;
    }

    @Nullable
    public static ShaderProgram getRenderTypeEntityCutoutNoNullZOffsetProgram() {
        return renderTypeEntityCutoutNoNullZOffsetProgram;
    }

    @Nullable
    public static ShaderProgram getRenderTypeItemEntityTranslucentCullProgram() {
        return renderTypeItemEntityTranslucentCullProgram;
    }

    @Nullable
    public static ShaderProgram getRenderTypeEntityTranslucentCullProgram() {
        return renderTypeEntityTranslucentCullProgram;
    }

    @Nullable
    public static ShaderProgram getRenderTypeEntityTranslucentProgram() {
        return renderTypeEntityTranslucentProgram;
    }

    @Nullable
    public static ShaderProgram getRenderTypeEntityTranslucentEmissiveProgram() {
        return renderTypeEntityTranslucentEmissiveProgram;
    }

    @Nullable
    public static ShaderProgram getRenderTypeEntitySmoothCutoutProgram() {
        return renderTypeEntitySmoothCutoutProgram;
    }

    @Nullable
    public static ShaderProgram getRenderTypeBeaconBeamProgram() {
        return renderTypeBeaconBeamProgram;
    }

    @Nullable
    public static ShaderProgram getRenderTypeEntityDecalProgram() {
        return renderTypeEntityDecalProgram;
    }

    @Nullable
    public static ShaderProgram getRenderTypeEntityNoOutlineProgram() {
        return renderTypeEntityNoOutlineProgram;
    }

    @Nullable
    public static ShaderProgram getRenderTypeEntityShadowProgram() {
        return renderTypeEntityShadowProgram;
    }

    @Nullable
    public static ShaderProgram getRenderTypeEntityAlphaProgram() {
        return renderTypeEntityAlphaProgram;
    }

    @Nullable
    public static ShaderProgram getRenderTypeEyesProgram() {
        return renderTypeEyesProgram;
    }

    @Nullable
    public static ShaderProgram getRenderTypeEnergySwirlProgram() {
        return renderTypeEnergySwirlProgram;
    }

    @Nullable
    public static ShaderProgram getRenderTypeBreezeWindProgram() {
        return renderTypeBreezeWindProgram;
    }

    @Nullable
    public static ShaderProgram getRenderTypeLeashProgram() {
        return renderTypeLeashProgram;
    }

    @Nullable
    public static ShaderProgram getRenderTypeWaterMaskProgram() {
        return renderTypeWaterMaskProgram;
    }

    @Nullable
    public static ShaderProgram getRenderTypeOutlineProgram() {
        return renderTypeOutlineProgram;
    }

    @Nullable
    public static ShaderProgram getRenderTypeArmorGlintProgram() {
        return renderTypeArmorGlintProgram;
    }

    @Nullable
    public static ShaderProgram getRenderTypeArmorEntityGlintProgram() {
        return renderTypeArmorEntityGlintProgram;
    }

    @Nullable
    public static ShaderProgram getRenderTypeGlintTranslucentProgram() {
        return renderTypeGlintTranslucentProgram;
    }

    @Nullable
    public static ShaderProgram getRenderTypeGlintProgram() {
        return renderTypeGlintProgram;
    }

    @Nullable
    public static ShaderProgram getRenderTypeGlintDirectProgram() {
        return renderTypeGlintDirectProgram;
    }

    @Nullable
    public static ShaderProgram getRenderTypeEntityGlintProgram() {
        return renderTypeEntityGlintProgram;
    }

    @Nullable
    public static ShaderProgram getRenderTypeEntityGlintDirectProgram() {
        return renderTypeEntityGlintDirectProgram;
    }

    @Nullable
    public static ShaderProgram getRenderTypeTextProgram() {
        return renderTypeTextProgram;
    }

    @Nullable
    public static ShaderProgram getRenderTypeTextBackgroundProgram() {
        return renderTypeTextBackgroundProgram;
    }

    @Nullable
    public static ShaderProgram getRenderTypeTextIntensityProgram() {
        return renderTypeTextIntensityProgram;
    }

    @Nullable
    public static ShaderProgram getRenderTypeTextSeeThroughProgram() {
        return renderTypeTextSeeThroughProgram;
    }

    @Nullable
    public static ShaderProgram getRenderTypeTextBackgroundSeeThroughProgram() {
        return renderTypeTextBackgroundSeeThroughProgram;
    }

    @Nullable
    public static ShaderProgram getRenderTypeTextIntensitySeeThroughProgram() {
        return renderTypeTextIntensitySeeThroughProgram;
    }

    @Nullable
    public static ShaderProgram getRenderTypeLightningProgram() {
        return renderTypeLightningProgram;
    }

    @Nullable
    public static ShaderProgram getRenderTypeTripwireProgram() {
        return renderTypeTripwireProgram;
    }

    @Nullable
    public static ShaderProgram getRenderTypeEndPortalProgram() {
        return renderTypeEndPortalProgram;
    }

    @Nullable
    public static ShaderProgram getRenderTypeEndGatewayProgram() {
        return renderTypeEndGatewayProgram;
    }

    @Nullable
    public static ShaderProgram getRenderTypeCloudsProgram() {
        return renderTypeCloudsProgram;
    }

    @Nullable
    public static ShaderProgram getRenderTypeLinesProgram() {
        return renderTypeLinesProgram;
    }

    @Nullable
    public static ShaderProgram getRenderTypeCrumblingProgram() {
        return renderTypeCrumblingProgram;
    }

    @Nullable
    public static ShaderProgram getRenderTypeGuiProgram() {
        return renderTypeGuiProgram;
    }

    @Nullable
    public static ShaderProgram getRenderTypeGuiOverlayProgram() {
        return renderTypeGuiOverlayProgram;
    }

    @Nullable
    public static ShaderProgram getRenderTypeGuiTextHighlightProgram() {
        return renderTypeGuiTextHighlightProgram;
    }

    @Nullable
    public static ShaderProgram getRenderTypeGuiGhostRecipeOverlayProgram() {
        return renderTypeGuiGhostRecipeOverlayProgram;
    }

    @Environment(value=EnvType.CLIENT)
    public record CachedResourceFactory(ResourceFactory original, Map<Identifier, Resource> cache) implements ResourceFactory
    {
        @Override
        public Optional<Resource> getResource(Identifier id) {
            Resource lv = this.cache.get(id);
            if (lv != null) {
                return Optional.of(lv);
            }
            return this.original.getResource(id);
        }
    }
}

