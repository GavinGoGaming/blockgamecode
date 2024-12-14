/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.command.argument;

import com.google.gson.JsonObject;
import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.Dynamic2CommandExceptionType;
import com.mojang.brigadier.exceptions.Dynamic3CommandExceptionType;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import com.mojang.datafixers.util.Either;
import java.util.Arrays;
import java.util.Collection;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.function.Predicate;
import net.minecraft.command.CommandRegistryAccess;
import net.minecraft.command.CommandSource;
import net.minecraft.command.argument.RegistryEntryReferenceArgumentType;
import net.minecraft.command.argument.serialize.ArgumentSerializer;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.registry.entry.RegistryEntryList;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

public class RegistryEntryPredicateArgumentType<T>
implements ArgumentType<EntryPredicate<T>> {
    private static final Collection<String> EXAMPLES = Arrays.asList("foo", "foo:bar", "012", "#skeletons", "#minecraft:skeletons");
    private static final Dynamic2CommandExceptionType NOT_FOUND_EXCEPTION = new Dynamic2CommandExceptionType((tag, type) -> Text.stringifiedTranslatable("argument.resource_tag.not_found", tag, type));
    private static final Dynamic3CommandExceptionType WRONG_TYPE_EXCEPTION = new Dynamic3CommandExceptionType((tag, type, expectedType) -> Text.stringifiedTranslatable("argument.resource_tag.invalid_type", tag, type, expectedType));
    private final RegistryWrapper<T> registryWrapper;
    final RegistryKey<? extends Registry<T>> registryRef;

    public RegistryEntryPredicateArgumentType(CommandRegistryAccess registryAccess, RegistryKey<? extends Registry<T>> registryRef) {
        this.registryRef = registryRef;
        this.registryWrapper = registryAccess.getWrapperOrThrow(registryRef);
    }

    public static <T> RegistryEntryPredicateArgumentType<T> registryEntryPredicate(CommandRegistryAccess registryRef, RegistryKey<? extends Registry<T>> registryAccess) {
        return new RegistryEntryPredicateArgumentType<T>(registryRef, registryAccess);
    }

    public static <T> EntryPredicate<T> getRegistryEntryPredicate(CommandContext<ServerCommandSource> context, String name, RegistryKey<Registry<T>> registryRef) throws CommandSyntaxException {
        EntryPredicate lv = context.getArgument(name, EntryPredicate.class);
        Optional<EntryPredicate<T>> optional = lv.tryCast(registryRef);
        return optional.orElseThrow(() -> lv.getEntry().map(entry -> {
            RegistryKey lv = entry.registryKey();
            return RegistryEntryReferenceArgumentType.INVALID_TYPE_EXCEPTION.create(lv.getValue(), lv.getRegistry(), registryRef.getValue());
        }, entryList -> {
            TagKey lv = entryList.getTag();
            return WRONG_TYPE_EXCEPTION.create(lv.id(), lv.registry(), registryRef.getValue());
        }));
    }

    @Override
    public EntryPredicate<T> parse(StringReader stringReader) throws CommandSyntaxException {
        if (stringReader.canRead() && stringReader.peek() == '#') {
            int i = stringReader.getCursor();
            try {
                stringReader.skip();
                Identifier lv = Identifier.fromCommandInput(stringReader);
                TagKey lv2 = TagKey.of(this.registryRef, lv);
                RegistryEntryList.Named lv3 = this.registryWrapper.getOptional(lv2).orElseThrow(() -> NOT_FOUND_EXCEPTION.createWithContext(stringReader, lv, this.registryRef.getValue()));
                return new TagBased(lv3);
            } catch (CommandSyntaxException commandSyntaxException) {
                stringReader.setCursor(i);
                throw commandSyntaxException;
            }
        }
        Identifier lv4 = Identifier.fromCommandInput(stringReader);
        RegistryKey lv5 = RegistryKey.of(this.registryRef, lv4);
        RegistryEntry.Reference lv6 = this.registryWrapper.getOptional(lv5).orElseThrow(() -> RegistryEntryReferenceArgumentType.NOT_FOUND_EXCEPTION.createWithContext(stringReader, lv4, this.registryRef.getValue()));
        return new EntryBased(lv6);
    }

    @Override
    public <S> CompletableFuture<Suggestions> listSuggestions(CommandContext<S> context, SuggestionsBuilder builder) {
        CommandSource.suggestIdentifiers(this.registryWrapper.streamTagKeys().map(TagKey::id), builder, "#");
        return CommandSource.suggestIdentifiers(this.registryWrapper.streamKeys().map(RegistryKey::getValue), builder);
    }

    @Override
    public Collection<String> getExamples() {
        return EXAMPLES;
    }

    @Override
    public /* synthetic */ Object parse(StringReader reader) throws CommandSyntaxException {
        return this.parse(reader);
    }

    public static interface EntryPredicate<T>
    extends Predicate<RegistryEntry<T>> {
        public Either<RegistryEntry.Reference<T>, RegistryEntryList.Named<T>> getEntry();

        public <E> Optional<EntryPredicate<E>> tryCast(RegistryKey<? extends Registry<E>> var1);

        public String asString();
    }

    record TagBased<T>(RegistryEntryList.Named<T> tag) implements EntryPredicate<T>
    {
        @Override
        public Either<RegistryEntry.Reference<T>, RegistryEntryList.Named<T>> getEntry() {
            return Either.right(this.tag);
        }

        @Override
        public <E> Optional<EntryPredicate<E>> tryCast(RegistryKey<? extends Registry<E>> registryRef) {
            return this.tag.getTag().isOf(registryRef) ? Optional.of(this) : Optional.empty();
        }

        @Override
        public boolean test(RegistryEntry<T> arg) {
            return this.tag.contains(arg);
        }

        @Override
        public String asString() {
            return "#" + String.valueOf(this.tag.getTag().id());
        }

        @Override
        public /* synthetic */ boolean test(Object entry) {
            return this.test((RegistryEntry)entry);
        }
    }

    record EntryBased<T>(RegistryEntry.Reference<T> value) implements EntryPredicate<T>
    {
        @Override
        public Either<RegistryEntry.Reference<T>, RegistryEntryList.Named<T>> getEntry() {
            return Either.left(this.value);
        }

        @Override
        public <E> Optional<EntryPredicate<E>> tryCast(RegistryKey<? extends Registry<E>> registryRef) {
            return this.value.registryKey().isOf(registryRef) ? Optional.of(this) : Optional.empty();
        }

        @Override
        public boolean test(RegistryEntry<T> arg) {
            return arg.equals(this.value);
        }

        @Override
        public String asString() {
            return this.value.registryKey().getValue().toString();
        }

        @Override
        public /* synthetic */ boolean test(Object entry) {
            return this.test((RegistryEntry)entry);
        }
    }

    public static class Serializer<T>
    implements ArgumentSerializer<RegistryEntryPredicateArgumentType<T>, Properties> {
        @Override
        public void writePacket(Properties arg, PacketByteBuf arg2) {
            arg2.writeRegistryKey(arg.registryRef);
        }

        @Override
        public Properties fromPacket(PacketByteBuf arg) {
            return new Properties(arg.readRegistryRefKey());
        }

        @Override
        public void writeJson(Properties arg, JsonObject jsonObject) {
            jsonObject.addProperty("registry", arg.registryRef.getValue().toString());
        }

        @Override
        public Properties getArgumentTypeProperties(RegistryEntryPredicateArgumentType<T> arg) {
            return new Properties(arg.registryRef);
        }

        @Override
        public /* synthetic */ ArgumentSerializer.ArgumentTypeProperties fromPacket(PacketByteBuf buf) {
            return this.fromPacket(buf);
        }

        public final class Properties
        implements ArgumentSerializer.ArgumentTypeProperties<RegistryEntryPredicateArgumentType<T>> {
            final RegistryKey<? extends Registry<T>> registryRef;

            Properties(RegistryKey<? extends Registry<T>> registryRef) {
                this.registryRef = registryRef;
            }

            @Override
            public RegistryEntryPredicateArgumentType<T> createType(CommandRegistryAccess arg) {
                return new RegistryEntryPredicateArgumentType(arg, this.registryRef);
            }

            @Override
            public ArgumentSerializer<RegistryEntryPredicateArgumentType<T>, ?> getSerializer() {
                return Serializer.this;
            }

            @Override
            public /* synthetic */ ArgumentType createType(CommandRegistryAccess commandRegistryAccess) {
                return this.createType(commandRegistryAccess);
            }
        }
    }
}

