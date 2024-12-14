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
import net.minecraft.client.render.entity.EntityRenderer;
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.client.render.entity.ItemEntityRenderer;
import net.minecraft.client.render.item.ItemRenderer;
import net.minecraft.client.texture.SpriteAtlasTexture;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.OminousItemSpawnerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RotationAxis;
import net.minecraft.world.World;

@Environment(value=EnvType.CLIENT)
public class OminousItemSpawnerEntityRenderer
extends EntityRenderer<OminousItemSpawnerEntity> {
    private static final float field_50231 = 40.0f;
    private static final int field_50232 = 50;
    private final ItemRenderer itemRenderer;

    protected OminousItemSpawnerEntityRenderer(EntityRendererFactory.Context arg) {
        super(arg);
        this.itemRenderer = arg.getItemRenderer();
    }

    @Override
    public Identifier getTexture(OminousItemSpawnerEntity arg) {
        return SpriteAtlasTexture.BLOCK_ATLAS_TEXTURE;
    }

    @Override
    public void render(OminousItemSpawnerEntity arg, float f, float g, MatrixStack arg2, VertexConsumerProvider arg3, int i) {
        ItemStack lv = arg.getItem();
        if (lv.isEmpty()) {
            return;
        }
        arg2.push();
        if (arg.age <= 50) {
            float h = Math.min((float)arg.age + g, 50.0f) / 50.0f;
            arg2.scale(h, h, h);
        }
        World lv2 = arg.getWorld();
        float j = MathHelper.wrapDegrees(lv2.getTime() - 1L) * 40.0f;
        float k = MathHelper.wrapDegrees(lv2.getTime()) * 40.0f;
        arg2.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(MathHelper.lerpAngleDegrees(g, j, k)));
        ItemEntityRenderer.renderStack(this.itemRenderer, arg2, arg3, 0xF000F0, lv, lv2.random, lv2);
        arg2.pop();
    }
}

