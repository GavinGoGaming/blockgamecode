/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 * 
 * Could not load the following classes:
 *  net.fabricmc.api.EnvType
 *  net.fabricmc.api.Environment
 */
package net.minecraft.client.render.entity;

import com.google.common.collect.Maps;
import java.util.Map;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.client.render.entity.MobEntityRenderer;
import net.minecraft.client.render.entity.feature.MooshroomMushroomFeatureRenderer;
import net.minecraft.client.render.entity.model.CowEntityModel;
import net.minecraft.client.render.entity.model.EntityModelLayers;
import net.minecraft.entity.passive.MooshroomEntity;
import net.minecraft.util.Identifier;
import net.minecraft.util.Util;

@Environment(value=EnvType.CLIENT)
public class MooshroomEntityRenderer
extends MobEntityRenderer<MooshroomEntity, CowEntityModel<MooshroomEntity>> {
    private static final Map<MooshroomEntity.Type, Identifier> TEXTURES = Util.make(Maps.newHashMap(), map -> {
        map.put(MooshroomEntity.Type.BROWN, Identifier.ofVanilla("textures/entity/cow/brown_mooshroom.png"));
        map.put(MooshroomEntity.Type.RED, Identifier.ofVanilla("textures/entity/cow/red_mooshroom.png"));
    });

    public MooshroomEntityRenderer(EntityRendererFactory.Context arg) {
        super(arg, new CowEntityModel(arg.getPart(EntityModelLayers.MOOSHROOM)), 0.7f);
        this.addFeature(new MooshroomMushroomFeatureRenderer<MooshroomEntity>(this, arg.getBlockRenderManager()));
    }

    @Override
    public Identifier getTexture(MooshroomEntity arg) {
        return TEXTURES.get(arg.getVariant());
    }
}

