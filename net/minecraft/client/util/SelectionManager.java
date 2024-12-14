/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 * 
 * Could not load the following classes:
 *  net.fabricmc.api.EnvType
 *  net.fabricmc.api.Environment
 */
package net.minecraft.client.util;

import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.function.Supplier;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextHandler;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.util.Formatting;
import net.minecraft.util.StringHelper;
import net.minecraft.util.Util;
import net.minecraft.util.math.MathHelper;

@Environment(value=EnvType.CLIENT)
public class SelectionManager {
    private final Supplier<String> stringGetter;
    private final Consumer<String> stringSetter;
    private final Supplier<String> clipboardGetter;
    private final Consumer<String> clipboardSetter;
    private final Predicate<String> stringFilter;
    private int selectionStart;
    private int selectionEnd;

    public SelectionManager(Supplier<String> stringGetter, Consumer<String> stringSetter, Supplier<String> clipboardGetter, Consumer<String> clipboardSetter, Predicate<String> stringFilter) {
        this.stringGetter = stringGetter;
        this.stringSetter = stringSetter;
        this.clipboardGetter = clipboardGetter;
        this.clipboardSetter = clipboardSetter;
        this.stringFilter = stringFilter;
        this.putCursorAtEnd();
    }

    public static Supplier<String> makeClipboardGetter(MinecraftClient client) {
        return () -> SelectionManager.getClipboard(client);
    }

    public static String getClipboard(MinecraftClient client) {
        return Formatting.strip(client.keyboard.getClipboard().replaceAll("\\r", ""));
    }

    public static Consumer<String> makeClipboardSetter(MinecraftClient client) {
        return clipboardString -> SelectionManager.setClipboard(client, clipboardString);
    }

    public static void setClipboard(MinecraftClient client, String clipboard) {
        client.keyboard.setClipboard(clipboard);
    }

    public boolean insert(char c) {
        if (StringHelper.isValidChar(c)) {
            this.insert(this.stringGetter.get(), Character.toString(c));
        }
        return true;
    }

    public boolean handleSpecialKey(int keyCode) {
        SelectionType lv;
        if (Screen.isSelectAll(keyCode)) {
            this.selectAll();
            return true;
        }
        if (Screen.isCopy(keyCode)) {
            this.copy();
            return true;
        }
        if (Screen.isPaste(keyCode)) {
            this.paste();
            return true;
        }
        if (Screen.isCut(keyCode)) {
            this.cut();
            return true;
        }
        SelectionType selectionType = lv = Screen.hasControlDown() ? SelectionType.WORD : SelectionType.CHARACTER;
        if (keyCode == 259) {
            this.delete(-1, lv);
            return true;
        }
        if (keyCode == 261) {
            this.delete(1, lv);
        } else {
            if (keyCode == 263) {
                this.moveCursor(-1, Screen.hasShiftDown(), lv);
                return true;
            }
            if (keyCode == 262) {
                this.moveCursor(1, Screen.hasShiftDown(), lv);
                return true;
            }
            if (keyCode == 268) {
                this.moveCursorToStart(Screen.hasShiftDown());
                return true;
            }
            if (keyCode == 269) {
                this.moveCursorToEnd(Screen.hasShiftDown());
                return true;
            }
        }
        return false;
    }

    private int clampCursorPosition(int pos) {
        return MathHelper.clamp(pos, 0, this.stringGetter.get().length());
    }

    private void insert(String string, String insertion) {
        if (this.selectionEnd != this.selectionStart) {
            string = this.deleteSelectedText(string);
        }
        this.selectionStart = MathHelper.clamp(this.selectionStart, 0, string.length());
        String string3 = new StringBuilder(string).insert(this.selectionStart, insertion).toString();
        if (this.stringFilter.test(string3)) {
            this.stringSetter.accept(string3);
            this.selectionEnd = this.selectionStart = Math.min(string3.length(), this.selectionStart + insertion.length());
        }
    }

    public void insert(String string) {
        this.insert(this.stringGetter.get(), string);
    }

    private void updateSelectionRange(boolean shiftDown) {
        if (!shiftDown) {
            this.selectionEnd = this.selectionStart;
        }
    }

    public void moveCursor(int offset, boolean shiftDown, SelectionType selectionType) {
        switch (selectionType.ordinal()) {
            case 0: {
                this.moveCursor(offset, shiftDown);
                break;
            }
            case 1: {
                this.moveCursorPastWord(offset, shiftDown);
            }
        }
    }

    public void moveCursor(int offset) {
        this.moveCursor(offset, false);
    }

    public void moveCursor(int offset, boolean shiftDown) {
        this.selectionStart = Util.moveCursor(this.stringGetter.get(), this.selectionStart, offset);
        this.updateSelectionRange(shiftDown);
    }

    public void moveCursorPastWord(int offset) {
        this.moveCursorPastWord(offset, false);
    }

    public void moveCursorPastWord(int offset, boolean shiftDown) {
        this.selectionStart = TextHandler.moveCursorByWords(this.stringGetter.get(), offset, this.selectionStart, true);
        this.updateSelectionRange(shiftDown);
    }

    public void delete(int offset, SelectionType selectionType) {
        switch (selectionType.ordinal()) {
            case 0: {
                this.delete(offset);
                break;
            }
            case 1: {
                this.deleteWord(offset);
            }
        }
    }

    public void deleteWord(int offset) {
        int j = TextHandler.moveCursorByWords(this.stringGetter.get(), offset, this.selectionStart, true);
        this.delete(j - this.selectionStart);
    }

    public void delete(int offset) {
        String string = this.stringGetter.get();
        if (!string.isEmpty()) {
            String string2;
            if (this.selectionEnd != this.selectionStart) {
                string2 = this.deleteSelectedText(string);
            } else {
                int j = Util.moveCursor(string, this.selectionStart, offset);
                int k = Math.min(j, this.selectionStart);
                int l = Math.max(j, this.selectionStart);
                string2 = new StringBuilder(string).delete(k, l).toString();
                if (offset < 0) {
                    this.selectionEnd = this.selectionStart = k;
                }
            }
            this.stringSetter.accept(string2);
        }
    }

    public void cut() {
        String string = this.stringGetter.get();
        this.clipboardSetter.accept(this.getSelectedText(string));
        this.stringSetter.accept(this.deleteSelectedText(string));
    }

    public void paste() {
        this.insert(this.stringGetter.get(), this.clipboardGetter.get());
        this.selectionEnd = this.selectionStart;
    }

    public void copy() {
        this.clipboardSetter.accept(this.getSelectedText(this.stringGetter.get()));
    }

    public void selectAll() {
        this.selectionEnd = 0;
        this.selectionStart = this.stringGetter.get().length();
    }

    private String getSelectedText(String string) {
        int i = Math.min(this.selectionStart, this.selectionEnd);
        int j = Math.max(this.selectionStart, this.selectionEnd);
        return string.substring(i, j);
    }

    private String deleteSelectedText(String string) {
        if (this.selectionEnd == this.selectionStart) {
            return string;
        }
        int i = Math.min(this.selectionStart, this.selectionEnd);
        int j = Math.max(this.selectionStart, this.selectionEnd);
        String string2 = string.substring(0, i) + string.substring(j);
        this.selectionEnd = this.selectionStart = i;
        return string2;
    }

    public void moveCursorToStart() {
        this.moveCursorToStart(false);
    }

    public void moveCursorToStart(boolean shiftDown) {
        this.selectionStart = 0;
        this.updateSelectionRange(shiftDown);
    }

    public void putCursorAtEnd() {
        this.moveCursorToEnd(false);
    }

    public void moveCursorToEnd(boolean shiftDown) {
        this.selectionStart = this.stringGetter.get().length();
        this.updateSelectionRange(shiftDown);
    }

    public int getSelectionStart() {
        return this.selectionStart;
    }

    public void moveCursorTo(int position) {
        this.moveCursorTo(position, true);
    }

    public void moveCursorTo(int position, boolean shiftDown) {
        this.selectionStart = this.clampCursorPosition(position);
        this.updateSelectionRange(shiftDown);
    }

    public int getSelectionEnd() {
        return this.selectionEnd;
    }

    public void setSelectionEnd(int pos) {
        this.selectionEnd = this.clampCursorPosition(pos);
    }

    public void setSelection(int start, int end) {
        int k = this.stringGetter.get().length();
        this.selectionStart = MathHelper.clamp(start, 0, k);
        this.selectionEnd = MathHelper.clamp(end, 0, k);
    }

    public boolean isSelecting() {
        return this.selectionStart != this.selectionEnd;
    }

    @Environment(value=EnvType.CLIENT)
    public static enum SelectionType {
        CHARACTER,
        WORD;

    }
}

