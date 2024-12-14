/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 * 
 * Could not load the following classes:
 *  net.fabricmc.api.EnvType
 *  net.fabricmc.api.Environment
 */
package net.minecraft.client.gui.screen.ingame;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.screen.BrewingStandScreenHandler;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.MathHelper;

@Environment(value=EnvType.CLIENT)
public class BrewingStandScreen
extends HandledScreen<BrewingStandScreenHandler> {
    private static final Identifier FUEL_LENGTH_TEXTURE = Identifier.ofVanilla("container/brewing_stand/fuel_length");
    private static final Identifier BREW_PROGRESS_TEXTURE = Identifier.ofVanilla("container/brewing_stand/brew_progress");
    private static final Identifier BUBBLES_TEXTURE = Identifier.ofVanilla("container/brewing_stand/bubbles");
    private static final Identifier TEXTURE = Identifier.ofVanilla("textures/gui/container/brewing_stand.png");
    private static final int[] BUBBLE_PROGRESS = new int[]{29, 24, 20, 16, 11, 6, 0};

    public BrewingStandScreen(BrewingStandScreenHandler handler, PlayerInventory inventory, Text title) {
        super(handler, inventory, title);
    }

    @Override
    protected void init() {
        super.init();
        this.titleX = (this.backgroundWidth - this.textRenderer.getWidth(this.title)) / 2;
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        super.render(context, mouseX, mouseY, delta);
        this.drawMouseoverTooltip(context, mouseX, mouseY);
    }

    @Override
    protected void drawBackground(DrawContext context, float delta, int mouseX, int mouseY) {
        int o;
        int k = (this.width - this.backgroundWidth) / 2;
        int l = (this.height - this.backgroundHeight) / 2;
        context.drawTexture(TEXTURE, k, l, 0, 0, this.backgroundWidth, this.backgroundHeight);
        int m = ((BrewingStandScreenHandler)this.handler).getFuel();
        int n = MathHelper.clamp((18 * m + 20 - 1) / 20, 0, 18);
        if (n > 0) {
            context.drawGuiTexture(FUEL_LENGTH_TEXTURE, 18, 4, 0, 0, k + 60, l + 44, n, 4);
        }
        if ((o = ((BrewingStandScreenHandler)this.handler).getBrewTime()) > 0) {
            int p = (int)(28.0f * (1.0f - (float)o / 400.0f));
            if (p > 0) {
                context.drawGuiTexture(BREW_PROGRESS_TEXTURE, 9, 28, 0, 0, k + 97, l + 16, 9, p);
            }
            if ((p = BUBBLE_PROGRESS[o / 2 % 7]) > 0) {
                context.drawGuiTexture(BUBBLES_TEXTURE, 12, 29, 0, 29 - p, k + 63, l + 14 + 29 - p, 12, p);
            }
        }
    }
}

