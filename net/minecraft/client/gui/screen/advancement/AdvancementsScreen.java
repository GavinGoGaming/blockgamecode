/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 * 
 * Could not load the following classes:
 *  net.fabricmc.api.EnvType
 *  net.fabricmc.api.Environment
 *  org.jetbrains.annotations.Nullable
 */
package net.minecraft.client.gui.screen.advancement;

import com.google.common.collect.Maps;
import com.mojang.blaze3d.systems.RenderSystem;
import java.util.Map;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.advancement.AdvancementEntry;
import net.minecraft.advancement.AdvancementProgress;
import net.minecraft.advancement.PlacedAdvancement;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.advancement.AdvancementTab;
import net.minecraft.client.gui.screen.advancement.AdvancementWidget;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.ClickableWidget;
import net.minecraft.client.gui.widget.ThreePartsLayoutWidget;
import net.minecraft.client.network.ClientAdvancementManager;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.network.packet.c2s.play.AdvancementTabC2SPacket;
import net.minecraft.screen.ScreenTexts;
import net.minecraft.text.Text;
import net.minecraft.util.Colors;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.Nullable;

@Environment(value=EnvType.CLIENT)
public class AdvancementsScreen
extends Screen
implements ClientAdvancementManager.Listener {
    private static final Identifier WINDOW_TEXTURE = Identifier.ofVanilla("textures/gui/advancements/window.png");
    public static final int WINDOW_WIDTH = 252;
    public static final int WINDOW_HEIGHT = 140;
    private static final int PAGE_OFFSET_X = 9;
    private static final int PAGE_OFFSET_Y = 18;
    public static final int PAGE_WIDTH = 234;
    public static final int PAGE_HEIGHT = 113;
    private static final int TITLE_OFFSET_X = 8;
    private static final int TITLE_OFFSET_Y = 6;
    public static final int field_32302 = 16;
    public static final int field_32303 = 16;
    public static final int field_32304 = 14;
    public static final int field_32305 = 7;
    private static final double field_45431 = 16.0;
    private static final Text SAD_LABEL_TEXT = Text.translatable("advancements.sad_label");
    private static final Text EMPTY_TEXT = Text.translatable("advancements.empty");
    private static final Text ADVANCEMENTS_TEXT = Text.translatable("gui.advancements");
    private final ThreePartsLayoutWidget layout = new ThreePartsLayoutWidget(this);
    @Nullable
    private final Screen parent;
    private final ClientAdvancementManager advancementHandler;
    private final Map<AdvancementEntry, AdvancementTab> tabs = Maps.newLinkedHashMap();
    @Nullable
    private AdvancementTab selectedTab;
    private boolean movingTab;

    public AdvancementsScreen(ClientAdvancementManager advancementHandler) {
        this(advancementHandler, null);
    }

    public AdvancementsScreen(ClientAdvancementManager advancementHandler, @Nullable Screen parent) {
        super(ADVANCEMENTS_TEXT);
        this.advancementHandler = advancementHandler;
        this.parent = parent;
    }

    @Override
    protected void init() {
        this.layout.addHeader(ADVANCEMENTS_TEXT, this.textRenderer);
        this.tabs.clear();
        this.selectedTab = null;
        this.advancementHandler.setListener(this);
        if (this.selectedTab == null && !this.tabs.isEmpty()) {
            AdvancementTab lv = this.tabs.values().iterator().next();
            this.advancementHandler.selectTab(lv.getRoot().getAdvancementEntry(), true);
        } else {
            this.advancementHandler.selectTab(this.selectedTab == null ? null : this.selectedTab.getRoot().getAdvancementEntry(), true);
        }
        this.layout.addFooter(ButtonWidget.builder(ScreenTexts.DONE, arg -> this.close()).width(200).build());
        this.layout.forEachChild(arg2 -> {
            ClickableWidget cfr_ignored_0 = (ClickableWidget)this.addDrawableChild(arg2);
        });
        this.initTabNavigation();
    }

    @Override
    protected void initTabNavigation() {
        this.layout.refreshPositions();
    }

    @Override
    public void close() {
        this.client.setScreen(this.parent);
    }

    @Override
    public void removed() {
        this.advancementHandler.setListener(null);
        ClientPlayNetworkHandler lv = this.client.getNetworkHandler();
        if (lv != null) {
            lv.sendPacket(AdvancementTabC2SPacket.close());
        }
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (button == 0) {
            int j = (this.width - 252) / 2;
            int k = (this.height - 140) / 2;
            for (AdvancementTab lv : this.tabs.values()) {
                if (!lv.isClickOnTab(j, k, mouseX, mouseY)) continue;
                this.advancementHandler.selectTab(lv.getRoot().getAdvancementEntry(), true);
                break;
            }
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (this.client.options.advancementsKey.matchesKey(keyCode, scanCode)) {
            this.client.setScreen(null);
            this.client.mouse.lockCursor();
            return true;
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        super.render(context, mouseX, mouseY, delta);
        int k = (this.width - 252) / 2;
        int l = (this.height - 140) / 2;
        this.drawAdvancementTree(context, mouseX, mouseY, k, l);
        this.drawWindow(context, k, l);
        this.drawWidgetTooltip(context, mouseX, mouseY, k, l);
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double deltaX, double deltaY) {
        if (button != 0) {
            this.movingTab = false;
            return false;
        }
        if (!this.movingTab) {
            this.movingTab = true;
        } else if (this.selectedTab != null) {
            this.selectedTab.move(deltaX, deltaY);
        }
        return true;
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double horizontalAmount, double verticalAmount) {
        if (this.selectedTab != null) {
            this.selectedTab.move(horizontalAmount * 16.0, verticalAmount * 16.0);
            return true;
        }
        return false;
    }

    private void drawAdvancementTree(DrawContext context, int mouseX, int mouseY, int x, int y) {
        AdvancementTab lv = this.selectedTab;
        if (lv == null) {
            context.fill(x + 9, y + 18, x + 9 + 234, y + 18 + 113, Colors.BLACK);
            int m = x + 9 + 117;
            context.drawCenteredTextWithShadow(this.textRenderer, EMPTY_TEXT, m, y + 18 + 56 - this.textRenderer.fontHeight / 2, Colors.WHITE);
            context.drawCenteredTextWithShadow(this.textRenderer, SAD_LABEL_TEXT, m, y + 18 + 113 - this.textRenderer.fontHeight, Colors.WHITE);
            return;
        }
        lv.render(context, x + 9, y + 18);
    }

    public void drawWindow(DrawContext context, int x, int y) {
        RenderSystem.enableBlend();
        context.drawTexture(WINDOW_TEXTURE, x, y, 0, 0, 252, 140);
        if (this.tabs.size() > 1) {
            for (AdvancementTab lv : this.tabs.values()) {
                lv.drawBackground(context, x, y, lv == this.selectedTab);
            }
            for (AdvancementTab lv : this.tabs.values()) {
                lv.drawIcon(context, x, y);
            }
        }
        context.drawText(this.textRenderer, this.selectedTab != null ? this.selectedTab.getTitle() : ADVANCEMENTS_TEXT, x + 8, y + 6, 0x404040, false);
    }

    private void drawWidgetTooltip(DrawContext context, int mouseX, int mouseY, int x, int y) {
        if (this.selectedTab != null) {
            context.getMatrices().push();
            context.getMatrices().translate(x + 9, y + 18, 400.0f);
            RenderSystem.enableDepthTest();
            this.selectedTab.drawWidgetTooltip(context, mouseX - x - 9, mouseY - y - 18, x, y);
            RenderSystem.disableDepthTest();
            context.getMatrices().pop();
        }
        if (this.tabs.size() > 1) {
            for (AdvancementTab lv : this.tabs.values()) {
                if (!lv.isClickOnTab(x, y, mouseX, mouseY)) continue;
                context.drawTooltip(this.textRenderer, lv.getTitle(), mouseX, mouseY);
            }
        }
    }

    @Override
    public void onRootAdded(PlacedAdvancement root) {
        AdvancementTab lv = AdvancementTab.create(this.client, this, this.tabs.size(), root);
        if (lv == null) {
            return;
        }
        this.tabs.put(root.getAdvancementEntry(), lv);
    }

    @Override
    public void onRootRemoved(PlacedAdvancement root) {
    }

    @Override
    public void onDependentAdded(PlacedAdvancement dependent) {
        AdvancementTab lv = this.getTab(dependent);
        if (lv != null) {
            lv.addAdvancement(dependent);
        }
    }

    @Override
    public void onDependentRemoved(PlacedAdvancement dependent) {
    }

    @Override
    public void setProgress(PlacedAdvancement advancement, AdvancementProgress progress) {
        AdvancementWidget lv = this.getAdvancementWidget(advancement);
        if (lv != null) {
            lv.setProgress(progress);
        }
    }

    @Override
    public void selectTab(@Nullable AdvancementEntry advancement) {
        this.selectedTab = this.tabs.get(advancement);
    }

    @Override
    public void onClear() {
        this.tabs.clear();
        this.selectedTab = null;
    }

    @Nullable
    public AdvancementWidget getAdvancementWidget(PlacedAdvancement advancement) {
        AdvancementTab lv = this.getTab(advancement);
        return lv == null ? null : lv.getWidget(advancement.getAdvancementEntry());
    }

    @Nullable
    private AdvancementTab getTab(PlacedAdvancement advancement) {
        PlacedAdvancement lv = advancement.getRoot();
        return this.tabs.get(lv.getAdvancementEntry());
    }
}

