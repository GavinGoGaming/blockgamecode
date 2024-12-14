/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 * 
 * Could not load the following classes:
 *  org.jetbrains.annotations.Nullable
 */
package net.minecraft.util;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import io.netty.buffer.ByteBuf;
import java.lang.reflect.Type;
import java.util.function.UnaryOperator;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.text.Text;
import net.minecraft.util.InvalidIdentifierException;
import net.minecraft.util.JsonHelper;
import org.jetbrains.annotations.Nullable;

public final class Identifier
implements Comparable<Identifier> {
    public static final Codec<Identifier> CODEC = Codec.STRING.comapFlatMap(Identifier::validate, Identifier::toString).stable();
    public static final PacketCodec<ByteBuf, Identifier> PACKET_CODEC = PacketCodecs.STRING.xmap(Identifier::of, Identifier::toString);
    public static final SimpleCommandExceptionType COMMAND_EXCEPTION = new SimpleCommandExceptionType(Text.translatable("argument.id.invalid"));
    public static final char NAMESPACE_SEPARATOR = ':';
    public static final String DEFAULT_NAMESPACE = "minecraft";
    public static final String REALMS_NAMESPACE = "realms";
    private final String namespace;
    private final String path;

    private Identifier(String namespace, String path) {
        assert (Identifier.isNamespaceValid(namespace));
        assert (Identifier.isPathValid(path));
        this.namespace = namespace;
        this.path = path;
    }

    private static Identifier ofValidated(String namespace, String path) {
        return new Identifier(Identifier.validateNamespace(namespace, path), Identifier.validatePath(namespace, path));
    }

    public static Identifier of(String namespace, String path) {
        return Identifier.ofValidated(namespace, path);
    }

    public static Identifier of(String id) {
        return Identifier.splitOn(id, ':');
    }

    public static Identifier ofVanilla(String path) {
        return new Identifier(DEFAULT_NAMESPACE, Identifier.validatePath(DEFAULT_NAMESPACE, path));
    }

    @Nullable
    public static Identifier tryParse(String id) {
        return Identifier.trySplitOn(id, ':');
    }

    @Nullable
    public static Identifier tryParse(String namespace, String path) {
        if (Identifier.isNamespaceValid(namespace) && Identifier.isPathValid(path)) {
            return new Identifier(namespace, path);
        }
        return null;
    }

    public static Identifier splitOn(String id, char delimiter) {
        int i = id.indexOf(delimiter);
        if (i >= 0) {
            String string2 = id.substring(i + 1);
            if (i != 0) {
                String string3 = id.substring(0, i);
                return Identifier.ofValidated(string3, string2);
            }
            return Identifier.ofVanilla(string2);
        }
        return Identifier.ofVanilla(id);
    }

    @Nullable
    public static Identifier trySplitOn(String id, char delimiter) {
        int i = id.indexOf(delimiter);
        if (i >= 0) {
            String string2 = id.substring(i + 1);
            if (!Identifier.isPathValid(string2)) {
                return null;
            }
            if (i != 0) {
                String string3 = id.substring(0, i);
                return Identifier.isNamespaceValid(string3) ? new Identifier(string3, string2) : null;
            }
            return new Identifier(DEFAULT_NAMESPACE, string2);
        }
        return Identifier.isPathValid(id) ? new Identifier(DEFAULT_NAMESPACE, id) : null;
    }

    public static DataResult<Identifier> validate(String id) {
        try {
            return DataResult.success(Identifier.of(id));
        } catch (InvalidIdentifierException lv) {
            return DataResult.error(() -> "Not a valid resource location: " + id + " " + lv.getMessage());
        }
    }

    public String getPath() {
        return this.path;
    }

    public String getNamespace() {
        return this.namespace;
    }

    public Identifier withPath(String path) {
        return new Identifier(this.namespace, Identifier.validatePath(this.namespace, path));
    }

    public Identifier withPath(UnaryOperator<String> pathFunction) {
        return this.withPath((String)pathFunction.apply(this.path));
    }

    public Identifier withPrefixedPath(String prefix) {
        return this.withPath(prefix + this.path);
    }

    public Identifier withSuffixedPath(String suffix) {
        return this.withPath(this.path + suffix);
    }

    public String toString() {
        return this.namespace + ":" + this.path;
    }

    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o instanceof Identifier) {
            Identifier lv = (Identifier)o;
            return this.namespace.equals(lv.namespace) && this.path.equals(lv.path);
        }
        return false;
    }

    public int hashCode() {
        return 31 * this.namespace.hashCode() + this.path.hashCode();
    }

    @Override
    public int compareTo(Identifier arg) {
        int i = this.path.compareTo(arg.path);
        if (i == 0) {
            i = this.namespace.compareTo(arg.namespace);
        }
        return i;
    }

    public String toUnderscoreSeparatedString() {
        return this.toString().replace('/', '_').replace(':', '_');
    }

    public String toTranslationKey() {
        return this.namespace + "." + this.path;
    }

    public String toShortTranslationKey() {
        return this.namespace.equals(DEFAULT_NAMESPACE) ? this.path : this.toTranslationKey();
    }

    public String toTranslationKey(String prefix) {
        return prefix + "." + this.toTranslationKey();
    }

    public String toTranslationKey(String prefix, String suffix) {
        return prefix + "." + this.toTranslationKey() + "." + suffix;
    }

    private static String readString(StringReader reader) {
        int i = reader.getCursor();
        while (reader.canRead() && Identifier.isCharValid(reader.peek())) {
            reader.skip();
        }
        return reader.getString().substring(i, reader.getCursor());
    }

    public static Identifier fromCommandInput(StringReader reader) throws CommandSyntaxException {
        int i = reader.getCursor();
        String string = Identifier.readString(reader);
        try {
            return Identifier.of(string);
        } catch (InvalidIdentifierException lv) {
            reader.setCursor(i);
            throw COMMAND_EXCEPTION.createWithContext(reader);
        }
    }

    public static Identifier fromCommandInputNonEmpty(StringReader reader) throws CommandSyntaxException {
        int i = reader.getCursor();
        String string = Identifier.readString(reader);
        if (string.isEmpty()) {
            throw COMMAND_EXCEPTION.createWithContext(reader);
        }
        try {
            return Identifier.of(string);
        } catch (InvalidIdentifierException lv) {
            reader.setCursor(i);
            throw COMMAND_EXCEPTION.createWithContext(reader);
        }
    }

    public static boolean isCharValid(char c) {
        return c >= '0' && c <= '9' || c >= 'a' && c <= 'z' || c == '_' || c == ':' || c == '/' || c == '.' || c == '-';
    }

    public static boolean isPathValid(String path) {
        for (int i = 0; i < path.length(); ++i) {
            if (Identifier.isPathCharacterValid(path.charAt(i))) continue;
            return false;
        }
        return true;
    }

    public static boolean isNamespaceValid(String namespace) {
        for (int i = 0; i < namespace.length(); ++i) {
            if (Identifier.isNamespaceCharacterValid(namespace.charAt(i))) continue;
            return false;
        }
        return true;
    }

    private static String validateNamespace(String namespace, String path) {
        if (!Identifier.isNamespaceValid(namespace)) {
            throw new InvalidIdentifierException("Non [a-z0-9_.-] character in namespace of location: " + namespace + ":" + path);
        }
        return namespace;
    }

    public static boolean isPathCharacterValid(char character) {
        return character == '_' || character == '-' || character >= 'a' && character <= 'z' || character >= '0' && character <= '9' || character == '/' || character == '.';
    }

    private static boolean isNamespaceCharacterValid(char character) {
        return character == '_' || character == '-' || character >= 'a' && character <= 'z' || character >= '0' && character <= '9' || character == '.';
    }

    private static String validatePath(String namespace, String path) {
        if (!Identifier.isPathValid(path)) {
            throw new InvalidIdentifierException("Non [a-z0-9/._-] character in path of location: " + namespace + ":" + path);
        }
        return path;
    }

    @Override
    public /* synthetic */ int compareTo(Object other) {
        return this.compareTo((Identifier)other);
    }

    public static class Serializer
    implements JsonDeserializer<Identifier>,
    JsonSerializer<Identifier> {
        @Override
        public Identifier deserialize(JsonElement jsonElement, Type type, JsonDeserializationContext jsonDeserializationContext) throws JsonParseException {
            return Identifier.of(JsonHelper.asString(jsonElement, "location"));
        }

        @Override
        public JsonElement serialize(Identifier arg, Type type, JsonSerializationContext jsonSerializationContext) {
            return new JsonPrimitive(arg.toString());
        }

        @Override
        public /* synthetic */ JsonElement serialize(Object id, Type type, JsonSerializationContext context) {
            return this.serialize((Identifier)id, type, context);
        }

        @Override
        public /* synthetic */ Object deserialize(JsonElement json, Type type, JsonDeserializationContext context) throws JsonParseException {
            return this.deserialize(json, type, context);
        }
    }
}

