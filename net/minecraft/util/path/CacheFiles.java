/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.util.path;

import com.mojang.logging.LogUtils;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.DirectoryNotEmptyException;
import java.nio.file.FileVisitResult;
import java.nio.file.FileVisitor;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.nio.file.attribute.FileTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import org.slf4j.Logger;

public class CacheFiles {
    private static final Logger LOGGER = LogUtils.getLogger();

    public static void clear(Path directory, int maxRetained) {
        try {
            List<CacheFile> list = CacheFiles.findCacheFiles(directory);
            int j = list.size() - maxRetained;
            if (j <= 0) {
                return;
            }
            list.sort(CacheFile.COMPARATOR);
            List<CacheEntry> list2 = CacheFiles.toCacheEntries(list);
            Collections.reverse(list2);
            list2.sort(CacheEntry.COMPARATOR);
            HashSet<Path> set = new HashSet<Path>();
            for (int k = 0; k < j; ++k) {
                CacheEntry lv = list2.get(k);
                Path path2 = lv.path;
                try {
                    Files.delete(path2);
                    if (lv.removalPriority != 0) continue;
                    set.add(path2.getParent());
                    continue;
                } catch (IOException iOException) {
                    LOGGER.warn("Failed to delete cache file {}", (Object)path2, (Object)iOException);
                }
            }
            set.remove(directory);
            for (Path path3 : set) {
                try {
                    Files.delete(path3);
                } catch (DirectoryNotEmptyException path2) {
                } catch (IOException iOException2) {
                    LOGGER.warn("Failed to delete empty(?) cache directory {}", (Object)path3, (Object)iOException2);
                }
            }
        } catch (IOException | UncheckedIOException exception) {
            LOGGER.error("Failed to vacuum cache dir {}", (Object)directory, (Object)exception);
        }
    }

    private static List<CacheFile> findCacheFiles(final Path directory) throws IOException {
        try {
            final ArrayList<CacheFile> list = new ArrayList<CacheFile>();
            Files.walkFileTree(directory, (FileVisitor<? super Path>)new SimpleFileVisitor<Path>(){

                @Override
                public FileVisitResult visitFile(Path path, BasicFileAttributes basicFileAttributes) {
                    if (basicFileAttributes.isRegularFile() && !path.getParent().equals(directory)) {
                        FileTime fileTime = basicFileAttributes.lastModifiedTime();
                        list.add(new CacheFile(path, fileTime));
                    }
                    return FileVisitResult.CONTINUE;
                }

                @Override
                public /* synthetic */ FileVisitResult visitFile(Object path, BasicFileAttributes attributes) throws IOException {
                    return this.visitFile((Path)path, attributes);
                }
            });
            return list;
        } catch (NoSuchFileException noSuchFileException) {
            return List.of();
        }
    }

    private static List<CacheEntry> toCacheEntries(List<CacheFile> files) {
        ArrayList<CacheEntry> list2 = new ArrayList<CacheEntry>();
        Object2IntOpenHashMap<Path> object2IntOpenHashMap = new Object2IntOpenHashMap<Path>();
        for (CacheFile lv : files) {
            int i = object2IntOpenHashMap.addTo(lv.path.getParent(), 1);
            list2.add(new CacheEntry(lv.path, i));
        }
        return list2;
    }

    record CacheFile(Path path, FileTime modifiedTime) {
        public static final Comparator<CacheFile> COMPARATOR = Comparator.comparing(CacheFile::modifiedTime).reversed();
    }

    record CacheEntry(Path path, int removalPriority) {
        public static final Comparator<CacheEntry> COMPARATOR = Comparator.comparing(CacheEntry::removalPriority).reversed();
    }
}

