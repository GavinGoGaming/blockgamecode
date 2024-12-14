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
import com.mojang.blaze3d.platform.GlConst;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.logging.LogUtils;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InvalidClassException;
import java.util.List;
import java.util.Map;
import java.util.function.IntSupplier;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gl.EffectShaderProgram;
import net.minecraft.client.gl.EffectShaderStage;
import net.minecraft.client.gl.GlBlendState;
import net.minecraft.client.gl.GlProgramManager;
import net.minecraft.client.gl.GlUniform;
import net.minecraft.client.gl.ShaderStage;
import net.minecraft.client.gl.Uniform;
import net.minecraft.resource.Resource;
import net.minecraft.resource.ResourceFactory;
import net.minecraft.util.Identifier;
import net.minecraft.util.InvalidHierarchicalFileException;
import net.minecraft.util.JsonHelper;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;

@Environment(value=EnvType.CLIENT)
public class JsonEffectShaderProgram
implements EffectShaderProgram,
AutoCloseable {
    private static final String PROGRAM_DIRECTORY = "shaders/program/";
    private static final Logger LOGGER = LogUtils.getLogger();
    private static final Uniform DEFAULT_UNIFORM = new Uniform();
    private static final boolean field_32683 = true;
    private static JsonEffectShaderProgram activeProgram;
    private static int activeProgramGlRef;
    private final Map<String, IntSupplier> samplerBinds = Maps.newHashMap();
    private final List<String> samplerNames = Lists.newArrayList();
    private final List<Integer> samplerLocations = Lists.newArrayList();
    private final List<GlUniform> uniformData = Lists.newArrayList();
    private final List<Integer> uniformLocations = Lists.newArrayList();
    private final Map<String, GlUniform> uniformByName = Maps.newHashMap();
    private final int glRef;
    private final String name;
    private boolean uniformStateDirty;
    private final GlBlendState blendState;
    private final List<Integer> attributeLocations;
    private final List<String> attributeNames;
    private final EffectShaderStage vertexShader;
    private final EffectShaderStage fragmentShader;

    public JsonEffectShaderProgram(ResourceFactory factory, String name) throws IOException {
        Identifier lv = Identifier.ofVanilla(PROGRAM_DIRECTORY + name + ".json");
        this.name = name;
        Resource lv2 = factory.getResourceOrThrow(lv);
        try (BufferedReader reader = lv2.getReader();){
            JsonArray jsonArray3;
            JsonArray jsonArray2;
            JsonObject jsonObject = JsonHelper.deserialize(reader);
            String string2 = JsonHelper.getString(jsonObject, "vertex");
            String string3 = JsonHelper.getString(jsonObject, "fragment");
            JsonArray jsonArray = JsonHelper.getArray(jsonObject, "samplers", null);
            if (jsonArray != null) {
                int i = 0;
                for (Object jsonElement : jsonArray) {
                    try {
                        this.addSampler((JsonElement)jsonElement);
                    } catch (Exception exception) {
                        InvalidHierarchicalFileException lv3 = InvalidHierarchicalFileException.wrap(exception);
                        lv3.addInvalidKey("samplers[" + i + "]");
                        throw lv3;
                    }
                    ++i;
                }
            }
            if ((jsonArray2 = JsonHelper.getArray(jsonObject, "attributes", null)) != null) {
                int j = 0;
                this.attributeLocations = Lists.newArrayListWithCapacity(jsonArray2.size());
                this.attributeNames = Lists.newArrayListWithCapacity(jsonArray2.size());
                for (Object jsonElement2 : jsonArray2) {
                    try {
                        this.attributeNames.add(JsonHelper.asString((JsonElement)jsonElement2, "attribute"));
                    } catch (Exception exception2) {
                        InvalidHierarchicalFileException lv4 = InvalidHierarchicalFileException.wrap(exception2);
                        lv4.addInvalidKey("attributes[" + j + "]");
                        throw lv4;
                    }
                    ++j;
                }
            } else {
                this.attributeLocations = null;
                this.attributeNames = null;
            }
            if ((jsonArray3 = JsonHelper.getArray(jsonObject, "uniforms", null)) != null) {
                int k = 0;
                for (JsonElement jsonElement3 : jsonArray3) {
                    try {
                        this.addUniform(jsonElement3);
                    } catch (Exception exception3) {
                        InvalidHierarchicalFileException lv5 = InvalidHierarchicalFileException.wrap(exception3);
                        lv5.addInvalidKey("uniforms[" + k + "]");
                        throw lv5;
                    }
                    ++k;
                }
            }
            this.blendState = JsonEffectShaderProgram.deserializeBlendState(JsonHelper.getObject(jsonObject, "blend", null));
            this.vertexShader = JsonEffectShaderProgram.loadEffect(factory, ShaderStage.Type.VERTEX, string2);
            this.fragmentShader = JsonEffectShaderProgram.loadEffect(factory, ShaderStage.Type.FRAGMENT, string3);
            this.glRef = GlProgramManager.createProgram();
            GlProgramManager.linkProgram(this);
            this.finalizeUniformsAndSamplers();
            if (this.attributeNames != null) {
                for (String string4 : this.attributeNames) {
                    int l = GlUniform.getAttribLocation(this.glRef, string4);
                    this.attributeLocations.add(l);
                }
            }
        } catch (Exception exception4) {
            InvalidHierarchicalFileException lv6 = InvalidHierarchicalFileException.wrap(exception4);
            lv6.addInvalidFile(lv.getPath() + " (" + lv2.getPackId() + ")");
            throw lv6;
        }
        this.markUniformsDirty();
    }

    public static EffectShaderStage loadEffect(ResourceFactory arg, ShaderStage.Type type, String name) throws IOException {
        EffectShaderStage lv4;
        ShaderStage lv = type.getLoadedShaders().get(name);
        if (lv != null && !(lv instanceof EffectShaderStage)) {
            throw new InvalidClassException("Program is not of type EffectProgram");
        }
        if (lv == null) {
            Identifier lv2 = Identifier.ofVanilla(PROGRAM_DIRECTORY + name + type.getFileExtension());
            Resource lv3 = arg.getResourceOrThrow(lv2);
            try (InputStream inputStream = lv3.getInputStream();){
                lv4 = EffectShaderStage.createFromResource(type, name, inputStream, lv3.getPackId());
            }
        } else {
            lv4 = (EffectShaderStage)lv;
        }
        return lv4;
    }

    public static GlBlendState deserializeBlendState(@Nullable JsonObject json) {
        if (json == null) {
            return new GlBlendState();
        }
        int i = GlConst.GL_FUNC_ADD;
        int j = 1;
        int k = 0;
        int l = 1;
        int m = 0;
        boolean bl = true;
        boolean bl2 = false;
        if (JsonHelper.hasString(json, "func") && (i = GlBlendState.getModeFromString(json.get("func").getAsString())) != GlConst.GL_FUNC_ADD) {
            bl = false;
        }
        if (JsonHelper.hasString(json, "srcrgb") && (j = GlBlendState.getFactorFromString(json.get("srcrgb").getAsString())) != 1) {
            bl = false;
        }
        if (JsonHelper.hasString(json, "dstrgb") && (k = GlBlendState.getFactorFromString(json.get("dstrgb").getAsString())) != 0) {
            bl = false;
        }
        if (JsonHelper.hasString(json, "srcalpha")) {
            l = GlBlendState.getFactorFromString(json.get("srcalpha").getAsString());
            if (l != 1) {
                bl = false;
            }
            bl2 = true;
        }
        if (JsonHelper.hasString(json, "dstalpha")) {
            m = GlBlendState.getFactorFromString(json.get("dstalpha").getAsString());
            if (m != 0) {
                bl = false;
            }
            bl2 = true;
        }
        if (bl) {
            return new GlBlendState();
        }
        if (bl2) {
            return new GlBlendState(j, k, l, m, i);
        }
        return new GlBlendState(j, k, i);
    }

    @Override
    public void close() {
        for (GlUniform lv : this.uniformData) {
            lv.close();
        }
        GlProgramManager.deleteProgram(this);
    }

    public void disable() {
        RenderSystem.assertOnRenderThread();
        GlProgramManager.useProgram(0);
        activeProgramGlRef = -1;
        activeProgram = null;
        for (int i = 0; i < this.samplerLocations.size(); ++i) {
            if (this.samplerBinds.get(this.samplerNames.get(i)) == null) continue;
            GlStateManager._activeTexture(GlConst.GL_TEXTURE0 + i);
            GlStateManager._bindTexture(0);
        }
    }

    public void enable() {
        this.uniformStateDirty = false;
        activeProgram = this;
        this.blendState.enable();
        if (this.glRef != activeProgramGlRef) {
            GlProgramManager.useProgram(this.glRef);
            activeProgramGlRef = this.glRef;
        }
        for (int i = 0; i < this.samplerLocations.size(); ++i) {
            String string = this.samplerNames.get(i);
            IntSupplier intSupplier = this.samplerBinds.get(string);
            if (intSupplier == null) continue;
            RenderSystem.activeTexture(GlConst.GL_TEXTURE0 + i);
            int j = intSupplier.getAsInt();
            if (j == -1) continue;
            RenderSystem.bindTexture(j);
            GlUniform.uniform1(this.samplerLocations.get(i), i);
        }
        for (GlUniform lv : this.uniformData) {
            lv.upload();
        }
    }

    @Override
    public void markUniformsDirty() {
        this.uniformStateDirty = true;
    }

    @Nullable
    public GlUniform getUniformByName(String name) {
        RenderSystem.assertOnRenderThread();
        return this.uniformByName.get(name);
    }

    public Uniform getUniformByNameOrDummy(String name) {
        GlUniform lv = this.getUniformByName(name);
        return lv == null ? DEFAULT_UNIFORM : lv;
    }

    private void finalizeUniformsAndSamplers() {
        int i;
        RenderSystem.assertOnRenderThread();
        IntArrayList intList = new IntArrayList();
        for (i = 0; i < this.samplerNames.size(); ++i) {
            String string = this.samplerNames.get(i);
            int j = GlUniform.getUniformLocation(this.glRef, string);
            if (j == -1) {
                LOGGER.warn("Shader {} could not find sampler named {} in the specified shader program.", (Object)this.name, (Object)string);
                this.samplerBinds.remove(string);
                intList.add(i);
                continue;
            }
            this.samplerLocations.add(j);
        }
        for (i = intList.size() - 1; i >= 0; --i) {
            this.samplerNames.remove(intList.getInt(i));
        }
        for (GlUniform lv : this.uniformData) {
            String string2 = lv.getName();
            int k = GlUniform.getUniformLocation(this.glRef, string2);
            if (k == -1) {
                LOGGER.warn("Shader {} could not find uniform named {} in the specified shader program.", (Object)this.name, (Object)string2);
                continue;
            }
            this.uniformLocations.add(k);
            lv.setLocation(k);
            this.uniformByName.put(string2, lv);
        }
    }

    private void addSampler(JsonElement json) {
        JsonObject jsonObject = JsonHelper.asObject(json, "sampler");
        String string = JsonHelper.getString(jsonObject, "name");
        if (!JsonHelper.hasString(jsonObject, "file")) {
            this.samplerBinds.put(string, null);
            this.samplerNames.add(string);
            return;
        }
        this.samplerNames.add(string);
    }

    public void bindSampler(String samplerName, IntSupplier intSupplier) {
        if (this.samplerBinds.containsKey(samplerName)) {
            this.samplerBinds.remove(samplerName);
        }
        this.samplerBinds.put(samplerName, intSupplier);
        this.markUniformsDirty();
    }

    private void addUniform(JsonElement json) throws InvalidHierarchicalFileException {
        JsonObject jsonObject = JsonHelper.asObject(json, "uniform");
        String string = JsonHelper.getString(jsonObject, "name");
        int i = GlUniform.getTypeIndex(JsonHelper.getString(jsonObject, "type"));
        int j = JsonHelper.getInt(jsonObject, "count");
        float[] fs = new float[Math.max(j, 16)];
        JsonArray jsonArray = JsonHelper.getArray(jsonObject, "values");
        if (jsonArray.size() != j && jsonArray.size() > 1) {
            throw new InvalidHierarchicalFileException("Invalid amount of values specified (expected " + j + ", found " + jsonArray.size() + ")");
        }
        int k = 0;
        for (JsonElement jsonElement2 : jsonArray) {
            try {
                fs[k] = JsonHelper.asFloat(jsonElement2, "value");
            } catch (Exception exception) {
                InvalidHierarchicalFileException lv = InvalidHierarchicalFileException.wrap(exception);
                lv.addInvalidKey("values[" + k + "]");
                throw lv;
            }
            ++k;
        }
        if (j > 1 && jsonArray.size() == 1) {
            while (k < j) {
                fs[k] = fs[0];
                ++k;
            }
        }
        int l = j > 1 && j <= 4 && i < 8 ? j - 1 : 0;
        GlUniform lv2 = new GlUniform(string, i + l, j, this);
        if (i <= 3) {
            lv2.setForDataType((int)fs[0], (int)fs[1], (int)fs[2], (int)fs[3]);
        } else if (i <= 7) {
            lv2.setForDataType(fs[0], fs[1], fs[2], fs[3]);
        } else {
            lv2.set(fs);
        }
        this.uniformData.add(lv2);
    }

    @Override
    public ShaderStage getVertexShader() {
        return this.vertexShader;
    }

    @Override
    public ShaderStage getFragmentShader() {
        return this.fragmentShader;
    }

    @Override
    public void attachReferencedShaders() {
        this.fragmentShader.attachTo(this);
        this.vertexShader.attachTo(this);
    }

    public String getName() {
        return this.name;
    }

    @Override
    public int getGlRef() {
        return this.glRef;
    }

    static {
        activeProgramGlRef = -1;
    }
}

