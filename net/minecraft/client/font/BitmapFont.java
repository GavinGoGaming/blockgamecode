/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 * 
 * Could not load the following classes:
 *  net.fabricmc.api.EnvType
 *  net.fabricmc.api.Environment
 *  org.jetbrains.annotations.Nullable
 */
package net.minecraft.client.font;

import com.mojang.datafixers.kinds.Applicative;
import com.mojang.datafixers.util.Either;
import com.mojang.logging.LogUtils;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import it.unimi.dsi.fastutil.ints.IntSet;
import it.unimi.dsi.fastutil.ints.IntSets;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.function.Function;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.font.Font;
import net.minecraft.client.font.FontLoader;
import net.minecraft.client.font.FontType;
import net.minecraft.client.font.Glyph;
import net.minecraft.client.font.GlyphContainer;
import net.minecraft.client.font.GlyphRenderer;
import net.minecraft.client.font.RenderableGlyph;
import net.minecraft.client.texture.NativeImage;
import net.minecraft.resource.ResourceManager;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;

@Environment(value=EnvType.CLIENT)
public class BitmapFont
implements Font {
    static final Logger LOGGER = LogUtils.getLogger();
    private final NativeImage image;
    private final GlyphContainer<BitmapFontGlyph> glyphs;

    BitmapFont(NativeImage image, GlyphContainer<BitmapFontGlyph> glyphs) {
        this.image = image;
        this.glyphs = glyphs;
    }

    @Override
    public void close() {
        this.image.close();
    }

    @Override
    @Nullable
    public Glyph getGlyph(int codePoint) {
        return this.glyphs.get(codePoint);
    }

    @Override
    public IntSet getProvidedGlyphs() {
        return IntSets.unmodifiable(this.glyphs.getProvidedGlyphs());
    }

    @Environment(value=EnvType.CLIENT)
    record BitmapFontGlyph(float scaleFactor, NativeImage image, int x, int y, int width, int height, int advance, int ascent) implements Glyph
    {
        @Override
        public float getAdvance() {
            return this.advance;
        }

        @Override
        public GlyphRenderer bake(Function<RenderableGlyph, GlyphRenderer> function) {
            return function.apply(new RenderableGlyph(){

                @Override
                public float getOversample() {
                    return 1.0f / scaleFactor;
                }

                @Override
                public int getWidth() {
                    return width;
                }

                @Override
                public int getHeight() {
                    return height;
                }

                @Override
                public float getAscent() {
                    return ascent;
                }

                @Override
                public void upload(int x, int y) {
                    image.upload(0, x, y, x, y, width, height, false, false);
                }

                @Override
                public boolean hasColor() {
                    return image.getFormat().getChannelCount() > 1;
                }
            });
        }
    }

    @Environment(value=EnvType.CLIENT)
    public record Loader(Identifier file, int height, int ascent, int[][] codepointGrid) implements FontLoader
    {
        private static final Codec<int[][]> CODE_POINT_GRID_CODEC = Codec.STRING.listOf().xmap(strings -> {
            int i = strings.size();
            int[][] is = new int[i][];
            for (int j = 0; j < i; ++j) {
                is[j] = ((String)strings.get(j)).codePoints().toArray();
            }
            return is;
        }, codePointGrid -> {
            ArrayList<String> list = new ArrayList<String>(((int[][])codePointGrid).length);
            for (int[] js : codePointGrid) {
                list.add(new String(js, 0, js.length));
            }
            return list;
        }).validate(Loader::validateCodePointGrid);
        public static final MapCodec<Loader> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(((MapCodec)Identifier.CODEC.fieldOf("file")).forGetter(Loader::file), Codec.INT.optionalFieldOf("height", 8).forGetter(Loader::height), ((MapCodec)Codec.INT.fieldOf("ascent")).forGetter(Loader::ascent), ((MapCodec)CODE_POINT_GRID_CODEC.fieldOf("chars")).forGetter(Loader::codepointGrid)).apply((Applicative<Loader, ?>)instance, Loader::new)).validate(Loader::validate);

        private static DataResult<int[][]> validateCodePointGrid(int[][] codePointGrid) {
            int i = codePointGrid.length;
            if (i == 0) {
                return DataResult.error(() -> "Expected to find data in codepoint grid");
            }
            int[] js = codePointGrid[0];
            int j = js.length;
            if (j == 0) {
                return DataResult.error(() -> "Expected to find data in codepoint grid");
            }
            for (int k = 1; k < i; ++k) {
                int[] ks = codePointGrid[k];
                if (ks.length == j) continue;
                return DataResult.error(() -> "Lines in codepoint grid have to be the same length (found: " + ks.length + " codepoints, expected: " + j + "), pad with \\u0000");
            }
            return DataResult.success(codePointGrid);
        }

        private static DataResult<Loader> validate(Loader fontLoader) {
            if (fontLoader.ascent > fontLoader.height) {
                return DataResult.error(() -> "Ascent " + arg.ascent + " higher than height " + arg.height);
            }
            return DataResult.success(fontLoader);
        }

        @Override
        public FontType getType() {
            return FontType.BITMAP;
        }

        @Override
        public Either<FontLoader.Loadable, FontLoader.Reference> build() {
            return Either.left(this::load);
        }

        private Font load(ResourceManager resourceManager) throws IOException {
            Identifier lv = this.file.withPrefixedPath("textures/");
            try (InputStream inputStream = resourceManager.open(lv);){
                NativeImage lv2 = NativeImage.read(NativeImage.Format.RGBA, inputStream);
                int i2 = lv2.getWidth();
                int j = lv2.getHeight();
                int k = i2 / this.codepointGrid[0].length;
                int l = j / this.codepointGrid.length;
                float f = (float)this.height / (float)l;
                GlyphContainer<BitmapFontGlyph> lv3 = new GlyphContainer<BitmapFontGlyph>(BitmapFontGlyph[]::new, i -> new BitmapFontGlyph[i][]);
                for (int m = 0; m < this.codepointGrid.length; ++m) {
                    int n = 0;
                    for (int o : this.codepointGrid[m]) {
                        int q;
                        BitmapFontGlyph lv4;
                        int p = n++;
                        if (o == 0 || (lv4 = lv3.put(o, new BitmapFontGlyph(f, lv2, p * k, m * l, k, l, (int)(0.5 + (double)((float)(q = this.findCharacterStartX(lv2, k, l, p, m)) * f)) + 1, this.ascent))) == null) continue;
                        LOGGER.warn("Codepoint '{}' declared multiple times in {}", (Object)Integer.toHexString(o), (Object)lv);
                    }
                }
                BitmapFont bitmapFont = new BitmapFont(lv2, lv3);
                return bitmapFont;
            }
        }

        private int findCharacterStartX(NativeImage image, int characterWidth, int characterHeight, int charPosX, int charPosY) {
            int m;
            for (m = characterWidth - 1; m >= 0; --m) {
                int n = charPosX * characterWidth + m;
                for (int o = 0; o < characterHeight; ++o) {
                    int p = charPosY * characterHeight + o;
                    if (image.getOpacity(n, p) == 0) continue;
                    return m + 1;
                }
            }
            return m + 1;
        }
    }
}

