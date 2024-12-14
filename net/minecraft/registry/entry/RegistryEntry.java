/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 * 
 * Could not load the following classes:
 *  org.jetbrains.annotations.Nullable
 */
package net.minecraft.registry.entry;

import com.mojang.datafixers.util.Either;
import java.util.Collection;
import java.util.Optional;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Stream;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.entry.RegistryEntryOwner;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.Nullable;

public interface RegistryEntry<T> {
    public T value();

    public boolean hasKeyAndValue();

    public boolean matchesId(Identifier var1);

    public boolean matchesKey(RegistryKey<T> var1);

    public boolean matches(Predicate<RegistryKey<T>> var1);

    public boolean isIn(TagKey<T> var1);

    @Deprecated
    public boolean matches(RegistryEntry<T> var1);

    public Stream<TagKey<T>> streamTags();

    public Either<RegistryKey<T>, T> getKeyOrValue();

    public Optional<RegistryKey<T>> getKey();

    public Type getType();

    public boolean ownerEquals(RegistryEntryOwner<T> var1);

    default public String getIdAsString() {
        return this.getKey().map(key -> key.getValue().toString()).orElse("[unregistered]");
    }

    public static <T> RegistryEntry<T> of(T value) {
        return new Direct<T>(value);
    }

    public record Direct<T>(T value) implements RegistryEntry<T>
    {
        @Override
        public boolean hasKeyAndValue() {
            return true;
        }

        @Override
        public boolean matchesId(Identifier id) {
            return false;
        }

        @Override
        public boolean matchesKey(RegistryKey<T> key) {
            return false;
        }

        @Override
        public boolean isIn(TagKey<T> tag) {
            return false;
        }

        @Override
        public boolean matches(RegistryEntry<T> entry) {
            return this.value.equals(entry.value());
        }

        @Override
        public boolean matches(Predicate<RegistryKey<T>> predicate) {
            return false;
        }

        @Override
        public Either<RegistryKey<T>, T> getKeyOrValue() {
            return Either.right(this.value);
        }

        @Override
        public Optional<RegistryKey<T>> getKey() {
            return Optional.empty();
        }

        @Override
        public Type getType() {
            return Type.DIRECT;
        }

        @Override
        public String toString() {
            return "Direct{" + String.valueOf(this.value) + "}";
        }

        @Override
        public boolean ownerEquals(RegistryEntryOwner<T> owner) {
            return true;
        }

        @Override
        public Stream<TagKey<T>> streamTags() {
            return Stream.of(new TagKey[0]);
        }
    }

    public static class Reference<T>
    implements RegistryEntry<T> {
        private final RegistryEntryOwner<T> owner;
        private Set<TagKey<T>> tags = Set.of();
        private final Type referenceType;
        @Nullable
        private RegistryKey<T> registryKey;
        @Nullable
        private T value;

        protected Reference(Type referenceType, RegistryEntryOwner<T> owner, @Nullable RegistryKey<T> registryKey, @Nullable T value) {
            this.owner = owner;
            this.referenceType = referenceType;
            this.registryKey = registryKey;
            this.value = value;
        }

        public static <T> Reference<T> standAlone(RegistryEntryOwner<T> owner, RegistryKey<T> registryKey) {
            return new Reference<Object>(Type.STAND_ALONE, owner, registryKey, null);
        }

        @Deprecated
        public static <T> Reference<T> intrusive(RegistryEntryOwner<T> owner, @Nullable T value) {
            return new Reference<T>(Type.INTRUSIVE, owner, null, value);
        }

        public RegistryKey<T> registryKey() {
            if (this.registryKey == null) {
                throw new IllegalStateException("Trying to access unbound value '" + String.valueOf(this.value) + "' from registry " + String.valueOf(this.owner));
            }
            return this.registryKey;
        }

        @Override
        public T value() {
            if (this.value == null) {
                throw new IllegalStateException("Trying to access unbound value '" + String.valueOf(this.registryKey) + "' from registry " + String.valueOf(this.owner));
            }
            return this.value;
        }

        @Override
        public boolean matchesId(Identifier id) {
            return this.registryKey().getValue().equals(id);
        }

        @Override
        public boolean matchesKey(RegistryKey<T> key) {
            return this.registryKey() == key;
        }

        @Override
        public boolean isIn(TagKey<T> tag) {
            return this.tags.contains(tag);
        }

        @Override
        public boolean matches(RegistryEntry<T> entry) {
            return entry.matchesKey(this.registryKey());
        }

        @Override
        public boolean matches(Predicate<RegistryKey<T>> predicate) {
            return predicate.test(this.registryKey());
        }

        @Override
        public boolean ownerEquals(RegistryEntryOwner<T> owner) {
            return this.owner.ownerEquals(owner);
        }

        @Override
        public Either<RegistryKey<T>, T> getKeyOrValue() {
            return Either.left(this.registryKey());
        }

        @Override
        public Optional<RegistryKey<T>> getKey() {
            return Optional.of(this.registryKey());
        }

        @Override
        public net.minecraft.registry.entry.RegistryEntry$Type getType() {
            return net.minecraft.registry.entry.RegistryEntry$Type.REFERENCE;
        }

        @Override
        public boolean hasKeyAndValue() {
            return this.registryKey != null && this.value != null;
        }

        void setRegistryKey(RegistryKey<T> registryKey) {
            if (this.registryKey != null && registryKey != this.registryKey) {
                throw new IllegalStateException("Can't change holder key: existing=" + String.valueOf(this.registryKey) + ", new=" + String.valueOf(registryKey));
            }
            this.registryKey = registryKey;
        }

        protected void setValue(T value) {
            if (this.referenceType == Type.INTRUSIVE && this.value != value) {
                throw new IllegalStateException("Can't change holder " + String.valueOf(this.registryKey) + " value: existing=" + String.valueOf(this.value) + ", new=" + String.valueOf(value));
            }
            this.value = value;
        }

        void setTags(Collection<TagKey<T>> tags) {
            this.tags = Set.copyOf(tags);
        }

        @Override
        public Stream<TagKey<T>> streamTags() {
            return this.tags.stream();
        }

        public String toString() {
            return "Reference{" + String.valueOf(this.registryKey) + "=" + String.valueOf(this.value) + "}";
        }

        protected static enum Type {
            STAND_ALONE,
            INTRUSIVE;

        }
    }

    public static enum Type {
        REFERENCE,
        DIRECT;

    }
}

