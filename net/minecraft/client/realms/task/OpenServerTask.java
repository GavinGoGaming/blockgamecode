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
import net.minecraft.client.realms.RealmsClient;
import net.minecraft.client.realms.dto.RealmsServer;
import net.minecraft.client.realms.exception.RetryCallException;
import net.minecraft.client.realms.gui.screen.RealmsConfigureWorldScreen;
import net.minecraft.client.realms.gui.screen.RealmsMainScreen;
import net.minecraft.client.realms.task.LongRunningTask;
import net.minecraft.text.Text;
import org.slf4j.Logger;

@Environment(value=EnvType.CLIENT)
public class OpenServerTask
extends LongRunningTask {
    private static final Logger LOGGER = LogUtils.getLogger();
    private static final Text TITLE = Text.translatable("mco.configure.world.opening");
    private final RealmsServer serverData;
    private final Screen returnScreen;
    private final boolean join;
    private final MinecraftClient client;

    public OpenServerTask(RealmsServer realmsServer, Screen returnScreen, boolean join, MinecraftClient client) {
        this.serverData = realmsServer;
        this.returnScreen = returnScreen;
        this.join = join;
        this.client = client;
    }

    @Override
    public void run() {
        RealmsClient lv = RealmsClient.create();
        for (int i = 0; i < 25; ++i) {
            if (this.aborted()) {
                return;
            }
            try {
                boolean bl = lv.open(this.serverData.id);
                if (!bl) continue;
                this.client.execute(() -> {
                    if (this.returnScreen instanceof RealmsConfigureWorldScreen) {
                        ((RealmsConfigureWorldScreen)this.returnScreen).stateChanged();
                    }
                    this.serverData.state = RealmsServer.State.OPEN;
                    if (this.join) {
                        RealmsMainScreen.play(this.serverData, this.returnScreen);
                    } else {
                        this.client.setScreen(this.returnScreen);
                    }
                });
                break;
            } catch (RetryCallException lv2) {
                if (this.aborted()) {
                    return;
                }
                OpenServerTask.pause(lv2.delaySeconds);
                continue;
            } catch (Exception exception) {
                if (this.aborted()) {
                    return;
                }
                LOGGER.error("Failed to open server", exception);
                this.error(exception);
            }
        }
    }

    @Override
    public Text getTitle() {
        return TITLE;
    }
}

