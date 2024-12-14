/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.server.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import java.util.Collection;
import net.minecraft.command.CommandRegistryAccess;
import net.minecraft.command.argument.EntityArgumentType;
import net.minecraft.command.argument.ItemStackArgument;
import net.minecraft.command.argument.ItemStackArgumentType;
import net.minecraft.entity.ItemEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;

public class GiveCommand {
    public static final int MAX_STACKS = 100;

    public static void register(CommandDispatcher<ServerCommandSource> dispatcher, CommandRegistryAccess commandRegistryAccess) {
        dispatcher.register((LiteralArgumentBuilder)((LiteralArgumentBuilder)CommandManager.literal("give").requires(source -> source.hasPermissionLevel(2))).then(CommandManager.argument("targets", EntityArgumentType.players()).then((ArgumentBuilder<ServerCommandSource, ?>)((RequiredArgumentBuilder)CommandManager.argument("item", ItemStackArgumentType.itemStack(commandRegistryAccess)).executes(context -> GiveCommand.execute((ServerCommandSource)context.getSource(), ItemStackArgumentType.getItemStackArgument(context, "item"), EntityArgumentType.getPlayers(context, "targets"), 1))).then(CommandManager.argument("count", IntegerArgumentType.integer(1)).executes(context -> GiveCommand.execute((ServerCommandSource)context.getSource(), ItemStackArgumentType.getItemStackArgument(context, "item"), EntityArgumentType.getPlayers(context, "targets"), IntegerArgumentType.getInteger(context, "count")))))));
    }

    private static int execute(ServerCommandSource source, ItemStackArgument item, Collection<ServerPlayerEntity> targets, int count) throws CommandSyntaxException {
        ItemStack lv = item.createStack(1, false);
        int j = lv.getMaxCount();
        int k = j * 100;
        if (count > k) {
            source.sendError(Text.translatable("commands.give.failed.toomanyitems", k, lv.toHoverableText()));
            return 0;
        }
        for (ServerPlayerEntity lv2 : targets) {
            int l = count;
            while (l > 0) {
                ItemEntity lv4;
                int m = Math.min(j, l);
                l -= m;
                ItemStack lv3 = item.createStack(m, false);
                boolean bl = lv2.getInventory().insertStack(lv3);
                if (!bl || !lv3.isEmpty()) {
                    lv4 = lv2.dropItem(lv3, false);
                    if (lv4 == null) continue;
                    lv4.resetPickupDelay();
                    lv4.setOwner(lv2.getUuid());
                    continue;
                }
                lv4 = lv2.dropItem(lv, false);
                if (lv4 != null) {
                    lv4.setDespawnImmediately();
                }
                lv2.getWorld().playSound(null, lv2.getX(), lv2.getY(), lv2.getZ(), SoundEvents.ENTITY_ITEM_PICKUP, SoundCategory.PLAYERS, 0.2f, ((lv2.getRandom().nextFloat() - lv2.getRandom().nextFloat()) * 0.7f + 1.0f) * 2.0f);
                lv2.currentScreenHandler.sendContentUpdates();
            }
        }
        if (targets.size() == 1) {
            source.sendFeedback(() -> Text.translatable("commands.give.success.single", count, lv.toHoverableText(), ((ServerPlayerEntity)targets.iterator().next()).getDisplayName()), true);
        } else {
            source.sendFeedback(() -> Text.translatable("commands.give.success.single", count, lv.toHoverableText(), targets.size()), true);
        }
        return targets.size();
    }
}

