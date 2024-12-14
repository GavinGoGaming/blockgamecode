/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 * 
 * Could not load the following classes:
 *  net.fabricmc.api.EnvType
 *  net.fabricmc.api.Environment
 */
package net.minecraft.client.texture;

import com.mojang.logging.LogUtils;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.function.Function;
import java.util.stream.Collectors;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.resource.metadata.AnimationResourceMetadata;
import net.minecraft.client.texture.MissingSprite;
import net.minecraft.client.texture.Sprite;
import net.minecraft.client.texture.SpriteAtlasTexture;
import net.minecraft.client.texture.SpriteContents;
import net.minecraft.client.texture.SpriteOpener;
import net.minecraft.client.texture.TextureStitcher;
import net.minecraft.client.texture.TextureStitcherCannotFitException;
import net.minecraft.client.texture.atlas.AtlasLoader;
import net.minecraft.resource.ResourceManager;
import net.minecraft.resource.metadata.ResourceMetadataReader;
import net.minecraft.util.Identifier;
import net.minecraft.util.Util;
import net.minecraft.util.crash.CrashException;
import net.minecraft.util.crash.CrashReport;
import net.minecraft.util.crash.CrashReportSection;
import net.minecraft.util.math.MathHelper;
import org.slf4j.Logger;

@Environment(value=EnvType.CLIENT)
public class SpriteLoader {
    public static final Set<ResourceMetadataReader<?>> METADATA_READERS = Set.of(AnimationResourceMetadata.READER);
    private static final Logger LOGGER = LogUtils.getLogger();
    private final Identifier id;
    private final int maxTextureSize;
    private final int width;
    private final int height;

    public SpriteLoader(Identifier id, int maxTextureSize, int width, int height) {
        this.id = id;
        this.maxTextureSize = maxTextureSize;
        this.width = width;
        this.height = height;
    }

    public static SpriteLoader fromAtlas(SpriteAtlasTexture atlasTexture) {
        return new SpriteLoader(atlasTexture.getId(), atlasTexture.getMaxTextureSize(), atlasTexture.getWidth(), atlasTexture.getHeight());
    }

    public StitchResult stitch(List<SpriteContents> sprites, int mipLevel, Executor executor) {
        int m;
        int j = this.maxTextureSize;
        TextureStitcher<SpriteContents> lv = new TextureStitcher<SpriteContents>(j, j, mipLevel);
        int k = Integer.MAX_VALUE;
        int l = 1 << mipLevel;
        for (SpriteContents lv2 : sprites) {
            k = Math.min(k, Math.min(lv2.getWidth(), lv2.getHeight()));
            m = Math.min(Integer.lowestOneBit(lv2.getWidth()), Integer.lowestOneBit(lv2.getHeight()));
            if (m < l) {
                LOGGER.warn("Texture {} with size {}x{} limits mip level from {} to {}", lv2.getId(), lv2.getWidth(), lv2.getHeight(), MathHelper.floorLog2(l), MathHelper.floorLog2(m));
                l = m;
            }
            lv.add(lv2);
        }
        int n = Math.min(k, l);
        int o = MathHelper.floorLog2(n);
        if (o < mipLevel) {
            LOGGER.warn("{}: dropping miplevel from {} to {}, because of minimum power of two: {}", this.id, mipLevel, o, n);
            m = o;
        } else {
            m = mipLevel;
        }
        try {
            lv.stitch();
        } catch (TextureStitcherCannotFitException lv3) {
            CrashReport lv4 = CrashReport.create(lv3, "Stitching");
            CrashReportSection lv5 = lv4.addElement("Stitcher");
            lv5.add("Sprites", lv3.getSprites().stream().map(sprite -> String.format(Locale.ROOT, "%s[%dx%d]", sprite.getId(), sprite.getWidth(), sprite.getHeight())).collect(Collectors.joining(",")));
            lv5.add("Max Texture Size", j);
            throw new CrashException(lv4);
        }
        int p = Math.max(lv.getWidth(), this.width);
        int q = Math.max(lv.getHeight(), this.height);
        Map<Identifier, Sprite> map = this.collectStitchedSprites(lv, p, q);
        Sprite lv6 = map.get(MissingSprite.getMissingSpriteId());
        CompletableFuture<Object> completableFuture = m > 0 ? CompletableFuture.runAsync(() -> map.values().forEach(sprite -> sprite.getContents().generateMipmaps(m)), executor) : CompletableFuture.completedFuture(null);
        return new StitchResult(p, q, m, lv6, map, completableFuture);
    }

    public static CompletableFuture<List<SpriteContents>> loadAll(SpriteOpener opener, List<Function<SpriteOpener, SpriteContents>> sources, Executor executor) {
        List<CompletableFuture> list2 = sources.stream().map(sprite -> CompletableFuture.supplyAsync(() -> (SpriteContents)sprite.apply(opener), executor)).toList();
        return Util.combineSafe(list2).thenApply(sprites -> sprites.stream().filter(Objects::nonNull).toList());
    }

    public CompletableFuture<StitchResult> load(ResourceManager resourceManager, Identifier path, int mipLevel, Executor executor) {
        return this.load(resourceManager, path, mipLevel, executor, METADATA_READERS);
    }

    public CompletableFuture<StitchResult> load(ResourceManager resourceManager, Identifier path, int mipLevel, Executor executor, Collection<ResourceMetadataReader<?>> metadatas) {
        SpriteOpener lv = SpriteOpener.create(metadatas);
        return ((CompletableFuture)CompletableFuture.supplyAsync(() -> AtlasLoader.of(resourceManager, path).loadSources(resourceManager), executor).thenCompose(sources -> SpriteLoader.loadAll(lv, sources, executor))).thenApply(sprites -> this.stitch((List<SpriteContents>)sprites, mipLevel, executor));
    }

    private Map<Identifier, Sprite> collectStitchedSprites(TextureStitcher<SpriteContents> stitcher, int atlasWidth, int atlasHeight) {
        HashMap<Identifier, Sprite> map = new HashMap<Identifier, Sprite>();
        stitcher.getStitchedSprites((info, x, y) -> map.put(info.getId(), new Sprite(this.id, (SpriteContents)info, atlasWidth, atlasHeight, x, y)));
        return map;
    }

    @Environment(value=EnvType.CLIENT)
    public record StitchResult(int width, int height, int mipLevel, Sprite missing, Map<Identifier, Sprite> regions, CompletableFuture<Void> readyForUpload) {
        public CompletableFuture<StitchResult> whenComplete() {
            return this.readyForUpload.thenApply(void_ -> this);
        }
    }
}

