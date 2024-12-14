/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 * 
 * Could not load the following classes:
 *  net.fabricmc.api.EnvType
 *  net.fabricmc.api.Environment
 */
package net.minecraft.client.texture.atlas;

import java.io.IOException;
import java.io.InputStream;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.texture.NativeImage;
import net.minecraft.resource.Resource;
import net.minecraft.util.Identifier;

@Environment(value=EnvType.CLIENT)
public class AtlasSprite {
    private final Identifier id;
    private final Resource resource;
    private final AtomicReference<NativeImage> image = new AtomicReference();
    private final AtomicInteger regionCount;

    public AtlasSprite(Identifier id, Resource resource, int regionCount) {
        this.id = id;
        this.resource = resource;
        this.regionCount = new AtomicInteger(regionCount);
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public NativeImage read() throws IOException {
        NativeImage lv = this.image.get();
        if (lv == null) {
            AtlasSprite atlasSprite = this;
            synchronized (atlasSprite) {
                lv = this.image.get();
                if (lv == null) {
                    try (InputStream inputStream = this.resource.getInputStream();){
                        lv = NativeImage.read(inputStream);
                        this.image.set(lv);
                    } catch (IOException iOException) {
                        throw new IOException("Failed to load image " + String.valueOf(this.id), iOException);
                    }
                }
            }
        }
        return lv;
    }

    public void close() {
        NativeImage lv;
        int i = this.regionCount.decrementAndGet();
        if (i <= 0 && (lv = (NativeImage)this.image.getAndSet(null)) != null) {
            lv.close();
        }
    }
}

