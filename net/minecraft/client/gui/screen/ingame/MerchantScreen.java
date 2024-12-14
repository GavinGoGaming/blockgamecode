/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 * 
 * Could not load the following classes:
 *  net.fabricmc.api.EnvType
 *  net.fabricmc.api.Environment
 */
package net.minecraft.client.gui.screen.ingame;

import com.mojang.blaze3d.systems.RenderSystem;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.network.packet.c2s.play.SelectMerchantTradeC2SPacket;
import net.minecraft.screen.MerchantScreenHandler;
import net.minecraft.screen.ScreenTexts;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.MathHelper;
import net.minecraft.village.TradeOffer;
import net.minecraft.village.TradeOfferList;
import net.minecraft.village.VillagerData;

@Environment(value=EnvType.CLIENT)
public class MerchantScreen
extends HandledScreen<MerchantScreenHandler> {
    private static final Identifier OUT_OF_STOCK_TEXTURE = Identifier.ofVanilla("container/villager/out_of_stock");
    private static final Identifier EXPERIENCE_BAR_BACKGROUND_TEXTURE = Identifier.ofVanilla("container/villager/experience_bar_background");
    private static final Identifier EXPERIENCE_BAR_CURRENT_TEXTURE = Identifier.ofVanilla("container/villager/experience_bar_current");
    private static final Identifier EXPERIENCE_BAR_RESULT_TEXTURE = Identifier.ofVanilla("container/villager/experience_bar_result");
    private static final Identifier SCROLLER_TEXTURE = Identifier.ofVanilla("container/villager/scroller");
    private static final Identifier SCROLLER_DISABLED_TEXTURE = Identifier.ofVanilla("container/villager/scroller_disabled");
    private static final Identifier TRADE_ARROW_OUT_OF_STOCK_TEXTURE = Identifier.ofVanilla("container/villager/trade_arrow_out_of_stock");
    private static final Identifier TRADE_ARROW_TEXTURE = Identifier.ofVanilla("container/villager/trade_arrow");
    private static final Identifier DISCOUNT_STRIKETHROUGH_TEXTURE = Identifier.ofVanilla("container/villager/discount_strikethrough");
    private static final Identifier TEXTURE = Identifier.ofVanilla("textures/gui/container/villager.png");
    private static final int TEXTURE_WIDTH = 512;
    private static final int TEXTURE_HEIGHT = 256;
    private static final int field_32356 = 99;
    private static final int XP_BAR_X_OFFSET = 136;
    private static final int TRADE_LIST_AREA_Y_OFFSET = 16;
    private static final int FIRST_BUY_ITEM_X_OFFSET = 5;
    private static final int SECOND_BUY_ITEM_X_OFFSET = 35;
    private static final int SOLD_ITEM_X_OFFSET = 68;
    private static final int field_32362 = 6;
    private static final int MAX_TRADE_OFFERS = 7;
    private static final int field_32364 = 5;
    private static final int TRADE_OFFER_BUTTON_HEIGHT = 20;
    private static final int TRADE_OFFER_BUTTON_WIDTH = 88;
    private static final int SCROLLBAR_HEIGHT = 27;
    private static final int SCROLLBAR_WIDTH = 6;
    private static final int SCROLLBAR_AREA_HEIGHT = 139;
    private static final int SCROLLBAR_OFFSET_Y = 18;
    private static final int SCROLLBAR_OFFSET_X = 94;
    private static final Text TRADES_TEXT = Text.translatable("merchant.trades");
    private static final Text DEPRECATED_TEXT = Text.translatable("merchant.deprecated");
    private int selectedIndex;
    private final WidgetButtonPage[] offers = new WidgetButtonPage[7];
    int indexStartOffset;
    private boolean scrolling;

    public MerchantScreen(MerchantScreenHandler handler, PlayerInventory inventory, Text title) {
        super(handler, inventory, title);
        this.backgroundWidth = 276;
        this.playerInventoryTitleX = 107;
    }

    private void syncRecipeIndex() {
        ((MerchantScreenHandler)this.handler).setRecipeIndex(this.selectedIndex);
        ((MerchantScreenHandler)this.handler).switchTo(this.selectedIndex);
        this.client.getNetworkHandler().sendPacket(new SelectMerchantTradeC2SPacket(this.selectedIndex));
    }

    @Override
    protected void init() {
        super.init();
        int i = (this.width - this.backgroundWidth) / 2;
        int j = (this.height - this.backgroundHeight) / 2;
        int k = j + 16 + 2;
        for (int l = 0; l < 7; ++l) {
            this.offers[l] = this.addDrawableChild(new WidgetButtonPage(i + 5, k, l, button -> {
                if (button instanceof WidgetButtonPage) {
                    this.selectedIndex = ((WidgetButtonPage)button).getIndex() + this.indexStartOffset;
                    this.syncRecipeIndex();
                }
            }));
            k += 20;
        }
    }

    @Override
    protected void drawForeground(DrawContext context, int mouseX, int mouseY) {
        int k = ((MerchantScreenHandler)this.handler).getLevelProgress();
        if (k > 0 && k <= 5 && ((MerchantScreenHandler)this.handler).isLeveled()) {
            MutableText lv = Text.translatable("merchant.title", this.title, Text.translatable("merchant.level." + k));
            int l = this.textRenderer.getWidth(lv);
            int m = 49 + this.backgroundWidth / 2 - l / 2;
            context.drawText(this.textRenderer, lv, m, 6, 0x404040, false);
        } else {
            context.drawText(this.textRenderer, this.title, 49 + this.backgroundWidth / 2 - this.textRenderer.getWidth(this.title) / 2, 6, 0x404040, false);
        }
        context.drawText(this.textRenderer, this.playerInventoryTitle, this.playerInventoryTitleX, this.playerInventoryTitleY, 0x404040, false);
        int n = this.textRenderer.getWidth(TRADES_TEXT);
        context.drawText(this.textRenderer, TRADES_TEXT, 5 - n / 2 + 48, 6, 0x404040, false);
    }

    @Override
    protected void drawBackground(DrawContext context, float delta, int mouseX, int mouseY) {
        int k = (this.width - this.backgroundWidth) / 2;
        int l = (this.height - this.backgroundHeight) / 2;
        context.drawTexture(TEXTURE, k, l, 0, 0.0f, 0.0f, this.backgroundWidth, this.backgroundHeight, 512, 256);
        TradeOfferList lv = ((MerchantScreenHandler)this.handler).getRecipes();
        if (!lv.isEmpty()) {
            int m = this.selectedIndex;
            if (m < 0 || m >= lv.size()) {
                return;
            }
            TradeOffer lv2 = (TradeOffer)lv.get(m);
            if (lv2.isDisabled()) {
                context.drawGuiTexture(OUT_OF_STOCK_TEXTURE, this.x + 83 + 99, this.y + 35, 0, 28, 21);
            }
        }
    }

    private void drawLevelInfo(DrawContext context, int x, int y, TradeOffer tradeOffer) {
        int k = ((MerchantScreenHandler)this.handler).getLevelProgress();
        int l = ((MerchantScreenHandler)this.handler).getExperience();
        if (k >= 5) {
            return;
        }
        context.drawGuiTexture(EXPERIENCE_BAR_BACKGROUND_TEXTURE, x + 136, y + 16, 0, 102, 5);
        int m = VillagerData.getLowerLevelExperience(k);
        if (l < m || !VillagerData.canLevelUp(k)) {
            return;
        }
        int n = 102;
        float f = 102.0f / (float)(VillagerData.getUpperLevelExperience(k) - m);
        int o = Math.min(MathHelper.floor(f * (float)(l - m)), 102);
        context.drawGuiTexture(EXPERIENCE_BAR_CURRENT_TEXTURE, 102, 5, 0, 0, x + 136, y + 16, 0, o, 5);
        int p = ((MerchantScreenHandler)this.handler).getMerchantRewardedExperience();
        if (p > 0) {
            int q = Math.min(MathHelper.floor((float)p * f), 102 - o);
            context.drawGuiTexture(EXPERIENCE_BAR_RESULT_TEXTURE, 102, 5, o, 0, x + 136 + o, y + 16, 0, q, 5);
        }
    }

    private void renderScrollbar(DrawContext context, int x, int y, TradeOfferList tradeOffers) {
        int k = tradeOffers.size() + 1 - 7;
        if (k > 1) {
            int l = 139 - (27 + (k - 1) * 139 / k);
            int m = 1 + l / k + 139 / k;
            int n = 113;
            int o = Math.min(113, this.indexStartOffset * m);
            if (this.indexStartOffset == k - 1) {
                o = 113;
            }
            context.drawGuiTexture(SCROLLER_TEXTURE, x + 94, y + 18 + o, 0, 6, 27);
        } else {
            context.drawGuiTexture(SCROLLER_DISABLED_TEXTURE, x + 94, y + 18, 0, 6, 27);
        }
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        super.render(context, mouseX, mouseY, delta);
        TradeOfferList lv = ((MerchantScreenHandler)this.handler).getRecipes();
        if (!lv.isEmpty()) {
            TradeOffer lv22;
            int k = (this.width - this.backgroundWidth) / 2;
            int l = (this.height - this.backgroundHeight) / 2;
            int m = l + 16 + 1;
            int n = k + 5 + 5;
            this.renderScrollbar(context, k, l, lv);
            int o = 0;
            for (TradeOffer lv22 : lv) {
                if (this.canScroll(lv.size()) && (o < this.indexStartOffset || o >= 7 + this.indexStartOffset)) {
                    ++o;
                    continue;
                }
                ItemStack lv3 = lv22.getOriginalFirstBuyItem();
                ItemStack lv4 = lv22.getDisplayedFirstBuyItem();
                ItemStack lv5 = lv22.getDisplayedSecondBuyItem();
                ItemStack lv6 = lv22.getSellItem();
                context.getMatrices().push();
                context.getMatrices().translate(0.0f, 0.0f, 100.0f);
                int p = m + 2;
                this.renderFirstBuyItem(context, lv4, lv3, n, p);
                if (!lv5.isEmpty()) {
                    context.drawItemWithoutEntity(lv5, k + 5 + 35, p);
                    context.drawItemInSlot(this.textRenderer, lv5, k + 5 + 35, p);
                }
                this.renderArrow(context, lv22, k, p);
                context.drawItemWithoutEntity(lv6, k + 5 + 68, p);
                context.drawItemInSlot(this.textRenderer, lv6, k + 5 + 68, p);
                context.getMatrices().pop();
                m += 20;
                ++o;
            }
            int q = this.selectedIndex;
            lv22 = (TradeOffer)lv.get(q);
            if (((MerchantScreenHandler)this.handler).isLeveled()) {
                this.drawLevelInfo(context, k, l, lv22);
            }
            if (lv22.isDisabled() && this.isPointWithinBounds(186, 35, 22, 21, mouseX, mouseY) && ((MerchantScreenHandler)this.handler).canRefreshTrades()) {
                context.drawTooltip(this.textRenderer, DEPRECATED_TEXT, mouseX, mouseY);
            }
            for (WidgetButtonPage lv7 : this.offers) {
                if (lv7.isSelected()) {
                    lv7.renderTooltip(context, mouseX, mouseY);
                }
                lv7.visible = lv7.index < ((MerchantScreenHandler)this.handler).getRecipes().size();
            }
            RenderSystem.enableDepthTest();
        }
        this.drawMouseoverTooltip(context, mouseX, mouseY);
    }

    private void renderArrow(DrawContext context, TradeOffer tradeOffer, int x, int y) {
        RenderSystem.enableBlend();
        if (tradeOffer.isDisabled()) {
            context.drawGuiTexture(TRADE_ARROW_OUT_OF_STOCK_TEXTURE, x + 5 + 35 + 20, y + 3, 0, 10, 9);
        } else {
            context.drawGuiTexture(TRADE_ARROW_TEXTURE, x + 5 + 35 + 20, y + 3, 0, 10, 9);
        }
    }

    private void renderFirstBuyItem(DrawContext context, ItemStack adjustedFirstBuyItem, ItemStack originalFirstBuyItem, int x, int y) {
        context.drawItemWithoutEntity(adjustedFirstBuyItem, x, y);
        if (originalFirstBuyItem.getCount() == adjustedFirstBuyItem.getCount()) {
            context.drawItemInSlot(this.textRenderer, adjustedFirstBuyItem, x, y);
        } else {
            context.drawItemInSlot(this.textRenderer, originalFirstBuyItem, x, y, originalFirstBuyItem.getCount() == 1 ? "1" : null);
            context.drawItemInSlot(this.textRenderer, adjustedFirstBuyItem, x + 14, y, adjustedFirstBuyItem.getCount() == 1 ? "1" : null);
            context.getMatrices().push();
            context.getMatrices().translate(0.0f, 0.0f, 300.0f);
            context.drawGuiTexture(DISCOUNT_STRIKETHROUGH_TEXTURE, x + 7, y + 12, 0, 9, 2);
            context.getMatrices().pop();
        }
    }

    private boolean canScroll(int listSize) {
        return listSize > 7;
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double horizontalAmount, double verticalAmount) {
        int i = ((MerchantScreenHandler)this.handler).getRecipes().size();
        if (this.canScroll(i)) {
            int j = i - 7;
            this.indexStartOffset = MathHelper.clamp((int)((double)this.indexStartOffset - verticalAmount), 0, j);
        }
        return true;
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double deltaX, double deltaY) {
        int j = ((MerchantScreenHandler)this.handler).getRecipes().size();
        if (this.scrolling) {
            int k = this.y + 18;
            int l = k + 139;
            int m = j - 7;
            float h = ((float)mouseY - (float)k - 13.5f) / ((float)(l - k) - 27.0f);
            h = h * (float)m + 0.5f;
            this.indexStartOffset = MathHelper.clamp((int)h, 0, m);
            return true;
        }
        return super.mouseDragged(mouseX, mouseY, button, deltaX, deltaY);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        this.scrolling = false;
        int j = (this.width - this.backgroundWidth) / 2;
        int k = (this.height - this.backgroundHeight) / 2;
        if (this.canScroll(((MerchantScreenHandler)this.handler).getRecipes().size()) && mouseX > (double)(j + 94) && mouseX < (double)(j + 94 + 6) && mouseY > (double)(k + 18) && mouseY <= (double)(k + 18 + 139 + 1)) {
            this.scrolling = true;
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Environment(value=EnvType.CLIENT)
    class WidgetButtonPage
    extends ButtonWidget {
        final int index;

        public WidgetButtonPage(int x, int y, int index, ButtonWidget.PressAction onPress) {
            super(x, y, 88, 20, ScreenTexts.EMPTY, onPress, DEFAULT_NARRATION_SUPPLIER);
            this.index = index;
            this.visible = false;
        }

        public int getIndex() {
            return this.index;
        }

        public void renderTooltip(DrawContext context, int x, int y) {
            if (this.hovered && ((MerchantScreenHandler)MerchantScreen.this.handler).getRecipes().size() > this.index + MerchantScreen.this.indexStartOffset) {
                if (x < this.getX() + 20) {
                    ItemStack lv = ((TradeOffer)((MerchantScreenHandler)MerchantScreen.this.handler).getRecipes().get(this.index + MerchantScreen.this.indexStartOffset)).getDisplayedFirstBuyItem();
                    context.drawItemTooltip(MerchantScreen.this.textRenderer, lv, x, y);
                } else if (x < this.getX() + 50 && x > this.getX() + 30) {
                    ItemStack lv = ((TradeOffer)((MerchantScreenHandler)MerchantScreen.this.handler).getRecipes().get(this.index + MerchantScreen.this.indexStartOffset)).getDisplayedSecondBuyItem();
                    if (!lv.isEmpty()) {
                        context.drawItemTooltip(MerchantScreen.this.textRenderer, lv, x, y);
                    }
                } else if (x > this.getX() + 65) {
                    ItemStack lv = ((TradeOffer)((MerchantScreenHandler)MerchantScreen.this.handler).getRecipes().get(this.index + MerchantScreen.this.indexStartOffset)).getSellItem();
                    context.drawItemTooltip(MerchantScreen.this.textRenderer, lv, x, y);
                }
            }
        }
    }
}

