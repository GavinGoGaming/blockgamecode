/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 * 
 * Could not load the following classes:
 *  net.fabricmc.api.EnvType
 *  net.fabricmc.api.Environment
 *  org.jetbrains.annotations.Nullable
 */
package net.minecraft.client.font;

import com.mojang.blaze3d.platform.TextureUtil;
import java.nio.file.Path;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.font.GlyphRenderer;
import net.minecraft.client.font.RenderableGlyph;
import net.minecraft.client.font.TextRenderLayerSet;
import net.minecraft.client.texture.AbstractTexture;
import net.minecraft.client.texture.DynamicTexture;
import net.minecraft.client.texture.NativeImage;
import net.minecraft.resource.ResourceManager;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.Nullable;

@Environment(value=EnvType.CLIENT)
public class GlyphAtlasTexture
extends AbstractTexture
implements DynamicTexture {
    private static final int SLOT_LENGTH = 256;
    private final TextRenderLayerSet textRenderLayers;
    private final boolean hasColor;
    private final Slot rootSlot;

    public GlyphAtlasTexture(TextRenderLayerSet textRenderLayers, boolean hasColor) {
        this.hasColor = hasColor;
        this.rootSlot = new Slot(0, 0, 256, 256);
        TextureUtil.prepareImage(hasColor ? NativeImage.InternalFormat.RGBA : NativeImage.InternalFormat.RED, this.getGlId(), 256, 256);
        this.textRenderLayers = textRenderLayers;
    }

    @Override
    public void load(ResourceManager manager) {
    }

    @Override
    public void close() {
        this.clearGlId();
    }

    @Nullable
    public GlyphRenderer getGlyphRenderer(RenderableGlyph glyph) {
        if (glyph.hasColor() != this.hasColor) {
            return null;
        }
        Slot lv = this.rootSlot.findSlotFor(glyph);
        if (lv != null) {
            this.bindTexture();
            glyph.upload(lv.x, lv.y);
            float f = 256.0f;
            float g = 256.0f;
            float h = 0.01f;
            return new GlyphRenderer(this.textRenderLayers, ((float)lv.x + 0.01f) / 256.0f, ((float)lv.x - 0.01f + (float)glyph.getWidth()) / 256.0f, ((float)lv.y + 0.01f) / 256.0f, ((float)lv.y - 0.01f + (float)glyph.getHeight()) / 256.0f, glyph.getXMin(), glyph.getXMax(), glyph.getYMin(), glyph.getYMax());
        }
        return null;
    }

    @Override
    public void save(Identifier id, Path path) {
        String string = id.toUnderscoreSeparatedString();
        TextureUtil.writeAsPNG(path, string, this.getGlId(), 0, 256, 256, color -> (color & 0xFF000000) == 0 ? -16777216 : color);
    }

    @Environment(value=EnvType.CLIENT)
    static class Slot {
        final int x;
        final int y;
        private final int width;
        private final int height;
        @Nullable
        private Slot subSlot1;
        @Nullable
        private Slot subSlot2;
        private boolean occupied;

        Slot(int x, int y, int width, int height) {
            this.x = x;
            this.y = y;
            this.width = width;
            this.height = height;
        }

        @Nullable
        Slot findSlotFor(RenderableGlyph glyph) {
            if (this.subSlot1 != null && this.subSlot2 != null) {
                Slot lv = this.subSlot1.findSlotFor(glyph);
                if (lv == null) {
                    lv = this.subSlot2.findSlotFor(glyph);
                }
                return lv;
            }
            if (this.occupied) {
                return null;
            }
            int i = glyph.getWidth();
            int j = glyph.getHeight();
            if (i > this.width || j > this.height) {
                return null;
            }
            if (i == this.width && j == this.height) {
                this.occupied = true;
                return this;
            }
            int k = this.width - i;
            int l = this.height - j;
            if (k > l) {
                this.subSlot1 = new Slot(this.x, this.y, i, this.height);
                this.subSlot2 = new Slot(this.x + i + 1, this.y, this.width - i - 1, this.height);
            } else {
                this.subSlot1 = new Slot(this.x, this.y, this.width, j);
                this.subSlot2 = new Slot(this.x, this.y + j + 1, this.width, this.height - j - 1);
            }
            return this.subSlot1.findSlotFor(glyph);
        }
    }
}

