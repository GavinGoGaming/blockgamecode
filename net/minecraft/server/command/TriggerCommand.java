/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.server.command;

import com.google.common.collect.Lists;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import java.util.ArrayList;
import java.util.concurrent.CompletableFuture;
import net.minecraft.command.CommandSource;
import net.minecraft.command.argument.ScoreboardObjectiveArgumentType;
import net.minecraft.entity.Entity;
import net.minecraft.scoreboard.ReadableScoreboardScore;
import net.minecraft.scoreboard.ScoreAccess;
import net.minecraft.scoreboard.ScoreHolder;
import net.minecraft.scoreboard.Scoreboard;
import net.minecraft.scoreboard.ScoreboardCriterion;
import net.minecraft.scoreboard.ScoreboardObjective;
import net.minecraft.scoreboard.ServerScoreboard;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;

public class TriggerCommand {
    private static final SimpleCommandExceptionType FAILED_UNPRIMED_EXCEPTION = new SimpleCommandExceptionType(Text.translatable("commands.trigger.failed.unprimed"));
    private static final SimpleCommandExceptionType FAILED_INVALID_EXCEPTION = new SimpleCommandExceptionType(Text.translatable("commands.trigger.failed.invalid"));

    public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        dispatcher.register((LiteralArgumentBuilder)CommandManager.literal("trigger").then((ArgumentBuilder<ServerCommandSource, ?>)((RequiredArgumentBuilder)((RequiredArgumentBuilder)CommandManager.argument("objective", ScoreboardObjectiveArgumentType.scoreboardObjective()).suggests((context, builder) -> TriggerCommand.suggestObjectives((ServerCommandSource)context.getSource(), builder)).executes(context -> TriggerCommand.executeSimple((ServerCommandSource)context.getSource(), ((ServerCommandSource)context.getSource()).getPlayerOrThrow(), ScoreboardObjectiveArgumentType.getObjective(context, "objective")))).then(CommandManager.literal("add").then((ArgumentBuilder<ServerCommandSource, ?>)CommandManager.argument("value", IntegerArgumentType.integer()).executes(context -> TriggerCommand.executeAdd((ServerCommandSource)context.getSource(), ((ServerCommandSource)context.getSource()).getPlayerOrThrow(), ScoreboardObjectiveArgumentType.getObjective(context, "objective"), IntegerArgumentType.getInteger(context, "value")))))).then(CommandManager.literal("set").then((ArgumentBuilder<ServerCommandSource, ?>)CommandManager.argument("value", IntegerArgumentType.integer()).executes(context -> TriggerCommand.executeSet((ServerCommandSource)context.getSource(), ((ServerCommandSource)context.getSource()).getPlayerOrThrow(), ScoreboardObjectiveArgumentType.getObjective(context, "objective"), IntegerArgumentType.getInteger(context, "value")))))));
    }

    public static CompletableFuture<Suggestions> suggestObjectives(ServerCommandSource source, SuggestionsBuilder builder) {
        Entity lv = source.getEntity();
        ArrayList<String> list = Lists.newArrayList();
        if (lv != null) {
            ServerScoreboard lv2 = source.getServer().getScoreboard();
            for (ScoreboardObjective lv3 : lv2.getObjectives()) {
                ReadableScoreboardScore lv4;
                if (lv3.getCriterion() != ScoreboardCriterion.TRIGGER || (lv4 = lv2.getScore(lv, lv3)) == null || lv4.isLocked()) continue;
                list.add(lv3.getName());
            }
        }
        return CommandSource.suggestMatching(list, builder);
    }

    private static int executeAdd(ServerCommandSource source, ServerPlayerEntity player, ScoreboardObjective objective, int amount) throws CommandSyntaxException {
        ScoreAccess lv = TriggerCommand.getScore(source.getServer().getScoreboard(), player, objective);
        int j = lv.incrementScore(amount);
        source.sendFeedback(() -> Text.translatable("commands.trigger.add.success", objective.toHoverableText(), amount), true);
        return j;
    }

    private static int executeSet(ServerCommandSource source, ServerPlayerEntity player, ScoreboardObjective objective, int value) throws CommandSyntaxException {
        ScoreAccess lv = TriggerCommand.getScore(source.getServer().getScoreboard(), player, objective);
        lv.setScore(value);
        source.sendFeedback(() -> Text.translatable("commands.trigger.set.success", objective.toHoverableText(), value), true);
        return value;
    }

    private static int executeSimple(ServerCommandSource source, ServerPlayerEntity player, ScoreboardObjective objective) throws CommandSyntaxException {
        ScoreAccess lv = TriggerCommand.getScore(source.getServer().getScoreboard(), player, objective);
        int i = lv.incrementScore(1);
        source.sendFeedback(() -> Text.translatable("commands.trigger.simple.success", objective.toHoverableText()), true);
        return i;
    }

    private static ScoreAccess getScore(Scoreboard scoreboard, ScoreHolder scoreHolder, ScoreboardObjective objective) throws CommandSyntaxException {
        if (objective.getCriterion() != ScoreboardCriterion.TRIGGER) {
            throw FAILED_INVALID_EXCEPTION.create();
        }
        ReadableScoreboardScore lv = scoreboard.getScore(scoreHolder, objective);
        if (lv == null || lv.isLocked()) {
            throw FAILED_UNPRIMED_EXCEPTION.create();
        }
        ScoreAccess lv2 = scoreboard.getOrCreateScore(scoreHolder, objective);
        lv2.lock();
        return lv2;
    }
}

