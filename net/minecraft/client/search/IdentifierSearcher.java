/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 * 
 * Could not load the following classes:
 *  net.fabricmc.api.EnvType
 *  net.fabricmc.api.Environment
 */
package net.minecraft.client.search;

import java.util.List;
import java.util.Locale;
import java.util.function.Function;
import java.util.stream.Stream;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.search.SuffixArray;
import net.minecraft.util.Identifier;

@Environment(value=EnvType.CLIENT)
public interface IdentifierSearcher<T> {
    public static <T> IdentifierSearcher<T> of() {
        return new IdentifierSearcher<T>(){

            @Override
            public List<T> searchNamespace(String namespace) {
                return List.of();
            }

            @Override
            public List<T> searchPath(String path) {
                return List.of();
            }
        };
    }

    public static <T> IdentifierSearcher<T> of(List<T> values, Function<T, Stream<Identifier>> identifiersGetter) {
        if (values.isEmpty()) {
            return IdentifierSearcher.of();
        }
        final SuffixArray lv = new SuffixArray();
        final SuffixArray lv2 = new SuffixArray();
        for (Object object : values) {
            identifiersGetter.apply(object).forEach(id -> {
                lv.add(object, id.getNamespace().toLowerCase(Locale.ROOT));
                lv2.add(object, id.getPath().toLowerCase(Locale.ROOT));
            });
        }
        lv.build();
        lv2.build();
        return new IdentifierSearcher<T>(){

            @Override
            public List<T> searchNamespace(String namespace) {
                return lv.findAll(namespace);
            }

            @Override
            public List<T> searchPath(String path) {
                return lv2.findAll(path);
            }
        };
    }

    public List<T> searchNamespace(String var1);

    public List<T> searchPath(String var1);
}

