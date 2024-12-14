/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 * 
 * Could not load the following classes:
 *  net.fabricmc.api.EnvType
 *  net.fabricmc.api.Environment
 *  org.jetbrains.annotations.Nullable
 */
package net.minecraft.client.gui;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.ScreenRect;
import net.minecraft.client.gui.navigation.GuiNavigation;
import net.minecraft.client.gui.navigation.GuiNavigationPath;
import net.minecraft.client.gui.navigation.Navigable;
import org.jetbrains.annotations.Nullable;

@Environment(value=EnvType.CLIENT)
public interface Element
extends Navigable {
    public static final long MAX_DOUBLE_CLICK_INTERVAL = 250L;

    default public void mouseMoved(double mouseX, double mouseY) {
    }

    default public boolean mouseClicked(double mouseX, double mouseY, int button) {
        return false;
    }

    default public boolean mouseReleased(double mouseX, double mouseY, int button) {
        return false;
    }

    default public boolean mouseDragged(double mouseX, double mouseY, int button, double deltaX, double deltaY) {
        return false;
    }

    default public boolean mouseScrolled(double mouseX, double mouseY, double horizontalAmount, double verticalAmount) {
        return false;
    }

    default public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        return false;
    }

    default public boolean keyReleased(int keyCode, int scanCode, int modifiers) {
        return false;
    }

    default public boolean charTyped(char chr, int modifiers) {
        return false;
    }

    @Nullable
    default public GuiNavigationPath getNavigationPath(GuiNavigation navigation) {
        return null;
    }

    default public boolean isMouseOver(double mouseX, double mouseY) {
        return false;
    }

    public void setFocused(boolean var1);

    public boolean isFocused();

    @Nullable
    default public GuiNavigationPath getFocusedPath() {
        if (this.isFocused()) {
            return GuiNavigationPath.of(this);
        }
        return null;
    }

    default public ScreenRect getNavigationFocus() {
        return ScreenRect.empty();
    }
}

