/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 * 
 * Could not load the following classes:
 *  net.fabricmc.api.EnvType
 *  net.fabricmc.api.Environment
 *  org.jetbrains.annotations.Nullable
 */
package net.minecraft.client.gl;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import com.mojang.blaze3d.platform.GlConst;
import com.mojang.blaze3d.systems.RenderSystem;
import java.io.BufferedReader;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gl.Framebuffer;
import net.minecraft.client.gl.GlUniform;
import net.minecraft.client.gl.PostEffectPass;
import net.minecraft.client.gl.SimpleFramebuffer;
import net.minecraft.client.texture.AbstractTexture;
import net.minecraft.client.texture.TextureManager;
import net.minecraft.resource.Resource;
import net.minecraft.resource.ResourceFactory;
import net.minecraft.util.Identifier;
import net.minecraft.util.InvalidHierarchicalFileException;
import net.minecraft.util.JsonHelper;
import org.jetbrains.annotations.Nullable;
import org.joml.Matrix4f;

@Environment(value=EnvType.CLIENT)
public class PostEffectProcessor
implements AutoCloseable {
    private static final String MAIN_TARGET_NAME = "minecraft:main";
    private final Framebuffer mainTarget;
    private final ResourceFactory resourceFactory;
    private final String name;
    private final List<PostEffectPass> passes = Lists.newArrayList();
    private final Map<String, Framebuffer> targetsByName = Maps.newHashMap();
    private final List<Framebuffer> defaultSizedTargets = Lists.newArrayList();
    private Matrix4f projectionMatrix;
    private int width;
    private int height;
    private float time;
    private float lastTickDelta;

    public PostEffectProcessor(TextureManager textureManager, ResourceFactory resourceFactory, Framebuffer framebuffer, Identifier id) throws IOException, JsonSyntaxException {
        this.resourceFactory = resourceFactory;
        this.mainTarget = framebuffer;
        this.time = 0.0f;
        this.lastTickDelta = 0.0f;
        this.width = framebuffer.viewportWidth;
        this.height = framebuffer.viewportHeight;
        this.name = id.toString();
        this.setupProjectionMatrix();
        this.parseEffect(textureManager, id);
    }

    private void parseEffect(TextureManager textureManager, Identifier id) throws IOException, JsonSyntaxException {
        block15: {
            Resource lv = this.resourceFactory.getResourceOrThrow(id);
            try (BufferedReader reader = lv.getReader();){
                int i;
                JsonArray jsonArray;
                JsonObject jsonObject = JsonHelper.deserialize(reader);
                if (JsonHelper.hasArray(jsonObject, "targets")) {
                    jsonArray = jsonObject.getAsJsonArray("targets");
                    i = 0;
                    for (JsonElement jsonElement : jsonArray) {
                        try {
                            this.parseTarget(jsonElement);
                        } catch (Exception exception) {
                            InvalidHierarchicalFileException lv2 = InvalidHierarchicalFileException.wrap(exception);
                            lv2.addInvalidKey("targets[" + i + "]");
                            throw lv2;
                        }
                        ++i;
                    }
                }
                if (!JsonHelper.hasArray(jsonObject, "passes")) break block15;
                jsonArray = jsonObject.getAsJsonArray("passes");
                i = 0;
                for (JsonElement jsonElement : jsonArray) {
                    try {
                        this.parsePass(textureManager, jsonElement);
                    } catch (Exception exception) {
                        InvalidHierarchicalFileException lv2 = InvalidHierarchicalFileException.wrap(exception);
                        lv2.addInvalidKey("passes[" + i + "]");
                        throw lv2;
                    }
                    ++i;
                }
            } catch (Exception exception2) {
                InvalidHierarchicalFileException lv3 = InvalidHierarchicalFileException.wrap(exception2);
                lv3.addInvalidFile(id.getPath() + " (" + lv.getPackId() + ")");
                throw lv3;
            }
        }
    }

    private void parseTarget(JsonElement jsonTarget) throws InvalidHierarchicalFileException {
        if (JsonHelper.isString(jsonTarget)) {
            this.addTarget(jsonTarget.getAsString(), this.width, this.height);
        } else {
            JsonObject jsonObject = JsonHelper.asObject(jsonTarget, "target");
            String string = JsonHelper.getString(jsonObject, "name");
            int i = JsonHelper.getInt(jsonObject, "width", this.width);
            int j = JsonHelper.getInt(jsonObject, "height", this.height);
            if (this.targetsByName.containsKey(string)) {
                throw new InvalidHierarchicalFileException(string + " is already defined");
            }
            this.addTarget(string, i, j);
        }
    }

    private void parsePass(TextureManager textureManager, JsonElement jsonPass) throws IOException {
        JsonArray jsonArray2;
        JsonObject jsonObject = JsonHelper.asObject(jsonPass, "pass");
        String string = JsonHelper.getString(jsonObject, "name");
        String string2 = JsonHelper.getString(jsonObject, "intarget");
        String string3 = JsonHelper.getString(jsonObject, "outtarget");
        Framebuffer lv = this.getTarget(string2);
        Framebuffer lv2 = this.getTarget(string3);
        boolean bl = JsonHelper.getBoolean(jsonObject, "use_linear_filter", false);
        if (lv == null) {
            throw new InvalidHierarchicalFileException("Input target '" + string2 + "' does not exist");
        }
        if (lv2 == null) {
            throw new InvalidHierarchicalFileException("Output target '" + string3 + "' does not exist");
        }
        PostEffectPass lv3 = this.addPass(string, lv, lv2, bl);
        JsonArray jsonArray = JsonHelper.getArray(jsonObject, "auxtargets", null);
        if (jsonArray != null) {
            int i = 0;
            for (JsonElement jsonElement2 : jsonArray) {
                try {
                    String string6;
                    boolean bl2;
                    JsonObject jsonObject2 = JsonHelper.asObject(jsonElement2, "auxtarget");
                    String string4 = JsonHelper.getString(jsonObject2, "name");
                    String string5 = JsonHelper.getString(jsonObject2, "id");
                    if (string5.endsWith(":depth")) {
                        bl2 = true;
                        string6 = string5.substring(0, string5.lastIndexOf(58));
                    } else {
                        bl2 = false;
                        string6 = string5;
                    }
                    Framebuffer lv4 = this.getTarget(string6);
                    if (lv4 == null) {
                        if (bl2) {
                            throw new InvalidHierarchicalFileException("Render target '" + string6 + "' can't be used as depth buffer");
                        }
                        Identifier lv5 = Identifier.ofVanilla("textures/effect/" + string6 + ".png");
                        this.resourceFactory.getResource(lv5).orElseThrow(() -> new InvalidHierarchicalFileException("Render target or texture '" + string6 + "' does not exist"));
                        RenderSystem.setShaderTexture(0, lv5);
                        textureManager.bindTexture(lv5);
                        AbstractTexture lv6 = textureManager.getTexture(lv5);
                        int j = JsonHelper.getInt(jsonObject2, "width");
                        int k = JsonHelper.getInt(jsonObject2, "height");
                        boolean bl3 = JsonHelper.getBoolean(jsonObject2, "bilinear");
                        if (bl3) {
                            RenderSystem.texParameter(GlConst.GL_TEXTURE_2D, GlConst.GL_TEXTURE_MIN_FILTER, GlConst.GL_LINEAR);
                            RenderSystem.texParameter(GlConst.GL_TEXTURE_2D, GlConst.GL_TEXTURE_MAG_FILTER, GlConst.GL_LINEAR);
                        } else {
                            RenderSystem.texParameter(GlConst.GL_TEXTURE_2D, GlConst.GL_TEXTURE_MIN_FILTER, GlConst.GL_NEAREST);
                            RenderSystem.texParameter(GlConst.GL_TEXTURE_2D, GlConst.GL_TEXTURE_MAG_FILTER, GlConst.GL_NEAREST);
                        }
                        lv3.addAuxTarget(string4, lv6::getGlId, j, k);
                    } else if (bl2) {
                        lv3.addAuxTarget(string4, lv4::getDepthAttachment, lv4.textureWidth, lv4.textureHeight);
                    } else {
                        lv3.addAuxTarget(string4, lv4::getColorAttachment, lv4.textureWidth, lv4.textureHeight);
                    }
                } catch (Exception exception) {
                    InvalidHierarchicalFileException lv7 = InvalidHierarchicalFileException.wrap(exception);
                    lv7.addInvalidKey("auxtargets[" + i + "]");
                    throw lv7;
                }
                ++i;
            }
        }
        if ((jsonArray2 = JsonHelper.getArray(jsonObject, "uniforms", null)) != null) {
            int l = 0;
            for (JsonElement jsonElement3 : jsonArray2) {
                try {
                    this.parseUniform(jsonElement3);
                } catch (Exception exception2) {
                    InvalidHierarchicalFileException lv8 = InvalidHierarchicalFileException.wrap(exception2);
                    lv8.addInvalidKey("uniforms[" + l + "]");
                    throw lv8;
                }
                ++l;
            }
        }
    }

    private void parseUniform(JsonElement jsonUniform) throws InvalidHierarchicalFileException {
        JsonObject jsonObject = JsonHelper.asObject(jsonUniform, "uniform");
        String string = JsonHelper.getString(jsonObject, "name");
        GlUniform lv = this.passes.get(this.passes.size() - 1).getProgram().getUniformByName(string);
        if (lv == null) {
            throw new InvalidHierarchicalFileException("Uniform '" + string + "' does not exist");
        }
        float[] fs = new float[4];
        int i = 0;
        JsonArray jsonArray = JsonHelper.getArray(jsonObject, "values");
        for (JsonElement jsonElement2 : jsonArray) {
            try {
                fs[i] = JsonHelper.asFloat(jsonElement2, "value");
            } catch (Exception exception) {
                InvalidHierarchicalFileException lv2 = InvalidHierarchicalFileException.wrap(exception);
                lv2.addInvalidKey("values[" + i + "]");
                throw lv2;
            }
            ++i;
        }
        switch (i) {
            case 0: {
                break;
            }
            case 1: {
                lv.set(fs[0]);
                break;
            }
            case 2: {
                lv.set(fs[0], fs[1]);
                break;
            }
            case 3: {
                lv.set(fs[0], fs[1], fs[2]);
                break;
            }
            case 4: {
                lv.setAndFlip(fs[0], fs[1], fs[2], fs[3]);
            }
        }
    }

    public Framebuffer getSecondaryTarget(String name) {
        return this.targetsByName.get(name);
    }

    public void addTarget(String name, int width, int height) {
        SimpleFramebuffer lv = new SimpleFramebuffer(width, height, true, MinecraftClient.IS_SYSTEM_MAC);
        lv.setClearColor(0.0f, 0.0f, 0.0f, 0.0f);
        this.targetsByName.put(name, lv);
        if (width == this.width && height == this.height) {
            this.defaultSizedTargets.add(lv);
        }
    }

    @Override
    public void close() {
        for (Framebuffer lv : this.targetsByName.values()) {
            lv.delete();
        }
        for (PostEffectPass lv2 : this.passes) {
            lv2.close();
        }
        this.passes.clear();
    }

    public PostEffectPass addPass(String programName, Framebuffer source, Framebuffer dest, boolean linear) throws IOException {
        PostEffectPass lv = new PostEffectPass(this.resourceFactory, programName, source, dest, linear);
        this.passes.add(this.passes.size(), lv);
        return lv;
    }

    private void setupProjectionMatrix() {
        this.projectionMatrix = new Matrix4f().setOrtho(0.0f, this.mainTarget.textureWidth, 0.0f, this.mainTarget.textureHeight, 0.1f, 1000.0f);
    }

    public void setupDimensions(int targetsWidth, int targetsHeight) {
        this.width = this.mainTarget.textureWidth;
        this.height = this.mainTarget.textureHeight;
        this.setupProjectionMatrix();
        for (PostEffectPass lv : this.passes) {
            lv.setProjectionMatrix(this.projectionMatrix);
        }
        for (Framebuffer lv2 : this.defaultSizedTargets) {
            lv2.resize(targetsWidth, targetsHeight, MinecraftClient.IS_SYSTEM_MAC);
        }
    }

    private void setTexFilter(int texFilter) {
        this.mainTarget.setTexFilter(texFilter);
        for (Framebuffer lv : this.targetsByName.values()) {
            lv.setTexFilter(texFilter);
        }
    }

    public void render(float tickDelta) {
        this.time += tickDelta;
        while (this.time > 20.0f) {
            this.time -= 20.0f;
        }
        int i = GlConst.GL_NEAREST;
        for (PostEffectPass lv : this.passes) {
            int j = lv.getTexFilter();
            if (i != j) {
                this.setTexFilter(j);
                i = j;
            }
            lv.render(this.time / 20.0f);
        }
        this.setTexFilter(GlConst.GL_NEAREST);
    }

    public void setUniforms(String name, float value) {
        for (PostEffectPass lv : this.passes) {
            lv.getProgram().getUniformByNameOrDummy(name).set(value);
        }
    }

    public final String getName() {
        return this.name;
    }

    @Nullable
    private Framebuffer getTarget(@Nullable String name) {
        if (name == null) {
            return null;
        }
        if (name.equals(MAIN_TARGET_NAME)) {
            return this.mainTarget;
        }
        return this.targetsByName.get(name);
    }
}

