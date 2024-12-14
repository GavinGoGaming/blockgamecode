/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 * 
 * Could not load the following classes:
 *  net.fabricmc.api.EnvType
 *  net.fabricmc.api.Environment
 *  org.jetbrains.annotations.Nullable
 */
package net.minecraft.client.particle;

import com.mojang.blaze3d.systems.RenderSystem;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.render.BufferBuilder;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.client.render.Tessellator;
import net.minecraft.client.render.VertexFormat;
import net.minecraft.client.render.VertexFormats;
import net.minecraft.client.texture.SpriteAtlasTexture;
import net.minecraft.client.texture.TextureManager;
import org.jetbrains.annotations.Nullable;

@Environment(value=EnvType.CLIENT)
public interface ParticleTextureSheet {
    public static final ParticleTextureSheet TERRAIN_SHEET = new ParticleTextureSheet(){

        @Override
        public BufferBuilder begin(Tessellator tessellator, TextureManager textureManager) {
            RenderSystem.enableBlend();
            RenderSystem.defaultBlendFunc();
            RenderSystem.depthMask(true);
            RenderSystem.setShaderTexture(0, SpriteAtlasTexture.BLOCK_ATLAS_TEXTURE);
            return tessellator.begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_TEXTURE_COLOR_LIGHT);
        }

        public String toString() {
            return "TERRAIN_SHEET";
        }
    };
    public static final ParticleTextureSheet PARTICLE_SHEET_OPAQUE = new ParticleTextureSheet(){

        @Override
        public BufferBuilder begin(Tessellator tessellator, TextureManager textureManager) {
            RenderSystem.disableBlend();
            RenderSystem.depthMask(true);
            RenderSystem.setShader(GameRenderer::getParticleProgram);
            RenderSystem.setShaderTexture(0, SpriteAtlasTexture.PARTICLE_ATLAS_TEXTURE);
            return tessellator.begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_TEXTURE_COLOR_LIGHT);
        }

        public String toString() {
            return "PARTICLE_SHEET_OPAQUE";
        }
    };
    public static final ParticleTextureSheet PARTICLE_SHEET_TRANSLUCENT = new ParticleTextureSheet(){

        @Override
        public BufferBuilder begin(Tessellator tessellator, TextureManager textureManager) {
            RenderSystem.depthMask(true);
            RenderSystem.setShaderTexture(0, SpriteAtlasTexture.PARTICLE_ATLAS_TEXTURE);
            RenderSystem.enableBlend();
            RenderSystem.defaultBlendFunc();
            return tessellator.begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_TEXTURE_COLOR_LIGHT);
        }

        public String toString() {
            return "PARTICLE_SHEET_TRANSLUCENT";
        }
    };
    public static final ParticleTextureSheet PARTICLE_SHEET_LIT = new ParticleTextureSheet(){

        @Override
        public BufferBuilder begin(Tessellator tessellator, TextureManager textureManager) {
            RenderSystem.disableBlend();
            RenderSystem.depthMask(true);
            RenderSystem.setShaderTexture(0, SpriteAtlasTexture.PARTICLE_ATLAS_TEXTURE);
            return tessellator.begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_TEXTURE_COLOR_LIGHT);
        }

        public String toString() {
            return "PARTICLE_SHEET_LIT";
        }
    };
    public static final ParticleTextureSheet CUSTOM = new ParticleTextureSheet(){

        @Override
        public BufferBuilder begin(Tessellator tessellator, TextureManager textureManager) {
            RenderSystem.depthMask(true);
            RenderSystem.disableBlend();
            return tessellator.begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_TEXTURE_COLOR_LIGHT);
        }

        public String toString() {
            return "CUSTOM";
        }
    };
    public static final ParticleTextureSheet NO_RENDER = new ParticleTextureSheet(){

        @Override
        @Nullable
        public BufferBuilder begin(Tessellator tessellator, TextureManager textureManager) {
            return null;
        }

        public String toString() {
            return "NO_RENDER";
        }
    };

    @Nullable
    public BufferBuilder begin(Tessellator var1, TextureManager var2);
}

