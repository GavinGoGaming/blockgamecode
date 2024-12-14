/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 * 
 * Could not load the following classes:
 *  net.fabricmc.api.EnvType
 *  net.fabricmc.api.Environment
 */
package net.minecraft.client.gui;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.Lists;
import java.util.List;
import java.util.function.Consumer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.input.CursorMovement;
import net.minecraft.text.Style;
import net.minecraft.util.StringHelper;
import net.minecraft.util.math.MathHelper;

@Environment(value=EnvType.CLIENT)
public class EditBox {
    public static final int UNLIMITED_LENGTH = Integer.MAX_VALUE;
    private static final int CURSOR_WIDTH = 2;
    private final TextRenderer textRenderer;
    private final List<Substring> lines = Lists.newArrayList();
    private String text;
    private int cursor;
    private int selectionEnd;
    private boolean selecting;
    private int maxLength = Integer.MAX_VALUE;
    private final int width;
    private Consumer<String> changeListener = text -> {};
    private Runnable cursorChangeListener = () -> {};

    public EditBox(TextRenderer textRenderer, int width) {
        this.textRenderer = textRenderer;
        this.width = width;
        this.setText("");
    }

    public int getMaxLength() {
        return this.maxLength;
    }

    public void setMaxLength(int maxLength) {
        if (maxLength < 0) {
            throw new IllegalArgumentException("Character limit cannot be negative");
        }
        this.maxLength = maxLength;
    }

    public boolean hasMaxLength() {
        return this.maxLength != Integer.MAX_VALUE;
    }

    public void setChangeListener(Consumer<String> changeListener) {
        this.changeListener = changeListener;
    }

    public void setCursorChangeListener(Runnable cursorChangeListener) {
        this.cursorChangeListener = cursorChangeListener;
    }

    public void setText(String text) {
        this.text = this.truncateForReplacement(text);
        this.selectionEnd = this.cursor = this.text.length();
        this.onChange();
    }

    public String getText() {
        return this.text;
    }

    public void replaceSelection(String string) {
        if (string.isEmpty() && !this.hasSelection()) {
            return;
        }
        String string2 = this.truncate(StringHelper.stripInvalidChars(string, true));
        Substring lv = this.getSelection();
        this.text = new StringBuilder(this.text).replace(lv.beginIndex, lv.endIndex, string2).toString();
        this.selectionEnd = this.cursor = lv.beginIndex + string2.length();
        this.onChange();
    }

    public void delete(int offset) {
        if (!this.hasSelection()) {
            this.selectionEnd = MathHelper.clamp(this.cursor + offset, 0, this.text.length());
        }
        this.replaceSelection("");
    }

    public int getCursor() {
        return this.cursor;
    }

    public void setSelecting(boolean selecting) {
        this.selecting = selecting;
    }

    public Substring getSelection() {
        return new Substring(Math.min(this.selectionEnd, this.cursor), Math.max(this.selectionEnd, this.cursor));
    }

    public int getLineCount() {
        return this.lines.size();
    }

    public int getCurrentLineIndex() {
        for (int i = 0; i < this.lines.size(); ++i) {
            Substring lv = this.lines.get(i);
            if (this.cursor < lv.beginIndex || this.cursor > lv.endIndex) continue;
            return i;
        }
        return -1;
    }

    public Substring getLine(int index) {
        return this.lines.get(MathHelper.clamp(index, 0, this.lines.size() - 1));
    }

    public void moveCursor(CursorMovement movement, int amount) {
        switch (movement) {
            case ABSOLUTE: {
                this.cursor = amount;
                break;
            }
            case RELATIVE: {
                this.cursor += amount;
                break;
            }
            case END: {
                this.cursor = this.text.length() + amount;
            }
        }
        this.cursor = MathHelper.clamp(this.cursor, 0, this.text.length());
        this.cursorChangeListener.run();
        if (!this.selecting) {
            this.selectionEnd = this.cursor;
        }
    }

    public void moveCursorLine(int offset) {
        if (offset == 0) {
            return;
        }
        int j = this.textRenderer.getWidth(this.text.substring(this.getCurrentLine().beginIndex, this.cursor)) + 2;
        Substring lv = this.getOffsetLine(offset);
        int k = this.textRenderer.trimToWidth(this.text.substring(lv.beginIndex, lv.endIndex), j).length();
        this.moveCursor(CursorMovement.ABSOLUTE, lv.beginIndex + k);
    }

    public void moveCursor(double x, double y) {
        int i = MathHelper.floor(x);
        int j = MathHelper.floor(y / (double)this.textRenderer.fontHeight);
        Substring lv = this.lines.get(MathHelper.clamp(j, 0, this.lines.size() - 1));
        int k = this.textRenderer.trimToWidth(this.text.substring(lv.beginIndex, lv.endIndex), i).length();
        this.moveCursor(CursorMovement.ABSOLUTE, lv.beginIndex + k);
    }

    public boolean handleSpecialKey(int keyCode) {
        this.selecting = Screen.hasShiftDown();
        if (Screen.isSelectAll(keyCode)) {
            this.cursor = this.text.length();
            this.selectionEnd = 0;
            return true;
        }
        if (Screen.isCopy(keyCode)) {
            MinecraftClient.getInstance().keyboard.setClipboard(this.getSelectedText());
            return true;
        }
        if (Screen.isPaste(keyCode)) {
            this.replaceSelection(MinecraftClient.getInstance().keyboard.getClipboard());
            return true;
        }
        if (Screen.isCut(keyCode)) {
            MinecraftClient.getInstance().keyboard.setClipboard(this.getSelectedText());
            this.replaceSelection("");
            return true;
        }
        switch (keyCode) {
            case 263: {
                if (Screen.hasControlDown()) {
                    Substring lv = this.getPreviousWordAtCursor();
                    this.moveCursor(CursorMovement.ABSOLUTE, lv.beginIndex);
                } else {
                    this.moveCursor(CursorMovement.RELATIVE, -1);
                }
                return true;
            }
            case 262: {
                if (Screen.hasControlDown()) {
                    Substring lv = this.getNextWordAtCursor();
                    this.moveCursor(CursorMovement.ABSOLUTE, lv.beginIndex);
                } else {
                    this.moveCursor(CursorMovement.RELATIVE, 1);
                }
                return true;
            }
            case 265: {
                if (!Screen.hasControlDown()) {
                    this.moveCursorLine(-1);
                }
                return true;
            }
            case 264: {
                if (!Screen.hasControlDown()) {
                    this.moveCursorLine(1);
                }
                return true;
            }
            case 266: {
                this.moveCursor(CursorMovement.ABSOLUTE, 0);
                return true;
            }
            case 267: {
                this.moveCursor(CursorMovement.END, 0);
                return true;
            }
            case 268: {
                if (Screen.hasControlDown()) {
                    this.moveCursor(CursorMovement.ABSOLUTE, 0);
                } else {
                    this.moveCursor(CursorMovement.ABSOLUTE, this.getCurrentLine().beginIndex);
                }
                return true;
            }
            case 269: {
                if (Screen.hasControlDown()) {
                    this.moveCursor(CursorMovement.END, 0);
                } else {
                    this.moveCursor(CursorMovement.ABSOLUTE, this.getCurrentLine().endIndex);
                }
                return true;
            }
            case 259: {
                if (Screen.hasControlDown()) {
                    Substring lv = this.getPreviousWordAtCursor();
                    this.delete(lv.beginIndex - this.cursor);
                } else {
                    this.delete(-1);
                }
                return true;
            }
            case 261: {
                if (Screen.hasControlDown()) {
                    Substring lv = this.getNextWordAtCursor();
                    this.delete(lv.beginIndex - this.cursor);
                } else {
                    this.delete(1);
                }
                return true;
            }
            case 257: 
            case 335: {
                this.replaceSelection("\n");
                return true;
            }
        }
        return false;
    }

    public Iterable<Substring> getLines() {
        return this.lines;
    }

    public boolean hasSelection() {
        return this.selectionEnd != this.cursor;
    }

    @VisibleForTesting
    public String getSelectedText() {
        Substring lv = this.getSelection();
        return this.text.substring(lv.beginIndex, lv.endIndex);
    }

    private Substring getCurrentLine() {
        return this.getOffsetLine(0);
    }

    private Substring getOffsetLine(int offsetFromCurrent) {
        int j = this.getCurrentLineIndex();
        if (j < 0) {
            throw new IllegalStateException("Cursor is not within text (cursor = " + this.cursor + ", length = " + this.text.length() + ")");
        }
        return this.lines.get(MathHelper.clamp(j + offsetFromCurrent, 0, this.lines.size() - 1));
    }

    @VisibleForTesting
    public Substring getPreviousWordAtCursor() {
        int i;
        if (this.text.isEmpty()) {
            return Substring.EMPTY;
        }
        for (i = MathHelper.clamp(this.cursor, 0, this.text.length() - 1); i > 0 && Character.isWhitespace(this.text.charAt(i - 1)); --i) {
        }
        while (i > 0 && !Character.isWhitespace(this.text.charAt(i - 1))) {
            --i;
        }
        return new Substring(i, this.getWordEndIndex(i));
    }

    @VisibleForTesting
    public Substring getNextWordAtCursor() {
        int i;
        if (this.text.isEmpty()) {
            return Substring.EMPTY;
        }
        for (i = MathHelper.clamp(this.cursor, 0, this.text.length() - 1); i < this.text.length() && !Character.isWhitespace(this.text.charAt(i)); ++i) {
        }
        while (i < this.text.length() && Character.isWhitespace(this.text.charAt(i))) {
            ++i;
        }
        return new Substring(i, this.getWordEndIndex(i));
    }

    private int getWordEndIndex(int startIndex) {
        int j;
        for (j = startIndex; j < this.text.length() && !Character.isWhitespace(this.text.charAt(j)); ++j) {
        }
        return j;
    }

    private void onChange() {
        this.rewrap();
        this.changeListener.accept(this.text);
        this.cursorChangeListener.run();
    }

    private void rewrap() {
        this.lines.clear();
        if (this.text.isEmpty()) {
            this.lines.add(Substring.EMPTY);
            return;
        }
        this.textRenderer.getTextHandler().wrapLines(this.text, this.width, Style.EMPTY, false, (style, start, end) -> this.lines.add(new Substring(start, end)));
        if (this.text.charAt(this.text.length() - 1) == '\n') {
            this.lines.add(new Substring(this.text.length(), this.text.length()));
        }
    }

    private String truncateForReplacement(String value) {
        if (this.hasMaxLength()) {
            return StringHelper.truncate(value, this.maxLength, false);
        }
        return value;
    }

    private String truncate(String value) {
        if (this.hasMaxLength()) {
            int i = this.maxLength - this.text.length();
            return StringHelper.truncate(value, i, false);
        }
        return value;
    }

    @Environment(value=EnvType.CLIENT)
    protected record Substring(int beginIndex, int endIndex) {
        static final Substring EMPTY = new Substring(0, 0);
    }
}

