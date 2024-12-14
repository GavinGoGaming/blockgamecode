/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 * 
 * Could not load the following classes:
 *  org.jetbrains.annotations.Nullable
 *  org.jetbrains.annotations.VisibleForTesting
 */
package net.minecraft.registry.entry;

import com.mojang.datafixers.util.Either;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.Spliterator;
import java.util.function.Function;
import java.util.stream.Stream;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.registry.entry.RegistryEntryOwner;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.util.Util;
import net.minecraft.util.math.random.Random;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.VisibleForTesting;

public interface RegistryEntryList<T>
extends Iterable<RegistryEntry<T>> {
    public Stream<RegistryEntry<T>> stream();

    public int size();

    public Either<TagKey<T>, List<RegistryEntry<T>>> getStorage();

    public Optional<RegistryEntry<T>> getRandom(Random var1);

    public RegistryEntry<T> get(int var1);

    public boolean contains(RegistryEntry<T> var1);

    public boolean ownerEquals(RegistryEntryOwner<T> var1);

    public Optional<TagKey<T>> getTagKey();

    @Deprecated
    @VisibleForTesting
    public static <T> Named<T> of(RegistryEntryOwner<T> owner, TagKey<T> tagKey) {
        return new Named<T>((RegistryEntryOwner)owner, (TagKey)tagKey){

            @Override
            protected List<RegistryEntry<T>> getEntries() {
                throw new UnsupportedOperationException("Tag " + String.valueOf(this.getTag()) + " can't be dereferenced during construction");
            }
        };
    }

    public static <T> RegistryEntryList<T> empty() {
        return Direct.EMPTY;
    }

    @SafeVarargs
    public static <T> Direct<T> of(RegistryEntry<T> ... entries) {
        return new Direct<T>(List.of(entries));
    }

    public static <T> Direct<T> of(List<? extends RegistryEntry<T>> entries) {
        return new Direct(List.copyOf(entries));
    }

    @SafeVarargs
    public static <E, T> Direct<T> of(Function<E, RegistryEntry<T>> mapper, E ... values) {
        return RegistryEntryList.of(Stream.of(values).map(mapper).toList());
    }

    public static <E, T> Direct<T> of(Function<E, RegistryEntry<T>> mapper, Collection<E> values) {
        return RegistryEntryList.of(values.stream().map(mapper).toList());
    }

    public static final class Direct<T>
    extends ListBacked<T> {
        static final Direct<?> EMPTY = new Direct(List.of());
        private final List<RegistryEntry<T>> entries;
        @Nullable
        private Set<RegistryEntry<T>> entrySet;

        Direct(List<RegistryEntry<T>> entries) {
            this.entries = entries;
        }

        @Override
        protected List<RegistryEntry<T>> getEntries() {
            return this.entries;
        }

        @Override
        public Either<TagKey<T>, List<RegistryEntry<T>>> getStorage() {
            return Either.right(this.entries);
        }

        @Override
        public Optional<TagKey<T>> getTagKey() {
            return Optional.empty();
        }

        @Override
        public boolean contains(RegistryEntry<T> entry) {
            if (this.entrySet == null) {
                this.entrySet = Set.copyOf(this.entries);
            }
            return this.entrySet.contains(entry);
        }

        public String toString() {
            return "DirectSet[" + String.valueOf(this.entries) + "]";
        }

        /*
         * Enabled force condition propagation
         * Lifted jumps to return sites
         */
        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }
            if (!(o instanceof Direct)) return false;
            Direct lv = (Direct)o;
            if (!this.entries.equals(lv.entries)) return false;
            return true;
        }

        public int hashCode() {
            return this.entries.hashCode();
        }
    }

    public static class Named<T>
    extends ListBacked<T> {
        private final RegistryEntryOwner<T> owner;
        private final TagKey<T> tag;
        private List<RegistryEntry<T>> entries = List.of();

        Named(RegistryEntryOwner<T> owner, TagKey<T> tag) {
            this.owner = owner;
            this.tag = tag;
        }

        void copyOf(List<RegistryEntry<T>> entries) {
            this.entries = List.copyOf(entries);
        }

        public TagKey<T> getTag() {
            return this.tag;
        }

        @Override
        protected List<RegistryEntry<T>> getEntries() {
            return this.entries;
        }

        @Override
        public Either<TagKey<T>, List<RegistryEntry<T>>> getStorage() {
            return Either.left(this.tag);
        }

        @Override
        public Optional<TagKey<T>> getTagKey() {
            return Optional.of(this.tag);
        }

        @Override
        public boolean contains(RegistryEntry<T> entry) {
            return entry.isIn(this.tag);
        }

        public String toString() {
            return "NamedSet(" + String.valueOf(this.tag) + ")[" + String.valueOf(this.entries) + "]";
        }

        @Override
        public boolean ownerEquals(RegistryEntryOwner<T> owner) {
            return this.owner.ownerEquals(owner);
        }
    }

    public static abstract class ListBacked<T>
    implements RegistryEntryList<T> {
        protected abstract List<RegistryEntry<T>> getEntries();

        @Override
        public int size() {
            return this.getEntries().size();
        }

        @Override
        public Spliterator<RegistryEntry<T>> spliterator() {
            return this.getEntries().spliterator();
        }

        @Override
        public Iterator<RegistryEntry<T>> iterator() {
            return this.getEntries().iterator();
        }

        @Override
        public Stream<RegistryEntry<T>> stream() {
            return this.getEntries().stream();
        }

        @Override
        public Optional<RegistryEntry<T>> getRandom(Random random) {
            return Util.getRandomOrEmpty(this.getEntries(), random);
        }

        @Override
        public RegistryEntry<T> get(int index) {
            return this.getEntries().get(index);
        }

        @Override
        public boolean ownerEquals(RegistryEntryOwner<T> owner) {
            return true;
        }
    }
}

