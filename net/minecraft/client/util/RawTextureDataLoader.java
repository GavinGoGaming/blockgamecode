/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 * 
 * Could not load the following classes:
 *  net.fabricmc.api.EnvType
 *  net.fabricmc.api.Environment
 */
package net.minecraft.client.util;

import java.io.IOException;
import java.io.InputStream;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.texture.NativeImage;
import net.minecraft.resource.ResourceManager;
import net.minecraft.util.Identifier;

@Environment(value=EnvType.CLIENT)
public class RawTextureDataLoader {
    @Deprecated
    public static int[] loadRawTextureData(ResourceManager resourceManager, Identifier id) throws IOException {
        try (InputStream inputStream = resourceManager.open(id);){
            NativeImage lv = NativeImage.read(inputStream);
            try {
                int[] nArray = lv.makePixelArray();
                if (lv != null) {
                    lv.close();
                }
                return nArray;
            } catch (Throwable throwable) {
                if (lv != null) {
                    try {
                        lv.close();
                    } catch (Throwable throwable2) {
                        throwable.addSuppressed(throwable2);
                    }
                }
                throw throwable;
            }
        }
    }
}

