/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 * 
 * Could not load the following classes:
 *  net.fabricmc.api.EnvType
 *  net.fabricmc.api.Environment
 *  org.jetbrains.annotations.Nullable
 */
package net.minecraft.client.realms.util;

import com.google.common.collect.Maps;
import com.mojang.logging.LogUtils;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.Base64;
import java.util.Map;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.texture.MissingSprite;
import net.minecraft.client.texture.NativeImage;
import net.minecraft.client.texture.NativeImageBackedTexture;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.Nullable;
import org.lwjgl.system.MemoryUtil;
import org.slf4j.Logger;

@Environment(value=EnvType.CLIENT)
public class RealmsTextureManager {
    private static final Map<String, RealmsTexture> TEXTURES = Maps.newHashMap();
    private static final Logger LOGGER = LogUtils.getLogger();
    private static final Identifier ISLES = Identifier.ofVanilla("textures/gui/presets/isles.png");

    public static Identifier getTextureId(String id, @Nullable String image) {
        if (image == null) {
            return ISLES;
        }
        return RealmsTextureManager.getTextureIdInternal(id, image);
    }

    private static Identifier getTextureIdInternal(String id, String image) {
        RealmsTexture lv = TEXTURES.get(id);
        if (lv != null && lv.image().equals(image)) {
            return lv.textureId;
        }
        NativeImage lv2 = RealmsTextureManager.loadImage(image);
        if (lv2 == null) {
            Identifier lv3 = MissingSprite.getMissingSpriteId();
            TEXTURES.put(id, new RealmsTexture(image, lv3));
            return lv3;
        }
        Identifier lv3 = Identifier.of("realms", "dynamic/" + id);
        MinecraftClient.getInstance().getTextureManager().registerTexture(lv3, new NativeImageBackedTexture(lv2));
        TEXTURES.put(id, new RealmsTexture(image, lv3));
        return lv3;
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @Nullable
    private static NativeImage loadImage(String image) {
        byte[] bs = Base64.getDecoder().decode(image);
        ByteBuffer byteBuffer = MemoryUtil.memAlloc(bs.length);
        try {
            NativeImage nativeImage = NativeImage.read(byteBuffer.put(bs).flip());
            return nativeImage;
        } catch (IOException iOException) {
            LOGGER.warn("Failed to load world image: {}", (Object)image, (Object)iOException);
        } finally {
            MemoryUtil.memFree(byteBuffer);
        }
        return null;
    }

    @Environment(value=EnvType.CLIENT)
    public record RealmsTexture(String image, Identifier textureId) {
    }
}

