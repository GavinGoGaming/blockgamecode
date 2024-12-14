/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 * 
 * Could not load the following classes:
 *  net.fabricmc.api.EnvType
 *  net.fabricmc.api.Environment
 */
package net.minecraft.client.gui.tooltip;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.DrawContext;

@Environment(value=EnvType.CLIENT)
public class TooltipBackgroundRenderer {
    public static final int field_41688 = 12;
    private static final int field_41693 = 3;
    public static final int field_41689 = 3;
    public static final int field_41690 = 3;
    public static final int field_41691 = 3;
    public static final int field_41692 = 3;
    private static final int BACKGROUND_COLOR = -267386864;
    private static final int START_Y_BORDER_COLOR = 0x505000FF;
    private static final int END_Y_BORDER_COLOR = 1344798847;

    public static void render(DrawContext context, int x, int y, int width, int height, int z) {
        int n = x - 3;
        int o = y - 3;
        int p = width + 3 + 3;
        int q = height + 3 + 3;
        TooltipBackgroundRenderer.renderHorizontalLine(context, n, o - 1, p, z, -267386864);
        TooltipBackgroundRenderer.renderHorizontalLine(context, n, o + q, p, z, -267386864);
        TooltipBackgroundRenderer.renderRectangle(context, n, o, p, q, z, -267386864);
        TooltipBackgroundRenderer.renderVerticalLine(context, n - 1, o, q, z, -267386864);
        TooltipBackgroundRenderer.renderVerticalLine(context, n + p, o, q, z, -267386864);
        TooltipBackgroundRenderer.renderBorder(context, n, o + 1, p, q, z, 0x505000FF, 1344798847);
    }

    private static void renderBorder(DrawContext context, int x, int y, int width, int height, int z, int startColor, int endColor) {
        TooltipBackgroundRenderer.renderVerticalLine(context, x, y, height - 2, z, startColor, endColor);
        TooltipBackgroundRenderer.renderVerticalLine(context, x + width - 1, y, height - 2, z, startColor, endColor);
        TooltipBackgroundRenderer.renderHorizontalLine(context, x, y - 1, width, z, startColor);
        TooltipBackgroundRenderer.renderHorizontalLine(context, x, y - 1 + height - 1, width, z, endColor);
    }

    private static void renderVerticalLine(DrawContext context, int x, int y, int height, int z, int color) {
        context.fill(x, y, x + 1, y + height, z, color);
    }

    private static void renderVerticalLine(DrawContext context, int x, int y, int height, int z, int startColor, int endColor) {
        context.fillGradient(x, y, x + 1, y + height, z, startColor, endColor);
    }

    private static void renderHorizontalLine(DrawContext context, int x, int y, int width, int z, int color) {
        context.fill(x, y, x + width, y + 1, z, color);
    }

    private static void renderRectangle(DrawContext context, int x, int y, int width, int height, int z, int color) {
        context.fill(x, y, x + width, y + height, z, color);
    }
}

