/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 * 
 * Could not load the following classes:
 *  net.fabricmc.api.EnvType
 *  net.fabricmc.api.Environment
 */
package net.minecraft.client.texture.atlas;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.MapCodec;
import java.util.List;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.texture.atlas.AtlasSource;
import net.minecraft.client.texture.atlas.AtlasSourceType;
import net.minecraft.client.texture.atlas.DirectoryAtlasSource;
import net.minecraft.client.texture.atlas.FilterAtlasSource;
import net.minecraft.client.texture.atlas.PalettedPermutationsAtlasSource;
import net.minecraft.client.texture.atlas.SingleAtlasSource;
import net.minecraft.client.texture.atlas.UnstitchAtlasSource;
import net.minecraft.util.Identifier;

@Environment(value=EnvType.CLIENT)
public class AtlasSourceManager {
    private static final BiMap<Identifier, AtlasSourceType> SOURCE_TYPE_BY_ID = HashBiMap.create();
    public static final AtlasSourceType SINGLE = AtlasSourceManager.register("single", SingleAtlasSource.CODEC);
    public static final AtlasSourceType DIRECTORY = AtlasSourceManager.register("directory", DirectoryAtlasSource.CODEC);
    public static final AtlasSourceType FILTER = AtlasSourceManager.register("filter", FilterAtlasSource.CODEC);
    public static final AtlasSourceType UNSTITCH = AtlasSourceManager.register("unstitch", UnstitchAtlasSource.CODEC);
    public static final AtlasSourceType PALETTED_PERMUTATIONS = AtlasSourceManager.register("paletted_permutations", PalettedPermutationsAtlasSource.CODEC);
    public static Codec<AtlasSourceType> CODEC = Identifier.CODEC.flatXmap(id -> {
        AtlasSourceType lv = (AtlasSourceType)SOURCE_TYPE_BY_ID.get(id);
        return lv != null ? DataResult.success(lv) : DataResult.error(() -> "Unknown type " + String.valueOf(id));
    }, type -> {
        Identifier lv = (Identifier)SOURCE_TYPE_BY_ID.inverse().get(type);
        return type != null ? DataResult.success(lv) : DataResult.error(() -> "Unknown type " + String.valueOf(lv));
    });
    public static Codec<AtlasSource> TYPE_CODEC = CODEC.dispatch(AtlasSource::getType, AtlasSourceType::codec);
    public static Codec<List<AtlasSource>> LIST_CODEC = ((MapCodec)TYPE_CODEC.listOf().fieldOf("sources")).codec();

    private static AtlasSourceType register(String id, MapCodec<? extends AtlasSource> codec) {
        AtlasSourceType lv = new AtlasSourceType(codec);
        Identifier lv2 = Identifier.ofVanilla(id);
        AtlasSourceType lv3 = SOURCE_TYPE_BY_ID.putIfAbsent(lv2, lv);
        if (lv3 != null) {
            throw new IllegalStateException("Duplicate registration " + String.valueOf(lv2));
        }
        return lv;
    }
}

