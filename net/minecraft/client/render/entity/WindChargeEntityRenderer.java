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
import net.minecraft.client.render.OverlayTexture;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.EntityRenderer;
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.client.render.entity.model.EntityModelLayers;
import net.minecraft.client.render.entity.model.WindChargeEntityModel;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.projectile.AbstractWindChargeEntity;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.MathHelper;

@Environment(value=EnvType.CLIENT)
public class WindChargeEntityRenderer
extends EntityRenderer<AbstractWindChargeEntity> {
    private static final float field_52258 = MathHelper.square(3.5f);
    private static final Identifier TEXTURE = Identifier.ofVanilla("textures/entity/projectiles/wind_charge.png");
    private final WindChargeEntityModel model;

    public WindChargeEntityRenderer(EntityRendererFactory.Context arg) {
        super(arg);
        this.model = new WindChargeEntityModel(arg.getPart(EntityModelLayers.WIND_CHARGE));
    }

    @Override
    public void render(AbstractWindChargeEntity arg, float f, float g, MatrixStack arg2, VertexConsumerProvider arg3, int i) {
        if (arg.age < 2 && this.dispatcher.camera.getFocusedEntity().squaredDistanceTo(arg) < (double)field_52258) {
            return;
        }
        float h = (float)arg.age + g;
        VertexConsumer lv = arg3.getBuffer(RenderLayer.getBreezeWind(TEXTURE, this.getXOffset(h) % 1.0f, 0.0f));
        this.model.setAngles(arg, 0.0f, 0.0f, h, 0.0f, 0.0f);
        this.model.render(arg2, lv, i, OverlayTexture.DEFAULT_UV);
        super.render(arg, f, g, arg2, arg3, i);
    }

    protected float getXOffset(float tickDelta) {
        return tickDelta * 0.03f;
    }

    @Override
    public Identifier getTexture(AbstractWindChargeEntity arg) {
        return TEXTURE;
    }
}

