/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 * 
 * Could not load the following classes:
 *  net.fabricmc.api.EnvType
 *  net.fabricmc.api.Environment
 */
package net.minecraft.client.gui.hud.spectator;

import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.hud.spectator.SpectatorMenu;
import net.minecraft.client.gui.hud.spectator.SpectatorMenuCommand;
import net.minecraft.client.gui.hud.spectator.SpectatorMenuCommandGroup;
import net.minecraft.client.gui.hud.spectator.TeleportToSpecificPlayerSpectatorCommand;
import net.minecraft.client.network.PlayerListEntry;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.world.GameMode;

@Environment(value=EnvType.CLIENT)
public class TeleportSpectatorMenu
implements SpectatorMenuCommandGroup,
SpectatorMenuCommand {
    private static final Identifier TELEPORT_TO_PLAYER_TEXTURE = Identifier.ofVanilla("spectator/teleport_to_player");
    private static final Comparator<PlayerListEntry> ORDERING = Comparator.comparing(a -> a.getProfile().getId());
    private static final Text TELEPORT_TEXT = Text.translatable("spectatorMenu.teleport");
    private static final Text PROMPT_TEXT = Text.translatable("spectatorMenu.teleport.prompt");
    private final List<SpectatorMenuCommand> elements;

    public TeleportSpectatorMenu() {
        this(MinecraftClient.getInstance().getNetworkHandler().getListedPlayerListEntries());
    }

    public TeleportSpectatorMenu(Collection<PlayerListEntry> entries) {
        this.elements = entries.stream().filter(entry -> entry.getGameMode() != GameMode.SPECTATOR).sorted(ORDERING).map(entry -> new TeleportToSpecificPlayerSpectatorCommand(entry.getProfile())).toList();
    }

    @Override
    public List<SpectatorMenuCommand> getCommands() {
        return this.elements;
    }

    @Override
    public Text getPrompt() {
        return PROMPT_TEXT;
    }

    @Override
    public void use(SpectatorMenu menu) {
        menu.selectElement(this);
    }

    @Override
    public Text getName() {
        return TELEPORT_TEXT;
    }

    @Override
    public void renderIcon(DrawContext context, float brightness, int alpha) {
        context.drawGuiTexture(TELEPORT_TO_PLAYER_TEXTURE, 0, 0, 16, 16);
    }

    @Override
    public boolean isEnabled() {
        return !this.elements.isEmpty();
    }
}

