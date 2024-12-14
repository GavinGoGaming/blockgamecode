/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 * 
 * Could not load the following classes:
 *  net.fabricmc.api.EnvType
 *  net.fabricmc.api.Environment
 */
package net.minecraft.client.gui.screen.advancement;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Identifier;

@Environment(value=EnvType.CLIENT)
enum AdvancementTabType {
    ABOVE(new Textures(Identifier.ofVanilla("advancements/tab_above_left_selected"), Identifier.ofVanilla("advancements/tab_above_middle_selected"), Identifier.ofVanilla("advancements/tab_above_right_selected")), new Textures(Identifier.ofVanilla("advancements/tab_above_left"), Identifier.ofVanilla("advancements/tab_above_middle"), Identifier.ofVanilla("advancements/tab_above_right")), 28, 32, 8),
    BELOW(new Textures(Identifier.ofVanilla("advancements/tab_below_left_selected"), Identifier.ofVanilla("advancements/tab_below_middle_selected"), Identifier.ofVanilla("advancements/tab_below_right_selected")), new Textures(Identifier.ofVanilla("advancements/tab_below_left"), Identifier.ofVanilla("advancements/tab_below_middle"), Identifier.ofVanilla("advancements/tab_below_right")), 28, 32, 8),
    LEFT(new Textures(Identifier.ofVanilla("advancements/tab_left_top_selected"), Identifier.ofVanilla("advancements/tab_left_middle_selected"), Identifier.ofVanilla("advancements/tab_left_bottom_selected")), new Textures(Identifier.ofVanilla("advancements/tab_left_top"), Identifier.ofVanilla("advancements/tab_left_middle"), Identifier.ofVanilla("advancements/tab_left_bottom")), 32, 28, 5),
    RIGHT(new Textures(Identifier.ofVanilla("advancements/tab_right_top_selected"), Identifier.ofVanilla("advancements/tab_right_middle_selected"), Identifier.ofVanilla("advancements/tab_right_bottom_selected")), new Textures(Identifier.ofVanilla("advancements/tab_right_top"), Identifier.ofVanilla("advancements/tab_right_middle"), Identifier.ofVanilla("advancements/tab_right_bottom")), 32, 28, 5);

    private final Textures selectedTextures;
    private final Textures unselectedTextures;
    private final int width;
    private final int height;
    private final int tabCount;

    private AdvancementTabType(Textures selectedTextures, Textures unselectedTextures, int width, int height, int tabCount) {
        this.selectedTextures = selectedTextures;
        this.unselectedTextures = unselectedTextures;
        this.width = width;
        this.height = height;
        this.tabCount = tabCount;
    }

    public int getTabCount() {
        return this.tabCount;
    }

    public void drawBackground(DrawContext context, int x, int y, boolean selected, int index) {
        Textures lv;
        Textures textures = lv = selected ? this.selectedTextures : this.unselectedTextures;
        Identifier lv2 = index == 0 ? lv.first() : (index == this.tabCount - 1 ? lv.last() : lv.middle());
        context.drawGuiTexture(lv2, x + this.getTabX(index), y + this.getTabY(index), this.width, this.height);
    }

    public void drawIcon(DrawContext context, int x, int y, int index, ItemStack stack) {
        int l = x + this.getTabX(index);
        int m = y + this.getTabY(index);
        switch (this.ordinal()) {
            case 0: {
                l += 6;
                m += 9;
                break;
            }
            case 1: {
                l += 6;
                m += 6;
                break;
            }
            case 2: {
                l += 10;
                m += 5;
                break;
            }
            case 3: {
                l += 6;
                m += 5;
            }
        }
        context.drawItemWithoutEntity(stack, l, m);
    }

    public int getTabX(int index) {
        switch (this.ordinal()) {
            case 0: {
                return (this.width + 4) * index;
            }
            case 1: {
                return (this.width + 4) * index;
            }
            case 2: {
                return -this.width + 4;
            }
            case 3: {
                return 248;
            }
        }
        throw new UnsupportedOperationException("Don't know what this tab type is!" + String.valueOf((Object)this));
    }

    public int getTabY(int index) {
        switch (this.ordinal()) {
            case 0: {
                return -this.height + 4;
            }
            case 1: {
                return 136;
            }
            case 2: {
                return this.height * index;
            }
            case 3: {
                return this.height * index;
            }
        }
        throw new UnsupportedOperationException("Don't know what this tab type is!" + String.valueOf((Object)this));
    }

    public boolean isClickOnTab(int screenX, int screenY, int index, double mouseX, double mouseY) {
        int l = screenX + this.getTabX(index);
        int m = screenY + this.getTabY(index);
        return mouseX > (double)l && mouseX < (double)(l + this.width) && mouseY > (double)m && mouseY < (double)(m + this.height);
    }

    @Environment(value=EnvType.CLIENT)
    record Textures(Identifier first, Identifier middle, Identifier last) {
    }
}

