/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 * 
 * Could not load the following classes:
 *  net.fabricmc.api.EnvType
 *  net.fabricmc.api.Environment
 *  org.jetbrains.annotations.Nullable
 */
package net.minecraft.client.gl;

import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.util.PathUtil;
import net.minecraft.util.StringHelper;
import org.jetbrains.annotations.Nullable;

@Environment(value=EnvType.CLIENT)
public abstract class GlImportProcessor {
    private static final String MULTI_LINE_COMMENT_PATTERN = "/\\*(?:[^*]|\\*+[^*/])*\\*+/";
    private static final String SINGLE_LINE_COMMENT_PATTERN = "//[^\\v]*";
    private static final Pattern MOJ_IMPORT_PATTERN = Pattern.compile("(#(?:/\\*(?:[^*]|\\*+[^*/])*\\*+/|\\h)*moj_import(?:/\\*(?:[^*]|\\*+[^*/])*\\*+/|\\h)*(?:\"(.*)\"|<(.*)>))");
    private static final Pattern IMPORT_VERSION_PATTERN = Pattern.compile("(#(?:/\\*(?:[^*]|\\*+[^*/])*\\*+/|\\h)*version(?:/\\*(?:[^*]|\\*+[^*/])*\\*+/|\\h)*(\\d+))\\b");
    private static final Pattern TRAILING_WHITESPACE_PATTERN = Pattern.compile("(?:^|\\v)(?:\\s|/\\*(?:[^*]|\\*+[^*/])*\\*+/|(//[^\\v]*))*\\z");

    public List<String> readSource(String source) {
        Context lv = new Context();
        List<String> list = this.parseImports(source, lv, "");
        list.set(0, this.readImport(list.get(0), lv.column));
        return list;
    }

    private List<String> parseImports(String source, Context context, String path) {
        String string4;
        int i = context.line;
        int j = 0;
        String string3 = "";
        ArrayList<String> list = Lists.newArrayList();
        Matcher matcher = MOJ_IMPORT_PATTERN.matcher(source);
        while (matcher.find()) {
            int k;
            boolean bl;
            if (GlImportProcessor.hasBogusString(source, matcher, j)) continue;
            string4 = matcher.group(2);
            boolean bl2 = bl = string4 != null;
            if (!bl) {
                string4 = matcher.group(3);
            }
            if (string4 == null) continue;
            String string5 = source.substring(j, matcher.start(1));
            String string6 = path + string4;
            Object string7 = this.loadImport(bl, string6);
            if (!Strings.isNullOrEmpty((String)string7)) {
                if (!StringHelper.endsWithLineBreak((String)string7)) {
                    string7 = (String)string7 + System.lineSeparator();
                }
                ++context.line;
                k = context.line;
                List<String> list2 = this.parseImports((String)string7, context, bl ? PathUtil.getPosixFullPath(string6) : "");
                list2.set(0, String.format(Locale.ROOT, "#line %d %d\n%s", 0, k, this.extractVersion(list2.get(0), context)));
                if (!StringHelper.isBlank(string5)) {
                    list.add(string5);
                }
                list.addAll(list2);
            } else {
                String string8 = bl ? String.format(Locale.ROOT, "/*#moj_import \"%s\"*/", string4) : String.format(Locale.ROOT, "/*#moj_import <%s>*/", string4);
                list.add(string3 + string5 + string8);
            }
            k = StringHelper.countLines(source.substring(0, matcher.end(1)));
            string3 = String.format(Locale.ROOT, "#line %d %d", k, i);
            j = matcher.end(1);
        }
        string4 = source.substring(j);
        if (!StringHelper.isBlank(string4)) {
            list.add(string3 + string4);
        }
        return list;
    }

    private String extractVersion(String line, Context context) {
        Matcher matcher = IMPORT_VERSION_PATTERN.matcher(line);
        if (matcher.find() && GlImportProcessor.isLineValid(line, matcher)) {
            context.column = Math.max(context.column, Integer.parseInt(matcher.group(2)));
            return line.substring(0, matcher.start(1)) + "/*" + line.substring(matcher.start(1), matcher.end(1)) + "*/" + line.substring(matcher.end(1));
        }
        return line;
    }

    private String readImport(String line, int start) {
        Matcher matcher = IMPORT_VERSION_PATTERN.matcher(line);
        if (matcher.find() && GlImportProcessor.isLineValid(line, matcher)) {
            return line.substring(0, matcher.start(2)) + Math.max(start, Integer.parseInt(matcher.group(2))) + line.substring(matcher.end(2));
        }
        return line;
    }

    private static boolean isLineValid(String line, Matcher matcher) {
        return !GlImportProcessor.hasBogusString(line, matcher, 0);
    }

    private static boolean hasBogusString(String string, Matcher matcher, int matchEnd) {
        int j = matcher.start() - matchEnd;
        if (j == 0) {
            return false;
        }
        Matcher matcher2 = TRAILING_WHITESPACE_PATTERN.matcher(string.substring(matchEnd, matcher.start()));
        if (!matcher2.find()) {
            return true;
        }
        int k = matcher2.end(1);
        return k == matcher.start();
    }

    @Nullable
    public abstract String loadImport(boolean var1, String var2);

    @Environment(value=EnvType.CLIENT)
    static final class Context {
        int column;
        int line;

        Context() {
        }
    }
}

