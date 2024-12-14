/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 * 
 * Could not load the following classes:
 *  net.fabricmc.api.EnvType
 *  net.fabricmc.api.Environment
 */
package net.minecraft.client.gui.widget;

import java.util.function.Consumer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.EditBox;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.narration.NarrationMessageBuilder;
import net.minecraft.client.gui.screen.narration.NarrationPart;
import net.minecraft.client.gui.widget.ScrollableWidget;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Colors;
import net.minecraft.util.StringHelper;
import net.minecraft.util.Util;

@Environment(value=EnvType.CLIENT)
public class EditBoxWidget
extends ScrollableWidget {
    private static final int CURSOR_PADDING = 1;
    private static final int CURSOR_COLOR = -3092272;
    private static final String UNDERSCORE = "_";
    private static final int FOCUSED_BOX_TEXT_COLOR = -2039584;
    private static final int UNFOCUSED_BOX_TEXT_COLOR = -857677600;
    private static final int CURSOR_BLINK_INTERVAL = 300;
    private final TextRenderer textRenderer;
    private final Text placeholder;
    private final EditBox editBox;
    private long lastSwitchFocusTime = Util.getMeasuringTimeMs();

    public EditBoxWidget(TextRenderer textRenderer, int x, int y, int width, int height, Text placeholder, Text message) {
        super(x, y, width, height, message);
        this.textRenderer = textRenderer;
        this.placeholder = placeholder;
        this.editBox = new EditBox(textRenderer, width - this.getPaddingDoubled());
        this.editBox.setCursorChangeListener(this::onCursorChange);
    }

    public void setMaxLength(int maxLength) {
        this.editBox.setMaxLength(maxLength);
    }

    public void setChangeListener(Consumer<String> changeListener) {
        this.editBox.setChangeListener(changeListener);
    }

    public void setText(String text) {
        this.editBox.setText(text);
    }

    public String getText() {
        return this.editBox.getText();
    }

    @Override
    public void appendClickableNarrations(NarrationMessageBuilder builder) {
        builder.put(NarrationPart.TITLE, (Text)Text.translatable("gui.narrate.editBox", this.getMessage(), this.getText()));
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (this.isWithinBounds(mouseX, mouseY) && button == 0) {
            this.editBox.setSelecting(Screen.hasShiftDown());
            this.moveCursor(mouseX, mouseY);
            return true;
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double deltaX, double deltaY) {
        if (super.mouseDragged(mouseX, mouseY, button, deltaX, deltaY)) {
            return true;
        }
        if (this.isWithinBounds(mouseX, mouseY) && button == 0) {
            this.editBox.setSelecting(true);
            this.moveCursor(mouseX, mouseY);
            this.editBox.setSelecting(Screen.hasShiftDown());
            return true;
        }
        return false;
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        return this.editBox.handleSpecialKey(keyCode);
    }

    @Override
    public boolean charTyped(char chr, int modifiers) {
        if (!(this.visible && this.isFocused() && StringHelper.isValidChar(chr))) {
            return false;
        }
        this.editBox.replaceSelection(Character.toString(chr));
        return true;
    }

    @Override
    protected void renderContents(DrawContext context, int mouseX, int mouseY, float delta) {
        String string = this.editBox.getText();
        if (string.isEmpty() && !this.isFocused()) {
            context.drawTextWrapped(this.textRenderer, this.placeholder, this.getX() + this.getPadding(), this.getY() + this.getPadding(), this.width - this.getPaddingDoubled(), -857677600);
            return;
        }
        int k = this.editBox.getCursor();
        boolean bl = this.isFocused() && (Util.getMeasuringTimeMs() - this.lastSwitchFocusTime) / 300L % 2L == 0L;
        boolean bl2 = k < string.length();
        int l = 0;
        int m = 0;
        int n = this.getY() + this.getPadding();
        for (EditBox.Substring lv : this.editBox.getLines()) {
            boolean bl3 = this.isVisible(n, n + this.textRenderer.fontHeight);
            if (bl && bl2 && k >= lv.beginIndex() && k <= lv.endIndex()) {
                if (bl3) {
                    l = context.drawTextWithShadow(this.textRenderer, string.substring(lv.beginIndex(), k), this.getX() + this.getPadding(), n, -2039584) - 1;
                    context.fill(l, n - 1, l + 1, n + 1 + this.textRenderer.fontHeight, -3092272);
                    context.drawTextWithShadow(this.textRenderer, string.substring(k, lv.endIndex()), l, n, -2039584);
                }
            } else {
                if (bl3) {
                    l = context.drawTextWithShadow(this.textRenderer, string.substring(lv.beginIndex(), lv.endIndex()), this.getX() + this.getPadding(), n, -2039584) - 1;
                }
                m = n;
            }
            n += this.textRenderer.fontHeight;
        }
        if (bl && !bl2 && this.isVisible(m, m + this.textRenderer.fontHeight)) {
            context.drawTextWithShadow(this.textRenderer, UNDERSCORE, l, m, -3092272);
        }
        if (this.editBox.hasSelection()) {
            EditBox.Substring lv2 = this.editBox.getSelection();
            int o = this.getX() + this.getPadding();
            n = this.getY() + this.getPadding();
            for (EditBox.Substring lv3 : this.editBox.getLines()) {
                if (lv2.beginIndex() > lv3.endIndex()) {
                    n += this.textRenderer.fontHeight;
                    continue;
                }
                if (lv3.beginIndex() > lv2.endIndex()) break;
                if (this.isVisible(n, n + this.textRenderer.fontHeight)) {
                    int p = this.textRenderer.getWidth(string.substring(lv3.beginIndex(), Math.max(lv2.beginIndex(), lv3.beginIndex())));
                    int q = lv2.endIndex() > lv3.endIndex() ? this.width - this.getPadding() : this.textRenderer.getWidth(string.substring(lv3.beginIndex(), lv2.endIndex()));
                    this.drawSelection(context, o + p, n, o + q, n + this.textRenderer.fontHeight);
                }
                n += this.textRenderer.fontHeight;
            }
        }
    }

    @Override
    protected void renderOverlay(DrawContext context) {
        super.renderOverlay(context);
        if (this.editBox.hasMaxLength()) {
            int i = this.editBox.getMaxLength();
            MutableText lv = Text.translatable("gui.multiLineEditBox.character_limit", this.editBox.getText().length(), i);
            context.drawTextWithShadow(this.textRenderer, lv, this.getX() + this.width - this.textRenderer.getWidth(lv), this.getY() + this.height + 4, 0xA0A0A0);
        }
    }

    @Override
    public int getContentsHeight() {
        return this.textRenderer.fontHeight * this.editBox.getLineCount();
    }

    @Override
    protected boolean overflows() {
        return (double)this.editBox.getLineCount() > this.getMaxLinesWithoutOverflow();
    }

    @Override
    protected double getDeltaYPerScroll() {
        return (double)this.textRenderer.fontHeight / 2.0;
    }

    private void drawSelection(DrawContext context, int left, int top, int right, int bottom) {
        context.fill(RenderLayer.getGuiTextHighlight(), left, top, right, bottom, Colors.BLUE);
    }

    private void onCursorChange() {
        double d = this.getScrollY();
        EditBox.Substring lv = this.editBox.getLine((int)(d / (double)this.textRenderer.fontHeight));
        if (this.editBox.getCursor() <= lv.beginIndex()) {
            d = this.editBox.getCurrentLineIndex() * this.textRenderer.fontHeight;
        } else {
            EditBox.Substring lv2 = this.editBox.getLine((int)((d + (double)this.height) / (double)this.textRenderer.fontHeight) - 1);
            if (this.editBox.getCursor() > lv2.endIndex()) {
                d = this.editBox.getCurrentLineIndex() * this.textRenderer.fontHeight - this.height + this.textRenderer.fontHeight + this.getPaddingDoubled();
            }
        }
        this.setScrollY(d);
    }

    private double getMaxLinesWithoutOverflow() {
        return (double)(this.height - this.getPaddingDoubled()) / (double)this.textRenderer.fontHeight;
    }

    private void moveCursor(double mouseX, double mouseY) {
        double f = mouseX - (double)this.getX() - (double)this.getPadding();
        double g = mouseY - (double)this.getY() - (double)this.getPadding() + this.getScrollY();
        this.editBox.moveCursor(f, g);
    }

    @Override
    public void setFocused(boolean focused) {
        super.setFocused(focused);
        if (focused) {
            this.lastSwitchFocusTime = Util.getMeasuringTimeMs();
        }
    }
}

