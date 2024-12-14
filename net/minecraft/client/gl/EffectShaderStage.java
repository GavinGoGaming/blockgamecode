/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 * 
 * Could not load the following classes:
 *  net.fabricmc.api.EnvType
 *  net.fabricmc.api.Environment
 */
package net.minecraft.client.gl;

import com.mojang.blaze3d.systems.RenderSystem;
import java.io.IOException;
import java.io.InputStream;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gl.EffectShaderProgram;
import net.minecraft.client.gl.GlImportProcessor;
import net.minecraft.client.gl.ShaderProgramSetupView;
import net.minecraft.client.gl.ShaderStage;

@Environment(value=EnvType.CLIENT)
public class EffectShaderStage
extends ShaderStage {
    private static final GlImportProcessor LOADER = new GlImportProcessor(){

        @Override
        public String loadImport(boolean inline, String name) {
            return "#error Import statement not supported";
        }
    };
    private int refCount;

    private EffectShaderStage(ShaderStage.Type type, int glRef, String name) {
        super(type, glRef, name);
    }

    public void attachTo(EffectShaderProgram program) {
        RenderSystem.assertOnRenderThread();
        ++this.refCount;
        this.attachTo((ShaderProgramSetupView)program);
    }

    @Override
    public void release() {
        RenderSystem.assertOnRenderThread();
        --this.refCount;
        if (this.refCount <= 0) {
            super.release();
        }
    }

    public static EffectShaderStage createFromResource(ShaderStage.Type type, String name, InputStream stream, String domain) throws IOException {
        RenderSystem.assertOnRenderThread();
        int i = EffectShaderStage.load(type, name, stream, domain, LOADER);
        EffectShaderStage lv = new EffectShaderStage(type, i, name);
        type.getLoadedShaders().put(name, lv);
        return lv;
    }
}

