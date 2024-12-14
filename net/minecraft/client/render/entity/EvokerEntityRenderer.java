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
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.client.render.entity.IllagerEntityRenderer;
import net.minecraft.client.render.entity.feature.HeldItemFeatureRenderer;
import net.minecraft.client.render.entity.model.EntityModelLayers;
import net.minecraft.client.render.entity.model.IllagerEntityModel;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.mob.SpellcastingIllagerEntity;
import net.minecraft.util.Identifier;

@Environment(value=EnvType.CLIENT)
public class EvokerEntityRenderer<T extends SpellcastingIllagerEntity>
extends IllagerEntityRenderer<T> {
    private static final Identifier TEXTURE = Identifier.ofVanilla("textures/entity/illager/evoker.png");

    public EvokerEntityRenderer(EntityRendererFactory.Context arg) {
        super(arg, new IllagerEntityModel(arg.getPart(EntityModelLayers.EVOKER)), 0.5f);
        this.addFeature(new HeldItemFeatureRenderer<T, IllagerEntityModel<T>>(this, this, arg.getHeldItemRenderer()){

            @Override
            public void render(MatrixStack arg, VertexConsumerProvider arg2, int i, T arg3, float f, float g, float h, float j, float k, float l) {
                if (((SpellcastingIllagerEntity)arg3).isSpellcasting()) {
                    super.render(arg, arg2, i, arg3, f, g, h, j, k, l);
                }
            }
        });
    }

    @Override
    public Identifier getTexture(T arg) {
        return TEXTURE;
    }
}

