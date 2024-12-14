/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 * 
 * Could not load the following classes:
 *  net.fabricmc.api.EnvType
 *  net.fabricmc.api.Environment
 */
package net.minecraft.client.realms.gui.screen;

import java.util.List;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.widget.AlwaysSelectedEntryListWidget;
import net.minecraft.client.realms.RealmsObjectSelectionList;

@Environment(value=EnvType.CLIENT)
public abstract class RealmsAcceptRejectButton {
    public final int width;
    public final int height;
    public final int x;
    public final int y;

    public RealmsAcceptRejectButton(int width, int height, int x, int y) {
        this.width = width;
        this.height = height;
        this.x = x;
        this.y = y;
    }

    public void render(DrawContext context, int x, int y, int mouseX, int mouseY) {
        int m = x + this.x;
        int n = y + this.y;
        boolean bl = mouseX >= m && mouseX <= m + this.width && mouseY >= n && mouseY <= n + this.height;
        this.render(context, m, n, bl);
    }

    protected abstract void render(DrawContext var1, int var2, int var3, boolean var4);

    public int getRight() {
        return this.x + this.width;
    }

    public int getBottom() {
        return this.y + this.height;
    }

    public abstract void handleClick(int var1);

    public static void render(DrawContext context, List<RealmsAcceptRejectButton> buttons, RealmsObjectSelectionList<?> selectionList, int x, int y, int mouseX, int mouseY) {
        for (RealmsAcceptRejectButton lv : buttons) {
            if (selectionList.getRowWidth() <= lv.getRight()) continue;
            lv.render(context, x, y, mouseX, mouseY);
        }
    }

    public static void handleClick(RealmsObjectSelectionList<?> selectionList, AlwaysSelectedEntryListWidget.Entry<?> entry, List<RealmsAcceptRejectButton> buttons, int button, double mouseX, double mouseY) {
        int j = selectionList.children().indexOf(entry);
        if (j > -1) {
            selectionList.setSelected(j);
            int k = selectionList.getRowLeft();
            int l = selectionList.getRowTop(j);
            int m = (int)(mouseX - (double)k);
            int n = (int)(mouseY - (double)l);
            for (RealmsAcceptRejectButton lv : buttons) {
                if (m < lv.x || m > lv.getRight() || n < lv.y || n > lv.getBottom()) continue;
                lv.handleClick(j);
            }
        }
    }
}

