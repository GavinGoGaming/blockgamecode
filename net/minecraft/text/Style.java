/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 * 
 * Could not load the following classes:
 *  org.jetbrains.annotations.Nullable
 */
package net.minecraft.text;

import com.mojang.datafixers.kinds.Applicative;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Objects;
import java.util.Optional;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.text.ClickEvent;
import net.minecraft.text.HoverEvent;
import net.minecraft.text.TextColor;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.Nullable;

public class Style {
    public static final Style EMPTY = new Style(null, null, null, null, null, null, null, null, null, null);
    public static final Identifier DEFAULT_FONT_ID = Identifier.ofVanilla("default");
    @Nullable
    final TextColor color;
    @Nullable
    final Boolean bold;
    @Nullable
    final Boolean italic;
    @Nullable
    final Boolean underlined;
    @Nullable
    final Boolean strikethrough;
    @Nullable
    final Boolean obfuscated;
    @Nullable
    final ClickEvent clickEvent;
    @Nullable
    final HoverEvent hoverEvent;
    @Nullable
    final String insertion;
    @Nullable
    final Identifier font;

    private static Style of(Optional<TextColor> color, Optional<Boolean> bold, Optional<Boolean> italic, Optional<Boolean> underlined, Optional<Boolean> strikethrough, Optional<Boolean> obfuscated, Optional<ClickEvent> optional7, Optional<HoverEvent> optional8, Optional<String> optional9, Optional<Identifier> optional10) {
        Style lv = new Style(color.orElse(null), bold.orElse(null), italic.orElse(null), underlined.orElse(null), strikethrough.orElse(null), obfuscated.orElse(null), optional7.orElse(null), optional8.orElse(null), optional9.orElse(null), optional10.orElse(null));
        if (lv.equals(EMPTY)) {
            return EMPTY;
        }
        return lv;
    }

    private Style(@Nullable TextColor color, @Nullable Boolean bold, @Nullable Boolean italic, @Nullable Boolean underlined, @Nullable Boolean strikethrough, @Nullable Boolean obfuscated, @Nullable ClickEvent clickEvent, @Nullable HoverEvent hoverEvent, @Nullable String insertion, @Nullable Identifier font) {
        this.color = color;
        this.bold = bold;
        this.italic = italic;
        this.underlined = underlined;
        this.strikethrough = strikethrough;
        this.obfuscated = obfuscated;
        this.clickEvent = clickEvent;
        this.hoverEvent = hoverEvent;
        this.insertion = insertion;
        this.font = font;
    }

    @Nullable
    public TextColor getColor() {
        return this.color;
    }

    public boolean isBold() {
        return this.bold == Boolean.TRUE;
    }

    public boolean isItalic() {
        return this.italic == Boolean.TRUE;
    }

    public boolean isStrikethrough() {
        return this.strikethrough == Boolean.TRUE;
    }

    public boolean isUnderlined() {
        return this.underlined == Boolean.TRUE;
    }

    public boolean isObfuscated() {
        return this.obfuscated == Boolean.TRUE;
    }

    public boolean isEmpty() {
        return this == EMPTY;
    }

    @Nullable
    public ClickEvent getClickEvent() {
        return this.clickEvent;
    }

    @Nullable
    public HoverEvent getHoverEvent() {
        return this.hoverEvent;
    }

    @Nullable
    public String getInsertion() {
        return this.insertion;
    }

    public Identifier getFont() {
        return this.font != null ? this.font : DEFAULT_FONT_ID;
    }

    private static <T> Style with(Style newStyle, @Nullable T oldAttribute, @Nullable T newAttribute) {
        if (oldAttribute != null && newAttribute == null && newStyle.equals(EMPTY)) {
            return EMPTY;
        }
        return newStyle;
    }

    public Style withColor(@Nullable TextColor color) {
        if (Objects.equals(this.color, color)) {
            return this;
        }
        return Style.with(new Style(color, this.bold, this.italic, this.underlined, this.strikethrough, this.obfuscated, this.clickEvent, this.hoverEvent, this.insertion, this.font), this.color, color);
    }

    public Style withColor(@Nullable Formatting color) {
        return this.withColor(color != null ? TextColor.fromFormatting(color) : null);
    }

    public Style withColor(int rgbColor) {
        return this.withColor(TextColor.fromRgb(rgbColor));
    }

    public Style withBold(@Nullable Boolean bold) {
        if (Objects.equals(this.bold, bold)) {
            return this;
        }
        return Style.with(new Style(this.color, bold, this.italic, this.underlined, this.strikethrough, this.obfuscated, this.clickEvent, this.hoverEvent, this.insertion, this.font), this.bold, bold);
    }

    public Style withItalic(@Nullable Boolean italic) {
        if (Objects.equals(this.italic, italic)) {
            return this;
        }
        return Style.with(new Style(this.color, this.bold, italic, this.underlined, this.strikethrough, this.obfuscated, this.clickEvent, this.hoverEvent, this.insertion, this.font), this.italic, italic);
    }

    public Style withUnderline(@Nullable Boolean underline) {
        if (Objects.equals(this.underlined, underline)) {
            return this;
        }
        return Style.with(new Style(this.color, this.bold, this.italic, underline, this.strikethrough, this.obfuscated, this.clickEvent, this.hoverEvent, this.insertion, this.font), this.underlined, underline);
    }

    public Style withStrikethrough(@Nullable Boolean strikethrough) {
        if (Objects.equals(this.strikethrough, strikethrough)) {
            return this;
        }
        return Style.with(new Style(this.color, this.bold, this.italic, this.underlined, strikethrough, this.obfuscated, this.clickEvent, this.hoverEvent, this.insertion, this.font), this.strikethrough, strikethrough);
    }

    public Style withObfuscated(@Nullable Boolean obfuscated) {
        if (Objects.equals(this.obfuscated, obfuscated)) {
            return this;
        }
        return Style.with(new Style(this.color, this.bold, this.italic, this.underlined, this.strikethrough, obfuscated, this.clickEvent, this.hoverEvent, this.insertion, this.font), this.obfuscated, obfuscated);
    }

    public Style withClickEvent(@Nullable ClickEvent clickEvent) {
        if (Objects.equals(this.clickEvent, clickEvent)) {
            return this;
        }
        return Style.with(new Style(this.color, this.bold, this.italic, this.underlined, this.strikethrough, this.obfuscated, clickEvent, this.hoverEvent, this.insertion, this.font), this.clickEvent, clickEvent);
    }

    public Style withHoverEvent(@Nullable HoverEvent hoverEvent) {
        if (Objects.equals(this.hoverEvent, hoverEvent)) {
            return this;
        }
        return Style.with(new Style(this.color, this.bold, this.italic, this.underlined, this.strikethrough, this.obfuscated, this.clickEvent, hoverEvent, this.insertion, this.font), this.hoverEvent, hoverEvent);
    }

    public Style withInsertion(@Nullable String insertion) {
        if (Objects.equals(this.insertion, insertion)) {
            return this;
        }
        return Style.with(new Style(this.color, this.bold, this.italic, this.underlined, this.strikethrough, this.obfuscated, this.clickEvent, this.hoverEvent, insertion, this.font), this.insertion, insertion);
    }

    public Style withFont(@Nullable Identifier font) {
        if (Objects.equals(this.font, font)) {
            return this;
        }
        return Style.with(new Style(this.color, this.bold, this.italic, this.underlined, this.strikethrough, this.obfuscated, this.clickEvent, this.hoverEvent, this.insertion, font), this.font, font);
    }

    public Style withFormatting(Formatting formatting) {
        TextColor lv = this.color;
        Boolean boolean_ = this.bold;
        Boolean boolean2 = this.italic;
        Boolean boolean3 = this.strikethrough;
        Boolean boolean4 = this.underlined;
        Boolean boolean5 = this.obfuscated;
        switch (formatting) {
            case OBFUSCATED: {
                boolean5 = true;
                break;
            }
            case BOLD: {
                boolean_ = true;
                break;
            }
            case STRIKETHROUGH: {
                boolean3 = true;
                break;
            }
            case UNDERLINE: {
                boolean4 = true;
                break;
            }
            case ITALIC: {
                boolean2 = true;
                break;
            }
            case RESET: {
                return EMPTY;
            }
            default: {
                lv = TextColor.fromFormatting(formatting);
            }
        }
        return new Style(lv, boolean_, boolean2, boolean4, boolean3, boolean5, this.clickEvent, this.hoverEvent, this.insertion, this.font);
    }

    public Style withExclusiveFormatting(Formatting formatting) {
        TextColor lv = this.color;
        Boolean boolean_ = this.bold;
        Boolean boolean2 = this.italic;
        Boolean boolean3 = this.strikethrough;
        Boolean boolean4 = this.underlined;
        Boolean boolean5 = this.obfuscated;
        switch (formatting) {
            case OBFUSCATED: {
                boolean5 = true;
                break;
            }
            case BOLD: {
                boolean_ = true;
                break;
            }
            case STRIKETHROUGH: {
                boolean3 = true;
                break;
            }
            case UNDERLINE: {
                boolean4 = true;
                break;
            }
            case ITALIC: {
                boolean2 = true;
                break;
            }
            case RESET: {
                return EMPTY;
            }
            default: {
                boolean5 = false;
                boolean_ = false;
                boolean3 = false;
                boolean4 = false;
                boolean2 = false;
                lv = TextColor.fromFormatting(formatting);
            }
        }
        return new Style(lv, boolean_, boolean2, boolean4, boolean3, boolean5, this.clickEvent, this.hoverEvent, this.insertion, this.font);
    }

    public Style withFormatting(Formatting ... formattings) {
        TextColor lv = this.color;
        Boolean boolean_ = this.bold;
        Boolean boolean2 = this.italic;
        Boolean boolean3 = this.strikethrough;
        Boolean boolean4 = this.underlined;
        Boolean boolean5 = this.obfuscated;
        block8: for (Formatting lv2 : formattings) {
            switch (lv2) {
                case OBFUSCATED: {
                    boolean5 = true;
                    continue block8;
                }
                case BOLD: {
                    boolean_ = true;
                    continue block8;
                }
                case STRIKETHROUGH: {
                    boolean3 = true;
                    continue block8;
                }
                case UNDERLINE: {
                    boolean4 = true;
                    continue block8;
                }
                case ITALIC: {
                    boolean2 = true;
                    continue block8;
                }
                case RESET: {
                    return EMPTY;
                }
                default: {
                    lv = TextColor.fromFormatting(lv2);
                }
            }
        }
        return new Style(lv, boolean_, boolean2, boolean4, boolean3, boolean5, this.clickEvent, this.hoverEvent, this.insertion, this.font);
    }

    public Style withParent(Style parent) {
        if (this == EMPTY) {
            return parent;
        }
        if (parent == EMPTY) {
            return this;
        }
        return new Style(this.color != null ? this.color : parent.color, this.bold != null ? this.bold : parent.bold, this.italic != null ? this.italic : parent.italic, this.underlined != null ? this.underlined : parent.underlined, this.strikethrough != null ? this.strikethrough : parent.strikethrough, this.obfuscated != null ? this.obfuscated : parent.obfuscated, this.clickEvent != null ? this.clickEvent : parent.clickEvent, this.hoverEvent != null ? this.hoverEvent : parent.hoverEvent, this.insertion != null ? this.insertion : parent.insertion, this.font != null ? this.font : parent.font);
    }

    public String toString() {
        final StringBuilder stringBuilder = new StringBuilder("{");
        class Writer {
            private boolean shouldAppendComma;

            Writer() {
            }

            private void appendComma() {
                if (this.shouldAppendComma) {
                    stringBuilder.append(',');
                }
                this.shouldAppendComma = true;
            }

            void append(String key, @Nullable Boolean value) {
                if (value != null) {
                    this.appendComma();
                    if (!value.booleanValue()) {
                        stringBuilder.append('!');
                    }
                    stringBuilder.append(key);
                }
            }

            void append(String key, @Nullable Object value) {
                if (value != null) {
                    this.appendComma();
                    stringBuilder.append(key);
                    stringBuilder.append('=');
                    stringBuilder.append(value);
                }
            }
        }
        Writer lv = new Writer();
        lv.append("color", this.color);
        lv.append("bold", this.bold);
        lv.append("italic", this.italic);
        lv.append("underlined", this.underlined);
        lv.append("strikethrough", this.strikethrough);
        lv.append("obfuscated", this.obfuscated);
        lv.append("clickEvent", this.clickEvent);
        lv.append("hoverEvent", this.hoverEvent);
        lv.append("insertion", this.insertion);
        lv.append("font", this.font);
        stringBuilder.append("}");
        return stringBuilder.toString();
    }

    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o instanceof Style) {
            Style lv = (Style)o;
            return this.bold == lv.bold && Objects.equals(this.getColor(), lv.getColor()) && this.italic == lv.italic && this.obfuscated == lv.obfuscated && this.strikethrough == lv.strikethrough && this.underlined == lv.underlined && Objects.equals(this.clickEvent, lv.clickEvent) && Objects.equals(this.hoverEvent, lv.hoverEvent) && Objects.equals(this.insertion, lv.insertion) && Objects.equals(this.font, lv.font);
        }
        return false;
    }

    public int hashCode() {
        return Objects.hash(this.color, this.bold, this.italic, this.underlined, this.strikethrough, this.obfuscated, this.clickEvent, this.hoverEvent, this.insertion);
    }

    public static class Codecs {
        public static final MapCodec<Style> MAP_CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(TextColor.CODEC.optionalFieldOf("color").forGetter(style -> Optional.ofNullable(style.color)), Codec.BOOL.optionalFieldOf("bold").forGetter(style -> Optional.ofNullable(style.bold)), Codec.BOOL.optionalFieldOf("italic").forGetter(style -> Optional.ofNullable(style.italic)), Codec.BOOL.optionalFieldOf("underlined").forGetter(style -> Optional.ofNullable(style.underlined)), Codec.BOOL.optionalFieldOf("strikethrough").forGetter(style -> Optional.ofNullable(style.strikethrough)), Codec.BOOL.optionalFieldOf("obfuscated").forGetter(style -> Optional.ofNullable(style.obfuscated)), ClickEvent.CODEC.optionalFieldOf("clickEvent").forGetter(style -> Optional.ofNullable(style.clickEvent)), HoverEvent.CODEC.optionalFieldOf("hoverEvent").forGetter(style -> Optional.ofNullable(style.hoverEvent)), Codec.STRING.optionalFieldOf("insertion").forGetter(style -> Optional.ofNullable(style.insertion)), Identifier.CODEC.optionalFieldOf("font").forGetter(style -> Optional.ofNullable(style.font))).apply((Applicative<Style, ?>)instance, Style::of));
        public static final Codec<Style> CODEC = MAP_CODEC.codec();
        public static final PacketCodec<RegistryByteBuf, Style> PACKET_CODEC = PacketCodecs.unlimitedRegistryCodec(CODEC);
    }
}

