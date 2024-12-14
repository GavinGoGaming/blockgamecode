/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 * 
 * Could not load the following classes:
 *  net.fabricmc.api.EnvType
 *  net.fabricmc.api.Environment
 *  org.jetbrains.annotations.Nullable
 */
package net.minecraft.client.gui.hud;

import com.google.common.collect.Lists;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.sound.SoundInstance;
import net.minecraft.client.sound.SoundInstanceListener;
import net.minecraft.client.sound.SoundListenerTransform;
import net.minecraft.client.sound.SoundManager;
import net.minecraft.client.sound.WeightedSoundSet;
import net.minecraft.text.Text;
import net.minecraft.util.Colors;
import net.minecraft.util.Util;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import org.jetbrains.annotations.Nullable;

@Environment(value=EnvType.CLIENT)
public class SubtitlesHud
implements SoundInstanceListener {
    private static final long REMOVE_DELAY = 3000L;
    private final MinecraftClient client;
    private final List<SubtitleEntry> entries = Lists.newArrayList();
    private boolean enabled;
    private final List<SubtitleEntry> audibleEntries = new ArrayList<SubtitleEntry>();

    public SubtitlesHud(MinecraftClient client) {
        this.client = client;
    }

    public void render(DrawContext context) {
        SoundManager lv = this.client.getSoundManager();
        if (!this.enabled && this.client.options.getShowSubtitles().getValue().booleanValue()) {
            lv.registerListener(this);
            this.enabled = true;
        } else if (this.enabled && !this.client.options.getShowSubtitles().getValue().booleanValue()) {
            lv.unregisterListener(this);
            this.enabled = false;
        }
        if (!this.enabled) {
            return;
        }
        SoundListenerTransform lv2 = lv.getListenerTransform();
        Vec3d lv3 = lv2.position();
        Vec3d lv4 = lv2.forward();
        Vec3d lv5 = lv2.right();
        this.audibleEntries.clear();
        for (SubtitleEntry lv6 : this.entries) {
            if (!lv6.canHearFrom(lv3)) continue;
            this.audibleEntries.add(lv6);
        }
        if (this.audibleEntries.isEmpty()) {
            return;
        }
        int i = 0;
        int j = 0;
        double d = this.client.options.getNotificationDisplayTime().getValue();
        Iterator<SubtitleEntry> iterator = this.audibleEntries.iterator();
        while (iterator.hasNext()) {
            SubtitleEntry lv7 = iterator.next();
            lv7.removeExpired(3000.0 * d);
            if (!lv7.hasSounds()) {
                iterator.remove();
                continue;
            }
            j = Math.max(j, this.client.textRenderer.getWidth(lv7.getText()));
        }
        j += this.client.textRenderer.getWidth("<") + this.client.textRenderer.getWidth(" ") + this.client.textRenderer.getWidth(">") + this.client.textRenderer.getWidth(" ");
        for (SubtitleEntry lv7 : this.audibleEntries) {
            int k = 255;
            Text lv8 = lv7.getText();
            SoundEntry lv9 = lv7.getNearestSound(lv3);
            if (lv9 == null) continue;
            Vec3d lv10 = lv9.location.subtract(lv3).normalize();
            double e = lv5.dotProduct(lv10);
            double f = lv4.dotProduct(lv10);
            boolean bl = f > 0.5;
            int l = j / 2;
            int m = this.client.textRenderer.fontHeight;
            int n = m / 2;
            float g = 1.0f;
            int o = this.client.textRenderer.getWidth(lv8);
            int p = MathHelper.floor(MathHelper.clampedLerp(255.0f, 75.0f, (float)(Util.getMeasuringTimeMs() - lv9.time) / (float)(3000.0 * d)));
            int q = p << 16 | p << 8 | p;
            context.getMatrices().push();
            context.getMatrices().translate((float)context.getScaledWindowWidth() - (float)l * 1.0f - 2.0f, (float)(context.getScaledWindowHeight() - 35) - (float)(i * (m + 1)) * 1.0f, 0.0f);
            context.getMatrices().scale(1.0f, 1.0f, 1.0f);
            context.fill(-l - 1, -n - 1, l + 1, n + 1, this.client.options.getTextBackgroundColor(0.8f));
            int r = q + Colors.BLACK;
            if (!bl) {
                if (e > 0.0) {
                    context.drawTextWithShadow(this.client.textRenderer, ">", l - this.client.textRenderer.getWidth(">"), -n, r);
                } else if (e < 0.0) {
                    context.drawTextWithShadow(this.client.textRenderer, "<", -l, -n, r);
                }
            }
            context.drawTextWithShadow(this.client.textRenderer, lv8, -o / 2, -n, r);
            context.getMatrices().pop();
            ++i;
        }
    }

    @Override
    public void onSoundPlayed(SoundInstance sound, WeightedSoundSet soundSet, float range) {
        if (soundSet.getSubtitle() == null) {
            return;
        }
        Text lv = soundSet.getSubtitle();
        if (!this.entries.isEmpty()) {
            for (SubtitleEntry lv2 : this.entries) {
                if (!lv2.getText().equals(lv)) continue;
                lv2.reset(new Vec3d(sound.getX(), sound.getY(), sound.getZ()));
                return;
            }
        }
        this.entries.add(new SubtitleEntry(lv, range, new Vec3d(sound.getX(), sound.getY(), sound.getZ())));
    }

    @Environment(value=EnvType.CLIENT)
    static class SubtitleEntry {
        private final Text text;
        private final float range;
        private final List<SoundEntry> sounds = new ArrayList<SoundEntry>();

        public SubtitleEntry(Text text, float range, Vec3d pos) {
            this.text = text;
            this.range = range;
            this.sounds.add(new SoundEntry(pos, Util.getMeasuringTimeMs()));
        }

        public Text getText() {
            return this.text;
        }

        @Nullable
        public SoundEntry getNearestSound(Vec3d pos) {
            if (this.sounds.isEmpty()) {
                return null;
            }
            if (this.sounds.size() == 1) {
                return this.sounds.getFirst();
            }
            return this.sounds.stream().min(Comparator.comparingDouble(soundPos -> soundPos.location().distanceTo(pos))).orElse(null);
        }

        public void reset(Vec3d pos) {
            this.sounds.removeIf(sound -> pos.equals(sound.location()));
            this.sounds.add(new SoundEntry(pos, Util.getMeasuringTimeMs()));
        }

        public boolean canHearFrom(Vec3d pos) {
            if (Float.isInfinite(this.range)) {
                return true;
            }
            if (this.sounds.isEmpty()) {
                return false;
            }
            SoundEntry lv = this.getNearestSound(pos);
            if (lv == null) {
                return false;
            }
            return pos.isInRange(lv.location, this.range);
        }

        public void removeExpired(double expiry) {
            long l = Util.getMeasuringTimeMs();
            this.sounds.removeIf(sound -> (double)(l - sound.time()) > expiry);
        }

        public boolean hasSounds() {
            return !this.sounds.isEmpty();
        }
    }

    @Environment(value=EnvType.CLIENT)
    record SoundEntry(Vec3d location, long time) {
    }
}

