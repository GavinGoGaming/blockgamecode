/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 * 
 * Could not load the following classes:
 *  net.fabricmc.api.EnvType
 *  net.fabricmc.api.Environment
 */
package net.minecraft.client.gui.widget;

import java.util.OptionalInt;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.font.MultilineText;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.widget.AbstractTextWidget;
import net.minecraft.text.Text;
import net.minecraft.util.CachedMapper;
import net.minecraft.util.Util;

@Environment(value=EnvType.CLIENT)
public class MultilineTextWidget
extends AbstractTextWidget {
    private OptionalInt maxWidth = OptionalInt.empty();
    private OptionalInt maxRows = OptionalInt.empty();
    private final CachedMapper<CacheKey, MultilineText> cacheKeyToText = Util.cachedMapper(cacheKey -> {
        if (cacheKey.maxRows.isPresent()) {
            return MultilineText.create(textRenderer, cacheKey.maxWidth, cacheKey.maxRows.getAsInt(), cacheKey.message);
        }
        return MultilineText.create(textRenderer, cacheKey.message, cacheKey.maxWidth);
    });
    private boolean centered = false;

    public MultilineTextWidget(Text message, TextRenderer textRenderer) {
        this(0, 0, message, textRenderer);
    }

    public MultilineTextWidget(int x, int y, Text message, TextRenderer textRenderer) {
        super(x, y, 0, 0, message, textRenderer);
        this.active = false;
    }

    @Override
    public MultilineTextWidget setTextColor(int i) {
        super.setTextColor(i);
        return this;
    }

    public MultilineTextWidget setMaxWidth(int maxWidth) {
        this.maxWidth = OptionalInt.of(maxWidth);
        return this;
    }

    public MultilineTextWidget setMaxRows(int maxRows) {
        this.maxRows = OptionalInt.of(maxRows);
        return this;
    }

    public MultilineTextWidget setCentered(boolean centered) {
        this.centered = centered;
        return this;
    }

    @Override
    public int getWidth() {
        return this.cacheKeyToText.map(this.getCacheKey()).getMaxWidth();
    }

    @Override
    public int getHeight() {
        return this.cacheKeyToText.map(this.getCacheKey()).count() * this.getTextRenderer().fontHeight;
    }

    @Override
    public void renderWidget(DrawContext context, int mouseX, int mouseY, float delta) {
        MultilineText lv = this.cacheKeyToText.map(this.getCacheKey());
        int k = this.getX();
        int l = this.getY();
        int m = this.getTextRenderer().fontHeight;
        int n = this.getTextColor();
        if (this.centered) {
            lv.drawCenterWithShadow(context, k + this.getWidth() / 2, l, m, n);
        } else {
            lv.drawWithShadow(context, k, l, m, n);
        }
    }

    private CacheKey getCacheKey() {
        return new CacheKey(this.getMessage(), this.maxWidth.orElse(Integer.MAX_VALUE), this.maxRows);
    }

    @Override
    public /* synthetic */ AbstractTextWidget setTextColor(int textColor) {
        return this.setTextColor(textColor);
    }

    @Environment(value=EnvType.CLIENT)
    record CacheKey(Text message, int maxWidth, OptionalInt maxRows) {
    }
}

