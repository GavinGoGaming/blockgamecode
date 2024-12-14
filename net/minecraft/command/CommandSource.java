/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.command;

import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import com.mojang.brigadier.Message;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Locale;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Stream;
import net.minecraft.registry.DynamicRegistryManager;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.resource.featuretoggle.FeatureSet;
import net.minecraft.util.Identifier;
import net.minecraft.world.World;

public interface CommandSource {
    public Collection<String> getPlayerNames();

    default public Collection<String> getChatSuggestions() {
        return this.getPlayerNames();
    }

    default public Collection<String> getEntitySuggestions() {
        return Collections.emptyList();
    }

    public Collection<String> getTeamNames();

    public Stream<Identifier> getSoundIds();

    public Stream<Identifier> getRecipeIds();

    public CompletableFuture<Suggestions> getCompletions(CommandContext<?> var1);

    default public Collection<RelativePosition> getBlockPositionSuggestions() {
        return Collections.singleton(RelativePosition.ZERO_WORLD);
    }

    default public Collection<RelativePosition> getPositionSuggestions() {
        return Collections.singleton(RelativePosition.ZERO_WORLD);
    }

    public Set<RegistryKey<World>> getWorldKeys();

    public DynamicRegistryManager getRegistryManager();

    public FeatureSet getEnabledFeatures();

    default public void suggestIdentifiers(Registry<?> registry, SuggestedIdType suggestedIdType, SuggestionsBuilder builder) {
        if (suggestedIdType.canSuggestTags()) {
            CommandSource.suggestIdentifiers(registry.streamTags().map(TagKey::id), builder, "#");
        }
        if (suggestedIdType.canSuggestElements()) {
            CommandSource.suggestIdentifiers(registry.getIds(), builder);
        }
    }

    public CompletableFuture<Suggestions> listIdSuggestions(RegistryKey<? extends Registry<?>> var1, SuggestedIdType var2, SuggestionsBuilder var3, CommandContext<?> var4);

    public boolean hasPermissionLevel(int var1);

    public static <T> void forEachMatching(Iterable<T> candidates, String remaining, Function<T, Identifier> identifier, Consumer<T> action) {
        boolean bl = remaining.indexOf(58) > -1;
        for (T object : candidates) {
            Identifier lv = identifier.apply(object);
            if (bl) {
                String string2 = lv.toString();
                if (!CommandSource.shouldSuggest(remaining, string2)) continue;
                action.accept(object);
                continue;
            }
            if (!CommandSource.shouldSuggest(remaining, lv.getNamespace()) && (!lv.getNamespace().equals("minecraft") || !CommandSource.shouldSuggest(remaining, lv.getPath()))) continue;
            action.accept(object);
        }
    }

    public static <T> void forEachMatching(Iterable<T> candidates, String remaining, String prefix, Function<T, Identifier> identifier, Consumer<T> action) {
        if (remaining.isEmpty()) {
            candidates.forEach(action);
        } else {
            String string3 = Strings.commonPrefix(remaining, prefix);
            if (!string3.isEmpty()) {
                String string4 = remaining.substring(string3.length());
                CommandSource.forEachMatching(candidates, string4, identifier, action);
            }
        }
    }

    public static CompletableFuture<Suggestions> suggestIdentifiers(Iterable<Identifier> candidates, SuggestionsBuilder builder, String prefix) {
        String string2 = builder.getRemaining().toLowerCase(Locale.ROOT);
        CommandSource.forEachMatching(candidates, string2, prefix, id -> id, id -> builder.suggest(prefix + String.valueOf(id)));
        return builder.buildFuture();
    }

    public static CompletableFuture<Suggestions> suggestIdentifiers(Stream<Identifier> candidates, SuggestionsBuilder builder, String prefix) {
        return CommandSource.suggestIdentifiers(candidates::iterator, builder, prefix);
    }

    public static CompletableFuture<Suggestions> suggestIdentifiers(Iterable<Identifier> candidates, SuggestionsBuilder builder) {
        String string = builder.getRemaining().toLowerCase(Locale.ROOT);
        CommandSource.forEachMatching(candidates, string, id -> id, id -> builder.suggest(id.toString()));
        return builder.buildFuture();
    }

    public static <T> CompletableFuture<Suggestions> suggestFromIdentifier(Iterable<T> candidates, SuggestionsBuilder builder, Function<T, Identifier> identifier, Function<T, Message> tooltip) {
        String string = builder.getRemaining().toLowerCase(Locale.ROOT);
        CommandSource.forEachMatching(candidates, string, identifier, object -> builder.suggest(((Identifier)identifier.apply(object)).toString(), (Message)tooltip.apply(object)));
        return builder.buildFuture();
    }

    public static CompletableFuture<Suggestions> suggestIdentifiers(Stream<Identifier> candidates, SuggestionsBuilder builder) {
        return CommandSource.suggestIdentifiers(candidates::iterator, builder);
    }

    public static <T> CompletableFuture<Suggestions> suggestFromIdentifier(Stream<T> candidates, SuggestionsBuilder builder, Function<T, Identifier> identifier, Function<T, Message> tooltip) {
        return CommandSource.suggestFromIdentifier(candidates::iterator, builder, identifier, tooltip);
    }

    public static CompletableFuture<Suggestions> suggestPositions(String remaining, Collection<RelativePosition> candidates, SuggestionsBuilder builder, Predicate<String> predicate) {
        ArrayList<String> list;
        block4: {
            String[] strings;
            block5: {
                block3: {
                    list = Lists.newArrayList();
                    if (!Strings.isNullOrEmpty(remaining)) break block3;
                    for (RelativePosition lv : candidates) {
                        String string2 = lv.x + " " + lv.y + " " + lv.z;
                        if (!predicate.test(string2)) continue;
                        list.add(lv.x);
                        list.add(lv.x + " " + lv.y);
                        list.add(string2);
                    }
                    break block4;
                }
                strings = remaining.split(" ");
                if (strings.length != 1) break block5;
                for (RelativePosition lv2 : candidates) {
                    String string3 = strings[0] + " " + lv2.y + " " + lv2.z;
                    if (!predicate.test(string3)) continue;
                    list.add(strings[0] + " " + lv2.y);
                    list.add(string3);
                }
                break block4;
            }
            if (strings.length != 2) break block4;
            for (RelativePosition lv2 : candidates) {
                String string3 = strings[0] + " " + strings[1] + " " + lv2.z;
                if (!predicate.test(string3)) continue;
                list.add(string3);
            }
        }
        return CommandSource.suggestMatching(list, builder);
    }

    public static CompletableFuture<Suggestions> suggestColumnPositions(String remaining, Collection<RelativePosition> candidates, SuggestionsBuilder builder, Predicate<String> predicate) {
        ArrayList<String> list;
        block3: {
            block2: {
                list = Lists.newArrayList();
                if (!Strings.isNullOrEmpty(remaining)) break block2;
                for (RelativePosition lv : candidates) {
                    String string2 = lv.x + " " + lv.z;
                    if (!predicate.test(string2)) continue;
                    list.add(lv.x);
                    list.add(string2);
                }
                break block3;
            }
            String[] strings = remaining.split(" ");
            if (strings.length != 1) break block3;
            for (RelativePosition lv2 : candidates) {
                String string3 = strings[0] + " " + lv2.z;
                if (!predicate.test(string3)) continue;
                list.add(string3);
            }
        }
        return CommandSource.suggestMatching(list, builder);
    }

    public static CompletableFuture<Suggestions> suggestMatching(Iterable<String> candidates, SuggestionsBuilder builder) {
        String string = builder.getRemaining().toLowerCase(Locale.ROOT);
        for (String string2 : candidates) {
            if (!CommandSource.shouldSuggest(string, string2.toLowerCase(Locale.ROOT))) continue;
            builder.suggest(string2);
        }
        return builder.buildFuture();
    }

    public static CompletableFuture<Suggestions> suggestMatching(Stream<String> candidates, SuggestionsBuilder builder) {
        String string = builder.getRemaining().toLowerCase(Locale.ROOT);
        candidates.filter(candidate -> CommandSource.shouldSuggest(string, candidate.toLowerCase(Locale.ROOT))).forEach(builder::suggest);
        return builder.buildFuture();
    }

    public static CompletableFuture<Suggestions> suggestMatching(String[] candidates, SuggestionsBuilder builder) {
        String string = builder.getRemaining().toLowerCase(Locale.ROOT);
        for (String string2 : candidates) {
            if (!CommandSource.shouldSuggest(string, string2.toLowerCase(Locale.ROOT))) continue;
            builder.suggest(string2);
        }
        return builder.buildFuture();
    }

    public static <T> CompletableFuture<Suggestions> suggestMatching(Iterable<T> candidates, SuggestionsBuilder builder, Function<T, String> suggestionText, Function<T, Message> tooltip) {
        String string = builder.getRemaining().toLowerCase(Locale.ROOT);
        for (T object : candidates) {
            String string2 = suggestionText.apply(object);
            if (!CommandSource.shouldSuggest(string, string2.toLowerCase(Locale.ROOT))) continue;
            builder.suggest(string2, tooltip.apply(object));
        }
        return builder.buildFuture();
    }

    public static boolean shouldSuggest(String remaining, String candidate) {
        int i = 0;
        while (!candidate.startsWith(remaining, i)) {
            int k;
            int j = candidate.indexOf(46, i);
            if (Math.max(j, k = candidate.indexOf(95, i)) < 0) {
                return false;
            }
            i = j >= 0 && k >= 0 ? Math.min(k, j) : (j >= 0 ? j : k);
            ++i;
        }
        return true;
    }

    public static class RelativePosition {
        public static final RelativePosition ZERO_LOCAL = new RelativePosition("^", "^", "^");
        public static final RelativePosition ZERO_WORLD = new RelativePosition("~", "~", "~");
        public final String x;
        public final String y;
        public final String z;

        public RelativePosition(String x, String y, String z) {
            this.x = x;
            this.y = y;
            this.z = z;
        }
    }

    public static enum SuggestedIdType {
        TAGS,
        ELEMENTS,
        ALL;


        public boolean canSuggestTags() {
            return this == TAGS || this == ALL;
        }

        public boolean canSuggestElements() {
            return this == ELEMENTS || this == ALL;
        }
    }
}

