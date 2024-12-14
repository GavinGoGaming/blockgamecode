/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 * 
 * Could not load the following classes:
 *  org.jetbrains.annotations.Nullable
 */
package net.minecraft.registry;

import com.mojang.datafixers.DataFixUtils;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.Keyable;
import com.mojang.serialization.Lifecycle;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;
import net.minecraft.registry.MutableRegistry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.registry.entry.RegistryEntryInfo;
import net.minecraft.registry.entry.RegistryEntryList;
import net.minecraft.registry.entry.RegistryEntryOwner;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.util.Identifier;
import net.minecraft.util.collection.IndexedIterable;
import net.minecraft.util.dynamic.Codecs;
import net.minecraft.util.math.random.Random;
import org.jetbrains.annotations.Nullable;

public interface Registry<T>
extends Keyable,
IndexedIterable<T> {
    public RegistryKey<? extends Registry<T>> getKey();

    default public Codec<T> getCodec() {
        return this.getReferenceEntryCodec().flatComapMap(RegistryEntry.Reference::value, value -> this.validateReference(this.getEntry(value)));
    }

    default public Codec<RegistryEntry<T>> getEntryCodec() {
        return this.getReferenceEntryCodec().flatComapMap(entry -> entry, this::validateReference);
    }

    private Codec<RegistryEntry.Reference<T>> getReferenceEntryCodec() {
        Codec<RegistryEntry.Reference> codec = Identifier.CODEC.comapFlatMap(id -> this.getEntry((Identifier)id).map(DataResult::success).orElseGet(() -> DataResult.error(() -> "Unknown registry key in " + String.valueOf(this.getKey()) + ": " + String.valueOf(id))), entry -> entry.registryKey().getValue());
        return Codecs.withLifecycle(codec, entry -> this.getEntryInfo(entry.registryKey()).map(RegistryEntryInfo::lifecycle).orElse(Lifecycle.experimental()));
    }

    private DataResult<RegistryEntry.Reference<T>> validateReference(RegistryEntry<T> entry) {
        DataResult<RegistryEntry.Reference<T>> dataResult;
        if (entry instanceof RegistryEntry.Reference) {
            RegistryEntry.Reference lv = (RegistryEntry.Reference)entry;
            dataResult = DataResult.success(lv);
        } else {
            dataResult = DataResult.error(() -> "Unregistered holder in " + String.valueOf(this.getKey()) + ": " + String.valueOf(entry));
        }
        return dataResult;
    }

    default public <U> Stream<U> keys(DynamicOps<U> ops) {
        return this.getIds().stream().map(id -> ops.createString(id.toString()));
    }

    @Nullable
    public Identifier getId(T var1);

    public Optional<RegistryKey<T>> getKey(T var1);

    @Override
    public int getRawId(@Nullable T var1);

    @Nullable
    public T get(@Nullable RegistryKey<T> var1);

    @Nullable
    public T get(@Nullable Identifier var1);

    public Optional<RegistryEntryInfo> getEntryInfo(RegistryKey<T> var1);

    public Lifecycle getLifecycle();

    default public Optional<T> getOrEmpty(@Nullable Identifier id) {
        return Optional.ofNullable(this.get(id));
    }

    default public Optional<T> getOrEmpty(@Nullable RegistryKey<T> key) {
        return Optional.ofNullable(this.get(key));
    }

    public Optional<RegistryEntry.Reference<T>> getDefaultEntry();

    default public T getOrThrow(RegistryKey<T> key) {
        T object = this.get(key);
        if (object == null) {
            throw new IllegalStateException("Missing key in " + String.valueOf(this.getKey()) + ": " + String.valueOf(key));
        }
        return object;
    }

    public Set<Identifier> getIds();

    public Set<Map.Entry<RegistryKey<T>, T>> getEntrySet();

    public Set<RegistryKey<T>> getKeys();

    public Optional<RegistryEntry.Reference<T>> getRandom(Random var1);

    default public Stream<T> stream() {
        return StreamSupport.stream(this.spliterator(), false);
    }

    public boolean containsId(Identifier var1);

    public boolean contains(RegistryKey<T> var1);

    public static <T> T register(Registry<? super T> registry, String id, T entry) {
        return Registry.register(registry, Identifier.of(id), entry);
    }

    public static <V, T extends V> T register(Registry<V> registry, Identifier id, T entry) {
        return Registry.register(registry, RegistryKey.of(registry.getKey(), id), entry);
    }

    public static <V, T extends V> T register(Registry<V> registry, RegistryKey<V> key, T entry) {
        ((MutableRegistry)registry).add(key, entry, RegistryEntryInfo.DEFAULT);
        return entry;
    }

    public static <T> RegistryEntry.Reference<T> registerReference(Registry<T> registry, RegistryKey<T> key, T entry) {
        return ((MutableRegistry)registry).add(key, entry, RegistryEntryInfo.DEFAULT);
    }

    public static <T> RegistryEntry.Reference<T> registerReference(Registry<T> registry, Identifier id, T entry) {
        return Registry.registerReference(registry, RegistryKey.of(registry.getKey(), id), entry);
    }

    public Registry<T> freeze();

    public RegistryEntry.Reference<T> createEntry(T var1);

    public Optional<RegistryEntry.Reference<T>> getEntry(int var1);

    public Optional<RegistryEntry.Reference<T>> getEntry(Identifier var1);

    public Optional<RegistryEntry.Reference<T>> getEntry(RegistryKey<T> var1);

    public RegistryEntry<T> getEntry(T var1);

    default public RegistryEntry.Reference<T> entryOf(RegistryKey<T> key) {
        return this.getEntry(key).orElseThrow(() -> new IllegalStateException("Missing key in " + String.valueOf(this.getKey()) + ": " + String.valueOf(key)));
    }

    public Stream<RegistryEntry.Reference<T>> streamEntries();

    public Optional<RegistryEntryList.Named<T>> getEntryList(TagKey<T> var1);

    default public Iterable<RegistryEntry<T>> iterateEntries(TagKey<T> tag) {
        return DataFixUtils.orElse(this.getEntryList(tag), List.of());
    }

    default public Optional<RegistryEntry<T>> getRandomEntry(TagKey<T> tag, Random random) {
        return this.getEntryList(tag).flatMap(entryList -> entryList.getRandom(random));
    }

    public RegistryEntryList.Named<T> getOrCreateEntryList(TagKey<T> var1);

    public Stream<Pair<TagKey<T>, RegistryEntryList.Named<T>>> streamTagsAndEntries();

    public Stream<TagKey<T>> streamTags();

    public void clearTags();

    public void populateTags(Map<TagKey<T>, List<RegistryEntry<T>>> var1);

    default public IndexedIterable<RegistryEntry<T>> getIndexedEntries() {
        return new IndexedIterable<RegistryEntry<T>>(){

            @Override
            public int getRawId(RegistryEntry<T> arg) {
                return Registry.this.getRawId(arg.value());
            }

            @Override
            @Nullable
            public RegistryEntry<T> get(int i) {
                return Registry.this.getEntry(i).orElse(null);
            }

            @Override
            public int size() {
                return Registry.this.size();
            }

            @Override
            public Iterator<RegistryEntry<T>> iterator() {
                return Registry.this.streamEntries().map(entry -> entry).iterator();
            }

            @Override
            @Nullable
            public /* synthetic */ Object get(int index) {
                return this.get(index);
            }
        };
    }

    public RegistryEntryOwner<T> getEntryOwner();

    public RegistryWrapper.Impl<T> getReadOnlyWrapper();

    default public RegistryWrapper.Impl<T> getTagCreatingWrapper() {
        return new RegistryWrapper.Impl.Delegating<T>(){

            @Override
            public RegistryWrapper.Impl<T> getBase() {
                return Registry.this.getReadOnlyWrapper();
            }

            @Override
            public Optional<RegistryEntryList.Named<T>> getOptional(TagKey<T> tag) {
                return Optional.of(this.getOrThrow(tag));
            }

            @Override
            public RegistryEntryList.Named<T> getOrThrow(TagKey<T> tag) {
                return Registry.this.getOrCreateEntryList(tag);
            }
        };
    }
}

