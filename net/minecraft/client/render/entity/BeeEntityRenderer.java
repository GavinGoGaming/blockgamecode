/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 * 
 * Could not load the following classes:
 *  net.fabricmc.api.EnvType
 *  net.fabricmc.api.Environment
 */
package net.minecraft.client.render.entity;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.client.render.entity.MobEntityRenderer;
import net.minecraft.client.render.entity.model.BeeEntityModel;
import net.minecraft.client.render.entity.model.EntityModelLayers;
import net.minecraft.entity.passive.BeeEntity;
import net.minecraft.util.Identifier;

@Environment(value=EnvType.CLIENT)
public class BeeEntityRenderer
extends MobEntityRenderer<BeeEntity, BeeEntityModel<BeeEntity>> {
    private static final Identifier ANGRY_TEXTURE = Identifier.ofVanilla("textures/entity/bee/bee_angry.png");
    private static final Identifier ANGRY_NECTAR_TEXTURE = Identifier.ofVanilla("textures/entity/bee/bee_angry_nectar.png");
    private static final Identifier PASSIVE_TEXTURE = Identifier.ofVanilla("textures/entity/bee/bee.png");
    private static final Identifier NECTAR_TEXTURE = Identifier.ofVanilla("textures/entity/bee/bee_nectar.png");

    public BeeEntityRenderer(EntityRendererFactory.Context arg) {
        super(arg, new BeeEntityModel(arg.getPart(EntityModelLayers.BEE)), 0.4f);
    }

    @Override
    public Identifier getTexture(BeeEntity arg) {
        if (arg.hasAngerTime()) {
            if (arg.hasNectar()) {
                return ANGRY_NECTAR_TEXTURE;
            }
            return ANGRY_TEXTURE;
        }
        if (arg.hasNectar()) {
            return NECTAR_TEXTURE;
        }
        return PASSIVE_TEXTURE;
    }
}

