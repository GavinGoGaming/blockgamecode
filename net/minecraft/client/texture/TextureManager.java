/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 * 
 * Could not load the following classes:
 *  net.fabricmc.api.EnvType
 *  net.fabricmc.api.Environment
 */
package net.minecraft.client.texture;

import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.logging.LogUtils;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.FileAttribute;
import java.util.Iterator;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.TitleScreen;
import net.minecraft.client.realms.gui.screen.BuyRealmsScreen;
import net.minecraft.client.texture.AbstractTexture;
import net.minecraft.client.texture.AsyncTexture;
import net.minecraft.client.texture.DynamicTexture;
import net.minecraft.client.texture.MissingSprite;
import net.minecraft.client.texture.NativeImageBackedTexture;
import net.minecraft.client.texture.ResourceTexture;
import net.minecraft.client.texture.TextureTickListener;
import net.minecraft.resource.ResourceManager;
import net.minecraft.resource.ResourceReloader;
import net.minecraft.util.Identifier;
import net.minecraft.util.crash.CrashException;
import net.minecraft.util.crash.CrashReport;
import net.minecraft.util.crash.CrashReportSection;
import net.minecraft.util.profiler.Profiler;
import org.slf4j.Logger;

@Environment(value=EnvType.CLIENT)
public class TextureManager
implements ResourceReloader,
TextureTickListener,
AutoCloseable {
    private static final Logger LOGGER = LogUtils.getLogger();
    public static final Identifier MISSING_IDENTIFIER = Identifier.ofVanilla("");
    private final Map<Identifier, AbstractTexture> textures = Maps.newHashMap();
    private final Set<TextureTickListener> tickListeners = Sets.newHashSet();
    private final Map<String, Integer> dynamicIdCounters = Maps.newHashMap();
    private final ResourceManager resourceContainer;

    public TextureManager(ResourceManager resourceManager) {
        this.resourceContainer = resourceManager;
    }

    public void bindTexture(Identifier id) {
        if (!RenderSystem.isOnRenderThread()) {
            RenderSystem.recordRenderCall(() -> this.bindTextureInner(id));
        } else {
            this.bindTextureInner(id);
        }
    }

    private void bindTextureInner(Identifier id) {
        AbstractTexture lv = this.textures.get(id);
        if (lv == null) {
            lv = new ResourceTexture(id);
            this.registerTexture(id, lv);
        }
        lv.bindTexture();
    }

    public void registerTexture(Identifier id, AbstractTexture texture) {
        AbstractTexture lv = this.textures.put(id, texture = this.loadTexture(id, texture));
        if (lv != texture) {
            if (lv != null && lv != MissingSprite.getMissingSpriteTexture()) {
                this.closeTexture(id, lv);
            }
            if (texture instanceof TextureTickListener) {
                this.tickListeners.add((TextureTickListener)((Object)texture));
            }
        }
    }

    private void closeTexture(Identifier id, AbstractTexture texture) {
        if (texture != MissingSprite.getMissingSpriteTexture()) {
            this.tickListeners.remove(texture);
            try {
                texture.close();
            } catch (Exception exception) {
                LOGGER.warn("Failed to close texture {}", (Object)id, (Object)exception);
            }
        }
        texture.clearGlId();
    }

    private AbstractTexture loadTexture(Identifier id, AbstractTexture texture) {
        try {
            texture.load(this.resourceContainer);
            return texture;
        } catch (IOException iOException) {
            if (id != MISSING_IDENTIFIER) {
                LOGGER.warn("Failed to load texture: {}", (Object)id, (Object)iOException);
            }
            return MissingSprite.getMissingSpriteTexture();
        } catch (Throwable throwable) {
            CrashReport lv = CrashReport.create(throwable, "Registering texture");
            CrashReportSection lv2 = lv.addElement("Resource location being registered");
            lv2.add("Resource location", id);
            lv2.add("Texture object class", () -> texture.getClass().getName());
            throw new CrashException(lv);
        }
    }

    public AbstractTexture getTexture(Identifier id) {
        AbstractTexture lv = this.textures.get(id);
        if (lv == null) {
            lv = new ResourceTexture(id);
            this.registerTexture(id, lv);
        }
        return lv;
    }

    public AbstractTexture getOrDefault(Identifier id, AbstractTexture fallback) {
        return this.textures.getOrDefault(id, fallback);
    }

    public Identifier registerDynamicTexture(String prefix, NativeImageBackedTexture texture) {
        Integer integer = this.dynamicIdCounters.get(prefix);
        if (integer == null) {
            integer = 1;
        } else {
            Integer n = integer;
            integer = integer + 1;
        }
        this.dynamicIdCounters.put(prefix, integer);
        Identifier lv = Identifier.ofVanilla(String.format(Locale.ROOT, "dynamic/%s_%d", prefix, integer));
        this.registerTexture(lv, texture);
        return lv;
    }

    public CompletableFuture<Void> loadTextureAsync(Identifier id, Executor executor) {
        if (!this.textures.containsKey(id)) {
            AsyncTexture lv = new AsyncTexture(this.resourceContainer, id, executor);
            this.textures.put(id, lv);
            return lv.getLoadCompleteFuture().thenRunAsync(() -> this.registerTexture(id, lv), TextureManager::runOnRenderThread);
        }
        return CompletableFuture.completedFuture(null);
    }

    private static void runOnRenderThread(Runnable runnable) {
        MinecraftClient.getInstance().execute(() -> RenderSystem.recordRenderCall(runnable::run));
    }

    @Override
    public void tick() {
        for (TextureTickListener lv : this.tickListeners) {
            lv.tick();
        }
    }

    public void destroyTexture(Identifier id) {
        AbstractTexture lv = this.textures.remove(id);
        if (lv != null) {
            this.closeTexture(id, lv);
        }
    }

    @Override
    public void close() {
        this.textures.forEach(this::closeTexture);
        this.textures.clear();
        this.tickListeners.clear();
        this.dynamicIdCounters.clear();
    }

    @Override
    public CompletableFuture<Void> reload(ResourceReloader.Synchronizer synchronizer, ResourceManager manager, Profiler prepareProfiler, Profiler applyProfiler, Executor prepareExecutor, Executor applyExecutor) {
        CompletableFuture<Void> completableFuture = new CompletableFuture<Void>();
        ((CompletableFuture)TitleScreen.loadTexturesAsync(this, prepareExecutor).thenCompose(synchronizer::whenPrepared)).thenAcceptAsync(void_ -> {
            MissingSprite.getMissingSpriteTexture();
            BuyRealmsScreen.refreshImages(this.resourceContainer);
            Iterator<Map.Entry<Identifier, AbstractTexture>> iterator = this.textures.entrySet().iterator();
            while (iterator.hasNext()) {
                Map.Entry<Identifier, AbstractTexture> entry = iterator.next();
                Identifier lv = entry.getKey();
                AbstractTexture lv2 = entry.getValue();
                if (lv2 == MissingSprite.getMissingSpriteTexture() && !lv.equals(MissingSprite.getMissingSpriteId())) {
                    iterator.remove();
                    continue;
                }
                lv2.registerTexture(this, manager, lv, applyExecutor);
            }
            MinecraftClient.getInstance().send(() -> completableFuture.complete(null));
        }, runnable -> RenderSystem.recordRenderCall(runnable::run));
        return completableFuture;
    }

    public void dumpDynamicTextures(Path path) {
        if (!RenderSystem.isOnRenderThread()) {
            RenderSystem.recordRenderCall(() -> this.dumpDynamicTexturesInternal(path));
        } else {
            this.dumpDynamicTexturesInternal(path);
        }
    }

    private void dumpDynamicTexturesInternal(Path path) {
        try {
            Files.createDirectories(path, new FileAttribute[0]);
        } catch (IOException iOException) {
            LOGGER.error("Failed to create directory {}", (Object)path, (Object)iOException);
            return;
        }
        this.textures.forEach((id, texture) -> {
            if (texture instanceof DynamicTexture) {
                DynamicTexture lv = (DynamicTexture)((Object)texture);
                try {
                    lv.save((Identifier)id, path);
                } catch (IOException iOException) {
                    LOGGER.error("Failed to dump texture {}", id, (Object)iOException);
                }
            }
        });
    }
}

