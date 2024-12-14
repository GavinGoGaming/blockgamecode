/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 * 
 * Could not load the following classes:
 *  org.jetbrains.annotations.Nullable
 */
package net.minecraft.text;

import com.google.common.collect.ImmutableMap;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.Lifecycle;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Stream;
import net.minecraft.util.Formatting;
import org.jetbrains.annotations.Nullable;

public final class TextColor {
    private static final String RGB_PREFIX = "#";
    public static final Codec<TextColor> CODEC = Codec.STRING.comapFlatMap(TextColor::parse, TextColor::getName);
    private static final Map<Formatting, TextColor> FORMATTING_TO_COLOR = Stream.of(Formatting.values()).filter(Formatting::isColor).collect(ImmutableMap.toImmutableMap(Function.identity(), formatting -> new TextColor(formatting.getColorValue(), formatting.getName())));
    private static final Map<String, TextColor> BY_NAME = FORMATTING_TO_COLOR.values().stream().collect(ImmutableMap.toImmutableMap(textColor -> textColor.name, Function.identity()));
    private final int rgb;
    @Nullable
    private final String name;

    private TextColor(int rgb, String name) {
        this.rgb = rgb & 0xFFFFFF;
        this.name = name;
    }

    private TextColor(int rgb) {
        this.rgb = rgb & 0xFFFFFF;
        this.name = null;
    }

    public int getRgb() {
        return this.rgb;
    }

    public String getName() {
        return this.name != null ? this.name : this.getHexCode();
    }

    private String getHexCode() {
        return String.format(Locale.ROOT, "#%06X", this.rgb);
    }

    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || this.getClass() != o.getClass()) {
            return false;
        }
        TextColor lv = (TextColor)o;
        return this.rgb == lv.rgb;
    }

    public int hashCode() {
        return Objects.hash(this.rgb, this.name);
    }

    public String toString() {
        return this.getName();
    }

    @Nullable
    public static TextColor fromFormatting(Formatting formatting) {
        return FORMATTING_TO_COLOR.get(formatting);
    }

    public static TextColor fromRgb(int rgb) {
        return new TextColor(rgb);
    }

    public static DataResult<TextColor> parse(String name) {
        if (name.startsWith(RGB_PREFIX)) {
            try {
                int i = Integer.parseInt(name.substring(1), 16);
                if (i < 0 || i > 0xFFFFFF) {
                    return DataResult.error(() -> "Color value out of range: " + name);
                }
                return DataResult.success(TextColor.fromRgb(i), Lifecycle.stable());
            } catch (NumberFormatException numberFormatException) {
                return DataResult.error(() -> "Invalid color value: " + name);
            }
        }
        TextColor lv = BY_NAME.get(name);
        if (lv == null) {
            return DataResult.error(() -> "Invalid color name: " + name);
        }
        return DataResult.success(lv, Lifecycle.stable());
    }
}

