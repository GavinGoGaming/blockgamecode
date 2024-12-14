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
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.EntityRenderer;
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.client.render.item.ItemRenderer;
import net.minecraft.client.render.model.json.ModelTransformationMode;
import net.minecraft.client.texture.SpriteAtlasTexture;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.projectile.FireworkRocketEntity;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.RotationAxis;

@Environment(value=EnvType.CLIENT)
public class FireworkRocketEntityRenderer
extends EntityRenderer<FireworkRocketEntity> {
    private final ItemRenderer itemRenderer;

    public FireworkRocketEntityRenderer(EntityRendererFactory.Context arg) {
        super(arg);
        this.itemRenderer = arg.getItemRenderer();
    }

    @Override
    public void render(FireworkRocketEntity arg, float f, float g, MatrixStack arg2, VertexConsumerProvider arg3, int i) {
        arg2.push();
        arg2.multiply(this.dispatcher.getRotation());
        if (arg.wasShotAtAngle()) {
            arg2.multiply(RotationAxis.POSITIVE_Z.rotationDegrees(180.0f));
            arg2.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(180.0f));
            arg2.multiply(RotationAxis.POSITIVE_X.rotationDegrees(90.0f));
        }
        this.itemRenderer.renderItem(arg.getStack(), ModelTransformationMode.GROUND, i, OverlayTexture.DEFAULT_UV, arg2, arg3, arg.getWorld(), arg.getId());
        arg2.pop();
        super.render(arg, f, g, arg2, arg3, i);
    }

    @Override
    public Identifier getTexture(FireworkRocketEntity arg) {
        return SpriteAtlasTexture.BLOCK_ATLAS_TEXTURE;
    }
}

