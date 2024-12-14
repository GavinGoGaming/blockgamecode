/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 * 
 * Could not load the following classes:
 *  net.fabricmc.api.EnvType
 *  net.fabricmc.api.Environment
 *  org.jetbrains.annotations.Nullable
 */
package net.minecraft.client.gui;

import com.google.common.collect.Lists;
import com.mojang.blaze3d.systems.RenderSystem;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.ScreenRect;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.tooltip.HoveredTooltipPositioner;
import net.minecraft.client.gui.tooltip.TooltipBackgroundRenderer;
import net.minecraft.client.gui.tooltip.TooltipComponent;
import net.minecraft.client.gui.tooltip.TooltipPositioner;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.render.BufferBuilder;
import net.minecraft.client.render.BufferRenderer;
import net.minecraft.client.render.DiffuseLighting;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.client.render.OverlayTexture;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.Tessellator;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.VertexFormat;
import net.minecraft.client.render.VertexFormats;
import net.minecraft.client.render.model.BakedModel;
import net.minecraft.client.render.model.json.ModelTransformationMode;
import net.minecraft.client.texture.GuiAtlasManager;
import net.minecraft.client.texture.Scaling;
import net.minecraft.client.texture.Sprite;
import net.minecraft.client.util.Window;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.tooltip.TooltipData;
import net.minecraft.text.HoverEvent;
import net.minecraft.text.OrderedText;
import net.minecraft.text.StringVisitable;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.util.Colors;
import net.minecraft.util.Identifier;
import net.minecraft.util.Util;
import net.minecraft.util.crash.CrashException;
import net.minecraft.util.crash.CrashReport;
import net.minecraft.util.crash.CrashReportSection;
import net.minecraft.util.math.ColorHelper;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;
import org.joml.Matrix4f;
import org.joml.Vector2ic;

@Environment(value=EnvType.CLIENT)
public class DrawContext {
    public static final float field_44931 = 10000.0f;
    public static final float field_44932 = -10000.0f;
    private static final int field_44655 = 2;
    private final MinecraftClient client;
    private final MatrixStack matrices;
    private final VertexConsumerProvider.Immediate vertexConsumers;
    private final ScissorStack scissorStack = new ScissorStack();
    private final GuiAtlasManager guiAtlasManager;
    private boolean runningDrawCallback;

    private DrawContext(MinecraftClient client, MatrixStack matrices, VertexConsumerProvider.Immediate vertexConsumers) {
        this.client = client;
        this.matrices = matrices;
        this.vertexConsumers = vertexConsumers;
        this.guiAtlasManager = client.getGuiAtlasManager();
    }

    public DrawContext(MinecraftClient client, VertexConsumerProvider.Immediate vertexConsumers) {
        this(client, new MatrixStack(), vertexConsumers);
    }

    @Deprecated
    public void draw(Runnable drawCallback) {
        this.draw();
        this.runningDrawCallback = true;
        drawCallback.run();
        this.runningDrawCallback = false;
        this.draw();
    }

    @Deprecated
    private void tryDraw() {
        if (!this.runningDrawCallback) {
            this.draw();
        }
    }

    @Deprecated
    private void drawIfRunning() {
        if (this.runningDrawCallback) {
            this.draw();
        }
    }

    public int getScaledWindowWidth() {
        return this.client.getWindow().getScaledWidth();
    }

    public int getScaledWindowHeight() {
        return this.client.getWindow().getScaledHeight();
    }

    public MatrixStack getMatrices() {
        return this.matrices;
    }

    public VertexConsumerProvider.Immediate getVertexConsumers() {
        return this.vertexConsumers;
    }

    public void draw() {
        RenderSystem.disableDepthTest();
        this.vertexConsumers.draw();
        RenderSystem.enableDepthTest();
    }

    public void drawHorizontalLine(int x1, int x2, int y, int color) {
        this.drawHorizontalLine(RenderLayer.getGui(), x1, x2, y, color);
    }

    public void drawHorizontalLine(RenderLayer layer, int x1, int x2, int y, int color) {
        if (x2 < x1) {
            int m = x1;
            x1 = x2;
            x2 = m;
        }
        this.fill(layer, x1, y, x2 + 1, y + 1, color);
    }

    public void drawVerticalLine(int x, int y1, int y2, int color) {
        this.drawVerticalLine(RenderLayer.getGui(), x, y1, y2, color);
    }

    public void drawVerticalLine(RenderLayer layer, int x, int y1, int y2, int color) {
        if (y2 < y1) {
            int m = y1;
            y1 = y2;
            y2 = m;
        }
        this.fill(layer, x, y1 + 1, x + 1, y2, color);
    }

    public void enableScissor(int x1, int y1, int x2, int y2) {
        this.setScissor(this.scissorStack.push(new ScreenRect(x1, y1, x2 - x1, y2 - y1)));
    }

    public void disableScissor() {
        this.setScissor(this.scissorStack.pop());
    }

    public boolean scissorContains(int x, int y) {
        return this.scissorStack.contains(x, y);
    }

    private void setScissor(@Nullable ScreenRect rect) {
        this.drawIfRunning();
        if (rect != null) {
            Window lv = MinecraftClient.getInstance().getWindow();
            int i = lv.getFramebufferHeight();
            double d = lv.getScaleFactor();
            double e = (double)rect.getLeft() * d;
            double f = (double)i - (double)rect.getBottom() * d;
            double g = (double)rect.width() * d;
            double h = (double)rect.height() * d;
            RenderSystem.enableScissor((int)e, (int)f, Math.max(0, (int)g), Math.max(0, (int)h));
        } else {
            RenderSystem.disableScissor();
        }
    }

    public void setShaderColor(float red, float green, float blue, float alpha) {
        this.drawIfRunning();
        RenderSystem.setShaderColor(red, green, blue, alpha);
    }

    public void fill(int x1, int y1, int x2, int y2, int color) {
        this.fill(x1, y1, x2, y2, 0, color);
    }

    public void fill(int x1, int y1, int x2, int y2, int z, int color) {
        this.fill(RenderLayer.getGui(), x1, y1, x2, y2, z, color);
    }

    public void fill(RenderLayer layer, int x1, int y1, int x2, int y2, int color) {
        this.fill(layer, x1, y1, x2, y2, 0, color);
    }

    public void fill(RenderLayer layer, int x1, int y1, int x2, int y2, int z, int color) {
        int o;
        Matrix4f matrix4f = this.matrices.peek().getPositionMatrix();
        if (x1 < x2) {
            o = x1;
            x1 = x2;
            x2 = o;
        }
        if (y1 < y2) {
            o = y1;
            y1 = y2;
            y2 = o;
        }
        VertexConsumer lv = this.vertexConsumers.getBuffer(layer);
        lv.vertex(matrix4f, (float)x1, (float)y1, (float)z).color(color);
        lv.vertex(matrix4f, (float)x1, (float)y2, (float)z).color(color);
        lv.vertex(matrix4f, (float)x2, (float)y2, (float)z).color(color);
        lv.vertex(matrix4f, (float)x2, (float)y1, (float)z).color(color);
        this.tryDraw();
    }

    public void fillGradient(int startX, int startY, int endX, int endY, int colorStart, int colorEnd) {
        this.fillGradient(startX, startY, endX, endY, 0, colorStart, colorEnd);
    }

    public void fillGradient(int startX, int startY, int endX, int endY, int z, int colorStart, int colorEnd) {
        this.fillGradient(RenderLayer.getGui(), startX, startY, endX, endY, colorStart, colorEnd, z);
    }

    public void fillGradient(RenderLayer layer, int startX, int startY, int endX, int endY, int colorStart, int colorEnd, int z) {
        VertexConsumer lv = this.vertexConsumers.getBuffer(layer);
        this.fillGradient(lv, startX, startY, endX, endY, z, colorStart, colorEnd);
        this.tryDraw();
    }

    private void fillGradient(VertexConsumer vertexConsumer, int startX, int startY, int endX, int endY, int z, int colorStart, int colorEnd) {
        Matrix4f matrix4f = this.matrices.peek().getPositionMatrix();
        vertexConsumer.vertex(matrix4f, (float)startX, (float)startY, (float)z).color(colorStart);
        vertexConsumer.vertex(matrix4f, (float)startX, (float)endY, (float)z).color(colorEnd);
        vertexConsumer.vertex(matrix4f, (float)endX, (float)endY, (float)z).color(colorEnd);
        vertexConsumer.vertex(matrix4f, (float)endX, (float)startY, (float)z).color(colorStart);
    }

    public void fillWithLayer(RenderLayer layer, int startX, int startY, int endX, int endY, int z) {
        Matrix4f matrix4f = this.matrices.peek().getPositionMatrix();
        VertexConsumer lv = this.vertexConsumers.getBuffer(layer);
        lv.vertex(matrix4f, (float)startX, (float)startY, (float)z);
        lv.vertex(matrix4f, (float)startX, (float)endY, (float)z);
        lv.vertex(matrix4f, (float)endX, (float)endY, (float)z);
        lv.vertex(matrix4f, (float)endX, (float)startY, (float)z);
        this.tryDraw();
    }

    public void drawCenteredTextWithShadow(TextRenderer textRenderer, String text, int centerX, int y, int color) {
        this.drawTextWithShadow(textRenderer, text, centerX - textRenderer.getWidth(text) / 2, y, color);
    }

    public void drawCenteredTextWithShadow(TextRenderer textRenderer, Text text, int centerX, int y, int color) {
        OrderedText lv = text.asOrderedText();
        this.drawTextWithShadow(textRenderer, lv, centerX - textRenderer.getWidth(lv) / 2, y, color);
    }

    public void drawCenteredTextWithShadow(TextRenderer textRenderer, OrderedText text, int centerX, int y, int color) {
        this.drawTextWithShadow(textRenderer, text, centerX - textRenderer.getWidth(text) / 2, y, color);
    }

    public int drawTextWithShadow(TextRenderer textRenderer, @Nullable String text, int x, int y, int color) {
        return this.drawText(textRenderer, text, x, y, color, true);
    }

    public int drawText(TextRenderer textRenderer, @Nullable String text, int x, int y, int color, boolean shadow) {
        if (text == null) {
            return 0;
        }
        int l = textRenderer.draw(text, x, y, color, shadow, this.matrices.peek().getPositionMatrix(), this.vertexConsumers, TextRenderer.TextLayerType.NORMAL, 0, 0xF000F0, textRenderer.isRightToLeft());
        this.tryDraw();
        return l;
    }

    public int drawTextWithShadow(TextRenderer textRenderer, OrderedText text, int x, int y, int color) {
        return this.drawText(textRenderer, text, x, y, color, true);
    }

    public int drawText(TextRenderer textRenderer, OrderedText text, int x, int y, int color, boolean shadow) {
        int l = textRenderer.draw(text, (float)x, (float)y, color, shadow, this.matrices.peek().getPositionMatrix(), (VertexConsumerProvider)this.vertexConsumers, TextRenderer.TextLayerType.NORMAL, 0, 0xF000F0);
        this.tryDraw();
        return l;
    }

    public int drawTextWithShadow(TextRenderer textRenderer, Text text, int x, int y, int color) {
        return this.drawText(textRenderer, text, x, y, color, true);
    }

    public int drawText(TextRenderer textRenderer, Text text, int x, int y, int color, boolean shadow) {
        return this.drawText(textRenderer, text.asOrderedText(), x, y, color, shadow);
    }

    public void drawTextWrapped(TextRenderer textRenderer, StringVisitable text, int x, int y, int width, int color) {
        for (OrderedText lv : textRenderer.wrapLines(text, width)) {
            this.drawText(textRenderer, lv, x, y, color, false);
            y += textRenderer.fontHeight;
        }
    }

    public int drawTextWithBackground(TextRenderer textRenderer, Text text, int x, int y, int width, int color) {
        int m = this.client.options.getTextBackgroundColor(0.0f);
        if (m != 0) {
            int n = 2;
            this.fill(x - 2, y - 2, x + width + 2, y + textRenderer.fontHeight + 2, ColorHelper.Argb.mixColor(m, color));
        }
        return this.drawText(textRenderer, text, x, y, color, true);
    }

    public void drawSprite(int x, int y, int z, int width, int height, Sprite sprite) {
        this.drawSprite(sprite, x, y, z, width, height);
    }

    public void drawSprite(int x, int y, int z, int width, int height, Sprite sprite, float red, float green, float blue, float alpha) {
        this.drawTexturedQuad(sprite.getAtlasId(), x, x + width, y, y + height, z, sprite.getMinU(), sprite.getMaxU(), sprite.getMinV(), sprite.getMaxV(), red, green, blue, alpha);
    }

    public void drawBorder(int x, int y, int width, int height, int color) {
        this.fill(x, y, x + width, y + 1, color);
        this.fill(x, y + height - 1, x + width, y + height, color);
        this.fill(x, y + 1, x + 1, y + height - 1, color);
        this.fill(x + width - 1, y + 1, x + width, y + height - 1, color);
    }

    public void drawGuiTexture(Identifier texture, int x, int y, int width, int height) {
        this.drawGuiTexture(texture, x, y, 0, width, height);
    }

    public void drawGuiTexture(Identifier texture, int x, int y, int z, int width, int height) {
        Sprite lv = this.guiAtlasManager.getSprite(texture);
        Scaling lv2 = this.guiAtlasManager.getScaling(lv);
        if (lv2 instanceof Scaling.Stretch) {
            this.drawSprite(lv, x, y, z, width, height);
        } else if (lv2 instanceof Scaling.Tile) {
            Scaling.Tile lv3 = (Scaling.Tile)lv2;
            this.drawSpriteTiled(lv, x, y, z, width, height, 0, 0, lv3.width(), lv3.height(), lv3.width(), lv3.height());
        } else if (lv2 instanceof Scaling.NineSlice) {
            Scaling.NineSlice lv4 = (Scaling.NineSlice)lv2;
            this.drawSprite(lv, lv4, x, y, z, width, height);
        }
    }

    public void drawGuiTexture(Identifier texture, int i, int j, int k, int l, int x, int y, int width, int height) {
        this.drawGuiTexture(texture, i, j, k, l, x, y, 0, width, height);
    }

    public void drawGuiTexture(Identifier texture, int i, int j, int k, int l, int x, int y, int z, int width, int height) {
        Sprite lv = this.guiAtlasManager.getSprite(texture);
        Scaling lv2 = this.guiAtlasManager.getScaling(lv);
        if (lv2 instanceof Scaling.Stretch) {
            this.drawSprite(lv, i, j, k, l, x, y, z, width, height);
        } else {
            this.drawSprite(lv, x, y, z, width, height);
        }
    }

    private void drawSprite(Sprite sprite, int i, int j, int k, int l, int x, int y, int z, int width, int height) {
        if (width == 0 || height == 0) {
            return;
        }
        this.drawTexturedQuad(sprite.getAtlasId(), x, x + width, y, y + height, z, sprite.getFrameU((float)k / (float)i), sprite.getFrameU((float)(k + width) / (float)i), sprite.getFrameV((float)l / (float)j), sprite.getFrameV((float)(l + height) / (float)j));
    }

    private void drawSprite(Sprite sprite, int x, int y, int z, int width, int height) {
        if (width == 0 || height == 0) {
            return;
        }
        this.drawTexturedQuad(sprite.getAtlasId(), x, x + width, y, y + height, z, sprite.getMinU(), sprite.getMaxU(), sprite.getMinV(), sprite.getMaxV());
    }

    public void drawTexture(Identifier texture, int x, int y, int u, int v, int width, int height) {
        this.drawTexture(texture, x, y, 0, u, v, width, height, 256, 256);
    }

    public void drawTexture(Identifier texture, int x, int y, int z, float u, float v, int width, int height, int textureWidth, int textureHeight) {
        this.drawTexture(texture, x, x + width, y, y + height, z, width, height, u, v, textureWidth, textureHeight);
    }

    public void drawTexture(Identifier texture, int x, int y, int width, int height, float u, float v, int regionWidth, int regionHeight, int textureWidth, int textureHeight) {
        this.drawTexture(texture, x, x + width, y, y + height, 0, regionWidth, regionHeight, u, v, textureWidth, textureHeight);
    }

    public void drawTexture(Identifier texture, int x, int y, float u, float v, int width, int height, int textureWidth, int textureHeight) {
        this.drawTexture(texture, x, y, width, height, u, v, width, height, textureWidth, textureHeight);
    }

    void drawTexture(Identifier texture, int x1, int x2, int y1, int y2, int z, int regionWidth, int regionHeight, float u, float v, int textureWidth, int textureHeight) {
        this.drawTexturedQuad(texture, x1, x2, y1, y2, z, (u + 0.0f) / (float)textureWidth, (u + (float)regionWidth) / (float)textureWidth, (v + 0.0f) / (float)textureHeight, (v + (float)regionHeight) / (float)textureHeight);
    }

    void drawTexturedQuad(Identifier texture, int x1, int x2, int y1, int y2, int z, float u1, float u2, float v1, float v2) {
        RenderSystem.setShaderTexture(0, texture);
        RenderSystem.setShader(GameRenderer::getPositionTexProgram);
        Matrix4f matrix4f = this.matrices.peek().getPositionMatrix();
        BufferBuilder lv = Tessellator.getInstance().begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_TEXTURE);
        lv.vertex(matrix4f, (float)x1, (float)y1, (float)z).texture(u1, v1);
        lv.vertex(matrix4f, (float)x1, (float)y2, (float)z).texture(u1, v2);
        lv.vertex(matrix4f, (float)x2, (float)y2, (float)z).texture(u2, v2);
        lv.vertex(matrix4f, (float)x2, (float)y1, (float)z).texture(u2, v1);
        BufferRenderer.drawWithGlobalProgram(lv.end());
    }

    void drawTexturedQuad(Identifier texture, int x1, int x2, int y1, int y2, int z, float u1, float u2, float v1, float v2, float red, float green, float blue, float alpha) {
        RenderSystem.setShaderTexture(0, texture);
        RenderSystem.setShader(GameRenderer::getPositionTexColorProgram);
        RenderSystem.enableBlend();
        Matrix4f matrix4f = this.matrices.peek().getPositionMatrix();
        BufferBuilder lv = Tessellator.getInstance().begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_TEXTURE_COLOR);
        lv.vertex(matrix4f, (float)x1, (float)y1, (float)z).texture(u1, v1).color(red, green, blue, alpha);
        lv.vertex(matrix4f, (float)x1, (float)y2, (float)z).texture(u1, v2).color(red, green, blue, alpha);
        lv.vertex(matrix4f, (float)x2, (float)y2, (float)z).texture(u2, v2).color(red, green, blue, alpha);
        lv.vertex(matrix4f, (float)x2, (float)y1, (float)z).texture(u2, v1).color(red, green, blue, alpha);
        BufferRenderer.drawWithGlobalProgram(lv.end());
        RenderSystem.disableBlend();
    }

    private void drawSprite(Sprite sprite, Scaling.NineSlice nineSlice, int x, int y, int z, int width, int height) {
        Scaling.NineSlice.Border lv = nineSlice.border();
        int n = Math.min(lv.left(), width / 2);
        int o = Math.min(lv.right(), width / 2);
        int p = Math.min(lv.top(), height / 2);
        int q = Math.min(lv.bottom(), height / 2);
        if (width == nineSlice.width() && height == nineSlice.height()) {
            this.drawSprite(sprite, nineSlice.width(), nineSlice.height(), 0, 0, x, y, z, width, height);
            return;
        }
        if (height == nineSlice.height()) {
            this.drawSprite(sprite, nineSlice.width(), nineSlice.height(), 0, 0, x, y, z, n, height);
            this.drawSpriteTiled(sprite, x + n, y, z, width - o - n, height, n, 0, nineSlice.width() - o - n, nineSlice.height(), nineSlice.width(), nineSlice.height());
            this.drawSprite(sprite, nineSlice.width(), nineSlice.height(), nineSlice.width() - o, 0, x + width - o, y, z, o, height);
            return;
        }
        if (width == nineSlice.width()) {
            this.drawSprite(sprite, nineSlice.width(), nineSlice.height(), 0, 0, x, y, z, width, p);
            this.drawSpriteTiled(sprite, x, y + p, z, width, height - q - p, 0, p, nineSlice.width(), nineSlice.height() - q - p, nineSlice.width(), nineSlice.height());
            this.drawSprite(sprite, nineSlice.width(), nineSlice.height(), 0, nineSlice.height() - q, x, y + height - q, z, width, q);
            return;
        }
        this.drawSprite(sprite, nineSlice.width(), nineSlice.height(), 0, 0, x, y, z, n, p);
        this.drawSpriteTiled(sprite, x + n, y, z, width - o - n, p, n, 0, nineSlice.width() - o - n, p, nineSlice.width(), nineSlice.height());
        this.drawSprite(sprite, nineSlice.width(), nineSlice.height(), nineSlice.width() - o, 0, x + width - o, y, z, o, p);
        this.drawSprite(sprite, nineSlice.width(), nineSlice.height(), 0, nineSlice.height() - q, x, y + height - q, z, n, q);
        this.drawSpriteTiled(sprite, x + n, y + height - q, z, width - o - n, q, n, nineSlice.height() - q, nineSlice.width() - o - n, q, nineSlice.width(), nineSlice.height());
        this.drawSprite(sprite, nineSlice.width(), nineSlice.height(), nineSlice.width() - o, nineSlice.height() - q, x + width - o, y + height - q, z, o, q);
        this.drawSpriteTiled(sprite, x, y + p, z, n, height - q - p, 0, p, n, nineSlice.height() - q - p, nineSlice.width(), nineSlice.height());
        this.drawSpriteTiled(sprite, x + n, y + p, z, width - o - n, height - q - p, n, p, nineSlice.width() - o - n, nineSlice.height() - q - p, nineSlice.width(), nineSlice.height());
        this.drawSpriteTiled(sprite, x + width - o, y + p, z, n, height - q - p, nineSlice.width() - o, p, o, nineSlice.height() - q - p, nineSlice.width(), nineSlice.height());
    }

    private void drawSpriteTiled(Sprite sprite, int x, int y, int z, int width, int height, int n, int o, int tileWidth, int tileHeight, int r, int s) {
        if (width <= 0 || height <= 0) {
            return;
        }
        if (tileWidth <= 0 || tileHeight <= 0) {
            throw new IllegalArgumentException("Tiled sprite texture size must be positive, got " + tileWidth + "x" + tileHeight);
        }
        for (int t = 0; t < width; t += tileWidth) {
            int u = Math.min(tileWidth, width - t);
            for (int v = 0; v < height; v += tileHeight) {
                int w = Math.min(tileHeight, height - v);
                this.drawSprite(sprite, r, s, n, o, x + t, y + v, z, u, w);
            }
        }
    }

    public void drawItem(ItemStack item, int x, int y) {
        this.drawItem(this.client.player, this.client.world, item, x, y, 0);
    }

    public void drawItem(ItemStack stack, int x, int y, int seed) {
        this.drawItem(this.client.player, this.client.world, stack, x, y, seed);
    }

    public void drawItem(ItemStack stack, int x, int y, int seed, int z) {
        this.drawItem(this.client.player, this.client.world, stack, x, y, seed, z);
    }

    public void drawItemWithoutEntity(ItemStack stack, int x, int y) {
        this.drawItemWithoutEntity(stack, x, y, 0);
    }

    public void drawItemWithoutEntity(ItemStack stack, int x, int y, int seed) {
        this.drawItem(null, this.client.world, stack, x, y, seed);
    }

    public void drawItem(LivingEntity entity, ItemStack stack, int x, int y, int seed) {
        this.drawItem(entity, entity.getWorld(), stack, x, y, seed);
    }

    private void drawItem(@Nullable LivingEntity entity, @Nullable World world, ItemStack stack, int x, int y, int seed) {
        this.drawItem(entity, world, stack, x, y, seed, 0);
    }

    private void drawItem(@Nullable LivingEntity entity, @Nullable World world, ItemStack stack, int x, int y, int seed, int z) {
        if (stack.isEmpty()) {
            return;
        }
        BakedModel lv = this.client.getItemRenderer().getModel(stack, world, entity, seed);
        this.matrices.push();
        this.matrices.translate(x + 8, y + 8, 150 + (lv.hasDepth() ? z : 0));
        try {
            boolean bl;
            this.matrices.scale(16.0f, -16.0f, 16.0f);
            boolean bl2 = bl = !lv.isSideLit();
            if (bl) {
                DiffuseLighting.disableGuiDepthLighting();
            }
            this.client.getItemRenderer().renderItem(stack, ModelTransformationMode.GUI, false, this.matrices, this.getVertexConsumers(), 0xF000F0, OverlayTexture.DEFAULT_UV, lv);
            this.draw();
            if (bl) {
                DiffuseLighting.enableGuiDepthLighting();
            }
        } catch (Throwable throwable) {
            CrashReport lv2 = CrashReport.create(throwable, "Rendering item");
            CrashReportSection lv3 = lv2.addElement("Item being rendered");
            lv3.add("Item Type", () -> String.valueOf(stack.getItem()));
            lv3.add("Item Components", () -> String.valueOf(stack.getComponents()));
            lv3.add("Item Foil", () -> String.valueOf(stack.hasGlint()));
            throw new CrashException(lv2);
        }
        this.matrices.pop();
    }

    public void drawItemInSlot(TextRenderer textRenderer, ItemStack stack, int x, int y) {
        this.drawItemInSlot(textRenderer, stack, x, y, null);
    }

    public void drawItemInSlot(TextRenderer textRenderer, ItemStack stack, int x, int y, @Nullable String countOverride) {
        ClientPlayerEntity lv;
        float f;
        int n;
        int m;
        if (stack.isEmpty()) {
            return;
        }
        this.matrices.push();
        if (stack.getCount() != 1 || countOverride != null) {
            String string2 = countOverride == null ? String.valueOf(stack.getCount()) : countOverride;
            this.matrices.translate(0.0f, 0.0f, 200.0f);
            this.drawText(textRenderer, string2, x + 19 - 2 - textRenderer.getWidth(string2), y + 6 + 3, 0xFFFFFF, true);
        }
        if (stack.isItemBarVisible()) {
            int k = stack.getItemBarStep();
            int l = stack.getItemBarColor();
            m = x + 2;
            n = y + 13;
            this.fill(RenderLayer.getGuiOverlay(), m, n, m + 13, n + 2, Colors.BLACK);
            this.fill(RenderLayer.getGuiOverlay(), m, n, m + k, n + 1, l | Colors.BLACK);
        }
        float f2 = f = (lv = this.client.player) == null ? 0.0f : lv.getItemCooldownManager().getCooldownProgress(stack.getItem(), this.client.getRenderTickCounter().getTickDelta(true));
        if (f > 0.0f) {
            m = y + MathHelper.floor(16.0f * (1.0f - f));
            n = m + MathHelper.ceil(16.0f * f);
            this.fill(RenderLayer.getGuiOverlay(), x, m, x + 16, n, Integer.MAX_VALUE);
        }
        this.matrices.pop();
    }

    public void drawItemTooltip(TextRenderer textRenderer, ItemStack stack, int x, int y) {
        this.drawTooltip(textRenderer, Screen.getTooltipFromItem(this.client, stack), stack.getTooltipData(), x, y);
    }

    public void drawTooltip(TextRenderer textRenderer, List<Text> text, Optional<TooltipData> data2, int x, int y) {
        List<TooltipComponent> list2 = text.stream().map(Text::asOrderedText).map(TooltipComponent::of).collect(Util.toArrayList());
        data2.ifPresent(data -> list2.add(list2.isEmpty() ? 0 : 1, TooltipComponent.of(data)));
        this.drawTooltip(textRenderer, list2, x, y, HoveredTooltipPositioner.INSTANCE);
    }

    public void drawTooltip(TextRenderer textRenderer, Text text, int x, int y) {
        this.drawOrderedTooltip(textRenderer, List.of(text.asOrderedText()), x, y);
    }

    public void drawTooltip(TextRenderer textRenderer, List<Text> text, int x, int y) {
        this.drawOrderedTooltip(textRenderer, Lists.transform(text, Text::asOrderedText), x, y);
    }

    public void drawOrderedTooltip(TextRenderer textRenderer, List<? extends OrderedText> text, int x, int y) {
        this.drawTooltip(textRenderer, text.stream().map(TooltipComponent::of).collect(Collectors.toList()), x, y, HoveredTooltipPositioner.INSTANCE);
    }

    public void drawTooltip(TextRenderer textRenderer, List<OrderedText> text, TooltipPositioner positioner, int x, int y) {
        this.drawTooltip(textRenderer, text.stream().map(TooltipComponent::of).collect(Collectors.toList()), x, y, positioner);
    }

    private void drawTooltip(TextRenderer textRenderer, List<TooltipComponent> components, int x, int y, TooltipPositioner positioner) {
        TooltipComponent lv2;
        int t;
        if (components.isEmpty()) {
            return;
        }
        int k = 0;
        int l = components.size() == 1 ? -2 : 0;
        for (TooltipComponent lv : components) {
            int m = lv.getWidth(textRenderer);
            if (m > k) {
                k = m;
            }
            l += lv.getHeight();
        }
        int n = k;
        int o = l;
        Vector2ic vector2ic = positioner.getPosition(this.getScaledWindowWidth(), this.getScaledWindowHeight(), x, y, n, o);
        int p = vector2ic.x();
        int q = vector2ic.y();
        this.matrices.push();
        int r = 400;
        this.draw(() -> TooltipBackgroundRenderer.render(this, p, q, n, o, 400));
        this.matrices.translate(0.0f, 0.0f, 400.0f);
        int s = q;
        for (t = 0; t < components.size(); ++t) {
            lv2 = components.get(t);
            lv2.drawText(textRenderer, p, s, this.matrices.peek().getPositionMatrix(), this.vertexConsumers);
            s += lv2.getHeight() + (t == 0 ? 2 : 0);
        }
        s = q;
        for (t = 0; t < components.size(); ++t) {
            lv2 = components.get(t);
            lv2.drawItems(textRenderer, p, s, this);
            s += lv2.getHeight() + (t == 0 ? 2 : 0);
        }
        this.matrices.pop();
    }

    public void drawHoverEvent(TextRenderer textRenderer, @Nullable Style style, int x, int y) {
        if (style == null || style.getHoverEvent() == null) {
            return;
        }
        HoverEvent lv = style.getHoverEvent();
        HoverEvent.ItemStackContent lv2 = lv.getValue(HoverEvent.Action.SHOW_ITEM);
        if (lv2 != null) {
            this.drawItemTooltip(textRenderer, lv2.asStack(), x, y);
        } else {
            HoverEvent.EntityContent lv3 = lv.getValue(HoverEvent.Action.SHOW_ENTITY);
            if (lv3 != null) {
                if (this.client.options.advancedItemTooltips) {
                    this.drawTooltip(textRenderer, lv3.asTooltip(), x, y);
                }
            } else {
                Text lv4 = lv.getValue(HoverEvent.Action.SHOW_TEXT);
                if (lv4 != null) {
                    this.drawOrderedTooltip(textRenderer, textRenderer.wrapLines(lv4, Math.max(this.getScaledWindowWidth() / 2, 200)), x, y);
                }
            }
        }
    }

    @Environment(value=EnvType.CLIENT)
    static class ScissorStack {
        private final Deque<ScreenRect> stack = new ArrayDeque<ScreenRect>();

        ScissorStack() {
        }

        public ScreenRect push(ScreenRect rect) {
            ScreenRect lv = this.stack.peekLast();
            if (lv != null) {
                ScreenRect lv2 = Objects.requireNonNullElse(rect.intersection(lv), ScreenRect.empty());
                this.stack.addLast(lv2);
                return lv2;
            }
            this.stack.addLast(rect);
            return rect;
        }

        @Nullable
        public ScreenRect pop() {
            if (this.stack.isEmpty()) {
                throw new IllegalStateException("Scissor stack underflow");
            }
            this.stack.removeLast();
            return this.stack.peekLast();
        }

        public boolean contains(int x, int y) {
            if (this.stack.isEmpty()) {
                return true;
            }
            return this.stack.peek().contains(x, y);
        }
    }
}

