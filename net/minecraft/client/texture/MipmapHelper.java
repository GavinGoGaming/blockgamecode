/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 * 
 * Could not load the following classes:
 *  net.fabricmc.api.EnvType
 *  net.fabricmc.api.Environment
 */
package net.minecraft.client.texture;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.texture.NativeImage;
import net.minecraft.util.Util;

@Environment(value=EnvType.CLIENT)
public class MipmapHelper {
    private static final int MIN_ALPHA = 96;
    private static final float[] COLOR_FRACTIONS = Util.make(new float[256], list -> {
        for (int i = 0; i < ((float[])list).length; ++i) {
            list[i] = (float)Math.pow((float)i / 255.0f, 2.2);
        }
    });

    private MipmapHelper() {
    }

    public static NativeImage[] getMipmapLevelsImages(NativeImage[] originals, int mipmap) {
        if (mipmap + 1 <= originals.length) {
            return originals;
        }
        NativeImage[] lvs = new NativeImage[mipmap + 1];
        lvs[0] = originals[0];
        boolean bl = MipmapHelper.hasAlpha(lvs[0]);
        for (int j = 1; j <= mipmap; ++j) {
            if (j < originals.length) {
                lvs[j] = originals[j];
                continue;
            }
            NativeImage lv = lvs[j - 1];
            NativeImage lv2 = new NativeImage(lv.getWidth() >> 1, lv.getHeight() >> 1, false);
            int k = lv2.getWidth();
            int l = lv2.getHeight();
            for (int m = 0; m < k; ++m) {
                for (int n = 0; n < l; ++n) {
                    lv2.setColor(m, n, MipmapHelper.blend(lv.getColor(m * 2 + 0, n * 2 + 0), lv.getColor(m * 2 + 1, n * 2 + 0), lv.getColor(m * 2 + 0, n * 2 + 1), lv.getColor(m * 2 + 1, n * 2 + 1), bl));
                }
            }
            lvs[j] = lv2;
        }
        return lvs;
    }

    private static boolean hasAlpha(NativeImage image) {
        for (int i = 0; i < image.getWidth(); ++i) {
            for (int j = 0; j < image.getHeight(); ++j) {
                if (image.getColor(i, j) >> 24 != 0) continue;
                return true;
            }
        }
        return false;
    }

    private static int blend(int one, int two, int three, int four, boolean checkAlpha) {
        if (checkAlpha) {
            float f = 0.0f;
            float g = 0.0f;
            float h = 0.0f;
            float m = 0.0f;
            if (one >> 24 != 0) {
                f += MipmapHelper.getColorFraction(one >> 24);
                g += MipmapHelper.getColorFraction(one >> 16);
                h += MipmapHelper.getColorFraction(one >> 8);
                m += MipmapHelper.getColorFraction(one >> 0);
            }
            if (two >> 24 != 0) {
                f += MipmapHelper.getColorFraction(two >> 24);
                g += MipmapHelper.getColorFraction(two >> 16);
                h += MipmapHelper.getColorFraction(two >> 8);
                m += MipmapHelper.getColorFraction(two >> 0);
            }
            if (three >> 24 != 0) {
                f += MipmapHelper.getColorFraction(three >> 24);
                g += MipmapHelper.getColorFraction(three >> 16);
                h += MipmapHelper.getColorFraction(three >> 8);
                m += MipmapHelper.getColorFraction(three >> 0);
            }
            if (four >> 24 != 0) {
                f += MipmapHelper.getColorFraction(four >> 24);
                g += MipmapHelper.getColorFraction(four >> 16);
                h += MipmapHelper.getColorFraction(four >> 8);
                m += MipmapHelper.getColorFraction(four >> 0);
            }
            int n = (int)(Math.pow(f /= 4.0f, 0.45454545454545453) * 255.0);
            int o = (int)(Math.pow(g /= 4.0f, 0.45454545454545453) * 255.0);
            int p = (int)(Math.pow(h /= 4.0f, 0.45454545454545453) * 255.0);
            int q = (int)(Math.pow(m /= 4.0f, 0.45454545454545453) * 255.0);
            if (n < 96) {
                n = 0;
            }
            return n << 24 | o << 16 | p << 8 | q;
        }
        int r = MipmapHelper.getColorComponent(one, two, three, four, 24);
        int s = MipmapHelper.getColorComponent(one, two, three, four, 16);
        int t = MipmapHelper.getColorComponent(one, two, three, four, 8);
        int u = MipmapHelper.getColorComponent(one, two, three, four, 0);
        return r << 24 | s << 16 | t << 8 | u;
    }

    private static int getColorComponent(int one, int two, int three, int four, int bits) {
        float f = MipmapHelper.getColorFraction(one >> bits);
        float g = MipmapHelper.getColorFraction(two >> bits);
        float h = MipmapHelper.getColorFraction(three >> bits);
        float n = MipmapHelper.getColorFraction(four >> bits);
        float o = (float)((double)((float)Math.pow((double)(f + g + h + n) * 0.25, 0.45454545454545453)));
        return (int)((double)o * 255.0);
    }

    private static float getColorFraction(int value) {
        return COLOR_FRACTIONS[value & 0xFF];
    }
}

