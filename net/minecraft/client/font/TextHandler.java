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
import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;
import java.util.Optional;
import java.util.function.BiConsumer;
import java.util.stream.Collectors;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.util.TextCollector;
import net.minecraft.text.CharacterVisitor;
import net.minecraft.text.OrderedText;
import net.minecraft.text.StringVisitable;
import net.minecraft.text.Style;
import net.minecraft.text.TextVisitFactory;
import org.apache.commons.lang3.mutable.MutableFloat;
import org.apache.commons.lang3.mutable.MutableInt;
import org.apache.commons.lang3.mutable.MutableObject;
import org.jetbrains.annotations.Nullable;

@Environment(value=EnvType.CLIENT)
public class TextHandler {
    final WidthRetriever widthRetriever;

    public TextHandler(WidthRetriever widthRetriever) {
        this.widthRetriever = widthRetriever;
    }

    public float getWidth(@Nullable String text) {
        if (text == null) {
            return 0.0f;
        }
        MutableFloat mutableFloat = new MutableFloat();
        TextVisitFactory.visitFormatted(text, Style.EMPTY, (unused, style, codePoint) -> {
            mutableFloat.add(this.widthRetriever.getWidth(codePoint, style));
            return true;
        });
        return mutableFloat.floatValue();
    }

    public float getWidth(StringVisitable text) {
        MutableFloat mutableFloat = new MutableFloat();
        TextVisitFactory.visitFormatted(text, Style.EMPTY, (unused, style, codePoint) -> {
            mutableFloat.add(this.widthRetriever.getWidth(codePoint, style));
            return true;
        });
        return mutableFloat.floatValue();
    }

    public float getWidth(OrderedText text) {
        MutableFloat mutableFloat = new MutableFloat();
        text.accept((index, style, codePoint) -> {
            mutableFloat.add(this.widthRetriever.getWidth(codePoint, style));
            return true;
        });
        return mutableFloat.floatValue();
    }

    public int getTrimmedLength(String text, int maxWidth, Style style) {
        WidthLimitingVisitor lv = new WidthLimitingVisitor(maxWidth);
        TextVisitFactory.visitForwards(text, style, lv);
        return lv.getLength();
    }

    public String trimToWidth(String text, int maxWidth, Style style) {
        return text.substring(0, this.getTrimmedLength(text, maxWidth, style));
    }

    public String trimToWidthBackwards(String text, int maxWidth, Style style2) {
        MutableFloat mutableFloat = new MutableFloat();
        MutableInt mutableInt = new MutableInt(text.length());
        TextVisitFactory.visitBackwards(text, style2, (index, style, codePoint) -> {
            float f = mutableFloat.addAndGet(this.widthRetriever.getWidth(codePoint, style));
            if (f > (float)maxWidth) {
                return false;
            }
            mutableInt.setValue(index);
            return true;
        });
        return text.substring(mutableInt.intValue());
    }

    public int getLimitedStringLength(String text, int maxWidth, Style style) {
        WidthLimitingVisitor lv = new WidthLimitingVisitor(maxWidth);
        TextVisitFactory.visitFormatted(text, style, (CharacterVisitor)lv);
        return lv.getLength();
    }

    @Nullable
    public Style getStyleAt(StringVisitable text2, int x) {
        WidthLimitingVisitor lv = new WidthLimitingVisitor(x);
        return text2.visit((style, text) -> TextVisitFactory.visitFormatted(text, style, (CharacterVisitor)lv) ? Optional.empty() : Optional.of(style), Style.EMPTY).orElse(null);
    }

    @Nullable
    public Style getStyleAt(OrderedText text, int x) {
        WidthLimitingVisitor lv = new WidthLimitingVisitor(x);
        MutableObject mutableObject = new MutableObject();
        text.accept((index, style, codePoint) -> {
            if (!lv.accept(index, style, codePoint)) {
                mutableObject.setValue(style);
                return false;
            }
            return true;
        });
        return (Style)mutableObject.getValue();
    }

    public String limitString(String text, int maxWidth, Style style) {
        return text.substring(0, this.getLimitedStringLength(text, maxWidth, style));
    }

    public StringVisitable trimToWidth(StringVisitable text, int width, Style style) {
        final WidthLimitingVisitor lv = new WidthLimitingVisitor(width);
        return text.visit(new StringVisitable.StyledVisitor<StringVisitable>(){
            private final TextCollector collector = new TextCollector();

            @Override
            public Optional<StringVisitable> accept(Style arg, String string) {
                lv.resetLength();
                if (!TextVisitFactory.visitFormatted(string, arg, (CharacterVisitor)lv)) {
                    String string2 = string.substring(0, lv.getLength());
                    if (!string2.isEmpty()) {
                        this.collector.add(StringVisitable.styled(string2, arg));
                    }
                    return Optional.of(this.collector.getCombined());
                }
                if (!string.isEmpty()) {
                    this.collector.add(StringVisitable.styled(string, arg));
                }
                return Optional.empty();
            }
        }, style).orElse(text);
    }

    public int getEndingIndex(String text, int maxWidth, Style style) {
        LineBreakingVisitor lv = new LineBreakingVisitor(maxWidth);
        TextVisitFactory.visitFormatted(text, style, (CharacterVisitor)lv);
        return lv.getEndingIndex();
    }

    public static int moveCursorByWords(String text, int offset, int cursor, boolean consumeSpaceOrBreak) {
        int k = cursor;
        boolean bl2 = offset < 0;
        int l = Math.abs(offset);
        for (int m = 0; m < l; ++m) {
            if (bl2) {
                while (consumeSpaceOrBreak && k > 0 && (text.charAt(k - 1) == ' ' || text.charAt(k - 1) == '\n')) {
                    --k;
                }
                while (k > 0 && text.charAt(k - 1) != ' ' && text.charAt(k - 1) != '\n') {
                    --k;
                }
                continue;
            }
            int n = text.length();
            int o = text.indexOf(32, k);
            int p = text.indexOf(10, k);
            k = o == -1 && p == -1 ? -1 : (o != -1 && p != -1 ? Math.min(o, p) : (o != -1 ? o : p));
            if (k == -1) {
                k = n;
                continue;
            }
            while (consumeSpaceOrBreak && k < n && (text.charAt(k) == ' ' || text.charAt(k) == '\n')) {
                ++k;
            }
        }
        return k;
    }

    public void wrapLines(String text, int maxWidth, Style style, boolean retainTrailingWordSplit, LineWrappingConsumer consumer) {
        int j = 0;
        int k = text.length();
        Style lv = style;
        while (j < k) {
            LineBreakingVisitor lv2 = new LineBreakingVisitor(maxWidth);
            boolean bl2 = TextVisitFactory.visitFormatted(text, j, lv, style, lv2);
            if (bl2) {
                consumer.accept(lv, j, k);
                break;
            }
            int l = lv2.getEndingIndex();
            char c = text.charAt(l);
            int m = c == '\n' || c == ' ' ? l + 1 : l;
            consumer.accept(lv, j, retainTrailingWordSplit ? m : l);
            j = m;
            lv = lv2.getEndingStyle();
        }
    }

    public List<StringVisitable> wrapLines(String text, int maxWidth, Style style2) {
        ArrayList<StringVisitable> list = Lists.newArrayList();
        this.wrapLines(text, maxWidth, style2, false, (style, start, end) -> list.add(StringVisitable.styled(text.substring(start, end), style)));
        return list;
    }

    public List<StringVisitable> wrapLines(StringVisitable text2, int maxWidth, Style style) {
        ArrayList<StringVisitable> list = Lists.newArrayList();
        this.wrapLines(text2, maxWidth, style, (StringVisitable text, Boolean lastLineWrapped) -> list.add((StringVisitable)text));
        return list;
    }

    public List<StringVisitable> wrapLines(StringVisitable text2, int maxWidth, Style style, StringVisitable wrappedLinePrefix) {
        ArrayList<StringVisitable> list = Lists.newArrayList();
        this.wrapLines(text2, maxWidth, style, (StringVisitable text, Boolean lastLineWrapped) -> list.add(lastLineWrapped != false ? StringVisitable.concat(wrappedLinePrefix, text) : text));
        return list;
    }

    public void wrapLines(StringVisitable text2, int maxWidth, Style style2, BiConsumer<StringVisitable, Boolean> lineConsumer) {
        ArrayList<StyledString> list = Lists.newArrayList();
        text2.visit((style, text) -> {
            if (!text.isEmpty()) {
                list.add(new StyledString(text, style));
            }
            return Optional.empty();
        }, style2);
        LineWrappingCollector lv = new LineWrappingCollector(list);
        boolean bl = true;
        boolean bl2 = false;
        boolean bl3 = false;
        block0: while (bl) {
            bl = false;
            LineBreakingVisitor lv2 = new LineBreakingVisitor(maxWidth);
            for (StyledString lv3 : lv.parts) {
                boolean bl4 = TextVisitFactory.visitFormatted(lv3.literal, 0, lv3.style, style2, lv2);
                if (!bl4) {
                    int j = lv2.getEndingIndex();
                    Style lv4 = lv2.getEndingStyle();
                    char c = lv.charAt(j);
                    boolean bl5 = c == '\n';
                    boolean bl6 = bl5 || c == ' ';
                    bl2 = bl5;
                    StringVisitable lv5 = lv.collectLine(j, bl6 ? 1 : 0, lv4);
                    lineConsumer.accept(lv5, bl3);
                    bl3 = !bl5;
                    bl = true;
                    continue block0;
                }
                lv2.offset(lv3.literal.length());
            }
        }
        StringVisitable lv6 = lv.collectRemainders();
        if (lv6 != null) {
            lineConsumer.accept(lv6, bl3);
        } else if (bl2) {
            lineConsumer.accept(StringVisitable.EMPTY, false);
        }
    }

    @FunctionalInterface
    @Environment(value=EnvType.CLIENT)
    public static interface WidthRetriever {
        public float getWidth(int var1, Style var2);
    }

    @Environment(value=EnvType.CLIENT)
    class WidthLimitingVisitor
    implements CharacterVisitor {
        private float widthLeft;
        private int length;

        public WidthLimitingVisitor(float maxWidth) {
            this.widthLeft = maxWidth;
        }

        @Override
        public boolean accept(int i, Style arg, int j) {
            this.widthLeft -= TextHandler.this.widthRetriever.getWidth(j, arg);
            if (this.widthLeft >= 0.0f) {
                this.length = i + Character.charCount(j);
                return true;
            }
            return false;
        }

        public int getLength() {
            return this.length;
        }

        public void resetLength() {
            this.length = 0;
        }
    }

    @Environment(value=EnvType.CLIENT)
    class LineBreakingVisitor
    implements CharacterVisitor {
        private final float maxWidth;
        private int endIndex = -1;
        private Style endStyle = Style.EMPTY;
        private boolean nonEmpty;
        private float totalWidth;
        private int lastSpaceBreak = -1;
        private Style lastSpaceStyle = Style.EMPTY;
        private int count;
        private int startOffset;

        public LineBreakingVisitor(float maxWidth) {
            this.maxWidth = Math.max(maxWidth, 1.0f);
        }

        @Override
        public boolean accept(int i, Style arg, int j) {
            int k = i + this.startOffset;
            switch (j) {
                case 10: {
                    return this.breakLine(k, arg);
                }
                case 32: {
                    this.lastSpaceBreak = k;
                    this.lastSpaceStyle = arg;
                }
            }
            float f = TextHandler.this.widthRetriever.getWidth(j, arg);
            this.totalWidth += f;
            if (this.nonEmpty && this.totalWidth > this.maxWidth) {
                if (this.lastSpaceBreak != -1) {
                    return this.breakLine(this.lastSpaceBreak, this.lastSpaceStyle);
                }
                return this.breakLine(k, arg);
            }
            this.nonEmpty |= f != 0.0f;
            this.count = k + Character.charCount(j);
            return true;
        }

        private boolean breakLine(int finishIndex, Style finishStyle) {
            this.endIndex = finishIndex;
            this.endStyle = finishStyle;
            return false;
        }

        private boolean hasLineBreak() {
            return this.endIndex != -1;
        }

        public int getEndingIndex() {
            return this.hasLineBreak() ? this.endIndex : this.count;
        }

        public Style getEndingStyle() {
            return this.endStyle;
        }

        public void offset(int extraOffset) {
            this.startOffset += extraOffset;
        }
    }

    @FunctionalInterface
    @Environment(value=EnvType.CLIENT)
    public static interface LineWrappingConsumer {
        public void accept(Style var1, int var2, int var3);
    }

    @Environment(value=EnvType.CLIENT)
    static class LineWrappingCollector {
        final List<StyledString> parts;
        private String joined;

        public LineWrappingCollector(List<StyledString> parts) {
            this.parts = parts;
            this.joined = parts.stream().map(part -> part.literal).collect(Collectors.joining());
        }

        public char charAt(int index) {
            return this.joined.charAt(index);
        }

        public StringVisitable collectLine(int lineLength, int skippedLength, Style style) {
            TextCollector lv = new TextCollector();
            ListIterator<StyledString> listIterator = this.parts.listIterator();
            int k = lineLength;
            boolean bl = false;
            while (listIterator.hasNext()) {
                String string2;
                StyledString lv2 = listIterator.next();
                String string = lv2.literal;
                int l = string.length();
                if (!bl) {
                    if (k > l) {
                        lv.add(lv2);
                        listIterator.remove();
                        k -= l;
                    } else {
                        string2 = string.substring(0, k);
                        if (!string2.isEmpty()) {
                            lv.add(StringVisitable.styled(string2, lv2.style));
                        }
                        k += skippedLength;
                        bl = true;
                    }
                }
                if (!bl) continue;
                if (k > l) {
                    listIterator.remove();
                    k -= l;
                    continue;
                }
                string2 = string.substring(k);
                if (string2.isEmpty()) {
                    listIterator.remove();
                    break;
                }
                listIterator.set(new StyledString(string2, style));
                break;
            }
            this.joined = this.joined.substring(lineLength + skippedLength);
            return lv.getCombined();
        }

        @Nullable
        public StringVisitable collectRemainders() {
            TextCollector lv = new TextCollector();
            this.parts.forEach(lv::add);
            this.parts.clear();
            return lv.getRawCombined();
        }
    }

    @Environment(value=EnvType.CLIENT)
    static class StyledString
    implements StringVisitable {
        final String literal;
        final Style style;

        public StyledString(String literal, Style style) {
            this.literal = literal;
            this.style = style;
        }

        @Override
        public <T> Optional<T> visit(StringVisitable.Visitor<T> visitor) {
            return visitor.accept(this.literal);
        }

        @Override
        public <T> Optional<T> visit(StringVisitable.StyledVisitor<T> styledVisitor, Style style) {
            return styledVisitor.accept(this.style.withParent(style), this.literal);
        }
    }
}

