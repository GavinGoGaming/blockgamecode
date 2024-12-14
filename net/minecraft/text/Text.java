/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 * 
 * Could not load the following classes:
 *  org.jetbrains.annotations.Nullable
 */
package net.minecraft.text;

import com.google.common.collect.Lists;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.google.gson.JsonParser;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import com.google.gson.stream.JsonReader;
import com.mojang.brigadier.Message;
import com.mojang.serialization.JsonOps;
import java.io.StringReader;
import java.lang.reflect.Type;
import java.net.URI;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.screen.ScreenTexts;
import net.minecraft.text.KeybindTextContent;
import net.minecraft.text.MutableText;
import net.minecraft.text.NbtDataSource;
import net.minecraft.text.NbtTextContent;
import net.minecraft.text.OrderedText;
import net.minecraft.text.PlainTextContent;
import net.minecraft.text.ScoreTextContent;
import net.minecraft.text.SelectorTextContent;
import net.minecraft.text.StringVisitable;
import net.minecraft.text.Style;
import net.minecraft.text.TextCodecs;
import net.minecraft.text.TextContent;
import net.minecraft.text.TranslatableTextContent;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.ChunkPos;
import org.jetbrains.annotations.Nullable;

public interface Text
extends Message,
StringVisitable {
    public Style getStyle();

    public TextContent getContent();

    @Override
    default public String getString() {
        return StringVisitable.super.getString();
    }

    default public String asTruncatedString(int length) {
        StringBuilder stringBuilder = new StringBuilder();
        this.visit(string -> {
            int j = length - stringBuilder.length();
            if (j <= 0) {
                return TERMINATE_VISIT;
            }
            stringBuilder.append(string.length() <= j ? string : string.substring(0, j));
            return Optional.empty();
        });
        return stringBuilder.toString();
    }

    public List<Text> getSiblings();

    @Nullable
    default public String getLiteralString() {
        TextContent textContent = this.getContent();
        if (textContent instanceof PlainTextContent) {
            PlainTextContent lv = (PlainTextContent)textContent;
            if (this.getSiblings().isEmpty() && this.getStyle().isEmpty()) {
                return lv.string();
            }
        }
        return null;
    }

    default public MutableText copyContentOnly() {
        return MutableText.of(this.getContent());
    }

    default public MutableText copy() {
        return new MutableText(this.getContent(), new ArrayList<Text>(this.getSiblings()), this.getStyle());
    }

    public OrderedText asOrderedText();

    @Override
    default public <T> Optional<T> visit(StringVisitable.StyledVisitor<T> styledVisitor, Style style) {
        Style lv = this.getStyle().withParent(style);
        Optional<T> optional = this.getContent().visit(styledVisitor, lv);
        if (optional.isPresent()) {
            return optional;
        }
        for (Text lv2 : this.getSiblings()) {
            Optional<T> optional2 = lv2.visit(styledVisitor, lv);
            if (!optional2.isPresent()) continue;
            return optional2;
        }
        return Optional.empty();
    }

    @Override
    default public <T> Optional<T> visit(StringVisitable.Visitor<T> visitor) {
        Optional<T> optional = this.getContent().visit(visitor);
        if (optional.isPresent()) {
            return optional;
        }
        for (Text lv : this.getSiblings()) {
            Optional<T> optional2 = lv.visit(visitor);
            if (!optional2.isPresent()) continue;
            return optional2;
        }
        return Optional.empty();
    }

    default public List<Text> withoutStyle() {
        return this.getWithStyle(Style.EMPTY);
    }

    default public List<Text> getWithStyle(Style style) {
        ArrayList<Text> list = Lists.newArrayList();
        this.visit((styleOverride, text) -> {
            if (!text.isEmpty()) {
                list.add(Text.literal(text).fillStyle(styleOverride));
            }
            return Optional.empty();
        }, style);
        return list;
    }

    default public boolean contains(Text text) {
        List<Text> list2;
        if (this.equals(text)) {
            return true;
        }
        List<Text> list = this.withoutStyle();
        return Collections.indexOfSubList(list, list2 = text.getWithStyle(this.getStyle())) != -1;
    }

    public static Text of(@Nullable String string) {
        return string != null ? Text.literal(string) : ScreenTexts.EMPTY;
    }

    public static MutableText literal(String string) {
        return MutableText.of(PlainTextContent.of(string));
    }

    public static MutableText translatable(String key) {
        return MutableText.of(new TranslatableTextContent(key, null, TranslatableTextContent.EMPTY_ARGUMENTS));
    }

    public static MutableText translatable(String key, Object ... args) {
        return MutableText.of(new TranslatableTextContent(key, null, args));
    }

    public static MutableText stringifiedTranslatable(String key, Object ... args) {
        for (int i = 0; i < args.length; ++i) {
            Object object = args[i];
            if (TranslatableTextContent.isPrimitive(object) || object instanceof Text) continue;
            args[i] = String.valueOf(object);
        }
        return Text.translatable(key, args);
    }

    public static MutableText translatableWithFallback(String key, @Nullable String fallback) {
        return MutableText.of(new TranslatableTextContent(key, fallback, TranslatableTextContent.EMPTY_ARGUMENTS));
    }

    public static MutableText translatableWithFallback(String key, @Nullable String fallback, Object ... args) {
        return MutableText.of(new TranslatableTextContent(key, fallback, args));
    }

    public static MutableText empty() {
        return MutableText.of(PlainTextContent.EMPTY);
    }

    public static MutableText keybind(String string) {
        return MutableText.of(new KeybindTextContent(string));
    }

    public static MutableText nbt(String rawPath, boolean interpret, Optional<Text> separator, NbtDataSource dataSource) {
        return MutableText.of(new NbtTextContent(rawPath, interpret, separator, dataSource));
    }

    public static MutableText score(String name, String objective) {
        return MutableText.of(new ScoreTextContent(name, objective));
    }

    public static MutableText selector(String pattern, Optional<Text> separator) {
        return MutableText.of(new SelectorTextContent(pattern, separator));
    }

    public static Text of(Date date) {
        return Text.literal(date.toString());
    }

    public static Text of(Message message) {
        Text text;
        if (message instanceof Text) {
            Text lv = (Text)message;
            text = lv;
        } else {
            text = Text.literal(message.getString());
        }
        return text;
    }

    public static Text of(UUID uuid) {
        return Text.literal(uuid.toString());
    }

    public static Text of(Identifier id) {
        return Text.literal(id.toString());
    }

    public static Text of(ChunkPos pos) {
        return Text.literal(pos.toString());
    }

    public static Text of(URI uri) {
        return Text.literal(uri.toString());
    }

    public static class Serializer
    implements JsonDeserializer<MutableText>,
    JsonSerializer<Text> {
        private final RegistryWrapper.WrapperLookup registries;

        public Serializer(RegistryWrapper.WrapperLookup registries) {
            this.registries = registries;
        }

        @Override
        public MutableText deserialize(JsonElement jsonElement, Type type, JsonDeserializationContext jsonDeserializationContext) throws JsonParseException {
            return Serialization.fromJson(jsonElement, this.registries);
        }

        @Override
        public JsonElement serialize(Text arg, Type type, JsonSerializationContext jsonSerializationContext) {
            return Serialization.toJson(arg, this.registries);
        }

        @Override
        public /* synthetic */ JsonElement serialize(Object text, Type type, JsonSerializationContext context) {
            return this.serialize((Text)text, type, context);
        }

        @Override
        public /* synthetic */ Object deserialize(JsonElement json, Type type, JsonDeserializationContext context) throws JsonParseException {
            return this.deserialize(json, type, context);
        }
    }

    public static class Serialization {
        private static final Gson GSON = new GsonBuilder().disableHtmlEscaping().create();

        private Serialization() {
        }

        static MutableText fromJson(JsonElement json, RegistryWrapper.WrapperLookup registries) {
            return (MutableText)TextCodecs.CODEC.parse(registries.getOps(JsonOps.INSTANCE), json).getOrThrow(JsonParseException::new);
        }

        static JsonElement toJson(Text text, RegistryWrapper.WrapperLookup registries) {
            return TextCodecs.CODEC.encodeStart(registries.getOps(JsonOps.INSTANCE), text).getOrThrow(JsonParseException::new);
        }

        public static String toJsonString(Text text, RegistryWrapper.WrapperLookup registries) {
            return GSON.toJson(Serialization.toJson(text, registries));
        }

        @Nullable
        public static MutableText fromJson(String json, RegistryWrapper.WrapperLookup registries) {
            JsonElement jsonElement = JsonParser.parseString(json);
            if (jsonElement == null) {
                return null;
            }
            return Serialization.fromJson(jsonElement, registries);
        }

        @Nullable
        public static MutableText fromJsonTree(@Nullable JsonElement json, RegistryWrapper.WrapperLookup registries) {
            if (json == null) {
                return null;
            }
            return Serialization.fromJson(json, registries);
        }

        @Nullable
        public static MutableText fromLenientJson(String json, RegistryWrapper.WrapperLookup registries) {
            JsonReader jsonReader = new JsonReader(new StringReader(json));
            jsonReader.setLenient(true);
            JsonElement jsonElement = JsonParser.parseReader(jsonReader);
            if (jsonElement == null) {
                return null;
            }
            return Serialization.fromJson(jsonElement, registries);
        }
    }
}

