/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 * 
 * Could not load the following classes:
 *  net.fabricmc.api.EnvType
 *  net.fabricmc.api.Environment
 */
package net.minecraft.client.gui.screen.pack;

import java.util.Objects;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.MultilineText;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.ConfirmScreen;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.pack.PackScreen;
import net.minecraft.client.gui.screen.pack.ResourcePackOrganizer;
import net.minecraft.client.gui.widget.AlwaysSelectedEntryListWidget;
import net.minecraft.resource.ResourcePackCompatibility;
import net.minecraft.text.MutableText;
import net.minecraft.text.OrderedText;
import net.minecraft.text.StringVisitable;
import net.minecraft.text.Text;
import net.minecraft.util.Colors;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;
import net.minecraft.util.Language;

@Environment(value=EnvType.CLIENT)
public class PackListWidget
extends AlwaysSelectedEntryListWidget<ResourcePackEntry> {
    static final Identifier SELECT_HIGHLIGHTED_TEXTURE = Identifier.ofVanilla("transferable_list/select_highlighted");
    static final Identifier SELECT_TEXTURE = Identifier.ofVanilla("transferable_list/select");
    static final Identifier UNSELECT_HIGHLIGHTED_TEXTURE = Identifier.ofVanilla("transferable_list/unselect_highlighted");
    static final Identifier UNSELECT_TEXTURE = Identifier.ofVanilla("transferable_list/unselect");
    static final Identifier MOVE_UP_HIGHLIGHTED_TEXTURE = Identifier.ofVanilla("transferable_list/move_up_highlighted");
    static final Identifier MOVE_UP_TEXTURE = Identifier.ofVanilla("transferable_list/move_up");
    static final Identifier MOVE_DOWN_HIGHLIGHTED_TEXTURE = Identifier.ofVanilla("transferable_list/move_down_highlighted");
    static final Identifier MOVE_DOWN_TEXTURE = Identifier.ofVanilla("transferable_list/move_down");
    static final Text INCOMPATIBLE = Text.translatable("pack.incompatible");
    static final Text INCOMPATIBLE_CONFIRM = Text.translatable("pack.incompatible.confirm.title");
    private final Text title;
    final PackScreen screen;

    public PackListWidget(MinecraftClient client, PackScreen screen, int width, int height, Text title) {
        super(client, width, height, 33, 36);
        this.screen = screen;
        this.title = title;
        this.centerListVertically = false;
        Objects.requireNonNull(client.textRenderer);
        this.setRenderHeader(true, (int)(9.0f * 1.5f));
    }

    @Override
    protected void renderHeader(DrawContext context, int x, int y) {
        MutableText lv = Text.empty().append(this.title).formatted(Formatting.UNDERLINE, Formatting.BOLD);
        context.drawText(this.client.textRenderer, lv, x + this.width / 2 - this.client.textRenderer.getWidth(lv) / 2, Math.min(this.getY() + 3, y), Colors.WHITE, false);
    }

    @Override
    public int getRowWidth() {
        return this.width;
    }

    @Override
    protected int getScrollbarX() {
        return this.getRight() - 6;
    }

    @Override
    protected void drawSelectionHighlight(DrawContext context, int y, int entryWidth, int entryHeight, int borderColor, int fillColor) {
        if (this.isScrollbarVisible()) {
            int n = 2;
            int o = this.getRowLeft() - 2;
            int p = this.getRight() - 6 - 1;
            int q = y - 2;
            int r = y + entryHeight + 2;
            context.fill(o, q, p, r, borderColor);
            context.fill(o + 1, q + 1, p - 1, r - 1, fillColor);
        } else {
            super.drawSelectionHighlight(context, y, entryWidth, entryHeight, borderColor, fillColor);
        }
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (this.getSelectedOrNull() != null) {
            switch (keyCode) {
                case 32: 
                case 257: {
                    ((ResourcePackEntry)this.getSelectedOrNull()).toggle();
                    return true;
                }
            }
            if (Screen.hasShiftDown()) {
                switch (keyCode) {
                    case 265: {
                        ((ResourcePackEntry)this.getSelectedOrNull()).moveTowardStart();
                        return true;
                    }
                    case 264: {
                        ((ResourcePackEntry)this.getSelectedOrNull()).moveTowardEnd();
                        return true;
                    }
                }
            }
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Environment(value=EnvType.CLIENT)
    public static class ResourcePackEntry
    extends AlwaysSelectedEntryListWidget.Entry<ResourcePackEntry> {
        private static final int field_32403 = 157;
        private static final int field_32404 = 157;
        private static final String ELLIPSIS = "...";
        private final PackListWidget widget;
        protected final MinecraftClient client;
        private final ResourcePackOrganizer.Pack pack;
        private final OrderedText displayName;
        private final MultilineText description;
        private final OrderedText incompatibleText;
        private final MultilineText compatibilityNotificationText;

        public ResourcePackEntry(MinecraftClient client, PackListWidget widget, ResourcePackOrganizer.Pack pack) {
            this.client = client;
            this.pack = pack;
            this.widget = widget;
            this.displayName = ResourcePackEntry.trimTextToWidth(client, pack.getDisplayName());
            this.description = ResourcePackEntry.createMultilineText(client, pack.getDecoratedDescription());
            this.incompatibleText = ResourcePackEntry.trimTextToWidth(client, INCOMPATIBLE);
            this.compatibilityNotificationText = ResourcePackEntry.createMultilineText(client, pack.getCompatibility().getNotification());
        }

        private static OrderedText trimTextToWidth(MinecraftClient client, Text text) {
            int i = client.textRenderer.getWidth(text);
            if (i > 157) {
                StringVisitable lv = StringVisitable.concat(client.textRenderer.trimToWidth(text, 157 - client.textRenderer.getWidth(ELLIPSIS)), StringVisitable.plain(ELLIPSIS));
                return Language.getInstance().reorder(lv);
            }
            return text.asOrderedText();
        }

        private static MultilineText createMultilineText(MinecraftClient client, Text text) {
            return MultilineText.create(client.textRenderer, 157, 2, text);
        }

        @Override
        public Text getNarration() {
            return Text.translatable("narrator.select", this.pack.getDisplayName());
        }

        @Override
        public void render(DrawContext context, int index, int y, int x, int entryWidth, int entryHeight, int mouseX, int mouseY, boolean hovered, float tickDelta) {
            ResourcePackCompatibility lv = this.pack.getCompatibility();
            if (!lv.isCompatible()) {
                int p = x + entryWidth - 3 - (this.widget.isScrollbarVisible() ? 7 : 0);
                context.fill(x - 1, y - 1, p, y + entryHeight + 1, -8978432);
            }
            context.drawTexture(this.pack.getIconId(), x, y, 0.0f, 0.0f, 32, 32, 32, 32);
            OrderedText lv2 = this.displayName;
            MultilineText lv3 = this.description;
            if (this.isSelectable() && (this.client.options.getTouchscreen().getValue().booleanValue() || hovered || this.widget.getSelectedOrNull() == this && this.widget.isFocused())) {
                context.fill(x, y, x + 32, y + 32, -1601138544);
                int q = mouseX - x;
                int r = mouseY - y;
                if (!this.pack.getCompatibility().isCompatible()) {
                    lv2 = this.incompatibleText;
                    lv3 = this.compatibilityNotificationText;
                }
                if (this.pack.canBeEnabled()) {
                    if (q < 32) {
                        context.drawGuiTexture(SELECT_HIGHLIGHTED_TEXTURE, x, y, 32, 32);
                    } else {
                        context.drawGuiTexture(SELECT_TEXTURE, x, y, 32, 32);
                    }
                } else {
                    if (this.pack.canBeDisabled()) {
                        if (q < 16) {
                            context.drawGuiTexture(UNSELECT_HIGHLIGHTED_TEXTURE, x, y, 32, 32);
                        } else {
                            context.drawGuiTexture(UNSELECT_TEXTURE, x, y, 32, 32);
                        }
                    }
                    if (this.pack.canMoveTowardStart()) {
                        if (q < 32 && q > 16 && r < 16) {
                            context.drawGuiTexture(MOVE_UP_HIGHLIGHTED_TEXTURE, x, y, 32, 32);
                        } else {
                            context.drawGuiTexture(MOVE_UP_TEXTURE, x, y, 32, 32);
                        }
                    }
                    if (this.pack.canMoveTowardEnd()) {
                        if (q < 32 && q > 16 && r > 16) {
                            context.drawGuiTexture(MOVE_DOWN_HIGHLIGHTED_TEXTURE, x, y, 32, 32);
                        } else {
                            context.drawGuiTexture(MOVE_DOWN_TEXTURE, x, y, 32, 32);
                        }
                    }
                }
            }
            context.drawTextWithShadow(this.client.textRenderer, lv2, x + 32 + 2, y + 1, 0xFFFFFF);
            lv3.drawWithShadow(context, x + 32 + 2, y + 12, 10, -8355712);
        }

        public String getName() {
            return this.pack.getName();
        }

        private boolean isSelectable() {
            return !this.pack.isPinned() || !this.pack.isAlwaysEnabled();
        }

        public void toggle() {
            if (this.pack.canBeEnabled() && this.enable()) {
                this.widget.screen.switchFocusedList(this.widget);
            } else if (this.pack.canBeDisabled()) {
                this.pack.disable();
                this.widget.screen.switchFocusedList(this.widget);
            }
        }

        void moveTowardStart() {
            if (this.pack.canMoveTowardStart()) {
                this.pack.moveTowardStart();
            }
        }

        void moveTowardEnd() {
            if (this.pack.canMoveTowardEnd()) {
                this.pack.moveTowardEnd();
            }
        }

        private boolean enable() {
            if (this.pack.getCompatibility().isCompatible()) {
                this.pack.enable();
                return true;
            }
            Text lv = this.pack.getCompatibility().getConfirmMessage();
            this.client.setScreen(new ConfirmScreen(confirmed -> {
                this.client.setScreen(this.widget.screen);
                if (confirmed) {
                    this.pack.enable();
                }
            }, INCOMPATIBLE_CONFIRM, lv));
            return false;
        }

        @Override
        public boolean mouseClicked(double mouseX, double mouseY, int button) {
            double f = mouseX - (double)this.widget.getRowLeft();
            double g = mouseY - (double)this.widget.getRowTop(this.widget.children().indexOf(this));
            if (this.isSelectable() && f <= 32.0) {
                this.widget.screen.clearSelection();
                if (this.pack.canBeEnabled()) {
                    this.enable();
                    return true;
                }
                if (f < 16.0 && this.pack.canBeDisabled()) {
                    this.pack.disable();
                    return true;
                }
                if (f > 16.0 && g < 16.0 && this.pack.canMoveTowardStart()) {
                    this.pack.moveTowardStart();
                    return true;
                }
                if (f > 16.0 && g > 16.0 && this.pack.canMoveTowardEnd()) {
                    this.pack.moveTowardEnd();
                    return true;
                }
            }
            return super.mouseClicked(mouseX, mouseY, button);
        }
    }
}

