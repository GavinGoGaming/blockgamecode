/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 * 
 * Could not load the following classes:
 *  net.fabricmc.api.EnvType
 *  net.fabricmc.api.Environment
 *  org.jetbrains.annotations.Nullable
 */
package net.minecraft.client.font;

import com.google.common.annotations.VisibleForTesting;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.datafixers.kinds.Applicative;
import com.mojang.datafixers.util.Either;
import com.mojang.logging.LogUtils;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import it.unimi.dsi.fastutil.bytes.ByteArrayList;
import it.unimi.dsi.fastutil.bytes.ByteList;
import it.unimi.dsi.fastutil.ints.IntSet;
import java.io.IOException;
import java.io.InputStream;
import java.nio.IntBuffer;
import java.util.List;
import java.util.function.Function;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
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
import net.minecraft.util.FixedBufferInputStream;
import net.minecraft.util.Identifier;
import net.minecraft.util.dynamic.Codecs;
import org.jetbrains.annotations.Nullable;
import org.lwjgl.system.MemoryUtil;
import org.slf4j.Logger;

@Environment(value=EnvType.CLIENT)
public class UnihexFont
implements Font {
    static final Logger LOGGER = LogUtils.getLogger();
    private static final int field_44764 = 16;
    private static final int field_44765 = 2;
    private static final int field_44766 = 32;
    private static final int field_44767 = 64;
    private static final int field_44768 = 96;
    private static final int field_44769 = 128;
    private final GlyphContainer<UnicodeTextureGlyph> glyphs;

    UnihexFont(GlyphContainer<UnicodeTextureGlyph> glyphs) {
        this.glyphs = glyphs;
    }

    @Override
    @Nullable
    public Glyph getGlyph(int codePoint) {
        return this.glyphs.get(codePoint);
    }

    @Override
    public IntSet getProvidedGlyphs() {
        return this.glyphs.getProvidedGlyphs();
    }

    @VisibleForTesting
    static void addRowPixels(IntBuffer pixelsOut, int row, int left, int right) {
        int l = 32 - left - 1;
        int m = 32 - right - 1;
        for (int n = l; n >= m; --n) {
            if (n >= 32 || n < 0) {
                pixelsOut.put(0);
                continue;
            }
            boolean bl = (row >> n & 1) != 0;
            pixelsOut.put(bl ? -1 : 0);
        }
    }

    static void addGlyphPixels(IntBuffer pixelsOut, BitmapGlyph glyph, int left, int right) {
        for (int k = 0; k < 16; ++k) {
            int l = glyph.getPixels(k);
            UnihexFont.addRowPixels(pixelsOut, l, left, right);
        }
    }

    @VisibleForTesting
    static void readLines(InputStream stream, BitmapGlyphConsumer callback) throws IOException {
        int i = 0;
        ByteArrayList byteList = new ByteArrayList(128);
        while (true) {
            int l;
            boolean bl = UnihexFont.readUntilDelimiter(stream, byteList, 58);
            int j = byteList.size();
            if (j == 0 && !bl) break;
            if (!bl || j != 4 && j != 5 && j != 6) {
                throw new IllegalArgumentException("Invalid entry at line " + i + ": expected 4, 5 or 6 hex digits followed by a colon");
            }
            int k = 0;
            for (l = 0; l < j; ++l) {
                k = k << 4 | UnihexFont.getHexDigitValue(i, byteList.getByte(l));
            }
            byteList.clear();
            UnihexFont.readUntilDelimiter(stream, byteList, 10);
            l = byteList.size();
            BitmapGlyph lv = switch (l) {
                case 32 -> FontImage8x16.read(i, byteList);
                case 64 -> FontImage16x16.read(i, byteList);
                case 96 -> FontImage32x16.read24x16(i, byteList);
                case 128 -> FontImage32x16.read32x16(i, byteList);
                default -> throw new IllegalArgumentException("Invalid entry at line " + i + ": expected hex number describing (8,16,24,32) x 16 bitmap, followed by a new line");
            };
            callback.accept(k, lv);
            ++i;
            byteList.clear();
        }
    }

    static int getHexDigitValue(int lineNum, ByteList bytes, int index) {
        return UnihexFont.getHexDigitValue(lineNum, bytes.getByte(index));
    }

    private static int getHexDigitValue(int lineNum, byte digit) {
        return switch (digit) {
            case 48 -> 0;
            case 49 -> 1;
            case 50 -> 2;
            case 51 -> 3;
            case 52 -> 4;
            case 53 -> 5;
            case 54 -> 6;
            case 55 -> 7;
            case 56 -> 8;
            case 57 -> 9;
            case 65 -> 10;
            case 66 -> 11;
            case 67 -> 12;
            case 68 -> 13;
            case 69 -> 14;
            case 70 -> 15;
            default -> throw new IllegalArgumentException("Invalid entry at line " + lineNum + ": expected hex digit, got " + (char)digit);
        };
    }

    private static boolean readUntilDelimiter(InputStream stream, ByteList data, int delimiter) throws IOException {
        int j;
        while ((j = stream.read()) != -1) {
            if (j == delimiter) {
                return true;
            }
            data.add((byte)j);
        }
        return false;
    }

    @Environment(value=EnvType.CLIENT)
    public static interface BitmapGlyph {
        public int getPixels(int var1);

        public int bitWidth();

        default public int getNonemptyColumnBitmask() {
            int i = 0;
            for (int j = 0; j < 16; ++j) {
                i |= this.getPixels(j);
            }
            return i;
        }

        default public int getPackedDimensions() {
            int l;
            int k;
            int i = this.getNonemptyColumnBitmask();
            int j = this.bitWidth();
            if (i == 0) {
                k = 0;
                l = j;
            } else {
                k = Integer.numberOfLeadingZeros(i);
                l = 32 - Integer.numberOfTrailingZeros(i) - 1;
            }
            return Dimensions.pack(k, l);
        }
    }

    @Environment(value=EnvType.CLIENT)
    record FontImage8x16(byte[] contents) implements BitmapGlyph
    {
        @Override
        public int getPixels(int y) {
            return this.contents[y] << 24;
        }

        static BitmapGlyph read(int lineNum, ByteList data) {
            byte[] bs = new byte[16];
            int j = 0;
            for (int k = 0; k < 16; ++k) {
                byte b;
                int l = UnihexFont.getHexDigitValue(lineNum, data, j++);
                int m = UnihexFont.getHexDigitValue(lineNum, data, j++);
                bs[k] = b = (byte)(l << 4 | m);
            }
            return new FontImage8x16(bs);
        }

        @Override
        public int bitWidth() {
            return 8;
        }
    }

    @Environment(value=EnvType.CLIENT)
    record FontImage16x16(short[] contents) implements BitmapGlyph
    {
        @Override
        public int getPixels(int y) {
            return this.contents[y] << 16;
        }

        static BitmapGlyph read(int lineNum, ByteList data) {
            short[] ss = new short[16];
            int j = 0;
            for (int k = 0; k < 16; ++k) {
                short s;
                int l = UnihexFont.getHexDigitValue(lineNum, data, j++);
                int m = UnihexFont.getHexDigitValue(lineNum, data, j++);
                int n = UnihexFont.getHexDigitValue(lineNum, data, j++);
                int o = UnihexFont.getHexDigitValue(lineNum, data, j++);
                ss[k] = s = (short)(l << 12 | m << 8 | n << 4 | o);
            }
            return new FontImage16x16(ss);
        }

        @Override
        public int bitWidth() {
            return 16;
        }
    }

    @Environment(value=EnvType.CLIENT)
    record FontImage32x16(int[] contents, int bitWidth) implements BitmapGlyph
    {
        private static final int field_44775 = 24;

        @Override
        public int getPixels(int y) {
            return this.contents[y];
        }

        static BitmapGlyph read24x16(int lineNum, ByteList data) {
            int[] is = new int[16];
            int j = 0;
            int k = 0;
            for (int l = 0; l < 16; ++l) {
                int m = UnihexFont.getHexDigitValue(lineNum, data, k++);
                int n = UnihexFont.getHexDigitValue(lineNum, data, k++);
                int o = UnihexFont.getHexDigitValue(lineNum, data, k++);
                int p = UnihexFont.getHexDigitValue(lineNum, data, k++);
                int q = UnihexFont.getHexDigitValue(lineNum, data, k++);
                int r = UnihexFont.getHexDigitValue(lineNum, data, k++);
                int s = m << 20 | n << 16 | o << 12 | p << 8 | q << 4 | r;
                is[l] = s << 8;
                j |= s;
            }
            return new FontImage32x16(is, 24);
        }

        public static BitmapGlyph read32x16(int lineNum, ByteList data) {
            int[] is = new int[16];
            int j = 0;
            int k = 0;
            for (int l = 0; l < 16; ++l) {
                int u;
                int m = UnihexFont.getHexDigitValue(lineNum, data, k++);
                int n = UnihexFont.getHexDigitValue(lineNum, data, k++);
                int o = UnihexFont.getHexDigitValue(lineNum, data, k++);
                int p = UnihexFont.getHexDigitValue(lineNum, data, k++);
                int q = UnihexFont.getHexDigitValue(lineNum, data, k++);
                int r = UnihexFont.getHexDigitValue(lineNum, data, k++);
                int s = UnihexFont.getHexDigitValue(lineNum, data, k++);
                int t = UnihexFont.getHexDigitValue(lineNum, data, k++);
                is[l] = u = m << 28 | n << 24 | o << 20 | p << 16 | q << 12 | r << 8 | s << 4 | t;
                j |= u;
            }
            return new FontImage32x16(is, 32);
        }
    }

    @FunctionalInterface
    @Environment(value=EnvType.CLIENT)
    public static interface BitmapGlyphConsumer {
        public void accept(int var1, BitmapGlyph var2);
    }

    @Environment(value=EnvType.CLIENT)
    record UnicodeTextureGlyph(BitmapGlyph contents, int left, int right) implements Glyph
    {
        public int width() {
            return this.right - this.left + 1;
        }

        @Override
        public float getAdvance() {
            return this.width() / 2 + 1;
        }

        @Override
        public float getShadowOffset() {
            return 0.5f;
        }

        @Override
        public float getBoldOffset() {
            return 0.5f;
        }

        @Override
        public GlyphRenderer bake(Function<RenderableGlyph, GlyphRenderer> function) {
            return function.apply(new RenderableGlyph(){

                @Override
                public float getOversample() {
                    return 2.0f;
                }

                @Override
                public int getWidth() {
                    return this.width();
                }

                @Override
                public int getHeight() {
                    return 16;
                }

                @Override
                public void upload(int x, int y) {
                    IntBuffer intBuffer = MemoryUtil.memAllocInt(this.width() * 16);
                    UnihexFont.addGlyphPixels(intBuffer, contents, left, right);
                    intBuffer.rewind();
                    GlStateManager.upload(0, x, y, this.width(), 16, NativeImage.Format.RGBA, intBuffer, MemoryUtil::memFree);
                }

                @Override
                public boolean hasColor() {
                    return true;
                }
            });
        }
    }

    @Environment(value=EnvType.CLIENT)
    public static class Loader
    implements FontLoader {
        public static final MapCodec<Loader> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(((MapCodec)Identifier.CODEC.fieldOf("hex_file")).forGetter(loader -> loader.sizes), ((MapCodec)DimensionOverride.CODEC.listOf().fieldOf("size_overrides")).forGetter(loader -> loader.overrides)).apply((Applicative<Loader, ?>)instance, Loader::new));
        private final Identifier sizes;
        private final List<DimensionOverride> overrides;

        private Loader(Identifier sizes, List<DimensionOverride> overrides) {
            this.sizes = sizes;
            this.overrides = overrides;
        }

        @Override
        public FontType getType() {
            return FontType.UNIHEX;
        }

        @Override
        public Either<FontLoader.Loadable, FontLoader.Reference> build() {
            return Either.left(this::load);
        }

        private Font load(ResourceManager resourceManager) throws IOException {
            try (InputStream inputStream = resourceManager.open(this.sizes);){
                UnihexFont unihexFont = this.loadHexFile(inputStream);
                return unihexFont;
            }
        }

        private UnihexFont loadHexFile(InputStream stream) throws IOException {
            GlyphContainer<BitmapGlyph> lv = new GlyphContainer<BitmapGlyph>(BitmapGlyph[]::new, rows -> new BitmapGlyph[rows][]);
            BitmapGlyphConsumer lv2 = lv::put;
            try (ZipInputStream zipInputStream = new ZipInputStream(stream);){
                ZipEntry zipEntry;
                while ((zipEntry = zipInputStream.getNextEntry()) != null) {
                    String string = zipEntry.getName();
                    if (!string.endsWith(".hex")) continue;
                    LOGGER.info("Found {}, loading", (Object)string);
                    UnihexFont.readLines(new FixedBufferInputStream(zipInputStream), lv2);
                }
                GlyphContainer<UnicodeTextureGlyph> lv3 = new GlyphContainer<UnicodeTextureGlyph>(UnicodeTextureGlyph[]::new, i -> new UnicodeTextureGlyph[i][]);
                for (DimensionOverride lv4 : this.overrides) {
                    int i2 = lv4.from;
                    int j = lv4.to;
                    Dimensions lv5 = lv4.dimensions;
                    for (int k = i2; k <= j; ++k) {
                        BitmapGlyph lv6 = (BitmapGlyph)lv.remove(k);
                        if (lv6 == null) continue;
                        lv3.put(k, new UnicodeTextureGlyph(lv6, lv5.left, lv5.right));
                    }
                }
                lv.forEachGlyph((codePoint, glyph) -> {
                    int j = glyph.getPackedDimensions();
                    int k = Dimensions.getLeft(j);
                    int l = Dimensions.getRight(j);
                    lv3.put(codePoint, new UnicodeTextureGlyph((BitmapGlyph)glyph, k, l));
                });
                UnihexFont unihexFont = new UnihexFont(lv3);
                return unihexFont;
            }
        }
    }

    @Environment(value=EnvType.CLIENT)
    public record Dimensions(int left, int right) {
        public static final MapCodec<Dimensions> MAP_CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(((MapCodec)Codec.INT.fieldOf("left")).forGetter(Dimensions::left), ((MapCodec)Codec.INT.fieldOf("right")).forGetter(Dimensions::right)).apply((Applicative<Dimensions, ?>)instance, Dimensions::new));
        public static final Codec<Dimensions> CODEC = MAP_CODEC.codec();

        public int packedValue() {
            return Dimensions.pack(this.left, this.right);
        }

        public static int pack(int left, int right) {
            return (left & 0xFF) << 8 | right & 0xFF;
        }

        public static int getLeft(int packed) {
            return (byte)(packed >> 8);
        }

        public static int getRight(int packed) {
            return (byte)packed;
        }
    }

    @Environment(value=EnvType.CLIENT)
    record DimensionOverride(int from, int to, Dimensions dimensions) {
        private static final Codec<DimensionOverride> NON_VALIDATED_CODEC = RecordCodecBuilder.create(instance -> instance.group(((MapCodec)Codecs.CODEPOINT.fieldOf("from")).forGetter(DimensionOverride::from), ((MapCodec)Codecs.CODEPOINT.fieldOf("to")).forGetter(DimensionOverride::to), Dimensions.MAP_CODEC.forGetter(DimensionOverride::dimensions)).apply((Applicative<DimensionOverride, ?>)instance, DimensionOverride::new));
        public static final Codec<DimensionOverride> CODEC = NON_VALIDATED_CODEC.validate(override -> {
            if (override.from >= override.to) {
                return DataResult.error(() -> "Invalid range: [" + arg.from + ";" + arg.to + "]");
            }
            return DataResult.success(override);
        });
    }
}

