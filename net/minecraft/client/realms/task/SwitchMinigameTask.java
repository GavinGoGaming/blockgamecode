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
import net.minecraft.client.realms.RealmsClient;
import net.minecraft.client.realms.dto.WorldTemplate;
import net.minecraft.client.realms.exception.RetryCallException;
import net.minecraft.client.realms.gui.screen.RealmsConfigureWorldScreen;
import net.minecraft.client.realms.task.LongRunningTask;
import net.minecraft.text.Text;
import org.slf4j.Logger;

@Environment(value=EnvType.CLIENT)
public class SwitchMinigameTask
extends LongRunningTask {
    private static final Logger LOGGER = LogUtils.getLogger();
    private static final Text TITLE = Text.translatable("mco.minigame.world.starting.screen.title");
    private final long worldId;
    private final WorldTemplate worldTemplate;
    private final RealmsConfigureWorldScreen lastScreen;

    public SwitchMinigameTask(long worldId, WorldTemplate worldTemplate, RealmsConfigureWorldScreen lastScreen) {
        this.worldId = worldId;
        this.worldTemplate = worldTemplate;
        this.lastScreen = lastScreen;
    }

    @Override
    public void run() {
        RealmsClient lv = RealmsClient.create();
        for (int i = 0; i < 25; ++i) {
            try {
                if (this.aborted()) {
                    return;
                }
                if (!lv.putIntoMinigameMode(this.worldId, this.worldTemplate.id).booleanValue()) continue;
                SwitchMinigameTask.setScreen(this.lastScreen);
                break;
            } catch (RetryCallException lv2) {
                if (this.aborted()) {
                    return;
                }
                SwitchMinigameTask.pause(lv2.delaySeconds);
                continue;
            } catch (Exception exception) {
                if (this.aborted()) {
                    return;
                }
                LOGGER.error("Couldn't start mini game!");
                this.error(exception);
            }
        }
    }

    @Override
    public Text getTitle() {
        return TITLE;
    }
}

