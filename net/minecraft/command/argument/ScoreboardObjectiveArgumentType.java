/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.command.argument;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import java.util.Arrays;
import java.util.Collection;
import java.util.concurrent.CompletableFuture;
import net.minecraft.command.CommandSource;
import net.minecraft.scoreboard.ScoreboardObjective;
import net.minecraft.scoreboard.ServerScoreboard;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.Text;

public class ScoreboardObjectiveArgumentType
implements ArgumentType<String> {
    private static final Collection<String> EXAMPLES = Arrays.asList("foo", "*", "012");
    private static final DynamicCommandExceptionType UNKNOWN_OBJECTIVE_EXCEPTION = new DynamicCommandExceptionType(name -> Text.stringifiedTranslatable("arguments.objective.notFound", name));
    private static final DynamicCommandExceptionType READONLY_OBJECTIVE_EXCEPTION = new DynamicCommandExceptionType(name -> Text.stringifiedTranslatable("arguments.objective.readonly", name));

    public static ScoreboardObjectiveArgumentType scoreboardObjective() {
        return new ScoreboardObjectiveArgumentType();
    }

    public static ScoreboardObjective getObjective(CommandContext<ServerCommandSource> context, String name) throws CommandSyntaxException {
        String string2 = context.getArgument(name, String.class);
        ServerScoreboard lv = context.getSource().getServer().getScoreboard();
        ScoreboardObjective lv2 = lv.getNullableObjective(string2);
        if (lv2 == null) {
            throw UNKNOWN_OBJECTIVE_EXCEPTION.create(string2);
        }
        return lv2;
    }

    public static ScoreboardObjective getWritableObjective(CommandContext<ServerCommandSource> context, String name) throws CommandSyntaxException {
        ScoreboardObjective lv = ScoreboardObjectiveArgumentType.getObjective(context, name);
        if (lv.getCriterion().isReadOnly()) {
            throw READONLY_OBJECTIVE_EXCEPTION.create(lv.getName());
        }
        return lv;
    }

    @Override
    public String parse(StringReader stringReader) throws CommandSyntaxException {
        return stringReader.readUnquotedString();
    }

    @Override
    public <S> CompletableFuture<Suggestions> listSuggestions(CommandContext<S> context, SuggestionsBuilder builder) {
        S object = context.getSource();
        if (object instanceof ServerCommandSource) {
            ServerCommandSource lv = (ServerCommandSource)object;
            return CommandSource.suggestMatching(lv.getServer().getScoreboard().getObjectiveNames(), builder);
        }
        if (object instanceof CommandSource) {
            CommandSource lv2 = (CommandSource)object;
            return lv2.getCompletions(context);
        }
        return Suggestions.empty();
    }

    @Override
    public Collection<String> getExamples() {
        return EXAMPLES;
    }

    @Override
    public /* synthetic */ Object parse(StringReader reader) throws CommandSyntaxException {
        return this.parse(reader);
    }
}

