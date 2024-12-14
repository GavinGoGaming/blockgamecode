/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 * 
 * Could not load the following classes:
 *  net.fabricmc.api.EnvType
 *  net.fabricmc.api.Environment
 *  org.jetbrains.annotations.Nullable
 */
package net.minecraft.client.render;

import it.unimi.dsi.fastutil.objects.Object2ObjectArrayMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectMap;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.util.Identifier;
import net.minecraft.util.Util;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.dimension.DimensionType;
import net.minecraft.world.dimension.DimensionTypes;
import org.jetbrains.annotations.Nullable;

@Environment(value=EnvType.CLIENT)
public abstract class DimensionEffects {
    private static final Object2ObjectMap<Identifier, DimensionEffects> BY_IDENTIFIER = Util.make(new Object2ObjectArrayMap(), map -> {
        Overworld lv = new Overworld();
        map.defaultReturnValue(lv);
        map.put(DimensionTypes.OVERWORLD_ID, lv);
        map.put(DimensionTypes.THE_NETHER_ID, new Nether());
        map.put(DimensionTypes.THE_END_ID, new End());
    });
    private final float[] rgba = new float[4];
    private final float cloudsHeight;
    private final boolean alternateSkyColor;
    private final SkyType skyType;
    private final boolean brightenLighting;
    private final boolean darkened;

    public DimensionEffects(float cloudsHeight, boolean alternateSkyColor, SkyType skyType, boolean brightenLighting, boolean darkened) {
        this.cloudsHeight = cloudsHeight;
        this.alternateSkyColor = alternateSkyColor;
        this.skyType = skyType;
        this.brightenLighting = brightenLighting;
        this.darkened = darkened;
    }

    public static DimensionEffects byDimensionType(DimensionType dimensionType) {
        return (DimensionEffects)BY_IDENTIFIER.get(dimensionType.effects());
    }

    @Nullable
    public float[] getFogColorOverride(float skyAngle, float tickDelta) {
        float h = 0.4f;
        float i = MathHelper.cos(skyAngle * ((float)Math.PI * 2)) - 0.0f;
        float j = -0.0f;
        if (i >= -0.4f && i <= 0.4f) {
            float k = (i - -0.0f) / 0.4f * 0.5f + 0.5f;
            float l = 1.0f - (1.0f - MathHelper.sin(k * (float)Math.PI)) * 0.99f;
            l *= l;
            this.rgba[0] = k * 0.3f + 0.7f;
            this.rgba[1] = k * k * 0.7f + 0.2f;
            this.rgba[2] = k * k * 0.0f + 0.2f;
            this.rgba[3] = l;
            return this.rgba;
        }
        return null;
    }

    public float getCloudsHeight() {
        return this.cloudsHeight;
    }

    public boolean isAlternateSkyColor() {
        return this.alternateSkyColor;
    }

    public abstract Vec3d adjustFogColor(Vec3d var1, float var2);

    public abstract boolean useThickFog(int var1, int var2);

    public SkyType getSkyType() {
        return this.skyType;
    }

    public boolean shouldBrightenLighting() {
        return this.brightenLighting;
    }

    public boolean isDarkened() {
        return this.darkened;
    }

    @Environment(value=EnvType.CLIENT)
    public static enum SkyType {
        NONE,
        NORMAL,
        END;

    }

    @Environment(value=EnvType.CLIENT)
    public static class Overworld
    extends DimensionEffects {
        public static final int CLOUDS_HEIGHT = 192;

        public Overworld() {
            super(192.0f, true, SkyType.NORMAL, false, false);
        }

        @Override
        public Vec3d adjustFogColor(Vec3d color, float sunHeight) {
            return color.multiply(sunHeight * 0.94f + 0.06f, sunHeight * 0.94f + 0.06f, sunHeight * 0.91f + 0.09f);
        }

        @Override
        public boolean useThickFog(int camX, int camY) {
            return false;
        }
    }

    @Environment(value=EnvType.CLIENT)
    public static class Nether
    extends DimensionEffects {
        public Nether() {
            super(Float.NaN, true, SkyType.NONE, false, true);
        }

        @Override
        public Vec3d adjustFogColor(Vec3d color, float sunHeight) {
            return color;
        }

        @Override
        public boolean useThickFog(int camX, int camY) {
            return true;
        }
    }

    @Environment(value=EnvType.CLIENT)
    public static class End
    extends DimensionEffects {
        public End() {
            super(Float.NaN, false, SkyType.END, true, false);
        }

        @Override
        public Vec3d adjustFogColor(Vec3d color, float sunHeight) {
            return color.multiply(0.15f);
        }

        @Override
        public boolean useThickFog(int camX, int camY) {
            return false;
        }

        @Override
        @Nullable
        public float[] getFogColorOverride(float skyAngle, float tickDelta) {
            return null;
        }
    }
}

