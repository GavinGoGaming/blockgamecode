/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 * 
 * Could not load the following classes:
 *  org.jetbrains.annotations.Nullable
 */
package net.minecraft.util;

import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import net.minecraft.util.math.MathHelper;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.Nullable;

public class StringHelper {
    private static final Pattern FORMATTING_CODE = Pattern.compile("(?i)\\u00A7[0-9A-FK-OR]");
    private static final Pattern LINE_BREAK = Pattern.compile("\\r\\n|\\v");
    private static final Pattern ENDS_WITH_LINE_BREAK = Pattern.compile("(?:\\r\\n|\\v)$");

    public static String formatTicks(int ticks, float tickRate) {
        int j = MathHelper.floor((float)ticks / tickRate);
        int k = j / 60;
        j %= 60;
        int l = k / 60;
        k %= 60;
        if (l > 0) {
            return String.format(Locale.ROOT, "%02d:%02d:%02d", l, k, j);
        }
        return String.format(Locale.ROOT, "%02d:%02d", k, j);
    }

    public static String stripTextFormat(String text) {
        return FORMATTING_CODE.matcher(text).replaceAll("");
    }

    public static boolean isEmpty(@Nullable String text) {
        return StringUtils.isEmpty(text);
    }

    public static String truncate(String text, int maxLength, boolean addEllipsis) {
        if (text.length() <= maxLength) {
            return text;
        }
        if (addEllipsis && maxLength > 3) {
            return text.substring(0, maxLength - 3) + "...";
        }
        return text.substring(0, maxLength);
    }

    public static int countLines(String text) {
        if (text.isEmpty()) {
            return 0;
        }
        Matcher matcher = LINE_BREAK.matcher(text);
        int i = 1;
        while (matcher.find()) {
            ++i;
        }
        return i;
    }

    public static boolean endsWithLineBreak(String text) {
        return ENDS_WITH_LINE_BREAK.matcher(text).find();
    }

    public static String truncateChat(String text) {
        return StringHelper.truncate(text, 256, false);
    }

    public static boolean isValidChar(char c) {
        return c != '\u00a7' && c >= ' ' && c != '\u007f';
    }

    public static boolean isValidPlayerName(String name) {
        if (name.length() > 16) {
            return false;
        }
        return name.chars().filter(c -> c <= 32 || c >= 127).findAny().isEmpty();
    }

    public static String stripInvalidChars(String string) {
        return StringHelper.stripInvalidChars(string, false);
    }

    public static String stripInvalidChars(String string, boolean allowLinebreak) {
        StringBuilder stringBuilder = new StringBuilder();
        for (char c : string.toCharArray()) {
            if (StringHelper.isValidChar(c)) {
                stringBuilder.append(c);
                continue;
            }
            if (!allowLinebreak || c != '\n') continue;
            stringBuilder.append(c);
        }
        return stringBuilder.toString();
    }

    public static boolean isWhitespace(int c) {
        return Character.isWhitespace(c) || Character.isSpaceChar(c);
    }

    public static boolean isBlank(@Nullable String string) {
        if (string == null || string.length() == 0) {
            return true;
        }
        return string.chars().allMatch(StringHelper::isWhitespace);
    }
}

