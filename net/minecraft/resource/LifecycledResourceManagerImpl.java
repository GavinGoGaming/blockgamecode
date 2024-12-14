/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 * 
 * Could not load the following classes:
 *  org.jetbrains.annotations.Nullable
 */
package net.minecraft.resource;

import com.mojang.logging.LogUtils;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.TreeMap;
import java.util.function.Predicate;
import java.util.stream.Stream;
import net.minecraft.resource.LifecycledResourceManager;
import net.minecraft.resource.NamespaceResourceManager;
import net.minecraft.resource.Resource;
import net.minecraft.resource.ResourceManager;
import net.minecraft.resource.ResourcePack;
import net.minecraft.resource.ResourceType;
import net.minecraft.resource.metadata.ResourceFilter;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;

public class LifecycledResourceManagerImpl
implements LifecycledResourceManager {
    private static final Logger LOGGER = LogUtils.getLogger();
    private final Map<String, NamespaceResourceManager> subManagers;
    private final List<ResourcePack> packs;

    public LifecycledResourceManagerImpl(ResourceType type, List<ResourcePack> packs) {
        this.packs = List.copyOf(packs);
        HashMap<String, NamespaceResourceManager> map = new HashMap<String, NamespaceResourceManager>();
        List list2 = packs.stream().flatMap(pack -> pack.getNamespaces(type).stream()).distinct().toList();
        for (ResourcePack lv : packs) {
            ResourceFilter lv2 = this.parseResourceFilter(lv);
            Set<String> set = lv.getNamespaces(type);
            Predicate<Identifier> predicate = lv2 != null ? id -> lv2.isPathBlocked(id.getPath()) : null;
            for (String string : list2) {
                boolean bl2;
                boolean bl = set.contains(string);
                boolean bl3 = bl2 = lv2 != null && lv2.isNamespaceBlocked(string);
                if (!bl && !bl2) continue;
                NamespaceResourceManager lv3 = (NamespaceResourceManager)map.get(string);
                if (lv3 == null) {
                    lv3 = new NamespaceResourceManager(type, string);
                    map.put(string, lv3);
                }
                if (bl && bl2) {
                    lv3.addPack(lv, predicate);
                    continue;
                }
                if (bl) {
                    lv3.addPack(lv);
                    continue;
                }
                lv3.addPack(lv.getId(), predicate);
            }
        }
        this.subManagers = map;
    }

    @Nullable
    private ResourceFilter parseResourceFilter(ResourcePack pack) {
        try {
            return pack.parseMetadata(ResourceFilter.SERIALIZER);
        } catch (IOException iOException) {
            LOGGER.error("Failed to get filter section from pack {}", (Object)pack.getId());
            return null;
        }
    }

    @Override
    public Set<String> getAllNamespaces() {
        return this.subManagers.keySet();
    }

    @Override
    public Optional<Resource> getResource(Identifier id) {
        ResourceManager lv = this.subManagers.get(id.getNamespace());
        if (lv != null) {
            return lv.getResource(id);
        }
        return Optional.empty();
    }

    @Override
    public List<Resource> getAllResources(Identifier id) {
        ResourceManager lv = this.subManagers.get(id.getNamespace());
        if (lv != null) {
            return lv.getAllResources(id);
        }
        return List.of();
    }

    @Override
    public Map<Identifier, Resource> findResources(String startingPath, Predicate<Identifier> allowedPathPredicate) {
        LifecycledResourceManagerImpl.validateStartingPath(startingPath);
        TreeMap<Identifier, Resource> map = new TreeMap<Identifier, Resource>();
        for (NamespaceResourceManager lv : this.subManagers.values()) {
            map.putAll(lv.findResources(startingPath, allowedPathPredicate));
        }
        return map;
    }

    @Override
    public Map<Identifier, List<Resource>> findAllResources(String startingPath, Predicate<Identifier> allowedPathPredicate) {
        LifecycledResourceManagerImpl.validateStartingPath(startingPath);
        TreeMap<Identifier, List<Resource>> map = new TreeMap<Identifier, List<Resource>>();
        for (NamespaceResourceManager lv : this.subManagers.values()) {
            map.putAll(lv.findAllResources(startingPath, allowedPathPredicate));
        }
        return map;
    }

    private static void validateStartingPath(String startingPath) {
        if (startingPath.endsWith("/")) {
            throw new IllegalArgumentException("Trailing slash in path " + startingPath);
        }
    }

    @Override
    public Stream<ResourcePack> streamResourcePacks() {
        return this.packs.stream();
    }

    @Override
    public void close() {
        this.packs.forEach(ResourcePack::close);
    }
}

