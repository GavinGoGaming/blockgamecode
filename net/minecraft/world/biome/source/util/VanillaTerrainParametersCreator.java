/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.world.biome.source.util;

import net.minecraft.util.function.ToFloatFunction;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Spline;
import net.minecraft.world.gen.densityfunction.DensityFunctions;

public class VanillaTerrainParametersCreator {
    private static final float field_38024 = -0.51f;
    private static final float field_38025 = -0.4f;
    private static final float field_38026 = 0.1f;
    private static final float field_38027 = -0.15f;
    private static final ToFloatFunction<Float> IDENTITY = ToFloatFunction.IDENTITY;
    private static final ToFloatFunction<Float> OFFSET_AMPLIFIER = ToFloatFunction.fromFloat(value -> value < 0.0f ? value : value * 2.0f);
    private static final ToFloatFunction<Float> FACTOR_AMPLIFIER = ToFloatFunction.fromFloat(value -> 1.25f - 6.25f / (value + 5.0f));
    private static final ToFloatFunction<Float> JAGGEDNESS_AMPLIFIER = ToFloatFunction.fromFloat(value -> value * 2.0f);

    public static <C, I extends ToFloatFunction<C>> Spline<C, I> createOffsetSpline(I continents, I erosion, I ridgesFolded, boolean amplified) {
        ToFloatFunction<Float> lv = amplified ? OFFSET_AMPLIFIER : IDENTITY;
        Spline<C, I> lv2 = VanillaTerrainParametersCreator.createContinentalOffsetSpline(erosion, ridgesFolded, -0.15f, 0.0f, 0.0f, 0.1f, 0.0f, -0.03f, false, false, lv);
        Spline<C, I> lv3 = VanillaTerrainParametersCreator.createContinentalOffsetSpline(erosion, ridgesFolded, -0.1f, 0.03f, 0.1f, 0.1f, 0.01f, -0.03f, false, false, lv);
        Spline<C, I> lv4 = VanillaTerrainParametersCreator.createContinentalOffsetSpline(erosion, ridgesFolded, -0.1f, 0.03f, 0.1f, 0.7f, 0.01f, -0.03f, true, true, lv);
        Spline<C, I> lv5 = VanillaTerrainParametersCreator.createContinentalOffsetSpline(erosion, ridgesFolded, -0.05f, 0.03f, 0.1f, 1.0f, 0.01f, 0.01f, true, true, lv);
        return Spline.builder(continents, lv).add(-1.1f, 0.044f).add(-1.02f, -0.2222f).add(-0.51f, -0.2222f).add(-0.44f, -0.12f).add(-0.18f, -0.12f).add(-0.16f, lv2).add(-0.15f, lv2).add(-0.1f, lv3).add(0.25f, lv4).add(1.0f, lv5).build();
    }

    public static <C, I extends ToFloatFunction<C>> Spline<C, I> createFactorSpline(I continents, I erosion, I ridges, I ridgesFolded, boolean amplified) {
        ToFloatFunction<Float> lv = amplified ? FACTOR_AMPLIFIER : IDENTITY;
        return Spline.builder(continents, IDENTITY).add(-0.19f, 3.95f).add(-0.15f, VanillaTerrainParametersCreator.method_42054(erosion, ridges, ridgesFolded, 6.25f, true, IDENTITY)).add(-0.1f, VanillaTerrainParametersCreator.method_42054(erosion, ridges, ridgesFolded, 5.47f, true, lv)).add(0.03f, VanillaTerrainParametersCreator.method_42054(erosion, ridges, ridgesFolded, 5.08f, true, lv)).add(0.06f, VanillaTerrainParametersCreator.method_42054(erosion, ridges, ridgesFolded, 4.69f, false, lv)).build();
    }

    public static <C, I extends ToFloatFunction<C>> Spline<C, I> createJaggednessSpline(I continents, I erosion, I ridges, I ridgesFolded, boolean amplified) {
        ToFloatFunction<Float> lv = amplified ? JAGGEDNESS_AMPLIFIER : IDENTITY;
        float f = 0.65f;
        return Spline.builder(continents, lv).add(-0.11f, 0.0f).add(0.03f, VanillaTerrainParametersCreator.method_42053(erosion, ridges, ridgesFolded, 1.0f, 0.5f, 0.0f, 0.0f, lv)).add(0.65f, VanillaTerrainParametersCreator.method_42053(erosion, ridges, ridgesFolded, 1.0f, 1.0f, 1.0f, 0.0f, lv)).build();
    }

    private static <C, I extends ToFloatFunction<C>> Spline<C, I> method_42053(I erosion, I ridges, I ridgesFolded, float f, float g, float h, float i, ToFloatFunction<Float> amplifier) {
        float j = -0.5775f;
        Spline<C, I> lv = VanillaTerrainParametersCreator.method_42052(ridges, ridgesFolded, f, h, amplifier);
        Spline<C, I> lv2 = VanillaTerrainParametersCreator.method_42052(ridges, ridgesFolded, g, i, amplifier);
        return Spline.builder(erosion, amplifier).add(-1.0f, lv).add(-0.78f, lv2).add(-0.5775f, lv2).add(-0.375f, 0.0f).build();
    }

    private static <C, I extends ToFloatFunction<C>> Spline<C, I> method_42052(I ridges, I ridgesFolded, float f, float g, ToFloatFunction<Float> amplifier) {
        float h = DensityFunctions.getPeaksValleysNoise(0.4f);
        float i = DensityFunctions.getPeaksValleysNoise(0.56666666f);
        float j = (h + i) / 2.0f;
        Spline.Builder<C, I> lv = Spline.builder(ridgesFolded, amplifier);
        lv.add(h, 0.0f);
        if (g > 0.0f) {
            lv.add(j, VanillaTerrainParametersCreator.method_42049(ridges, g, amplifier));
        } else {
            lv.add(j, 0.0f);
        }
        if (f > 0.0f) {
            lv.add(1.0f, VanillaTerrainParametersCreator.method_42049(ridges, f, amplifier));
        } else {
            lv.add(1.0f, 0.0f);
        }
        return lv.build();
    }

    private static <C, I extends ToFloatFunction<C>> Spline<C, I> method_42049(I ridges, float f, ToFloatFunction<Float> amplifier) {
        float g = 0.63f * f;
        float h = 0.3f * f;
        return Spline.builder(ridges, amplifier).add(-0.01f, g).add(0.01f, h).build();
    }

    private static <C, I extends ToFloatFunction<C>> Spline<C, I> method_42054(I erosion, I ridges, I ridgesFolded, float f, boolean bl, ToFloatFunction<Float> amplifier) {
        Spline lv = Spline.builder(ridges, amplifier).add(-0.2f, 6.3f).add(0.2f, f).build();
        Spline.Builder lv2 = Spline.builder(erosion, amplifier).add(-0.6f, lv).add(-0.5f, Spline.builder(ridges, amplifier).add(-0.05f, 6.3f).add(0.05f, 2.67f).build()).add(-0.35f, lv).add(-0.25f, lv).add(-0.1f, Spline.builder(ridges, amplifier).add(-0.05f, 2.67f).add(0.05f, 6.3f).build()).add(0.03f, lv);
        if (bl) {
            Spline lv3 = Spline.builder(ridges, amplifier).add(0.0f, f).add(0.1f, 0.625f).build();
            Spline lv4 = Spline.builder(ridgesFolded, amplifier).add(-0.9f, f).add(-0.69f, lv3).build();
            lv2.add(0.35f, f).add(0.45f, lv4).add(0.55f, lv4).add(0.62f, f);
        } else {
            Spline lv3 = Spline.builder(ridgesFolded, amplifier).add(-0.7f, lv).add(-0.15f, 1.37f).build();
            Spline lv4 = Spline.builder(ridgesFolded, amplifier).add(0.45f, lv).add(0.7f, 1.56f).build();
            lv2.add(0.05f, lv4).add(0.4f, lv4).add(0.45f, lv3).add(0.55f, lv3).add(0.58f, f);
        }
        return lv2.build();
    }

    private static float method_42047(float f, float g, float h, float i) {
        return (g - f) / (i - h);
    }

    private static <C, I extends ToFloatFunction<C>> Spline<C, I> method_42050(I ridgesFolded, float f, boolean bl, ToFloatFunction<Float> amplifier) {
        Spline.Builder lv = Spline.builder(ridgesFolded, amplifier);
        float g = -0.7f;
        float h = -1.0f;
        float i = VanillaTerrainParametersCreator.getOffsetValue(-1.0f, f, -0.7f);
        float j = 1.0f;
        float k = VanillaTerrainParametersCreator.getOffsetValue(1.0f, f, -0.7f);
        float l = VanillaTerrainParametersCreator.method_42045(f);
        float m = -0.65f;
        if (-0.65f < l && l < 1.0f) {
            float n = VanillaTerrainParametersCreator.getOffsetValue(-0.65f, f, -0.7f);
            float o = -0.75f;
            float p = VanillaTerrainParametersCreator.getOffsetValue(-0.75f, f, -0.7f);
            float q = VanillaTerrainParametersCreator.method_42047(i, p, -1.0f, -0.75f);
            lv.add(-1.0f, i, q);
            lv.add(-0.75f, p);
            lv.add(-0.65f, n);
            float r = VanillaTerrainParametersCreator.getOffsetValue(l, f, -0.7f);
            float s = VanillaTerrainParametersCreator.method_42047(r, k, l, 1.0f);
            float t = 0.01f;
            lv.add(l - 0.01f, r);
            lv.add(l, r, s);
            lv.add(1.0f, k, s);
        } else {
            float n = VanillaTerrainParametersCreator.method_42047(i, k, -1.0f, 1.0f);
            if (bl) {
                lv.add(-1.0f, Math.max(0.2f, i));
                lv.add(0.0f, MathHelper.lerp(0.5f, i, k), n);
            } else {
                lv.add(-1.0f, i, n);
            }
            lv.add(1.0f, k, n);
        }
        return lv.build();
    }

    private static float getOffsetValue(float f, float g, float h) {
        float i = 1.17f;
        float j = 0.46082947f;
        float k = 1.0f - (1.0f - g) * 0.5f;
        float l = 0.5f * (1.0f - g);
        float m = (f + 1.17f) * 0.46082947f;
        float n = m * k - l;
        if (f < h) {
            return Math.max(n, -0.2222f);
        }
        return Math.max(n, 0.0f);
    }

    private static float method_42045(float f) {
        float g = 1.17f;
        float h = 0.46082947f;
        float i = 1.0f - (1.0f - f) * 0.5f;
        float j = 0.5f * (1.0f - f);
        return j / (0.46082947f * i) - 1.17f;
    }

    public static <C, I extends ToFloatFunction<C>> Spline<C, I> createContinentalOffsetSpline(I erosion, I ridgesFolded, float continentalness, float g, float h, float i, float j, float k, boolean bl, boolean bl2, ToFloatFunction<Float> amplifier) {
        float l = 0.6f;
        float m = 0.5f;
        float n = 0.5f;
        Spline<C, I> lv = VanillaTerrainParametersCreator.method_42050(ridgesFolded, MathHelper.lerp(i, 0.6f, 1.5f), bl2, amplifier);
        Spline<C, I> lv2 = VanillaTerrainParametersCreator.method_42050(ridgesFolded, MathHelper.lerp(i, 0.6f, 1.0f), bl2, amplifier);
        Spline<C, I> lv3 = VanillaTerrainParametersCreator.method_42050(ridgesFolded, i, bl2, amplifier);
        Spline<C, I> lv4 = VanillaTerrainParametersCreator.method_42048(ridgesFolded, continentalness - 0.15f, 0.5f * i, MathHelper.lerp(0.5f, 0.5f, 0.5f) * i, 0.5f * i, 0.6f * i, 0.5f, amplifier);
        Spline<C, I> lv5 = VanillaTerrainParametersCreator.method_42048(ridgesFolded, continentalness, j * i, g * i, 0.5f * i, 0.6f * i, 0.5f, amplifier);
        Spline<C, I> lv6 = VanillaTerrainParametersCreator.method_42048(ridgesFolded, continentalness, j, j, g, h, 0.5f, amplifier);
        Spline<C, I> lv7 = VanillaTerrainParametersCreator.method_42048(ridgesFolded, continentalness, j, j, g, h, 0.5f, amplifier);
        Spline lv8 = Spline.builder(ridgesFolded, amplifier).add(-1.0f, continentalness).add(-0.4f, lv6).add(0.0f, h + 0.07f).build();
        Spline<C, I> lv9 = VanillaTerrainParametersCreator.method_42048(ridgesFolded, -0.02f, k, k, g, h, 0.0f, amplifier);
        Spline.Builder<C, I> lv10 = Spline.builder(erosion, amplifier).add(-0.85f, lv).add(-0.7f, lv2).add(-0.4f, lv3).add(-0.35f, lv4).add(-0.1f, lv5).add(0.2f, lv6);
        if (bl) {
            lv10.add(0.4f, lv7).add(0.45f, lv8).add(0.55f, lv8).add(0.58f, lv7);
        }
        lv10.add(0.7f, lv9);
        return lv10.build();
    }

    private static <C, I extends ToFloatFunction<C>> Spline<C, I> method_42048(I ridgesFolded, float continentalness, float g, float h, float i, float j, float k, ToFloatFunction<Float> amplifier) {
        float l = Math.max(0.5f * (g - continentalness), k);
        float m = 5.0f * (h - g);
        return Spline.builder(ridgesFolded, amplifier).add(-1.0f, continentalness, l).add(-0.4f, g, Math.min(l, m)).add(0.0f, h, m).add(0.4f, i, 2.0f * (i - h)).add(1.0f, j, 0.7f * (j - i)).build();
    }
}

