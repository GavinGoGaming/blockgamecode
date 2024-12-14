/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.server.command;

import com.google.common.collect.Lists;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.logging.LogUtils;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import net.minecraft.resource.ResourcePackManager;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.Text;
import net.minecraft.world.SaveProperties;
import org.slf4j.Logger;

public class ReloadCommand {
    private static final Logger LOGGER = LogUtils.getLogger();

    public static void tryReloadDataPacks(Collection<String> dataPacks, ServerCommandSource source) {
        source.getServer().reloadResources(dataPacks).exceptionally(throwable -> {
            LOGGER.warn("Failed to execute reload", (Throwable)throwable);
            source.sendError(Text.translatable("commands.reload.failure"));
            return null;
        });
    }

    private static Collection<String> findNewDataPacks(ResourcePackManager dataPackManager, SaveProperties saveProperties, Collection<String> enabledDataPacks) {
        dataPackManager.scanPacks();
        ArrayList<String> collection2 = Lists.newArrayList(enabledDataPacks);
        List<String> collection3 = saveProperties.getDataConfiguration().dataPacks().getDisabled();
        for (String string : dataPackManager.getIds()) {
            if (collection3.contains(string) || collection2.contains(string)) continue;
            collection2.add(string);
        }
        return collection2;
    }

    public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        dispatcher.register((LiteralArgumentBuilder)((LiteralArgumentBuilder)CommandManager.literal("reload").requires(source -> source.hasPermissionLevel(2))).executes(context -> {
            ServerCommandSource lv = (ServerCommandSource)context.getSource();
            MinecraftServer minecraftServer = lv.getServer();
            ResourcePackManager lv2 = minecraftServer.getDataPackManager();
            SaveProperties lv3 = minecraftServer.getSaveProperties();
            Collection<String> collection = lv2.getEnabledIds();
            Collection<String> collection2 = ReloadCommand.findNewDataPacks(lv2, lv3, collection);
            lv.sendFeedback(() -> Text.translatable("commands.reload.success"), true);
            ReloadCommand.tryReloadDataPacks(collection2, lv);
            return 0;
        }));
    }
}

