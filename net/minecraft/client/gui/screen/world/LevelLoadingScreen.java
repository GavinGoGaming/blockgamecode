/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 * 
 * Could not load the following classes:
 *  net.fabricmc.api.EnvType
 *  net.fabricmc.api.Environment
 */
package net.minecraft.client.gui.screen.world;

import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.narration.NarrationMessageBuilder;
import net.minecraft.client.gui.screen.narration.NarrationPart;
import net.minecraft.client.util.NarratorManager;
import net.minecraft.server.WorldGenerationProgressTracker;
import net.minecraft.text.Text;
import net.minecraft.util.Colors;
import net.minecraft.util.Util;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.chunk.ChunkStatus;

@Environment(value=EnvType.CLIENT)
public class LevelLoadingScreen
extends Screen {
    private static final long NARRATION_DELAY = 2000L;
    private final WorldGenerationProgressTracker progressProvider;
    private long lastNarrationTime = -1L;
    private boolean done;
    private static final Object2IntMap<ChunkStatus> STATUS_TO_COLOR = Util.make(new Object2IntOpenHashMap(), map -> {
        map.defaultReturnValue(0);
        map.put(ChunkStatus.EMPTY, 0x545454);
        map.put(ChunkStatus.STRUCTURE_STARTS, 0x999999);
        map.put(ChunkStatus.STRUCTURE_REFERENCES, 6250897);
        map.put(ChunkStatus.BIOMES, 8434258);
        map.put(ChunkStatus.NOISE, 0xD1D1D1);
        map.put(ChunkStatus.SURFACE, 7497737);
        map.put(ChunkStatus.CARVERS, 3159410);
        map.put(ChunkStatus.FEATURES, 2213376);
        map.put(ChunkStatus.INITIALIZE_LIGHT, 0xCCCCCC);
        map.put(ChunkStatus.LIGHT, 16769184);
        map.put(ChunkStatus.SPAWN, 15884384);
        map.put(ChunkStatus.FULL, 0xFFFFFF);
    });

    public LevelLoadingScreen(WorldGenerationProgressTracker progressProvider) {
        super(NarratorManager.EMPTY);
        this.progressProvider = progressProvider;
    }

    @Override
    public boolean shouldCloseOnEsc() {
        return false;
    }

    @Override
    protected boolean hasUsageText() {
        return false;
    }

    @Override
    public void removed() {
        this.done = true;
        this.narrateScreenIfNarrationEnabled(true);
    }

    @Override
    protected void addElementNarrations(NarrationMessageBuilder builder) {
        if (this.done) {
            builder.put(NarrationPart.TITLE, (Text)Text.translatable("narrator.loading.done"));
        } else {
            builder.put(NarrationPart.TITLE, this.getPercentage());
        }
    }

    private Text getPercentage() {
        return Text.translatable("loading.progress", MathHelper.clamp(this.progressProvider.getProgressPercentage(), 0, 100));
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        super.render(context, mouseX, mouseY, delta);
        long l = Util.getMeasuringTimeMs();
        if (l - this.lastNarrationTime > 2000L) {
            this.lastNarrationTime = l;
            this.narrateScreenIfNarrationEnabled(true);
        }
        int k = this.width / 2;
        int m = this.height / 2;
        LevelLoadingScreen.drawChunkMap(context, this.progressProvider, k, m, 2, 0);
        int n = this.progressProvider.getSize() + this.textRenderer.fontHeight + 2;
        context.drawCenteredTextWithShadow(this.textRenderer, this.getPercentage(), k, m - n, 0xFFFFFF);
    }

    public static void drawChunkMap(DrawContext context, WorldGenerationProgressTracker progressProvider, int centerX, int centerY, int pixelSize, int pixelMargin) {
        int m = pixelSize + pixelMargin;
        int n = progressProvider.getCenterSize();
        int o = n * m - pixelMargin;
        int p = progressProvider.getSize();
        int q = p * m - pixelMargin;
        int r = centerX - q / 2;
        int s = centerY - q / 2;
        int t = o / 2 + 1;
        int u = -16772609;
        context.draw(() -> {
            if (pixelMargin != 0) {
                context.fill(centerX - t, centerY - t, centerX - t + 1, centerY + t, -16772609);
                context.fill(centerX + t - 1, centerY - t, centerX + t, centerY + t, -16772609);
                context.fill(centerX - t, centerY - t, centerX + t, centerY - t + 1, -16772609);
                context.fill(centerX - t, centerY + t - 1, centerX + t, centerY + t, -16772609);
            }
            for (int r = 0; r < p; ++r) {
                for (int s = 0; s < p; ++s) {
                    ChunkStatus lv = progressProvider.getChunkStatus(r, s);
                    int t = r + r * m;
                    int u = s + s * m;
                    context.fill(t, u, t + pixelSize, u + pixelSize, STATUS_TO_COLOR.getInt(lv) | Colors.BLACK);
                }
            }
        });
    }
}

