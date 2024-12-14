/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.command.argument.packrat;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;
import net.minecraft.command.CommandSource;
import net.minecraft.command.argument.packrat.IdentifierSuggestable;
import net.minecraft.command.argument.packrat.ParseError;
import net.minecraft.command.argument.packrat.ParseErrorList;
import net.minecraft.command.argument.packrat.ParsingRules;
import net.minecraft.command.argument.packrat.ParsingState;
import net.minecraft.command.argument.packrat.ParsingStateImpl;
import net.minecraft.command.argument.packrat.Suggestable;
import net.minecraft.command.argument.packrat.Symbol;

public record ArgumentParser<T>(ParsingRules<StringReader> rules, Symbol<T> top) {
    public Optional<T> startParsing(ParsingState<StringReader> state) {
        return state.startParsing(this.top);
    }

    public T parse(StringReader reader) throws CommandSyntaxException {
        Object r;
        ParseErrorList.Impl<StringReader> lv = new ParseErrorList.Impl<StringReader>();
        ParsingStateImpl lv2 = new ParsingStateImpl(this.rules(), lv, reader);
        Optional<T> optional = this.startParsing(lv2);
        if (optional.isPresent()) {
            return optional.get();
        }
        List list = lv.getErrors().stream().mapMulti((error, consumer) -> {
            Object object = error.reason();
            if (object instanceof Exception) {
                Exception exception = (Exception)object;
                consumer.accept(exception);
            }
        }).toList();
        for (Exception exception : list) {
            if (!(exception instanceof CommandSyntaxException)) continue;
            CommandSyntaxException commandSyntaxException = (CommandSyntaxException)exception;
            throw commandSyntaxException;
        }
        if (list.size() == 1 && (r = list.get(0)) instanceof RuntimeException) {
            RuntimeException runtimeException = (RuntimeException)r;
            throw runtimeException;
        }
        throw new IllegalStateException("Failed to parse: " + lv.getErrors().stream().map(ParseError::toString).collect(Collectors.joining(", ")));
    }

    public CompletableFuture<Suggestions> listSuggestions(SuggestionsBuilder builder) {
        StringReader stringReader = new StringReader(builder.getInput());
        stringReader.setCursor(builder.getStart());
        ParseErrorList.Impl<StringReader> lv = new ParseErrorList.Impl<StringReader>();
        ParsingStateImpl lv2 = new ParsingStateImpl(this.rules(), lv, stringReader);
        this.startParsing(lv2);
        List<ParseError<StringReader>> list = lv.getErrors();
        if (list.isEmpty()) {
            return builder.buildFuture();
        }
        SuggestionsBuilder suggestionsBuilder2 = builder.createOffset(lv.getCursor());
        for (ParseError<StringReader> lv3 : list) {
            Suggestable<StringReader> suggestable = lv3.suggestions();
            if (suggestable instanceof IdentifierSuggestable) {
                IdentifierSuggestable lv4 = (IdentifierSuggestable)suggestable;
                CommandSource.suggestIdentifiers(lv4.possibleIds(), suggestionsBuilder2);
                continue;
            }
            CommandSource.suggestMatching(lv3.suggestions().possibleValues(lv2), suggestionsBuilder2);
        }
        return suggestionsBuilder2.buildFuture();
    }
}

