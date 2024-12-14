/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 * 
 * Could not load the following classes:
 *  net.fabricmc.api.EnvType
 *  net.fabricmc.api.Environment
 *  org.jetbrains.annotations.Nullable
 */
package net.minecraft.client.realms.gui.screen;

import com.google.common.collect.Lists;
import com.mojang.logging.LogUtils;
import java.util.List;
import java.util.function.Supplier;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.realms.RealmsClient;
import net.minecraft.client.realms.dto.RealmsServer;
import net.minecraft.client.realms.dto.RealmsWorldOptions;
import net.minecraft.client.realms.dto.WorldTemplate;
import net.minecraft.client.realms.exception.RealmsServiceException;
import net.minecraft.client.realms.gui.RealmsPopups;
import net.minecraft.client.realms.gui.RealmsWorldSlotButton;
import net.minecraft.client.realms.gui.screen.RealmsBackupScreen;
import net.minecraft.client.realms.gui.screen.RealmsCreateWorldScreen;
import net.minecraft.client.realms.gui.screen.RealmsGenericErrorScreen;
import net.minecraft.client.realms.gui.screen.RealmsLongRunningMcoTaskScreen;
import net.minecraft.client.realms.gui.screen.RealmsMainScreen;
import net.minecraft.client.realms.gui.screen.RealmsPlayerScreen;
import net.minecraft.client.realms.gui.screen.RealmsScreen;
import net.minecraft.client.realms.gui.screen.RealmsSelectWorldTemplateScreen;
import net.minecraft.client.realms.gui.screen.RealmsSettingsScreen;
import net.minecraft.client.realms.gui.screen.RealmsSlotOptionsScreen;
import net.minecraft.client.realms.gui.screen.RealmsSubscriptionInfoScreen;
import net.minecraft.client.realms.task.CloseServerTask;
import net.minecraft.client.realms.task.OpenServerTask;
import net.minecraft.client.realms.task.SwitchMinigameTask;
import net.minecraft.client.realms.task.SwitchSlotTask;
import net.minecraft.screen.ScreenTexts;
import net.minecraft.text.Text;
import net.minecraft.util.Colors;
import net.minecraft.util.Identifier;
import net.minecraft.util.StringHelper;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;

@Environment(value=EnvType.CLIENT)
public class RealmsConfigureWorldScreen
extends RealmsScreen {
    private static final Identifier EXPIRED_STATUS_TEXTURE = Identifier.ofVanilla("realm_status/expired");
    private static final Identifier EXPIRES_SOON_STATUS_TEXTURE = Identifier.ofVanilla("realm_status/expires_soon");
    private static final Identifier OPEN_STATUS_TEXTURE = Identifier.ofVanilla("realm_status/open");
    private static final Identifier CLOSED_STATUS_TEXTURE = Identifier.ofVanilla("realm_status/closed");
    private static final Logger LOGGER = LogUtils.getLogger();
    private static final Text WORLDS_TITLE = Text.translatable("mco.configure.worlds.title");
    private static final Text CONFIGURE_REALM_TITLE = Text.translatable("mco.configure.world.title");
    private static final Text EXPIRED_TEXT = Text.translatable("mco.selectServer.expired");
    private static final Text EXPIRES_SOON_TEXT = Text.translatable("mco.selectServer.expires.soon");
    private static final Text EXPIRES_IN_A_DAY_TEXT = Text.translatable("mco.selectServer.expires.day");
    private static final Text OPEN_TEXT = Text.translatable("mco.selectServer.open");
    private static final Text CLOSED_TEXT = Text.translatable("mco.selectServer.closed");
    private static final int field_32121 = 80;
    private static final int field_32122 = 5;
    @Nullable
    private Text tooltip;
    private final RealmsMainScreen parent;
    @Nullable
    private RealmsServer server;
    private final long serverId;
    private int left_x;
    private int right_x;
    private ButtonWidget playersButton;
    private ButtonWidget settingsButton;
    private ButtonWidget subscriptionButton;
    private ButtonWidget optionsButton;
    private ButtonWidget backupButton;
    private ButtonWidget resetWorldButton;
    private ButtonWidget switchMinigameButton;
    private boolean stateChanged;
    private final List<RealmsWorldSlotButton> slotButtons = Lists.newArrayList();

    public RealmsConfigureWorldScreen(RealmsMainScreen parent, long serverId) {
        super(CONFIGURE_REALM_TITLE);
        this.parent = parent;
        this.serverId = serverId;
    }

    @Override
    public void init() {
        if (this.server == null) {
            this.fetchServerData(this.serverId);
        }
        this.left_x = this.width / 2 - 187;
        this.right_x = this.width / 2 + 190;
        this.playersButton = this.addDrawableChild(ButtonWidget.builder(Text.translatable("mco.configure.world.buttons.players"), button -> this.client.setScreen(new RealmsPlayerScreen(this, this.server))).dimensions(this.buttonCenter(0, 3), RealmsConfigureWorldScreen.row(0), 100, 20).build());
        this.settingsButton = this.addDrawableChild(ButtonWidget.builder(Text.translatable("mco.configure.world.buttons.settings"), button -> this.client.setScreen(new RealmsSettingsScreen(this, this.server.clone()))).dimensions(this.buttonCenter(1, 3), RealmsConfigureWorldScreen.row(0), 100, 20).build());
        this.subscriptionButton = this.addDrawableChild(ButtonWidget.builder(Text.translatable("mco.configure.world.buttons.subscription"), button -> this.client.setScreen(new RealmsSubscriptionInfoScreen(this, this.server.clone(), this.parent))).dimensions(this.buttonCenter(2, 3), RealmsConfigureWorldScreen.row(0), 100, 20).build());
        this.slotButtons.clear();
        for (int i = 1; i < 5; ++i) {
            this.slotButtons.add(this.addSlotButton(i));
        }
        this.switchMinigameButton = this.addDrawableChild(ButtonWidget.builder(Text.translatable("mco.configure.world.buttons.switchminigame"), button -> this.client.setScreen(new RealmsSelectWorldTemplateScreen(Text.translatable("mco.template.title.minigame"), this::switchMinigame, RealmsServer.WorldType.MINIGAME))).dimensions(this.buttonLeft(0), RealmsConfigureWorldScreen.row(13) - 5, 100, 20).build());
        this.optionsButton = this.addDrawableChild(ButtonWidget.builder(Text.translatable("mco.configure.world.buttons.options"), button -> this.client.setScreen(new RealmsSlotOptionsScreen(this, this.server.slots.get(this.server.activeSlot).clone(), this.server.worldType, this.server.activeSlot))).dimensions(this.buttonLeft(0), RealmsConfigureWorldScreen.row(13) - 5, 90, 20).build());
        this.backupButton = this.addDrawableChild(ButtonWidget.builder(Text.translatable("mco.configure.world.backup"), button -> this.client.setScreen(new RealmsBackupScreen(this, this.server.clone(), this.server.activeSlot))).dimensions(this.buttonLeft(1), RealmsConfigureWorldScreen.row(13) - 5, 90, 20).build());
        this.resetWorldButton = this.addDrawableChild(ButtonWidget.builder(Text.translatable("mco.configure.world.buttons.resetworld"), button -> this.client.setScreen(RealmsCreateWorldScreen.resetWorld(this, this.server.clone(), () -> this.client.execute(() -> this.client.setScreen(this.getNewScreen()))))).dimensions(this.buttonLeft(2), RealmsConfigureWorldScreen.row(13) - 5, 90, 20).build());
        this.addDrawableChild(ButtonWidget.builder(ScreenTexts.BACK, button -> this.close()).dimensions(this.right_x - 80 + 8, RealmsConfigureWorldScreen.row(13) - 5, 70, 20).build());
        this.backupButton.active = true;
        if (this.server == null) {
            this.hideMinigameButtons();
            this.hideRegularButtons();
            this.playersButton.active = false;
            this.settingsButton.active = false;
            this.subscriptionButton.active = false;
        } else {
            this.disableButtons();
            if (this.isMinigame()) {
                this.hideRegularButtons();
            } else {
                this.hideMinigameButtons();
            }
        }
    }

    private RealmsWorldSlotButton addSlotButton(int slotIndex) {
        int j = this.frame(slotIndex);
        int k = RealmsConfigureWorldScreen.row(5) + 5;
        RealmsWorldSlotButton lv = new RealmsWorldSlotButton(j, k, 80, 80, slotIndex, button -> {
            RealmsWorldSlotButton.State lv = ((RealmsWorldSlotButton)button).getState();
            if (lv != null) {
                switch (lv.action) {
                    case NOTHING: {
                        break;
                    }
                    case JOIN: {
                        this.joinRealm(this.server);
                        break;
                    }
                    case SWITCH_SLOT: {
                        if (lv.minigame) {
                            this.switchToMinigame();
                            break;
                        }
                        if (lv.empty) {
                            this.switchToEmptySlot(slotIndex, this.server);
                            break;
                        }
                        this.switchToFullSlot(slotIndex, this.server);
                        break;
                    }
                    default: {
                        throw new IllegalStateException("Unknown action " + String.valueOf((Object)lv.action));
                    }
                }
            }
        });
        if (this.server != null) {
            lv.setServer(this.server);
        }
        return this.addDrawableChild(lv);
    }

    private int buttonLeft(int i) {
        return this.left_x + i * 95;
    }

    private int buttonCenter(int i, int total) {
        return this.width / 2 - (total * 105 - 5) / 2 + i * 105;
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        String string2;
        super.render(context, mouseX, mouseY, delta);
        this.tooltip = null;
        context.drawCenteredTextWithShadow(this.textRenderer, WORLDS_TITLE, this.width / 2, RealmsConfigureWorldScreen.row(4), Colors.WHITE);
        if (this.server == null) {
            context.drawCenteredTextWithShadow(this.textRenderer, this.title, this.width / 2, 17, Colors.WHITE);
            return;
        }
        String string = this.server.getName();
        int k = this.textRenderer.getWidth(string);
        int l = this.server.state == RealmsServer.State.CLOSED ? Colors.LIGHT_GRAY : 0x7FFF7F;
        int m = this.textRenderer.getWidth(this.title);
        context.drawCenteredTextWithShadow(this.textRenderer, this.title, this.width / 2, 12, Colors.WHITE);
        context.drawCenteredTextWithShadow(this.textRenderer, string, this.width / 2, 24, l);
        int n = Math.min(this.buttonCenter(2, 3) + 80 - 11, this.width / 2 + k / 2 + m / 2 + 10);
        this.drawServerState(context, n, 7, mouseX, mouseY);
        if (this.isMinigame() && (string2 = this.server.getMinigameName()) != null) {
            context.drawText(this.textRenderer, Text.translatable("mco.configure.world.minigame", string2), this.left_x + 80 + 20 + 10, RealmsConfigureWorldScreen.row(13), Colors.WHITE, false);
        }
    }

    private int frame(int ordinal) {
        return this.left_x + (ordinal - 1) * 98;
    }

    @Override
    public void close() {
        this.client.setScreen(this.parent);
        if (this.stateChanged) {
            this.parent.removeSelection();
        }
    }

    private void fetchServerData(long worldId) {
        new Thread(() -> {
            RealmsClient lv = RealmsClient.create();
            try {
                RealmsServer lv2 = lv.getOwnWorld(worldId);
                this.client.execute(() -> {
                    this.server = lv2;
                    this.disableButtons();
                    if (this.isMinigame()) {
                        this.addButton(this.switchMinigameButton);
                    } else {
                        this.addButton(this.optionsButton);
                        this.addButton(this.backupButton);
                        this.addButton(this.resetWorldButton);
                    }
                    for (RealmsWorldSlotButton lv : this.slotButtons) {
                        lv.setServer(lv2);
                    }
                });
            } catch (RealmsServiceException lv3) {
                LOGGER.error("Couldn't get own world", lv3);
                this.client.execute(() -> this.client.setScreen(new RealmsGenericErrorScreen(lv3, (Screen)this.parent)));
            }
        }).start();
    }

    private void disableButtons() {
        this.playersButton.active = !this.server.expired;
        this.settingsButton.active = !this.server.expired;
        this.subscriptionButton.active = true;
        this.switchMinigameButton.active = !this.server.expired;
        this.optionsButton.active = !this.server.expired;
        this.resetWorldButton.active = !this.server.expired;
    }

    private void joinRealm(RealmsServer serverData) {
        if (this.server.state == RealmsServer.State.OPEN) {
            RealmsMainScreen.play(serverData, this);
        } else {
            this.openTheWorld(true);
        }
    }

    private void switchToMinigame() {
        RealmsSelectWorldTemplateScreen lv = new RealmsSelectWorldTemplateScreen(Text.translatable("mco.template.title.minigame"), this::switchMinigame, RealmsServer.WorldType.MINIGAME);
        lv.setWarning(Text.translatable("mco.minigame.world.info.line1"), Text.translatable("mco.minigame.world.info.line2"));
        this.client.setScreen(lv);
    }

    private void switchToFullSlot(int selectedSlot, RealmsServer serverData) {
        this.client.setScreen(RealmsPopups.createInfoPopup(this, Text.translatable("mco.configure.world.slot.switch.question.line1"), arg2 -> {
            this.stateChanged();
            this.client.setScreen(new RealmsLongRunningMcoTaskScreen(this.parent, new SwitchSlotTask(arg.id, selectedSlot, () -> this.client.execute(() -> this.client.setScreen(this.getNewScreen())))));
        }));
    }

    private void switchToEmptySlot(int selectedSlot, RealmsServer serverData) {
        this.client.setScreen(RealmsPopups.createInfoPopup(this, Text.translatable("mco.configure.world.slot.switch.question.line1"), arg2 -> {
            this.stateChanged();
            RealmsCreateWorldScreen lv = RealmsCreateWorldScreen.newWorld(this, selectedSlot, serverData, () -> this.client.execute(() -> this.client.setScreen(this.getNewScreen())));
            this.client.setScreen(lv);
        }));
    }

    private void drawServerState(DrawContext context, int x, int y, int mouseX, int mouseY) {
        if (this.server.expired) {
            this.drawServerState(context, x, y, mouseX, mouseY, EXPIRED_STATUS_TEXTURE, () -> EXPIRED_TEXT);
        } else if (this.server.state == RealmsServer.State.CLOSED) {
            this.drawServerState(context, x, y, mouseX, mouseY, CLOSED_STATUS_TEXTURE, () -> CLOSED_TEXT);
        } else if (this.server.state == RealmsServer.State.OPEN) {
            if (this.server.daysLeft < 7) {
                this.drawServerState(context, x, y, mouseX, mouseY, EXPIRES_SOON_STATUS_TEXTURE, () -> {
                    if (this.server.daysLeft <= 0) {
                        return EXPIRES_SOON_TEXT;
                    }
                    if (this.server.daysLeft == 1) {
                        return EXPIRES_IN_A_DAY_TEXT;
                    }
                    return Text.translatable("mco.selectServer.expires.days", this.server.daysLeft);
                });
            } else {
                this.drawServerState(context, x, y, mouseX, mouseY, OPEN_STATUS_TEXTURE, () -> OPEN_TEXT);
            }
        }
    }

    private void drawServerState(DrawContext context, int x, int y, int mouseX, int mouseY, Identifier texture, Supplier<Text> tooltipGetter) {
        context.drawGuiTexture(texture, x, y, 10, 28);
        if (mouseX >= x && mouseX <= x + 9 && mouseY >= y && mouseY <= y + 27) {
            this.setTooltip(tooltipGetter.get());
        }
    }

    private boolean isMinigame() {
        return this.server != null && this.server.isMinigame();
    }

    private void hideRegularButtons() {
        this.removeButton(this.optionsButton);
        this.removeButton(this.backupButton);
        this.removeButton(this.resetWorldButton);
    }

    private void removeButton(ButtonWidget button) {
        button.visible = false;
    }

    private void addButton(ButtonWidget button) {
        button.visible = true;
    }

    private void hideMinigameButtons() {
        this.removeButton(this.switchMinigameButton);
    }

    public void saveSlotSettings(RealmsWorldOptions options) {
        RealmsWorldOptions lv = this.server.slots.get(this.server.activeSlot);
        options.templateId = lv.templateId;
        options.templateImage = lv.templateImage;
        RealmsClient lv2 = RealmsClient.create();
        try {
            lv2.updateSlot(this.server.id, this.server.activeSlot, options);
            this.server.slots.put(this.server.activeSlot, options);
        } catch (RealmsServiceException lv3) {
            LOGGER.error("Couldn't save slot settings", lv3);
            this.client.setScreen(new RealmsGenericErrorScreen(lv3, (Screen)this));
            return;
        }
        this.client.setScreen(this);
    }

    public void saveSettings(String name, String desc) {
        String string3 = StringHelper.isBlank(desc) ? null : desc;
        RealmsClient lv = RealmsClient.create();
        try {
            lv.update(this.server.id, name, string3);
            this.server.setName(name);
            this.server.setDescription(string3);
            this.stateChanged();
        } catch (RealmsServiceException lv2) {
            LOGGER.error("Couldn't save settings", lv2);
            this.client.setScreen(new RealmsGenericErrorScreen(lv2, (Screen)this));
            return;
        }
        this.client.setScreen(this);
    }

    public void openTheWorld(boolean join) {
        RealmsConfigureWorldScreen lv = this.getNewScreen();
        this.client.setScreen(new RealmsLongRunningMcoTaskScreen(lv, new OpenServerTask(this.server, lv, join, this.client)));
    }

    public void closeTheWorld() {
        RealmsConfigureWorldScreen lv = this.getNewScreen();
        this.client.setScreen(new RealmsLongRunningMcoTaskScreen(lv, new CloseServerTask(this.server, lv)));
    }

    public void stateChanged() {
        this.stateChanged = true;
    }

    private void switchMinigame(@Nullable WorldTemplate template) {
        if (template != null && WorldTemplate.WorldTemplateType.MINIGAME == template.type) {
            this.stateChanged();
            this.client.setScreen(new RealmsLongRunningMcoTaskScreen(this.parent, new SwitchMinigameTask(this.server.id, template, this.getNewScreen())));
        } else {
            this.client.setScreen(this);
        }
    }

    public RealmsConfigureWorldScreen getNewScreen() {
        RealmsConfigureWorldScreen lv = new RealmsConfigureWorldScreen(this.parent, this.serverId);
        lv.stateChanged = this.stateChanged;
        return lv;
    }
}

