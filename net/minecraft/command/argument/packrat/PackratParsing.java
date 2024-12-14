/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.command.argument.packrat;

import com.google.common.collect.ImmutableList;
import com.mojang.brigadier.ImmutableStringReader;
import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;
import net.minecraft.command.argument.packrat.AnyIdParsingRule;
import net.minecraft.command.argument.packrat.ArgumentParser;
import net.minecraft.command.argument.packrat.IdentifiableParsingRule;
import net.minecraft.command.argument.packrat.Literals;
import net.minecraft.command.argument.packrat.NbtParsingRule;
import net.minecraft.command.argument.packrat.ParsingRules;
import net.minecraft.command.argument.packrat.Symbol;
import net.minecraft.command.argument.packrat.Term;
import net.minecraft.nbt.NbtElement;
import net.minecraft.util.Identifier;
import net.minecraft.util.Unit;
import net.minecraft.util.Util;

public class PackratParsing {
    public static <T, C, P> ArgumentParser<List<T>> createParser(Callbacks<T, C, P> callbacks) {
        Symbol lv = Symbol.of("top");
        Symbol lv2 = Symbol.of("type");
        Symbol lv3 = Symbol.of("any_type");
        Symbol lv4 = Symbol.of("element_type");
        Symbol lv5 = Symbol.of("tag_type");
        Symbol lv6 = Symbol.of("conditions");
        Symbol lv7 = Symbol.of("alternatives");
        Symbol lv8 = Symbol.of("term");
        Symbol lv9 = Symbol.of("negation");
        Symbol lv10 = Symbol.of("test");
        Symbol lv11 = Symbol.of("component_type");
        Symbol lv12 = Symbol.of("predicate_type");
        Symbol<Identifier> lv13 = Symbol.of("id");
        Symbol lv14 = Symbol.of("tag");
        ParsingRules<StringReader> lv15 = new ParsingRules<StringReader>();
        lv15.set(lv, Term.anyOf(Term.sequence(Term.symbol(lv2), Literals.character('['), Term.cutting(), Term.optional(Term.symbol(lv6)), Literals.character(']')), Term.symbol(lv2)), results -> {
            ImmutableList.Builder builder = ImmutableList.builder();
            ((Optional)results.getOrThrow(lv2)).ifPresent(builder::add);
            List list = (List)results.get(lv6);
            if (list != null) {
                builder.addAll((Iterable)list);
            }
            return builder.build();
        });
        lv15.set(lv2, Term.anyOf(Term.symbol(lv4), Term.sequence(Literals.character('#'), Term.cutting(), Term.symbol(lv5)), Term.symbol(lv3)), results -> Optional.ofNullable(results.getAny(lv4, lv5)));
        lv15.set(lv3, Literals.character('*'), results -> Unit.INSTANCE);
        lv15.set(lv4, new ItemParsingRule<T, C, P>(lv13, callbacks));
        lv15.set(lv5, new TagParsingRule<T, C, P>(lv13, callbacks));
        lv15.set(lv6, Term.sequence(Term.symbol(lv7), Term.optional(Term.sequence(Literals.character(','), Term.symbol(lv6)))), results -> {
            Object object = callbacks.anyOf((List)results.getOrThrow(lv7));
            return Optional.ofNullable((List)results.get(lv6)).map(predicates -> Util.withPrepended(object, predicates)).orElse(List.of(object));
        });
        lv15.set(lv7, Term.sequence(Term.symbol(lv8), Term.optional(Term.sequence(Literals.character('|'), Term.symbol(lv7)))), results -> {
            Object object = results.getOrThrow(lv8);
            return Optional.ofNullable((List)results.get(lv7)).map(predicates -> Util.withPrepended(object, predicates)).orElse(List.of(object));
        });
        lv15.set(lv8, Term.anyOf(Term.symbol(lv10), Term.sequence(Literals.character('!'), Term.symbol(lv9))), results -> results.getAnyOrThrow(lv10, lv9));
        lv15.set(lv9, Term.symbol(lv10), results -> callbacks.negate(results.getOrThrow(lv10)));
        lv15.set(lv10, Term.anyOf(Term.sequence(Term.symbol(lv11), Literals.character('='), Term.cutting(), Term.symbol(lv14)), Term.sequence(Term.symbol(lv12), Literals.character('~'), Term.cutting(), Term.symbol(lv14)), Term.symbol(lv11)), (state, results) -> {
            Object object = results.get(lv12);
            try {
                if (object != null) {
                    NbtElement lv = (NbtElement)results.getOrThrow(lv14);
                    return Optional.of(callbacks.subPredicatePredicate((ImmutableStringReader)state.getReader(), object, lv));
                }
                Object object2 = results.getOrThrow(lv11);
                NbtElement lv2 = (NbtElement)results.get(lv14);
                return Optional.of(lv2 != null ? callbacks.componentMatchPredicate((ImmutableStringReader)state.getReader(), object2, lv2) : callbacks.componentPresencePredicate((ImmutableStringReader)state.getReader(), object2));
            } catch (CommandSyntaxException commandSyntaxException) {
                state.getErrors().add(state.getCursor(), commandSyntaxException);
                return Optional.empty();
            }
        });
        lv15.set(lv11, new ComponentParsingRule<T, C, P>(lv13, callbacks));
        lv15.set(lv12, new SubPredicateParsingRule<T, C, P>(lv13, callbacks));
        lv15.set(lv14, NbtParsingRule.INSTANCE);
        lv15.set(lv13, AnyIdParsingRule.INSTANCE);
        return new ArgumentParser<List<T>>(lv15, lv);
    }

    static class ItemParsingRule<T, C, P>
    extends IdentifiableParsingRule<Callbacks<T, C, P>, T> {
        ItemParsingRule(Symbol<Identifier> symbol, Callbacks<T, C, P> callbacks) {
            super(symbol, callbacks);
        }

        @Override
        protected T parse(ImmutableStringReader reader, Identifier id) throws Exception {
            return ((Callbacks)this.callbacks).itemMatchPredicate(reader, id);
        }

        @Override
        public Stream<Identifier> possibleIds() {
            return ((Callbacks)this.callbacks).streamItemIds();
        }
    }

    public static interface Callbacks<T, C, P> {
        public T itemMatchPredicate(ImmutableStringReader var1, Identifier var2) throws CommandSyntaxException;

        public Stream<Identifier> streamItemIds();

        public T tagMatchPredicate(ImmutableStringReader var1, Identifier var2) throws CommandSyntaxException;

        public Stream<Identifier> streamTags();

        public C componentCheck(ImmutableStringReader var1, Identifier var2) throws CommandSyntaxException;

        public Stream<Identifier> streamComponentIds();

        public T componentMatchPredicate(ImmutableStringReader var1, C var2, NbtElement var3) throws CommandSyntaxException;

        public T componentPresencePredicate(ImmutableStringReader var1, C var2);

        public P subPredicateCheck(ImmutableStringReader var1, Identifier var2) throws CommandSyntaxException;

        public Stream<Identifier> streamSubPredicateIds();

        public T subPredicatePredicate(ImmutableStringReader var1, P var2, NbtElement var3) throws CommandSyntaxException;

        public T negate(T var1);

        public T anyOf(List<T> var1);
    }

    static class TagParsingRule<T, C, P>
    extends IdentifiableParsingRule<Callbacks<T, C, P>, T> {
        TagParsingRule(Symbol<Identifier> symbol, Callbacks<T, C, P> callbacks) {
            super(symbol, callbacks);
        }

        @Override
        protected T parse(ImmutableStringReader reader, Identifier id) throws Exception {
            return ((Callbacks)this.callbacks).tagMatchPredicate(reader, id);
        }

        @Override
        public Stream<Identifier> possibleIds() {
            return ((Callbacks)this.callbacks).streamTags();
        }
    }

    static class ComponentParsingRule<T, C, P>
    extends IdentifiableParsingRule<Callbacks<T, C, P>, C> {
        ComponentParsingRule(Symbol<Identifier> symbol, Callbacks<T, C, P> callbacks) {
            super(symbol, callbacks);
        }

        @Override
        protected C parse(ImmutableStringReader reader, Identifier id) throws Exception {
            return ((Callbacks)this.callbacks).componentCheck(reader, id);
        }

        @Override
        public Stream<Identifier> possibleIds() {
            return ((Callbacks)this.callbacks).streamComponentIds();
        }
    }

    static class SubPredicateParsingRule<T, C, P>
    extends IdentifiableParsingRule<Callbacks<T, C, P>, P> {
        SubPredicateParsingRule(Symbol<Identifier> symbol, Callbacks<T, C, P> callbacks) {
            super(symbol, callbacks);
        }

        @Override
        protected P parse(ImmutableStringReader reader, Identifier id) throws Exception {
            return ((Callbacks)this.callbacks).subPredicateCheck(reader, id);
        }

        @Override
        public Stream<Identifier> possibleIds() {
            return ((Callbacks)this.callbacks).streamSubPredicateIds();
        }
    }
}

