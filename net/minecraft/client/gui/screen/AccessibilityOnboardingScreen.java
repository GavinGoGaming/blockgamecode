/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 * 
 * Could not load the following classes:
 *  net.fabricmc.api.EnvType
 *  net.fabricmc.api.Environment
 *  org.jetbrains.annotations.Nullable
 */
package net.minecraft.client.gui.screen;

import com.mojang.text2speech.Narrator;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.LogoDrawer;
import net.minecraft.client.gui.screen.AccessibilityOnboardingButtons;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.option.AccessibilityOptionsScreen;
import net.minecraft.client.gui.screen.option.LanguageOptionsScreen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.ClickableWidget;
import net.minecraft.client.gui.widget.CyclingButtonWidget;
import net.minecraft.client.gui.widget.DirectionalLayoutWidget;
import net.minecraft.client.gui.widget.NarratedMultilineTextWidget;
import net.minecraft.client.gui.widget.ThreePartsLayoutWidget;
import net.minecraft.client.option.GameOptions;
import net.minecraft.screen.ScreenTexts;
import net.minecraft.text.Text;
import org.jetbrains.annotations.Nullable;

@Environment(value=EnvType.CLIENT)
public class AccessibilityOnboardingScreen
extends Screen {
    private static final Text TITLE_TEXT = Text.translatable("accessibility.onboarding.screen.title");
    private static final Text NARRATOR_PROMPT = Text.translatable("accessibility.onboarding.screen.narrator");
    private static final int field_41838 = 4;
    private static final int field_41839 = 16;
    private final LogoDrawer logoDrawer;
    private final GameOptions gameOptions;
    private final boolean isNarratorUsable;
    private boolean narratorPrompted;
    private float narratorPromptTimer;
    private final Runnable onClose;
    @Nullable
    private NarratedMultilineTextWidget textWidget;
    private final ThreePartsLayoutWidget layout = new ThreePartsLayoutWidget(this, this.yMargin(), 33);

    public AccessibilityOnboardingScreen(GameOptions gameOptions, Runnable onClose) {
        super(TITLE_TEXT);
        this.gameOptions = gameOptions;
        this.onClose = onClose;
        this.logoDrawer = new LogoDrawer(true);
        this.isNarratorUsable = MinecraftClient.getInstance().getNarratorManager().isActive();
    }

    @Override
    public void init() {
        DirectionalLayoutWidget lv = this.layout.addBody(DirectionalLayoutWidget.vertical());
        lv.getMainPositioner().alignHorizontalCenter().margin(4);
        this.textWidget = lv.add(new NarratedMultilineTextWidget(this.width, this.title, this.textRenderer), positioner -> positioner.margin(8));
        ClickableWidget clickableWidget = this.gameOptions.getNarrator().createWidget(this.gameOptions);
        if (clickableWidget instanceof CyclingButtonWidget) {
            CyclingButtonWidget lv2;
            this.narratorToggleButton = lv2 = (CyclingButtonWidget)clickableWidget;
            this.narratorToggleButton.active = this.isNarratorUsable;
            lv.add(this.narratorToggleButton);
        }
        lv.add(AccessibilityOnboardingButtons.createAccessibilityButton(150, button -> this.setScreen(new AccessibilityOptionsScreen(this, this.client.options)), false));
        lv.add(AccessibilityOnboardingButtons.createLanguageButton(150, button -> this.setScreen(new LanguageOptionsScreen((Screen)this, this.client.options, this.client.getLanguageManager())), false));
        this.layout.addFooter(ButtonWidget.builder(ScreenTexts.CONTINUE, button -> this.close()).build());
        this.layout.forEachChild(this::addDrawableChild);
        this.initTabNavigation();
    }

    @Override
    protected void initTabNavigation() {
        if (this.textWidget != null) {
            this.textWidget.initMaxWidth(this.width);
        }
        this.layout.refreshPositions();
    }

    @Override
    protected void setInitialFocus() {
        if (this.isNarratorUsable && this.narratorToggleButton != null) {
            this.setInitialFocus(this.narratorToggleButton);
        } else {
            super.setInitialFocus();
        }
    }

    private int yMargin() {
        return 90;
    }

    @Override
    public void close() {
        this.saveAndRun(true, this.onClose);
    }

    private void setScreen(Screen screen) {
        this.saveAndRun(false, () -> this.client.setScreen(screen));
    }

    private void saveAndRun(boolean dontShowAgain, Runnable callback) {
        if (dontShowAgain) {
            this.gameOptions.setAccessibilityOnboarded();
        }
        Narrator.getNarrator().clear();
        callback.run();
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        super.render(context, mouseX, mouseY, delta);
        this.tickNarratorPrompt();
        this.logoDrawer.draw(context, this.width, 1.0f);
    }

    @Override
    protected void renderPanoramaBackground(DrawContext context, float delta) {
        ROTATING_PANORAMA_RENDERER.render(context, this.width, this.height, 1.0f, 0.0f);
    }

    private void tickNarratorPrompt() {
        if (!this.narratorPrompted && this.isNarratorUsable) {
            if (this.narratorPromptTimer < 40.0f) {
                this.narratorPromptTimer += 1.0f;
            } else if (this.client.isWindowFocused()) {
                Narrator.getNarrator().say(NARRATOR_PROMPT.getString(), true);
                this.narratorPrompted = true;
            }
        }
    }
}

