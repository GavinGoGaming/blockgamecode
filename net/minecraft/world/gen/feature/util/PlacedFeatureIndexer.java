/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.world.gen.feature.util;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.ListIterator;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.function.Function;
import java.util.function.ToIntFunction;
import java.util.stream.Collectors;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.registry.entry.RegistryEntryList;
import net.minecraft.util.TopologicalSorts;
import net.minecraft.util.Util;
import net.minecraft.world.gen.feature.PlacedFeature;
import org.apache.commons.lang3.mutable.MutableInt;

public class PlacedFeatureIndexer {
    public static <T> List<IndexedFeatures> collectIndexedFeatures(List<T> biomes, Function<T, List<RegistryEntryList<PlacedFeature>>> biomesToPlacedFeaturesList, boolean listInvolvedBiomesOnFailure) {
        record IndexedFeature(int featureIndex, int step, PlacedFeature feature) {
        }
        ArrayList<IndexedFeature> list2;
        Object2IntOpenHashMap<PlacedFeature> object2IntMap = new Object2IntOpenHashMap<PlacedFeature>();
        MutableInt mutableInt = new MutableInt(0);
        Comparator<IndexedFeature> comparator = Comparator.comparingInt(IndexedFeature::step).thenComparingInt(IndexedFeature::featureIndex);
        TreeMap<IndexedFeature, Set> map = new TreeMap<IndexedFeature, Set>(comparator);
        int i = 0;
        for (T object : biomes) {
            int j;
            list2 = Lists.newArrayList();
            List<RegistryEntryList<PlacedFeature>> list3 = biomesToPlacedFeaturesList.apply(object);
            i = Math.max(i, list3.size());
            for (j = 0; j < list3.size(); ++j) {
                for (RegistryEntry lv : (RegistryEntryList)list3.get(j)) {
                    PlacedFeature lv2 = (PlacedFeature)lv.value();
                    list2.add(new IndexedFeature(object2IntMap.computeIfAbsent(lv2, feature -> mutableInt.getAndIncrement()), j, lv2));
                }
            }
            for (j = 0; j < list2.size(); ++j) {
                Set set = map.computeIfAbsent((IndexedFeature)list2.get(j), feature -> new TreeSet(comparator));
                if (j >= list2.size() - 1) continue;
                set.add((IndexedFeature)list2.get(j + 1));
            }
        }
        TreeSet<IndexedFeature> set2 = new TreeSet<IndexedFeature>(comparator);
        TreeSet<IndexedFeature> set3 = new TreeSet<IndexedFeature>(comparator);
        list2 = Lists.newArrayList();
        for (IndexedFeature lv3 : map.keySet()) {
            if (!set3.isEmpty()) {
                throw new IllegalStateException("You somehow broke the universe; DFS bork (iteration finished with non-empty in-progress vertex set");
            }
            if (set2.contains(lv3) || !TopologicalSorts.sort(map, set2, set3, list2::add, lv3)) continue;
            if (listInvolvedBiomesOnFailure) {
                int k;
                ArrayList<T> list4 = new ArrayList<T>(biomes);
                do {
                    k = list4.size();
                    ListIterator listIterator = list4.listIterator();
                    while (listIterator.hasNext()) {
                        Object object2 = listIterator.next();
                        listIterator.remove();
                        try {
                            PlacedFeatureIndexer.collectIndexedFeatures(list4, biomesToPlacedFeaturesList, false);
                        } catch (IllegalStateException illegalStateException) {
                            continue;
                        }
                        listIterator.add(object2);
                    }
                } while (k != list4.size());
                throw new IllegalStateException("Feature order cycle found, involved sources: " + String.valueOf(list4));
            }
            throw new IllegalStateException("Feature order cycle found");
        }
        Collections.reverse(list2);
        ImmutableList.Builder builder = ImmutableList.builder();
        int j = 0;
        while (j < i) {
            int l = j++;
            List<PlacedFeature> list5 = list2.stream().filter(feature -> feature.step() == l).map(IndexedFeature::feature).collect(Collectors.toList());
            builder.add(new IndexedFeatures(list5));
        }
        return builder.build();
    }

    public record IndexedFeatures(List<PlacedFeature> features, ToIntFunction<PlacedFeature> indexMapping) {
        IndexedFeatures(List<PlacedFeature> features) {
            this(features, Util.lastIdentityIndexGetter(features));
        }
    }
}

