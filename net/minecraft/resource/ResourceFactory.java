/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.resource;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Map;
import java.util.Optional;
import net.minecraft.resource.Resource;
import net.minecraft.util.Identifier;

@FunctionalInterface
public interface ResourceFactory {
    public static final ResourceFactory MISSING = id -> Optional.empty();

    public Optional<Resource> getResource(Identifier var1);

    default public Resource getResourceOrThrow(Identifier id) throws FileNotFoundException {
        return this.getResource(id).orElseThrow(() -> new FileNotFoundException(id.toString()));
    }

    default public InputStream open(Identifier id) throws IOException {
        return this.getResourceOrThrow(id).getInputStream();
    }

    default public BufferedReader openAsReader(Identifier id) throws IOException {
        return this.getResourceOrThrow(id).getReader();
    }

    public static ResourceFactory fromMap(Map<Identifier, Resource> map) {
        return id -> Optional.ofNullable((Resource)map.get(id));
    }
}

