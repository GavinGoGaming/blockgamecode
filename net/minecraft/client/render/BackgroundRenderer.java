/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 * 
 * Could not load the following classes:
 *  net.fabricmc.api.EnvType
 *  net.fabricmc.api.Environment
 *  org.jetbrains.annotations.Nullable
 */
package net.minecraft.client.render;

import com.google.common.collect.Lists;
import com.mojang.blaze3d.systems.RenderSystem;
import java.util.List;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.block.enums.CameraSubmersionType;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.render.Camera;
import net.minecraft.client.render.FogShape;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.registry.tag.BiomeTags;
import net.minecraft.util.CubicSampler;
import net.minecraft.util.Util;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.biome.source.BiomeAccess;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector3f;

@Environment(value=EnvType.CLIENT)
public class BackgroundRenderer {
    private static final int WATER_FOG_LENGTH = 96;
    private static final List<StatusEffectFogModifier> FOG_MODIFIERS = Lists.newArrayList(new BlindnessFogModifier(), new DarknessFogModifier());
    public static final float WATER_FOG_CHANGE_DURATION = 5000.0f;
    private static float red;
    private static float green;
    private static float blue;
    private static int waterFogColor;
    private static int nextWaterFogColor;
    private static long lastWaterFogColorUpdateTime;

    public static void render(Camera camera, float tickDelta, ClientWorld world, int viewDistance, float skyDarkness) {
        LivingEntity lv9;
        CameraSubmersionType lv = camera.getSubmersionType();
        Entity lv2 = camera.getFocusedEntity();
        if (lv == CameraSubmersionType.WATER) {
            long l = Util.getMeasuringTimeMs();
            int j = world.getBiome(BlockPos.ofFloored(camera.getPos())).value().getWaterFogColor();
            if (lastWaterFogColorUpdateTime < 0L) {
                waterFogColor = j;
                nextWaterFogColor = j;
                lastWaterFogColorUpdateTime = l;
            }
            int k = waterFogColor >> 16 & 0xFF;
            int m = waterFogColor >> 8 & 0xFF;
            int n = waterFogColor & 0xFF;
            int o = nextWaterFogColor >> 16 & 0xFF;
            int p = nextWaterFogColor >> 8 & 0xFF;
            int q = nextWaterFogColor & 0xFF;
            float h = MathHelper.clamp((float)(l - lastWaterFogColorUpdateTime) / 5000.0f, 0.0f, 1.0f);
            float r = MathHelper.lerp(h, (float)o, (float)k);
            float s = MathHelper.lerp(h, (float)p, (float)m);
            float t = MathHelper.lerp(h, (float)q, (float)n);
            red = r / 255.0f;
            green = s / 255.0f;
            blue = t / 255.0f;
            if (waterFogColor != j) {
                waterFogColor = j;
                nextWaterFogColor = MathHelper.floor(r) << 16 | MathHelper.floor(s) << 8 | MathHelper.floor(t);
                lastWaterFogColorUpdateTime = l;
            }
        } else if (lv == CameraSubmersionType.LAVA) {
            red = 0.6f;
            green = 0.1f;
            blue = 0.0f;
            lastWaterFogColorUpdateTime = -1L;
        } else if (lv == CameraSubmersionType.POWDER_SNOW) {
            red = 0.623f;
            green = 0.734f;
            blue = 0.785f;
            lastWaterFogColorUpdateTime = -1L;
            RenderSystem.clearColor(red, green, blue, 0.0f);
        } else {
            float r;
            float s;
            float h;
            float u = 0.25f + 0.75f * (float)viewDistance / 32.0f;
            u = 1.0f - (float)Math.pow(u, 0.25);
            Vec3d lv3 = world.getSkyColor(camera.getPos(), tickDelta);
            float v = (float)lv3.x;
            float w = (float)lv3.y;
            float x2 = (float)lv3.z;
            float y2 = MathHelper.clamp(MathHelper.cos(world.getSkyAngle(tickDelta) * ((float)Math.PI * 2)) * 2.0f + 0.5f, 0.0f, 1.0f);
            BiomeAccess lv4 = world.getBiomeAccess();
            Vec3d lv5 = camera.getPos().subtract(2.0, 2.0, 2.0).multiply(0.25);
            Vec3d lv6 = CubicSampler.sampleColor(lv5, (x, y, z) -> world.getDimensionEffects().adjustFogColor(Vec3d.unpackRgb(lv4.getBiomeForNoiseGen(x, y, z).value().getFogColor()), y2));
            red = (float)lv6.getX();
            green = (float)lv6.getY();
            blue = (float)lv6.getZ();
            if (viewDistance >= 4) {
                float[] fs;
                h = MathHelper.sin(world.getSkyAngleRadians(tickDelta)) > 0.0f ? -1.0f : 1.0f;
                Vector3f vector3f = new Vector3f(h, 0.0f, 0.0f);
                s = camera.getHorizontalPlane().dot(vector3f);
                if (s < 0.0f) {
                    s = 0.0f;
                }
                if (s > 0.0f && (fs = world.getDimensionEffects().getFogColorOverride(world.getSkyAngle(tickDelta), tickDelta)) != null) {
                    red = red * (1.0f - (s *= fs[3])) + fs[0] * s;
                    green = green * (1.0f - s) + fs[1] * s;
                    blue = blue * (1.0f - s) + fs[2] * s;
                }
            }
            red += (v - red) * u;
            green += (w - green) * u;
            blue += (x2 - blue) * u;
            h = world.getRainGradient(tickDelta);
            if (h > 0.0f) {
                float r2 = 1.0f - h * 0.5f;
                s = 1.0f - h * 0.4f;
                red *= r2;
                green *= r2;
                blue *= s;
            }
            if ((r = world.getThunderGradient(tickDelta)) > 0.0f) {
                s = 1.0f - r * 0.5f;
                red *= s;
                green *= s;
                blue *= s;
            }
            lastWaterFogColorUpdateTime = -1L;
        }
        float u = ((float)camera.getPos().y - (float)world.getBottomY()) * world.getLevelProperties().getHorizonShadingRatio();
        StatusEffectFogModifier lv7 = BackgroundRenderer.getFogModifier(lv2, tickDelta);
        if (lv7 != null) {
            LivingEntity lv8 = (LivingEntity)lv2;
            u = lv7.applyColorModifier(lv8, lv8.getStatusEffect(lv7.getStatusEffect()), u, tickDelta);
        }
        if (u < 1.0f && lv != CameraSubmersionType.LAVA && lv != CameraSubmersionType.POWDER_SNOW) {
            if (u < 0.0f) {
                u = 0.0f;
            }
            u *= u;
            red *= u;
            green *= u;
            blue *= u;
        }
        if (skyDarkness > 0.0f) {
            red = red * (1.0f - skyDarkness) + red * 0.7f * skyDarkness;
            green = green * (1.0f - skyDarkness) + green * 0.6f * skyDarkness;
            blue = blue * (1.0f - skyDarkness) + blue * 0.6f * skyDarkness;
        }
        float v = lv == CameraSubmersionType.WATER ? (lv2 instanceof ClientPlayerEntity ? ((ClientPlayerEntity)lv2).getUnderwaterVisibility() : 1.0f) : (lv2 instanceof LivingEntity && (lv9 = (LivingEntity)lv2).hasStatusEffect(StatusEffects.NIGHT_VISION) && !lv9.hasStatusEffect(StatusEffects.DARKNESS) ? GameRenderer.getNightVisionStrength(lv9, tickDelta) : 0.0f);
        if (red != 0.0f && green != 0.0f && blue != 0.0f) {
            float w = Math.min(1.0f / red, Math.min(1.0f / green, 1.0f / blue));
            red = red * (1.0f - v) + red * w * v;
            green = green * (1.0f - v) + green * w * v;
            blue = blue * (1.0f - v) + blue * w * v;
        }
        RenderSystem.clearColor(red, green, blue, 0.0f);
    }

    public static void clearFog() {
        RenderSystem.setShaderFogStart(Float.MAX_VALUE);
    }

    @Nullable
    private static StatusEffectFogModifier getFogModifier(Entity entity, float tickDelta) {
        if (entity instanceof LivingEntity) {
            LivingEntity lv = (LivingEntity)entity;
            return FOG_MODIFIERS.stream().filter(modifier -> modifier.shouldApply(lv, tickDelta)).findFirst().orElse(null);
        }
        return null;
    }

    public static void applyFog(Camera camera, FogType fogType, float viewDistance, boolean thickFog, float tickDelta) {
        CameraSubmersionType lv = camera.getSubmersionType();
        Entity lv2 = camera.getFocusedEntity();
        FogData lv3 = new FogData(fogType);
        StatusEffectFogModifier lv4 = BackgroundRenderer.getFogModifier(lv2, tickDelta);
        if (lv == CameraSubmersionType.LAVA) {
            if (lv2.isSpectator()) {
                lv3.fogStart = -8.0f;
                lv3.fogEnd = viewDistance * 0.5f;
            } else if (lv2 instanceof LivingEntity && ((LivingEntity)lv2).hasStatusEffect(StatusEffects.FIRE_RESISTANCE)) {
                lv3.fogStart = 0.0f;
                lv3.fogEnd = 5.0f;
            } else {
                lv3.fogStart = 0.25f;
                lv3.fogEnd = 1.0f;
            }
        } else if (lv == CameraSubmersionType.POWDER_SNOW) {
            if (lv2.isSpectator()) {
                lv3.fogStart = -8.0f;
                lv3.fogEnd = viewDistance * 0.5f;
            } else {
                lv3.fogStart = 0.0f;
                lv3.fogEnd = 2.0f;
            }
        } else if (lv4 != null) {
            LivingEntity lv5 = (LivingEntity)lv2;
            StatusEffectInstance lv6 = lv5.getStatusEffect(lv4.getStatusEffect());
            if (lv6 != null) {
                lv4.applyStartEndModifier(lv3, lv5, lv6, viewDistance, tickDelta);
            }
        } else if (lv == CameraSubmersionType.WATER) {
            lv3.fogStart = -8.0f;
            lv3.fogEnd = 96.0f;
            if (lv2 instanceof ClientPlayerEntity) {
                ClientPlayerEntity lv7 = (ClientPlayerEntity)lv2;
                lv3.fogEnd *= Math.max(0.25f, lv7.getUnderwaterVisibility());
                RegistryEntry<Biome> lv8 = lv7.getWorld().getBiome(lv7.getBlockPos());
                if (lv8.isIn(BiomeTags.HAS_CLOSER_WATER_FOG)) {
                    lv3.fogEnd *= 0.85f;
                }
            }
            if (lv3.fogEnd > viewDistance) {
                lv3.fogEnd = viewDistance;
                lv3.fogShape = FogShape.CYLINDER;
            }
        } else if (thickFog) {
            lv3.fogStart = viewDistance * 0.05f;
            lv3.fogEnd = Math.min(viewDistance, 192.0f) * 0.5f;
        } else if (fogType == FogType.FOG_SKY) {
            lv3.fogStart = 0.0f;
            lv3.fogEnd = viewDistance;
            lv3.fogShape = FogShape.CYLINDER;
        } else {
            float h = MathHelper.clamp(viewDistance / 10.0f, 4.0f, 64.0f);
            lv3.fogStart = viewDistance - h;
            lv3.fogEnd = viewDistance;
            lv3.fogShape = FogShape.CYLINDER;
        }
        RenderSystem.setShaderFogStart(lv3.fogStart);
        RenderSystem.setShaderFogEnd(lv3.fogEnd);
        RenderSystem.setShaderFogShape(lv3.fogShape);
    }

    public static void applyFogColor() {
        RenderSystem.setShaderFogColor(red, green, blue);
    }

    static {
        waterFogColor = -1;
        nextWaterFogColor = -1;
        lastWaterFogColorUpdateTime = -1L;
    }

    @Environment(value=EnvType.CLIENT)
    static interface StatusEffectFogModifier {
        public RegistryEntry<StatusEffect> getStatusEffect();

        public void applyStartEndModifier(FogData var1, LivingEntity var2, StatusEffectInstance var3, float var4, float var5);

        default public boolean shouldApply(LivingEntity entity, float tickDelta) {
            return entity.hasStatusEffect(this.getStatusEffect());
        }

        default public float applyColorModifier(LivingEntity entity, StatusEffectInstance effect, float f, float tickDelta) {
            StatusEffectInstance lv = entity.getStatusEffect(this.getStatusEffect());
            if (lv != null) {
                f = lv.isDurationBelow(19) ? 1.0f - (float)lv.getDuration() / 20.0f : 0.0f;
            }
            return f;
        }
    }

    @Environment(value=EnvType.CLIENT)
    static class FogData {
        public final FogType fogType;
        public float fogStart;
        public float fogEnd;
        public FogShape fogShape = FogShape.SPHERE;

        public FogData(FogType fogType) {
            this.fogType = fogType;
        }
    }

    @Environment(value=EnvType.CLIENT)
    public static enum FogType {
        FOG_SKY,
        FOG_TERRAIN;

    }

    @Environment(value=EnvType.CLIENT)
    static class BlindnessFogModifier
    implements StatusEffectFogModifier {
        BlindnessFogModifier() {
        }

        @Override
        public RegistryEntry<StatusEffect> getStatusEffect() {
            return StatusEffects.BLINDNESS;
        }

        @Override
        public void applyStartEndModifier(FogData fogData, LivingEntity entity, StatusEffectInstance effect, float viewDistance, float tickDelta) {
            float h;
            float f = h = effect.isInfinite() ? 5.0f : MathHelper.lerp(Math.min(1.0f, (float)effect.getDuration() / 20.0f), viewDistance, 5.0f);
            if (fogData.fogType == FogType.FOG_SKY) {
                fogData.fogStart = 0.0f;
                fogData.fogEnd = h * 0.8f;
            } else {
                fogData.fogStart = h * 0.25f;
                fogData.fogEnd = h;
            }
        }
    }

    @Environment(value=EnvType.CLIENT)
    static class DarknessFogModifier
    implements StatusEffectFogModifier {
        DarknessFogModifier() {
        }

        @Override
        public RegistryEntry<StatusEffect> getStatusEffect() {
            return StatusEffects.DARKNESS;
        }

        @Override
        public void applyStartEndModifier(FogData fogData, LivingEntity entity, StatusEffectInstance effect, float viewDistance, float tickDelta) {
            float h = MathHelper.lerp(effect.getFadeFactor(entity, tickDelta), viewDistance, 15.0f);
            fogData.fogStart = fogData.fogType == FogType.FOG_SKY ? 0.0f : h * 0.75f;
            fogData.fogEnd = h;
        }

        @Override
        public float applyColorModifier(LivingEntity entity, StatusEffectInstance effect, float f, float tickDelta) {
            return 1.0f - effect.getFadeFactor(entity, tickDelta);
        }
    }
}

