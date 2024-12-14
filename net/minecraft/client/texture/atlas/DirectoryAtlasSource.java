/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 * 
 * Could not load the following classes:
 *  net.fabricmc.api.EnvType
 *  net.fabricmc.api.Environment
 */
package net.minecraft.client.texture.atlas;

import com.mojang.datafixers.kinds.Applicative;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.texture.atlas.AtlasSource;
import net.minecraft.client.texture.atlas.AtlasSourceManager;
import net.minecraft.client.texture.atlas.AtlasSourceType;
import net.minecraft.resource.Resource;
import net.minecraft.resource.ResourceFinder;
import net.minecraft.resource.ResourceManager;
import net.minecraft.util.Identifier;

@Environment(value=EnvType.CLIENT)
public class DirectoryAtlasSource
implements AtlasSource {
    public static final MapCodec<DirectoryAtlasSource> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(((MapCodec)Codec.STRING.fieldOf("source")).forGetter(arg -> arg.source), ((MapCodec)Codec.STRING.fieldOf("prefix")).forGetter(arg -> arg.prefix)).apply((Applicative<DirectoryAtlasSource, ?>)instance, DirectoryAtlasSource::new));
    private final String source;
    private final String prefix;

    public DirectoryAtlasSource(String source, String prefix) {
        this.source = source;
        this.prefix = prefix;
    }

    @Override
    public void load(ResourceManager resourceManager, AtlasSource.SpriteRegions regions) {
        ResourceFinder lv = new ResourceFinder("textures/" + this.source, ".png");
        lv.findResources(resourceManager).forEach((arg3, resource) -> {
            Identifier lv = lv.toResourceId((Identifier)arg3).withPrefixedPath(this.prefix);
            regions.add(lv, (Resource)resource);
        });
    }

    @Override
    public AtlasSourceType getType() {
        return AtlasSourceManager.DIRECTORY;
    }
}

