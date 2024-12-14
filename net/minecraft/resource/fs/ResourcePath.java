/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 * 
 * Could not load the following classes:
 *  org.jetbrains.annotations.Nullable
 */
package net.minecraft.resource.fs;

import com.google.common.base.Joiner;
import com.google.common.collect.ImmutableList;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.FileSystem;
import java.nio.file.LinkOption;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.nio.file.ProviderMismatchException;
import java.nio.file.ReadOnlyFileSystemException;
import java.nio.file.WatchEvent;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;
import java.nio.file.attribute.BasicFileAttributeView;
import java.nio.file.attribute.BasicFileAttributes;
import java.nio.file.attribute.FileTime;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import net.minecraft.resource.fs.ResourceFile;
import net.minecraft.resource.fs.ResourceFileAttributes;
import net.minecraft.resource.fs.ResourceFileSystem;
import org.jetbrains.annotations.Nullable;

class ResourcePath
implements Path {
    private static final BasicFileAttributes DIRECTORY_ATTRIBUTES = new ResourceFileAttributes(){

        @Override
        public boolean isRegularFile() {
            return false;
        }

        @Override
        public boolean isDirectory() {
            return true;
        }
    };
    private static final BasicFileAttributes FILE_ATTRIBUTES = new ResourceFileAttributes(){

        @Override
        public boolean isRegularFile() {
            return true;
        }

        @Override
        public boolean isDirectory() {
            return false;
        }
    };
    private static final Comparator<ResourcePath> COMPARATOR = Comparator.comparing(ResourcePath::getPathString);
    private final String name;
    private final ResourceFileSystem fileSystem;
    @Nullable
    private final ResourcePath parent;
    @Nullable
    private List<String> names;
    @Nullable
    private String pathString;
    private final ResourceFile file;

    public ResourcePath(ResourceFileSystem fileSystem, String name, @Nullable ResourcePath parent, ResourceFile file) {
        this.fileSystem = fileSystem;
        this.name = name;
        this.parent = parent;
        this.file = file;
    }

    private ResourcePath relativize(@Nullable ResourcePath path, String name) {
        return new ResourcePath(this.fileSystem, name, path, ResourceFile.RELATIVE);
    }

    @Override
    public ResourceFileSystem getFileSystem() {
        return this.fileSystem;
    }

    @Override
    public boolean isAbsolute() {
        return this.file != ResourceFile.RELATIVE;
    }

    @Override
    public File toFile() {
        ResourceFile resourceFile = this.file;
        if (resourceFile instanceof ResourceFile.File) {
            ResourceFile.File lv = (ResourceFile.File)resourceFile;
            return lv.contents().toFile();
        }
        throw new UnsupportedOperationException("Path " + this.getPathString() + " does not represent file");
    }

    @Override
    @Nullable
    public ResourcePath getRoot() {
        if (this.isAbsolute()) {
            return this.fileSystem.getRoot();
        }
        return null;
    }

    @Override
    public ResourcePath getFileName() {
        return this.relativize(null, this.name);
    }

    @Override
    @Nullable
    public ResourcePath getParent() {
        return this.parent;
    }

    @Override
    public int getNameCount() {
        return this.getNames().size();
    }

    private List<String> getNames() {
        if (this.name.isEmpty()) {
            return List.of();
        }
        if (this.names == null) {
            ImmutableList.Builder builder = ImmutableList.builder();
            if (this.parent != null) {
                builder.addAll(this.parent.getNames());
            }
            builder.add(this.name);
            this.names = builder.build();
        }
        return this.names;
    }

    @Override
    public ResourcePath getName(int i) {
        List<String> list = this.getNames();
        if (i < 0 || i >= list.size()) {
            throw new IllegalArgumentException("Invalid index: " + i);
        }
        return this.relativize(null, list.get(i));
    }

    @Override
    public ResourcePath subpath(int i, int j) {
        List<String> list = this.getNames();
        if (i < 0 || j > list.size() || i >= j) {
            throw new IllegalArgumentException();
        }
        ResourcePath lv = null;
        for (int k = i; k < j; ++k) {
            lv = this.relativize(lv, list.get(k));
        }
        return lv;
    }

    @Override
    public boolean startsWith(Path other) {
        if (other.isAbsolute() != this.isAbsolute()) {
            return false;
        }
        if (other instanceof ResourcePath) {
            ResourcePath lv = (ResourcePath)other;
            if (lv.fileSystem != this.fileSystem) {
                return false;
            }
            List<String> list = this.getNames();
            List<String> list2 = lv.getNames();
            int i = list2.size();
            if (i > list.size()) {
                return false;
            }
            for (int j = 0; j < i; ++j) {
                if (list2.get(j).equals(list.get(j))) continue;
                return false;
            }
            return true;
        }
        return false;
    }

    @Override
    public boolean endsWith(Path other) {
        if (other.isAbsolute() && !this.isAbsolute()) {
            return false;
        }
        if (other instanceof ResourcePath) {
            ResourcePath lv = (ResourcePath)other;
            if (lv.fileSystem != this.fileSystem) {
                return false;
            }
            List<String> list = this.getNames();
            List<String> list2 = lv.getNames();
            int i = list2.size();
            int j = list.size() - i;
            if (j < 0) {
                return false;
            }
            for (int k = i - 1; k >= 0; --k) {
                if (list2.get(k).equals(list.get(j + k))) continue;
                return false;
            }
            return true;
        }
        return false;
    }

    @Override
    public ResourcePath normalize() {
        return this;
    }

    @Override
    public ResourcePath resolve(Path path) {
        ResourcePath lv = this.toResourcePath(path);
        if (path.isAbsolute()) {
            return lv;
        }
        return this.get(lv.getNames());
    }

    private ResourcePath get(List<String> paths) {
        ResourcePath lv = this;
        for (String string : paths) {
            lv = lv.get(string);
        }
        return lv;
    }

    ResourcePath get(String name) {
        if (ResourcePath.isSpecial(this.file)) {
            return new ResourcePath(this.fileSystem, name, this, this.file);
        }
        ResourceFile resourceFile = this.file;
        if (resourceFile instanceof ResourceFile.Directory) {
            ResourceFile.Directory lv = (ResourceFile.Directory)resourceFile;
            ResourcePath lv2 = lv.children().get(name);
            return lv2 != null ? lv2 : new ResourcePath(this.fileSystem, name, this, ResourceFile.EMPTY);
        }
        if (this.file instanceof ResourceFile.File) {
            return new ResourcePath(this.fileSystem, name, this, ResourceFile.EMPTY);
        }
        throw new AssertionError((Object)"All content types should be already handled");
    }

    private static boolean isSpecial(ResourceFile file) {
        return file == ResourceFile.EMPTY || file == ResourceFile.RELATIVE;
    }

    @Override
    public ResourcePath relativize(Path path) {
        ResourcePath lv = this.toResourcePath(path);
        if (this.isAbsolute() != lv.isAbsolute()) {
            throw new IllegalArgumentException("absolute mismatch");
        }
        List<String> list = this.getNames();
        List<String> list2 = lv.getNames();
        if (list.size() >= list2.size()) {
            throw new IllegalArgumentException();
        }
        for (int i = 0; i < list.size(); ++i) {
            if (list.get(i).equals(list2.get(i))) continue;
            throw new IllegalArgumentException();
        }
        return lv.subpath(list.size(), list2.size());
    }

    @Override
    public URI toUri() {
        try {
            return new URI("x-mc-link", this.fileSystem.getStore().name(), this.getPathString(), null);
        } catch (URISyntaxException uRISyntaxException) {
            throw new AssertionError("Failed to create URI", uRISyntaxException);
        }
    }

    @Override
    public ResourcePath toAbsolutePath() {
        if (this.isAbsolute()) {
            return this;
        }
        return this.fileSystem.getRoot().resolve(this);
    }

    @Override
    public ResourcePath toRealPath(LinkOption ... linkOptions) {
        return this.toAbsolutePath();
    }

    @Override
    public WatchKey register(WatchService watcher, WatchEvent.Kind<?>[] events, WatchEvent.Modifier ... modifiers) {
        throw new UnsupportedOperationException();
    }

    @Override
    public int compareTo(Path path) {
        ResourcePath lv = this.toResourcePath(path);
        return COMPARATOR.compare(this, lv);
    }

    @Override
    public boolean equals(Object o) {
        if (o == this) {
            return true;
        }
        if (o instanceof ResourcePath) {
            ResourcePath lv = (ResourcePath)o;
            if (this.fileSystem != lv.fileSystem) {
                return false;
            }
            boolean bl = this.isNormal();
            if (bl != lv.isNormal()) {
                return false;
            }
            if (bl) {
                return this.file == lv.file;
            }
            return Objects.equals(this.parent, lv.parent) && Objects.equals(this.name, lv.name);
        }
        return false;
    }

    private boolean isNormal() {
        return !ResourcePath.isSpecial(this.file);
    }

    @Override
    public int hashCode() {
        return this.isNormal() ? this.file.hashCode() : this.name.hashCode();
    }

    @Override
    public String toString() {
        return this.getPathString();
    }

    private String getPathString() {
        if (this.pathString == null) {
            StringBuilder stringBuilder = new StringBuilder();
            if (this.isAbsolute()) {
                stringBuilder.append("/");
            }
            Joiner.on("/").appendTo(stringBuilder, (Iterable<? extends Object>)this.getNames());
            this.pathString = stringBuilder.toString();
        }
        return this.pathString;
    }

    private ResourcePath toResourcePath(@Nullable Path path) {
        if (path == null) {
            throw new NullPointerException();
        }
        if (path instanceof ResourcePath) {
            ResourcePath lv = (ResourcePath)path;
            if (lv.fileSystem == this.fileSystem) {
                return lv;
            }
        }
        throw new ProviderMismatchException();
    }

    public boolean isReadable() {
        return this.isNormal();
    }

    @Nullable
    public Path toPath() {
        Path path;
        ResourceFile resourceFile = this.file;
        if (resourceFile instanceof ResourceFile.File) {
            ResourceFile.File lv = (ResourceFile.File)resourceFile;
            path = lv.contents();
        } else {
            path = null;
        }
        return path;
    }

    @Nullable
    public ResourceFile.Directory toDirectory() {
        ResourceFile.Directory lv;
        ResourceFile resourceFile = this.file;
        return resourceFile instanceof ResourceFile.Directory ? (lv = (ResourceFile.Directory)resourceFile) : null;
    }

    public BasicFileAttributeView getAttributeView() {
        return new BasicFileAttributeView(){

            @Override
            public String name() {
                return "basic";
            }

            @Override
            public BasicFileAttributes readAttributes() throws IOException {
                return ResourcePath.this.getAttributes();
            }

            @Override
            public void setTimes(FileTime lastModifiedTime, FileTime lastAccessFile, FileTime createTime) {
                throw new ReadOnlyFileSystemException();
            }
        };
    }

    public BasicFileAttributes getAttributes() throws IOException {
        if (this.file instanceof ResourceFile.Directory) {
            return DIRECTORY_ATTRIBUTES;
        }
        if (this.file instanceof ResourceFile.File) {
            return FILE_ATTRIBUTES;
        }
        throw new NoSuchFileException(this.getPathString());
    }

    @Override
    public /* synthetic */ Path toRealPath(LinkOption[] options) throws IOException {
        return this.toRealPath(options);
    }

    @Override
    public /* synthetic */ Path toAbsolutePath() {
        return this.toAbsolutePath();
    }

    @Override
    public /* synthetic */ Path relativize(Path other) {
        return this.relativize(other);
    }

    @Override
    public /* synthetic */ Path resolve(Path other) {
        return this.resolve(other);
    }

    @Override
    public /* synthetic */ Path normalize() {
        return this.normalize();
    }

    @Override
    public /* synthetic */ Path subpath(int beginIndex, int endIndex) {
        return this.subpath(beginIndex, endIndex);
    }

    @Override
    public /* synthetic */ Path getName(int index) {
        return this.getName(index);
    }

    @Override
    @Nullable
    public /* synthetic */ Path getParent() {
        return this.getParent();
    }

    @Override
    public /* synthetic */ Path getFileName() {
        return this.getFileName();
    }

    @Override
    @Nullable
    public /* synthetic */ Path getRoot() {
        return this.getRoot();
    }

    @Override
    public /* synthetic */ FileSystem getFileSystem() {
        return this.getFileSystem();
    }
}

