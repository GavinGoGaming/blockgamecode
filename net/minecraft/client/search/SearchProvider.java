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

@FunctionalInterface
@Environment(value=EnvType.CLIENT)
public interface SearchProvider<T> {
    public static <T> SearchProvider<T> empty() {
        return string -> List.of();
    }

    public static <T> SearchProvider<T> plainText(List<T> list, Function<T, Stream<String>> function) {
        if (list.isEmpty()) {
            return SearchProvider.empty();
        }
        SuffixArray lv = new SuffixArray();
        for (Object object : list) {
            function.apply(object).forEach(string -> lv.add(object, string.toLowerCase(Locale.ROOT)));
        }
        lv.build();
        return lv::findAll;
    }

    public List<T> findAll(String var1);
}

