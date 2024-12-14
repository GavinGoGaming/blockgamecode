/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 * 
 * Could not load the following classes:
 *  net.fabricmc.api.EnvType
 *  net.fabricmc.api.Environment
 *  org.jetbrains.annotations.Nullable
 */
package net.minecraft.client.font;

import com.google.common.collect.Lists;
import com.ibm.icu.text.ArabicShaping;
import com.ibm.icu.text.ArabicShapingException;
import com.ibm.icu.text.Bidi;
import java.util.List;
import java.util.function.Function;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.font.EmptyGlyphRenderer;
import net.minecraft.client.font.FontStorage;
import net.minecraft.client.font.Glyph;
import net.minecraft.client.font.GlyphRenderer;
import net.minecraft.client.font.TextHandler;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.text.CharacterVisitor;
import net.minecraft.text.OrderedText;
import net.minecraft.text.StringVisitable;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.text.TextColor;
import net.minecraft.text.TextVisitFactory;
import net.minecraft.util.Identifier;
import net.minecraft.util.Language;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.random.Random;
import org.jetbrains.annotations.Nullable;
import org.joml.Matrix4f;
import org.joml.Vector3f;

@Environment(value=EnvType.CLIENT)
public class TextRenderer {
    private static final float Z_INDEX = 0.01f;
    private static final Vector3f FORWARD_SHIFT = new Vector3f(0.0f, 0.0f, 0.03f);
    public static final int ARABIC_SHAPING_LETTERS_SHAPE = 8;
    public final int fontHeight = 9;
    public final Random random = Random.create();
    private final Function<Identifier, FontStorage> fontStorageAccessor;
    final boolean validateAdvance;
    private final TextHandler handler;

    public TextRenderer(Function<Identifier, FontStorage> fontStorageAccessor, boolean validateAdvance) {
        this.fontStorageAccessor = fontStorageAccessor;
        this.validateAdvance = validateAdvance;
        this.handler = new TextHandler((codePoint, style) -> this.getFontStorage(style.getFont()).getGlyph(codePoint, this.validateAdvance).getAdvance(style.isBold()));
    }

    FontStorage getFontStorage(Identifier id) {
        return this.fontStorageAccessor.apply(id);
    }

    public String mirror(String text) {
        try {
            Bidi bidi = new Bidi(new ArabicShaping(8).shape(text), 127);
            bidi.setReorderingMode(0);
            return bidi.writeReordered(2);
        } catch (ArabicShapingException arabicShapingException) {
            return text;
        }
    }

    public int draw(String text, float x, float y, int color, boolean shadow, Matrix4f matrix, VertexConsumerProvider vertexConsumers, TextLayerType layerType, int backgroundColor, int light) {
        return this.draw(text, x, y, color, shadow, matrix, vertexConsumers, layerType, backgroundColor, light, this.isRightToLeft());
    }

    public int draw(String text, float x, float y, int color, boolean shadow, Matrix4f matrix, VertexConsumerProvider vertexConsumers, TextLayerType layerType, int backgroundColor, int light, boolean rightToLeft) {
        return this.drawInternal(text, x, y, color, shadow, matrix, vertexConsumers, layerType, backgroundColor, light, rightToLeft);
    }

    public int draw(Text text, float x, float y, int color, boolean shadow, Matrix4f matrix, VertexConsumerProvider vertexConsumers, TextLayerType layerType, int backgroundColor, int light) {
        return this.draw(text.asOrderedText(), x, y, color, shadow, matrix, vertexConsumers, layerType, backgroundColor, light);
    }

    public int draw(OrderedText text, float x, float y, int color, boolean shadow, Matrix4f matrix, VertexConsumerProvider vertexConsumers, TextLayerType layerType, int backgroundColor, int light) {
        return this.drawInternal(text, x, y, color, shadow, matrix, vertexConsumers, layerType, backgroundColor, light);
    }

    public void drawWithOutline(OrderedText text, float x, float y, int color, int outlineColor, Matrix4f matrix, VertexConsumerProvider vertexConsumers, int light) {
        int l = TextRenderer.tweakTransparency(outlineColor);
        Drawer lv = new Drawer(vertexConsumers, 0.0f, 0.0f, l, false, matrix, TextLayerType.NORMAL, light);
        for (int m = -1; m <= 1; ++m) {
            for (int n = -1; n <= 1; ++n) {
                if (m == 0 && n == 0) continue;
                float[] fs = new float[]{x};
                int o = m;
                int p = n;
                text.accept((index, style, codePoint) -> {
                    boolean bl = style.isBold();
                    FontStorage lv = this.getFontStorage(style.getFont());
                    Glyph lv2 = lv.getGlyph(codePoint, this.validateAdvance);
                    arg.x = fs[0] + (float)o * lv2.getShadowOffset();
                    arg.y = y + (float)p * lv2.getShadowOffset();
                    fs[0] = fs[0] + lv2.getAdvance(bl);
                    return lv.accept(index, style.withColor(l), codePoint);
                });
            }
        }
        Drawer lv2 = new Drawer(vertexConsumers, x, y, TextRenderer.tweakTransparency(color), false, matrix, TextLayerType.POLYGON_OFFSET, light);
        text.accept(lv2);
        lv2.drawLayer(0, x);
    }

    private static int tweakTransparency(int argb) {
        if ((argb & 0xFC000000) == 0) {
            return argb | 0xFF000000;
        }
        return argb;
    }

    private int drawInternal(String text, float x, float y, int color, boolean shadow, Matrix4f matrix, VertexConsumerProvider vertexConsumers, TextLayerType layerType, int backgroundColor, int light, boolean mirror) {
        if (mirror) {
            text = this.mirror(text);
        }
        color = TextRenderer.tweakTransparency(color);
        Matrix4f matrix4f2 = new Matrix4f(matrix);
        if (shadow) {
            this.drawLayer(text, x, y, color, true, matrix, vertexConsumers, layerType, backgroundColor, light);
            matrix4f2.translate(FORWARD_SHIFT);
        }
        x = this.drawLayer(text, x, y, color, false, matrix4f2, vertexConsumers, layerType, backgroundColor, light);
        return (int)x + (shadow ? 1 : 0);
    }

    private int drawInternal(OrderedText text, float x, float y, int color, boolean shadow, Matrix4f matrix, VertexConsumerProvider vertexConsumerProvider, TextLayerType layerType, int backgroundColor, int light) {
        color = TextRenderer.tweakTransparency(color);
        Matrix4f matrix4f2 = new Matrix4f(matrix);
        if (shadow) {
            this.drawLayer(text, x, y, color, true, matrix, vertexConsumerProvider, layerType, backgroundColor, light);
            matrix4f2.translate(FORWARD_SHIFT);
        }
        x = this.drawLayer(text, x, y, color, false, matrix4f2, vertexConsumerProvider, layerType, backgroundColor, light);
        return (int)x + (shadow ? 1 : 0);
    }

    private float drawLayer(String text, float x, float y, int color, boolean shadow, Matrix4f matrix, VertexConsumerProvider vertexConsumerProvider, TextLayerType layerType, int underlineColor, int light) {
        Drawer lv = new Drawer(vertexConsumerProvider, x, y, color, shadow, matrix, layerType, light);
        TextVisitFactory.visitFormatted(text, Style.EMPTY, (CharacterVisitor)lv);
        return lv.drawLayer(underlineColor, x);
    }

    private float drawLayer(OrderedText text, float x, float y, int color, boolean shadow, Matrix4f matrix, VertexConsumerProvider vertexConsumerProvider, TextLayerType layerType, int underlineColor, int light) {
        Drawer lv = new Drawer(vertexConsumerProvider, x, y, color, shadow, matrix, layerType, light);
        text.accept(lv);
        return lv.drawLayer(underlineColor, x);
    }

    void drawGlyph(GlyphRenderer glyphRenderer, boolean bold, boolean italic, float weight, float x, float y, Matrix4f matrix, VertexConsumer vertexConsumer, float red, float green, float blue, float alpha, int light) {
        glyphRenderer.draw(italic, x, y, matrix, vertexConsumer, red, green, blue, alpha, light);
        if (bold) {
            glyphRenderer.draw(italic, x + weight, y, matrix, vertexConsumer, red, green, blue, alpha, light);
        }
    }

    public int getWidth(String text) {
        return MathHelper.ceil(this.handler.getWidth(text));
    }

    public int getWidth(StringVisitable text) {
        return MathHelper.ceil(this.handler.getWidth(text));
    }

    public int getWidth(OrderedText text) {
        return MathHelper.ceil(this.handler.getWidth(text));
    }

    public String trimToWidth(String text, int maxWidth, boolean backwards) {
        return backwards ? this.handler.trimToWidthBackwards(text, maxWidth, Style.EMPTY) : this.handler.trimToWidth(text, maxWidth, Style.EMPTY);
    }

    public String trimToWidth(String text, int maxWidth) {
        return this.handler.trimToWidth(text, maxWidth, Style.EMPTY);
    }

    public StringVisitable trimToWidth(StringVisitable text, int width) {
        return this.handler.trimToWidth(text, width, Style.EMPTY);
    }

    public int getWrappedLinesHeight(String text, int maxWidth) {
        return 9 * this.handler.wrapLines(text, maxWidth, Style.EMPTY).size();
    }

    public int getWrappedLinesHeight(StringVisitable text, int maxWidth) {
        return 9 * this.handler.wrapLines(text, maxWidth, Style.EMPTY).size();
    }

    public List<OrderedText> wrapLines(StringVisitable text, int width) {
        return Language.getInstance().reorder(this.handler.wrapLines(text, width, Style.EMPTY));
    }

    public boolean isRightToLeft() {
        return Language.getInstance().isRightToLeft();
    }

    public TextHandler getTextHandler() {
        return this.handler;
    }

    @Environment(value=EnvType.CLIENT)
    public static enum TextLayerType {
        NORMAL,
        SEE_THROUGH,
        POLYGON_OFFSET;

    }

    @Environment(value=EnvType.CLIENT)
    class Drawer
    implements CharacterVisitor {
        final VertexConsumerProvider vertexConsumers;
        private final boolean shadow;
        private final float brightnessMultiplier;
        private final float red;
        private final float green;
        private final float blue;
        private final float alpha;
        private final Matrix4f matrix;
        private final TextLayerType layerType;
        private final int light;
        float x;
        float y;
        @Nullable
        private List<GlyphRenderer.Rectangle> rectangles;

        private void addRectangle(GlyphRenderer.Rectangle rectangle) {
            if (this.rectangles == null) {
                this.rectangles = Lists.newArrayList();
            }
            this.rectangles.add(rectangle);
        }

        public Drawer(VertexConsumerProvider vertexConsumers, float x, float y, int color, boolean shadow, Matrix4f matrix, TextLayerType layerType, int light) {
            this.vertexConsumers = vertexConsumers;
            this.x = x;
            this.y = y;
            this.shadow = shadow;
            this.brightnessMultiplier = shadow ? 0.25f : 1.0f;
            this.red = (float)(color >> 16 & 0xFF) / 255.0f * this.brightnessMultiplier;
            this.green = (float)(color >> 8 & 0xFF) / 255.0f * this.brightnessMultiplier;
            this.blue = (float)(color & 0xFF) / 255.0f * this.brightnessMultiplier;
            this.alpha = (float)(color >> 24 & 0xFF) / 255.0f;
            this.matrix = matrix;
            this.layerType = layerType;
            this.light = light;
        }

        @Override
        public boolean accept(int i, Style arg, int j) {
            float n;
            float l;
            float h;
            float g;
            FontStorage lv = TextRenderer.this.getFontStorage(arg.getFont());
            Glyph lv2 = lv.getGlyph(j, TextRenderer.this.validateAdvance);
            GlyphRenderer lv3 = arg.isObfuscated() && j != 32 ? lv.getObfuscatedGlyphRenderer(lv2) : lv.getGlyphRenderer(j);
            boolean bl = arg.isBold();
            float f = this.alpha;
            TextColor lv4 = arg.getColor();
            if (lv4 != null) {
                int k = lv4.getRgb();
                g = (float)(k >> 16 & 0xFF) / 255.0f * this.brightnessMultiplier;
                h = (float)(k >> 8 & 0xFF) / 255.0f * this.brightnessMultiplier;
                l = (float)(k & 0xFF) / 255.0f * this.brightnessMultiplier;
            } else {
                g = this.red;
                h = this.green;
                l = this.blue;
            }
            if (!(lv3 instanceof EmptyGlyphRenderer)) {
                float m = bl ? lv2.getBoldOffset() : 0.0f;
                n = this.shadow ? lv2.getShadowOffset() : 0.0f;
                VertexConsumer lv5 = this.vertexConsumers.getBuffer(lv3.getLayer(this.layerType));
                TextRenderer.this.drawGlyph(lv3, bl, arg.isItalic(), m, this.x + n, this.y + n, this.matrix, lv5, g, h, l, f, this.light);
            }
            float m = lv2.getAdvance(bl);
            float f2 = n = this.shadow ? 1.0f : 0.0f;
            if (arg.isStrikethrough()) {
                this.addRectangle(new GlyphRenderer.Rectangle(this.x + n - 1.0f, this.y + n + 4.5f, this.x + n + m, this.y + n + 4.5f - 1.0f, 0.01f, g, h, l, f));
            }
            if (arg.isUnderlined()) {
                this.addRectangle(new GlyphRenderer.Rectangle(this.x + n - 1.0f, this.y + n + 9.0f, this.x + n + m, this.y + n + 9.0f - 1.0f, 0.01f, g, h, l, f));
            }
            this.x += m;
            return true;
        }

        public float drawLayer(int underlineColor, float x) {
            if (underlineColor != 0) {
                float g = (float)(underlineColor >> 24 & 0xFF) / 255.0f;
                float h = (float)(underlineColor >> 16 & 0xFF) / 255.0f;
                float j = (float)(underlineColor >> 8 & 0xFF) / 255.0f;
                float k = (float)(underlineColor & 0xFF) / 255.0f;
                this.addRectangle(new GlyphRenderer.Rectangle(x - 1.0f, this.y + 9.0f, this.x + 1.0f, this.y - 1.0f, 0.01f, h, j, k, g));
            }
            if (this.rectangles != null) {
                GlyphRenderer lv = TextRenderer.this.getFontStorage(Style.DEFAULT_FONT_ID).getRectangleRenderer();
                VertexConsumer lv2 = this.vertexConsumers.getBuffer(lv.getLayer(this.layerType));
                for (GlyphRenderer.Rectangle lv3 : this.rectangles) {
                    lv.drawRectangle(lv3, this.matrix, lv2, this.light);
                }
            }
            return this.x;
        }
    }
}

