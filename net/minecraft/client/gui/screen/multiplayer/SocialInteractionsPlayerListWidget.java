/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 * 
 * Could not load the following classes:
 *  net.fabricmc.api.EnvType
 *  net.fabricmc.api.Environment
 *  org.jetbrains.annotations.Nullable
 */
package net.minecraft.client.gui.screen.multiplayer;

import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import com.mojang.authlib.GameProfile;
import it.unimi.dsi.fastutil.objects.ObjectLinkedOpenHashSet;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.multiplayer.SocialInteractionsPlayerListEntry;
import net.minecraft.client.gui.screen.multiplayer.SocialInteractionsScreen;
import net.minecraft.client.gui.widget.ElementListWidget;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.client.network.PlayerListEntry;
import net.minecraft.client.session.report.log.ChatLog;
import net.minecraft.client.session.report.log.ChatLogEntry;
import net.minecraft.client.session.report.log.ReceivedMessage;
import org.jetbrains.annotations.Nullable;

@Environment(value=EnvType.CLIENT)
public class SocialInteractionsPlayerListWidget
extends ElementListWidget<SocialInteractionsPlayerListEntry> {
    private final SocialInteractionsScreen parent;
    private final List<SocialInteractionsPlayerListEntry> players = Lists.newArrayList();
    @Nullable
    private String currentSearch;

    public SocialInteractionsPlayerListWidget(SocialInteractionsScreen parent, MinecraftClient client, int width, int height, int y, int itemHeight) {
        super(client, width, height, y, itemHeight);
        this.parent = parent;
    }

    @Override
    protected void drawMenuListBackground(DrawContext context) {
    }

    @Override
    protected void drawHeaderAndFooterSeparators(DrawContext context) {
    }

    @Override
    protected void enableScissor(DrawContext context) {
        context.enableScissor(this.getX(), this.getY() + 4, this.getRight(), this.getBottom());
    }

    public void update(Collection<UUID> uuids, double scrollAmount, boolean includeOffline) {
        HashMap<UUID, SocialInteractionsPlayerListEntry> map = new HashMap<UUID, SocialInteractionsPlayerListEntry>();
        this.setPlayers(uuids, map);
        this.markOfflineMembers(map, includeOffline);
        this.refresh(map.values(), scrollAmount);
    }

    private void setPlayers(Collection<UUID> playerUuids, Map<UUID, SocialInteractionsPlayerListEntry> entriesByUuids) {
        ClientPlayNetworkHandler lv = this.client.player.networkHandler;
        for (UUID uUID : playerUuids) {
            PlayerListEntry lv2 = lv.getPlayerListEntry(uUID);
            if (lv2 == null) continue;
            boolean bl = lv2.hasPublicKey();
            entriesByUuids.put(uUID, new SocialInteractionsPlayerListEntry(this.client, this.parent, uUID, lv2.getProfile().getName(), lv2::getSkinTextures, bl));
        }
    }

    private void markOfflineMembers(Map<UUID, SocialInteractionsPlayerListEntry> entries, boolean includeOffline) {
        Collection<GameProfile> collection = SocialInteractionsPlayerListWidget.collectReportableProfiles(this.client.getAbuseReportContext().getChatLog());
        for (GameProfile gameProfile : collection) {
            SocialInteractionsPlayerListEntry lv;
            if (includeOffline) {
                lv = entries.computeIfAbsent(gameProfile.getId(), uuid -> {
                    SocialInteractionsPlayerListEntry lv = new SocialInteractionsPlayerListEntry(this.client, this.parent, gameProfile.getId(), gameProfile.getName(), this.client.getSkinProvider().getSkinTexturesSupplier(gameProfile), true);
                    lv.setOffline(true);
                    return lv;
                });
            } else {
                lv = entries.get(gameProfile.getId());
                if (lv == null) continue;
            }
            lv.setSentMessage(true);
        }
    }

    private static Collection<GameProfile> collectReportableProfiles(ChatLog log) {
        ObjectLinkedOpenHashSet<GameProfile> set = new ObjectLinkedOpenHashSet<GameProfile>();
        for (int i = log.getMaxIndex(); i >= log.getMinIndex(); --i) {
            ReceivedMessage.ChatMessage lv2;
            ChatLogEntry lv = log.get(i);
            if (!(lv instanceof ReceivedMessage.ChatMessage) || !(lv2 = (ReceivedMessage.ChatMessage)lv).message().hasSignature()) continue;
            set.add(lv2.profile());
        }
        return set;
    }

    private void sortPlayers() {
        this.players.sort(Comparator.comparing(player -> {
            if (this.client.uuidEquals(player.getUuid())) {
                return 0;
            }
            if (this.client.getAbuseReportContext().draftPlayerUuidEquals(player.getUuid())) {
                return 1;
            }
            if (player.getUuid().version() == 2) {
                return 4;
            }
            if (player.hasSentMessage()) {
                return 2;
            }
            return 3;
        }).thenComparing(player -> {
            int i;
            if (!player.getName().isBlank() && ((i = player.getName().codePointAt(0)) == 95 || i >= 97 && i <= 122 || i >= 65 && i <= 90 || i >= 48 && i <= 57)) {
                return 0;
            }
            return 1;
        }).thenComparing(SocialInteractionsPlayerListEntry::getName, String::compareToIgnoreCase));
    }

    private void refresh(Collection<SocialInteractionsPlayerListEntry> players, double scrollAmount) {
        this.players.clear();
        this.players.addAll(players);
        this.sortPlayers();
        this.filterPlayers();
        this.replaceEntries(this.players);
        this.setScrollAmount(scrollAmount);
    }

    private void filterPlayers() {
        if (this.currentSearch != null) {
            this.players.removeIf(player -> !player.getName().toLowerCase(Locale.ROOT).contains(this.currentSearch));
            this.replaceEntries(this.players);
        }
    }

    public void setCurrentSearch(String currentSearch) {
        this.currentSearch = currentSearch;
    }

    public boolean isEmpty() {
        return this.players.isEmpty();
    }

    public void setPlayerOnline(PlayerListEntry player, SocialInteractionsScreen.Tab tab) {
        UUID uUID = player.getProfile().getId();
        for (SocialInteractionsPlayerListEntry lv : this.players) {
            if (!lv.getUuid().equals(uUID)) continue;
            lv.setOffline(false);
            return;
        }
        if ((tab == SocialInteractionsScreen.Tab.ALL || this.client.getSocialInteractionsManager().isPlayerMuted(uUID)) && (Strings.isNullOrEmpty(this.currentSearch) || player.getProfile().getName().toLowerCase(Locale.ROOT).contains(this.currentSearch))) {
            SocialInteractionsPlayerListEntry lv;
            boolean bl = player.hasPublicKey();
            lv = new SocialInteractionsPlayerListEntry(this.client, this.parent, player.getProfile().getId(), player.getProfile().getName(), player::getSkinTextures, bl);
            this.addEntry(lv);
            this.players.add(lv);
        }
    }

    public void setPlayerOffline(UUID uuid) {
        for (SocialInteractionsPlayerListEntry lv : this.players) {
            if (!lv.getUuid().equals(uuid)) continue;
            lv.setOffline(true);
            return;
        }
    }
}

