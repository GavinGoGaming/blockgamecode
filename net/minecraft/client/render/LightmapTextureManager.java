/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 * 
 * Could not load the following classes:
 *  net.fabricmc.api.EnvType
 *  net.fabricmc.api.Environment
 */
package net.minecraft.client.render;

import com.mojang.blaze3d.platform.GlConst;
import com.mojang.blaze3d.systems.RenderSystem;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.client.texture.NativeImage;
import net.minecraft.client.texture.NativeImageBackedTexture;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.dimension.DimensionType;
import org.joml.Vector3f;

@Environment(value=EnvType.CLIENT)
public class LightmapTextureManager
implements AutoCloseable {
    public static final int MAX_LIGHT_COORDINATE = 0xF000F0;
    public static final int MAX_SKY_LIGHT_COORDINATE = 0xF00000;
    public static final int MAX_BLOCK_LIGHT_COORDINATE = 240;
    private final NativeImageBackedTexture texture;
    private final NativeImage image;
    private final Identifier textureIdentifier;
    private boolean dirty;
    private float flickerIntensity;
    private final GameRenderer renderer;
    private final MinecraftClient client;

    public LightmapTextureManager(GameRenderer renderer, MinecraftClient client) {
        this.renderer = renderer;
        this.client = client;
        this.texture = new NativeImageBackedTexture(16, 16, false);
        this.textureIdentifier = this.client.getTextureManager().registerDynamicTexture("light_map", this.texture);
        this.image = this.texture.getImage();
        for (int i = 0; i < 16; ++i) {
            for (int j = 0; j < 16; ++j) {
                this.image.setColor(j, i, -1);
            }
        }
        this.texture.upload();
    }

    @Override
    public void close() {
        this.texture.close();
    }

    public void tick() {
        this.flickerIntensity += (float)((Math.random() - Math.random()) * Math.random() * Math.random() * 0.1);
        this.flickerIntensity *= 0.9f;
        this.dirty = true;
    }

    public void disable() {
        RenderSystem.setShaderTexture(2, 0);
    }

    public void enable() {
        RenderSystem.setShaderTexture(2, this.textureIdentifier);
        this.client.getTextureManager().bindTexture(this.textureIdentifier);
        RenderSystem.texParameter(GlConst.GL_TEXTURE_2D, GlConst.GL_TEXTURE_MIN_FILTER, GlConst.GL_LINEAR);
        RenderSystem.texParameter(GlConst.GL_TEXTURE_2D, GlConst.GL_TEXTURE_MAG_FILTER, GlConst.GL_LINEAR);
    }

    private float getDarknessFactor(float delta) {
        StatusEffectInstance lv = this.client.player.getStatusEffect(StatusEffects.DARKNESS);
        if (lv != null) {
            return lv.getFadeFactor(this.client.player, delta);
        }
        return 0.0f;
    }

    private float getDarkness(LivingEntity entity, float factor, float delta) {
        float h = 0.45f * factor;
        return Math.max(0.0f, MathHelper.cos(((float)entity.age - delta) * (float)Math.PI * 0.025f) * h);
    }

    public void update(float delta) {
        if (!this.dirty) {
            return;
        }
        this.dirty = false;
        this.client.getProfiler().push("lightTex");
        ClientWorld lv = this.client.world;
        if (lv == null) {
            return;
        }
        float g = lv.getSkyBrightness(1.0f);
        float h = lv.getLightningTicksLeft() > 0 ? 1.0f : g * 0.95f + 0.05f;
        float i = this.client.options.getDarknessEffectScale().getValue().floatValue();
        float j = this.getDarknessFactor(delta) * i;
        float k = this.getDarkness(this.client.player, j, delta) * i;
        float l = this.client.player.getUnderwaterVisibility();
        float m = this.client.player.hasStatusEffect(StatusEffects.NIGHT_VISION) ? GameRenderer.getNightVisionStrength(this.client.player, delta) : (l > 0.0f && this.client.player.hasStatusEffect(StatusEffects.CONDUIT_POWER) ? l : 0.0f);
        Vector3f vector3f = new Vector3f(g, g, 1.0f).lerp(new Vector3f(1.0f, 1.0f, 1.0f), 0.35f);
        float n = this.flickerIntensity + 1.5f;
        Vector3f vector3f2 = new Vector3f();
        for (int o = 0; o < 16; ++o) {
            for (int p = 0; p < 16; ++p) {
                float w;
                Vector3f vector3f4;
                float v;
                float r;
                float q = LightmapTextureManager.getBrightness(lv.getDimension(), o) * h;
                float s = r = LightmapTextureManager.getBrightness(lv.getDimension(), p) * n;
                float t = r * ((r * 0.6f + 0.4f) * 0.6f + 0.4f);
                float u = r * (r * r * 0.6f + 0.4f);
                vector3f2.set(s, t, u);
                boolean bl = lv.getDimensionEffects().shouldBrightenLighting();
                if (bl) {
                    vector3f2.lerp(new Vector3f(0.99f, 1.12f, 1.0f), 0.25f);
                    LightmapTextureManager.clamp(vector3f2);
                } else {
                    Vector3f vector3f3 = new Vector3f(vector3f).mul(q);
                    vector3f2.add(vector3f3);
                    vector3f2.lerp(new Vector3f(0.75f, 0.75f, 0.75f), 0.04f);
                    if (this.renderer.getSkyDarkness(delta) > 0.0f) {
                        v = this.renderer.getSkyDarkness(delta);
                        vector3f4 = new Vector3f(vector3f2).mul(0.7f, 0.6f, 0.6f);
                        vector3f2.lerp(vector3f4, v);
                    }
                }
                if (m > 0.0f && (w = Math.max(vector3f2.x(), Math.max(vector3f2.y(), vector3f2.z()))) < 1.0f) {
                    v = 1.0f / w;
                    vector3f4 = new Vector3f(vector3f2).mul(v);
                    vector3f2.lerp(vector3f4, m);
                }
                if (!bl) {
                    if (k > 0.0f) {
                        vector3f2.add(-k, -k, -k);
                    }
                    LightmapTextureManager.clamp(vector3f2);
                }
                float w2 = this.client.options.getGamma().getValue().floatValue();
                Vector3f vector3f5 = new Vector3f(this.easeOutQuart(vector3f2.x), this.easeOutQuart(vector3f2.y), this.easeOutQuart(vector3f2.z));
                vector3f2.lerp(vector3f5, Math.max(0.0f, w2 - j));
                vector3f2.lerp(new Vector3f(0.75f, 0.75f, 0.75f), 0.04f);
                LightmapTextureManager.clamp(vector3f2);
                vector3f2.mul(255.0f);
                int x = 255;
                int y = (int)vector3f2.x();
                int z = (int)vector3f2.y();
                int aa = (int)vector3f2.z();
                this.image.setColor(p, o, 0xFF000000 | aa << 16 | z << 8 | y);
            }
        }
        this.texture.upload();
        this.client.getProfiler().pop();
    }

    private static void clamp(Vector3f vec) {
        vec.set(MathHelper.clamp(vec.x, 0.0f, 1.0f), MathHelper.clamp(vec.y, 0.0f, 1.0f), MathHelper.clamp(vec.z, 0.0f, 1.0f));
    }

    private float easeOutQuart(float x) {
        float g = 1.0f - x;
        return 1.0f - g * g * g * g;
    }

    public static float getBrightness(DimensionType type, int lightLevel) {
        float f = (float)lightLevel / 15.0f;
        float g = f / (4.0f - 3.0f * f);
        return MathHelper.lerp(type.ambientLight(), g, 1.0f);
    }

    public static int pack(int block, int sky) {
        return block << 4 | sky << 20;
    }

    public static int getBlockLightCoordinates(int light) {
        return light >> 4 & (MAX_BLOCK_LIGHT_COORDINATE | 0xFF0F);
    }

    public static int getSkyLightCoordinates(int light) {
        return light >> 20 & (MAX_BLOCK_LIGHT_COORDINATE | 0xFF0F);
    }
}

