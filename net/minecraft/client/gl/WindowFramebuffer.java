/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 * 
 * Could not load the following classes:
 *  net.fabricmc.api.EnvType
 *  net.fabricmc.api.Environment
 */
package net.minecraft.client.gl;

import com.google.common.collect.ImmutableList;
import com.mojang.blaze3d.platform.GlConst;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.platform.TextureUtil;
import com.mojang.blaze3d.systems.RenderSystem;
import java.util.List;
import java.util.Objects;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gl.Framebuffer;

@Environment(value=EnvType.CLIENT)
public class WindowFramebuffer
extends Framebuffer {
    public static final int DEFAULT_WIDTH = 854;
    public static final int DEFAULT_HEIGHT = 480;
    static final Size DEFAULT = new Size(854, 480);

    public WindowFramebuffer(int width, int height) {
        super(true);
        this.init(width, height);
    }

    private void init(int width, int height) {
        Size lv = this.findSuitableSize(width, height);
        this.fbo = GlStateManager.glGenFramebuffers();
        GlStateManager._glBindFramebuffer(GlConst.GL_FRAMEBUFFER, this.fbo);
        GlStateManager._bindTexture(this.colorAttachment);
        GlStateManager._texParameter(GlConst.GL_TEXTURE_2D, GlConst.GL_TEXTURE_MIN_FILTER, GlConst.GL_NEAREST);
        GlStateManager._texParameter(GlConst.GL_TEXTURE_2D, GlConst.GL_TEXTURE_MAG_FILTER, GlConst.GL_NEAREST);
        GlStateManager._texParameter(GlConst.GL_TEXTURE_2D, GlConst.GL_TEXTURE_WRAP_S, GlConst.GL_CLAMP_TO_EDGE);
        GlStateManager._texParameter(GlConst.GL_TEXTURE_2D, GlConst.GL_TEXTURE_WRAP_T, GlConst.GL_CLAMP_TO_EDGE);
        GlStateManager._glFramebufferTexture2D(GlConst.GL_FRAMEBUFFER, GlConst.GL_COLOR_ATTACHMENT0, GlConst.GL_TEXTURE_2D, this.colorAttachment, 0);
        GlStateManager._bindTexture(this.depthAttachment);
        GlStateManager._texParameter(GlConst.GL_TEXTURE_2D, GlConst.GL_TEXTURE_COMPARE_MODE, 0);
        GlStateManager._texParameter(GlConst.GL_TEXTURE_2D, GlConst.GL_TEXTURE_MIN_FILTER, GlConst.GL_NEAREST);
        GlStateManager._texParameter(GlConst.GL_TEXTURE_2D, GlConst.GL_TEXTURE_MAG_FILTER, GlConst.GL_NEAREST);
        GlStateManager._texParameter(GlConst.GL_TEXTURE_2D, GlConst.GL_TEXTURE_WRAP_S, GlConst.GL_CLAMP_TO_EDGE);
        GlStateManager._texParameter(GlConst.GL_TEXTURE_2D, GlConst.GL_TEXTURE_WRAP_T, GlConst.GL_CLAMP_TO_EDGE);
        GlStateManager._glFramebufferTexture2D(GlConst.GL_FRAMEBUFFER, GlConst.GL_DEPTH_ATTACHMENT, GlConst.GL_TEXTURE_2D, this.depthAttachment, 0);
        GlStateManager._bindTexture(0);
        this.viewportWidth = lv.width;
        this.viewportHeight = lv.height;
        this.textureWidth = lv.width;
        this.textureHeight = lv.height;
        this.checkFramebufferStatus();
        GlStateManager._glBindFramebuffer(GlConst.GL_FRAMEBUFFER, 0);
    }

    private Size findSuitableSize(int width, int height) {
        RenderSystem.assertOnRenderThreadOrInit();
        this.colorAttachment = TextureUtil.generateTextureId();
        this.depthAttachment = TextureUtil.generateTextureId();
        Attachment lv = Attachment.NONE;
        for (Size lv2 : Size.findCompatible(width, height)) {
            lv = Attachment.NONE;
            if (this.supportsColor(lv2)) {
                lv = lv.with(Attachment.COLOR);
            }
            if (this.supportsDepth(lv2)) {
                lv = lv.with(Attachment.DEPTH);
            }
            if (lv != Attachment.COLOR_DEPTH) continue;
            return lv2;
        }
        throw new RuntimeException("Unrecoverable GL_OUT_OF_MEMORY (allocated attachments = " + lv.name() + ")");
    }

    private boolean supportsColor(Size size) {
        RenderSystem.assertOnRenderThreadOrInit();
        GlStateManager._getError();
        GlStateManager._bindTexture(this.colorAttachment);
        GlStateManager._texImage2D(GlConst.GL_TEXTURE_2D, 0, GlConst.GL_RGBA8, size.width, size.height, 0, GlConst.GL_RGBA, GlConst.GL_UNSIGNED_BYTE, null);
        return GlStateManager._getError() != GlConst.GL_OUT_OF_MEMORY;
    }

    private boolean supportsDepth(Size size) {
        RenderSystem.assertOnRenderThreadOrInit();
        GlStateManager._getError();
        GlStateManager._bindTexture(this.depthAttachment);
        GlStateManager._texImage2D(GlConst.GL_TEXTURE_2D, 0, GlConst.GL_DEPTH_COMPONENT, size.width, size.height, 0, GlConst.GL_DEPTH_COMPONENT, GlConst.GL_FLOAT, null);
        return GlStateManager._getError() != GlConst.GL_OUT_OF_MEMORY;
    }

    @Environment(value=EnvType.CLIENT)
    static class Size {
        public final int width;
        public final int height;

        Size(int width, int height) {
            this.width = width;
            this.height = height;
        }

        static List<Size> findCompatible(int width, int height) {
            RenderSystem.assertOnRenderThreadOrInit();
            int k = RenderSystem.maxSupportedTextureSize();
            if (width <= 0 || width > k || height <= 0 || height > k) {
                return ImmutableList.of(DEFAULT);
            }
            return ImmutableList.of(new Size(width, height), DEFAULT);
        }

        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }
            if (o == null || this.getClass() != o.getClass()) {
                return false;
            }
            Size lv = (Size)o;
            return this.width == lv.width && this.height == lv.height;
        }

        public int hashCode() {
            return Objects.hash(this.width, this.height);
        }

        public String toString() {
            return this.width + "x" + this.height;
        }
    }

    @Environment(value=EnvType.CLIENT)
    static enum Attachment {
        NONE,
        COLOR,
        DEPTH,
        COLOR_DEPTH;

        private static final Attachment[] VALUES;

        Attachment with(Attachment other) {
            return VALUES[this.ordinal() | other.ordinal()];
        }

        static {
            VALUES = Attachment.values();
        }
    }
}

