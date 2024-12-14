/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.command;

import com.google.common.collect.Maps;
import com.mojang.brigadier.Message;
import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Predicate;
import net.minecraft.advancement.AdvancementEntry;
import net.minecraft.advancement.AdvancementProgress;
import net.minecraft.advancement.PlayerAdvancementTracker;
import net.minecraft.advancement.criterion.CriterionProgress;
import net.minecraft.command.CommandSource;
import net.minecraft.command.EntitySelector;
import net.minecraft.command.EntitySelectorReader;
import net.minecraft.command.FloatRangeArgument;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.loot.condition.LootCondition;
import net.minecraft.loot.context.LootContext;
import net.minecraft.loot.context.LootContextParameterSet;
import net.minecraft.loot.context.LootContextParameters;
import net.minecraft.loot.context.LootContextTypes;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtHelper;
import net.minecraft.nbt.StringNbtReader;
import net.minecraft.predicate.NumberRange;
import net.minecraft.registry.Registries;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.scoreboard.AbstractTeam;
import net.minecraft.scoreboard.ReadableScoreboardScore;
import net.minecraft.scoreboard.ScoreHolder;
import net.minecraft.scoreboard.ScoreboardObjective;
import net.minecraft.scoreboard.ServerScoreboard;
import net.minecraft.scoreboard.Team;
import net.minecraft.server.ServerAdvancementLoader;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.GameMode;

public class EntitySelectorOptions {
    private static final Map<String, SelectorOption> OPTIONS = Maps.newHashMap();
    public static final DynamicCommandExceptionType UNKNOWN_OPTION_EXCEPTION = new DynamicCommandExceptionType(option -> Text.stringifiedTranslatable("argument.entity.options.unknown", option));
    public static final DynamicCommandExceptionType INAPPLICABLE_OPTION_EXCEPTION = new DynamicCommandExceptionType(option -> Text.stringifiedTranslatable("argument.entity.options.inapplicable", option));
    public static final SimpleCommandExceptionType NEGATIVE_DISTANCE_EXCEPTION = new SimpleCommandExceptionType(Text.translatable("argument.entity.options.distance.negative"));
    public static final SimpleCommandExceptionType NEGATIVE_LEVEL_EXCEPTION = new SimpleCommandExceptionType(Text.translatable("argument.entity.options.level.negative"));
    public static final SimpleCommandExceptionType TOO_SMALL_LEVEL_EXCEPTION = new SimpleCommandExceptionType(Text.translatable("argument.entity.options.limit.toosmall"));
    public static final DynamicCommandExceptionType IRREVERSIBLE_SORT_EXCEPTION = new DynamicCommandExceptionType(sortType -> Text.stringifiedTranslatable("argument.entity.options.sort.irreversible", sortType));
    public static final DynamicCommandExceptionType INVALID_MODE_EXCEPTION = new DynamicCommandExceptionType(gameMode -> Text.stringifiedTranslatable("argument.entity.options.mode.invalid", gameMode));
    public static final DynamicCommandExceptionType INVALID_TYPE_EXCEPTION = new DynamicCommandExceptionType(entity -> Text.stringifiedTranslatable("argument.entity.options.type.invalid", entity));

    private static void putOption(String id, SelectorHandler handler, Predicate<EntitySelectorReader> condition, Text description) {
        OPTIONS.put(id, new SelectorOption(handler, condition, description));
    }

    public static void register() {
        if (!OPTIONS.isEmpty()) {
            return;
        }
        EntitySelectorOptions.putOption("name", reader -> {
            int i = reader.getReader().getCursor();
            boolean bl = reader.readNegationCharacter();
            String string = reader.getReader().readString();
            if (reader.excludesName() && !bl) {
                reader.getReader().setCursor(i);
                throw INAPPLICABLE_OPTION_EXCEPTION.createWithContext(reader.getReader(), "name");
            }
            if (bl) {
                reader.setExcludesName(true);
            } else {
                reader.setSelectsName(true);
            }
            reader.addPredicate(entity -> entity.getName().getString().equals(string) != bl);
        }, reader -> !reader.selectsName(), Text.translatable("argument.entity.options.name.description"));
        EntitySelectorOptions.putOption("distance", reader -> {
            int i = reader.getReader().getCursor();
            NumberRange.DoubleRange lv = NumberRange.DoubleRange.parse(reader.getReader());
            if (lv.min().isPresent() && lv.min().get() < 0.0 || lv.max().isPresent() && lv.max().get() < 0.0) {
                reader.getReader().setCursor(i);
                throw NEGATIVE_DISTANCE_EXCEPTION.createWithContext(reader.getReader());
            }
            reader.setDistance(lv);
            reader.setLocalWorldOnly();
        }, reader -> reader.getDistance().isDummy(), Text.translatable("argument.entity.options.distance.description"));
        EntitySelectorOptions.putOption("level", reader -> {
            int i = reader.getReader().getCursor();
            NumberRange.IntRange lv = NumberRange.IntRange.parse(reader.getReader());
            if (lv.min().isPresent() && lv.min().get() < 0 || lv.max().isPresent() && lv.max().get() < 0) {
                reader.getReader().setCursor(i);
                throw NEGATIVE_LEVEL_EXCEPTION.createWithContext(reader.getReader());
            }
            reader.setLevelRange(lv);
            reader.setIncludesNonPlayers(false);
        }, reader -> reader.getLevelRange().isDummy(), Text.translatable("argument.entity.options.level.description"));
        EntitySelectorOptions.putOption("x", reader -> {
            reader.setLocalWorldOnly();
            reader.setX(reader.getReader().readDouble());
        }, reader -> reader.getX() == null, Text.translatable("argument.entity.options.x.description"));
        EntitySelectorOptions.putOption("y", reader -> {
            reader.setLocalWorldOnly();
            reader.setY(reader.getReader().readDouble());
        }, reader -> reader.getY() == null, Text.translatable("argument.entity.options.y.description"));
        EntitySelectorOptions.putOption("z", reader -> {
            reader.setLocalWorldOnly();
            reader.setZ(reader.getReader().readDouble());
        }, reader -> reader.getZ() == null, Text.translatable("argument.entity.options.z.description"));
        EntitySelectorOptions.putOption("dx", reader -> {
            reader.setLocalWorldOnly();
            reader.setDx(reader.getReader().readDouble());
        }, reader -> reader.getDx() == null, Text.translatable("argument.entity.options.dx.description"));
        EntitySelectorOptions.putOption("dy", reader -> {
            reader.setLocalWorldOnly();
            reader.setDy(reader.getReader().readDouble());
        }, reader -> reader.getDy() == null, Text.translatable("argument.entity.options.dy.description"));
        EntitySelectorOptions.putOption("dz", reader -> {
            reader.setLocalWorldOnly();
            reader.setDz(reader.getReader().readDouble());
        }, reader -> reader.getDz() == null, Text.translatable("argument.entity.options.dz.description"));
        EntitySelectorOptions.putOption("x_rotation", reader -> reader.setPitchRange(FloatRangeArgument.parse(reader.getReader(), true, MathHelper::wrapDegrees)), reader -> reader.getPitchRange() == FloatRangeArgument.ANY, Text.translatable("argument.entity.options.x_rotation.description"));
        EntitySelectorOptions.putOption("y_rotation", reader -> reader.setYawRange(FloatRangeArgument.parse(reader.getReader(), true, MathHelper::wrapDegrees)), reader -> reader.getYawRange() == FloatRangeArgument.ANY, Text.translatable("argument.entity.options.y_rotation.description"));
        EntitySelectorOptions.putOption("limit", reader -> {
            int i = reader.getReader().getCursor();
            int j = reader.getReader().readInt();
            if (j < 1) {
                reader.getReader().setCursor(i);
                throw TOO_SMALL_LEVEL_EXCEPTION.createWithContext(reader.getReader());
            }
            reader.setLimit(j);
            reader.setHasLimit(true);
        }, reader -> !reader.isSenderOnly() && !reader.hasLimit(), Text.translatable("argument.entity.options.limit.description"));
        EntitySelectorOptions.putOption("sort", reader -> {
            int i = reader.getReader().getCursor();
            String string = reader.getReader().readUnquotedString();
            reader.setSuggestionProvider((builder, consumer) -> CommandSource.suggestMatching(Arrays.asList("nearest", "furthest", "random", "arbitrary"), builder));
            reader.setSorter(switch (string) {
                case "nearest" -> EntitySelectorReader.NEAREST;
                case "furthest" -> EntitySelectorReader.FURTHEST;
                case "random" -> EntitySelectorReader.RANDOM;
                case "arbitrary" -> EntitySelector.ARBITRARY;
                default -> {
                    reader.getReader().setCursor(i);
                    throw IRREVERSIBLE_SORT_EXCEPTION.createWithContext(reader.getReader(), string);
                }
            });
            reader.setHasSorter(true);
        }, reader -> !reader.isSenderOnly() && !reader.hasSorter(), Text.translatable("argument.entity.options.sort.description"));
        EntitySelectorOptions.putOption("gamemode", reader -> {
            reader.setSuggestionProvider((builder, consumer) -> {
                String string = builder.getRemaining().toLowerCase(Locale.ROOT);
                boolean bl = !reader.excludesGameMode();
                boolean bl2 = true;
                if (!string.isEmpty()) {
                    if (string.charAt(0) == '!') {
                        bl = false;
                        string = string.substring(1);
                    } else {
                        bl2 = false;
                    }
                }
                for (GameMode lv : GameMode.values()) {
                    if (!lv.getName().toLowerCase(Locale.ROOT).startsWith(string)) continue;
                    if (bl2) {
                        builder.suggest("!" + lv.getName());
                    }
                    if (!bl) continue;
                    builder.suggest(lv.getName());
                }
                return builder.buildFuture();
            });
            int i = reader.getReader().getCursor();
            boolean bl = reader.readNegationCharacter();
            if (reader.excludesGameMode() && !bl) {
                reader.getReader().setCursor(i);
                throw INAPPLICABLE_OPTION_EXCEPTION.createWithContext(reader.getReader(), "gamemode");
            }
            String string = reader.getReader().readUnquotedString();
            GameMode lv = GameMode.byName(string, null);
            if (lv == null) {
                reader.getReader().setCursor(i);
                throw INVALID_MODE_EXCEPTION.createWithContext(reader.getReader(), string);
            }
            reader.setIncludesNonPlayers(false);
            reader.addPredicate(entity -> {
                if (!(entity instanceof ServerPlayerEntity)) {
                    return false;
                }
                GameMode lv = ((ServerPlayerEntity)entity).interactionManager.getGameMode();
                return bl ? lv != lv : lv == lv;
            });
            if (bl) {
                reader.setExcludesGameMode(true);
            } else {
                reader.setSelectsGameMode(true);
            }
        }, reader -> !reader.selectsGameMode(), Text.translatable("argument.entity.options.gamemode.description"));
        EntitySelectorOptions.putOption("team", reader -> {
            boolean bl = reader.readNegationCharacter();
            String string = reader.getReader().readUnquotedString();
            reader.addPredicate(entity -> {
                if (!(entity instanceof LivingEntity)) {
                    return false;
                }
                Team lv = entity.getScoreboardTeam();
                String string2 = lv == null ? "" : ((AbstractTeam)lv).getName();
                return string2.equals(string) != bl;
            });
            if (bl) {
                reader.setExcludesTeam(true);
            } else {
                reader.setSelectsTeam(true);
            }
        }, reader -> !reader.selectsTeam(), Text.translatable("argument.entity.options.team.description"));
        EntitySelectorOptions.putOption("type", reader -> {
            reader.setSuggestionProvider((builder, consumer) -> {
                CommandSource.suggestIdentifiers(Registries.ENTITY_TYPE.getIds(), builder, String.valueOf('!'));
                CommandSource.suggestIdentifiers(Registries.ENTITY_TYPE.streamTags().map(TagKey::id), builder, "!#");
                if (!reader.excludesEntityType()) {
                    CommandSource.suggestIdentifiers(Registries.ENTITY_TYPE.getIds(), builder);
                    CommandSource.suggestIdentifiers(Registries.ENTITY_TYPE.streamTags().map(TagKey::id), builder, String.valueOf('#'));
                }
                return builder.buildFuture();
            });
            int i = reader.getReader().getCursor();
            boolean bl = reader.readNegationCharacter();
            if (reader.excludesEntityType() && !bl) {
                reader.getReader().setCursor(i);
                throw INAPPLICABLE_OPTION_EXCEPTION.createWithContext(reader.getReader(), "type");
            }
            if (bl) {
                reader.setExcludesEntityType();
            }
            if (reader.readTagCharacter()) {
                TagKey<EntityType<?>> lv = TagKey.of(RegistryKeys.ENTITY_TYPE, Identifier.fromCommandInput(reader.getReader()));
                reader.addPredicate(entity -> entity.getType().isIn(lv) != bl);
            } else {
                Identifier lv2 = Identifier.fromCommandInput(reader.getReader());
                EntityType lv3 = (EntityType)Registries.ENTITY_TYPE.getOrEmpty(lv2).orElseThrow(() -> {
                    reader.getReader().setCursor(i);
                    return INVALID_TYPE_EXCEPTION.createWithContext(reader.getReader(), lv2.toString());
                });
                if (Objects.equals(EntityType.PLAYER, lv3) && !bl) {
                    reader.setIncludesNonPlayers(false);
                }
                reader.addPredicate(entity -> Objects.equals(lv3, entity.getType()) != bl);
                if (!bl) {
                    reader.setEntityType(lv3);
                }
            }
        }, reader -> !reader.selectsEntityType(), Text.translatable("argument.entity.options.type.description"));
        EntitySelectorOptions.putOption("tag", reader -> {
            boolean bl = reader.readNegationCharacter();
            String string = reader.getReader().readUnquotedString();
            reader.addPredicate(entity -> {
                if ("".equals(string)) {
                    return entity.getCommandTags().isEmpty() != bl;
                }
                return entity.getCommandTags().contains(string) != bl;
            });
        }, reader -> true, Text.translatable("argument.entity.options.tag.description"));
        EntitySelectorOptions.putOption("nbt", reader -> {
            boolean bl = reader.readNegationCharacter();
            NbtCompound lv = new StringNbtReader(reader.getReader()).parseCompound();
            reader.addPredicate(entity -> {
                ServerPlayerEntity lv2;
                ItemStack lv3;
                NbtCompound lv = entity.writeNbt(new NbtCompound());
                if (entity instanceof ServerPlayerEntity && !(lv3 = (lv2 = (ServerPlayerEntity)entity).getInventory().getMainHandStack()).isEmpty()) {
                    lv.put("SelectedItem", lv3.encode(lv2.getRegistryManager()));
                }
                return NbtHelper.matches(lv, lv, true) != bl;
            });
        }, reader -> true, Text.translatable("argument.entity.options.nbt.description"));
        EntitySelectorOptions.putOption("scores", reader -> {
            StringReader stringReader = reader.getReader();
            HashMap<String, NumberRange.IntRange> map = Maps.newHashMap();
            stringReader.expect('{');
            stringReader.skipWhitespace();
            while (stringReader.canRead() && stringReader.peek() != '}') {
                stringReader.skipWhitespace();
                String string = stringReader.readUnquotedString();
                stringReader.skipWhitespace();
                stringReader.expect('=');
                stringReader.skipWhitespace();
                NumberRange.IntRange lv = NumberRange.IntRange.parse(stringReader);
                map.put(string, lv);
                stringReader.skipWhitespace();
                if (!stringReader.canRead() || stringReader.peek() != ',') continue;
                stringReader.skip();
            }
            stringReader.expect('}');
            if (!map.isEmpty()) {
                reader.addPredicate(entity -> {
                    ServerScoreboard lv = entity.getServer().getScoreboard();
                    for (Map.Entry entry : map.entrySet()) {
                        ScoreboardObjective lv2 = lv.getNullableObjective((String)entry.getKey());
                        if (lv2 == null) {
                            return false;
                        }
                        ReadableScoreboardScore lv3 = lv.getScore((ScoreHolder)entity, lv2);
                        if (lv3 == null) {
                            return false;
                        }
                        if (((NumberRange.IntRange)entry.getValue()).test(lv3.getScore())) continue;
                        return false;
                    }
                    return true;
                });
            }
            reader.setSelectsScores(true);
        }, reader -> !reader.selectsScores(), Text.translatable("argument.entity.options.scores.description"));
        EntitySelectorOptions.putOption("advancements", reader -> {
            StringReader stringReader = reader.getReader();
            HashMap<Identifier, Predicate<AdvancementProgress>> map = Maps.newHashMap();
            stringReader.expect('{');
            stringReader.skipWhitespace();
            while (stringReader.canRead() && stringReader.peek() != '}') {
                stringReader.skipWhitespace();
                Identifier lv = Identifier.fromCommandInput(stringReader);
                stringReader.skipWhitespace();
                stringReader.expect('=');
                stringReader.skipWhitespace();
                if (stringReader.canRead() && stringReader.peek() == '{') {
                    HashMap<String, Predicate<CriterionProgress>> map2 = Maps.newHashMap();
                    stringReader.skipWhitespace();
                    stringReader.expect('{');
                    stringReader.skipWhitespace();
                    while (stringReader.canRead() && stringReader.peek() != '}') {
                        stringReader.skipWhitespace();
                        String string = stringReader.readUnquotedString();
                        stringReader.skipWhitespace();
                        stringReader.expect('=');
                        stringReader.skipWhitespace();
                        boolean bl = stringReader.readBoolean();
                        map2.put(string, criterionProgress -> criterionProgress.isObtained() == bl);
                        stringReader.skipWhitespace();
                        if (!stringReader.canRead() || stringReader.peek() != ',') continue;
                        stringReader.skip();
                    }
                    stringReader.skipWhitespace();
                    stringReader.expect('}');
                    stringReader.skipWhitespace();
                    map.put(lv, advancementProgress -> {
                        for (Map.Entry entry : map2.entrySet()) {
                            CriterionProgress lv = advancementProgress.getCriterionProgress((String)entry.getKey());
                            if (lv != null && ((Predicate)entry.getValue()).test(lv)) continue;
                            return false;
                        }
                        return true;
                    });
                } else {
                    boolean bl2 = stringReader.readBoolean();
                    map.put(lv, advancementProgress -> advancementProgress.isDone() == bl2);
                }
                stringReader.skipWhitespace();
                if (!stringReader.canRead() || stringReader.peek() != ',') continue;
                stringReader.skip();
            }
            stringReader.expect('}');
            if (!map.isEmpty()) {
                reader.addPredicate(entity -> {
                    if (!(entity instanceof ServerPlayerEntity)) {
                        return false;
                    }
                    ServerPlayerEntity lv = (ServerPlayerEntity)entity;
                    PlayerAdvancementTracker lv2 = lv.getAdvancementTracker();
                    ServerAdvancementLoader lv3 = lv.getServer().getAdvancementLoader();
                    for (Map.Entry entry : map.entrySet()) {
                        AdvancementEntry lv4 = lv3.get((Identifier)entry.getKey());
                        if (lv4 != null && ((Predicate)entry.getValue()).test(lv2.getProgress(lv4))) continue;
                        return false;
                    }
                    return true;
                });
                reader.setIncludesNonPlayers(false);
            }
            reader.setSelectsAdvancements(true);
        }, reader -> !reader.selectsAdvancements(), Text.translatable("argument.entity.options.advancements.description"));
        EntitySelectorOptions.putOption("predicate", reader -> {
            boolean bl = reader.readNegationCharacter();
            RegistryKey<LootCondition> lv = RegistryKey.of(RegistryKeys.PREDICATE, Identifier.fromCommandInput(reader.getReader()));
            reader.addPredicate(entity -> {
                if (!(entity.getWorld() instanceof ServerWorld)) {
                    return false;
                }
                ServerWorld lv = (ServerWorld)entity.getWorld();
                Optional<LootCondition> optional = lv.getServer().getReloadableRegistries().createRegistryLookup().getOptionalEntry(RegistryKeys.PREDICATE, lv).map(RegistryEntry::value);
                if (optional.isEmpty()) {
                    return false;
                }
                LootContextParameterSet lv2 = new LootContextParameterSet.Builder(lv).add(LootContextParameters.THIS_ENTITY, entity).add(LootContextParameters.ORIGIN, entity.getPos()).build(LootContextTypes.SELECTOR);
                LootContext lv3 = new LootContext.Builder(lv2).build(Optional.empty());
                lv3.markActive(LootContext.predicate(optional.get()));
                return bl ^ optional.get().test(lv3);
            });
        }, reader -> true, Text.translatable("argument.entity.options.predicate.description"));
    }

    public static SelectorHandler getHandler(EntitySelectorReader reader, String option, int restoreCursor) throws CommandSyntaxException {
        SelectorOption lv = OPTIONS.get(option);
        if (lv != null) {
            if (lv.condition.test(reader)) {
                return lv.handler;
            }
            throw INAPPLICABLE_OPTION_EXCEPTION.createWithContext(reader.getReader(), option);
        }
        reader.getReader().setCursor(restoreCursor);
        throw UNKNOWN_OPTION_EXCEPTION.createWithContext(reader.getReader(), option);
    }

    public static void suggestOptions(EntitySelectorReader reader, SuggestionsBuilder suggestionBuilder) {
        String string = suggestionBuilder.getRemaining().toLowerCase(Locale.ROOT);
        for (Map.Entry<String, SelectorOption> entry : OPTIONS.entrySet()) {
            if (!entry.getValue().condition.test(reader) || !entry.getKey().toLowerCase(Locale.ROOT).startsWith(string)) continue;
            suggestionBuilder.suggest(entry.getKey() + "=", (Message)entry.getValue().description);
        }
    }

    record SelectorOption(SelectorHandler handler, Predicate<EntitySelectorReader> condition, Text description) {
    }

    public static interface SelectorHandler {
        public void handle(EntitySelectorReader var1) throws CommandSyntaxException;
    }
}

