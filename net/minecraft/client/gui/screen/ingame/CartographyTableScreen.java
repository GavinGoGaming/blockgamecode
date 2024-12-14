/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 * 
 * Could not load the following classes:
 *  net.fabricmc.api.EnvType
 *  net.fabricmc.api.Environment
 *  org.jetbrains.annotations.Nullable
 */
package net.minecraft.client.gui.screen.ingame;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.MapIdComponent;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.FilledMapItem;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.item.map.MapState;
import net.minecraft.screen.CartographyTableScreenHandler;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

@Environment(value=EnvType.CLIENT)
public class CartographyTableScreen
extends HandledScreen<CartographyTableScreenHandler> {
    private static final Identifier ERROR_TEXTURE = Identifier.ofVanilla("container/cartography_table/error");
    private static final Identifier SCALED_MAP_TEXTURE = Identifier.ofVanilla("container/cartography_table/scaled_map");
    private static final Identifier DUPLICATED_MAP_TEXTURE = Identifier.ofVanilla("container/cartography_table/duplicated_map");
    private static final Identifier MAP_TEXTURE = Identifier.ofVanilla("container/cartography_table/map");
    private static final Identifier LOCKED_TEXTURE = Identifier.ofVanilla("container/cartography_table/locked");
    private static final Identifier TEXTURE = Identifier.ofVanilla("textures/gui/container/cartography_table.png");

    public CartographyTableScreen(CartographyTableScreenHandler handler, PlayerInventory inventory, Text title) {
        super(handler, inventory, title);
        this.titleY -= 2;
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        super.render(context, mouseX, mouseY, delta);
        this.drawMouseoverTooltip(context, mouseX, mouseY);
    }

    @Override
    protected void drawBackground(DrawContext context, float delta, int mouseX, int mouseY) {
        MapState lv4;
        int k = this.x;
        int l = this.y;
        context.drawTexture(TEXTURE, k, l, 0, 0, this.backgroundWidth, this.backgroundHeight);
        ItemStack lv = ((CartographyTableScreenHandler)this.handler).getSlot(1).getStack();
        boolean bl = lv.isOf(Items.MAP);
        boolean bl2 = lv.isOf(Items.PAPER);
        boolean bl3 = lv.isOf(Items.GLASS_PANE);
        ItemStack lv2 = ((CartographyTableScreenHandler)this.handler).getSlot(0).getStack();
        MapIdComponent lv3 = lv2.get(DataComponentTypes.MAP_ID);
        boolean bl4 = false;
        if (lv3 != null) {
            lv4 = FilledMapItem.getMapState(lv3, (World)this.client.world);
            if (lv4 != null) {
                if (lv4.locked) {
                    bl4 = true;
                    if (bl2 || bl3) {
                        context.drawGuiTexture(ERROR_TEXTURE, k + 35, l + 31, 28, 21);
                    }
                }
                if (bl2 && lv4.scale >= 4) {
                    bl4 = true;
                    context.drawGuiTexture(ERROR_TEXTURE, k + 35, l + 31, 28, 21);
                }
            }
        } else {
            lv4 = null;
        }
        this.drawMap(context, lv3, lv4, bl, bl2, bl3, bl4);
    }

    private void drawMap(DrawContext context, @Nullable MapIdComponent mapId, @Nullable MapState mapState, boolean cloneMode, boolean expandMode, boolean lockMode, boolean cannotExpand) {
        int i = this.x;
        int j = this.y;
        if (expandMode && !cannotExpand) {
            context.drawGuiTexture(SCALED_MAP_TEXTURE, i + 67, j + 13, 66, 66);
            this.drawMap(context, mapId, mapState, i + 85, j + 31, 0.226f);
        } else if (cloneMode) {
            context.drawGuiTexture(DUPLICATED_MAP_TEXTURE, i + 67 + 16, j + 13, 50, 66);
            this.drawMap(context, mapId, mapState, i + 86, j + 16, 0.34f);
            context.getMatrices().push();
            context.getMatrices().translate(0.0f, 0.0f, 1.0f);
            context.drawGuiTexture(DUPLICATED_MAP_TEXTURE, i + 67, j + 13 + 16, 50, 66);
            this.drawMap(context, mapId, mapState, i + 70, j + 32, 0.34f);
            context.getMatrices().pop();
        } else if (lockMode) {
            context.drawGuiTexture(MAP_TEXTURE, i + 67, j + 13, 66, 66);
            this.drawMap(context, mapId, mapState, i + 71, j + 17, 0.45f);
            context.getMatrices().push();
            context.getMatrices().translate(0.0f, 0.0f, 1.0f);
            context.drawGuiTexture(LOCKED_TEXTURE, i + 118, j + 60, 10, 14);
            context.getMatrices().pop();
        } else {
            context.drawGuiTexture(MAP_TEXTURE, i + 67, j + 13, 66, 66);
            this.drawMap(context, mapId, mapState, i + 71, j + 17, 0.45f);
        }
    }

    private void drawMap(DrawContext context, @Nullable MapIdComponent mapId, @Nullable MapState mapState, int x, int y, float scale) {
        if (mapId != null && mapState != null) {
            context.getMatrices().push();
            context.getMatrices().translate(x, y, 1.0f);
            context.getMatrices().scale(scale, scale, 1.0f);
            this.client.gameRenderer.getMapRenderer().draw(context.getMatrices(), context.getVertexConsumers(), mapId, mapState, true, 0xF000F0);
            context.draw();
            context.getMatrices().pop();
        }
    }
}

