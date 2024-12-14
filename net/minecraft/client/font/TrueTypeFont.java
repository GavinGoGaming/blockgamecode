/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 * 
 * Could not load the following classes:
 *  net.fabricmc.api.EnvType
 *  net.fabricmc.api.Environment
 *  org.jetbrains.annotations.Nullable
 */
package net.minecraft.client.font;

import it.unimi.dsi.fastutil.ints.IntArraySet;
import it.unimi.dsi.fastutil.ints.IntOpenHashSet;
import it.unimi.dsi.fastutil.ints.IntSet;
import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.util.Objects;
import java.util.function.Function;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.font.Font;
import net.minecraft.client.font.FreeTypeUtil;
import net.minecraft.client.font.Glyph;
import net.minecraft.client.font.GlyphRenderer;
import net.minecraft.client.font.RenderableGlyph;
import net.minecraft.client.texture.NativeImage;
import org.jetbrains.annotations.Nullable;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.system.MemoryUtil;
import org.lwjgl.util.freetype.FT_Bitmap;
import org.lwjgl.util.freetype.FT_Face;
import org.lwjgl.util.freetype.FT_GlyphSlot;
import org.lwjgl.util.freetype.FT_Vector;
import org.lwjgl.util.freetype.FreeType;

@Environment(value=EnvType.CLIENT)
public class TrueTypeFont
implements Font {
    @Nullable
    private ByteBuffer buffer;
    @Nullable
    private FT_Face face;
    final float oversample;
    private final IntSet excludedCharacters = new IntArraySet();

    public TrueTypeFont(ByteBuffer buffer, FT_Face face, float size, float oversample, float shiftX, float shiftY, String excludedCharacters) {
        this.buffer = buffer;
        this.face = face;
        this.oversample = oversample;
        excludedCharacters.codePoints().forEach(this.excludedCharacters::add);
        int j = Math.round(size * oversample);
        FreeType.FT_Set_Pixel_Sizes(face, j, j);
        float k = shiftX * oversample;
        float l = -shiftY * oversample;
        try (MemoryStack memoryStack = MemoryStack.stackPush();){
            FT_Vector fT_Vector = FreeTypeUtil.set(FT_Vector.malloc(memoryStack), k, l);
            FreeType.FT_Set_Transform(face, null, fT_Vector);
        }
    }

    @Override
    @Nullable
    public Glyph getGlyph(int codePoint) {
        FT_Face fT_Face = this.getInfo();
        if (this.excludedCharacters.contains(codePoint)) {
            return null;
        }
        int j = FreeType.FT_Get_Char_Index(fT_Face, codePoint);
        if (j == 0) {
            return null;
        }
        FreeTypeUtil.checkFatalError(FreeType.FT_Load_Glyph(fT_Face, j, 0x400008), "Loading glyph");
        FT_GlyphSlot fT_GlyphSlot = Objects.requireNonNull(fT_Face.glyph(), "Glyph not initialized");
        float f = FreeTypeUtil.getX(fT_GlyphSlot.advance());
        FT_Bitmap fT_Bitmap = fT_GlyphSlot.bitmap();
        int k = fT_GlyphSlot.bitmap_left();
        int l = fT_GlyphSlot.bitmap_top();
        int m = fT_Bitmap.width();
        int n = fT_Bitmap.rows();
        if (m <= 0 || n <= 0) {
            return () -> f / this.oversample;
        }
        return new TtfGlyph(k, l, m, n, f, j);
    }

    FT_Face getInfo() {
        if (this.buffer == null || this.face == null) {
            throw new IllegalStateException("Provider already closed");
        }
        return this.face;
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @Override
    public void close() {
        if (this.face != null) {
            Object object = FreeTypeUtil.LOCK;
            synchronized (object) {
                FreeTypeUtil.checkError(FreeType.FT_Done_Face(this.face), "Deleting face");
            }
            this.face = null;
        }
        MemoryUtil.memFree(this.buffer);
        this.buffer = null;
    }

    @Override
    public IntSet getProvidedGlyphs() {
        FT_Face fT_Face = this.getInfo();
        IntOpenHashSet intSet = new IntOpenHashSet();
        try (MemoryStack memoryStack = MemoryStack.stackPush();){
            IntBuffer intBuffer = memoryStack.mallocInt(1);
            long l = FreeType.FT_Get_First_Char(fT_Face, intBuffer);
            while (intBuffer.get(0) != 0) {
                intSet.add((int)l);
                l = FreeType.FT_Get_Next_Char(fT_Face, l, intBuffer);
            }
        }
        intSet.removeAll(this.excludedCharacters);
        return intSet;
    }

    @Environment(value=EnvType.CLIENT)
    class TtfGlyph
    implements Glyph {
        final int width;
        final int height;
        final float bearingX;
        final float ascent;
        private final float advance;
        final int glyphIndex;

        TtfGlyph(float bearingX, float ascent, int width, int height, float advance, int glyphIndex) {
            this.width = width;
            this.height = height;
            this.advance = advance / TrueTypeFont.this.oversample;
            this.bearingX = bearingX / TrueTypeFont.this.oversample;
            this.ascent = ascent / TrueTypeFont.this.oversample;
            this.glyphIndex = glyphIndex;
        }

        @Override
        public float getAdvance() {
            return this.advance;
        }

        @Override
        public GlyphRenderer bake(Function<RenderableGlyph, GlyphRenderer> function) {
            return function.apply(new RenderableGlyph(){

                @Override
                public int getWidth() {
                    return TtfGlyph.this.width;
                }

                @Override
                public int getHeight() {
                    return TtfGlyph.this.height;
                }

                @Override
                public float getOversample() {
                    return TrueTypeFont.this.oversample;
                }

                @Override
                public float getBearingX() {
                    return TtfGlyph.this.bearingX;
                }

                @Override
                public float getAscent() {
                    return TtfGlyph.this.ascent;
                }

                @Override
                public void upload(int x, int y) {
                    NativeImage lv = new NativeImage(NativeImage.Format.LUMINANCE, TtfGlyph.this.width, TtfGlyph.this.height, false);
                    FT_Face fT_Face = TrueTypeFont.this.getInfo();
                    if (lv.makeGlyphBitmapSubpixel(fT_Face, TtfGlyph.this.glyphIndex)) {
                        lv.upload(0, x, y, 0, 0, TtfGlyph.this.width, TtfGlyph.this.height, false, true);
                    } else {
                        lv.close();
                    }
                }

                @Override
                public boolean hasColor() {
                    return false;
                }
            });
        }
    }
}

