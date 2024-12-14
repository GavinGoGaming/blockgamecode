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
import com.google.common.collect.Sets;
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
import java.io.Reader;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Set;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gl.Framebuffer;
import net.minecraft.client.gl.GlImportProcessor;
import net.minecraft.client.gl.GlProgramManager;
import net.minecraft.client.gl.GlUniform;
import net.minecraft.client.gl.ShaderProgramSetupView;
import net.minecraft.client.gl.ShaderStage;
import net.minecraft.client.gl.Uniform;
import net.minecraft.client.render.VertexFormat;
import net.minecraft.client.texture.AbstractTexture;
import net.minecraft.client.util.Window;
import net.minecraft.resource.Resource;
import net.minecraft.resource.ResourceFactory;
import net.minecraft.util.Identifier;
import net.minecraft.util.InvalidHierarchicalFileException;
import net.minecraft.util.JsonHelper;
import net.minecraft.util.PathUtil;
import org.apache.commons.io.IOUtils;
import org.jetbrains.annotations.Nullable;
import org.joml.Matrix4f;
import org.slf4j.Logger;

@Environment(value=EnvType.CLIENT)
public class ShaderProgram
implements ShaderProgramSetupView,
AutoCloseable {
    public static final String SHADERS_DIRECTORY = "shaders";
    private static final String CORE_DIRECTORY = "shaders/core/";
    private static final String INCLUDE_DIRECTORY = "shaders/include/";
    static final Logger LOGGER = LogUtils.getLogger();
    private static final Uniform DEFAULT_UNIFORM = new Uniform();
    private static final boolean field_32780 = true;
    private static ShaderProgram activeProgram;
    private static int activeProgramGlRef;
    private final Map<String, Object> samplers = Maps.newHashMap();
    private final List<String> samplerNames = Lists.newArrayList();
    private final List<Integer> loadedSamplerIds = Lists.newArrayList();
    private final List<GlUniform> uniforms = Lists.newArrayList();
    private final List<Integer> loadedUniformIds = Lists.newArrayList();
    private final Map<String, GlUniform> loadedUniforms = Maps.newHashMap();
    private final int glRef;
    private final String name;
    private boolean dirty;
    private final ShaderStage vertexShader;
    private final ShaderStage fragmentShader;
    private final VertexFormat format;
    @Nullable
    public final GlUniform modelViewMat;
    @Nullable
    public final GlUniform projectionMat;
    @Nullable
    public final GlUniform textureMat;
    @Nullable
    public final GlUniform screenSize;
    @Nullable
    public final GlUniform colorModulator;
    @Nullable
    public final GlUniform light0Direction;
    @Nullable
    public final GlUniform light1Direction;
    @Nullable
    public final GlUniform glintAlpha;
    @Nullable
    public final GlUniform fogStart;
    @Nullable
    public final GlUniform fogEnd;
    @Nullable
    public final GlUniform fogColor;
    @Nullable
    public final GlUniform fogShape;
    @Nullable
    public final GlUniform lineWidth;
    @Nullable
    public final GlUniform gameTime;
    @Nullable
    public final GlUniform chunkOffset;

    public ShaderProgram(ResourceFactory factory, String name, VertexFormat format) throws IOException {
        this.name = name;
        this.format = format;
        Identifier lv = Identifier.ofVanilla(CORE_DIRECTORY + name + ".json");
        try (BufferedReader reader = factory.openAsReader(lv);){
            JsonArray jsonArray2;
            JsonObject jsonObject = JsonHelper.deserialize(reader);
            String string2 = JsonHelper.getString(jsonObject, "vertex");
            String string3 = JsonHelper.getString(jsonObject, "fragment");
            JsonArray jsonArray = JsonHelper.getArray(jsonObject, "samplers", null);
            if (jsonArray != null) {
                int i = 0;
                for (JsonElement jsonElement : jsonArray) {
                    try {
                        this.readSampler(jsonElement);
                    } catch (Exception exception) {
                        InvalidHierarchicalFileException lv2 = InvalidHierarchicalFileException.wrap(exception);
                        lv2.addInvalidKey("samplers[" + i + "]");
                        throw lv2;
                    }
                    ++i;
                }
            }
            if ((jsonArray2 = JsonHelper.getArray(jsonObject, "uniforms", null)) != null) {
                int j = 0;
                for (JsonElement jsonElement2 : jsonArray2) {
                    try {
                        this.addUniform(jsonElement2);
                    } catch (Exception exception2) {
                        InvalidHierarchicalFileException lv3 = InvalidHierarchicalFileException.wrap(exception2);
                        lv3.addInvalidKey("uniforms[" + j + "]");
                        throw lv3;
                    }
                    ++j;
                }
            }
            this.vertexShader = ShaderProgram.loadShader(factory, ShaderStage.Type.VERTEX, string2);
            this.fragmentShader = ShaderProgram.loadShader(factory, ShaderStage.Type.FRAGMENT, string3);
            this.glRef = GlProgramManager.createProgram();
            int j = 0;
            for (String string4 : format.getAttributeNames()) {
                GlUniform.bindAttribLocation(this.glRef, j, string4);
                ++j;
            }
            GlProgramManager.linkProgram(this);
            this.loadReferences();
        } catch (Exception exception3) {
            InvalidHierarchicalFileException lv4 = InvalidHierarchicalFileException.wrap(exception3);
            lv4.addInvalidFile(lv.getPath());
            throw lv4;
        }
        this.markUniformsDirty();
        this.modelViewMat = this.getUniform("ModelViewMat");
        this.projectionMat = this.getUniform("ProjMat");
        this.textureMat = this.getUniform("TextureMat");
        this.screenSize = this.getUniform("ScreenSize");
        this.colorModulator = this.getUniform("ColorModulator");
        this.light0Direction = this.getUniform("Light0_Direction");
        this.light1Direction = this.getUniform("Light1_Direction");
        this.glintAlpha = this.getUniform("GlintAlpha");
        this.fogStart = this.getUniform("FogStart");
        this.fogEnd = this.getUniform("FogEnd");
        this.fogColor = this.getUniform("FogColor");
        this.fogShape = this.getUniform("FogShape");
        this.lineWidth = this.getUniform("LineWidth");
        this.gameTime = this.getUniform("GameTime");
        this.chunkOffset = this.getUniform("ChunkOffset");
    }

    private static ShaderStage loadShader(final ResourceFactory factory, ShaderStage.Type type, String name) throws IOException {
        ShaderStage lv3;
        ShaderStage lv = type.getLoadedShaders().get(name);
        if (lv == null) {
            String string2 = CORE_DIRECTORY + name + type.getFileExtension();
            Resource lv2 = factory.getResourceOrThrow(Identifier.ofVanilla(string2));
            try (InputStream inputStream = lv2.getInputStream();){
                final String string3 = PathUtil.getPosixFullPath(string2);
                lv3 = ShaderStage.createFromResource(type, name, inputStream, lv2.getPackId(), new GlImportProcessor(){
                    private final Set<String> visitedImports = Sets.newHashSet();

                    @Override
                    public String loadImport(boolean inline, String name) {
                        String string;
                        block9: {
                            name = PathUtil.normalizeToPosix((inline ? string3 : ShaderProgram.INCLUDE_DIRECTORY) + name);
                            if (!this.visitedImports.add(name)) {
                                return null;
                            }
                            Identifier lv = Identifier.of(name);
                            BufferedReader reader = factory.openAsReader(lv);
                            try {
                                string = IOUtils.toString(reader);
                                if (reader == null) break block9;
                            } catch (Throwable throwable) {
                                try {
                                    if (reader != null) {
                                        try {
                                            ((Reader)reader).close();
                                        } catch (Throwable throwable2) {
                                            throwable.addSuppressed(throwable2);
                                        }
                                    }
                                    throw throwable;
                                } catch (IOException iOException) {
                                    LOGGER.error("Could not open GLSL import {}: {}", (Object)name, (Object)iOException.getMessage());
                                    return "#error " + iOException.getMessage();
                                }
                            }
                            ((Reader)reader).close();
                        }
                        return string;
                    }
                });
            }
        } else {
            lv3 = lv;
        }
        return lv3;
    }

    @Override
    public void close() {
        for (GlUniform lv : this.uniforms) {
            lv.close();
        }
        GlProgramManager.deleteProgram(this);
    }

    public void unbind() {
        RenderSystem.assertOnRenderThread();
        GlProgramManager.useProgram(0);
        activeProgramGlRef = -1;
        activeProgram = null;
        int i = GlStateManager._getActiveTexture();
        for (int j = 0; j < this.loadedSamplerIds.size(); ++j) {
            if (this.samplers.get(this.samplerNames.get(j)) == null) continue;
            GlStateManager._activeTexture(GlConst.GL_TEXTURE0 + j);
            GlStateManager._bindTexture(0);
        }
        GlStateManager._activeTexture(i);
    }

    public void bind() {
        RenderSystem.assertOnRenderThread();
        this.dirty = false;
        activeProgram = this;
        if (this.glRef != activeProgramGlRef) {
            GlProgramManager.useProgram(this.glRef);
            activeProgramGlRef = this.glRef;
        }
        int i = GlStateManager._getActiveTexture();
        for (int j = 0; j < this.loadedSamplerIds.size(); ++j) {
            String string = this.samplerNames.get(j);
            if (this.samplers.get(string) == null) continue;
            int k = GlUniform.getUniformLocation(this.glRef, string);
            GlUniform.uniform1(k, j);
            RenderSystem.activeTexture(GlConst.GL_TEXTURE0 + j);
            Object object = this.samplers.get(string);
            int l = -1;
            if (object instanceof Framebuffer) {
                l = ((Framebuffer)object).getColorAttachment();
            } else if (object instanceof AbstractTexture) {
                l = ((AbstractTexture)object).getGlId();
            } else if (object instanceof Integer) {
                l = (Integer)object;
            }
            if (l == -1) continue;
            RenderSystem.bindTexture(l);
        }
        GlStateManager._activeTexture(i);
        for (GlUniform lv : this.uniforms) {
            lv.upload();
        }
    }

    @Override
    public void markUniformsDirty() {
        this.dirty = true;
    }

    @Nullable
    public GlUniform getUniform(String name) {
        RenderSystem.assertOnRenderThread();
        return this.loadedUniforms.get(name);
    }

    public Uniform getUniformOrDefault(String name) {
        GlUniform lv = this.getUniform(name);
        return lv == null ? DEFAULT_UNIFORM : lv;
    }

    private void loadReferences() {
        int i;
        RenderSystem.assertOnRenderThread();
        IntArrayList intList = new IntArrayList();
        for (i = 0; i < this.samplerNames.size(); ++i) {
            String string = this.samplerNames.get(i);
            int j = GlUniform.getUniformLocation(this.glRef, string);
            if (j == -1) {
                LOGGER.warn("Shader {} could not find sampler named {} in the specified shader program.", (Object)this.name, (Object)string);
                this.samplers.remove(string);
                intList.add(i);
                continue;
            }
            this.loadedSamplerIds.add(j);
        }
        for (i = intList.size() - 1; i >= 0; --i) {
            int k = intList.getInt(i);
            this.samplerNames.remove(k);
        }
        for (GlUniform lv : this.uniforms) {
            String string2 = lv.getName();
            int l = GlUniform.getUniformLocation(this.glRef, string2);
            if (l == -1) {
                LOGGER.warn("Shader {} could not find uniform named {} in the specified shader program.", (Object)this.name, (Object)string2);
                continue;
            }
            this.loadedUniformIds.add(l);
            lv.setLocation(l);
            this.loadedUniforms.put(string2, lv);
        }
    }

    private void readSampler(JsonElement json) {
        JsonObject jsonObject = JsonHelper.asObject(json, "sampler");
        String string = JsonHelper.getString(jsonObject, "name");
        if (!JsonHelper.hasString(jsonObject, "file")) {
            this.samplers.put(string, null);
            this.samplerNames.add(string);
            return;
        }
        this.samplerNames.add(string);
    }

    public void addSampler(String name, Object sampler) {
        this.samplers.put(name, sampler);
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
            lv2.set(Arrays.copyOfRange(fs, 0, j));
        }
        this.uniforms.add(lv2);
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

    public VertexFormat getFormat() {
        return this.format;
    }

    public String getName() {
        return this.name;
    }

    @Override
    public int getGlRef() {
        return this.glRef;
    }

    public void initializeUniforms(VertexFormat.DrawMode drawMode, Matrix4f viewMatrix, Matrix4f projectionMatrix, Window window) {
        for (int i = 0; i < 12; ++i) {
            int j = RenderSystem.getShaderTexture(i);
            this.addSampler("Sampler" + i, j);
        }
        if (this.modelViewMat != null) {
            this.modelViewMat.set(viewMatrix);
        }
        if (this.projectionMat != null) {
            this.projectionMat.set(projectionMatrix);
        }
        if (this.colorModulator != null) {
            this.colorModulator.set(RenderSystem.getShaderColor());
        }
        if (this.glintAlpha != null) {
            this.glintAlpha.set(RenderSystem.getShaderGlintAlpha());
        }
        if (this.fogStart != null) {
            this.fogStart.set(RenderSystem.getShaderFogStart());
        }
        if (this.fogEnd != null) {
            this.fogEnd.set(RenderSystem.getShaderFogEnd());
        }
        if (this.fogColor != null) {
            this.fogColor.set(RenderSystem.getShaderFogColor());
        }
        if (this.fogShape != null) {
            this.fogShape.set(RenderSystem.getShaderFogShape().getId());
        }
        if (this.textureMat != null) {
            this.textureMat.set(RenderSystem.getTextureMatrix());
        }
        if (this.gameTime != null) {
            this.gameTime.set(RenderSystem.getShaderGameTime());
        }
        if (this.screenSize != null) {
            this.screenSize.set((float)window.getFramebufferWidth(), (float)window.getFramebufferHeight());
        }
        if (this.lineWidth != null && (drawMode == VertexFormat.DrawMode.LINES || drawMode == VertexFormat.DrawMode.LINE_STRIP)) {
            this.lineWidth.set(RenderSystem.getShaderLineWidth());
        }
        RenderSystem.setupShaderLights(this);
    }

    static {
        activeProgramGlRef = -1;
    }
}

