/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.text;

import java.util.Optional;
import net.minecraft.text.CharacterVisitor;
import net.minecraft.text.StringVisitable;
import net.minecraft.text.Style;
import net.minecraft.util.Formatting;
import net.minecraft.util.Unit;

public class TextVisitFactory {
    private static final char REPLACEMENT_CHARACTER = '\ufffd';
    private static final Optional<Object> VISIT_TERMINATED = Optional.of(Unit.INSTANCE);

    private static boolean visitRegularCharacter(Style style, CharacterVisitor visitor, int index, char c) {
        if (Character.isSurrogate(c)) {
            return visitor.accept(index, style, 65533);
        }
        return visitor.accept(index, style, c);
    }

    public static boolean visitForwards(String text, Style style, CharacterVisitor visitor) {
        int i = text.length();
        for (int j = 0; j < i; ++j) {
            char c = text.charAt(j);
            if (Character.isHighSurrogate(c)) {
                if (j + 1 >= i) {
                    if (visitor.accept(j, style, 65533)) break;
                    return false;
                }
                char d = text.charAt(j + 1);
                if (Character.isLowSurrogate(d)) {
                    if (!visitor.accept(j, style, Character.toCodePoint(c, d))) {
                        return false;
                    }
                    ++j;
                    continue;
                }
                if (visitor.accept(j, style, 65533)) continue;
                return false;
            }
            if (TextVisitFactory.visitRegularCharacter(style, visitor, j, c)) continue;
            return false;
        }
        return true;
    }

    public static boolean visitBackwards(String text, Style style, CharacterVisitor visitor) {
        int i = text.length();
        for (int j = i - 1; j >= 0; --j) {
            char c = text.charAt(j);
            if (Character.isLowSurrogate(c)) {
                if (j - 1 < 0) {
                    if (visitor.accept(0, style, 65533)) break;
                    return false;
                }
                char d = text.charAt(j - 1);
                if (!(Character.isHighSurrogate(d) ? !visitor.accept(--j, style, Character.toCodePoint(d, c)) : !visitor.accept(j, style, 65533))) continue;
                return false;
            }
            if (TextVisitFactory.visitRegularCharacter(style, visitor, j, c)) continue;
            return false;
        }
        return true;
    }

    public static boolean visitFormatted(String text, Style style, CharacterVisitor visitor) {
        return TextVisitFactory.visitFormatted(text, 0, style, visitor);
    }

    public static boolean visitFormatted(String text, int startIndex, Style style, CharacterVisitor visitor) {
        return TextVisitFactory.visitFormatted(text, startIndex, style, style, visitor);
    }

    public static boolean visitFormatted(String text, int startIndex, Style startingStyle, Style resetStyle, CharacterVisitor visitor) {
        int j = text.length();
        Style lv = startingStyle;
        for (int k = startIndex; k < j; ++k) {
            char d;
            char c = text.charAt(k);
            if (c == '\u00a7') {
                if (k + 1 >= j) break;
                d = text.charAt(k + 1);
                Formatting lv2 = Formatting.byCode(d);
                if (lv2 != null) {
                    lv = lv2 == Formatting.RESET ? resetStyle : lv.withExclusiveFormatting(lv2);
                }
                ++k;
                continue;
            }
            if (Character.isHighSurrogate(c)) {
                if (k + 1 >= j) {
                    if (visitor.accept(k, lv, 65533)) break;
                    return false;
                }
                d = text.charAt(k + 1);
                if (Character.isLowSurrogate(d)) {
                    if (!visitor.accept(k, lv, Character.toCodePoint(c, d))) {
                        return false;
                    }
                    ++k;
                    continue;
                }
                if (visitor.accept(k, lv, 65533)) continue;
                return false;
            }
            if (TextVisitFactory.visitRegularCharacter(lv, visitor, k, c)) continue;
            return false;
        }
        return true;
    }

    public static boolean visitFormatted(StringVisitable text, Style style, CharacterVisitor visitor) {
        return text.visit((stylex, string) -> TextVisitFactory.visitFormatted(string, 0, stylex, visitor) ? Optional.empty() : VISIT_TERMINATED, style).isEmpty();
    }

    public static String validateSurrogates(String text) {
        StringBuilder stringBuilder = new StringBuilder();
        TextVisitFactory.visitForwards(text, Style.EMPTY, (index, style, codePoint) -> {
            stringBuilder.appendCodePoint(codePoint);
            return true;
        });
        return stringBuilder.toString();
    }

    public static String removeFormattingCodes(StringVisitable text) {
        StringBuilder stringBuilder = new StringBuilder();
        TextVisitFactory.visitFormatted(text, Style.EMPTY, (int index, Style style, int codePoint) -> {
            stringBuilder.appendCodePoint(codePoint);
            return true;
        });
        return stringBuilder.toString();
    }
}

