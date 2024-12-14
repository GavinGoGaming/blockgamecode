/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.util;

import com.mojang.serialization.DataResult;
import java.io.File;
import java.io.IOException;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.Files;
import java.nio.file.InvalidPathException;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.FileAttribute;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import net.minecraft.SharedConstants;
import org.apache.commons.io.FilenameUtils;

public class PathUtil {
    private static final Pattern FILE_NAME_WITH_COUNT = Pattern.compile("(<name>.*) \\((<count>\\d*)\\)", 66);
    private static final int MAX_NAME_LENGTH = 255;
    private static final Pattern RESERVED_WINDOWS_NAMES = Pattern.compile(".*\\.|(?:COM|CLOCK\\$|CON|PRN|AUX|NUL|COM[1-9]|LPT[1-9])(?:\\..*)?", 2);
    private static final Pattern VALID_FILE_NAME = Pattern.compile("[-._a-z0-9]+");

    public static String replaceInvalidChars(String fileName) {
        for (char c : SharedConstants.INVALID_CHARS_LEVEL_NAME) {
            fileName = fileName.replace(c, '_');
        }
        return fileName.replaceAll("[./\"]", "_");
    }

    public static String getNextUniqueName(Path path, String name, String extension) throws IOException {
        if (RESERVED_WINDOWS_NAMES.matcher((CharSequence)(name = PathUtil.replaceInvalidChars((String)name))).matches()) {
            name = "_" + (String)name + "_";
        }
        Matcher matcher = FILE_NAME_WITH_COUNT.matcher((CharSequence)name);
        int i = 0;
        if (matcher.matches()) {
            name = matcher.group("name");
            i = Integer.parseInt(matcher.group("count"));
        }
        if (((String)name).length() > 255 - extension.length()) {
            name = ((String)name).substring(0, 255 - extension.length());
        }
        while (true) {
            Object string3 = name;
            if (i != 0) {
                String string4 = " (" + i + ")";
                int j = 255 - string4.length();
                if (((String)string3).length() > j) {
                    string3 = ((String)string3).substring(0, j);
                }
                string3 = (String)string3 + string4;
            }
            string3 = (String)string3 + extension;
            Path path2 = path.resolve((String)string3);
            try {
                Path path3 = Files.createDirectory(path2, new FileAttribute[0]);
                Files.deleteIfExists(path3);
                return path.relativize(path3).toString();
            } catch (FileAlreadyExistsException fileAlreadyExistsException) {
                ++i;
                continue;
            }
            break;
        }
    }

    public static boolean isNormal(Path path) {
        Path path2 = path.normalize();
        return path2.equals(path);
    }

    public static boolean isAllowedName(Path path) {
        for (Path path2 : path) {
            if (!RESERVED_WINDOWS_NAMES.matcher(path2.toString()).matches()) continue;
            return false;
        }
        return true;
    }

    public static Path getResourcePath(Path path, String resourceName, String extension) {
        String string3 = resourceName + extension;
        Path path2 = Paths.get(string3, new String[0]);
        if (path2.endsWith(extension)) {
            throw new InvalidPathException(string3, "empty resource name");
        }
        return path.resolve(path2);
    }

    public static String getPosixFullPath(String path) {
        return FilenameUtils.getFullPath(path).replace(File.separator, "/");
    }

    public static String normalizeToPosix(String path) {
        return FilenameUtils.normalize(path).replace(File.separator, "/");
    }

    public static DataResult<List<String>> split(String path) {
        int i = path.indexOf(47);
        if (i == -1) {
            return switch (path) {
                case "", ".", ".." -> DataResult.error(() -> "Invalid path '" + path + "'");
                default -> !PathUtil.isFileNameValid(path) ? DataResult.error(() -> "Invalid path '" + path + "'") : DataResult.success(List.of(path));
            };
        }
        ArrayList<String> list = new ArrayList<String>();
        int j = 0;
        boolean bl = false;
        while (true) {
            String string2;
            switch (string2 = path.substring(j, i)) {
                case "": 
                case ".": 
                case "..": {
                    return DataResult.error(() -> "Invalid segment '" + string2 + "' in path '" + path + "'");
                }
            }
            if (!PathUtil.isFileNameValid(string2)) {
                return DataResult.error(() -> "Invalid segment '" + string2 + "' in path '" + path + "'");
            }
            list.add(string2);
            if (bl) {
                return DataResult.success(list);
            }
            j = i + 1;
            if ((i = path.indexOf(47, j)) != -1) continue;
            i = path.length();
            bl = true;
        }
    }

    public static Path getPath(Path root, List<String> paths) {
        int i = paths.size();
        return switch (i) {
            case 0 -> root;
            case 1 -> root.resolve(paths.get(0));
            default -> {
                String[] strings = new String[i - 1];
                for (int j = 1; j < i; ++j) {
                    strings[j - 1] = paths.get(j);
                }
                yield root.resolve(root.getFileSystem().getPath(paths.get(0), strings));
            }
        };
    }

    public static boolean isFileNameValid(String name) {
        return VALID_FILE_NAME.matcher(name).matches();
    }

    public static void validatePath(String ... paths) {
        if (paths.length == 0) {
            throw new IllegalArgumentException("Path must have at least one element");
        }
        for (String string : paths) {
            if (!string.equals("..") && !string.equals(".") && PathUtil.isFileNameValid(string)) continue;
            throw new IllegalArgumentException("Illegal segment " + string + " in path " + Arrays.toString(paths));
        }
    }

    public static void createDirectories(Path path) throws IOException {
        Files.createDirectories(Files.exists(path, new LinkOption[0]) ? path.toRealPath(new LinkOption[0]) : path, new FileAttribute[0]);
    }
}

