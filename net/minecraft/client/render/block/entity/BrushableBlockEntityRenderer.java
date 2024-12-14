/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 * 
 * Could not load the following classes:
 *  net.fabricmc.api.EnvType
 *  net.fabricmc.api.Environment
 */
package net.minecraft.client.render.block.entity;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.block.entity.BrushableBlockEntity;
import net.minecraft.client.render.OverlayTexture;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.WorldRenderer;
import net.minecraft.client.render.block.entity.BlockEntityRenderer;
import net.minecraft.client.render.block.entity.BlockEntityRendererFactory;
import net.minecraft.client.render.item.ItemRenderer;
import net.minecraft.client.render.model.json.ModelTransformationMode;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.ItemStack;
import net.minecraft.state.property.Properties;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.RotationAxis;

@Environment(value=EnvType.CLIENT)
public class BrushableBlockEntityRenderer
implements BlockEntityRenderer<BrushableBlockEntity> {
    private final ItemRenderer itemRenderer;

    public BrushableBlockEntityRenderer(BlockEntityRendererFactory.Context context) {
        this.itemRenderer = context.getItemRenderer();
    }

    @Override
    public void render(BrushableBlockEntity arg, float f, MatrixStack arg2, VertexConsumerProvider arg3, int i, int j) {
        if (arg.getWorld() == null) {
            return;
        }
        int k = arg.getCachedState().get(Properties.DUSTED);
        if (k <= 0) {
            return;
        }
        Direction lv = arg.getHitDirection();
        if (lv == null) {
            return;
        }
        ItemStack lv2 = arg.getItem();
        if (lv2.isEmpty()) {
            return;
        }
        arg2.push();
        arg2.translate(0.0f, 0.5f, 0.0f);
        float[] fs = this.getTranslation(lv, k);
        arg2.translate(fs[0], fs[1], fs[2]);
        arg2.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(75.0f));
        boolean bl = lv == Direction.EAST || lv == Direction.WEST;
        arg2.multiply(RotationAxis.POSITIVE_Y.rotationDegrees((bl ? 90 : 0) + 11));
        arg2.scale(0.5f, 0.5f, 0.5f);
        int l = WorldRenderer.getLightmapCoordinates(arg.getWorld(), arg.getCachedState(), arg.getPos().offset(lv));
        this.itemRenderer.renderItem(lv2, ModelTransformationMode.FIXED, l, OverlayTexture.DEFAULT_UV, arg2, arg3, arg.getWorld(), 0);
        arg2.pop();
    }

    private float[] getTranslation(Direction direction, int dustedLevel) {
        float[] fs = new float[]{0.5f, 0.0f, 0.5f};
        float f = (float)dustedLevel / 10.0f * 0.75f;
        switch (direction) {
            case EAST: {
                fs[0] = 0.73f + f;
                break;
            }
            case WEST: {
                fs[0] = 0.25f - f;
                break;
            }
            case UP: {
                fs[1] = 0.25f + f;
                break;
            }
            case DOWN: {
                fs[1] = -0.23f - f;
                break;
            }
            case NORTH: {
                fs[2] = 0.25f - f;
                break;
            }
            case SOUTH: {
                fs[2] = 0.73f + f;
            }
        }
        return fs;
    }
}

