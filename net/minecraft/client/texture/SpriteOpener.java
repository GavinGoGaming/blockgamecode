/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 * 
 * Could not load the following classes:
 *  net.fabricmc.api.EnvType
 *  net.fabricmc.api.Environment
 *  org.jetbrains.annotations.Nullable
 */
package net.minecraft.client.texture;

import com.mojang.logging.LogUtils;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collection;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.resource.metadata.AnimationResourceMetadata;
import net.minecraft.client.texture.NativeImage;
import net.minecraft.client.texture.SpriteContents;
import net.minecraft.client.texture.SpriteDimensions;
import net.minecraft.resource.Resource;
import net.minecraft.resource.metadata.ResourceMetadata;
import net.minecraft.resource.metadata.ResourceMetadataReader;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.MathHelper;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;

@FunctionalInterface
@Environment(value=EnvType.CLIENT)
public interface SpriteOpener {
    public static final Logger LOGGER = LogUtils.getLogger();

    public static SpriteOpener create(Collection<ResourceMetadataReader<?>> metadatas) {
        return (id, resource) -> {
            NativeImage lv2;
            ResourceMetadata lv;
            try {
                lv = resource.getMetadata().copy(metadatas);
            } catch (Exception exception) {
                LOGGER.error("Unable to parse metadata from {}", (Object)id, (Object)exception);
                return null;
            }
            try (InputStream inputStream = resource.getInputStream();){
                lv2 = NativeImage.read(inputStream);
            } catch (IOException iOException) {
                LOGGER.error("Using missing texture, unable to load {}", (Object)id, (Object)iOException);
                return null;
            }
            AnimationResourceMetadata lv3 = lv.decode(AnimationResourceMetadata.READER).orElse(AnimationResourceMetadata.EMPTY);
            SpriteDimensions lv4 = lv3.getSize(lv2.getWidth(), lv2.getHeight());
            if (MathHelper.isMultipleOf(lv2.getWidth(), lv4.width()) && MathHelper.isMultipleOf(lv2.getHeight(), lv4.height())) {
                return new SpriteContents(id, lv4, lv2, lv);
            }
            LOGGER.error("Image {} size {},{} is not multiple of frame size {},{}", id, lv2.getWidth(), lv2.getHeight(), lv4.width(), lv4.height());
            lv2.close();
            return null;
        };
    }

    @Nullable
    public SpriteContents loadSprite(Identifier var1, Resource var2);
}

