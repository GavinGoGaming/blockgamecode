/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 * 
 * Could not load the following classes:
 *  net.fabricmc.api.EnvType
 *  net.fabricmc.api.Environment
 */
package net.minecraft.client.render.entity.model;

import com.google.common.collect.ImmutableMap;
import java.util.Map;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.model.ModelPart;
import net.minecraft.client.model.TexturedModelData;
import net.minecraft.client.render.entity.model.EntityModelLayer;
import net.minecraft.client.render.entity.model.EntityModels;
import net.minecraft.resource.ResourceManager;
import net.minecraft.resource.SynchronousResourceReloader;

@Environment(value=EnvType.CLIENT)
public class EntityModelLoader
implements SynchronousResourceReloader {
    private Map<EntityModelLayer, TexturedModelData> modelParts = ImmutableMap.of();

    public ModelPart getModelPart(EntityModelLayer layer) {
        TexturedModelData lv = this.modelParts.get(layer);
        if (lv == null) {
            throw new IllegalArgumentException("No model for layer " + String.valueOf(layer));
        }
        return lv.createModel();
    }

    @Override
    public void reload(ResourceManager manager) {
        this.modelParts = ImmutableMap.copyOf(EntityModels.getModels());
    }
}

