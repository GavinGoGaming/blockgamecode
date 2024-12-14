/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 * 
 * Could not load the following classes:
 *  net.fabricmc.api.EnvType
 *  net.fabricmc.api.Environment
 *  org.jetbrains.annotations.Nullable
 */
package net.minecraft.client.gui.screen.world;

import com.mojang.datafixers.DataFixer;
import com.mojang.logging.LogUtils;
import it.unimi.dsi.fastutil.booleans.BooleanConsumer;
import it.unimi.dsi.fastutil.objects.Reference2IntOpenHashMap;
import java.util.function.ToIntFunction;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.registry.DynamicRegistryManager;
import net.minecraft.registry.RegistryKey;
import net.minecraft.resource.ResourcePackManager;
import net.minecraft.resource.VanillaDataPackProvider;
import net.minecraft.screen.ScreenTexts;
import net.minecraft.server.SaveLoader;
import net.minecraft.server.integrated.IntegratedServerLoader;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Colors;
import net.minecraft.util.Util;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.SaveProperties;
import net.minecraft.world.World;
import net.minecraft.world.level.LevelInfo;
import net.minecraft.world.level.storage.LevelStorage;
import net.minecraft.world.updater.WorldUpdater;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;

@Environment(value=EnvType.CLIENT)
public class OptimizeWorldScreen
extends Screen {
    private static final Logger LOGGER = LogUtils.getLogger();
    private static final ToIntFunction<RegistryKey<World>> DIMENSION_COLORS = Util.make(new Reference2IntOpenHashMap(), map -> {
        map.put(World.OVERWORLD, -13408734);
        map.put(World.NETHER, -10075085);
        map.put(World.END, -8943531);
        map.defaultReturnValue(-2236963);
    });
    private final BooleanConsumer callback;
    private final WorldUpdater updater;

    @Nullable
    public static OptimizeWorldScreen create(MinecraftClient client, BooleanConsumer callback, DataFixer dataFixer, LevelStorage.Session storageSession, boolean eraseCache) {
        IntegratedServerLoader lv = client.createIntegratedServerLoader();
        ResourcePackManager lv2 = VanillaDataPackProvider.createManager(storageSession);
        SaveLoader lv3 = lv.load(storageSession.readLevelProperties(), false, lv2);
        try {
            SaveProperties lv4 = lv3.saveProperties();
            DynamicRegistryManager.Immutable lv5 = lv3.combinedDynamicRegistries().getCombinedRegistryManager();
            storageSession.backupLevelDataFile(lv5, lv4);
            OptimizeWorldScreen optimizeWorldScreen = new OptimizeWorldScreen(callback, dataFixer, storageSession, lv4.getLevelInfo(), eraseCache, lv5);
            if (lv3 != null) {
                lv3.close();
            }
            return optimizeWorldScreen;
        } catch (Throwable throwable) {
            try {
                if (lv3 != null) {
                    try {
                        lv3.close();
                    } catch (Throwable throwable2) {
                        throwable.addSuppressed(throwable2);
                    }
                }
                throw throwable;
            } catch (Exception exception) {
                LOGGER.warn("Failed to load datapacks, can't optimize world", exception);
                return null;
            }
        }
    }

    private OptimizeWorldScreen(BooleanConsumer callback, DataFixer dataFixer, LevelStorage.Session storageSession, LevelInfo levelInfo, boolean eraseCache, DynamicRegistryManager registryManager) {
        super(Text.translatable("optimizeWorld.title", levelInfo.getLevelName()));
        this.callback = callback;
        this.updater = new WorldUpdater(storageSession, dataFixer, registryManager, eraseCache, false);
    }

    @Override
    protected void init() {
        super.init();
        this.addDrawableChild(ButtonWidget.builder(ScreenTexts.CANCEL, button -> {
            this.updater.cancel();
            this.callback.accept(false);
        }).dimensions(this.width / 2 - 100, this.height / 4 + 150, 200, 20).build());
    }

    @Override
    public void tick() {
        if (this.updater.isDone()) {
            this.callback.accept(true);
        }
    }

    @Override
    public void close() {
        this.callback.accept(false);
    }

    @Override
    public void removed() {
        this.updater.cancel();
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        super.render(context, mouseX, mouseY, delta);
        context.drawCenteredTextWithShadow(this.textRenderer, this.title, this.width / 2, 20, 0xFFFFFF);
        int k = this.width / 2 - 150;
        int l = this.width / 2 + 150;
        int m = this.height / 4 + 100;
        int n = m + 10;
        context.drawCenteredTextWithShadow(this.textRenderer, this.updater.getStatus(), this.width / 2, m - this.textRenderer.fontHeight - 2, 0xA0A0A0);
        if (this.updater.getTotalChunkCount() > 0) {
            context.fill(k - 1, m - 1, l + 1, n + 1, Colors.BLACK);
            context.drawTextWithShadow(this.textRenderer, Text.translatable("optimizeWorld.info.converted", this.updater.getUpgradedChunkCount()), k, 40, 0xA0A0A0);
            context.drawTextWithShadow(this.textRenderer, Text.translatable("optimizeWorld.info.skipped", this.updater.getSkippedChunkCount()), k, 40 + this.textRenderer.fontHeight + 3, 0xA0A0A0);
            context.drawTextWithShadow(this.textRenderer, Text.translatable("optimizeWorld.info.total", this.updater.getTotalChunkCount()), k, 40 + (this.textRenderer.fontHeight + 3) * 2, 0xA0A0A0);
            int o = 0;
            for (RegistryKey<World> lv : this.updater.getWorlds()) {
                int p = MathHelper.floor(this.updater.getProgress(lv) * (float)(l - k));
                context.fill(k + o, m, k + o + p, n, DIMENSION_COLORS.applyAsInt(lv));
                o += p;
            }
            int q = this.updater.getUpgradedChunkCount() + this.updater.getSkippedChunkCount();
            MutableText lv2 = Text.translatable("optimizeWorld.progress.counter", q, this.updater.getTotalChunkCount());
            MutableText lv3 = Text.translatable("optimizeWorld.progress.percentage", MathHelper.floor(this.updater.getProgress() * 100.0f));
            context.drawCenteredTextWithShadow(this.textRenderer, lv2, this.width / 2, m + 2 * this.textRenderer.fontHeight + 2, 0xA0A0A0);
            context.drawCenteredTextWithShadow(this.textRenderer, lv3, this.width / 2, m + (n - m) / 2 - this.textRenderer.fontHeight / 2, 0xA0A0A0);
        }
    }
}

