/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.resource;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Stream;
import net.minecraft.resource.Resource;
import net.minecraft.resource.ResourceFactory;
import net.minecraft.resource.ResourcePack;
import net.minecraft.util.Identifier;

public interface ResourceManager
extends ResourceFactory {
    public Set<String> getAllNamespaces();

    public List<Resource> getAllResources(Identifier var1);

    public Map<Identifier, Resource> findResources(String var1, Predicate<Identifier> var2);

    public Map<Identifier, List<Resource>> findAllResources(String var1, Predicate<Identifier> var2);

    public Stream<ResourcePack> streamResourcePacks();

    public static enum Empty implements ResourceManager
    {
        INSTANCE;


        @Override
        public Set<String> getAllNamespaces() {
            return Set.of();
        }

        @Override
        public Optional<Resource> getResource(Identifier id) {
            return Optional.empty();
        }

        @Override
        public List<Resource> getAllResources(Identifier id) {
            return List.of();
        }

        @Override
        public Map<Identifier, Resource> findResources(String startingPath, Predicate<Identifier> allowedPathPredicate) {
            return Map.of();
        }

        @Override
        public Map<Identifier, List<Resource>> findAllResources(String startingPath, Predicate<Identifier> allowedPathPredicate) {
            return Map.of();
        }

        @Override
        public Stream<ResourcePack> streamResourcePacks() {
            return Stream.of(new ResourcePack[0]);
        }
    }
}

