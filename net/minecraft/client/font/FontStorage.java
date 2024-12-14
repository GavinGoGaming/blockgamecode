/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 * 
 * Could not load the following classes:
 *  net.fabricmc.api.EnvType
 *  net.fabricmc.api.Environment
 */
package net.minecraft.client.font;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntList;
import it.unimi.dsi.fastutil.ints.IntOpenHashSet;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.font.BuiltinEmptyGlyph;
import net.minecraft.client.font.Font;
import net.minecraft.client.font.FontFilterType;
import net.minecraft.client.font.Glyph;
import net.minecraft.client.font.GlyphAtlasTexture;
import net.minecraft.client.font.GlyphContainer;
import net.minecraft.client.font.GlyphRenderer;
import net.minecraft.client.font.RenderableGlyph;
import net.minecraft.client.font.TextRenderLayerSet;
import net.minecraft.client.texture.TextureManager;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.random.Random;

@Environment(value=EnvType.CLIENT)
public class FontStorage
implements AutoCloseable {
    private static final Random RANDOM = Random.create();
    private static final float MAX_ADVANCE = 32.0f;
    private final TextureManager textureManager;
    private final Identifier id;
    private GlyphRenderer blankGlyphRenderer;
    private GlyphRenderer whiteRectangleGlyphRenderer;
    private List<Font.FontFilterPair> allFonts = List.of();
    private List<Font> availableFonts = List.of();
    private final GlyphContainer<GlyphRenderer> glyphRendererCache = new GlyphContainer(GlyphRenderer[]::new, rowCount -> new GlyphRenderer[rowCount][]);
    private final GlyphContainer<GlyphPair> glyphCache = new GlyphContainer(GlyphPair[]::new, rowCount -> new GlyphPair[rowCount][]);
    private final Int2ObjectMap<IntList> charactersByWidth = new Int2ObjectOpenHashMap<IntList>();
    private final List<GlyphAtlasTexture> glyphAtlases = Lists.newArrayList();

    public FontStorage(TextureManager textureManager, Identifier id) {
        this.textureManager = textureManager;
        this.id = id;
    }

    public void setFonts(List<Font.FontFilterPair> allFonts, Set<FontFilterType> activeFilters) {
        this.allFonts = allFonts;
        this.setActiveFilters(activeFilters);
    }

    public void setActiveFilters(Set<FontFilterType> activeFilters) {
        this.availableFonts = List.of();
        this.clear();
        this.availableFonts = this.applyFilters(this.allFonts, activeFilters);
    }

    private void clear() {
        this.closeGlyphAtlases();
        this.glyphRendererCache.clear();
        this.glyphCache.clear();
        this.charactersByWidth.clear();
        this.blankGlyphRenderer = BuiltinEmptyGlyph.MISSING.bake(this::getGlyphRenderer);
        this.whiteRectangleGlyphRenderer = BuiltinEmptyGlyph.WHITE.bake(this::getGlyphRenderer);
    }

    private List<Font> applyFilters(List<Font.FontFilterPair> allFonts, Set<FontFilterType> activeFilters) {
        IntOpenHashSet intSet = new IntOpenHashSet();
        ArrayList<Font> list2 = new ArrayList<Font>();
        for (Font.FontFilterPair lv : allFonts) {
            if (!lv.filter().isAllowed(activeFilters)) continue;
            list2.add(lv.provider());
            intSet.addAll(lv.provider().getProvidedGlyphs());
        }
        HashSet set2 = Sets.newHashSet();
        intSet.forEach(codePoint -> {
            for (Font lv : list2) {
                Glyph lv2 = lv.getGlyph(codePoint);
                if (lv2 == null) continue;
                set2.add(lv);
                if (lv2 == BuiltinEmptyGlyph.MISSING) break;
                this.charactersByWidth.computeIfAbsent(MathHelper.ceil(lv2.getAdvance(false)), i -> new IntArrayList()).add(codePoint);
                break;
            }
        });
        return list2.stream().filter(set2::contains).toList();
    }

    @Override
    public void close() {
        this.closeGlyphAtlases();
    }

    private void closeGlyphAtlases() {
        for (GlyphAtlasTexture lv : this.glyphAtlases) {
            lv.close();
        }
        this.glyphAtlases.clear();
    }

    private static boolean isAdvanceInvalid(Glyph glyph) {
        float f = glyph.getAdvance(false);
        if (f < 0.0f || f > 32.0f) {
            return true;
        }
        float g = glyph.getAdvance(true);
        return g < 0.0f || g > 32.0f;
    }

    private GlyphPair findGlyph(int codePoint) {
        Glyph lv = null;
        for (Font lv2 : this.availableFonts) {
            Glyph lv3 = lv2.getGlyph(codePoint);
            if (lv3 == null) continue;
            if (lv == null) {
                lv = lv3;
            }
            if (FontStorage.isAdvanceInvalid(lv3)) continue;
            return new GlyphPair(lv, lv3);
        }
        if (lv != null) {
            return new GlyphPair(lv, BuiltinEmptyGlyph.MISSING);
        }
        return GlyphPair.MISSING;
    }

    public Glyph getGlyph(int codePoint, boolean validateAdvance) {
        return this.glyphCache.computeIfAbsent(codePoint, this::findGlyph).getGlyph(validateAdvance);
    }

    private GlyphRenderer findGlyphRenderer(int codePoint) {
        for (Font lv : this.availableFonts) {
            Glyph lv2 = lv.getGlyph(codePoint);
            if (lv2 == null) continue;
            return lv2.bake(this::getGlyphRenderer);
        }
        return this.blankGlyphRenderer;
    }

    public GlyphRenderer getGlyphRenderer(int codePoint) {
        return this.glyphRendererCache.computeIfAbsent(codePoint, this::findGlyphRenderer);
    }

    private GlyphRenderer getGlyphRenderer(RenderableGlyph c) {
        for (GlyphAtlasTexture lv : this.glyphAtlases) {
            GlyphRenderer lv2 = lv.getGlyphRenderer(c);
            if (lv2 == null) continue;
            return lv2;
        }
        Identifier lv3 = this.id.withSuffixedPath("/" + this.glyphAtlases.size());
        boolean bl = c.hasColor();
        TextRenderLayerSet lv4 = bl ? TextRenderLayerSet.of(lv3) : TextRenderLayerSet.ofIntensity(lv3);
        GlyphAtlasTexture lv5 = new GlyphAtlasTexture(lv4, bl);
        this.glyphAtlases.add(lv5);
        this.textureManager.registerTexture(lv3, lv5);
        GlyphRenderer lv6 = lv5.getGlyphRenderer(c);
        return lv6 == null ? this.blankGlyphRenderer : lv6;
    }

    public GlyphRenderer getObfuscatedGlyphRenderer(Glyph glyph) {
        IntList intList = (IntList)this.charactersByWidth.get(MathHelper.ceil(glyph.getAdvance(false)));
        if (intList != null && !intList.isEmpty()) {
            return this.getGlyphRenderer(intList.getInt(RANDOM.nextInt(intList.size())));
        }
        return this.blankGlyphRenderer;
    }

    public Identifier getId() {
        return this.id;
    }

    public GlyphRenderer getRectangleRenderer() {
        return this.whiteRectangleGlyphRenderer;
    }

    @Environment(value=EnvType.CLIENT)
    record GlyphPair(Glyph glyph, Glyph advanceValidatedGlyph) {
        static final GlyphPair MISSING = new GlyphPair(BuiltinEmptyGlyph.MISSING, BuiltinEmptyGlyph.MISSING);

        Glyph getGlyph(boolean validateAdvance) {
            return validateAdvance ? this.advanceValidatedGlyph : this.glyph;
        }
    }
}

