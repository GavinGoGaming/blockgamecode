/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 * 
 * Could not load the following classes:
 *  net.fabricmc.api.EnvType
 *  net.fabricmc.api.Environment
 *  org.jetbrains.annotations.Nullable
 */
package net.minecraft.client.network;

import com.mojang.logging.LogUtils;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import java.util.Map;
import java.util.Optional;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.advancement.AdvancementDisplay;
import net.minecraft.advancement.AdvancementEntry;
import net.minecraft.advancement.AdvancementManager;
import net.minecraft.advancement.AdvancementProgress;
import net.minecraft.advancement.PlacedAdvancement;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.client.session.telemetry.WorldSession;
import net.minecraft.client.toast.AdvancementToast;
import net.minecraft.network.packet.c2s.play.AdvancementTabC2SPacket;
import net.minecraft.network.packet.s2c.play.AdvancementUpdateS2CPacket;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;

@Environment(value=EnvType.CLIENT)
public class ClientAdvancementManager {
    private static final Logger LOGGER = LogUtils.getLogger();
    private final MinecraftClient client;
    private final WorldSession worldSession;
    private final AdvancementManager manager = new AdvancementManager();
    private final Map<AdvancementEntry, AdvancementProgress> advancementProgresses = new Object2ObjectOpenHashMap<AdvancementEntry, AdvancementProgress>();
    @Nullable
    private Listener listener;
    @Nullable
    private AdvancementEntry selectedTab;

    public ClientAdvancementManager(MinecraftClient client, WorldSession worldSession) {
        this.client = client;
        this.worldSession = worldSession;
    }

    public void onAdvancements(AdvancementUpdateS2CPacket packet) {
        if (packet.shouldClearCurrent()) {
            this.manager.clear();
            this.advancementProgresses.clear();
        }
        this.manager.removeAll(packet.getAdvancementIdsToRemove());
        this.manager.addAll(packet.getAdvancementsToEarn());
        for (Map.Entry<Identifier, AdvancementProgress> entry : packet.getAdvancementsToProgress().entrySet()) {
            PlacedAdvancement lv = this.manager.get(entry.getKey());
            if (lv != null) {
                Optional<AdvancementDisplay> optional;
                AdvancementProgress lv2 = entry.getValue();
                lv2.init(lv.getAdvancement().requirements());
                this.advancementProgresses.put(lv.getAdvancementEntry(), lv2);
                if (this.listener != null) {
                    this.listener.setProgress(lv, lv2);
                }
                if (packet.shouldClearCurrent() || !lv2.isDone()) continue;
                if (this.client.world != null) {
                    this.worldSession.onAdvancementMade(this.client.world, lv.getAdvancementEntry());
                }
                if (!(optional = lv.getAdvancement().display()).isPresent() || !optional.get().shouldShowToast()) continue;
                this.client.getToastManager().add(new AdvancementToast(lv.getAdvancementEntry()));
                continue;
            }
            LOGGER.warn("Server informed client about progress for unknown advancement {}", (Object)entry.getKey());
        }
    }

    public AdvancementManager getManager() {
        return this.manager;
    }

    public void selectTab(@Nullable AdvancementEntry tab, boolean local) {
        ClientPlayNetworkHandler lv = this.client.getNetworkHandler();
        if (lv != null && tab != null && local) {
            lv.sendPacket(AdvancementTabC2SPacket.open(tab));
        }
        if (this.selectedTab != tab) {
            this.selectedTab = tab;
            if (this.listener != null) {
                this.listener.selectTab(tab);
            }
        }
    }

    public void setListener(@Nullable Listener listener) {
        this.listener = listener;
        this.manager.setListener(listener);
        if (listener != null) {
            this.advancementProgresses.forEach((advancement, progress) -> {
                PlacedAdvancement lv = this.manager.get((AdvancementEntry)advancement);
                if (lv != null) {
                    listener.setProgress(lv, (AdvancementProgress)progress);
                }
            });
            listener.selectTab(this.selectedTab);
        }
    }

    @Nullable
    public AdvancementEntry get(Identifier id) {
        PlacedAdvancement lv = this.manager.get(id);
        return lv != null ? lv.getAdvancementEntry() : null;
    }

    @Environment(value=EnvType.CLIENT)
    public static interface Listener
    extends AdvancementManager.Listener {
        public void setProgress(PlacedAdvancement var1, AdvancementProgress var2);

        public void selectTab(@Nullable AdvancementEntry var1);
    }
}

