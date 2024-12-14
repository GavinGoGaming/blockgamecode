/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.server.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import com.mojang.brigadier.tree.LiteralCommandNode;
import java.util.List;
import net.minecraft.command.argument.MessageArgumentType;
import net.minecraft.entity.Entity;
import net.minecraft.network.message.MessageType;
import net.minecraft.network.message.SentMessage;
import net.minecraft.network.message.SignedMessage;
import net.minecraft.scoreboard.Team;
import net.minecraft.server.PlayerManager;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.ClickEvent;
import net.minecraft.text.HoverEvent;
import net.minecraft.text.MutableText;
import net.minecraft.text.Style;
import net.minecraft.text.Text;

public class TeamMsgCommand {
    private static final Style STYLE = Style.EMPTY.withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, Text.translatable("chat.type.team.hover"))).withClickEvent(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, "/teammsg "));
    private static final SimpleCommandExceptionType NO_TEAM_EXCEPTION = new SimpleCommandExceptionType(Text.translatable("commands.teammsg.failed.noteam"));

    public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        LiteralCommandNode<ServerCommandSource> literalCommandNode = dispatcher.register((LiteralArgumentBuilder)CommandManager.literal("teammsg").then((ArgumentBuilder<ServerCommandSource, ?>)CommandManager.argument("message", MessageArgumentType.message()).executes(context -> {
            ServerCommandSource lv = (ServerCommandSource)context.getSource();
            Entity lv2 = lv.getEntityOrThrow();
            Team lv3 = lv2.getScoreboardTeam();
            if (lv3 == null) {
                throw NO_TEAM_EXCEPTION.create();
            }
            List<ServerPlayerEntity> list = lv.getServer().getPlayerManager().getPlayerList().stream().filter(player -> player == lv2 || player.getScoreboardTeam() == lv3).toList();
            if (!list.isEmpty()) {
                MessageArgumentType.getSignedMessage(context, "message", message -> TeamMsgCommand.execute(lv, lv2, lv3, list, message));
            }
            return list.size();
        })));
        dispatcher.register((LiteralArgumentBuilder)CommandManager.literal("tm").redirect(literalCommandNode));
    }

    private static void execute(ServerCommandSource source, Entity entity, Team team, List<ServerPlayerEntity> recipients, SignedMessage message) {
        MutableText lv = team.getFormattedName().fillStyle(STYLE);
        MessageType.Parameters lv2 = MessageType.params(MessageType.TEAM_MSG_COMMAND_INCOMING, source).withTargetName(lv);
        MessageType.Parameters lv3 = MessageType.params(MessageType.TEAM_MSG_COMMAND_OUTGOING, source).withTargetName(lv);
        SentMessage lv4 = SentMessage.of(message);
        boolean bl = false;
        for (ServerPlayerEntity lv5 : recipients) {
            MessageType.Parameters lv6 = lv5 == entity ? lv3 : lv2;
            boolean bl2 = source.shouldFilterText(lv5);
            lv5.sendChatMessage(lv4, bl2, lv6);
            bl |= bl2 && message.isFullyFiltered();
        }
        if (bl) {
            source.sendMessage(PlayerManager.FILTERED_FULL_TEXT);
        }
    }
}

