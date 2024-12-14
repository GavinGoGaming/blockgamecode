/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 * 
 * Could not load the following classes:
 *  net.fabricmc.api.EnvType
 *  net.fabricmc.api.Environment
 *  org.jetbrains.annotations.Nullable
 */
package net.minecraft.client.gui.hud;

import com.mojang.blaze3d.systems.RenderSystem;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.block.BlockRenderType;
import net.minecraft.block.BlockState;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.render.BufferBuilder;
import net.minecraft.client.render.BufferRenderer;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.client.render.LightmapTextureManager;
import net.minecraft.client.render.Tessellator;
import net.minecraft.client.render.VertexFormat;
import net.minecraft.client.render.VertexFormats;
import net.minecraft.client.render.model.ModelLoader;
import net.minecraft.client.texture.Sprite;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.registry.tag.FluidTags;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RotationAxis;
import org.jetbrains.annotations.Nullable;
import org.joml.Matrix4f;

@Environment(value=EnvType.CLIENT)
public class InGameOverlayRenderer {
    private static final Identifier UNDERWATER_TEXTURE = Identifier.ofVanilla("textures/misc/underwater.png");

    public static void renderOverlays(MinecraftClient client, MatrixStack matrices) {
        BlockState lv2;
        ClientPlayerEntity lv = client.player;
        if (!lv.noClip && (lv2 = InGameOverlayRenderer.getInWallBlockState(lv)) != null) {
            InGameOverlayRenderer.renderInWallOverlay(client.getBlockRenderManager().getModels().getModelParticleSprite(lv2), matrices);
        }
        if (!client.player.isSpectator()) {
            if (client.player.isSubmergedIn(FluidTags.WATER)) {
                InGameOverlayRenderer.renderUnderwaterOverlay(client, matrices);
            }
            if (client.player.isOnFire()) {
                InGameOverlayRenderer.renderFireOverlay(client, matrices);
            }
        }
    }

    @Nullable
    private static BlockState getInWallBlockState(PlayerEntity player) {
        BlockPos.Mutable lv = new BlockPos.Mutable();
        for (int i = 0; i < 8; ++i) {
            double d = player.getX() + (double)(((float)((i >> 0) % 2) - 0.5f) * player.getWidth() * 0.8f);
            double e = player.getEyeY() + (double)(((float)((i >> 1) % 2) - 0.5f) * 0.1f * player.getScale());
            double f = player.getZ() + (double)(((float)((i >> 2) % 2) - 0.5f) * player.getWidth() * 0.8f);
            lv.set(d, e, f);
            BlockState lv2 = player.getWorld().getBlockState(lv);
            if (lv2.getRenderType() == BlockRenderType.INVISIBLE || !lv2.shouldBlockVision(player.getWorld(), lv)) continue;
            return lv2;
        }
        return null;
    }

    private static void renderInWallOverlay(Sprite sprite, MatrixStack matrices) {
        RenderSystem.setShaderTexture(0, sprite.getAtlasId());
        RenderSystem.setShader(GameRenderer::getPositionTexColorProgram);
        float f = 0.1f;
        float g = -1.0f;
        float h = 1.0f;
        float i = -1.0f;
        float j = 1.0f;
        float k = -0.5f;
        float l = sprite.getMinU();
        float m = sprite.getMaxU();
        float n = sprite.getMinV();
        float o = sprite.getMaxV();
        Matrix4f matrix4f = matrices.peek().getPositionMatrix();
        BufferBuilder lv = Tessellator.getInstance().begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_TEXTURE_COLOR);
        lv.vertex(matrix4f, -1.0f, -1.0f, -0.5f).texture(m, o).color(0.1f, 0.1f, 0.1f, 1.0f);
        lv.vertex(matrix4f, 1.0f, -1.0f, -0.5f).texture(l, o).color(0.1f, 0.1f, 0.1f, 1.0f);
        lv.vertex(matrix4f, 1.0f, 1.0f, -0.5f).texture(l, n).color(0.1f, 0.1f, 0.1f, 1.0f);
        lv.vertex(matrix4f, -1.0f, 1.0f, -0.5f).texture(m, n).color(0.1f, 0.1f, 0.1f, 1.0f);
        BufferRenderer.drawWithGlobalProgram(lv.end());
    }

    private static void renderUnderwaterOverlay(MinecraftClient client, MatrixStack matrices) {
        RenderSystem.setShader(GameRenderer::getPositionTexProgram);
        RenderSystem.setShaderTexture(0, UNDERWATER_TEXTURE);
        BlockPos lv = BlockPos.ofFloored(client.player.getX(), client.player.getEyeY(), client.player.getZ());
        float f = LightmapTextureManager.getBrightness(client.player.getWorld().getDimension(), client.player.getWorld().getLightLevel(lv));
        RenderSystem.enableBlend();
        RenderSystem.setShaderColor(f, f, f, 0.1f);
        float g = 4.0f;
        float h = -1.0f;
        float i = 1.0f;
        float j = -1.0f;
        float k = 1.0f;
        float l = -0.5f;
        float m = -client.player.getYaw() / 64.0f;
        float n = client.player.getPitch() / 64.0f;
        Matrix4f matrix4f = matrices.peek().getPositionMatrix();
        BufferBuilder lv2 = Tessellator.getInstance().begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_TEXTURE);
        lv2.vertex(matrix4f, -1.0f, -1.0f, -0.5f).texture(4.0f + m, 4.0f + n);
        lv2.vertex(matrix4f, 1.0f, -1.0f, -0.5f).texture(0.0f + m, 4.0f + n);
        lv2.vertex(matrix4f, 1.0f, 1.0f, -0.5f).texture(0.0f + m, 0.0f + n);
        lv2.vertex(matrix4f, -1.0f, 1.0f, -0.5f).texture(4.0f + m, 0.0f + n);
        BufferRenderer.drawWithGlobalProgram(lv2.end());
        RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, 1.0f);
        RenderSystem.disableBlend();
    }

    private static void renderFireOverlay(MinecraftClient client, MatrixStack matrices) {
        RenderSystem.setShader(GameRenderer::getPositionTexColorProgram);
        RenderSystem.depthFunc(519);
        RenderSystem.depthMask(false);
        RenderSystem.enableBlend();
        Sprite lv = ModelLoader.FIRE_1.getSprite();
        RenderSystem.setShaderTexture(0, lv.getAtlasId());
        float f = lv.getMinU();
        float g = lv.getMaxU();
        float h = (f + g) / 2.0f;
        float i = lv.getMinV();
        float j = lv.getMaxV();
        float k = (i + j) / 2.0f;
        float l = lv.getAnimationFrameDelta();
        float m = MathHelper.lerp(l, f, h);
        float n = MathHelper.lerp(l, g, h);
        float o = MathHelper.lerp(l, i, k);
        float p = MathHelper.lerp(l, j, k);
        float q = 1.0f;
        for (int r = 0; r < 2; ++r) {
            matrices.push();
            float s = -0.5f;
            float t = 0.5f;
            float u = -0.5f;
            float v = 0.5f;
            float w = -0.5f;
            matrices.translate((float)(-(r * 2 - 1)) * 0.24f, -0.3f, 0.0f);
            matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees((float)(r * 2 - 1) * 10.0f));
            Matrix4f matrix4f = matrices.peek().getPositionMatrix();
            BufferBuilder lv2 = Tessellator.getInstance().begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_TEXTURE_COLOR);
            lv2.vertex(matrix4f, -0.5f, -0.5f, -0.5f).texture(n, p).color(1.0f, 1.0f, 1.0f, 0.9f);
            lv2.vertex(matrix4f, 0.5f, -0.5f, -0.5f).texture(m, p).color(1.0f, 1.0f, 1.0f, 0.9f);
            lv2.vertex(matrix4f, 0.5f, 0.5f, -0.5f).texture(m, o).color(1.0f, 1.0f, 1.0f, 0.9f);
            lv2.vertex(matrix4f, -0.5f, 0.5f, -0.5f).texture(n, o).color(1.0f, 1.0f, 1.0f, 0.9f);
            BufferRenderer.drawWithGlobalProgram(lv2.end());
            matrices.pop();
        }
        RenderSystem.disableBlend();
        RenderSystem.depthMask(true);
        RenderSystem.depthFunc(515);
    }
}

