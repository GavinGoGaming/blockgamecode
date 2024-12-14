/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 * 
 * Could not load the following classes:
 *  net.fabricmc.api.EnvType
 *  net.fabricmc.api.Environment
 */
package net.minecraft.client.render.entity;

import com.google.common.collect.Maps;
import java.util.Locale;
import java.util.Map;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.client.render.entity.MobEntityRenderer;
import net.minecraft.client.render.entity.model.AxolotlEntityModel;
import net.minecraft.client.render.entity.model.EntityModelLayers;
import net.minecraft.entity.passive.AxolotlEntity;
import net.minecraft.util.Identifier;
import net.minecraft.util.Util;

@Environment(value=EnvType.CLIENT)
public class AxolotlEntityRenderer
extends MobEntityRenderer<AxolotlEntity, AxolotlEntityModel<AxolotlEntity>> {
    private static final Map<AxolotlEntity.Variant, Identifier> TEXTURES = Util.make(Maps.newHashMap(), variants -> {
        for (AxolotlEntity.Variant lv : AxolotlEntity.Variant.values()) {
            variants.put(lv, Identifier.ofVanilla(String.format(Locale.ROOT, "textures/entity/axolotl/axolotl_%s.png", lv.getName())));
        }
    });

    public AxolotlEntityRenderer(EntityRendererFactory.Context arg) {
        super(arg, new AxolotlEntityModel(arg.getPart(EntityModelLayers.AXOLOTL)), 0.5f);
    }

    @Override
    public Identifier getTexture(AxolotlEntity arg) {
        return TEXTURES.get(arg.getVariant());
    }
}

