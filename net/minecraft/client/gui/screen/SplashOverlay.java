/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 * 
 * Could not load the following classes:
 *  net.fabricmc.api.EnvType
 *  net.fabricmc.api.Environment
 */
package net.minecraft.client.gui.screen;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.IntSupplier;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Overlay;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.resource.metadata.TextureResourceMetadata;
import net.minecraft.client.texture.NativeImage;
import net.minecraft.client.texture.ResourceTexture;
import net.minecraft.resource.DefaultResourcePack;
import net.minecraft.resource.InputSupplier;
import net.minecraft.resource.ResourceManager;
import net.minecraft.resource.ResourceReload;
import net.minecraft.resource.ResourceType;
import net.minecraft.util.Identifier;
import net.minecraft.util.Util;
import net.minecraft.util.math.ColorHelper;
import net.minecraft.util.math.MathHelper;

@Environment(value=EnvType.CLIENT)
public class SplashOverlay
extends Overlay {
    static final Identifier LOGO = Identifier.ofVanilla("textures/gui/title/mojangstudios.png");
    private static final int MOJANG_RED = ColorHelper.Argb.getArgb(255, 239, 50, 61);
    private static final int MONOCHROME_BLACK = ColorHelper.Argb.getArgb(255, 0, 0, 0);
    private static final IntSupplier BRAND_ARGB = () -> MinecraftClient.getInstance().options.getMonochromeLogo().getValue() != false ? MONOCHROME_BLACK : MOJANG_RED;
    private static final int field_32251 = 240;
    private static final float LOGO_RIGHT_HALF_V = 60.0f;
    private static final int field_32253 = 60;
    private static final int field_32254 = 120;
    private static final float LOGO_OVERLAP = 0.0625f;
    private static final float PROGRESS_LERP_DELTA = 0.95f;
    public static final long RELOAD_COMPLETE_FADE_DURATION = 1000L;
    public static final long RELOAD_START_FADE_DURATION = 500L;
    private final MinecraftClient client;
    private final ResourceReload reload;
    private final Consumer<Optional<Throwable>> exceptionHandler;
    private final boolean reloading;
    private float progress;
    private long reloadCompleteTime = -1L;
    private long reloadStartTime = -1L;

    public SplashOverlay(MinecraftClient client, ResourceReload monitor, Consumer<Optional<Throwable>> exceptionHandler, boolean reloading) {
        this.client = client;
        this.reload = monitor;
        this.exceptionHandler = exceptionHandler;
        this.reloading = reloading;
    }

    public static void init(MinecraftClient client) {
        client.getTextureManager().registerTexture(LOGO, new LogoTexture());
    }

    private static int withAlpha(int color, int alpha) {
        return color & 0xFFFFFF | alpha << 24;
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        float o;
        int n;
        float h;
        int k = context.getScaledWindowWidth();
        int l = context.getScaledWindowHeight();
        long m = Util.getMeasuringTimeMs();
        if (this.reloading && this.reloadStartTime == -1L) {
            this.reloadStartTime = m;
        }
        float g = this.reloadCompleteTime > -1L ? (float)(m - this.reloadCompleteTime) / 1000.0f : -1.0f;
        float f = h = this.reloadStartTime > -1L ? (float)(m - this.reloadStartTime) / 500.0f : -1.0f;
        if (g >= 1.0f) {
            if (this.client.currentScreen != null) {
                this.client.currentScreen.render(context, 0, 0, delta);
            }
            n = MathHelper.ceil((1.0f - MathHelper.clamp(g - 1.0f, 0.0f, 1.0f)) * 255.0f);
            context.fill(RenderLayer.getGuiOverlay(), 0, 0, k, l, SplashOverlay.withAlpha(BRAND_ARGB.getAsInt(), n));
            o = 1.0f - MathHelper.clamp(g - 1.0f, 0.0f, 1.0f);
        } else if (this.reloading) {
            if (this.client.currentScreen != null && h < 1.0f) {
                this.client.currentScreen.render(context, mouseX, mouseY, delta);
            }
            n = MathHelper.ceil(MathHelper.clamp((double)h, 0.15, 1.0) * 255.0);
            context.fill(RenderLayer.getGuiOverlay(), 0, 0, k, l, SplashOverlay.withAlpha(BRAND_ARGB.getAsInt(), n));
            o = MathHelper.clamp(h, 0.0f, 1.0f);
        } else {
            n = BRAND_ARGB.getAsInt();
            float p = (float)(n >> 16 & 0xFF) / 255.0f;
            float q = (float)(n >> 8 & 0xFF) / 255.0f;
            float r = (float)(n & 0xFF) / 255.0f;
            GlStateManager._clearColor(p, q, r, 1.0f);
            GlStateManager._clear(16384, MinecraftClient.IS_SYSTEM_MAC);
            o = 1.0f;
        }
        n = (int)((double)context.getScaledWindowWidth() * 0.5);
        int s = (int)((double)context.getScaledWindowHeight() * 0.5);
        double d = Math.min((double)context.getScaledWindowWidth() * 0.75, (double)context.getScaledWindowHeight()) * 0.25;
        int t = (int)(d * 0.5);
        double e = d * 4.0;
        int u = (int)(e * 0.5);
        RenderSystem.disableDepthTest();
        RenderSystem.depthMask(false);
        RenderSystem.enableBlend();
        RenderSystem.blendFunc(770, 1);
        context.setShaderColor(1.0f, 1.0f, 1.0f, o);
        context.drawTexture(LOGO, n - u, s - t, u, (int)d, -0.0625f, 0.0f, 120, 60, 120, 120);
        context.drawTexture(LOGO, n, s - t, u, (int)d, 0.0625f, 60.0f, 120, 60, 120, 120);
        context.setShaderColor(1.0f, 1.0f, 1.0f, 1.0f);
        RenderSystem.defaultBlendFunc();
        RenderSystem.disableBlend();
        RenderSystem.depthMask(true);
        RenderSystem.enableDepthTest();
        int v = (int)((double)context.getScaledWindowHeight() * 0.8325);
        float w = this.reload.getProgress();
        this.progress = MathHelper.clamp(this.progress * 0.95f + w * 0.050000012f, 0.0f, 1.0f);
        if (g < 1.0f) {
            this.renderProgressBar(context, k / 2 - u, v - 5, k / 2 + u, v + 5, 1.0f - MathHelper.clamp(g, 0.0f, 1.0f));
        }
        if (g >= 2.0f) {
            this.client.setOverlay(null);
        }
        if (this.reloadCompleteTime == -1L && this.reload.isComplete() && (!this.reloading || h >= 2.0f)) {
            try {
                this.reload.throwException();
                this.exceptionHandler.accept(Optional.empty());
            } catch (Throwable throwable) {
                this.exceptionHandler.accept(Optional.of(throwable));
            }
            this.reloadCompleteTime = Util.getMeasuringTimeMs();
            if (this.client.currentScreen != null) {
                this.client.currentScreen.init(this.client, context.getScaledWindowWidth(), context.getScaledWindowHeight());
            }
        }
    }

    private void renderProgressBar(DrawContext arg, int minX, int minY, int maxX, int maxY, float opacity) {
        int m = MathHelper.ceil((float)(maxX - minX - 2) * this.progress);
        int n = Math.round(opacity * 255.0f);
        int o = ColorHelper.Argb.getArgb(n, 255, 255, 255);
        arg.fill(minX + 2, minY + 2, minX + m, maxY - 2, o);
        arg.fill(minX + 1, minY, maxX - 1, minY + 1, o);
        arg.fill(minX + 1, maxY, maxX - 1, maxY - 1, o);
        arg.fill(minX, minY, minX + 1, maxY, o);
        arg.fill(maxX, minY, maxX - 1, maxY, o);
    }

    @Override
    public boolean pausesGame() {
        return true;
    }

    @Environment(value=EnvType.CLIENT)
    static class LogoTexture
    extends ResourceTexture {
        public LogoTexture() {
            super(LOGO);
        }

        @Override
        protected ResourceTexture.TextureData loadTextureData(ResourceManager resourceManager) {
            ResourceTexture.TextureData textureData;
            block9: {
                DefaultResourcePack lv = MinecraftClient.getInstance().getDefaultResourcePack();
                InputSupplier<InputStream> lv2 = lv.open(ResourceType.CLIENT_RESOURCES, LOGO);
                if (lv2 == null) {
                    return new ResourceTexture.TextureData(new FileNotFoundException(LOGO.toString()));
                }
                InputStream inputStream = lv2.get();
                try {
                    textureData = new ResourceTexture.TextureData(new TextureResourceMetadata(true, true), NativeImage.read(inputStream));
                    if (inputStream == null) break block9;
                } catch (Throwable throwable) {
                    try {
                        if (inputStream != null) {
                            try {
                                inputStream.close();
                            } catch (Throwable throwable2) {
                                throwable.addSuppressed(throwable2);
                            }
                        }
                        throw throwable;
                    } catch (IOException iOException) {
                        return new ResourceTexture.TextureData(iOException);
                    }
                }
                inputStream.close();
            }
            return textureData;
        }
    }
}

