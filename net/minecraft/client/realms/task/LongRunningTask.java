/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 * 
 * Could not load the following classes:
 *  net.fabricmc.api.EnvType
 *  net.fabricmc.api.Environment
 */
package net.minecraft.client.realms.task;

import com.mojang.logging.LogUtils;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.TitleScreen;
import net.minecraft.client.realms.exception.RealmsServiceException;
import net.minecraft.client.realms.gui.screen.RealmsGenericErrorScreen;
import net.minecraft.client.realms.gui.screen.RealmsMainScreen;
import net.minecraft.text.Text;
import org.slf4j.Logger;

@Environment(value=EnvType.CLIENT)
public abstract class LongRunningTask
implements Runnable {
    protected static final int MAX_RETRIES = 25;
    private static final Logger LOGGER = LogUtils.getLogger();
    private boolean aborted = false;

    protected static void pause(long seconds) {
        try {
            Thread.sleep(seconds * 1000L);
        } catch (InterruptedException interruptedException) {
            Thread.currentThread().interrupt();
            LOGGER.error("", interruptedException);
        }
    }

    public static void setScreen(Screen screen) {
        MinecraftClient lv = MinecraftClient.getInstance();
        lv.execute(() -> lv.setScreen(screen));
    }

    protected void error(Text message) {
        this.abortTask();
        MinecraftClient lv = MinecraftClient.getInstance();
        lv.execute(() -> lv.setScreen(new RealmsGenericErrorScreen(message, (Screen)new RealmsMainScreen(new TitleScreen()))));
    }

    protected void error(Exception exception) {
        if (exception instanceof RealmsServiceException) {
            RealmsServiceException lv = (RealmsServiceException)exception;
            this.error(lv.error.getText());
        } else {
            this.error(Text.literal(exception.getMessage()));
        }
    }

    protected void error(RealmsServiceException exception) {
        this.error(exception.error.getText());
    }

    public abstract Text getTitle();

    public boolean aborted() {
        return this.aborted;
    }

    public void tick() {
    }

    public void init() {
    }

    public void abortTask() {
        this.aborted = true;
    }
}

