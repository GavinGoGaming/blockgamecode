/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 * 
 * Could not load the following classes:
 *  net.fabricmc.api.EnvType
 *  net.fabricmc.api.Environment
 *  org.jetbrains.annotations.Nullable
 */
package net.minecraft.client.gui.screen;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.font.MultilineText;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.LoadingDisplay;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.screen.ScreenTexts;
import net.minecraft.text.Text;
import net.minecraft.util.Util;
import org.jetbrains.annotations.Nullable;

@Environment(value=EnvType.CLIENT)
public class TaskScreen
extends Screen {
    private static final int TITLE_TEXT_Y = 80;
    private static final int DESCRIPTION_TEXT_Y = 120;
    private static final int DESCRIPTION_TEXT_WIDTH = 360;
    @Nullable
    private final Text descriptionText;
    private final Text closeButtonText;
    private final Runnable closeCallback;
    @Nullable
    private MultilineText description;
    private ButtonWidget button;
    private int buttonCooldown;

    public static TaskScreen createRunningScreen(Text title, Text closeButtonText, Runnable closeCallback) {
        return new TaskScreen(title, null, closeButtonText, closeCallback, 0);
    }

    public static TaskScreen createResultScreen(Text title, Text descriptionText, Text closeButtonText, Runnable closeCallback) {
        return new TaskScreen(title, descriptionText, closeButtonText, closeCallback, 20);
    }

    protected TaskScreen(Text title, @Nullable Text descriptionText, Text closeButtonText, Runnable closeCallback, int buttonCooldown) {
        super(title);
        this.descriptionText = descriptionText;
        this.closeButtonText = closeButtonText;
        this.closeCallback = closeCallback;
        this.buttonCooldown = buttonCooldown;
    }

    @Override
    protected void init() {
        super.init();
        if (this.descriptionText != null) {
            this.description = MultilineText.create(this.textRenderer, this.descriptionText, 360);
        }
        int i = 150;
        int j = 20;
        int k = this.description != null ? this.description.count() : 1;
        int l = Math.max(k, 5) * this.textRenderer.fontHeight;
        int m = Math.min(120 + l, this.height - 40);
        this.button = this.addDrawableChild(ButtonWidget.builder(this.closeButtonText, arg -> this.close()).dimensions((this.width - 150) / 2, m, 150, 20).build());
    }

    @Override
    public void tick() {
        if (this.buttonCooldown > 0) {
            --this.buttonCooldown;
        }
        this.button.active = this.buttonCooldown == 0;
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        super.render(context, mouseX, mouseY, delta);
        context.drawCenteredTextWithShadow(this.textRenderer, this.title, this.width / 2, 80, 0xFFFFFF);
        if (this.description == null) {
            String string = LoadingDisplay.get(Util.getMeasuringTimeMs());
            context.drawCenteredTextWithShadow(this.textRenderer, string, this.width / 2, 120, 0xA0A0A0);
        } else {
            this.description.drawCenterWithShadow(context, this.width / 2, 120);
        }
    }

    @Override
    public boolean shouldCloseOnEsc() {
        return this.description != null && this.button.active;
    }

    @Override
    public void close() {
        this.closeCallback.run();
    }

    @Override
    public Text getNarratedTitle() {
        return ScreenTexts.joinSentences(this.title, this.descriptionText != null ? this.descriptionText : ScreenTexts.EMPTY);
    }
}

