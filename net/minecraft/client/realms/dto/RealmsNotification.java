/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 * 
 * Could not load the following classes:
 *  net.fabricmc.api.EnvType
 *  net.fabricmc.api.Environment
 *  org.jetbrains.annotations.Nullable
 */
package net.minecraft.client.realms.dto;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.mojang.logging.LogUtils;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.function.Consumer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.ConfirmLinkScreen;
import net.minecraft.client.gui.screen.PopupScreen;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.realms.dto.RealmsText;
import net.minecraft.client.realms.util.JsonUtils;
import net.minecraft.screen.ScreenTexts;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.Util;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;

@Environment(value=EnvType.CLIENT)
public class RealmsNotification {
    static final Logger LOGGER = LogUtils.getLogger();
    private static final String NOTIFICATION_UUID_KEY = "notificationUuid";
    private static final String DISMISSABLE_KEY = "dismissable";
    private static final String SEEN_KEY = "seen";
    private static final String TYPE_KEY = "type";
    private static final String VISIT_URL_TYPE = "visitUrl";
    private static final String INFO_POPUP_TYPE = "infoPopup";
    static final Text OPEN_LINK_TEXT = Text.translatable("mco.notification.visitUrl.buttonText.default");
    final UUID uuid;
    final boolean dismissable;
    final boolean seen;
    final String type;

    RealmsNotification(UUID uuid, boolean dismissable, boolean seen, String type) {
        this.uuid = uuid;
        this.dismissable = dismissable;
        this.seen = seen;
        this.type = type;
    }

    public boolean isSeen() {
        return this.seen;
    }

    public boolean isDismissable() {
        return this.dismissable;
    }

    public UUID getUuid() {
        return this.uuid;
    }

    public static List<RealmsNotification> parse(String json) {
        ArrayList<RealmsNotification> list = new ArrayList<RealmsNotification>();
        try {
            JsonArray jsonArray = JsonParser.parseString(json).getAsJsonObject().get("notifications").getAsJsonArray();
            for (JsonElement jsonElement : jsonArray) {
                list.add(RealmsNotification.fromJson(jsonElement.getAsJsonObject()));
            }
        } catch (Exception exception) {
            LOGGER.error("Could not parse list of RealmsNotifications", exception);
        }
        return list;
    }

    private static RealmsNotification fromJson(JsonObject json) {
        UUID uUID = JsonUtils.getUuidOr(NOTIFICATION_UUID_KEY, json, null);
        if (uUID == null) {
            throw new IllegalStateException("Missing required property notificationUuid");
        }
        boolean bl = JsonUtils.getBooleanOr(DISMISSABLE_KEY, json, true);
        boolean bl2 = JsonUtils.getBooleanOr(SEEN_KEY, json, false);
        String string = JsonUtils.getString(TYPE_KEY, json);
        RealmsNotification lv = new RealmsNotification(uUID, bl, bl2, string);
        return switch (string) {
            case VISIT_URL_TYPE -> VisitUrl.fromJson(lv, json);
            case INFO_POPUP_TYPE -> InfoPopup.fromJson(lv, json);
            default -> lv;
        };
    }

    @Environment(value=EnvType.CLIENT)
    public static class VisitUrl
    extends RealmsNotification {
        private static final String URL_KEY = "url";
        private static final String BUTTON_TEXT_KEY = "buttonText";
        private static final String MESSAGE_KEY = "message";
        private final String url;
        private final RealmsText buttonText;
        private final RealmsText message;

        private VisitUrl(RealmsNotification parent, String url, RealmsText buttonText, RealmsText message) {
            super(parent.uuid, parent.dismissable, parent.seen, parent.type);
            this.url = url;
            this.buttonText = buttonText;
            this.message = message;
        }

        public static VisitUrl fromJson(RealmsNotification parent, JsonObject json) {
            String string = JsonUtils.getString(URL_KEY, json);
            RealmsText lv = JsonUtils.get(BUTTON_TEXT_KEY, json, RealmsText::fromJson);
            RealmsText lv2 = JsonUtils.get(MESSAGE_KEY, json, RealmsText::fromJson);
            return new VisitUrl(parent, string, lv, lv2);
        }

        public Text getDefaultMessage() {
            return this.message.toText(Text.translatable("mco.notification.visitUrl.message.default"));
        }

        public ButtonWidget createButton(Screen currentScreen) {
            Text lv = this.buttonText.toText(OPEN_LINK_TEXT);
            return ButtonWidget.builder(lv, ConfirmLinkScreen.opening(currentScreen, this.url)).build();
        }
    }

    @Environment(value=EnvType.CLIENT)
    public static class InfoPopup
    extends RealmsNotification {
        private static final String TITLE_KEY = "title";
        private static final String MESSAGE_KEY = "message";
        private static final String IMAGE_KEY = "image";
        private static final String URL_BUTTON_KEY = "urlButton";
        private final RealmsText title;
        private final RealmsText message;
        private final Identifier image;
        @Nullable
        private final UrlButton urlButton;

        private InfoPopup(RealmsNotification parent, RealmsText title, RealmsText message, Identifier image, @Nullable UrlButton urlButton) {
            super(parent.uuid, parent.dismissable, parent.seen, parent.type);
            this.title = title;
            this.message = message;
            this.image = image;
            this.urlButton = urlButton;
        }

        public static InfoPopup fromJson(RealmsNotification parent, JsonObject json) {
            RealmsText lv = JsonUtils.get(TITLE_KEY, json, RealmsText::fromJson);
            RealmsText lv2 = JsonUtils.get(MESSAGE_KEY, json, RealmsText::fromJson);
            Identifier lv3 = Identifier.of(JsonUtils.getString(IMAGE_KEY, json));
            UrlButton lv4 = JsonUtils.getNullable(URL_BUTTON_KEY, json, UrlButton::fromJson);
            return new InfoPopup(parent, lv, lv2, lv3, lv4);
        }

        @Nullable
        public PopupScreen createScreen(Screen backgroundScreen, Consumer<UUID> dismissCallback) {
            Text lv = this.title.toText();
            if (lv == null) {
                LOGGER.warn("Realms info popup had title with no available translation: {}", (Object)this.title);
                return null;
            }
            PopupScreen.Builder lv2 = new PopupScreen.Builder(backgroundScreen, lv).image(this.image).message(this.message.toText(ScreenTexts.EMPTY));
            if (this.urlButton != null) {
                lv2.button(this.urlButton.urlText.toText(OPEN_LINK_TEXT), screen -> {
                    MinecraftClient lv = MinecraftClient.getInstance();
                    lv.setScreen(new ConfirmLinkScreen(confirmed -> {
                        if (confirmed) {
                            Util.getOperatingSystem().open(this.urlButton.url);
                            lv.setScreen(backgroundScreen);
                        } else {
                            lv.setScreen((Screen)screen);
                        }
                    }, this.urlButton.url, true));
                    dismissCallback.accept(this.getUuid());
                });
            }
            lv2.button(ScreenTexts.OK, screen -> {
                screen.close();
                dismissCallback.accept(this.getUuid());
            });
            lv2.onClosed(() -> dismissCallback.accept(this.getUuid()));
            return lv2.build();
        }
    }

    @Environment(value=EnvType.CLIENT)
    record UrlButton(String url, RealmsText urlText) {
        private static final String URL_KEY = "url";
        private static final String URL_TEXT_KEY = "urlText";

        public static UrlButton fromJson(JsonObject json) {
            String string = JsonUtils.getString(URL_KEY, json);
            RealmsText lv = JsonUtils.get(URL_TEXT_KEY, json, RealmsText::fromJson);
            return new UrlButton(string, lv);
        }
    }
}

