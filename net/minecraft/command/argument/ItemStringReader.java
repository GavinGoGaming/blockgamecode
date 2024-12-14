/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.command.argument;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.Dynamic2CommandExceptionType;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.DynamicOps;
import it.unimi.dsi.fastutil.objects.ReferenceArraySet;
import java.util.Locale;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;
import net.minecraft.command.CommandSource;
import net.minecraft.component.ComponentChanges;
import net.minecraft.component.ComponentMapImpl;
import net.minecraft.component.ComponentType;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.StringNbtReader;
import net.minecraft.registry.Registries;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.Unit;
import org.apache.commons.lang3.mutable.MutableObject;

public class ItemStringReader {
    static final DynamicCommandExceptionType INVALID_ITEM_ID_EXCEPTION = new DynamicCommandExceptionType(id -> Text.stringifiedTranslatable("argument.item.id.invalid", id));
    static final DynamicCommandExceptionType UNKNOWN_COMPONENT_EXCEPTION = new DynamicCommandExceptionType(id -> Text.stringifiedTranslatable("arguments.item.component.unknown", id));
    static final Dynamic2CommandExceptionType MALFORMED_COMPONENT_EXCEPTION = new Dynamic2CommandExceptionType((type, error) -> Text.stringifiedTranslatable("arguments.item.component.malformed", type, error));
    static final SimpleCommandExceptionType COMPONENT_EXPECTED_EXCEPTION = new SimpleCommandExceptionType(Text.translatable("arguments.item.component.expected"));
    static final DynamicCommandExceptionType REPEATED_COMPONENT_EXCEPTION = new DynamicCommandExceptionType(type -> Text.stringifiedTranslatable("arguments.item.component.repeated", type));
    private static final DynamicCommandExceptionType MALFORMED_ITEM_EXCEPTION = new DynamicCommandExceptionType(error -> Text.stringifiedTranslatable("arguments.item.malformed", error));
    public static final char OPEN_SQUARE_BRACKET = '[';
    public static final char CLOSED_SQUARE_BRACKET = ']';
    public static final char COMMA = ',';
    public static final char EQUAL_SIGN = '=';
    public static final char EXCLAMATION_MARK = '!';
    static final Function<SuggestionsBuilder, CompletableFuture<Suggestions>> SUGGEST_DEFAULT = SuggestionsBuilder::buildFuture;
    final RegistryWrapper.Impl<Item> itemRegistry;
    final DynamicOps<NbtElement> nbtOps;

    public ItemStringReader(RegistryWrapper.WrapperLookup registryLookup) {
        this.itemRegistry = registryLookup.getWrapperOrThrow(RegistryKeys.ITEM);
        this.nbtOps = registryLookup.getOps(NbtOps.INSTANCE);
    }

    public ItemResult consume(StringReader reader) throws CommandSyntaxException {
        final MutableObject mutableObject = new MutableObject();
        final ComponentChanges.Builder lv = ComponentChanges.builder();
        this.consume(reader, new Callbacks(){

            @Override
            public void onItem(RegistryEntry<Item> item) {
                mutableObject.setValue(item);
            }

            @Override
            public <T> void onComponentAdded(ComponentType<T> type, T value) {
                lv.add(type, value);
            }

            @Override
            public <T> void onComponentRemoved(ComponentType<T> type) {
                lv.remove(type);
            }
        });
        RegistryEntry lv2 = Objects.requireNonNull((RegistryEntry)mutableObject.getValue(), "Parser gave no item");
        ComponentChanges lv3 = lv.build();
        ItemStringReader.validate(reader, lv2, lv3);
        return new ItemResult(lv2, lv3);
    }

    private static void validate(StringReader reader, RegistryEntry<Item> item, ComponentChanges components) throws CommandSyntaxException {
        ComponentMapImpl lv = ComponentMapImpl.create(item.value().getComponents(), components);
        DataResult<Unit> dataResult = ItemStack.validateComponents(lv);
        dataResult.getOrThrow(error -> MALFORMED_ITEM_EXCEPTION.createWithContext(reader, error));
    }

    public void consume(StringReader reader, Callbacks callbacks) throws CommandSyntaxException {
        int i = reader.getCursor();
        try {
            new Reader(reader, callbacks).read();
        } catch (CommandSyntaxException commandSyntaxException) {
            reader.setCursor(i);
            throw commandSyntaxException;
        }
    }

    public CompletableFuture<Suggestions> getSuggestions(SuggestionsBuilder builder) {
        StringReader stringReader = new StringReader(builder.getInput());
        stringReader.setCursor(builder.getStart());
        SuggestionCallbacks lv = new SuggestionCallbacks();
        Reader lv2 = new Reader(stringReader, lv);
        try {
            lv2.read();
        } catch (CommandSyntaxException commandSyntaxException) {
            // empty catch block
        }
        return lv.getSuggestions(builder, stringReader);
    }

    public static interface Callbacks {
        default public void onItem(RegistryEntry<Item> item) {
        }

        default public <T> void onComponentAdded(ComponentType<T> type, T value) {
        }

        default public <T> void onComponentRemoved(ComponentType<T> type) {
        }

        default public void setSuggestor(Function<SuggestionsBuilder, CompletableFuture<Suggestions>> suggestor) {
        }
    }

    public record ItemResult(RegistryEntry<Item> item, ComponentChanges components) {
    }

    class Reader {
        private final StringReader reader;
        private final Callbacks callbacks;

        Reader(StringReader reader, Callbacks callbacks) {
            this.reader = reader;
            this.callbacks = callbacks;
        }

        public void read() throws CommandSyntaxException {
            this.callbacks.setSuggestor(this::suggestItems);
            this.readItem();
            this.callbacks.setSuggestor(this::suggestBracket);
            if (this.reader.canRead() && this.reader.peek() == '[') {
                this.callbacks.setSuggestor(SUGGEST_DEFAULT);
                this.readComponents();
            }
        }

        private void readItem() throws CommandSyntaxException {
            int i = this.reader.getCursor();
            Identifier lv = Identifier.fromCommandInput(this.reader);
            this.callbacks.onItem((RegistryEntry<Item>)ItemStringReader.this.itemRegistry.getOptional(RegistryKey.of(RegistryKeys.ITEM, lv)).orElseThrow(() -> {
                this.reader.setCursor(i);
                return INVALID_ITEM_ID_EXCEPTION.createWithContext(this.reader, lv);
            }));
        }

        private void readComponents() throws CommandSyntaxException {
            this.reader.expect('[');
            this.callbacks.setSuggestor(this::suggestComponents);
            ReferenceArraySet set = new ReferenceArraySet();
            while (this.reader.canRead() && this.reader.peek() != ']') {
                this.reader.skipWhitespace();
                if (this.reader.canRead() && this.reader.peek() == '!') {
                    this.reader.skip();
                    this.callbacks.setSuggestor(this::suggestComponentsToRemove);
                    lv = Reader.readComponentType(this.reader);
                    if (!set.add(lv)) {
                        throw REPEATED_COMPONENT_EXCEPTION.create(lv);
                    }
                    this.callbacks.onComponentRemoved(lv);
                    this.callbacks.setSuggestor(SUGGEST_DEFAULT);
                    this.reader.skipWhitespace();
                } else {
                    lv = Reader.readComponentType(this.reader);
                    if (!set.add(lv)) {
                        throw REPEATED_COMPONENT_EXCEPTION.create(lv);
                    }
                    this.callbacks.setSuggestor(this::suggestEqual);
                    this.reader.skipWhitespace();
                    this.reader.expect('=');
                    this.callbacks.setSuggestor(SUGGEST_DEFAULT);
                    this.reader.skipWhitespace();
                    this.readComponentValue(lv);
                    this.reader.skipWhitespace();
                }
                this.callbacks.setSuggestor(this::suggestEndOfComponent);
                if (!this.reader.canRead() || this.reader.peek() != ',') break;
                this.reader.skip();
                this.reader.skipWhitespace();
                this.callbacks.setSuggestor(this::suggestComponents);
                if (this.reader.canRead()) continue;
                throw COMPONENT_EXPECTED_EXCEPTION.createWithContext(this.reader);
            }
            this.reader.expect(']');
            this.callbacks.setSuggestor(SUGGEST_DEFAULT);
        }

        public static ComponentType<?> readComponentType(StringReader reader) throws CommandSyntaxException {
            if (!reader.canRead()) {
                throw COMPONENT_EXPECTED_EXCEPTION.createWithContext(reader);
            }
            int i = reader.getCursor();
            Identifier lv = Identifier.fromCommandInput(reader);
            ComponentType<?> lv2 = Registries.DATA_COMPONENT_TYPE.get(lv);
            if (lv2 == null || lv2.shouldSkipSerialization()) {
                reader.setCursor(i);
                throw UNKNOWN_COMPONENT_EXCEPTION.createWithContext(reader, lv);
            }
            return lv2;
        }

        private <T> void readComponentValue(ComponentType<T> type) throws CommandSyntaxException {
            int i = this.reader.getCursor();
            NbtElement lv = new StringNbtReader(this.reader).parseElement();
            DataResult dataResult = type.getCodecOrThrow().parse(ItemStringReader.this.nbtOps, lv);
            this.callbacks.onComponentAdded(type, dataResult.getOrThrow(error -> {
                this.reader.setCursor(i);
                return MALFORMED_COMPONENT_EXCEPTION.createWithContext(this.reader, type.toString(), error);
            }));
        }

        private CompletableFuture<Suggestions> suggestBracket(SuggestionsBuilder builder) {
            if (builder.getRemaining().isEmpty()) {
                builder.suggest(String.valueOf('['));
            }
            return builder.buildFuture();
        }

        private CompletableFuture<Suggestions> suggestEndOfComponent(SuggestionsBuilder builder) {
            if (builder.getRemaining().isEmpty()) {
                builder.suggest(String.valueOf(','));
                builder.suggest(String.valueOf(']'));
            }
            return builder.buildFuture();
        }

        private CompletableFuture<Suggestions> suggestEqual(SuggestionsBuilder builder) {
            if (builder.getRemaining().isEmpty()) {
                builder.suggest(String.valueOf('='));
            }
            return builder.buildFuture();
        }

        private CompletableFuture<Suggestions> suggestItems(SuggestionsBuilder builder) {
            return CommandSource.suggestIdentifiers(ItemStringReader.this.itemRegistry.streamKeys().map(RegistryKey::getValue), builder);
        }

        private CompletableFuture<Suggestions> suggestComponents(SuggestionsBuilder builder) {
            builder.suggest(String.valueOf('!'));
            return this.suggestComponents(builder, String.valueOf('='));
        }

        private CompletableFuture<Suggestions> suggestComponentsToRemove(SuggestionsBuilder builder) {
            return this.suggestComponents(builder, "");
        }

        private CompletableFuture<Suggestions> suggestComponents(SuggestionsBuilder builder, String suffix) {
            String string2 = builder.getRemaining().toLowerCase(Locale.ROOT);
            CommandSource.forEachMatching(Registries.DATA_COMPONENT_TYPE.getEntrySet(), string2, entry -> ((RegistryKey)entry.getKey()).getValue(), entry -> {
                ComponentType lv = (ComponentType)entry.getValue();
                if (lv.getCodec() != null) {
                    Identifier lv2 = ((RegistryKey)entry.getKey()).getValue();
                    builder.suggest(String.valueOf(lv2) + suffix);
                }
            });
            return builder.buildFuture();
        }
    }

    static class SuggestionCallbacks
    implements Callbacks {
        private Function<SuggestionsBuilder, CompletableFuture<Suggestions>> suggestor = SUGGEST_DEFAULT;

        SuggestionCallbacks() {
        }

        @Override
        public void setSuggestor(Function<SuggestionsBuilder, CompletableFuture<Suggestions>> suggestor) {
            this.suggestor = suggestor;
        }

        public CompletableFuture<Suggestions> getSuggestions(SuggestionsBuilder builder, StringReader reader) {
            return this.suggestor.apply(builder.createOffset(reader.getCursor()));
        }
    }
}

