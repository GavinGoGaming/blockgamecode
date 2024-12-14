/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.command.argument;

import com.google.gson.JsonObject;
import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.function.Supplier;
import net.minecraft.command.CommandRegistryAccess;
import net.minecraft.command.CommandSource;
import net.minecraft.command.EntitySelector;
import net.minecraft.command.EntitySelectorReader;
import net.minecraft.command.argument.EntityArgumentType;
import net.minecraft.command.argument.serialize.ArgumentSerializer;
import net.minecraft.entity.Entity;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.scoreboard.ScoreHolder;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;

public class ScoreHolderArgumentType
implements ArgumentType<ScoreHolders> {
    public static final SuggestionProvider<ServerCommandSource> SUGGESTION_PROVIDER = (context, builder) -> {
        StringReader stringReader = new StringReader(builder.getInput());
        stringReader.setCursor(builder.getStart());
        EntitySelectorReader lv = new EntitySelectorReader(stringReader);
        try {
            lv.read();
        } catch (CommandSyntaxException commandSyntaxException) {
            // empty catch block
        }
        return lv.listSuggestions(builder, (SuggestionsBuilder builderx) -> CommandSource.suggestMatching(((ServerCommandSource)context.getSource()).getPlayerNames(), builderx));
    };
    private static final Collection<String> EXAMPLES = Arrays.asList("Player", "0123", "*", "@e");
    private static final SimpleCommandExceptionType EMPTY_SCORE_HOLDER_EXCEPTION = new SimpleCommandExceptionType(Text.translatable("argument.scoreHolder.empty"));
    final boolean multiple;

    public ScoreHolderArgumentType(boolean multiple) {
        this.multiple = multiple;
    }

    public static ScoreHolder getScoreHolder(CommandContext<ServerCommandSource> context, String name) throws CommandSyntaxException {
        return ScoreHolderArgumentType.getScoreHolders(context, name).iterator().next();
    }

    public static Collection<ScoreHolder> getScoreHolders(CommandContext<ServerCommandSource> context, String name) throws CommandSyntaxException {
        return ScoreHolderArgumentType.getScoreHolders(context, name, Collections::emptyList);
    }

    public static Collection<ScoreHolder> getScoreboardScoreHolders(CommandContext<ServerCommandSource> context, String name) throws CommandSyntaxException {
        return ScoreHolderArgumentType.getScoreHolders(context, name, context.getSource().getServer().getScoreboard()::getKnownScoreHolders);
    }

    public static Collection<ScoreHolder> getScoreHolders(CommandContext<ServerCommandSource> context, String name, Supplier<Collection<ScoreHolder>> players) throws CommandSyntaxException {
        Collection<ScoreHolder> collection = context.getArgument(name, ScoreHolders.class).getNames(context.getSource(), players);
        if (collection.isEmpty()) {
            throw EntityArgumentType.ENTITY_NOT_FOUND_EXCEPTION.create();
        }
        return collection;
    }

    public static ScoreHolderArgumentType scoreHolder() {
        return new ScoreHolderArgumentType(false);
    }

    public static ScoreHolderArgumentType scoreHolders() {
        return new ScoreHolderArgumentType(true);
    }

    @Override
    public ScoreHolders parse(StringReader stringReader) throws CommandSyntaxException {
        if (stringReader.canRead() && stringReader.peek() == '@') {
            EntitySelectorReader lv = new EntitySelectorReader(stringReader);
            EntitySelector lv2 = lv.read();
            if (!this.multiple && lv2.getLimit() > 1) {
                throw EntityArgumentType.TOO_MANY_ENTITIES_EXCEPTION.createWithContext(stringReader);
            }
            return new SelectorScoreHolders(lv2);
        }
        int i = stringReader.getCursor();
        while (stringReader.canRead() && stringReader.peek() != ' ') {
            stringReader.skip();
        }
        String string = stringReader.getString().substring(i, stringReader.getCursor());
        if (string.equals("*")) {
            return (source, players) -> {
                Collection collection = (Collection)players.get();
                if (collection.isEmpty()) {
                    throw EMPTY_SCORE_HOLDER_EXCEPTION.create();
                }
                return collection;
            };
        }
        List<ScoreHolder> list = List.of(ScoreHolder.fromName(string));
        if (string.startsWith("#")) {
            return (source, players) -> list;
        }
        try {
            UUID uUID = UUID.fromString(string);
            return (source, holders) -> {
                MinecraftServer minecraftServer = source.getServer();
                Entity lv = null;
                ArrayList<Entity> list2 = null;
                for (ServerWorld lv2 : minecraftServer.getWorlds()) {
                    Entity lv3 = lv2.getEntity(uUID);
                    if (lv3 == null) continue;
                    if (lv == null) {
                        lv = lv3;
                        continue;
                    }
                    if (list2 == null) {
                        list2 = new ArrayList<Entity>();
                        list2.add(lv);
                    }
                    list2.add(lv3);
                }
                if (list2 != null) {
                    return list2;
                }
                if (lv != null) {
                    return List.of(lv);
                }
                return list;
            };
        } catch (IllegalArgumentException illegalArgumentException) {
            return (source, holders) -> {
                MinecraftServer minecraftServer = source.getServer();
                ServerPlayerEntity lv = minecraftServer.getPlayerManager().getPlayer(string);
                if (lv != null) {
                    return List.of(lv);
                }
                return list;
            };
        }
    }

    @Override
    public Collection<String> getExamples() {
        return EXAMPLES;
    }

    @Override
    public /* synthetic */ Object parse(StringReader reader) throws CommandSyntaxException {
        return this.parse(reader);
    }

    @FunctionalInterface
    public static interface ScoreHolders {
        public Collection<ScoreHolder> getNames(ServerCommandSource var1, Supplier<Collection<ScoreHolder>> var2) throws CommandSyntaxException;
    }

    public static class SelectorScoreHolders
    implements ScoreHolders {
        private final EntitySelector selector;

        public SelectorScoreHolders(EntitySelector selector) {
            this.selector = selector;
        }

        @Override
        public Collection<ScoreHolder> getNames(ServerCommandSource arg, Supplier<Collection<ScoreHolder>> supplier) throws CommandSyntaxException {
            List<? extends Entity> list = this.selector.getEntities(arg);
            if (list.isEmpty()) {
                throw EntityArgumentType.ENTITY_NOT_FOUND_EXCEPTION.create();
            }
            return List.copyOf(list);
        }
    }

    public static class Serializer
    implements ArgumentSerializer<ScoreHolderArgumentType, Properties> {
        private static final byte MULTIPLE_FLAG = 1;

        @Override
        public void writePacket(Properties arg, PacketByteBuf arg2) {
            int i = 0;
            if (arg.multiple) {
                i |= 1;
            }
            arg2.writeByte(i);
        }

        @Override
        public Properties fromPacket(PacketByteBuf arg) {
            byte b = arg.readByte();
            boolean bl = (b & 1) != 0;
            return new Properties(bl);
        }

        @Override
        public void writeJson(Properties arg, JsonObject jsonObject) {
            jsonObject.addProperty("amount", arg.multiple ? "multiple" : "single");
        }

        @Override
        public Properties getArgumentTypeProperties(ScoreHolderArgumentType arg) {
            return new Properties(arg.multiple);
        }

        @Override
        public /* synthetic */ ArgumentSerializer.ArgumentTypeProperties fromPacket(PacketByteBuf buf) {
            return this.fromPacket(buf);
        }

        public final class Properties
        implements ArgumentSerializer.ArgumentTypeProperties<ScoreHolderArgumentType> {
            final boolean multiple;

            Properties(boolean multiple) {
                this.multiple = multiple;
            }

            @Override
            public ScoreHolderArgumentType createType(CommandRegistryAccess arg) {
                return new ScoreHolderArgumentType(this.multiple);
            }

            @Override
            public ArgumentSerializer<ScoreHolderArgumentType, ?> getSerializer() {
                return Serializer.this;
            }

            @Override
            public /* synthetic */ ArgumentType createType(CommandRegistryAccess commandRegistryAccess) {
                return this.createType(commandRegistryAccess);
            }
        }
    }
}

