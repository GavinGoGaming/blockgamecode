/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 * 
 * Could not load the following classes:
 *  net.fabricmc.api.EnvType
 *  net.fabricmc.api.Environment
 */
package net.minecraft.client.gui.screen.option;

import java.util.ArrayList;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.option.GameOptionsScreen;
import net.minecraft.client.gui.widget.ClickableWidget;
import net.minecraft.client.gui.widget.CyclingButtonWidget;
import net.minecraft.client.option.GameOptions;
import net.minecraft.entity.player.PlayerModelPart;
import net.minecraft.text.Text;

@Environment(value=EnvType.CLIENT)
public class SkinOptionsScreen
extends GameOptionsScreen {
    private static final Text TITLE_TEXT = Text.translatable("options.skinCustomisation.title");

    public SkinOptionsScreen(Screen parent, GameOptions gameOptions) {
        super(parent, gameOptions, TITLE_TEXT);
    }

    @Override
    protected void addOptions() {
        ArrayList<ClickableWidget> list = new ArrayList<ClickableWidget>();
        for (PlayerModelPart lv : PlayerModelPart.values()) {
            list.add(CyclingButtonWidget.onOffBuilder(this.gameOptions.isPlayerModelPartEnabled(lv)).build(lv.getOptionName(), (arg2, boolean_) -> this.gameOptions.togglePlayerModelPart(lv, (boolean)boolean_)));
        }
        list.add(this.gameOptions.getMainArm().createWidget(this.gameOptions));
        this.body.addAll(list);
    }
}

