/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 * 
 * Could not load the following classes:
 *  net.fabricmc.api.EnvType
 *  net.fabricmc.api.Environment
 *  org.jetbrains.annotations.Nullable
 */
package net.minecraft.client.gui.screen.option;

import java.util.function.Supplier;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.ConfirmScreen;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.option.AccessibilityOptionsScreen;
import net.minecraft.client.gui.screen.option.ChatOptionsScreen;
import net.minecraft.client.gui.screen.option.ControlsOptionsScreen;
import net.minecraft.client.gui.screen.option.CreditsAndAttributionScreen;
import net.minecraft.client.gui.screen.option.LanguageOptionsScreen;
import net.minecraft.client.gui.screen.option.OnlineOptionsScreen;
import net.minecraft.client.gui.screen.option.SkinOptionsScreen;
import net.minecraft.client.gui.screen.option.SoundOptionsScreen;
import net.minecraft.client.gui.screen.option.TelemetryInfoScreen;
import net.minecraft.client.gui.screen.option.VideoOptionsScreen;
import net.minecraft.client.gui.screen.pack.PackScreen;
import net.minecraft.client.gui.tooltip.Tooltip;
import net.minecraft.client.gui.widget.AxisGridWidget;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.ClickableWidget;
import net.minecraft.client.gui.widget.CyclingButtonWidget;
import net.minecraft.client.gui.widget.DirectionalLayoutWidget;
import net.minecraft.client.gui.widget.GridWidget;
import net.minecraft.client.gui.widget.LockButtonWidget;
import net.minecraft.client.gui.widget.Positioner;
import net.minecraft.client.gui.widget.TextWidget;
import net.minecraft.client.gui.widget.ThreePartsLayoutWidget;
import net.minecraft.client.gui.widget.Widget;
import net.minecraft.client.option.GameOptions;
import net.minecraft.network.packet.c2s.play.UpdateDifficultyC2SPacket;
import net.minecraft.network.packet.c2s.play.UpdateDifficultyLockC2SPacket;
import net.minecraft.resource.ResourcePackManager;
import net.minecraft.screen.ScreenTexts;
import net.minecraft.text.Text;
import net.minecraft.world.Difficulty;
import org.jetbrains.annotations.Nullable;

@Environment(value=EnvType.CLIENT)
public class OptionsScreen
extends Screen {
    private static final Text TITLE_TEXT = Text.translatable("options.title");
    private static final Text SKIN_CUSTOMIZATION_TEXT = Text.translatable("options.skinCustomisation");
    private static final Text SOUNDS_TEXT = Text.translatable("options.sounds");
    private static final Text VIDEO_TEXT = Text.translatable("options.video");
    private static final Text CONTROL_TEXT = Text.translatable("options.controls");
    private static final Text LANGUAGE_TEXT = Text.translatable("options.language");
    private static final Text CHAT_TEXT = Text.translatable("options.chat");
    private static final Text RESOURCE_PACK_TEXT = Text.translatable("options.resourcepack");
    private static final Text ACCESSIBILITY_TEXT = Text.translatable("options.accessibility");
    private static final Text TELEMETRY_TEXT = Text.translatable("options.telemetry");
    private static final Tooltip TELEMETRY_DISABLED_TOOLTIP = Tooltip.of(Text.translatable("options.telemetry.disabled"));
    private static final Text CREDITS_AND_ATTRIBUTION_TEXT = Text.translatable("options.credits_and_attribution");
    private static final int COLUMNS = 2;
    private final ThreePartsLayoutWidget layout = new ThreePartsLayoutWidget(this, 61, 33);
    private final Screen parent;
    private final GameOptions settings;
    @Nullable
    private CyclingButtonWidget<Difficulty> difficultyButton;
    @Nullable
    private LockButtonWidget lockDifficultyButton;

    public OptionsScreen(Screen parent, GameOptions gameOptions) {
        super(TITLE_TEXT);
        this.parent = parent;
        this.settings = gameOptions;
    }

    @Override
    protected void init() {
        DirectionalLayoutWidget lv = this.layout.addHeader(DirectionalLayoutWidget.vertical().spacing(8));
        lv.add(new TextWidget(TITLE_TEXT, this.textRenderer), Positioner::alignHorizontalCenter);
        DirectionalLayoutWidget lv2 = lv.add(DirectionalLayoutWidget.horizontal()).spacing(8);
        lv2.add(this.settings.getFov().createWidget(this.client.options));
        lv2.add(this.createTopRightButton());
        GridWidget lv3 = new GridWidget();
        lv3.getMainPositioner().marginX(4).marginBottom(4).alignHorizontalCenter();
        GridWidget.Adder lv4 = lv3.createAdder(2);
        lv4.add(this.createButton(SKIN_CUSTOMIZATION_TEXT, () -> new SkinOptionsScreen(this, this.settings)));
        lv4.add(this.createButton(SOUNDS_TEXT, () -> new SoundOptionsScreen(this, this.settings)));
        lv4.add(this.createButton(VIDEO_TEXT, () -> new VideoOptionsScreen((Screen)this, this.client, this.settings)));
        lv4.add(this.createButton(CONTROL_TEXT, () -> new ControlsOptionsScreen(this, this.settings)));
        lv4.add(this.createButton(LANGUAGE_TEXT, () -> new LanguageOptionsScreen((Screen)this, this.settings, this.client.getLanguageManager())));
        lv4.add(this.createButton(CHAT_TEXT, () -> new ChatOptionsScreen(this, this.settings)));
        lv4.add(this.createButton(RESOURCE_PACK_TEXT, () -> new PackScreen(this.client.getResourcePackManager(), this::refreshResourcePacks, this.client.getResourcePackDir(), Text.translatable("resourcePack.title"))));
        lv4.add(this.createButton(ACCESSIBILITY_TEXT, () -> new AccessibilityOptionsScreen(this, this.settings)));
        ButtonWidget lv5 = lv4.add(this.createButton(TELEMETRY_TEXT, () -> new TelemetryInfoScreen(this, this.settings)));
        if (!this.client.isTelemetryEnabledByApi()) {
            lv5.active = false;
            lv5.setTooltip(TELEMETRY_DISABLED_TOOLTIP);
        }
        lv4.add(this.createButton(CREDITS_AND_ATTRIBUTION_TEXT, () -> new CreditsAndAttributionScreen(this)));
        this.layout.addBody(lv3);
        this.layout.addFooter(ButtonWidget.builder(ScreenTexts.DONE, button -> this.close()).width(200).build());
        this.layout.forEachChild(arg2 -> {
            ClickableWidget cfr_ignored_0 = (ClickableWidget)this.addDrawableChild(arg2);
        });
        this.initTabNavigation();
    }

    @Override
    protected void initTabNavigation() {
        this.layout.refreshPositions();
    }

    @Override
    public void close() {
        this.client.setScreen(this.parent);
    }

    private void refreshResourcePacks(ResourcePackManager resourcePackManager) {
        this.settings.refreshResourcePacks(resourcePackManager);
        this.client.setScreen(this);
    }

    private Widget createTopRightButton() {
        if (this.client.world != null && this.client.isIntegratedServerRunning()) {
            this.difficultyButton = OptionsScreen.createDifficultyButtonWidget(0, 0, "options.difficulty", this.client);
            if (!this.client.world.getLevelProperties().isHardcore()) {
                this.lockDifficultyButton = new LockButtonWidget(0, 0, button -> this.client.setScreen(new ConfirmScreen(this::lockDifficulty, Text.translatable("difficulty.lock.title"), Text.translatable("difficulty.lock.question", this.client.world.getLevelProperties().getDifficulty().getTranslatableName()))));
                this.difficultyButton.setWidth(this.difficultyButton.getWidth() - this.lockDifficultyButton.getWidth());
                this.lockDifficultyButton.setLocked(this.client.world.getLevelProperties().isDifficultyLocked());
                this.lockDifficultyButton.active = !this.lockDifficultyButton.isLocked();
                this.difficultyButton.active = !this.lockDifficultyButton.isLocked();
                AxisGridWidget lv = new AxisGridWidget(150, 0, AxisGridWidget.DisplayAxis.HORIZONTAL);
                lv.add(this.difficultyButton);
                lv.add(this.lockDifficultyButton);
                return lv;
            }
            this.difficultyButton.active = false;
            return this.difficultyButton;
        }
        return ButtonWidget.builder(Text.translatable("options.online"), button -> this.client.setScreen(new OnlineOptionsScreen(this, this.settings))).dimensions(this.width / 2 + 5, this.height / 6 - 12 + 24, 150, 20).build();
    }

    public static CyclingButtonWidget<Difficulty> createDifficultyButtonWidget(int x, int y, String translationKey, MinecraftClient client) {
        return CyclingButtonWidget.builder(Difficulty::getTranslatableName).values((Difficulty[])Difficulty.values()).initially(client.world.getDifficulty()).build(x, y, 150, 20, Text.translatable(translationKey), (button, difficulty) -> client.getNetworkHandler().sendPacket(new UpdateDifficultyC2SPacket((Difficulty)difficulty)));
    }

    private void lockDifficulty(boolean difficultyLocked) {
        this.client.setScreen(this);
        if (difficultyLocked && this.client.world != null && this.lockDifficultyButton != null && this.difficultyButton != null) {
            this.client.getNetworkHandler().sendPacket(new UpdateDifficultyLockC2SPacket(true));
            this.lockDifficultyButton.setLocked(true);
            this.lockDifficultyButton.active = false;
            this.difficultyButton.active = false;
        }
    }

    @Override
    public void removed() {
        this.settings.write();
    }

    private ButtonWidget createButton(Text message, Supplier<Screen> screenSupplier) {
        return ButtonWidget.builder(message, button -> this.client.setScreen((Screen)screenSupplier.get())).build();
    }
}

