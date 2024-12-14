/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 * 
 * Could not load the following classes:
 *  net.fabricmc.api.EnvType
 *  net.fabricmc.api.Environment
 *  org.jetbrains.annotations.Nullable
 */
package net.minecraft.client.gui.hud;

import com.mojang.authlib.GameProfile;
import com.mojang.blaze3d.systems.RenderSystem;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.PlayerSkinDrawer;
import net.minecraft.client.gui.hud.InGameHud;
import net.minecraft.client.network.PlayerListEntry;
import net.minecraft.client.render.entity.LivingEntityRenderer;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerModelPart;
import net.minecraft.scoreboard.ReadableScoreboardScore;
import net.minecraft.scoreboard.ScoreHolder;
import net.minecraft.scoreboard.Scoreboard;
import net.minecraft.scoreboard.ScoreboardCriterion;
import net.minecraft.scoreboard.ScoreboardObjective;
import net.minecraft.scoreboard.Team;
import net.minecraft.scoreboard.number.NumberFormat;
import net.minecraft.scoreboard.number.StyledNumberFormat;
import net.minecraft.text.MutableText;
import net.minecraft.text.OrderedText;
import net.minecraft.text.Text;
import net.minecraft.text.Texts;
import net.minecraft.util.Colors;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;
import net.minecraft.util.Nullables;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.GameMode;
import org.jetbrains.annotations.Nullable;

@Environment(value=EnvType.CLIENT)
public class PlayerListHud {
    private static final Identifier PING_UNKNOWN_ICON_TEXTURE = Identifier.ofVanilla("icon/ping_unknown");
    private static final Identifier PING_1_ICON_TEXTURE = Identifier.ofVanilla("icon/ping_1");
    private static final Identifier PING_2_ICON_TEXTURE = Identifier.ofVanilla("icon/ping_2");
    private static final Identifier PING_3_ICON_TEXTURE = Identifier.ofVanilla("icon/ping_3");
    private static final Identifier PING_4_ICON_TEXTURE = Identifier.ofVanilla("icon/ping_4");
    private static final Identifier PING_5_ICON_TEXTURE = Identifier.ofVanilla("icon/ping_5");
    private static final Identifier CONTAINER_HEART_BLINKING_TEXTURE = Identifier.ofVanilla("hud/heart/container_blinking");
    private static final Identifier CONTAINER_HEART_TEXTURE = Identifier.ofVanilla("hud/heart/container");
    private static final Identifier FULL_HEART_BLINKING_TEXTURE = Identifier.ofVanilla("hud/heart/full_blinking");
    private static final Identifier HALF_HEART_BLINKING_TEXTURE = Identifier.ofVanilla("hud/heart/half_blinking");
    private static final Identifier ABSORBING_FULL_HEART_BLINKING_TEXTURE = Identifier.ofVanilla("hud/heart/absorbing_full_blinking");
    private static final Identifier FULL_HEART_TEXTURE = Identifier.ofVanilla("hud/heart/full");
    private static final Identifier ABSORBING_HALF_HEART_BLINKING_TEXTURE = Identifier.ofVanilla("hud/heart/absorbing_half_blinking");
    private static final Identifier HALF_HEART_TEXTURE = Identifier.ofVanilla("hud/heart/half");
    private static final Comparator<PlayerListEntry> ENTRY_ORDERING = Comparator.comparingInt(entry -> entry.getGameMode() == GameMode.SPECTATOR ? 1 : 0).thenComparing(entry -> Nullables.mapOrElse(entry.getScoreboardTeam(), Team::getName, "")).thenComparing(entry -> entry.getProfile().getName(), String::compareToIgnoreCase);
    public static final int MAX_ROWS = 20;
    private final MinecraftClient client;
    private final InGameHud inGameHud;
    @Nullable
    private Text footer;
    @Nullable
    private Text header;
    private boolean visible;
    private final Map<UUID, Heart> hearts = new Object2ObjectOpenHashMap<UUID, Heart>();

    public PlayerListHud(MinecraftClient client, InGameHud inGameHud) {
        this.client = client;
        this.inGameHud = inGameHud;
    }

    public Text getPlayerName(PlayerListEntry entry) {
        if (entry.getDisplayName() != null) {
            return this.applyGameModeFormatting(entry, entry.getDisplayName().copy());
        }
        return this.applyGameModeFormatting(entry, Team.decorateName(entry.getScoreboardTeam(), Text.literal(entry.getProfile().getName())));
    }

    private Text applyGameModeFormatting(PlayerListEntry entry, MutableText name) {
        return entry.getGameMode() == GameMode.SPECTATOR ? name.formatted(Formatting.ITALIC) : name;
    }

    public void setVisible(boolean visible) {
        if (this.visible != visible) {
            this.hearts.clear();
            this.visible = visible;
            if (visible) {
                MutableText lv = Texts.join(this.collectPlayerEntries(), Text.literal(", "), this::getPlayerName);
                this.client.getNarratorManager().narrate(Text.translatable("multiplayer.player.list.narration", lv));
            }
        }
    }

    private List<PlayerListEntry> collectPlayerEntries() {
        return this.client.player.networkHandler.getListedPlayerListEntries().stream().sorted(ENTRY_ORDERING).limit(80L).toList();
    }

    public void render(DrawContext context, int scaledWindowWidth, Scoreboard scoreboard, @Nullable ScoreboardObjective objective) {
        int y;
        int v;
        boolean bl;
        int o;
        int n;
        List<PlayerListEntry> list = this.collectPlayerEntries();
        ArrayList<ScoreDisplayEntry> list2 = new ArrayList<ScoreDisplayEntry>(list.size());
        int j = this.client.textRenderer.getWidth(" ");
        int k = 0;
        int l = 0;
        for (PlayerListEntry lv : list) {
            Text lv2 = this.getPlayerName(lv);
            k = Math.max(k, this.client.textRenderer.getWidth(lv2));
            int m = 0;
            MutableText lv3 = null;
            n = 0;
            if (objective != null) {
                ScoreHolder lv4 = ScoreHolder.fromProfile(lv.getProfile());
                ReadableScoreboardScore lv5 = scoreboard.getScore(lv4, objective);
                if (lv5 != null) {
                    m = lv5.getScore();
                }
                if (objective.getRenderType() != ScoreboardCriterion.RenderType.HEARTS) {
                    NumberFormat lv6 = objective.getNumberFormatOr(StyledNumberFormat.YELLOW);
                    lv3 = ReadableScoreboardScore.getFormattedScore(lv5, lv6);
                    n = this.client.textRenderer.getWidth(lv3);
                    l = Math.max(l, n > 0 ? j + n : 0);
                }
            }
            list2.add(new ScoreDisplayEntry(lv2, m, lv3, n));
        }
        if (!this.hearts.isEmpty()) {
            Set set = list.stream().map(playerEntry -> playerEntry.getProfile().getId()).collect(Collectors.toSet());
            this.hearts.keySet().removeIf(uuid -> !set.contains(uuid));
        }
        int p = o = list.size();
        int q = 1;
        while (p > 20) {
            p = (o + ++q - 1) / q;
        }
        boolean bl2 = bl = this.client.isInSingleplayer() || this.client.getNetworkHandler().getConnection().isEncrypted();
        int r = objective != null ? (objective.getRenderType() == ScoreboardCriterion.RenderType.HEARTS ? 90 : l) : 0;
        n = Math.min(q * ((bl ? 9 : 0) + k + r + 13), scaledWindowWidth - 50) / q;
        int s = scaledWindowWidth / 2 - (n * q + (q - 1) * 5) / 2;
        int t = 10;
        int u = n * q + (q - 1) * 5;
        List<OrderedText> list3 = null;
        if (this.header != null) {
            list3 = this.client.textRenderer.wrapLines(this.header, scaledWindowWidth - 50);
            for (OrderedText orderedText : list3) {
                u = Math.max(u, this.client.textRenderer.getWidth(orderedText));
            }
        }
        List<OrderedText> list4 = null;
        if (this.footer != null) {
            list4 = this.client.textRenderer.wrapLines(this.footer, scaledWindowWidth - 50);
            for (OrderedText lv8 : list4) {
                u = Math.max(u, this.client.textRenderer.getWidth(lv8));
            }
        }
        if (list3 != null) {
            context.fill(scaledWindowWidth / 2 - u / 2 - 1, t - 1, scaledWindowWidth / 2 + u / 2 + 1, t + list3.size() * this.client.textRenderer.fontHeight, Integer.MIN_VALUE);
            for (OrderedText lv8 : list3) {
                v = this.client.textRenderer.getWidth(lv8);
                context.drawTextWithShadow(this.client.textRenderer, lv8, scaledWindowWidth / 2 - v / 2, t, -1);
                t += this.client.textRenderer.fontHeight;
            }
            ++t;
        }
        context.fill(scaledWindowWidth / 2 - u / 2 - 1, t - 1, scaledWindowWidth / 2 + u / 2 + 1, t + p * 9, Integer.MIN_VALUE);
        int n2 = this.client.options.getTextBackgroundColor(0x20FFFFFF);
        for (int x = 0; x < o; ++x) {
            int ab;
            int ac;
            v = x / p;
            y = x % p;
            int z = s + v * n + v * 5;
            int aa = t + y * 9;
            context.fill(z, aa, z + n, aa + 8, n2);
            RenderSystem.enableBlend();
            if (x >= list.size()) continue;
            PlayerListEntry lv9 = list.get(x);
            ScoreDisplayEntry lv10 = (ScoreDisplayEntry)list2.get(x);
            GameProfile gameProfile = lv9.getProfile();
            if (bl) {
                PlayerEntity lv11 = this.client.world.getPlayerByUuid(gameProfile.getId());
                boolean bl22 = lv11 != null && LivingEntityRenderer.shouldFlipUpsideDown(lv11);
                boolean bl3 = lv11 != null && lv11.isPartVisible(PlayerModelPart.HAT);
                PlayerSkinDrawer.draw(context, lv9.getSkinTextures().texture(), z, aa, 8, bl3, bl22);
                z += 9;
            }
            context.drawTextWithShadow(this.client.textRenderer, lv10.name, z, aa, lv9.getGameMode() == GameMode.SPECTATOR ? -1862270977 : Colors.WHITE);
            if (objective != null && lv9.getGameMode() != GameMode.SPECTATOR && (ac = (ab = z + k + 1) + r) - ab > 5) {
                this.renderScoreboardObjective(objective, aa, lv10, ab, ac, gameProfile.getId(), context);
            }
            this.renderLatencyIcon(context, n, z - (bl ? 9 : 0), aa, lv9);
        }
        if (list4 != null) {
            context.fill(scaledWindowWidth / 2 - u / 2 - 1, (t += p * 9 + 1) - 1, scaledWindowWidth / 2 + u / 2 + 1, t + list4.size() * this.client.textRenderer.fontHeight, Integer.MIN_VALUE);
            for (OrderedText lv12 : list4) {
                y = this.client.textRenderer.getWidth(lv12);
                context.drawTextWithShadow(this.client.textRenderer, lv12, scaledWindowWidth / 2 - y / 2, t, -1);
                t += this.client.textRenderer.fontHeight;
            }
        }
    }

    protected void renderLatencyIcon(DrawContext context, int width, int x, int y, PlayerListEntry entry) {
        Identifier lv = entry.getLatency() < 0 ? PING_UNKNOWN_ICON_TEXTURE : (entry.getLatency() < 150 ? PING_5_ICON_TEXTURE : (entry.getLatency() < 300 ? PING_4_ICON_TEXTURE : (entry.getLatency() < 600 ? PING_3_ICON_TEXTURE : (entry.getLatency() < 1000 ? PING_2_ICON_TEXTURE : PING_1_ICON_TEXTURE))));
        context.getMatrices().push();
        context.getMatrices().translate(0.0f, 0.0f, 100.0f);
        context.drawGuiTexture(lv, x + width - 11, y, 10, 8);
        context.getMatrices().pop();
    }

    private void renderScoreboardObjective(ScoreboardObjective objective, int y, ScoreDisplayEntry scoreDisplayEntry, int left, int right, UUID uuid, DrawContext context) {
        if (objective.getRenderType() == ScoreboardCriterion.RenderType.HEARTS) {
            this.renderHearts(y, left, right, uuid, context, scoreDisplayEntry.score);
        } else if (scoreDisplayEntry.formattedScore != null) {
            context.drawTextWithShadow(this.client.textRenderer, scoreDisplayEntry.formattedScore, right - scoreDisplayEntry.scoreWidth, y, 0xFFFFFF);
        }
    }

    private void renderHearts(int y, int left, int right, UUID uuid, DrawContext context, int score) {
        int p;
        Heart lv = this.hearts.computeIfAbsent(uuid, uuid2 -> new Heart(score));
        lv.tick(score, this.inGameHud.getTicks());
        int m = MathHelper.ceilDiv(Math.max(score, lv.getPrevScore()), 2);
        int n = Math.max(score, Math.max(lv.getPrevScore(), 20)) / 2;
        boolean bl = lv.useHighlighted(this.inGameHud.getTicks());
        if (m <= 0) {
            return;
        }
        int o = MathHelper.floor(Math.min((float)(right - left - 4) / (float)n, 9.0f));
        if (o <= 3) {
            float f = MathHelper.clamp((float)score / 20.0f, 0.0f, 1.0f);
            int p2 = (int)((1.0f - f) * 255.0f) << 16 | (int)(f * 255.0f) << 8;
            float g = (float)score / 2.0f;
            MutableText lv2 = Text.translatable("multiplayer.player.list.hp", Float.valueOf(g));
            MutableText lv3 = right - this.client.textRenderer.getWidth(lv2) >= left ? lv2 : Text.literal(Float.toString(g));
            context.drawTextWithShadow(this.client.textRenderer, lv3, (right + left - this.client.textRenderer.getWidth(lv3)) / 2, y, p2);
            return;
        }
        Identifier lv4 = bl ? CONTAINER_HEART_BLINKING_TEXTURE : CONTAINER_HEART_TEXTURE;
        for (p = m; p < n; ++p) {
            context.drawGuiTexture(lv4, left + p * o, y, 9, 9);
        }
        for (p = 0; p < m; ++p) {
            context.drawGuiTexture(lv4, left + p * o, y, 9, 9);
            if (bl) {
                if (p * 2 + 1 < lv.getPrevScore()) {
                    context.drawGuiTexture(FULL_HEART_BLINKING_TEXTURE, left + p * o, y, 9, 9);
                }
                if (p * 2 + 1 == lv.getPrevScore()) {
                    context.drawGuiTexture(HALF_HEART_BLINKING_TEXTURE, left + p * o, y, 9, 9);
                }
            }
            if (p * 2 + 1 < score) {
                context.drawGuiTexture(p >= 10 ? ABSORBING_FULL_HEART_BLINKING_TEXTURE : FULL_HEART_TEXTURE, left + p * o, y, 9, 9);
            }
            if (p * 2 + 1 != score) continue;
            context.drawGuiTexture(p >= 10 ? ABSORBING_HALF_HEART_BLINKING_TEXTURE : HALF_HEART_TEXTURE, left + p * o, y, 9, 9);
        }
    }

    public void setFooter(@Nullable Text footer) {
        this.footer = footer;
    }

    public void setHeader(@Nullable Text header) {
        this.header = header;
    }

    public void clear() {
        this.header = null;
        this.footer = null;
    }

    @Environment(value=EnvType.CLIENT)
    record ScoreDisplayEntry(Text name, int score, @Nullable Text formattedScore, int scoreWidth) {
        @Nullable
        public Text formattedScore() {
            return this.formattedScore;
        }
    }

    @Environment(value=EnvType.CLIENT)
    static class Heart {
        private static final long COOLDOWN_TICKS = 20L;
        private static final long SCORE_DECREASE_HIGHLIGHT_TICKS = 20L;
        private static final long SCORE_INCREASE_HIGHLIGHT_TICKS = 10L;
        private int score;
        private int prevScore;
        private long lastScoreChangeTick;
        private long highlightEndTick;

        public Heart(int score) {
            this.prevScore = score;
            this.score = score;
        }

        public void tick(int score, long currentTick) {
            if (score != this.score) {
                long m = score < this.score ? 20L : 10L;
                this.highlightEndTick = currentTick + m;
                this.score = score;
                this.lastScoreChangeTick = currentTick;
            }
            if (currentTick - this.lastScoreChangeTick > 20L) {
                this.prevScore = score;
            }
        }

        public int getPrevScore() {
            return this.prevScore;
        }

        public boolean useHighlighted(long currentTick) {
            return this.highlightEndTick > currentTick && (this.highlightEndTick - currentTick) % 6L >= 3L;
        }
    }
}

