/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 * 
 * Could not load the following classes:
 *  net.fabricmc.api.EnvType
 *  net.fabricmc.api.Environment
 *  org.jetbrains.annotations.Nullable
 */
package net.minecraft.client.render;

import com.mojang.blaze3d.systems.RenderSystem;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gl.VertexBuffer;
import net.minecraft.client.render.BuiltBuffer;
import net.minecraft.client.render.VertexFormat;
import org.jetbrains.annotations.Nullable;

@Environment(value=EnvType.CLIENT)
public class BufferRenderer {
    @Nullable
    private static VertexBuffer currentVertexBuffer;

    public static void reset() {
        if (currentVertexBuffer != null) {
            BufferRenderer.resetCurrentVertexBuffer();
            VertexBuffer.unbind();
        }
    }

    public static void resetCurrentVertexBuffer() {
        currentVertexBuffer = null;
    }

    public static void drawWithGlobalProgram(BuiltBuffer buffer) {
        if (!RenderSystem.isOnRenderThreadOrInit()) {
            RenderSystem.recordRenderCall(() -> BufferRenderer.drawWithGlobalProgramInternal(buffer));
        } else {
            BufferRenderer.drawWithGlobalProgramInternal(buffer);
        }
    }

    private static void drawWithGlobalProgramInternal(BuiltBuffer buffer) {
        VertexBuffer lv = BufferRenderer.upload(buffer);
        lv.draw(RenderSystem.getModelViewMatrix(), RenderSystem.getProjectionMatrix(), RenderSystem.getShader());
    }

    public static void draw(BuiltBuffer buffer) {
        VertexBuffer lv = BufferRenderer.upload(buffer);
        lv.draw();
    }

    private static VertexBuffer upload(BuiltBuffer buffer) {
        RenderSystem.assertOnRenderThread();
        VertexBuffer lv = BufferRenderer.bind(buffer.getDrawParameters().format());
        lv.upload(buffer);
        return lv;
    }

    private static VertexBuffer bind(VertexFormat vertexFormat) {
        VertexBuffer lv = vertexFormat.getBuffer();
        BufferRenderer.bind(lv);
        return lv;
    }

    private static void bind(VertexBuffer vertexBuffer) {
        if (vertexBuffer != currentVertexBuffer) {
            vertexBuffer.bind();
            currentVertexBuffer = vertexBuffer;
        }
    }
}

