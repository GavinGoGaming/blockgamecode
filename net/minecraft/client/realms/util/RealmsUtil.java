/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 * 
 * Could not load the following classes:
 *  net.fabricmc.api.EnvType
 *  net.fabricmc.api.Environment
 */
package net.minecraft.client.realms.util;

import com.mojang.authlib.yggdrasil.ProfileResult;
import java.util.Date;
import java.util.UUID;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.PlayerSkinDrawer;
import net.minecraft.client.util.DefaultSkinHelper;
import net.minecraft.client.util.SkinTextures;
import net.minecraft.text.Text;

@Environment(value=EnvType.CLIENT)
public class RealmsUtil {
    private static final Text NOW_TEXT = Text.translatable("mco.util.time.now");
    private static final int SECONDS_PER_MINUTE = 60;
    private static final int SECONDS_PER_HOUR = 3600;
    private static final int SECONDS_PER_DAY = 86400;

    public static Text convertToAgePresentation(long milliseconds) {
        if (milliseconds < 0L) {
            return NOW_TEXT;
        }
        long m = milliseconds / 1000L;
        if (m < 60L) {
            return Text.translatable("mco.time.secondsAgo", m);
        }
        if (m < 3600L) {
            long n = m / 60L;
            return Text.translatable("mco.time.minutesAgo", n);
        }
        if (m < 86400L) {
            long n = m / 3600L;
            return Text.translatable("mco.time.hoursAgo", n);
        }
        long n = m / 86400L;
        return Text.translatable("mco.time.daysAgo", n);
    }

    public static Text convertToAgePresentation(Date date) {
        return RealmsUtil.convertToAgePresentation(System.currentTimeMillis() - date.getTime());
    }

    public static void drawPlayerHead(DrawContext context, int x, int y, int size, UUID playerUuid) {
        MinecraftClient lv = MinecraftClient.getInstance();
        ProfileResult profileResult = lv.getSessionService().fetchProfile(playerUuid, false);
        SkinTextures lv2 = profileResult != null ? lv.getSkinProvider().getSkinTextures(profileResult.profile()) : DefaultSkinHelper.getSkinTextures(playerUuid);
        PlayerSkinDrawer.draw(context, lv2.texture(), x, y, size);
    }
}

