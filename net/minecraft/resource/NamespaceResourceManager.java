/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 * 
 * Could not load the following classes:
 *  org.jetbrains.annotations.Nullable
 */
package net.minecraft.resource;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.mojang.logging.LogUtils;
import it.unimi.dsi.fastutil.objects.Object2ObjectArrayMap;
import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.lang.invoke.LambdaMetafactory;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.TreeMap;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.Stream;
import net.minecraft.resource.InputSupplier;
import net.minecraft.resource.Resource;
import net.minecraft.resource.ResourceManager;
import net.minecraft.resource.ResourcePack;
import net.minecraft.resource.ResourceType;
import net.minecraft.resource.metadata.ResourceMetadata;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;

public class NamespaceResourceManager
implements ResourceManager {
    static final Logger LOGGER = LogUtils.getLogger();
    protected final List<FilterablePack> packList = Lists.newArrayList();
    private final ResourceType type;
    private final String namespace;

    public NamespaceResourceManager(ResourceType type, String namespace) {
        this.type = type;
        this.namespace = namespace;
    }

    public void addPack(ResourcePack pack) {
        this.addPack(pack.getId(), pack, null);
    }

    public void addPack(ResourcePack pack, Predicate<Identifier> filter) {
        this.addPack(pack.getId(), pack, filter);
    }

    public void addPack(String id, Predicate<Identifier> filter) {
        this.addPack(id, null, filter);
    }

    private void addPack(String id, @Nullable ResourcePack underlyingPack, @Nullable Predicate<Identifier> filter) {
        this.packList.add(new FilterablePack(id, underlyingPack, filter));
    }

    @Override
    public Set<String> getAllNamespaces() {
        return ImmutableSet.of(this.namespace);
    }

    @Override
    public Optional<Resource> getResource(Identifier id) {
        for (int i = this.packList.size() - 1; i >= 0; --i) {
            InputSupplier<InputStream> lv3;
            FilterablePack lv = this.packList.get(i);
            ResourcePack lv2 = lv.underlying;
            if (lv2 != null && (lv3 = lv2.open(this.type, id)) != null) {
                InputSupplier<ResourceMetadata> lv4 = this.createMetadataSupplier(id, i);
                return Optional.of(NamespaceResourceManager.createResource(lv2, id, lv3, lv4));
            }
            if (!lv.isFiltered(id)) continue;
            LOGGER.warn("Resource {} not found, but was filtered by pack {}", (Object)id, (Object)lv.name);
            return Optional.empty();
        }
        return Optional.empty();
    }

    private static Resource createResource(ResourcePack pack, Identifier id, InputSupplier<InputStream> supplier, InputSupplier<ResourceMetadata> metadataSupplier) {
        return new Resource(pack, NamespaceResourceManager.wrapForDebug(id, pack, supplier), metadataSupplier);
    }

    private static InputSupplier<InputStream> wrapForDebug(Identifier id, ResourcePack pack, InputSupplier<InputStream> supplier) {
        if (LOGGER.isDebugEnabled()) {
            return () -> new DebugInputStream((InputStream)supplier.get(), id, pack.getId());
        }
        return supplier;
    }

    @Override
    public List<Resource> getAllResources(Identifier id) {
        Identifier lv = NamespaceResourceManager.getMetadataPath(id);
        ArrayList<Resource> list = new ArrayList<Resource>();
        boolean bl = false;
        String string = null;
        for (int i = this.packList.size() - 1; i >= 0; --i) {
            InputSupplier<InputStream> lv4;
            FilterablePack lv2 = this.packList.get(i);
            ResourcePack lv3 = lv2.underlying;
            if (lv3 != null && (lv4 = lv3.open(this.type, id)) != null) {
                InputSupplier<ResourceMetadata> lv5 = bl ? ResourceMetadata.NONE_SUPPLIER : () -> {
                    InputSupplier<InputStream> lv = lv3.open(this.type, lv);
                    return lv != null ? NamespaceResourceManager.loadMetadata(lv) : ResourceMetadata.NONE;
                };
                list.add(new Resource(lv3, lv4, lv5));
            }
            if (lv2.isFiltered(id)) {
                string = lv2.name;
                break;
            }
            if (!lv2.isFiltered(lv)) continue;
            bl = true;
        }
        if (list.isEmpty() && string != null) {
            LOGGER.warn("Resource {} not found, but was filtered by pack {}", (Object)id, (Object)string);
        }
        return Lists.reverse(list);
    }

    private static boolean isMcmeta(Identifier id) {
        return id.getPath().endsWith(".mcmeta");
    }

    private static Identifier getMetadataFileName(Identifier id) {
        String string = id.getPath().substring(0, id.getPath().length() - ".mcmeta".length());
        return id.withPath(string);
    }

    static Identifier getMetadataPath(Identifier id) {
        return id.withPath(id.getPath() + ".mcmeta");
    }

    @Override
    public Map<Identifier, Resource> findResources(String startingPath, Predicate<Identifier> allowedPathPredicate) {
        record Result(ResourcePack pack, InputSupplier<InputStream> supplier, int packIndex) {
        }
        HashMap<Identifier, Result> map = new HashMap<Identifier, Result>();
        HashMap map2 = new HashMap();
        int i = this.packList.size();
        for (int j = 0; j < i; ++j) {
            FilterablePack lv = this.packList.get(j);
            lv.removeFiltered(map.keySet());
            lv.removeFiltered(map2.keySet());
            ResourcePack lv2 = lv.underlying;
            if (lv2 == null) continue;
            int k = j;
            lv2.findResources(this.type, this.namespace, startingPath, (id, supplier) -> {
                if (NamespaceResourceManager.isMcmeta(id)) {
                    if (allowedPathPredicate.test(NamespaceResourceManager.getMetadataFileName(id))) {
                        map2.put(id, new Result(lv2, (InputSupplier<InputStream>)supplier, k));
                    }
                } else if (allowedPathPredicate.test((Identifier)id)) {
                    map.put((Identifier)id, new Result(lv2, (InputSupplier<InputStream>)supplier, k));
                }
            });
        }
        TreeMap<Identifier, Resource> map3 = Maps.newTreeMap();
        map.forEach((id, result) -> {
            Identifier lv = NamespaceResourceManager.getMetadataPath(id);
            Result lv2 = (Result)map2.get(lv);
            InputSupplier<ResourceMetadata> lv3 = lv2 != null && lv2.packIndex >= result.packIndex ? NamespaceResourceManager.getMetadataSupplier(lv2.supplier) : ResourceMetadata.NONE_SUPPLIER;
            map3.put((Identifier)id, NamespaceResourceManager.createResource(result.pack, id, result.supplier, lv3));
        });
        return map3;
    }

    private InputSupplier<ResourceMetadata> createMetadataSupplier(Identifier id, int index) {
        return () -> {
            Identifier lv = NamespaceResourceManager.getMetadataPath(id);
            for (int j = this.packList.size() - 1; j >= index; --j) {
                InputSupplier<InputStream> lv4;
                FilterablePack lv2 = this.packList.get(j);
                ResourcePack lv3 = lv2.underlying;
                if (lv3 != null && (lv4 = lv3.open(this.type, lv)) != null) {
                    return NamespaceResourceManager.loadMetadata(lv4);
                }
                if (lv2.isFiltered(lv)) break;
            }
            return ResourceMetadata.NONE;
        };
    }

    private static InputSupplier<ResourceMetadata> getMetadataSupplier(InputSupplier<InputStream> supplier) {
        return () -> NamespaceResourceManager.loadMetadata(supplier);
    }

    private static ResourceMetadata loadMetadata(InputSupplier<InputStream> supplier) throws IOException {
        try (InputStream inputStream = supplier.get();){
            ResourceMetadata resourceMetadata = ResourceMetadata.create(inputStream);
            return resourceMetadata;
        }
    }

    private static void applyFilter(FilterablePack pack, Map<Identifier, EntryList> idToEntryList) {
        for (EntryList lv : idToEntryList.values()) {
            if (pack.isFiltered(lv.id)) {
                lv.fileSources.clear();
                continue;
            }
            if (!pack.isFiltered(lv.metadataId())) continue;
            lv.metaSources.clear();
        }
    }

    private void findAndAdd(FilterablePack pack, String startingPath, Predicate<Identifier> allowedPathPredicate, Map<Identifier, EntryList> idToEntryList) {
        ResourcePack lv = pack.underlying;
        if (lv == null) {
            return;
        }
        lv.findResources(this.type, this.namespace, startingPath, (id, supplier) -> {
            if (NamespaceResourceManager.isMcmeta(id)) {
                Identifier lv = NamespaceResourceManager.getMetadataFileName(id);
                if (!allowedPathPredicate.test(lv)) {
                    return;
                }
                map.computeIfAbsent(lv, (Function<Identifier, EntryList>)LambdaMetafactory.metafactory(null, null, null, (Ljava/lang/Object;)Ljava/lang/Object;, <init>(net.minecraft.util.Identifier ), (Lnet/minecraft/util/Identifier;)Lnet/minecraft/resource/NamespaceResourceManager$EntryList;)()).metaSources.put(lv, (InputSupplier<InputStream>)supplier);
            } else {
                if (!allowedPathPredicate.test((Identifier)id)) {
                    return;
                }
                map.computeIfAbsent(id, (Function<Identifier, EntryList>)LambdaMetafactory.metafactory(null, null, null, (Ljava/lang/Object;)Ljava/lang/Object;, <init>(net.minecraft.util.Identifier ), (Lnet/minecraft/util/Identifier;)Lnet/minecraft/resource/NamespaceResourceManager$EntryList;)()).fileSources.add(new FileSource(lv, (InputSupplier<InputStream>)supplier));
            }
        });
    }

    @Override
    public Map<Identifier, List<Resource>> findAllResources(String startingPath, Predicate<Identifier> allowedPathPredicate) {
        HashMap<Identifier, EntryList> map = Maps.newHashMap();
        for (FilterablePack lv : this.packList) {
            NamespaceResourceManager.applyFilter(lv, map);
            this.findAndAdd(lv, startingPath, allowedPathPredicate, map);
        }
        TreeMap<Identifier, List<Resource>> treeMap = Maps.newTreeMap();
        for (EntryList lv2 : map.values()) {
            if (lv2.fileSources.isEmpty()) continue;
            ArrayList<Resource> list = new ArrayList<Resource>();
            for (FileSource lv3 : lv2.fileSources) {
                ResourcePack lv4 = lv3.sourcePack;
                InputSupplier<InputStream> lv5 = lv2.metaSources.get(lv4);
                InputSupplier<ResourceMetadata> lv6 = lv5 != null ? NamespaceResourceManager.getMetadataSupplier(lv5) : ResourceMetadata.NONE_SUPPLIER;
                list.add(NamespaceResourceManager.createResource(lv4, lv2.id, lv3.supplier, lv6));
            }
            treeMap.put(lv2.id, list);
        }
        return treeMap;
    }

    @Override
    public Stream<ResourcePack> streamResourcePacks() {
        return this.packList.stream().map(pack -> pack.underlying).filter(Objects::nonNull);
    }

    record FilterablePack(String name, @Nullable ResourcePack underlying, @Nullable Predicate<Identifier> filter) {
        public void removeFiltered(Collection<Identifier> ids) {
            if (this.filter != null) {
                ids.removeIf(this.filter);
            }
        }

        public boolean isFiltered(Identifier id) {
            return this.filter != null && this.filter.test(id);
        }

        @Nullable
        public ResourcePack underlying() {
            return this.underlying;
        }

        @Nullable
        public Predicate<Identifier> filter() {
            return this.filter;
        }
    }

    record EntryList(Identifier id, Identifier metadataId, List<FileSource> fileSources, Map<ResourcePack, InputSupplier<InputStream>> metaSources) {
        EntryList(Identifier id) {
            this(id, NamespaceResourceManager.getMetadataPath(id), new ArrayList<FileSource>(), new Object2ObjectArrayMap<ResourcePack, InputSupplier<InputStream>>());
        }
    }

    record FileSource(ResourcePack sourcePack, InputSupplier<InputStream> supplier) {
    }

    static class DebugInputStream
    extends FilterInputStream {
        private final Supplier<String> leakMessage;
        private boolean closed;

        public DebugInputStream(InputStream parent, Identifier id, String packId) {
            super(parent);
            Exception exception = new Exception("Stacktrace");
            this.leakMessage = () -> {
                StringWriter stringWriter = new StringWriter();
                exception.printStackTrace(new PrintWriter(stringWriter));
                return "Leaked resource: '" + String.valueOf(id) + "' loaded from pack: '" + packId + "'\n" + String.valueOf(stringWriter);
            };
        }

        @Override
        public void close() throws IOException {
            super.close();
            this.closed = true;
        }

        protected void finalize() throws Throwable {
            if (!this.closed) {
                LOGGER.warn("{}", (Object)this.leakMessage.get());
            }
            super.finalize();
        }
    }
}

