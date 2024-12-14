/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 * 
 * Could not load the following classes:
 *  net.fabricmc.api.EnvType
 *  net.fabricmc.api.Environment
 *  org.jetbrains.annotations.Nullable
 */
package net.minecraft.client.gui.screen.ingame;

import java.util.List;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.block.entity.BannerPattern;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.client.model.ModelPart;
import net.minecraft.client.render.DiffuseLighting;
import net.minecraft.client.render.OverlayTexture;
import net.minecraft.client.render.block.entity.BannerBlockEntityRenderer;
import net.minecraft.client.render.entity.model.EntityModelLayers;
import net.minecraft.client.render.model.ModelLoader;
import net.minecraft.client.sound.PositionedSoundInstance;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.BannerPatternsComponent;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.BannerItem;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.screen.LoomScreenHandler;
import net.minecraft.screen.slot.Slot;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.DyeColor;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.MathHelper;
import org.jetbrains.annotations.Nullable;

@Environment(value=EnvType.CLIENT)
public class LoomScreen
extends HandledScreen<LoomScreenHandler> {
    private static final Identifier BANNER_SLOT_TEXTURE = Identifier.ofVanilla("container/loom/banner_slot");
    private static final Identifier DYE_SLOT_TEXTURE = Identifier.ofVanilla("container/loom/dye_slot");
    private static final Identifier PATTERN_SLOT_TEXTURE = Identifier.ofVanilla("container/loom/pattern_slot");
    private static final Identifier SCROLLER_TEXTURE = Identifier.ofVanilla("container/loom/scroller");
    private static final Identifier SCROLLER_DISABLED_TEXTURE = Identifier.ofVanilla("container/loom/scroller_disabled");
    private static final Identifier PATTERN_SELECTED_TEXTURE = Identifier.ofVanilla("container/loom/pattern_selected");
    private static final Identifier PATTERN_HIGHLIGHTED_TEXTURE = Identifier.ofVanilla("container/loom/pattern_highlighted");
    private static final Identifier PATTERN_TEXTURE = Identifier.ofVanilla("container/loom/pattern");
    private static final Identifier ERROR_TEXTURE = Identifier.ofVanilla("container/loom/error");
    private static final Identifier TEXTURE = Identifier.ofVanilla("textures/gui/container/loom.png");
    private static final int PATTERN_LIST_COLUMNS = 4;
    private static final int PATTERN_LIST_ROWS = 4;
    private static final int SCROLLBAR_WIDTH = 12;
    private static final int SCROLLBAR_HEIGHT = 15;
    private static final int PATTERN_ENTRY_SIZE = 14;
    private static final int SCROLLBAR_AREA_HEIGHT = 56;
    private static final int PATTERN_LIST_OFFSET_X = 60;
    private static final int PATTERN_LIST_OFFSET_Y = 13;
    private ModelPart bannerField;
    @Nullable
    private BannerPatternsComponent bannerPatterns;
    private ItemStack banner = ItemStack.EMPTY;
    private ItemStack dye = ItemStack.EMPTY;
    private ItemStack pattern = ItemStack.EMPTY;
    private boolean canApplyDyePattern;
    private boolean hasTooManyPatterns;
    private float scrollPosition;
    private boolean scrollbarClicked;
    private int visibleTopRow;

    public LoomScreen(LoomScreenHandler screenHandler, PlayerInventory inventory, Text title) {
        super(screenHandler, inventory, title);
        screenHandler.setInventoryChangeListener(this::onInventoryChanged);
        this.titleY -= 2;
    }

    @Override
    protected void init() {
        super.init();
        this.bannerField = this.client.getEntityModelLoader().getModelPart(EntityModelLayers.BANNER).getChild("flag");
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        super.render(context, mouseX, mouseY, delta);
        this.drawMouseoverTooltip(context, mouseX, mouseY);
    }

    private int getRows() {
        return MathHelper.ceilDiv(((LoomScreenHandler)this.handler).getBannerPatterns().size(), 4);
    }

    @Override
    protected void drawBackground(DrawContext context, float delta, int mouseX, int mouseY) {
        int k = this.x;
        int l = this.y;
        context.drawTexture(TEXTURE, k, l, 0, 0, this.backgroundWidth, this.backgroundHeight);
        Slot lv = ((LoomScreenHandler)this.handler).getBannerSlot();
        Slot lv2 = ((LoomScreenHandler)this.handler).getDyeSlot();
        Slot lv3 = ((LoomScreenHandler)this.handler).getPatternSlot();
        Slot lv4 = ((LoomScreenHandler)this.handler).getOutputSlot();
        if (!lv.hasStack()) {
            context.drawGuiTexture(BANNER_SLOT_TEXTURE, k + lv.x, l + lv.y, 16, 16);
        }
        if (!lv2.hasStack()) {
            context.drawGuiTexture(DYE_SLOT_TEXTURE, k + lv2.x, l + lv2.y, 16, 16);
        }
        if (!lv3.hasStack()) {
            context.drawGuiTexture(PATTERN_SLOT_TEXTURE, k + lv3.x, l + lv3.y, 16, 16);
        }
        int m = (int)(41.0f * this.scrollPosition);
        Identifier lv5 = this.canApplyDyePattern ? SCROLLER_TEXTURE : SCROLLER_DISABLED_TEXTURE;
        context.drawGuiTexture(lv5, k + 119, l + 13 + m, 12, 15);
        DiffuseLighting.disableGuiDepthLighting();
        if (this.bannerPatterns != null && !this.hasTooManyPatterns) {
            context.getMatrices().push();
            context.getMatrices().translate(k + 139, l + 52, 0.0f);
            context.getMatrices().scale(24.0f, 24.0f, 1.0f);
            context.getMatrices().translate(0.5f, -0.5f, 0.5f);
            float g = 0.6666667f;
            context.getMatrices().scale(0.6666667f, 0.6666667f, -0.6666667f);
            this.bannerField.pitch = 0.0f;
            this.bannerField.pivotY = -32.0f;
            DyeColor lv6 = ((BannerItem)lv4.getStack().getItem()).getColor();
            BannerBlockEntityRenderer.renderCanvas(context.getMatrices(), context.getVertexConsumers(), 0xF000F0, OverlayTexture.DEFAULT_UV, this.bannerField, ModelLoader.BANNER_BASE, true, lv6, this.bannerPatterns);
            context.getMatrices().pop();
            context.draw();
        } else if (this.hasTooManyPatterns) {
            context.drawGuiTexture(ERROR_TEXTURE, k + lv4.x - 5, l + lv4.y - 5, 26, 26);
        }
        if (this.canApplyDyePattern) {
            int n = k + 60;
            int o = l + 13;
            List<RegistryEntry<BannerPattern>> list = ((LoomScreenHandler)this.handler).getBannerPatterns();
            block0: for (int p = 0; p < 4; ++p) {
                for (int q = 0; q < 4; ++q) {
                    boolean bl;
                    int r = p + this.visibleTopRow;
                    int s = r * 4 + q;
                    if (s >= list.size()) break block0;
                    int t = n + q * 14;
                    int u = o + p * 14;
                    boolean bl2 = bl = mouseX >= t && mouseY >= u && mouseX < t + 14 && mouseY < u + 14;
                    Identifier lv7 = s == ((LoomScreenHandler)this.handler).getSelectedPattern() ? PATTERN_SELECTED_TEXTURE : (bl ? PATTERN_HIGHLIGHTED_TEXTURE : PATTERN_TEXTURE);
                    context.drawGuiTexture(lv7, t, u, 14, 14);
                    this.drawBanner(context, list.get(s), t, u);
                }
            }
        }
        DiffuseLighting.enableGuiDepthLighting();
    }

    private void drawBanner(DrawContext context, RegistryEntry<BannerPattern> pattern, int x, int y) {
        MatrixStack lv = new MatrixStack();
        lv.push();
        lv.translate((float)x + 0.5f, y + 16, 0.0f);
        lv.scale(6.0f, -6.0f, 1.0f);
        lv.translate(0.5f, 0.5f, 0.0f);
        lv.translate(0.5f, 0.5f, 0.5f);
        float f = 0.6666667f;
        lv.scale(0.6666667f, -0.6666667f, -0.6666667f);
        this.bannerField.pitch = 0.0f;
        this.bannerField.pivotY = -32.0f;
        BannerPatternsComponent lv2 = new BannerPatternsComponent.Builder().add(pattern, DyeColor.WHITE).build();
        BannerBlockEntityRenderer.renderCanvas(lv, context.getVertexConsumers(), 0xF000F0, OverlayTexture.DEFAULT_UV, this.bannerField, ModelLoader.BANNER_BASE, true, DyeColor.GRAY, lv2);
        lv.pop();
        context.draw();
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        this.scrollbarClicked = false;
        if (this.canApplyDyePattern) {
            int j = this.x + 60;
            int k = this.y + 13;
            for (int l = 0; l < 4; ++l) {
                for (int m = 0; m < 4; ++m) {
                    double f = mouseX - (double)(j + m * 14);
                    double g = mouseY - (double)(k + l * 14);
                    int n = l + this.visibleTopRow;
                    int o = n * 4 + m;
                    if (!(f >= 0.0) || !(g >= 0.0) || !(f < 14.0) || !(g < 14.0) || !((LoomScreenHandler)this.handler).onButtonClick(this.client.player, o)) continue;
                    MinecraftClient.getInstance().getSoundManager().play(PositionedSoundInstance.master(SoundEvents.UI_LOOM_SELECT_PATTERN, 1.0f));
                    this.client.interactionManager.clickButton(((LoomScreenHandler)this.handler).syncId, o);
                    return true;
                }
            }
            j = this.x + 119;
            k = this.y + 9;
            if (mouseX >= (double)j && mouseX < (double)(j + 12) && mouseY >= (double)k && mouseY < (double)(k + 56)) {
                this.scrollbarClicked = true;
            }
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double deltaX, double deltaY) {
        int j = this.getRows() - 4;
        if (this.scrollbarClicked && this.canApplyDyePattern && j > 0) {
            int k = this.y + 13;
            int l = k + 56;
            this.scrollPosition = ((float)mouseY - (float)k - 7.5f) / ((float)(l - k) - 15.0f);
            this.scrollPosition = MathHelper.clamp(this.scrollPosition, 0.0f, 1.0f);
            this.visibleTopRow = Math.max((int)((double)(this.scrollPosition * (float)j) + 0.5), 0);
            return true;
        }
        return super.mouseDragged(mouseX, mouseY, button, deltaX, deltaY);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double horizontalAmount, double verticalAmount) {
        int i = this.getRows() - 4;
        if (this.canApplyDyePattern && i > 0) {
            float h = (float)verticalAmount / (float)i;
            this.scrollPosition = MathHelper.clamp(this.scrollPosition - h, 0.0f, 1.0f);
            this.visibleTopRow = Math.max((int)(this.scrollPosition * (float)i + 0.5f), 0);
        }
        return true;
    }

    @Override
    protected boolean isClickOutsideBounds(double mouseX, double mouseY, int left, int top, int button) {
        return mouseX < (double)left || mouseY < (double)top || mouseX >= (double)(left + this.backgroundWidth) || mouseY >= (double)(top + this.backgroundHeight);
    }

    private void onInventoryChanged() {
        ItemStack lv = ((LoomScreenHandler)this.handler).getOutputSlot().getStack();
        this.bannerPatterns = lv.isEmpty() ? null : lv.getOrDefault(DataComponentTypes.BANNER_PATTERNS, BannerPatternsComponent.DEFAULT);
        ItemStack lv2 = ((LoomScreenHandler)this.handler).getBannerSlot().getStack();
        ItemStack lv3 = ((LoomScreenHandler)this.handler).getDyeSlot().getStack();
        ItemStack lv4 = ((LoomScreenHandler)this.handler).getPatternSlot().getStack();
        BannerPatternsComponent lv5 = lv2.getOrDefault(DataComponentTypes.BANNER_PATTERNS, BannerPatternsComponent.DEFAULT);
        boolean bl = this.hasTooManyPatterns = lv5.layers().size() >= 6;
        if (this.hasTooManyPatterns) {
            this.bannerPatterns = null;
        }
        if (!(ItemStack.areEqual(lv2, this.banner) && ItemStack.areEqual(lv3, this.dye) && ItemStack.areEqual(lv4, this.pattern))) {
            boolean bl2 = this.canApplyDyePattern = !lv2.isEmpty() && !lv3.isEmpty() && !this.hasTooManyPatterns && !((LoomScreenHandler)this.handler).getBannerPatterns().isEmpty();
        }
        if (this.visibleTopRow >= this.getRows()) {
            this.visibleTopRow = 0;
            this.scrollPosition = 0.0f;
        }
        this.banner = lv2.copy();
        this.dye = lv3.copy();
        this.pattern = lv4.copy();
    }
}

